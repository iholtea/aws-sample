package ionuth.filters;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class CORSFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletResponse httpresponse = (HttpServletResponse)response;
        httpresponse.setHeader("Access-Control-Allow-Origin","*");
        httpresponse.setHeader("Access-Control-Allow-Methods",
        		"GET, POST, OPTIONS, PUT, PATCH, DELETE");
        httpresponse.setHeader("Access-Control-Allow-Headers","*");
        httpresponse.setHeader("Access-Control-Expose-Headers","*");
        // age is used to invalidate the cached pre-fligth response
        // that the browser keeps. Useful if we change the configuration
        //httpresponse.setHeader("Access-Control-Max-Age","3600");
        chain.doFilter(request, httpresponse);
		
	}
	

}
