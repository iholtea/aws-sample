package ionuth.test.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

record IntegerRecord(int x, int y, String message) {}

/*
 * so a LambdaImplementation implements RequestHandler<TIN, TOUT>
 * TOUT handleRequest(TIN in, Context context)
 * 
 * Lambda will attempt to map the input event which is in JSON format
 * to class used as TIN - in this case IntegerRecord
 * 
 * Input Event example:
 * 
 * {
 *   "x": 1,
 *   "y": 20,
 *   "message": "Hello, World"  			
 * }
 * 
 * Mapping to JSON will be done for the return parameter also
 * ( if it is a string or a number then it return just the string representation )
 * 
 * Also we could use as input type Map<String,String>
 * this will map the input JSON event to this Map<String, String>
 * It will work only if we do not have nested objects in the input JSON event
 * 
 */
public class LambdaTest implements RequestHandler<IntegerRecord, Integer> {
	
	public Integer handleRequest(IntegerRecord event, Context context) {
		
		LambdaLogger logger = context.getLogger();
		logger.log("Input message sent: " + event.message());
		
		// simulate exception
		if( event.x()==5 && event.y()==5 ) {
			throw new ArithmeticException("cannot add 5 and 5 :P");
		}
		
		return event.x() + event.y();
	}
	

}
