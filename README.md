# TÀI LIỆU ĐẶC TẢ: SỬ DỤNG AI TRONG PHÁT TRIỂN PHẦN MỀM

> **Dự án:** Quản Lý Kho (whmanagement)  
> **Ngày tạo:** 04/03/2026  
> **Phiên bản:** 1.0  
> **Tác giả:** Lê Tiến Đạt

---

## MỤC LỤC

1. [Giới thiệu](#1-giới-thiệu)
2. [Phân loại AI được sử dụng](#2-phân-loại-ai-được-sử-dụng)
3. [Giai đoạn 1: Implement Code (Lập trình)](#3-giai-đoạn-1-implement-code-lập-trình)
4. [Giai đoạn 2: Testing sản phẩm (Kiểm thử)](#4-giai-đoạn-2-testing-sản-phẩm-kiểm-thử)
5. [Quy trình làm việc với AI](#5-quy-trình-làm-việc-với-ai)
6. [Đánh giá hiệu quả](#6-đánh-giá-hiệu-quả)
7. [Rủi ro và biện pháp kiểm soát](#7-rủi-ro-và-biện-pháp-kiểm-soát)
8. [Kết luận](#8-kết-luận)

---

## 1. GIỚI THIỆU

### 1.1 Mục đích tài liệu

Tài liệu này mô tả cách AI được sử dụng trong quá trình phát triển phần mềm **Quản Lý Kho (whmanagement)** — một ứng dụng desktop JavaFX quản lý nhập/xuất kho, khách hàng, nhà cung cấp, thanh toán, công nợ và hóa đơn. Tài liệu tập trung vào hai giai đoạn chính:

- **Implement Code** (Lập trình)
- **Testing sản phẩm** (Kiểm thử)

### 1.2 Phạm vi áp dụng

| Hạng mục          | Chi tiết                                      |
|--------------------|-----------------------------------------------|
| Ngôn ngữ           | Java 17                                       |
| Framework UI       | JavaFX 21, AtlantaFX                          |
| Database           | SQLite (qua sqlite-jdbc)                      |
| Build tool         | Maven                                         |
| Kiến trúc          | DAO – Service – View (MVC)                    |
| Đóng gói           | jpackage → .exe (Windows)                     |

### 1.3 Đối tượng đọc

- Lập trình viên (Developer)
- Kiểm thử viên (Tester / QA)
- Quản lý dự án (Project Manager)
- Giảng viên / Sinh viên (mục đích học thuật)

---

## 2. PHÂN LOẠI AI ĐƯỢC SỬ DỤNG

### 2.1 Chatbot AI

| Công cụ       | Vai trò chính                              | Giai đoạn sử dụng         |
|----------------|---------------------------------------------|----------------------------|
| **ChatGPT**    | Hỏi đáp, giải thích logic, viết tài liệu  | Thiết kế, Implement, Test  |
| **Claude**     | Phân tích code, refactor, debug lỗi phức tạp| Implement, Test, Review    |

**Cách dùng điển hình:**
- Đặt câu hỏi bằng ngôn ngữ tự nhiên (tiếng Việt/Anh)
- Dán đoạn code + mô tả lỗi → nhận hướng dẫn sửa
- Yêu cầu giải thích thuật toán / design pattern

### 2.2 Tool AI hỗ trợ coding

| Công cụ                | Vai trò chính                                    |
|--------------------------|--------------------------------------------------|
| **GitHub Copilot**       | Gợi ý code real-time trong IDE (IntelliJ IDEA)   |
| **Copilot Chat (IDE)**   | Hỏi đáp ngay trong editor, không cần chuyển tab  |

**Cách dùng điển hình:**
- Viết comment mô tả → AI tự sinh code bên dưới
- Chọn đoạn code → yêu cầu refactor / explain / fix
- Tự động hoàn thành phương thức dựa trên ngữ cảnh lớp

### 2.3 Agent AI

| Công cụ                     | Vai trò chính                                         |
|-------------------------------|-------------------------------------------------------|
| **GitHub Copilot Agent Mode** | Tự động đọc, phân tích, sửa nhiều file cùng lúc       |
| **Copilot Workspace**         | Lên kế hoạch, tạo plan, thực thi thay đổi đa file     |

**Cách dùng điển hình:**
- Mô tả yêu cầu tổng quát → Agent tự tìm file liên quan, đề xuất & áp dụng thay đổi
- Tự chạy lệnh build/test, kiểm tra lỗi, sửa lại cho đến khi pass
- Thực hiện refactor quy mô lớn trải dài nhiều lớp (Model, DAO, Service, View)

---

## 3. GIAI ĐOẠN 1: IMPLEMENT CODE (LẬP TRÌNH)

### 3.1 Tổng quan quy trình

```
┌─────────────┐     ┌──────────────┐     ┌───────────────┐     ┌──────────────┐
│  Mô tả yêu  │────▶│  AI phân tích│────▶│  AI sinh code │────▶│  Dev review  │
│  cầu bằng   │     │  ngữ cảnh    │     │  đề xuất      │     │  & chỉnh sửa │
│  ngôn ngữ   │     │  dự án       │     │               │     │              │
│  tự nhiên   │     │              │     │               │     │              │
└─────────────┘     └──────────────┘     └───────────────┘     └──────────────┘
                                                                       │
                                                                       ▼
                                                               ┌──────────────┐
                                                               │  Build & chạy│
                                                               │  kiểm tra    │
                                                               └──────────────┘
```

### 3.2 Các tình huống sử dụng AI trong Implement Code

#### 3.2.1 Sinh code mới từ yêu cầu nghiệp vụ

**Mô tả:** Developer mô tả tính năng bằng tiếng Việt, AI tạo code hoàn chỉnh.

**Ví dụ thực tế trong dự án:**

> **Prompt:** *"Thêm phần trả lại hàng từ khách hàng, khi trả hàng thì trừ vào nợ trước, nếu nợ hết thì trừ vào đã thanh toán"*

**AI đã tạo ra:**

| File được tạo/sửa               | Nội dung                                        |
|-----------------------------------|--------------------------------------------------|
| `ReturnOrder.java`               | Model chứa thông tin phiếu trả hàng              |
| `ReturnOrderDetail.java`         | Chi tiết sản phẩm trả                            |
| `ReturnOrderDAO.java`            | Interface DAO                                     |
| `ReturnOrderDAOImpl.java`        | Implement truy vấn SQLite                         |
| `ReturnOrderService.java`        | Interface Service                                 |
| `ReturnOrderServiceImpl.java`    | Logic nghiệp vụ trừ nợ → trừ thanh toán          |
| `ReturnOrderPanel.java`          | Giao diện JavaFX                                  |
| `V3__return_deduction_tracking.sql` | Migration thêm bảng `return_orders`            |

**Đoạn code ví dụ — Logic trừ nợ khi trả hàng (do AI sinh):**

```java
// ReturnOrderServiceImpl.java — Logic nghiệp vụ
public void create(ReturnOrder order) {
    returnOrderDAO.insert(order);
    
    // Lấy thông tin công nợ hiện tại
    DebtSummary debt = debtDAO.findByCustomerId(order.getCustomerId());
    double returnAmount = order.getTotalAmount();
    
    if (debt.getOwed() > 0) {
        // Trừ vào nợ trước
        double deductFromDebt = Math.min(returnAmount, debt.getOwed());
        debt.setOwed(debt.getOwed() - deductFromDebt);
        returnAmount -= deductFromDebt;
    }
    
    if (returnAmount > 0) {
        // Nợ hết rồi → trừ vào đã thanh toán
        debt.setPaid(debt.getPaid() - returnAmount);
    }
    
    debtDAO.update(debt);
    
    // Cộng lại tồn kho
    for (ReturnOrderDetail d : order.getDetails()) {
        stockDAO.updateStock(d.getProductId(), d.getQuantity());
    }
}
```

#### 3.2.2 Sửa lỗi runtime (Debug)

**Mô tả:** Developer dán stack trace lỗi vào AI, AI phân tích và đưa ra cách sửa.

**Ví dụ thực tế trong dự án:**

> **Lỗi gặp phải:**
> ```
> ClassCastException: class java.lang.String cannot be cast to
> class com.dat.whmanagement.model.Product
>   at ComboBoxHelper.lambda$makeSearchable$2(ComboBoxHelper.java:66)
> ```

> **Prompt:** *"Khi chọn sản phẩm nó bị lỗi ClassCastException, fix cho tôi"*

**AI phân tích:**
- JavaFX editable ComboBox đôi khi truyền `String` thay vì object `Product`
- Cần thêm type guard trong `StringConverter` và `valueProperty` listener

**Kết quả sửa:**

```java
// ComboBoxHelper.java — Trước khi sửa
cb.valueProperty().addListener((obs, oldVal, newVal) -> {
    String text = display.apply(newVal);  // ← CRASH nếu newVal là String
    cb.getEditor().setText(text);
});

// ComboBoxHelper.java — Sau khi sửa  
cb.valueProperty().addListener((obs, oldVal, newVal) -> {
    if (updating[0]) return;
    if (newVal == null) return;
    if (newVal instanceof String) return;  // ← Guard thêm bởi AI
    try {
        String text = display.apply(newVal);
        updating[0] = true;
        cb.getEditor().setText(text);
        updating[0] = false;
    } catch (ClassCastException ignored) {}
});
```

#### 3.2.3 Refactor / Cải thiện code hiện có

**Mô tả:** Yêu cầu AI nâng cấp tính năng đã có.

**Ví dụ thực tế:**

> **Prompt:** *"Sửa lại phần chọn khách hàng, điền mã khách hàng cũng tìm được thay vì chỉ tên"*

**AI đã thực hiện:**

1. Thêm overload `makeSearchable()` với tham số `searchText` riêng biệt:

```java
// Trước: chỉ tìm theo display text (tên)
public static <T> void makeSearchable(ComboBox<T> cb, List<T> allItems,
                                       Function<T, String> display)

// Sau: tìm theo cả display và searchText (mã + tên)
public static <T> void makeSearchable(ComboBox<T> cb, List<T> allItems,
                                       Function<T, String> display,
                                       Function<T, String> searchText)
```

2. Cập nhật 3 file View sử dụng Customer ComboBox:

```java
// Trước
ComboBoxHelper.makeSearchable(cbCustomer, customers, c -> c.getName());

// Sau
ComboBoxHelper.makeSearchable(cbCustomer, customers,
        c -> c.getCode() + " - " + c.getName(),
        c -> (c.getCode() + " " + c.getName()).toLowerCase());
```

#### 3.2.4 Tạo migration database

**Mô tả:** AI sinh file SQL migration khi cần thêm bảng/cột mới.

**Ví dụ thực tế:**

> **Prompt:** *"Thêm phần hóa đơn, khi xuất kho thì tự động tạo hóa đơn"*

**AI tạo file:** `V4__invoice_export.sql`

```sql
CREATE TABLE IF NOT EXISTS invoices (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL UNIQUE,
    sales_order_id INTEGER,
    customer_id   INTEGER NOT NULL,
    invoice_date  TEXT NOT NULL,
    subtotal      REAL NOT NULL DEFAULT 0,
    vat_rate      REAL NOT NULL DEFAULT 10,
    vat_amount    REAL NOT NULL DEFAULT 0,
    total_amount  REAL NOT NULL DEFAULT 0,
    note          TEXT,
    FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
```

#### 3.2.5 Xử lý yêu cầu liên module (Agent AI)

**Mô tả:** Khi một thay đổi ảnh hưởng nhiều lớp (Model → DAO → Service → View), Agent AI tự động tìm và sửa tất cả.

**Ví dụ thực tế:**

> **Prompt:** *"Xóa cột giá bán trong tồn kho, fix lại giá nhập và giá trị tồn"*

**Agent AI tự động sửa 4 file:**

| File                         | Thay đổi                                               |
|-------------------------------|--------------------------------------------------------|
| `StockItem.java`             | Xóa field `sellPrice`, cập nhật constructor             |
| `StockDAOImpl.java`          | Sửa SQL dùng subquery lấy giá nhập từ phiếu nhập mới nhất |
| `StockPanel.java`            | Xóa cột "Giá bán" khỏi TableView                       |
| `PurchaseOrderDAOImpl.java`  | Thêm cập nhật `buy_price` khi nhập hàng                |

**Quy trình Agent thực hiện:**

```
1. Đọc StockPanel.java      → Xác định cột cần xóa
2. Đọc StockItem.java       → Tìm field sellPrice
3. Đọc StockDAOImpl.java    → Phát hiện SQL lấy buy_price = 0
4. Đọc PurchaseOrderDAOImpl → Phát hiện không cập nhật buy_price
5. Sửa 4 file đồng thời
6. Chạy mvn compile          → Xác nhận build thành công
```

### 3.3 Bảng tổng hợp AI trong Implement Code

| STT | Tác vụ                          | Loại AI sử dụng       | Hiệu quả                     |
|-----|----------------------------------|------------------------|-------------------------------|
| 1   | Sinh code mới từ yêu cầu        | Chatbot + Agent        | Tiết kiệm 60-70% thời gian   |
| 2   | Debug từ stack trace             | Chatbot                | Xác định root cause nhanh     |
| 3   | Refactor code                    | Tool AI + Agent        | Đảm bảo sửa đồng bộ đa file  |
| 4   | Viết SQL migration               | Chatbot                | Đúng cú pháp, có constraint   |
| 5   | Sinh boilerplate (DAO, Service)  | Tool AI (Copilot)      | Giảm code lặp                 |
| 6   | Tạo UI JavaFX                   | Agent                  | Layout + logic binding nhanh  |
| 7   | Tích hợp license/bảo mật        | Chatbot + Agent        | Triển khai hardware ID lock   |

---

## 4. GIAI ĐOẠN 2: TESTING SẢN PHẨM (KIỂM THỬ)

### 4.1 Tổng quan quy trình

```
┌──────────────┐    ┌───────────────┐    ┌───────────────┐    ┌──────────────┐
│  Xác định    │───▶│  AI sinh test │───▶│  Chạy test &  │───▶│  AI phân tích│
│  phạm vi test│    │  case / code  │    │  thu thập KQ  │    │  kết quả     │
└──────────────┘    └───────────────┘    └───────────────┘    └──────────────┘
                                                                      │
                                                                      ▼
                                                              ┌──────────────┐
                                                              │  Báo cáo &   │
                                                              │  đề xuất fix │
                                                              └──────────────┘
```

### 4.2 Các tình huống sử dụng AI trong Testing

#### 4.2.1 Sinh Test Case từ yêu cầu nghiệp vụ

**Mô tả:** Từ mô tả nghiệp vụ, AI tạo danh sách test case đầy đủ bao gồm cả edge case.

**Ví dụ — Chức năng trả hàng:**

> **Prompt:** *"Tạo test case cho chức năng trả hàng: khi trả hàng trừ nợ trước, nếu nợ hết thì trừ đã thanh toán"*

**AI sinh test case:**

| TC ID   | Mô tả                                              | Đầu vào                                 | Kết quả mong đợi                        |
|---------|------------------------------------------------------|------------------------------------------|------------------------------------------|
| TC-RT01 | Trả hàng khi khách còn nợ đủ                        | Nợ=300, Trả=200                          | Nợ=100, Thanh toán giữ nguyên           |
| TC-RT02 | Trả hàng khi nợ ít hơn tiền trả                     | Nợ=100, Trả=200                          | Nợ=0, Thanh toán giảm 100               |
| TC-RT03 | Trả hàng khi không có nợ                            | Nợ=0, TT=1000, Trả=200                  | Nợ=0, TT=800                            |
| TC-RT04 | Trả hàng với số tiền = 0                            | Trả=0                                    | Không thay đổi gì                        |
| TC-RT05 | Trả hàng — tồn kho được cộng lại                   | Tồn=50, Trả SL=10                       | Tồn=60                                  |
| TC-RT06 | Xóa đơn trả hàng → hoàn lại giá trị ban đầu        | Đã trả 200, xóa đơn                     | Nợ/TT trở về giá trị trước khi trả     |
| TC-RT07 | Trả hàng có thuế VAT                                | SP 100đ × 5, VAT=10%                    | Tổng trả = 550đ                         |

#### 4.2.2 Sinh Unit Test code

**Mô tả:** AI viết JUnit test dựa trên source code hiện có.

**Ví dụ — Test cho PurchaseOrderDAOImpl:**

> **Prompt:** *"Viết unit test cho PurchaseOrderDAOImpl, đặc biệt phần insert phải cập nhật buy_price"*

**AI sinh code test:**

```java
@Test
void insert_shouldUpdateBuyPriceInProducts() {
    // Arrange
    PurchaseOrder order = new PurchaseOrder();
    order.setOrderNumber("PN0001");
    order.setSupplierId(1);
    order.setOrderDate(LocalDate.now());
    order.setTotalAmount(5000000);

    PurchaseOrderDetail detail = new PurchaseOrderDetail();
    detail.setProductId(1);
    detail.setQuantity(100);
    detail.setUnitPrice(50000);  // Giá nhập mới
    detail.setTotal(5000000);
    order.setDetails(List.of(detail));

    // Act
    purchaseOrderDAO.insert(order);

    // Assert
    Product product = productDAO.findById(1).orElseThrow();
    assertEquals(50000, product.getBuyPrice(), 
        "buy_price phải được cập nhật theo đơn giá nhập mới nhất");
    assertEquals(100, product.getStock(), 
        "stock phải tăng theo số lượng nhập");
}

@Test
void insert_shouldCreateStockMovement() {
    // Arrange & Act
    PurchaseOrder order = createSampleOrder();
    purchaseOrderDAO.insert(order);

    // Assert — Kiểm tra stock_movements được tạo
    List<StockMovement> movements = stockMovementDAO
        .findByReference("PURCHASE", order.getId());
    assertFalse(movements.isEmpty(), 
        "Phải tạo bản ghi stock_movement khi nhập hàng");
    assertEquals("PURCHASE", movements.get(0).getReferenceType());
}
```

#### 4.2.3 Sinh Integration Test

**Mô tả:** AI tạo test tích hợp kiểm tra luồng nghiệp vụ end-to-end.

**Ví dụ — Luồng Nhập hàng → Xuất hàng → Hóa đơn:**

```java
@Test
void fullFlow_purchase_then_sell_shouldCreateInvoice() {
    // 1. Nhập hàng
    PurchaseOrder po = createPurchaseOrder("PN0001", supplierId, 
        List.of(new OrderLine(productId, 100, 50000)));
    purchaseService.create(po);
    
    // Verify: tồn kho tăng
    assertEquals(100, productService.getStock(productId));
    
    // 2. Xuất hàng cho khách
    SalesOrder so = createSalesOrder("PX0001", customerId,
        List.of(new OrderLine(productId, 30, 80000)));
    salesService.create(so);
    
    // Verify: tồn kho giảm
    assertEquals(70, productService.getStock(productId));
    
    // Verify: hóa đơn tự động tạo
    Invoice invoice = invoiceService.findBySalesOrderId(so.getId());
    assertNotNull(invoice, "Hóa đơn phải được tự động tạo khi xuất kho");
    assertEquals(so.getTotalAmount(), invoice.getTotalAmount());
}
```

#### 4.2.4 Test giao diện thủ công với AI hỗ trợ

**Mô tả:** AI tạo checklist kiểm thử giao diện cho tester thực hiện thủ công.

**Ví dụ — Checklist test màn hình Xuất hàng:**

| #  | Bước thực hiện                                    | Kết quả mong đợi                              | Pass/Fail |
|----|----------------------------------------------------|-------------------------------------------------|-----------|
| 1  | Mở tab "Xuất hàng", nhấn "Tạo phiếu xuất"       | Dialog hiện, số phiếu tự sinh (PX0001...)       | ☐         |
| 2  | Gõ mã KH vào ô "Khách hàng"                      | Dropdown lọc theo mã, hiện "MÃ - Tên"          | ☐         |
| 3  | Gõ tên KH vào ô "Khách hàng"                     | Dropdown lọc theo tên                           | ☐         |
| 4  | Chọn sản phẩm, nhập SL vượt tồn kho              | Hiện cảnh báo "Không đủ tồn kho"               | ☐         |
| 5  | Nhập thuế VAT = 15%                               | Thành tiền tính đúng: subtotal × 1.15           | ☐         |
| 6  | Nhấn Lưu với khách hàng trống                     | Hiện lỗi validation                            | ☐         |
| 7  | Lưu thành công → kiểm tra Tồn kho                | SL tồn giảm đúng                                | ☐         |
| 8  | Lưu thành công → kiểm tra Hóa đơn                | Hóa đơn tự động tạo, thông tin khớp             | ☐         |
| 9  | Sửa phiếu xuất đã tạo                            | Thông tin cũ được load, cho phép sửa            | ☐         |
| 10 | Xóa phiếu xuất                                    | Tồn kho hoàn lại, hóa đơn xóa theo             | ☐         |

#### 4.2.5 Phân tích lỗi runtime bằng AI

**Mô tả:** Dán stack trace lỗi → AI xác định nguyên nhân và file cần sửa.

**Ví dụ thực tế:**

> **Stack trace:**
> ```
> SQLiteException: table return_orders has no column named deducted_from_total
>   at ReturnOrderDAOImpl.insert(ReturnOrderDAOImpl.java:35)
> ```

**AI phân tích:**
- **Nguyên nhân:** Code DAO insert cột `deducted_from_total` nhưng DB schema chưa có cột này
- **Giải pháp:** Tạo migration `V5__add_deducted_from_total.sql` thêm cột mới
- **File cần sửa:** Migration SQL + kiểm tra lại `ReturnOrderDAOImpl.java`

#### 4.2.6 Sinh dữ liệu test

**Mô tả:** AI tạo bộ dữ liệu mẫu để test các tình huống.

```sql
-- AI sinh dữ liệu test cho các tình huống biên
-- Sản phẩm với tồn kho = 0 (test xuất hàng khi hết hàng)
INSERT INTO products(code, name, unit, stock, buy_price) 
VALUES ('SP001', 'Sản phẩm hết hàng', 'Cái', 0, 50000);

-- Sản phẩm với tồn kho rất lớn
INSERT INTO products(code, name, unit, stock, buy_price) 
VALUES ('SP002', 'Sản phẩm nhiều', 'Kg', 999999, 10000);

-- Khách hàng có nợ
INSERT INTO customers(code, name) VALUES ('KH001', 'Khách nợ nhiều');
-- Tạo đơn xuất chưa thanh toán đủ → nợ = 500000
INSERT INTO sales_orders(order_number, customer_id, order_date, total_amount, paid_amount)
VALUES ('PX0001', 1, '2026-01-01', 1000000, 500000);
```

### 4.3 Bảng tổng hợp AI trong Testing

| STT | Tác vụ                              | Loại AI sử dụng  | Hiệu quả                        |
|-----|---------------------------------------|-------------------|----------------------------------|
| 1   | Sinh test case từ nghiệp vụ          | Chatbot           | Bao phủ edge case thường bỏ sót  |
| 2   | Viết Unit Test code                   | Chatbot + Tool AI | Giảm 50% thời gian viết test     |
| 3   | Viết Integration Test                 | Agent             | Test luồng đa module             |
| 4   | Tạo UI test checklist                 | Chatbot           | Checklist chi tiết, có hệ thống  |
| 5   | Phân tích stack trace                 | Chatbot           | Xác định root cause trong < 1 phút|
| 6   | Sinh dữ liệu test                    | Chatbot           | Dữ liệu đa dạng, biên giá trị   |
| 7   | Review code tìm bug tiềm ẩn          | Agent             | Phát hiện lỗi logic trước khi test|

---

## 5. QUY TRÌNH LÀM VIỆC VỚI AI

### 5.1 Quy trình tổng thể

```
  Developer                          AI                            Kết quả
  ─────────                          ──                            ────────
      │                               │                               │
      │  1. Mô tả yêu cầu/lỗi       │                               │
      │──────────────────────────────▶│                               │
      │                               │                               │
      │  2. AI phân tích context      │                               │
      │                               │──── Đọc file, hiểu cấu trúc  │
      │                               │                               │
      │  3. AI đề xuất giải pháp      │                               │
      │◀──────────────────────────────│                               │
      │                               │                               │
      │  4. Dev review & phản hồi     │                               │
      │──────────────────────────────▶│                               │
      │                               │                               │
      │  5. AI áp dụng thay đổi       │                               │
      │                               │──────────────────────────────▶│
      │                               │                               │
      │  6. AI chạy build/test         │                               │
      │                               │──── mvn compile ─────────────▶│
      │                               │                               │
      │  7. Kết quả cuối cùng         │                               │
      │◀──────────────────────────────│◀──────────────────────────────│
      │                               │                               │
```

### 5.2 Nguyên tắc khi dùng AI

| Nguyên tắc                                  | Giải thích                                                      |
|----------------------------------------------|-----------------------------------------------------------------|
| **AI là trợ lý, không phải thay thế**       | Developer luôn review và chịu trách nhiệm code cuối cùng       |
| **Mô tả rõ ràng, cụ thể**                   | Prompt càng chi tiết → kết quả càng chính xác                  |
| **Cung cấp context đầy đủ**                 | Đính kèm file, stack trace, schema DB khi cần                  |
| **Kiểm tra kết quả AI trước khi commit**    | Không tin tưởng mù quáng, luôn build + test thủ công            |
| **Chia nhỏ yêu cầu phức tạp**               | Thay vì 1 prompt lớn → chia thành nhiều bước nhỏ               |
| **Lưu lại prompt hiệu quả**                 | Tái sử dụng cho các dự án sau                                   |

### 5.3 Cấu trúc Prompt hiệu quả

```
[BỐI CẢNH]    Dự án Java 17, JavaFX, SQLite. Kiến trúc DAO-Service-View.
[YÊU CẦU]     Thêm chức năng XYZ...
[RÀNG BUỘC]   Phải tương thích với schema DB hiện tại. Không thay đổi API cũ.
[VÍ DỤ]       Tương tự cách PurchasePanel hoạt động.
[ĐẦU RA]      Tạo đầy đủ Model, DAO, Service, View. Kèm migration SQL.
```

---

## 6. ĐÁNH GIÁ HIỆU QUẢ

### 6.1 So sánh thời gian phát triển

| Tác vụ                                    | Không có AI  | Có AI      | Tiết kiệm |
|---------------------------------------------|-------------|------------|------------|
| Tạo CRUD module mới (Model→DAO→Service→UI) | 4-6 giờ     | 1-2 giờ    | ~65%       |
| Debug lỗi runtime từ stack trace            | 30-60 phút  | 5-10 phút  | ~80%       |
| Viết unit test cho 1 service class          | 2-3 giờ     | 30-45 phút | ~75%       |
| Tạo test case list cho 1 chức năng          | 1-2 giờ     | 10-15 phút | ~85%       |
| Refactor đa file (thay đổi model)          | 2-4 giờ     | 15-30 phút | ~85%       |
| Viết SQL migration                          | 20-30 phút  | 3-5 phút   | ~85%       |

### 6.2 Chất lượng code

| Tiêu chí                  | Đánh giá                                                       |
|---------------------------|----------------------------------------------------------------|
| **Đúng chức năng**         | ★★★★☆ — Đa số đúng, đôi khi cần chỉnh logic nghiệp vụ đặc thù|
| **Clean code**             | ★★★★★ — Code sạch, đặt tên biến rõ ràng                       |
| **Error handling**         | ★★★★☆ — Có try-catch, nhưng đôi khi thiếu edge case           |
| **Bảo mật**               | ★★★☆☆ — Cần developer bổ sung validation thêm                 |
| **Performance**            | ★★★★☆ — Dùng PreparedStatement, connection quản lý tốt        |

---

## 7. RỦI RO VÀ BIỆN PHÁP KIỂM SOÁT

### 7.1 Rủi ro

| Rủi ro                                        | Mức độ  | Biện pháp                                          |
|------------------------------------------------|---------|-----------------------------------------------------|
| AI sinh code sai logic nghiệp vụ              | Cao     | Developer review kỹ, viết test case trước           |
| AI không hiểu context dự án đầy đủ            | Trung bình | Cung cấp file đính kèm, giải thích schema          |
| Phụ thuộc quá nhiều vào AI                     | Trung bình | Đảm bảo developer hiểu code AI sinh ra              |
| AI sinh code không tương thích phiên bản cũ   | Thấp    | Chỉ định rõ Java version, thư viện version          |
| Lỗ hổng bảo mật trong code AI sinh            | Trung bình | Security review, không dùng AI cho phần nhạy cảm    |
| AI "hallucinate" — sinh API không tồn tại     | Thấp    | Luôn build & chạy thử trước khi commit              |

### 7.2 Checklist kiểm soát chất lượng code AI

- [ ] Code compile thành công (`mvn compile`)
- [ ] Không có warning nghiêm trọng
- [ ] Logic nghiệp vụ đúng với yêu cầu
- [ ] Edge case được xử lý (null, 0, giá trị âm, giá trị rất lớn)
- [ ] SQL injection được phòng tránh (dùng PreparedStatement)
- [ ] Transaction được sử dụng cho thao tác đa bảng
- [ ] UI hiển thị đúng tiếng Việt, format số/tiền đúng locale
- [ ] Test case bao phủ happy path + error path

---

## 8. KẾT LUẬN

### 8.1 Tóm tắt

Việc sử dụng AI trong phát triển phần mềm **Quản Lý Kho** đã mang lại hiệu quả rõ rệt:

- **Giai đoạn Implement:** AI giúp sinh code nhanh, debug hiệu quả, refactor an toàn trên nhiều file. Agent AI đặc biệt hữu ích khi thay đổi trải dài từ Model → DAO → Service → View.

- **Giai đoạn Testing:** AI giúp tạo test case bao phủ edge case mà developer thường bỏ sót, sinh unit test code nhanh, và phân tích stack trace để xác định root cause trong vài phút.

### 8.2 Khuyến nghị

1. **Nên dùng AI cho:** Sinh boilerplate code, debug, viết test, tạo migration SQL, refactor
2. **Cần cẩn thận khi dùng AI cho:** Logic nghiệp vụ phức tạp, xử lý bảo mật, tối ưu performance
3. **Không nên dùng AI cho:** Quyết định kiến trúc quan trọng mà không có review từ senior developer

### 8.3 Công cụ AI khuyến nghị cho dự án tương tự

| Giai đoạn    | Công cụ chính              | Công cụ bổ trợ            |
|--------------|----------------------------|---------------------------|
| Implement    | GitHub Copilot Agent Mode  | Claude (phân tích phức tạp)|
| Testing      | ChatGPT (sinh test case)   | Copilot (sinh test code)   |
| Review       | Claude (review logic)      | Copilot Chat (review nhanh)|
| Tài liệu    | ChatGPT / Claude           | —                          |
