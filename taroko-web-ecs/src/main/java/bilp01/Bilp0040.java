/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-01  V1.00.00  Andy       program initial                            *
* 107-09-04  V1.00.01  Andy       Update                                     *
* 107-06-14  V1.00.02  Amber      Update p_seqno → acno_seqno                *
*109-04-23   V1.00.03  shiyuqi       updated for project coding standard     *                                                                         *
* 110-10-15  V1.00.04  Yang Bo    joint sql replace to parameters way        *
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0040 extends BaseEdit {
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
    // wp.whereStr += "and decode(a.auth_code,'','N',a.auth_code) <> 'N' ";

    if (empty(exMchtNo) == false) {
//      wp.whereStr += " and  a.mcht_no like '" + exMchtNo + "%' ";
      wp.whereStr += sqlCol(exMchtNo, "a.mcht_no", "like%");
    }

    if (empty(exKeyin) == false) {
//      wp.whereStr += " and  a.mod_user like '" + exKeyin + "%' ";
      wp.whereStr += sqlCol(exKeyin, "a.mod_user", "like%");
    }

    if (empty(exContractNo) == false) {
//      wp.whereStr += " and  a.contract_no like '" + exContractNo + "%' ";
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
        + "a.auto_delv_flag, " + "a.fees_fix_amt, " + "a.fees_rate, " + "a.extra_fees, "
        + "a.first_post_date, " + "a.post_cycle_dd, " + "a.install_curr_term, "
        + "a.all_post_flag, " + "a.refund_flag, " + "a.apr_date, " + "a.apr_flag, "
        + "a.receive_name, " + "a.receive_address, " + "a.delv_date, " + "a.delv_confirm_flag, "
        + "a.register_no, " + "a.auth_code, " + "a.delv_batch_no, " + "a.forced_post_flag, "
        + "a.install_back_term, " + "a.install_back_term_flag, " + "a.mod_user, " + "a.mod_time, "
        + "a.mod_pgm, " + "a.mod_seqno, " + "'0', " + "a.contract_kind, "
        + "decode(a.contract_kind,'1','分期付款','2','郵購') as db_contract_kind, " + "a.exchange_amt, "
        + "a.against_num, " + "' '  db_modify_flag, " + "a.delv_confirm_date, " + "a.refund_qty, "
        + "a.clt_fees_amt, " + "a.clt_unit_price, " + "a.clt_install_tot_term, "
        + "a.clt_remd_amt, " + "a.fh_flag, " + "a.first_remd_amt, " + "a.redeem_amt, "
        + "a.redeem_point, " + "b.reference_seq, " + "b.acaj_amt, "
        + "b.mcht_no as bil_contract_acaj_mcht_no,"
        + "decode(refund_apr_flag,'Y',(qty-refund_qty)*tot_amt,qty*tot_amt) as  wk_tot_amt ,"
        + " c.chi_name as card_desc ";
    wp.daoTable = " bil_contract a left join bil_contract_acaj b on a.contract_no = b.contract_no "
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
    mExContractNo = itemKk("data_k1");
    mExContractSeqNo = itemKk("data_k2");
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "contract_no, " + "contract_seq_no, "
        + "card_no, " + "back_card_no, " + "new_card_no, " + "id_p_seqno, "
        + "uf_idno_name(id_p_seqno) db_cname, " + "uf_idno_id(id_p_seqno) db_idno, "
        + "acno_p_seqno, " + "acct_type, " + "stmt_cycle, " + "product_no, " + "product_name, "
        + "mcht_no, " + "mcht_chi_name, " + "mcht_eng_name, " + "contract_kind, "
        + "allocate_flag, " + "cvv2, " + "exchange_amt, " + "against_num, " + "unit_price, "
        + "qty, " + "tot_amt, " + "install_tot_term, " + "remd_amt, " + "auto_delv_flag, "
        + "fees_fix_amt, " + "fees_rate, " + "extra_fees, " + "first_post_date, "
        + "post_cycle_dd, " + "install_curr_term, " + "all_post_flag, " + "refund_flag, "
        + "refund_qty, " + "receive_name, " + "receive_tel, " + "receive_tel1, " + "voucher_head, "
        + "uniform_no, " + "zip_code, " + "receive_address, " + "delv_date, "
        + "delv_confirm_flag, " + "delv_confirm_date, " + "register_no, " + "auth_code, "
        + "delv_batch_no, " + "forced_post_flag, " + "install_back_term, "
        + "install_back_term_flag, " + "dev_flag_20, " + "prt_flag_21, " + "limit_end_date, "
        + "fh_flag, " + "clt_fees_amt, " + "clt_unit_price, " + "clt_install_tot_term, "
        + "clt_remd_amt, " + "clt_forced_post_flag, " + "cps_flag, " + "fee_flag, " + "film_no, "
        + "reference_no, " + "payment_type, " + "first_remd_amt, " + "vip_code, "
        + "ccas_resp_code, " + "nccc_resp_code, " + "batch_no, " + "acquirer_member_id, "
        + "installment_kind, " + "refund_batch_no, " + "terminal_term, " + "terminal_confirm_flag, "
        + "terminal_post_flag, " + "purchase_date, " + "redeem_point, " + "redeem_amt, "
        + "redeem_kind, " + "org_contract_no, " + "org_contract_seq_no, " + "merge_flag, "
        + "ptr_mcht_no, " + "fees_reference_no, " + "int_rate, " + "int_rate_flag, "
        + "new_it_flag, " + "last_update_date, " + "new_proc_date, " + "year_fees_rate, "
        + "year_fees_date, " + "trans_rate, " + "bill_prod_type, " + "billing_disp_date, "
        + "first_post_kind, " + "sale_emp_no, " + "refund_reference_no, " + "refund_apr_flag, "
        + "refund_apr_date, "
        // + "appropriation_bank, "
        // + "appropriation_account, "
        + "apr_date, " + "apr_flag, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno";
    wp.daoTable = "bil_contract";
    wp.whereStr = " where 1=1";
    wp.whereStr += " and contract_no = :contract_no and contract_seq_no = :contract_seq_no";
    setString("contract_no", mExContractNo);
    setString("contract_seq_no", mExContractSeqNo);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, contract_no=" + mExContractNo);
      return;
    }

    // wf_check_cardno(wp.col_ss("card_no")); //not use Andy20180904
    String lsSql = "select chi_name, " + "birthday, " + "resident_zip, "
        + "home_tel_no1||home_tel_ext1 as tel,id_no " + "from crd_idno "
        + "where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", wp.colStr("id_p_seqno"));
    sqlSelect(lsSql);
    wp.colSet("birthday", sqlStr("birthday"));
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

    // check
    // int rr = -1;
    // for (int ii = 0; ii < opt.length; ii++) {
    // rr = (int) this.to_Num(opt[ii]) - 1;
    // if (rr < 0) {
    // return;
    // }
    // }
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
      // ls_new_it_flag = "Y";
      // if (ls_trans_flag.equals("Y")) {
      // ls_new_it_flag = "N";
      // }

      // wf_cyc_bpcd_rtn(aa_card_no[rr],aa_redeem_point[rr],aa_against_num[rr]);

      // upd_act_acaj 不論成功失敗都繼續做 Andy20180904
      updActAcaj(aaContractNo[ii], aaContractSeqNo[ii]);


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
          + "post_cycle_dd =:post_cycle_dd, " + "mod_pgm = 'Bilp0040', "
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
    // 20180904 Andy ....no use
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

  /* 此段用不到 */
  public int wfCycBpcdRtn(String card, String point, String againstNum) throws Exception {
    double adcPoint = 0;

    String lsSql =
        " select acno_p_seqno, acct_type     " + "   from crd_card " + "  where card_no=:card_no ";
    setString("card_no", card);
    sqlSelect(lsSql);
    String asPSeqno = sqlStr("acno_p_seqno");
    // String type = sql_ss("acct_type");

    if (empty(asPSeqno)) {
      isMsg = "ACNO_P-seqno無效或空值";
      return 0;
    }

    if (empty(point) || toNum(point) == 0) {
      isMsg = "紅利點數為 0 或空值";
      return 0;
    } else {
      adcPoint = toNum(point);
    }

    lsSql = "select decode(net_ttl_bp,'',0,net_ttl_bp) net_ttl_bp, "
        + "decode(use_bp,'',0,use_bp) use_bp " + "decode(trans_bp,'',0,trans_bp) trans_bp "
        + "decode(pre_subbp,'',0,pre_subbp) pre_subbp "
        + "from cyc_bpcd where 1=1 and type_code = 'BONU' ";
    lsSql += sqlCol("p_seqno", asPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      llNetNp = toNum(sqlStr("net_ttl_bp"));
      if (llNetNp < adcPoint) {
        return 1;
      }
      if (ofSubBonus(asPSeqno, point) != 1) {
        return 2;
      }
    }
    lsSql = "INSERT INTO cyc_bpjr ( "
        + "p_seqno, acct_type, acct_key, type_code, card_no, acct_date, "
        + "trans_code, trans_bp, net_bp, trans_bp_tax, net_ttl_tax_bef, net_ttl_notax_bef, "
        + "gift_no, gift_name, gift_cnt, gift_cash_value, gift_pay_cash, reason_code, "
        + "mod_user, mod_time, mod_pgm ) " + "values ( "
        + ":ls_p_seqno, :ls_acct_type, :ls_acct_key, 'BONU', :ls_card_no, to_char(sysdate,'yyyymmdd'), "
        + "'USE', :ll_redeem_point, :ll_net_bp, :ll_trans_bp_tax, :ll_net_ttl_tax_bef, :ll_net_ttl_notax_bef, "
        + "'','', 0,0, 0, '7', :ls_mod_user, sysdate, 'bilp0040' )";
    sqlExec(lsSql);
    if (sqlRowNum <= 0) {
      return 3;
    }
    return 0;
  }

  public int ofSubBonus(String asPSeqno, String adcPoint) throws Exception {
    String lsSql = "";

    if (empty(asPSeqno)) {
      isMsg = "P-seqno無效或空值";
      return 0;
    }

    if (empty(adcPoint) || toNum(adcPoint) == 0) {
      isMsg = "紅利點數為 0 或空值";
      return 0;
    }
    lsSql = "select decode(net_ttl_bp,'',0,net_ttl_bp) net_ttl_bp, "
        + "decode(use_bp,'',0,use_bp) use_bp " + "decode(trans_bp,'',0,trans_bp) trans_bp "
        + "decode(pre_subbp,'',0,pre_subbp) pre_subbp "
        + "from cyc_bpcd where 1=1 and type_code = 'BONU' ";
    lsSql += sqlCol("p_seqno", asPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      // ll_net_np =
      // 0,ll_use_bp=0,ll_net_ttl_bp=0,ll_trans_bp=0,ll_pre_subbp=0;
      llNetTtlBp = toNum(sqlStr("net_ttl_bp"));
      llUseBp = toNum(sqlStr("use_bp"));
      llTransBp = toNum(sqlStr("trans_bp"));
      llPreSubbp = toNum(sqlStr("pre_subbp"));

      if (llNetTtlBp < toNum(adcPoint)) {
        isMsg = "持卡人紅利點數不足";
        return -1;
      }
      llUseBp = llUseBp + toNum(adcPoint);
      llNetTtlBp = llNetTtlBp - toNum(adcPoint);
      llTransBp = llTransBp - toNum(adcPoint);
      if (llTransBp < 0) {
        llTransBp = 0;
      }
      llPreSubbp = llPreSubbp - toNum(adcPoint);
      if (llPreSubbp < 0) {
        llPreSubbp = 0;
      }
      if (subNetTtlPoint2(asPSeqno, adcPoint) != 1) {
        return -1;
      }
      if (updCycBpcd(asPSeqno) != 1) {
        isMsg = "Update CYC_BPCD error: ";
        return -1;
      }
    }
    return 1;
  }

  public int subNetTtlPoint2(String asPSeqno, String adcPoint) throws Exception {
    String lsSql = "";
    double ldcPoint = 0;
    ttls = new double[11];

    ldcPoint = toNum(adcPoint);
    lsSql = "select * fom cyc_bpcd where 1=1 and type_code = 'BONU' ";
    lsSql += sqlCol("p_seqno", asPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum != 1) {
      isMsg = "持卡人紅利點數累積資料有誤~";
      return -1;
    }

    // Null Dereference
    if (ttls == null) {
      isMsg = "ttls is null";
      return -1;
    }

    ttls[1] = toNum(sqlStr("net_ttl_tax_5"));
    ttls[2] = toNum(sqlStr("net_ttl_notax_5"));
    ttls[3] = toNum(sqlStr("net_ttl_tax_4"));
    ttls[4] = toNum(sqlStr("net_ttl_notax_4"));
    ttls[5] = toNum(sqlStr("net_ttl_tax_3"));
    ttls[6] = toNum(sqlStr("net_ttl_notax_3"));
    ttls[7] = toNum(sqlStr("net_ttl_tax_2"));
    ttls[8] = toNum(sqlStr("net_ttl_notax_2"));
    ttls[9] = toNum(sqlStr("net_ttl_tax_1"));
    ttls[10] = toNum(sqlStr("net_ttl_notax_1"));
    idcNetTtlTaxBef = ttls[1] + ttls[3] + ttls[5] + ttls[7] + ttls[9];
    idcNetTtlNotaxBef = ttls[2] + ttls[4] + ttls[6] + ttls[8] + ttls[10];
    // -扣點數-
    for (int i = 1; i <= 10; i++) {
      if (ttls[i] >= ldcPoint) {
        ttls[i] = ttls[i] - ldcPoint;
        ldcPoint = 0;
        continue;
      }
      ldcPoint = ldcPoint - ttls[i];
      ttls[i] = 0;
    }
    if (ldcPoint > 0) {
      isMsg = "持卡人點數不足";
      return -1;
    }
    // -使用點數-

    idcTransBp = idcNetTtlNotaxBef - (ttls[2] + ttls[4] + ttls[6] + ttls[8] + ttls[10]);
    idcTransBpTax = idcNetTtlTaxBef - (ttls[1] + ttls[3] + ttls[5] + ttls[7] + ttls[9]);

    tax1 = ttls[9];
    tax2 = ttls[7];
    tax3 = ttls[5];
    tax4 = ttls[3];
    tax5 = ttls[1];
    notax1 = ttls[10];
    notax2 = ttls[8];
    notax3 = ttls[6];
    notax4 = ttls[4];
    notax5 = ttls[2];

    // --net_ttl_1~5--------------------
    netttl1 = ttls[9] + ttls[10];
    netTtl2 = ttls[7] + ttls[8];
    netTtl3 = ttls[5] + ttls[6];
    netTtl4 = ttls[3] + ttls[4];
    neTttl5 = ttls[1] + ttls[2];

    return 1;
  }

  public int updCycBpcd(String asPSeqno) throws Exception {
    String lsSql = "";
    lsSql = "update bpcd set "
        // + "p_seqno =:p_seqno, "
        + "use_bp :use_bp, " + "trans_bp :trans_bp, " + "pre_subbp :pre_subbp, "
        + "net_ttl_bp =:net_ttl_bp, " + "net_ttl_1 =:net_ttl_1, "
        + "net_ttl_notax_1 =:net_ttl_notax_1, " + "net_ttl_tax_1 =:net_ttl_tax_1, "
        + "net_ttl_2 =:net_ttl_2, " + "net_ttl_notax_2 =:net_ttl_notax_2, "
        + "net_ttl_tax_2 =:net_ttl_tax_2, " + "net_ttl_3 =:net_ttl_3, "
        + "net_ttl_notax_3 =:net_ttl_notax_3, " + "net_ttl_tax_3 =:net_ttl_tax_3, "
        + "net_ttl_4 =:net_ttl_4, " + "net_ttl_notax_4 =:net_ttl_notax_4, "
        + "net_ttl_tax_4 =:net_ttl_tax_4, " + "net_ttl_5 =:net_ttl_5, "
        + "net_ttl_notax_5 =:net_ttl_notax_5, " + "net_ttl_tax_5 =:net_ttl_tax_5, "
        + "apr_flag=:apr_flag, " + " apr_date=:apr_date, " + " apr_user=:apr_user, "
        + "mod_pgm = 'Bilp0040'," + "mod_seqno = nvl(mod_seqno,0)+1  "
        + "where 1=1 and type_code = 'BONU' and p_seqno =:p_seqno ";
    setString("use_bp", numToStr(llUseBp, "#,###"));
    setString("trans_bp", numToStr(llTransBp, "#,###"));
    setString("pre_subbp", numToStr(llPreSubbp, "#,###"));
    setString("net_ttl_bp", numToStr(llNetTtlBp, "#,###"));
    setString("net_ttl_1", numToStr(netttl1, "#,###"));
    setString("net_ttl_notax_1", numToStr(notax1, "#,###"));
    setString("net_ttl_tax_1", numToStr(tax1, "#,###"));
    setString("net_ttl_2", numToStr(netTtl2, "#,###"));
    setString("net_ttl_notax_2", numToStr(notax2, "#,###"));
    setString("net_ttl_tax_2", numToStr(tax2, "#,###"));
    setString("net_ttl_3", numToStr(netTtl3, "#,###"));
    setString("net_ttl_notax_3", numToStr(notax3, "#,###"));
    setString("net_ttl_tax_3", numToStr(tax3, "#,###"));
    setString("net_ttl_4", numToStr(netTtl4, "#,###"));
    setString("net_ttl_notax_4", numToStr(notax4, "#,###"));
    setString("net_ttl_tax_4", numToStr(tax4, "#,###"));
    setString("net_ttl_5", numToStr(neTttl5, "#,###"));
    setString("net_ttl_notax_5", numToStr(notax5, "#,###"));
    setString("net_ttl_tax_5", numToStr(tax5, "#,###"));
    setString("apr_flag", "Y");
    setString("apr_date", getSysDate());
    setString("apr_user", wp.loginUser);
    setString("p_sno", asPSeqno);
    sqlExec(lsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;
  }

  public int updActAcaj(String contractNo, String contractSeqNo) throws Exception {
    String lsSql = "";
    lsSql = "update act_acaj " + "set apr_flag = 'Y', "
        + "update_date = to_char(sysdate,'yyyymmdd'), " + "update_user = 'bilp0040' "
        + "where reference_no in (select reference_seq from bil_contract_acaj "
        + "where contract_no = :ls_contract_no " + "and contract_seq_no = :ls_contract_no_seq )";
    setString("ls_contract_no", contractNo);
    setString("ls_contract_no_seq", contractSeqNo);
    sqlExec(lsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
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
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_mcht_no");
      // this.dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no",
      // "mcht_chi_name", "where 1=1 and mcht_status = '1' group by
      // mcht_no,mcht_chi_name order by mcht_no");
      //
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_product_no");
      // this.dddw_list("dddw_product_no", "bil_prod", "product_no",
      // "product_name", " where 1=1 order by product_no");
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
  
//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}
}
