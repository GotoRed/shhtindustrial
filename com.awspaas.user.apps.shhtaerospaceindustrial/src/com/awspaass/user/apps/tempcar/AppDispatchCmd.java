package com.awspaass.user.apps.tempcar;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class AppDispatchCmd {
	/**
	 * @Description //用车取消派单
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_cancelMission")
	public String dispatchCancelMission(String ids,String processInstId,String id,UserContext uc) {
		//System.out.println("Enter cancelMission!");
		System.out.println(id);
    	JSONObject returnData = new JSONObject();
    	try {
    		String userid = uc.getUID();
    		String isyjqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
    				+ "("+id+") and zt ='2'";
    		int isyjqx = DBSql.getInt(isyjqxsql, "sl");
    		if (isyjqx>0) {//已经取消的订单不允许再次取消
    			returnData.put("status", "1");
    			returnData.put("message", "该订单已经取消");
			}else{
				/*String isqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
	    				+ "("+id+") and missionstatus>2 and zt='1' and to_char(UDATE,'yyyy-mm-dd') <= (select to_char(sysdate,'yyyy-mm-dd') from dual)";*/
				
				String isqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
	    				+ "("+id+") and missionstatus>2 and zt='1' ";
	    		int isqx = DBSql.getInt(isqxsql, "sl");
	    		//System.out.println(isqxsql);
	    		if (isqx>0) {//不给取消派单
	    			returnData.put("status", "1");
	    			returnData.put("message", "已进入结算，不允许取消！");
				}else {//允许取消派单
//					//未派单
//					String ispdsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
//		    				+ "("+id+") and zt = '0' ";
					//结束子流程
					/*String idsql = "select a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
							+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and to_char(UDATE,'yyyy-mm-dd') > "
							+ "(select to_char(sysdate,'yyyy-mm-dd') from dual) and id in ("+id+")) B ON A.RESOURCETASKFPID = B.ID";*/
					String idsql = "select a.SJXM, a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
							+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and id in ("+id+")) B ON A.RESOURCETASKFPID = B.ID";
					
					System.out.println(idsql);
					List<Map<String, Object>> idList = DBSql.query(idsql, new ColumnMapRowMapper(), new Object[] {});
					
					System.out.println("准备取消的派单任务列表数目:"+idList.size());
					if (idList!=null&&!idList.isEmpty())
					{
						for (Map<String, Object> idmap : idList) {
							String proid = CoreUtil.objToStr(idmap.get("bindid"));
							String sjzh = CoreUtil.objToStr(idmap.get("SJZH"));
							String applyUserName = CoreUtil.objToStr(idmap.get("APPLYUSERNAME"));
							String applyUid = CoreUtil.objToStr(idmap.get("APPLYUID"));
							String applyUserCellPhone = CoreUtil.objToStr(idmap.get("APPLYUSERCELLPHONE"));
							String udate = CoreUtil.objToStr(idmap.get("UDATE"));
							String driverphone =  CoreUtil.objToStr(idmap.get("SJLXFS"));
							String drivername= CoreUtil.objToStr(idmap.get("SJXM"));
							if(!udate.equals("")) {
								udate = udate.substring(0, 10);
							}
							String cph = CoreUtil.objToStr(idmap.get("CPH"));
							String vehicletype = CoreUtil.objToStr(idmap.get("VEHICLETYPE"));
							String contactPerson = CoreUtil.objToStr(idmap.get("CONTACTPERSON"));//用车联系人
							String contactPhone = CoreUtil.objToStr(idmap.get("CONTACTPHONE"));//用车联系人手机
							String msg = "您于【"+udate+"】日，车牌号为"+cph+"的"+vehicletype+"出行已经取消";
							String content = applyUserName+"您好！由于您"+udate+"日的用车需求不能及时满足，您的预定暂不成功，给您带来不便深表歉意。";
							//MsgNoticeController.sendNoticeMsg(uc, msg, userid, sjzh, "1", "");
							//MsgNoticeController.sendNoticeMsg(uc, content, userid, applyUid, "1", "");
							try {
								System.out.println("准备终止流程！流程号:"+proid+"用户ID:"+userid);
								SDK.getProcessAPI().terminateById(proid, userid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							String xgztsql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set ZT='2',MISSIONSTATUS='6' where id in ("+id+")";
							System.out.println(xgztsql);
							DBSql.update(xgztsql);//修改上航_车辆任务分配状态
							
							SmsUtil sms = new SmsUtil();
							System.out.println("预定人电话："+applyUserCellPhone);
							
							if(!applyUserCellPhone.equals("")) {
								
								String phone = applyUserCellPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								//String param = "{'APPLYUSERNAME':'"+applyUserName+"'}";
								String message_user = "{'applyUserName':'"+applyUserName+"','udate':'"+udate+"','cph':'"+cph+"'}";
								String message_driver = "{'applyUserName':'"+drivername+"','udate':'"+udate+"','cph':'"+cph+"'}";
								try {
									
									returnData = sms.sendSms(phone,"SMS_228016523",message_user);
									sms.sendSms(driverphone, "SMS_228116397", message_driver);
									System.out.println("车辆预定取消短信通知预订人成功==========="+returnData);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if(!contactPhone.equals("")) {
								String phone = contactPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								//String param = "{'APPLYUSERNAME':'"+contactPerson+"'}";
								String message_user = "{'applyUserName':'"+contactPerson+"','udate':'"+udate+"','cph':'"+cph+"'}";
								try {
									returnData = sms.sendSms(phone,"SMS_228016523",message_user);
									System.out.println("车辆预定取消短信通知用车人成功==========="+returnData);
								} catch (Exception e) {
									e.printStackTrace();
								}
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
	
	  @Mapping("com.awspaass.user.apps.tempcar_dispatchModifyMission")
		public String dispatchModifyMission(String ids,String processInstId,String id,UserContext uc) {
		  JSONObject returnData = new JSONObject();
		  String userid = uc.getUID();
		  
		  String queryInfoSql = "select a.UDATE, a.CPH,a.SJXM, a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION a WHERE BINDID = "
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
		  String message_user = "{'applyUserName':'"+APPLYUSERNAME+"','udate':'"+UDATE+"','cph':'"+CPH+"'}";
		  String message_driver ="{'applyUserName':'"+SJXM+"','udate':'"+UDATE+"','cph':'"+CPH+"'}";
		  
		  SmsUtil sms = new SmsUtil();
		  try {
				System.out.println("准备终止流程！流程号:"+BINDID+"用户ID:"+userid);
				SDK.getProcessAPI().terminateById(BINDID, userid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		  try {
			  	//sms.sendSms(APPLYUSERCELLPHONE,"SMS_228016523",message_user);
				sms.sendSms(SJLXFS, "SMS_228116397", message_driver);
				//sms.sendSms("13918947832", "SMS_228116397", message_driver);
				
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
