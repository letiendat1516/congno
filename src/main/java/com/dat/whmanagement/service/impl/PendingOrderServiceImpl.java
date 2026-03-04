package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.impl.CustomerDAOImpl;
import com.dat.whmanagement.dao.impl.PendingOrderDAOImpl;
import com.dat.whmanagement.dao.impl.SalesOrderDAOImpl;
import com.dat.whmanagement.model.*;
import com.dat.whmanagement.service.InvoiceService;
import com.dat.whmanagement.service.PendingOrderService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PendingOrderServiceImpl implements PendingOrderService {

    private final PendingOrderDAOImpl pendingDAO  = new PendingOrderDAOImpl();
    private final SalesOrderDAOImpl   salesDAO    = new SalesOrderDAOImpl();
    private final CustomerDAOImpl     customerDAO = new CustomerDAOImpl();
    private final InvoiceService      invoiceService = new InvoiceServiceImpl();

    @Override
    public void create(PendingOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Đơn đặt hàng phải có ít nhất 1 sản phẩm");
        order.setStatus("PENDING");
        pendingDAO.insert(order);
    }

    @Override public List<PendingOrder>       getAll()               { return pendingDAO.findAll(); }
    @Override public List<PendingOrderDetail> getDetails(int id)     { return pendingDAO.findDetailsByOrderId(id); }
    @Override public void                     delete(int orderId)    { pendingDAO.delete(orderId); }
    @Override public String                   nextOrderNumber()       { return pendingDAO.nextOrderNumber(); }

    @Override
    public void cancelOrder(int pendingOrderId) {
        pendingDAO.updateStatus(pendingOrderId, "CANCELLED");
    }

    /**
     * Xuất kho từ đơn đặt hàng:
     * - Kiểm tra tồn kho từng sản phẩm
     * - Tạo SalesOrder thực sự (trừ kho, ghi movement)
     * - Đánh dấu pending order = EXPORTED
     */
    @Override
    public void exportOrder(int pendingOrderId) {
        PendingOrder pending = pendingDAO.findById(pendingOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt hàng #" + pendingOrderId));

        if (!"PENDING".equals(pending.getStatus()))
            throw new IllegalStateException("Đơn đặt hàng đã được xử lý hoặc huỷ");

        List<PendingOrderDetail> details = pendingDAO.findDetailsByOrderId(pendingOrderId);

        // 1. Kiểm tra tồn kho tất cả sản phẩm trước khi xuất
        StringBuilder stockErr = new StringBuilder();
        for (PendingOrderDetail d : details) {
            double currentStock = getProductStock(d.getProductId());
            if (currentStock < d.getQuantity()) {
                stockErr.append(String.format("\n• %s (cần %.2f, còn %.2f)",
                        d.getProductName(), d.getQuantity(), currentStock));
            }
        }
        if (stockErr.length() > 0)
            throw new IllegalStateException("Không đủ tồn kho cho các sản phẩm:" + stockErr);

        // 2. Tạo SalesOrder từ đơn đặt hàng
        SalesOrder sales = new SalesOrder();
        sales.setOrderNumber(new SalesOrderDAOImpl().nextOrderNumber());
        sales.setCustomerId(pending.getCustomerId());
        sales.setCustomerName(pending.getCustomerName());
        sales.setOrderDate(LocalDate.now());
        sales.setPaidAmount(0);
        sales.setNote("Xuất từ đơn đặt hàng " + pending.getOrderNumber());

        List<SalesOrderDetail> salesDetails = new ArrayList<>();
        for (PendingOrderDetail d : details) {
            SalesOrderDetail sd = new SalesOrderDetail();
            sd.setProductId(d.getProductId());
            sd.setProductCode(d.getProductCode());
            sd.setProductName(d.getProductName());
            sd.setUnit(d.getUnit());
            sd.setQuantity(d.getQuantity());
            sd.setUnitPrice(d.getUnitPrice());
            sd.setTotal(d.getTotal());
            salesDetails.add(sd);
        }
        double subTotal = salesDetails.stream().mapToDouble(SalesOrderDetail::getTotal).sum();
        double vatRate = pending.getVatRate() > 0 ? pending.getVatRate() : 10;
        sales.setTotalAmount(Math.round(subTotal * (1 + vatRate / 100) * 100.0) / 100.0);
        sales.setDetails(salesDetails);

        // 3. Lưu sales order (trừ kho bên trong DAO)
        salesDAO.insert(sales);

        // 4. Tự động tạo hóa đơn từ phiếu xuất
        createInvoiceFromSalesOrder(sales, pending);

        // 5. Cập nhật trạng thái pending
        pendingDAO.updateStatus(pendingOrderId, "EXPORTED");
    }

    private double getProductStock(int productId) {
        String sql = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createInvoiceFromSalesOrder(SalesOrder sales, PendingOrder pending) {
        try {
            // Lấy thông tin khách hàng đầy đủ
            Customer cust = customerDAO.findById(pending.getCustomerId()).orElse(null);
            String buyerName    = cust != null ? cust.getName()    : pending.getCustomerName();
            String buyerAddress = cust != null && cust.getAddress() != null ? cust.getAddress() : "";

            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceService.generateNextInvoiceNumber());
            invoice.setInvoiceFormNumber("01GTKT0/001");
            invoice.setInvoiceSymbol("AA/22E");
            invoice.setIssueDate(sales.getOrderDate());
            invoice.setStatus(Invoice.Status.DRAFT);
            invoice.setSalesOrderId(sales.getId());
            invoice.setCustomerId(pending.getCustomerId());

            // Thông tin người bán (mặc định)
            invoice.setSellerName("ĐẠI LÝ CẤP I XUÂN TRƯỜNG – CÔNG TY CỔ PHẦN VIGLACERA HẠ LONG GIẾNG ĐÁY - QUẢNG NINH");
            invoice.setSellerAddress("Cây xăng Bồ Sơn, gần bệnh viện đa khoa tỉnh Bắc Ninh");
            invoice.setSellerPhone("0977.556.638 – 0972.070.186 – 0925.234.898");
            invoice.setSellerBankAccount("3866 1616 8666 tại NH Quân Đội (MB) - Chủ TK: Nguyễn Thị Hiến");

            // Thông tin người mua
            invoice.setBuyerName(buyerName);
            invoice.setBuyerAddress(buyerAddress);
            invoice.setPaymentMethod("Tiền mặt");

            invoice.setVatRate(pending.getVatRate() > 0 ? pending.getVatRate() : 10);
            invoice.setNotes("Tự động tạo từ đơn đặt hàng " + pending.getOrderNumber()
                    + " → phiếu xuất " + sales.getOrderNumber());

            // Chuyển SalesOrderDetail → InvoiceItem
            List<InvoiceItem> invoiceItems = new ArrayList<>();
            for (SalesOrderDetail d : sales.getDetails()) {
                InvoiceItem item = new InvoiceItem();
                item.setProductId(d.getProductId());
                item.setName(d.getProductName());
                item.setUnit(d.getUnit() != null ? d.getUnit() : "");
                item.setQuantity((int) d.getQuantity());
                item.setUnitPrice(d.getUnitPrice());
                item.setTotalPrice(d.getTotal());
                item.setDiscount(0);
                invoiceItems.add(item);
            }

            invoiceService.saveInvoiceWithItems(invoice, invoiceItems);
            System.out.println("Tự động tạo hóa đơn " + invoice.getInvoiceNumber()
                    + " từ đơn đặt hàng " + pending.getOrderNumber());
        } catch (Exception e) {
            System.out.println("Lỗi tự động tạo hóa đơn từ đơn đặt hàng: " + e.getMessage());
            // Không throw — phiếu xuất đã lưu, chỉ hóa đơn lỗi
        }
    }
}

