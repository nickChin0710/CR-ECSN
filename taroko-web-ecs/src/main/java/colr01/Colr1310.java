/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/20  V1.00.00   phopho     program initial                           *
*  109-05-06  V1.00.01   Tanwei       updated for project coding standard     *
*  109-06-04  V1.00.02   shiyuqi       修改无意义命名                                                                                     
*  109/07/11  V1.00.01   phopho     Mantis 0003716: fix cb cc ci nego_status  *
*  109/07/24  V1.00.02   phopho     add nego_type, nego_status                *
*  109/10/29  V1.00.03   chilai     Mantis 0004534，修改帶入報表檔案的查詢條件內容      *
*  110/03/03  V1.00.04   richard    modify PDF隠碼						      *
*  112/02/02  V1.00.05   Zuwei Su   update nameing rules, remark field col_bad_debt.nego_type,col_bad_debt.nego_status in sql  *
*  112/12/05  V1.00.06   Sunny      調整colr1310.xlsx的ID欄位，以明碼顯示，不隱暱      *
*  112/12/05  V1.00.07   Sunny      增加列表顯示欄位，公司統編，若為公司則出現公司名稱。
******************************************************************************/

package colr01;

import java.util.ArrayList;
import java.util.HashMap;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr1310 extends BaseReport {
    CommString commString = new CommString();
	String mProgName = "colr1310";
	String strCorpChiName = "";
	String strChiName = "";
	String strCardSince = "";
	double dCbAmtAcct=0;
	double dCiAmtAcct=0;
	double dCcAmtAcct=0;
	double hDebtIdTotalAmt=0;
	ArrayList<String> list1 = new ArrayList<String>();
	ArrayList<String> list2 = new ArrayList<String>();
	HashMap<String,String> map = new HashMap<String,String>();
	String wfValue = "";
	String wfValue2 = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			//dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			// is_action = "R";
			// dataRead();
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			pdfPrint();
		}

		//dddw_select();
		initButton();
	}
	
	private boolean getWhereStr() throws Exception {
		if(empty(wp.itemStr("exTransYymm")) && empty(wp.itemStr("exStmtCycle"))
			&& empty(wp.itemStr("exId")) && empty(wp.itemStr("exCorpNo"))) {
			alertErr("請輸入查詢條件");
			return false;
		}

		wp.whereStr = " where 1=1 "
				+ "and col_bad_debt.p_seqno    = col_bad_detail.p_seqno "
				+ "and col_bad_debt.trans_date = col_bad_detail.trans_date "
				+ "and col_bad_debt.trans_type = col_bad_detail.trans_type "
				+ "and col_bad_debt.trans_type = '4' "
				+ "and col_bad_debt.id_p_seqno = crd_idno.id_p_seqno ";
	
		if(empty(wp.itemStr("exTransYymm")) == false){
			wp.whereStr += " and substr(col_bad_debt.trans_date,1,6) = :trans_date ";
			setString("trans_date", wp.itemStr("exTransYymm"));
		}
		if (empty(wp.itemStr("exId")) == false) {
			wp.whereStr += " and crd_idno.id_no = :id_no ";
			setString("id_no", wp.itemStr("exId"));
		}
		
		if (empty(wp.itemStr("exCorpNo")) == false) {
			wp.whereStr += " and crd_corp.corp_no = :corp_no ";
			setString("corp_no", wp.itemStr("exCorpNo"));
		}
		
		if (empty(wp.itemStr("exStmtCycle")) == false) {
			wp.whereStr += " and col_bad_debt.stmt_cycle = :stmt_cycle ";
			setString("stmt_cycle", wp.itemStr("exStmtCycle"));
		}
		
		//-page control-
		wp.queryWhere = wp.whereStr;
	
		return true;
	}
	
	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		
		if (getWhereStr() == false)
			return;

		//2020.7.24 phopho add nego_type, nego_status
		wp.selectSQL = " col_bad_debt.id_p_seqno, " 
				 + " crd_idno.id_no, "
				 + " crd_corp.corp_no, "
				 + " decode(col_bad_debt.acct_type,'01',crd_idno.chi_name,crd_corp.chi_name) as chi_name, "
