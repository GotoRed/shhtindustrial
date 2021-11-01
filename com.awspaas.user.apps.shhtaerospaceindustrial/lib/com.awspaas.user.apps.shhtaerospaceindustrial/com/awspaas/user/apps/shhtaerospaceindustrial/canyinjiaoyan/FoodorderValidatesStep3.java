package com.awspaas.user.apps.shhtaerospaceindustrial.canyinjiaoyan;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.exception.BPMNError;

public class FoodorderValidatesStep3 extends InterruptListener implements InterruptListenerInterface {

	public String getDescription() {
        return "餐饮预订流程，用餐结算确认结点填写校验";
    }
	

    public String getProvider() {
        return "Actionsoft";
    }

    public String getVersion() {
        return "1.0";
    }
	public boolean execute(ProcessExecutionContext arg) throws Exception {
		// TODO Auto-generated method stub
//		String bindId = arg.getProcessInstance().getId();
//		String querywxb = "SELECT PAYTYPE,TOTALMONEY  FROM BO_EU_SH_FOODORDER WHERE BINDID = '"+bindId+"'";
//		String paytype = CoreUtil.objToStr(DBSql.getString(querywxb, "PAYTYPE"));//结账方式
//		String totalmoney = CoreUtil.objToStr(DBSql.getString(querywxb, "TOTALMONEY"));//消费金额
		 BO boData = (BO) arg.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
	     String paytype=boData.getString("PAYTYPE");//结账方式	
	     String totalmoney=boData.getString("TOTALMONEY");//消费金额
		if("".equals(paytype)||"".equals(totalmoney)){
				throw new BPMNError("0312","未填写“结账方式”和“消费金额”，请填写后在提交");	
			}
		return true;
	}
}