package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;

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
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

/**
 * 
 * @author zhangchunkui
 * 工程部调度派单后给维修师傅发消息提醒
 */
public class RepairSendRepairman extends ExecuteListener implements ExecuteListenerInterface {
	
	public String getDescription() {
        return "工程部调度派单后给维修师傅发消息提醒！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
//		boolean flag = SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "派单");
		if(true) {
			try {
				ProcessInstance processInstance = pec.getProcessInstance();
				String bindId = processInstance.getId();//流程实例ID
//				String createUser = processInstance.getCreateUser();//流程创建人
				String target = pec.getTaskInstance().getTarget();//节点任务办理人
				String orderSql = "select id,sqrq,sqr,bxsj,wxdd,bxnr,sfkfdt,bxrxm,bxdw,processdefid, "
						+ "lxdh,lxfs  from bo_eu_sh_repair where bindid='"+bindId+"' ";

//				String sqr = CoreUtil.objToStr(DBSql.getString(orderSql, "SQR"));//报修人
//				String lxfs = CoreUtil.objToStr(DBSql.getString(orderSql, "LXFS"));//联系方式
//				String sqrq = CoreUtil.objToStr(DBSql.getString(orderSql, "SQRQ"));//报修日期
//				String bxsj = CoreUtil.objToStr(DBSql.getString(orderSql, "BXSJ"));//报修时间
//				String wxdd = CoreUtil.objToStr(DBSql.getString(orderSql, "WXDD"));//维修地点
//				String bxnr = CoreUtil.objToStr(DBSql.getString(orderSql, "BXNR"));//报修内容
				String sfkfdt = CoreUtil.objToStr(DBSql.getString(orderSql, "SFKFDT"));//是否客服代填
//				String bxrxm = CoreUtil.objToStr(DBSql.getString(orderSql, "BXRXM"));//代报修人姓名
//				String lxdh = CoreUtil.objToStr(DBSql.getString(orderSql, "LXDH"));//联系电话
				 List<Map<String, Object>> queryorderList = DBSql.query(orderSql, new ColumnMapRowMapper(), new Object[] {});
				String queryDblr = "select target from wfc_task"
						+ " where processinstid = '"+bindId+"'"; //查询待办理人
	             List<Map<String, Object>> queryDataList = DBSql.query(queryDblr, new ColumnMapRowMapper(), new Object[] {});
	             
				if("1".equals(sfkfdt)) {
					if(queryDataList != null && queryDataList.size() > 0 && queryorderList!= null) {
						Map<String, Object> maporder = queryorderList.get(0);
						String sqrq = CoreUtil.objToStr(maporder.get("sqrq"));//报修日期
						String bxsj = CoreUtil.objToStr(maporder.get("bxsj"));//报修时间
						String wxdd = CoreUtil.objToStr(maporder.get("wxdd"));//维修地点
						String bxnr = CoreUtil.objToStr(maporder.get("bxnr"));//报修内容
						String bxrxm = CoreUtil.objToStr(maporder.get("bxrxm"));//代报修人姓名
						String lxdh = CoreUtil.objToStr(maporder.get("lxdh"));//联系电话
						for (int i = 0; i < queryDataList.size(); i++) {
							Map<String, Object> map = queryDataList.get(i);
							String repairname = CoreUtil.objToStr(map.get("target"));//待维修师傅
							MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), 
									sqrq+" "+bxsj+"提交的报修单，在["+wxdd+"]需维修["+bxnr+"]，联系方式["+bxrxm+":"+lxdh+"]，请及时维修处理!"
									,"admin", repairname, "1", "");
						}
					}
					
				}else {
					if(queryDataList != null && queryDataList.size() > 0 && queryorderList!= null) {
						Map<String, Object> maporder = queryorderList.get(0);
						String sqr = CoreUtil.objToStr(maporder.get("sqr"));//报修人
						String lxfs = CoreUtil.objToStr(maporder.get("lxfs"));//联系方式
						String sqrq = CoreUtil.objToStr(maporder.get("sqrq"));//报修日期
						String bxsj = CoreUtil.objToStr(maporder.get("bxsj"));//报修时间
						String wxdd = CoreUtil.objToStr(maporder.get("wxdd"));//维修地点
						String bxnr = CoreUtil.objToStr(maporder.get("bxnr"));//报修内容
						for (int i = 0; i < queryDataList.size(); i++) {
							Map<String, Object> map = queryDataList.get(i);
							String repairname = CoreUtil.objToStr(map.get("target"));//待维修师傅
							MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), 
									sqrq+" "+bxsj+"提交的报修单，在["+wxdd+"]需维修["+bxnr+"]，联系方式["+sqr+":"+lxfs+"]，请及时维修处理!"
									,"admin", repairname, "1", "");
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
}