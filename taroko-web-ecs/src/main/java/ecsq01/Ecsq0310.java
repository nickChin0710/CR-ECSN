
/*****************************************************************************
*                                                                            *
*SMODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ecsq01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ecsq0310 extends BaseAction implements InfacePdf {
  boolean isPrint = false;

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
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

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_crt_user") && wp.itemEmpty("ex_batch_no")) {
      alertErr2("日期(起),產生經辦,批號 不可同時空白");
      return;
    }

    if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("產生日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_user"), "crt_user")
        + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
        + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "crt_date");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.daoTable = " ecs_addr_label ";
    wp.selectSQL = " crt_date , " + " crt_user , " + " crt_pgm , " + " batch_no , " + " crt_desc , "
        + " count(*) as db_cnt ";
    wp.whereOrder =
        " group by crt_date, crt_user, crt_pgm, batch_no, crt_desc order by crt_date, crt_user, batch_no ";
    wp.pageCountSql =
        " select count(*) from (select distinct crt_date, crt_user, crt_pgm, batch_no, crt_desc from ecs_addr_label "
            + wp.whereStr + ") ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(0);

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
    wp.reportId = "ecsq0310";
    dataPrint();
    wp.pageRows = 9999;
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "ecsq0310.xlsx";
    pdf.pageCount = 1;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  void dataPrint() {

    boolean lsLeftUp = true;
    boolean lsRightUp = true;
    boolean lsLeftDown = true;
    boolean lsRightDown = true;
    boolean lsSex = true;

    String[] lsCrtDate = wp.itemBuff("crt_date");
    String[] lsCrtUser = wp.itemBuff("crt_user");
    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] lsCrtDesc = wp.itemBuff("crt_desc");
    String[] aaOpt = wp.itemBuff("opt");

    if (eqIgno(wp.itemNvl("ex_print_leftup", "N"), "N"))
      lsLeftUp = false;
    if (eqIgno(wp.itemNvl("ex_print_rightup", "N"), "N"))
      lsRightUp = false;
    if (eqIgno(wp.itemNvl("ex_print_leftdown", "N"), "N"))
      lsLeftDown = false;
    if (eqIgno(wp.itemNvl("ex_print_rightdown", "N"), "N"))
      lsRightDown = false;
    if (eqIgno(wp.itemNvl("ex_print_sex", "N"), "N"))
      lsSex = false;

    int rows = 0;
    String sql1 = " select " + " addr_zip , " + " addr_1 , " + " addr_2 , " + " addr_3 , "
        + " addr_4 , " + " addr_14 , " + " addr_5 , " + " chi_name , " + " idno_sex , "
        + " remark_1 , " + " remark_2 , " + " remark_3 , " + " remark_4 " + " from ecs_addr_label "
        + " where crt_date = ? " + " and crt_user = ? " + " and batch_no = ? "
        + " and crt_desc = ? " + " order by batch_seq ";
    int cnt = -1, page = 0;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      cnt = optToIndex(aaOpt[ii]);
      if (cnt < 0)
        continue;

      sqlSelect(sql1,
          new Object[] {lsCrtDate[cnt], lsCrtUser[cnt], lsBatchNo[cnt], lsCrtDesc[ii]});

      rows = sqlRowNum;
      if (rows % 16 != 0) {
        page = (rows / 16) + 1;
      } else {
        page = (rows / 16);
      }
      int param = 0;
      for (int ll = 0; ll < page; ll++) {
        for (int zz = 1; zz <= 16; zz++) {
          wp.colSet(ll, "ex_addr_zip_" + zz, sqlStr(param, "addr_zip"));
          wp.colSet(ll, "ex_addr_14_" + zz, sqlStr(param, "addr_14"));
          wp.colSet(ll, "ex_addr_5_" + zz, sqlStr(param, "addr_5"));
          wp.colSet(ll, "ex_chi_name_" + zz, sqlStr(param, "chi_name"));
          wp.colSet(ll, "ex_rec_" + zz, "收");
          if (lsLeftUp)
            wp.colSet(ll, "ex_remark_1_" + zz, sqlStr(param, "remark_1"));
          if (lsRightUp)
            wp.colSet(ll, "ex_remark_2_" + zz, sqlStr(param, "remark_2"));
          if (lsLeftDown)
            wp.colSet(ll, "ex_remark_3_" + zz, sqlStr(param, "remark_3"));
          if (lsRightDown)
            wp.colSet(ll, "ex_remark_4_" + zz, sqlStr(param, "remark_4"));
          if (lsSex)
            wp.colSet(ll, "ex_idno_sex_" + zz, sqlStr(param, "idno_sex"));

          param++;
          if (param == rows)
            break;
        }
      }

    }

    wp.listCount[0] = page;

  }

}
