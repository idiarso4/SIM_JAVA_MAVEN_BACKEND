-- Performance optimization indexes for School Information Management System
-- These indexes are designed to optimize the most common query patterns

-- User table indexes
CREATE INDEX IF NOT EXISTS idx_user_email_type ON users(email, user_type);
CREATE INDEX IF NOT EXISTS idx_user_nip ON users(nip);
CREATE INDEX IF NOT EXISTS idx_user_active_type ON users(is_active, user_type);
CREATE INDEX IF NOT EXISTS idx_user_last_login ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at);

-- Student table indexes
CREATE INDEX IF NOT EXISTS idx_student_nis ON students(nis);
CREATE INDEX IF NOT EXISTS idx_student_class_status ON students(class_room_id, status);
CREATE INDEX IF NOT EXISTS idx_student_status ON students(status);
CREATE INDEX IF NOT EXISTS idx_student_tahun_masuk ON students(tahun_masuk);
CREATE INDEX IF NOT EXISTS idx_student_nama_lengkap ON students(nama_lengkap);
CREATE INDEX IF NOT EXISTS idx_student_user_id ON students(user_id);

-- ClassRoom table indexes
CREATE INDEX IF NOT EXISTS idx_classroom_major_grade ON class_rooms(major_id, grade);
CREATE INDEX IF NOT EXISTS idx_classroom_academic_year ON class_rooms(academic_year);
CREATE INDEX IF NOT EXISTS idx_classroom_homeroom_teacher ON class_rooms(homeroom_teacher_id);
CREATE INDEX IF NOT EXISTS idx_classroom_active ON class_rooms(is_active);
CREATE INDEX IF NOT EXISTS idx_classroom_name ON class_rooms(name);

-- Major table indexes
CREATE INDEX IF NOT EXISTS idx_major_department ON majors(department_id);
CREATE INDEX IF NOT EXISTS idx_major_code ON majors(code);
CREATE INDEX IF NOT EXISTS idx_major_active ON majors(is_active);

-- Department table indexes
CREATE INDEX IF NOT EXISTS idx_department_code ON departments(code);
CREATE INDEX IF NOT EXISTS idx_department_active ON departments(is_active);

-- Subject table indexes
CREATE INDEX IF NOT EXISTS idx_subject_kode_mapel ON subjects(kode_mapel);
CREATE INDEX IF NOT EXISTS idx_subject_active ON subjects(is_active);

-- Schedule table indexes
CREATE INDEX IF NOT EXISTS idx_schedule_classroom_day ON schedules(class_room_id, day_of_week);
CREATE INDEX IF NOT EXISTS idx_schedule_teacher_day ON schedules(teacher_id, day_of_week);
CREATE INDEX IF NOT EXISTS idx_schedule_academic_period ON schedules(academic_year, semester);
CREATE INDEX IF NOT EXISTS idx_schedule_active ON schedules(is_active);
CREATE INDEX IF NOT EXISTS idx_schedule_time_range ON schedules(start_time, end_time);

-- TeachingActivity table indexes
CREATE INDEX IF NOT EXISTS idx_teaching_activity_teacher_date ON teaching_activities(teacher_id, date);
CREATE INDEX IF NOT EXISTS idx_teaching_activity_classroom_date ON teaching_activities(class_room_id, date);
CREATE INDEX IF NOT EXISTS idx_teaching_activity_subject_date ON teaching_activities(subject_id, date);
CREATE INDEX IF NOT EXISTS idx_teaching_activity_schedule ON teaching_activities(schedule_id);
CREATE INDEX IF NOT EXISTS idx_teaching_activity_completed ON teaching_activities(is_completed);
CREATE INDEX IF NOT EXISTS idx_teaching_activity_date_time ON teaching_activities(date, start_time);

-- Attendance table indexes (most critical for performance)
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendances(student_id, created_at);
CREATE INDEX IF NOT EXISTS idx_attendance_teaching_activity ON attendances(teaching_activity_id);
CREATE INDEX IF NOT EXISTS idx_attendance_status ON attendances(status);
CREATE INDEX IF NOT EXISTS idx_attendance_recorded_by ON attendances(recorded_by);
CREATE INDEX IF NOT EXISTS idx_attendance_student_status ON attendances(student_id, status);

-- Composite index for attendance reporting queries
CREATE INDEX IF NOT EXISTS idx_attendance_reporting ON attendances(teaching_activity_id, student_id, status, created_at);

-- Assessment table indexes
CREATE INDEX IF NOT EXISTS idx_assessment_subject_classroom ON assessments(subject_id, class_room_id);
CREATE INDEX IF NOT EXISTS idx_assessment_teacher ON assessments(teacher_id);
CREATE INDEX IF NOT EXISTS idx_assessment_academic_period ON assessments(academic_year, semester);
CREATE INDEX IF NOT EXISTS idx_assessment_due_date ON assessments(due_date);
CREATE INDEX IF NOT EXISTS idx_assessment_type ON assessments(type);
CREATE INDEX IF NOT EXISTS idx_assessment_active ON assessments(is_active);

-- StudentAssessment table indexes
CREATE INDEX IF NOT EXISTS idx_student_assessment_student ON student_assessments(student_id);
CREATE INDEX IF NOT EXISTS idx_student_assessment_assessment ON student_assessments(assessment_id);
CREATE INDEX IF NOT EXISTS idx_student_assessment_submitted ON student_assessments(is_submitted);
CREATE INDEX IF NOT EXISTS idx_student_assessment_graded ON student_assessments(graded_at);
CREATE INDEX IF NOT EXISTS idx_student_assessment_score ON student_assessments(score);

-- Role and Permission indexes
CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_permission_name ON permissions(name);

-- User-Role junction table indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);

-- Role-Permission junction table indexes
CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- Covering indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_student_search_covering ON students(nama_lengkap, nis, status, class_room_id, tahun_masuk);
CREATE INDEX IF NOT EXISTS idx_attendance_summary_covering ON attendances(teaching_activity_id, status, student_id, created_at);

-- Full-text search indexes (if needed for advanced search)
-- Note: These are MySQL specific and may need adjustment based on actual requirements
-- ALTER TABLE students ADD FULLTEXT(nama_lengkap, alamat);
-- ALTER TABLE users ADD FULLTEXT(name, email);

-- Analyze tables to update statistics after index creation
ANALYZE TABLE users;
ANALYZE TABLE students;
ANALYZE TABLE class_rooms;
ANALYZE TABLE majors;
ANALYZE TABLE departments;
ANALYZE TABLE subjects;
ANALYZE TABLE schedules;
ANALYZE TABLE teaching_activities;
ANALYZE TABLE attendances;
ANALYZE TABLE assessments;
ANALYZE TABLE student_assessments;
ANALYZE TABLE roles;
ANALYZE TABLE permissions;