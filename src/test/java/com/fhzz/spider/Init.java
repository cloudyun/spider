package com.fhzz.spider;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;

import com.fhzz.util.MD5Util;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

@SuppressWarnings("deprecation")
public class Init {
	
	public static void initUrlCollection(MongoClient server) {
		DBCollection collection = server.getDB(Config.DB).getCollection("url");
		BasicDBObject url_ = new BasicDBObject();
		url_.put("url", 1);
		collection.ensureIndex(url_, "url_", true);
		BasicDBObject status_ = new BasicDBObject();
		status_.put("status", 1);
		collection.ensureIndex(status_, "status_");
	}

	public static void initContentCollection(MongoClient server) {
		DBCollection collection = server.getDB(Config.DB).getCollection("content");
		BasicDBObject url_ = new BasicDBObject();
		url_.put("url", 1);
		collection.ensureIndex(url_, "url_", true);
	}

	public static void initEntry(MongoClient server) {
		DBCollection collection = server.getDB(Config.DB).getCollection("url");
		try (FileInputStream fis = new FileInputStream(Config.ENTRYS);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				if (line.contains("{")) {
					addAll(collection, line);
					continue;
				}
				add(collection, line);
			}
		} catch (Exception e) {
			System.out.println("读取黑名单出现异常：" + e.getMessage());
		}
	}
	
	private static void addAll(DBCollection collection, String line) {
		String se = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
		int start = Integer.parseInt(se.substring(0, se.indexOf("-")));
		int end = Integer.parseInt(se.substring(se.indexOf("-") + 1));
		for (int x = start; x <= end; x++) {
			String url = line.replace("{" + se + "}", String.valueOf(x));
			add(collection, url);
		}
	}

	private static void add(DBCollection collection, String line) {
		BasicDBObject entry = new BasicDBObject();
		entry.put("url", line);
		entry.put("status", 0);
		entry.put("md5", MD5Util.getMD5(line));
		entry.put("date", new Date());
		collection.insert(entry);
	}

	public static void main(String[] args) throws UnknownHostException {
		MongoClient server = new MongoClient(Config.HOST);
		System.out.println("初始化链接表...");
		initUrlCollection(server);
		System.out.println("初始化内容表...");
		initContentCollection(server);
		System.out.println("初始化入口链接...");
		initEntry(server);
	}
}
