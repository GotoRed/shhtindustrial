package com.awspaass.user.apps.businesscar;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;


public class missionStatusUpdate extends ExecuteListener implements ExecuteListenerInterface {
	public String getDescription() {
        return "更新任务分配表中的行车任务单状态！";
    }
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
