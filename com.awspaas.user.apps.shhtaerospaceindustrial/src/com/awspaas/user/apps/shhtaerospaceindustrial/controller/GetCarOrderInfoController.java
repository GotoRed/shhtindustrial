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

@Controller
public class GetCarOrderInfoController {
	
	
	
	
	
	
	/**
	 * @Description 入参：角色类型（0：普通用户|1：驾驶员|2：车队|3：外租公司|4：车辆调度|5：车队结算员|6:：客服）、开始日期、结束日期、页数、每页数量、任务类型（0：待办|1：全部）
	 *	 出参：状态（0：成功|1：失败）、信息提示（失败时携带）、车辆订单列表【{车型、预定时间、用车开始时间、用车结束时间、用车数量、状态（未提交、已提交、已结算、已取消、已接单）、链接地址、流程实例序号}】
	 * 	注意：主流程状态：未提交、已提交、已派单、外租派单、已取消 子流程状态：行程已提交、行程已核实、行程待修改、结算已确认、行程待确认、外租已派单
	 *	 普通用户：车辆预定（只查业务表）、车辆行程单（任务表关联业务表）；其他角色（司机和调度）：车辆预定（任务表关联业务表）、车辆行程单（任务表关联业务表）
	 *	任务类型（0：待办|1：全部） 任务类型为0查行车确认单待办任务，任务类型为1查订单和行车确认单任务
	 * @author WU LiHua
	 * @date 2020年6月17日 上午15:26:42
	 */
	@Mapping("jch5.kq_getCarOrderInfo")
	public String getLoginUserInfo(UserContext uc,int roleType,String bDate,String eDate,int page,int pageCount,int taskType) {
    	JSONObject returnData = new JSONObject();
    	
		try {
			String userId = uc.getUID();
			String sid = uc.getSessionId();
			if(page<1) {
				returnData.put("status", "1");
				returnData.put("message", "请传入大于等于1的起始页！");
				return returnData.toString();
			}
			//获取起始条数和结束条数
			int start = (page-1)*pageCount+1;
			int end = page*pageCount;
			String xcqr = "";//行车确认是否查全部
			if(!bDate.equals("") && !eDate.equals("")) {
				xcqr = "AND B.UDATE >= TO_DATE('"+bDate+"','yyyy-MM-dd') AND B.UDATE <= TO_DATE('"+eDate+"','yyyy-MM-dd')";
			}
			String queryXcqrTaskInfo = "";
			String queryClydTaskInfo = "";
			List<Map<String, Object>> clydTaskInfoList = null;
			String clyd = "";//车辆预定是否查全部
			if(!bDate.equals("") && !eDate.equals("")) {
				clyd = "AND B.ORDERDATE >= TO_DATE('"+bDate+"','yyyy-MM-dd') AND B.ORDERDATE <= TO_DATE('"+eDate+"','yyyy-MM-dd')";
			}
			if(taskType == 0) {//普通用户、客服待办看行程确认单，如果是其他角色的待办需要看行程确认单
				//查看行车确认单待办任务情况
				queryXcqrTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT (CASE WHEN B.SJLXFS IS NOT NULL THEN CONCAT("
						+ "CONCAT(CONCAT(B.SJXM,'('),B.SJLXFS),')') ELSE CONCAT(B.SJXM,B.SJLXFS) END) SJXX,B.CFSJ,B.FHSJ,B.TOTALMONEY,B.VEHICLETYPE,"
						+ "(CASE WHEN B.MISSIONSTATUS=0 THEN '未派单' WHEN B.MISSIONSTATUS=1 THEN '已派单' WHEN B.MISSIONSTATUS=2 THEN '已接单' WHEN B.MISSIONSTATUS"
						+ "=3 THEN '待结算' WHEN B.MISSIONSTATUS=4 THEN '待确认' WHEN B.MISSIONSTATUS=5 THEN '已确认' WHEN B.MISSIONSTATUS=6 THEN '已取消' ELSE '' END) MISSIONSTATUS,A.PROCESSINSTID,A.ID,'1' RWZT,B.ORDERDATE FROM BO_EU_SH_VEHICLEORDER_MISSION B LEFT JOIN WFC_TASK A ON "
						+ "A.PROCESSINSTID = B.BINDID WHERE A.TARGET = '"+userId+"' "+xcqr+" AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%'"
						+ " ORDER BY B.UDATE) TT WHERE ROWNUM <= "+end+") table_alias WHERE table_alias.rowno >= "+start+"";
				
				
				System.out.println("####TaskType==0########queryXcqrTaskInfo");
				System.out.println(queryXcqrTaskInfo);
				
			}
			if((roleType == 1 || roleType == 2 || roleType == 3 || roleType == 4 || roleType == 5) && taskType == 0) {//其他角色待办需要看车辆预定
				queryClydTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT * FROM (SELECT B.VEHICLETYPE,B.ORDERDATE,B.BDATE,B.EDATE,B.VEHICLENUM,B.ORDERSTATUS,B.BINDID,A.ID,A.BEGINTIME,'1' RWZT"
						+ " FROM BO_EU_SH_VEHICLEORDER B LEFT JOIN WFC_TASK A ON A.PROCESSINSTID = B.BINDID WHERE A.TARGET = '"+userId+"' "+clyd+" "
						+ "AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%') C ORDER BY C.BDATE DESC) TT WHERE ROWNUM <= "+end+") table_alias WHERE table_alias.rowno >= "+start+"";
				System.out.println("######ROLETYPE 其他角色待办需要看车辆预定queryClydTaskInfo ");
				System.out.println(queryClydTaskInfo);
				clydTaskInfoList = DBSql.query(queryClydTaskInfo, new ColumnMapRowMapper(), new Object[] {});
			}
			if(taskType == 1) {//普通用户、客服已办看全部，其他角色已办看全部
				queryClydTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT * FROM (SELECT B.VEHICLETYPE,B.ORDERDATE,B.BDATE,B.EDATE,B.VEHICLENUM,B.ORDERSTATUS,B.BINDID,A.ID,A.BEGINTIME,'1' RWZT"
						+ " FROM BO_EU_SH_VEHICLEORDER B LEFT JOIN WFC_TASK A ON A.PROCESSINSTID = B.BINDID WHERE A.TARGET = '"+userId+"' "+clyd+" "
						+ "AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%' UNION SELECT B.VEHICLETYPE,B.ORDERDATE,"
						+ "B.BDATE,B.EDATE,B.VEHICLENUM,B.ORDERSTATUS,B.BINDID,A.ID,A.BEGINTIME,'2' RWZT FROM BO_EU_SH_VEHICLEORDER B "
						+ "LEFT JOIN WFH_TASK A ON A.PROCESSINSTID = B.BINDID WHERE A.TARGET = '"+userId+"' "+clyd+" AND A.DISPATCHID IS NOT NULL "
						+ "AND A.TASKTITLE NOT LIKE '%空标题%') C ORDER BY C.BDATE DESC) TT WHERE ROWNUM <= "+end+") table_alias WHERE table_alias.rowno >= "+start+"";
				clydTaskInfoList = DBSql.query(queryClydTaskInfo, new ColumnMapRowMapper(), new Object[] {});
				queryXcqrTaskInfo = "SELECT * FROM (SELECT TT.*, ROWNUM AS rowno FROM (SELECT A.BEGINTIME,(CASE WHEN B.SJLXFS IS NOT "
						+ "NULL THEN CONCAT(CONCAT(CONCAT(B.SJXM,'('),B.SJLXFS),')') ELSE CONCAT(B.SJXM,B.SJLXFS) END) SJXX,B.CFSJ,"
						+ "B.FHSJ,B.TOTALMONEY,B.VEHICLETYPE,(CASE WHEN B.MISSIONSTATUS=0 THEN '未派单' WHEN B.MISSIONSTATUS=1 THEN '已派单' WHEN B.MISSIONSTATUS=2 THEN '已接单' WHEN B.MISSIONSTATUS" + 
						"=3 THEN '待结算' WHEN B.MISSIONSTATUS=4 THEN '待确认' WHEN B.MISSIONSTATUS=5 THEN '已确认' WHEN B.MISSIONSTATUS=6 THEN '已取消' ELSE '' END) MISSIONSTATUS,A.PROCESSINSTID,"
						+ "A.ID,'1' RWZT,B.ORDERDATE,B.UDATE FROM BO_EU_SH_VEHICLEORDER_MISSION B LEFT JOIN WFC_TASK A ON A.PROCESSINSTID = B.BINDID WHERE A.TARGET"
						+ " = '"+userId+"' "+xcqr+" AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%' UNION SELECT A.BEGINTIME,"
						+ "(CASE WHEN B.SJLXFS IS NOT NULL THEN CONCAT(CONCAT(CONCAT(B.SJXM,'('),B.SJLXFS),')') ELSE CONCAT(B.SJXM,B.SJLXFS) "
						+ "END) SJXX,B.CFSJ,B.FHSJ,B.TOTALMONEY,B.VEHICLETYPE,(CASE WHEN B.MISSIONSTATUS=0 THEN '未派单' WHEN B.MISSIONSTATUS=1 THEN '已派单' WHEN B.MISSIONSTATUS=2 THEN '已接单' WHEN B.MISSIONSTATUS" + 
						"=3 THEN '待结算' WHEN B.MISSIONSTATUS=4 THEN '待确认' WHEN B.MISSIONSTATUS=5 THEN '已确认' WHEN B.MISSIONSTATUS=6 THEN '已取消' ELSE '' END) MISSIONSTATUS,A.PROCESSINSTID,"
						+ "A.ID,'2' RWZT,B.ORDERDATE,B.UDATE FROM BO_EU_SH_VEHICLEORDER_MISSION B LEFT JOIN WFH_TASK A ON A.PROCESSINSTID = B.BINDID WHERE A.TARGET = '"+userId+"'"
						+ " "+xcqr+" AND A.DISPATCHID IS NOT NULL AND A.TASKTITLE NOT LIKE '%空标题%' ORDER BY UDATE) TT WHERE ROWNUM <="
						+ " "+end+") table_alias WHERE table_alias.rowno >= "+start+"";
				System.out.println("####TaskType==1########queryClydTaskInfo");
				System.out.println(queryClydTaskInfo);
				System.out.println("####TaskType==1########queryXcqrTaskInfo");
				System.out.println(queryClydTaskInfo);
			}

			String portalUrl = SDK.getPortalAPI().getPortalUrl();//http://localhost:8088/portal
			JSONArray jsonXcqr = new JSONArray();
			JSONArray jsonClyd = new JSONArray();
			if(!queryXcqrTaskInfo.equals("")) {
				List<Map<String, Object>> xcqrTaskInfoList = DBSql.query(queryXcqrTaskInfo, new ColumnMapRowMapper(), new Object[] {});
				if(xcqrTaskInfoList == null || xcqrTaskInfoList.isEmpty()) {
					returnData.put("status", "0");
					returnData.put("jsonXcqr", jsonXcqr);
				}else {
					for (int i = 0; i < xcqrTaskInfoList.size(); i++) {
						JSONObject jsonXcqrObj = new JSONObject();
						Map<String, Object> xcqrMap = xcqrTaskInfoList.get(i);
						String sjxx = CoreUtil.objToStr(xcqrMap.get("SJXX"));//司机姓名（联系方式）
						String cfsj = CoreUtil.objToStr(xcqrMap.get("CFSJ"));//出车时间
						String fhsj = CoreUtil.objToStr(xcqrMap.get("FHSJ"));//返回时间
						String sjjd = CoreUtil.objToStr(xcqrMap.get("ORDERDATE"));//司机接单
						if(!cfsj.equals("")) {
							cfsj = cfsj.substring(0, 16);
						}
						if(!fhsj.equals("")) {
							fhsj = fhsj.substring(0, 16);
						}
						if(!sjjd.equals("")) {
							sjjd = sjjd.substring(0, 16);
						}
						String fyzj = CoreUtil.objToStr(xcqrMap.get("TOTALMONEY"));//费用总计
						String cx = CoreUtil.objToStr(xcqrMap.get("VEHICLETYPE"));//车型
						String zt = CoreUtil.objToStr(xcqrMap.get("MISSIONSTATUS"));//状态
						String processInstId = CoreUtil.objToStr(xcqrMap.get("PROCESSINSTID"));//流程实例ID
						String taskInstId = CoreUtil.objToStr(xcqrMap.get("ID"));//任务实例Id
						String rwzt = CoreUtil.objToStr(xcqrMap.get("RWZT"));//1：待办|2：已办
						//https://www.ht804dzs.cn/portal/r/w?sid=c82f49da-e292-44d5-9a63-87d1c72db466&
						//cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=b3cddb87-ebf4-4546-82ee-6d24543079c0&openState=1
						//&taskInstId=ff8ea21c-ec24-42fa-8293-6a8a53f459de&displayToolbar=true
						String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
						jsonXcqrObj.put("sjxx", sjxx);
						jsonXcqrObj.put("cfsj", cfsj);
						jsonXcqrObj.put("fhsj", fhsj);
						jsonXcqrObj.put("sjjd", sjjd);
						jsonXcqrObj.put("fyzj", fyzj);
						cx = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype",cx, "CNNAME");
						jsonXcqrObj.put("cx", cx);
						jsonXcqrObj.put("zt", zt);
						jsonXcqrObj.put("rwzt", rwzt);
						jsonXcqrObj.put("processInstId", processInstId);
						jsonXcqrObj.put("url", url);
						jsonXcqr.add(jsonXcqrObj);
					}
				}
			}
			if(clydTaskInfoList == null || clydTaskInfoList.isEmpty()) {
				returnData.put("status", "0");
				returnData.put("jsonClyd", jsonClyd);
			}else {
				for (int i = 0; i < clydTaskInfoList.size(); i++) {
					JSONObject jsonClydObj = new JSONObject();
					Map<String, Object> clydMap = clydTaskInfoList.get(i);
					String cx = CoreUtil.objToStr(clydMap.get("VEHICLETYPE"));//车型
					String ydsj = CoreUtil.objToStr(clydMap.get("ORDERDATE"));//预定时间
					String ydkssj = CoreUtil.objToStr(clydMap.get("BDATE"));//预定开始时间
					String ydjssj = CoreUtil.objToStr(clydMap.get("EDATE"));//预定结束时间
					if(!ydsj.equals("")) {
						ydsj = ydsj.substring(0, 10);
					}
					if(!ydkssj.equals("")) {
						ydkssj = ydkssj.substring(0, 16);
					}
					if(!ydjssj.equals("")) {
						ydjssj = ydjssj.substring(0, 16);
					}
					String ycsl = CoreUtil.objToStr(clydMap.get("VEHICLENUM"));//用车数量
					String zt = CoreUtil.objToStr(clydMap.get("ORDERSTATUS"));//状态
					String processInstId = CoreUtil.objToStr(clydMap.get("BINDID"));//流程实例ID
					String taskInstId = CoreUtil.objToStr(clydMap.get("ID"));//任务实例Id
					String rwzt = CoreUtil.objToStr(clydMap.get("RWZT"));//1：待办|2：已办
					String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
					cx = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shcartype",cx, "CNNAME");
					jsonClydObj.put("cx", cx);
					jsonClydObj.put("ydsj", ydsj);
					jsonClydObj.put("ydkssj", ydkssj);
					jsonClydObj.put("ydjssj", ydjssj);
					jsonClydObj.put("ycsl", ycsl);
					zt = SDK.getDictAPI().getValue("com.awspaas.user.apps.shhtaerospaceindustrial", "shorderstatus",zt, "CNNAME");
					jsonClydObj.put("zt", zt);
					jsonClydObj.put("processInstId", processInstId);
					jsonClydObj.put("url", url);
					jsonClydObj.put("rwzt", rwzt);
					jsonClyd.add(jsonClydObj);
				}
			}
			returnData.put("status", "0");
			returnData.put("jsonXcqr", jsonXcqr);
			returnData.put("jsonClyd", jsonClyd);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
	
	/**
	 * @Description 车辆派单接口启动内租或者外租流程 ->入参：任务派单子表ID和车辆属性（0:内租|1:外租）、流程实例ID|出参：状态（0：成功|1：失败）、信息提示（失败时携带）
	 * 	创建内租/外租流程及数据，并启动，扭转到下个节点
	 * @ids 任务派单子表记录数据信息集合
	 * @ids 任务派单子表记录id集合  'b33e8aeb-0573-4f3b-9504-427de5d46424','caed1e04-4a3c-4d06-93e9-7d156a5ad691'
	 * @processInstId 流程实例ID
	 * @author WU LiHua 
	 * @date 2020年6月18日 上午13:38:42
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_pdApply")
	public String bqApply(String ids,String processInstId,String id,UserContext uc) {
    	JSONObject returnData = new JSONObject();
    	System.out.println("front args:"+ids);
		try {
			String userId = uc.getUID();
			String queryCount = "SELECT COUNT(1) SL FROM BO_EU_SH_VEHICLEORDER_ASSIGMIS WHERE BINDID = '"+processInstId+"' AND ID IN ("+id+") AND ZT <> '0'";
			int sl = CoreUtil.objToInt(DBSql.getInt(queryCount, "SL"));
			if(sl > 0) {
				returnData.put("status", "1");
				returnData.put("message", "您勾选的数据中存在重复派单！");
				return returnData.toString();
			}
			if(!id.equals("")) {//如果ID已经派单了，不能重复派单
				String[] splitIds = id.split(",");
				for (int i = 0; i < splitIds.length; i++) {
					String splitId = splitIds[i];
					String queryId = "SELECT COUNT(1) SL FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE RESOURCETASKFPID = "+splitId+"";
					int idSl = CoreUtil.objToInt(DBSql.getInt(queryId, "SL"));
					if(idSl > 0) {
						returnData.put("status", "1");
						returnData.put("message", "您勾选的数据中存在重复派单！");
						return returnData.toString();
					}
				}
			}
			String queryUseCarInfo = "SELECT APPLYUSERNAME,APPLYUID,APPLYUSERCELLPHONE,CONTACTPERSON,CONTACTUNIT,CONTACTUNITID,CONTACTDEPT,"
									+ "CONTACTDEPTID,CONTACTPERSON,CONTACTPERSONZH,BDATE,CONTACTPHONE,ID,BOARDINGPLACE,TARGETPLACE,APPLYUNIT,APPLYUNITID,APPLYDEPTNAME,APPLYDEPTID,ISOUTSHANGHAI  "
									+ "FROM BO_EU_SH_VEHICLEORDER WHERE BINDID = '"+processInstId+"'";
			List<Map<String, Object>> queryUserCarInfoList = DBSql.query(queryUseCarInfo, new ColumnMapRowMapper(), new Object[] {});
			String contactPerson = "";
			String applyUserName = "";
			String applyUserId = "";
			String applyUserPhone = "";
			String contactUserName = "";
			String contactUnit = "";
			String contactUnitId = "";
			String contactDept = "";
			String contactDeptId = "";
			String contactPhone = "";
			String contactUserId = "";
			

			
			String jlId = "";
			String scdd = "";
			String mdd = "";
			String sfcs = "";
			String bdate="";
			SSOUtil ssoUtil = new SSOUtil();
			if(queryUserCarInfoList != null && !queryUserCarInfoList.isEmpty()) {//如果车辆预定表中没有数据
				Map<String, Object> userCarInfoMap = queryUserCarInfoList.get(0);
				contactPerson = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPERSON"));//用车联系人
				applyUserName = CoreUtil.objToStr(userCarInfoMap.get("APPLYUSERNAME"));//预定人
				applyUserId = CoreUtil.objToStr(userCarInfoMap.get("APPLYUID"));//预定人账号
				applyUserPhone = CoreUtil.objToStr(userCarInfoMap.get("APPLYUSERCELLPHONE"));//预定人手机
				contactUnit = CoreUtil.objToStr(userCarInfoMap.get("APPLYUNIT"));//预定单位
				contactUnitId = CoreUtil.objToStr(userCarInfoMap.get("APPLYUNITID"));//预定单位ID
				contactDept = CoreUtil.objToStr(userCarInfoMap.get("APPLYDEPTNAME"));//预定部门
				contactDeptId = CoreUtil.objToStr(userCarInfoMap.get("APPLYDEPTID"));//预定部门ID
				contactUserName = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPERSON"));//用车联系人
				contactUserId = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPERSONZH"));//用车联系人账号
				contactPhone = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPHONE"));//用车联系人手机
				jlId = CoreUtil.objToStr(userCarInfoMap.get("ID"));//车辆预定表ID
				scdd = CoreUtil.objToStr(userCarInfoMap.get("BOARDINGPLACE"));//上车地点
				mdd = CoreUtil.objToStr(userCarInfoMap.get("TARGETPLACE"));//目的地
				sfcs = CoreUtil.objToStr(userCarInfoMap.get("ISOUTSHANGHAI"));//是否出省
				bdate = CoreUtil.objToStr(userCarInfoMap.get("BDATE"));//是否出省
			}
			String carType = "";
			String udate = "";
			String vehicleType = "";
			String cph = "";
			String sjxm = "";
			String sjzh = "";
			String sjlxfs = "";
			String sjxmNew = "";
			String sjzhNew = "";
			String sjlxfsNew = "";
			String idNew = "";
			String clppgg = "";
			String drivernameout="";
			String driverphoneout="";
			String carnoout="";
			String udate_n = "";
			if(!ids.equals("")) {//ids ->  b33e8aeb-0573-4f3b-9504-427de5d46424:0:2020-06-17,caed1e04-4a3c-4d06-93e9-7d156a5ad691:1:2020-06-20
				String[] idsArr = ids.split(",");
				for (int i = 0; i < idsArr.length; i++) {
					String idsStr = idsArr[i];
					String[] idsStrI = idsStr.split(":");
					int length = idsStrI.length;//传来的字段长度
					if(length>=1) {
						idNew = idsStrI[0];//任务派单子表ID
					}
					if(length>=2) {
						carType = idsStrI[1];//车辆属性（0:内租|1:外租）
					}
					if(length>=3) {
						udate = idsStrI[2];//使用日期
					}
					if(length>=4) {
						vehicleType = idsStrI[3];//车辆类型
					}
					if(length>=5) {
						cph = idsStrI[4];//车牌号
					}
					if(length>=6) {
						sjxm = idsStrI[5];//司机姓名
					}
					if(length>=7) {
						sjzh = idsStrI[6];//司机账号
					}
					if(length>=8) {
						sjlxfs = idsStrI[7];//司机联系方式
					}
					if(length>=9) {
						sjxmNew = idsStrI[8];//外租公司调度
					}
					if(length>=10) {
						sjzhNew = idsStrI[9];//外租公司调度账号
					}
					if(length>=11) {
						sjlxfsNew = idsStrI[10];//外租公司调度联系方式
					}
					if(length>=12) {
						clppgg = idsStrI[11];//车辆品牌规格
					}
					if(length>=13) {
						drivernameout=idsStrI[12];
					}
					if(length>=14) {
						driverphoneout=idsStrI[13];
					}
					if(length>=15) {
						carnoout=idsStrI[14];
					}
					Date bdate_f = (Date)new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bdate);
			        Date udate_f = (Date)new SimpleDateFormat("yyyy-MM-dd").parse(udate);
			        if(bdate_f.getDate()<udate_f.getDate()) {
			        	udate_n = udate+" 8:00";
			        }else {
			        	udate_n=bdate;
			        }
			        
			        System.out.println("#########外租字段"+drivernameout+driverphoneout+carnoout);
					String processDefId = "";//流程定义ID
					String title = "";//流程标题
					String orderId = "";//订单号
					if(carType.equals("0")) {//内租
						processDefId = "obj_d951639b5cf447d592ea82551b884081";
						title = "内租";
						orderId = "NZ";
						if(sjzh.equals("")) {//如果内租司机为空
							returnData.put("status", "1");
							returnData.put("message", "未正确发起派车流程，请确认司机姓名是否选择！");
							return returnData.toString();
						}
						if(cph.equals("")) {//如果车牌号为空
							returnData.put("status", "1");
							returnData.put("message", "未正确发起派车流程，请确认车牌号是否选择！");
							return returnData.toString();
						}
					}else if(carType.equals("1")){//外租
						processDefId = "obj_5b822a42fa7540948190da32038878dc";
						title = "外租";
						orderId = "WZ";
						sjxm = drivernameout;
						sjzh = "";
						sjlxfs = driverphoneout;
						if(drivernameout.equals("")) {//如果外租司机为空
							returnData.put("status", "1");
							returnData.put("message", "未正确发起派车流程，请确认外租公司调度是否选择！");
							return returnData.toString();
						}
					}else {
						returnData.put("status", "1");
						returnData.put("message", "未正确发起派车流程，请确认您的车辆属性是否选择！");
						return returnData.toString();
					}
					ProcessInstance createProcessInstance;
					if(carType.equals("0")) {
						createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, sjzh, title+"-"+udate_n+"-"+contactPerson);
					}else {
						createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, "A755C03FB0B1471CE053F401A8C0C17FmB0", title+"-"+udate_n+"-"+contactPerson);
					}
					//1、创建流程实例
					//ProcessInstance createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, "A755C03FB0B1471CE053F401A8C0C17FmB0", title+"-"+udate_n+"-"+contactPerson);
					//2、为创建的流程实例写入Bo数据
					BO boRecordData = new BO();
					boRecordData.set("ORDERID", orderId+SDK.getRuleAPI().executeAtScript("@replace(@date,-)")+SDK.getRuleAPI().executeAtScript("@sequence('"+orderId+"AA@year@month',4,0)"));//订单号
					boRecordData.set("APPLYUSERNAME", applyUserName);//预定人
					boRecordData.set("APPLYUID", applyUserId);//预定人账号
					boRecordData.set("APPLYUSERCELLPHONE", applyUserPhone);//预定人手机
					boRecordData.set("APPLYUNIT", contactUnit);//用车单位
					boRecordData.set("APPLYUNITID", contactUnitId);//用车单位ID
					boRecordData.set("CONTACTPERSON", contactPerson);//用车联系人
					boRecordData.set("CONTACTPERSONZH", contactUserId);//用车联系人账号
					boRecordData.set("CONTACTPHONE", contactPhone);//用车联系人手机
					boRecordData.set("UDATE", udate_n);//使用日期
					boRecordData.set("VEHICLETYPE", vehicleType);//车辆类型
					
					//String sid = "";
					if(carType.equals("1")) {
						//boRecordData.set("SJXM", "");//司机姓名
						//boRecordData.set("SJZH", "");//司机账号
						//boRecordData.set("SJLXFS", "");//司机联系方式
						//boRecordData.set("CPH", "");//车牌号
						//sid = ssoUtil.registerClientSessionNoPassword(sjzhNew, "cn", "", "mobile");
						//boRecordData.set("DIRVERNAMEOUT",drivernameout);
						//boRecordData.set("DIRVERPHONEOUT",driverphoneout);
						//boRecordData.set("CARNOOUT",carnoout);
						boRecordData.set("SJZH","A755C03FB0B1471CE053F401A8C0C17FmB0");
						boRecordData.set("SJXM", drivernameout);
						boRecordData.set("SJLXFS", driverphoneout);
						boRecordData.set("CPH", carnoout);
					}else {
						boRecordData.set("SJXM", sjxm);//司机姓名
						boRecordData.set("SJZH", sjzh);//司机账号
						boRecordData.set("SJLXFS", sjlxfs);//司机联系方式
						boRecordData.set("CPH", cph);//车牌号
						//根据文件创建者创建sid，formFile.getCreateUser()为userId
						//sid = ssoUtil.registerClientSessionNoPassword(sjzh, "cn", "", "mobile");
					}
					boRecordData.set("WZUNITPSN", sjxmNew);//外租公司调度
					boRecordData.set("WZUNITPSNID", sjzhNew);//外租公司调度ID
					boRecordData.set("WZUNITPSNPHONE", sjlxfsNew);//外租公司联系方式
					boRecordData.set("RESOURCETASKID", jlId);//来源预订单ID
					boRecordData.set("RESOURCETASKFPID", idNew);//来源任务分配单ID
					boRecordData.set("ISINTERNALVEHICLE", carType);//车辆属性
					boRecordData.set("APPLYDEPTNAME", contactDept);//用车部门
					boRecordData.set("APPLYDEPTID", contactDeptId);//用车部门ID
					boRecordData.set("VEHICLELABELNAME", clppgg);//车辆品牌
					boRecordData.set("BOARDINGPLACE", scdd);//上车地点
					boRecordData.set("TARGETPLACE", mdd);//目的地
					boRecordData.set("ISOUTSHANGHAI", sfcs);//是否出省
					SDK.getBOAPI().create(CoreUtil.MISSION, boRecordData, createProcessInstance, UserContext.fromUID(userId));
					//3、启动创建的流程
					SDK.getProcessAPI().start(createProcessInstance);
					DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET ZT = '1' WHERE ID = '"+idNew+"'");
					DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '1' WHERE ID = '"+idNew+"'");
					String message = "";//发送给司机
					
					String param_to_user="";
					String param_to_driver="";
					String message_to_user="";
					String message_to_driver="";
					
					String driver_name="";
					String driver_phone="";
					String car_use="";
					
					SmsUtil sms = new SmsUtil();
					String templateId_user = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
					String templateId_driver = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID);

					
					
					if(carType.equals("0")) {
						driver_name=sjxm;
						driver_phone=sjlxfs;
						car_use=cph;
						param_to_user = "{'APPLYUSERNAME':'"+applyUserName+"','UDATE':'"+udate_n+"','SJXM':'"+sjxm+"','SJLXFS':'"+sjlxfs
								+"','CPH':'"+cph+"','VEHICLETYPE':'"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"'}";
						param_to_driver = "{'SJXM':'"+sjxm+"','APPLYUSERNAME':'"+applyUserName+"','APPLYUSERCELLPHONE':'"+applyUserPhone+"','UDATE':'"+udate_n
								+"','CPH':'"+cph+"','BOARDINGPLACE':'"+scdd+"','TARGETPLACE':'"+mdd+"'}";
						
						message_to_user=applyUserName+ "您好！您已成功预定"+udate_n+"的车辆，为您服务的司机为"+sjxm+"联系方式："+sjlxfs+"车辆为"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"（"+cph+"）";//发送给用户
						message_to_driver=udate_n+"日的用车人是["+contactPerson+contactPhone+"]的任务已到达，车牌号为："+cph+"],请确认！";
						
						
					} else if(carType.equals("1")) {
						driver_name=drivernameout;
						driver_phone=driverphoneout;
						car_use=carnoout;
						param_to_user = "{'APPLYUSERNAME':'"+applyUserName+"','UDATE':'"+udate_n+"','SJXM':'"+drivernameout+"','SJLXFS':'"+driverphoneout
								+"','CPH':'"+carnoout+"','VEHICLETYPE':'"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"'}";
						
						param_to_driver = "{'SJXM':'"+drivernameout+"','APPLYUSERNAME':'"+applyUserName+"','APPLYUSERCELLPHONE':'"+applyUserPhone+"','UDATE':'"+udate_n
								+"','CPH':'"+carnoout+"','BOARDINGPLACE':'"+scdd+"','TARGETPLACE':'"+mdd+"'}";
						
						message_to_user=applyUserName+ "您好！您已成功预定"+udate_n+"的车辆，为您服务的司机为"+sjxm+"联系方式："+sjlxfs+"车辆为"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"（"+cph+"）";//发送给用户
						message_to_driver=udate_n+"日的用车人是["+contactPerson+contactPhone+"]的任务已到达，车牌号为："+cph+"],请确认！";
						
					}
					
					/**
					 * 给订车人、用车人、派遣司机发送短信
					 */
					if(!applyUserPhone.equals("")) {
						try {
							returnData = sms.sendSms(applyUserPhone,templateId_user,param_to_user);
							System.out.println("车辆预定成功后给用户发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(!contactPhone.equals("")) {
						try {
							returnData = sms.sendSms(contactPhone,templateId_user,param_to_user);
							System.out.println("车辆预定成功后给用车联系人发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(!driver_phone.equals("")) {
						try {
							returnData = sms.sendSms(sjlxfs,templateId_driver,param_to_driver);
							System.out.println("内租车辆预定成功后给司机发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					/**
					 * 给内部用车司机、用户推送通知
					 */	
					if(carType.equals("0")) {
						MsgNoticeController.sendNoticeMsg(UserContext.fromUID(userId), param_to_driver, userId, sjzh, "1", "");
					}
					MsgNoticeController.sendNoticeMsg(UserContext.fromUID(userId), message_to_user, userId, applyUserId, "1", "");
					
					/*
					if(!applyUserPhone.equals("")) {
						String phone = applyUserPhone;
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
						String param = "{'APPLYUSERNAME':'"+applyUserName+"','UDATE':'"+udate_n+"','SJXM':'"+sjxm+"','SJLXFS':'"+sjlxfs
										+"','CPH':'"+cph+"','VEHICLETYPE':'"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"'}";
						try {
							returnData = sms.sendSms(phone,templateId,param);
							System.out.println("车辆预定成功后给用户发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(!contactPhone.equals("")) {//如果用车联系人手机不为空
						String phone = contactPhone;
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
						String param = "{'APPLYUSERNAME':'"+contactUserName+"','UDATE':'"+udate_n+"','SJXM':'"+sjxm+"','SJLXFS':'"+sjlxfs
										+"','CPH':'"+cph+"','VEHICLETYPE':'"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+"'}";
						try {
							returnData = sms.sendSms(phone,templateId,param);
							System.out.println("车辆预定成功后给用车联系人发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(carType.equals("0") && !sjlxfs.equals("")) {//司机联系方式
						String phone = sjlxfs;
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID);
						String param = "{'SJXM':'"+sjxm+"','APPLYUSERNAME':'"+applyUserName+"','APPLYUSERCELLPHONE':'"+applyUserPhone+"','UDATE':'"+udate_n
										+"','CPH':'"+cph+"','BOARDINGPLACE':'"+scdd+"','TARGETPLACE':'"+mdd+"'}";
						try {
							returnData = sms.sendSms(phone,templateId,param);
							System.out.println("内租车辆预定成功后给司机发送短信消息======="+returnData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}*/
					
					/*
					if(!sjlxfs.equals("")) {
						sjlxfs = "("+sjlxfs+")";
					}
					if(!contactPhone.equals("")) {
						contactPhone = "("+contactPhone+")";
					}
					if(!cph.equals("")) {
						cph = "("+cph+")";
					}
					String content = applyUserName+ "您好！您已成功预定"+udate_n+"的车辆，为您服务的司机为"+sjxm+sjlxfs+"车辆为"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+cph;//发送给用户
					if(carType.equals("0")) {
						message = udate_n+"日的用车人是["+contactPerson+contactPhone+"]的任务已到达，车牌号为："+cph+"],请确认！";
						MsgNoticeController.sendNoticeMsg(UserContext.fromUID(userId), message, userId, sjzh, "1", "");
						MsgNoticeController.sendNoticeMsg(UserContext.fromUID(userId), content, userId, applyUserId, "1", "");
					}
					*/
					//String portalUrl = SDK.getPortalAPI().getPortalUrl();//http://localhost:8088/portal
					//String url = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=2&taskInstId="+taskInstId+"&displayToolbar=true";
				}
				returnData.put("status", "0");
			}else {
				returnData.put("status", "1");
				returnData.put("message", "未正确发起派车流程，请联系管理员！");
				return returnData.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
	
	/**
	 * @Desc 获取基本租车费，超时费，超公里费接口
	 * @param uc
	 * @param cx 车型
	 * @param clpp 车辆品牌
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getJsjfy")
	public static String sendNoticeMsg(UserContext uc, String cx,String clpp) {
		JSONObject result = new JSONObject();
		try {
			String queryjjFy = "SELECT HONGQIAO,PUDONG,DAYPRICE,DAYOVERKILOMETERSPRICE,DAYOVERTIMEPRICE FROM BO_EU_SH_VEHICLETYPE WHERE "
								+ "VEHICLETYPE = '"+cx+"' AND VEHICLELABELNAME = '"+clpp+"'";//查询虹桥机场、浦东机场费用、临时服务费、超公里费、超时费
			List<Map<String, Object>> yjjFyList = DBSql.query(queryjjFy, new ColumnMapRowMapper(), new Object[] {});
			if(yjjFyList == null || yjjFyList.isEmpty()) {
				result.put("hq", "");
				result.put("pd", "");
				result.put("jbzcf", "");
				result.put("cglf", "");
				result.put("csf", "");
				result.put("status", "0");
				return result.toString();
			}
			for (int i = 0; i < yjjFyList.size(); i++) {
				String hq = CoreUtil.objToStr(DBSql.getString(queryjjFy, "HONGQIAO"));//虹桥机场接送
				String pd = CoreUtil.objToStr(DBSql.getString(queryjjFy, "PUDONG"));//浦东机场接送
				String jbzcf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYPRICE"));//临时服务费
				String cglf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYOVERKILOMETERSPRICE"));//超公里费
				String csf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYOVERTIMEPRICE"));//超时费
				result.put("hq", hq);
				result.put("pd", pd);
				result.put("jbzcf", jbzcf);
				result.put("cglf", cglf);
				result.put("csf", csf);
			}
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	@Mapping("com.awspaass.user.apps.shhtaerospaceindustrial_vehicleNotice")
	public static boolean vehicleNotice(UserContext uc, String odType, String processInstId, String revocationReason) {
		//JSONObject result = new JSONObject();
		String userID = uc.getUID();
		String opType="";
		String reason="";
		if(odType.equals("1")){
			opType = "取消";
			reason = "取消原因："+revocationReason;
		}else if(odType.equals("0")){
			opType = "变更";
			reason = "变更原因: "+revocationReason;
		}
		try {
			String queryOrderInfo = "select * from Bo_Eu_Sh_Vehicleorder_Mission  WHERE Bo_Eu_Sh_Vehicleorder_Mission.Bindid ='" + processInstId + "'";
			List<Map<String, Object>> OrderInfoList = DBSql.query(queryOrderInfo, new ColumnMapRowMapper(), new Object[] {});
			if(OrderInfoList == null ||OrderInfoList.isEmpty() ) {
				// result.put("orderid","")
				// result.put("contact","");
				// result.put("phone","");
				// result.put("drivername","");
				// result.put("driverphone","");
				return false;
			}
			
			
			Map<String, Object> orDerInfo  = OrderInfoList.get(0);
			String orDerId = CoreUtil.objToStr(orDerInfo.get("ORDERID"));
			String contact = CoreUtil.objToStr(orDerInfo.get("CONTACTPERSON"));
			String phone = CoreUtil.objToStr(orDerInfo.get("CONTACTPHONE"));
			String driverName = CoreUtil.objToStr(orDerInfo.get("SJXM"));
			String driverPhone = CoreUtil.objToStr(orDerInfo.get("SJLXFS"));

			String queryDispatcherInfo = "select * from ORGUSER LEFT JOIN ORGROLE on ORGROLE.ID= ORGUSER.Roleid where ORGROLE.ROLENAME='车辆调度'";
			List<Map<String, Object>> dispacherInfoList = DBSql.query(queryDispatcherInfo, new ColumnMapRowMapper(), new Object[] {});
			if(dispacherInfoList == null || dispacherInfoList.isEmpty()) {
				return false;
			}
			for(int i =0; i<dispacherInfoList.size(); i++) {
				Map<String, Object> dispacerInfo= dispacherInfoList.get(i);
				String dispacherID = CoreUtil.objToStr(dispacerInfo.get("USERID"));
				String messageContent = "订单号： " + orDerId + " " + opType + reason + "，用车人： " + contact + "手机号: " + phone + ", 司机姓名: " + driverName + "手机号： " + driverPhone;
				MsgNoticeController.sendNoticeMsg(uc, messageContent, userID, dispacherID, "1","");
				
			}

			
			
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_gerCarBaseInfo")
	public static String sendNoticeMsg(UserContext uc, String cx,String clpp,String isInternal) {
		JSONObject result = new JSONObject();
		try {
			if(isInternal.equals("1")) {
				String queryjjFy = "SELECT HONGQIAO,PUDONG,DAYPRICE,DAYOVERKILOMETERSPRICE,DAYOVERTIMEPRICE FROM BO_EU_SH_VEHICLETYPE WHERE "
						+ "VEHICLETYPE = '"+cx+"' AND ISBZ = '是'";//查询虹桥机场、浦东机场费用、临时服务费、超公里费、超时费
			}else {
				String queryjjFy = "SELECT HONGQIAO,PUDONG,DAYPRICE,DAYOVERKILOMETERSPRICE,DAYOVERTIMEPRICE FROM BO_EU_SH_VEHICLETYPE WHERE "
						+ "VEHICLETYPE = '"+cx+"' AND VEHICLELABELNAME = '"+clpp+"'";//查询虹桥机场、浦东机场费用、临时服务费、超公里费、超时费
			}
			String queryjjFy = "SELECT HONGQIAO,PUDONG,DAYPRICE,DAYOVERKILOMETERSPRICE,DAYOVERTIMEPRICE FROM BO_EU_SH_VEHICLETYPE WHERE "
								+ "VEHICLETYPE = '"+cx+"' AND VEHICLELABELNAME = '"+clpp+"'";//查询虹桥机场、浦东机场费用、临时服务费、超公里费、超时费
			List<Map<String, Object>> yjjFyList = DBSql.query(queryjjFy, new ColumnMapRowMapper(), new Object[] {});
			if(yjjFyList == null || yjjFyList.isEmpty()) {
				result.put("hq", "");
				result.put("pd", "");
				result.put("jbzcf", "");
				result.put("cglf", "");
				result.put("csf", "");
				result.put("status", "0");
				return result.toString();
			}
			for (int i = 0; i < yjjFyList.size(); i++) {
				String hq = CoreUtil.objToStr(DBSql.getString(queryjjFy, "HONGQIAO"));//虹桥机场接送
				String pd = CoreUtil.objToStr(DBSql.getString(queryjjFy, "PUDONG"));//浦东机场接送
				String jbzcf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYPRICE"));//临时服务费
				String cglf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYOVERKILOMETERSPRICE"));//超公里费
				String csf = CoreUtil.objToStr(DBSql.getString(queryjjFy, "DAYOVERTIMEPRICE"));//超时费
				result.put("hq", hq);
				result.put("pd", pd);
				result.put("jbzcf", jbzcf);
				result.put("cglf", cglf);
				result.put("csf", csf);
			}
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	/**
	 * @Desc 司机确认接单接口
	 * @param uc
	 * @param processInstId 流程实例ID
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getSjqrjd")
	public static String sendNoticeMsg(UserContext uc, String processInstId) {
		JSONObject result = new JSONObject();
		try {
			String updateSjqrjd = "SELECT RESOURCETASKFPID,RESOURCETASKID FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE BINDID = '"+processInstId+"'";
			String resourceTaskDpId = CoreUtil.objToStr(DBSql.getString(updateSjqrjd, "RESOURCETASKFPID"));//来源任务分配单ID
			String resourceTaskId = CoreUtil.objToStr(DBSql.getString(updateSjqrjd, "RESOURCETASKID"));//来源预订单ID
			DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS='2' WHERE ID = '"+resourceTaskDpId+"'");
			DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_MISSION SET ORDERDATE = SYSDATE,MISSIONSTATUS='2' WHERE BINDID = '"+processInstId+"'");
			DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER SET ORDERSTATUS='2' WHERE ID = '"+resourceTaskId+"'");
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	
	/**
	 * @Desc 根据任务分配表中的id去行车任务表中找到流程实例ID和任务实例ID和任务办理者然后查看内外租流程的单子
	 * @param uc
	 * @param processInstId 流程实例ID
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_openForm")
	public static String openForm(UserContext uc, String id) {
		JSONObject result = new JSONObject();
		try {
			String processInstIdSql = "SELECT BINDID FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE RESOURCETASKFPID = '"+id+"'";
			String processInstId = CoreUtil.objToStr(DBSql.getString(processInstIdSql, "BINDID"));
			if(processInstId.equals("")) {//没有进行派单
				result.put("status", "1");
				result.put("message", "此条任务还未进行派单，请确认！");
				return result.toString();
			}
			//内租、外租流程第一节点的定义IDobj_c8f1f74f91b00001a4a53941155bca40、obj_055dc6822a5547c489578e750084c3ba
			String queryUrl = "SELECT ID,TARGET FROM WFC_TASK WHERE PROCESSINSTID = '"+processInstId+"' AND (ACTIVITYDEFID = "
							+ "'obj_c8f1f74f91b00001a4a53941155bca40' OR ACTIVITYDEFID = 'obj_055dc6822a5547c489578e750084c3ba') "
							+ "UNION SELECT ID,TARGET FROM WFH_TASK WHERE PROCESSINSTID = '"+processInstId+"' "
							+ "AND (ACTIVITYDEFID = 'obj_c8f1f74f91b00001a4a53941155bca40' OR ACTIVITYDEFID = "
							+ "'obj_055dc6822a5547c489578e750084c3ba')";
			List<Map<String, Object>> urlList = DBSql.query(queryUrl, new ColumnMapRowMapper(), new Object[]{});
			if(urlList == null || urlList.isEmpty()) {
				result.put("status", "1");
				result.put("message", "此条任务还未进行派单，请确认！");
				return result.toString();
			}
			for (int i = 0; i < urlList.size(); i++) {
				Map<String, Object> urlMap = urlList.get(i);
				String taskInstId = CoreUtil.objToStr(urlMap.get("ID"));
				String target = CoreUtil.objToStr(urlMap.get("TARGET"));
				SSOUtil ssoUtil = new SSOUtil();
				//根据文件创建者创建sid，formFile.getCreateUser()为userId
				String sidPc = ssoUtil.registerClientSessionNoPassword(target, "cn", "", "pc");
				String sidMobile = ssoUtil.registerClientSessionNoPassword(target, "cn", "", "mobile");
				String portalUrl = SDK.getPortalAPI().getPortalUrl();//http://localhost:8088/portal
				String urlPc = portalUrl + "/r/w?sid="+sidPc+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=2&taskInstId="+taskInstId+"&displayToolbar=true";
				String urlMobile = portalUrl + "/r/w?sid="+sidMobile+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=2&taskInstId="+taskInstId+"&displayToolbar=true";
				result.put("status", "0");
				result.put("urlPc", urlPc);
				result.put("urlMobile", urlMobile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	/**
	 * @Desc 车辆预订派单成功通知
	 * @param uc
	 * @param processInstId 流程实例ID
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getSjqrjd")
	public static String sendNoticeMsg(UserContext uc) {
		JSONObject result = new JSONObject();
		try {
			
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	/**
	 * @Description 
	 * @author WU LiHua
	 * @date 2020年6月18日 下午1:45:51
	 */
	public static void main(String[] args) {
		String str = "b33e8aeb-0573-4f3b-9504-427de5d46424:0,caed1e04-4a3c-4d06-93e9-7d156a5ad691:1";
		String[] split = str.split(",");
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			String[] split2 = string.split(":");
			System.out.println(split2[0]+"==="+split2[1]);
		}
	}
	
	@Mapping(value="com.awspaas.user.apps.shhtaerospaceindustrial_getVisitedObjectInfo", session = false, noSessionEvaluate = "无安全隐患", noSessionReason = "自定义接口")
	public String getVisitedObjectInfo(String userId,UserContext uc) {
		JSONObject reMessage = new JSONObject();
		List<Map<String,Object>> employeeList = null;
		
		try {
			String employeeQuery = "select e.Username ,e.mobile, d.departmentname, c.companyname from ORGUSER  e left join ORGDEPARTMENT  d on e.DEPARTMENTID=d.ID left join ORGCOMPANY c on c.id=d.companyid where e.id='" + userId +"'";
			employeeList = DBSql.query(employeeQuery, new ColumnMapRowMapper(), new Object[] {});
			if(employeeList.isEmpty() || employeeList == null ) {
				reMessage.put("status", "0");
				reMessage.put("message", "employee not exist!");
				
			}
			Map<String,Object> employeeInfo=employeeList.get(0);
			String name = CoreUtil.objToStr(employeeInfo.get("USERNAME"));
			String mobile = CoreUtil.objToStr(employeeInfo.get("MOBILE"));
			String departmentname = CoreUtil.objToStr(employeeInfo.get("DEPARTMENTNAME"));
			String companyname = CoreUtil.objToStr(employeeInfo.get("COMPANYNAME"));
			reMessage.put("status", "1");
			reMessage.put("name", name);
			reMessage.put("mobile", mobile);
			reMessage.put("departmentname", departmentname);
			reMessage.put("companyname", companyname);
		} catch (Exception e ) {
			e.printStackTrace();
			reMessage.put("status", "0");
			reMessage.put("message", e.getMessage());
			
		}
		return reMessage.toString();
	}
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getEmployeeUid")
	public String getVisitedObjectInfo(UserContext uc) {
		JSONObject reMessage = new JSONObject();

		String userId = uc.getUniqueId();
		
		reMessage.put("status","1");
		reMessage.put("userId", userId);
		return reMessage.toString();

	}
}
