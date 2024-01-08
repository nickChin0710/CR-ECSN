/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-02-01  V1.00.00  Andy Liu   program initial                            *
 * 107-07-10  V1.00.01  Andy       Debug                                      *
 * 109-05-22  V1.00.02  Andy       Mantis3482                                 *
 * 109-05-27  V1.00.03  Amber      Mantis:0003512                             *
 * 109-10-05  V1.00.04  Amber      Update dataWriter                          *
 * 110-06-26  V1.00.05  Andy       Update add check_approve                   *
 * 111/10/28  V1.00.06  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package cycr01;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ofcapp.BaseReport;
import taroko.com.*;

public class Cycr1010 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "cycr1010";
  TarokoParm tarokoParm = TarokoParm.getInstance();
  String   workDir = tarokoParm.getRootDir()+"/WebData/work";

  String condWhere = "";
  String reportSubtitle = "";
  ArrayList<String> datalist = new ArrayList<String>();

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
    } else if (eqIgno(wp.buttonCode, "S2")) { // -CSV-
      strAction = "S2";
      // wp.setExcelMode();
      //-check approve-
      if (!checkApprove(wp.itemStr("zz_apr_user"),wp.itemStr("zz_apr_passwd"))){
        return;
      }
      dataProcess();
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
    // sysdate1 = ss_mid(getSysDate(),0,8);
    // 續卡日期起-迄日
    // wp.colSet("exDateS", "");
    wp.colSet("ex_int_rate", "0.000");
  }

  private boolean getWhereStr() throws Exception {
    String exAcctMonth = wp.itemStr("ex_acct_month");
    double exIntRate = wp.itemNum("ex_int_rate");

    wp.whereStr = "where 1=1  ";

    // 固定搜尋條件

    // user搜尋條件
    wp.whereStr += sqlCol(exAcctMonth, "acct_month");
    if (exIntRate == 0) {
      // wp.whereStr += " and revolve_int_rate != 0 "; //減碼利率為0時不設為查詢條件
    } else {
      wp.whereStr += " and revolve_int_rate =:revolve_int_rate ";
      setDouble("revolve_int_rate", exIntRate);
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
    String exSortCol = wp.itemStr("ex_sort_col");

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "hex(rowid) as rowid, "
            + "acct_month, "
            + "last_rc_flag, "
            + "rc_flag, "
            + "new_card_holder, "
            + "revolve_int_rate, "
            + "pd_rating, "
            + "payment_rate, "
            + "acct_status, "
            + "decode (acct_status,'1','正常','2','逾放','3','催收','4','呆帳') db_acct_status, "
            + "active_card_num, "
            + "last_ttl_amt, "
            + "billed_end_bal, "
            + "ttl_amt, "
            + "acno_num,"
            + "mod_time ";
    wp.daoTable = " cyc_int_rate_static ";

    switch (exSortCol) {
      case "REVOLVE_INT_RATE":
        wp.whereOrder = " order by REVOLVE_INT_RATE,rowid ";
        break;
      case "RC_FLAG":
        wp.whereOrder = " order by RC_FLAG,rowid ";
        break;
    }

    wp.colSet("sel_sql", "select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr + wp.whereOrder);
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
    int sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0;
    int select_cnt = wp.selectCnt;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt = 1;
      wp.colSet(ii, "group_ct", "1");
      // 轉換顯示格式
      DecimalFormat a2 = new DecimalFormat("#.000");
      wp.colSet(ii, "revolve_int_rate", a2.format(wp.colNum(ii, "revolve_int_rate")));

      // wk_modify_type
      String[] cde1 = new String[] { "1", "2", "3", "4" };
      String[] txt1 = new String[] { "正常", "逾放", "催收", "呆帳" };
      wp.colSet(ii, "db_acct_status", commString.decode(wp.colStr(ii, "acct_status"), cde1, txt1));
      wp.colSet(ii, "row_ct", intToStr(rowCt));
    }

    // 20200527 add sum
    String exAcctMonth = wp.itemStr("ex_acct_month");
    double exIntRate = wp.itemNum("ex_int_rate");
    String lsSql = "select count(acct_month) count_all , "
            + "sum(active_card_num) sum_active_card_num ,"
            + "sum(acno_num) sum_acno_num , "
            + "sum(last_ttl_amt) sum_last_ttl_amt ,"
            + "sum(billed_end_bal) sum_billed_end_bal ,"
            + "sum(ttl_amt) sum_ttl_amt "
            + "from cyc_int_rate_static where 1=1 ";
    lsSql += sqlCol(exAcctMonth, "acct_month");
    if (exIntRate != 0) {
      lsSql += sqlCol(numToStr(exIntRate, "#.###"), "revolve_int_rate");
    }
    sqlSelect(lsSql);
    wp.colSet("count_all", sqlStr("count_all"));
    wp.colSet("sum_active_card_num", sqlStr("sum_active_card_num"));
    wp.colSet("sum_acno_num", sqlStr("sum_acno_num"));
    wp.colSet("sum_last_ttl_amt", sqlStr("sum_last_ttl_amt"));
    wp.colSet("sum_billed_end_bal", sqlStr("sum_billed_end_bal"));
    wp.colSet("sum_ttl_amt", sqlStr("sum_ttl_amt"));

  }

  void subTitle() {
    String exAcctMonth = wp.itemStr("ex_acct_month");
    double exIntRate = wp.itemNum("ex_int_rate");
    String exSortCol = wp.itemStr("ex_sort_col");

    String ss = "";
    // ex_toibmdate送IBM取三軌日期
    ss += "報表年月 : " + exAcctMonth;
    ss += "   減碼利率 : " + exIntRate;
    switch (exSortCol) {
      case "RC_FLAG":
        ss += "   報表排序 : 本期RC戶註記 ";
        break;
      case "REVOLVE_INT_RATE":
        ss += "   報表排序 : 減碼利率 ";
        break;
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
    // ===========================
    wp.pageRows = 99999;
    queryFunc();
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    pdf.pageCount = 28;
    // wp.setListCount(1);

    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;

    // pdf.pageVert= true; //直印
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
      // //dddw_trans_type
      // wp.initOption = "--";
      // wp.optionKey = wp.colStr("ex_trans_type");
      // dddw_list("dddw_trans_type", "ptr_sys_idtab", "wf_id", "wf_desc",
      // "where 1=1 and wf_type = 'IBM_STOPTYPE' order by wf_id");
      //
      // //dddw_rtn_code
      // wp.initOption = "--";
      // wp.optionKey = wp.colStr("ex_rtn_code");
      // dddw_list("dddw_rtn_code", "ptr_sys_idtab", "wf_id", "wf_desc",
      // "where 1=1 and wf_type = 'IBM_RTNCODE' and wf_id <> '00' and
      // wf_id <>'99' order by wf_id");
    } catch (Exception ex) {
    }
  }

  public String getSysTime() {
    String dateStr = "";
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMdd-HHmmss");
    dateStr = form1.format(currDate);
    return dateStr;
  }

  void dataProcess() throws Exception {
    // 產生csv檔 20200527
    String lsSql = "";
    String lsText = "";
    String exAcctMonth = wp.itemStr("ex_acct_month");
    double exIntRate = wp.itemNum("ex_int_rate");
    //
    lsText = "信用卡差別循環利率統計表" + ",,,,,,,,,,,";
    datalist.add(lsText);
    lsText = "報表年月 : " + exAcctMonth + ",,,,,,,,,,,";
    datalist.add(lsText);
    lsText = "User-id : " + wp.loginUser + ",,,,,,,,,,,";
    datalist.add(lsText);
    lsText = "Date-Time : " + getSysTime() + ",,,,,,,,,,,";
    datalist.add(lsText);
    lsText = "前期RC戶,本期RC戶,新卡友,減碼利率,違約預測,Payment rate,帳戶狀態,有效卡數,戶數,上期本金結欠金額,本期消費金額,本期本金總欠金額";
    datalist.add(lsText);

    setSelectLimit(0);
    lsSql = wp.colStr("sel_sql");
    if (empty(lsSql)) {
      alertErr("請先查詢!");
      return;
    }
    setDouble("revolve_int_rate", exIntRate);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      alertErr("此條件查無資料!");
      return;
    }
    for (int i = 0; i < sqlRowNum; i++) {
      lsText = "";
      lsText += sqlStr(i, "last_rc_flag") + ","; // 前期RC戶
      lsText += sqlStr(i, "rc_flag") + ","; // 本期RC戶
      lsText += sqlStr(i, "new_card_holder") + ","; // 新卡友
      lsText += sqlStr(i, "revolve_int_rate") + ","; // 減碼利率
      lsText += sqlStr(i, "pd_rating") + ","; // 違約預測
      lsText += sqlStr(i, "payment_rate") + ","; // Payment rate
      lsText += sqlStr(i, "db_acct_status") + ","; // 帳戶狀態
      lsText += sqlStr(i, "active_card_num") + ","; // 有效卡數
      lsText += sqlStr(i, "acno_num") + ","; // 戶數
      lsText += sqlStr(i, "last_ttl_amt") + ","; // 上期本金結欠金額
      lsText += sqlStr(i, "billed_end_bal") + ","; // 本期消費金額
      lsText += sqlStr(i, "ttl_amt") + ","; // 本期本金總欠金額
      datalist.add(lsText);
    }
    //
    // 20200527 add sum
//		double count_all = 0; // 總筆數
//		double sum_active_card_num = 0; // active_card_num(有效卡數)
//		double sum_acno_num = 0; // acno_num(戶數)
//		double sum_last_ttl_amt = 0; // last_ttl_amt(上期本金結欠金額)
//		double sum_billed_end_bal = 0; // billed_end_bal(本期消費金額)
//		double sum_ttl_amt = 0; // ttl_amt(有效卡數)
//		ls_sql = "select count(acct_month) count_all , "
//				+ "sum(active_card_num) sum_active_card_num ,"
//				+ "sum(acno_num) sum_acno_num , "
//				+ "sum(last_ttl_amt) sum_last_ttl_amt ,"
//				+ "sum(billed_end_bal) sum_billed_end_bal ,"
//				+ "sum(ttl_amt) sum_ttl_amt "
//				+ "from cyc_int_rate_static where 1=1 ";
//		ls_sql += sqlCol(ex_acct_month, "acct_month");
//		if (ex_int_rate == 0) {
//			//ls_sql += " and revolve_int_rate != 0 ";
//		} else {
//			ls_sql += sqlCol(numToStr(ex_int_rate, "#.###"), "revolve_int_rate");
//		}
//		sqlSelect(ls_sql);
//		count_all = sql_num("count_all");
//		sum_active_card_num = sql_num("sum_active_card_num");
//		sum_acno_num += sql_num("sum_acno_num");
//		sum_last_ttl_amt += sql_num("sum_last_ttl_amt");
//		sum_billed_end_bal += sql_num("sum_billed_end_bal");
//		sum_ttl_amt += sql_num("sum_ttl_amt");
//		//
//		ls_text = "總筆數 : " + numToStr(count_all, "###") + ",,,,,,合計 : " + ",";
//		ls_text += numToStr(sum_active_card_num, "###") + ",";
//		ls_text += numToStr(sum_acno_num, "###") + ",";
//		ls_text += numToStr(sum_last_ttl_amt, "###") + ",";
//		ls_text += numToStr(sum_billed_end_bal, "###") + ",";
//		ls_text += numToStr(sum_ttl_amt, "###") + ",";
    lsText = "總筆數 : " + wp.colStr("count_all") + ",,,,,,合計 : " + ",";
    lsText += wp.colStr("sum_active_card_num") + ",";
    lsText += wp.colStr("sum_acno_num") + ",";
    lsText += wp.colStr("sum_last_ttl_amt") + ",";
    lsText += wp.colStr("sum_billed_end_bal") + ",";
    lsText += wp.colStr("sum_ttl_amt") + ",";
    datalist.add(lsText);

    // 寫入檔名
    String fileName = "cycr1010_" + getSysDate() + ".csv";
    if (dataWriter(fileName) != 1) {
      alertErr("產生.CSV檔失敗, 檔名: " + fileName);
      return;
    }
    // 由web端ftp至本機目錄
    if (dataFtp() != 1) {
      alertErr("下載.CSV檔失敗Ftp err, 檔名: " + fileName);
      return;
    }

  }

  int dataWriter(String ls_doc) {
    // server端 檔案存放位置
    File childFile = new File(workDir, ls_doc);

    try {
//			BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(childFile), "UTF-8")); // 指點編碼格式，以免讀取時中文字符異常
      BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(childFile), "MS950")); // USER要求直接開啟csv檔案, 改以MS950編碼 (但會有文字數字自動轉換格式為科學符號問題)
      // BufferedWriter fileout = new BufferedWriter(new
      // OutputStreamWriter(new FileOutputStream(childFile, true),
      // "big5")); // 指點編碼格式，以免讀取時中文字符異常

      for (int rr = 0; rr < datalist.size(); rr++) {

        fileout.write(datalist.get(rr));
        fileout.newLine();

      }

      fileout.flush();
      fileout.close();

      // 檔案下載
      /*
       * try { wp.setDownload(ls_doc); } catch (Exception e) { return -1;
       * }
       */
    } catch (IOException e) {
      return -1;
    }
    return 1;
  }

  // 由web端ftp至本機目錄
  int dataFtp() {
    String inputFile = "MKTOP" + wp.itemStr("kk_batchno") + ".txt";
    String msg = "";
    String fileName = "cycr1010_" + getSysDate() + ".csv";
    try {
      TarokoFTP ftp = new TarokoFTP();

      // ftp.set_remotePath(ex_frompath); //set_remotePath 完整路徑
      ftp.setRemotePath2(TarokoParm.getInstance().getDataRoot() + "/work"); // set_remotePath2 :
      // media...以後路徑
      ftp.fileName = fileName;
      ftp.localPath = workDir;
      ftp.ftpMode = "BIN";
      int rc = ftp.getFile(wp);
      wp.showLogMessage("I", "", "get_File return code: " + rc);
      if (rc != 0) {
        alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
      } else {
        // wp.setDownload(ftp.fileName); //轉碼下載
        wp.setDownload(ftp.fileName); // 不轉碼下載
        // ****不轉碼直接開
        // wp.linkMode = "Y";
        // wp.linkURL =
        // "http://"+wp.applHost+"/"+wp.applName+"/WebData/work/"+ftp.fileName;

      }
      msg = ftp.getMesg();
    } catch (Exception ex) {
      msg = ex.getMessage();
      // msg = "FTP 下載檔案失敗";
      if (msg.indexOf("路徑名稱中的檔案或目錄不存在") > -1) {
        msg = "下載檔案不存在!!";
      }

      if (msg.indexOf("A file or directory in the path name does not exist") > -1) {
        msg = "下載檔案不存在!!";
      }
    }
    alertErr(msg);
    return 1;
  }

}
