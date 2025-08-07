/**
 * Student Form Component
 * Handles student creation and editing with validation
 */

import { validator } from '../utils/validation.js';
import { StudentService } from '../services/studentService.js';
import { NotificationService } from '../services/notification.js';

export class StudentForm {
  constructor(container, options = {}) {
    this.container = container;
    this.studentService = new StudentService();
    this.notificationService = new NotificationService();
    
    this.options = {
      mode: 'create', // 'create' or 'edit'
      studentId: null,
      onSave: null,
      onCancel: null,
      ...options
    };

    this.data = {
      student: this.getEmptyStudent(),
      loading: false,
      saving: false,
      errors: {}
    };

    this.validationSchema = {
      firstName: ['required', 'minLength:2', 'maxLength:50'],
      lastName: ['required', 'minLength:2', 'maxLength:50'],
      email: ['required', 'email'],
      phone: ['phone'],
      studentId: ['required', 'pattern:^STU\\d{4}$'],
      classRoom: ['required'],
      major: ['required'],
      status: ['required'],
      address: ['maxLength:200'],
      enrollmentDate: ['required', 'date']
    };
  }

  /**
   * Get empty student object
   */
  getEmptyStudent() {
    return {
      id: null,
      studentId: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      classRoom: '',
      major: '',
      status: 'ACTIVE',
      enrollmentDate: new Date().toISOString().split('T')[0]
    };
  }

