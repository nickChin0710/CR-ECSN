/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-13  V1.00.00  Andy Liu       program initial                        *
* 107-05-24  V1.00.01  Andy Liu       Update  UI,pg flow                     *
* 109-04-24  V1.00.02  shiyuqi       updated for project coding standard     *   
******************************************************************************/

package bilm01;
import busi.FuncEdit; 
import taroko.com.TarokoCommon;


public class Bilm0330Func extends FuncEdit {
  String mKkMchtNo = "";
  String mKkSeqNo = "";
  String mKkMchtType = "";
  String mKkMchtNoCpy = "";
  String mKkConfirmFlag = "";


  public Bilm0330Func(TarokoCommon wr) {
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
    // pk check
    // if (this.ib_add) {
    //
    // }else{
    // m_kk_mcht_no = wp.item_ss("mcht_no");
    // }

    mKkMchtNo = varsStr("aa_mcht_no");
    mKkMchtType = varsStr("aa_mcht_type");
    mKkSeqNo = varsStr("aa_seq_no");

    // if (this.isAdd()){
    // //檢查新增資料是否重複
    // String lsSql = "select count(*) as tot_cnt from bil_prod_copy_mas where mcht_no = ?";
    // Object[] param = new Object[] {m_kk_mcht_no};
    // sqlSelect(lsSql, param);
    // if (col_num("tot_cnt") > 0) {
    // errmsg("資料已存在，無法新增");
    // }
    // return;
    // }
    //
    // -other modify-
    // sql_where = " where mcht_no= ? "
    // + " and nvl(mod_seqno,0) = ? " ;
    // Object[] param = new Object[] {m_kk_mcht_no};
    // if (this.other_modify("bil_prod_copy_dtl", sql_where,param)) {
    // errmsg("請重新查詢 !");
    // return;
    // }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    strSql = "insert into bil_prod_copy_mas (" + " mcht_no " + " , seq_no" + " , confirm_flag"
        + " , copy_flag" + " , mod_user, mod_time , mod_pgm , mod_seqno " + " ) values ("
        + " ?,?,?,?, " + " ?,sysdate,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkMchtNo // 1
        , mKkSeqNo, "N", "N", wp.loginUser, wp.itemStr("mod_pgm")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    mKkMchtNo = wp.itemStr("mcht_no1");
    mKkSeqNo = wp.itemStr("seq_no");

    strSql = "delete bil_prod_copy_dtl " + "where 1=1 " + "and mcht_no =:mcht_no "
        + "and seq_no =:seq_no ";
    setString("mcht_no", mKkMchtNo);
    setString("seq_no", mKkSeqNo);
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      return rc;
    }

    strSql = "delete bil_prod_copy_mas " + "where 1=1 " + "and mcht_no =:mcht_no "
        + "and seq_no =:seq_no";
    setString("mcht_no", mKkMchtNo);
    setString("seq_no", mKkSeqNo);
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      return rc;
    }
    return rc;
  }
}
