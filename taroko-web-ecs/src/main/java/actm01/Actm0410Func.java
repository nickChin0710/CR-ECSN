/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-27  V1.00.01  Simon      sync codes with mega                       *
******************************************************************************/

package actm01;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0410Func extends FuncEdit {

  public Actm0410Func(TarokoCommon wr) {
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
    // check PK

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("act_pay_sms");
    // sp.ppss("id_no",wp.item_ss("kk_id_no"));
    sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
    sp.ppstr("sms_s_acct_month", wp.itemStr("sms_s_acct_month"));
    sp.ppstr("sms_e_acct_month", wp.itemStr("sms_e_acct_month"));
    sp.ppstr("lastpay_date_m3", wp.itemStr("lastpay_date_m3"));
    sp.ppstr("lastpay_date_m2", wp.itemStr("lastpay_date_m2"));
    sp.ppstr("lastpay_date_m1", wp.itemStr("lastpay_date_m1"));
    sp.ppstr("lastpay_date_m0", wp.itemStr("lastpay_date_m0"));
    sp.ppstr("lastpay_date_p1", wp.itemStr("lastpay_date_p1"));
    sp.ppstr("lastpay_date_p2", wp.itemStr("lastpay_date_p2"));
    sp.ppstr("lastpay_date_p3", wp.itemStr("lastpay_date_p3"));
    sp.ppstr("stop_s_date", wp.itemStr("stop_s_date"));
    sp.ppstr("stop_e_date", wp.itemStr("stop_e_date"));
    sp.addsql(", crt_date ", ", to_char(sysdate,'YYYYMMDD') ");
		sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
    // sp.addsql(", apr_date ",", to_char(sysdate,'YYYYMMDD') ");
    // sp.ppss("apr_user", wp.item_ss("approval_user"));
    // sp.ppss("apr_user", wp.loginUser);
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "1");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("act_pay_sms");
    sp.ppstr("lastpay_date_m3", wp.itemStr("lastpay_date_m3").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_m2", wp.itemStr("lastpay_date_m2").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_m1", wp.itemStr("lastpay_date_m1").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_m0", wp.itemStr("lastpay_date_m0").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_p1", wp.itemStr("lastpay_date_p1").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_p2", wp.itemStr("lastpay_date_p2").equals("Y") ? "Y" : "N");
    sp.ppstr("lastpay_date_p3", wp.itemStr("lastpay_date_p3").equals("Y") ? "Y" : "N");
    sp.ppstr("sms_s_acct_month", wp.itemStr("sms_s_acct_month"));
    sp.ppstr("sms_e_acct_month", wp.itemStr("sms_e_acct_month"));
    sp.ppstr("stop_s_date", wp.itemStr("stop_s_date"));
    sp.ppstr("stop_e_date", wp.itemStr("stop_e_date"));
    // sp.addsql(", apr_date = to_char(sysdate,'YYYYMMDD') ");
    // sp.ppss("apr_user", wp.item_ss("approval_user"));
    // sp.ppss("apr_user", wp.loginUser);
		sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.addsql(", crt_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("apr_flag", "N");
		sp.ppstr("apr_date", "");
		sp.ppstr("apr_time", "");
		sp.ppstr("apr_user", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where id_p_seqno = ?", wp.itemStr("id_p_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete act_pay_sms where id_p_seqno = ?";
    Object[] param = new Object[] {wp.itemStr("id_p_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }


}
