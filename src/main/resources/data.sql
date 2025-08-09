-- Initial data for SIM application
-- This file will be executed after schema creation

-- Insert default roles (using INSERT with ON DUPLICATE KEY for H2)
INSERT INTO roles (name, description, is_system_role, created_at, updated_at) VALUES 
('ADMIN', 'System Administrator', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TEACHER', 'Teacher', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('STUDENT', 'Student', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert default permissions
INSERT INTO permissions (name, description, created_at, updated_at) VALUES 
('VIEW_STUDENTS', 'View student information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGE_STUDENTS', 'Create, update, delete students', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIEW_USERS', 'View user information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGE_USERS', 'Create, update, delete users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIEW_GRADES', 'View grades and assessments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGE_GRADES', 'Create, update, delete grades', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIEW_REPORTS', 'View reports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GENERATE_REPORTS', 'Generate and export reports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGE_CLASSES', 'Manage class schedules and assignments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIEW_ATTENDANCE', 'View attendance records', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGE_ATTENDANCE', 'Manage attendance records', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign permissions to roles
-- Admin gets all permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- Teacher gets limited permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'TEACHER' AND p.name IN ('VIEW_STUDENTS', 'VIEW_GRADES', 'MANAGE_GRADES', 'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 'VIEW_REPORTS');

-- Student gets very limited permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'STUDENT' AND p.name IN ('VIEW_GRADES');

-- Insert default users with BCrypt hash for 'admin123' (for testing)
-- Admin user (password: admin123) - Hash generated with BCrypt strength 10
INSERT INTO users (username, email, first_name, last_name, password, user_type, is_active, email_verified_at, created_at, updated_at) VALUES 
('admin', 'admin@sim.edu', 'System', 'Administrator', '$2a$10$dfkaus4NbeoGMcnW5EpTduBHzIME2OlcP2eVuLfQX7.w9lj9a1MGq', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Teacher user (password: teacher123) - Hash generated with BCrypt strength 10  
INSERT INTO users (username, email, first_name, last_name, password, user_type, nip, is_active, email_verified_at, created_at, updated_at) VALUES 
('teacher', 'teacher@sim.edu', 'Test', 'Teacher', '$2a$10$dfkaus4NbeoGMcnW5EpTduBHzIME2OlcP2eVuLfQX7.w9lj9a1MGq', 'TEACHER', 'T001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'teacher' AND r.name = 'TEACHER';

-- Insert sample students for dashboard testing
INSERT INTO students (nis, nama_lengkap, tempat_lahir, tanggal_lahir, jenis_kelamin, agama, alamat, nama_ayah, nama_ibu, pekerjaan_ayah, pekerjaan_ibu, no_hp_ortu, alamat_ortu, tahun_masuk, asal_sekolah, status, created_at, updated_at) VALUES 
('2024001', 'Ahmad Rizki Pratama', 'Jakarta', '2006-03-15', 'MALE', 'Islam', 'Jl. Merdeka No. 123, Jakarta', 'Budi Pratama', 'Siti Nurhaliza', 'Pegawai Swasta', 'Ibu Rumah Tangga', '081234567890', 'Jl. Merdeka No. 123, Jakarta', 2024, 'SMP Negeri 1 Jakarta', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024002', 'Sari Dewi Lestari', 'Bandung', '2006-07-22', 'FEMALE', 'Islam', 'Jl. Sudirman No. 456, Bandung', 'Andi Lestari', 'Dewi Sartika', 'Guru', 'Pegawai Bank', '081234567891', 'Jl. Sudirman No. 456, Bandung', 2024, 'SMP Negeri 2 Bandung', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024003', 'Muhammad Fajar Sidiq', 'Surabaya', '2006-01-10', 'MALE', 'Islam', 'Jl. Pemuda No. 789, Surabaya', 'Hasan Sidiq', 'Fatimah Zahra', 'Wiraswasta', 'Guru', '081234567892', 'Jl. Pemuda No. 789, Surabaya', 2024, 'SMP Negeri 3 Surabaya', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024004', 'Indira Putri Maharani', 'Yogyakarta', '2006-05-18', 'FEMALE', 'Hindu', 'Jl. Malioboro No. 321, Yogyakarta', 'I Made Maharani', 'Ni Kadek Sari', 'Pegawai Negeri', 'Dokter', '081234567893', 'Jl. Malioboro No. 321, Yogyakarta', 2024, 'SMP Negeri 1 Yogyakarta', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024005', 'Kevin Alexander Wijaya', 'Medan', '2006-09-03', 'MALE', 'Kristen', 'Jl. Asia No. 654, Medan', 'Alexander Wijaya', 'Maria Susanti', 'Pengusaha', 'Akuntan', '081234567894', 'Jl. Asia No. 654, Medan', 2024, 'SMP Swasta Medan', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2023001', 'Rina Sari Dewi', 'Semarang', '2005-11-12', 'FEMALE', 'Islam', 'Jl. Pandanaran No. 111, Semarang', 'Bambang Dewi', 'Sari Wulandari', 'Pegawai Swasta', 'Guru', '081234567895', 'Jl. Pandanaran No. 111, Semarang', 2023, 'SMP Negeri 5 Semarang', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2023002', 'Dimas Arya Pratama', 'Makassar', '2005-04-25', 'MALE', 'Islam', 'Jl. Veteran No. 222, Makassar', 'Arya Gunawan', 'Lestari Pratama', 'TNI', 'Perawat', '081234567896', 'Jl. Veteran No. 222, Makassar', 2023, 'SMP Negeri 2 Makassar', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2023003', 'Putri Ayu Lestari', 'Palembang', '2005-08-14', 'FEMALE', 'Islam', 'Jl. Sudirman No. 333, Palembang', 'Lestari Budi', 'Ayu Sari', 'Polisi', 'Bidan', '081234567897', 'Jl. Sudirman No. 333, Palembang', 2023, 'SMP Negeri 1 Palembang', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2022001', 'Bayu Adi Nugroho', 'Solo', '2004-12-08', 'MALE', 'Islam', 'Jl. Slamet Riyadi No. 444, Solo', 'Adi Nugroho', 'Siti Bayu', 'Pegawai Bank', 'Guru', '081234567898', 'Jl. Slamet Riyadi No. 444, Solo', 2022, 'SMP Negeri 3 Solo', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2022002', 'Citra Dewi Anggraini', 'Malang', '2004-06-30', 'FEMALE', 'Kristen', 'Jl. Ijen No. 555, Malang', 'Dewi Santoso', 'Anggraini Putri', 'Dosen', 'Dokter', '081234567899', 'Jl. Ijen No. 555, Malang', 2022, 'SMP Negeri 1 Malang', 'GRADUATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);