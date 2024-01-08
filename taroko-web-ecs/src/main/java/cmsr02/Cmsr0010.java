/** 
 *  * 109-04-28  V1.00.04  shiyuqi       updated for project coding standard     * 
 * 2019-12-02 V.1.00.3    Alex     fix pdf_print , queryAfter
 * 2019-06-14 V.1.00.2    JH       p_xxx >>acno_p_xxx
 * 2018-03-06 V.1.00.1    Alex     add dddw                                   *
*/

package cmsr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr0010 extends BaseAction implements InfacePdf {
  String isWhere = "", isWhere2 = "";
  boolean ibPrint = false;

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
      ibPrint = true;
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsr0010")) {
        wp.optionKey = wp.colStr(0, "ex_docu_code");
        dddwList("d_dddw_idtab4", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='VOICE_LIST'");
      }
    } catch (Exception ex) {
    }


  }

  @Override
  public void queryFunc() throws Exception {

    wp.setQueryMode();

    queryRead();
  }

  void condWhere() {

    isWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "input_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "input_date", "<=");

    isWhere2 = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=");

    if (empty(wp.itemStr("ex_docu_code"))) {
      isWhere +=
          " and document in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST' and wf_id not like '9%')";
      isWhere2 +=
          " and docu_code in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST' and wf_id like '9%')";
    } else {
      isWhere += sqlCol(wp.itemStr("ex_docu_code"), "document");
      isWhere2 += sqlCol(wp.itemStr("ex_docu_code"), "docu_code");
    }

  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {

//      if (!eqIgno(wp.colStr(ii, "db_table"), "VOICE")) {
//        continue;
//      }
      String sql1 = "select bill_sending_zip , "
          + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4 as db_addr1 ,"
          + " bill_sending_addr5 as db_addr5 " + " from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "acno_p_seqno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_zip", sqlStr("bill_sending_zip"));
        wp.colSet(ii, "db_addr1", sqlStr("db_addr1"));
        wp.colSet(ii, "db_addr5", sqlStr("db_addr5"));
      }

      String sql2 = " select sex , chi_name from crd_idno where id_no = ? ";
      sqlSelect(sql2, new Object[] {wp.colStr(ii, "id_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_sex", sqlStr("sex"));
        wp.colSet(ii, "db_chi_name", sqlStr("chi_name"));
      }

    }
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    condWhere();

    wp.sqlCmd =
        "SELECT input_date , " + " p_seqno, p_seqno as acno_p_seqno," + " document as docu_code , "
            + " card_no ," + " id_no ," + " 'N' as debit_flag ," + " '' as db_chi_name ,"
            + " '' as db_zip ," + " '' as db_addr1," + " '' as db_addr5, " + " input_time , "
            + " '' as db_sex ," + " 'VOICE' as db_table " + " FROM mkt_voice " + isWhere;
    wp.sqlCmd += " UNION " + "SELECT crt_date as input_date , "
        + " p_seqno, p_seqno as acno_p_seqno," + " docu_code , " + " card_no , " + " id_no , "
        + " debit_flag , " + " chi_name as db_chi_name , " + " bill_sending_zip as db_zip , "
        + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4 as db_addr1 ,"
        + " bill_sending_addr5 as db_addr5 , " + " crt_time as input_time , " + " sex as db_sex , "
        + " 'VOCDATA' as db_table " + " FROM mkt_vocdata" + isWhere2
        + " order by docu_code Asc, input_date Asc, id_no Asc, card_no Asc";

    wp.pageCountSql =
        "select count(*) " + " from (" + "SELECT input_date,input_time from mkt_voice " + isWhere
            + " union select crt_date as input_date,crt_time as input_time from mkt_vocdata "
            + isWhere2 + ")";

    this.pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
    if (ibPrint == true)
      printMatch();

  }

  void printMatch() {
    int llRows = 0, liPage = 0;
    llRows = wp.selectCnt;

    if (llRows % 14 != 0) {
      liPage = (llRows / 14) + 1;
    } else {
      liPage = (llRows / 14);
    }

    int param = 0;
    for (int ll = 0; ll < liPage; ll++) {
      for (int zz = 1; zz <= 14; zz++) {
        wp.colSet(ll, "ex_zip_code_" + zz, wp.colStr(param, "db_zip"));
        wp.colSet(ll, "ex_addr_14_" + zz, wp.colStr(param, "db_addr1"));
        wp.colSet(ll, "ex_addr_5_" + zz, wp.colStr(param, "db_addr5"));
        wp.colSet(ll, "ex_name_" + zz, wp.colStr(param, "db_chi_name"));
        wp.colSet(ll, "ex_docu_code_" + zz, wp.colStr(param, "docu_code"));
        if (wp.colEq(param, "db_sex", "1")) {
          wp.colSet(ll, "ex_sex_" + zz, "先生　　收");
        } else if (wp.colEq(param, "db_sex", "2")) {
          wp.colSet(ll, "ex_sex_" + zz, "小姐　　收");
        }


        param++;
        if (param == llRows)
          break;
      }
    }

    wp.listCount[0] = liPage;
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
    wp.reportId = "cmsr0010";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr0010.xlsx";
    pdf.pageCount = 1;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
