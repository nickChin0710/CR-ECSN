/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/06  V1.00.00    phopho     program initial                          *
*  109/01/16  V1.00.01    JustinWu   p_seqno -> acct_type and acct_key        *
*  109/05/06  V1.00.02  Aoyulan    updated for project coding standard        * 
*  112/01/13  V1.00.03  Sunny      統編修改時，項下所有個卡帳戶層都需要更新                          *
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0040Func extends FuncEdit {
  String kkAcctType, kkAcctKey,kkAcnoFlag;

  public Colm0040Func(TarokoCommon wr) {
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
  public void dataCheck() {
    // kk1 =wp.item_ss("p_seqno");
    kkAcctType = wp.itemStr("acct_type");
    kkAcctKey = wp.itemStr("acct_key");
    kkAcnoFlag = wp.itemStr("acno_flag");

    if (this.isAdd()) {
      return;
    }

    // -other modify-

    // //2020-01-16 JustinWu: p_seqno is not primary key of table ACT_ACNO
    // sql_where = " where p_seqno= ? "
    // + " and nvl(mod_seqno,0) = ? ";
    // Object[] param = new Object[] { kk1, wp.mod_seqno() };
    sqlWhere = " where acct_type = ? " + " and acct_key = ? " + " and nvl(mod_seqno,0) = ? ";

    Object[] param = new Object[] {kkAcctType, kkAcctKey, wp.modSeqno()};
    if (this.isOtherModify("act_acno", sqlWhere, param)) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    // No use..
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("act_acno");
    sp.ppstr("no_tel_coll_flag", varsStr("no_tel_coll_flag"));
    sp.ppstr("no_tel_coll_s_date", wp.itemStr("no_tel_coll_s_date"));
    sp.ppstr("no_tel_coll_e_date", wp.itemStr("no_tel_coll_e_date"));
    sp.ppstr("no_sms_flag", varsStr("no_sms_flag"));
    sp.ppstr("no_sms_s_date", wp.itemStr("no_sms_s_date"));
    sp.ppstr("no_sms_e_date", wp.itemStr("no_sms_e_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");

    // // 2020-01-16 JustinWu: p_seqno is not primary key
    // sp.sql2Where(" where p_seqno=?", kk1);
    sp.sql2Where(" where acct_type=?", kkAcctType);
    
    // 20220131 sunny 統編修改時，項下所有個卡帳戶層都需要更新。
    
    //if(kkAcctKey.length()==10)
    if(kkAcctType.equalsIgnoreCase("01"))
    {
      sp.sql2Where(" and acct_key=?", kkAcctKey);
      sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
    }
    else
    {
      sp.sql2Where(" and CORP_P_SEQNO IN (SELECT CORP_P_SEQNO FROM crd_corp WHERE corp_no = ?)", kkAcctKey);	
    }
    	
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    // No use..
    return rc;
  }

}
