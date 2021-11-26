package com.awspaass.user.apps.tempcar;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;


public class BillNotifyEvent extends ExecuteListener implements ExecuteListenerInterface {
	public String getDescription() {
        return "通知用户结算确认！";
    }
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			//if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "确认")) {
				String bindId = pec.getProcessInstance().getId();//流程实例ID
				String queryResourceTaskFpId = "SELECT RESOURCETASKFPID FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE BINDID = '"+bindId+"'";
				List<Map<String, Object>> resourceTaskFpIdList = DBSql.query(queryResourceTaskFpId, new ColumnMapRowMapper(), new Object[] {});
				if(resourceTaskFpIdList != null && !resourceTaskFpIdList.isEmpty()) {
					for (int i = 0; i < resourceTaskFpIdList.size(); i++) {
						Map<String, Object> resourceTaskFpIdMap = resourceTaskFpIdList.get(i);
						String resourceTaskFpId = CoreUtil.objToStr(resourceTaskFpIdMap.get("RESOURCETASKFPID"));//来源任务分配单ID
						if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "提交")) {
							DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '3' WHERE ID = '"+resourceTaskFpId+"'");
						}else if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "确认行车单")) {
							DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '4' WHERE ID = '"+resourceTaskFpId+"'");
						}
					}
				}
				
				
				String missionInfoQuery = "SELECT * FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE MISSIONSTATUS='4' AND BINDID = '"+bindId+"'";
				
				String APPLYUSERNAME = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "APPLYUSERNAME"));//预定人姓名
				String APPLYUSERCELLPHONE = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "APPLYUSERCELLPHONE"));
				String UDATE = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "UDATE"));
				String SJXM = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "SJXM"));
				String CPH	= CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "CPH"));
				
				SmsUtil sms = new SmsUtil();
				String message = "{'APPLYUSERNAME':'"+APPLYUSERNAME+"','UDATE':'"+UDATE+"','SJXM':'"+SJXM+"','CPH':'"+CPH+"'}";
				sms.sendSms(APPLYUSERCELLPHONE, "SMS_228138821", message);
				
				
				String insertMissionSMSLog = "INSERT INTO MISSIONSMSLOG  (MISSIONID,SMSCOUNT)VALUES(:MISSIONID,:SMSCOUNT)";
				 Map<String, Object> paraMap = new HashMap<>();
				 paraMap.put("MISSIONID", bindId);
				 paraMap.put("SMSCOUNT", 1);
				 
				 DBSql.update(insertMissionSMSLog, paraMap);
				
				
		//	}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}