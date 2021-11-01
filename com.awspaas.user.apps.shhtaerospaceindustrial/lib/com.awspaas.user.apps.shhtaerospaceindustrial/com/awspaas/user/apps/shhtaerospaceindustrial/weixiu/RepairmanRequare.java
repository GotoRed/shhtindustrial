package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.util.DBSql;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class RepairmanRequare extends ExecuteListener {
	  
		public String getDescription() {
	        return "维修师傅维修完毕后，回写维修师傅到数据库";
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
	    	String target = param.getTaskInstance().getTarget();//节点任务办理人id
	        String querySql = "select username from ORGUSER where userid ='"+target+"'";
			String username = CoreUtil.objToStr(DBSql.getString(querySql, "username"));//办理人姓名
	        if(!"".equals(target) && !"".equals(username)) {
	            	String updateSql = "update BO_EU_SH_REPAIR set WORKER = '"+username+"'"+",WORKERID='"
	            			+ target+"' where bindid = '"+bindId+"'";
	    			DBSql.update(updateSql);
	        }
	     
		}

	}