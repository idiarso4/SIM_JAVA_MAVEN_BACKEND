/**
 * Loading Service
 * Handles loading states and spinners throughout the application
 */

export class LoadingService {
    constructor() {
        this.loadingSpinner = document.getElementById('loading-spinner');
        this.loadingStates = new Map();
        this.loadingCounter = 0;
    }

    /**
     * Show global loading spinner
     */
    show(message = 'Loading...') {
        if (this.loadingSpinner) {
            this.loadingSpinner.classList.remove('d-none');
            
            // Update loading message if provided
            const loadingText = this.loadingSpinner.querySelector('.visually-hidden');
            if (loadingText) {
                loadingText.textContent = message;
            }
        }
    }

    /**
     * Hide global loading spinner
     */
    hide() {
        if (this.loadingSpinner) {
            this.loadingSpinner.classList.add('d-none');
        }
    }

    /**
     * Show loading overlay on specific element
     */
    showOverlay(element, message = 'Loading...') {
        if (!element) return null;

        const overlayId = `loading-overlay-${++this.loadingCounter}`;
        
        // Create overlay element
        const overlay = document.createElement('div');
        overlay.id = overlayId;
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            <div class="d-flex flex-column align-items-center">
                <div class="spinner-border text-primary mb-2" role="status">
                    <span class="visually-hidden">${message}</span>
                </div>
                <small class="text-muted">${message}</small>
            </div>
        `;

        // Position overlay
        const rect = element.getBoundingClientRect();
        overlay.style.position = 'absolute';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(255, 255, 255, 0.8)';
        overlay.style.zIndex = '1000';
        overlay.style.display = 'flex';
        overlay.style.alignItems = 'center';
        overlay.style.justifyContent = 'center';

        // Make parent element relative if not already positioned
        const computedStyle = window.getComputedStyle(element);
        if (computedStyle.position === 'static') {
            element.style.position = 'relative';
        }

        // Add overlay to element
        element.appendChild(overlay);

        // Store reference
        this.loadingStates.set(overlayId, {
            element: element,
            overlay: overlay
        });

        return overlayId;
    }

    /**
     * Hide loading overlay
     */
    hideOverlay(overlayId) {
        const loadingState = this.loadingStates.get(overlayId);
        if (loadingState) {
            loadingState.overlay.remove();
            this.loadingStates.delete(overlayId);
        }
    }

    /**
     * Show loading state on button
     */
    showButtonLoading(button, loadingText = 'Loading...') {
        if (!button) return null;

        const originalText = button.innerHTML;
        const originalDisabled = button.disabled;

        // Store original state
        button.dataset.originalText = originalText;
        button.dataset.originalDisabled = originalDisabled;

        // Set loading state
        button.disabled = true;
        button.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            ${loadingText}
        `;

        return {
            restore: () => this.hideButtonLoading(button)
        };
    }

    /**
     * Hide loading state on button
     */
    hideButtonLoading(button) {
        if (!button) return;

        const originalText = button.dataset.originalText;
        const originalDisabled = button.dataset.originalDisabled === 'true';

        if (originalText) {
            button.innerHTML = originalText;
            button.disabled = originalDisabled;
            
            // Clean up data attributes
            delete button.dataset.originalText;
            delete button.dataset.originalDisabled;
        }
    }

    /**
     * Show skeleton loading for table
     */
    showTableSkeleton(tableBody, rowCount = 5, columnCount = 4) {
        if (!tableBody) return;

        tableBody.innerHTML = '';

        for (let i = 0; i < rowCount; i++) {
            const row = document.createElement('tr');
            
            for (let j = 0; j < columnCount; j++) {
                const cell = document.createElement('td');
                cell.innerHTML = '<div class="skeleton" style="height: 20px; width: 100%;"></div>';
                row.appendChild(cell);
            }
            
            tableBody.appendChild(row);
        }
    }

