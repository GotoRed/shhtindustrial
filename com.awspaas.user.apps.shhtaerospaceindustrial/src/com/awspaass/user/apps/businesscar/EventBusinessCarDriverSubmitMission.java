package com.awspaass.user.apps.businesscar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class EventBusinessCarDriverSubmitMission extends ExecuteListener implements ExecuteListenerInterface {

	public String getDescription() {
		return "保障用车司机提交行程任务单事件";
	}
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			String bindId = pec.getProcessInstance().getId();// 流程实例ID
			String queryMissionSql = "SELECT a.RESOURCETASKFPID FROM BO_EU_YBBZUSECAR_MISSION WHERE BINID='"+bindId+"'";
			List<Map<String, Object>> missionList = DBSql.query(queryMissionSql, new ColumnMapRowMapper(),
					new Object[] {});
			if (missionList != null && !missionList.isEmpty()) {
				Map<String, Object> MissonInfo = missionList.get(0);
				String RESOURCETASKFPID=CoreUtil.objToStr(MissonInfo.get("RESOURCETASKFPID"));
				DBSql.update("UPDATE BO_EU_YBBZUSECAR_MISSION SET MISSIONSTATUS = '4' WHERE BINDID = '"
						+ bindId + "'");
				DBSql.update("UPDATE BO_EU_YBOFFICEUSECAR_DS SET MISSIONSTATUS = '4' WHERE ID = '"
						+ RESOURCETASKFPID + "'");
			}
			
			
			String missionInfoQuery = "SELECT * FROM BO_EU_YBBZUSECAR_MISSION WHERE MISSIONSTATUS='4' AND BINDID = '"
					+ bindId + "'";

			String APPLYUSERNAME = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "APPLYUSERNAME"));// 预定人姓名
			String APPLYUSERCELLPHONE = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "APPLYUSERMOBILE"));
			String USEDATE = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "USEDATE"));
			String SJXM = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "SJXM"));
			String CPH = CoreUtil.objToStr(DBSql.getString(missionInfoQuery, "CPH"));

			SmsUtil sms = new SmsUtil();
			String message = "{'APPLYUSERNAME':'" + APPLYUSERNAME + "','UDATE':'" + USEDATE + "','SJXM':'" + SJXM
					+ "','CPH':'" + CPH + "'}";
			sms.sendSms(APPLYUSERCELLPHONE, "SMS_228138821", message);

			String insertMissionSMSLog = "INSERT INTO MISSIONSMSLOG  (MISSIONID,SMSCOUNT,TYPE)VALUES(:MISSIONID,:SMSCOUNT,:TYPE)";
			Map<String, Object> paraMap = new HashMap<>();
			paraMap.put("MISSIONID", bindId);
			paraMap.put("SMSCOUNT", 1);
			paraMap.put("TYPE", "2");

			DBSql.update(insertMissionSMSLog, paraMap);
		}catch(Exception e) {
			e.printStackTrace();
			}
	}



}
