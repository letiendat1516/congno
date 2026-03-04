package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.model.StockItem;
import com.dat.whmanagement.service.StockService;
import com.dat.whmanagement.service.impl.StockServiceImpl;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StockPanel extends BorderPane {

    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final StockService service = new StockServiceImpl();
    private final ObservableList<StockItem> masterList = FXCollections.observableArrayList();
    private FilteredList<StockItem> filteredList;

    // Summary
    private Label lblTotalItems;
    private Label lblTotalStock;
    private Label lblTotalValue;

    public StockPanel() {
        setPadding(new Insets(24));
        setTop(buildHeader());
        setCenter(buildTable());
        loadData();
    }

    private VBox buildHeader() {
        Label title = new Label("Tồn kho");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label subtitle = new Label("Theo dõi số lượng tồn kho theo sản phẩm");
        subtitle.setStyle("-fx-text-fill: #888;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm theo tên, mã SP...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add(Styles.ROUNDED);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        btnRefresh.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(10, searchField, spacer, btnRefresh);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(16, 0, 8, 0));

        // Summary cards
        lblTotalItems = new Label("0");
        lblTotalStock = new Label("0");
        lblTotalValue = new Label("0 ₫");

        HBox summaryBar = new HBox(16,
                buildCard("Số mặt hàng", lblTotalItems, "#1976D2"),
                buildCard("Tổng SL tồn", lblTotalStock, "#F57C00"),
                buildCard("Tổng giá trị", lblTotalValue, "#388E3C"));
        summaryBar.setPadding(new Insets(0, 0, 10, 0));

        return new VBox(4, new VBox(2, title, subtitle), actionBar, summaryBar);
    }

    private VBox buildCard(String label, Label value, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        value.setFont(Font.font("System", FontWeight.BOLD, 16));
        value.setStyle("-fx-text-fill: " + color + ";");
        VBox card = new VBox(2, lbl, value);
        card.setPadding(new Insets(12, 20, 12, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        card.setPrefWidth(200);
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView<StockItem> buildTable() {
        TableView<StockItem> table = new TableView<>();
        table.getStyleClass().add(Styles.STRIPED);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Chưa có sản phẩm nào"));

        TableColumn<StockItem, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colId.setPrefWidth(50); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<StockItem, String> colCode = new TableColumn<>("Mã SP");
        colCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colCode.setPrefWidth(100);

        TableColumn<StockItem, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colName.setPrefWidth(220);

        TableColumn<StockItem, String> colUnit = new TableColumn<>("Đơn vị");
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setPrefWidth(80); colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<StockItem, Double> colStock = new TableColumn<>("Tồn kho");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(100);
        colStock.setCellFactory(col -> new TableCell<StockItem, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.1f", val));
                String color = val <= 0 ? "#E53935" : val < 10 ? "#F57C00" : "#333";
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });

        TableColumn<StockItem, Double> colBuy = new TableColumn<>("Giá nhập");
        colBuy.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        colBuy.setPrefWidth(110);
        colBuy.setCellFactory(col -> currencyCell());


        TableColumn<StockItem, Double> colValue = new TableColumn<>("Giá trị tồn");
        colValue.setCellValueFactory(new PropertyValueFactory<>("stockValue"));
        colValue.setPrefWidth(130);
        colValue.setCellFactory(col -> new TableCell<StockItem, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) setText(null);
                else {
                    setText(CURRENCY.format(val) + " ₫");
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #388E3C;");
                }
            }
        });

        table.getColumns().addAll(colId, colCode, colName, colUnit, colStock, colBuy, colValue);
        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);
        BorderPane.setMargin(table, new Insets(8, 0, 0, 0));
        return table;
    }

    private TableCell<StockItem, Double> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) setText(null);
                else {
                    setText(CURRENCY.format(val) + " ₫");
                    setStyle("-fx-alignment: CENTER_RIGHT;");
                }
            }
        };
    }

    private void loadData() {
        List<StockItem> data = service.getAll();
        masterList.setAll(data);

        lblTotalItems.setText(String.valueOf(data.size()));
        lblTotalStock.setText(String.format("%.1f", data.stream().mapToDouble(StockItem::getStock).sum()));
        lblTotalValue.setText(CURRENCY.format(data.stream().mapToDouble(StockItem::getStockValue).sum()) + " ₫");
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredList.setPredicate(s ->
                kw.isEmpty()
                        || s.getProductName().toLowerCase().contains(kw)
                        || (s.getProductCode() != null && s.getProductCode().toLowerCase().contains(kw))
        );
    }
}

