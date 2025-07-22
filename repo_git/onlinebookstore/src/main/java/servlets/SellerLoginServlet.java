package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import com.bittercode.service.impl.UserServiceImpl;

public class SellerLoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SellerLoginServlet.class.getName());

    private final UserService userService;

    public SellerLoginServlet() {
        this(new UserServiceImpl());
    }

    public SellerLoginServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        String uName = req.getParameter(UsersDBConstants.COLUMN_USERNAME);
        String pWord = req.getParameter(UsersDBConstants.COLUMN_PASSWORD);

        try {
            PrintWriter pw = res.getWriter();

            User user = userService.login(UserRole.SELLER, uName, pWord, req.getSession());
            if (user != null) {
                RequestDispatcher rd = req.getRequestDispatcher("SellerHome.html");
                rd.include(req, res);
                pw.println("<div id=\"topmid\"><h1>Welcome to Online <br>Book Store</h1></div>");
                pw.println("<br><table class=\"tab\"><tr><td><p>Welcome " + user.getFirstName() + ", Happy Learning !!</p></td></tr></table>");
            } else {
                RequestDispatcher rd = req.getRequestDispatcher("SellerLogin.html");
                rd.include(req, res);
                pw.println("<div class=\"tab\">Incorrect UserName or PassWord</div>");
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException while writing response", e);
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to write response");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "IOException while sending error response", ex);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception during seller login", e);
            try {
                PrintWriter pw = res.getWriter();
                pw.println("<div class=\"error\">Internal server error occurred. Please try again later.</div>");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "IOException while sending error response", ex);
            }
        }
    }
}
