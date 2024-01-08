/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6120Func extends FuncEdit {
  private String PROGNAME = "年費優惠免收參數設定作業處理程式108/08/20 V1.00.01";
  String groupCode, cardTYpe;
  String controlTabName = "cyc_offer_afee_parm";

  public Mktm6120Func(TarokoCommon wr) {
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
      groupCode = wp.itemStr("kk_group_code");
      if (empty(groupCode)) {
        errmsg("團體代號 不可空白");
        return;
      }
      cardTYpe = wp.itemStr("kk_card_type");
    } else {
      groupCode = wp.itemStr("group_code");
      cardTYpe = wp.itemStr("card_type");
    }
    if (this.ibAdd)
      if (groupCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? "
            + " and   card_type = ? ";
        Object[] param = new Object[] {groupCode, cardTYpe};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[團體代號][卡種代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }



    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((wp.itemNum("sup_rate_rc") > 100) || (wp.itemNum("sup_end_rate_rc") > 100)
          || (wp.itemNum("sup_rate_nrc") > 100) || (wp.itemNum("sup_end_rate_nrc") > 100)) {
        errmsg("年費比例, 不可超過 100!");
        return;
      }
    }

    if (checkDecnum(wp.itemStr("last_ttl_amt"), 9, 2) != 0) {
      errmsg("結帳日之上期應繳結欠本金: 格式超出範圍 : [9][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("sup_end_rate_rc"), 4, 2) != 0) {
      errmsg(" 格式超出範圍 : [4][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("sup_rate_nrc"), 4, 2) != 0) {
      errmsg("附卡年費按正卡 格式超出範圍 : [4][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("sup_end_rate_nrc"), 4, 2) != 0) {
      errmsg(" 格式超出範圍 : [4][2]");
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


    strSql = " insert into  " + controlTabName + " (" + " group_code, " + " card_type, "
        + " last_ttl_amt, " + " first_fee_rc, " + " other_fee_rc, " + " sup_rate_rc, "
        + " sup_end_month_rc, " + " sup_end_rate_rc, " + " first_fee_nrc, " + " other_fee_nrc, "
        + " sup_rate_nrc, " + " sup_end_month_nrc, " + " sup_end_rate_nrc, " + " apr_date, "
        + " apr_flag, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {groupCode, cardTYpe, wp.itemNum("last_ttl_amt"), wp.itemNum("first_fee_rc"),
        wp.itemNum("other_fee_rc"), wp.itemNum("sup_rate_rc"), wp.itemNum("sup_end_month_rc"),
        wp.itemNum("sup_end_rate_rc"), wp.itemNum("first_fee_nrc"), wp.itemNum("other_fee_nrc"),
        wp.itemNum("sup_rate_nrc"), wp.itemNum("sup_end_month_nrc"), wp.itemNum("sup_end_rate_nrc"),
        "Y", wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "last_ttl_amt = ?, " + "first_fee_rc = ?, "
        + "other_fee_rc = ?, " + "sup_rate_rc = ?, " + "sup_end_month_rc = ?, "
        + "sup_end_rate_rc = ?, " + "first_fee_nrc = ?, " + "other_fee_nrc = ?, "
        + "sup_rate_nrc = ?, " + "sup_end_month_nrc = ?, " + "sup_end_rate_nrc = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemNum("last_ttl_amt"), wp.itemNum("first_fee_rc"),
        wp.itemNum("other_fee_rc"), wp.itemNum("sup_rate_rc"), wp.itemNum("sup_end_month_rc"),
        wp.itemNum("sup_end_rate_rc"), wp.itemNum("first_fee_nrc"), wp.itemNum("other_fee_nrc"),
        wp.itemNum("sup_rate_nrc"), wp.itemNum("sup_end_month_nrc"), wp.itemNum("sup_end_rate_nrc"),
        wp.loginUser, wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }
  // ************************************************************************

} // End of class
