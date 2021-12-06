package com.awspaass.user.apps.syncwechataddress;
import java.io.*;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.bpms.schedule.IJob;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.*;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;
import com.awspaass.user.apps.syncwechataddress.LogPrinter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import com.actionsoft.bpms.org.model.impl.CompanyModelImpl;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.org.model.CompanyModel;
import com.actionsoft.bpms.org.model.DepartmentModel;
import com.actionsoft.bpms.org.model.RoleModel;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.org.model.UserMapModel;
import org.apache.commons.lang.*;

public class SyncAddress implements IJob  {
	
	public String compareInfo(List<DepartmentModel> departments, String dept,String name) {
		if(departments.size()==0)
			return null;
//		System.out.println("Wechat name "+name+"dept name "+dept);
		for(int i=0;i<departments.size();i++) {
			
//		    System.out.println(departments.get(i).getId());
//			System.out.println(departments.get(i).getName());
			List<UserModel> employees = SDK.getORGAPI().getAllUsersByDepartment(departments.get(i).getId());
			
			for(int j=0; j<employees.size(); j++) {
				
				String nameAws = employees.get(j).getUserName();
				String uId = employees.get(j).getUID();
//				System.out.println(employees.get(j).getRoleId());
				DepartmentModel depModel = SDK.getORGAPI().getDepartmentByUser(uId);
				String depAws = depModel.getName();
//				System.out.println(depAws+"  "+nameAws);
				if(name.equals(nameAws)&&dept.equals(depAws)) {
//					System.out.println("find "+name);
					return employees.get(j).getUID();
				}
					
			}
			
	    }
//		System.out.println("unfind "+name);
		return null;
	}
	
	public  List<UserModel> getAllUserByDepartments(List<Department> departments) {
		if(departments.size()==0)
			return null;
		List<UserModel> employees = null;
		for (int i = 0; i<departments.size(); i++) {
			List<UserModel> e = SDK.getORGAPI().getAllUsersByDepartment(departments.get(i).getDepidaws());
			if(employees == null)
				employees = e;
			else
				employees.addAll(e);
			
		}
		return employees;
	}
	
	public String readToString(String fileName) {
		String encoding = "UTF-8";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}
	
	public void createDpartments(List<Map<String,String>> depts, String CompanyId) {
		Map<String, String> deps_created = new HashMap<String,String>();
		
		for (int count=0; count<depts.size(); count++) {
			Map<String,String> depid =  depts.get(count);
			count++;
			Map<String,String> depname =  depts.get(count);
			count++;
			Map<String,String> p_id = depts.get(count);
			String newDepId;
			if(("0").equals(p_id.get("parentid"))) {
				newDepId=SDK.getORGAPI().createDepartment(CompanyId, depname.get("name"), null, null, "0", null, null);
			}else {
				newDepId=SDK.getORGAPI().createDepartment(CompanyId, depname.get("name"), null, null, deps_created.get(p_id.get("parentid")), null, null);
			}		
			deps_created.put(depid.get("id"), newDepId);
			
	

		}
	}
	

