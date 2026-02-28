package com.dat.whmanagement.view;

import atlantafx.base.theme.CupertinoLight;
import com.dat.whmanagement.license.LicenseDialog;
import com.dat.whmanagement.license.LicenseManager;
import com.dat.whmanagement.migration.MigrationRunner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        // ── Kiểm tra License ──
        LicenseManager.LicenseStatus status = LicenseManager.verify();

        if (status == LicenseManager.LicenseStatus.EXPIRED) {
            // Xóa file license cũ → buộc nhập key mới
            LicenseManager.deleteLicense();
            status = LicenseManager.LicenseStatus.EXPIRED; // giữ lý do để dialog hiện đúng thông báo
        }

        if (status != LicenseManager.LicenseStatus.VALID) {
            boolean activated = LicenseDialog.show(status);
            if (!activated || LicenseManager.verify() != LicenseManager.LicenseStatus.VALID) {
                Platform.exit();
                return;
            }
        }

        // Chạy migration tạo bảng (nếu chưa tồn tại)
        MigrationRunner.runMigration();


        MainController root = new MainController();

        Scene scene = new Scene(root, 1280, 780);
        stage.setTitle("📦 Quản Lý Kho Hàng");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}