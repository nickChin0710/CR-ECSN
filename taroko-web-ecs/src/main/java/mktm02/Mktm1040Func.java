/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名  
* 111-06-08  V1.00.04   machao      新增、編輯/刪除畫面,新增两个字段                                                                                    *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1040Func extends FuncEdit {
  private String PROGNAME = "高鐵車廂升等卡類維護作業處理程式110/06/29 V1.00.04";
  String cardMode;
  String orgControlTabName = "mkt_thsr_upmode";
  String controlTabName = "mkt_thsr_upmode_t";

  public Mktm1040Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " mode_desc, "+" start_date, " + " stop_flag, " + " stop_date, " + " stop_desc, "
        + " card_type_sel, " + " group_code_sel, " + " max_ticket_cnt, " + " ticket_pnt_cond, "
        + " ticket_pnt_cnt, " + " ticket_pnt, " + " ticket_amt_cond, " + " ticket_amt_cnt, "
        + " ticket_amt, " + " ex_ticket_amt, " + " add_file_flag, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardMode = wp.itemStr("card_mode");
    } else {
      cardMode = wp.itemStr("card_mode");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (cardMode.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where card_mode = ? ";
          Object[] param = new Object[] {cardMode};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[車廂升等卡類] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (cardMode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where card_mode = ? ";
        Object[] param = new Object[] {cardMode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[車廂升等卡類] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("stop_flag").equals("Y"))
      wp.itemSet("stop_flag", "N");
    if (!wp.itemStr("ticket_pnt_cond").equals("Y"))
      wp.itemSet("ticket_pnt_cond", "N");
    if (!wp.itemStr("ticket_amt_cond").equals("Y"))
      wp.itemSet("ticket_amt_cond", "N");


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("ex_ticket_amt").length() == 0)
        wp.itemSet("ex_ticket_amt", "0");
      if (wp.itemNum("ex_ticket_amt") == 0) {
        errmsg("其餘每張加檔金額不可為 0 !");
        return;
      }
      if (wp.itemStr("ticket_pnt_cond").equals("Y")) {
        if (wp.itemStr("ticket_pnt_cnt").length() == 0)
          wp.itemSet("ticket_pnt_cnt", "0");
        if (wp.itemNum("ticket_pnt_cnt") == 0) {
          errmsg("紅利積點張數條件不可為 0 !");
          return;
        }
        if (wp.itemStr("ticket_pnt").length() == 0)
          wp.itemSet("ticket_pnt", "0");
        if (wp.itemNum("ticket_pnt") == 0) {
          errmsg("每張扣除紅利不可為 0 !");
          return;
        }
      }
      if (wp.itemStr("ticket_amt_cond").equals("Y")) {
        if (wp.itemStr("ticket_amt_cnt").length() == 0)
          wp.itemSet("ticket_amt_cnt", "0");
        if (wp.itemNum("ticket_amt_cnt") == 0) {
          errmsg("加檔金額張數條件不可為 0 !");
          return;
        }
        if (wp.itemStr("ticket_amt").length() == 0)
          wp.itemSet("ticket_amt", "0");
        if (wp.itemNum("ticket_amt") == 0) {
          errmsg("每張加檔金額不可為 0 !");
          return;
        }
      }
    }
    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("mode_desc")) {
        errmsg("卡類說明： 不可空白");
        return;
      }
    if ((this.ibAdd) || (this.ibUpdate))
        if (!wp.itemEmpty("start_date") && !wp.itemEmpty("stop_date")) {
        	int start_date = Integer.parseInt(wp.itemStr2("start_date"));
        	int stop_date = Integer.parseInt(wp.itemStr2("stop_date"));
          if(start_date == stop_date) {
        	  errmsg("啓用日期、停用日期,不可是同一天");
          }else if(start_date > stop_date) {
        	  errmsg("啓用日期需小於停用日期");
          }
          
          return;
        }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  " + controlTabName + " (" + " card_mode, " + " aud_type, "
        + " mode_desc, " + " start_date, " + " stop_flag, " + " stop_date, " + " stop_desc, " + " card_type_sel, "
        + " group_code_sel, " + " max_ticket_cnt, " + " ticket_pnt_cond, " + " ticket_pnt_cnt, "
        + " ticket_pnt, " + " ticket_amt_cond, " + " ticket_amt_cnt, " + " ticket_amt, "
        + " ex_ticket_amt, " + " add_file_flag, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {cardMode, wp.itemStr("aud_type"), wp.itemStr("mode_desc"),wp.itemStr("start_date"),
        wp.itemStr("stop_flag"), wp.itemStr("stop_date"), wp.itemStr("stop_desc"),
        wp.itemStr("card_type_sel"), wp.itemStr("group_code_sel"), wp.itemNum("max_ticket_cnt"),
        wp.itemStr("ticket_pnt_cond"), wp.itemNum("ticket_pnt_cnt"), wp.itemNum("ticket_pnt"),
        wp.itemStr("ticket_amt_cond"), wp.itemNum("ticket_amt_cnt"), wp.itemNum("ticket_amt"),
        wp.itemNum("ex_ticket_amt"),wp.itemStr("add_file_flag"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'MKT_THSR_UPMODE' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("card_mode"),};

    sqlExec(strSql, param);


    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "mode_desc = ?, " + "start_date = ?, " + "stop_flag = ?, "
        + "stop_date = ?, " + "stop_desc = ?, " + "card_type_sel = ?, " + "group_code_sel = ?, "
        + "max_ticket_cnt = ?, " + "ticket_pnt_cond = ?, " + "ticket_pnt_cnt = ?, "
        + "ticket_pnt = ?, " + "ticket_amt_cond = ?, " + "ticket_amt_cnt = ?, " + "ticket_amt = ?, "
        + "ex_ticket_amt = ?, " + "add_file_flag = ?, " + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("mode_desc"),wp.itemStr("start_date"), wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemStr("card_type_sel"),
        wp.itemStr("group_code_sel"), wp.itemNum("max_ticket_cnt"), wp.itemStr("ticket_pnt_cond"),
        wp.itemNum("ticket_pnt_cnt"), wp.itemNum("ticket_pnt"), wp.itemStr("ticket_amt_cond"),
        wp.itemNum("ticket_amt_cnt"), wp.itemNum("ticket_amt"), wp.itemNum("ex_ticket_amt"),wp.itemStr("add_file_flag"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'MKT_THSR_UPMODE' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("card_mode"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm1040_cdtp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm1040_gpcd"))
      dataType = "2";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_THSR_UPMODE', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("card_mode"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm1040_cdtp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm1040_gpcd"))
      dataType = "2";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("card_mode")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_THSR_UPMODE' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_THSR_UPMODE'  ";
    sqlExec(strSql, param);


    return 1;

  }
  // ************************************************************************

} // End of class
