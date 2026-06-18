package org.scoutsdecanarias.ecatlim_backend.features.scout_group;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;

import java.util.List;

@Getter
@Setter
@Entity
public class ScoutGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int provinceId;

    @Column(nullable = false)
    private int groupNumber;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "scoutGroup")
    private List<UserProfile> groupMembers;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_education_id")
    private UserProfile headOfEducation;
}
