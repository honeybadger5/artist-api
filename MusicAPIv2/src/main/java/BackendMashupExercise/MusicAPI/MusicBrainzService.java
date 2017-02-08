package BackendMashupExercise.MusicAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import BackendMashupExercise.MusicAPI.dto.musicbrainz.Artist;

@Service
public class MusicBrainzService {

	private static final Logger logger = LoggerFactory.getLogger(MusicBrainzService.class);
	private RestTemplate restTemplate;
	
	public MusicBrainzService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public Artist getArtistInfo(String mbid) {

		final String musicBrainzUrl = "http://musicbrainz.org/ws/2/artist/"+mbid+"?&fmt=json&inc=url-rels+release-groups";
		logger.info("Requesting artist info, URL="+musicBrainzUrl);
		Artist artist = null;
		try{
			artist = restTemplate.getForObject(musicBrainzUrl, Artist.class);
		} catch (HttpClientErrorException e) {
			logger.error(e.getMessage());
			throw e;
		}
		return artist;
	}
}
