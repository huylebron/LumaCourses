**

# Tài liệu Đặc tả API - Đề tài Quản lý Khóa học (Course Management System)

## 1. Giới thiệu

Hệ thống API Quản lý Khóa học cung cấp các chức năng:

- Đăng ký, đăng nhập tài khoản
    
- Sinh viên: xem danh sách khóa học, chi tiết khóa học, đăng ký khóa học
    
- Giảng viên: thêm/sửa/xóa bài học trong khóa học mình phụ trách
    
- Admin: tạo/sửa/xóa khóa học, quản lý người dùng và phân quyền
    

Hệ thống có phân quyền người dùng gồm các vai trò chính:

- ADMIN: Toàn quyền quản lý hệ thống, khóa học, người dùng
    
- TEACHER: Tạo và chỉnh sửa bài học cho các khóa học mình phụ trách
    
- STUDENT: Đăng ký, xem thông tin và học các khóa học
    



---

## 2. Chuẩn Response API

Tất cả response trả về theo format chuẩn:

{

  "success": true,

  "message": "Thao tác thành công",

  "data": [],

  "errors": null,

  "timestamp": "2025-07-30T09:45:00"

}

  
  

hoặc có phân trang : 

{

  "success": true,

  "message": "Lấy danh sách thành công",

  "data": {

    "items": [

      { "id": 1, "name": "Item 1" },

      { "id": 2, "name": "Item 2" }

    ],

    "pagination": {

      "currentPage": 1,

      "pageSize": 10,

      "totalPages": 5,

      "totalItems": 50

    }

  },

  "errors": null,

  "timestamp": "2025-07-30T09:45:00"

}

  

### Trường thông tin:

- success: true/false
    
- message: Thông báo
    
- data: Kết quả trả về (object hoặc array)
    
- errors: Danh sách lỗi validation (nếu có)
    
- timestamp: Thời gian server trả về
    

### Response lỗi validation:

{

  "success": false,

  "message": "Dữ liệu không hợp lệ",

  "data": null,

  "errors": [

    { "field": "email", "message": "Email không hợp lệ" },

    { "field": "password", "message": "Mật khẩu phải tối thiểu 6 ký tự" }

  ],

  "timestamp": "2025-07-30T09:45:00"

}

  
  

### Mã trạng thái HTTP:

|   |   |   |
|---|---|---|
|HTTP Status|Ý nghĩa|Tình huống sử dụng|
|200 OK|Thành công|GET, PUT, DELETE thành công|
|201 Created|Tạo mới thành công|POST thành công (ví dụ: tạo tài khoản, khóa học)|
|400 Bad Request|Dữ liệu đầu vào không hợp lệ|Sai định dạng, thiếu trường, validation lỗi|
|401 Unauthorized|Thiếu token hoặc token sai|Không đăng nhập hoặc JWT không hợp lệ|
|403 Forbidden|Không có quyền truy cập|Truy cập sai vai trò|
|404 Not Found|Không tìm thấy tài nguyên|ID không tồn tại|
|409 Conflict|Xung đột logic nghiệp vụ|Đăng ký trùng, tên bị trùng|
|500 Internal Server Error|Lỗi hệ thống bất ngờ|Null pointer, exception không xử lý|

  

---

## 3. Yêu cầu Test Case

Mỗi API bắt buộc phải viết tối thiểu 3 test case:

|   |   |
|---|---|
|Loại test case|Mô tả|
|Happy path|Truyền đúng input, kỳ vọng kết quả trả về đúng|
|Invalid input|Bỏ trường, input sai format, ID không tồn tại|
|Unauthorized / Role|Thiếu token(401), hoặc sai vai trò truy cập (403)|
|Conflict / Business|Trùng đăng ký, trái ràng buộc logic (đã ghi danh không đăng ký lại)|

---

## 

---

## 4. Cấu trúc Dự án (Spring Boot)

src/main/java/com/example/coursems

├── config                # JWT, Security, Exception Handler

├── controller            # API endpoints

├── dto                  # Request/Response models

├── entity                # JPA Entity

├── repository            # Spring Data JPA Repos

├── service               # Business logic (interface + impl)

├── mapper                # Map DTO <-> Entity

└── util                  # Helper classes (Validation, Constants)

---

## 5. Yêu cầu Clean Code

✅ Tên hàm/tên biến rõ ràng, camelCase

✅ Tuân thủ kiến trúc phân tầng, tránh lặp code

✅ Mỗi lớp service tách rõ interface và implement

✅ Dùng @Valid, DTO và Mapper để tách logic entity

✅ Không trả trực tiếp Entity ra ngoài (dùng DTO)

✅ Tách config JWT/Exception thành package riêng

✅ Dùng enum thay cho chuỗi cứng (status, role...)

✅ Tuân thủ chuẩn REST

---

## 6. Yêu cầu Công nghệ Sử dụng


| Thành phần       | Công nghệ đề xuất             |
| ---------------- | ----------------------------- |
| Ngôn ngữ backend | Java 21                       |
| Framework chính  | Spring Boot 3.x               |
| ORM              | Spring Data JPA               |
| CSDL             | PostGreSQL                    |
| Bảo mật          | Spring Security + JWT         |
| Build tool       | Maven                         |
| Test             | Postman                       |
| Triển khai       | Tomcat Server                 |
| Tiện ích         | lombok , swagger , validation |


