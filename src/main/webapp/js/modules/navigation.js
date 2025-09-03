// Navigation Module
const Navigation = {
    init() {
        this.setupNavigation();
    },

    setupNavigation() {
        const navLinks = document.querySelectorAll('.nav-link[data-section]');
        const contentSections = document.querySelectorAll('.content-section');

        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetSection = link.getAttribute('data-section');
                this.switchSection(targetSection, navLinks, contentSections);
            });
        });
    },

    switchSection(targetSection, navLinks, contentSections) {
        // Remove active from all nav links
        navLinks.forEach(nav => nav.classList.remove('active'));

        // Add active to clicked nav link
        document.querySelector(`[data-section="${targetSection}"]`).classList.add('active');

        // Hide all sections
        contentSections.forEach(section => section.classList.add('d-none'));

        // Show target section
        const targetElement = document.getElementById(`${targetSection}-section`);
        if (targetElement) {
            targetElement.classList.remove('d-none');

            // Initialize the appropriate module when section is shown
            this.initializeModule(targetSection);
        }
    },

    initializeModule(section) {
        switch (section) {
            case 'students':
                if (typeof StudentModule !== 'undefined') {
                    StudentModule.init();
                }
                break;
            case 'teachers':
                if (typeof TeacherModule !== 'undefined') {
                    TeacherModule.init();
                }
                break;
            case 'attendance':
                if (typeof AttendanceModule !== 'undefined') {
                    AttendanceModule.init();
                }
                break;
            case 'assessments':
                if (typeof AssessmentModule !== 'undefined') {
                    AssessmentModule.init();
                }
                break;
            case 'classes':
                if (typeof ClassModule !== 'undefined') {
                    ClassModule.init();
                }
                break;
            case 'grades':
                if (typeof GradeModule !== 'undefined') {
                    GradeModule.init();
                }
                break;
            case 'users':
                if (typeof UserManagement !== 'undefined') {
                    UserManagement.init();
                }
                break;
            case 'reports':
                if (typeof ReportModule !== 'undefined') {
                    ReportModule.init();
                }
                break;
            case 'excel':
                if (typeof ExcelModule !== 'undefined') {
                    ExcelModule.init();
                }
                break;
            case 'system':
                if (typeof SystemModule !== 'undefined') {
                    SystemModule.init();
                }
                break;
            default:
                // No module initialization needed for this section
        }
    }
};