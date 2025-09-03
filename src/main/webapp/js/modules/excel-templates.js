/* global ExcelJS */
// Excel Templates Module - Real Database Structure
const ExcelTemplates = {

  // Student Template based on real database structure
  downloadStudentTemplate() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Student Template');

    // Add headers based on real database structure
    worksheet.columns = [
      { header: 'nis*', key: 'nis', width: 15 },
      { header: 'first_name*', key: 'firstName', width: 20 },
      { header: 'last_name*', key: 'lastName', width: 20 },
      { header: 'email*', key: 'email', width: 30 },
      { header: 'phone', key: 'phone', width: 15 },
      { header: 'date_of_birth', key: 'dateOfBirth', width: 15 },
      { header: 'gender', key: 'gender', width: 10 },
      { header: 'address', key: 'address', width: 40 },
      { header: 'enrollment_date', key: 'enrollmentDate', width: 15 },
      { header: 'status', key: 'status', width: 12 },
      { header: 'class_room_id', key: 'classRoomId', width: 15 },
      { header: 'parent_name', key: 'parentName', width: 25 },
      { header: 'parent_phone', key: 'parentPhone', width: 15 },
      { header: 'emergency_contact', key: 'emergencyContact', width: 25 },
      { header: 'emergency_phone', key: 'emergencyPhone', width: 15 }
    ];

    // Add sample data
    worksheet.addRow({
      nis: '2024001',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@student.sim.edu',
      phone: '+62812-3456-7890',
      dateOfBirth: '2005-01-15',
      gender: 'MALE',
      address: 'Jl. Merdeka No. 123, Jakarta',
      enrollmentDate: '2024-07-01',
      status: 'ACTIVE',
      classRoomId: '1',
      parentName: 'Robert Doe',
      parentPhone: '+62811-1111-1111',
      emergencyContact: 'Jane Doe',
      emergencyPhone: '+62822-2222-2222'
    });

    worksheet.addRow({
      nis: '2024002',
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@student.sim.edu',
      phone: '+62813-4567-8901',
      dateOfBirth: '2005-03-20',
      gender: 'FEMALE',
      address: 'Jl. Sudirman No. 456, Bandung',
      enrollmentDate: '2024-07-01',
      status: 'ACTIVE',
      classRoomId: '1',
      parentName: 'Michael Smith',
      parentPhone: '+62833-3333-3333',
      emergencyContact: 'Sarah Smith',
      emergencyPhone: '+62844-4444-4444'
    });

    this.styleTemplate(worksheet, 'Student');
    this.addInstructions(workbook, 'Student', {
      nis: 'Unique student identification number',
      first_name: 'Student first name',
      last_name: 'Student last name',
      email: 'Valid email address',
      phone: 'Phone number format: +62xxx-xxxx-xxxx',
      date_of_birth: 'Format: YYYY-MM-DD',
      gender: 'MALE or FEMALE',
      address: 'Full address',
      enrollment_date: 'Format: YYYY-MM-DD',
      status: 'ACTIVE, INACTIVE, or GRADUATED',
      class_room_id: 'Class room ID (get from class list)',
      parent_name: 'Parent/Guardian full name',
      parent_phone: 'Parent phone number',
      emergency_contact: 'Emergency contact name',
      emergency_phone: 'Emergency contact phone'
    });

    this.downloadFile(workbook, 'student_import_template.xlsx');
  },

  // Teacher Template based on real database structure
  downloadTeacherTemplate() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Teacher Template');

    worksheet.columns = [
      { header: 'nip*', key: 'nip', width: 20 },
      { header: 'first_name*', key: 'firstName', width: 20 },
      { header: 'last_name*', key: 'lastName', width: 20 },
      { header: 'email*', key: 'email', width: 30 },
      { header: 'phone', key: 'phone', width: 15 },
      { header: 'date_of_birth', key: 'dateOfBirth', width: 15 },
      { header: 'gender', key: 'gender', width: 10 },
      { header: 'address', key: 'address', width: 40 },
      { header: 'hire_date', key: 'hireDate', width: 15 },
      { header: 'status', key: 'status', width: 12 },
      { header: 'department_id', key: 'departmentId', width: 15 },
      { header: 'specialization', key: 'specialization', width: 25 },
      { header: 'qualification', key: 'qualification', width: 30 },
      { header: 'experience_years', key: 'experienceYears', width: 15 }
    ];

    worksheet.addRow({
      nip: '198501012010011001',
      firstName: 'Ahmad',
      lastName: 'Wijaya',
      email: 'ahmad.wijaya@sim.edu',
      phone: '+62812-1111-2222',
      dateOfBirth: '1985-01-01',
      gender: 'MALE',
      address: 'Jl. Pendidikan No. 789, Jakarta',
      hireDate: '2010-01-01',
      status: 'ACTIVE',
      departmentId: 1,
      specialization: 'Matematika',
      qualification: 'S1 Pendidikan Matematika',
      experienceYears: 14
    });

    worksheet.addRow({
      nip: '198703152012012002',
      firstName: 'Siti',
      lastName: 'Nurhaliza',
      email: 'siti.nurhaliza@sim.edu',
      phone: '+62813-2222-3333',
      dateOfBirth: '1987-03-15',
      gender: 'FEMALE',
      address: 'Jl. Guru No. 456, Surabaya',
      hireDate: '2012-01-15',
      status: 'ACTIVE',
      departmentId: 2,
      specialization: 'Bahasa Indonesia',
      qualification: 'S1 Pendidikan Bahasa Indonesia',
      experienceYears: 12
    });

    this.styleTemplate(worksheet, 'Teacher');
    this.addInstructions(workbook, 'Teacher', {
      nip: 'Unique teacher identification number',
      first_name: 'Teacher first name',
      last_name: 'Teacher last name',
      email: 'Valid email address',
      phone: 'Phone number format: +62xxx-xxxx-xxxx',
      date_of_birth: 'Format: YYYY-MM-DD',
      gender: 'MALE or FEMALE',
      address: 'Full address',
      hire_date: 'Format: YYYY-MM-DD',
      status: 'ACTIVE or INACTIVE',
      department_id: 'Department ID (get from department list)',
      specialization: 'Teaching specialization',
      qualification: 'Educational qualification',
      experience_years: 'Years of teaching experience'
    });

    this.downloadFile(workbook, 'teacher_import_template.xlsx');
  },

  // Attendance Template
  downloadAttendanceTemplate() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Attendance Template');

    worksheet.columns = [
      { header: 'student_id*', key: 'studentId', width: 15 },
      { header: 'date*', key: 'date', width: 15 },
      { header: 'status*', key: 'status', width: 12 },
      { header: 'check_in_time', key: 'checkInTime', width: 15 },
      { header: 'check_out_time', key: 'checkOutTime', width: 15 },
      { header: 'notes', key: 'notes', width: 30 }
    ];

    worksheet.addRow({
      studentId: '1',
      date: '2024-09-03',
      status: 'PRESENT',
      checkInTime: '07:30:00',
      checkOutTime: '14:30:00',
      notes: 'On time'
    });

    worksheet.addRow({
      studentId: '2',
      date: '2024-09-03',
      status: 'LATE',
      checkInTime: '08:15:00',
      checkOutTime: '14:30:00',
      notes: 'Late 15 minutes'
    });

    this.styleTemplate(worksheet, 'Attendance');
    this.addInstructions(workbook, 'Attendance', {
      student_id: 'Student ID (get from student list)',
      date: 'Format: YYYY-MM-DD',
      status: 'PRESENT, ABSENT, LATE, or EXCUSED',
      check_in_time: 'Format: HH:MM:SS (optional)',
      check_out_time: 'Format: HH:MM:SS (optional)',
      notes: 'Additional notes (optional)'
    });

    this.downloadFile(workbook, 'attendance_import_template.xlsx');
  },

  // Assessment Template
  downloadAssessmentTemplate() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Assessment Template');

    worksheet.columns = [
      { header: 'title*', key: 'title', width: 30 },
      { header: 'description', key: 'description', width: 40 },
      { header: 'type*', key: 'type', width: 15 },
      { header: 'subject*', key: 'subject', width: 20 },
      { header: 'class_room_id*', key: 'classRoomId', width: 15 },
      { header: 'max_score*', key: 'maxScore', width: 12 },
      { header: 'due_date', key: 'dueDate', width: 15 },
      { header: 'status', key: 'status', width: 12 }
    ];

    worksheet.addRow({
      title: 'Ujian Tengah Semester Matematika',
      description: 'Ujian tengah semester untuk mata pelajaran Matematika kelas X',
      type: 'EXAM',
      subject: 'Matematika',
      classRoomId: '1',
      maxScore: 100,
      dueDate: '2024-10-15',
      status: 'ACTIVE'
    });

    worksheet.addRow({
      title: 'Tugas Harian Bahasa Indonesia',
      description: 'Tugas harian membuat karangan deskriptif',
      type: 'ASSIGNMENT',
      subject: 'Bahasa Indonesia',
      classRoomId: '1',
      maxScore: 80,
      dueDate: '2024-09-20',
      status: 'ACTIVE'
    });

    this.styleTemplate(worksheet, 'Assessment');
    this.addInstructions(workbook, 'Assessment', {
      title: 'Assessment title',
      description: 'Detailed description (optional)',
      type: 'EXAM, QUIZ, ASSIGNMENT, or PROJECT',
      subject: 'Subject name',
      class_room_id: 'Class room ID (get from class list)',
      max_score: 'Maximum possible score',
      due_date: 'Format: YYYY-MM-DD (optional)',
      status: 'ACTIVE, INACTIVE, or COMPLETED'
    });

    this.downloadFile(workbook, 'assessment_import_template.xlsx');
  },

  // Utility methods
  styleTemplate(worksheet, _entityName) {
    // Style header row
    const headerRow = worksheet.getRow(1);
    headerRow.font = { bold: true, color: { argb: 'FFFFFF' } };
    headerRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: '366092' }
    };
    headerRow.alignment = { horizontal: 'center', vertical: 'middle' };
    headerRow.height = 25;

    // Add borders to all cells
    worksheet.eachRow((row, _rowNumber) => {
      row.eachCell((cell) => {
        cell.border = {
          top: { style: 'thin' },
          left: { style: 'thin' },
          bottom: { style: 'thin' },
          right: { style: 'thin' }
        };
      });
    });

    // Auto-fit columns
    worksheet.columns.forEach(column => {
      if (column.width < 10) {
        column.width = 10;
      }
    });
  },

  addInstructions(workbook, entityName, fieldDescriptions) {
    const instructionSheet = workbook.addWorksheet('Instructions');

    instructionSheet.addRow([`${entityName} Import Template Instructions`]);
    instructionSheet.addRow([]);
    instructionSheet.addRow(['Field Name', 'Description', 'Required']);

    Object.entries(fieldDescriptions).forEach(([field, description]) => {
      const isRequired = field.includes('*') || ['id', 'name', 'email'].some(req => field.includes(req));
      instructionSheet.addRow([field, description, isRequired ? 'Yes' : 'No']);
    });

    instructionSheet.addRow([]);
    instructionSheet.addRow(['Important Notes:']);
    instructionSheet.addRow(['1. Fields marked with * are required']);
    instructionSheet.addRow(['2. Follow the exact format specified']);
    instructionSheet.addRow(['3. Remove sample data before importing']);
    instructionSheet.addRow(['4. Ensure all required fields are filled']);
    instructionSheet.addRow(['5. Check data validation before import']);

    // Style instructions
    const titleRow = instructionSheet.getRow(1);
    titleRow.font = { bold: true, size: 14 };
    titleRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'E7E6E6' }
    };

    const headerRow = instructionSheet.getRow(3);
    headerRow.font = { bold: true };
    headerRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'D9D9D9' }
    };

    instructionSheet.columns = [
      { width: 20 },
      { width: 50 },
      { width: 12 }
    ];
  },

  downloadFile(workbook, filename) {
    workbook.xlsx.writeBuffer().then(buffer => {
      const blob = new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }
};

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ExcelTemplates;
}
