package BackendMashupExercise.MusicAPI.dto.musicbrainz;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Artist {
    private String type;
    private List<Relation> relations;
    private String name;
    @JsonProperty("release-groups")
    private List<MBAlbum> releasegroups;

	public List<MBAlbum> getReleasegroups() {
		return releasegroups;
	}

	public void setReleasegroups(List<MBAlbum> releasegroups) {
		this.releasegroups = releasegroups;
	}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    @Override
    public String toString() {
        return "Artist{" +
                "name='" + name + '\'' +
                ", relations=" + relations.size() +
                '}';
    }
}
