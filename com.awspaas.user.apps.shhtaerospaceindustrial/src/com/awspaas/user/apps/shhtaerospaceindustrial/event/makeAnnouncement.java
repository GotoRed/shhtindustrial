package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
import com.awspaass.user.apps.syncwechataddress.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class makeAnnouncement extends ExecuteListener implements ExecuteListenerInterface{
	
	public static String  getAccessToken(String corpid,String corpsecret) {

		String access_token="";
		String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret;
		String result = HttpClientUtil.doGet(access_token_url);

		JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
		

		access_token = jsonObject.get("access_token").getAsString();
		//System.out.print(access_token);

		return access_token;
	}
	
	public static String sendWeChatMessage(String toUser, String toParty, String msgType,String toTag, String msgCotent,int safe,String agentid,String access_token) {
		HashMap<String, Object> jsonmap = new HashMap<String, Object>();
		jsonmap.put("touser", toUser);
		jsonmap.put("toparty", toParty);
		jsonmap.put("totag",toTag);
		jsonmap.put("msgtype", msgType);
		String content="{\"content\":"+"\"" + msgCotent + "\"}";
		jsonmap.put("text",content);
		jsonmap.put("safe", 0);
		jsonmap.put("enable_id_trans", 0);
		jsonmap.put("enable_duplicate_check", 0);

		//System.out.println(jsonmap.toString());
		StringBuffer stringBuffer = new StringBuffer();
	    stringBuffer.append("{");
	    stringBuffer.append("\"touser\":" + "\"" + toUser + "\",");
	    stringBuffer.append("\"toparty\":" + "\"" + toParty + "\",");
	    stringBuffer.append("\"totag\":" + "\"" + toTag + "\",");
	    stringBuffer.append("\"msgtype\":" + "\"" + msgType + "\",");
	    stringBuffer.append("\"text\":" + "{");
	    stringBuffer.append("\"content\":" + "\"" + msgCotent + "\"");
	    stringBuffer.append("}");
	    stringBuffer.append(",\"safe\":" + "\"" + safe + "\",");
	    stringBuffer.append("\"agentid\":" + "\"" + agentid + "\",");
	    stringBuffer.append("\"debug\":" + "\"" + "1" + "\"");
	    stringBuffer.append("}");
	    String msg_json = stringBuffer.toString();
	    
	    
	    String sendMsgUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+access_token;
	    //String re= HttpClientUtil.doPost(sendMsgUrl, jsonmap);
	    String re=HttpClientUtil.doPostForJson(sendMsgUrl, msg_json);
	    
	    
	    

		return re;
		
	}
	public void execute(ProcessExecutionContext ctx) throws Exception {
		boolean button = SDK.getTaskAPI().isChoiceActionMenu(ctx.getTaskInstance(), "同意");
		String processInstId= ctx.getProcessInstance().getId();
		if(button) {
			List<Map<String, Object>> annoucementInfoList = DBSql.query("SELECT * FROM BO_EU_SHSY_NOTICE_APP WHERE BINDID = ? ", new ColumnMapRowMapper(), new Object[] {processInstId});
			String CORPIDS = CoreUtil.objToStr(annoucementInfoList.get(0).get("CORPIDS"));
			String AGENTIDS = CoreUtil.objToStr(annoucementInfoList.get(0).get("AGENTIDS"));
			String AGENTSECRETIDS = CoreUtil.objToStr(annoucementInfoList.get(0).get("AGENTSECRETIDS"));
			
			String USERUNIT=CoreUtil.objToStr(annoucementInfoList.get(0).get("AGENTSECRETIDS"));
			String APPDATE=CoreUtil.objToStr(annoucementInfoList.get(0).get("APPDATE"));
			String TITLE=CoreUtil.objToStr(annoucementInfoList.get(0).get("TITLE"));
			String CONTENT=CoreUtil.objToStr(annoucementInfoList.get(0).get("CONTENT"));
			String XGFILE=CoreUtil.objToStr(annoucementInfoList.get(0).get("XGFILE"));
			
			String cporid[] =  CORPIDS.split("\\|");
			String angetid[] = AGENTIDS.split("\\|");
			String secretid[] = AGENTSECRETIDS.split("\\|");
			
			for(int i = 0; i< cporid.length; i++) {
				String acessToken = getAccessToken(cporid[i],secretid[i]);
				String content = "【"+TITLE+"】 ："+CONTENT;
				if(XGFILE!="") {
					content+="\n <a href=\"http://baidu.com\">通知附件</a>";
				}
				sendWeChatMessage("@all","1","text","",content,0,angetid[i],acessToken);				
			}
		}
		return;
	}
}
