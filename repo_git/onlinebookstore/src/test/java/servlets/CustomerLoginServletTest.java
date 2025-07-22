package servlets;

import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.StoreException;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class CustomerLoginServletTest {

    private CustomerLoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter outputWriter;
    private PrintWriter printWriter;
    private UserService mockUserService;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CustomerLoginServlet();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        outputWriter = new StringWriter();
        printWriter = new PrintWriter(outputWriter);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(printWriter);

        mockUserService = mock(UserService.class);
        java.lang.reflect.Field field = CustomerLoginServlet.class.getDeclaredField("authService");
        field.setAccessible(true);
        field.set(servlet, mockUserService);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        User mockUser = new User();
        mockUser.setFirstName("Alice");

        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("alice");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("pass123");
        when(mockUserService.login(UserRole.CUSTOMER, "alice", "pass123", session)).thenReturn(mockUser);
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);
        printWriter.flush();

        verify(dispatcher).include(request, response);
        String result = outputWriter.toString();
        assertTrue(result.contains("Welcome Alice, Happy Learning !!"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("bob");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("wrongpass");
        when(mockUserService.login(UserRole.CUSTOMER, "bob", "wrongpass", session)).thenReturn(null);
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);
        printWriter.flush();

        verify(dispatcher).include(request, response);
        String result = outputWriter.toString();
        assertTrue(result.contains("Incorrect UserName or PassWord"));
    }

    @Test
    void testLoginThrowsStoreException() throws Exception {
        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("charlie");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("secret");
        when(mockUserService.login(UserRole.CUSTOMER, "charlie", "secret", session))
                .thenThrow(new StoreException("DB error"));
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);

        assertDoesNotThrow(() -> servlet.doPost(request, response));
        printWriter.flush();

        verify(dispatcher).include(request, response);
        String result = outputWriter.toString();
        assertTrue(result.contains("Internal error occurred. Please try again later."));
    }

    @Test
    void testHandlesIOExceptionFromGetWriter() throws Exception {
        HttpServletResponse brokenResponse = mock(HttpServletResponse.class);
        when(brokenResponse.getWriter()).thenThrow(new IOException("Simulated IO error"));
        when(request.getSession()).thenReturn(session);
        assertDoesNotThrow(() -> servlet.doPost(request, brokenResponse));
    }
}
