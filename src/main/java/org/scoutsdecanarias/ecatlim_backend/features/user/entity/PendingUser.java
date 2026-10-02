package org.scoutsdecanarias.ecatlim_backend.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;

@Getter
@Setter
@Entity
public class PendingUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nif;

    @ManyToOne
    private ScoutGroup scoutGroup;
}