package colq01;
/**
 * 20230106   Sunny    copy from actq0040
 * 20230206   Ryan     modify
 */

import busi.func.ColFunc;
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Colq0040 extends BaseAction {
  String mProgName = "colq0040";

  String idCorpName = "";
  String idPSeqno = "";
  String corpPSeqno = "";
  String stmtCycle = "";
  
  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
        strAction = "new";
        clearFunc(); break;
      case "Q": //查詢功能
        queryFunc();
        tabClick();
        break;
      case "R": // -資料讀取-
        dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
        saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
        queryRead(); break;
      case "S": //動態查詢--
        querySelect(); break;
      case "L": //清畫面--
        strAction = "";
        clearFunc(); break;
      case "C": // -資料處理-
        procFunc(); break;
      default:
        alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsq0040")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
    }
    catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    wp.colSet("ex_scope_cnt", "0");
    wp.colSet("ex_acno_name", "");
    wp.colSet("ex_stmt", "");

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("入帳起迄日期 : 起迄錯誤 !");
      return;
    }
    //start-查詢權限檢查，參考【f_auth_query】
    ColFunc colfunc = new ColFunc();
    colfunc.setConn(wp);
    String exIdCorpNo = wp.itemStr("ex_id_corp_no");

    if(empty(exIdCorpNo)) {
    	return;
    }
    
    if(exIdCorpNo.length()!=8 && exIdCorpNo.length()!=10 && exIdCorpNo.length()!=11) {
      alertErr("身份證字號/統編輸入錯誤 !");
      return;
    }
    
    if (colfunc.fAuthQuery(mProgName, exIdCorpNo) != 1) {
      alertErr(colfunc.getMsg());
      return;
    }
    //end-查詢權限檢查，參考【f_auth_query】
    
    String acctKey = commString.acctKey(exIdCorpNo);
    zzVipColor((exIdCorpNo.length()==10?"01":"03")+acctKey);
    
    if (getIdCorpName() == false) {
        return;
    }

    StringBuffer strBufWhere = new StringBuffer();
    
    strBufWhere.append(" where 1=1 ");
    if(exIdCorpNo.length()==10) {
    	strBufWhere.append(" and A.id_p_seqno = '")
    	.append(idPSeqno).append("' ")
    	.append(" and A.acct_type = '01'");
    }else {
    	strBufWhere.append(" and A.corp_p_seqno = '")
    	.append(corpPSeqno).append("' ")
    	.append(" and A.acct_type = '03'");
    }
         
    if (!empty(wp.itemStr("ex_date1"))) {
    	strBufWhere.append(" and A.acct_date >= '")
    	.append(wp.itemStr("ex_date1"))
    	.append("' ");
    }
    if (!empty(wp.itemStr("ex_date2"))) {
      strBufWhere.append(" and A.acct_date <= '")
      .append(wp.itemStr("ex_date2"))
      .append("' ");
    }

    String lsSql = "";
    int liSelTotcnt = 0;

    lsSql  = " select ";
    lsSql += " count(*) colh_sel_totcnt ";
    lsSql += " from COL_BAD_JRNL_EXT a ";
    lsSql += strBufWhere.toString();
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
    	liSelTotcnt = sqlInt("colh_sel_totcnt");
    } else {
    	liSelTotcnt = 0;
    }
    wp.colSet("ex_scope_cnt", liSelTotcnt);

    if (liSelTotcnt == 0) {
      alertErr("此條件查無資料 ");
      return;
    }
    
    wp.whereStr = strBufWhere.toString();
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  boolean getIdCorpName() throws Exception {
    String exIdCorpNo = wp.itemStr("ex_id_corp_no");
    boolean sqlResult = false;
    if (!empty(exIdCorpNo)) {
    	if(exIdCorpNo.length()==10) {
    		sqlResult = getIdPSeqno(exIdCorpNo);
    		if(!sqlResult) return false;
    		getStmtCycle(exIdCorpNo,"01");
    	}
    	if(exIdCorpNo.length()==8 || exIdCorpNo.length()==11) {
    		sqlResult = getCorpPseqno(exIdCorpNo);
    		if(!sqlResult) return false;
    		getStmtCycle(exIdCorpNo,"03");
    	}
      wp.colSet("ex_id_crop_name", idCorpName);
      wp.colSet("ex_stmt", stmtCycle);
    }
    return true;
  }
  
  boolean getIdPSeqno(String idNo){
	  String sqlStr = " select chi_name,id_p_seqno from crd_idno where id_no = ?";
	  setString(1,idNo);
	  sqlSelect(sqlStr);
	  if(sqlRowNum>0) {
		  idCorpName = sqlStr("chi_name");
		  idPSeqno = sqlStr("id_p_seqno");
		  return true;
	  }
	  alertErr("select crd_idno not found ,查無身份證字號");
	  return false;
  }
  
  boolean getCorpPseqno(String idNo){
	  String sqlStr = " select chi_name,corp_p_seqno from crd_corp where corp_no = ?";
	  setString(1,idNo);
	  sqlSelect(sqlStr);
	  if(sqlRowNum>0) {
		  idCorpName = sqlStr("chi_name");
		  corpPSeqno = sqlStr("corp_p_seqno");
		  return true;
	  }
	  alertErr("select crd_corp not found ,查無統編號碼");
	  return false;
  }
  
  void getStmtCycle(String acctKey ,String acctType) {
	  acctKey = commString.acctKey(acctKey);
	  String sqlStr = " select stmt_cycle from act_acno where acct_key = ? and acct_type = ? ";
	  setString(1,acctKey);
	  setString(2,acctType);
	  sqlSelect(sqlStr);  
	  if(sqlRowNum>0) {
		  stmtCycle = sqlStr("stmt_cycle");
		  return;
	  }
  }

  @Override
  public void queryRead() throws Exception {
    //select_noLimit();
    wp.pageControl();
    wp.selectSQL = " A.* , "
            + " decode(uf_tt_acct_code(A.acct_code),'',A.acct_code,uf_tt_acct_code(A.acct_code)) as tt_acct_code , "
            + " decode(A.tran_class,'B','帳單','P','繳款','D','銷帳','A','調整') as tt_tran_class , "
            + " decode(A.dr_cr,'D','-','C','+') as tt_dr_cr  "
            //+ " ,decode(A.cash_type,'1','溢付提領','2','開立即支票','3','匯入本行帳戶','4','匯入它行帳戶','5','CRS溢繳款超過4萬美金') as tt_cash_type "
    ;
    wp.daoTable = " COL_BAD_JRNL_EXT A ";
    wp.whereOrder = " order by A.crt_date Asc, A.crt_time Asc, A.enq_seqno Asc ";
    pageQuery();
    wp.setListCount(9);

    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
    }
    wp.setPageValue();
    queryAfter(wp.selectCnt);
  }

  void queryAfter(int llNrow) throws Exception {
    int rr = 0;
    String ss = "", ss2 = "";
    //A1
    for (int ii = 0; ii < llNrow; ii++) {
      wp.colSet(rr, "A1_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A1_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A1_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A1_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      wp.colSet(rr, "A1_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A1_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A1_item_bal", wp.colStr(ii, "item_bal"));

      wp.colSet(rr, "transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "jrnl_bal", wp.colStr(ii, "jrnl_bal"));
      wp.colSet(rr, "item_bal", wp.colStr(ii, "item_bal"));
     // wp.colSet(rr, "item_d_bal", wp.colStr(ii, "curr_code").equals("901") ? wp.colStr(ii, "item_d_bal") : wp.colStr(ii, "dc_item_d_bal"));
      if (empty(wp.colStr(ii, "trans_acct_type")))
        wp.colSet(rr, "wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
      else
        wp.colSet(rr, "wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));


     // wp.colSet(rr, "A1_dc_jrnl_bal", wp.colStr(ii, "dc_jrnl_bal"));
      wp.colSet(rr, "A1_jrnl_bal", wp.colStr(ii, "jrnl_bal"));
      if (wp.colNum(ii, "jrnl_bal") < 0) {
        wp.colSet(rr, "A1_bal_style", "col_key");
        wp.colSet(rr, "bal_style", "col_key");
      }
      else {
        wp.colSet(rr, "A1_bal_style", "");
      }
      wp.colSet(rr, "A1_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A1_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A1_crt_time", wp.colStr(ii, "crt_time"));
    //  wp.colSet(rr, "A1_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A1_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A1_memo", wp.colStr(ii, "memo")); //備註欄約30個中文字
      wp.colSet(rr, "A1_int_rate", wp.colStr(ii, "int_rate")); //利息利率
     // wp.colSet(rr, "A1_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[0] = rr;
    rr = 0;
    //A2----------------------------------------------
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "B"))
        continue;

      wp.colSet(rr, "A2_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A2_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A2_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A2_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      wp.colSet(rr, "A2_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A2_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A2_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A2_item_bal", wp.colStr(ii, "item_bal"));
      wp.colSet(rr, "A2_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A2_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A2_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A2_crt_time", wp.colStr(ii, "crt_time"));
     // wp.colSet(rr, "A2_curr_code", wp.colStr(ii, "curr_code"));
     // wp.colSet(rr, "A2_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[1] = rr;
    rr = 0;

    //A3
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type").substring(0, 2);
      if (!eqIgno(ss, "DE") && !eqIgno(ss, "DR"))
        continue;

      wp.colSet(rr, "A3_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A3_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A3_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      //wp.colSet(rr, "A3_item_d_bal", wp.colStr(ii, "item_d_bal"));
      wp.colSet(rr, "A3_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A3_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A3_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A3_item_bal", wp.colStr(ii, "item_bal"));
     // wp.colSet(rr, "A3_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
      wp.colSet(rr, "A3_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A3_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A3_adj_reason_code", wp.colStr(ii, "adj_reason_code"));
      wp.colSet(rr, "A3_adj_comment", wp.colStr(ii, "adj_comment"));
      wp.colSet(rr, "A3_c_debt_key", wp.colStr(ii, "c_debt_key"));
      wp.colSet(rr, "A3_debit_item", wp.colStr(ii, "debit_item"));
      wp.colSet(rr, "A3_value_type", wp.colStr(ii, "value_type"));
      wp.colSet(rr, "A3_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A3_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A3_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A3_curr_code", wp.colStr(ii, "curr_code"));
     // wp.colSet(rr, "A3_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
      ss = "";
    }
    wp.listCount[2] = rr;
    rr = 0;

    //A4
    for (int ii = 0; ii < llNrow; ii++) {
      ss = wp.colStr(ii, "tran_class");
      ss2 = wp.colStr(ii, "tran_type");
      if (pos("|P|D", ss) > 0 || (eqIgno(ss, "A") && eqIgno(ss2, "CN01"))) {
        wp.colSet(rr, "A4_tran_type", ss2);
        wp.colSet(rr, "A4_acct_date", wp.colStr(ii, "acct_date"));
        wp.colSet(rr, "A4_acct_code", wp.colStr(ii, "acct_code"));
        wp.colSet(rr, "A4_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
        wp.colSet(rr, "A4_dr_cr", wp.colStr(ii, "dr_cr"));
        wp.colSet(rr, "A4_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
        wp.colSet(rr, "A4_transaction_amt", wp.colStr(ii, "transaction_amt"));
        wp.colSet(rr, "A4_item_bal", wp.colStr(ii, "item_bal"));
        wp.colSet(rr, "A4_item_date", wp.colStr(ii, "item_date"));
        wp.colSet(rr, "A4_reference_no", wp.colStr(ii, "reference_no"));
        wp.colSet(rr, "A4_crt_date", wp.colStr(ii, "crt_date"));
        wp.colSet(rr, "A4_crt_time", wp.colStr(ii, "crt_time"));
       // wp.colSet(rr, "A4_curr_code", wp.colStr(ii, "curr_code"));
       // wp.colSet(rr, "A4_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
        rr++;
        ss = "";
        ss2 = "";
      }
    }
    wp.listCount[3] = rr;
    rr = 0;

    //A5
    String sql1 = " select lgd_coll_flag from col_lgd_jrnl where 1=1 "
            + " and p_seqno = ? "
            + " and acct_date = ? "
            + " and jrnl_seqno = ? "
            + " and enq_seqno = ? "
            + " and trans_amt = ? "
            + " and apr_date <> '' "
            + this.sqlRownum(1);
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "P")
              && !eqIgno(wp.colStr(ii, "tran_type"), "DR11"))
        continue;
      wp.colSet(rr, "A5_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A5_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A5_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A5_reversal_flag", wp.colStr(ii, "reversal_flag"));
      wp.colSet(rr, "A5_tran_type", wp.colStr(ii, "tran_type"));
      wp.colSet(rr, "A5_tt_tran_type", wp.colStr(ii, "tran_type") + "." + selectTranTypeDesc(wp.colStr(ii, "tran_type")));
      wp.colSet(rr, "A5_payment_rev_amt", wp.colStr(ii, "payment_rev_amt"));
      wp.colSet(rr, "A5_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A5_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A5_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A5_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A5_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A5_transaction_amt", wp.colStr(ii, "transaction_amt"));
  //    wp.colSet(rr, "A5_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
  //    wp.colSet(rr, "A5_wk_amt", "" + (wp.colNum(ii, "dc_transaction_amt") - wp.colNum(ii, "payment_rev_amt")));
      wp.colSet(rr, "A5_wk_amt", "" + (wp.colNum(ii, "transaction_amt") - wp.colNum(ii, "payment_rev_amt")));

      sqlSelect(sql1, new Object[]{
              wp.colStr(ii, "p_seqno"), wp.colStr(ii, "acct_date"), wp.colStr(ii, "jrnl_seqno"),
              wp.colStr(ii, "enq_seqno"), wp.colStr(ii, "transaction_amt")
      });
      if (sqlRowNum == 0) {
        wp.colSet(rr, "db_lgd_coll_flag", "");
      }
      else if (sqlRowNum > 0) {
        wp.colSet(rr, "db_lgd_coll_flag", sqlStr("lgd_coll_flag"));
      }

      rr++;
    }
    wp.listCount[4] = rr;
    rr = 0;

    //A6
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type");
      if (!eqIgno(ss, "OP02") && !eqIgno(ss, "OP03"))
        continue;
      wp.colSet(rr, "A6_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A6_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A6_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A6_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A6_item_bal", wp.colStr(ii, "item_bal"));
     // wp.colSet(rr, "A6_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
     // wp.colSet(rr, "A6_tt_cash_type", wp.colStr(ii, "tt_cash_type"));
     // wp.colSet(rr, "A6_cash_type", wp.colStr(ii, "cash_type"));
      wp.colSet(rr, "A6_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A6_crt_time", wp.colStr(ii, "crt_time"));
    //  wp.colSet(rr, "A6_curr_code", wp.colStr(ii, "curr_code"));
    //  wp.colSet(rr, "A6_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      if (empty(wp.colStr(ii, "trans_acct_type")))
        //wp.colSet(rr, "wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
        wp.colSet(rr, "A6_wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
      else
        //wp.colSet(rr, "wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));
        wp.colSet(rr, "A6_wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));

      rr++;
      ss = "";
    }
    wp.listCount[5] = rr;
    rr = 0;

    //A7
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type");
      if (!eqIgno(ss, "OP01") && !eqIgno(ss, "OP04"))
        continue;

      wp.colSet(rr, "A7_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A7_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A7_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A7_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A7_item_bal", wp.colStr(ii, "item_bal"));
   //   wp.colSet(rr, "A7_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
      wp.colSet(rr, "A7_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A7_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A7_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A7_crt_time", wp.colStr(ii, "crt_time"));
   //   wp.colSet(rr, "A7_curr_code", wp.colStr(ii, "curr_code"));
    //  wp.colSet(rr, "A7_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
      ss = "";
    }
    wp.listCount[6] = rr;
    rr = 0;

    //A8
    for (int ii = 0; ii < llNrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type").substring(0, 2);
      if (!eqIgno(ss, "AI"))
        continue;

      wp.colSet(rr, "A8_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A8_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A8_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A8_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A8_item_bal", wp.colStr(ii, "item_bal"));
      wp.colSet(rr, "A8_item_d_bal", wp.colStr(ii, "item_d_bal"));
      wp.colSet(rr, "A8_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A8_crt_time", wp.colStr(ii, "crt_time"));
    //  wp.colSet(rr, "A8_curr_code", wp.colStr(ii, "curr_code"));
    //  wp.colSet(rr, "A8_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[7] = rr;
    rr = 0;
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
    tabClick();

  }

  public void wfAjaxKey(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =

    selectData1(wp.itemStr("ax_key"), wp.itemStr("ax_type"));
    if (rc != 1) {
      wp.addJSON("acno_name", "　");
      wp.addJSON("stmt_cycle", "　");
      wp.addJSON("payment_no", "　");
      wp.addJSON("p_seqno", "　");
      return;
    }
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("payment_no", sqlStr("payment_no"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));
  }

  public void wfAjaxCard(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectData2(wp.itemStr("ax_card"));
    if (rc != 1) {
      wp.addJSON("acno_name", "");
      wp.addJSON("stmt_cycle", "");
      wp.addJSON("payment_no", "");
      wp.addJSON("p_seqno", "");
      wp.addJSON("acct_type", "");
      wp.addJSON("acct_key", "");
      return;
    }
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("payment_no", sqlStr("payment_no"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acct_key", sqlStr("acct_key"));


  }

  public void wfAjaxPay(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectData3(wp.itemStr("ax_pay"));
    if (rc != 1) {
      wp.addJSON("acct_type", "");
      wp.addJSON("acno_name", "");
      wp.addJSON("stmt_cycle", "");
      wp.addJSON("acct_key", "");
      wp.addJSON("p_seqno", "");
      return;
    }
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("acct_key", sqlStr("acct_key"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));

  }

  public void ajaxChkPayno(TarokoCommon wr) throws Exception {
    super.wp = wr;

    String js_payment_no_1 = wp.itemStr("aj_payment_no_1");
    String js_payno_flag   = "N";
    String js_payment_no   = "";

    String ls_sql = "select "
            + " acct_type , "
            + " acct_key , "
            + " p_seqno "
            + " from act_acno "
            + " where payment_no = ? "
            + " and acno_p_seqno = p_seqno ";
    this.sqlSelect(ls_sql, new Object[]{js_payment_no_1});

    if (sqlRowNum <= 0) {
      String ls_sql2 = "select "
              + " acct_type , "
              + " acct_key , "
              + " payment_no , "
              + " p_seqno "
              + " from act_acno "
              + " where payment_no like substr(:payment_no,1,12)||'%' "
              + " and acno_p_seqno = p_seqno "
              + " fetch first 1 row only ";
      this.setString("payment_no", js_payment_no_1);
      sqlSelect(ls_sql2);
      if (sqlRowNum > 0) {
        js_payment_no = sqlStr("payment_no");
      } else {
        js_payment_no = "";
      }
    } else {
      js_payno_flag   = "Y";
      js_payment_no   = "";
    }

    wp.addJSON("ax_payno_flag", js_payno_flag);
    wp.addJSON("ax_payment_no", js_payment_no);

  }

  void selectData1(String s1, String s2) throws Exception {

    s1 = commString.acctKey(s1);
    if (empty(s1)) {
      if (s1.length() != 11) {
        alertErr("帳戶帳號輸入錯誤 帳號:" + s1);
        return;
      }
    }

    if (empty(s2)) s2 = "01";

    String ls_sql = "select "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " payment_no , "
            + " p_seqno "
            + " from act_acno "
            + " where acct_key = ? "
            + " and acct_type = ? "
            + " and acno_p_seqno = p_seqno ";
    sqlSelect(ls_sql, new Object[]{s1, s2});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_acct_key=" + s1);
    }
    return;
  }

  void selectData2(String s1) throws Exception {
    String ls_sql = "select "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " payment_no , "
            + " p_seqno , "
            + " acct_type , "
            + " acct_key "
            + " from act_acno "
            + " where acno_p_seqno in (select acno_p_seqno from crd_card where card_no =?)";


    sqlSelect(ls_sql, new Object[]{s1});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_card_no=" + s1);
    }
    return;
  }

  void selectData3(String s1) throws Exception {
    String ls_sql = "select "
            + " acct_type , "
            + " acct_key , "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " p_seqno "
            + " from act_acno "
            + " where payment_no =?"
            + " and acno_p_seqno = p_seqno ";
    this.sqlSelect(ls_sql, new Object[]{s1});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_pay_no=" + s1);
    }
    return;
  }

  String selectTranTypeDesc(String ls_tran_type) throws Exception {
    String sql1 = " select "
            + " bill_desc "
            + " from ptr_payment "
            + " where payment_type = ? ";

    sqlSelect(sql1, new Object[]{ls_tran_type});

    if (sqlRowNum > 0) return sqlStr("bill_desc");

    return ls_tran_type;
  }

  void tabClick() {
    wp.colSet("a_click_1", "t_click_1");
    wp.colSet("a_click_2", "t_click_2");
    wp.colSet("a_click_3", "t_click_3");
    wp.colSet("a_click_4", "t_click_4");
    wp.colSet("a_click_5", "t_click_5");
    wp.colSet("a_click_6", "t_click_6");
    wp.colSet("a_click_7", "t_click_7");
    wp.colSet("a_click_8", "t_click_8");
    wp.colSet("a_click_9", "t_click_9");

    String isClick = "";
    isClick = wp.itemStr("tab_click");
    if (eqIgno(isClick, "1")) {
      wp.colSet("a_click_1", "tab_active");
    }
    else if (eqIgno(isClick, "2")) {
      wp.colSet("a_click_2", "tab_active");
    }
    else if (eqIgno(isClick, "3")) {
      wp.colSet("a_click_3", "tab_active");
    }
    else if (eqIgno(isClick, "4")) {
      wp.colSet("a_click_4", "tab_active");
    }
    else if (eqIgno(isClick, "5")) {
      wp.colSet("a_click_5", "tab_active");
    }
    else if (eqIgno(isClick, "6")) {
      wp.colSet("a_click_6", "tab_active");
    }
    else if (eqIgno(isClick, "7")) {
      wp.colSet("a_click_7", "tab_active");
    }
    else if (eqIgno(isClick, "8")) {
      wp.colSet("a_click_8", "tab_active");
    }
    else if (eqIgno(isClick, "9")) {
      wp.colSet("a_click_9", "tab_active");
    }
    else {
      wp.colSet("a_click_1", "tab_active");
    }
  }

}
