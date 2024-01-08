/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-08-11  V1.00.02  JustinWu     add dbInsertDetlByDataType
* 109-08-18  V1.00.03 JustinWu      remove useless code
* 109-12-24  V1.00.04 JustinWu      parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;

import java.util.ArrayList;

import busi.FuncEdit;
import taroko.base.CommSqlStr;
import taroko.com.TarokoCommon;

public class Ptrm0470Func extends FuncEdit {
  String seqNo = "", aprFlag = "";

  // public Ptrm0470Func(TarokoCommon wr) {
  // wp = wr;
  // this.conn = wp.getConn();
  // }
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
    seqNo = wp.itemStr("seq_no");
    aprFlag = wp.itemNvl("apr_flag", "Y");
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where seq_no = ?  and nvl(mod_seqno,0) =nvl(?,0) " ;
    
    if (eqIgno(aprFlag, "Y")) {
      sqlWhere += " and apr_flag='Y'";
    } else {
      sqlWhere += " and nvl(apr_flag,'N')<>'Y'";
    }
    if (this.isOtherModify("ptr_vip_code", sqlWhere, new Object[] {seqNo, wp.modSeqno()})) {
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

    insertPtrVipCode();

    return rc;
  }

  void insertPtrVipCode() {
    String lsInclCond = wp.itemNvl("db_incl_class_code", "N") + wp.itemNvl("db_incl_pd_rate", "N")
        + wp.itemNvl("db_incl_pay_rate", "N") + wp.itemNvl("db_incl_card_since", "N")
        + wp.itemNvl("db_incl_limit_amt", "N") + wp.itemNvl("db_incl_purch_amt", "N")
        + wp.itemNvl("db_incl_bank_rela", "N") + wp.itemNvl("db_incl_list", "N")
        + wp.itemNvl("db_incl_group_code", "N") + wp.itemNvl("db_incl_reason_down", "N") // 例外調降理由碼
    ;

    String lsExclCond = wp.itemNvl("db_excl_block", "N") + wp.itemNvl("db_excl_hi_risk", "N")
        + wp.itemNvl("db_excl_overdue", "N") + wp.itemNvl("db_excl_rc_use", "N")
        + wp.itemNvl("db_excl_pre_cash", "N") + wp.itemNvl("db_excl_limit_use", "N")
        + wp.itemNvl("db_excl_action_code", "N") + wp.itemNvl("db_excl_list", "N")
        + wp.itemNvl("db_excl_exblock", "N") // -no-use
    ;
    if (wp.itemEq("apr_flag", "Y") == true) {
      ptrVipDataCopy();
    }

    strSql = "insert into ptr_vip_code (" + " seq_no, " // 1
        + " incl_cond," + " vip_code, " + " vip_desc, " + " class_code_flag, " // 5
        + " pd_rate_mm_cond, " + " pd_rate_mm, " + " pd_rate_mm_flag, " + " pd_rate_m1_cond, "
        + " pd_rate_m1_flag, " // 10
        + " pay_rate_mm_cond, " + " pay_rate_mm, " + " pay_rate_mm_flag, " + " pay_rate_m1_cond, "
        + " pay_rate_m1_flag, " // 15
        + " card_since1, " + " card_since2, " + " limit_amt1, " + " limit_amt2, "
        + " purch_amt_mm, " // 20
        + " purch_amt1, " + " purch_amt2, " + " purch_amt_num, " + " bank_rela_flag, "
        + " excl_cond, " // 25
        + " overdue_mm, " + " overdue_times1, " + " overdue_times2, " + " rc_use_mm_flag, "
        + " rc_use_mm, " // 30
        + " rc_use_mm_rate1, " + " rc_use_mm_rate2, " + " rc_use_m1_flag, " + " rc_use_m1_rate1, "
        + " rc_use_m1_rate2, "// 35
        + " pre_cash_mm, " + " pre_cash_times1, " + " pre_cash_times2, " + " limit_use_mm_flag, "
        + " limit_use_mm, " // 40
        + " limit_use_mm_rate1, " + " limit_use_mm_rate2, " + " limit_use_m1_flag, "
        + " limit_use_m1_rate1, " + " limit_use_m1_rate2, "// 45
        + " group_code_cond, " + " reset_flag, " + " crt_user, " + " crt_date, " + " apr_flag, "
        + " apr_date, "// 50
        + " apr_user "// 51
        + ", mod_time, mod_user, mod_pgm, mod_seqno"// 53
        + " ) values (" + " ?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,to_char(sysdate,'yyyymmdd'),?" + ",?,?"
        + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {seqNo// 1
        , lsInclCond, wp.itemStr("vip_code"), wp.itemStr("vip_desc"), wp.itemStr("class_code_flag") // 5
        , wp.itemStr("pd_rate_mm_cond"), wp.itemNum("pd_rate_mm"), wp.itemStr("pd_rate_mm_flag"),
        wp.itemStr("pd_rate_m1_cond"), wp.itemStr("pd_rate_m1_flag") // 10
        , wp.itemStr("pay_rate_mm_cond"), wp.itemNum("pay_rate_mm"), wp.itemStr("pay_rate_mm_flag"),
        wp.itemStr("pay_rate_m1_cond"), wp.itemStr("pay_rate_m1_flag") // 15
        , wp.itemNum("card_since1"), wp.itemNum("card_since2"), wp.itemNum("limit_amt1"),
        wp.itemNum("limit_amt2"), wp.itemNum("purch_amt_mm") // 20
        , wp.itemNum("purch_amt1"), wp.itemNum("purch_amt2"), wp.itemNum("purch_amt_num"),
        wp.itemStr("bank_rela_flag"), lsExclCond // 25
        , wp.itemNum("overdue_mm"), wp.itemNum("overdue_times1"), wp.itemNum("overdue_times2"),
        wp.itemStr("rc_use_mm_flag"), wp.itemNum("rc_use_mm") // 30
        , wp.itemNum("rc_use_mm_rate1"), wp.itemNum("rc_use_mm_rate2"),
        wp.itemStr("rc_use_m1_flag"), wp.itemNum("rc_use_m1_rate1"), wp.itemNum("rc_use_m1_rate2") // 35
        , wp.itemNum("pre_cash_mm"), wp.itemNum("pre_cash_times1"), wp.itemNum("pre_cash_times2"),
        wp.itemStr("limit_use_mm_flag"), wp.itemNum("limit_use_mm") // 40
        , wp.itemNum("limit_use_mm_rate1"), wp.itemNum("limit_use_mm_rate2"),
        wp.itemStr("limit_use_m1_flag"), wp.itemNum("limit_use_m1_rate1"),
        wp.itemNum("limit_use_m1_rate2") // 45
        , wp.itemStr("group_code_cond"), wp.itemStr("reset_flag"), wp.loginUser
        // , wp.item_ss("crt_date")
        , "N" // 50
        , "", wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm") // 54
    };
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert PTR_vip_code error; " + sqlErrtext);
    }
    return;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    if (wp.itemEq("apr_flag", "Y") == true) {
      insertPtrVipCode();
    }
    if (wp.itemEq("apr_flag", "N") == true) {
      deletePtrVipCode();
      insertPtrVipCode();
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
    strSql = "delete ptr_vip_code " + sqlWhere;
    rc = sqlExec(strSql, new Object[] {seqNo, wp.modSeqno()});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    if (rc == 1) {
      this.dbDeleteDetl();
    }
    return rc;
  }

