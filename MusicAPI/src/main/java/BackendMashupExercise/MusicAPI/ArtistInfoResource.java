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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import BackendMashupExercise.MusicAPI.dto.aggregatedresponse.Album;
import BackendMashupExercise.MusicAPI.dto.aggregatedresponse.ArtistInfo;
import BackendMashupExercise.MusicAPI.dto.coverartarchive.CoverArt;
import BackendMashupExercise.MusicAPI.dto.coverartarchive.Image;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.MBAlbum;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.Relation;
import BackendMashupExercise.MusicAPI.dto.musicbrainz.Artist;

@RestController
public class ArtistInfoResource {

	private static final Logger logger = LoggerFactory.getLogger(ArtistInfoResource.class);
	//TODO safety check on incoming id?
	//TODO document my API. It should handle get params that must be described.
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody ArtistInfo queryApi(@RequestParam("data") String mbid) {
		
		long start = System.currentTimeMillis();
		logger.info("Incoming GET req, mbid: "+mbid);
		
		MusicBrainzService musicBrainzService = new MusicBrainzService(new RestTemplate());
		Artist artistInfo = musicBrainzService.getArtistInfo(mbid);
	
		//Get artist description
		List<Relation> relations = artistInfo.getRelations();
		//TODO config resource fix
		Predicate<Relation> predicate = r-> r.getType().equals("wikipedia");
		Relation relation = relations.stream().filter(predicate).findFirst().get();
		WikipediaService wikipediaService = new WikipediaService(new AsyncRestTemplate());
		DeferredResult<String> description = null;
		if(relation != null) {
			description = wikipediaService.getArtistDescription(relation.getUrl().getResource());
		}

		//Get cover art for all albums.
		CoverArtService coverArtService = new CoverArtService(new AsyncRestTemplate());
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
		
		return artistInfoResponse;
	}
}
