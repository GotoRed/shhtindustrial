package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.util.DBSql;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class RepairProcessAfterCompleted extends ExecuteListener  {
	
	public String getDescription() {
        return "报修申请流程第一个节点办理后，客户还是客服中心填报维修状态不同";
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
    	String querySqlzb = "select jsname from BO_EU_SH_REPAIR where bindId ='"+bindId+"'";
		String jsname = CoreUtil.objToStr(DBSql.getString(querySqlzb, "jsname"));//查询角色名
        String querySql = "select rolename from ORGROLE where id ='78e93ab0-cb73-4d28-a0e5-57c1b29dda40'";
		String rolename = CoreUtil.objToStr(DBSql.getString(querySql, "rolename"));//查询角色名称
        System.out.print("获取系统角色："+ rolename);
        if(!"".equals(rolename) && !"".equals(jsname)) {
        	if(rolename.equals(jsname)) {
            	String updateSql = "update BO_EU_SH_REPAIR set BXZT = '6' where bindid = '"+bindId+"'";
    			DBSql.update(updateSql);
        		
        	}else {
            	String updateSql = "update BO_EU_SH_REPAIR set BXZT = '1' where bindid = '"+bindId+"'";
    			DBSql.update(updateSql);
        	}

        }
     
	}

}
