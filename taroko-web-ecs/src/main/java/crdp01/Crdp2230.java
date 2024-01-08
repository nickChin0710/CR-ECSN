/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-12  V1.00.00  Andy       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package crdp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdp2230 extends BaseEdit {
  String mExMchtNo = "";
  String mExProductNo = "";
  String mRowid = "";
  String mModSeqno = "";

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
    String exBatchno = wp.itemStr("ex_batchno");
    // 固定條件
    wp.whereStr = " where 1=1  ";
    wp.whereStr +=
        "and (decode(in_main_error,'','0', in_main_error) != '0')" + " and in_main_date = '' ";
    //
    wp.whereStr += sqlCol(exBatchno, "batchno");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "batchno, " + "recno, "
        + "(batchno||'-'||recno)as wk_batchno, " + "emboss_source, " + "card_type, " + "unit_code, "
        + "pp_card_no, " + "old_card_no, " + "id_no, " + "id_no_code, "
        + "(id_no||'-'||id_no_code)as wk_id_no, " + "eng_name, " + "zip_code, " + "mail_addr1, "
        + "mail_addr2, " + "mail_addr3, " + "mail_addr4, " + "mail_addr5, " + "valid_fm, "
        + "valid_to, " + "mail_type, " + "mail_no, " + "mail_branch, " + "mail_proc_date, "
        + "old_beg_date, " + "old_end_date, " + "in_main_date, " + "in_main_error, " + "mod_time, "
        + "mod_pgm, " + "mod_seqno, " + "'0' db_optcode, " + "change_reason, " + "mod_user ";
    wp.daoTable = "crd_emboss_pp";
    wp.whereOrder = " order by wk_batchno ";
    getWhereStr();

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    String emboss = "";
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      emboss = wp.colStr(ii, "emboss_source");
      wp.colSet(ii, "wk_emboss_source",
          commString.decode(emboss, ",1,2,3,4,5,7", ",新製卡,普升金,整批續卡,提前續卡,重製,緊急補發"));
      emboss = wp.colStr(ii, "in_main_error");
      wp.colSet(ii, "wk_in_main_error", commString.decode(emboss, ",1,2", ",已無有效信用卡主卡,原貴賓卡已無效"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");
    String[] aaBatchno = wp.itemBuff("batchno");
    String[] aaEmbossSource = wp.itemBuff("emboss_source");
    String[] aaOldCardNo = wp.itemBuff("old_card_no");
    wp.listCount[0] = aaBatchno.length;
    String usSql = "";
    // check
    int rr = -1;
    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      mRowid = aaRowid[rr];
      mModSeqno = aaModSeqno[rr];
      if (wfAddReject() != 1) {
        wp.colSet(rr, "ok_flag", "!");
        llErr++;
        continue;
      }
      // if(wf_update_card(rr) != 1){
      // wp.col_set(rr, "ok_flag", "!");
      // ll_err++;
      // continue;
      // }
      if (aaEmbossSource[ii].equals("3") || aaEmbossSource[ii].equals("4")) {
        usSql = "update crd_emboss_pp " + "set change_status = '4', " + "mod_user =:mod_user, "
            + "mod_time = sysdate, " + "mod_pgm = 'crdp2230', " + "mod_seqno = nvl(mod_seqno,0)+1 "
            + "where pp_card_no =:pp_card_no ";
        setString("mod_user", wp.loginUser);
        setString("pp_card_no", aaOldCardNo[ii]);
        sqlExec(usSql);
        if (sqlRowNum <= 0) {
          llErr++;
          continue;
        }
      }
    }
    if (llErr > 0) {
      sqlCommit(0);
      alertMsg("退件處理失敗!!");
    } else {
      sqlCommit(1);
      alertMsg("退件處理成功!!");
    }
    queryFunc();
  }

  public int wfAddReject() {
    String usSql = "";
    usSql = "update crd_emboss_pp set in_main_date =:in_main_date " + "where hex(rowid) =:m_rowid "
        + "and mod_seqno =:m_mod_seqno ";
    setString("in_main_date", getSysDate());
    setString("m_rowid", mRowid);
    setString("m_mod_seqno", mModSeqno);
    sqlExec(usSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;
  }

  public int wfUpdateCard(int ll) {
    String[] aaOldCardNo = wp.itemBuff("old_card_no");
    String[] aaEmbossSource = wp.itemBuff("emboss_source");
    String usSql = "";
    if (aaEmbossSource[ll].equals("1")) {
      return 1;
    }
    if (aaEmbossSource[ll].equals("3") || aaEmbossSource[ll].equals("4")) {
      usSql = "update crd_emboss_pp " + "set change_status = '4', " + "mod_user =:mod_user, "
          + "mod_time = sysdate, " + "mod_pgm = 'crdp2230', " + "mod_seqno = nvl(mod_seqno,0)+1 "
          + "where pp_card_no =:pp_card_no ";
      setString("mod_user", wp.loginUser);
      setString("pp_card_no", aaOldCardNo[ll]);
      sqlExec(usSql);
      if (sqlRowNum <= 0) {
        return -1;
      }
    }
    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

  }

}
