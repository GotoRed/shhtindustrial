package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;



import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ListenerConst;
import com.actionsoft.bpms.util.DBSql;

public class RepairFormAfterSave extends ExecuteListener {
  
	public String getDescription() {
        return "报修申请流程，客户还是客服中心填报维修状态不同";
    }
	

    public String getProvider() {
        return "Actionsoft";
    }

    public String getVersion() {
        return "1.0";
    }

    public void execute(ProcessExecutionContext param) throws Exception {
		//流程id
    	String bindId=param.getProcessInstance().getId();
    	
        //表单数据
        BO formData = (BO) param.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
        Object jsname =formData.get("JSNAME");
        System.out.print("当前用户角色："+ jsname);
        Object sqrzh =formData.get("SQRZH");
        String querySql = "select rolename from ORGROLE where id ='78e93ab0-cb73-4d28-a0e5-57c1b29dda40'";
		String rolename = DBSql.getString(querySql, "rolename");//查询角色名称
        System.out.print("获取系统角色："+ rolename);
        if(!"".equals(rolename) && !"".equals(jsname)) {
        	if(rolename.equals(jsname)) {
            	String updateSql = "update BO_EU_SH_REPAIR set BXZT = 6 where bindid = '"+bindId+"'";
    			DBSql.update(updateSql);
        	}else {
            	String updateSql = "update BO_EU_SH_REPAIR set BXZT = 1 where bindid = '"+bindId+"'";
    			DBSql.update(updateSql);
        	}

        }
     
	}

}
