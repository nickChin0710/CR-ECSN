package colm05;
/** 整批凍結客戶明細表[含其它批次凍結]
 * 19-1129:   Alex  add set_selectLimit(9999);
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.02  Tanwei       updated for project coding standard
 * 109-12-07  V1.00.03   Justin        新增PDF查無資料時顯示錯誤
 * * 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
 * */
import ofcapp.BaseQuery;
import ofcapp.InfacePdf;

import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr5830 extends BaseQuery implements InfacePdf {
  int ii = -1;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("5830") > 0) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "param_type", "1")) {
        wp.colSet(ii, "param_type", "1.一般卡(個繳)");
      } else if (wp.colEq(ii, "param_type", "3")) {
        wp.colSet(ii, "param_type", "3.商務卡(總繳)");
      }
    }
  }


  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_exec_date1"))) {
      alertErr2("執行日期 (起) : 不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_exec_date1"), wp.itemStr("ex_exec_date2")) == false) {
      alertErr2("執行日期起迄：輸入錯誤");
      return;
    }


    wp.whereStr = " where param_type in ('1','3') and   acct_type <> '00' "
        + sqlCol(wp.itemStr("ex_exec_date1"), "exec_date", ">=")
        + sqlCol(wp.itemStr("ex_exec_date2"), "exec_date", "<=")
        + sqlCol(wp.itemStr("ex_acct_type"), "acct_type");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " param_type , " + " acct_type , " + " valid_date , " + " exec_date , "
        + " print_flag , " + " t_acct_cnt,"
        + " t_block_cnt+t_block_cnt2+t_block_cnt3 as db_block_cnt , "
        + " t_blocknot1_cnt + t_blocknot2_cnt + t_blocknot3_cnt as db_block_not_cnt ";
    wp.daoTable = "rsk_blockexec";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by exec_date DESC, param_type, acct_type, valid_date desc ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkdata();
    wp.setListCount(1);
    wp.totalRows = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {

    wp.reportId = "colr5830";
    //String ss;
    // ss = "帳戶類別 : "+wp.item_ss(ii,"acct_type")+" "+"生效日期 :
    // "+commString.ss_2ymd(wp.item_ss(ii,"valid_date"))
    // +" "+"執行日期 : "+commString.ss_2ymd(wp.item_ss(ii,"exec_date"));
    // wp.col_set("cond1", ss);

    boolean dataResult = dataPrint();
    if (! dataResult) return;
    wp.pageRows = 9999;
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5830.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;


  }

  boolean dataPrint() {
    String wkAcctType = "", wkExecDate = "", wkValidDate = "";
    String[] lsAcctType = wp.itemBuff("acct_type");
    String[] lsExecDate = wp.itemBuff("exec_date");
    String[] lsValidDate = wp.itemBuff("valid_date");
    String[] lsOpt = wp.itemBuff("opt");
    int cnt = -1;
    int optOn = 0;
    setSelectLimit(9999);
    for (int rr = 0; rr < lsOpt.length; rr++) {
      cnt = optToIndex(lsOpt[rr]);
      if (cnt < 0)
        continue;
      optOn ++;
      boolean selectResult = selectLogAcno(lsAcctType[cnt], lsExecDate[cnt]);
      if (! selectResult) return false; 
      wkAcctType = lsAcctType[cnt];
      wkExecDate = lsExecDate[cnt];
      wkValidDate = lsValidDate[cnt];
      break;
    }
    
    if(optOn ==0) {
    	alertPdfErr("未勾選列印資料");
    	wp.alertMesg("未勾選列印資料");
    	return false;
    }
    
    String cond1;
    cond1 = "帳戶類別 : " + wkAcctType + " " + "生效日期 : " + commString.strToYmd(wkValidDate) + " "
        + "執行日期 : " + commString.strToYmd(wkExecDate);
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);

    wp.listCount[0] = ii + 1;
    return true;
  }

  boolean selectLogAcno(String lsAcctType, String lsExecDate) {
    daoTid = "wk-";
    String sql1 = " select " + " A.log_date ," + " A.log_mode ," + " A.log_reason ,"
        + " A.log_not_reason ," + " A.mod_pgm , " + " B.line_of_credit_amt ," + " B.acct_key ,"
        + " B.payment_rate1 ," + " B.payment_rate2 ," + " B.payment_rate3 ," + " B.payment_rate4 ,"
        + " B.payment_rate5 ," + " B.payment_rate6 ," + " B.vip_code ,"
        + " B.p_seqno, B.acno_p_seqno," + " B.id_p_seqno ," + " B.corp_p_seqno ,"
        + " replace(uf_idno_name(B.id_p_seqno),'　','') as db_chi_name ," + " 0 as db_jrnl_bal ,"
        + " 0 as c_prb_amt ," + " 'N' as c_sup_flag " + " from rsk_acnolog A, act_acno B "
        + " where A.acno_p_seqno = B.acno_p_seqno " + " and A.log_date = ? "
        + " and A.kind_flag = 'A' " + " and A.log_type = '3' " + " and A.log_mode ='2' "
        + " and A.acct_type = ? " + " and A.log_not_reason = '' "
        // +" and A.log_reason like 'M%'" //-M%:表參數凍結-
        + " order by A.log_reason Asc , B.acct_key Asc ";
    sqlSelect(sql1, new Object[] {lsExecDate, lsAcctType});
    log("A:" + sqlRowNum);
    if (sqlRowNum <= 0) {
    	alertPdfErr("無資料可印列");
    	wp.alertMesg("查無資料");
    	return false;
    }

    int ilSelectCnt = sqlRowNum;
    int rr = 0;
    for (int ll = 0; ll < ilSelectCnt; ll++) {
      ii++;
      rr++;
      if (rr < 10) {
        wp.colSet(ii, "ser_num", "0" + rr);
      } else {
        wp.colSet(ii, "ser_num", "" + rr);
      }
      wp.colSet(ii, "ex_acct_key", sqlStr(ll, "wk-acct_key"));
      wp.colSet(ii, "ex_chi_name", sqlStr(ll, "wk-db_chi_name"));
      wp.colSet(ii, "ex_line_of_credit_amt", sqlNum(ll, "wk-line_of_credit_amt"));
      wp.colSet(ii, "ex_log_reason", sqlStr(ll, "wk-log_reason"));
      wp.colSet(ii, "ex_payment_record",
          sqlStr(ll, "wk-payment_rate1") + "_" + sqlStr(ll, "wk-payment_rate2") + "_"
              + sqlStr(ll, "wk_payment_rate3") + "_" + sqlStr(ll, "wk_payment_rate4") + "_"
              + sqlStr(ll, "wk_payment_rate5") + "_" + sqlStr(ll, "wk_payment_rate6"));
      wp.colSet(ii, "ex_vip_code", sqlStr(ll, "wk-vip_code"));
      wp.colSet(ii, "ex_mod_pgm", sqlStr(ll, "wk-mod_pgm"));
      if (selectCrbAmt(sqlStr(ll, "wk-p_seqno")) == false) {
        wp.colSet(ii, "ex_c_prb_amt", "0");
      } else {
        wp.colSet(ii, "ex_c_prb_amt", sqlNum("c_prb_amt"));
      }
      if (selectCSupFlag(sqlStr(ll, "wk-p_seqno")) == false) {
        wp.colSet(ii, "ex_c_sup_flag", "N");
      } else {
        wp.colSet(ii, "ex_c_sup_flag", "Y");
      }
      if (selectDbJrnlBal(sqlStr(ll, "wk-p_seqno")) == false) {
        wp.colSet(ii, "ex_db_jrnl_bal", "0");
      } else {
        wp.colSet(ii, "ex_db_jrnl_bal", sqlNum("db_jrnl_bal"));
      }
    }
    return true;
  }

  boolean selectCrbAmt(String lsPSeqno) {
    String sql1 = " select sum(prb_amount) as c_prb_amt " + " from rsk_problem "
        + " where card_no in (select card_no from crd_card where p_seqno =?) "
        + " and prb_status >= '30' " + " and prb_status < '80' ";

    sqlSelect(sql1, new Object[] {lsPSeqno});

    if (sqlRowNum <= 0) {
    	return false;
    }

    return true;
  }

  boolean selectCSupFlag(String lsPSeqno) {
    String sql1 = " select count(*) as db_cnt " + " from crd_card " + " where p_seqno = ? "
        + " and current_code ='0' " + " and sup_flag ='1' ";
    sqlSelect(sql1, new Object[] {lsPSeqno});

    if (sqlNum("db_cnt") <= 0) {
    	return false;
    }

    return true;
  }

  boolean selectDbJrnlBal(String lsPSeqno) {
    String sql1 =
        " select " + " acct_jrnl_bal as db_jrnl_bal " + " from act_acct " + " where p_seqno = ? ";
    sqlSelect(sql1, new Object[] {lsPSeqno});

    if (sqlRowNum <= 0) {
    	return false;
    }

    return true;

  }
}
