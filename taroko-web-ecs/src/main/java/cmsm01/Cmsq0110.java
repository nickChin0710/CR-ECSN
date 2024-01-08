package cmsm01;
/**
 * 2019-1223  Alex  fix sum
 * 2019-1213  Alex  fix query
 * 2019-0911   JH    --rsk-ctrlseqno-log
 * 19-0613:   JH    p_xxx >> acno_p_xxx
*  109-04-27  shiyuqi       updated for project coding standard     *  
*  109-12-25  Justin          parameterize sql
** 109-12-31  V1.00.03   shiyuqi       修改无意义命名     
*  112-11-22  V1.00.04   JeffKung      金額欄位若為負值顯示負值                                                                                 * 
* */
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import rskm01.BilBill;
import taroko.com.TarokoExcel;

public class Cmsq0110 extends BaseAction implements InfaceExcel {
  String lsKey = "", lsWhere = "", referenceNo = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
    	
      sqlParm.clear();
      
      if (eqIgno(wp.respHtml, "cmsq0110")) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
        wp.optionKey = wp.colStr("ex_curr_code");
        dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'DC_CURRENCY'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEq("ex_one_card", "Y")) {
      if (wp.itemEmpty("ex_card_no")) {
        alertErr2("單卡交易查詢 卡號不可空白");
        return;
      }
    }

    boolean lbTpan = false;
    if (wp.itemEmpty("ex_acct_key") == false) {
      lsKey = wp.itemStr("ex_acct_key");
    } else if (wp.itemEmpty("ex_card_no") == false) {
      lsKey = wp.itemStr("ex_card_no");
    } else if (wp.itemEmpty("ex_vcard_no") == false) {
      lbTpan = true;
      lsKey = wp.itemStr2("ex_vcard_no");
    }

    if (empty(lsKey)) {
      errmsg("[帳戶帳號, 卡號, TPAN] 不可同時空白");
      return;
    }

    if (wp.itemEq("ex_bill", "1") && wp.itemEmpty("ex_acct_month")) {
      alertErr2("關帳年月：不可空白");
      return;
    }

    if (lbTpan == true) {
      String sql0 = "select card_no from hce_card where 1=1 and v_card_no = ? ";
      sqlSelect(sql0, new Object[] {lsKey});
      if (sqlRowNum <= 0) {
        alertErr2("TPAN 輸入錯誤");
        return;
      }
      lsKey = sqlStr("card_no");
    }

    /*
     * if (auth_query(ls_key)==false) { return; }
     */
    zzVipColor(lsKey);

    String sql1 = "", sql2 = "";


    if (wp.itemEmpty("ex_acct_key") == false) {
      if (lsKey.length() != 8 && lsKey.length() < 10) {
        errmsg("帳戶帳號: 輸入錯誤");
        return;
      }
      if (lsKey.length() == 10) {
    	  sql1 = "select p_seqno , uf_acno_name(acno_p_seqno) as ex_chi_name from act_acno"
    			  + " where acct_key like ?" + " and acct_type = ?" + commSqlStr.rownum(1);
    	  setString2(1, lsKey + "%");
    	  setString(wp.itemNvl("ex_acct_type", "01"));
      } else {
    	  sql1 = "select corp_p_seqno , chi_name as ex_chi_name from crd_corp "
    			  + " where corp_no like ?"  + commSqlStr.rownum(1);
    	  setString2(1, lsKey + "%");
      }
    } else if (wp.itemEmpty("ex_card_no") == false || wp.itemEmpty("ex_vcard_no") == false) {
      sql1 = "select p_seqno , uf_acno_name(acno_p_seqno) as ex_chi_name from crd_card"
          + " where card_no =?";
      setString2(1, lsKey);
    }
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      errmsg("[帳戶帳號, 卡號] 輸入錯誤, 查無帳戶流水號[p_seqno], kk[%s]", lsKey);
      return;
    }

    String lsCorpPSeqno = sqlStr("corp_p_seqno");
    String lsPSeqno = sqlStr("p_seqno");
    String lsChiName = sqlStr("ex_chi_name");
    
    //輸入統編時
    if (lsKey.length() == 8) {
    	sql1 = "select p_seqno from crd_card "
  			  + " where corp_p_seqno = ?" + commSqlStr.rownum(1);
  	    setString2(1, sqlStr("corp_p_seqno"));
  	    sqlSelect(sql1);
  	    
  	    lsPSeqno = sqlStr("p_seqno");
    }


    wp.colSet("ex_p_seqno", lsPSeqno);
    wp.colSet("ex_chi_name", lsChiName);

    sql2 = "select b.stmt_cycle, b.next_acct_month, A.acct_status , B.this_acct_month as is_this_mm"
        + " from act_acno a, ptr_workday B" + " where A.stmt_cycle = B.stmt_cycle"
        + " and A.p_seqno = ? ";
    sqlSelect(sql2, new Object[] {lsPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("查無卡人之關帳周期[stmt_cycle]");
      return;
    }
    
    if (wp.itemEq("ex_one_card", "Y")) {
        lsWhere = " where A.card_no = ? ";
        setString(wp.itemStr2("ex_card_no"));
      } else {
    	//輸入統編時
    	    if (lsKey.length() == 8) {
    	    	lsWhere = " where A.card_no in (select card_no from crd_card where corp_p_seqno = ? ) ";
    	    	setString(lsCorpPSeqno);
    	    } else {
    	    	lsWhere = " where A.p_seqno = ? ";
    	        setString(lsPSeqno);
    	    }
      }
    
    String lsAcctMonth = "", lsNextAcctMonth = "", isThisMm = "";
    lsAcctMonth = wp.itemStr("ex_acct_month");
    lsNextAcctMonth = sqlStr("next_acct_month");
    isThisMm = sqlStr("is_this_mm");
    if (wp.itemEq("ex_bill", "1")) {

      if (chkStrend(lsAcctMonth, isThisMm) == false) {
        alertErr2("輸入關帳年月尚未發生!");
        return;
      }

      lsWhere += sqlCol(lsAcctMonth, "A.acct_month");
    } else {
      lsWhere += sqlCol(lsNextAcctMonth, "A.acct_month");
    }

    lsWhere += " and A.rsk_type not in ('1','2','3') ";

    if (wp.itemEmpty("ex_curr_code") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_curr_code"), wp.sqlID + "uf_nvl(A.curr_code,'901')");
    }

    lsWhere += sqlCol(wp.itemStr("ex_vcard_no"), "A.v_card_no");
    lsWhere += sqlCol(wp.itemStr("ex_dest_amt"), "A.dest_amt", ">=");

    wkListSum(lsWhere);

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  void wkListSum(String lsWhere) {

    wp.logSql = true;
    String sql1 = " select " + " sum(A.dc_dest_amt) as tl_dc_dest_amt_0 , "
        + " sum(decode(A.curr_code,'901',dc_dest_amt,0)) as tl_tw_desc_amt_0 , "
        + " sum(decode(A.curr_code,'840',dc_dest_amt,0)) as tl_us_desc_amt_0 , "
        + " sum(decode(A.curr_code,'392',dc_dest_amt,0)) as tl_jp_desc_amt_0   "
        + " from bil_bill A " + lsWhere + " and A.sign_flag = '+' ";
    setSqlParmNoClear(true);
    sqlSelect(sql1);

    String sql2 = " select " + " sum(A.dc_dest_amt) as tl_dc_dest_amt_1 , "
        + " sum(decode(A.curr_code,'901',dc_dest_amt,0)) as tl_tw_desc_amt_1 , "
        + " sum(decode(A.curr_code,'840',dc_dest_amt,0)) as tl_us_desc_amt_1 , "
        + " sum(decode(A.curr_code,'392',dc_dest_amt,0)) as tl_jp_desc_amt_1   "
        + " from bil_bill A  " + lsWhere + " and A.sign_flag = '-' ";

    setSqlParmNoClear(true);
    sqlSelect(sql2);


    wp.colSet("tl_dc_dest_amt", sqlNum("tl_dc_dest_amt_0") - sqlNum("tl_dc_dest_amt_1"));
    wp.colSet("tl_tw_desc_amt", sqlNum("tl_tw_desc_amt_0") - sqlNum("tl_tw_desc_amt_1"));
    wp.colSet("tl_us_desc_amt", sqlNum("tl_us_desc_amt_0") - sqlNum("tl_us_desc_amt_1"));
    wp.colSet("tl_jp_desc_amt", sqlNum("tl_jp_desc_amt_0") - sqlNum("tl_jp_desc_amt_1"));
  }

  @Override
  public void queryRead() throws Exception {

    wp.selectSQL = "" + " A.post_date , " + " A.purchase_date , " + " A.interest_date , "
        + " A.card_no , " + " uf_hi_cardno(A.card_no) as hh_card_no ," 
    	+ " decode(A.sign_flag,'-',A.source_amt*-1,A.source_amt) as source_amt , "
        + " decode(A.sign_flag,'-',A.dest_amt*-1,A.dest_amt) as dest_amt," 
    	+ " A.curr_code," 
        + " decode(A.sign_flag,'-',A.dc_dest_amt*-1,A.dc_dest_amt) as dc_dest_amt," 
    	+ commSqlStr.mchtName("", "")
        + " as mcht_name," + " A.mcht_city," + " A.auth_code," + " A.txn_code," + " A.bin_type,"
        + " A.payment_type," 
        + " decode(A.sign_flag,'-',A.cash_pay_amt*-1,A.cash_pay_amt) as cash_pay_amt," 
        + " A.reference_no," + " A.merge_flag,"
        + " A.v_card_no," + " A.rsk_ctrl_seqno," + " A.ec_ind, A.reference_no , "
        + " A.pos_entry_mode , " + " A.mcht_category , " + " B.qr_flag "
    // + " '' as rept_status , "
    // + " '' as prbl_mark , "
    // + " '' as prbl_status , "
    // + " '' as chgb_status , "
    // + " '' as chgb_status2 , "
    // + " '' as precom_mark , "
    // + " '' as prearb_mark "
    ;
    wp.daoTable = "bil_bill A left join bil_nccc300_dtl B on A.reference_no =B.reference_no "; 
    wp.whereOrder = " order by A.card_no Desc , A.purchase_date Desc";


    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    // query_After(wp.selectCnt);
    wp.setPageValue();
  }
  // void query_After(int ll_nrow) throws Exception {
  // /* + " '' as rept_status , "
  // + " '' as prbl_mark , "
  // + " '' as prbl_status , "
  // + " '' as chgb_status , "
  // + " '' as chgb_status2 , "
  // + " '' as precom_mark , "
  // + " '' as prearb_mark " */
  // rskm01.RskCtrlseqno ooctrl=new rskm01.RskCtrlseqno();
  // ooctrl.setConn(wp);
  // for (int ll=0; ll<ll_nrow; ll++) {
  // if (wp.col_empty(ll,"rsk_ctrl_seqno")) continue;
  //
  // String ls_refno =wp.col_ss(ll,"reference_no");
  // ooctrl.select_xxx_bill(ls_refno,"N");
  // wp.col_set(ll,"prbl_mark",ooctrl.prbl_src_code);
  // wp.col_set(ll,"prbl_status",ooctrl.prbl_status);
  // wp.col_set(ll,"chgb_status",ooctrl.chgb_stage1);
  // wp.col_set(ll,"chgb_status2",ooctrl.chgb_stage2);
  // wp.col_set(ll,"rept_status",ooctrl.rept_status);
  // wp.col_set(ll,"precom_mark",ooctrl.compl_status);
  // wp.col_set(ll,"prearb_mark",ooctrl.arbit_status);
  // }
  // }

  @Override
  public void querySelect() throws Exception {
    referenceNo = wp.itemStr2("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    BilBill bill = new BilBill();
    bill.setConn(wp);
    bill.varsSet("reference_no", referenceNo);

    if (bill.dataSelect() == -1) {
      alertErr2(bill.getMsg());
      return;
    }

    return;
  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "cmsq0110";
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "cmsq0110.xlsx";
      wp.pageRows = 9999;
      wp.colSet("user_id", wp.loginUser);
      queryFunc();
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
