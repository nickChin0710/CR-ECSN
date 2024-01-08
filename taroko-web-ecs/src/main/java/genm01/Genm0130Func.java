/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-20  V1.00.00  yash       program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        **                                                                            *
******************************************************************************/
package genm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Genm0130Func extends FuncEdit {

  public Genm0130Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {


    if (this.ibAdd) {
      if (wp.itemStr("hand_user_id").equals(wp.itemStr("hand_manager_id"))) {
        errmsg("人工自由格式:經辦代碼與主管代碼不相同");
        return;
      }
      if (wp.itemStr("r6_user_id").equals(wp.itemStr("r6_manager_id"))) {
        errmsg("R6自動啟帳:經辦代碼與主管代碼不相同");
        return;
      }

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_gen_a002 where 1=1 " + " and  hand_user_id = ? "
              + " and  hand_manager_id = ? " + " and  r6_user_id = ? " + " and  r6_manager_id = ? ";
      Object[] param = new Object[] {wp.itemStr("hand_user_id"), wp.itemStr("hand_manager_id"),
          wp.itemStr("r6_user_id"), wp.itemStr("r6_manager_id")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("ptr_gen_a002");
    sp.ppstr("hand_user_id", wp.itemStr("hand_user_id"));
    sp.ppstr("hand_manager_id", wp.itemStr("hand_manager_id"));
    sp.ppstr("r6_user_id", wp.itemStr("r6_user_id"));
    sp.ppstr("r6_manager_id", wp.itemStr("r6_manager_id"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_ws", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  @Override
  public int dbUpdate() {
    return rc;

  }

  @Override
  public int dbDelete() {
    return rc;
  }

}
