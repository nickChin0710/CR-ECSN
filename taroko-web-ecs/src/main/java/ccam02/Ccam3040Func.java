
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

public class Ccam3040Func extends FuncEdit {
  String mchtNo = "", bankId = "";

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
      mchtNo = wp.itemStr("kk_mcht_no");
      bankId = wp.itemStr("kk_acq_bank_id");
    } else {
      mchtNo = wp.itemStr("mcht_no");
      bankId = wp.itemStr("acq_bank_id");
    }


    if (isEmpty(mchtNo)) {
      errmsg("特店代碼：不可空白");
      return;
    }

    if (empty(bankId)) {
      errmsg("收單行： 不可空白");
      return;
    }

    if (empty(wp.itemStr("mcht_name"))) {
      errmsg("特店名稱：不可空白");
      return;
    }

    if (this.ibAdd || this.ibUpdate) {
      if (wp.itemEmpty("mcc_code") == false) {
        if (checkMccTrue(wp.itemStr2("mcc_code")) == false) {
          errmsg("Mcc Code 輸入錯誤");
          return;
        }
      }

    }

    if (this.ibAdd) {
      if (checkMcc() == false) {
        errmsg("特店代號 , Mcc Code 已存在 不可新增");
        return;
      }
    }
    if (this.ibAdd)
      return;

    sqlWhere = " where mcht_no=?" + " and acq_bank_id=?" + " and nvl(mod_seqno,0) =?";

    Object[] parms = new Object[] {mchtNo, bankId, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_mcht_base", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }
  }

  boolean checkMcc() {
    if (wp.itemEmpty("mcc_code"))
      return true;
    String sql1 = " select " + " count(*) as db_cnt " + " from cca_mcht_base "
        + " where mcht_no = ? " + " and mcc_code = ? ";


    sqlSelect(sql1, new Object[] {mchtNo, wp.itemStr("mcc_code")});

    if (colNum("db_cnt") > 0)
      return false;

    return true;
  }

  boolean checkMccTrue(String lsMccCode) {

    String sql1 = " select count(*) as db_cnt from cca_mcc_risk where mcc_code = ? ";
    sqlSelect(sql1, new Object[] {lsMccCode});

    if (sqlRowNum < 0 || colNum("db_cnt") <= 0)
      return false;

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "insert into cca_mcht_base (" + " mcht_no ," + " acq_bank_id ," + " mcht_name ,"
        + " zip_code ," + " zip_city ," + " mcht_addr ," + " tel_no ," + " mcc_code ,"
        + " mcht_remark , " + " ncc_risk_level , " + " crt_date , " + " crt_user ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( " + " :mchtNo ," + " :bankId ,"
        + " :mcht_name ," + " :zip_code ," + " :zip_city ," + " :mcht_addr ," + " :tel_no ,"
        + " :mcc_code ," + " :mcht_remark ," + " :ncc_risk_level ,"
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " )";

    setString("mchtNo", mchtNo);
    setString("bankId", bankId);
    item2ParmStr("mcht_name");
    item2ParmStr("zip_code");
    item2ParmStr("zip_city");
    item2ParmStr("mcht_addr");
    item2ParmStr("tel_no");
    item2ParmStr("mcc_code");
    item2ParmStr("mcht_remark");
    item2ParmStr("ncc_risk_level");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam3040");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料已存在 不可新增");
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update cca_mcht_base set " + " mcht_name = :mcht_name, " + " zip_code = :zip_code, "
        + " zip_city = :zip_city, " + " mcht_addr = :mcht_addr, " + " tel_no = :tel_no, "
        + " mcc_code = :mcc_code, " + " mcht_remark =:mcht_remark, "
        + " ncc_risk_level =:ncc_risk_level , "
        + " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + " where mcht_no =:kk" + " and acq_bank_id =:bank_id "
        + " and nvl(mod_seqno,0) =:mod_seqno";

    item2ParmStr("mcht_name");
    item2ParmStr("zip_code");
    item2ParmStr("zip_city");
    item2ParmStr("mcht_addr");
    item2ParmStr("tel_no");
    item2ParmStr("mcc_code");
    item2ParmStr("mcht_remark");
    item2ParmStr("ncc_risk_level");
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);
    // -kk-
    item2ParmStr("kk", "mcht_no");
    item2ParmStr("bank_id", "acq_bank_id");
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
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
    strSql = " delete cca_mcht_base where mcht_no=:mchtNo and acq_bank_id=:bankId ";
    setString("mchtNo", mchtNo);
    setString("bankId", bankId);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cca_mcht_base error !");
    }
    return rc;
  }

}
