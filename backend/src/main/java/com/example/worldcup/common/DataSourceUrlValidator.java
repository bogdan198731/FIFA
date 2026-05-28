package com.example.worldcup.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Fails fast at boot if {@code spring.datasource.url} isn't a JDBC URL.
 *
 * Without this, pasting Render's "Internal Database URL"
 * ({@code postgresql://user:pass@host/db}) directly into {@code DB_URL}
 * produces a five-deep chain of Spring bean failures, with the actual cause
 * ("Driver org.postgresql.Driver claims to not accept jdbcUrl, …") buried
 * at the bottom of the stack trace. This validator surfaces it as a one-line
 * {@link IllegalStateException} during application context startup instead.
 */
@Configuration
public class DataSourceUrlValidator {

    private final String url;

    public DataSourceUrlValidator(@Value("${spring.datasource.url}") String url) {
        this.url = url;
    }

    @PostConstruct
    void validate() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "spring.datasource.url is not configured. Set the DB_URL " +
                    "environment variable to a JDBC URL " +
                    "(e.g. jdbc:postgresql://host:5432/dbname).");
        }
        if (!url.startsWith("jdbc:")) {
            throw new IllegalStateException(
                    "spring.datasource.url must be a JDBC URL beginning with " +
                    "'jdbc:' — got '" + redactCredentials(url) + "'. " +
                    "If this came from Render's \"Internal Database URL\" " +
                    "(postgresql://user:pass@host/db), rewrite it as " +
                    "jdbc:postgresql://host:5432/db and put user/password " +
                    "in DB_USERNAME / DB_PASSWORD instead.");
        }
    }

    /** Hide any user:pass embedded in the URL so error logs don't leak it. */
    private static String redactCredentials(String url) {
        int at = url.indexOf('@');
        int scheme = url.indexOf("://");
        if (scheme > 0 && at > scheme) {
            return url.substring(0, scheme + 3) + "***@" + url.substring(at + 1);
        }
        return url;
    }
}
