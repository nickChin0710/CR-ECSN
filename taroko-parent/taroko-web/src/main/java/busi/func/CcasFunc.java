/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;
/** CCAS公用程式 V.2018-1026-JH
 * 2019-1220   JH    取消 cca_user_base
 * 2018-1026:	JH		spec_Approve()
 * 2018-1008:	JH		cca_user_base.file_date
 * 18-0926:		JH		modify
 *
 * */

import busi.FuncBase;

public class CcasFunc extends FuncBase {
  double limitAmt = 0;
  String aprUser = "", aprPawd = "";

  public int limitUserAuth(String aUser, String idPSeqno, String pSeqno, double aLimitAmt) {

    return 1;
  }

  void selectUserBase() {
    // is_sql = "select auth_flag, mon_auth_amt, tot_chker_amt, file_date"
    // + " from cca_user_base"
    // + " where user_id =?";
    // ppp(1, _apr_user);
    // sqlSelect(is_sql);
    // if (sql_nrow <= 0) {
    // errmsg("不是授權人員");
    // return;
    // }
    // if (!col_eq("auth_flag", "Y")) {
    // errmsg("授權人員: 權限已停用");
    // return;
    // }
    //
    // this.dateTime();
    // double lm_chker_amt =_limit_amt;
    // if (eq_igno(this.sys_Date,col_ss("file_date")) ) {
    // lm_chker_amt =col_num("tot_chker_amt") + _limit_amt;
    // }
    // if (lm_chker_amt > col_num("mon_auth_amt")) {
    // errmsg("臨調超過放行人員累積已放行金額");
    // return;
    // }
    //
    // is_sql="update cca_user_base set"
    // +" tot_chker_amt =?,"
    // +" file_date ="+commSqlStr .sys_YYmd+","
    // +commSqlStr .setMod_xxx(mod_user,mod_pgm)
    // +" where user_id =?";
    // ppp(1,lm_chker_amt);
    // ppp(_apr_user);
    // sqlExec(is_sql);
    // if (sql_nrow !=1) {
    // errmsg("update cca_user_base error, kk="+_apr_user);
    // }
    return;
  }

  public int specApprove(String aprUser, String specStatus) {
    if (eqIgno(specStatus, "91") == false) {
      return 1;
    }
    if (empty(aprUser)) {
      errmsg("覆核主管: 不可空白");
      return -1;
    }

    strSql =
        "select count(*) as xx_cnt" + " from sec_user" + " where usr_id =?"
            + " and usr_level in ('1','A','B')";
    setString2(1, aprUser);
    sqlSelect(strSql);
    if (sqlRowNum > 0 && colNum("xx_cnt") > 0) {
      return 1;
    }

    errmsg("戶特指(91) 須甲級主管以上才可放行");
    return rc;
  }

  public int limitApprove(String aprUser, String aprPasswd, double amAmt) {
    if (wp == null) {
      errmsg("程式設計錯誤; wp is NULL");
      return -1;
    }
    if (empty(aprUser) || empty(aprPasswd)) {
      errmsg("覆核主管及密碼: 不可空白");
      return -1;
    }
    if (eqIgno(aprUser, modUser)) {
      errmsg("自己不可是放行人員..無法作業");
      return -1;
    }
    if (commString.strIn2(aprUser, ",DXC,EDS")) {
      wp.log("EDS/DXC approve no-check");
      return 1;
    }

    this.aprUser = aprUser;
    aprPawd = aprPasswd;

    limitAmt = amAmt;
    selectUserBase();
    if (rc != 1) {
      sqlCommit(-1);
      return rc;
    }

    selectPasswdList();
    if (rc != 1)
      return rc;

    // sql_commit(1);
    return 1;
  }

