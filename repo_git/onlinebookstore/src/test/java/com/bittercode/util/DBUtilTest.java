package com.bittercode.util;

import com.bittercode.model.StoreException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DBUtilTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("test.env", "true");
        System.setProperty("test.db.user", "sa");
        System.setProperty("test.db.pass", "P@ssw0rdTest123!");
    }

    @AfterAll
    static void tearDown() {
        System.clearProperty("test.env");
        System.clearProperty("test.db.user");
        System.clearProperty("test.db.pass");
    }

    @Test
    void testConnectionInTestEnvironment() {
        try (Connection conn = DBUtil.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");

            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO test_table (id, name) VALUES (1, 'H2 Test')");
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");

            assertTrue(rs.next(), "Result set should contain at least one row");
            assertEquals("H2 Test", rs.getString("name"));

            rs.close();
            stmt.close();
        } catch (Exception e) {
            fail("Exception during H2 test connection: " + e.getMessage());
        }
    }

    @Test
    void testInvalidCredentialsThrowException() {
        System.setProperty("test.db.pass", "wrong_password");

        StoreException thrown = assertThrows(StoreException.class, DBUtil::getConnection);
        assertEquals("DATABASE_CONNECTION_FAILURE", thrown.getErrorCode());

        System.setProperty("test.db.pass", "P@ssw0rdTest123!");
    }
}
