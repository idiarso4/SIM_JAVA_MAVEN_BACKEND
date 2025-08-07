#!/usr/bin/env node

/**
 * Build Validation Script
 * Validates the build output and checks for common issues
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

class BuildValidator {
  constructor() {
    this.distPath = path.join(__dirname, '..', 'dist');
    this.errors = [];
    this.warnings = [];
  }

  /**
   * Run all validation checks
   */
  async validate() {
    console.log('🔍 Validating build output...\n');

    try {
      this.checkDistDirectory();
      this.checkRequiredFiles();
      this.checkFilesSizes();
      this.checkHtmlStructure();
      this.checkJavaScriptSyntax();
      this.checkCssValidity();
      this.checkAssetOptimization();
      
      this.printResults();
      
      if (this.errors.length > 0) {
        process.exit(1);
      }
      
    } catch (error) {
      console.error('❌ Validation failed:', error.message);
      process.exit(1);
    }
  }

  /**
   * Check if dist directory exists
   */
  checkDistDirectory() {
    if (!fs.existsSync(this.distPath)) {
      this.errors.push('Dist directory does not exist');
      return;
    }
    console.log('✅ Dist directory exists');
  }

  /**
   * Check for required files
   */
  checkRequiredFiles() {
    const requiredFiles = [
      'index.html',
      'js/main.js',
      'css/main.css'
    ];

    requiredFiles.forEach(file => {
      const filePath = path.join(this.distPath, file);
      if (!fs.existsSync(filePath)) {
        this.errors.push(`Required file missing: ${file}`);
      } else {
        console.log(`✅ Found required file: ${file}`);
      }
    });
  }

  /**
   * Check file sizes
   */
  checkFilesSizes() {
    const maxSizes = {
      'js/main.js': 500 * 1024, // 500KB
      'css/main.css': 100 * 1024, // 100KB
      'index.html': 50 * 1024 // 50KB
    };

    Object.entries(maxSizes).forEach(([file, maxSize]) => {
      const filePath = path.join(this.distPath, file);
      if (fs.existsSync(filePath)) {
        const stats = fs.statSync(filePath);
        const size = stats.size;
        
        if (size > maxSize) {
          this.warnings.push(`File ${file} is large: ${(size / 1024).toFixed(1)}KB (max: ${(maxSize / 1024).toFixed(1)}KB)`);
        } else {
          console.log(`✅ File size OK: ${file} (${(size / 1024).toFixed(1)}KB)`);
        }
      }
    });
  }

  /**
   * Check HTML structure
   */
  checkHtmlStructure() {
    const htmlPath = path.join(this.distPath, 'index.html');
    if (!fs.existsSync(htmlPath)) return;

    const html = fs.readFileSync(htmlPath, 'utf8');
    
    // Check for required elements
    const requiredElements = [
      '<html',
      '<head>',
      '<body>',
      '<title>',
      'viewport',
      'charset'
    ];

    requiredElements.forEach(element => {
      if (!html.includes(element)) {
        this.errors.push(`HTML missing required element: ${element}`);
      }
    });

    // Check for script and style tags
    if (!html.includes('<script') && !html.includes('src="js/')) {
      this.warnings.push('No JavaScript files found in HTML');
    }

    if (!html.includes('<link') && !html.includes('href="css/')) {
      this.warnings.push('No CSS files found in HTML');
    }

    console.log('✅ HTML structure validated');
  }

  /**
   * Check JavaScript syntax
   */
  checkJavaScriptSyntax() {
    const jsDir = path.join(this.distPath, 'js');
    if (!fs.existsSync(jsDir)) return;

    const jsFiles = fs.readdirSync(jsDir).filter(file => file.endsWith('.js'));
    
    jsFiles.forEach(file => {
      const filePath = path.join(jsDir, file);
      const content = fs.readFileSync(filePath, 'utf8');
      
      try {
        // Basic syntax check - try to parse as JavaScript
        new Function(content);
        console.log(`✅ JavaScript syntax OK: ${file}`);
      } catch (error) {
        this.errors.push(`JavaScript syntax error in ${file}: ${error.message}`);
      }
    });
  }

  /**
   * Check CSS validity
   */
  checkCssValidity() {
    const cssDir = path.join(this.distPath, 'css');
    if (!fs.existsSync(cssDir)) return;

    const cssFiles = fs.readdirSync(cssDir).filter(file => file.endsWith('.css'));
    
    cssFiles.forEach(file => {
      const filePath = path.join(cssDir, file);
      const content = fs.readFileSync(filePath, 'utf8');
      
      // Basic CSS validation - check for common issues
      const issues = [];
      
      // Check for unclosed braces
      const openBraces = (content.match(/{/g) || []).length;
      const closeBraces = (content.match(/}/g) || []).length;
      if (openBraces !== closeBraces) {
        issues.push('Mismatched braces');
      }
      
      // Check for empty rules
      if (content.includes('{}')) {
        this.warnings.push(`Empty CSS rules found in ${file}`);
      }
      
      if (issues.length > 0) {
        this.errors.push(`CSS issues in ${file}: ${issues.join(', ')}`);
      } else {
        console.log(`✅ CSS syntax OK: ${file}`);
      }
    });
  }

  /**
   * Check asset optimization
   */
  checkAssetOptimization() {
    // Check if files are minified in production
    if (process.env.NODE_ENV === 'production') {
      const jsPath = path.join(this.distPath, 'js', 'main.js');
      if (fs.existsSync(jsPath)) {
        const content = fs.readFileSync(jsPath, 'utf8');
        
        // Simple check for minification
        const lines = content.split('\n');
        const avgLineLength = content.length / lines.length;
        
        if (avgLineLength < 100) {
          this.warnings.push('JavaScript may not be properly minified');
        } else {
          console.log('✅ JavaScript appears to be minified');
        }
      }
    }

    // Check for source maps
    const jsFiles = fs.readdirSync(path.join(this.distPath, 'js')).filter(f => f.endsWith('.js'));
    const hasSourceMaps = jsFiles.some(file => 
      fs.existsSync(path.join(this.distPath, 'js', file + '.map'))
    );
    
    if (hasSourceMaps) {
      console.log('✅ Source maps found');
    } else {
      this.warnings.push('No source maps found');
    }
  }

  /**
   * Print validation results
   */
  printResults() {
    console.log('\n📊 Validation Results:');
    console.log('='.repeat(50));
    
    if (this.errors.length === 0 && this.warnings.length === 0) {
      console.log('🎉 All checks passed! Build is valid.');
    } else {
      if (this.errors.length > 0) {
        console.log(`\n❌ Errors (${this.errors.length}):`);
        this.errors.forEach(error => console.log(`  • ${error}`));
      }
      
      if (this.warnings.length > 0) {
        console.log(`\n⚠️  Warnings (${this.warnings.length}):`);
        this.warnings.forEach(warning => console.log(`  • ${warning}`));
      }
    }
    
    console.log('='.repeat(50));
  }
}

// Run validation if called directly
if (require.main === module) {
  const validator = new BuildValidator();
  validator.validate();
}

module.exports = BuildValidator;