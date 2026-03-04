package com.dat.whmanagement.view;

import com.dat.whmanagement.model.Invoice;
import com.dat.whmanagement.model.InvoiceItem;
import com.dat.whmanagement.util.CurrencyUtil;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Xem trước và in hóa đơn bán hàng A4.
 */
public class InvoicePreviewStage extends Stage {

    private static final double PAGE_W = 595;
    private static final double PAGE_H = 842;
    private static final double MG = 32;
    private static final double CW = PAGE_W - 2 * MG;
    private static final String F = "Times New Roman";

    private static final String BG_HDR   = "-fx-background-color: #e8e8e8;";
    private static final String BG_ALT   = "-fx-background-color: #f7f7f7;";
    private static final String BG_WHITE = "-fx-background-color: white;";
    private static final String BORDER_T = "-fx-border-color: #000; -fx-border-width: 0.8;";

    private final Invoice invoice;
    private final List<InvoiceItem> items;
    private VBox page;

    public InvoicePreviewStage(Invoice invoice, List<InvoiceItem> items) {
        this.invoice = invoice;
        this.items = items;
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Xem trước hóa đơn");

        page = buildInvoiceLayout();
        page.setPrefWidth(CW);
        page.setMaxWidth(CW);
        page.setStyle(BG_WHITE);
        page.setPadding(new Insets(MG));

        StackPane paper = new StackPane(page);
        paper.setPrefSize(PAGE_W, PAGE_H);
        paper.setMaxSize(PAGE_W, PAGE_H);
        paper.setMinSize(PAGE_W, PAGE_H);
        paper.setStyle(BG_WHITE
                + "-fx-border-color: #bbb; -fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 2, 2);");

        Button btnPrint = new Button("🖨 In ngay");
        btnPrint.setOnAction(e -> doPrint());
        Button btnClose = new Button("Đóng");
        btnClose.setOnAction(e -> close());
        HBox bar = new HBox(12, btnPrint, btnClose);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(8));

        StackPane scroll = new StackPane(paper);
        scroll.setPadding(new Insets(12));
        scroll.setStyle("-fx-background-color: #ddd;");
        ScrollPane sp = new ScrollPane(scroll);
        sp.setFitToWidth(true);

