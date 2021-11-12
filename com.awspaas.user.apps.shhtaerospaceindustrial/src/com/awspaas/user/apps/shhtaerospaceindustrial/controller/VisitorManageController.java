package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.HashMap;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.commons.htmlframework.HtmlPageTemplate;
import com.actionsoft.bpms.server.SSOUtil;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.bpms.util.DBSql;

@Controller
public class VisitorManageController {
	
	
	/**
	 * 
	 * @Description 扫描二维码打开访客申请流程
	 * @author dingyi
	 * @date 2021年6月28日 上午10:07:20
	 * @param type
	 * @param employeeSid
	 * @return
	 */
	@Mapping(value="com.awspaas.user.apps.shhtaerospaceindustrial_VisitorManageController", session = false, noSessionEvaluate = "无安全隐患", noSessionReason = "自定义接口")
	public String getTripCityInfo(String type,String userId) {
		Map<String, Object> map = new HashMap<String, Object>();//存放传递到HTML页面的值
		try {
			SSOUtil ssoUtil = new SSOUtil();
			String sid = ssoUtil.registerClientSessionNoPassword("guest", "cn", "", "mobile");
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			map.put("sid", sid);
			map.put("url", portalUrl+"/r/w?sid="+sid+"&userId="+userId+"&userStatus=self&cmd=com.actionsoft.apps.workbench_mobile_process_start&groupId=obj_17a020d1d52941da856e3d0f520766ca&processDefId=obj_275bed151e5840d08e10e518644de33a");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HtmlPageTemplate.merge("com.awspaas.user.apps.workattendance", "openProcessHtml.html", map);
	}
	
	
	
	
}
