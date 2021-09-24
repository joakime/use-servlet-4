package demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DumpServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        Class<?> clazz = resp.getClass();
        out.printf("HttpServletResponse = %s (%s)%n", clazz.getName(), getLocation(clazz));
        out.printf("HttpServletResponse methods%n");
        for (Method method : clazz.getMethods())
        {
            out.printf("  %s%n", method);
        }
    }

    private String getLocation(Class<?> clazz)
    {
        return getCodeSourceLocation(clazz).toASCIIString();
    }

    public static URI getCodeSourceLocation(Class<?> clazz)
    {
        try
        {
            ProtectionDomain domain = AccessController.doPrivileged((PrivilegedAction<ProtectionDomain>)() -> clazz.getProtectionDomain());
            if (domain != null)
            {
                CodeSource source = domain.getCodeSource();
                if (source != null)
                {
                    URL location = source.getLocation();

                    if (location != null)
                    {
                        return location.toURI();
                    }
                }
            }
        }
        catch (URISyntaxException ignored)
        {
        }
        return null;
    }
}
