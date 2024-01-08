/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1110Func extends FuncEdit {
  private String PROGNAME = "新貴通貴賓室發行參數維護處理程式108/06/12 V1.00.01";
  String binType, groupCode, vipKind;
  String controlTabName = "mkt_ppcard_issue";

  public Mktm1110Func(TarokoCommon wr) {
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
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      binType = wp.itemStr("kk_bin_type");
      groupCode = wp.itemStr("kk_group_code");
      vipKind = wp.itemStr("kk_vip_kind");
      if (empty(groupCode)) {
        errmsg("團體代號 不可空白");
        return;
      }
    } else {
      binType = wp.itemStr("bin_type");
      groupCode = wp.itemStr("group_code");
      vipKind = wp.itemStr("vip_kind");
    }
    if (this.ibAdd)
      if (binType.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where bin_type = ? "
            + " and   group_code = ? " + " and   vip_kind = ? ";
        Object[] param = new Object[] {binType, groupCode, vipKind};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[卡別][團體代號][貴賓卡] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("issue_seq").length() == 0)
        wp.itemSet("issue_seq", "0");
      if (wp.itemNum("issue_seq") == 0) {
        errmsg("發行順序不可為 0 !");
        return;
      }
      strSql = "select bin_type " + "from mkt_ppcard_issue " + " where (bin_type != ? "
          + "  or group_code != ?  " + "  or vip_kind != ?) " + " and issue_seq = ? ";
      Object[] param = new Object[] {binType, groupCode, vipKind, wp.itemNum("issue_seq")};
      sqlSelect(strSql, param);

      if (sqlRowNum > 0) {
        errmsg("發行順序[" + wp.itemStr("issue_seq") + "]不可重複 !");
        return;
      }
      if (wp.itemStr("ppcard_bin_no").length() != 6) {
        errmsg("BIN_NO[" + wp.itemStr("ppcard_bin_no") + "]長度必須為6碼 !");
        return;
      }
      if (wp.itemStr("valid_month").length() == 0)
        wp.itemSet("valid_month", "0");
      if (wp.itemStr("holder_amt").length() == 0)
        wp.itemSet("holder_amt", "0");
      if (wp.itemStr("toget_amt").length() == 0)
        wp.itemSet("toget_amt", "0");

      if (wp.itemNum("valid_month") == 0) {
        errmsg("有效月數不可為 0 !");
        return;
      }
      /*
       * if (wp.item_num("holder_amt")==0) { errmsg("卡友單次自費金額不可為 0 !"); return; } if
       * (wp.item_num("toget_amt")==0) { errmsg("同行旅客自費金額不可為 0 !"); return; }
       */
      strSql = "select bin_no " + "from ptr_bintable " + " where bin_no = ?  ";
      param = new Object[] {wp.itemStr("ppcard_bin_no")};
      sqlSelect(strSql, param);

      if (sqlRowNum <= 0) {
        errmsg("BIN_NO[" + wp.itemStr("ppcard_bin_no") + "]不存在 !");
        return;
      }
    }


    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("ppcard_bin_no")) {
        errmsg("BIN-NO: 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " bin_type, " + " group_code, "
        + " vip_kind, " + " issue_seq, " + " card_type, " + " valid_month, " + " holder_amt, "
        + " toget_amt, " + " ppcard_bin_no, " + " ppcard_ica_no, " + " lost_fee, " + " make_fee, " + " ppcard_seqno, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    double makeFee = wp.itemNum("make_fee");
    if ("".equals(wp.itemStr("make_fee")) || wp.itemStr("make_fee") == null) {
      makeFee = 0;
    }

    Object[] param = new Object[] {binType, groupCode, vipKind, wp.itemNum("issue_seq"), wp.itemStr("card_type"),
        wp.itemNum("valid_month"), wp.itemNum("holder_amt"), wp.itemNum("toget_amt"),
        wp.itemStr("ppcard_bin_no"), wp.itemStr("ppcard_ica_no"), wp.itemNum("lost_fee"), makeFee, wp.itemStr("ppcard_seqno"),
        wp.loginUser, wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "issue_seq = ?, " + "card_type = ?, "
        + "valid_month = ?, " + "holder_amt = ?, " + "toget_amt = ?, " + "ppcard_bin_no = ?, " + " ppcard_seqno = ?, "
        + "ppcard_ica_no = ?, " + "lost_fee = ?, " + "make_fee = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    double makeFee = wp.itemNum("make_fee");
    if ("".equals(wp.itemStr("make_fee")) || wp.itemStr("make_fee") == null) {
      makeFee = 0;
    }

    Object[] param =
        new Object[] {wp.itemNum("issue_seq"), wp.itemStr("card_type"), wp.itemNum("valid_month"),
            wp.itemNum("holder_amt"), wp.itemNum("toget_amt"), wp.itemStr("ppcard_bin_no"), wp.itemStr("ppcard_seqno"),
            wp.itemStr("ppcard_ica_no"), wp.itemNum("lost_fee"), makeFee, wp.itemStr("approval_user"),
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
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
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
