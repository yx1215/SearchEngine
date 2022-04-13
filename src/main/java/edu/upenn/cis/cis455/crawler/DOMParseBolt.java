package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.SAXHandler;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class DOMParseBolt implements IRichBolt {

    static Logger logger = LogManager.getLogger(LinkExtractorBolt.class);

    String executorId = UUID.randomUUID().toString();

    private final Fields fields = new Fields("url", "event", "type");

    private OutputCollector collector;

    public DOMParseBolt() {
    }

    @Override
    public String getExecutorId() {
        return this.executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(this.fields);
    }

    /**
     * Called when a bolt is about to be shut down
     */
    @Override
    public void cleanup() {

    }

    /**
     * Processes a tuple
     *
     * @param input
     */
    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        String content = input.getStringByField("content");
        String contentType = input.getStringByField("contentType");
        String type;
        if (contentType.contains("html")){
            // if the document is html, parse through jsoup for formatting purpose.
            Document doc = Jsoup.parse(content);
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            content = doc.html();
            type = "html";
        }
        else {
            type = "xml";
        }

        ArrayList<OccurrenceEvent> events = new ArrayList<>();
        getOccurEvents(content, events);
        for (OccurrenceEvent event : events){
            this.collector.emit(new Values<Object>(url, event, type));
        }

    }

    /**
    Get all occurrence events from the document content
    **/
    public static void getOccurEvents(String content, ArrayList<OccurrenceEvent> events){
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            SAXHandler handler = new SAXHandler(events);
            InputSource is = new InputSource(new StringReader(content));
            saxParser.parse(is, handler);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    /**
     * Called when this task is initialized
     *
     * @param stormConf
     * @param context
     * @param collector
     */
    @Override
    public void prepare(Map<String, Object> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    /**
     * Called during topology creation: sets the output router
     *
     * @param router
     */
    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    /**
     * Get the list of fields in the stream tuple
     *
     * @return
     */
    @Override
    public Fields getSchema() {
        return this.fields;
    }
}
