package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.exception.BPMNError;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

/**
 * 
 * @Description:表单办理前校验，车辆预定流程如果含有为派单的任务，阻止办理  
 * @author: wanghb 
 * @date:   2020年6月19日 上午9:42:30
 */
public class UserScheduledVehiclesBeforeHandlValidateEvent implements InterruptListenerInterface{

	@Override
	public String getDescription() {
		
		return "表单办理前校验，车辆预定流程如果含有未派单的任务，阻止办理  ";
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
	public boolean execute(ProcessExecutionContext processExecutionContext) throws Exception {
		//获取流程实例ID
		String processInstId = processExecutionContext.getProcessInstance().getId();
		//根据流程实例ID获取任务表内数据
		List<Map<String, Object>> userScheduledVehicles = DBSql.query("SELECT * FROM "+CoreUtil.ASSIGMIS +" WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
		if(userScheduledVehicles == null || userScheduledVehicles.isEmpty()) {
			throw new BPMNError("任务为空，请添加任务后提交");
		}
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formatDate = sdf.format(date);
		long formatDateL = sdf.parse(formatDate).getTime();
		for (Map<String, Object> map : userScheduledVehicles) {
			String status = CoreUtil.objToStr(map.get("ZT"));
			String udate = CoreUtil.objToStr(map.get("UDATE"));//用车日期
			long udateL = sdf.parse(udate).getTime();
			if(udateL > formatDateL) {//如果用车日期大于系统日期
				throw new BPMNError("用户全部用车结束后，方能结束此流程！");
			}
			if("0".equals(status)) {
				throw new BPMNError("您有未派单的任务，请派单后提交！");
			}
		}
		return true;
	}
}