//				 + " uf_hi_idno(crd_idno.id_no) id_no_hi, " 
				 + " col_bad_debt.acct_type, "
				 + " col_bad_debt.p_seqno, "
				 + " crd_corp.corp_p_seqno, "
//				 + " col_bad_debt.nego_type, "
//				 + " col_bad_debt.nego_status, "
				 + " max(col_bad_debt.trans_date) trans_date4, " //改帶最早轉呆日期
				 + " sum(col_bad_detail.end_bal) sum_end_bal ";
		
//		wp.daoTable = " col_bad_debt, col_bad_detail, crd_idno ";
		wp.daoTable = " col_bad_debt, col_bad_detail, crd_idno "
				    + "left join crd_corp on col_bad_debt.corp_p_seqno=crd_corp.corp_p_seqno";
		
//		wp.whereOrder = " group by col_bad_debt.id_p_seqno, crd_idno.id_no, col_bad_debt.acct_type, "			
		wp.whereOrder = " group by col_bad_debt.id_p_seqno, crd_idno.id_no, "				
				+ "crd_corp.corp_no,crd_corp.corp_p_seqno, col_bad_debt.acct_type, "
				+ "col_bad_debt.p_seqno, "
				+ "decode(col_bad_debt.acct_type,'01',crd_idno.chi_name,crd_corp.chi_name), "
		        +"'' "
//					+ "col_bad_debt.nego_type, "
//					+ "col_bad_debt.nego_status "
					+ " order by col_bad_debt.acct_type,crd_corp.corp_no,crd_idno.id_no,sum_end_bal desc ";
//		            + " order by col_bad_debt.acct_type,crd_idno.id_no,sum_end_bal desc ";
		//20231205 配合卡部調整排序方式，依ID排序
		
		wp.pageCountSql = "SELECT COUNT(*) FROM ( select "
				+ wp.selectSQL + " from " + wp.daoTable + " "
				+ wp.whereStr + " "
				+ wp.whereOrder
				+ " )"
				;
		
		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