    /**
     * Show skeleton loading for cards
     */
    showCardSkeleton(container, cardCount = 3) {
        if (!container) return;

        container.innerHTML = '';

        for (let i = 0; i < cardCount; i++) {
            const card = document.createElement('div');
            card.className = 'col-md-4 mb-3';
            card.innerHTML = `
                <div class="card">
                    <div class="card-body">
                        <div class="skeleton mb-2" style="height: 24px; width: 60%;"></div>
                        <div class="skeleton mb-2" style="height: 16px; width: 100%;"></div>
                        <div class="skeleton mb-2" style="height: 16px; width: 80%;"></div>
                        <div class="skeleton" style="height: 32px; width: 40%;"></div>
                    </div>
                </div>
            `;
            container.appendChild(card);
        }
    }

    /**
     * Show loading state for form
     */
    showFormLoading(form) {
        if (!form) return null;

        const overlayId = this.showOverlay(form, 'Processing...');
        
        // Disable all form inputs
        const inputs = form.querySelectorAll('input, select, textarea, button');
        inputs.forEach(input => {
            input.disabled = true;
            input.dataset.wasDisabled = input.disabled;
        });

        return {
            hide: () => {
                this.hideOverlay(overlayId);
                // Re-enable form inputs
                inputs.forEach(input => {
                    if (input.dataset.wasDisabled !== 'true') {
                        input.disabled = false;
                    }
                    delete input.dataset.wasDisabled;
                });
            }
        };
    }

    /**
     * Show progress bar
     */
    showProgress(container, progress = 0, message = '') {
        if (!container) return null;

        const progressId = `progress-${++this.loadingCounter}`;
        const progressElement = document.createElement('div');
        progressElement.id = progressId;
        progressElement.className = 'progress-container mb-3';
        progressElement.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2">
                <small class="text-muted progress-message">${message}</small>
                <small class="text-muted progress-percentage">${progress}%</small>
            </div>
            <div class="progress">
                <div class="progress-bar progress-bar-striped progress-bar-animated" 
                     role="progressbar" 
                     style="width: ${progress}%" 
                     aria-valuenow="${progress}" 
                     aria-valuemin="0" 
                     aria-valuemax="100">
                </div>
            </div>
        `;

        container.appendChild(progressElement);

        return {
            update: (newProgress, newMessage) => {
                const progressBar = progressElement.querySelector('.progress-bar');
                const progressMessage = progressElement.querySelector('.progress-message');
                const progressPercentage = progressElement.querySelector('.progress-percentage');
                
                if (progressBar) {
                    progressBar.style.width = `${newProgress}%`;
                    progressBar.setAttribute('aria-valuenow', newProgress);
                }
                
                if (progressMessage && newMessage) {
                    progressMessage.textContent = newMessage;
                }
                
                if (progressPercentage) {
                    progressPercentage.textContent = `${newProgress}%`;
                }
            },
            complete: () => {
                setTimeout(() => {
                    progressElement.remove();
                }, 1000);
            },
            remove: () => {
                progressElement.remove();
            }
        };
    }

    /**
     * Show loading dots animation
     */
    showLoadingDots(container, message = 'Loading') {
        if (!container) return null;

        const dotsId = `loading-dots-${++this.loadingCounter}`;
        const dotsElement = document.createElement('div');
        dotsElement.id = dotsId;
        dotsElement.className = 'loading-dots text-center py-4';
        dotsElement.innerHTML = `
            <div class="text-muted">
                ${message}<span class="dots">
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                </span>
            </div>
            <style>
                .loading-dots .dots .dot {
                    animation: loadingDots 1.4s infinite ease-in-out both;
                }
                .loading-dots .dots .dot:nth-child(1) { animation-delay: -0.32s; }
                .loading-dots .dots .dot:nth-child(2) { animation-delay: -0.16s; }
                @keyframes loadingDots {
                    0%, 80%, 100% { opacity: 0; }
                    40% { opacity: 1; }
                }
            </style>
        `;

        container.appendChild(dotsElement);

        return {
            remove: () => dotsElement.remove()
        };
    }

    /**
     * Clear all loading states
     */
    clearAll() {
        // Hide global spinner
        this.hide();

        // Remove all overlays
        this.loadingStates.forEach((state, id) => {
            this.hideOverlay(id);
        });

        // Reset counter
        this.loadingCounter = 0;
    }

    /**
     * Check if any loading state is active
     */
    isLoading() {
        return this.loadingStates.size > 0 || 
               (this.loadingSpinner && !this.loadingSpinner.classList.contains('d-none'));
    }
}