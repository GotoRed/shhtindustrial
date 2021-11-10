package com.awspaass.user.apps.syncwechataddress;
import java.io.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.bpms.schedule.IJob;
import com.actionsoft.bpms.server.fs.DCContext;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.*;
import com.awspaass.user.apps.syncwechataddress.LogPrinter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import com.actionsoft.bpms.org.model.impl.CompanyModelImpl;
import com.actionsoft.bpms.commons.mvc.view.ResponseObject;
import com.actionsoft.bpms.commons.wechat.bean.WechatMessage;
import com.actionsoft.bpms.org.model.CompanyModel;
import com.actionsoft.bpms.org.model.DepartmentModel;
import com.actionsoft.bpms.org.model.RoleModel;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.org.model.UserMapModel;
public class testApi  implements IJob {
	public void execute(JobExecutionContext jobExecutionContext)
	        throws JobExecutionException {
		System.out.println("Enter TestAPi!");
	        // 读管理员配置的扩展参数串，支持简单的@公式
	/*	
		LogPrinter p = new LogPrinter();
		Logger log= p.getMylog();
		String corpid="wwae8baae9cb425bc7";
		//String corpsecret="tBQRUDNSAIEKx_HwkqTLXjSwr_0ifr_o2vdsOc1_aU8";
		//String aslp = "aslp://com.actionsoft.apps.wechat/downloadMeida";
		String aslp = "aslp://com.actionsoft.apps.wechat/sendMessage";
		// 参数定义列表
		Map<String, Object> params = new HashMap<String, Object>();
		WechatMessage msg = WechatMessage.TEXT().agentId("1000003").content("Hi, AWS PaaS!").toParty("1").build();
		// 要发送的消息,参数必须
		params.put("message", msg.toJson());
		// 企业的CorpId,参数必须
		params.put("corpId", "wwae8baae9cb425bc7");
		// 执行API
		ResponseObject ro = SDK.getAppAPI().callASLP(SDK.getAppAPI().getAppContext("com.awspaas.user.apps.shhtaerospaceindustrial"
				+ ""), aslp, params);
	*/
		  String sourceAppId = "com.awspaas.user.apps.shhtaerospaceindustrial";
		// 服务地址
		String aslp = "aslp://com.actionsoft.apps.wechat/sendMessage";
		// 参数定义列表  
		Map params = new HashMap<String, Object>();
		WechatMessage msg = WechatMessage.TEXT().content("Hello").build();
		//企业号应用agentId,必填 
		params.put("agentId", "1000003");
		//企业号Id,必填 
		params.put("corpId", "wwae8baae9cb425bc7");
		//要发送的消息格式，由WechatMessage对象构建。例如WechatMessage.TEXT.xxx,必填 
		params.put("message", msg.toJson());
		AppAPI appAPI =  SDK.getAppAPI(); 
		//发送微信消息 
		appAPI.asynCallASLP(appAPI.getAppContext(sourceAppId), aslp, params);
		/*if (ro.isOk()) {
		    // todo
			System.out.println("send message sucess!");
		} else {
		    // todo
			System.out.println("send message failed!");
		}*/

	}

}
