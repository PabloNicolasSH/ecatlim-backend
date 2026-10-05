package org.scoutsdecanarias.ecatlim_backend.features.recognition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionType;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class RecognitionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private LessonBlock lessonBlock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecognitionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecognitionStatus status;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    /** Staff members chosen by management to review and answer this request. */
    @ManyToMany
    @JoinTable(name = "recognition_request_commission",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> commission = new HashSet<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<RecognitionMessage> messages = new ArrayList<>();

    public void addMessage(RecognitionMessage message) {
        message.setRequest(this);
        messages.add(message);
        updatedAt = message.getCreatedAt();
    }
}
