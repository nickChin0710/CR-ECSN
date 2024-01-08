/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-12-31  V1.00.00   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package cmsm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cmsm6010Func extends FuncEdit {
  String caseType = "", caseId = "";

  public Cmsm6010Func(TarokoCommon wr) {
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
      caseType = wp.itemStr("kk_case_type");
      caseId = wp.itemStr("kk_case_id");
    } else {
      caseType = wp.itemStr("case_type");
      caseId = wp.itemStr("case_id");
    }

    if (empty(caseType)) {
      errmsg("代碼類別:不可空白");
      return;
    }

    if (eqIgno(caseType, "1")) {
      if (wp.itemEq("send_code", "Y") == false) {
        if (wp.itemEmpty("dept_no") == false) {
          errmsg("不移送，部門代號應為空白！");
          return;
        }
      }
    }
    if (eqIgno(caseType, "2")) {
      if (wp.itemEmpty("dept_no")) {
        errmsg("部門代號不可空白 !");
        return;
      }
    }
    if (empty(caseId)) {
      errmsg("分類代碼: 不可空白!");
      return;
    }
    if (wp.itemEmpty("case_desc")) {
      errmsg("分類說明: 不可空白!");
      return;
    }
    if (this.isAdd()) {
    	if (doesPKExist()) {
    		errmsg("資料已存在不可新增,請以修改作業來維護資料");
			return;
		}
    	
    	return;
    }
    sqlWhere = " where case_type =:kk1 " + " and case_id =:kk2 ";
    setString("kk1", caseType);
    setString("kk2", caseId);
    if (isOtherModify("CMS_CASETYPE", sqlWhere) == true) {
      errmsg("資料已被修改, 請重新讀取");
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
    insertData();

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    if (wp.itemEq("apr_flag", "Y")) {
      insertData();
    } else if (wp.itemEq("apr_flag", "N")) {
      deleteData();
      insertData();
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
    deleteData();

    return rc;
  }

  void insertData() {
    msgOK();
    strSql = "insert into CMS_CASETYPE (" + " case_type, " // 1
        + " case_id, " + " case_desc, " + " send_code," + " dept_no," + " conf_mark," + " dept_no2,"
        + " dept_no3," + " apr_flag," + " crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " :case_type, :case_id,:case_desc, :send_code, :dept_no, :conf_mark, "
        + " :dept_no2, :dept_no3, 'N'" + ",to_char(sysdate,'yyyymmdd'),:crt_user "
        + ",sysdate, :mod_user, :mod_pgm, 1" + " )";
    // -set ?value-
    try {
      setString("case_type", caseType);
      setString("case_id", caseId);
      item2ParmStr("case_desc");
      item2ParmNvl("send_code", "N");
      item2ParmStr("dept_no");
      item2ParmNvl("conf_mark", "N");
      item2ParmStr("dept_no2");
      item2ParmStr("dept_no3");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      ((TarokoCommon) wp).expHandle("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
  }

  void deleteData() {
    msgOK();
    strSql = "delete CMS_CASETYPE " 
                + " where case_type =:kk1 " 
    		    + " and case_id =:kk2"
                + " and apr_flag =:apr_flag";

    setString("kk1", caseType);
    setString("kk2", caseId);
    setString("apr_flag", wp.itemStr("apr_flag"));
    // item2Parm_num("mod_seqno");
    sqlExec(strSql);
    if (rc < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
  }

private boolean doesPKExist() {
	strSql = " select 1"
			    + " from CMS_CASETYPE "
			    + " where case_type =:kk1 "
			    + " and case_id =:kk2 ";
	setString("kk1", caseType);
    setString("kk2", caseId);
    
    sqlSelect(strSql);
    
    if (sqlRowNum <= 0) {
    	return false;
    }
    
	return true;
}


}
