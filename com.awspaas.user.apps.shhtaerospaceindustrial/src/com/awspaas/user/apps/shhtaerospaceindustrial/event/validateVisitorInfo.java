package com.awspaas.user.apps.shhtaerospaceindustrial.event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.exception.BPMNError;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
public class validateVisitorInfo implements InterruptListenerInterface{
	public boolean execute(ProcessExecutionContext processExecutionContext) throws Exception {
		String processInstId = processExecutionContext.getProcessInstance().getId();
		List<Map<String, Object>> userinfolist = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE_MX WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
		if(userinfolist == null || userinfolist.isEmpty()) {
			throw new BPMNError("人员明细为空，请添加人员信息后提交");
		}
		
		for (Map<String, Object> map : userinfolist) {
			String VISITORNAME = CoreUtil.objToStr(map.get("VISITORNAME"));
			String VISITORCELL = CoreUtil.objToStr(map.get("VISITORCELL"));
			
			String CERTNO = CoreUtil.objToStr(map.get("CERTNO"));
			String ABOUTFILE = CoreUtil.objToStr(map.get("ABOUTFILE"));
			
			if(ABOUTFILE=="") {
				
				throw new BPMNError("请确认访客："+VISITORNAME+"的证件照是否上传！");
			}
			}
			
		
		return true;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}
}
