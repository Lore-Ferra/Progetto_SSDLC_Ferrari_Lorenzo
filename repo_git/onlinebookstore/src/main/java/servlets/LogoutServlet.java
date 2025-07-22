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
import com.bittercode.service.UserService;
import com.bittercode.service.impl.UserServiceImpl;

public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());
    private final UserService authService = new UserServiceImpl();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        try {
            PrintWriter pw = res.getWriter();
            boolean logout = authService.logout(req.getSession());

            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);

            // StoreUtil.setActiveTab(pw, "logout");

            if (logout) {
                pw.println("<table class=\"tab\"><tr><td>Successfully logged out!</td></tr></table>");
            }

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Errore durante il logout (IOException): " + e.getMessage(), e);
            }
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel logout");
            } catch (IOException sendErr) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Errore durante sendError(): " + sendErr.getMessage(), sendErr);
                }
            }

        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Errore generico durante il logout: " + e.getMessage(), e);
            }
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore generico nel logout");
            } catch (IOException sendErr) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Errore durante sendError(): " + sendErr.getMessage(), sendErr);
                }
            }
        }
    }
}
