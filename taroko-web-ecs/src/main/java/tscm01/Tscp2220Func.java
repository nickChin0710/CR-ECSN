/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 112-04-17  V1.00.01  Alex       修正寫入檔案名稱 PRTN.00600000                  *
******************************************************************************/
package tscm01;

import busi.FuncAction;

import taroko.base.CommString;

public class Tscp2220Func extends FuncAction {
  String lsBillDesc = "";
  private String isBatchNo = "";
  private int ilProcCnt = 0;

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    if (empty(isBatchNo)) {
      if (getBatchNo() != 1) {
        return rc;
      }
    }

    strSql = "select * from tsc_prtn_log" + " where 1=1 and rowid = ? and nvl(mod_seqno,0) = ? ";
    setRowId(1,varsStr("rowid"));
    setString(2,varsStr("mod_seqno"));
//    Object[] param = new Object[] { varsStr("rowid"), varsStr("mod_seqno")};	
    
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料不存在 or 已被異動");
      return rc;
    }
    if (this.colPos("|1|2|3|4", "close_reason") <= 0) {
      errmsg("結案理由碼錯誤");
      return rc;
    }
    strSql = " update tsc_prtn_log set" + " apr_flag ='Y'"
        + ", apr_date =to_char(sysdate,'yyyymmdd')" + ", apr_user =:mod_user"
        + ", mod_time =sysdate" + ", mod_pgm =:mod_pgm" + ", mod_seqno =mod_seqno+1" + " where 1=1 "
        + " and hex(rowid) =:rowid " + " and mod_seqno =:mod_seqno ";
    var2ParmStr("rowid");
    var2ParmNum("mod_seqno");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscp2220");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料不存在 or 已被異動");
      return rc;
    }

    insertBilSysexp();
    if (rc == -1) {
      return rc;
    }

    return rc;
  }

  void insertBilSysexp() {
    if (eqIgno(varsStr("close_reason"), "3") || eqIgno(varsStr("close_reason"), "4")) {
      return;
    }
    if (eqIgno(varsStr("close_reason"), "1")) {
      lsBillDesc = commString.left(
          "餘轉後信用卡加值" + commString.left(varsStr("traff_subname"), 10) + varsStr("place_subname"), 40);
      wp.log("desc=" + lsBillDesc);
    } else if (eqIgno(varsStr("close_reason"), "2")) {
      lsBillDesc = commString.left(
          "餘轉後信用卡減值" + commString.left(varsStr("traff_subname"), 10) + varsStr("place_subname"), 40);
    }
    selectCardNo();

    strSql = "insert into tsc_cgec_all (" + " batch_no ," + " seq_no ," + " card_no ,"
        + " tsc_card_no ," + " bill_type ," + " txn_code ," + " tsc_tx_code ," + " purchase_date ,"
        + " purchase_time ," + " mcht_no ," + " mcht_category ," + " mcht_chi_name ,"
        + " dest_amt ," + " dest_curr ," + " bill_desc ," + " traffic_cd ," + " traffic_abbr ,"
        + " addr_cd ," + " addr_abbr ," + " post_flag ," + " file_name ," + " online_mark ,"
        + " mod_time ," + " mod_user ," + " mod_pgm ," + " mod_seqno " + " ) values ("
        + " :batch_no ," + " :seq_no ," + " :card_no ," + " :tsc_card_no ," + " 'TSCC' ,"
        + " '05' ," + " '8309' ," + " :tran_date ," + " :tran_time ," + " 'EASY8004' ,"
        + " '4100' ," + " :mcht_chi_name ," + " :tran_amt ," + " '901' ," + " :bill_desc ,"
        + " :traff_code ," + " :traff_subname ," + " :place_code ," + " :place_subname ," + " 'N' ,"
        + " :file_name ," + " :online_mark ," + " sysdate ," + " :mod_user ," + " :mod_pgm ,"
        + " '1' " + " )";

    setString("batch_no", isBatchNo);
    var2ParmNum("seq_no");
    setString("card_no", colStr("card_no"));
    var2ParmStr("tsc_card_no");
    var2ParmStr("tran_date");
    var2ParmStr("tran_time");
    setString("mcht_chi_name", lsBillDesc);
    var2ParmNum("tran_amt");
    setString("bill_desc", lsBillDesc);
    var2ParmStr("traff_code");
    var2ParmStr("traff_subname");
    var2ParmStr("place_code");
    var2ParmStr("place_subname");
    setString("file_name", "PRTN.00600000." + varsStr("notify_date") + "01");
    var2ParmStr("online_mark");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscp2220");
    log("E:");
    this.sqlExec(strSql);
    log("F:" + sqlRowNum);
    if (sqlRowNum <= 0) {
      errmsg("Insert tsc_cgec_all error, " + getMsg());
      rc = -1;
    }
    return;
  }

  public int insertBilPostcntl() {
    if (empty(isBatchNo) || eqIgno(isBatchNo, null)) {
      errmsg("批號不可空白");
      rc = -1;
      return rc;
    }

    if (isBatchNo.length() != 14) {
      errmsg("資料批號錯誤~: " + isBatchNo);
      rc = -1;
      return rc;
    }

    if (checkBatchNo()) {
      errmsg("批號已存在: " + isBatchNo);
      rc = -1;
      return rc;
    }
    strSql = "insert into bil_postcntl (" + " batch_date , " + " batch_unit , " + " batch_seq , "
        + " batch_no , " + " tot_record , " + " tot_amt , " + " confirm_flag , " + " auth_flag , "
        + " confirm_flag_p , " + " apr_date , " + " apr_user , " + " this_close_date , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ("
        + " :batch_date , " + " :batch_unit , " + " :batch_seq , " + " :batch_no , " + " :ll_ok , "
        + " :tot_amt , " + " 'N' , " + " '' , " + " 'Y' , " + " to_char(sysdate,'yyyymmdd') , "
        + " :apr_user , " + " to_char(sysdate,'yyyymmdd') , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " '1' " + " )";

    setString("batch_date", isBatchNo.substring(0, 8));
    setString("batch_unit", isBatchNo.substring(8, 10));
    setString("batch_seq", isBatchNo.substring(10, 14));
    setString("batch_no", isBatchNo);
    var2ParmNum("ll_ok");
    var2ParmNum("tot_amt");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscp2220");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert bil_postcntl error, " + getMsg());
      rc = -1;
    }
    return rc;
  }

  int getBatchNo() {
    String lsBusiDate = this.businDate();

    if (empty(lsBusiDate)) {
      errmsg("無法取得 CGEC batch_no:營業日");
      return rc;
    }

    strSql = "select substr(to_char(nvl(max(batch_seq),0)+1,'0000'),2,4) as batch_seq"
        + " from bil_postcntl" + " where batch_unit = substrb('TSCC',1,2) and batch_date = ? "
        ;
    sqlSelect(strSql , new Object[] {lsBusiDate});
    if (sqlRowNum <= 0) {
      errmsg("無法取得 CGEC batch_no: 批號");
      return rc;
    }
    isBatchNo = lsBusiDate + "TS" + colStr("batch_seq");

    return rc;
  }

  void selectCardNo() {
    String sql1 = "select card_no " + " from tsc_card " + " where tsc_card_no =:tsc_card_no ";
    var2ParmStr("tsc_card_no");
    sqlSelect(sql1);
  }

  boolean checkBatchNo() {
    String sql1 = "select count(*) as db_cnt " + " from bil_postcntl " + " where batch_no =?";
    sqlSelect(sql1, new Object[] {isBatchNo});

    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

}
