/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-08-11  V1.00.02  JustinWu     add dbInsertA05Detl                                      *
* 109-08-18  V1.00.03 JustinWu      remove useless code
* 109-12-24  V1.00.04 JustinWu     parameterize sql 
******************************************************************************/
package ptrm02;
/*V.2018-0117
 * 2018-0117:	JH		modify
 * */

import java.util.ArrayList;

import busi.FuncEdit;


public class Ptrm0480Func extends FuncEdit {
  //String kk1 = "", kk2 = "", kk3 = "", kk4 = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {

    sqlWhere = " where 1=1" + " and nvl(mod_seqno,0) = ? " ;
    if (eqIgno(wp.itemStr("apr_flag"), "Y")) {
      sqlWhere += " and apr_flag='Y'";
    } else {
      sqlWhere += " and nvl(apr_flag,'N')<>'Y'";
    }
    if (this.isOtherModify("ptr_vip_cancel", sqlWhere, new Object[] {wp.modSeqno()})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    if (wp.itemEq("apr_flag", "Y") == true) {
      insertPtrVipCancel();
    }
    if (wp.itemEq("apr_flag", "N") == true) {
      deletePtrVipCancel();
      insertPtrVipCancel();
    }

    return rc;
  }

  public int copyMaster2unApr(String a_type) {
    strSql =
        "select count(*) as db_cnt" 
    + " from ptr_vip_cancel" 
    + " where 1=1" 
    + " and apr_flag <>'Y'";
    sqlSelect(strSql);
    if (colNum("db_cnt") > 0)
      return 1;

    // -copy Master-
    strSql = "insert into ptr_vip_cancel" 
        + " select cancel_cond " + ", overdue_mm         "
        + ", overdue_times1     " + ", overdue_times2     " + ", rc_use_mm_flag     "
        + ", rc_use_mm          " + ", rc_use_mm_rate1    " + ", rc_use_mm_rate2    "
        + ", rc_use_m1_flag     " + ", rc_use_m1_rate1    " + ", rc_use_m1_rate2    "
        + ", pre_cash_mm        " + ", pre_cash_times1    " + ", pre_cash_times2    "
        + ", limit_use_mm_flag  " + ", limit_use_mm       " + ", limit_use_mm_rate1 "
        + ", limit_use_mm_rate2 " + ", limit_use_m1_flag  " + ", limit_use_m1_rate1 "
        + ", limit_use_m1_rate2 " + ", ?" // crt_user "
        + ", " + commSqlStr.sysYYmd // crt_date "
        + ", 'N'" // apr_flag "
        + ", ''" // apr_date "
        + ", ''" // apr_user "
        + ", ?" // mod_user "
        + ", " + commSqlStr.sysdate // mod_time "
        + ", ?" // mod_pgm "
        + ", 1" // mod_seqno "
        + " from ptr_vip_cancel" 
        + " where 1=1" 
        + " and apr_flag ='Y'";
    setString2(1, modUser);
    setString(modUser);
    setString(modPgm);
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("主檔未異動, 不可修改明細資料; ptr_vip_cancel");
      return rc;
    }

    // -copy-detail-
    ptrVipDataCopy();

