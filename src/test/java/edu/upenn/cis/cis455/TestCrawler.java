package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageFactory;
import static edu.upenn.cis.cis455.crawler.utils.CrawlerHandler.*;

import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class TestCrawler extends TestCase {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

//    public void testCrawl(){
//
//        try {
//            Path tmp_path = Files.createTempDirectory("tmpDatabase");
//            Storage database = (Storage) StorageFactory.getDatabaseInstance(tmp_path.toString());
//            String[] args = {
//                    "https://crawltest.cis.upenn.edu/",
//                    tmp_path.toString(),
//                    "1",
//                    "1"
//            };
//            Crawler.main(args);
//            assertEquals(1, database.getCorpusSize());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void testGetRobot() {
        String url = "https://crawltest.cis.upenn.edu/";
        URLInfo urlInfo = new URLInfo(url);
        HashMap<String, Long> checkPoints = new HashMap<>();
        HashMap<String, ArrayList<String>> disallow = new HashMap<>();
        HashMap<String, Integer> crawlDelay = new HashMap<>();
        parseRobot(urlInfo, checkPoints, disallow, crawlDelay);
        ArrayList<String> expectedDisallow = new ArrayList<>();
        expectedDisallow.add("/marie/private/");
        expectedDisallow.add("/foo/");
        assertEquals(expectedDisallow, disallow.get(urlInfo.getHostName()));
        assertEquals(Optional.of(5), Optional.of(crawlDelay.get(urlInfo.getHostName())));
    }

    public void testGetUrlContentNotExist() throws IOException {
        Path tmp_path = Files.createTempDirectory("tmpDatabase");
        Storage database = (Storage) StorageFactory.getDatabaseInstance(tmp_path.toString());
        String url = "https://crawltest.cis.upenn.edu/";
        String body = getUrlContent("https://crawltest.cis.upenn.edu/notexist");
        assertNull(body);
    }
}
