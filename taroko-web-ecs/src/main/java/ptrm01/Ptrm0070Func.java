/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-05-27  V1.00.02  Wilson     mCardNoteJcic補齊                                                                           *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0070Func extends FuncEdit {
  String cardType = "";// kk2 = "";
  String mCardNoteJcic = "";

  public Ptrm0070Func(TarokoCommon wr) {
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
      cardType = wp.itemStr("kk_card_type");
      if (cardType.length() < 2) {
        errmsg("卡種代碼 有誤");
        return;
      }

    } else {
      cardType = wp.itemStr("card_type");
    }
    String lsCardNote = wp.itemStr("card_note");
    if (lsCardNote.equals("C")) {
      mCardNoteJcic = "R";
    }
    if (lsCardNote.equals("S")) {
      mCardNoteJcic = "E";
    }
    if (lsCardNote.equals("I")) {
      mCardNoteJcic = "T";
    }
    if (lsCardNote.equals("G")) {
        mCardNoteJcic = "G";
    }
    if (lsCardNote.equals("P")) {
        mCardNoteJcic = "P";
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from Ptr_card_type where card_type = ?";
      Object[] param = new Object[] {cardType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;

    }

    // ddd(this.actionCode+", kk1="+kk1+", mod_seqno="+wp.mod_seqno());

    // -other modify-
    sqlWhere = " where card_type= ? " + " and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {cardType, wp.modSeqno()};
    if (this.isOtherModify("Ptr_card_type", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }


  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql =
        "insert into Ptr_card_type (" + " card_type, " + " name, " + " card_note," + " rds_pcard,"
            + " card_note_jcic," + " crt_date, " + " crt_user, " + " mod_pgm, " + " mod_seqno"
            + " ) values (" + " ?,?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {cardType // card_type
        , wp.itemStr("name"), wp.itemStr("card_note"), wp.itemStr("rds_pcard"), mCardNoteJcic
        // , wp.item_ss("card_note_jcic")
        // , wp.item_ss("top_card_flag")
        // , wp.item_ss("neg_card_type")
        // , wp.item_ss("out_going_type")
        // , wp.item_ss("sort_type")
        , wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update Ptr_card_type set " + " name =?, " + " card_note =?, " + " rds_pcard =?, "
        + " card_note_jcic =?, " + " mod_user =?, " + " mod_time=sysdate, " + " mod_pgm =?, "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("name"), wp.itemStr("card_note"),
        wp.itemStr("rds_pcard"), mCardNoteJcic,
        // wp.item_ss("card_note_jcic"),
        // wp.item_ss("top_card_flag"),
        // wp.item_ss("neg_card_type"),
        // wp.item_ss("out_going_type"),
        // wp.item_ss("sort_type"),
        wp.loginUser, wp.itemStr("mod_pgm"), cardType, wp.modSeqno()};
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
    strSql = "delete ptr_card_type " + sqlWhere;
    // ddd("del-sql="+is_sql);
    Object[] param = new Object[] {cardType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
