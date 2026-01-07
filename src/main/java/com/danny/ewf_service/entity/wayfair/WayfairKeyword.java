package com.danny.ewf_service.entity.wayfair;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wayfair_keyword")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "keyword_id",  nullable = false, unique = true)
    private Long KeywordId;

    @Column(name = "keyword_value", length = 255, nullable = false)
    private String keywordValue;

    @Column(name = "default_bid", nullable = false)
    private Double defaultBid = 0.0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "type")
    private String type;

}
