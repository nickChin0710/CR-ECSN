/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-13  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱                           *
* 109-09-16  V1.00.03  JeffKung 處理後不再重新query資料, 改為USER重新查詢才顯示新的資料  *
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名                                                                                      *  
* 111-03-29  V1.00.05   Ryan     覆核人員與異動人員相同不能覆核                       *  
******************************************************************************/
package dbbm01;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Dbbp0040 extends BaseProc {
  CommString commString = new CommString();

  String msg = "", msgok = "";


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_batch_unit");
      this.dddwList("dddw_liab_type", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");
    } catch (Exception ex) {
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 and manual_upd_flag = 'Y' ";

    if (empty(wp.itemStr("ex_batch_unit")) == false) {
      wp.whereStr += " and batch_no like :ex_batch_unit ";
      setString("ex_batch_unit", "%" + wp.itemStr("ex_batch_unit") + "%");
    }
    if (empty(wp.itemStr("ex_batch_no")) == false) {
      wp.whereStr += " and batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(rowid) as rowid, " + " card_no, " + " batch_no, " + " purchase_date, "
        + " film_no, " + " dest_amt, " + " dest_curr, " + " source_amt, " + " source_curr,"
        + " doubt_type," + " reference_no," + " curr_post_flag," + " mod_user," + " mod_time,"
        + " mod_pgm," + " mod_seqno, " + " mod_log," + " rsk_type," + " format_chk_ok_flag,"
        + " manual_upd_flag," + " txn_code," + " bill_type  ";
    wp.daoTable = " dbb_curpost ";
    wp.whereOrder = "  ";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
    apprDisabled("mod_user");
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    busi.SqlPrepare sp = new SqlPrepare();
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaReferenceNo = wp.itemBuff("reference_no");
    String[] aaModLog = wp.itemBuff("mod_log");
    wp.listCount[0] = aaReferenceNo.length;
    // -update-
    for (int ll = 0; ll < aaReferenceNo.length; ll++) {
      if (!checkBoxOptOn(ll, aaOpt)) {
        continue;
      }
      if (aaModLog[ll].equals("D")) {
        String sql_delete = "Delete From dbb_curpost Where reference_no = :aa_reference_no";
        setString("aa_reference_no", aaReferenceNo[ll]);
        sqlExec(sql_delete);
        if (sqlRowNum <= 0) {
          sqlCommit(0);
          wp.colSet(ll, "ok_flag", "!");
          alertMsg("資料處理失敗");
          return;
        }
        continue;
      }
      sp.sql2Update("dbb_curpost");
      sp.ppstr("manual_upd_flag", "P");
      sp.sql2Where(" where reference_no=?", aaReferenceNo[ll]);
      sp.sql2Where(" and mod_seqno=?", aaModSeqno[ll]);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        wp.colSet(ll, "ok_flag", "!");
        alertMsg("資料處理失敗");
        return;
      }

    }
    sqlCommit(1);
    //queryFunc(); 
    alertMsg("資料處理成功");

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() {
    String modLog = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      modLog = wp.colStr(ii, "mod_log");
      wp.colSet(ii, "tt_mod_log", commString.decode(modLog, ",D,1,0", ",刪除,否,否"));
    }
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
