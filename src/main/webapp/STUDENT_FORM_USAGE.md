# Student Form Component Usage Guide

## Overview

The `StudentForm` component provides a comprehensive form interface for creating and editing student records. It includes validation, API integration, and seamless integration with the Students management interface.

## Features

- ✅ **Create new students** with full validation
- ✅ **Edit existing students** with pre-populated data
- ✅ **Real-time form validation** with user-friendly error messages
- ✅ **API integration** for saving and loading student data
- ✅ **Bootstrap modal interface** for better UX
- ✅ **Responsive design** that works on all devices
- ✅ **Custom validation rules** including duplicate checking
- ✅ **Auto-generation** of student IDs for new students

## Basic Usage

### Creating a New Student

```javascript
import { StudentForm } from './js/components/student-form.js';

// Create form container
const formContainer = document.createElement('div');
document.body.appendChild(formContainer);

// Initialize form in create mode
const studentForm = new StudentForm(formContainer, {
  mode: 'create',
  onSave: (savedStudent) => {
    console.log('Student created:', savedStudent);
    // Refresh your students list here
    document.body.removeChild(formContainer);
  },
  onCancel: () => {
    document.body.removeChild(formContainer);
  }
});

// Show the form
await studentForm.init();
```

### Editing an Existing Student

```javascript
import { StudentForm } from './js/components/student-form.js';

// Create form container
const formContainer = document.createElement('div');
document.body.appendChild(formContainer);

// Initialize form in edit mode
const studentForm = new StudentForm(formContainer, {
  mode: 'edit',
  studentId: 'existing-student-id',
  onSave: (savedStudent) => {
    console.log('Student updated:', savedStudent);
    // Refresh your students list here
    document.body.removeChild(formContainer);
  },
  onCancel: () => {
    document.body.removeChild(formContainer);
  }
});

// Show the form
await studentForm.init();
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `mode` | string | `'create'` | Form mode: `'create'` or `'edit'` |
| `studentId` | string | `null` | Student ID for edit mode |
| `onSave` | function | `null` | Callback when student is saved |
| `onCancel` | function | `null` | Callback when form is cancelled |

## Form Fields

The student form includes the following fields:

### Required Fields
- **Student ID**: Format `STU####` (e.g., STU0001)
- **First Name**: 2-50 characters
- **Last Name**: 2-50 characters
- **Email**: Valid email address
- **Class Room**: Selected from predefined options
- **Major**: Selected from predefined options
- **Status**: Student status (Active, Inactive, etc.)
- **Enrollment Date**: Valid date, not in future

### Optional Fields
- **Phone**: Valid phone number format
- **Address**: Up to 200 characters

## Validation Rules

The form implements comprehensive validation:

### Client-Side Validation
- Required field validation
- Format validation (email, phone, student ID pattern)
- Length validation (min/max characters)
- Date validation (enrollment date)

### Server-Side Validation
- Duplicate student ID checking
- Duplicate email checking
- Business rule validation

### Custom Validation
- Student ID uniqueness
- Email uniqueness (except for current student in edit mode)
- Enrollment date cannot be in the future

## API Integration

The form integrates with the following API endpoints:

### Create Student
```
POST /api/v1/students
```

### Update Student
```
PUT /api/v1/students/{id}
```

### Load Student (for editing)
```
GET /api/v1/students/{id}
```

### Validation Endpoints
```
GET /api/v1/students/check-student-id/{studentId}
GET /api/v1/students/check-email/{email}
```

## Error Handling

The form provides comprehensive error handling:

### Validation Errors
- Field-level error messages
- Real-time validation feedback
- Server-side validation error display

### API Errors
- Network error handling
- Server error messages
- User-friendly error notifications

### Example Error Response
```javascript
{
  "status": 422,
  "data": {
    "fieldErrors": {
      "email": ["Email address already exists"],
      "studentId": ["Student ID must follow STU#### format"]
    }
  }
}
```

## Integration with Students Component

The form is designed to work seamlessly with the Students management component:

```javascript
// In Students component
async handleStudentAction(action, studentId = null) {
  switch (action) {
    case 'add':
      await this.showStudentForm('create');
      break;
    case 'edit':
      await this.showStudentForm('edit', studentId);
      break;
  }
}

async showStudentForm(mode, studentId = null) {
  const { StudentForm } = await import('./student-form.js');
  
  const formContainer = document.createElement('div');
  document.body.appendChild(formContainer);
  
  const studentForm = new StudentForm(formContainer, {
    mode: mode,
    studentId: studentId,
    onSave: (savedStudent) => {
      this.loadStudents(); // Refresh list
      document.body.removeChild(formContainer);
    },
    onCancel: () => {
      document.body.removeChild(formContainer);
    }
  });
  
  await studentForm.init();
}
```

## Programmatic Usage

### Setting Form Data
```javascript
studentForm.setData({
  studentId: 'STU0001',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@example.com',
  // ... other fields
});
```

### Getting Form Data
```javascript
const formData = studentForm.getData();
console.log('Current form data:', formData);
```

### Manual Validation
```javascript
const isValid = await studentForm.validateForm();
if (!isValid) {
  console.log('Validation errors:', studentForm.data.errors);
}
```

### Checking for Unsaved Changes
```javascript
if (studentForm.hasUnsavedChanges()) {
  const confirmLeave = confirm('You have unsaved changes. Are you sure you want to leave?');
  if (!confirmLeave) return;
}
```

## Styling and Customization

The form uses Bootstrap 5 classes and can be customized with CSS:

### Custom CSS Classes
- `.student-form-container`: Main form container
- `.is-invalid`: Invalid form fields
- `.invalid-feedback`: Error message styling

### Modal Customization
The form uses Bootstrap modal which can be styled with standard Bootstrap modal classes.

## Testing

The component includes comprehensive tests:

```bash
# Run tests (when npm is available)
npm test -- student-form.test.js

# Run with coverage
npm run test:coverage
```

### Test Coverage
- Component initialization
- Form rendering (create/edit modes)
- Data management (set/get/reset)
- Validation logic
- Error handling
- User interactions

## Browser Support

The component supports all modern browsers:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Dependencies

- Bootstrap 5.3+ (for modal and styling)
- Axios (for API calls)
- Custom validation utility
- Custom template manager
- Notification service

## Troubleshooting

### Common Issues

1. **Modal not showing**: Ensure Bootstrap JS is loaded
2. **Validation not working**: Check validation utility import
3. **API calls failing**: Verify API endpoints and authentication
4. **Form not submitting**: Check for JavaScript errors in console

### Debug Mode
Enable debug logging:
```javascript
const studentForm = new StudentForm(container, {
  debug: true, // Enable debug logging
  // ... other options
});
```

## Examples

See `js/examples/student-form-integration.js` for complete usage examples including:
- Creating students
- Editing students
- Validation demonstrations
- Error handling examples
- Form population examples

## Requirements Fulfilled

This implementation fulfills the following requirements from task 6.2:

✅ **Create student creation form with all required fields**
- Complete form with all necessary student fields
- Proper field types and validation
- Bootstrap styling for professional appearance

✅ **Implement form validation for student data**
- Client-side validation with real-time feedback
- Server-side validation integration
- Custom validation rules for business logic
- User-friendly error messages

✅ **Add student editing functionality with pre-populated data**
- Edit mode with automatic data loading
- Pre-populated form fields
- Proper handling of existing student data
- Update API integration

✅ **Create form submission with API integration**
- POST API for creating new students
- PUT API for updating existing students
- Proper error handling and user feedback
- Success callbacks for UI updates

The implementation provides a complete, production-ready student form component that integrates seamlessly with the existing Students management interface.