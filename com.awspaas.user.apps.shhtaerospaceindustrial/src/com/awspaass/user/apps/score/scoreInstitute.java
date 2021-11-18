package com.awspaass.user.apps.score;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ValueListener;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.commons.mvc.view.ResponseObject;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.BOAPI;
import com.actionsoft.sdk.local.api.ProcessAPI;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class scoreInstitute  extends ValueListener{
	public String execute(ProcessExecutionContext param) throws Exception {
		String depProcessDefid="obj_f255e8bfefec4434bdd1b8c390004d98";
		String bindid = param.getProcessInstance().getId();
		String unitInSql="select * from BO_EU_MYD_CEPING_YB_UNITJB t whert t.BINDID = '" + bindid +"'";
		List<Map<String, Object>> scoreUnitList = DBSql.query(unitInSql, new ColumnMapRowMapper(), new Object[] {});
		if(scoreUnitList != null && scoreUnitList.size()!=0) {
			for(Map<String, Object> unit : scoreUnitList) {
				String depInName = CoreUtil.objToStr(unit.get("APPLYDEPTNAME"));
				String  userName = CoreUtil.objToStr(unit.get("APPLYUSERNAME"));
				String uniName = CoreUtil.objToStr(unit.get("APPLYUNIT"));
				String peopleNum = CoreUtil.objToStr(unit.get("NEEDCENUM"));
				//ProcessInstance createProcessInstance = SDK.getProcessAPI().createProcessInstance(depProcessDefid,
			}
 
		}
		return null;
	}

}
