/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-03  V1.00.00  Andy Liu   program initial                            *
* 108-06-14  V1.00.03  Andy		  update : p_seqno ==> acno_p_seqno          *
* 108-12-20  v1.00.03  Andy       Update ptr_branch=>gen_brn                 *	
* 109-04-21  V1.00.04  YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ipsr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ipsr0020 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "ipsr0020";

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
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
    // 本程式where 條件均於queryRead()處理
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exAutoLoad = wp.itemStr("ex_auto_load");
    String exCardActive = wp.itemStr("ex_card_active");
    String exOverLimit = wp.itemStr("ex_over_limit");
    String exBlock = wp.itemStr("ex_block");

    wp.sqlCmd = "SELECT b.card_no, " + "       b.ips_card_no, " + "       b.purchase_date, "
        + "       b.crt_date, " + "       b.db_idno, " + "       b.db_proc_flag, "
        + "       b.db_auto_load, " + "       b.db_auto_load_date, " + "       b.db_card_active, "
        + "       b.db_active_date, " + "       b.batch_no, " + "       b.id_p_seqno, "
        + "       b.acno_p_seqno, " + "       b.p_seqno, " + "       b.block_flag, "
        + "       b.db_block_reason, " + "       b.db_limit_amt, " + "       b.lm_debt, "
        + "       b.lm_cash_use, " + "       b.lm_unbill_amt, " + "       b.lm_unbill_fee, "
        + "       (b.lm_debt + b.lm_cash_use + b.lm_unbill_amt + b.lm_unbill_fee) AS db_end_bal "
        + "  FROM (" + "        SELECT e.card_no, " + "               e.ips_card_no, "
        + "               e.purchase_date, " + "               e.crt_date, "
        + "               e.db_idno, " + "               e.db_proc_flag, "
        + "               e.db_auto_load, " + "               e.db_auto_load_date, "
        + "               e.db_card_active, " + "               e.db_active_date, "
        + "               e.batch_no, " + "               e.id_p_seqno, "
        + "               e.acno_p_seqno, " + "               e.p_seqno, "
        + "               e.block_flag, " + "               e.db_block_reason, "
        + "               nvl ( (SELECT d.line_of_credit_amt FROM act_acno AS d WHERE d.acno_p_seqno = e.acno_p_seqno),0) AS db_limit_amt, "
        // --卡人:[欠款總額]=
        + "               nvl ( (SELECT sum (f.end_bal) FROM act_debt AS f WHERE f.p_seqno IN (SELECT g.p_seqno FROM act_acno AS g WHERE g.p_seqno = e.p_seqno)),0) AS lm_debt, "
        // -歡喜卡之預借現金金額-
        + "               nvl ( (SELECT sum (h.cash_use_balance) FROM act_combo_m_jrnl h WHERE h.id_p_seqno = e.id_p_seqno AND h.p_seqno = e.p_seqno), 0) AS lm_cash_use, "
        // -分期未billing-
        + "               nvl ( (SELECT sum (i.unit_price * (i.install_tot_term - i.install_curr_term)+ i.remd_amt + decode (i.install_curr_term,0,i.first_remd_amt + i.extra_fees,0)) "
        + "                     FROM bil_contract i "
        + "                    WHERE     i.id_p_seqno = e.id_p_seqno "
        + "                          AND i.p_seqno = e.p_seqno "
        + "                          AND i.install_tot_term != i.install_curr_term "
        + "                          AND decode (i.auth_code,'','N',i.auth_code) NOT IN ('N', 'REJECT', 'P', 'reject')),0) AS lm_unbill_amt, "
        + "               nvl ( (SELECT sum (j.clt_unit_price * (j.clt_install_tot_term - j.install_curr_term)+ j.clt_remd_amt) "
        + "                     FROM bil_contract j "
        + "                    WHERE     j.id_p_seqno = e.id_p_seqno "
        + "                          AND j.p_seqno = e.p_seqno "
        + "                          AND j.install_tot_term != j.install_curr_term "
        + "                          AND decode (j.auth_code,'','N',j.auth_code) NOT IN('N', 'REJECT', 'P', 'reject')),0)AS lm_unbill_fee "
        + "          FROM (" + " 			   SELECT a.card_no, "
        + "                       a.ips_card_no, "
        + "                       a.txn_date AS purchase_date, "
        + "                       a.crt_date, "
        + "                       uf_idno_id (a.card_no) AS db_idno, "
        + "                       b.blacklt_flag AS db_proc_flag, "
        + "                       decode(nvl(b.autoload_clo_date,''),'','N','Y') AS db_auto_load, "
        + "                       nvl (b.autoload_clo_date, '' ) as db_auto_load_date, "
        + "                       decode(nvl(c.activate_date,''),'','N','Y') AS db_card_active, "
        + "                       nvl(c.activate_date,'') AS db_active_date, "
        + "                       a.batch_no, " + "                       c.id_p_seqno, "
        + "                       c.acno_p_seqno, " + "                       c.p_seqno, "
        + "                       decode(c.block_code,'','N','Y') block_flag, "
        + "                       c.block_code as db_block_reason "
        + "                 FROM ips_cgec_all AS a left join ips_card b on a.ips_card_no = b.ips_card_no "
        + "                                        left join crd_card c on a.card_no = c.card_no "; // 串卡檔取得acno_p_seqno
                                                                                                    // 及p_seqno
    wp.sqlCmd += " where 1=1 " + "and a.online_mark='0' ";
    wp.sqlCmd += sqlStrend(exDateS, exDateE, " substrb(a.batch_no,1,8)");
    wp.sqlCmd += ") as e " + ") as b " + " where 1=1 ";
    wp.sqlCmd += sqlCol(exAutoLoad, "db_auto_load");
    wp.sqlCmd += sqlCol(exCardActive, "db_card_active");
    if (exOverLimit.equals("Y")) {
      wp.sqlCmd +=
          "and (b.lm_debt + b.lm_cash_use + b.lm_unbill_amt + b.lm_unbill_fee) <= b.db_limit_amt ";
    }
    if (exOverLimit.equals("N")) {
      wp.sqlCmd +=
          "and (b.lm_debt + b.lm_cash_use + b.lm_unbill_amt + b.lm_unbill_fee) > b.db_limit_amt ";
    }
    wp.sqlCmd += sqlCol(exBlock, "block_flag");
    // System.out.println("sqlCmd : "+wp.sqlCmd);
    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

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
    int rowCt = 0;
    String lsSql = "";
    double dbEndBal = 0, dbLimitAmt = 0, wkOverLimit = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_cnt", "1");
      dbEndBal = wp.colNum(ii, "db_end_bal");
      dbLimitAmt = wp.colNum(ii, "db_limit_amt");
      wkOverLimit = dbEndBal - dbLimitAmt;
      wp.colSet(ii, "wk_over_limit", numToStr(wkOverLimit, "###"));
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exAutoLoad = wp.itemStr("ex_auto_load");
    String exCardActive = wp.itemStr("ex_card_active");
    String exOverLimit = wp.itemStr("ex_over_limit");
    String exBlock = wp.itemStr("ex_block");
    String tmpStr = "";
    // 傳送日期
    if (empty(exDateS) == false || empty(exDateE) == false) {
      tmpStr += "請款日期 : ";
      if (empty(exDateS) == false) {
        tmpStr += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        tmpStr += " ~ " + exDateE + " 迄 ";
      }
    }
    if (empty(exAutoLoad) == false) {
      tmpStr += "  是否開卡: " + exCardActive;
    }
    if (empty(exOverLimit) == false) {
      tmpStr += "   是否超額:" + exOverLimit;
    }
    if (empty(exBlock) == false) {
      tmpStr += "   是否凍結 :" + exBlock;
    }


    reportSubtitle = tmpStr;

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
      wp.fileMode = "N";
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
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
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
      // dddw_group_code
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_branch");
      // dddw_list("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

