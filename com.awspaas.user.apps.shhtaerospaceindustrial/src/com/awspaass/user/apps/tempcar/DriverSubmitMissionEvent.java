package com.awspaass.user.apps.tempcar;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.math.*;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class DriverSubmitMissionEvent extends ExecuteListener implements ExecuteListenerInterface {
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			String bindId = pec.getProcessInstance().getId();//流程实例ID
			String queryMissionSql = "SELECT * FROM BO_EU_SH_VEHICLEORDER_MISSION WHERE BINDID = '"+bindId+"'";
			List<Map<String, Object>> missionList = DBSql.query(queryMissionSql, new ColumnMapRowMapper(), new Object[] {});
			if(missionList != null && !missionList.isEmpty()) {
				for (int i = 0; i < missionList.size(); i++) {
					Map<String, Object> MissonInfo = missionList.get(i);
					String resourceTaskFpId = CoreUtil.objToStr(MissonInfo.get("RESOURCETASKFPID"));//来源任务分配单ID
					String startTime = CoreUtil.objToStr(MissonInfo.get("CFSJ"));//出车时间
					String getOnTime = CoreUtil.objToStr(MissonInfo.get("SKDATETIME"));//上客时间
					String dropOffTime = CoreUtil.objToStr(MissonInfo.get("XKDATIME"));//下客时间
					String retrunTime = CoreUtil.objToStr(MissonInfo.get("FHSJ"));//回场时间
					
					
					float overTimePrice = (float) CoreUtil.objToInt(MissonInfo.get("OTTIMEPRICE"));//超小时费(元/小时)
					float overKmPrice = (float) CoreUtil.objToInt(MissonInfo.get("OTLEAVERICE"));//超公里费(元/公里)
					float roadBridgeFee = (float) CoreUtil.objToInt(MissonInfo.get("GLF"));
			        float hotelCost = (float) CoreUtil.objToInt(MissonInfo.get("STAYMONEY"));
			        float parkingFee = (float) CoreUtil.objToInt(MissonInfo.get("TCF"));
			        float otherFee = (float) CoreUtil.objToInt(MissonInfo.get("QTMONEY"));				
					
					int getOnDistance = CoreUtil.objToInt(MissonInfo.get("RESOURCETASKFPID"));//上客路码
					int dropOffDistance = CoreUtil.objToInt(MissonInfo.get("RESOURCETASKFPID"));//下客路码
					int returnDistance = CoreUtil.objToInt(MissonInfo.get("RESOURCETASKFPID"));//返场路码
					
					String carType = CoreUtil.objToStr(MissonInfo.get("RESOURCETASKFPID"));//车辆类型
					String carLogo = CoreUtil.objToStr(MissonInfo.get("RESOURCETASKFPID"));//车辆品牌
					
					String billType =  CoreUtil.objToStr(MissonInfo.get("BILLLIST"));
					long useCarTimeUtc =( new Date(retrunTime).getTime()- new Date(getOnTime).getTime());
					int useCarTimeInt = (int)useCarTimeUtc/1000/3600;
					double userCarTimeFloat =(double)useCarTimeUtc/1000/3600;
					double useCarTime = (useCarTimeInt+0.5*Math.ceil(userCarTimeFloat-useCarTimeInt)+0.5*Math.ceil(userCarTimeFloat-useCarTimeInt-0.5));
					int useCarDistance = returnDistance-getOnDistance;
					
					if(billType.equals("2")) {
						if(useCarTime <= 4){         
			                totalBasicFee = Number(basicFee)/2;  
			                if(useCarDistance>50){
			                    $("#OVERERDISTANCENUM").val(Number(useCarDistance)-50);
			                    $("#DAYOVERKILOMETERSPRICE").val(Number(useCarDistance-50)*Number(overKmFee));
			                }
			                totalOverKmFee = $("#DAYOVERKILOMETERSPRICE").val();                
			            }else if(useCarTime > 4){
			                totalBasicFee = Number(basicFee); 
			                if(Number(useCarDistance)>100){
			                    $("#OVERERDISTANCENUM").val(Number(useCarDistance)-100);
			                    $("#DAYOVERKILOMETERSPRICE").val(Number(useCarDistance-100)*Number(overKmFee));
			                }
			                totalOverKmFee = $("#DAYOVERKILOMETERSPRICE").val();
			                if(useCarTime > 9){
			                    $("#OVERTIMENUM").val(useCarTime-9);
			                    $("#DAYOVERTIMEPRICE").val(Number(useCarTime-9)*Number(overTimeFee));//超时费                     
			                }
			                totalOverTimeFee = $("#DAYOVERTIMEPRICE").val();
			            }
			            totalFee = Number(totalBasicFee)+Number(totalOverKmFee)+Number(totalOverTimeFee)+Number(totalOtherFee);
			            $("#TOTALMONEY").val(totalFee);
					}else if(billType.equals("1")||billType.equals("0")) {
						
					}

					float basicFee = 0;//基础费用
					float totalOverTime=0;//总超时时间
					float totalOverTimeFee=0;
					float totalOverKmFee=0;
					
					
					

				        

					DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '4' WHERE ID = '"+resourceTaskFpId+"'");					
				}
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
