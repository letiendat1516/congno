package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.dao.impl.ProductDAOImpl;
import com.dat.whmanagement.dao.impl.ProductServiceImpl;
import com.dat.whmanagement.model.Product;
import com.dat.whmanagement.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.util.Optional;

public class ProductPanel extends BorderPane {

    private final ProductService service = new ProductServiceImpl(new ProductDAOImpl());

    private final ObservableList<Product> masterList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredList;
    private TableView<Product> table;

    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProductPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildTable());
        loadData();
    }

    private VBox buildHeader() {
        Label title    = new Label("Sản phẩm");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        Label subtitle = new Label("Quản lý danh sách sản phẩm");
        subtitle.setStyle("-fx-text-fill: #888;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm theo tên, mã sản phẩm...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add(Styles.ROUNDED);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));

        Button btnAdd = new Button("Thêm sản phẩm", new FontIcon(Material2OutlinedAL.ADD));
        btnAdd.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        btnAdd.setOnAction(e -> openDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(10, searchField, spacer, btnAdd);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(16, 0, 8, 0));

        VBox header = new VBox(4, new VBox(2, title, subtitle), actionBar);
        return header;
    }

    @SuppressWarnings("unchecked")
    private TableView<Product> buildTable() {
        table = new TableView<>();
        table.getStyleClass().add(Styles.STRIPED);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Chưa có sản phẩm nào"));

        TableColumn<Product, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colCode = new TableColumn<>("Mã SP");
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCode.setPrefWidth(110);

        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(260);

        TableColumn<Product, String> colUnit = new TableColumn<>("Đơn vị");
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setPrefWidth(90);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, LocalDateTime> colDate = new TableColumn<>("Ngày tạo");
        colDate.setPrefWidth(140);
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setCellFactory(col -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                setText(val.format(DTF));
                setStyle("-fx-text-fill: #888; -fx-alignment: CENTER;");
            }
        });

        TableColumn<Product, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("",
                    new FontIcon(Material2OutlinedAL.EDIT));
            private final Button btnDel  = new Button("",
                    new FontIcon(Material2OutlinedAL.DELETE));
            private final HBox box = new HBox(6, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                btnDel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e ->
                        openDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e ->
                        confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colId, colCode, colName, colUnit, colDate, colAction);

        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);
        BorderPane.setMargin(table, new Insets(8, 0, 0, 0));
        return table;
    }

    private void loadData() {
        masterList.setAll(service.getAll());
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredList.setPredicate(p ->
                kw.isEmpty()
                        || p.getName().toLowerCase().contains(kw)
                        || p.getCode().toLowerCase().contains(kw)
        );
    }

    private void openDialog(Product product) {
        ProductDialog dialog = new ProductDialog(product, service);
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> loadData());
    }

    private void confirmDelete(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa: " + product.getName());
        alert.setContentText("Bạn có chắc muốn xóa sản phẩm này không?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(product.getId());
                loadData();
            }
        });
    }
}