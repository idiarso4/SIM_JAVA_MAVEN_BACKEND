// Simple Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Dashboard loading...');
    
    // Setup navigation
    setupNavigation();
    
    // Load initial data
    loadDashboardStats();
    
    console.log('✅ Dashboard ready!');
});

// Setup navigation
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link[data-section]');
    const contentSections = document.querySelectorAll('.content-section');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetSection = this.getAttribute('data-section');
            
            // Remove active from all nav links
            navLinks.forEach(nav => nav.classList.remove('active'));
            
            // Add active to clicked nav link
            this.classList.add('active');
            
            // Hide all sections
            contentSections.forEach(section => section.classList.add('d-none'));
            
            // Show target section
            const targetElement = document.getElementById(`${targetSection}-section`);
            if (targetElement) {
                targetElement.classList.remove('d-none');
            }
        });
    });
}

// Load dashboard stats
function loadDashboardStats() {
    // Update stat cards with sample data
    updateStatCard('totalStudents', 150);
    updateStatCard('totalUsers', 25);
    updateStatCard('activeClasses', 12);
    updateStatCard('totalAssessments', 8);
}

// Update stat card
function updateStatCard(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
    }
}

// Show alert function
function showAlert(type, message) {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;
    
    const alertId = 'alert-' + Date.now();
    const alertHtml = `
        <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    alertContainer.insertAdjacentHTML('beforeend', alertHtml);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        const alertElement = document.getElementById(alertId);
        if (alertElement) {
            alertElement.remove();
        }
    }, 5000);
}

// Student functions
function loadStudents() {
    showAlert('info', 'Loading students... (Demo mode)');
}

function showCreateStudentForm() {
    showAlert('info', 'Create Student form will be implemented');
}

function showSearchStudents() {
    showAlert('info', 'Search Students will be implemented');
}

// Class functions
function loadClasses() {
    showAlert('info', 'Loading classes... (Demo mode)');
}

function showAddClassForm() {
    showAlert('info', 'Add Class form will be implemented');
}

function showSearchClasses() {
    showAlert('info', 'Search Classes will be implemented');
}

function loadAvailableClasses() {
    showAlert('info', 'Loading available classes... (Demo mode)');
}

// Teacher functions
function loadTeachers() {
    showAlert('info', 'Loading teachers... (Demo mode)');
}

function showAddTeacherForm() {
    showAlert('info', 'Add Teacher form will be implemented');
}

function showSearchTeachers() {
    showAlert('info', 'Search Teachers will be implemented');
}

function loadActiveTeachers() {
    showAlert('info', 'Loading active teachers... (Demo mode)');
}

// Attendance functions
function loadTodayAttendance() {
    showAlert('info', 'Loading today\'s attendance... (Demo mode)');
}

function showMarkAttendanceForm() {
    showAlert('info', 'Mark Attendance form will be implemented');
}

function showAttendanceReports() {
    showAlert('info', 'Attendance Reports will be implemented');
}

function loadAttendanceHistory() {
    showAlert('info', 'Loading attendance history... (Demo mode)');
}

// Assessment functions
function loadAssessments() {
    showAlert('info', 'Loading assessments... (Demo mode)');
}

function showCreateAssessmentForm() {
    showAlert('info', 'Create Assessment form will be implemented');
}

function showSearchAssessments() {
    showAlert('info', 'Search Assessments will be implemented');
}

function loadUpcomingAssessments() {
    showAlert('info', 'Loading upcoming assessments... (Demo mode)');
}

// Grade functions
function loadGrades() {
    showAlert('info', 'Loading grades... (Demo mode)');
}

function showEnterGradesForm() {
    showAlert('info', 'Enter Grades form will be implemented');
}

function showGradeReports() {
    showAlert('info', 'Grade Reports will be implemented');
}

function loadGradeAnalytics() {
    showAlert('info', 'Loading grade analytics... (Demo mode)');
}

// User functions
function loadAllUsers() {
    showAlert('info', 'Loading users... (Demo mode)');
}

function showCreateUserForm() {
    showAlert('info', 'Create User form will be implemented');
}

function showSearchUsers() {
    showAlert('info', 'Search Users will be implemented');
}

function loadUserRoles() {
    showAlert('info', 'Loading user roles... (Demo mode)');
}

// Report functions
function generateStudentReport() {
    showAlert('info', 'Generating student report... (Demo mode)');
}

function generateAttendanceReport() {
    showAlert('info', 'Generating attendance report... (Demo mode)');
}

function generateGradeReport() {
    showAlert('info', 'Generating grade report... (Demo mode)');
}

function generateCustomReport() {
    showAlert('info', 'Generating custom report... (Demo mode)');
}

// Excel functions
function showImportExcel() {
    showAlert('info', 'Import Excel will be implemented');
}

function exportToExcel() {
    showAlert('info', 'Export to Excel will be implemented');
}

function showImportTeachersExcel() {
    showAlert('info', 'Import Teachers from Excel will be implemented');
}

function showImportStudentsExcel() {
    showAlert('info', 'Import Students from Excel will be implemented');
}

function showExcelTemplates() {
    showAlert('info', 'Excel Templates will be implemented');
}

function showImportHistory() {
    showAlert('info', 'Import History will be implemented');
}

// System functions
function checkSystemHealth() {
    showAlert('info', 'Checking system health... (Demo mode)');
}

function viewSystemLogs() {
    showAlert('info', 'System Logs will be implemented');
}

function showDatabaseStatus() {
    showAlert('info', 'Database Status will be implemented');
}

function showPerformanceMetrics() {
    showAlert('info', 'Performance Metrics will be implemented');
}

// Logout function
document.addEventListener('DOMContentLoaded', function() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Clear auth data
            localStorage.removeItem('sim_auth_token');
            localStorage.removeItem('sim_refresh_token');
            localStorage.removeItem('sim_current_user');
            
            // Redirect to login
            window.location.href = '/auth-login.html';
        });
    }
});