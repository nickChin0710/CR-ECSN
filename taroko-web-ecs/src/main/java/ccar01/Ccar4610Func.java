package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-06-30  V1.00.02  Zuwei      fix code scan issue Password Management: Empty Password                           *
*  110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
*/
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import busi.FuncAction;
import taroko.base.CommDate;
import taroko.base.CommString;

public class Ccar4610Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  String lsPawd = "", lsTime = "";
  int liCnt = 0, llSeed = 0, liPawd = 0;

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    deletePasswd();
    lsTime = commDate.sysTime();
    liCnt = (int) wp.itemNum("ex_passwd_cnt");
    llSeed = (int) (Double.parseDouble(lsTime.substring(2, 4)) * 60
        + Double.parseDouble(lsTime.substring(4, 6)));
    SecureRandom random = null;
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      // random = new Random(new Date().getTime());
      throw new RuntimeException("init SecureRandom failed.", e);
    }
    for (int ii = 0; ii < liCnt; ii++) {
      liPawd = (int) ((random.nextDouble() + 1) * 32767) + llSeed * ii;
      lsPawd = commString.intToStr(liPawd).substring(0, 5);
      insertPasswd(ii);
      if (rc != 1)
        break;
    }
    // if(rc!=1){
    // return rc;
    // }
    // updateBase();
    return rc;
  }

  public int deletePasswd() {
    msgOK();
    strSql = "delete cca_passwd_list " + " where user_id =:user_id ";
    item2ParmStr("user_id", "ex_user_id");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("密碼刪除錯誤");
      return rc;
    }

    return rc;
  }

  public int insertPasswd(int ll) {
    msgOK();
    ll = ll + 1;
    strSql = "insert into cca_passwd_list (" + " user_id, " + " seq_no, " + " chker_passwd,"
        + " crt_date," + " crt_time " + " ) values (" + " :user_id," + " :seq_no,"
        + " :chker_passwd," + " to_char(sysdate,'yyyymmdd') ,"// 5
        + " to_char(sysdate,'hh24miss') " + " )";
    // -set ?value-

    setString("user_id", wp.loginUser);
    setString("seq_no", "" + ll);
    setString("chker_passwd", lsPawd);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  public int updateBase() {
    strSql = "update cca_user_base set " + " tot_chker_amt = 0  " + " where user_id =:user_id ";
    setString("user_id", wp.loginUser);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
