package com.awspaass.user.apps.syncwechataddress;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import com.awspaass.user.apps.syncwechataddress.HttpClientUtil;
import com.awspaass.user.apps.syncwechataddress.WxUserInfo;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.awspaass.user.apps.syncwechataddress.Department;

public class WeChatUtil {

	public String getAccessToken(String corpid, String corpsecret) {
		String access_token;

//		String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ww0605aae701a55d9e&corpsecret=ZwrE6zsNaSfu_OdShAm1OzvJz0CvK-AZnVho9lPOMQU";
		String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret;
		String result = HttpClientUtil.doGet(access_token_url);

		JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
		//System.out.println(jsonObject);

		access_token = jsonObject.get("access_token").getAsString();
		//System.out.print(access_token);

		return access_token;
	}



	/**
	 * 获取部门列表
	 * 
	 * @param accessToken
	 * @param departmentId
	 * @return
	 */
	public List<Map<String,String>> getDepartmentList(String accessToken) {
		List<Map<String,String>> departments = new ArrayList<Map<String,String>>();
		// 1.获取请求的url
		String getDepartmentList_url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + accessToken;
		// 2.调用接口，发送请求，获取成员
		JsonObject jsonObject = new JsonParser().parse(HttpClientUtil.doGet(getDepartmentList_url)).getAsJsonObject();

		//System.out.println("jsonObject:" + jsonObject.toString());
		// 3.错误消息处理
		if (null != jsonObject) {
// 查询成功
			 List<Map<String, Object>> mapListJson = new Gson().fromJson(jsonObject.get("department").getAsJsonArray(), new TypeToken<List<Map<String, Object>>>() {}.getType()); 
			//List<Map<String, Object>> mapListJson =(List)jsonObject.get("department").getAsJsonArray();

			if (null != mapListJson) {
				// Department department = new Department();
				for (int i = 0; i < mapListJson.size(); i++) {
					Map<String, Object> dept = mapListJson.get(i);					
					if (null != dept.get("id")) {	
						Integer id=((Double)dept.get("id")).intValue();
						Integer p_id=((Double)dept.get("parentid")).intValue();
						String name=(String)dept.get("name");
						departments.add(new HashMap<String, String>(){{  
						      put("id",id.toString());      
						}}  );
						departments.add(new HashMap<String, String>(){{  
						      put("name",name);      
						}}  );
						departments.add(new HashMap<String, String>(){{  
						      put("parentid",p_id.toString());      
						}}  );
						
					}

				}
			}

		}
		//System.out.println(departments);
		return departments;
	}

	/**
	 * 获取部门成员详情
	 * 
	 * @param depts
	 * @param accessToken
	 * @param fetchChild//是否遍历子部门的成员，一般不要遍历，除非你就只获取父级部门或者子部门为空，不然会导致数据重复
	 * @return
	 */
	public List<WxUserInfo> getDepartmentUserDetails(List<Map<String,String>> depts, String accessToken, String fetchChild) {
		List<WxUserInfo> users = new ArrayList<WxUserInfo>();
		int count =0;
		//System.out.println(depts.get(0));
		for (count=0; count<depts.size(); count++) {
			// 1.获取请求的url
			Map<String,String> depid =  depts.get(count);
			count++;
			Map<String,String> depname =  depts.get(count);
			count++;
			//System.out.println(depname.get("name"));
			String getDepartmentUserDetails_url = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + accessToken + "&department_id=" + depid.get("id")
					+ "&fetch_child=" + fetchChild;

			// 2.调用接口，发送请求，获取部门成员
			JsonObject jsonObject = new JsonParser().parse(HttpClientUtil.doGet(getDepartmentUserDetails_url)).getAsJsonObject();
			//System.out.println(jsonObject);
			// 3.错误消息处理
			if (null != jsonObject) {

				// JSONArray array=jsonObject.getJSONArray("userlist");
				 List<Map<String, Object>> mapListJson = new Gson().fromJson(jsonObject.get("userlist").getAsJsonArray(), new TypeToken<List<Map<String, Object>>>() {}.getType()); 
				
				if (null != mapListJson) {
					for (int i = 0; i < mapListJson.size(); i++) {
						Map<String, Object> de = mapListJson.get(i);
						// 具体字段看自己业务需求
						WxUserInfo user = new WxUserInfo();
						user.setUserId(String.valueOf(de.get("userid")));
						user.setDepartment(depname.get("name"));
						user.setName(String.valueOf(de.get("name")));
						user.setPosition(String.valueOf(de.get("position")));
						user.setMobile(String.valueOf(de.get("mobile")));
						user.setGender(String.valueOf(de.get("gender")));
						user.setEmail(String.valueOf(de.get("email")));
						user.setStatus(String.valueOf(de.get("status")));
						user.setAlias(String.valueOf(de.get("alias")));
						//System.out.println(user.getName());

						
						users.add(user);						
					}
				}
			}

		}

		return users;
	}
/*	
	public static void main(String[] args) {
		WeChatUtil a = new WeChatUtil();
		String access_token=a.getAccessToken("wwd1feef53690aecf0","tBQRUDNSAIEKx_HwkqTLXjSwr_0ifr_o2vdsOc1_aU8");
		List<Map<String,String>> deps =a.getDepartmentList(access_token);
		a.getDepartmentUserDetails(deps, access_token, "0");
	}
	*/
}
