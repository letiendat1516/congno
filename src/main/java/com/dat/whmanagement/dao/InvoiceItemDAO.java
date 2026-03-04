package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.InvoiceItem;

import java.util.List;

public interface InvoiceItemDAO {
    InvoiceItem insert(InvoiceItem item);
    List<InvoiceItem> findByInvoice(int invoiceId);
    void deleteByInvoice(int invoiceId);
}

