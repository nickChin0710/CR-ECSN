/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-03-09  V1.00.01  Jeff Kung  program initial                            *
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0050 extends BaseEdit {
  String mExContractNo = "";
  String mExContractSeqNo = "";

  String receiveName = "", receiveTel = "", zipCode = "", receiveAddress = "", stmtCycle = "";
  String dbEffectDate = "", dbId = "", dbName = "", dbBirthday = "";
  String isMsg = "";
  double llNetNp = 0, llUseBp = 0, llNetTtlBp = 0, llTransBp = 0, llPreSubbp = 0;
  double idcNetTtlBp = 0, idcNetTtlNotaxBef = 0, idcNetTtlTaxBef = 0, idcTransBp = 0,
      idcTransBpTax = 0;
  double tax1 = 0, tax2 = 0, tax3 = 0, tax4 = 0, tax5 = 0, notax1 = 0, notax2 = 0, notax3 = 0,
      notax4 = 0, notax5 = 0;
  double netttl1 = 0, netTtl2 = 0, netTtl3 = 0, netTtl4 = 0, neTttl5 = 0;
  private double ttls[];

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
      /* 執行 */
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exMchtNo = wp.itemStr("ex_merchant");
    String exKeyin = wp.itemStr("ex_keyin");
    String exContractNo = wp.itemStr("ex_contract_no");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");

    if (empty(exMchtNo) && empty(exKeyin) && empty(exContractNo) && empty(exDateS)
        && empty(exDateE)) {
      alertErr("請至少輸一項查詢條件");
      return false;
    }

    wp.whereStr = " where 1=1  ";
    wp.whereStr += "and decode(a.apr_flag,'','N',a.apr_flag) <> 'Y' ";
    wp.whereStr += "and decode(a.allocate_flag,'','N',a.allocate_flag) != 'Y' ";

    if (empty(exMchtNo) == false) {
      wp.whereStr += sqlCol(exMchtNo, "a.mcht_no", "like%");
    }

    if (empty(exKeyin) == false) {
      wp.whereStr += sqlCol(exKeyin, "a.mod_user", "like%");
    }

    if (empty(exContractNo) == false) {
      wp.whereStr += sqlCol(exContractNo, "a.contract_no", "like%");
    }

    wp.whereStr += sqlStrend(exDateS, exDateE, "to_char(a.mod_time,'YYYYMMDD')");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "uf_idno_name(a.id_p_seqno) db_cname, " + "a.contract_no, "
        + "a.contract_seq_no, " + "a.card_no, " + "a.product_name, " + "a.mcht_chi_name, "
        + "a.tot_amt, " + "a.acct_type, " + "a.stmt_cycle, " + "a.product_no, " + "a.mcht_no, "
        + "a.unit_price, " + "a.qty, " + "a.install_tot_term, " + "a.remd_amt, "
        + "a.extra_fees, "
        + "a.first_post_date, " + "a.post_cycle_dd, " + "a.install_curr_term, "
        + "a.apr_date, " + "a.apr_flag, "
        + "a.mod_user, " + "a.mod_time, " + "a.mod_pgm, " + "a.mod_seqno, " + "'0', " 
        + "CASE WHEN b.STMT_INST_FLAG='Y' THEN '帳單分期' WHEN b.TRANS_FLAG='Y' THEN '長循分期' ELSE '一般分期' END install_type,"
        + "decode(a.first_post_kind,'1','當期','次期') as installment_delay, " 
        + "a.against_num, " + "' '  db_modify_flag, "  
		+ "a.first_remd_amt, " + "a.year_fees_rate, " + "a.trans_rate, "
        + " c.chi_name as card_desc ";
    wp.daoTable = " bil_contract a left join bil_merchant b on a.mcht_no = b.mcht_no "
        + "                    left join crd_idno c on a.id_p_seqno=c.id_p_seqno ";
    wp.whereOrder = " order by a.contract_no ";
//    getWhereStr();
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr +
    // wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // list_wkdata();
    apprDisabled("mod_user");
  }

  void listWkdata() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
	  ;
  }

  @Override
  public void saveFunc() throws Exception {
    String[] opt = wp.itemBuff("opt");
    String[] aaContractNo = wp.itemBuff("contract_no");
    String[] aaContractSeqNo = wp.itemBuff("contract_seq_no");
    String[] aaTotAmt = wp.itemBuff("tot_amt");
    String[] aaQty = wp.itemBuff("qty");
    String[] aaRefundQty = wp.itemBuff("refund_qty");
    String[] aaRemdAmt = wp.itemBuff("remd_amt");
    String[] aaRedeemAmt = wp.itemBuff("redeem_amt");
    String[] aaInstallTotTerm = wp.itemBuff("install_tot_term");
    String[] aaUnitPrice = wp.itemBuff("unit_price");
    String[] aaFirstRemdAmt = wp.itemBuff("first_remd_amt");
    String[] aaExchangeAmt = wp.itemBuff("exchange_amt");
    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] aaDelvDate = wp.itemBuff("delv_date");
    String[] aaDelvConfirmDate = wp.itemBuff("delv_confirm_date");
    String[] aaDelvConfirmFlag = wp.itemBuff("delv_confirm_flag");
    String[] aaForcedPostFlag = wp.itemBuff("forced_post_flag");
    String[] aaPostCycleDd = wp.itemBuff("post_cycle_dd");


    wp.listCount[0] = aaContractNo.length;

    // save
    String lsMchtNo = "", lsSql = "", lsTransFlag = "";
    String lsForcePostFlag = "", lsContractNo = "", lsContractNoSeq = "";
    double liDd = 0, liPostCycleDd = 0;
    int llOk = 0, llErr = 0;

    for (int ii = 0; ii < aaContractNo.length; ii++) {
      if (!checkBoxOptOn(ii, opt)) {
        continue;
      }
      // check
      double tt1 = 0, tt2 = 0;
      tt1 = toNum(aaUnitPrice[ii]) * toNum(aaInstallTotTerm[ii]) + toNum(aaRedeemAmt[ii])
          + toNum(aaRemdAmt[ii]) + toNum(aaFirstRemdAmt[ii]) + toNum(aaExchangeAmt[ii]);
      tt2 = toNum(aaTotAmt[ii]) * (toNum(aaQty[ii]) - toNum(aaRefundQty[ii]));
      if (!(tt1 == tt2)) {
        wp.colSet(ii, "ok_flag", "!");
        wp.colSet(ii, "err_msg", "單價  * 數量  != 總價");
        llErr++;
        continue;
      }

      lsContractNo = aaContractNo[ii];
      lsContractNoSeq = aaContractSeqNo[ii];

      lsMchtNo = aaMchtNo[ii];
      lsSql = "select decode(trans_flag,'','N',trans_flag) trans_flag " + "from	bil_merchant "
          + "where mcht_no = :ls_mcht_no";
      setString("ls_mcht_no", lsMchtNo);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        lsTransFlag = sqlStr("trans_flag");
      }


      // delv_date,delv_confirm_date,delv_confirm_flag
      if (empty(aaDelvDate[ii])) {
        aaDelvDate[ii] = getSysDate();
        aaDelvConfirmDate[ii] = getSysDate();
        aaDelvConfirmFlag[ii] = "Y";
      }
      // forced_post_flag,post_cycle_dd
      lsForcePostFlag = aaForcedPostFlag[ii];
      liPostCycleDd = toNum(aaPostCycleDd[ii]);
      if (lsForcePostFlag.equals("Y") && !lsTransFlag.equals("Y") && liPostCycleDd > 0) {
        lsSql = "select to_number(substr(online_date,7,2)) as li_dd " + "from ptr_businday ";
        sqlSelect(lsSql);
        liDd = sqlNum("li_dd");
        aaPostCycleDd[ii] = numToStr(liDd, "###");
      }

      // update bil_contract
      lsSql = "update bil_contract set " + "apr_date = to_char(sysdate,'yyyymmdd') ,"
          + "apr_flag = 'Y' ," + "delv_date =:delv_date, "
          + "delv_confirm_date =:delv_confirm_date, " + "delv_confirm_flag =:delv_confirm_flag, "
          + "post_cycle_dd =:post_cycle_dd, " + "mod_pgm = 'Bilp0050', "
          + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1=1 " + "and contract_no =:contract_no "
          + "and contract_seq_no =:contract_seq_no ";
      setString("delv_date", aaDelvDate[ii]);
      setString("delv_confirm_date", aaDelvConfirmDate[ii]);
      setString("delv_confirm_flag", aaDelvConfirmFlag[ii]);
      setString("post_cycle_dd", aaPostCycleDd[ii]);
      setString("contract_no", lsContractNo);
      setString("contract_seq_no", lsContractNoSeq);
      sqlExec(lsSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "ok_flag", "!");
        wp.colSet(ii, "err_msg", "Update bil_contract error");
        sqlCommit(0);
        llErr++;
        continue;
      } else {
        llOk++;
      }
    }
    sqlCommit(1);
    alertMsg("處理完成 ! 成功: " + llOk + "筆, 失敗 :" + llErr + "筆");
  }

  public int wfCheckCardno(String cardNo) throws Exception {
    if (empty(cardNo)) {
      return -1;
    }
    String lsSql = "select sup_flag," + "major_card_no, " + "acct_type, " + "id_p_seqno, "
        + "new_end_date, " + "acno_p_seqno " + "from crd_card" + "where card_no = :as_cardno ";
    setString("as_cardno", cardNo);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    String lsFlag = sqlStr("sup_flag");
    String lsMajor = sqlStr("major_card_no");
    String lsAcctType = sqlStr("acct_type");
    String lsIdPSeqno = sqlStr("id_p_seqno");
    String lsNewEndDate = sqlStr("new_end_date");
    String lsPSeqno = sqlStr("anco_p_seqno");

    String lsSql2 = "select chi_name, " + "birthday, " + "resident_zip, "
        + "home_tel_no1||home_tel_ext1 as tel,id_no " + "from crd_idno "
        + "where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdPSeqno);

    sqlSelect(lsSql2);
    if (sqlRowNum <= 0) {
      return -1;
    }

    String lsChiName = sqlStr("chi_name");
    String lsBirthday = sqlStr("birthday");
    String lsZip = sqlStr("resident_zip");
    String lsTel = sqlStr("tel");
    String lsId = sqlStr("id_no");

    String lsSql3 =
        "select bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as addr,"
            + "stmt_cycle " + "from act_acno " + "where acno_p_seqno = :ls_p_seqno ";
    setString("ls_p_seqno", lsPSeqno);
    sqlSelect(lsSql3);
    if (sqlRowNum <= 0) {
      return -1;
    }
    String lsMailAddr = sqlStr("addr");
    String lsStmtCycle = sqlStr("stmt_cycle");

    receiveName = lsChiName;
    receiveTel = lsTel;
    zipCode = lsZip;
    receiveAddress = lsMailAddr;
    stmtCycle = lsStmtCycle;
    dbEffectDate = lsNewEndDate;
    dbId = lsId;
    dbName = lsChiName;
    dbBirthday = lsBirthday;
    stmtCycle = lsStmtCycle;
    wp.colSet("db_birthday", lsBirthday);
    wp.colSet("db_id", lsId);
    wp.colSet("stmt_cycle", lsStmtCycle);
    return 1;
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

      wp.initOption = "--";
      wp.optionKey = wp.colStr("sale_emp_no");
      this.dddwList("dddw_sale_emp_no", "sec_user", "usr_id", "usr_cname",
          " where 1=1 order by usr_id ");
    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {

    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }

    return;
  }
  
}
