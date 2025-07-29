package servlets;

import com.bittercode.constant.ResponseCode;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UpdateBookServletTest {

    private UpdateBookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;
    private BookService mockBookService;

    @BeforeEach
    void setUp() throws Exception {
        mockBookService = mock(BookService.class);
        servlet = new UpdateBookServlet(mockBookService);

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
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(responseWriter.toString().contains("Please Login First to Continue"));
        }
    }

    @Test
    void testUpdateBookSuccessfully() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            mockUpdateFormInput();
            when(mockBookService.updateBook(any(Book.class))).thenReturn(ResponseCode.SUCCESS.name());
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("storebooks"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Book Detail Updated Successfully!"));
        }
    }

    @Test
    void testUpdateBookFails() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            mockUpdateFormInput();
            when(mockBookService.updateBook(any(Book.class))).thenReturn(ResponseCode.FAILURE.name());
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("storebooks"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Failed to Update Book"));
        }
    }

    @Test
    void testShowUpdateForm() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            Book book = new Book("ABC123", "Book Name", "Author", 20.0, 10);
            when(request.getParameter("updateFormSubmitted")).thenReturn(null);
            when(request.getParameter("bookId")).thenReturn("ABC123");
            when(mockBookService.getBookById("ABC123")).thenReturn(book);
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("storebooks"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Enter Book Code"));
        }
    }

    @Test
    void testExceptionHandling() throws Exception {
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            when(request.getParameter("updateFormSubmitted")).thenReturn("true");
            when(request.getParameter(BooksDBConstants.COLUMN_NAME)).thenThrow(new RuntimeException("Test Exception"));
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("storebooks"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Failed to Load Book data"));
        }
    }

    private void mockUpdateFormInput() {
        when(request.getParameter("updateFormSubmitted")).thenReturn("true");
        when(request.getParameter(BooksDBConstants.COLUMN_NAME)).thenReturn("New Book");
        when(request.getParameter(BooksDBConstants.COLUMN_BARCODE)).thenReturn("ABC123");
        when(request.getParameter(BooksDBConstants.COLUMN_AUTHOR)).thenReturn("John Doe");
        when(request.getParameter(BooksDBConstants.COLUMN_PRICE)).thenReturn("15.99");
        when(request.getParameter(BooksDBConstants.COLUMN_QUANTITY)).thenReturn("5");
    }
}
