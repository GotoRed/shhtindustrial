package com.awspaass.user.apps.businesscar;


import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.SSOUtil;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.ProcessExecuteQuery;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class AppCmdBusinessUser {
	/**
	 * 用户获取进行中的行程任务单
	 * @param uc
	 * @param roleType
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_userGetMission")
	public String userGetMission(UserContext uc, int roleType, String bDate, String eDate, int page, int pageCount,
			int taskType) {
		JSONObject returnData = new JSONObject();
		try {
			String userId = uc.getUID();
			String sid = uc.getSessionId();
			if (page < 1) {
				returnData.put("status", "1");
				returnData.put("message", "请传入大于等于1的起始页！");
				return returnData.toString();
			}
			// 获取起始条数和结束条数
			int start = (page - 1) * pageCount + 1;
			int end = page * pageCount;
			String timePeriod = "";// 行车确认是否查全部
			if (!bDate.equals("") && !eDate.equals("")) {
				timePeriod = "AND B.USEDATE >= TO_DATE('" + bDate + "','yyyy-MM-dd') AND B.USEDATE <= TO_DATE('" + eDate
						+ "','yyyy-MM-dd')";
			}
			String missionQuerySql = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT (CASE WHEN B.SJLXFS IS NOT NULL THEN CONCAT("
					+ "CONCAT(CONCAT(B.SJXM,'('),B.SJLXFS),')') ELSE CONCAT(B.SJXM,B.SJLXFS) END) SJXX,B.USEDATE,B.CFSJ,B.FHSJ,B.TOTALMONEY,B.VEHICLETYPE,B.CPH,"
					+ "(CASE WHEN B.MISSIONSTATUS=0 THEN '未派单' WHEN B.MISSIONSTATUS=1 THEN '已派单' WHEN B.MISSIONSTATUS=2 THEN '已接单' WHEN B.MISSIONSTATUS"
					+ "=3 THEN '待结算' WHEN B.MISSIONSTATUS=4 THEN '待确认' WHEN B.MISSIONSTATUS=5 THEN '已确认' WHEN B.MISSIONSTATUS=6 THEN '已取消' ELSE '' END) MISSIONSTATUS,A.PROCESSINSTID,A.ID,'1' RWZT FROM BO_EU_YBBZUSECAR_MISSION B LEFT JOIN WFC_TASK A ON "
					+ "A.PROCESSINSTID = B.BINDID WHERE B.APPLYUID = '" + userId + "' " + timePeriod
					+ " AND B.MISSIONSTATUS IN (1,2,3,4) AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%'"
					+ " ORDER BY B.USEDATE) TT WHERE ROWNUM <= " + end + ") table_alias WHERE table_alias.rowno >= "
					+ start + "";
			System.out.println(missionQuerySql);
			
			
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			JSONArray jsonMission = new JSONArray();
			
			if (!missionQuerySql.equals("")) {
				List<Map<String, Object>> missionInfoList = DBSql.query(missionQuerySql,
						new ColumnMapRowMapper(), new Object[] {});
				if (missionInfoList == null || missionInfoList.isEmpty()) {
					returnData.put("status", "0");
					returnData.put("jsonMission", jsonMission);
				} else {
					for (int i = 0; i < missionInfoList.size(); i++) {
						JSONObject jsonMissionOBJ = new JSONObject();
						Map<String, Object> xcqrMap = missionInfoList.get(i);
						String udate = CoreUtil.objToStr(xcqrMap.get("USEDATE"));
						String driverInfo = CoreUtil.objToStr(xcqrMap.get("SJXX"));// 司机姓名（联系方式）
						String startTime = CoreUtil.objToStr(xcqrMap.get("CFSJ"));// 出车时间
						String returnTime = CoreUtil.objToStr(xcqrMap.get("FHSJ"));// 返回时间
						//String sjjd = CoreUtil.objToStr(xcqrMap.get("ORDERDATE"));// 司机接单
						if (!cfsj.equals("")) {
							cfsj = cfsj.substring(0, 16);
						}
						if (!fhsj.equals("")) {
							fhsj = fhsj.substring(0, 16);
						}
						/*
						if (!sjjd.equals("")) {
							sjjd = sjjd.substring(0, 16);
						}
						*/
						String carNo = CoreUtil.objToStr(xcqrMap.get("CPH"));
						//String fyzj = CoreUtil.objToStr(xcqrMap.get("TOTALMONEY"));// 费用总计
						String carType = CoreUtil.objToStr(xcqrMap.get("VEHICLETYPE"));// 车型
						String missionStatus = CoreUtil.objToStr(xcqrMap.get("MISSIONSTATUS"));// 状态
						String processInstId = CoreUtil.objToStr(xcqrMap.get("PROCESSINSTID"));// 流程实例ID
						String taskInstId = CoreUtil.objToStr(xcqrMap.get("ID"));// 任务实例Id
						//String rwzt = CoreUtil.objToStr(xcqrMap.get("RWZT"));// 1：待办|2：已办
						// https://www.ht804dzs.cn/portal/r/w?sid=c82f49da-e292-44d5-9a63-87d1c72db466&
						// cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=b3cddb87-ebf4-4546-82ee-6d24543079c0&openState=1
						// &taskInstId=ff8ea21c-ec24-42fa-8293-6a8a53f459de&displayToolbar=true
						String url = portalUrl + "/r/w?sid=" + sid
								+ "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=" + processInstId
								+ "&openState=1&taskInstId=" + taskInstId + "&displayToolbar=true";
						jsonMissionOBJ.put("driverInfo", driverInfo);
						jsonMissionOBJ.put("startTime", startTime);
						jsonMissionOBJ.put("returnTime", returnTime);
						//jsonXcqrObj.put("sjjd", sjjd);
						//jsonXcqrObj.put("fyzj", fyzj);
						carType = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype",
								carType, "CNNAME");
						jsonMissionOBJ.put("carType", carType);
						jsonMissionOBJ.put("missionStatus", missionStatus);
						//jsonXcqrObj.put("rwzt", rwzt);
						jsonMissionOBJ.put("processInstId", processInstId);
						jsonMissionOBJ.put("url", url);
						jsonMissionOBJ.put("udate",udate);
						jsonMission.add(jsonMissionOBJ);
					}
				}
			}
		}catch (Exception e) {
			
		}
		return returnData.toString();
	}
	
	/**
	 * 用户取消出车前30分钟的任务单
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_userCancelMission")
	public String userCancelMission(String ids, String processInstId, String id, UserContext uc) {
		JSONObject returnData = new JSONObject();
		return returnData.toString();
	}
	
}
