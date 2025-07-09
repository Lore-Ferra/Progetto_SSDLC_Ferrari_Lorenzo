package com.bittercode.service.util;

import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

class StoreUtilTest {

    private HttpServletRequest request;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void testAddToCartFirstItem() {
        when(request.getParameter("selectedBookId")).thenReturn("book1");
        when(request.getParameter("addToCart")).thenReturn("true");
        when(session.getAttribute("items")).thenReturn(null);
        when(session.getAttribute("qty_book1")).thenReturn(null);

        StoreUtil.updateCartItems(request);

        verify(session).setAttribute("items", "book1");
        verify(session).setAttribute("qty_book1", 1);
    }

    @Test
    void testAddToCartExistingItems() {
        when(request.getParameter("selectedBookId")).thenReturn("book2");
        when(request.getParameter("addToCart")).thenReturn("true");
        when(session.getAttribute("items")).thenReturn("book1");
        when(session.getAttribute("qty_book2")).thenReturn(1);

        StoreUtil.updateCartItems(request);

        verify(session).setAttribute("items", "book1,book2");
        verify(session).setAttribute("qty_book2", 2);
    }

    @Test
    void testRemoveFromCartReduceQuantity() {
        when(request.getParameter("selectedBookId")).thenReturn("book3");
        when(request.getParameter("addToCart")).thenReturn(null);
        when(session.getAttribute("qty_book3")).thenReturn(2);
        when(session.getAttribute("items")).thenReturn("book1,book3");

        StoreUtil.updateCartItems(request);

        verify(session).setAttribute("qty_book3", 1);
    }

    @Test
    void testRemoveFromCartLastItem() {
        when(request.getParameter("selectedBookId")).thenReturn("book4");
        when(request.getParameter("addToCart")).thenReturn(null); // rimozione
        when(session.getAttribute("qty_book4")).thenReturn(1);
        when(session.getAttribute("items")).thenReturn("book4,book5");

        StoreUtil.updateCartItems(request);

        verify(session).removeAttribute("qty_book4");
        verify(session).setAttribute("items", "book5");
    }

    @Test
    void testIsLoggedInReturnsTrue() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("SELLER")).thenReturn("seller@example.com");

        boolean result = StoreUtil.isLoggedIn(UserRole.SELLER, mockSession);

        assertTrue(result);
    }

    @Test
    void testIsLoggedInReturnsFalse() {
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("CUSTOMER")).thenReturn(null);

        boolean result = StoreUtil.isLoggedIn(UserRole.CUSTOMER, mockSession);

        assertFalse(result);
    }

    @Test
    void testSetActiveTab() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String activeTab = "books";

        StoreUtil.setActiveTab(pw, activeTab);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("document.getElementById(activeTab).classList.remove(\"active\")"));
        assertTrue(output.contains("document.getElementById('books').classList.add(\"active\")"));
    }
}
