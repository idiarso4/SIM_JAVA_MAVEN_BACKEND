/* eslint-disable no-unused-vars */
/* global Utils, bootstrap */

// Assessment Management Module
const AssessmentModule = {
  currentPage: 0,
  pageSize: 20,
  totalPages: 0,
  currentFilters: {},

  init() {
    this.loadAssessments();
    this.loadSubjects();
  },

  async loadAssessments(page = 0) {
    try {
      this.currentPage = page;
      const response = await fetch(`/api/v1/assessments?page=${page}&size=${this.pageSize}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.renderAssessmentsTable(data.content);
        this.renderPagination(data);
        this.totalPages = data.totalPages;
      } else {
        Utils.showAlert('error', 'Failed to load assessments');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading assessments');
    }
  },

  async loadSubjects() {
    try {
      const response = await fetch('/api/v1/subjects?page=0&size=1000', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        const selects = ['assessmentSubject', 'assessmentSubjectFilter'];
        selects.forEach(selectId => {
          const select = document.getElementById(selectId);
          if (select) {
            const defaultOption = selectId.includes('Filter')
              ? '<option value="">All Subjects</option>'
              : '<option value="">Select Subject</option>';
            const options = data.content.map(subject => `<option value="${subject.id}">${subject.name}</option>`).join('');
            select.innerHTML = defaultOption + options;
          }
        });
      }
    } catch (error) {
      // Fallback - create some dummy options
      const selects = ['assessmentSubject', 'assessmentSubjectFilter'];
      selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
          const defaultOption = selectId.includes('Filter')
            ? '<option value="">All Subjects</option>'
            : '<option value="">Select Subject</option>';
          select.innerHTML = `${defaultOption}<option value="1">Mathematics</option><option value="2">English</option><option value="3">Science</option><option value="4">History</option>`;
        }
      });
    }
  },

  renderAssessmentsTable(assessments) {
    const tbody = document.getElementById('assessmentsTableBody');
    if (!tbody) {
      return;
    }

    if (assessments.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No assessments found</td></tr>';
      return;
    }

    tbody.innerHTML = assessments.map(assessment => `
      <tr>
        <td><input type="checkbox" value="${assessment.id}"></td>
        <td>${assessment.title || 'N/A'}</td>
        <td>
          <span class="badge bg-${this.getTypeColor(assessment.type)}">
            ${assessment.type || 'N/A'}
          </span>
        </td>
        <td>${assessment.subject?.name || 'N/A'}</td>
        <td>${assessment.maxScore || 'N/A'}</td>
        <td>${assessment.assessmentDate ? new Date(assessment.assessmentDate).toLocaleDateString() : 'N/A'}</td>
        <td>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary" onclick="AssessmentModule.viewAssessment(${assessment.id})" title="View">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-outline-warning" onclick="AssessmentModule.editAssessment(${assessment.id})" title="Edit">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-outline-success" onclick="AssessmentModule.gradeAssessment(${assessment.id})" title="Grade">
              <i class="fas fa-star"></i>
            </button>
            <button class="btn btn-outline-danger" onclick="AssessmentModule.deleteAssessment(${assessment.id})" title="Delete">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  },

  getTypeColor(type) {
    switch (type) {
      case 'QUIZ': return 'primary';
      case 'EXAM': return 'danger';
      case 'ASSIGNMENT': return 'warning';
      case 'PROJECT': return 'info';
      default: return 'secondary';
    }
  },

  renderPagination(data) {
    const pagination = document.getElementById('assessmentsPagination');
    if (!pagination) {
      return;
    }

    let paginationHTML = '';

    // Previous button
    paginationHTML += `
      <li class="page-item ${data.first ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="AssessmentModule.loadAssessments(${data.number - 1})">Previous</a>
      </li>
    `;

    // Page numbers (show max 5 pages)
    const startPage = Math.max(0, data.number - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
      paginationHTML += `
        <li class="page-item ${i === data.number ? 'active' : ''}">
          <a class="page-link" href="#" onclick="AssessmentModule.loadAssessments(${i})">${i + 1}</a>
        </li>
      `;
    }

    // Next button
    paginationHTML += `
      <li class="page-item ${data.last ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="AssessmentModule.loadAssessments(${data.number + 1})">Next</a>
      </li>
    `;

    pagination.innerHTML = paginationHTML;
  },

  async searchAssessments() {
    const searchInput = document.getElementById('assessmentSearchInput');
    const typeFilter = document.getElementById('assessmentTypeFilter');
    const subjectFilter = document.getElementById('assessmentSubjectFilter');

    const searchRequest = {
      title: searchInput?.value || '',
      type: typeFilter?.value || null,
      subjectId: subjectFilter?.value || null
    };

    try {
      const response = await fetch(`/api/v1/assessments/search?page=0&size=${this.pageSize}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(searchRequest)
      });

      if (response.ok) {
        const data = await response.json();
        this.renderAssessmentsTable(data.content);
        this.renderPagination(data);
        this.currentFilters = searchRequest;
      } else {
        Utils.showAlert('error', 'Search failed');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error searching assessments');
    }
  },

  clearAssessmentFilters() {
    document.getElementById('assessmentSearchInput').value = '';
    document.getElementById('assessmentTypeFilter').value = '';
    document.getElementById('assessmentSubjectFilter').value = '';
    this.currentFilters = {};
    this.loadAssessments(0);
  },

  async viewAssessment(id) {
    try {
      const response = await fetch(`/api/v1/assessments/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const assessment = await response.json();
        this.showAssessmentModal(assessment, 'view');
      } else {
        Utils.showAlert('error', 'Failed to load assessment details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading assessment');
    }
  },

  async editAssessment(id) {
    try {
      const response = await fetch(`/api/v1/assessments/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const assessment = await response.json();
        this.showAssessmentModal(assessment, 'edit');
      } else {
        Utils.showAlert('error', 'Failed to load assessment details');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading assessment');
    }
  },

  showAssessmentModal(assessment = null, mode = 'add') {
    const modal = new bootstrap.Modal(document.getElementById('assessmentModal'));
    const title = document.getElementById('assessmentModalTitle');

    if (mode === 'add') {
      title.textContent = 'Create Assessment';
      this.clearAssessmentForm();
    } else if (mode === 'edit') {
      title.textContent = 'Edit Assessment';
      this.populateAssessmentForm(assessment);
    } else if (mode === 'view') {
      title.textContent = 'View Assessment';
      this.populateAssessmentForm(assessment);
      // Make form read-only for view mode
      document.querySelectorAll('#assessmentForm input, #assessmentForm select, #assessmentForm textarea').forEach(el => {
        el.disabled = true;
      });
    }

    modal.show();
  },

  populateAssessmentForm(assessment) {
    document.getElementById('assessmentId').value = assessment.id || '';
    document.getElementById('assessmentTitle').value = assessment.title || '';
    document.getElementById('assessmentType').value = assessment.type || 'QUIZ';
    document.getElementById('assessmentSubject').value = assessment.subject?.id || '';
    document.getElementById('assessmentMaxScore').value = assessment.maxScore || '';
    document.getElementById('assessmentDate').value = assessment.assessmentDate
      ? new Date(assessment.assessmentDate).toISOString().split('T')[0]
      : '';
    document.getElementById('assessmentDuration').value = assessment.duration || '';
    document.getElementById('assessmentDescription').value = assessment.description || '';
  },

  clearAssessmentForm() {
    document.getElementById('assessmentForm').reset();
    document.getElementById('assessmentId').value = '';
    // Re-enable all form elements
    document.querySelectorAll('#assessmentForm input, #assessmentForm select, #assessmentForm textarea').forEach(el => {
      el.disabled = false;
    });
  },

  async saveAssessment() {
    const form = document.getElementById('assessmentForm');
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const assessmentId = document.getElementById('assessmentId').value;
    const assessmentData = {
      title: document.getElementById('assessmentTitle').value,
      type: document.getElementById('assessmentType').value,
      subjectId: parseInt(document.getElementById('assessmentSubject').value, 10),
      maxScore: parseFloat(document.getElementById('assessmentMaxScore').value),
      assessmentDate: document.getElementById('assessmentDate').value || null,
      duration: document.getElementById('assessmentDuration').value
        ? parseInt(document.getElementById('assessmentDuration').value, 10)
        : null,
      description: document.getElementById('assessmentDescription').value
    };

    try {
      const url = assessmentId ? `/api/v1/assessments/${assessmentId}` : '/api/v1/assessments';
      const method = assessmentId ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(assessmentData)
      });

      if (response.ok) {
        Utils.showAlert('success', `Assessment ${assessmentId ? 'updated' : 'created'} successfully`);
        bootstrap.Modal.getInstance(document.getElementById('assessmentModal')).hide();
        this.loadAssessments(this.currentPage);
      } else {
        const error = await response.text();
        Utils.showAlert('error', `Failed to ${assessmentId ? 'update' : 'create'} assessment: ${error}`);
      }
    } catch (error) {
      Utils.showAlert('error', 'Error saving assessment');
    }
  },

  async deleteAssessment(id) {
    if (confirm('Are you sure you want to delete this assessment?')) {
      try {
        const response = await fetch(`/api/v1/assessments/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          Utils.showAlert('success', 'Assessment deleted successfully');
          this.loadAssessments(this.currentPage);
        } else {
          Utils.showAlert('error', 'Failed to delete assessment');
        }
      } catch (error) {
        Utils.showAlert('error', 'Error deleting assessment');
      }
    }
  },

  async gradeAssessment(id) {
    try {
      const response = await fetch(`/api/v1/assessments/${id}/grades`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const grades = await response.json();
        this.showGradingInterface(id, grades.content);
      } else {
        Utils.showAlert('error', 'Failed to load assessment grades');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error loading grades');
    }
  },

  showGradingInterface(assessmentId, grades) {
    // This would open a modal or navigate to a grading interface
    Utils.showAlert('info', `Grading interface for assessment ${assessmentId} will be implemented. Found ${grades.length} students.`);
  },

  async exportAssessmentsToExcel() {
    try {
      // Since there's no specific export endpoint, we'll create a simple export
      const response = await fetch('/api/v1/assessments?page=0&size=1000', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.downloadAsExcel(data.content, 'assessments_export.xlsx');
        Utils.showAlert('success', 'Assessments exported successfully');
      } else {
        Utils.showAlert('error', 'Failed to export assessments');
      }
    } catch (error) {
      Utils.showAlert('error', 'Error exporting assessments');
    }
  },

  downloadAsExcel(data, filename) {
    // Simple CSV export (can be enhanced with actual Excel library)
    const headers = ['Title', 'Type', 'Subject', 'Max Score', 'Date', 'Duration', 'Description'];
    const csvContent = [
      headers.join(','),
      ...data.map(assessment => [
        `"${assessment.title || ''}"`,
        `"${assessment.type || ''}"`,
        `"${assessment.subject?.name || ''}"`,
        `"${assessment.maxScore || ''}"`,
        `"${assessment.assessmentDate
          ? new Date(assessment.assessmentDate).toLocaleDateString()
          : ''}"`,
        `"${assessment.duration || ''}"`,
        `"${assessment.description || ''}"`
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

  printAssessmentList() {
    const printWindow = window.open('', '_blank');
    const assessmentsTable = document.getElementById('assessmentsTable').cloneNode(true);

    // Remove action column
    assessmentsTable.querySelectorAll('th:last-child, td:last-child').forEach(el => el.remove());
    assessmentsTable.querySelectorAll('th:first-child, td:first-child').forEach(el => el.remove());

    printWindow.document.write(`
      <html>
        <head>
          <title>Assessments List</title>
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
            <h2 class="text-center mb-4">Assessments List</h2>
            <p class="text-center">Generated on: ${new Date().toLocaleDateString()}</p>
            ${assessmentsTable.outerHTML}
          </div>
          <script>window.print(); window.close();</script>
        </body>
      </html>
    `);
    printWindow.document.close();
  }
};
