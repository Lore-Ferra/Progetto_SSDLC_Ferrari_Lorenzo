package servlets;

import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.util.StoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReceiptServletTest {

    private ReceiptServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;
    private BookService mockBookService;

    @BeforeEach
    void setUp() throws Exception {
        mockBookService = mock(BookService.class);
        servlet = new ReceiptServlet(mockBookService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        responseWriter = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testRedirectIfNotLoggedIn() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(responseWriter.toString().contains("Please Login First to Continue"));
        }
    }

    @Test
    void testOrderProcessedSuccessfully() throws Exception {
        Book mockBook = new Book("B001", "Clean Code", "Robert C. Martin", 500.0, 10);

        try (
            MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class);
        ) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("cart"))).thenAnswer(inv -> null);
            when(mockBookService.getAllBooks()).thenReturn(Collections.singletonList(mockBook));

            when(request.getParameter("qty1")).thenReturn("2");
            when(request.getParameter("checked1")).thenReturn("pay");

            servlet.service(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Clean Code"));
            assertTrue(output.contains("Robert C. Martin"));
            assertTrue(output.contains("1000.0"));
            assertTrue(output.contains("Total Paid Amount"));

            verify(mockBookService).updateBookQtyById("B001", 8);
        }
    }

    @Test
    void testInsufficientQuantity() throws Exception {
        Book mockBook = new Book("B001", "Java in Depth", "John Doe", 300.0, 1);

        try (
            MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class);
        ) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("cart"))).thenAnswer(inv -> null);
            when(mockBookService.getAllBooks()).thenReturn(Collections.singletonList(mockBook));

            when(request.getParameter("qty1")).thenReturn("5"); // richiesta eccessiva
            when(request.getParameter("checked1")).thenReturn("pay");

            servlet.service(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Please Select the Qty less than Available Books Quantity"));

            verify(mockBookService, never()).updateBookQtyById(anyString(), anyInt());
        }
    }
}
