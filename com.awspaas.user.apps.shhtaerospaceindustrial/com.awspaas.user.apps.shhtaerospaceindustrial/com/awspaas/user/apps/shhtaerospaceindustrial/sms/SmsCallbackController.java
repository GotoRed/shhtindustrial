//package com.awspaas.user.apps.shhtaerospaceindustrial.sms;
//
//import java.util.UUID;
//
//import com.actionsoft.bpms.bo.engine.BO;
//import com.actionsoft.bpms.server.bind.annotation.Controller;
//import com.actionsoft.bpms.server.bind.annotation.Mapping;
//import com.actionsoft.bpms.util.DBSql;
//import com.actionsoft.sdk.local.SDK;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
////import com.awspaas.user.apps.hangyuan.mnms.i18n.MnmsConstant;
////import com.awspaas.user.apps.hangyuan.mnms.util.MeetingUtil;
//
//@Controller
//public class SmsCallbackController {
//
//	@Mapping(value = "com.awspaas.user.apps.hangyuan.mnms_smsCallback", session = false, noSessionEvaluate = "无安全隐患", noSessionReason = "用于短信息回执处理")
//	public Object smsCallback(String params) throws Exception {
//		System.out.println("callback接收：" + params);
//		if (params != null && params != "") {
//			JSONArray jsonArray = JSONArray.parseArray(params);
//
//            if (null != jsonArray && jsonArray.size() > 0)
//            {
//            	for (int i = 0; i < jsonArray.size(); i++)
//                {
//                    JSONObject json = jsonArray.getJSONObject(i);
//
//                    // 记录上行短信日志
//                    this.smsLog(json);
//                    
//                    // 处理上行短信业务
//                    this.smsHandle(json);
//                }
//            }
//        }
//
//		// 返回给阿里云SMS平台固定response
//		JSONObject ok = new JSONObject();
//		ok.put("code", 0);
//		ok.put("msg", "成功");
//
//		return ok;
//	}
//	
//	/**
//	 * SMS上行短信日志记录
//	 * @param json
//	 */
//	private void smsLog(JSONObject json)
//	{
//		String id = UUID.randomUUID().toString();
//		DBSql.update("INSERT INTO " + MnmsConstant.BO_NAME_SMS + " (ID, PHONE, TYPE, CONTENT, STATUS) VALUES ('" + id + "', '"
//				+ json.getString("phone_number") + "', '接收', '"+ json.getString("content") + "', '成功')");
//	}
//	
//	/**
//	 * SMS处理上行短信业务
//	 * @param json
//	 */
//	@SuppressWarnings("static-access")
//	private void smsHandle(JSONObject json)
//	{
//		SDK.getLogAPI().getLogger(this.getClass()).info("接收上行短信:\n" + json.toString());
//		String phone = json.getString("phone_number");
//		String content = json.getString("content");
//		String sendTime = json.getString("send_time");
//		
//		if (null != content && content.length() >= 4)
//		{
//			String meetingNo = content.substring(0, 4);
//			String result = "";
//
//			if (content.length() == 4)
//			{
//				result = "取消确认";
//			}
//			else if ("1".equals(content.substring(4, 5)))
//			{
//				result = "参加";
//			}
//			else if ("2".equals(content.substring(4, 5)))
//			{
//				result = "不参加";
//			}
//			
//			// 先根据meetingNo获取会议BO
//			BO meeting = SDK.getBOAPI().getByKeyField(MnmsConstant.BO_NAME_MEETING, "NO", meetingNo);
//			
//			if (meeting == null)
//			{
//				SDK.getLogAPI().getLogger(this.getClass()).error("上行短信会议记录不存在，不处理");
//				return;
//			}
//			
//			// 根据会议BO查询对应子表中该手机号的记录
//			BO meetingExpert = SDK.getBOAPI().query(MnmsConstant.BO_NAME_MEETING_EXPERT).addQuery("BINDID=", meeting.getBindId()).addQuery("EXPERT_PHONE=", phone).detail();
//			
//			if (meetingExpert == null)
//			{
//				SDK.getLogAPI().getLogger(this.getClass()).error("上行短信会议关联专家不存在，不处理");
//				return;
//			}
//			
//			// 根据BOID更新回执状态、回执内容、回执时间
//			meetingExpert.set("RECEIPT_STATUS", result);
//			meetingExpert.set("RECEIPT_CONTENT", content);
//			meetingExpert.set("RECEIPT_DATE", sendTime);
//			SDK.getBOAPI().update(MnmsConstant.BO_NAME_MEETING_EXPERT, meetingExpert);
//			
//			// 更新人数统计信息
////	        MeetingUtil.updateMeetingNumInfo(meeting.getBindId());
//		}
//	}
//}
