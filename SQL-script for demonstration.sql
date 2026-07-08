-- ==========================================
-- 1. STUDENT PROFILE SERVICE DATABASE
-- ==========================================
DROP DATABASE IF EXISTS db_student_profile;
CREATE DATABASE db_student_profile;
USE db_student_profile;

CREATE TABLE students
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    student_matric_no VARCHAR(20) UNIQUE  NOT NULL,
    full_name         VARCHAR(100)        NOT NULL,
    email             VARCHAR(100) UNIQUE NOT NULL,
    programme         VARCHAR(50)         NOT NULL,
    status            VARCHAR(20) DEFAULT 'ACTIVE' -- INACTIVE, GRADUATED
);

CREATE TABLE academic_records
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_matric_no VARCHAR(255) NOT NULL,
    course_code       VARCHAR(255) NOT NULL,
    semester          VARCHAR(50)  NOT NULL,
    midterm_marks     DOUBLE DEFAULT 0.0,
    assessment_marks  DOUBLE DEFAULT 0.0,
    lab_test_marks    DOUBLE DEFAULT 0.0,
    final_exam_marks  DOUBLE DEFAULT 0.0,
    total_marks       DOUBLE,
    grade             VARCHAR(2),

    CONSTRAINT uk_student_course_semester UNIQUE (student_matric_no, course_code, semester)
);

-- ==========================================
-- 2. COURSE ENROLMENT SERVICE DATABASE
-- ==========================================
DROP DATABASE IF EXISTS db_course_enrolment;
CREATE DATABASE db_course_enrolment;
USE db_course_enrolment;

CREATE TABLE courses
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    course_code      VARCHAR(20) UNIQUE NOT NULL,
    course_name      VARCHAR(100)       NOT NULL,
    max_capacity     INT                NOT NULL,
    current_enrolled INT DEFAULT 0
);

CREATE TABLE enrolments
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    student_matric_no VARCHAR(20) NOT NULL,
    course_code       VARCHAR(20) NOT NULL,
    semester          VARCHAR(20) NOT NULL,
    status            VARCHAR(20) DEFAULT 'ENROLLED',
    enrolment_date    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. NOTIFICATION SERVICE DATABASE
-- ==========================================
DROP DATABASE IF EXISTS db_notification;
CREATE DATABASE db_notification;
USE db_notification;

CREATE TABLE notification_logs
(
    id              INT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(100) NOT NULL,
    event_type      VARCHAR(50)  NOT NULL,
    message_body    TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 4. LIBRARY / BOOKING SERVICE DATABASE
-- ==========================================
DROP DATABASE IF EXISTS db_library_booking;
CREATE DATABASE db_library_booking;
USE db_library_booking;

CREATE TABLE books
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    isbn             VARCHAR(20) UNIQUE NOT NULL,
    title            VARCHAR(200)       NOT NULL,
    available_copies INT DEFAULT 1
);

CREATE TABLE book_loans
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    student_matric_no VARCHAR(20) NOT NULL,
    isbn              VARCHAR(20) NOT NULL,
    loan_date         DATE        NOT NULL,
    due_date          DATE        NOT NULL,
    status            VARCHAR(20) DEFAULT 'BORROWED' -- RETURNED, OVERDUE
);

CREATE TABLE room_reservations
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    room_id           VARCHAR(20) NOT NULL,
    student_matric_no VARCHAR(20) NOT NULL,
    start_time        TIMESTAMP   NOT NULL,
    end_time          TIMESTAMP   NOT NULL
);

CREATE TABLE room
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    room_id           VARCHAR(20) UNIQUE NOT NULL,
    room_type         VARCHAR(50) NOT NULL
);
-- ==========================================
-- 5. REPORTING / ANALYTICS SERVICE DATABASE

-- ==========================================
DROP DATABASE IF EXISTS db_reporting;
CREATE DATABASE db_reporting;
USE db_reporting;

