package cmsm01;
/** 19-0613:   JH    p_xxx >>acno_p_xxx
  * 109-04-19    shiyuqi       updated for project coding standard
  * 109-12-31  V1.00.03   shiyuqi       修改无意义命名   
  * 110-10-08  V1.00.04   Justin        add contact_zip2              * 
 * */
import busi.FuncAction;

public class Cmsm2020Contr extends FuncAction {
  String corppSeqno = "";

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub
    corppSeqno = wp.itemStr("corp_p_seqno");

    sqlWhere = " where corp_p_seqno =:kk1 ";
    setString("kk1", corppSeqno);
    if (isOtherModify("crd_corp", sqlWhere) == true) {
      errmsg("資料已被修改, 請重新讀取");
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {

    }
    strSql = "update crd_corp set " + " contact_name =:contact_name  , "
        + " contact_area_code =:contact_area_code  , " + " contact_tel_no =:contact_tel_no  , "
        + " contact_tel_ext =:contact_tel_ext  , " + " contact_zip =:contact_zip  , "
        + " contact_addr1 =:contact_addr1  , " + " contact_addr2 =:contact_addr2  , "
        + " contact_addr3 =:contact_addr3  , " + " contact_addr4 =:contact_addr4  , "
        + " contact_addr5 =:contact_addr5  , " + " mod_user =:mod_user  , "
        + " mod_time =sysdate  , " + " mod_pgm =:mod_pgm  , " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where corp_p_seqno =:kk1 " + " and nvl(mod_seqno,0)=:mod_seqno";
    setString("kk1", corppSeqno);
    item2ParmStr("contact_name");
    item2ParmStr("contact_area_code");
    item2ParmStr("contact_tel_no");
    item2ParmStr("contact_tel_ext");
//    item2ParmStr("contact_zip");
    setString("contact_zip", wp.itemStr("contact_zip") + wp.itemStr("contact_zip2"));
    item2ParmStr("contact_addr1");
    item2ParmStr("contact_addr2");
    item2ParmStr("contact_addr3");
    item2ParmStr("contact_addr4");
    item2ParmStr("contact_addr5");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm2020");
    item2ParmNum("mod_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
    }
    return rc;
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
