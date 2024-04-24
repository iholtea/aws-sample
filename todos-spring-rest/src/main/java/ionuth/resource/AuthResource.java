package ionuth.resource;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;

@RestController
public class AuthResource {
	
	record LoginRequest(String email, String password) {};
	record LoginResponse(String message, String jwt) {};
	
	@Autowired
	private SecretKey jwtKey;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		
		System.out.println("Login endpoint: received: " + loginRequest);
		if( "holteai@yahoo.com".equals(loginRequest.email) && "test".equals(loginRequest.password) ) {
			String jwt = createJWT(loginRequest);
			return ResponseEntity.ok(new LoginResponse("login successful",jwt));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
					.body(new LoginResponse("login failed", ""));
		}
	}
	
	private String createJWT(LoginRequest loginRequest) {
		Instant now = Instant.now();
		return Jwts.builder()
				.issuer("ionuth-todos")
				.subject(loginRequest.email)
				.claim("role", "regular-user")
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(1000 * 60 * 60 * 24)))
				.signWith(jwtKey)
				.compact();
	}

}
