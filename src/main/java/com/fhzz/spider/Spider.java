package com.fhzz.spider;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fhzz.util.MD5Util;
import com.fhzz.util.ZLib;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * @FileName : (Spider.java) 
 * 
 * @description  : (爬虫)
 * @author: gaoyun
 * @version: Version No.1
 * @date: 2017年12月29日
 * @modify: 2017年12月29日 下午2:43:45
 * @copyright: gaoyun
 *
 */
public class Spider {
	
	private BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("status", 1));
	private BasicDBObject query = new BasicDBObject("status", 0);
	private MongoClient server;
	private Regex regex;
	
	public static void main(String[] args) {
		try {
			new Spider().execute();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void execute() throws InterruptedException {
		init();
		
		Thread[] threads = new Thread[Config.THREAD_COUNT];
		for (int x = 0; x < threads.length; x++) {
			threads[x] = new Thread(new Crawl());
			threads[x].start();
		}

		for (int x = 0; x < threads.length; x++) {
			threads[x].join();
		}
		
		server.close();
		
		System.out.println("end");
	}
	
	private void init() {
		try {
			server = new MongoClient(Config.HOST);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			return;
		}
		loadConfig();
	}
	
	public synchronized void loadConfig() {
		String blackPath = Config.BLACK;
		String whitePath = Config.WHITE;
		regex = new Regex(blackPath, whitePath);
	}
	
	private void analysisUrls(Document doc) {
		Elements select = doc.select("a[href]");
		for (Element link : select) {
			String url = link.absUrl("href");
			if (!Global.regex(url, regex)) {
				continue;
			}
			saveUrl(url);
		}
	}
	
	private void saveUrl(String url) {
		if (url.contains("#")) {
			url = url.substring(0, url.indexOf("#"));
		}
		DBCollection collection = server.getDB(Config.DB).getCollection("url");
		BasicDBObject doc = new BasicDBObject();
		doc.append("url", url);
		doc.append("md5", MD5Util.getMD5(url));
		doc.append("status", 0);
		doc.append("date", new Date());
		try {
			collection.insert(doc);
		} catch (Exception e) {
			return;
		}
	}
	
	class Crawl implements Runnable {
		
		@Override
		public void run() {
			DBCollection collection = server.getDB(Config.DB).getCollection("url");
			while (true) {
				DBObject find = collection.findAndModify(query, update);
				if (find == null) {
					break;
				}
				String url = find.get("url").toString();
				Connection connect = Jsoup.connect(url).timeout(30000).followRedirects(true);
				Document doc = null;
				try {
					doc = connect.get();
				} catch (IOException e) {
					System.out.println("crawl >> " + url + " >> " + e.getMessage());
					continue;
				}
				System.out.println("crawl >> " + url);
				if (doc == null) {
					continue;
				}
				commitUrl(url);
				analysisUrls(doc);
				commitContent(doc.html(), url);
			}
		}
	}

	private void commitUrl(String url) {
		DBCollection collection = server.getDB(Config.DB).getCollection("url");
		BasicDBObject query = new BasicDBObject();
		query.put("url", url);
		BasicDBObject update = new BasicDBObject();
		BasicDBObject modify = new BasicDBObject();
		modify.put("status", 2);
		update.put("$set", modify);
		collection.update(query, update, true, true);
	}
	
	private void commitContent(String content, String url) {
		try {
			DBCollection collection = server.getDB(Config.DB).getCollection("content");
			BasicDBObject doc = new BasicDBObject();
			doc.append("url", url);
			doc.append("data", ZLib.compress(content.getBytes(Config.CHARSET)));
			doc.append("md5", MD5Util.getMD5(url));
			doc.append("date", new Date());
			collection.insert(doc);
		} catch (Exception e) {
			return;
		}
	}
}
