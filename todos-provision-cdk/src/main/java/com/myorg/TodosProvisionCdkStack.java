package com.myorg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.ApiKey;
import software.amazon.awscdk.services.apigateway.ApiKeyProps;
import software.amazon.awscdk.services.apigateway.IApiKey;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.IntegrationResponse;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LambdaIntegrationOptions;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.MethodResponse;
import software.amazon.awscdk.services.apigateway.MockIntegration;
import software.amazon.awscdk.services.apigateway.PassthroughBehavior;
import software.amazon.awscdk.services.apigateway.Period;
import software.amazon.awscdk.services.apigateway.QuotaSettings;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.apigateway.ThrottleSettings;
import software.amazon.awscdk.services.apigateway.UsagePlan;
import software.amazon.awscdk.services.apigateway.UsagePlanPerApiStage;
import software.amazon.awscdk.services.apigateway.UsagePlanProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.constructs.Construct;

public class TodosProvisionCdkStack extends Stack {
    
	private static final String BUCKET_WEBSITE_NAME = "cdk-todo-site-iholtea";
	private static final String DYNAMODB_TABLE_NAME = "Cdk-Todos";
	
	public TodosProvisionCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public TodosProvisionCdkStack(final Construct scope, final String id, final StackProps props) {
        
    	super(scope, id, props);
    	
    	createWebsiteBucket();
    	Function todoFunction = createLambda();
    	createDynamoDbTable(todoFunction);
    	createApiGateway(todoFunction);
    	
    	
    }
    
    private Bucket createWebsiteBucket() {
    	
    	// check https://docs.aws.amazon.com/cdk/api/v2/java/software/amazon/awscdk/services/s3/package-summary.html
    	// after an update to how access is granted to S3 buckets
    	// we need to disable also the public access using a BlockPublicAccess object
    	// just calling publicReadAccess(true) is not enough
    	// it will result in conflicting policy and cdk deploy will throw an error
    	BlockPublicAccess bpa = BlockPublicAccess.Builder.create()
    			.blockPublicPolicy(false)
    			.blockPublicAcls(false)
    			.restrictPublicBuckets(false)
    			.ignorePublicAcls(false)
    			.build();
    	
    	// calling websiteIndexDocument() also enables static website hosting
    	BucketProps bucketProps = BucketProps.builder()
    			.bucketName(BUCKET_WEBSITE_NAME)
    			.blockPublicAccess(bpa)
    			.publicReadAccess(true)
    			.websiteIndexDocument("index.html")
    			.removalPolicy(RemovalPolicy.DESTROY)
    			.autoDeleteObjects(true)
    			.build();
    	
    	return new Bucket(this, "CdkTodoBucketId", bucketProps);
    }
    
    private Table createDynamoDbTable(Function todoFunction) {
    	
    	Attribute pk = Attribute.builder().name("PK")
    			.type(AttributeType.STRING).build();
    	Attribute sk = Attribute.builder().name("SK")
    			.type(AttributeType.STRING).build();
    	
    	TableProps tableProps = TableProps.builder()
    			.tableName(DYNAMODB_TABLE_NAME)
    			.partitionKey(pk)
    			.sortKey(sk)
    			.removalPolicy(RemovalPolicy.DESTROY)
    			.build();
    	
    	Table dynamoTable = new Table(this, "CdkTodoDynamoId", tableProps);
    	dynamoTable.grantReadWriteData(todoFunction);
    	
    	return dynamoTable;
    }
    
    private Function createLambda() {
    	
    	FunctionProps functionProps = FunctionProps.builder()
    			.functionName("CdkTodoLambda")
    			.runtime(Runtime.JAVA_17)
    			.code(Code.fromAsset("/Users/tzucu/git/aws-sample/todos-lambda/build/distributions/todos-lambda.zip"))
    			.handler("ionuth.todos.lambda.TodosLambda::handleRequest")
    			.timeout(Duration.seconds(15))
    			.memorySize(512)
    			.environment( Map.of("TABLE_NAME", DYNAMODB_TABLE_NAME) )
    			.build();
    	
    	Function todoLambda = new Function(this, "CdkTodoLambdaId", functionProps);
    	todoLambda.applyRemovalPolicy(RemovalPolicy.DESTROY);
    	
    	return todoLambda;
    			
    }
    
