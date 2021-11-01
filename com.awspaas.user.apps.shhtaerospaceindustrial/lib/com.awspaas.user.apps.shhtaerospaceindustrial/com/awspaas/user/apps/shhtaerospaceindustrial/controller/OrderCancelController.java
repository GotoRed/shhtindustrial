package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.sql.Connection;
import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class OrderCancelController {
	//日期格式
//	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * 调度取消餐饮订单，中止流程
	 * @param uc
	 * @param bindid
	 * @param revocationReason
	 * @return
	 */
	@Mapping("shsy.foodOrderCancel")
	public String cancelFoodOrder(UserContext uc,String processInstId,String revocationReason) {
//		Date currDate = new Date();
//		String curr = sdf.format(currDate);
//		Date currentDate = null;
//		try {
//			currentDate = sdf.parse(curr);
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//		}
		JSONObject result = new JSONObject();
		Connection conn = null;
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			//查询未取消未结束的订单
			String orderSql = "select id,applyid,orderid,orderdate,ycdate,area,roomnum from BO_EU_SH_FOODORDER where ORDERSTATUS!='6' and bindid='"+processInstId+"' and isend!=1 ";
			List<Map<String, Object>> dataList = DBSql.query(orderSql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				result.put("status", "0");
				result.put("message", "没有任何可取消的餐饮订单");
				return result.toString();
			}
			Map<String, Object> dataMap = dataList.get(0);
			
			String itId = CoreUtil.objToStr(dataMap.get("id"));
			String applyUid = CoreUtil.objToStr(dataMap.get("applyid"));
			String orderId = CoreUtil.objToStr(dataMap.get("orderid"));
//			String orderDate = CoreUtil.objToStr(dataMap.get("orderdate"));
			String ycdate = CoreUtil.objToStr(dataMap.get("ycdate"));
			String area = CoreUtil.objToStr(dataMap.get("area"));
			String areaName = CoreUtil.objToStr(DBSql.getString("select item.cnname areaname from Bo_Act_Dict_Kv_Item item "
					+ "left join BO_ACT_DICT_KV_MAIN main on item.bindid = main.bindid "
					+ "where substr('"+area+"',0,1)=item.itemno and main.dictkey = 'shdininghallarea'", "areaname"));
			String roomnum = CoreUtil.objToStr(dataMap.get("roomnum"));
			String applyName = SDK.getORGAPI().getUser(applyUid).getUserName();
			String applyMobile = SDK.getORGAPI().getUser(applyUid).getMobile();
			
			
//			if(orderDate!=null&&!orderDate.equals("")&&orderDate.length()>10) {
//				orderDate = orderDate.substring(0,10);
//			}else {
//				result.put("status", "0");
//				result.put("orderDayStatus", "");
//				result.put("message", "订单日期格式错误");
//				return result.toString();
//			}
//			Date beginDate = sdf.parse(orderDate);
//			int orderDayStatus = beginDate.compareTo(currentDate);
			
			String tempResult = "";
//			if(orderDayStatus>=0) {
				//直接终止
				SDK.getProcessAPI().terminateById(processInstId, uc.getUID());
				//更新状态
				DBSql.update("update BO_EU_SH_FOODORDER set ORDERSTATUS='6',ISEND=1,cancelreason='"+revocationReason+"' where id='" + itId + "'");
				//消息通知
				String msg = "尊敬的"+applyName+"，您好！您预订"+ycdate+"在"+areaName+"的"+roomnum+"房间的订单取消成功，期待您下次光临！";
				String sendMsg = MsgNoticeController.sendNoticeMsg(uc, msg,"admin", applyUid, "1", "");
				JSONObject sendRes = (JSONObject) JSONObject.parseObject(sendMsg);
				if(sendRes.get("status").equals("0")) {
					tempResult = "取消餐饮订单成功！";
				}else {
					tempResult = "流程终止，但消息发送失败！";
				}
				//短信发送
				SmsUtil sms = new SmsUtil();
				String phone = applyMobile;
				String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID,MnmsConstant.PARAM_CANTEEN_ORDER_CANCEL_TEMPLATE_ID);
				String param = "{'CONTACTPERSON':'"+applyName+"','YCDATE':'"+ycdate+"','AREA':'"+areaName+"','ROOMNUM':'"+roomnum+"'}";
				sms.sendSms(phone,templateId,param);
				conn.commit();
//			}else {
//				//订单已过期，不可取消
//				tempResult = "餐饮订单过期，取消失败。";
//			}
			result.put("status", "0");
//			result.put("orderDayStatus", orderDayStatus+"");
			result.put("message", tempResult);
			
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
			if(conn!=null) {
			    try {
			    	conn.rollback();
			    } catch (SQLException e1) {
			    	e1.printStackTrace();
			    	result.put("status", "1");
					result.put("message", e1.getMessage());
			   }
			}
		}finally {
			DBSql.close(conn);
		}
		return result.toString();
	}
	


//	//资源归还
//	private void vehicleResourceAdd(String typeid,String bDate,String eDate,String addNum) throws Exception {
//		String querySql1 = 
//				" select t.recorddate,b.id bid,b.typeid, b.typeextend, b.initnum " + 
//						"    from BO_EU_SH_RESOURCEMANAGE_B b " + 
//						"    left join BO_EU_SH_RESOURCEMANAGE t " + 
//						"      on b.bindid = t.bindid " + 
//						"   where (t.recorddate between to_date('"+bDate+"', 'YYYY/MM/DD') and " + 
//						"         to_date('"+eDate+"', 'YYYY/MM/DD')) " + 
//						"     and typeextend = 'cheliang' " ;
//		List<Map<String, Object>> dataList1 = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
//		if (dataList1 == null || dataList1.isEmpty()) {
//			throw new Exception("没有任何配置记录");
//		}
//		if (dataList1.size()>1) {
//			throw new Exception("该类型配置记录大于一条");
//		}
//		
//		for(Map<String, Object> dataMap1 : dataList1) {
//			String bid = CoreUtil.objToStr(dataMap1.get("ordernumber"));
//			int vAddNum = Integer.parseInt(addNum);
//			String updateSql1 = "update BO_EU_SH_RESOURCEMANAGE set initnum=initnum+"+vAddNum+" where id='"+bid+"' ";
//			DBSql.update(updateSql1);
//		}
//		
//	}
	

}
