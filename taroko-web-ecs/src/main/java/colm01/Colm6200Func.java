/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-09-22  V1.00.01  Ryan       Initial                                  
* 112-10-12  V1.00.02  Ryan       新增刪除、修改功能，調整查詢條件功能，  調整時效完成日計算功能*
* 112-10-19  V1.00.03  Ryan       時效完成日已到期,債權憑證狀態系統自動改為2.註銷                           *
* 112-11-23  V1.00.01  Sunny      調整更新資料時不覆蓋建檔日期及建檔日期                         *
***************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm6200Func extends FuncEdit {

	public Colm6200Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}
	private final String COL_BAD_OUTSOURCE = "COL_BAD_OUTSOURCE";
	private final String COL_BAD_OUTSOURCE_HST = "COL_BAD_OUTSOURCE_HST";
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
        }
        
        if(!this.isDelete()) {
        	if(!wp.itemEq("acct_status","4")) {
        		errmsg("戶況為呆帳，始可新增或修改");
        		return;
        	}
        	if(!wp.itemEmpty("back_code")) {
        		if(wp.itemEmpty("back_date")) {
        			errmsg("若有填寫【撤退件原因】則【撤退日期】為必填");
            		return;
        		}	
        	}
        	if(!wp.itemEmpty("back_date")) {
        		if(wp.itemEmpty("back_code")) {
        			errmsg("若有填寫【撤退日期】則【撤退件原因】為必填");
            		return;
        		}	
        		if(wp.itemStr("back_date").compareTo(wp.itemStr("os_date"))<=0) {
             		errmsg("【撤退日期】必須大於【委外日期】");
            		return;
            	}
        	}
        }
        
        if (this.isAdd()) {
            //檢查新增資料是否重複
            String lsSql = "select count(*) as tot_cnt from " + COL_BAD_OUTSOURCE
            		+ " where ID_CORP_P_SEQNO = ? ";
            Object[] param = new Object[] { wp.itemStr("id_corp_p_seqno")};
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("COL_BAD_OUTSOURCE ,資料已存在不可新增");
            }
            return;
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
		sp.sql2Insert(COL_BAD_OUTSOURCE);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("os_cmp_id", wp.itemStr("os_cmp_id"));
		sp.ppstr("os_cmp_no", wp.itemStr("os_cmp_no"));
		sp.ppstr("os_cmp_name", getOsCmpName());
		sp.ppstr("hand_type", wp.itemStr("hand_type"));
		sp.ppstr("os_amt", wp.itemStr("os_amt"));
		sp.ppstr("os_date", wp.itemStr("os_date"));
		sp.ppstr("back_code", wp.itemStr("back_code"));
		sp.addsql(", back_date ", wp.itemEmpty("back_date")? ", NULL" : ", TIMESTAMP_FORMAT( "+wp.itemStr("back_date") + wp.sysTime+",'YYYYMMDDHH24MISS') ");
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_time ", ", sysdate ");
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", wp.sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppint("mod_seqno", 0);
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg("新增 "+COL_BAD_OUTSOURCE+" 錯誤");
			return rc;
		}
		return dbInsertHst();
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update(COL_BAD_OUTSOURCE);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("os_cmp_id", wp.itemStr("os_cmp_id"));
		sp.ppstr("os_cmp_no", wp.itemStr("os_cmp_no"));
		sp.ppstr("os_cmp_name", getOsCmpName());
		sp.ppstr("hand_type", wp.itemStr("hand_type"));
		sp.ppstr("os_amt", wp.itemStr("os_amt"));
		sp.ppstr("os_date", wp.itemStr("os_date"));
		sp.ppstr("back_code", wp.itemStr("back_code"));
		if(!wp.itemEmpty("back_date"))
			sp.addsql(", back_date = TIMESTAMP_FORMAT( "+wp.itemStr("back_date") + wp.sysTime+",'YYYYMMDDHH24MISS') ");
		else 
			sp.addsql(", back_date = NULL");
//		sp.ppstr("crt_user", wp.loginUser); //更新時不覆蓋
//		sp.addsql(", crt_time = sysdate "); //更新時不覆蓋
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", wp.sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ");
		sp.addsql(", mod_seqno = mod_seqno + 1 ");
		sp.sql2Where(" where id_corp_p_seqno = ? ", wp.itemStr("id_corp_p_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg("修改 "+COL_BAD_OUTSOURCE+" 錯誤");
			return rc;
		}
		return dbInsertHst();
	}
	
	public int dbInsertHst() {
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert(COL_BAD_OUTSOURCE_HST);
		sp.ppstr("id_corp_no", wp.itemStr("id_corp_no"));
		sp.ppstr("id_corp_p_seqno", wp.itemStr("id_corp_p_seqno"));
		sp.ppstr("card_flag", wp.itemStr("card_flag"));
		sp.ppstr("os_cmp_id", wp.itemStr("os_cmp_id"));
		sp.ppstr("os_cmp_no", wp.itemStr("os_cmp_no"));
		sp.ppstr("os_cmp_name", getOsCmpName());
		sp.ppstr("hand_type", wp.itemStr("hand_type"));
		sp.ppstr("os_amt", wp.itemStr("os_amt"));
		sp.ppstr("os_date", wp.itemStr("os_date"));
		sp.ppstr("back_code", this.isDelete()?"DEL":wp.itemStr("back_code"));
		sp.addsql(", back_date ", wp.itemEmpty("back_date")? " ,NULL" : ", TIMESTAMP_FORMAT( "+wp.itemStr("back_date") + wp.sysTime+",'YYYYMMDDHH24MISS') ");
		sp.ppstr("crt_user", wp.loginUser);
		sp.addsql(", crt_time ", ", sysdate ");
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", wp.sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppint("mod_seqno", 0);
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg("新增 "+COL_BAD_OUTSOURCE_HST+" 錯誤");
		}
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
	

	private String getOsCmpName() {
		String sqlCmd = "select wf_desc from ptr_sys_idtab where wf_type = 'COLM6200_OS_CMP' and wf_id = :wf_id ";
		setString("wf_id", wp.itemStr("os_cmp_no"));
		sqlSelect(sqlCmd);
		return colStr("wf_desc");
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc != 1) 
			return rc;
		strSql = "delete "+COL_BAD_OUTSOURCE+" where id_corp_p_seqno = ? ";

		Object[] param = new Object[] { wp.itemStr("id_corp_p_seqno") };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg("刪除 "+COL_BAD_OUTSOURCE+" 錯誤");
			return rc;
		}
		return dbInsertHst();
	}
	
	// ************************************************************************
	
} // End of class
