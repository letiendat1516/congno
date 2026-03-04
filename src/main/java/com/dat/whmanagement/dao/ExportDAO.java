package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.ExportRecord;

import java.util.List;

public interface ExportDAO {
    ExportRecord insert(ExportRecord rec);
    List<ExportRecord> findByInvoiceId(int invoiceId);
    boolean existsByInvoiceId(int invoiceId);
    List<ExportRecord> findAllWithInvoiceNumber();
    boolean deleteByInvoiceId(int invoiceId);
}

