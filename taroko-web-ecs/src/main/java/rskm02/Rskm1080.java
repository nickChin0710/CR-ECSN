/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1206  V1.00.01  Alex  add initButton
* 109-04-27  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/

package rskm02;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Rskm1080 extends BaseAction {

  String condCode = "", aprFlag = "";

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
      // -明細覆核-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -複製-
      copyFunc();
    }
  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_data_from"), "data_from")
        + sqlCol(wp.itemStr("ex_cond_code"), "cond_code", "like%");

    if (wp.itemEmpty("ex_trial_type") == false || wp.itemEq("ex_apr_flag", "0") == false) {
      lsWhere += " and cond_code in " + " (select cond_code from rsk_score_parmdtl where 1=1 "
          + sqlCol(wp.itemStr("ex_trial_type"), "trial_type");
      if (wp.itemEq("ex_apr_flag", "0") == false) {
        lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
      }
      lsWhere += " ) ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " cond_code , " + " cond_desc , " + " data_from , "
        + " decode(data_from,'1','本行資料','2','JCIC資料','') as tt_data_from , " + " exec_flag , "
        + " cond_seq_desc , " + " apr_flag , "
        + " (select count(*) from rsk_score_parmdtl B where B.cond_code=A.cond_code and apr_flag ='Y') as cond_cnt_Y , "
        + " (select count(*) from rsk_score_parmdtl B where B.cond_code=A.cond_code and apr_flag <>'Y') as cond_cnt_N ";

    wp.daoTable = " rsk_score_parm A ";
    wp.whereOrder = " order by cond_code, apr_flag ";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      wp.colSet("copy_trial_type", "");
      wp.colSet("tt_copy_trial_type", "");
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("copy_trial_type", wp.itemStr("ex_trial_type"));
    wp.colSet("tt_copy_trial_type", ttTrialType(wp.itemStr("ex_trial_type")));
    wp.setPageValue();
  }

  String ttTrialType(String lsTrialType) {
    String sql1 =
        " select wf_desc from ptr_sys_idtab where wf_type='RSK_TRIAL_TYPE' and wf_id = ? ";
    sqlSelect(sql1, new Object[] {lsTrialType});
    if (sqlRowNum > 0)
      return "_" + sqlStr("wf_desc");
    return "";
  }

  @Override
  public void querySelect() throws Exception {
    condCode = wp.itemStr("data_k1");
    aprFlag = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(condCode))
      condCode = wp.itemStr("cond_code");
    if (empty(aprFlag))
      aprFlag = wp.itemNvl("apr_flag", "N");

    if (empty(condCode)) {
      alertErr2("評分條件代碼: 不可空白");
      return;
    }

    wp.selectSQL = " cond_code , " + " cond_desc , " + " cond_seq_desc , " + " data_from , "
        + " data_type as ls_data_type , " + " data_desc , " + " apr_flag , "
        + " hex(rowid) as rowid , " + " mod_seqno ";
    wp.daoTable = "rsk_score_parm";
    wp.whereStr = " where 1=1 and apr_flag = 'Y' " + sqlCol(condCode, "cond_code");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + condCode);
      return;
    }

    if (eqIgno(wp.colStr("ls_data_type"), "1")) {
      wp.colSet("tt_data_type", ".文字");
    } else if (eqIgno(wp.colStr("ls_data_type"), "2")) {
      wp.colSet("tt_data_type", ".數值");
    }
    detlRead();
  }

  boolean checkApr() {

    String sql1 =
        " select count(*) as db_cnt from rsk_score_parmdtl where cond_code = ? and apr_flag <> 'Y' ";
    sqlSelect(sql1, new Object[] {wp.colStr("cond_code")});
    if (sqlNum("db_cnt") > 0)
      return true;

    return false;
  }

  void detlRead() throws Exception {
    String lsAprFlag = "";
    lsAprFlag = wp.itemStr("kk_apr_flag");

    wp.selectSQL = " cond_code , " + " cond_data_seq , " + " cond_data_desc , " + " data_char1 , "
        + " data_char2 , " + " data_num1 , " + " data_num2 , " + " data_score , " + " dsp_color , "
        + " mod_user , " + " mod_time , " + " data_type , " + " trial_type , " + " apr_flag ";
    wp.daoTable = " rsk_score_parmdtl ";
    wp.whereStr =
        " where 1=1" + sqlCol(condCode, "cond_code") + sqlCol(wp.itemStr("kk_trial_type"), "trial_type");
    if (empty(lsAprFlag)) {
      if (checkApr() == true)
        wp.whereStr += " and apr_flag <> 'Y' ";
      else
        wp.whereStr += " and apr_flag = 'Y' ";
    } else {
      wp.whereStr += sqlCol(lsAprFlag, "apr_flag");
    }

    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
    listWkdata();
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  void listWkdata() {
    if (wp.colEq("data_from", "1")) {
      wp.colSet("tt_data_from", "本行資料");
    } else if (wp.colEq("data_from", "2")) {
      wp.colSet("tt_data_from", "JCIC資料");
    }

    // --計算明細已覆核未覆核筆數
    String sql0 = " select " + " sum(decode(apr_flag,'Y',1,0)) as apr_cnt_Y , "
        + " sum(decode(apr_flag,'N',1,0)) as apr_cnt_N " + " from rsk_score_parmdtl "
        + " where cond_code = ? " + sqlCol(wp.itemStr("kk_trial_type"), "trial_type");

    sqlSelect(sql0, new Object[] {wp.colStr("cond_code")});

    wp.colSet("apr_cnt_Y", sqlStr("apr_cnt_Y"));
    wp.colSet("apr_cnt_N", sqlStr("apr_cnt_N"));

    // --
    String sql1 = " select wf_id ||'_'|| wf_desc as tt_trial_type " + " from ptr_sys_idtab"
        + " where wf_type = 'RSK_TRIAL_TYPE' and wf_id = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      condCode = wp.colStr(ii, "trial_type");
      sqlSelect(sql1, new Object[] {condCode});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_trial_type", sqlStr("tt_trial_type"));
      }
    }
  }

  @Override
  public void saveFunc() throws Exception {
    rskm02.Rskm1080Func func = new rskm02.Rskm1080Func();
    func.setConn(wp);

    wp.listCount[0] = wp.itemRows("cond_data_seq");

    if (isDelete() && checkAprY() == false) {
      if (checkApproveZz() == false)
        return;
    }

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }
    if (this.isUpdate() || this.isAdd()) {
      detl2Save();
    }
  }

  boolean checkAprY() {

    String sql1 =
        " select count(*) as db_cnt from rsk_score_parmdtl where cond_code =? and apr_flag = 'Y' ";
    sqlSelect(sql1, new Object[] {wp.itemStr("cond_code")});
    if (sqlNum("db_cnt") > 0)
      return false;

    return true;
  }

  void detl2Save() throws Exception {
    int ilOk = 0, ilErr = 0;
    rskm02.Rskm1080Func func = new rskm02.Rskm1080Func();
    func.setConn(wp);

    boolean lbApr = false;

    if (wp.itemEmpty("approval_user") == false && wp.itemEmpty("approval_passwd") == false) {
      if (checkApproveZz() == false)
        return;
      lbApr = true;
    }

    String lsCondCode = wp.itemStr("cond_code");
    String[] lsAprFlag = wp.itemBuff("apr_flag");
    String[] lsCondDataSeq = wp.itemBuff("cond_data_seq");
    String[] lsTrialType = wp.itemBuff("trial_type");
    String[] lsDataType = wp.itemBuff("data_type");
    String[] lsDspColor = wp.itemBuff("dsp_color");
    String[] lsDataChar1 = wp.itemBuff("data_char1");
    String[] lsDataChar2 = wp.itemBuff("data_char2");
    String[] lsDataNum1 = wp.itemBuff("data_num1");
    String[] lsDataNum2 = wp.itemBuff("data_num2");
    String[] lsDataScore = wp.itemBuff("data_score");
    String[] lsCondDataDesc = wp.itemBuff("cond_data_desc");

    String[] aaOpt = wp.itemBuff("opt");
    
    // --只要有一筆失敗就rollback , 無失敗再commit
    for (int ii = 0; ii < wp.itemRows("cond_data_seq"); ii++) {
      // --刪除明細
      if (checkBoxOptOn(ii, aaOpt)) {
        if (eqIgno(lsAprFlag[ii], "Y") && lbApr == false) {
          wp.colSet(ii, "ok_flag", "X");
          ilErr++;
          break;
        }
        func.varsSet("cond_code", lsCondCode);
        func.varsSet("cond_data_seq", lsCondDataSeq[ii]);
        func.varsSet("apr_flag", lsAprFlag[ii]);
        func.varsSet("trial_type", lsTrialType[ii]);
        rc = func.deleteScoreDetl();
        if (rc != 1) {
          wp.colSet(ii, "ok_flag", "X");
          ilErr++;
          dbRollback();
          break;
        }
        ilOk++;
        wp.colSet(ii, "ok_flag", "V");
        continue;
      }

      // --明細異動:若為已覆核資料需確認該筆資料是否有未覆核資料,若有未覆核資料則不可異動,需先處理未覆核資料
      if (eqIgno(lsAprFlag[ii], "Y")) {
        if (checkData(lsCondCode, lsCondDataSeq[ii], lsTrialType[ii]) == false) {
          wp.colSet(ii, "ok_flag", "!");
          ilErr++;
          dbRollback();
          alertErr2("此筆尚有未覆核資料,不可異動");
          return;
        }
      }

      func.varsSet("cond_code", lsCondCode);
      func.varsSet("cond_data_seq", lsCondDataSeq[ii]);
      func.varsSet("trial_type", lsTrialType[ii]);
      func.varsSet("cond_data_desc", lsCondDataDesc[ii]);
      func.varsSet("data_type", lsDataType[ii]);
      func.varsSet("data_char1", lsDataChar1[ii]);
      func.varsSet("data_char2", lsDataChar2[ii]);
      func.varsSet("data_num1", lsDataNum1[ii]);
      func.varsSet("data_num2", lsDataNum2[ii]);
      func.varsSet("data_score", lsDataScore[ii]);
      func.varsSet("dsp_color", lsDspColor[ii]);

      rc = func.insertScoreDetl();
      if (rc != 1) {
        wp.colSet(ii, "ok_flag", "X");
        ilErr++;
        dbRollback();
        break;
      }
      ilOk++;
      wp.colSet(ii, "ok_flag", "V");
      continue;
    }

    if (ilErr == 0)
      sqlCommit(1);
    else	
    	wp.respMesg = "存檔失敗: 明細存檔錯誤 "+ilErr+" 筆";
  }

  boolean checkData(String lsCondCode, String lsCondDataSeq, String lsTrialType) {

    String sql1 = " select count(*) as db_apr_N from rsk_score_parmdtl where cond_code = ? "
        + " and cond_data_seq = ? and trial_type = ? and apr_flag <> 'Y' ";

    sqlSelect(sql1, new Object[] {lsCondCode, lsCondDataSeq, lsTrialType});

    if (sqlNum("db_apr_N") > 0)
      return false;

    return true;
  }

  @Override
  public void procFunc() throws Exception {
    int ilOk = 0, ilErr = 0;
    rskm02.Rskm1080Func func = new rskm02.Rskm1080Func();
    func.setConn(wp);

    wp.listCount[0] = wp.itemRows("cond_code");

    if (checkApproveZz() == false)
      return;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsCondCode = wp.itemBuff("cond_code");
    String[] lsCondCntN = wp.itemBuff("cond_cnt_N");

    int rr = -1;
    rr = optToIndex(aaOpt[0]);

    if (rr < 0) {
      alertErr2("請點選欲覆核資料");
      return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      if (commString.strToNum(lsCondCntN[rr]) == 0) {
        ilErr++;
        wp.colSet(rr, "ok_flag", "X");
        dbRollback();
        alertErr2("此筆資料沒有未覆核資料,請取消勾選");
        return;
      }

      func.varsSet("cond_code", lsCondCode[rr]);
      rc = func.dataProc();
      if (rc != 1) {
        wp.colSet(rr, "ok_flag", "X");
        ilErr++;
        dbRollback();
        break;
      }
      wp.colSet(rr, "ok_flag", "V");
      ilOk++;
    }

    if (ilErr > 0) {
      alertErr2("覆核失敗");
      return;
    }
    sqlCommit(1);
    alertMsg("覆核完成");

  }

  void copyFunc() throws Exception {
    int ilOk = 0, ilErr = 0;
    wp.listCount[0] = wp.itemRows("cond_code");

    if (wp.itemEmpty("new_trial_type")) {
      alertErr2("異動的審核類別不可空白");
      return;
    }

    if (wp.itemEmpty("copy_trial_type")) {
      alertErr2("複製的審核類別不可空白");
      return;
    }

    if (checkCopyCond() == false) {
      alertErr2("異動的審核類別:條件筆數需為空白");
      return;
    }

    rskm02.Rskm1080Func func = new rskm02.Rskm1080Func();
    func.setConn(wp);

    String lsTrialTypeCopy = wp.itemStr("copy_trial_type");
    String lsTrialTypeNew = wp.itemStr("new_trial_type");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsCondCode = wp.itemBuff("cond_code");

    int rr = -1;
    rr = optToIndex(aaOpt[0]);

    if (rr < 0) {
      alertErr2("請點選欲複製資料");
      return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) {
        continue;
      }

      func.varsSet("cond_code", lsCondCode[rr]);
      func.varsSet("trial_type", lsTrialTypeCopy);
      func.varsSet("trial_type_new", lsTrialTypeNew);

      rc = func.copyProc();
      if (rc != 1) {
        ilErr++;
        dbRollback();
        wp.colSet(rr, "ok_flag", "X");
        break;
      }
      ilOk++;
      wp.colSet(rr, "ok_flag", "V");
    }

    if (ilErr > 0) {
      alertErr2("複製失敗");
      return;
    }

    sqlCommit(1);
    alertMsg("複製成功,請重新查詢資料");

  }

  boolean checkCopyCond() {

    String sql1 = " select count(*) as db_cnt from rsk_score_parmdtl where trial_type = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("new_trial_type")});
    if (sqlNum("db_cnt") > 0)
      return false;

    return true;
  }

  @Override
  public void initButton() {
    btnModeAud("XX");
    if (eqIgno(wp.respHtml, "rskm1080_add")) {
      if (wp.autUpdate() == true) {
        this.btnAddOn(true);
      }
    }

  }

  @Override
  public void initPage() {
    wp.colSet("ind_num", "0");
  }

  @Override
  public void dddwSelect() {
    try {

      if (eqIgno(wp.respHtml, "rskm1080")) {
        wp.optionKey = wp.colStr("ex_trial_type");
        dddwList("d_dddw_trial_type", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK_TRIAL_TYPE'");
        wp.optionKey = wp.colStr("new_trial_type");
        dddwList("d_dddw_new_trial_type", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK_TRIAL_TYPE'");
      }

      if (eqIgno(wp.respHtml, "rskm1080_detl")) {
        wp.optionKey = wp.colStr("trial_type");
        dddwList("d_dddw_idtab4", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK_TRIAL_TYPE'");

        wp.optionKey = wp.colStr("kk_trial_type");
        dddwList("d_dddw_trial_type", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK_TRIAL_TYPE'");
      }

      if (eqIgno(wp.respHtml, "rskm1080_add")) {
        wp.optionKey = wp.colStr("trial_type");
        dddwList("d_dddw_idtab4", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK_TRIAL_TYPE'");
      }
    } catch (Exception ex) {
    }
  }

}
