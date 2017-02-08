package BackendMashupExercise.MusicAPI.dto.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Relation {
    private Url url;
    private String type;

    public Relation() {
    }

    public Url getUrl() {
        return this.url;
    }

    public String getType() {
        return this.type;
    }

    public void setUrl(Url url) {
        this.url = url;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "url=" + url.toString() +
                ", type='" + type + '\'' +
                '}';
    }
}
