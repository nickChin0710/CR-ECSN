/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-09  V1.00.2   Alex        fix queryWhere                            *
* 108-11-25  V1.00.1   Alex        dddw add , code ->chinese                 *
* 109-04-28  V1.00.03  shiyuqi     updated for project coding standard       *
* 109-07-20  V1.00.04  sunny       fix bug,add cms_casetype.apr_flag ='Y'    *  
* 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                  
* 111-11-22  V1.00.01  Machao     頁面bug調整                                                   *    
******************************************************************************/
package cmsr02;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Cmsr6022 extends BaseQuery implements InfacePdf {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsr6022")) {
        wp.optionKey = wp.colStr("ex_casetype1");
        dddwList("d_dddw_casetype1", "CMS_CASETYPE", "case_id", "substr(case_desc,1,10)",
            "where 1=1 and case_type = '1' and apr_flag ='Y'");
        wp.optionKey = wp.colStr("ex_casetype2");
        dddwList("d_dddw_casetype2", "CMS_CASETYPE", "case_id", "substr(case_desc,1,10)",
            "where 1=1 and case_type = '1' and apr_flag ='Y'");
        wp.optionKey = wp.colStr("ex_user");
        dddwList("dddw_user", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_cms_date_s"), wp.itemStr("ex_cms_date_e")) == false) {
      alertErr2("統計期間起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " 
    	+ sqlCol(wp.itemStr("ex_cms_date_s"), "A.case_date", ">=")
        + sqlCol(wp.itemStr("ex_cms_date_e"), "A.case_date", "<=")
        + sqlCol(wp.itemStr("ex_user"), "A.case_user", "like%")
        + sqlBetween(wp.itemStr("ex_casetype1"), wp.itemStr("ex_casetype2"), "A.case_type");


    if (wp.itemEq("ex_type_a", "") && wp.itemEq("ex_type_b", "") && wp.itemEq("ex_type_c", "")
        && wp.itemEq("ex_type_d", "") && wp.itemEq("ex_type_e", "") && wp.itemEq("ex_type_f", "")
        && wp.itemEq("ex_type_g", "") && wp.itemEq("ex_type_h", "") && wp.itemEq("ex_type_i", "")
        && wp.itemEq("ex_type_z", "")) {

    } else {
      lsWhere += " and (1=2";

      if (wp.itemEq("ex_type_a", "Y"))
        lsWhere += " or A.case_type like 'A%' ";

      if (wp.itemEq("ex_type_b", "Y"))
        lsWhere += " or A.case_type like 'B%' ";

      if (wp.itemEq("ex_type_c", "Y"))
        lsWhere += " or A.case_type like 'C%' ";

      if (wp.itemEq("ex_type_d", "Y"))
        lsWhere += " or A.case_type like 'D%' ";

      if (wp.itemEq("ex_type_e", "Y"))
        lsWhere += " or A.case_type like 'E%' ";

      if (wp.itemEq("ex_type_f", "Y"))
        lsWhere += " or A.case_type like 'F%' ";

      if (wp.itemEq("ex_type_g", "Y"))
        lsWhere += " or A.case_type like 'G%' ";

      if (wp.itemEq("ex_type_h", "Y"))
        lsWhere += " or A.case_type like 'H%' ";

      if (wp.itemEq("ex_type_i", "Y"))
        lsWhere += " or A.case_type like 'I%' ";

      if (wp.itemEq("ex_type_z", "Y"))
        lsWhere += " or A.case_type like 'Z%' ";

      lsWhere += " )";
    }
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.card_no," + "A.case_type," + "A.case_idno,"
        + "replace(A.case_desc,' ','') as case_desc ,"
        + "replace(A.case_desc2,' ','') as case_desc2 ," + "A.case_user," + "A.finish_date,"
        + "A.reply_flag," + "A.eta_date," + "A.send_code," + "A.case_date," + "A.mod_time,"
        + "A.case_result ," + "B.proc_deptno," + "replace(B.proc_desc,' ','') as proc_desc,"
        + "replace(B.proc_desc2,' ','') as proc_desc2 ," + "uf_hi_idno(A.case_idno) as wk_id,"
        + "uf_hi_cardno(A.card_no) as wk_card_no," + "1 as db_sum , "
        + "(select case_desc from cms_casetype where case_id = A.case_type and case_type = '1' and apr_flag ='Y' ) as tt_case_type ,"
        + "A.case_user , "
        + "(select usr_cname from sec_user where usr_id =A.case_user) as tt_case_user , "
        + "B.proc_deptno ,"
        + "(select dept_name from ptr_dept_code where dept_code =B.proc_deptno) as tt_dept_name ";
    wp.daoTable =
        "CMS_CASEMASTER A left join CMS_CASEDETAIL B  on  ( A.case_date = B.case_date ) and  ( A.case_seqno = B.case_seqno ) ";
    wp.whereOrder = " order by case_date ";
    logSql();
    pageQuery(sqlParm.getConvParm());


    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {
    wp.logSql = false;
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      if (wp.colEmpty(ii, "tt_case_type") == false) {
        wp.colSet(ii, "wk_case_type",
            wp.colStr(ii, "case_type") + "_" + wp.colStr(ii, "tt_case_type"));
      } else {
        wp.colSet(ii, "wk_case_type", wp.colStr(ii, "case_type"));
      }

      if (wp.colEmpty(ii, "tt_case_user") == false) {
        wp.colSet(ii, "wk_case_user",
            wp.colStr(ii, "case_user") + "_" + wp.colStr(ii, "tt_case_user"));
      } else {
        wp.colSet(ii, "wk_case_user", wp.colStr(ii, "case_user"));
      }

      if (wp.colEmpty(ii, "tt_dept_name") == false) {
        wp.colSet(ii, "wk_proc_dept",
            wp.colStr(ii, "proc_deptno") + "_" + wp.colStr(ii, "tt_dept_name"));
      } else {
        wp.colSet(ii, "wk_proc_dept", wp.colStr(ii, "proc_deptno"));
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
  public void pdfPrint() throws Exception {
    wp.reportId = "cmsr6022";
    wp.pageRows = 9999;
    String cond1;

    cond1 = "統計期間: " + commString.strToYmd(wp.itemStr("ex_cms_date_s")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_cms_date_e"));
    if (wp.itemEmpty("ex_user") == false) {
      cond1 += " 受理人員: " + wp.itemStr("ex_user");
    }
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr6022.xlsx";
    pdf.pageCount = 26;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