//		getValue();  //2020.7.24 phopho 協商狀態參考colm0100
		listWkdata();
		wp.setPageValue();
	}

	void listWkdata() throws Exception {
		String ss = "";
		String ssCorp= "";
		String strTransDate3= "";
		String strDelinquentDate="";
		double ssSumEndBal = 0;
		double ssCorpSumEndBal = 0;
		double totalEndBal = 0;
		long ssCnt = 0;
		long ssCorpCnt = 0;
		long totalIdCnt = 0;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss =wp.colStr(ii,"id_p_seqno");
			{
			 strTransDate3=wfSelectTransDate3(ss);
			 strDelinquentDate=wfSelectActAcno(ss);
			 getEndBalbyIdPSeqno(ss);  //phopho add 2020.7.14
			}

			ssCorp =wp.colStr(ii,"corp_p_seqno");
			if(ssCorp.length()>0) {
			strTransDate3=wfSelectTransDateCorp3(ssCorp);
			strDelinquentDate=wfSelectActAcnoCorp(ssCorp);
			getEndBalbyCorpPSeqno(ssCorp);
			}
		
//			wp.colSet(ii,"id_no_hi", wp.colStr(ii,"id_no_hi"));
//			wp.colSet(ii,"chi_name", strChiName);
			wp.colSet(ii,"id_no", wp.colStr(ii,"id_no"));
			wp.colSet(ii,"corp_no", wp.colStr(ii,"corp_no"));
			wp.colSet(ii,"corp_p_seqno", wp.colStr(ii,"corp_p_seqno"));
			wp.colSet(ii,"chi_name", wp.colStr(ii,"chi_name"));
			wp.colSet(ii,"card_since", strCardSince);
//            wp.colSet(ii,"org_delinquent_date", wfSelectActAcno(ss));
			wp.colSet(ii,"org_delinquent_date", strDelinquentDate);
            wp.colSet(ii,"trans_date3", strTransDate3);	
//            wp.colSet(ii,"trans_date3", wfSelectTransDate3(ss));		
//			wp.col_set(ii,"nego_status", wf_GetNegoStatus(ss));
			//2020.7.24 phopho 協商狀態參考colm0100。程式勿用hot code。
			wp.colSet(ii,"tt_nego_status", wfGetPtrSysIdtab(ii));
			
			//2020.7.11 phopho Mantis 0003716 3.
			//  【id歸戶轉呆總額】，則是以id_no 為單位，做統計值。
			//   以上邏輯，應該與【ColB027 催收加速轉呆彙整倒檔作業】一致。
//			getActBadData(ss);
//			getEndBalbyIdPSeqno(ss);  //phopho add 2020.7.14
			wp.colSet(ii,"id_total_amt", hDebtIdTotalAmt+"");
			//2020.7.11 phopho Mantis 0003716 1.
			ss =wp.colStr(ii,"p_seqno");  //todo p_seqno or id_p_seqno?
			wfSelectColBadDetail(ss);
			wp.colSet(ii,"cb_amt_acct", dCbAmtAcct+"");
			wp.colSet(ii,"ci_amt_acct", dCiAmtAcct+"");
			wp.colSet(ii,"cc_amt_acct", dCcAmtAcct+"");

			if(ssCorp.length()>0) {
				ssCorpSumEndBal += wp.colNum(ii,"sum_end_bal");
				ssCorpCnt++;
			}
			else
			{
				ssSumEndBal += wp.colNum(ii,"sum_end_bal");
				ssCnt++;
			}
			
			totalEndBal += wp.colNum(ii,"sum_end_bal");
			totalIdCnt++;
		}

		wp.colSet("corp_sum_end_bal",ssCorpSumEndBal+"");
		wp.colSet("corp_cnt",ssCorpCnt+"");
		wp.colSet("id_sum_end_bal",ssSumEndBal+"");
		wp.colSet("id_cnt",ssCnt+"");
		
		wp.colSet("total_end_bal",totalEndBal+"");
		wp.colSet("total_id_cnt",totalIdCnt+"");
	}
	
	void wfSelectCrdIdno(String idpseqno) throws Exception {
		strChiName = "";
		strCardSince = "";
		String lsSql = "select chi_name, card_since from crd_idno "
					+ "where id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno", idpseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			strChiName = sqlStr("chi_name");
			strCardSince = sqlStr("card_since");
		}
		return;
	}
	
	void wfSelectCrdCorp(String corpPseqno) throws Exception {
		strCorpChiName = "";
		String lsSql = "select chi_name from crd_corp "
					+ "where corp_p_seqno = :corp_p_seqno ";
		setString("corp_p_seqno", corpPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			strCorpChiName = sqlStr("chi_name");
		}
		return;
	}
	
	//個人
	String wfSelectActAcno(String idpseqno) throws Exception {
		String rtn="";
		String lsSql = "select min(org_delinquent_date) org_delinquent_date from act_acno "
					+ "where id_p_seqno= :id_p_seqno ";
		setString("id_p_seqno", idpseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn=sqlStr("org_delinquent_date");
		}
		return rtn;
	}
	
	//公司
	String wfSelectActAcnoCorp(String corpPseqno) throws Exception {
		String rtn="";
		String lsSql = "select min(org_delinquent_date) org_delinquent_date from act_acno "
					+ "where corp_p_seqno= :corp_p_seqno ";
		setString("corp_p_seqno", corpPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn=sqlStr("org_delinquent_date");
		}
		return rtn;
	}
	
	//個人
	String wfSelectTransDate3(String idpseqno) throws Exception {
		String rtn="";
		String lsSql = "select min(trans_date) trans_date3 from col_bad_debt "
					+ "where id_p_seqno= :id_p_seqno and trans_type = '3' ";
		setString("id_p_seqno", idpseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn=sqlStr("trans_date3");
		}
		return rtn;
	}
	
	//公司
	String wfSelectTransDateCorp3(String corpPseqno) throws Exception {
		String rtn="";
		String lsSql = "select min(trans_date) trans_date3 from col_bad_debt "
					+ "where corp_p_seqno= :corp_p_seqno and trans_type = '3' ";
		setString("corp_p_seqno", corpPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn=sqlStr("trans_date3");
		}
		return rtn;
	}
	