  public int ccasApprove(String aprUser, String aprPasswd) {
    this.aprUser = aprUser;
    aprPawd = aprPasswd;

    if (wp == null) {
      errmsg("程式設計錯誤; wp is NULL");
      return -1;
    }
    if (empty(aprUser) || empty(aprPasswd)) {
      errmsg("覆核主管及密碼: 不可空白");
      return -1;
    }
    if (eqIgno(aprUser, modUser)) {
      errmsg("自己不可是放行人員..無法作業");
      return -1;
    }

    selectPasswdList();
    if (rc != 1)
      return rc;

    return 1;

    /*
     * SELECT eff_end_date,chker_flag,user_passwd,auth_amt_pct,usr_amt_pct,usr_end_date INTO
     * :ls_eff_end_date,:ls_flag,:ls_pass,:ld_auth_amt_pct,:ld_usr_amt_pct,: ls_usr_end_date FROM
     * USER_BASE WHERE USER_ID=:ls_userid ; IF sqlca.sqlcode <> 0 then
     * f_show_msg("Stop...非放行人員..無法作業~") return false END IF IF isnull(ls_eff_end_date) then
     * ls_eff_end_date='00000000' IF isnull(ls_flag) then ls_flag='N' IF isnull(ls_pass) then
     * ls_pass='' IF ls_flag <> '1' then f_show_msg("Stop...非放行主管..無法作業~") return false END IF
     * //todatex = string(today(), "yyyymmdd") f_get_sysdatetime(ls_date, ls_time) IF
     * ls_eff_end_date < ls_date then f_show_msg("Stop...此放行人員密碼效期已過..無法作業~") return false END IF IF
     * ls_usr_end_date < ls_date then f_show_msg("Stop...此放行人員效期已過..無法作業~") return false END IF
     * choose case ls_flag case 'N' f_show_msg("Stop...非放行主管..無法作業~") return false case '2' // 職員
     * f_show_msg("Stop...非放行主管..無法作業~") return false case '1' // 主管 long ll_seq_no
     * dw_3.AcceptTExt() lm_tot_amt_month = dw_3.Object.tot_amt_month[1] IF lm_tot_amt_month >
     * ld_auth_amt_pct THEN f_show_msg("放大倍數超過主管人員權限("+String(ld_auth_amt_pct)+")倍") RETURN FALSE
     * END IF select chker_passwd, seq_no into :ls_pass, :ll_seq_no from passwd_list where user_id =
     * :ls_userid ; if sqlca.sqlcode = 100 then f_show_msgbox("警告訊息","主管未產生放行密碼..無法作業~") RETURN
     * FALSE end if ls_after_pass = Space(Len(ls_befor_pass)) eds_encodepsw(ls_befor_pass,
     * ls_after_pass) IF ls_after_pass <> ls_pass then f_show_msgbox("警告訊息","主管放行密碼錯誤~..無法作業~")
     * //============98/08/06 密碼錯誤寫檔============BEG ii_err_cnt = ii_err_cnt + 1 ls_err_msg =
     * is_prd_type+& ' 產品~類別臨調:放行人員='+ls_userid+'作業~人員='+gs_userid+'密碼第'+String(ii_err_cnt)+'次錯誤~ '
     * f_err_log(ii_err_cnt, ls_err_msg, this.classname()) //============98/08/06
     * 密碼錯誤寫檔============END return false END IF delete passwd_list where user_id = :ls_userid and
     * seq_no = :ll_seq_no;
     */

  }

  void selectPasswdList() {
    strSql =
        "select seq_no, chker_passwd, hex(rowid) as rowid from cca_passwd_list"
            + " where user_id =?" + " order by seq_no" + commSqlStr.rownum(1);
    setString2(1, aprUser);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("主管未產生放行密碼..無法作業");
      return;
    }
    if (!eqIgno(aprPawd, colStr("chker_passwd"))) {
      errmsg("主管放行密碼錯誤..無法作業");
      return;
      // errmsg("放行人員[%s] 作業人員[%s] 密碼第[%s]次錯誤", apr_user,mod_user,col);
    }

    strSql = "delete cca_passwd_list where rowid =?";
    setRowId2(1, colStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum != 1) {
      errmsg("密碼使用刪除失敗");
      return;
    }
  }

}
