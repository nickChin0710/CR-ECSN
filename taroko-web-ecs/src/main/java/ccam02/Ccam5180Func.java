package ccam02;

/* ccam5180	特殊指示原因碼維護　spec_code
 * V01.0		Alex	2018-0625
 * V00.0		Alex	2017-0823
 *  V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5180Func extends FuncEdit {
  String specCode = "";

  public Ccam5180Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      specCode = wp.itemStr("kk_spec_code");
    } else {
      specCode = wp.itemStr("spec_code");
    }

    if (empty(specCode)) {
      errmsg("原因代碼不可空白 !");
      return;
    }

    if (this.ibAdd || this.ibUpdate) {
      if (wp.itemEmpty("spec_desc")) {
        errmsg("說明不可空白 !");
        return;
      }

      if (wp.itemEmpty("resp_code")) {
        errmsg("回覆碼:不可空白");
        return;
      }

      if (wp.itemEmpty("check_level") && wp.itemEmpty("check_flag01")
          && wp.itemEmpty("check_flag02") && wp.itemEmpty("check_flag03")
          && wp.itemEmpty("check_flag04") && wp.itemEmpty("check_flag06")) {
        errmsg("作業指示碼: 不可全部空白");
        return;
      }

      if (wp.itemEmpty("spec_type")) {
        errmsg("代碼類別不可空白 !");
        return;
      }

//      if (!wp.itemEmpty("check_level") && !wp.itemEmpty("check_flag05")) {
//        errmsg("直接回覆 , 拒絕條件 , 額度100%內可用 只可選擇一個");
//        return;
//      }
      
      if(wp.itemEq("spec_type","1") && wp.itemEq("send_ibm", "Y")) {
    	  errmsg("代碼類別為凍結時，不可勾選 VD送主機 !");
    	  return ;
      }
      
//      if (wp.itemEq("check_level", "2")) {
//        isCheckFlag05 = "05";
//        isCheckLevel = "";
//      } else {
//        isCheckFlag05 = "00";
//        isCheckLevel = wp.itemStr("check_level");
//      }
            
    }

    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where spec_code=? and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {specCode, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_spec_code", sqlWhere,parms)) {
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
    strSql = "insert into CCA_spec_code (" + " spec_code, " // 1
        + " spec_desc, " + " resp_code, " + " check_level, " + " check_flag01 , " // 5
        + " check_flag02 , " + " check_flag03 , " + " check_flag04 , " + " check_flag05 , "
        + " check_flag06 , " // 10
        + " send_ibm, " + " neg_reason, " + " visa_reason, " + " mast_reason, " + " jcb_reason, " // 15
        + " spec_type, " // 16
        + " crt_date, crt_user, " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?, " // 5
        + " ?,?,?,?,?, " // 10
        + " ?,?,?,?,?, " // 15
        + " ? " // 16
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {specCode // 1
        , wp.itemStr("spec_desc"), wp.itemStr("resp_code"), wp.itemStr("check_level"),
        wp.itemNvl("check_flag01", "00") // 5
        , wp.itemNvl("check_flag02", "00"), wp.itemNvl("check_flag03", "00"),
        wp.itemNvl("check_flag04", "00"), wp.itemNvl("check_flag05", "N"), wp.itemNvl("check_flag06", "00") // 10
        , wp.itemNvl("send_ibm", "N"), wp.itemStr("neg_reason"), wp.itemStr("visa_reason"),
        wp.itemStr("mast_reason"), wp.itemStr("jcb_reason") // 15
        , wp.itemNvl("spec_type", "1") // 16
        , wp.loginUser, wp.loginUser, wp.loginUser, wp.modPgm() // 12
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    strSql = "update CCA_spec_code set " + " spec_desc =?, " + " resp_code =?, "
        + " check_level =?, " + " check_flag05 =?, " + " neg_reason =?, " + " visa_reason =?, "
        + " mast_reason =?, " + " jcb_reason =?, " + " send_ibm =?, " + " spec_type =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where spec_code=? and nvl(mod_seqno,0) =? ";
    Object[] param = new Object[] {wp.itemStr("spec_desc"), wp.itemStr("resp_code"), wp.itemStr("check_level"),
        wp.itemNvl("check_flag05","N"), wp.itemStr("neg_reason"), wp.itemStr("visa_reason"),
        wp.itemStr("mast_reason"), wp.itemStr("jcb_reason"), wp.itemNvl("send_ibm", "N"),
        wp.itemNvl("spec_type", "1"), wp.loginUser, wp.modPgm(),specCode, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete cca_spec_code where spec_code=? and nvl(mod_seqno,0) =? " ;
    Object[] parms = new Object[] {specCode, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,parms);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    if (rc == 1) {
      this.detlDeleteAll();
    }

    return rc;
  }

  public int detlDeleteAll() {
    msgOK();
    strSql = " delete cca_spec_detl " + " where spec_code =:spec_code ";
    item2ParmStr("spec_code");
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete cca_spec_detl error !");
    } else {
      rc = 1;
    }

    return rc;
  }

  public int detlDelete() {
    msgOK();

    strSql =
        " delete cca_spec_detl " + " where spec_code =:spec_code " + " and data_type =:data_type ";

    item2ParmStr("spec_code");
    item2ParmStr("data_type");

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete cca_spec_detl error !");
    } else {
      rc = 1;
    }

    return rc;
  }

  public int detlInsert() {
    msgOK();

    strSql = " insert into cca_spec_detl ( " + " spec_code , " + " data_type , " + " data_code , "
        + " data_code2 , " + " apr_flag , " + " mod_time , " + " mod_pgm " + " ) values ( "
        + " :spec_code , " + " :data_type , " + " :data_code , " + " :data_code2 , " + " 'Y' , "
        + " sysdate , " + " :mod_pgm " + " ) ";

    item2ParmStr("spec_code");
    item2ParmStr("data_type");
    var2ParmStr("data_code");
    var2ParmStr("data_code2");
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cca_spec_detl error !");
    }

    return rc;
  }

  public void updateCheckFlag() {
    msgOK();
    strSql = " update cca_spec_code set ";

    if (varEq("db_cnt", "0")) {
      if (wp.itemEq("data_type", "01")) {
        strSql += " check_flag01 ='00' ";
      } else if (wp.itemEq("data_type", "02")) {
        strSql += " check_flag02 ='00' ";
      } else if (wp.itemEq("data_type", "03")) {
        strSql += " check_flag03 ='00' ";
      } else if (wp.itemEq("data_type", "04")) {
        strSql += " check_flag04 ='00' ";
      } else if (wp.itemEq("data_type", "06")) {
        strSql += " check_flag06 ='00' ";
      }
    } else {
      if (wp.itemEq("data_type", "01")) {
        strSql += " check_flag01 ='01' ";
      } else if (wp.itemEq("data_type", "02")) {
        strSql += " check_flag02 ='02' ";
      } else if (wp.itemEq("data_type", "03")) {
        strSql += " check_flag03 ='03' ";
      } else if (wp.itemEq("data_type", "04")) {
        strSql += " check_flag04 ='04' ";
      } else if (wp.itemEq("data_type", "06")) {
        strSql += " check_flag06 ='06' ";
      }
    }

    strSql += " where spec_code =:spec_code ";

    item2ParmStr("spec_code");

    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("update cca_spec_code error !");
  }

}
