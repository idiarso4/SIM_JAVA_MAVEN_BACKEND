/**
 * API Service
 * Handles HTTP requests to the backend API with authentication and error handling
 */

import { APP_CONFIG } from '../main.js';

export class ApiService {
    constructor() {
        this.baseURL = APP_CONFIG.API_BASE_URL;
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    /**
     * Make HTTP request
     */
    async request(method, endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            method: method.toUpperCase(),
            headers: {
                ...this.defaultHeaders,
                ...options.headers
            },
            ...options
        };

        // Add authentication header if token exists
        const token = localStorage.getItem(APP_CONFIG.TOKEN_KEY);
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        // Add body for POST, PUT, PATCH requests
        if (options.data && ['POST', 'PUT', 'PATCH'].includes(config.method)) {
            config.body = JSON.stringify(options.data);
        }

        try {
            console.log(`Making ${method.toUpperCase()} request to: ${url}`);
            
            const response = await fetch(url, config);
            
            // Handle different response types
            const contentType = response.headers.get('content-type');
            let data;
            
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else if (contentType && contentType.includes('text/')) {
                data = await response.text();
            } else {
                data = await response.blob();
            }

            if (!response.ok) {
                throw new ApiError(
                    data.message || `HTTP ${response.status}: ${response.statusText}`,
                    response.status,
                    data
                );
            }

            return {
                data,
                status: response.status,
                headers: response.headers
            };

        } catch (error) {
            console.error(`API request failed: ${method.toUpperCase()} ${url}`, error);
            
            if (error instanceof ApiError) {
                throw error;
            }
            
            // Handle network errors
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                throw new ApiError('Network error. Please check your connection.', 0);
            }
            
            throw new ApiError(error.message || 'An unexpected error occurred', 500);
        }
    }

    /**
     * GET request
     */
    async get(endpoint, options = {}) {
        return this.request('GET', endpoint, options);
    }

    /**
     * POST request
     */
    async post(endpoint, data = null, options = {}) {
        return this.request('POST', endpoint, { ...options, data });
    }

    /**
     * PUT request
     */
    async put(endpoint, data = null, options = {}) {
        return this.request('PUT', endpoint, { ...options, data });
    }

    /**
     * PATCH request
     */
    async patch(endpoint, data = null, options = {}) {
        return this.request('PATCH', endpoint, { ...options, data });
    }

    /**
     * DELETE request
     */
    async delete(endpoint, options = {}) {
        return this.request('DELETE', endpoint, options);
    }

    /**
     * Upload file
     */
    async uploadFile(endpoint, file, options = {}) {
        const formData = new FormData();
        formData.append('file', file);

        // Add additional form data if provided
        if (options.data) {
            Object.keys(options.data).forEach(key => {
                formData.append(key, options.data[key]);
            });
        }

        const config = {
            method: 'POST',
            body: formData,
            headers: {
                // Don't set Content-Type for FormData, let browser set it with boundary
                ...options.headers
            }
        };

        // Remove Content-Type header to let browser set it
        delete config.headers['Content-Type'];

        return this.request('POST', endpoint, config);
    }

    /**
     * Download file
     */
    async downloadFile(endpoint, filename, options = {}) {
        try {
            const response = await this.request('GET', endpoint, {
                ...options,
                headers: {
                    ...options.headers,
                    'Accept': 'application/octet-stream'
                }
            });

            // Create blob and download
            const blob = new Blob([response.data]);
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = filename || 'download';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            return response;

        } catch (error) {
            console.error('File download error:', error);
            throw error;
        }
    }

    /**
     * Make paginated request
     */
    async getPaginated(endpoint, params = {}) {
        const queryParams = new URLSearchParams({
            page: params.page || 0,
            size: params.size || 20,
            sortBy: params.sortBy || 'id',
            sortDir: params.sortDir || 'asc',
            ...params.filters
        });

        return this.get(`${endpoint}?${queryParams.toString()}`);
    }

    /**
     * Search with criteria
     */
    async search(endpoint, searchCriteria, params = {}) {
        const queryParams = new URLSearchParams({
            page: params.page || 0,
            size: params.size || 20,
            sortBy: params.sortBy || 'id',
            sortDir: params.sortDir || 'asc'
        });

        return this.post(`${endpoint}/search?${queryParams.toString()}`, searchCriteria);
    }

    /**
     * Set default header
     */
    setDefaultHeader(key, value) {
        this.defaultHeaders[key] = value;
    }

    /**
     * Remove default header
     */
    removeDefaultHeader(key) {
        delete this.defaultHeaders[key];
    }

    /**
     * Set base URL
     */
    setBaseURL(url) {
        this.baseURL = url;
    }

    /**
     * Get base URL
     */
    getBaseURL() {
        return this.baseURL;
    }
}

/**
 * Custom API Error class
 */
export class ApiError extends Error {
    constructor(message, status = 500, data = null) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
        this.timestamp = new Date().toISOString();
    }

    /**
     * Check if error is authentication related
     */
    isAuthError() {
        return this.status === 401 || this.status === 403;
    }

    /**
     * Check if error is client error (4xx)
     */
    isClientError() {
        return this.status >= 400 && this.status < 500;
    }

    /**
     * Check if error is server error (5xx)
     */
    isServerError() {
        return this.status >= 500;
    }

    /**
     * Check if error is network error
     */
    isNetworkError() {
        return this.status === 0;
    }

    /**
     * Get user-friendly error message
     */
    getUserMessage() {
        if (this.isNetworkError()) {
            return 'Network error. Please check your internet connection.';
        }
        
        if (this.status === 401) {
            return 'Your session has expired. Please log in again.';
        }
        
        if (this.status === 403) {
            return 'You do not have permission to perform this action.';
        }
        
        if (this.status === 404) {
            return 'The requested resource was not found.';
        }
        
        if (this.status === 429) {
            return 'Too many requests. Please try again later.';
        }
        
        if (this.isServerError()) {
            return 'A server error occurred. Please try again later.';
        }
        
        return this.message || 'An unexpected error occurred.';
    }
}