package com.bittercode.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.BooksDBConstants;
import com.bittercode.model.Book;
import com.bittercode.model.StoreException;
import com.bittercode.service.BookService;
import com.bittercode.util.DBUtil;

public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private static final String SELECT_ALL_FROM = "SELECT * FROM ";
    private static final String SQL_WHERE = " WHERE ";
    private static final String GET_ALL_BOOKS_QUERY = SELECT_ALL_FROM + BooksDBConstants.TABLE_BOOK;
    private static final String GET_BOOK_BY_ID_QUERY = SELECT_ALL_FROM + BooksDBConstants.TABLE_BOOK
            + SQL_WHERE + BooksDBConstants.COLUMN_BARCODE + " = ?";
    private static final String DELETE_BOOK_BY_ID_QUERY = "DELETE FROM " + BooksDBConstants.TABLE_BOOK
            + SQL_WHERE + BooksDBConstants.COLUMN_BARCODE + "=?";
    private static final String ADD_BOOK_QUERY = "INSERT INTO " + BooksDBConstants.TABLE_BOOK + " VALUES(?,?,?,?,?)";
    private static final String UPDATE_BOOK_QTY_BY_ID_QUERY = "UPDATE " + BooksDBConstants.TABLE_BOOK + " SET "
            + BooksDBConstants.COLUMN_QUANTITY + "=? " + SQL_WHERE + BooksDBConstants.COLUMN_BARCODE + "=?";
    private static final String UPDATE_BOOK_BY_ID_QUERY = "UPDATE " + BooksDBConstants.TABLE_BOOK + " SET "
            + BooksDBConstants.COLUMN_NAME + "=? , "
            + BooksDBConstants.COLUMN_AUTHOR + "=?, "
            + BooksDBConstants.COLUMN_PRICE + "=?, "
            + BooksDBConstants.COLUMN_QUANTITY + "=? "
            + SQL_WHERE + BooksDBConstants.COLUMN_BARCODE + "=?";

    @Override
    public Book getBookById(String bookId) throws StoreException {
        Book book = null;
        Connection con = DBUtil.getConnection();
        try (PreparedStatement ps = con.prepareStatement(GET_BOOK_BY_ID_QUERY)) {
            ps.setString(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bCode = rs.getString(1);
                    String bName = rs.getString(2);
                    String bAuthor = rs.getString(3);
                    int bPrice = rs.getInt(4);
                    int bQty = rs.getInt(5);

                    book = new Book(bCode, bName, bAuthor, bPrice, bQty);
                }
            }
        } catch (SQLException e) {
            throw new StoreException("Error retrieving book by ID: " + bookId + ". Cause: " + e.getMessage());
        }
        return book;
    }

    @Override
    public List<Book> getAllBooks() throws StoreException {
        List<Book> books = new ArrayList<>();
        Connection con = DBUtil.getConnection();

        try (
                PreparedStatement ps = con.prepareStatement(GET_ALL_BOOKS_QUERY);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String bCode = rs.getString(1);
                String bName = rs.getString(2);
                String bAuthor = rs.getString(3);
                int bPrice = rs.getInt(4);
                int bQty = rs.getInt(5);

                Book book = new Book(bCode, bName, bAuthor, bPrice, bQty);
                books.add(book);
            }
        } catch (SQLException e) {
            throw new StoreException("Error retrieving all books: " + e.getMessage());
        }

        return books;
    }

    @Override
    public String deleteBookById(String bookId) throws StoreException {
        String response = ResponseCode.FAILURE.name();
        Connection con = DBUtil.getConnection();

        try (PreparedStatement ps = con.prepareStatement(DELETE_BOOK_BY_ID_QUERY)) {
            ps.setString(1, bookId);
            int k = ps.executeUpdate();
            if (k == 1) {
                response = ResponseCode.SUCCESS.name();
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting book with ID: {}", bookId, e);
            response += " : " + e.getMessage();
        }

        return response;
    }

    @Override
    public String addBook(Book book) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        Connection con = DBUtil.getConnection();

        try (PreparedStatement ps = con.prepareStatement(ADD_BOOK_QUERY)) {
            ps.setString(1, book.getBarcode());
            ps.setString(2, book.getName());
            ps.setString(3, book.getAuthor());
            ps.setDouble(4, book.getPrice());
            ps.setInt(5, book.getQuantity());
            int k = ps.executeUpdate();
            if (k == 1) {
                responseCode = ResponseCode.SUCCESS.name();
            }
        } catch (Exception e) {
            LOGGER.error("Error adding book: {}, barcode: {}", book.getName(), book.getBarcode(), e);
            responseCode += " : " + e.getMessage();
        }

        return responseCode;
    }

    @Override
    public String updateBookQtyById(String bookId, int quantity) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        Connection con = DBUtil.getConnection();

        try (PreparedStatement ps = con.prepareStatement(UPDATE_BOOK_QTY_BY_ID_QUERY)) {
            ps.setInt(1, quantity);
            ps.setString(2, bookId);
            ps.executeUpdate();
            responseCode = ResponseCode.SUCCESS.name();
        } catch (Exception e) {
            LOGGER.error("Error updating quantity for book ID: {}", bookId, e);
            responseCode += " : " + e.getMessage();
        }

        return responseCode;
    }

    @Override
    public List<Book> getBooksByCommaSeparatedBookIds(String commaSeparatedBookIds) throws StoreException {
        List<Book> books = new ArrayList<>();

        if (commaSeparatedBookIds == null || commaSeparatedBookIds.trim().isEmpty()) {
            return books;
        }

        String[] ids = commaSeparatedBookIds.split(",");

        for (String id : ids) {
            if (!id.trim().matches("\\d+")) {
                LOGGER.warn("Invalid book ID format received: {}", id);
                throw new StoreException("Invalid book ID format: " + id);
            }
        }

        String placeholders = String.join(",", Collections.nCopies(ids.length, "?"));
        String query = SELECT_ALL_FROM + BooksDBConstants.TABLE_BOOK +
                SQL_WHERE + BooksDBConstants.COLUMN_BARCODE + " IN (" + placeholders + ")";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(query)) {

            for (int i = 0; i < ids.length; i++) {
                ps.setString(i + 1, ids[i].trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bCode = rs.getString(1);
                    String bName = rs.getString(2);
                    String bAuthor = rs.getString(3);
                    int bPrice = rs.getInt(4);
                    int bQty = rs.getInt(5);

                    books.add(new Book(bCode, bName, bAuthor, bPrice, bQty));
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error in getBooksByCommaSeparatedBookIds for input: {}", commaSeparatedBookIds, e);
            throw new StoreException(ResponseCode.DATABASE_CONNECTION_FAILURE);
        }

        return books;
    }

    @Override
    public String updateBook(Book book) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        Connection con = DBUtil.getConnection();

        try (PreparedStatement ps = con.prepareStatement(UPDATE_BOOK_BY_ID_QUERY)) {
            ps.setString(1, book.getName());
            ps.setString(2, book.getAuthor());
            ps.setDouble(3, book.getPrice());
            ps.setInt(4, book.getQuantity());
            ps.setString(5, book.getBarcode());
            ps.executeUpdate();
            responseCode = ResponseCode.SUCCESS.name();
        } catch (Exception e) {
            LOGGER.error("Error updating book: {}", book.getBarcode(), e);
            responseCode += " : " + e.getMessage();
        }

        return responseCode;
    }
}
