package actp01;

import busi.FuncAction;

public class Actp0350Func extends FuncAction {

  @Override
  public void dataCheck() {
    // if(var_eq("apr_flag","Y")){
    // errmsg("此筆已放行不可再調整 !!");
    // return ;
    // }

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
    dataCheck();
    if (rc != 1)
      return rc;

    if (eqIgno(varsStr("apr_flag"), "Y")) {
      strSql = " update act_manu_debit set " + " apr_flag = '' , " + " apr_date = '' , "
          + " apr_user = '' , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
          + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 "
          + commSqlStr.whereRowid(varsStr("rowid")) + " and mod_seqno =:mod_seqno ";
      setString("apr_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
      var2ParmNum("mod_seqno");
    } else {
      strSql = " update act_manu_debit set " + " apr_flag = 'Y' , "
          + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
          + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , "
          + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 " + commSqlStr.whereRowid(varsStr("rowid"))
          + " and mod_seqno =:mod_seqno ";
      setString("apr_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
      var2ParmNum("mod_seqno");
    }


    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update act_manu_debit error ");
      return rc;
    }

    return rc;
  }

}
