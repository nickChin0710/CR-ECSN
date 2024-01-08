package secm01;
/*子系統使用者層級權限匯入
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Secp2061 extends BaseAction {
  String isPgmDesc = "";

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
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      procFunc1();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secp2061")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "secp2061")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

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
    if (wp.itemEmpty("ex_group_id") || wp.itemEmpty("ex_user_level")) {
      alertErr2("子系統代碼 , 使用者層級 ");
      return;
    }

    int llCnt = 0, llOk = 0, llErr = 0;
    Secp2061Func func = new Secp2061Func();
    func.setConn(wp);
    String lsGroupId = "";
    String lsUserLevel = "";
    String[] lsInputData = wp.itemBuff("input_data");
    String[] lsAutQuery = wp.itemBuff("aut_query");
    String[] lsWfWinid = wp.itemBuff("wf_winid");
    String[] lsWfUpdate = wp.itemBuff("wf_update");
    String[] lsWfApprove = wp.itemBuff("wf_approve");
    String[] lsWfPrint = wp.itemBuff("wf_print");

    wp.listCount[0] = wp.itemRows("input_data");

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    lsGroupId = wp.itemStr("ex_group_id");
    lsUserLevel = wp.itemStr("ex_user_level");

    func.varsSet("group_id", lsGroupId);
    func.varsSet("user_level", lsUserLevel);

    if (func.delAuthority() != 1) {
      dbRollback();
      alertErr2("處理錯誤 ! Err:" + func.getMsg());
      return;
    }

    for (int rr = 0; rr < wp.itemRows("input_data"); rr++) {
      if (eqIgno(lsInputData[rr], "N"))
        continue;
      llCnt++;

      func.varsSet("aut_query", lsAutQuery[rr]);
      func.varsSet("wf_winid", lsWfWinid[rr]);
      func.varsSet("aut_update", lsWfUpdate[rr]);
      func.varsSet("aut_approve", lsWfApprove[rr]);
      func.varsSet("aut_print", lsWfPrint[rr]);

      if (func.insertAuthority() == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        llOk++;
        wp.colSet(rr, "ok_flag", "X");
        dbRollback();
        continue;
      }
    }

    alertMsg("處理筆數 :" + llCnt + "  成功:" + llOk + "  失敗:" + llErr);

  }

  public void procFunc1() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();
  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
    // String inputFile = wp.dataRoot + "/upload/" + wp.item_ss("file_name");
    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    int llCnt = 0, rr = -1;
    String[] tt = new String[2];
    while (true) {
      String tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (tmpStr.length() < 2) {
        continue;
      }
      rr++;
      llCnt++;
      tt[0] = tmpStr;
      // --匯入 程式代碼 作業名稱
      tt = commString.token(tt, ",");
      if (checkPgm(tt[1])) {
        wp.colSet(rr, "input_data", "Y");
        wp.colSet(rr, "wf_name", isPgmDesc);
      } else {
        wp.colSet(rr, "input_data", "N");
      }
      wp.colSet(rr, "wf_winid", tt[1]);
      // --權限
      tt = commString.token(tt, ",");
      wp.colSet(rr, "aut_query", tt[1]);
      // --存檔
      tt = commString.token(tt, ",");
      wp.colSet(rr, "wf_update", tt[1]);
      // --覆核
      tt = commString.token(tt, ",");
      wp.colSet(rr, "wf_approve", tt[1]);
      // --列印
      tt = commString.token(tt, ",");
      wp.colSet(rr, "wf_print", tt[1]);
      this.setRowNum(rr, llCnt);
    }
    wp.listCount[0] = llCnt;
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    return;
  }

  boolean checkPgm(String lsPgm) {
    isPgmDesc = "";

    String sql1 = " select " + " wf_name " + " from sec_window " + " where wf_winid = ? ";
    sqlSelect(sql1, new Object[] {lsPgm});

    if (sqlRowNum <= 0)
      return false;

    isPgmDesc = sqlStr("wf_name");

    return true;
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
