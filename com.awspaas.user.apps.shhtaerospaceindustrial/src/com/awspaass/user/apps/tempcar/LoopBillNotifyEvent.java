package com.awspaass.user.apps.tempcar;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.bpms.schedule.IJob;
import com.actionsoft.bpms.util.DBSql;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class LoopBillNotifyEvent implements IJob{
	public void execute(JobExecutionContext jobExecutionContext)
	        throws JobExecutionException {
		String missionSMSLogListSql = "select a.SMSCOUNT, b.BINDID,b.APPLYUSERNAME,b.APPLYUSERCELLPHONE,b.UDATE,b.SJXM,b.CPH from MISSIONSMSLOG a ,BO_EU_SH_VEHICLEORDER_MISSION b where a.MISSIONID=b.BINDID and b.MISSIONSTATUS=4 and a.SMSCOUNT<3";
		String APPLYUSERNAME = CoreUtil.objToStr(DBSql.getString(missionSMSLogListSql, "APPLYUSERNAME"));//预定人姓名
		String APPLYUSERCELLPHONE = CoreUtil.objToStr(DBSql.getString(missionSMSLogListSql, "APPLYUSERCELLPHONE"));
		String UDATE = CoreUtil.objToStr(DBSql.getString(missionSMSLogListSql, "UDATE"));
		String SJXM = CoreUtil.objToStr(DBSql.getString(missionSMSLogListSql, "SJXM"));
		String CPH	= CoreUtil.objToStr(DBSql.getString(missionSMSLogListSql, "CPH"));
		int SMSCOUNT	= CoreUtil.objToInt(DBSql.getString(missionSMSLogListSql, "SMSCOUNT"));
		SmsUtil sms = new SmsUtil();
		String message = "{'APPLYUSERNAME':'"+APPLYUSERNAME+"','UDATE':'"+UDATE+"','SJXM':'"+SJXM+"','CPH':'"+CPH+"'}";
		try {
			sms.sendSms(APPLYUSERCELLPHONE, "SMS_228138821", message);
			int newCount = SMSCOUNT+1;
			DBSql.update("update MISSIONSMSLOG t set t.SMSCOUNT='"+ newCount+ "'");
		}catch(Exception e){
			e.printStackTrace();			
		}
		
	}
}
