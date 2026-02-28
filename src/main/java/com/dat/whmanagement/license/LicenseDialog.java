package com.dat.whmanagement.license;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Dialog yêu cầu nhập license key.
 * Hiển thị Hardware ID để bạn (dev) copy qua Ultraview rồi tạo key.
 */
public class LicenseDialog {

    /**
     * Hiển thị dialog kích hoạt.
     * @param reason  lý do hiển thị (NO_LICENSE, INVALID, EXPIRED)
     * @return true nếu kích hoạt thành công, false nếu đóng dialog
     */
    public static boolean show(LicenseManager.LicenseStatus reason) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("🔑 Kích hoạt phần mềm");
        dialog.setHeaderText(null);
        dialog.setResizable(false);

        // ── Title ──
        Label title = new Label("Kích hoạt Quản Lý Kho Hàng");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        // ── Banner nổi bật khi hết hạn ──
        javafx.scene.layout.HBox banner = null;
        if (reason == LicenseManager.LicenseStatus.EXPIRED) {
            Label bannerLbl = new Label("⏰  License đã hết hạn — File license cũ đã bị xóa.\n"
                    + "Vui lòng liên hệ nhà cung cấp để nhận key mới rồi nhập vào bên dưới.");
            bannerLbl.setWrapText(true);
            bannerLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            banner = new javafx.scene.layout.HBox(bannerLbl);
            banner.setStyle("-fx-background-color: #C62828; -fx-background-radius: 6; -fx-padding: 10 14 10 14;");
        }

        // ── Thông báo lý do ──
        String msg = switch (reason) {
            case NO_LICENSE -> "Phần mềm chưa được kích hoạt. Vui lòng nhập License Key.";
            case INVALID    -> "License Key không hợp lệ hoặc không đúng máy tính này.";
            case EXPIRED    -> "Nhập License Key mới để tiếp tục sử dụng phần mềm.";
            default         -> "Vui lòng nhập License Key để tiếp tục sử dụng.";
        };
        Label lblMsg = new Label(msg);
        lblMsg.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
        lblMsg.setWrapText(true);

        // ── Hardware ID (cho dev copy) ──
        String hwId = HardwareId.get();
        Label lblHwTitle = new Label("Mã máy (Hardware ID):");
        lblHwTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextField tfHwId = new TextField(hwId);
        tfHwId.setEditable(false);
        tfHwId.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-background-color: #F5F5F5;");
        tfHwId.setPrefWidth(300);

        Button btnCopy = new Button("📋 Copy");
        btnCopy.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        btnCopy.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(hwId);
            Clipboard.getSystemClipboard().setContent(cc);
            btnCopy.setText("✅ Đã copy!");
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> btnCopy.setText("📋 Copy"));
            }).start();
        });

        HBox hwBox = new HBox(8, tfHwId, btnCopy);
        hwBox.setAlignment(Pos.CENTER_LEFT);

        Text note1 = new Text("Gửi mã máy trên cho nhà cung cấp để nhận License Key.\n");
        note1.setStyle("-fx-fill: #666; -fx-font-size: 11px;");
        Text note2 = new Text("⚠ License chỉ dùng được trên máy tính này.");
        note2.setStyle("-fx-fill: #E65100; -fx-font-size: 11px; -fx-font-weight: bold;");
        TextFlow noteFlow = new TextFlow(note1, note2);

        // ── Nhập License Key ──
        Label lblKeyTitle = new Label("License Key:");
        lblKeyTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextField tfKey = new TextField();
        tfKey.setPromptText("XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX");
        tfKey.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        tfKey.setPrefWidth(380);

        // ── Nhập ngày hết hạn ──
        Label lblExpTitle = new Label("Loại license:");
        lblExpTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        ComboBox<String> cbExpiry = new ComboBox<>();
        cbExpiry.getItems().addAll("PERMANENT", "Có thời hạn");
        cbExpiry.setValue("PERMANENT");
        cbExpiry.setPrefWidth(150);

        DatePicker dpExpiry = new DatePicker();
        dpExpiry.setDisable(true);
        dpExpiry.setPrefWidth(150);

        cbExpiry.valueProperty().addListener((obs, old, val) -> {
            dpExpiry.setDisable("PERMANENT".equals(val));
        });

        HBox expiryBox = new HBox(8, cbExpiry, dpExpiry);

        // ── Kết quả ──
        Label lblResult = new Label();
        lblResult.setStyle("-fx-font-size: 12px;");
        lblResult.setVisible(false); lblResult.setManaged(false);

        // ── Layout ──
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);
        if (banner != null) content.getChildren().add(banner);
        content.getChildren().addAll(
                title, lblMsg,
                sep1,
                lblHwTitle, hwBox, noteFlow,
                sep2,
                lblKeyTitle, tfKey,
                lblExpTitle, expiryBox,
                lblResult
        );

        dialog.getDialogPane().setContent(content);

        ButtonType btnActivate = new ButtonType("Kích hoạt", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnExit     = new ButtonType("Thoát", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnActivate, btnExit);

        Button activateBtn = (Button) dialog.getDialogPane().lookupButton(btnActivate);
        activateBtn.getStyleClass().add(Styles.ACCENT);

        // Xử lý kích hoạt
        activateBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String key = tfKey.getText().trim();
            if (key.isEmpty()) {
                lblResult.setText("⚠ Vui lòng nhập License Key!");
                lblResult.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
                lblResult.setVisible(true); lblResult.setManaged(true);
                e.consume();
                return;
            }

            String expiry;
            if ("PERMANENT".equals(cbExpiry.getValue())) {
                expiry = "PERMANENT";
            } else {
                if (dpExpiry.getValue() == null) {
                    lblResult.setText("⚠ Vui lòng chọn ngày hết hạn!");
                    lblResult.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
                    lblResult.setVisible(true); lblResult.setManaged(true);
                    e.consume();
                    return;
                }
                expiry = dpExpiry.getValue().toString(); // yyyy-MM-dd
            }

            if (LicenseManager.validateInput(key, expiry)) {
                LicenseManager.saveLicense(key, expiry);
                lblResult.setText("✅ Kích hoạt thành công!");
                lblResult.setStyle("-fx-text-fill: #388E3C; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblResult.setVisible(true); lblResult.setManaged(true);
                // Cho phép đóng
            } else {
                lblResult.setText("❌ License Key không hợp lệ! Kiểm tra lại key và loại license.");
                lblResult.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
                lblResult.setVisible(true); lblResult.setManaged(true);
                e.consume(); // Không cho đóng
            }
        });

        dialog.setResultConverter(btn -> btn == btnActivate);

        return dialog.showAndWait().orElse(false);
    }
}

