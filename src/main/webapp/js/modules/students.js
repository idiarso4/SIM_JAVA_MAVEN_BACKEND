// Student Management Module
const Students = {
    async loadStudents() {
        try {
            Utils.showAlert('info', 'Loading students from API...');
            const response = await fetch('/api/test/students/sample');
            if (response.ok) {
                const data = await response.json();
                Utils.showAlert('success', `Loaded ${data.length || 0} students!`);
            } else {
                Utils.showAlert('warning', 'Backend still starting. Please wait.');
            }
        } catch (error) {
            Utils.showAlert('warning', 'Backend loading. Try again in a moment.');
        }
    },

    showCreateForm() {
        Utils.showAlert('info', 'Create Student form will be implemented');
    },

    showSearchForm() {
        Utils.showAlert('info', 'Search Students will be implemented');
    },

    showImportExcel() {
        Excel.showImportStudentsExcel();
    },

    viewStudent(id) {
        Utils.showAlert('info', `View student ID: ${id} (Demo)`);
    },

    editStudent(id) {
        Utils.showAlert('info', `Edit student ID: ${id} (Demo)`);
    }
};