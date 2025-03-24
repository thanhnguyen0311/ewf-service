package com.danny.ewf_service.entity.auth;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "permission")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 75)
    private String title;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TINYTEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;


    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

}
