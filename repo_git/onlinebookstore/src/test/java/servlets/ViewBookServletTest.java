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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ViewBookServletTest {

    private ViewBookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;
    private BookService mockBookService;

    @BeforeEach
    void setUp() throws Exception {
        mockBookService = mock(BookService.class);
        servlet = new ViewBookServlet(mockBookService);

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
        try (MockedStatic<StoreUtil> storeUtilMock = mockStatic(StoreUtil.class)) {
            storeUtilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(responseWriter.toString().contains("Please Login First to Continue"));
        }
    }

    @Test
    void testDisplayBooksIfLoggedIn() throws Exception {
        List<Book> mockBooks = List.of(new Book("ABC123", "Test Book", "Author", 10.0, 5));

        try (MockedStatic<StoreUtil> storeUtilMock = mockStatic(StoreUtil.class)) {
            storeUtilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            storeUtilMock.when(() -> StoreUtil.setActiveTab(any(), eq("books"))).thenAnswer(inv -> null);
            storeUtilMock.when(() -> StoreUtil.updateCartItems(request)).thenAnswer(inv -> null);
            when(mockBookService.getAllBooks()).thenReturn(mockBooks);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Available Books"));
            assertTrue(responseWriter.toString().contains("Test Book"));
        }
    }

    @Test
    void testAddBookToCard_Available_NotInCart() {
        Book book = new Book("ABC123", "Clean Code", "Robert C. Martin", 30.0, 10);
        when(session.getAttribute("qty_ABC123")).thenReturn(null);

        String cardHtml = servlet.addBookToCard(session, book);

        assertTrue(cardHtml.contains("Add To Cart"));
        assertTrue(cardHtml.contains("Clean Code"));
    }

    @Test
    void testAddBookToCard_Available_InCart() {
        Book book = new Book("ABC123", "Clean Code", "Robert C. Martin", 30.0, 10);
        when(session.getAttribute("qty_ABC123")).thenReturn(3);

        String cardHtml = servlet.addBookToCard(session, book);

        assertTrue(cardHtml.contains("glyphicon glyphicon-minus"));
        assertTrue(cardHtml.contains("3"));
    }

    @Test
    void testAddBookToCard_OutOfStock() {
        Book book = new Book("XYZ999", "Out of Stock Book", "Author", 20.0, 0);

        String cardHtml = servlet.addBookToCard(session, book);

        assertTrue(cardHtml.contains("Out Of Stock"));
        assertTrue(cardHtml.contains("Out of Stock Book"));
    }

    @Test
    void testExceptionHandlingDuringService() throws Exception {
        try (MockedStatic<StoreUtil> storeUtilMock = mockStatic(StoreUtil.class)) {
            storeUtilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            storeUtilMock.when(() -> StoreUtil.setActiveTab(any(), anyString())).thenAnswer(inv -> null);
            storeUtilMock.when(() -> StoreUtil.updateCartItems(request)).thenAnswer(inv -> null);
            when(mockBookService.getAllBooks()).thenThrow(new RuntimeException("DB error"));

            servlet.service(request, response);

            assertTrue(true);
        }
    }

    @Test
    void testGetBookServiceReturnsInjectedService() {
        BookService mockBookService = mock(BookService.class);
        ViewBookServlet servlet = new ViewBookServlet(mockBookService);

        assertSame(mockBookService, servlet.getBookService());
    }
}
