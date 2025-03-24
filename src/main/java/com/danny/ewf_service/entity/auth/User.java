package com.danny.ewf_service.entity.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    private Role role;

    @Column(name= "first_name",nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name",nullable = false, length = 50)
    private String lastName;

    @Column(name = "email",unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name= "active")
    private Boolean active;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registered_at", updatable = false)
    private java.util.Date registeredAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private java.util.Date lastLogin;

}
