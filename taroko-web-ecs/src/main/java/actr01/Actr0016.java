/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-07-28  V1.00.00  Andy Liu   program initial                            *
 * 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
 * 107-05-10  V1.00.02  Andy		  update : SQL ,UI,report                    *
 * 108-12-31  V1.00.03  Andy		  update : SQL p_seqno => acno_p_seqno       *
 * 109-02-17  V1.00.04  Andy		  update : Mantis2217                        *
 * 110-03-04  v1.00.05  Andy       Update PDF隠碼作業                                                                      *
 * 111/10/24  V1.00.06  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0016 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "actr0016";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // strAction="new";
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
    String exAcctKey = wp.itemStr("ex_acct_key");
    String exCurrCode = wp.itemStr("ex_curr_code");

    wp.whereStr = "where 1=1  ";
    if (!empty(exDateS) & !empty(exDateE)) {
      if (exDateE.compareTo(exDateS) < 0) {
        alertErr("出帳日期起迄輸入錯誤!!");
        return false;
      }
    }
    wp.whereStr += sqlStrend(exDateS,exDateE,"a.crt_date");

    if (empty(exAcctKey) == false) {
      String lsAcctKey = fillZeroAcctKey(exAcctKey);
      wp.whereStr += " and b.acct_key =:ex_acct_key ";
      setString("ex_acct_key", lsAcctKey);
    }
    wp.whereStr += sqlCol(exCurrCode, "a.curr_code");
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

    wp.selectSQL = ""
            + "a.p_seqno, "
            + "a.acct_type, "
            + "a.chi_name, "
            + "uf_hi_cname(a.chi_name) as db_hi_chi_name, "
            + "a.curr_code, "
            + "a.usd_rate, "
            + "a.dc_end_bal_op, "
            + "a.cellar_phone, "
            + "uf_hi_telno(a.cellar_phone) as db_hi_cellar_phone, "
            + "a.office_tel_no, "
            + "uf_hi_telno(a.office_tel_no) as db_hi_office_tel_no, "
            + "a.home_tel_no1, "
            + "uf_hi_telno(a.home_tel_no1) as db_hi_home_tel_no1, "
            + "a.bill_sending_addr, "
            + "uf_hi_addr(a.bill_sending_addr) as db_hi_bill_sending_addr, "
            + "a.crt_date, "
            + "a.mod_time, "
            + "a.mod_pgm,"
            + "b.acct_key,"
            + "uf_hi_idno(substr(b.acct_key,1,10))||substr(b.acct_key,11,1) as db_hi_acct_key ";
    wp.daoTable = " act_crslist a left join act_acno b on a.p_seqno = b.p_seqno "
            + "and b.p_seqno = b.acno_p_seqno ";
    wp.whereOrder = " order by a.crt_date,b.acct_key ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
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
      String op = wp.colStr(ii, "curr_code");
      String[] cde1 = new String[] { "901", "840", "392" };
      String[] txt1 = new String[] { "台幣", "美金", "日幣" };
      wp.colSet(ii, "curr_code", commString.decode(op, cde1, txt1));
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exAcctKey = wp.itemStr("ex_acct_key");
    String exCurrCode = wp.itemStr("ex_curr_code");
    String ss = "";

    if (empty(exDateS) == false || empty(exDateE) == false) {
      ss += " 出帳日期 : ";
      if (empty(exDateS) == false) {
        ss += exDateS ;
      }
      ss +=" -- ";
      if (empty(exDateE) == false) {
        ss +=  exDateE ;
      }
    }
    if (!empty(exAcctKey)) {
      ss += " 帳戶帳號 : ";
      ss += exAcctKey;
    }
    if (!empty(exCurrCode)) {
      String[] cde1=new String[]{"901","840","392"};
      String[] txt1=new String[]{"台幣","美金","日幣"};
      ss += " 幣別 : "+ commString.decode(exCurrCode, cde1, txt1);
    }
    reportSubtitle = ss;
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
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where);
       * wp.listCount[1] =sqlRowNum; log("Summ: rowcnt:" +
       * wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
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
      // 雙幣幣別
      wp.optionKey = wp.colStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
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
