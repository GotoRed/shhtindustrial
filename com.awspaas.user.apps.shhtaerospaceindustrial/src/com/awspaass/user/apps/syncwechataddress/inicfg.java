package com.awspaass.user.apps.syncwechataddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class inicfg {



	    private String name;
	    private String awsid;
	    private String department;
	    private String companywechatid;
	    private String companywechatseret;
	    public void setName(String name) {
	         this.name = name;
	     }
	     public String getName() {
	         return name;
	     }

	    public void setAwsid(String awsid) {
	         this.awsid = awsid;
	     }
	     public String getAwsid() {
	         return awsid;
	     }

	    public void setDepartment(String department) {
	         this.department = department;
	     }
	     public String getDepartment() {
	         return department;
	     }

	    public void setCompanywechatid(String companywechatid) {
	         this.companywechatid = companywechatid;
	     }
	     public String getCompanywechatid() {
	         return companywechatid;
	     }

	    public void setCompanywechatseret(String companywechatseret) {
	         this.companywechatseret = companywechatseret;
	     }
	     public String getCompanywechatseret() {
	         return companywechatseret;
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
}
