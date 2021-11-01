/**
 * @Description 任务完成后更新手机界面显示值标题和手机界面显示值内容
 * @author WU LiHua
 * @date 2020年2月8日 下午3:35:45
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.event;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.core.delegate.ProcessExecutionContext;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListener;
import com.actionsoft.bpms.bpmn.engine.listener.ExecuteListenerInterface;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

public class UpdateExchangeTitleAndContent extends ExecuteListener implements ExecuteListenerInterface {

	public String getDescription() {
        return "更新手机界面显示值标题和手机界面显示值内容！";
    }
	
	@Override
	public void execute(ProcessExecutionContext pec) throws Exception {
		try {
			String bindId = pec.getProcessInstance().getId();//流程实例ID
			String queryBookType = "SELECT ORDERTYPE,BDATE FROM BO_EU_SH_JLCENTER_TYORDERHEAD WHERE BINDID = '"+bindId+"'";
			String orderType = CoreUtil.objToStr(DBSql.getString(queryBookType, "ORDERTYPE"));//预订类型
			String ydsj = CoreUtil.objToStr(DBSql.getString(queryBookType, "BDATE"));//预订时间
			if(!ydsj.equals("")) {
				ydsj = ydsj.substring(0, 16);
			}
			String orderTypeStr = "";
			StringBuffer strBuf = new StringBuffer();
			if(!orderType.equals("")) {
				String[] orderTypeArr = orderType.split(",");
				for (int i = 0; i < orderTypeArr.length; i++) {
					orderTypeStr = orderTypeArr[i];
					if(orderTypeStr.equals("0")) {
						orderTypeStr = "住宿";
					}else if(orderTypeStr.equals("1")) {
						orderTypeStr = "会议";
					}else if(orderTypeStr.equals("2")) {
						orderTypeStr = "餐饮";
					}
					strBuf.append(orderTypeStr).append("&");
				}
				orderTypeStr = strBuf.substring(0, strBuf.length()-1);//预订类型
			}
			if(orderType.contains(",")) {//如果预定类型有多个
				String kfqk = "";//客房情况
				String hyqk = "";//会议室情况
				String cyqk = "";//餐饮情况
				JSONObject obj = new JSONObject();
				if(orderType.contains("0")) {//0:客房|1:会议室|2:餐饮(上航_交流中心统一预订_客房)
					String queryKfqk = "SELECT CONCAT(CONCAT(CONCAT((CASE WHEN ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='2' THEN "
										+ "'商务大床房' WHEN ROOMTYPE='3' THEN '行政大床房' WHEN ROOMTYPE='4' THEN '标准标间' ELSE '' END),"
										+ "'('),ORDERNUM),')') KFXX FROM BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"'";
					List<Map<String, Object>> queryDataList = DBSql.query(queryKfqk, new ColumnMapRowMapper(), new Object[] {});
					if(queryDataList != null && queryDataList.size() > 0) {
						for (int i = 0; i < queryDataList.size(); i++) {
							Map<String, Object> map = queryDataList.get(i);
							String kfxx = CoreUtil.objToStr(map.get("KFXX"));//客房信息
							kfqk = kfqk + kfxx+"|";
						}
						kfqk = kfqk.substring(0, kfqk.length()-1);
					}
					obj.put("住宿情况", kfqk);
				}else {
					kfqk = "无";
					obj.put("住宿情况", kfqk);
				}
				if(orderType.contains("1")) {
					String queryHyqk = "SELECT CONCAT(CONCAT(CONCAT(MEETINGROOM,'('),JOINMEETPSNNUM),')') HYXX FROM BO_EU_SH_JLCENTER_TYORDER_MEET"
										+ " WHERE BINDID = '"+bindId+"'";
					List<Map<String, Object>> queryDataList = DBSql.query(queryHyqk, new ColumnMapRowMapper(), new Object[] {});
					if(queryDataList != null && queryDataList.size() > 0) {
						for (int i = 0; i < queryDataList.size(); i++) {
							Map<String, Object> map = queryDataList.get(i);
							String hyxx = CoreUtil.objToStr(map.get("HYXX"));//会议信息
							hyqk = hyqk + hyxx+"|";
						}
						hyqk = hyqk.substring(0, hyqk.length()-1);
					}
					obj.put("会议情况", hyqk);
				}else {
					hyqk = "无";
					obj.put("会议情况", hyqk);
				}
				if(orderType.contains("2")) {
					String queryCyqk = "SELECT ROOMNUM,PERSONNUM FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"'";
					List<Map<String, Object>> queryDataList = DBSql.query(queryCyqk, new ColumnMapRowMapper(), new Object[] {});
					if(queryDataList != null && queryDataList.size() > 0) {
						for (int i = 0; i < queryDataList.size(); i++) {
							Map<String, Object> map = queryDataList.get(i);
							String room = CoreUtil.objToStr(map.get("ROOMNUM"));//用餐包厢
							String person = CoreUtil.objToStr(map.get("PERSONNUM"));//用餐人数
							if(!room.equals("")) {
								String rooms = "";
								String[] eatingStr = room.split(",");
								for (int j = 0; j < eatingStr.length; j++) {
									room = eatingStr[j];
									if(room.equals("1")) {
										rooms = rooms + "大厅自助餐"+"/";
									}
									if(room.equals("2")) {
										rooms = rooms + "小包7"+"/";
									}
									if(room.equals("3")) {
										rooms = rooms + "小包6"+"/";
									}
									if(room.equals("4")) {
										rooms = rooms + "小包5"+"/";
									}
									if(room.equals("5")) {
										rooms = rooms + "中包"+"/";
									}
									if(room.equals("6")) {
										rooms = rooms + "大包"+"/";
									}
								}
								cyqk = cyqk + rooms.substring(0, rooms.length()-1)+"("+person+")";
							}
							cyqk = cyqk +"|";
						}
						cyqk = cyqk.substring(0, cyqk.length()-1);
					}
					obj.put("订餐情况", cyqk);
				}else {
					cyqk = "无";
					obj.put("订餐情况", cyqk);
				}
				obj.put("预订时间", ydsj);
				DBSql.update("UPDATE BO_EU_SH_JLCENTER_TYORDERHEAD SET MOBILETITLE = '"+orderTypeStr+"',MOBILECONTENT='"+obj.toString()+"' WHERE BINDID = '"+bindId+"'");
			}
			if(orderType.equals("0")) {//0:客房|1:会议室|2:餐饮(上航_交流中心统一预订_客房)
				String queryKfData = "SELECT BDATE,EDATE,CONCAT(CONCAT(CONCAT((CASE WHEN ROOMTYPE='1' THEN '标准大床房' WHEN ROOMTYPE='2' THEN '商务大床房' "
							+ "WHEN ROOMTYPE='3' THEN '行政大床房' WHEN ROOMTYPE='4' THEN '标准标间' ELSE '' END),'('),ORDERNUM),')') DATA "
							+ "FROM BO_EU_SH_JLCENTER_TYORDER_ROOM WHERE BINDID = '"+bindId+"'";
				List<Map<String, Object>> queryDataList = DBSql.query(queryKfData, new ColumnMapRowMapper(), new Object[] {});
				if(queryDataList != null && queryDataList.size() > 0) {
					JSONObject obj = new JSONObject();
					StringBuffer bdateSbf = new StringBuffer();
					StringBuffer edateSbf = new StringBuffer();
					StringBuffer dataSbf = new StringBuffer();
					for (int i = 0; i < queryDataList.size(); i++) {
						Map<String, Object> map = queryDataList.get(i);
						String bdate = CoreUtil.objToStr(map.get("BDATE"));//抵店日期
						String edate = CoreUtil.objToStr(map.get("EDATE"));//离店日期
						String data = CoreUtil.objToStr(map.get("DATA"));//客房类型(客房数量)
						if(!bdate.equals("")) {
							bdate = bdate.substring(0, 16);
						}
						if(!edate.equals("")) {
							edate = edate.substring(0, 16);
						}
						if(i==0 || i==queryDataList.size()-1) {
							bdateSbf.append(bdate).append("~");
							edateSbf.append(edate).append("~");
						}
						dataSbf.append(data).append("~");
					}
					String bdate = bdateSbf.substring(0, bdateSbf.length()-1);
					String edate = edateSbf.substring(0, edateSbf.length()-1);
					String data = dataSbf.substring(0, dataSbf.length()-1);
					obj.put("预订时间", ydsj);
					obj.put("抵店时间", bdate);
					obj.put("离店时间", edate);
					obj.put("房型种类及数量", data);
					DBSql.update("UPDATE BO_EU_SH_JLCENTER_TYORDERHEAD SET MOBILETITLE = '"+orderTypeStr+"',MOBILECONTENT='"+obj.toString()+"' WHERE BINDID = '"+bindId+"'");
				}
			}
			if(orderType.equals("1")) {
				String queryMeetData = "SELECT BDATE,MEETINGROOM,JOINMEETPSNNUM DATA FROM BO_EU_SH_JLCENTER_TYORDER_MEET WHERE BINDID = '"+bindId+"'";
				List<Map<String, Object>> queryDataList = DBSql.query(queryMeetData, new ColumnMapRowMapper(), new Object[] {});
				if(queryDataList != null && queryDataList.size() > 0) {
					JSONObject obj = new JSONObject();
					StringBuffer bdateSbf = new StringBuffer();
					StringBuffer meetingRoomSbf = new StringBuffer();
					StringBuffer joinMeetNumSbf = new StringBuffer();
					for (int i = 0; i < queryDataList.size(); i++) {
						Map<String, Object> map = queryDataList.get(i);
						String bdate = CoreUtil.objToStr(map.get("BDATE"));//会议开始日期
						String meetingRoom = CoreUtil.objToStr(map.get("MEETINGROOM"));//会议室
						String joinMeetNum = CoreUtil.objToStr(map.get("DATA"));//参会人数
						if(!bdate.equals("")) {
							bdate = bdate.substring(0, 16);
						}
						if(i==0 || i==queryDataList.size()-1) {
							bdateSbf.append(bdate).append("~");
						}
						meetingRoomSbf.append(meetingRoom).append("~");
						joinMeetNumSbf.append(joinMeetNum).append("~");
					}
					String bdate = bdateSbf.substring(0, bdateSbf.length()-1);
					String meetingRoom = meetingRoomSbf.substring(0, meetingRoomSbf.length()-1);
					String joinMeetNum = joinMeetNumSbf.substring(0, joinMeetNumSbf.length()-1);
					obj.put("预订时间", ydsj);
					obj.put("会议时间", bdate);
					obj.put("会议地点", meetingRoom);
					obj.put("参会人数", joinMeetNum);
					DBSql.update("UPDATE BO_EU_SH_JLCENTER_TYORDERHEAD SET MOBILETITLE = '"+orderTypeStr+"',MOBILECONTENT='"+obj.toString()+"' WHERE BINDID = '"+bindId+"'");
				}
			}
			if(orderType.equals("2")) {
				String queryCyData = "SELECT EATTINGSTARTDATE,ROOMNUM,PERSONNUM FROM BO_EU_SH_JLCENTER_TYORDER_DINR WHERE BINDID = '"+bindId+"'";
				List<Map<String, Object>> queryDataList = DBSql.query(queryCyData, new ColumnMapRowMapper(), new Object[] {});
				if(queryDataList != null && queryDataList.size() > 0) {
					JSONObject obj = new JSONObject();
					StringBuffer bdateSbf = new StringBuffer();
					StringBuffer eatingRoomSbf = new StringBuffer();
					StringBuffer eatingNumSbf = new StringBuffer();
					for (int i = 0; i < queryDataList.size(); i++) {
						Map<String, Object> map = queryDataList.get(i);
						String bdate = CoreUtil.objToStr(map.get("EATTINGSTARTDATE"));//用餐开始时间
						String eatingRoom = CoreUtil.objToStr(map.get("ROOMNUM"));//用餐地点
						String eatingNum = CoreUtil.objToStr(map.get("PERSONNUM"));//用餐人数
						if(!bdate.equals("")) {
							bdate = bdate.substring(0, 16);
						}
						if(i==0 || i==queryDataList.size()-1) {
							bdateSbf.append(bdate).append("~");
						}
						String room = "";
						if(!eatingRoom.equals("")) {
							String[] eatingStr = eatingRoom.split(",");
							for (int j = 0; j < eatingStr.length; j++) {
								eatingRoom = eatingStr[j];
								if(eatingRoom.equals("1")) {
									room = room + "大厅自助餐"+"|";
								}
								if(eatingRoom.equals("2")) {
									room = room + "小包7"+"|";
								}
								if(eatingRoom.equals("3")) {
									room = room + "小包6"+"|";
								}
								if(eatingRoom.equals("4")) {
									room = room + "小包5"+"|";
								}
								if(eatingRoom.equals("5")) {
									room = room + "中包"+"|";
								}
								if(eatingRoom.equals("6")) {
									room = room + "大包"+"|";
								}
							}
							room = room.substring(0, room.length()-1);
						}
						eatingNumSbf.append(eatingNum).append("~");
						eatingRoomSbf.append(room).append("~");
					}
					String bdate = bdateSbf.substring(0, bdateSbf.length()-1);
					String eatingRoom = eatingRoomSbf.substring(0, eatingRoomSbf.length()-1);
					String eatingNum = eatingNumSbf.substring(0, eatingNumSbf.length()-1);
					obj.put("预订时间", ydsj);
					obj.put("用餐开始时间", bdate);
					obj.put("用餐地点", eatingRoom);
					obj.put("用餐人数", eatingNum);
					DBSql.update("UPDATE BO_EU_SH_JLCENTER_TYORDERHEAD SET MOBILETITLE = '"+orderTypeStr+"',MOBILECONTENT='"+obj.toString()+"' WHERE BINDID = '"+bindId+"'");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
