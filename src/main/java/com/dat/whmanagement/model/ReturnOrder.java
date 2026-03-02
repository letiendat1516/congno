package com.dat.whmanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Phiếu trả hàng từ khách */
public class ReturnOrder {

    private Integer   id;
    private String    orderNumber;
    private int       customerId;
    private String    customerName;
    private LocalDate returnDate;
    private double    totalAmount;
    private String    note;
    private double    deductedFromTotal;
    private double    deductedFromPaid;
    private List<ReturnOrderDetail> details = new ArrayList<>();

    public ReturnOrder() {}

    public Integer   getId()                              { return id; }
    public void      setId(Integer id)                    { this.id = id; }

    public String    getOrderNumber()                     { return orderNumber; }
    public void      setOrderNumber(String n)             { this.orderNumber = n; }

    public int       getCustomerId()                      { return customerId; }
    public void      setCustomerId(int id)                { this.customerId = id; }

    public String    getCustomerName()                    { return customerName; }
    public void      setCustomerName(String n)            { this.customerName = n; }

    public LocalDate getReturnDate()                      { return returnDate; }
    public void      setReturnDate(LocalDate d)           { this.returnDate = d; }

    public double    getTotalAmount()                     { return totalAmount; }
    public void      setTotalAmount(double t)             { this.totalAmount = t; }

    public String    getNote()                            { return note; }
    public void      setNote(String n)                    { this.note = n; }

    public double    getDeductedFromTotal()               { return deductedFromTotal; }
    public void      setDeductedFromTotal(double v)       { this.deductedFromTotal = v; }

    public double    getDeductedFromPaid()                { return deductedFromPaid; }
    public void      setDeductedFromPaid(double v)        { this.deductedFromPaid = v; }

    public List<ReturnOrderDetail> getDetails()                           { return details; }
    public void                    setDetails(List<ReturnOrderDetail> d)  { this.details = d; }
}

