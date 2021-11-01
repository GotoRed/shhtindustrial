/**
 * @Description 任务完成后更新任务分配表中的行车任务单状态
 * @author WU LiHua
 * @date 2020年2月8日 下午3:35:45
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class UpdateRwfpbzt extends ExecuteListener implements ExecuteListenerInterface {

	public String getDescription() {
        return "更新任务分配表中的行车任务单状态！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
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
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
