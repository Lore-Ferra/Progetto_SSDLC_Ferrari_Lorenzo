package servlets;

import com.bittercode.constant.db.BooksDBConstants;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AddBookServletTest {

    private AddBookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private StringWriter responseWriter;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new AddBookServlet();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        responseWriter = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    public void testRedirectToLoginWhenNotLoggedIn() throws Exception {
        try (MockedStatic<StoreUtil> storeUtil = mockStatic(StoreUtil.class)) {
            storeUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(responseWriter.toString().contains("Please Login First"));
        }
    }

    @Test
    public void testShowFormWhenBookNameIsNull() throws Exception {
        try (MockedStatic<StoreUtil> storeUtil = mockStatic(StoreUtil.class)) {
            storeUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            storeUtil.when(() -> StoreUtil.setActiveTab(any(), eq("addbook"))).thenCallRealMethod();

            when(request.getParameter(BooksDBConstants.COLUMN_NAME)).thenReturn(null);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(responseWriter.toString().contains("<form action=\"addbook\" method=\"post\">"));
        }
    }

    @Test
    public void testAddBookSuccess() throws Exception {
        try (MockedStatic<StoreUtil> storeUtil = mockStatic(StoreUtil.class)) {
            storeUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            storeUtil.when(() -> StoreUtil.setActiveTab(any(), eq("addbook"))).thenCallRealMethod();

            when(request.getParameter(BooksDBConstants.COLUMN_NAME)).thenReturn("Test Book");
            when(request.getParameter(BooksDBConstants.COLUMN_AUTHOR)).thenReturn("Author");
            when(request.getParameter(BooksDBConstants.COLUMN_PRICE)).thenReturn("10.5");
            when(request.getParameter(BooksDBConstants.COLUMN_QUANTITY)).thenReturn("3");

            BookService mockService = mock(BookService.class);
            when(mockService.addBook(any(Book.class))).thenReturn("SUCCESS");

            AddBookServlet.setBookService(mockService);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Book Detail Updated Successfully"));
        }
    }

    @Test
    public void testAddBookFailureInvalidInput() throws Exception {
        try (MockedStatic<StoreUtil> storeUtil = mockStatic(StoreUtil.class)) {
            storeUtil.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            storeUtil.when(() -> StoreUtil.setActiveTab(any(), eq("addbook"))).thenCallRealMethod();

            when(request.getParameter(BooksDBConstants.COLUMN_NAME)).thenReturn("Book");
            when(request.getParameter(BooksDBConstants.COLUMN_AUTHOR)).thenReturn("Author");
            when(request.getParameter(BooksDBConstants.COLUMN_PRICE)).thenReturn("invalid");
            when(request.getParameter(BooksDBConstants.COLUMN_QUANTITY)).thenReturn("3");

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Failed to Add Books"));
        }
    }
}
