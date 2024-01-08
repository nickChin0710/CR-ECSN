/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-25  V1.00.00  yash           program initial                        *
* 108-09-18  V1.00.01  Andy       update                                     *
* 108-12-02  V1.00.02  Amber	  Update init_button  Authority 			 *
* 109-04-24  V1.00.03  shiyuqi       updated for project coding standard     *   
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名                                                                                      * 
* 111-05-24  V1.00.05  JeffKung     補"ADD"功能                                                  *
******************************************************************************/

package bilm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Bilm0810 extends BaseAction {
  String mExBankNo = "";
  String mExMchtNo = "";

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
    } else if (eqIgno(wp.buttonCode, "A")) {
        /* 新增功能 */
        strAction = "A";
        saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
      strAction = "D";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      strAction = "UPLOAD";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    } else if (eqIgno(wp.buttonCode, "D1")) {
      /* 清畫面 */
      strAction = "D1";
      saveFunc();
    }
    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 ";

	if (empty(wp.itemStr("ex_bank_no")) == false) {
		wp.whereStr += sqlCol(wp.itemStr("ex_bank_no"), "bank_no");
	}
	if (empty(wp.itemStr("ex_merchant")) == false) {
		wp.whereStr += sqlCol(wp.itemStr("ex_merchant"), "mcht_no", "like%");
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

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno , bank_no , mcht_no , remark_40";

    wp.daoTable = "bil_no_installment";
    wp.whereOrder = " order by bank_no";
    if (empty(wp.whereStr) == true) {
    	getWhereStr();
    }

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    mExBankNo = itemkk("data_k1");

    mExMchtNo = itemkk("data_k2");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", bank_no " + ", mcht_no" + ", remark_40";
    wp.daoTable = "bil_no_installment";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  bank_no = :bank_no ";
    setString("bank_no", mExBankNo);
    wp.whereStr += " and  mcht_no = :mcht_no ";
    setString("mcht_no", mExMchtNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, 銀行代號=" + mExBankNo + " 特店代號=" + mExMchtNo);
    }
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnAddOn(wp.autUpdate());
    btnDeleteOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_bank_no");
      this.dddwList("dddw_bank_no", "bil_auto_ica", "bank_no", "ica_desc",
          "where 1=1 group by bank_no,ica_desc  order by bank_no");

    } catch (Exception ex) {
    }
  }

  int checkBankNo(String bankNo, String mchtNo) throws Exception {
    String lsSql =
        "select * from BIL_NO_INSTALLMENT where bank_no =:bank_no and mcht_no =:mcht_no ";
    setString("bank_no", bankNo);
    setString("mcht_no", mchtNo);
    sqlSelect(lsSql);
    return sqlRowNum;
  }

  int checkBankNo1(String bankNo) throws Exception {
    String lsSql =
        "select * from BIL_NO_INSTALLMENT where bank_no =:bank_no and mcht_no =:mcht_no ";
    setString("bank_no", bankNo);
    sqlSelect(lsSql);
    return sqlRowNum;
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (strAction.equals("D1")) {

      String[] aaMchtNo = wp.itemBuff("mcht_no");
      String[] aaOpt = wp.itemBuff("opt");

      wp.listCount[0] = aaMchtNo.length;

      for (int ll = 0; ll < aaMchtNo.length; ll++) {
        if (checkBoxOptOn(ll, aaOpt)) {
          wp.colSet(ll, "aa_opt", "checked");
        }
      }

      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;

      } else {
        delFunc();
      }
    } else if (strAction.equals("UPLOAD")) {
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      } else {
        procFunc();
      }
    } else {

      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }

      Bilm0810Func func = new Bilm0810Func(wp);

      rc = func.dbSave(strAction);
      log(func.getMsg());
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
      this.sqlCommit(rc);

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
    // int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
    int fi = tf.openInputText(inputFile, "MS950");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    if (fi == -1) {
      return;
    }

    Bilm0810Func func = new Bilm0810Func(wp);
    func.setConn(wp);

    String lsSql = "";
    int llOk = 0, llErr = 0, llCnt = 0, llErrFormat = 0, llDub = 0, llBankErr = 0, llMchtErr = 0;
    wp.logSql = false;
    while (true) {
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }
      int fileLength = file.split(",").length;
      llCnt++;
      // if(aa < 7){
      // err_alert("檔案格式錯誤!!");
      // return;
      // }
      if (fileLength < 3) {
        llErrFormat++;
        llErr++;
        continue;
      }

      String lsError = "";
      String[] splitLine = file.split(",");
      try {
        String lsBankNo = splitLine[0]; // 銀行代碼
        String lsMchtNo = splitLine[1]; // 特店代號
        String lsRemark40 = splitLine[2]; // 備註

        // server debug message
        // wp.alertMesg = "<script language='javascript'>
        // alert('"+ls_id+ls_amt+ls_card+ls_type+ls_date+"')</script>";

        // check bank 銀行代號長度需為 3碼以內
        // 欄位長度
        if (lsBankNo.length() > 3) {
          llBankErr++;
          llErr++;
          continue;
        }
        // check mchtno 特店代號長度需為 15碼以內
        if (lsMchtNo.length() > 15) {
          llMchtErr++;
          llErr++;
          continue;
        }
        // 重複性資料不處理
        lsSql = "select count(*)ct from bil_no_installment " + "where bank_no =:bank_no "
            + "and mcht_no =:mcht_no ";
        setString("bank_no", lsBankNo);
        setString("mcht_no", lsMchtNo);
        sqlSelect(lsSql);
        if (sqlNum("ct") > 0) {
          llDub++;
          llErr++;
          continue;
        }

        wp.itemSet("bank_no", lsBankNo);
        wp.itemSet("mcht_no", lsMchtNo);
        wp.itemSet("remark_40", lsRemark40);

        func.varsSet("bank_no", lsBankNo);
        func.varsSet("mcht_no", lsMchtNo);
        func.varsSet("remark_40", lsRemark40);
        if (func.dbInsert1() != 1) {
          llErr++;
          continue;
        } else {
          llOk++;
        }

        // 固定長度上傳檔
        // wp.item_set("id_no",commString.mid_big5(ss,0,10));
        // wp.item_set("data_flag1",commString.mid_big5(ss,10,1));
        // wp.item_set("data_flag2",commString.mid_big5(ss,11,1));

      } catch (Exception e) {
        alertMsg("匯入資料異常!!");
        return;
      }

      // ll_cnt++;
      // int rr=ll_cnt-1;
      // this.set_rowNum(rr, ll_cnt);

    }
    // wp.listCount[0]=ll_cnt; //--->開啟上傳檔檢視
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
    if (llErr > 0) {
      sqlCommit(0);
    } else {
      sqlCommit(1);
    }

    alertMsg("處理筆數=" + llCnt + ",成功=" + llOk + ";格式錯=" + llErrFormat + ",銀行碼錯=" + llBankErr
        + ",特店碼錯=" + llMchtErr + ",重複=" + llDub + ",失敗=" + llErr);
    // queryRead();

  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      System.out.println("mcht_no :" + wp.getValue("kk_mcht_no", 0) + "%");
      setString("mcht_no", wp.getValue("kk_mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

  void delFunc() throws Exception {

    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    int llOk = 0, llErr = 0;

    wp.listCount[0] = aaRowid.length;

    for (int ll = 0; ll < aaRowid.length; ll++) {

      if (checkBoxOptOn(ll, aaOpt)) {
        String lsSql = "delete from bil_no_installment where hex(rowid) = :rowid ";
        setString("rowid", aaRowid[ll]);
        sqlExec(lsSql);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          sqlCommit(0);
        } else {
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          llOk++;
        }
      }

    }
    alertMsg("刪除: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");

  }
}