  public int dbDeleteDetl() {
    msgOK();
    strSql = "Delete ptr_vip_data" + " where seq_no = ? "
        + " and apr_flag = ? ";
    setString(wp.itemStr("seq_no"));
    setString(wp.itemStr("apr_flag"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ptr_vip_data err; " + getMsg());
      rc = -1;
    } else
      rc = 1;

    return rc;
  }

  void deletePtrVipCode() {

    strSql = "delete ptr_vip_code " + sqlWhere;
    rc = sqlExec(strSql, new Object[] {seqNo, wp.modSeqno()});
    if (sqlRowNum < 0) {
      errmsg("delete PTR_vip_code error; " + this.sqlErrtext);
    }
  }

  public int ptrVipDataCopy() {
    msgOK();
    strSql = "insert into ptr_vip_data " + " select table_name, seq_no, data_type" + ", data_code"
        + ",'N'" + " from ptr_vip_data" + " where table_name='PTR_VIP_CODE'" + " and seq_no=?"
        + " and apr_flag='Y'";
    Object[] param = new Object[] {wp.itemStr("seq_no"),};

    this.sqlExec(strSql, param);
    if (this.sqlRowNum < 0) {
      errmsg("Insert ptr_vip_code_dtl.COPY error; " + getMsg());
    }
    return rc;
  }

  public int copyMaster2unApr(String a_type) {
    strSql = "select count(*) as db_cnt" + " from ptr_vip_code" 
        + " where 1=1 and seq_no = ?  and apr_flag <>'Y'";
    setDouble(wp.itemNum("seq_no"));
    sqlSelect(strSql);
    if (colNum("db_cnt") > 0)
      return 1;

    // -copy Master-
    strSql = "insert into ptr_vip_code" + " select seq_no " + ", vip_code          "
        + ", vip_desc          " + ", incl_cond         " + ", class_code_flag   "
        + ", pd_rate_mm_cond   " + ", pd_rate_mm        " + ", pd_rate_mm_flag   "
        + ", pd_rate_m1_cond   " + ", pd_rate_m1_flag   " + ", pay_rate_mm_cond  "
        + ", pay_rate_mm       " + ", pay_rate_mm_flag  " + ", pay_rate_m1_cond  "
        + ", pay_rate_m1_flag  " + ", card_since1       " + ", card_since2       "
        + ", limit_amt1        " + ", limit_amt2        " + ", purch_amt_mm      "
        + ", purch_amt1        " + ", purch_amt2        " + ", purch_amt_num     "
        + ", bank_rela_flag    " + ", excl_cond         " + ", overdue_mm        "
        + ", overdue_times1    " + ", overdue_times2    " + ", rc_use_mm_flag    "
        + ", rc_use_mm         " + ", rc_use_mm_rate1   " + ", rc_use_mm_rate2   "
        + ", rc_use_m1_flag    " + ", rc_use_m1_rate1   " + ", rc_use_m1_rate2   "
        + ", pre_cash_mm       " + ", pre_cash_times1   " + ", pre_cash_times2   "
        + ", limit_use_mm_flag " + ", limit_use_mm      " + ", limit_use_mm_rate1 "
        + ", limit_use_mm_rate2 " + ", limit_use_m1_flag  " + ", limit_use_m1_rate1 "
        + ", limit_use_m1_rate2 " + ", group_code_cond    " + ", reset_flag " + ", ?" + ", "
        + commSqlStr.sysYYmd + ", 'N'" + ", ''" + ", ''" + ", ?" // mod_user "
        + ", sysdate" // mod_time "
        + ", ?" // mod_pgm "
        + ", 1  " + " from ptr_vip_code" + " where 1=1 and seq_no = ? " 
        + " and apr_flag ='Y'";
    setString2(1, modUser);
    setString(modUser);
    setString(modPgm);
    setDouble(wp.itemNum("seq_no"));
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("主檔未異動, 不可修改明細資料");
      return rc;
    }

    // -copy-detail-
    ptrVipDataCopy();

    return rc;
  }

