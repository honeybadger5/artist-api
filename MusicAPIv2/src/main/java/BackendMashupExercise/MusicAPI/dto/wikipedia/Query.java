package BackendMashupExercise.MusicAPI.dto.wikipedia;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Query {

	private Map<Long,PageContent> pages;
	
	public  Map<Long,PageContent> getPages() {
		return pages;
	}

	public void setPages( Map<Long,PageContent> pages) {
		this.pages = pages;
	}
}
