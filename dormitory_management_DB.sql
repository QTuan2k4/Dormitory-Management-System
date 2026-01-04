-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3307
-- Thời gian đã tạo: Th10 18, 2025 lúc 06:54 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `dormitory_management`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `buildings`
--

CREATE TABLE `buildings` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `total_floors` int(11) DEFAULT 0,
  `description` text DEFAULT NULL,
  `status` varchar(50) DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `contracts`
--

CREATE TABLE `contracts` (
  `id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `manual_fee` decimal(10,2) NOT NULL,
  `status` enum('active','expired') DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Bẫy `contracts`
--
DELIMITER $$
CREATE TRIGGER `after_contract_delete` AFTER DELETE ON `contracts` FOR EACH ROW BEGIN
    UPDATE rooms SET current_occupants = current_occupants - 1 WHERE id = OLD.room_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `after_contract_insert` AFTER INSERT ON `contracts` FOR EACH ROW BEGIN
    UPDATE rooms SET current_occupants = current_occupants + 1 WHERE id = NEW.room_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `damage_reports`
--

CREATE TABLE `damage_reports` (
  `id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text NOT NULL,
  `photo_paths` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`photo_paths`)),
  `status` enum('reported','assigned','resolved','rejected') DEFAULT 'reported',
  `assigned_staff_id` int(11) DEFAULT NULL,
  `resolution_notes` text DEFAULT NULL,
  `reported_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `resolved_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoices`
--

CREATE TABLE `invoices` (
  `id` int(11) NOT NULL,
  `contract_id` int(11) NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
  `room_fee` decimal(10,2) DEFAULT 0.00,
  `electricity_fee` decimal(10,2) DEFAULT 0.00,
  `water_fee` decimal(10,2) DEFAULT 0.00,
  `internet_fee` decimal(10,2) DEFAULT 0.00,
  `total_amount` decimal(10,2) GENERATED ALWAYS AS (`room_fee` + `electricity_fee` + `water_fee` + `internet_fee`) STORED,
  `status` enum('unpaid','paid','overdue') DEFAULT 'unpaid',
  `pdf_path` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `type` enum('reminder_payment','approval_status','damage_update','general') NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `sent_via` enum('email','app','both') DEFAULT 'email',
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `invoice_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('cash','bank_transfer','qr_bank') DEFAULT 'qr_bank',
  `transaction_id` varchar(100) DEFAULT NULL,
  `payment_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('pending','success','failed') DEFAULT 'pending',
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `rooms`
--

CREATE TABLE `rooms` (
  `id` int(11) NOT NULL,
  `building_id` int(11) NOT NULL,
  `room_number` varchar(20) NOT NULL,
  `floor` int(11) NOT NULL,
  `slot` int(11) DEFAULT 4,
  `current_occupants` int(11) DEFAULT 0,
  `status` enum('AVAILABLE','OCCUPIED','MAINTENANCE') DEFAULT 'AVAILABLE',
  `price_per_year` decimal(10,2) DEFAULT 0.00,
  `area` decimal(10,2) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `students`
--

CREATE TABLE `students` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `student_id` varchar(20) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `documents_path` varchar(500) DEFAULT NULL,
  `registration_status` enum('pending','approved','rejected') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('admin','staff','student') NOT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `buildings`
--
ALTER TABLE `buildings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_name` (`name`);

--
-- Chỉ mục cho bảng `contracts`
--
ALTER TABLE `contracts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_student` (`student_id`),
  ADD KEY `idx_room` (`room_id`),
  ADD KEY `idx_status` (`status`);

--
-- Chỉ mục cho bảng `damage_reports`
--
ALTER TABLE `damage_reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `assigned_staff_id` (`assigned_staff_id`),
  ADD KEY `idx_student` (`student_id`),
  ADD KEY `idx_room` (`room_id`),
  ADD KEY `idx_status` (`status`);

