package servlets;

import com.bittercode.constant.ResponseCode;
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

class RemoveBookServletTest {

    private RemoveBookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;
    private BookService mockBookService;

    @BeforeEach
    void setUp() throws Exception {
        mockBookService = mock(BookService.class);
        servlet = new RemoveBookServlet() {
            @Override
            protected BookService getBookService() {
                return mockBookService;
            }
        };

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
    void testShowFormWhenBookIdMissing() throws Exception {
        when(request.getParameter("bookId")).thenReturn(null);
        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("removebook"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Enter BookId to Remove"));
        }
    }

    @Test
    void testBookRemovedSuccessfully() throws Exception {
        when(request.getParameter("bookId")).thenReturn("ABC123");
        when(mockBookService.deleteBookById("ABC123")).thenReturn(ResponseCode.SUCCESS.name());

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("removebook"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Book Removed Successfully"));
            verify(mockBookService).deleteBookById("ABC123");
        }
    }

    @Test
    void testBookRemovalFails() throws Exception {
        when(request.getParameter("bookId")).thenReturn("XYZ999");
        when(mockBookService.deleteBookById("XYZ999")).thenReturn(ResponseCode.FAILURE.name());

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("removebook"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Book Not Available In The Store"));
        }
    }

    @Test
    void testExceptionHandling() throws Exception {
        when(request.getParameter("bookId")).thenThrow(new RuntimeException("Test Exception"));

        try (MockedStatic<StoreUtil> utilMock = mockStatic(StoreUtil.class)) {
            utilMock.when(() -> StoreUtil.isLoggedIn(UserRole.SELLER, session)).thenReturn(true);
            utilMock.when(() -> StoreUtil.setActiveTab(any(), eq("removebook"))).thenAnswer(inv -> null);

            servlet.service(request, response);

            assertTrue(responseWriter.toString().contains("Failed to Remove Books"));
        }
    }
}
