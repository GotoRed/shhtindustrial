package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import java.util.List;
import java.util.Map;

//import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.exception.BPMNError;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.controller.MsgNoticeController;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
/**
 * 
 * @author fanzhenjie
 * 餐饮接收后推送预订成功信息给预定人
 */
public class CanYinSendMsgBeforeLeave extends InterruptListener implements InterruptListenerInterface {
	
	public String getDescription() {
        return "餐饮取消给用户发消息！";
    }
	
	@Override
	public boolean execute(ProcessExecutionContext pec) throws Exception {
		boolean flag_cancel = SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "预订取消");
		if(flag_cancel) {
			try {
				ProcessInstance processInstance = pec.getProcessInstance();
				String bindId = processInstance.getId();//流程实例ID
				String target = pec.getTaskInstance().getTarget();//节点任务办理人
				String orderSql = "select id,applyid,orderid,orderdate,remark,ycdate,area,roomnum,personnum,packagestandard from BO_EU_SH_FOODORDER where bindid='"+bindId+"' ";
				List<Map<String, Object>> dataList = DBSql.query(orderSql, new ColumnMapRowMapper(), new Object[] {});
				if (dataList == null || dataList.isEmpty()) {
					throw new BPMNError("未查询到订单，消息取消通知失败！");
				}
				Map<String, Object> dataMap = dataList.get(0);
				String applyUid = CoreUtil.objToStr(dataMap.get("applyid"));
				String ycdate = CoreUtil.objToStr(dataMap.get("ycdate"));
				String area = CoreUtil.objToStr(dataMap.get("area"));
				String areaName = CoreUtil.objToStr(DBSql.getString("select item.cnname areaname from Bo_Act_Dict_Kv_Item item "
						+ "left join BO_ACT_DICT_KV_MAIN main on item.bindid = main.bindid "
						+ "where substr('"+area+"',0,1)=item.itemno and main.dictkey = 'shdininghallarea'", "areaname"));
				String roomnum = CoreUtil.objToStr(dataMap.get("roomnum"));
				String applyName = SDK.getORGAPI().getUser(applyUid).getUserName();
				String applyMobile = SDK.getORGAPI().getUser(applyUid).getMobile();
//				String personnum = CoreUtil.objToStr(dataMap.get("personnum"));
//				String packagestandard = CoreUtil.objToStr(dataMap.get("packagestandard"));
//				String packagestandardName = CoreUtil.objToStr(DBSql.getString("select item.cnname packagestandardName from Bo_Act_Dict_Kv_Item item "
//						+ "left join BO_ACT_DICT_KV_MAIN main on item.bindid = main.bindid "
//						+ "where '"+packagestandard+"'=item.itemno and main.dictkey = 'dinnertype'", "packagestandardName"));
//				String remark = CoreUtil.objToStr(dataMap.get("remark"));
				//表单数据
//		        BO formData = (BO) pec.getParameter(ListenerConst.FORM_EVENT_PARAM_FORMDATA);
//		        Object REMARK = formData.get("REMARK");
//		        System.out.print("当前用户角色："+ REMARK);
				//发消息
				String msg = "尊敬的"+applyName+"，您好！您预订"+ycdate+"在"+areaName+"的"+roomnum+"房间,因食堂包房已满（原因），无法进行预定。给您带来不便敬请谅解，谢谢！";
				MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), msg,"admin", applyUid, "1", "");
				//发短信
				SmsUtil sms = new SmsUtil();
				String phone = applyMobile;
				String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_FAIL_TEMPLATE_ID);
				String param = "{'CONTACTPERSON':'"+applyName+"','YCDATE':'"+ycdate+"','AREA':'"+areaName+"','ROOMNUM':'"+roomnum+"'}";
				sms.sendSms(phone,templateId,param);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
}
