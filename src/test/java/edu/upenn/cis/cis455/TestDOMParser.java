package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.crawler.DOMParseBolt;
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImp;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class TestDOMParser extends TestCase {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    public void testGetOccurEvents1(){
        String content = "<rss>\n" +
                "    <channel>\n" +
                "        <language>en-us</language>\n" +
                "    </channel>\n" +
                "</rss>";
        ArrayList<OccurrenceEvent> events = new ArrayList<>();
        DOMParseBolt.getOccurEvents(content, events);

        OccurrenceEvent[] expectedEvents = {
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "rss", 1),
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "channel", 2),
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "language",  3),
                new OccurrenceEvent(OccurrenceEvent.Type.Text, "language", 3, "en-us"),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "language", 3),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "channel", 2),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "rss", 1),
        };

        events.remove(0);
        events.remove(events.size() - 1);

        for (int i=0; i<events.size(); i++){
            assertEquals(expectedEvents[i], events.get(i));
        }

//        assertEquals(expectedEvents, events);
    }

    public void testGetOccurEvents2(){
        String content = "<rss>\n" +
                "    <channel>\n" +
                "        interesting\n" +
                "        <language>en-us</language>\n" +
                "        <title>my sports</title>\n" +
                "    </channel>\n" +
                "</rss>";
        ArrayList<OccurrenceEvent> events = new ArrayList<>();
        DOMParseBolt.getOccurEvents(content, events);

        OccurrenceEvent[] expectedEvents = {
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "rss", 1),
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "channel", 2),
                new OccurrenceEvent(OccurrenceEvent.Type.Text, "channel", 2, "interesting"),
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "language",  3),
                new OccurrenceEvent(OccurrenceEvent.Type.Text, "language", 3, "en-us"),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "language", 3),
                new OccurrenceEvent(OccurrenceEvent.Type.Open, "title", 3),
                new OccurrenceEvent(OccurrenceEvent.Type.Text, "title", 3, "my sports"),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "title", 3),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "channel", 2),
                new OccurrenceEvent(OccurrenceEvent.Type.Close, "rss", 1),
        };

        events.remove(0);
        events.remove(events.size() - 1);

        for (int i=0; i<events.size(); i++){
            assertEquals(expectedEvents[i], events.get(i));
        }

//        assertEquals(expectedEvents, events);
    }

    public void testParseAndMatch(){
        String content = "<rss>\n" +
                "    <channel>\n" +
                "        interesting\n" +
                "        <language>en-us</language>\n" +
                "        <title>my sports</title>\n" +
                "    </channel>\n" +
                "</rss>";
        ArrayList<OccurrenceEvent> events = new ArrayList<>();
        DOMParseBolt.getOccurEvents(content, events);
        events.remove(0);
        events.remove(events.size() - 1);

        String[] xPaths = {
                "/rss/channel/language[text()    =   \"en-us\"  ]",
                "/rss/channel/language[      contains(  text(), \"en\")]",
                "/rss/channel/title[contains(text(), \"sports\")]",
                "/rss/channel[  contains(text(), \"int\")]/title",
                "/rss/channel",
                "/rss/whatever"
        };

        XPathEngineImp engine = (XPathEngineImp) XPathEngineFactory.getXPathEngine();
        engine.setXPaths(xPaths);
//        System.out.println(engine.getMatchTable());
        for (OccurrenceEvent event : events){
            engine.evaluateEvent(event, "xml");
        }
//        System.out.println(engine.getMatchTable());
        boolean[] matched = engine.getMatchResult();
        boolean[] expected = {true, true, true, true, true, false};
        System.out.println(Arrays.toString(matched));
        assertArrayEquals(matched, expected);
    }

    public void testValidXPathElement(){
        String[] XPathElements = {
                "_123",
                "x_123.-",
                "x x",
                "x_123.-$",
                "-123"
        };

        boolean[] expected = {
                true,
                true,
                false,
                false,
                false
        };

        for (int i=0; i<expected.length; i++){
            assertEquals(expected[i], XPathEngineImp.validXMLElement(XPathElements[i]));
        }

    }
}
