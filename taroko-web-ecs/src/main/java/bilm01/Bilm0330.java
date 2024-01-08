/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-13  V1.00.00  Andy Liu      program initial                         *
* 107-05-24  V1.00.01  Andy Liu       Update  UI,pg flow                     *
* 109-04-24  V1.00.02  shiyuqi       updated for project coding standard     *   
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package bilm01;

import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import ofcapp.BaseAction;

public class Bilm0330 extends BaseAction {
  String mExMchtNo = "";
  String mExMchtNoBuf = "";
  String mExMchtNoCpy = "";
  String mExSeqNo = "";
  String mExMchtType = "";

  Bilm0330Func func;

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
      // dataRead1();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "A1")) {
      /* 新增功能 */
      insertFunc1();
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
      /* 存檔 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "S3")) {
      /* 存檔 */
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      String atype = wp.itemStr("ajax_type");
      strAction = "AJAX";
      if (empty(atype)) {
        processAjaxOption();
      }
      if (atype.equals("kk_merchant")) {
        processAjaxOption1(atype);
      } else if (atype.equals("cc_merchant")) {
        processAjaxOption2(atype);
      }
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_merchant")) == false) {
      wp.whereStr += " and mcht_no = :mcht_no ";
      setString("mcht_no", wp.itemStr("ex_merchant"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "mcht_no" + " , seq_no" + " , confirm_flag" + " , confirm_date" + " , copy_flag"
        + " , uf_2ymd(mod_time) as mod_date" + " , mod_user ";
    wp.daoTable = "bil_prod_copy_mas";
    wp.whereOrder = " order by mcht_no";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.pageRows = 999;
    // Destination特店
    mExMchtNo = itemkk("data_k1");
    if (empty(mExMchtNo)) {
      mExMchtNo = wp.itemStr("mcht_no1");
    }
    mExSeqNo = itemkk("data_k2");
    if (empty(mExSeqNo)) {
      mExSeqNo = wp.itemStr("seq_no");
    }
    wp.pageControl();
    wp.selectSQL =
        "hex(a.rowid) as rowid " + " , a.seq_no" + " , a.mcht_no_cpy " + " , b.mcht_chi_name ";
    wp.daoTable =
        "bil_prod_copy_dtl as a left join bil_merchant as b on a.mcht_no_cpy = b.mcht_no ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  a.mcht_no = :mcht_no ";
    wp.whereStr += " and  a.seq_no = :seq_no ";
    setString("mcht_no", mExMchtNo);
    setString("seq_no", mExSeqNo);
    wp.whereOrder = " order by a.mcht_no_cpy ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr("mcht_no_cpy資料檔查無資料!!");
      // alert_err(AppMsg.err_condNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // Source 特店
    wp.sqlCmd = "select hex(a.rowid) as rowid, a.mod_seqno " + " , a.mcht_no" + " , b.mcht_chi_name"
        + " , b.mcht_type" + " , a.seq_no" + " , a.copy_flag" + " , a.confirm_flag"
        + " , a.confirm_date" + " , uf_2ymd(a.mod_time) as mod_date" + " , a.mod_user "
        + "from bil_prod_copy_mas a left JOIN bil_merchant b on a.mcht_no = b.mcht_no "
        + "where 1=1 " + "and  a.mcht_no = :mcht_no " + "and  a.seq_no = :seq_no ";
    setString("mcht_no", mExMchtNo);
    setString("seq_no", mExSeqNo);
    sqlSelect(wp.sqlCmd);
    wp.colSet("mcht_no1", sqlStr("mcht_no"));
    wp.colSet("mcht_chi_name1", sqlStr("mcht_chi_name"));
    wp.colSet("mcht_type", sqlStr("mcht_type"));
    wp.colSet("seq_no1", sqlStr("seq_no"));
    wp.colSet("copy_flag", sqlStr("copy_flag"));
    wp.colSet("confirm_flag", sqlStr("confirm_flag"));
    wp.colSet("confirm_date", sqlStr("confirm_date"));
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Bilm0330Func(wp);
    mExMchtNo = wp.colStr("mcht_no1");
    mExSeqNo = wp.colStr("seq_no");
    String isSql = "", dsSql = "";
    int llOk = 0, llErr = 0, rc = 0;
    String[] aaMchtNoCpy = wp.itemBuff("mcht_no_cpy");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaMchtNoCpy.length;
    if (wp.itemStr("confirm_flag").equals("Y")) {
      alertErr("已放行資料不可修改!!");
      return;
    }
    // Delete bil_prod_copy_dtl;
    dsSql = "delete bil_prod_copy_dtl where mcht_no =:mcht_no and seq_no =:seq_no ";
    setString("mcht_no", mExMchtNo);
    setString("seq_no", mExSeqNo);
    sqlExec(dsSql);
    if (sqlRowNum < 0) {
      alertErr("Delete bil_prod_copy_dtl error!!");
      sqlCommit(0);
      return;
    }
    // insert bil_prod_copy_dtl
    for (int ll = 0; ll < aaMchtNoCpy.length; ll++) {
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }
      if (empty(aaMchtNoCpy[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "目的特店資料空白 !!");
        llErr++;
        continue;
      }
      if (inputDataCheck(ll) != 1) {
        llErr++;
        continue;
      }
      // -check duplication-
      if (ll != Arrays.asList(aaMchtNoCpy).indexOf(aaMchtNoCpy[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "特店資料值重複 !!");
        llErr++;
        continue;
      }
      isSql = "insert into bil_prod_copy_dtl " + "(mcht_no, seq_no, mcht_no_cpy) "
          + "values (:mcht_no, :seq_no, :mcht_no_cpy )";
      setString("mcht_no", mExMchtNo);
      setString("seq_no", mExSeqNo);
      setString("mcht_no_cpy", aaMchtNoCpy[ll]);
      sqlExec(isSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "Insert bil_prod_copy_dtl error ");
        llErr++;
        sqlCommit(0);
        continue;
      } else {
        wp.colSet(ll, "ok_flag", "V");
        llOk++;
        sqlCommit(1);
      }
    }
    alertMsg("成功=" + llOk + " 失敗=" + llErr);
    // dataRead();
    // dataRead1();
  }

  // input_data_check
  public int inputDataCheck(int ll) {

    String wkMchtNoCpy, ssType, ssType1;
    String[] aaMchtNoCpy = wp.itemBuff("mcht_no_cpy");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaMchtNoCpy.length;
    int llErr = 0;

    // 判斷mcht_no_cpy字串長度15 or 10

    if (aaMchtNoCpy[ll].length() == 15) {
      wkMchtNoCpy = aaMchtNoCpy[ll];
      // set_mcht_no_cpy()
      if (wkMchtNoCpy.contains("_") == true) {
        wkMchtNoCpy = wkMchtNoCpy.substring(0, 10);
        if (wkMchtNoCpy.contains("_") == true) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "err_mesg", "特店代號錯誤");
          return -1;
        }
      }
    }

