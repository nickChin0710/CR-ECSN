/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-27  V1.00.00  ryan       program initial                            *
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     * 
* 112-12-07  V1.00.02  JeffKung   增加分期資訊                                                                            *
******************************************************************************/
package bilm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0020 extends BaseEdit {
  Bilm0020Func func;
  String exCardNo = "";
  String exBatchNo = "";
  String exReferenceNo = "";
  String exBatchUnit = "";
  int ll = 0;
  int llErr2 = 0;
  String msg = "";
  String lsCardNo = "";
  String lsMajorCardNo = "", lsCurrentCode = "";
  String lsIssueDate = "", lsOppostDate = "";
  String lsPromoteDept = "", lsProdNo = "";
  String lsGroupCode = "", lsCardType = "";
  String lsAcnoPSeqno = "", lsID = "";
  String lsAcctType = "", lsAcctKey = "";
  String lsAcctStatus = "", lsStmtCycle = "";
  String lsBlockStatus = "", lsBlockDate = "";
  String lsPayByStageFlag = "", lsAutopayAcctNo = "";
  String lsCurrencyCode = "", lsCurrCode, lsBin = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;

    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      saveFunc();

    }

    dddwSelect();
    initButton();
  }

  private int getWhereStr() throws Exception {
	exCardNo = wp.itemStr("ex_card_no");
    exBatchNo = wp.itemStr("ex_batch_no");
    exReferenceNo = wp.itemStr("ex_reference_no");
    exBatchUnit = wp.itemStr("ex_batch_unit");

    wp.whereStr = "where 1=1 ";
    wp.whereStr +=
        " and ((curr_post_flag != 'Y' and doubt_type like '000%' or manual_upd_flag = 'P') or manual_upd_flag = 'Y') AND DEST_AMT != 0 ";

    if (empty(exCardNo) == false) {
        wp.whereStr += " and card_no = :card_no ";
        setString("card_no", exCardNo );
    }
    if (empty(exReferenceNo) == false) {
      wp.whereStr += " and reference_no like :reference_no ";
      setString("reference_no", exReferenceNo + "%");
    }
    if (empty(exBatchNo) == false) {
      wp.whereStr += " and batch_no like :batch_no ";
      setString("batch_no", exBatchNo + "%");
    }
    if (empty(exBatchUnit) == false) {
      wp.whereStr += " and batch_no like :batch_unit ";
      setString("batch_unit", "%" + exBatchUnit + "%");
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

	// check
	if (empty(wp.itemStr("ex_card_no")) 
		&& empty(wp.itemStr("ex_batch_no")) 
		&& empty(wp.itemStr("ex_reference_no"))
		&& empty(wp.itemStr("ex_batch_unit"))) {
		alertErr("請至少輸入一個查詢條件!");
		return;
	}
	    
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL =
        "card_no, " + "batch_no, " + "purchase_date, " + "film_no, " + "dest_curr," + "dest_amt, "
        + "source_amt, " + "source_curr, " + "doubt_type, " + "reference_no, "
        + "rsk_rsn, " + "install_first_amt, " + "install_per_amt, " + "install_tot_term, "
        + "payment_type, " + "mod_seqno "
        		;

    wp.daoTable = "bil_curpost";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

    // queryFunc();
    func = new Bilm0020Func(wp);
    if (strAction.equals("S2")) {

      String[] aaReferenceNo = wp.itemBuff("reference_no");
      String[] aaOpt = wp.itemBuff("opt");
      String[] aaCardNo = wp.itemBuff("card_no");
      String[] aaPurchaseDate = wp.itemBuff("purchase_date");
      String[] aaFilmNo = wp.itemBuff("film_no");
      String[] aaDestCurr = wp.itemBuff("dest_curr");
      String[] aaDestAmt = wp.itemBuff("dest_amt");
      String[] aaRskRsn = wp.itemBuff("rsk_rsn");
      String[] aaPaymentType = wp.itemBuff("payment_type");
      String[] aaInstallFirstAmt = wp.itemBuff("install_first_amt");
      String[] aaInstallPerAmt = wp.itemBuff("install_per_amt");
      String[] aaInstallTotTerm = wp.itemBuff("install_tot_term");
      String[] aaModSeqno = wp.itemBuff("mod_seqno");

      int llOK = 0, llERR = 0;

      wp.listCount[0] = aaReferenceNo.length;

      // -check duplication-
      for (ll = 0; ll < aaReferenceNo.length; ll++) {

        if (!checkBoxOptOn(ll, aaOpt)) {
          wp.colSet(ll, "check", "");
          if (empty(aaPurchaseDate[ll]) == true) {
            wp.colSet(ll, "errmsg", "交易日期不能為空白!");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }

          if (empty(aaFilmNo[ll])) {
            wp.colSet(ll, "errmsg", "微縮影編號不能為空白!");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }

          if (empty(aaDestAmt[ll]) || aaDestAmt[ll].equals("0")) {
            wp.colSet(ll, "errmsg", "金額錯誤: 金額為空白或0");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }

          if (empty(aaReferenceNo[ll]) == true) {
            wp.colSet(ll, "errmsg", "參考號碼不能為空白");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }

          String sql3 = "select business_date from ptr_businday";
          sqlSelect(sql3);
          String lsBusinessDate = sqlStr("business_date");
          String lsPurchaseDate = aaPurchaseDate[ll];

          if (this.toInt(lsPurchaseDate) > this.toInt(lsBusinessDate)) {
            wp.colSet(ll, "errmsg", "消費日期大於營業日!");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;

          }

          if (wfCheckRealCardNo(aaCardNo[ll]) != 1) {
            wp.colSet(ll, "errmsg", "此卡號不存在 !!");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }

          if (wfCheckCurrencyCode(aaCardNo[ll], aaDestCurr[ll]) != 1) {
            wp.colSet(ll, "errmsg", "目的地幣別請輸入901 or TWD!!");
            wp.colSet(ll, "ok_flag", "!");
            llERR++;
            sqlCommit(0);
            continue;
          }
          
          if ( "I".equals(aaPaymentType[ll]) &&
        		(empty(aaInstallTotTerm[ll]) || aaInstallTotTerm[ll].equals("0")) ) {
              wp.colSet(ll, "errmsg", "期數錯誤: 期數為空白或0");
              wp.colSet(ll, "ok_flag", "!");
              llERR++;
              sqlCommit(0);
              continue;
            }
          
          func.varsSet("aa_mod_log", "0");
        } else {
          wp.colSet(ll, "check", "checked");
          func.varsSet("aa_mod_log", "D");
        }

        func.varsSet("aa_card_no", aaCardNo[ll]);
        func.varsSet("aa_purchase_date", aaPurchaseDate[ll]);
        func.varsSet("aa_film_no", aaFilmNo[ll]);
        func.varsSet("aa_dest_curr", aaDestCurr[ll]);
        func.varsSet("aa_dest_amt", aaDestAmt[ll]);
        func.varsSet("aa_rsk_rsn", aaRskRsn[ll]);
        func.varsSet("aa_reference_no", aaReferenceNo[ll]);
        func.varsSet("aa_payment_type", aaPaymentType[ll]);
        func.varsSet("aa_install_first_amt", aaInstallFirstAmt[ll]);
        func.varsSet("aa_install_per_amt", aaInstallPerAmt[ll]);
        func.varsSet("aa_install_tot_term", aaInstallTotTerm[ll]);
        func.varsSet("aa_mod_seqno", aaModSeqno[ll]);

        if (func.dbUpdate() != 1) {
          alertErr("");
          wp.colSet(ll, "errmsg", "update bil_curpost err");
          wp.colSet(ll, "ok_flag", "!");
          llERR++;
          sqlCommit(0);
          continue;
        }
        llOK++;
        wp.colSet(ll, "ok_flag", "V");
        sqlCommit(1);
      }
      alertMsg("資料存檔處理,成功:" + llOK + "筆 ,失敗:" + llERR + "筆");
    }
  }

  int wfCheckRealCardNo(String cardNO) throws Exception {

    lsCardNo = cardNO;
    String lsSql = "select " + " major_card_no " + " ,current_code  " + " ,issue_date "
        + " ,oppost_date " + " ,promote_dept " + " ,prod_no " + " ,group_code " + " ,card_type "
        + " ,acno_p_seqno " + " ,p_seqno " + " ,id_p_seqno " + " from crd_card ";

    lsSql += " where card_no = :ls_card_no ";
    setString("ls_card_no", lsCardNo);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {

      return -1;
    }
    lsAcnoPSeqno = sqlStr("acno_p_seqno");
    func.varsSet("ls_major_card_no", sqlStr("major_card_no"));
    func.varsSet("ls_issue_date", sqlStr("issue_date"));
    func.varsSet("ls_oppost_date", sqlStr("oppost_date"));
    func.varsSet("ls_promote_dept", sqlStr("promote_dept"));
    func.varsSet("ls_prod_no", sqlStr("prod_no"));
    func.varsSet("ls_group_code", sqlStr("group_code"));
    func.varsSet("ls_card_type", sqlStr("card_type"));
    func.varsSet("ls_acno_p_seqno", sqlStr("acno_p_seqno"));
    func.varsSet("ls_p_seqno", sqlStr("p_seqno"));
    func.varsSet("ls_id", sqlStr("id_p_seqno"));

    String lsSql2 = " select " + " acct_type " + " ,acct_key  " + " ,acct_status " + " ,stmt_cycle "
    // + " ,block_status "
    // + " ,block_date "
        + " ,pay_by_stage_flag " + " ,autopay_acct_no " + " from act_acno ";

    lsSql2 += " where acno_p_seqno = :ls_acno_p_seqno ";
    setString("ls_acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(lsSql2);

    if (sqlRowNum <= 0) {

      return -1;
    }

    func.varsSet("ls_acct_type", sqlStr("acct_type"));
    func.varsSet("ls_acct_key", sqlStr("acct_key"));
    func.varsSet("ls_acct_status", sqlStr("acct_status"));
    func.varsSet("ls_stmt_cycle", sqlStr("stmt_cycle"));
    // func.vars_set("ls_block_status", sql_ss("block_status"));
    // func.vars_set("ls_block_date", sql_ss("block_date"));
    func.varsSet("ls_pay_by_stage_flag", sqlStr("pay_by_stage_flag"));
    func.varsSet("ls_autopay_acct_no", sqlStr("autopay_acct_no"));

    return 1;
  }

  int wfCheckCurrencyCode(String realCardNo, String destinationCurrency) throws Exception {
    if (!destinationCurrency.equals("901") && !destinationCurrency.equals("TWD")) {
      return -1;
    }
    /*
     * if (empty(real_card_no) == true) { return -1; } ls_bin = ss_mid(real_card_no,0,6);
     * ls_currency_code = destination_currency; System.out.println("ls_bin:"+ls_bin);
     * System.out.println("ls_currency_code:"+ls_currency_code); if
     * (!ls_currency_code.equals("TWD")) { String ls_sql2 =
     * "select dc_curr_code from ptr_bintable where substr(bin_no,1,6) = :ls_bin";
     * setString("ls_bin", ls_bin); sqlSelect(ls_sql2); ls_curr_code = sql_ss("dc_curr_code");
     * System.out.println("ls_curr_code:"+ls_curr_code); if(empty(ls_curr_code)){ ls_curr_code =
     * "901"; } // sql_nrow = 0; if (sql_nrow > 0) { if (!ls_curr_code.equals(ls_currency_code)) {
     * return -1; } } else return -1; }
     */

    return 1;
  }

  @Override
  public void initButton() {

    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud("XX");
    // }
    // this.btnMode_aud("XX");
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_batch_unit");
      this.dddwList("dddw_liab_type", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");
    } catch (Exception ex) {
    }
  }

}
