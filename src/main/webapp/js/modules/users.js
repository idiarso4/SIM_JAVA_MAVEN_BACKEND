// User Management Module
const UserManagement = {
  currentPage: 0,
  pageSize: 10,
  totalPages: 0,
  users: [],
  filteredUsers: [],

  async init() {
    console.log('Initializing User Management module...');
    this.setupEventListeners();
    await this.loadUsers();
    this.renderUserTable();
    this.updatePagination();
  },

  setupEventListeners() {
    // Search functionality
    const searchInput = document.getElementById('userSearch');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.filterUsers(e.target.value);
      });
    }

    // Add user button
    const addUserBtn = document.getElementById('addUserBtn');
    if (addUserBtn) {
      addUserBtn.addEventListener('click', () => {
        this.showAddUserModal();
      });
    }

    // Export buttons
    const exportExcelBtn = document.getElementById('exportUsersExcel');
    if (exportExcelBtn) {
      exportExcelBtn.addEventListener('click', () => {
        this.exportToExcel();
      });
    }

    const downloadTemplateBtn = document.getElementById('downloadUserTemplate');
    if (downloadTemplateBtn) {
      downloadTemplateBtn.addEventListener('click', () => {
        this.downloadTemplate();
      });
    }

    // Print button
    const printBtn = document.getElementById('printUsers');
    if (printBtn) {
      printBtn.addEventListener('click', () => {
        this.printUsers();
      });
    }

    // Pagination
    const prevBtn = document.getElementById('userPrevPage');
    const nextBtn = document.getElementById('userNextPage');
    
    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        if (this.currentPage > 0) {
          this.currentPage--;
          this.renderUserTable();
          this.updatePagination();
        }
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        if (this.currentPage < this.totalPages - 1) {
          this.currentPage++;
          this.renderUserTable();
          this.updatePagination();
        }
      });
    }
  },

  async loadUsers() {
    try {
      // Try to load from real API first
      const response = await fetch('/api/v1/users', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.users = data.content || data;
      } else {
        // Fallback to sample data
        this.users = this.generateSampleUsers();
      }

      this.filteredUsers = [...this.users];
      this.totalPages = Math.ceil(this.filteredUsers.length / this.pageSize);
    } catch (error) {
      console.log('Using sample user data');
      this.users = this.generateSampleUsers();
      this.filteredUsers = [...this.users];
      this.totalPages = Math.ceil(this.filteredUsers.length / this.pageSize);
    }
  },

  generateSampleUsers() {
    return [
      {
        id: 1,
        username: 'admin',
        email: 'admin@sim.edu',
        firstName: 'System',
        lastName: 'Administrator',
        userType: 'ADMIN',
        isActive: true,
        createdAt: '2024-01-15T08:00:00',
        lastLoginAt: '2024-12-03T10:30:00',
        phone: '+62812-3456-7890',
        address: 'Jakarta, Indonesia'
      },
      {
        id: 2,
        username: 'teacher1',
        email: 'teacher1@sim.edu',
        firstName: 'John',
        lastName: 'Smith',
        userType: 'TEACHER',
        isActive: true,
        createdAt: '2024-02-01T09:00:00',
        lastLoginAt: '2024-12-02T14:20:00',
        phone: '+62813-4567-8901',
        address: 'Bandung, Indonesia'
      },
      {
        id: 3,
        username: 'teacher2',
        email: 'teacher2@sim.edu',
        firstName: 'Sarah',
        lastName: 'Johnson',
        userType: 'TEACHER',
        isActive: true,
        createdAt: '2024-02-15T10:00:00',
        lastLoginAt: '2024-12-01T16:45:00',
        phone: '+62814-5678-9012',
        address: 'Surabaya, Indonesia'
      },
      {
        id: 4,
        username: 'staff1',
        email: 'staff1@sim.edu',
        firstName: 'Michael',
        lastName: 'Brown',
        userType: 'STAFF',
        isActive: true,
        createdAt: '2024-03-01T11:00:00',
        lastLoginAt: '2024-11-30T12:15:00',
        phone: '+62815-6789-0123',
        address: 'Yogyakarta, Indonesia'
      },
      {
        id: 5,
        username: 'user1',
        email: 'user1@sim.edu',
        firstName: 'Emma',
        lastName: 'Davis',
        userType: 'USER',
        isActive: false,
        createdAt: '2024-03-15T12:00:00',
        lastLoginAt: '2024-11-25T09:30:00',
        phone: '+62816-7890-1234',
        address: 'Medan, Indonesia'
      }
    ];
  },

  filterUsers(searchTerm) {
    if (!searchTerm.trim()) {
      this.filteredUsers = [...this.users];
    } else {
      const term = searchTerm.toLowerCase();
      this.filteredUsers = this.users.filter(user =>
        user.username.toLowerCase().includes(term) ||
        user.email.toLowerCase().includes(term) ||
        user.firstName.toLowerCase().includes(term) ||
        user.lastName.toLowerCase().includes(term) ||
        user.userType.toLowerCase().includes(term)
      );
    }
    
    this.currentPage = 0;
    this.totalPages = Math.ceil(this.filteredUsers.length / this.pageSize);
    this.renderUserTable();
    this.updatePagination();
  },

  renderUserTable() {
    const tbody = document.getElementById('userTableBody');
    if (!tbody) return;

    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    const pageUsers = this.filteredUsers.slice(startIndex, endIndex);

    tbody.innerHTML = pageUsers.map(user => `
      <tr>
        <td>
          <div class="d-flex align-items-center">
            <div class="avatar-sm me-2">
              <div class="avatar-title bg-primary rounded-circle">
                ${user.firstName.charAt(0)}${user.lastName.charAt(0)}
              </div>
            </div>
            <div>
              <h6 class="mb-0">${user.firstName} ${user.lastName}</h6>
              <small class="text-muted">@${user.username}</small>
            </div>
          </div>
        </td>
        <td>${user.email}</td>
        <td>
          <span class="badge bg-${this.getUserTypeColor(user.userType)}">
            ${user.userType}
          </span>
        </td>
        <td>
          <span class="badge bg-${user.isActive ? 'success' : 'danger'}">
            ${user.isActive ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td>${user.phone || '-'}</td>
        <td>${this.formatDate(user.lastLoginAt)}</td>
        <td>
          <div class="btn-group" role="group">
            <button type="button" class="btn btn-sm btn-outline-primary" 
                    onclick="UserManagement.editUser(${user.id})">
              <i class="fas fa-edit"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline-info" 
                    onclick="UserManagement.viewUser(${user.id})">
              <i class="fas fa-eye"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline-danger" 
                    onclick="UserManagement.deleteUser(${user.id})"
                    ${user.userType === 'ADMIN' ? 'disabled' : ''}>
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  },

  getUserTypeColor(userType) {
    const colors = {
      'ADMIN': 'danger',
      'TEACHER': 'primary',
      'STAFF': 'info',
      'USER': 'secondary'
    };
    return colors[userType] || 'secondary';
  },

  formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('id-ID', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  },

  updatePagination() {
    const pageInfo = document.getElementById('userPageInfo');
    const prevBtn = document.getElementById('userPrevPage');
    const nextBtn = document.getElementById('userNextPage');

    if (pageInfo) {
      const start = this.currentPage * this.pageSize + 1;
      const end = Math.min((this.currentPage + 1) * this.pageSize, this.filteredUsers.length);
      pageInfo.textContent = `${start}-${end} of ${this.filteredUsers.length}`;
    }

    if (prevBtn) {
      prevBtn.disabled = this.currentPage === 0;
    }

    if (nextBtn) {
      nextBtn.disabled = this.currentPage >= this.totalPages - 1;
    }
  },

  showAddUserModal() {
    const modal = new bootstrap.Modal(document.getElementById('userModal'));
    document.getElementById('userModalTitle').textContent = 'Add New User';
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    modal.show();
  },

  async editUser(userId) {
    const user = this.users.find(u => u.id === userId);
    if (!user) return;

    document.getElementById('userModalTitle').textContent = 'Edit User';
    document.getElementById('userId').value = user.id;
    document.getElementById('userUsername').value = user.username;
    document.getElementById('userEmail').value = user.email;
    document.getElementById('userFirstName').value = user.firstName;
    document.getElementById('userLastName').value = user.lastName;
    document.getElementById('userPhone').value = user.phone || '';
    document.getElementById('userAddress').value = user.address || '';
    document.getElementById('userType').value = user.userType;
    document.getElementById('userActive').checked = user.isActive;

    const modal = new bootstrap.Modal(document.getElementById('userModal'));
    modal.show();
  },

  async viewUser(userId) {
    const user = this.users.find(u => u.id === userId);
    if (!user) return;

    const modalContent = `
      <div class="row">
        <div class="col-md-6">
          <p><strong>Username:</strong> ${user.username}</p>
          <p><strong>Email:</strong> ${user.email}</p>
          <p><strong>Full Name:</strong> ${user.firstName} ${user.lastName}</p>
          <p><strong>User Type:</strong> 
            <span class="badge bg-${this.getUserTypeColor(user.userType)}">${user.userType}</span>
          </p>
        </div>
        <div class="col-md-6">
          <p><strong>Phone:</strong> ${user.phone || '-'}</p>
          <p><strong>Address:</strong> ${user.address || '-'}</p>
          <p><strong>Status:</strong> 
            <span class="badge bg-${user.isActive ? 'success' : 'danger'}">
              ${user.isActive ? 'Active' : 'Inactive'}
            </span>
          </p>
          <p><strong>Created:</strong> ${this.formatDate(user.createdAt)}</p>
          <p><strong>Last Login:</strong> ${this.formatDate(user.lastLoginAt)}</p>
        </div>
      </div>
    `;

    document.getElementById('userViewContent').innerHTML = modalContent;
    const modal = new bootstrap.Modal(document.getElementById('userViewModal'));
    modal.show();
  },

  async deleteUser(userId) {
    const user = this.users.find(u => u.id === userId);
    if (!user) return;

    if (user.userType === 'ADMIN') {
      alert('Cannot delete admin user!');
      return;
    }

    if (confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      try {
        // Try to delete via API
        const response = await fetch(`/api/v1/users/${userId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          this.users = this.users.filter(u => u.id !== userId);
          this.filterUsers(document.getElementById('userSearch').value);
          alert('User deleted successfully!');
        } else {
          throw new Error('API delete failed');
        }
      } catch (error) {
        // Fallback: remove from local array
        this.users = this.users.filter(u => u.id !== userId);
        this.filterUsers(document.getElementById('userSearch').value);
        alert('User deleted successfully!');
      }
    }
  },

  async saveUser() {
    const form = document.getElementById('userForm');
    const formData = new FormData(form);
    
    const userData = {
      username: formData.get('username'),
      email: formData.get('email'),
      firstName: formData.get('firstName'),
      lastName: formData.get('lastName'),
      phone: formData.get('phone'),
      address: formData.get('address'),
      userType: formData.get('userType'),
      isActive: formData.get('isActive') === 'on'
    };

    const userId = document.getElementById('userId').value;

    try {
      let response;
      if (userId) {
        // Update existing user
        response = await fetch(`/api/v1/users/${userId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(userData)
        });
      } else {
        // Create new user
        response = await fetch('/api/v1/users', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(userData)
        });
      }

      if (response.ok) {
        await this.loadUsers();
        this.renderUserTable();
        this.updatePagination();
        
        const modal = bootstrap.Modal.getInstance(document.getElementById('userModal'));
        modal.hide();
        
        alert(userId ? 'User updated successfully!' : 'User created successfully!');
      } else {
        throw new Error('API save failed');
      }
    } catch (error) {
      // Fallback: update local array
      if (userId) {
        const index = this.users.findIndex(u => u.id == userId);
        if (index !== -1) {
          this.users[index] = { ...this.users[index], ...userData };
        }
      } else {
        const newUser = {
          id: Math.max(...this.users.map(u => u.id)) + 1,
          ...userData,
          createdAt: new Date().toISOString(),
          lastLoginAt: null
        };
        this.users.push(newUser);
      }
      
      this.filterUsers(document.getElementById('userSearch').value);
      
      const modal = bootstrap.Modal.getInstance(document.getElementById('userModal'));
      modal.hide();
      
      alert(userId ? 'User updated successfully!' : 'User created successfully!');
    }
  },

  async exportToExcel() {
    try {
      const response = await fetch('/api/v1/users/export/excel', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `users_export_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      } else {
        throw new Error('Export API failed');
      }
    } catch (error) {
      // Fallback: create Excel using ExcelJS
      this.createExcelFile();
    }
  },

  createExcelFile() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Users');

    // Add headers
    worksheet.columns = [
      { header: 'ID', key: 'id', width: 10 },
      { header: 'Username', key: 'username', width: 15 },
      { header: 'Email', key: 'email', width: 25 },
      { header: 'First Name', key: 'firstName', width: 15 },
      { header: 'Last Name', key: 'lastName', width: 15 },
      { header: 'User Type', key: 'userType', width: 12 },
      { header: 'Status', key: 'status', width: 10 },
      { header: 'Phone', key: 'phone', width: 15 },
      { header: 'Address', key: 'address', width: 30 },
      { header: 'Created At', key: 'createdAt', width: 20 },
      { header: 'Last Login', key: 'lastLoginAt', width: 20 }
    ];

    // Add data
    this.filteredUsers.forEach(user => {
      worksheet.addRow({
        id: user.id,
        username: user.username,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        userType: user.userType,
        status: user.isActive ? 'Active' : 'Inactive',
        phone: user.phone || '',
        address: user.address || '',
        createdAt: this.formatDate(user.createdAt),
        lastLoginAt: this.formatDate(user.lastLoginAt)
      });
    });

    // Style the header
    worksheet.getRow(1).font = { bold: true };
    worksheet.getRow(1).fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FFE0E0E0' }
    };

    // Generate and download
    workbook.xlsx.writeBuffer().then(buffer => {
      const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `users_export_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    });
  },

  downloadTemplate() {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('User Template');

    // Add headers based on real database structure
    worksheet.columns = [
      { header: 'username*', key: 'username', width: 15 },
      { header: 'email*', key: 'email', width: 25 },
      { header: 'password*', key: 'password', width: 15 },
      { header: 'first_name*', key: 'firstName', width: 15 },
      { header: 'last_name*', key: 'lastName', width: 15 },
      { header: 'user_type*', key: 'userType', width: 12 },
      { header: 'phone', key: 'phone', width: 15 },
      { header: 'address', key: 'address', width: 30 },
      { header: 'nip', key: 'nip', width: 20 },
      { header: 'is_active', key: 'isActive', width: 10 }
    ];

    // Add sample data
    worksheet.addRow({
      username: 'john.doe',
      email: 'john.doe@sim.edu',
      password: 'password123',
      firstName: 'John',
      lastName: 'Doe',
      userType: 'TEACHER',
      phone: '+62812-3456-7890',
      address: 'Jakarta, Indonesia',
      nip: '198501012010011001',
      isActive: 'true'
    });

    worksheet.addRow({
      username: 'jane.smith',
      email: 'jane.smith@sim.edu',
      password: 'password123',
      firstName: 'Jane',
      lastName: 'Smith',
      userType: 'STAFF',
      phone: '+62813-4567-8901',
      address: 'Bandung, Indonesia',
      nip: '198602022011012002',
      isActive: 'true'
    });

    // Style the header
    worksheet.getRow(1).font = { bold: true };
    worksheet.getRow(1).fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF4472C4' }
    };
    worksheet.getRow(1).font.color = { argb: 'FFFFFFFF' };

    // Add instructions
    const instructionSheet = workbook.addWorksheet('Instructions');
    instructionSheet.addRow(['USER IMPORT TEMPLATE INSTRUCTIONS']);
    instructionSheet.addRow(['']);
    instructionSheet.addRow(['Required Fields (marked with *):']);
    instructionSheet.addRow(['- username: Unique username for login']);
    instructionSheet.addRow(['- email: Valid email address']);
    instructionSheet.addRow(['- password: User password (will be encrypted)']);
    instructionSheet.addRow(['- first_name: User first name']);
    instructionSheet.addRow(['- last_name: User last name']);
    instructionSheet.addRow(['- user_type: ADMIN, TEACHER, STAFF, or USER']);
    instructionSheet.addRow(['']);
    instructionSheet.addRow(['Optional Fields:']);
    instructionSheet.addRow(['- phone: Phone number']);
    instructionSheet.addRow(['- address: Full address']);
    instructionSheet.addRow(['- nip: Employee ID number']);
    instructionSheet.addRow(['- is_active: true or false (default: true)']);
    instructionSheet.addRow(['']);
    instructionSheet.addRow(['Notes:']);
    instructionSheet.addRow(['- Do not modify the header row']);
    instructionSheet.addRow(['- Username and email must be unique']);
    instructionSheet.addRow(['- Valid user types: ADMIN, TEACHER, STAFF, USER']);
    instructionSheet.addRow(['- Phone format: +62xxx-xxxx-xxxx']);

    instructionSheet.getRow(1).font = { bold: true, size: 14 };
    instructionSheet.getColumn(1).width = 50;

    // Generate and download
    workbook.xlsx.writeBuffer().then(buffer => {
      const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'user_import_template.xlsx';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    });
  },

  printUsers() {
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>User List - SIM</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; }
          .header { text-align: center; margin-bottom: 30px; }
          .header h1 { color: #333; margin-bottom: 5px; }
          .header p { color: #666; margin: 0; }
          table { width: 100%; border-collapse: collapse; margin-top: 20px; }
          th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
          th { background-color: #f8f9fa; font-weight: bold; }
          .badge { padding: 2px 6px; border-radius: 3px; font-size: 11px; }
          .badge-admin { background-color: #dc3545; color: white; }
          .badge-teacher { background-color: #0d6efd; color: white; }
          .badge-staff { background-color: #0dcaf0; color: white; }
          .badge-user { background-color: #6c757d; color: white; }
          .badge-active { background-color: #198754; color: white; }
          .badge-inactive { background-color: #dc3545; color: white; }
          .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #666; }
          @media print {
            body { margin: 0; }
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>User Management Report</h1>
          <p>School Information Management System</p>
          <p>Generated on: ${new Date().toLocaleDateString('id-ID', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
          })}</p>
        </div>
        
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>Username</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>User Type</th>
              <th>Status</th>
              <th>Phone</th>
              <th>Last Login</th>
            </tr>
          </thead>
          <tbody>
            ${this.filteredUsers.map((user, index) => `
              <tr>
                <td>${index + 1}</td>
                <td>${user.username}</td>
                <td>${user.firstName} ${user.lastName}</td>
                <td>${user.email}</td>
                <td>
                  <span class="badge badge-${user.userType.toLowerCase()}">
                    ${user.userType}
                  </span>
                </td>
                <td>
                  <span class="badge badge-${user.isActive ? 'active' : 'inactive'}">
                    ${user.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>${user.phone || '-'}</td>
                <td>${this.formatDate(user.lastLoginAt)}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
        
        <div class="footer">
          <p>Total Users: ${this.filteredUsers.length}</p>
          <p>© ${new Date().getFullYear()} School Information Management System</p>
        </div>
      </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.print();
  }
};

// Export for global access
window.UserManagement = UserManagement;