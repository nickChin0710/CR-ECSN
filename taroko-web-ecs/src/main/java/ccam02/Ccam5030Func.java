
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-29  V1.00.01  ryan       program initial                            *
* 109-11-26  V1.00.02  ryan       調整dataCheck 端末機號必需為4碼                                           *
* 110-11-07  V1.00.03  Alex       調整dataCheck 端末機號必需為4碼或8碼                                     *
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;


public class Ccam5030Func extends FuncEdit {
  String kkMccCode = "" , kkMccLinkId = "";

  public Ccam5030Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
    	kkMccCode = wp.itemStr("kk_mcc_code");
    	kkMccLinkId = wp.itemStr("kk_mcc_link_id");
    } else {
    	kkMccCode = wp.itemStr("mcc_code");
    	kkMccLinkId = wp.itemStr("mcc_link_id");
    }
    
    if (empty(kkMccCode)) {
    	errmsg("MCC CODE 不可空白");
        return;
    }
      
    if(kkMccLinkId.trim().length()!=4 && kkMccLinkId.trim().length()!=8) {
    	errmsg("端末機號必需為4碼或8碼");
      	return ;
    }

    if(!this.isDelete()){
    	if(wp.itemNum("int_min_amt")>wp.itemNum("int_max_amt")){
    		errmsg("手續費收取最低金額 不可高於 手續費收取最高金額");
          	return ;
    	}
    	
    	if(wp.itemNum("int_max_amt")<wp.itemNum("int_fix_amt")) {
    		errmsg("手續費收取最高金額 不可以低於  交易手續費");
    		return ;
    	}
    	
    	if(wp.itemNum("int_percent")>1000) {
    		errmsg("交易收取百分比不可大於等於 1000%");
    		return ;
    	}
    	
    	if(wp.itemNum("int_percent")*100 % 1 != 0) {
    		errmsg("交易收取百分比只可以輸入至小數點2位");
    		return ;
    	}
    	
    }
    
    if (this.isAdd()) {
    	String lsSql = "select count(*) as tot_cnt from cca_mcc_risk where mcc_code = ? ";
		Object[] param = new Object[] {kkMccCode};
		sqlSelect(lsSql, param);
		if (colNum("tot_cnt") <= 0) {
			errmsg("mcc code不存在，無法新增");
			return;
		}
    	
    	// 檢查新增資料是否重複
		lsSql = "select count(*) as tot_cnt from cca_mcc_egov_fee where mcc_code = ? and mcc_link_id= ? ";
		param = new Object[] {kkMccCode, kkMccLinkId};
		sqlSelect(lsSql, param);
		if (colNum("tot_cnt") > 0) {
			errmsg("資料已存在，無法新增,請從新查詢");
			return;
		}
    }else{
        // -other modify-
        sqlWhere = " where mcc_code= ? and mcc_link_id= ? and nvl(mod_seqno,0) =? ";
        Object[] parms = new Object[] {kkMccCode, kkMccLinkId, wp.modSeqno()};
        if (this.isOtherModify("cca_mcc_egov_fee", sqlWhere, parms)) {
        	wp.log(sqlWhere, parms);
        	return;
        }
    }
  }
 

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into cca_mcc_egov_fee ( " 
    		+ " mcc_code "
			+ ", mcc_link_id "
			+ ", mcc_remark "
			+ ", int_min_amt "
			+ ", int_max_amt "
			+ ", int_fix_amt "
			+ ", int_percent "
			+ ", cntry_code "
			+ ", crt_date "
			+ ", crt_user "
			+ ", apr_date "
			+ ", apr_user "
			+ ", mod_user "
			+ ", mod_time "
			+ ", mod_pgm "
			+ ", mod_seqno "
    	+ " ) values ( "
        + " ?,?,?,?,?,?,?,? " 
    	+ " ,to_char(sysdate,'yyyymmdd'),? " 
    	+ " ,to_char(sysdate,'yyyymmdd'),? " 
        + " ,?,sysdate,?,1 " 
    	+ " ) ";
    Object[] param = new Object[] {
    	  kkMccCode // 1
        , kkMccLinkId
        , wp.itemStr("mcc_remark")
        , wp.itemNum("int_min_amt")
        , wp.itemNum("int_max_amt")
        , wp.itemNum("int_fix_amt")
        , wp.itemNum("int_percent")
        , wp.itemStr("cntry_code")
        , wp.loginUser
        , wp.itemStr("approval_user")
        , wp.loginUser
        , wp.itemStr("mod_pgm")
     };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
    	errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) { 
      return rc;
    }
    strSql = "update cca_mcc_egov_fee set " 
			+ " mcc_remark = ? "
			+ ", int_min_amt = ? "
			+ ", int_max_amt = ? "
			+ ", int_fix_amt = ? "
			+ ", int_percent = ? "
			+ ", cntry_code = ? "
			+ ", apr_date = to_char(sysdate,'yyyymmdd') "
			+ ", apr_user = ? "
    		+ ", mod_user = ?"
    		+ ", mod_time = sysdate"
    		+ ", mod_pgm =? "
    		+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
    		+ sqlWhere;
    Object[] param = new Object[] {
    	  wp.itemStr("mcc_remark")
    	, wp.itemNum("int_min_amt")
    	, wp.itemNum("int_max_amt") 
    	, wp.itemNum("int_fix_amt")
    	, wp.itemNum("int_percent")
    	, wp.itemStr("cntry_code")
    	, wp.itemStr("approval_user")
    	, wp.loginUser
    	, wp.itemStr("mod_pgm")
    	, kkMccCode
    	, kkMccLinkId
    	, wp.modSeqno()
    };
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
    	errmsg(this.sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete cca_mcc_egov_fee " + sqlWhere;
    Object[] parms = new Object[] {kkMccCode, kkMccLinkId, wp.modSeqno()};
    rc = sqlExec(strSql, parms);
    if (sqlRowNum <= 0) {
    	errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
