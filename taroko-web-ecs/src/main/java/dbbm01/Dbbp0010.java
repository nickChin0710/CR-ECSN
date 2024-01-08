/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-23  V1.00.01  Justin        parameterize sql
* 111-03-29  V1.00.02   Ryan      覆核人員與異動人員相同不能覆核                      *  
******************************************************************************/
package dbbm01;

import dbbm01.Dbbp0010Func;
import ofcapp.BaseAction;

public class Dbbp0010 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere =
        " where 1=1 " + " and (post_flag = 'N' or post_flag = '') " + " and apr_flag <> 'Y' "  + " and error_code ='' "
            + sqlCol(wp.itemStr("ex_key_no"), "key_no") + sqlCol(wp.itemStr("ex_user"), "mod_user");

    if (!wp.itemEmpty("ex_idno")) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from dbc_idno "
      		           + " where 1=1 " 
                       + sqlCol(wp.itemStr("ex_idno"), "id_no") 
                       + ") ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
    
  }

  @Override
  public void queryRead() throws Exception {

    wp.selectSQL = "" + " uf_idno_id2(id_p_seqno,'Y') as id_no ," + " id_p_seqno ,"
        + " uf_idno_name2(id_p_seqno,'Y') as chi_name ," + " acct_type ," + " add_item ,"
        + " card_no ," + " key_no ," + " dest_amt ," + " dest_curr ," + " purchase_date ,"
        + " chi_desc ," + " bill_desc ," + " dept_flag ," + " apr_flag ," + " apr_user ,"
        + " post_flag ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ,"
        + " hex(rowid) as rowid ";

    wp.daoTable = " dbb_othexp ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    apprDisabled("mod_user");
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int isOk = 0, isError = 0;

    String[] rowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("rowid");

    Dbbp0010Func func = new Dbbp0010Func();
    func.setConn(wp);

    for (int ii = 0; ii < wp.itemRows("rowid"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;

      func.varsSet("rowid", rowid[ii]);

      if (func.dataProc() == 1) {
        isOk++;
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        isError++;
        wp.colSet(ii, "ok_flag", "X");
        continue;
      }

    }

    if (isOk > 0) {
      sqlCommit(1);
    }

    alertMsg("覆核完成 , 成功:" + isOk + " 失敗:" + isError);

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  
	public void apprDisabled(String col) throws Exception {
		for (int ll = 0; ll < wp.listCount[0]; ll++) {
			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
				wp.colSet(ll, "opt_disabled", "");
			} else
				wp.colSet(ll, "opt_disabled", "disabled");
		}
	}
}
