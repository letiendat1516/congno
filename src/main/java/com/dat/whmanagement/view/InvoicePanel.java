package com.dat.whmanagement.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Panel chứa tab Tạo hóa đơn và Danh sách hóa đơn.
 */
public class InvoicePanel extends BorderPane {

    private final TabPane tabPane = new TabPane();

    public InvoicePanel() {
        InvoiceFormPane formPane = new InvoiceFormPane();
        InvoiceListPane listPane = new InvoiceListPane();

        // Khi sửa từ danh sách → chuyển sang tab form và load dữ liệu
        listPane.setOnEditInvoice((invoice, items) -> {
            formPane.loadInvoice(invoice, items);
            tabPane.getSelectionModel().select(0); // Chuyển sang tab form
        });

        // Khi in lại → mở preview
        listPane.setOnReprintInvoice((invoice, items) -> {
            formPane.showPreview(invoice, items);
        });

        Tab tabForm = new Tab("Tạo hóa đơn", formPane.getRoot());
        tabForm.setClosable(false);

        Tab tabList = new Tab("Danh sách hóa đơn", listPane.getRoot());
        tabList.setClosable(false);

        // Refresh list khi chuyển sang tab danh sách
        tabList.setOnSelectionChanged(e -> {
            if (tabList.isSelected()) listPane.refresh();
        });

        tabPane.getTabs().addAll(tabForm, tabList);
        setCenter(tabPane);
    }
}
