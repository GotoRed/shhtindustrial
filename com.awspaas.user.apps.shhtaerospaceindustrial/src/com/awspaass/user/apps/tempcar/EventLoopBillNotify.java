package com.awspaass.user.apps.tempcar;

import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.schedule.IJob;
import com.actionsoft.bpms.util.DBSql;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class EventLoopBillNotify implements IJob {
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		String missionSMSLogListSql = "select a.SMSCOUNT, b.BINDID,b.APPLYUSERNAME,b.APPLYUSERCELLPHONE,b.UDATE,b.SJXM,b.CPH from MISSIONSMSLOG a ,BO_EU_SH_VEHICLEORDER_MISSION b where a.MISSIONID=b.BINDID and b.MISSIONSTATUS=4 and a.SMSCOUNT<3";
		List<Map<String, Object>> missionSMSLogList = DBSql.query(missionSMSLogListSql, new ColumnMapRowMapper(),
				new Object[] {});

		if (missionSMSLogList != null && !missionSMSLogList.isEmpty()) {
			for (int i = 0; i < missionSMSLogList.size(); i++) {
				Map<String, Object> smsLog = missionSMSLogList.get(i);
				String APPLYUSERNAME = CoreUtil.objToStr(smsLog.get("APPLYUSERNAME"));// 预定人姓名
				String APPLYUSERCELLPHONE = CoreUtil.objToStr(smsLog.get("APPLYUSERCELLPHONE"));
				String UDATE = CoreUtil.objToStr(smsLog.get("UDATE"));
				String SJXM = CoreUtil.objToStr(smsLog.get("SJXM"));
				String CPH = CoreUtil.objToStr(smsLog.get("CPH"));
				int SMSCOUNT = CoreUtil.objToInt(smsLog.get("SMSCOUNT"));
				SmsUtil sms = new SmsUtil();
				String message = "{'APPLYUSERNAME':'" + APPLYUSERNAME + "','UDATE':'" + UDATE + "','SJXM':'" + SJXM
						+ "','CPH':'" + CPH + "'}";
				try {
					sms.sendSms(APPLYUSERCELLPHONE, "SMS_228138821", message);
					int newCount = SMSCOUNT + 1;
					DBSql.update("update MISSIONSMSLOG t set t.SMSCOUNT='" + newCount + "'");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
}
