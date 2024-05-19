package episodelist;

import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.cloudfront.model.Paths;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
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
        List<Episode> episodes = null;
        try {
            episodes = this.findEpisodes((Integer)event.get("fandom_id"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String filename = this.putInS3((Integer)event.get("fandom_id"), episodes);
        if (filename != null) {
            this.invalidateCache(filename);
        }
        return new Response("Success", 200);
    }

    private List<Episode> findEpisodes(Integer value) throws Exception {

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

    private String putInS3(Integer fandom_id, List<Episode> episodes) {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        String text = gson.toJson(episodes);
        byte[] contentAsBytes = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
        ObjectMetadata md = new ObjectMetadata();
        md.setContentLength(contentAsBytes.length);
        md.setContentType("application/json");
        try {
            String filename = "episodelist-"+fandom_id.toString()+".html";
            s3.putObject("episodelist-source", filename, contentsAsStream, md);
            return filename;
        } catch (AmazonServiceException e) {
            System.err.println("S3 error: " + e.getErrorMessage());
            System.exit(1);
        }
        return null;
    }

    private void invalidateCache(String file_name)
    {
        AmazonCloudFront client = AmazonCloudFrontClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        Paths paths = new Paths()
                .withItems("/"+file_name)
                .withQuantity(1);
        InvalidationBatch batch = new InvalidationBatch()
                .withPaths(paths)
                .withCallerReference(String.valueOf(System.currentTimeMillis()));
        CreateInvalidationRequest request = new CreateInvalidationRequest("E7AKHQ5SDO0BJ", batch);
        client.createInvalidation(request);
    }

}
