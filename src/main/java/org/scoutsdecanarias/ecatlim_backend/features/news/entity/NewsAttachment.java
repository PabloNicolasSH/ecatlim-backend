package org.scoutsdecanarias.ecatlim_backend.features.news.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class NewsAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_id")
    private News news;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String blobPath;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private long sizeBytes;
}
