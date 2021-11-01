package com.shht.apps.yqj.openapi;

import com.actionsoft.bpms.api.OpenApiClient;
import com.actionsoft.bpms.api.common.ApiResponse;
import com.actionsoft.bpms.util.UtilIO;
import com.actionsoft.sdk.service.model.FormFileModel;
import com.actionsoft.sdk.service.model.ProcessInstance;
import com.actionsoft.sdk.service.model.UploadFile;
import com.actionsoft.sdk.service.response.StringResponse;
import com.actionsoft.sdk.service.response.process.ProcessInstResponse;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用代码创建一个流程并初始化其数据，并按照路由规则继续办理 到第二个节点 
 * @author zhaoj
 *
 */
public class invokeShhtXcysoweb {

	public static void main(String[] arg) {
		// TODO Auto-generated method stub
		//创建连接
		String apiServer = "https://www.sht808.com/portal/openapi";

		//身份验证
		//身份验证
		String accessKey = "opapiuser4326";
		String secret = "Abcd123456@#";
		OpenApiClient client = new OpenApiClient(apiServer, accessKey, secret);
		/**
		 * 创建流程
		 */
		String apiMethod = "process.create";
		//参数设置
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("processDefId", "obj_c49c76181e83491b86e7259dce0c1113");//下厂监制流程

		args.put("uid", "china_zh");//系统登录账号
		args.put("title", "下厂验收TITLE");//流程标题
		
		ProcessInstResponse r = client.exec(apiMethod, args, ProcessInstResponse.class);
		ProcessInstance processInst = r.getData();
		System.out.println("已经创建了一个流程 ："+r.getData().getId());
		String bindid = processInst.getId();
		/**
		 * 启动流程
		 */
		
		String apiMethod2 = "process.start";
		Map<String, Object> start  = new HashMap<String, Object>();
		start.put("processInstId",processInst.getId() );
		ApiResponse rstart = client.exec(apiMethod2, start, ApiResponse.class);
		System.out.println("启动一个流程："+rstart.getResult());
		/**
		 * 初始化数据
		 * 
		 */
		String boApiMethod = "bo.create";
		Map<String, Object> boCreaD = new HashMap<String, Object>();
		boCreaD.put("boName","BO_EU_SHHT_CHECKNOTIFY_HEAD" );//下厂验收主表
		
		Map<String, Object> bodata = new HashMap<String, Object>();
		
		bodata.put("PRODUCEUNIT", "4326厂");//生产单位
		bodata.put("USERUNIT", "802所");//用户单位 ,可通过userunitquery获取
		bodata.put("JINGBANREN", "经办人");//经办人姓名
		bodata.put("ISXCCHECK", "是");//是否下厂验收
		bodata.put("ISXCJIANZHI", "是");//是否下厂监制
		bodata.put("ISCOMMONFILE", "是");//是否统一附件
		bodata.put("MEMO", "memo");//备注


		boCreaD.put("recordData", bodata);
		boCreaD.put("bindId",bindid );
		boCreaD.put("uid", "china_zh");//系统登录账号
		StringResponse rbo = client.exec(boApiMethod, boCreaD, StringResponse.class);
		System.out.println("初始化BO："+rbo.getData());
		String BoId=rbo.getData();

		Map<String, Object> boCreaDMx = new HashMap<String, Object>();
		boCreaDMx.put("boName","BO_EU_SHHT_CHECKNOTIFY_DETIAL" );//下厂监制验收明细表

		Map<String, Object> mxbodata = new HashMap<String, Object>();

		mxbodata.put("HETONGNO", "HTBH");//合同好
		mxbodata.put("INVNAME", "器件名称");//器件名称
		mxbodata.put("MODEL", "CCC");//器件型号\规格
		mxbodata.put("ZLDJ", "DDD");//质量等级

		mxbodata.put("ISXCJIANZHI", "是");//是否下厂监制 是|否
		mxbodata.put("ISXCCHECK", "是");//是否下厂验收 是|否

		mxbodata.put("BATCHCODE", "BATCHCODE");//生产批次
		mxbodata.put("PRODUCEDATE", "2021-08-11");//生产日期
		mxbodata.put("ODRERNUM", "100");//订货数量
		mxbodata.put("CHECKNUM", "100");//验收数量
		mxbodata.put("SOURCEHTMXID", "合同明细id");//来源合同明细id



		boCreaDMx.put("recordData", mxbodata);
		boCreaDMx.put("bindId",bindid );
		boCreaDMx.put("uid", "china_zh");//系统登录账号

		StringResponse rmxbo = client.exec(boApiMethod, boCreaDMx, StringResponse.class);
		System.out.println("初始化BO："+rmxbo.getData());
		String MxBoId=rmxbo.getData();

		Map<String, Object> param = new HashMap<String, Object>();
		//读取文件
		byte[] content = null;
		File file = new File("D:\\Actionsoft\\Test1.java");
//		 File file = new File("/home/chengy/桌面/222222222.png");
		ByteArrayOutputStream imgdf = new ByteArrayOutputStream();
		try {
			UtilIO.copyAndCloseInput(new FileInputStream(file), imgdf);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		content = imgdf.toByteArray();


		//参数1
		FormFileModel formFile = new FormFileModel();
//		formFile.setId("300e4423-c384-4b33-b006-a7e05ed6a1ea");
		formFile.setBoName("BO_EU_SHHT_CHECKNOTIFY_HEAD");
		formFile.setProcessInstId(bindid);
		formFile.setBoId(BoId);

		SimpleDateFormat df = new SimpleDateFormat("2021-08-02 19:30:23");
		String time = df.format(new Date());
		Timestamp ts = Timestamp.valueOf(time);

		formFile.setCreateDate(ts );//创建时间
		formFile.setCreateUser("china_zh");//创建人

		formFile.setAppId("com.awspaas.user.apps.shht808yuanqijian.xcbusiness");//应用ID
		formFile.setTaskInstId("obj_c90f45fafb400001bc23f82c5e8517c1");//任务实例ID
		formFile.setFileName(file.getName());//附件名称
		formFile.setBoItemName("COMMONFILE");//列名
		System.out.println(formFile);
		param.put("formFileModel", formFile);
		//参数2
		UploadFile uploadFile = new UploadFile();

		uploadFile.setContent(content );//附件转为byte数组
		uploadFile.setName("Test1.java");//附件名称
		System.out.println(uploadFile);
		param.put("data", uploadFile);//
		String upfileapiMethod = "bo.file.up";
		StringResponse r2 = client.exec(upfileapiMethod, param, StringResponse.class);
		System.out.println(r2.getMsg()+":::"+r2.getData()+":::"+r2.getResult()+"::::"+r2.getErrorCode());


		
		/**
		 * 按照平台设定的路由向下办理,
		 * 注意：要确定下一个节点要设置了路由方案，否则就会到无人执行里面
		 * 
		 */
		String taskcom = "task.complete.all";
		Map<String, Object> taskcomvalue = new HashMap<String, Object>();
		taskcomvalue.put("processInstId", processInst.getId());
		taskcomvalue.put("activityDefId","obj_c90f45fafb400001bc23f82c5e8517c1" );
		taskcomvalue.put("uid","china_zh" );
		ApiResponse taskR = client.exec(taskcom, taskcomvalue, ApiResponse.class);

		System.out.println("流程办理到下一个节点："+taskR.getResult());
		
		

	}

}
