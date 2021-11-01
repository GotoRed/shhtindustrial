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
 * @Description:表单办理前校验，检查被访单位的接待人是否存在  
 * @author: dingyi 
 * @date:   2021年6月28日 上午9:42:30
 */
public class checkVisitorTaegetValidateEvent implements InterruptListenerInterface{

	@Override
	public String getDescription() {
		
		return "表单办理前校验，被访单位接待人不存在，阻止办理  ";
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
		List<Map<String, Object>> visitmain = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
		List<Map<String, Object>> userinfolist = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE_MX WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});

		if(userinfolist == null || userinfolist.isEmpty()) {
			throw new BPMNError("人员明细为空，请添加人员信息后提交");
		}
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formatDate = sdf.format(date);
		long formatDateL = sdf.parse(formatDate).getTime();
		for (Map<String, Object> map : visitmain) {
			String TARGETMAN = CoreUtil.objToStr(map.get("TARGETMAN"));//被访客姓名
			String TARGETDEPT = CoreUtil.objToStr(map.get("TARGETDEPT"));//被访部门
			String TARGETUNIT = CoreUtil.objToStr(map.get("TARGETUNIT"));//被访单位
			String TARGETMANUID = CoreUtil.objToStr(map.get("TARGETMANUID"));//被访单位
			if(TARGETMANUID!=null && TARGETMANUID.equals("")) {
			String queryuser = "SELECT userid  FROM view_sastorginfo WHERE departmentname like  '%"+TARGETDEPT+"%' AND username like  '%"
					+TARGETMAN+ "%'";
			String userid = CoreUtil.objToStr(DBSql.getString(queryuser, "userid"));//根据部门和姓名查询userid
			if(userid!=null && !userid.equals("")) {
				DBSql.update("UPDATE BO_EU_VISITOR_MANAGE SET TARGETMANUID = '"+userid+"' WHERE BINDID = '"+processInstId+"'");

			}else {
				throw new BPMNError("查询不到被访部门的接待人员！");
			}}
			

			String udate = CoreUtil.objToStr(map.get("INPARTTIME"));//进入园区日期
			long udateL = sdf.parse(udate).getTime();
			if(udateL < formatDateL) {//如果进入日期小于系统日期
				throw new BPMNError("预计来访时间不能小于当前时间！");
			}
			
		}

		return true;
	}
}
