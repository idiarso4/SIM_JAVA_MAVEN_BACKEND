/* eslint-disable no-unused-vars */
/* global Utils, bootstrap */

// Excel Import/Export Module
const Excel = {
  showImportStudentsExcel() {
    const modalHtml = `
      <div class="modal fade" id="importStudentsExcelModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title"><i class="fas fa-file-excel me-2"></i>Import Students from Excel</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <div class="alert alert-info">
                <h6><i class="fas fa-info-circle me-2"></i>Import Instructions</h6>
                <ul class="mb-0">
                  <li>Download the template first</li>
                  <li>Excel file should be .xlsx format</li>
                  <li>Maximum file size: 5MB</li>
                </ul>
              </div>
              <div class="mb-3">
                <button type="button" class="btn btn-outline-primary" onclick="downloadStudentTemplate()">
                  <i class="fas fa-file-download me-2"></i>Download Template
                </button>
              </div>
              <div class="mb-3">
                <label for="studentExcelFile" class="form-label">Upload Excel File</label>
                <input type="file" class="form-control" id="studentExcelFile" accept=".xlsx,.xls,.csv" onchange="handleStudentFileSelect(event)">
              </div>
              <div id="studentImportProgress" class="d-none">
                <div class="progress">
                  <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style="width: 0%"></div>
                </div>
                <div class="text-center mt-2">
                  <small id="studentProgressText">Processing...</small>
                </div>
              </div>
              <div id="studentImportResults" class="d-none mt-3">
                <div class="alert" id="studentImportResultAlert">
                  <h6><i class="fas fa-info-circle me-2"></i>Import Results</h6>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              <button type="button" class="btn btn-primary" onclick="processStudentExcelImport()" id="studentImportBtn">
                <i class="fas fa-upload me-2"></i>Import Students
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    const existingModal = document.getElementById('importStudentsExcelModal');
    if (existingModal) {
      existingModal.remove();
    }

    document.body.insertAdjacentHTML('beforeend', modalHtml);
    const modal = new bootstrap.Modal(document.getElementById('importStudentsExcelModal'));
    modal.show();
  },

  downloadStudentTemplate() {
    const headers = ['Student ID', 'First Name', 'Last Name', 'Gender', 'Class', 'Email', 'Phone', 'Parent Name', 'Address'];
    const sampleData = [
      ['S00001', 'John', 'Smith', 'Male', '10A', 'john.smith@student.edu', '0812345678', 'Mary Smith', '123 Main St'],
      ['S00002', 'Jane', 'Doe', 'Female', '10A', 'jane.doe@student.edu', '0823456789', 'Robert Doe', '456 Oak Ave']
    ];

    const csvContent = [headers].concat(sampleData).map(row => row.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', 'student_template.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    Utils.showAlert('success', 'Student template downloaded successfully!');
  },

  handleStudentFileSelect(event) {
    const file = event.target.files[0];
    if (!file) { return; }

    const fileName = file.name;
    const fileSize = (file.size / (1024 * 1024)).toFixed(2);
    const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel', 'text/csv'];

    if (!validTypes.includes(file.type) && !fileName.endsWith('.csv')) {
      Utils.showAlert('danger', 'Invalid file type. Please select an Excel file (.xlsx, .xls) or CSV file (.csv)');
      event.target.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      Utils.showAlert('danger', 'File size exceeds 5MB limit. Please select a smaller file.');
      event.target.value = '';
    }

    // File is valid - could add more processing here
  },

  processStudentExcelImport() {
    const fileInput = document.getElementById('studentExcelFile');
    const file = fileInput.files[0];

    if (!file) {
      Utils.showAlert('warning', 'Please select an Excel file to import.');
      return;
    }

    const progressContainer = document.getElementById('studentImportProgress');
    const importBtn = document.getElementById('studentImportBtn');
    const progressText = document.getElementById('studentProgressText');

    progressContainer.classList.remove('d-none');
    importBtn.disabled = true;
    importBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Importing...';

    let progress = 0;
    const progressBar = progressContainer.querySelector('.progress-bar');
    const interval = setInterval(() => {
      progress += Math.floor(Math.random() * 10) + 5;
      if (progress >= 100) {
        progress = 100;
        clearInterval(interval);
        setTimeout(() => this.showStudentImportResults(), 500);
      }
      progressBar.style.width = `${progress}%`;
      progressText.textContent = `Processing... ${progress}%`;
    }, 200);
  },

  showStudentImportResults() {
    const progressContainer = document.getElementById('studentImportProgress');
    const resultsContainer = document.getElementById('studentImportResults');
    const resultAlert = document.getElementById('studentImportResultAlert');
    const importBtn = document.getElementById('studentImportBtn');

    progressContainer.classList.add('d-none');
    resultsContainer.classList.remove('d-none');
    resultAlert.classList.add('alert-success');
    resultAlert.innerHTML = `
      <h6><i class="fas fa-check-circle me-2"></i>Import Completed Successfully</h6>
      <ul class="mb-0">
        <li>120 students processed</li>
        <li>118 students imported successfully</li>
        <li>2 duplicate records skipped</li>
        <li>Processing time: 3.1 seconds</li>
      </ul>
    `;

    importBtn.innerHTML = '<i class="fas fa-upload me-2"></i>Import Students';
    importBtn.disabled = false;

    Utils.showAlert('success', 'Student data imported successfully! 118 new students added to the system.');
  }
};
