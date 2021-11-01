package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.commons.htmlframework.HtmlPageTemplate;
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
import com.awspaas.user.apps.shhtaerospaceindustrial.util.UtilToolsForString;

@Controller
public class ExchangeCenterOrderController {
	
	/**
	 * 
	 * @Description 获取账号
	 * @author WU LiHua
	 * @date 2020年8月25日 上午10:07:20
	 * @param type
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getUserId")
	public String getUserId(UserContext uc) {
		JSONObject returnData = new JSONObject();
		try {
			String userId = uc.getUID();
			returnData.put("status", "0");
			returnData.put("userId", userId);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
		}
		return returnData.toString();
	}
	/**
	 * 
	 * @Description 扫描二维码打开客户投诉流程
	 * @author WU LiHua
	 * @date 2020年8月25日 上午10:07:20
	 * @param type
	 * @return
	 */
	@Mapping(value="com.awspaas.user.apps.shhtaerospaceindustrial_openRzProcessController", session = false, noSessionEvaluate = "无安全隐患", noSessionReason = "自定义接口")
	public String getTripCityInfo(String type) {
		Map<String, Object> map = new HashMap<String, Object>();//存放传递到HTML页面的值
		try {
			SSOUtil ssoUtil = new SSOUtil();
			String sid = ssoUtil.registerClientSessionNoPassword("guest", "cn", "", "mobile");
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			map.put("sid", sid);
			map.put("url", portalUrl+"/r/w?sid="+sid+"&cmd=com.actionsoft.apps.workbench_mobile_process_start&groupId=obj_cc96267d3eac4b0aa98fa47a5a85c4a2&processDefId=obj_b8c81bcf702b40acbdf1662b7da16d5f");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HtmlPageTemplate.merge("com.awspaas.user.apps.workattendance", "openProcessHtml.html", map);
	}
	/**
	 * @Desc 查询交流中心待办、已办任务
	 * @param uc
	 * @param roleType 角色类型（0：普通用户|1：交流中心销售角色）
	 * @param bDate 开始日期
	 * @param eDate 结束日期
	 * @param page 页数
	 * @param pageCount 每页数量
	 * @param taskType 任务类型（0：待办|1：全部）
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getExchangeCenterOrder")
	public String getExchangeCenterOrder(UserContext uc,int roleType,String bDate,String eDate,int page,int pageCount,int taskType) {
    	JSONObject returnData = new JSONObject();
		try {
			String sid = uc.getSessionId();
			if(page<1) {
				returnData.put("status", "1");
				returnData.put("message", "请传入大于等于1的起始页！");
				return returnData.toString();
			}
			String condition = "";
			if(!bDate.equals("") && !eDate.equals("")) {
				condition = "and  a.BDATE >= TO_DATE('"+bDate+"','yyyy-MM-dd') AND a.BDATE <= TO_DATE('"+eDate+"','yyyy-MM-dd')";
			}
			//获取起始条数和结束条数
			int start = (page-1)*pageCount+1;
			int end = page*pageCount;
			String querSql = "";
			if(roleType == 0) {//普通用户查询已办
				querSql = " select * from (select a.MOBILETITLE,a.MOBILECONTENT,a.ORDERSTATUS,a.ORDERID,b.processinstId,b.id taskinstId  from  BO_EU_SH_JLCENTER_TYORDERHEAD a  left join wfh_task  b on a.bindid = b.processinstid "
						+ " where b.target = '"+uc.getUID()+"' "+condition+" order by b.begintime desc) where rownum  >='"+start +"'  and rownum <= '"+end+"' ";
			}else if(roleType == 1) {//交流中心销售角色查看
				if(taskType == 0) {//待办
					querSql = "  select * from (select a.MOBILETITLE,a.MOBILECONTENT,a.ORDERSTATUS,a.ORDERID,b.processinstId,b.id taskinstId from  BO_EU_SH_JLCENTER_TYORDERHEAD a  left join wfc_task  b on a.bindid = b.processinstid "
							+ " where b.target = '"+uc.getUID()+"' "+condition+" order by b.begintime desc) where rownum  >='"+start +"'  and rownum <= '"+end+"' ";
				}else {//已办
					querSql = " select * from ( select a.MOBILETITLE,a.MOBILECONTENT,a.ORDERSTATUS,a.ORDERID,b.processinstId,b.id taskinstId,b.begintime from  BO_EU_SH_JLCENTER_TYORDERHEAD a  left join wfc_task  b on a.bindid = b.processinstid "
							+ " where b.target = '"+uc.getUID()+"' "+condition+" union "
							+ " select a.MOBILETITLE,a.MOBILECONTENT,a.ORDERSTATUS,a.ORDERID,b.processinstId,b.id taskinstId,b.begintime from  BO_EU_SH_JLCENTER_TYORDERHEAD a  left join wfh_task  b on a.bindid = b.processinstid " 
							+ "	where b.target = '"+uc.getUID()+"' "+condition+" ) c where rownum  >='"+start +"'  and rownum <= '"+end+"' order by c.begintime desc";
				}
			}else {
				returnData.put("status", "1");
				returnData.put("message", "传入的角色类型有误！");
				return returnData.toString();
			}
			JSONArray dataArr = new JSONArray();
			List<Map<String , Object>> list = DBSql.query(querSql, new ColumnMapRowMapper(),new Object[] {});
			if(list==null  || list.isEmpty()) {
				returnData.put("status", "0");
				returnData.put("data", dataArr);
				return returnData.toString();
			}
			String portalUrl = SDK.getPortalAPI().getPortalUrl();
			for(Map<String , Object> map:list) {
				JSONObject  dataJson = new JSONObject();
				dataJson.put("mobileTitle",  UtilToolsForString.handleObjectToString(map.get("MOBILETITLE")));
				dataJson.put("mobileContent",  UtilToolsForString.handleObjectToString(map.get("MOBILECONTENT")));
				String orderStatus = UtilToolsForString.handleObjectToString(map.get("ORDERSTATUS"));
				//、已提交、已结算、已取消、已接单）、链接地址
				if(orderStatus.equals("0")) {
					orderStatus = "未提交";
				}else if(orderStatus.equals("1")) {
					orderStatus = "已提交";
				}else if(orderStatus.equals("2")) {
					orderStatus = "已接单";
				}else if(orderStatus.equals("3")) {
					orderStatus = "已取消";
				}
				dataJson.put("orderStatus", orderStatus);
				String processInstId =  UtilToolsForString.handleObjectToString(map.get("PROCESSINSTID"));
				dataJson.put("processInstId", processInstId);
				String taskInstId =  UtilToolsForString.handleObjectToString(map.get("taskInstId"));
				dataJson.put("taskInstId",  UtilToolsForString.handleObjectToString(map.get("TASKINSTID")));
				String linkUrl = portalUrl + "/r/w?sid="+sid+"&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId="+processInstId+"&openState=1&taskInstId="+taskInstId+"&displayToolbar=true";
				dataJson.put("url",  linkUrl);
				dataArr.add(dataJson);
			}
			returnData.put("status", "0");
			returnData.put("data", dataArr);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
	
	
	/**
	 * @Description //用车取消派单
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_qxApply")
	public String qxApply(String ids,String processInstId,String id,UserContext uc) {
    	JSONObject returnData = new JSONObject();
    	try {
    		String userid = uc.getUID();
    		String isyjqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
    				+ "("+id+") and zt ='2'";
    		int isyjqx = DBSql.getInt(isyjqxsql, "sl");
    		if (isyjqx>0) {//已经取消的订单不允许再次取消
    			returnData.put("status", "1");
    			returnData.put("message", "已经取消的订单不允许再次取消");
			}else{
				String isqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
	    				+ "("+id+") and zt = '1' and to_char(UDATE,'yyyy-mm-dd') <= (select to_char(sysdate,'yyyy-mm-dd') from dual)";
	    		int isqx = DBSql.getInt(isqxsql, "sl");
	    		if (isqx>0) {//不给取消派单
	    			returnData.put("status", "1");
	    			returnData.put("message", "所勾选的当中有不允许取消的派单");
				}else {//允许取消派单
//					//未派单
//					String ispdsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
//		    				+ "("+id+") and zt = '0' ";
					//结束子流程
					String idsql = "select a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
							+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and to_char(UDATE,'yyyy-mm-dd') > "
							+ "(select to_char(sysdate,'yyyy-mm-dd') from dual) and id in ("+id+")) B ON A.RESOURCETASKFPID = B.ID";
					List<Map<String, Object>> idList = DBSql.query(idsql, new ColumnMapRowMapper(), new Object[] {});
					String xgztsql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set ZT='2' where id in ("+id+")";
					DBSql.update(xgztsql);//修改上航_车辆任务分配状态
					String ztsql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set MISSIONSTATUS='6' where id in ("+id+")";
					DBSql.update(ztsql);//修改上航_车辆任务分配任务单状态
					if (idList!=null&&!idList.isEmpty()) {
						for (Map<String, Object> idmap : idList) {
							String proid = CoreUtil.objToStr(idmap.get("bindid"));
							String sjzh = CoreUtil.objToStr(idmap.get("SJZH"));
							String applyUserName = CoreUtil.objToStr(idmap.get("APPLYUSERNAME"));
							String applyUid = CoreUtil.objToStr(idmap.get("APPLYUID"));
							String applyUserCellPhone = CoreUtil.objToStr(idmap.get("APPLYUSERCELLPHONE"));
							String udate = CoreUtil.objToStr(idmap.get("UDATE"));
							if(!udate.equals("")) {
								udate = udate.substring(0, 10);
							}
							String cph = CoreUtil.objToStr(idmap.get("CPH"));
							String vehicletype = CoreUtil.objToStr(idmap.get("VEHICLETYPE"));
							String contactPerson = CoreUtil.objToStr(idmap.get("CONTACTPERSON"));//用车联系人
							String contactPhone = CoreUtil.objToStr(idmap.get("CONTACTPHONE"));//用车联系人手机
							String msg = "您于【"+udate+"】日，车牌号为"+cph+"的"+vehicletype+"出行已经取消";
							String content = applyUserName+"您好！由于您"+udate+"日的用车需求不能及时满足，您的预定暂不成功，给您带来不便深表歉意。";
							MsgNoticeController.sendNoticeMsg(uc, msg, userid, sjzh, "1", "");
							MsgNoticeController.sendNoticeMsg(uc, content, userid, applyUid, "1", "");
							SmsUtil sms = new SmsUtil();
							if(!applyUserCellPhone.equals("")) {
								String phone = applyUserCellPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								String param = "{'APPLYUSERNAME':'"+applyUserName+"'}";
								try {
									returnData = sms.sendSms(phone,templateId,param);
									System.out.println("车辆预定取消短信通知预订人成功==========="+returnData);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if(!contactPhone.equals("")) {
								String phone = contactPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								String param = "{'APPLYUSERNAME':'"+contactPerson+"'}";
								try {
									returnData = sms.sendSms(phone,templateId,param);
									System.out.println("车辆预定取消短信通知用车人成功==========="+returnData);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							try {
								SDK.getProcessAPI().terminateById(proid, userid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
						returnData.put("status", "0");
		    			returnData.put("message", "取消成功");
					}
					returnData.put("status", "0");
	    			returnData.put("message", "取消成功");
				}
			}
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData.toString();
	}
}
