package com.university.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Legacy DBs created before {@code Employee#status} existed map a column Hibernate expects; without it,
 * any query on {@code hr_employees} fails with SQLState 42703 and portal endpoints return 500.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class PostgresHrEmployeeStatusMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (!url.contains("postgresql")) {
            return;
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE hr_employees ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'"
            );
            log.info("hr_employees.status column ensured (required for HR portal employee queries).");
        } catch (Exception ex) {
            log.debug("hr_employees.status migration skipped: {}", ex.getMessage());
        }
    }
}
