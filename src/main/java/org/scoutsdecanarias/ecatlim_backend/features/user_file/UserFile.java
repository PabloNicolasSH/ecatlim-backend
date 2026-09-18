package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UserFileType fileType;

    @Column(nullable = false)
    private String mimeType;

    private String customName;

    @Column(nullable = false)
    private ZonedDateTime uploadDate;
}