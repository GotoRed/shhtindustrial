package com.awspaass.user.apps.tempcar;

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
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class AppUserCmd {
	/***
	 * Descript:用户查询行车任务单cmd,操作页签调用
	 * 
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
		if(taskType==0) {
			

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
				String xcqr = "";// 行车确认是否查全部
				if (!bDate.equals("") && !eDate.equals("")) {
					xcqr = "AND B.UDATE >= TO_DATE('" + bDate + "','yyyy-MM-dd') AND B.UDATE <= TO_DATE('" + eDate
							+ "','yyyy-MM-dd')";
				}
				String queryXcqrTaskInfo = "";

				queryXcqrTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT (CASE WHEN B.SJLXFS IS NOT NULL THEN CONCAT("
						+ "CONCAT(CONCAT(B.SJXM,'('),B.SJLXFS),')') ELSE CONCAT(B.SJXM,B.SJLXFS) END) SJXX,B.UDATE,B.CFSJ,B.FHSJ,B.TOTALMONEY,B.VEHICLETYPE,"
						+ "(CASE WHEN B.MISSIONSTATUS=0 THEN '未派单' WHEN B.MISSIONSTATUS=1 THEN '已派单' WHEN B.MISSIONSTATUS=2 THEN '已接单' WHEN B.MISSIONSTATUS"
						+ "=3 THEN '待结算' WHEN B.MISSIONSTATUS=4 THEN '待确认' WHEN B.MISSIONSTATUS=5 THEN '已确认' WHEN B.MISSIONSTATUS=6 THEN '已取消' ELSE '' END) MISSIONSTATUS,A.PROCESSINSTID,A.ID,'1' RWZT,B.ORDERDATE FROM BO_EU_SH_VEHICLEORDER_MISSION B LEFT JOIN WFC_TASK A ON "
						+ "A.PROCESSINSTID = B.BINDID WHERE B.APPLYUID = '" + userId + "' " + xcqr
						+ " AND B.MISSIONSTATUS IN (1,2,3,4) AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%'"
						+ " ORDER BY B.UDATE) TT WHERE ROWNUM <= " + end + ") table_alias WHERE table_alias.rowno >= "
						+ start + "";

				System.out.println("####TaskType==0########queryXcqrTaskInfo");
				System.out.println(queryXcqrTaskInfo);

				String portalUrl = SDK.getPortalAPI().getPortalUrl();// http://localhost:8088/portal
				JSONArray jsonXcqr = new JSONArray();

				if (!queryXcqrTaskInfo.equals("")) {
					List<Map<String, Object>> xcqrTaskInfoList = DBSql.query(queryXcqrTaskInfo, new ColumnMapRowMapper(),
							new Object[] {});
					if (xcqrTaskInfoList == null || xcqrTaskInfoList.isEmpty()) {
						returnData.put("status", "0");
						returnData.put("jsonXcqr", jsonXcqr);
					} else {
						for (int i = 0; i < xcqrTaskInfoList.size(); i++) {
							JSONObject jsonXcqrObj = new JSONObject();
							Map<String, Object> xcqrMap = xcqrTaskInfoList.get(i);
							String udate = CoreUtil.objToStr(xcqrMap.get("UDATE"));
							String sjxx = CoreUtil.objToStr(xcqrMap.get("SJXX"));// 司机姓名（联系方式）
							String cfsj = CoreUtil.objToStr(xcqrMap.get("CFSJ"));// 出车时间
							String fhsj = CoreUtil.objToStr(xcqrMap.get("FHSJ"));// 返回时间
							String sjjd = CoreUtil.objToStr(xcqrMap.get("ORDERDATE"));// 司机接单
							if (!cfsj.equals("")) {
								cfsj = cfsj.substring(0, 16);
							}
							if (!fhsj.equals("")) {
								fhsj = fhsj.substring(0, 16);
							}
							if (!sjjd.equals("")) {
								sjjd = sjjd.substring(0, 16);
							}
							String fyzj = CoreUtil.objToStr(xcqrMap.get("TOTALMONEY"));// 费用总计
							String cx = CoreUtil.objToStr(xcqrMap.get("VEHICLETYPE"));// 车型
							String zt = CoreUtil.objToStr(xcqrMap.get("MISSIONSTATUS"));// 状态
							String processInstId = CoreUtil.objToStr(xcqrMap.get("PROCESSINSTID"));// 流程实例ID
							String taskInstId = CoreUtil.objToStr(xcqrMap.get("ID"));// 任务实例Id
							String rwzt = CoreUtil.objToStr(xcqrMap.get("RWZT"));// 1：待办|2：已办
							// https://www.ht804dzs.cn/portal/r/w?sid=c82f49da-e292-44d5-9a63-87d1c72db466&
							// cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=b3cddb87-ebf4-4546-82ee-6d24543079c0&openState=1
							// &taskInstId=ff8ea21c-ec24-42fa-8293-6a8a53f459de&displayToolbar=true
							String url = portalUrl + "/r/w?sid=" + sid
									+ "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=" + processInstId
									+ "&openState=1&taskInstId=" + taskInstId + "&displayToolbar=true";
							jsonXcqrObj.put("sjxx", sjxx);
							jsonXcqrObj.put("cfsj", cfsj);
							jsonXcqrObj.put("fhsj", fhsj);
							jsonXcqrObj.put("sjjd", sjjd);
							jsonXcqrObj.put("fyzj", fyzj);
							cx = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype", cx,
									"CNNAME");
							jsonXcqrObj.put("cx", cx);
							jsonXcqrObj.put("zt", zt);
							jsonXcqrObj.put("rwzt", rwzt);
							jsonXcqrObj.put("processInstId", processInstId);
							jsonXcqrObj.put("url", url);
							jsonXcqrObj.put("udate", udate);
							jsonXcqr.add(jsonXcqrObj);
						}
					}
				}

				returnData.put("status", "0");
				returnData.put("jsonXcqr", jsonXcqr);
				returnData.put("jsonClyd", "");
			} catch (Exception e) {
				e.printStackTrace();
				returnData.put("status", "1");
				returnData.put("message", e.getMessage());
			}
			
		}else if (taskType==1) {
			

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

				String queryClydTaskInfo = "";
				List<Map<String, Object>> clydTaskInfoList = null;
				String clyd = "";// 车辆预定是否查全部
				if (!bDate.equals("") && !eDate.equals("")) {
					clyd = "AND B.ORDERDATE >= TO_DATE('" + bDate + "','yyyy-MM-dd') AND B.ORDERDATE <= TO_DATE('" + eDate
							+ "','yyyy-MM-dd')";
				}

				queryClydTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT * FROM (SELECT B.APPLYUSERNAME,B.ORDERID,B.VEHICLETYPE,B.ORDERDATE,B.BDATE,B.EDATE,B.VEHICLENUM,B.ORDERSTATUS,B.BINDID,A.ID,A.BEGINTIME,'1' RWZT"
						+ " FROM BO_EU_SH_VEHICLEORDER B LEFT JOIN WFC_TASK A ON A.PROCESSINSTID = B.BINDID WHERE B.APPLYUID = '"
						+ userId + "' " + clyd + " "
						+ "AND B.ISEND!='1' AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%') C ORDER BY C.BDATE DESC) TT WHERE ROWNUM <= "
						+ end + ") table_alias WHERE table_alias.rowno >= " + start + "";
				System.out.println("######ROLETYPE 其他角色待办需要看车辆预定queryClydTaskInfo ");
				System.out.println(queryClydTaskInfo);
				clydTaskInfoList = DBSql.query(queryClydTaskInfo, new ColumnMapRowMapper(), new Object[] {});

				String portalUrl = SDK.getPortalAPI().getPortalUrl();// http://localhost:8088/portal

				JSONArray jsonClyd = new JSONArray();

				if (clydTaskInfoList == null || clydTaskInfoList.isEmpty()) {
					returnData.put("status", "0");
					returnData.put("jsonClyd", jsonClyd);
				} else {
					for (int i = 0; i < clydTaskInfoList.size(); i++) {
						JSONObject jsonClydObj = new JSONObject();
						Map<String, Object> clydMap = clydTaskInfoList.get(i);
						String cx = CoreUtil.objToStr(clydMap.get("VEHICLETYPE"));// 车型
						String ydsj = CoreUtil.objToStr(clydMap.get("ORDERDATE"));// 预定时间
						String ydkssj = CoreUtil.objToStr(clydMap.get("BDATE"));// 预定开始时间
						String ydjssj = CoreUtil.objToStr(clydMap.get("EDATE"));// 预定结束时间
						String orderid = CoreUtil.objToStr(clydMap.get("ORDERID"));
						String applyuser = CoreUtil.objToStr(clydMap.get("APPLYUSERNAME"));

						if (!ydsj.equals("")) {
							ydsj = ydsj.substring(0, 10);
						}
						if (!ydkssj.equals("")) {
							ydkssj = ydkssj.substring(0, 11);
						}
						if (!ydjssj.equals("")) {
							ydjssj = ydjssj.substring(0, 11);
						}
						String ycsl = CoreUtil.objToStr(clydMap.get("VEHICLENUM"));// 用车数量
						String zt = CoreUtil.objToStr(clydMap.get("ORDERSTATUS"));// 状态
						String processInstId = CoreUtil.objToStr(clydMap.get("BINDID"));// 流程实例ID
						String taskInstId = CoreUtil.objToStr(clydMap.get("ID"));// 任务实例Id
						String rwzt = CoreUtil.objToStr(clydMap.get("RWZT"));// 1：待办|2：已办
						String url = portalUrl + "/r/w?sid=" + sid + "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="
								+ processInstId + "&openState=1&taskInstId=" + taskInstId + "&displayToolbar=true";
						cx = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype", cx,
								"CNNAME");
						jsonClydObj.put("cx", cx);
						jsonClydObj.put("ydsj", ydsj);
						jsonClydObj.put("ydkssj", ydkssj);
						jsonClydObj.put("ydjssj", ydjssj);
						jsonClydObj.put("ycsl", ycsl);
						jsonClydObj.put("orderid", orderid);
						jsonClydObj.put("applyuser", applyuser);
						zt = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shorderstatus", zt,
								"CNNAME");
						jsonClydObj.put("zt", zt);
						jsonClydObj.put("processInstId", processInstId);
						jsonClydObj.put("url", url);
						jsonClydObj.put("rwzt", rwzt);
						jsonClyd.add(jsonClydObj);
					}
				}
				returnData.put("status", "0");

				returnData.put("jsonClyd", jsonClyd);
				returnData.put("jsonXcqr", "");
				
			} catch (Exception e) {
				e.printStackTrace();
				returnData.put("status", "1");
				returnData.put("message", e.getMessage());
			}
			
		}
		return returnData.toString();
	}
	/***
	 * Descript:用户查询订单cmd,操作页签调用
	 * 
	 * @param uc
	 * @param roleType
	 * @param bDate
	 * @param eDate
	 * @param page
	 * @param pageCount
	 * @param taskType
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_userGetOrder")
	public String userGetOrder(UserContext uc, int roleType, String bDate, String eDate, int page, int pageCount,
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

			String queryClydTaskInfo = "";
			List<Map<String, Object>> clydTaskInfoList = null;
			String clyd = "";// 车辆预定是否查全部
			if (!bDate.equals("") && !eDate.equals("")) {
				clyd = "AND B.ORDERDATE >= TO_DATE('" + bDate + "','yyyy-MM-dd') AND B.ORDERDATE <= TO_DATE('" + eDate
						+ "','yyyy-MM-dd')";
			}

			queryClydTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT * FROM (SELECT B.APPLYUSERNAME,B.ORDERID,B.VEHICLETYPE,B.ORDERDATE,B.BDATE,B.EDATE,B.VEHICLENUM,B.ORDERSTATUS,B.BINDID,A.ID,A.BEGINTIME,'1' RWZT"
					+ " FROM BO_EU_SH_VEHICLEORDER B LEFT JOIN WFC_TASK A ON A.PROCESSINSTID = B.BINDID WHERE B.APPLYUID = '"
					+ userId + "' " + clyd + " "
					+ "AND B.ISEND!='1' AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%') C ORDER BY C.BDATE DESC) TT WHERE ROWNUM <= "
					+ end + ") table_alias WHERE table_alias.rowno >= " + start + "";
			System.out.println("######ROLETYPE 其他角色待办需要看车辆预定queryClydTaskInfo ");
			System.out.println(queryClydTaskInfo);
			clydTaskInfoList = DBSql.query(queryClydTaskInfo, new ColumnMapRowMapper(), new Object[] {});

			String portalUrl = SDK.getPortalAPI().getPortalUrl();// http://localhost:8088/portal

			JSONArray jsonClyd = new JSONArray();

			if (clydTaskInfoList == null || clydTaskInfoList.isEmpty()) {
				returnData.put("status", "0");
				returnData.put("jsonClyd", jsonClyd);
			} else {
				for (int i = 0; i < clydTaskInfoList.size(); i++) {
					JSONObject jsonClydObj = new JSONObject();
					Map<String, Object> clydMap = clydTaskInfoList.get(i);
					String cx = CoreUtil.objToStr(clydMap.get("VEHICLETYPE"));// 车型
					String ydsj = CoreUtil.objToStr(clydMap.get("ORDERDATE"));// 预定时间
					String ydkssj = CoreUtil.objToStr(clydMap.get("BDATE"));// 预定开始时间
					String ydjssj = CoreUtil.objToStr(clydMap.get("EDATE"));// 预定结束时间
					String orderid = CoreUtil.objToStr(clydMap.get("ORDERID"));
					String applyuser = CoreUtil.objToStr(clydMap.get("APPLYUSERNAME"));

					if (!ydsj.equals("")) {
						ydsj = ydsj.substring(0, 10);
					}
					if (!ydkssj.equals("")) {
						ydkssj = ydkssj.substring(0, 11);
					}
					if (!ydjssj.equals("")) {
						ydjssj = ydjssj.substring(0, 11);
					}
					String ycsl = CoreUtil.objToStr(clydMap.get("VEHICLENUM"));// 用车数量
					String zt = CoreUtil.objToStr(clydMap.get("ORDERSTATUS"));// 状态
					String processInstId = CoreUtil.objToStr(clydMap.get("BINDID"));// 流程实例ID
					String taskInstId = CoreUtil.objToStr(clydMap.get("ID"));// 任务实例Id
					String rwzt = CoreUtil.objToStr(clydMap.get("RWZT"));// 1：待办|2：已办
					String url = portalUrl + "/r/w?sid=" + sid + "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="
							+ processInstId + "&openState=1&taskInstId=" + taskInstId + "&displayToolbar=true";
					cx = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype", cx,
							"CNNAME");
					jsonClydObj.put("cx", cx);
					jsonClydObj.put("ydsj", ydsj);
					jsonClydObj.put("ydkssj", ydkssj);
					jsonClydObj.put("ydjssj", ydjssj);
					jsonClydObj.put("ycsl", ycsl);
					jsonClydObj.put("orderid", orderid);
					jsonClydObj.put("applyuser", applyuser);
					zt = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shorderstatus", zt,
							"CNNAME");
					jsonClydObj.put("zt", zt);
					jsonClydObj.put("processInstId", processInstId);
					jsonClydObj.put("url", url);
					jsonClydObj.put("rwzt", rwzt);
					jsonClyd.add(jsonClydObj);
				}
			}
			returnData.put("status", "0");

			returnData.put("jsonClyd", jsonClyd);
			returnData.put("jsonXcqr", "");
			
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}

	
	  @Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_userCancelMission") 
	  public String userCancelMission(String ids,String processInstId,String id,UserContext uc) {
		  JSONObject returnData = new JSONObject();
		  String userid = uc.getUID();
		  
		  String queryInfoSql = "select a.RESOURCETASKFPID,a.UDATE, a.CPH,a.SJXM, a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION a WHERE BINDID = "
		  		+ "'"+processInstId+"'";
		  String BINDID = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "BINDID"));
		  String APPLYUSERNAME = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "APPLYUSERNAME"));//预定人姓名
		  String APPLYUSERCELLPHONE = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "APPLYUSERCELLPHONE"));
		  String SJXM = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "SJXM"));
		  String SJLXFS = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "SJLXFS"));
		  String CONTACTPERSON = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "CONTACTPERSON"));
		  String CONTACTPHONE = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "CONTACTPHONE"));
		  String UDATE = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "UDATE"));
		  String CPH = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "CPH"));
		  String RESOURCETASKFPID = CoreUtil.objToStr(DBSql.getString(queryInfoSql, "RESOURCETASKFPID"));
		  String message_user = "{'applyUserName':'"+APPLYUSERNAME+"','udate':'"+UDATE+"','cph':'"+CPH+"'}";
		  String message_driver ="{'applyUserName':'"+SJXM+"','udate':'"+UDATE+"','cph':'"+CPH+"'}";
		  SmsUtil sms = new SmsUtil();
		  
		  String updateAssignSql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set ZT='2',MISSIONSTATUS='6' where id ='"+RESOURCETASKFPID+"'";
		  String updateMissionSql = "update BO_EU_SH_VEHICLEORDER_MISSION set MISSIONSTATUS='6' where bindid='"+BINDID+"'";
		  System.out.println(updateAssignSql);
		  DBSql.update(updateAssignSql);//修改上航_车辆任务分配状态
		  DBSql.update(updateMissionSql);
		  try {
				System.out.println("准备终止流程！流程号:"+BINDID+"用户ID:"+userid);
				SDK.getProcessAPI().terminateById(BINDID, userid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		  try {
			  	sms.sendSms(APPLYUSERCELLPHONE,"SMS_228016523",message_user);
				sms.sendSms(SJLXFS, "SMS_228116397", message_driver);
				sms.sendSms("13918947832", "SMS_228116397", message_driver);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		  
		  if(!CONTACTPERSON.equals("")) {
			  message_user  = "{'applyUserName':'"+CONTACTPERSON+"','udate':'"+UDATE+"','cph':'"+CPH+"'}";
			  
			  try {
				  sms.sendSms(CONTACTPHONE,"SMS_228016523",message_user);
				} catch (Exception e) {
					e.printStackTrace();
				}
		  }

		 
		  returnData.put("status", "0");
		returnData.put("message", "取消成功！");
		return returnData.toString();
	  }
	  

	  
}
