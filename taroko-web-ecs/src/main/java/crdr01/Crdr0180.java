/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-29  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-06-01  V1.00.02  Andy		  update SQL,UI                              * 
* 108-06-13  V1.00.03  Andy		  update :act_acno p_seqno ==> acno_p_seqno  *
*                                         act_acct p_seqno ==> p_seqno       * 
* 109-05-06  V1.00.04  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdr01;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0180 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0180";

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

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    String sysdate1 = "", sysdate0 = "";
    sysdate1 = strMid(getSysDate(), 0, 8);
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -2);
    dateformat.format(c.getTime());
    sysdate0 = dateformat.format(c.getTime());

    wp.colSet("exDateS", sysdate0);
    wp.colSet("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exExpireReason = wp.itemStr("ex_expire_reason");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += "and a.current_code = '0' ";

    // user搜尋條件
    if (empty(exCardType) == false) {
    	lsWhere += sqlCol(exCardType, "a.card_type");
    }
    if (empty(exGroupCode) == false) {
    	lsWhere += sqlCol(exGroupCode, "a.group_code");
    }
    if (empty(exExpireReason) == false) {
    	lsWhere += sqlCol(exExpireReason, "a.expire_reason");
    }

    lsWhere += sqlStrend(exDateS, exDateE, "a.expire_chg_date");

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false)
//      return;
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

    wp.selectSQL = "" + "a.card_no, " + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:卡號
        + "a.id_p_seqno, " + "uf_idno_id(a.id_p_seqno) id_no, "
        + "uf_hi_idno (uf_idno_id(a.id_p_seqno)) db_id_no, "// 轉碼:身分證號
        + "a.issue_date, " + "a.expire_reason, " + "a.expire_chg_flag, " + "a.expire_chg_date, "
        + "uf_idno_name(a.id_p_seqno) chi_name, "
        + "uf_hi_cname(uf_idno_name(a.id_p_seqno)) db_chi_name, "// 轉碼:姓名
        + "a.acno_p_seqno, " + "a.acct_type, " + "lpad(' ',3,'') act_bank_no, "
        + "lpad(' ',16,'') act_no, " + "a.p_seqno, " + "b.risk_bank_no, " + "b.autopay_acct_no, "
        + "b.autopay_acct_bank, "
        + "(case when b.autopay_acct_bank != '' then (b.autopay_acct_bank || '-' || b.autopay_acct_no) else '' end)  AS wk_act, "
        + "nvl((select acct_jrnl_bal from act_acct c where a.p_seqno = c.p_seqno ),0) as acct_jrnl_bal, "
        + "0 acct_jrnl_bal, " + "a.sup_flag, " + "a.major_card_no, " + "a.group_code ";
    wp.daoTable = " crd_card a left join act_acno b on a.acno_p_seqno = b.acno_p_seqno ";
    wp.whereOrder = " order by a.card_no ";

    // setParameter();
    // s System.out.println("select " + wp.selectSQL + " from "
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
    int rowCt = 0, sumAmt = 0;
    String lsSql = "";
    String wkExpireReason = "", wkAcctJrnBal = "";
    String wpExpireReason = "", wpPSeqno = "";
    int setCt = wp.selectCnt;
    wp.logSql = false;
    for (int ii = 0; ii < setCt; ii++) {
      // 計算欄位
      rowCt += 1;

      // expire_reason不續卡原因中文
      wpExpireReason = wp.colStr(ii, "expire_reason");
      if (empty(wpExpireReason) == false) {
        lsSql = "select wf_type, wf_id, wf_desc ";
        lsSql += "from ptr_sys_idtab ";
        lsSql += "where 1=1 and wf_type = 'NOTCHG_KIND_S' ";
        lsSql += sqlCol(wpExpireReason, "wf_id");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          wkExpireReason = sqlStr("wf_id") + "-" + sqlStr("wf_desc");
        }
      }
      wp.colSet(ii, "expire_reason", wkExpireReason);

    }

    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exExpireReason = wp.itemStr("ex_expire_reason");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String title = "", lsSql = "";
    if (empty(exCardType) == false) {
      title += " 卡種: " + exCardType;
    }
    if (empty(exGroupCode) == false) {
      title += " 團體代碼: " + exGroupCode;
    }
    if (empty(exExpireReason) == false) {
      lsSql =
          "select wf_id||':'||wf_desc as wk_expire_reason " + "from ptr_sys_idtab " + "where 1=1 "
              + "and wf_type = 'NOTCHG_KIND_S' " + "and wf_id =:wf_id" + "group by wf_id,wf_desc ";
      setString("wf_id", exExpireReason);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        title += " 不續卡原因: " + sqlStr("wk_expire_reason");
      }
    }
    if (!empty(exDateS) || !empty(exDateE)) {
      title += "  不續卡日期: ";
      if (!empty(exDateS)) {
        title += exDateS;
      }
      title += " -- ";
      if (!empty(exDateE)) {
        title += exDateE;
      }
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
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
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

      // dddw_expire_reason
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_expire_reason");
      dddwList("dddw_expire_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'NOTCHG_KIND_S' group by wf_id,wf_desc order by wf_id");

    } catch (Exception ex) {
    }
  }

}

