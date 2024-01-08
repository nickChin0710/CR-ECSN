/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/15   V1.00.01  Zuwei  fix    coding scan issue                      *
*  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.03    Zuwei     coding standard, rename field method                   *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       * 
*  109/09/04  V1.00.06    Zuwei     code scan issue    
*110-01-07  V1.00.7    shiyuqi       修改无意义命名                               
* 111/01/18  V1.00.08  Justin     fix Erroneous String Compare               *                                            *
*****************************************************************************/
package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.Properties;

import Dxc.Util.SecurityUtil;

import java.sql.Connection;

public class CommWbk extends AccessDAO {

  CommCrd comc = new CommCrd();
  CommIps comips = new CommIps();

  String[] dbname = new String[10];

  String h0001LogoutDatetime = "";
  String h0001ELogoutDatetime = "";
  long hInt = 0;
  String h0001Rowid = "";
  String h0001Id = "";

  String h0002WebSecCode = "";
  String h0002PawdResetFlag = "";
  String h0002UserId = "";
  String h0002IdPSeqno = "";
  String h0002UserPawd = "";
  String h0002Id = "";

  String filename = "";
  String tmpstr = "";
  int TimeOut = 0;

  public class Rtnval {
    public int cmdInt = 0;
    public int chgType = 0;
    public String id = "";
    public String userId = "";
    public String userPawd = "";
    public String retId = "";
    public String retPawd = "";
  }

  public CommWbk(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    notExit = "Y";
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    dbname[0] = dbAlias[0];

    checkOpen();

    return;
  }

  /***********************************************************************/
  private int checkOpen() throws Exception {
    filename = String.format("%s/etc/%s", comc.getECSHOME(), "wbk_0000.dat");
    filename = SecurityUtil.verifyPath(filename);
    File f = new File(filename);
    if (f.exists() == false) {
      comc.errExit("檔案不存在：" + filename, "");
    }
    Properties properties = new Properties();
    try (FileInputStream in = new FileInputStream(filename)) {
      properties.load(in);
    } catch (FileNotFoundException ex) {
      comc.errExit("開檔錯誤訊息：" + ex.getMessage(), "");
    } catch (IOException ex) {
      comc.errExit("讀檔錯誤訊息：" + ex.getMessage(), "");
    }
    TimeOut = Integer.valueOf(properties.getProperty("timeout", "10"));
    // try {
    // properties.load(new FileInputStream(filename));
    // } catch (FileNotFoundException ex) {
    // comc.err_exit("開檔錯誤訊息：" + ex.getMessage(), "");
    // } catch (IOException ex) {
    // comc.err_exit("讀檔錯誤訊息：" + ex.getMessage(), "");
    // }

    // 第二個參數為預設值，如果沒取到值的時候回傳預設值
    // port = Integer.valueOf(properties.getProperty("port", "9001"));
    // TimeOut = Integer.valueOf(properties.getProperty("timeout", "10"));
    // sw_check = properties.getProperty("sw_check", "Y");
    // SMS_TimeOut = Integer.valueOf(properties.getProperty("sms_timeout",
    // "10"));
    return (0);
  }

  /***********************************************************************/
  public int commWebLogin(String id) throws Exception {

    h0001LogoutDatetime = "";
    h0001ELogoutDatetime = "";
    h0001Id = id;

    sqlCmd = "select logout_datetime,";
    sqlCmd +=
        " decode(sign(e_logout_datetime- to_char(sysdate+ ? minutes,'yyyymmddhh24miss')), 1,e_logout_datetime, to_char(sysdate+ ? minutes,'yyyymmddhh24miss')) h_0001_e_logout_datetime,";
    sqlCmd += " (sysdate - to_date(e_logout_datetime,'yyyymmddhh24miss'))/60 h_int,";
    sqlCmd += " rowid rowid ";
    sqlCmd += "  from web_login  ";
    sqlCmd += " where id_no = ? ";
    setInt(1, TimeOut);
    setInt(2, TimeOut);
    setString(3, h0001Id);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      h0001LogoutDatetime = getValue("logout_datetime");
      h0001ELogoutDatetime = getValue("h_0001_e_logout_datetime");
      hInt = getValueLong("h_int");
      h0001Rowid = getValue("rowid");
    } else {
      return (10);
    }

