package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.UtilToolsForString;

@Controller
public class MsgNoticeController {
	/**
	 * 
	 * @param uc
	 * @param bstype 业务类型 （0：前两条|1：全部）
	 * @param dqzt 读取状态（0：未读|1：全部）
	 * @return 我的消息
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_getMsgNotice")
	public String getMsgNotice(UserContext uc, String  bstype, String dqzt) {
		JSONObject result = new JSONObject();
		try {
			StringBuilder querySql = new StringBuilder();
			querySql.append("select ID,XXNR,FSRXM, to_char(FSRQ,'yyyy-MM-dd HH24:mi') FSRQ,DQZT,SFKDJ,LJURL from BO_EU_SH_MSGNOTICE where  1=1  ");
			querySql.append(" and  JSRZH = '").append(uc.getUID()).append("' ");
			if(dqzt.equals("0")) {//未读
				querySql.append(" and DQZT = '0' ");
			}		
			querySql.append("order by FSRQ desc ");
			if(bstype.equals("0")) {//业务类型
				querySql.replace(0, 1, "SELECT * FROM (s");
				querySql.append(")");
				querySql.append(" where rownum <=2 ");
			}
			JSONArray msgArry = new JSONArray();
			List<Map<String, Object>> msgList = DBSql.query(querySql.toString(), new ColumnMapRowMapper(), new Object[] {});
			if(msgList ==null || msgList.isEmpty()) {
				result.put("status", "0");
				result.put("msgList", msgArry);
				return result.toString();
			}
			for(Map<String, Object> msg : msgList) {
				JSONObject msgJson = new JSONObject();
				msgJson.put("ID", UtilToolsForString.handleObjectToString(msg.get("ID")));//消息内容
				msgJson.put("XXNR", UtilToolsForString.handleObjectToString(msg.get("XXNR")));//消息内容
				msgJson.put("FSRXM", UtilToolsForString.handleObjectToString(msg.get("FSRXM")));//消息发送人姓名
				msgJson.put("FSRQ", UtilToolsForString.handleObjectToString(msg.get("FSRQ")));//消息发送日期
				msgJson.put("DQZT", UtilToolsForString.handleObjectToString(msg.get("DQZT")));//读取状态
				String sfkdj = UtilToolsForString.handleObjectToString(msg.get("SFKDJ"));
				msgJson.put("SFKDJ", sfkdj);//是否可点击
				if(sfkdj.equals("0")) {//可点击
					String  ljUrl =  UtilToolsForString.handleObjectToString(msg.get("LJURL"));
					ljUrl = SDK.getRuleAPI().executeAtScript(ljUrl, uc);
					msgJson.put("LJURL", ljUrl);//链接url
				}
				msgArry.add(msgJson);
			}
			// 成功
			result.put("status", "0");
			result.put("msgList", msgArry);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	/**
	 * 
	 * @param uc
	 * @param bstype 业务类型 （0：前两条|1：全部）
	 * @param dqzt 读取状态（0：未读|1：全部）
	 * @return 我的消息
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_readMsgNoticeById")
	public String readMsgNoticeById(UserContext uc, String  msgId) {
		JSONObject result = new JSONObject();
		try {
			String updateSql = "update BO_EU_SH_MSGNOTICE set DQZT = 1,DQSJ=sysdate where id = '"+msgId+"'";
			DBSql.update(updateSql);
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	/**
	 * @Desc 发送消息提醒
	 * @param uc
	 * @param msgContent 消息内容
	 * @param fsrzh 发送人账号
	 * @param jsrzh 接收人账号
	 * @param sfkdj 是否可点击
	 * @param ljUrl 链接url内容   可点击时，需要传入
	 * @return
	 */
	@Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_sendNoticeMsg")
	public static String sendNoticeMsg(UserContext uc, String  msgContent,String fsrzh,String jsrzh,String sfkdj ,String ljUrl) {
		JSONObject result = new JSONObject();
		try {
			BO bo = new BO();
			bo.set("XXFL", "1");//消息分类
			bo.set("XXNR", msgContent);//消息内容
			bo.set("FSRZH", fsrzh);
			bo.set("FSRXM", SDK.getORGAPI().getUserNames(fsrzh));
			
			bo.set("JSRZH", jsrzh);
			bo.set("JSRXM", SDK.getORGAPI().getUserNames(jsrzh));
			bo.set("DQZT", "0");
			bo.set("SFKDJ",sfkdj);
			bo.set("LJURL", ljUrl);
			SDK.getBOAPI().createDataBO("BO_EU_SH_MSGNOTICE", bo, uc);
			result.put("status", "0");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
}
