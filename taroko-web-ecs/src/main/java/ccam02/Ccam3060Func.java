
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam3060Func extends FuncEdit {
  String bankId = "", voiceId = "";

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
    if (this.ibAdd) {
      bankId = wp.itemStr("kk_acq_bank_id");
      voiceId = wp.itemStr("kk_voice_id");
    } else {
      bankId = wp.itemStr("acq_bank_id");
      voiceId = wp.itemStr("voice_id");
    }
    if (isEmpty(voiceId)) {
      errmsg("特店代號 : 不可空白");
      return;
    }

    if (this.isAdd()) {
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

    strSql = "insert into CCA_VOICE (" + " voice_id, " // 1
        + " acq_bank_id, " + " crt_date, crt_user ," + " apr_date, apr_user ,"
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " :voice_id, :acq_bank_id "
        + ",to_char(sysdate,'yyyymmdd'),:crt_user ," + "to_char(sysdate,'yyyymmdd'),:apr_user ,"
        + "sysdate, :mod_user, :mod_pgm, 1" + " )";

    try {
      setString("acq_bank_id", bankId);
      setString("voice_id", voiceId);
      setString("crt_user", wp.loginUser);
      setString("apr_user", wp.itemStr2("approval_user"));
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.log("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete CCA_VOICE " + " where acq_bank_id =:kk1 " + " and voice_id =:kk2"
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    // ddd("del-sql="+is_sql);
    setString("kk1", bankId);
    setString("kk2", voiceId);
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
