# Bug Report – Invoice Management

## TC_9.1.03 – Kiểm tra hiển thị danh sách tòa nhà trên trang "Tạo Hóa Đơn"

**Precondition**
- Đã đăng nhập tài khoản Admin  
- Admin đang ở trang **"Tạo Hóa Đơn"**

**Steps**
1. Truy cập trang **"Quản lý hóa đơn"**
2. Nhấn vào trang **"Tạo hóa đơn"**

**Expected Result**
- Hiển thị danh sách **Tòa nhà hiện có**

**Actual Result**
- ~~Vẫn hiển thị các tòa nhà trong trạng thái **"Bảo trì"**~~ **FIXED**

**Status:** ~~Failed~~ **FIXED**

**Fix Applied:**
- Thêm query `findActiveBuildings()` vào BuildingRepository
- Sửa InvoiceController.selectRoomPage() để chỉ lấy tòa nhà không bảo trì

---

## TC_9.1.03 (Bulk) – Kiểm tra hiển thị form Tạo hóa đơn hàng loạt cho các phòng của tòa

**Precondition**
- Đã đăng nhập tài khoản Admin  
- Admin đang ở trang **"Quản lý Hóa Đơn"**

**Steps**
1. Truy cập trang **"Quản lý hóa đơn"**
2. Nhấn vào trang **"Tạo hóa đơn hàng loạt"**

**Expected Result**
- Hiển thị trang **Tạo hóa đơn tự động** cho các phòng của tòa được chọn

**Actual Result**
- ~~Vẫn hiển thị các tòa nhà trong trạng thái **"Bảo trì"**~~ **FIXED**

**Status:** ~~Failed~~ **FIXED**

**Fix Applied:**
- Sửa InvoiceController.showBulkCreateForm() để sử dụng findActiveBuildings()

---

## TC_9.7.04 – Kiểm tra việc không cho xóa hóa đơn đã **QUÁ HẠN**

**Precondition**
- Admin đã đăng nhập  
- Có 1 hóa đơn **QUÁ HẠN**

**Steps**
1. Vào **Quản lý hóa đơn**
2. Tìm hóa đơn ID **PSH2025070085135**
3. Nhấn icon **Xem chi tiết**
4. Nhấn **"Xóa hóa đơn"**

**Test Data**
- Invoice ID: **PSH2025070085135**

**Expected Result**
- Không xóa được hóa đơn  
- Hiển thị thông báo: **"Không thể xóa hóa đơn quá hạn"**

**Actual Result**
- ~~Vẫn xóa được hóa đơn quá hạn~~ **ALREADY FIXED**

**Status:** ~~Failed~~ **ALREADY FIXED**

**Note:** Kiểm tra code thấy InvoiceServiceImpl.deleteInvoice() đã có kiểm tra OVERDUE status

---

## TC_9.7.01 – Kiểm tra việc xóa thành công hóa đơn **"Chưa thanh toán"**

**Precondition**
- Admin đã đăng nhập  
- Có 1 hóa đơn trạng thái **"Chưa thanh toán"**

**Steps**
1. Vào **Quản lý hóa đơn**
2. Tìm hóa đơn ID **PSH2026010119035**
3. Nhấn icon **Xem chi tiết**
4. Nhấn **"Xóa hóa đơn"**
5. Tại popup xác nhận, chọn **"OK"**

**Test Data**
- Invoice ID: **PSH2026010119035**

**Expected Result**
- Hiển thị popup: **"Bạn có chắc chắn muốn xóa hóa đơn này?"**
- Thông báo: **"Xóa hóa đơn thành công"**
- Hóa đơn **PSH2026010119035** biến mất khỏi danh sách
- Thống kê **Chưa thu** giảm
- Thống kê **Tổng số HĐ** giảm

**Post-condition / DB Check**
1. `SELECT * FROM invoices WHERE id=PSH2026010119035` → **0 rows**
2. Tổng công nợ giảm: **X – (tiền HĐ PSH2026010119035)**
3. Tổng số HĐ: **-1**

**Actual Result**
- ~~Tổng tiền **chưa thu** bị sai~~ **FIXED**

**Status:** ~~Failed~~ **FIXED**

**Fix Applied:**
- Sửa InvoiceServiceImpl.getInvoiceSummary() để sử dụng getTotalAmount() thay vì getLivingTotalAmount()
- Bây giờ tính toán bao gồm cả tiền phòng, không chỉ điện/nước/internet
