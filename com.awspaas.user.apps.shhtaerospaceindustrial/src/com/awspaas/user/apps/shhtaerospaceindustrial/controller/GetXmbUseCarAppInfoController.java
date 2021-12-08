package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class GetXmbUseCarAppInfoController {
	/**
	 * 维修订单列表
	 * @param uc
	 * @param roleType 0：普通用户|1：调度|6：项目办主任|3：单位分管领导|4:司机
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType
	 * @return
	 */
	@Mapping("shsy.getYbUseCarAppInfoList")
	public String getYbUseCarAppInfo(UserContext uc,String roleType,String bDate,String eDate,String page,String pageCount,String taskType) {
		JSONObject returnData = new JSONObject();
		int page1 = Integer.parseInt(page);
		int pageCount1 = Integer.parseInt(pageCount);
		try {
			String userId = uc.getUID();
//			userId = "gcbdd1";//测试
			String sid = uc.getSessionId();
			if(page1<1) {
				returnData.put("status", "1");
				returnData.put("message", "请传入大于等于1的起始页！");
				return returnData.toString();
			}
			//获取起始条数和结束条数
			int start = (page1-1)*pageCount1+1;
			int end = page1*pageCount1;
			StringBuilder querySql0 = new StringBuilder();
			//涉及自己的代办
			String querySql1 = "select rownum rn,t.BDATE,t.USEREASON,t.BOARDINGPLACE,t.TARGETPLACE,t.VEHICLETYPE,t.CPH,wfc.processinstid wf_processinstid,wfc.id wf_id " +
					"from BO_EU_YBOFFICEUSECAR t " + 
					"left join WFC_TASK wfc on wfc.PROCESSINSTID=t.bindid " + 
					"where wfc.TARGET = '"+userId+"' AND wfc.DISPATCHID IS NOT NULL AND wfc.TASKTITLE NOT LIKE '%空标题%' ";
			
			//涉及自己的已办
			String querySql2 = "select rownum rn,t.BDATE,t.USEREASON,t.BOARDINGPLACE,t.TARGETPLACE,t.VEHICLETYPE,t.CPH,wfh.processinstid wf_processinstid,wfh.id wf_id " +
					"from BO_EU_YBOFFICEUSECAR t " + 
					"left join WFH_TASK wfh on wfh.PROCESSINSTID=t.bindid " + 
					"where  wfh.TARGET = '"+userId+"' AND wfh.DISPATCHID IS NOT NULL AND wfh.TASKTITLE NOT LIKE '%空标题%' ";
			if(!bDate.equals("") && !eDate.equals("")) {
				querySql1 = querySql1+"and (to_date(substr(t.BDATE,0,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
				querySql2 = querySql2+"and (to_date(substr(t.BDATE,0,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
			}
			//if((roleType.equals("0")||roleType.equals("1")||roleType.equals("6")||roleType.equals("3"))&&taskType.equals("0")) {//普通用户，工程部调度，客服，维修人员查看0代办
			if(taskType.equals("0")) {
			querySql0.append(querySql1);
			//}else if((roleType.equals("0")||roleType.equals("1")||roleType.equals("6")||roleType.equals("3"))&&taskType.equals("1")) {//普通用户，工程部调度，客服，维修人员查看1全部
			}else if(taskType.equals("1")) {
			querySql0.append("(").append(querySql2).append(") union (").append(querySql1).append(")");
			}else {
				returnData.put("status", "1");
				returnData.put("message", "输入参数不合法！");
				return returnData.toString();
			}
			String querySql = "select * from ("+querySql0+") where rn>="+start+" and rn<="+end+" order by BDATE desc";
			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				returnData.put("status", "0");
				JSONArray orderNull = new JSONArray();
				returnData.put("orderList", orderNull);
				returnData.put("message", "没有任何项目办用车申请记录");
				return returnData.toString();
			}
			JSONArray orderArr = new JSONArray();
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			for (Map<String, Object> dataMap : dataList) {
				JSONObject orderItem = new JSONObject();
//				String bxrqsj = CoreUtil.objToStr(dataMap.get("sqrq")) +" "+ CoreUtil.objToStr(dataMap.get("bxsj"));
				String BDATE = CoreUtil.objToStr(dataMap.get("BDATE"));
//				if(yywxsj.length()>16) {
//					yywxsj = yywxsj.substring(0, 16);
//				}
				
				String USEREASON = CoreUtil.objToStr(dataMap.get("USEREASON"));
				String BOARDINGPLACE = CoreUtil.objToStr(dataMap.get("BOARDINGPLACE"));
				String TARGETPLACE = CoreUtil.objToStr(dataMap.get("TARGETPLACE"));
				String VEHICLETYPE = CoreUtil.objToStr(dataMap.get("VEHICLETYPE"));
				String CPH = CoreUtil.objToStr(dataMap.get("CPH"));
				String processInstId = CoreUtil.objToStr(dataMap.get("wf_processinstid"));//流程实例ID
				String taskInstId = CoreUtil.objToStr(dataMap.get("wf_id"));//任务实例Id
				String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
				
				orderItem.put("usereason", USEREASON);
				orderItem.put("bdate", BDATE);
				orderItem.put("cfplace", BOARDINGPLACE);
				orderItem.put("targeplace", TARGETPLACE);
				orderItem.put("cartype", VEHICLETYPE);
				orderItem.put("cph", CPH);
				orderItem.put("url", url);
				orderArr.add(orderItem);
			}
			
			// 成功
			returnData.put("status", "0");
			returnData.put("orderList", orderArr);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
	@Mapping("shsy.getXmbUseCarAppInfoList")
	public String getRepairOrderList(UserContext uc,String roleType,String bDate,String eDate,String page,String pageCount,String taskType) {
		JSONObject returnData = new JSONObject();
		int page1 = Integer.parseInt(page);
		int pageCount1 = Integer.parseInt(pageCount);
		try {
			String userId = uc.getUID();
//			userId = "gcbdd1";//测试
			String sid = uc.getSessionId();
			if(page1<1) {
				returnData.put("status", "1");
				returnData.put("message", "请传入大于等于1的起始页！");
				return returnData.toString();
			}
			//获取起始条数和结束条数
			int start = (page1-1)*pageCount1+1;
			int end = page1*pageCount1;
			StringBuilder querySql0 = new StringBuilder();
			//涉及自己的代办
			String querySql1 = "select rownum rn,t.BDATE,t.USEREASON,t.BOARDINGPLACE,t.TARGETPLACE,t.VEHICLETYPE,t.CPH,wfc.processinstid wf_processinstid,wfc.id wf_id " +
					"from BO_EU_SHHT_XMOFFICEUSECAR t " + 
					"left join WFC_TASK wfc on wfc.PROCESSINSTID=t.bindid " + 
					"where wfc.TARGET = '"+userId+"' AND wfc.DISPATCHID IS NOT NULL AND wfc.TASKTITLE NOT LIKE '%空标题%' ";
			
			//涉及自己的已办
			String querySql2 = "select rownum rn,t.BDATE,t.USEREASON,t.BOARDINGPLACE,t.TARGETPLACE,t.VEHICLETYPE,t.CPH,wfh.processinstid wf_processinstid,wfh.id wf_id " +
					"from BO_EU_SHHT_XMOFFICEUSECAR t " + 
					"left join WFH_TASK wfh on wfh.PROCESSINSTID=t.bindid " + 
					"where  wfh.TARGET = '"+userId+"' AND wfh.DISPATCHID IS NOT NULL AND wfh.TASKTITLE NOT LIKE '%空标题%' ";
			if(!bDate.equals("") && !eDate.equals("")) {
				querySql1 = querySql1+"and (to_date(substr(t.BDATE,0,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
				querySql2 = querySql2+"and (to_date(substr(t.BDATE,0,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
			}
			if((roleType.equals("0")||roleType.equals("1")||roleType.equals("6")||roleType.equals("3"))&&taskType.equals("0")) {//普通用户，工程部调度，客服，维修人员查看0代办
				querySql0.append(querySql1);
			}else if((roleType.equals("0")||roleType.equals("1")||roleType.equals("6")||roleType.equals("3"))&&taskType.equals("1")) {//普通用户，工程部调度，客服，维修人员查看1全部
				querySql0.append("(").append(querySql2).append(") union (").append(querySql1).append(")");
			}else {
				returnData.put("status", "1");
				returnData.put("message", "输入参数不合法！");
				return returnData.toString();
			}
			String querySql = "select * from ("+querySql0+") where rn>="+start+" and rn<="+end+" order by BDATE desc";
			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				returnData.put("status", "0");
				JSONArray orderNull = new JSONArray();
				returnData.put("orderList", orderNull);
				returnData.put("message", "没有任何项目办用车申请记录");
				return returnData.toString();
			}
			JSONArray orderArr = new JSONArray();
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			for (Map<String, Object> dataMap : dataList) {
				JSONObject orderItem = new JSONObject();
//				String bxrqsj = CoreUtil.objToStr(dataMap.get("sqrq")) +" "+ CoreUtil.objToStr(dataMap.get("bxsj"));
				String BDATE = CoreUtil.objToStr(dataMap.get("BDATE"));
//				if(yywxsj.length()>16) {
//					yywxsj = yywxsj.substring(0, 16);
//				}
				
				String USEREASON = CoreUtil.objToStr(dataMap.get("USEREASON"));
				String BOARDINGPLACE = CoreUtil.objToStr(dataMap.get("BOARDINGPLACE"));
				String TARGETPLACE = CoreUtil.objToStr(dataMap.get("TARGETPLACE"));
				String VEHICLETYPE = CoreUtil.objToStr(dataMap.get("VEHICLETYPE"));
				String CPH = CoreUtil.objToStr(dataMap.get("CPH"));
				String processInstId = CoreUtil.objToStr(dataMap.get("wf_processinstid"));//流程实例ID
				String taskInstId = CoreUtil.objToStr(dataMap.get("wf_id"));//任务实例Id
				String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
				
				orderItem.put("usereason", USEREASON);
				orderItem.put("bdate", BDATE);
				orderItem.put("cfplace", BOARDINGPLACE);
				orderItem.put("targeplace", TARGETPLACE);
				orderItem.put("cartype", VEHICLETYPE);
				orderItem.put("cph", CPH);
				orderItem.put("url", url);
				orderArr.add(orderItem);
			}
			
			// 成功
			returnData.put("status", "0");
			returnData.put("orderList", orderArr);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
}
