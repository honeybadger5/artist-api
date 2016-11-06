package BackendMashupExercise.MusicAPI.dto.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Url {
	private String resource;
	private String id;
	
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}   
	
	@Override
    public String toString() {
        return "Url{" +
                "resource=" + resource +
                ", id='" + id + '\'' +
                '}';
    }
	
}
