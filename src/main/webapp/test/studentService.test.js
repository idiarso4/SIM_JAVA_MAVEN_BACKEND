/**
 * Student Service Tests
 * Tests for student data operations
 */

import { StudentService } from '../js/services/studentService.js';
import { ApiService } from '../js/services/api.js';

// Mock ApiService
jest.mock('../js/services/api.js');

describe('StudentService', () => {
    let studentService;
    let mockApiService;

    beforeEach(() => {
        // Clear all mocks
        jest.clearAllMocks();
        
        // Create mock API service
        mockApiService = {
            getPaginated: jest.fn(),
            get: jest.fn(),
            post: jest.fn(),
            put: jest.fn(),
            delete: jest.fn(),
            downloadFile: jest.fn(),
            uploadFile: jest.fn()
        };
        
        // Mock the ApiService constructor
        ApiService.mockImplementation(() => mockApiService);
        
        // Create student service instance
        studentService = new StudentService();
    });

    describe('getStudents', () => {
        it('should fetch students with default parameters', async () => {
            const mockResponse = {
                content: [
                    { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com' }
                ],
                totalPages: 1,
                totalElements: 1,
                number: 0
            };

            mockApiService.getPaginated.mockResolvedValue({ data: mockResponse });

            const result = await studentService.getStudents();

            expect(mockApiService.getPaginated).toHaveBeenCalledWith('/api/v1/students', {
                page: 0,
                size: 10,
                sortBy: 'lastName',
                sortDir: 'asc'
            });
            expect(result).toEqual(mockResponse);
        });

        it('should fetch students with custom parameters', async () => {
            const params = {
                page: 1,
                size: 20,
                sortBy: 'firstName',
                sortDir: 'desc',
                filters: {
                    status: 'ACTIVE',
                    classRoom: '10A'
                }
            };

            const mockResponse = {
                content: [],
                totalPages: 0,
                totalElements: 0,
                number: 1
            };

            mockApiService.getPaginated.mockResolvedValue({ data: mockResponse });

            const result = await studentService.getStudents(params);

            expect(mockApiService.getPaginated).toHaveBeenCalledWith('/api/v1/students', {
                page: 1,
                size: 20,
                sortBy: 'firstName',
                sortDir: 'desc',
                status: 'ACTIVE',
                classRoom: '10A'
            });
            expect(result).toEqual(mockResponse);
        });
    });

    describe('getStudentById', () => {
        it('should fetch student by ID', async () => {
            const mockStudent = {
                id: 1,
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com'
            };

            mockApiService.get.mockResolvedValue({ data: mockStudent });

            const result = await studentService.getStudentById(1);

            expect(mockApiService.get).toHaveBeenCalledWith('/api/v1/students/1');
            expect(result).toEqual(mockStudent);
        });
    });

    describe('createStudent', () => {
        it('should create a new student', async () => {
            const studentData = {
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                studentId: 'STU0001',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE'
            };

            const mockResponse = { ...studentData, id: 1 };
            mockApiService.post.mockResolvedValue({ data: mockResponse });

            const result = await studentService.createStudent(studentData);

            expect(mockApiService.post).toHaveBeenCalledWith('/api/v1/students', studentData);
            expect(result).toEqual(mockResponse);
        });

        it('should throw error for invalid student data', async () => {
            const invalidData = {
                firstName: 'John'
                // Missing required fields
            };

            await expect(studentService.createStudent(invalidData))
                .rejects.toThrow('Missing required fields');
        });
    });

    describe('updateStudent', () => {
        it('should update an existing student', async () => {
            const studentData = {
                id: 1,
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                studentId: 'STU0001',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE'
            };

            mockApiService.put.mockResolvedValue({ data: studentData });

            const result = await studentService.updateStudent(1, studentData);

            expect(mockApiService.put).toHaveBeenCalledWith('/api/v1/students/1', studentData);
            expect(result).toEqual(studentData);
        });
    });

    describe('deleteStudent', () => {
        it('should delete a student', async () => {
            mockApiService.delete.mockResolvedValue({});

            const result = await studentService.deleteStudent(1);

            expect(mockApiService.delete).toHaveBeenCalledWith('/api/v1/students/1');
            expect(result).toBe(true);
        });
    });

    describe('validateStudentData', () => {
        it('should validate valid student data', () => {
            const validData = {
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                studentId: 'STU0001',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE'
            };

            expect(() => studentService.validateStudentData(validData)).not.toThrow();
        });

        it('should throw error for missing required fields', () => {
            const invalidData = {
                firstName: 'John'
            };

            expect(() => studentService.validateStudentData(invalidData))
                .toThrow('Missing required fields');
        });

        it('should throw error for invalid email', () => {
            const invalidData = {
                firstName: 'John',
                lastName: 'Doe',
                email: 'invalid-email',
                studentId: 'STU0001',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE'
            };

            expect(() => studentService.validateStudentData(invalidData))
                .toThrow('Invalid email format');
        });

        it('should throw error for invalid student ID format', () => {
            const invalidData = {
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                studentId: 'INVALID',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE'
            };

            expect(() => studentService.validateStudentData(invalidData))
                .toThrow('Student ID must be in format STU0000');
        });

        it('should throw error for future enrollment date', () => {
            const futureDate = new Date();
            futureDate.setDate(futureDate.getDate() + 1);

            const invalidData = {
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                studentId: 'STU0001',
                classRoom: '10A',
                major: 'SCIENCE',
                status: 'ACTIVE',
                enrollmentDate: futureDate.toISOString()
            };

            expect(() => studentService.validateStudentData(invalidData))
                .toThrow('Enrollment date cannot be in the future');
        });
    });

    describe('checkStudentIdExists', () => {
        it('should check if student ID exists', async () => {
            mockApiService.get.mockResolvedValue({ 
                data: { exists: true, studentId: 1 } 
            });

            const result = await studentService.checkStudentIdExists('STU0001');

            expect(mockApiService.get).toHaveBeenCalledWith('/api/v1/students/check-student-id/STU0001');
            expect(result).toBe(true);
        });

        it('should exclude specific ID when checking', async () => {
            mockApiService.get.mockResolvedValue({ 
                data: { exists: true, studentId: 1 } 
            });

            const result = await studentService.checkStudentIdExists('STU0001', 1);

            expect(result).toBe(false);
        });
    });

    describe('checkEmailExists', () => {
        it('should check if email exists', async () => {
            mockApiService.get.mockResolvedValue({ 
                data: { exists: true, studentId: 1 } 
            });

            const result = await studentService.checkEmailExists('john@example.com');

            expect(mockApiService.get).toHaveBeenCalledWith('/api/v1/students/check-email/john%40example.com');
            expect(result).toBe(true);
        });
    });
});