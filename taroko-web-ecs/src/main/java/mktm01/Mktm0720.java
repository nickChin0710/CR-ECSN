/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-1  V1.00.00  Andy       program initial                             *
* 107-08-01 V1.00.01  Andy       Upadte Debug                                *
* 109-04-27 V1.00.02  YangFang   updated for project coding standard        * 
* 112-01-04 V1.00.03  Zuwei Su   getWhereStr重複調用        * 
******************************************************************************/

package mktm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Mktm0720 extends BaseAction {
  String mExBatchno = "";
  String mExEmbossSource = "";
  String mExEmbossReason = "";
  String gsReadMark = "";

  String gsMchtNo = "";
  String gsMchtGroupId = "";
  String gsMchtChiName = "";
  String gsMchtGroupIdNew = "";

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
    } else if (eqIgno(wp.buttonCode, "A")) {
      // insertFunc();
      strAction = "A";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      // updateFunc();
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      // deleteFunc();
      strAction = "D";
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      /* 執行 */
      strAction = "C2";
      setFunc();
    } else if (eqIgno(wp.buttonCode, "C3")) {
      /* 執行 */
      strAction = "C3";
      setFunc1();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exMchtGroupOld = wp.itemStr("ex_mcht_group_old");
    String exMchtGroupNew = wp.itemStr("ex_mcht_group_new");
    String exMchtNo1 = wp.itemStr("ex_mcht_no1");
    String exMchtNo2 = wp.itemStr("ex_mcht_no2");
    String exMchtCname = wp.itemStr("ex_mcht_cname");
    String exApprFlag = wp.itemStr("ex_appr_flag");
    wp.whereStr = " where 1=1 ";
    if (exApprFlag.equals("1")) {
      if (empty(exMchtGroupOld) & empty(exMchtGroupNew) & empty(exMchtNo1) & empty(exMchtCname)) {
        alertErr2("請輪入查詢條件!!");
        return false;
      }
    }

    if (exApprFlag.equals("1")) {
      wp.whereStr += sqlStrend(exMchtNo1, exMchtNo2, "a.mcht_no");
      wp.whereStr += sqlCol(exMchtGroupOld, "a.mcht_group_id");
      wp.whereStr += sqlCol(exMchtCname, "a.mcht_chi_name", "like%");
    }
    if (exApprFlag.equals("2")) {
      wp.whereStr += sqlStrend(exMchtNo1, exMchtNo2, "b.mcht_no");
      wp.whereStr += sqlCol(exMchtGroupOld, "b.mcht_group_id");
      wp.whereStr += sqlCol(exMchtGroupNew, "b.mcht_group_id_new");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false) {
//      return;
//    } ;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    String exApprFlag = wp.itemStr("ex_appr_flag");

    wp.pageControl();

    wp.selectSQL = "" + "b.mcht_group_id_new, " + "a.mcht_chi_name, " + "'0' db_delcode, "
        + "'1' db_temp, " + "a.mcht_no, " + "a.mcht_group_id, " + "'0' db_optcode, "
        + "b.mcht_no  as t_mcht_no , " + "'' wk_appr_flag," + "'' err_msg ";
    if (exApprFlag.equals("1")) {
      wp.daoTable = " bil_merchant a left join bil_mcht_group_t b on a.mcht_no = b.mcht_no ";
    } else {
      wp.daoTable = " bil_mcht_group_t b left join bil_merchant a on b.mcht_no = a.mcht_no ";
    }
    wp.whereOrder += " order by a.mcht_no ";
    if (getWhereStr() == false) {
        return;
    }

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr +
    // wp.whereOrder);
    // 重新計算筆數
    // wp.pageCount_sql = "select count(*) from ( select " + wp.selectSQL + " from " + wp.daoTable +
    // wp.whereStr + wp.whereOrder + " )";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";
    String wkTMchtNo = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wkTMchtNo = wp.colStr(ii, "t_mcht_no");
      if (empty(wkTMchtNo) == false) {
        wp.colSet(ii, "wk_appr_flag", "Y");
      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  public void setFunc() {
    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] setOpt = wp.itemBuff("set_opt");
    String[] deleteOpt = wp.itemBuff("delete_opt");
    String exMchtGroupNew = wp.itemStr("ex_mcht_group_new");
    if (empty(exMchtGroupNew)) {
      alertErr2("新特店群組代號碼不可空白!!");
      return;
    }
    for (int ii = 0; ii < aaMchtNo.length; ii++) {
      if (checkBoxOptOn(ii, setOpt)) {
        wp.colSet(ii, "mcht_group_id_new", exMchtGroupNew);
        wp.colSet(ii, "set_opt", "checked");
      }
    }
    wp.listCount[0] = aaMchtNo.length;
    alertMsg("群組代號碼已設定，按[存檔]完成資料異動!!");
  }

  public void setFunc1() {
    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] setOpt = wp.itemBuff("set_opt");

    // javax.swing.JOptionPane.getRootFrame().setAlwaysOnTop(true);
    // if(JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
    // "是否確定清除新特店代碼？", "訊息",JOptionPane.YES_NO_OPTION)!=0){
    // return;
    // }
    for (int ii = 0; ii < aaMchtNo.length; ii++) {
      if (checkBoxOptOn(ii, setOpt)) {
        wp.colSet(ii, "mcht_group_id_new", "");
        wp.colSet(ii, "set_opt", "checked");
      }
    }
    wp.listCount[0] = aaMchtNo.length;
    alertMsg("群組代號已清空，按[存檔]完成資料異動!!");
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    String exApprFlag = wp.itemStr("ex_appr_flag");
    String exOptFlag = wp.itemStr("ex_opt_flag");
    String[] setOpt = wp.itemBuff("set_opt");
    String[] deleteOpt = wp.itemBuff("delete_opt");
    String[] modSeqno = wp.itemBuff("mod_seqno");

    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] aaMchtGroupId = wp.itemBuff("mcht_group_id");
    String[] aaMchtGroupIdNew = wp.itemBuff("mcht_group_id_new");
    String[] aaWkApprFlag = wp.itemBuff("wk_appr_flag");
    String[] aaChiName = wp.itemBuff("chi_name");

    wp.listCount[0] = aaMchtNo.length;

    String dsSql = "", isSql = "", lsNoautoFlag = "";
    double ldcModSeqno = 0;
    int llOk = 0, llErr = 0, rr = 0;
    if (exOptFlag.equals("1")) {
      for (int ii = 0; ii < aaMchtNo.length; ii++) {
        if (!checkBoxOptOn(ii, setOpt))
          continue;
        if (empty(aaMchtNo[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "特店代號不可為空白");
          llErr++;
          sqlCommit(0);
          continue;
        }

        dsSql = "delete from bil_mcht_group_t " + "where mcht_no =:ls_mcht_no ";
        setString("ls_mcht_no", aaMchtNo[ii]);
        sqlExec(dsSql);
        if (sqlRowNum < 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "delete bil_mcht_group_t error");
          llErr++;
          sqlCommit(0);
          continue;
        }

        isSql = "INSERT INTO bil_mcht_group_t ( "
            + "mcht_no, mcht_group_id, mcht_group_id_new, crt_user, "
            + "crt_date, mod_user, mod_time, mod_pgm, " + "mod_seqno " + ") VALUES ( "
            + ":ls_mcht_no, :ls_group_id, :ls_group_id_new, :crt_user, "
            + "to_char(sysdate,'yyyymmdd'), :mod_user, sysdate, :mod_pgm, " + "0 " + " )";
        setString("ls_mcht_no", aaMchtNo[ii]);
        setString("ls_group_id", aaMchtGroupId[ii]);
        setString("ls_group_id_new", aaMchtGroupIdNew[ii]);
        setString("crt_user", wp.loginUser);
        setString("mod_user", wp.loginUser);
        setString("mod_pgm", "mktm0720");
        sqlExec(isSql);
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "insert bil_mcht_group_t error");
          llErr++;
          sqlCommit(0);
        } else {
          llOk++;
          wp.colSet(ii, "ok_flag", "V");
          wp.colSet(ii, "wk_appr_flag", "Y");
          sqlCommit(1);
        }
      }
    } else {
      for (int ii = 0; ii < aaMchtNo.length; ii++) {
        if (!checkBoxOptOn(ii, setOpt))
          continue;
        dsSql = "delete from bil_mcht_group_t " + "where mcht_no =:ls_mcht_no ";
        setString("ls_mcht_no", aaMchtNo[ii]);
        sqlExec(dsSql);
        if (sqlRowNum < 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "delete bil_mcht_group_t error");
          llErr++;
          sqlCommit(0);
        } else {
          llOk++;
          wp.colSet(ii, "ok_flag", "V");
          wp.colSet(ii, "mcht_group_id_new", "");
          wp.colSet(ii, "wk_appr_flag", "");
          sqlCommit(1);
        }
      }
    }
    alertMsg("資料處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnModeAud("XX");
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_mcht_group_old");
      this.dddwList("dddw_mcht_group_old", "mkt_mcht_group ", "mcht_group_id", "mcht_group_desc ",
          "where 1=1 and apr_date !='' order by mcht_group_id ");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_mcht_group_new");
      this.dddwList("dddw_mcht_group_new", "mkt_mcht_group ", "mcht_group_id", "mcht_group_desc ",
          "where 1=1 and apr_date !='' order by mcht_group_id ");
    } catch (Exception ex) {
    }
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
  }

  @Override
  public void procFunc() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    file_dataImp();
  }

  void file_dataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    Mktm0720Func func = new Mktm0720Func(wp);
    func.setConn(wp);

    String lsSql = "";
    int llOk = 0, llErr = 0, llCnt = 0;
    wp.logSql = false;
    while (true) {
      String tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (tmpStr.length() < 2) {
        continue;
      }

      // llCnt++;
      String[] splitLine = tmpStr.split(",");
      try {
        String lsMchtNo = splitLine[0]; // mcht_no
        String lsMchtGrouopId = splitLine[1];// mcht_grouop_id new
        // server debug message
        // wp.alertMesg = "<script language='javascript'>
        // alert('"+ls_id+ls_amt+ls_card+ls_type+ls_date+"')</script>";

        // check mcht_no
        lsSql = "SELECT mcht_chi_name, mcht_group_id " + "FROM bil_merchant " + "WHERE 1=1 ";
        lsSql += sqlCol(lsMchtNo, "mcht_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          llErr++;
          continue;
        } else {
          gsMchtNo = lsMchtNo;
          gsMchtGroupId = sqlStr("mcht_group_id");
          gsMchtGroupIdNew = lsMchtGrouopId;
          gsMchtChiName = sqlStr("mcht_chi_name");
        }
        func.varsSet("aa_mcht_no", gsMchtNo);
        func.varsSet("aa_mcht_group_id", gsMchtGroupId);
        func.varsSet("aa_mcht_group_id_new", gsMchtGroupIdNew);
        wp.itemSet("mcht_no", gsMchtNo);
        wp.itemSet("mcht_group_id", gsMchtGroupId);
        wp.itemSet("mcht_group_id_new", gsMchtGroupIdNew);
        wp.itemSet("mcht_chi_name", gsMchtChiName);

        // 刪除原bil_mcht_group_t資料
        String ds_sql = "delete from bil_mcht_group_t " + "where mcht_no =:ls_mcht_no ";
        setString("ls_mcht_no", gsMchtNo);
        sqlExec(ds_sql);
        // 新增bil_mcht_group_t
        rc = func.dbInsert();
        if (rc < 0) {
          llErr++;
          continue;
        } else {
          llOk++;
          sqlCommit(1);
        }
        // 固定長度上傳檔
        // wp.item_set("id_no",commString.mid_big5(ss,0,10));
        // wp.item_set("data_flag1",commString.mid_big5(ss,10,1));
        // wp.item_set("data_flag2",commString.mid_big5(ss,11,1));

      } catch (Exception e) {
        alertMsg("匯入資料異常!!");
        return;
      }

      llCnt++;
      int rr = llCnt - 1;
      this.setRowNum(rr, llCnt);

    }
    // wp.listCount[0]=llCnt; //--->開啟上傳檔檢視
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr);
  }
}
