package com.awspaass.user.apps.businesscar;
import java.util.List;

import org.apache.logging.log4j.core.util.SystemNanoClock;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CommUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;


public class createAssignEvent extends ExecuteListener implements ExecuteListenerInterface {
	public String getDescription() {
        return "创建保障用车派遣表！";
    }
	public void execute(ProcessExecutionContext pec) throws Exception {
		if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "同意")) {
			String bindId = pec.getProcessInstance().getId();
			String orderInfoQuery = "SELECT APPLYUID,BDATE,EDATE,VEHICLETYPE FROM BO_EU_YBOFFICEUSECAR WHERE BINDID = '"+bindId+"'";
			String bdate = CoreUtil.objToStr(DBSql.getString(orderInfoQuery, "BDATE"));//预订开始日期
			String edate = CoreUtil.objToStr(DBSql.getString(orderInfoQuery, "EDATE"));//预订结束日期
			String vehicleType = CoreUtil.objToStr(DBSql.getString(orderInfoQuery, "VEHICLETYPE"));//车辆类型
			String ApplyUserId = CoreUtil.objToStr(DBSql.getString(orderInfoQuery, "APPLYUID"));//流程创建人
			

			if(!bdate.equals("") && !edate.equals("")) {
				bdate = bdate.substring(0, 10);
				edate = edate.substring(0, 10);
				List<String> days = CommUtil.getDays(bdate, edate);
				for (int i = 0; i < days.size(); i++) {
					String date = days.get(i);
					String missionNumQuery = "SELECT COUNT(1) SL FROM BO_EU_YBOFFICEUSECAR_DS WHERE BINDID = '"+bindId+"' AND UDATE ="
										+ " TO_DATE('"+date+"','yyyy-MM-dd')";
					int sl = CoreUtil.objToInt(DBSql.getInt(missionNumQuery, "SL"));
					if(sl == 0) {
						BO boRecordData = new BO();
						boRecordData.set("BINDID", bindId);//子表BINDID
						boRecordData.set("VEHICLETYPE", vehicleType);//车辆类型
						boRecordData.set("UDATE", date);//使用日期
						SDK.getBOAPI().create(CoreUtil.BO_EU_YBOFFICEUSECAR_DS, boRecordData, bindId, ApplyUserId);
						
					}
					
				}
			}
			
		}
		
		
		
	}

}
