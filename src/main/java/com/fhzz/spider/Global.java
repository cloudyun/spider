package com.fhzz.spider;

/**
 * @FileName : (Global.java) 
 * 
 * @description  : (规则匹配)
 * @author: gaoyun
 * @version: Version No.1
 * @date: 2017年12月29日
 * @modify: 2017年12月29日 下午2:41:23
 * @copyright: gaoyun
 *
 */
public class Global {
	
	public static boolean regex(String target, Regex regex) {
		for (String black : regex.getBlackList()) {
			if (!matches(target, black)) {
				continue;
			}
			return false;
		}
		for (String white : regex.getWhiteList()) {
			if (!matches(target, white)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private static boolean matches(String target, String regex) {
		String[] splits = regex.split("\\{");
		if (splits.length == 0) {
			return target.equals(regex);
		}
		if (splits[0].length() != 0 && !target.startsWith(splits[0])) {
			return false;
		}
		target = target.substring(splits[0].length());
		if (splits.length < 2) {
			return true;
		}
		for (int x = 1; x < splits.length; x++) {
			target = matcheSplit(target, splits[x]);
			if (target == null) {
				return false;
			}
		}
		if (target.length() != 0) {
			return false;
		}
		return true;
	}
	
	private static String matcheSplit(String target, String split) {
		String regex = getMatches(split.substring(0, split.indexOf("}")));
		if (regex == null) {
			return null;
		}
		String splitEnd = split.substring(split.indexOf("}") + 1);
		if (splitEnd.length() == 0) {
			return target.matches(regex) ? "" : null;
		}
		if (!target.contains(splitEnd)) {
			return null;
		}
		String targetStart = target.substring(0, target.indexOf(splitEnd));
		target = target.substring(target.indexOf(splitEnd));
		if (!targetStart.matches(regex)) {
			return null;
		}
		if (!target.startsWith(splitEnd)) {
			return null;
		}
		return target.substring(splitEnd.length());
	}
	
	private static String getMatches(String str) {
		switch (str) {
		case "d+":
			return "\\d+";
		case "d":
			return "\\d";
		case "*":
			return ".*";
		default:
			return null;
		}
	}
}
