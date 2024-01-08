/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-05-31  V1.00.00  Ryan                                                  *
* 112-07-27  V1.00.01  Ryan        增加三個欄位                                                                                             *
******************************************************************************/
package cycm01;

import busi.FuncAction;

public class Cycm0070Func extends FuncAction {
  String correlateId = "" , crtDate = "", pSeqno = "";
  String corpPSeqno = "";
  String acctType = "";
  @Override
  public void dataCheck() {
    if (ibAdd) {
    	correlateId = wp.itemStr("kk_correlate_id");
    	crtDate = wp.sysDate;
    } else {
    	correlateId = wp.itemStr("correlate_id");
    	crtDate = wp.itemStr("crt_date");
    }

    if (empty(correlateId)) {
      errmsg("利害關係人 ID : 不可空白 ");
      return;
    }
    
    if(this.ibAdd) {
    	if(correlateId.length() == 8) {
        	String sqlSelect = "SELECT CORP_P_SEQNO FROM CRD_CORP WHERE CORP_NO = :CORP_NO ";
        	setString("CORP_NO",correlateId);
        	sqlSelect(sqlSelect);
        	if(sqlRowNum > 0) {
        		corpPSeqno = colStr("acno_p_seqno");
        		acctType = "03";
            }else {
                errmsg("查無此統一編號 ");
                return;
            }
    	}
    	if(correlateId.length() != 8) {
    		String sqlSelect = "SELECT p_seqno FROM ACT_ACNO WHERE acct_type = '01' AND acct_key = :acct_key and corp_p_seqno = '' ";
            setString("acct_key", correlateId.length() == 10 ? correlateId + "0" : correlateId);
            sqlSelect(sqlSelect);
            if(sqlRowNum > 0) {
            	pSeqno = colStr("p_seqno");
            	acctType = "01";
            }else {
            	  errmsg("查無此身分證號 ");
                  return;
            }
    	}
    }
    
    
	if (this.isAdd()) {
		// 檢查新增資料是否重複
		String lsSql = "select count(*) as tot_cnt from CRD_CORRELATE where CRT_DATE = ? and CORRELATE_ID = ? and decode(correlate_id_code,'','0',correlate_id_code) = ? ";
		Object[] param = new Object[] { crtDate,correlateId,"0" };
		sqlSelect(lsSql, param);
		if (colNum("tot_cnt") > 0) {
			errmsg("資料已存在，無法新增");
			return;
		}
	} else {
		// -other modify-
		sqlWhere = " where CRT_DATE = ? and CORRELATE_ID = ? and decode(correlate_id_code,'','0',correlate_id_code) = ? ";
		Object[] param = new Object[] {crtDate,correlateId,"0" };
		if (isOtherModify("CRD_CORRELATE", sqlWhere, param)) {
			errmsg("請重新查詢");
			return;
		}
	}
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into CRD_CORRELATE ( " + " CRT_DATE , " + " CORRELATE_ID , " + " CORRELATE_ID_CODE , "
        + " BK_FLAG , " + " FH_FLAG , " + " NON_ASSET_BALANCE , " + " P_SEQNO , "
        + " NON_CREDIT_AMT ," + "ACCT_TYPE , " + "CORP_P_SEQNO , " + " RELATE_STATUS , " + " mod_user , " + " mod_time , "
        + " mod_pgm  " + ") values ( " + " :CRT_DATE , " + " :CORRELATE_ID , "
        + " :CORRELATE_ID_CODE , " + " :BK_FLAG , " + " :FH_FLAG , " + " :NON_ASSET_BALANCE , "
        + " :P_SEQNO , " + " :NON_CREDIT_AMT , " + " :ACCT_TYPE , " + " :CORP_P_SEQNO , " + " :RELATE_STATUS , "
        + " :mod_user , " + " sysdate , " + " :mod_pgm  "
        + " ) ";

    setString("CRT_DATE", crtDate);
    setString("CORRELATE_ID" ,correlateId);
    setString("CORRELATE_ID_CODE" ,"0");
    setString("BK_FLAG" ,wp.itemNvl("BK_FLAG","N"));
    setString("FH_FLAG" ,wp.itemNvl("FH_FLAG","N"));
    setDouble("NON_ASSET_BALANCE" ,wp.itemNum("NON_ASSET_BALANCE"));
    setString("P_SEQNO" ,pSeqno);
    setDouble("NON_CREDIT_AMT" ,wp.itemNum("NON_CREDIT_AMT"));
    setString("ACCT_TYPE" ,acctType);
    setString("CORP_P_SEQNO" ,corpPSeqno);
    setString("RELATE_STATUS" ,wp.itemStr("RELATE_STATUS"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cycm0070");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert CRD_CORRELATE error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update CRD_CORRELATE set " + " BK_FLAG =:BK_FLAG , "
        + " FH_FLAG =:FH_FLAG , " + " NON_ASSET_BALANCE =:NON_ASSET_BALANCE , "
        + " NON_CREDIT_AMT =:NON_CREDIT_AMT , "
        + " RELATE_STATUS = :RELATE_STATUS , "
        + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm "
        + " where CRT_DATE =:CRT_DATE and CORRELATE_ID = :CORRELATE_ID and decode(correlate_id_code,'','0',correlate_id_code) = :CORRELATE_ID_CODE ";

    setString("BK_FLAG", wp.itemNvl("BK_FLAG","N"));
    setString("FH_FLAG", wp.itemNvl("FH_FLAG","N"));
    setDouble("NON_ASSET_BALANCE", wp.itemNum("NON_ASSET_BALANCE"));
    setDouble("NON_CREDIT_AMT", wp.itemNum("NON_CREDIT_AMT"));
    setString("RELATE_STATUS" ,wp.itemStr("RELATE_STATUS"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cycm0070");
    setString("CRT_DATE", crtDate);
    setString("CORRELATE_ID", correlateId);
    setString("CORRELATE_ID_CODE", "0");
    
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update CRD_CORRELATE error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "delete CRD_CORRELATE where CRT_DATE =:CRT_DATE and CORRELATE_ID = :CORRELATE_ID and decode(correlate_id_code,'','0',correlate_id_code) = :CORRELATE_ID_CODE ";
    setString("CRT_DATE", wp.itemStr("CRT_DATE"));
    setString("CORRELATE_ID", correlateId);
    setString("CORRELATE_ID_CODE", "0");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete CRD_CORRELATE error ! err:" + getMsg());
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
