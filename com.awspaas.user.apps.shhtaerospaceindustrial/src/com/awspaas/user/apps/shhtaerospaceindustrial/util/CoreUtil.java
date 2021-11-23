/**
 * @Description 常量类
 * @author WU LiHua
 * @date 2020年2月4日 下午2:49:11
 */
package com.awspaas.user.apps.shhtaerospaceindustrial.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class CoreUtil {
	public static String YGJBB = "BO_EU_YGJBB"; //员工加班表
	public static String YGQJB = "BO_EU_YGQJB";//员工请假表
	public static String YGQJZB = "BO_EU_YGQJBZB";//员工请假子表
	public static String BQSQ = "BO_EU_BQSQ";//员工手动签卡表
	public static String KQZQXXB = "BO_EU_KQZQXXB";//考勤周期信息表
	public static String KQRQXXB = "BO_EU_KQRQXXB";//考勤日期信息表
	public static String TXJLB = "BO_EU_TXJLB";//调休记录主表
	public static String TXJLZB = "BO_EU_TXLJZB";//调休记录子表
	public static String GXFFJLB = "BO_EU_GXFFJLB";//公休发放记录表
	public static String GXSYJLZB = "BO_EU_GXSYJLZB";//公休使用记录子表
	public static String XBDKB = "BO_EU_XBDK";//下班打卡
	public static String SBDKB = "BO_EU_KQ_SBDK";//上班打卡
	public static String DKJLB = "VIEW_EU_DKJLB";//打卡记录汇总视图
	public static String JBSZB = "BO_EU_JBSZB";//加班设置表
	public static String YXDKJLB = "BO_EU_YXDKJLB";//有效打卡记录表
	public static String ASSIGMIS = "BO_EU_SH_VEHICLEORDER_ASSIGMIS";//上航_车辆任务分配
	public static String MISSION = "BO_EU_SH_VEHICLEORDER_MISSION";//上航_车辆_行车任务单
	public static String BO_EU_YBOFFICEUSECAR_DS = "BO_EU_YBOFFICEUSECAR_DS";//保障用车任务分配单
	/** 审批状态 **/
	public static String SPZT_WSP = "0";//未审批
	public static String SPZT_TG = "1";//通过
	public static String SPZT_WTG = "2";//未通过
	/** 流程定义Id **/
	public static String jbProcessDefId = "obj_967d0006afb14afc95089f00f3d089d5";//加班申请流程定义Id
	public static String qjProcessDefId = "obj_f1fc2b8c1d8349febcb5c9a7c101141c";//请假申请流程定义Id
	public static String bqProcessDefId = "obj_3696160ee06b4d3198bf936675a68573";//手动签卡申请流程定义Id

	/** 角色id **/
	public static String ZLCGBROLEID = "2d644ee9-6167-49dd-be48-28734452a878";//助理层干部
	public static String FSJLDROLEID = "e340c470-ad21-4c5d-ba25-4140fe26c490";//副所级领导
	public static String SZROLEID = "4b72d6e3-9cdc-42a3-a955-ae84725429c8";//所长
	public static String BMFZRROLEID = "469d90fc-08b4-48fb-a36b-0b15d43a41ab";//部门负责人
	public static String DZBSJROLEID = "469d90fc-08b4-48fb-a36b-0b15d43a41ab";//党支部书记
	public static String DWSJROLEID = "228fbb38-16f8-4322-a1ad-5a49b7aa2fcd";//党委书记
	public static String QTZCGBROLEID = "c324ca6c-90c4-4233-b285-58c630e3ab0a";//其它中层干部

	public static String objToStr(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	public static int objToInt(Object obj) {
		return obj == null ? 0 : Integer.parseInt(objToStr(obj));
	}
	/**
	 * 
	 * @Title: getTheFirstThreeDay 
	 * @Description: 取当前时间前forwardTime天的日期
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String[] getTheFirstThreeDay(int forwardTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar beforeTime = Calendar.getInstance();
		//		beforeTime.add(Calendar.DATE, -3);// 3天之前的时间
		//		Date beforeD = beforeTime.getTime();
		String[] time = new String[1];
		beforeTime.add(Calendar.DATE, -forwardTime);
		//		time[0] = sdf.format(beforeTime.getTime());
		//		beforeTime.add(Calendar.DATE, 1);
		//		time[1] = sdf.format(beforeTime.getTime());
		//		beforeTime.add(Calendar.DATE, 1);
		//		time[2] = sdf.format(beforeTime.getTime());
		//		String[] time = sdf.format(beforeD);
		return time; 
	}
	/**
	 * 
	 * @Title: getLastTimeInterval 
	 * @Description: 根据当前日期获得上周的日期区间（上周周一和周日日期）
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getLastTimeInterval() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar1 = Calendar.getInstance();  
		int dayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK) - 1;  
		int offset1 = 1 - dayOfWeek;  
		calendar1.add(Calendar.DATE, offset1 - 7);  
		String lastBeginDate = sdf.format(calendar1.getTime());  
		return lastBeginDate;  
	}
	/**
	 * 获取两个日期之间的所有日期
	 * 
	 * @param startTime
	 *            开始日期
	 * @param endTime
	 *            结束日期
	 * @return
	 */
	public static String[] getDays(String startTime, String endTime) {
		// 返回的日期集合
		List<String> days = new ArrayList<String>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start = dateFormat.parse(startTime);
			Date end = dateFormat.parse(endTime);

			Calendar tempStart = Calendar.getInstance();
			tempStart.setTime(start);

			Calendar tempEnd = Calendar.getInstance();
			tempEnd.setTime(end);
			tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
			while (tempStart.before(tempEnd)) {
				days.add(dateFormat.format(tempStart.getTime()));
				tempStart.add(Calendar.DAY_OF_YEAR, 1);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String[] str = new String[days.size()];
		for (int i = 0; i < days.size(); i++) {
			str[i] = days.get(i);
		}
		return str;
	}
	/**
	 * 
	 * @Title: getPreDate 
	 * @Description: 获取当前日期的前一天日期
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getPreDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1); //得到前一天
		Date date = calendar.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	/**
	 * 获取指定时间的后一天
	 * @Description 
	 * @author WU LiHua
	 * @date 2020年3月9日 下午2:53:03
	 * @param dateSub
	 * @return
	 */
	public static String getAfterDay(String dateSub){
		Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd");   //日期格式化
	    Date date = null;
	    try {
	     date = sdfm.parse(dateSub);
	    } catch (ParseException e) {
	     e.printStackTrace();
	    }
	    calendar.setTime(date);
	    calendar.setTime(date);
	    calendar.add(calendar.DATE, +1);
        return sdfm.format(calendar.getTime());
	}
	
	/**
	 * 获取指定时间的前一天
	 * @Description 
	 * @author WU LiHua
	 * @date 2020年3月9日 下午2:53:03
	 * @param dateSub
	 * @return
	 */
	public static String getPreDay(String dateSub){
		Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd");   //日期格式化
	    Date date = null;
	    try {
	     date = sdfm.parse(dateSub);
	    } catch (ParseException e) {
	     e.printStackTrace();
	    }
	    calendar.setTime(date);
	    calendar.setTime(date);
	    calendar.add(calendar.DATE, -1);
        return sdfm.format(calendar.getTime());
	}

	/**
	 * 
	 * @Title: getPreDate 
	 * @Description: 获取当前日期的后一天日期
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getAfterDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1); //得到前一天
		Date date = calendar.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}
	/**
	 * 
	 * @Title: getPreDate 
	 * @Description: 判断通宵加班单是否发生在当前日期之后
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static boolean judeTxJbRq(String date,String userId) {
		String sql = "select max( to_char(JSSJ,'yyyy-MM-dd')) JSSJMAX from BO_EU_YGJBB where to_char(KSSJ,'yyyy-MM-dd') = '"+date+"' and to_char(JSSJ,'yyyy-MM-dd') = '"+CoreUtil.getAfterDay(date)+"' "
				+ "and ygzh = '"+userId+"'";
		String jssjMax = DBSql.getString(sql, "JSSJMAX");
		if(jssjMax == null || jssjMax.equals("")) {
			return true;
		}
		int timeCompare = timeCompare(jssjMax,CoreUtil.getCurrentDate());//比当前日期小返回-1 	相等返回 0  比当前日期大 返回1  CoreUtil.getCurrentDate()
		if(timeCompare == -1) {//比当前日期小
			return true;
		}else {
			return false;
		}
	}
	/**
	 * 
	* @Title: getSbDkTime 
	* @Description: 获取上班打卡时间
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String getSbDkTime(List<Map<String, Object>> sbDkSjData) {
		if(sbDkSjData == null || sbDkSjData.size() == 0) {
			return "";
		}else {
			return  CoreUtil.objToStr(sbDkSjData.get(0).get("SJ"));
		}
	}
	/**
	 * 
	* @Title: getSbDkTime 
	* @Description: 获取上班打卡时间
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String getRealSbDkTime(List<Map<String, Object>> sbDkSjData) {
		if(sbDkSjData == null || sbDkSjData.size() == 0) {
			return "";
		}else {
			return CoreUtil.objToStr(sbDkSjData.get(0).get("ZSSJ")).substring(0,16);
		}
	}
	/**
	 * 
	* @Title: getRealXbDkTime 
	* @Description: 获取真实的下班打卡时间
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String getRealXbDkTime(String userid,String kqSj) {
		String sj = "";
		//判断是否有晚通宵加班
		String getWtxSql = "select KSSJ,TO_CHAR(JSSJ,'YYYY-MM-DD') JSRQ from BO_EU_YGJBB where to_char(KSSJ,'yyyy-MM-dd') = '"+kqSj+"' and "
				+ "(to_char(JSSJ,'yyyy-MM-dd') = '"+CoreUtil.getAfterDay(kqSj)+"' or to_char(JSSJ,'yyyy-MM-dd') = '"+kqSj+"') and ygzh = '"+userid+"' and spzt = 1";
		List<Map<String, Object>> jbData = DBSql.query(getWtxSql, new ColumnMapRowMapper(), new Object[] {});//通宵加班数据
		if(jbData.size() >= 1) {
			String jbkssj = CoreUtil.objToStr(jbData.get(0).get("KSSJ")).substring(0,19);//加班开始时间
			String jsrq = CoreUtil.objToStr(jbData.get(0).get("JSRQ"));//加班结束日期
			//判断第二天是否工作日
			String kqLxSql = "select RQLX from BO_EU_KQRQXXB where RQ = TO_DATE('"+CoreUtil.getAfterDay(kqSj)+"','yyyy-MM-dd') ";
			String lx = DBSql.getString(kqLxSql, "RQLX");//获取当前日期的类型
			if(lx.equals("0")) {//如果是工作日,则查询加班开始时间到第二天早晨8:00之间的下班打卡最早时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+CoreUtil.getAfterDay(kqSj)+" 08:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 08:00:00"+"'"
						+ " and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "";
				}else {
					sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("qdsj")).substring(0,16);
				}
			}else {//非工作日，则获取加班开始时间到第二天结束时间之间的打卡时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+CoreUtil.getAfterDay(kqSj)+" 24:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 24:00:00"+"' "
						+ "and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "";
				}else {
					sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("qdsj")).substring(0,16);
				}
			}
		}else {//如果没有加班
			String sql="select * from (select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' "
					+ "AND to_char(SJ,'yyyy-mm-dd') = '"+kqSj+"' and zh = '"+userid+"' union select  2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,"
					+ "to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') AND to_char(QDSJ,'yyyy-mm-dd') = '"+kqSj+"'  "
					+ "and BKLX = 1 and YGZH = '"+userid+"') order by qdsj desc";
			List<Map<String, Object>> getXbdkSjData = DBSql.query(sql, new ColumnMapRowMapper(), new Object[] {});
			if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
				sj = "";
			}else {
				sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("qdsj")).substring(0,16);			}
		}
		return sj;
	}
	/**
	 * 
	* @Title: getXbDkTime 
	* @Description: 获取最晚打卡时间，如果是通宵加班的，则返回24:00
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String getXbDkTime(String userid,String kqSj) {
		String sj = "";
		//判断是否有晚通宵加班
		String getWtxSql = "select KSSJ,TO_CHAR(JSSJ,'YYYY-MM-DD') JSRQ from BO_EU_YGJBB where to_char(KSSJ,'yyyy-MM-dd') = '"+kqSj+"' and "
				+ "(to_char(JSSJ,'yyyy-MM-dd') = '"+CoreUtil.getAfterDay(kqSj)+"' or to_char(JSSJ,'yyyy-MM-dd') = '"+kqSj+"') and ygzh = '"+userid+"' and spzt = 1";
		List<Map<String, Object>> jbData = DBSql.query(getWtxSql, new ColumnMapRowMapper(), new Object[] {});//通宵加班数据
		if(jbData.size() >= 1) {
			String jbkssj = CoreUtil.objToStr(jbData.get(0).get("KSSJ")).substring(0,19);//加班开始时间
			String jsrq = CoreUtil.objToStr(jbData.get(0).get("JSRQ"));//加班结束日期
			//判断第二天是否工作日
			String kqLxSql = "select RQLX from BO_EU_KQRQXXB where RQ = TO_DATE('"+CoreUtil.getAfterDay(kqSj)+"','yyyy-MM-dd') ";
			String lx = DBSql.getString(kqLxSql, "RQLX");//获取当前日期的类型
			if(lx.equals("0")) {//如果是工作日,则查询加班开始时间到第二天早晨8:00之间的下班打卡最早时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+CoreUtil.getAfterDay(kqSj)+" 08:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 08:00:00"+"'"
						+ " and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "";
				}else {
					String rq = CoreUtil.objToStr(getXbdkSjData.get(0).get("RQ"));
					int timeCompare = CoreUtil.timeCompare(rq, kqSj);//比当前日期小返回-1
					if(timeCompare > 0) {
						sj = "24:00";
					}else {
						sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
					}
//					String sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
				}
			}else {//非工作日，则获取加班开始时间到第二天结束时间之间的打卡时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+CoreUtil.getAfterDay(kqSj)+" 24:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 24:00:00"+"' "
						+ "and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "";
				}else {
					String rq = CoreUtil.objToStr(getXbdkSjData.get(0).get("RQ"));
					int timeCompare = CoreUtil.timeCompare(rq, kqSj);//比当前日期小返回-1
					if(timeCompare > 0) {
						sj = "24:00";
					}else {
						sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
					}
				}
			}
		}else {//如果没有加班
			String sql="select * from (select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' "
					+ "AND to_char(SJ,'yyyy-mm-dd') = '"+kqSj+"' and zh = '"+userid+"' union select  2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,"
					+ "to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') AND to_char(QDSJ,'yyyy-mm-dd') = '"+kqSj+"'  "
					+ "and BKLX = 1 and YGZH = '"+userid+"') order by qdsj desc";
			List<Map<String, Object>> getXbdkSjData = DBSql.query(sql, new ColumnMapRowMapper(), new Object[] {});
			if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
				sj = "";
			}else {
				sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));			}
		}
		return sj;
	}
	/**
	 * 
	* @Title: getZtxDkTime 
	* @Description: 获取早通宵时间
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String getZtxDkTime(String userid,String kqRq) {
		String sj = "";
		//判断是否有早通宵加班
		String getZtxSql = "select KSSJ,TO_CHAR(JSSJ,'YYYY-MM-DD') JSRQ from BO_EU_YGJBB where (to_char(KSSJ,'yyyy-MM-dd') = '"+CoreUtil.getPreDay(kqRq)+"' "
				+ "or to_char(KSSJ,'yyyy-MM-dd') = '"+kqRq+"') and to_char(JSSJ,'yyyy-MM-dd') = '"+kqRq+"' and ygzh = '"+userid+"'";
		List<Map<String, Object>> jbData = DBSql.query(getZtxSql, new ColumnMapRowMapper(), new Object[] {});//早通宵加班数据
		if(jbData.size() >= 1) {
			String jbkssj = CoreUtil.objToStr(jbData.get(0).get("KSSJ")).substring(0,19);//加班开始时间
			String jsrq = CoreUtil.objToStr(jbData.get(0).get("JSRQ"));//加班结束日期
			//判断第二天是否工作日
			String kqLxSql = "select RQLX from BO_EU_KQRQXXB where RQ = TO_DATE('"+kqRq+"','yyyy-MM-dd') ";
			String lx = DBSql.getString(kqLxSql, "RQLX");//获取当前日期的类型
			if(lx.equals("0")) {//如果当天是工作日，则判断加班时间到当天08:00:00的最早打卡时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 08:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj  from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 08:00:00"+"'"
						+ "and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "1";
				}else {
					String rq = CoreUtil.objToStr(getXbdkSjData.get(0).get("RQ"));
					int timeCompare = CoreUtil.timeCompare(rq, kqRq);//比当前日期小返回-1
					if(timeCompare < 0) {
						sj = "1";
					}else {
						sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
//						sj = "00:00";
					}
//					String sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
				}
			}else {//非工作日，则获取加班开始时间到第二天结束时间之间的打卡时间
				String getXbdkSjSql = "select 1 type ,ZH,TO_CHAR(SJ,'hh24:mi') SJ,to_char(SJ,'yyyy-mm-dd') RQ,SJ qdsj from BO_EU_XBDK WHERE ISEND = '1' AND "
						+ "to_char(SJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' and to_char(SJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 24:00:00"+"' and zh = '"+userid+"' "
						+ "union select 2 type ,YGZH,TO_CHAR(QDSJ,'hh24:mi') SJ,to_char(QDSJ,'yyyy-mm-dd') RQ,QDSJ qdsj from BO_EU_BQSQ  WHERE SPZT in('0','1') "
						+ "AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') >= '"+jbkssj+"' AND to_char(QDSJ,'YYYY-MM-DD HH24:MI:SS') <= '"+jsrq+" 24:00:00"+"' "
						+ "and BKLX = 1 and YGZH = '"+userid+"' order by qdsj desc";
				List<Map<String, Object>> getXbdkSjData = DBSql.query(getXbdkSjSql, new ColumnMapRowMapper(), new Object[] {});
				if(getXbdkSjData == null || getXbdkSjData.size() == 0) {
					sj = "1";
				}else {
					String rq = CoreUtil.objToStr(getXbdkSjData.get(0).get("RQ"));
					int timeCompare = CoreUtil.timeCompare(rq, kqRq);//比当前日期小返回-1
					if(timeCompare < 0) {
						sj = "1";
					}else {
						sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
//						sj = "00:00";
					}
//					String sj = CoreUtil.objToStr(getXbdkSjData.get(0).get("SJ"));
				}
			}
		}else {
			//如果没有加班
			sj = "1";
		}
		return sj;
	}
	/**
	 * 
	 * @Title: getCurrentYear 
	 * @Description: 获取当前日期
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getCurrentDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return sdf.format(date);
	}
	/**
	 * 
	* @Title: timeCompare 
	* @Description: 比较两个日期的大小,比当前日期小返回-1 	相等返回 0  比当前日期大 返回1
	* @author: OnlyWjt
	* @return int 返回类型 
	* @param t1传入日期
	* @param t2当前日期
	* @throws 
	 */
	public static int timeCompare(String t1,String t2){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		try {
			c1.setTime(formatter.parse(t1));
			c2.setTime(formatter.parse(t2));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int result=c1.compareTo(c2);
		return result;
	}
	/**
	 * 
	* @Title: timeCompare 
	* @Description: 比较两个日期时间的大小,比当前日期小返回-1 	相等返回 0  比当前日期大 返回1
	* @author: OnlyWjt
	* @return int 返回类型 
	* @param t1传入日期
	* @param t2当前日期
	* @throws 
	 */
	public static int rqTimeCompare(String t1,String t2){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		try {
			c1.setTime(formatter.parse(t1));
			c2.setTime(formatter.parse(t2));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int result=c1.compareTo(c2);
		return result;
	}
	/**
	 * 
	 * @Title: getKqzRq 
	 * @Description: 获取某个考勤周期内的所有日期
	 * @author: OnlyWjt
	 * @return String[] 返回类型 
	 * @throws 
	 */
	public static String[] getKqzRq(String kqRq) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//可以方便地修改日期格式
		try {
			Calendar beforeTime = Calendar.getInstance();
			String queryKqZqSql = "select ksrq,jsrq from BO_EU_KQZQXXB where kqzq = '"+kqRq+"'";
			List<Map<String, Object>> queryKqZq = DBSql.query(queryKqZqSql, new ColumnMapRowMapper(), new Object[] {});
			//			String ksRq = "2020-02-21";
			String ksRq = CoreUtil.objToStr(queryKqZq.get(0).get("ksrq")).substring(0,10);//开始日期
			Date ksRqDate = dateFormat.parse(ksRq);//开始日期
			String jsRq = CoreUtil.objToStr(queryKqZq.get(0).get("jsrq")).substring(0,10);//结束日期
			//			String jsRq = "2020-03-20";
			Date jsRqDate = dateFormat.parse(jsRq);//开始日期
			int days = (int) Math.ceil(jsRqDate.getTime() / 1000 / 3600 / 24 - ksRqDate.getTime() / 1000 / 3600 / 24);
			String[] str = new String[days+1];
			beforeTime.setTime(jsRqDate);
			beforeTime.add(Calendar.DATE, -days);
			for (int i = 0; i <=days; i++) {
				str[i] = dateFormat.format(beforeTime.getTime());
				beforeTime.add(Calendar.DATE, 1);
			}
			return str;
		} catch (Exception e) {//如果失败，则需要返回当前日期
			e.printStackTrace();
			String[] str = new String[1];
			str[0] = dateFormat.format(new Date());
			return str;
		}
	}
	/**
	 * 
	 * @Title: getCurrentYear 
	 * @Description: 获取当前年份
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getCurrentYear(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Date date = new Date();
		return sdf.format(date);
	}

	/**
	 * 
	 * @Title: getWeek 
	 * @Description: 获取某个日期是周几
	 * @author: OnlyWjt
	 * @return String 返回类型 
	 * @throws 
	 */
	public static String getWeek(String dates){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Date d=null;
		try {
			d=f.parse(dates);
		} catch (Exception e) {
			e.printStackTrace();
		}
		cal.setTime(d);
		int w=cal.get(Calendar.DAY_OF_WEEK)-1;
		if(w==0) w=7;
		String week = "";
		switch(w) {
		case 1: week = "一";break;
		case 2: week = "二";break;
		case 3: week = "三";break;
		case 4: week = "四";break;
		case 5: week = "五";break;
		case 6: week = "六";break;
		case 7: week = "日";break;
		}
		return week;
	}
	/**
	 * @Description 传入yyyy-MM-dd的String类型的date 2019-06-28
	 * @Date 18:13 2019/6/28
	 * @Param [m, st, ed] m=判断时间  st开始时间 ed结束日期时间 时间都是yyyy-MM-dd格式
	 * @return boolean true 表示这个日期在这二个日期之
	 **/
	public static boolean inTheTwoDate(String m ,String st,String ed){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		int startDay = 0;
		int endDay = 0;
		int mDay = 0;
		try {
			Date dateStart = format.parse(st);
			Date datEnd = format.parse(ed);
			Date mDate = format.parse(m);

			startDay = (int) (dateStart.getTime() / 1000);
			endDay = (int) (datEnd.getTime() / 1000);
			mDay = (int) (mDate.getTime() / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(startDay <= mDay && mDay <=endDay){
			return true;
		} else {
			return false;
		}
	}
	/**
	 * @Description 传入yyyy-MM-dd的String类型的date 2019-06-28
	 * @Date 18:13 2019/6/28
	 * @Param [m, st, ed] m=判断时间  st开始时间 ed结束日期时间 时间都是yyyy-MM-dd格式
	 * @return boolean true 表示这个日期在这二个日期之
	 **/
	public static boolean inTheTwoDateTime(String m ,String st,String ed){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int startDay = 0;
		int endDay = 0;
		int mDay = 0;
		try {
			Date dateStart = format.parse(st);
			Date datEnd = format.parse(ed);
			Date mDate = format.parse(m);

			startDay = (int) (dateStart.getTime() / 1000);
			endDay = (int) (datEnd.getTime() / 1000);
			mDay = (int) (mDate.getTime() / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(startDay <= mDay && mDay <=endDay){
			return true;
		} else {
			return false;
		}
	}
	
	
	
//	public static void main(String[] args) {
//		boolean dateDiff = dateDiff("2020-02-03 08:00","2020-02-03 16:30","yyyy-MM-dd HH:mm");
//		System.out.println(dateDiff);
//	}
	/**
	 * 
	* @Title: dateDiff 
	* @Description: 比较两个时间之间相差的天数、小时数、分钟数
	* @author: OnlyWjt
	* @return boolean 返回类型 
	* @throws 
	 */
	public static boolean dateDiff(String startTime, String endTime,     
            float qjSc,String format) {
		if(startTime.equals("")||endTime.equals("")) {
			return false;
		}
        // 按照传入的格式生成一个simpledateformate对象     
        SimpleDateFormat sd = new SimpleDateFormat(format);     
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数     
        long nh = 1000 * 60 * 60;// 一小时的毫秒数     
        long nm = 1000 * 60;// 一分钟的毫秒数     
        long ns = 1000;// 一秒钟的毫秒数     
        long diff;     
        long day = 0;     
        long hour = 0;     
        long min = 0;     
        long sec = 0;     
        // 获得两个时间的毫秒时间差异     
        try {     
            diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();     
            day = diff / nd;// 计算差多少天     
            hour = diff % nd / nh + day * 24;// 计算差多少小时     
            min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟     
            sec = diff % nd % nh % nm / ns;// 计算差多少秒     
            // 输出结果     
            if(day==0) {
            	if(hour+qjSc == 8) {
            		if(min>=30) {
            			return true;
            		}else {
            			return false;
            		}
            	}else if(hour+qjSc<8) {
            		return false;
            	}else {
            		return true;
            	}
            }else if(day<0) {
            	return false;
            }else {
            	return true;
            }
        } catch (ParseException e) {     
            e.printStackTrace();
            return false;
        }
    }  
	/**
	 * 
	* @Title: getDatePoor 
	* @Description: 计算两个日期相差多少个小时
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static float getDatePoor(String startTime, String endTime, String format) {
		try {
			if(startTime.equals("")||endTime.equals("")) {
				return 0f;
			}
			SimpleDateFormat sd = new SimpleDateFormat(format);
			Date nowDate = sd.parse(startTime);
			Date endDate = sd.parse(endTime);
			long nd = 1000 * 24 * 60 * 60;
			long nh = 1000 * 60 * 60;
			long nm = 1000 * 60;
			// long ns = 1000;
			// 获得两个时间的毫秒时间差异
			long diff = endDate.getTime() - nowDate.getTime();
			// 计算差多少天
			long day = diff / nd;
			// 计算差多少小时
			long hour = diff % nd / nh;
			// 计算差多少分钟
			long min = diff % nd % nh / nm;
			// 计算差多少秒//输出结果
			// long sec = diff % nd % nh % nm / ns;
			if(min>=30) {
				return (float)(day*24)+(float)hour+0.5f;
			}else {
				return (float)(day*24)+(float)hour;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	/**
	 * 
	* @Title: getDatePoor 
	* @Description: 计算两个日期相差多少个小时
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static float getMinPoor(String startTime, String endTime, String format) {
		try {
			if(startTime.equals("")||endTime.equals("")) {
				return 0f;
			}
			SimpleDateFormat sd = new SimpleDateFormat(format);
			Date nowDate = sd.parse(startTime);
			Date endDate = sd.parse(endTime);
			long nd = 1000 * 24 * 60 * 60;
			long nh = 1000 * 60 * 60;
			long nm = 1000 * 60;
			// long ns = 1000;
			// 获得两个时间的毫秒时间差异
			long diff = endDate.getTime() - nowDate.getTime();
			// 计算差多少天
			long day = diff / nd;
			// 计算差多少小时
			long hour = diff % nd / nh;
			// 计算差多少分钟
			long min = diff % nd % nh / nm;
			// 计算差多少秒//输出结果
			// long sec = diff % nd % nh % nm / ns;
			return (float)(day*24*60)+(float)(hour*60)+(float)min;
//			if(min>30) {
//				
//			}else {
//				return (float)(day*24*60)+(float)hour;
//			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	/**
	 * 
	* @Title: updateYeByUserid 
	* @Description: userId 人员id  删除加班换调休的数据，然后增加一条记录
	* @author: OnlyWjt
	* @return void 返回类型 
	* @throws 
	 */
	public static String updateYeByUserid(String userId,String jbDh,String date) {
		try {
			String getIfSql = "select NVL(count(1),0) SUM from BO_EU_TXLJZB where kysc>0 and GLTXWJJLID = '"+jbDh+"' and ZKLY = '0' and bindid in (select a.bindid from BO_EU_TXJLB a "
					+ "left join BO_EU_TXLJZB b on a.bindid = b.bindid where a.ygzh = '"+userId+"') and TXSJ = '"+date+"' and TXSC != KYSC";
			int sfXd = DBSql.getInt(getIfSql, "SUM");
			if(sfXd==0) {//能获取到，则继续执行
				String deleteSql = "delete from BO_EU_TXLJZB where kysc>0 and GLTXWJJLID = '"+jbDh+"' and ZKLY = '0' and bindid in (select a.bindid from BO_EU_TXJLB a "
						+ "left join BO_EU_TXLJZB b on a.bindid = b.bindid where a.ygzh = '"+userId+"') and TXSJ = '"+date+"'";
				DBSql.update(deleteSql);
				String getKyscSql = "select nvl(sum(kysc),0) zsc from BO_EU_TXLJZB where bindid in (select bindid from BO_EU_TXJLB  where ygzh = '"+userId+"')";
				String zsc = DBSql.getString(getKyscSql, "zsc");
				String updateSql = "update BO_EU_TXJLB set TXLJSC = '"+zsc+"' where ygzh = '"+userId+"'";
				DBSql.update(updateSql);
				return "0";
			}else {//不相等，则返回不执行
				return "1";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "1";
		}
	}
	/**
	 * 根据考勤周期返回考勤周期范围内的所有日期及类型
	 * @Description 
	 * @author WU LiHua
	 * @date 2020年3月10日 下午2:26:41
	 * @param kqzq
	 * @return
	 */
	public static JSONObject getRqlxInfoByKqzq(String kqzq) {
		JSONObject returnData = new JSONObject();
		try {
			//根据考勤周期查询改周期范围的开始日期和结束日期
			String queryKqzx = "SELECT KSRQ,JSRQ FROM "+CoreUtil.KQZQXXB+" WHERE KQZQ = '"+kqzq+"'";
			System.out.println(queryKqzx);
			String ksrq = CoreUtil.objToStr(DBSql.getString(queryKqzx, "KSRQ").substring(0, 10));
			String jsrq = CoreUtil.objToStr(DBSql.getString(queryKqzx, "JSRQ").substring(0, 10));
			//根据考勤周期的开始时间和结束时间查设置的考勤周期内的日期和类型
			String queryKqxx = "SELECT RQ,RQLX FROM BO_EU_KQRQXXB WHERE TO_DATE('"+ksrq+"','yyyy-MM-dd')<=RQ and RQ<=TO_DATE('"+jsrq+"','yyyy-MM-dd')";
			List<Map<String, Object>> queryKqxxData = DBSql.query(queryKqxx, new ColumnMapRowMapper());
			JSONArray deptArrInfo = new JSONArray();
			if(queryKqxxData != null && queryKqxxData.size() > 0) {//如果查到数据
				for(Map<String,Object> map : queryKqxxData) {//遍历考勤信息表
					JSONObject returnDepartment = new JSONObject();
					String rq = CoreUtil.objToStr(map.get("RQ")).replace("00.0", "00");//获取日期
					String rqlx = CoreUtil.objToStr(map.get("RQLX")).replace("00.0", "00");//获取日期类型
					returnDepartment.put("date", rq);
					returnDepartment.put("type", rqlx);
					deptArrInfo.add(returnDepartment);
				}
			}
			returnData.put("status", "0");
			returnData.put("data", deptArrInfo);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData;
	}
}
