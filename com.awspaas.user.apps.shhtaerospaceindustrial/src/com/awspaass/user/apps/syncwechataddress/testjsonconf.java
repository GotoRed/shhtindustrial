package com.awspaass.user.apps.syncwechataddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

//import org.json.JSONObject;

import com.google.gson.Gson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.apps.*;
//import com.alibaba.fastjson.JSONObject;

public class testjsonconf {

	public static void main(String[] args) {
		inicfg ini = new inicfg();
		
			Gson gson = new Gson();
			String str = ini.readToString("C:\\shht\\bpm804\\apps\\install\\com.awspaas.user.apps.shhtaerospaceindustrial\\src\\com\\awspaass\\user\\apps\\syncwechataddress\\test.json");
			//System.out.println(str.trim());
			/*try {
				str  = gson.toJson(ini);
				//System.out.println("json 格式："+str);
				ini = gson.fromJson(str.trim(), inicfg.class);
				//System.out.println("配置文件：\r\n"+str);
				System.out.println(ini.toString());
			} catch (Exception e) {
				System.out.println("配置文件读取失败，异常退出");
				return ;
			}*/
			JSONArray btnArray = null;
			JSONObject jsonObject = JSONObject.fromObject(str);
			if (jsonObject != null) {
				   //取出按钮权限的数据
				   btnArray = jsonObject.getJSONArray("companys");
				  }
			 Iterator<Object> num = btnArray.iterator();
			 while(num.hasNext()) {
				 JSONObject btn = (JSONObject) num.next();
				 System.out.println(btn.get("name"));
				 System.out.println(btn.get("awsid"));
			 }

	}
	}

