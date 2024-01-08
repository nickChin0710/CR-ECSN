
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-04  V1.00.01  Ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan    修改了變量名稱和方法名稱                                            *
* 109-12-23  V1.00.03  Justin     zz -> commString                           *
* 111-11-02  V1.00.04  JeffKung   顯示回覆原因對應的中文說明                                        *
******************************************************************************/

package dbar01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbar0030 extends BaseReport {
	String progName = "dbar0030";
	CommString commString = new CommString();
	int ii = 0;
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc=1;

		strAction = wp.buttonCode;
		//ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			//-資料處理-
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
		
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) { 
			//-資料讀取- 
		//	is_action = "R";
		//	dataRead();
		//} else if (eq_igno(wp.buttonCode, "A")) {
		//	/* 新增功能 */
		//	insertFunc();
		//} else if (eq_igno(wp.buttonCode, "U")) {
		//	/* 更新功能 */
		//	updateFunc();
		//} else if (eq_igno(wp.buttonCode, "D")) {
		//	/* 刪除功能 */
		//	deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page*/
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
			strAction = "XLS";
			xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
		   	strAction = "PDF";
		   	pdfPrint();
	    }

		dddwSelect();
		initButton();
	}
	
	@Override
	public void dddwSelect() {
	/*	try {
			wp.initOption = "--";
			wp.optionKey = wp.item_ss("ex_bno1");
			dddw_list("dddw_branch1","ptr_branch"
					, "branch", "branch_name", " where 1=1 order by branch");
			wp.optionKey = wp.item_ss("ex_bno2");
			dddw_list("dddw_branch2","ptr_branch"
					, "branch", "branch_name", " where 1=1 order by branch");
		}
		catch(Exception ex){}*/
	}
	@Override
	public void initPage(){

	}
	
	private int getWhereStr() throws Exception {
		StringBuffer  str  = new StringBuffer();
		String date1 = wp.itemStr("ex_crdate_s");
		String date2 = wp.itemStr("ex_crdate_e");
		if (this.chkStrend(date1, date2) == false) {
			alertErr2("[扣款日期-起迄]  輸入錯誤");
			return -1;
		}
		if( empty(wp.itemStr("ex_id"))&&
			    empty(wp.itemStr("ex_accno"))&&
			    empty(wp.itemStr("ex_cardno"))&&
			    empty(wp.itemStr("ex_type"))&&
			    empty(wp.itemStr("ex_crdate_s"))&&
			    empty(wp.itemStr("ex_crdate_e"))
			){
			    alertErr("至少輸入一個查詢條件");
			    return -1;
			}
	/*	if(f_auth_query_vd()!=1){
			return -1;
		}*/
		wp.whereStr = " where 1=1 and ( substr(a.adjust_type, 1,2) = 'DE' or a.adjust_type in ('RE20','DP01')) ";

		if(empty(wp.itemStr("ex_id")) == false){
			str.append(" and a.p_seqno in ( ");
			String sqlSelect="select p_seqno from dba_acno where acct_holder_id = :acct_holder_id ";
			setString("acct_holder_id",wp.itemStr("ex_id"));
			sqlSelect(sqlSelect);
			for(int i = 0;i<sqlRowNum;i++){
				str.append("'");
				str.append(sqlStr(i,"p_seqno"));
				str.append("'");
				if(i==sqlRowNum-1){
					continue;
				}
				str.append(",");
			}
			str.append(")");
		
			wp.whereStr += str.toString();
		}

		if(empty(wp.itemStr("ex_accno")) == false){
			wp.whereStr += " and a.acct_no = :ex_accno ";
			setString("ex_accno", wp.itemStr("ex_accno"));
		}
		
		if(empty(wp.itemStr("ex_cardno")) == false){
			wp.whereStr += " and a.card_no like :ex_cardno ";
			setString("ex_cardno", wp.itemStr("ex_cardno")+"%");
		}

		if(wp.itemStr("ex_deduct_type").equals("00")){
			wp.whereStr += " and a.deduct_proc_code = '00' ";
		}else{
			wp.whereStr += " and a.deduct_proc_code <> '00' ";
		}
		
		if(wp.itemStr("ex_type").equals("1")){
			wp.whereStr += " and a.adjust_type = 'FD10' ";
		}else if(wp.itemStr("ex_type").equals("3")){
			wp.whereStr += " and a.adjust_type = 'TO10' ";
		}else if(wp.itemStr("ex_type").equals("2")){
			wp.whereStr += " and a.adjust_type <> 'FD10' and adjust_type <> 'TO10' ";
		}
		
		if(empty(wp.itemStr("ex_crdate_s")) == false){
			wp.whereStr += " and a.deduct_date >= :ex_crdate_s ";
			setString("ex_crdate_s", wp.itemStr("ex_crdate_s"));
		}
		if(empty(wp.itemStr("ex_crdate_e")) == false){
			wp.whereStr += " and a.deduct_date <= :ex_crdate_e ";
			setString("ex_crdate_e", wp.itemStr("ex_crdate_e"));
		}
		
		return 1;
	}
	
	public void queryFunc() throws Exception {

		//-page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
   
		queryRead();
		
	}

	@Override
	public void queryRead() throws Exception {
		String daoTable = "";
		wp.pageControl();

		wp.selectSQL = " a.deduct_date " 
					 + ", a.acct_type "
					 + ", a.acct_no "
					 + ", a.card_no "
					 + ", a.purchase_date "
					 + ", a.item_post_date "
					 + ", a.dr_amt "
					 + ", a.orginal_amt "
					 + ", a.cr_amt "
					 + ", a.bef_amt "
					 + ", a.aft_amt "
					 + ", a.bef_d_amt "
					 + ", a.aft_d_amt "
					 + ", a.acct_code "
					 + ", a.from_code "
					 + ", a.adjust_type "
					 + ", a.deduct_proc_code || '(' || nvl(d.wf_desc, '') || ')' as deduct_proc_code " //轉中文
					 + ", a.deduct_proc_type"
					 + ", a.deduct_amt "
					 + ", b.p_seqno "
					 + ", b.id_p_seqno "
					 + ", b.acct_holder_id||'_'||b.acct_holder_id_code as db_id "
					 + ", c.acct_code||'_'||c.chi_short_name as tt_acct_code "
					 + ", b.id_p_seqno "
					 + ", b.corp_p_seqno "
					 + ", UF_VD_IDNO_NAME(b.id_p_seqno) as chi_name1 "
					 + ", UF_CORP_NAME(b.corp_p_seqno) as chi_name2 "
					 ;
		wp.daoTable = " dba_acaj as a left join dba_acno as b on a.p_seqno = b.p_seqno "
					+ " left join ptr_actcode as c on a.acct_code = c.acct_code " 
				    + " left join ptr_sys_idtab d on a.deduct_proc_code = d.wf_id and d.wf_type = 'DBAR0010' ";
		wp.whereOrder ="  ";
		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		if(getWhereStr()!=1)return;
		daoTable = wp.daoTable;
		pageQuery(); 
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		if(getWhereStr()!=1)return;
		String sqlSelect = "select sum(deduct_amt) as deduct_amt_tol from "+daoTable+wp.whereStr;
		sqlSelect(sqlSelect);
		wp.colSet("deduct_amt_tol", sqlNum("deduct_amt_tol"));
		wp.colSet("selectCnt",wp.selectCnt);
	    listWkdata();
		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		
	}
	
	
	public void dataProcess() throws Exception {
		queryFunc();
		if (wp.selectCnt==0){
			alertErr2("報表無資料可比對");
			return;
		}
		
	}
	
	void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = progName;
			//-cond-
		/*	String ss = "生效年月: " + commString.ss_2ymd(wp.item_ss("ex_yymm1"))
					  + " -- " + commString.ss_2ymd(wp.item_ss("ex_yymm2"));
			wp.col_set("cond_1", ss);*/
			/*String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1"))
			  + " -- " + commString.ss_2ymd(wp.item_ss("ex_send_date1"));
			wp.col_set("cond_2", ss2);*/

			//===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			//xlsx.report_id ="rskr0020";
			xlsx.excelTemplate = progName + ".xlsx";
			
			//====================================
			//-明細-
			xlsx.sheetName[0] ="明細";
			
			queryFunc();
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
     		xlsx.processExcelSheet(wp);

			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");
			
		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}
	
	void pdfPrint() throws Exception {
		wp.reportId = progName;
		//-cond-
		String cond1 = commString.strToYmd(wp.itemStr("ex_crdate_s"))
		  + " -- " + commString.strToYmd(wp.itemStr("ex_crdate_e"));
		wp.colSet("cond_1", cond1);
		/*String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1"))
		  + " -- " + commString.ss_2ymd(wp.item_ss("ex_send_date1"));
		wp.col_set("cond_2", ss2);*/
		wp.colSet("ex_deduct_type", wp.itemStr("ex_deduct_type"));
		wp.colSet("ex_id", wp.itemStr("ex_id"));
		wp.colSet("ex_cardno", wp.itemStr("ex_cardno"));
		wp.colSet("ex_accno", wp.itemStr("ex_accno"));
		wp.colSet("IdUser", wp.loginUser);
		wp.pageRows =99999;
		if(getWhereStr()!=1){
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		//wp.dddSql_log=false;
		queryFunc();
	//	wp.setListCount(1);
	
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.pageCount = 27;
		pdf.excelTemplate = progName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
	}
	
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl")>0) {
			this.btnModeAud();
		}
	}
	
	void listWkdata(){
		String chiName="";
		for(int i = 0; i<wp.selectCnt;i++){
			chiName = wp.colStr(i,"chi_name1");
			if(empty(chiName)){
				chiName = wp.colStr(i,"chi_name2");	
			}
			wp.colSet(i,"db_chiname", chiName);
		}

	}
	
	int authQueryVd(){
		String idno="";
		String sqlSelect="select vd_end_bal from ptr_comm_data where 1=1 and parm_code='COLM0910' and seq_no =1 ";
		sqlSelect(sqlSelect);
		double lmAmtParm = sqlNum("vd_end_bal");
		if(sqlRowNum<0){
			alertErr("資料查詢權限: select PTR_COMM_DATA error");
			return -1;
		}
		if(lmAmtParm<=0){
			return 1;
		}
		if(empty(wp.itemStr("ex_id"))&&empty(wp.itemStr("ex_cardno"))){
			alertErr("資料查詢權限:卡號 or 身分證ID 不可空白");
			return -1;
		}
		if(wp.itemStr("ex_cardno").length()>=14){
			sqlSelect = "select id_p_seqno from dbc_card where card_no like :card_no fetch first 1 rows only ";
			setString("card_no",wp.itemStr("ex_cardno"));
			sqlSelect(sqlSelect);
			idno = sqlStr("id_p_seqno");
			if(sqlRowNum<=0){
				idno = "";
			}
		}else{
			idno = wp.itemStr("ex_id");
			sqlSelect = "select id_p_seqno from dbc_idno where id_no = :ls_idno fetch first 1 rows only ";
			setString("ls_idno",idno);
			sqlSelect(sqlSelect);
			idno = sqlStr("id_p_seqno");
		}
		
		if(empty(idno)){
			alertErr("資料查詢權限:查核資料不是 VD 卡號 or 身分證ID");
			return -1;
		}
		sqlSelect = "select sum(end_bal) as lm_amt from dba_debt where id_p_seqno = :ls_idno ";
		setString("ls_idno",idno);
		sqlSelect(sqlSelect);
		double lmAmt = sqlNum("lm_amt");
		if(sqlRowNum<0){
			alertErr("資料查詢權限: select DBC_DEBT error,KEY="+idno);
			return -1;
		}
		if(lmAmt>=lmAmtParm){
			return 1;
		}
		alertErr("資料查詢權限:卡友欠款未達 [參數金額] 不可查詢,KEY="+idno);
		
		return -1;
	}
	
}
