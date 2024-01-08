/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/02/15  V1.00.03  Allen Ho   Commroutine initial                        *
* 106/02/26  V1.00.04  Allen Ho   Modify getMcode                            *
* 106/03/17  V1.00.05  Allen Ho   Modify trans database alias name           *
* 106/05/19  V1.00.06  Allen Ho   Add object_owner % TonotifyLog method      *
* 106/07/21  V1.00.07  Allen Ho   Add newCardCheck method                    *
* 107/05/15  V1.00.08  Allen Ho   Modify getMcode                            *
*  109/07/06  V1.00.09    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V1.00.10    Zuwei     coding standard, rename field method                   *
*                                                                            *
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.nio.file.Paths;

public class CommRoutine extends AccessDAO {
  CommFunction comm = new CommFunction();

  public String increaseNewDate = "";

  // ************************************************************************
  public CommRoutine(Connection conn[], String[] dbAlias) throws Exception {
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    return;
  }

  // ************************************************************************
  public int getMcode(String hAcnoAcctType, String hAcagPSeqno) throws Exception {
    return (getMcode(hAcnoAcctType, hAcagPSeqno, ""));
  }

  // ************************************************************************
  public int getMcode(String acctType, String pSeqno, String dbNameTo) throws Exception {
    selectSQL = "mix_mp_balance";
    daoTable = "ptr_actgeneral_n";
    whereStr = "WHERE acct_type = ? ";

    setString(1, acctType);

    selectTable();

    int hMixMpBalance = 0;
    if (!notFound.equals("Y"))
      hMixMpBalance = getValueInt("MIX_MP_BALANCE");

    dataBase = dbNameTo;
    extendField = "acag.";
    selectSQL = "acct_month, " + "max(stmt_cycle) as stmt_cycle, " + "sum(pay_amt) as pay_amt" + "";
    daoTable = "act_acag";
    whereStr = "WHERE p_seqno = ? " + "GROUP BY p_seqno,acct_month";
    setString(1, pSeqno);

    int cnt2 = selectTable();

    if (notFound.equals("Y"))
      return (0);

    double minAmount = 0;
    for (int i = 0; i < cnt2; i++) {
      minAmount = minAmount + getValueDouble("acag.pay_amt");
      if (minAmount > hMixMpBalance) {
        dataBase = dbNameTo;
        selectSQL = "this_acct_month ";
        daoTable = "ptr_workday";
        whereStr = "WHERE stmt_cycle = ? ";

        setString(1, getValue("acag.stmt_cycle"));

        selectTable();

        return ((int) comm.monthBetween(getValue("acag.acct_month"), getValue("this_acct_month")));
      }
    }
    return (0);
  }

  // ************************************************************************
  public String getSeqno(String seqName) throws Exception {
    return getSeqno(seqName, "");
  }

  // ************************************************************************
  public String getSeqno(String seqName, String dbNameTo) throws Exception {
    dataBase = dbNameTo;
    selectSQL = "NEXTVAL FOR " + seqName + " AS MOD_SEQNO ";
    daoTable = "SYSIBM.SYSDUMMY1";
    selectTable();

    String output = String.format("%010.0f", getValueDouble("MOD_SEQNO"));
    while (output.length() < 10)
      output = "0" + output;

    return output;
  }

  // ************************************************************************
  public int increaseDays(String oriDate, int calDays) throws Exception {
    return (increaseDays(oriDate, calDays, ""));
  }

