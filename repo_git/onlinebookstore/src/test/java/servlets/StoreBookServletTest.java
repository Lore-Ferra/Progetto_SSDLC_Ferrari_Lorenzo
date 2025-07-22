package servlets;

import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
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
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

class StoreBookServletTest {

    private StoreBookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private BookService mockBookService;
    private StringWriter outputWriter;

    @BeforeEach
    void setUp() throws IOException {
        mockBookService = mock(BookService.class);
        servlet = new StoreBookServlet(mockBookService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        outputWriter = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(new PrintWriter(outputWriter));
    }

    @Test
    void shouldRedirectIfNotLoggedIn() throws ServletException, IOException {
        try (MockedStatic<StoreUtil> mockedUtil = mockStatic(StoreUtil.class)) {
            mockedUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);
            when(request.getRequestDispatcher("SellerLogin.html")).thenReturn(dispatcher);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assert outputWriter.toString().contains("Please Login First to Continue");
        }
    }

    @Test
    void shouldRenderEmptyBookListMessage() throws ServletException, IOException {
        try (MockedStatic<StoreUtil> mockedUtil = mockStatic(StoreUtil.class)) {
            mockedUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            when(mockBookService.getAllBooks()).thenReturn(Collections.emptyList());
            when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assert outputWriter.toString().contains("No Books Available in the store");
        }
    }

    @Test
    void shouldRenderListOfBooks() throws ServletException, IOException {
        Book book = new Book("123", "Test Book", "Author", 99.99, 5);
        try (MockedStatic<StoreUtil> mockedUtil = mockStatic(StoreUtil.class)) {
            mockedUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            when(mockBookService.getAllBooks()).thenReturn(Arrays.asList(book));
            when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

            servlet.service(request, response);

            String output = outputWriter.toString();
            assert output.contains("Test Book");
            assert output.contains("&#8377;");
            assert output.contains("updatebook");
        }
    }

    @Test
    void shouldHandleExceptionGracefully() throws ServletException, IOException {
        try (MockedStatic<StoreUtil> mockedUtil = mockStatic(StoreUtil.class)) {
            mockedUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            when(mockBookService.getAllBooks()).thenThrow(new RuntimeException("DB error"));
            when(request.getRequestDispatcher("SellerHome.html")).thenReturn(dispatcher);

            servlet.service(request, response);

            String output = outputWriter.toString();
            assert output.contains("Books Available In the Store");
        }
    }
}
