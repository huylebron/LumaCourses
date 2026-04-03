package com.luma.lumacourses.config;

import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Enrollment;
import com.luma.lumacourses.entity.Lesson;
import com.luma.lumacourses.entity.LessonProgress;
import com.luma.lumacourses.entity.Notification;
import com.luma.lumacourses.entity.RefreshToken;
import com.luma.lumacourses.entity.Review;
import com.luma.lumacourses.entity.TokenBlacklist;
import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.EnrollmentStatus;
import com.luma.lumacourses.util.enums.NotificationType;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.RefreshTokenRepository;
import com.luma.lumacourses.repository.TokenBlacklistRepository;
import com.luma.lumacourses.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@luma.com";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_FULL_NAME = "Admin Luma";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String SEED_PASSWORD = "seed12345";

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = findOrCreateUser(
                ADMIN_USERNAME,
                ADMIN_EMAIL,
                ADMIN_FULL_NAME,
                Role.ADMIN,
                true,
                ADMIN_PASSWORD);

        User teacherOne = findOrCreateUser(
                "teacher.alpha",
                "teacher.alpha@luma.com",
                "Teacher Alpha",
                Role.TEACHER,
                true,
                SEED_PASSWORD);

        User teacherTwo = findOrCreateUser(
                "teacher.beta",
                "teacher.beta@luma.com",
                "Teacher Beta",
                Role.TEACHER,
                true,
                SEED_PASSWORD);

        User studentOne = findOrCreateUser(
                "student.alpha",
                "student.alpha@luma.com",
                "Student Alpha",
                Role.STUDENT,
                true,
                SEED_PASSWORD);

        User studentTwo = findOrCreateUser(
                "student.beta",
                "student.beta@luma.com",
                "Student Beta",
                Role.STUDENT,
                true,
                SEED_PASSWORD);

        List<Course> courses = seedCourses(teacherOne, teacherTwo);
        List<Lesson> lessons = seedLessons(courses);
        List<Enrollment> enrollments = seedEnrollments(studentOne, studentTwo, courses);
        seedLessonProgress(enrollments, lessons);
        seedReviews(studentOne, studentTwo, courses);
        seedNotifications(admin, teacherOne, teacherTwo, studentOne, studentTwo);
        seedRefreshTokens(admin, teacherOne, teacherTwo, studentOne, studentTwo);
        seedTokenBlacklist();

        log.info("Seed completed. Admin account: {} / {}", admin.getEmail(), ADMIN_PASSWORD);
    }

    private User findOrCreateUser(String username,
                                  String email,
                                  String fullName,
                                  Role role,
                                  boolean active,
                                  String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setActive(active);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            User saved = userRepository.save(user);
            log.info("Seeded user: email={}, role={}", email, role);
            return saved;
        });
    }

    private List<Course> seedCourses(User teacherOne, User teacherTwo) {
        Course courseOne = findOrCreateCourse("Java Core Fundamentals",
                "Java basics for beginners", teacherOne, 199000, 12, CourseStatus.PUBLISHED);
        Course courseTwo = findOrCreateCourse("Spring Boot REST API",
                "Build RESTful services with Spring Boot", teacherOne, 299000, 16, CourseStatus.PUBLISHED);
        Course courseThree = findOrCreateCourse("Spring Security JWT",
                "Secure APIs with JWT and role-based access", teacherOne, 349000, 14, CourseStatus.DRAFT);
        Course courseFour = findOrCreateCourse("JPA & Hibernate Mastery",
                "Practical persistence with JPA and Hibernate", teacherTwo, 259000, 15, CourseStatus.PUBLISHED);
        Course courseFive = findOrCreateCourse("Clean Code in Java",
                "Refactoring and clean architecture practices", teacherTwo, 279000, 10, CourseStatus.ARCHIVED);
        return List.of(courseOne, courseTwo, courseThree, courseFour, courseFive);
    }

    private List<Lesson> seedLessons(List<Course> courses) {
        Lesson lessonOne = findOrCreateLesson(
                courses.get(0), "Lesson 1 - Java Variables", 1, true,
                "https://cdn.luma.local/lessons/java-variables",
                "Variables, primitive types, and operators.");
        Lesson lessonTwo = findOrCreateLesson(
                courses.get(1), "Lesson 1 - REST Architecture", 1, true,
                "https://cdn.luma.local/lessons/rest-architecture",
                "HTTP methods, status codes, and API resources.");
        Lesson lessonThree = findOrCreateLesson(
                courses.get(2), "Lesson 1 - JWT Basics", 1, true,
                "https://cdn.luma.local/lessons/jwt-basics",
                "Access token, refresh token, and claim design.");
        Lesson lessonFour = findOrCreateLesson(
                courses.get(3), "Lesson 1 - Entity Mapping", 1, true,
                "https://cdn.luma.local/lessons/entity-mapping",
                "Entity relationships and fetch strategy.");
        Lesson lessonFive = findOrCreateLesson(
                courses.get(4), "Lesson 1 - Naming & Structure", 1, false,
                "https://cdn.luma.local/lessons/naming-structure",
                "Naming conventions and package boundaries.");
        return List.of(lessonOne, lessonTwo, lessonThree, lessonFour, lessonFive);
    }

    private List<Enrollment> seedEnrollments(User studentOne, User studentTwo, List<Course> courses) {
        Enrollment enrollmentOne = findOrCreateEnrollment(studentOne, courses.get(0), EnrollmentStatus.ENROLLED, BigDecimal.valueOf(20));
        Enrollment enrollmentTwo = findOrCreateEnrollment(studentOne, courses.get(1), EnrollmentStatus.COMPLETED, BigDecimal.valueOf(100));
        Enrollment enrollmentThree = findOrCreateEnrollment(studentOne, courses.get(3), EnrollmentStatus.ENROLLED, BigDecimal.valueOf(40));
        Enrollment enrollmentFour = findOrCreateEnrollment(studentTwo, courses.get(0), EnrollmentStatus.DROPPED, BigDecimal.valueOf(15));
        Enrollment enrollmentFive = findOrCreateEnrollment(studentTwo, courses.get(2), EnrollmentStatus.ENROLLED, BigDecimal.valueOf(60));
        return List.of(enrollmentOne, enrollmentTwo, enrollmentThree, enrollmentFour, enrollmentFive);
    }

    private void seedLessonProgress(List<Enrollment> enrollments, List<Lesson> lessons) {
        findOrCreateLessonProgress(enrollments.get(0), lessons.get(0), false);
        findOrCreateLessonProgress(enrollments.get(1), lessons.get(1), true);
        findOrCreateLessonProgress(enrollments.get(2), lessons.get(3), false);
        findOrCreateLessonProgress(enrollments.get(3), lessons.get(0), false);
        findOrCreateLessonProgress(enrollments.get(4), lessons.get(2), true);
    }

    private void seedReviews(User studentOne, User studentTwo, List<Course> courses) {
        findOrCreateReview(courses.get(0), studentOne, 5, "Easy to follow and practical.");
        findOrCreateReview(courses.get(1), studentOne, 4, "Good examples for REST design.");
        findOrCreateReview(courses.get(3), studentOne, 4, "JPA mappings are explained clearly.");
        findOrCreateReview(courses.get(0), studentTwo, 3, "Helpful but I needed more exercises.");
        findOrCreateReview(courses.get(2), studentTwo, 5, "JWT flow became much clearer.");
    }

    private void seedNotifications(User admin,
                                   User teacherOne,
                                   User teacherTwo,
                                   User studentOne,
                                   User studentTwo) {
        findOrCreateNotification(admin, "System maintenance scheduled at 23:00.", NotificationType.GENERIC, "/admin/system");
        findOrCreateNotification(teacherOne, "Your course Java Core Fundamentals has a new enrollment.", NotificationType.ENROLLMENT_CONFIRMED, "/teacher/courses/1");
        findOrCreateNotification(teacherTwo, "A lesson was updated in JPA & Hibernate Mastery.", NotificationType.LESSON_UPDATED, "/teacher/courses/4/lessons");
        findOrCreateNotification(studentOne, "New course available: Spring Boot REST API.", NotificationType.NEW_COURSE, "/courses");
        findOrCreateNotification(studentTwo, "Enrollment confirmed for Spring Security JWT.", NotificationType.ENROLLMENT_CONFIRMED, "/my-courses");
    }

    private void seedRefreshTokens(User admin,
                                   User teacherOne,
                                   User teacherTwo,
                                   User studentOne,
                                   User studentTwo) {
        LocalDateTime now = LocalDateTime.now();
        findOrCreateRefreshToken(admin, "seed-jti-admin-01", "seed-refresh-token-admin-01", now.plusDays(7), false);
        findOrCreateRefreshToken(teacherOne, "seed-jti-teacher-alpha-01", "seed-refresh-token-teacher-alpha-01", now.plusDays(7), false);
        findOrCreateRefreshToken(teacherTwo, "seed-jti-teacher-beta-01", "seed-refresh-token-teacher-beta-01", now.plusDays(7), false);
        findOrCreateRefreshToken(studentOne, "seed-jti-student-alpha-01", "seed-refresh-token-student-alpha-01", now.plusDays(7), true);
        findOrCreateRefreshToken(studentTwo, "seed-jti-student-beta-01", "seed-refresh-token-student-beta-01", now.plusDays(7), false);
    }

    private void seedTokenBlacklist() {
        LocalDateTime now = LocalDateTime.now();
        findOrCreateBlacklistedToken("seed-blacklist-jti-01", now.plusDays(1));
        findOrCreateBlacklistedToken("seed-blacklist-jti-02", now.plusDays(2));
        findOrCreateBlacklistedToken("seed-blacklist-jti-03", now.plusDays(3));
        findOrCreateBlacklistedToken("seed-blacklist-jti-04", now.plusDays(4));
        findOrCreateBlacklistedToken("seed-blacklist-jti-05", now.plusDays(5));
    }

    private Course buildCourse(String title,
                               String description,
                               User teacher,
                               int price,
                               int durationHours,
                               CourseStatus status) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setTeacher(teacher);
        course.setPrice(BigDecimal.valueOf(price));
        course.setDurationHours(durationHours);
        course.setStatus(status);
        return course;
    }

    private Course findOrCreateCourse(String title,
                                      String description,
                                      User teacher,
                                      int price,
                                      int durationHours,
                                      CourseStatus status) {
        Optional<Course> existing = entityManager.createQuery(
                        "select c from Course c where c.title = :title", Course.class)
                .setParameter("title", title)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        Course course = buildCourse(title, description, teacher, price, durationHours, status);
        Course saved = courseRepository.save(course);
        log.info("Seeded course: title={}", title);
        return saved;
    }

    private Lesson findOrCreateLesson(Course course,
                                      String title,
                                      int orderIndex,
                                      boolean published,
                                      String contentUrl,
                                      String textContent) {
        Optional<Lesson> existing = entityManager.createQuery(
                        "select l from Lesson l where l.course = :course and l.orderIndex = :orderIndex", Lesson.class)
                .setParameter("course", course)
                .setParameter("orderIndex", orderIndex)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(title);
        lesson.setOrderIndex(orderIndex);
        lesson.setPublished(published);
        lesson.setContentUrl(contentUrl);
        lesson.setTextContent(textContent);
        entityManager.persist(lesson);
        log.info("Seeded lesson: courseId={}, orderIndex={}", course.getId(), orderIndex);
        return lesson;
    }

    private Enrollment findOrCreateEnrollment(User student,
                                              Course course,
                                              EnrollmentStatus status,
                                              BigDecimal progressPercentage) {
        Optional<Enrollment> existing = entityManager.createQuery(
                        "select e from Enrollment e where e.student = :student and e.course = :course", Enrollment.class)
                .setParameter("student", student)
                .setParameter("course", course)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(status);
        enrollment.setProgressPercentage(progressPercentage);
        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.setCompletionDate(LocalDateTime.now().minusDays(1));
        }
        entityManager.persist(enrollment);
        log.info("Seeded enrollment: studentId={}, courseId={}", student.getId(), course.getId());
        return enrollment;
    }

    private void findOrCreateLessonProgress(Enrollment enrollment, Lesson lesson, boolean completed) {
        boolean exists = !entityManager.createQuery(
                        "select p from LessonProgress p where p.enrollment = :enrollment and p.lesson = :lesson",
                        LessonProgress.class)
                .setParameter("enrollment", enrollment)
                .setParameter("lesson", lesson)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();

        if (exists) {
            return;
        }

        LessonProgress progress = new LessonProgress();
        progress.setEnrollment(enrollment);
        progress.setLesson(lesson);
        progress.setCompleted(completed);
        if (completed) {
            progress.setCompletedAt(LocalDateTime.now().minusHours(2));
        }
        entityManager.persist(progress);
        log.info("Seeded lesson progress: enrollmentId={}, lessonId={}", enrollment.getId(), lesson.getId());
    }

    private void findOrCreateReview(Course course, User student, int rating, String comment) {
        boolean exists = !entityManager.createQuery(
                        "select r from Review r where r.course = :course and r.student = :student",
                        Review.class)
                .setParameter("course", course)
                .setParameter("student", student)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();

        if (exists) {
            return;
        }

        Review review = new Review();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(rating);
        review.setComment(comment);
        entityManager.persist(review);
        log.info("Seeded review: courseId={}, studentId={}", course.getId(), student.getId());
    }

    private void findOrCreateNotification(User user, String message, NotificationType type, String targetUrl) {
        boolean exists = !entityManager.createQuery(
                        "select n from Notification n where n.user = :user and n.message = :message",
                        Notification.class)
                .setParameter("user", user)
                .setParameter("message", message)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();

        if (exists) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetUrl(targetUrl);
        notification.setRead(false);
        entityManager.persist(notification);
        log.info("Seeded notification: userId={}, type={}", user.getId(), type);
    }

    private void findOrCreateRefreshToken(User user,
                                          String jti,
                                          String token,
                                          LocalDateTime expiresAt,
                                          boolean revoked) {
        if (refreshTokenRepository.findByJti(jti).isPresent()) {
            return;
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setJti(jti);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(revoked);
        refreshTokenRepository.save(refreshToken);
        log.info("Seeded refresh token: jti={}", jti);
    }

    private void findOrCreateBlacklistedToken(String jti, LocalDateTime expiresAt) {
        if (tokenBlacklistRepository.existsByJti(jti)) {
            return;
        }

        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setJti(jti);
        tokenBlacklist.setExpiresAt(expiresAt);
        tokenBlacklistRepository.save(tokenBlacklist);
        log.info("Seeded blacklisted token: jti={}", jti);
    }
}
