package com.awspaas.user.apps.shhtaerospaceindustrial.canyinjiaoyan;


import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.exception.BPMNError;

public class FoodorderValidatesStep1 extends InterruptListener implements InterruptListenerInterface{
	public String getDescription() {
        return "餐饮预订流程，是否用餐人填写校验";
    }
	

    public String getProvider() {
        return "Actionsoft";
    }

    public String getVersion() {
        return "1.0";
    }
    
	public boolean  execute(ProcessExecutionContext param) throws Exception {
		//记录ID
        String boId = param.getParameterOfString(ListenerConst.FORM_EVENT_PARAM_BOID);
        //BO表记录，注意：该记录的数据如果被修改，将会体现到表单上，修改后不会直接持久化到数据库中
        BO boData = (BO) param.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
		String sfycr=boData.getString("SFYCR");//是否用餐人
		String ycunit=boData.getString("YCUNIT");//用餐人单位
		String ycname=boData.getString("YCNAME");//用餐人姓名
		String yctele=boData.getString("YCTELE");//用餐人电话
		if("0".equals(sfycr)) {
//			if(ycunit.equals("")) {
//				throw new BPMNError("0312","未填写“用餐人单位”、“用餐人姓名”和“用餐人电话”，请填写后在提交");	
//			}
			if("".equals(ycunit)||"".equals(ycname)||"".equals(yctele)){
				throw new BPMNError("0312","未填写“用餐人单位”、“用餐人姓名”和“用餐人电话”，请填写后在提交");	
			}
		}
		return true;
		
	}
	
}
