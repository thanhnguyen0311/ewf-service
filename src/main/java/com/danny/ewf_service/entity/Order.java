package com.danny.ewf_service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "type")
    private String type;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "ship_date")
    private LocalDateTime shipDate;

    @Column(name = "date_created")
    private LocalDateTime dataCreated;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "prices_id", referencedColumnName = "id") // FK column in Order table
    private OrderPrices orderPrices ;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public void addToMetadata(String key, Object value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> metadataMap = this.getMetadataAsMap();
            metadataMap.put(key, value);
            this.metadata = objectMapper.writeValueAsString(metadataMap); // Convert map back to JSON string
        } catch (IOException e) {
            throw new RuntimeException("Failed to add metadata key-value pair", e);
        }
    }

    public Map<String, Object> getMetadataAsMap() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return this.metadata == null ? new HashMap<>() : objectMapper.readValue(this.metadata, new TypeReference<Map<String, Object>>() { });
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse metadata JSON", e);
        }
    }


}
