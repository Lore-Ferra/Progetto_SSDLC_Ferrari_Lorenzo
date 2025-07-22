package servlets;

import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SellerLoginServletTest {

    private SellerLoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private UserService userService;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        userService = mock(UserService.class);
        servlet = new SellerLoginServlet(userService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void shouldLoginSuccessfully() throws ServletException, IOException {
        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("seller");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("password");
        User user = new User();
        user.setEmailId("seller@test.com");
        user.setPassword("password");
        user.setFirstName("Seller");
        user.setLastName("Doe");

        when(userService.login(UserRole.SELLER, "seller", "password", session)).thenReturn(user);

        when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(dispatcher).include(request, response);
        writer.flush();
        String output = stringWriter.toString();
        assert output.contains("Welcome Seller");
    }

    @Test
    void shouldFailLoginAndShowErrorMessage() throws ServletException, IOException {
        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("wrong");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("wrong");
        when(userService.login(UserRole.SELLER, "wrong", "wrong", session))
                .thenReturn(null);

        when(request.getRequestDispatcher("SellerLogin.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(dispatcher).include(request, response);
        writer.flush();
        String output = stringWriter.toString();
        assert output.contains("Incorrect UserName or PassWord");
    }

    @Test
    void shouldHandleIOExceptionFromGetWriter() throws ServletException, IOException {
        when(response.getWriter()).thenThrow(new IOException("Writer error"));

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to write response");
    }

    @Test
    void shouldHandleUnexpectedExceptionInLogin() throws ServletException, IOException {
        when(request.getParameter(UsersDBConstants.COLUMN_USERNAME)).thenReturn("seller");
        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("password");
        when(userService.login(UserRole.SELLER, "seller", "password", session))
                .thenThrow(new RuntimeException("DB error"));

        when(response.getWriter()).thenReturn(writer);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        writer.flush();
        String output = stringWriter.toString();
        assertFalse(output.isEmpty(), "Expected servlet to write something even in case of error");
    }
}
