/*****************************************************************************
*                                                                            *

*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-23  V1.00.00  yash       program initial                            *
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     *   
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    *                                                                            *
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0150 extends BaseEdit {
  String mExSeqNo = "";
  String errmsg = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 and manual_upd_flag = 'Y' ";

    if (empty(wp.itemStr("ex_batch_no")) == false) {
      wp.whereStr += " and batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }

    if (empty(wp.itemStr("ex_batch_unit")) == false) {
      wp.whereStr += " and  substr(bill_type,1,2)  = :ex_batch_unit ";
      setString("ex_batch_unit", wp.itemStr("ex_batch_unit"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " card_no" + ", reference_no  " + ", purchase_date" + ", film_no"
        + ", source_amt" + ", bill_type" + ", source_curr" + ", txn_code" + ", dest_amt"
        + ", dest_curr" + ", mod_pgm" + ", mod_log" + ", hex(rowid) as rowid, mod_seqno ,mod_user ";

    wp.daoTable = "bil_curpost ";
    wp.whereOrder = " order by card_no";
//    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    apprDisabled("mod_user");
  }

  @Override
  public void querySelect() throws Exception {
    mExSeqNo = wp.itemStr("seq_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExSeqNo = wp.itemStr("kk_seq_no");
    if (empty(mExSeqNo)) {
      mExSeqNo = itemKk("data_k1");
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", batch_no " + ", xxx" + ", crt_date" + ", crt_user";
    wp.daoTable = "bil_postcntl";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  batch_no = :batch_no ";
    setString("batch_no", mExSeqNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, batch_no=" + mExSeqNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {



    String[] aaOpt = wp.itemBuff("opt");
    String[] aaModLog = wp.itemBuff("mod_log");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");


    wp.listCount[0] = aaRowid.length;

    int rr = -1;
    int llOk = 0, llErr = 0;
    for (int ll = 0; ll < aaOpt.length; ll++) {

      rr = optToIndex(aaOpt[ll]);
      if (rr < 0) {
        continue;
      }

      if (aaModLog[ll].equals("D")) {

        if (wfDelFile(aaRowid[ll], aaModSeqno[ll]) == 1) {
          wp.colSet(rr, "ok_flag", "V");
          llOk++;
        } else {
          wp.colSet(rr, "ok_flag", "!");
          llErr++;
        }
      } else {
        if (wfUpFile(aaRowid[ll], aaModSeqno[ll]) == 1) {
          wp.colSet(rr, "ok_flag", "V");
          llOk++;
        } else {
          wp.colSet(rr, "ok_flag", "!");
          llErr++;
        }
      }



    }


    sqlCommit(llOk > 0 ? 1 : 0);
    alertMsg("放行完成; OK = " + llOk + ", ERR = " + llErr);

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_batch_unit");
      this.dddwList("dddw_batch_unit", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");
    } catch (Exception ex) {
    }
  }

  public int wfDelFile(String rowid, String modSeqno) throws Exception {

    String ldsSql =
        "delete from bil_curpost   where  hex(rowid) = :rowid  and  mod_seqno = :mod_seqno ";
    setString("rowid", rowid);
    setString("mod_seqno", modSeqno);
    sqlExec(ldsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }

    return 1;

  }

  public int wfUpFile(String rowid, String modSeqno) throws Exception {

    String upSql = "update bil_curpost set " + "manual_upd_flag =:flag "
        + " , mod_user =:mod_user, mod_time=sysdate " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + "where  hex(rowid) = :rowid  and  mod_seqno = :mod_seqno ";
    setString("flag", "P");
    setString("mod_user", wp.loginUser);
    setString("rowid", rowid);
    setString("mod_seqno", modSeqno);
    sqlExec(upSql);
    if (sqlRowNum <= 0) {
      return -1;
    }

    return 1;

  }
//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}
}
