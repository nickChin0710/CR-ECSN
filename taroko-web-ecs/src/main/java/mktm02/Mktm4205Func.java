/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-06  V1.00.01  Amber      program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm4205Func extends FuncEdit {
  String kk1Rowid = "", keyData = "", keyType = "";
  int rankSeq = 0;

  public Mktm4205Func(TarokoCommon wr) {
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



    if (this.isAdd()) {

      if (wp.itemStr("group_code").equals("0000")) {
        keyType = "2";
        keyData = wp.itemStr("group_code") + wp.itemStr("kk_card_type");
      } else {
        keyType = "5";
        keyData = wp.itemStr("group_code") + wp.itemStr("kk_card_type");
      }

      if (keyData.length() < 6) {
        keyData = wp.itemStr("group_code") + wp.itemStr("card_type");
      }

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from mkt_contri_insu_t " + "where key_data=? and key_type=? ";
      Object[] param = new Object[] {keyData, wp.itemStr("key_type")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {

      if (wp.itemStr("group_code").equals("0000")) {
        keyType = "2";
        keyData = wp.itemStr("group_code") + wp.itemStr("card_type");
      } else {
        keyType = "5";
        keyData = wp.itemStr("group_code") + wp.itemStr("card_type");
      }

      // -other modify-
      sqlWhere = " where 1=1 and hex(rowid) = ?  and nvl(mod_seqno,0) = ?";
      Object[] param1 = new Object[] {kk1Rowid, wp.modSeqno()};
      if (this.isOtherModify("mkt_contri_insu_t", sqlWhere, param1)) {
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
    strSql = "insert into mkt_contri_insu_t (" + " key_data, " + " key_type, " + " rank_seq, "
        + " crt_date,crt_user,mod_user,mod_time,mod_pgm,mod_seqno " + " ) values ( "
        + " ?,?,?,?,?,?, " + " sysdate,?,?) ";
    // -set ?value-
    Object[] param = new Object[] {keyData, keyType, wp.itemNum("rank_seq"), wp.sysDate,
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), wp.modSeqno()};
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
    strSql = " update mkt_contri_insu_t set " + " key_data=?, " + " key_type=?, " + " rank_seq=?, "
        + " mod_user=?, " + " mod_time=sysdate, " + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;

    Object[] param = new Object[] {keyData, keyType, wp.itemNum("rank_seq"), wp.loginUser, kk1Rowid,
        wp.modSeqno()};
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
      strSql = "delete mkt_contri_insu_t " + sqlWhere;
    } else {
      strSql = "delete mkt_contri_insu " + sqlWhere;
    }
    Object[] param = new Object[] {kk1Rowid, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
