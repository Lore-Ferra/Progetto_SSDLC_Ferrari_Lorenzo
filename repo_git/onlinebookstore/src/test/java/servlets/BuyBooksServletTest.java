package servlets;

import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
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

class BuyBooksServletTest {

    private BuyBooksServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BuyBooksServlet();

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
    void testRedirectToLoginWhenNotLoggedIn() throws Exception {
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn(null);
        when(request.getRequestDispatcher("CustomerLogin.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(dispatcher).include(request, response);
        writer.flush();
        assert stringWriter.toString().contains("Please Login First to Continue!!");
    }

    @Test
    void testShowBooksToCustomerWhenLoggedIn() throws Exception {
        when(session.getAttribute(UserRole.CUSTOMER.toString())).thenReturn("user@x.it");
        when(request.getRequestDispatcher("CustomerHome.html")).thenReturn(dispatcher);

        try (MockedStatic<StoreUtil> storeUtilMock = mockStatic(StoreUtil.class);
             MockedStatic<BookServiceImpl> bookServiceMock = mockStatic(BookServiceImpl.class)) {

            storeUtilMock.when(() -> StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)).thenReturn(true);

            Book book = new Book("B001", "Title", "Author", 10, 5);
            BookService mockedBookService = mock(BookService.class);
            when(mockedBookService.getAllBooks()).thenReturn(Collections.singletonList(book));

            bookServiceMock.when(BookServiceImpl::new).thenReturn(mockedBookService);

            servlet.doPost(request, response);

            writer.flush();
            String html = stringWriter.toString();
            assertTrue(html.contains("Books Available In Our Store"));
            assertTrue(html.contains("Title"));
        }
    }
}
