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
import static org.mockito.Mockito.*;

class CartServletTest {

    private CartServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private BookService mockedBookService;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CartServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testRedirectsToLoginIfNotLoggedIn() throws Exception {
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn(null);

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(false);

            servlet.service(request, response);
            writer.flush();

            verify(dispatcher).include(request, response);
            assertTrue(stringWriter.toString().contains("Please Login First to Continue!!"));
        }
    }

    @Test
    void testCartDisplayWithBooks() throws Exception {
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn("user@example.com");
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);
        when(session.getAttribute("items")).thenReturn("B001");
        when(session.getAttribute("qty_B001")).thenReturn(2);

        Book book = new Book("B001", "Java Book", "Author", 100, 10);
        mockedBookService = mock(BookService.class);
        when(mockedBookService.getBooksByCommaSeperatedBookIds("B001")).thenReturn(Collections.singletonList(book));

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.updateCartItems(request)).thenAnswer(inv -> null);
            utilMock.when(() -> StoreUtil.setActiveTab(writer, "cart")).thenAnswer(inv -> null);

            java.lang.reflect.Field bookServiceField = CartServlet.class.getDeclaredField("bookService");
            bookServiceField.setAccessible(true);
            bookServiceField.set(servlet, mockedBookService);

            servlet.service(request, response);
            writer.flush();

            String output = stringWriter.toString();
            assertTrue(output.contains("Shopping Cart"));
            assertTrue(output.contains("Java Book"));
            assertTrue(output.contains("Total Amount To Pay"));
        }
    }


    @Test
    void testCartDisplayWhenEmpty() throws Exception {
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn("user@example.com");
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);
        when(session.getAttribute("items")).thenReturn(""); // nessun libro

        mockedBookService = mock(BookService.class);
        when(mockedBookService.getBooksByCommaSeperatedBookIds("")).thenReturn(Collections.emptyList());

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.updateCartItems(request)).thenCallRealMethod();
            utilMock.when(() -> StoreUtil.setActiveTab(writer, "cart")).thenCallRealMethod();

            java.lang.reflect.Field bookServiceField = CartServlet.class.getDeclaredField("bookService");
            bookServiceField.setAccessible(true);
            bookServiceField.set(servlet, mockedBookService);

            servlet.service(request, response);
            writer.flush();

            String output = stringWriter.toString();
            assertTrue(output.contains("No Items In the Cart"));
        }
    }
}
