package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

public class BuyBooksServlet extends HttpServlet {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(BuyBooksServlet.class.getName());
    private final BookService bookService;
    private static final String TD_CLOSE = "</td>";

    public BuyBooksServlet() {
        this(new BookServiceImpl());
    }

    public BuyBooksServlet(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        try {
            PrintWriter pw = res.getWriter();
            res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

            if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, req.getSession())) {
                redirectToLogin(req, res, pw);
                return;
            }

            showBooksToCustomer(req, res, pw);

        } catch (IOException e) {
            LOGGER.severe("Error while processing BuyBooksServlet: " + e.getMessage());
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) {
        try {
            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);
        } catch (IOException | ServletException e) {
            LOGGER.severe("Error while processing BuyBooksServlet: " + e.getMessage());
        }

        pw.println("<table class=\"tab\"><tr><td>Please Login First to Continue!!</td></tr></table>");
    }

    private void showBooksToCustomer(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) {
        try {
            List<Book> books = bookService.getAllBooks();
            RequestDispatcher rd = req.getRequestDispatcher("CustomerHome.html");
            rd.include(req, res);

            StoreUtil.setActiveTab(pw, "cart");

            pw.println("<div class=\"tab hd brown \">Books Available In Our Store</div>");
            pw.println("<div class=\"tab\"><form action=\"buys\" method=\"post\">");

            pw.println("<table><tr>" +
                    "<th>Books</th><th>Code</th><th>Name</th><th>Author</th><th>Price</th><th>Avail</th><th>Qty</th>" +
                    "</tr>");

            int i = 0;
            for (Book book : books) {
                String n = "checked" + (++i);
                String q = "qty" + i;

                pw.println("<tr>");
                pw.println("<td><input type=\"checkbox\" name=\"" + n + "\" value=\"pay\"></td>");
                pw.println("<td>" + book.getBarcode() + TD_CLOSE);
                pw.println("<td>" + book.getName() + TD_CLOSE);
                pw.println("<td>" + book.getAuthor() + TD_CLOSE);
                pw.println("<td>" + book.getPrice() + TD_CLOSE);
                pw.println("<td>" + book.getQuantity() + TD_CLOSE);
                pw.println("<td><input type=\"text\" name=\"" + q + "\" value=\"0\" style=\"text-align:center\"></td>");
                pw.println("</tr>");
            }

            pw.println("</table><input type=\"submit\" value=\" PAY NOW \"><br/></form></div>");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while showing books to customer", e);
        }
    }
}
