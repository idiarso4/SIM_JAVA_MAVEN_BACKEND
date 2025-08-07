/**
 * Dashboard Component
 * Main dashboard with overview statistics and quick navigation
 */

export default class Dashboard {
    constructor() {
        this.data = {
            stats: {
                totalStudents: 0,
                totalUsers: 0,
                activeClasses: 0,
                recentActivities: []
            }
        };
    }

    /**
     * Render dashboard component
     */
    async render() {
        return `
            <div class="dashboard-container">
                <div class="row mb-4">
                    <div class="col-12">
                        <h1 class="h3 mb-0">Dashboard</h1>
                        <p class="text-muted">Welcome to the School Information Management System</p>
                    </div>
                </div>

                <!-- Statistics Cards -->
                <div class="row mb-4" id="stats-cards">
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card dashboard-card h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-grow-1">
                                        <div class="dashboard-stat" id="total-students">--</div>
                                        <div class="dashboard-label">Total Students</div>
                                    </div>
                                    <div class="text-primary">
                                        <i class="fas fa-user-graduate fa-2x"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card dashboard-card h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-grow-1">
                                        <div class="dashboard-stat" id="total-users">--</div>
                                        <div class="dashboard-label">Total Users</div>
                                    </div>
                                    <div class="text-success">
                                        <i class="fas fa-users fa-2x"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card dashboard-card h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-grow-1">
                                        <div class="dashboard-stat" id="active-classes">--</div>
                                        <div class="dashboard-label">Active Classes</div>
                                    </div>
                                    <div class="text-info">
                                        <i class="fas fa-chalkboard-teacher fa-2x"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card dashboard-card h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-grow-1">
                                        <div class="dashboard-stat" id="pending-tasks">--</div>
                                        <div class="dashboard-label">Pending Tasks</div>
                                    </div>
                                    <div class="text-warning">
                                        <i class="fas fa-tasks fa-2x"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Quick Actions -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="fas fa-bolt me-2"></i>Quick Actions
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-primary w-100" data-route="students">
                                            <i class="fas fa-user-plus me-2"></i>Add Student
                                        </button>
                                    </div>
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-success w-100" data-route="users">
                                            <i class="fas fa-user-plus me-2"></i>Add User
                                        </button>
                                    </div>
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-info w-100" data-route="grades">
                                            <i class="fas fa-chart-line me-2"></i>View Grades
                                        </button>
                                    </div>
                                    <div class="col-md-3 mb-3">
                                        <button class="btn btn-warning w-100" data-route="reports">
                                            <i class="fas fa-file-alt me-2"></i>Generate Report
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Activities and Charts -->
                <div class="row">
                    <div class="col-lg-8 mb-4">
                        <div class="card h-100">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="fas fa-chart-area me-2"></i>Student Enrollment Trend
                                </h5>
                            </div>
                            <div class="card-body">
                                <canvas id="enrollment-chart" width="400" height="200"></canvas>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-4 mb-4">
                        <div class="card h-100">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="fas fa-clock me-2"></i>Recent Activities
                                </h5>
                            </div>
                            <div class="card-body">
                                <div id="recent-activities">
                                    <div class="d-flex justify-content-center py-4">
                                        <div class="spinner-border text-primary" role="status">
                                            <span class="visually-hidden">Loading...</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Initialize dashboard component
     */
    async init() {
        try {
            // Load dashboard data
            await this.loadDashboardData();
            
            // Initialize charts
            this.initializeCharts();
            
            console.log('Dashboard initialized');
        } catch (error) {
            console.error('Dashboard initialization error:', error);
            this.showError('Failed to load dashboard data');
        }
    }

    /**
     * Load dashboard statistics and data
     */
    async loadDashboardData() {
        try {
            // This would typically make API calls to get real data
            // For now, we'll use mock data
            this.data.stats = {
                totalStudents: 1250,
                totalUsers: 45,
                activeClasses: 28,
                pendingTasks: 12
            };

            // Update statistics display
            this.updateStatistics();
            
            // Load recent activities
            this.loadRecentActivities();
            
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            throw error;
        }
    }

    /**
     * Update statistics display
     */
    updateStatistics() {
        const stats = this.data.stats;
        
        // Animate counters
        this.animateCounter('total-students', stats.totalStudents);
        this.animateCounter('total-users', stats.totalUsers);
        this.animateCounter('active-classes', stats.activeClasses);
        this.animateCounter('pending-tasks', stats.pendingTasks);
    }

    /**
     * Animate counter from 0 to target value
     */
    animateCounter(elementId, targetValue) {
        const element = document.getElementById(elementId);
        if (!element) return;

        let currentValue = 0;
        const increment = targetValue / 50;
        const timer = setInterval(() => {
            currentValue += increment;
            if (currentValue >= targetValue) {
                currentValue = targetValue;
                clearInterval(timer);
            }
            element.textContent = Math.floor(currentValue).toLocaleString();
        }, 30);
    }

    /**
     * Load recent activities
     */
    loadRecentActivities() {
        const activitiesContainer = document.getElementById('recent-activities');
        if (!activitiesContainer) return;

        // Mock recent activities data
        const activities = [
            {
                icon: 'fas fa-user-plus text-success',
                text: 'New student John Doe enrolled',
                time: '2 minutes ago'
            },
            {
                icon: 'fas fa-edit text-info',
                text: 'Grade updated for Math Class',
                time: '15 minutes ago'
            },
            {
                icon: 'fas fa-file-alt text-warning',
                text: 'Monthly report generated',
                time: '1 hour ago'
            },
            {
                icon: 'fas fa-users text-primary',
                text: 'New teacher account created',
                time: '2 hours ago'
            },
            {
                icon: 'fas fa-calendar text-secondary',
                text: 'Class schedule updated',
                time: '3 hours ago'
            }
        ];

        let activitiesHTML = '';
        activities.forEach((activity, index) => {
            activitiesHTML += `
                <div class="d-flex align-items-start mb-3 ${index === activities.length - 1 ? '' : 'border-bottom pb-3'}">
                    <div class="me-3">
                        <i class="${activity.icon}"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-medium">${activity.text}</div>
                        <small class="text-muted">${activity.time}</small>
                    </div>
                </div>
            `;
        });

        activitiesContainer.innerHTML = activitiesHTML;
    }

    /**
     * Initialize charts
     */
    initializeCharts() {
        this.initializeEnrollmentChart();
    }

    /**
     * Initialize enrollment trend chart
     */
    initializeEnrollmentChart() {
        const canvas = document.getElementById('enrollment-chart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        
        // Mock enrollment data
        const enrollmentData = {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'New Enrollments',
                data: [65, 59, 80, 81, 56, 55],
                borderColor: 'rgb(13, 110, 253)',
                backgroundColor: 'rgba(13, 110, 253, 0.1)',
                tension: 0.4,
                fill: true
            }]
        };

        new Chart(ctx, {
            type: 'line',
            data: enrollmentData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    /**
     * Show error message
     */
    showError(message) {
        if (window.SIMApp && window.SIMApp.notificationService) {
            window.SIMApp.notificationService.showError(message);
        }
    }
}