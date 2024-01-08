/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-04  V1.00.00  yash       program initial                            *
* 106-12-14            Andy		  update : program name : Bili0010==>Bilq0010*
* 107-03-14  V1.00.01  Andy       Update dddw_list merchant UI               *
* 109-04-23  V1.00.02  shiyuqi       updated for project coding standard     *
* 110-10-15  V1.00.04  Yang Bo    joint sql replace to parameters way        *
******************************************************************************/

package bilq01;



import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilq0010 extends BaseEdit {
  String mKkContractNo = "", mKkContractSeqNo = "";

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    } else if (eqIgno(wp.buttonCode, "ItemChange")) {
      /* TEST */
      strAction = "ItemChange";
      itemChange();
    }


    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    
    if(wp.itemEmpty("ex_contract_no")&&wp.itemEmpty("ex_card_no")&&wp.itemEmpty("ex_merchant")
    		&&wp.itemEmpty("ex_product")&&wp.itemEmpty("ex_idno")&&wp.itemEq("ex_op","3")) {
    	if(wp.itemEmpty("ex_purchase_date1")&&wp.itemEmpty("ex_purchase_date2")) {
    		alertErr("消費日期不能為空值");
    		return false;
    	}
    }
    
    if (empty(wp.itemStr("ex_contract_no")) == false) {
      wp.whereStr += " and  p.contract_no like :ex_contract_no ";
      setString("ex_contract_no", wp.itemStr("ex_contract_no") + "%");
    }
    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  p.card_no like :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no") + "%");
    }
    if (empty(wp.itemStr("ex_merchant")) == false) {
      wp.whereStr += " and  p.mcht_no like :ex_merchant ";
      setString("ex_merchant", wp.itemStr("ex_merchant") + "%");
    }
    if (empty(wp.itemStr("ex_product")) == false) {
      wp.whereStr += " and  p.product_no like :ex_product ";
      setString("ex_product", wp.itemStr("ex_product") + "%");
    }
    if (empty(wp.itemStr("ex_idno")) == false) {
      wp.whereStr += " and c.id_no = :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno"));
    }
    if (empty(wp.itemStr("ex_op")) == false) {
      switch (wp.itemStr("ex_op")) {
        case "1":
          wp.whereStr += " and  p.cps_flag = 'Y' ";
          break;
        case "2":
          wp.whereStr += " and  p.cps_flag = 'N' ";
          break;
        case "4":
          wp.whereStr += " and  p.cps_flag = 'C' ";
          break;
      }
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
    if(!getWhereStr()) {
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

    wp.selectSQL = " p.contract_no " + ",p.contract_seq_no "
        + ",decode(p.contract_kind,'1','1-分期','2','2-郵購',p.contract_kind) as contract_kind  "
        + ",p.card_no " + ",p.auth_code" + ",p.product_no " + ",p.product_name " + ",p.cps_flag"
        + ",p.mcht_no " + ",p.mcht_chi_name " + ",p.qty" + ",p.exchange_amt" + ",p.apr_flag "
        + ",p.tot_amt "
        + ",decode(p.refund_apr_flag,'Y',(p.qty-refund_qty)*p.tot_amt-p.exchange_amt,p.qty*p.tot_amt-p.exchange_amt) as wk_tot_amt"
        + ",p.unit_price" + ",p.install_tot_term" + ",p.install_curr_term" + ",p.remd_amt"
        + ",p.refund_qty" + ",decode(p.spec_flag,'Y','Y','N') spec_flag" + ",decode(p.allocate_flag,'Y','Y','N') allocate_flag" + ",p.purchase_date";

    wp.daoTable = "bil_contract p left join crd_idno c on p.id_p_seqno = c.id_p_seqno";
    wp.whereOrder = " order by p.purchase_date,p.contract_no";
//    getWhereStr();

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
    mKkContractNo = wp.itemStr("kk_contract_no");
    if (empty(mKkContractNo)) {
      mKkContractNo = itemKk("data_k1");
    }
    mKkContractSeqNo = wp.itemStr("kk_contract_seq_no");
    if (empty(mKkContractSeqNo)) {
      mKkContractSeqNo = itemKk("data_k2");
    }


    wp.selectSQL = "hex(c.rowid) as rowid, c.mod_seqno " + ", c.contract_no , contract_kind"
        + ", c.contract_seq_no " + ", decode(c.redeem_kind,'Y','Y','N')  redeem_kind " + ", decode(c.allocate_flag,'Y','Y','N') allocate_flag " + ", c.mcht_no "
        + ", c.mcht_chi_name " + ", c.product_no " + ", c.product_name " + ", c.cvv2 " + ", decode(c.spec_flag,'Y','Y','N') spec_flag "
        + ", c.card_no " + ", c.acct_type " + ", c.limit_end_date " + ", c.vip_code "
        + ", c.first_post_date " + ", c.post_cycle_dd " + ", c.purchase_date " + ", c.stmt_cycle "
        + ", c.install_curr_term " + ", c.all_post_flag " + ", c.forced_post_flag "
        + ", c.fee_flag " + ", c.reference_no " + ", c.payment_type " + ", c.batch_no "
        + ", c.cps_flag " + ", c.film_no " + ", c.auth_code " + ", c.ccas_resp_code "
        + ", c.tot_amt " + ", c.qty " + ", c.exchange_amt " + ", c.unit_price "
        + ", c.install_tot_term " + ", c.unit_price " + ", c.redeem_amt " + ", c.redeem_point "
        + ", c.first_remd_amt " + ", c.remd_amt " + ", c.auto_delv_flag " + ", c.extra_fees "
        + ", c.fees_fix_amt " + ", c.fees_rate " + ", c.clt_fees_amt " + ", c.clt_unit_price "
        + ", c.clt_install_tot_term " + ", c.clt_remd_amt " + ", c.zip_code "
        + ", c.receive_address " + ", c.receive_name " + ", c.receive_tel " + ", c.receive_tel1 "
        + ", c.voucher_head " + ", c.uniform_no " + ", c.delv_confirm_flag "
        + ", c.delv_confirm_date " + ", c.delv_date " + ", c.delv_batch_no " + ", c.register_no "
        + ", c.delv_confirm_flag " + ", c.back_card_no " + ", decode(c.install_back_term_flag,'Y','Y','N') install_back_term_flag "
        + ", c.install_back_term " + ", c.refund_qty " + ", c.refund_apr_date " + ", decode(c.refund_flag,'Y','Y','N') refund_flag "
        + ", c.apr_date " + ", c.apr_flag " + ", UF_IDNO_ID(c.id_p_seqno) as db_id"
        + ", UF_IDNO_NAME(c.card_no) as db_name" + ", m.loan_flag" + ", i.chi_name" + ", i.id_no"
        + ", i.birthday" + ", a.reference_seq" + ", a.acaj_amt"
        + ", decode(c.refund_apr_flag,'y',case when (c.qty - c.refund_qty) * c.tot_amt - c.exchange_amt - c.redeem_amt <'0' then '0' else (c.qty - c.refund_qty) * c.tot_amt - c.exchange_amt - c.redeem_amt end,case when c.qty * c.tot_amt - c.exchange_amt - c.redeem_amt <'0' then '0' else c.qty * c.tot_amt - c.exchange_amt - c.redeem_amt end )  as refundamt";

    wp.daoTable = "bil_contract c left join bil_merchant m on c.mcht_no=m.mcht_no  "
        + "               left join crd_idno i on c.id_p_seqno=i.id_p_seqno  "
        + "               left join bil_contract_acaj a on c.contract_no=a.contract_no and c.contract_seq_no=a.contract_seq_no  ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  c.contract_no = :contract_no and c.contract_seq_no = :contract_seq_no ";
    setString("contract_no", mKkContractNo);
    setString("contract_seq_no", mKkContractSeqNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, contract_no=" + mKkContractNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // Bili0010_func func = new Bili0010_func(wp);

    // rc = func.dbSave(is_action);
    // ddd(func.getMsg());
    // if (rc != 1) {
    // err_alert(func.getMsg());
    // }
    // this.sql_commit(rc);
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
      wp.optionKey = wp.itemStr("ex_merchant");
      this.dddwList("dddw_merchant", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 and mcht_status = '1' group by mcht_no,mcht_chi_name order by mcht_no");
      String exMchtNo = wp.itemStr("ex_merchant");
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_product");
      // this.dddw_list("dddw_product_no", "bil_prod", "product_no", "product_name", "where 1=1 and
      // mcht_no ='"+ex_mcht_no+"' order by product_no");
      // 為下面dddwList方法傳參數
      setString("mcht_no", exMchtNo);
      this.dddwList("dddw_product", "bil_prod", "product_no", "product_name",
          " where  1=1  and mcht_no = :mcht_no order by product_no");
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
    wp.addJSON("ex", "mcht_no");
    return;
  }

  void itemChange() throws Exception {

  }
}
