package BackendMashupExercise.MusicAPI.dto.aggregatedresponse;

import java.util.List;

public class Album {

	private String title;
	private String id;
	private List<String> images;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}
}