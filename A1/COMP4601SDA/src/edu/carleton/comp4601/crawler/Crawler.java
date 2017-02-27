package edu.carleton.comp4601.crawler;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.client.model.FindOneAndReplaceOptions;

import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.db.Database;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|"
            + "|mp3|mp3|zip|gz))$");
    private Graph graph = new Graph();

	static String crawlStorageFolder = "crawl";
	static int numCrawlers = 1;
	
	public static final String INDEX_DIR = "/Users/jackmccracken/index";
	
	public Crawler() {
		try {
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void crawl(String url) throws Exception {
		CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(5);
        config.setMaxPagesToFetch(1000);
        config.setPolitenessDelay(10);
		
		/*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        // "http://sikaman.dyndns.org:8888/courses/4601/resources/N-0.html"
        controller.addSeed(url);
        
        controller.start(Crawler.class, numCrawlers);
      
        while (!controller.isFinished()) Thread.sleep(100);
	}

	@Override
	public boolean shouldVisit(Page referrer, WebURL url) {
		String href = url.getURL();
		
		if (!FILTERS.matcher(href).matches() && href.startsWith("http://sikaman.dyndns.org:8888/")) {
			if (Database.getCollection("documents").count(eq("url", href)) == 0) {
				System.out.println("Processing " + url.getURL());
				return true;
			}
			else {
				System.out.println("Skipping url " + url.getURL());
				return false;
			}
		}

		return false;
	}
	
	@Override
	public void visit(Page page) {
		try {
			String url = page.getWebURL().getURL();
//			System.out.println("URL: " + url);
			
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				String text = htmlParseData.getText();
				String html = htmlParseData.getHtml();
				String title = htmlParseData.getTitle();
				Set<WebURL> links = htmlParseData.getOutgoingUrls();
				List<String> linksList = new ArrayList<>();

				for (WebURL u : links) {
					linksList.add(u.getURL());
				}
				
//				System.out.println("Text length: " + text.length());
//				System.out.println("Html length: " + html.length());
//				System.out.println("Number of outgoing links: " + links.size());

				Document doc = new Document();
				
				doc.put("id", page.getWebURL().getDocid());
				doc.put("url", page.getWebURL().getURL());
				doc.put("name", title);
				doc.put("text", text);
				doc.put("html", html);
				doc.put("links", linksList);
				doc.put("tags", new ArrayList<String>());

				edu.carleton.comp4601.dao.Document docObj = new edu.carleton.comp4601.dao.Document(doc);
				
				DocumentCollection.getInstance().add(docObj);
			}
			else {
				
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void onBeforeExit() {
		super.onBeforeExit();
	}
}