	public String getDepByName(String depName, String c_awsid) {
		String depIdAws=null;
		try {
			String sql = "select * from BO_INFO_WECHAT_AWS where COMPANYIDAWS ='" + c_awsid +"' AND NAME='"+depName+"'";
			List<Map<String,Object>> departlist = DBSql.query(sql, new ColumnMapRowMapper(), new Object[] {});
			if(departlist.isEmpty() || departlist == null ) {
				System.out.println("NO COMPANY");
				return null;
			}
			Map<String,Object> dep=departlist.get(0);
			depIdAws = CoreUtil.objToStr(dep.get("DEPIDAWS"));
		}catch (Exception e ) {
			e.printStackTrace();
			
		}
		return depIdAws;
	}
	public List<Department> getAllDeps(String companyId) {
		List<Department> depAll=new ArrayList<Department>();
		List<Map<String,Object>> departlist=null;
		
		try {
			String sql = "select * from BO_INFO_WECHAT_AWS where COMPANYIDAWS ='" + companyId +"'";
			departlist = DBSql.query(sql, new ColumnMapRowMapper(), new Object[] {});
			if(departlist.isEmpty() || departlist == null ) {
				System.out.println("NO COMPANY");
				return null;
			}
			for(int i=0; i<departlist.size();i++) {
				Map<String,Object> dep=departlist.get(i);
				String name = CoreUtil.objToStr(dep.get("NAME"));
				String depidaws = CoreUtil.objToStr(dep.get("DEPIDAWS"));
				String idwechat = CoreUtil.objToStr(dep.get("IDWECHAT"));
				String parentidwechat = CoreUtil.objToStr(dep.get("PARENTIDWECHAT"));
				String companyidaws = CoreUtil.objToStr(dep.get("COMPANYIDAWS"));
				String companyidwechat = CoreUtil.objToStr(dep.get("CONMPANYIDWECHAT"));
				String parentidaws = CoreUtil.objToStr(dep.get("PARENTIDAWS"));
				Department d = new Department();
				d.setName(name);
				d.setDepidaws(depidaws);
				d.setIdwechat(idwechat);
				d.setParentidwechat(parentidwechat);
				d.setCompanyidaws(companyidaws);
				d.setCompanyidwechat(companyidwechat);
				d.setParentidaws(parentidaws);
				depAll.add(d);
			}
			

		} catch (Exception e ) {
			e.printStackTrace();
			
		}

		return depAll;
	}
	public String getDepIdAwsByidwechat(String id_wechat,String companyid) {
		String depid = null;
		List<Map<String,Object>> departlist=null;
		try {
			String sql = "select * from BO_INFO_WECHAT_AWS where COMPANYIDAWS='"+companyid+"' and IDWECHAT = '"+id_wechat+"'";
			departlist = DBSql.query(sql, new ColumnMapRowMapper(), new Object[] {});
			if(departlist.isEmpty() || departlist == null ) {
				System.out.println("NO DEPARTMENT");
				return null;
			}
			Map<String,Object> dep=departlist.get(0);				
			depid = CoreUtil.objToStr(dep.get("DEPIDAWS"));	
		} catch (Exception e ) {
			e.printStackTrace();
			
		}
		return depid;
	}
	/*public void UpdateDepAwsNew(List<Map<String,String>> dep_wechat,List<Department>dep_aws,String companyid_aws,String companyid_wechat) {
		String depQuerySql = "select * from BO_INFO_WECHAT_AWS t where t.conmpanyidwechat =  " + companyid_wechat;
	}*/
	public void UpdateDepartments(List<Map<String,String>> dep_wechat,List<Department>dep_aws,String companyid_aws,String companyid_wechat) {
		
		/*System.out.println("微信部门数目："+dep_wechat.size()/3+" ，aws部门数目"+dep_aws.size());
		if(dep_wechat.size()/3<=dep_aws.size()) {
			return;
		}*/
		
		for(int i = 0; i < dep_wechat.size(); i++) {
			 String id_wechat = dep_wechat.get(i).get("id");
			 i++;
			 String name_wechat = dep_wechat.get(i).get("name");
			 i++;
			 String parentid_wechat = dep_wechat.get(i).get("parentid");
			 int j=0;
			 //System.out.println("微信部门ID:"+id_wechat);
			// System.out.println("微信部门名称："+name_wechat);
			 //System.out.println("parentid_wechat"+parentid_wechat);
			 String depQuerySql = "select * from BO_INFO_WECHAT_AWS t where t.conmpanyidwechat =  '" + companyid_wechat + "'and t.idwechat = '"+id_wechat + "'";
			 List<Map<String,Object>> depQueryList = DBSql.query(depQuerySql, new ColumnMapRowMapper(), new Object[] {});
			 
			 if(depQueryList == null ) {
					System.out.println("depQuerySql Error!");
					return;
				}
			 
			 if(depQueryList.size()==0) {
					String newDepId;
					String dep_p_id_aws=getDepIdAwsByidwechat(parentid_wechat,companyid_aws);
					if(("0").equals(parentid_wechat)) {
						newDepId=SDK.getORGAPI().createDepartment(companyid_aws, name_wechat, null, null, "0", null, null);
					}else {
						newDepId=SDK.getORGAPI().createDepartment(companyid_aws, name_wechat, null, null, dep_p_id_aws, null, null);
					}		
					 System.out.println("Insert"+ name_wechat);
					 String sql = "INSERT INTO BO_INFO_WECHAT_AWS  (DEPIDAWS,IDWECHAT,PARENTIDWECHAT,NAME,COMPANYIDAWS,CONMPANYIDWECHAT,PARENTIDAWS)VALUES(:DEPIDAWS,:IDWECHAT,:PARENTIDWECHAT,:NAME,:COMPANYIDAWS,:CONMPANYIDWECHAT,:PARENTIDAWS)";
					 Map<String, Object> paraMap = new HashMap<>();
					 paraMap.put("DEPIDAWS", newDepId);
					 paraMap.put("IDWECHAT", id_wechat);
					 paraMap.put("PARENTIDWECHAT", parentid_wechat);
					 paraMap.put("NAME", name_wechat);
					 paraMap.put("COMPANYIDAWS", companyid_aws);
					 paraMap.put("CONMPANYIDWECHAT", companyid_wechat);
					 paraMap.put("PARENTIDAWS", dep_p_id_aws);
					 DBSql.update(sql, paraMap);
			 } else {
				 Map<String,Object> dep=depQueryList.get(0);				
				 String dep_name_aws = CoreUtil.objToStr(dep.get("NAME"));
				 String depidAws = CoreUtil.objToStr(dep.get("DEPIDAWS"));
				 
				 if(dep_name_aws.equals(name_wechat)) {
					 continue;
				 }else {
					 //System.out.println("update BO_INFO_WECHAT_AWS t set t.name='"+ name_wechat+"' where t.companyidaws= '"+companyid_aws + "' and t.idwechat='" + id_wechat+ "'");
					 DBSql.update("update BO_INFO_WECHAT_AWS t set t.name='"+ name_wechat+"' where t.companyidaws= '"+companyid_aws + "' and t.idwechat='" + id_wechat+ "'");
					 SDK.getORGAPI().updateDepartment(depidAws,name_wechat,ORGAPI.NO_UPDATE,ORGAPI.NO_UPDATE,ORGAPI.NO_UPDATE,ORGAPI.NO_UPDATE);				 
				 }
			 }
			 /*
			 for(j =0; j<dep_aws.size();j++) {
				 if(name_wechat.equals(dep_aws.get(j).getName()))
					 break;
			 }
			 if(j>=dep_aws.size()) {
				 //System.out.println("Need to crete "+name_wechat);
				 

				 
			 }*/
		}
	}
	
