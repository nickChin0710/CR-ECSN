package cmsm02;
/**
 *  19-1127:   Alex  insert errmsg
 *  19-0614:   JH    p_xxx >>acno_pxxx
 * */
import busi.FuncAction;


public class Cmsm3150Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("card_no")) {
      errmsg("卡號不可空白");
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "insert into mkt_vocdata (" + " docu_code ," + " crt_date ," + " card_no ,"
        + " debit_flag ," + " p_seqno ," + " id_no ," + " chi_name ," + " sex ,"
        + " major_card_no ," + " crt_time ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values (" + " :docu_code ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :card_no ," + " :debit_flag ," + " :p_seqno ," + " :id_no ," + " :chi_name ,"
        + " :sex ," + " :major_card_no ," + " to_char(sysdate,'hh24miss') ," + " :mod_user ,"
        + " sysdate ," + " :mod_pgm ," + " '1' " + " )";
    item2ParmStr("docu_code");
    item2ParmStr("card_no");
    item2ParmStr("debit_flag");
    item2ParmStr("p_seqno");
    item2ParmStr("id_no");
    item2ParmStr("chi_name");
    item2ParmStr("sex");
    item2ParmStr("major_card_no");
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料已存在不可新增,請以修改作業來維護資料");
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
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
