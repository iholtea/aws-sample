package ionuth.todos.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;

public class TodosLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		
		Map<String, String> pathParameters = request.getPathParameters();
		
		LambdaLogger logger = context.getLogger(); 
		logger.log("Request Http Method: " + request.getHttpMethod());
		logger.log("Request path: " + request.getPath());
		logger.log("Request path parameters: " + pathParameters);
		logger.log("Request resource: " + request.getResource());
		
		
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter objWriter = objMapper.writerWithDefaultPrettyPrinter();
		
		Object returnObj = null;
		
		if( pathParameters!=null && pathParameters.containsKey("todoId") ) {
			
			TodoItem item = new TodoItem();
			item.setUuid("uuid-" + pathParameters.get("todoId"));
			item.setText("Programare pasaport");
			item.setDone(false);
			returnObj = item;
			
		} else {
			
			List<TodoList> todos = new ArrayList<>();
			
			TodoList list1 = new TodoList();
			list1.setUuid("uuid-list-01");
			list1.setTitle("Norway Road Trip");
			list1.setUserEmail("holteai@yahoo.com");
			list1.setCreationDate("2024-01-25");
			list1.setLastUpdate("2024-01-25");
			todos.add(list1);
			
			list1.setUuid("uuid-list-02");
			list1.setTitle("Supermarket shopping");
			list1.setUserEmail("holteai@yahoo.com");
			list1.setCreationDate("2024-02-14");
			list1.setLastUpdate("2024-02-14");
			todos.add(list1);
			
			returnObj = todos;
			
		}
		
		
		String jsonStr = "";
		try {
			jsonStr = objWriter.writeValueAsString(returnObj);
		} catch(JsonProcessingException ex) {
			logger.log("Exception while building json response: " + ex.getMessage());
		}
		
		
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody(jsonStr);
		return response;
		
		
	}

}
