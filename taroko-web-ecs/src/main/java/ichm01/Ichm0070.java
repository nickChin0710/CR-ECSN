/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*                        V1.00.00                          program initial                            *
* 109-01-03  V1.00.01  Justin Wu    updated for archit.  change
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Ichm0070 extends BaseAction {
  String rowid = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "UPLOAD":
        // -資料處理-
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        wf_ajax_key();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr2("ex_crt_date1"), wp.itemStr2("ex_crt_date1")) == false) {
      alertErr2("建檔日期:起迄錯誤");
      return;
    }

    String lsWhere =
        " where 1=1 and from_type like '1%' " + sqlCol(wp.itemStr2("ex_card_no"), "card_no")
            + sqlCol(wp.itemStr2("ex_crt_date1"), "crt_date", ">=")
            + sqlCol(wp.itemStr2("ex_crt_date2"), "crt_date", "<=")
            + sqlCol(wp.itemStr2("ex_crt_user"), "crt_user", "like%");

    if (wp.itemEq("ex_send_flag", "1")) {
      lsWhere += " and send_date = '' ";
    } else if (wp.itemEq("ex_send_flag", "2")) {
      lsWhere += " and send_date <> '' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date ," + " crt_time ," + " card_no ," + " secu_code ,"
        + " risk_remark ," + " crt_user ," + " send_date ," + " hex(rowid) as rowid , "
        + " decode(secu_code,'R','R.拒絕代行','Q','Q.取消拒絕代行') as tt_secu_code , " + " ich_card_no ";

    wp.daoTable = " ich_refuse_log ";

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
    rowid = wp.itemStr2("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(rowid))
      rowid = wp.itemStr2("rowid");

    wp.selectSQL =
        " A.crt_date ," + " A.crt_time ," + " A.card_no ," + " A.secu_code ," + " A.risk_remark ,"
            + " A.crt_user ," + " A.send_date ," + " A.mod_user ," + " A.mod_time ,"
            + " A.mod_pgm ," + " A.mod_seqno ," + " hex(A.rowid) as rowid ," + " B.ich_card_no ,"
            + " B.new_end_date ," + " B.current_code , " + " 'N' db_tsc57 , " + " 'N' db_tsc04 ";
    wp.daoTable = " ich_refuse_log A join ich_card B on A.card_no = B.card_no ";
    wp.whereStr = " where 1=1 " + sqlRowId(rowid, "A.rowid");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return;
    }

    wp.colSet("ajax_code", "N");

  }

  @Override
  public void saveFunc() throws Exception {

    ichm01.Ichm0070Func func = new ichm01.Ichm0070Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);

    if (rc <= 0) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

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
    int llErr = 0;
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");

    ichm01.Ichm0070Func func = new ichm01.Ichm0070Func();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

    int llOk = 0, llcnt = 0;
    while (true) {
      String tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (tmpStr.length() < 2) {
        continue;
      }
      // String merchantNo = readData.substring(0,15);
      // wp.ddd(ss);
      llcnt++;
      String[] tt = new String[2];
      tt[0] = tmpStr;
      tt = commString.token(tt, ",");
      String lsIchCardNo = tt[1];
      tt = commString.token(tt, ",");
      String lsSecuCode = tt[1];
      String lsCardNo = selectCardNo(lsIchCardNo);
      if (empty(lsCardNo)) {
        llErr++;
        tf.writeTextFile(fileErr, tmpStr + ";" + "查無卡號" + wp.newLine);
        continue;
      }


      wp.itemSet("ich_card_no", lsIchCardNo);
      wp.itemSet("secu_code", lsSecuCode);
      wp.itemSet("card_no", lsCardNo);
      wp.itemSet("refuse_type", lsSecuCode);

      if (func.insertData() == 1) {
        llOk++;
      } else {
        llErr++;
        tf.writeTextFile(fileErr, tmpStr + ";" + func.getMsg() + wp.newLine);
      }
    }

    if (llOk > 0) {
      sqlCommit(1);
    } else {
      sqlCommit(-1);
    }

    tf.closeOutputText(fileErr);
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    if (llErr > 0) {
      wp.setDownload(inputFile + ".err");
    }

    alertMsg("資料匯入處理筆數: " + llcnt + ", 成功筆數=" + llOk);
    wp.colSet("zz_file_name", "");
    return;
  }

  String selectCardNo(String aIchCardNo) {

    String sql1 = " select " + " card_no " + " from ich_card " + " where ich_card_no = ? ";

    sqlSelect(sql1, new Object[] {aIchCardNo});

    if (sqlRowNum > 0) {
      return sqlStr("card_no");
    }

    return "";
  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ichm0070_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  public void wf_ajax_key() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    selectData(wp.itemStr("ax_card_no"));
    if (rc != 1) {
      wp.addJSON("card_no", "");
      wp.addJSON("current_code", "");
      wp.addJSON("new_end_date", "");
      return;
    }
    wp.addJSON("ich_card_no", sqlStr("ich_card_no"));
    wp.addJSON("current_code", sqlStr("current_code"));
    wp.addJSON("new_end_date", sqlStr("new_end_date"));


  }

  void selectData(String cardNo) {
    String sql1 = " select " + " ich_card_no , " + " current_code , " + " new_end_date "
        + " from ich_card " + " where card_no = ? " + " and new_end_date in "
        + " (select max(new_end_date) from ich_card where card_no = ? ) ";

    sqlSelect(sql1, new Object[] {cardNo, cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("卡號不存在:" + cardNo);
      return;
    }
  }

}
