/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-13  V1.00.00  Andy Liu   program initial                            *
* 106-12-14            Andy		  update : ucStr==>zzstr                     *
* 108-12-20  v1.00.03  Andy       Update ptr_branch=>gen_brn                 *	 
* 109-04-20  V1.00.04  shiyuqi       updated for project coding standard     * 
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
 * 112-06-05  V1.00.06  Ryan       增加退卡編號、掛號條碼、退卡原因欄位    ,PDF要改成EXCEL                                                                              *
******************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;


public class Crdr0510 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0510";

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
    String exReturnDate1 = wp.itemStr("ex_return_date1");
    String exReturnDate2 = wp.itemStr("ex_return_date2");
    String exUserId = wp.itemStr("ex_user_id");
    String exProcStatus = wp.itemStr("ex_proc_status");
    String exPkgDate = wp.itemStr("ex_pkg_date");
    String exReturnType = wp.itemStr("ex_return_type");
    String exIcflag = wp.itemStr("ex_icflag");
    String exReasonCode = wp.itemStr("ex_reason_code");
    String exModDate1 = wp.itemStr("ex_mod_date1");
    String exModDate2 = wp.itemStr("ex_mod_date2");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    lsWhere += sqlStrend(exReturnDate1, exReturnDate2, "a.return_date");
    if (empty(exUserId) == false) {
      lsWhere += sqlCol(exUserId, "a.mod_user");
    }
    if (exProcStatus.equals("0") == false) {
      lsWhere += sqlCol(exProcStatus, "a.proc_status");
    }
    if (exProcStatus.equals("3")) {
      if (empty(exPkgDate) == false) {
        lsWhere += sqlCol(exPkgDate, "a.package_date");
      }
    }
    if (exReturnType.equals("0") == false) {
      lsWhere += sqlCol(exReturnType, "a.return_type");
    }
    if (exIcflag.equals("0") == false) {
      lsWhere += sqlCol(exIcflag, "a.ic_flag");
    }
    if (empty(exReasonCode) == false) {
      lsWhere += sqlCol(exReasonCode, "a.reason_code");
    }
    if (empty(exModDate1) == false) {
      lsWhere += "and uf_2ymd(a.mod_time) >= '" + exModDate1 + "' ";
    }
    if (empty(exModDate2) == false) {
      lsWhere += "and uf_2ymd(a.mod_time) <= '" + exModDate2 + "' ";
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

    wp.selectSQL = "" + "a.return_date, " + "a.id_p_seqno, " + "a.card_no, "
        + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:卡號
        + "a.return_type, " + "a.proc_status, " + "lpad (' ', 12, ' ') db_idcode, "
        + "lpad (' ', 20, ' ') db_cname, " + "a.package_date, " + "nvl(b.chi_name,'') chi_name, "
        + "uf_hi_cname(nvl(b.chi_name,'')) db_chi_name, "// 轉碼:中文姓名
        + "nvl(uf_idno_id(a.id_p_seqno),'') id_no, "
        + "uf_hi_idno (nvl(uf_idno_id(a.id_p_seqno),'')) db_id_no, "// 轉碼:正卡人身分證號碼
        + "a.mod_user, " + "uf_2ymd(a.mod_time) mod_date," + "1 group_ct "
        + ",a.return_seqno ,a.barcode_num ,a.reason_code ";
    wp.daoTable = " crd_return a left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
    wp.whereOrder = " order by a.return_seqno ";

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
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // return_type 退卡性質中文
      String returnType = wp.colStr(ii, "return_type");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"新製卡", "續卡", "毀損重製"};
      wp.colSet(ii, "return_type", commString.decode(returnType, cde, txt));

      // proc_status 處理結果中文
      String procStatus = wp.colStr(ii, "proc_status");
      String[] cde1 = new String[] {"1", "2", "3", "4", "5", "6"};
      String[] txt1 = new String[] {"處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝"};
      wp.colSet(ii, "proc_status", commString.decode(procStatus, cde1, txt1));
      
      // reason_code 處理結果中文
      String reasonCode = wp.colStr(ii, "reason_code");
      String[] cde2 = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11"};
      String[] txt2 = new String[] {"01.查無此公司", "02.查無此人", "03.遷移不明", "04.地址欠詳", "05.查無此址", "06.收件人拒收" ,"07.招領逾期" ,"08.分行退件" ,"09.其他" ,"10.地址改變" ,"11.先寄"};
      wp.colSet(ii, "tt_reason_code", commString.decode(reasonCode, cde2, txt2));

    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exReturnDate1 = wp.itemStr("ex_return_date1");
    String exReturnDate2 = wp.itemStr("ex_return_date2");
    String exUserId = wp.itemStr("ex_user_id");
    String exProcStatus = wp.itemStr("ex_proc_status");
    String exPkgDate = wp.itemStr("ex_pkg_date");
    String exReturnType = wp.itemStr("ex_return_type");
    String exIcflag = wp.itemStr("ex_icflag");
    String exReasonCode = wp.itemStr("ex_reason_code");
    String exModDate1 = wp.itemStr("ex_mod_date1");
    String exModSate2 = wp.itemStr("ex_mod_date2");
    String subTitle = "";
    // 退卡日期
    if (empty(exReturnDate1) == false || empty(exReturnDate2) == false) {
      subTitle += "退卡日期 : ";
      if (empty(exReturnDate1) == false) {
        subTitle += exReturnDate1 + " 起 ";
      }
      if (empty(exReturnDate2) == false) {
        subTitle += " ~ " + exReturnDate2 + " 迄 ";
      }
    }
    // 經辦人員
    if (empty(exUserId) == false) {
      subTitle += " 經辦人員 : " + exUserId;
    }
    // 處理結果
    String procStatus = exProcStatus;
    String[] cde1 = new String[] {"0", "1", "2", "3", "4", "5", "6"};
    String[] txt1 = new String[] {"全部", "處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝"};
    subTitle += " 處理結果: " + commString.decode(procStatus, cde1, txt1);
    // 封裝日期
    if (empty(exPkgDate) == false) {
      subTitle += " 封裝日期 : " + exPkgDate;
    }
    // 退卡性質
    String retunType = exReturnType;
    String[] cde2 = new String[] {"0", "1", "2", "3"};
    String[] txt2 = new String[] {"全部", "新製卡", "續卡", "毀損重製"};
    subTitle += " 退卡性質: " + commString.decode(retunType, cde2, txt2);
    // 晶片旗標
    String icFlag = exIcflag;
    String[] cde3 = new String[] {"0", "1", "2"};
    String[] txt3 = new String[] {"全部", "晶片卡", "一般卡"};
    subTitle += " 晶片旗標: " + commString.decode(icFlag, cde3, txt3);
    // 退卡原因
    String reasonCode = exReasonCode;
    String[] cde4 = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};
    String[] txt4 = new String[] {"查無此公司", "查無此人", "遷移不明", "地址欠詳", "查無此址", "收件人拒收", "招領逾期", "分行退件",
        "其他", "地址改變"};
    if (empty(exReasonCode) == false) {
      subTitle += " 退卡原因: " + commString.decode(reasonCode, cde4, txt4);
    }
    // 異動日期
    if (empty(exModDate1) == false || empty(exModSate2) == false) {
      subTitle += "異動日期 : ";
      if (empty(exModDate1) == false) {
        subTitle += exModDate1 + " 起 ";
      }
      if (empty(exModSate2) == false) {
        subTitle += " ~ " + exModSate2 + " 迄 ";
      }
    }

    reportSubtitle = subTitle;
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
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_branch");
      // dddw_list("dddw_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

