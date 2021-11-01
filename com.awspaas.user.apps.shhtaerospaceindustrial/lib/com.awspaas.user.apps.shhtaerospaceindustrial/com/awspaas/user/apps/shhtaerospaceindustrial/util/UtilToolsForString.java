/**
 * 
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.util;

/**
 * @author Tlang
 *
 */
public class UtilToolsForString {
	/**
	 * @desc 将对象转换成字符串
	 * @param obj
	 * @return
	 */
	public static String handleObjectToString(Object obj) {
		return obj == null ? "":obj.toString();
	}
}
