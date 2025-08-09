/**
 * Students Component
 * Student management interface with listing, search, and pagination
 */

import { StudentService } from '../services/studentService.js';
import { NotificationService } from '../services/notification.js';
import { LoadingService } from '../services/loading.js';

export default class Students {
    constructor() {
        this.studentService = new StudentService();
        this.notificationService = new NotificationService();
        this.loadingService = new LoadingService();

        this.data = {
            students: [],
            loading: false,
            currentPage: 0,
            totalPages: 0,
            totalElements: 0,
            pageSize: 10,
            sortBy: 'lastName',
            sortDir: 'asc',
            searchTerm: '',
            filters: {
                status: '',
                classRoom: '',
                major: ''
            }
        };

        this.pageSizeOptions = [5, 10, 20, 50];
    }

    async render() {
        return `
            <div class="students-container">
                <div class="row mb-4">
                    <div class="col-md-8">
                        <h1 class="h3 mb-0">Student Management</h1>
                        <p class="text-muted">Manage student information and records</p>
                    </div>
                    <div class="col-md-4 text-end">
                        <button type="button" class="btn btn-primary" id="add-student-btn">
                            <i class="fas fa-plus me-2"></i>Add Student
                        </button>
                    </div>
                </div>

                <!-- Search and Filters -->
                <div class="card mb-4">
                    <div class="card-body">
                        <div class="row g-3">
                            <div class="col-md-4">
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <i class="fas fa-search"></i>
                                    </span>
                                    <input type="text" class="form-control" id="search-input" 
                                           placeholder="Search students..." value="${this.data.searchTerm}">
                                </div>
                            </div>
                            <div class="col-md-2">
                                <select class="form-select" id="status-filter">
                                    <option value="">All Status</option>
                                    <option value="ACTIVE" ${this.data.filters.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                                    <option value="INACTIVE" ${this.data.filters.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                                    <option value="GRADUATED" ${this.data.filters.status === 'GRADUATED' ? 'selected' : ''}>Graduated</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <select class="form-select" id="classroom-filter">
                                    <option value="">All Classes</option>
                                    <option value="10A" ${this.data.filters.classRoom === '10A' ? 'selected' : ''}>10A</option>
                                    <option value="10B" ${this.data.filters.classRoom === '10B' ? 'selected' : ''}>10B</option>
                                    <option value="11A" ${this.data.filters.classRoom === '11A' ? 'selected' : ''}>11A</option>
                                    <option value="11B" ${this.data.filters.classRoom === '11B' ? 'selected' : ''}>11B</option>
                                    <option value="12A" ${this.data.filters.classRoom === '12A' ? 'selected' : ''}>12A</option>
                                    <option value="12B" ${this.data.filters.classRoom === '12B' ? 'selected' : ''}>12B</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <select class="form-select" id="major-filter">
                                    <option value="">All Majors</option>
                                    <option value="SCIENCE" ${this.data.filters.major === 'SCIENCE' ? 'selected' : ''}>Science</option>
                                    <option value="SOCIAL" ${this.data.filters.major === 'SOCIAL' ? 'selected' : ''}>Social</option>
                                    <option value="LANGUAGE" ${this.data.filters.major === 'LANGUAGE' ? 'selected' : ''}>Language</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button type="button" class="btn btn-outline-secondary w-100" id="clear-filters-btn">
                                    <i class="fas fa-times me-1"></i>Clear
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Students Table -->
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Students List</h5>
                        <div class="d-flex align-items-center">
                            <span class="text-muted me-3">
                                Showing ${this.getDisplayRange()} of ${this.data.totalElements} students
                            </span>
                            <select class="form-select form-select-sm" id="page-size-select" style="width: auto;">
                                ${this.pageSizeOptions.map(size =>
            `<option value="${size}" ${this.data.pageSize === size ? 'selected' : ''}>${size} per page</option>`
        ).join('')}
                            </select>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        ${this.renderStudentsTable()}
                    </div>
                    ${this.data.totalPages > 1 ? this.renderPagination() : ''}
                </div>
            </div>
        `;
    }

