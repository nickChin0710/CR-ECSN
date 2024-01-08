/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/09/23  V1.00.01   Ryan          Initial                              *
*                                                                          *
***************************************************************************/
package ipsm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ipsm0010Func extends FuncEdit {

  public Ipsm0010Func(TarokoCommon wr) {
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
		// -other modify-
		String sqlwhere = " where 1=1 and rowid = ? and nvl(mod_seqno,0) = ?";
		Object[] param = new Object[] { wp.itemRowId("rowid"), wp.itemNum("mod_seqno") };
		if (this.isOtherModify("ptr_sys_parm", sqlwhere, param)) {
			errmsg("請重新查詢 !");
			return;
		}

		if (checkDecnum(wp.itemStr("wf_value6"), 11, 2) != 0) {
			errmsg("每日自動加值次數上限: 格式超出範圍 : [11][2]");
			return;
		}

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update ptr_sys_parm set " + "wf_value6 = ?, "+" wf_value7 = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemNum("wf_value6"),wp.itemNum("wf_value7"), wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 ptr_sys_parm 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    return 1;
  }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }
  // ************************************************************************

} // End of class