    private RestApi createApiGateway(Function todoFunction) {
    	
    	
    	StageOptions testStageOptions = StageOptions.builder()
    			.stageName("test")
    			.build();
    	
    	RestApiProps apiProps = RestApiProps.builder()
    			.restApiName("cdk-todo")
    			.deployOptions(testStageOptions)
    			.build();
    	
    	RestApi api = new RestApi(this, "CdkTodoRestApiId", apiProps);
    	api.applyRemovalPolicy(RemovalPolicy.DESTROY);
    	
    	addThrottlingUsagePlan(api);
    	
    	LambdaIntegrationOptions integrationOpts = LambdaIntegrationOptions.builder()
    			.proxy(true)
    			.build();
    	Integration lambdaIntegration = new LambdaIntegration(todoFunction, integrationOpts);
    	
    	MethodOptions methodOptions = MethodOptions.builder()
    			.apiKeyRequired(true).build();
    	
    	IResource todosResource = api.getRoot().addResource("todos");
    	todosResource.addMethod("GET", lambdaIntegration, methodOptions);
    	todosResource.addMethod("POST", lambdaIntegration, methodOptions);
    	addCorsOptions(todosResource);
    	
    	IResource todoIdResource = todosResource.addResource("{todoId}");
    	todoIdResource.addMethod("GET", lambdaIntegration, methodOptions);
    	todoIdResource.addMethod("DELETE", lambdaIntegration, methodOptions);
    	addCorsOptions(todoIdResource);
    	
    	IResource itemsResource = todoIdResource.addResource("items");
    	itemsResource.addMethod("POST", lambdaIntegration, methodOptions);
    	addCorsOptions(itemsResource);
    	
    	IResource itemIdResource = itemsResource.addResource("{itemId}");
    	itemIdResource.addMethod("DELETE", lambdaIntegration, methodOptions);
    	itemIdResource.addMethod("PUT", lambdaIntegration, methodOptions);
    	addCorsOptions(itemIdResource);
    	
    	return api;
    	
    }
    
    private void addThrottlingUsagePlan(RestApi api) {
    	
    	ApiKeyProps apiKeyProps = ApiKeyProps.builder()
    			.apiKeyName("cdk-todo-api-key").build();
    	IApiKey apiKey = new ApiKey(this, "CdkTodoApiKeyId", apiKeyProps);
    	apiKey.applyRemovalPolicy(RemovalPolicy.DESTROY);
    	
    	ThrottleSettings throttleSetting = ThrottleSettings.builder()
    			.rateLimit(5).burstLimit(5).build();
    	QuotaSettings quotaSettins = QuotaSettings.builder()
    			.limit(200).period(Period.DAY).build();
    	UsagePlanProps usagePlanProps = UsagePlanProps.builder()
    			.name("cdk-todo-usage-plan")
    			.throttle(throttleSetting)
    			.quota(quotaSettins)
    			.build();
    	UsagePlan usagePlan = new UsagePlan(this, "CdkTodoUsagePlan", usagePlanProps);
    	usagePlan.applyRemovalPolicy(RemovalPolicy.DESTROY);
    	usagePlan.addApiKey(apiKey);
    	usagePlan.addApiStage( UsagePlanPerApiStage.builder()
    			.stage(api.getDeploymentStage()).build());
    	
    }
    
    private void addCorsOptions(IResource resource) {
    	
    	Map<String, Boolean> responseParams = new HashMap<>();
    	responseParams.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
    	responseParams.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
    	responseParams.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
    	responseParams.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);
    	
    	List<MethodResponse> methodResponses = new ArrayList<>();
    	methodResponses.add(MethodResponse.builder()
    			.responseParameters(responseParams)
    			.statusCode("200")
    			.build());
    	
    	MethodOptions methodOptions= MethodOptions.builder()
    			.methodResponses(methodResponses).build();
    	
    	
    	Map<String, String> integrationRespParams = new HashMap<>();
    	integrationRespParams.put("method.response.header.Access-Control-Allow-Headers",
    			"'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
    	integrationRespParams.put("method.response.header.Access-Control-Allow-Origin",
    			"'*'");
    	integrationRespParams.put("method.response.header.Access-Control-Allow-Credentials",
    			"'true'");
    	integrationRespParams.put("method.response.header.Access-Control-Allow-Methods",
    			"'OPTIONS,GET,PUT,POST,DELETE'");
        
    	List<IntegrationResponse> integrationResps = new ArrayList<>();
    	integrationResps.add(IntegrationResponse.builder()
    			.responseParameters(integrationRespParams)
    			.statusCode("200")
    			.build());
    			
    	Integration methodIntegration = MockIntegration.Builder.create()
    			.integrationResponses(integrationResps)
    			.requestTemplates(Map.of(
    					"application/json","{\"statusCode\": 200}" ))
    			.passthroughBehavior(PassthroughBehavior.NEVER)
    			.build();
    	
    	resource.addMethod("OPTIONS", methodIntegration, methodOptions);
    	
    }
}
