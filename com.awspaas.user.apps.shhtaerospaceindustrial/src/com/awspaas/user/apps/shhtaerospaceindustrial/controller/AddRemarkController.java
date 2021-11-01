package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import net.sf.json.JSONObject;

@Controller
public class AddRemarkController {
	
	@Mapping("shsy.addRemark")
	public String addRoomRemark(UserContext uc,String odType,String orderId,String userStars,String userComments) {
		String uid = uc.getUID();
		uid = "fanzhenjie";//测试用
		
		JSONObject result = new JSONObject();
		try {
			//查询订单
			String orderSql = "";
			if(odType.equals("kefang")) {
				orderSql = "select id,orderid,bindid,stars,comments from BO_EU_SH_ROOMORDER where applyuid='"+uid+"' and ORDERID='"+orderId+"' ";
			}else if(odType.equals("cheliang")) {
				orderSql = "select id,orderid,bindid,PJXJ stars,PJNR comments from BO_EU_SH_VEHICLEORDER where applyuid='"+uid+"' and ORDERID='"+orderId+"' ";
			}else if(odType.equals("huiyi")) {
				orderSql = "select id,orderid,bindid,stars,comments from BO_EU_SH_MEETINGORDER where applyuid='"+uid+"' and ORDERID='"+orderId+"' ";
			}else if(odType.equals("canyin")) {
				orderSql = "select id,orderid,bindid,stars,comments from BO_EU_SH_FOODORDER where applyuid='"+uid+"' and ORDERID='"+orderId+"' ";
			}else {
				result.put("status", "0");
				result.put("message", "类型传值非法");
				return result.toString();
			}
			List<Map<String, Object>> dataList = DBSql.query(orderSql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				result.put("status", "0");
				result.put("message", "没有任何订单记录");
				return result.toString();
			}
			Map<String, Object> dataMap = dataList.get(0);
			
			String itId = CoreUtil.objToStr(dataMap.get("id"));
			String bindId = CoreUtil.objToStr(dataMap.get("bindid"));
			String stars = CoreUtil.objToStr(dataMap.get("stars"));
			String comments = CoreUtil.objToStr(dataMap.get("comments"));
			boolean isEnd = SDK.getProcessAPI().isEndById(bindId);
			String tempResult = "";
			if(!isEnd) {
				tempResult = "订单尚未结束！";
			}else {
				if(stars!=null&&comments!=null) {
					tempResult = "已经评价过了！";
				}else {
					String updateSql = "";
					if(odType.equals("kefang")) {
						updateSql = "update BO_EU_SH_ROOMORDER set starts='"+userStars+"',comments='"+userComments+"' where id='" + itId + "'";
					}else if(odType.equals("cheliang")) {
						updateSql = "update BO_EU_SH_VEHICLEORDER set PJXJ='"+userStars+"',PJNR='"+userComments+"' where id='" + itId + "'";
					}
					DBSql.update(updateSql);
					tempResult = "评价成功！";
				}
			}
			result.put("status", "0");
			result.put("message", tempResult);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		} 
		return result.toString();
	}
	
}
