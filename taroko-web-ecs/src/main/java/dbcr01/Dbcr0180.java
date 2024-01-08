/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-25  V1.00.00  Andy Liu   program initial                            *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *	
* 109-12-28  V1.00.03  Justin        parameterize sql
* 110/1/4    V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0180 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0180";

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
	sqlParm.clear();
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exExpireReason = wp.itemStr("ex_expire_reason");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");

    String lsWhere = "where 1=1  ";
    // 固定SQL條件
    lsWhere += "and a.current_code = '0' ";
    lsWhere += "and a.expire_chg_date != '' "; // 20171017 Andy :因資料量龐大自行增加此條件

    // user搜尋條件

    // ex_card_type卡種
    if (empty(exCardType) && empty(exGroupCode) && empty(exExpireReason) && empty(date1)
        && empty(date2)) {
      alertErr("請至少輸入一項查詢條件");
      return false;
    }
    if (empty(exCardType) == false) {
      lsWhere += sqlCol(exCardType, "a.card_type");
    }

    // ex_group_code團體代碼
    if (empty(exGroupCode) == false) {
      lsWhere += sqlCol(exGroupCode, "a.group_code");
    }

    // ex_expire_reason不續卡原因
    if (empty(exExpireReason) == false) {
      lsWhere += sqlCol(exExpireReason, "a.expire_reason");
    }

    // ex_date不續卡起迄日期
    lsWhere += sqlStrend(date1, date2, "a.expire_chg_date");

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

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "a.card_no, " + "a.id_p_seqno, " + "a.issue_date, " + "a.expire_reason, "
        + "a.expire_chg_flag, " + "a.expire_chg_date, " + "a.p_seqno, " + "a.sup_flag, "
        + "a.major_card_no, " + "b.risk_bank_no, " + "b.autopay_acct_no, " + "b.autopay_acct_bank, "
        + "(c.id_no||'_'||c.id_no_code) wk_id_no, " + "c.chi_name, "
        + "lpad(' ',3,'') act_bank_no, " + "lpad(' ',16,'') act_no, " + "0 acct_jrnl_bal  ";
    wp.daoTable = " dbc_card a left join  dba_acno b on a.p_seqno = b.p_seqno "
        + "            left join dbc_idno c on a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by card_no asc  ";

    // setParameter();
    // System.out.println(" select " + wp.selectSQL + " from "
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

  void listWkdata() throws Exception {
    int row_ct = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row_ct += 1;
      wp.colSet(ii, "group_ct", "1");
    }
    wp.colSet("row_ct", intToStr(row_ct));
  }

  void subTitle() {
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exExpireReason = wp.itemStr("ex_expire_reason");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");

    String tmpStr = "";
    // ex_card_type卡種
    if (empty(exCardType) == false) {
      tmpStr += " 卡種 : " + exCardType;
    }

    // ex_group_code團體代碼
    if (empty(exGroupCode) == false) {
      tmpStr += " 團體代碼 : " + exGroupCode;
    }
    // ex_expire_reason不續卡原因
    if (empty(exExpireReason) == false) {
      tmpStr += " 不續卡原因 : " + exExpireReason;
    }

    // ex_date不續卡起迄日期
    if (empty(date1) == false || empty(date2) == false) {
      tmpStr += " 不續卡起迄日期 : ";
      if (empty(date1) == false) {
        tmpStr += date1 + " 起 ";
      }
      if (empty(date2) == false) {
        tmpStr += " ~ " + date2 + " 迄 ";
      }
    }

    reportSubtitle = tmpStr;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = progName + ".xlsx";

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
    wp.reportId = progName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);

    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 42;
    pdf.pageVert = true; // 直印
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
      // dddw_card_type
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_card_type");
      dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 group by card_type,name order by card_type");

      // dddw_group_code
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");

      // dddw_reject_reason(參考檔無此類別andy 20171025)
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_expire_reason");
      dddwList("dddw_expire_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'NOTCHG_VD_S'");

    } catch (Exception ex) {
    }
  }

}

