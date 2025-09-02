/* eslint-disable no-unused-vars */
/* global Utils, bootstrap */

// Student Management Module
const StudentModule = {
  currentPage: 0,
  pageSize: 20,
  totalPages: 0,
  currentFilters: {},

  init() {
    this.loadStudents();
    this.loadClassRooms();
  },

  async loadStudents(page = 0) {
    try {
      this.currentPage = page;
      const response = await fetch(`/api/v1/students?page=${page}&size=${this.pageSize}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderStudentsTable(data.content);
        this.renderPagination(data);
        this.totalPages = data.totalPages;
      } else {
        Utils.showAlert('error', 'Failed to load students');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading students');
    }
  },

  async loadClassRooms() {
    try {
      const response = await fetch('/api/v1/classrooms', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        const select = document.getElementById('studentClassFilter');
        if (select) {
          const options = data.content.map(cls => `<option value="${cls.id}">${cls.name}</option>`).join('');
          select.innerHTML = `<option value="">All Classes</option>${options}`;
        }
      }
    } catch (error) {
      // Handle error silently for optional feature
    }
  },

  renderStudentsTable(students) {
    const tbody = document.getElementById('studentsTableBody');
    if (!tbody) {
      return;
    }

    if (students.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No students found</td></tr>';
      return;
    }

    tbody.innerHTML = students.map(student => `
      <tr>
        <td><input type="checkbox" value="${student.id}"></td>
        <td>${student.nis || 'N/A'}</td>
        <td>${student.namaLengkap || 'N/A'}</td>
        <td>${student.classRoom?.name || 'Not Assigned'}</td>
        <td>
          <span class="badge bg-${this.getStatusColor(student.status)}">
            ${student.status || 'N/A'}
          </span>
        </td>
        <td>${student.phone || 'N/A'}</td>
        <td>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary" onclick="StudentModule.viewStudent(${student.id})" title="View">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-outline-warning" onclick="StudentModule.editStudent(${student.id})" title="Edit">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-outline-danger" onclick="StudentModule.deleteStudent(${student.id})" title="Delete">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  },

  getStatusColor(status) {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'INACTIVE': return 'secondary';
      case 'GRADUATED': return 'info';
      default: return 'secondary';
    }
  },

  renderPagination(data) {
    const pagination = document.getElementById('studentsPagination');
    if (!pagination) {
      return;
    }

    let paginationHTML = '';

    // Previous button
    paginationHTML += `
      <li class="page-item ${data.first ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="StudentModule.loadStudents(${data.number - 1})">Previous</a>
      </li>
    `;

    // Page numbers (show max 5 pages)
    const startPage = Math.max(0, data.number - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
      paginationHTML += `
        <li class="page-item ${i === data.number ? 'active' : ''}">
          <a class="page-link" href="#" onclick="StudentModule.loadStudents(${i})">${i + 1}</a>
        </li>
      `;
    }

    // Next button
    paginationHTML += `
      <li class="page-item ${data.last ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="StudentModule.loadStudents(${data.number + 1})">Next</a>
      </li>
    `;

    pagination.innerHTML = paginationHTML;
  },

  async searchStudents() {
    const searchInput = document.getElementById('studentSearchInput');
    const statusFilter = document.getElementById('studentStatusFilter');
    const classFilter = document.getElementById('studentClassFilter');

    const searchRequest = {
      query: searchInput?.value || '',
      status: statusFilter?.value || null,
      classRoomId: classFilter?.value || null
    };

    try {
      const response = await fetch(`/api/v1/students/search?page=0&size=${this.pageSize}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(searchRequest)
      });

      if (response.ok) {
        const data = await response.json();
        this.renderStudentsTable(data.content);
        this.renderPagination(data);
        this.currentFilters = searchRequest;
      } else {
        Utils.showAlert('error', 'Search failed');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error searching students');
    }
  },

  clearStudentFilters() {
    document.getElementById('studentSearchInput').value = '';
    document.getElementById('studentStatusFilter').value = '';
    document.getElementById('studentClassFilter').value = '';
    this.currentFilters = {};
    this.loadStudents(0);
  },

  async viewStudent(id) {
    try {
      const response = await fetch(`/api/v1/students/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const student = await response.json();
        this.showStudentModal(student, 'view');
      } else {
        Utils.showAlert('error', 'Failed to load student details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading student');
    }
  },

  async editStudent(id) {
    try {
      const response = await fetch(`/api/v1/students/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const student = await response.json();
        this.showStudentModal(student, 'edit');
      } else {
        Utils.showAlert('error', 'Failed to load student details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading student');
    }
  },

  showStudentModal(student = null, mode = 'add') {
    const modal = new bootstrap.Modal(document.getElementById('studentModal'));
    const title = document.getElementById('studentModalTitle');

    if (mode === 'add') {
      title.textContent = 'Add Student';
      this.clearStudentForm();
    } else if (mode === 'edit') {
      title.textContent = 'Edit Student';
      this.populateStudentForm(student);
    } else if (mode === 'view') {
      title.textContent = 'View Student';
      this.populateStudentForm(student);
      // Make form read-only for view mode
      document.querySelectorAll('#studentForm input, #studentForm select, #studentForm textarea').forEach(el => {
        el.disabled = true;
      });
    }

    modal.show();
  },

  populateStudentForm(student) {
    document.getElementById('studentId').value = student.id || '';
    document.getElementById('studentNis').value = student.nis || '';
    document.getElementById('studentFullName').value = student.namaLengkap || '';
    document.getElementById('studentEmail').value = student.email || '';
    document.getElementById('studentPhone').value = student.phone || '';
    document.getElementById('studentGender').value = student.gender || 'MALE';
    document.getElementById('studentStatus').value = student.status || 'ACTIVE';
    document.getElementById('studentAddress').value = student.address || '';
  },

  clearStudentForm() {
    document.getElementById('studentForm').reset();
    document.getElementById('studentId').value = '';
    // Re-enable all form elements
    document.querySelectorAll('#studentForm input, #studentForm select, #studentForm textarea').forEach(el => {
      el.disabled = false;
    });
  },

  async saveStudent() {
    const form = document.getElementById('studentForm');
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const studentId = document.getElementById('studentId').value;
    const studentData = {
      nis: document.getElementById('studentNis').value,
      namaLengkap: document.getElementById('studentFullName').value,
      email: document.getElementById('studentEmail').value,
      phone: document.getElementById('studentPhone').value,
      gender: document.getElementById('studentGender').value,
      status: document.getElementById('studentStatus').value,
      address: document.getElementById('studentAddress').value
    };

    try {
      const url = studentId ? `/api/v1/students/${studentId}` : '/api/v1/students';
      const method = studentId ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(studentData)
      });

      if (response.ok) {
        Utils.showAlert('success', `Student ${studentId ? 'updated' : 'created'} successfully`);
        bootstrap.Modal.getInstance(document.getElementById('studentModal')).hide();
        this.loadStudents(this.currentPage);
      } else {
        const error = await response.text();
        Utils.showAlert('error', `Failed to ${studentId ? 'update' : 'create'} student: ${error}`);
      }
    } catch (error) {
      Utils.showAlert('error', 'Error saving student');
    }
  },

  async deleteStudent(id) {
    if (confirm('Are you sure you want to delete this student?')) {
      try {
        const response = await fetch(`/api/v1/students/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          Utils.showAlert('success', 'Student deleted successfully');
          this.loadStudents(this.currentPage);
        } else {
          Utils.showAlert('error', 'Failed to delete student');
        }
      } catch (error) {
        Utils.showAlert('error', 'Error deleting student');
      }
    }
  },

  async exportStudentsToExcel() {
    try {
      const response = await fetch('/api/v1/students/excel/export', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.currentFilters)
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'students_export.xlsx';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        Utils.showAlert('success', 'Students exported successfully');
      } else {
        Utils.showAlert('error', 'Failed to export students');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error exporting students');
    }
  },

  printStudentList() {
    const printWindow = window.open('', '_blank');
    const studentsTable = document.getElementById('studentsTable').cloneNode(true);

    // Remove action column
    studentsTable.querySelectorAll('th:last-child, td:last-child').forEach(el => el.remove());
    studentsTable.querySelectorAll('th:first-child, td:first-child').forEach(el => el.remove());

    printWindow.document.write(`
      <html>
        <head>
          <title>Students List</title>
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
            <h2 class="text-center mb-4">Students List</h2>
            <p class="text-center">Generated on: ${new Date().toLocaleDateString()}</p>
            ${studentsTable.outerHTML}
          </div>
          <script>window.print(); window.close();</script>
        </body>
      </html>
    `);
    printWindow.document.close();
  }
};

// Backward compatibility
const Students = StudentModule;
