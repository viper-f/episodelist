package episodelist;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        List<Episode> episodes = null;
        try {
            episodes = this.FindEpisodes(data.fandom_id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.putInS3(data.fandom_id, episodes);
        return new Response("Success", 200);
    }

//    private List<Episode> getEpisodes(Integer fandom_id) {
//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
//        DynamoDB dynamoDB = new DynamoDB(client);
//
//        Table table = dynamoDB.getTable("EpisodeTable");
//
//        QuerySpec spec = new QuerySpec()
//                .withFilterExpression("fandom_id = :v_id")
//                .withValueMap(new ValueMap()
//                        .withInt(":v_id", fandom_id));
//
//        List<Episode> episodes = new ArrayList<>();
//        ItemCollection<QueryOutcome> items = table.query(spec);
//        Iterator<Item> iterator = items.iterator();
//        Boolean t = iterator.hasNext();
//        Item item = null;
//        while (iterator.hasNext()) {
//            item = iterator.next();
//            episodes.add(new Episode(item));
//        }
//
//        return episodes;
//    }

    private List<Episode> FindEpisodes(Integer value) throws Exception {

        List<Episode> scanResult = new ArrayList<>();
        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
            DynamoDBMapper mapper = new DynamoDBMapper(client);
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":val1", new AttributeValue().withN(value.toString()));

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("fandom_id = :val1").withExpressionAttributeValues(eav);

            scanResult = mapper.scan(Episode.class, scanExpression);

            return scanResult;

        } catch (Throwable t) {
            System.err.println("Error running the DynamoDBMapperQueryScanExample: " + t);
            t.printStackTrace();
        }
        return scanResult;
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
