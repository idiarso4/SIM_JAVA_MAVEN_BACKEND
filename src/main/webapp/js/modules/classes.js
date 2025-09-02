// Class Management Module
const Classes = {
    async loadClasses() {
        try {
            Utils.showAlert('info', 'Loading classes from API...');
            const response = await fetch('/api/test/classes/sample');
            if (response.ok) {
                const data = await response.json();
                Utils.showAlert('success', `Loaded ${data.length || 0} classes!`);
            } else {
                Utils.showAlert('warning', 'Backend still starting. Please wait.');
            }
        } catch (error) {
            Utils.showAlert('warning', 'Backend loading. Try again in a moment.');
        }
    },

    showAddForm() {
        Utils.showAlert('info', 'Add Class form will be implemented');
    },

    showSearchForm() {
        Utils.showAlert('info', 'Search Classes will be implemented');
    },

    loadAvailableClasses() {
        Utils.showAlert('info', 'Loading available classes... (Demo mode)');
    },

    loadClassesByGrade(grade) {
        Utils.showAlert('info', `Loading classes for grade ${grade}... (Demo mode)`);
    }
};