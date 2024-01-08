package actq01;
/**
 * 2022-0323	JH		9321.預算結清違約金
 * 2021-0519.7054		JH		xxx_bal_db_B/I/C
 * 2021-0119.5601		JH		預算結清違約金:
 * 2020-0323	JH		db_
 * 2020-0211   Alex  ptr_actpenalty fix
 * 2019-1226   Alex  bugfix
 * 2019-1204   Alex  bugfix
 * 2019-0813   JH    bugfix  
 * 111-10-12  Machao     功能查詢失敗bug调整    * 
 * */

import busi.FuncAction;

public class Actq0020Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  public String modVersion() { return "v22.0323 mt9321"; }
  
  boolean isInqlog = true;
  String lsPgmName = "", lsDesc = "", lsCardNo = "";
  String isAcctMonth = "", isThisMm = "", isLastMm = "", isNextMm = "";
  private String isThisDelayDate = "";
  private String isThisIntrDate = "";
  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void wfSaveInqlog(int alQuery, String asNo) throws Exception{
    if(isInqlog == false)  return ;
    lsPgmName ="單月帳務資料查詢";
    lsDesc = wp.colStr("ex_acct_type")+"-"+wp.colStr("ex_acct_key");
		lsCardNo = wp.itemStr2("ex_card_no");
		if(!empty(lsCardNo))	lsDesc+=" "+lsCardNo;
		lsDesc +=" stmt:"+wp.colStr("ex_stmt_cycle").trim()
				   +" hsty:"+wp.itemStr2("ex_history").trim()
				   +" "+wp.itemStr2("ex_yymm")
				   +" Qry:"+asNo+" Cnt:"+commString.intToStr(alQuery);

    if(lsDesc.length()>60){
      lsDesc = lsDesc.substring(0, 60);
    }

    strSql = " insert into log_inq_history ("
    + " prog_code , "
    + " prog_name , "
    + " user_id , "
//				 + " user_name , "
    + " inq_date , "
    + " inq_time , "
    + " acct_key , "
    + " card_no , "
    + " inq_kind , "
    + " inq_desc , "
    + " mod_time , "
    + " mod_pgm "
    + " ) values ("
    + " :prog_code , "
    + " :prog_name , "
    + " :user_id , "
//				 + " :user_name , "
    + " to_char(sysdate,'yyyymmdd') , "
    + " to_char(sysdate,'hh24miss') , "
    + " :acct_key , "
    + " :card_no , "
    + " 'I' , "
    + " :inq_desc , "
    + " sysdate , "
    + " :mod_pgm"
    + " )"
    ;

    setString("prog_code", wp.modPgm());
    setString("prog_name", lsPgmName);
    setString("user_id", wp.loginUser);
    setString("acct_key", wp.itemStr("ex_acct_key"));
    setString("card_no", lsCardNo);
    setString("inq_desc", lsDesc);
    setString("mod_pgm", wp.modPgm());
    sqlExec(strSql);
    if (sqlRowNum > 0) {
      this.sqlCommit(1);
    }   else   {
      sqlCommit(0);
    }

    return;
  }

  // --
  public int wfCloseEnq() throws Exception {
//		String ls_val[20],beg_date,end_date, ls_acct_p_seqno
//		dec    ldc_val[12], ld_interest_rate, ld_interest_amt, ld_interest_amtsum
//		dec    ld_intr,ld_penl
//		Long	 L,ws_days
//		int	li_mcode=0

    double lmInterestAmtsum = 0;
    // -- Get business_date --
    String lsBusiDate = this.businDate();
    String[] aaPayrate=new String[6];
	String lsPseqno =varsStr2("is_p_seqno");

    // -- Get some data from ptr_workday --
    // -若繳款日小於等於寬限期則不計息-
	String ss = wp.colStr("ex_delay_date");
    if (commString.strComp(lsBusiDate, wp.colStr("ex_delay_date")) <= 0) return 1;

    double[] lmRevoIntr = {0, 0, 0, 0, 0, 0, 0};
    strSql ="select revolving_interest1, revolving_interest2,"
        +" revolving_interest3, revolving_interest4,"
        +" revolving_interest5, revolving_interest6"
        +" from ptr_actgeneral_n"
        + " where acct_type =? " + commSqlStr.rownum(1);
    sqlSelect(strSql, new Object[] {varsStr("is_acct_type")});
    if (sqlRowNum > 0) {
      lmRevoIntr[1] = colNum("revolving_interest1");
      lmRevoIntr[2] = colNum("revolving_interest2");
      lmRevoIntr[3] = colNum("revolving_interest3");
      lmRevoIntr[4] = colNum("revolving_interest4");
      lmRevoIntr[5] = colNum("revolving_interest5");
      lmRevoIntr[6] = colNum("revolving_interest6");
    }
    // -- Get temp bill from act_acct --
    strSql ="select temp_unbill_interest,"
    +" acct_jrnl_bal,"
    +" last_min_pay_date,"
    +" delaypay_ok_flag, ao_fee_bal, ttl_amt " 
    +" from  act_acct"
    +" where  p_seqno =?";

    double lmUnbillIntr = 0, lmAcctJrnlBal = 0;
    String lsMinPayDate = "";
    String lsDelaypayOk = "";
    double lmAoFeeBal = 0;
    setString2(1, lsPseqno);
    daoTid = "acct.";
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      lmUnbillIntr = colNum("acct.temp_unbill_interest");
      if (colEq("acct.delaypay_ok_flag", "Y"))
        lmUnbillIntr = 0;
      lmAcctJrnlBal = colNum("acct.acct_jrnl_bal");
      lmAoFeeBal = colNum("acct.ao_fee_bal");
//      lmTtlAmt = colNum("acct.ttl_amt");
    }

    // -- get special revolving rate from act_acno --
    String lsAcnoRevoSMon = "";
    String lsAcnoRevoEMon = "";
    String lsAcnoRevoSign = "";
    double lmAcnoRevoRate = 0;
    strSql ="select revolve_rate_s_month,"
    +" revolve_rate_e_month,"
    +" revolve_int_sign,"
    +" revolve_int_rate"+
    ", PAYMENT_RATE1, PAYMENT_RATE2, PAYMENT_RATE3"+
    ", PAYMENT_RATE4, PAYMENT_RATE5, PAYMENT_RATE6"
    +" from  act_acno"
    +" where  p_seqno =?";
    setString2(1, lsPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      lsAcnoRevoSMon = colNvl("revolve_rate_s_month", "99912");
      lsAcnoRevoEMon = colNvl("revolve_rate_e_month", "99912");
      lsAcnoRevoSign = colStr("revolve_int_sign");
      lmAcnoRevoRate = colNum("revolve_int_rate");   
      aaPayrate[0] =colStr("payment_rate1");
      aaPayrate[1] =colStr("payment_rate2");
      aaPayrate[2] =colStr("payment_rate3");
      aaPayrate[3] =colStr("payment_rate4");
      aaPayrate[4] =colStr("payment_rate5");
      aaPayrate[5] =colStr("payment_rate6");
    }

    // -- get end_bal > 0 from act_debt --
    busi.DataSet dsDebt2 = new busi.DataSet();
    strSql ="SELECT  reference_no,"
    +" post_date,"
    +" end_bal,"
    +" interest_rs_date,"
    +" acct_code,"
    +" acct_month,"
    +" stmt_cycle,"
    +" interest_date,"
    +" int_rate,"
    +" int_rate_flag"
    +" FROM act_debt   "
    +" where p_seqno =?"
    +" and end_bal >0 and acct_code <>'DP'";
    setString2(1, lsPseqno);
    dsDebt2.colList = sqlQuery(strSql);
    if (dsDebt2.listRows() <= 0)
      return 1;

    // -get Mcode-
    busi.func.EcsComm func = new busi.func.EcsComm();
    func.setConn(wp.getConn());
    int liMcode = func.getMcode(varsStr2("is_p_seqno"));

    double lmIntrAmtSum = 0;
    while (dsDebt2.listNext()) {
      strSql ="select inter_rate_code, interest_method"
      +" from  ptr_actcode"
      +" where  acct_code =?" // :ls_val[10]
      ;
      // into :ls_val[11], :ls_val[12]
      setString2(1, dsDebt2.colStr("acct_code"));
      sqlSelect(strSql);
      if (sqlRowNum>0) {
        if (colEq("interest_method", "Y")==false)
          continue;
      }
      // -- set up the standard interest rate
      int liRevo = colInt("inter_rate_code");
      double lmIntrRate = lmRevoIntr[liRevo];

      // -- add special revolving rate --
      if (commString.comp(varsStr("is_busi_date"), lsAcnoRevoSMon, 6) >= 0 && 
            commString.comp(varsStr("is_busi_date"), lsAcnoRevoEMon, 6) <= 0) {
        if (eqIgno(lsAcnoRevoSign,"+"))
          lmIntrRate += lmAcnoRevoRate;
        else lmIntrRate = lmIntrRate - lmAcnoRevoRate;
      }

      // -- get some field of act_debt --
      String lsDebtIntrDate = dsDebt2.colStr("interest_date");
      String lsDebtIntrRsDate = dsDebt2.colStr("interest_rs_date");
      String lsDebtAcctMonth = dsDebt2.colStr("acct_month");
      double lmDebtEndBal = dsDebt2.colNum("end_bal");
      // -- unbill skip --
      if (eqAny(lsDebtAcctMonth, varsStr("is_next_mm")))
        continue;

      String lsBegDate = varsStr("is_this_intr_date");
      String lsEndDate = varsStr("is_busi_date");
      if (!empty(lsDebtIntrRsDate)) {
        lsBegDate = lsDebtIntrRsDate;
        lsEndDate = varsStr("is_busi_date");
      } 
      else {
        if (eqAny(lsDebtAcctMonth, varsStr("is_this_mm"))) {
          lsBegDate = lsDebtIntrDate;
          lsEndDate = varsStr("is_busi_date");
        }
      }

      int liDays=0;
      liDays = commDate.daysBetween(lsBegDate, lsEndDate);
      if (dsDebt2.colEq("int_rate_flag", "Y") && liMcode == 0) {
        lmIntrRate = dsDebt2.colNum("int_rate");
      }
      double lmIntrAmt = commString.numScale(dsDebt2.colNum("end_bal") * liDays * lmIntrRate / 10000, 2);
      lmIntrAmtSum += lmIntrAmt;
    } // while-list.next()--

    // -ld_intr
    double lmIntr = commString.numScale(lmIntrAmtSum + lmUnbillIntr, 0);

    // --預算結清違約金 fr ptr_actpenalty
	double lmTtlAmt =0;
	strSql ="select sum(billed_end_bal) as xx_ttl_amt"+
			" from act_acct_sum"+
			" where p_seqno=? "+
			" and acct_code in (select acct_code from ptr_actcode where interest_method='Y') ";
	sqlSelect(strSql,lsPseqno);
	if (sqlRowNum >0) {
		lmTtlAmt =colNum("acct.ttl_amt");
	}
	//double ld_penl=0;
	int liPenl=0;
	if (lmTtlAmt>0) {
		daoTid ="apty.";
		strSql ="SELECT A.acct_month, A.penalty_amt" +
				", A.org_pn_flag, A.act_pn_flag"+
				", C.first_penalty, C.second_penalty, C.third_penalty"+
				" FROM act_penalty_log A JOIN act_acno B ON A.p_seqno=B.p_seqno"+
				"      JOIN ptr_actpenalty C ON C.acct_type=B.acct_type"+
				" WHERE A.p_seqno =?" +
				" ORDER BY acct_month desc" + commSqlStr.rownum(1)
		;
		sqlSelect(strSql,lsPseqno);
		if (sqlRowNum >0) {
			int liPlamt =colInt("apty.penalty_amt");
			String lsOrgFlag= colNvl("apty.org_pn_flag","N");
			String lsPnFlag =colNvl("apty.act_pn_flag","N");
			int liPnAmt1 =colInt("apty.first_penalty");
			int liPnAmt2 =colInt("apty.second_penalty");
			int liPnAmt3 =colInt("apty.third_penalty");
			//當org_pn_flag='N' and act_pn_flag='N'
			//或org_pn_flag='Y' and act_pn_flag='1'
			//下次收 ptr_actpenalty.first_penalty
			if ( (eq(lsOrgFlag,"N") && eq(lsPnFlag,"N"))
			|| (eq(lsOrgFlag,"Y") && eq(lsPnFlag,"1")) ) {
				liPenl =liPnAmt1;
			}
			//當org_pn_flag='Y' and act_pn_flag='Y' and penalty_amt=ptr_actpenalty.first_penalty
			//或org_pn_flag='Y' and act_pn_flag='2'
			//下次收 ptr_actpenalty.second_penalty
			if ( (eq(lsOrgFlag,"Y") && eq(lsPnFlag,"Y") && liPnAmt1==liPlamt) ||
					(eq(lsOrgFlag,"Y") && eq(lsPnFlag,"2")) ) {
				liPenl =liPnAmt2;
			}
			//當org_pn_flag='Y' and act_pn_flag='Y' and penalty_amt=ptr_actpenalty.second_penalty
			//或org_pn_flag='Y' and act_pn_flag='3'
			//下次收 ptr_actpenalty.third_penalty
			if ( (eq(lsOrgFlag,"Y") && eq(lsPnFlag,"Y") && liPnAmt2==liPlamt) ||
					(eq(lsOrgFlag,"Y") && eq(lsPnFlag,"3")) ) {
				liPenl =liPnAmt3;
			}
		}
	}
	//-JH-20220323-
	//最近沖銷MP=0的日期 between ptrm0080.關帳日期-本次and 繳款寬延日期-本次
	if (liPenl >0) {
		strSql="select count(*) as penl_cnt"
				+" FROM act_acct A , ptr_workday B"
				+" WHERE A.p_seqno =? and B.stmt_cycle =?"
				+" AND A.last_min_pay_date BETWEEN B.this_close_date AND B.this_delaypay_date"
				;
		setString(1, lsPseqno);
		setString(wp.colStr("ex_stmt_cycle"));
		sqlSelect(strSql);
		if (sqlRowNum >0 && colInt("penl_cnt")>0) {
			liPenl =0;
		}
	}
	//--
	wp.colSet("A1_db_adv_penl",liPenl);
	wp.colSet("A1_db_aofee",lmAoFeeBal);		//get ao_fee_bal fr act_acct
	wp.colSet("A1_db_adv_intr",lmIntr);
