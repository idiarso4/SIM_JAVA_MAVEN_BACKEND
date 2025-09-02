// Authentication Module
const Auth = {
    async logout() {
        try {
            const token = localStorage.getItem('sim_auth_token');
            if (token) {
                await fetch('/api/v1/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            localStorage.removeItem('sim_auth_token');
            localStorage.removeItem('sim_refresh_token');
            localStorage.removeItem('sim_current_user');
            window.location.href = '/auth-login.html';
        }
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