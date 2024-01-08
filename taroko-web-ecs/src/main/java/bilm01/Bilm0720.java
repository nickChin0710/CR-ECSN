/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-23  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>commString                     *
* 107-02-05  V1.00.02  ryan       update					  		 		 *
* 107-02-09  V1.00.03  ryan       update									 *
* 109-01-06  V1.00.04  Ru Chen    modify AJAX                                *
* 109-04-24  V1.00.05  shiyuqi       updated for project coding standard     *   
* 109-11-19  V1.00.06  Ryan       移除畫面部分欄位與邏輯                                                                  *   
* 109-01-04  V1.00.07   shiyuqi       修改无意义命名  
* 111-08-18  V1.00.08  machao      增加檢查bil_auto_parm有設定此分期期數邏輯     
* 112-12-13  V1.00.09  JeffKung   增加前一個月份的交易可以分期                                                                          *  
******************************************************************************/

package bilm01;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Bilm0720 extends BaseEdit {
  String kk1 = "", lsType = "", lsDesc = "", lsCode1 = "", lsCode2 = "";
  String kkReferenceNo = "", kkCardNo = "", kkAuthorization = "";
  String actionCodeKk1 = "", paymetRateKK1 = "", cardNoKk1 = "";
  String actionCodeKk2 = "";
  String isCardNo = "", name = "", lsMchtNo = "";
  String isStmtCycle = "",  lsIssueDate = "";
  String mProgName = "bilm0720";
  String ps1 = "1269857100";
  Pattern pattern = Pattern.compile("[0-9]*");
  Calendar cal = Calendar.getInstance();
  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      String exCardNo = wp.itemStr("ex_card_no");
      clearFunc();
      wp.colSet("kk_card_no", exCardNo);
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "TOTTERM")) {
      /* 查詢 */
      strAction = "TOTTERM";
      changeTotterm();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "S2")) {

    } else if (eqIgno(wp.buttonCode, "Q2")) {
      strAction = "Q2";
      selectDate();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      strAction = "C2";
      wfCheckParm();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200106 modify AJAX
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_tx_date1");
    String lsDate2 = wp.itemStr("ex_tx_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return -1;
    }
    if (empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_tx_date1"))
        && empty(wp.itemStr("ex_tx_date2"))) {
      alertErr2("請輸入 卡號 OR 登錄日期");
      return -1;
    }

    wp.whereStr = " where 1=1 ";

    if (!empty(wp.itemStr("ex_card_no"))) {
      wp.whereStr += " and  a.card_no = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
    }
    if (!empty(wp.itemStr("ex_tx_date1"))) {
      wp.whereStr += " and  a.tx_date >= :ex_tx_date1 ";
      setString("ex_tx_date1", wp.itemStr("ex_tx_date1"));
    }
    if (!empty(wp.itemStr("ex_tx_date2"))) {
      wp.whereStr += " and  a.tx_date <= :ex_tx_date2 ";
      setString("ex_tx_date2", wp.itemStr("ex_tx_date2"));
    }
    switch (wp.itemStr("ex_apr_flag")) {
      case "N":
        wp.whereStr += " and  a.apr_date_1 <> '' and (a.post_flag = 'N' or a.post_flag='') ";
        break;
      case "Y":
        wp.whereStr += " and  a.apr_date_1 <>'' and a.post_flag = 'Y' ";
    }
    if (!empty(wp.itemStr("ex_stmt_cycle"))) {
      wp.whereStr += " and  b.stmt_cycle = :ex_stmt_cycle ";
      setString("ex_stmt_cycle", wp.itemStr("ex_stmt_cycle"));
    }
    if (!empty(wp.itemStr("ex_mcht_parm"))) {
      wp.whereStr += " and  a.mcht_no_parm = :ex_mcht_parm ";
      setString("ex_mcht_parm", wp.itemStr("ex_mcht_parm"));
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " a.reference_no " + ", a.card_no" + ", a.tx_date" + ", a.mcht_no"
        + ", a.mcht_no_parm" + ", a.action_desc" + ", a.purchase_date" + ", a.authorization"
        + ", a.dest_amt" + ", a.tot_term" + ", a.crt_user" + ", a.apr_date_1" + ", b.stmt_cycle";

    wp.daoTable = " bil_auto_tx as a join crd_card as b on a.card_no = b.card_no ";
    wp.whereOrder = " order by a.tx_date,a.card_no ";

    if (getWhereStr() != 1)
      return;

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // kk_card_no = wp.item_ss("kk_card_no");
    // if(empty(kk_card_no))
    kkCardNo = itemKk("data_k1");

    // kk_reference_no = wp.item_ss("reference_no");
    // if(empty(kk_reference_no))
    kkReferenceNo = itemKk("data_k2");

    // kk_authorization = wp.item_ss("authorization");
    // if(empty(kk_authorization))
    kkAuthorization = itemKk("data_k3");
    /*
     * if (empty(kk1)) { kk1 = item_kk("data_k1"); }
     */

    wp.selectSQL = "hex(a.rowid) as rowid, " + " a.tx_date, " + " a.card_no, " + " a.mcht_no, "
        + " a.mcht_chi_name, " + " a.authorization, " + " a.authorization as authorization2, "
        + " a.purchase_date, " + " a.dest_amt, " + " a.reference_no, " + " a.tot_term, "
        + " a.payment_rate1, " + " a.payment_rate2, " + " a.payment_rate3, " + " a.payment_rate4, "
        + " a.payment_rate5, " + " a.payment_rate6, " + " a.payment_rate7, " + " a.payment_rate8, "
        + " a.payment_rate9, " + " a.payment_rate10, " + " a.payment_rate11, "
        + " a.payment_rate12, " + " a.payment_rate13, " + " a.payment_rate14, "
        + " a.payment_rate15, " + " a.payment_rate16, " + " a.payment_rate17, "
        + " a.payment_rate18, " + " a.payment_rate19, " + " a.payment_rate20, "
        + " a.payment_rate21, " + " a.payment_rate22, " + " a.payment_rate23, "
        + " a.payment_rate24, " + " a.payment_rate25, " + " a.close_flag, " + " a.crt_user, "
        + " nvl((select usr_id|| '[' || usr_cname || ']' from sec_user where usr_id = a.crt_user ),a.crt_user) as usr_cname, "
        + " a.apr_date_1, " + " a.apr_user_1, " + " a.error_desc, " + " a.post_flag, "
        + " a.post_date, " + " a.mod_user, " + " a.mod_time, " + " a.mod_pgm, " + " a.mod_seqno, "
        + " a.mcht_no_parm, " + " a.action_desc, " + " a.sale_emp_no, "
        + " a.destination_amt_flag, " + " a.destination_amt_parm, " + " a.payment_rate_flag, "
        + " a.payment_rate_term, " 
        + " a.mcht_flag, " 
        + " a.rc_rate_flag, " + " a.rc_rate, " + " a.credit_amt_rate, "
        + " a.rc_rate_parm, " + " a.credit_amt_rate_parm, " + " a.mcc_code_flag, "
        + " a.mcht_category, " + " a.over_credit_amt_flag, " + " a.line_of_credit_amt, "
        + " a.acct_jrnl_bal, " + " a.block_reason_flag, " + " a.block_reason, "
        + " a.block_reason2, " + " a.spec_status_flag, " + " a.spec_status, " + " a.spec_del_date, "
        + " a.trial_status, "
        + " b.stmt_cycle, " + " '0' db_bal " 
        ;
    wp.daoTable = "bil_auto_tx as a join crd_card as b on a.card_no=b.card_no ";
    wp.whereStr = "where 1=1";
    wp.whereStr +=
        " and  a.card_no = :kk_card_no and a.reference_no = :kk_reference_no and a.authorization = :kk_authorization ";
    setString("kk_card_no", kkCardNo);
    setString("kk_reference_no", kkReferenceNo);
    setString("kk_authorization", kkAuthorization);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, card_no=" + kkCardNo);
    } else {
      wp.colSet("disabl", "readonly");
    }
    lsMchtNo = itemKk("data_k4");
    if (wp.colStr("mcht_flag").equals("N")) {
      wp.colSet("wk_mcht_no", wp.colStr("mcht_no"));
    }
    dddwLiist();
    wfGetParm(wp.colStr("mcht_no_parm"),String.format("%02d", Integer.parseInt(wp.colStr("tot_term"))), 0);

  }

  @Override
  public void saveFunc() throws Exception {
    String lsRefNo = "", lsCurrCode = "", lsCardNo = "", sBinno = "", llLineOfCreditAmt = "",
        llDbBal, llAmt = "", lsMerchantNo = "", liTotTerm = "";
    if (!strAction.equals("D")) {
      if (empty(wp.itemStr("card_no")) || empty(wp.itemStr("mcht_no_parm"))
          || empty(wp.itemStr("tot_term")) 
          // || empty(wp.itemStr("mcht_no"))   //消費特店代號允許空白
          || empty(wp.itemStr("dest_amt")) || empty(wp.itemStr("purchase_date"))) {
        alertErr("下列為必輸欄位:分期特店代號,分期期數,消費金額,消費日期,卡號");
        return;
      }
    }
    if (!empty(wp.itemStr("apr_date_1"))) {
      alertErr("主管已覆核不可修改");
      return;
    }
    if (wp.itemStr("post_flag").equals("Y")) {
      alertErr("已匯入合約檔分期, 不可異動");
      return;
    }
    lsRefNo = wp.itemStr("reference_no");
    String sql1 = " select curr_code from bil_bill where reference_no =:ls_ref_no ";
    setString("ls_ref_no", lsRefNo);
    sqlSelect(sql1);
    lsCurrCode = sqlStr("curr_code");
    if (sqlRowNum > 0) {
      if (empty(lsCurrCode)) {
        if (!lsCurrCode.equals("901") && !lsCurrCode.equals("TWD")) {
          alertErr("結算幣別非台幣, 不可分期");
          return;
        }
      }
    }
    String sql2 = "select post_flag,close_flag from bil_auto_tx where reference_no = :ls_ref_no ";
    setString("ls_ref_no", lsRefNo);
    sqlSelect(sql2);
    if (strAction.equals("U") || strAction.equals("D")) {
      if (sqlStr("post_flag").equals("Y") && sqlStr("close_flag").equals("Y")) {
        alertErr("該筆資料已入合約檔 不可修改刪除");
        return;
      }
    }
    if (wp.itemStr("destination_amt_flag").equals("N")
        || wp.itemStr("payment_rate_flag").equals("N")
        || wp.itemStr("rc_rate_flag").equals("N") || wp.itemStr("mcc_code_flag").equals("N")
        || wp.itemStr("over_credit_amt_flag").equals("N")
        || wp.itemStr("block_reason_flag").equals("N") 
        || wp.itemStr("spec_status_flag").equals("N")
        || wp.itemStr("trial_status_flag").equals("N")) {
      alertErr("條件內含'N'者 無法分期 !!");
      return;
    }
    lsCardNo = wp.itemStr("card_no");
    if (strAction.equals("A")) {
      lsCardNo = wp.itemStr("kk_card_no");
    }
    if (empty(lsCardNo)) {
      alertErr("卡號不可空白 !!");
      return;
    } else {
      if (lsCardNo.length() >= 6) {
        sBinno = strMid(lsCardNo, 0, 6);
        String sql3 =
            "select count(*) as L_cnt from ptr_bintable where bin_no = :s_binno and debit_flag='Y'";
        setString("s_binno", sBinno);
        sqlSelect(sql3);

        if (this.toNum(sqlStr("L_cnt")) > 0) {
          alertErr("Visa 金融卡不可自動分期 !!");
          return;
        }
      }
    }
    String sql4 = "select current_code from	crd_card where card_no =:ls_card_no ";
    setString("ls_card_no", lsCardNo);
    sqlSelect(sql4);
    if (sqlRowNum <= 0) {
      alertErr("卡號不存在");
      return;
    }
    if (!sqlStr("current_code").equals("0")) {
      alertErr("非流通卡");
      return;
    }
    if (this.toNum(wp.itemStr("dest_amt")) <= 0) {
      alertErr("消費金額 需大於 0");
      return;
    }
    if (this.toNum(wp.itemStr("tot_term")) <= 0) {
      alertErr("未指定 分期期數; 期數需大於 0");
      return;
    }
    llLineOfCreditAmt = wp.itemStr("line_of_credit_amt");
    llDbBal = wp.itemStr("db_bal");
    llAmt = wp.itemStr("destination_amt");
    if (empty(lsRefNo)) {
      if (toNum(llDbBal) + toNum(llAmt) > toNum(llLineOfCreditAmt)) {
        alertErr("[超額] 此筆消費金額 + 已用額度 大於總額度");
        return;
      }
    }

    if (this.toNum(llDbBal) > this.toNum(llLineOfCreditAmt)) {
      alertErr("金額 大於額度 錯誤~ !!");
      return;
    }
    lsMerchantNo = wp.itemStr("mcht_no_parm");
    liTotTerm = wp.itemStr("tot_term");
    if (strAction.equals("A") || strAction.equals("U")) {
      if (lsMerchantNo.equals("0022200030") && (liTotTerm.equals("6") || liTotTerm.equals("9"))) {
        alertErr("本特店代號僅適用分3期，6、9期請用0022200033鍵檔");
        return;
      }
      if (lsMerchantNo.equals("0025800030") && (liTotTerm.equals("6") || liTotTerm.equals("9"))) {
        alertErr("(6,9)期之分期, 請改登分期特店代號 0025800031");
        return;
      }
    }
    
    Bilm0720Func func = new Bilm0720Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_mcht_parm");
      wp.initOption = "--";
      this.dddwList("dddw_merchant_auto", "bil_auto_parm", "mcht_no", "action_desc",
          "where 1=1 and ( effc_date_b <= to_char(sysdate,'yyyymmdd') ) and ( effc_date_e >= to_char(sysdate,'yyyymmdd') ) ");
      wp.optionKey = wp.colStr("mcht_no_parm");
      wp.initOption = "--";
      
      String sql1  = "select mcht_no AS db_code ,mcht_no||'_'||mcht_chi_name AS db_desc from bil_merchant ";
             sql1 += " where mcht_no in (select mcht_no from bil_auto_parm where 1=1 and ( effc_date_b <= to_char(sysdate,'yyyymmdd') ) and ( effc_date_e >= to_char(sysdate,'yyyymmdd') ) )  ";
      this.dddwList("dddw_merchant_auto2", sql1 );
      
      //this.dddwList("dddw_merchant_auto2", "bil_auto_parm", "mcht_no", "action_desc",
      //    "where 1=1 and ( effc_date_b <= to_char(sysdate,'yyyymmdd') ) and ( effc_date_e >= to_char(sysdate,'yyyymmdd') ) ");

      String lsMchtNoParm = wp.colStr("mcht_no_parm");
      wp.initOption = "--";
      String totTerm = wp.colStr("tot_term");
      if (!empty(totTerm) && isNumber(totTerm) && totTerm.length() < 2) {
        totTerm = "0" + totTerm;
      }
      wp.optionKey = totTerm;
      StringBuffer dddwWhere = new StringBuffer();
      dddwWhere.append(" where 1=1 and mcht_no = '");
      dddwWhere.append(lsMchtNoParm);
      dddwWhere.append("' order by product_no ");
      if (!empty(lsMchtNoParm)) {
        this.dddwList("dddw_productno_in", "bil_prod", "product_no",
            "product_no||'_'||product_name||'('||mcht_no||')'", dddwWhere.toString());
        // dddw_where.delete(0, dddw_where.length());
        // dddw_where.append(" where 1=1 order by product_no ");
      }


    } catch (Exception ex) {
    }
  }

  public void selectDate() throws Exception {
    this.selectNoLimit();
    String lsCloseDate = "";
    String lsLastCloseDate = "";
    cardNoKk1 = wp.itemStr("card_no");
    if (empty(cardNoKk1)) {
      alertErr("未輸入 卡號");
      return;
    }
    String sql = "  select next_close_date,last_close_date " + " from ptr_workday "
        + " where 1=1 and stmt_cycle in (select stmt_cycle from crd_card where card_no =:is_card_no)";
    setString("is_card_no", cardNoKk1);
    sqlSelect(sql);
    lsCloseDate = sqlStr("next_close_date");
    lsLastCloseDate = sqlStr("last_close_date");
    if (!empty(lsCloseDate)) {
      Date date = format.parse(lsCloseDate);
      cal.setTime(date);
      cal.add(Calendar.DATE, -3);
      lsCloseDate = format.format(cal.getTime());
    }
    if (sqlRowNum <= 0) {
      alertErr("查無卡號之下次關帳日期");
      return;
    }
    daoTid = "P-";
    wp.selectSQL = "hex(rowid) as rowid1 " + ", card_no" + ", auth_code" + ", mcht_category"
        + ", purchase_date" + ", reference_no" + ", curr_code" + ", mcht_no " + ", mcht_chi_name "
        + ", mcht_no||'_'||mcht_chi_name as wk_mcht_id_cname"
        + ", decode(cash_pay_amt, 0, dest_amt, cash_pay_amt) as db_dest_amt";
    wp.daoTable = "bil_bill";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  card_no = :card_no and post_date > :ls_last_close_date "
        + "	and txn_code = '05' and acct_code = 'BL' and dest_amt >= 3000 "
        + " and installment_kind = '' "
        + " and curr_code = '901' "  //只有台幣的交易可以做分期
        + " and rsk_type not in('1','2','3')";
    setString("card_no", cardNoKk1);
    setString("ls_last_close_date", lsLastCloseDate);
    pageQuery();
    if (sqlNotFind()) {
      alertErr("查無消費紀錄");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    wp.colSet("queryReadCnt", wp.selectCnt);
  }


  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    String lsDate1 = wp.itemStr("ex_tx_date1");
    String lsDate2 = wp.itemStr("ex_tx_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    if (empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_tx_date1"))
        && empty(wp.itemStr("ex_tx_date2"))) {
      alertErr2("請輸入 卡號 OR 登錄日期");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    // -cond-
    String cond1 = "";
    switch (wp.itemStr("ex_apr_flag")) {
      case "0":
        cond1 = "全部";
        break;
      case "N":
        cond1 = "未完成分期";
        break;
      case "Y":
        cond1 = "已完成分期";
        break;
    }

    cond1 += " 卡號: " + wp.itemStr("ex_card_no");

    cond1 += " 登錄日期: " + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_tx_date2"));

    cond1 += " 結帳日: " + wp.itemStr("ex_stmt_cycle");

    cond1 += " 特店代號: " + wp.itemStr("ex_mcht_parm");

    wp.colSet("cond_1", cond1);
    wp.colSet("loginUser", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  // 20200106 modify AJAX
  public int itemchanged() throws Exception {
    // super.wp = wr;
    String lsMerchantNo = "", llLineOfCreditAmt = "", llDbBal = "", llAmt = "";
    name = wp.itemStr("name");
    switch (name) {
      case "merchant_no_parm":
        lsMerchantNo = wp.itemStr("data_mcht_no");
        
        //加期數欄位, 在選期數的地方執行
        //if (wfGetParm(lsMerchantNo, 1) != 1) {
        //  alertErr("未設定 卡友來電自動分期參數 1 !!");
        //  return -1;
        //}
        
        String lsMchtNoParm = lsMerchantNo;
        String dddwWhere = "", option = "";

        dddwWhere = " and mcht_no = :ls_mcht_no_parm ";
        String selectBilProd = "select product_no "
            + " ,product_no||'_'||product_name||'('||mcht_no||')' as product_name "
            + " from bil_prod " + " where 1=1 " + dddwWhere + " order by product_no ";
        setString("ls_mcht_no_parm", lsMchtNoParm);
        sqlSelect(selectBilProd);
        if (sqlRowNum <= 0) {
          break;
        }
        option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "product_no") + "\" ${tot_term-"
              + sqlStr(ii, "product_no") + "} >" + sqlStr(ii, "product_name") + "</option>";
        }
        wp.addJSON("dddw_productno_in2", option);
        break;
      case "dest_amt":
        llLineOfCreditAmt = wp.itemStr("line_of_credit_amt");
        llDbBal = wp.itemStr("db_bal");
        llAmt = wp.itemStr("data_dest_amt");

        wp.addJSON("dest_amt2", llAmt);
        if (!wp.itemStr("over_credit_amt_flag").equals("X")) {
          if (this.toNum(llDbBal) + this.toNum(llAmt) > this.toNum(llLineOfCreditAmt)) {
            alertErr("此筆消費金額(" + this.toNum(llAmt) + ") + 已用額度(" + this.toNum(llDbBal) + ") 大於總額度("
                + this.toNum(llLineOfCreditAmt) + ") 錯誤~ !!");
            wp.addJSON("destination_amt_flag", wp.itemStr("destination_amt_flag"));
            wp.addJSON("dest_amt_ok", "N");
            return -1;
          }
        }
        double lsDestinationAmtParm = wp.itemNum("destination_amt_parm");
        String destinationAmtFlag = wp.itemStr("destination_amt_flag");
        if (destinationAmtFlag.equals("C") || destinationAmtFlag.equals("N")
            || destinationAmtFlag.equals("Y")) {
          if (this.toNum(llAmt) >= lsDestinationAmtParm) {
            wp.addJSON("destination_amt_flag", "Y");
          } else if (this.toNum(llAmt) == 0) {
            wp.addJSON("destination_amt_flag", "C");
          } else {
            wp.addJSON("destination_amt_flag", "N");
          }
        } else {
          wp.addJSON("destination_amt_flag", wp.itemStr("destination_amt_flag"));
        }
        break;
      case "kk_card_no":
        if (empty(wp.itemStr("rowid"))) {
          isCardNo = wp.itemStr("data_kk_card_no");
        } else if (empty(isCardNo)) {
          isCardNo = wp.itemStr("card_no");
        }

        wp.addJSON("card_no", isCardNo);
        break;

      case "tot_term":
    	  
    	  lsMerchantNo = wp.itemStr("mcht_no_parm");
    	  log("lsMerchantNo=" + lsMerchantNo);
    	  String lsx02 = wp.itemStr("data_tot_term");
    	  log("lsx02=" + lsx02);
    	  
    	  if (wfGetParm(lsMerchantNo,lsx02, 1) != 1) {
              alertErr("未設定 卡友來電自動分期參數 1 !!");
              return -1;
           }

    	  /*
    	  if (!empty(lsMerchantNo))  {
            dddwWhere = " and mcht_no = :ls_mcht_no_parm ";
            
            selectBilProd = "select product_no "
            		+ " ,product_no||'_'||product_name||'('||mcht_no||')' as product_name "
            		+ " from bil_prod " + " where 1=1 " + dddwWhere + " order by product_no ";
            setString("ls_mcht_no_parm", lsMerchantNo);
            sqlSelect(selectBilProd);
            if (sqlRowNum <= 0) {
            	break;
            }
    	  }
            
    	  String option1 = "";
    	  option1 += "<option value=\"\">--</option>";
          for (int ii = 0; ii < sqlRowNum; ii++) {
            option1 += "<option value=\"" + sqlStr(ii, "product_no") + "\" ${tot_term-"
                + sqlStr(ii, "product_no") + "} >" + sqlStr(ii, "product_name") + "</option>";
          }
          wp.addJSON("dddw_productno_in2", option1);
          */
    	  wp.addJSON("dddw_productno_refresh", "Y");
          
          String sql = "select count(*) as li_cnt from bil_prod  where product_no  = lpad(:ls_x02 , 2 ,'0') and mcht_no = :ls_merchant_no";
          setString("ls_x02", lsx02);
          setString("ls_merchant_no", lsMerchantNo);

          sqlSelect(sql);

          if (this.toNum(sqlStr("li_cnt")) < 1) {
        	  alertErr("期數 錯誤~ !!");
              return -1;
          }
              
          String sql1 = "select count(*) as li_cnt from bil_auto_parm where product_no  = :ls_x02 and mcht_no = :ls_merchant_no";
              
          if(lsx02.substring(0,1).equals("0")) {
        	  String lsx03 = lsx02.substring(1,2); 
              lsx02 = lsx03.concat(".0");
          }

          setString("ls_x02", lsx02);
          setString("ls_merchant_no", lsMerchantNo);
              
          sqlSelect(sql1);
          if (this.toNum(sqlStr("li_cnt")) < 1) {
        	  alertErr("分期期數在bil_auto_parm不存在");
              return -1;
          }
              
          wfCheckParm();
          
          break;
    }

    return 1;
  }

  @Override
  public void initPage() {
    wp.colSet("sale_emp_no", wp.loginUser);
    wp.colSet("dest_amt", "0");
    wp.colSet("destination_amt_parm", "0");
    wp.colSet("destination_amt_parm", "0");
    wp.colSet("line_of_credit_amt", "0");
    wp.colSet("db_bal", "0");
    wp.colSet("crt_user", wp.loginUser);
    wp.colSet("tx_date", wp.sysDate);
    if (wp.respHtml.indexOf("_detl") > 0) {
      String sqlSelect =
          "select usr_id|| '[' || usr_cname || ']' as usr_cname from sec_user where usr_id = :loginUser ";
      setString("loginUser", wp.loginUser);
      sqlSelect(sqlSelect);
      String usrCname = sqlStr("usr_cname");
      if (sqlRowNum <= 0) {
        usrCname = wp.loginUser;
      }
      wp.colSet("usr_cname", usrCname);
    }
  }

  public int wfGetParm(String asMerchantNo, String totTerm, int x) throws Exception {

    if (empty(asMerchantNo)) {
      return 0;
    }

    String sql1 =
        "select action_desc," + "decode(destination_amt_flag,'N','X','C') as destination_amt_flag, "
            + "destination_amt, " + "decode(payment_rate_flag,'N','X','C') as payment_rate_flag, "
            + "payment_rate, " 
            + "decode(rc_rate_flag,'N','X','C') as rc_rate_flag, " + "rc_rate, "
            + "credit_amt_rate, " + "decode(mcc_code_flag,'N','X','C') as mcc_code_flag, "
            + "decode(over_credit_amt_flag,'N','X','C') as over_credit_amt_flag, "
            + "decode(block_reason_flag,'N','X','C') as block_reason_flag, "
            + "decode(spec_status_flag,'N','X','C') as spec_status_flag, "
            + "decode(mcht_flag,'N','X','C') as mcht_flag " 
            + " from bil_auto_parm where mcht_no = :as_merchant_no and product_no = :as_product_no ";
    setString("as_merchant_no", asMerchantNo);
    setString("as_product_no", totTerm);
    
    this.sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr("未設定 卡友來電自動分期參數 0");
      return -1;
    }
    if (x == 1) {
      wp.addJSON("action_desc", sqlStr("action_desc"));
      wp.addJSON("mcht_parm", asMerchantNo);
      wp.addJSON("destination_amt_flag", sqlStr("destination_amt_flag"));
      wp.addJSON("destination_amt_parm", sqlStr("destination_amt"));
      wp.addJSON("payment_rate_flag", sqlStr("payment_rate_flag"));
      wp.addJSON("payment_rate_term", sqlStr("payment_rate"));
      wp.addJSON("rc_rate_flag", sqlStr("rc_rate_flag"));
      wp.addJSON("rc_rate_parm", sqlStr("rc_rate"));
      wp.addJSON("credit_amt_rate_parm", sqlStr("credit_amt_rate"));
      wp.addJSON("mcc_code_flag", sqlStr("mcc_code_flag"));
      wp.addJSON("over_credit_amt_flag", sqlStr("over_credit_amt_flag"));
      wp.addJSON("block_reason_flag", sqlStr("block_reason_flag"));
      wp.addJSON("spec_status_flag", sqlStr("spec_status_flag"));
      wp.addJSON("mcht_flag", sqlStr("mcht_flag"));
      wp.addJSON("payment_rate", sqlStr("payment_rate"));
    } else {
      wp.colSet("action_desc", sqlStr("action_desc"));
      wp.colSet("mcht_parm", asMerchantNo);
      wp.colSet("destination_amt_flag", sqlStr("destination_amt_flag"));
      wp.colSet("destination_amt_parm", sqlStr("destination_amt"));
      wp.colSet("payment_rate_flag", sqlStr("payment_rate_flag"));
      wp.colSet("payment_rate_term", sqlStr("payment_rate"));
      wp.colSet("rc_rate_flag", sqlStr("rc_rate_flag"));
      wp.colSet("rc_rate_parm", sqlStr("rc_rate"));
      wp.colSet("credit_amt_rate_parm", sqlStr("credit_amt_rate"));
      wp.colSet("mcc_code_flag", sqlStr("mcc_code_flag"));
      wp.colSet("over_credit_amt_flag", sqlStr("over_credit_amt_flag"));
      wp.colSet("block_reason_flag", sqlStr("block_reason_flag"));
      wp.colSet("spec_status_flag", sqlStr("spec_status_flag"));
      wp.colSet("mcht_flag", sqlStr("mcht_flag"));
      wp.colSet("payment_rate", sqlStr("payment_rate"));
      // istr_auto.
      wp.colSet("istr_auto_mcht_flag", sqlStr("istr_auto_mcht_flag"));
      wp.colSet("istr_auto_payment_rate_flag", sqlStr("istr_auto_payment_rate_flag"));
      wp.colSet("istr_auto_payment_rate", sqlStr("istr_auto_payment_rate"));
    }
    return 1;
  }

  public int wfCheckParm() throws Exception {

    String sBinno = "", lsMajorCardNo = "", lsMchtParm = "", lsMchtNo = "", lsAcnoPSeqno = "",
        lsPSeqno = "", lsPdRate = "", lsIdPSeqno = "", lsRcRate = "", lsLastAcctMm = "",
        lsCreditAmtRate = "", lsMerchantCategory = "", lsSpecStatus = "", lsSpecDelDate = "",
        lsRecheckDay = "", lsBlock1 = "", lsBlock2 = "", lsBlock3 = "", lsBlock4 = "",
        lsBlock5 = "", lsBlock6 = "", lsProductNo="";
    double lsRCParm = 0, lsAmtParm = 0, ldA = 0, ldB = 0, ldAmt = 0, ldLineOfCreditAmt = 0,
        ldAcctJrnlBal = 0;
    isCardNo = wp.itemStr("card_no");

    isStmtCycle = "";
    if (empty(isCardNo)) {
      return 0;
    }

    if (isCardNo.length() < 15) {
      alertErr("卡號輸入錯誤~");
      return -1;
    }
    if (isCardNo.length() >= 6) {
      sBinno = strMid(isCardNo, 0, 6);
      String sql3 =
          "select count(*) as L_cnt from ptr_bintable where bin_no = :s_binno and debit_flag='Y'";
      setString("s_binno", sBinno);
      sqlSelect(sql3);

      if (this.toNum(sqlStr("L_cnt")) > 0) {
        alertErr("Visa 金融卡不可自動分期 !!");
        return -1;
      }
    }

    String sql1 = "select major_card_no,acno_p_seqno from crd_card where card_no = :is_card_no";
    setString("is_card_no", isCardNo);
    sqlSelect(sql1);
    lsMajorCardNo = sqlStr("major_card_no");
    String acnoPSeqno = sqlStr("acno_p_seqno");
    if (sqlRowNum <= 0) {
      alertErr("major_card_no error");
      return -1;
    }

    // 取得該卡號與卡人等相關資料
    String sql2 = "select a.current_code, " + "a.p_seqno, " + "a.acct_type, " + "a.acno_p_seqno, "
        + "a.id_p_seqno,"
        // + "b.block_reason1 as block_reason, "
        // + "b.block_reason2||b.block_reason3||b.block_reason4||b.block_reason5 as block_reason2, "
        + "a.stmt_cycle "
        // + "a.SPEC_STATUS, "
        // + "a.spec_del_date "
        + "from crd_card as a " + "where a.card_no = :ls_major_card_no ";

    setString("ls_major_card_no", lsMajorCardNo);
    sqlSelect(sql2);
    if (sqlRowNum <= 0) {
      alertErr("卡號不存在1");
      return -1;
    }

    lsAcnoPSeqno = sqlStr("acno_p_seqno");
    lsPSeqno = sqlStr("p_seqno");
    lsIdPSeqno = sqlStr("id_p_seqno");
    isStmtCycle = sqlStr("stmt_cycle");
    lsMchtParm = wp.itemStr("mcht_no_parm");
    lsProductNo = wp.itemStr("tot_term");
    lsMchtNo = wp.itemStr("mcht_no");
    String lsAcctType = sqlStr("ls_acct_type");

    sql2 = "select spec_status,spec_del_date from cca_card_base where card_no = :ls_major_card_no";
    setString("ls_major_card_no", lsMajorCardNo);
    sqlSelect(sql2);
    lsSpecStatus = sqlStr("spec_status");
    lsSpecDelDate = sqlStr("spec_del_date");

    // 單筆消費門檻

    if (wp.itemStr("destination_amt_flag").equals("C")
        || wp.itemStr("destination_amt_flag").equals("N")
        || wp.itemStr("destination_amt_flag").equals("Y")) {
      wp.colSet("destination_amt_flag", "N");
      if (this.toNum(wp.itemStr("dest_amt")) >= this.toNum(wp.itemStr("destination_amt_parm"))) {
        wp.colSet("destination_amt_flag", "Y");
      } else if (this.toNum(wp.itemStr("dest_amt")) == 0) {
        wp.colSet("destination_amt_flag", "C");
      }
    }
    // 繳款記錄

    wfChkPaymentRate(lsAcnoPSeqno);

    // 循環信用比率
    // 額度使用比率
    if (wp.itemStr("rc_rate_flag").equals("C")) {
      String sql5 =
          "select last_acct_month from ptr_workday  where stmt_cycle in (select stmt_cycle from act_acno where acno_p_seqno = :ls_acno_p_seqno)";
      setString("ls_acno_p_seqno", lsAcnoPSeqno);
      sqlSelect(sql5);

      lsLastAcctMm = sqlStr("last_acct_month");
      if (sqlRowNum <= 0) {
        alertErr("select ptr_workday error");
        return -1;
      }
      lsRcRate = "0";
      String sql6 =
          "select to_char(cast(his_rc_percentage as double)/100,'0.90') as ls_rc_rate from act_anal_sub where p_seqno = :ls_p_seqno and acct_month = :ls_last_acct_mm ";
      setString("ls_p_seqno", lsPSeqno);
      setString("ls_last_acct_mm", lsLastAcctMm);
      sqlSelect(sql6);

      lsRcRate = sqlStr("ls_rc_rate");
      String sql7 =
          "select to_char(acct.acct_jrnl_bal / acno.line_of_credit_amt,'0.90') as ls_credit_amt_rate "
              + "from act_acct as acct, act_acno as acno " + "where acct.p_seqno = :ls_p_seqno "
              + "and acno.acno_p_seqno = :ls_acno_p_seqno "
              + "and acno.acno_p_seqno = acct.p_seqno ";
      setString("ls_p_seqno", lsPSeqno);
      setString("ls_acno_p_seqno", lsAcnoPSeqno);
      sqlSelect(sql7);
      lsCreditAmtRate = sqlStr("ls_credit_amt_rate");
      if (sqlRowNum <= 0) {
        alertErr("select act_acct,act_acno error");
        return -1;
      }
      // 溢繳歸0
      if (this.toNum(lsCreditAmtRate) < 0) {
        lsCreditAmtRate = "0";
      }

      wp.colSet("rc_rate", this.toInt(lsRcRate) + "");
      wp.colSet("credit_amt_rate", lsCreditAmtRate);
      ldA = this.toNum(lsRcRate);
      ldB = this.toNum(lsCreditAmtRate);
      lsRCParm = (this.toNum(wp.itemStr("rc_rate_parm"))) / 100;
      lsAmtParm = (this.toNum(wp.itemStr("credit_amt_rate_parm"))) / 100;
      if (ldA <= lsRCParm || ldB <= lsAmtParm) {
        wp.colSet("rc_rate_flag", "Y");
      } else {
        wp.colSet("rc_rate_flag", "N");
      }
    }

    // 排除MC CODE
    if (wp.itemStr("mcc_code_flag").equals("C") || wp.itemStr("mcc_code_flag").equals("Y")) {
      lsMerchantCategory = wp.itemStr("mcht_category");
      String sql8 =
          "select count(*) as li_count from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type = '05' and data_code = :ls_merchant_category";
      setString("ls_mcht_parm", lsMchtParm);
      setString("ls_product_no", lsProductNo);
      setString("ls_merchant_category", lsMerchantCategory);
      sqlSelect(sql8);
      if (sqlRowNum <= 0) {
        alertErr("select bil_auto_parm_data error 05");
        return -1;
      }
      if (sqlStr("li_count").equals("0")) {
        wp.colSet("mcc_code_flag", "Y");
      } else {
        wp.colSet("mcc_code_flag", "N");
      }
    }

    // -jh:R104027:排除特店-
    if ((wp.itemStr("istr_auto_mcht_flag").equals("C") || wp.itemStr("mcht_flag").equals("Y"))
        && !empty(lsMchtNo)) {
      String sql9 =
          "select count(*) as li_count from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type = '10' and data_code = :ls_mcht_no";
      setString("ls_mcht_parm", lsMchtParm);
      setString("ls_product_no", lsProductNo);
      setString("ls_mcht_no", lsMchtNo);
      sqlSelect(sql9);
      if (this.toNum(sqlStr("li_count")) > 0) {
        wp.colSet("mcht_flag", "N");
      } else {
        wp.colSet("mcht_flag", "Y");
      }
    }

    // 排除超額
    if (wp.itemStr("over_credit_amt_flag").equals("C")
        || wp.itemStr("over_credit_amt_flag").equals("Y")) {
      String sql10 = "select acno.card_indicator " + ",acno.line_of_credit_amt "
          + ",acct.end_bal_op " + ",acct.end_bal_lk " + "from act_acct as acct, act_acno as acno "
          + "where acct.p_seqno = :ls_p_seqno " + "and acno.acno_p_seqno = :ls_acno_p_seqno "
          + "and acno.acno_p_seqno = acct.p_seqno";
      setString("ls_p_seqno", lsPSeqno);
      setString("ls_acno_p_seqno", lsAcnoPSeqno);

      sqlSelect(sql10);
      ldLineOfCreditAmt = this.toNum(sqlStr("line_of_credit_amt"));
      String lsCardIndicator = sqlStr("card_indicator");
      double ldEndBalOp = sqlNum("end_bal_op");
      double ldEndBalLk = sqlNum("end_bal_lk");
      if (sqlRowNum <= 0) {
        alertErr("select act_acct,act_acno error2");
        return -1;
      }
      String sqlSelect = "";
      if (lsCardIndicator.equals("1")) {
        sqlSelect =
            "select sum(decode (acct_code, 'ID',unbill_end_bal_m2 + billed_end_bal_m2,unbill_end_bal + billed_end_bal)) as ld_acct_jrnl_bal "
                + "from act_acct_sum "
                + "where acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') and p_seqno = :ls_p_seqno ";
        setString("ls_p_seqno", lsPSeqno);
        sqlSelect(sqlSelect);
        ldAcctJrnlBal = sqlInt("ld_acct_jrnl_bal");
      } else {
        sqlSelect = "select sum(unbill_end_bal+billed_end_bal) as ld_acct_jrnl_bal "
            + "from act_acct_sum "
            + "where acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') and p_seqno = :ls_p_seqno ";
        setString("ls_p_seqno", lsPSeqno);
        sqlSelect(sqlSelect);
        ldAcctJrnlBal = sqlInt("ld_acct_jrnl_bal");
      }
      if (sqlRowNum <= 0) {
        alertErr("SELECT act_acct_sum error");
        return -1;
      }
      // bil_auto_tx
      ldAmt = 0;
      String sql11 =
          "select sum(dest_amt) as ld_amt from bil_auto_tx where card_no = :ls_major_card_no and reference_no =''";
      setString("ls_major_card_no", lsMajorCardNo);
      sqlSelect(sql11);
      ldAmt = sqlNum("ld_amt");

      // -分期未billing-
      double lmUnbillAmt = 0, lmUnbillFee = 0;
      sqlSelect = " select sum(nvl(a.unit_price,0)*(nvl(a.install_tot_term,0)- "
          + " nvl(a.install_curr_term,0))+nvl(a.remd_amt,0)+ "
          + " decode(a.install_curr_term,0,nvl(a.first_remd_amt,0)+nvl(a.extra_fees,0),0)) as lm_unbill_amt, "
          + " sum(nvl(a.clt_unit_price,0)*(nvl(a.clt_install_tot_term,0)- "
          + " nvl(a.install_curr_term,0))+nvl(a.clt_remd_amt,0)) as lm_unbill_fee "
          + " FROM bil_contract a, act_acno b " + " WHERE a.acct_type = b.acct_type "
          + " AND a.acno_p_seqno = b.acno_p_seqno " + " AND b.acno_p_seqno = :ls_acno_p_seqno "
          + " AND a.install_tot_term != a.install_curr_term "
          + " AND a.auth_code not in ('','N','REJECT','P','reject') ";
      setString("ls_acno_p_seqno", lsAcnoPSeqno);
      sqlSelect(sqlSelect);
      lmUnbillAmt = sqlNum("lm_unbill_amt");
      lmUnbillFee = sqlNum("lm_unbill_fee");
      // cps_inst_fee
      sqlSelect =
          "select sum((install_resp_63_5 * install_resp_63_3)+ install_resp_63_4 + install_resp_63_6) as lm_cps_inst_fee "
              + " from cps_install_log " + " WHERE acct_type = :ls_acct_type "
              + " AND p_seqno = :ls_p_seqno " + " and mod_pgm = 'CpsA201' "
              + " and resp_flag_39 = '00' " + " and (apr_flag = 'N' or apr_flag = '') "
              + " and tx_date>to_char(add_months(sysdate,-24),'yyyymmdd') ";
      setString("ls_acct_type", lsAcctType);
      setString("ls_p_seqno", lsPSeqno);
      double lmCpsInstFee = sqlNum("lm_cps_inst_fee");

      // bil_inst_fee
      sqlSelect =
          "select sum((install_resp_63_5 * install_resp_63_3)+ install_resp_63_4 + install_resp_63_6) as lm_bil_inst_fee "
              + " from bil_install_log " + " WHERE acct_type = :ls_acct_type "
              + " AND p_seqno = :ls_p_seqno " + " and mod_pgm = 'BilO201' "
              + " and resp_flag_39 = '00' " + " and (reversal_flag = 'N' or reversal_flag = '') "
              + " and (refund_flag = 'N' or refund_flag = '') "
              + " and (apr_flag = 'N' or apr_flag = '') "
              + " and tx_date>to_char(add_months(sysdate,-24),'yyyymmdd') ";
      setString("ls_acct_type", lsAcctType);
      setString("ls_p_seqno", lsPSeqno);
      double lmBilInstFee = sqlNum("lm_bil_inst_fee");

      wp.colSet("line_of_credit_amt", ldLineOfCreditAmt + "");
      wp.colSet("acct_jrnl_bal", ldAcctJrnlBal + "");
      wp.colSet("db_bal",
          (ldAcctJrnlBal + ldAmt + lmUnbillAmt + lmUnbillFee + lmCpsInstFee + lmBilInstFee) + "");


      if (ldLineOfCreditAmt >= (ldAcctJrnlBal + ldAmt + lmUnbillAmt + lmUnbillFee + ldEndBalOp
          + ldEndBalLk + lmCpsInstFee + lmBilInstFee)) {
        wp.colSet("over_credit_amt_flag", "Y");
      } else {
        wp.colSet("over_credit_amt_flag", "N");
      }
    }

    // 排除特指戶
    if (wp.itemStr("spec_status_flag").equals("C") || wp.itemStr("spec_status_flag").equals("Y")) {
      if (!empty(lsSpecStatus)) {
    	  wp.colSet("spec_status", lsSpecStatus);
    	  wp.colSet("spec_del_date", lsSpecDelDate);
    	  String sql12 =
    			  "select count(*) as li_count from bil_auto_parm_data where mcht_no = :ls_mcht_parm and data_type ='07' and product_no = :ls_product_no and data_code = decode(:ls_spec_status,'', data_code, :ls_spec_status2)";
    	  setString("ls_mcht_parm", lsMchtParm);
    	  setString("ls_product_no", lsProductNo);
    	  setString("ls_spec_status", lsSpecStatus);
    	  setString("ls_spec_status2", lsSpecStatus);
    	  sqlSelect(sql12);
    	  if (this.toNum(sqlStr("li_count")) > 0) {
    		  wp.colSet("spec_status_flag", "Y");
    	  } else {
    		  wp.colSet("spec_status_flag", "N");
    	  }
      } else {
    	  wp.colSet("spec_status_flag", "Y");
      }
    }

    // 排除凍結碼
    if (wp.itemStr("block_reason_flag").equals("C") || wp.itemStr("block_reason_flag").equals("Y")
        || wp.itemStr("block_reason_flag").equals("N")) {
      String sql14 = "select block_reason1" + ",block_reason2" + ",block_reason3" + ",block_reason4"
          + ",block_reason5"
          + ",block_reason2||block_reason3||block_reason4||block_reason5 as ls_block6 "
          + " from cca_card_acct " + " where acno_p_seqno = :acno_p_seqno and debit_flag = 'N' ";
      setString("acno_p_seqno", acnoPSeqno);
      sqlSelect(sql14);
      lsBlock1 = sqlStr("block_reason1");
      lsBlock2 = sqlStr("block_reason2");
      lsBlock3 = sqlStr("block_reason3");
      lsBlock4 = sqlStr("block_reason4");
      lsBlock5 = sqlStr("block_reason5");
      lsBlock6 = sqlStr("ls_block6");

      String sql15 = "select count(*) as li_count from dual "
          + " where decode(:ls_block1,'','  ',:ls_block1) not in (select data_code from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type   = '06' union select '  ' from dual)"
          + " or  decode(:ls_block2,'','  ',:ls_block2) not in (select data_code from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type   = '06' union select '  '   from dual)"
          + " or  decode(:ls_block3,'','  ',:ls_block3) not in (select data_code from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type   = '06' union select '  '   from dual)"
          + " or  decode(:ls_block4,'','  ',:ls_block4) not in (select data_code from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type   = '06' union select '  '   from dual)"
          + " or  decode(:ls_block5,'','  ',:ls_block5) not in (select data_code from bil_auto_parm_data where mcht_no = :ls_mcht_parm and product_no = :ls_product_no and data_type   = '06' union select '  '   from dual)";
      setString("ls_block1", lsBlock1);
      setString("ls_block2", lsBlock2);
      setString("ls_block3", lsBlock3);
      setString("ls_block4", lsBlock4);
      setString("ls_block5", lsBlock5);
      setString("ls_mcht_parm", lsMchtParm);
      setString("ls_product_no", lsProductNo);

      sqlSelect(sql15);
      if (this.toNum(sqlStr("li_count")) > 0) {
        wp.colSet("block_reason_flag", "N");
      } else {
        wp.colSet("block_reason_flag", "Y");
      }
    }
    wp.colSet("block_reason", lsBlock1);
    wp.colSet("block_reason2", lsBlock6);

    return 1;
  }

  public int wfChkPaymentRate(String asAcnoPSeqno) throws Exception {
    String lsNull = "", ls0AE = "", ls02 = "", ss = "", mchtParm = "", productNo= "";
    String[] laPay = new String[25];
    int liItem = 0;

    if (!wp.itemStr("istr_auto_payment_rate_flag").equals("C")
        && !wp.itemStr("istr_auto_payment_rate_flag").equals("Y")) {
      return 0;
    }
    String sql1 = "select payment_rate1, payment_rate2,payment_rate3"
        + ", payment_rate4, payment_rate5, payment_rate6"
        + ", payment_rate7, payment_rate8, payment_rate9"
        + ", payment_rate10, payment_rate11, payment_rate12"
        + ", payment_rate13, payment_rate14, payment_rate15"
        + ", payment_rate16, payment_rate17, payment_rate18"
        + ", payment_rate19, payment_rate20, payment_rate21"
        + ", payment_rate22, payment_rate23, payment_rate24"
        + ", payment_rate25 from act_acno where acno_p_seqno = :as_acno_p_seqno";
    setString("as_acno_p_seqno", asAcnoPSeqno);
    sqlSelect(sql1);

    for (int i = 0; i < 25; i++) {
      laPay[i] = sqlStr("payment_rate" + (i + 1));

    }

    if (sqlRowNum < 0) {
      alertErr("select act_acno error");
      return -1;
    }

    for (int i = 0; i < 25; i++) {
      wp.colSet("payment_rate" + (i + 1), laPay[i]);

    }

    wp.colSet("payment_rate_flag", "N");
    if (!pattern.matcher(wp.itemStr("istr_auto_payment_rate")).matches()) {
      alertErr("繳款記錄.期數 不為數字");
      return -1;
    }

    mchtParm = wp.itemStr("mcht_parm");
    productNo = wp.itemStr("tot_term");
    liItem = (int) this.toNum(wp.itemStr("istr_auto_payment_rate"));
    String sql2 =
        "select decode(data_code2,'','Y',data_code2) as ls_null from bil_auto_parm_data  where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and data_code ='' ";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql2);
    lsNull = sqlStr(0, "ls_null");
    if (sqlRowNum <= 0) {
      lsNull = "N";
    }
    ls0AE = "";
    String sql3 =
        "select '0A' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='0A'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql3);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    String sql4 =
        "select '0B' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='0B'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql4);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    String sql5 =
        "select '0C' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='0C'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql5);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    String sql6 =
        "select '0D' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='0D'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql6);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    String sql7 =
        "select '0E' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='0E'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql7);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    String sql8 =
        "select '01' as ss from bil_auto_parm_data where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' and nvl(data_code2,'Y')='Y' and data_code ='01'";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql8);
    ss = sqlStr(0, "ss");
    if (sqlRowNum > 0) {
      ls0AE += ss;
    }
    // -02+-
    ls02 = "N";
    String sql9 = "select count(*) as li_count from	bil_auto_parm_data "
        + " where mcht_no = :mcht_parm and product_no = :ls_product_no and data_type ='01' " + " and nvl(data_code2,'Y')='Y' "
        + " and data_code not in ('0A','0B','0C','0D','0E') "
        + " and data_code between '02' and '99' ";
    setString("mcht_parm", mchtParm);
    setString("ls_product_no", productNo);
    sqlSelect(sql9);

    if (this.toNum(sqlStr("li_count")) > 0) {
      ls02 = "Y";
    }

    for (int i = 0; i <= liItem; i++) {
      if (i > 25)
        break;
      // -NULL-
      ss = laPay[i];

      if (lsNull.equals("Y")) {
        if (!empty(ss)) {
          return 1;
        }
        if (empty(ss)) {
          continue;
        }
        // -0A~0E,01-

        if (ls0AE.indexOf(ss) >= 0) {
          return 1;
        }

        if (ls02.equals("Y") && pattern.matcher(ss).matches() == true) {
          if (this.toNum(ss) >= 2) {
            return 1;
          }
        }
      }
    }

    wp.colSet("payment_rate_flag", "Y");

    return 1;
  }


  void dddwLiist() throws Exception {

  }

  void changeTotterm() throws Exception {
    String lsMerchantNo = wp.itemStr("mcht_no_parm");

    String lsx02 = wp.itemStr("tot_term");
    
    String sql =
        "select count(*) as li_cnt from bil_prod  where product_no  = lpad(:ls_x02 , 2 ,'0') and mcht_no = :ls_merchant_no";
    setString("ls_x02", lsx02);
    setString("ls_merchant_no", lsMerchantNo);

    sqlSelect(sql);

    if (this.toNum(sqlStr("li_cnt")) < 1) {
      alertErr("期數 錯誤~ !!");
      return;
    }
    
    String sql1 = "select count(*) as li_cnt from bil_auto_parm where product_no  = :ls_x02 and mcht_no = :ls_merchant_no";
    
    if(lsx02.substring(0,1).equals("0")) {
        String lsx03 = lsx02.substring(1,2); 
        lsx02 = lsx03.concat(".0");
    }

    setString("ls_x02", lsx02);
    setString("ls_merchant_no", lsMerchantNo);
    
    sqlSelect(sql1);
    if (this.toNum(sqlStr("li_cnt")) < 1) {
        alertErr("分期期數在bil_auto_parm不存在");
        return;
      }
    
    wfCheckParm();
  }
}
