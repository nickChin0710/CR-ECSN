/*****************************************************************************
*                                                                            *

*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-03  V1.00.00  David FU   program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard  
* 111-08-19  V1.00.01  Machao     新增”最近異動時間” 欄位      *
* 111-12-19  V1.00.02  Zuwei      gen_user_log 資料不存在者, 新增一筆     *
******************************************************************************/

package genp01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Genp0150 extends BaseEdit {
  String mExUSER = "";
  private static String progranCd = "GenA002";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      // queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      // dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      // queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      // querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      // clearFunc();
    }

    initButton();
  }

  @Override
  public void initPage() {
	  String lsSql = "",db_mod_date ="",db_business_date="";
		// 最後異動軋帳時間
		wp.colSet("ex_USER", wp.loginUser);

		lsSql = "select to_char(max(mod_time),'yyyy/mm/dd hh24:mi:ss') db_mod_time,"
				+ "to_char(max(mod_time),'yyyy/mm/dd') db_mod_date "
				+ "from gen_user_log where 1=1 ";
		lsSql += sqlCol(wp.loginUser, "mod_user");
		try {
			sqlSelect(lsSql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db_mod_date = sqlStr("db_mod_date");
		wp.colSet("db_mod_time", sqlStr("db_mod_time"));
		
		// 判斷是否仍有未覆核資料
		lsSql = "select count(*) ct from gen_vouch "
				+ "where 1=1 "
				+ "and decode(post_flag, '' , 'N', post_flag)='N' "
				+ "and decode(jrn_status, '', '0', jrn_status) <> '1' "
				+ "and decode(mod_log, '', '0', mod_log) not in ('D','U') "
				+ "and decode(jrn_status, '', '2', jrn_status) = '2' ";
		try {
//			ddd(lssql);
			sqlSelect(lsSql);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (sqlInt("ct") > 0) {
			alertErr("尚有會計帳未放行(genP0120), 不可執行軋帳覆核作業");
			wp.colSet("set_btn", "disabled = disabled style='background: lightgray;'");
			return;
		}
  }

  @Override
  public void queryFunc() throws Exception {}

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    if (strAction.equalsIgnoreCase("U") == false)
      return;

    mExUSER = wp.itemStr("ex_USER");
    String sql = "select usr_id from sec_user where usr_id = ?";
    Object[] param = new Object[] {mExUSER};
    sqlSelect(sql, param);
    if (sqlRowNum <= 0) {
      alertErr2("覆核者代號不存在");
      return;
    }
    
    sql = "select 1 from gen_user_log where PROGRAN_CD = ?";
    param = new Object[] {progranCd};
    sqlSelect(sql, param);
    if (sqlRowNum <= 0) {
        sql = "insert into gen_user_log("
                + "PROGRAN_CD, "
                + "CRT_USER, "
                + "mod_user, "
                + "mod_time, "
                + "mod_pgm, "
                + "mod_seqno) "
                + " values(?, ?, ?, sysdate, ?, 1)";
        param = new Object[] {progranCd, mExUSER, wp.loginUser, wp.modPgm()};
        sqlExec(sql, param);
    } else {
        // ========================================
        sql = "update gen_user_log set " + " CRT_USER =? "
            + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
            + " where PROGRAN_CD = ?";
        param = new Object[] {mExUSER, wp.loginUser, wp.modPgm(), progranCd};
        sqlExec(sql, param);
    }
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    sqlCommit(1);
	String lsSql = "select to_char(max(mod_time),'yyyy/mm/dd hh24:mi:ss') db_mod_time from gen_user_log where 1=1 ";
	lsSql += sqlCol(wp.loginUser, "mod_user");
	sqlSelect(lsSql);
	wp.colSet("db_mod_time", sqlStr("db_mod_time"));
  }

  @Override
  public void initButton() {

  }

}
