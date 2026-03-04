package com.dat.whmanagement.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

/**
 * Utility: biến ComboBox thường thành searchable ComboBox.
 * Cho phép gõ mã hoặc tên để lọc danh sách.
 */
public class ComboBoxHelper {

    /**
     * An toàn lấy giá trị từ editable ComboBox.
     * Trả null nếu value là String (do JavaFX bug với editable ComboBox).
     */
    @SuppressWarnings("unchecked")
    public static <T> T safeGetValue(ComboBox<T> cb) {
        Object raw = cb.getValue();
        if (raw == null) return null;
        if (raw instanceof String) {
            // JavaFX truyền String thay vì T → dùng converter để tìm
            try {
                return cb.getConverter().fromString((String) raw);
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return (T) raw;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Làm cho ComboBox có thể gõ để tìm kiếm (chỉ dùng display để filter).
     */
    public static <T> void makeSearchable(ComboBox<T> cb, List<T> allItems, Function<T, String> display) {
        makeSearchable(cb, allItems, display, display);
    }

    /**
     * Làm cho ComboBox có thể gõ để tìm kiếm.
     * @param cb          ComboBox cần xử lý
     * @param allItems    Danh sách gốc đầy đủ
     * @param display     Hàm hiển thị text cho mỗi item
     * @param searchText  Hàm trả về text dùng để filter (có thể chứa nhiều trường hơn display)
     */
    public static <T> void makeSearchable(ComboBox<T> cb, List<T> allItems,
                                           Function<T, String> display,
                                           Function<T, String> searchText) {
        cb.setItems(FXCollections.observableArrayList(allItems));
        cb.setEditable(true);

        // StringConverter ngăn JavaFX cast String thành T
        cb.setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : display.apply(item);
            }

            @Override
            public T fromString(String text) {
                if (text == null || text.isBlank()) return null;
                String kw = text.trim().toLowerCase();
                // Tìm item khớp chính xác với text hiển thị hoặc text tìm kiếm
                for (T item : allItems) {
                    String d = display.apply(item).toLowerCase();
                    String s = searchText.apply(item).toLowerCase();
                    if (d.equalsIgnoreCase(kw) || d.contains(kw)
                            || s.contains(kw)) {
                        return item;
                    }
                }
                return null;
            }
        });

        // Custom cell hiển thị đúng text
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : display.apply(item));
            }
        });

        // Flag chống loop khi đang cập nhật từ code
        final boolean[] updating = {false};

        // Filter khi gõ — tìm theo cả display và searchText
        cb.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating[0]) return;

            // Nếu đang chọn item từ list → không filter
            T current = null;
            try { current = cb.getValue(); } catch (ClassCastException ignored) {}
            if (current != null && display.apply(current).equals(newVal)) return;

            String kw = newVal == null ? "" : newVal.trim().toLowerCase();
            ObservableList<T> filtered = FXCollections.observableArrayList();
            for (T item : allItems) {
                if (kw.isEmpty()
                        || display.apply(item).toLowerCase().contains(kw)
                        || searchText.apply(item).toLowerCase().contains(kw)) {
                    filtered.add(item);
                }
            }

            updating[0] = true;
            cb.setItems(filtered);
            updating[0] = false;

            if (!filtered.isEmpty() && !kw.isEmpty()) {
                cb.show();
            }
        });

        // Khi chọn 1 item từ dropdown → hiển thị text đúng
        cb.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updating[0]) return;
            if (newVal == null) return;
            // Guard: JavaFX đôi khi truyền String thay vì T khi editable
            if (newVal instanceof String) return;
            try {
                String text = display.apply(newVal);
                updating[0] = true;
                cb.getEditor().setText(text);
                updating[0] = false;
            } catch (ClassCastException ignored) {
                // newVal là String chứ không phải T → bỏ qua
            }
        });
    }
}
