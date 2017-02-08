package BackendMashupExercise.MusicAPI;

import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.async.DeferredResult;

import BackendMashupExercise.MusicAPI.dto.wikipedia.WikiData;

@Service
public class WikipediaService {
	
	private static final Logger logger = LoggerFactory.getLogger(WikipediaService.class);
	private String wikiUrlBase = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles=";
	AsyncRestTemplate restTemplate;
	
	public WikipediaService(AsyncRestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public DeferredResult<String> getArtistDescription(String wikipediaResourceUrl) {

		String wikiName = "";
		try
		{
			URI uri = new URI(wikipediaResourceUrl);
			String path = uri.getPath();
			wikiName = path.substring(path.lastIndexOf('/') + 1);
		}
		catch(URISyntaxException e)
		{
			System.err.println("URISyntaxException: " + e.getMessage());
		}

		DeferredResult<String> deferredResult = new DeferredResult<String>();
		ListenableFuture<ResponseEntity<WikiData>> future = null;
		logger.debug("Requesting artist description, URL="+wikiUrlBase+wikiName);
		try {
			future = restTemplate.getForEntity(wikiUrlBase+wikiName, WikiData.class);
		}
		catch (RestClientException e) {
			System.err.println(e.getMessage());
		}
		
		future.addCallback(new ListenableFutureCallback<ResponseEntity<WikiData>>() {

	        @Override
	        public void onSuccess(ResponseEntity<WikiData> result) {
	        	String description = result.getBody().getQuery().getPages().get(result.getBody().getQuery().getPages().keySet().stream().findFirst().get()).getExtract();
	        	logger.debug("Requesting artist description SUCCESS: "+description);
	            deferredResult.setResult(description);
	        }

	        @Override
	        public void onFailure(Throwable ex) {
	        	logger.debug("Requesting artist description FAILURE: "+ex.getMessage());
	        	deferredResult.setErrorResult(new String());
	        }
	    });

		return deferredResult;
	}
}
