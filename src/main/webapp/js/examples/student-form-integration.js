/**
 * Student Form Integration Example
 * Demonstrates how to use the StudentForm component with the Students component
 */

import { StudentForm } from '../components/student-form.js';
import Students from '../components/students.js';

/**
 * Example of how to integrate StudentForm with Students component
 */
export class StudentFormIntegrationExample {
  constructor() {
    this.studentsComponent = new Students();
  }

  /**
   * Example: Create new student
   */
  async createStudent() {
    // Create form container
    const formContainer = document.createElement('div');
    document.body.appendChild(formContainer);

    // Initialize student form in create mode
    const studentForm = new StudentForm(formContainer, {
      mode: 'create',
      onSave: (savedStudent) => {
        console.log('Student created:', savedStudent);
        
        // Refresh students list
        this.studentsComponent.loadStudents();
        
        // Clean up
        document.body.removeChild(formContainer);
        
        // Show success message
        alert(`Student ${savedStudent.firstName} ${savedStudent.lastName} created successfully!`);
      },
      onCancel: () => {
        console.log('Student creation cancelled');
        document.body.removeChild(formContainer);
      }
    });

    // Initialize and show the form
    await studentForm.init();
  }

  /**
   * Example: Edit existing student
   */
  async editStudent(studentId) {
    // Create form container
    const formContainer = document.createElement('div');
    document.body.appendChild(formContainer);

    // Initialize student form in edit mode
    const studentForm = new StudentForm(formContainer, {
      mode: 'edit',
      studentId: studentId,
      onSave: (savedStudent) => {
        console.log('Student updated:', savedStudent);
        
        // Refresh students list
        this.studentsComponent.loadStudents();
        
        // Clean up
        document.body.removeChild(formContainer);
        
        // Show success message
        alert(`Student ${savedStudent.firstName} ${savedStudent.lastName} updated successfully!`);
      },
      onCancel: () => {
        console.log('Student editing cancelled');
        document.body.removeChild(formContainer);
      }
    });

    // Initialize and show the form
    await studentForm.init();
  }

  /**
   * Example: Validate student data before submission
   */
  async validateStudentData() {
    const formContainer = document.createElement('div');
    document.body.appendChild(formContainer);

    const studentForm = new StudentForm(formContainer, {
      mode: 'create',
      onSave: (savedStudent) => {
        console.log('Validation passed, student saved:', savedStudent);
        document.body.removeChild(formContainer);
      },
      onCancel: () => {
        document.body.removeChild(formContainer);
      }
    });

    await studentForm.init();

    // Set some test data
    studentForm.setData({
      studentId: 'STU0001',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      phone: '+1234567890',
      classRoom: '10A',
      major: 'SCIENCE',
      status: 'ACTIVE',
      address: '123 Main Street',
      enrollmentDate: '2024-01-15'
    });

    // Validate the form
    const isValid = await studentForm.validateForm();
    console.log('Form validation result:', isValid);
    console.log('Validation errors:', studentForm.data.errors);

    return isValid;
  }

  /**
   * Example: Handle form with validation errors
   */
  async demonstrateValidationErrors() {
    const formContainer = document.createElement('div');
    document.body.appendChild(formContainer);

    const studentForm = new StudentForm(formContainer, {
      mode: 'create',
      onSave: (savedStudent) => {
        console.log('Student saved:', savedStudent);
        document.body.removeChild(formContainer);
      },
      onCancel: () => {
        document.body.removeChild(formContainer);
      }
    });

    await studentForm.init();

    // Set invalid data to demonstrate validation
    studentForm.setData({
      studentId: 'INVALID', // Should match STU\d{4} pattern
      firstName: 'J', // Too short (min 2 chars)
      lastName: '', // Required field
      email: 'invalid-email', // Invalid email format
      phone: 'abc123', // Invalid phone format
      classRoom: '', // Required field
      major: '', // Required field
      status: '', // Required field
      enrollmentDate: '2025-12-31' // Future date (invalid)
    });

    // Try to validate - should fail
    const isValid = await studentForm.validateForm();
    console.log('Validation should fail:', isValid);
    console.log('Expected validation errors:', studentForm.data.errors);

    // Clean up after demonstration
    setTimeout(() => {
      if (document.body.contains(formContainer)) {
        document.body.removeChild(formContainer);
      }
    }, 5000);

    return studentForm.data.errors;
  }

  /**
   * Example: Programmatically populate form
   */
  async populateFormExample() {
    const formContainer = document.createElement('div');
    document.body.appendChild(formContainer);

    const studentForm = new StudentForm(formContainer, {
      mode: 'create',
      onSave: (savedStudent) => {
        console.log('Pre-populated student saved:', savedStudent);
        document.body.removeChild(formContainer);
      },
      onCancel: () => {
        document.body.removeChild(formContainer);
      }
    });

    await studentForm.init();

    // Populate with sample data
    const sampleStudent = {
      studentId: 'STU0123',
      firstName: 'Alice',
      lastName: 'Johnson',
      email: 'alice.johnson@school.edu',
      phone: '+1-555-0123',
      classRoom: '11A',
      major: 'SCIENCE',
      status: 'ACTIVE',
      address: '456 Oak Avenue, Springfield, IL 62701',
      enrollmentDate: '2024-01-15'
    };

    studentForm.setData(sampleStudent);
    console.log('Form populated with:', sampleStudent);

    return studentForm;
  }
}

/**
 * Usage examples
 */
export const examples = {
  // Create a new student
  createStudent: async () => {
    const integration = new StudentFormIntegrationExample();
    await integration.createStudent();
  },

  // Edit an existing student (replace 'student-id' with actual ID)
  editStudent: async (studentId = 'student-id') => {
    const integration = new StudentFormIntegrationExample();
    await integration.editStudent(studentId);
  },

  // Validate student data
  validateData: async () => {
    const integration = new StudentFormIntegrationExample();
    return await integration.validateStudentData();
  },

  // Show validation errors
  showValidationErrors: async () => {
    const integration = new StudentFormIntegrationExample();
    return await integration.demonstrateValidationErrors();
  },

  // Populate form with sample data
  populateForm: async () => {
    const integration = new StudentFormIntegrationExample();
    return await integration.populateFormExample();
  }
};

// Console helper for testing
if (typeof window !== 'undefined') {
  window.StudentFormExamples = examples;
  console.log('Student Form Integration Examples loaded!');
  console.log('Try: StudentFormExamples.createStudent()');
  console.log('Try: StudentFormExamples.validateData()');
  console.log('Try: StudentFormExamples.showValidationErrors()');
  console.log('Try: StudentFormExamples.populateForm()');
}