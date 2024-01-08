/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/02/28  V1.00.01   machao      Initial         
* 112/03/05  V1.00.02   machao      程式調整：bug修訂                            *    * 
* 112/05/10  V1.00.03   Zuwei Su    程式調整：bug修訂                  *
* 112/05/12  V1.00.04   Zuwei Su    dataSelect 異常                 *
* 112/05/17  V1.00.05   Ryan        活動新增擇一回饋, 提供參數設定         
* 112-06-06  V1.00.06   machao      活動群組增 ‘群組月累績最低消費金額’         *
* 112-08-01  V1.00.07   Zuwei Su    insert/update前check limit_amt和sum_amt是否為空，為空置0         *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0851Func extends FuncEdit {
  private final String PROGNAME = "行銷通路活動群組設定檔維護112/02/28 V1.00.01";
  String mchtGroupId;
  String orgControlTabName = "MKT_CHANNELGP_PARM";
  String controlTabName = "MKT_CHANNELGP_PARM_T";

  public Mktm0851Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    String procTabName;
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " 
            + "active_group_id,"
            + "active_group_desc,"
            + "sum_amt,"
            + "feedback_type,"
            + "limit_amt,"
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mchtGroupId = wp.itemStr("active_group_id");
    } else {
      mchtGroupId = wp.itemStr("active_group_id");
    }
    if (wp.respHtml.indexOf("_nadd") > 0) {
      if (this.ibAdd)
        if (mchtGroupId.length() > 0) {
          strSql = "select count(*) as qua " + "from " + orgControlTabName + " where active_group_id = ? ";
          Object[] param = new Object[]{mchtGroupId};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[活動群組代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }
    }

    if (this.ibAdd)
      if (mchtGroupId.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where active_group_id = ? ";
        Object[] param = new Object[] {mchtGroupId};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[活動群組代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibUpdate) {
      strSql = "select count(*) as qua from MKT_CHANNELGP_PARM_T where active_group_id = ?";
      Object[] param1 = new Object[] {wp.itemStr("active_group_id")};
      sqlSelect(strSql, param1);
      int qua1 = Integer.parseInt(colStr("qua"));
      if (qua1 == 0) {
        errmsg("活動群組代碼不存在，不可更改!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemEmpty("active_group_desc")) {
        errmsg("活動群組說明： 不可空白");
        return;
      }

      if (wp.itemEmpty("feedback_type")) {
        errmsg("回饋類別： 不可空白");
        return;
      }

      if (wp.itemEq("feedback_type", "2")) {
        if (wp.itemEmpty("limit_amt")) {
          errmsg("回饋上限總金額： 不可空白");
          return;
        }
      }
      
      if (wp.itemEmpty("limit_amt")) {
          wp.itemSet("limit_amt", "0");
      }
      if (wp.itemEmpty("sum_amt")) {
          wp.itemSet("sum_amt", "0");
      }
    }
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  "
            + controlTabName
            + " ("
            + " active_group_id, "
            + " active_group_desc, "
            + " sum_amt, "
            + " feedback_type, "
            + " limit_amt, "
            + " aud_type, "
            + " crt_date, "
            + " crt_user, "
            + " mod_seqno, "
            + " mod_time,"
            + "mod_user,"
            + "mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "sysdate,?,?)";

    Object[] param = new Object[] {
            mchtGroupId,
            wp.itemStr("active_group_desc"),
            wp.itemStr("sum_amt"),
            wp.itemStr("feedback_type"),
            wp.itemNum("limit_amt"),
            wp.itemStr("aud_type"),
            wp.loginUser,
            wp.modSeqno(),
            wp.loginUser,
            wp.modPgm()
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();
//    mkt_mcht_gp

    strSql = "insert into MKT_CHANNELGP_DATA_T " + "select * " + "from MKT_CHANNELGP_DATA "
        + "where active_group_id = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_group_id"),};

    sqlExec(strSql, param);


    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

  strSql = "update "
          + controlTabName
          + " set "
          + "active_group_desc = ?, "
          + "sum_amt = ?, "
          + "feedback_type = ?, "
          + "limit_amt = ?, "
          + "crt_user  = ?, "
          + "crt_date  = to_char(sysdate,'yyyymmdd'), "
          + "mod_user  = ?, "
          + "mod_seqno = nvl(mod_seqno,0)+1, "
          + "mod_time  = sysdate, "
          + "mod_pgm   = ? "
          + "where rowid = ? "
          + "and   mod_seqno = ? ";

  Object[] param = new Object[] {
          wp.itemStr("active_group_desc"),
          wp.itemStr("sum_amt"),
          wp.itemStr("feedback_type"),
          wp.itemNum("limit_amt"),
          wp.loginUser,
          wp.loginUser,
          wp.itemStr("mod_pgm"),
          wp.itemRowId("rowid"),
          wp.itemNum("mod_seqno")
  };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
//    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_CHANNELGP_DATA_T " + " where  active_group_id = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_group_id"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_CHANNELGP_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();
    strSql = "insert into MKT_CHANNELGP_DATA_T ( " + "active_group_id, " + "active_code,"
        + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "?,?," 
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] { wp.itemStr("active_group_id"), varsStr("active_code"),
            wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_MCHTGP_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
	  msgOK();
	// 如果沒有資料回傳成功
	Object[] param = new Object[] { wp.itemStr("active_group_id"),varsStr("active_code"),};
    	if (sqlRowcount("mkt_channelgp_data_t",
        "where active_group_id = ? " + "and   active_code = ? " ,
        param) <= 0)
      return 1;

    strSql = "delete mkt_channelgp_data_t " + "where active_group_id = ? " + "and   active_code = ?  ";
    sqlExec(strSql, param);


    return 1;
    
//    msgOK();
//
//    //如果沒有資料回傳成功2
//    Object[] param = new Object[]
//      {
//      };
//    if (sqlRowcount("mkt_channelgp_data_t" 
//                     , "where active_group_id = '"+wp.itemStr("active_group_id")+"' "
//                     , param) <= 0)
//        return 1;
//
//    strSql = "delete mkt_channelgp_data_t "
//           + "where active_group_id = '"+ wp.itemStr("active_group_id")+"' "
//           ;
//    sqlExec(strSql,param);
//
//
//    return 1;

  }

  // ************************************************************************

} // End of class
