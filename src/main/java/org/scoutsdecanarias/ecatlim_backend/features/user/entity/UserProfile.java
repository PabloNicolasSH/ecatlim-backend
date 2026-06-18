package org.scoutsdecanarias.ecatlim_backend.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;

@Getter
@Setter
@Entity
public class UserProfile {

    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String profilePictureUrl;

    private String nif;
    private String phone;
    private String address;
    private String city;
    private String country;
    private Integer census;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scout_group_id")
    private ScoutGroup scoutGroup;
}