        VBox root = new VBox(sp, bar);
        VBox.setVgrow(sp, Priority.ALWAYS);
        Scene scene = new Scene(root, PAGE_W + 50, Math.min(PAGE_H + 70, 830));
        setScene(scene);
    }

    private VBox buildInvoiceLayout() {
        VBox layout = new VBox(0);
        layout.getChildren().addAll(
                buildHeader(),
                buildTitle(),
                buildCustomerInfo(),
                buildItemsTable(),
                buildTotalBlock(),
                buildAmountInWords(),
                buildSignatures()
        );
        return layout;
    }

    private Node buildHeader() {
        VBox box = new VBox(2);
        box.setPadding(new Insets(0, 0, 6, 0));

        Text line1 = txt(s(invoice.getSellerName()), FontWeight.BOLD, 11);
        Text c1 = txt("ĐC: " + s(invoice.getSellerAddress()), FontWeight.NORMAL, 8);
        Text c2 = txt("ĐT: " + s(invoice.getSellerPhone()), FontWeight.NORMAL, 8);
        Text c3 = txt("STK: " + s(invoice.getSellerBankAccount()), FontWeight.NORMAL, 8);
        c1.setFill(Color.rgb(80, 80, 80));
        c2.setFill(Color.rgb(80, 80, 80));
        c3.setFill(Color.rgb(80, 80, 80));

        box.getChildren().addAll(line1, c1, c2, c3);

        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: #999;");

        VBox wrapper = new VBox(0, box, line);
        wrapper.setPadding(new Insets(0, 0, 2, 0));
        return wrapper;
    }

    private Node buildTitle() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 0, 6, 0));

        Text title = txt("HÓA ĐƠN BÁN HÀNG", FontWeight.BOLD, 16);

        String info = "";
        if (invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().isBlank()) {
            info += "Số: " + invoice.getInvoiceNumber();
        }
        if (invoice.getIssueDate() != null) {
            if (!info.isEmpty()) info += "        ";
            info += "Ngày " + p2(invoice.getIssueDate().getDayOfMonth())
                    + " tháng " + p2(invoice.getIssueDate().getMonthValue())
                    + " năm " + invoice.getIssueDate().getYear();
        }
        Text sub = txt(info, FontWeight.NORMAL, 9);

        box.getChildren().addAll(title, sub);
        return box;
    }

    private Node buildCustomerInfo() {
        GridPane g = new GridPane();
        g.setHgap(6);
        g.setVgap(3);
        g.setPadding(new Insets(2, 0, 8, 0));

        ColumnConstraints col1 = new ColumnConstraints(CW * 0.22);
        ColumnConstraints col2 = new ColumnConstraints(CW * 0.78);
        col2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(col1, col2);

        int r = 0;
        addLabelValue(g, r++, "Tên khách hàng:", s(invoice.getBuyerName()));
        addLabelValue(g, r++, "Đơn vị:", s(invoice.getBuyerCompany()));
        addLabelValue(g, r++, "Địa chỉ:", s(invoice.getBuyerAddress()));
        addLabelValue(g, r, "Hình thức TT:", s(invoice.getPaymentMethod()));

        return g;
    }

    private void addLabelValue(GridPane g, int row, String label, String value) {
        Text l = txt(label, FontWeight.BOLD, 9);
        Text v = txt(value, FontWeight.NORMAL, 9);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    private Node buildItemsTable() {
        double[] pct = {0.06, 0.36, 0.09, 0.09, 0.18, 0.22};
        String[] hdr = {"TT", "Tên hàng hóa, dịch vụ", "ĐVT", "SL", "Đơn giá", "Thành tiền"};

        GridPane tbl = new GridPane();
        for (double p : pct) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(CW * p);
            cc.setMinWidth(CW * p);
            tbl.getColumnConstraints().add(cc);
        }

        // Header
        for (int i = 0; i < hdr.length; i++) {
            StackPane cell = cell(hdr[i], FontWeight.BOLD, 9, Pos.CENTER);
            cell.setStyle(BG_HDR + BORDER_T);
            cell.setPadding(new Insets(5, 4, 5, 4));
            tbl.add(cell, i, 0);
        }

        // Data
        for (int idx = 0; idx < items.size(); idx++) {
            InvoiceItem it = items.get(idx);
            int row = idx + 1;
            String bg = (idx % 2 == 1) ? BG_ALT : BG_WHITE;

            String[] vals = {
                    String.valueOf(idx + 1),
                    s(it.getName()),
                    s(it.getUnit()),
                    String.valueOf(it.getQuantity()),
                    CurrencyUtil.formatPlain(it.getUnitPrice()),
                    CurrencyUtil.formatPlain(it.getTotalPrice())
            };

            for (int i = 0; i < vals.length; i++) {
                Pos align = (i == 4 || i == 5) ? Pos.CENTER_RIGHT
                        : (i == 1) ? Pos.CENTER_LEFT : Pos.CENTER;
                StackPane c = cell(vals[i], FontWeight.NORMAL, 9, align);
                c.setStyle(bg + "-fx-border-color: #000; -fx-border-width: 0 0.6 0.6 0.6;");
                c.setPadding(new Insets(4, 5, 4, 5));
                tbl.add(c, i, row);
            }
        }

        return tbl;
    }

    private StackPane cell(String text, FontWeight w, double size, Pos align) {
        Text t = txt(text, w, size);
        StackPane sp = new StackPane(t);
        sp.setAlignment(align);
        return sp;
    }

    private Node buildTotalBlock() {
        VBox box = new VBox(3);
        box.setPadding(new Insets(6, 0, 0, 0));

        double gridW  = CW * 0.55;
        double labelW = gridW * 0.65;
        double valueW = gridW * 0.35;

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(0, 0, 0, CW - gridW));
        ColumnConstraints cLabel = new ColumnConstraints(labelW);
        ColumnConstraints cValue = new ColumnConstraints(valueW);
        cValue.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(cLabel, cValue);
        grid.setVgap(3);

        String vatRateStr = (int) invoice.getVatRate() + "%";

        int r = 0;
        addTotalRow(grid, r++, "Tiền hàng:", CurrencyUtil.formatPlain(invoice.getSubtotal()), FontWeight.NORMAL, 9.5);
        addTotalRow(grid, r++, "Thuế suất: " + vatRateStr, "", FontWeight.NORMAL, 9.5);
        addTotalRow(grid, r++, "Tiền thuế:", CurrencyUtil.formatPlain(invoice.getVatAmount()), FontWeight.NORMAL, 9.5);
        addTotalRow(grid, r, "Tổng cộng thanh toán:", CurrencyUtil.formatPlain(invoice.getTotalAmount()), FontWeight.BOLD, 11);

        box.getChildren().add(grid);
        return box;
    }

    private void addTotalRow(GridPane grid, int row, String label, String value, FontWeight weight, double size) {
        Text lbl = txt(label, weight, size);
        grid.add(lbl, 0, row);
        if (!value.isEmpty()) {
            Text val = txt(value, weight, size);
            grid.add(val, 1, row);
        }
    }

    private Node buildAmountInWords() {
        HBox box = new HBox(4);
        box.setPadding(new Insets(8, 0, 0, 0));
        box.setAlignment(Pos.CENTER_LEFT);

        Text label = txt("Số tiền viết bằng chữ: ", FontWeight.BOLD, 9.5);
        Text value = new Text(s(invoice.getAmountInWords()));
        value.setFont(Font.font(F, FontPosture.ITALIC, 9.5));

        box.getChildren().addAll(label, value);
        return box;
    }

    private Node buildSignatures() {
        GridPane g = new GridPane();
        g.setPadding(new Insets(30, 0, 0, 0));

        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        half.setHalignment(HPos.CENTER);
        ColumnConstraints half2 = new ColumnConstraints();
        half2.setPercentWidth(50);
        half2.setHalignment(HPos.CENTER);
        g.getColumnConstraints().addAll(half, half2);

        VBox left  = sigCol("Khách hàng", "(Ký, ghi rõ họ tên)");
        VBox right = sigCol("Người bán hàng", "(Ký, ghi rõ họ tên)");

        g.add(left, 0, 0);
        g.add(right, 1, 0);
        return g;
    }

    private VBox sigCol(String title, String sub) {
        Text t = txt(title, FontWeight.BOLD, 10);
        Text s = txt(sub, FontWeight.NORMAL, 8);
        s.setFill(Color.rgb(100, 100, 100));

        Region space = new Region();
        space.setPrefHeight(50);

        VBox col = new VBox(3, t, s, space);
        col.setAlignment(Pos.TOP_CENTER);
        return col;
    }

    private void doPrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;
        if (!job.showPrintDialog(this)) return;

        Printer printer = job.getPrinter();
        PageLayout layout = printer.createPageLayout(
                Paper.A4, PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM);
        job.getJobSettings().setPageLayout(layout);

        if (job.printPage(layout, page)) {
            job.endJob();
            close();
        }
    }

    private Text txt(String content, FontWeight weight, double size) {
        Text t = new Text(content);
        t.setFont(Font.font(F, weight, size));
        return t;
    }

    private String s(String v) { return v != null ? v : ""; }

    private String p2(int v) { return v < 10 ? "0" + v : String.valueOf(v); }
}

