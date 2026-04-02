|   |                                                              |   |   |   |
|---|--------------------------------------------------------------|---|---|---|
|STT| API endpoint                                                 |Phương thức|Quyền|Chức năng chi tiết|
|1| /api/auth/login                                              |POST|PUBLIC|Đăng nhập và nhận token xác thực (JWT)|
|2| /api/auth/verify                                             |POST|AUTH|Xác thực token người dùng|
|3| /api/auth/me                                                 |GET|AUTH|Lấy thông tin hồ sơ của người dùng hiện tại|
|4| /api/users                                                   |GET|ADMIN|Lấy danh sách tất cả người dùng (có thể lọc theo role, trạng thái)|
|5| /api/users/{user_id}                                         |GET|ADMIN|Lấy thông tin chi tiết một người dùng|
|6| /api/users/register                                          |POST|ADMIN|Tạo tài khoản người dùng mới|
|7| /api/users/{user_id}/role                                    |PUT|ADMIN|Cập nhật vai trò (role) của người dùng (ADMIN không được quyền update ROLE của ADMIN khác)|
|8| /api/users/{user_id}/status                                  |PUT|ADMIN|Kích hoạt/vô hiệu hóa tài khoản người dùng (is_active)|
|9| /api/users/{user_id}                                         |DELETE|ADMIN|Xóa người dùng khỏi hệ thống|
|10| /api/courses                                                 |GET|AUTH|Lấy danh sách tất cả khóa học (có thể lọc theo trạng thái PUBLISHED)|
|11| /api/courses/{course_id}                                     |GET|AUTH|Lấy thông tin chi tiết một khóa học (bao gồm danh sách bài học PUBLISHED)|
|12| /api/courses                                                 |POST|ADMIN|Tạo khóa học mới (gán giảng viên, trạng thái ban đầu là DRAFT)|
|13| /api/courses/{course_id}                                     |PUT|ADMIN|Cập nhật thông tin chi tiết khóa học|
|14| /api/courses/{course_id}/status                              |PUT|ADMIN|Cập nhật trạng thái khóa học (DRAFT, PUBLISHED, ARCHIVED)|
|15| /api/courses/{course_id}                                     |DELETE|ADMIN|Xóa khóa học|
|16| /api/courses/{course_id}/lessons                             |GET|AUTH|Lấy danh sách tất cả bài học trong một khóa học (chỉ hiển thị bài PUBLISHED)|
|17| /api/lessons/{lesson_id}                                     |GET|AUTH|Lấy thông tin chi tiết một bài học (chỉ bài PUBLISHED)|
|18| /api/courses/{course_id}/lessons                             |POST|TEACHER_OR_ADMIN|Thêm bài học mới vào khóa học (TEACHER phải là người phụ trách)|
|19| /api/lessons/{lesson_id}                                     |PUT|TEACHER_OR_ADMIN|Cập nhật thông tin bài học|
|20| /api/lessons/{lesson_id}/publish                             |PUT|TEACHER_OR_ADMIN|Cập nhật trạng thái hiển thị bài học (is_published)|
|21| /api/lessons/{lesson_id}                                     |DELETE|TEACHER_OR_ADMIN|Xóa bài học|
|22| /api/enrollments                                             |GET|STUDENT|Lấy danh sách các khóa học sinh viên đã đăng ký|
|23| /api/enrollments                                             |POST|STUDENT|Đăng ký một khóa học|
|24| /api/enrollments/{enrollment_id}                             |GET|STUDENT|Lấy chi tiết thông tin đăng ký (tiến độ học) của mình|
|25| /api/enrollments/{enrollment_id}/complete_lesson/{lesson_id} |PUT|STUDENT|Cập nhật tiến độ học: đánh dấu một bài học đã hoàn thành|
|26| /api/users/{user_id}                                         |PUT|OWNER_OR_ADMIN|Cập nhật thông tin cá nhân của người dùng|
|27| /api/users/{user_id}/password                                |PUT|OWNER_OR_ADMIN|Đổi mật khẩu của người dùng|
|28| /api/courses?search={keyword}                                |GET|AUTH|Tìm kiếm khóa học theo từ khóa trong tiêu đề/mô tả|
|29| /api/courses?teacher_id={teacher_id}                         |GET|AUTH|Lọc khóa học theo giảng viên|
|30| /api/auth/logout                                             |POST|AUTH|Đăng xuất (invalidate token)|
|31| /api/users?status={status}                                   |GET|ADMIN|Lọc người dùng theo trạng thái (active/inactive)|
|32| /api/courses?status={status}                                 |GET|AUTH|Lọc khóa học theo trạng thái (ADMIN thấy tất cả, STUDENT/TEACHER chỉ thấy PUBLISHED)|
|33| /api/notifications                                           |GET|AUTH|Lấy danh sách thông báo của người dùng|
|34| /api/notifications/{notification_id}/read                    |PUT|AUTH|Đánh dấu thông báo đã đọc|
|35| /api/notifications                                           |POST|ADMIN|Tạo thông báo mới cho người dùng|
|36| /api/notifications/{notification_id}                         |DELETE|ADMIN|Xóa thông báo|
|37| /api/reports/top_courses                                     |GET|ADMIN|Lấy danh sách các khóa học phổ biến nhất (theo số lượt đăng ký)|
|38| /api/reports/student_progress/{student_id}                   |GET|ADMIN|Thống kê tiến độ học của một sinh viên cụ thể|
|39| /api/reports/teacher_courses_overview/{teacher_id}           |GET|ADMIN|Thống kê tổng quan về các khóa học của một giảng viên|
|40| /api/courses/{course_id}/reviews                             |GET|AUTH|Lấy danh sách đánh giá/bình luận về khóa học|
|41| /api/courses/{course_id}/reviews                             |POST|STUDENT|Sinh viên gửi đánh giá/bình luận về khóa học đã học|
|42| /api/reviews/{review_id}                                     |PUT|OWNER_OR_ADMIN|Cập nhật đánh giá/bình luận|
|43| /api/reviews/{review_id}                                     |DELETE|OWNER_OR_ADMIN|Xóa đánh giá/bình luận|
|44| /api/lessons/{lesson_id}/content_preview                     |GET|AUTH|Lấy nội dung xem trước của bài học (ví dụ: đoạn trích ngắn)|