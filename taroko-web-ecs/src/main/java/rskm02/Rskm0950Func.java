/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskm0950Func extends FuncAction {
  String adjYymm = "", adjLocFlag = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      adjYymm = wp.itemStr("kk_adj_yymm");
      adjLocFlag = wp.itemStr("kk_adj_loc_flag");
    } else {
      adjYymm = wp.itemStr("adj_yymm");
      adjLocFlag = wp.itemStr("adj_loc_flag");
    }

    if (empty(adjYymm)) {
      errmsg("額度覆核月份: 不可空白");
      return;
    }

    if (empty(adjLocFlag)) {
      errmsg("調額類別: 不可空白");
      return;
    }

    if (ibUpdate) {
      if (wp.itemEmpty("proc_date") == false) {
        errmsg("已處理 , 不可進行修改,需刪除後重新建立");
        return;
      }
    }

    if (wp.itemNum("adj_limit_e1") == 0 && wp.itemNum("adj_limit_e2") == 0
        && wp.itemNum("adj_limit_e3") == 0 && wp.itemNum("adj_limit_e4") == 0
        && wp.itemNum("adj_limit_e5") == 0) {
      errmsg("原額度區間不可全部為 0");
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    sql2Insert("rsk_r001_parm");
    addsqlParm(" ?", "adj_yymm", adjYymm);
    addsqlParm(",?", ", adj_loc_flag", adjLocFlag);
    addsqlParm(",?", ", purch_mm1", wp.colInt("purch_mm1"));
    addsqlParm(",?", ", purch_mm2", wp.colInt("purch_mm2"));
    addsqlParm(",?", ", adj_user1", wp.colStr("adj_user1"));
    addsqlParm(",?", ", adj_user2", wp.colStr("adj_user2"));
    addsqlParm(",?", ", adj_user3", wp.colStr("adj_user3"));
    addsqlParm(",?", ", adj_user4", wp.colStr("adj_user4"));
    addsqlParm(",?", ", adj_user5", wp.colStr("adj_user5"));
    addsqlParm(",?", ", adj_user6", wp.colStr("adj_user6"));
    addsqlParm(",?", ", adj_limit_e1", wp.colNum("adj_limit_e1"));
    addsqlParm(",?", ", adj_limit_e2", wp.colNum("adj_limit_e2"));
    addsqlParm(",?", ", adj_limit_e3", wp.colNum("adj_limit_e3"));
    addsqlParm(",?", ", adj_limit_e4", wp.colNum("adj_limit_e4"));
    addsqlParm(",?", ", adj_limit_e5", wp.colNum("adj_limit_e5"));
    addsqlYmd(", crt_date");
    addsqlParm(",?", ", crt_user", modUser);
    addsqlYmd(", apr_date");
    addsqlParm(",?", ", apr_user", wp.itemStr("approval_user"));
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_r001_parm error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    sql2Update("rsk_r001_parm");
    addsqlParm("purch_mm1 =?", wp.colInt("purch_mm1"));
    addsqlParm(", purch_mm2 =?", wp.colInt("purch_mm2"));
    addsqlParm(", adj_user1 =?", wp.itemStr2("adj_user1"));
    addsqlParm(", adj_user2 =?", wp.itemStr2("adj_user2"));
    addsqlParm(", adj_user3 =?", wp.itemStr2("adj_user3"));
    addsqlParm(", adj_user4 =?", wp.itemStr2("adj_user4"));
    addsqlParm(", adj_user5 =?", wp.itemStr2("adj_user5"));
    addsqlParm(", adj_user6 =?", wp.itemStr2("adj_user6"));
    addsqlParm(", adj_limit_e1 =?", wp.itemNum("adj_limit_e1"));
    addsqlParm(", adj_limit_e2 =?", wp.itemNum("adj_limit_e2"));
    addsqlParm(", adj_limit_e3 =?", wp.itemNum("adj_limit_e3"));
    addsqlParm(", adj_limit_e4 =?", wp.itemNum("adj_limit_e4"));
    addsqlParm(", adj_limit_e5 =?", wp.itemNum("adj_limit_e5"));
    addsql2(", apr_date =" + commSqlStr.sysYYmd);
    addsqlParm(", apr_user =?", wp.itemStr2("approval_user"));
    addsqlModXXX(modUser, modPgm);
    sqlWhere(" where adj_yymm =?", adjYymm);
    sqlWhere(" and adj_loc_flag =?", adjLocFlag);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("update rsk_r001_parm error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete rsk_r001_parm where adj_yymm =:kk1 and adj_loc_flag =:kk2 ";
    setString("kk1", adjYymm);
    setString("kk2", adjLocFlag);
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete rsk_r001_parm error ");
      return rc;
    }

    if (wp.itemEmpty("proc_date") == false) {
      strSql = "delete rsk_r001_data1 where adj_yymm =:kk1 and adj_loc_flag =:kk2 ";
      setString("kk1", adjYymm);
      setString("kk2", adjLocFlag);
      sqlExec(strSql);

      if (sqlRowNum <= 0) {
        errmsg("delete rsk_r001_data1 error ");
        return rc;
      }

    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void dataCopyCheck() {
    String kkAdjYymm = "", kkAdjLocFlag = "", copyAdjUymm = "";
    kkAdjYymm = wp.itemStr("adj_yymm");
    kkAdjLocFlag = wp.itemStr("adj_loc_flag");
    copyAdjUymm = wp.itemStr("copy_adj_yymm");

    String sql1 =
        " select count(*) as db_cnt from rsk_r001_parm where adj_yymm = ? and adj_loc_flag = ? ";
    sqlSelect(sql1, new Object[] {copyAdjUymm, kkAdjLocFlag});

    if (colNum("db_cnt") > 0) {
      errmsg("複製月份資料已存在");
      return;
    }


  }

  public int dataCopy() {
    dataCopyCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into rsk_r001_parm ( " + " adj_yymm , " + " adj_loc_flag , " + " purch_mm1 , "
        + " purch_mm2 , " + " adj_user1 , " + " adj_user2 , " + " adj_user3 , " + " adj_user4 , "
        + " adj_user5 , " + " adj_user6 , " + " adj_limit_e1 , " + " adj_limit_e2 , "
        + " adj_limit_e3 , " + " adj_limit_e4 , " + " adj_limit_e5 , " + " crt_date , "
        + " crt_user , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " :adj_yymm , " + " :adj_loc_flag , " + " :purch_mm1 , "
        + " :purch_mm2 , " + " :adj_user1 , " + " :adj_user2 , " + " :adj_user3 , "
        + " :adj_user4 , " + " :adj_user5 , " + " :adj_user6 , " + " :adj_limit_e1 , "
        + " :adj_limit_e2 , " + " :adj_limit_e3 , " + " :adj_limit_e4 , " + " :adj_limit_e5 , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " 1 " + " ) ";

    item2ParmStr("adj_yymm", "copy_adj_yymm");
    item2ParmStr("adj_loc_flag");
    item2ParmNum("purch_mm1");
    item2ParmNum("purch_mm2");
    item2ParmStr("adj_user1");
    item2ParmStr("adj_user2");
    item2ParmStr("adj_user3");
    item2ParmStr("adj_user4");
    item2ParmStr("adj_user5");
    item2ParmStr("adj_user6");
    item2ParmNum("adj_limit_e1");
    item2ParmNum("adj_limit_e2");
    item2ParmNum("adj_limit_e3");
    item2ParmNum("adj_limit_e4");
    item2ParmNum("adj_limit_e5");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("copy rsk_r001_parm error ");
    }

    return rc;
  }

}
