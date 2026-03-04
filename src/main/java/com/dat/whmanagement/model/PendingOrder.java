package com.dat.whmanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Đơn đặt hàng chờ xuất từ khách hàng */
public class PendingOrder {

    private Integer   id;
    private String    orderNumber;
    private int       customerId;
    private String    customerName;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private double    totalAmount;
    private double    vatRate = 10;   // % thuế, mặc định 10
    private String    status;        // PENDING | EXPORTED | CANCELLED
    private String    note;
    private List<PendingOrderDetail> details = new ArrayList<>();

    public PendingOrder() {}

    public Integer   getId()              { return id; }
    public void      setId(Integer id)    { this.id = id; }

    public String    getOrderNumber()                  { return orderNumber; }
    public void      setOrderNumber(String n)          { this.orderNumber = n; }

    public int       getCustomerId()                   { return customerId; }
    public void      setCustomerId(int id)             { this.customerId = id; }

    public String    getCustomerName()                 { return customerName; }
    public void      setCustomerName(String n)         { this.customerName = n; }

    public LocalDate getOrderDate()                    { return orderDate; }
    public void      setOrderDate(LocalDate d)         { this.orderDate = d; }

    public LocalDate getExpectedDate()                 { return expectedDate; }
    public void      setExpectedDate(LocalDate d)      { this.expectedDate = d; }

    public double    getTotalAmount()                  { return totalAmount; }
    public void      setTotalAmount(double t)          { this.totalAmount = t; }

    public double    getVatRate()                      { return vatRate; }
    public void      setVatRate(double v)              { this.vatRate = v; }

    public String    getStatus()                       { return status; }
    public void      setStatus(String s)               { this.status = s; }

    public String    getNote()                         { return note; }
    public void      setNote(String n)                 { this.note = n; }

    public List<PendingOrderDetail> getDetails()                              { return details; }
    public void                     setDetails(List<PendingOrderDetail> d)    { this.details = d; }
}

