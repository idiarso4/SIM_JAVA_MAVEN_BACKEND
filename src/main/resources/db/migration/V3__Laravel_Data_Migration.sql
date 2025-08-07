-- Laravel to Spring Boot data migration script
-- This script handles data transformation and mapping from Laravel schema to Spring Boot schema

-- Create temporary tables for data validation and transformation
CREATE TABLE IF NOT EXISTS migration_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    records_processed INT DEFAULT 0,
    records_failed INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    status ENUM('STARTED', 'COMPLETED', 'FAILED') DEFAULT 'STARTED'
);

-- Create backup tables for rollback purposes
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users WHERE 1=0;
CREATE TABLE IF NOT EXISTS students_backup AS SELECT * FROM students WHERE 1=0;
CREATE TABLE IF NOT EXISTS class_rooms_backup AS SELECT * FROM class_rooms WHERE 1=0;

-- Data transformation procedures

-- Procedure to migrate user data with Laravel to Spring Boot field mapping
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS MigrateUserData()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_id BIGINT;
    DECLARE v_name VARCHAR(255);
    DECLARE v_email VARCHAR(255);
    DECLARE v_password VARCHAR(255);
    DECLARE v_user_type VARCHAR(50);
    DECLARE v_created_at TIMESTAMP;
    DECLARE v_updated_at TIMESTAMP;
    
    DECLARE user_cursor CURSOR FOR 
        SELECT id, name, email, password, 
               CASE 
                   WHEN role = 'admin' THEN 'ADMIN'
                   WHEN role = 'teacher' THEN 'TEACHER'
                   WHEN role = 'student' THEN 'STUDENT'
                   ELSE 'STUDENT'
               END as user_type,
               created_at, updated_at
        FROM users_laravel; -- Assuming Laravel table is renamed to users_laravel
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Log migration start
    INSERT INTO migration_log (table_name, operation) VALUES ('users', 'MIGRATE_FROM_LARAVEL');
    
    OPEN user_cursor;
    
    read_loop: LOOP
        FETCH user_cursor INTO v_id, v_name, v_email, v_password, v_user_type, v_created_at, v_updated_at;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Insert transformed data
        INSERT IGNORE INTO users (
            id, name, email, password, user_type, is_active, 
            created_at, updated_at
        ) VALUES (
            v_id, v_name, v_email, v_password, v_user_type, TRUE,
            v_created_at, v_updated_at
        );
        
    END LOOP;
    
    CLOSE user_cursor;
    
    -- Update migration log
    UPDATE migration_log 
    SET status = 'COMPLETED', 
        completed_at = CURRENT_TIMESTAMP,
        records_processed = (SELECT COUNT(*) FROM users)
    WHERE table_name = 'users' AND operation = 'MIGRATE_FROM_LARAVEL' 
    AND status = 'STARTED';
    
END//
DELIMITER ;

-- Procedure to migrate student data
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS MigrateStudentData()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_id BIGINT;
    DECLARE v_nis VARCHAR(20);
    DECLARE v_nama_lengkap VARCHAR(255);
    DECLARE v_class_room_id BIGINT;
    DECLARE v_status VARCHAR(50);
    DECLARE v_created_at TIMESTAMP;
    DECLARE v_updated_at TIMESTAMP;
    
    DECLARE student_cursor CURSOR FOR 
        SELECT id, nis, nama_lengkap, class_id as class_room_id,
               CASE 
                   WHEN status = 'active' THEN 'ACTIVE'
                   WHEN status = 'inactive' THEN 'INACTIVE'
                   WHEN status = 'graduated' THEN 'GRADUATED'
                   ELSE 'ACTIVE'
               END as status,
               created_at, updated_at
        FROM students_laravel; -- Assuming Laravel table is renamed
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Log migration start
    INSERT INTO migration_log (table_name, operation) VALUES ('students', 'MIGRATE_FROM_LARAVEL');
    
    OPEN student_cursor;
    
    read_loop: LOOP
        FETCH student_cursor INTO v_id, v_nis, v_nama_lengkap, v_class_room_id, v_status, v_created_at, v_updated_at;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Insert transformed data
        INSERT IGNORE INTO students (
            id, nis, nama_lengkap, class_room_id, status,
            created_at, updated_at
        ) VALUES (
            v_id, v_nis, v_nama_lengkap, v_class_room_id, v_status,
            v_created_at, v_updated_at
        );
        
    END LOOP;
    
    CLOSE student_cursor;
    
    -- Update migration log
    UPDATE migration_log 
    SET status = 'COMPLETED', 
        completed_at = CURRENT_TIMESTAMP,
        records_processed = (SELECT COUNT(*) FROM students)
    WHERE table_name = 'students' AND operation = 'MIGRATE_FROM_LARAVEL' 
    AND status = 'STARTED';
    
END//
DELIMITER ;

-- Data validation queries
-- These can be used to verify data integrity after migration

-- Check for orphaned records
CREATE VIEW IF NOT EXISTS v_orphaned_students AS
SELECT s.id, s.nis, s.nama_lengkap, s.class_room_id
FROM students s
LEFT JOIN class_rooms c ON s.class_room_id = c.id
WHERE s.class_room_id IS NOT NULL AND c.id IS NULL;

-- Check for duplicate NIS
CREATE VIEW IF NOT EXISTS v_duplicate_nis AS
SELECT nis, COUNT(*) as count
FROM students
GROUP BY nis
HAVING COUNT(*) > 1;

-- Check for duplicate emails
CREATE VIEW IF NOT EXISTS v_duplicate_emails AS
SELECT email, COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Migration rollback procedures
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS RollbackMigration(IN table_name VARCHAR(100))
BEGIN
    CASE table_name
        WHEN 'users' THEN
            DELETE FROM users;
            INSERT INTO users SELECT * FROM users_backup;
        WHEN 'students' THEN
            DELETE FROM students;
            INSERT INTO students SELECT * FROM students_backup;
        WHEN 'class_rooms' THEN
            DELETE FROM class_rooms;
            INSERT INTO class_rooms SELECT * FROM class_rooms_backup;
    END CASE;
    
    INSERT INTO migration_log (table_name, operation) 
    VALUES (table_name, 'ROLLBACK_COMPLETED');
END//
DELIMITER ;

-- Data integrity constraints
-- Add constraints that might be missing after migration

-- Ensure unique constraints
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE students ADD CONSTRAINT uk_students_nis UNIQUE (nis);

-- Add foreign key constraints if not exists
ALTER TABLE students 
ADD CONSTRAINT fk_students_class_room 
FOREIGN KEY (class_room_id) REFERENCES class_rooms(id) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE students 
ADD CONSTRAINT fk_students_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- Performance optimization after migration
-- Update table statistics
ANALYZE TABLE users;
ANALYZE TABLE students;
ANALYZE TABLE class_rooms;
ANALYZE TABLE majors;
ANALYZE TABLE departments;

-- Optimize tables
OPTIMIZE TABLE users;
OPTIMIZE TABLE students;
OPTIMIZE TABLE class_rooms;

-- Create migration completion marker
INSERT INTO migration_log (table_name, operation, status) 
VALUES ('MIGRATION_COMPLETE', 'LARAVEL_TO_SPRING_MIGRATION', 'COMPLETED');