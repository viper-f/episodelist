package episodelist;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
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
        Episode episode = this.gson.fromJson((String)event.get("body"), Episode.class);
        this.putItemInTable(episode);
        return new Response("Success", 200);
    }

    public void putItemInTable(Episode episode) {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("EpisodeTable");
        Item item = new Item();
        String key = episode.fandom_id.toString()+'-'+episode.episode_id.toString();
        item.withPrimaryKey("Id", key);
        item.withString("title", episode.title);
        item.withInt("fandom_id", episode.fandom_id);
        item.withList("characters", episode.characters);
        item.withString("description", episode.description);
        item.withString("date_created", episode.date_created);
        table.putItem(item);
    }
}
