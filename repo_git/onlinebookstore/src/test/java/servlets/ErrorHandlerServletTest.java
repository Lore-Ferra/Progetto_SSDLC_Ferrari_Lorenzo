package servlets;

import com.bittercode.model.StoreException;
import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ErrorHandlerServletTest {

    private ErrorHandlerServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter output;
    private PrintWriter writer;

    @BeforeEach
    void setup() throws Exception {
        servlet = new ErrorHandlerServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        output = new StringWriter();
        writer = new PrintWriter(output);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testGenericErrorWithoutException() throws Exception {
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            verify(dispatcher).include(request, response);
            assertTrue(result.contains("INTERNAL_SERVER_ERROR (500)"));
            assertTrue(result.contains("Internal Server Error"));
        }
    }

    @Test
    void testStoreExceptionHandledProperly() throws Exception {
        StoreException storeException = mock(StoreException.class);
        when(storeException.getMessage()).thenReturn("Custom DB error");
        when(storeException.getErrorCode()).thenReturn("DB_DOWN");
        when(storeException.getStatusCode()).thenReturn(503);

        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(503);
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(storeException);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("DB_DOWN (503)"));
            assertTrue(result.contains("Custom DB error"));
        }
    }

    @Test
    void testCustomerViewError() throws Exception {
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            mockUtil.when(() -> StoreUtil.setActiveTab(writer, "home")).thenAnswer(inv -> null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            verify(dispatcher).include(request, response);
            assertTrue(result.contains("PAGE_NOT_FOUND (404)"));
            assertTrue(result.contains("The Page You are Searching For is Not available"));
        }
    }

    @Test
    void testSellerViewError() throws Exception {
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(403);
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);
        when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            mockUtil.when(() -> StoreUtil.setActiveTab(writer, "home")).thenAnswer(inv -> null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            verify(dispatcher).include(request, response);
            assertTrue(result.contains("ACCESS_DENIED (403)"));
            assertTrue(result.contains("Please Login First to continue"));
        }
    }

    @Test
    void testGuestViewWithScriptInjection() throws Exception {
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(400);
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);
        when(request.getRequestDispatcher("index.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("BAD_REQUEST (400)"));
            assertTrue(result.contains("Bad Request, Please Try Again"));
            assertTrue(result.contains("document.getElementById('topmid').innerHTML='';"));
        }
    }
}
