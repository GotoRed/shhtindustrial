package com.awspaas.user.apps.shhtaerospaceindustrial.controller;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.SSOUtil;
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

public class CarInfoConsoleCmd {
	/**
	 * 调度查询订单与任务单
	 * @param uc
	 * @param roleType
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType 0：查看未派单订单 1：查看为接单任务单
	 * @return
	 */
	@Mapping("jch5.dispatchInfo")
	public String dispatchInfo(UserContext uc,int roleType,String bDate,String eDate,int page,int pageCount,int taskType) {
		JSONObject returnData = new JSONObject();
		String userId = uc.getUID();
		String sid = uc.getSessionId();
		String portalUrl = SDK.getPortalAPI().getPortalUrl();
		try {
			JSONArray jsonOrderUnassignedList= new JSONArray();
			JSONArray jsonMissionUntakedList = new JSONArray();
			
			String orderUnassignedQuery="select distinct a.BINDID,a.APPLYUSERNAME,a.CREATEDATE,a.BDATE,A.EDATE,A.BOARDINGPLACE,A.TARGETPLACE ，c.ID from BO_EU_SH_VEHICLEORDER a ， BO_EU_SH_VEHICLEORDER_ASSIGMIS  b，WFC_TASK c where a.bindid=b.bindid AND (b.MISSIONSTATUS='0' or b.MISSIONSTATUS='1') AND a.bindid=c.PROCESSINSTID";
			List<Map<String, Object>>orderUnassignedList = DBSql.query(orderUnassignedQuery, new ColumnMapRowMapper(), new Object[] {});
			//System.out.println(orderUnassignedList.size());
			for(int i =0; i<orderUnassignedList.size();i++) {
				
				
				Map<String, Object> order = orderUnassignedList.get(i);
				String orderid_bindid = CoreUtil.objToStr(order.get("BINDID"));
				//System.out.println(orderid_bindid);
				String applyusername = CoreUtil.objToStr(order.get("APPLYUSERNAME"));
				//System.out.println(applyusername);
				String ordertime = CoreUtil.objToStr(order.get("CREATEDATE"));
			//	System.out.println(ordertime);
				String bdate = CoreUtil.objToStr(order.get("BDATE"));
			//	System.out.println(bdate);
				String edate = CoreUtil.objToStr(order.get("EDATE"));
				//System.out.println(edate);
				String boardingplace=CoreUtil.objToStr(order.get("BOARDINGPLACE"));
			//	System.out.println(boardingplace);
				String targetplace=CoreUtil.objToStr(order.get("TARGETPLACE"));
				
				String orderTaskId = CoreUtil.objToStr(order.get("ID"));
				String orderFormUrl = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+orderid_bindid+"&openState=1&taskInstId="+orderTaskId+"&displayToolbar=true";
				System.out.println("订单id"+orderid_bindid);
				System.out.println("订单表单链接：");
				System.out.println(orderFormUrl);
				String missionUnassignedQuery="select a.bindid from BO_EU_SH_VEHICLEORDER a ， BO_EU_SH_VEHICLEORDER_ASSIGMIS b where a.bindid=b.bindid AND b.MISSIONSTATUS='0' AND  a.bindid='"+orderid_bindid+"'";
				List<Map<String, Object>> missionUnassignedList = DBSql.query(missionUnassignedQuery, new ColumnMapRowMapper(), new Object[] {});
				System.out.println("未派单任务单数量:"+missionUnassignedList.size());
				if(missionUnassignedList.size()>0) {
					JSONObject orderjson = new JSONObject();
					orderjson.put("orderid", orderid_bindid);
					orderjson.put("applyusername", applyusername);
					orderjson.put("ordertime", ordertime);
					orderjson.put("bdate", bdate);
					orderjson.put("edate", edate);
					orderjson.put("boardingplace", boardingplace);
					orderjson.put("targetplace", targetplace);
					orderjson.put("url", orderFormUrl);
					orderjson.put("undo", missionUnassignedList.size());
				}
				String missionUnfinishedQuery="select b.bindid, b.sjxm,b.sjlxfs,b.cph,b.missionstatus ,b.updatedate ,c.id  from BO_EU_SH_VEHICLEORDER a, BO_EU_SH_VEHICLEORDER_MISSION b ,WFC_TASK c where a.id=b.resourcetaskid and b.bindid=c.processinstid and a.bindid = '"+ orderid_bindid +"'";
				List<Map<String, Object>>missonUnfinishedList = DBSql.query(missionUnfinishedQuery, new ColumnMapRowMapper(), new Object[] {});
				System.out.println("未接单任务量："+missonUnfinishedList.size());
				for(int j=0; j<missonUnfinishedList.size();j++) {
					JSONObject missonJson = new JSONObject();
					Map<String, Object> mission = missonUnfinishedList.get(j);
					String  missionstatus=CoreUtil.objToStr(mission.get("MISSIONSTATUS"));
					//System.out.println(missionstatus);
					if("1".equals(missionstatus)) {//未接单任务单
						String processid= CoreUtil.objToStr(mission.get("BINDID"));
						String drivername = CoreUtil.objToStr(mission.get("SJXM"));
						String driverphone = CoreUtil.objToStr(mission.get("SJLXFS"));
						String carno = CoreUtil.objToStr(mission.get("CPH"));
						String usetime  = CoreUtil.objToStr(mission.get("UPDATEDATE"));
						String missionTaskId = CoreUtil.objToStr(mission.get("ID"));
						System.out.println("未接单行车任务单表单链接:");
						String missionFormUrl = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processid+"&openState=1&taskInstId="+missionTaskId+"&displayToolbar=true";
						System.out.println(missionFormUrl);
						/*if(drivername.equals("")) {
							drivername=CoreUtil.objToStr(mission.get("DIRVERNAMEOUT"));
							driverphone=CoreUtil.objToStr(mission.get("DIRVERPHONEOUT"));
							carno=CoreUtil.objToStr(mission.get("CARNOOUT"));
						}else {
							driverphone=CoreUtil.objToStr(mission.get("SJLXFS"));
							carno=CoreUtil.objToStr(mission.get("CPH"));
						}*/
						missonJson.put("orderid", orderid_bindid);
						missonJson.put("drivername", drivername);
						missonJson.put("driverphone", driverphone);
						missonJson.put("carno", carno);
						missonJson.put("usetime", usetime);
						missonJson.put("url", missionFormUrl);
						jsonMissionUntakedList.add(missonJson);
					}
				}

				
			}
			//System.out.println("End for loop");
			returnData.put("status", "0");
			returnData.put("unassignedOrder", jsonOrderUnassignedList);
			returnData.put("untakedMission", jsonMissionUntakedList);
			
		} catch(Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
		
	}
	/**
	 * @Description 普通用户获取已派未接单任务、接单进行中任务、结算待确认任务；驾驶员获取已派未确认任务、接单进行中任务、待结算确认任务；
	 * 结算员获取结算审核任务；
	 * @param uc
	 * @param roleType 角色类型（0：普通用户|1：驾驶员|5：车队结算员)
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType
	 * @return
	 */
	/*@Mapping("jch5.missionInfo")
	public String missionInfo(UserContext uc,int roleType,String bDate,String eDate,int page,int pageCount,int taskType) {
		JSONObject returnData = new JSONObject();
		String userId = uc.getUID();
		String sid = uc.getSessionId();
		String portalUrl = SDK.getPortalAPI().getPortalUrl();
		String missi
		try {
			
		} catch(Exception e) {}
		return returnData.toString();
	}*/
}
