package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.controller.MsgNoticeController;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

/**
 * 
 * @Description:任务完成后发送通知给车队调度以及客户（包含车辆信息和驾驶员信息）  
 * @author: wanghb 
 * @date:   2020年6月19日 下午3:13:14
 */
public class TaskCompleteSendNotificationsEvent implements ExecuteListenerInterface{

	@Override
	public String getDescription() {
		
		return "任务完成后发送通知给预订人和调度！";
	}

	@Override
	public String getProvider() {
		
		return "wanghb";
	}

	@Override
	public String getVersion() {
		
		return "1.0";
	}

	@Override
	public void execute(ProcessExecutionContext processExecutionContext) throws Exception {
		//根据流程实例ID获取流程数据
		BO byProcess = SDK.getBOAPI().getByProcess(CoreUtil.MISSION, processExecutionContext.getProcessInstance().getId());
		String bindId = processExecutionContext.getProcessInstance().getId();
		String queryDdy = "SELECT CREATEUSER FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE BINDID = '"+bindId+"'";
		String createUser = CoreUtil.objToStr(DBSql.getString(queryDdy, "CREATEUSER"));
		String target = processExecutionContext.getTaskInstance().getTarget();//节点任务办理人
		String applyUserId = CoreUtil.objToStr(byProcess.getString("APPLYUID"));//预订人账号
		String applyUserName = CoreUtil.objToStr(byProcess.getString("APPLYUSERNAME"));//预订人
		String applyUserPhone = CoreUtil.objToStr(byProcess.getString("APPLYUSERCELLPHONE"));//预定人手机
		String driversName = CoreUtil.objToStr(byProcess.getString("SJXM"));//司机姓名
		String driversPhone = CoreUtil.objToStr(byProcess.getString("SJLXFS"));//司机联系方式
		String cph = CoreUtil.objToStr(byProcess.getString("CPH"));//车牌号
		String userDate = CoreUtil.objToStr(byProcess.getString("UDATE"));//使用日期
		String contactPerson = CoreUtil.objToStr(byProcess.getString("CONTACTPERSON"));//用车联系人
		String contactPhone = CoreUtil.objToStr(byProcess.getString("CONTACTPHONE"));//用车联系人手机
		String wzUnitPsnid = CoreUtil.objToStr(byProcess.getString("WZUNITPSNID"));//外租公司调度ID
		String vehicleType = CoreUtil.objToStr(byProcess.getString("VEHICLETYPE"));//用车类型
		String scdd = CoreUtil.objToStr(byProcess.getString("BOARDINGPLACE"));//上车地点
		String mdd = CoreUtil.objToStr(byProcess.getString("TARGETPLACE"));//目的地
		SmsUtil sms = new SmsUtil();
		if(!driversPhone.equals("")) {
			JSONObject returnData = new JSONObject();
			String phone = driversPhone;
			String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID);
			String param = "{'SJXM':'"+driversName+"','APPLYUSERNAME':'"+applyUserName+"','APPLYUSERCELLPHONE':'"+applyUserPhone+"','UDATE':'"+userDate
							+"','CPH':'"+cph+"','BOARDINGPLACE':'"+scdd+"','TARGETPLACE':'"+mdd+"'}";
			try {
				returnData = sms.sendSms(phone,templateId,param);
				System.out.println("外租车辆预定成功后给司机发送短信消息======="+returnData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!driversPhone.equals("")) {
			driversPhone = "("+driversPhone+")";
		}
		if(!contactPhone.equals("")) {
			contactPhone = "("+contactPhone+")";
		}
		if(!cph.equals("")) {
			cph = "("+cph+")";
		}
		String notification = userDate+"日的用车人是["+contactPerson+contactPhone+"]的外租用车已分派给["+driversName+driversPhone+",车牌号为："+cph+"],请确认!";
		String notificationYd = applyUserName + "您好！您已成功预定"+userDate+"日的车辆，为您服务的司机为"+driversName+driversPhone+"车辆为"+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType)+cph;
		MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), notificationYd, wzUnitPsnid, applyUserId, "1", "");
		MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), notification, wzUnitPsnid, createUser, "1", "");
		//SDK.getNotificationAPI().sendMessage("admin", processExecutionContext.getUserContext().getUID(), "已派出车牌号为："+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "car.vehicle", cph)+"， 司机为:"+driversName+" 联系方式："+driversPhone);
		//SDK.getNotificationAPI().sendMessage("admin", applyUserId, "已派出车牌号为："+SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "car.vehicle", cph)+"， 司机为:"+driversName+" 联系方式："+driversPhone);
		//系统给预订人和司机发消息告知相关信息（预订人：司机，车牌号，时间，联系方式；司机：车牌号，时间，上车地点，用车人信息）
	}
}
