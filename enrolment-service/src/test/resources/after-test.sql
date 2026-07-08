--cleanup after
DELETE FROM enrolments WHERE course_code = 'LOADTEST';
use db_student_profile;
DELETE FROM students WHERE programme = 'TEST';