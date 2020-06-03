package com.xxl.job.admin.core.alarm.impl;

import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.stream.Collectors;

/**
 * job alarm by wechat message
 *
 * @author majq 2020-05-25
 */
@Component
public class WechatJobAlarm implements JobAlarm {
    private static final Logger logger = LoggerFactory.getLogger(WechatJobAlarm.class);

    /**
     * fail alarm
     *
     * @param jobLog
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        boolean alarmResult = true;

        // send monitor wechat message
        if (info != null && info.getAlarmWechat() != null && info.getAlarmWechat().trim().length() > 0) {

            // alarmContent
            String alarmContent = "Alarm Job LogId=" + jobLog.getId();
            if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += " " + jobLog.getTriggerMsg().replaceAll("<br>","\\\\n");
            }
            if (jobLog.getHandleCode() > 0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += " " + jobLog.getHandleMsg();
            }
            // message info
            XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(Integer.valueOf(info.getJobGroup()));
            String content = MessageFormat.format(loadWechatJobAlarmTemplate(),
                    group != null ? group.getTitle() : "null",
                    info.getId(),
                    info.getJobDesc(),
                    alarmContent,
                    info.getAlarmWechat());

            String reqcontent = String.format("%08d", content.getBytes().length) + content;
            logger.info(">>>>>>>>>>> xxl-job, wechat msg:" + reqcontent);
            // make wechat message
            try {
                Socket s = new Socket(XxlJobAdminConfig.getAdminConfig().getWechatHost(), XxlJobAdminConfig.getAdminConfig().getWechatPort());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                dout.write(reqcontent.getBytes());
                BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String returnMsg = din.lines().collect(Collectors.joining());
                logger.info("Return msg:" + returnMsg);
                din.close();
                dout.close();
                s.close();

            } catch (Exception e) {
                logger.error(">>>>>>>>>>> xxl-job, job fail alarm wechat send error, JobLogId:{}", jobLog.getId(), e);

                alarmResult = false;
            }
        }
        return alarmResult;
    }

    /**
     * load email job alarm template
     *
     * @return
     */
    private static final String loadWechatJobAlarmTemplate() {
        String wechatMsgBodyTemplate = "'{'\"msg\":\""
                + I18nUtil.getString("jobconf_monitor_detail") + "\\n"
                + I18nUtil.getString("jobinfo_field_jobgroup") + ": {0}\\n"
                + I18nUtil.getString("jobinfo_field_id") + ": {1}\\n"
                + I18nUtil.getString("jobinfo_field_jobdesc") + ": {2}\\n"
                + I18nUtil.getString("jobconf_monitor_alarm_title") + ": " + I18nUtil.getString("jobconf_monitor_alarm_type") + "\\n"
                + I18nUtil.getString("jobconf_monitor_alarm_content") + ": {3}"
                + "\",\"touser\":\"\",\"toMobile\":\"" + "{4}"
                + "\",\"msgType\":\"text\"'}'";

        return wechatMsgBodyTemplate;
    }

}