//	a.	取得各個TABLE資料後，比對CRT_DATE欄位值，取得最新日期的資料種類(債務協商/前置協商/前置調解/清算/更生)。
//	若有CRT_DATE相同者，則比較MOD_TIME，取得MOD_TIME較新的該筆資料。
//	b.	解析該筆資料種類的狀態，回傳。
//	c.	回傳內容，【協商類別-協商狀態】，例如: 債務協商-協商成功。
	String wfGetNegoStatus(String idpseqno) throws Exception {
		String rtn="";
		String lsNegoType="", lsNegoStatus="";
//		String ls_sql = "select nego_type, nego_status from ( "
//				+ "select 'LIAB' nego_type, liab_status nego_status, crt_date, mod_time from COL_LIAB_NEGO where id_p_seqno = :id_p_seqnob "
//				+ "UNION "
//				+ "select 'LIAC' nego_type, liac_status nego_status, crt_date, mod_time from COL_LIAC_NEGO where id_p_seqno = :id_p_seqnoc "
//				+ "UNION "
//				+ "select 'LIAM' nego_type, liam_status nego_status, crt_date, mod_time from COL_LIAM_NEGO where id_p_seqno = :id_p_seqnom "
//				+ "UNION "
//				+ "select 'LIAD' nego_type, renew_status nego_status, crt_date, mod_time from COL_LIAD_RENEW where id_p_seqno = :id_p_seqnod "
//				+ "UNION "
//				+ "select 'LIAQ' nego_type, liqu_status nego_status, crt_date, mod_time from COL_LIAD_LIQUIDATE where id_p_seqno = :id_p_seqnoq "
//				+ ") tt "
//				+ "order by crt_date desc, mod_time desc "
//				+ "fetch first 1 row only ";
//		setString("id_p_seqnob", idpseqno);
//		setString("id_p_seqnoc", idpseqno);
//		setString("id_p_seqnom", idpseqno);
//		setString("id_p_seqnod", idpseqno);
//		setString("id_p_seqnoq", idpseqno);
//		sqlSelect(ls_sql);
//		if (sql_nrow <= 0) {
//			return rtn;
//		}
		
		//2020.7.11 phopho Mantis 0003716 2.
		//2. 更新【協商狀態】的邏輯，使與【ColB027 催收加速轉呆彙整倒檔作業】的【協商狀態】一致。
		String lsSql = "select "
//		        + "nego_type, nego_status, "
		        + "apply_nego_mcode ";
		lsSql += "from col_nego_status_curr WHERE id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno", idpseqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			return rtn;
		}
		
		lsNegoType = sqlStr("nego_type");
		lsNegoStatus = sqlStr("nego_status");
//		參數：
//		1. 債務協商
//		   1-停催,  2-復催, 3-協商成功, 4-結案,  else liab_status。
//		2. 前置協商
//		   1-受理申請, 2-停催, 3-簽約成功, 4-結案/復催, 5-結案/結清, else liab_status。
//		3. 前置調解
//		   1-受理申請, 3-簽約成功, 4-結案/復催, 5-結案/結清, 6-本行無債權, else liab_status。
//		4. 更生
//		   1-更生開始, 2-更生撤回, 3-更生認可, 4-更生履行完畢, 5-更生裁定免責, 6-更生調查程序, 7-更生駁回, else liab_status。
//		5. 清算
//		   1-清算程序開始, 2-清算程序終止, 3-清算程序開始同時終止, 4-清算撤銷免責, 5-清算調查程序, 6-清算駁回, 7-清算撤回, else liab_status。

		switch(lsNegoType){
		case "LIAB":
			rtn = "債務協商-";
			rtn += commString.decode(lsNegoStatus, ",1,2,3,4", ",停催,復催,協商成功,結案");
			break;
		case "LIAC":
			rtn = "前置協商-";
			rtn += commString.decode(lsNegoStatus, ",1,2,3,4,5", ",受理申請,停催,簽約成功,結案/復催,結案/結清");
			break;
		case "LIAM":
			rtn = "前置調解-";
			rtn += commString.decode(lsNegoStatus, ",1,3,4,5,6", ",受理申請,簽約成功,結案/復催,結案/結清,本行無債權");
			break;
		case "LIAD":
			rtn = "更生-";
			rtn += commString.decode(lsNegoStatus, ",1,2,3,4,5,6,7", ",更生開始,更生撤回,更生認可,更生履行完畢,更生裁定免責,更生調查程序,更生駁回");
			break;
		case "LIAQ":
			rtn = "清算-";
			rtn += commString.decode(lsNegoStatus, ",1,2,3,4,5,6,7", ",清算程序開始,清算程序終止,清算程序開始同時終止,清算撤銷免責,清算調查程序,清算駁回,清算撤回");
			break;
		}
		
		return rtn;
	}

	//2020.7.11 phopho Mantis 0003716 1.
	void wfSelectColBadDetail(String pseqno) throws Exception {
		dCbAmtAcct=0;
		dCiAmtAcct=0;
		dCcAmtAcct=0;
		String lsSql = "select sum(decode(acct_code,'CB',nvl(end_bal,0),0)) as cb_amt_acct, "
				+ "sum(decode(acct_code,'CI',nvl(end_bal,0),0)) as ci_amt_acct, "
				+ "sum(decode(acct_code,'CC',nvl(end_bal,0),0)) as cc_amt_acct "
				+ "from col_bad_detail "
				+ "where p_seqno = :p_seqno1 and trans_type = '4' "
				+ "  and trans_date = (Select max(trans_date) From col_bad_detail "
				+ "                 Where p_seqno = :p_seqno2 And trans_type = '4' )";
		setString("p_seqno1", pseqno);
		setString("p_seqno2", pseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			dCbAmtAcct = sqlNum("cb_amt_acct");
			dCiAmtAcct = sqlNum("ci_amt_acct");
			dCcAmtAcct = sqlNum("cc_amt_acct");
		}
		return;
	}
	
