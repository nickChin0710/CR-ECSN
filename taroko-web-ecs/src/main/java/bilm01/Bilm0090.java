/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-07  V1.00.00  yash       program initial                            *
* 108-05-02  V1.00.01  Andy       Update Debug                               *
* 109-01-06  V1.00.02  Ru Chen    modify AJAX                                *
* 109-04-23  V1.00.03  shiyuqi    updated for project coding standard        * 
* 109-11-20   V1.00.04  tanwei     默認db中獲取值， 尾數改bug                        *
* 109-11-26   V1.00.05  Justin     fix lots of bugs                              
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *                           *
******************************************************************************/

package bilm01;

import java.util.Arrays;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Bilm0090 extends BaseAction {
  String mchtNo = "", productNo = "";
  String tableName = "";

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
      strAction = "A";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      strAction = "D";
      saveFunc();
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
      // } else if (eq_igno(wp.buttonCode, "AJAX")) {
      // String atype = wp.item_ss("ajax_type");
      // is_action = "AJAX";
      // if (atype.equals("merchant")) {
      // processAjaxOption(atype);
      // }
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    } else if (eqIgno(wp.buttonCode, "itemchange")) {
      /* itemchange */
      itemchanged1();
    } else if (eqIgno(wp.buttonCode, "itemchange2")) {
      /* itemchange */
      itemchanged2();
    } 

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_detl") > 0) {
      if (strAction.equals("new")) {
        //wp.colSet("db_merchant_type", "1");

        wp.colSet("clt_fees_max_amt", "99999");
        wp.colSet("fees_max_amt", "99999");
        wp.colSet("auto_delv_flag", "N");
        wp.colSet("auto_print_flag", "N");
        wp.colSet("refund_flag", "N");

      }
      wp.colSet("trans_rate", "0.00");
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("ex_prod")) && empty(wp.itemStr("ex_merchant"))) {
      alertErr("至少輸入一個查詢條件");
      return false;
    }
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_prod")) == false) {
      wp.whereStr += " and  product_no = :product_no ";
      setString("product_no", wp.itemStr("ex_prod"));
    }
    if (empty(wp.itemStr("ex_merchant")) == false) {
      wp.whereStr += " and  mcht_no = :mcht_no ";
      setString("mcht_no", wp.itemStr("ex_merchant"));
    }
    if (wp.itemStr("ex_apr_flag").equals("Y")) {
      tableName = "bil_prod";
    } else {
      tableName = "bil_prod_t";
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
    if (getWhereStr() != true)
      return;
    wp.selectSQL = " mcht_no" + ", product_no" + ", product_name" + ", tot_term" + ", unit_price"
        + ", remd_amt" + ", tot_amt" + ", extra_fees" + ", clt_fees_fix_amt" + ", clt_interest_rate"
        + ", fees_fix_amt" + ", interest_rate" + ", against_num";

    wp.daoTable = tableName;
    wp.whereOrder = " order by mcht_no";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
//    itemchanged1();
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mchtNo = wp.itemStr("kk_mcht_no");
    productNo = wp.itemStr("kk_product_no");
    if (empty(mchtNo)) {
      mchtNo = itemkk("data_k1");
    }
    if (empty(productNo)) {
      productNo = itemkk("data_k2");
    }

    if (empty(mchtNo)) {
      mchtNo = wp.colStr("mcht_no");
    }
    if (empty(productNo)) {
      productNo = wp.colStr("product_no");
    }
    
    getMchtType(mchtNo);
    if (sqlRowNum > 0) {
        wp.colSet("db_merchant_type", sqlStr("MCHT_TYPE"));
    }

    String lsSql =
        "select count(*) as tot_cnt from bil_prod_t where mcht_no = ? and product_no = ?";
    Object[] param = new Object[] {mchtNo, productNo};
    sqlSelect(lsSql, param);
    if (sqlNum("tot_cnt") > 0) {
      tableName = "bil_prod_t";
    } else {
      tableName = "bil_prod";
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", mcht_no " + ", product_no"
        + ", product_name" + ", trans_rate" + ", risk_code" + ", tot_amt" + ", unit_price"
        + ", tot_term" + ", redeem_point" + ", redeem_amt" + ", remd_amt" + ", extra_fees"
        + ", against_num" + ", clt_fees_fix_amt" + ", interest_rate" + ", installment_flag"
        + ", clt_fees_min_amt" + ", clt_fees_max_amt" + ", year_fees_rate" + ", fees_fix_amt"
        + ", clt_interest_rate" + ", fees_min_amt" + ", fees_max_amt" + ", auto_delv_flag"
        + ", auto_print_flag" + ", refund_flag" + ", confirm_flag";
    wp.daoTable = tableName;
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no and product_no = :product_no";
    setString("mcht_no", mchtNo);
    setString("product_no", productNo);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料,mcht_no=" + mchtNo + ",product_no=" + productNo);
      return;
    }
    
    Double amount = wp.colNum("unit_price")*wp.colNum("tot_term");
    
    Double remdAmt = wp.colNum("tot_amt") - amount;
    
    if (tableName.equals("bil_prod_t")) {
      wp.colSet("apr_flag", "N");
      wp.colSet("tt_apr_flag", "U.更新待覆核");
    } else if (tableName.equals("bil_prod")) {
      wp.colSet("apr_flag", "Y");
      wp.colSet("tt_apr_flag", "Y.未異動");
//      wp.colSet("rowid", ""); //Justin
      wp.colSet("kk_mcht_no", wp.colStr("mcht_no"));
      wp.colSet("kk_product_no", wp.colStr("product_no"));
    }
    wp.colSet("remd_amt", remdAmt); 
    wp.colSet("mchtNo", mchtNo);
    wp.colSet("productNo", productNo);
    
    queryAcct(mchtNo, productNo, wp.colStr("apr_flag"));
    queryGroup(mchtNo, productNo, wp.colStr("apr_flag"));
    queryCard(mchtNo, productNo, wp.colStr("apr_flag"));
    
    // Justin
//    // 更新尾數
//    updateRemdAmt();
  }

  /**
   * get merchant type
   * @param merchantNo
   */
	private void getMchtType(String merchantNo) {
		String SqlStr = "select MCHT_TYPE from bil_merchant where mcht_no = ?";
		Object[] paramType = new Object[] { merchantNo };
		sqlSelect(SqlStr, paramType);
	}

  @Override
  public void saveFunc() throws Exception {

    Bilm0090Func func = new Bilm0090Func(wp);
    
    String[] aaValueA = wp.itemBuff("dtl_value_a");
	String[] aaValueG = wp.itemBuff("dtl_value_g");
	String[] aaValueC = wp.itemBuff("dtl_value_c");
	String[] aaOpta = wp.itemBuff("opta");
	String[] aaOtpg = wp.itemBuff("optg");
	String[] aaOtpc = wp.itemBuff("optc");
	
	// keep the value of the inputs
	keepList(aaValueA, 1);
	keepList(aaValueG, 2);
	keepList(aaValueC, 3);

    if ( rc == 1 && ( strAction.equals("A") || strAction.equals("U"))) {

		if (strAction.equals("A")) {
			if (!func.checkBilProd()) {
				alertErr2(func.getMsg());
				return;
			}
			if (!func.checkBilProdT()) {
				alertErr2(func.getMsg());
				return;
			}
			
			// delete bil_prod_dtl_t
			func.dbDeleteDtlTForInsert();
		} else {
			// delete bil_prod_dtl_t
			func.dbDelete2();
		}
		
		int insertCnt = 0;
		insertCnt += insertBilProdDtlT(func, aaValueA, aaOpta, "ACCT-TYPE");
		insertCnt += insertBilProdDtlT(func, aaValueG, aaOtpg, "GROUP-CODE");
		insertCnt += insertBilProdDtlT(func, aaValueC, aaOtpc, "CARD-TYPE");
		
		func.varsSet("insertCnt", Integer.toString(insertCnt));
    }

    func.setConn(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());

    this.sqlCommit(rc);

    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      saveAfter(true);
	  if (strAction.equals("D")) cleanList();
    }

  }

  /**
   * remove the value of the inputs
   * @throws Exception
   */
	private void cleanList() throws Exception {
		wp.selectCnt = 0;
		wp.sumLine = 0;
		wp.setListCount(1); //ACCT
		wp.setListCount(2); // GROUP
		wp.setListCount(3); // CARD
	}

  /**
   * keep the value of the input
   * @param arr
   * @param i
   * @throws Exception
   */
	private void keepList(String[] arr, int i) throws Exception {
		wp.sumLine = 0;
		if (empty(arr[0]))
			wp.selectCnt = 0;
		else
			wp.selectCnt = arr.length;
		wp.setListCount(i);
	}

