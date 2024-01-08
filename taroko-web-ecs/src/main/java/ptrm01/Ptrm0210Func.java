/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/07/22  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard    *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
***************************************************************************/
package ptrm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ptrm0210Func extends FuncEdit {
  private String PROGNAME = "幣別資料參數維護處理程式108/07/22 V1.00.01";
  String currCode;
  String controlTabName = "ptr_currcode";

  public Ptrm0210Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      currCode = wp.itemStr("kk_curr_code");
    } else {
      currCode = wp.itemStr("curr_code");
    }
    if (this.ibAdd)
      if (currCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where curr_code = ? ";
        Object[] param = new Object[] {currCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[幣別代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      strSql = "select count(*) as qua " + "from " + controlTabName + " "
          + "where bill_sort_seq = ? " + "and   bill_sort_seq != '' ";

      Object[] param = new Object[] {wp.itemStr("bill_sort_seq")};
      sqlSelect(strSql, param);
      int qua = (int) colNum("qua");
      int dupCnt = 0;
      if (this.ibUpdate)
        dupCnt = 1;
      if (qua > dupCnt) {
        errmsg("對帳單幣別排列順序: 不可重複 , 請重新輸入!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("curr_eng_name")) {
        errmsg("英文幣別名稱: 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " curr_code, " + " curr_eng_name, "
        + " curr_chi_name, " + " curr_code_gl, " + " country_code, " + " bill_curr_code, "
        + " bill_sort_seq, " + " curr_amt_dp, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {currCode, wp.itemStr("curr_eng_name"), wp.itemStr("curr_chi_name"),
        wp.itemStr("curr_code_gl"), wp.itemStr("country_code"), wp.itemStr("bill_curr_code"),
        wp.itemStr("bill_sort_seq"), wp.itemNum("curr_amt_dp"), wp.itemStr("approval_user"),
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "curr_eng_name = ?, " + "curr_chi_name = ?, "
        + "curr_code_gl = ?, " + "country_code = ?, " + "bill_curr_code = ?, "
        + "bill_sort_seq = ?, " + "curr_amt_dp = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("curr_eng_name"), wp.itemStr("curr_chi_name"),
        wp.itemStr("curr_code_gl"), wp.itemStr("country_code"), wp.itemStr("bill_curr_code"),
        wp.itemStr("bill_sort_seq"), wp.itemNum("curr_amt_dp"), wp.itemStr("approval_user"),
        wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
