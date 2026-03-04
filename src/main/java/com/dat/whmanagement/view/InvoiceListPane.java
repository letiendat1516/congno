package com.dat.whmanagement.view;

import com.dat.whmanagement.model.Invoice;
import com.dat.whmanagement.model.InvoiceItem;
import com.dat.whmanagement.service.InvoiceService;
import com.dat.whmanagement.service.impl.InvoiceServiceImpl;
import com.dat.whmanagement.util.CurrencyUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Danh sách hóa đơn đã lưu.
 */
public class InvoiceListPane {

    private final InvoiceService invoiceService = new InvoiceServiceImpl();
    private final BorderPane root;
    private TableView<Invoice> tblInvoices;
    private ObservableList<Invoice> invoiceList;
    private TextField txtSearch;

    private BiConsumer<Invoice, List<InvoiceItem>> onEditInvoice;
    private BiConsumer<Invoice, List<InvoiceItem>> onReprintInvoice;

    public InvoiceListPane() {
        root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(buildToolbar());
        root.setCenter(buildTable());
        refresh();
    }

    public Parent getRoot() { return root; }

    public void setOnEditInvoice(BiConsumer<Invoice, List<InvoiceItem>> cb) { this.onEditInvoice = cb; }
    public void setOnReprintInvoice(BiConsumer<Invoice, List<InvoiceItem>> cb) { this.onReprintInvoice = cb; }

    private ToolBar buildToolbar() {
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo số hóa đơn...");
        txtSearch.setPrefWidth(200);

        Button btnSearch  = new Button("🔍 Tìm");
        btnSearch.setOnAction(e -> refresh());
        Button btnRefresh = new Button("⟳ Làm mới");
        btnRefresh.setOnAction(e -> { txtSearch.setText(""); refresh(); });

        Button btnEdit    = new Button("✏ Sửa");
        btnEdit.setOnAction(e -> onEdit());

        Button btnReprint = new Button("🖨 In lại");
        btnReprint.setOnAction(e -> onReprint());

        Button btnPaid    = new Button("✔ Đã thanh toán");
        btnPaid.setOnAction(e -> onMarkPaid());

        Button btnCancel  = new Button("✖ Hủy HĐ");
        btnCancel.setOnAction(e -> onCancel());

        Button btnDelete  = new Button("🗑 Xóa");
        btnDelete.setOnAction(e -> onDelete());

        return new ToolBar(
                new Label("Tìm:"), txtSearch, btnSearch, btnRefresh,
                new Separator(),
                btnEdit, btnReprint,
                new Separator(),
                btnPaid, btnCancel, btnDelete
        );
    }

    @SuppressWarnings("unchecked")
    private TableView<Invoice> buildTable() {
        invoiceList = FXCollections.observableArrayList();
        tblInvoices = new TableView<>(invoiceList);
        tblInvoices.setPlaceholder(new Label("Chưa có hóa đơn nào."));

        TableColumn<Invoice, String> colNo = new TableColumn<>("Số HĐ");
        colNo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInvoiceNumber()));
        colNo.setPrefWidth(130);

        TableColumn<Invoice, String> colDate = new TableColumn<>("Ngày");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getIssueDate() != null
                        ? d.getValue().getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : ""));
        colDate.setPrefWidth(100);

        TableColumn<Invoice, String> colBuyer = new TableColumn<>("Người mua");
        colBuyer.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getBuyerName() != null ? d.getValue().getBuyerName() : ""));
        colBuyer.setPrefWidth(200);

        TableColumn<Invoice, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(translateStatus(d.getValue().getStatus())));
        colStatus.setPrefWidth(120);

        TableColumn<Invoice, String> colTotal = new TableColumn<>("Tổng tiền");
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                CurrencyUtil.format(d.getValue().getTotalAmount())));
        colTotal.setPrefWidth(140);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tblInvoices.getColumns().addAll(colNo, colDate, colBuyer, colStatus, colTotal);

        tblInvoices.setRowFactory(tv -> {
            TableRow<Invoice> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) onEdit();
            });
            return row;
        });

        return tblInvoices;
    }

    public void refresh() {
        List<Invoice> all = invoiceService.getAllInvoices();
        String kw = txtSearch.getText().trim().toLowerCase();
        if (!kw.isEmpty()) {
            all = all.stream()
                    .filter(inv -> inv.getInvoiceNumber() != null
                            && inv.getInvoiceNumber().toLowerCase().contains(kw))
                    .toList();
        }
        invoiceList.setAll(all);
    }

    private void onEdit() {
        Invoice inv = getSelected();
        if (inv == null) return;
        Optional<Invoice> full = invoiceService.loadInvoiceDetails(inv.getId());
        if (full.isEmpty()) { showError("Không tìm thấy hóa đơn."); return; }
        Invoice loaded = full.get();
        List<InvoiceItem> items = invoiceService.getInvoiceItems(inv.getId());
        if (onEditInvoice != null) onEditInvoice.accept(loaded, items);
    }

    private void onReprint() {
        Invoice inv = getSelected();
        if (inv == null) return;
        Optional<Invoice> full = invoiceService.loadInvoiceDetails(inv.getId());
        if (full.isEmpty()) { showError("Không tìm thấy hóa đơn."); return; }
        Invoice loaded = full.get();
        List<InvoiceItem> items = invoiceService.getInvoiceItems(inv.getId());
        if (onReprintInvoice != null) onReprintInvoice.accept(loaded, items);
    }

    private void onMarkPaid() {
        Invoice inv = getSelected();
        if (inv == null) return;
        if (!showConfirm("Đánh dấu hóa đơn " + inv.getInvoiceNumber() + " là ĐÃ THANH TOÁN?")) return;
        invoiceService.updateInvoiceStatus(inv.getId(), Invoice.Status.PAID);
        refresh();
    }

    private void onCancel() {
        Invoice inv = getSelected();
        if (inv == null) return;
        if (!showConfirm("Hủy hóa đơn " + inv.getInvoiceNumber() + "?")) return;
        invoiceService.updateInvoiceStatus(inv.getId(), Invoice.Status.CANCELLED);
        refresh();
    }

    private void onDelete() {
        Invoice inv = getSelected();
        if (inv == null) return;
        if (!showConfirm("Xóa vĩnh viễn hóa đơn " + inv.getInvoiceNumber() + "?")) return;
        invoiceService.deleteInvoice(inv.getId());
        refresh();
    }

    private Invoice getSelected() {
        Invoice inv = tblInvoices.getSelectionModel().getSelectedItem();
        if (inv == null) showWarning("Vui lòng chọn một hóa đơn.");
        return inv;
    }

    private String translateStatus(Invoice.Status status) {
        if (status == null) return "";
        return switch (status) {
            case DRAFT     -> "Nháp";
            case PAID      -> "Đã thanh toán";
            case CANCELLED -> "Đã hủy";
        };
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private boolean showConfirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}