//	//2020.7.11 phopho Mantis 0003716 3. FROM ColB027
//	void getActBadData(String idpseqno) throws Exception {
//        h_debt_id_total_amt = 0;
//        
//        String ls_sql = "select sum(end_bal) tot_end_bal ";
//        ls_sql += "  from act_debt  ";
//        ls_sql += " where acct_code in ('CB','CI','CC')  ";
//        ls_sql += "   and p_seqno   in (select acno_p_seqno from act_acno where id_p_seqno = :id_p_seqno) ";
//        setString("id_p_seqno", idpseqno);
//        sqlSelect(ls_sql);
//        if (sql_nrow > 0) {
//        	h_debt_id_total_amt = sql_num("tot_end_bal");
//		} else {
//			ls_sql = "select sum(end_bal) tot_end_bal ";
//			ls_sql += "  from act_debt  ";
//			ls_sql += " where acct_code in ('CB','CI','CC')  ";
//			ls_sql += "   and p_seqno   in (select acno_p_seqno from act_acno where corp_p_seqno = :corp_p_seqno) ";
//			setString("corp_p_seqno", idpseqno);
//			sqlSelect(ls_sql);
//	        if (sql_nrow > 0) {
//	        	h_debt_id_total_amt = sql_num("tot_end_bal");
//			}
//		}
//    }
	
	//2020.7.11 phopho Mantis 0003716 3. FROM col_bad_detail
	void getEndBalbyIdPSeqno(String idpseqno) throws Exception {
        hDebtIdTotalAmt = 0;
        
        String lsSql = "select sum(col_bad_detail.end_bal) id_tot_end_bal ";
        lsSql += "  from col_bad_debt, col_bad_detail ";
        lsSql += " where col_bad_debt.p_seqno    = col_bad_detail.p_seqno ";
        lsSql += "   and col_bad_debt.trans_date = col_bad_detail.trans_date ";
        lsSql += "   and col_bad_debt.trans_type = col_bad_detail.trans_type ";
        lsSql += "   and col_bad_debt.trans_type = '4' ";
        lsSql += "   and col_bad_debt.id_p_seqno = :id_p_seqno ";
        setString("id_p_seqno", idpseqno);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
        	hDebtIdTotalAmt = sqlNum("id_tot_end_bal");
		}
    }
	
	//20231205 sunny add
	void getEndBalbyCorpPSeqno(String CorpPSeqno) throws Exception {
        hDebtIdTotalAmt = 0;
        
        String lsSql = "select sum(col_bad_detail.end_bal) id_tot_end_bal ";
        lsSql += "  from col_bad_debt, col_bad_detail ";
        lsSql += " where col_bad_debt.p_seqno    = col_bad_detail.p_seqno ";
        lsSql += "   and col_bad_debt.trans_date = col_bad_detail.trans_date ";
        lsSql += "   and col_bad_debt.trans_type = col_bad_detail.trans_type ";
        lsSql += "   and col_bad_debt.trans_type = '4' ";
        lsSql += "   and col_bad_debt.corp_p_seqno = :corp_p_seqno ";
        setString("corp_p_seqno", CorpPSeqno);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
        	hDebtIdTotalAmt = sqlNum("id_tot_end_bal");
		}
    }
	
	void getValue() throws Exception{
		//【nego_type】 的中文對應
		String sqlSelect = "select wf_value,wf_value2 from ptr_sys_parm "
				+ " where wf_parm = 'COL_PARM' "
				+ " and wf_key = 'COL_NEGO_TYPE_PARM' ";
		sqlSelect(sqlSelect);
		String[] ss1 = sqlStr("wf_value").split(",");
		String[] ss2 = sqlStr("wf_value2").split(",");

		for(int x = 0 ; x<ss1.length ; x++){
			wfValue += (","+ss1[x]);
			list1.add(ss1[x]);
		}
		for(int x = 0 ; x<ss2.length ; x++){
			wfValue2 += (","+ss2[x]);
		}
		
		//【nego_status】的中文對應
		sqlSelect = "select wf_value,wf_value2 from ptr_sys_parm "
				+ " where wf_parm = 'COL_PARM' "
				+ " and wf_key = 'COL_NEGO_STATUS_PARM' ";
		sqlSelect(sqlSelect);
		ss1 = sqlStr("wf_value").split(",");
		ss2 = sqlStr("wf_value2").split(",");

		for(int x = 0 ; x<ss1.length ; x++){
			list2.add(ss1[x]);
		}
		for(int x = 0 ; x<ss2.length ; x++){
			list2.add(ss2[x]);
		}		
		for(int x = 0 ; x<list1.size() ; x++){
			map.put(list1.get(x), list2.get(x));
		}
	}
	
	String wfGetPtrSysIdtab(int n) throws Exception {
		String negoType = wp.colStr(n,"nego_type");
		String negoStatus = wp.colStr(n,"nego_status");
		String negoTypeNegoStatus = "";
		
		negoTypeNegoStatus += commString.decode(negoType, wfValue , wfValue2);

		String sqlSelect = "select wf_id,wf_desc from PTR_SYS_IDTAB "
				+ " where wf_type = :wf_type ";
		setString("wf_type",map.get(negoType));
		sqlSelect(sqlSelect);
		String wfId = "";
		String wfDesc = "";
		for(int x = 0 ; x<sqlRowNum; x++){
			wfId += (","+sqlStr(x,"wf_id"));
			wfDesc += (","+sqlStr(x,"wf_id")+"."+sqlStr(x,"wf_desc"));
		}
		negoTypeNegoStatus += (!empty(negoStatus))?("-"+commString.decode(negoStatus, wfId , wfDesc)):"";
		
		return negoTypeNegoStatus;
	}

	@Override
	public void querySelect() throws Exception {

	}

	void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			// -cond-
			String ss = "轉呆月份: " + commString.strToYmd(wp.itemStr("exTransYymm"))
			       + "   結帳週期: " + wp.itemStr("exStmtCycle");
			wp.colSet("cond_1", ss);
			wp.colSet("reportName", mProgName.toUpperCase());
			wp.colSet("loginUser", wp.loginUser);
			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = mProgName + ".xlsx";
			// ====================================
			// -明細-
			xlsx.sheetName[0] = "明細";

			queryFunc();
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
			xlsx.processExcelSheet(wp);
			/*
			 * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where);
			 * wp.listCount[1] =sql_nrow; ddd("Summ: rowcnt:" +
			 * wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
			 */
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

	void pdfPrint() throws Exception {
		if (getWhereStr() == false) {
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		wp.reportId = mProgName;
		wp.pageRows =9999;
		
		String ss = "轉呆月份: " + commString.strToYmd(wp.itemStr("exTransYymm"))
	           + "   結帳週期: " + wp.itemStr("exStmtCycle");
		wp.colSet("cond_1", ss);
		wp.colSet("reportName", mProgName.toUpperCase());
		wp.colSet("loginUser", wp.loginUser);
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