//	wp.col_set("A1_db_adv_total",col_num("acct.acct_jrnl_bal") + lm_intr + ld_penl + col_num("acct.ao_fee_bal"));

	return 1;
  }

  public int wfReadDebt() throws Exception {
    String lsPseqno = varsStr("is_p_seqno");
    if (empty(lsPseqno)) {
      errmsg("帳戶流水號: 不可空白");
      return -1;
    }
    String lsAcename = "AF|LF|CF|PF|BL|CA|ID|IT|RI|PN|AO|AI|SF|DP|CB|CI|CC|DB|OT";

    String sqlDebt ="SELECT acct_code,"
    +" sum(end_bal) as db_endbal,"
    +" sum(beg_bal) as db_begbal "
    +" from act_debt"
    +" where locate(acct_code,'AF|LF|CF|PF|BL|CA|ID|IT|RI|PN|AO|AI|SF|DP|CB|CI|CC|DB|OT') >0"
     ;

    busi.DataSet idsDebt = new busi.DataSet();
    // --get billed_end_bal_XX--
    String sql1 = "";
    String lsThisMm = varsStr("is_this_mm");
    if (!empty(lsThisMm)) {
      sql1 =sqlDebt+
            " and p_seqno =? and acct_month <=?"+
            " group by acct_code";	
      setString2(1, lsPseqno);
      setString(lsThisMm);

      idsDebt.colList =sqlQuery(sql1);
      while (idsDebt.listNext()) {
        String lsCode =idsDebt.colStr("acct_code");
        String lsCol ="A2_billed_end_bal_" + lsCode;
        wp.colSet(lsCol, idsDebt.colStr("db_endbal"));
      }
    }

    // --Set unbilled_end_bal_XX--
    idsDebt.dataClear();
    String lsNextMm = varsStr("is_next_mm");
    if (!empty(lsNextMm)) {
      sql1=sqlDebt+
         " and p_seqno =? and acct_month =?"+
            " group by acct_code";
      setString2(1, lsPseqno);
      setString2(2, lsNextMm);
      idsDebt.colList = sqlQuery(sql1);
      while (idsDebt.listNext()) {
        String lsCode = idsDebt.colStr("acct_code");
        // if (commString.ssIN(ls_code,ls_acename)==false)
        // continue;
        wp.colSet("A2_unbill_end_bal_" + lsCode, idsDebt.colStr("db_endbal"));
        wp.colSet("A2_unbill_beg_bal_" + lsCode, idsDebt.colStr("db_begbal"));
      }
    }

    // --Set unbilled/billed_beg_bal_XX--
    String lsLastMm = varsStr("is_last_mm");
    if (!empty(lsLastMm)) {
      strSql="select nvl(billed_end_bal_af,0) + nvl(unbill_end_bal_af,0) as bal_af,"
      +" nvl(billed_end_bal_lf,0) + nvl(unbill_end_bal_lf,0) as bal_lf,"
      +" nvl(billed_end_bal_pf,0) + nvl(unbill_end_bal_pf,0) as bal_pf,"
      +" nvl(billed_end_bal_bl,0) + nvl(unbill_end_bal_bl,0) as bal_bl,"
      +" nvl(billed_end_bal_ca,0) + nvl(unbill_end_bal_ca,0) as bal_ca,"
      +" nvl(billed_end_bal_it,0) + nvl(unbill_end_bal_it,0) as bal_it,"
      +" nvl(billed_end_bal_id,0) + nvl(unbill_end_bal_id,0) as bal_id,"
      +" nvl(billed_end_bal_ri,0) + nvl(unbill_end_bal_ri,0) as bal_ri,"
      +" nvl(billed_end_bal_pn,0) + nvl(unbill_end_bal_pn,0) as bal_pn,"
      +" nvl(billed_end_bal_ao,0) + nvl(unbill_end_bal_ao,0) as bal_ao,"
      +" nvl(billed_end_bal_ai,0) + nvl(unbill_end_bal_ai,0) as bal_ai,"
      +" nvl(billed_end_bal_sf,0) + nvl(unbill_end_bal_sf,0) as bal_sf,"
      +" nvl(billed_end_bal_dp,0) + nvl(unbill_end_bal_dp,0) as bal_dp,"
      +" nvl(billed_end_bal_cb,0) + nvl(unbill_end_bal_cb,0) as bal_cb,"
      +" nvl(billed_end_bal_ci,0) + nvl(unbill_end_bal_ci,0) as bal_ci,"
      +" nvl(billed_end_bal_cf,0) + nvl(unbill_end_bal_cf,0) as bal_cf,"
      +" nvl(billed_end_bal_db,0) + nvl(unbill_end_bal_db,0) as bal_db,"
      +" nvl(billed_end_bal_cc,0) + nvl(unbill_end_bal_cc,0) as bal_cc,"
      +" nvl(billed_end_bal_db_b,0) + nvl(unbill_end_bal_db_b,0) as bal_dbb,"
      +" nvl(billed_end_bal_db_i,0) + nvl(unbill_end_bal_db_i,0) as bal_dbi,"
      +" nvl(billed_end_bal_db_c,0) + nvl(unbill_end_bal_db_c,0) as bal_dbc,"
      +" nvl(billed_end_bal_ot,0)+nvl(unbill_end_bal_ot,0) as bal_ot"
      +" FROM act_acct_hst"
      +" WHERE p_seqno = ?" //:ls_p_seqno
      +" and acct_month =?" //:is_last_mm
      ;
      setString2(1, lsPseqno);
      setString2(2, lsLastMm);
      sqlSelect(strSql);
      if (sqlRowNum <= 0)
        return 1;
        wp.colSet("A2_billed_beg_bal_af", colStr("bal_af"));
        wp.colSet("A2_billed_beg_bal_lf", colStr("bal_lf"));
        wp.colSet("A2_billed_beg_bal_pf", colStr("bal_pf"));
        wp.colSet("A2_billed_beg_bal_bl", colStr("bal_bl"));
        wp.colSet("A2_billed_beg_bal_ca", colStr("bal_ca"));
        wp.colSet("A2_billed_beg_bal_it", colStr("bal_it"));
        wp.colSet("A2_billed_beg_bal_id", colStr("bal_id"));
        wp.colSet("A2_billed_beg_bal_ri", colStr("bal_ri"));
        wp.colSet("A2_billed_beg_bal_pn", colStr("bal_pn"));
        wp.colSet("A2_billed_beg_bal_ao", colStr("bal_ao"));
        wp.colSet("A2_billed_beg_bal_ai", colStr("bal_ai"));
        wp.colSet("A2_billed_beg_bal_sf", colStr("bal_sf"));
        wp.colSet("A2_billed_beg_bal_dp", colStr("bal_dp"));
        wp.colSet("A2_billed_beg_bal_cb", colStr("bal_cb"));
        wp.colSet("A2_billed_beg_bal_ci", colStr("bal_ci"));
        wp.colSet("A2_billed_beg_bal_cf", colStr("bal_cf"));
        wp.colSet("A2_billed_beg_bal_db", colStr("bal_db"));
        wp.colSet("A2_billed_beg_bal_cc", colStr("bal_cc"));
        wp.colSet("A2_billed_beg_bal_db_b", colStr("bal_dbb"));
        wp.colSet("A2_billed_beg_bal_db_i", colStr("bal_dbi"));
        wp.colSet("A2_billed_beg_bal_db_c", colStr("bal_dbc"));
        wp.colSet("A2_billed_beg_bal_ot", colStr("bal_ot"));
    }

    return 1;
  }

  public int wfReadDebtCurr(String aCurr) throws Exception {
    String lsPseqno = wp.itemStr2("ex_p_seqno");
    if (empty(lsPseqno)) {
      errmsg("帳戶流水號: 不可空白");
      return -1;
    }

    wp.colSet("curr_code", aCurr);
    // --
    String sql1 =
        "select dc_acct_jrnl_bal from act_acct_curr" + 
    " where p_seqno =? and curr_code =?";
    sqlSelect(sql1, new Object[] {lsPseqno, aCurr});
    if (sqlRowNum > 0) {
      wp.colSet("acct_jrnl_bal", colNum("dc_acct_jrnl_bal"));
    }

    String sqlDebt = "SELECT acct_code,"
    	      +" sum(dc_end_bal) as db_endbal,"
    	      +" sum(dc_beg_bal) as db_begbal "
    				+", sum(decode(acct_code_type,'B',dc_beg_bal,0)) as db_beg_B"
    				+", sum(decode(acct_code_type,'B',dc_end_bal,0)) as db_end_B"
    				+", sum(decode(acct_code_type,'I',dc_beg_bal,0)) as db_beg_I"
    				+", sum(decode(acct_code_type,'I',dc_end_bal,0)) as db_end_I"
    				+", sum(decode(acct_code_type,'C',dc_beg_bal,0)) as db_beg_C"
    				+", sum(decode(acct_code_type,'C',dc_end_bal,0)) as db_end_C"
    	      +" from act_debt"
    	      +" where locate(acct_code,'AF|LF|CF|PF|BL|CA|ID|IT|RI|PN|AO|AI|SF|DP|CB|CI|CC|DB|OT') >0"
    	      +" and decode(curr_code,'','901',curr_code) =?"
    	      ;

    busi.DataSet idsDebt = new busi.DataSet();
    // --get billed_end_bal_XX--
    String lsThisMm = wp.itemStr2("ex_this_mm");
    if (!empty(lsThisMm)) {
      sql1 = sqlDebt+
      " and p_seqno =? and acct_month <=?"+
      " group by acct_code";
      setString2(1, aCurr);
      setString(lsPseqno);
      setString(lsThisMm);

      idsDebt.colList = sqlQuery(sql1);
      while (idsDebt.listNext()) {
        String lsCode = idsDebt.colStr("acct_code");
        String lsCol = "billed_end_bal_" + lsCode;
        wp.colSet(lsCol, idsDebt.colStr("db_endbal"));
        if (eq(lsCode,"DB")) {
//         	wp.col_set("billed_beg_bal_db_b",ids_debt.colStr("db_beg_B"));
				wp.colSet("billed_end_bal_db_b",idsDebt.colStr("db_end_B"));
//				wp.col_set("billed_beg_bal_db_i",ids_debt.colStr("db_beg_I"));
				wp.colSet("billed_end_bal_db_i",idsDebt.colStr("db_end_I"));
//				wp.col_set("billed_beg_bal_db_c",ids_debt.colStr("db_beg_C"));
				wp.colSet("billed_end_bal_db_c",idsDebt.colStr("db_end_C"));
			}
      }
    }

    // --Set unbilled_end_bal_XX--
    idsDebt.dataClear();
    String lsNextMm = wp.itemStr2("ex_next_mm");
    if (!empty(lsNextMm)) {
      sql1 = sqlDebt+ 
      " and p_seqno =? and acct_month =?"+
      " group by acct_code";
      setString(1,aCurr);
      setString(lsPseqno);
      setString(lsNextMm);
      idsDebt.colList = sqlQuery(sql1);
      while (idsDebt.listNext()) {
        String lsCode = idsDebt.colStr("acct_code");
        wp.colSet("unbill_end_bal_" + lsCode, idsDebt.colStr("db_endbal"));
        wp.colSet("unbill_beg_bal_" + lsCode, idsDebt.colStr("db_begbal"));
        if (eq(lsCode,"DB")) {
			wp.colSet("unbill_beg_bal_db_b",idsDebt.colStr("db_beg_B"));
			wp.colSet("unbill_end_bal_db_b",idsDebt.colStr("db_end_B"));
			wp.colSet("unbill_beg_bal_db_i",idsDebt.colStr("db_beg_I"));
			wp.colSet("unbill_end_bal_db_i",idsDebt.colStr("db_end_I"));
			wp.colSet("unbill_beg_bal_db_c",idsDebt.colStr("db_beg_C"));
			wp.colSet("unbill_end_bal_db_c",idsDebt.colStr("db_end_C"));
		}
      }
    }

    // --Set unbilled/billed_beg_bal_XX--
    String lsLastMm = wp.itemStr2("ex_last_mm");
    if (!empty(lsLastMm)) {
      strSql ="select nvl(billed_end_bal_af,0) + nvl(unbill_end_bal_af,0) as bal_af,"
      +" nvl(billed_end_bal_lf,0) + nvl(unbill_end_bal_lf,0) as bal_lf,"
      +" nvl(billed_end_bal_pf,0) + nvl(unbill_end_bal_pf,0) as bal_pf,"
      +" nvl(billed_end_bal_bl,0) + nvl(unbill_end_bal_bl,0) as bal_bl,"
      +" nvl(billed_end_bal_ca,0) + nvl(unbill_end_bal_ca,0) as bal_ca,"
      +" nvl(billed_end_bal_it,0) + nvl(unbill_end_bal_it,0) as bal_it,"
      +" nvl(billed_end_bal_id,0) + nvl(unbill_end_bal_id,0) as bal_id,"
      +" nvl(billed_end_bal_ri,0) + nvl(unbill_end_bal_ri,0) as bal_ri,"
      +" nvl(billed_end_bal_pn,0) + nvl(unbill_end_bal_pn,0) as bal_pn,"
      +" nvl(billed_end_bal_ao,0) + nvl(unbill_end_bal_ao,0) as bal_ao,"
      +" nvl(billed_end_bal_ai,0) + nvl(unbill_end_bal_ai,0) as bal_ai,"
      +" nvl(billed_end_bal_sf,0) + nvl(unbill_end_bal_sf,0) as bal_sf,"
      +" nvl(billed_end_bal_dp,0) + nvl(unbill_end_bal_dp,0) as bal_dp,"
      +" nvl(billed_end_bal_cb,0) + nvl(unbill_end_bal_cb,0) as bal_cb,"
      +" nvl(billed_end_bal_ci,0) + nvl(unbill_end_bal_ci,0) as bal_ci,"
      +" nvl(billed_end_bal_cf,0) + nvl(unbill_end_bal_cf,0) as bal_cf,"
      +" nvl(billed_end_bal_db,0) + nvl(unbill_end_bal_db,0) as bal_db,"
      +" nvl(billed_end_bal_cc,0) + nvl(unbill_end_bal_cc,0) as bal_cc,"
//         +" nvl(billed_end_bal_db_b,0) + nvl(unbill_end_bal_db_b,0) as bal_dbb,"
//         +" nvl(billed_end_bal_db_i,0) + nvl(unbill_end_bal_db_i,0) as bal_dbi,"
//         +" nvl(billed_end_bal_db_c,0) + nvl(unbill_end_bal_db_c,0) as bal_dbc,"
      +" nvl(billed_end_bal_ot,0)+nvl(unbill_end_bal_ot,0) as bal_ot"
      +" FROM act_curr_hst"
      +" WHERE p_seqno = ?" //:ls_p_seqno
      +" and acct_month =? and curr_code =?" //:is_last_mm
   ;
      setString2(1, lsPseqno);
      setString2(2, lsLastMm);
      setString(aCurr);

      sqlSelect(strSql);
      if (sqlRowNum <= 0)
        return 1;
        wp.colSet("billed_beg_bal_af", colStr("bal_af"));
        wp.colSet("billed_beg_bal_lf", colStr("bal_lf"));
        wp.colSet("billed_beg_bal_pf", colStr("bal_pf"));
        wp.colSet("billed_beg_bal_bl", colStr("bal_bl"));
        wp.colSet("billed_beg_bal_ca", colStr("bal_ca"));
        wp.colSet("billed_beg_bal_it", colStr("bal_it"));
        wp.colSet("billed_beg_bal_id", colStr("bal_id"));
        wp.colSet("billed_beg_bal_ri", colStr("bal_ri"));
        wp.colSet("billed_beg_bal_pn", colStr("bal_pn"));
        wp.colSet("billed_beg_bal_ao", colStr("bal_ao"));
        wp.colSet("billed_beg_bal_ai", colStr("bal_ai"));
        wp.colSet("billed_beg_bal_sf", colStr("bal_sf"));
        wp.colSet("billed_beg_bal_dp", colStr("bal_dp"));
        wp.colSet("billed_beg_bal_cb", colStr("bal_cb"));
        wp.colSet("billed_beg_bal_ci", colStr("bal_ci"));
        wp.colSet("billed_beg_bal_cf", colStr("bal_cf"));
        wp.colSet("billed_beg_bal_db", colStr("bal_db"));
        wp.colSet("billed_beg_bal_cc", colStr("bal_cc"));
        wp.colSet("billed_beg_bal_ot", colStr("bal_ot"));
  
        //--
      if (eq(aCurr,"901")) {
    	  strSql ="select "
					+" nvl(billed_end_bal_db_b,0) + nvl(unbill_end_bal_db_b,0) as bal_dbb,"
       +" nvl(billed_end_bal_db_i,0) + nvl(unbill_end_bal_db_i,0) as bal_dbi,"
       +" nvl(billed_end_bal_db_c,0) + nvl(unbill_end_bal_db_c,0) as bal_dbc "
					+" from act_acct_hst"
					+" WHERE p_seqno = ?" //:ls_p_seqno
					+" and acct_month =?" //:is_last_mm
			;
    	  setString2(1, lsPseqno);
          setString2(2, lsLastMm);
			sqlSelect(strSql);
			if (sqlRowNum >0) {
				wp.colSet("billed_beg_bal_db_b", colStr("bal_dbb"));
				wp.colSet("billed_beg_bal_db_i", colStr("bal_dbi"));
				wp.colSet("billed_beg_bal_db_c", colStr("bal_dbc"));
			}
		}

    }

    return 1;
  }


}
