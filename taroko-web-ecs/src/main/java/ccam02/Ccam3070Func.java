
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncAction;

public class Ccam3070Func extends FuncAction {
  String mchtNo = "", bankId = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mchtNo = wp.itemStr("kk_mcht_no");
      bankId = wp.itemStr("kk_acq_bank_id");
    } else {
      mchtNo = wp.itemStr("mcht_no");
      bankId = wp.itemStr("acq_bank_id");
    }
    if (empty(mchtNo)) {
      errmsg("特店代碼:不可空白");
      return;
    }
    if (empty(bankId)) {
      errmsg("收單行代碼:不可空白");
      return;
    }
    if (this.ibAdd)
      return;

    sqlWhere = " where mcht_no= ? " + " and acq_bank_id = ? " + " and nvl(mod_seqno,0) = ? " ;
    Object[] parms = new Object[] {mchtNo, bankId, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_mcht_bill", sqlWhere,parms)) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into CCA_MCHT_BILL (" + " mcht_no , " + " acq_bank_id , " + " mcht_name , "
        + " zip_code , " + " zip_city , " + " mcht_addr , " + " mcht_remark , " + " mcc_code , "
        + " mcht_eng_name , bin_type ," + " crt_user , " + " crt_date , " + " crt_time , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values (" + " :kk1 , " + " :kk2 , "
        + " :mcht_name , " + " :zip_code , " + " :zip_city , " + " :mcht_addr , "
        + " :mcht_remark , " + " :mcc_code , " + " :mcht_eng_name , :bin_type ," + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :mod_user , "
        + " sysdate , " + " :mod_pgm , " + " '1' " + " )";
    // -set ?value-
    setString("kk1", mchtNo);
    setString("kk2", bankId);
    item2ParmStr("mcht_name");
    item2ParmStr("zip_code");
    item2ParmStr("zip_city");
    item2ParmStr("mcht_addr");
    item2ParmStr("mcht_remark");
    item2ParmStr("mcc_code");
    item2ParmStr("mcht_eng_name");
    item2ParmStr("bin_type");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam3070");
    sqlExec(strSql);
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

    strSql = "update CCA_MCHT_BILL set " + " mcht_name =:mcht_name ,"
        + " mcht_eng_name =:mcht_eng_name ," + " zip_code =:zip_code ," + " zip_city =:zip_city ,"
        + " mcht_addr =:mcht_addr ," + " mcht_remark =:mcht_remark ," + " mcc_code =:mcc_code , bin_type =:bin_type ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 " + " and mcht_no =:kk1 "
        + " and acq_bank_id =:kk2 "// 13
        + " and nvl(mod_seqno,0)=:mod_seqno"// 14
    ;

    item2ParmStr("mcht_name");
    item2ParmStr("mcht_eng_name");
    item2ParmStr("zip_code");
    item2ParmStr("zip_city");
    item2ParmStr("mcht_addr");
    item2ParmStr("mcht_remark");
    item2ParmStr("mcc_code");
    item2ParmStr("bin_type");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam3070");
    item2ParmNum("mod_seqno");
    setString("kk1", mchtNo);
    setString("kk2", bankId);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
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
    strSql = "delete CCA_MCHT_BILL " + " where 1=1 " + " and mcht_no =:kk1 "
        + " and acq_bank_id =:kk2 " + " and nvl(mod_seqno,0)=:mod_seqno";

    setString("kk1", mchtNo);
    setString("kk2", bankId);
    item2ParmNum("mod_seqno");
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
