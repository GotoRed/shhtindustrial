package com.awspaas.user.apps.shhtaerospaceindustrial.sms;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.sdk.local.SDK;
//import com.actionsoft.sdk.local.api.LogAPI;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
//import com.awspaas.user.apps.hangyuan.mnms.i18n.MnmsConstant;

public class SmsUtil
{
	// 阿里云SMS AccessKeyId
	private static String accessKeyId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
			MnmsConstant.PARAM_ALIYUN_SMS_ACCESS_KEY_ID);
	// 阿里云SMS AccessKeySecret
	private static String accessKeySecret = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
			MnmsConstant.PARAM_ALIYUN_SMS_ACCESS_KEY_SECRET);
	// 阿里云SMS短信签名
	private static String smsSignName = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,
			MnmsConstant.PARAM_ALIYUN_SMS_SIGN_NAME);

	/**
	 * 通用短信发送
	 * @param phone 手机号
	 * @param templateId 短信模板ID
	 * @param param 短信模板参数
	 * @return
	 * @throws Exception
	 */
	public static JSONObject sendSms(String phone, String templateId, String param) throws Exception
	{
		DefaultProfile profile = DefaultProfile.getProfile("default", accessKeyId, accessKeySecret);
		IAcsClient client = new DefaultAcsClient(profile);

		CommonRequest request = new CommonRequest();
		request.setProtocol(ProtocolType.HTTPS);
		request.setMethod(MethodType.POST);
		request.setDomain("dysmsapi.aliyuncs.com");
		request.setVersion("2017-05-25");
		request.setAction("SendSms");
		request.putQueryParameter("PhoneNumbers", phone);
		request.putQueryParameter("SignName", smsSignName);
		request.putQueryParameter("TemplateCode", templateId);
		request.putQueryParameter("TemplateParam", param);
		request.putQueryParameter("OutId", LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() + "");

		CommonResponse response = client.getCommonResponse(request);
		
		return JSONObject.parseObject(response.getData());
	}
	
	/**
	 * 记录短信日志
	 * @param uc
	 * @param data
	 * @param phone
	 */
	public static void smsLog(UserContext uc, String param, JSONObject data, String phone)
	{
		try {
			// 获得Message
			String message = data.getString("Message");

			// 更新SMS日志表
			BO smsBo = new BO();
			smsBo.set(MnmsConstant.FIELD_SMS_PHONE, phone);
			smsBo.set(MnmsConstant.FIELD_SMS_TYPE, MnmsConstant.SMS_TYPE_SEND);
			smsBo.set(MnmsConstant.FIELD_SMS_CONTENT, param);
			smsBo.set(MnmsConstant.FIELD_SMS_STATUS, MnmsConstant.SMS_RESULT_OK.equals(message) ? MnmsConstant.SEND_STATUS_SUCCESS : MnmsConstant.SEND_STATUS_FAILED);
			SDK.getBOAPI().createDataBO(MnmsConstant.BO_NAME_SMS, smsBo, uc);
		} catch (Exception e) {
			// 日志记录异常忽略处理
		}
	}
	
}
