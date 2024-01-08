/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package mktm05;

import busi.FuncAction;

public class Mktm5040Func extends FuncAction {
  String validDate = "", paramType = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      validDate = wp.itemStr("valid_date");
      paramType = wp.itemStr("param_type");
    } else {
      validDate = wp.itemStr("valid_date");
      paramType = wp.itemStr("param_type");
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into mkt_ds_parm1 ( " + " valid_date ," + " param_type ,"
        + " sales_hire_day ," + " cut_card_mon ," + " sales_amt_max ," + " season_point ,"
        + " season_amt ," + " alive_cond_pct ," + " alive_amt_pct ," + " crt_date ," + " crt_user ,"
        + " apr_flag ," + " apr_date ," + " apr_user ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values ( " + " :kk1 ," + " :kk2 ,"
        + " :sales_hire_day ," + " :cut_card_mon ," + " :sales_amt_max ," + " :season_point ,"
        + " :season_amt ," + " :alive_cond_pct ," + " :alive_amt_pct ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " 'Y' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    setString("kk1", validDate);
    setString("kk2", paramType);
    item2ParmNum("sales_hire_day");
    item2ParmNum("cut_card_mon");
    item2ParmNum("sales_amt_max");
    item2ParmNum("season_point");
    item2ParmNum("season_amt");
    item2ParmNum("alive_cond_pct");
    item2ParmNum("alive_amt_pct");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
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

    strSql = " update mkt_ds_parm1 set " + " sales_hire_day =:sales_hire_day ,"
        + " cut_card_mon =:cut_card_mon ," + " sales_amt_max =:sales_amt_max ,"
        + " season_point =:season_point ," + " season_amt =:season_amt ,"
        + " alive_cond_pct =:alive_cond_pct ," + " alive_amt_pct =:alive_amt_pct ,"
        + " apr_user =:apr_user , " + " apr_flag ='Y' , "
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where 1=1 " + " and valid_date =:kk1 " + " and param_type =:kk2 "
        + " and apr_flag ='Y' ";

    item2ParmNum("sales_hire_day");
    item2ParmNum("cut_card_mon");
    item2ParmNum("sales_amt_max");
    item2ParmNum("season_point");
    item2ParmNum("season_amt");
    item2ParmNum("alive_cond_pct");
    item2ParmNum("alive_amt_pct");
    setString("apr_user", wp.itemStr("approval_user"));
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
    /*
     * actionInit("D"); dataCheck(); if(rc!=1) return rc;
     * 
     * is_sql = " delete mkt_ds_parm1 where 1=1 " + " and valid_date =:kk1 " +
     * " and param_type =:kk2 " + " and apr_flag = 'N' " ;
     * 
     * setString("kk1",kk1); setString("kk2",kk2);
     * 
     * sqlExec(is_sql);
     * 
     * if(sql_nrow<=0){ errmsg("delete mkt_ds_parm1 error"); return rc; }
     * 
     * is_sql = " delete mkt_ds_parm1_detl where 1=1 " + " and valid_date =:kk1 " +
     * " and param_type =:kk2 " + " and apr_flag = 'N' " ;
     * 
     * setString("kk1",kk1); setString("kk2",kk2);
     * 
     * sqlExec(is_sql); if(sql_nrow<0){ errmsg("delete mkt_ds_parm1_detl error"); } else rc =1;
     */
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
        + " and param_type =:param_type " + " and apr_flag = 'Y' ";

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
        + " promote_type ," + " data_code1 ," + " data_remark1 ," + " point_amt ," + " apr_flag ,"
        + " mod_time ," + " mod_pgm " + " ) values ( " + " :valid_date ," + " :param_type ,"
        + " :promote_type ," + " :data_code1 ," + " :data_remark1 ," + " :point_amt ," + " 'Y' ,"
        + " sysdate ," + " :mod_pgm " + " ) ";

    var2ParmStr("valid_date");
    var2ParmStr("param_type");
    var2ParmStr("promote_type");
    var2ParmStr("data_code1");
    var2ParmStr("data_remark1");
    var2ParmNum("point_amt");
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_parm1_detl error");
    }

    return rc;
  }

  public int updateDetl() {
    msgOK();

    strSql = " update mkt_ds_parm1_detl set " + " data_code1 =:data_code1 , "
        + " data_remark1 =:data_remark1 , " + " point_amt =:point_amt , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm " + " where 1=1 " + " and valid_date =:valid_date "
        + " and param_type =:param_type " + " and apr_flag ='Y' ";

    var2ParmStr("data_code1");
    var2ParmStr("data_remark1");
    var2ParmNum("point_amt");
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