  // ************************************************************************
  public int increaseDays(String oriDate, int calDays, String dbNameTo) throws Exception {
    setConsoleMode("Y");
    if (!comm.checkDateFormat(oriDate, "yyyyMMdd")) {
      showLogMessage("I", "", "[ncreateDays] select ecs_modseq error!");
      increaseNewDate = "";
      return (1);
    }
    int calInt = 0;
    int cntInt = 0;
    String calDate = oriDate;
    while (true) {
      cntInt++;
      if (cntInt > 1000) {
        showLogMessage("I", "", "[ncreateDays] select ecs_modseq error!");
        showLogMessage("I", "", "[ncreateDays] Maybe not right ptr_holiday");
        return (1);
      }

      if (calDays > 0) {
        calDate = comm.nextDate(calDate);
      } else {
        calDate = comm.lastDate(calDate);
      }

      dataBase = dbNameTo;
      selectSQL = "holiday";
      daoTable = "PTR_HOLIDAY";
      whereStr = "WHERE HOLIDAY  = ? ";
      setString(1, calDate);

      int recCnt = selectTable();

      if (notFound.equals("Y"))
        if (calDays > 0) {
          calDays--;
        } else {
          calDays++;
        }

      increaseNewDate = calDate;

      if (calDays == 0)
        break;
    }
    return (0);
  }

  // ************************************************************************
  public String getObjectOwner(String objType, String objCode) throws Exception {
    daoTable = "ecs_object_owner";
    whereStr = "WHERE obj_code = ? " + "AND   obj_type = ? ";

    setString(1, objCode);
    setString(2, objType);

    int recordCnt = selectTable();

    if (notFound.equals("Y"))
      return "";

    return getValue("UNIT_CODE");
  }

  // ************************************************************************
  public int newCardCheck(String pSeqno, String businessDate) throws Exception {
    extendField = "comm_newcard.";
    selectSQL = "card_no, " + "ori_issue_date, " + "oppost_Date, " + "current_code ";
    daoTable = "crd_card";
    whereStr = "where p_seqno = ? "
        + "and   card_no = major_card_no "
        + "and   card_no = end_card_no "
        + "and   issue_date < ? "
        + "order by ori_issue_date";

    setString(1, pSeqno);
    setString(2, businessDate);

    int recCnt = selectTable();

    int aliveFlag = 0;
    int newCardFlag = 0;
    String currentCode = "";
    String oppostDate = "";
    String oriIssueDate = "";

    for (int inti = 0; inti < recCnt; inti++) {
      currentCode = getValue("comm_newcard.current_code", inti);
      oppostDate = getValue("comm_newcard.oppost_date", inti);
      oriIssueDate = getValue("comm_newcard.ori_issue_date", inti);

      if (currentCode.equals("5"))
        currentCode = "2";
      if (currentCode.equals("2")) {
        if ((comm.lastMonth(businessDate, 3) + businessDate.substring(6, 8))
            .compareTo(oppostDate) > 0)
          currentCode = "0";
      }
      if (currentCode.equals("0")) {
        aliveFlag = 1;
        break;
      }
    }

    for (int inti = 0; inti < recCnt; inti++) {
      currentCode = getValue("comm_newcard.current_code");
      oppostDate = getValue("comm_newcard.oppost_date");
      oriIssueDate = getValue("comm_newcard.ori_issue_date");
      if (currentCode.equals("5"))
        currentCode = "2";
      if (currentCode.equals("2")) {
        if ((comm.lastMonth(businessDate, 3) + businessDate.substring(6, 8))
            .compareTo(oppostDate) > 0)
          currentCode = "0";
      }
      if (currentCode.equals("0")) {
        if (comm.nextNDate(businessDate, -180).compareTo(oriIssueDate) > 0) {
          newCardFlag = 1;
          break;
        }
      } else {
        if (comm.nextNDate(businessDate, 180).compareTo(oppostDate) > 0)
          continue;
        if (comm.nextNDate(businessDate, 180).compareTo(oriIssueDate) > 0)
          newCardFlag = 1;
        break;
      }
    }
    if ((aliveFlag == 0) && (newCardFlag == 0))
      return (3); // 無有效卡 舊卡友
    if ((aliveFlag == 0) && (newCardFlag == 1))
      return (2); // 無有效卡 新卡友
    if ((aliveFlag == 1) && (newCardFlag == 0))
      return (1); // 有有效卡 舊卡友
    if ((aliveFlag == 1) && (newCardFlag == 1))
      return (0); // 有有效卡 新卡友
    return (0);
  }
  // ************************************************************************

} // End of class CommRoutine

