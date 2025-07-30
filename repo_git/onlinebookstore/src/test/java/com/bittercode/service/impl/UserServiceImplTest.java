package com.bittercode.service.impl;

import com.bittercode.constant.ResponseCode;
import com.bittercode.model.StoreException;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.util.DBUtil;
import org.junit.jupiter.api.*;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private UserServiceImpl userService;
    private HttpSession session;

    @BeforeAll
    static void setupDB() throws Exception {
        Connection con = DBUtil.getConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE USERS (" +
                "username VARCHAR(255)," +
                "password VARCHAR(255)," +
                "firstName VARCHAR(255)," +
                "lastName VARCHAR(255)," +
                "address VARCHAR(255)," +
                "phone BIGINT," +
                "mailid VARCHAR(255) UNIQUE," +
                "usertype INT" +
                ")");
    }

    @BeforeEach
    void init() {
        userService = new UserServiceImpl();
        session = mock(HttpSession.class);
    }

    @Test
    void testRegisterAndLoginCustomer() throws StoreException {
        User user = new User();
        user.setEmailId("test@example.com");
        user.setPassword("secure");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAddress("Street 123");
        user.setPhone(1234567890L);

        // Register user
        String result = userService.register(UserRole.CUSTOMER, user);
        assertEquals(ResponseCode.SUCCESS.name(), result);

        // Login user
        User loggedInUser = userService.login(UserRole.CUSTOMER, user.getEmailId(), user.getPassword(), session);
        assertNotNull(loggedInUser);
        assertEquals("John", loggedInUser.getFirstName());
        verify(session, times(1)).setAttribute(UserRole.CUSTOMER.toString(), user.getEmailId());
    }

    @Test
    void testIsLoggedIn() {
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn("test@example.com");
        assertTrue(userService.isLoggedIn(UserRole.CUSTOMER, session));

        when(session.getAttribute(UserRole.SELLER.toString())).thenReturn(null);
        assertFalse(userService.isLoggedIn(UserRole.SELLER, session));
    }

    @Test
    void testLogout() {
        doNothing().when(session).invalidate();
        assertTrue(userService.logout(session));
        verify(session).removeAttribute(UserRole.CUSTOMER.toString());
        verify(session).removeAttribute(UserRole.SELLER.toString());
        verify(session).invalidate();
    }

    @Test
    void testRegisterDuplicateUser() throws StoreException {
        User user = new User();
        user.setEmailId("dupe@example.com");
        user.setPassword("123");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setAddress("123 Road");
        user.setPhone(9999999999L);

        String res1 = userService.register(UserRole.CUSTOMER, user);
        assertEquals(ResponseCode.SUCCESS.name(), res1);

        String res2 = userService.register(UserRole.CUSTOMER, user);
        assertTrue(
        res2.equals("User already registered with this email !!") ||
        res2.contains("Duplicate") ||
        res2.equals(ResponseCode.FAILURE.name()),
        "Expected duplicate user message or FAILURE"
    );
    }
}
