package servlets;

import com.bittercode.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.servlet.RequestDispatcher;
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

    @BeforeEach
    void setUp() throws IOException {
        servlet = new LogoutServlet();
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
    void testSuccessfulLogout() throws Exception {
        try (MockedConstruction<UserServiceImpl> mock = mockConstruction(UserServiceImpl.class,
                (mocked, context) -> when(mocked.logout(session)).thenReturn(true))) {

            servlet.doGet(request, response);

            verify(dispatcher).include(request, response);
            writer.flush();
            String result = stringWriter.toString();
            assertTrue(result.contains("Successfully logged out!"));
        }
    }

    @Test
    void testLogoutReturnsFalse() throws Exception {
        try (MockedConstruction<UserServiceImpl> mock = mockConstruction(UserServiceImpl.class,
                (mocked, context) -> when(mocked.logout(session)).thenReturn(false))) {

            servlet.doGet(request, response);

            verify(dispatcher).include(request, response);
            writer.flush();
            String result = stringWriter.toString();
            assertFalse(result.contains("Successfully logged out!"));
        }
    }

    @Test
    void testIOExceptionDuringGetWriter() throws Exception {
        when(response.getWriter()).thenThrow(new IOException("Writer failure"));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel logout");
    }

    @Test
    void testGenericExceptionDuringLogout() throws Exception {
        try (MockedConstruction<UserServiceImpl> mock = mockConstruction(UserServiceImpl.class,
                (mocked, context) -> when(mocked.logout(session)).thenThrow(new RuntimeException("Boom")))) {

            servlet.doGet(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore generico nel logout");
        }
    }
}
