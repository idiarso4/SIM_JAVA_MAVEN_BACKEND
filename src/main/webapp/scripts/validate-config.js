#!/usr/bin/env node

/**
 * Configuration Validation Script
 * Validates all build tool configurations are properly set up
 */

const fs = require('fs');
const path = require('path');

class ConfigValidator {
    constructor() {
        this.rootPath = path.resolve(__dirname, '..');
        this.errors = [];
        this.warnings = [];
        this.success = [];
    }

    /**
     * Run all configuration validations
     */
    async validate() {
        console.log('🔧 Validating build tool configurations...\n');

        try {
            this.validatePackageJson();
            this.validateWebpackConfig();
            this.validateBabelConfig();
            this.validateESLintConfig();
            this.validatePrettierConfig();
            this.validateJestConfig();
            this.validatePostCSSConfig();
            this.validateEnvironmentFiles();
            this.validateBuildScripts();

            this.printResults();

            if (this.errors.length > 0) {
                process.exit(1);
            }

        } catch (error) {
            console.error('❌ Configuration validation failed:', error.message);
            process.exit(1);
        }
    }

    /**
     * Validate package.json
     */
    validatePackageJson() {
        const packagePath = path.join(this.rootPath, 'package.json');

        if (!fs.existsSync(packagePath)) {
            this.errors.push('package.json not found');
            return;
        }

        try {
            const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));

            // Check required dependencies
            const requiredDeps = [
                'webpack',
                'webpack-cli',
                'webpack-dev-server',
                '@babel/core',
                '@babel/preset-env',
                'eslint',
                'prettier',
                'jest'
            ];

            const missingDeps = requiredDeps.filter(dep =>
                !packageJson.dependencies?.[dep] && !packageJson.devDependencies?.[dep]
            );

            if (missingDeps.length > 0) {
                this.errors.push(`Missing dependencies: ${missingDeps.join(', ')}`);
            } else {
                this.success.push('All required dependencies present');
            }

            // Check required scripts
            const requiredScripts = [
                'build',
                'build:prod',
                'dev:serve',
                'test',
                'lint',
                'format'
            ];

            const missingScripts = requiredScripts.filter(script =>
                !packageJson.scripts?.[script]
            );

