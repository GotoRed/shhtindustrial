package com.awspaass.user.apps.tempcar;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;

public class DriverSaveMissionEvent extends ExecuteListener  {
	

	    public String getDescription() {
	        return "司机保存";
	    }

	    public String getProvider() {
	        return "Actionsoft";
	    }

	    public String getVersion() {
	        return "1.0";
	    }

	    public void execute(ProcessExecutionContext param) throws Exception {
	    	
	    }
}
