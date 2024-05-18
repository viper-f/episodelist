package episodelist;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class GetList implements RequestHandler<Object, Response> {
    public  GetList() {
    }

    public Response handleRequest(Object input, Context context) {
        return new Response("This function is working!", 200);
    }
}
