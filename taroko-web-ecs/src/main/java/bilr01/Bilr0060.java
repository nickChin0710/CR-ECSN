/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-25  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-02-12  V1.00.02  Andy		  update : report add UserID                 *
* 107-05-18  V1.00.03  Andy		  update : UI                                *
* 109-04-27  V1.00.04  shiyuqi       updated for project coding standard     *  
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 111-05-26  V1.00.05   Ryan       移除重複getWhereStr()                        *      
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0060 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilr0060";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

     dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    String exKeyNo = wp.itemStr("ex_key_no");
    String exUser = wp.itemStr("ex_user");
    String exCurrCode = wp.itemStr("ex_curr_code");
    String exIdNo = wp.itemStr("ex_id_no");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exCardNo = wp.itemStr("ex_card_no");

    String lsWhere = "where 1=1  ";
    if (empty(exKeyNo) == false) {
      lsWhere += " and key_no = :ex_key_no";
      setString("ex_key_no", exKeyNo);
    }

    if (empty(exUser) == false) {
      lsWhere += " and mod_user = :ex_user";
      setString("ex_user", exUser);
    }

    if (empty(exCurrCode) == false) {
      lsWhere += " and curr_code = :ex_curr_code";
      setString("ex_curr_code", exCurrCode);
    }

    if (empty(exDateS) == false) {
      lsWhere += " and to_char(mod_time,'yyyymmdd') >= :exDateS ";
      setString("exDateS", exDateS);
    }

    if (empty(exDateE) == false) {
      lsWhere += " and to_char(mod_time,'yyyymmdd') <= :exDateE ";
      setString("exDateE", exDateE);
    }

    if (empty(exCardNo) == false) {
      lsWhere += " and card_no = :ex_card_no";
      setString("ex_card_no", exCardNo);
    }
    if (empty(exIdNo) == false) {
      lsWhere += " and id_p_seqno = (select id_p_seqno from crd_idno where 1=1 and id_no =:ex_id) ";
      setString("ex_id", exIdNo);
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL = "" + " bill_type, " + " tx_code, " + " add_item, " + " card_no, "
        + "uf_hi_cardno (card_no) db_card_no, "// 轉碼:加檔卡號
        + " acct_type, " + " id_p_seqno, " + " uf_idno_id (id_p_seqno) as id_no, "
        + "uf_hi_idno(uf_idno_id(id_p_seqno)) db_id_no , "// 編碼:正卡ID
        + " uf_idno_name (id_p_seqno) as db_chiname, "
        + "uf_hi_cname(uf_idno_name (id_p_seqno)) db_cname , "// 編碼:中文姓名
        + " corp_no, " + " uf_corp_name(corp_no) as db_corp_name, " + " seq_no, " + " dest_amt, "
        + " dest_curr, " + " purchase_date, " + " chi_desc, " + " bill_desc, " + " dept_flag, "
        + " post_flag, " + " key_no, " + " apr_user, " + " apr_date, " + " mod_user, "
        + " mod_time, " + " mod_pgm, " + " decode(curr_code,'','901',curr_code) as curr_code, "
        + " decode(curr_code,'901',dest_amt,dc_dest_amt) as dc_dest_amt ";

    wp.daoTable = " bil_othexp ";
    wp.whereOrder = " order by acct_type ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() {
    int rowCt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt = 1;
      wp.colSet(ii, "row_ct", intToStr(rowCt));
    }

  }

  void subTitle() {
    String exKeyNo = wp.itemStr("ex_key_no");
    String exUser = wp.itemStr("ex_user");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exCardNo = wp.itemStr("ex_card_no");
    String exCurrCode = wp.itemStr("ex_curr_code");
    String title = "";
    // ex_key_no
    if (!empty(exKeyNo)) {
      title += " 登錄批號 : " + exKeyNo;
    }
    // ex_user
    if (!empty(exUser)) {
      title += " 登錄人員 : " + exUser;
    }

    // exDateS & exDateE
    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += " 登錄日期 : ";
      if (empty(exDateS) == false) {
        title += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += " ～ " + exDateE + " 迄 ";
      }
    }
    if (empty(exCardNo) == false) {
      title += " 卡號 : " + exCardNo;
    }
    // ex_user
    if (!empty(exCurrCode)) {
      title += " 結算幣別 : " + exCurrCode;
    }
    reportSubtitle = title;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF2 pdf = new TarokoPDF2();
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";

    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_mcht_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_mcht_no");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
      // loan_flag = 'N' order by mcht_no");
      //
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_product_no");
      // this.dddw_list("dddw_product_no", "bil_prod", "product_no", "", "where 1=1 group by
      // product_no order by product_no");
    	
    	wp.initOption = "--";
    	wp.optionKey = wp.itemStr("ex_curr_code");
        this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1  and wf_type = 'DC_CURRENCY' order by wf_id");

    } catch (Exception ex) {
    }
  }

}
