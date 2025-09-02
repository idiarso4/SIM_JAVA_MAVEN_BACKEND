// Main Application
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 SIM Dashboard - Modular Version');
    
    // Initialize modules
    Navigation.init();
    Dashboard.init();
    Auth.setupLogout();
    
    console.log('✅ Dashboard ready!');
});

// Global functions for HTML onclick events
function loadStudents() { Students.loadStudents(); }
function showCreateStudentForm() { Students.showCreateForm(); }
function showSearchStudents() { Students.showSearchForm(); }
function showImportStudentsExcel() { Students.showImportExcel(); }

function loadTeachers() { Teachers.loadTeachers(); }
function showAddTeacherForm() { Teachers.showAddForm(); }
function showSearchTeachers() { Teachers.showSearchForm(); }
function showImportTeachersExcel() { Teachers.showImportExcel(); }
function loadActiveTeachers() { Teachers.loadActiveTeachers(); }

function loadClasses() { Classes.loadClasses(); }
function showAddClassForm() { Classes.showAddForm(); }
function showSearchClasses() { Classes.showSearchForm(); }
function loadAvailableClasses() { Classes.loadAvailableClasses(); }
function loadClassesByGrade(grade) { Classes.loadClassesByGrade(grade); }

// Other module functions
function loadTodayAttendance() { Utils.showAlert('info', 'Attendance feature coming soon'); }
function showMarkAttendanceForm() { Utils.showAlert('info', 'Mark Attendance coming soon'); }
function showAttendanceReports() { Utils.showAlert('info', 'Attendance Reports coming soon'); }
function loadAttendanceHistory() { Utils.showAlert('info', 'Attendance History coming soon'); }

function loadAssessments() { Utils.showAlert('info', 'Assessments feature coming soon'); }
function showCreateAssessmentForm() { Utils.showAlert('info', 'Create Assessment coming soon'); }
function showSearchAssessments() { Utils.showAlert('info', 'Search Assessments coming soon'); }
function loadUpcomingAssessments() { Utils.showAlert('info', 'Upcoming Assessments coming soon'); }

function loadGrades() { Utils.showAlert('info', 'Grades feature coming soon'); }
function showEnterGradesForm() { Utils.showAlert('info', 'Enter Grades coming soon'); }
function showGradeReports() { Utils.showAlert('info', 'Grade Reports coming soon'); }
function loadGradeAnalytics() { Utils.showAlert('info', 'Grade Analytics coming soon'); }

function loadAllUsers() { Utils.showAlert('info', 'User Management coming soon'); }
function showCreateUserForm() { Utils.showAlert('info', 'Create User coming soon'); }
function showSearchUsers() { Utils.showAlert('info', 'Search Users coming soon'); }
function loadUserRoles() { Utils.showAlert('info', 'User Roles coming soon'); }

function generateStudentReport() { Utils.showAlert('info', 'Student Reports coming soon'); }
function generateAttendanceReport() { Utils.showAlert('info', 'Attendance Reports coming soon'); }
function generateGradeReport() { Utils.showAlert('info', 'Grade Reports coming soon'); }
function generateCustomReport() { Utils.showAlert('info', 'Custom Reports coming soon'); }

function showImportExcel() { Utils.showAlert('info', 'Excel Import coming soon'); }
function exportToExcel() { Utils.showAlert('info', 'Excel Export coming soon'); }
function showExcelTemplates() { Utils.showAlert('info', 'Excel Templates coming soon'); }
function showImportHistory() { Utils.showAlert('info', 'Import History coming soon'); }

function checkSystemHealth() { Utils.showAlert('info', 'System Health coming soon'); }
function viewSystemLogs() { Utils.showAlert('info', 'System Logs coming soon'); }
function showDatabaseStatus() { Utils.showAlert('info', 'Database Status coming soon'); }
function showPerformanceMetrics() { Utils.showAlert('info', 'Performance Metrics coming soon'); }