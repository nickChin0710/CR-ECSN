/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6280Func extends FuncEdit {
  private String PROGNAME = "帳戶基金(現金回饋)明細檔維護作業處理程式108/12/12 V1.00.01";
  String tranSeqno;
  String orgControlTabName = "mkt_cashback_dtl";
  String controlTabName = "mkt_cashback_dtl_t";

  public Mktm6280Func(TarokoCommon wr) {
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
    strSql = " select " + " acct_type, " + " p_seqno, " + " id_p_seqno, " + " fund_code, "
        + " fund_name, " + " tran_code, " + " beg_tran_amt, " + " effect_e_date, " + " mod_reason, "
        + " mod_desc, " + " mod_memo, " + " end_tran_amt, " + " tran_pgm, "
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
      tranSeqno = wp.itemStr("tran_seqno");
    } else {
      tranSeqno = wp.itemStr("tran_seqno");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (tranSeqno.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where tran_seqno = ? ";
          Object[] param = new Object[] {tranSeqno};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[交易序號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (tranSeqno.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where tran_seqno = ? ";
        Object[] param = new Object[] {tranSeqno};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[交易序號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibAdd) {
      if (wp.itemStr("control_tab_name").equals(orgControlTabName)) {
        errmsg("已覆核資料, 只可查詢不可異動 !");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      strSql = "select " + " fund_name as fund_name " + " from  vmkt_fund_name"
          + " where fund_code = ? ";
      Object[] param = new Object[] {wp.itemStr("fund_code")};
      sqlSelect(strSql, param);

      wp.itemSet("fund_name", colStr("fund_name"));

      if (wp.itemStr("mod_desc").length() == 0)
        wp.itemSet("mod_desc", "帳戶基金(現金回饋)線上調整");

      if (wp.itemNum("beg_tran_amt") == 0) {
        errmsg("調整金額不可為 0 !");
        return;
      }

      if (wp.itemStr("fund_code").length() == 0) {
        errmsg("[基金代碼] 必須選取 !");
        return;
      }

      if (wp.itemStr("mod_reason").length() == 0) {
        errmsg("[異動原因] 必須選取 !");
        return;
      }
      /*
       * if (wp.item_ss("res_tran_amt").length()==0) wp.item_set("res_tran_amt","0"); if
       * (wp.item_ss("res_total_cnt").length()==0) wp.item_set("res_total_cnt","0"); if
       * (((wp.item_num("res_tran_amt")==0)&& (wp.item_num("res_total_cnt")!=0))||
       * ((wp.item_num("res_tran_amt")!=0)&& (wp.item_num("res_total_cnt")==0))) {
       * errmsg("分次贈送金額及次數均不可為 0 !"); return; }
       */
    }
    if (wp.itemStr("tran_seqno").length() == 0) {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("tran_seqno", comr.getSeqno("MKT_MODSEQ"));
      tranSeqno = wp.itemStr("tran_seqno");
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("acct_type")) {
        errmsg("帳戶類別: 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("tran_code")) {
        errmsg("交易類別： 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " tran_seqno, " + " aud_type, "
        + " acct_type, " + " p_seqno, " + " id_p_seqno, " + " fund_code, " + " fund_name, "
        + " tran_code, " + " beg_tran_amt, " + " effect_e_date, " + " mod_reason, " + " mod_desc, "
        + " mod_memo, " + " end_tran_amt, " + " tran_pgm, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {tranSeqno, wp.itemStr("aud_type"), wp.itemStr("acct_type"),
        wp.itemStr("p_seqno"), wp.itemStr("id_p_seqno"), wp.itemStr("fund_code"),
        wp.itemStr("fund_name"), wp.itemStr("tran_code"), wp.itemNum("beg_tran_amt"),
        wp.itemStr("effect_e_date"), wp.itemStr("mod_reason"), wp.itemStr("mod_desc"),
        wp.itemStr("mod_memo"), wp.itemNum("beg_tran_amt"), wp.modPgm(), wp.loginUser,
        wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
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

    strSql = "update " + controlTabName + " set " + "acct_type = ?, " + "fund_code = ?, "
        + "fund_name = ?, " + "tran_code = ?, " + "beg_tran_amt = ?, " + "effect_e_date = ?, "
        + "mod_reason = ?, " + "mod_desc = ?, " + "mod_memo = ?, " + "end_tran_amt = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("acct_type"), wp.itemStr("fund_code"),
        wp.itemStr("fund_name"), wp.itemStr("tran_code"), wp.itemNum("beg_tran_amt"),
        wp.itemStr("effect_e_date"), wp.itemStr("mod_reason"), wp.itemStr("mod_desc"),
        wp.itemStr("mod_memo"), wp.itemNum("beg_tran_amt"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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

} // End of class
