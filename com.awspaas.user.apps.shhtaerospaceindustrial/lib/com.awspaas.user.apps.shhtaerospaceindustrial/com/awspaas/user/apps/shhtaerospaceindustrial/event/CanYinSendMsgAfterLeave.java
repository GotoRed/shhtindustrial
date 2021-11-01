package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.controller.MsgNoticeController;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class CanYinSendMsgAfterLeave  extends ExecuteListener implements ExecuteListenerInterface {
	public String getDescription() {
        return "餐饮确认给用户发消息！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
		boolean flag_ok = SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "预订确认");
		if(flag_ok) {
			try {
				ProcessInstance processInstance = pec.getProcessInstance();
				String bindId = processInstance.getId();//流程实例ID
				String target = pec.getTaskInstance().getTarget();//节点任务办理人
				String orderSql = "select id,applyid,orderid,orderdate,ycdate,area,roomnum,personnum,packagestandard from BO_EU_SH_FOODORDER where bindid='"+bindId+"' ";
				List<Map<String, Object>> dataList = DBSql.query(orderSql, new ColumnMapRowMapper(), new Object[] {});
				if (dataList == null || dataList.isEmpty()) {
					return ;
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
				String personnum = CoreUtil.objToStr(dataMap.get("personnum"));
				String packagestandard = CoreUtil.objToStr(dataMap.get("packagestandard"));
				String packagestandardName = CoreUtil.objToStr(DBSql.getString("select item.cnname packagestandardName from Bo_Act_Dict_Kv_Item item "
						+ "left join BO_ACT_DICT_KV_MAIN main on item.bindid = main.bindid "
						+ "where '"+packagestandard+"'=item.itemno and main.dictkey = 'dinnertype'", "packagestandardName"));
				//发消息
				String msg = "尊敬的"+applyName+"，您好！您已成功预订"+ycdate+"在"+areaName+"的"+roomnum+"房间,用餐人数"+personnum+"，餐标"+packagestandardName+"，如有任何疑问请及时联系18217410090，恭候您的光临.";
				MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), msg,"admin", applyUid, "1", "");
				//发短信
				SmsUtil sms = new SmsUtil();
				String phone = applyMobile;
				String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS_TEMPLATE_ID);
				String param = "{'CONTACTPERSON':'"+applyName+"','YCDATE':'"+ycdate+"','AREA':'"+areaName+"','ROOMNUM':'"+roomnum+"','PERSONNUM':'"+personnum+"','PACKAGESTANDARD':'"+packagestandardName+"','STPHONE':'18217410090'}";
				sms.sendSms(phone,templateId,param);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
