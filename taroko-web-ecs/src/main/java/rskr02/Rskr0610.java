/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-24  V1.00.01   Justin         parameterize sql
******************************************************************************/
package rskr02;
/**
 * 2019-1225   JH    cond: +kind_flag='A'
 * 2019-0628:  JH    cond-check
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskr0610 extends BaseAction implements InfacePdf {
  String isCond = "", isReportName = "信用額度調整表";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskr0610")) {
        wp.optionKey = wp.colStr("ex_dept_no");
        dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
      }

    } catch (Exception ex) {
    }


  }

  @Override
  public void queryFunc() throws Exception {
    if (icondStrend("ex_date1", "ex_date2") == false) {
      alertErr2("日期起迄錯誤");
      return;
    }

    String lsWhere = " where A.log_type='1' and A.kind_flag='A' ";
    if (wp.itemEq("ex_log_mode", "1")) {
      lsWhere += " and A.log_mode='1' and A.apr_flag='Y'";
      isReportName = "信用額度調整表";
      isCond += "調整方式: 線上 ";
    } else if (wp.itemEq("ex_log_mode", "2")) {
      lsWhere += " and A.log_mode ='2'";
      isReportName = "信用額度調整表";
      isCond += "調整方式: 整批 ";
    } else {
      isReportName = "信用額度調整表";
      isCond += "調整方式: 全部 ";
    }

    if (wp.itemEq("ex_ask_mode", "1")) {
      lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.log_date", ">=")
          + sqlCol(wp.itemStr("ex_date2"), "A.log_date", "<=");
      isCond += " 調整日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_date2"));
    } else if (wp.itemEq("ex_ask_mode", "2")) {
      lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.apr_date", ">=")
          + sqlCol(wp.itemStr("ex_date2"), "A.apr_date", "<=");
      isCond += " 覆核日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_date2"));
    }

    if (wp.itemEq("ex_adj", "1") || wp.itemEq("ex_adj", "2")) {
      lsWhere += 
      String.format(" and A.adj_loc_flag = decode( '%s' ,A.adj_loc_flag, '%s' )", wp.itemStr("ex_adj"),wp.itemStr("ex_adj"));
//      setString(wp.itemStr("ex_adj"));
//      setString(wp.itemStr("ex_adj"));
    }

    if (wp.itemEq("ex_log_mode", "1")) {
      lsWhere += sqlCol(wp.itemStr("ex_adj_user"), "A.mod_user", "like%");
      lsWhere += sqlCol(wp.itemStr("ex_dept_no"), "B.usr_deptno");
    }



    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " A.mod_user ," + " uf_acno_key(A.acno_p_seqno) as acct_key ,"
        + " A.aft_loc_amt ," + " A.bef_loc_amt ," + " A.log_date ," + " A.log_reason ,"
        + " A.security_amt ," + " A.apr_user ," + " A.apr_date ," + " A.bef_loc_cash ,"
        + " A.aft_loc_cash ," + " A.emend_type ," + " A.acct_type ," + " A.id_p_seqno ,"
        + " A.corp_p_seqno ," + " A.adj_loc_flag ,"
        + " uf_acno_name(A.acno_p_seqno) as db_acno_name ,"
        + " decode(A.emend_type,'5',A.aft_loc_cash - A.bef_loc_cash,A.aft_loc_amt - A.bef_loc_amt) as wk_adj_amt ,"
        + " uf_tt_idtab('ADJ_REASON%',A.log_reason) as tt_log_reason , "
        + " '' as tt_mod_branch , "
        + " '' as tt_apr_branch "        
        ;
    wp.daoTable = " rsk_acnolog A left join sec_user B on A.mod_user = B.usr_id ";
    wp.whereOrder = " order by 1,2 ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();

  }

  void queryAfter() {
    String sql1 = " select A.al_amt , A.al_amt02 " + ", B.usr_amtlevel"
        + " from sec_amtlimit A join sec_user B on A.al_level=B.usr_amtlevel "
        + " where B.usr_id =? ";
    
    String sql2 = " select A.full_chi_name from gen_brn A left join sec_user B "
    		+ " on A.branch = B.bank_unitno where B.usr_id = ? ";
    
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));

      wp.logSql = false;
      
      sqlSelect(sql2,new Object[] {wp.colStr(ii,"mod_user")});
      if(sqlRowNum>0) {
    	  wp.colSet(ii,"tt_mod_branch", sqlStr("full_chi_name"));
      }
      
      sqlSelect(sql2,new Object[] {wp.colStr(ii,"apr_user")});
      if(sqlRowNum>0) {
    	  wp.colSet(ii,"tt_apr_branch", sqlStr("full_chi_name"));
      }
      
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "apr_user")});
      if (sqlRowNum <= 0)
        continue;

      if (eqIgno(wp.colStr(ii, "acct_type"), "02") || eqIgno(wp.colStr(ii, "acct_type"), "03")) {
        wp.colSet(ii, "security_amt", sqlNum("al_amt02"));
      } else {
        wp.colSet(ii, "security_amt", sqlNum("al_amt"));
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_ask_mode", "2");

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "Rskr0610";
    wp.pageRows = 9999;
    // if(wp.item_eq("ex_ask_mode", "1")){
    // is_cond = "調整日期:"+commString.ss_2ymd(wp.item_ss("ex_date1"))+" --
    // "+commString.ss_2ymd(wp.item_ss("ex_date2"));
    // } else if (wp.item_eq("ex_ask_mode", "2")){
    // is_cond = "覆核日期:"+commString.ss_2ymd(wp.item_ss("ex_date1"))+" --
    // "+commString.ss_2ymd(wp.item_ss("ex_date2"));
    // }
    queryFunc();
    wp.colSet("user_id", wp.loginUser);
    wp.colSet("cond1", isCond);
    wp.colSet("report_name", isReportName);
    TarokoPDF pdf = new TarokoPDF();
    pdf.pageCount =30;

    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0610.xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
