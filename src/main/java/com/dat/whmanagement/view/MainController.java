package com.dat.whmanagement.view;

import com.dat.whmanagement.license.LicenseManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MainController extends BorderPane {

    private final BorderPane contentArea = new BorderPane();

    public MainController() {
        setLeft(buildSidebar());
        setCenter(contentArea);
        navigate("products");
    }

    // ─────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1e1e2e;");

        Label logo = new Label("📦 Kho Hàng");
        logo.setFont(Font.font("System", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);
        logo.setPadding(new Insets(24, 20, 24, 20));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #444;");

        VBox menu = new VBox(4);
        menu.setPadding(new Insets(12, 8, 12, 8));
        menu.getChildren().addAll(
                buildMenuItem("Sản phẩm",     new FontIcon(Material2OutlinedAL.CATEGORY),          "products"),
                buildMenuItem("Nhà cung cấp", new FontIcon(Material2OutlinedAL.BUSINESS),           "suppliers"),
                buildMenuItem("Khách hàng",   new FontIcon(Material2OutlinedAL.CONTACTS),           "customers"),
                buildMenuItem("Nhập hàng",    new FontIcon(Material2OutlinedAL.ARROW_CIRCLE_DOWN),  "purchase"),
                buildMenuItem("Xuất hàng",    new FontIcon(Material2OutlinedAL.ARROW_CIRCLE_UP),    "sales"),
                buildMenuItem("Đơn đặt hàng", new FontIcon(Material2OutlinedAL.ASSIGNMENT),          "pending"),
                buildMenuItem("Trả hàng",     new FontIcon(Material2OutlinedAL.ASSIGNMENT_RETURN),   "returns"),
                buildMenuItem("Tồn kho",      new FontIcon(Material2OutlinedMZ.STORAGE),            "stock"),
                buildMenuItem("Thanh toán",   new FontIcon(Material2OutlinedMZ.PAYMENT),            "payments"),
                buildMenuItem("Hóa đơn",     new FontIcon(Material2OutlinedAL.ARTICLE),             "invoices")
        );

        VBox.setVgrow(menu, Priority.ALWAYS);

        // ── License info ──
        VBox licenseBox = buildLicenseInfo();

        sidebar.getChildren().addAll(logo, sep, menu, licenseBox);
        return sidebar;
    }

    private VBox buildLicenseInfo() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #444;");

        Label lblIcon = new Label("🔑");
        lblIcon.setStyle("-fx-font-size: 12px;");

        Label lblStatus = new Label();
        Label lblExpiry = new Label();
        lblStatus.setStyle("-fx-font-size: 11px;");
        lblExpiry.setStyle("-fx-font-size: 10px;");

        try {
            java.nio.file.Path path = Paths.get("license.key");
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                String[] lines = content.split("\\R");
                if (lines.length >= 2) {
                    String expiry = lines[1].trim();
                    if ("PERMANENT".equalsIgnoreCase(expiry)) {
                        lblStatus.setText("✅ License: Vĩnh viễn");
                        lblStatus.setTextFill(Color.web("#81C784"));
                        lblExpiry.setText("Không giới hạn thời gian");
                        lblExpiry.setTextFill(Color.web("#A5D6A7"));
                    } else {
                        LocalDate expiryDate = LocalDate.parse(expiry, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
                        if (daysLeft > 30) {
                            lblStatus.setText("✅ License: Còn hạn");
                            lblStatus.setTextFill(Color.web("#81C784"));
                            lblExpiry.setText("Hết hạn: " + expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    + " (còn " + daysLeft + " ngày)");
                            lblExpiry.setTextFill(Color.web("#A5D6A7"));
                        } else if (daysLeft > 0) {
                            lblStatus.setText("⚠ License: Sắp hết hạn");
                            lblStatus.setTextFill(Color.web("#FFB74D"));
                            lblExpiry.setText("Hết hạn: " + expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    + " (còn " + daysLeft + " ngày)");
                            lblExpiry.setTextFill(Color.web("#FFCC80"));
                        } else {
                            lblStatus.setText("❌ License: Đã hết hạn");
                            lblStatus.setTextFill(Color.web("#EF5350"));
                            lblExpiry.setText("Hết hạn: " + expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            lblExpiry.setTextFill(Color.web("#EF9A9A"));
                        }
                    }
                }
            } else {
                lblStatus.setText("⚠ Chưa kích hoạt");
                lblStatus.setTextFill(Color.web("#FFB74D"));
                lblExpiry.setText("");
            }
        } catch (Exception e) {
            lblStatus.setText("⚠ Lỗi đọc license");
            lblStatus.setTextFill(Color.web("#FFB74D"));
            lblExpiry.setText("");
        }

        VBox box = new VBox(2, sep, lblStatus, lblExpiry);
        box.setPadding(new Insets(8, 12, 12, 12));
        return box;
    }

    private Button buildMenuItem(String text, FontIcon icon, String target) {
        icon.setIconSize(18);
        icon.setIconColor(Color.web("#aaaacc"));

        String styleNormal = """
            -fx-background-color: transparent;
            -fx-text-fill: #ccccee;
            -fx-font-size: 13px;
            -fx-alignment: CENTER_LEFT;
            -fx-padding: 10 16 10 16;
            -fx-cursor: hand;
            -fx-background-radius: 8;
            """;
        String styleHover = """
            -fx-background-color: #2e2e4e;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-alignment: CENTER_LEFT;
            -fx-padding: 10 16 10 16;
            -fx-cursor: hand;
            -fx-background-radius: 8;
            """;

        Button btn = new Button(text, icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e  -> btn.setStyle(styleNormal));
        btn.setOnAction(e -> navigate(target));
        return btn;
    }

    // ─────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────
    private void navigate(String target) {
        switch (target) {
            case "products"  -> contentArea.setCenter(new ProductPanel());
            case "customers" -> contentArea.setCenter(new CustomerPanel());
            case "suppliers" -> contentArea.setCenter(new SupplierPanel());
            case "purchase"  -> contentArea.setCenter(new PurchasePanel());
            case "sales"     -> contentArea.setCenter(new SalesPanel());
            case "pending"   -> contentArea.setCenter(new PendingOrderPanel());
            case "returns"   -> contentArea.setCenter(new ReturnOrderPanel());
            case "stock"     -> contentArea.setCenter(new StockPanel());
            case "payments"  -> contentArea.setCenter(new DebtPanel());
            case "invoices"  -> contentArea.setCenter(new InvoicePanel());
            default          -> contentArea.setCenter(buildPlaceholder(target));
        }
    }

    private Label buildPlaceholder(String name) {
        Label lbl = new Label("🚧 " + name + " — Coming soon");
        lbl.setFont(Font.font(16));
        lbl.setStyle("-fx-text-fill: #999;");
        return lbl;
    }
}