package episodelist;

import com.amazonaws.services.dynamodbv2.document.Item;

import java.util.List;

public class Episode {
    public Integer episode_id;
    public String title;
    public Integer fandom_id;
    public List<Object> characters;
    public String description;
    public String date_created;

    public Episode(Item document)
    {
        this.episode_id = document.getInt("episode_id");
        this.title = document.getString("title");
        this.fandom_id = document.getInt("fandom_id");
        this.characters = document.getList("characters");
        this.description = document.getString("description");
        this.date_created = document.getString("date_created");
    }
}
