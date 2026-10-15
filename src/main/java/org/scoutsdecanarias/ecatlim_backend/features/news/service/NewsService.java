package org.scoutsdecanarias.ecatlim_backend.features.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.news.dto.NewsFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.news.entity.News;
import org.scoutsdecanarias.ecatlim_backend.features.news.entity.NewsAttachment;
import org.scoutsdecanarias.ecatlim_backend.features.news.repository.NewsRepository;
import org.scoutsdecanarias.ecatlim_backend.core.auth.SecurityUtils;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.notification.service.NotificationService;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileNames;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {

    private static final int MAX_ATTACHMENTS = 10;

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final BlobStorageService blobStorageService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<News> getLatest(int page, int size) {
        return newsRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(page, size));
    }

    public News create(NewsFormDto form, MultipartFile image) throws IOException {
        News news = new News();
        news.setAuthor(userRepository.findByEmail(SecurityUtils.getLoggedUsername()).orElse(null));
        news.setCreatedAt(LocalDateTime.now());
        applyForm(news, form);
        storeImage(news, image);
        News saved = newsRepository.save(news);

        List<Integer> recipients = userRepository.findAllByEnabled(true).stream().map(User::getId).toList();
        notificationService.notifyUsers(
                recipients,
                NotificationType.NEWS_PUBLISHED,
                "Nueva noticia: " + saved.getTitle(),
                excerpt(saved.getContent()),
                "/app/home",
                false,
                saved.getId()
        );

        log.info("METHOD create() - News created {}", saved.getId());
        return saved;
    }

    public News update(Integer id, NewsFormDto form, MultipartFile image) throws IOException {
        News news = findById(id);
        String previousImage = news.getImageBlobPath();

        applyForm(news, form);
        news.setUpdatedAt(LocalDateTime.now());

        boolean newImage = image != null && !image.isEmpty();
        if (newImage) {
            storeImage(news, image);
        } else if (form.removeImage()) {
            news.setImageBlobPath(null);
            news.setImageMimeType(null);
        }

        News saved = newsRepository.saveAndFlush(news);

        if (previousImage != null && !previousImage.equals(saved.getImageBlobPath())) {
            blobStorageService.delete(previousImage, BlobDirectory.NEWS_IMAGES);
        }

        log.info("METHOD update() - News updated {}", id);
        return saved;
    }

    public void delete(Integer id) {
        News news = findById(id);
        String image = news.getImageBlobPath();
        List<String> attachments = news.getAttachments().stream().map(NewsAttachment::getBlobPath).toList();

        newsRepository.delete(news);
        newsRepository.flush();

        if (image != null) {
            blobStorageService.delete(image, BlobDirectory.NEWS_IMAGES);
        }
        attachments.forEach(blobPath -> blobStorageService.delete(blobPath, BlobDirectory.NEWS_ATTACHMENTS));

        log.info("METHOD delete() - News deleted {}", id);
    }

    public News addAttachment(Integer id, MultipartFile file) throws IOException {
        News news = findById(id);
        if (file == null || file.isEmpty()) {
            throw new EcatlimException("El archivo es obligatorio", HttpStatus.BAD_REQUEST);
        }
        if (news.getAttachments().size() >= MAX_ATTACHMENTS) {
            throw new EcatlimException("Una noticia no puede tener más de " + MAX_ATTACHMENTS + " adjuntos", HttpStatus.BAD_REQUEST);
        }

        var response = blobStorageService.upload(file, BlobDirectory.NEWS_ATTACHMENTS, FileNames.randomBlobName(file.getOriginalFilename()));

        NewsAttachment attachment = new NewsAttachment();
        attachment.setNews(news);
        attachment.setName(fileName(file));
        attachment.setBlobPath(response.fileName());
        attachment.setMimeType(mimeType(file));
        attachment.setSizeBytes(file.getSize());
        news.getAttachments().add(attachment);

        return newsRepository.save(news);
    }

    public News removeAttachment(Integer id, Integer attachmentId) {
        News news = findById(id);
        NewsAttachment attachment = news.getAttachments().stream()
                .filter(a -> a.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado"));

        news.getAttachments().remove(attachment);
        News saved = newsRepository.saveAndFlush(news);
        blobStorageService.delete(attachment.getBlobPath(), BlobDirectory.NEWS_ATTACHMENTS);
        return saved;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> image(Integer id, boolean thumbnail) {
        News news = findById(id);
        if (!news.hasImage()) {
            throw new ResourceNotFoundException("La noticia no tiene imagen");
        }
        byte[] bytes = thumbnail
                ? blobStorageService.downloadThumbnail(news.getImageBlobPath(), BlobDirectory.NEWS_IMAGES)
                : blobStorageService.download(news.getImageBlobPath(), BlobDirectory.NEWS_IMAGES);
        return new FileTransferDto(bytes, news.getImageBlobPath(), news.getImageMimeType()).asResponseEntity();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadAttachment(Integer id, Integer attachmentId) {
        NewsAttachment attachment = findById(id).getAttachments().stream()
                .filter(a -> a.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado"));
        byte[] bytes = blobStorageService.download(attachment.getBlobPath(), BlobDirectory.NEWS_ATTACHMENTS);
        return new FileTransferDto(bytes, attachment.getName(), attachment.getMimeType()).asResponseEntity();
    }

    private News findById(Integer id) {
        return newsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada"));
    }

    private void applyForm(News news, NewsFormDto form) {
        news.setTitle(form.title().trim());
        news.setContent(form.content().trim());
    }

    private void storeImage(News news, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return;
        }
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new EcatlimException("La portada debe ser una imagen", HttpStatus.BAD_REQUEST);
        }
        var response = blobStorageService.upload(image, BlobDirectory.NEWS_IMAGES, FileNames.randomBlobName(image.getOriginalFilename()));
        news.setImageBlobPath(response.fileName());
        news.setImageMimeType(image.getContentType());
    }

    private String fileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            return "adjunto";
        }
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }

    private String mimeType(MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private String excerpt(String content) {
        String singleLine = content.replaceAll("\\s+", " ").trim();
        return singleLine.length() > 200 ? singleLine.substring(0, 197) + "..." : singleLine;
    }
}
