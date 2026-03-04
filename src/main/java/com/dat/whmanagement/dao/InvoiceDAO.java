package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceDAO {
    Invoice insert(Invoice invoice);
    boolean update(Invoice invoice);
    Optional<Invoice> findById(int id);
    List<Invoice> findAll();
    boolean delete(int id);
    List<Invoice> findByCustomer(int customerId);
    String generateInvoiceNumber();
}

