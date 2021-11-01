package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ValueListenerInterface;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
/**
 * 
 * @Description:时限的自定义事件  （24小时客户未确认，发提醒给驾驶员）
 * @author: wanghb 
 * @date:   2020年6月19日 下午1:43:52
 */
public class Hours24TaskOverdueReminderEvent implements ValueListenerInterface {

	@Override
	public String getDescription() {
		
		return "24小时客户未确认，发提醒给驾驶员";
	}

	@Override
	public String getProvider() {
		
		return "wanghb";
	}

	@Override
	public String getVersion() {
		
		return "1.0";
	}

	@Override
	public String execute(ProcessExecutionContext processExecutionContext) throws Exception {
		//获取流程实例ID
		String processInstId = processExecutionContext.getProcessInstance().getId();
		//根据流程实例ID获取当前流程数据
		BO missionBoData = SDK.getBOAPI().getByProcess(CoreUtil.MISSION, processInstId);
		String uid = missionBoData.getString("SJZH");
		String userName = missionBoData.getString("SJXM");
		String orderId = missionBoData.getString("ORDERID");
		boolean sendMessage = SDK.getNotificationAPI().sendMessage("admin", uid, "【"+userName+"】您好，您的车辆任务分配订单："+orderId+" 的客户已经超过24小时未确认");
		if(sendMessage) {
			return "发送成功";
		}else {
			return "发送失败,具体原因请联系管理员";
		}
		
	}

}
