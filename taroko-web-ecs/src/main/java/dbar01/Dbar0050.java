
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-06  V1.00.01  Ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package dbar01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbar0050 extends BaseReport {
  String progName = "dbar0050";
  CommString commString = new CommString();
  int ii = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";

      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    /*
     * try { wp.initOption = "--"; wp.optionKey = wp.item_ss("ex_bno1");
     * dddw_list("dddw_branch1","ptr_branch" , "branch", "branch_name",
     * " where 1=1 order by branch"); wp.optionKey = wp.item_ss("ex_bno2");
     * dddw_list("dddw_branch2","ptr_branch" , "branch", "branch_name",
     * " where 1=1 order by branch"); } catch(Exception ex){}
     */
  }

  @Override
  public void initPage() {
    wp.colSet("tol_beg_bal", 0);
    wp.colSet("tol_end_bal", 0);
  }

  private int getWhereStr() throws Exception {

    String date1 = wp.itemStr("ex_crdate_s");
    String date2 = wp.itemStr("ex_crdate_e");
    if (this.chkStrend(date1, date2) == false) {
      alertErr2("[扣款日期-起迄]  輸入錯誤");
      return -1;
    }
    if (empty(wp.itemStr("ex_id")) && empty(wp.itemStr("ex_accno"))
        && empty(wp.itemStr("ex_cardno")) && empty(wp.itemStr("ex_crdate_s"))
        && empty(wp.itemStr("ex_crdate_e")) && wp.itemStr("ex_deduct_type").equals("0")) {
      alertErr("至少輸入一個查詢條件");
      return -1;
    }
    /*
     * if(f_auth_query_vd()!=1){ return -1; }
     */
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_id")) == false) {

      String sqlSelect =
          "select id_p_seqno from dbc_idno where id_no = :ex_id fetch first 1 rows only";
      setString("ex_id", wp.itemStr("ex_id"));
      sqlSelect(sqlSelect);
      String idPSeqno = sqlStr("id_p_seqno");
      wp.whereStr += " and A.id_p_seqno = :id_p_seqno ";
      setString("id_p_seqno", idPSeqno);
    }

    if (empty(wp.itemStr("ex_accno")) == false) {
      wp.whereStr += " and A.acct_no = :ex_accno ";
      setString("ex_accno", wp.itemStr("ex_accno"));
    }

    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and A.card_no like :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno") + "%");
    }


    if (empty(wp.itemStr("ex_crdate_s")) == false) {
      wp.whereStr += " and A.deduct_proc_date >= :ex_crdate_s ";
      setString("ex_crdate_s", wp.itemStr("ex_crdate_s"));
    }
    if (empty(wp.itemStr("ex_crdate_e")) == false) {
      wp.whereStr += " and A.deduct_proc_date <= :ex_crdate_e ";
      setString("ex_crdate_e", wp.itemStr("ex_crdate_e"));
    }

    if (!wp.itemStr("ex_deduct_type").equals("0")) {
      wp.whereStr += " and A.debt_status = :ex_deduct_type ";
      setString("ex_deduct_type", wp.itemStr("ex_deduct_type"));
    }

    return 1;
  }

  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " UF_IDNO_NAME2(A.id_p_seqno, 'Y') as chi_name "
        + ", UF_IDNO_ID2(A.id_p_seqno,'Y') as id_no " + ", A.deduct_date " + ", A.p_seqno "
        + ", A.acct_type " + ", A.card_no " + ", A.acct_no " + ", A.item_post_date "
        + ", A.purchase_date " + ", A.beg_bal " + ", A.end_bal " + ", A.debt_status "
        + ", A.trans_col_date " + ", A.trans_bad_date " + ", A.id_p_seqno "
        + ", B.office_area_code1 " + ", B.office_tel_no1" + ", B.office_tel_ext1 "
        + ", B.office_area_code2 " + ", B.office_tel_no2 " + ", B.office_tel_ext2 "
        + ", B.home_area_code1 " + ", B.home_tel_no1 " + ", B.home_tel_ext1 "
        + ", B.home_area_code2 " + ", B.home_tel_no2 " + ", B.home_tel_ext2 " + ", B.cellar_phone "
        + ", B.office_area_code1||B.office_tel_no1||B.office_tel_ext1 as oficeaddr1 "
        + ", B.office_area_code2||B.office_tel_no2||B.office_tel_ext2 as oficeaddr2 "
        + ", B.home_area_code1||B.home_tel_no1||B.home_tel_ext1 as homeaddr1 "
        + ", B.home_area_code2||B.home_tel_no2||B.home_tel_ext2 as homeaddr2 ";

    wp.daoTable = " dba_deduct_txn A LEFT OUTER JOIN  dbc_idno B ON A.id_p_seqno = B.id_p_seqno ";
    wp.whereOrder = "  ";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {

  }


  public void dataProcess() throws Exception {
    queryFunc();
    if (wp.selectCnt == 0) {
      alertErr2("報表無資料可比對");
      return;
    }

  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      String cond = "扣款日期: " + commString.strToYmd(wp.itemStr("ex_crdate_s")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_crdate_e"));
      wp.colSet("cond_1", cond);

      cond = "ID: " + wp.itemStr("ex_id");
      wp.colSet("cond_2", cond);

      cond = "金融卡帳號: " + wp.itemStr("ex_accno");
      wp.colSet("cond_3", cond);

      cond = "消費卡號: " + wp.itemStr("ex_cardno");
      wp.colSet("cond_4", cond);

      cond = wp.itemStr("ex_deduct_type");
      switch (cond) {
        case "0":
          cond = "處理狀況: 全部";
          break;
        case "1":
          cond = "處理狀況: 成功";
          break;
        case "2":
          cond = "處理狀況: 逾放";
          break;
        case "3":
          cond = "處理狀況: 催收";
          break;
        case "4":
          cond = "處理狀況: 呆帳";
          break;
      }
      wp.colSet("cond_4", cond);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
      xlsx.excelTemplate = progName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);

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
    String stuct = "扣款日期: " + commString.strToYmd(wp.itemStr("ex_crdate_s")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crdate_e"));
    wp.colSet("cond_1", stuct);

    stuct = "ID: " + wp.itemStr("ex_id");
    wp.colSet("cond_2", stuct);

    stuct = "金融卡帳號: " + wp.itemStr("ex_accno");
    wp.colSet("cond_3", stuct);

    stuct = "消費卡號: " + wp.itemStr("ex_cardno");
    wp.colSet("cond_4", stuct);

    stuct = wp.itemStr("ex_deduct_type");
    switch (stuct) {
      case "0":
        stuct = "處理狀況: 全部";
        break;
      case "1":
        stuct = "處理狀況: 成功";
        break;
      case "2":
        stuct = "處理狀況: 逾放";
        break;
      case "3":
        stuct = "處理狀況: 催收";
        break;
      case "4":
        stuct = "處理狀況: 呆帳";
        break;
    }
    wp.colSet("cond_5", stuct);
    wp.colSet("IdUser", wp.loginUser);
    wp.pageRows = 9999;
    if (getWhereStr() != 1) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.pageCount = 28;
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() {
    String homeaddr = "";
    double tolBegBal = 0, tolEndBal = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      homeaddr = wp.colStr(ii, "debt_status");
      if (homeaddr.equals("3")) {
        wp.colSet(ii, "db_date", wp.colStr(ii, "trans_col_date"));
      }
      if (homeaddr.equals("4")) {
        wp.colSet(ii, "db_date", wp.colStr(ii, "trans_bad_date"));
      }

      homeaddr = wp.colStr(ii, "oficeaddr1");
      if (empty(homeaddr)) {
        homeaddr = wp.colStr(ii, "oficeaddr2");
      }
      wp.colSet(ii, "db_oficeaddr", homeaddr);

      homeaddr = wp.colStr(ii, "homeaddr1");
      if (empty(homeaddr)) {
        homeaddr = wp.colStr(ii, "homeaddr2");
      }
      wp.colSet(ii, "db_homeaddr", homeaddr);

      homeaddr = wp.colStr(ii, "debt_status");
      wp.colSet(ii, "tt_debt_status", commString.decode(homeaddr, ",1,2,3,4", ",1.成功,2.逾放,3.催收,4.呆帳"));

      tolBegBal += wp.colNum(ii, "beg_bal");
      tolEndBal += wp.colNum(ii, "end_bal");
    }
    wp.colSet("tol_beg_bal", tolBegBal);
    wp.colSet("tol_end_bal", tolEndBal);
  }

  int authQueryVd() {
    String idno = "";
    String sqlSelect =
        "select vd_end_bal from ptr_comm_data where 1=1 and parm_code='COLM0910' and seq_no =1 ";
    sqlSelect(sqlSelect);
    double lmamtParm = sqlNum("vd_end_bal");
    if (sqlRowNum < 0) {
      alertErr("資料查詢權限: select PTR_COMM_DATA error");
      return -1;
    }
    if (lmamtParm <= 0) {
      return 1;
    }
    if (empty(wp.itemStr("ex_id")) && empty(wp.itemStr("ex_cardno"))) {
      alertErr("資料查詢權限:卡號 or 身分證ID 不可空白");
      return -1;
    }
    if (wp.itemStr("ex_cardno").length() >= 14) {
      sqlSelect =
          "select id_p_seqno from dbc_card where card_no like :card_no fetch first 1 rows only ";
      setString("card_no", wp.itemStr("ex_cardno"));
      sqlSelect(sqlSelect);
      idno = sqlStr("id_p_seqno");
      if (sqlRowNum <= 0) {
        idno = "";
      }
    } else {
      idno = wp.itemStr("ex_id");
      sqlSelect = "select id_p_seqno from dbc_idno where id_no = :ls_idno fetch first 1 rows only ";
      setString("ls_idno", idno);
      sqlSelect(sqlSelect);
      idno = sqlStr("id_p_seqno");
    }

    if (empty(idno)) {
      alertErr("資料查詢權限:查核資料不是 VD 卡號 or 身分證ID");
      return -1;
    }
    sqlSelect = "select sum(end_bal) as lm_amt from dba_debt where id_p_seqno = :ls_idno ";
    setString("ls_idno", idno);
    sqlSelect(sqlSelect);
    double lmAmt = sqlNum("lm_amt");
    if (sqlRowNum < 0) {
      alertErr("資料查詢權限: select DBC_DEBT error,KEY=" + idno);
      return -1;
    }
    if (lmAmt >= lmamtParm) {
      return 1;
    }
    alertErr("資料查詢權限:卡友欠款未達 [參數金額] 不可查詢,KEY=" + idno);

    return -1;
  }

  int wfGetCardno() {

    return 1;
  }

}
