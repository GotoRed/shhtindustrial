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
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class GetFoodOrderInfoController {
	
	/**
	 * 餐饮订单列表
	 * @param uc
	 * @param roleType
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType
	 * @return
	 */
	@Mapping("shsy.getFoodOrderList")
	public String getFoodOrderList(UserContext uc,String roleType,String bDate,String eDate,String page,String pageCount,String taskType) {
		JSONObject returnData = new JSONObject();
		int page1 = Integer.parseInt(page);
		int pageCount1 = Integer.parseInt(pageCount);
		
		try {
			String userId = uc.getUID();
//			userId = "fanzhenjie";//测试
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
			//代办
			String querySql1 = "select rownum rn,'canyin' ordertype,wfc.processinstid processinstid,fd.ordersel,fd.packagetype,item3.cnname packagestandard,item2.itemno areano,item2.cnname areaname," + 
			"fd.ORDERID,to_char(fd.ORDERDATE, 'YYYY-MM-dd') orderdate,fd.ycdate,fd.orderstatus,item1.cnname,wfc.processinstid wf_processinstid,wfc.id wf_id " +
			"from BO_EU_SH_FOODORDER fd " + 
			"left join BO_EU_SH_FOODROOMDOC fdd on fd.roomid=fdd.id " + 
			"left join WFC_TASK wfc on wfc.PROCESSINSTID=fd.bindid " + 
			"left join Bo_Act_Dict_Kv_Item item1 on fd.orderstatus=item1.itemno " +
			"left join BO_ACT_DICT_KV_MAIN main1 on item1.bindid=main1.bindid " +
			"left join Bo_Act_Dict_Kv_Item item2 on substr(fd.area,0,1)=item2.itemno " +//substr(fd.area,0,1)是因为控件问题，存储数据有问题
			"left join BO_ACT_DICT_KV_MAIN main2 on item2.bindid = main2.bindid " +
			"left join Bo_Act_Dict_Kv_Item item3 on fd.packagestandard=item3.itemno " +
			"left join BO_ACT_DICT_KV_MAIN main3 on item3.bindid=main3.bindid " +
			"where fd.orderstatus!=5 and fd.orderstatus!=6 and main1.dictkey ='shorderstatus' and main2.dictkey = 'shdininghallarea' and (main3.dictkey = 'dinnertype' or fd.packagestandard is null) " +//packagestandard可能非必填
			"and wfc.TARGET = '"+userId+"' AND wfc.DISPATCHID IS NOT NULL AND wfc.TASKTITLE NOT LIKE '%空标题%' ";
			//已办
			String querySql2 = "select rownum rn,'canyin' ordertype,wfh.processinstid processinstid,fd.ordersel,fd.packagetype,item3.cnname packagestandard,item2.itemno areano,item2.cnname areaname," + 
			"fd.ORDERID,to_char(fd.ORDERDATE, 'YYYY-MM-dd') orderdate,fd.ycdate,fd.orderstatus,item1.cnname,wfh.processinstid wf_processinstid,wfh.id wf_id " +
			"from BO_EU_SH_FOODORDER fd " + 
			"left join BO_EU_SH_FOODROOMDOC fdd on fd.roomid=fdd.id " + 
			"left join WFH_TASK wfh on wfh.PROCESSINSTID=fd.bindid " + 
			"left join Bo_Act_Dict_Kv_Item item1 on fd.orderstatus=item1.itemno " +
			"left join BO_ACT_DICT_KV_MAIN main1 on item1.bindid=main1.bindid " +
			"left join Bo_Act_Dict_Kv_Item item2 on substr(fd.area,0,1)=item2.itemno " +//substr(fd.area,0,1)是因为控件问题，存储数据有问题
			"left join BO_ACT_DICT_KV_MAIN main2 on item2.bindid = main2.bindid " +
			"left join Bo_Act_Dict_Kv_Item item3 on fd.packagestandard=item3.itemno " +
			"left join BO_ACT_DICT_KV_MAIN main3 on item3.bindid=main3.bindid " +
			"where fd.orderstatus!=5 and fd.orderstatus!=6 and main1.dictkey ='shorderstatus' and main2.dictkey = 'shdininghallarea' and (main3.dictkey = 'dinnertype' or fd.packagestandard is null) " +
			"and wfh.TARGET = '"+userId+"' AND wfh.DISPATCHID IS NOT NULL AND wfh.TASKTITLE NOT LIKE '%空标题%' ";
			if(!bDate.equals("") && !eDate.equals("")) {
				querySql1 = querySql1+"and (to_date(substr(fd.ycdate,1,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
				querySql2 = querySql2+"and (to_date(substr(fd.ycdate,1,10), 'YYYY/MM/DD') between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD'))";
			}
			if((roleType.equals("0")||roleType.equals("1"))&&taskType.equals("0")) {//普通用户,食堂调度查看0代办
				querySql0.append(querySql1);
			}else if((roleType.equals("0")||roleType.equals("1"))&&taskType.equals("1")) {//普通用户,食堂调度查看1全部
				querySql0.append("(").append(querySql2).append(") union (").append(querySql1).append(")");
			}else {
				returnData.put("status", "1");
				returnData.put("message", "输入参数不合法！");
				return returnData.toString();
			}
			String querySql = "select * from ("+querySql0+") where rn>="+start+" and rn<="+end+" order by ycdate desc ";
			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				returnData.put("status", "0");
				JSONArray orderNull = new JSONArray();
				returnData.put("orderList", orderNull);
				returnData.put("message", "没有任何订单记录");
				return returnData.toString();
			}
			JSONArray orderArr = new JSONArray();
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			for (Map<String, Object> dataMap : dataList) {
				JSONObject orderItem = new JSONObject();
				String orderType = CoreUtil.objToStr(dataMap.get("ordertype"));
				String orderId = CoreUtil.objToStr(dataMap.get("orderid"));
				String processinstId = CoreUtil.objToStr(dataMap.get("processinstid"));
				
				String areaNo = CoreUtil.objToStr(dataMap.get("areano"));
				String areaName = CoreUtil.objToStr(dataMap.get("areaname"));
				String orderDate = CoreUtil.objToStr(dataMap.get("orderdate"));
				String ycDate = CoreUtil.objToStr(dataMap.get("ycdate"));
				if(ycDate.length()>16) {
					ycDate = ycDate.substring(0, 16);
				}
				String orderstatus = CoreUtil.objToStr(dataMap.get("orderstatus"));
				String cnname = CoreUtil.objToStr(dataMap.get("cnname"));
				String orderSel = CoreUtil.objToStr(dataMap.get("ordersel"));
				String orderSelName = "未选择用餐时段";
				switch(orderSel) {
					case "0" : orderSelName="午餐";break;
					case "1" : orderSelName="晚餐";break;
					case "2" : orderSelName="两餐";break;
					default :break;
				}
				String packageType = CoreUtil.objToStr(dataMap.get("packagetype"));
				String packageStandard = CoreUtil.objToStr(dataMap.get("packagestandard"));
				String processInstId = CoreUtil.objToStr(dataMap.get("wf_processinstid"));//流程实例ID
				String taskInstId = CoreUtil.objToStr(dataMap.get("wf_id"));//任务实例Id
				String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
				
				orderItem.put("orderType", orderType);
				orderItem.put("orderId", orderId);
				orderItem.put("processinstId", processinstId);
				
				orderItem.put("areaNo", areaNo);
				orderItem.put("areaName", areaName);
				orderItem.put("orderDate", orderDate);
				orderItem.put("ycDate", ycDate);
				orderItem.put("orderStatus", orderstatus);
				orderItem.put("statusName", cnname);
				orderItem.put("orderSel", orderSelName);
				orderItem.put("packageType", packageType);
				orderItem.put("packageStandard", packageStandard);
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
	
	/**
	 * 酒店预订不成功信息
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.hotelOrderFailSMS")
	public String HotelOrderFailSMS(UserContext uc) {
		JSONObject returnData = new JSONObject();
		SmsUtil sms = new SmsUtil();
		String phone = "18217410090";
		String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_HOTEL_ORDER_FAIL_SMS_TEMPLATE_ID);
		String param = "{'APPLYNAME':'樊祯杰','CANCELREASON':'测试取消'}";
		try {
			returnData = sms.sendSms(phone,templateId,param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
	
	/**
	 * 酒店消息预订成功通知-单记录
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.hotelOrderSucessSMS")
	public String HotelOrderSucessSMS(UserContext uc) {
		JSONObject returnData = new JSONObject();
		SmsUtil sms = new SmsUtil();
		String phone = "15736950660";
		String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_HOTEL_ORDER_SUCESS_TEMPLATE_ID);
		String param = "{'APPLYNAME':'樊祯杰','BDATE':'2020-07-07','EDATE':'2020-07-08','ORDERNUM':'101','ROOMTYPE':'标准双床房','EATTINGSTARTDATE':'2020-07-08','ROOMNUM':'vip包厢','PERSONNUM':'2','PACKAGESTANDARD':'桌餐（vip接待）','MEETBDATE':'2020-07-08','MEETEDATE':'2020-07-08','MEETINGROOM':'vip会议室'}";
		try {
			returnData = sms.sendSms(phone,templateId,param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
	
	/**
	 * 车辆预订派单成功通知
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.vehicleDispatchSucessSMS")
	public String VehicleDispatchSucessSMS(UserContext uc) {
		JSONObject returnData = new JSONObject();
		SmsUtil sms = new SmsUtil();
		String phone = "18217410090";
		String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
		String param = "{'APPLYUSERNAME':'樊祯杰','UDATE':'2020-07-08','SJXM':'张春魁','SJLXFS':'18601731687','CPH':'沪A88888','VEHICLETYPE':'保时捷'}";
		try {
			returnData = sms.sendSms(phone,templateId,param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
	
	/**
	 * 食堂餐饮预订成功通知
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.canteenOrderSucessSMS")
	public String CanteenOrderSucessSMS(UserContext uc) {
		JSONObject returnData = new JSONObject();
		SmsUtil sms = new SmsUtil();
		String phone = "18217410090";
		String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS_TEMPLATE_ID);
		String param = "{'CONTACTPERSON':'樊祯杰','YCDATE':'2020-07-15','AREA':'八食堂','ROOMNUM':'202','PERSONNUM':'2','PACKAGESTANDARD':'桌餐（vip接待）','STPHONE':'18217410090'}";
		try {
			returnData = sms.sendSms(phone,templateId,param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
	
	/**
	 * 食堂餐饮预订不成功通知
	 * @param uc
	 * @return
	 */
	@Mapping("shsy.canteenOrderFailSMS")
	public String CanteenOrderFailSMS(UserContext uc) {
		JSONObject returnData = new JSONObject();
		SmsUtil sms = new SmsUtil();
		String phone = "18217410090";
		String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_FAIL_TEMPLATE_ID);
		String param = "{'CONTACTPERSON':'樊祯杰','YCDATE':'2020-07-15','AREA':'八食堂','ROOMNUM':'202'}";
		try {
			returnData = sms.sendSms(phone,templateId,param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
	
	
//	/**
//	 * 
//	 * @param uc
//	 * @param uid
//	 * @param oddate 年月
//	 * @param odtype 订单类型（餐饮/客房/……）
//	 * @return 我的订单列表
//	 */
//	@Mapping("shsy.getPersonlOrderList")
//	public String getOrderList(UserContext uc,String oddate,String odtype) {
//		String uid = uc.getUID();//登录人账号
//		oddate = oddate.replace('-', '/');
//		uid = "fanzhenjie";
//		JSONObject result = new JSONObject();
//		try {
//			String querySql = null;
//			//四种订单拼接
//			String querySql1 = "select 'canyin' type,fd.ROOMNUM itemname,fd.ORDERID,to_char(fd.ORDERDATE, 'YYYY-MM-dd') orderdate,fd.orderstatus,item1.cnname,fdd.price from BO_EU_SH_FOODORDER fd " + 
//					"left join BO_EU_SH_FOODROOMDOC fdd on fd.roomnum=fdd.roomno " + 
//					"left join Bo_Act_Dict_Kv_Item item1 on fd.orderstatus=item1.itemno " +
//					"left join BO_ACT_DICT_KV_MAIN main1 on item1.bindid=main1.bindid " +
//					"where fd.APPLYUID='"+uid+"' and to_char(fd.ORDERDATE,'YYYY/MM')='"+oddate+"' and main1.dictkey ='shorderstatus' ";
//			String querySql2 = "select 'huiyi' type,mer.roomname itemname,me.orderid,to_char(me.ORDERDATE, 'YYYY-MM-dd') orderdate,me.orderstatus,item2.cnname,mer.price from BO_EU_SH_MEETINGORDER me  " + 
//					"left join BO_EU_SH_MEETTINGROOM mer on me.MEETINGROOM=mer.ROOMNAME " + 
//					"left join Bo_Act_Dict_Kv_Item item2 on me.orderstatus=item2.itemno " +
//					"left join BO_ACT_DICT_KV_MAIN main2 on item2.bindid=main2.bindid " +
//					"where me.APPLYUID='"+uid+"' and to_char(me.ORDERDATE,'YYYY/MM')='"+oddate+"' and main2.dictkey ='shorderstatus' ";
//			String querySql3 = "select 'kefang' type,rt.ROOMTYPE itemname,rd.orderid,to_char(rd.ORDERDATE, 'YYYY-MM-dd') orderdate,rd.orderstatus,item3.cnname,rt.sdprince price from BO_EU_SH_ROOMORDER rd " + 
//					"left join BO_EU_SH_ROOMTYPE rt on rd.ROOMTYPE=rt.id " + 
//					"left join Bo_Act_Dict_Kv_Item item3 on rd.orderstatus=item3.itemno " +
//					"left join BO_ACT_DICT_KV_MAIN main3 on item3.bindid=main3.bindid " +
//					"where rd.APPLYUID='"+uid+"' and to_char(rd.ORDERDATE,'YYYY/MM')='"+oddate+"' and main3.dictkey ='shorderstatus' ";
//			String querySql4 = "select 'cheliang' type,vt.vehicletype itemname,vd.orderid,to_char(vd.ORDERDATE, 'YYYY-MM-dd') orderdate,vd.orderstatus,item4.cnname,vt.DAYPRICE price from BO_EU_SH_VEHICLEORDER vd " + 
//					"left join BO_EU_SH_VEHICLETYPE vt on vd.VEHICLETYPE=vt.id " + 
//					"left join Bo_Act_Dict_Kv_Item item4 on vd.orderstatus=item4.itemno " +
//					"left join BO_ACT_DICT_KV_MAIN main4 on item4.bindid=main4.bindid " +
//					"where vd.APPLYUID='"+uid+"' and to_char(vd.ORDERDATE,'YYYY/MM')='"+oddate+"' and main4.dictkey ='shorderstatus' ";
//			if(odtype!=null&&!odtype.equalsIgnoreCase("null")&&!odtype.isEmpty()) {
//				if(odtype.equals("canyin")) {
//					querySql = querySql1;
//				}else if(odtype.equals("huiyi")) {
//					querySql = querySql2;
//				}else if(odtype.equals("kefang")) {
//					querySql = querySql3;
//				}else if(odtype.equals("cheliang")) {
//					querySql = querySql4;
//				}else {
//					throw new Exception("odtype");
//				}
//			}else {
//				querySql = querySql1 + "union (" + querySql2 + ") union (" + querySql3 + ") union (" + querySql4 + ") ";
//			}
//			 
//			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList == null || dataList.isEmpty()) {
//				result.put("status", "0");
//				JSONArray orderNull = new JSONArray();
//				result.put("orderList", orderNull);
//				result.put("message", "没有任何订单记录");
//				return result.toString();
//			}
//			JSONArray orderArr = new JSONArray();
//			for (Map<String, Object> dataMap : dataList) {
//				JSONObject orderItem = new JSONObject();
//				String orderType = CoreUtil.objToStr(dataMap.get("type"));
//				String orderId = CoreUtil.objToStr(dataMap.get("orderid"));
//				String itemName = CoreUtil.objToStr(dataMap.get("itemname"));
//				String orderDate = CoreUtil.objToStr(dataMap.get("orderdate"));
//				String orderstatus = CoreUtil.objToStr(dataMap.get("orderstatus"));
//				String cnname = CoreUtil.objToStr(dataMap.get("cnname"));
//				String orderPrice = CoreUtil.objToStr(dataMap.get("price"));
//				
//				orderItem.put("orderType", orderType);
//				orderItem.put("orderId", orderId);
//				orderItem.put("itemName", itemName);
//				orderItem.put("orderDate", orderDate);
//				orderItem.put("orderStatus", orderstatus);
//				orderItem.put("statusName", cnname);
//				orderItem.put("orderPrice", orderPrice);
//				orderArr.add(orderItem);
//			}
//			// 成功
//			result.put("status", "0");
//			result.put("orderList", orderArr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result.put("status", "1");
//			result.put("message", e.getMessage());
//		}
//		return result.toString();
//	}
//	
//	
	//车辆订单列表
//	@Mapping("shsy.getVehicleService")
//	public String getVehicleService(UserContext uc,String oddate) {
//		String uid = uc.getUID();//登录人账号
//		oddate = oddate.replace('-', '/');
//		uid = "fanzhenjie";
//		JSONObject result = new JSONObject();
//		try {
//			//四种订单拼接
//			String querySql = "select 'cheliang' type,vt.vehicletype itemname,vd.orderid,to_char(vd.ORDERDATE, 'YYYY-MM-dd') orderdate,vd.orderstatus,item4.cnname,vt.DAYPRICE price " +
//					"from BO_EU_SH_VEHICLEORDER vd " + 
//					"left join BO_EU_SH_VEHICLETYPE vt on vd.VEHICLETYPE=vt.id " + 
//					"left join Bo_Act_Dict_Kv_Item item4 on vd.orderstatus=item4.itemno " +
//					"left join BO_ACT_DICT_KV_MAIN main4 on item4.bindid=main4.bindid " +
//					"where vd.司机字段='"+uid+"' and to_char(vd.ORDERDATE,'YYYY/MM')='"+oddate+"' and main4.dictkey ='shorderstatus' ";
//			 
//			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList == null || dataList.isEmpty()) {
//				result.put("status", "0");
//				JSONArray orderNull = new JSONArray();
//				result.put("orderList", orderNull);
//				result.put("message", "没有任何订单记录");
//				return result.toString();
//			}
//			JSONArray orderArr = new JSONArray();
//			for (Map<String, Object> dataMap : dataList) {
//				JSONObject orderItem = new JSONObject();
//				String orderType = CoreUtil.objToStr(dataMap.get("type"));
//				String orderId = CoreUtil.objToStr(dataMap.get("orderid"));
//				String itemName = CoreUtil.objToStr(dataMap.get("itemname"));
//				String orderDate = CoreUtil.objToStr(dataMap.get("orderdate"));
//				String orderstatus = CoreUtil.objToStr(dataMap.get("orderstatus"));
//				String cnname = CoreUtil.objToStr(dataMap.get("cnname"));
//				String orderPrice = CoreUtil.objToStr(dataMap.get("price"));
//				
//				orderItem.put("orderType", orderType);
//				orderItem.put("orderId", orderId);
//				orderItem.put("itemName", itemName);
//				orderItem.put("orderDate", orderDate);
//				orderItem.put("orderStatus", orderstatus);
//				orderItem.put("statusName", cnname);
//				orderItem.put("orderPrice", orderPrice);
//				orderArr.add(orderItem);
//			}
//			// 成功
//			result.put("status", "0");
//			result.put("orderList", orderArr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result.put("status", "1");
//			result.put("message", e.getMessage());
//		}
//		return result.toString();
//	}
	
	
}
