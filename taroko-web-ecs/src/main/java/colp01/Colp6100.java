/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-09-22  V1.00.00  Ryan        Initial                                  
* 112-10-12  V1.00.02  Ryan        調整查詢條件功能                                                                                      *
******************************************************************************/
package colp01;

import busi.ecs.CommRoutine;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colp6100 extends BaseProc {
	CommRoutine commr = new CommRoutine();
	CommString comms = new CommString();
	private final String COL_BAD_CERTINFO = "COL_BAD_CERTINFO";
	private final String COL_BAD_CERTINFO_T = "COL_BAD_CERTINFO_T";

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
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "D")) {
			// -資料刪除-
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
		} 

		dddwSelect();
		initButton();
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


	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		String sqlWhere = getWhereStr();
		if(sqlWhere == null) {
			return;
		}
		wp.pageControl();
		
		wp.sqlCmd = sqlCmdTmpTable();
		wp.sqlCmd += sqlWhere;
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
		apprDisabled("mod_user");
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
		sqlCmd += " from ";
		sqlCmd += COL_BAD_CERTINFO_T;
		return sqlCmd;
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {
		Colp6100Func func = new Colp6100Func(wp);
		int resultCnt = 0;
		int ilOk = 0;
		int ilErr = 0;
		String[] idCorpPSeqno = wp.itemBuff("id_corp_p_seqno");
		String[] certStatus = wp.itemBuff("cert_status");
		String[] certEndDate = wp.itemBuff("cert_end_date");
		String[] certDate = wp.itemBuff("cert_date");
		
		String[] opt = wp.itemBuff("opt");
		wp.listCount[0] = idCorpPSeqno.length;
		
		for(int i=0;i<idCorpPSeqno.length;i++) {
			if (!checkBoxOptOn(i, opt)) {
				continue;
			}
			func.varsSet("id_corp_p_seqno", idCorpPSeqno[i]);
			func.varsSet("cert_status", certStatus[i]);
			func.varsSet("cert_end_date", certEndDate[i]);
			func.varsSet("cert_date", certDate[i]);
			
			resultCnt = func.dataProc();
			if(resultCnt > 0) {
				ilOk++;
				wp.colSet(i,"ok_flag", "V");
			}else {
				ilErr++;
				wp.colSet(i,"ok_flag", "X");
			}
			sqlCommit(resultCnt);
		}
		alertMsg(String.format("覆核完成 ,成功 = %d ,失敗 = %d", ilOk,ilErr));
	}
	
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

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}
	
	@Override
	public void initPage() {
		wp.colSet("DEFAULT_CHK", "checked");
		return;
	}

	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_cert_status");
			this.dddwList("dddw_cert_status", "ptr_sys_idtab", "wf_id", "wf_desc",
					"where wf_type = 'COLM6100_CERT_STATUS' order by wf_id ");
		} catch (Exception e) {
		}
	}
}
