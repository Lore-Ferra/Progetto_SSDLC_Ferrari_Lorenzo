package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;

import java.util.logging.Logger;

public class CheckoutServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(CheckoutServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);
        PrintWriter pw;

        try {
            pw = res.getWriter();
        } catch (IOException e) {
            logger.severe("Errore durante l'ottenimento del PrintWriter: " + e.getMessage());
            return;
        }

        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, req.getSession())) {
            try {
                RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
                rd.include(req, res);
                pw.println("<table class=\"tab\"><tr><td>Please Login First to Continue!!</td></tr></table>");
            } catch (ServletException | IOException e) {
                logger.severe("Errore durante il redirect alla pagina di login: " + e.getMessage());
            }
            return;
        }

        try {
            RequestDispatcher rd = req.getRequestDispatcher("payment.html");
            rd.include(req, res);

            StoreUtil.setActiveTab(pw, "cart");

            Object amount = req.getSession().getAttribute("amountToPay");

            pw.println("Total Amount<span class=\"price\" style=\"color: black\"><b>&#8377; "
                    + amount + "</b></span>");
            pw.println("<input type=\"submit\" value=\"Pay & Place Order\" class=\"btn\">");
            pw.println("</form></div></div></div></div>");

        } catch (ServletException | IOException e) {
            logger.severe("Errore durante l'elaborazione del checkout: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Errore generico nel checkout: " + e.getMessage());
        }
    }
}
