/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-25  V1.00.00             program initial                            *
* 109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                                                    
******************************************************************************/

package dbcp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Dbcp0010Func extends FuncEdit {
  String cardCode = "";
  String digitalFlag = "";

  public Dbcp0010Func(TarokoCommon wr) {
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

      if (empty(wp.itemStr("kk_card_code"))) {
        errmsg("卡別不可空白!!!");
        return;
      }

      cardCode = wp.itemStr("kk_card_code");
      digitalFlag =
          !wp.itemStr("kk_digital_flag").equals("Y") ? "N" : wp.itemStr("kk_digital_flag");
    } else {
      cardCode = wp.itemStr("card_code");
      digitalFlag =
          !wp.itemStr("digital_flag").equals("Y") ? "N" : wp.itemStr("digital_flag");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from dbc_card_type where card_code = ? and digital_flag=?";
      Object[] param = new Object[] {cardCode, digitalFlag};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where card_code = ? and digital_flag=?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {cardCode, digitalFlag, wp.modSeqno()};
      isOtherModify("dbc_card_type", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into dbc_card_type (" + " card_code " + ", digital_flag " + ", card_type "
        + ", group_code " + ", unit_code " + ", source_code " + ", name " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {cardCode // 1
        , digitalFlag, wp.itemStr("card_type"), wp.itemStr("group_code"),
        wp.itemStr("unit_code"), wp.itemStr("source_code"), wp.itemStr("name"), wp.loginUser,
        wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql =
        "update dbc_card_type set " + "  card_type =? " + " ,group_code =? " + " ,unit_code =? "
            + " ,source_code =? " + " ,name =? " + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
            + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("card_type"), wp.itemStr("group_code"),
        wp.itemStr("unit_code"), wp.itemStr("source_code"), wp.itemStr("name"), wp.loginUser,
        wp.itemStr("mod_pgm"), cardCode, digitalFlag, wp.modSeqno()};
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
    strSql = "delete dbc_card_type " + sqlWhere;
    Object[] param = new Object[] {cardCode, digitalFlag, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
