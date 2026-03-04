package com.dat.whmanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hóa đơn bán hàng (VAT Invoice).
 */
public class Invoice {

    public enum Status { DRAFT, PAID, CANCELLED }

    private int id;
    private String invoiceNo;
    private String invoiceSymbol;
    private String invoiceFormNumber;
    private LocalDate issueDate;
    private Status status;
    private String notes;
    private int salesOrderId;

    // Seller
    private String sellerName;
    private String sellerTaxCode;
    private String sellerAddress;
    private String sellerBankAccount;
    private String sellerPhone;

    // Buyer
    private String buyerName;
    private String buyerCompany;
    private String buyerTaxCode;
    private String buyerAddress;
    private String buyerBankAccount;
    private String paymentMethod;

    // Totals
    private double subtotal;
    private double vatRate;
    private double vatAmount;
    private double totalAmount;
    private String amountInWords;

    private int customerId;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public String getInvoiceNumber() { return invoiceNo; }
    public void setInvoiceNumber(String n) { this.invoiceNo = n; }

    public String getInvoiceSymbol() { return invoiceSymbol; }
    public void setInvoiceSymbol(String invoiceSymbol) { this.invoiceSymbol = invoiceSymbol; }

    public String getInvoiceFormNumber() { return invoiceFormNumber; }
    public void setInvoiceFormNumber(String invoiceFormNumber) { this.invoiceFormNumber = invoiceFormNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(int salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerTaxCode() { return sellerTaxCode; }
    public void setSellerTaxCode(String sellerTaxCode) { this.sellerTaxCode = sellerTaxCode; }

    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }

    public String getSellerBankAccount() { return sellerBankAccount; }
    public void setSellerBankAccount(String sellerBankAccount) { this.sellerBankAccount = sellerBankAccount; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerCompany() { return buyerCompany; }
    public void setBuyerCompany(String buyerCompany) { this.buyerCompany = buyerCompany; }

    public String getBuyerTaxCode() { return buyerTaxCode; }
    public void setBuyerTaxCode(String buyerTaxCode) { this.buyerTaxCode = buyerTaxCode; }

    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }

    public String getBuyerBankAccount() { return buyerBankAccount; }
    public void setBuyerBankAccount(String buyerBankAccount) { this.buyerBankAccount = buyerBankAccount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getVatRate() { return vatRate; }
    public void setVatRate(double vatRate) { this.vatRate = vatRate; }

    public double getVatAmount() { return vatAmount; }
    public void setVatAmount(double vatAmount) { this.vatAmount = vatAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getAmountInWords() { return amountInWords; }
    public void setAmountInWords(String amountInWords) { this.amountInWords = amountInWords; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items != null ? items : new ArrayList<>(); }

    @Override
    public String toString() {
        return "Invoice{id=" + id + ", no='" + invoiceNo + "', buyer='" + buyerName + "', total=" + totalAmount + "}";
    }
}

