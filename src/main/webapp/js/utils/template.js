/**
 * Template Utility
 * Handles HTML template loading and rendering
 */

export class TemplateManager {
  constructor() {
    this.templates = new Map();
    this.loadTemplates();
  }

  /**
   * Load all templates from the DOM
   */
  loadTemplates() {
    // Load templates from components.html if available
    this.loadComponentTemplates();
    
    // Load inline templates
    const templateElements = document.querySelectorAll('template[id]');
    templateElements.forEach(template => {
      this.templates.set(template.id, template);
    });
  }

  /**
   * Load component templates from external file
   */
  async loadComponentTemplates() {
    try {
      const response = await fetch('/templates/components.html');
      if (response.ok) {
        const html = await response.text();
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        
        const templates = tempDiv.querySelectorAll('template[id]');
        templates.forEach(template => {
          this.templates.set(template.id, template);
        });
      }
    } catch (error) {
      console.warn('Could not load component templates:', error);
    }
  }

  /**
   * Get template by ID
   */
  getTemplate(templateId) {
    return this.templates.get(templateId);
  }

  /**
   * Clone template content
   */
  cloneTemplate(templateId) {
    const template = this.getTemplate(templateId);
    if (!template) {
      console.warn(`Template not found: ${templateId}`);
      return null;
    }
    return template.content.cloneNode(true);
  }

  /**
   * Render template with data
   */
  render(templateId, data = {}) {
    const clone = this.cloneTemplate(templateId);
    if (!clone) return null;

    // Replace placeholders with data
    this.replacePlaceholders(clone, data);
    
    return clone;
  }

  /**
   * Replace placeholders in template
   */
  replacePlaceholders(element, data) {
    const walker = document.createTreeWalker(
      element,
      NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT,
      null,
      false
    );

    const nodesToProcess = [];
    let node;
    while (node = walker.nextNode()) {
      nodesToProcess.push(node);
    }

    nodesToProcess.forEach(node => {
      if (node.nodeType === Node.TEXT_NODE) {
        node.textContent = this.processTextContent(node.textContent, data);
      } else if (node.nodeType === Node.ELEMENT_NODE) {
        this.processElementAttributes(node, data);
      }
    });
  }

  /**
   * Process text content for placeholders
   */
  processTextContent(text, data) {
    return text.replace(/\{\{(\w+)\}\}/g, (match, key) => {
      return data[key] !== undefined ? data[key] : match;
    });
  }

  /**
   * Process element attributes for placeholders
   */
  processElementAttributes(element, data) {
    Array.from(element.attributes).forEach(attr => {
      if (attr.value.includes('{{')) {
        attr.value = this.processTextContent(attr.value, data);
      }
    });

    // Handle special data attributes
    Object.keys(data).forEach(key => {
      if (element.classList.contains(key)) {
        element.textContent = data[key];
      }
      if (element.hasAttribute(`data-${key}`)) {
        element.setAttribute(`data-${key}`, data[key]);
      }
    });
  }

  /**
   * Create loading card
   */
  createLoadingCard(message = 'Loading...') {
    return this.render('loading-card-template', { message });
  }

  /**
   * Create error card
   */
  createErrorCard(message = 'Error loading data', onRetry = null) {
    const element = this.render('error-card-template', { message });
    if (element && onRetry) {
      const retryBtn = element.querySelector('.retry-btn');
      if (retryBtn) {
        retryBtn.addEventListener('click', onRetry);
      }
    }
    return element;
  }

  /**
   * Create empty state
   */
  createEmptyState(title = 'No Data Available', description = '', onCreate = null) {
    const element = this.render('empty-state-template', { title, description });
    if (element && onCreate) {
      const createBtn = element.querySelector('.create-btn');
      if (createBtn) {
        createBtn.addEventListener('click', onCreate);
      }
    }
    return element;
  }

  /**
   * Create statistics card
   */
  createStatsCard(data) {
    const element = this.render('stats-card-template', data);
    if (element) {
      // Animate the counter
      const valueElement = element.querySelector('.stat-value');
      if (valueElement && data.value) {
        this.animateCounter(valueElement, data.value);
      }
    }
    return element;
  }

  /**
   * Animate counter from 0 to target value
   */
  animateCounter(element, targetValue, duration = 1000) {
    const startValue = 0;
    const startTime = performance.now();

    const animate = (currentTime) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      
      const currentValue = Math.floor(startValue + (targetValue - startValue) * progress);
      element.textContent = currentValue.toLocaleString();

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }

  /**
   * Create data table
   */
  createDataTable(config) {
    const element = this.render('data-table-template', config);
    if (element) {
      this.setupDataTable(element, config);
    }
    return element;
  }

  /**
   * Setup data table functionality
   */
  setupDataTable(element, config) {
    const table = element.querySelector('.data-table');
    const thead = table.querySelector('thead');
    const tbody = table.querySelector('tbody');
    const searchInput = element.querySelector('.search-input');
    const addBtn = element.querySelector('.add-btn');
    const refreshBtn = element.querySelector('.refresh-btn');

    // Setup headers
    if (config.columns) {
      const headerRow = document.createElement('tr');
      config.columns.forEach(column => {
        const th = document.createElement('th');
        th.textContent = column.title;
        if (column.sortable) {
          th.classList.add('sortable');
          th.style.cursor = 'pointer';
        }
        headerRow.appendChild(th);
      });
      thead.appendChild(headerRow);
    }

    // Setup event listeners
    if (searchInput && config.onSearch) {
      searchInput.addEventListener('input', (e) => {
        config.onSearch(e.target.value);
      });
    }

    if (addBtn && config.onAdd) {
      addBtn.addEventListener('click', config.onAdd);
    }

    if (refreshBtn && config.onRefresh) {
      refreshBtn.addEventListener('click', config.onRefresh);
    }
  }

