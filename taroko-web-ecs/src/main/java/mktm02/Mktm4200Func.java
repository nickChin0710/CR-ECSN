/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-28  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm4200Func extends FuncEdit {
  String kk1Rowid = "", itemEnameIncl = "", itemEnameExcl = "", keyData = "", costAmt = "";

  public Mktm4200Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TOD11111
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    kk1Rowid = wp.itemStr("rowid");
    String[] excl = new String[6];
    String[] incl = new String[6];
    if (wp.itemNum("cost_amt") < 0) {
      errmsg("成本金額 要 >= 0");
      return;
    }
    if ((wp.itemStr("excl_bl").equals("Y") && !wp.itemStr("incl_bl").equals("Y"))
        || (wp.itemStr("excl_it").equals("Y") && !wp.itemStr("incl_it").equals("Y"))
        || (wp.itemStr("excl_ca").equals("Y") && !wp.itemStr("incl_ca").equals("Y"))
        || (wp.itemStr("excl_id").equals("Y") && !wp.itemStr("incl_id").equals("Y"))
        || (wp.itemStr("excl_ao").equals("Y") && !wp.itemStr("incl_ao").equals("Y"))
        || (wp.itemStr("excl_ot").equals("Y") && !wp.itemStr("incl_ot").equals("Y"))) {
      errmsg("排除科目 輸入錯誤~");
      return;
    }
    if (wp.itemStr("excl_bl").equals("Y")) {
      excl[0] = "Y";
    } else {
      excl[0] = "N";
    }
    if (wp.itemStr("excl_it").equals("Y")) {
      excl[1] = "Y";
    } else {
      excl[1] = "N";
    }
    if (wp.itemStr("excl_ca").equals("Y")) {
      excl[2] = "Y";
    } else {
      excl[2] = "N";
    }
    if (wp.itemStr("excl_id").equals("Y")) {
      excl[3] = "Y";
    } else {
      excl[3] = "N";
    }
    if (wp.itemStr("excl_ao").equals("Y")) {
      excl[4] = "Y";
    } else {
      excl[4] = "N";
    }
    if (wp.itemStr("excl_ot").equals("Y")) {
      excl[5] = "Y";
    } else {
      excl[5] = "N";
    }
    if (wp.itemStr("incl_bl").equals("Y")) {
      incl[0] = "Y";
    } else {
      incl[0] = "N";
    }
    if (wp.itemStr("incl_it").equals("Y")) {
      incl[1] = "Y";
    } else {
      incl[1] = "N";
    }
    if (wp.itemStr("incl_ca").equals("Y")) {
      incl[2] = "Y";
    } else {
      incl[2] = "N";
    }
    if (wp.itemStr("incl_id").equals("Y")) {
      incl[3] = "Y";
    } else {
      incl[3] = "N";
    }
    if (wp.itemStr("incl_ao").equals("Y")) {
      incl[4] = "Y";
    } else {
      incl[4] = "N";
    }
    if (wp.itemStr("incl_ot").equals("Y")) {
      incl[5] = "Y";
    } else {
      incl[5] = "N";
    }
    itemEnameIncl = incl[0] + incl[1] + incl[2] + incl[3] + incl[4] + incl[5];
    itemEnameExcl = excl[0] + excl[1] + excl[2] + excl[3] + excl[4] + excl[5];
    if (wp.itemStr("key_type").equals("1")) {
      keyData = wp.itemStr("kk_group_code");
    }
    if (wp.itemStr("key_type").equals("2")) {
      keyData = wp.itemStr("kk_card_type");
    }
    if (wp.itemStr("key_type").equals("5")) {
      keyData = wp.itemStr("kk_group_code") + wp.itemStr("kk_card_type");
    }
    if (empty(keyData)) {
      keyData = wp.itemStr("key_data");
    }
    costAmt = wp.itemStr("cost_amt");
    if (empty(costAmt)) {
      costAmt = "0";
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from mkt_contri_parm_t "
          + "where item_no = ? and key_data=? and key_type=? and cost_month=? ";
      Object[] param = new Object[] {wp.itemStr("item_no"), keyData, wp.itemStr("key_type"),
          wp.itemStr("cost_month")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {
      // -other modify-
      sqlWhere = " where 1=1 and hex(rowid) = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {kk1Rowid, wp.modSeqno()};
      if (this.isOtherModify("mkt_contri_parm_t", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into mkt_contri_parm_t (" + " item_no, " + " input_type, " + " key_data, "
        + " key_type, " + " cost_amt, " + " item_ename_incl," + " item_ename_excl, "
        + " crt_date,crt_user,mod_time,mod_seqno " + " ) values ( " + " ?,'1',?,?,?,?,?,"
        + " ?,?,sysdate,?) ";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("item_no"), keyData, wp.itemStr("key_type"), costAmt,
        itemEnameIncl, itemEnameExcl, wp.sysDate, wp.loginUser, wp.modSeqno()};
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
    strSql = " update mkt_contri_parm_t set " + " cost_amt=?, " + " item_ename_incl=?, "
        + " item_ename_excl=?, " + " mod_time=sysdate, " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;

    Object[] param = new Object[] {costAmt, itemEnameIncl, itemEnameExcl, kk1Rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
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
    if (wp.itemStr("db_temp").equals("Y")) {
      strSql = "delete mkt_contri_parm_t " + sqlWhere;
    } else {
      strSql = "delete mkt_contri_parm " + sqlWhere;
    }
    Object[] param = new Object[] {kk1Rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
