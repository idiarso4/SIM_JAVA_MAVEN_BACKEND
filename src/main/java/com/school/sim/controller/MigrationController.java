package com.school.sim.controller;

import com.school.sim.service.DataMigrationService;
import com.school.sim.service.MigrationValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for data migration operations
 * Provides endpoints for Laravel to Spring Boot data migration and validation
 */
@RestController
@RequestMapping("/api/v1/migration")
@Tag(name = "Data Migration", description = "Data migration and validation endpoints")
@Validated
public class MigrationController {

    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    private DataMigrationService dataMigrationService;

    @Autowired
    private MigrationValidationService migrationValidationService;

    /**
     * Execute complete migration from Laravel to Spring Boot
     */
    @PostMapping("/execute/complete")
    @Operation(summary = "Execute complete migration", description = "Execute complete data migration from Laravel to Spring Boot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Migration executed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid migration configuration"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> executeCompleteMigration(
            @Valid @RequestBody Map<String, Object> migrationConfig) {
        logger.info("Executing complete migration");

        try {
            Map<String, Object> result = dataMigrationService.executeCompleteMigration(migrationConfig);
            logger.info("Complete migration executed with result: {}", result.get("status"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to execute complete migration", e);
            throw e;
        }
    }

    /**
     * Execute partial migration for specific tables
     */
    @PostMapping("/execute/partial")
    @Operation(summary = "Execute partial migration", description = "Execute migration for specific tables only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partial migration executed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid migration configuration"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> executePartialMigration(
            @RequestBody List<String> tableNames,
            @RequestParam(required = false) Map<String, Object> migrationConfig) {

        logger.info("Executing partial migration for {} tables", tableNames.size());

        try {
            if (migrationConfig == null) {
                migrationConfig = dataMigrationService.getDefaultMigrationConfiguration();
            }

            Map<String, Object> result = dataMigrationService.executePartialMigration(tableNames, migrationConfig);
            logger.info("Partial migration executed with result: {}", result.get("status"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to execute partial migration", e);
            throw e;
        }
    }

    /**
     * Get migration progress
     */
    @GetMapping("/progress/{migrationId}")
    @Operation(summary = "Get migration progress", description = "Get progress status of a running migration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getMigrationProgress(
            @PathVariable("migrationId") @NotNull String migrationId) {
        logger.info("Getting migration progress for: {}", migrationId);

        try {
            Map<String, Object> progress = dataMigrationService.getMigrationProgress(migrationId);
            logger.info("Retrieved migration progress for: {}", migrationId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            logger.error("Failed to get migration progress for: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Cancel running migration
     */
    @PostMapping("/cancel/{migrationId}")
    @Operation(summary = "Cancel migration", description = "Cancel a running migration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Migration cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelMigration(
            @PathVariable("migrationId") @NotNull String migrationId) {
        logger.info("Cancelling migration: {}", migrationId);

        try {
            dataMigrationService.cancelMigration(migrationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Migration cancelled successfully");
            response.put("migrationId", migrationId);
            response.put("timestamp", System.currentTimeMillis());

            logger.info("Successfully cancelled migration: {}", migrationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to cancel migration: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Rollback migration
     */
    @PostMapping("/rollback/{migrationId}")
    @Operation(summary = "Rollback migration", description = "Rollback a completed or failed migration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Migration rolled back successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> rollbackMigration(
            @PathVariable("migrationId") @NotNull String migrationId) {
        logger.info("Rolling back migration: {}", migrationId);

        try {
            Map<String, Object> result = dataMigrationService.rollbackMigration(migrationId);
            logger.info("Migration rollback completed with result: {}", result.get("status"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to rollback migration: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Validate data integrity
     */
    @PostMapping("/validate/integrity/{migrationId}")
    @Operation(summary = "Validate data integrity", description = "Validate integrity of migrated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> validateDataIntegrity(
            @PathVariable("migrationId") @NotNull String migrationId,
            @RequestBody(required = false) Map<String, Object> validationConfig) {

        logger.info("Validating data integrity for migration: {}", migrationId);

        try {
            if (validationConfig == null) {
                validationConfig = new HashMap<>();
            }

            Map<String, Object> result = dataMigrationService.validateDataIntegrity(validationConfig);
            logger.info("Data integrity validation completed with result: {}", result.get("status"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to validate data integrity for migration: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Compare record counts between source and target
     */
    @GetMapping("/validate/record-counts")
    @Operation(summary = "Compare record counts", description = "Compare record counts between Laravel and Spring Boot databases")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> compareRecordCounts() {
        logger.info("Comparing record counts between databases");

        try {
            Map<String, Object> comparison = migrationValidationService.compareRecordCounts();
            logger.info("Record count comparison completed");
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            logger.error("Failed to compare record counts", e);
            throw e;
        }
    }

    /**
     * Run migration test suite
     */
    @PostMapping("/test/{migrationId}")
    @Operation(summary = "Run migration test suite", description = "Execute comprehensive migration test suite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test suite executed successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> runMigrationTestSuite(
            @PathVariable("migrationId") @NotNull String migrationId) {
        logger.info("Running migration test suite for: {}", migrationId);

        try {
            Map<String, Object> testResults = migrationValidationService.runMigrationTestSuite(migrationId);
            logger.info("Migration test suite completed with overall status: {}", testResults.get("overallStatus"));
            return ResponseEntity.ok(testResults);
        } catch (Exception e) {
            logger.error("Failed to run migration test suite for: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Generate validation report
     */
    @PostMapping("/validate/report/{migrationId}")
    @Operation(summary = "Generate validation report", description = "Generate comprehensive validation report for migration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "404", description = "Migration not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> generateValidationReport(
            @PathVariable("migrationId") @NotNull String migrationId) {
        logger.info("Generating validation report for migration: {}", migrationId);

        try {
            Map<String, Object> report = migrationValidationService.generateValidationReport(migrationId);
            logger.info("Validation report generated successfully for migration: {}", migrationId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate validation report for migration: {}", migrationId, e);
            throw e;
        }
    }

    /**
     * Get migration history
     */
    @GetMapping("/history")
    @Operation(summary = "Get migration history", description = "Get history of all migrations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getMigrationHistory() {
        logger.info("Getting migration history");

        try {
            List<Map<String, Object>> history = dataMigrationService.getMigrationHistory();
            logger.info("Retrieved {} migration records", history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Failed to get migration history", e);
            throw e;
        }
    }

    /**
     * Get supported table mappings
     */
    @GetMapping("/mappings/tables")
    @Operation(summary = "Get table mappings", description = "Get supported table mappings between Laravel and Spring Boot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mappings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> getSupportedTableMappings() {
        logger.info("Getting supported table mappings");

        try {
            Map<String, String> mappings = dataMigrationService.getSupportedTableMappings();
            logger.info("Retrieved {} table mappings", mappings.size());
            return ResponseEntity.ok(mappings);
        } catch (Exception e) {
            logger.error("Failed to get table mappings", e);
            throw e;
        }
    }

    /**
     * Get data type mappings
     */
    @GetMapping("/mappings/data-types")
    @Operation(summary = "Get data type mappings", description = "Get supported data type mappings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mappings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> getDataTypeMappings() {
        logger.info("Getting data type mappings");

        try {
            Map<String, String> mappings = dataMigrationService.getDataTypeMappings();
            logger.info("Retrieved {} data type mappings", mappings.size());
            return ResponseEntity.ok(mappings);
        } catch (Exception e) {
            logger.error("Failed to get data type mappings", e);
            throw e;
        }
    }

    /**
     * Check migration prerequisites
     */
    @GetMapping("/prerequisites")
    @Operation(summary = "Check migration prerequisites", description = "Check if all prerequisites for migration are met")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prerequisites checked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkMigrationPrerequisites() {
        logger.info("Checking migration prerequisites");

        try {
            Map<String, Object> prerequisites = dataMigrationService.checkMigrationPrerequisites();
            logger.info("Migration prerequisites check completed");
            return ResponseEntity.ok(prerequisites);
        } catch (Exception e) {
            logger.error("Failed to check migration prerequisites", e);
            throw e;
        }
    }

    /**
     * Estimate migration time
     */
    @PostMapping("/estimate")
    @Operation(summary = "Estimate migration time", description = "Estimate time required for migration based on configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estimation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid configuration"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> estimateMigrationTime(@RequestBody Map<String, Object> config) {
        logger.info("Estimating migration time");

        try {
            Map<String, Object> estimation = dataMigrationService.estimateMigrationTime(config);
            logger.info("Migration time estimation completed");
            return ResponseEntity.ok(estimation);
        } catch (Exception e) {
            logger.error("Failed to estimate migration time", e);
            throw e;
        }
    }

    /**
     * Get default migration configuration
     */
    @GetMapping("/config/default")
    @Operation(summary = "Get default configuration", description = "Get default migration configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDefaultMigrationConfiguration() {
        logger.info("Getting default migration configuration");

        try {
            Map<String, Object> config = dataMigrationService.getDefaultMigrationConfiguration();
            logger.info("Retrieved default migration configuration");
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get default migration configuration", e);
            throw e;
        }
    }
}