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

import com.actionsoft.bpms.org.model.CompanyModel;
import com.actionsoft.bpms.org.model.DepartmentModel;
import com.actionsoft.bpms.org.model.RoleModel;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.org.model.UserMapModel;
public class testApi  implements IJob {
	public void execute(JobExecutionContext jobExecutionContext)
	        throws JobExecutionException {
	        // 读管理员配置的扩展参数串，支持简单的@公式
		LogPrinter p = new LogPrinter();
		Logger log= p.getMylog();
		String corpid="ww0605aae701a55d9e";
		String corpsecret="ZwrE6zsNaSfu_OdShAm1OzvJz0CvK-AZnVho9lPOMQU";
		List<CompanyModel> Companys = SDK.getORGAPI().getCompanys();
		
		for(int i=0;i<Companys.size();i++) {
			
		    System.out.println(Companys.get(i).getId());
			System.out.println(Companys.get(i).getName());
		
	    }
		int i=1,j=1;
		while(j!=0) {
			List<DepartmentModel>departments=SDK.getORGAPI().getDepartmentsByCompanyId(i, "c8306c56-2732-45e7-a091-dbfb7185f7b6");
			j=departments.size();
			System.out.println(departments.size());
			i++;
		}
		System.out.println(i-2);
		

		//SDK.getORGAPI().createDepartment("c8306c56-2732-45e7-a091-dbfb7185f7b6", "TestAPi", null, null, "a1626e09-578c-4f1b-a802-5ba189c5ede3", null, null);
/*		List<DepartmentModel> departments=SDK.getORGAPI().getDepartmentsByCompanyId(2, "8911e732-b42a-4556-853f-ad32761bcbee");
		for(int i=0;i<departments.size();i++) {
			
		    System.out.println(departments.get(i).getId());
			System.out.println(departments.get(i).getName());
			List<UserMapModel> employees = SDK.getORGAPI().getUserMapsByDept(departments.get(i).getId());
			for(int j=0;j<employees.size();j++) {
				//System.out.println(employees.get(j));
				System.out.println("UID:"+employees.get(j).getUID());
				//SDK.getORGAPI().updateUser(corpsecret, corpsecret, corpsecret, corpsecret, corpsecret, corpsecret, corpsecret, corpsecret, corpid, corpsecret);
			}
	    }

		SDK.getORGAPI().disabledUser("testfordisable");
		SDK.getORGAPI().activateUser("testfordisable");
*/		
	}
	public void printdep(List<DepartmentModel> deps) {
		for(int i = 0;i<deps.size();i++) {
			System.out.print(deps.get(i).getName());
			System.out.println(" ");
		}
	}
}
