package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.model.Supplier;
import com.dat.whmanagement.service.SupplierService;
import com.dat.whmanagement.service.impl.SupplierServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SupplierPanel extends BorderPane {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SupplierService service = new SupplierServiceImpl();
    private final ObservableList<Supplier> masterList = FXCollections.observableArrayList();
    private FilteredList<Supplier> filteredList;

    public SupplierPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildTable());
        loadData();
    }

    private VBox buildHeader() {
        Label title = new Label("Nhà cung cấp");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Quản lý danh sách nhà cung cấp");
        subtitle.setStyle("-fx-text-fill: #888;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm theo tên, mã...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add(Styles.ROUNDED);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));

        Button btnAdd = new Button("Thêm NCC", new FontIcon(Material2OutlinedAL.ADD));
        btnAdd.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAdd.setOnAction(e -> openDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(10, searchField, spacer, btnAdd);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(16, 0, 8, 0));

        return new VBox(4, new VBox(2, title, subtitle), actionBar);
    }

    @SuppressWarnings("unchecked")
    private TableView<Supplier> buildTable() {
        TableView<Supplier> table = new TableView<>();
        table.getStyleClass().add(Styles.STRIPED);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Chưa có nhà cung cấp nào"));

        TableColumn<Supplier, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Supplier, String> colCode = new TableColumn<>("Mã NCC");
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCode.setPrefWidth(100);

        TableColumn<Supplier, String> colName = new TableColumn<>("Tên NCC");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(220);

        TableColumn<Supplier, String> colPhone = new TableColumn<>("Điện thoại");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(120);

        TableColumn<Supplier, String> colAddress = new TableColumn<>("Địa chỉ");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddress.setPrefWidth(200);

        TableColumn<Supplier, LocalDateTime> colDate = new TableColumn<>("Ngày tạo");
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setPrefWidth(140);
        colDate.setCellFactory(col -> new TableCell<Supplier, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                setText(val.format(DTF));
                setStyle("-fx-text-fill: #888; -fx-alignment: CENTER;");
            }
        });

        TableColumn<Supplier, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("", new FontIcon(Material2OutlinedAL.EDIT));
            private final Button btnDel  = new Button("", new FontIcon(Material2OutlinedAL.DELETE));
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                btnDel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colId, colCode, colName, colPhone, colAddress, colDate, colAction);
        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);
        BorderPane.setMargin(table, new Insets(8, 0, 0, 0));
        return table;
    }

    private void loadData() { masterList.setAll(service.getAll()); }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredList.setPredicate(s ->
                kw.isEmpty()
                        || s.getName().toLowerCase().contains(kw)
                        || (s.getCode() != null && s.getCode().toLowerCase().contains(kw))
                        || (s.getPhone() != null && s.getPhone().contains(kw))
        );
    }

    private void openDialog(Supplier existing) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
        dialog.setResizable(false);

        TextField tfCode    = new TextField();  tfCode.setPromptText("VD: NCC001");
        TextField tfName    = new TextField();  tfName.setPromptText("Tên NCC *");
        TextField tfPhone   = new TextField();  tfPhone.setPromptText("SĐT");
        TextField tfAddress = new TextField();  tfAddress.setPromptText("Địa chỉ");
        tfCode.getStyleClass().add(Styles.ROUNDED);
        tfName.getStyleClass().add(Styles.ROUNDED);
        tfPhone.getStyleClass().add(Styles.ROUNDED);
        tfAddress.getStyleClass().add(Styles.ROUNDED);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setVisible(false); lblError.setManaged(false);

        if (existing != null) {
            tfCode.setText(existing.getCode()); tfCode.setDisable(true);
            tfName.setText(existing.getName());
            tfPhone.setText(existing.getPhone());
            tfAddress.setText(existing.getAddress());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(80);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        grid.addRow(0, new Label("Mã NCC"),   tfCode);
        grid.addRow(1, new Label("Tên *"),     tfName);
        grid.addRow(2, new Label("SĐT"),       tfPhone);
        grid.addRow(3, new Label("Địa chỉ"),   tfAddress);
        grid.add(lblError, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType btnSave   = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);
        ((Button) dialog.getDialogPane().lookupButton(btnSave)).getStyleClass().add(Styles.ACCENT);

        ((Button) dialog.getDialogPane().lookupButton(btnSave)).addEventFilter(ActionEvent.ACTION, e -> {
            if (tfName.getText().trim().isEmpty()) {
                lblError.setText("⚠  Tên NCC không được rỗng!");
                lblError.setVisible(true); lblError.setManaged(true);
                e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                if (existing != null) {
                    existing.setName(tfName.getText().trim());
                    existing.setPhone(tfPhone.getText().trim());
                    existing.setAddress(tfAddress.getText().trim());
                    service.update(existing);
                    return existing;
                } else {
                    Supplier s = new Supplier(
                            tfCode.getText().trim(), tfName.getText().trim(),
                            tfPhone.getText().trim(), tfAddress.getText().trim());
                    return service.create(s);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> loadData());
    }

    private void confirmDelete(Supplier s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa: " + s.getName());
        alert.setContentText("Bạn có chắc muốn xóa nhà cung cấp này?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.delete(s.getId());
                    loadData();
                } catch (IllegalStateException ex) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Lỗi");
                    err.setContentText(ex.getMessage());
                    err.showAndWait();
                }
            }
        });
    }
}

