package com.awspaass.user.apps.tempcar;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.math.*;
import java.text.SimpleDateFormat;

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
			String queryMissionSql = "SELECT a.*, b.HONGQIAO,b.PUDONG,b.DAYPRICE,b.DAYOVERKILOMETERSPRICE,b.DAYOVERTIMEPRICE FROM BO_EU_SH_VEHICLEORDER_MISSION a ,BO_EU_SH_VEHICLETYPE b WHERE a.vehicletype=b.vehicletype AND a.vehiclelabelname=b.vehiclelabelname AND a.BINDID = '"+bindId+"'";
			List<Map<String, Object>> missionList = DBSql.query(queryMissionSql, new ColumnMapRowMapper(), new Object[] {});
			if(missionList != null && !missionList.isEmpty()) {
				for (int i = 0; i < missionList.size(); i++) {
					Map<String, Object> MissonInfo = missionList.get(i);
					
					double totalFee=0;
					double totaloverTimeFee=0;
					double totalOverKmFee=0;
					double totalOverKm=0;
					double totalOverTime=0;
					double outFee=0;
					int dayFee = 0;
					double basicFee=(double)CoreUtil.objToInt(MissonInfo.get("DAYPRICE"));
					double hongQiaoFee=(double)CoreUtil.objToInt(MissonInfo.get("HONGQIAO"));
					double puDongFee=(double)CoreUtil.objToInt(MissonInfo.get("PUDONG"));
					double overTimePrice = (double)CoreUtil.objToInt(MissonInfo.get("DAYOVERTIMEPRICE"));//超小时费(元/小时)
					double overKmPrice = (double)CoreUtil.objToInt(MissonInfo.get("DAYOVERKILOMETERSPRICE"));//超公里费(元/公里)
					
					
					String resourceTaskFpId = CoreUtil.objToStr(MissonInfo.get("RESOURCETASKFPID"));//来源任务分配单ID
					String startTime = CoreUtil.objToStr(MissonInfo.get("CFSJ"));//出车时间
					String getOnTime = CoreUtil.objToStr(MissonInfo.get("SKDATETIME"));//上客时间
					String dropOffTime = CoreUtil.objToStr(MissonInfo.get("XKDATIME"));//下客时间
					String retrunTime = CoreUtil.objToStr(MissonInfo.get("FHSJ"));//回场时间
					String isOutShanghai = CoreUtil.objToStr(MissonInfo.get("ISOUTSHANGHAI"));
					

					double roadBridgeFee = (double) CoreUtil.objToInt(MissonInfo.get("GLF"));
					double hotelCost = (double) CoreUtil.objToInt(MissonInfo.get("STAYMONEY"));
					double parkingFee = (double) CoreUtil.objToInt(MissonInfo.get("TCF"));
					double otherFee = (double) CoreUtil.objToInt(MissonInfo.get("QTMONEY"));				
					
					int getOnDistance = CoreUtil.objToInt(MissonInfo.get("SKLMS"));//上客路码
					int dropOffDistance = CoreUtil.objToInt(MissonInfo.get("XKLMS"));//下客路码
					int returnDistance = CoreUtil.objToInt(MissonInfo.get("FHSLMS"));//返场路码
					
					String carType = CoreUtil.objToStr(MissonInfo.get("vehicletype"));//车辆类型
					String carLogo = CoreUtil.objToStr(MissonInfo.get("vehiclelabelname"));//车辆品牌
					
					String billType =  CoreUtil.objToStr(MissonInfo.get("BILLLIST"));
					System.out.println(retrunTime);
					System.out.println(getOnTime);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					long useCarTimeUtc =( format.parse(retrunTime).getTime()- format.parse(getOnTime).getTime());
					int useCarTimeInt = (int)useCarTimeUtc/1000/3600;
					double userCarTimeFloat =(double)useCarTimeUtc/1000/3600;
					double useCarTime = (useCarTimeInt+0.5*Math.ceil(userCarTimeFloat-useCarTimeInt)+0.5*Math.ceil(userCarTimeFloat-useCarTimeInt-0.5));
					int useCarDistance = returnDistance-getOnDistance;
					
					if(billType.equals("2")) {
						dayFee=(int)basicFee;
						if("1".equals(isOutShanghai)) {
							outFee= overTimePrice*5;
						}
						if(useCarTime <= 4){         
							totalFee = basicFee/2;  
			                if(useCarDistance>50){
			                	totalOverKm = useCarDistance-50;
			                	totalOverKmFee = (useCarDistance-50)*(overKmPrice);
			                   
			                }                
			            }else if(useCarTime > 4){
			            	totalFee = basicFee; 
			                if(useCarDistance>100){
			                	totalOverKm = useCarDistance-100;
			                	totalOverKmFee = (useCarDistance-100)*(overKmPrice);
			                }
			                if(useCarTime > 9){
			                	totalOverTime = useCarTime-9;
			                	totaloverTimeFee=totalOverTime*overTimePrice;
			                }
			            }
			            totalFee = totalFee+outFee+totalOverKmFee+totaloverTimeFee+roadBridgeFee+hotelCost+parkingFee+otherFee;

					}else if(billType.equals("1")||billType.equals("0")) {
						if(billType.equals("1")) {
							dayFee=(int)(puDongFee);
							totalFee=puDongFee;
						}else if(billType.equals("0")) {
							dayFee=(int)hongQiaoFee;
							totalFee=(int)hongQiaoFee;
						}
					}
					String missionUpdateSql="UPDATE BO_EU_SH_VEHICLEORDER_MISSION SET QRLC= "+useCarDistance+",DAYPRICE= "+(int)dayFee+",DAYOVERKILOMETERSPRICE= "+totalOverKmFee+",DAYOVERTIMEPRICE= "+totaloverTimeFee+",USECARTIME="+useCarTime+",TOTALMONEY="+totalFee+
							",OVERTIMENUM="+totalOverTime+",OVERERDISTANCENUM="+totalOverKm+",OTTIMEPRICE="+(int)overTimePrice+",OTLEAVERICE="+(int)overKmPrice+",CCGYF="+(int)outFee;
					System.out.println("QRLC"+useCarDistance);
					System.out.println("OVERTIMENUM"+totalOverTime);
					System.out.println("OVERERDISTANCENUM"+totalOverKm);
					System.out.println("totalOverKmFee"+totalOverKmFee);
					System.out.println("totaloverTimeFee"+totaloverTimeFee);
					System.out.println("totalFee"+totalFee);
					System.out.println("useCarTime"+useCarTime);

					DBSql.update(missionUpdateSql);
					
					DBSql.update("UPDATE BO_EU_SH_VEHICLEORDER_ASSIGMIS SET MISSIONSTATUS = '4' WHERE ID = '"+resourceTaskFpId+"'");					
				}
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
