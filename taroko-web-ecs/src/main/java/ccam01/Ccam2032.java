package ccam01;
/**
 * 2019-1230   JH    acno_Block: busi.func >>ecsfunc
 * 2019-1210  V1.00.01  add initButton
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名 
 * 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  
 */

import busi.func.AcnoBlockReason;
import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;

public class Ccam2032 extends BaseAction {
 // String kk1 = "";
  String isFileName = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
    if (empty(wp.itemStr("ex_crt_date1"))) {
      errmsg("匯入日期起 : 不可空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("匯入日期起迄：輸入錯誤");
      return;
    }
    String lsWhere =
        " where 1=1 and from_type ='2' " + sqlCol(wp.itemStr("ex_crt_date1"), "chg_date", ">=")
            + sqlCol(wp.itemStr("ex_crt_date2"), "chg_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " crt_date , " + " crt_time , " + " card_no , " + " bin_type , "
        + " spec_status , " + " spec_del_date , " + " spec_outgo_reason , " + " spec_neg_reason , "
        + " spec_dept_no , " + " vm_resp_code , " + " neg_resp_code ," + " chg_date , "
        + " chg_time ";
    wp.daoTable = "cca_special_visa ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by chg_date desc , chg_time desc ";
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
    // if (file_upLoad()!=1)
    // return;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();
  }

  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
      wp.colSet("upload_flag", "|| 1==1");
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
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

    Ccam2032Func func = new Ccam2032Func();
    func.setConn(wp);
    AcnoBlockReason funcBlock = new AcnoBlockReason();
    funcBlock.setConn(wp);
    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));
    wp.itemSet("data_from", "2");


    int llOk = 0, llCnt = 0;
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
      llCnt++;
      String[] tt = new String[2];
      tt[0] = file;
      tt = commString.token(tt, ",");
      String lsCardNo = tt[1];
      tt = commString.token(tt, ",");
      String lsSpecStatus = tt[1];
      tt = commString.token(tt, ",");
      String lsSpecDeptNo = tt[1];
      tt = commString.token(tt, ",");
      String lsSpecDelDate = tt[1];

      if (empty(lsSpecDelDate)) {
        lsSpecDelDate = selectEndDate(lsCardNo);
        if (empty(lsSpecDelDate))
          continue;
        lsSpecDelDate = commDate.dateAdd(lsSpecDelDate, 0, 0, 1);
      }

      if (checkCard(lsCardNo) == false)
        continue;

      wp.itemSet("card_no", lsCardNo);
      wp.itemSet("spec_status", lsSpecStatus);
      wp.itemSet("spec_dept_no", lsSpecDeptNo);
      wp.itemSet("spec_del_date", lsSpecDelDate);
      if(func.procOutGoing() !=1)	{
    	  sqlCommit(-1);
    	  continue;
      }
      func.updateCardBase();
      if (checkInsert(lsCardNo)) {
        if (func.updateData() == 1) {

          this.sqlCommit(1);
        } else {
          this.sqlCommit(-1);
          wp.log(file + ", error=" + func.getMsg());
          continue;
        }
      } else {
        if (func.insertData() == 1) {
          this.sqlCommit(1);
        } else {
          this.sqlCommit(-1);
          wp.log(file + ", error=" + func.getMsg());
          continue;
        }
      }

      if (funcBlock.ccaM2030Spec(lsCardNo, "A") == 1) {
        llOk++;
        this.sqlCommit(1);
      } else {
        this.sqlCommit(-1);
        wp.log(file + ", error=" + funcBlock.getMsg());
        continue;
      }


    }

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk);

    return;
  }

  boolean checkInsert(String kkIdNo) throws Exception {

    String sql1 = "select card_no " + " from cca_special_visa" + " where card_no =:card_no";
    setString("card_no", kkIdNo);
    sqlSelect(sql1);

    if (this.sqlRowNum <= 0) {
      log("data-no-find=" + kkIdNo);
      return false;
    }
    return true;
  }

  boolean checkCard(String kkCardNo) throws Exception {
    String sql1 = " select " + " current_code , " + " new_end_date   " + " from crd_card "
        + " where card_no = ? " + " union all " + " select " + " current_code , "
        + " new_end_date   " + " from dbc_card " + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {kkCardNo, kkCardNo});

    if (sqlRowNum <= 0)
      return false;

    if (!eqIgno(sqlStr("current_code"), "0"))
      return false;
    if (chkStrend(getSysDate(), sqlStr("new_end_date")) == false)
      return false;

    return true;
  }

  String selectEndDate(String kkCardNo) throws Exception {
    String sql1 =
        " select " + " new_end_date " + " from crd_card " + " where 1=1 " + " and card_no = ? ";

    sqlSelect(sql1, new Object[] {kkCardNo});

    if (sqlRowNum > 0)
      return sqlStr("new_end_date");

    String sql2 =
        " select " + " new_end_date " + " from dbc_card " + " where 1=1 " + " and card_no = ? ";

    sqlSelect(sql2, new Object[] {kkCardNo});

    if (sqlRowNum > 0)
      return sqlStr("new_end_date");

    return "";
  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
