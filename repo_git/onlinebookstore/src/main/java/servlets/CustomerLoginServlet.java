package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.StoreException;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import com.bittercode.service.impl.UserServiceImpl;

public class CustomerLoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CustomerLoginServlet.class);
    private final UserService authService = new UserServiceImpl();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter pw;
        try {
            pw = res.getWriter();
        } catch (IOException e) {
            logger.error("Failed to obtain response writer: {}", e.getMessage(), e);
            return;
        }

        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        String uName = req.getParameter(UsersDBConstants.COLUMN_USERNAME);
        String pWord = req.getParameter(UsersDBConstants.COLUMN_PASSWORD);

        User user = null;
        try {
            user = authService.login(UserRole.CUSTOMER, uName, pWord, req.getSession());
        } catch (StoreException e) {
            logger.error("StoreException during login: {}", e.getMessage(), e);
            try {
                RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
                rd.include(req, res);
                pw.println("<table class=\"tab\"><tr><td>Internal error occurred. Please try again later.</td></tr></table>");
            } catch (ServletException | IOException ex) {
                logger.error("Error including login page after StoreException: {}", ex.getMessage(), ex);
            }
            return;
        }

        try {
            if (user != null) {
                RequestDispatcher rd = req.getRequestDispatcher("CustomerHome.html");
                rd.include(req, res);
                pw.println("<div id=\"topmid\"><h1>Welcome to Online <br>Book Store</h1></div>");
                pw.println("<br>");
                pw.println("<table class=\"tab\">");
                pw.println("<tr><td><p>Welcome " + user.getFirstName() + ", Happy Learning !!</p></td></tr>");
                pw.println("</table>");
            } else {
                RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
                rd.include(req, res);
                pw.println("<table class=\"tab\"><tr><td>Incorrect UserName or PassWord</td></tr></table>");
            }
        } catch (Exception e) {
            logger.error("Unexpected error while handling login response: {}", e.getMessage(), e);
        }
    }
}
