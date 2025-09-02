/* eslint-disable no-unused-vars */
/* global Utils, bootstrap */

// Teacher Management Module
const TeacherModule = {
  currentPage: 0,
  pageSize: 20,
  totalPages: 0,
  currentFilters: {},

  init() {
    this.loadTeachers();
  },

  async loadTeachers(page = 0) {
    try {
      this.currentPage = page;
      const response = await fetch(`/api/v1/teachers?page=${page}&size=${this.pageSize}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderTeachersTable(data.content);
        this.renderPagination(data);
        this.totalPages = data.totalPages;
      } else {
        Utils.showAlert('error', 'Failed to load teachers');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading teachers');
    }
  },

  renderTeachersTable(teachers) {
    const tbody = document.getElementById('teachersTableBody');
    if (!tbody) {
      return;
    }

    if (teachers.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No teachers found</td></tr>';
      return;
    }

    tbody.innerHTML = teachers.map(teacher => `
      <tr>
        <td><input type="checkbox" value="${teacher.id}"></td>
        <td>${teacher.firstName} ${teacher.lastName}</td>
        <td>${teacher.email || 'N/A'}</td>
        <td>${teacher.phone || 'N/A'}</td>
        <td>
          <span class="badge bg-${teacher.isActive ? 'success' : 'secondary'}">
            ${teacher.isActive ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td>${new Date(teacher.createdAt).toLocaleDateString()}</td>
        <td>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary" onclick="TeacherModule.viewTeacher(${teacher.id})" title="View">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-outline-warning" onclick="TeacherModule.editTeacher(${teacher.id})" title="Edit">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-outline-danger" onclick="TeacherModule.deleteTeacher(${teacher.id})" title="Delete">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  },

  renderPagination(data) {
    const pagination = document.getElementById('teachersPagination');
    if (!pagination) {
      return;
    }

    let paginationHTML = '';

    // Previous button
    paginationHTML += `
      <li class="page-item ${data.first ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="TeacherModule.loadTeachers(${data.number - 1})">Previous</a>
      </li>
    `;

    // Page numbers (show max 5 pages)
    const startPage = Math.max(0, data.number - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
      paginationHTML += `
        <li class="page-item ${i === data.number ? 'active' : ''}">
          <a class="page-link" href="#" onclick="TeacherModule.loadTeachers(${i})">${i + 1}</a>
        </li>
      `;
    }

    // Next button
    paginationHTML += `
      <li class="page-item ${data.last ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="TeacherModule.loadTeachers(${data.number + 1})">Next</a>
      </li>
    `;

    pagination.innerHTML = paginationHTML;
  },

  async searchTeachers() {
    const searchInput = document.getElementById('teacherSearchInput');
    const query = searchInput?.value || '';
    const url = `/api/v1/teachers/search?query=${encodeURIComponent(query)}&page=0&size=${this.pageSize}`;

    try {
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderTeachersTable(data.content);
        this.renderPagination(data);
        this.currentFilters = { query };
      } else {
        Utils.showAlert('error', 'Search failed');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error searching teachers');
    }
  },

  clearTeacherFilters() {
    document.getElementById('teacherSearchInput').value = '';
    document.getElementById('teacherStatusFilter').value = '';
    document.getElementById('teacherDepartmentFilter').value = '';
    this.currentFilters = {};
    this.loadTeachers(0);
  },

  async viewTeacher(id) {
    try {
      const response = await fetch(`/api/v1/teachers/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const teacher = await response.json();
        this.showTeacherModal(teacher, 'view');
      } else {
        Utils.showAlert('error', 'Failed to load teacher details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading teacher');
    }
  },

  async editTeacher(id) {
    try {
      const response = await fetch(`/api/v1/teachers/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const teacher = await response.json();
        this.showTeacherModal(teacher, 'edit');
      } else {
        Utils.showAlert('error', 'Failed to load teacher details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading teacher');
    }
  },

  showTeacherModal(teacher = null, mode = 'add') {
    const modal = new bootstrap.Modal(document.getElementById('teacherModal'));
    const title = document.getElementById('teacherModalTitle');

    if (mode === 'add') {
      title.textContent = 'Add Teacher';
      this.clearTeacherForm();
    } else if (mode === 'edit') {
      title.textContent = 'Edit Teacher';
      this.populateTeacherForm(teacher);
    } else if (mode === 'view') {
      title.textContent = 'View Teacher';
      this.populateTeacherForm(teacher);
      // Make form read-only for view mode
      document.querySelectorAll('#teacherForm input, #teacherForm select').forEach(el => {
        el.disabled = true;
      });
    }

    modal.show();
  },

  populateTeacherForm(teacher) {
    document.getElementById('teacherId').value = teacher.id || '';
    document.getElementById('teacherFirstName').value = teacher.firstName || '';
    document.getElementById('teacherLastName').value = teacher.lastName || '';
    document.getElementById('teacherEmail').value = teacher.email || '';
    document.getElementById('teacherPhone').value = teacher.phone || '';
    document.getElementById('teacherStatus').value = teacher.isActive ? 'true' : 'false';
    // Don't populate password for security
    document.getElementById('teacherPassword').value = '';
  },

  clearTeacherForm() {
    document.getElementById('teacherForm').reset();
    document.getElementById('teacherId').value = '';
    // Re-enable all form elements
    document.querySelectorAll('#teacherForm input, #teacherForm select').forEach(el => {
      el.disabled = false;
    });
  },

  async saveTeacher() {
    const form = document.getElementById('teacherForm');
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const teacherId = document.getElementById('teacherId').value;
    const teacherData = {
      firstName: document.getElementById('teacherFirstName').value,
      lastName: document.getElementById('teacherLastName').value,
      email: document.getElementById('teacherEmail').value,
      phone: document.getElementById('teacherPhone').value,
      userType: 'TEACHER',
      isActive: document.getElementById('teacherStatus').value === 'true'
    };

    // Only include password for new teachers or if it's being changed
    const password = document.getElementById('teacherPassword').value;
    if (password) {
      teacherData.password = password;
    }

    try {
      const url = teacherId ? `/api/v1/users/${teacherId}` : '/api/v1/users';
      const method = teacherId ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(teacherData)
      });

      if (response.ok) {
        Utils.showAlert('success', `Teacher ${teacherId ? 'updated' : 'created'} successfully`);
        bootstrap.Modal.getInstance(document.getElementById('teacherModal')).hide();
        this.loadTeachers(this.currentPage);
      } else {
        const error = await response.text();
        Utils.showAlert('error', `Failed to ${teacherId ? 'update' : 'create'} teacher: ${error}`);
      }
    } catch (error) {
      Utils.showAlert('error', 'Error saving teacher');
    }
  },

  async deleteTeacher(id) {
    if (confirm('Are you sure you want to delete this teacher?')) {
      try {
        const response = await fetch(`/api/v1/users/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          Utils.showAlert('success', 'Teacher deleted successfully');
          this.loadTeachers(this.currentPage);
        } else {
          Utils.showAlert('error', 'Failed to delete teacher');
        }
      } catch (error) {
        Utils.showAlert('error', 'Error deleting teacher');
      }
    }
  },

  async exportTeachersToExcel() {
    try {
      // Since there's no specific export endpoint for teachers, we'll create a simple export
      const response = await fetch(`/api/v1/teachers?page=0&size=1000`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.downloadAsExcel(data.content, 'teachers_export.xlsx');
        Utils.showAlert('success', 'Teachers exported successfully');
      } else {
        Utils.showAlert('error', 'Failed to export teachers');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error exporting teachers');
    }
  },

  downloadAsExcel(data, filename) {
    // Simple CSV export (can be enhanced with actual Excel library)
    const headers = ['Name', 'Email', 'Phone', 'Status', 'Created'];
    const csvContent = [
      headers.join(','),
      ...data.map(teacher => [
        `"${teacher.firstName} ${teacher.lastName}"`,
        `"${teacher.email || ''}"`,
        `"${teacher.phone || ''}"`,
        `"${teacher.isActive ? 'Active' : 'Inactive'}"`,
        `"${new Date(teacher.createdAt).toLocaleDateString()}"`
      ].join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename.replace('.xlsx', '.csv');
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },

  printTeacherList() {
    const printWindow = window.open('', '_blank');
    const teachersTable = document.getElementById('teachersTable').cloneNode(true);

    // Remove action column
    teachersTable.querySelectorAll('th:last-child, td:last-child').forEach(el => el.remove());
    teachersTable.querySelectorAll('th:first-child, td:first-child').forEach(el => el.remove());

    printWindow.document.write(`
      <html>
        <head>
          <title>Teachers List</title>
          <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
          <style>
            @media print {
              .table { font-size: 12px; }
              .badge { color: #000 !important; }
            }
          </style>
        </head>
        <body>
          <div class="container-fluid">
            <h2 class="text-center mb-4">Teachers List</h2>
            <p class="text-center">Generated on: ${new Date().toLocaleDateString()}</p>
            ${teachersTable.outerHTML}
          </div>
          <script>window.print(); window.close();</script>
        </body>
      </html>
    `);
    printWindow.document.close();
  }
};

// Backward compatibility
const Teachers = TeacherModule;