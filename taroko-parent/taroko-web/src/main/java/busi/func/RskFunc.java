/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;
/* 風管公用程式 V.2018-0420
 * 2018-0420:	JH		++vouch_isClose()
 * */
import busi.FuncBase;


public class RskFunc extends FuncBase {

  public boolean vouchIsClose() {
    // -是否已啟動軋帳-
    strSql = "select count(*) as ll_cnt from ptr_businday" + "where 1=1 and vouch_chk_flag ='Y'";
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      errmsg("已啟動軋帳作業");
      return true;
    }

    return false;
  }

  public String arbitExpireDate(String aBinType, String aTxnCode, String aBaseDate) {
    // -arbit_expire_date-
    strSql =
        "select " + commSqlStr.sqlID + "uf_rsk_stage_days(?,?,'4',?) as expire_date from"
            + commSqlStr.sqlDual;
    setString2(1, aBinType);
    setString2(2, aTxnCode);
    setString(aBaseDate);

    sqlSelect(strSql);
    // String ls_expire_date =col_ss("expire_date");
    // ddd("expire_date="+ls_expire_date);

    return colStr("expire_date");
  }

  public String complExpireDate(String aBinType, String aTxnCode, String aBaseDate) {
    // -arbit_expire_date-
    strSql =
        "select " + commSqlStr.sqlID + "uf_rsk_stage_days(?,?,'5',?) as expire_date from"
            + commSqlStr.sqlDual;
    setString2(1, aBinType);
    setString2(2, aTxnCode);
    setString(aBaseDate);

    sqlSelect(strSql);
    // String ls_expire_date =col_ss("expire_date");
    // ddd("expire_date="+ls_expire_date);

    return colStr("expire_date");
  }

}
