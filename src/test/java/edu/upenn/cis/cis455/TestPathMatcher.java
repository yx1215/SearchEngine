package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImp;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;

import static org.junit.Assert.assertArrayEquals;


public class TestPathMatcher extends TestCase {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    public void testAddInvalidXPath(){
        boolean passed = true;

        String[] xPaths = {
                "/rss/channel/language[t331ext()=\"en-us\"]",
                "/rss/channel/language[text()=en-us\"]",
                "/rss/channel/language[text()]",
                "/rss/channel/language[contains(text(), \"\")",
                "/rss/channel/language[contains(text(), dad\")]",
                "/rss/channel/language[contains(whatever(), \"did\")]",
        };
        for (String xPath : xPaths){
            XPathEngineImp engine = (XPathEngineImp) XPathEngineFactory.getXPathEngine();
            try {
                engine.addXPath(xPath);
                passed = false;
                System.out.println(engine.getMatchTable());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        assertTrue(passed);
    }

    public void testValidXPath(){
        String[] xPaths = {
                "/rss/channel/language[text()    =   \"en-us\"  ]",
                "/rss/channel/language[      contains(  text(), \"en\")]",
                "/rss/channel/title[contains(text(), \"sports\")]",
                "/rss/channel[  contains(text(), \"int\")]/title",
                "/rss/channel",
                "/rss/whatever"
        };
        // equivalent xml:
//         <rss>
//            <channel>
//                interesting
//                <language>en-us</language>
//                <title>my sports</title>
//            </channel>
//         </rss>
        OccurrenceEvent[] events = {
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

    public void testValidXPathWithHtml(){
        String[] xPaths = {
                "/RSS/channel/language[text()    =   \"en-us\"  ]",
                "/rss/Channel/Language[      contains(  text(), \"en\")]",
        };
        // equivalent xml:
        // <rss>
        //    <channel>
        //        interesting
        //        <language>en-us</language>
        //        <title>my sports</title>
        //    </channel>
        // </rss>
        OccurrenceEvent[] events = {
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
        XPathEngineImp engine = (XPathEngineImp) XPathEngineFactory.getXPathEngine();
        engine.setXPaths(xPaths);
//        System.out.println(engine.getMatchTable());
        for (OccurrenceEvent event : events){
            engine.evaluateEvent(event, "html");
        }
//        System.out.println(engine.getMatchTable());
        boolean[] matched = engine.getMatchResult();
        boolean[] expected = {true, true};
        System.out.println(Arrays.toString(matched));
        assertArrayEquals(matched, expected);
    }

}
