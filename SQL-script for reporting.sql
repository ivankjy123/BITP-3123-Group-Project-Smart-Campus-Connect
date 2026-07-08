-- ==========================================
-- 0. DATABASE & TABLE CREATION
-- ==========================================
DROP DATABASE IF EXISTS db_student_profile;
CREATE DATABASE db_student_profile;
USE db_student_profile;

CREATE TABLE students (
                          id                INT AUTO_INCREMENT PRIMARY KEY,
                          student_matric_no VARCHAR(20) UNIQUE  NOT NULL,
                          full_name         VARCHAR(100)        NOT NULL,
                          email             VARCHAR(100) UNIQUE NOT NULL,
                          programme         VARCHAR(50)         NOT NULL,
                          status            VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE academic_records (
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

DROP DATABASE IF EXISTS db_course_enrolment;
CREATE DATABASE db_course_enrolment;
USE db_course_enrolment;

CREATE TABLE courses (
                         id               INT AUTO_INCREMENT PRIMARY KEY,
                         course_code      VARCHAR(20) UNIQUE NOT NULL,
                         course_name      VARCHAR(100)       NOT NULL,
                         max_capacity     INT                NOT NULL,
                         current_enrolled INT DEFAULT 0
);

CREATE TABLE enrolments (
                            id                INT AUTO_INCREMENT PRIMARY KEY,
                            student_matric_no VARCHAR(20) NOT NULL,
                            course_code       VARCHAR(20) NOT NULL,
                            semester          VARCHAR(20) NOT NULL,
                            status            VARCHAR(20) DEFAULT 'ENROLLED',
                            enrolment_date    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

DROP DATABASE IF EXISTS db_notification;
CREATE DATABASE db_notification;
USE db_notification;

CREATE TABLE notification_logs (
                                   id              INT AUTO_INCREMENT PRIMARY KEY,
                                   recipient_email VARCHAR(100) NOT NULL,
                                   event_type      VARCHAR(50)  NOT NULL,
                                   message_body    TEXT         NOT NULL,
                                   status          VARCHAR(20)  NOT NULL,
                                   created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP DATABASE IF EXISTS db_library_booking;
CREATE DATABASE db_library_booking;
USE db_library_booking;

CREATE TABLE room (
                      id        INT AUTO_INCREMENT PRIMARY KEY,
                      room_id   VARCHAR(20) UNIQUE NOT NULL,
                      room_type VARCHAR(50) NOT NULL
);

CREATE TABLE books (
                       id               INT AUTO_INCREMENT PRIMARY KEY,
                       isbn             VARCHAR(20) UNIQUE NOT NULL,
                       title            VARCHAR(200)       NOT NULL,
                       available_copies INT DEFAULT 1
);

CREATE TABLE book_loans (
                            id                INT AUTO_INCREMENT PRIMARY KEY,
                            student_matric_no VARCHAR(20) NOT NULL,
                            isbn              VARCHAR(20) NOT NULL,
                            loan_date         DATE        NOT NULL,
                            due_date          DATE        NOT NULL,
                            status            VARCHAR(20) DEFAULT 'BORROWED'
);

CREATE TABLE room_reservations (
                                   id                INT AUTO_INCREMENT PRIMARY KEY,
                                   room_id           VARCHAR(20) NOT NULL,
                                   student_matric_no VARCHAR(20) NOT NULL,
                                   start_time        TIMESTAMP   NOT NULL,
                                   end_time          TIMESTAMP   NOT NULL
);

DROP DATABASE IF EXISTS db_reporting;
CREATE DATABASE db_reporting;
USE db_reporting;

CREATE TABLE aggregated_metrics (
                                    id                 INT AUTO_INCREMENT PRIMARY KEY,
                                    metric_name        VARCHAR(100) UNIQUE NOT NULL,
                                    metric_value       INT       DEFAULT 0,
                                    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 1. MOCK DATA GENERATION PROCEDURE
-- ==========================================
DELIMITER $$
CREATE PROCEDURE generate_mock_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 0;
    DECLARE v_matric VARCHAR(20);
    DECLARE v_name VARCHAR(100);
    DECLARE v_email VARCHAR(100);
    DECLARE v_prog VARCHAR(10);
    DECLARE v_status VARCHAR(20);
    DECLARE v_course_code VARCHAR(20);
    DECLARE v_sem VARCHAR(50);
    DECLARE v_cnt INT;
    DECLARE v_inner INT DEFAULT 0;
    DECLARE v_mid DOUBLE;
    DECLARE v_assess DOUBLE;
    DECLARE v_lab DOUBLE;
    DECLARE v_final DOUBLE;
    DECLARE v_total DOUBLE;
    DECLARE v_grade VARCHAR(2);

    -- Name components
    DECLARE first_names VARCHAR(1000) DEFAULT 'Ahmad,Siti,Muhammad,Nur,Ali,Fatimah,Wei,Mei,Raj,Kavitha,Hakim,Aisyah,Zain,Lina,Hassan,Amira,Chong,Wong,Gan,Siva,Farid,Aziz,Ling,Yusof,Suresh,Prakash,Devi,Min,Jia,Hui';
    DECLARE last_names VARCHAR(1000)  DEFAULT 'Abdullah,Tan,Lim,Subramaniam,Ibrahim,Yap,Kumar,Chong,Lee,Ramasamy,Ong,Samad,Pillai,Rajendran,Hussein,Teo,Khoo,Muthu,Govind,Suppiah,Singh,Kaur,Hamid,Ismail,Johan,Bakar,Salleh,Rahman,Yusof,Karim';

    -- Programme list and counts for courses
    DECLARE prog_list VARCHAR(200) DEFAULT 'BITC,BITZ,BITS,BITM,BAXZ,BAXI,BERL,BERG,BEKE,BEKP,BEKC,BEKM,BTEC,BTMM';
    DECLARE count_list VARCHAR(100) DEFAULT '15,15,15,15,15,15,8,8,8,8,8,8,6,6';
    DECLARE prog_idx INT DEFAULT 0;
    DECLARE v_prog_prefix VARCHAR(10);
    DECLARE v_course_count INT;
    DECLARE v_course_num INT;

    -- Cursor for students
    DECLARE student_cursor CURSOR FOR SELECT student_matric_no FROM db_student_profile.students ORDER BY id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET @finished = 1;

    -- ======== 1.1 Students (1000) ========
    SET i = 1;
    WHILE i <= 1000 DO
            SET v_matric = CONCAT('B0324', LPAD(i, 5, '0'));
            SET v_name = CONCAT(
                    SUBSTRING_INDEX(SUBSTRING_INDEX(first_names, ',', 1 + FLOOR(RAND() * 30)), ',', -1),
                    ' ',
                    SUBSTRING_INDEX(SUBSTRING_INDEX(last_names, ',', 1 + FLOOR(RAND() * 30)), ',', -1)
                         );
            SET v_email = CONCAT(v_matric, '@student.university.edu.my');
            SET v_prog = SUBSTRING_INDEX(SUBSTRING_INDEX(prog_list, ',', 1 + FLOOR(RAND() * 14)), ',', -1);
            SET v_status = CASE
                               WHEN RAND() < 0.05 THEN 'INACTIVE'
                               WHEN RAND() < 0.05 THEN 'GRADUATED'
                               ELSE 'ACTIVE'
                END;
            INSERT INTO db_student_profile.students (student_matric_no, full_name, email, programme, status)
            VALUES (v_matric, v_name, v_email, v_prog, v_status);
            SET i = i + 1;
        END WHILE;

    -- ======== 1.2 Courses (150) ========
    SET prog_idx = 0;
    WHILE prog_idx < 14 DO
            SET v_prog_prefix = SUBSTRING_INDEX(SUBSTRING_INDEX(prog_list, ',', prog_idx + 1), ',', -1);
            SET v_course_count = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(count_list, ',', prog_idx + 1), ',', -1) AS UNSIGNED);
            SET v_course_num = 1001;
            WHILE v_course_num < 1001 + v_course_count DO
                    SET v_course_code = CONCAT(v_prog_prefix, v_course_num);
                    INSERT INTO db_course_enrolment.courses (course_code, course_name, max_capacity)
                    VALUES (
                               v_course_code,
                               CONCAT(
                                       ELT(1 + FLOOR(RAND() * 10), 'Introduction to','Advanced','Principles of','Fundamentals of','Applied','Modern','Theoretical','Computational','Engineering','Systems in'),
                                       ' ',
                                       ELT(1 + FLOOR(RAND() * 10), 'Programming','Mathematics','Physics','Data Structures','Electronics','Thermodynamics','Manufacturing','Economics','Artificial Intelligence','Design')
                               ),
                               30 + FLOOR(RAND() * 71)   -- 30..100
                           );
                    SET v_course_num = v_course_num + 1;
                END WHILE;
            SET prog_idx = prog_idx + 1;
        END WHILE;

    -- ======== 1.3 Enrolments & Academic Records ========
    OPEN student_cursor;
    SET @finished = 0;
    student_loop: LOOP
        FETCH student_cursor INTO v_matric;
        IF @finished = 1 THEN
            LEAVE student_loop;
        END IF;

        SET v_cnt = 1 + FLOOR(RAND() * 5);   -- 1..5 enrolments
        SET v_inner = 0;
        WHILE v_inner < v_cnt DO
                -- pick a random course
                SELECT course_code INTO v_course_code
                FROM db_course_enrolment.courses
                ORDER BY RAND() LIMIT 1;

                -- random semester
                SET v_sem = ELT(1 + FLOOR(RAND() * 4), 'Sem 1 2023/2024', 'Sem 2 2023/2024', 'Sem 1 2024/2025', 'Sem 2 2024/2025');

                -- avoid duplicates
                IF NOT EXISTS (
                    SELECT 1 FROM db_course_enrolment.enrolments
                    WHERE student_matric_no = v_matric AND course_code = v_course_code AND semester = v_sem
                ) THEN
                    INSERT INTO db_course_enrolment.enrolments (student_matric_no, course_code, semester, status)
                    VALUES (v_matric, v_course_code, v_sem, 'ENROLLED');

                    -- academic record with random marks
                    SET v_mid = ROUND(RAND() * 20, 1);
                    SET v_assess = ROUND(RAND() * 20, 1);
                    SET v_lab = ROUND(RAND() * 20, 1);
                    SET v_final = ROUND(RAND() * 40, 1);
                    SET v_total = v_mid + v_assess + v_lab + v_final;
                    SET v_grade = CASE
                                      WHEN v_total >= 80 THEN 'A'
                                      WHEN v_total >= 65 THEN 'B'
                                      WHEN v_total >= 50 THEN 'C'
                                      WHEN v_total >= 40 THEN 'D'
                                      ELSE 'F'
                        END;

                    INSERT INTO db_student_profile.academic_records
                    (student_matric_no, course_code, semester,
                     midterm_marks, assessment_marks, lab_test_marks,
                     final_exam_marks, total_marks, grade)
                    VALUES (v_matric, v_course_code, v_sem,
                            v_mid, v_assess, v_lab, v_final, v_total, v_grade);
                END IF;
                SET v_inner = v_inner + 1;
            END WHILE;
    END LOOP;
    CLOSE student_cursor;

    -- ======== 1.4 Notifications (300) ========
    INSERT INTO db_notification.notification_logs (recipient_email, event_type, message_body, status, created_at)
    SELECT
        s.email,
        ELT(1 + FLOOR(RAND() * 3), 'ENROLMENT', 'PROFILE_UPDATE', 'COURSE_DROP'),
        CONCAT('Notification for ', s.full_name, ': ',
               ELT(1 + FLOOR(RAND() * 3),
                   'Your enrolment has been confirmed.',
                   'Your profile information was updated.',
                   'You have successfully dropped a course.')),
        'SENT',
        TIMESTAMPADD(SECOND, FLOOR(RAND() * 31536000), '2023-01-01 00:00:00')
    FROM db_student_profile.students s
    ORDER BY RAND()
    LIMIT 300;

    -- ======== 1.5 Rooms (50) ========
    SET i = 1;
    WHILE i <= 50 DO
            INSERT INTO db_library_booking.room (room_id, room_type)
            VALUES (
                       CONCAT('R', LPAD(i, 3, '0')),
                       ELT(1 + FLOOR(RAND() * 4), 'Meeting Room', 'Study Room', 'Lecture Hall', 'Discussion Pod')
                   );
            SET i = i + 1;
        END WHILE;

    -- ======== 1.6 Books (700) ========
    SET i = 1;
    WHILE i <= 700 DO
            INSERT INTO db_library_booking.books (isbn, title, available_copies)
            VALUES (
                       CONCAT('978-0-', LPAD(FLOOR(RAND() * 1000), 3, '0'), '-',
                              LPAD(FLOOR(RAND() * 100000), 5, '0'), '-', FLOOR(RAND() * 10)),
                       CONCAT('Book Title ', i),
                       1 + FLOOR(RAND() * 5)
                   );
            SET i = i + 1;
        END WHILE;

    -- ======== 1.7 Book Loans (520) ========
    INSERT INTO db_library_booking.book_loans (student_matric_no, isbn, loan_date, due_date, status)
    SELECT
        s.student_matric_no,
        b.isbn,
        DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 700) DAY),
        DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 700) DAY) + INTERVAL 14 DAY,
        ELT(1 + FLOOR(RAND() * 3), 'BORROWED', 'RETURNED', 'OVERDUE')
    FROM db_student_profile.students s
             CROSS JOIN db_library_booking.books b
    ORDER BY RAND()
    LIMIT 520;

    -- ======== 1.8 Room Reservations (330) ========
    INSERT INTO db_library_booking.room_reservations (room_id, student_matric_no, start_time, end_time)
    SELECT
        r.room_id,
        s.student_matric_no,
        TIMESTAMPADD(SECOND, FLOOR(RAND() * 31536000), '2023-06-01 08:00:00'),
        TIMESTAMPADD(SECOND, FLOOR(RAND() * 31536000), '2023-06-01 08:00:00') + INTERVAL (1 + FLOOR(RAND() * 3)) HOUR
    FROM db_library_booking.room r
             CROSS JOIN db_student_profile.students s
    ORDER BY RAND()
    LIMIT 330;

    -- ======== 1.9 Reporting Aggregation ========
    INSERT INTO db_reporting.aggregated_metrics (metric_name, metric_value)
    SELECT 'TOTAL_STUDENTS', COUNT(*) FROM db_student_profile.students
    UNION ALL
    SELECT 'TOTAL_COURSES', COUNT(*) FROM db_course_enrolment.courses
    UNION ALL
    SELECT 'TOTAL_ENROLMENTS', COUNT(*) FROM db_course_enrolment.enrolments
    UNION ALL
    SELECT 'TOTAL_ACADEMIC_RECORDS', COUNT(*) FROM db_student_profile.academic_records
    UNION ALL
    SELECT 'TOTAL_NOTIFICATIONS', COUNT(*) FROM db_notification.notification_logs
    UNION ALL
    SELECT 'TOTAL_BOOKS', COUNT(*) FROM db_library_booking.books
    UNION ALL
    SELECT 'TOTAL_BOOK_LOANS', COUNT(*) FROM db_library_booking.book_loans
    UNION ALL
    SELECT 'TOTAL_ROOMS', COUNT(*) FROM db_library_booking.room
    UNION ALL
    SELECT 'TOTAL_ROOM_RESERVATIONS', COUNT(*) FROM db_library_booking.room_reservations;

END$$
DELIMITER ;

-- ==========================================
-- 2. EXECUTE & CLEAN UP
-- ==========================================
CALL generate_mock_data();
DROP PROCEDURE IF EXISTS generate_mock_data;