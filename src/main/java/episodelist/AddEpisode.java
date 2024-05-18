package episodelist;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.google.gson.Gson;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

import java.util.Map;

public class AddEpisode implements RequestHandler<Map, Response> {
    Gson gson = new Gson();

    public Response handleRequest(Map event, Context context) {
        LambdaLogger logger = context.getLogger();
        Episode episode = this.gson.fromJson((String)event.get("body"), Episode.class);
        this.putItemInTable(episode, logger);
        this.invokeRebuild(episode.fandom_id);
        return new Response("Success", 200);
    }

    private void putItemInTable(Episode episode, LambdaLogger logger) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("EpisodeTable");
        Item item = new Item();
        String key = episode.fandom_id.toString()+'-'+episode.episode_id.toString();
        item.withPrimaryKey("Id", key);
        item.withString("title", episode.title);
        item.withInt("episode_id", episode.episode_id);
        item.withInt("fandom_id", episode.fandom_id);
        item.withList("characters", episode.characters);
        item.withString("description", episode.description);
        item.withString("date_created", episode.date_created);
        table.putItem(item);
    }

    private void invokeRebuild(Integer fandom_id) {
        AWSLambda client = AWSLambdaClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        InvokeRequest req = new InvokeRequest()
                .withFunctionName("BuildList")
                .withInvocationType(InvocationType.Event)
                .withPayload(gson.toJson(new RebuildTrigger(fandom_id)));
        client.invoke(req);
    }
}
