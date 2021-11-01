package com.awspaas.user.apps.shhtaerospaceindustrial.weixiu;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import java.util.List;
import java.util.Map;

public class RepairController {
	/**
	 * 
	 * @Description 根据月份获得报修信息数据
	 * @author wanghb
	 * @date 2020年2月7日 上午10:26:36
	 * @param nf 年份
	 * @return
	 */
	@Mapping("jch5.Sh_getRepairData")
	public String getRepairData(UserContext uc, String ny) {
		String uid = uc.getUID();//登录人账号
		ny = ny.replace('-', '/');
		JSONObject result = new JSONObject();
		try {
			// 获取当年考勤周期
			String querySql = "SELECT * FROM BO_EU_SH_REPAIR  WHERE  to_char(KQZQ,'YYYY/MM')='" + ny + "' ORDER BY KQZQ ASC";
			List<Map<String, Object>> dataList = DBSql.query(querySql, new ColumnMapRowMapper(), new Object[] {});
			if (dataList == null || dataList.isEmpty()) {
				result.put("status", "0");
				JSONArray orderNull = new JSONArray();
				result.put("orderList", orderNull);
				result.put("message", "没有任何报修记录");
				return result.toString();
			}
			JSONArray kqZqQkArr = new JSONArray();
			for (Map<String, Object> dataMap : dataList) {
				JSONObject kqZqQk = new JSONObject();
				String kqny = CoreUtil.objToStr(dataMap.get("KQZQ"));// 当前考勤年月
				String kqzqmc = CoreUtil.objToStr(dataMap.get("KQZQMC"));// 当前考勤周期名称
				String ksrq = CoreUtil.objToStr(dataMap.get("KSRQ"));// 当前考勤周期开始时间
				String jsrq = CoreUtil.objToStr(dataMap.get("JSRQ"));// 当前考勤周期结束时间
				String bz = CoreUtil.objToStr(dataMap.get("BZ"));// 备注
				kqZqQk.put("kqNy", kqny);
				kqZqQk.put("kqZqMc", kqzqmc);
				kqZqQk.put("ksRq", ksrq.equals("") ? "" : ksrq.substring(0, 10));
				kqZqQk.put("jsRq", jsrq.equals("") ? "" : jsrq.substring(0, 10));
				kqZqQk.put("bz", bz);
				kqZqQkArr.add(kqZqQk);
			}
			// 成功
			result.put("status", 0);
			result.put("kqZqQk", kqZqQkArr);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
		}
		return result.toString();
	}

}