    if (h0001LogoutDatetime.length() != 0)
      return (11);
    if (hInt > 0) {
      updateWebLogin(1);
      return (1);
    }
    updateWebLogin(0);
    return (0);
  }

  /***********************************************************************/
  private int updateWebLogin(int hInt) throws Exception {
    daoTable = "web_login";
    updateSQL =
        " update_datetime   = decode(cast(? as varchar(14)), 0, to_char(sysdate, 'yyyymmddhh24miss'), update_datetime),";
    updateSQL += " e_logout_datetime = decode(cast(? as varchar(14)), 0, ?, e_logout_datetime),";
    updateSQL +=
        " logout_datetime   = decode(cast(? as varchar(14)), 0, logout_datetime, to_char(sysdate, 'yyyymmddhh24miss')),";
    updateSQL += " logout_type       = decode(cast(? as varchar(8)), 0, logout_type, '3'),";
    updateSQL += " mod_pgm           = 'Wbk0001',";
    updateSQL += " mod_time          = sysdate";
    whereStr = "where rowid          = ? ";
    setLong(1, hInt);
    setLong(2, hInt);
    setString(3, h0001ELogoutDatetime);
    setLong(4, hInt);
    setLong(5, hInt);
    setRowId(6, h0001Rowid);
    updateTable();
    if (notFound.equals("Y")) {
      return (-1);
    }
    return (0);
  }

  /***********************************************************************/

  /*****************************************************************************/
  public String commWebSecCode(String paradata1) {
    int[] aType = {9, 3, 7, 5, 1, 2, 0, 4, 8, 6};
    int[] bType = {0, 6, 2, 4, 8, 7, 9, 5, 1, 3};
    int[] cType = {5, 4, 3, 9, 2, 8, 1, 7, 0, 6};
    int[] dType = {4, 5, 6, 0, 7, 1, 8, 2, 9, 3};
    int int1, int2, int3;
    String tmpstr = "";
    String paradata2 = "";

    if (paradata1.length() != 10)
      return paradata2;

    int3 = paradata1.toCharArray()[8] % 4;
    for (int1 = 0; int1 < 10; int1++) {
      if (int1 % 2 == 0)
        int2 = 1;
      else
        int2 = -1;

      char chars = '\0';
      if (int3 == 0)
        chars = (char) (paradata1.toCharArray()[aType[int1]] + int2);
      else if (int3 == 1)
        chars = (char) (paradata1.toCharArray()[bType[int1]] + int2);
      else if (int3 == 2)
        chars = (char) (paradata1.toCharArray()[cType[int1]] + int2);
      else if (int3 == 3)
        chars = (char) (paradata1.toCharArray()[dType[int1]] + int2);

      if (chars < 48)
        tmpstr = tmpstr + (char) (57);
      else if (chars > 57)
        tmpstr = tmpstr + (char) (48);
      else
        tmpstr = tmpstr + chars;
    }
    paradata2 = tmpstr;
    return paradata2;
  }

  /*************************************************************************/
  public Rtnval commWebUsrpwd(int chgType, String id, String userId, String userPasswd,
      String retId, String retPasswd) throws Exception {
    String chkno = "";
    String tmpstr = "";
    String tmpData = "";
    String tmpData2 = "";
    Rtnval rtn = new Rtnval();

    int int1 = 0, int2 = 0, int3 = 0, retLen = 0;

    if ((chgType != 0) && (chgType != 1) && (chgType != 2)) {
      rtn.cmdInt = -1;
      rtn.chgType = chgType;
      rtn.id = id;
      rtn.userId = userId;
      rtn.userPawd = userPasswd;
      rtn.retId = retId;
      rtn.retPawd = retPasswd;

      return rtn;
    }

    if (chgType == 2) {
      h0002WebSecCode = id;
    } else {
      h0002Id = id;
      if (selectWebIdno() != 0) {
        rtn.cmdInt = -1;
        rtn.chgType = chgType;
        rtn.id = id;
        rtn.userId = userId;
        rtn.userPawd = userPasswd;
        rtn.retId = retId;
        rtn.retPawd = retPasswd;

        return rtn;
      }
    }

    if (h0002WebSecCode.length() == 0) {
      if (chgType == 0) {
        if ("P".equals(h0002PawdResetFlag)) {
          h0002WebSecCode = commWebSecCode(h0002IdPSeqno);
          for (int1 = 0; int1 < 12; int1++)
            chkno = chkno + h0002WebSecCode;

          tmpstr = String.format("%c%c", h0002WebSecCode.toCharArray()[9],
              h0002WebSecCode.toCharArray()[6]);
          int2 = str2int(tmpstr);

          tmpData = "";
          for (int1 = 0; int1 < userId.length(); int1++) {
            if (int1 % 2 == 0) {
              int3 = 1;
            } else {
              int3 = -1;
            }
            tmpstr = String.format("%d", chkno.toCharArray()[int2 + int1] + 7);
            tmpData = tmpData + (char) (userId.toCharArray()[int1] + str2int(tmpstr) * int3);
          }
          tmpstr = commStr2hex(tmpData, userId.length());
          retId = tmpstr;
        } else {
          retId = userId;
        }
        retPasswd = userPasswd;
      } else {
        userId = retId;
        userPasswd = retPasswd;
      }
      if (chgType == 0) {
        if (!retId.equals(h0002UserId)) {
          rtn.cmdInt = 1;
          rtn.chgType = chgType;
          rtn.id = id;
          rtn.userId = userId;
          rtn.userPawd = userPasswd;
          rtn.retId = retId;
          rtn.retPawd = retPasswd;

          return rtn;
        }
        if (!retPasswd.equals(h0002UserPawd)) {
          rtn.cmdInt = 2;
          rtn.chgType = chgType;
          rtn.id = id;
          rtn.userId = userId;
          rtn.userPawd = userPasswd;
          rtn.retId = retId;
          rtn.retPawd = retPasswd;

          return rtn;
        }
      }
      rtn.cmdInt = 0;
      rtn.chgType = chgType;
      rtn.id = id;
      rtn.userId = userId;
      rtn.userPawd = userPasswd;
      rtn.retId = retId;
      rtn.retPawd = retPasswd;

      return rtn;

    }

    for (int1 = 0; int1 < 12; int1++)
      chkno = chkno + h0002WebSecCode;

    tmpstr =
        String.format("%c%c", h0002WebSecCode.toCharArray()[9], h0002WebSecCode.toCharArray()[6]);
    int2 = str2int(tmpstr);

    if (chgType != 1) {
      tmpData = "";
      for (int1 = 0; int1 < userId.length(); int1++) {
        if (int1 % 2 == 0) {
          int3 = 1;
        } else {
          int3 = -1;
        }
        tmpstr = String.format("%d", chkno.toCharArray()[int2 + int1] + 7);
        tmpData = tmpData + (char) (userId.toCharArray()[int1] + str2int(tmpstr) * int3);
      }
      tmpstr = commStr2hex(tmpData, userId.length());
      retId = tmpstr;

      tmpData = "";
      for (int1 = 0; int1 < userPasswd.length(); int1++) {
        if (int1 % 2 == 0) {
          int3 = -1;
        } else {
          int3 = 1;
        }
        tmpstr = String.format("%d", chkno.toCharArray()[int2 + int1] - 7);
        tmpData = tmpData + (char) (userPasswd.toCharArray()[int1] + str2int(tmpstr) * int3);
      }

      tmpstr = commStr2hex(tmpData, userPasswd.length());
      retPasswd = String.format(tmpstr);

      if (chgType == 0) {
        if (retId.compareTo(h0002UserId) != 0) {
          rtn.cmdInt = 1;
          rtn.chgType = chgType;
          rtn.id = id;
          rtn.userId = userId;
          rtn.userPawd = userPasswd;
          rtn.retId = retId;
          rtn.retPawd = retPasswd;

          return rtn;
        }
        if (retPasswd.compareTo(h0002UserPawd) != 0) {
          rtn.cmdInt = 2;
          rtn.chgType = chgType;
          rtn.id = id;
          rtn.userId = userId;
          rtn.userPawd = userPasswd;
          rtn.retId = retId;
          rtn.retPawd = retPasswd;

          return rtn;
        }
      }
    } else {

      tmpData = "";
      tmpstr = new String(hexStrToByteArr(retId)); // COMM_hex2str
      retLen = retId.length() / 2;

      tmpData2 = comc.getSubString(tmpstr, 0, retLen);

      for (int1 = 0; int1 < retLen; int1++) {
        if (int1 % 2 == 0) {
          int3 = -1;
        } else {
          int3 = 1;
        }
        tmpstr = String.format("%d", chkno.toCharArray()[int2 + int1] + 7);
        tmpData = tmpData + (char) (tmpData2.toCharArray()[int1] + str2int(tmpstr) * int3);
      }
      userId = String.format(tmpData);

      tmpData = "";
      tmpstr = new String(hexStrToByteArr(retId));// COMM_hex2str
      retLen = retPasswd.length() / 2;

      tmpData2 = comc.getSubString(tmpstr, 0, retLen);

      for (int1 = 0; int1 < retLen; int1++) {
        if (int1 % 2 == 0) {
          int3 = 1;
        } else {
          int3 = -1;
        }
        tmpstr = String.format("%d", chkno.toCharArray()[int2 + int1] - 7);
        tmpData = tmpData + (char) (tmpData2.toCharArray()[int1] + str2int(tmpstr) * int3);
      }
      userPasswd = String.format(tmpData);
    }

    rtn.cmdInt = 0;
    rtn.chgType = chgType;
    rtn.id = id;
    rtn.userId = userId;
    rtn.userPawd = userPasswd;
    rtn.retId = retId;
    rtn.retPawd = retPasswd;

    return rtn;

    // return (0);
  }

  // ************************************************************************
  public int str2int(String val) {
    int rtn = 0;
    try {
      rtn = Integer.parseInt(val.replaceAll(",", "").trim());
    } catch (Exception e) {
      rtn = 0;
    }
    return rtn;
  }

  /*****************************************************************************/
  private String commStr2hex(String tmpstr1, int dataLen) {
    int inta = 0;
    String tmpbb = "", tmpaa = "";
    String tmpstr2 = "";

    for (inta = 0; inta < dataLen; inta++) {
      tmpbb = String.format("%02X", (short) tmpstr1.toCharArray()[inta]);
      tmpaa = tmpaa + tmpbb;
    }
    tmpstr2 = tmpaa.substring(0, dataLen * 2);
    return tmpstr2;
  }

  /************************************************************************/
  private int selectWebIdno() throws Exception {
    h0002WebSecCode = "";
    h0002PawdResetFlag = "";
    h0002UserId = "";
    h0002IdPSeqno = "";
    h0002UserPawd = "";

    sqlCmd = "SELECT a.web_sec_code,   ";
    sqlCmd += "      a.pwd_reset_flag, ";
    sqlCmd += "      a.user_id,        ";
    sqlCmd += "      a.id_p_seqno,     ";
    sqlCmd += "      a.user_passwd     ";
    sqlCmd += "FROM  web_idno a, crd_idno b       ";
    sqlCmd += "WHERE b.id_p_seqno = a.id_p_seqno and b.id_no = ?  ";
    setString(1, h0002Id);
    selectTable();
    if (notFound.equals("Y")) {
      comips.logPrintf(0, "[CommWbk] select_web_idno1 not found!");
      return (-1);
    }
    h0002WebSecCode = getValue("web_sec_code");
    h0002PawdResetFlag = getValue("pwd_reset_flag");
    h0002UserId = getValue("user_id");
    h0002IdPSeqno = getValue("id_p_seqno");
    h0002UserPawd = getValue("user_passwd");

    return (0);
  }

}
