package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class GetNoticeController {
	/**
	 * 每种业务代办任务数量
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.getRemainTask")
	public String getRemainTaskList(UserContext uc) {
		String userId = uc.getUID();
		JSONObject result = new JSONObject();
		String role = uc.getRoleModel().getName();
//		role="车队结算员";
//		ArrayList<String> items = new ArrayList<String>(Arrays.asList("canyin","cheliang_order","cheliang_out","cheliang_in","jiaoliu","baoxiu"));
		if(null==role) {
			result.put("status", "0");
			result.put("message", "您未分配角色");
			return result.toString();
		}
		String querySql0 = "";
		switch(role) {
			case "上航实业餐饮部":
				querySql0 = "select 'canyin' tasktype,count(1) remains from WFC_TASK t " + 
						"where t.processdefid='obj_4e2ae61aba4f47009d579fef68411056' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "交流中心销售":
				querySql0 = "select 'jiaoliu' tasktype,count(1) remains from WFC_TASK t " + 
						"where t.processdefid='obj_005ba5bf5ef34b278464d1620779fe7f' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "工程部调度":case "维修人员":
				querySql0 = "select 'baoxiu' tasktype,count(1) remains from WFC_TASK t " + 
						"where t.processdefid='obj_e3862d66965a460abdbb2cdb8031773d' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "车辆调度":case "上航实业车队管理部":
				querySql0 = "select 'cheliang' tasktype,count(1) remains from WFC_TASK t " + 
						"where t.processdefid='obj_b958c0c1ee3e4d05848413ed5ea2e2c4' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "外租公司调度":
				querySql0 = "select 'cheliang' tasktype,count(1) remains from WFC_TASK t " + 
						"where t.processdefid='obj_5b822a42fa7540948190da32038878dc' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "车辆驾驶员":
				querySql0 = "select 'cheliang' tasktype,count(1) remains from WFC_TASK t  " + 
						"where t.processdefid='obj_d951639b5cf447d592ea82551b884081' " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
			case "车队结算员"://车队结算员看所有
				querySql0 = "select 'cheliang' tasktype,count(1) remains from WFC_TASK t " + 
						"where (t.processdefid ='obj_b958c0c1ee3e4d05848413ed5ea2e2c4' or t.processdefid='obj_5b822a42fa7540948190da32038878dc' or t.processdefid='obj_d951639b5cf447d592ea82551b884081') " + 
						"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " + 
						"and t.target = '"+userId+"' " ; 
				break;
			case "普通员工":case "上航实业普通用户"://普通用户看到自己的行车任务单，外部加内部，不是代办
				querySql0 = "select 'cheliang' tasktype,count(t.CONTACTPERSONZH) remains from BO_EU_SH_VEHICLEORDER_MISSION t where t.CONTACTPERSONZH='"+userId+"' ";
				break;
			default :break;
		}
		if(querySql0.equals("")) {
			result.put("status", "0");
			result.put("message", "参数错误");
			return result.toString();
		}
//		else if(querySql0.equals("normel")) {//普通用户不显示代办
//			result.put("status", "0");
//			JSONArray orderNormal = new JSONArray();
//			JSONObject noticeItem = new JSONObject();
//			noticeItem.put("tasktype", role);
//			noticeItem.put("remains", 0);
//			orderNormal.add(noticeItem);
//			result.put("orderList", orderNormal);
//			return result.toString();
//		}
		
//		querySql0 = "select " + 
//				"(case  " + 
//				"when processdefid='obj_4e2ae61aba4f47009d579fef68411056' then 'canyin' " + 
//				"when processdefid='obj_b958c0c1ee3e4d05848413ed5ea2e2c4' then 'cheliang_order' " +
//		        "when processdefid='obj_5b822a42fa7540948190da32038878dc' then 'cheliang_out' " +
//		        "when processdefid='obj_d951639b5cf447d592ea82551b884081' then 'cheliang_in' " +
//				"when processdefid='obj_005ba5bf5ef34b278464d1620779fe7f' then 'jiaoliu' " + 
//				"when processdefid='obj_e3862d66965a460abdbb2cdb8031773d' then 'baoxiu' " + 
//				"else '-' " + 
//				"end " + 
//				") tasktype, " + 
//				"remains " + 
//				" from ( " + 
//				"select t.processdefid,count(1) remains from WFC_TASK t  " + 
//				"where t.processdefid in('obj_4e2ae61aba4f47009d579fef68411056','obj_b958c0c1ee3e4d05848413ed5ea2e2c4','obj_5b822a42fa7540948190da32038878dc','obj_d951639b5cf447d592ea82551b884081','obj_005ba5bf5ef34b278464d1620779fe7f','obj_e3862d66965a460abdbb2cdb8031773d') " + 
//				"and t.dispatchid is not null and t.tasktitle not like '%空标题%' " +
//				"and t.target = '"+userId+"' " +
//				"group by t.processdefid " + 
//				")";
		try {
			List<Map<String, Object>> dataList = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				result.put("status", "0");
				JSONArray orderNull = new JSONArray();
				
				if(role.equals("普通员工")) {
					JSONObject noticeItem = new JSONObject();
					noticeItem.put("tasktype", "canyin");
					noticeItem.put("remains", 0);
					orderNull.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "jiaoliu");
					noticeItem.put("remains", 0);
					orderNull.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "baoxiu");
					noticeItem.put("remains", 0);
					orderNull.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "cheliang");
					noticeItem.put("remains", 0);
					orderNull.add(noticeItem);
				}else {
					JSONObject noticeItem = new JSONObject();
					noticeItem.put("role", role);
					noticeItem.put("tasktype", "-");
					noticeItem.put("remains", 0);
					orderNull.add(noticeItem);
				}

				result.put("orderList", orderNull);
				return result.toString();
			}
			JSONArray noticeArr = new JSONArray();
//			ArrayList<String> partItems = new ArrayList<String>();
			for (Map<String, Object> dataMap : dataList) {
				
				String tasktype = CoreUtil.objToStr(dataMap.get("tasktype"));
				String remains = CoreUtil.objToStr(dataMap.get("remains"));
				if(role.equals("普通员工")) {
					JSONObject noticeItem = new JSONObject();
					noticeItem.put("tasktype", "canyin");
					noticeItem.put("remains", 0);
					noticeArr.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "jiaoliu");
					noticeItem.put("remains", 0);
					noticeArr.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "baoxiu");
					noticeItem.put("remains", 0);
					noticeArr.add(noticeItem);
					noticeItem = new JSONObject();
					noticeItem.put("tasktype", "cheliang");
					noticeItem.put("remains", remains);
					noticeArr.add(noticeItem);
				}else {
					JSONObject noticeItem = new JSONObject();
					noticeItem.put("tasktype", tasktype);
					noticeItem.put("remains", remains);
					noticeArr.add(noticeItem);
				}
				
//				partItems.add(tasktype);
			}
//			for(String key:items) {
//				if(!partItems.contains(key)) {
//					JSONObject noticeItem = new JSONObject();
//					noticeItem.put("tasktype", key);
//					noticeItem.put("remains", 0);
//					noticeArr.add(noticeItem);
//				}
//			}
			// 成功
			result.put("status", "0");
			result.put("noticeArr", noticeArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
//	@Mapping("shsy.getNoticeList")
//	public String getNoticeList(UserContext uc,String selType,String page,String pageRows) {
//		String userId = uc.getUID();//登录人账号
////		String sid = uc.getSessionId();
//		JSONObject result = new JSONObject();
//		int startnum = (Integer.parseInt(page)-1)*(Integer.parseInt(pageRows));
//		int endnum = startnum + Integer.parseInt(pageRows)+1;
//		try {
//			String querySql = null;
//			String querySql0 = "select rownum rn,t.tasktitle,t.begintime,t.owner from WFC_TASK t where t.target='"+userId+"' " +
//					"order by t.begintime desc";
//			if(selType.equals("0")) {
//				querySql = "select * from ("+querySql0+") where rn<=2";
//			}else if(selType.equals("1")){
//				querySql = "select * from ("+querySql0+") where rn>"+startnum+" and rn<"+endnum+" ";
//			}else {
//				result.put("status", "0");
//				JSONArray noticeNull = new JSONArray();
//				result.put("noticeArr", noticeNull);
//				result.put("message", "参数传值错误");
//				return result.toString();
//			}
//			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList == null || dataList.isEmpty()) {
//				result.put("status", "0");
//				JSONArray noticeNull = new JSONArray();
//				result.put("noticeArr", noticeNull);
//				result.put("message", "没有任何消息记录");
//				return result.toString();
//			}
//			JSONArray noticeArr = new JSONArray();
//			JSONObject noticeItem = null;
//			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////			String portalUrl = SDK.getPortalAPI().getPortalUrl();
//			for (Map<String, Object> dataMap : dataList) {
//				noticeItem = new JSONObject();
//				String taskTitle = CoreUtil.objToStr(dataMap.get("tasktitle"));
//				String beginTime = CoreUtil.objToStr(dataMap.get("begintime"));
//				
//				Date beginTime2 = ft.parse(beginTime.replace('/', '-'));
//				String beginTime3= fromToday(beginTime2);
//				String owner = CoreUtil.objToStr(dataMap.get("owner"));
//				String ownerName = UserContext.fromUID(owner).getUserName();
////				String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=2&taskInstId="+taskInstId+"&displayToolbar=true";
//				
//				noticeItem.put("taskTitle", taskTitle);
//				noticeItem.put("beginTime", beginTime);
//				noticeItem.put("beginTime3", beginTime3);
//				noticeItem.put("owner", owner);
//				noticeItem.put("ownerName", ownerName);
//				
//				noticeArr.add(noticeItem);
//			}
//			
//			// 成功
//			result.put("status", "0");
//			result.put("noticeArr", noticeArr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result.put("status", "1");
//			result.put("message", e.getMessage());
//		}
//		return result.toString();
//	}
//	
//	private long ONE_MINUTE = 60;
//	private long ONE_HOUR = 3600;
//	private long ONE_DAY = 86400;
//	private long ONE_MONTH = 2592000;
//	private long ONE_YEAR = 31104000;
//	public String fromToday(Date date) {
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(date);
//
//		long time = date.getTime() / 1000;
//		long now = new Date().getTime() / 1000;
//		long ago = now - time;
//		if (ago <= ONE_HOUR)
//			return ago / ONE_MINUTE + "分钟前";
//		else if (ago <= ONE_DAY)
//			return ago / ONE_HOUR + "小时" + (ago % ONE_HOUR / ONE_MINUTE)
//					+ "分钟前";
//		else if (ago <= ONE_DAY * 2)
//			return "昨天" + calendar.get(Calendar.HOUR_OF_DAY) + "点"
//					+ calendar.get(Calendar.MINUTE) + "分";
//		else if (ago <= ONE_DAY * 3)
//			return "前天" + calendar.get(Calendar.HOUR_OF_DAY) + "点"
//					+ calendar.get(Calendar.MINUTE) + "分";
//		else if (ago <= ONE_MONTH) {
//			long day = ago / ONE_DAY;
//			return day + "天前" + calendar.get(Calendar.HOUR_OF_DAY) + "点"
//					+ calendar.get(Calendar.MINUTE) + "分";
//		} else if (ago <= ONE_YEAR) {
//			long month = ago / ONE_MONTH;
//			long day = ago % ONE_MONTH / ONE_DAY;
//			return month + "个月" + day + "天前"
//					+ calendar.get(Calendar.HOUR_OF_DAY) + "点"
//					+ calendar.get(Calendar.MINUTE) + "分";
//		} else {
//			long year = ago / ONE_YEAR;
//			int month = calendar.get(Calendar.MONTH) + 1;// JANUARY which is 0 so month+1
//			return year + "年前" + month + "月" + calendar.get(Calendar.DATE)
//					+ "日";
//		}
//
//	}
	
}