    // double check 2 tables pk
    if (mExMchtNo.equals(aaMchtNoCpy[ll])) {
      wp.colSet(ll, "ok_flag", "X");
      wp.colSet(ll, "err_mesg", "來源特店與目的特店代號相同");
      return -1;
    }

    // 判斷來源與目的特店類別是否不同
    String lsSql = "select mcht_type from bil_merchant where mcht_no = ?";
    Object[] param = new Object[] {mExMchtNo};
    sqlSelect(lsSql, param);
    ssType = sqlStr("mcht_type");

    String lsSql1 = "select mcht_type from bil_merchant where mcht_no = ?";
    Object[] param1 = new Object[] {aaMchtNoCpy[ll]};
    sqlSelect(lsSql1, param1);
    if (sqlRowNum > 0) {
      ssType1 = sqlStr("mcht_type");
    } else {
      wp.colSet(ll, "ok_flag", "X");
      wp.colSet(ll, "err_mesg", "目的特店代號不存在");
      return -1;
    }
    if (ssType.equals(ssType1) == false) {
      wp.colSet(ll, "ok_flag", "X");
      wp.colSet(ll, "err_mesg", "來源特店與目的特店類別不同");
      return -1;
    }
    return 1;
  }

  public void insertFunc() throws Exception {
    func = new Bilm0330Func(wp);
    int rc = 0;
    mExMchtNo = wp.itemStr("kk_merchant");
    String[] aaMchtNoCpy = wp.itemBuff("mcht_no_cpy");
    String lsSql = "";
    if (empty(mExMchtNo)) {
      alertErr("請先輸入Source特店資料!!");
      return;
    }
    if (empty(aaMchtNoCpy[0])) {
      alertErr("請先輸入Destination特店資料!!");
      return;
    }

    // 判斷是否已有未放行料
    lsSql =
        "select count(*) as ct from bil_prod_copy_mas " + "where 1=1 " + "and confirm_flag != 'Y' ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      if (sqlNum("ct") > 0) {
        alertErr("此特店代號已有尚未放行之資料 !!");
        return;
      }
    }
    // 取特店類別
    lsSql = "select mcht_no, mcht_chi_name, mcht_type " + "from bil_merchant " + "where 1=1 ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mExMchtType = sqlStr("mcht_type");
      wp.colSet("mcht_type", sqlStr("mcht_type"));
    } else {
      alertErr("來源特店代號不存在 !!");
      return;
    }

    // 取最大序號
    lsSql = "select max(seq_no) as seq_no from bil_prod_copy_mas " + "where 1=1 ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mExSeqNo = numToStr(sqlNum("seq_no") + 1, "###");
    } else {
      mExSeqNo = "1";
      wp.colSet("seq_no", mExSeqNo);
    }

    // 資料檢核
    func.varsSet("aa_mcht_no", mExMchtNo);
    func.varsSet("aa_seq_no", mExSeqNo);
    func.varsSet("aa_mcht_type", mExMchtType);

    rc = func.dbInsert();
    if (rc <= 0) {
      alertErr2("資料新增失敗!");
      sqlCommit(0);
      return;
    }

    rc = insertFunc1();
    if (rc == 1) {
      alertMsg("資料新增成功!");
    } else {
      alertMsg("資料新增失敗!");
    }
    sqlCommit(rc > 0 ? 1 : 0);

  }

  // insert bil_prod_copy_dtl
  public int insertFunc1() throws Exception {
    String isSql = "", dsSql = "";
    int llOk = 0, llErr = 0, rc = 0;
    String[] aaMchtNoCpy = wp.itemBuff("mcht_no_cpy");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaMchtNoCpy.length;
    for (int ll = 0; ll < aaMchtNoCpy.length; ll++) {
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }
      if (empty(aaMchtNoCpy[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "目的特店資料空白 !!");
        llErr++;
        continue;
      }
      if (inputDataCheck(ll) != 1) {
        llErr++;
        continue;
      }

      // -check duplication-
      if (ll != Arrays.asList(aaMchtNoCpy).indexOf(aaMchtNoCpy[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "特店資料值重複 !!");
        llErr++;
        continue;
      }
      isSql = "insert into bil_prod_copy_dtl " + "(mcht_no, seq_no, mcht_no_cpy) "
          + "values (:mcht_no, :seq_no, :mcht_no_cpy )";
      setString("mcht_no", mExMchtNo);
      setString("seq_no", mExSeqNo);
      setString("mcht_no_cpy", aaMchtNoCpy[ll]);
      sqlExec(isSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "Insert bil_prod_copy_dtl error ");
        llErr++;
        sqlCommit(0);
        continue;
      } else {
        llOk++;
        wp.colSet(ll, "ok_flag", "V");
      }
    }
    if (llOk == 0) {
      return -1;
    }
    return 1;
  }

  // 匯入用新增Destination特店資料
  int insertFunc2() throws Exception {
    func = new Bilm0330Func(wp);
    int rc = 0;
    mExMchtNo = wp.itemStr("kk_merchant");
    String[] aaMchtNoCpy = wp.itemBuff("mcht_no_cpy");
    String lsSql = "";
    if (empty(mExMchtNo)) {
      alertErr("請先輸入Source特店資料!!");
      return -1;
    }
    // 判斷是否已有未放行料
    lsSql =
        "select count(*) as ct from bil_prod_copy_mas " + "where 1=1 " + "and confirm_flag != 'Y' ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      if (sqlNum("ct") > 0) {
        alertErr("此特店代號已有尚未放行之資料 !!");
        return -1;
      }
    }
    // 取特店類別
    lsSql = "select mcht_no, mcht_chi_name, mcht_type " + "from bil_merchant " + "where 1=1 ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mExMchtType = sqlStr("mcht_type");
      wp.colSet("mcht_type", sqlStr("mcht_type"));
    } else {
      alertErr("來源特店代號不存在 !!");
      return -1;
    }

    // 取最大序號
    lsSql = "select max(seq_no) as seq_no from bil_prod_copy_mas " + "where 1=1 ";
    lsSql += sqlCol(mExMchtNo, "mcht_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mExSeqNo = numToStr(sqlNum("seq_no") + 1, "###");
    } else {
      mExSeqNo = "1";
      wp.colSet("seq_no", mExSeqNo);
    }

    // 資料檢核
    func.varsSet("aa_mcht_no", mExMchtNo);
    func.varsSet("aa_seq_no", mExSeqNo);
    func.varsSet("aa_mcht_type", mExMchtType);

    rc = func.dbInsert();
    if (rc <= 0) {
      alertErr2("資料新增失敗!");
      return -1;
    }
    return 1;
  }

  public void updateFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
    if (wp.respHtml.indexOf("_add") > 0) {
      this.btnModeAud();
    }
    if (wp.respHtml.indexOf("_insert") > 0) {
      this.btnModeAud();
    }
  }

  public void deleteFunc() {
    int llOk = 0, llErr = 0;
    func = new Bilm0330Func(wp);
    if (wp.itemStr("confirm_flag").equals("Y")) {
      alertErr("已放行資料不可刪除!!");
      return;
    }
    rc = func.dbDelete();
    if (rc != 1) {
      alertErr2("資料刪除失敗!!");
    }
    sqlCommit(rc);
  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_mcht_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_mcht_no");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "", "where
      // 1=1 and loan_flag = 'N' order by mcht_no");

    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {

    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("kk_merchant", 0) + "%");
    } else {
      if (wp.respHtml.indexOf("_insert") > 0) {
        setString("mcht_no", wp.getValue("cc_merchant", 0) + "%");
      }
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }

    return;
  }

  public void processAjaxOption1(String type) throws Exception {

    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    setString("mcht_no", wp.getValue("kk_merchant", 0) + "%");
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
      wp.addJSON("ajtype", type);
    }

    return;
  }

  public void processAjaxOption2(String type) throws Exception {

    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    setString("mcht_no", wp.getValue("cc_merchant", 0) + "%");
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
      wp.addJSON("ajtype", type);
    }

    return;
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
    String lsSql = "", dateType = "", isSql = "";
    String inputFile = wp.itemStr("zz_file_name");
    String lsMchtNoMas = wp.itemStr("mcht_no1");
    if (empty(lsMchtNoMas)) {
      lsMchtNoMas = wp.itemStr("kk_merchant");
      if (empty(lsMchtNoMas)) {
        alertErr("請輸入來源特店資料!!");
        return;
      }
    }
    wp.showLogMessage("I", "", "Start  insertFunc2 ");
    if (insertFunc2() != 1) {
      alertMsg("來源特店資料新增失敗!!");
      return;
    }
    wp.showLogMessage("I", "", "End insertFunc2 ");

    String lsSeqNo = wp.itemStr("seq_no");
    if (empty(lsSeqNo)) {
      lsSeqNo = mExSeqNo;
    }
    // int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    int llOk = 0, llErr = 0, llCnt = 0;
    wp.logSql = false;
    while (true) {
      String file = "";
      try {
        file = tf.readTextFile(fi);
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }

      llCnt++;
      String[] spliLline = file.split(",");

      try {
        String ccDataCode = spliLline[0];// data_code特店代號
        // check
        lsSql = "select mcht_no_cpy " + "from bil_prod_copy_dtl " + "where 1=1 "
            + "and mcht_no =:mcht_no " + "and seq_no =:seq_no " + "and mcht_no_cpy =:mcht_no_cpy";
        setString("mcht_no", lsMchtNoMas);
        setString("seq_no", lsSeqNo);
        setString("mcht_no_cpy", ccDataCode);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          llErr++;
          continue;
        }

        // server debug message ==>只會顯示最後一筆訊息
        // wp.alertMesg = "<script
        // language='javascript'>alert('"+cc_data_code+"')</script>";
        isSql = "insert into bil_prod_copy_dtl " + "(mcht_no, seq_no, mcht_no_cpy) "
            + "values (:mcht_no, :seq_no, :mcht_no_cpy )";
        setString("mcht_no", lsMchtNoMas);
        setString("seq_no", lsSeqNo);
        setString("mcht_no_cpy", ccDataCode);
        sqlExec(isSql);
        if (sqlRowNum <= 0) {
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          llOk++;
        }

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
    try {
      tf.deleteFile(inputFile);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // alert_msg("資料匯入處理筆數: " + ll_cnt + ", 成功筆數=" + ll_ok + ", 失敗筆數=" +
    // ll_err);
    // dataRead();
    // }
    // if (wp.respHtml.indexOf("_insert") > 0) {
    // alert_msg("資料匯入處理筆數: " + ll_cnt + ", 成功筆數=" + ll_ok + ", 失敗筆數=" +
    // ll_err+" 請回主頁面重新查詢!!");
    // }
    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr);
    // dataRead();
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }
}
