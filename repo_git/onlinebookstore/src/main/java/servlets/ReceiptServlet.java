package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

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

public class ReceiptServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ReceiptServlet.class.getName());
    private static final BookService bookService = new BookServiceImpl();

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\"><tr><td>Please Login First to Continue!!</td></tr></table>");
            return;
        }
        try {
            List<Book> books = bookService.getAllBooks();
            int i = 0;
            RequestDispatcher rd = req.getRequestDispatcher("CustomerHome.html");
            rd.include(req, res);
            StoreUtil.setActiveTab(pw, "cart");
            pw.println("<div class=\"tab\">Your order status is as below</div>");
            pw.println(
                    "<div class=\"tab\">\r\n" + "		<table>\r\n" + "			<tr>\r\n" + "				\r\n"
                            + "				<th>Book Code</th>\r\n" + "				<th>Book Name</th>\r\n"
                            + "				<th>Book Author</th>\r\n" + "				<th>Book Price</th>\r\n"
                            + "				<th>Quantity</th>\r\n" + "				<th>Amount</th>\r\n"
                            + "			</tr>");
            double total = 0.0;
            for (Book book : books) {
                double bPrice = book.getPrice();
                i = i + 1;

                String qt = "qty" + Integer.toString(i);
                int quantity = Integer.parseInt(req.getParameter(qt));
                total += processOrder(req, pw, book, i, bPrice, quantity);
            }
            pw.println("</table><br/><div class='tab'>Total Paid Amount: " + total + "</div>");
            // String fPay = req.getParameter("f_pay");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double processOrder(HttpServletRequest req, PrintWriter pw, Book book, int i, double bPrice, int quantity) {
        try {
            String check1 = "checked" + i;
            String getChecked = req.getParameter(check1);

            if (book.getQuantity() < quantity) {
                pw.println(
                        "</table><div class=\"tab\" style='color:red;'>Please Select the Qty less than Available Books Quantity</div>");
                return 0.0;
            }

            if ("pay".equals(getChecked)) {
                pw.println("<tr><td>" + book.getBarcode() + "</td>");
                pw.println("<td>" + book.getName() + "</td>");
                pw.println("<td>" + book.getAuthor() + "</td>");
                pw.println("<td>" + bPrice + "</td>");
                pw.println("<td>" + quantity + "</td>");
                double amount = bPrice * quantity;
                pw.println("<td>" + amount + "</td></tr>");

                int updatedQty = book.getQuantity() - quantity;
                logger.info("Updated quantity: " + updatedQty);
                bookService.updateBookQtyById(book.getBarcode(), updatedQty);
                return amount;
            }
        } catch (Exception e) {
            logger.severe("Errore nel processOrder: " + e.getMessage());
        }
        return 0.0;
    }
}
