/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-09-22  V1.00.00  Ryan        Initial                                   *
* 112-10-12  V1.00.02  Ryan        調整查詢條件功能、債證狀態 = 2 為覆核刪除                                          *
* 112-10-18  V1.00.03  Ryan        覆核時mod_time,mod_user保留維護者資訊,新增apr_time欄位  *
******************************************************************************/
package colp01;

import busi.FuncProc;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

public class Colp6100Func extends FuncProc {
	private final String COL_BAD_CERTINFO = "COL_BAD_CERTINFO";
	private final String COL_BAD_CERTINFO_T = "COL_BAD_CERTINFO_T";
	private final String COL_BAD_CERTINFO_HST = "COL_BAD_CERTINFO_HST";
	String idCorpPSeqno = "";
	int certinfoCnt = 0;
	public Colp6100Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		CommDate commDate = new CommDate();
		idCorpPSeqno = varsStr("id_corp_p_seqno");
		String certEndDate = varsStr("cert_end_date");
		String certDate = varsStr("cert_date");
		if(chkStrend(certDate, certEndDate)== -1 || empty(certEndDate)) {
			certEndDate = commDate.dateAdd(certDate, 5, 0, -1);
			varsSet("cert_end_date",certEndDate);
		}
		String sqlCmd = "select count(*) cnt1 from ";
		sqlCmd += COL_BAD_CERTINFO;
		sqlCmd += " where id_corp_p_seqno = ? ";
		Object[] param = new Object[] { idCorpPSeqno };
		sqlSelect(sqlCmd,param);
		certinfoCnt = colInt("cnt1");
	}

	@Override
	public int dataProc() {
		msgOK();
		dataCheck();
		if(certinfoCnt > 0)
			dbDelete();
		if(!"2".equals(varsStr("cert_status"))) {
			dbInsert();
		}
		dbInsertHst();
		dbDeleteT();
		return rc;
	}

	private int dbInsert() {
		if(rc != 1)
			return rc;
		String sqlCmd = "insert into ";
		sqlCmd += COL_BAD_CERTINFO;
		sqlCmd += " ( ID_CORP_P_SEQNO,ID_CORP_NO,CHI_NAME,CARD_FLAG,CERT_TYPE,CERT_KIND,CERT_DATE ";
		sqlCmd += " ,CERT_END_DATE,CERT_STATUS,COURT_AREA,COURT_ID,COURT_NAME,COURT_YEAR,COURT_DESC ";
		sqlCmd += " ,BRUNCH,CRT_TIME,CRT_USER,APR_USER,APR_DATE,MOD_TIME,MOD_USER,MOD_PGM,MOD_SEQNO ";
		sqlCmd += " ,CERT_NO,CERT_SRC_AMT,CERT_MEMO,APR_TIME )";
		sqlCmd += " select ID_CORP_P_SEQNO,ID_CORP_NO,CHI_NAME,CARD_FLAG,CERT_TYPE,CERT_KIND,CERT_DATE ";
		sqlCmd += " ,?,CERT_STATUS,COURT_AREA,COURT_ID,COURT_NAME,COURT_YEAR,COURT_DESC ";
		sqlCmd += " ,BRUNCH,CRT_TIME,CRT_USER,?,to_char(sysdate,'yyyymmdd'),MOD_TIME,MOD_USER,MOD_PGM,MOD_SEQNO ";
		sqlCmd += " ,CERT_NO,CERT_SRC_AMT,CERT_MEMO,sysdate from ";
		sqlCmd += COL_BAD_CERTINFO_T;
		sqlCmd += " where id_corp_p_seqno = ? ";
		Object[] param = new Object[] {varsStr("cert_end_date"),wp.loginUser, idCorpPSeqno };
		rc = sqlExec(sqlCmd,param);
		
		return rc;
	}
	
	private int dbInsertHst() {
		if(rc != 1)
			return rc;
		String sqlCmd = "insert into ";
		sqlCmd += COL_BAD_CERTINFO_HST;
		sqlCmd += " ( ID_CORP_P_SEQNO,ID_CORP_NO,CHI_NAME,CARD_FLAG,CERT_TYPE,CERT_KIND,CERT_DATE ";
		sqlCmd += " ,CERT_END_DATE,CERT_STATUS,COURT_AREA,COURT_ID,COURT_NAME,COURT_YEAR,COURT_DESC ";
		sqlCmd += " ,BRUNCH,CRT_TIME,CRT_USER,APR_USER,APR_DATE,MOD_TIME,MOD_USER,MOD_PGM,MOD_SEQNO ";
		sqlCmd += " ,CERT_NO,CERT_SRC_AMT,CERT_MEMO,APR_TIME )";
		sqlCmd += " select ID_CORP_P_SEQNO,ID_CORP_NO,CHI_NAME,CARD_FLAG,CERT_TYPE,CERT_KIND,CERT_DATE ";
		sqlCmd += " ,?,CERT_STATUS,COURT_AREA,COURT_ID,COURT_NAME,COURT_YEAR,COURT_DESC ";
		sqlCmd += " ,BRUNCH,CRT_TIME,CRT_USER,?,to_char(sysdate,'yyyymmdd'),MOD_TIME,MOD_USER,MOD_PGM,MOD_SEQNO ";
		sqlCmd += " ,CERT_NO,CERT_SRC_AMT,CERT_MEMO,sysdate from ";
		sqlCmd += COL_BAD_CERTINFO_T;
		sqlCmd += " where id_corp_p_seqno = ? ";
		Object[] param = new Object[] {varsStr("cert_end_date"),wp.loginUser, idCorpPSeqno };
		rc = sqlExec(sqlCmd,param);
		
		return rc;
	}
	
	private int dbDelete() {
		if(rc != 1)
			return rc;
		String sqlCmd = "delete ";
		sqlCmd += COL_BAD_CERTINFO;
		sqlCmd += " where id_corp_p_seqno = ? ";
		Object[] param = new Object[] { idCorpPSeqno };
		rc = sqlExec(sqlCmd,param);
		
		return rc;
	}
	
	private int dbDeleteT() {
		if(rc != 1)
			return rc;
		String sqlCmd = "delete ";
		sqlCmd += COL_BAD_CERTINFO_T;
		sqlCmd += " where id_corp_p_seqno = ? ";
		Object[] param = new Object[] { idCorpPSeqno };
		rc = sqlExec(sqlCmd,param);
		
		return rc;
	}
	
}
