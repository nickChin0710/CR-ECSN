/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package mktm02;

import busi.FuncAction;

public class Mktm4080Func extends FuncAction {

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    msgOK();

    String reasonCode = varsStr2("reason_code");
    if ("".equals(reasonCode)) {
    	strSql = " update cyc_afee set " + " reason_code ='M1' , "
    	        + " mod_user =:mod_user , apr_date = '', apr_user = '', "
    	        + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
    	        + " where card_no =:card_no " + " and fee_date =:fee_date ";
    	
    	    setString("mod_user", wp.loginUser);
    	    setString("mod_pgm", wp.modPgm());
    	    var2ParmStr("card_no");
    	    var2ParmStr("fee_date");
    	    
    } else {
    	strSql = " update cyc_afee set " + " reason_code ='' , "
    	        + " mod_user =:mod_user , apr_date = '', apr_user = '', "
    	        + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
    	        + " where card_no =:card_no " + " and fee_date =:fee_date and reason_code='M1' ";
    	
    	    setString("mod_user", wp.loginUser);
    	    setString("mod_pgm", wp.modPgm());
    	    var2ParmStr("card_no");
    	    var2ParmStr("fee_date");
    }

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cyc_afee error !");
    }

    return rc;
  }

}
