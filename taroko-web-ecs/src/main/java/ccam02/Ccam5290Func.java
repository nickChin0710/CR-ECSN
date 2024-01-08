/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncAction;
import taroko.com.TarokoCommon;
	
public class Ccam5290Func extends FuncAction {
  String acqId = "", mchtNo = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      acqId = wp.itemStr("kk_acq_id");
      mchtNo = wp.itemStr("kk_mcht_no");
    } else {
      acqId = wp.itemStr("acq_id");
      mchtNo = wp.itemStr("mcht_no");
    }

    if (empty(acqId)) {
      errmsg("收單行代碼: 不可空白");
      return;
    }

    if (empty(mchtNo)) {
      errmsg("特店代號: 不可空白");
      return;
    }

    if (ibDelete)
      return;

    if (wp.itemEmpty("mcht_name")) {
      errmsg("特店名稱:不可空白");
      return;
    }

    if (wp.itemEmpty("online_date")) {
      errmsg("上線日期:不可空白");
      return;
    }

    if (wp.itemEmpty("stop_date")) {
      errmsg("下線日期:不可空白");
      return;
    }

    if (wp.itemNum("online_date") < commString.strToNum(getSysDate())) {
      errmsg("上線日期不可小於系統日");
      return;
    }

    if (wp.itemNum("stop_date") < commString.strToNum(getSysDate())) {
      errmsg("下線日期不可小於系統日");
      return;
    }

    if (wp.itemNum("stop_date") < wp.itemNum("online_date")) {
      errmsg("下線日期不可小於上線日期");
      return;
    }

    if (this.ibAdd) {
      if (checkMcht() == false) {
        errmsg("資料已存在 , 不可新增");
        return;
      }
    }


  }

  boolean checkMcht() {
    String sql1 =
        " select " + " count(*) as db_cnt " + " from cca_mcht_notonline " + " where mcht_no = ? ";

    sqlSelect(sql1, new Object[] {mchtNo});

    if (colNum("db_cnt") > 0) {
      return false;
    }
    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    deleteNotOnline();
    insertNotOnline();
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    deleteNotOnline();
    insertNotOnline();
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete cca_mcht_notonline " + " where acq_id =:kk1 " + " and mcht_no =:kk2";
    // ddd("del-sql="+is_sql);
    setString("kk1", acqId);
    setString("kk2", mchtNo);

    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int deleteNotOnline() {
    dataCheck();
    strSql = "delete cca_mcht_notonline " + " where acq_id =:kk1 " + " and mcht_no =:kk2"
        + " and apr_flag<>'Y' ";
    // ddd("del-sql="+is_sql);
    setString("kk1", acqId);
    setString("kk2", mchtNo);

    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

  public int insertNotOnline() {
    strSql = "insert into cca_mcht_notonline (" + " acq_id , " + " mcht_no , " + " apr_flag , "
        + " mcht_name , " + " online_date , " + " stop_date , " + " crt_date , " + " crt_user , "
        + " data_from , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values (" + " :kk1 , " + " :kk2 , " + " 'N' , " + " :mcht_name , "
        + " :online_date , " + " :stop_date , " + " to_char(sysdate,'yyyymmdd') , "
        + " :crt_user , " + " '1' , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 "
        + " )";
    // -set ?value-
    setString("kk1", acqId);
    setString("kk2", mchtNo);
    item2ParmStr("mcht_name");
    item2ParmStr("online_date");
    item2ParmStr("stop_date");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5290");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int insertNotOnline2() {
    strSql = "insert into cca_mcht_notonline (" + " acq_id , " + " mcht_no , " + " apr_flag , "
        + " mcht_name , " + " online_date , " + " stop_date , " + " crt_date , " + " crt_user , "
        + " data_from , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values (" + " :acq_id , " + " :mcht_no , " + " 'N' , " + " :mcht_name , "
        + " :online_date , " + " :stop_date , " + " to_char(sysdate,'yyyymmdd') , "
        + " :crt_user , " + " '2' , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 "
        + " )";
    // -set ?value-
    item2ParmStr("acq_id");
    item2ParmStr("mcht_no");
    item2ParmStr("mcht_name");
    item2ParmStr("online_date");
    item2ParmStr("stop_date");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5290");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int deleteNotOnline2() {
    dataCheck();
    strSql = "delete cca_mcht_notonline " + " where acq_id =:acq_id " + " and mcht_no =:mcht_no"
        + " and apr_flag<>'Y'" + " and data_from='2' ";
    // ddd("del-sql="+is_sql);
    item2ParmStr("acq_id");
    item2ParmStr("mcht_no");

    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

}
