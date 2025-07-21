package servlets;

import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CheckoutServletTest {

    private CheckoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter outputWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CheckoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        outputWriter = new StringWriter();
        printWriter = new PrintWriter(outputWriter);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testRedirectsToLoginIfNotLoggedIn() throws Exception {
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockStatic = mockStatic(StoreUtil.class)) {
            mockStatic.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);

            servlet.doPost(request, response);
            printWriter.flush();

            verify(dispatcher).include(request, response);
            assertTrue(outputWriter.toString().contains("Please Login First to Continue!!"));
        }
    }

    @Test
    void testCheckoutWithValidSession() throws Exception {
        when(request.getRequestDispatcher("payment.html")).thenReturn(dispatcher);
        when(session.getAttribute("amountToPay")).thenReturn(500.0);

        try (MockedStatic<StoreUtil> mockStatic = mockStatic(StoreUtil.class)) {
            mockStatic.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            mockStatic.when(() -> StoreUtil.setActiveTab(printWriter, "cart")).thenAnswer(inv -> null);

            servlet.doPost(request, response);
            printWriter.flush();

            String result = outputWriter.toString();
            verify(dispatcher).include(request, response);
            assertTrue(result.contains("Total Amount"));
            assertTrue(result.contains("&#8377; 500.0"));
            assertTrue(result.contains("Pay & Place Order"));
        }
    }

    @Test
    void testCheckoutHandlesNullAmount() {
        when(request.getRequestDispatcher("payment.html")).thenReturn(dispatcher);
        when(session.getAttribute("amountToPay")).thenReturn(null);

        try (MockedStatic<StoreUtil> mockStatic = mockStatic(StoreUtil.class)) {
            mockStatic.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            mockStatic.when(() -> StoreUtil.setActiveTab(printWriter, "cart")).thenAnswer(inv -> null);

            servlet.doPost(request, response);
            printWriter.flush();

            String result = outputWriter.toString();
            assertTrue(result.contains("Total Amount"));
            assertTrue(result.contains("&#8377; null"));
        }
    }

    @Test
    void testHandlesIOExceptionFromGetWriter() throws Exception {
        HttpServletResponse brokenResponse = mock(HttpServletResponse.class);
        when(brokenResponse.getWriter()).thenThrow(new IOException("Simulated IO error"));

        servlet.doPost(request, brokenResponse);
    }

    @Test
    void testHandlesServletExceptionDuringLoginRedirect() throws Exception {
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> mockStatic = mockStatic(StoreUtil.class)) {
            mockStatic.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);
            doThrow(new ServletException("Boom")).when(dispatcher).include(request, response);

            servlet.doPost(request, response);
        }
    }

    @Test
    void testHandlesExceptionDuringPaymentProcessing() throws Exception {
        when(request.getRequestDispatcher("payment.html")).thenReturn(dispatcher);
        when(session.getAttribute("amountToPay")).thenReturn("Boom");

        try (MockedStatic<StoreUtil> mockStatic = mockStatic(StoreUtil.class)) {
            mockStatic.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            mockStatic.when(() -> StoreUtil.setActiveTab(printWriter, "cart")).thenThrow(new RuntimeException("Generic error"));

            servlet.doPost(request, response);
        }
    }
}
