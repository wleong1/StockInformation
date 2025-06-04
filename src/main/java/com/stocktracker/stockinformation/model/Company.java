package com.stocktracker.stockinformation.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "company_id")
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String ticker;

    @Column(nullable = false, name = "company_name")
    private String companyName;

    public Company() {}

}
