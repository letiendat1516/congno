package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.dao.impl.ProductDAOImpl;
import com.dat.whmanagement.dao.impl.ProductServiceImpl;
import com.dat.whmanagement.model.*;
import com.dat.whmanagement.service.CustomerService;
import com.dat.whmanagement.service.ProductService;
import com.dat.whmanagement.service.ReturnOrderService;
import com.dat.whmanagement.service.impl.CustomerServiceImpl;
import com.dat.whmanagement.service.impl.ReturnOrderServiceImpl;
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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReturnOrderPanel extends BorderPane {

    private static final NumberFormat      CURRENCY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DTF      = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReturnOrderService service         = new ReturnOrderServiceImpl();
    private final CustomerService    customerService = new CustomerServiceImpl();
    private final ProductService     productService  = new ProductServiceImpl(new ProductDAOImpl());

    private final ObservableList<ReturnOrder>       masterList = FXCollections.observableArrayList();
    private final ObservableList<ReturnOrderDetail> detailList = FXCollections.observableArrayList();
    private       FilteredList<ReturnOrder>         filteredList;

    public ReturnOrderPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildSplitPane());
        loadData();
    }

    // ─────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────
    private VBox buildHeader() {
        Label title    = new Label("Trả hàng");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Nhận hàng trả lại từ khách hàng — tồn kho được cộng lại");
        subtitle.setStyle("-fx-text-fill: #888;");

        Button btnAdd = new Button("Tạo phiếu trả hàng", new FontIcon(Material2OutlinedAL.ASSIGNMENT_RETURN));
        btnAdd.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAdd.setOnAction(e -> openDialog(null));

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm số phiếu, khách hàng...");
        search.setPrefWidth(260);
        search.getStyleClass().add(Styles.ROUNDED);
        search.textProperty().addListener((obs, o, v) -> {
            if (filteredList == null) return;
            String kw = v == null ? "" : v.trim().toLowerCase();
            filteredList.setPredicate(r -> kw.isEmpty()
                    || r.getOrderNumber().toLowerCase().contains(kw)
                    || (r.getCustomerName() != null && r.getCustomerName().toLowerCase().contains(kw)));
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
        TableView<ReturnOrder> orderTable = new TableView<>();
        orderTable.getStyleClass().add(Styles.STRIPED);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        orderTable.setPlaceholder(new Label("Chưa có phiếu trả hàng nào"));

        TableColumn<ReturnOrder, String> colNum = new TableColumn<>("Số phiếu");
        colNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber")); colNum.setPrefWidth(120);

        TableColumn<ReturnOrder, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName")); colCustomer.setPrefWidth(200);

        TableColumn<ReturnOrder, LocalDate> colDate = new TableColumn<>("Ngày trả");
        colDate.setCellValueFactory(new PropertyValueFactory<>("returnDate")); colDate.setPrefWidth(110);
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DTF));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<ReturnOrder, Double> colTotal = new TableColumn<>("Tổng tiền trả");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount")); colTotal.setPrefWidth(140);
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(CURRENCY.format(v) + " ₫");
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #E53935;");
            }
        });

        TableColumn<ReturnOrder, String> colNote = new TableColumn<>("Ghi chú");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note")); colNote.setPrefWidth(180);

        TableColumn<ReturnOrder, Void> colAction = new TableColumn<>("Thao tác");
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

        orderTable.getColumns().addAll(colNum, colCustomer, colDate, colTotal, colNote, colAction);
        filteredList = new FilteredList<>(masterList, p -> true);
        orderTable.setItems(filteredList);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { detailList.clear(); return; }
            detailList.setAll(service.getDetails(sel.getId()));
        });

        // ── Bảng chi tiết (dưới) ──
        Label detailTitle = new Label("Chi tiết sản phẩm trả");
        detailTitle.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 6 0 4 0;");

        TableView<ReturnOrderDetail> detailTable = new TableView<>(detailList);
        detailTable.getStyleClass().add(Styles.STRIPED);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        detailTable.setPlaceholder(new Label("← Chọn một phiếu để xem chi tiết"));

        TableColumn<ReturnOrderDetail, String> dCode  = new TableColumn<>("Mã sản phẩm");
        dCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode())); dCode.setPrefWidth(110);
        TableColumn<ReturnOrderDetail, String> dName  = new TableColumn<>("Tên sản phẩm");
        dName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName())); dName.setPrefWidth(220);
        TableColumn<ReturnOrderDetail, String> dUnit  = new TableColumn<>("ĐVT");
        dUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit())); dUnit.setPrefWidth(70);
        dUnit.setStyle("-fx-alignment: CENTER;");
        TableColumn<ReturnOrderDetail, String> dQty   = new TableColumn<>("Số lượng trả");
        dQty.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getQuantity())));
        dQty.setPrefWidth(100); dQty.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<ReturnOrderDetail, String> dPrice = new TableColumn<>("Đơn giá");
        dPrice.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dPrice.setPrefWidth(130); dPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<ReturnOrderDetail, String> dTotal = new TableColumn<>("Thành tiền");
        dTotal.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dTotal.setPrefWidth(140); dTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #E53935;");

        detailTable.getColumns().addAll(dCode, dName, dUnit, dQty, dPrice, dTotal);

        VBox detailPane = new VBox(0, detailTitle, detailTable);
        detailPane.setPadding(new Insets(4, 0, 0, 0));
        VBox.setVgrow(detailTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(orderTable, detailPane);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.55);
        BorderPane.setMargin(split, new Insets(8, 0, 0, 0));
        return split;
    }

    private void loadData() { masterList.setAll(service.getAll()); detailList.clear(); }

    // ─────────────────────────────────────────────
    // DIALOG TẠO / SỬA PHIẾU TRẢ HÀNG
    // ─────────────────────────────────────────────
    private void openDialog(ReturnOrder existing) {
        boolean isEdit = (existing != null);
        Dialog<ReturnOrder> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Sửa phiếu trả hàng" : "Tạo phiếu trả hàng");
        dialog.setResizable(true);

        String orderNum = isEdit ? existing.getOrderNumber() : service.nextOrderNumber();
        Label lblNum = new Label(orderNum);
        lblNum.setFont(Font.font("System", FontWeight.BOLD, 14));

        List<Customer> customers = customerService.getAll();
        ComboBox<Customer> cbCustomer = new ComboBox<>();
        cbCustomer.setPromptText("Gõ mã/tên KH để tìm *"); cbCustomer.setPrefWidth(260);
        ComboBoxHelper.makeSearchable(cbCustomer, customers,
                c -> c.getCode() + " - " + c.getName(),
                c -> (c.getCode() + " " + c.getName()).toLowerCase());

        DatePicker dpDate = new DatePicker(isEdit ? existing.getReturnDate() : LocalDate.now());
        TextField  tfNote = new TextField(isEdit && existing.getNote() != null ? existing.getNote() : "");
        tfNote.setPromptText("Lý do trả hàng...");
        TextField tfVatRate = new TextField("10");
        tfVatRate.setPromptText("% thuế");
        tfVatRate.setPrefWidth(80);

        if (isEdit) {
            customers.stream().filter(c -> c.getId() == existing.getCustomerId())
                    .findFirst().ifPresent(cbCustomer::setValue);
        }

        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(12); headerGrid.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        headerGrid.getColumnConstraints().addAll(c1, c2);
        headerGrid.addRow(0, new Label("Số phiếu:"),   lblNum);
        headerGrid.addRow(1, new Label("Khách hàng *:"), cbCustomer);
        headerGrid.addRow(2, new Label("Ngày trả:"),    dpDate);
        headerGrid.addRow(3, new Label("Thuế (%):"),    tfVatRate);
        headerGrid.addRow(4, new Label("Lý do:"),       tfNote);

        ObservableList<ReturnOrderDetail> details = FXCollections.observableArrayList();
        if (isEdit) details.setAll(service.getDetails(existing.getId()));

        TableView<ReturnOrderDetail> detailTable = new TableView<>(details);
        detailTable.setPrefHeight(190);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ReturnOrderDetail, String> dCode  = new TableColumn<>("Mã SP");
        dCode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductCode())); dCode.setPrefWidth(90);
        TableColumn<ReturnOrderDetail, String> dName  = new TableColumn<>("Tên sản phẩm");
        dName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName())); dName.setPrefWidth(170);
        TableColumn<ReturnOrderDetail, String> dUnit  = new TableColumn<>("ĐVT");
        dUnit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnit())); dUnit.setPrefWidth(60);
        dUnit.setStyle("-fx-alignment: CENTER;");
        TableColumn<ReturnOrderDetail, String> dQty   = new TableColumn<>("Số lượng");
        dQty.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getQuantity())));
        dQty.setPrefWidth(80); dQty.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<ReturnOrderDetail, String> dPrice = new TableColumn<>("Đơn giá");
        dPrice.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getUnitPrice()) + " ₫"));
        dPrice.setPrefWidth(110); dPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        TableColumn<ReturnOrderDetail, String> dTotal = new TableColumn<>("Thành tiền");
        dTotal.setCellValueFactory(cd -> new SimpleStringProperty(CURRENCY.format(cd.getValue().getTotal()) + " ₫"));
        dTotal.setPrefWidth(120); dTotal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        Label lblSubVal   = new Label("0 ₫");
        Label lblVATVal   = new Label("0 ₫");
        Label lblTotalVal = new Label("0 ₫");
        Label lblVATLabel = new Label("Thuế (10%):");
        lblTotalVal.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblTotalVal.setStyle("-fx-text-fill: #E53935;");
        Runnable refreshTotal = () -> {
            double sub = details.stream().mapToDouble(ReturnOrderDetail::getTotal).sum();
            double vatRate = 10;
            try { vatRate = Double.parseDouble(tfVatRate.getText().trim()); } catch (NumberFormatException ignored) {}
            double vat = sub * vatRate / 100;
            double total = sub + vat;
            lblSubVal.setText(CURRENCY.format(sub) + " ₫");
            lblVATLabel.setText("Thuế (" + (int) vatRate + "%):");
            lblVATVal.setText(CURRENCY.format(vat) + " ₫");
            lblTotalVal.setText(CURRENCY.format(total) + " ₫");
        };
        details.addListener((javafx.collections.ListChangeListener<ReturnOrderDetail>) c -> refreshTotal.run());
        refreshTotal.run();
        tfVatRate.textProperty().addListener((obs, o, v) -> refreshTotal.run());

        TableColumn<ReturnOrderDetail, Void> dDel = new TableColumn<>("");
        dDel.setPrefWidth(40);
        dDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            { btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
              btn.setOnAction(e -> details.remove(getIndex())); }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
        });
        detailTable.getColumns().addAll(dCode, dName, dUnit, dQty, dPrice, dTotal, dDel);

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
                ReturnOrderDetail d = new ReturnOrderDetail();
                d.setProductId(sel.getId()); d.setProductCode(sel.getCode());
                d.setProductName(sel.getName()); d.setUnit(sel.getUnit() != null ? sel.getUnit() : "");
                d.setQuantity(qty); d.setUnitPrice(price); d.setTotal(qty * price);
                details.add(d);
                cbProd.setValue(null); tfQty.clear(); tfPrice.clear();
            } catch (NumberFormatException ignored) {}
        });

        HBox addRow = new HBox(8, cbProd, tfQty, tfPrice, btnAddRow);
        addRow.setAlignment(Pos.CENTER_LEFT); addRow.setPadding(new Insets(6, 0, 0, 0));

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(12); summaryGrid.setVgap(4);
        summaryGrid.setStyle("-fx-padding: 6 0 0 0;");
        summaryGrid.addRow(0, new Label("Tiền hàng:"), lblSubVal);
        summaryGrid.addRow(1, lblVATLabel, lblVATVal);
        Label totTitle = new Label("Tổng tiền trả:");
        totTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        summaryGrid.addRow(2, totTitle, lblTotalVal);
        HBox totalBar = new HBox(summaryGrid);
        totalBar.setAlignment(Pos.CENTER_RIGHT);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setVisible(false); lblError.setManaged(false);

        VBox content = new VBox(10, headerGrid, new Separator(), detailTable, addRow, totalBar, lblError);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(720);
        dialog.getDialogPane().setPrefHeight(620);

        ButtonType btnSave = new ButtonType("Lưu phiếu", ButtonBar.ButtonData.OK_DONE);
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
                ReturnOrder order = isEdit ? existing : new ReturnOrder();
                order.setOrderNumber(orderNum);
                order.setCustomerId(cust.getId());
                order.setCustomerName(cust.getName());
                order.setReturnDate(dpDate.getValue());
                double sub = details.stream().mapToDouble(ReturnOrderDetail::getTotal).sum();
                double vatRate = 10;
                try { vatRate = Double.parseDouble(tfVatRate.getText().trim()); } catch (NumberFormatException ignored) {}
                order.setTotalAmount(sub + sub * vatRate / 100);
                order.setNote(tfNote.getText().trim());
                order.setDetails(new java.util.ArrayList<>(details));
                if (!isEdit) service.create(order);
                else service.update(order);
                return order;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(o -> loadData());
    }

    private void confirmDelete(ReturnOrder order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa phiếu trả hàng: " + order.getOrderNumber());
        alert.setContentText("Tồn kho sẽ được điều chỉnh lại. Bạn có chắc muốn xóa?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(order.getId());
                loadData();
            }
        });
    }
}

