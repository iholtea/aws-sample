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

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.dynamodb.TodoRepositoryDynamodb;
import ionuth.todos.repo.util.TodoDynamoMapper;
import ionuth.todos.service.SecurityService;
import ionuth.todos.service.TodoService;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class TodosLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	
	enum TodoAction {
		LIST_GET_ALL, LIST_GET_ONE, LIST_DELETE, LIST_ADD,
		ITEM_DELETE, ITEM_ADD, ITEM_UPDATE,
		NO_ACTION
	};
	
	// making it static will enable Java AWS Lambda runtime to keep it initialized between cold starts
	private static DynamoDbClient dynamoClient = null;
	
	private TodoService todoService;
	private LambdaLogger logger;
	
	void initAws(Context context) {
		
		logger = context.getLogger();
		if( dynamoClient == null ) {
			logger.log("Initializing DynamoDb client...");
			dynamoClient = DynamoDbClient.builder()
					.region(Region.US_EAST_1)
			        .build();
		}
		
	}
	
	void initTodoServices() {
		String dynamoTableName = System.getenv("TABLE_NAME");
		TodoDynamoMapper dynamoMapper = new TodoDynamoMapper();
		TodoRepository todoRepo = new TodoRepositoryDynamodb(dynamoClient, dynamoMapper, dynamoTableName);
		SecurityService securityService = new SecurityService();
		todoService = new TodoService(todoRepo, securityService);
	}
	
	APIGatewayProxyResponseEvent initResponse() {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		// enable CORS
		Map<String, String> responseHeaders = Map.of(
				"Access-Control-Allow-Origin", "*",
	            "Access-Control-Allow-Methods", "OPTIONS,GET,POST,PUT,DELETE");
		response.setHeaders(responseHeaders);
		return response;
	}
	
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		
		initAws(context);
		initTodoServices();
		APIGatewayProxyResponseEvent response = initResponse();
		
		/*
		logger.log("Request Http Method: " + request.getHttpMethod());
		logger.log("Request path: " + request.getPath());
		logger.log("Request path parameters: " + pathParameters);
		logger.log("Request resource: " + request.getResource());
		*/
		
		TodoAction action = computeAction(request, context); 
		
		switch (action) {
		case LIST_GET_ALL:
			response = getAllListsByUser(response);
			break;
		case LIST_ADD:
			response = addTodoList(response, request);
			break;
		case LIST_GET_ONE:
			response = getListById(response, request);
			break;
		case LIST_DELETE:
			response = deleteList(response, request);
			break;
		case ITEM_ADD:
			response = createItem(response, request);
			break;
		case ITEM_UPDATE:
			response = updateItem(response, request);
			break;
		case ITEM_DELETE:
			response = deleteItem(response, request);
			break;
		default:
			response.setStatusCode(HttpStatusCode.BAD_REQUEST);
			response.setBody( "'message': 'unknown request'");
			break;
		}
		
		return response;
		
	}
	
	APIGatewayProxyResponseEvent getAllListsByUser(APIGatewayProxyResponseEvent response) {
		
		var todoLists = todoService.getAllListsByUser();
		response.setStatusCode(HttpStatusCode.OK);
		response.setBody( toJson(todoLists) );
		return response;
	
	}
	
	APIGatewayProxyResponseEvent getListById(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
		
		String listUuid = request.getPathParameters().get("todoId");
		var todoList = todoService.getListById(listUuid);
		response.setStatusCode(HttpStatusCode.OK);
		response.setBody( toJson(todoList) );
		return response;
	
	}
	
	APIGatewayProxyResponseEvent deleteList(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
		
		String listUuid = request.getPathParameters().get("todoId");
		todoService.deleteList(listUuid);
		response.setStatusCode(HttpStatusCode.OK);
		return response;
	
	}
	
	APIGatewayProxyResponseEvent addTodoList(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
		
		TodoList todoList = listFromJson(request.getBody());
		todoList = todoService.addTodoList(todoList);
		response.setStatusCode(HttpStatusCode.CREATED);
		response.setBody( toJson(todoList) );
		return response;
	
	}
	
	APIGatewayProxyResponseEvent deleteItem(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
		
		String listUuuid = request.getPathParameters().get("todoId");
		String itemUuid = request.getPathParameters().get("itemId");
		todoService.deleteItem(listUuuid, itemUuid);
		response.setStatusCode(HttpStatusCode.OK);
		return response;
	}
	
	APIGatewayProxyResponseEvent updateItem(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
	
		TodoItem item = itemFromJson( request.getBody() );
		item = todoService.updateItem(item);
		response.setStatusCode(HttpStatusCode.OK);
		response.setBody( toJson(item) );
		return response;
		
	}
	
	APIGatewayProxyResponseEvent createItem(APIGatewayProxyResponseEvent response,
			APIGatewayProxyRequestEvent request) {
		
		String listUuid = request.getPathParameters().get("todoId");
		TodoItem item = itemFromJson( request.getBody() );
		item.setListUuid(listUuid);
		item = todoService.createItem(item);
		response.setStatusCode(HttpStatusCode.CREATED);
		response.setBody( toJson(item) );
		return response;
		
	}
	
	String toJson(Object obj) {
		try {
			ObjectMapper objMapper = new ObjectMapper();
			ObjectWriter objWriter = objMapper.writerWithDefaultPrettyPrinter();
			return objWriter.writeValueAsString(obj);
		} catch(JsonProcessingException ex) {
			logger.log("Exception while building json response: " + ex.getMessage());
			return "";
		}
	}
	
	TodoList listFromJson(String jsonStr) {
		try {
			ObjectMapper objMapper = new ObjectMapper();
			return objMapper.readValue(jsonStr, TodoList.class);
		} catch(JsonProcessingException ex) {
			logger.log("Exception while building json response: " + ex.getMessage());
			return null;
		}
	}
	
	TodoItem itemFromJson(String jsonStr) {
		try {
			ObjectMapper objMapper = new ObjectMapper();
			return objMapper.readValue(jsonStr, TodoItem.class);
		} catch(JsonProcessingException ex) {
			logger.log("Exception while building json response: " + ex.getMessage());
			return null;
		}
	}
	
	TodoAction computeAction(APIGatewayProxyRequestEvent request, Context context) {
		
		Map<String, String> pathParameters = request.getPathParameters();
		String httpMethod = request.getHttpMethod().toUpperCase();
		
		if(pathParameters == null || pathParameters.isEmpty()) {
			
			switch (httpMethod) {
			case "GET":
				return TodoAction.LIST_GET_ALL;
			case "POST":
				return TodoAction.LIST_ADD;
			default:
				return TodoAction.NO_ACTION;
			}
			
		} else if( pathParameters.containsKey("todoId") && !pathParameters.containsKey("itemId")) {	
			
			switch (httpMethod) {
			case "GET":
				return TodoAction.LIST_GET_ONE;
			case "DELETE":
				return TodoAction.LIST_DELETE;
			case "POST":
				// path will be /todos/{todoId}/items. No conflict as there isn't any other POST just on /todos/{todoId}
				return TodoAction.ITEM_ADD;
			default:
				return TodoAction.NO_ACTION;
			}
			
		} else if( pathParameters.containsKey("todoId") && pathParameters.containsKey("itemId")) {
			
			switch (httpMethod) {
			case "PUT":
				return TodoAction.ITEM_UPDATE;
			case "DELETE":
				return TodoAction.ITEM_DELETE;
			default:
				return TodoAction.NO_ACTION;
			}
		}
		
		return TodoAction.NO_ACTION;
	}

}
