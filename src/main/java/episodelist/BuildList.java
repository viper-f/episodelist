package episodelist;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


public class BuildList implements RequestHandler<Map, Response> {
    Gson gson = new Gson();

    public Response handleRequest(Map event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString());
        RebuildTrigger data = this.gson.fromJson(event.toString(), RebuildTrigger.class);
        List<Episode> episodes = this.getEpisodes(data.fandom_id);
        this.putInS3(data.fandom_id, episodes);
        return new Response("Success", 200);
    }

    private List<Episode> getEpisodes(Integer fandom_id) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("EpisodeTable");

        QuerySpec spec = new QuerySpec()
                .withFilterExpression("fandom_id = :v_id")
                .withValueMap(new ValueMap()
                        .withInt(":v_id", fandom_id));

        ItemCollection<QueryOutcome> query = table.query(spec);
        List<Episode> episodes = new ArrayList<>();
        for (Item q: query) {
            episodes.add(new Episode(q));
        }
        return episodes;
    }

    private void putInS3(Integer fandom_id, List<Episode> episodes) {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        String text = gson.toJson(episodes);
        byte[] contentAsBytes = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
        ObjectMetadata md = new ObjectMetadata();
        md.setContentLength(contentAsBytes.length);
        try {
            s3.putObject("episodelist-source", "episodelist-"+fandom_id.toString()+".json", contentsAsStream, md);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }

}
