
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package ccam02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5070 extends BaseEdit {
  Ccam5070Func func;
  String cardNote, areaType = "T", prdAttrib = "P";

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
    }
    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam5070")) {
    	  wp.optionKey =wp.itemStr("ex_card_note");			
		  dddwAddOption("*","*_通用");
		  dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5070_detl")) {
        wp.optionKey = wp.itemStr("kk_card_note");
        dddwAddOption("*","*_通用");
		dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr =
        "where area_type ='T' and prd_attrib='P'" + sqlCol(wp.itemStr("ex_card_note"), "card_note");
    wp.queryWhere = wp.whereStr;

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "card_note ," + "start_date ," + "end_date ," + "prd_remark ,"
        + "nat_lmt_amt_day as nat_amt_day," + "nat_lmt_cnt_day as nat_cnt_day,"
        + "nat_lmt_amt_time as nat_amt_time," + "mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "CCA_PRD_TYPE_INTR";
    wp.whereOrder = " order by card_note,start_date ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "tt_card_note", checkCardNote(wp.colStr(ii, "card_note")));
    }

    wp.setPageValue();
  }

  String checkCardNote(String cardNote) {
    if (empty(cardNote))
      return "";

    if (eqIgno(cardNote, "*"))
      return "通用";

    return ecsfunc.DeCodeCrd.cardNote(cardNote);
  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNote)) {
      cardNote = itemKk("card_note");
    }

    if (empty(cardNote)) {
      alertErr("卡片等級: 不可空白");
      return;
    }

    wp.selectSQL = " hex(rowid) as rowid, " + " mod_seqno , " + " card_note , " + " start_date , "
        + " end_date , " + " prd_remark , " + " nat_lmt_amt_time as nat_amt_time , "
        + " nat_lmt_amt_day as nat_amt_day , " + " nat_lmt_cnt_day  as nat_cnt_day , "
        + " crt_user , " + " crt_date , " + " mod_user , " + "uf_2ymd(mod_time) as mod_date ";
    wp.daoTable = "CCA_PRD_TYPE_INTR";
    wp.whereStr = "where 1=1" + sqlCol(cardNote, "card_note") + sqlCol(areaType, "area_type")
        + sqlCol(prdAttrib, "prd_attrib");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote + ",");
      return;
    }

    wp.colSet("tt_card_note", checkCardNote(wp.colStr("card_note")));

  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ccam5070Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("start_date", getSysDate());
      wp.colSet("end_date", "99991231");
      wp.colSet("nat_cnt_day", "1");
    }
  }

}
