package org.scoutsdecanarias.ecatlim_backend.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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
    private List<User> groupMembers;
}
