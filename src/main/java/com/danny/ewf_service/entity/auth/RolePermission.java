package com.danny.ewf_service.entity.auth;


import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "role_permission",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"role_id", "permission_id"})
        })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "role_id", nullable = false, referencedColumnName = "id")
        private Product product;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "permission_id", nullable = false, referencedColumnName = "id")
        private Component component;
}
