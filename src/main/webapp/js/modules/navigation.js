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
        }
    }
};