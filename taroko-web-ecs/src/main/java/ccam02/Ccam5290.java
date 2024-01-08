/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-12  V1.00.01  Alex        add initButton                            *
*109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
 * 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package ccam02;


import ofcapp.BaseAction;
import taroko.base.CommString;
import taroko.com.TarokoFileAccess;

public class Ccam5290 extends BaseAction {
  String acqId = "", mchtNo = "";

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
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    String lsWhere =
        " where 1=1" + sqlCol(wp.itemStr("ex_date1"), "to_char(mod_time,'yyyymmdd')", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "to_char(mod_time,'yyyymmdd')", "<=")
            + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
            + sqlCol(wp.itemStr("ex_acq_id"), "acq_id");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " apr_flag ," + " acq_id , " + " mcht_no , " + " mcht_name , " + " online_date , "
            + " stop_date ," + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "cca_mcht_notonline";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();


    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    acqId = wp.itemStr("data_k1");
    mchtNo = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acqId)) {
      acqId = itemkk("acq_id");
    }
    if (empty(mchtNo)) {
      mchtNo = itemkk("mcht_no");
    }

    wp.selectSQL = "apr_flag ," + " acq_id ," + " mcht_no , " + " mcht_name ," + " online_date , "
        + " stop_date ," + " crt_user , " + " crt_date , " + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " hex(rowid) as rowid , mod_seqno";
    wp.daoTable = "cca_mcht_notonline";
    wp.whereStr = "where 1=1" + sqlCol(acqId, "acq_id") + sqlCol(mchtNo, "mcht_no");
    wp.whereOrder = " order by acq_id, mcht_no, apr_flag fetch first 1 row only ";
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acqId);
    }

  }

  @Override
  public void saveFunc() throws Exception {

    if (isDelete() && wp.itemEq("apr_flag", "Y") && checkApproveZz() == false) {
      return;
    }
    ccam02.Ccam5290Func func = new ccam02.Ccam5290Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);

    if (rc == 1 && isAdd())
      alertMsg("此筆資料待覆核");
    if (rc == 1 && isUpdate())
      alertMsg("修改完成，此筆資料待覆核");

  }

  @Override
  public void procFunc() throws Exception {
    // --05/27取消匯入檢核
    // if(check_approve_zz()==false) return;

    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }


    fileDataImp();

  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    ccam02.Ccam5290Func func = new ccam02.Ccam5290Func();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));
    wp.itemSet("data_from", "2");

    int isOk = 0, count = 0;
    int isError = 0;
    while (true) {
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }
      // String merchantNo = readData.substring(0,15);
      // wp.ddd(ss);
      String[] ss = new String[2];
      count++;
      ss[0] = file;
      ss = commString.token(ss, ",");
      String acqId = ss[1];
      ss = commString.token(ss, ",");
      String mchtNo = ss[1];
      ss = commString.token(ss, ",");
      String mchtName = ss[1];
      ss = commString.token(ss, ",");
      String lsOnlineDate = ss[1];
      ss = commString.token(ss, ",");
      String lsStopDate = ss[1];

      wp.itemSet("acq_id", acqId);
      wp.itemSet("mcht_no", mchtNo);
      wp.itemSet("mcht_name", mchtName);
      wp.itemSet("online_date", lsOnlineDate);
      wp.itemSet("stop_date", lsStopDate);

      if (func.deleteNotOnline2() != 1) {
        isError++;
        continue;
      }

      if (func.insertNotOnline2() == 1) {
        isOk++;
        this.sqlCommit(1);
        continue;
      }

    }
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
    alertMsg("資料匯入處理筆數: " + count + ", 成功筆數=" + isOk);
    return;
  }

  @Override
  public void initButton() {

    if (eqIgno(wp.respHtml, "ccam5290")) {
      btnModeAud("XX");
    }

    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
