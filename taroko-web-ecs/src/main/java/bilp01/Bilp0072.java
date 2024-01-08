/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-31  V1.00.00  yash       program initial                            *
*109-04-23   V1.00.01  shiyuqi       updated for project coding standard     *      
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    *                                                                           *
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0072 extends BaseEdit {
  String mExBatchNo = "";

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
    wp.whereStr = " where 1=1  and o.apr_user='' and post_flag !='Y' and error_code='' ";

    if (empty(wp.itemStr("ex_key_no")) == false) {
      wp.whereStr += " and  o.key_no = :ex_key_no ";
      setString("ex_key_no", wp.itemStr("ex_key_no"));
    }

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.whereStr += " and  o.mod_user = :ex_user ";
      setString("ex_user", wp.itemStr("ex_user"));
    }

    if (empty(wp.itemStr("ex_idno")) == false) {
      wp.whereStr += " and  i.id_no = :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno"));
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  o.curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
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

    wp.selectSQL = " o.acct_type" + ", i.id_no" + ", i.chi_name" + ", o.corp_no" + ", o.add_item"
        + ", o.card_no" + ", o.curr_code" + ", o.dc_dest_amt" + ", o.dest_amt" + ", o.purchase_date"
        + ", o.bill_desc" + ", o.mod_user" + ", o.apr_user" + ", o.mod_seqno"
        + ",hex(o.rowid) as rowid";

    wp.daoTable = "bil_othexp o left join crd_card c on o.card_no = c.card_no ";
    wp.daoTable += "            left join crd_idno i on c.id_p_seqno = i.id_p_seqno ";
    wp.whereOrder = " order by o.acct_type";

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
    mExBatchNo = wp.itemStr("batch_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExBatchNo = wp.itemStr("kk_batch_no");
    if (empty(mExBatchNo)) {
      mExBatchNo = itemKk("data_k1");
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", batch_no " + ", xxx" + ", crt_date" + ", crt_user";
    wp.daoTable = "bil_postcntl";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  batch_no = :batch_no ";
    setString("batch_no", mExBatchNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, batch_no=" + mExBatchNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {


    Bilp0072Func func = new Bilp0072Func(wp);
    int llOK = 0, llErr = 0;
    if (strAction.equals("S2")) {

      String[] aaOpt = wp.itemBuff("opt");
      String[] hRowid = wp.itemBuff("h_rowid");
      String[] hModSeqno = wp.itemBuff("h_mod_seqno");
      wp.listCount[0] = hRowid.length;
      int rr = -1;
      for (int ll = 0; ll < aaOpt.length; ll++) {
        rr = optToIndex(aaOpt[ll]);
        if (rr < 0) {
          continue;
        }
        // var
        func.varsSet("h_rowid", hRowid[rr]);
        func.varsSet("h_mod_seqno", hModSeqno[rr]);
        func.varsSet("h_apr_user", wp.loginUser);

        if (func.dbUpdate() == 1) {
          wp.colSet(ll, "ok_flag", "V");
          llOK++;
          sqlCommit(1);

        } else {
          wp.colSet(ll, "ok_flag", "X");
          llErr++;
          sqlCommit(0);

        }

      }

    }
    alertMsg("放行資料處理完成; OK = " + llOK + ", ERR = " + llErr);

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
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
    } catch (Exception ex) {
    }
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
