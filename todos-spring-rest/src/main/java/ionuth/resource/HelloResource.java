package ionuth.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloResource {
	
	@GetMapping("/hello")
	public String sayHello() {
		System.out.println("HelloResource /hello endpoint called");
		return "hello worlds";
	}
	
}
