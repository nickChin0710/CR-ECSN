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
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm6200 extends BaseEdit {
	CommRoutine commr = new CommRoutine();
	CommString comms = new CommString();
	private final String COL_BAD_OUTSOURCE = "COL_BAD_OUTSOURCE";
	String kk1;
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
	    String exCardFlag = wp.itemStr("ex_card_flag");
		
	    if (!empty(exIdCorpNo)) {
	        lsWhere += " and  id_corp_no = :exIdCorpNo ";
	        setString("exIdCorpNo", exIdCorpNo);
	    }
	    if(!"0".equals(exCardFlag)) {
	        lsWhere += " and  card_flag = :exCardFlag ";
	        setString("exCardFlag", exCardFlag);
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
		wp.sqlCmd += sqlCmd();
		wp.sqlCmd += sqlWhere;

	    wp.pageCountSql = " select count(*) cnt from ( " + wp.sqlCmd + " )";
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setPageValue();
		commCardFlag("card_flag");
		for(int i = 0 ; i<wp.selectCnt ; i++) {
			getName(wp.colStr(i,"card_flag"),wp.colStr(i,"id_corp_no"),i);
		}
	}
	
	
	private String sqlCmd() {
		String sqlCmd = "select hex(rowid) as rowid ";
		sqlCmd += " ,id_corp_p_seqno ";
		sqlCmd += " ,id_corp_no ";
		sqlCmd += " ,card_flag ";
		sqlCmd += " ,OS_CMP_ID ";
		sqlCmd += " ,OS_CMP_NO ";
		sqlCmd += " ,OS_CMP_NAME ";
		sqlCmd += " ,OS_AMT ";
		sqlCmd += " ,OS_DATE ";
		sqlCmd += " ,COMMISSION ";
		sqlCmd += " ,HAND_TYPE ";
		sqlCmd += " ,BACK_CODE ";
		sqlCmd += " ,to_char(BACK_DATE,'yyyymmdd') as back_date ";
		sqlCmd += " ,to_char(crt_time,'yyyymmdd') as crt_date ";
		sqlCmd += " ,CRT_USER ";
		sqlCmd += " ,mod_user ";
		sqlCmd += " ,to_char(mod_time,'yyyymmdd') as mod_date ";
		sqlCmd += " ,apr_user ";
		sqlCmd += " ,apr_date ";
		sqlCmd += " from ";
		sqlCmd += COL_BAD_OUTSOURCE;
		return sqlCmd;
	}
	
	void getName(String cardFlag , String idCorpNo , int index) {
		if("1".equals(cardFlag)) {
			String sqlSelect = "select chi_name from crd_idno where id_no = :idNo";
			setString("idNo",idCorpNo);
			sqlSelect(sqlSelect);
		}else {
			String sqlSelect = "select chi_name from crd_corp where corp_no = :corpNo";
			setString("corpNo",idCorpNo);
			sqlSelect(sqlSelect);
		}
	
		wp.colSet(index,"chi_name", sqlStr("chi_name"));
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
		getName(wp.colStr("card_flag"),wp.colStr("id_corp_no"),0);
		getAcctStatus(wp.colStr("card_flag"),wp.colStr("id_corp_p_seqno"));
	}
	
	void checkIdCorpNo() throws Exception{
		this.msgOK();
		String kkIdCorpNo = wp.itemStr("kk_id_corp_no");
		String kkCardFlag = wp.itemStr("kk_card_flag");

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
		wp.colSet("id_corp_no", kkIdCorpNo);
		wp.colSet("card_flag", kkCardFlag);
		wp.colSet("crt_user", wp.loginUser);
		wp.colSet("crt_date", wp.sysDate);

		getName(kkCardFlag,kkIdCorpNo,0);
		getAcctStatus(kkCardFlag,idCorpPSeqno);
	}
	
	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		kk1 = itemKk("data_k1");
		
		if(empty(kk1))
			kk1 = wp.itemStr("id_corp_p_seqno");
		
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		
		wp.sqlCmd = sqlCmd();
		wp.sqlCmd += " where id_corp_p_seqno = :kk1 ";
		setString("kk1",kk1);
		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + kk1 + "]");
			return;
		}
		listWkdata();
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
			   return;
		}		
		
		colm01.Colm6200Func func = new colm01.Colm6200Func(wp);
		func.setConn(wp);
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		   try {
			wp.initOption ="--";
			if(wp.respHtml.indexOf("_detl") > 0) {
			    wp.optionKey = wp.colStr("os_cmp_no");
				this.dddwList("dddw_cmp", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6200_OS_CMP' order by wf_id ");
			    wp.optionKey = wp.colStr("back_code");
				this.dddwList("dddw_back_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6200_OS_BACK_CODE' order by wf_id ");
			}else {
				wp.optionKey = wp.itemStr("ex_os_cmp_no");
				wp.optionKey = wp.itemStr("ex_back_code");
			}
		} catch (Exception e) {
		}
	}

	// ************************************************************************
	@Override
	public void initPage() {
		wp.colSet("DEFAULT_CHK", "checked");
		return;
	}
	// ************************************************************************
	
	private void commCardFlag(String commStr) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String commCertType = comms.decode(wp.colStr(ii,commStr), ",1,2",",個人,公司");
			wp.colSet(ii,"comm_" + commStr, commCertType);
		}
	}
	
} // End of class
