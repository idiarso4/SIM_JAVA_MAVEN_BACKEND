/**
 * Notification Service
 * Handles toast notifications and user feedback messages
 */

export class NotificationService {
    constructor() {
        this.container = document.getElementById('toast-container');
        this.toastCounter = 0;
    }

    /**
     * Show success notification
     */
    showSuccess(message, options = {}) {
        return this.showToast(message, 'success', {
            icon: 'fas fa-check-circle',
            ...options
        });
    }

    /**
     * Show error notification
     */
    showError(message, options = {}) {
        return this.showToast(message, 'danger', {
            icon: 'fas fa-exclamation-circle',
            autoHide: false,
            ...options
        });
    }

    /**
     * Show warning notification
     */
    showWarning(message, options = {}) {
        return this.showToast(message, 'warning', {
            icon: 'fas fa-exclamation-triangle',
            ...options
        });
    }

    /**
     * Show info notification
     */
    showInfo(message, options = {}) {
        return this.showToast(message, 'info', {
            icon: 'fas fa-info-circle',
            ...options
        });
    }

    /**
     * Show generic toast notification
     */
    showToast(message, type = 'info', options = {}) {
        const toastId = `toast-${++this.toastCounter}`;
        const {
            title = this.getDefaultTitle(type),
            icon = 'fas fa-info-circle',
            autoHide = true,
            delay = 5000,
            position = 'bottom-end'
        } = options;

        // Create toast element
        const toastElement = this.createToastElement(toastId, message, type, title, icon);
        
        // Add to container
        this.container.appendChild(toastElement);

        // Initialize Bootstrap toast
        const toast = new bootstrap.Toast(toastElement, {
            autohide: autoHide,
            delay: delay
        });

        // Show toast
        toast.show();

        // Remove from DOM after hide
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });

        // Add click to dismiss functionality
        const closeBtn = toastElement.querySelector('.btn-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                toast.hide();
            });
        }

        return {
            id: toastId,
            element: toastElement,
            toast: toast,
            hide: () => toast.hide()
        };
    }

    /**
     * Create toast HTML element
     */
    createToastElement(id, message, type, title, icon) {
        const toast = document.createElement('div');
        toast.id = id;
        toast.className = `toast align-items-center text-bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');

        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <div class="d-flex align-items-center">
                        <i class="${icon} me-2"></i>
                        <div>
                            ${title ? `<strong class="me-2">${title}</strong>` : ''}
                            ${message}
                        </div>
                    </div>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                        data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        return toast;
    }

    /**
     * Get default title for toast type
     */
    getDefaultTitle(type) {
        const titles = {
            success: 'Success',
            danger: 'Error',
            warning: 'Warning',
            info: 'Information'
        };
        return titles[type] || 'Notification';
    }

    /**
     * Show loading notification
     */
    showLoading(message = 'Loading...', options = {}) {
        return this.showToast(message, 'primary', {
            icon: 'fas fa-spinner fa-spin',
            autoHide: false,
            ...options
        });
    }

    /**
     * Show confirmation dialog
     */
    showConfirmation(message, options = {}) {
        return new Promise((resolve) => {
            const {
                title = 'Confirm Action',
                confirmText = 'Confirm',
                cancelText = 'Cancel',
                type = 'warning'
            } = options;

            // Create modal element
            const modalId = `confirm-modal-${++this.toastCounter}`;
            const modalElement = this.createConfirmationModal(
                modalId, title, message, confirmText, cancelText, type
            );

            // Add to body
            document.body.appendChild(modalElement);

            // Initialize Bootstrap modal
            const modal = new bootstrap.Modal(modalElement);

            // Handle confirm button
            const confirmBtn = modalElement.querySelector('.btn-confirm');
            confirmBtn.addEventListener('click', () => {
                modal.hide();
                resolve(true);
            });

            // Handle cancel button
            const cancelBtn = modalElement.querySelector('.btn-cancel');
            cancelBtn.addEventListener('click', () => {
                modal.hide();
                resolve(false);
            });

            // Handle modal close
            modalElement.addEventListener('hidden.bs.modal', () => {
                modalElement.remove();
            });

            // Show modal
            modal.show();
        });
    }

    /**
     * Create confirmation modal element
     */
    createConfirmationModal(id, title, message, confirmText, cancelText, type) {
        const modal = document.createElement('div');
        modal.id = id;
        modal.className = 'modal fade';
        modal.setAttribute('tabindex', '-1');
        modal.setAttribute('aria-hidden', 'true');

        const iconClass = {
            warning: 'fas fa-exclamation-triangle text-warning',
            danger: 'fas fa-exclamation-circle text-danger',
            info: 'fas fa-info-circle text-info',
            success: 'fas fa-check-circle text-success'
        }[type] || 'fas fa-question-circle text-primary';

        const buttonClass = {
            warning: 'btn-warning',
            danger: 'btn-danger',
            info: 'btn-info',
            success: 'btn-success'
        }[type] || 'btn-primary';

        modal.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="${iconClass} me-2"></i>
                            ${title}
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-0">${message}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary btn-cancel">
                            ${cancelText}
                        </button>
                        <button type="button" class="btn ${buttonClass} btn-confirm">
                            ${confirmText}
                        </button>
                    </div>
                </div>
            </div>
        `;

        return modal;
    }

    /**
     * Clear all notifications
     */
    clearAll() {
        const toasts = this.container.querySelectorAll('.toast');
        toasts.forEach(toast => {
            const bsToast = bootstrap.Toast.getInstance(toast);
            if (bsToast) {
                bsToast.hide();
            }
        });
    }

    /**
     * Show form validation errors
     */
    showValidationErrors(errors, formElement = null) {
        // Clear existing validation states
        if (formElement) {
            const invalidFields = formElement.querySelectorAll('.is-invalid');
            invalidFields.forEach(field => {
                field.classList.remove('is-invalid');
                const feedback = field.nextElementSibling;
                if (feedback && feedback.classList.contains('invalid-feedback')) {
                    feedback.textContent = '';
                }
            });
        }

        // Show field-specific errors
        if (typeof errors === 'object' && !Array.isArray(errors)) {
            Object.keys(errors).forEach(fieldName => {
                const field = formElement ? 
                    formElement.querySelector(`[name="${fieldName}"]`) :
                    document.querySelector(`[name="${fieldName}"]`);
                
                if (field) {
                    field.classList.add('is-invalid');
                    const feedback = field.nextElementSibling;
                    if (feedback && feedback.classList.contains('invalid-feedback')) {
                        feedback.textContent = errors[fieldName];
                    }
                }
            });

            // Show general error toast
            this.showError('Please correct the highlighted fields');
        } else {
            // Show general error message
            const message = Array.isArray(errors) ? errors.join(', ') : errors;
            this.showError(message);
        }
    }

    /**
     * Show API error
     */
    showApiError(error) {
        if (error.status === 422 && error.data && error.data.fieldErrors) {
            // Validation errors
            this.showValidationErrors(error.data.fieldErrors);
        } else {
            // General API error
            this.showError(error.getUserMessage());
        }
    }
}