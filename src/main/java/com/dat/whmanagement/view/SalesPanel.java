package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.model.*;
import com.dat.whmanagement.service.CustomerService;
import com.dat.whmanagement.service.ProductService;
import com.dat.whmanagement.service.SalesOrderService;
import com.dat.whmanagement.service.impl.CustomerServiceImpl;
import com.dat.whmanagement.service.impl.SalesOrderServiceImpl;
import com.dat.whmanagement.dao.impl.ProductDAOImpl;
import com.dat.whmanagement.dao.impl.ProductServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.collections.transformation.FilteredList;
import com.dat.whmanagement.util.ComboBoxHelper;

public class SalesPanel extends BorderPane {

    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final SalesOrderService orderService  = new SalesOrderServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final ProductService productService   = new ProductServiceImpl(new ProductDAOImpl());

    private final ObservableList<SalesOrder>       masterList = FXCollections.observableArrayList();
    private final ObservableList<SalesOrderDetail> detailList = FXCollections.observableArrayList();
    private       FilteredList<SalesOrder>         filteredList;

    public SalesPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildSplitPane());
        loadData();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private VBox buildHeader() {
        Label title = new Label("Xuất hàng");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Quản lý phiếu xuất hàng cho khách (có thuế VAT 10%)");
        subtitle.setStyle("-fx-text-fill: #888;");

        Button btnAdd = new Button("Tạo phiếu xuất", new FontIcon(Material2OutlinedAL.ADD));
        btnAdd.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAdd.setOnAction(e -> openDialog(null));

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm số phiếu, khách hàng...");
        search.setPrefWidth(260);
        search.getStyleClass().add(Styles.ROUNDED);
        search.textProperty().addListener((obs, o, v) -> {
            if (filteredList == null) return;
            String kw = v == null ? "" : v.trim().toLowerCase();
            filteredList.setPredicate(ord -> kw.isEmpty()
                    || ord.getOrderNumber().toLowerCase().contains(kw)
                    || (ord.getCustomerName() != null && ord.getCustomerName().toLowerCase().contains(kw)));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(10, search, spacer, btnAdd);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(16, 0, 8, 0));

        return new VBox(4, new VBox(2, title, subtitle), actionBar);
    }

    // ─────────────────────────────────────────
    // SPLIT PANE: phiếu xuất + chi tiết sản phẩm
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private SplitPane buildSplitPane() {
        // ── Bảng danh sách phiếu xuất ──
        TableView<SalesOrder> orderTable = new TableView<>();
        orderTable.getStyleClass().add(Styles.STRIPED);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        orderTable.setPlaceholder(new Label("Chưa có phiếu xuất nào"));

        TableColumn<SalesOrder, String> colNum = new TableColumn<>("Số phiếu");
        colNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colNum.setPrefWidth(110);

        TableColumn<SalesOrder, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(190);

        TableColumn<SalesOrder, LocalDate> colDate = new TableColumn<>("Ngày xuất");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDate.setPrefWidth(110);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<SalesOrder, Double> colTotal = new TableColumn<>("Tổng tiền (VAT)");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colTotal.setPrefWidth(140);
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) setText(null);
                else { setText(CURRENCY.format(val) + " ₫"); setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;"); }
            }
        });

        TableColumn<SalesOrder, Double> colPaid = new TableColumn<>("Đã trả");
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colPaid.setPrefWidth(120);
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) setText(null);
                else { setText(CURRENCY.format(val) + " ₫"); setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #388E3C;"); }
            }
        });

        TableColumn<SalesOrder, String> colNote = new TableColumn<>("Ghi chú");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setPrefWidth(120);

        TableColumn<SalesOrder, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("", new FontIcon(Material2OutlinedAL.EDIT));
            private final Button btnDel  = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            private final HBox   box     = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                btnEdit.setTooltip(new Tooltip("Sửa"));
                btnDel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                btnDel.setTooltip(new Tooltip("Xóa"));
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : box);
            }
        });

        orderTable.getColumns().addAll(colNum, colCustomer, colDate, colTotal, colPaid, colNote, colAction);
        filteredList = new FilteredList<>(masterList, p -> true);
        orderTable.setItems(filteredList);

        // Khi chọn phiếu → load chi tiết sản phẩm
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) { detailList.clear(); return; }
            detailList.setAll(orderService.getDetails(selected.getId()));
        });

        // ── Bảng chi tiết sản phẩm (bên dưới) ──
        Label detailTitle = new Label("Chi tiết sản phẩm của phiếu đã chọn (giá chưa VAT)");
        detailTitle.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 6 0 4 0;");

        TableView<SalesOrderDetail> detailTable = new TableView<>(detailList);
        detailTable.getStyleClass().add(Styles.STRIPED);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        detailTable.setPlaceholder(new Label("← Chọn một phiếu xuất để xem chi tiết"));

        TableColumn<SalesOrderDetail, String> dColCode = new TableColumn<>("Mã sản phẩm");
        dColCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode()));
        dColCode.setPrefWidth(110);

        TableColumn<SalesOrderDetail, String> dColName = new TableColumn<>("Tên sản phẩm");
        dColName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName()));
        dColName.setPrefWidth(220);

        TableColumn<SalesOrderDetail, String> dColUnit = new TableColumn<>("Đơn vị tính");
        dColUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit()));
        dColUnit.setPrefWidth(90);
        dColUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<SalesOrderDetail, String> dColQty = new TableColumn<>("Số lượng");
        dColQty.setCellValueFactory(cd -> new SimpleStringProperty(
                String.format("%.2f", cd.getValue().getQuantity())));
        dColQty.setPrefWidth(90);
        dColQty.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<SalesOrderDetail, String> dColPrice = new TableColumn<>("Đơn giá");
        dColPrice.setCellValueFactory(cd -> new SimpleStringProperty(
                CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dColPrice.setPrefWidth(130);
        dColPrice.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<SalesOrderDetail, String> dColTotal = new TableColumn<>("Thành tiền");
        dColTotal.setCellValueFactory(cd -> new SimpleStringProperty(
                CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dColTotal.setPrefWidth(140);
        dColTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #388E3C;");

        detailTable.getColumns().addAll(dColCode, dColName, dColUnit, dColQty, dColPrice, dColTotal);

        VBox detailPane = new VBox(0, detailTitle, detailTable);
        detailPane.setPadding(new Insets(4, 0, 0, 0));
        VBox.setVgrow(detailTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(orderTable, detailPane);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.55);
        BorderPane.setMargin(split, new Insets(8, 0, 0, 0));
        return split;
    }

    private void loadData() {
        masterList.setAll(orderService.getAll());
        detailList.clear();
    }

    // ─────────────────────────────────────────
    // DIALOG TẠO / XEM PHIẾU XUẤT (VAT 10%)
    // ─────────────────────────────────────────
    private void openDialog(SalesOrder existing) {
        boolean isEdit = (existing != null);
        Dialog<SalesOrder> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chi tiết phiếu xuất" : "Tạo phiếu xuất hàng (VAT 10%)");
        dialog.setResizable(true);

        String orderNum = isEdit ? existing.getOrderNumber() : orderService.nextOrderNumber();
        Label lblOrderNum = new Label(orderNum);
        lblOrderNum.setFont(Font.font("System", FontWeight.BOLD, 14));

        List<Customer> customers = customerService.getAll();
        ComboBox<Customer> cbCustomer = new ComboBox<>();
        cbCustomer.setPromptText("Gõ mã/tên KH để tìm *");
        cbCustomer.setPrefWidth(260);
        ComboBoxHelper.makeSearchable(cbCustomer, customers, c -> c.getName());

        DatePicker dpDate = new DatePicker(isEdit ? existing.getOrderDate() : LocalDate.now());
        TextField tfPaid  = new TextField(isEdit ? String.valueOf((long) existing.getPaidAmount()) : "0");
        tfPaid.setPromptText("Số tiền khách trả"); tfPaid.setPrefWidth(160);
        TextField tfNote  = new TextField(isEdit && existing.getNote() != null ? existing.getNote() : "");
        tfNote.setPromptText("Ghi chú...");
        TextField tfVatRate = new TextField("10");
        tfVatRate.setPromptText("% thuế");
        tfVatRate.setPrefWidth(80);

        if (isEdit) {
            customers.stream().filter(c -> c.getId() == existing.getCustomerId())
                    .findFirst().ifPresent(cbCustomer::setValue);
        }

        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(12); headerGrid.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(90);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        headerGrid.getColumnConstraints().addAll(c1, c2);
        headerGrid.addRow(0, new Label("Số phiếu:"),  lblOrderNum);
        headerGrid.addRow(1, new Label("Khách *:"),    cbCustomer);
        headerGrid.addRow(2, new Label("Ngày xuất:"),  dpDate);
        headerGrid.addRow(3, new Label("Thuế (%):"),   tfVatRate);
        headerGrid.addRow(4, new Label("Khách trả:"),  tfPaid);
        headerGrid.addRow(5, new Label("Ghi chú:"),    tfNote);

        ObservableList<SalesOrderDetail> details = FXCollections.observableArrayList();
        if (isEdit) details.setAll(orderService.getDetails(existing.getId()));

        TableView<SalesOrderDetail> detailTable = new TableView<>(details);
        detailTable.setPrefHeight(190);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<SalesOrderDetail, String> dCode  = new TableColumn<>("Mã SP");
        dCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode()));
        dCode.setPrefWidth(90);
        TableColumn<SalesOrderDetail, String> dName  = new TableColumn<>("Tên sản phẩm");
        dName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName()));
        dName.setPrefWidth(170);
        TableColumn<SalesOrderDetail, String> dUnit  = new TableColumn<>("ĐVT");
        dUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit()));
        dUnit.setPrefWidth(60); dUnit.setStyle("-fx-alignment: CENTER;");
        TableColumn<SalesOrderDetail, String> dQty   = new TableColumn<>("Số lượng");
        dQty.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getQuantity())));
        dQty.setPrefWidth(80); dQty.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<SalesOrderDetail, String> dPrice = new TableColumn<>("Đơn giá");
        dPrice.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dPrice.setPrefWidth(110); dPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<SalesOrderDetail, String> dTotal = new TableColumn<>("Thành tiền");
        dTotal.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dTotal.setPrefWidth(120); dTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        // VAT summary labels
        Label lblSub   = new Label("0 ₫");
        Label lblVAT   = new Label("0 ₫");
        Label lblTotalVal = new Label("0 ₫");
        Label lblVATLabel = new Label("Thuế (10%):");
        lblTotalVal.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblTotalVal.setStyle("-fx-text-fill: #E53935;");

        Runnable refreshSummary = () -> {
            double sub   = details.stream().mapToDouble(SalesOrderDetail::getTotal).sum();
            double vatRate = 10;
            try { vatRate = Double.parseDouble(tfVatRate.getText().trim()); } catch (NumberFormatException ignored) {}
            double vat   = sub * vatRate / 100;
            double total = sub + vat;
            lblSub.setText(CURRENCY.format(sub) + " ₫");
            lblVATLabel.setText("Thuế (" + (int) vatRate + "%):");
            lblVAT.setText(CURRENCY.format(vat) + " ₫");
            lblTotalVal.setText(CURRENCY.format(total) + " ₫");
        };
        refreshSummary.run();
        tfVatRate.textProperty().addListener((obs, o, v) -> refreshSummary.run());

        TableColumn<SalesOrderDetail, Void> dDel = new TableColumn<>("");
        dDel.setPrefWidth(40);
        dDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            { btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
              btn.setOnAction(e -> { details.remove(getIndex()); refreshSummary.run(); }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : btn);
            }
        });
        detailTable.getColumns().addAll(dCode, dName, dUnit, dQty, dPrice, dTotal, dDel);
        details.addListener((javafx.collections.ListChangeListener<SalesOrderDetail>) c -> refreshSummary.run());

        // Form thêm dòng
        List<Product> products = productService.getAll();
        ComboBox<Product> cbProd = new ComboBox<>();
        cbProd.setPromptText("Gõ mã/tên SP để tìm"); cbProd.setPrefWidth(210);
        ComboBoxHelper.makeSearchable(cbProd, products, p -> p.getCode() + " - " + p.getName());

        TextField tfQty   = new TextField(); tfQty.setPromptText("Số lượng"); tfQty.setPrefWidth(85);
        TextField tfPrice = new TextField(); tfPrice.setPromptText("Đơn giá"); tfPrice.setPrefWidth(110);
        Label lblStock = new Label("Tồn kho: —"); lblStock.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        // Hiển thị tồn kho khi chọn sản phẩm
        cbProd.valueProperty().addListener((obs, old, sel) -> {
            if (sel == null) { lblStock.setText("Tồn kho: —"); lblStock.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;"); return; }
            try {
                double st = productService.getStock(sel.getId());
                lblStock.setText(String.format("Tồn kho: %.2f %s", st, sel.getUnit() != null ? sel.getUnit() : ""));
                lblStock.setStyle("-fx-text-fill: " + (st > 0 ? "#388E3C" : "#E53935") + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            } catch (ClassCastException ignored) { /* JavaFX passed a String */ }
        });

        Button btnAddRow  = new Button("+ Thêm", new FontIcon(Material2OutlinedAL.ADD));
        btnAddRow.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAddRow.setOnAction(e -> {
            Product sel = ComboBoxHelper.safeGetValue(cbProd); if (sel == null) return;
            try {
                double qty   = Double.parseDouble(tfQty.getText().trim());
                double price = Double.parseDouble(tfPrice.getText().trim().replace(",", ""));
                if (qty <= 0 || price < 0) return;
                // Kiểm tra tồn kho ngay tại dialog
                double stock = productService.getStock(sel.getId());
                // Trừ số lượng đã có trong bảng chi tiết cho sản phẩm này
                double alreadyAdded = details.stream()
                        .filter(d -> d.getProductId() == sel.getId())
                        .mapToDouble(SalesOrderDetail::getQuantity).sum();
                if (qty + alreadyAdded > stock + 0.0001) {
                    new Alert(Alert.AlertType.WARNING,
                            String.format("⚠ Không đủ tồn kho!\n%s: cần %.2f, tồn kho: %.2f",
                                    sel.getName(), qty + alreadyAdded, stock),
                            ButtonType.OK).showAndWait();
                    return;
                }
                SalesOrderDetail d = new SalesOrderDetail();
                d.setProductId(sel.getId()); d.setProductCode(sel.getCode());
                d.setProductName(sel.getName()); d.setUnit(sel.getUnit() != null ? sel.getUnit() : "");
                d.setQuantity(qty); d.setUnitPrice(price); d.setTotal(qty * price);
                details.add(d);
                cbProd.setValue(null); tfQty.clear(); tfPrice.clear();
                lblStock.setText("Tồn kho: —"); lblStock.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
            } catch (NumberFormatException ignored) {}
        });

        HBox addRow = new HBox(8, cbProd, tfQty, tfPrice, btnAddRow, lblStock);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addRow.setPadding(new Insets(6, 0, 0, 0));

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(12); summaryGrid.setVgap(5);
        summaryGrid.setPadding(new Insets(6, 0, 0, 0));
        ColumnConstraints sc1 = new ColumnConstraints(140);
        ColumnConstraints sc2 = new ColumnConstraints(); sc2.setHgrow(Priority.ALWAYS);
        summaryGrid.getColumnConstraints().addAll(sc1, sc2);
        summaryGrid.addRow(0, new Label("Tạm tính:"),       lblSub);
        summaryGrid.addRow(1, lblVATLabel, lblVAT);
        Label totTitle = new Label("Tổng cộng:");
        totTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        summaryGrid.addRow(2, totTitle, lblTotalVal);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setVisible(false); lblError.setManaged(false);

        VBox content = new VBox(10, headerGrid, new Separator(), detailTable, addRow, summaryGrid, lblError);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(740);
        dialog.getDialogPane().setPrefHeight(660);

        ButtonType btnSave   = new ButtonType("Lưu phiếu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Đóng",       ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add(Styles.ACCENT);

        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (ComboBoxHelper.safeGetValue(cbCustomer) == null) {
                lblError.setText("⚠  Vui lòng chọn khách hàng!");
                lblError.setVisible(true); lblError.setManaged(true); e.consume(); return;
            }
            if (details.isEmpty()) {
                lblError.setText("⚠  Phải có ít nhất 1 sản phẩm!");
                lblError.setVisible(true); lblError.setManaged(true); e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                Customer cust = ComboBoxHelper.safeGetValue(cbCustomer);
                double sub = details.stream().mapToDouble(SalesOrderDetail::getTotal).sum();
                double vatRate = 10;
                try { vatRate = Double.parseDouble(tfVatRate.getText().trim()); } catch (NumberFormatException ignored) {}
                double totalWithVAT = Math.round(sub * (1 + vatRate / 100) * 100.0) / 100.0;
                double paid = 0;
                try { paid = Double.parseDouble(tfPaid.getText().trim().replace(",", "")); }
                catch (NumberFormatException ignored) {}
                SalesOrder order = isEdit ? existing : new SalesOrder();
                order.setOrderNumber(orderNum);
                order.setCustomerId(cust.getId());
                order.setCustomerName(cust.getName());
                order.setOrderDate(dpDate.getValue());
                order.setTotalAmount(totalWithVAT);
                order.setPaidAmount(paid);
                order.setNote(tfNote.getText().trim());
                order.setDetails(new java.util.ArrayList<>(details));
                if (!isEdit) {
                    try {
                        orderService.create(order);
                    } catch (IllegalStateException ise) {
                        lblError.setText("⚠  " + ise.getMessage());
                        lblError.setVisible(true); lblError.setManaged(true);
                        return null;
                    }
                } else {
                    try {
                        orderService.update(order);
                    } catch (IllegalStateException ise) {
                        lblError.setText("⚠  " + ise.getMessage());
                        lblError.setVisible(true); lblError.setManaged(true);
                        return null;
                    }
                }
                return order;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(o -> loadData());
    }

    private void confirmDelete(SalesOrder order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa phiếu xuất: " + order.getOrderNumber());
        alert.setContentText("Tồn kho sẽ được hoàn lại. Bạn có chắc muốn xóa?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                orderService.delete(order.getId());
                loadData();
            }
        });
    }
}

