package servlets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bittercode.model.StoreException;
import com.bittercode.model.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorHandlerServletTest {

    private ErrorHandlerServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter output;
    private PrintWriter writer;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new ErrorHandlerServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        output = new StringWriter();
        writer = new PrintWriter(output);

        when(response.getWriter()).thenReturn(writer);
        when(request.getSession()).thenReturn(session);
        doAnswer(inv -> {
            writer.write("MockPageContent");
            return null;
        }).when(dispatcher).include(any(), any());
    }

    @Test
    public void testCustomerViewError() throws Exception {
        when(session.getAttribute("role")).thenReturn(UserRole.CUSTOMER);
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(403);
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);

        servlet.service(request, response);
        writer.flush();

        String result = output.toString();
        assertTrue(result.contains("ACCESS_DENIED"));
    }

    @Test
    public void testSellerViewError() throws Exception {
        when(session.getAttribute("role")).thenReturn(UserRole.SELLER);
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);
        when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

        servlet.service(request, response);
        writer.flush();

        String result = output.toString();
        assertTrue(result.contains("INTERNAL_SERVER_ERROR"));
    }

    @Test
    public void testGuestViewWithScriptInjection() throws Exception {
        when(session.getAttribute("role")).thenReturn(null);
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        servlet.service(request, response);
        writer.flush();

        String result = output.toString();
        assertTrue(result.contains("PAGE_NOT_FOUND"));
        assertTrue(result.contains("document.getElementById('topmid').innerHTML='';"));
    }

    @Test
    public void testStoreExceptionHandledProperly() throws Exception {
        StoreException storeException = new StoreException(503, "DB_DOWN", "Custom DB error");
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(storeException);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        servlet.service(request, response);
        writer.flush();

        String result = output.toString();
        assertTrue(result.contains("DB_DOWN"));
        assertTrue(result.contains("Custom DB error"));
    }

    @Test
    public void testGenericErrorWithoutException() throws Exception {
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(400);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        servlet.service(request, response);
        writer.flush();

        String result = output.toString();
        assertTrue(result.contains("BAD_REQUEST"));
    }
}
