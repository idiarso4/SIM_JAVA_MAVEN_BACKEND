package com.school.sim.security;

/**
 * Constants for permission names used in role-based access control
 * Defines all available permissions in the system
 */
public final class Permission {

    // User Management Permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_LIST = "user:list";

    // Student Management Permissions
    public static final String STUDENT_CREATE = "student:create";
    public static final String STUDENT_READ = "student:read";
    public static final String STUDENT_UPDATE = "student:update";
    public static final String STUDENT_DELETE = "student:delete";
    public static final String STUDENT_LIST = "student:list";
    public static final String STUDENT_IMPORT = "student:import";
    public static final String STUDENT_EXPORT = "student:export";

    // Teacher Management Permissions
    public static final String TEACHER_CREATE = "teacher:create";
    public static final String TEACHER_READ = "teacher:read";
    public static final String TEACHER_UPDATE = "teacher:update";
    public static final String TEACHER_DELETE = "teacher:delete";
    public static final String TEACHER_LIST = "teacher:list";

    // Class Management Permissions
    public static final String CLASS_CREATE = "class:create";
    public static final String CLASS_READ = "class:read";
    public static final String CLASS_UPDATE = "class:update";
    public static final String CLASS_DELETE = "class:delete";
    public static final String CLASS_LIST = "class:list";
    public static final String CLASS_ASSIGN_TEACHER = "class:assign_teacher";
    public static final String CLASS_ASSIGN_STUDENTS = "class:assign_students";

    // Subject Management Permissions
    public static final String SUBJECT_CREATE = "subject:create";
    public static final String SUBJECT_READ = "subject:read";
    public static final String SUBJECT_UPDATE = "subject:update";
    public static final String SUBJECT_DELETE = "subject:delete";
    public static final String SUBJECT_LIST = "subject:list";

    // Schedule Management Permissions
    public static final String SCHEDULE_CREATE = "schedule:create";
    public static final String SCHEDULE_READ = "schedule:read";
    public static final String SCHEDULE_UPDATE = "schedule:update";
    public static final String SCHEDULE_DELETE = "schedule:delete";
    public static final String SCHEDULE_LIST = "schedule:list";

    // Attendance Management Permissions
    public static final String ATTENDANCE_CREATE = "attendance:create";
    public static final String ATTENDANCE_READ = "attendance:read";
    public static final String ATTENDANCE_UPDATE = "attendance:update";
    public static final String ATTENDANCE_DELETE = "attendance:delete";
    public static final String ATTENDANCE_LIST = "attendance:list";
    public static final String ATTENDANCE_REPORT = "attendance:report";

    // Assessment Management Permissions
    public static final String ASSESSMENT_CREATE = "assessment:create";
    public static final String ASSESSMENT_READ = "assessment:read";
    public static final String ASSESSMENT_UPDATE = "assessment:update";
    public static final String ASSESSMENT_DELETE = "assessment:delete";
    public static final String ASSESSMENT_LIST = "assessment:list";
    public static final String ASSESSMENT_GRADE = "assessment:grade";

    // Report Permissions
    public static final String REPORT_GENERATE = "report:generate";
    public static final String REPORT_EXPORT = "report:export";
    public static final String REPORT_VIEW_ALL = "report:view_all";

    // System Administration Permissions
    public static final String SYSTEM_CONFIG = "system:config";
    public static final String SYSTEM_BACKUP = "system:backup";
    public static final String SYSTEM_RESTORE = "system:restore";
    public static final String SYSTEM_MONITOR = "system:monitor";

    // Private constructor to prevent instantiation
    private Permission() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
