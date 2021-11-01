package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.exception.BPMNError;

public class RepairValidatesStep5 extends InterruptListener implements InterruptListenerInterface {

	public String getDescription() {
        return "报修流程，客服回访确认结点填写校验";
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
		String jddate=formData.getString("JDDATE");
		String xlqk=formData.getString("XLQK");
		String sfhf=formData.getString("SFHF");
		String hfryname=formData.getString("HFRYNAME");
		String pjnr=formData.getString("PJNR");
//		String querywxb = "SELECT WORKER,WORKERID,JDDATE,XLQK,SFHF,HFRYNAME,PJNR FROM BO_EU_SH_REPAIR WHERE BINDID = '"+bindId+"'";
//		String worker = CoreUtil.objToStr(DBSql.getString(querywxb, "WORKER"));//维修人
//		String workerid = CoreUtil.objToStr(DBSql.getString(querywxb, "WORKERID"));//维修人id
//		String jddate = CoreUtil.objToStr(DBSql.getString(querywxb, "JDDATE"));//维修时间
//		String xlqk = CoreUtil.objToStr(DBSql.getString(querywxb, "XLQK"));//修理情况
//		String sfhf = CoreUtil.objToStr(DBSql.getString(querywxb, "SFHF"));//是否回访
//		String hfryname = CoreUtil.objToStr(DBSql.getString(querywxb, "HFRYNAME"));//回访人员
//		String pjnr = CoreUtil.objToStr(DBSql.getString(querywxb, "PJNR"));//回访情况
		if("".equals(worker) ||"".equals(workerid)||"".equals(jddate) ||"".equals(xlqk)||
				"".equals(sfhf) ||"".equals(hfryname)||"".equals(pjnr)) {
			throw new BPMNError("0312","请写“维修人员”、“维修时间”、“修理情况”、“是否回访”、"
					+ "“回访人员”、“回访情况”，请填写完在提交");
		}
		return true;
	}
}