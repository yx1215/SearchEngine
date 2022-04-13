package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.stormlite.bolt.OutputCollector;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Implement this factory to produce your XPath engine and SAX handler as
 * necessary. It may be called by the test/grading infrastructure.
 * 
 * @author cis455
 *
 */
public class XPathEngineFactory {
    public static XPathEngine getXPathEngine() {
        return new XPathEngineImp();
    }

    public static DefaultHandler getSAXHandler(ArrayList<OccurrenceEvent> events) {
        return new SAXHandler(events);
    }
}
