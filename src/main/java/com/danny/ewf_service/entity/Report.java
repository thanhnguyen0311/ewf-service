package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Setter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="sales_report")
    private Long salesReport = 0L;

    @Column(name="on_po")
    private Long onPO = 0L;

    @Column(name="in_transit")
    private Long inTransit = 0L;

    @Column(name="to_ship")
    private Long toShip = 0L;

    @Column(name="stock_vn")
    private Long stockVN = 0L;

    @Column(name="in_production")
    private Long inProduction = 0L;

    @Column(name="missing_report")
    private Long missingReport = 0L;



    @Column(name="overstock_report")
    private Long overstockReport = 0L;

    @Column(name="poor_selling_report")
    private Long poorSellingReport = 0L;
}
