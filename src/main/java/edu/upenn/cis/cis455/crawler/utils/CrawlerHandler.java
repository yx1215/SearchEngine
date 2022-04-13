package edu.upenn.cis.cis455.crawler.utils;

import edu.upenn.cis.cis455.crawler.Crawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CrawlerHandler {

    final static Logger logger = LogManager.getLogger(CrawlerHandler.class);

    public static HashMap<String, String> createMetadataMap(Map<String, List<String>> headers){
        Map<String, List<String>> tmp = new HashMap<>();
        for (String key : headers.keySet()){
            if (key != null){
                tmp.put(key.toLowerCase(), headers.get(key));
            }
        }
        HashMap<String, String> metadata = new HashMap<>();
        String contentType = tmp.get("content-type") == null ? "" : tmp.get("content-type").get(0).split(";")[0];
        metadata.put("content-type", contentType);
        String contentLength = tmp.get("content-length") == null ? String.valueOf(Integer.MAX_VALUE) : tmp.get("content-length").get(0);
        metadata.put("content-length", contentLength);
        String lastModified = tmp.get("last-modified") == null ? null : tmp.get("last-modified").get(0);
        metadata.put("last-modified", lastModified);
        String location = tmp.get("location") == null ? null: tmp.get("location").get(0);
        metadata.put("location", location);
        return metadata;

    }

    public static HashMap<String, String> getUrlMetaData(String urlString){
        HashMap<String, String> metaData = new HashMap<>();
        logger.info("Sending HEAD request to: " + urlString);
        try {
            URL url = new URL(urlString);
            URLInfo urlInfo = new URLInfo(urlString);
            HttpURLConnection connection;
            if (urlInfo.isSecure()){
                connection = (HttpsURLConnection) url.openConnection();
            }
            else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestProperty("User-Agent", "cis455crawler");
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            Map<String, List<String>> headers = connection.getHeaderFields();
            metaData = createMetadataMap(headers);
            metaData.put("status", String.valueOf(connection.getResponseCode()));
            connection.disconnect();
//            System.out.println(metaData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metaData;
    }

    public static void parseRobot(URLInfo urlInfo,
                                  HashMap<String, Long> checkPoints,
                                  HashMap<String, ArrayList<String>> disallow,
                                  HashMap<String, Integer> crawlDelay) {

        String protocol;

        String host = urlInfo.getHostName();
        logger.info("Getting robot.txt for host " + host);
        if (urlInfo.isSecure()){
            protocol = "https://";
        }
        else {
            protocol = "http://";
        }
        String robotPath = protocol + host + "/robots.txt";
        HashMap<String, String> metaData = getUrlMetaData(robotPath);
        String body = null;
        if (metaData.get("status").equals("200")){
            body = getUrlContent(robotPath);
            checkPoint(host, checkPoints);
        }

        ArrayList<String> disallowList = new ArrayList<>();
        int delay = 0;
        if (body != null){
            String crawlerRule = getCrawlerRule(body).strip();
            HashMap<String, ArrayList<String>> rules = parseCrawlerRule(crawlerRule);
            if (rules.get("Crawl-delay") != null){
                delay = Integer.parseInt(rules.get("Crawl-delay").get(0));
            }
            if (rules.get("Disallow") != null){
                disallowList = rules.get("Disallow");
            }

        }
        disallow.put(host, disallowList);
        crawlDelay.put(host, delay);

    }

    public static String getUrlContent(String urlString){
        String body = null;
        logger.info("Sending GET request to: " + urlString);
        try {
            URL url = new URL(urlString);
            URLInfo urlInfo = new URLInfo(urlString);
            HttpURLConnection connection;
            if (urlInfo.isSecure()){
                connection = (HttpsURLConnection) url.openConnection();
            }
            else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "cis455crawler");
            if (connection.getResponseCode() == 200){
                body = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            else {
                logger.warn("status for url: " + urlString + " is " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    public static String getCrawlerRule(String body){
        int start;
        start = body.indexOf("User-agent: cis455crawler");
        if (start == -1){
            start = body.indexOf("User-agent: *");
        }
        if (start == - 1){
            return "";
        } else {
            int end = body.indexOf("User-agent:", start + 1);
            if (end == -1){
                end = body.length();
            }
            return body.substring(start, end);
        }
    }

    public static HashMap<String, ArrayList<String>> parseCrawlerRule(String rule){
        String[] subStrings = rule.split("\n");
        HashMap<String, ArrayList<String>> content = new HashMap<>();

        for (String subString: subStrings){
            int splitIndex = subString.indexOf(":");
            if (splitIndex == -1){
                continue;
            }
            String key = subString.substring(0, splitIndex).trim();
            String value = subString.substring(splitIndex + 1).trim();

            ArrayList<String> tmp = content.get(key);
            if (tmp == null){
                tmp = new ArrayList<>();
            }
            tmp.add(value);
            content.put(key, tmp);
        }

        return content;
    }

    public static void checkPoint(String host, HashMap<String, Long> checkPoints){
        checkPoints.put(host, new Date().getTime());
    }

    public static boolean isDisallowed(URLInfo urlInfo, HashMap<String, ArrayList<String>> disallow){

        String host = urlInfo.getHostName();
        String path = urlInfo.getFilePath();
        ArrayList<String> hostDisallow = disallow.get(host);
        if (hostDisallow == null){
            return true;
        }
        boolean state = false;
        for (String dir : hostDisallow){
            if (path.startsWith(dir)) {
                state = true;
                break;
            }
        }
        return state;
    }
}
