package com.awspaas.user.apps.shhtaerospaceindustrial.event;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListener;
import com.actionsoft.bpms.bpmn.engine.listener.InterruptListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.exception.BPMNError;
import com.actionsoft.sdk.local.SDK;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.exceptions.ClientException;
import com.alibaba.fastjson.*;

public class NotifyVistor  extends ExecuteListener implements ExecuteListenerInterface  {
	

	
	
	  public void execute(ProcessExecutionContext ctx) throws Exception {

		  boolean flag_em_submit = SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "提交");
		  
		  boolean flag_em_comfirm=SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "确认");
		  
		 // if(flag_em_submit||flag_em_comfirm) {
		  if(true) {
			  String car_sms_str[]= {"carnos","carno2","carno3"};
			  String doorno_sms_str[]= {"doorno","doorno2","doorno3"};
			  String permitdates_sms_str[]= {"permitdates","permitdate2","permitdate3"};
			  String sms_template[]= {"SMS_227258595","SMS_227263575","SMS_227248755"};
			  int  door=3;
			  boolean isOneday=true;
			  boolean isDepartment=false;
			  boolean haveCar=false;
				  String processInstId= ctx.getProcessInstance().getId();
				  String carinfo_str="";
			        List<Map<String, Object>> visitorinfo = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE_MX WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
			        List<Map<String,Object>> employeeinfo = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
			        List<Map<String,Object>> carinfo = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE_CARMX WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
			        
			        String carnos ="";
			        String indate="";
			        String outdate="";
			        String sms_code=null;
					if(employeeinfo == null || employeeinfo.isEmpty()) {
						throw new BPMNError("无接待人员信息");
					}
					if(visitorinfo == null || visitorinfo.isEmpty()) {
						throw new BPMNError("无访客信息");
					}
					if(carinfo.size()>0){
						haveCar=true;
						int i= 0;
						//for(Map<String, Object> map : carinfo) {
						for(i =0;i<3&&i<carinfo.size();i++) {
							String carno = CoreUtil.objToStr(carinfo.get(i).get("CARNO"));
							String instr = CoreUtil.objToStr(carinfo.get(i).get("INPARKDATE"));
							indate = instr.substring(0,instr.lastIndexOf(" "));	
							
							String outstr = CoreUtil.objToStr(carinfo.get(i).get("OUTPARKDATE"));
							outdate = outstr.substring(0,outstr.lastIndexOf(" "));
							String time_in="";
							if(indate.equals(outdate)) {
								time_in=indate;
							}else {
								time_in= indate+"至"+outdate;
							}
							 
							String doorno = CoreUtil.objToStr(carinfo.get(i).get("INOUTDOOR"));
							if("园区三号门".equals(doorno)) {
								door=3;
							}else if(("园区一号门").equals(doorno)) {
								door=1;
							}
							
							//carinfo_str+=carno+"入园时间："+indate+",出园时间:"+outdate+"，入园门号："+door;
							carinfo_str+="','"+car_sms_str[i]+"':'"+carno+"','"+doorno_sms_str[i]+"':'"+doorno+"','"+permitdates_sms_str[i]+"':'"+time_in;

						}
						if(i>=1) {
							sms_code=sms_template[i-1];
						}
						
					}else {
						haveCar=false;
					}
					Map<String, Object> employee = employeeinfo.get(0);
					String e_name =  CoreUtil.objToStr(employee.get("TARGETMAN"));
					String e_unit = CoreUtil.objToStr(employee.get("TARGETUNIT"));
					if("上海航天技术研究院".equals(e_unit)) {
						isDepartment=true;
					}else {
						isDepartment=false;
					}
					String visittype=CoreUtil.objToStr(employee.get("VISITTYPE"));
					if("单日".equals(visittype)) {
						isOneday=true;
					}else {
						isOneday=false;
					}
					String e_department = CoreUtil.objToStr(employee.get("TARGETDEPT"));
					String e_date = CoreUtil.objToStr(employee.get("UPDATEDATE"));
					String date = e_date.substring(0,e_date.lastIndexOf(":"));
					String e_phone = CoreUtil.objToStr(employee.get("TARGETMANPHONE"));

					
					
					SmsUtil sms = new SmsUtil();
					System.out.println("用户节点批准：-----------");
					System.out.println(isOneday?"单日":"短期");
					System.out.println(door==1?"一号门":"三号门");
					System.out.println(haveCar?"有车":"无车");
					if(flag_em_submit||flag_em_comfirm) {
						if((isOneday==true)&&(haveCar==false)||(isOneday==true&&door==3)){
							if(haveCar==false) {
								
								for (Map<String, Object> map : visitorinfo) {
									
									String v_name = CoreUtil.objToStr(map.get("VISITORNAME"));
									String message = "{'VISITORNAME':'"+v_name+"','date':'"+date+"','unitname':'"+e_unit+"','deptname':'"+e_department+"','psnname':'"+e_name+"','mobilephone':'"+e_phone+"'}";
									System.out.println(message);
									String v_phone = CoreUtil.objToStr(map.get("VISITORCELL"));
									sms.sendSms(v_phone, "SMS_227253695",message);
								}
							}else {
								
								
								
								
								
								for (Map<String, Object> map : visitorinfo) {
									
									String v_name = CoreUtil.objToStr(map.get("VISITORNAME"));
									String message = "{'VISITORNAME':'"+v_name+"','date':'"+date+"','unitname':'"+e_unit+"','deptname':'"+e_department+"','psnname':'"+e_name+"','mobilephone':'"+e_phone+carinfo_str+"'}";
									System.out.println(message);
									String v_phone = CoreUtil.objToStr(map.get("VISITORCELL"));
									sms.sendSms(v_phone, sms_code,message);
								}
							}			
		
						}
					}
					if(SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "终止")) {
						String message = "{'date':'"+date+"','unitname':'"+e_unit+"','psnname':'"+e_name+"','mobilephone':'"+e_phone+"'}";
						for (Map<String, Object> map : visitorinfo) {
							
							String v_name = CoreUtil.objToStr(map.get("VISITORNAME"));
							String v_phone = CoreUtil.objToStr(map.get("VISITORCELL"));
							sms.sendSms(v_phone, "SMS_227260006",message);
						}
					  }
					
					
				return;
			}
		  //boolean flag_company_ok = SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "同意");

		  
		  
		  
		  /*
		   * 				
				HashMap<String, Object> e_info = new HashMap<>();
				
				e_info.put("date", date);
				e_info.put("unitname", e_unit);
				e_info.put("deptname", e_department);
				e_info.put("psnname", e_name);
				e_info.put("mobilephone", e_phone);
		  flag_ok=SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "确认");
		  boolean flag_submit=SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "提交");
		  System.out.println("第一步审核！");
		  if(flag_ok||flag_submit){
			  String processInstId= ctx.getProcessInstance().getId();
		        List<Map<String, Object>> visitorinfo = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE_MX WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
		        List<Map<String,Object>> employeeinfo = DBSql.query("SELECT * FROM BO_EU_VISITOR_MANAGE WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
				if(employeeinfo == null || employeeinfo.isEmpty()) {
					throw new BPMNError("无接待人员信息");
				}
				if(visitorinfo == null || visitorinfo.isEmpty()) {
					throw new BPMNError("无访客信息");
				}
				Map<String, Object> employee = employeeinfo.get(0);
				String visittype=CoreUtil.objToStr(employee.get("VISITTYPE"));
				if("单日".equals(visittype)==false) {
					return;
				}
				System.out.println("单日只需员工审核");
				String e_name =  CoreUtil.objToStr(employee.get("TARGETMAN"));
				String e_unit = CoreUtil.objToStr(employee.get("TARGETUNIT"));
				String e_department = CoreUtil.objToStr(employee.get("TARGETDEPT"));
				String e_date = CoreUtil.objToStr(employee.get("UPDATEDATE"));
				String e_phone = CoreUtil.objToStr(employee.get("TARGETMANPHONE"));
				HashMap<String, Object> e_info = new HashMap<>();
				String date = e_date.substring(0,e_date.lastIndexOf(":"));
				e_info.put("date", date);
				e_info.put("unitname", e_unit);
				e_info.put("deptname", e_department);
				e_info.put("psnname", e_name);
				e_info.put("mobilephone", e_phone);
				String message = "{'date':'"+date+"','unitname':'"+e_unit+"','deptname':'"+e_department+"','psnname':'"+e_name+"','mobilephone':'"+e_phone+"'}";
				SmsUtil sms = new SmsUtil();
				for (Map<String, Object> map : visitorinfo) {
					
					String v_name = CoreUtil.objToStr(map.get("VISITORNAME"));
					String v_phone = CoreUtil.objToStr(map.get("VISITORCELL"));
					sms.sendSms(v_phone, "SMS_226505539",message);
				}
				return;
		  }
		  */
	      


	    }
}

