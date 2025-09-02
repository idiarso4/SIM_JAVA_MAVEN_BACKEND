// Authentication Module
const Auth = {
    logout() {
        // Clear auth data
        localStorage.removeItem('sim_auth_token');
        localStorage.removeItem('sim_refresh_token');
        localStorage.removeItem('sim_current_user');
        
        // Redirect to login
        window.location.href = '/auth-login.html';
    },

    setupLogout() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.logout();
            });
        }
    }
};