/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-04  V2.00  ryan     acq_bank_id ==>MCC代碼
******************************************************************************/
package ccam02;
 
import busi.FuncAction;
import taroko.com.TarokoCommon;

public class Ccam5310Func extends FuncAction {
  String groupCode = "", acqBankId = "", mchtNo = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      groupCode = wp.itemStr("kk_group_code");
      acqBankId = wp.itemStr("kk_acq_bank_id");
      mchtNo = wp.itemStr("kk_mcht_no");
    } else {
      groupCode = wp.itemStr("group_code");
      acqBankId = wp.itemStr("acq_bank_id");
      mchtNo = wp.itemStr("mcht_no");
    }
    if (empty(groupCode)) {
      errmsg("團體代碼: 不可空白");
      return;
    }
    if(ibAdd){
    	String sqlSelect = "select cca_group_mcht_chk from ptr_group_code where group_code = :group_code";
    	setString("group_code",groupCode);
    	sqlSelect(sqlSelect);
    	String cca_group_mcht_chk = colStr("cca_group_mcht_chk");
    	if(empty(cca_group_mcht_chk)){
    		errmsg("無法新增，團體代碼檔未設定");
    	    return;
    	}
   	    if (empty(acqBankId)) {
	        errmsg("MCC代碼: 不可空白");
	        return;
	    }
  		if (acqBankId.length()>4) {
		    errmsg("MCC代碼: 不可大於4碼");
		    return;
		}
    	if(cca_group_mcht_chk.equals("M")){
    	    if (empty(mchtNo)) {
    	        errmsg("特店代碼: 不可空白");
    	        return;
    	    }
    	}
    	if(cca_group_mcht_chk.equals("C")){
    	    if (!empty(mchtNo)) {
    	        errmsg("不可輸入特店代碼");
    	        return;
    	    }
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

    strSql = "insert into cca_group_mcht (" + " group_code , " + " acq_bank_id , " + " mcht_no , "
        + " crt_date , " + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values (" + " :kk1 , " + " :kk2 , "
        + " :kk3 , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , " + " sysdate, "
        + " :mod_pgm , " + " '1' " + " )";
    // -set ?value-
    setString("kk1", groupCode);
    setString("kk2", acqBankId);
    setString("kk3", mchtNo);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_pgm", "ccam5310");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete cca_group_mcht " + " where group_code =:kk1 " + " and acq_bank_id =:kk2 "
        + " and mcht_no =:kk3";

    setString("kk1", groupCode);
    setString("kk2", acqBankId);
    setString("kk3", mchtNo);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
