/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-27  V1.00.00  Andy Liu   program initial                            *
* 106-12-14            Andy		  update : ucStr==>commString                     *
* 109-12-24            Andy		  update : UI                                *
* 109-04-23  V1.00.04  yanghan  修改了變量名稱和方法名稱*
* 109-12-28   V1.00.05 Justin        parameterize sql
*  110/1/4  V1.00.06  yanghan       修改了變量名稱和方法名稱            *
 * 112-06-05  V1.00.07  Ryan       增加退卡編號、掛號條碼、退卡原因欄位    ,PDF要改成EXCEL       
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0510 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0510";

  String condWhere = "";
  String reportSubtitle = "";
  String reportSubtitle2 = "";

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
    String exReturnDate1 = wp.itemStr("ex_return_date1");
    String exReturnDate2 = wp.itemStr("ex_return_date2");
    String exUserId = wp.itemStr("ex_user_id");
    String exProcStatus = wp.itemStr("ex_proc_status");
    String exPkgDate = wp.itemStr("ex_pkg_date");
    String exModDate1 = wp.itemStr("ex_mod_date1");
    String exModDate2 = wp.itemStr("ex_mod_date2");
    // String ex_digital_flag = wp.item_ss("ex_digital_flag");
    String exReturnType = wp.itemStr("ex_return_type");
    String exEeasonCode = wp.itemStr("ex_reason_code");

    String lsWhere = "where 1=1  ";
    // 固定SQL條件

    // user搜尋條件
    lsWhere += sqlStrend(exReturnDate1, exReturnDate2, "a.return_date");

    if (empty(exUserId) == false) {
      lsWhere += sqlCol(exUserId, "a.mod_user");
    }

    if (exProcStatus.equals("0") == false) {
      if (empty(exProcStatus) == false) {
        lsWhere += sqlCol(exProcStatus, "a.proc_status");
      }
    }

    if (empty(exPkgDate) == false) {
      lsWhere += sqlCol(exPkgDate, "a.package_date");
    }

    lsWhere += sqlStrend(exModDate1, exModDate2, "to_char(a.mod_time,'yyyymmdd')");

    if (exReturnType.equals("0") == false) {
      if (empty(exReturnType) == false) {
        lsWhere += sqlCol(exReturnType, "a.return_type");
      }
    }

    if (empty(exEeasonCode) == false) {
      lsWhere += sqlCol(exEeasonCode, "a.reason_code");
    }
    // ex_digital_flag存戶別 //20191224 Mantis2131
    // if (ex_digital_flag.equals("0")==false){
    // if (ex_digital_flag.equals("Y")){
    // ls_where += " and nvl(c.digital_flag,'N') ='Y' ";
    // }else{
    // ls_where += " and nvl(c.digital_flag,'N') ='N' ";
    // }
    // }

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

    wp.selectSQL = "" + "a.return_date, " + "a.id_p_seqno, " + "a.card_no, " + "a.return_type, "
        + "a.proc_status, " + "a.mod_user, " + "(uf_hi_idno(b.id_no)||'_'||b.id_no_code) db_idno, "
        + "(b.id_no||'_'||b.id_no_code) db_idno1, " + "b.chi_name as chi_name1, "
        + "uf_hi_cname(b.chi_name) chi_name, " + "a.package_date,  "
        + "nvl(c.digital_flag,'N') digital_flag," + "'' wk_temp "
    	+ ",a.return_seqno ,a.barcode_num ,a.reason_code ";
    wp.daoTable = " dbc_return a left join dbc_idno b on a.id_p_seqno = b.id_p_seqno "
        + "              left join dbc_card c on a.card_no = c.card_no ";
    wp.whereOrder = " order by a.return_seqno  ";

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
    int row = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row += 1;
      wp.colSet(ii, "group_ct", "1");

      // return_type 退卡性質中文
      String returnType = wp.colStr(ii, "return_type");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"新製卡", "續卡", "重製卡"};
      wp.colSet(ii, "db_return_type", commString.decode(returnType, cde, txt));

      // proc_status 處理結果中文
      String procStatus = wp.colStr(ii, "proc_status");
      String[] cde1 = new String[] {"1", "2", "3", "4", "5", "6", "7"};
      String[] txt1 = new String[] {"處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝", "庫存"};
      wp.colSet(ii, "db_proc_status", commString.decode(procStatus, cde1, txt1));

      // digital_flag 存戶別
      String digitalFlag = wp.colStr(ii, "digital_flag");
      String[] cde2 = new String[] {"Y", "N"};
      String[] txt2 = new String[] {"數位存款帳戶", "一般款帳戶"};
      wp.colSet(ii, "digital_flag", commString.decode(digitalFlag, cde2, txt2));
      
      // reason_code 處理結果中文
      String reasonCode = wp.colStr(ii, "reason_code");
      String[] cde3 = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11"};
      String[] txt3 = new String[] {"01.查無此公司", "02.查無此人", "03.遷移不明", "04.地址欠詳", "05.查無此址", "06.收件人拒收" ,"07.招領逾期" ,"08.分行退件" ,"09.其他" ,"10.地址改變" ,"11.先寄"};
      wp.colSet(ii, "tt_reason_code", commString.decode(reasonCode, cde3, txt3));
    }
    wp.colSet("row_ct", intToStr(row));
  }

  void subTitle() {
    String exReturnDate1 = wp.itemStr("ex_return_date1");
    String exReturnDate2 = wp.itemStr("ex_return_date2");
    String exUserId = wp.itemStr("ex_user_id");
    String exProcStatus = wp.itemStr("ex_proc_status");
    String exPkgDate = wp.itemStr("ex_pkg_date");
    String exModDate1 = wp.itemStr("ex_mod_date1");
    String exModDate2 = wp.itemStr("ex_mod_date2");
    String exDigitalFlag = wp.itemStr("ex_digital_flag");
    String exReturnType = wp.itemStr("ex_return_type");
    String exReasonCode = wp.itemStr("ex_reason_code");

    String all = "";
    String all2 = "";
    // ex_return_date退卡起迄日期
    if (empty(exReturnDate1) == false || empty(exReturnDate2) == false) {
      all += " 退卡起迄日期 : ";
      if (empty(exReturnDate1) == false) {
        all += exReturnDate1 + " 起 ";
      }
      if (empty(exReturnDate2) == false) {
        all += " ~ " + exReturnDate2 + " 迄 ";
      }
    }
    // ex_user_id經辦人員：
    if (empty(exUserId) == false) {
      all += " 經辦人員： " + exUserId;
    }
    // ex_proc_status處理結果：
    String[] cde1 = new String[] {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] txt1 = new String[] {"全部", "處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝", "庫存"};
    all += " 處理結果： " + commString.decode(exProcStatus, cde1, txt1);
    // ex_pkg_date封裝日期
    if (empty(exPkgDate) == false) {
      all += " 封裝日期： " + exPkgDate;
    }
    // ex_mod_date異動日期
    if (empty(exModDate1) == false || empty(exModDate2) == false) {
      all2 += " 異動日期 : ";
      if (empty(exModDate1) == false) {
        all2 += exModDate1 + " 起 ";
      }
      if (empty(exModDate2) == false) {
        all2 += " ~ " + exModDate2 + " 迄 ";
      }
    }
    // ex_digital_flag存戶別： //20191224 Mantis2131
    // String[] cde2=new String[]{"0","N","Y"};
    // String[] txt2=new String[]{"全部","一般存款帳戶","數位存款帳戶"};
    // ss2 += " 存戶別： "+commString.decode(ex_digital_flag, cde2, txt2);

    // ex_return_type退卡性質：
    String[] cde3 = new String[] {"0", "1", "2", "3"};
    String[] txt3 = new String[] {"全部", "新製卡", "續卡", "重製卡"};
    all2 += " 退卡性質： " + commString.decode(exReturnType, cde3, txt3);
    // ex_reason_code退卡原因：
    if (empty(exReasonCode) == false) {
      String[] cde4 = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};
      String[] txt4 = new String[] {"01.查無此公司", "02.查無此人", "03.遷移不明", "04.地址欠詳", "05.查無此址",
          "06.收件人拒收", "07.招領逾期", "08.分行退件", "09.其他", "10.地址改變"};
      all2 += " 退卡原因： " + commString.decode(exReasonCode, cde4, txt4);
    }

    reportSubtitle = all;
    reportSubtitle2 = all2;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);
      wp.colSet("cond_2", reportSubtitle2);
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
    wp.colSet("cond_2", reportSubtitle2);
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
      // dddw_card_type
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_card_type");
      // dddw_list("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1 group by
      // card_type,name order by card_type");

      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");

      // dddw_reject_reason
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_expire_reason");
      // dddw_list("dddw_expire_reason", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type
      // = 'NOTCHG_VD_S'");

    } catch (Exception ex) {
    }
  }

}

