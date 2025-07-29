package servlets;

import com.bittercode.model.Book;
import com.bittercode.model.Cart;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProcessPaymentServletTest {

    private ProcessPaymentServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private BookService bookService;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProcessPaymentServlet();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        bookService = mock(BookService.class);

        ProcessPaymentServlet.setBookService(bookService);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testRedirectsIfNotLoggedIn() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            String output = responseWriter.toString();
            assertTrue(output.contains("Please Login First to Continue"));
        }
    }

    @Test
    void testProcessesCartAndUpdatesBooks() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("cart"))).then(invocation -> null);

            Book mockBook = new Book();
            mockBook.setBarcode("ABC123");
            mockBook.setName("Java 101");
            mockBook.setAuthor("X Dev");
            mockBook.setPrice(450.0);
            mockBook.setQuantity(10);

            Cart cart = new Cart(mockBook, 2);

            when(session.getAttribute("cartItems")).thenReturn(Arrays.asList(cart));

            servlet.service(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Java 101"));
            assertTrue(output.contains("X Dev"));
            assertTrue(output.contains("ORDABC123TM"));

            verify(bookService).updateBookQtyById(eq("ABC123"), 8);

            verify(session).removeAttribute("qty_ABC123");
            verify(session).removeAttribute("amountToPay");
            verify(session).removeAttribute("cartItems");
            verify(session).removeAttribute("items");
            verify(session).removeAttribute("selectedBookId");
        }
    }
}
