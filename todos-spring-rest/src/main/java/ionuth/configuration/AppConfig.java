package ionuth.configuration;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.dynamodb.TodoRepositoryDynamodb;
import ionuth.todos.repo.util.TodoDynamoMapper;
import ionuth.todos.service.TodoService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AppConfig {
	
	// enable CORS API calls
	/*
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				//registry.addMapping("/greeting-javaconfig").allowedOrigins("http://localhost:9000");
				System.out.println("WebMvcConfigurer implementation: enable CORS");
				registry.addMapping("/**");
			}
		};
	}
	*/
	
	
	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
				.region(Region.US_EAST_1)
		        .build();
	}
	
	@Bean
	public TodoDynamoMapper todoDynamoMapper() {
		return new TodoDynamoMapper();
	}
	
	public String getDynamoDbTableName() {
		return "Manual-Todos";
	}
	
	@Bean
	public TodoRepository todoRepository() {
		return new TodoRepositoryDynamodb(dynamoDbClient(), todoDynamoMapper(), getDynamoDbTableName());
	}
	
	@Bean
	public TodoService todoService() {
		return new TodoService(todoRepository());
	}
	
	@Bean
	public SecretKey keyJWT() {
		// this app is used only for test on localhost
		final String secret = "secret123secret456secret789secretABCsecretXYZ";
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}
}
