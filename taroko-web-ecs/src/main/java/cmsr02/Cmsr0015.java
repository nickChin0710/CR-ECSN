/** 
 *   * 109-04-28  shiyuqi       updated for project coding standard     * 
 * 19-1209:   Alex  fix queryWhere
 * 19-1127:   Alex  querFunc , queryRead
 * 19-0614:   JH    p_xxx >>acno_p_xxx
 * 107/03/06  V1.00.1     Alex     add dddw                                   *
 * 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                                                                     *    
*/

package cmsr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Cmsr0015 extends BaseAction implements InfacePdf {
  String isWhere = "", isWhere2 = "";

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
      if (eqIgno(wp.respHtml, "cmsr0015")) {
        wp.optionKey = wp.colStr(0, "ex_docu_code");
        dddwList("d_dddw_idtab4", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='VOICE_LIST'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_date1")) {
      alertErr2("登錄日期(起):不可空白!");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("日期起迄：輸入錯誤");
      return;
    }
    wp.setQueryMode();

    queryRead();
  }

  void condWhere() {

    isWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "input_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "input_date", "<=");

    isWhere2 = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=");

    if (empty(wp.itemStr("ex_docu_code"))) {
      isWhere += " and document in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST')";
      isWhere2 += " and docu_code in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST')";
      // is_where += " and document in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST'
      // and wf_id not like '9%')";
      // is_where2 += " and docu_code in (select wf_id from ptr_sys_idtab where wf_type='VOICE_LIST'
      // and wf_id like '9%')";
    } else {
      isWhere += sqlCol(wp.itemStr("ex_docu_code"), "document");
      isWhere2 += sqlCol(wp.itemStr("ex_docu_code"), "docu_code");
    }

    if (!empty(wp.itemStr("ex_idno"))) {
      isWhere += sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
      isWhere2 += sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
    } else if (!empty(wp.itemStr("ex_cardno"))) {
      isWhere += sqlCol(wp.itemStr("ex_cardno"), "card_no", "like%");
      isWhere2 += sqlCol(wp.itemStr("ex_cardno"), "card_no", "like%");
    }

    if (wp.itemEq("ex_debit_flag", "Y")) {
      isWhere += " and p_seqno = 'x'";
    }

    if (wp.itemEq("ex_debit_flag", "0") == false) {
      isWhere2 += sqlCol(wp.itemStr2("ex_debit_flag"), "uf_nvl(debit_flag,'Y')");
    }

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    condWhere();

    wp.sqlCmd = "SELECT mod_time , " + " p_seqno ," + " document as docu_code ,"
        + " document||'_'||(select wf_desc from ptr_sys_idtab where wf_type ='VOICE_LIST' and wf_id = document) as tt_docu_code , "
        + " card_no , " + " uf_hi_cardno(card_no) as hi_card_no ," + " id_no , "
        + " uf_hi_idno(id_no) as hi_id_no ," + " 'N' as debit_flag ,"
        + " uf_acno_name(p_seqno) as db_chi_name ," + " 'VOICE' as db_table ," + " input_date , "
        + " input_time , "
        + " decode(uf_nvl(mod_time,''),'',input_date,to_char(mod_time,'yyyymmdd')) as mod_date2 ,  "
        + " decode(uf_nvl(mod_time,''),'',input_time,to_char(mod_time,'hh24miss')) as mod_time2 "
        + " FROM mkt_voice " + isWhere;
    wp.sqlCmd += " UNION " + "SELECT mod_time , " + " p_seqno ," + " docu_code ,"
        + " docu_code||'_'||(select wf_desc from ptr_sys_idtab where wf_type ='VOICE_LIST' and wf_id = docu_code) as tt_docu_code , "
        + " card_no , " + " uf_hi_cardno(card_no) as hi_card_no ," + " id_no , "
        + " uf_hi_idno(id_no) as hi_id_no ," + " debit_flag ," + " chi_name as db_chi_name ,"
        + " 'VOCDATA' as db_table ," + " crt_date as input_date , " + " crt_time as input_time , "
        + " decode(uf_nvl(mod_time,''),'',crt_date,to_char(mod_time,'yyyymmdd')) as mod_date2 ,  "
        + " decode(uf_nvl(mod_time,''),'',crt_time,to_char(mod_time,'hh24miss')) as mod_time2 "
        + " FROM mkt_vocdata" + isWhere2
        + " order by docu_code Asc, input_date Asc, id_no Asc, card_no Asc";

    wp.pageCountSql =
        "select count(*) " + " from (" + "SELECT input_date,input_time from mkt_voice " + isWhere
            + " union select crt_date as input_date,crt_time as input_time from mkt_vocdata "
            + isWhere2 + ")";

    this.pageQuery();
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();

  }

  void queryAfter() {
    String lsTime = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (this.eqIgno(wp.colStr(ii, "debit_flag"), "Y")) {
        wp.colSet(ii, "debit_flag", "Y.VD卡");
      } else if (this.eqIgno(wp.colStr(ii, "debit_flag"), "N")) {
        wp.colSet(ii, "debit_flag", "N.信用卡");
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
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "cmsr0015";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "轉入日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr0015.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
