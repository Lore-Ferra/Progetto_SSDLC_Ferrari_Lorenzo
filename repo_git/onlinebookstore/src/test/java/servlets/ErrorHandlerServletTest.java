package servlets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bittercode.model.StoreException;
import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class ErrorHandlerServletTest {

    private ErrorHandlerServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter output;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ErrorHandlerServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        output = new StringWriter();
        writer = new PrintWriter(output, true);

        when(request.getSession(false)).thenReturn(session);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        doNothing().when(dispatcher).include(any(), any());
    }

    @Test
    void testCustomerViewError() throws Exception {
        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            mockUtil.when(() -> StoreUtil.setActiveTab(writer, "home")).thenReturn(null);

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("PAGE_NOT_FOUND"), "Expected PAGE_NOT_FOUND error message");
        }
    }

    @Test
    void testSellerViewError() throws Exception {
        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            mockUtil.when(() -> StoreUtil.setActiveTab(writer, "home")).thenReturn(null);

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("INTERNAL_SERVER_ERROR"), "Expected INTERNAL_SERVER_ERROR for seller");
        }
    }

    @Test
    public void testGuestViewWithScriptInjection() throws Exception {
        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("PAGE_NOT_FOUND"));
            assertTrue(result.contains("document.getElementById('topmid').innerHTML='';"));
        }
    }

    @Test
    public void testStoreExceptionHandledProperly() throws Exception {
        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            StoreException storeException = new StoreException(503, "DB_DOWN", "Custom DB error");

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(503);
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(storeException);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("DB_DOWN"));
            assertTrue(result.contains("Custom DB error"));
        }
    }

    @Test
    public void testGenericErrorWithoutException() throws Exception {
        try (MockedStatic<StoreUtil> mockUtil = mockStatic(StoreUtil.class)) {
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            mockUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(400);
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(null);

            servlet.service(request, response);
            writer.flush();

            String result = output.toString();
            assertTrue(result.contains("BAD_REQUEST"));
        }
    }
}