    return rc;
  }

  void insertPtrVipCancel() {
    String lsCancelCond = wp.itemNvl("db_cond_vip", "N") + wp.itemNvl("db_cond_block", "N")
        + wp.itemNvl("db_cond_hirisk", "N") + wp.itemNvl("db_cond_overdue", "N") + "N"
        + wp.itemNvl("db_cond_precash", "N") + "N" + wp.itemNvl("db_cond_action_code", "N")
        + wp.itemNvl("db_cond_excllist", "N");
    if (wp.itemEq("apr_flag", "Y") == true) {
      ptrVipDataCopy();
    }

    strSql = "insert into ptr_vip_cancel (" + " cancel_cond, " // 1
        + " overdue_mm," + " overdue_times1, " + " overdue_times2, " + " rc_use_mm_flag, " // 5
        + " rc_use_mm, " + " rc_use_mm_rate1, " + " rc_use_mm_rate2, " + " rc_use_m1_flag, "
        + " rc_use_m1_rate1, " // 10
        + " rc_use_m1_rate2, " + " pre_cash_mm, " + " pre_cash_times1, " + " pre_cash_times2, "
        + " limit_use_mm_flag, " // 15
        + " limit_use_mm, " + " limit_use_mm_rate1, " + " limit_use_mm_rate2, "
        + " limit_use_m1_flag, " + " limit_use_m1_rate1, " // 20
        + " limit_use_m1_rate2, " + " crt_user, " + " crt_date, " + " apr_flag, " + " apr_date, "
        + " apr_user " // 26
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?" + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {lsCancelCond// 1
        , wp.itemNum("overdue_mm"), wp.itemNum("overdue_times1"), wp.itemNum("overdue_times2"),
        wp.itemNvl("rc_use_mm_flag", "N") // 5
        , wp.itemNum("rc_use_mm"), wp.itemNum("rc_use_mm_rate1"), wp.itemNum("rc_use_mm_rate2"),
        wp.itemNvl("rc_use_m1_flag", "N"), wp.itemNum("rc_use_m1_rate1") // 10
        , wp.itemNum("rc_use_m1_rate2"), wp.itemNum("pre_cash_mm"), wp.itemNum("pre_cash_times1"),
        wp.itemNum("pre_cash_times2"), wp.itemNvl("limit_use_mm_flag", "N") // 15
        , wp.itemNum("limit_use_mm"), wp.itemNum("limit_use_mm_rate1"),
        wp.itemNum("limit_use_mm_rate2"), wp.itemNvl("limit_use_m1_flag", "N"),
        wp.itemNum("limit_use_m1_rate1") // 20
        , wp.itemNum("limit_use_m1_rate2"), wp.loginUser, ""
        // , wp.item_ss("crt_date")
        , "N" // 25
        , "", wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert PTR_VIP_CANCEL error; " + sqlErrtext);
    }
    return;
  }

  void deletePtrVipCancel() {
    strSql = "delete ptr_vip_cancel " + " where apr_flag <>'Y'";
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete PTR_vip_cancel error; " + this.sqlErrtext);
    }
  }

  public int ptrVipDataCopy() {
    msgOK();
    strSql = "insert into ptr_vip_data " 
        + " select table_name, "
        + " seq_no, "
        + " data_type" 
        + ", data_code"
        + ",'N'" 
        + " from ptr_vip_data" 
        + " where table_name='PTR_VIP_CANCEL'" 
        + " and seq_no='0'"
        + " and apr_flag='Y'";

    this.sqlExec(strSql);
    if (this.sqlRowNum < 0) {
      errmsg("Insert ptr_vip_data.COPY error; " + getMsg());
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    msgOK();

    if (wp.itemEq("apr_flag", "Y")) {
      errmsg("主管己覆核, 不可刪除");
    }

    deletePtrVipCancel();
    if (rc != 1)
      return rc;

    dbDeleteDetl("%");

    return rc;
  }

  // --vip_data--
  // --A01
  public int dbInsertDetl(String aType) {
    msgOK();

    strSql = "insert into ptr_vip_data (" + " table_name, " // 1
        + " seq_no, " + " data_type, " + " data_code," + " apr_flag " + " ) values (" + " ?,?,?,?,?"
        + " )";
    Object[] param = new Object[] {"PTR_VIP_CANCEL", // 1
        "0", aType, varsStr("data_code"), "N"};

    this.sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_vip_data.%s error; %s", aType, getMsg());
    }
    return rc;
  }
  
	public int dbInsertA05Detl(ArrayList<String> idArr) {
		msgOK();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("insert into ptr_vip_data ( table_name, seq_no, data_type, data_code, apr_flag ) ")
				.append(" values ");
		String valueTemplate = " ( 'PTR_VIP_CANCEL' , '0' , 'A05' , ? , 'N' ),";
		int i = 0;

		for (String id : idArr) {
			sqlBuilder.append(valueTemplate);
			setString(i, id);
			i++;
		}
		// remove the last character(,)
		strSql = sqlBuilder.substring(0, sqlBuilder.length() - 1);

		this.sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("Insert ptr_vip_data.%s error; %s", "A05", getMsg());
		}
		return rc;

	}

  public int dbDeleteDetl(String aType) {
    msgOK();

    strSql = "Delete ptr_vip_data" 
        + " where table_name ='PTR_VIP_CANCEL'" 
    	+ " and data_type like ?"
        + " and seq_no ='0'" 
    	+ " and apr_flag<>'Y'";

    setString2(1, aType);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ptr_vip_data.%s err; %s", aType, getMsg());
    }

    return rc;
  }
}
