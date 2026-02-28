package com.dat.whmanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesOrder {

    private Integer id;
    private String orderNumber;
    private int customerId;
    private String customerName; // for display
    private LocalDate orderDate;
    private double totalAmount;
    private double paidAmount;
    private String note;
    private List<SalesOrderDetail> details = new ArrayList<>();

    public SalesOrder() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<SalesOrderDetail> getDetails() { return details; }
    public void setDetails(List<SalesOrderDetail> details) { this.details = details; }
}

