package com.dat.whmanagement.view;

import atlantafx.base.theme.Styles;
import com.dat.whmanagement.model.Product;
import com.dat.whmanagement.service.ProductService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProductDialog extends Dialog<Product> {

    private final TextField tfCode   = new TextField();
    private final TextField tfName   = new TextField();
    private final TextField tfUnit   = new TextField();
    private final Label     lblError = new Label();

    private final Product existing;
    private final ProductService service;

    public ProductDialog(Product product, ProductService service) {
        this.existing = product;
        this.service  = service;

        setTitle(product == null ? "Thêm sản phẩm mới" : "Chỉnh sửa sản phẩm");
        setResizable(false);
        getDialogPane().setContent(buildContent());
        getDialogPane().setPrefWidth(400);

        ButtonType btnSave   = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        // Style nút Lưu
        Button saveBtn = (Button) getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add(Styles.ACCENT);

        // Validate trước khi đóng
        saveBtn.addEventFilter(ActionEvent.ACTION, e -> {
            if (!validate()) e.consume();
        });

        // Fill data nếu đang edit
        if (product != null) fillForm(product);

        // Trả về Product sau khi nhấn Lưu
        setResultConverter(btn -> {
            if (btn == btnSave) return saveProduct();
            return null;
        });
    }

    // ─────────────────────────────────────────
    // FORM
    // ─────────────────────────────────────────
    private VBox buildContent() {
        Label title = new Label(existing == null ? "Thông tin sản phẩm" : "Cập nhật sản phẩm");
        title.setFont(Font.font("System", FontWeight.BOLD, 15));

        tfCode.setPromptText("VD: SP001");
        tfName.setPromptText("VD: Gạo ST25");
        tfUnit.setPromptText("VD: kg, cái, thùng...");

        // Disable mã SP khi edit (tránh trùng)
        if (existing != null) {
            tfCode.setDisable(true);
            tfCode.setStyle("-fx-opacity: 0.6;");
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 0, 8, 0));

        ColumnConstraints col1 = new ColumnConstraints(90);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        addRow(grid, 0, "Mã SP *",  tfCode);
        addRow(grid, 1, "Tên SP *", tfName);
        addRow(grid, 2, "Đơn vị",   tfUnit);

        lblError.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        VBox content = new VBox(8, title, new Separator(), grid, lblError);
        content.setPadding(new Insets(20));
        return content;
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        field.setMaxWidth(Double.MAX_VALUE);
        field.getStyleClass().add(Styles.ROUNDED);
        grid.add(lbl,   0, row);
        grid.add(field, 1, row);
    }

    // ─────────────────────────────────────────
    // VALIDATE + SAVE
    // ─────────────────────────────────────────
    private boolean validate() {
        hideError();

        if (tfCode.getText().trim().isEmpty()) {
            showError("Mã sản phẩm không được để trống!");
            tfCode.requestFocus();
            return false;
        }
        if (tfName.getText().trim().isEmpty()) {
            showError("Tên sản phẩm không được để trống!");
            tfName.requestFocus();
            return false;
        }
        // Kiểm tra trùng mã khi thêm mới
        if (existing == null && service.existsByCode(tfCode.getText().trim())) {
            showError("Mã sản phẩm \"" + tfCode.getText().trim() + "\" đã tồn tại!");
            tfCode.requestFocus();
            return false;
        }
        return true;
    }

    private Product saveProduct() {
        if (existing != null) {
            // UPDATE
            existing.setName(tfName.getText().trim());
            existing.setUnit(tfUnit.getText().trim());
            service.update(existing);
            return existing;
        } else {
            // CREATE
            Product p = new Product(
                    tfCode.getText().trim(),
                    tfName.getText().trim(),
                    tfUnit.getText().trim()
            );
            return service.create(p);
        }
    }

    private void fillForm(Product p) {
        tfCode.setText(p.getCode());
        tfName.setText(p.getName());
        tfUnit.setText(p.getUnit() != null ? p.getUnit() : "");
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private void showError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}