package secm01;
/**
 * 2019-1205   JH    安控
 * 2019-1017   JH    UTF-8 >>MS950
 * 2019-1016   JH    copy-check
 * 2019-0822   JH    Excel.header
 * 109-04-20  shiyuqi       updated for project coding standard     *
 * 109-12-24  Justin          parameterize sql
 * 110/1/4    V1.00.04  yanghan       修改了變量名稱和方法名稱            *
 * 110-01-05  V1.00.05  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
 * 110-01-06  V1.00.03  shiyuqi       修改无意义命名 
 * 110-02-02  Justin                        fix the bugs about encoding                                                                               *    
 * */

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
import taroko.com.TarokoFileAccess;

import java.nio.charset.StandardCharsets;

public class Secp2060 extends BaseAction implements InfaceExcel {
  String lsWhere = "";
  boolean ibPrint = false;
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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      uploadProcFunc();
    } else if (eqIgno(wp.buttonCode, "C3")) {
      copyData();
    } else if (eqIgno(wp.buttonCode, "XLS")) {
      strAction = "XLS";
      xlsPrint();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "secp2060")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1 and apr_date<>''");
        wp.optionKey = wp.colStr(0, "ex_copy_group_id");
        dddwList("dddw_copy_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1 and apr_date<>''");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "secp2060")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
        wp.optionKey = wp.colStr(0, "ex_copy_user_level");
        dddwList("dddw_copy_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    // ls_where = " where 1=1 "
    // + sql_col(wp.item_ss("ex_group_id"), "A.group_id")
    // + sql_col(wp.item_ss("ex_user_level"), "A.user_level");
    //
    // wp.whereStr = ls_where;
    // wp.queryWhere = wp.whereStr;
    // wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.colSet("kk_group_id", wp.itemStr("ex_group_id"));
    wp.colSet("kk_user_level", wp.itemStr("ex_user_level"));
    wp.colSet("kk_apr_flag", wp.itemStr("ex_apr_flag"));
    // if (wp.col_eq("kk_apr_flag","Y")) {
    // wp.col_set("tt_apr_flag","已覆核");
    // }
    // else wp.col_set("tt_apr_flag","未覆核");
    String tmpStr = commString.decode(wp.colStr("kk_apr_flag"), new String[] {"Y", "N"},
        new String[] {"己覆核", "未覆核"});
    wp.colSet("tt_apr_flag", tmpStr);

    if (wp.itemEq("ex_apr_flag", "Y")) {
      wp.sqlCmd = "select " + " A.group_id , " + " A.user_level , "
          + " A.wf_winid, A.aut_query, A.aut_update, A.aut_approve"
          // + ", A.aut_print"
          + ", B.wf_name, B.wf_update, B.wf_approve"
          // + ", B.wf_print"
          + ", decode(B.wf_update,'Y','','none') as disp_update"
          + ", decode(B.wf_approve,'Y','','none') as disp_approve"
          // +", decode(B.wf_print,'Y','','none') as disp_print"
          + ", 'checked' as opt_on" + ", decode(A.aut_query,'Y','checked','') as query_on"
          + ", decode(A.aut_update,'Y','checked','') as update_on"
          + ", decode(A.aut_approve,'Y','checked','') as appr_on"
          // +", decode(A.aut_print,'Y','checked','') as print_on"
          + " from sec_authority A join sec_window B " + " on A.wf_winid =B.wf_winid" + " where 1=1"
          + sqlCol(wp.itemStr2("ex_group_id"), "A.group_id")
          + sqlCol(wp.itemStr2("ex_user_level"), "A.user_level") + " order by A.wf_winid";
    } else {
      wp.sqlCmd = "select " + " A.group_id , " + " A.user_level , "
          + " A.wf_winid, A.aut_query, A.aut_update, A.aut_approve"
          // + ", A.aut_print"
          + ", B.wf_name, B.wf_update, B.wf_approve"
          // + ", B.wf_print"
          + ", decode(B.wf_update,'Y','','none') as disp_update"
          + ", decode(B.wf_approve,'Y','','none') as disp_approve"
          // +", decode(B.wf_print,'Y','','none') as disp_print"
          + ", 'checked' as opt_on" + ", decode(A.aut_query,'Y','checked','') as query_on"
          + ", decode(A.aut_update,'Y','checked','') as update_on"
          + ", decode(A.aut_approve,'Y','checked','') as appr_on"
          // +", decode(A.aut_print,'Y','checked','') as print_on"
          + " from sec_authority_log A join sec_window B " + " on A.wf_winid =B.wf_winid"
          + " where A.apr_flag <>'Y'" 
          + sqlCol(wp.itemStr2("ex_group_id"), "A.group_id")
          + sqlCol(wp.itemStr2("ex_user_level"), "A.user_level") 
          + " order by A.wf_winid";
    }

//    setString("group_id", wp.itemStr("ex_group_id"));
//    setString("user_level", wp.itemStr("ex_user_level"));

    this.pageQuery();
    wp.setListCount(1);
    if (wp.selectCnt <= 0) {
      alertErr2("查無資料");
      return;
    }
    // queryAfter(sql_nrow);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  void keepCheckBox(int listNum) {
    this.optNumKeep(listNum);
    this.optNumKeep(listNum, "aut_query", "query_on");
    this.optNumKeep(listNum, "aut_update", "update_on");
    this.optNumKeep(listNum, "aut_approve", "appr_on");
    // this.opt_numKeep(list_num,"wf_print_A","print_on");
  }

  @Override
  public void saveFunc() throws Exception {
    Secp2060Func func = new Secp2060Func();
    func.setConn(wp);

    int listRow = wp.itemRows("wf_winid");
    wp.listCount[0] = listRow;
    keepCheckBox(listRow);

    if (func.dataCheckUpdate() != 1) {
      alertErr2(func.mesg());
      return;
    }

    String[] aaWinid = wp.itemBuff("wf_winid");
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaQuery = wp.itemBuff("aut_query");
    String[] aaUpdate = wp.itemBuff("aut_update");
    String[] aaAppr = wp.itemBuff("aut_approve");
    // String[] aa_print =wp.item_buff("aut_print");

    String[] aaCode = new String[5];
    int llOk = 0, llErr = 0;
    for (int ll = 0; ll < aaWinid.length; ll++) {
      if (checkBoxOptOn(ll, aaOpt) == false)
        continue;

      aaCode = new String[] {"", "", "", "", ""};
      aaCode[0] = aaWinid[ll];
      if (checkBoxOptOn(ll, aaQuery))
        aaCode[1] = "Y";
      if (wp.itemEq(ll, "wf_update", "Y") && checkBoxOptOn(ll, aaUpdate))
        aaCode[2] = "Y";
      if (wp.itemEq(ll, "wf_approve", "Y") && checkBoxOptOn(ll, aaAppr))
        aaCode[3] = "Y";
      // if (wp.item_eq(ll, "wf_print","Y") && checkBox_opt_on(ll,aa_print))
      // aa_code[4] ="Y";

      if (func.procUpdate(aaCode) == 1)
        llOk++;
      else
        llErr++;
    }
    sqlCommit(1);
    okMsg(commString.format("權限異動處理完成; 成功[%s], 失敗[%s]", llOk, llErr));

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;

    Secp2060Func func = new Secp2060Func();
    func.setConn(wp);

    String lsGroupId = wp.itemStr("kk_group_id");
    String lsUserLevel = wp.itemStr("kk_user_level");
    String[] lsWfWinid = wp.itemBuff("wf_winid");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsUpdateA = wp.itemBuff("wf_update_A");
    String[] lsApproveA = wp.itemBuff("wf_approve_A");
    // String[] ls_print_A = wp.item_buff("wf_print_A");
    wp.listCount[0] = wp.itemRows("wf_winid");

    // func.vars_set("group_id", ls_group_id);
    // func.vars_set("user_level", ls_user_level);

    if (func.delLog(lsGroupId, lsUserLevel) == -1) {
      this.dbRollback();
      errmsg(func.getMsg());
      return;
    }

    for (int ii = 0; ii < wp.itemRows("wf_winid"); ii++) {
      if (this.checkBoxOptOn(ii, aaOpt) == false)
        continue;

      func.varsSet("wf_winid", lsWfWinid[ii]);

      if (checkBoxOptOn(ii, lsUpdateA)) {
        func.varsSet("aut_update", "Y");
      } else {
        func.varsSet("aut_update", "N");
      }

      if (checkBoxOptOn(ii, lsApproveA)) {
        func.varsSet("aut_approve", "Y");
      } else {
        func.varsSet("aut_approve", "N");
      }


      if (func.dbInsert() == 1) {
        llOk++;
        sqlCommit(1);
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        continue;
      }

    }

    alertMsg("指定權限完成 , 成功 : " + llOk + " , 失敗: " + llErr);

  }

  void uploadProcFunc() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    if (wp.itemEmpty("kk_group_id") || wp.itemEmpty("kk_user_level")) {
      alertErr2("子系統代碼, 使用者層級: 不可空白, 請先查詢");
      return;
    }

    fileDataImp();
  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }
    
    String lsFileErr = inputFile + "-" + wp.sysTime + ".err";
    int fileErr = tf.openOutputText(lsFileErr, "UTF-8");
    // int file_err =tf.openOutputText(ls_file_err,"utf-8");

    Secp2060Func func = new Secp2060Func();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));
    String[] tt = new String[2];
    int llOk = 0, llCnt = 0, llErr = 0;

    // func.vars_set("group_id", wp.item_ss("ex_group_id"));
    // func.vars_set("user_level", wp.item_ss("ex_user_level"));
    String lsGroup = wp.itemStr2("ex_group_id");
    String lsLevel = wp.itemStr2("ex_user_level");
    if (wp.itemEq("ex_all_level", "Y")) {
      lsLevel = "%";
    }

    if (func.delLog(lsGroup, lsLevel) == -1) {
      errmsg(func.getMsg());
      return;
    }

    int ll = 0; // avoid 無窮LOOP
    String kkData = "";
    while (ll < 99999) {
      ll++;

      String lsData = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (lsData.length() < 2) {
        continue;
      }
      // 跳過標題行
      if (ll == 1) {
        continue;
      }
      llCnt++;
      if (eqAny(lsData, kkData))
        continue;

      func.varsSet("data", lsData);
      int liRc = func.dbInsert();
      if (liRc == 1)
        llOk++;
      else if (liRc == -1) {
        llErr++;
        String msg = lsData + ";" + func.getMsg();
        tf.writeTextFile(fileErr, msg + wp.newLine);
      }
      kkData = lsData;
    }
    if (llOk > 0) {
      sqlCommit(1);
    } else {
      sqlCommit(-1);
    }

    tf.writeTextFile(fileErr, "資料處理完成" + wp.newLine);
    tf.closeOutputText(fileErr);
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    if (llErr > 0) {
      wp.setDownload(lsFileErr);
      // wp.setDownload2(ls_file_err);
    }
    wp.colSet("zz_file_name", "");
    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr);
    return;
  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void xlsPrint() throws Exception {
    // queryFunc();
    // if(wp.selectCnt<=0){
    int llNrow = wp.itemRows("wf_winid");
    if (llNrow <= 0) {
      alertErr2("無資料可列印");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.listCount[0] = llNrow;

    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String fileName = "secp2060-" + commString.mid(commDate.sysDatetime(), 4) + ".csv";

    // int file =tf.openOutputText(file_name,"UTF-8");
    int file = tf.openOutputText(fileName, "MS950");
    String lsData = "子系統,使用者層級,程式代碼,作業說明,查詢,維護,線上覆核";
    tf.writeTextFile(file, lsData + wp.newLine);
    String[] aaQuery = wp.itemBuff("aut_query");
    String[] aaUpdate = wp.itemBuff("aut_update");
    String[] aaAppr = wp.itemBuff("aut_approve");

    for (int ii = 0; ii < llNrow; ii++) {
      lsData = wp.itemStr("kk_group_id") + "," + wp.itemStr("kk_user_level") + ","
          + wp.itemStr(ii, "wf_winid") + "," + wp.itemStr(ii, "wf_name");
      if (checkBoxOptOn(ii, aaQuery))
        lsData += ",Y";
      else
        lsData += ",N";
      if (checkBoxOptOn(ii, aaUpdate))
        lsData += ",Y";
      else
        lsData += ",N";
      if (checkBoxOptOn(ii, aaAppr))
        lsData += ",Y";
      else
        lsData += ",N";

      tf.writeTextFile(file, lsData + wp.newLine);
    }
    tf.closeOutputText(file);
    wp.setDownload(fileName);

    // try {
    // ddd("xlsFunction: started--------");
    // wp.reportId = "Secp2060";
    // TarokoExcel xlsx = new TarokoExcel();
    // wp.fileMode = "Y";
    // xlsx.excelTemplate = "secp2060.xlsx";
    // wp.pageRows = 9999;
    // ib_print = true;
    // queryFunc();
    // xlsx.processExcelSheet(wp);
    // xlsx.outputExcel();
    // xlsx = null;
    // ddd("xlsFunction: ended-------------");
    // }
    // catch (Exception ex) {
    // wp.expMethod = "xlsPrint";
    // wp.expHandle(ex);
    // }

  }

  public void copyData() throws Exception {

    wp.listCount[0] = wp.itemRows("wf_winid");

    if (wp.itemEmpty("ex_copy_group_id")) {
      alertErr2("複製到 子系統名稱 不可空白 !!");
      return;
    }
    if (wp.itemEmpty("ex_copy_user_level")) {
      alertErr2("複製到 使用者層級 不可空白 !!");
      return;
    }
    if (eqIgno(wp.itemStr2("ex_group_id"), wp.itemStr2("ex_copy_group_id"))
        && eqIgno(wp.itemStr2("ex_user_level"), wp.itemStr2("ex_copy_user_level"))) {
      alertErr("複製 子系統,層級: 不可相同");
      return;
    }

    Secp2060Func func = new Secp2060Func();
    func.setConn(wp);

    rc = func.copyData();
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      alertMsg("複製完成");
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
