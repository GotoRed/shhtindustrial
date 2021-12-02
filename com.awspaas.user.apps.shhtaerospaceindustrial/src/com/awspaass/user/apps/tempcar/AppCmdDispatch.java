package com.awspaass.user.apps.tempcar;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
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
public class AppCmdDispatch {
	/**
	 * @Description //用车取消派单
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_cancelMission")
	public String dispatchCancelMission(String ids, String processInstId, String id, UserContext uc) {
		// System.out.println("Enter cancelMission!");
		System.out.println(id);
		JSONObject returnData = new JSONObject();
		try {
			String userid = uc.getUID();
			String isyjqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in " + "(" + id
					+ ") and zt ='2'";
			int isyjqx = DBSql.getInt(isyjqxsql, "sl");
			if (isyjqx > 0) {// 已经取消的订单不允许再次取消
				returnData.put("status", "1");
				returnData.put("message", "该订单已经取消");
			} else {
				/*
				 * String isqxsql =
				 * "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in " + "("+
				 * id+") and missionstatus>2 and zt='1' and to_char(UDATE,'yyyy-mm-dd') <= (select to_char(sysdate,'yyyy-mm-dd') from dual)"
				 * ;
				 */

				String isqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in " + "(" + id
						+ ") and missionstatus>2 and zt='1' ";
				int isqx = DBSql.getInt(isqxsql, "sl");
				// System.out.println(isqxsql);
				if (isqx > 0) {// 不给取消派单
					returnData.put("status", "1");
					returnData.put("message", "已进入结算，不允许取消！");
				} else {// 允许取消派单
//					//未派单
//					String ispdsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
//		    				+ "("+id+") and zt = '0' ";
					// 结束子流程
					/*
					 * String idsql =
					 * "select a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
					 * +
					 * "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and to_char(UDATE,'yyyy-mm-dd') > "
					 * + "(select to_char(sysdate,'yyyy-mm-dd') from dual) and id in ("
					 * +id+")) B ON A.RESOURCETASKFPID = B.ID";
					 */
					String idsql = "select a.SJXM, a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
							+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and id in (" + id
							+ ")) B ON A.RESOURCETASKFPID = B.ID";

					System.out.println(idsql);
					List<Map<String, Object>> idList = DBSql.query(idsql, new ColumnMapRowMapper(), new Object[] {});

					System.out.println("准备取消的派单任务列表数目:" + idList.size());
					if (idList != null && !idList.isEmpty()) {
						for (Map<String, Object> idmap : idList) {
							String proid = CoreUtil.objToStr(idmap.get("bindid"));
							String sjzh = CoreUtil.objToStr(idmap.get("SJZH"));
							String applyUserName = CoreUtil.objToStr(idmap.get("APPLYUSERNAME"));
							String applyUid = CoreUtil.objToStr(idmap.get("APPLYUID"));
							String applyUserCellPhone = CoreUtil.objToStr(idmap.get("APPLYUSERCELLPHONE"));
							String udate = CoreUtil.objToStr(idmap.get("UDATE"));
							String driverphone = CoreUtil.objToStr(idmap.get("SJLXFS"));
							String drivername = CoreUtil.objToStr(idmap.get("SJXM"));
							if (!udate.equals("")) {
								udate = udate.substring(0, 10);
							}
							String cph = CoreUtil.objToStr(idmap.get("CPH"));
							String vehicletype = CoreUtil.objToStr(idmap.get("VEHICLETYPE"));
							String contactPerson = CoreUtil.objToStr(idmap.get("CONTACTPERSON"));// 用车联系人
							String contactPhone = CoreUtil.objToStr(idmap.get("CONTACTPHONE"));// 用车联系人手机
							String msg = "您于【" + udate + "】日，车牌号为" + cph + "的" + vehicletype + "出行已经取消";
							String content = applyUserName + "您好！由于您" + udate + "日的用车需求不能及时满足，您的预定暂不成功，给您带来不便深表歉意。";
							// MsgNoticeController.sendNoticeMsg(uc, msg, userid, sjzh, "1", "");
							// MsgNoticeController.sendNoticeMsg(uc, content, userid, applyUid, "1", "");
							try {
								System.out.println("准备终止流程！流程号:" + proid + "用户ID:" + userid);
								SDK.getProcessAPI().terminateById(proid, userid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							String updateMissionSql = "update BO_EU_SH_VEHICLEORDER_MISSION set MISSIONSTATUS='6' where bindid='"
									+ proid + "'";
							String xgztsql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set ZT='2',MISSIONSTATUS='6' where id in ("
									+ id + ")";
							System.out.println(xgztsql);
							DBSql.update(xgztsql);// 修改上航_车辆任务分配状态
							DBSql.update(updateMissionSql);

							SmsUtil sms = new SmsUtil();
							System.out.println("预定人电话：" + applyUserCellPhone);

							if (!applyUserCellPhone.equals("")) {

								String phone = applyUserCellPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
										MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								// String param = "{'APPLYUSERNAME':'"+applyUserName+"'}";
								String message_user = "{'applyUserName':'" + applyUserName + "','udate':'" + udate
										+ "','cph':'" + cph + "'}";
								String message_driver = "{'applyUserName':'" + drivername + "','udate':'" + udate
										+ "','cph':'" + cph + "'}";
								try {

									returnData = sms.sendSms(phone, "SMS_228016523", message_user);
									sms.sendSms(driverphone, "SMS_228116397", message_driver);
									System.out.println("车辆预定取消短信通知预订人成功===========" + returnData);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (!contactPhone.equals("")) {
								String phone = contactPhone;
								String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
										MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
								// String param = "{'APPLYUSERNAME':'"+contactPerson+"'}";
								String message_user = "{'applyUserName':'" + contactPerson + "','udate':'" + udate
										+ "','cph':'" + cph + "'}";
								try {
									returnData = sms.sendSms(phone, "SMS_228016523", message_user);
									System.out.println("车辆预定取消短信通知用车人成功===========" + returnData);
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
	public String dispatchModifyMission(String ids, String processInstId, String id, UserContext uc) {
		JSONObject returnData = new JSONObject();
		System.out.println("front args:" + ids);
		try {
			String missionQuerySql = "select A.* from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
					+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and id in (" + id
					+ ")) B ON A.RESOURCETASKFPID = B.ID";
			ProcessInstance createProcessInstance;
			String userId = uc.getUID();
			SmsUtil sms = new SmsUtil();
			
			String newDriverName = "";
			String newDriverAccount = "";
			String newDriverPhone = "";
			String newVehicleLabelName = "";
			String newCarNo="";
			
			String proid = "";
			String oldDriverAccount = "";
			String applyUserName = "";
			String applyUid = "";
			String applyUserCellPhone = "";
			String udate = "";
			String vehicleType = "";
			String driverphone = "";
			String drivername = "";
			String oldCarNo = "";
			String oldVehicleType = "";
			String useType = "";
			String contactPerson = "";
			String contactPhone = "";
			String orderId="";
			String applyUnit="";
			String applyUnitId="";
			String contactPersonAccount="";
			
			String resourceTaskId ="";
			String resourceTaskFpId="";
			String IsInnerCar = "";
			String applyDepName="";
			String applyDepId="";
			String getOnPlace="";
			String targetPlace="";
			String isOutShangHai="";
			

			List<Map<String, Object>> idList = DBSql.query(missionQuerySql, new ColumnMapRowMapper(), new Object[] {});
			if (idList != null && !idList.isEmpty()) {
				Map<String, Object> missionInfo = idList.get(0);
				proid = CoreUtil.objToStr(missionInfo.get("bindid"));
				oldDriverAccount = CoreUtil.objToStr(missionInfo.get("SJZH"));
				applyUserName = CoreUtil.objToStr(missionInfo.get("APPLYUSERNAME"));
				applyUid = CoreUtil.objToStr(missionInfo.get("APPLYUID"));
				applyUserCellPhone = CoreUtil.objToStr(missionInfo.get("APPLYUSERCELLPHONE"));
				udate = CoreUtil.objToStr(missionInfo.get("UDATE"));
				driverphone = CoreUtil.objToStr(missionInfo.get("SJLXFS"));
				drivername = CoreUtil.objToStr(missionInfo.get("SJXM"));

				vehicleType = CoreUtil.objToStr(missionInfo.get("VEHICLETYPE"));
				useType = CoreUtil.objToStr(missionInfo.get("USECARTYPE"));
				orderId=CoreUtil.objToStr(missionInfo.get("ORDERID"));
				applyUnit=CoreUtil.objToStr(missionInfo.get("APPLYUNIT"));
				applyUnitId=CoreUtil.objToStr(missionInfo.get("APPLYUNITID"));
				contactPersonAccount=CoreUtil.objToStr(missionInfo.get("CONTACTPERSONZH"));
				
				resourceTaskId=CoreUtil.objToStr(missionInfo.get("RESOURCETASKID"));
				resourceTaskFpId=CoreUtil.objToStr(missionInfo.get("RESOURCETASKFPID"));
				IsInnerCar=CoreUtil.objToStr(missionInfo.get("ISINTERNALVEHICLE"));
				applyDepName=CoreUtil.objToStr(missionInfo.get("APPLYDEPTNAME"));
				applyDepId=CoreUtil.objToStr(missionInfo.get("APPLYDEPTID"));
				getOnPlace=CoreUtil.objToStr(missionInfo.get("BOARDINGPLACE"));
				targetPlace=CoreUtil.objToStr(missionInfo.get("TARGETPLACE"));
				isOutShangHai=CoreUtil.objToStr(missionInfo.get("ISOUTSHANGHAI"));
				
				
				oldCarNo = CoreUtil.objToStr(missionInfo.get("CPH"));
				oldVehicleType = CoreUtil.objToStr(missionInfo.get("VEHICLETYPE"));
				contactPerson = CoreUtil.objToStr(missionInfo.get("CONTACTPERSON"));// 用车联系人
				contactPhone = CoreUtil.objToStr(missionInfo.get("CONTACTPHONE"));// 用车联系人手机
			}

			if (!ids.equals("")) {
				String[] idsArr = ids.split(",");

				String idsStr = idsArr[0];
				String[] idsStrI = idsStr.split(":");
				int length = idsStrI.length;
				if (length >= 1) {
					newDriverName = idsStrI[0];
				}
				if (length >= 2) {
					newDriverAccount = idsStrI[1];
				}
				if (length >= 3) {
					newDriverPhone = idsStrI[2];
				}
				if (length >= 4) {
					newVehicleLabelName = idsStrI[3];
				}
				if (length >= 5) {
					newCarNo = idsStrI[4];
				}

			}
			if (useType.equals("0")) {
				try {
					System.out.println("准备终止流程！流程号:" + proid + "用户ID:" + userId);
					SDK.getProcessAPI().terminateById(proid, userId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String message_driver = "{'applyUserName':'" + drivername + "','udate':'" + udate
						+ "','cph':'" + oldCarNo + "'}";
				sms.sendSms(driverphone, MnmsConstant.SHSY_CACELMISSION_NOTIFYTODRIVER, message_driver);
				
				String updateMissionSql = "update BO_EU_SH_VEHICLEORDER_MISSION set MISSIONSTATUS='6' where bindid='"
						+ proid + "'";
				String updateAssignSql = "update BO_EU_SH_VEHICLEORDER_ASSIGMIS set ZT='2',MISSIONSTATUS='6' where id in ("
						+ id + ")";

				DBSql.update(updateAssignSql);// 修改上航_车辆任务分配状态
				DBSql.update(updateMissionSql);
				
				createProcessInstance = SDK.getProcessAPI().createProcessInstance("obj_d951639b5cf447d592ea82551b884081", newDriverAccount, "内租-"+udate+"-"+contactPerson);
				
				BO boRecordData = new BO();
				boRecordData.set("ORDERID", orderId+SDK.getRuleAPI().executeAtScript("@replace(@date,-)")+SDK.getRuleAPI().executeAtScript("@sequence('"+orderId+"AA@year@month',4,0)"));//订单号
				boRecordData.set("APPLYUSERNAME", applyUserName);//预定人
				boRecordData.set("APPLYUID", applyUid);//预定人账号
				boRecordData.set("APPLYUSERCELLPHONE", applyUserCellPhone);//预定人手机
				boRecordData.set("APPLYUNIT", applyUnit);//用车单位
				boRecordData.set("APPLYUNITID", applyUnitId);//用车单位ID
				boRecordData.set("CONTACTPERSON", contactPerson);//用车联系人
				boRecordData.set("CONTACTPERSONZH", contactPersonAccount);//用车联系人账号
				boRecordData.set("CONTACTPHONE", contactPhone);//用车联系人手机
				boRecordData.set("UDATE", udate);//使用日期
				boRecordData.set("VEHICLETYPE", vehicleType);//车辆类型
				
				
				boRecordData.set("WZUNITPSN", "");//外租公司调度
				boRecordData.set("WZUNITPSNID", "");//外租公司调度ID
				boRecordData.set("WZUNITPSNPHONE", "");//外租公司联系方式
				boRecordData.set("RESOURCETASKID", resourceTaskId);//来源预订单ID
				boRecordData.set("RESOURCETASKFPID", resourceTaskFpId);//来源任务分配单ID
				boRecordData.set("ISINTERNALVEHICLE", IsInnerCar);//车辆属性
				boRecordData.set("APPLYDEPTNAME", applyDepName);//用车部门
				boRecordData.set("APPLYDEPTID", applyDepId);//用车部门ID
				
				boRecordData.set("BOARDINGPLACE", getOnPlace);//上车地点
				boRecordData.set("TARGETPLACE", targetPlace);//目的地
				boRecordData.set("ISOUTSHANGHAI", isOutShangHai);//是否出省
				
				boRecordData.set("SJXM", newDriverName);//司机姓名
				boRecordData.set("SJZH", newDriverAccount);//司机账号
				boRecordData.set("SJLXFS", newDriverPhone);//司机联系方式
				boRecordData.set("CPH", newCarNo);//车牌号
				boRecordData.set("VEHICLELABELNAME", newVehicleLabelName);//车辆品牌
				SDK.getBOAPI().create(CoreUtil.MISSION, boRecordData, createProcessInstance, UserContext.fromUID(userId));
				SDK.getProcessAPI().start(createProcessInstance);
				
				DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET ZT = '1', MISSIONSTATUS = '1',MISSIONBINDID='"+boRecordData.getId()+"' WHERE ID = '"+id+"'");
				
				String msg_to_driver = "{'SJXM':'"+newDriverName+"','APPLYUSERNAME':'"+applyUserName+"','APPLYUSERCELLPHONE':'"+applyUserCellPhone+"','UDATE':'"+udate
						+"','CPH':'"+newCarNo+"','BOARDINGPLACE':'"+getOnPlace+"','TARGETPLACE':'"+targetPlace+"'}";
				String msg_to_user = "{'APPLYUSERNAME':'"+applyUserName+"','UDATE':'"+udate+"','OLDSJXM':'"+drivername+"','NEWSJXM':'"+newDriverName
						+"','SJLXFS':'"+newDriverPhone+"','OLDCPH':'"+oldCarNo+"','NEWCPH':'"+newCarNo+"'}";
				
				sms.sendSms(newDriverPhone, MnmsConstant.SHSY_MISSION_NOTIFYTODRIVER, msg_to_driver);
				sms.sendSms(applyUserCellPhone, MnmsConstant.SHSY_MODIFYMISSION_NOTIFYTOUSER, msg_to_user);
				
				if(!contactPhone.equals("")) {
					sms.sendSms(contactPhone, MnmsConstant.SHSY_MODIFYMISSION_NOTIFYTOUSER, msg_to_user);
				}
				
				returnData.put("status", "0");
				returnData.put("message", "成功变更");
			} else if (useType.equals("1")) {
				returnData.put("status", "1");
				returnData.put("message", "外租车辆变更需取消后，再派单！");
				return returnData.toString();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}



}
