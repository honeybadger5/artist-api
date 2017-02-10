package BackendMashupExercise.MusicAPI;

import BackendMashupExercise.MusicAPI.dto.aggregatedresponse.ArtistInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by malin on 2017-02-08.
 */
@RestController
public class RequestHandler {

    @Autowired
    private ArtistInfoResource artistInfoResource;

    @RequestMapping(value = "/mashup-api/get-artist-info", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<ArtistInfo> queryApi(@RequestParam("mbid") String mbid) {

        ResponseEntity<ArtistInfo> artistInfo = artistInfoResource.getArtistInfoResponse(mbid);
        return artistInfo;
    }

}
