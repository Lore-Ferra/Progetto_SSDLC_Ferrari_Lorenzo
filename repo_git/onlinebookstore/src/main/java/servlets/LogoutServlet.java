package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.service.UserService;
import com.bittercode.service.impl.UserServiceImpl;

public class LogoutServlet extends HttpServlet {

    UserService authService = new UserServiceImpl();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);
        
        try {
            PrintWriter pw = res.getWriter();

            boolean logout = authService.logout(req.getSession());

            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);
//            StoreUtil.setActiveTab(pw, "logout");
            if (logout) {
                pw.println("<table class=\"tab\"><tr><td>Successfully logged out!</td></tr></table>");
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'ottenimento del PrintWriter: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel logout");
        } catch (Exception e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore generico nel logout");
        }
    }
}