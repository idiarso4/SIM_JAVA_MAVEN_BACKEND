-- Additional performance indexes for complex queries and reporting
-- These indexes support advanced reporting and analytics queries

-- Composite indexes for attendance reporting
CREATE INDEX IF NOT EXISTS idx_attendance_date_range_reporting ON attendances(
    teaching_activity_id, 
    student_id, 
    status, 
    created_at
);

-- Index for attendance statistics by date
CREATE INDEX IF NOT EXISTS idx_teaching_activity_date_subject ON teaching_activities(
    date, 
    subject_id, 
    class_room_id, 
    is_completed
);

-- Assessment reporting indexes
CREATE INDEX IF NOT EXISTS idx_student_assessment_reporting ON student_assessments(
    student_id, 
    assessment_id, 
    score, 
    is_submitted, 
    graded_at
);

-- Academic performance tracking
CREATE INDEX IF NOT EXISTS idx_assessment_academic_tracking ON assessments(
    class_room_id, 
    subject_id, 
    academic_year, 
    semester, 
    type, 
    is_active
);

-- Student search and filtering
CREATE INDEX IF NOT EXISTS idx_student_comprehensive_search ON students(
    status, 
    class_room_id, 
    tahun_masuk, 
    nama_lengkap(50)
);

-- User authentication and session management
CREATE INDEX IF NOT EXISTS idx_user_auth_session ON users(
    email, 
    is_active, 
    last_login_at
);

-- Schedule conflict detection
CREATE INDEX IF NOT EXISTS idx_schedule_conflict_detection ON schedules(
    teacher_id, 
    day_of_week, 
    start_time, 
    end_time, 
    academic_year, 
    semester, 
    is_active
);

-- Classroom schedule optimization
CREATE INDEX IF NOT EXISTS idx_schedule_classroom_optimization ON schedules(
    class_room_id, 
    day_of_week, 
    start_time, 
    end_time, 
    is_active
);

-- Teaching activity completion tracking
CREATE INDEX IF NOT EXISTS idx_teaching_activity_completion ON teaching_activities(
    teacher_id, 
    date, 
    is_completed, 
    subject_id
);

-- Assessment deadline tracking
CREATE INDEX IF NOT EXISTS idx_assessment_deadline_tracking ON assessments(
    due_date, 
    class_room_id, 
    teacher_id, 
    is_active
);

-- Student assessment submission tracking
CREATE INDEX IF NOT EXISTS idx_student_assessment_submission ON student_assessments(
    assessment_id, 
    is_submitted, 
    submission_date, 
    graded_at
);

-- Role-based access control optimization
CREATE INDEX IF NOT EXISTS idx_user_roles_access_control ON user_roles(
    user_id, 
    role_id
);

-- Permission checking optimization
CREATE INDEX IF NOT EXISTS idx_role_permissions_check ON role_permissions(
    role_id, 
    permission_id
);

-- Audit trail indexes for tracking changes
CREATE INDEX IF NOT EXISTS idx_users_audit_trail ON users(
    updated_at, 
    created_at
);

CREATE INDEX IF NOT EXISTS idx_students_audit_trail ON students(
    updated_at, 
    created_at, 
    status
);

-- Performance monitoring indexes
CREATE INDEX IF NOT EXISTS idx_teaching_activities_performance ON teaching_activities(
    created_at, 
    teacher_id, 
    class_room_id
);

-- Attendance pattern analysis
CREATE INDEX IF NOT EXISTS idx_attendance_pattern_analysis ON attendances(
    student_id, 
    status, 
    created_at, 
    teaching_activity_id
);

-- Academic year and semester filtering
CREATE INDEX IF NOT EXISTS idx_schedule_academic_filtering ON schedules(
    academic_year, 
    semester, 
    is_active, 
    class_room_id
);

CREATE INDEX IF NOT EXISTS idx_assessment_academic_filtering ON assessments(
    academic_year, 
    semester, 
    is_active, 
    subject_id
);

-- Covering index for student dashboard queries
CREATE INDEX IF NOT EXISTS idx_student_dashboard_covering ON students(
    id, 
    nama_lengkap, 
    nis, 
    status, 
    class_room_id, 
    tahun_masuk, 
    created_at
);

-- Covering index for teacher dashboard queries
CREATE INDEX IF NOT EXISTS idx_teacher_dashboard_covering ON teaching_activities(
    teacher_id, 
    date, 
    subject_id, 
    class_room_id, 
    is_completed, 
    start_time, 
    end_time
);

-- Update table statistics after creating indexes
ANALYZE TABLE users;
ANALYZE TABLE students;
ANALYZE TABLE class_rooms;
ANALYZE TABLE teaching_activities;
ANALYZE TABLE attendances;
ANALYZE TABLE assessments;
ANALYZE TABLE student_assessments;
ANALYZE TABLE schedules;
ANALYZE TABLE user_roles;
ANALYZE TABLE role_permissions;