--
-- Chỉ mục cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `invoice_number` (`invoice_number`),
  ADD KEY `idx_contract` (`contract_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_due_date` (`due_date`);

--
-- Chỉ mục cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_type` (`type`),
  ADD KEY `idx_is_read` (`is_read`);

--
-- Chỉ mục cho bảng `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_invoice` (`invoice_id`),
  ADD KEY `idx_status` (`status`);

--
-- Chỉ mục cho bảng `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_room` (`building_id`,`room_number`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_building` (`building_id`);

--
-- Chỉ mục cho bảng `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `student_id` (`student_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `idx_status` (`registration_status`),
  ADD KEY `idx_student_id` (`student_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_role` (`role`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `buildings`
--
ALTER TABLE `buildings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `contracts`
--
ALTER TABLE `contracts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `damage_reports`
--
ALTER TABLE `damage_reports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `rooms`
--
ALTER TABLE `rooms`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `students`
--
ALTER TABLE `students`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `contracts`
--
ALTER TABLE `contracts`
  ADD CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `contracts_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `damage_reports`
--
ALTER TABLE `damage_reports`
  ADD CONSTRAINT `damage_reports_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `damage_reports_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `damage_reports_ibfk_3` FOREIGN KEY (`assigned_staff_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `invoices_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `students`
--
ALTER TABLE `students`
  ADD CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


-- =====================================================
-- DỮ LIỆU MẪU CHO HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ
-- =====================================================

-- Thêm cột version cho Optimistic Locking (nếu chưa có)
ALTER TABLE `invoices` ADD COLUMN IF NOT EXISTS `version` BIGINT DEFAULT 0;

-- Dữ liệu mẫu cho Buildings
INSERT INTO `buildings` (`id`, `name`, `total_floors`, `description`, `status`) VALUES
(1, 'Tòa A - Nam', 5, 'Ký túc xá nam sinh viên', 'ACTIVE'),
(2, 'Tòa B - Nữ', 5, 'Ký túc xá nữ sinh viên', 'ACTIVE'),
(3, 'Tòa C - Hỗn hợp', 4, 'Ký túc xá hỗn hợp', 'ACTIVE');

-- Dữ liệu mẫu cho Rooms
INSERT INTO `rooms` (`id`, `building_id`, `room_number`, `floor`, `slot`, `current_occupants`, `status`, `price_per_year`, `area`, `description`) VALUES
(1, 1, 'A101', 1, 4, 2, 'AVAILABLE', 3600000.00, 25.00, 'Phòng 4 người, có điều hòa'),
(2, 1, 'A102', 1, 4, 4, 'OCCUPIED', 3600000.00, 25.00, 'Phòng 4 người, có điều hòa'),
(3, 1, 'A201', 2, 6, 3, 'AVAILABLE', 3000000.00, 30.00, 'Phòng 6 người'),
(4, 2, 'B101', 1, 4, 2, 'AVAILABLE', 4000000.00, 25.00, 'Phòng 4 người, có điều hòa, WC riêng'),
(5, 2, 'B201', 2, 4, 4, 'OCCUPIED', 4000000.00, 25.00, 'Phòng 4 người, có điều hòa, WC riêng');

-- Dữ liệu mẫu cho Users
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `role`, `is_active`) VALUES
(1, 'admin', 'admin@ktx.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'admin', 1),
(2, 'staff01', 'staff01@ktx.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'staff', 1),
(3, 'sv001', 'nguyenvana@student.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'student', 1),
(4, 'sv002', 'tranthib@student.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'student', 1),
(5, 'sv003', 'levanc@student.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'student', 1),
(6, 'sv004', 'phamthid@student.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLGjPsZf8/X3JZmPpTvN3Cmu', 'student', 1);
-- Password mẫu: 123456

-- Dữ liệu mẫu cho Students
INSERT INTO `students` (`id`, `user_id`, `full_name`, `student_id`, `phone`, `address`, `birth_date`, `registration_status`) VALUES
(1, 3, 'Nguyễn Văn An', '20210001', '0901234567', 'Hà Nội', '2002-05-15', 'approved'),
(2, 4, 'Trần Thị Bình', '20210002', '0912345678', 'Hải Phòng', '2002-08-20', 'approved'),
(3, 5, 'Lê Văn Cường', '20210003', '0923456789', 'Nam Định', '2002-12-10', 'approved'),
(4, 6, 'Phạm Thị Dung', '20210004', '0934567890', 'Thái Bình', '2002-03-25', 'pending');

-- Dữ liệu mẫu cho Contracts (status: 0=ACTIVE, 1=INACTIVE, 2=TERMINATED)
INSERT INTO `contracts` (`id`, `student_id`, `room_id`, `start_date`, `end_date`, `manual_fee`, `status`) VALUES
(1, 1, 1, '2024-09-01', '2025-06-30', 3600000.00, 0),
(2, 2, 4, '2024-09-01', '2025-06-30', 4000000.00, 0),
(3, 3, 2, '2024-09-01', '2025-06-30', 3600000.00, 0);

-- Dữ liệu mẫu cho Invoices (Hóa đơn)
-- Lưu ý: status là enum số (0=UNPAID, 1=PAID, 2=OVERDUE) theo thứ tự trong Java enum
INSERT INTO `invoices` (`id`, `contract_id`, `invoice_number`, `issue_date`, `due_date`, `room_fee`, `electricity_fee`, `water_fee`, `internet_fee`, `status`, `version`) VALUES
(1, 1, 'INV-2024-001', '2024-10-01', '2024-10-15', 300000.00, 150000.00, 50000.00, 100000.00, 1, 1),
(2, 1, 'INV-2024-002', '2024-11-01', '2024-11-15', 300000.00, 180000.00, 55000.00, 100000.00, 1, 1),
(3, 1, 'INV-2024-003', '2024-12-01', '2024-12-15', 300000.00, 200000.00, 60000.00, 100000.00, 0, 0),
(4, 2, 'INV-2024-004', '2024-10-01', '2024-10-15', 333333.00, 120000.00, 45000.00, 100000.00, 1, 1),
(5, 2, 'INV-2024-005', '2024-11-01', '2024-11-15', 333333.00, 140000.00, 50000.00, 100000.00, 0, 0),
(6, 2, 'INV-2024-006', '2024-12-01', '2024-12-15', 333333.00, 160000.00, 55000.00, 100000.00, 0, 0),
(7, 3, 'INV-2024-007', '2024-10-01', '2024-10-15', 300000.00, 130000.00, 48000.00, 100000.00, 2, 0),
(8, 3, 'INV-2024-008', '2024-11-01', '2024-11-15', 300000.00, 145000.00, 52000.00, 100000.00, 0, 0);

-- Dữ liệu mẫu cho Payments (Thanh toán)
INSERT INTO `payments` (`id`, `invoice_id`, `amount`, `payment_method`, `transaction_id`, `payment_date`, `status`) VALUES
(1, 1, 600000.00, 'bank_transfer', 'TXN-2024-10-001', '2024-10-10 10:30:00', 'success'),
(2, 2, 635000.00, 'qr_bank', 'TXN-2024-11-001', '2024-11-12 14:20:00', 'success'),
(3, 4, 598333.00, 'bank_transfer', 'TXN-2024-10-002', '2024-10-08 09:15:00', 'success');

-- Dữ liệu mẫu cho Notifications
INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `sent_via`) VALUES
(1, 3, 'Hóa đơn mới', 'Bạn có hóa đơn tháng 12/2024 cần thanh toán. Hạn: 15/12/2024', 'reminder_payment', 0, 'both'),
(2, 4, 'Hóa đơn mới', 'Bạn có hóa đơn tháng 11/2024 cần thanh toán. Hạn: 15/11/2024', 'reminder_payment', 0, 'both'),
(3, 5, 'Nhắc nhở thanh toán', 'Hóa đơn tháng 10/2024 đã quá hạn. Vui lòng thanh toán ngay!', 'reminder_payment', 0, 'email');

