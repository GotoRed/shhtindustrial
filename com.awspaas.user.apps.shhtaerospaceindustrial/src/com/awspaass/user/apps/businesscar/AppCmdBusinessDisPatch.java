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
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class AppCmdBusinessDisPatch {
	/**
	 * 
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.businesscar_dispatchCancelMission")
	public String b_dispatchCancelMission(String ids, String processInstId, String id, UserContext uc) {
		JSONObject returnData = new JSONObject();
		try {

			String userid = uc.getUID();
			String isyjqxsql = "select count(1) sl from BO_EU_YBOFFICEUSECAR_DS where id in " + "(" + id
					+ ") and zt ='2'";
			int isyjqx = DBSql.getInt(isyjqxsql, "sl");
			if (isyjqx > 0) {// 已经取消的订单不允许再次取消
				returnData.put("status", "1");
				returnData.put("message", "该订单已经取消");
			} else {
				/*
				 * String isqxsql =
				 * "select count(1) sl from BO_EU_YBOFFICEUSECAR_DS where id in " + "("+
				 * id+") and missionstatus>2 and zt='1' and to_char(UDATE,'yyyy-mm-dd') <= (select to_char(sysdate,'yyyy-mm-dd') from dual)"
				 * ;
				 */

				String isqxsql = "select count(1) sl from BO_EU_YBOFFICEUSECAR_DS where id in " + "(" + id
						+ ") and missionstatus>2 and zt='1' ";
				int isqx = DBSql.getInt(isqxsql, "sl");
				// System.out.println(isqxsql);
				if (isqx > 0) {// 不给取消派单
					returnData.put("status", "1");
					returnData.put("message", "已进入结算，不允许取消！");
				} else {// 允许取消派单
					String idsql = "select a.SJXM, a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERMOBILE,a.USEDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_YBBZUSECAR_MISSION A right JOIN "
							+ "(select id from BO_EU_YBOFFICEUSECAR_DS  where zt = '1' and id in (" + id
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
							String applyUserCellPhone = CoreUtil.objToStr(idmap.get("APPLYUSERMOBILE"));
							String udate = CoreUtil.objToStr(idmap.get("USEDATE"));
							String driverphone = CoreUtil.objToStr(idmap.get("SJLXFS"));
							String drivername = CoreUtil.objToStr(idmap.get("SJXM"));
							if (!udate.equals("")) {
								udate = udate.substring(0, 10);
							}
							String cph = CoreUtil.objToStr(idmap.get("CPH"));
							String vehicletype = CoreUtil.objToStr(idmap.get("VEHICLETYPE"));
							String contactPerson = CoreUtil.objToStr(idmap.get("CONTACTPERSON"));// 用车联系人
							String contactPhone = CoreUtil.objToStr(idmap.get("CONTACTPHONE"));// 用车联系人手机

							String content = applyUserName + "您好！由于您" + udate + "日的用车需求不能及时满足，您的预定暂不成功，给您带来不便深表歉意。";

							try {
								System.out.println("准备终止流程！流程号:" + proid + "用户ID:" + userid);
								SDK.getProcessAPI().terminateById(proid, userid);
							} catch (Exception e) {
								e.printStackTrace();
							}
							String updateMissionSql = "update BO_EU_YBBZUSECAR_MISSION set MISSIONSTATUS='6' where bindid='"
									+ proid + "'";
							String xgztsql = "update BO_EU_YBOFFICEUSECAR_DS set ZT='2',MISSIONSTATUS='6' where id in ("
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

	/**
	 * 保障用车调度派遣任务
	 */
	@Mapping("com.awspaas.user.apps.businesscar__dispatchAssignMission")
	public String b_dispatchassignMission(String ids, String processInstId, String id, UserContext uc) {

		JSONObject returnData = new JSONObject();
		try {
			String userId = uc.getUID();
			String queryCount = "SELECT COUNT(1) SL FROM BO_EU_YBOFFICEUSECAR_DS WHERE BINDID = '" + processInstId
					+ "' AND ID IN (" + id + ") AND ZT <> '0'";
			int sl = CoreUtil.objToInt(DBSql.getInt(queryCount, "SL"));
			if (sl > 0) {
				returnData.put("status", "1");
				returnData.put("message", "此状态任务单不能进行派单！");
				return returnData.toString();
			}
			if (!id.equals("")) {// 如果ID已经派单了，不能重复派单
				String[] splitIds = id.split(",");
				for (int i = 0; i < splitIds.length; i++) {
					String splitId = splitIds[i];
					String queryId = "SELECT COUNT(1) SL FROM BO_EU_YBBZUSECAR_MISSION WHERE RESOURCETASKFPID = "
							+ splitId + "";
					int idSl = CoreUtil.objToInt(DBSql.getInt(queryId, "SL"));
					if (idSl > 0) {
						returnData.put("status", "1");
						returnData.put("message", "您勾选的数据中存在重复派单！");
						return returnData.toString();
					}
				}
			}
			String orderInfoQuerySql = "SELECT * FROM BO_EU_YBOFFICEUSECAR WHERE BINDID = '" + processInstId + "'";
			List<Map<String, Object>> orderInfoList = DBSql.query(orderInfoQuerySql, new ColumnMapRowMapper(),
					new Object[] {});
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
			String orderId = "";
			String getOnPlace = "";
			String targetPlace = "";
			String isOutShanghai = "";
			String bDate = "";

			String carType = "";
			String udate = "";
			String vehicleType = "";
			String carNo = "";
			String driverName = "";
			String driverAccount = "";
			String driverPhone = "";
			String newDriverName = "";
			String newDriverAccount = "";
			String newDriverPhone = "";
			String idNew = "";
			String route = "";
			/*
			 * String clppgg = ""; String drivernameout=""; String driverphoneout=""; String
			 * carnoout="";
			 */
			String udate_n = "";

			if (orderInfoList != null && !orderInfoList.isEmpty()) {// 如果车辆预定表中没有数据
				Map<String, Object> oderInfo = orderInfoList.get(0);
				contactPerson = CoreUtil.objToStr(oderInfo.get("CONTACTPERSON"));// 用车联系人
				applyUserName = CoreUtil.objToStr(oderInfo.get("APPLYUSERNAME"));// 预定人
				applyUserId = CoreUtil.objToStr(oderInfo.get("APPLYUID"));// 预定人账号
				applyUserPhone = CoreUtil.objToStr(oderInfo.get("APPLYUSERMOBILE"));// 预定人手机
				contactUnit = CoreUtil.objToStr(oderInfo.get("APPLYUNIT"));// 预定单位
				contactDept = CoreUtil.objToStr(oderInfo.get("APPLYDEPTNAME"));// 预定部门
				contactUserName = CoreUtil.objToStr(oderInfo.get("CONTACTPERSON"));// 用车联系人
				contactPhone = CoreUtil.objToStr(oderInfo.get("CONTACTPHONE"));// 用车联系人手机
				orderId = CoreUtil.objToStr(oderInfo.get("ID"));// 车辆预定表ID
				getOnPlace = CoreUtil.objToStr(oderInfo.get("RUNLINE"));// 上车地点
				targetPlace = CoreUtil.objToStr(oderInfo.get("RUNLINEEND"));// 目的地
				isOutShanghai = CoreUtil.objToStr(oderInfo.get("ISOUTSHANGHAI"));// 是否出省
				bDate = CoreUtil.objToStr(oderInfo.get("BDATE"));// 日期
			}

			if (!ids.equals("")) {
				String[] idsArr = ids.split(",");
				String idsStr = idsArr[0];
				String[] idsStrI = idsStr.split(":");
				int length = idsStrI.length;// 传来的字段长度
				if (length >= 1) {
					idNew = idsStrI[0];// 任务派单子表ID
				}
				if (length >= 2) {
					udate = idsStrI[1];// 使用日期
				}
				if (length >= 3) {
					vehicleType = idsStrI[2];// 车辆类型
				}
				if (length >= 4) {
					carNo = idsStrI[3];// 车牌号
				}
				if (length >= 5) {
					driverName = idsStrI[4];// 司机姓名
				}
				if (length >= 6) {
					driverAccount = idsStrI[5];// 司机账号
				}
				if (length >= 7) {
					driverPhone = idsStrI[6];// 司机联系方式
				}
				if (length >= 8) {
					route = idsStrI[7];
				}

				Date bdate_f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bDate);
				Date udate_f = new SimpleDateFormat("yyyy-MM-dd").parse(udate);
				if (bdate_f.getDate() < udate_f.getDate()) {
					udate_n = udate + " 8:00";
				} else {
					udate_n = bDate;
				}
				System.out.println("#保障用车任务派单" + "#");
				String processDefId = "";// 流程定义ID
				String title = "";// 流程标题

				processDefId = "obj_7f2b76f2e0544a0fa0b9891a4061abed";
				title = "保障用车任务";
				if (driverName.equals("")) {// 如果司机为空
					returnData.put("status", "1");
					returnData.put("message", "未正确发起派车流程，请确认司机姓名是否选择！");
					return returnData.toString();
				}
				if (carNo.equals("")) {// 如果车牌号为空
					returnData.put("status", "1");
					returnData.put("message", "未正确发起派车流程，请确认车牌号是否选择！");
					return returnData.toString();
				}

				ProcessInstance createProcessInstance;
				createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, driverAccount,
						title + "-" + udate_n + "-" + contactPerson);

				// 1、创建流程实例
				// ProcessInstance createProcessInstance =
				// SDK.getProcessAPI().createProcessInstance(processDefId,
				// "A755C03FB0B1471CE053F401A8C0C17FmB0", title+"-"+udate_n+"-"+contactPerson);
				// 2、为创建的流程实例写入Bo数据
				BO boRecordData = new BO();
				boRecordData.set("ORDERID", SDK.getRuleAPI().executeAtScript("@replace(@date,-)")
						+ SDK.getRuleAPI().executeAtScript("@sequence('" + "AA@year@month',4,0)"));
				boRecordData.set("APPLYUSERNAME", applyUserName);// 预定人
				boRecordData.set("APPLYUID", applyUserId);// 预定人账号
				boRecordData.set("APPLYUSERMOBILE", applyUserPhone);// 预定人手机
				boRecordData.set("APPLYUNIT", contactUnit);// 用车单位
				boRecordData.set("APPLYUNITID", contactUnitId);// 用车单位ID
				boRecordData.set("CONTACTPERSON", contactPerson);// 用车联系人
				boRecordData.set("CONTACTPHONE", contactPhone);// 用车联系人手机
				boRecordData.set("USEDATE", udate_n);// 使用日期
				boRecordData.set("VEHICLETYPE", vehicleType);// 车辆类型

				boRecordData.set("SJXM", driverName);// 司机姓名
				boRecordData.set("SJZH", driverAccount);// 司机账号
				boRecordData.set("SJLXFS", driverPhone);// 司机联系方式
				boRecordData.set("CPH", carNo);// 车牌号
				// 根据文件创建者创建sid，formFile.getCreateUser()为userId
				// sid = ssoUtil.registerClientSessionNoPassword(sjzh, "cn", "", "mobile");

				boRecordData.set("RESOURCETASKID", orderId);// 来源预订单ID
				boRecordData.set("RESOURCETASKFPID", idNew);// 来源任务分配单ID
				boRecordData.set("ISINTERNALVEHICLE", carType);// 车辆属性
				// boRecordData.set("APPLYDEPTNAME", contactDept);// 用车部门
				// boRecordData.set("APPLYDEPTID", contactDeptId);// 用车部门ID
				// boRecordData.set("VEHICLELABELNAME", clppgg);//车辆品牌
				boRecordData.set("BOARDINGPLACE", getOnPlace);// 上车地点
				boRecordData.set("TARGETPLACE", targetPlace);// 目的地
				boRecordData.set("RUNLINE", route);// 路线
				boRecordData.set("ISOUTSHANGHAI", isOutShanghai);// 是否出省
				boRecordData.set("MISSIONSTATUS", 1);// 任务状态已经派单

				int MissisonID = SDK.getBOAPI().create(CoreUtil.BO_EU_YBBZUSECAR_MISSION, boRecordData,
						createProcessInstance, UserContext.fromUID(userId));
				// 3、启动创建的流程
				ProcessExecuteQuery pquery = SDK.getProcessAPI().start(createProcessInstance);

				int updateflag = DBSql
						.update("UPDATE BO_EU_YBOFFICEUSECAR_DS SET ZT = '1',MISSIONSTATUS = '1',MISSIONBINDID='"
								+ createProcessInstance.getId() + "'  WHERE ID = '" + idNew + "'");

				String message = "";// 发送给司机

				String param_to_user = "";
				String param_to_driver = "";
				String message_to_user = "";
				String message_to_driver = "";

				SmsUtil sms = new SmsUtil();
				String templateId_user = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
						MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
				String templateId_driver = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
						MnmsConstant.PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID);

				param_to_user = "{'APPLYUSERNAME':'" + applyUserName + "','UDATE':'" + udate_n + "','SJXM':'"
						+ driverName + "','SJLXFS':'" + driverPhone + "','CPH':'" + carNo + "','VEHICLETYPE':'"
						+ SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType) + "'}";
				param_to_driver = "{'SJXM':'" + driverName + "','APPLYUSERNAME':'" + applyUserName
						+ "','APPLYUSERCELLPHONE':'" + applyUserPhone + "','UDATE':'" + udate_n + "','CPH':'" + carNo
						+ "','BOARDINGPLACE':'" + getOnPlace + "','TARGETPLACE':'" + targetPlace + "'}";

				/**
				 * 给订车人、用车人、派遣司机发送短信
				 */
				if (!applyUserPhone.equals("")) {
					try {
						returnData = SmsUtil.sendSms(applyUserPhone, templateId_user, param_to_user);
						System.out.println("车辆预定成功后给用户发送短信消息=======" + returnData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (!contactPhone.equals("")) {
					try {
						returnData = SmsUtil.sendSms(contactPhone, templateId_user, param_to_user);
						System.out.println("车辆预定成功后给用车联系人发送短信消息=======" + returnData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (!driverPhone.equals("")) {
					try {
						returnData = SmsUtil.sendSms(driverPhone, templateId_driver, param_to_driver);
						System.out.println("车辆预定成功后给司机发送短信消息=======" + returnData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				returnData.put("status", "0");

			} else {
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
	 * 保障用车调度取消任务
	 * 
	 * @param ids
	 * @param processInstId
	 * @param id
	 * @param uc
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.businesscar__dispatchModifyMission")
	public String b_dispatchModifyMission(String ids, String processInstId, String id, UserContext uc) {
		JSONObject returnData = new JSONObject();
		System.out.println("front args:" + ids);
		System.out.println(id);
		try {
			String missionQuerySql = "select a.* from BO_EU_YBBZUSECAR_MISSION a,BO_EU_YBOFFICEUSECAR_DS b WHERE a.RESOURCETASKFPID = b.ID AND b.ID='"
					+ id + "'";
			System.out.println(missionQuerySql);
			ProcessInstance createProcessInstance;
			String userId = uc.getUID();
			SmsUtil sms = new SmsUtil();

			String newDriverName = "";
			String newDriverAccount = "";
			String newDriverPhone = "";
			String newVehicleLabelName = "";
			String newCarNo = "";

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
			String orderId = "";
			String applyUnit = "";
			String applyUnitId = "";
			String contactPersonAccount = "";

			String resourceTaskId = "";
			String resourceTaskFpId = "";
			// String IsInnerCar = "";
			// String applyDepName="";
			// String applyDepId="";
			String getOnPlace = "";
			String targetPlace = "";
			String route = "";
			String isOutShangHai = "";

			List<Map<String, Object>> idList = DBSql.query(missionQuerySql, new ColumnMapRowMapper(), new Object[] {});
			System.out.print(idList.size());
			if (idList != null && !idList.isEmpty()) {
				Map<String, Object> missionInfo = idList.get(0);
				proid = CoreUtil.objToStr(missionInfo.get("bindid"));
				oldDriverAccount = CoreUtil.objToStr(missionInfo.get("SJZH"));
				applyUserName = CoreUtil.objToStr(missionInfo.get("APPLYUSERNAME"));
				applyUid = CoreUtil.objToStr(missionInfo.get("APPLYUID"));
				applyUserCellPhone = CoreUtil.objToStr(missionInfo.get("APPLYUSERMOBILE"));
				udate = CoreUtil.objToStr(missionInfo.get("USEDATE"));
				driverphone = CoreUtil.objToStr(missionInfo.get("SJLXFS"));
				drivername = CoreUtil.objToStr(missionInfo.get("SJXM"));

				vehicleType = CoreUtil.objToStr(missionInfo.get("VEHICLETYPE"));
				useType = CoreUtil.objToStr(missionInfo.get("USECARTYPE"));
				orderId = CoreUtil.objToStr(missionInfo.get("ORDERID"));
				applyUnit = CoreUtil.objToStr(missionInfo.get("APPLYUNIT"));
				applyUnitId = CoreUtil.objToStr(missionInfo.get("APPLYUNITID"));
				contactPersonAccount = CoreUtil.objToStr(missionInfo.get("CONTACTPERSONZH"));

				resourceTaskId = CoreUtil.objToStr(missionInfo.get("RESOURCETASKID"));
				resourceTaskFpId = CoreUtil.objToStr(missionInfo.get("RESOURCETASKFPID"));
				// IsInnerCar=CoreUtil.objToStr(missionInfo.get("ISINTERNALVEHICLE"));
				// applyDepName=CoreUtil.objToStr(missionInfo.get("APPLYDEPTNAME"));
				// applyDepId=CoreUtil.objToStr(missionInfo.get("APPLYDEPTID"));

				getOnPlace = CoreUtil.objToStr(missionInfo.get("BOARDINGPLACE"));
				targetPlace = CoreUtil.objToStr(missionInfo.get("TARGETPLACE"));
				isOutShangHai = CoreUtil.objToStr(missionInfo.get("ISOUTSHANGHAI"));
				route = CoreUtil.objToStr(missionInfo.get("RUNLINE"));

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

			try {
				System.out.println("准备终止流程！流程号:" + proid + "用户ID:" + userId);
				SDK.getProcessAPI().terminateById(proid, userId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String message_driver = "{'applyUserName':'" + drivername + "','udate':'" + udate + "','cph':'" + oldCarNo
					+ "'}";
			sms.sendSms(driverphone, MnmsConstant.SHSY_CACELMISSION_NOTIFYTODRIVER, message_driver);

			String updateMissionSql = "update BO_EU_YBBZUSECAR_MISSION set MISSIONSTATUS='6' where bindid='"
					+ proid + "'";
			String updateAssignSql = "update BO_EU_YBOFFICEUSECAR_DS set ZT='2',MISSIONSTATUS='6' where id ='"
					+ id + "'";

			DBSql.update(updateAssignSql);// 修改上航_车辆任务分配状态
			DBSql.update(updateMissionSql);

			createProcessInstance = SDK.getProcessAPI().createProcessInstance("obj_7f2b76f2e0544a0fa0b9891a4061abed",
					newDriverAccount, "保障用车-" + udate + "-" + contactPerson);

			BO boRecordData = new BO();
			boRecordData.set("ORDERID", orderId + SDK.getRuleAPI().executeAtScript("@replace(@date,-)")
					+ SDK.getRuleAPI().executeAtScript("@sequence('" + orderId + "AA@year@month',4,0)"));// 订单号
			boRecordData.set("APPLYUSERNAME", applyUserName);// 预定人
			boRecordData.set("APPLYUID", applyUid);// 预定人账号
			boRecordData.set("APPLYUSERMOBILE", applyUserCellPhone);// 预定人手机
			boRecordData.set("APPLYUNIT", applyUnit);// 用车单位
			boRecordData.set("APPLYUNITID", applyUnitId);// 用车单位ID
			boRecordData.set("CONTACTPERSON", contactPerson);// 用车联系人
			boRecordData.set("CONTACTPERSONZH", contactPersonAccount);// 用车联系人账号
			boRecordData.set("CONTACTPHONE", contactPhone);// 用车联系人手机
			boRecordData.set("USEDATE", udate);// 使用日期
			boRecordData.set("VEHICLETYPE", vehicleType);// 车辆类型

			boRecordData.set("WZUNITPSN", "");// 外租公司调度
			boRecordData.set("WZUNITPSNID", "");// 外租公司调度ID
			boRecordData.set("WZUNITPSNPHONE", "");// 外租公司联系方式
			boRecordData.set("RESOURCETASKID", resourceTaskId);// 来源预订单ID
			boRecordData.set("RESOURCETASKFPID", resourceTaskFpId);// 来源任务分配单ID

			// boRecordData.set("ISINTERNALVEHICLE", IsInnerCar);//车辆属性
			// boRecordData.set("APPLYDEPTNAME", applyDepName);//用车部门
			// boRecordData.set("APPLYDEPTID", applyDepId);//用车部门ID

			boRecordData.set("BOARDINGPLACE", getOnPlace);// 上车地点
			boRecordData.set("TARGETPLACE", targetPlace);// 目的地
			boRecordData.set("ISOUTSHANGHAI", isOutShangHai);// 是否出省

			boRecordData.set("SJXM", newDriverName);// 司机姓名
			boRecordData.set("SJZH", newDriverAccount);// 司机账号
			boRecordData.set("SJLXFS", newDriverPhone);// 司机联系方式
			boRecordData.set("CPH", newCarNo);// 车牌号
			boRecordData.set("VEHICLELABELNAME", newVehicleLabelName);// 车辆品牌
			boRecordData.set("MISSIONSTATUS", "1");
			SDK.getBOAPI().create(CoreUtil.BO_EU_YBBZUSECAR_MISSION, boRecordData, createProcessInstance, UserContext.fromUID(userId));
			SDK.getProcessAPI().start(createProcessInstance);

			DBSql.update("UPDATE BO_EU_YBOFFICEUSECAR_DS SET ZT = '1', MISSIONSTATUS = '1',MISSIONBINDID='"
					+ boRecordData.getId() + "' WHERE ID = '" + id + "'");

			String msg_to_driver = "{'SJXM':'" + newDriverName + "','APPLYUSERNAME':'" + applyUserName
					+ "','APPLYUSERCELLPHONE':'" + applyUserCellPhone + "','UDATE':'" + udate + "','CPH':'" + newCarNo
					+ "','BOARDINGPLACE':'" + getOnPlace + "','TARGETPLACE':'" + targetPlace + "'}";
			String msg_to_user = "{'APPLYUSERNAME':'" + applyUserName + "','UDATE':'" + udate + "','OLDSJXM':'"
					+ drivername + "','NEWSJXM':'" + newDriverName + "','SJLXFS':'" + newDriverPhone + "','OLDCPH':'"
					+ oldCarNo + "','NEWCPH':'" + newCarNo + "'}";

			sms.sendSms(newDriverPhone, MnmsConstant.SHSY_MISSION_NOTIFYTODRIVER, msg_to_driver);
			sms.sendSms(applyUserCellPhone, MnmsConstant.SHSY_MODIFYMISSION_NOTIFYTOUSER, msg_to_user);

			if (!contactPhone.equals("")) {
				sms.sendSms(contactPhone, MnmsConstant.SHSY_MODIFYMISSION_NOTIFYTOUSER, msg_to_user);
			}

			returnData.put("status", "0");
			returnData.put("message", "成功变更");

		} catch (Exception e) {

			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}

}
