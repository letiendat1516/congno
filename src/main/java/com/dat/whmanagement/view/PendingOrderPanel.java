package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.dao.impl.ProductDAOImpl;
import com.dat.whmanagement.dao.impl.ProductServiceImpl;
import com.dat.whmanagement.model.*;
import com.dat.whmanagement.service.CustomerService;
import com.dat.whmanagement.service.PendingOrderService;
import com.dat.whmanagement.service.ProductService;
import com.dat.whmanagement.service.impl.CustomerServiceImpl;
import com.dat.whmanagement.service.impl.PendingOrderServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import com.dat.whmanagement.util.ComboBoxHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PendingOrderPanel extends BorderPane {

    private static final NumberFormat       CURRENCY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter  DTF      = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PendingOrderService service         = new PendingOrderServiceImpl();
    private final CustomerService     customerService = new CustomerServiceImpl();
    private final ProductService      productService  = new ProductServiceImpl(new ProductDAOImpl());

    private final ObservableList<PendingOrder>       masterList = FXCollections.observableArrayList();
    private final ObservableList<PendingOrderDetail> detailList = FXCollections.observableArrayList();
    private       FilteredList<PendingOrder>         filteredList;

    public PendingOrderPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildSplitPane());
        loadData();
    }

    // ─────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────
    private VBox buildHeader() {
        Label title    = new Label("Đơn đặt hàng");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Đơn khách đặt trước — chờ xuất kho khi có hàng");
        subtitle.setStyle("-fx-text-fill: #888;");

        Button btnAdd = new Button("Tạo đơn đặt hàng", new FontIcon(Material2OutlinedAL.ADD));
        btnAdd.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAdd.setOnAction(e -> openDialog());

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm số đơn, khách hàng...");
        search.setPrefWidth(260);
        search.getStyleClass().add(Styles.ROUNDED);
        search.textProperty().addListener((obs, o, v) -> {
            if (filteredList == null) return;
            String kw = v == null ? "" : v.trim().toLowerCase();
            filteredList.setPredicate(ord -> kw.isEmpty()
                    || ord.getOrderNumber().toLowerCase().contains(kw)
                    || (ord.getCustomerName() != null && ord.getCustomerName().toLowerCase().contains(kw)));
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actionBar = new HBox(10, search, spacer, btnAdd);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(16, 0, 8, 0));

        return new VBox(4, new VBox(2, title, subtitle), actionBar);
    }

    // ─────────────────────────────────────────────
    // SPLIT PANE
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private SplitPane buildSplitPane() {
        // ── Bảng đơn đặt hàng (trên) ──
        TableView<PendingOrder> orderTable = new TableView<>();
        orderTable.getStyleClass().add(Styles.STRIPED);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        orderTable.setPlaceholder(new Label("Chưa có đơn đặt hàng nào"));

        TableColumn<PendingOrder, String> colNum = new TableColumn<>("Số đơn");
        colNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber")); colNum.setPrefWidth(110);

        TableColumn<PendingOrder, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName")); colCustomer.setPrefWidth(190);

        TableColumn<PendingOrder, LocalDate> colDate = new TableColumn<>("Ngày đặt");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate")); colDate.setPrefWidth(100);
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DTF));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<PendingOrder, LocalDate> colExpected = new TableColumn<>("Ngày dự kiến");
        colExpected.setCellValueFactory(new PropertyValueFactory<>("expectedDate")); colExpected.setPrefWidth(110);
        colExpected.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DTF));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<PendingOrder, Double> colTotal = new TableColumn<>("Tổng tiền");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount")); colTotal.setPrefWidth(130);
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(CURRENCY.format(v) + " ₫");
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
            }
        });

        TableColumn<PendingOrder, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); colStatus.setPrefWidth(110);
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                String display;
                String color;
                switch (v) {
                    case "PENDING"   -> { display = "⏳ Chờ xuất";  color = "#E65100"; }
                    case "EXPORTED"  -> { display = "✅ Đã xuất";   color = "#388E3C"; }
                    case "CANCELLED" -> { display = "❌ Đã huỷ";    color = "#999";    }
                    default          -> { display = v;              color = "#333";    }
                }
                setText(display);
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });

        TableColumn<PendingOrder, String> colNote = new TableColumn<>("Ghi chú");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note")); colNote.setPrefWidth(130);

        TableColumn<PendingOrder, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(210);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnExport = new Button("Xuất kho", new FontIcon(Material2OutlinedAL.ARROW_CIRCLE_UP));
            private final Button btnCancel = new Button("Huỷ",      new FontIcon(Material2OutlinedMZ.REMOVE_CIRCLE));
            private final Button btnDel    = new Button("",         new FontIcon(Material2OutlinedAL.DELETE));
            private final HBox   boxPending = new HBox(6, btnExport, btnCancel, btnDel);
            private final HBox   boxOther   = new HBox(6, btnDel);
            {
                btnExport.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
                btnExport.setStyle("-fx-font-size: 11px;");
                btnCancel.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
                btnCancel.setStyle("-fx-font-size: 11px;");
                btnDel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                btnDel.setTooltip(new Tooltip("Xóa"));
                boxPending.setAlignment(Pos.CENTER);
                boxOther.setAlignment(Pos.CENTER);
                btnExport.setOnAction(e -> doExport(getTableView().getItems().get(getIndex())));
                btnCancel.setOnAction(e -> doCancel(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                PendingOrder item = getTableView().getItems().get(getIndex());
                setGraphic("PENDING".equals(item.getStatus()) ? boxPending : boxOther);
            }
        });

        orderTable.getColumns().addAll(colNum, colCustomer, colDate, colExpected, colTotal, colStatus, colNote, colAction);
        filteredList = new FilteredList<>(masterList, p -> true);
        orderTable.setItems(filteredList);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { detailList.clear(); return; }
            detailList.setAll(service.getDetails(sel.getId()));
        });

        // ── Bảng chi tiết sản phẩm (dưới) ──
        Label detailTitle = new Label("Chi tiết sản phẩm của đơn đã chọn");
        detailTitle.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 6 0 4 0;");

        TableView<PendingOrderDetail> detailTable = new TableView<>(detailList);
        detailTable.getStyleClass().add(Styles.STRIPED);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        detailTable.setPlaceholder(new Label("← Chọn một đơn để xem chi tiết"));

        TableColumn<PendingOrderDetail, String> dCode  = new TableColumn<>("Mã sản phẩm");
        dCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode())); dCode.setPrefWidth(110);
        TableColumn<PendingOrderDetail, String> dName  = new TableColumn<>("Tên sản phẩm");
        dName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName())); dName.setPrefWidth(220);
        TableColumn<PendingOrderDetail, String> dUnit  = new TableColumn<>("ĐVT");
        dUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit())); dUnit.setPrefWidth(70);
        dUnit.setStyle("-fx-alignment: CENTER;");
        TableColumn<PendingOrderDetail, String> dQty   = new TableColumn<>("Số lượng");
        dQty.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getQuantity())));
        dQty.setPrefWidth(90); dQty.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<PendingOrderDetail, String> dPrice = new TableColumn<>("Đơn giá");
        dPrice.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dPrice.setPrefWidth(120); dPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<PendingOrderDetail, String> dTotal = new TableColumn<>("Thành tiền");
        dTotal.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dTotal.setPrefWidth(130); dTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

        detailTable.getColumns().addAll(dCode, dName, dUnit, dQty, dPrice, dTotal);

        VBox detailPane = new VBox(0, detailTitle, detailTable);
        detailPane.setPadding(new Insets(4, 0, 0, 0));
        VBox.setVgrow(detailTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(orderTable, detailPane);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.58);
        BorderPane.setMargin(split, new Insets(8, 0, 0, 0));
        return split;
    }

    private void loadData() { masterList.setAll(service.getAll()); detailList.clear(); }

    // ─────────────────────────────────────────────
    // XUẤT KHO
    // ─────────────────────────────────────────────
    private void doExport(PendingOrder order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xuất kho");
        confirm.setHeaderText("Xuất kho đơn " + order.getOrderNumber() + " cho " + order.getCustomerName() + "?");
        confirm.setContentText("Thao tác này sẽ tạo phiếu xuất kho thực sự và trừ tồn kho.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                service.exportOrder(order.getId());
                loadData();
                new Alert(Alert.AlertType.INFORMATION, "✅ Đã xuất kho thành công! Phiếu xuất đã được tạo.", ButtonType.OK).showAndWait();
            } catch (IllegalStateException e) {
                new Alert(Alert.AlertType.ERROR, "❌ " + e.getMessage(), ButtonType.OK).showAndWait();
            }
        });
    }

    // ─────────────────────────────────────────────
    // HUỶ ĐƠN
    // ─────────────────────────────────────────────
    private void doCancel(PendingOrder order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận huỷ đơn");
        confirm.setHeaderText("Huỷ đơn đặt hàng " + order.getOrderNumber() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { service.cancelOrder(order.getId()); loadData(); }
        });
    }

    // ─────────────────────────────────────────────
    // DIALOG TẠO ĐƠN ĐẶT HÀNG
    // ─────────────────────────────────────────────
    private void openDialog() {
        Dialog<PendingOrder> dialog = new Dialog<>();
        dialog.setTitle("Tạo đơn đặt hàng");
        dialog.setResizable(true);

        String orderNum = service.nextOrderNumber();
        Label lblNum = new Label(orderNum);
        lblNum.setFont(Font.font("System", FontWeight.BOLD, 14));

        List<Customer> customers = customerService.getAll();
        ComboBox<Customer> cbCustomer = new ComboBox<>();
        cbCustomer.setPromptText("Gõ mã/tên KH để tìm *"); cbCustomer.setPrefWidth(260);
        ComboBoxHelper.makeSearchable(cbCustomer, customers, c -> c.getName());

        DatePicker dpOrder    = new DatePicker(LocalDate.now());
        DatePicker dpExpected = new DatePicker(LocalDate.now().plusWeeks(1));
        TextField  tfNote     = new TextField(); tfNote.setPromptText("Ghi chú...");

        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(12); headerGrid.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(110);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        headerGrid.getColumnConstraints().addAll(c1, c2);
        headerGrid.addRow(0, new Label("Số đơn:"),          lblNum);
        headerGrid.addRow(1, new Label("Khách hàng *:"),    cbCustomer);
        headerGrid.addRow(2, new Label("Ngày đặt:"),        dpOrder);
        headerGrid.addRow(3, new Label("Ngày dự kiến:"),    dpExpected);
        headerGrid.addRow(4, new Label("Ghi chú:"),         tfNote);

        // Chi tiết sản phẩm
        ObservableList<PendingOrderDetail> details = FXCollections.observableArrayList();
        TableView<PendingOrderDetail> detailTable = new TableView<>(details);
        detailTable.setPrefHeight(190);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<PendingOrderDetail, String> dCode  = new TableColumn<>("Mã SP");
        dCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode())); dCode.setPrefWidth(90);
        TableColumn<PendingOrderDetail, String> dName  = new TableColumn<>("Tên sản phẩm");
        dName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName())); dName.setPrefWidth(170);
        TableColumn<PendingOrderDetail, String> dUnit  = new TableColumn<>("ĐVT");
        dUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit())); dUnit.setPrefWidth(60);
        dUnit.setStyle("-fx-alignment: CENTER;");
        TableColumn<PendingOrderDetail, String> dQty   = new TableColumn<>("Số lượng");
        dQty.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getQuantity())));
        dQty.setPrefWidth(80); dQty.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<PendingOrderDetail, String> dPrice = new TableColumn<>("Đơn giá");
        dPrice.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dPrice.setPrefWidth(110); dPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<PendingOrderDetail, String> dTotal = new TableColumn<>("Thành tiền");
        dTotal.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dTotal.setPrefWidth(120); dTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
        TableColumn<PendingOrderDetail, Void> dDel = new TableColumn<>("");
        dDel.setPrefWidth(40);
        dDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            { btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
              btn.setOnAction(e -> details.remove(getIndex())); }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
        });
        detailTable.getColumns().addAll(dCode, dName, dUnit, dQty, dPrice, dTotal, dDel);

        Label lblTotalVal = new Label("0 ₫");
        lblTotalVal.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblTotalVal.setStyle("-fx-text-fill: #1976D2;");
        Runnable refreshTotal = () -> lblTotalVal.setText(CURRENCY.format(details.stream().mapToDouble(PendingOrderDetail::getTotal).sum()) + " ₫");
        details.addListener((javafx.collections.ListChangeListener<PendingOrderDetail>) c -> refreshTotal.run());

        List<Product> products = productService.getAll();
        ComboBox<Product> cbProd = new ComboBox<>();
        cbProd.setPromptText("Gõ mã/tên SP để tìm"); cbProd.setPrefWidth(210);
        ComboBoxHelper.makeSearchable(cbProd, products, p -> p.getCode() + " - " + p.getName());

        TextField tfQty   = new TextField(); tfQty.setPromptText("Số lượng"); tfQty.setPrefWidth(85);
        TextField tfPrice = new TextField(); tfPrice.setPromptText("Đơn giá"); tfPrice.setPrefWidth(110);
        Button btnAddRow  = new Button("+ Thêm", new FontIcon(Material2OutlinedAL.ADD));
        btnAddRow.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAddRow.setOnAction(e -> {
            Product sel = ComboBoxHelper.safeGetValue(cbProd); if (sel == null) return;
            try {
                double qty   = Double.parseDouble(tfQty.getText().trim());
                double price = Double.parseDouble(tfPrice.getText().trim().replace(",", ""));
                if (qty <= 0 || price < 0) return;
                PendingOrderDetail d = new PendingOrderDetail();
                d.setProductId(sel.getId()); d.setProductCode(sel.getCode());
                d.setProductName(sel.getName()); d.setUnit(sel.getUnit() != null ? sel.getUnit() : "");
                d.setQuantity(qty); d.setUnitPrice(price); d.setTotal(qty * price);
                details.add(d);
                cbProd.setValue(null); tfQty.clear(); tfPrice.clear();
            } catch (NumberFormatException ignored) {}
        });

        HBox addRow = new HBox(8, cbProd, tfQty, tfPrice, btnAddRow);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addRow.setPadding(new Insets(6, 0, 0, 0));

        HBox totalBar = new HBox(8, new Label("Tổng tiền (chưa VAT):"), lblTotalVal);
        totalBar.setAlignment(Pos.CENTER_RIGHT);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setVisible(false); lblError.setManaged(false);

        VBox content = new VBox(10, headerGrid, new Separator(), detailTable, addRow, totalBar, lblError);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(720);
        dialog.getDialogPane().setPrefHeight(630);

        ButtonType btnSave = new ButtonType("Lưu đơn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add(Styles.ACCENT);

        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (ComboBoxHelper.safeGetValue(cbCustomer) == null) {
                lblError.setText("⚠  Vui lòng chọn khách hàng!"); lblError.setVisible(true); lblError.setManaged(true); e.consume(); return;
            }
            if (details.isEmpty()) {
                lblError.setText("⚠  Phải có ít nhất 1 sản phẩm!"); lblError.setVisible(true); lblError.setManaged(true); e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                Customer cust = ComboBoxHelper.safeGetValue(cbCustomer);
                PendingOrder order = new PendingOrder();
                order.setOrderNumber(orderNum);
                order.setCustomerId(cust.getId());
                order.setCustomerName(cust.getName());
                order.setOrderDate(dpOrder.getValue());
                order.setExpectedDate(dpExpected.getValue());
                order.setTotalAmount(details.stream().mapToDouble(PendingOrderDetail::getTotal).sum());
                order.setNote(tfNote.getText().trim());
                order.setDetails(new java.util.ArrayList<>(details));
                service.create(order);
                return order;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(o -> loadData());
    }

    private void confirmDelete(PendingOrder order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa đơn đặt hàng: " + order.getOrderNumber());
        alert.setContentText("Bạn có chắc muốn xóa đơn này?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(order.getId());
                loadData();
            }
        });
    }
}

