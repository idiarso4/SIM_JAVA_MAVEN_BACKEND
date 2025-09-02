// Advanced Features Module
const Advanced = {
    // Class Statistics
    async loadClassStats() {
        const token = localStorage.getItem('sim_auth_token');
        const container = document.getElementById('classStatsCard');
        if (!container) return;

        try {
            const response = await fetch('/api/v1/classrooms/stats', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const stats = await response.json();
                container.innerHTML = `
                    <div class="row">
                        <div class="col-6"><h4>${stats.totalCount}</h4><small>Total Classes</small></div>
                        <div class="col-6"><h4>${stats.activeCount}</h4><small>Active Classes</small></div>
                    </div>
                `;
            } else {
                container.innerHTML = '<div class="alert alert-danger">Failed to load statistics</div>';
            }
        } catch (error) {
            container.innerHTML = '<div class="alert alert-danger">Error loading statistics</div>';
        }
    },

    // Display Classes
    displayClasses(classes, totalCount) {
        const container = document.getElementById('classesListContainer');
        let html = `
            <div class="d-flex justify-content-between mb-3">
                <h6>Total Classes: ${totalCount}</h6>
                <button class="btn btn-sm btn-outline-primary" onclick="loadClasses()">
                    <i class="fas fa-sync-alt me-1"></i>Refresh
                </button>
            </div>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr><th>Class Name</th><th>Grade</th><th>Capacity</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
        `;

        classes.forEach(classRoom => {
            html += `
                <tr>
                    <td><strong>${classRoom.name || 'N/A'}</strong></td>
                    <td><span class="badge bg-primary">Grade ${classRoom.grade}</span></td>
                    <td>${classRoom.capacity || 'N/A'}</td>
                    <td><span class="badge bg-${classRoom.isActive ? 'success' : 'secondary'}">${classRoom.isActive ? 'Active' : 'Inactive'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="viewClass(${classRoom.id})">
                            <i class="fas fa-eye"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        html += '</tbody></table></div>';
        container.innerHTML = html;
    },

    // View Class
    viewClass(classId) {
        Utils.showAlert('info', `View class details for ID: ${classId}`);
    },

    // Edit Class
    editClass(classId) {
        Utils.showAlert('info', `Edit class for ID: ${classId}`);
    },

    // Display Teachers
    displayTeachers(teachers, totalCount) {
        const container = document.getElementById('teachersListContainer');
        let html = `
            <div class="d-flex justify-content-between mb-3">
                <h6>Teachers (${totalCount} total)</h6>
                <button class="btn btn-sm btn-outline-primary" onclick="loadTeachers()">
                    <i class="fas fa-sync-alt me-1"></i>Refresh
                </button>
            </div>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr><th>Name</th><th>Email</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
        `;

        teachers.forEach(teacher => {
            html += `
                <tr>
                    <td>${teacher.firstName} ${teacher.lastName}</td>
                    <td>${teacher.email}</td>
                    <td><span class="badge bg-${teacher.isActive ? 'success' : 'secondary'}">${teacher.isActive ? 'Active' : 'Inactive'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="viewTeacher(${teacher.id})">
                            <i class="fas fa-eye"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        html += '</tbody></table></div>';
        container.innerHTML = html;
    },

    // View Teacher
    viewTeacher(teacherId) {
        Utils.showAlert('info', `View teacher details for ID: ${teacherId}`);
    },

    // Edit Teacher
    editTeacher(teacherId) {
        Utils.showAlert('info', `Edit teacher for ID: ${teacherId}`);
    }
};