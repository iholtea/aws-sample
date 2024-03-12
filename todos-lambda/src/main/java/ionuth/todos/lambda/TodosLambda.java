package ionuth.todos.lambda;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.dynamodb.TodoRepositoryDynamodb;

public class TodosLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	
	private static final String USER_EMAIL = "holteai@yahoo.com";
	
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
		TodoRepository todoRepo = new TodoRepositoryDynamodb();
		
		if( pathParameters!=null && pathParameters.containsKey("todoId") ) {
			returnObj = todoRepo.getListById(pathParameters.get("todoId"), USER_EMAIL);
		} else {
			returnObj = todoRepo.getListsByUserEmail(USER_EMAIL);
		}
		
		
		String jsonStr = "";
		try {
			jsonStr = objWriter.writeValueAsString(returnObj);
		} catch(JsonProcessingException ex) {
			logger.log("Exception while building json response: " + ex.getMessage());
		}
		
		
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		// enable CORS
		Map<String, String> responseHeaders = Map.of(
				"Access-Control-Allow-Origin", "*",
	            "Access-Control-Allow-Methods", "OPTIONS,GET,POST,PUT,DELETE");
		response.setHeaders(responseHeaders);
				
		response.setStatusCode(200);
		response.setBody(jsonStr);
		
		return response;
		
		
	}

}
