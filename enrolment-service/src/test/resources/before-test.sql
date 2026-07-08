use db_student_profile;
-- Add 10 test students for the load test
DELETE FROM students WHERE programme = 'TEST';

INSERT INTO students (student_matric_no, full_name, email, programme, status)
VALUES
    ('B_LOADTEST0', 'Load Test 0', 'load0@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST1', 'Load Test 1', 'load1@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST2', 'Load Test 2', 'load2@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST3', 'Load Test 3', 'load3@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST4', 'Load Test 4', 'load4@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST5', 'Load Test 5', 'load5@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST6', 'Load Test 6', 'load6@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST7', 'Load Test 7', 'load7@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST8', 'Load Test 8', 'load8@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST9', 'Load Test 9', 'load9@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST10', 'Load Test 10', 'load10@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST11', 'Load Test 11', 'load11@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST12', 'Load Test 12', 'load12@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST13', 'Load Test 13', 'load13@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST14', 'Load Test 14', 'load14@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST15', 'Load Test 15', 'load15@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST16', 'Load Test 16', 'load16@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST17', 'Load Test 17', 'load17@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST18', 'Load Test 18', 'load18@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST19', 'Load Test 19', 'load19@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST20', 'Load Test 20', 'load20@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST21', 'Load Test 21', 'load21@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST22', 'Load Test 22', 'load22@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST23', 'Load Test 23', 'load23@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST24', 'Load Test 24', 'load24@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST25', 'Load Test 25', 'load25@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST26', 'Load Test 26', 'load26@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST27', 'Load Test 27', 'load27@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST28', 'Load Test 28', 'load28@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST29', 'Load Test 29', 'load29@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST30', 'Load Test 30', 'load30@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST31', 'Load Test 31', 'load31@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST32', 'Load Test 32', 'load32@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST33', 'Load Test 33', 'load33@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST34', 'Load Test 34', 'load34@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST35', 'Load Test 35', 'load35@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST36', 'Load Test 36', 'load36@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST37', 'Load Test 37', 'load37@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST38', 'Load Test 38', 'load38@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST39', 'Load Test 39', 'load39@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST40', 'Load Test 40', 'load40@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST41', 'Load Test 41', 'load41@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST42', 'Load Test 42', 'load42@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST43', 'Load Test 43', 'load43@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST44', 'Load Test 44', 'load44@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST45', 'Load Test 45', 'load45@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST46', 'Load Test 46', 'load46@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST47', 'Load Test 47', 'load47@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST48', 'Load Test 48', 'load48@test.com', 'TEST', 'ACTIVE'),
    ('B_LOADTEST49', 'Load Test 49', 'load49@test.com', 'TEST', 'ACTIVE');

use db_course_enrolment;

-- Clean up previous test enrolments for the LOADTEST course
DELETE FROM enrolments WHERE course_code = 'LOADTEST';

-- (If the course does not exist, create it)
UPDATE courses SET current_enrolled = 0, max_capacity = 25 WHERE course_code = 'LOADTEST';

INSERT INTO courses (course_code, course_name, max_capacity, current_enrolled)
SELECT 'LOADTEST', 'Load Test Course', 25, 0
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE course_code = 'LOADTEST');
