package episodelist;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;


import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;


public class AddEpisode implements RequestHandler<Map, Response> {
    Gson gson = new Gson();

    public Response handleRequest(Map event, Context context) {
        Episode episode = this.gson.fromJson((String)event.get("body"), Episode.class);
        this.putItemInTable(episode);
        return new Response("Success", 200);
    }

    public void putItemInTable(Episode episode) {

        List<Object> t = episode.characters;

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("EpisodeTable");
        Item item = new Item();
        item.withString("title", episode.title);
        item.withInt("fandom_id", episode.fandom_id);
        item.withList("characters", episode.characters);
        item.withString("description", episode.description);
        item.withString("date_created", episode.date_created);
        table.putItem(item);
    }
}
