/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-05-28  V1.00.01  Wilson       檢核卡人等級重複不可新增                                                                    *
* 109-12-23   V1.00.02 Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import busi.FuncAction;

public class Ptrm0073Func extends FuncAction {
	String classCode = "";
	
  @Override
  public void dataCheck() {
	      classCode = wp.itemStr("ex_class_code");

	      strSql = "select count(*) as tot_cnt from ptr_class where class_code= ?";
	      Object[] param1 = new Object[] {wp.itemStr("ex_class_code")};
	      sqlSelect(strSql, param1);
	      if (colNum("tot_cnt") > 0) {
	        errmsg("此卡人等級已存在，不可新增!");
	        return;
	      } 	 
	  
    if (wp.itemNum("ex_beg_credit_lmt") > wp.itemNum("ex_end_credit_lmt")) {
      errmsg("起點額度不可大於迄點額度 ");
      return;
    }

    if (wp.itemEmpty("ex_class_code")) {
      errmsg("卡人等級:不可空白");
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

  public int deleteClass() {
    msgOK();
    strSql = "delete ptr_class where 1=1 and hex(rowid) = ? " ;
    setString(varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_class error !");
    }

    return rc;
  }

  public int insertClass() {
    msgOK();

    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ptr_class ( " + " beg_credit_lmt , " + " end_credit_lmt , "
        + " class_code , " + " crt_date , " + " crt_user , " + " mod_time , " + " mod_user , "
        + " mod_pgm , " + " mod_seqno " + " ) values ( " + " :beg_credit_lmt , "
        + " :end_credit_lmt , " + " :class_code , " + " to_char(sysdate,'yyyymmdd') , "
        + " :crt_user , " + " sysdate , " + " :mod_user , " + " :mod_pgm , " + " 1 " + " ) ";

    item2ParmNum("beg_credit_lmt", "ex_beg_credit_lmt");
    item2ParmNum("end_credit_lmt", "ex_end_credit_lmt");
    item2ParmStr("class_code", "ex_class_code");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0073");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ptr_class error !");
    }
    return rc;
  }

}
