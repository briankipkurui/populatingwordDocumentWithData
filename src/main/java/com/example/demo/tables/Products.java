package com.example.demo.tables;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Products {
    private Long id;
    private int availableQuantity;
    private double sellingPrice;
    private double buyingPrice;
    private String category;
    private double sellingPriceAtDebt;
    private String item;

    public Products(Long id,
                    int availableQuantity,
                    double sellingPrice,
                    double buyingPrice,
                    String category,
                    double sellingPriceAtDebt,
                    String item) {
        this.id = id;
        this.availableQuantity = availableQuantity;
        this.sellingPrice = sellingPrice;
        this.buyingPrice = buyingPrice;
        this.category = category;
        this.sellingPriceAtDebt = sellingPriceAtDebt;
        this.item = item;
    }
}
