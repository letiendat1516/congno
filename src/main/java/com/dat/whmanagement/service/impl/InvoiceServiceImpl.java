package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.InvoiceDAO;
import com.dat.whmanagement.dao.InvoiceItemDAO;
import com.dat.whmanagement.dao.impl.InvoiceDAOImpl;
import com.dat.whmanagement.dao.impl.InvoiceItemDAOImpl;
import com.dat.whmanagement.model.Invoice;
import com.dat.whmanagement.model.InvoiceItem;
import com.dat.whmanagement.service.InvoiceService;
import com.dat.whmanagement.util.NumberToVietnameseWordsUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDAO invoiceDAO;
    private final InvoiceItemDAO invoiceItemDAO;

    public InvoiceServiceImpl() {
        this.invoiceDAO = new InvoiceDAOImpl();
        this.invoiceItemDAO = new InvoiceItemDAOImpl();
    }

    @Override
    public Invoice saveInvoiceWithItems(Invoice invoice, List<InvoiceItem> items) {
        // Tính toán VAT
        double vatRate  = invoice.getVatRate() > 0 ? invoice.getVatRate() : 10;
        double subtotal = calculateSubtotal(items);
        double vatAmt   = calculateVat(subtotal, vatRate);
        double total    = calculateTotal(subtotal, vatAmt);

        invoice.setSubtotal(subtotal);
        invoice.setVatRate(vatRate);
        invoice.setVatAmount(vatAmt);
        invoice.setTotalAmount(total);
        if (invoice.getAmountInWords() == null || invoice.getAmountInWords().isBlank()) {
            invoice.setAmountInWords(generateAmountInWords(total));
        }

        applyDefaults(invoice);

        // Lưu header
        invoice = invoiceDAO.insert(invoice);

        // Lưu items
        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            item.setInvoiceId(invoice.getId());
            item.setIndex(i + 1);
            invoiceItemDAO.insert(item);
        }

        System.out.println("Invoice " + invoice.getInvoiceNumber() + " saved with " + items.size() + " items.");
        return invoice;
    }

    @Override
    public Invoice updateInvoiceWithItems(Invoice invoice, List<InvoiceItem> items) {
        double vatRate  = invoice.getVatRate() > 0 ? invoice.getVatRate() : 10;
        double subtotal = calculateSubtotal(items);
        double vatAmt   = calculateVat(subtotal, vatRate);
        double total    = calculateTotal(subtotal, vatAmt);

        invoice.setSubtotal(subtotal);
        invoice.setVatRate(vatRate);
        invoice.setVatAmount(vatAmt);
        invoice.setTotalAmount(total);
        invoice.setAmountInWords(generateAmountInWords(total));

        invoiceDAO.update(invoice);
        invoiceItemDAO.deleteByInvoice(invoice.getId());

        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            item.setInvoiceId(invoice.getId());
            item.setIndex(i + 1);
            item.setId(0);
            invoiceItemDAO.insert(item);
        }

        System.out.println("Invoice " + invoice.getInvoiceNumber() + " updated with " + items.size() + " items.");
        return invoice;
    }

    @Override
    public Optional<Invoice> loadInvoiceDetails(int invoiceId) {
        Optional<Invoice> opt = invoiceDAO.findById(invoiceId);
        opt.ifPresent(inv -> inv.setItems(invoiceItemDAO.findByInvoice(invoiceId)));
        return opt;
    }

    @Override
    public List<InvoiceItem> getInvoiceItems(int invoiceId) {
        return invoiceItemDAO.findByInvoice(invoiceId);
    }

    @Override
    public boolean updateInvoiceStatus(int invoiceId, Invoice.Status status) {
        Optional<Invoice> opt = invoiceDAO.findById(invoiceId);
        if (opt.isEmpty()) return false;
        Invoice invoice = opt.get();
        invoice.setStatus(status);
        return invoiceDAO.update(invoice);
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    @Override
    public List<Invoice> getInvoicesByCustomer(int customerId) {
        return invoiceDAO.findByCustomer(customerId);
    }

    @Override
    public double calculateSubtotal(List<InvoiceItem> items) {
        if (items == null || items.isEmpty()) return 0;
        return items.stream().mapToDouble(InvoiceItem::getTotalPrice).sum();
    }

    @Override
    public double calculateVat(double subtotal, double vatRate) {
        if (subtotal == 0) return 0;
        return Math.round(subtotal * vatRate / 100);
    }

    @Override
    public double calculateTotal(double subtotal, double vatAmount) {
        return subtotal + vatAmount;
    }

    @Override
    public String generateAmountInWords(double total) {
        if (total <= 0) return "Không đồng chẵn";
        return NumberToVietnameseWordsUtil.convert(Math.round(total));
    }

    @Override
    public String generateNextInvoiceNumber() {
        return invoiceDAO.generateInvoiceNumber();
    }

    @Override
    public boolean deleteInvoice(int invoiceId) {
        invoiceItemDAO.deleteByInvoice(invoiceId);
        return invoiceDAO.delete(invoiceId);
    }

    private void applyDefaults(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank())
            invoice.setInvoiceNumber(invoiceDAO.generateInvoiceNumber());
        if (invoice.getIssueDate() == null)
            invoice.setIssueDate(LocalDate.now());
        if (invoice.getStatus() == null)
            invoice.setStatus(Invoice.Status.DRAFT);
        if (invoice.getVatRate() <= 0)
            invoice.setVatRate(10);
        if (invoice.getPaymentMethod() == null || invoice.getPaymentMethod().isBlank())
            invoice.setPaymentMethod("Tiền mặt");
    }
}