  /**
   * Create alert
   */
  createAlert(type, title, message, dismissible = true) {
    const element = this.render('alert-template', { title, message });
    if (element) {
      const alert = element.querySelector('.alert');
      alert.classList.add(`alert-${type}`);
      
      const icon = element.querySelector('.alert-icon');
      const iconClass = this.getAlertIcon(type);
      icon.className = `alert-icon me-2 ${iconClass}`;

      if (!dismissible) {
        const closeBtn = element.querySelector('.btn-close');
        if (closeBtn) {
          closeBtn.remove();
        }
        alert.classList.remove('alert-dismissible');
      }
    }
    return element;
  }

  /**
   * Get icon class for alert type
   */
  getAlertIcon(type) {
    const icons = {
      success: 'fas fa-check-circle text-success',
      danger: 'fas fa-exclamation-circle text-danger',
      warning: 'fas fa-exclamation-triangle text-warning',
      info: 'fas fa-info-circle text-info',
      primary: 'fas fa-info-circle text-primary',
      secondary: 'fas fa-info-circle text-secondary'
    };
    return icons[type] || icons.info;
  }

  /**
   * Create progress bar
   */
  createProgress(label, percentage = 0) {
    const element = this.render('progress-template', { label, percentage });
    if (element) {
      const progressBar = element.querySelector('.progress-bar');
      const percentageElement = element.querySelector('.progress-percentage');
      
      progressBar.style.width = `${percentage}%`;
      progressBar.setAttribute('aria-valuenow', percentage);
      percentageElement.textContent = `${percentage}%`;
    }
    return element;
  }

  /**
   * Update progress bar
   */
  updateProgress(element, percentage, label = null) {
    const progressBar = element.querySelector('.progress-bar');
    const percentageElement = element.querySelector('.progress-percentage');
    const labelElement = element.querySelector('.progress-label');

    if (progressBar) {
      progressBar.style.width = `${percentage}%`;
      progressBar.setAttribute('aria-valuenow', percentage);
    }

    if (percentageElement) {
      percentageElement.textContent = `${percentage}%`;
    }

    if (label && labelElement) {
      labelElement.textContent = label;
    }
  }

  /**
   * Create file upload area
   */
  createFileUpload(config = {}) {
    const element = this.render('file-upload-template', config);
    if (element) {
      this.setupFileUpload(element, config);
    }
    return element;
  }

  /**
   * Setup file upload functionality
   */
  setupFileUpload(element, config) {
    const uploadArea = element.querySelector('.file-upload-area');
    const fileInput = element.querySelector('.file-input');
    const browseBtn = element.querySelector('.browse-btn');
    const fileList = element.querySelector('.file-list');

    // Setup drag and drop
    uploadArea.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
      uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadArea.classList.remove('dragover');
      const files = Array.from(e.dataTransfer.files);
      this.handleFiles(files, fileList, config);
    });

    // Setup browse button
    browseBtn.addEventListener('click', () => {
      fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files);
      this.handleFiles(files, fileList, config);
    });
  }

  /**
   * Handle selected files
   */
  handleFiles(files, fileList, config) {
    if (files.length === 0) return;

    fileList.classList.remove('d-none');
    fileList.innerHTML = '';

    files.forEach(file => {
      const fileItem = this.createFileItem(file, config);
      fileList.appendChild(fileItem);
    });

    if (config.onFilesSelected) {
      config.onFilesSelected(files);
    }
  }

  /**
   * Create file item element
   */
  createFileItem(file, config) {
    const fileItem = document.createElement('div');
    fileItem.className = 'file-item';
    
    const icon = this.getFileIcon(file.type);
    const size = this.formatFileSize(file.size);
    
    fileItem.innerHTML = `
      <div class="file-icon">
        <i class="${icon}"></i>
      </div>
      <div class="file-info">
        <div class="file-name">${file.name}</div>
        <div class="file-size">${size}</div>
      </div>
      <div class="file-actions">
        <button type="button" class="btn btn-sm btn-outline-danger remove-file">
          <i class="fas fa-times"></i>
        </button>
      </div>
    `;

    // Setup remove button
    const removeBtn = fileItem.querySelector('.remove-file');
    removeBtn.addEventListener('click', () => {
      fileItem.remove();
      if (config.onFileRemoved) {
        config.onFileRemoved(file);
      }
    });

    return fileItem;
  }

  /**
   * Get file icon based on file type
   */
  getFileIcon(fileType) {
    const icons = {
      'application/pdf': 'fas fa-file-pdf text-danger',
      'application/msword': 'fas fa-file-word text-primary',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'fas fa-file-word text-primary',
      'application/vnd.ms-excel': 'fas fa-file-excel text-success',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'fas fa-file-excel text-success',
      'image/jpeg': 'fas fa-file-image text-info',
      'image/png': 'fas fa-file-image text-info',
      'image/gif': 'fas fa-file-image text-info'
    };
    return icons[fileType] || 'fas fa-file text-secondary';
  }

  /**
   * Format file size
   */
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}

// Create singleton instance
export const templateManager = new TemplateManager();