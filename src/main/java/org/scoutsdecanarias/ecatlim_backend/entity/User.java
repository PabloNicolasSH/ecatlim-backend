package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String surname;

    private String password;

    private String identityCardNumber;
    private String email;
    private String phone;

    private String address;
    private String city;
    private String country;

    private String census;

    @ManyToOne
    private ScoutGroup scoutGroup;
}
