package ionuth.filters;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class AuthenticationFilter implements Filter {
	
	@Autowired
	private SecretKey jwtKey;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		
		final HttpServletRequest httpReq = (HttpServletRequest)request;
		final HttpServletResponse httpResp = (HttpServletResponse)response;
		
		if(!allow(httpReq)) {
			
			String authHeader = httpReq.getHeader("authorization");
			
			if( authHeader == null || !authHeader.startsWith("Bearer ") ) {
				httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResp.getWriter().write("body: user not authenticated");
				return;
			}
				
			try {
				String jwtToken = authHeader.substring("Bearer ".length());
				DefaultClaims payload = parseJwtToken(jwtToken);
				httpReq.setAttribute("loginEmail", payload.getSubject());
			} catch(Exception ex) {
				httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResp.getWriter().write("body: user not authenticated");
				return;
			}
			
		}
		
		chain.doFilter(httpReq, httpResp);
		
	}
	
	private DefaultClaims parseJwtToken(String jwtToken) {
		try {
			return (DefaultClaims)Jwts.parser()
					.verifyWith(jwtKey)
					.build()
					.parse(jwtToken)
					.getPayload();
		} catch(ExpiredJwtException ex) {
			System.out.println("Authorization exception: JWT expired");
			throw ex;
		} catch(SignatureException ex) {
			System.out.println("Authorization exception: JWT signature verification failed");
			throw ex;
		} catch(Exception ex) {
			System.out.println("Authorization exception: " + ex.getMessage());
			throw ex;
		}
	}
	
	private boolean allow(HttpServletRequest httpReq) {
		if("OPTIONS".equals(httpReq.getMethod())) {
			return true;
		}
		String requestURI = httpReq.getRequestURI();
		if("/login".equals(requestURI) || "/register".equals(requestURI) ) {
			return true;
		}
		return false;
	}
	
	
	
}
