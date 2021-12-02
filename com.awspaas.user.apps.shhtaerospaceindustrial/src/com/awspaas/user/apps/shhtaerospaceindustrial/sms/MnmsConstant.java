package com.awspaas.user.apps.shhtaerospaceindustrial.sms;

public class MnmsConstant {
	/**
	 * BO表名 专家基本信息主表
	 */
	public final static String BO_NAME_EXPERT = "BO_EU_MNMS_EXPERT";

	/**
	 * BO表名 会议基本信息主表
	 */
	public final static String BO_NAME_MEETING = "BO_EU_MNMS_MEETING";

	/**
	 * BO表名 会议关联专家子表
	 */
	public final static String BO_NAME_MEETING_EXPERT = "BO_EU_MNMS_MEETING_EXPERT";

	/**
	 * BO表名 OCR识别记录
	 */
	public final static String BO_NAME_OCR = "BO_EU_MNMS_OCR";

	/**
	 * BO表名 SMS短信发送记录
	 */
	public final static String BO_NAME_SMS = "BO_EU_MNMS_SMS";

	/**
	 * APP ID
	 */
	public final static String APP_ID = "com.awspaas.user.apps.shhtaerospaceindustrial";

	/**
	 * 日期格式 yyyy-MM-dd HH:mm:ss
	 */
	public final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * SMS返回结果 OK
	 */
	public final static String SMS_RESULT_OK = "OK";

	/**
	 * 字段名称 BINDID
	 */
	public final static String FIELD_BINDID = "BINDID";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.NO
	 */
	public final static String FIELD_MEETING_CODE = "NO";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.NAME
	 */
	public final static String FIELD_MEETING_NAME = "NAME";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.ADDRESS
	 */
	public final static String FIELD_MEETING_ADDRESS = "ADDRESS";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.DUTY
	 */
	public final static String FIELD_MEETING_DUTY = "DUTY";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.DUTY_PHONE
	 */
	public final static String FIELD_MEETING_DUTY_PHONE = "DUTY_PHONE";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.DUTY_UNIT
	 */
	public final static String FIELD_MEETING_DUTY_UNIT = "DUTY_UNIT";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.STATUS
	 */
	public final static String FIELD_MEETING_STATUS = "STATUS";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING.DATE_BEGIN
	 */
	public final static String FIELD_MEETING_DATE = "DATE_BEGIN";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING_EXPERT.EXPERT_NAME
	 */
	public final static String FIELD_EXPERT_NAME = "EXPERT_NAME";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING_EXPERT.EXPERT_PHONE
	 */
	public final static String FIELD_EXPERT_PHONE = "EXPERT_PHONE";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING_EXPERT.SEND_TIMES
	 */
	public final static String FIELD_SEND_TIMES = "SEND_TIMES";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING_EXPERT.RECENT_SEND_DATE
	 */
	public final static String FIELD_RECENT_SEND_DATE = "RECENT_SEND_DATE";

	/**
	 * 字段名称 BO_EU_MNMS_MEETING_EXPERT.RECEIPT_STATUS
	 */
	public final static String FIELD_RECEIPT_STATUS = "RECEIPT_STATUS";

	/**
	 * 字段名称 BO_EU_MNMS_SMS.PHONE
	 */
	public final static String FIELD_SMS_PHONE = "PHONE";

	/**
	 * 字段名称 BO_EU_MNMS_SMS.TYPE
	 */
	public final static String FIELD_SMS_TYPE = "TYPE";

	/**
	 * 字段名称 BO_EU_MNMS_SMS.CONTENT
	 */
	public final static String FIELD_SMS_CONTENT = "CONTENT";

	/**
	 * 字段名称 BO_EU_MNMS_SMS.STATUS
	 */
	public final static String FIELD_SMS_STATUS = "STATUS";

	/**
	 * 字段名称 BO_EU_MNMS_SMS.SEND_DATE
	 */
	public final static String FIELD_SMS_SEND_DATE = "SEND_DATE";

	/**
	 * 会议状态 准备中
	 */
	public final static String MEETING_STATUS_STANDBY = "准备中";

	/**
	 * 会议状态 完成
	 */
	public final static String MEETING_STATUS_FINISH = "完成";

	/**
	 * 会议状态 取消
	 */
	public final static String MEETING_STATUS_CANCEL = "取消";

	/**
	 * 回执状态 未回复
	 */
	public final static String RECEIPT_STATUS_NO_RES = "未回复";

	/**
	 * 回执状态 参加
	 */
	public final static String RECEIPT_STATUS_PART = "参加";

	/**
	 * 回执状态 不参加
	 */
	public final static String RECEIPT_STATUS_NOT_PART = "不参加";

	/**
	 * 发送状态 成功
	 */
	public final static String SEND_STATUS_SUCCESS = "成功";

	/**
	 * 发送状态 失败
	 */
	public final static String SEND_STATUS_FAILED = "失败";

	/**
	 * SMS类型 发送
	 */
	public final static String SMS_TYPE_SEND = "发送";

	/**
	 * SMS类型 定时发送
	 */
	public final static String SMS_TYPE_SEND_AUTO = "定时发送";

