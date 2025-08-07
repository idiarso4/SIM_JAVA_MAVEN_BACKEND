/**
 * Student Form Component Tests
 */

import { StudentForm } from '../js/components/student-form.js';

// Mock dependencies
jest.mock('../js/services/api.js');
jest.mock('../js/services/notification.js');
jest.mock('../js/utils/validation.js');

describe('StudentForm', () => {
  let container;
  let studentForm;

  beforeEach(() => {
    // Create container element
    container = document.createElement('div');
    document.body.appendChild(container);

    // Mock Bootstrap modal
    global.bootstrap = {
      Modal: jest.fn().mockImplementation(() => ({
        show: jest.fn(),
        hide: jest.fn()
      }))
    };
  });

  afterEach(() => {
    if (container && container.parentNode) {
      container.parentNode.removeChild(container);
    }
  });

  describe('Initialization', () => {
    test('should create StudentForm instance with default options', () => {
      studentForm = new StudentForm(container);
      
      expect(studentForm.container).toBe(container);
      expect(studentForm.options.mode).toBe('create');
      expect(studentForm.options.studentId).toBeNull();
    });

    test('should create StudentForm instance with custom options', () => {
      const options = {
        mode: 'edit',
        studentId: '123',
        onSave: jest.fn(),
        onCancel: jest.fn()
      };
      
      studentForm = new StudentForm(container, options);
      
      expect(studentForm.options.mode).toBe('edit');
      expect(studentForm.options.studentId).toBe('123');
      expect(studentForm.options.onSave).toBe(options.onSave);
      expect(studentForm.options.onCancel).toBe(options.onCancel);
    });
  });

  describe('Data Management', () => {
    beforeEach(() => {
      studentForm = new StudentForm(container);
    });

    test('should return empty student object', () => {
      const emptyStudent = studentForm.getEmptyStudent();
      
      expect(emptyStudent).toEqual({
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
        enrollmentDate: expect.any(String)
      });
    });

    test('should set and get form data', () => {
      const testData = {
        id: '123',
        studentId: 'STU0001',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phone: '+1234567890',
        address: '123 Main St',
        classRoom: '10A',
        major: 'SCIENCE',
        status: 'ACTIVE',
        enrollmentDate: '2024-01-15T00:00:00.000Z'
      };

      studentForm.setData(testData);
      const formData = studentForm.getData();

      expect(formData.id).toBe('123');
      expect(formData.studentId).toBe('STU0001');
      expect(formData.firstName).toBe('John');
      expect(formData.lastName).toBe('Doe');
      expect(formData.email).toBe('john.doe@example.com');
    });

    test('should reset form to initial state', () => {
      // Set some data first
      studentForm.setData({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com'
      });

      // Reset form
      studentForm.reset();

      const formData = studentForm.getData();
      expect(formData.firstName).toBe('');
      expect(formData.lastName).toBe('');
      expect(formData.email).toBe('');
      expect(studentForm.data.errors).toEqual({});
      expect(studentForm.data.loading).toBe(false);
      expect(studentForm.data.saving).toBe(false);
    });
  });

  describe('Validation', () => {
    beforeEach(() => {
      studentForm = new StudentForm(container);
    });

    test('should have correct validation schema', () => {
      expect(studentForm.validationSchema).toEqual({
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
      });
    });

    test('should return correct field CSS class based on validation state', () => {
      // No errors
      expect(studentForm.getFieldClass('firstName')).toBe('');

      // With error
      studentForm.data.errors.firstName = ['First name is required'];
      expect(studentForm.getFieldClass('firstName')).toBe('is-invalid');
    });
  });

  describe('Rendering', () => {
    beforeEach(() => {
      studentForm = new StudentForm(container);
    });

    test('should render create form correctly', async () => {
      const html = await studentForm.render();
      
      expect(html).toContain('Add New Student');
      expect(html).toContain('Create Student');
      expect(html).toContain('studentFormModal');
    });

    test('should render edit form correctly', async () => {
      studentForm.options.mode = 'edit';
      const html = await studentForm.render();
      
      expect(html).toContain('Edit Student');
      expect(html).toContain('Update Student');
    });

    test('should render loading state', () => {
      const loadingHtml = studentForm.renderLoading();
      
      expect(loadingHtml).toContain('Loading student data...');
      expect(loadingHtml).toContain('spinner-border');
    });

    test('should render form fields with student data', () => {
      studentForm.data.student = {
        studentId: 'STU0001',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        phone: '+1234567890',
        classRoom: '10A',
        major: 'SCIENCE',
        status: 'ACTIVE',
        address: '123 Main St',
        enrollmentDate: '2024-01-15'
      };

      const fieldsHtml = studentForm.renderFormFields();
      
      expect(fieldsHtml).toContain('value="STU0001"');
      expect(fieldsHtml).toContain('value="John"');
      expect(fieldsHtml).toContain('value="Doe"');
      expect(fieldsHtml).toContain('value="john@example.com"');
      expect(fieldsHtml).toContain('value="+1234567890"');
      expect(fieldsHtml).toContain('selected="">Science');
      expect(fieldsHtml).toContain('123 Main St');
    });
  });

  describe('Form Interaction', () => {
    beforeEach(() => {
      studentForm = new StudentForm(container);
    });

    test('should detect unsaved changes in create mode', () => {
      // Initially no changes
      expect(studentForm.hasUnsavedChanges()).toBe(false);

      // After setting data
      studentForm.data.student.firstName = 'John';
      expect(studentForm.hasUnsavedChanges()).toBe(true);
    });

    test('should generate student ID for create mode', async () => {
      await studentForm.generateStudentId();
      
      expect(studentForm.data.student.studentId).toMatch(/^STU\d{4}$/);
    });
  });
});