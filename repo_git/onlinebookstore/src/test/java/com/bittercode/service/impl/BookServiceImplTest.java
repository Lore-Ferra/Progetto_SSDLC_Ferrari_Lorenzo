package com.bittercode.service.impl;

import com.bittercode.model.Book;
import com.bittercode.util.DBUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    private BookServiceImpl bookService;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DBUtil> dbUtilMockedStatic;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl();

        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        dbUtilMockedStatic = mockStatic(DBUtil.class);
        dbUtilMockedStatic.when(DBUtil::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        if (dbUtilMockedStatic != null) {
            dbUtilMockedStatic.close();
        }
    }

    @Test
    void testGetAllBooksReturnsList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString(1)).thenReturn("001");
        when(mockResultSet.getString(2)).thenReturn("Book Title");
        when(mockResultSet.getString(3)).thenReturn("Author");
        when(mockResultSet.getInt(4)).thenReturn(100);
        when(mockResultSet.getInt(5)).thenReturn(10);

        List<Book> books = bookService.getAllBooks();

        assertEquals(1, books.size());
        assertEquals("001", books.get(0).getBarcode());
    }

    @Test
    void testAddBookReturnsSuccess() throws Exception {
        Book book = new Book("001", "Book Title", "Author", 100, 10);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        String result = bookService.addBook(book);

        assertEquals("SUCCESS", result);
    }

    @Test
    void testDeleteBookReturnsSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        String result = bookService.deleteBookById("001");

        assertEquals("SUCCESS", result);
    }

    @Test
    void testUpdateBookQtyReturnsSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        String result = bookService.updateBookQtyById("001", 20);

        assertEquals("SUCCESS", result);
    }

    @Test
    void testUpdateBookReturnsSuccess() throws Exception {
        Book book = new Book("001", "Book Title", "Author", 100, 10);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        String result = bookService.updateBook(book);

        assertEquals("SUCCESS", result);
    }

    @Test
    void testGetBookByIdReturnsBook() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString(1)).thenReturn("001");
        when(mockResultSet.getString(2)).thenReturn("Book Title");
        when(mockResultSet.getString(3)).thenReturn("Author");
        when(mockResultSet.getInt(4)).thenReturn(100);
        when(mockResultSet.getInt(5)).thenReturn(10);

        Book book = bookService.getBookById("001");

        assertNotNull(book);
        assertEquals("001", book.getBarcode());
    }

    @Test
    void testAddBookReturnsFailure() throws Exception {
        Book book = new Book("002", "Book Title", "Author", 100, 10);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        String result = bookService.addBook(book);

        assertTrue(result.startsWith("FAILURE"));
    }

    @Test
    void testGetAllBooksSQLException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        List<Book> books = bookService.getAllBooks();

        assertNotNull(books);
        assertEquals(0, books.size());
    }
}
