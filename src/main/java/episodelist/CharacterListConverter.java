package episodelist;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.google.gson.Gson;

import java.util.List;

public class CharacterListConverter implements DynamoDBTypeConverter<String, List<Object>>
{
    Gson gson = new Gson();

    @Override
    public String convert(final List<Object> characters) {
        return gson.toJson(characters);
    }

    @Override
    public List<Object> unconvert(final String string) {
        Integer t =1;
        return gson.fromJson(string, List.class);
    }
}
