import connector.ConnectorUtils;
import connector.HttpStatus;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeServer implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        out.println(ConnectorUtils.renderStatus(HttpStatus.SC_OK));
        out.println("What time is it now? ");
        out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
