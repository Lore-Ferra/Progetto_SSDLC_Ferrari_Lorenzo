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
    private UserService authService = new UserServiceImpl();

    public LogoutServlet() {
        this(new UserServiceImpl());
    }

    public LogoutServlet(UserService authService) {
        this.authService = authService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        try (PrintWriter pw = res.getWriter()) {
            boolean logout = authService.logout(req.getSession());

            includeLoginPage(req, res);

            if (logout) {
                printLogoutMessage(pw);
            }

        } catch (IOException ioEx) {
            logSevere(() -> "IOException durante il logout", ioEx);
            safelySendError(res, "Errore nel logout");

        } catch (Exception ex) {
            logSevere(() -> "Eccezione generica durante il logout", ex);
            safelySendError(res, "Errore generico nel logout");
        }
    }

    private void includeLoginPage(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
    rd.include(req, res);
    }

    private void printLogoutMessage(PrintWriter pw) {
        pw.println("<table class=\"tab\"><tr><td>Successfully logged out!</td></tr></table>");
    }

    private void safelySendError(HttpServletResponse res, String message) {
        try {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        } catch (IOException e) {
            logSevere(() -> "Errore durante sendError(): " + message, e);
        }
    }

    private void logSevere(java.util.function.Supplier<String> msgSupplier, Throwable throwable) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, msgSupplier.get(), throwable);
        }
    }
}
