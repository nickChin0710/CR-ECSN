/**
 * 2023-0705    JH    ++fileDataImp()
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package cmsm03;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Cmsm4240 extends BaseAction {
  String mccGroup = "";

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      /* 新增明細 */
      insertDetl();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D2")) {
      /* 刪除明細 */
      deleteDetl();
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
    } else if (eqIgno(wp.buttonCode, "C2")) {
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
    String lsWhere = "";
    lsWhere = " where A.wf_type='CMS-MCC-GROUP' " + sqlCol(wp.itemStr2("ex_mcc_group"), "A.wf_id");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.wf_id as mcc_group , " + " A.wf_desc as group_desc , "
        + " (select count(*) from cms_mcc_group where mcc_group=A.wf_id) as mcc_cnt ";

    wp.daoTable = " ptr_sys_idtab A ";
    wp.whereOrder = " order by 1 ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mccGroup = wp.itemStr2("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mccGroup))
      mccGroup = itemkk("mcc_group");
    if (empty(mccGroup)) {
      alertErr2("MCC類別 不可空白");
      return;
    }

    // --先讀中文
    wp.selectSQL = " wf_id as mcc_group , wf_desc as group_desc , hex(rowid) as rowid , mod_seqno ";
    wp.daoTable = " ptr_sys_idtab ";
    wp.whereStr = " where wf_type='CMS-MCC-GROUP' " + sqlCol(mccGroup, "wf_id");
    pageSelect();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    String lsMccCode ="";
    if (eqIgno(strAction,"R2")) {
      lsMccCode =wp.itemStr("ex_mcc_code");
    }
    // --讀取明細
    wp.selectSQL = " mcc_code ";
    wp.daoTable = " cms_mcc_group ";
    wp.whereStr = " where 1=1 " + sqlCol(mccGroup, "mcc_group")+
            sqlCol(lsMccCode,"mcc_code",">=")+
            " order by mcc_code"
//    commSqlStr.rownum(100)
    ;

    wp.pageRows =100;
    pageQuery();
    if (sqlNotFind()) {
      selectOK();
      return;
    }
    wp.setListCount(0);
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm03.Cmsm4240Func func = new cmsm03.Cmsm4240Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("mcc_code");

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(true);

    if (isDelete())
      wp.listCount[0] = 0;

  }

  void insertDetl() throws Exception {
    cmsm03.Cmsm4240Func func = new cmsm03.Cmsm4240Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("mcc_code");

    rc = func.insertDetl();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      wp.colSet("ex_mcc_code", "");
      alertMsg("新增明細成功  , 請重新讀取 ");
    }
  }

  void deleteDetl() throws Exception {
    int ilOk = 0, ilErr = 0;
    cmsm03.Cmsm4240Func func = new cmsm03.Cmsm4240Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("mcc_code");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsMccCode = wp.itemBuff("mcc_code");

    for (int ii = 0; ii < wp.itemRows("mcc_code"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;
      func.varsSet("mcc_code", lsMccCode[ii]);

      rc = func.deleteDetl();
      if (rc != 1) {
        ilErr++;
        wp.colSet(ii, "ok_flag", "X");
        dbRollback();
        continue;
      } else {
        ilOk++;
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
        continue;
      }
    }

    alertMsg("刪除明細完成 , 成功:" + ilOk + "  失敗:" + ilErr);

  }

  @Override
  public void procFunc() throws Exception {
    if (wp.itemEmpty("zz_file_name")) {
      alertErr("上傳檔名: 不可空白");
      return;
    }
    if (wp.itemEmpty("mcc_group")) {
      alertErr("MCC類別: 不可空白");
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

  cmsm03.Cmsm4240Func func = new cmsm03.Cmsm4240Func();
  func.setConn(wp);
  wp.listCount[0] = wp.itemRows("mcc_code");

  rc = func.deleteAllDetl();
  if (rc != 1) {
    alertErr(func.getMsg());
    return;
  }

  int llOk = 0, llCnt = 0, llErr = 0;
  while (true) {
    String ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) {
      break;
    }
    if (ss.length() < 4) {
      continue;
    }
    llCnt++;

//    if (checkMccCode(ss) == false) {
//      llErr++;
//      continue;
//    }

    wp.itemSet("ex_mcc_code", ss);
    rc = func.insertDetl();

    if (rc != 1) {
      llErr++;
      dbRollback();
      continue;
    }
    else {
      llOk++;
      dbCommit();
    }

  }

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);

  alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + " , 失敗筆數:" + llErr);

  return;
}
//-------
boolean checkMccCode(String _mccCode) throws Exception  {

  String sql1 = " select count(*) as db_cnt from cca_mcc_risk where mcc_code = ? ";

  sqlSelect(sql1, _mccCode); //new Object[]{_mccCode});
  return sqlNum("db_cnt") > 0;
}

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "cmsm4240_detl")) {
      btnModeAud();
//      if (!autUpdate) {
//        buttonOff("detl_update_off");
//      }
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
