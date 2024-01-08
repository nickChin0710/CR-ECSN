/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-09-22  V1.00.01  Ryan       Initial                                  
* 112-10-12  V1.00.02  Ryan       新增刪除、修改功能，調整查詢條件功能，  調整時效完成日計算功能*
* 112-10-19  V1.00.03  Ryan       時效完成日已到期,債權憑證狀態系統自動改為2.註銷                           *
***************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm6100Func extends FuncEdit {

	public Colm6100Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}
	private final String COL_BAD_CERTINFO = "COL_BAD_CERTINFO";
	private final String COL_BAD_CERTINFO_T = "COL_BAD_CERTINFO_T";
	// ************************************************************************
	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	// ************************************************************************
	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 1;
	}

	// ************************************************************************
	@Override
	public void dataCheck() {
		this.msgOK();
		if(!this.isDelete()) {
			String certDate = wp.itemStr("cert_date"); 
			String certEndDate = wp.itemStr("cert_end_date");
			CommDate commDate = new CommDate();
			if(chkStrend(certDate, certEndDate)== -1 || empty(certEndDate)) {
				certEndDate = commDate.dateAdd(certDate, 5, 0, -1);
				wp.itemSet("cert_end_date", certEndDate);
				if(certEndDate.compareTo(wp.sysDate)<0) {
					wp.itemSet("cert_status", "2");
					wp.alertMesg("時效完成日已到期,債權憑證狀態系統自動改為2.註銷");
				}
			}
			getCourtName();
		}
        if (this.isAdd())
        {
        	int resultCode = 0;
    		String kkIdCorpNo = wp.itemStr("kk_id_corp_no");
    		String kkCardFlag = wp.itemStr("kk_card_flag");
    		if(empty(kkIdCorpNo)) {
    			errmsg(String.format("身份證字號/統編不能為空值"));
    			return;
    		}
    		if(empty(kkCardFlag)) {
    			errmsg(String.format("類型不能為空值"));
    			return;
    		}
        	if(kkIdCorpNo.length() > 0 && kkIdCorpNo.length() != 8 
    				&& kkIdCorpNo.length() != 10 && kkIdCorpNo.length() != 11) {
    			errmsg(String.format("身份證字號/統編長度只能輸入8、10、11碼"));
    			return;
    		}
        	if("1".equals(kkCardFlag)) {
        		resultCode = chkIdNo(kkIdCorpNo);
        	}else {
        		resultCode = chkCorpNo(kkIdCorpNo);
        	}
        	if(resultCode == 1) {
        		return;
        	}
            //檢查新增資料是否重複
            String lsSql = "select count(*) as tot_cnt from " + COL_BAD_CERTINFO_T
            		+ " where ID_CORP_P_SEQNO = ? ";
            Object[] param = new Object[] { wp.itemStr("id_corp_p_seqno")};
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("COL_BAD_CERTINFO_T ,資料已存在不可新增");
            }
            
            //檢查新增資料是否重複
            lsSql = "select count(*) as tot_cnt from " + COL_BAD_CERTINFO
            		+ " where ID_CORP_P_SEQNO = ? ";
            param = new Object[] { wp.itemStr("id_corp_p_seqno")};
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("COL_BAD_CERTINFO ,資料已存在不可新增");
            }
            return;
        }
        
        if(this.isDelete()) {
        	if(wp.itemEmpty("apr_date") == false) {
        		 errmsg("資料已覆核 ,不可刪除");
        	}
        }
	}

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert(COL_BAD_CERTINFO_T);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("chi_name", wp.itemStr("chi_name"));
		sp.ppstr("cert_no", wp.itemStr("cert_no"));
		sp.ppstr("brunch", wp.itemStr("brunch"));
		sp.ppstr("cert_type", wp.itemStr("cert_type"));
		sp.ppstr("court_year", wp.itemStr("court_year"));
		sp.ppstr("court_id", wp.itemStr("court_id"));
		sp.ppstr("court_name", wp.itemStr("court_name"));
		sp.ppstr("court_desc", wp.itemStr("court_desc"));
		sp.ppstr("cert_kind", wp.itemStr("cert_kind"));
		sp.ppstr("cert_date", wp.itemStr("cert_date"));
		sp.ppstr("cert_end_date", wp.itemStr("cert_end_date"));
		sp.ppstr("cert_status", wp.itemStr("cert_status"));
