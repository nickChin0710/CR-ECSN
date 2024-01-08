/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-26  V1.00.00  yash           program initial                        *
* 106-12-14            Andy		  update : program name : Bili0030==>Bilq0030*
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilq01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilq0030 extends BaseEdit {
  String mExReferenceNo = "";

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
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and  card_no like :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno") + "%");
    }
    if (empty(wp.itemStr("ex_bill_unit")) == false) {
      wp.whereStr += " and  bill_type like :ex_bill_unit ";
      setString("ex_bill_unit", wp.itemStr("ex_bill_unit") + "%");
    }

    if (empty(wp.itemStr("ex_batch_no")) == false) {
      wp.whereStr += " and  batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }

    if (empty(wp.itemStr("exDate")) == false) {
      wp.whereStr += " and  acct_month like :exDate ";
      setString("exDate", wp.itemStr("exDate") + "%");
    }

	if (empty(wp.itemStr("ex_curr_code")) == false) {
		wp.whereStr += " and  curr_code = :ex_curr_code ";
		setString("ex_curr_code", wp.itemStr("ex_curr_code"));
	}

	if (empty(wp.itemStr("ex_post_date1")) == false) {
		wp.whereStr += " and  post_date >= :ex_post_date1 ";
		setString("ex_post_date1", wp.itemStr("ex_post_date1"));
	}

	if (empty(wp.itemStr("ex_post_date2")) == false) {
		wp.whereStr += " and  post_date <= :ex_post_date2 ";
		setString("ex_post_date2", wp.itemStr("ex_post_date2"));
	}

    /*
    if (empty(wp.itemStr("ex_flag")) == false) {
      if (wp.itemStr("ex_flag").equals("1")) {
        wp.whereStr += " and  billed_date =''  ";
      } else {
        wp.whereStr += " and  billed_date !=''  ";
      }
    }
    */

    if (empty(wp.itemStr("ex_okflag")) == false) {

      if (wp.itemStr("ex_okflag").equals("1")) {
        wp.whereStr += " and  rsk_type =''   ";
      } else if (wp.itemStr("ex_okflag").equals("2")) {
        wp.whereStr += " and  rsk_type != ''  ";
      } else if (wp.itemStr("ex_okflag").equals("3")) {
        // wp.whereStr += " and (rsk_type ='' or rsk_type !='' ) ";
      }
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // check
    if (!empty(wp.itemStr("ex_batch_no"))) {
      if (wp.itemStr("ex_batch_no").length() < 6) {
        alertErr("請款批號不可小於六碼!");
        return;
      }
    }
    
    if (wp.itemEmpty("ex_cardno")) {
    	alertErr("卡號為必輸入的查詢條件!");
        return;	    
    }
    
    /*
    if (empty(wp.itemStr("exDate")) && empty(wp.itemStr("ex_bill_unit"))
    	&& empty(wp.itemStr("ex_post_date1")) 
    	&& empty(wp.itemStr("ex_post_date2"))
    	&& empty(wp.itemStr("ex_batch_no"))
    	&& wp.itemEmpty("ex_cardno")) {
    	    alertErr("卡號、請款單位、入帳日期區間、請款批號、關帳年月,至少輸入一個查詢條件");
    	    return;	    
    }
	*/

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " card_no" + ", reference_no" + ", batch_no" + ", purchase_date" + ", dest_amt"
        + ", curr_code" + ", decode(sign_flag,'-',dc_dest_amt*-1,dc_dest_amt) dc_dest_amt " + ", bill_type" + ", acct_type" + ", rsk_type"
        + ", mcht_chi_name" + ", mcht_city" + ", mcht_country" + ", mcht_country "
        + ", decode(mcht_chi_name,'',mcht_eng_name,mcht_chi_name) as mcht_name "
        + ", interest_date , ecs_platform_kind ";

    wp.daoTable = "bil_bill";
    // wp.whereOrder=" Fetch First 50 Row Only ";
    wp.whereOrder = " order by card_no,reference_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();


    String lsSql = "select  sum(decode(sign_flag,'-',dc_dest_amt*-1,dc_dest_amt)) as dest_amt from bil_bill ";
    lsSql += " where 1=1 ";
    if (empty(wp.itemStr("ex_cardno")) == false) {
      lsSql += " and  card_no like :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno") + "%");
    }
    if (empty(wp.itemStr("ex_bill_unit")) == false) {
      lsSql += " and  bill_type like :ex_bill_unit ";
      setString("ex_bill_unit", wp.itemStr("ex_bill_unit") + "%");
    }

    if (empty(wp.itemStr("ex_batch_no")) == false) {
      lsSql += " and  batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }

    if (empty(wp.itemStr("exDate")) == false) {
      lsSql += " and  acct_month like :exDate ";
      setString("exDate", wp.itemStr("exDate") + "%");
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      lsSql += " and  curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }

    if (empty(wp.itemStr("ex_post_date1")) == false) {
    	lsSql += " and  post_date >= :ex_post_date1 ";
		setString("ex_post_date1", wp.itemStr("ex_post_date1"));
	}

	if (empty(wp.itemStr("ex_post_date2")) == false) {
		lsSql += " and  post_date <= :ex_post_date2 ";
		setString("ex_post_date2", wp.itemStr("ex_post_date2"));
	}
	
    /*
    if (empty(wp.itemStr("ex_flag")) == false) {
      if (wp.itemStr("ex_flag").equals("1")) {
        lsSql += " and  billed_date =''  ";
      } else {
        lsSql += " and  billed_date !=''  ";
      }
    }
    */

    if (empty(wp.itemStr("ex_okflag")) == false) {

      if (wp.itemStr("ex_okflag").equals("1")) {
        lsSql += " and  rsk_type =''   ";
      } else if (wp.itemStr("ex_okflag").equals("2")) {
        lsSql += " and  rsk_type != ''  ";
      } else if (wp.itemStr("ex_okflag").equals("3")) {
        lsSql += " and  (rsk_type =''  or rsk_type !='' )  ";
      }
    }
    sqlSelect(lsSql);
    wp.colSet("exCnt", intToStr(wp.dataCnt));
    wp.colSet("exAmt", sqlStr("dest_amt"));

  }

  @Override
  public void querySelect() throws Exception {
    mExReferenceNo = wp.itemStr("reference_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExReferenceNo = wp.itemStr("kk_reference_no");
    if (empty(mExReferenceNo)) {
      mExReferenceNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ",reference_no " + " ,batch_no" + " ,card_no"
        + " ,txn_code" + " ,bill_type" + " ,dest_amt" + " ,dest_curr" + " ,source_amt " 
    	+ ", decode(sign_flag,'-',dc_dest_amt*-1,dc_dest_amt) dc_dest_amt "
        + " ,source_curr" + " ,post_date" + " ,curr_code" + " ,purchase_date" + " ,dc_exchange_rate"
        + " ,auth_code" + " ,pos_entry_mode" + " ,film_no" + " ,rsk_type" + " ,process_date"
        + " ,mcht_no" + " ,mcht_city" + " ,mcht_chi_name" + " ,mcht_eng_name" + " ,mcht_country"
        + " ,mcht_state" + " ,mcht_zip" + " ,mcht_category" + " ,acq_member_id" + " ,acct_month"
        + " ,decode(mcht_chi_name,'',mcht_eng_name,mcht_chi_name) as mcht_name "
        + " ,settl_amt " + ", interest_date, ecs_platform_kind ";
    wp.daoTable = "bil_bill";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  reference_no = :reference_no ";
    setString("reference_no", mExReferenceNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, reference_no=" + mExReferenceNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // Bili0020_func func = new Bili0020_func(wp);
    //
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
      wp.optionKey = wp.itemStr("ex_bill_unit");
      this.dddwList("dddw_bill_unit", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", 
    		  "where wf_type = 'DC_CURRENCY' order by wf_id");
      
    } catch (Exception ex) {
    }
  }

}
