package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr2420 extends BaseAction implements InfacePdf {

	@Override
	public void userAction() throws Exception {
		switch (wp.buttonCode) {
	      case "X":
	        /* 轉換顯示畫面 */
	        strAction = "new";
	        clearFunc();
	        break;
	      case "Q":
	        /* 查詢功能 */
	        strAction = "Q";
	        queryFunc();
	        break;
	      case "R":
	        // -資料讀取-
	        strAction = "R";
	        dataRead();
	        break;
	      case "A":
	        /* 新增功能 */
	        saveFunc();
	        break;
	      case "U":
	        /* 更新功能 */
	        saveFunc();
	        break;
	      case "D":
	        /* 刪除功能 */
	        saveFunc();
	        break;
	      case "M":
	        /* 瀏覽功能 :skip-page */
	        queryRead();
	        break;
	      case "S":
	        /* 動態查詢 */
	        querySelect();
	        break;
	      case "L":
	        /* 清畫面 */
	        strAction = "";
	        clearFunc();
	        break;
	      case "C":
	        // -資料處理-
	        procFunc();
	        break;	      
	      case "PDF":
	    	// -資料處理-
	    	pdfPrint();
		    break;  
	      default:
	        break;
	    }

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		
		String lsWhere = " where 1=1 and A.corp_p_seqno <> '' "
					   +sqlCol(wp.itemStr("ex_user_bank"),"A.reg_bank_no")		
					   +sqlCol(wp.itemStr("ex_corp_no"),"A.corp_no")
					   ;
				
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		
		wp.selectSQL = " A.corp_no , A.corp_p_seqno , A.card_no , uf_card_name(A.card_no) as chi_name , A.activate_date , decode(A.current_code,'0','*','') as tt_current_code , A.oppost_reason , A.new_end_date ,"
					 + " B.line_of_credit_amt , B.int_rate_mcode , B.acno_p_seqno ";
		wp.daoTable = " crd_card A join act_acno B on A.acno_p_seqno = B.acno_p_seqno ";
		wp.whereOrder = " order by A.corp_p_seqno ";
		
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}
		
		wp.setListCount(0);
		wp.setPageValue();
		queryAfter();
	}
	
	void queryAfter() throws Exception {
		String corpNo = "", sysMonth = "" , officeTel = "" , corpName = "" , corpAcnoPSeqno = "";
		double corpLineOfCreditAmt = 0.0 , totDue = 0.0 , totLimitAmt = 0.0 ;
		sysMonth = getSysDate().substring(0,6)+"%";
		String sql1 = "select sum(nt_amt) as month_consume from cca_auth_txlog where cacu_amount in ('Y','M') and tx_date like ? ";
		String sql2 = "select acct_jrnl_bal from act_acct where p_seqno = ? ";
		String sql3 = "select line_of_credit_amt as corp_line_of_credit_amt , acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='2' ";
		String sql4 = "select chi_name , corp_tel_zone1||corp_tel_no1||corp_tel_ext1 as office_tel from crd_corp where corp_no = ? ";
		String sql5 = "select B.tot_due , B.tot_limit_amt from cca_card_acct A join cca_consume B on A.card_acct_idx = B.card_acct_idx where acno_p_seqno = ? ";
		for(int ii=0;ii<wp.selectCnt;ii++) {
			wp.colSet(ii, "card_no_6",wp.colStr(ii,"card_no").substring(wp.colStr(ii,"card_no").length()-6, wp.colStr(ii,"card_no").length()));
			if(wp.colStr(ii,"new_end_date").length() == 8)
				wp.colSet(ii, "new_end_date",wp.colStr(ii,"new_end_date").substring(4,6)+"/"+wp.colStr(ii,"new_end_date").substring(2,4));
			//--查詢月消費
			sqlSelect(sql1,new Object[] {sysMonth});			
			//--現欠
			sqlSelect(sql2,new Object[] {wp.colStr(ii,"acno_p_seqno")});
			//--公司戶電話、公司戶總額度
			corpNo = wp.colStr(ii,"corp_no");
			if(ii==0 || corpNo.equals(wp.colStr((ii-1),"corp_no")) == false) {
				corpLineOfCreditAmt = 0;
				officeTel = "";
				totLimitAmt = 0;
				totDue = 0;
				sqlSelect(sql3,new Object[] {wp.colStr(ii,"corp_p_seqno")});
				if(sqlRowNum > 0) {
					corpLineOfCreditAmt = sqlNum("corp_line_of_credit_amt");
					corpAcnoPSeqno = sqlStr("corp_acno_p_seqno");
				}					
				
				sqlSelect(sql4,new Object[] {corpNo});
				if(sqlRowNum > 0) {
					officeTel = sqlStr("office_tel");
					corpName = sqlStr("chi_name");
				}
								
				sqlSelect(sql5,new Object[] {corpAcnoPSeqno});
				if(sqlRowNum > 0) {
					totDue = sqlNum("tot_due");
					totLimitAmt = sqlNum("tot_limit_amt");
					wp.colSet(ii, "tl_last_consume",totLimitAmt);
					wp.colSet(ii, "tl_this_consume",totDue);					
					wp.colSet(ii, "last_pct","");
				}
				if((totDue - totLimitAmt)>600000)
					wp.colSet(ii, "over_sixty_flag","Y");
				else
					wp.colSet(ii, "over_sixty_flag"," ");
			}
			
			wp.colSet(ii, "month_consume", sqlNum("month_consume"));
			wp.colSet(ii, "acct_jrnl_bal", sqlNum("acct_jrnl_bal"));
			wp.colSet(ii, "corp_line_of_credit_amt",corpLineOfCreditAmt);
			wp.colSet(ii, "office_tel",officeTel);	
			wp.colSet(ii, "corp_name",corpName);
		}
		
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
		if(wp.itemEmpty("ex_user_bank_name"))
			getUserBankNo();

	}
	
	void getUserBankNo() {
		String bankUnitNo = "" , bankName = "";
		
		String sql1 = " select bank_unitno from sec_user where 1=1 " +sqlCol(wp.loginUser,"usr_id");
		sqlSelect(sql1);
		
		bankUnitNo = sqlStr("bank_unitno");
		
		if(bankUnitNo.isEmpty())
			return ;
		
		String sql2 = " select full_chi_name from gen_brn where 1=1 " +sqlCol(bankUnitNo,"branch");
		sqlSelect(sql2);
		
		bankName = sqlStr("full_chi_name");
		
		wp.colSet("ex_user_bank", bankUnitNo);
		wp.colSet("ex_user_bank_name", bankName);				
	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "Rskr2420";
	    wp.pageRows = 9999;
//	    String cond1;	    
//	    wp.colSet("cond1", cond1);
	    queryFunc();
	    TarokoPDF pdf = new TarokoPDF();
	    wp.fileMode = "Y";
	    pdf.excelTemplate = "rskr2420.xlsx";
	    pdf.pageCount = 30;
	    pdf.sheetNo = 0;
	    pdf.procesPDFreport(wp);
	    pdf = null;
	    return;
		
	}
	
}