  public int dbDeleteDetl(String aType) {
    msgOK();

    strSql = "Delete ptr_vip_data" + " where table_name ='PTR_VIP_CODE' and seq_no = ? "
        + " and data_type =?" + " and apr_flag<>'Y'";
    setDouble(wp.itemNum("seq_no"));
    setString(aType);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ptr_vip_data.%s err", aType);
    }

    return rc;
  }

  // --I01
  public int dbInsertDetl(String aType) {
    msgOK();

    strSql = "insert into ptr_vip_data (" + " table_name, " // 1
        + " seq_no, " + " data_type, " + " data_code," + " apr_flag " + " ) values (" + " ?,?,?,?,?"
        + " )";
    Object[] param = new Object[] {"PTR_VIP_CODE", // 1
        wp.itemStr("seq_no"), aType, varsStr("data_code"), "N"};

    this.sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_vip_data.%s error, seqno=%s", aType, wp.itemStr("seq_no"));
    }
    return rc;
  }
  
	public int dbInsertDetlByDataType(ArrayList<String> idArr, String dataType, String seqno) {
		msgOK();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("insert into ptr_vip_data ( table_name, seq_no, data_type, data_code, apr_flag ) ")
				           .append(" values ");
		String valueTemplate = " ( 'PTR_VIP_CODE' , ? , ? , ? , 'N' ),";
		int i = 0;

		for (String id : idArr) {
			sqlBuilder.append(valueTemplate);
			setString(i++, seqno);
			setString(i++,dataType);
			setString(i++, id);
		}
		// remove the last character(,)
		strSql = sqlBuilder.substring(0, sqlBuilder.length() - 1);

		this.sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("Insert ptr_vip_data.%s error; %s", "E04", getMsg());
		}
		return rc;

	}
}
