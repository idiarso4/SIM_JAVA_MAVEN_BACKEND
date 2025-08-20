-- Initial data for SIM application
-- This will be loaded after schema creation

-- Insert departments
INSERT INTO departments (name, code, description, is_active, created_at, updated_at) VALUES
('Ilmu Pengetahuan Alam', 'IPA', 'Departemen Ilmu Pengetahuan Alam', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ilmu Pengetahuan Sosial', 'IPS', 'Departemen Ilmu Pengetahuan Sosial', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bahasa dan Sastra', 'BAHASA', 'Departemen Bahasa dan Sastra', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert majors
INSERT INTO majors (name, code, description, department_id, is_active, created_at, updated_at) VALUES
('Ilmu Pengetahuan Alam', 'IPA', 'Jurusan IPA - Matematika, Fisika, Kimia, Biologi', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ilmu Pengetahuan Sosial', 'IPS', 'Jurusan IPS - Sejarah, Geografi, Ekonomi, Sosiologi', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert class rooms
INSERT INTO class_rooms (name, grade, capacity, academic_year, major_id, is_active, created_at, updated_at) VALUES
('10A', 10, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10B', 10, 30, '2024/2025', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10C', 10, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11A', 11, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11B', 11, 30, '2024/2025', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11C', 11, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('12A', 12, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('12B', 12, 30, '2024/2025', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('12C', 12, 30, '2024/2025', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert users
INSERT INTO users (username, email, password, first_name, last_name, user_type, is_active, created_at, updated_at) VALUES
('admin', 'admin@sim.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYoHA8t0r5lDvC', 'System', 'Administrator', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Teachers can be added through the application interface

-- No sample students - data will be added through the application interface