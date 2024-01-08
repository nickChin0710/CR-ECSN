/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-16  V1.00.01  ryan       program initial                            *
* 109-04-22  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                                                                        *
******************************************************************************/
package rdsm01;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Rdsm0050 extends BaseAction {
  String projNo = "";

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      dataReadCard();
    } else if (eqIgno(wp.buttonCode, "R3")) {
      // -資料讀取-
      strAction = "R";
      dataReadMcht();
    } else if (eqIgno(wp.buttonCode, "R4")) {
      // -資料讀取-
      strAction = "R";
      dataReadMchtgp();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      /* 更新功能 */
      procCardData();
    } else if (eqIgno(wp.buttonCode, "U3")) {
      /* 更新功能 */
      // procMchtData();
    } else if (eqIgno(wp.buttonCode, "U4")) {
      /* 更新功能 */
      procMchtgpData();
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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rdsm0050_card")) {
        wp.optionKey = wp.colStr(0, "wk_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "", "where 1=1 ");
      }

      if (eqIgno(wp.respHtml, "rdsm0050_card")) {
        wp.optionKey = wp.colStr(0, "wk_card_type");
        dddwList("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1");
      }

      if (eqIgno(wp.respHtml, "rdsm0050_card")) {
        wp.optionKey = wp.colStr(0, "wk_group_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1");
      }

      if ((wp.respHtml.equals("rdsm0050_mcht_gp"))) {
        wp.optionKey = wp.colStr(0, "wk_mcht_no_gp");
        dddwList("dddw_data_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where 1 = 1 ");
      }

    } catch (Exception ex) {
    }

  }

  int getWhereStr() {

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_proj_no")) == false) {
      wp.whereStr += sqlCol(wp.itemStr("ex_proj_no"), "proj_no", "like%");
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    wp.whereStr = wp.whereStr;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " proj_no " + " ,proj_desc " + " ,valid_end_date " + " ,amt_sum_flag "
        + " ,decode(amt_sum_flag,'1','1.帳戶',decode(amt_sum_flag,'2','2.卡片',amt_sum_flag)) as tt_amt_sum_flag ";
    wp.daoTable = " cms_roadparm2 ";
    wp.whereOrder = " order by proj_no Asc ";
    getWhereStr();
    pageQuery();

    if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    projNo = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(projNo))
      projNo = itemkk("proj_no");

    wp.selectSQL = " A.* , " + " hex(A.rowid) as rowid ";
    wp.daoTable = " cms_roadparm2 A ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += sqlCol(projNo, "A.proj_no");
    pageSelect();

    if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("fst_mcht_gp", (empty(wp.colStr("fst_mcht_gp"))) ? "0" : wp.colStr("fst_mcht_gp"));
    wp.colSet("lst_mcht_gp", (empty(wp.colStr("lst_mcht_gp"))) ? "0" : wp.colStr("lst_mcht_gp"));
    wp.colSet("cur_mcht_gp", (empty(wp.colStr("cur_mcht_gp"))) ? "0" : wp.colStr("cur_mcht_gp"));
  }

  public void dataReadCard() throws Exception {
    wp.pageRows = 99999;
    String lsKey = "";
    lsKey = wp.itemStr("data_k1");
    if (empty(lsKey))
      lsKey = wp.itemStr("proj_no");
    wp.selectSQL = " hex(rowid) as rowid" + " ,proj_no" + " ,acct_type " + " ,card_type "
        + " ,group_code " + " ,corp_no " + " ,mod_time " + " ,mod_pgm ";
    wp.daoTable = " cms_roadparm2_dtl ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "proj_no");
    pageQuery();
    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    wp.colSet("ind_num", sqlRowNum);
    wp.setListCount(1);
  }

  public void dataReadMcht() throws Exception {
    wp.pageRows = 99999;
    String lsKey1 = "", lsKey2 = "";
    lsKey1 = wp.itemStr("data_k1");
    if (empty(lsKey1)) {
      lsKey1 = wp.itemStr("proj_no");
    }
    lsKey2 = wp.itemStr("data_k2");
    if (empty(lsKey2)) {
      lsKey2 = wp.itemStr("data_type");
    }
    wp.colSet("data_type", lsKey2);
    wp.selectSQL = " hex(rowid) as rowid " + " ,data_code ";
    wp.daoTable = " cms_roadparm2_bn_data ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and apr_flag = 'Y' " + " and type_desc = '特店代號' ";
    wp.whereStr += sqlCol(lsKey1, "proj_no");
    wp.whereStr += sqlCol(lsKey2, "data_type");
    pageQuery();

    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    wp.colSet("ind_num", sqlRowNum);
    wp.setListCount(1);
  }

  public void dataReadMchtgp() throws Exception {
    // wp.pageRows = 99999;
    String lsKey1 = "", lsKey2 = "";
    lsKey1 = wp.itemStr("data_k1");
    if (empty(lsKey1)) {
      lsKey1 = wp.itemStr("proj_no");
    }
    lsKey2 = wp.itemStr("data_k2");
    if (empty(lsKey2)) {
      lsKey2 = wp.itemStr("data_type");
    }
    wp.colSet("data_type", lsKey2);
    wp.selectSQL = " hex(rowid) as rowid " + " ,data_code "
        + " ,(select mcht_group_id||'_'||mcht_group_desc from mkt_mcht_gp where mcht_group_id = cms_roadparm2_bn_data.data_code) as tt_data_code ";
    wp.daoTable = " cms_roadparm2_bn_data ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and apr_flag = 'Y' " + " and type_desc = '特店群組' ";
    wp.whereStr += sqlCol(lsKey1, "proj_no");
    wp.whereStr += sqlCol(lsKey2, "data_type");
    pageQuery();

    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    wp.colSet("ind_num", sqlRowNum);
    wp.setListCount(1);
  }

  @Override
  public void saveFunc() throws Exception {
    Rdsm0050Func func = new Rdsm0050Func();
    func.setConn(wp);

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      this.saveAfter(false);
    }
  }

  public void procCardData() throws Exception {
    Rdsm0050Func func = new Rdsm0050Func();
    func.setConn(wp);
    int llOk = 0, llErr = 0;
    String lsProjNo = wp.itemStr("proj_no");
    String[] lsAcctType = wp.itemBuff("acct_type");
    String[] lsCardType = wp.itemBuff("card_type");
    String[] lsGroupCode = wp.itemBuff("group_code");
    String[] lsCorpNo = wp.itemBuff("corp_no");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsAcctType.length;

    func.varsSet("proj_no", lsProjNo);

    if (func.deleteCardData() != 1) {
      errmsg(func.getMsg());
      return;
    }

    for (int ii = 0; ii < lsAcctType.length; ii++) {
      if (checkBoxOptOn(ii, aaOpt)) {
        llOk++;
        continue;
      }

      func.varsSet("acct_type", lsAcctType[ii]);
      func.varsSet("card_type", lsCardType[ii]);
      func.varsSet("group_code", lsGroupCode[ii]);
      func.varsSet("corp_no", lsCorpNo[ii]);

      if (func.insertCardData() == 1) {
        llOk++;
        continue;
      } else {
        llErr++;
        this.dbRollback();
        errmsg(func.getMsg());
        break;
      }
    }

    if (llOk > 0) {
      sqlCommit(1);
      alertMsg("處理完成 : 成功:" + llOk + " 失敗:" + llErr);
      dataReadCard();
    }
  }

  public void procMchtData() throws Exception {
    Rdsm0050Func func = new Rdsm0050Func();
    func.setConn(wp);
    int llOk = 0, llErr = 0;
    String lsProjNo = wp.itemStr("proj_no");
    String lsDataType = wp.itemStr("data_type");
    String[] lsDataCode = wp.itemBuff("data_code");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsDataCode.length;
    func.varsSet("proj_no", lsProjNo);
    func.varsSet("data_type", lsDataType);
    if (func.deleteMchtData() != 1) {
      errmsg(func.getMsg());
      return;
    }
    for (int ii = 0; ii < lsDataCode.length; ii++) {
      if (checkBoxOptOn(ii, aaOpt)) {
        llOk++;
        continue;
      }
      func.varsSet("data_code", lsDataCode[ii]);
      if (func.insertMchtData() == 1) {
        llOk++;
        continue;
      } else {
        llErr++;
        this.dbRollback();
        errmsg(func.getMsg());
        break;
      }
    }
    if (llOk > 0) {
      sqlCommit(1);
      alertMsg("處理完成 : 成功:" + llOk + " 失敗:" + llErr);
      dataReadMcht();
    }
  }

  public void procMchtgpData() throws Exception {
    Rdsm0050Func func = new Rdsm0050Func();
    func.setConn(wp);
    int llOk = 0, llErr = 0;
    String lsProjNo = wp.itemStr("proj_no");
    String lsDataType = wp.itemStr("data_type");
    String[] lsDataCode = wp.itemBuff("data_code");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsDataCode.length;

    func.varsSet("proj_no", lsProjNo);
    func.varsSet("data_type", lsDataType);
    if (func.deleteMchtgpData() != 1) {
      errmsg(func.getMsg());
      return;
    }

    for (int ii = 0; ii < lsDataCode.length; ii++) {
      if (checkBoxOptOn(ii, aaOpt)) {
        llOk++;
        continue;
      }

      func.varsSet("data_code", lsDataCode[ii]);
      if (func.insertMchtgpData() == 1) {
        llOk++;
        continue;
      } else {
        llErr++;
        this.dbRollback();
        errmsg(func.getMsg());
        break;
      }
    }

    if (llOk > 0) {
      sqlCommit(1);
      alertMsg("處理完成 : 成功:" + llOk + " 失敗:" + llErr);
      dataReadMchtgp();
    }
  }

  @Override
  public void procFunc() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();

  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }
    Rdsm0050Func func = new Rdsm0050Func();
    func.setConn(wp);

    String lsProjNo = wp.itemStr("proj_no");
    String lsDataType = wp.itemStr("data_k1");

    func.varsSet("proj_no", lsProjNo);
    func.varsSet("data_type", lsDataType);
    int llOk = 0, llCnt = 0;
    if (func.deleteMchtData() == 1) {
      while (true) {
        String tmpStr = tf.readTextFile(fi);
        if (tf.endFile[fi].equals("Y")) {
          break;
        }
        if (empty(tmpStr)) {
          continue;
        }
        tmpStr = strMid(tmpStr, 0, 16);
        llCnt++;
        func.varsSet("data_code", tmpStr);
        if (func.insertMchtData() == 1) {
          llOk++;
          this.sqlCommit(1);
        } else {
          this.sqlCommit(-1);
        }
      }
      tf.closeInputText(fi);
      tf.deleteFile(inputFile);
      alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk);
    } else {
      dbRollback();
      errmsg(func.getMsg());
      tf.closeInputText(fi);
      tf.deleteFile(inputFile);
    }
    return;
  }

  @Override
  public void initButton() {
    if (posAny(wp.respHtml, "_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
    wp.colSet("fst_mm", "0");
    wp.colSet("fst_one_low_amt", "0");
    wp.colSet("fst_purch_amt", "0");
    wp.colSet("fst_purch_row", "0");

    wp.colSet("lst_tol_amt", "0");
    wp.colSet("lst_mm", "0");
    wp.colSet("lst_one_low_amt", "0");
    wp.colSet("lst_purch_amt", "0");
    wp.colSet("lst_purch_row", "0");

    wp.colSet("cur_mm", "0");
    wp.colSet("cur_one_low_amt", "0");
    wp.colSet("cur_purch_amt", "0");
    wp.colSet("cur_purch_row", "0");
  }

}

