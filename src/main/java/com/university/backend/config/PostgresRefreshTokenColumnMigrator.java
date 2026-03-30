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
 * Hibernate {@code columnDefinition = TEXT} does not widen an existing {@code varchar(255)} column.
 * JWT refresh tokens exceed 255 chars, so register/login fail with SQLState 22001 until this runs.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class PostgresRefreshTokenColumnMigrator implements ApplicationRunner {

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
                    "ALTER TABLE refresh_tokens ALTER COLUMN token TYPE TEXT USING token::text"
            );
            log.info("refresh_tokens.token column widened to TEXT (required for JWT refresh tokens).");
        } catch (Exception ex) {
            log.debug("refresh_tokens.token migration skipped: {}", ex.getMessage());
        }
    }
}