    renderStudentsTable() {
        if (this.data.loading) {
            return `
                <div class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="text-muted mt-2">Loading students...</p>
                </div>
            `;
        }

        if (this.data.students.length === 0) {
            return `
                <div class="text-center py-5">
                    <i class="fas fa-user-graduate fa-4x text-muted mb-3"></i>
                    <h5>No Students Found</h5>
                    <p class="text-muted">
                        ${this.data.searchTerm || Object.values(this.data.filters).some(f => f)
                    ? 'No students match your search criteria.'
                    : 'No students have been added yet.'}
                    </p>
                    ${!this.data.searchTerm && !Object.values(this.data.filters).some(f => f)
                    ? '<button type="button" class="btn btn-primary" onclick="document.getElementById(\'add-student-btn\').click()">Add First Student</button>'
                    : ''}
                </div>
            `;
        }

        return `
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th scope="col" class="sortable" data-sort="studentId">
                                Student ID
                                ${this.getSortIcon('studentId')}
                            </th>
                            <th scope="col" class="sortable" data-sort="lastName">
                                Name
                                ${this.getSortIcon('lastName')}
                            </th>
                            <th scope="col" class="sortable" data-sort="email">
                                Email
                                ${this.getSortIcon('email')}
                            </th>
                            <th scope="col" class="sortable" data-sort="classRoom">
                                Class
                                ${this.getSortIcon('classRoom')}
                            </th>
                            <th scope="col" class="sortable" data-sort="major">
                                Major
                                ${this.getSortIcon('major')}
                            </th>
                            <th scope="col" class="sortable" data-sort="status">
                                Status
                                ${this.getSortIcon('status')}
                            </th>
                            <th scope="col" class="sortable" data-sort="enrollmentDate">
                                Enrollment Date
                                ${this.getSortIcon('enrollmentDate')}
                            </th>
                            <th scope="col" width="120">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${this.data.students.map(student => this.renderStudentRow(student)).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    renderStudentRow(student) {
        const statusBadge = this.getStatusBadge(student.status);
        const enrollmentDate = student.enrollmentDate ?
            new Date(student.enrollmentDate).toLocaleDateString() : 'N/A';

        return `
            <tr>
                <td>
                    <strong>${student.studentId || 'N/A'}</strong>
                </td>
                <td>
                    <div class="d-flex align-items-center">
                        <div class="avatar-sm bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2">
                            ${(student.firstName?.[0] || '') + (student.lastName?.[0] || '')}
                        </div>
                        <div>
                            <div class="fw-medium">${student.firstName} ${student.lastName}</div>
                            ${student.phone ? `<small class="text-muted">${student.phone}</small>` : ''}
                        </div>
                    </div>
                </td>
                <td>
                    <a href="mailto:${student.email}" class="text-decoration-none">
                        ${student.email}
                    </a>
                </td>
                <td>
                    <span class="badge bg-info">${student.classRoom || 'N/A'}</span>
                </td>
                <td>
                    <span class="badge bg-secondary">${student.major || 'N/A'}</span>
                </td>
                <td>${statusBadge}</td>
                <td>
                    <small class="text-muted">${enrollmentDate}</small>
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        <button type="button" class="btn btn-outline-primary" 
                                data-action="view" data-student-id="${student.id}"
                                title="View Details">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button type="button" class="btn btn-outline-secondary" 
                                data-action="edit" data-student-id="${student.id}"
                                title="Edit Student">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button type="button" class="btn btn-outline-danger" 
                                data-action="delete" data-student-id="${student.id}"
                                title="Delete Student">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    renderPagination() {
        if (this.data.totalPages <= 1) return '';

        const currentPage = this.data.currentPage;
        const totalPages = this.data.totalPages;
        const maxVisiblePages = 5;

        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        let paginationHtml = `
            <div class="card-footer">
                <nav aria-label="Students pagination">
                    <ul class="pagination justify-content-center mb-0">
        `;

        // Previous button
        paginationHtml += `
            <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
                <button class="page-link" data-page="${currentPage - 1}" ${currentPage === 0 ? 'disabled' : ''}>
                    <i class="fas fa-chevron-left"></i>
                </button>
            </li>
        `;

        // First page
        if (startPage > 0) {
            paginationHtml += `
                <li class="page-item">
                    <button class="page-link" data-page="0">1</button>
                </li>
            `;
            if (startPage > 1) {
                paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        // Page numbers
        for (let i = startPage; i <= endPage; i++) {
            paginationHtml += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <button class="page-link" data-page="${i}">${i + 1}</button>
                </li>
            `;
        }

        // Last page
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            paginationHtml += `
                <li class="page-item">
                    <button class="page-link" data-page="${totalPages - 1}">${totalPages}</button>
                </li>
            `;
        }

        // Next button
        paginationHtml += `
            <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
                <button class="page-link" data-page="${currentPage + 1}" ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                    <i class="fas fa-chevron-right"></i>
                </button>
            </li>
        `;

        paginationHtml += `
                    </ul>
                </nav>
            </div>
        `;

        return paginationHtml;
    }

    getSortIcon(column) {
        if (this.data.sortBy !== column) {
            return '<i class="fas fa-sort text-muted ms-1"></i>';
        }

        return this.data.sortDir === 'asc'
            ? '<i class="fas fa-sort-up text-primary ms-1"></i>'
            : '<i class="fas fa-sort-down text-primary ms-1"></i>';
    }

    getStatusBadge(status) {
        const statusConfig = {
            'ACTIVE': { class: 'bg-success', text: 'Active' },
            'INACTIVE': { class: 'bg-warning', text: 'Inactive' },
            'GRADUATED': { class: 'bg-info', text: 'Graduated' },
            'SUSPENDED': { class: 'bg-danger', text: 'Suspended' }
        };

        const config = statusConfig[status] || { class: 'bg-secondary', text: status || 'Unknown' };
        return `<span class="badge ${config.class}">${config.text}</span>`;
    }

    getDisplayRange() {
        if (this.data.totalElements === 0) return '0-0';

        const start = this.data.currentPage * this.data.pageSize + 1;
        const end = Math.min((this.data.currentPage + 1) * this.data.pageSize, this.data.totalElements);

        return `${start}-${end}`;
    }

    async init() {
        console.log('Students component initialized');
        await this.loadStudents();
        this.attachEventListeners();
    }

    attachEventListeners() {
        const container = document.querySelector('.students-container');
        if (!container) return;

        // Search input
        const searchInput = container.querySelector('#search-input');
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.data.searchTerm = e.target.value;
                    this.data.currentPage = 0;
                    this.loadStudents();
                }, 500);
            });
        }

        // Filter selects
        const statusFilter = container.querySelector('#status-filter');
        const classroomFilter = container.querySelector('#classroom-filter');
        const majorFilter = container.querySelector('#major-filter');

        [statusFilter, classroomFilter, majorFilter].forEach(filter => {
            if (filter) {
                filter.addEventListener('change', (e) => {
                    const filterType = e.target.id.replace('-filter', '').replace('classroom', 'classRoom');
                    this.data.filters[filterType] = e.target.value;
                    this.data.currentPage = 0;
                    this.loadStudents();
                });
            }
        });

        // Clear filters button
        const clearFiltersBtn = container.querySelector('#clear-filters-btn');
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', () => {
                this.clearFilters();
            });
        }

        // Page size select
        const pageSizeSelect = container.querySelector('#page-size-select');
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                this.data.pageSize = parseInt(e.target.value);
                this.data.currentPage = 0;
                this.loadStudents();
            });
        }

        // Sortable columns
        const sortableHeaders = container.querySelectorAll('.sortable');
        sortableHeaders.forEach(header => {
            header.addEventListener('click', () => {
                const sortBy = header.dataset.sort;
                if (this.data.sortBy === sortBy) {
                    this.data.sortDir = this.data.sortDir === 'asc' ? 'desc' : 'asc';
                } else {
                    this.data.sortBy = sortBy;
                    this.data.sortDir = 'asc';
                }
                this.data.currentPage = 0;
                this.loadStudents();
            });
            header.style.cursor = 'pointer';
        });

        // Pagination buttons
        container.addEventListener('click', (e) => {
            if (e.target.closest('[data-page]')) {
                const page = parseInt(e.target.closest('[data-page]').dataset.page);
                if (page !== this.data.currentPage && page >= 0 && page < this.data.totalPages) {
                    this.data.currentPage = page;
                    this.loadStudents();
                }
            }
        });

        // Action buttons
        container.addEventListener('click', (e) => {
            const actionBtn = e.target.closest('[data-action]');
            if (actionBtn) {
                const action = actionBtn.dataset.action;
                const studentId = actionBtn.dataset.studentId;
                this.handleStudentAction(action, studentId);
            }
        });

        // Add student button
        const addStudentBtn = container.querySelector('#add-student-btn');
        if (addStudentBtn) {
            addStudentBtn.addEventListener('click', () => {
                this.handleStudentAction('add');
            });
        }
    }

    async loadStudents() {
        try {
            this.data.loading = true;
            this.updateUI();

            const params = {
                page: this.data.currentPage,
                size: this.data.pageSize,
                sortBy: this.data.sortBy,
                sortDir: this.data.sortDir,
                filters: {
                    ...this.data.filters,
                    search: this.data.searchTerm
                }
            };

            // Remove empty filters
            Object.keys(params.filters).forEach(key => {
                if (!params.filters[key]) {
                    delete params.filters[key];
                }
            });

            const response = await this.studentService.getStudents(params);

            this.data.students = response.content || [];
            this.data.totalPages = response.totalPages || 0;
            this.data.totalElements = response.totalElements || 0;
            this.data.currentPage = response.number || 0;

        } catch (error) {
            console.error('Error loading students:', error);
            this.notificationService.showApiError(error);
            this.data.students = [];
            this.data.totalPages = 0;
            this.data.totalElements = 0;
        } finally {
            this.data.loading = false;
            this.updateUI();
        }
    }

    clearFilters() {
        this.data.searchTerm = '';
        this.data.filters = {
            status: '',
            classRoom: '',
            major: ''
        };
        this.data.currentPage = 0;

        // Reset form controls
        const container = document.querySelector('.students-container');
        if (container) {
            const searchInput = container.querySelector('#search-input');
            const statusFilter = container.querySelector('#status-filter');
            const classroomFilter = container.querySelector('#classroom-filter');
            const majorFilter = container.querySelector('#major-filter');

            if (searchInput) searchInput.value = '';
            if (statusFilter) statusFilter.value = '';
            if (classroomFilter) classroomFilter.value = '';
            if (majorFilter) majorFilter.value = '';
        }

        this.loadStudents();
    }

    async handleStudentAction(action, studentId = null) {
        switch (action) {
            case 'add':
                await this.showStudentForm('create');
                break;
            case 'view':
                await this.showStudentDetails(studentId);
                break;
            case 'edit':
                await this.showStudentForm('edit', studentId);
                break;
            case 'delete':
                await this.confirmDeleteStudent(studentId);
                break;
        }
    }

    /**
     * Show student form modal
     */
    async showStudentForm(mode, studentId = null) {
        try {
            // Import StudentForm dynamically
            const { StudentForm } = await import('./student-form.js');

            // Create form container
            const formContainer = document.createElement('div');
            document.body.appendChild(formContainer);

            // Initialize form
            const studentForm = new StudentForm(formContainer, {
                mode: mode,
                studentId: studentId,
                onSave: (savedStudent) => {
                    // Refresh the students list
                    this.loadStudents();
                    // Remove form container
                    document.body.removeChild(formContainer);
                },
                onCancel: () => {
                    // Remove form container
                    document.body.removeChild(formContainer);
                }
            });

            await studentForm.init();

        } catch (error) {
            console.error('Error showing student form:', error);
            this.notificationService.showError('Error loading student form');
        }
    }

    /**
     * Show student details modal
     */
    async showStudentDetails(studentId) {
        try {
            this.loadingService.show('Loading student details...');

            const student = await this.studentService.getStudentById(studentId);

            // Create details modal
            const modalHtml = this.createStudentDetailsModal(student);
            const modalContainer = document.createElement('div');
            modalContainer.innerHTML = modalHtml;
            document.body.appendChild(modalContainer);

            // Show modal
            const modal = new bootstrap.Modal(modalContainer.querySelector('.modal'));
            modal.show();

            // Handle edit button in details modal
            modalContainer.querySelector('.modal').addEventListener('editStudent', (e) => {
                modal.hide();
                this.showStudentForm('edit', e.detail.studentId);
            });

            // Remove modal when hidden
            modalContainer.querySelector('.modal').addEventListener('hidden.bs.modal', () => {
                document.body.removeChild(modalContainer);
            });

        } catch (error) {
            console.error('Error loading student details:', error);
            this.notificationService.showApiError(error);
        } finally {
            this.loadingService.hide();
        }
    }

    /**
     * Create student details modal HTML
     */
    createStudentDetailsModal(student) {
        const enrollmentDate = student.enrollmentDate ?
            new Date(student.enrollmentDate).toLocaleDateString() : 'N/A';
        const statusBadge = this.getStatusBadge(student.status);

        return `
            <div class="modal fade" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">
                                <i class="fas fa-user-graduate me-2"></i>
                                Student Details
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row g-3">
                                <div class="col-12 text-center mb-3">
                                    <div class="avatar-lg bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-2" style="width: 80px; height: 80px; font-size: 2rem;">
                                        ${(student.firstName?.[0] || '') + (student.lastName?.[0] || '')}
                                    </div>
                                    <h4 class="mb-1">${student.firstName} ${student.lastName}</h4>
                                    <p class="text-muted mb-2">${student.studentId}</p>
                                    ${statusBadge}
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Email Address</label>
                                    <p class="mb-0">
                                        <a href="mailto:${student.email}" class="text-decoration-none">
                                            <i class="fas fa-envelope me-2"></i>${student.email}
                                        </a>
                                    </p>
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Phone Number</label>
                                    <p class="mb-0">
                                        ${student.phone ? `<i class="fas fa-phone me-2"></i>${student.phone}` : 'Not provided'}
                                    </p>
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Class Room</label>
                                    <p class="mb-0">
                                        <span class="badge bg-info">${student.classRoom || 'N/A'}</span>
                                    </p>
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Major</label>
                                    <p class="mb-0">
                                        <span class="badge bg-secondary">${student.major || 'N/A'}</span>
                                    </p>
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Enrollment Date</label>
                                    <p class="mb-0">
                                        <i class="fas fa-calendar me-2"></i>${enrollmentDate}
                                    </p>
                                </div>
                                
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Status</label>
                                    <p class="mb-0">${statusBadge}</p>
                                </div>
                                
                                ${student.address ? `
                                <div class="col-12">
                                    <label class="form-label fw-bold">Address</label>
                                    <p class="mb-0">
                                        <i class="fas fa-map-marker-alt me-2"></i>${student.address}
                                    </p>
                                </div>
                                ` : ''}
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button type="button" class="btn btn-primary" onclick="this.closest('.modal').dispatchEvent(new CustomEvent('editStudent', {detail: {studentId: '${student.id}'}}))">
                                <i class="fas fa-edit me-1"></i>Edit Student
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Confirm and delete student
     */
    async confirmDeleteStudent(studentId) {
        try {
            // Get student details for confirmation
            const student = await this.studentService.getStudentById(studentId);

            // Create confirmation modal
            const modalHtml = this.createDeleteConfirmationModal(student);
            const modalContainer = document.createElement('div');
            modalContainer.innerHTML = modalHtml;
            document.body.appendChild(modalContainer);

            // Show modal
            const modal = new bootstrap.Modal(modalContainer.querySelector('.modal'));
            modal.show();

            // Handle confirmation
            const confirmBtn = modalContainer.querySelector('#confirm-delete-btn');
            const cancelBtn = modalContainer.querySelector('#cancel-delete-btn');

            confirmBtn.addEventListener('click', async () => {
                try {
                    confirmBtn.disabled = true;
                    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Deleting...';

                    await this.studentService.deleteStudent(studentId);

                    this.notificationService.showSuccess(`Student ${student.firstName} ${student.lastName} deleted successfully`);

                    // Refresh the students list
                    await this.loadStudents();

                    modal.hide();
                } catch (error) {
                    console.error('Error deleting student:', error);
                    this.notificationService.showApiError(error);

                    confirmBtn.disabled = false;
                    confirmBtn.innerHTML = '<i class="fas fa-trash me-1"></i>Delete Student';
                }
            });

            cancelBtn.addEventListener('click', () => {
                modal.hide();
            });

            // Remove modal when hidden
            modalContainer.querySelector('.modal').addEventListener('hidden.bs.modal', () => {
                document.body.removeChild(modalContainer);
            });

        } catch (error) {
            console.error('Error loading student for deletion:', error);
            this.notificationService.showApiError(error);
        }
    }

    /**
     * Create delete confirmation modal HTML
     */
    createDeleteConfirmationModal(student) {
        const statusBadge = this.getStatusBadge(student.status);

        return `
            <div class="modal fade" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header bg-danger text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-exclamation-triangle me-2"></i>
                                Confirm Delete Student
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="alert alert-warning">
                                <i class="fas fa-warning me-2"></i>
                                <strong>Warning:</strong> This action cannot be undone. All student data including grades and assessments will be permanently deleted.
                            </div>
                            
                            <div class="text-center mb-3">
                                <div class="avatar-lg bg-danger text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-2" style="width: 60px; height: 60px; font-size: 1.5rem;">
                                    ${(student.firstName?.[0] || '') + (student.lastName?.[0] || '')}
                                </div>
                                <h5 class="mb-1">${student.firstName} ${student.lastName}</h5>
                                <p class="text-muted mb-2">${student.studentId}</p>
                                ${statusBadge}
                            </div>
                            
                            <div class="row g-2 text-sm">
                                <div class="col-6">
                                    <strong>Email:</strong><br>
                                    <span class="text-muted">${student.email}</span>
                                </div>
                                <div class="col-6">
                                    <strong>Class:</strong><br>
                                    <span class="text-muted">${student.classRoom || 'N/A'}</span>
                                </div>
                                <div class="col-6">
                                    <strong>Major:</strong><br>
                                    <span class="text-muted">${student.major || 'N/A'}</span>
                                </div>
                                <div class="col-6">
                                    <strong>Phone:</strong><br>
                                    <span class="text-muted">${student.phone || 'N/A'}</span>
                                </div>
                            </div>
                            
                            <p class="mt-3 mb-0">
                                Are you sure you want to delete this student? This will permanently remove:
                            </p>
                            <ul class="mt-2 mb-0">
                                <li>Student profile and personal information</li>
                                <li>All academic records and grades</li>
                                <li>Assessment history</li>
                                <li>Attendance records</li>
                            </ul>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" id="cancel-delete-btn">
                                <i class="fas fa-times me-1"></i>Cancel
                            </button>
                            <button type="button" class="btn btn-danger" id="confirm-delete-btn">
                                <i class="fas fa-trash me-1"></i>Delete Student
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    updateUI() {
        const container = document.querySelector('.students-container');
        if (container) {
            const tableContainer = container.querySelector('.card-body');
            if (tableContainer) {
                tableContainer.innerHTML = this.renderStudentsTable();
            }

            // Update pagination
            const existingPagination = container.querySelector('.card-footer');
            if (existingPagination) {
                existingPagination.remove();
            }

            if (this.data.totalPages > 1) {
                const card = container.querySelector('.card:last-child');
                if (card) {
                    card.insertAdjacentHTML('beforeend', this.renderPagination());
                }
            }

            // Update display info
            const displayInfo = container.querySelector('.card-header span');
            if (displayInfo) {
                displayInfo.textContent = `Showing ${this.getDisplayRange()} of ${this.data.totalElements} students`;
            }

            // Re-attach event listeners for new elements
            this.attachEventListeners();
        }
    }
}