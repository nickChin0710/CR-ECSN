/******************************************************************************
*                                                                             *
*                               MODIFICATION LOG                              *
*                                                                             *
*    DATE     Version    AUTHOR              DESCRIPTION                      *
*  --------   -------------------  ------------------------------------------ *
*   97/03/26  V1.00.04  Allen Ho   RECS941116-119                             *
*  107/10/29  V1.01.00  David      Transfer to JAVA                           *
*  109/07/06  V1.01.01    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V1.01.02    Zuwei     coding standard, rename field method                    *
*******************************************************************************/
package com;

import java.sql.Connection;

public class CommJobqueue extends AccessDAO {
  private final String PROGNAME = "JOB QUEUE 線上啟動批次處理副程式 V1.01.00 107/10/29";

  CommCrd comc = new CommCrd();

  private String hEjelQueueNo = "";

  private int hEjelQueueSeq = 0;
  private String hEjelProcDesc = "";
  private String hEjelErrorCode = "";
  private String hEjelErrorDesc = "";

  /* COMM_jobqueue(int type ,int error_code,char data_str */
  /* type data_str desc */
  /* ------------------------------------------------- */
  /* 0 queue_no||queue_seq job start execute */
  /* 1 PROC_DESC job end execute */
  /* 9 ERROR_DESC job unnormal break */
  /*****************************************************************************/

  public CommJobqueue(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    return;
  }

  /*****************************************************************************/
  public int commJobqueue(int tempType, String dataStr) throws Exception {
    if (tempType == 0) {
      if ((dataStr.length() != 12) || (comc.getSubString(dataStr, 0, 2).equals("JQ") == false)) {
        hEjelQueueNo = "";
        return (0);
      }
      hEjelQueueNo = String.format("%10.10s", dataStr);
      hEjelQueueSeq = comc.str2int(String.format("%2.2s", comc.getSubString(dataStr, 10)));
      hEjelProcDesc = "";
      hEjelErrorCode = "";
      hEjelErrorDesc = "";
      updateEcsJobqueueCtl1();
      commitDataBase();
      return (0);
    } else if (tempType == 1) {
      if (hEjelQueueNo.length() == 0)
        return (0);
      if (dataStr.length() != 0)
        hEjelProcDesc = dataStr;
      updateEcsJobqueueCtl2();
      commitDataBase();
    } else if (tempType == 9) {
      if (hEjelQueueNo.length() == 0)
        return (0);
      rollbackDataBase();
      hEjelErrorDesc = dataStr;
      updateEcsJobqueueCtl3();
      commitDataBase();
    }

    return (0);
  }

  /*****************************************************************************/
  private int updateEcsJobqueueCtl1() throws Exception {
    try {
      daoTable = "ecs_jobqueue_ctl";
      updateSQL = "proc_flag      = '2', ";
      updateSQL += "proc_desc      = '程式已開始執行 !', ";
      updateSQL += "execute_date_s = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "execute_time_s = to_char(sysdate,'hh24miss'), ";
      updateSQL += "proc_date      = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "proc_time      = to_char(sysdate,'hh24miss'), ";
      updateSQL += "mod_time       = sysdate, ";
      updateSQL += "mod_user       = 'MktD050' ";
      whereStr = " where queue_no       = ? ";
      whereStr += " and    queue_seq  = ?";
      setString(1, hEjelQueueNo);
      setInt(2, hEjelQueueSeq);
      updateTable();
    } catch (Exception ex) {
      showLogMessage("I", "", "[COMM_jobqueue] update ecs_jobqueue_ctl_1 error");
      commJobqueue(9, String.format("%60.60s", ex.getMessage()));
      throw ex;
    }
    if (notFound.equals("Y"))
      hEjelQueueNo = "";
    return 0;

  }

  /*************************************************************************/
  private int updateEcsJobqueueCtl2() throws Exception {
    try {
      daoTable = "ecs_jobqueue_ctl";
      updateSQL = "proc_flag      = 'Y', ";
      updateSQL += "proc_desc      = '程式正常結束 !', ";
      updateSQL += "execute_date_e = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "execute_time_e = to_char(sysdate,'hh24miss'), ";
      updateSQL += "proc_date      = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "proc_time      = to_char(sysdate,'hh24miss'), ";
      updateSQL += "error_code     = ?, ";
      updateSQL += "error_desc     = ?, ";
      updateSQL += "mod_time       = sysdate, ";
      updateSQL += "mod_user       = 'MktD050' ";
      whereStr = " where queue_no       = ? ";
      whereStr += " and    queue_seq  = ?";
      setString(1, hEjelErrorCode);
      setString(2, hEjelErrorDesc);
      setString(3, hEjelQueueNo);
      setInt(4, hEjelQueueSeq);
      updateTable();
    } catch (Exception ex) {
      showLogMessage("I", "", "[COMM_jobqueue] update ecs_jobqueue_ctl_2 error");
      commJobqueue(9, String.format("%60.60s", ex.getMessage()));
      throw ex;
    }
    if (notFound.equals("Y")) {
      showLogMessage("I", "", "[COMM_jobqueue] update ecs_jobqueue_ctl_2 error");
      commJobqueue(9, String.format("%60.60s", "update ecs_jobqueue_ctl_2 not Found"));
      throw new Exception("update ecs_jobqueue_ctl_2 not Found");
    }
    return 0;
  }

  /*************************************************************************/
  private int updateEcsJobqueueCtl3() throws Exception {
    try {
      daoTable = "ecs_jobqueue_ctl";
      updateSQL = "proc_flag      = 'E', ";
      updateSQL += "proc_desc      = '系統錯誤, 請洽資訊人員 !', ";
      updateSQL += "execute_date_e = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "execute_time_e = to_char(sysdate,'hh24miss'), ";
      updateSQL += "proc_date      = to_char(sysdate,'yyyymmdd'), ";
      updateSQL += "proc_time      = to_char(sysdate,'hh24miss'), ";
      updateSQL += "error_code     = 'S1', ";
      updateSQL += "error_desc     = ?, ";
      updateSQL += "mod_time       = sysdate, ";
      updateSQL += "mod_user       = 'MktD050' ";
      whereStr = " where queue_no       = ? ";
      whereStr += " and    queue_seq  = ?";
      setString(1, hEjelErrorDesc);
      setString(2, hEjelQueueNo);
      setInt(3, hEjelQueueSeq);
      updateTable();
    } catch (Exception ex) {
      showLogMessage("I", "", "[COMM_jobqueue] update ecs_jobqueue_ctl_3 error");
      commJobqueue(9, String.format("%60.60s", ex.getMessage()));
      throw ex;
    }
    if (notFound.equals("Y")) {
      showLogMessage("I", "", "[COMM_jobqueue] update ecs_jobqueue_ctl_3 error");
      commJobqueue(9, String.format("%60.60s", "update ecs_jobqueue_ctl_3 not Found"));
      throw new Exception("update ecs_jobqueue_ctl_3 not Found");
    }
    return 0;

  }

  /*************************************************************************/
  public double getJobSeq() throws Exception {
    double seqno = 0;

    try {
      sqlCmd = "select ecs_jobseq.nextval as seqno from dual";
      selectTable();
    } catch (Exception ex) {
      showLogMessage("I", "", "select ecs_jobseq error");
      throw ex;
    }
    if (notFound.equals("Y")) {
      showLogMessage("I", "", "select ecs_jobseq error");
      commJobqueue(9, String.format("%60.60s", "update ecs_jobqueue_ctl_3 not Found"));
      throw new Exception("GetJobSeq() not Found");
    }
    seqno = getValueDouble("seqno");
    return seqno;

  }
}