	public boolean isDepRootCreated(List<Map<String,String>>wechatDep, List<DepartmentModel>awsWechat) {
		//System.out.println(wechatDep);
		String depRootWechat = wechatDep.get(1).get("name");
		for(int i = 0; i<awsWechat.size(); i++) {
			if(depRootWechat.equals(awsWechat.get(i).getName()))
				
				return true;
		}
		return false;
	}
	
	public boolean isEmployeeCreated(List<UserModel> employees, WxUserInfo e_wechat) {
		if(employees.isEmpty()|| employees.size()==0)
			return false;
		for(int i =0; i<employees.size(); i++) {
			String nameAws = employees.get(i).getUserName();
			String phoneAws = employees.get(i).getMobile();
			//if(e_wechat.getName().equals(nameAws)&&e_wechat.getMobile().equals(phoneAws))
			if(e_wechat.getMobile().equals(phoneAws))
				return true;
		}
		return false;
	}
	public String getParentdepByName(String depName, List<Map<String,String>> depts) {
		for (int count=0; count<depts.size(); count++) {
			Map<String,String> depid =  depts.get(count);
			count++;
			Map<String,String> depname =  depts.get(count);

			count++;
			Map<String,String> p_id = depts.get(count);
			
			if(depName.equals(depname.get("name"))) {
				for(int j = 0; j<depts.size();j++) {
					Map<String,String> id =  depts.get(j);
					j++;
					Map<String,String> p_name =  depts.get(j);
					j++;	
					if(id.get("id").equals(p_id.get("parentid"))) {
						return p_name.get("name");
					}
				
				}
			}
		}
		return null;
	}
	public boolean isDepIn(String name, List<DepartmentModel> deps_aws) {
		for(int j = 0; j< deps_aws.size(); j++) {
			if(name.equals(deps_aws.get(j).getName()))
				return true;
		}
		return false;
	}

	public String getdepartnamebyId(String id){
		//System.out.println(id);
		
		DepartmentModel t = SDK.getORGAPI().getDepartmentById(id);
		return t.getName();
	}
	public String getdepwechatnamebyID(String id, List<Map<String,String>>deps) {
		String name = null;
		 for(int j = 0; j<deps.size();j++) {
			 String id_wechat = deps.get(j).get("id");
			 j++;
			 String name_wechat = deps.get(j).get("name");
			 j++;
			 if(id.equals(id_wechat)) {
				 name = name_wechat;
			 }
		 }
		return name;
	}
	
