package servlets;

import com.bittercode.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutServletTest {

    private LogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private UserService mockUserService;

    @BeforeEach
    void setUp() throws IOException {
        mockUserService = mock(UserService.class);
        servlet = new LogoutServlet(mockUserService); // ✅ INIETTA IL MOCK

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter, true);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testLogoutSuccessful() throws ServletException, IOException {
        when(mockUserService.logout(session)).thenReturn(true);

        servlet.doGet(request, response);

        verify(mockUserService).logout(session);
        verify(dispatcher).include(request, response);
        assertTrue(stringWriter.toString().contains("Successfully logged out!"));
    }

    @Test
    void testLogoutReturnsFalse() throws ServletException, IOException {
        when(mockUserService.logout(session)).thenReturn(false);

        servlet.doGet(request, response);

        verify(mockUserService).logout(session);
        verify(dispatcher).include(request, response);
        assertFalse(stringWriter.toString().contains("Successfully logged out!"));
    }

    @Test
    void testIOExceptionDuringGetWriter() throws Exception {
        when(response.getWriter()).thenThrow(new IOException("Writer failure"));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel logout");
    }

    @Test
    void testGenericExceptionDuringLogout() throws Exception {
        when(mockUserService.logout(session)).thenThrow(new RuntimeException("Boom"));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore generico nel logout");
    }
}
