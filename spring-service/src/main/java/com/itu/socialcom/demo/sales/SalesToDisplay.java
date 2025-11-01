package com.itu.socialcom.demo.sales;

import lombok.Data;

import java.util.List;

@Data
public class SalesToDisplay {
    List<Sales> sales;
    int totalSales;
}

