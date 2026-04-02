# Bảng Users ( người dùng )
Bàng này lưu trữ thông tin chung của tất cả người dùng trong hệ thống ( ADMIN , TEACHER , STUDENT ) , 

Mục đích : quản lý tài khoản người dùng , phân quyền truy cập 


| **Tên trường**    | **Kiểu dữ liệu**                        | **Ràng buộc**                                                       | **Mô tả**                                            |
| ------------- | ----------------------------------- | --------------------------------------------------------------- | ------------------------------------------------ |
| user_id       | INT                                 | PRIMARY KEY, AUTO_INCREMENT                                     | ID duy nhất của người dùng                       |
| username      | VARCHAR(50)                         | UNIQUE, NOT NULL                                                | Tên đăng nhập                                    |
| password_hash | VARCHAR(255)                        | NOT NULL                                                        | Mật khẩu đã được mã hóa                          |
| email         | VARCHAR(100)                        | UNIQUE, NOT NULL                                                | Địa chỉ email                                    |
| full_name     | VARCHAR(100)                        | NOT NULL                                                        | Họ và tên đầy đủ                                 |
| role          | ENUM('ADMIN', 'TEACHER', 'STUDENT') | NOT NULL, DEFAULT 'STUDENT'                                     | Vai trò của người dùng (ADMIN, TEACHER, STUDENT) |
| is_active     | BOOLEAN                             | NOT NULL, DEFAULT TRUE                                          | Trạng thái hoạt động của tài khoản               |
| created_at    | DATETIME                            | NOT NULL, DEFAULT CURRENT_TIMESTAMP                             | Thời gian tạo tài khoản                          |
| updated_at    | DATETIME                            | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật tài khoản gần nhất            |

# Bảng Courses ( khóa học )

Bảng này lưu trữ thông tin về các khóa học được cung cấp

Mục đích: Quản lý thông tin chi tiết các khóa học, liên kết với giảng viên.

|   |   |   |   |
|---|---|---|---|
|Tên trường|Kiểu dữ liệu|Ràng buộc|Mô tả|
|course_id|INT|PRIMARY KEY, AUTO_INCREMENT|ID duy nhất của khóa học|
|title|VARCHAR(255)|NOT NULL|Tiêu đề của khóa học|
|description|TEXT|NULLABLE|Mô tả chi tiết về khóa học|
|teacher_id|INT|FOREIGN KEY REFERENCES Users(user_id), NOT NULL|ID của giảng viên phụ trách khóa học (phải là role = 'TEACHER')|
|price|DECIMAL(10, 2)|NOT NULL, DEFAULT 0.00|Giá của khóa học|
|duration_hours|INT|NULLABLE|Thời lượng khóa học tính bằng giờ|
|status|ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED')|NOT NULL, DEFAULT 'DRAFT'|Trạng thái hiện tại của khóa học|
|created_at|DATETIME|NOT NULL, DEFAULT CURRENT_TIMESTAMP|Thời gian tạo khóa học|
|updated_at|DATETIME|NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|Thời gian cập nhật khóa học gần nhất|

# 3. Bảng Lessons (Bài học)

Bảng này lưu trữ các bài học thuộc về từng khóa học.
Mục đích: Quản lý nội dung và thứ tự các bài học trong một khóa học.


| Tên trường   | Kiểu dữ liệu | Ràng buộc                                                       | Mô tả                                                                                  |
| ------------ | ------------ | --------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| lesson_id    | INT          | PRIMARY KEY, AUTO_INCREMENT                                     | ID duy nhất của bài học                                                                |
| course_id    | INT          | FOREIGN KEY REFERENCES Courses(course_id), NOT NULL             | ID của khóa học chứa bài học này                                                       |
| title        | VARCHAR(255) | NOT NULL                                                        | Tiêu đề của bài học                                                                    |
| content_url  | VARCHAR(500) | NULLABLE                                                        | URL đến nội dung chính của bài học (video, slide, v.v.). Có thể là link file đính kèm. |
| text_content | TEXT         | NULLABLE                                                        | Nội dung văn bản của bài học                                                           |
| order_index  | INT          | NOT NULL                                                        | Thứ tự của bài học trong khóa học                                                      |
| is_published | BOOLEAN      | NOT NULL, DEFAULT FALSE                                         | Trạng thái xuất bản của bài học                                                        |
| created_at   | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP                             | Thời gian tạo bài học                                                                  |
| updated_at   | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật bài học gần nhất                                                    |
# 4. Bảng Enrollments (Đăng ký khóa học)

Bảng này ghi lại mối quan hệ giữa sinh viên và khóa học mà họ đã đăng ký.
Mục đích: Theo dõi sinh viên đã đăng ký khóa học nào và tình trạng đăng ký của họ.


