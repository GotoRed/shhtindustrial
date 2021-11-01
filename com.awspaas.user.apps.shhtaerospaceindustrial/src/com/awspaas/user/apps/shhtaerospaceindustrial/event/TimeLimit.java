package com.awspaas.user.apps.shhtaerospaceindustrial.event;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.commons.htmlframework.HtmlPageTemplate;
import com.actionsoft.exception.BPMNError;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TimeLimit extends InterruptListener{

    public boolean execute(ProcessExecutionContext ctx) throws Exception {
    	SimpleDateFormat sdf=new SimpleDateFormat("HH");
    	Integer hour = Integer.valueOf(sdf.format(new Date()));
    	System.out.println("测试中断");
    	if(hour<10) {
    		
    		throw new BPMNError("BIZ001", "PROCESS_BEFORE_CREATED事件模拟抛出业务异常");
    	}else {
    		return true;
    	}
    		
     }
    }
