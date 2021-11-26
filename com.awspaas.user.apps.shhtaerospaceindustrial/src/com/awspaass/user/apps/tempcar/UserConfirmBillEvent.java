package com.awspaass.user.apps.tempcar;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class UserConfirmBillEvent extends ExecuteListener implements ExecuteListenerInterface {
	public String getDescription() {
        return "用户确认更新订单表状态，更新账单发送短信日志";
    }
	
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			//if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "确认结算单")) {
				String bindId = pec.getProcessInstance().getId();//流程实例ID
				String queryResourceTaskFpId = "SELECT ID,RESOURCETASKFPID FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE BINDID = '"+bindId+"'";
				List<Map<String, Object>> resourceTaskFpIdList = DBSql.query(queryResourceTaskFpId, new ColumnMapRowMapper(), new Object[] {});
				if(resourceTaskFpIdList != null && !resourceTaskFpIdList.isEmpty()) {
					for (int i = 0; i < resourceTaskFpIdList.size(); i++) {
						Map<String, Object> resourceTaskFpIdMap = resourceTaskFpIdList.get(i);
						String resourceTaskFpId = CoreUtil.objToStr(resourceTaskFpIdMap.get("RESOURCETASKFPID"));//来源任务分配单ID
						String id = CoreUtil.objToStr(resourceTaskFpIdMap.get("ID"));//行车任务表记录Id
						DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONBINDID = '"+id+"' WHERE ID = '"+resourceTaskFpId+"'");
						if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "确认结算单")) {
							DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '5' WHERE ID = '"+resourceTaskFpId+"'");
							String delSMSLogSql = "delete from MISSIONSMSLOG where MISSIONID = ' "+ bindId+"'";
							DBSql.update(delSMSLogSql);
						}else if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "退回修改")) {
							DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '3' WHERE ID = '"+resourceTaskFpId+"'");
						}
					}
				}
				
				String delSMSLogSql = "delete from MISSIONSMSLOG where MISSIONID = ' "+ bindId+"'";
				DBSql.update(delSMSLogSql);
			//}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
