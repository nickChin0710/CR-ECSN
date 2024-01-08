/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
* 109-12-24   V1.00.02  Justin       parameterize sql
* 110-01-08  V1.00.03  tanwei        修改意義不明確變量                                                                          * 
* 110-01-19  V1.00.04  JustinWu    change a variable name
******************************************************************************/
package ecsfunc;
/** 個資查詢記錄
 * 2019-0807   JH    modify
 *  V.2019-0410.jh
 *
 * */

public class QueryLog extends taroko.base.BaseSQL {

  taroko.com.TarokoCommon wp = null;
  private String kkIdno = "";

  public QueryLog(taroko.com.TarokoCommon wr) {
    wp = wr;
  }

  public boolean authQuery(String asKey) {
    String sql1 = "";
    String lsWinid = wp.modPgm();
    String lsKey = commString.nvl(asKey);

    if (empty(lsWinid)) {
      errmsg("資料查詢權限: 程式代碼 不可空白");
      return false;
    }
    if (empty(wp.loginUser)) {
      errmsg("未指定資料查詢經辦 [loginUser]");
      return false;
    }

    try {
      // pgm-id:不受管制
      sql1 =
          "select count(*) as db_cnt from ptr_sys_idtab" + " where wf_type='COLM0920'"
              + " and wf_id =?";
      this.sqlSelect(wp.getConn(), sql1, new Object[] {lsWinid});
      if (sqlRowNum <= 0 || sqlNum("db_cnt") <= 0)
        return true;
      // --
      logIdno("N", lsKey);
      // -user:不受管制
      sql1 =
          "select count(*) from col_qry_data_auth where 1=1 and user_id = ? " ;
      taroko.base.SqlAccess sqlAccess = new taroko.base.SqlAccess();
      if (sqlAccess.getNumber(wp.getConn(), sql1, new Object[] {wp.loginUser}) <= 0) {
        return true;
      }
    } catch (Exception ex) {
    }
    // ------------------------------------
    if (empty(asKey)) {
      errmsg("資料查詢權限: 卡號 or 身分證ID[統編] 不可空白");
      return false;
    }

    int len = lsKey.length();
    if (len != 8 && len != 10 && len != 11 && (len > 11 && len < 15)) {
      errmsg("卡號,身分證ID: 輸入錯誤");
      return false;
    }
    if (len == 8 && commString.isNumber(lsKey) == false) {
      errmsg("統編: 輸入錯誤");
      return false;
    }

    try {
      // --
      sql1 =
          "select " + " nvl(acct_status,'NNNNN') as pa_acct "
              + ", nvl(stop_status,'N') as pa_stop " + ", nvl(mcode_cond,'N') as pa_cond "
              + ", nvl(mcode,'00') as pa_mcode " + " from ptr_comm_data "
              + " where parm_code ='COLM0910' " + " and seq_no =1 " + commSqlStr.rownum(1);
      sqlSelect(wp.getConn(), sql1, null);
      if (sqlRowNum <= 0) {
        errmsg("資料查詢權限: 未指定查詢條件 [w_colm0920], 不允許查詢");
        return false;
      }
      String lsPaAcct = sqlStr("pa_acct");
      String lsPaStop = sqlStr("pa_stop");
      String lsPaCond = sqlStr("pa_cond");
      String lsPaMcode = sqlStr("pa_mcode");
      // --
      if (lsPaAcct.indexOf("Y") >= 0 || commString.eqIgno(lsPaStop, "Y")) {
        sql1 =
            "select " + "sum(decode(nvl(acct_status,'0'),'2',1,0)) as db_acct2 "
                + ", sum(decode(nvl(acct_status,'0'),'3',1,0)) as db_acct3 "
                + ", sum(decode(nvl(acct_status,'0'),'4',1,0)) as db_acct4 "
                + ", sum(decode(nvl(stop_status,'N'),'Y',1,0)) as db_stop " + " from act_acno ";
        if (len >= 15) {
          sql1 +=
              " where id_p_seqno in (select major_id_p_seqno"
                  + " from crd_card where card_no =:key)";
          this.setString("key", lsKey);
        } else {
          sql1 += " where acct_key like :key)";
          setString("key", lsKey + "%");
        }
        if (sqlRowNum <= 0) {
          errmsg("資料查詢權限: select ACT_ACNO.acct_status error; ID=" + lsKey);
          return false;
        }
        if (lsPaAcct.substring(1, 2).equals("Y") && sqlNum("db_acct2") > 0)
          return true;
        if (lsPaAcct.substring(2, 3).equals("Y") && sqlNum("db_acct3") > 0)
          return true;
        if (lsPaAcct.substring(3, 4).equals("Y") && sqlNum("db_acct4") > 0)
          return true;
        if (lsPaStop.equalsIgnoreCase("Y") && sqlNum("db_stop") > 0)
          return true;
      }

      if (commString.eqIgno(lsPaCond, "Y")) {
        sql1 = "select count(*) as db_cnt from act_acno";
        if (len >= 15) {
          sql1 +=
              " where id_p_seqno in (select major_id_p_seqno"
                  + " from crd_card where card_no =:key)"
                  + " and nvl(payment_rate1,' ') >=:pa_mcode "
                  + " and nvl(payment_rate1,'') not in ('','0A','0B','0C','0D','0E')";
          setString("key", lsKey);
          setString("pa_mcode", lsPaMcode);
        } else {
          sql1 +=
              " where acct_key like :key" + " and nvl(payment_rate1,' ') >=:pa_mcode "
                  + " and nvl(payment_rate1,'') not in ('','0A','0B','0C','0D','0E')";
          setString("key", lsKey + "%");
          setString("pa_mcode", lsPaMcode);
        }
        sqlSelect(wp.getConn(), sql1, null);
        if (sqlRowNum > 0) {
          if (sqlNum("db_cnt") > 0)
            return true;
        }
      }
    } catch (Exception ex) {
      wp.log("auth_Query", ex.getMessage());
    }

    errmsg("資料查詢權限: 卡友帳務狀況未達 [參數條件] 不可查詢; ID[CORP]=" + lsKey);
    return false;
  }