//		sp.ppstr("cert_status_date", wp.itemStr("cert_status_date"));
		sp.ppnum("cert_src_amt", wp.itemNum("cert_src_amt"));
		sp.ppstr("cert_memo", wp.itemStr("cert_memo"));
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 0);
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("新增 "+COL_BAD_CERTINFO_T+" 錯誤");
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc != 1) 
			return rc;
		if(wp.itemEq("table_type","1")) {
			dbInsert2();
			return rc;
		}
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update(COL_BAD_CERTINFO_T);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("chi_name", wp.itemStr("chi_name"));
		sp.ppstr("cert_no", wp.itemStr("cert_no"));
		sp.ppstr("brunch", wp.itemStr("brunch"));
		sp.ppstr("cert_type", wp.itemStr("cert_type"));
		sp.ppstr("court_year", wp.itemStr("court_year"));
		sp.ppstr("court_id", wp.itemStr("court_id"));
		sp.ppstr("court_name", wp.itemStr("court_name"));
		sp.ppstr("court_desc", wp.itemStr("court_desc"));
		sp.ppstr("cert_kind", wp.itemStr("cert_kind"));
		sp.ppstr("cert_date", wp.itemStr("cert_date"));
		sp.ppstr("cert_end_date", wp.itemStr("cert_end_date"));
		sp.ppstr("cert_status", wp.itemStr("cert_status"));
//		sp.ppstr("cert_status_date", wp.itemStr("cert_status_date"));
		sp.ppnum("cert_src_amt", wp.itemNum("cert_src_amt"));
		sp.ppstr("cert_memo", wp.itemStr("cert_memo"));
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_time = sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ");
		sp.addsql(", mod_seqno = mod_seqno + 1 ");
		sp.sql2Where(" where id_corp_p_seqno = ? ", wp.itemStr("id_corp_p_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("修改 "+COL_BAD_CERTINFO_T+" 錯誤");
		return rc;
	}
	
	public int dbInsert2() {
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert(COL_BAD_CERTINFO_T);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("chi_name", wp.itemStr("chi_name"));
		sp.ppstr("cert_no", wp.itemStr("cert_no"));
		sp.ppstr("brunch", wp.itemStr("brunch"));
		sp.ppstr("cert_type", wp.itemStr("cert_type"));
		sp.ppstr("court_year", wp.itemStr("court_year"));
		sp.ppstr("court_id", wp.itemStr("court_id"));
		sp.ppstr("court_name", wp.itemStr("court_name"));
		sp.ppstr("court_desc", wp.itemStr("court_desc"));
		sp.ppstr("cert_kind", wp.itemStr("cert_kind"));
		sp.ppstr("cert_date", wp.itemStr("cert_date"));
		sp.ppstr("cert_end_date", wp.itemStr("cert_end_date"));
		sp.ppstr("cert_status", wp.itemStr("cert_status"));
//		sp.ppstr("cert_status_date", wp.itemStr("cert_status_date"));
		sp.ppnum("cert_src_amt", wp.itemNum("cert_src_amt"));
		sp.ppstr("cert_memo", wp.itemStr("cert_memo"));
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 0);
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("新增 "+COL_BAD_CERTINFO_T+" 錯誤");
		return rc;
	}
	
	public int chkIdNo(String kkIdCorpNo) {
		sqlSelect = "select id_p_seqno from crd_idno where id_no = :id_no";
		setString("id_no", kkIdCorpNo);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("查無身份證號,無法新增");
			return 1;
		}
		return 0;
	}
	
	public int chkCorpNo(String kkIdCorpNo) {
		String sqlSelect = "select corp_p_seqno from crd_corp where corp_no = :corp_no";
		setString("corp_no", kkIdCorpNo);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("查無統一編號,無法新增");
			return 1;
		}
		return 0;
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc != 1) 
			return rc;
		strSql = "delete "+COL_BAD_CERTINFO_T+" where id_corp_p_seqno = ? ";

		Object[] param = new Object[] { wp.itemStr("id_corp_p_seqno") };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0)
			errmsg("刪除 "+COL_BAD_CERTINFO_T+" 錯誤");
		return rc;
	}
	
	private void getCourtName() {
		String sqlCmd = "select wf_desc from ptr_sys_idtab where wf_type = 'COLM6100_CERT_COURT' and wf_id = :wf_id ";
		setString("wf_id", wp.itemStr("court_id"));
		sqlSelect(sqlCmd);
		if (sqlRowNum > 0) {
			wp.itemSet("court_name", colStr("wf_desc"));
		}
	}
	// ************************************************************************
	
} // End of class
