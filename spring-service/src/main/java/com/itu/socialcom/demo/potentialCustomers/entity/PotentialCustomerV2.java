package com.itu.socialcom.demo.potentialCustomers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "potential_customers_v2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialCustomerV2 {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id_pc", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "link_to_profile")
    private String linkToProfile;

    @Column(name = "d_platform", length = 50)
    private String platform;

    @Column(name = "identifier_on_platform", length = 50, nullable = false)
    private String identifierOnPlatform;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "id_sp", nullable = false)
    private Long supportedPlatform;



}

