package com.bittercode.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    void testDriverIsLoaded() {
        assertEquals("org.h2.Driver", DatabaseConfig.DRIVER_NAME);
    }

    @Test
    void testAllPropertiesAreLoaded() {
        assertNotNull(DatabaseConfig.DB_HOST, "DB_HOST should not be null");
        assertNotNull(DatabaseConfig.DB_PORT, "DB_PORT should not be null");
        assertNotNull(DatabaseConfig.DB_NAME, "DB_NAME should not be null");
        assertNotNull(DatabaseConfig.DB_USER_NAME, "DB_USER_NAME should not be null");
        assertNotNull(DatabaseConfig.DB_PASSWORD, "DB_PASSWORD should not be null");
    }

    @Test
    void testConnectionStringIsComposedCorrectly() {
        String actual = DatabaseConfig.CONNECTION_STRING;
        assertEquals("jdbc:h2:mem:1234/testdb", actual);
    }
}
