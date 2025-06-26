package com.danny.ewf_service.entity;
import com.danny.ewf_service.entity.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lpn_logs")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LpnLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lpn_id", nullable = false)
    private LPN lpn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "previous_bay_location")
    private String previousBayLocation;

    @Column(name = "new_bay_location")
    private String newBayLocation;

    @Column(name = "previous_quantity")
    private Long previousQuantity;

    @Column(name = "new_quantity")
    private Long newQuantity;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "log_date")
    private LocalDateTime logDate;

    @Column(name = "remarks")
    private String remarks;

}
