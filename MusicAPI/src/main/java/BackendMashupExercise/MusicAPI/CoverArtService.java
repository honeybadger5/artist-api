package BackendMashupExercise.MusicAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.async.DeferredResult;

import BackendMashupExercise.MusicAPI.dto.coverartarchive.CoverArt;

@Service
public class CoverArtService {
	
	private static final Logger logger = LoggerFactory.getLogger(CoverArtService.class);
	@Autowired
	private AsyncRestTemplate restTemplate;
	
	public CoverArtService(AsyncRestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

    public DeferredResult<CoverArt> getAlbumCoverArt(String id) {
		
    	DeferredResult<CoverArt> deferredResult = new DeferredResult<CoverArt>();
 
		ListenableFuture<ResponseEntity<CoverArt>> future = null;
		final String coverArtUrl = "http://coverartarchive.org/release-group/"+id;
		logger.info("Requesting coverArt, URL="+coverArtUrl);
		try {
			future = restTemplate.getForEntity(coverArtUrl, CoverArt.class);
		}
		catch (RestClientException e) {
			System.err.println(e.getMessage());
		}
		
		future.addCallback(new ListenableFutureCallback<ResponseEntity<CoverArt>>() {

	        @Override
	        public void onSuccess(ResponseEntity<CoverArt> result) {
	        	logger.debug("got success coverArt, URL="+coverArtUrl);
	            deferredResult.setResult(result.getBody());
	        }

	        @Override
	        public void onFailure(Throwable ex) {
	        	logger.debug("got error coverArt, URL="+coverArtUrl);
	        	deferredResult.setErrorResult(new CoverArt());
	        }
	    });

		return deferredResult;
	}
}
