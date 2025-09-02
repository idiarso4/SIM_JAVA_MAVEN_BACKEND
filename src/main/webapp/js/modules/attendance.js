/* eslint-disable no-unused-vars */
/* global Utils, bootstrap */

// Attendance Management Module
const AttendanceModule = {
  currentPage: 0,
  pageSize: 20,
  totalPages: 0,
  currentFilters: {},

  init() {
    this.loadAttendance();
    this.loadStudents();
    this.loadTeachingActivities();
  },

  async loadAttendance(page = 0) {
    try {
      this.currentPage = page;
      const response = await fetch(`/api/v1/attendance?page=${page}&size=${this.pageSize}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderAttendanceTable(data.content);
        this.renderPagination(data);
        this.totalPages = data.totalPages;
      } else {
        Utils.showAlert('error', 'Failed to load attendance');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading attendance');
    }
  },

  async loadStudents() {
    try {
      const response = await fetch('/api/v1/students?page=0&size=1000', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        const select = document.getElementById('attendanceStudent');
        if (select) {
          const options = data.content.map(student => 
            `<option value="${student.id}">${student.namaLengkap} (${student.nis})</option>`
          ).join('');
          select.innerHTML = `<option value="">Select Student</option>${options}`;
        }
      }
    } catch (error) {
      // Handle error silently for optional feature
    }
  },

  async loadTeachingActivities() {
    try {
      // Assuming there's an endpoint for teaching activities
      const response = await fetch('/api/v1/teaching-activities?page=0&size=1000', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        const select = document.getElementById('attendanceTeachingActivity');
        if (select) {
          const options = data.content.map(activity => 
            `<option value="${activity.id}">${activity.subject?.name} - ${activity.classRoom?.name}</option>`
          ).join('');
          select.innerHTML = `<option value="">Select Teaching Activity</option>${options}`;
        }
      }
    } catch (error) {
      // Fallback - create some dummy options
      const select = document.getElementById('attendanceTeachingActivity');
      if (select) {
        select.innerHTML = `
          <option value="">Select Teaching Activity</option>
          <option value="1">Mathematics - Class A</option>
          <option value="2">English - Class B</option>
          <option value="3">Science - Class C</option>
        `;
      }
    }
  },

  renderAttendanceTable(attendance) {
    const tbody = document.getElementById('attendanceTableBody');
    if (!tbody) {
      return;
    }

    if (attendance.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No attendance records found</td></tr>';
      return;
    }

    tbody.innerHTML = attendance.map(record => `
      <tr>
        <td><input type="checkbox" value="${record.id}"></td>
        <td>${new Date(record.date).toLocaleDateString()}</td>
        <td>${record.student?.namaLengkap || 'N/A'}</td>
        <td>${record.teachingActivity?.subject?.name || 'N/A'}</td>
        <td>
          <span class="badge bg-${this.getStatusColor(record.status)}">
            ${record.status || 'N/A'}
          </span>
        </td>
        <td>${record.notes || '-'}</td>
        <td>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary" onclick="AttendanceModule.viewAttendance(${record.id})" title="View">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-outline-warning" onclick="AttendanceModule.editAttendance(${record.id})" title="Edit">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-outline-danger" onclick="AttendanceModule.deleteAttendance(${record.id})" title="Delete">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  },

  getStatusColor(status) {
    switch (status) {
      case 'PRESENT': return 'success';
      case 'ABSENT': return 'danger';
      case 'LATE': return 'warning';
      case 'EXCUSED': return 'info';
      default: return 'secondary';
    }
  },

  renderPagination(data) {
    const pagination = document.getElementById('attendancePagination');
    if (!pagination) {
      return;
    }

    let paginationHTML = '';

    // Previous button
    paginationHTML += `
      <li class="page-item ${data.first ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="AttendanceModule.loadAttendance(${data.number - 1})">Previous</a>
      </li>
    `;

    // Page numbers (show max 5 pages)
    const startPage = Math.max(0, data.number - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
      paginationHTML += `
        <li class="page-item ${i === data.number ? 'active' : ''}">
          <a class="page-link" href="#" onclick="AttendanceModule.loadAttendance(${i})">${i + 1}</a>
        </li>
      `;
    }

    // Next button
    paginationHTML += `
      <li class="page-item ${data.last ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="AttendanceModule.loadAttendance(${data.number + 1})">Next</a>
      </li>
    `;

    pagination.innerHTML = paginationHTML;
  },

  async searchAttendance() {
    const dateFilter = document.getElementById('attendanceDateFilter');
    const statusFilter = document.getElementById('attendanceStatusFilter');

    let url = `/api/v1/attendance?page=0&size=${this.pageSize}`;

    if (dateFilter?.value) {
      url = `/api/v1/attendance/date-range?startDate=${dateFilter.value}&endDate=${dateFilter.value}&page=0&size=${this.pageSize}`;
    }

    if (statusFilter?.value) {
      url = `/api/v1/attendance/status/${statusFilter.value}?page=0&size=${this.pageSize}`;
    }

    try {
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderAttendanceTable(data.content);
        this.renderPagination(data);
        this.currentFilters = { date: dateFilter?.value, status: statusFilter?.value };
      } else {
        Utils.showAlert('error', 'Search failed');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error searching attendance');
    }
  },

  clearAttendanceFilters() {
    document.getElementById('attendanceSearchInput').value = '';
    document.getElementById('attendanceDateFilter').value = '';
    document.getElementById('attendanceStatusFilter').value = '';
    this.currentFilters = {};
    this.loadAttendance(0);
  },

  async viewAttendance(id) {
    try {
      const response = await fetch(`/api/v1/attendance/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const attendance = await response.json();
        this.showAttendanceModal(attendance, 'view');
      } else {
        Utils.showAlert('error', 'Failed to load attendance details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading attendance');
    }
  },

  async editAttendance(id) {
    try {
      const response = await fetch(`/api/v1/attendance/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const attendance = await response.json();
        this.showAttendanceModal(attendance, 'edit');
      } else {
        Utils.showAlert('error', 'Failed to load attendance details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading attendance');
    }
  },

  showAttendanceModal(attendance = null, mode = 'add') {
    const modal = new bootstrap.Modal(document.getElementById('attendanceModal'));
    const title = document.getElementById('attendanceModalTitle');

    if (mode === 'add') {
      title.textContent = 'Record Attendance';
      this.clearAttendanceForm();
      // Set today's date as default
      document.getElementById('attendanceDate').value = new Date().toISOString().split('T')[0];
    } else if (mode === 'edit') {
      title.textContent = 'Edit Attendance';
      this.populateAttendanceForm(attendance);
    } else if (mode === 'view') {
      title.textContent = 'View Attendance';
      this.populateAttendanceForm(attendance);
      // Make form read-only for view mode
      document.querySelectorAll('#attendanceForm input, #attendanceForm select, #attendanceForm textarea').forEach(el => {
        el.disabled = true;
      });
    }

    modal.show();
  },

  populateAttendanceForm(attendance) {
    document.getElementById('attendanceId').value = attendance.id || '';
    document.getElementById('attendanceStudent').value = attendance.student?.id || '';
    document.getElementById('attendanceTeachingActivity').value = attendance.teachingActivity?.id || '';
    document.getElementById('attendanceDate').value = attendance.date ? new Date(attendance.date).toISOString().split('T')[0] : '';
    document.getElementById('attendanceStatus').value = attendance.status || 'PRESENT';
    document.getElementById('attendanceNotes').value = attendance.notes || '';
  },

  clearAttendanceForm() {
    document.getElementById('attendanceForm').reset();
    document.getElementById('attendanceId').value = '';
    // Re-enable all form elements
    document.querySelectorAll('#attendanceForm input, #attendanceForm select, #attendanceForm textarea').forEach(el => {
      el.disabled = false;
    });
  },

  async saveAttendance() {
    const form = document.getElementById('attendanceForm');
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const attendanceId = document.getElementById('attendanceId').value;
    const attendanceData = {
      studentId: parseInt(document.getElementById('attendanceStudent').value),
      teachingActivityId: parseInt(document.getElementById('attendanceTeachingActivity').value, 10),
      date: document.getElementById('attendanceDate').value,
      status: document.getElementById('attendanceStatus').value,
      notes: document.getElementById('attendanceNotes').value
    };

    try {
      const url = attendanceId ? `/api/v1/attendance/${attendanceId}` : '/api/v1/attendance';
      const method = attendanceId ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(attendanceData)
      });

      if (response.ok) {
        Utils.showAlert('success', `Attendance ${attendanceId ? 'updated' : 'recorded'} successfully`);
        bootstrap.Modal.getInstance(document.getElementById('attendanceModal')).hide();
        this.loadAttendance(this.currentPage);
      } else {
        const error = await response.text();
        Utils.showAlert('error', `Failed to ${attendanceId ? 'update' : 'record'} attendance: ${error}`);
      }
    } catch (error) {
      Utils.showAlert('error', 'Error saving attendance');
    }
  },

  async deleteAttendance(id) {
    if (confirm('Are you sure you want to delete this attendance record?')) {
      try {
        const response = await fetch(`/api/v1/attendance/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          Utils.showAlert('success', 'Attendance record deleted successfully');
          this.loadAttendance(this.currentPage);
        } else {
          Utils.showAlert('error', 'Failed to delete attendance record');
        }
      } catch (error) {
        Utils.showAlert('error', 'Error deleting attendance');
      }
    }
  },

  async exportAttendanceToExcel() {
    try {
      const response = await fetch('/api/v1/attendance/reports/export', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          startDate: this.currentFilters.date || new Date(Date.now() - 30*24*60*60*1000).toISOString().split('T')[0],
          endDate: this.currentFilters.date || new Date().toISOString().split('T')[0]
        })
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'attendance_report.xlsx';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        Utils.showAlert('success', 'Attendance exported successfully');
      } else {
        Utils.showAlert('error', 'Failed to export attendance');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error exporting attendance');
    }
  },

  printAttendanceList() {
    const printWindow = window.open('', '_blank');
    const attendanceTable = document.getElementById('attendanceTable').cloneNode(true);

    // Remove action column
    attendanceTable.querySelectorAll('th:last-child, td:last-child').forEach(el => el.remove());
    attendanceTable.querySelectorAll('th:first-child, td:first-child').forEach(el => el.remove());

    printWindow.document.write(`
      <html>
        <head>
          <title>Attendance Report</title>
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
            <h2 class="text-center mb-4">Attendance Report</h2>
            <p class="text-center">Generated on: ${new Date().toLocaleDateString()}</p>
            ${attendanceTable.outerHTML}
          </div>
          <script>window.print(); window.close();</script>
        </body>
      </html>
    `);
    printWindow.document.close();
  }
};