/**
   * 
   * @param func
   * @param aaValueA
   * @param aaOpta
   * @param kind
   * @return the number of inserted data
   */
private int insertBilProdDtlT(Bilm0090Func func, String[] aaValueA, String[] aaOpta, String kind) {
	int insertCnt = 0;
	for (int ll = 0; ll < aaValueA.length; ll++) {
        if (checkBoxOptOn(ll, aaOpta)) {
          continue;
        }
        if (empty(aaValueA[ll])) {
          continue;
        }
        if (ll != Arrays.asList(aaValueA).indexOf(aaValueA[ll])) {
          // wp.col_set(ll, "err_mesg_a", "帳戶種類資料值重複 !!");
          continue;
        }
        func.varsSet("aa_kind", kind);
        func.varsSet("aa_value", aaValueA[ll]);

        if (func.dbInsert2() > 0) {
        	insertCnt ++;
			sqlCommit(1);
		}else {
			sqlCommit(0);
		}
      }
	return insertCnt;
}

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
    	
      this.btnModeAud();
      
      if (empty(wp.colStr("rowid"))) {
		  wp.colSet("pro_no_display", " style='display:none' ");
		  btnReadOn(false);
	  }
      
      // 若已覆核，則不能修改
      if (wp.colStr("apr_flag").equals("Y")) {
    	  btnUpdateOn(false);
    	  btnDeleteOn(false);
	  }
      
      switch(strAction) {
      case "new": 
    	  //wp.colSet("db_merchant_type", "1");
          wp.colSet("clt_fees_max_amt", "99999");
          wp.colSet("fees_max_amt", "99999");
          wp.colSet("auto_delv_flag", "N");
          wp.colSet("auto_print_flag", "N");
          wp.colSet("refund_flag", "N");
          wp.colSet("pro_no_display", " style='display:none' ");
    	  break; 
    	  
      }
      
    }
  }

  @Override
  public void dddwSelect() {
    try {

      // wp.initOption="--";
      // wp.optionKey = wp.col_ss("ex_mcht_no");
      // this.dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no",
      // "mcht_chi_name", "where 1=1 and mcht_status = '1' order by
      // mcht_no");
      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_product_no");
      // this.dddw_list("dddw_product_no", "bil_prod", "product_no",
      // "product_no||product_name||mcht_no", " where 1=1 group by
      // product_no,product_name,mcht_no order by product_no");
      //
      this.dddwList("dddw_acct", "ptr_acct_type", "acct_type", "chin_name",
          " where  1=1  order by acct_type");
      this.dddwList("dddw_group", "ptr_group_code", "group_code", "group_name",
          " where  1=1  order by group_code");
      this.dddwList("dddw_card", "ptr_card_type", "card_type", "name",
          " where  1=1  order by card_type");
      
      
      if ( ( (wp.respHtml.indexOf("_detl") == -1 && !empty(wp.colStr("ex_merchant"))) ||  (! empty(wp.colStr("rowid"))&&!empty(wp.colStr("kk_mcht_no"))) ) 
    		 ) {  
    	  
    	  String merchantNo = empty(wp.colStr("ex_merchant"))?wp.colStr("kk_mcht_no"):wp.colStr("ex_merchant");
    	  String productNo = empty(wp.colStr("ex_prod"))?wp.colStr("kk_product_no"):wp.colStr("ex_prod");
    	  
          wp.initOption="--";
          wp.optionKey = productNo;
          setString("mcht_no", merchantNo);
          /*
          if ("Y".equals(wp.colStr("ex_apr_flag"))) {
        	  this.dddwList("dddw_product_no", "bil_prod", "product_no",
        			  "product_no||'_'||product_name", " where mcht_no = :mcht_no order by product_no ");  
          } else {
        	  this.dddwList("dddw_product_no", "bil_prod_t", "product_no",
        			  "product_no||'_'||product_name", " where mcht_no = :mcht_no order by product_no ");  
          }
          */
          String sql1  = "select product_no AS db_code ,product_no||'_'||product_name AS db_desc from bil_prod where mcht_no = :mcht_no   ";
                 sql1 += " union select product_no AS db_code ,product_no||'_'||product_name AS db_desc from bil_prod_t where mcht_no = :mcht_no order by db_code ";
          this.dddwList("dddw_product_no", sql1 );
	 }
      
    } catch (Exception ex) {
    }
  }
  
  void updateRemdAmt() {
    Bilm0090Func func = new Bilm0090Func(wp);
    func.setConn(wp);
    rc = func.updateRemdAmt();
    if (rc != 1) {
      alertErr2(func.getMsg());
    } 
    
  }

  void queryAcct(String mchtNo, String productNo, String aprFlag) throws Exception {
		String table = "";
		if (aprFlag.equals("Y"))
			table = "bil_prod_dtl";
		else
			table = "bil_prod_dtl_t";

		wp.selectSQL = " dtl_value as dtl_value_a";
		wp.daoTable = table;
		wp.whereStr = "where dtl_kind=:dtl_kind  and mcht_no=:mcht_no and product_no=:product_no ";
		setString("dtl_kind", "ACCT-TYPE");
		setString("mcht_no", mchtNo);
		setString("product_no", productNo);
		wp.whereOrder = " order by dtl_value ";

		pageQuery();

		wp.notFound = "N";
		wp.setListCount(1);

		String parm = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			parm = wp.colStr(ii, "dtl_value_a");
			String sqlSelect = "select acct_type||'_'||chin_name as tt_dtl_value_a from ptr_acct_type where acct_type = :tt_dtl_value_a ";
			setString("tt_dtl_value_a", parm);
			sqlSelect(sqlSelect);
			if (sqlRowNum > 0) {
				parm = sqlStr("tt_dtl_value_a");
			}
			wp.colSet(ii, "tt_dtl_value_a", parm);
		}

  }

  void queryGroup(String mchtNo, String productNo, String aprFlag) throws Exception {
	String table = "";
	if (aprFlag.equals("Y"))
		table = "bil_prod_dtl";
	else
		table = "bil_prod_dtl_t";

    wp.selectSQL = " dtl_value as dtl_value_g";
    wp.daoTable = table;
    wp.whereStr = "where dtl_kind=:dtl_kind  and mcht_no=:mcht_no and product_no=:product_no ";
    setString("dtl_kind", "GROUP-CODE");
    setString("mcht_no", mchtNo);
    setString("product_no", productNo);
    wp.whereOrder = " order by dtl_value ";

    pageQuery();
    wp.notFound = "N";
    wp.setListCount(2);

    String dtlValueG = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      dtlValueG = wp.colStr(ii, "dtl_value_g");
      String sqlSelect =
          "select group_code||'_'||group_name as tt_dtl_value_g from ptr_group_code where group_code = :tt_dtl_value_g ";
      setString("tt_dtl_value_g", dtlValueG);
      sqlSelect(sqlSelect);
      if (sqlRowNum > 0) {
        dtlValueG = sqlStr("tt_dtl_value_g");
      }
      wp.colSet(ii, "tt_dtl_value_g", dtlValueG);
    }

  }

  void queryCard(String mchtNo, String productNo, String aprFlag) throws Exception {
	String table = "";
	if (aprFlag.equals("Y"))
		table = "bil_prod_dtl";
	else
		table = "bil_prod_dtl_t";

    wp.selectSQL = " dtl_value as dtl_value_c";
    wp.daoTable = table;
    wp.whereStr = "where dtl_kind=:dtl_kind  and mcht_no=:mcht_no and product_no=:product_no ";
    setString("dtl_kind", "CARD-TYPE");
    setString("mcht_no", mchtNo);
    setString("product_no", productNo);
    wp.whereOrder = " order by dtl_value ";

    pageQuery();
    wp.notFound = "N";
    wp.setListCount(3);

    String dtlValueC = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      dtlValueC = wp.colStr(ii, "dtl_value_c");
      String sqlSelect =
          "select card_type||'_'||name as tt_dtl_value_c from ptr_card_type where card_type = :tt_dtl_value_c ";
      setString("tt_dtl_value_c", dtlValueC);
      sqlSelect(sqlSelect);
      if (sqlRowNum > 0) {
        dtlValueC = sqlStr("tt_dtl_value_c");
      }
      wp.colSet(ii, "tt_dtl_value_c", dtlValueC);
    }
  }

  public void processAjaxOption() throws Exception {
    // return;
    wp.varRows = 1000;
    setSelectLimit(0);
    if (wp.itemEq("action_code", "AJAX1")) {
		itemChangMcht();
	} else {
		String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
				+ " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
				+ " order by mcht_no ";
		if (wp.respHtml.indexOf("_detl") > 0) {
			setString("mcht_no", wp.getValue("kk_mcht_no", 0) + "%");
		} else {
			setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
		}
		sqlSelect(lsSql);

		for (int i = 0; i < sqlRowNum; i++) {
			wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
			wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
		}
		return;
	}

  }

  void itemChangMcht() throws Exception {
		String lsMerchantNo = wp.itemStr("data_mcht_no");
		if(empty(lsMerchantNo))
			return;
		
		wp.initOption="--";
        wp.optionKey = productNo;
        setString("mcht_no", lsMerchantNo);

        String sql1  = "select product_no AS db_code ,product_no||'_'||product_name AS db_desc from bil_prod where mcht_no = :mcht_no   ";
               sql1 += " union select product_no AS db_code ,product_no||'_'||product_name AS db_desc from bil_prod_t where mcht_no = :mcht_no order by db_code ";
        this.dddwList("dddw_product_no", sql1 );
        String str = wp.colStr("dddw_product_no");
        log(str);
        wp.addJSON("dddw_tot_term_option", str);
        wp.addJSON("json_action_code", "AJAX1");
        
	}
  
  public int itemchanged(TarokoCommon wr) throws Exception {

    super.wp = wr;
    String ajaxName = "";
    String lsSql = "", lsMchtType = "", lsTransFlag = "";
    // int rc = 0;
    ajaxName = wp.itemStr("ajaxName");

    switch (ajaxName) {
      case "mcht_no":
        lsSql =
            " select mcht_chi_name, NVL(mcht_type,'1') as mcht_type  ,NVL(trans_flag,'N') as trans_flag from bil_merchant where mcht_no = :mcht_no ";
        setString("mcht_no", wp.itemStr("val_mcht_no"));
        sqlSelect(lsSql);
        lsMchtType = sqlStr("mcht_type");
        lsTransFlag = sqlStr("trans_flag");

        wp.addJSON("j_mcht_type", lsMchtType);
        wp.addJSON("j_trans_flag", lsTransFlag);

    }
    return 1;
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  void itemchanged1() throws Exception {
    String lsMchtNo = "", product_no= "", product_name = "";
    
    wp.colSet("apr_flag", "Y");

    lsMchtNo = wp.itemStr("ex_merchant");
    if (empty(lsMchtNo)) {
      lsMchtNo = wp.itemStr("kk_mcht_no");
    }
    
    if ( ! empty(lsMchtNo)) {
    	
    	getMchtType(lsMchtNo);
        if (sqlRowNum > 0) {
            wp.colSet("db_merchant_type", sqlStr("MCHT_TYPE"));
        }
        
    }

    wp.colSet("product_no", product_no);
    wp.colSet("product_name", product_name);
    wp.colSet("mcht_no", lsMchtNo);
    
    //Justin reset the value of the input
    wp.colSet("ex_prod", "");
    wp.colSet("kk_product_no", "");

    
    return;
  }

  void itemchanged2() throws Exception {
	  dataRead();
	  // Justin
//    String lsSql = "", lsMchtNo = "", lsProductNo = "";
//
//    lsMchtNo = wp.itemStr("ex_merchant");
//    if (empty(lsMchtNo)) {
//      lsMchtNo = wp.itemStr("kk_mcht_no");
//    }
//    if (empty(lsMchtNo)) {
//      return;
//    }
//    
//    lsProductNo = wp.itemStr("kk_product_no");
//    if (empty(lsProductNo)) {
//      return;
//    }
//    lsSql = "select product_no, product_name from bil_prod " + "where 1=1 ";
//    lsSql += sqlCol(lsMchtNo, "mcht_no");
//    lsSql += sqlCol(lsProductNo, "product_no");
//    sqlSelect(lsSql);
//    if (sqlRowNum <= 0) {
//      return;
//    }
//    wp.colSet("product_no", sqlStr("product_no"));
//    wp.colSet("product_name", sqlStr("product_name"));
//    
//    queryAcct(lsMchtNo, lsProductNo, "Y");
//    queryGroup(lsMchtNo, lsProductNo, "Y");
//    queryCard(lsMchtNo, lsProductNo, "Y");

    return;
  }
}
