package BackendMashupExercise.MusicAPI.dto.aggregatedresponse;

import java.util.List;


public class ArtistInfo {

	private String mbid;
	private String description;
	private List<Album> albums;
	
	public String getMbid() {
		return mbid;
	}
	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Album> getAlbums() {
		return albums;
	}
	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}
}
