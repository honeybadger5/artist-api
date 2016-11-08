package BackendMashupExercise.MusicAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.async.DeferredResult;

import BackendMashupExercise.MusicAPI.dto.aggregatedresponse.Album;
import BackendMashupExercise.MusicAPI.dto.aggregatedresponse.ArtistInfo;
import BackendMashupExercise.MusicAPI.dto.coverartarchive.CoverArt;
import BackendMashupExercise.MusicAPI.dto.coverartarchive.Image;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.MBAlbum;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.Relation;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.Artist;


@RestController("/mashup-api")
public class ArtistInfoResource {

	private static final Logger logger = LoggerFactory.getLogger(ArtistInfoResource.class);
	
	@Autowired
	private MusicBrainzService musicBrainzService;

	@Autowired
	private WikipediaService wikipediaService;
	
	@Autowired
	private CoverArtService coverArtService;
	
	/*
	 * TODO:
	 * Severe stuff:
	 * 1. Obviously the queryApi method is way too large and needs to be broken down
	 * into several methods with designated function
	 * 2. The API cannot yet handle rate limiting on the underlying services - the only
	 * limitation so far is that asynchronous CoverArt requests from a single request on the api
	 * is sent with 1 s intervals. But if there are several requests on the mashup api at once, 
	 * there will be several requests per second to the musicbrainz api, which could cause denied service.
	 * This could be handled e.g. by having a thread-safe singleton request queue shared by the different request threads.
	 * 3. More tests need to be implemented, e.g. CoverArtService lacks tests
	 * 4. Find all places where error checks are needed and implement them. E.g. missed null pointers, more handling of
	 *   bad requests
	 * 
	 * - Other stuff:
	 * - Instead of using AsyncRestTemplate inside services, look at using @Async to thread whole service instead
	 * - Edit User-agent in http header (required by underlying web services)
	 * - The queryApi method should have its own part of the url, e.g. /mashup-api/<get-artist-info>/?mbid=... in case
	 *   more requestMapping methods needs to be be added later
	 * - Configuration for the logger, set trace levels
	 * - The data classes should have toString methods
	 * - Document the api
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	ResponseEntity<ArtistInfo> queryApi(@RequestParam("mbid") String mbid) {
		
		long start = System.currentTimeMillis();
		logger.info("Incoming GET req, mbid: "+mbid);
		
		if (mbid==null || mbid.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		Artist artistInfo = null;
		
		try {
			artistInfo = musicBrainzService.getArtistInfo(mbid); 
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		}

		//Get artist description
		List<Relation> relations = artistInfo.getRelations();
		Predicate<Relation> predicate = r-> r.getType().equals("wikipedia");
		Relation relation = relations.stream().filter(predicate).findFirst().get();
		DeferredResult<String> description = null;
		if(relation != null) {
			description = wikipediaService.getArtistDescription(relation.getUrl().getResource());
		}

		//Get cover art for all albums.
		Map<String, DeferredResult<CoverArt>> coverArtMap = new HashMap<String, DeferredResult<CoverArt>>();
		Iterator<MBAlbum> albumIterator = artistInfo.getReleasegroups().iterator();
		
		while (albumIterator.hasNext()) {
			String id = albumIterator.next().getId();
			DeferredResult<CoverArt> coverArt = coverArtService.getAlbumCoverArt(id);
			if(coverArt !=null ) {
				coverArtMap.put(id, coverArt);
			}
			//Musicbrainz service only allows 1 req/sec
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}

		//Aggregate all info into POJO for json export		
		//Set mbid
		ArtistInfo artistInfoResponse= new ArtistInfo();
		artistInfoResponse.setMbid(mbid);

		//wait for the future to come
		boolean futureHasArrived = false;
		while(!futureHasArrived) {
			futureHasArrived =true;

			//Check if description future is set
			if(description != null && !description.hasResult()) {
				futureHasArrived = false;
			}

			//Check if CoverArt future is set
			if(futureHasArrived) {
				Iterator<Entry<String, DeferredResult<CoverArt>>> it = coverArtMap.entrySet().iterator();
				while (it.hasNext()) {
					if(!((DeferredResult<CoverArt>)(it.next().getValue())).hasResult()) {
						futureHasArrived = false;
					}
				}
			}
		}

		//Set artist description
		if(description != null) {
			artistInfoResponse.setDescription((String) description.getResult());
		}
		else {
			artistInfoResponse.setDescription("");
		}


		//Write album info to response
		List<Album> albumResponseList = new ArrayList<Album>();
		Iterator<MBAlbum> albumIter = artistInfo.getReleasegroups().iterator();
		while (albumIter.hasNext()) {
			MBAlbum mbAlbum = albumIter.next();
			Album responseAlbum = new Album();
			responseAlbum.setId(mbAlbum.getId());
			responseAlbum.setTitle(mbAlbum.getTitle());
			List<String> images = new ArrayList<String>();
			//Add coverArt links to album info
			Iterator<Image> imageIter = null;
			List<Image> mbImages = ((CoverArt) (coverArtMap.get(mbAlbum.getId()).getResult())).getImages();
			if(mbImages!=null) {
				imageIter = mbImages.iterator();
			}
			while (imageIter!=null && imageIter.hasNext()) {
				images.add(imageIter.next().getImage());
			}
	
			responseAlbum.setImages(images);
			albumResponseList.add(responseAlbum);

		}
		artistInfoResponse.setAlbums(albumResponseList);
		
		//Print time to process request
		logger.info("Elapsed time: " + (System.currentTimeMillis() - start)/1000 + " s");
		
		return new ResponseEntity<ArtistInfo>(artistInfoResponse,HttpStatus.OK);
	}
}