CREATE TABLE aggregated_metrics
(
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    metric_name        VARCHAR(100) UNIQUE NOT NULL,
    metric_value       INT       DEFAULT 0,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- ==========================================
-- 1. POPULATE STUDENT PROFILE
-- ==========================================
USE db_student_profile;
INSERT INTO students (student_matric_no, full_name, email, programme, status)
VALUES ('B032110015', 'Ahmad Faiz bin Razali', 'faiz@student.edu.my', 'BIT', 'ACTIVE'),
       ('B032110016', 'Tan Mei Ling', 'meiling@student.edu.my', 'BSE', 'ACTIVE'),
       ('B032410569', 'MPP', 'pp@student.edu.my', 'BITS', 'ACTIVE'),
       ('B032110017', 'Siti Nurhaliza', 'siti@student.edu.my', 'BIT', 'GRADUATED');

INSERT INTO academic_records (student_matric_no, course_code, semester, midterm_marks, assessment_marks, lab_test_marks,
                              final_exam_marks,
                              total_marks, grade)
VALUES ('B032110015', 'SEC3133', '2023/2024-1', 15.5, 20.0, 18.0, 30.5, 84.0, 'A'),
       ('B032110015', 'BIT2043', '2023/2024-1', 12.0, 15.0, 15.0, 30.0, 72.0, 'B'),
       ('B032110088', 'SEC3133', '2023/2024-1', 10.0, 12.0, 10.0, 20.0, 52.0, 'D');

-- ==========================================
-- 2. POPULATE COURSE ENROLMENT
-- ==========================================
USE db_course_enrolment;
INSERT INTO courses (course_code, course_name, max_capacity, current_enrolled)
VALUES ('SEC3133', 'Distributed Systems', 50, 2),
       ('SEC3143', 'Mobile Application Development', 40, 1),
       ('BITP2123', 'Database', 40, 1),
       ('BB6969', 'TEST', 40, 1),
       ('SEC4113', 'Software Architecture', 30, 0);

INSERT INTO enrolments (student_matric_no, course_code, semester, status)
VALUES ('B032110015', 'SEC3133', '2023/2024-1', 'ENROLLED'),
       ('B032110016', 'SEC3133', '2023/2024-1', 'ENROLLED'),
       ('B032410569', 'BITP2123', '2025/2026-2', 'ENROLLED'),
       ('B032110015', 'SEC3143', '2023/2024-1', 'ENROLLED');

-- ==========================================
-- 3. POPULATE NOTIFICATIONS
-- ==========================================
USE db_notification;
INSERT INTO notification_logs (recipient_email, event_type, message_body, status)
VALUES ('faiz@student.edu.my', 'ENROLMENT_SUCCESS', 'Successfully enrolled in SEC3133.', 'SENT'),
       ('meiling@student.edu.my', 'ENROLMENT_SUCCESS', 'Successfully enrolled in SEC3133.', 'SENT');

-- ==========================================
-- 4. POPULATE LIBRARY / BOOKING
-- ==========================================
USE db_library_booking;
INSERT INTO books (isbn, title, available_copies)
VALUES ('978-0134685991', 'Effective Java', 3),
       ('978-1492031081', 'Designing Data-Intensive Applications', 1);

INSERT INTO book_loans (student_matric_no, isbn, loan_date, due_date, status)
VALUES ('B032110015', '978-0134685991', '2026-06-20', '2026-07-04', 'BORROWED');

INSERT INTO room (room_id, room_type)
VALUES ('DR001', 'Discussion Room'),
       ('DR002', 'Discussion Room'),
       ('CR001', 'Computer Room'),
       ('CR002', 'Computer Room'),
       ('MR001', 'Meeting Room');

INSERT INTO room_reservations (room_id, student_matric_no, start_time, end_time)
VALUES ('DR001', 'B03211001\5', '2026-06-25 10:00:00', '2026-06-25 12:00:00');

-- ==========================================
-- 5. POPULATE REPORTING
-- ==========================================
USE db_reporting;
INSERT INTO aggregated_metrics (metric_name, metric_value)
VALUES ('TOTAL_ACTIVE_STUDENTS', 2),
       ('TOTAL_ENROLMENTS_SEM1_2026', 3),
       ('TOTAL_OVERDUE_BOOKS', 0);