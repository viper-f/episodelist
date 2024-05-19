package episodelist;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.List;

@DynamoDBTable(tableName = "EpisodeTable")
public class Episode {
    @DynamoDBHashKey(attributeName="Id")
    private String Id;
    private Integer episode_id;
    private String title;
    private Integer fandom_id;
    private List<Object> characters;
    private String description;
    private String date_created;

    @DynamoDBHashKey(attributeName="Id")
    public String getId() { return this.Id; }
    public void setId(String id) { this.Id = id; }

    @DynamoDBAttribute(attributeName = "episode_id")
    public Integer getEpisodeId() { return this.episode_id; }
    public void setEpisodeId(Integer episode_id) { this.episode_id = episode_id; }

    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "fandom_id")
    public Integer getFandom_id() {
        return fandom_id;
    }

    public void setFandom_id(Integer fandom_id) {
        this.fandom_id = fandom_id;
    }

    @DynamoDBAttribute(attributeName = "characters")
    @DynamoDBTypeConverted(converter = CharacterListConverter.class)
    public List<Object> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Object> characters) {
        this.characters = characters;
    }

    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBAttribute(attributeName = "date_created")
    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }




//    public Episode(Item document)
//    {
//        this.Id = document.getString("Id");
//        this.episode_id = document.getInt("episode_id");
//        this.title = document.getString("title");
//        this.fandom_id = document.getInt("fandom_id");
//        this.characters = document.getList("characters");
//        this.description = document.getString("description");
//        this.date_created = document.getString("date_created");
//    }
}
