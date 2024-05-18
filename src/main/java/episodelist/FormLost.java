package episodelist;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import java.util.Map;


public class FormLost implements RequestHandler<Map, Response> {
    Gson gson = new Gson();

    public Response handleRequest(Map event, Context context) {
        Episode episode = this.gson.fromJson((String)event.get("body"), Episode.class);
    //    return new Response(episode.title, 200);



        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = "EpisodeTable";
        Table table = dynamoDB.getTable(tableName);

        String primaryKeyName = "id";
        int primaryKeyValue = 123;
        PrimaryKey primaryKey = new PrimaryKey(primaryKeyName, primaryKeyValue);

        Item item = table.getItem(primaryKey);

        // Use the item as neededString attributeName = "name";
        String name = item.getString("title");
        System.out.println("Retrieved item: " + name);
        return new Response(episode.title, 200);
    }

    public void putItemInTable(Episode episode) {


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
