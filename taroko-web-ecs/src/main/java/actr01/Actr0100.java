/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-28  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		    update : ucStr==>zzStr                     *	
* 107-05-10  V1.00.02  Andy		    update : SQL ,UI,report                    *	
* 108-12-31  V1.00.03  Andy		    update : SQL p_seqno => acno_p_seqno		   *
* 109-04-01	 V1.00.04  Zhenwu Zhu 增加報表統計，條件查詢功能       				   * 
* 109-04-06	 V1.00.05  Zuwei      修改PDF格式       				                 * 
* 109-04-23  V1.00.06  shiyuqi    updated for project coding standard        * 
* 109-01-04  V1.00.07  shiyuqi    修改无意义命名                             *  
* 112-03-23  V1.00.08  Simon      1.fixded double setString(x,x)             *  
*                                 2.acct_type='01' changed into '06'         *  
******************************************************************************/
package actr01;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0100 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "actr0110";

  String condWhere = "";
  String reportSubtitle = "";
  BigDecimal sum = new BigDecimal(0);

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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exAcctMonth = wp.itemStr("ex_acct_month");

    wp.whereStr =
        "where BIL_BILL.ACCT_TYPE = '06' and NVL(billed_flag,'') <> 'B' and NVL(RSK_POST,'') = ''  ";
    if (!empty(exDateS) & !empty(exDateE)) {
      if (exDateE.compareTo(exDateS) < 0) {
        alertErr("入帳日期起迄輸入錯誤!!");
        return false;
      }
    }
    wp.whereStr += sqlStrend(exDateS, exDateE, "bil_bill.post_date");

    if (empty(exAcctMonth) == false) {
      wp.whereStr += " and acct_month =:ex_acct_month ";
      setString("ex_acct_month", exAcctMonth);
    }

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

    if (!"Q".equals(strAction)) {
      if (getWhereStr() == false)
      { return; }
    }

    wp.selectSQL = "BIL_BILL.P_SEQNO," 
		+ "CRD_IDNO.chi_name, " 
		+ "bil_bill.mcht_eng_name, "
        + "act_acno.acct_key, " 
		+ "BIL_BILL.PURCHASE_DATE, " 
		+ "BIL_BILL.post_date, "
        + "BIL_BILL.MCHT_CITY, " 
		+ "BIL_BILL.MCHT_COUNTRY, " 
		+ "BIL_BILL.DEST_AMT, "
        + "BIL_BILL.BIN_TYPE, " 
		+ "BIL_BILL.PROCESS_DATE, " 
		+ "BIL_BILL.SOURCE_CURR, "
        + "BIL_BILL.SOURCE_AMT, " 
		+ "substr(card_no, 13, 4) as card_no, "
        + "case when MCHT_CHI_NAME is null or trim(MCHT_CHI_NAME) = '' then mcht_eng_name else mcht_chi_name end as mcht_chi_name";
    wp.daoTable = " BIL_BILL left join CRD_IDNO on BIL_BILL.id_p_seqno = CRD_IDNO.id_p_seqno "
        + "left join act_acno on BIL_BILL.p_seqno = act_acno.acno_p_seqno ";
    wp.whereOrder = " order by BIL_BILL.P_SEQNO,BIL_BILL.ACCT_MONTH,BIL_BILL.PURCHASE_DATE ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();


    /*
     * String mchtEngName = wp.col_ss("mcht_eng_name"); String mchtChiName =
     * wp.col_ss("mcht_chi_name"); if (isEmpty(mchtChiName)) { System.out.println("test:" +
     * mchtEngName); }
     */
    // wp.col_set("mcht_eng_name_desc", wp.col_ss("mcht_eng_name"));
    // System.out.println("test:" + wp.col_ss("mcht_eng_name_desc"));


    // String cardNo = wp.col_ss("card_no");
    // wp.col_set("card_no_desc", cardNo.substring(6));

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
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      sum = sum.add(new BigDecimal(this.toNum(wp.colStr(ii, "source_amt"))));

      String op = wp.colStr(ii, "source_curr");
      String[] cde1 = new String[] {"901", "840", "392"};
      String[] txt1 = new String[] {"台幣", "美金", "日幣"};
      wp.colSet(ii, "source_curr", commString.decode(op, cde1, txt1));
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
    wp.colSet("sum_source_amt", sum.toString());
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exAcctKey = wp.itemStr("ex_acct_key");
    String exCurrCode = wp.itemStr("ex_curr_code");
    String title = "";

    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += " 入帳日期 : ";
      if (empty(exDateS) == false) {
        title += exDateS; // + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += " ~ " + exDateE; // + " 迄 ";
      }
    }
    if (!empty(exAcctKey)) {
      title += " 帳戶帳號 : ";
      title += exAcctKey;
    }
    if (!empty(exCurrCode)) {
      String[] cde1 = new String[] {"901", "840", "392"};
      String[] txt1 = new String[] {"台幣", "美金", "日幣"};
      title += " 幣別 : " + commString.decode(exCurrCode, cde1, txt1);
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

    strAction = "Q";
    queryFunc();
    // wp.setListCount(1);

    DecimalFormat df = new DecimalFormat("#.00");
    wp.colSet("sum_source_amt", df.format(sum));

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "actr0100.xlsx";

    pdf.sheetNo = 0;
    pdf.pageCount = 30;
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dddwSelect() {
    try {
      // 雙幣幣別
      wp.optionKey = wp.colStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
    } catch (Exception ex) {
    }
  }

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    if (acctkey.trim().length() == 8)
      rtn += "000";
    if (acctkey.trim().length() == 10)
      rtn += "0";

    return rtn;
  }
}
