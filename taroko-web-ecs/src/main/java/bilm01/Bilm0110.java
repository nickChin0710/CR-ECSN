/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-07  V1.00.00  yash            program initial                            *
* 108-06-13  V1.00.03  Andy		   update : p_seqno ==> acno_p_seqno          *
* 109-01-06  V1.00.04  Ru Chen    Modify AJAX                                *
* 109-04-23  V1.00.05  shiyuqi       updated for project coding standard     * 
* 109-05-19  V1.00.06  Ryan           updated for project coding standard        *   
* 110-01-18  V1.00.07  Justin          fix the error renaming variable names
* 112-12-06  V1.00.08  JeffKung      畫面欄位調整                                                                     
******************************************************************************/

package bilm01;

import java.text.DecimalFormat;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGateway;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0110 extends BaseEdit {
  String mKkContractNo = "", mKkContractSeqNo = "";
  Bilm0110Func func;

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
      // insertFunc();
      strAction = "A";
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200106 modify AJAX
      if ("0".equals(wp.getValue("ID_CODE"))) {
        strAction = "AJAX";
        processAjaxOption1();
      } else if ("1".equals(wp.getValue("ID_CODE"))) {
        itemChange();
      }

    }

    initButton();
  }

	// for query use only
	private boolean getWhereStr() throws Exception {

		wp.whereStr = " where 1=1 ";
		if (empty(wp.itemStr("ex_contract_no")) && empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_idno"))) {
			alertErr("請至少輸入一項查詢條件!!");
			return false;
		}

		if (empty(wp.itemStr("ex_contract_no")) == false) {
		      wp.whereStr += " and  p.contract_no like :ex_contract_no ";
		      setString("ex_contract_no", wp.itemStr("ex_contract_no") + "%");
	    }
		
		if (empty(wp.itemStr("ex_card_no")) == false) {
		      wp.whereStr += " and  p.card_no like :ex_card_no ";
		      setString("ex_card_no", wp.itemStr("ex_card_no") + "%");
		}

		if (empty(wp.itemStr("ex_idno")) == false) {
			wp.whereStr += " and c.id_no = :ex_idno ";
			setString("ex_idno", wp.itemStr("ex_idno"));
		}

		if (empty(wp.itemStr("ex_purchase_date1")) == false) {
			wp.whereStr += " and  p.purchase_date >= :ex_purchase_date1 ";
			setString("ex_purchase_date1", wp.itemStr("ex_purchase_date1"));
		}

		if (empty(wp.itemStr("ex_purchase_date2")) == false) {
			wp.whereStr += " and  p.purchase_date <= :ex_purchase_date2 ";
			setString("ex_purchase_date2", wp.itemStr("ex_purchase_date2"));
		}

		return true;
	}

  @Override
  public void queryFunc() throws Exception {
    // getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " p.contract_no " 
        + ",p.contract_seq_no " 
    	+ ",p.contract_kind "
    	+ ",p.apr_flag " 
    	+ ",p.mod_user "
    	+ ",c.id_no "
        + ",p.card_no " 
        + ",p.purchase_date " 
        + ",p.tot_amt "
        + ",p.install_tot_term "
        + ",p.unit_price "
        + ",p.install_curr_term "
        + ",decode(p.fee_flag ,'L', p.unit_price * (p.install_tot_term - p.install_curr_term) + p.remd_amt, "
        + " decode(p.install_curr_term,0,p.unit_price * (p.install_tot_term - p.install_curr_term)+p.first_remd_amt, p.unit_price * (p.install_tot_term - p.install_curr_term))) as unbill_amt "
        + ",decode(p.all_post_flag,'Y','closed','未結案') as status_code "  //分期狀態
        + ",p.forced_post_flag " 
        + ",p.product_no " 
        + ",p.product_name "
        + ",p.mcht_no " 
        + ",p.mcht_chi_name "
        + ",decode(p.contract_kind,'1','1-分期','2','2-郵購',p.contract_kind) as db_contract_kind  "
        ;

    wp.daoTable = "bil_contract p left join crd_idno c on p.id_p_seqno = c.id_p_seqno";
    wp.whereOrder = " order by status_code desc, p.mod_time desc, p.contract_no ";
    if (getWhereStr() != true)
      return;

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      //alertErr(appMsg.errCondNodata);
      alertErr("KEY值未輸入或未輸入完整，無法讀取資料");
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

    mKkContractNo = wp.itemStr("kk_contract_no");
    if (empty(mKkContractNo)) {
      mKkContractNo = itemKk("data_k1");
    }

    mKkContractSeqNo = wp.itemStr("kk_contract_seq_no");
    if (empty(mKkContractSeqNo)) {
      mKkContractSeqNo = itemKk("data_k2");
    }


    if (empty(mKkContractNo)) {
      mKkContractNo = wp.colStr("contract_no");
    }


    if (empty(mKkContractSeqNo)) {
      mKkContractSeqNo = wp.colStr("contract_seq_no");
    }



    wp.selectSQL = "hex(c.rowid) as rowid, c.mod_seqno " 
    	+ ", c.contract_no "
        + ", c.contract_seq_no " 
    	+ ", c.contract_kind " 
        + ", c.allocate_flag " 
    	+ ", c.mcht_no "
        + ", c.mcht_chi_name " 
    	+ ", c.product_no " 
        + ", c.product_name " 
    	+ ", c.cvv2 "
    	+ ", c.new_it_flag "
        + ", c.card_no " 
    	+ ", c.acct_type " 
        + ", c.limit_end_date " 
    	+ ", c.vip_code "
        + ", c.first_post_date " 
    	+ ", c.post_cycle_dd " 
        + ", c.purchase_date " 
    	+ ", c.stmt_cycle "
        + ", c.install_curr_term " 
    	+ ", c.all_post_flag " 
        + ", c.forced_post_flag "
        + ", c.fee_flag " 
        + ", c.reference_no " 
        + ", c.payment_type " 
        + ", c.batch_no "
        + ", c.cps_flag " 
        + ", c.film_no " 
        + ", c.auth_code " 
        + ", c.ccas_resp_code "
        + ", c.tot_amt " 
        + ", c.qty " 
        + ", c.exchange_amt " 
        + ", c.unit_price "
        + ", c.install_tot_term " 
        + ", c.install_tot_term as install_tot_term_a" 
        + ", c.unit_price "
        + ", c.redeem_amt " 
        + ", c.redeem_point " 
        + ", c.first_remd_amt " 
        + ", c.remd_amt "
        + ", c.auto_delv_flag " 
        + ", c.extra_fees " 
        + ", c.fees_fix_amt " 
        + ", c.fees_rate "
        + ", c.clt_fees_amt " 
        + ", c.clt_unit_price " 
        + ", c.clt_install_tot_term "
        + ", c.clt_remd_amt " 
        + ", c.zip_code " 
        + ", c.receive_address " 
        + ", c.receive_name "
        + ", c.receive_tel " 
        + ", c.receive_tel1 " 
        + ", c.voucher_head " 
        + ", c.uniform_no "
        + ", c.delv_confirm_flag " 
        + ", c.delv_confirm_date " 
        + ", c.delv_date "
        + ", c.delv_batch_no " 
        + ", c.register_no " 
        + ", c.delv_confirm_flag " 
        + ", c.back_card_no "
        + ", c.install_back_term_flag " 
        + ", c.install_back_term " 
        + ", c.refund_qty "
        + ", c.refund_apr_date " 
        + ", c.refund_flag " 
        + ", c.apr_date " 
        + ", c.apr_flag "
        + ", c.mod_user "
        + ", to_char(c.mod_time,'yyyymmdd') as mod_date "
        + ", c.sale_emp_no " 
        + ", c.redeem_kind " 
        + ", UF_IDNO_ID(c.id_p_seqno) as db_id"
        + ", UF_IDNO_NAME(c.card_no) as db_name" 
        + ", m.loan_flag" 
        + ", i.chi_name" 
        + ", i.id_no"
        + ", i.birthday" 
        + ", a.reference_seq" 
        + ", a.acaj_amt" 
        + ", t.vip_code" 
        + ", t.acct_type"
        + ", t.acct_key"
        + ", decode(c.refund_apr_flag,'y',case when (c.qty - c.refund_qty) * c.tot_amt - c.exchange_amt - c.redeem_amt <'0' then '0' else (c.qty - c.refund_qty) * c.tot_amt - c.exchange_amt - c.redeem_amt end,case when c.qty * c.tot_amt - c.exchange_amt - c.redeem_amt <'0' then '0' else c.qty * c.tot_amt - c.exchange_amt - c.redeem_amt end )  as refundamt";

    wp.daoTable = "bil_contract c left join bil_merchant m on c.mcht_no=m.mcht_no  "
        + "               left join crd_idno i on c.id_p_seqno=i.id_p_seqno  "
        + "               left join bil_contract_acaj a on c.contract_no=a.contract_no and c.contract_seq_no=a.contract_seq_no  "
        + "               left join act_acno t on c.acno_p_seqno=t.p_seqno   ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  c.contract_no = :contract_no and c.contract_seq_no = :contract_seq_no ";
    setString("contract_no", mKkContractNo);
    setString("contract_seq_no", mKkContractSeqNo);

    pageSelect();

    if (sqlNotFind()) {
      //alertErr("查無資料, contract_no=" + mKkContractNo);
      alertErr("KEY值未輸入或未輸入完整，無法讀取資料");
    }

    if (empty(wp.colStr("redeem_kind"))) {
      wp.colSet("redeem_kind", "0");
    }


  }

  @Override
  public void initPage() {
    wp.colSet("kk_contract_seq_no", "1");

    if (empty(wp.itemStr("purchase_date"))) {
      wp.colSet("purchase_date", getSysDate());
    }
    wp.colSet("sale_emp_no",wp.loginUser);	
  }

  @Override
  public void saveFunc() throws Exception {


    func = new Bilm0110Func(wp);

    if(strAction.equals("A") || strAction.equals("U")){
    	if( wp.itemStr("purchase_date").compareTo(getSysDate()) > 0){
     		errmsg("消費日不可大於系統日 !");
    		return;
     	}
    }

    int llCheckCardno = 0;
    llCheckCardno = wfCheckCardno(wp.itemStr("card_no"));
    switch (llCheckCardno) {

      case -1:
        alertErr("此為無效卡 1");
        return;

      case 2:
        alertErr("此團代不符 (check office_time) !");
        return;

      case 3:
        alertErr("此為停用卡 !!");
        return;

      case 4:
        alertErr("此為分期戶 !!");
        return;

      case 5:
        alertErr("此為 催收/呆帳 !!");
        return;

      case 6:
        alertErr("此紅利帳戶主檔不存在  !!");
        return;

      case 7:
        alertErr("此紅利帳戶主檔小於可抵減數 !!");
        return;

      case 8:
        alertErr("此為商務卡, 不能分期 !!");
        return;

    }

    // -2為刪除授權失敗, <20221108 Jeff 先點掉, 不跑授權 >
    //if (ofcUpdatebefore() <= 0) {
    //  return;
    //} ;


    if (empty(wp.itemStr("qty")) || wp.itemNum("qty") <= 0) {
      alertErr("訂購數量不可小於0");
      return;
    }
    String lsContractNo = "";
    String lsContractSeqNo = "";
    if (strAction.equals("A")) {
      String lsSqlc =
          " select substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) as cseq from dual";
      sqlSelect(lsSqlc);

      lsContractNo = sqlStr("cseq");
      System.out.println("ls_contract_no : " + lsContractNo);
      lsContractSeqNo = "1";
      wp.colSet("contract_no", lsContractNo);
      wp.colSet("contract_seq_no", lsContractSeqNo);
      func.varsSet("aa_contract_no", lsContractNo);
      func.varsSet("aa_contract_seq_no", lsContractSeqNo);
    }


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
      dddwSelect();
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

    StringBuffer strMchtNo = new StringBuffer();
    strMchtNo.append("where  1=1 and mcht_no = ");
    strMchtNo.append("'");
    strMchtNo.append(wp.colStr("mcht_no"));
    strMchtNo.append("'");
    strMchtNo.append(" order by product_no");
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        // wp.initOption ="--";
        // wp.optionKey = wp.col_ss("mcht_no");
        // this.dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
        // mcht_status = '1' order by mcht_no fetch first 99999 rows only ");
        wp.optionKey = wp.colStr("product_no");
        this.dddwList("dddw_product_no", "bil_prod", "product_no",
            "product_no||'_'||product_name||'('||mcht_no||')'", strMchtNo.toString());

        wp.initOption = "--";
        wp.optionKey = wp.colStr("sale_emp_no");
        this.dddwList("dddw_sale_emp_no", "sec_user", "usr_id", "usr_cname",
            " where 1=1 order by usr_id ");
      }
      // this.dddw_list("dddw_product_no", "", "", "", "");
    } catch (Exception ex) {
    }
  }

  // 20200106 modify AJAX
  public int itemChange() throws Exception {
    // super.wp = wr;
    String val = wp.itemStr("val");
    String name = wp.itemStr("name");
    wp.addJSON("name", name);
    switch (name) {
      case "mcht_no":
        dddwOption(val);
        break;
      case "product_no":
        String mchtNo = wp.itemStr("mcht_no");
        String delvConfirmDate = wp.itemStr("delv_confirm_date");
        if (wfGetProddata(val, mchtNo, delvConfirmDate) != 1)
          break;
        double llRedeemAmt = wp.itemNum("redeem_amt");
        String lsEedeemKind = wp.itemStr("redeem_kind");
        if (llRedeemAmt < 1 && !lsEedeemKind.equals("0")) {
          alertErr("此商品~檔無法使用紅利折抵 !!");
        }
        break;
      case "card_no":
        String lsSql = "select c.card_no," 
        	+ "c.id_p_seqno," 
        	+ "c.new_end_date," 
        	+ "i.id_no,"
            + "substrb(i.chi_name,1,10) as chi_name ," 
        	+ "i.birthday," 
            + "a.vip_code,"
            + "a.acct_type," 
            + "a.acct_key," 
            + "a.stmt_cycle,"
            + "i.home_area_code1||'-'||i.home_tel_no1||'-'||i.home_tel_ext1 as ls_tel ,"
            + " substr((i.office_area_code1||'-'||i.office_tel_no1||'-'||i.office_tel_ext1),1,17) as ls_tel1 ,"
            + "a.bill_sending_zip ,"
            + "a.bill_sending_addr1||a.bill_sending_addr2||a.bill_sending_addr3||a.bill_sending_addr4||a.bill_sending_addr5 as ls_mail_addr "
            + " from crd_card c left join crd_idno i on c.id_p_seqno=i.id_p_seqno   "
            + "                 left join act_acno a on c.acno_p_seqno=a.acno_p_seqno "
            + " where c.card_no = :ls_card_no ";
        setString("ls_card_no", wp.itemStr("card_no"));
        sqlSelect(lsSql);
        wp.addJSON("j_acct_type", sqlStr("acct_type"));
        wp.addJSON("j_acct_key", sqlStr("acct_key"));
        wp.addJSON("j_limit_end_date", sqlStr("new_end_date"));
        wp.addJSON("j_db_name", sqlStr("chi_name"));
        wp.addJSON("j_db_id", sqlStr("id_no"));
        wp.addJSON("j_vip_code", sqlStr("vip_code"));
        wp.addJSON("j_birthday", sqlStr("birthday"));
        wp.addJSON("j_stmt_cycle", sqlStr("stmt_cycle"));

        wp.addJSON("j_bill_sending_zip", sqlStr("bill_sending_zip"));
        wp.addJSON("j_ls_mail_addr", sqlStr("ls_mail_addr"));
        wp.addJSON("j_db_r_name", sqlStr("chi_name"));
        wp.addJSON("j_ls_tel", sqlStr("ls_tel"));
        wp.addJSON("j_ls_tel1", sqlStr("ls_tel1"));



        break;
    }
    return 1;
  }

  int wfGetProddata(String val, String mchtNo, String delvConfirmDate) throws Exception {
    String[] lsVal = new String[6];
    String[] ldVal = new String[15];
    double llCltUnitPrice = 0;
    String cltInstallTotTerm = "", cltRemdAmt = "", installTotTerm = "";
    installTotTerm = wp.itemStr("install_tot_term");

    if (empty(val)) {
      wp.addJSON("product_name", "");
      // wp.addJSON("mcht_no","");
      // wp.addJSON("mcht_chi_name","");
      wp.addJSON("unit_price", "0");
      wp.addJSON("tot_amt", "0");
      installTotTerm = "0";
      wp.addJSON("install_tot_term", installTotTerm);
      wp.addJSON("auto_delv_flag", "Y");
      wp.addJSON("against_num", "0");
      wp.addJSON("fees_fix_amt", "0");
      wp.addJSON("fees_rate", "0");
      wp.addJSON("extra_fees", "0");
      wp.addJSON("remd_amt", "0");
      wp.addJSON("first_remd_amt", "0");
      wp.addJSON("wk_tot_amt", "0");
      wp.addJSON("clt_fees_amt", "0");
      wp.addJSON("clt_unit_price", "0");
      wp.addJSON("clt_install_tot_term", "0");
      wp.addJSON("clt_remd_amt", "0");
      wp.addJSON("redeem_point", "0");
      wp.addJSON("redeem_amt", "0");
      return 0;
    }

    String sqlSelect = "select mcht_type from bil_merchant where mcht_no = :ls_merchant ";
    setString("ls_merchant", mchtNo);
    sqlSelect(sqlSelect);
    String lsMerchantType = sqlStr("mcht_type");
    if (empty(lsMerchantType)) {
      lsMerchantType = "1";
    }
    if (lsMerchantType.equals("2")) {
      sqlSelect = "select product_name" 
    	  + ",mcht_no" 
    	  + ",unit_price" 
    	  + ",tot_amt" 
    	  + ",tot_term"
          + ",'Y' as ls_val4" 
    	  + ",fees_fix_amt" 
          + ",interest_rate" 
    	  + ",extra_fees"
          + ",'0' as ld_val7" 
    	  + ",clt_fees_fix_amt" 
          + ",clt_interest_rate" 
    	  + ",'1' as ld_val10"
          + ",'1000000' as ld_val11" 
    	  + ",installment_flag " 
          + "from bil_prod_nccc "
          + "where product_no = :ls_prod " 
          + "and mcht_no = :ls_merchant "
          + "and start_date <= :ls_confirm_date " 
          + "and end_date >= :ls_confirm_date2 "
          + "fetch first 1 row only ";
      setString("ls_prod", val);
      setString("ls_merchant", mchtNo);
      setString("ls_confirm_date", delvConfirmDate);
      setString("ls_confirm_date2", delvConfirmDate);
      sqlSelect(sqlSelect);
      lsVal[1] = sqlStr("product_name");
      lsVal[2] = sqlStr("mcht_no");
      ldVal[1] = sqlStr("unit_price");
      ldVal[2] = sqlStr("tot_amt");
      ldVal[3] = sqlStr("tot_term");
      lsVal[4] = sqlStr("ls_val4");
      ldVal[4] = sqlStr("fees_fix_amt");
      ldVal[5] = sqlStr("interest_rate");
      ldVal[6] = sqlStr("extra_fees");
      ldVal[7] = sqlStr("ld_val7");
      ldVal[8] = sqlStr("clt_fees_fix_amt");
      ldVal[9] = sqlStr("clt_interest_rate");
      ldVal[10] = sqlStr("ld_val10");
      ldVal[11] = sqlStr("ld_val11");
      lsVal[5] = sqlStr("installment_flag");
      if (empty(lsVal[5])) {
        lsVal[5] = "N";
      }
    } else {
      sqlSelect = "select product_name" 
    	  + ",mcht_no" 
    	  + ",unit_price" 
    	  + ",tot_amt" 
    	  + ",tot_term"
          + ",auto_delv_flag" 
    	  + ",fees_fix_amt" 
          + ",interest_rate" 
    	  + ",extra_fees" 
          + ",against_num"
          + ",clt_fees_fix_amt" 
          + ",clt_interest_rate" 
          + ",clt_fees_min_amt " 
          + ",clt_fees_max_amt "
          + ",installment_flag " 
          + ",redeem_point " 
          + ",redeem_amt " 
          + ",remd_amt "
          + "from bil_prod " 
          + "where product_no = :ls_prod " 
          + "and mcht_no = :ls_merchant ";
      setString("ls_prod", val);
      setString("ls_merchant", mchtNo);


      sqlSelect(sqlSelect);
      lsVal[1] = sqlStr("product_name");
      lsVal[2] = sqlStr("mcht_no");
      ldVal[1] = sqlStr("unit_price");
      ldVal[2] = sqlStr("tot_amt");
      ldVal[3] = sqlStr("tot_term");
      lsVal[4] = sqlStr("auto_delv_flag");
      ldVal[4] = sqlStr("fees_fix_amt");
      ldVal[5] = sqlStr("interest_rate");
      ldVal[6] = sqlStr("extra_fees");
      ldVal[7] = sqlStr("against_num");
      ldVal[8] = sqlStr("clt_fees_fix_amt");
      ldVal[9] = sqlStr("clt_interest_rate");
      ldVal[10] = sqlStr("clt_fees_min_amt");
      ldVal[11] = sqlStr("clt_fees_max_amt");
      lsVal[5] = sqlStr("installment_flag");
      ldVal[12] = sqlStr("redeem_point");
      ldVal[13] = sqlStr("redeem_amt");
      ldVal[14] = sqlStr("remd_amt");
    }
    lsVal[3] = wp.itemStr("mcht_chi_name");
    if (sqlRowNum > 0) {
      for (int i = 1; i <= 11; i++) {
        if (empty(ldVal[i])) {
          ldVal[i] = "0";
        }
      }
      wp.addJSON("product_name", lsVal[1]);
      wp.addJSON("unit_price", ldVal[1]);
      wp.addJSON("tot_amt", ldVal[2]);
      installTotTerm = ldVal[3];
      wp.addJSON("install_tot_term", installTotTerm);
      wp.addJSON("auto_delv_flag", empty(lsVal[4]) ? "Y" : lsVal[4]);
      wp.addJSON("fees_fix_amt", ldVal[4]);
      wp.addJSON("fees_rate", ldVal[5]);
      wp.addJSON("extra_fees", ldVal[6]);
      wp.addJSON("against_num", ldVal[7]);
      wp.addJSON("redeem_point", ldVal[12]);
      wp.addJSON("redeem_amt", ldVal[13]);
      wp.addJSON("exchange_amt", "0");
      wp.addJSON("qty", "1");
      wp.addJSON("first_remd_amt", formatDouble1(this.toNum(ldVal[2])
          - (this.toNum(ldVal[1]) * this.toNum(ldVal[3])) - this.toNum(ldVal[13])));
      wp.addJSON("db_tot_amount", ldVal[2]);
      wp.addJSON("wk_tot_amt", ldVal[2]);
      wp.addJSON("ls_clt_interest_rate", ldVal[9]);
      wp.addJSON("ls_clt_fees_fix_amt", ldVal[8]);
      wp.addJSON("installment_flag", lsVal[5]);
      // 計算客戶手續費 = 商品總金額 * 客戶手續費率 + 客戶手續費固定金額
      double ldCltFeesAmt =
          (this.toNum(ldVal[2]) * this.toNum(ldVal[9]) / 100) + this.toNum(ldVal[8]);
      if (ldCltFeesAmt < this.toNum(ldVal[10])) {
        ldCltFeesAmt = this.toNum(ldVal[10]);
      } else if (ldCltFeesAmt > this.toNum(ldVal[11])) {
        ldCltFeesAmt = this.toNum(ldVal[11]);
      }

      wp.addJSON("clt_fees_amt", formatDouble1(ldCltFeesAmt));
      if (lsVal[5].equals("Y")) {
        cltInstallTotTerm = ldVal[3];
        wp.addJSON("clt_install_tot_term", cltInstallTotTerm);
        // double ld_tmp_price = (ld_clt_fees_amt/this.to_Num(ld_val[3]))-0.499;
        double ldTmpPrice = Math.floor((ldCltFeesAmt / this.toNum(ldVal[3])));
        llCltUnitPrice = ldTmpPrice;

      } else {
        cltInstallTotTerm = "1";
        wp.addJSON("clt_install_tot_term", cltInstallTotTerm);
        llCltUnitPrice = ldCltFeesAmt;

      }

      cltRemdAmt = formatDouble1(ldCltFeesAmt - (llCltUnitPrice * toNum(cltInstallTotTerm)));
      if (ldCltFeesAmt < 0) {
        llCltUnitPrice = 0;
        cltRemdAmt = "0";
      }

      wp.addJSON("clt_unit_price", formatDouble1(llCltUnitPrice));
      wp.addJSON("clt_remd_amt", cltRemdAmt);

    } else {
      wp.addJSON("product_name", "");
      wp.addJSON("mcht_no", "");
      wp.addJSON("mcht_chi_name", "");
      wp.addJSON("unit_price", "0");
      wp.addJSON("tot_amt", "0");
      installTotTerm = "0";
      wp.addJSON("install_tot_term", installTotTerm);
      wp.addJSON("auto_delv_flag", "N");
      wp.addJSON("against_num", "0");
      wp.addJSON("fees_fix_amt", "0");
      wp.addJSON("fees_rate", "0");
      wp.addJSON("extra_fees", "0");
      wp.addJSON("remd_amt", "0");
      wp.addJSON("first_remd_amt", "0");
      wp.addJSON("wk_tot_amt", "0");
      wp.addJSON("clt_fees_amt", "0");
      wp.addJSON("clt_unit_price", "0");
      wp.addJSON("clt_install_tot_term", "0");
      wp.addJSON("clt_remd_amt", "0");
      wp.addJSON("redeem_point", "0");
      wp.addJSON("redeem_amt", "0");
    }
    if (this.toNum(installTotTerm) > 0) {
      wp.addJSON("contract_kind", "1");
    } else {
      wp.addJSON("contract_kind", "2");
    }

    return 1;
  }

  int ofcUpdatebefore() throws Exception {

    // check
    if (empty(wp.itemStr("card_no"))) {
      alertErr("卡號不可空白!!");
      return -1;
    }

    if (empty(wp.itemStr("limit_end_date"))) {
      alertErr("效期不可空白!!");
      return -1;
    }

    if (strAction.equals("A") || strAction.equals("D")) {
      String lsTransType = "";
      if (strAction.equals("A")) {
        lsTransType = "1";
      } else {
        lsTransType = "2";
      }
      //
      String lsLoanFlag = wp.itemStr("db_loan_flag");
      String lsAuthorization = wp.itemStr("auth_code");
      String lsContractKind = wp.itemStr("contract_kind");
      double ldcAmt = (wp.itemNum("tot_amt") * wp.itemNum("qty")) + wp.itemNum("extra_fees")
          - wp.itemNum("exchange_amt") - wp.itemNum("redeem_amt");
      if (ldcAmt < 1) {
        ldcAmt = 1;
      }
      String subType = "";
      String sqlSelect = "select mcc_code from bil_merchant where mcht_no = :ls_merchant_no ";
      setString("ls_merchant_no", wp.itemStr("mcht_no"));
      sqlSelect(sqlSelect);
      String lsMcc = sqlStr("mcc_code");

      String sqlSelectIp = "select wf_value,wf_value2 from ptr_sys_parm where wf_parm='SYSPARM' and WF_KEY='CCASLINK'";
      sqlSelect(sqlSelectIp);
      String wfValue = sqlStr("wf_value");
      String wfValue2 = sqlStr("wf_value2");
      if (empty(wfValue)) {
        alertErr("無法取得IP位置");
        return -1;
      }

      AuthGateway authGatewayTest = new AuthGateway();
      AuthData lAuthData = new AuthData();
      lAuthData.setCardNo(wp.itemStr("card_no"));
      lAuthData.setCvv2(wp.itemStr("cvv2"));
      lAuthData.setExpireDate(wp.itemStr("limit_end_date")); /** YYYYMMDD */
      lAuthData.setLocalTime(wp.sysDate + wp.sysTime); /* yyyymmdzdhhmmss when trans_type=3 */ // Howard:不知道此欄位要放到
                                                                                                // //
                                                                                                // ISO
      // 的哪個欄位中.....
      lAuthData.setMccCode(lsMcc); /* bit18 mcc code */
      lAuthData.setMchtNo(wp.itemStr("mcht_no"));/* bit42 acceptor_id=mcht_no */
      lAuthData.setOrgAuthNo(wp.itemStr("auth_code"));
      lAuthData.setOrgRefNo("");
      lAuthData.setTransAmt(formatDouble1(ldcAmt));
      lAuthData.setTransType(lsTransType);/* 1: regular 2:refund 3:reversal 4:代行 */

      if (!empty(lsAuthorization) && strAction.equals("D") && lsLoanFlag.equals("N")) {
        subType = "B";
      }
      if (empty(lsAuthorization) && !strAction.equals("D") && lsLoanFlag.equals("N")) {
        subType = "A";
      }
      if (lsContractKind.equals("2")) {
        if (empty(lsAuthorization) && !strAction.equals("D") && lsLoanFlag.equals("N")) {
          subType = "B";
        }
      }
      lAuthData.setTypeFlag(subType);/// * A: install B: mail*/
      String sLTranxResult = authGatewayTest.startProcess(lAuthData, wfValue, wfValue2);
      // wp.alertMesg = "<script language='javascript'> alert('"+sL_TranxResult+"')</script>";
      // sL_TranxResult 八碼 00 成功 加六碼授權碼
      String ccasRespCode = strMid(sLTranxResult, 0, 2);
      String authCode = "";
      wp.showLogMessage("I", "", "ccas_resp_code :" + ccasRespCode);
      if (ccasRespCode.equals("00")) {
        // suc
        if (sLTranxResult.length() >= 8) {
          authCode = strMid(sLTranxResult, 2, 6);
        }
        wp.showLogMessage("I", "", "auth_code :" + authCode);
        func.varsSet("auth_code", authCode);
      } else {
        // err
        if (strAction.equals("D")) {
          alertMsg("授權失敗," + ccasRespCode + "   ,不能刪除!");
          func.varsSet("ccas_resp_code", ccasRespCode);
          return -2;
        }
        alertMsg("授權失敗," + ccasRespCode);
        func.varsSet("ccas_resp_code", ccasRespCode);
        return -1;
      }

      wp.colSet("ccas_resp_code", ccasRespCode);
      wp.colSet("auth_code", authCode);
    }
    return 1;
  }

  void dddwOption(String val) throws Exception {
    String sqlSelect = "select mcht_chi_name, " 
        + "nvl(loan_flag,'N') as loan_flag, "
        + "nvl(trans_flag,'N') as trans_flag " 
        + "from	bil_merchant where MCHT_NO = :val ";
    setString("val", val);
    sqlSelect(sqlSelect);
    wp.addJSON("mcht_chi_name", sqlStr("mcht_chi_name"));
    wp.addJSON("loan_flag", sqlStr("loan_flag"));
    wp.addJSON("product_name", "");
    String dddwWhere = "", option = "";
    if (!empty(val))
      dddwWhere = " and mcht_no = :val ";
    String selectBilProd =
        "select product_no " + " ,product_no||'_'||product_name||'('||mcht_no||')' as product_name "
            + " from bil_prod " + " where 1=1 " + dddwWhere + " order by product_no ";
    setString("val", val);
    sqlSelect(selectBilProd);

    option += "<option value=\"\">--</option>";
    for (int ii = 0; ii < sqlRowNum; ii++) {
      option += "<option value=\"" + sqlStr(ii, "product_no") + "\" ${tot_term-"
          + sqlStr(ii, "product_no") + "} >" + sqlStr(ii, "product_name") + "</option>";
    }
    wp.addJSON("dddw_product_no2", option);

  }


  public static String formatDouble1(double d) {
    DecimalFormat df = new DecimalFormat("0");
    return df.format(d);
  }

  int wfCheckCardno(String cardNo) throws Exception{

    int lsOppostDate = 0, lsCurrentCode = 0, lsOnlineDate = 0, lsStatusChangeDate = 0,
        lsEndTranDp = 0, lsAgainstNum = 0, lsRedeemPoint = 0, liNetNp = 0;
    String lsPSeqno = "", lsActType = "", lsActKey = "", lsPayByStageFlag = "", lsAcctStatus = "",
        lsCardIndicator = "", lsIdPSeqno = "";

    String sqlSelectDate = "select online_date from ptr_businday";
    sqlSelect(sqlSelectDate);
    lsOnlineDate = (int) sqlNum("online_date");

    String sqlSelect = "select sup_flag," 
        + "  nvl(major_card_no,' ') as major_card_no,"
        + "  acct_type," 
        + "  id_p_seqno," 
        + "  new_end_date, " 
        + "  current_code,"
        + "  oppost_date," 
        + "  acno_p_seqno ," 
        + "  p_seqno ,"
        + "  nvl(indiv_inst_lmt,0)*nvl(indiv_crd_lmt,0)/100 as amt"
        + " from crd_card where card_no = :ls_card_no ";
    setString("ls_card_no", cardNo);
    sqlSelect(sqlSelect);
    lsCurrentCode = (int) sqlNum("current_code");
    lsOppostDate = (int) sqlNum("oppost_date");
    lsPSeqno = sqlStr("acno_p_seqno");

    if (lsCurrentCode != 0) {
      return -1;// 無效
    }

    String sqlAct = " select acct_type, " 
    	+ "  acct_key ," 
    	+ "  pay_by_stage_flag , "
        + "  acct_status, " 
    	+ "  status_change_date, " 
        + "  id_p_seqno " 
    	+ " from act_acno "
        + " where acno_p_seqno = :ls_p_seqno ";
    setString("ls_p_seqno", lsPSeqno);
    sqlSelect(sqlAct);

    if (sqlRowNum <= 0) {
      return -1;// 無效
    }

    lsActType = sqlStr("acct_type");
    lsActKey = sqlStr("acct_key");
    lsIdPSeqno = sqlStr("id_p_seqno");
    lsPayByStageFlag = sqlStr("pay_by_stage_flag");
    lsAcctStatus = sqlStr("acct_status");
    lsStatusChangeDate = sqlInt("status_change_date");



    if (lsCurrentCode != 0 && lsOppostDate > 0 && (lsOnlineDate >= lsOppostDate)) {
      return 3;// 停用
    }

    if (!empty(lsPayByStageFlag) && !lsPayByStageFlag.equals("00")) {
      return 4; // 分期
    }

    if ((lsAcctStatus.equals("3") || lsAcctStatus.equals("4"))
        && (lsOnlineDate > lsStatusChangeDate)) {
      return 5;// 催收 :呆帳
    }


    String sqlPtract = "select nvl(card_indicator ,'1') as card_indicator " + " from ptr_acct_type "
        + " where acct_type = :ls_acct_type ";
    setString("ls_acct_type", lsActType);
    sqlSelect(sqlPtract);
    lsCardIndicator = sqlStr("card_indicator");

    if (lsCardIndicator.equals("2")) {
      return 8;//
    }
    // 0823取消check紅利檔
    // String sql_bonus = "select end_tran_bp "
    // + " from dbm_bonus_dtl "
    // + " where acct_type=:ls_acct_type and id_p_seqno = :ls_id_p_seqno ";
    // setString("ls_acct_type",ls_act_type);
    // setString("id_p_seqno",ls_id_p_seqno);
    // sqlSelect(sql_bonus);
    // System.out.println("EES:"+ls_act_type);
    // System.out.println("EES:"+ls_id_p_seqno);
    // ls_end_tran_bp= (int) sql_num("end_tran_bp");
    //
    // if (sql_nrow <= 0) {
    // return 6;//紅利主檔不存
    // }
    //
    //
    //
    // if(is_action.equals("U")){
    // ls_against_num=(int) wp.item_num("against_num");
    // ls_redeem_point=(int) wp.item_num("redeem_point");
    // li_net_np=li_net_np+ls_against_num+ls_redeem_point;
    // }
    //
    // if(li_net_np < ls_against_num+ls_redeem_point){
    // return 7;//小於紅利主檔
    // }


    return 1;
  }

  public void processAjaxOption1() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " 
    	+ " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " 
    	+ " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";

    setString("mcht_no", wp.getValue("a_mcht_no", 0) + "%");
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    wp.addJSON("ex", "j_mcht_no");
    return;
  }


}
