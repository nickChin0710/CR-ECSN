/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/15   V1.00.01  Zuwei  fix    coding scan issue                      *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.03    Zuwei     coding standard, rename field method & format                   *
* 111/01/18  V1.00.04  Justin     fix Erroneous String Compare               *
*****************************************************************************/
package com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

public class CommAuthCtrl extends AccessDAO {

  CommCrd comc = new CommCrd();

  String[] dbname = new String[10];
  String hAuthAutPgmid = "";
  String[] hMUserUsrId = new String[250];
  String hUserUsrId = "";
  String hUserUsrPd = "";
  String hAuthAutRun = "";
  String hAuthAutApprove = "";
  Scanner scanner = null;

  public CommAuthCtrl(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    dbname[0] = dbAlias[0];

    return;
  }

  public int authorityCtrl(String pgmName, int userCnt) throws Exception {
    String[] user1 = new String[10];
    String[] mPd = new String[10];
    int inta, intb, chk = 0;
    String ldata;

    hAuthAutPgmid = pgmName;
    for (inta = 0; inta < userCnt; inta++) {
      scanner = new Scanner(System.in);
      showLogMessage("I", "", String.format("Enter %dth User ID : ", inta + 1));
      user1[inta] = scanner.nextLine();
      showLogMessage("I", "", "Password : ");
      mPd[inta] = scanner.nextLine();
      // strcpy(pass1[inta],getpass("Password : "));
      /*****************************************************
       * main function 需抓取多組user id 2001/11/5 shu yu
       *****************************************************/
      hMUserUsrId[inta] = "";
      hUserUsrId = "";
      hMUserUsrId[inta] = user1[inta];
      hUserUsrId = user1[inta];
      ldata = "";
      ArrayList<String> rcv = new ArrayList<String>();
      rcv = secPasswd(mPd[inta], ldata);
      ldata = rcv.get(1);
      if ((chk = comc.str2int(rcv.get(0))) != 0) {
        showLogMessage("I", "", String.format("密碼錯誤"));
        return (1);
      }
      hUserUsrPd = ldata;
      if (selectSecSystem(userCnt, inta) != 0)
        return (1);
    }

    for (inta = 0; inta < userCnt; inta++)
      for (intb = inta + 1; intb < userCnt; intb++)
        if (user1[inta].equals(user1[intb])) {
          showLogMessage("I", "", String.format("\n\n USER CAN'T BE SAME ONE\n"));
          return (1);
        }

    return (0);
  }

  /*****************************************************************************/
  private int selectSecSystem(int chkInt1, int chkInt2) throws Exception {
    int int3;

    int3 = chkInt1 - chkInt2;
    sqlCmd = " SELECT a.aut_run, ";
    sqlCmd = "        a.aut_approve ";
    sqlCmd = " FROM   sec_authority a,sec_user b,sec_usergroup c ";
    sqlCmd = " where  b.usr_id       = c.ug_userid ";
    sqlCmd = " and    c.ug_groupid   = a.aut_groupid ";
    sqlCmd = " and    b.usr_level    = a.aut_userlevel ";
    sqlCmd = " and    b.usr_password = ? ";
    sqlCmd = " and    b.usr_id       = ? ";
    sqlCmd = " and    a.aut_pgmid    = ? ";
    setString(1, hUserUsrPd);
    setString(2, hUserUsrId);
    setString(3, hAuthAutPgmid);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hAuthAutRun = getValue("aut_run", i);
      hAuthAutApprove = getValue("aut_approve", i);
      if (((int3 != 1) || (chkInt1 == 1)) && (hAuthAutRun.equals("Y")))
        return (0);
      if ((int3 == 1) && (chkInt1 != 1) && (hAuthAutApprove.equals("Y")))
        return (0);
    }
    if ((int3 != 1) || (chkInt1 == 1)) {
      showLogMessage("I", "", String.format("\n\n NO AUTHORITY TO RUN THIS PROGRAM!!!\n\n"));
    } else {
      showLogMessage("I", "", String.format("\n\n NO AUTHORITY TO APPROVE THIS PROGRAM!!!\n\n"));
    }
    return (1);
  }

  private ArrayList<String> secPasswd(String tpasswd, String data) throws Exception {
    ArrayList<String> rtn = new ArrayList<String>();
    int len = 0;
    int tlen = 0;
    int i = 0, j = 0;
    int chk = 0;
    String val;
    String isChar = "";
    String fpawd = "";
    String real;
    String tmp;
    rtn.add(0, "");
    rtn.add(1, "");
    isChar = "zaq1xsw2cde3vfr4bgt5nhy6mju7,ki8.lo9/;p0-\\=";
    len = tpasswd.length();
    if (len > 8) {
      showLogMessage("I", "", String.format("密碼長度超過 8 碼"));
      return rtn;
    }
    tlen = isChar.length();
    for (i = 0; i < len; i++) {
      val = "";
      val = comc.getSubString(tpasswd, i, i + 1);
      chk = 0;
      for (j = 0; j < tlen; j++) {
        if (val.equals(isChar.substring(j, j + 1))) {
          tmp = "";
          tmp = String.format("%02d", j + 1);
          fpawd += tmp;
          chk = 1;
          break;
        }
      }
      if (chk == 0) {
        showLogMessage("I", "", String.format("no encpty"));
        break;
      }
    }
    real = "";
    /*******************************************
     * trans_passwd只能接受八碼, 因此每次不可傳超過八碼
     *******************************************/
    /*
     * printf("FPASSWD[%s]\n",fpasswd);
     */
    if (fpawd.length() > 8) {
      /*********************************
       * 第一個八碼
       *********************************/
      tmp = "";
      tmp = fpawd;
      tmp = comc.transPasswd(0, tmp);
      real += tmp;
      tmp = "";
      tmp = fpawd.substring(8);
      tmp = comc.transPasswd(0, tmp);
      real += tmp;
    } else {
      real = comc.transPasswd(0, fpawd);
    }
    data = real;
    /*
     * printf("REAL trans_passwd[%s] DATA [%s]\n",real,data);
     */
    rtn.set(0, "0");
    rtn.set(1, data);
    return rtn;
  }

}
