package colm05;
/** 強制停卡明細表
 * 19-1210:   Alex  fix queryAfter
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.02  Tanwei       updated for project coding standard
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;

import taroko.com.TarokoPDF;

public class Colr5740 extends BaseAction implements InfacePdf {
  boolean lbId = false;
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
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    int llCnt = 0, ii = 0, rr = -1;

    String lsAcctKey = "" , lsAcnoPSeqno = "";
    lsAcctKey = wp.itemStr("ex_acct_key");
    if (wp.itemEmpty("ex_stop_ym") && empty(lsAcctKey)) {
      alertErr2("強停年月 , 身分證ID/統編 : 不可同時空白 ");
      return;
    }

    if (!empty(lsAcctKey) && lsAcctKey.length() < 8) {
      alertErr2("身分證/統編 : 至少8碼");
      return;
    }
    
    if(!empty(lsAcctKey)) {
    	checkAcctKey(lsAcctKey);
    }
    

    String sql1 = " select distinct log_date , uf_acno_key(acno_p_seqno) as acct_key , "
            + " acct_type , acno_p_seqno from rsk_acnolog where 1=1 "
            + sqlCol(wp.itemStr("ex_stop_ym"), "log_date", "like%")
            + " and nvl(log_type ,'') = '2' " + " and nvl(log_not_reason,'') = '' ";
    
    if (!empty(lsAcctKey)) {
//      sql1 += " and acno_p_seqno in (select acno_p_seqno from act_acno where acct_key like '"
//          + lsAcctKey + "%')";
    	if(lbId) {
    		sql1 += " and acno_p_seqno in (select acno_p_seqno from act_acno where id_p_seqno in "
    			 + " (select id_p_seqno from crd_idno where 1=1 "+sqlCol(lsAcctKey,"id_no")+"))";
    	}	else {
    		sql1 += " and acno_p_seqno in (select acno_p_seqno from act_acno where corp_p_seqno in "
       			 + " (select corp_p_seqno from crd_corp where 1=1 "+sqlCol(lsAcctKey,"corp_no")+"))";
    	}
    	
    }
    sql1 += " order by 1 , 2 , 3 ";
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }

    String detailAcctKey = "" , detailCardNo = "";
    llCnt = sqlRowNum;
    for (ii = 0; ii < llCnt; ii++) {
      // --check max stop date
      detailAcctKey = "";
      detailCardNo = "";
      int liCnt = 0;
      String sql2 =
          " select " + " count(*) as li_cnt " + " from rsk_acnolog " + " where acno_p_seqno = ? "
              + " and log_date > ? " + " and nvl(log_type,'') = '2' " + " and log_not_reason = '' ";
      sqlSelect(sql2, new Object[] {sqlStr(ii, "acno_p_seqno"), sqlStr(ii, "log_date")});
      liCnt = sqlInt("li_cnt");
      if (liCnt > 0)
        continue;
      String sql3 = " select " + " uf_acno_name(A.acno_p_seqno) as chi_name , "
          + " A.aft_loc_amt as line_credit_amt , " + " A.acct_jrnl_bal , "
          + " A.ccas_mcode_aft as payment_rate1 , " + " A.log_mode , "
          + " A.log_mode||'.'||decode(A.log_mode,'1','人工','2','批次') as tt_log_mode ,"
          + " A.block_reason , " + " A.block_reason2 , " + " A.block_reason3 , "
          + " A.block_reason4 , " + " A.block_reason5 , " + " A.spec_status , " + " B.stmt_cycle "
          + " from act_acno B, rsk_acnolog A " + " where A.acno_p_seqno = B.acno_p_seqno "
          + " and A.acno_p_seqno = ? " + " and A.log_date = ? " + " and A.log_type = '2' "
          + " and A.log_not_reason = '' ";
      if (!eqIgno(wp.itemStr("ex_log_mode"), "0")) {
        sql3 += " and A.log_mode = ?  " + commSqlStr.rownum(1);
        sqlSelect(sql3, new Object[] {sqlStr(ii, "acno_p_seqno"), sqlStr(ii, "log_date"),
            wp.itemStr("ex_log_mode")});
      } else {
        sql3 += commSqlStr.rownum(1);
        sqlSelect(sql3, new Object[] {sqlStr(ii, "acno_p_seqno"), sqlStr(ii, "log_date")});
      }

      if (sqlRowNum <= 0)
        continue;
      if (!empty(wp.itemStr("ex_stmt_cycle"))
          && !eqIgno(sqlStr("stmt_cycle"), wp.itemStr("ex_stmt_cycle")))
        continue;

      String sql4 = " select " + " min(issue_date) as card_since " + " from crd_card "
          + " where acno_p_seqno = ? " + " and sup_flag = '0' ";
      sqlSelect(sql4, new Object[] {sqlStr(ii, "acno_p_seqno")});

      if (sqlRowNum <= 0) {
        sqlSet(0, "card_since", "");
      }

      String sql5 = " select " + " a.chi_name as sup_chi_name , " + " a.id_no as sup_idno , "
          + " b.oppost_date as sup_oppo_date " + " from crd_idno a,crd_card b "
          + " where a.id_p_seqno = b.id_p_seqno " + " and b.sup_flag = '1' "
          + " and b.acno_p_seqno = ? " + commSqlStr.rownum(1);
      sqlSelect(sql5, new Object[] {sqlStr(ii, "acno_p_seqno")});
      if (sqlRowNum <= 0) {
        sqlSet(0, "sup_chi_name", "");
        sqlSet(0, "sup_idno", "");
        sqlSet(0, "sup_oppo_date", "");
      }
      
      String sql6 = "select id_p_seqno as detail_id_p_seqno , corp_p_seqno as detail_corp_p_seqno , acno_p_seqno as detail_acno_p_seqno from act_acno where acct_type = ? and acct_key = ? ";
      sqlSelect(sql6,new Object[] {sqlStr(ii,"acct_type"),sqlStr(ii,"acct_key")});
      if(sqlRowNum >0) {
    	  if(empty(sqlStr("detail_corp_p_seqno"))) {
    		  String sql7 = "select id_no as detail_acct_key from crd_idno where id_p_seqno = ? ";
    		  sqlSelect(sql7,new Object[] {sqlStr("detail_id_p_seqno")});
    		  if(sqlRowNum > 0 ) {
    			  detailAcctKey = sqlStr("detail_acct_key");
    		  }
    	  }	else {
    		  String sql7 = "select corp_no as detail_acct_key from crd_corp where corp_p_seqno = ? ";
    		  sqlSelect(sql7,new Object[] {sqlStr("detail_corp_p_seqno")});
    		  if(sqlRowNum > 0 ) {
    			  detailAcctKey = sqlStr("detail_acct_key");
    			  String sql8 = "select card_no as detail_card_no from crd_card where acno_p_seqno = ? " + sqlRownum(1);
    			  sqlSelect(sql8,new Object[] {sqlStr(ii, "acno_p_seqno")});
    			  if(sqlRowNum > 0 ) {
    				  detailCardNo = sqlStr("detail_card_no");
    			  }
    		  }
    	  }
      }
      
      rr++;
      if (rr < 9) {
        wp.colSet(rr, "ser_num", "0" + (rr + 1));
      } else {
        wp.colSet(rr, "ser_num", "" + (rr + 1));
      }
      // ddd("p_seqno:"+sql_ss(ii,"p_seqno"));
      // log_date , acct_key , p_seqno , acct_type
      wp.colSet(rr, "db_idno", detailAcctKey);
      wp.colSet(rr, "db_card_no", detailCardNo);
      wp.colSet(rr, "chi_name", sqlStr("chi_name"));
      wp.colSet(rr, "acct_type", sqlStr(ii, "acct_type"));
      wp.colSet(rr, "card_since", sqlStr("card_since"));
      wp.colSet(rr, "line_credit_amt", sqlStr("line_credit_amt"));
      wp.colSet(rr, "stmt_cycle", sqlStr("stmt_cycle"));
      wp.colSet(rr, "log_date", sqlStr(ii, "log_date"));
      wp.colSet(rr, "acct_jrnl_bal", sqlStr("acct_jrnl_bal"));
      wp.colSet(rr, "payment_rate1", sqlStr("payment_rate1"));
      wp.colSet(rr, "log_mode", sqlStr("log_mode"));
      wp.colSet(rr, "tt_log_mode", sqlStr("tt_log_mode"));
      wp.colSet(rr, "block_reason", sqlStr("block_reason"));
      wp.colSet(rr, "block_reason2", sqlStr("block_reason2"));
      wp.colSet(rr, "block_reason3", sqlStr("block_reason3"));
      wp.colSet(rr, "block_reason4", sqlStr("block_reason4"));
      wp.colSet(rr, "block_reason5", sqlStr("block_reason5"));
      wp.colSet(rr, "spec_status", sqlStr("spec_status"));
      wp.colSet(rr, "sup_chi_name", sqlStr("sup_chi_name"));
      wp.colSet(rr, "sup_idno", sqlStr("sup_idno"));
      wp.colSet(rr, "sup_oppo_date", sqlStr("sup_oppo_date"));
      wp.colSet(rr, "wk_block_reason", sqlStr("block_reason") + sqlStr("block_reason2")
          + sqlStr("block_reason3") + sqlStr("block_reason4") + sqlStr("block_reason5"));
    }

    if (rr < 0) {
      alertMsg("此條件查無資料");
      return;
    }

    wp.listCount[0] = rr + 1;
    queryAfter(rr + 1);
  }
  
  void checkAcctKey(String akey) {
	  String sql1 = "";
	  
	  sql1 = "select count(*) as db_cnt1 from crd_idno where id_no = ? ";
	  sqlSelect(sql1,new Object[] {akey});
	  if(sqlNum("db_cnt1") > 0) {
		  lbId = true ;
		  return ;
	  }
	  
	  sql1 = "select count(*) as db_cnt2 from crd_corp where corp_no = ? ";
	  sqlSelect(sql1,new Object[] {akey});
	  if(sqlNum("db_cnt2") > 0) {
		  lbId = false ;
		  return ;
	  }
		  
  }
  
  void queryAfter(int rr) {
    int liCnt1 = 0, liCnt2 = 0, liCnt3 = 0, liCnt5 = 0, liCnt6 = 0;
    int limit01 = 0, limit02 = 0, limit03 = 0, limit05 = 0, limit06 = 0;
    int jrnlBal01 = 0, jrnlBal02 = 0, jrnlBal03 = 0, jrnlBal05 = 0, jrnlBal06 = 0;
    int cnt01Mode1 = 0, cnt02Mode1 = 0, cnt03Mode1 = 0, cnt05Mode1 = 0, cnt06Mode1 = 0;
    int jrnlBal01Mode1 = 0, jrnlBal0Mode1 = 0, jrnlBal03Mode1 = 0, jrnlBal05Mode1 = 0,
        jrnlBal06Mode1 = 0;
    int cnt01Mode2 = 0, cnt02Mode2 = 0, cnt03Mode2 = 0, cnt05Mode2 = 0, cnt06Mode2 = 0;
    int jrnlBal0Mode2 = 0, jrnlBal02Mode2 = 0, jrnlBal03Mode2 = 0, jrnlBal05Mode2 = 0,
        jrnlBal06Mode2 = 0;
    int tlCnt = 0, tlLimit = 0, tlJrnlBal = 0, tlCnt1 = 0, tlJrnlBal1 = 0, tlCnt2 = 0,
        tlJrnlBal2 = 0;
    for (int ii = 0; ii < rr; ii++) {
      if (eqIgno(wp.colStr(ii, "acct_type"), "01")) {
        liCnt1++;
        limit01 += wp.colNum(ii, "line_credit_amt");
        jrnlBal01 += wp.colNum(ii, "acct_jrnl_bal");
        if (eqIgno(wp.colStr(ii, "log_mode"), "1")) {
          cnt01Mode1++;
          jrnlBal01Mode1 += wp.colNum(ii, "acct_jrnl_bal");
        } else if (eqIgno(wp.colStr(ii, "log_mode"), "2")) {
          cnt01Mode2++;
          jrnlBal0Mode2 += wp.colNum(ii, "acct_jrnl_bal");
        }
      } else if (eqIgno(wp.colStr(ii, "acct_type"), "03")) {
        liCnt3++;
        limit03 += wp.colNum(ii, "line_credit_amt");
        jrnlBal03 += wp.colNum(ii, "acct_jrnl_bal");
        if (eqIgno(wp.colStr(ii, "log_mode"), "1")) {
          cnt03Mode1++;
          jrnlBal03Mode1 += wp.colNum(ii, "acct_jrnl_bal");
        } else if (eqIgno(wp.colStr(ii, "log_mode"), "2")) {
          cnt03Mode2++;
          jrnlBal03Mode2 += wp.colNum(ii, "acct_jrnl_bal");
        }
      } else if (eqIgno(wp.colStr(ii, "acct_type"), "05")) {
        liCnt5++;
        limit05 += wp.colNum(ii, "line_credit_amt");
        jrnlBal05 += wp.colNum(ii, "acct_jrnl_bal");
        if (eqIgno(wp.colStr(ii, "log_mode"), "1")) {
          cnt05Mode1++;
          jrnlBal05Mode1 += wp.colNum(ii, "acct_jrnl_bal");
        } else if (eqIgno(wp.colStr(ii, "log_mode"), "2")) {
          cnt05Mode2++;
          jrnlBal05Mode2 += wp.colNum(ii, "acct_jrnl_bal");
        }
      } else if (eqIgno(wp.colStr(ii, "acct_type"), "06")) {
        liCnt6++;
        limit06 += wp.colNum(ii, "line_credit_amt");
        jrnlBal06 += wp.colNum(ii, "acct_jrnl_bal");
        if (eqIgno(wp.colStr(ii, "log_mode"), "1")) {
          cnt06Mode1++;
          jrnlBal06Mode1 += wp.colNum(ii, "acct_jrnl_bal");
        } else if (eqIgno(wp.colStr(ii, "log_mode"), "2")) {
          cnt06Mode2++;
          jrnlBal06Mode2 += wp.colNum(ii, "acct_jrnl_bal");
        }
      }
    }
    tlCnt = liCnt1 + liCnt2 + liCnt3 + liCnt5 + liCnt6;
    tlLimit = limit01 + limit02 + limit03 + limit05 + limit06;
    tlJrnlBal = jrnlBal01 + jrnlBal02 + jrnlBal03 + jrnlBal05 + jrnlBal06;
    tlCnt1 = cnt01Mode1 + cnt02Mode1 + cnt03Mode1 + cnt05Mode1 + cnt06Mode1;
    tlJrnlBal1 = jrnlBal01Mode1 + jrnlBal0Mode1 + jrnlBal03Mode1 + jrnlBal05Mode1
        + jrnlBal06Mode1;
    tlCnt2 = cnt01Mode2 + cnt02Mode2 + cnt03Mode2 + cnt05Mode2 + cnt06Mode2;
    tlJrnlBal2 = jrnlBal0Mode2 + jrnlBal02Mode2 + jrnlBal03Mode2 + jrnlBal05Mode2
        + jrnlBal06Mode2;

    wp.colSet("cnt_01", "" + liCnt1);    
    wp.colSet("cnt_03", "" + liCnt3);
    wp.colSet("cnt_05", "" + liCnt5);
    wp.colSet("cnt_06", "" + liCnt6);
    wp.colSet("limit_01", "" + limit01);    
    wp.colSet("limit_03", "" + limit03);
    wp.colSet("limit_05", "" + limit05);
    wp.colSet("limit_06", "" + limit06);
    wp.colSet("jrnl_bal_01", "" + jrnlBal01);    
    wp.colSet("jrnl_bal_03", "" + jrnlBal03);
    wp.colSet("jrnl_bal_05", "" + jrnlBal05);
    wp.colSet("jrnl_bal_06", "" + jrnlBal06);
    wp.colSet("cnt_01mode_1", "" + cnt01Mode1);    
    wp.colSet("cnt_03mode_1", "" + cnt03Mode1);
    wp.colSet("cnt_05mode_1", "" + cnt05Mode1);
    wp.colSet("cnt_06mode_1", "" + cnt06Mode1);
    wp.colSet("jrnl_bal_01mode_1", "" + jrnlBal01Mode1);    
    wp.colSet("jrnl_bal_03mode_1", "" + jrnlBal03Mode1);
    wp.colSet("jrnl_bal_05mode_1", "" + jrnlBal05Mode1);
    wp.colSet("jrnl_bal_06mode_1", "" + jrnlBal06Mode1);
    wp.colSet("cnt_01mode_2", "" + cnt01Mode2);    
    wp.colSet("cnt_03mode_2", "" + cnt03Mode2);
    wp.colSet("cnt_05mode_2", "" + cnt05Mode2);
    wp.colSet("cnt_06mode_2", "" + cnt06Mode2);
    wp.colSet("jrnl_bal_01mode_2", "" + jrnlBal0Mode2);
    wp.colSet("jrnl_bal_03mode_2", "" + jrnlBal03Mode2);
    wp.colSet("jrnl_bal_05mode_2", "" + jrnlBal05Mode2);
    wp.colSet("jrnl_bal_06mode_2", "" + jrnlBal06Mode2);
    wp.colSet("tl_cnt", "" + tlCnt);
    wp.colSet("tl_limit", "" + tlLimit);
    wp.colSet("tl_jrnl_bal", "" + tlJrnlBal);
    wp.colSet("tl_cnt1", "" + tlCnt1);
    wp.colSet("tl_jrnl_bal_1", "" + tlJrnlBal1);
    wp.colSet("tl_cnt2", "" + tlCnt2);
    wp.colSet("tl_jrnl_bal_2", "" + tlJrnlBal2);
  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "Colr5740";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "強停月份 :" + commString.strToYmd(wp.itemStr("ex_stop_ym"));
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5740.xlsx";
    pdf.pageCount = 25;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;
  }

}
