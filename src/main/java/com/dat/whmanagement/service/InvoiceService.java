package com.dat.whmanagement.service;

import com.dat.whmanagement.model.Invoice;
import com.dat.whmanagement.model.InvoiceItem;

import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    Invoice saveInvoiceWithItems(Invoice invoice, List<InvoiceItem> items);
    Invoice updateInvoiceWithItems(Invoice invoice, List<InvoiceItem> items);
    Optional<Invoice> loadInvoiceDetails(int invoiceId);
    List<InvoiceItem> getInvoiceItems(int invoiceId);
    boolean updateInvoiceStatus(int invoiceId, Invoice.Status status);
    List<Invoice> getAllInvoices();
    List<Invoice> getInvoicesByCustomer(int customerId);
    double calculateSubtotal(List<InvoiceItem> items);
    double calculateVat(double subtotal, double vatRate);
    double calculateTotal(double subtotal, double vatAmount);
    String generateAmountInWords(double total);
    String generateNextInvoiceNumber();
    boolean deleteInvoice(int invoiceId);
}

