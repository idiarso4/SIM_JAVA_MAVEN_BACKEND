-- Initial data for SIM application (Hibernate import.sql)
-- This runs after schema creation when hbm2ddl.auto=create/create-drop

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

-- Insert users (passwords are bcrypt-hashed: 'password')
INSERT INTO users (username, email, password, first_name, last_name, user_type, is_active, created_at, updated_at) VALUES
('admin', 'admin@sim.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYoHA8t0r5lDvC', 'System', 'Administrator', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('teacher1', 'teacher1@sim.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYoHA8t0r5lDvC', 'Budi', 'Santoso', 'TEACHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('teacher2', 'teacher2@sim.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYoHA8t0r5lDvC', 'Siti', 'Nurhaliza', 'TEACHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('teacher3', 'teacher3@sim.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYoHA8t0r5lDvC', 'Ahmad', 'Wijaya', 'TEACHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample students
INSERT INTO students (nis, nama_lengkap, class_room_id, jenis_kelamin, tahun_masuk, status, tempat_lahir, tanggal_lahir, agama, alamat, nama_ayah, nama_ibu, no_hp_ortu, asal_sekolah, created_at, updated_at) VALUES
('2024001', 'Ahmad Rizki Pratama', 1, 'LAKI_LAKI', 2024, 'ACTIVE', 'Jakarta', '2007-05-15', 'Islam', 'Jl. Merdeka No. 123, Jakarta', 'Budi Pratama', 'Siti Pratama', '081234567890', 'SMP Negeri 1 Jakarta', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024002', 'Sari Dewi Lestari', 1, 'PEREMPUAN', 2024, 'ACTIVE', 'Bandung', '2007-08-22', 'Islam', 'Jl. Sudirman No. 456, Bandung', 'Andi Lestari', 'Dewi Lestari', '081234567891', 'SMP Negeri 2 Bandung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024003', 'Budi Santoso', 2, 'LAKI_LAKI', 2024, 'ACTIVE', 'Surabaya', '2007-03-10', 'Kristen', 'Jl. Pahlawan No. 789, Surabaya', 'Joko Santoso', 'Maya Santoso', '081234567892', 'SMP Swasta Al-Azhar', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024004', 'Maya Sari', 2, 'PEREMPUAN', 2024, 'ACTIVE', 'Medan', '2007-11-05', 'Islam', 'Jl. Gatot Subroto No. 321, Medan', 'Rudi Sari', 'Rina Sari', '081234567893', 'SMP Muhammadiyah', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024005', 'Andi Wijaya', 3, 'LAKI_LAKI', 2024, 'ACTIVE', 'Makassar', '2007-07-18', 'Islam', 'Jl. Diponegoro No. 654, Makassar', 'Hendra Wijaya', 'Lina Wijaya', '081234567894', 'SMP Katolik', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024006', 'Rina Kartika', 4, 'PEREMPUAN', 2024, 'ACTIVE', 'Yogyakarta', '2007-12-25', 'Hindu', 'Jl. Malioboro No. 987, Yogyakarta', 'Agus Kartika', 'Fitri Kartika', '081234567895', 'SMP Kristen', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024007', 'Gilang Ramadhan', 5, 'LAKI_LAKI', 2024, 'ACTIVE', 'Semarang', '2007-04-30', 'Islam', 'Jl. Pemuda No. 147, Semarang', 'Eko Ramadhan', 'Sari Ramadhan', '081234567896', 'SMP Negeri 3 Semarang', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024008', 'Hani Putri', 6, 'PEREMPUAN', 2024, 'ACTIVE', 'Palembang', '2007-09-12', 'Islam', 'Jl. Sudirman No. 258, Palembang', 'Doni Putri', 'Vina Putri', '081234567897', 'SMP Negeri 1 Palembang', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024009', 'Indra Kusuma', 7, 'LAKI_LAKI', 2024, 'ACTIVE', 'Denpasar', '2007-06-08', 'Hindu', 'Jl. Gajah Mada No. 369, Denpasar', 'Wayan Kusuma', 'Kadek Kusuma', '081234567898', 'SMP Negeri 2 Denpasar', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2024010', 'Kiki Maharani', 8, 'PEREMPUAN', 2024, 'ACTIVE', 'Balikpapan', '2007-01-20', 'Kristen', 'Jl. Ahmad Yani No. 741, Balikpapan', 'Bayu Maharani', 'Cici Maharani', '081234567899', 'SMP Swasta Balikpapan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
