// Dashboard Module
const Dashboard = {
    init() {
        this.updateTime();
        this.loadStatsFromAPI();
    },

    async loadStatsFromAPI() {
        try {
            this.showStatsLoading();
            const response = await fetch('/api/test/dashboard-data');
            if (response.ok) {
                const data = await response.json();
                this.updateStatsFromData(data);
            } else {
                this.loadStatsFallback();
            }
        } catch (error) {
            console.warn('API not ready, using fallback:', error);
            this.loadStatsFallback();
        }
    },

    showStatsLoading() {
        ['totalStudents', 'totalUsers', 'activeClasses', 'totalAssessments'].forEach(id => {
            const element = document.getElementById(id);
            if (element) element.innerHTML = '<div class="spinner-border spinner-border-sm"></div>';
        });
    },

    updateStatsFromData(data) {
        Utils.updateStatCard('totalStudents', data.totalStudents || 0);
        Utils.updateStatCard('totalUsers', data.totalUsers || 0);
        Utils.updateStatCard('activeClasses', data.activeClasses || 0);
        Utils.updateStatCard('totalAssessments', data.totalAssessments || 0);
    },

    loadStatsFallback() {
        setTimeout(() => {
            Utils.updateStatCard('totalStudents', 150);
            Utils.updateStatCard('totalUsers', 25);
            Utils.updateStatCard('activeClasses', 12);
            Utils.updateStatCard('totalAssessments', 8);
        }, 1000);
    },

    updateTime() {
        const currentTime = new Date().toLocaleString();
        const loginTimeElement = document.getElementById('loginTime');
        const currentTimeElement = document.getElementById('currentTime');
        
        if (loginTimeElement) {
            loginTimeElement.textContent = `Logged in at: ${currentTime}`;
        }
        if (currentTimeElement) {
            currentTimeElement.textContent = currentTime;
        }
    }
};