/**
 * Student Service
 * Handles all student-related API operations
 */

import { ApiService } from './api.js';
import { NotificationService } from './notification.js';

export class StudentService {
    constructor() {
        this.apiService = new ApiService();
        this.notificationService = new NotificationService();
        this.baseEndpoint = '/api/v1/students';
    }

    /**
     * Get paginated list of students with filters
     */
    async getStudents(params = {}) {
        try {
            const queryParams = {
                page: params.page || 0,
                size: params.size || 10,
                sortBy: params.sortBy || 'lastName',
                sortDir: params.sortDir || 'asc'
            };

            // Add filters if provided
            if (params.filters) {
                Object.keys(params.filters).forEach(key => {
                    if (params.filters[key]) {
                        queryParams[key] = params.filters[key];
                    }
                });
            }

            const response = await this.apiService.getPaginated(this.baseEndpoint, queryParams);
            return response.data;
        } catch (error) {
            console.error('Error fetching students:', error);
            throw error;
        }
    }

    /**
     * Get student by ID
     */
    async getStudentById(id) {
        try {
            const response = await this.apiService.get(`${this.baseEndpoint}/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching student ${id}:`, error);
            throw error;
        }
    }

    /**
     * Create new student
     */
    async createStudent(studentData) {
        try {
            // Validate required fields
            this.validateStudentData(studentData);

            const response = await this.apiService.post(this.baseEndpoint, studentData);
            return response.data;
        } catch (error) {
            console.error('Error creating student:', error);
            throw error;
        }
    }

    /**
     * Update existing student
     */
    async updateStudent(id, studentData) {
        try {
            // Validate required fields
            this.validateStudentData(studentData);

            const response = await this.apiService.put(`${this.baseEndpoint}/${id}`, studentData);
            return response.data;
        } catch (error) {
            console.error(`Error updating student ${id}:`, error);
            throw error;
        }
    }

    /**
     * Delete student
     */
    async deleteStudent(id) {
        try {
            await this.apiService.delete(`${this.baseEndpoint}/${id}`);
            return true;
        } catch (error) {
            console.error(`Error deleting student ${id}:`, error);
            throw error;
        }
    }

    /**
     * Check if student ID exists
     */
    async checkStudentIdExists(studentId, excludeId = null) {
        try {
            const response = await this.apiService.get(`${this.baseEndpoint}/check-student-id/${studentId}`);
            const exists = response.data.exists;
            
            // If excluding a specific ID (for updates), check if it's the same student
            if (exists && excludeId && response.data.studentId === excludeId) {
                return false;
            }
            
            return exists;
        } catch (error) {
            console.warn('Could not check student ID uniqueness:', error);
            return false;
        }
    }

    /**
     * Check if email exists
     */
    async checkEmailExists(email, excludeId = null) {
        try {
            const response = await this.apiService.get(`${this.baseEndpoint}/check-email/${encodeURIComponent(email)}`);
            const exists = response.data.exists;
            
            // If excluding a specific ID (for updates), check if it's the same student
            if (exists && excludeId && response.data.studentId === excludeId) {
                return false;
            }
            
            return exists;
        } catch (error) {
            console.warn('Could not check email uniqueness:', error);
            return false;
        }
    }

    /**
     * Search students
     */
    async searchStudents(searchTerm, params = {}) {
        try {
            const queryParams = {
                page: params.page || 0,
                size: params.size || 10,
                sortBy: params.sortBy || 'lastName',
                sortDir: params.sortDir || 'asc',
                search: searchTerm
            };

            // Add additional filters
            if (params.filters) {
                Object.keys(params.filters).forEach(key => {
                    if (params.filters[key]) {
                        queryParams[key] = params.filters[key];
                    }
                });
            }

            const response = await this.apiService.getPaginated(this.baseEndpoint, queryParams);
            return response.data;
        } catch (error) {
            console.error('Error searching students:', error);
            throw error;
        }
    }

    /**
     * Get student assessments
     */
    async getStudentAssessments(studentId, params = {}) {
        try {
            const queryParams = {
                page: params.page || 0,
                size: params.size || 10,
                sortBy: params.sortBy || 'date',
                sortDir: params.sortDir || 'desc'
            };

            const response = await this.apiService.getPaginated(
                `${this.baseEndpoint}/${studentId}/assessments`, 
                queryParams
            );
            return response.data;
        } catch (error) {
            console.error(`Error fetching assessments for student ${studentId}:`, error);
            throw error;
        }
    }

    /**
     * Export students to Excel
     */
    async exportStudents(filters = {}) {
        try {
            const queryParams = new URLSearchParams(filters);
            const filename = `students_export_${new Date().toISOString().split('T')[0]}.xlsx`;
            
            await this.apiService.downloadFile(
                `${this.baseEndpoint}/export?${queryParams.toString()}`,
                filename
            );
            
            this.notificationService.showSuccess('Students exported successfully');
        } catch (error) {
            console.error('Error exporting students:', error);
            this.notificationService.showError('Failed to export students');
            throw error;
        }
    }

    /**
     * Import students from Excel
     */
    async importStudents(file, options = {}) {
        try {
            const response = await this.apiService.uploadFile(
                `${this.baseEndpoint}/import`,
                file,
                { data: options }
            );
            
            this.notificationService.showSuccess(
                `Successfully imported ${response.data.imported} students`
            );
            
            return response.data;
        } catch (error) {
            console.error('Error importing students:', error);
            this.notificationService.showError('Failed to import students');
            throw error;
        }
    }

    /**
     * Validate student data
     */
    validateStudentData(studentData) {
        const required = ['firstName', 'lastName', 'email', 'studentId', 'classRoom', 'major', 'status'];
        const missing = required.filter(field => !studentData[field]);
        
        if (missing.length > 0) {
            throw new Error(`Missing required fields: ${missing.join(', ')}`);
        }

        // Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(studentData.email)) {
            throw new Error('Invalid email format');
        }

        // Validate student ID format
        const studentIdRegex = /^STU\d{4}$/;
        if (!studentIdRegex.test(studentData.studentId)) {
            throw new Error('Student ID must be in format STU0000');
        }

        // Validate status
        const validStatuses = ['ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED'];
        if (!validStatuses.includes(studentData.status)) {
            throw new Error('Invalid student status');
        }

        // Validate enrollment date
        if (studentData.enrollmentDate) {
            const enrollmentDate = new Date(studentData.enrollmentDate);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (enrollmentDate > today) {
                throw new Error('Enrollment date cannot be in the future');
            }
        }
    }

    /**
     * Get available class rooms
     */
    getAvailableClassRooms() {
        return ['10A', '10B', '11A', '11B', '12A', '12B'];
    }

    /**
     * Get available majors
     */
    getAvailableMajors() {
        return [
            { value: 'SCIENCE', label: 'Science' },
            { value: 'SOCIAL', label: 'Social Studies' },
            { value: 'LANGUAGE', label: 'Language' },
            { value: 'ARTS', label: 'Arts' },
            { value: 'MATHEMATICS', label: 'Mathematics' }
        ];
    }

    /**
     * Get available statuses
     */
    getAvailableStatuses() {
        return [
            { value: 'ACTIVE', label: 'Active' },
            { value: 'INACTIVE', label: 'Inactive' },
            { value: 'GRADUATED', label: 'Graduated' },
            { value: 'SUSPENDED', label: 'Suspended' }
        ];
    }
}