  /**
   * Render the student form
   */
  async render() {
    const isEdit = this.options.mode === 'edit';
    const title = isEdit ? 'Edit Student' : 'Add New Student';
    
    return `
      <div class="student-form-container">
        <div class="modal fade" id="studentFormModal" tabindex="-1" aria-hidden="true">
          <div class="modal-dialog modal-lg">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">
                  <i class="fas fa-user-graduate me-2"></i>
                  ${title}
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              
              <form id="studentForm" novalidate>
                <div class="modal-body">
                  ${this.data.loading ? this.renderLoading() : this.renderFormFields()}
                </div>
                
                <div class="modal-footer">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                    Cancel
                  </button>
                  <button type="submit" class="btn btn-primary" ${this.data.saving ? 'disabled' : ''}>
                    ${this.data.saving ? '<span class="spinner-border spinner-border-sm me-2"></span>' : ''}
                    <i class="fas fa-save me-1"></i>
                    ${isEdit ? 'Update Student' : 'Create Student'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  /**
   * Render loading state
   */
  renderLoading() {
    return `
      <div class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="text-muted">Loading student data...</p>
      </div>
    `;
  }

  /**
   * Render form fields
   */
  renderFormFields() {
    const student = this.data.student;
    
    return `
      <div class="row g-3">
        <!-- Student ID -->
        <div class="col-md-6">
          <label for="studentId" class="form-label">
            Student ID <span class="text-danger">*</span>
          </label>
          <input type="text" class="form-control ${this.getFieldClass('studentId')}" 
                 id="studentId" name="studentId" value="${student.studentId}"
                 placeholder="STU0001" pattern="^STU\\d{4}$" required>
          <div class="invalid-feedback">${this.data.errors.studentId || ''}</div>
          <div class="form-text">Format: STU followed by 4 digits (e.g., STU0001)</div>
        </div>

        <!-- Status -->
        <div class="col-md-6">
          <label for="status" class="form-label">
            Status <span class="text-danger">*</span>
          </label>
          <select class="form-select ${this.getFieldClass('status')}" 
                  id="status" name="status" required>
            <option value="">Select Status</option>
            <option value="ACTIVE" ${student.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
            <option value="INACTIVE" ${student.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
            <option value="GRADUATED" ${student.status === 'GRADUATED' ? 'selected' : ''}>Graduated</option>
            <option value="SUSPENDED" ${student.status === 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
          </select>
          <div class="invalid-feedback">${this.data.errors.status || ''}</div>
        </div>

        <!-- First Name -->
        <div class="col-md-6">
          <label for="firstName" class="form-label">
            First Name <span class="text-danger">*</span>
          </label>
          <input type="text" class="form-control ${this.getFieldClass('firstName')}" 
                 id="firstName" name="firstName" value="${student.firstName}"
                 minlength="2" maxlength="50" required>
          <div class="invalid-feedback">${this.data.errors.firstName || ''}</div>
        </div>

        <!-- Last Name -->
        <div class="col-md-6">
          <label for="lastName" class="form-label">
            Last Name <span class="text-danger">*</span>
          </label>
          <input type="text" class="form-control ${this.getFieldClass('lastName')}" 
                 id="lastName" name="lastName" value="${student.lastName}"
                 minlength="2" maxlength="50" required>
          <div class="invalid-feedback">${this.data.errors.lastName || ''}</div>
        </div>

        <!-- Email -->
        <div class="col-md-6">
          <label for="email" class="form-label">
            Email Address <span class="text-danger">*</span>
          </label>
          <input type="email" class="form-control ${this.getFieldClass('email')}" 
                 id="email" name="email" value="${student.email}" required>
          <div class="invalid-feedback">${this.data.errors.email || ''}</div>
        </div>

        <!-- Phone -->
        <div class="col-md-6">
          <label for="phone" class="form-label">Phone Number</label>
          <input type="tel" class="form-control ${this.getFieldClass('phone')}" 
                 id="phone" name="phone" value="${student.phone}"
                 placeholder="+1234567890">
          <div class="invalid-feedback">${this.data.errors.phone || ''}</div>
        </div>

        <!-- Class Room -->
        <div class="col-md-6">
          <label for="classRoom" class="form-label">
            Class Room <span class="text-danger">*</span>
          </label>
          <select class="form-select ${this.getFieldClass('classRoom')}" 
                  id="classRoom" name="classRoom" required>
            <option value="">Select Class</option>
            <option value="10A" ${student.classRoom === '10A' ? 'selected' : ''}>10A</option>
            <option value="10B" ${student.classRoom === '10B' ? 'selected' : ''}>10B</option>
            <option value="11A" ${student.classRoom === '11A' ? 'selected' : ''}>11A</option>
            <option value="11B" ${student.classRoom === '11B' ? 'selected' : ''}>11B</option>
            <option value="12A" ${student.classRoom === '12A' ? 'selected' : ''}>12A</option>
            <option value="12B" ${student.classRoom === '12B' ? 'selected' : ''}>12B</option>
          </select>
          <div class="invalid-feedback">${this.data.errors.classRoom || ''}</div>
        </div>

        <!-- Major -->
        <div class="col-md-6">
          <label for="major" class="form-label">
            Major <span class="text-danger">*</span>
          </label>
          <select class="form-select ${this.getFieldClass('major')}" 
                  id="major" name="major" required>
            <option value="">Select Major</option>
            <option value="SCIENCE" ${student.major === 'SCIENCE' ? 'selected' : ''}>Science</option>
            <option value="SOCIAL" ${student.major === 'SOCIAL' ? 'selected' : ''}>Social Studies</option>
            <option value="LANGUAGE" ${student.major === 'LANGUAGE' ? 'selected' : ''}>Language</option>
            <option value="ARTS" ${student.major === 'ARTS' ? 'selected' : ''}>Arts</option>
            <option value="MATHEMATICS" ${student.major === 'MATHEMATICS' ? 'selected' : ''}>Mathematics</option>
          </select>
          <div class="invalid-feedback">${this.data.errors.major || ''}</div>
        </div>

        <!-- Enrollment Date -->
        <div class="col-md-6">
          <label for="enrollmentDate" class="form-label">
            Enrollment Date <span class="text-danger">*</span>
          </label>
          <input type="date" class="form-control ${this.getFieldClass('enrollmentDate')}" 
                 id="enrollmentDate" name="enrollmentDate" value="${student.enrollmentDate}" required>
          <div class="invalid-feedback">${this.data.errors.enrollmentDate || ''}</div>
        </div>

        <!-- Address -->
        <div class="col-12">
          <label for="address" class="form-label">Address</label>
          <textarea class="form-control ${this.getFieldClass('address')}" 
                    id="address" name="address" rows="3" maxlength="200"
                    placeholder="Enter student's address">${student.address}</textarea>
          <div class="invalid-feedback">${this.data.errors.address || ''}</div>
          <div class="form-text">Maximum 200 characters</div>
        </div>
      </div>
    `;
  }

  /**
   * Get CSS class for form field based on validation state
   */
  getFieldClass(fieldName) {
    if (this.data.errors[fieldName]) {
      return 'is-invalid';
    }
    return '';
  }

  /**
   * Initialize the form
   */
  async init() {
    // Render the form
    this.container.innerHTML = await this.render();
    
    // Load student data if editing
    if (this.options.mode === 'edit' && this.options.studentId) {
      await this.loadStudent(this.options.studentId);
    }

    // Setup event listeners
    this.attachEventListeners();
    
    // Show the modal
    this.showModal();
  }

  /**
   * Load student data for editing
   */
  async loadStudent(studentId) {
    try {
      this.data.loading = true;
      this.updateUI();

      const student = await this.studentService.getStudentById(studentId);
      this.data.student = {
        ...this.getEmptyStudent(),
        ...student,
        enrollmentDate: student.enrollmentDate ? 
          new Date(student.enrollmentDate).toISOString().split('T')[0] : 
          new Date().toISOString().split('T')[0]
      };

      // Store original student data for comparison
      this.originalStudent = { ...this.data.student };

    } catch (error) {
      console.error('Error loading student:', error);
      this.notificationService.showApiError(error);
      
      // Close modal on error
      this.hideModal();
    } finally {
      this.data.loading = false;
      this.updateUI();
    }
  }

  /**
   * Attach event listeners
   */
  attachEventListeners() {
    const form = this.container.querySelector('#studentForm');
    const modal = this.container.querySelector('#studentFormModal');
    
    if (!form || !modal) return;

    // Form submission
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      this.handleSubmit();
    });

    // Real-time validation
    validator.setupRealTimeValidation(form);

    // Modal events
    modal.addEventListener('hidden.bs.modal', () => {
      if (this.options.onCancel) {
        this.options.onCancel();
      }
    });

    // Form field changes
    const formFields = form.querySelectorAll('input, select, textarea');
    formFields.forEach(field => {
      field.addEventListener('change', (e) => {
        this.data.student[e.target.name] = e.target.value;
        
        // Clear field error when user starts typing
        if (this.data.errors[e.target.name]) {
          delete this.data.errors[e.target.name];
          e.target.classList.remove('is-invalid');
          const feedback = e.target.nextElementSibling;
          if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.textContent = '';
          }
        }
      });

      field.addEventListener('input', (e) => {
        this.data.student[e.target.name] = e.target.value;
      });
    });

    // Generate student ID if empty (for create mode)
    if (this.options.mode === 'create') {
      const studentIdField = form.querySelector('#studentId');
      if (studentIdField && !studentIdField.value) {
        this.generateStudentId();
      }
    }
  }

  /**
   * Generate a unique student ID
   */
  async generateStudentId() {
    try {
      // Generate a random 4-digit number
      const randomNum = Math.floor(1000 + Math.random() * 9000);
      const studentId = `STU${randomNum}`;
      
      // Check if it already exists (simplified - in real app, check with API)
      this.data.student.studentId = studentId;
      
      const studentIdField = this.container.querySelector('#studentId');
      if (studentIdField) {
        studentIdField.value = studentId;
      }
    } catch (error) {
      console.error('Error generating student ID:', error);
    }
  }

  /**
   * Handle form submission
   */
  async handleSubmit() {
    try {
      // Validate form
      const isValid = await this.validateForm();
      if (!isValid) {
        return;
      }

      this.data.saving = true;
      this.updateUI();

      // Prepare data for API
      const studentData = { ...this.data.student };
      
      // Convert enrollment date to proper format
      if (studentData.enrollmentDate) {
        studentData.enrollmentDate = new Date(studentData.enrollmentDate).toISOString();
      }

      let savedStudent;
      if (this.options.mode === 'edit') {
        savedStudent = await this.studentService.updateStudent(studentData.id, studentData);
        this.notificationService.showSuccess('Student updated successfully');
      } else {
        // Remove id for create
        delete studentData.id;
        savedStudent = await this.studentService.createStudent(studentData);
        this.notificationService.showSuccess('Student created successfully');
      }

      // Call success callback
      if (this.options.onSave) {
        this.options.onSave(savedStudent);
      }

      // Hide modal
      this.hideModal();

    } catch (error) {
      console.error('Error saving student:', error);
      
      if (error.status === 422 && error.data && error.data.fieldErrors) {
        // Handle validation errors from server
        this.data.errors = error.data.fieldErrors;
        this.updateUI();
        this.notificationService.showError('Please correct the highlighted fields');
      } else {
        this.notificationService.showApiError(error);
      }
    } finally {
      this.data.saving = false;
      this.updateUI();
    }
  }

  /**
   * Validate form data
   */
  async validateForm() {
    try {
      this.data.errors = await validator.validateForm(this.data.student, this.validationSchema);
      
      // Additional custom validations
      await this.performCustomValidations();
      
      this.updateUI();
      
      return Object.keys(this.data.errors).length === 0;
    } catch (error) {
      console.error('Validation error:', error);
      return false;
    }
  }

  /**
   * Perform custom validations
   */
  async performCustomValidations() {
    // Check for duplicate student ID (only for create mode or if ID changed)
    if (this.options.mode === 'create' || 
        (this.options.mode === 'edit' && this.data.student.studentId !== this.originalStudent?.studentId)) {
      
      try {
        const excludeId = this.options.mode === 'edit' ? this.data.student.id : null;
        const exists = await this.studentService.checkStudentIdExists(this.data.student.studentId, excludeId);
        if (exists) {
          this.data.errors.studentId = ['Student ID already exists'];
        }
      } catch (error) {
        // If API call fails, skip this validation
        console.warn('Could not check student ID uniqueness:', error);
      }
    }

    // Check for duplicate email
    if (this.data.student.email) {
      try {
        const excludeId = this.options.mode === 'edit' ? this.data.student.id : null;
        const exists = await this.studentService.checkEmailExists(this.data.student.email, excludeId);
        if (exists) {
          this.data.errors.email = ['Email address already exists'];
        }
      } catch (error) {
        // If API call fails, skip this validation
        console.warn('Could not check email uniqueness:', error);
      }
    }

    // Validate enrollment date is not in the future
    if (this.data.student.enrollmentDate) {
      const enrollmentDate = new Date(this.data.student.enrollmentDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      if (enrollmentDate > today) {
        this.data.errors.enrollmentDate = ['Enrollment date cannot be in the future'];
      }
    }
  }

  /**
   * Update UI elements
   */
  updateUI() {
    const modalBody = this.container.querySelector('.modal-body');
    const submitBtn = this.container.querySelector('button[type="submit"]');
    
    if (modalBody) {
      modalBody.innerHTML = this.data.loading ? this.renderLoading() : this.renderFormFields();
    }

    if (submitBtn) {
      submitBtn.disabled = this.data.saving;
      const isEdit = this.options.mode === 'edit';
      submitBtn.innerHTML = `
        ${this.data.saving ? '<span class="spinner-border spinner-border-sm me-2"></span>' : ''}
        <i class="fas fa-save me-1"></i>
        ${isEdit ? 'Update Student' : 'Create Student'}
      `;
    }

    // Re-attach event listeners if form was re-rendered
    if (!this.data.loading) {
      this.attachEventListeners();
    }
  }

  /**
   * Show the modal
   */
  showModal() {
    const modal = this.container.querySelector('#studentFormModal');
    if (modal) {
      const bsModal = new bootstrap.Modal(modal);
      bsModal.show();
    }
  }

  /**
   * Hide the modal
   */
  hideModal() {
    const modal = this.container.querySelector('#studentFormModal');
    if (modal) {
      const bsModal = bootstrap.Modal.getInstance(modal);
      if (bsModal) {
        bsModal.hide();
      }
    }
  }

  /**
   * Reset form to initial state
   */
  reset() {
    this.data.student = this.getEmptyStudent();
    this.data.errors = {};
    this.data.loading = false;
    this.data.saving = false;
    this.updateUI();
  }

  /**
   * Set form data
   */
  setData(studentData) {
    this.data.student = {
      ...this.getEmptyStudent(),
      ...studentData,
      enrollmentDate: studentData.enrollmentDate ? 
        new Date(studentData.enrollmentDate).toISOString().split('T')[0] : 
        new Date().toISOString().split('T')[0]
    };
    this.updateUI();
  }

  /**
   * Get form data
   */
  getData() {
    return { ...this.data.student };
  }

  /**
   * Check if form has unsaved changes
   */
  hasUnsavedChanges() {
    if (this.options.mode === 'create') {
      const emptyStudent = this.getEmptyStudent();
      return JSON.stringify(this.data.student) !== JSON.stringify(emptyStudent);
    } else {
      return JSON.stringify(this.data.student) !== JSON.stringify(this.originalStudent);
    }
  }
}