| Tên trường                    | Kiểu dữ liệu                             | Ràng buộc                                                                               | Mô tả                                                           |
| ----------------------------- | ---------------------------------------- | --------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| enrollment_id                 | INT                                      | PRIMARY KEY, AUTO_INCREMENT                                                             | ID duy nhất của lượt đăng ký                                    |
| student_id                    | INT                                      | FOREIGN KEY REFERENCES Users(user_id), NOT NULL                                         | ID của sinh viên đăng ký (phải là role = 'STUDENT')             |
| course_id                     | INT                                      | FOREIGN KEY REFERENCES Courses(course_id), NOT NULL                                     | ID của khóa học được đăng ký                                    |
| enrollment_date               | DATETIME                                 | NOT NULL, DEFAULT CURRENT_TIMESTAMP                                                     | Ngày sinh viên đăng ký khóa học                                 |
| status                        | ENUM('ENROLLED', 'COMPLETED', 'DROPPED') | NOT NULL, DEFAULT 'ENROLLED'                                                            | Trạng thái đăng ký của sinh viên                                |
| completion_date               | DATETIME                                 | NULLABLE                                                                                | Ngày sinh viên hoàn thành khóa học                              |
| progress_percentage           | DECIMAL(5, 2)                            | NOT NULL, DEFAULT 0.00, CHECK (progress_percentage >= 0 AND progress_percentage <= 100) | Phần trăm tiến độ hoàn thành khóa học (dựa trên LessonProgress) |
| UNIQUE(student_id, course_id) |                                          |                                                                                         | Đảm bảo mỗi sinh viên chỉ đăng ký một khóa học một lần          |

# 5. Bảng LessonProgress (Tiến độ bài học)

Bảng này theo dõi tiến độ của từng sinh viên đối với từng bài học trong khóa học đã đăng ký.
Mục đích: Ghi nhận và tính toán tiến độ học tập chi tiết của sinh viên.

| Tên trường                       | Kiểu dữ liệu | Ràng buộc                                                       | Mô tả                                                               |
| -------------------------------- | ------------ | --------------------------------------------------------------- | ------------------------------------------------------------------- |
| progress_id                      | INT          | PRIMARY KEY, AUTO_INCREMENT                                     | ID duy nhất của tiến độ bài học                                     |
| enrollment_id                    | INT          | FOREIGN KEY REFERENCES Enrollments(enrollment_id), NOT NULL     | ID của lượt đăng ký tương ứng                                       |
| lesson_id                        | INT          | FOREIGN KEY REFERENCES Lessons(lesson_id), NOT NULL             | ID của bài học                                                      |
| is_completed                     | BOOLEAN      | NOT NULL, DEFAULT FALSE                                         | Trạng thái hoàn thành bài học                                       |
| completed_at                     | DATETIME     | NULLABLE                                                        | Thời gian hoàn thành bài học                                        |
| last_accessed_at                 | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian truy cập bài học gần nhất                                 |
| UNIQUE(enrollment_id, lesson_id) |              |                                                                 | Đảm bảo mỗi bài học chỉ có một bản ghi tiến độ cho mỗi lượt đăng ký |

# 6. Bảng Notifications (Thông báo)

Bảng này lưu trữ các thông báo hệ thống gửi đến người dùng.
Mục đích: Cung cấp chức năng thông báo cho người dùng về các sự kiện quan trọng.

| Tên trường      | Kiểu dữ liệu | Ràng buộc                                       | Mô tả                                                                          |
| --------------- | ------------ | ----------------------------------------------- | ------------------------------------------------------------------------------ |
| notification_id | INT          | PRIMARY KEY, AUTO_INCREMENT                     | ID duy nhất của thông báo                                                      |
| user_id         | INT          | FOREIGN KEY REFERENCES Users(user_id), NOT NULL | ID của người dùng nhận thông báo                                               |
| message         | TEXT         | NOT NULL                                        | Nội dung thông báo                                                             |
| type            | VARCHAR(50)  | NULLABLE                                        | Loại thông báo (ví dụ: 'NEW_COURSE', 'LESSON_UPDATED', 'ENROLLMENT_CONFIRMED') |
| target_url      | VARCHAR(500) | NULLABLE                                        | URL liên quan đến thông báo (ví dụ: đến trang khóa học)                        |
| is_read         | BOOLEAN      | NOT NULL, DEFAULT FALSE                         | Trạng thái đã đọc của thông báo                                                |
| created_at      | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP             | Thời gian tạo thông báo                                                        |

# 7. Bảng Reviews (Đánh giá khóa học)
Bảng này lưu trữ các đánh giá và bình luận của sinh viên về khóa học.
Mục đích: Cho phép sinh viên đánh giá khóa học, cung cấp thông tin phản hồi.

| Tên trường                    | Kiểu dữ liệu | Ràng buộc                                                       | Mô tả                                                   |
| ----------------------------- | ------------ | --------------------------------------------------------------- | ------------------------------------------------------- |
| review_id                     | INT          | PRIMARY KEY, AUTO_INCREMENT                                     | ID duy nhất của đánh giá                                |
| course_id                     | INT          | FOREIGN KEY REFERENCES Courses(course_id), NOT NULL             | ID của khóa học được đánh giá                           |
| student_id                    | INT          | FOREIGN KEY REFERENCES Users(user_id), NOT NULL                 | ID của sinh viên đã đánh giá                            |
| rating                        | INT          | NOT NULL, CHECK (rating >= 1 AND rating <= 5)                   | Điểm đánh giá (từ 1 đến 5 sao)                          |
| comment                       | TEXT         | NULLABLE                                                        | Bình luận chi tiết của sinh viên                        |
| created_at                    | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP                             | Thời gian tạo đánh giá                                  |
| updated_at                    | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật đánh giá gần nhất                    |
| UNIQUE(course_id, student_id) |              |                                                                 | Đảm bảo mỗi sinh viên chỉ đánh giá một khóa học một lần |

