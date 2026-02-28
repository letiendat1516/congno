package com.dat.whmanagement.model;

import java.time.LocalDateTime;

public class Product {

    private Integer id;
    private String code;
    private String name;
    private String unit;
    private double stock;          // tồn kho hiện tại
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(String code, String name, String unit) {
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.createdAt = LocalDateTime.now();
    }

    public Product(Integer id, String code, String name, String unit, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getStock() { return stock; }
    public void   setStock(double stock) { this.stock = stock; }
}