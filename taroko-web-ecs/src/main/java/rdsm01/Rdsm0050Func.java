/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-16  V1.00.01  ryan       program initial                            *
* 109-04-22  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                 *   
* 110/1/4    V1.00.04  yanghan       修改了變量名稱和方法名稱                                                                 *
* 110/1/12   V1.00.05  tanwei        bug修改，新增outstanding_cond字段                                *
******************************************************************************/
package rdsm01;

import busi.FuncAction;

public class Rdsm0050Func extends FuncAction {
  String projNo = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      projNo = wp.itemStr("kk_proj_no");
    } else {
      projNo = wp.itemStr("proj_no");
    }

    if (empty(projNo)) {
      errmsg("專案代號不可空白");
      return;
    }

    if (this.ibDelete)
      return;

    // -首年-
    if (!eqIgno(wp.itemNvl("fst_cond", "N"), "Y") && !eqIgno(wp.itemNvl("lst_cond", "N"), "Y")
        && !eqIgno(wp.itemNvl("cur_cond", "N"), "Y")) {
      errmsg("首年~, 非首年~ 之條件至少要選一項");
      return;
    }

    int liMm = 0;
    String tmpStr = "";
    if (eqIgno(wp.itemNvl("fst_cond", "N"), "Y")) {
      liMm = (int) wp.itemNum("fst_mm");
      if (liMm <= 0 || liMm > 12) {
        errmsg("A.近 n 月消費月數 須為 01..12");
        return;
      }
      tmpStr = wp.itemNvl("fst_acct_code_bl", "N") + wp.itemNvl("fst_acct_code_it", "N")
          + wp.itemNvl("fst_acct_code_ca", "N") + wp.itemNvl("fst_acct_code_ot", "N")
          + wp.itemNvl("fst_acct_code_id", "N") + wp.itemNvl("fst_acct_code_ao", "N");

      if (pos(tmpStr, "Y") < 0) {
        errmsg("A.消費本金科目 至少要點選一項");
        return;
      }

      if (eqIgno(wp.itemNvl("fst_amt_cond", "N"), "N")
          && eqIgno(wp.itemNvl("fst_row_cond", "N"), "N")) {
        errmsg("A.累積消費金額/筆數 至少要選一項");
        return;
      }

    }

    // -非首年:最早流通卡非今年-
    liMm = 0;
    tmpStr = "";
    if (eqIgno(wp.itemNvl("lst_cond", "N"), "Y")) {
      liMm = (int) wp.itemNum("lst_mm");
      if (liMm <= 0 || liMm > 12) {
        errmsg("B.近 n 月消費月數 須為 01..12");
        return;
      }

      tmpStr = wp.itemNvl("lst_acct_code_bl", "N") + wp.itemNvl("lst_acct_code_it", "N")
          + wp.itemNvl("lst_acct_code_ca", "N") + wp.itemNvl("lst_acct_code_ot", "N")
          + wp.itemNvl("lst_acct_code_id", "N") + wp.itemNvl("lst_acct_code_ao", "N");
      if (pos(tmpStr, "Y") < 0) {
        errmsg("B.消費本金科目 至少要點選一項");
        return;
      }

      if (eqIgno(wp.itemNvl("lst_amt_cond", "N"), "N")
          && eqIgno(wp.itemNvl("lst_row_cond", "N"), "N")) {
        errmsg("B.累積消費金額/筆數 至少要選一項");
        return;
      }

    }

    // -非首年:最早流通卡非今年-
    liMm = 0;
    tmpStr = "";
    if (eqIgno(wp.itemNvl("cur_cond", "N"), "Y")) {
      liMm = (int) wp.itemNum("cur_mm");
      if (liMm <= 0 || liMm > 12) {
        errmsg("C.近 n 月消費月數 須為 01..12");
        return;
      }

      tmpStr = wp.itemNvl("cur_acct_code_bl", "N") + wp.itemNvl("cur_acct_code_it", "N")
          + wp.itemNvl("cur_acct_code_ca", "N") + wp.itemNvl("cur_acct_code_ot", "N")
          + wp.itemNvl("cur_acct_code_id", "N") + wp.itemNvl("cur_acct_code_ao", "N");
      if (pos(tmpStr, "Y") < 0) {
        errmsg("C.消費本金科目 至少要點選一項");
        return;
      }

      if (eqIgno(wp.itemNvl("cur_amt_cond", "N"), "N")
          && eqIgno(wp.itemNvl("cur_row_cond", "N"), "N")) {
        errmsg("C.累積消費金額/筆數 至少要選一項");
        return;
      }

    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into cms_roadparm2 ( " + " proj_no , " // 1
        + " proj_desc , " + " valid_end_date , " + " amt_sum_flag , " + " fst_cond , " // 5
        + " fst_mm , " + " fst_acct_code_bl , " + " fst_acct_code_it , " + " fst_acct_code_ca , "
        + " fst_acct_code_id , " // 10
        + " outstanding_cond , "
        + " fst_acct_code_ao , " + " fst_acct_code_ot , " + " fst_acct_code_it_flag , "
        + " fst_one_low_amt , " + " fst_amt_cond , " // 15
        + " fst_purch_amt , " + " fst_row_cond , " + " fst_purch_row , " + " lst_cond , "
        + " lst_tol_amt , " // 20
        + " lst_mm , " + " lst_acct_code_bl , " + " lst_acct_code_it , " + " lst_acct_code_ca , "
        + " lst_acct_code_id , " // 25   
        + " lst_acct_code_ao , " + " lst_acct_code_ot , " + " lst_acct_code_it_flag , "
        + " lst_one_low_amt , " + " lst_amt_cond , " // 30
        + " lst_purch_amt , " + " lst_row_cond , " + " lst_purch_row , " + " cur_cond , "
        + " cur_mm , " // 35
        + " cur_acct_code_bl , " + " cur_acct_code_it , " + " cur_acct_code_ca , "
        + " cur_acct_code_id , " + " cur_acct_code_ao , " // 40
        + " cur_acct_code_ot , " + " cur_acct_code_it_flag , " + " cur_one_low_amt , "
        + " cur_amt_cond , " + " cur_purch_amt , " // 45
        + " cur_row_cond , " + " cur_purch_row , " + " fst_mcht , " + " lst_mcht , "
        + " cur_mcht , " // 50
        + " crt_user , " + " crt_date , " + " apr_flag , " + " apr_date , " + " apr_user , " // 55
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno, " // 59
        + " fst_mcht_gp , " + " lst_mcht_gp , " + " cur_mcht_gp  " // 62
        + " ) values ( " + " :kk1 , " // 1
        + " :proj_desc , " + " :valid_end_date , " + " :amt_sum_flag , " + " :fst_cond , " // 5
        + " :fst_mm , " + " :fst_acct_code_bl , " + " :fst_acct_code_it , "
        + " :fst_acct_code_ca , " + " :fst_acct_code_id , " // 10
        + " :outstanding_cond , "
        + " :fst_acct_code_ao , " + " :fst_acct_code_ot , " + " 'N' , " + " :fst_one_low_amt , "
        + " :fst_amt_cond , " // 15
        + " :fst_purch_amt , " + " :fst_row_cond , " + " :fst_purch_row , " + " :lst_cond , "
        + " :lst_tol_amt , " // 20
        + " :lst_mm , " + " :lst_acct_code_bl , " + " :lst_acct_code_it , "
        + " :lst_acct_code_ca , " + " :lst_acct_code_id , " // 25
        + " :lst_acct_code_ao , " + " :lst_acct_code_ot , " + " 'N' , " + " :lst_one_low_amt , "
        + " :lst_amt_cond , " // 30
        + " :lst_purch_amt , " + " :lst_row_cond , " + " :lst_purch_row , " + " :cur_cond , "
        + " :cur_mm , " // 35
        + " :cur_acct_code_bl , " + " :cur_acct_code_it , " + " :cur_acct_code_ca , "
        + " :cur_acct_code_id , " + " :cur_acct_code_ao , " // 40
        + " :cur_acct_code_ot , " + " 'N' , " + " :cur_one_low_amt , " + " :cur_amt_cond , "
        + " :cur_purch_amt , " // 45
        + " :cur_row_cond , " + " :cur_purch_row , " + " :fst_mcht , " + " :lst_mcht , "
        + " :cur_mcht , " // 50
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " 'Y' , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " // 55
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 ," // 59
        + " :fst_mcht_gp , " + " :lst_mcht_gp , " + " :cur_mcht_gp  " // 62
        + " )";

    setString("kk1", projNo);
    item2ParmStr("proj_desc");
    item2ParmStr("valid_end_date");
    item2ParmNvl("amt_sum_flag", "1");
    item2ParmNvl("fst_cond", "N");
    item2ParmNum("fst_mm");
    item2ParmNvl("fst_acct_code_bl", "N");
    item2ParmNvl("fst_acct_code_it", "N");
    item2ParmNvl("fst_acct_code_ca", "N");
    item2ParmNvl("fst_acct_code_id", "N");
    item2ParmNvl("outstanding_cond", "N");
    item2ParmNvl("fst_acct_code_ao", "N");
    item2ParmNvl("fst_acct_code_ot", "N");
    item2ParmNum("fst_one_low_amt");
    item2ParmNvl("fst_amt_cond", "N");
    item2ParmNum("fst_purch_amt");
    item2ParmNvl("fst_row_cond", "N");
    item2ParmNum("fst_purch_row");
    item2ParmNvl("lst_cond", "N");
    item2ParmNum("lst_tol_amt");
    item2ParmNum("lst_mm");
    item2ParmNvl("lst_acct_code_bl", "N");
    item2ParmNvl("lst_acct_code_it", "N");
    item2ParmNvl("lst_acct_code_ca", "N");
    item2ParmNvl("lst_acct_code_id", "N");
    item2ParmNvl("lst_acct_code_ao", "N");
    item2ParmNvl("lst_acct_code_ot", "N");
    item2ParmNum("lst_one_low_amt");
    item2ParmNvl("lst_amt_cond", "N");
    item2ParmNum("lst_purch_amt");
    item2ParmNvl("lst_row_cond", "N");
    item2ParmNum("lst_purch_row");
    item2ParmNvl("cur_cond", "N");
    item2ParmNum("cur_mm");
    item2ParmNvl("cur_acct_code_bl", "N");
    item2ParmNvl("cur_acct_code_it", "N");
    item2ParmNvl("cur_acct_code_ca", "N");
    item2ParmNvl("cur_acct_code_id", "N");
    item2ParmNvl("cur_acct_code_ao", "N");
    item2ParmNvl("cur_acct_code_ot", "N");
    item2ParmNum("cur_one_low_amt");
    item2ParmNvl("cur_amt_cond", "N");
    item2ParmNum("cur_purch_amt");
    item2ParmNvl("cur_row_cond", "N");
    item2ParmNum("cur_purch_row");
    item2ParmNvl("fst_mcht", "0");
    item2ParmNvl("lst_mcht", "0");
    item2ParmNvl("cur_mcht", "0");
    item2ParmNvl("fst_mcht_gp", "0");
    item2ParmNvl("lst_mcht_gp", "0");
    item2ParmNvl("cur_mcht_gp", "0");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rdsm0050");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_roadparm2_bn_data error !");
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update cms_roadparm2 set " + " proj_desc =:proj_desc ,"
        + " valid_end_date =:valid_end_date ," + " amt_sum_flag =:amt_sum_flag ,"
        + " fst_cond =:fst_cond ," + " fst_mm =:fst_mm ," + " fst_acct_code_bl =:fst_acct_code_bl ,"
        + " fst_acct_code_it =:fst_acct_code_it ," + " fst_acct_code_ca =:fst_acct_code_ca ,"
        + " fst_acct_code_id =:fst_acct_code_id ," + " fst_acct_code_ao =:fst_acct_code_ao ,"
        + " fst_acct_code_ot =:fst_acct_code_ot ," + " outstanding_cond =:outstanding_cond ,"
        + " fst_acct_code_it_flag =:fst_acct_code_it_flag ,"
        + " fst_one_low_amt =:fst_one_low_amt ," + " fst_amt_cond =:fst_amt_cond ,"
        + " fst_purch_amt =:fst_purch_amt ," + " fst_row_cond =:fst_row_cond ,"
        + " fst_purch_row =:fst_purch_row ," + " lst_cond =:lst_cond ,"
        + " lst_tol_amt =:lst_tol_amt ," + " lst_mm =:lst_mm ,"
        + " lst_acct_code_bl =:lst_acct_code_bl ," + " lst_acct_code_it =:lst_acct_code_it ,"
        + " lst_acct_code_ca =:lst_acct_code_ca ," + " lst_acct_code_id =:lst_acct_code_id ,"
        + " lst_acct_code_ao =:lst_acct_code_ao ," + " lst_acct_code_ot =:lst_acct_code_ot ,"
        + " lst_acct_code_it_flag =:lst_acct_code_it_flag ,"
        + " lst_one_low_amt =:lst_one_low_amt ," + " lst_amt_cond =:lst_amt_cond ,"
        + " lst_purch_amt =:lst_purch_amt ," + " lst_row_cond =:lst_row_cond ,"
        + " lst_purch_row =:lst_purch_row ," + " cur_cond =:cur_cond ," + " cur_mm =:cur_mm ,"
        + " cur_acct_code_bl =:cur_acct_code_bl ," + " cur_acct_code_it =:cur_acct_code_it ,"
        + " cur_acct_code_ca =:cur_acct_code_ca ," + " cur_acct_code_id =:cur_acct_code_id ,"
        + " cur_acct_code_ao =:cur_acct_code_ao ," + " cur_acct_code_ot =:cur_acct_code_ot ,"
        + " cur_acct_code_it_flag =:cur_acct_code_it_flag ,"
        + " cur_one_low_amt =:cur_one_low_amt ," + " cur_amt_cond =:cur_amt_cond ,"
        + " cur_purch_amt =:cur_purch_amt ," + " cur_row_cond =:cur_row_cond ,"
        + " cur_purch_row =:cur_purch_row ," + " fst_mcht =:fst_mcht ," + " lst_mcht =:lst_mcht ,"
        + " cur_mcht =:cur_mcht ," + " fst_mcht_gp =:fst_mcht_gp ," + " lst_mcht_gp =:lst_mcht_gp ,"
        + " cur_mcht_gp =:cur_mcht_gp ," + " apr_flag ='Y' ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where proj_no =:kk1 ";

    item2ParmStr("proj_desc");
    item2ParmStr("valid_end_date");
    item2ParmNvl("amt_sum_flag", "1");
    item2ParmNvl("fst_cond", "N");
    item2ParmNum("fst_mm");
    item2ParmNvl("fst_acct_code_bl", "N");
    item2ParmNvl("fst_acct_code_it", "N");
    item2ParmNvl("fst_acct_code_ca", "N");
    item2ParmNvl("fst_acct_code_id", "N");
    item2ParmNvl("fst_acct_code_ao", "N");
    item2ParmNvl("fst_acct_code_ot", "N");
    item2ParmNvl("outstanding_cond", "N");
    item2ParmNvl("fst_acct_code_it_flag", "N");
    item2ParmNum("fst_one_low_amt");
    item2ParmNvl("fst_amt_cond", "N");
    item2ParmNum("fst_purch_amt");
    item2ParmNvl("fst_row_cond", "N");
    item2ParmNum("fst_purch_row");
    item2ParmNvl("lst_cond", "N");
    item2ParmNum("lst_tol_amt");
    item2ParmNum("lst_mm");
    item2ParmNvl("lst_acct_code_bl", "N");
    item2ParmNvl("lst_acct_code_it", "N");
    item2ParmNvl("lst_acct_code_ca", "N");
    item2ParmNvl("lst_acct_code_id", "N");
    item2ParmNvl("lst_acct_code_ao", "N");
    item2ParmNvl("lst_acct_code_ot", "N");
    item2ParmNvl("lst_acct_code_it_flag", "N");
    item2ParmNum("lst_one_low_amt");
    item2ParmNvl("lst_amt_cond", "N");
    item2ParmNum("lst_purch_amt");
    item2ParmNvl("lst_row_cond", "N");
    item2ParmNum("lst_purch_row");
    item2ParmNvl("cur_cond", "N");
    item2ParmNum("cur_mm");
    item2ParmNvl("cur_acct_code_bl", "N");
    item2ParmNvl("cur_acct_code_it", "N");
    item2ParmNvl("cur_acct_code_ca", "N");
    item2ParmNvl("cur_acct_code_id", "N");
    item2ParmNvl("cur_acct_code_ao", "N");
    item2ParmNvl("cur_acct_code_ot", "N");
    item2ParmNvl("cur_acct_code_it_flag", "N");
    item2ParmNum("cur_one_low_amt");
    item2ParmNvl("cur_amt_cond", "N");
    item2ParmNum("cur_purch_amt");
    item2ParmNvl("cur_row_cond", "N");
    item2ParmNum("cur_purch_row");
    item2ParmNvl("fst_mcht", "0");
    item2ParmNvl("lst_mcht", "0");
    item2ParmNvl("cur_mcht", "0");
    item2ParmNvl("fst_mcht_gp", "0");
    item2ParmNvl("lst_mcht_gp", "0");
    item2ParmNvl("cur_mcht_gp", "0");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rdsm0050");
    setString("kk1", projNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cms_roadparm2 error !");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete cms_roadparm2 where rowid =:rowid ";
    setRowId("rowid", wp.itemStr("rowid"));
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete cms_roadparm2 error ");
    }

    deleteAllCard();
    if (rc != 1)
      return rc;
    deleteAllMcht();
    if (rc != 1)
      return rc;
    return rc;
  }

  public int deleteAllCard() {
    msgOK();
    strSql = " delete cms_roadparm2_dtl where proj_no =:kk1 ";
    setString("kk1", projNo);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_roadparm2_dtl error !");
    } else
      rc = 1;
    return rc;
  }

  public int deleteAllMcht() {
    msgOK();
    strSql = " delete cms_roadparm2_bn_data where proj_no =:kk1 ";
    setString("kk1", projNo);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_roadparm2_bn_data error !");
    } else
      rc = 1;
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int deleteMchtData() {
    msgOK();
    strSql = " delete cms_roadparm2_bn_data " + " where proj_no =:proj_no "
        + " and data_type =:data_type " + " and type_desc = '特店代號' ";
    var2ParmStr("proj_no");
    var2ParmStr("data_type");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_roadparm2_bn_data error !");
    } else
      rc = 1;

    return rc;

  }

  public int insertMchtData() {
    msgOK();
    strSql = " insert into cms_roadparm2_bn_data ( " + " proj_no ," + " data_type ,"
        + " data_code ," + " data_code2 ," + " apr_flag ," + " type_desc ," + " mod_time ,"
        + " mod_pgm " + " ) values ( " + " :proj_no ," + " :data_type ," + " :data_code ," + " '' ,"
        + " 'Y' ," + " :type_desc ," + " sysdate ," + " :mod_pgm " + " ) ";
    var2ParmStr("proj_no");
    var2ParmStr("data_type");
    var2ParmStr("data_code");
    setString("type_desc", "特店代號");
    setString("mod_pgm", "rdsm0050");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_roadparm2_bn_data error !");
    }
    System.out.println("rc:" + rc);
    return rc;
  }


  public int deleteMchtgpData() {
    msgOK();
    strSql = " delete cms_roadparm2_bn_data " + " where proj_no =:proj_no "
        + " and data_type =:data_type " + " and type_desc = '特店群組' ";
    var2ParmStr("proj_no");
    var2ParmStr("data_type");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_roadparm2_bn_data error !");
    } else
      rc = 1;

    return rc;

  }

  public int insertMchtgpData() {
    msgOK();
    strSql = " insert into cms_roadparm2_bn_data ( " + " proj_no ," + " data_type ,"
        + " data_code ," + " data_code2 ," + " apr_flag ," + " type_desc ," + " mod_time ,"
        + " mod_pgm " + " ) values ( " + " :proj_no ," + " :data_type ," + " :data_code ," + " '' ,"
        + " 'Y' ," + " :type_desc ," + " sysdate ," + " :mod_pgm " + " ) ";
    var2ParmStr("proj_no");
    var2ParmStr("data_type");
    var2ParmStr("data_code");
    setString("type_desc", "特店群組");
    setString("mod_pgm", "rdsm0050");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_roadparm2_bn_data error !");
    }
    System.out.println("rc:" + rc);
    return rc;
  }

  public int deleteCardData() {
    msgOK();
    strSql = " delete cms_roadparm2_dtl where proj_no =:proj_no ";
    var2ParmStr("proj_no");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_roadparm2_dtl error ! proj_no = " + varsStr("proj_no"));
    } else
      rc = 1;

    return rc;
  }

  public int insertCardData() {
    msgOK();
    strSql = " insert into cms_roadparm2_dtl ( " + " proj_no , " + " acct_type , " + " card_type , "
        + " group_code , " + " corp_no , " + " mod_time , " + " mod_pgm " + " ) values ( "
        + " :proj_no , " + " :acct_type , " + " :card_type , " + " :group_code , " + " :corp_no , "
        + " sysdate , " + " :mod_pgm " + " ) ";
    var2ParmStr("proj_no");
    var2ParmStr("acct_type");
    var2ParmStr("card_type");
    var2ParmStr("group_code");
    var2ParmStr("corp_no");
    setString("mod_pgm", "rdsm0050");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_roadparm2_dtl error ! ");
    }

    return rc;
  }

}
