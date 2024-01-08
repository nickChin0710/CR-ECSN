/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-27  V1.00.00  yash       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/

package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm4220Func extends FuncEdit {
  String mKkItemNo = "";

  public Mktm4220Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mKkItemNo = wp.itemStr("kk_item_no");
      if (empty(mKkItemNo)) {
        errmsg("請選擇卡片權益項目編號");
        return;
      }
    } else {
      mKkItemNo = wp.itemStr("item_no");
    }
    if (this.isAdd()) {

      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from mkt_contri_cnt2amt_parm where item_no = ?";
      Object[] param = new Object[] {mKkItemNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where item_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkItemNo, wp.modSeqno()};
      isOtherModify("mkt_contri_cnt2amt_parm", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into mkt_contri_cnt2amt_parm (" + " item_no " + ", use_cnt_n1 "
        + ", use_cnt_n2 " + ", use_cnt_n3 " + ", use_cnt_n4 " + ", use_cnt_n5 " + ", use_cnt_n6 "
        + ", tot_amt_n1 " + ", tot_amt_n2 " + ", tot_amt_n3 " + ", tot_amt_n4 " + ", tot_amt_n5 "
        + ", tot_amt_n6 " + ", use_cnt_o1 " + ", use_cnt_o2 " + ", use_cnt_o3 " + ", use_cnt_o4 "
        + ", use_cnt_o5 " + ", use_cnt_o6 " + ", tot_amt_o1 " + ", tot_amt_o2 " + ", tot_amt_o3 "
        + ", tot_amt_o4 " + ", tot_amt_o5 " + ", tot_amt_o6 " + ", crt_date, crt_user "
        + ", apr_date, apr_user " + ", mod_pgm, mod_seqno" + " ) values ("
        + "  ?,?,?,?,?,?,?,?,?,? " + ", ?,?,?,?,?,?,?,?,?,? " + ", ?,?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", to_char(sysdate,'yyyymmdd'), ?" + ", ?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkItemNo // 1
        , wp.itemNum("use_cnt_n1"), wp.itemNum("use_cnt_n2"), wp.itemNum("use_cnt_n3"),
        wp.itemNum("use_cnt_n4"), wp.itemNum("use_cnt_n5"), wp.itemNum("use_cnt_n6"),
        wp.itemNum("tot_amt_n1"), wp.itemNum("tot_amt_n2"), wp.itemNum("tot_amt_n3"),
        wp.itemNum("tot_amt_n4"), wp.itemNum("tot_amt_n5"), wp.itemNum("tot_amt_n6"),
        wp.itemNum("use_cnt_o1"), wp.itemNum("use_cnt_o2"), wp.itemNum("use_cnt_o3"),
        wp.itemNum("use_cnt_o4"), wp.itemNum("use_cnt_o5"), wp.itemNum("use_cnt_o6"),
        wp.itemNum("tot_amt_o1"), wp.itemNum("tot_amt_o2"), wp.itemNum("tot_amt_o3"),
        wp.itemNum("tot_amt_o4"), wp.itemNum("tot_amt_o5"), wp.itemNum("tot_amt_o6"), wp.loginUser,
        wp.itemStr("approval_user"), wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update mkt_contri_cnt2amt_parm set " + "  use_cnt_n1 =? " + ", use_cnt_n2 =? "
        + ", use_cnt_n3 =? " + ", use_cnt_n4 =? " + ", use_cnt_n5 =? " + ", use_cnt_n6 =? "
        + ", tot_amt_n1 =? " + ", tot_amt_n2 =? " + ", tot_amt_n3 =? " + ", tot_amt_n4 =? "
        + ", tot_amt_n5 =? " + ", tot_amt_n6 =? " + ", use_cnt_o1 =? " + ", use_cnt_o2 =? "
        + ", use_cnt_o3 =? " + ", use_cnt_o4 =? " + ", use_cnt_o5 =? " + ", use_cnt_o6 =? "
        + ", tot_amt_o1 =? " + ", tot_amt_o2 =? " + ", tot_amt_o3 =? " + ", tot_amt_o4 =? "
        + ", tot_amt_o5 =? " + ", tot_amt_o6 =? " + ", apr_user=? , apr_date =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("use_cnt_n1"), wp.itemStr("use_cnt_n2"),
        wp.itemStr("use_cnt_n3"), wp.itemStr("use_cnt_n4"), wp.itemStr("use_cnt_n5"),
        wp.itemStr("use_cnt_n6"), wp.itemStr("tot_amt_n1"), wp.itemStr("tot_amt_n2"),
        wp.itemStr("tot_amt_n3"), wp.itemStr("tot_amt_n4"), wp.itemStr("tot_amt_n5"),
        wp.itemStr("tot_amt_n6"), wp.itemStr("use_cnt_o1"), wp.itemStr("use_cnt_o2"),
        wp.itemStr("use_cnt_o3"), wp.itemStr("use_cnt_o4"), wp.itemStr("use_cnt_o5"),
        wp.itemStr("use_cnt_o6"), wp.itemStr("tot_amt_o1"), wp.itemStr("tot_amt_o2"),
        wp.itemStr("tot_amt_o3"), wp.itemStr("tot_amt_o4"), wp.itemStr("tot_amt_o5"),
        wp.itemStr("tot_amt_o6"), wp.itemStr("approval_user"), getSysDate(), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkItemNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete mkt_contri_cnt2amt_parm " + sqlWhere;
    Object[] param = new Object[] {mKkItemNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
