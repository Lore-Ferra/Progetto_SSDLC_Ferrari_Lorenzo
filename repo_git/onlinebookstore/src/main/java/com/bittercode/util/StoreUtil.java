package com.bittercode.util;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.bittercode.model.UserRole;

/*
 * Store UTil File To Store Commonly used methods
 */
public class StoreUtil {

    private StoreUtil() {
        // Prevent instantiation
    }

    /**
     * Check if the User is logged in with the requested role
     */
    public static boolean isLoggedIn(UserRole role, HttpSession session) {

        return session.getAttribute(role.toString()) != null;
    }

    /**
     * Modify the active tab in the page menu bar
     */
    public static void setActiveTab(PrintWriter pw, String activeTab) {

        pw.println("<script>document.getElementById(activeTab).classList.remove(\"active\");activeTab=" + activeTab
                + "</script>");
        pw.println("<script>document.getElementById('" + activeTab + "').classList.add(\"active\");</script>");

    }

    /**
     * Add/Remove/Update Item in the cart using the session
     */
    public static void updateCartItems(HttpServletRequest req) {
        final String ITEMS_KEY = "items";
        String selectedBookId = req.getParameter("selectedBookId");
        HttpSession session = req.getSession();

        if (selectedBookId == null)
            return;

        boolean isAdd = req.getParameter("addToCart") != null;
        String items = (String) session.getAttribute(ITEMS_KEY);

        if (isAdd) {
            items = addItemToCart(items, selectedBookId);
            session.setAttribute(ITEMS_KEY, items);
            int qty = getQuantity(session, selectedBookId) + 1;
            session.setAttribute("qty_" + selectedBookId, qty);
        } else {
            int qty = getQuantity(session, selectedBookId);
            if (qty > 1) {
                session.setAttribute("qty_" + selectedBookId, qty - 1);
            } else {
                session.removeAttribute("qty_" + selectedBookId);
                items = removeItemFromCart(items, selectedBookId);
                session.setAttribute(ITEMS_KEY, items);
            }
        }
    }

    private static int getQuantity(HttpSession session, String bookId) {
        Object qtyObj = session.getAttribute("qty_" + bookId);
        return (qtyObj instanceof Integer) ? (int) qtyObj : 0;
    }

    private static String addItemToCart(String items, String bookId) {
        if (items == null || items.isEmpty())
            return bookId;
        if (!items.contains(bookId))
            return items + "," + bookId;
        return items;
    }

    private static String removeItemFromCart(String items, String bookId) {
        if (items == null)
            return null;
        items = items.replace(bookId + ",", "")
                .replace("," + bookId, "")
                .replace(bookId, "");
        return items;
    }

}
