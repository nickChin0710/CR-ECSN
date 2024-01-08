/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*                        V1.00.01  Ray Ho     program initial                            * 
* 108-07-02  V1.00.02  Andy		    update :card_no itemchange                 *		
* 109-01-03  V1.00.03  Justin Wu  updated for archit.  change
* 109-04-21  V1.00.04  yanghan  修改了變量名稱和方法名稱*
* 109-09-14  V1.00.05  JeffKung  本金類交易開放加檔 *
* 109-12-30  V1.00.06  shiyuqi    修改無意義的命名
* 110-01-05  V1.00.07  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *
******************************************************************************/

package dbbm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Dbbm0010 extends BaseAction {
  String rowid = "", modSeqno = "";
  String gsChiName = "";
  String gsCardNo = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();

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
      case "C2":
        // -批次刪除-
        procDel();
        break;
      case "UPLOAD":
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        wfAjaxKey();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "dbbm0010_detl")) {
        wp.optionKey = wp.colStr(0, "add_item");
        dddwList("dddw_add_item", "ptr_billtype", "txn_code", "txn_code||'_'||exter_desc",
            "where (bill_type ='OKOL' )");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";

    lsWhere = " where 1=1 " + " and (A.post_flag = 'N' or A.post_flag = '') "
        + sqlCol(wp.itemStr("ex_key_no"), "A.key_no", "like%")
        + sqlCol(wp.itemStr("ex_card_no"), "A.card_no", "like%")
        + sqlCol(wp.itemStr("ex_idno"), "B.id_no", "like%")
        + sqlCol(wp.itemStr("ex_user"), "A.mod_user", "like%");

    if (wp.itemEq("ex_error_code", "Y")) {
      lsWhere += " and A.error_code <> ''";
    } else if (wp.itemEq("ex_error_code", "N")) {
      lsWhere += " and A.error_code = ''";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " B.id_no ," + " B.chi_name ," + " A.acct_type ," + " A.key_no ,"
        + " A.bill_type ," + " A.txn_code ," + " A.add_item ," + " A.card_no ," + " A.id_p_seqno ,"
        + " A.seq_no ," + " A.dest_amt ," + " A.dest_curr ," + " A.purchase_date ,"
        + " A.chi_desc ," + " A.bill_desc ," + " A.dept_flag ," + " A.apr_flag ," + " A.apr_user ,"
        + " A.post_flag ," + " A.mod_user ," + " A.mod_time ," + " A.mod_pgm ," + " A.mod_seqno ,"
        + " hex(A.rowid) as rowid , " + " A.error_code ";
    wp.daoTable = " dbb_othexp A left join dbc_idno B on A.id_p_seqno = B.id_p_seqno ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料 ");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    modSeqno = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(rowid)) {
      rowid = wp.itemStr("rowid");
    }

    if (empty(modSeqno)) {
      modSeqno = wp.itemStr("mod_seqno");
    }

    wp.selectSQL = " uf_idno_id2(id_p_seqno,'Y') as id_no , " + " key_no , " + " acct_type , "
        + " uf_idno_name2(id_p_seqno,'Y') as chi_name , " + " add_item , " + " card_no , "
        + " dest_amt , " + " purchase_date , " + " bill_desc , " + " apr_user , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " hex(rowid) as rowid , "
        + " mod_seqno , " + " apr_flag ";
    wp.daoTable = "dbb_othexp";
    wp.whereStr = " where 1=1 " + sqlCol(modSeqno, "mod_seqno") + sqlRowId(rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    Dbbm0010Func func = new Dbbm0010Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
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

    if (wp.itemEmpty("ex_key_no")) {
      alertErr2("登錄批號: 不可空白");
      return;
    }

    fileDataImp();

  }

  void fileDataImp() throws Exception {
    String lsIdNo = "", lsCorpNo = "", lsCardNo = "", lsAddItem = "", lsPurchaseDate = "",
        lsBillDesc = "", liDestAmt = "", lsErrCode = "", lsPSeqno = "", lsIdPSeqno = "" , 
        lsAcctCode = "";
    double liiDestAmt = 0;

    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");

    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    Dbbm0010Func func = new Dbbm0010Func();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));
    wp.itemSet("data_from", "2");

    String sql1 = "select id_p_seqno , id_no_code , chi_name from dbc_idno where id_no = ? ";
    String sql2 = "select acct_code from ptr_billtype where bill_type = 'OKOL' and txn_code = ? ";
    String sql3 = "select p_seqno , card_no , id_p_seqno as dbc_id_p_seqno from dbc_card where card_no = ? ";
    String sql4 = "select acct_status from dba_acno where p_seqno = ? ";

    int isOk = 0, count = 0;
    while (true) {
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }

      count++;
      lsErrCode = "";
      lsIdPSeqno = "";
      lsPSeqno = "";
      // --身分證,金額,卡號,加檔項目,消費日期,對帳單文字
      String[] tt = new String[2];
      tt[0] = file;
      tt = commString.token(tt, ",");
      lsIdNo = tt[1];
      tt = commString.token(tt, ",");
      liDestAmt = tt[1];
      liiDestAmt = commString.strToNum(liDestAmt);
      // if(lii_dest_amt <=0) continue;
      tt = commString.token(tt, ",");
      lsCardNo = tt[1];
      tt = commString.token(tt, ",");
      lsAddItem = tt[1];
      tt = commString.token(tt, ",");
      lsPurchaseDate = tt[1];
      if (lsPurchaseDate.length() == 7) {
        lsPurchaseDate = commDate.twToAdDate(lsPurchaseDate);
      }
      tt = commString.token(tt, ",");
      lsBillDesc = tt[1];

      // --檢核ID
      if (lsIdNo.length() > 0 ) {
    	  sqlSelect(sql1, new Object[] {lsIdNo});
          if (sqlRowNum <= 0) {
        	  if (empty(lsErrCode))
        		  lsErrCode = "1";
        	  else
        		  lsErrCode += ",1";
          } else {
        	  lsIdPSeqno = sqlStr("id_p_seqno");
          }
       }
      // --檢核金額
      if (isNumber(liDestAmt) == false) {
        if (empty(lsErrCode))
          lsErrCode = "3";
        else
          lsErrCode += ",3";
      }

      // --檢核卡片
      sqlSelect(sql3, new Object[] {lsCardNo});
      if (sqlRowNum <= 0) {
        if (empty(lsErrCode))
          lsErrCode = "4";
        else
          lsErrCode += ",4";
      } else {
        lsPSeqno = sqlStr("p_seqno");
      }
      
      if (sqlRowNum > 0) {
        // --檢核ID 與卡號對應
        if (lsIdNo.length() > 0) {
           	if (!lsIdPSeqno.equals(sqlStr("dbc_id_p_seqno"))) {
            	if (empty(lsErrCode))
            		lsErrCode = "1";
            	else
            		lsErrCode += ",1";
            }
        }
      }
      
      // --加檔項目
      sqlSelect(sql2, new Object[] {lsAddItem});
      if (sqlRowNum <= 0) {
        if (empty(lsErrCode))
          lsErrCode = "2";
        else
          lsErrCode += ",2";
      } else
        lsAcctCode = sqlStr("acct_code");

      // --檢核帳戶
      sqlSelect(sql4, new Object[] {lsPSeqno});
      if (sqlRowNum <= 0) {
        if (empty(lsErrCode))
          lsErrCode = "5";
        else
          lsErrCode += ",5";
      }
      
      func.varsSet("bill_type", "OKOL");
      func.varsSet("id_no", lsIdNo);
      func.varsSet("id_p_seqno", lsIdPSeqno);
      func.varsSet("dest_amt", liDestAmt);
      func.varsSet("card_no", lsCardNo);
      func.varsSet("add_item", lsAddItem);
      func.varsSet("txn_code", lsAddItem);
      func.varsSet("purchase_date", lsPurchaseDate);
      func.varsSet("bill_desc", lsBillDesc);
      func.varsSet("error_code", lsErrCode);
      func.varsSet("p_seqno", lsPSeqno);
      func.varsSet("key_no", wp.itemStr2("ex_key_no"));
      if (func.dataProc() == 1) {
        isOk++;
        sqlCommit(1);
      } else {
        sqlCommit(-1);
      }

    }

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    alertMsg("資料匯入處理筆數: " + count + ", 成功筆數=" + isOk);

    return;
  }

  @Override
  public void initButton() {
    btnModeAud();
    if (!wp.colEmpty("rowid")) {
      colReadOnly("db_read");
    }
  }

  @Override
  public void initPage() {

  }

  public void wfAjaxKey() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    String ajaxName = wp.itemStr("ajaxName");
    switch (ajaxName) {
      case "idno":
        selectData(wp.itemStr("ax_idno"));
        if (rc != 1) {
          wp.addJSON("chi_name", "");
          wp.addJSON("card_mo", "");
          return;
        }
        wp.addJSON("chi_name", sqlStr("chi_name"));
        wp.addJSON("card_no", sqlStr("card_no"));
        break;
      case "cardno":
        selectData2(wp.itemStr("ax_cardno"));
        if (rc != 1) {
          wp.addJSON("chi_name", "");
          wp.addJSON("id_no", "");
          return;
        }
        wp.addJSON("chi_name", sqlStr("chi_name"));
        wp.addJSON("id_no", sqlStr("id_no"));
        break;
    }


  }

  void selectData(String idNo) {
    String sql1 = " select " + " a.chi_name, " + " b.card_no "
        + " from dbc_idno a left join dbc_card b" + " on a.id_p_seqno = b.id_p_seqno "
        + " where id_no = ? " + " and b.current_code ='0' " + " fetch first 1 rows only ";

    sqlSelect(sql1, new Object[] {idNo});
    if (sqlRowNum <= 0) {
      alertErr2("查無姓名、卡號, 身分證字號:" + idNo);
      return;
    }
  }

  void selectData2(String cardNo) {
    String sql1 = "select b.id_no, " + "b.chi_name " + "from dbc_card a left join dbc_idno b "
        + "on a.id_p_seqno =b.id_p_seqno " + "where 1=1 and a.current_code = '0' ";
    sql1 += sqlCol(cardNo, "a.card_no");
    sql1 += " fetch first 1 rows only ";
    sqlSelect(sql1);

    if (sqlRowNum <= 0) {
      alertErr2("卡號無效或查無姓名及身分證字號    卡號:" + cardNo);
      return;
    }
  }

  void procDel() {
    int count = 0, isOk = 0, isError = 0;
    dbbm01.Dbbm0010Func func = new dbbm01.Dbbm0010Func();
    func.setConn(wp);

    String[] optArray = wp.itemBuff("opt");
    String[] lsRowidArray = wp.itemBuff("rowid");
    wp.listCount[0] = wp.itemRows("rowid");
    int rr = 0;
    for (int ii = 0; ii < optArray.length; ii++) {
      rr = (int) optToIndex(optArray[ii]);
      if (rr < 0) {
        continue;
      }
      count++;
      func.varsSet("rowid", lsRowidArray[rr]);
      rc = func.deleteProc();
      if (rc == 1) {
        isOk++;
        sqlCommit(1);
        wp.colSet(rr, "ok_flag", "V");
        continue;
      } else {
        isError++;
        sqlCommit(-1);
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }
    }

    if (count <= 0) {
      alertErr2("請點選欲刪除資料 !");
      return;
    }

    alertMsg("刪除完成,成功:" + isOk + " 失敗:" + isError);


  }

}
