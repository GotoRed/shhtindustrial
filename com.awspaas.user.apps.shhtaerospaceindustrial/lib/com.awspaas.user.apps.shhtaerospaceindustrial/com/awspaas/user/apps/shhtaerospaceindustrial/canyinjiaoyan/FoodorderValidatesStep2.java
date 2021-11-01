package com.awspaas.user.apps.shhtaerospaceindustrial.canyinjiaoyan;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.exception.BPMNError;
import com.actionsoft.sdk.local.SDK;

public class FoodorderValidatesStep2 extends InterruptListener implements InterruptListenerInterface{

	public String getDescription() {
        return "餐饮预订流程，食堂调度接收结点填写校验";
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
//		String querywxb = "SELECT ROOMNUM  FROM BO_EU_SH_FOODORDER WHERE BINDID = '"+bindId+"'";
//		String roomnum = CoreUtil.objToStr(DBSql.getString(querywxb, "ROOMNUM"));//包房号
		//记录ID
        String boId = arg.getParameterOfString(ListenerConst.FORM_EVENT_PARAM_BOID);
        //BO表记录，注意：该记录的数据如果被修改，将会体现到表单上，修改后不会直接持久化到数据库中
        BO boData = (BO) arg.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
        SDK.getTaskAPI().isChoiceActionMenu(bindId, "不同意");
        String roomnum=boData.getString("ROOMNUM");//包房号
        System.out.println("包房号："+roomnum);
		if("".equals(roomnum)) {
		     throw new BPMNError("0312","未填写“包房号”，请填写后在提交");	
		}
		return true;
	}
}