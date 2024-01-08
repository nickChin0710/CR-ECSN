/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-09-22  V1.00.01  Ryan       Initial                                  *
* 112-10-12  V1.00.02  Ryan       新增刪除、修改功能，調整查詢條件功能，  調整時效完成日計算功能 ，債證狀態顯示調整*
* 112-10-19  V1.00.03  Ryan       畫面新增 apr_user,apr_date,apr_flag欄位                  *
***************************************************************************/
package colm01;

import busi.ecs.CommRoutine;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm6100 extends BaseEdit {
	CommRoutine commr = new CommRoutine();
	CommString comms = new CommString();
	private final String COL_BAD_CERTINFO = "COL_BAD_CERTINFO";
	private final String COL_BAD_CERTINFO_T = "COL_BAD_CERTINFO_T";
	String kk1,kk2,kk3,kk4;
	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "X";
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
		} else if (eqIgno(wp.buttonCode, "S1")) {/* 身份證字號/統編讀取 */
			strAction = "S1";
			checkIdCorpNo();
		} else if (eqIgno(wp.buttonCode, "D1")) {/* 修改取消 */
			strAction = "D1";
			saveFunc();
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
	
	private String getWhereStr() throws Exception {
		String lsWhere = " where 1=1 ";
	    String exIdCorpNo = wp.itemStr("ex_id_corp_no");
	    String exCertDate1 = wp.itemStr("ex_cert_date1");
	    String exCertDate2 = wp.itemStr("ex_cert_date2");
	    String exCardFlag = wp.itemStr("ex_card_flag");
	    String exCertType = wp.itemStr("ex_cert_type");
	    String exCertStatus = wp.itemStr("ex_cert_status");
	    String exCertEndDate1 = wp.itemStr("ex_cert_end_date1");
	    String exCertEndDate2 = wp.itemStr("ex_cert_end_date2");
	    String exBranch = wp.itemStr("ex_branch");
	    
		if (this.chkStrend(exCertDate1, exCertDate2) == false) {
			alertErr("[時效起算日-起迄]  輸入錯誤");
			return null;
		}
		if (this.chkStrend(exCertEndDate1, exCertEndDate2) == false) {
			alertErr("[時效到期日-起迄]  輸入錯誤");
			return null;
		}
		
	    if (!empty(exIdCorpNo)) {
	        lsWhere += " and  id_corp_no = :exIdCorpNo ";
	        setString("exIdCorpNo", exIdCorpNo);
	    }
	    if (!empty(exCertDate1)) {
	    	lsWhere += " and  cert_date >= :exCertDate1 ";
		    setString("exCertDate1", exCertDate1);
		}
	    if (!empty(exCertDate2)) {
	    	lsWhere += " and  cert_date <= :exCertDate2 ";
		    setString("exCertDate2", exCertDate2);
		}
	    if (toInt(exCardFlag) > 0) {
	    	lsWhere += " and  card_flag = :exCardFlag ";
		    setString("exCardFlag", exCardFlag);
		}
	    if (toInt(exCertType) > 0) {
	    	lsWhere += " and  cert_type = :exCertType ";
		    setString("exCertType", exCertType);
		}
	    if(!empty(exCertStatus)) {
	    	lsWhere += " and  cert_status = :exCertStatus ";
			setString("exCertStatus", exCertStatus);
	    }
	    if (!empty(exCertEndDate1)) {
	    	lsWhere += " and  cert_end_date >= :exCertEndDate1 ";
		    setString("exCertEndDate1", exCertEndDate1);
		}
	    if (!empty(exCertEndDate2)) {
	    	lsWhere += " and  cert_end_date <= :exCertEndDate2 ";
		    setString("exCertEndDate2", exCertEndDate2);
		}
	    if (!empty(exBranch)) {
	    	lsWhere += " and  brunch = :exBranch ";
		    setString("exBranch", exBranch);
		}
	    return lsWhere;
	  }

	
	// ************************************************************************
	@Override
	public void queryRead() throws Exception {
		String sqlWhere = getWhereStr();
		if(sqlWhere == null) {
			return;
		}
		wp.pageControl();
		wp.sqlCmd = " ";
		if(wp.itemEq("ex_apr_flag", "0")) {
			wp.sqlCmd += sqlCmdMainTable();
			wp.sqlCmd += sqlWhere;
			wp.sqlCmd += " union ";
			wp.sqlCmd += sqlCmdTmpTable();
			wp.sqlCmd += sqlWhere;
		}
		if(wp.itemEq("ex_apr_flag", "1")) {
			wp.sqlCmd += sqlCmdMainTable();
			wp.sqlCmd += sqlWhere;
		}
		if(wp.itemEq("ex_apr_flag", "2")) {
			wp.sqlCmd += sqlCmdTmpTable();
			wp.sqlCmd += sqlWhere;
		}

	    wp.pageCountSql = " select count(*) cnt from ( " + wp.sqlCmd + " )";
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setPageValue();
		commPtrSysIdtab("cert_kind","COLM6100_CERT_KIND");
		commPtrSysIdtab("cert_status","COLM6100_CERT_STATUS");
		commPtrSysIdtab("court_id","COLM6100_CERT_COURT");
		commCertType("cert_type");
		commCardFlag("card_flag");
	}
	
	private String sqlCmdMainTable() {
		String sqlCmd = "select '1' as table_type ";
		sqlCmd += " ,id_corp_p_seqno ";
		sqlCmd += " ,id_corp_no ";
		sqlCmd += " ,card_flag ";
		sqlCmd += " ,to_char(crt_time,'yyyymmdd') as crt_date ";
		sqlCmd += " ,cert_type ";
		sqlCmd += " ,chi_name ";
		sqlCmd += " ,court_id ";
		sqlCmd += " ,court_name ";
		sqlCmd += " ,court_year ";
		sqlCmd += " ,court_desc ";
		sqlCmd += " ,cert_kind ";
		sqlCmd += " ,cert_status ";
		sqlCmd += " ,cert_date ";
		sqlCmd += " ,cert_end_date ";
		sqlCmd += " ,mod_user ";
		sqlCmd += " ,to_char(mod_time,'yyyymmdd') as mod_date ";
		sqlCmd += " ,apr_user ";
		sqlCmd += " ,apr_date ";
		sqlCmd += " ,'已覆核' apr_flag ";
//		sqlCmd += " ,decode((select 1 from ";
//		sqlCmd += COL_BAD_CERTINFO_T;
//		sqlCmd += " where id_corp_p_seqno = ";
//		sqlCmd += COL_BAD_CERTINFO;
//		sqlCmd += ".id_corp_p_seqno),'1','未覆核','已覆核') as apr_flag ";
		sqlCmd += " from ";
		sqlCmd += COL_BAD_CERTINFO;
		return sqlCmd;
	}
	
	
	private String sqlCmdTmpTable() {
		String sqlCmd = "select '2' as table_type ";
		sqlCmd += " ,id_corp_p_seqno ";
		sqlCmd += " ,id_corp_no ";
		sqlCmd += " ,card_flag ";
		sqlCmd += " ,to_char(crt_time,'yyyymmdd') as crt_date ";
		sqlCmd += " ,cert_type ";
		sqlCmd += " ,chi_name ";
		sqlCmd += " ,court_id ";
		sqlCmd += " ,court_name ";
		sqlCmd += " ,court_year ";
		sqlCmd += " ,court_desc ";
		sqlCmd += " ,cert_kind ";
		sqlCmd += " ,cert_status ";
		sqlCmd += " ,cert_date ";
		sqlCmd += " ,cert_end_date ";
		sqlCmd += " ,mod_user ";
		sqlCmd += " ,to_char(mod_time,'yyyymmdd') as mod_date ";
		sqlCmd += " ,'' apr_user ";
		sqlCmd += " ,'' apr_date ";
		sqlCmd += " ,'未覆核' apr_flag ";
		sqlCmd += " from ";
		sqlCmd += COL_BAD_CERTINFO_T;
		return sqlCmd;
	}
	
	void getAddr(String cardFlag , String idCorpNo) {
		if("1".equals(cardFlag)) {
			String sqlSelect = "select resident_addr1,resident_addr2,chi_name from crd_idno where id_no = :idNo";
			setString("idNo",idCorpNo);
			sqlSelect(sqlSelect);
		}else {
			String sqlSelect = "select reg_addr1 as resident_addr1,reg_addr2 as resident_addr2,chi_name from crd_corp where corp_no = :corpNo";
			setString("corpNo",idCorpNo);
			sqlSelect(sqlSelect);
		}
		
		wp.colSet("resident_addr1", sqlStr("resident_addr1"));
		wp.colSet("resident_addr2", sqlStr("resident_addr2"));
		wp.colSet("chi_name", sqlStr("chi_name"));
	}
	
	void getAcctStatus(String cardFlag ,String idCorpPSeqno) {
		if("1".equals(cardFlag)) {
			String sqlSelect = "select acct_status from act_acno where acno_flag='1' and id_p_seqno = :id_corp_p_seqno ";
			setString("id_corp_p_seqno",idCorpPSeqno);
			sqlSelect(sqlSelect);
		}else {
			String sqlSelect = "select acct_status from act_acno where acno_flag='2' and acct_type='03' and corp_p_seqno = :id_corp_p_seqno ";
			setString("id_corp_p_seqno",idCorpPSeqno);
			sqlSelect(sqlSelect);
		}
		
		wp.colSet("acct_status", sqlStr("acct_status"));
	}
	
	void listWkdata() {
		getAddr(wp.colStr("card_flag"),wp.colStr("id_corp_no"));
		getAcctStatus(wp.colStr("card_flag"),wp.colStr("id_corp_p_seqno"));
		if(empty(wp.colStr("cert_status_date"))) {
			commr.setConn(wp);
			wp.colSet("cert_status_date", commr.getBusinDate());
		}
		
		int resultCnt = selectMainTable();
		if(resultCnt > 0) {
			wp.colSet("display_btn2", "style=display:none");
			wp.colSet("display_btn1", "");
		}else {
			wp.colSet("display_btn2", "");
			wp.colSet("display_btn1", "style=display:none");
		}
	}
	
	void checkIdCorpNo() throws Exception{
		this.msgOK();
		String kkIdCorpNo = wp.itemStr("kk_id_corp_no");
		String kkCardFlag = wp.itemStr("kk_card_flag");
//		if(kkIdCorpNo.length() > 0 && kkIdCorpNo.length() != 8 
//				&& kkIdCorpNo.length() != 10 && kkIdCorpNo.length() != 11) {
//			errmsg(String.format("身份證字號/統編長度只能輸入8、10、11碼"));
//			return;
//		}
		//個人輸入長度檢核
		if(kkIdCorpNo.length() > 0 && kkCardFlag.equals("1") && kkIdCorpNo.length() != 10) {
			errmsg(String.format("身份證字號長度只能輸入10碼"));
			return;
		}
		//公司輸入長度檢核
		if(kkIdCorpNo.length() > 0 && kkCardFlag.equals("2") && kkIdCorpNo.length() != 8 
				&& kkIdCorpNo.length() != 10 && kkIdCorpNo.length() != 11) {
			errmsg(String.format("統編長度只能輸入8、10、11碼"));
			return;
		}
		String idCorpPSeqno = "";
		String sqlCmd = "";
		if("1".equals(kkCardFlag)) {
			sqlCmd = "select id_p_seqno as id_corp_p_seqno from crd_idno where id_no = :kkIdCorpNo";
		}else {
			sqlCmd = "select corp_p_seqno as id_corp_p_seqno from crd_corp where corp_no = :kkIdCorpNo";
		}
		setString("kkIdCorpNo",kkIdCorpNo);
		sqlSelect(sqlCmd);
		if(sqlRowNum<=0) {
			errmsg(String.format("查無此%s,無法新增","1".equals(kkCardFlag)?"身份證號":"統一編號"));
			return;
		}
		idCorpPSeqno = sqlStr("id_corp_p_seqno");
		wp.colSet("id_corp_p_seqno",idCorpPSeqno);
		wp.colSet("brunch", "3144");
		wp.colSet("court_id", "");
		wp.colSet("court_desc", "");
		wp.colSet("cert_status", "0");
		wp.colSet("id_corp_no", kkIdCorpNo);
		wp.colSet("card_flag", kkCardFlag);
		commr.setConn(wp);
		wp.colSet("cert_status_date", commr.getBusinDate());
		wp.colSet("crt_user", wp.loginUser);
		wp.colSet("crt_date", wp.sysDate);

		getAddr(kkCardFlag,kkIdCorpNo);
		getAcctStatus(kkCardFlag,idCorpPSeqno);
	}
	
	private String selectTmpTable() {
		String sqlCmd = "select count(*) cnt from col_bad_certinfo_t where id_corp_p_seqno = :kk2 ";
		setString("kk2",kk2);
		sqlSelect(sqlCmd);
		return sqlInt("cnt") > 0 ? "2" : "1";
	}
	
	private int selectMainTable() {
		String sqlCmd = "select count(*) cnt from col_bad_certinfo where id_corp_p_seqno = :kk2 ";
		setString("kk2",kk2);
		sqlSelect(sqlCmd);
		return sqlInt("cnt") > 0 ? 1 : 0;
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		kk2 = itemKk("data_k2");
		
		if(empty(kk2))
			kk2 = wp.itemStr("id_corp_p_seqno");
		
		kk1 = selectTmpTable();
		
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		
		wp.selectSQL = " hex(rowid) as rowid, to_char(crt_time,'yyyymmdd') as crt_date "
				+ " ,to_char(mod_time,'yyyymmdd') as mod_date ,*";
		wp.selectSQL += "1".equals(kk1)?" ,'1' as table_type " : " ,'2' as table_type ";
		wp.daoTable = "1".equals(kk1)?COL_BAD_CERTINFO:COL_BAD_CERTINFO_T;
		wp.whereStr = "where 1=1 ";
		wp.whereStr += "and id_corp_p_seqno = :kk2 ";
		setString("kk2",kk2);
		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + kk2 + "]");
			return;
		}
		if("2".equals(kk1)) {
			wp.alertMesg("此筆資料待覆核…");
		}
		listWkdata();
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		colm01.Colm6100Func func = new colm01.Colm6100Func(wp);
		func.setConn(wp);
		if("D1".equals(strAction))
			rc = func.dbSave("D");
		else
			rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
		if(rc == 1)
			saveAfterMsg(strAction);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
			if(rc==-1 && "S1".equals(strAction)) {
				this.btnAddOn(false);
			}
			if(wp.colEmpty("apr_date") == false) {
				this.btnDeleteOn(false);
			}
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		   try {
			wp.initOption ="--";
			if(wp.respHtml.indexOf("_detl") > 0) {
			    wp.optionKey = wp.colStr("cert_kind");
				this.dddwList("dddw_cert_kind", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6100_CERT_KIND' order by wf_id ");
			    wp.optionKey = wp.colStr("court_id");
				this.dddwList("dddw_court_id", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6100_CERT_COURT' order by wf_id ");
				wp.optionKey = wp.colStr("cert_status");
			}else {
				wp.optionKey = wp.itemStr("ex_cert_status");
			}
			if(!"X".equals(strAction)) {
			    this.dddwList("dddw_cert_status", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6100_CERT_STATUS' order by wf_id ");
			}else {
				wp.initOption ="";
			    this.dddwList("dddw_cert_status", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6100_CERT_STATUS' and wf_id = '0' order by wf_id ");
			}
		} catch (Exception e) {
		}
	}

	// ************************************************************************
	@Override
	public void initPage() {
		wp.colSet("display_btn1", "style=display:none");
		wp.colSet("DEFAULT_CHK", "checked");
		return;
	}
	// ************************************************************************

	private void commPtrSysIdtab(String commStr ,String wfType) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String sqlCmd = "select wf_desc from ptr_sys_idtab where wf_type = :wf_type and wf_id = :wf_id ";
			setString("wf_type",wfType);
			setString("wf_id",wp.colStr(ii,commStr));
			sqlSelect(sqlCmd);
			if(sqlRowNum > 0) {
				wp.colSet(ii,"comm_" + commStr, sqlStr("wf_desc"));
			}
		}
	}
	
	private void commCertType(String commStr) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String commCertType = comms.decode(wp.colStr(ii,commStr), ",1,2",",執行名義,債權憑證");
			wp.colSet(ii,"comm_" + commStr, commCertType);
		}
	}
	
	private void commCardFlag(String commStr) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String commCertType = comms.decode(wp.colStr(ii,commStr), ",1,2",",個人,公司");
			wp.colSet(ii,"comm_" + commStr, commCertType);
		}
	}
	
	private void saveAfterMsg(String btnAction) throws Exception {
		switch(btnAction) {
		case"A":
			alertMsg("新增完成，此筆資待覆核…");
			break;
		case"U":
			querySelect();
			alertMsg("修改完成，此筆資料待覆核…");
			break;
		case"D1":
			alertMsg("修改取消完成");
			querySelect();
			break;
		}
	}
	
} // End of class
