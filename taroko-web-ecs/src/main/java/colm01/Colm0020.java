/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-16  V1.00.01  Ryan       Initial                                  *
* 112-04-26  V1.00.02  Ryan       修正mos_time,mod_user 新增mod_pgm                                  *
* 112-04-26  V1.00.02  Ryan       移除button disable控制                                                                                   *
* 112-10-04  V1.00.03  Ryan       增加總結算利息欄位                                                          *
* 112-10-12  V1.00.04  Sunny      增加線上覆核檢查判斷                                                       *
* 112-10-13  V1.00.04  Ryan       因為影響覆核權限，重新命名mod_pgm為mod_pgm1                                         *
***************************************************************************/
package colm01;

import java.math.BigDecimal;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm0020 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	ColFunc colFunc = new ColFunc();

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;
		
		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {/* 清畫面 */
			getAfterTransactionAmt();
		}

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}
	
	private boolean getWhereStr() throws Exception {
	    wp.whereStr = " where 1=1 ";
	    String exIdCorpNo = wp.itemStr("ex_id_corp_no");
	    String exAcctType = wp.itemStr("ex_acct_type");
	    String exTransDate1 = wp.itemStr("ex_trans_date1");
	    String exTransDate2 = wp.itemStr("ex_trans_date2");
	    String exTrialDate = wp.itemStr("ex_trial_date");
	    String exTrialRate = wp.itemStr("ex_trial_rate");
	    if (this.chkStrend(exTransDate1, exTransDate2) == false) {
	        alertErr("[日期區間-起迄]  輸入錯誤");
	        return false;
	      }
	    if(!empty(exIdCorpNo)&&exIdCorpNo.length()!=10 
	    		&&exIdCorpNo.length()!=8
	    		&&exIdCorpNo.length()!=11) {
	    	   alertErr("正卡人身份證/公司統編  輸入錯誤");
		       return false;
	    }
	    
	    if(!empty(exTrialDate)&&empty(exIdCorpNo)) {
	    	 alertErr("利息計算截止日有值時，正卡人身份證/公司統編不可空白");
		     return false;
	    }
	    
	    if(!empty(exTrialRate)&&(empty(exIdCorpNo)||empty(exTrialDate))) {
	    	 alertErr("結算年利率有值時，正卡人身份證/公司統編 或 利息計算截止日 不可空白");
		     return false;
	    }
	    
	    if (!empty(exIdCorpNo)) {
	      String idCorpSeqno = getIdCorpSeqno(exIdCorpNo);
	      if(idCorpSeqno.equals("error"))  return false;
	      wp.whereStr += " and  a.id_corp_p_seqno = :idCorpSeqno ";
	      setString("idCorpSeqno", idCorpSeqno);
	    }
	    if (!empty(exAcctType)) {
		    wp.whereStr += " and  b.acct_type = :exAcctType ";
		    setString("exAcctType", exAcctType);
		}
	    if (!empty(exTransDate1)) {
		    wp.whereStr += " and  a.trans_date >= :exTransDate1 ";
		    setString("exTransDate1", exTransDate1);
		}
	    if (!empty(exTransDate2)) {
		    wp.whereStr += " and  a.trans_date <= :exTransDate2 ";
		    setString("exTransDate2", exTransDate2);
		}
	    return true;
	  }
	
	String getIdCorpSeqno(String idCorpNo){
		String sqlSelect = "";
		if(idCorpNo.length()==10)
			sqlSelect = "select id_p_seqno as id_corp_seqno from crd_idno where id_no = :idCorpNo ";
		else
			sqlSelect = "select corp_p_seqno as id_corp_seqno from crd_corp where corp_no = :idCorpNo ";
		setString("idCorpNo",idCorpNo);
		sqlSelect(sqlSelect);
		if(sqlRowNum<=0) {
			alertErr("正卡人身份證/公司統編 查無資料");
			return "error";
		}
		return sqlStr("id_corp_seqno");
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {
		if(!getWhereStr()) 
			return;
		
		wp.pageControl();

		wp.selectSQL = "hex(a.rowid) as rowid "
				+ ", a.id_corp_p_seqno" 
				+ ", a.trans_type" 
				+ ", a.trans_date" 
				+ ", a.org_trans_date" 
				+ ", a.last_pay_date"
				+ ", a.int_rate_day" 
				+ ", a.issue_bank_no"
				;
		wp.daoTable = "col_bad_debt_ext a  ";
		wp.whereOrder = " ";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setPageValue();
		listWkdata();
	}
	
	void listWkdata() throws Exception {
		String sqlSelect = "";
		double trialAi = 0;
		int countDays = 0;

		double exTrialRate = wp.itemNum("ex_trial_rate");
		String exTrialDate = wp.itemStr("ex_trial_date");
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect = "select 'IDNO' as id_corp_type ,id_no as id_corp_no from crd_idno where id_p_seqno = :id_corp_p_seqno ";
			sqlSelect += " union ";
			sqlSelect += " select 'CORP' as id_corp_type ,corp_no as id_corp_no from crd_corp where corp_p_seqno = :id_corp_p_seqno ";
			setString("id_corp_p_seqno",wp.colStr(ii,"id_corp_p_seqno"));
			sqlSelect(sqlSelect);
			String idCorpType = sqlStr("id_corp_type");
			String idCorpNo = sqlStr("id_corp_no");
			wp.colSet(ii, "id_corp_no",idCorpNo);
				
			selectColBadDetailExt(ii,idCorpType);
			
			wp.colSet(ii,"tt_acct_type",idCorpType.equals("IDNO")?"01.一般卡":"03.商務卡");
			wp.colSet(ii,"acct_type",idCorpType.equals("IDNO")?"01":"03");
			
			String transType = wp.colStr(ii, "trans_type");
			String[] cde = new String[] { "1","2","3", "4", };
			String[] txt = new String[] { "1.正常", "2.逾放","3.催收", "4.呆帳" };
			wp.colSet(ii, "tt_trans_type", commString.decode(transType, cde, txt));
			
			countDays = 0;
			if(!empty(exTrialDate)) {
				if(!wp.colEmpty(ii,"last_pay_date"))
					countDays = comm.datePeriod(wp.colStr(ii,"last_pay_date"),exTrialDate);
				
				if(exTrialRate == 0) {
					trialAi = colFunc.getDayRateInterest(wp.colNum(ii,"end_bal_cb"),wp.colNum(ii,"int_rate_day"), countDays);
				}else{
					trialAi = colFunc.getYearRateInterest(wp.colNum(ii,"end_bal_cb"),exTrialRate , countDays);
				}
				wp.colSet(ii,"trial_ai",trialAi);	
			}else {
				wp.colSet(ii,"trial_ai","");	
			}
			
			double endBalTotal2 = numAdd(wp.colNum(ii,"trial_ai"),wp.colNum(ii,"end_bal_total"));
			wp.colSet(ii,"end_bal_total2",endBalTotal2);
		}
	}

   Double numAdd(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}
	
	void selectColBadDetailExt(int ii ,String idCorpType){
		StringBuffer strBuf = new StringBuffer();
		String idCorpSeqno= " id_corp_p_seqno = ";

		/*期末金額*/
		//total
		strBuf.append("select (select sum(end_bal) from col_bad_detail_ext where ");
		//strBuf.append(", (select (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(") as end_bal_total");
		//CB
		strBuf.append(", (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(" and ACCT_CODE = 'CB' ");
		strBuf.append(") as end_bal_cb");
		//CI
		strBuf.append(", (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(" and ACCT_CODE = 'CI' ");
		strBuf.append(") as end_bal_ci");
		//CC
		strBuf.append(", (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(" and ACCT_CODE = 'CC' ");
		strBuf.append(") as end_bal_cc");
		//SF
		strBuf.append(", (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(" and ACCT_CODE = 'SF' ");
		strBuf.append(") as end_bal_sf");
		//AI
		strBuf.append(", (select sum(end_bal) from col_bad_detail_ext where ");
		strBuf.append(idCorpSeqno);
		strBuf.append(" :id_corp_p_seqno ");
		strBuf.append(" and ACCT_CODE = 'AI' ");
		strBuf.append(") as end_bal_ai");
		
		if(strAction.equals("S")) {
			/*期初金額*/
			//total
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(") as beg_bal_total");
			//CB
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(" and ACCT_CODE = 'CB' ");
			strBuf.append(") as beg_bal_cb");
			//CI
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(" and ACCT_CODE = 'CI' ");
			strBuf.append(") as beg_bal_ci");
			//CC
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(" and ACCT_CODE = 'CC' ");
			strBuf.append(") as beg_bal_cc");
			//SF
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(" and ACCT_CODE = 'SF' ");
			strBuf.append(") as beg_bal_sf");
			//AI
			strBuf.append(", (select sum(beg_bal) from col_bad_detail_ext where ");
			strBuf.append(idCorpSeqno);
			strBuf.append(" :id_corp_p_seqno ");
			strBuf.append(" and ACCT_CODE = 'AI' ");
			strBuf.append(") as beg_bal_ai");
			//註解
//			strBuf.append(", (select adj_comment from col_bad_jrnl_ext where ");
//			strBuf.append("IDNO".equals(idCorpType)?"id_p_seqno = ":"corp_p_seqno = ");
//			strBuf.append(" :id_corp_p_seqno ");
//			strBuf.append(" order by crt_date||crt_time desc fetch first 1 rows only ");
//			strBuf.append(") as adj_comment");
			
			String sqlCmd = "select to_char(mod_time,'yyyymmdd') as mod_date ,mod_user,mod_pgm as mod_pgm1 "
					+ " from col_bad_detail_ext where id_corp_p_seqno = :id_corp_p_seqno "
					+ " order by mod_time desc fetch first 1 rows only ";
			setString("id_corp_p_seqno",wp.colStr(ii,"id_corp_p_seqno"));
			sqlSelect(sqlCmd);
		}
		strBuf.append(" from dual ");
		
		String sqlSelect = strBuf.toString();
		setString("id_corp_p_seqno",wp.colStr(ii,"id_corp_p_seqno"));
		sqlSelect(sqlSelect);
		wp.colSet(ii,"beg_bal_total", sqlNum("beg_bal_total"));
		wp.colSet(ii,"beg_bal_cb", sqlNum("beg_bal_cb"));
		wp.colSet(ii,"beg_bal_ci", sqlNum("beg_bal_ci"));
		wp.colSet(ii,"beg_bal_cc", sqlNum("beg_bal_cc"));
		wp.colSet(ii,"beg_bal_sf", sqlNum("beg_bal_sf"));
		wp.colSet(ii,"beg_bal_ai", sqlNum("beg_bal_ai"));
		wp.colSet(ii,"end_bal_total", sqlNum("end_bal_total"));
		wp.colSet(ii,"end_bal_cb", sqlNum("end_bal_cb"));
		wp.colSet(ii,"end_bal_ci", sqlNum("end_bal_ci"));
		wp.colSet(ii,"end_bal_cc", sqlNum("end_bal_cc"));
		wp.colSet(ii,"end_bal_sf", sqlNum("end_bal_sf"));
		wp.colSet(ii,"end_bal_ai", sqlNum("end_bal_ai"));
//		wp.colSet(ii,"adj_comment", sqlStr("adj_comment"));
		wp.colSet(ii,"mod_date", sqlStr("mod_date"));
		wp.colSet(ii,"mod_user", sqlStr("mod_user"));
		wp.colSet(ii,"mod_pgm1", sqlStr("mod_pgm1"));
	}
	
	void getAfterTransactionAmt() throws Exception{
		double transactionAmt = wp.itemNum("transaction_amt");
		String adjReasonCode = wp.itemStr("adj_reason_code");
		String acctCode = wp.itemStr("acct_code");
		String endBal = "END_BAL_";
		
		double afterTransactionAmt = 0;
		double afterTransactionTotal = 0;
		if(empty(adjReasonCode)) {
			return;
		}

		if(!empty(acctCode)) {
			endBal += acctCode;
		}
		
		if(adjReasonCode.equals("01")) {
			afterTransactionAmt = colFunc.numAdd(wp.itemNum(endBal), transactionAmt);
			afterTransactionTotal = colFunc.numAdd(wp.itemNum("end_bal_total"), transactionAmt);
		}else{
			afterTransactionAmt = colFunc.numSub(wp.itemNum(endBal), transactionAmt);
			afterTransactionTotal = colFunc.numSub(wp.itemNum("end_bal_total"), transactionAmt);
		}
		wp.addJSON("after_transaction_amt",this.commString.numFormat(afterTransactionAmt, "###,###.##"));
		wp.addJSON("after_transaction_total",this.commString.numFormat(afterTransactionTotal, "###,###.##"));
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		
		String dataK1 = itemKk("data_k1");
		if(empty(dataK1)) {
			dataK1 =  wp.itemStr("id_corp_p_seqno");
		}
		
		wp.selectSQL = " hex(a.rowid) as rowid,a.id_corp_p_seqno,a.trans_type,a.trans_date,a.org_trans_date,a.last_pay_date,a.int_rate_day,a.issue_bank_no"
				      + ",to_char(a.crt_time,'yyyymmdd') as crt_date,a.crt_user ";
		wp.daoTable = "col_bad_debt_ext a ";
		wp.whereStr = "where 1=1 ";
		wp.whereStr += sqlCol(dataK1, "a.id_corp_p_seqno");

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + dataK1 + "]");
			return;
		}
		listWkdata();
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		colm01.Colm0020Func func = new colm01.Colm0020Func(wp);
		func.setConn(wp);
		
		if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
		   return;
		}		
		
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
		if(rc == 1 ) {
			strAction = "S";
			dataRead();
		}
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
//			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {

	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************

} // End of class
