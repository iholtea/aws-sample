package ionuth.configuraiton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.file.local.TodoRepositoryFileLocal;

@Configuration
public class AppConfig {
	
	@Bean
	public TodoRepository todoRepository() {
		return new TodoRepositoryFileLocal();
	}
	
}
