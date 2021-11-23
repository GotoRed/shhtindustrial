/**
 * @Description 车辆预定到【车队调度接收】节点，需要根据预订开始日期和预订结束日期分割成多条车辆任务分配子表中（以预定开始日期写入）
 * @author WU LiHua
 * @date 2020年2月8日 下午3:35:45
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.event;

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

public class CreateClrwfp extends ExecuteListener implements ExecuteListenerInterface {

	public String getDescription() {
        return "创建车辆任务子表数据！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			
			String bindId = pec.getProcessInstance().getId();
			
			String queryClyuData = "SELECT VEHICLENUM, BDATE,EDATE,VEHICLETYPE,CREATEUSER FROM BO_EU_SH_VEHICLEORDER WHERE BINDID = '"+bindId+"'";
			String bdate = CoreUtil.objToStr(DBSql.getString(queryClyuData, "BDATE"));//预订开始日期
			String edate = CoreUtil.objToStr(DBSql.getString(queryClyuData, "EDATE"));//预订结束日期
			String vehicleType = CoreUtil.objToStr(DBSql.getString(queryClyuData, "VEHICLETYPE"));//车辆类型
			String createUser = CoreUtil.objToStr(DBSql.getString(queryClyuData, "CREATEUSER"));//流程创建人
			int carNeedNum =  CoreUtil.objToInt(DBSql.getString(queryClyuData, "VEHICLENUM"));//用车数量
			System.out.println("用车数量"+carNeedNum);
			
			if(!bdate.equals("")) {
				bdate = bdate.substring(0, 10);
			}
			if(!edate.equals("")) {
				edate = edate.substring(0, 10);
			}
			if(!bdate.equals("") && !edate.equals("")) {
				List<String> days = CommUtil.getDays(bdate, edate);
				for (int i = 0; i < days.size(); i++) {//获取两个日期中间的所有日期
					for(int j = 0 ;j < carNeedNum; j++){
						String date = days.get(i);
						String querySfcz = "SELECT COUNT(1) SL FROM BO_EU_SH_VEHICLEORDER_ASSIGMIS WHERE BINDID = '"+bindId+"' AND UDATE ="
											+ " TO_DATE('"+date+"','yyyy-MM-dd')";
						int sl = CoreUtil.objToInt(DBSql.getInt(querySfcz, "SL"));//根据流程实例ID和使用日期是否已经存在
						System.out.println("单日已经创建订单数目！"+sl);
						if(sl < carNeedNum) {
							BO boRecordData = new BO();
							boRecordData.set("BINDID", bindId);//子表BINDID
							boRecordData.set("VEHICLETYPE", vehicleType);//车辆类型
							boRecordData.set("UDATE", date);//使用日期
							SDK.getBOAPI().create(CoreUtil.ASSIGMIS, boRecordData, bindId, createUser);
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
