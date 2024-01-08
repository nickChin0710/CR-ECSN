/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-31  V1.00.00  yash       program initial                            *
* 108-08-20  V1.00.01  Andy       bug fix                                    *
*109-04-23   V1.00.02  shiyuqi       updated for project coding standard     * 
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/

package bilp01;

import java.util.concurrent.TimeUnit;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0010 extends BaseEdit {
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

  @Override
  public void initPage() {
    String sysdate1 = "";
    sysdate1 = strMid(getSysDate(), 0, 8);
    wp.colSet("exDateS", sysdate1);
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1   ";

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and  batch_date >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and  batch_date <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
    }

    if (wp.itemStr("ex_confirm_flag_p").contains("Y")) {
      wp.whereStr += " and  confirm_flag_p = :ex_confirm_flag_p ";
      setString("ex_confirm_flag_p", wp.itemStr("ex_confirm_flag_p"));
    } else {
      wp.whereStr += " and  confirm_flag_p <> 'Y' ";
    }


    if (empty(wp.itemStr("ex_batch_unit")) == false) {
      wp.whereStr += " and  batch_unit = :ex_batch_unit ";
      setString("ex_batch_unit", wp.itemStr("ex_batch_unit"));
    }

    if (empty(wp.itemStr("ex_batch_seq")) == false) {
      wp.whereStr += " and  batch_seq = :ex_batch_seq ";
      setString("ex_batch_seq", wp.itemStr("ex_batch_seq"));
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

    wp.selectSQL = " batch_date" + ", batch_unit" + ", batch_seq" + ", batch_no" + ", tot_record"
        + ", tot_amt" + ", this_close_date" + ", confirm_flag" + ", confirm_flag_p" + ", mod_user"
        + ", mod_seqno" + ", batch_no" + ", stmt_cycle ";

    wp.daoTable = "bil_postcntl";
    wp.whereOrder = " order by batch_date";
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

    Bilp0010Func func = new Bilp0010Func(wp);

    int llOk = 0, llErr = 0;
    if (strAction.equals("S2")) {

      String[] aaOpt = wp.itemBuff("opt");
      String[] aaBatchNo = wp.itemBuff("batch_no");
      String[] aaModSeqno = wp.itemBuff("mod_seqno");
      String[] aaConfirmFlagP = wp.itemBuff("confirm_flag_p");
      String[] aaStmtCycle = wp.itemBuff("stmt_cycle");
      wp.listCount[0] = aaBatchNo.length;
      String lsStmtCycle = "", lsRun = "", lsBatchNo = "", lsSql = "", lsMediaName = "";
      String lsMesg = "";
//      int rr = -1;
      for (int ll = 0; ll < aaBatchNo.length; ll++) {

//        rr = optToIndex(aaOpt[ll]);
//        if (rr < 0) {
//          continue;
//        }
    	if (checkBoxOptOn(ll,aaOpt) == false)
    	   continue;
        if (aaConfirmFlagP[ll].equals("Y")) {
          llErr++;
          continue;
        }
        // var
        func.varsSet("aa_batch_no", aaBatchNo[ll]);
        func.varsSet("aa_mod_seqno", aaModSeqno[ll]);

        if (func.dbUpdate() == 1) {
          llOk++;
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
        } else {
          llErr++;
          wp.colSet(ll, "ok_flag", "X");
          sqlCommit(0);
          continue;
        }
      }

    }
    alertMsg("放行資料處理完成; OK = " + llOk + ", ERR = " + llErr);

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
  

//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}

}
