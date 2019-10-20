package com.roy.football.match.crawler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ContentFetchException;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.url.WebURL;

public class OFNCrawler {
	private final static Pattern FILTERS = Pattern
			.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	private final static String MATCH_URL_PREFIX = "http://odds.159cai.com/match";
	
	private Parser parser;
	private PageFetcher pageFetcher;
	
	public OFNCrawler (PageFetcher pageFetcher, Parser parser) {
		this.pageFetcher = pageFetcher;
		this.parser = parser;
	}

	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches()
				&& href.startsWith(MATCH_URL_PREFIX);
	}

	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		System.out.println("URL: " + url);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("Text:\r\n " + text);
			System.out.println("Html:\r\n " + html);
			System.out.println(links);
		}
	}
	
	public void process(WebURL curURL) {
		PageFetchResult fetchResult = null;
	    try {
	      if (curURL == null) {
	        throw new Exception("Failed processing a NULL url !?");
	      }

	      fetchResult = pageFetcher.fetchPage(curURL);
	      int statusCode = fetchResult.getStatusCode();

	      Page page = new Page(curURL);
	      page.setFetchResponseHeaders(fetchResult.getResponseHeaders());
	      page.setStatusCode(statusCode);
	      if (statusCode == 200) { // if status code is 200
	        if (!fetchResult.fetchContent(page)) {
	          throw new ContentFetchException();
	        }

	        parser.parse(page, curURL.getURL());

	        visit(page);
	      }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	      if (fetchResult != null) {
	        fetchResult.discardContentIfNotConsumed();
	      }
	    }
	}
}
