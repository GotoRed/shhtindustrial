package com.awspaas.user.apps.statistic;


import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;



@Controller
public class TempCarState {
	
	@Mapping("com.awspaass.user.apps.TempCarStateDeptWeek")

	public String TempCarStateDeptWeek(UserContext uc, String sid, String year, String weekno, String orgname) {
		JSONObject returnData = new JSONObject();

		try {
			String userId = uc.getUID();

			int yearinfo = Integer.parseInt(year);
			int weekNo = Integer.parseInt(weekno);

			String querySql = "select t.yearinfo,t.weekno,t.orgname,t.username,t.totalwork,t.totalextra,t.totalxj "
					+ "from VIEW_DR_DEPT_WEEKLYGROUP_LIST t " + "where yearinfo ='" + yearinfo + "' and weekno='"
					+ weekNo + "' and orgname='" + orgname + "'";

			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				returnData.put("status", "0");
				JSONArray deptWeekNull = new JSONArray();
				returnData.put("deptWeekList", deptWeekNull);
				returnData.put("message", "本周没有考勤记录");
				return returnData.toString();
			}
			JSONArray deptWeekArr = new JSONArray();

			for (Map<String, Object> dataMap : dataList) {
				JSONObject orderItem = new JSONObject();

				String userName = CoreUtil.objToStr(dataMap.get("username"));
			//	double totalDK = CoreUtil.objToStr(dataMap.get("totalwork"));
			//	double totalOT = CoreUtil.objToDouble(dataMap.get("totalextra"));
			//	double totalXJ = CoreUtil.objToDouble(dataMap.get("totalxj"));

				orderItem.put("userName", userName);
				//orderItem.put("totalDK", totalDK);
				//orderItem.put("totalOT", totalOT);
				//orderItem.put("totalXJ", totalXJ);
				deptWeekArr.add(orderItem);
			}

			// 成功
			returnData.put("status", "0");
			returnData.put("deptWeekList", deptWeekArr);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}

		return returnData.toString();
	}
}