	/**
	 * 系统参数 阿里云OCR appcode
	 */
	public final static String PARAM_ALIYUN_OCR_APP_CODE = "aliyunOcrAppCode";

	/**
	 * 系统参数 阿里云SMS AccessKeyId
	 */
	public final static String PARAM_ALIYUN_SMS_ACCESS_KEY_ID = "aliyunSmsAccessKeyId";

	/**
	 * 系统参数 阿里云SMS AccessKeySecret
	 */
	public final static String PARAM_ALIYUN_SMS_ACCESS_KEY_SECRET = "aliyunSmsAccessKeySecret";

	/**
	 * 系统参数 阿里云SMS短信签名
	 */
	public final static String PARAM_ALIYUN_SMS_SIGN_NAME = "aliyunSmsSignName";

	/**
	 * 系统参数 酒店预订不成功信息模板ID
	 */
	public final static String PARAM_HOTEL_ORDER_FAIL_SMS_TEMPLATE_ID = "HotelOrderFailTemplateId";

	/**
	 * 系统参数 酒店消息预订成功通知-单记录模板ID
	 */
	public final static String PARAM_HOTEL_ORDER_SUCESS_TEMPLATE_ID = "HotelOrderSucessTemplateId";
	
	/**
	 * 系统参数 酒店预订模板100
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS100_TEMPLATE_ID = "HotelOrderSucessTemplateId100";

	/**
	 * 系统参数 酒店预订成功010
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS010_TEMPLATE_ID = "HotelOrderFailTemplateId010";

	/**
	 * 系统参数 酒店预订模板001
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS001_TEMPLATE_ID = "HotelOrderFailTemplateId001";

	/**
	 * 系统参数 酒店预订模板011
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS011_TEMPLATE_ID = "HotelOrderFailTemplateId011";

	/**
	 * 系统参数 酒店预订模板101
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS101_TEMPLATE_ID = "VehicleDispatchSucessTemplateId101";

	/**
	 * 系统参数 酒店预订模板110
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS110_TEMPLATE_ID = "VehicleDispatchSucessTemplateId110";
	
	/**
	 * 系统参数 车辆预订派单成功通知模板ID
	 */
	public final static String PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID = "VehicleDispatchSucessTemplateId";
	
	/**
	 * 系统参数 车辆预订派单取消通知模板ID
	 */
	public final static String PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID = "VehicleDispatchFailTemplateId";

	/**
	 * 系统参数 车辆派单给司机发通知模板ID
	 */
	public final static String PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID = "VehicleDispatchToDriverTemplateId";
	
	/**
	 * 系统参数 食堂餐饮预订成功通知模板ID
	 */
	public final static String PARAM_CANTEEN_ORDER_SUCESS_TEMPLATE_ID = "CanteenOrderSucessTemplateId";

	/**
	 * 系统参数 食堂餐饮预订不成功通知模板
	 */
	public final static String PARAM_CANTEEN_ORDER_FAIL_TEMPLATE_ID = "CanteenOrderFailTemplateId";
	
	/**
	 * 食堂预订用户取消模板ID
	 */
	public final static String PARAM_CANTEEN_ORDER_CANCEL_TEMPLATE_ID = "CanteenOrderCancelTemplateId";
	
	/**
	 * 业务参数 每个会议单人最多短信发送次数
	 */
	public final static String PARAM_SEND_TIMES_MAX = "smsMaxCount";

	/**
	 * 业务参数 距离会议开始不再自动发送短信通知时间
	 */
	public final static String PARAM_SMS_AUTO_OFF_DAYS = "smsAutoOffDays";

	/**
	 * OCR 行类型 会议名称及地点
	 */
	public final static String OCR_ROW_TYPE_NAME_AND_ADDRESS = "OCR_ROW_TYPE_NAME_AND_ADDRESS";

	/**
	 * OCR 行类型 会议时间
	 */
	public final static String OCR_ROW_TYPE_MEETING_DATE = "OCR_ROW_TYPE_MEETING_DATE";

	/**
	 * OCR 行类型 责任人
	 */
	public final static String OCR_ROW_TYPE_DUTY = "OCR_ROW_TYPE_DUTY";

	/**
	 * OCR 行类型 专家信息
	 */
	public final static String OCR_ROW_TYPE_EXPERT = "OCR_ROW_TYPE_EXPERT";
	
	/**
	 * 车辆预订取消通知乘客V1
	 */
	public final static String SHSY_CACELMISSION_NOTIFYTOUSER = "SMS_228016523";
	
	/**
	 * 车辆预订取消通知-司机V1
	 */
	public final static String SHSY_CACELMISSION_NOTIFYTODRIVER = "SMS_228116397";
	
	/**
	 * 临时用车任务变更通知用户
	 */
	public final static String SHSY_MODIFYMISSION_NOTIFYTOUSER = "SMS_228850142";
	
	/**
	 *  临时用车成功派发通知司机
	 */
	public final static String SHSY_MISSION_NOTIFYTODRIVER= "SMS_200194554";
	/**
	 *  临时用车成功派发通知用户
	 */
	public final static String SHSY_MISSION_NOTIFYTOUSER= "SMS_200194579";

}
