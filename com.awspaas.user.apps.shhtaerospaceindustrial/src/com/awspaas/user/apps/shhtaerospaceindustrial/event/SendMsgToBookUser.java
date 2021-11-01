/**
 * @Description 流程结束后推送预订成功信息以及具体内容（客房信息|会议室信息|餐饮信息）给预定人
 * @author WU LiHua
 * @date 2020年2月8日 下午3:35:45
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.event;

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
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.controller.MsgNoticeController;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class SendMsgToBookUser extends ExecuteListener implements ExecuteListenerInterface {

	public String getDescription() {
        return "推送预订成功信息以及具体内容给预定人！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			ProcessInstance processInstance = pec.getProcessInstance();
			String bindId = processInstance.getId();//流程实例ID
			String createUser = processInstance.getCreateUser();//流程创建人
			String userName = SDK.getORGAPI().getUserNames(createUser);//预定人姓名
			String target = pec.getTaskInstance().getTarget();//节点任务办理人
			String queryYdlx = "SELECT ORDERTYPE,CANCELREASON,APPLYUSERCELLPHONE FROM BO_EU_SH_JLCENTER_TYORDERHEAD WHERE BINDID = '"+bindId+"'";
			List<Map<String, Object>> dataInfo = DBSql.query(queryYdlx, new ColumnMapRowMapper(), new Object[] {});
			String orderType = "";
			String orderCancel = "";
			String applyUserPhone = "";
			if(dataInfo != null && !dataInfo.isEmpty()) {
				for (int i = 0; i < dataInfo.size(); i++) {
					Map<String, Object> dataInfoMap = dataInfo.get(i);
					orderType = CoreUtil.objToStr(dataInfoMap.get("ORDERTYPE"));//预订类型
					orderCancel = CoreUtil.objToStr(dataInfoMap.get("CANCELREASON"));//预订失败原因
					applyUserPhone = CoreUtil.objToStr(dataInfoMap.get("APPLYUSERCELLPHONE"));//预定人手机
				}
			}
			String message = "";
			if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "预订确认")) {
				StringBuffer sbf = new StringBuffer();
				if(orderType.contains("0")) {//0:客房|1:会议室|2:餐饮
					String queryKf = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd') EDATE,(CASE WHEN ROOMTYPE='1' "
									+ "THEN '标准大床房' WHEN ROOMTYPE='4' THEN '标准双床房' ELSE '' END) KFXX,CONCAT(ORDERNUM,'间') ORDERNUM FROM "
									+ "BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"'";
					List<Map<String, Object>> kfqkList = DBSql.query(queryKf, new ColumnMapRowMapper(), new Object[] {});
					if(kfqkList == null || kfqkList.isEmpty()) {
						message = message + "";
					}else {
						sbf.append("住宿：");
						for (int i = 0; i < kfqkList.size(); i++) {
							Map<String, Object> kfqkMap = kfqkList.get(i);
							String bdate = CoreUtil.objToStr(kfqkMap.get("BDATE"));//抵店日期
							if(!bdate.equals("")) {
								sbf.append(bdate).append("抵店，");
							}
							String edate = CoreUtil.objToStr(kfqkMap.get("EDATE"));//离店日期
							if(!edate.equals("")) {
								sbf.append(edate).append("退房，");
							}
							String kflx = CoreUtil.objToStr(kfqkMap.get("KFXX"));//客房类型
							String kfsl = CoreUtil.objToStr(kfqkMap.get("ORDERNUM"));//客房数量
							if(!kflx.equals("") && !kfsl.equals("")) {
								sbf.append(kfsl).append(kflx).append(";");
							}
						}
					}
				}
				if(orderType.contains("2")) {
					String queryDcxx = "SELECT TO_CHAR(EATTINGSTARTDATE,'yyyy-MM-dd HH24:mi') EATTINGSTARTDATE,ROOMNUM,CONCAT(PERSONNUM,"
										+ "'位') PERSONNUM,(CASE WHEN PACKAGESTANDARD='0' THEN '桌餐人均100' WHEN PACKAGESTANDARD='1' THEN "
										+ "'桌餐人均120' WHEN PACKAGESTANDARD='2' THEN '桌餐人均150' WHEN PACKAGESTANDARD='3' THEN "
										+ "'自助餐70元/人起' ELSE '' END) PACKAGESTANDARD FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = "
										+ "'"+bindId+"'";
					List<Map<String, Object>> dcqkList = DBSql.query(queryDcxx, new ColumnMapRowMapper(), new Object[] {});
					if(dcqkList == null || dcqkList.isEmpty()) {
						message = sbf.substring(0, sbf.length()-1);
					}else {
						sbf.append("用餐：");
						for (int i = 0; i < dcqkList.size(); i++) {
							Map<String, Object> dcqkMap = dcqkList.get(i);
							String bdate = CoreUtil.objToStr(dcqkMap.get("EATTINGSTARTDATE"));//用餐开始时间
							if(!bdate.equals("")) {
								sbf.append(bdate).append(",");
							}
							String ycdd = CoreUtil.objToStr(dcqkMap.get("ROOMNUM"));//用餐地点
							if(ycdd.contains("1")) {
								sbf.append("大厅自助餐").append(",");
							}
							if(ycdd.contains("2")) {
								sbf.append("小包7").append(",");
							}
							if(ycdd.contains("3")) {
								sbf.append("小包6").append(",");
							}
							if(ycdd.contains("4")) {
								sbf.append("小包5").append(",");
							}
							if(ycdd.contains("5")) {
								sbf.append("中包").append(",");
							}
							if(ycdd.contains("6")) {
								sbf.append("大包").append(",");
							}
							String ycrs = CoreUtil.objToStr(dcqkMap.get("PERSONNUM"));//用餐人数
							if(!ycrs.equals("")) {
								sbf.append(ycrs).append(",");
							}
							String ycbz = CoreUtil.objToStr(dcqkMap.get("PACKAGESTANDARD"));//用餐标准
							if(!ycbz.equals("")) {
								sbf.append(ycbz).append(";");
							}
						}
					}
				}
				if(orderType.contains("1")) {
					String queryHyqk = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd HH24:mi') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd HH24:mi') EDATE,"
									+ "MEETINGROOM FROM BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"'";
					List<Map<String, Object>> hyqkList = DBSql.query(queryHyqk, new ColumnMapRowMapper(), new Object[] {});
					if(hyqkList == null || hyqkList.isEmpty()) {
						message = sbf.substring(0, sbf.length()-1);
					}else {
						sbf.append("会议：");
						for (int i = 0; i < hyqkList.size(); i++) {
							Map<String, Object> hyqkMap = hyqkList.get(i);
							String bdate = CoreUtil.objToStr(hyqkMap.get("BDATE"));//会议开始日期
							if(!bdate.equals("")) {
								sbf.append(bdate).append("至");
							}
							String edate = CoreUtil.objToStr(hyqkMap.get("EDATE"));//会议结束日期
							if(!edate.equals("")) {
								sbf.append(edate).append(",");
							}
							String roomName = CoreUtil.objToStr(hyqkMap.get("MEETINGROOM"));//会议室
							if(!roomName.equals("")) {
								sbf.append(roomName).append(";");
							}
						}
					}
				}
				message = "尊敬的"+userName+"，您好！您的订单已预定成功，具体信息请核对：" + sbf.substring(0, sbf.length()-1)+";交流中心地址/电话：上海闵行区中春路1368号/021-24099900";
				MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), message, target, createUser, "1", "");
				if(orderType.equals("0")) {//0:客房
					String queryZfxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd') EDATE,ORDERNUM,(CASE WHEN"
									+ " ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='4' THEN '标准双床房' ELSE '' END) ROOMTYPE FROM "
									+ "BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> zfxxList = DBSql.query(queryZfxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String roomType = "";
					String orderNum = "";
					if(zfxxList != null && !zfxxList.isEmpty()) {
						Map<String, Object> zfxxMap = zfxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//抵店日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//离店日期
						roomType = CoreUtil.objToStr(zfxxMap.get("ROOMTYPE"));//客房类型
						orderNum = CoreUtil.objToStr(zfxxMap.get("ORDERNUM"));//客房数量
					}
					SmsUtil sms = new SmsUtil();
					if(!applyUserPhone.equals("") && zfxxList != null && !zfxxList.isEmpty()) {
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS100_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','BDATE':'"+bdate+"','EDATE':'"+edate+"','ORDERNUM':'"+orderNum+"','ROOMTYPE':'"+roomType+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定住宿短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定住宿短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("1")) {//1:会议室 
					String queryHyxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd HH24:mi') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd HH24:mi') EDATE,MEETINGROOM FROM "
										+ "BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> hyxxList = DBSql.query(queryHyxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String meetingRoom = "";
					if(hyxxList != null && !hyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = hyxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//会议开始日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//会议结束日期
						meetingRoom = CoreUtil.objToStr(zfxxMap.get("MEETINGROOM"));//会议室
					}
					SmsUtil sms = new SmsUtil();
					if(!applyUserPhone.equals("") && hyxxList != null && !hyxxList.isEmpty()) {
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS001_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','MEETBDATE':'"+bdate+"','MEETEDATE':'"+edate+"','MEETINGROOM':'"+meetingRoom+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定会议室短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定会议室短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("2")) {//2:餐饮
					StringBuffer sbuf = new StringBuffer();
					String queryYcxx = "SELECT TO_CHAR(EATTINGSTARTDATE,'yyyy-MM-dd HH24:mi') EATTINGSTARTDATE,ROOMNUM,PERSONNUM,"
									+ "(CASE WHEN PACKAGESTANDARD='0' THEN '桌餐人均100' WHEN PACKAGESTANDARD='1' THEN '桌餐人均120' WHEN "
									+ "PACKAGESTANDARD='2' THEN '桌餐人均150' WHEN PACKAGESTANDARD='3' THEN '自助餐70元/人起' ELSE '' END)"
									+ " PACKAGESTANDARD FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"' ORDER BY EATTINGSTARTDATE";
					List<Map<String, Object>> ycxxList = DBSql.query(queryYcxx, new ColumnMapRowMapper(), new Object[] {});
					String eattingStartDate = "";
					String roomNum = "";
					String personNum = "";
					String packagestAndard = "";
					if(ycxxList != null && !ycxxList.isEmpty()) {
						Map<String, Object> zfxxMap = ycxxList.get(0);
						eattingStartDate = CoreUtil.objToStr(zfxxMap.get("EATTINGSTARTDATE"));//用餐开始时间
						roomNum = CoreUtil.objToStr(zfxxMap.get("ROOMNUM"));//用餐地点
						personNum = CoreUtil.objToStr(zfxxMap.get("PERSONNUM"));//用餐人数
						packagestAndard = CoreUtil.objToStr(zfxxMap.get("PACKAGESTANDARD"));//用餐标准
						if(roomNum.contains("1")) {
							sbuf.append("大厅自助餐").append(",");
						}
						if(roomNum.contains("2")) {
							sbuf.append("小包7").append(",");
						}
						if(roomNum.contains("3")) {
							sbuf.append("小包6").append(",");
						}
						if(roomNum.contains("4")) {
							sbuf.append("小包5").append(",");
						}
						if(roomNum.contains("5")) {
							sbuf.append("中包").append(",");
						}
						if(roomNum.contains("6")) {
							sbuf.append("大包").append(",");
						}
						roomNum = sbuf.substring(0, sbuf.length()-1);
					}
					SmsUtil sms = new SmsUtil();
					if(!applyUserPhone.equals("") && ycxxList != null && !ycxxList.isEmpty()) {
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS010_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','EATTINGSTARTDATE':'"+eattingStartDate+"','ROOMNUM':'"+roomNum+"','PERSONNUM':'"+personNum+"','PACKAGESTANDARD':'"+packagestAndard+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定用餐短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定用餐短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("1,2")) {//1:会议室|2:餐饮
					StringBuffer sbuf = new StringBuffer();
					String queryYcxx = "SELECT TO_CHAR(EATTINGSTARTDATE,'yyyy-MM-dd HH24:mi') EATTINGSTARTDATE,ROOMNUM,PERSONNUM,"
							+ "(CASE WHEN PACKAGESTANDARD='0' THEN '桌餐人均100' WHEN PACKAGESTANDARD='1' THEN '桌餐人均120' WHEN "
							+ "PACKAGESTANDARD='2' THEN '桌餐人均150' WHEN PACKAGESTANDARD='3' THEN '自助餐70元/人起' ELSE '' END)"
							+ " PACKAGESTANDARD FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"' ORDER BY EATTINGSTARTDATE";
					List<Map<String, Object>> yHyxxList = DBSql.query(queryYcxx, new ColumnMapRowMapper(), new Object[] {});
					String eattingStartDate = "";
					String roomNum = "";
					String personNum = "";
					String packagestAndard = "";
					if(yHyxxList != null && !yHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = yHyxxList.get(0);
						eattingStartDate = CoreUtil.objToStr(zfxxMap.get("EATTINGSTARTDATE"));//用餐开始时间
						roomNum = CoreUtil.objToStr(zfxxMap.get("ROOMNUM"));//用餐地点
						personNum = CoreUtil.objToStr(zfxxMap.get("PERSONNUM"));//用餐人数
						packagestAndard = CoreUtil.objToStr(zfxxMap.get("PACKAGESTANDARD"));//用餐标准
						if(roomNum.contains("1")) {
							sbuf.append("大厅自助餐").append(",");
						}
						if(roomNum.contains("2")) {
							sbuf.append("小包7").append(",");
						}
						if(roomNum.contains("3")) {
							sbuf.append("小包6").append(",");
						}
						if(roomNum.contains("4")) {
							sbuf.append("小包5").append(",");
						}
						if(roomNum.contains("5")) {
							sbuf.append("中包").append(",");
						}
						if(roomNum.contains("6")) {
							sbuf.append("大包").append(",");
						}
						roomNum = sbuf.substring(0, sbuf.length()-1);
					}
					String queryHyxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd HH24:mi') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd HH24:mi') EDATE,MEETINGROOM FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> queryHyxxList = DBSql.query(queryHyxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String meetingRoom = "";
					if(queryHyxxList != null && !queryHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = queryHyxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//会议开始日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//会议结束日期
						meetingRoom = CoreUtil.objToStr(zfxxMap.get("MEETINGROOM"));//会议室
					}
					SmsUtil sms = new SmsUtil();
					if(!applyUserPhone.equals("") && queryHyxxList != null && !queryHyxxList.isEmpty() && yHyxxList != null && !yHyxxList.isEmpty()) {
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS011_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','EATTINGSTARTDATE':'"+eattingStartDate+"','ROOMNUM':'"+roomNum+"',"
										+ "'PERSONNUM':'"+personNum+"','PACKAGESTANDARD':'"+packagestAndard+"','MEETBDATE':'"+bdate+"',"
										+ "'MEETEDATE':'"+edate+"','MEETINGROOM':'"+meetingRoom+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定会议室+用餐短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定会议室+用餐短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("0,1")) {//0:客房|1:会议室
					String queryZfxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd') EDATE,ORDERNUM,(CASE WHEN"
							+ " ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='4' THEN '标准双床房' ELSE '' END) ROOMTYPE FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> zfxxList = DBSql.query(queryZfxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String roomType = "";
					String orderNum = "";
					if(zfxxList != null && !zfxxList.isEmpty()) {
						Map<String, Object> zfxxMap = zfxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//抵店日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//离店日期
						roomType = CoreUtil.objToStr(zfxxMap.get("ROOMTYPE"));//客房类型
						orderNum = CoreUtil.objToStr(zfxxMap.get("ORDERNUM"));//客房数量
					}
					String queryHyxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd HH24:mi') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd HH24:mi') EDATE,MEETINGROOM FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> queryHyxxList = DBSql.query(queryHyxx, new ColumnMapRowMapper(), new Object[] {});
					String mbdate = "";
					String medate = "";
					String meetingRoom = "";
					if(queryHyxxList != null && !queryHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = queryHyxxList.get(0);
						mbdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//会议开始日期
						medate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//会议结束日期
						meetingRoom = CoreUtil.objToStr(zfxxMap.get("MEETINGROOM"));//会议室
					}
					if(!applyUserPhone.equals("") && zfxxList != null && !zfxxList.isEmpty() && queryHyxxList != null && !queryHyxxList.isEmpty()) {
						SmsUtil sms = new SmsUtil();
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS101_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','BDATE':'"+bdate+"','EDATE':'"+edate+"','ORDERNUM':'"+orderNum+"',"
										+ "'ROOMTYPE':'"+roomType+"','MEETBDATE':'"+mbdate+"','MEETEDATE':'"+medate+"','MEETINGROOM':'"+meetingRoom+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定住宿+会议室短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定住宿+会议室短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("0,2")) {//0:客房|2:餐饮
					StringBuffer sbuf = new StringBuffer();
					String queryYcxx = "SELECT TO_CHAR(EATTINGSTARTDATE,'yyyy-MM-dd HH24:mi') EATTINGSTARTDATE,ROOMNUM,PERSONNUM,"
							+ "(CASE WHEN PACKAGESTANDARD='0' THEN '桌餐人均100' WHEN PACKAGESTANDARD='1' THEN '桌餐人均120' WHEN "
							+ "PACKAGESTANDARD='2' THEN '桌餐人均150' WHEN PACKAGESTANDARD='3' THEN '自助餐70元/人起' ELSE '' END)"
							+ " PACKAGESTANDARD FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"' ORDER BY EATTINGSTARTDATE";
					List<Map<String, Object>> yHyxxList = DBSql.query(queryYcxx, new ColumnMapRowMapper(), new Object[] {});
					String eattingStartDate = "";
					String roomNum = "";
					String personNum = "";
					String packagestAndard = "";
					if(yHyxxList != null && !yHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = yHyxxList.get(0);
						eattingStartDate = CoreUtil.objToStr(zfxxMap.get("EATTINGSTARTDATE"));//用餐开始时间
						roomNum = CoreUtil.objToStr(zfxxMap.get("ROOMNUM"));//用餐地点
						personNum = CoreUtil.objToStr(zfxxMap.get("PERSONNUM"));//用餐人数
						packagestAndard = CoreUtil.objToStr(zfxxMap.get("PACKAGESTANDARD"));//用餐标准
						if(roomNum.contains("1")) {
							sbuf.append("大厅自助餐").append(",");
						}
						if(roomNum.contains("2")) {
							sbuf.append("小包7").append(",");
						}
						if(roomNum.contains("3")) {
							sbuf.append("小包6").append(",");
						}
						if(roomNum.contains("4")) {
							sbuf.append("小包5").append(",");
						}
						if(roomNum.contains("5")) {
							sbuf.append("中包").append(",");
						}
						if(roomNum.contains("6")) {
							sbuf.append("大包").append(",");
						}
						roomNum = sbuf.substring(0, sbuf.length()-1);
					}
					String queryZfxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd') EDATE,ORDERNUM,(CASE WHEN"
							+ " ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='4' THEN '标准双床房' ELSE '' END) ROOMTYPE FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> zfxxList = DBSql.query(queryZfxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String roomType = "";
					String orderNum = "";
					if(zfxxList != null && !zfxxList.isEmpty()) {
						Map<String, Object> zfxxMap = zfxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//抵店日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//离店日期
						roomType = CoreUtil.objToStr(zfxxMap.get("ROOMTYPE"));//客房类型
						orderNum = CoreUtil.objToStr(zfxxMap.get("ORDERNUM"));//客房数量
					}
					if(!applyUserPhone.equals("") && yHyxxList != null && !yHyxxList.isEmpty() && zfxxList != null && !zfxxList.isEmpty()) {
						SmsUtil sms = new SmsUtil();
						JSONObject json = new JSONObject();
						String phone = applyUserPhone;
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_SUCESS110_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','BDATE':'"+bdate+"','EDATE':'"+edate+"','ORDERNUM':'"+orderNum+"',"
										+ "'ROOMTYPE':'"+roomType+"','EATTINGSTARTDATE':'"+eattingStartDate+"','ROOMNUM':'"+roomNum+"'"
										+ ",'PERSONNUM':'"+personNum+"','PACKAGESTANDARD':'"+packagestAndard+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定住宿+餐饮短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定住宿+餐饮短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
				if(orderType.equals("0,1,2")) {//0:客房|1:会议室|2:餐饮
					StringBuffer sbuf = new StringBuffer();
					String queryYcxx = "SELECT TO_CHAR(EATTINGSTARTDATE,'yyyy-MM-dd HH24:mi') EATTINGSTARTDATE,ROOMNUM,PERSONNUM,"
							+ "(CASE WHEN PACKAGESTANDARD='0' THEN '桌餐人均100' WHEN PACKAGESTANDARD='1' THEN '桌餐人均120' WHEN "
							+ "PACKAGESTANDARD='2' THEN '桌餐人均150' WHEN PACKAGESTANDARD='3' THEN '自助餐70元/人起' ELSE '' END)"
							+ " PACKAGESTANDARD FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"' ORDER BY EATTINGSTARTDATE";
					List<Map<String, Object>> yHyxxList = DBSql.query(queryYcxx, new ColumnMapRowMapper(), new Object[] {});
					String eattingStartDate = "";
					String roomNum = "";
					String personNum = "";
					String packagestAndard = "";
					if(yHyxxList != null && !yHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = yHyxxList.get(0);
						eattingStartDate = CoreUtil.objToStr(zfxxMap.get("EATTINGSTARTDATE"));//用餐开始时间
						roomNum = CoreUtil.objToStr(zfxxMap.get("ROOMNUM"));//用餐地点
						personNum = CoreUtil.objToStr(zfxxMap.get("PERSONNUM"));//用餐人数
						packagestAndard = CoreUtil.objToStr(zfxxMap.get("PACKAGESTANDARD"));//用餐标准
						if(roomNum.contains("1")) {
							sbuf.append("大厅自助餐").append(",");
						}
						if(roomNum.contains("2")) {
							sbuf.append("小包7").append(",");
						}
						if(roomNum.contains("3")) {
							sbuf.append("小包6").append(",");
						}
						if(roomNum.contains("4")) {
							sbuf.append("小包5").append(",");
						}
						if(roomNum.contains("5")) {
							sbuf.append("中包").append(",");
						}
						if(roomNum.contains("6")) {
							sbuf.append("大包").append(",");
						}
						roomNum = sbuf.substring(0, sbuf.length()-1);
					}
					String queryZfxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd') EDATE,ORDERNUM,(CASE WHEN"
							+ " ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='4' THEN '标准双床房' ELSE '' END) ROOMTYPE FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> zfxxList = DBSql.query(queryZfxx, new ColumnMapRowMapper(), new Object[] {});
					String bdate = "";
					String edate = "";
					String roomType = "";
					String orderNum = "";
					if(zfxxList != null && !zfxxList.isEmpty()) {
						Map<String, Object> zfxxMap = zfxxList.get(0);
						bdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//抵店日期
						edate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//离店日期
						roomType = CoreUtil.objToStr(zfxxMap.get("ROOMTYPE"));//客房类型
						orderNum = CoreUtil.objToStr(zfxxMap.get("ORDERNUM"));//客房数量
					}
					String queryHyxx = "SELECT TO_CHAR(BDATE,'yyyy-MM-dd HH24:mi') BDATE,TO_CHAR(EDATE,'yyyy-MM-dd HH24:mi') EDATE,MEETINGROOM FROM "
							+ "BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"' ORDER BY BDATE";
					List<Map<String, Object>> queryHyxxList = DBSql.query(queryHyxx, new ColumnMapRowMapper(), new Object[] {});
					String mbdate = "";
					String medate = "";
					String meetingRoom = "";
					if(queryHyxxList != null && !queryHyxxList.isEmpty()) {
						Map<String, Object> zfxxMap = queryHyxxList.get(0);
						mbdate = CoreUtil.objToStr(zfxxMap.get("BDATE"));//会议开始日期
						medate = CoreUtil.objToStr(zfxxMap.get("EDATE"));//会议结束日期
						meetingRoom = CoreUtil.objToStr(zfxxMap.get("MEETINGROOM"));//会议室
					}
					if(!applyUserPhone.equals("") && yHyxxList != null && !yHyxxList.isEmpty() && zfxxList != null && !zfxxList.isEmpty() && queryHyxxList != null && !queryHyxxList.isEmpty()) {
						SmsUtil sms = new SmsUtil();
						String phone = applyUserPhone;
						JSONObject json = new JSONObject();
						String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_HOTEL_ORDER_SUCESS_TEMPLATE_ID);
						String param = "{'APPLYNAME':'"+userName+"','BDATE':'"+bdate+"','EDATE':'"+edate+"','ORDERNUM':'"+orderNum+"',"
										+ "'ROOMTYPE':'"+roomType+"','EATTINGSTARTDATE':'"+eattingStartDate+"','ROOMNUM':'"+roomNum+"'"
										+ ",'PERSONNUM':'"+personNum+"','PACKAGESTANDARD':'"+packagestAndard+"','MEETBDATE':'"+mbdate+"'"
										+ ",'MEETEDATE':'"+medate+"','MEETINGROOM':'"+meetingRoom+"'}";
						try {
							json = sms.sendSms(phone,templateId,param);
							System.out.println("交流中心预定住宿+餐饮+会议短信消息======"+json);
						} catch (Exception e) {
							System.out.println("交流中心预定住宿+餐饮+会议短信消息发送失败！");
							e.printStackTrace();
						}
					}
				}
			}else if(SDK.getTaskAPI().isChoiceActionMenu(pec.getTaskInstance(), "预订取消")) {
				message = "尊敬的"+userName+"，您好！非常遗憾的通知您，您的订单预定未成功，";
				if(!orderCancel.equals("")) {
					message = message + "具体原因如下："+ orderCancel +"。";
				}
				message = message + "如有疑问，可致电021-24099900转营销部。";
				MsgNoticeController.sendNoticeMsg(UserContext.fromUID(target), message, target, createUser, "1", "");
				SmsUtil sms = new SmsUtil();
				JSONObject returnData = new JSONObject();
				if(!applyUserPhone.equals("")) {
					String phone = applyUserPhone;
					String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_HOTEL_ORDER_FAIL_SMS_TEMPLATE_ID);
					String param = "{'APPLYNAME':'"+userName+"','CANCELREASON':'"+orderCancel+"'}";
					try {
						returnData = sms.sendSms(phone,templateId,param);
						System.out.println("交流中心预定失败短信消息======"+returnData);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("交流中心预定失败短信消息发送失败！");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
