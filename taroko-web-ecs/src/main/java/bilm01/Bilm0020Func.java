/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-27  V1.00.00  ryan       program initial                            *
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     *                                                                            *
******************************************************************************/
package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0020Func extends FuncEdit {
  String exCardNo = "";
  String exPurchaseDate = "";
  String exFilmNo = "";
  String exDestAmt = "";
  String kkReferenceNo = "";

  public Bilm0020Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

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
  public void dataCheck() {}

  @Override
  public int dbInsert() {
    actionInit("A");
    return rc;

  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("bil_curpost");
    if (!empty(varsStr("ls_major_card_no"))) {
      sp.ppstr("major_card_no", varsStr("ls_major_card_no"));
    }
    if (!empty(varsStr("ls_issue_date"))) {
      sp.ppstr("issue_date", varsStr("ls_issue_date"));
    }
    if (!empty(varsStr("ls_promote_dept"))) {
      sp.ppstr("promote_dept", varsStr("ls_promote_dept"));
    }
    if (!empty(varsStr("ls_prod_no"))) {
      sp.ppstr("prod_no", varsStr("ls_prod_no"));
    }
    if (!empty(varsStr("ls_group_code"))) {
      sp.ppstr("group_code", varsStr("ls_group_code"));
    }
    if (!empty(varsStr("ls_p_seqno"))) {
      sp.ppstr("p_seqno", varsStr("ls_p_seqno"));
    }
    if (!empty(varsStr("ls_acno_p_seqno"))) {
      sp.ppstr("acno_p_seqno", varsStr("ls_acno_p_seqno"));
    }
    if (!empty(varsStr("ls_id"))) {
      sp.ppstr("id_p_seqno", varsStr("ls_id"));
    }
    if (!empty(varsStr("ls_acct_type"))) {
      sp.ppstr("acct_type", varsStr("ls_acct_type"));
    }
    if (!empty(varsStr("ls_stmt_cycle"))) {
      sp.ppstr("stmt_cycle", varsStr("ls_stmt_cycle"));
    }
    
    //分期I4的處理
    if (!empty(varsStr("aa_rsk_rsn")) && "I4".equals(varsStr("aa_rsk_rsn")) ) {
    	long tempDestAmt = (long) commString.strToNum(varsStr("aa_dest_amt"));
    	int tempTotTerm = (int) commString.strToNum(varsStr("aa_install_tot_term"));
    	int tempInstallPerAmt = (int) (tempDestAmt / tempTotTerm);
    	int tempInstallFirstAmt = (int) (tempInstallPerAmt + tempDestAmt - (tempTotTerm * tempInstallPerAmt));
    	sp.ppstr("rsk_rsn", "");
    	sp.ppint("install_tot_term",tempTotTerm);
    	sp.ppint("install_first_amt",tempInstallFirstAmt);
    	sp.ppint("install_per_amt",tempInstallPerAmt);
    } 
    sp.ppstr("rsk_type", "");
    sp.ppstr("format_chk_ok_flag", "Y");
    sp.ppstr("doubt_type", "");
    sp.ppstr("manual_upd_flag", "Y");
    sp.ppstr("purchase_date", varsStr("aa_purchase_date"));
    sp.ppstr("film_no", varsStr("aa_film_no"));
    sp.ppstr("dest_curr", varsStr("aa_dest_curr"));
    sp.ppstr("mod_log", varsStr("aa_mod_log"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", "bilm0020");
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where reference_no=?", varsStr("aa_reference_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0)
      rc = 0;
    return rc;
  }


  @Override
  public int dbDelete() {

    return rc;
  }

}
