package episodelist;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import java.util.Map;


public class AddEpisode implements RequestHandler<Map, Response> {
    public AddEpisode() {
    }

    public Response handleRequest(Map event, Context context) {
        Gson gson = new Gson();
        Episode episode = gson.fromJson((String)event.get("body"), Episode.class);
        return new Response(episode.title, 200);

    }
}
