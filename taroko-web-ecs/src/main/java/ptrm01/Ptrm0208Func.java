/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-05  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0208Func extends FuncEdit {
  String mKkCorpNo = "";
  String mKkCardType = "";
  String mDbCorpPSeqno = "";

  public Ptrm0208Func(TarokoCommon wr) {
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
      mKkCorpNo = wp.itemStr("kk_corp_no");
      mKkCardType = wp.itemStr("kk_card_type");

    } else {
      // item_kk("data_k1");
      mKkCorpNo = wp.itemStr("corp_no");
      mKkCardType = wp.itemStr("card_type");
    }
    strSql = "select corp_p_seqno from crd_corp where corp_no=?";
    Object[] param = new Object[] {mKkCorpNo};
    sqlSelect(strSql, param);
    mDbCorpPSeqno = colStr("corp_p_seqno");

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from Ptr_corp_fee where corp_p_seqno = ?  and  card_type= ?";
      Object[] param1 = new Object[] {mDbCorpPSeqno, mKkCardType};
      sqlSelect(lsSql, param1);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;
    }

    // -other modify-
    sqlWhere = " where corp_p_seqno= ?  and  card_type= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param1 = new Object[] {mDbCorpPSeqno, mKkCardType, wp.modSeqno()};
    if (this.isOtherModify("Ptr_corp_fee", sqlWhere, param1)) {
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

    strSql = "insert into Ptr_corp_fee (" + " corp_p_seqno, " + " corp_no, " + " card_type, "
        + " first_fee_amt," + " other_fee_amt," + " crt_date, " + " crt_user, " + " mod_pgm, "
        + " mod_seqno" + " ) values (" + " ?,?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {mDbCorpPSeqno, mKkCorpNo, mKkCardType,
        wp.itemStr("first_fee_amt").equals("") ? 0 : wp.itemStr("first_fee_amt"),
        wp.itemStr("other_fee_amt").equals("") ? 0 : wp.itemStr("other_fee_amt"), wp.loginUser,
        wp.itemStr("mod_pgm")};
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

    strSql = "update Ptr_corp_fee set " + "   first_fee_amt =? " + " , other_fee_amt =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param =
        new Object[] {wp.itemStr("first_fee_amt").equals("") ? 0 : wp.itemStr("first_fee_amt"),
            wp.itemStr("other_fee_amt").equals("") ? 0 : wp.itemStr("other_fee_amt"), wp.loginUser,
            wp.itemStr("mod_pgm"), mDbCorpPSeqno, mKkCardType, wp.modSeqno()};
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
    strSql = "delete Ptr_corp_fee " + sqlWhere;
    Object[] param = new Object[] {mDbCorpPSeqno, mKkCardType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
