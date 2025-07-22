package com.bittercode.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.bittercode.constant.ResponseCode;
import com.bittercode.model.StoreException;

public class DBUtil {

    public static Connection getConnection() throws StoreException {
        try {
            if (isTestEnvironment()) {
                Class.forName("org.h2.Driver");
                return DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
            } else {
                Class.forName(DatabaseConfig.DRIVER_NAME);
                return DriverManager.getConnection(DatabaseConfig.CONNECTION_STRING,
                        DatabaseConfig.DB_USER_NAME, DatabaseConfig.DB_PASSWORD);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new StoreException(ResponseCode.DATABASE_CONNECTION_FAILURE);
        }
    }

    private static boolean isTestEnvironment() {
        return "true".equalsIgnoreCase(System.getProperty("test.env"));
    }
}
