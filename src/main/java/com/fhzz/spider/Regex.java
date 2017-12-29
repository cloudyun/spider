package com.fhzz.spider;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @FileName : (Regex.java) 
 * 
 * @description  : (黑白名单)
 * @author: gaoyun
 * @version: Version No.1
 * @date: 2017年12月29日
 * @modify: 2017年12月29日 下午2:43:14
 * @copyright: gaoyun
 *
 */
public class Regex {
	
	private List<String> blackList = new ArrayList<String>();

	private List<String> whiteList = new ArrayList<String>();

	public Regex(String blackPath, String whitePath) {
		try (FileInputStream fis = new FileInputStream(blackPath);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				blackList.add(line);
			}
		} catch (Exception e) {
			System.out.println("读取黑名单出现异常：" + e.getMessage());
		}
		try (FileInputStream fis = new FileInputStream(whitePath);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				whiteList.add(line);
			}
		} catch (Exception e) {
			System.out.println("读取黑名单出现异常：" + e.getMessage());
		}
	}

	public List<String> getBlackList() {
		return blackList;
	}

	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}

	public List<String> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(List<String> whiteList) {
		this.whiteList = whiteList;
	}
}
