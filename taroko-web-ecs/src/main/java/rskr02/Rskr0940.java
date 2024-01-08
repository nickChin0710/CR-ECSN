/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                                                                     *    
******************************************************************************/
package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0940 extends BaseAction implements InfacePdf {

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("來電日期: 起迄錯誤");
      return;
    }
    String lsWhere = "";

    lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "tel_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tel_date", "<=");

    if (wp.itemEq("ex_self", "Y")) {
      lsWhere += " and uf_nvl(self_flag,'N') = 'Y' ";
    } else if (wp.itemEq("ex_self", "N")) {
      lsWhere += " and uf_nvl(self_flag,'N') = 'N' ";
    }

    if (wp.itemEq("ex_purch_flag", "Y"))
      lsWhere += " and uf_nvl(purch_flag,'x') ='Y' ";
    if (wp.itemEq("ex_over_lmt_flag", "Y"))
      lsWhere += " and uf_nvl(over_lmt_parm_flag,'x') ='Y' ";
    if (wp.itemEq("ex_oth_flag", "Y"))
      lsWhere += " and uf_nvl(oth_flag,'x') ='Y' ";

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " self_flag , " + " count(*) db_tot_cnt , "
        + " sum(decode(audit_result,'審核後拒絕',1,0)) as  db_reject , "
        + " sum(decode(position('核准',audit_result),0,0,1)) as db_pass , "
        + " sum(decode(audit_result,'取消臨調',1,0)) as db_cancel ";
    wp.daoTable = " rsk_credits_vd ";
    wp.whereOrder = " group by self_flag ";
    wp.pageCountSql = " select count(distinct self_flag) from rsk_credits_vd " + wp.whereStr;
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();
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
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "Rskr0940";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "來電日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0940.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