  public boolean authQueryVd(String asKey) {
    msgOK();
    String sql1 = "";
    String lsWinid = wp.modPgm();
    String lsKey = commString.nvl(asKey);

    if (empty(lsWinid)) {
      errmsg("資料查詢權限: 程式代碼 不可空白");
      return false;
    }
    if (empty(wp.loginUser)) {
      errmsg("未指定資料查詢經辦 [login-User]");
      return false;
    }

    logIdno("Y", lsKey);

    try {
      // pgm-id:不受管制
      sql1 =
          "select count(*) as db_cnt from ptr_sys_idtab" + " where wf_type='COLM0920'"
              + " and wf_id =?";
      this.sqlSelect(wp.getConn(), sql1, new Object[] {lsWinid});
      if (sqlRowNum <= 0)
        return true;
      if (sqlNum("db_cnt") <= 0)
        return true;

      // -user:不受管制
      sql1 = "select count(*) as db_cnt from col_qry_data_auth" + " where user_id =?";
      sqlSelect(wp.conn(), sql1, new Object[] {wp.loginUser});
      if (sqlNum("db_cnt") <= 0)
        return true;
    } catch (Exception ex) {
      wp.log("auth_Query-VD", ex);
    }

    // ----------------------------------------
    if (empty(asKey)) {
      errmsg("資料查詢權限: 卡號 or 身分證ID[統編] 不可空白");
      return false;
    }

    int len = lsKey.length();
    if (len != 8 && len != 10 && (len > 10 && len < 15)) {
      errmsg("卡號,身分證ID: 輸入錯誤");
      return false;
    }
    if (len == 8 && commString.isNumber(lsKey)) {
      errmsg("統編: 輸入錯誤");
      return false;
    }

    try {
      // -欠款條件-
      double lmAmtParm = 0;
      sql1 =
          "select nvl(vd_end_bal,0) " + " from ptr_comm_data " + " where parm_code='COLM0910'"
              + " and seq_no =1";
      sqlSelect(wp.conn(), sql1, null);
      if (sqlRowNum <= 0) {
        errmsg("資料查詢權限: select PTR_COMM_DATA error");
        return false;
      }
      if (lmAmtParm <= 0)
        return true;
      if (lsKey.length() > 14) {
        sql1 =
            "select sum(A.end_bal) as db_amt from dba_debt A, dbc_card B"
                + " where A.p_seqno =B.p_seqno" + " and B.card_no =:ls_key";
        setString("key", lsKey);
      } else {
        sql1 =
            "select sum(A.end_bal) as db_amt from dba_debt A, dbc_acno B"
                + " where A.p_seqno =B.p_seqno" + " and B.acck_key like :key";
        setString("key", lsKey + "%");
      }
      sqlSelect(wp.conn(), sql1, null);
      if (sqlRowNum <= 0) {
        errmsg("資料查詢權限: select DBC_DEBT error; KEY=" + asKey);
        return false;
      }
      double lmAmt = sqlNum("db_amt");
      if (lmAmt >= lmAmtParm)
        return true;
    } catch (Exception ex) {
      wp.log("auth_Query-VD", ex);
    }
    errmsg("資料查詢權限: 卡友欠款未達 [參數金額] 不可查詢; KEY=" + asKey);
    return false;
  }

  public int logIdno(String debitFlag, String idNo) {
    String lsVip = "";
    String sql1 = "";

    if (empty(idNo))
      return 1;

    if (commString.eqIgno(debitFlag, "Y")) {
      sql1 =
          "select max(vip_code) from dba_acno"
              + " where id_p_seqno in (select id_p_seqno from dbc_idno where id_no=?)";
    } else {
      debitFlag = "N";
      sql1 =
          "select max(vip_code) as vip_code from act_acno"
              + " where id_p_seqno in (select id_p_seqno from crd_idno where id_no=?)";
    }
    try {
      sqlSelect(wp.getConn(), sql1, new Object[] {idNo});
      if (sqlRowNum > 0) {
        lsVip = sqlStr("vip_code");
      }
    } catch (Exception ex) {
    }

    sql1 =
        "INSERT INTO LOG_ONLINE_QUERY (" + " program_id " + ", proc_type " + ", file_name "
            + ", id_no " + ", vip_code " + ", query_reason" + ", crt_date, crt_user, crt_dept"
            + ", apr_flag, apr_date, apr_user" + ", mod_time" + " ) VALUES ( " + " :pgm_id" // :as_pgm,
            + ", :proc_type" // :as_type,
            + ", ''" // file_name
            + ", :kk" // :as_key,
            + ", :vip" // :ls_vip,
            + ", :query_reason" + ", to_char(sysdate,'yyyymmdd')" + ", :mod_user" // ls_moduser,
            + ", :dept_no" // :ls_dept_no,
            + ", '', '', ''" + ", sysdate " + " ) ";
    setString2("pgm_id", wp.modPgm());
    setString2("proc_type", debitFlag);
    setString2("kk", idNo);
    setString2("vip", lsVip);
    setString2("query_reason", wp.queryReason);
    setString2("mod_user", wp.loginUser);
    setString2("dept_no", wp.loginDeptNo);

    this.sqlExec(wp.getConn(), sql1, null);
    if (sqlRowNum > 0) {
      wp.commitOnly();
    }

    return 1;
  }

}
