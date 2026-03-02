package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.model.DebtSummary;
import com.dat.whmanagement.model.DebtSummary.TargetType;
import com.dat.whmanagement.model.OrderLine;
import com.dat.whmanagement.model.PaymentRecord;
import com.dat.whmanagement.service.DebtService;
import com.dat.whmanagement.service.impl.DebtServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import java.util.Optional;

public class DebtPanel extends BorderPane {

    private static final NumberFormat  CURRENCY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DTF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DebtService service = new DebtServiceImpl();

    private final ObservableList<DebtSummary> masterList    = FXCollections.observableArrayList();
    private       FilteredList<DebtSummary>   filteredList;

    private final ObservableList<OrderLine>     orderLines    = FXCollections.observableArrayList();
    private final ObservableList<PaymentRecord> paymentHistory = FXCollections.observableArrayList();

    private TargetType currentTab = TargetType.CUSTOMER;

    private Label lblTotalDebt, lblTotalPaid, lblTotalRemaining;
    private Label lblDetailName;

    public DebtPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildSplitPane());
        loadData();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private VBox buildHeader() {
        Label title    = new Label("Công nợ / Thanh toán");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Theo dõi công nợ và lịch sử thanh toán");
        subtitle.setStyle("-fx-text-fill: #888;");

        ToggleGroup tg = new ToggleGroup();
        ToggleButton tabCust = new ToggleButton("Khách hàng"); tabCust.setToggleGroup(tg); tabCust.setSelected(true);
        ToggleButton tabSupp = new ToggleButton("Nhà cung cấp"); tabSupp.setToggleGroup(tg);
        tabCust.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        tabSupp.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        tabCust.setOnAction(e -> { currentTab = TargetType.CUSTOMER; loadData(); });
        tabSupp.setOnAction(e -> { currentTab = TargetType.SUPPLIER; loadData(); });

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, mã...");
        search.setPrefWidth(260);
        search.getStyleClass().add(Styles.ROUNDED);
        search.textProperty().addListener((obs, o, v) -> applyFilter(v));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actionBar = new HBox(10, search, tabCust, tabSupp, spacer);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(14, 0, 8, 0));

        // Summary cards
        lblTotalDebt      = new Label("0 ₫");
        lblTotalPaid      = new Label("0 ₫");
        lblTotalRemaining = new Label("0 ₫");
        HBox summaryBar = new HBox(16,
                buildCard("Tổng tiền", lblTotalDebt, "#1976D2"),
                buildCard("Đã thanh toán",  lblTotalPaid, "#388E3C"),
                buildCard("Còn nợ",         lblTotalRemaining, "#E53935"));
        summaryBar.setPadding(new Insets(0, 0, 8, 0));

        return new VBox(4, new VBox(2, title, subtitle), actionBar, summaryBar);
    }

    private VBox buildCard(String lbl, Label val, String color) {
        Label l = new Label(lbl); l.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        val.setFont(Font.font("System", FontWeight.BOLD, 16));
        val.setStyle("-fx-text-fill: " + color + ";");
        VBox card = new VBox(2, l, val);
        card.setPadding(new Insets(10, 20, 10, 20)); card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        return card;
    }

    // ─────────────────────────────────────────
    // SPLIT PANE
    // ─────────────────────────────────────────
    private SplitPane buildSplitPane() {
        SplitPane split = new SplitPane(buildDebtTable(), buildDetailPane());
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.42);
        BorderPane.setMargin(split, new Insets(4, 0, 0, 0));
        return split;
    }

    // ─────────────────────────────────────────
    // BẢNG TỔNG HỢP CÔNG NỢ (trên)
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<DebtSummary> buildDebtTable() {
        TableView<DebtSummary> table = new TableView<>();
        table.getStyleClass().add(Styles.STRIPED);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Không có công nợ nào"));

        TableColumn<DebtSummary, String>  colCode  = col("Mã",           "targetCode",  100);
        TableColumn<DebtSummary, String>  colName  = col("Tên",          "targetName",  210);
        TableColumn<DebtSummary, String>  colPhone = col("Điện thoại",   "phone",       120);

        TableColumn<DebtSummary, Double> colTotal = new TableColumn<>("Tổng tiền");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colTotal.setPrefWidth(140);
        colTotal.setCellFactory(c -> currencyCell("#333"));

        TableColumn<DebtSummary, Double> colPaid = new TableColumn<>("Đã thanh toán");
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colPaid.setPrefWidth(140);
        colPaid.setCellFactory(c -> currencyCell("#388E3C"));

        TableColumn<DebtSummary, Double> colDebt = new TableColumn<>("Còn nợ");
        colDebt.setCellValueFactory(new PropertyValueFactory<>("debtAmount"));
        colDebt.setPrefWidth(130);
        colDebt.setCellFactory(c -> currencyCell("#E53935"));

        TableColumn<DebtSummary, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(120);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnPay = new Button("Thanh toán",
                    new FontIcon(Material2OutlinedMZ.PAYMENT));
            {
                btnPay.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
                btnPay.setStyle("-fx-font-size: 11px;");
                btnPay.setOnAction(e -> openPaymentDialog(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                DebtSummary item = getTableView().getItems().get(getIndex());
                setGraphic(item.getDebtAmount() > 0.005 ? btnPay : null);
            }
        });

        table.getColumns().addAll(colCode, colName, colPhone, colTotal, colPaid, colDebt, colAction);
        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);

        // Khi click hàng → load chi tiết bên dưới
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { orderLines.clear(); paymentHistory.clear(); lblDetailName.setText(""); return; }
            lblDetailName.setText(sel.getTargetName() + "  —  Còn nợ: " + CURRENCY.format(sel.getDebtAmount()) + " ₫");
            orderLines.setAll(service.getOrderLines(sel.getTargetType().name(), sel.getTargetId()));
            paymentHistory.setAll(service.getPaymentHistory(sel.getTargetType().name(), sel.getTargetId()));
        });

        return table;
    }

    private <T> TableColumn<DebtSummary, T> col(String title, String prop, double width) {
        TableColumn<DebtSummary, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    // ─────────────────────────────────────────
    // DETAIL PANE (dưới) – Tabs: Chi tiết đơn hàng | Lịch sử thanh toán
    // ─────────────────────────────────────────
    private VBox buildDetailPane() {
        lblDetailName = new Label("← Chọn một khách hàng / NCC để xem chi tiết");
        lblDetailName.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 4 0 4 0;");
        lblDetailName.setFont(Font.font("System", FontWeight.BOLD, 13));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("📋  Chi tiết đơn hàng",      buildOrderLinesTable()),
                new Tab("💳  Lịch sử thanh toán",     buildPaymentHistoryTable())
        );

        VBox pane = new VBox(0, lblDetailName, tabs);
        pane.setPadding(new Insets(6, 0, 0, 0));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return pane;
    }

    // ─────────────────────────────────────────
    // CHI TIẾT ĐƠN HÀNG TABLE
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<OrderLine> buildOrderLinesTable() {
        TableView<OrderLine> t = new TableView<>(orderLines);
        t.getStyleClass().add(Styles.STRIPED);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        t.setPlaceholder(new Label("Chưa có đơn hàng nào"));

        TableColumn<OrderLine, LocalDate> colDate = new TableColumn<>("Ngày");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDate.setPrefWidth(100);
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DTF));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<OrderLine, String> colNum = new TableColumn<>("Số phiếu");
        colNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colNum.setPrefWidth(100);

        TableColumn<OrderLine, String> colCode = new TableColumn<>("Mã SP");
        colCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colCode.setPrefWidth(90);

        TableColumn<OrderLine, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colName.setPrefWidth(190);

        TableColumn<OrderLine, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setPrefWidth(65);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<OrderLine, Double> colQty = new TableColumn<>("Số lượng");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(80);
        colQty.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f", v));
                if (!empty) setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        TableColumn<OrderLine, Double> colUnitPrice = new TableColumn<>("Đơn giá");
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colUnitPrice.setPrefWidth(115);
        colUnitPrice.setCellFactory(c -> currencyCell2("#333"));

        TableColumn<OrderLine, Double> colReturnQty = new TableColumn<>("SL hàng trả");
        colReturnQty.setCellValueFactory(new PropertyValueFactory<>("returnQuantity"));
        colReturnQty.setPrefWidth(100);
        colReturnQty.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.2f", v));
                String clr = v > 0.005 ? "#E53935" : "#555";
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: " + clr + ";");
            }
        });

        TableColumn<OrderLine, Double> colActualQty = new TableColumn<>("SL thực lấy");
        colActualQty.setCellValueFactory(new PropertyValueFactory<>("actualQuantity"));
        colActualQty.setPrefWidth(100);
        colActualQty.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.2f", v));
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #388E3C;");
            }
        });

        t.getColumns().addAll(colDate, colNum, colCode, colName, colUnit,
                colQty, colUnitPrice, colReturnQty, colActualQty);
        return t;
    }

    // ─────────────────────────────────────────
    // LỊCH SỬ THANH TOÁN TABLE
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<PaymentRecord> buildPaymentHistoryTable() {
        TableView<PaymentRecord> t = new TableView<>(paymentHistory);
        t.getStyleClass().add(Styles.STRIPED);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        t.setPlaceholder(new Label("Chưa có thanh toán nào"));

        TableColumn<PaymentRecord, LocalDate> colDate = new TableColumn<>("Ngày TT");
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colDate.setPrefWidth(110);
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DTF));
                if (!empty) setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<PaymentRecord, Double> colAmt = new TableColumn<>("Số tiền thanh toán");
        colAmt.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmt.setPrefWidth(180);
        colAmt.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(CURRENCY.format(v) + " ₫");
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #388E3C;");
            }
        });

        TableColumn<PaymentRecord, String> colNote = new TableColumn<>("Ghi chú");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setPrefWidth(260);

        TableColumn<PaymentRecord, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(100);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("", new FontIcon(Material2OutlinedAL.EDIT));
            private final Button btnDel  = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                btnDel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> openEditPaymentDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e  -> confirmDeletePayment(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : box);
            }
        });

        t.getColumns().addAll(colDate, colAmt, colNote, colAction);
        return t;
    }

    // ─────────────────────────────────────────
    // DATA
    // ─────────────────────────────────────────
    private void loadData() {
        List<DebtSummary> data = currentTab == TargetType.CUSTOMER
                ? service.getCustomerDebts() : service.getSupplierDebts();
        masterList.setAll(data);
        orderLines.clear(); paymentHistory.clear();
        if (lblDetailName != null) lblDetailName.setText("← Chọn một khách hàng / NCC để xem chi tiết");

        double sumTotal = data.stream().mapToDouble(DebtSummary::getTotalAmount).sum();
        double sumPaid  = data.stream().mapToDouble(DebtSummary::getPaidAmount).sum();
        double sumDebt  = data.stream().mapToDouble(DebtSummary::getDebtAmount).sum();
        lblTotalDebt.setText(CURRENCY.format(sumTotal) + " ₫");
        lblTotalPaid.setText(CURRENCY.format(sumPaid)  + " ₫");
        lblTotalRemaining.setText(CURRENCY.format(sumDebt) + " ₫");
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredList.setPredicate(d ->
                kw.isEmpty()
                        || d.getTargetName().toLowerCase().contains(kw)
                        || (d.getTargetCode() != null && d.getTargetCode().toLowerCase().contains(kw)));
    }

    // ─────────────────────────────────────────
    // DIALOG THANH TOÁN MỚI
    // ─────────────────────────────────────────
    private void openPaymentDialog(DebtSummary debt) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Ghi nhận thanh toán");
        dialog.setHeaderText(debt.getTargetName() + "  —  Còn nợ: " + CURRENCY.format(debt.getDebtAmount()) + " ₫");

        TextField tfAmount = new TextField(String.valueOf((long) debt.getDebtAmount()));
        tfAmount.getStyleClass().add(Styles.ROUNDED);
        TextField tfNote   = new TextField();
        tfNote.setPromptText("VD: Thanh toán đợt 1...");
        tfNote.getStyleClass().add(Styles.ROUNDED);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setManaged(false); lblError.setVisible(false);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Số tiền (₫) *"), tfAmount);
        grid.addRow(1, new Label("Ghi chú"),        tfNote);
        grid.add(lblError, 0, 2, 2, 1);
        GridPane.setHgrow(tfAmount, Priority.ALWAYS);
        GridPane.setHgrow(tfNote,   Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType btnOK = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK,
                new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE));
        Button confirmBtn = (Button) dialog.getDialogPane().lookupButton(btnOK);
        confirmBtn.getStyleClass().add(Styles.ACCENT);

        confirmBtn.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                double a = Double.parseDouble(tfAmount.getText().trim().replace(",", ""));
                if (a <= 0) throw new NumberFormatException();
                if (a > debt.getDebtAmount() + 0.01) {
                    lblError.setText("⚠  Số tiền vượt quá số nợ!"); lblError.setManaged(true); lblError.setVisible(true); e.consume();
                }
            } catch (NumberFormatException ex) {
                lblError.setText("⚠  Số tiền không hợp lệ!"); lblError.setManaged(true); lblError.setVisible(true); e.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == btnOK
                ? Double.parseDouble(tfAmount.getText().trim().replace(",", "")) : null);

        dialog.showAndWait().ifPresent(amount -> {
            service.recordPayment(debt.getTargetType().name(), debt.getTargetId(), amount, tfNote.getText().trim());
            loadData();
            new Alert(Alert.AlertType.INFORMATION, "Đã ghi nhận thanh toán " + CURRENCY.format(amount) + " ₫",
                    ButtonType.OK).showAndWait();
        });
    }

    // ─────────────────────────────────────────
    // DIALOG EDIT THANH TOÁN
    // ─────────────────────────────────────────
    private void openEditPaymentDialog(PaymentRecord pr) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Sửa thanh toán");
        dialog.setHeaderText("Chỉnh sửa khoản thanh toán ngày " + pr.getPaymentDate().format(DTF));

        TextField tfAmount = new TextField(String.valueOf((long) pr.getAmount()));
        tfAmount.getStyleClass().add(Styles.ROUNDED);
        TextField tfNote = new TextField(pr.getNote() != null ? pr.getNote() : "");
        tfNote.getStyleClass().add(Styles.ROUNDED);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setManaged(false); lblError.setVisible(false);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Số tiền (₫) *"), tfAmount);
        grid.addRow(1, new Label("Ghi chú"),        tfNote);
        grid.add(lblError, 0, 2, 2, 1);
        GridPane.setHgrow(tfAmount, Priority.ALWAYS);
        GridPane.setHgrow(tfNote,   Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK,
                new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE));
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnOK);
        saveBtn.getStyleClass().add(Styles.ACCENT);

        saveBtn.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                double a = Double.parseDouble(tfAmount.getText().trim().replace(",", ""));
                if (a <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                lblError.setText("⚠  Số tiền không hợp lệ!"); lblError.setManaged(true); lblError.setVisible(true); e.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == btnOK);
        dialog.showAndWait().ifPresent(ok -> {
            if (!ok) return;
            double newAmount = Double.parseDouble(tfAmount.getText().trim().replace(",", ""));
            service.updatePayment(pr.getId(), newAmount, tfNote.getText().trim());
            // Reload detail for currently selected row
            pr.setAmount(newAmount); pr.setNote(tfNote.getText().trim());
            // Trigger full refresh
            loadData();
        });
    }

    // ─────────────────────────────────────────
    // XÁC NHẬN XÓA THANH TOÁN
    // ─────────────────────────────────────────
    private void confirmDeletePayment(PaymentRecord pr) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa thanh toán " + CURRENCY.format(pr.getAmount()) + " ₫ ngày " + pr.getPaymentDate().format(DTF) + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.deletePayment(pr.getId());
                loadData();
            }
        });
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private TableCell<DebtSummary, Double> currencyCell(String color) {
        return new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(CURRENCY.format(v) + " ₫");
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: " + color + ";");
            }
        };
    }

    private TableCell<OrderLine, Double> currencyCell2(String color) {
        return new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(CURRENCY.format(v) + " ₫");
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: " + color + ";");
            }
        };
    }
}