	public List<Department> getAllDepAws(String c_awsid){
		List<Department> depAll=new ArrayList<Department>();
		List<Map<String,Object>> depList = null;
		
		try {
			String employeeQuery = "select * from ORGDEPARTMENT where COMPANYID= '"+c_awsid+"'";
			depList = DBSql.query(employeeQuery, new ColumnMapRowMapper(), new Object[] {});
			if(depList.isEmpty() || depList == null ) {
				return null;
				
			}
			for(int i =0;i<depList.size();i++) {
			Map<String,Object> depInfo=depList.get(i);
			String id = CoreUtil.objToStr(depInfo.get("ID"));
			String p_id = CoreUtil.objToStr(depInfo.get("PARENTDEPARTMENTID"));
			String departmentname = CoreUtil.objToStr(depInfo.get("DEPARTMENTNAME"));
			Department d = new Department();
			d.setDepidaws(id);
			d.setName(departmentname);
			d.setParentidaws(p_id);
			depAll.add(d);
			}
		} catch (Exception e ) {
			e.printStackTrace();

			
		}
		return depAll;
	}
	public void execute(JobExecutionContext jobExecutionContext)
	        throws JobExecutionException {
		
		String cfgstr = readToString("E:\\Actionsoft\\bpm808\\bpm804\\apps\\install\\com.awspaas.user.apps.shhtaerospaceindustrial\\lib\\company.json");
		
		JSONArray companyArray = null;
		JSONObject jsonObject = JSONObject.fromObject(cfgstr);
		if (jsonObject != null) {
			   //取出按钮权限的数据
			companyArray = jsonObject.getJSONArray("companys");
			  }
		 Iterator<Object> num = companyArray.iterator();
		 while(num.hasNext()) {
			 
			 JSONObject company = (JSONObject) num.next();
			 String c_wechatid = (String)company.get("companywechatid");
			 String c_secret = (String)company.get("companywechatseret");
			 String c_awsid = (String)company.get("awsid");
			 String role = (String)company.get("companyuserrole");
			 WeChatUtil wechat = new WeChatUtil();
			 System.out.println((String)company.get("name"));
			 
			 String access_token=wechat.getAccessToken(c_wechatid,c_secret);
			 List<Map<String,String>> deps =wechat.getDepartmentList(access_token);
			 List<WxUserInfo> wechatuserlist=wechat.getDepartmentUserDetails(deps, access_token, "0");
			 List<Department> deps_aws = getAllDeps(c_awsid);
			 UpdateDepartments(deps,deps_aws,c_awsid,c_wechatid);
			 //List<Department> departments = getAllDepAws(c_awsid);
			 
			 
			 
			  


			 
			 List<UserModel> employees = getAllUserByDepartments(deps_aws);
			 
			 for(int i = 0; i< wechatuserlist.size(); i++) {
				 WxUserInfo e_wechat = wechatuserlist.get(i);
				 if(isEmployeeCreated(employees, e_wechat)==false) {
					 System.out.println("####"+(String)company.get("name"));
					 System.out.println(e_wechat.getName()+e_wechat.getDepartment()+" 开始创建");
					 String depId = getDepByName(e_wechat.getDepartment(),c_awsid);
					 String account=e_wechat.getUserId()+RandomStringUtils.randomAlphanumeric(5);
					 if(account.length()>36)
						 account=account.substring(0,35);
					 //System.out.println(depId);
					 int c_re = SDK.getORGAPI().createUser(depId, account, e_wechat.getName(), role, null, null, false, null, e_wechat.getMobile(), null, null, null, null, null, null, null, null, null, null, e_wechat.getUserId(), null);
					
				 }
				 
			 }
			 
			 
		 }
		 
/*		 
		 
	        // 读管理员配置的扩展参数串，支持简单的@公式
		String Companyid="13b5de8a-96f7-4d6d-bca5-ca3695b6e390";
		WeChatUtil wechat = new WeChatUtil();
		String access_token=wechat.getAccessToken("wwd1feef53690aecf0","tBQRUDNSAIEKx_HwkqTLXjSwr_0ifr_o2vdsOc1_aU8");
		List<Map<String,String>> deps =wechat.getDepartmentList(access_token);
		List<WxUserInfo> wechatuserlist=wechat.getDepartmentUserDetails(deps, access_token, "0");
		
	
		

		List<DepartmentModel> departments=getAllDeps(Companyid);
		if (isDepRootCreated(deps,departments)==false)
			createDpartments(deps,Companyid);
		departments=getAllDeps(Companyid);
		
		
		
		for(int index=0; index<wechatuserlist.size();index++) {
			String nameWechat = wechatuserlist.get(index).getName();
			String  depWechat = wechatuserlist.get(index).getDepartment();
			String uIdWechat = wechatuserlist.get(index).getUserId();
			String phone = wechatuserlist.get(index).getMobile();
			String uIdAws =null;
			
			if(nameWechat.equals("sysadmin")==false) {
				uIdAws = compareInfo(departments,depWechat,nameWechat);
				if(uIdAws != null) {
					SDK.getORGAPI().updateUser(uIdAws, ORGAPI.NO_UPDATE, ORGAPI.NO_UPDATE, ORGAPI.NO_UPDATE,phone , ORGAPI.NO_UPDATE, ORGAPI.NO_UPDATE, ORGAPI.NO_UPDATE, ORGAPI.NO_UPDATE,uIdWechat);
				}else {

					String depId = getDepByName(depWechat,departments);

					String account=uIdWechat+RandomStringUtils.randomAlphanumeric(5);
					if(account.length()>36)
						account=account.substring(0,35);

					SDK.getORGAPI().createUser(depId, account, nameWechat, "a2466571-b615-42bb-86b4-b9c9c15d6730", null, null, false, null, phone, null, null, null, null, null, null, null, null, null, null, uIdWechat, null);
				}
			}
			
			
		}
		*/

		
	}		

}

