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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lpn_id", nullable = false) // Foreign key to the LPN table.
    private LPN lpn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Foreign key to the LPN table.
    private User user;

    @Column(name = "method", nullable = false)
    private String method; // Method or operation that triggered the log (e.g., CREATE, UPDATE).

    @Column(name = "previous_bay_location")
    private String previousBayLocation; // Previous bay location of the LPN.

    @Column(name = "new_bay_location")
    private String newBayLocation; // Updated bay location of the LPN.

    @Column(name = "previous_quantity")
    private Long previousQuantity; // Previous quantity of the LPN.

    @Column(name = "new_quantity")
    private Long newQuantity; // Updated quantity of the LPN.

    @Column(name = "previous_status")
    private String previousStatus; // Previous status of the LPN.

    @Column(name = "new_status")
    private String newStatus; // Updated status of the LPN.

    @Column(name = "log_date")
    private LocalDateTime logDate; // The timestamp for when the log was created.

    @Column(name = "remarks")
    private String remarks; // Optional remarks or comments about the log entry.

}
