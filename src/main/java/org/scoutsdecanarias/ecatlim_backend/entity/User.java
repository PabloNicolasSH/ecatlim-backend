package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.enums.Role;

import java.util.List;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String password;

    private String nif;
    private String email;
    private String phone;

    private String address;
    private String city;
    private String country;

    private Integer census;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "scout_group_id")
    private ScoutGroup scoutGroup;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLessonBlock> lessonBlocks;

    private boolean enabled = true;
}
