/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-2-13  V1.00.01   Ryan                                                  * 
******************************************************************************/
package ipsm01;

import busi.FuncAction;

public class Ipsm0005Func extends FuncAction {
	 String kkBankNo = "", kkCardKind = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
    	kkBankNo = wp.itemStr("kk_bank_no");
    	kkCardKind = wp.itemStr("kk_card_kind");

    } else {
    	kkBankNo = wp.itemStr("bank_no");
    	kkCardKind = wp.itemStr("card_kind");
    }
    if (this.isAdd()) {
        // 檢查新增資料是否重複
        
         String lsSql = "select count(*) as tot_cnt from IPS_CARD_SEQNO where BANK_NO = ? and CARD_KIND = ? "; 
         Object[] param = new Object[] {kkBankNo,kkCardKind}; 
         sqlSelect(lsSql, param);
         if (colNum("tot_cnt") > 0) 
         { 
        	 errmsg("資料已存在，無法新增,請從新查詢");
        	 return; 
         }
         

      } else {
        // -other modify-
        sqlWhere = " where BANK_NO = ? and CARD_KIND = ? " ;
        Object[] param = new Object[] {kkBankNo,kkCardKind};
        if (this.isOtherModify("IPS_CARD_SEQNO", sqlWhere, param)) {
          errmsg("請重新查詢 !");
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

    strSql = " insert into IPS_CARD_SEQNO ( BANK_NO," + " CARD_KIND,"+ " SEQ_NO ," 
        + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ,"
        + " mod_user ," + " mod_time ," + " mod_pgm " 
        + " ) values (?,?,?, "
        + " to_char(sysdate,'yyyymmdd') ," + " ? ," + " to_char(sysdate,'yyyymmdd') ,"
        + " ? ," + " ? ," + " sysdate ," + " ? ) ";
    
    Object[] param = new Object[] {
    		kkBankNo,kkCardKind
    		,wp.itemStr("SEQ_NO")
    		,wp.loginUser
    		,wp.loginUser
    		,wp.loginUser
    		,wp.modPgm()
    		};

    sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg("insert IPS_CARD_SEQNO error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql =
        " update IPS_CARD_SEQNO set " + " SEQ_NO = ? , " 
            + " apr_date =to_char(sysdate,'yyyymmdd') , " + " apr_user =? , "
            + " mod_time = sysdate , " + " mod_user =? , " + " mod_pgm =? ";
          
    strSql += sqlWhere;
    Object[] param = new Object[] {
    		wp.itemStr("SEQ_NO")
    		,wp.loginUser
    		,wp.loginUser
    		,wp.modPgm()
    		,kkBankNo,kkCardKind};
    sqlExec(strSql,param);

    if (sqlRowNum <= 0) {
      errmsg("update IPS_CARD_SEQNO error !");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete IPS_CARD_SEQNO ";
    strSql += sqlWhere;
    Object[] param = new Object[] {kkBankNo,kkCardKind};
    sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg("delete IPS_CARD_SEQNO error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
