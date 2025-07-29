package servlets;

import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerRegisterServletTest {

    private CustomerRegisterServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;
    private UserService mockUserService;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new CustomerRegisterServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        responseWriter = new StringWriter();

        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        mockUserService = mock(UserService.class);
        CustomerRegisterServlet.setUserService(mockUserService);

        when(request.getParameter(UsersDBConstants.COLUMN_PASSWORD)).thenReturn("securePwd123");
        when(request.getParameter(UsersDBConstants.COLUMN_FIRSTNAME)).thenReturn("Mario");
        when(request.getParameter(UsersDBConstants.COLUMN_LASTNAME)).thenReturn("Rossi");
        when(request.getParameter(UsersDBConstants.COLUMN_ADDRESS)).thenReturn("Via Roma");
        when(request.getParameter(UsersDBConstants.COLUMN_PHONE)).thenReturn("3331234567");
        when(request.getParameter(UsersDBConstants.COLUMN_MAILID)).thenReturn("mario@example.com");
    }

    @Test
    public void testRegistrationSuccess() throws Exception {
        when(mockUserService.register(eq(UserRole.CUSTOMER), any(User.class)))
                .thenReturn(ResponseCode.SUCCESS.name());

        servlet.service(request, response);

        verify(request).getRequestDispatcher("CustomerLogin.html");
        verify(dispatcher).include(request, response);
        assertTrue(responseWriter.toString().contains("User Registered Successfully"));
    }

    @Test
    public void testRegistrationFailure() throws Exception {
        when(mockUserService.register(eq(UserRole.CUSTOMER), any(User.class)))
                .thenReturn("EMAIL_ALREADY_EXISTS");

        servlet.service(request, response);

        verify(request).getRequestDispatcher("CustomerRegister.html");
        verify(dispatcher).include(request, response);
        assertTrue(responseWriter.toString().contains("EMAIL_ALREADY_EXISTS"));
        assertTrue(responseWriter.toString().contains("Sorry for interruption"));
    }

    @Test
    public void testUserIsCorrectlyBuilt() throws Exception {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(mockUserService.register(eq(UserRole.CUSTOMER), any(User.class)))
                .thenReturn(ResponseCode.SUCCESS.name());

        servlet.service(request, response);

        verify(mockUserService).register(eq(UserRole.CUSTOMER), userCaptor.capture());
        User user = userCaptor.getValue();

        assertEquals("mario@example.com", user.getEmailId());
        assertEquals("Mario", user.getFirstName());
        assertEquals("Rossi", user.getLastName());
        assertEquals("securePwd123", user.getPassword());
        assertEquals("Via Roma", user.getAddress());
        assertEquals(3331234567L, user.getPhone());
    }

    @Test
    public void testExceptionHandling() throws Exception {
        when(mockUserService.register(any(), any())).thenThrow(new RuntimeException("DB down"));

        servlet.service(request, response);

        verify(dispatcher, never()).include(any(), any());
    }
}
