package com.erp.Enterprise_Resource_Planning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Employment employment;
}