/*
 *  for(int i =0; i<departments.size();i++) {
				  Department dep_aws = departments.get(i);
			 				 String dep_id_aws = dep_aws.getDepidaws();
			 				 String name = dep_aws.getName();
			 				 String dep_p_id_aws=dep_aws.getParentidaws();
			 				 System.out.println("检查-------"+name);
			 				 //System.out.println(dep_id_aws);
			 				 //System.out.println(dep_p_id_aws);
			 				 for(int j = 0; j<deps.size();j++) {
			 					 String id_wechat = deps.get(j).get("id");
			 					 j++;
			 					 String name_wechat = deps.get(j).get("name");
			 					 j++;
			 					 String parentid_wechat = deps.get(j).get("parentid");
			 					 System.out.println("*"+name_wechat);
			 					 
			 					 
			 					 
			 					 if(name.equals(name_wechat)) {
			 						 String p_n_aws = null;
			 						 String p_n_wechat= null;
			 						 System.out.println("微信父号"+parentid_wechat);
			 						 if(!dep_p_id_aws.equals("0")) 
			 							 p_n_aws=getdepartnamebyId(dep_p_id_aws);
			 						 else
			 							 p_n_aws="true";
			 						 if(!parentid_wechat.equals("0"))
			 							 p_n_wechat = getdepwechatnamebyID(parentid_wechat,deps);
			 						 else 
			 							 p_n_wechat="true";
			 						 if(p_n_aws.equals(p_n_wechat)) {
			 							 System.out.println("Insert"+ name);
			 							 String sql = "INSERT INTO BO_INFO_WECHAT_AWS  (DEPIDAWS,IDWECHAT,PARENTIDWECHAT,NAME,COMPANYIDAWS,CONMPANYIDWECHAT,PARENTIDAWS)VALUES(:DEPIDAWS,:IDWECHAT,:PARENTIDWECHAT,:NAME,:COMPANYIDAWS,:CONMPANYIDWECHAT,:PARENTIDAWS)";
			 							 Map<String, Object> paraMap = new HashMap<>();
			 							 paraMap.put("DEPIDAWS", dep_id_aws);
			 							 paraMap.put("IDWECHAT", id_wechat);
			 							 paraMap.put("PARENTIDWECHAT", parentid_wechat);
			 							 paraMap.put("NAME", name);
			 							 paraMap.put("COMPANYIDAWS", c_awsid);
			 							 paraMap.put("CONMPANYIDWECHAT", c_wechatid);
			 							 paraMap.put("PARENTIDAWS", dep_p_id_aws);
			 							 DBSql.update(sql, paraMap);
			 							 break;
			 						 }

			 					 }
			 				 }
			 			 }
 */
