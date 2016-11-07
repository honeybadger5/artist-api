package BackendMashupExercise.MusicAPI;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import BackendMashupExercise.MusicAPI.dto.musicbrainz.Artist;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Services.
 */
public class AppTest extends TestCase
{
	MockRestServiceServer server1;
	RestTemplate restTemplate;
	
	MockRestServiceServer server2;
	AsyncRestTemplate asyncRestTemplate;

	// assigning the values
	protected void setUp(){
        restTemplate = new RestTemplate();
		server1 = MockRestServiceServer.bindTo(restTemplate).build();
		
		asyncRestTemplate = new AsyncRestTemplate();
		server2 = MockRestServiceServer.bindTo(asyncRestTemplate).build();
	}
	
	protected void tearDown(){
		server1.reset();
		server2.reset();
	}
	   
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Test MusicBrainz service
     */
    public void testMusicBrainzService()
    {
    	String id = "test123";
    	
    	server1.expect(ExpectedCount.manyTimes(), 
    			MockRestRequestMatchers.requestTo("http://musicbrainz.org/ws/2/artist/"+id+"?fmt=json&inc=url-rels%2Brelease-groups"))
    	.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
    	.andRespond(MockRestResponseCreators.withSuccess("{ \"type\" : \"Group\", \"name\" : \"Placebo\", "
    			+ "\"release-groups\" : [{ \"title\" : \"Black Market Music\" , \"id\" : \"test456\" }] , "
    			+ "\"relations\" : [{ \"type\" : \"wiki\", \"url\" : {\"resource\": \"http://www.wiki.org/Placebo\"}}] }"
    			, MediaType.APPLICATION_JSON));

    	MusicBrainzService musicBrainzService = new MusicBrainzService(restTemplate);
    	Artist artistInfo = musicBrainzService.getArtistInfo(id);
    	
    	assertEquals(artistInfo.getName(), "Placebo");
    	assertEquals(artistInfo.getType(), "Group");
    	assertEquals("Black Market Music", artistInfo.getReleasegroups().get(0).getTitle());
    	assertEquals("test456", artistInfo.getReleasegroups().get(0).getId());
    	assertEquals("wiki", artistInfo.getRelations().get(0).getType());
    	assertEquals("http://www.wiki.org/Placebo", artistInfo.getRelations().get(0).getUrl().getResource());

    	// Verify all expectations met
    	server1.verify();
    }
    
    /**
     * Test WikipediaService service
     */
    public void testWikipediaService()
    {
    	String wikiName = "Ludwig_van_Beethoven";
    	
    	server2.expect(ExpectedCount.once(), 
    			MockRestRequestMatchers.requestTo("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles="+wikiName))
    	.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
    	.andRespond(MockRestResponseCreators.withSuccess("{ \"query\" : { \"pages\" : {\"21231\" : {"
    			+ "\"extract\" : \"Ludwig van Beethoven was a German composer and pianist.\" }}}}"
    			+ "", MediaType.APPLICATION_JSON));

    	WikipediaService wikipediaService = new WikipediaService(asyncRestTemplate);
    	DeferredResult<String> description = wikipediaService.getArtistDescription("https://en.wikipedia.org/"+wikiName);
    	
    	assertEquals("Ludwig van Beethoven was a German composer and pianist.", description.getResult());

    	// Verify all expectations met
    	server2.verify();
    }
}
