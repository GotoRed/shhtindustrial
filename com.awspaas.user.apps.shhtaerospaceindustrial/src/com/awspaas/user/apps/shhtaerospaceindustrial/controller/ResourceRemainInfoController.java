package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.commons.wechat.bean.WechatMessage;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

/**
 * 日期范围内各种类型的资源和剩余数量
 * @author 
 *
 */
@Controller
public class ResourceRemainInfoController {
	
	//更新类资源某日开放
	@Mapping("shsy.test")
	public String test(UserContext uc,String a,String b) {
		
		JSONObject result = new JSONObject();
		try {
			WechatMessage aaa = new WechatMessage();
			aaa.setAgentId("1000003");
//			aaa.setToParty("58");--部门可以多个，用"|"隔开
			aaa.setContent("这是一条来自fzj的企业微信接口测试");
//			aaa.setDescription("xxx");
			aaa.setToUser("fanzhenjie|DingYi");//人员
			aaa.setMsgType("text");
			boolean re = SDK.getNotificationAPI().sendWechatSync(aaa, "wwf28cf3ff5240e200", "1000003");
			if (re) {
				result.put("status", "0");
				result.put("message", "发送成功");
				return result.toString();
			}else {
				result.put("status", "1");
				result.put("message", "发送失败");
			}
			
		}catch(Exception e) {
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
		
	}
	
	/**
	 * 车辆资源配置页面读取
	 * @param uc
	 * @param vehicleType 车辆类型id，可带出类型和品牌名称
	 * @param orderNum
	 * @param bDate
	 * @param eDate
	 * @return
	 */
	@Mapping("shsy.getVehicleRemainList")
	public String getVehicleOrderList(UserContext uc,String vehicleType,String orderNum,String bDate,String eDate) {
		bDate = bDate.replace('-', '/');
		eDate = eDate.replace('-', '/');
		JSONObject result = new JSONObject();
		
		try {
			//各种类型车辆（轿车、中巴……）
			String querySql0 = 
					"select t.itemno,t.cnname from Bo_Act_Dict_Kv_Item t " + 
					"left join BO_ACT_DICT_KV_MAIN a on t.bindid=a.bindid " + 
					"where a.dictkey='shcartype' " + 
					"order by t.itemno ";
			List<Map<String, Object>> dataList0 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
			if (dataList0 == null || dataList0.isEmpty()) {
				JSONArray typeCheckNull = new JSONArray();
				result.put("vehicleTypeRemain", typeCheckNull);
				result.put("status", "0");
				result.put("message", "没有用车类型档案");
				return result.toString();
			}
			JSONArray veTypeArr = new JSONArray();//JSONArray.parseArray(JSON.toJSONString(dataList0));
			JSONObject veTypeItem = null;
			//定义好分类的存放list
			for(Map<String, Object> dataMap0 : dataList0) {
				veTypeItem = new JSONObject();
				String itemno = CoreUtil.objToStr(dataMap0.get("itemno"));
				String cnname = CoreUtil.objToStr(dataMap0.get("cnname"));
				veTypeItem.put("itemno", itemno);
				veTypeItem.put("cnname", cnname);
				veTypeItem.put("items", new JSONArray());
				veTypeArr.add(veTypeItem);

			}
			//资源配置，和对应订单（非取消）剩余数量,
			//读资源配置是为了判断资源是否用完，界面加载可变disable
			String querySql1 = 
					"with t1 as " + 
					" (select t.recorddate, t.vehicleopen resourceopen,b.typeid, b.typeextend, b.initnum " + 
					"    from BO_EU_SH_RESOURCEMANAGE_B b " + 
					"    left join BO_EU_SH_RESOURCEMANAGE t " + 
					"      on b.bindid = t.bindid " + 
					"   where (t.recorddate between to_date('"+bDate+"', 'YYYY/MM/DD') and " + 
					"         to_date('"+eDate+"', 'YYYY/MM/DD')) " + 
					"     and typeextend = 'cheliang'), " + 
					"t2 as " + 
					" (select VEHICLETYPE, orderdate, sum(nvl(VEHICLENUM,0)) ordernum " + 
					"    from BO_EU_SH_VEHICLEORDER " + 
					"   where (orderdate between to_date('"+bDate+"', 'YYYY/MM/DD') and " + 
					"         to_date('"+eDate+"', 'YYYY/MM/DD')) and ORDERSTATUS!='6' " + 
					"   group by VEHICLETYPE, orderdate) " + 
					"select t1.recorddate, " + 
					"		t1.resourceopen, " + 
					"       t1.typeextend, " + 
					"       t3.id vehicletyid, " + 
					"       t3.vehicletype, " + 
					"       t4.cnname vehicletypename, " + 
					"       t3.vehiclelabelname, " + 
					"       t4.cnname||'_'||t3.vehiclelabelname keyword, " + 
					"       t1.initnum, " + 
					"       nvl(t2.ordernum, 0) ordernumber " + 
					"  from t1 " + 
					"  left join t2 " + 
					"    on t1.recorddate = t2.orderdate " + 
					"   and t1.typeid = t2.VEHICLETYPE " + 
					"  left join BO_EU_SH_VEHICLETYPE t3 " + 
					"    on t1.typeid = t3.id " + 
					"  left join Bo_Act_Dict_Kv_Item t4 " +
					"	 on t4.itemno=t3.vehicletype " + 
					"  left join BO_ACT_DICT_KV_MAIN t5 " +
					"	 on t4.bindid=t5.bindid " + 
					" where t3.VEHICLETYPE is not null " +
					"	 and t5.dictkey='shcartype' " ;
					
			if(null!=vehicleType&&!vehicleType.equalsIgnoreCase("null")&&!vehicleType.isEmpty()) {
				querySql1 = querySql1+" and t3.id='"+vehicleType+"' ";
			}
			querySql1 += " order by t1.recorddate";
			List<Map<String, Object>> dataList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				JSONArray typeCheckNull = new JSONArray();
				result.put("vehicleTypeRemain", typeCheckNull);
				result.put("status", "0");
				result.put("message", "没有符合条件的用车记录");
				return result.toString();
			}
			Integer orderN = new Integer(0);
			//检查其中还有剩余的是哪些类型
			if(null!=orderNum&&!orderNum.equalsIgnoreCase("null")&&!orderNum.isEmpty()) {
				orderN = Integer.parseInt(orderNum);
			}
			
			List typeIllEgalList = new ArrayList();//过滤已经不满足的类型，减少开销
			JSONObject vehicleItem = null;
			//检查每种类型日期范围内每天数量够预定
			for(Map<String, Object> dataMap : dataList) {
				vehicleItem = new JSONObject();
				String vehicletype = CoreUtil.objToStr(dataMap.get("vehicletype"));
				
				if(typeIllEgalList.contains(vehicletype)) {
					continue;
				}
				String vehicletyid = CoreUtil.objToStr(dataMap.get("vehicletyid"));
				String typeextend = CoreUtil.objToStr(dataMap.get("typeextend"));
				String vehicletypename = CoreUtil.objToStr(dataMap.get("vehicletypename"));
				String vehiclelabelname = CoreUtil.objToStr(dataMap.get("vehiclelabelname"));
				String keyword = CoreUtil.objToStr(dataMap.get("keyword"));
				Integer initnum = Integer.parseInt(CoreUtil.objToStr(dataMap.get("initnum")));
				Integer ordernumber = Integer.parseInt(CoreUtil.objToStr(dataMap.get("ordernumber")));
				vehicleItem.put("vehicletyid", vehicletyid);
				vehicleItem.put("vehicletype", vehicletype);
				vehicleItem.put("vehicletypename", vehicletypename);
				vehicleItem.put("typeextend", typeextend);
				vehicleItem.put("vehiclelabelname", vehiclelabelname);
				vehicleItem.put("keyword", keyword);
				vehicleItem.put("ordernumber", ordernumber);
				
				if(ordernumber + orderN< initnum) {	
					vehicleItem.put("orderCheck", "true");
				}else {
					vehicleItem.put("orderCheck", "false");
					typeIllEgalList.add(vehicletype);
				}
				//将小类（中巴_考斯特归类给中巴……）归到大类
				for(int i=0;i<veTypeArr.size();i++) {
					JSONObject vitem = veTypeArr.getJSONObject(i);
					String itemno = vitem.get("itemno").toString();
					if(itemno.equals(vehicletype)) {
						veTypeArr.getJSONObject(i).put("items", vehicleItem);
					}
				}
			}
			
			// 成功
			result.put("status", "0");
			result.put("vehicleTypeRemain", veTypeArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	/**
	 * 总调度打开，可查看每个区域、每个包房的可预订情况
	 * @param uc
	 * @param orderDate
	 * @return
	 */
	@Mapping("shsy.getFoodAreaList")
	public String getFoodAreaList(UserContext uc,String orderDate) {
		orderDate = orderDate.replace('-', '/');
		JSONObject result = new JSONObject();
		if(null==orderDate||orderDate.equalsIgnoreCase("null")||orderDate.isEmpty()) {
			result.put("status", "0");
			result.put("message", "日期不能为空");
			return result.toString();
		}
		
		try {
			//资源配置查询
			String querySql0 = "select t.recorddate, " + 
					"         t.vehicleopen resourceopen, " + 
					"         b.typeid, " + 
					"         b.typeextend, " + 
					"         b.initnum " + 
					"    from BO_EU_SH_RESOURCEMANAGE_B b " + 
					"    left join BO_EU_SH_RESOURCEMANAGE t " + 
					"      on b.bindid = t.bindid " + 
					"   where t.recorddate = to_date('"+orderDate+"', 'YYYY/MM/DD') " + 
					"     and typeextend = 'canyin' " ;
			List<Map<String, Object>> dataList0 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
			if (dataList0 == null || dataList0.isEmpty()) {
				
				result.put("status", "0");
				result.put("message", "资源配置当天没有定义记录");
				return result.toString();
			}
			JSONArray foodResourceArr = new JSONArray();
			JSONObject foodResourceItem = null;
			JSONObject remain = null;
			//定义好分类的存放list
			for(Map<String, Object> dataMap0 : dataList0) {
				remain = new JSONObject();
				remain.put("lunchRemain", "true");
				remain.put("supperRemain", "true");
				foodResourceItem = new JSONObject();
				String typeid = CoreUtil.objToStr(dataMap0.get("typeid"));
				String resourceopen = CoreUtil.objToStr(dataMap0.get("resourceopen"));
				foodResourceItem.put("typeid", typeid);
				foodResourceItem.put("resourceopen", resourceopen);
				foodResourceItem.put("remain", remain);
				foodResourceArr.add(foodResourceItem);

			}
			//查询订单里面上午、下午、全天情况，对每个资源可预订进行汇总
			String querySql1 = "select AREA,ORDERSEL,ROOMNUMBER roomid from BO_EU_SH_FOODORDER " + 
					"where orderdate=to_date('2020-06-16', 'YYYY/MM/DD')  " ;
			List<Map<String, Object>> orderList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
			if (orderList == null || orderList.isEmpty()) {
				result.put("foodResourceArr", foodResourceArr);
				result.put("status", "0");
				result.put("message", "无订单记录");
				return result.toString();
			}else {
				//小类为各个包房信息
				JSONObject orderItem = null;
				
				for(Map<String, Object> orderMap : orderList) {
					String roomid = CoreUtil.objToStr(orderMap.get("roomid"));
					String ORDERSEL = CoreUtil.objToStr(orderMap.get("ORDERSEL"));
					
					orderItem = new JSONObject();
					orderItem.put("roomid", roomid);
					orderItem.put("ORDERSEL", ORDERSEL);
					
					//对数据加工，根据ORDERSEL（0午餐，1晚餐，2全天）分类上下午资源
					for(int i=0;i<foodResourceArr.size();i++) {
						JSONObject foodResourceIt = foodResourceArr.getJSONObject(i);
						String typeid = foodResourceIt.get("typeid").toString();
						if(roomid.equals(typeid)) {
							if(ORDERSEL.equals("0")) {
								((JSONObject) foodResourceArr.getJSONObject(i).get("remain")).put("lunchRemain", "false");
							}else if(ORDERSEL.equals("1")) {
								((JSONObject) foodResourceArr.getJSONObject(i).get("remain")).put("supperRemain", "false");
							}else if(ORDERSEL.equals("2")) {
								((JSONObject) foodResourceArr.getJSONObject(i).get("remain")).put("lunchRemain", "false");
								((JSONObject) foodResourceArr.getJSONObject(i).get("remain")).put("supperRemain", "false");
							}else {
								
							}
						}
					}
				}
			
				//根据区域组装
				//各区域查询
				String querySql2 = "select t.id areaid,t.itemno,t.cnname from Bo_Act_Dict_Kv_Item t " + 
						"left join BO_ACT_DICT_KV_MAIN a on t.bindid=a.bindid " + 
						"where t.dictkey='shdininghallarea' " ;
				List<Map<String, Object>> dataList2 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
				if (dataList2 == null || dataList2.isEmpty()) {
					
					result.put("status", "0");
					result.put("message", "没有食堂区域定义记录");
					return result.toString();
				}
				JSONArray foodAreaArr = new JSONArray();
				JSONObject foodAreaItem = null;
				//定义好分类area,将包间按area存放list
				for(Map<String, Object> dataMap2 : dataList2) {
					foodAreaItem = new JSONObject();
					String areaid = CoreUtil.objToStr(dataMap2.get("areaid"));
					String itemno = CoreUtil.objToStr(dataMap2.get("itemno"));
					String cnname = CoreUtil.objToStr(dataMap2.get("cnname"));
					foodAreaItem.put("areaid", areaid);
					foodAreaItem.put("itemno", itemno);
					foodAreaItem.put("cnname", cnname);
					foodAreaItem.put("rooms", new JSONArray());
					foodAreaArr.add(foodAreaItem);
					
				}
				
			}
			// 成功
			result.put("status", "0");
			result.put("foodResourceArr", foodResourceArr);
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
	 * @param roomType 客房类型（标间id/大床房id……）
	 * @param orderNum 预订数量
	 * @param bDate 预定开始日期
	 * @param eDate 预定结束日期
	 * @return 每种类型客房是否可预定
	 */
	@Mapping("shsy.getRoomRemainList")
	public String getRoomOrderList(UserContext uc,String roomType,String orderNum,String bDate,String eDate) {
		bDate = bDate.replace('-', '/');
		eDate = eDate.replace('-', '/');
		JSONObject result = new JSONObject();
		//查询资源初始数量，已预定非取消订单数量
		//读资源配置是为了判断资源是否用完，界面加载可变disable
		try {
			String querySql1 = "with t1 as (select t.recorddate,b.typeid,b.typeextend,b.initnum from BO_EU_SH_RESOURCEMANAGE_B b  " + 
					"left join BO_EU_SH_RESOURCEMANAGE t on b.bindid=t.bindid " + 
					"where (t.recorddate between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD')) and typeextend='kefang'), " + 
					"t2 as " + 
					"(select ROOMTYPE,orderdate,sum(nvl(ORDERNUM,0)) ordernum from BO_EU_SH_ROOMORDER  " + 
					"where (orderdate between to_date('"+bDate+"','YYYY/MM/DD') and to_date('"+eDate+"','YYYY/MM/DD')) and ORDERSTATUS!='6' " + 
					"group by ROOMTYPE,orderdate) " + 
					"select t1.recorddate,t1.typeextend,t3.id roomtyid,t3.roomtype,t1.initnum,nvl(t2.ordernum,0) ordernumber,t3.iconurl,t3.PSLIMIT,t3.SDPRINCE from t1 " + 
					"left join t2 on t1.recorddate=t2.orderdate and t1.typeid=t2.ROOMTYPE " + 
					"left join BO_EU_SH_ROOMTYPE t3 on t1.typeid= t3.id where t3.roomtype is not null " ;
					
			if(null!=roomType&&!roomType.equalsIgnoreCase("null")&&!roomType.isEmpty()) {
				querySql1 = querySql1+" and t3.id='"+roomType+"' ";
			}
			querySql1 += " order by t1.recorddate";
			List<Map<String, Object>> dataList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				JSONArray typeCheckNull = new JSONArray();
				result.put("roomTypeRemain", typeCheckNull);
				result.put("status", "0");
				result.put("message", "没有符合条件的客房记录");
				return result.toString();
			}
			Integer orderN = new Integer(0);
			//检查其中还有剩余的是哪些类型
			if(null!=orderNum&&!orderNum.equalsIgnoreCase("null")&&!orderNum.isEmpty()) {
				orderN = Integer.parseInt(orderNum);
			}
			
			List typeIllEgalList = new ArrayList();//过滤已经不满足的类型，减少开销
			JSONArray roomArr = new JSONArray();
			JSONObject roomItem = null;
			//检查每种类型日期范围内每天数量够预定
			for(Map<String, Object> dataMap : dataList) {
				roomItem = new JSONObject();
				String roomtype = CoreUtil.objToStr(dataMap.get("roomtype"));
				
				if(typeIllEgalList.contains(roomtype)) {
					continue;
				}
				String roomtyid = CoreUtil.objToStr(dataMap.get("roomtyid"));
				String iconurl = CoreUtil.objToStr(dataMap.get("iconurl"));
				String psLimit = CoreUtil.objToStr(dataMap.get("PSLIMIT"));
				String sdPrice = CoreUtil.objToStr(dataMap.get("SDPRINCE"));
				Integer initnum = Integer.parseInt(CoreUtil.objToStr(dataMap.get("initnum")));
				Integer ordernumber = Integer.parseInt(CoreUtil.objToStr(dataMap.get("ordernumber")));
				roomItem.put("roomtyid", roomtyid);
				roomItem.put("roomtype", roomtype);
				roomItem.put("roomIcon", iconurl);
				roomItem.put("psLimit", psLimit);
				roomItem.put("sdPrice", sdPrice);
				
				if(ordernumber + orderN< initnum) {	
					roomItem.put("roomCheck", "true");
				}else {
					roomItem.put("roomCheck", "false");
					typeIllEgalList.add(roomtype);
				}
				roomArr.add(roomItem);
			}
			
			// 成功
			result.put("status", "0");
			result.put("roomTypeRemain", roomArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	/**
	 * 会议室列表和每个会议室预订情况
	 * @param uc
	 * @param orderDate 预订日期
	 * @return
	 */
	@Mapping("shsy.getMeetingRoomList")
	public String getMeetingRoomOrderList(UserContext uc,String orderDate) {
		orderDate = orderDate.replace('-', '/');
		JSONObject result = new JSONObject();
		if(null==orderDate||orderDate.equalsIgnoreCase("null")||orderDate.isEmpty()) {
			result.put("status", "0");
			result.put("message", "日期不能为空");
			return result.toString();
		}
		
		try {
			//各会议室档案
			String querySql0 = "select ID,ROOMID,ROOMNAME from BO_EU_SH_MEETTINGROOM " ;
			List<Map<String, Object>> dataList0 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
			if (dataList0 == null || dataList0.isEmpty()) {
				
				result.put("status", "0");
				result.put("message", "没有会议室档案");
				return result.toString();
			}
			JSONArray roomArr = new JSONArray();
			JSONObject roomItem = null;
			//定义好分类的存放list
			for(Map<String, Object> dataMap0 : dataList0) {
				roomItem = new JSONObject();
				String roomid = CoreUtil.objToStr(dataMap0.get("id"));
				String roomno = CoreUtil.objToStr(dataMap0.get("ROOMID"));
				String roomname = CoreUtil.objToStr(dataMap0.get("ROOMNAME"));
				roomItem.put("roomid", roomid);
				roomItem.put("roomno", roomno);
				roomItem.put("roomname", roomname);
				roomItem.put("orders", new JSONArray());
				roomArr.add(roomItem);

			}
			String querySql1 = "select MEETINGROOM,EISMORING,count(1) ordernum from BO_EU_SH_MEETINGORDER " + 
					"where orderdate=to_date('"+orderDate+"', 'YYYY/MM/DD')  " +
					"group by MEETINGROOM,EISMORING  " ;

			List<Map<String, Object>> dataList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
//			JSONArray orders = new JSONArray();

			if (dataList == null || dataList.isEmpty()) {
				result.put("roomArr", roomArr);
				result.put("status", "0");
				result.put("message", "无订单记录");
				return result.toString();
			}else {
				//小类各个会议室订单
//				JSONObject roomOrder = null;
//				JSONArray orderArr = new JSONArray();
				JSONObject orderItem = null;
				
				for(Map<String, Object> dataMap : dataList) {
					String MEETINGROOM = CoreUtil.objToStr(dataMap.get("MEETINGROOM"));
					String EISMORING = CoreUtil.objToStr(dataMap.get("EISMORING"));
					orderItem = new JSONObject();
					orderItem.put("MEETINGROOM", MEETINGROOM);
					orderItem.put("EISMORING", EISMORING);
					
					//将上下午预订情况归到会议室档案
					for(int i=0;i<roomArr.size();i++) {
						JSONObject roomit = roomArr.getJSONObject(i);
						String itemid = roomit.get("roomid").toString();
						if(itemid.equals(MEETINGROOM)) {
							((JSONArray) roomArr.getJSONObject(i).get("orders")).add(orderItem);
						}
					}
				}
			}
			// 成功
			result.put("status", "0");
			result.put("roomArr", roomArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	
	/**
	 * 会议室列表和是否可预订
	 * @param uc
	 * @param orderDate
	 * @param meetingRoom 预订会议室
	 * @param orderType 0上午，1下午，2全天
	 * @return
	 */
	@Mapping("shsy.meetingRoomCheck")
	public String meetingRoomOrderCheck(UserContext uc,String orderDate,String meetingRoom,String orderType) {
		orderDate = orderDate.replace('-', '/');

		JSONObject result = new JSONObject();

		try {
			//查询订单
			String querySql0 = "select t.MEETINGROOM,EISMORING,count(1) ordernum from BO_EU_SH_MEETINGORDER " + 
					"group by MEETINGROOM,EISMORING " ;
			List<Map<String, Object>> dataList0 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
			if (dataList0 == null || dataList0.isEmpty()) {
				JSONArray typeCheckNull = new JSONArray();
				result.put("vehicleTypeRemain", typeCheckNull);
				result.put("status", "0");
				result.put("message", "没有用车类型档案");
				return result.toString();
			}
			JSONArray veTypeArr = new JSONArray();//JSONArray.parseArray(JSON.toJSONString(dataList0));
			JSONObject veTypeItem = null;
			//定义好分类的存放list
			for(Map<String, Object> dataMap0 : dataList0) {
				veTypeItem = new JSONObject();
				String itemno = CoreUtil.objToStr(dataMap0.get("itemno"));
				String cnname = CoreUtil.objToStr(dataMap0.get("cnname"));
				veTypeItem.put("itemno", itemno);
				veTypeItem.put("cnname", cnname);
				veTypeItem.put("items", new JSONArray());
				veTypeArr.add(veTypeItem);

			}
			
			
			// 成功
			result.put("status", "0");
			result.put("vehicleTypeRemain", veTypeArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	
	
	//更新类资源某日开放
	@Mapping("shsy.resourceOpen")
	public String updateResourceOpen(UserContext uc,String configDate,String odType) {
		JSONObject result = new JSONObject();
		try {
			String querySql1 = "select id from BO_EU_SH_RESOURCEMANAGE where RECORDDATE=to_date('"+configDate+"', 'YYYY/MM/DD') ";
			List<Map<String, Object>> dataList1 = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});

			if (dataList1 == null || dataList1.isEmpty()) {
				result.put("status", "0");
				result.put("message", "没有任何配置记录");
				return result.toString();
			}
			if (dataList1.size()>1) {
				result.put("status", "0");
				result.put("message", "该类型配置记录大于一条");
				return result.toString();
			}
			Map<String, Object> dataMap1 = dataList1.get(0);
			String configId = CoreUtil.objToStr(dataMap1.get("id"));
			String tempMsg = "";
			if(odType.equals("cheliang")) {
				String updateSql1 = "update BO_EU_SH_RESOURCEMANAGE set VEHICLEOPEN='1' where id='" + configId + "'";
				DBSql.update(updateSql1);
				tempMsg = "更新数据成功";
			}else {
				tempMsg = "该类型未开发更新功能";
			}
			result.put("status", "0");
			result.put("message", tempMsg);
			
		}catch(Exception e) {
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
		
	}
	
	
//	/**
//	 * 
//	 * @param uc
//	 * @param configDate 资源日期
//	 * @return 车辆资源配置页面读取
//	 */
//	@Mapping("shsy.getVehicleResourceList")
//	public String getVehicleResource(UserContext uc,String configDate) {
//		configDate = configDate.replace('-', '/');
//		JSONObject result = new JSONObject();
//		String vehicleOpen = null;
//		//日期，资源类型，房间类型，初始数量，已预定数量
//		try {
//			//各种类型车辆（轿车、中巴……）
//			String querySql0 = 
//					"select t.itemno,t.cnname from Bo_Act_Dict_Kv_Item t " + 
//					"left join BO_ACT_DICT_KV_MAIN a on t.bindid=a.bindid " + 
//					"where a.dictkey='shcartype' " + 
//					"order by t.itemno ";
//			List<Map<String, Object>> dataList0 = DBSql.query(querySql0, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList0 == null || dataList0.isEmpty()) {
//				JSONArray typeCheckNull = new JSONArray();
//				result.put("vehicleTypeRemain", typeCheckNull);
//				result.put("status", "0");
//				result.put("message", "没有用车类型档案");
//				return result.toString();
//			}
//			JSONArray veTypeArr = new JSONArray();//JSONArray.parseArray(JSON.toJSONString(dataList0));
//			JSONObject veTypeItem = null;
//			//定义好分类的存放list
//			for(Map<String, Object> dataMap0 : dataList0) {
//				veTypeItem = new JSONObject();
//				String itemno = CoreUtil.objToStr(dataMap0.get("itemno"));
//				String cnname = CoreUtil.objToStr(dataMap0.get("cnname"));
//				veTypeItem.put("itemno", itemno);
//				veTypeItem.put("cnname", cnname);
//				veTypeItem.put("items", new JSONArray());
//				veTypeArr.add(veTypeItem);
//			}
//			//车辆所有小类
//			String querySql1 = "with t1 as( " + 
//					"select c.id resourceid,t4.cnname||'_'||c.vehiclelabelname resourcetype,c.vehicletype, b.typeextend, b.initnum,b.bindid " + 
//					"	from BO_EU_SH_RESOURCEMANAGE_B b " + 
//					"	left join BO_EU_SH_VEHICLETYPE c on b.typeid = c.id " + 
//					"  left join Bo_Act_Dict_Kv_Item t4 " +
//					"	 on t4.itemno=c.vehicletype " + 
//					"  left join BO_ACT_DICT_KV_MAIN t5 " +
//					"	 on t4.bindid=t5.bindid " + 
//					"where typeextend = 'cheliang' and t5.dictkey='shcartype' " + 
//					") " + 
//					"select t1.resourceid,t1.resourcetype,t1.vehicletype,t1.typeextend,t1.initnum,t.recorddate,t.vehicleopen from t1 " + 
//					"left join BO_EU_SH_RESOURCEMANAGE t on t1.bindid = t.bindid " + 
//					"where t.recorddate = to_date('"+configDate+"', 'YYYY/MM/DD') " ;
//					
//			List<Map<String, Object>> dataList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList == null || dataList.isEmpty()) {
//				result.put("status", "0");
//				result.put("message", "没有任何订单记录");
//				return result.toString();
//			}
//			for (Map<String, Object> dataMap : dataList) {
//				JSONObject configItem = new JSONObject();
//				String resourceId = CoreUtil.objToStr(dataMap.get("resourceid"));
//				String resourceType = CoreUtil.objToStr(dataMap.get("resourcetype"));
//				String vehicleType = CoreUtil.objToStr(dataMap.get("vehicletype"));
//				String typeextend = CoreUtil.objToStr(dataMap.get("typeextend"));
//				String initNum = CoreUtil.objToStr(dataMap.get("initnum"));
//				String vehicleOpen1 = CoreUtil.objToStr(dataMap.get("vehicleopen"));
//				if(null==vehicleOpen) {
//					vehicleOpen = vehicleOpen1;
//				}else if(!vehicleOpen.equals(vehicleOpen1)) {
//					throw new Exception("子项对应主表开关状态不一致！");
//				}
//				configItem.put("resourceId", resourceId);
//				configItem.put("resourceType", resourceType);
//				configItem.put("vehicleType", vehicleType);
//				configItem.put("typeextend", typeextend);
//				configItem.put("initNum", initNum);
//				configItem.put("vehicleOpen", vehicleOpen1);
//				
//				//将小类（中巴_考斯特归类给中巴……）归到大类
//				for(int i=0;i<veTypeArr.size();i++) {
//					JSONObject vitem = veTypeArr.getJSONObject(i);
//					String itemno = vitem.get("itemno").toString();
//					if(itemno.equals(vehicleType)) {
//						veTypeArr.getJSONObject(i).put("items", configItem);
//					}
//				}
//				
//			}
//			
//			// 成功
//			result.put("status", "0");
//			result.put("vehicleOpen", vehicleOpen);
//			result.put("veTypeArr", veTypeArr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result.put("status", "1");
//			result.put("message", e.getMessage());
//		}
//		return result.toString();
//	}
//	
	

	
	
//	//所有资源配置页
//	@Mapping("shsy.getResourceConfigList")
//	public String getResourceConfig(UserContext uc,String configDate) {
//		configDate = configDate.replace('-', '/');
//		JSONObject result = new JSONObject();
//		//日期，资源类型，房间类型，初始数量，已预定数量
//		try {
//			String querySql1 = "with t1 as( " + 
//					"select c.id resourceid,c.roomtype resourcetype, b.typeextend, b.initnum,b.bindid " + 
//					"from BO_EU_SH_RESOURCEMANAGE_B b " + 
//					"left join BO_EU_SH_ROOMTYPE c on b.typeid = c.id " + 
//					"where typeextend = 'kefang' " + 
//					"union( " + 
//					"select c.id resourceid,t4.cnname||'_'||c.vehiclelabelname resourcetype, b.typeextend, b.initnum,b.bindid " + 
//					"	from BO_EU_SH_RESOURCEMANAGE_B b " + 
//					"	left join BO_EU_SH_VEHICLETYPE c on b.typeid = c.id " + 
//					"  left join Bo_Act_Dict_Kv_Item t4 " +
//					"	 on t4.itemno=c.vehicletype " + 
//					"  left join BO_ACT_DICT_KV_MAIN t5 " +
//					"	 on t4.bindid=t5.bindid " + 
//					"where typeextend = 'cheliang' and t5.dictkey='shcartype' " + 
//					") " + 
//					"union " + 
//					"(select c.id resourceid,c.roomno resourcetype, b.typeextend, b.initnum,b.bindid " + 
//					"from BO_EU_SH_RESOURCEMANAGE_B b " + 
//					"left join BO_EU_SH_FOODROOMDOC c on b.typeid = c.id " + 
//					"where typeextend = 'canyin' " + 
//					") " + 
//					"union " + 
//					"(select c.id resourceid,c.roomname resourcetype, b.typeextend, b.initnum,b.bindid " + 
//					"from BO_EU_SH_RESOURCEMANAGE_B b " + 
//					"left join BO_EU_SH_MEETTINGROOM c on b.typeid = c.id " + 
//					"where typeextend = 'huiyi' " + 
//					")     " + 
//					") " + 
//					"select t1.resourceid,t1.resourcetype,t1.typeextend,t1.initnum,t.recorddate,t.foodopen,t.meetingroomopen,t.roomopen,t.vehicleopen from t1 " + 
//					"left join BO_EU_SH_RESOURCEMANAGE t on t1.bindid = t.bindid " + 
//					"where " + 
//					"t.recorddate = to_date('"+configDate+"', 'YYYY/MM/DD') " ;
//					
//
//			List<Map<String, Object>> dataList = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
//			if (dataList == null || dataList.isEmpty()) {
//				result.put("status", "0");
//				result.put("message", "没有任何订单记录");
//				return result.toString();
//			}
//			JSONArray configArr = new JSONArray();
//			for (Map<String, Object> dataMap : dataList) {
//				JSONObject configItem = new JSONObject();
//				String resourceId = CoreUtil.objToStr(dataMap.get("resourceid"));
//				String resourceType = CoreUtil.objToStr(dataMap.get("resourcetype"));
//				String typeextend = CoreUtil.objToStr(dataMap.get("typeextend"));
//				String initNum = CoreUtil.objToStr(dataMap.get("initnum"));
//				String recordDate = CoreUtil.objToStr(dataMap.get("recorddate"));
//				String foodOpen = CoreUtil.objToStr(dataMap.get("foodopen"));
//				String meetingRoomOpen = CoreUtil.objToStr(dataMap.get("meetingroomopen"));
//				String roomOpen = CoreUtil.objToStr(dataMap.get("roomopen"));
//				String vehicleOpen = CoreUtil.objToStr(dataMap.get("meetingroomopen"));
//				
//				configItem.put("resourceId", resourceId);
//				configItem.put("itemName", resourceType);
//				configItem.put("typeextend", typeextend);
//				configItem.put("initNum", initNum);
//				configArr.add(configItem);
//			}
//			
//			// 成功
//			result.put("status", "0");
//			result.put("configArr", configArr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result.put("status", "1");
//			result.put("message", e.getMessage());
//		}
//		return result.toString();
//	}

	/**
	 * 
	 * @param uc
	 * @param typeid
	 * @param oddate
	 * @param minusNum
	 * @return
	 */
	@Mapping("shsy.vehicleMinus")
	public String vehicleResourceMinus(UserContext uc,String typeid,String oddate,String minusNum) {
		oddate = oddate.replace('-', '/');
		JSONObject result = new JSONObject();
		//查询车辆资源初始数量、非取消预订数量
		String querySql1 = 
				"with t1 as " + 
				" (select t.recorddate,b.id bid,b.typeid, b.typeextend, b.initnum " + 
				"    from BO_EU_SH_RESOURCEMANAGE_B b " + 
				"    left join BO_EU_SH_RESOURCEMANAGE t " + 
				"      on b.bindid = t.bindid " + 
				"   where (t.recorddate=to_date('"+oddate+"', 'YYYY/MM/DD')) " + 
				"     and typeextend = 'cheliang'), " + 
				"t2 as " + 
				" (select VEHICLETYPE, orderdate, sum(nvl(VEHICLENUM, 0)) ordernum " + 
				"    from BO_EU_SH_VEHICLEORDER " + 
				"   where (orderdate=to_date('"+oddate+"', 'YYYY/MM/DD')) and ORDERSTATUS!='6' " + 
				"   group by VEHICLETYPE, orderdate) " + 
				"select t1.recorddate, " + 
				"       t1.typeextend, " + 
				"       t1.bid, " + 
				"       t3.id vehicletyid, " + 
				"       t1.initnum, " + 
				"       nvl(t2.ordernum, 0) ordernumber " + 
				"  from t1 " + 
				"  left join t2 " + 
				"    on t1.recorddate = t2.orderdate " + 
				"   and t1.typeid = t2.VEHICLETYPE " + 
				"  left join BO_EU_SH_VEHICLETYPE t3 " + 
				"    on t1.typeid = t3.id " + 
				" where t3.id = '"+typeid+"' ";
		Connection conn = DBSql.open();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<Map<String, Object>> dataList1 = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
		if (dataList1 == null || dataList1.isEmpty()) {
			result.put("status", "0");
			result.put("message", "没有任何配置记录");
			result.put("orderList", new JSONArray());
			return result.toString();
		}
		if (dataList1.size()>1) {
			result.put("status", "0");
			result.put("message", "该类型配置记录大于一条");
			result.put("orderList", new JSONArray());
			return result.toString();
		}
		
		Map<String, Object> dataMap1 = dataList1.get(0);
		Integer initnum = Integer.parseInt(CoreUtil.objToStr(dataMap1.get("initnum")));
		Integer ordernumber = Integer.parseInt(CoreUtil.objToStr(dataMap1.get("ordernumber")));
		int vMinusNum = Integer.parseInt(minusNum);
		if(initnum-vMinusNum>=ordernumber) {
			String bid = CoreUtil.objToStr(dataMap1.get("bid"));
			String updateSql1 = "update BO_EU_SH_RESOURCEMANAGE_B set initnum=initnum-"+vMinusNum+" where id='"+bid+"' ";
			DBSql.update(updateSql1);
			result.put("message", "更新成功");
		}else {
			result.put("message", "数量不足");
		}
		if(conn!=null) {
		    try {
		    	conn.rollback();
		    	conn.commit();
		    } catch (SQLException e1) {
		    	e1.printStackTrace();
		    	result.put("status", "1");
				result.put("message", e1.getMessage());
		   }finally {
	 			DBSql.close(conn);
			}
		}
		DBSql.close(conn);
		result.put("status", "0");
		return result.toString();
	}
	
	@Mapping("shsy.vehicleAdd")
	public String vehicleResourceAdd(String typeid,String oddate,String addNum) {
		String querySql1 = 
				" select t.recorddate,b.id bid,b.typeid, b.typeextend, b.initnum " + 
						"    from BO_EU_SH_RESOURCEMANAGE_B b " + 
						"    left join BO_EU_SH_RESOURCEMANAGE t " + 
						"      on b.bindid = t.bindid " + 
						"   where (t.recorddate=to_date('"+oddate+"', 'YYYY/MM/DD')) " + 
						"     and typeextend = 'cheliang' and b.typeid='"+typeid+"' ";
		List<Map<String, Object>> dataList1 = DBSql.query(querySql1, new ColumnMapRowMapper(), new Object[] {});
		JSONObject result = new JSONObject();
		if (dataList1 == null || dataList1.isEmpty()) {
			result.put("status", "0");
			result.put("message", "没有任何配置记录");
			result.put("orderList", new JSONArray());
			return result.toString();
		}
		if (dataList1.size()>1) {
			result.put("status", "0");
			result.put("message", "该类型配置记录大于一条");
			result.put("orderList", new JSONArray());
			return result.toString();
		}
		
		Map<String, Object> dataMap1 = dataList1.get(0);
		String bid = CoreUtil.objToStr(dataMap1.get("bid"));
		int vAddNum = Integer.parseInt(addNum);
		String updateSql1 = "update BO_EU_SH_RESOURCEMANAGE_B set initnum=initnum+"+vAddNum+" where id='"+bid+"' ";
		DBSql.update(updateSql1);
		result.put("message", "更新成功");
		result.put("orderList", new JSONArray());
		result.put("status", "0");
		return result.toString();
	}
	
}
