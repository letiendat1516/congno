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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Form nhập hóa đơn bán hàng.
 */
public class InvoiceFormPane {

    private final InvoiceService invoiceService = new InvoiceServiceImpl();
    private final ScrollPane root;

    // Header
    private TextField txtFormNumber, txtSymbol, txtInvoiceNo;
    private DatePicker dpDate;

    // Seller
    private TextField txtSellerName, txtSellerTaxCode, txtSellerAddress, txtSellerBank, txtSellerPhone;

    // Buyer
    private TextField txtBuyerName, txtBuyerCompany, txtBuyerTaxCode, txtBuyerAddress;
    private ComboBox<String> cmbPayment;

    // Items
    private TableView<InvoiceItem> tblItems;
    private ObservableList<InvoiceItem> itemsList;

    // Totals
    private Label lblSubtotal, lblVatAmount, lblTotal;
    private Spinner<Integer> spnVatRate;
    private TextArea txtAmountInWords;

    private Button btnSave;
    private int editInvoiceId = 0;

    public InvoiceFormPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getChildren().addAll(
                buildHeader(),
                buildSellerBuyerRow(),
                buildItemsSection(),
                buildTotalsSection(),
                buildButtonBar()
        );

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        prefillDefaults();
    }

    public Parent getRoot() { return root; }

    private VBox buildHeader() {
        Label lblTitle = new Label("HÓA ĐƠN BÁN HÀNG");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        lblTitle.setAlignment(Pos.CENTER);

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        txtFormNumber = new TextField("01GTKT0/001");
        txtFormNumber.setPrefWidth(130);
        txtSymbol = new TextField("AA/22E");
        txtSymbol.setPrefWidth(100);
        txtInvoiceNo = new TextField(invoiceService.generateNextInvoiceNumber());
        txtInvoiceNo.setPrefWidth(100);
        dpDate = new DatePicker(LocalDate.now());
        dpDate.setPrefWidth(140);

        headerRow.getChildren().addAll(
                new Label("Mẫu số:"), txtFormNumber,
                new Label("Ký hiệu:"), txtSymbol,
                new Label("Số:"), txtInvoiceNo,
                new Label("Ngày:"), dpDate
        );

        return new VBox(6, lblTitle, headerRow);
    }

    private HBox buildSellerBuyerRow() {
        txtSellerName = new TextField();
        txtSellerTaxCode = new TextField();
        txtSellerAddress = new TextField();
        txtSellerBank = new TextField();
        txtSellerPhone = new TextField();

        GridPane sellerGrid = new GridPane();
        sellerGrid.setHgap(8); sellerGrid.setVgap(6); sellerGrid.setPadding(new Insets(8));
        applyGridConstraints(sellerGrid);
        int sr = 0;
        addRow(sellerGrid, sr++, "Đơn vị bán hàng:", txtSellerName);
        addRow(sellerGrid, sr++, "Mã số thuế:", txtSellerTaxCode);
        addRow(sellerGrid, sr++, "Địa chỉ:", txtSellerAddress);
        addRow(sellerGrid, sr++, "Số TK ngân hàng:", txtSellerBank);
        addRow(sellerGrid, sr, "Điện thoại:", txtSellerPhone);

        TitledPane sellerPane = new TitledPane("NGƯỜI BÁN", sellerGrid);
        sellerPane.setCollapsible(false);

        txtBuyerName = new TextField();
        txtBuyerCompany = new TextField();
        txtBuyerTaxCode = new TextField();
        txtBuyerAddress = new TextField();
        cmbPayment = new ComboBox<>(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "Tiền mặt/Chuyển khoản"));
        cmbPayment.setValue("Tiền mặt");
        cmbPayment.setMinWidth(100);
        cmbPayment.setPrefWidth(200);

        GridPane buyerGrid = new GridPane();
        buyerGrid.setHgap(8); buyerGrid.setVgap(6); buyerGrid.setPadding(new Insets(8));
        applyGridConstraints(buyerGrid);
        int r = 0;
        addRow(buyerGrid, r++, "Họ tên người mua:", txtBuyerName);
        addRow(buyerGrid, r++, "Tên đơn vị:", txtBuyerCompany);
        addRow(buyerGrid, r++, "Mã số thuế:", txtBuyerTaxCode);
        addRow(buyerGrid, r++, "Địa chỉ:", txtBuyerAddress);
        addRow(buyerGrid, r, "Hình thức TT:", cmbPayment);

        TitledPane buyerPane = new TitledPane("NGƯỜI MUA", buyerGrid);
        buyerPane.setCollapsible(false);

        HBox row = new HBox(10, sellerPane, buyerPane);
        HBox.setHgrow(sellerPane, Priority.ALWAYS);
        HBox.setHgrow(buyerPane, Priority.ALWAYS);
        sellerPane.prefWidthProperty().bind(row.widthProperty().subtract(10).divide(2));
        buyerPane.prefWidthProperty().bind(row.widthProperty().subtract(10).divide(2));
        sellerPane.setMaxWidth(Double.MAX_VALUE);
        buyerPane.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    @SuppressWarnings("unchecked")
    private VBox buildItemsSection() {
        Label lblSection = new Label("BẢNG KÊ HÀNG HÓA, DỊCH VỤ");
        lblSection.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        itemsList = FXCollections.observableArrayList();
        tblItems = new TableView<>(itemsList);
        tblItems.setEditable(true);
        tblItems.setPrefHeight(220);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<InvoiceItem, String> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(itemsList.indexOf(d.getValue()) + 1)));
        colSTT.setMinWidth(40); colSTT.setMaxWidth(55);

        TableColumn<InvoiceItem, String> colName = new TableColumn<>("Tên hàng hóa, dịch vụ");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setMinWidth(150);

        TableColumn<InvoiceItem, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnit()));
        colUnit.setMinWidth(45); colUnit.setMaxWidth(70);

        TableColumn<InvoiceItem, String> colQty = new TableColumn<>("Số lượng");
        colQty.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQuantity())));
        colQty.setMinWidth(55); colQty.setMaxWidth(80);
        colQty.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InvoiceItem, String> colPrice = new TableColumn<>("Đơn giá");
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(
                CurrencyUtil.formatPlain(d.getValue().getUnitPrice())));
        colPrice.setMinWidth(90); colPrice.setMaxWidth(140);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InvoiceItem, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                CurrencyUtil.formatPlain(d.getValue().getTotalPrice())));
        colTotal.setMinWidth(90); colTotal.setMaxWidth(140);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InvoiceItem, String> colNotes = new TableColumn<>("Ghi chú");
        colNotes.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNotes() != null ? d.getValue().getNotes() : ""));
        colNotes.setMinWidth(80);

        tblItems.getColumns().addAll(colSTT, colName, colUnit, colQty, colPrice, colTotal, colNotes);

        // Double-click to edit row
        tblItems.setRowFactory(tv -> {
            TableRow<InvoiceItem> tableRow = new TableRow<>();
            tableRow.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !tableRow.isEmpty()) {
                    editItemDialog(tableRow.getIndex());
                }
            });
            return tableRow;
        });

        Button btnAdd = new Button("＋ Thêm dòng");
        btnAdd.setOnAction(e -> onAddItem());

        Button btnRemove = new Button("－ Xóa dòng");
        btnRemove.setOnAction(e -> onRemoveItem());

        HBox btnRow = new HBox(8, btnAdd, btnRemove);
        btnRow.setPadding(new Insets(4, 0, 0, 0));

        return new VBox(6, lblSection, tblItems, btnRow);
    }

    private VBox buildTotalsSection() {
        lblSubtotal  = new Label("VND 0");
        lblVatAmount = new Label("VND 0");
        lblTotal     = new Label("VND 0");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #E53935;");

        spnVatRate = new Spinner<>(0, 100, 10);
        spnVatRate.setPrefWidth(120);
        spnVatRate.setMinWidth(120);
        spnVatRate.valueProperty().addListener((obs, o, n) -> refreshTotals());

        txtAmountInWords = new TextArea();
        txtAmountInWords.setPrefRowCount(2);
        txtAmountInWords.setEditable(true);
        txtAmountInWords.setWrapText(true);
        txtAmountInWords.setPromptText("Tự động tạo – có thể sửa lại nếu chưa chuẩn");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(6); grid.setPadding(new Insets(8));

        ColumnConstraints totalLabelCol = new ColumnConstraints();
        totalLabelCol.setPercentWidth(40);
        ColumnConstraints totalValueCol = new ColumnConstraints();
        totalValueCol.setPercentWidth(60);
        totalValueCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(totalLabelCol, totalValueCol);

        lblSubtotal.setMaxWidth(Double.MAX_VALUE);
        lblVatAmount.setMaxWidth(Double.MAX_VALUE);
        lblTotal.setMaxWidth(Double.MAX_VALUE);

        int r = 0;
        grid.add(new Label("Cộng tiền hàng:"), 0, r);
        grid.add(lblSubtotal, 1, r++);

        HBox vatRow = new HBox(4, new Label("Thuế suất:"), spnVatRate, new Label("%"));
        vatRow.setAlignment(Pos.CENTER_LEFT);
        grid.add(vatRow, 0, r++);

        grid.add(new Label("Tiền thuế:"), 0, r);
        grid.add(lblVatAmount, 1, r++);

        grid.add(new Label("Tổng cộng tiền thanh toán:"), 0, r);
        grid.add(lblTotal, 1, r++);

        grid.add(new Label("Số tiền viết bằng chữ:"), 0, r);
        grid.add(txtAmountInWords, 1, r);
        GridPane.setHgrow(txtAmountInWords, Priority.ALWAYS);

        TitledPane totalsPane = new TitledPane("TỔNG CỘNG", grid);
        totalsPane.setCollapsible(false);

        return new VBox(totalsPane);
    }

    private HBox buildButtonBar() {
        btnSave = new Button("💾 Lưu hóa đơn");
        btnSave.setOnAction(e -> onSave());

        Button btnPreview = new Button("🔍 Xem trước");
        btnPreview.setOnAction(e -> onPreview());

        Button btnPrint = new Button("🖨 In");
        btnPrint.setOnAction(e -> onPreview());

        HBox bar = new HBox(10, btnSave, btnPreview, btnPrint);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 0, 0, 0));
        return bar;
    }

    // ── Actions ──

    private void onAddItem() {
        InvoiceItem item = new InvoiceItem();
        item.setName("Sản phẩm mới");
        item.setUnit("Cái");
        item.setQuantity(1);
        item.setUnitPrice(0);
        item.setTotalPrice(0);
        itemsList.add(item);
        editItemDialog(itemsList.size() - 1);
    }

    private void onRemoveItem() {
        int idx = tblItems.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showWarning("Chọn dòng cần xóa.");
            return;
        }
        itemsList.remove(idx);
        refreshTotals();
    }

    private void editItemDialog(int idx) {
        InvoiceItem item = itemsList.get(idx);

        Dialog<InvoiceItem> dlg = new Dialog<>();
        dlg.setTitle("Nhập thông tin hàng hóa");

        TextField tfName  = new TextField(item.getName());
        TextField tfUnit  = new TextField(item.getUnit());
        Spinner<Integer> spQty = new Spinner<>(1, 999999, item.getQuantity());
        spQty.setEditable(true);
        TextField tfPrice = new TextField(item.getUnitPrice() > 0 ? String.valueOf((long) item.getUnitPrice()) : "0");
        TextField tfNotes = new TextField(item.getNotes() != null ? item.getNotes() : "");

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.add(new Label("Tên hàng hóa:"), 0, 0); gp.add(tfName, 1, 0);
        gp.add(new Label("ĐVT:"), 0, 1); gp.add(tfUnit, 1, 1);
        gp.add(new Label("Số lượng:"), 0, 2); gp.add(spQty, 1, 2);
        gp.add(new Label("Đơn giá:"), 0, 3); gp.add(tfPrice, 1, 3);
        gp.add(new Label("Ghi chú:"), 0, 4); gp.add(tfNotes, 1, 4);
        tfName.setPrefWidth(250);

        dlg.getDialogPane().setContent(gp);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                item.setName(tfName.getText().trim());
                item.setUnit(tfUnit.getText().trim());
                item.setQuantity(spQty.getValue());
                item.setNotes(tfNotes.getText().trim());
                try {
                    double price = Double.parseDouble(tfPrice.getText().trim().replace(",", ""));
                    item.setUnitPrice(price);
                    item.setTotalPrice(price * spQty.getValue());
                } catch (NumberFormatException ex) {
                    item.setUnitPrice(0);
                    item.setTotalPrice(0);
                }
                return item;
            }
            return null;
        });

        dlg.showAndWait().ifPresent(result -> {
            tblItems.refresh();
            refreshTotals();
        });
    }

    private void onSave() {
        if (itemsList.isEmpty()) {
            showError("Vui lòng thêm ít nhất một dòng hàng hóa.");
            return;
        }
        if (txtBuyerName.getText().isBlank()) {
            showError("Vui lòng nhập tên người mua hàng.");
            return;
        }

        Invoice invoice = buildInvoiceFromForm();
        List<InvoiceItem> items = new ArrayList<>(itemsList);

        try {
            Invoice saved;
            if (editInvoiceId > 0) {
                invoice.setId(editInvoiceId);
                saved = invoiceService.updateInvoiceWithItems(invoice, items);
                showInfo("Đã cập nhật hóa đơn số: " + saved.getInvoiceNumber());
            } else {
                saved = invoiceService.saveInvoiceWithItems(invoice, items);
                showInfo("Đã lưu hóa đơn số: " + saved.getInvoiceNumber());
            }
            clearForm();
        } catch (Exception ex) {
            showError("Lỗi lưu hóa đơn:\n" + ex.getMessage());
        }
    }

    private void onPreview() {
        if (itemsList.isEmpty()) {
            showWarning("Chưa có dòng hàng hóa để xem trước.");
            return;
        }
        Invoice invoice = buildInvoiceFromForm();
        if (editInvoiceId > 0) invoice.setId(editInvoiceId);
        showPreview(invoice, new ArrayList<>(itemsList));
    }

    public void showPreview(Invoice invoice, List<InvoiceItem> items) {
        InvoicePreviewStage preview = new InvoicePreviewStage(invoice, items);
        preview.show();
    }

    /** Nạp hóa đơn để sửa. Gọi từ InvoiceListPane. */
    public void loadInvoice(Invoice inv, List<InvoiceItem> items) {
        editInvoiceId = inv.getId();

        txtFormNumber.setText(inv.getInvoiceFormNumber() != null ? inv.getInvoiceFormNumber() : "");
        txtSymbol.setText(inv.getInvoiceSymbol() != null ? inv.getInvoiceSymbol() : "");
        txtInvoiceNo.setText(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "");
        dpDate.setValue(inv.getIssueDate() != null ? inv.getIssueDate() : LocalDate.now());

        txtSellerName.setText(str(inv.getSellerName()));
        txtSellerTaxCode.setText(str(inv.getSellerTaxCode()));
        txtSellerAddress.setText(str(inv.getSellerAddress()));
        txtSellerBank.setText(str(inv.getSellerBankAccount()));
        txtSellerPhone.setText(str(inv.getSellerPhone()));

        txtBuyerName.setText(str(inv.getBuyerName()));
        txtBuyerCompany.setText(str(inv.getBuyerCompany()));
        txtBuyerTaxCode.setText(str(inv.getBuyerTaxCode()));
        txtBuyerAddress.setText(str(inv.getBuyerAddress()));
        cmbPayment.setValue(inv.getPaymentMethod() != null ? inv.getPaymentMethod() : "Tiền mặt");

        if (inv.getVatRate() > 0) spnVatRate.getValueFactory().setValue((int) inv.getVatRate());

        itemsList.setAll(items);
        tblItems.refresh();
        refreshTotals();

        btnSave.setText("💾 Cập nhật hóa đơn");
    }

    public void refresh() { /* nothing needed on tab switch */ }

    // ── Helpers ──

    private void refreshTotals() {
        List<InvoiceItem> items = new ArrayList<>(itemsList);
        double vatRate  = spnVatRate.getValue();
        double subtotal = invoiceService.calculateSubtotal(items);
        double vat      = invoiceService.calculateVat(subtotal, vatRate);
        double total    = invoiceService.calculateTotal(subtotal, vat);
        String words    = invoiceService.generateAmountInWords(total);

        lblSubtotal.setText(CurrencyUtil.format(subtotal));
        lblVatAmount.setText(CurrencyUtil.format(vat));
        lblTotal.setText(CurrencyUtil.format(total));
        txtAmountInWords.setText(words);
    }

    private Invoice buildInvoiceFromForm() {
        Invoice inv = new Invoice();
        inv.setInvoiceFormNumber(txtFormNumber.getText().trim());
        inv.setInvoiceSymbol(txtSymbol.getText().trim());
        inv.setInvoiceNo(txtInvoiceNo.getText().trim());
        inv.setIssueDate(dpDate.getValue());

        inv.setSellerName(txtSellerName.getText().trim());
        inv.setSellerTaxCode(txtSellerTaxCode.getText().trim());
        inv.setSellerAddress(txtSellerAddress.getText().trim());
        inv.setSellerBankAccount(txtSellerBank.getText().trim());
        inv.setSellerPhone(txtSellerPhone.getText().trim());

        inv.setBuyerName(txtBuyerName.getText().trim());
        inv.setBuyerCompany(txtBuyerCompany.getText().trim());
        inv.setBuyerTaxCode(txtBuyerTaxCode.getText().trim());
        inv.setBuyerAddress(txtBuyerAddress.getText().trim());
        inv.setPaymentMethod(cmbPayment.getValue());

        double vatRate  = spnVatRate.getValue();
        List<InvoiceItem> items = new ArrayList<>(itemsList);
        double subtotal = invoiceService.calculateSubtotal(items);
        double vatAmt   = invoiceService.calculateVat(subtotal, vatRate);
        double total    = invoiceService.calculateTotal(subtotal, vatAmt);

        inv.setVatRate(vatRate);
        inv.setSubtotal(subtotal);
        inv.setVatAmount(vatAmt);
        inv.setTotalAmount(total);
        inv.setAmountInWords(txtAmountInWords.getText().trim());
        inv.setStatus(Invoice.Status.DRAFT);
        return inv;
    }

    private void prefillDefaults() {
        txtSellerName.setText("ĐẠI LÝ CẤP I XUÂN TRƯỜNG – CÔNG TY CỔ PHẦN VIGLACERA HẠ LONG GIẾNG ĐÁY - QUẢNG NINH");
        txtSellerAddress.setText("Cây xăng Bồ Sơn, gần bệnh viện đa khoa tỉnh Bắc Ninh");
        txtSellerPhone.setText("0977.556.638 – 0972.070.186 – 0925.234.898");
        txtSellerBank.setText("3866 1616 8666 tại NH Quân Đội (MB) - Chủ TK: Nguyễn Thị Hiến");
    }

    private void clearForm() {
        editInvoiceId = 0;
        itemsList.clear();
        txtBuyerName.setText(""); txtBuyerCompany.setText("");
        txtBuyerTaxCode.setText(""); txtBuyerAddress.setText("");
        txtInvoiceNo.setText(invoiceService.generateNextInvoiceNumber());
        dpDate.setValue(LocalDate.now());
        refreshTotals();
        btnSave.setText("💾 Lưu hóa đơn");
    }

    private void applyGridConstraints(GridPane grid) {
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPercentWidth(25);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setPercentWidth(75);
        fieldCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
    }

    private String str(String s) { return s != null ? s : ""; }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

