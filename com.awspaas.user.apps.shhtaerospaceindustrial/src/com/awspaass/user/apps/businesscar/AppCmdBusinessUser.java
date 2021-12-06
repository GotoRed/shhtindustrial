package com.awspaass.user.apps.businesscar;


import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.SSOUtil;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.ProcessExecuteQuery;
import com.alibaba.fastjson.JSONObject;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.MnmsConstant;
import com.awspaas.user.apps.shhtaerospaceindustrial.sms.SmsUtil;
import com.awspaas.user.apps.shhtaerospaceindustrial.util.CoreUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class AppCmdBusinessUser {
	 /**
     * @param uc
     * @param processInstId 流程实例ID
     * @return
     * @Desc 根据任务分配表中的id去行车任务表中找到流程实例ID和任务实例ID和任务办理者然后查看内外租流程的单子
     */
    @Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_bzycOpenForm")
    public static String bzycOpenForm(UserContext uc, String id) {
        JSONObject result = new JSONObject();
        try {
            String processInstIdSql = "SELECT BINDID FROM BO_EU_YBBZUSECAR_MISSION WHERE RESOURCETASKFPID = '" + id + "'";
            String processInstId = CoreUtil.objToStr(DBSql.getString(processInstIdSql, "BINDID"));
            if (processInstId.equals("")) {//没有进行派单
                result.put("status", "1");
                result.put("message", "此条任务还未进行派单，请确认！");
                return result.toString();
            }
            //内租、外租流程第一节点的定义IDobj_c8f1f74f91b00001a4a53941155bca40、obj_055dc6822a5547c489578e750084c3ba
            String queryUrl = "SELECT ID,TARGET FROM WFC_TASK WHERE PROCESSINSTID = '" + processInstId + "'"
                    + "UNION SELECT ID,TARGET FROM WFH_TASK WHERE PROCESSINSTID = '" + processInstId + "' ";
            List<Map<String, Object>> urlList = DBSql.query(queryUrl, new ColumnMapRowMapper());
            if (urlList == null || urlList.isEmpty()) {
                result.put("status", "1");
                result.put("message", "此条任务还未进行派单，请确认！");
                return result.toString();
            }
            for (int i = 0; i < urlList.size(); i++) {
                Map<String, Object> urlMap = urlList.get(i);
                String taskInstId = CoreUtil.objToStr(urlMap.get("ID"));
                String target = CoreUtil.objToStr(urlMap.get("TARGET"));
                SSOUtil ssoUtil = new SSOUtil();
                //根据文件创建者创建sid，formFile.getCreateUser()为userId
                String sidPc = ssoUtil.registerClientSessionNoPassword(target, "cn", "", "pc");
                String sidMobile = ssoUtil.registerClientSessionNoPassword(target, "cn", "", "mobile");
                String portalUrl = SDK.getPortalAPI().getPortalUrl();//http://localhost:8088/portal
                String urlPc = portalUrl + "/r/w?sid=" + sidPc + "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=" + processInstId + "&openState=2&taskInstId=" + taskInstId + "&displayToolbar=true";
                String urlMobile = portalUrl + "/r/w?sid=" + sidMobile + "&cmd=CLIENT_BPM_FORM_MAIN_PAGE_OPEN&processInstId=" + processInstId + "&openState=2&taskInstId=" + taskInstId + "&displayToolbar=true";
                result.put("status", "0");
                result.put("urlPc", urlPc);
                result.put("urlMobile", urlMobile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "1");
            result.put("message", e.getMessage());
        }
        return result.toString();
    }


    /**
     * @Description 车辆派单接口启动内租或者外租流程 ->入参：任务派单子表ID和流程实例ID|出参：状态（0：成功|1：失败）、信息提示（失败时携带）
     * 创建保障用车任务流程及数据，并启动，扭转到下个节点
     * @ids 任务派单子表记录数据信息集合
     * @processInstId 流程实例ID
     * @author DingYi
     * @date 2021年11月24日 下午20:49:42
     */
    @Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_startBzycMission")
    public String bzycpdApply(String ids, String processInstId, String id, UserContext uc) {
        JSONObject returnData = new JSONObject();
        System.out.println("front args:" + ids);
        try {
            String userId = uc.getUID();
            String queryCount = "SELECT COUNT(1) SL FROM BO_EU_YBOFFICEUSECAR_DS WHERE BINDID = '" + processInstId + "' AND ID IN (" + id + ") AND ZT <> '0'";
            int sl = CoreUtil.objToInt(DBSql.getInt(queryCount, "SL"));
            if (sl > 0) {
                returnData.put("status", "1");
                returnData.put("message", "您勾选的数据中存在重复派单！");
                return returnData.toString();
            }
            if (!id.equals("")) {//如果ID已经派单了，不能重复派单
                String[] splitIds = id.split(",");
                for (int i = 0; i < splitIds.length; i++) {
                    String splitId = splitIds[i];
                    String queryId = "SELECT COUNT(1) SL FROM BO_EU_YBBZUSECAR_MISSION WHERE RESOURCETASKFPID = " + splitId + "";
                    int idSl = CoreUtil.objToInt(DBSql.getInt(queryId, "SL"));
                    if (idSl > 0) {
                        returnData.put("status", "1");
                        returnData.put("message", "您勾选的数据中存在重复派单！");
                        return returnData.toString();
                    }
                }
            }
            String queryUseCarInfo = "SELECT * FROM BO_EU_YBOFFICEUSECAR WHERE BINDID = '" + processInstId + "'";
            List<Map<String, Object>> queryUserCarInfoList = DBSql.query(queryUseCarInfo, new ColumnMapRowMapper());
            String contactPerson = "";
            String applyUserName = "";
            String applyUserId = "";
            String applyUserPhone = "";
            String contactUserName = "";
            String contactUnit = "";
            String contactUnitId = "";
            String contactDept = "";
            String contactDeptId = "";
            String contactPhone = "";
            String contactUserId = "";


            String jlId = "";
            String scdd = "";
            String mdd = "";
            String sfcs = "";
            String bdate = "";
            SSOUtil ssoUtil = new SSOUtil();
            if (queryUserCarInfoList != null && !queryUserCarInfoList.isEmpty()) {//如果车辆预定表中没有数据
                Map<String, Object> userCarInfoMap = queryUserCarInfoList.get(0);
                contactPerson = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPERSON"));//用车联系人
                applyUserName = CoreUtil.objToStr(userCarInfoMap.get("APPLYUSERNAME"));//预定人
                applyUserId = CoreUtil.objToStr(userCarInfoMap.get("APPLYUID"));//预定人账号
                applyUserPhone = CoreUtil.objToStr(userCarInfoMap.get("APPLYUSERMOBILE"));//预定人手机
                contactUnit = CoreUtil.objToStr(userCarInfoMap.get("APPLYUNIT"));//预定单位
                contactDept = CoreUtil.objToStr(userCarInfoMap.get("APPLYDEPTNAME"));//预定部门
                contactUserName = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPERSON"));//用车联系人
                contactPhone = CoreUtil.objToStr(userCarInfoMap.get("CONTACTPHONE"));//用车联系人手机
                jlId = CoreUtil.objToStr(userCarInfoMap.get("ID"));//车辆预定表ID
                scdd = CoreUtil.objToStr(userCarInfoMap.get("RUNLINE"));//上车地点
                mdd = CoreUtil.objToStr(userCarInfoMap.get("RUNLINEEND"));//目的地
                sfcs = CoreUtil.objToStr(userCarInfoMap.get("ISOUTSHANGHAI"));//是否出省
                bdate = CoreUtil.objToStr(userCarInfoMap.get("BDATE"));//日期
            }
            String carType = "";
            String udate = "";
            String vehicleType = "";
            String cph = "";
            String sjxm = "";
            String sjzh = "";
            String sjlxfs = "";
            String sjxmNew = "";
            String sjzhNew = "";
            String sjlxfsNew = "";
            String idNew = "";
            String clppgg = "";
            String drivernameout = "";
            String driverphoneout = "";
            String carnoout = "";
            String udate_n = "";
            if (!ids.equals("")) {
                String[] idsArr = ids.split(",");
                for (int i = 0; i < idsArr.length; i++) {
                    String idsStr = idsArr[i];
                    String[] idsStrI = idsStr.split(":");
                    int length = idsStrI.length;//传来的字段长度
                    if (length >= 1) {
                        idNew = idsStrI[0];//任务派单子表ID
                    }
                    if (length >= 2) {
                        udate = idsStrI[1];//使用日期
                    }
                    if (length >= 3) {
                        vehicleType = idsStrI[3];//车辆类型
                    }
                    if (length >= 4) {
                        cph = idsStrI[4];//车牌号
                    }
                    if (length >= 5) {
                        sjxm = idsStrI[5];//司机姓名
                    }
                    if (length >= 6) {
                        sjzh = idsStrI[6];//司机账号
                    }
                    if (length >= 7) {
                        sjlxfs = idsStrI[7];//司机联系方式
                    }

                    Date bdate_f = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(bdate);
                    Date udate_f = new SimpleDateFormat("yyyy-MM-dd").parse(udate);
                    if (bdate_f.getDate() < udate_f.getDate()) {
                        udate_n = udate + " 8:00";
                    } else {
                        udate_n = bdate;
                    }

                    System.out.println("#保障用车任务派单" + "#");
                    String processDefId = "";//流程定义ID
                    String title = "";//流程标题


                    processDefId = "obj_7f2b76f2e0544a0fa0b9891a4061abed";
                    title = "保障用车任务";

                    if (sjzh.equals("")) {//如果司机为空
                        returnData.put("status", "1");
                        returnData.put("message", "未正确发起派车流程，请确认司机姓名是否选择！");
                        return returnData.toString();
                    }
                    if (cph.equals("")) {//如果车牌号为空
                        returnData.put("status", "1");
                        returnData.put("message", "未正确发起派车流程，请确认车牌号是否选择！");
                        return returnData.toString();
                    }


                    ProcessInstance createProcessInstance;
                    createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, sjzh, title + "-" + udate_n + "-" + contactPerson);

                    //1、创建流程实例
                    //ProcessInstance createProcessInstance = SDK.getProcessAPI().createProcessInstance(processDefId, "A755C03FB0B1471CE053F401A8C0C17FmB0", title+"-"+udate_n+"-"+contactPerson);
                    //2、为创建的流程实例写入Bo数据
                    BO boRecordData = new BO();
                    boRecordData.set("ORDERID", SDK.getRuleAPI().executeAtScript("@replace(@date,-)") + SDK.getRuleAPI().executeAtScript("@sequence('" + "AA@year@month',4,0)"));//订单号
                    boRecordData.set("APPLYUSERNAME", applyUserName);//预定人
                    boRecordData.set("APPLYUID", applyUserId);//预定人账号
                    boRecordData.set("APPLYUSERMOBILE", applyUserPhone);//预定人手机
                    boRecordData.set("APPLYUNIT", contactUnit);//用车单位
                    boRecordData.set("APPLYUNITID", contactUnitId);//用车单位ID
                    boRecordData.set("CONTACTPERSON", contactPerson);//用车联系人
//					boRecordData.set("CONTACTPHONE", contactPhone);//用车联系人手机
                    boRecordData.set("UDATE", udate_n);//使用日期
                    boRecordData.set("VEHICLETYPE", vehicleType);//车辆类型

                    boRecordData.set("SJXM", sjxm);//司机姓名
                    boRecordData.set("SJZH", sjzh);//司机账号
                    boRecordData.set("SJLXFS", sjlxfs);//司机联系方式
                    boRecordData.set("CPH", cph);//车牌号
                    //根据文件创建者创建sid，formFile.getCreateUser()为userId
                    //sid = ssoUtil.registerClientSessionNoPassword(sjzh, "cn", "", "mobile");

                    boRecordData.set("WZUNITPSN", sjxmNew);//外租公司调度
                    boRecordData.set("WZUNITPSNID", sjzhNew);//外租公司调度ID
                    boRecordData.set("WZUNITPSNPHONE", sjlxfsNew);//外租公司联系方式
                    boRecordData.set("RESOURCETASKID", jlId);//来源预订单ID
                    boRecordData.set("RESOURCETASKFPID", idNew);//来源任务分配单ID
                    boRecordData.set("ISINTERNALVEHICLE", carType);//车辆属性
                    boRecordData.set("APPLYDEPTNAME", contactDept);//用车部门
                    boRecordData.set("APPLYDEPTID", contactDeptId);//用车部门ID
                    boRecordData.set("VEHICLELABELNAME", clppgg);//车辆品牌
                    boRecordData.set("BOARDINGPLACE", scdd);//上车地点
                    boRecordData.set("TARGETPLACE", mdd);//目的地
                    boRecordData.set("ISOUTSHANGHAI", sfcs);//是否出省
                    boRecordData.set("MISSIONSTATUS", 1);//任务状态已经派单
                    int MissisonID = SDK.getBOAPI().create(CoreUtil.BZYC_MISSION, boRecordData, createProcessInstance, UserContext.fromUID(userId));
                    //3、启动创建的流程
                    ProcessExecuteQuery pquery = SDK.getProcessAPI().start(createProcessInstance);


                    int updateflag = DBSql.update("UPDATE BO_EU_YBOFFICEUSECAR_DS SET ZT = '1',MISSIONSTATUS = '1',MISSIONBINDID='" + createProcessInstance.getId() + "'  WHERE ID = '" + idNew + "'");

                    String message = "";//发送给司机

                    String param_to_user = "";
                    String param_to_driver = "";
                    String message_to_user = "";
                    String message_to_driver = "";

                    String driver_name = "";
                    String driver_phone = "";
                    String car_use = "";

                    SmsUtil sms = new SmsUtil();
                    String templateId_user = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID, MnmsConstant.PARAM_VEHICLE_DISPATCH_SUCESS_TEMPLATE_ID);
                    String templateId_driver = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID, MnmsConstant.PARAM_VEHICLE_DISPATCH_ToDriver_TEMPLATE_ID);


                    driver_name = sjxm;
                    driver_phone = sjlxfs;
                    car_use = cph;
                    param_to_user = "{'APPLYUSERNAME':'" + applyUserName + "','UDATE':'" + udate_n + "','SJXM':'" + sjxm + "','SJLXFS':'" + sjlxfs
                            + "','CPH':'" + cph + "','VEHICLETYPE':'" + SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType) + "'}";
                    param_to_driver = "{'SJXM':'" + sjxm + "','APPLYUSERNAME':'" + applyUserName + "','APPLYUSERCELLPHONE':'" + applyUserPhone + "','UDATE':'" + udate_n
                            + "','CPH':'" + cph + "','BOARDINGPLACE':'" + scdd + "','TARGETPLACE':'" + mdd + "'}";

                    message_to_user = applyUserName + "您好！您已成功预定" + udate_n + "的车辆，为您服务的司机为" + sjxm + "联系方式：" + sjlxfs + "车辆为" + SDK.getDictAPI().getValue("com.actionsoft.apps.dict", "shcartype", vehicleType) + "（" + cph + "）";//发送给用户
                    message_to_driver = udate_n + "日的用车人是[" + contactPerson + contactPhone + "]的任务已到达，车牌号为：" + cph + "],请确认！";

                    /**
                     * 给订车人、用车人、派遣司机发送短信
                     */
                    if (!applyUserPhone.equals("")) {
                        try {
                            returnData = SmsUtil.sendSms(applyUserPhone, templateId_user, param_to_user);
                            System.out.println("车辆预定成功后给用户发送短信消息=======" + returnData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!contactPhone.equals("")) {
                        try {
                            returnData = SmsUtil.sendSms(contactPhone, templateId_user, param_to_user);
                            System.out.println("车辆预定成功后给用车联系人发送短信消息=======" + returnData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!driver_phone.equals("")) {
                        try {
                            returnData = SmsUtil.sendSms(sjlxfs, templateId_driver, param_to_driver);
                            System.out.println("车辆预定成功后给司机发送短信消息=======" + returnData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }



                }
                returnData.put("status", "0");
            } else {
                returnData.put("status", "1");
                returnData.put("message", "未正确发起派车流程，请联系管理员！");
                return returnData.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnData.put("status", "1");
            returnData.put("message", e.getMessage());
        }
        return returnData.toString();
    }

    /**
     * @param ids
     * @param processInstId
     * @param id
     * @param uc
     * @return
     * @Description //用车取消派单
     */
    @Mapping("com.awspaas.user.apps.shhtaerospaceindustrial_bzyccancelMission")
    public String bzyccancelMission(String ids, String processInstId, String id, UserContext uc) {
        //System.out.println("Enter cancelMission!");
//        System.out.println(id);
        JSONObject returnData = new JSONObject();
        try {
            String userid = uc.getUID();
            String isyjqxsql = "select count(1) sl from BO_EU_YBOFFICEUSECAR_DS where id in "
                    + "(" + id + ") and zt ='2'";
            int isyjqx = DBSql.getInt(isyjqxsql, "sl");
            if (isyjqx > 0) {//已经取消的订单不允许再次取消
                returnData.put("status", "1");
                returnData.put("message", "该订单已经取消");
            } else {
				/*String isqxsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
	    				+ "("+id+") and missionstatus>2 and zt='1' and to_char(UDATE,'yyyy-mm-dd') <= (select to_char(sysdate,'yyyy-mm-dd') from dual)";*/

                String isqxsql = "select count(1) sl from BO_EU_YBOFFICEUSECAR_DS where id in "
                        + "(" + id + ") and missionstatus>2 and zt='1' ";
                int isqx = DBSql.getInt(isqxsql, "sl");
                //System.out.println(isqxsql);
                if (isqx > 0) {//不给取消派单
                    returnData.put("status", "1");
                    returnData.put("message", "已经提交确认，不允许取消！");
                } else {//允许取消派单
//					//未派单
//					String ispdsql = "select count(1) sl from BO_EU_SH_VEHICLEORDER_ASSIGMIS where id in "
//		    				+ "("+id+") and zt = '0' ";
                    //结束子流程
					/*String idsql = "select a.SJLXFS,a.bindid,a.SJZH,a.APPLYUSERNAME,a.APPLYUID,a.APPLYUSERCELLPHONE,a.UDATE,a.CPH,a.VEHICLETYPE,a.CONTACTPERSON,a.CONTACTPHONE from BO_EU_SH_VEHICLEORDER_MISSION A right JOIN "
							+ "(select id from BO_EU_SH_VEHICLEORDER_ASSIGMIS  where zt = '1' and to_char(UDATE,'yyyy-mm-dd') > "
							+ "(select to_char(sysdate,'yyyy-mm-dd') from dual) and id in ("+id+")) B ON A.RESOURCETASKFPID = B.ID";*/
                    String idsql = "select a.* from BO_EU_YBBZUSECAR_MISSION A right JOIN "
                            + "(select id from BO_EU_YBOFFICEUSECAR_DS  where zt = '1' and id in (" + id + ")) B ON A.RESOURCETASKFPID = B.ID";

                    System.out.println(idsql);

                    List<Map<String, Object>> idList = DBSql.query(idsql, new ColumnMapRowMapper());
                    System.out.println("准备取消的派单任务列表数目:" + idList.size());
                    if (idList != null && !idList.isEmpty()) {
                        for (Map<String, Object> idmap : idList) {
                            String proid = CoreUtil.objToStr(idmap.get("bindid"));
                            String sjzh = CoreUtil.objToStr(idmap.get("SJZH"));
                            String applyUserName = CoreUtil.objToStr(idmap.get("APPLYUSERNAME"));
                            String applyUid = CoreUtil.objToStr(idmap.get("APPLYUID"));
                            String applyUserCellPhone = CoreUtil.objToStr(idmap.get("APPLYUSERMOBILE"));
                            String udate = CoreUtil.objToStr(idmap.get("UDATE"));
                            String driverphone = CoreUtil.objToStr(idmap.get("SJLXFS"));
                            String drivername = CoreUtil.objToStr(idmap.get("SJXM"));
                            if (!udate.equals("")) {
                                udate = udate.substring(0, 10);
                            }
                            String cph = CoreUtil.objToStr(idmap.get("CPH"));
                            String vehicletype = CoreUtil.objToStr(idmap.get("VEHICLETYPE"));
                            String contactPerson = CoreUtil.objToStr(idmap.get("CONTACTPERSON"));//用车联系人
                            String contactPhone = CoreUtil.objToStr(idmap.get("CONTACTPHONE"));//用车联系人手机
                            String msg = "您于【" + udate + "】日，车牌号为" + cph + "的" + vehicletype + "出行已经取消";
                            String content = applyUserName + "您好！由于您" + udate + "日的用车需求不能及时满足，您的预定暂不成功，给您带来不便深表歉意。";
                            //MsgNoticeController.sendNoticeMsg(uc, msg, userid, sjzh, "1", "");
                            //MsgNoticeController.sendNoticeMsg(uc, content, userid, applyUid, "1", "");
                            SmsUtil sms = new SmsUtil();
                            System.out.println("预定人电话：" + applyUserCellPhone);
                            try {
                                System.out.println("准备终止流程！流程号:" + proid + "用户ID:" + userid);
                                SDK.getProcessAPI().terminateById(proid, userid);

                                String xgztsql = "update BO_EU_YBOFFICEUSECAR_DS set ZT='2',MISSIONSTATUS='6' where " +
                                        "id " +
                                        "in (" + id + ")";
                                System.out.println(xgztsql);
                                DBSql.update(xgztsql);//修改保障用车-车辆任务分配状态
//                                String ztsql = "update BO_EU_YBOFFICEUSECAR_DS set MISSIONSTATUS='6' where id in (" + id + ")";
//                                System.out.println(ztsql);
//                                DBSql.update(ztsql);//修改保障用车-任务分配任务单状态
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!applyUserCellPhone.equals("")) {

                                String phone = applyUserCellPhone;
                                String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID, MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
                                //String param = "{'APPLYUSERNAME':'"+applyUserName+"'}";
                                String message_user = "{'applyUserName':'" + applyUserName + "','udate':'" + udate + "','cph':'" + cph + "'}";
                                String message_driver = "{'applyUserName':'" + drivername + "','udate':'" + udate + "','cph':'" + cph + "'}";
                                try {

                                    returnData = SmsUtil.sendSms(phone, "SMS_228016523", message_user);
                                    SmsUtil.sendSms(driverphone, "SMS_228116397", message_driver);
                                    System.out.println("车辆预定取消短信通知预订人成功===========" + returnData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!contactPhone.equals("")) {
                                String phone = contactPhone;
                                String templateId = SDK.getAppAPI().getProperty(MnmsConstant.APP_ID, MnmsConstant.PARAM_VEHICLE_DISPATCH_FAIL_TEMPLATE_ID);
                                //String param = "{'APPLYUSERNAME':'"+contactPerson+"'}";
                                String message_user = "{'applyUserName':'" + contactPerson + "','udate':'" + udate + "','cph':'" + cph + "'}";
                                try {
                                    returnData = SmsUtil.sendSms(phone, "SMS_228016523", message_user);
                                    System.out.println("车辆预定取消短信通知用车人成功===========" + returnData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        }
                        returnData.put("status", "0");
                        returnData.put("message", "取消成功");
                    }
                    returnData.put("status", "0");
                    returnData.put("message", "取消成功");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnData.toString();
    }
}
