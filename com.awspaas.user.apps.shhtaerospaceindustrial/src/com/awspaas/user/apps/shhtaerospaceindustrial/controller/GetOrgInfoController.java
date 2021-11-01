package com.awspaas.user.apps.shhtaerospaceindustrial.controller;

import java.util.List;
import java.util.Map;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.org.model.UserMapModel;
import com.actionsoft.bpms.server.SSOUtil;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

@Controller
public class GetOrgInfoController {
	/**
	 * @Description 获取登录人信息-> 入参：sid|出参:登录人账号、所在部门、头像地址、角色（先按照部门管理者为干部的逻辑来，否则为普通员工）、状态（0：成功|1：失败）、信息提示（失败时携带）
	 * @author WU LiHua
	 * @date 2020年2月4日 上午10:28:42
	 */
	@Mapping("jch5.kq_getShLoginUserInfo")
	public String getLoginUserInfo(UserContext uc,String company) {
    	JSONObject returnData = new JSONObject();
		try {
			String userId = uc.getUID();//登录人账号
			String userName = uc.getUserName();//登录人姓名
			String departmentName = uc.getDepartmentModel().getName();//所在部门名称
			boolean manager = uc.getUserModel().isManager();//是否是管理者，true
			String userPhoto = SDK.getPortalAPI().getUserPhoto(uc, userId);//获取用户头像地址
			List<UserMapModel> userMaps = SDK.getORGAPI().getUserMaps(userId);//查询兼任
			JSONArray deptArrInfo = new JSONArray();
			if(userMaps != null && userMaps.size() > 0) {//如果查到数据
				for (int i = 0; i < userMaps.size(); i++) {
					JSONObject returnDepartment = new JSONObject();
					String roleId = userMaps.get(i).getRoleId();
		            String roleName = CoreUtil.objToStr(DBSql.getString("SELECT ROLENAME FROM ORGROLE WHERE ID = '"+roleId+"'", "ROLENAME"));
					returnDepartment.put("roleId", roleId);
					returnDepartment.put("roleName", roleName);
					deptArrInfo.add(returnDepartment);
				}
			}
			//添加本岗   tlang add 20200617 
			JSONObject selfRole = new JSONObject();
            selfRole.put("roleId", uc.getRoleModel().getId());
            selfRole.put("roleName", uc.getRoleModel().getName());
			deptArrInfo.add(selfRole);
	    	JSONArray queryNavListArr = null;
	    	String parentId = SDK.getAppAPI().getProperty("com.awspaas.user.apps.shhtaerospaceindustrial", "parentId");
			queryNavListArr = SDK.getPortalAPI().queryNavList(uc, parentId);
			SSOUtil ssoUtil = new SSOUtil();
			//根据文件创建者创建sid，formFile.getCreateUser()为userId
			String sid = ssoUtil.registerClientSessionNoPassword(userId, "cn", "", "pc");
			JSONArray menuArrInfo = new JSONArray();
			for(int i=0;i<queryNavListArr.size();i++) {
				JSONObject returnMenuData = new JSONObject();
				String menuName = queryNavListArr.getJSONObject(i).get("name").toString();
				String menuUrl = queryNavListArr.getJSONObject(i).get("url").toString().replace("sid=null", "sid="+sid);
				String menuIco = queryNavListArr.getJSONObject(i).get("icon96").toString();
				String menuDesc = queryNavListArr.getJSONObject(i).get("desc").toString();
				String menuId = queryNavListArr.getJSONObject(i).get("id").toString();
				String orderIndex = DBSql.getString("SELECT ORDERINDEX FROM SYS_NAV_FUNCTION WHERE ID = '"+menuId+"'", "ORDERINDEX");
				returnMenuData.put("menuName", menuName);
				returnMenuData.put("menuUrl", menuUrl);
				returnMenuData.put("menuIco96", menuIco);
				returnMenuData.put("menuDesc", menuDesc);
				returnMenuData.put("orderIndex",orderIndex);
				menuArrInfo.add(returnMenuData);
			}
			returnData.put("status", "0");
			returnData.put("userId", userId);
			returnData.put("userName", userName);
			returnData.put("departmentName", departmentName);
			returnData.put("userPhoto", userPhoto);
			returnData.put("manager", manager);
			returnData.put("roleMaps", deptArrInfo);
			returnData.put("menuArrInfo", menuArrInfo);
			returnData.put("company", "公司信息待补充");
			//20200319 tlang add  添加人员扩展信息
			String uniqueId = uc.getUniqueId();
            String queryUserExtInfoSql = "select BCLX from BO_ACT_ORG_EXT_USER where userid = '"+uniqueId+"'";
            List<Map<String, Object>> userExtInfo = DBSql.query(queryUserExtInfoSql, new ColumnMapRowMapper());
			if(userExtInfo!=null && userExtInfo.size()>0){
			    //班次类型字段
			    String bclx = CoreUtil.objToStr(userExtInfo.get(0).get("BCLX"));
			    returnData.put("bclx",bclx);//0:常规|1:翻班|2:老炼
			}
            
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
}
