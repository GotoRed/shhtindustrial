package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.exception.BPMNError;


public class RepairValidatesStep2 extends InterruptListener implements InterruptListenerInterface {

	public String getDescription() {
        return "报修流程，工程部派单结点填写校验";
    }
	

    public String getProvider() {
        return "Actionsoft";
    }

    public String getVersion() {
        return "1.0";
    }
    
	public boolean execute(ProcessExecutionContext arg) throws Exception {
		// TODO Auto-generated method stub
		String bindId = arg.getProcessInstance().getId();
		BO formData = (BO) arg.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
		String worker=formData.getString("WORKER");
		String workerid=formData.getString("WORKERID");
//		String querywxb = "SELECT WORKER,WORKERID FROM BO_EU_SH_REPAIR WHERE BINDID = '"+bindId+"'";
//		String worker = CoreUtil.objToStr(DBSql.getString(querywxb, "WORKER"));//维修人
//		String workerid = CoreUtil.objToStr(DBSql.getString(querywxb, "WORKERID"));//维修人id
		System.out.println("维修人:"+worker);
		if("".equals(worker) ||"".equals(workerid)) {
			throw new BPMNError("0312","未填写“维修人员”，请填写“维修人员”后在提交");
		}
		return true;
	}
}


