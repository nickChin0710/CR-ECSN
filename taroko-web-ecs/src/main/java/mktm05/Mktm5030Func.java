/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110-11-01  V1.00.04  Ryan       update function dbInsert() dbUpdate()      *
******************************************************************************/
package mktm05;

import busi.FuncAction;

public class Mktm5030Func extends FuncAction {
  String validDate = "", paramType = "";

  @Override
  public void dataCheck() {
    validDate = wp.itemStr("valid_date");
    paramType = wp.itemStr("param_type");

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into mkt_ds_parm1 ( " + " valid_date ," + " param_type ," + " new_card_days ,"
        + " add_card_point ," + " sup_card_point ," + " add_card_max ," + " sup_card_max ,"
        + " promote_type ," + " no_match_point ," + " crt_date ," + " crt_user ," + " apr_flag ,"
        + " apr_date ," + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :kk1 ," + " :kk2 ," + " 0 ,"
        + " :add_card_point ," + " :sup_card_point ," + " :add_card_max ," + " 99999 ,"
        + " '' ," + " :no_match_point ," + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ,"
        + " 'Y' ," + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ,"
        + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("kk1", validDate);
    setString("kk2", paramType);
//    item2ParmNum("new_card_days");
    item2ParmNum("add_card_point");
    item2ParmNum("sup_card_point");
    item2ParmNum("add_card_max");
//    item2ParmNum("sup_card_max");
    item2ParmStr("promote_type");
    item2ParmNum("no_match_point");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_parm1 error !");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update mkt_ds_parm1 set " 
//    	+ " new_card_days =:new_card_days ,"
        + " add_card_point =:add_card_point ," + " sup_card_point =:sup_card_point ,"
        + " add_card_max =:add_card_max ," 
//        + " sup_card_max =:sup_card_max ,"
        + " no_match_point =:no_match_point ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 "
        + " and valid_date =:kk1 " + " and param_type =:kk2 ";

//    item2ParmNum("new_card_days");
    item2ParmNum("add_card_point");
    item2ParmNum("sup_card_point");
    item2ParmNum("add_card_max");
//    item2ParmNum("sup_card_max");
    item2ParmNum("no_match_point");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", validDate);
    setString("kk2", paramType);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update mkt_ds_parm1 error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql =
        " delete mkt_ds_parm1 where 1=1 " + " and valid_date =:kk1 " + " and param_type =:kk2 ";

    setString("kk1", validDate);
    setString("kk2", paramType);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete mkt_ds_parm1 error");
      return rc;
    }

    strSql = " delete mkt_ds_parm1_detl where 1=1 " + " and valid_date =:kk1 "
        + " and param_type =:kk2 ";

    setString("kk1", validDate);
    setString("kk2", paramType);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete mkt_ds_parm1_detl error");
    } else
      rc = 1;

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int deleteAllDetl() {
    msgOK();

    strSql = " delete mkt_ds_parm1_detl where 1=1 " + " and valid_date =:valid_date "
        + " and param_type =:param_type ";

    var2ParmStr("valid_date");
    var2ParmStr("param_type");

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete mkt_ds_parm1_detl error ");
    } else
      rc = 1;

    return rc;
  }

  public int insertDetl() {
    msgOK();

    strSql = " insert into mkt_ds_parm1_detl ( " + " valid_date ," + " param_type ,"
        + " promote_type ," + " data_code1 ," + " data_code2 ," + " data_remark1 ,"
        + " data_remark2 ," + " add_point1 ," + " apr_flag ," + " mod_time ," + " mod_pgm "
        + " ) values ( " + " :valid_date ," + " :param_type ," + " :promote_type ,"
        + " :data_code1 ," + " :data_code2 ," + " :data_remark1 ," + " :data_remark2 ,"
        + " :add_point1 ," + " 'Y' ," + " sysdate ," + " :mod_pgm " + " ) ";

    var2ParmStr("valid_date");
    var2ParmStr("param_type");
    var2ParmStr("promote_type");
    var2ParmStr("data_code1");
    var2ParmStr("data_code2");
    var2ParmStr("data_remark1");
    var2ParmStr("data_remark2");
    var2ParmNum("add_point1");
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_parm1_detl error");
    }

    return rc;
  }

  public int updateDetl() {
    msgOK();

    strSql = " update mkt_ds_parm1_detl set " + " data_remark1 =:data_remark1 , "
        + " add_point1 =:add_point1 , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm "
        + " where 1=1 " + " and valid_date =:valid_date " + " and param_type =:param_type ";

    var2ParmStr("data_remark1");
    var2ParmNum("add_point1");
    setString("mod_pgm", wp.modPgm());
    var2ParmStr("valid_date");
    var2ParmStr("param_type");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update mkt_ds_parm1_detl error ");
    }

    return rc;
  }

}