            if (missingScripts.length > 0) {
                this.errors.push(`Missing scripts: ${missingScripts.join(', ')}`);
            } else {
                this.success.push('All required scripts present');
            }

        } catch (error) {
            this.errors.push(`Invalid package.json: ${error.message}`);
        }
    }

    /**
     * Validate Webpack configuration
     */
    validateWebpackConfig() {
        const configPath = path.join(this.rootPath, 'webpack.config.js');

        if (!fs.existsSync(configPath)) {
            this.errors.push('webpack.config.js not found');
            return;
        }

        try {
            const config = require(configPath);

            // Test both development and production modes
            const devConfig = config({}, { mode: 'development' });
            const prodConfig = config({}, { mode: 'production' });

            // Check essential properties
            const requiredProps = ['entry', 'output', 'module', 'plugins'];

            [devConfig, prodConfig].forEach((cfg, index) => {
                const mode = index === 0 ? 'development' : 'production';

                requiredProps.forEach(prop => {
                    if (!cfg[prop]) {
                        this.errors.push(`Webpack ${mode} config missing: ${prop}`);
                    }
                });
            });

            // Check if dev server is configured
            if (!devConfig.devServer) {
                this.warnings.push('Development server not configured');
            }

            this.success.push('Webpack configuration valid');

        } catch (error) {
            this.errors.push(`Invalid webpack.config.js: ${error.message}`);
        }
    }

    /**
     * Validate Babel configuration
     */
    validateBabelConfig() {
        const configPath = path.join(this.rootPath, 'babel.config.js');

        if (!fs.existsSync(configPath)) {
            this.errors.push('babel.config.js not found');
            return;
        }

        try {
            const config = require(configPath);

            if (!config.presets || !Array.isArray(config.presets)) {
                this.errors.push('Babel presets not configured');
            } else {
                const hasEnvPreset = config.presets.some(preset =>
                    preset === '@babel/preset-env' ||
                    (Array.isArray(preset) && preset[0] === '@babel/preset-env')
                );

                if (!hasEnvPreset) {
                    this.errors.push('Babel @babel/preset-env not configured');
                } else {
                    this.success.push('Babel configuration valid');
                }
            }

        } catch (error) {
            this.errors.push(`Invalid babel.config.js: ${error.message}`);
        }
    }

    /**
     * Validate ESLint configuration
     */
    validateESLintConfig() {
        const configPath = path.join(this.rootPath, '.eslintrc.js');

        if (!fs.existsSync(configPath)) {
            this.errors.push('.eslintrc.js not found');
            return;
        }

        try {
            const config = require(configPath);

            if (!config.extends || !Array.isArray(config.extends)) {
                this.warnings.push('ESLint extends not configured');
            }

            if (!config.env) {
                this.warnings.push('ESLint environments not configured');
            }

            this.success.push('ESLint configuration valid');

        } catch (error) {
            this.errors.push(`Invalid .eslintrc.js: ${error.message}`);
        }
    }

    /**
     * Validate Prettier configuration
     */
    validatePrettierConfig() {
        const configPath = path.join(this.rootPath, '.prettierrc');

        if (!fs.existsSync(configPath)) {
            this.errors.push('.prettierrc not found');
            return;
        }

        try {
            const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));

            const recommendedProps = ['semi', 'singleQuote', 'printWidth', 'tabWidth'];
            const missingProps = recommendedProps.filter(prop => !(prop in config));

            if (missingProps.length > 0) {
                this.warnings.push(`Prettier missing recommended props: ${missingProps.join(', ')}`);
            }

            this.success.push('Prettier configuration valid');

        } catch (error) {
            this.errors.push(`Invalid .prettierrc: ${error.message}`);
        }
    }

    /**
     * Validate Jest configuration
     */
    validateJestConfig() {
        const configPath = path.join(this.rootPath, 'jest.config.js');

        if (!fs.existsSync(configPath)) {
            this.errors.push('jest.config.js not found');
            return;
        }

        try {
            const config = require(configPath);

            if (!config.testEnvironment) {
                this.warnings.push('Jest test environment not specified');
            }

            if (!config.setupFilesAfterEnv) {
                this.warnings.push('Jest setup files not configured');
            }

            this.success.push('Jest configuration valid');

        } catch (error) {
            this.errors.push(`Invalid jest.config.js: ${error.message}`);
        }
    }

    /**
     * Validate PostCSS configuration
     */
    validatePostCSSConfig() {
        const configPath = path.join(this.rootPath, 'postcss.config.js');

        if (!fs.existsSync(configPath)) {
            this.errors.push('postcss.config.js not found');
            return;
        }

        try {
            const config = require(configPath);

            if (!config.plugins || !Array.isArray(config.plugins)) {
                this.errors.push('PostCSS plugins not configured');
            } else {
                this.success.push('PostCSS configuration valid');
            }

        } catch (error) {
            this.errors.push(`Invalid postcss.config.js: ${error.message}`);
        }
    }

    /**
     * Validate environment files
     */
    validateEnvironmentFiles() {
        const envFiles = ['.env.development', '.env.production', '.env.test'];

        envFiles.forEach(file => {
            const filePath = path.join(this.rootPath, file);

            if (!fs.existsSync(filePath)) {
                this.warnings.push(`Environment file missing: ${file}`);
            } else {
                try {
                    const content = fs.readFileSync(filePath, 'utf8');

                    // Check for required variables
                    const requiredVars = ['NODE_ENV', 'API_BASE_URL'];
                    const missingVars = requiredVars.filter(varName =>
                        !content.includes(`${varName}=`)
                    );

                    if (missingVars.length > 0) {
                        this.warnings.push(`${file} missing variables: ${missingVars.join(', ')}`);
                    }

                } catch (error) {
                    this.errors.push(`Cannot read ${file}: ${error.message}`);
                }
            }
        });

        this.success.push('Environment files validated');
    }

    /**
     * Validate build scripts
     */
    validateBuildScripts() {
        const scriptsDir = path.join(this.rootPath, 'scripts');

        if (!fs.existsSync(scriptsDir)) {
            this.errors.push('Scripts directory not found');
            return;
        }

        const requiredScripts = [
            'validate-build.js',
            'dev-server.js',
            'build-prod.js',
            'lint-fix.js'
        ];

        requiredScripts.forEach(script => {
            const scriptPath = path.join(scriptsDir, script);

            if (!fs.existsSync(scriptPath)) {
                this.errors.push(`Build script missing: ${script}`);
            } else {
                try {
                    // Check if script is valid JavaScript
                    const content = fs.readFileSync(scriptPath, 'utf8');
                    new Function(content); // Basic syntax check
                } catch (error) {
                    this.errors.push(`Invalid script ${script}: ${error.message}`);
                }
            }
        });

        this.success.push('Build scripts validated');
    }

    /**
     * Print validation results
     */
    printResults() {
        console.log('\n📊 Configuration Validation Results:');
        console.log('='.repeat(60));

        if (this.success.length > 0) {
            console.log(`\n✅ Success (${this.success.length}):`);
            this.success.forEach(item => console.log(`  • ${item}`));
        }

        if (this.warnings.length > 0) {
            console.log(`\n⚠️  Warnings (${this.warnings.length}):`);
            this.warnings.forEach(warning => console.log(`  • ${warning}`));
        }

        if (this.errors.length > 0) {
            console.log(`\n❌ Errors (${this.errors.length}):`);
            this.errors.forEach(error => console.log(`  • ${error}`));
        }

        if (this.errors.length === 0) {
            console.log('\n🎉 All configurations are valid! Build tools are ready to use.');
        } else {
            console.log('\n💥 Configuration validation failed. Please fix the errors above.');
        }

        console.log('='.repeat(60));
    }
}

// Run validation if called directly
if (require.main === module) {
    const validator = new ConfigValidator();
    validator.validate();
}

module.exports = ConfigValidator;