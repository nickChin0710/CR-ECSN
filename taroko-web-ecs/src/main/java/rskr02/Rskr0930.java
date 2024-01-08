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

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0930 extends BaseAction implements InfacePdf {

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
      if (eqIgno(wp.respHtml, "rskr0930")) {
        wp.optionKey = wp.colStr(0, "ex_charge_user");
        dddwList("dddw_charge_user", "ptr_sys_idtab", "wf_id", "wf_id",
            "where wf_type ='RSKM0930_CHARGE' ");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("來電日期: 起迄錯誤");
      return;
    }

    String lsWhere = getWhereStr();
    setSqlParmNoClear(true);
    sum(lsWhere);

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

private String getWhereStr() {
	String lsWhere = "";
    lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "tel_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tel_date", "<=")
        + sqlCol(wp.itemStr("ex_decs_lvl"), "decs_level")
        + sqlCol(wp.itemStr("ex_charge_user"), "charge_user");

    if (wp.itemEq("ex_self", "Y")) {
      lsWhere += " and uf_nvl(self_flag,'N') = 'Y' ";
    } else if (wp.itemEq("ex_self", "N")) {
      lsWhere += " and uf_nvl(self_flag,'N') = 'N' ";
    }

    if (wp.itemEq("ex_temp_adj", "Y")) {
      lsWhere += " and uf_nvl(temp_adj_flag,'N') ='Y' ";
    } else if (wp.itemEq("ex_temp_adj", "N")) {
      lsWhere += " and uf_nvl(temp_adj_flag,'N') ='N' ";
    }

    if (wp.itemEq("ex_impo_cust", "Y")) {
      lsWhere += " and uf_nvl(impo_cust_flag,'N') ='Y' ";
    } else if (wp.itemEq("ex_impo_cust", "N")) {
      lsWhere += " and uf_nvl(impo_cust_flag,'N') = 'N' ";
    }

    if (wp.itemEq("ex_cond1", "Y"))
      lsWhere += " and uf_nvl(per_purch_flag,'N') = 'Y' ";
    if (wp.itemEq("ex_cond2", "Y"))
      lsWhere += " and uf_nvl(overpay_adj_flag,'N') = 'Y' ";
    if (wp.itemEq("ex_cond3", "Y"))
      lsWhere += " and uf_nvl(reserve_flag,'N') = 'Y' ";
    if (wp.itemEq("ex_cond4", "Y"))
      lsWhere += " and uf_nvl(adj2_flag,'N') = 'Y' ";
    if (wp.itemEq("ex_cond5", "Y"))
      lsWhere += " and uf_nvl(adj_corp_flag,'N') = 'Y' ";
    if (wp.itemEq("ex_cond6", "Y"))
      lsWhere += " and uf_nvl(oth_flag,'N') = 'Y' ";
	return lsWhere;
}

  void sum(String lsWhere) {
    String sql1 = " select " + " count(*) as tl_cnt , "
        + " sum(decode(audit_result,'初審未通過',1,0)) tl_nopass ,"
        + " sum(decode(audit_result,'審核後拒絕',1,0)) tl_reject ,"
        + " sum(decode(audit_result,'部分核准',1,'全額核准',1,0)) tl_pass ,"
        + " sum(decode(audit_result,'取消臨調',1,0)) tl_cancel " + " from rsk_credits_adj " + lsWhere;
    sqlSelect(sql1, null);

    if (sqlRowNum <= 0 || sqlNum("tl_cnt") == 0) {
      wp.colSet("tl_cnt", "" + 0);
      wp.colSet("tl_nopass", "" + 0);
      wp.colSet("tl_reject", "" + 0);
      wp.colSet("tl_pass", "" + 0);
      wp.colSet("tl_cancel", "" + 0);
    }

    wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
    wp.colSet("tl_nopass", "" + sqlNum("tl_nopass"));
    wp.colSet("tl_reject", "" + sqlNum("tl_reject"));
    wp.colSet("tl_pass", "" + sqlNum("tl_pass"));
    wp.colSet("tl_cancel", "" + sqlNum("tl_cancel"));

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " self_flag ," + " temp_adj_flag ," + " impo_cust_flag ," + " decs_level ,"
        + " charge_user ," + " count(*) db_tot_cnt ,"
        + " sum(decode(audit_result,'初審未通過',1,0)) db_nopass ,"
        + " sum(decode(audit_result,'審核後拒絕',1,0)) db_reject ,"
        + " sum(decode(audit_result,'部分核准',1,'全額核准',1,0)) db_pass ,"
        + " sum(decode(audit_result,'取消臨調',1,0)) db_cancel ";
    wp.daoTable = " rsk_credits_adj ";
    wp.whereOrder =
        " group by self_flag , temp_adj_flag , impo_cust_flag , decs_level , charge_user ";
    wp.pageCountSql =
        " select count from (select distinct self_flag , temp_adj_flag , impo_cust_flag , decs_level , charge_user from rsk_credits_adj "
            + wp.whereStr + ")";
    pageQuery();
    if (sqlRowNum <= 0) {
      wp.colSet("tl_cnt", "" + 0);
      wp.colSet("tl_nopass", "" + 0);
      wp.colSet("tl_reject", "" + 0);
      wp.colSet("tl_pass", "" + 0);
      wp.colSet("tl_cancel", "" + 0);
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
    wp.reportId = "Rskr0930";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "來電日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0930.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
