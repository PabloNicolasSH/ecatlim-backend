package org.scoutsdecanarias.ecatlim_backend.features.recognition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionMessageKind;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class RecognitionMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private RecognitionRequest request;

    @ManyToOne(optional = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecognitionMessageKind kind;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "recognition_message_files",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<UserFile> files = new ArrayList<>();
}
