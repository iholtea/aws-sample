package ionuth.configuraiton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.dynamodb.TodoRepositoryDynamodb;
import ionuth.todos.repo.util.TodoDynamoMapper;
import ionuth.todos.service.SecurityService;
import ionuth.todos.service.TodoService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AppConfig {
	
	// enable CORS API calls
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
	
	@Bean
	public SecurityService securityService() {
		return new SecurityService();
	}
	
	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
				.region(Region.US_EAST_1)
		        .build();
	}
	
	public TodoDynamoMapper todoDynamoMapper() {
		return new TodoDynamoMapper();
	}
	
	@Bean
	public TodoRepository todoRepository() {
		return new TodoRepositoryDynamodb(dynamoDbClient(), todoDynamoMapper());
	}
	
	@Bean
	public TodoService todoService() {
		return new TodoService(todoRepository(), securityService());
	}
}
