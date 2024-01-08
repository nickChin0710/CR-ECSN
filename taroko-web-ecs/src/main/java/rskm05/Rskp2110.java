/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-07-17  V1.00.01  Zuwei       兆豐 => 合庫      *
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package rskm05;
/** 流通卡不良記錄通報匯入處理
 * 2019-1206   Alex  add initButton
 * 2019-1125   JH    1.JCIC/2.行內
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 *  V1080315.jh
   1080315:    JH    bugfix
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
* */

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Rskp2110 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "Q2")) {
      queryRead2();
    } else if (eqIgno(wp.buttonCode, "D2")) {
      saveFunc2();
    }

  }
  
  void saveFunc2() throws Exception {
	  
	  if(wp.itemEmpty("ex_crt_date")) {
		  alertErr("匯入日期不可空白");
		  return ;
	  }
	  
	  rskm05.Rskp2110Func func = new rskm05.Rskp2110Func(); 
	  func.setConn(wp);
	  
	  rc = func.deleteAll();
	  sqlCommit(rc);
	  if(rc!=1) {
		  alertErr("刪除未處理資料失敗");
		  return ;
	  }	  
  }
  
  void queryRead2() throws Exception {
	  wp.pageControl();
	  wp.selectSQL = " crt_date , id_no , corp_no , from_type , chi_name , "
	  			   + " imp_file , annou_type , stop_reason , stop_date , bank_no , "
	  			   + " bank_name , crt_user , block_reason4 , id_code_rows , "
	  			   + " decode(annou_type,'1','他行強停','2','支票拒往','6','退票','9','轉催停用') as tt_annou_type , "
	  			   + " '人工匯入' as tt_from_type "	  			   
			  	   ;
	  wp.daoTable = " rsk_bad_annou ";
	  wp.whereStr = " where 1=1 and proc_flag <> 'Y' "
			      + sqlCol(wp.itemStr("ex_crt_date"),"crt_date");
			  	  ;
	  wp.whereOrder = " order by crt_date , id_no , corp_no , from_type ";
	  pageQuery();
	  
	  if(sqlNotFind()) {
		  alertErr("查無未處理資料");
		  return ;
	  }
	  
	  wp.setListCount(0);
	  wp.setPageValue();
	  
  }
  
  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.colEmpty("zz_file_name")) {
      alertErr2("匯入媒體檔: 不可空白");
      return;
    }

    if (wp.itemEq("ex_from_type", "4")) {
      if (empty(wp.itemStr("ex_annou_type"))) {
        alertErr2("請指定 通報類別");
        return;
      }
//      if (wp.itemStr("ex_proc_reason").length() != 2) {
//        alertErr2("請指定 停掛原因");
//        return;
//      }
    }    

    personInsertQuery();
  }

  void dataInsertQuerry() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }
    int llOk = 0, llCnt = 0;
    int llErr = 0, llTxt = 0;
    String lsAnnouType = ttAnnouType();
    while (true) {
      llTxt++;
      String data = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y") || llTxt > 9999) {
        break;
      }
      if (data.length() < 2) {
        continue;
      }

      String list = "";
      list = commString.midBig5(data, 0, 1);
      if (commString.pos(",A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,0,1,2,3,4,5,6,7,8,9",
          list) <= 0)
        continue;

      String lsIdNo = commString.midBig5(data, 0, 10);
      String stopDate = commString.midBig5(data, 50, 9).replace("/", "");
      // ddd("A:"+commString.mid_big5(ss,0,10));
      int liDate = 0;
      liDate = Integer.parseInt(stopDate) + 19110000;

      wp.colSet(llCnt, "opt_on", "checked");
      // wp.col_set(ll_cnt,"db_err_text","");

      wp.colSet(llCnt, "crt_date", wp.sysDate); // this.get_sysDate()
      wp.colSet(llCnt, "id_no", commString.midBig5(data, 0, 10));
      wp.colSet(llCnt, "from_type", "3"); // -JCIC-
      wp.colSet(llCnt, "tt_from_type", "他行強停-JCIC");
      wp.colSet(llCnt, "chi_name", commString.midBig5(data, 11, 10));
      wp.colSet(llCnt, "imp_file", wp.itemStr("zz_file_name"));
      wp.colSet(llCnt, "annou_type", wp.itemStr2("ex_annou_type"));
      wp.colSet(llCnt, "tt_annou_type", lsAnnouType);
      wp.colSet(llCnt, "stop_date", "" + liDate);
      wp.colSet(llCnt, "stop_reason", commString.midBig5(data, 60, 10));
      wp.colSet(llCnt, "bank_name", commString.midBig5(data, 22, 16));
      wp.colSet(llCnt, "block_reason4", "06");
      wp.colSet(llCnt, "crt_user", wp.loginUser);
      selectCount(lsIdNo.trim());
      if (sqlNum("db_cnt") > 1) {
        wp.colSet(llCnt, "id_code_rows", sqlStr("db_cnt"));
      } else {
        wp.colSet(llCnt, "id_code_rows", "0");
      }

      llCnt++;
      int rr = llCnt - 1;
      this.setRowNum(rr, llCnt);
    }
    wp.colSet("tl_cnt", llCnt);
    wp.listCount[0] = llCnt;
    // wp.setListCount(1);
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

  }

  String ttAnnouType() {
    String tmpStr = wp.itemStr2("ex_annou_type");
    if (eqAny(tmpStr, "1"))
      return "他行強停";
    else if (eqIgno(tmpStr, "2"))
      return "支票拒往";
    else if (eqIgno(tmpStr, "3"))
      return "失業/收入不足";
    else if (eqIgno(tmpStr, "4"))
      return "親屬代償訊息";
    else if (eqIgno(tmpStr, "5"))
      return "授信異常";
    else if (eqIgno(tmpStr, "6"))
      return "退票";
    else if (eqIgno(tmpStr, "9"))
      return "轉催停用";
    else if (eqIgno(tmpStr, "A"))
      return "ID失效";

    return "";
  }

  void personInsertQuery() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }
    int llOk = 0, llCnt = 0;
    int llErr = 0, llTxt = 0;
    String ls_annou_type = ttAnnouType();
    while (true && llTxt < 9999) {
      llTxt++;
      String tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (tmpStr.length() < 2) {
        continue;
      }

      String lsS1 = "";
      lsS1 = commString.midBig5(tmpStr, 0, 1);
      if (commString.pos(",A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,0,1,2,3,4,5,6,7,8,9",
          lsS1) <= 0)
        continue;

      String lsIdNo = commString.midBig5(tmpStr, 0, 10);
      if (lsIdNo.trim().length() == 10) {
        wp.colSet(llCnt, "id_no", lsIdNo.trim());
        selectChiName(lsIdNo.trim());
      } else {
        wp.colSet(llCnt, "corp_no", lsIdNo.trim());
        selectCorpName(lsIdNo.trim());
      }
      wp.colSet(llCnt, "opt_on", "checked");
      // wp.col_set(ll_cnt,"db_err_text","");

      wp.colSet(llCnt, "crt_date", wp.sysDate);
      wp.colSet(llCnt, "chi_name", sqlStr("chi_name"));
      wp.colSet(llCnt, "from_type", "4"); // -2.行內-
      wp.colSet(llCnt, "tt_from_type", "人工匯入");
      wp.colSet(llCnt, "annou_type", wp.itemStr2("ex_annou_type"));
      wp.colSet(llCnt, "tt_annou_type", ls_annou_type);
      if(wp.itemEq("ex_annou_type", "1")) {
    	  wp.colSet(llCnt, "block_reason4", "E2");
      }	else if(wp.itemEq("ex_annou_type", "2")) {
    	  wp.colSet(llCnt, "block_reason4", "F1");
      }	else if(wp.itemEq("ex_annou_type", "6")) {
    	  wp.colSet(llCnt, "block_reason4", "38");
      }	else if(wp.itemEq("ex_annou_type", "9")) {
    	  wp.colSet(llCnt, "block_reason4", "H1");
      }
//      wp.colSet(llCnt, "block_reason4", wp.itemStr("ex_proc_reason"));
      wp.colSet(llCnt, "imp_file", wp.itemStr("zz_file_name"));
      wp.colSet(llCnt, "bank_name", "合作金庫商業銀行");
      wp.colSet(llCnt, "crt_user", wp.loginUser);
      selectCount(lsIdNo.trim());
      if (sqlNum("db_cnt") > 1) {
        wp.colSet(llCnt, "id_code_rows", sqlStr("db_cnt"));
      } else {
        wp.colSet(llCnt, "id_code_rows", "0");
      }


      llCnt++;
      int rr = llCnt - 1;
      this.setRowNum(rr, llCnt);
    }
    wp.colSet("tl_cnt", llCnt);
    wp.listCount[0] = llCnt;
    // wp.setListCount(1);
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
  }

  void selectChiName(String lsIdNo) {
    String sql1 = "select chi_name  " + " from crd_idno " + " where id_no =? ";
    sqlSelect(sql1, new Object[] {lsIdNo});
    if (sqlRowNum <= 0)
      sqlSet(0, "chi_name", "");
  }

  void selectCorpName(String lsIdNo) {
    String sql1 = "select chi_name" + " from crd_corp " + " where corp_no =? ";
    sqlSelect(sql1, new Object[] {lsIdNo});
    if (sqlRowNum <= 0)
      sqlSet(0, "chi_name", "");
  }

  void selectCount(String lsIdNo) {
    wp.logSql = false;
    String sql1 = "select count(*) as db_cnt " + " from crd_idno " + " where id_no =? ";
    sqlSelect(sql1, new Object[] {lsIdNo});
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
    int llOk = 0, llErr = 0;
    // int ii=0;
    rskm05.Rskp2110Func func = new rskm05.Rskp2110Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    String[] aaCrtDate = wp.itemBuff("crt_date");
    wp.listCount[0] = wp.itemRows("crt_date");
    optNumKeep(wp.listCount[0], aaOpt);

    for (int ii = 0; ii < aaCrtDate.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;


      String lsType = wp.colStr(rr, "annou_type");
      func.varsSet("proc_row", "" + rr);
      int liRc = func.dbInsert();
      if (liRc == 1) {
        llOk++;
        // opt_okflag(rr,1);
      } else {
        wp.colSet(llErr, "crt_date", wp.colStr(rr, "crt_date"));
        wp.colSet(llErr, "id_no", wp.colStr(rr, "id_no"));
        wp.colSet(llErr, "corp_no", wp.colStr(rr, "corp_no"));
        wp.colSet(llErr, "tt_from_type", wp.colStr(rr, "tt_from_type"));
        wp.colSet(llErr, "from_type", wp.colStr(rr, "from_type"));
        wp.colSet(llErr, "chi_name", wp.colStr(rr, "chi_name"));
        wp.colSet(llErr, "imp_file", wp.colStr(rr, "imp_file"));
        wp.colSet(llErr, "annou_type", wp.colStr(rr, "annou_type"));
        wp.colSet(llErr, "tt_annou_type", wp.colStr(rr, "tt_annou_type"));
        wp.colSet(llErr, "stop_reason", wp.colStr(rr, "stop_reason"));
        wp.colSet(llErr, "stop_date", wp.colStr(rr, "stop_date"));
        wp.colSet(llErr, "bank_no", wp.colStr(rr, "bank_no"));
        wp.colSet(llErr, "bank_name", wp.colStr(rr, "bank_name"));
        wp.colSet(llErr, "crt_user", wp.colStr(rr, "crt_user"));
        wp.colSet(llErr, "block_reason4", wp.colStr(rr, "block_reason4"));
        wp.colSet(llErr, "id_code_rows", wp.colStr(rr, "id_code_rows"));
        wp.colSet(llErr, "db_err_text", func.getMsg());
        optOkflag(llErr, -1);
        llErr++;
      }
    }
    if (llOk > 0) {
      sqlCommit(1);
    }
    wp.listCount[0] = llErr;
    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);

  }

  @Override
  public void procFunc() throws Exception {
    wp.listCount[0] = wp.itemRows("crt_date");
    if (empty(wp.itemStr("ex_from_type"))) {
      alertErr2("請選擇來源管道");
      return;
    }

    if (eqIgno(wp.itemStr("ex_annou_type"), "A")) {
      ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
      rc = batch.callBatch("RskP560");
      if (rc != 1) {
        alertErr2("ID失效凍結處理: callbatch 失敗");
      } else {
        alertMsg("ID失效凍結處理中...; 處理結果請至callBatch查詢");
      }
    } else {
      ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
      rc = batch.callBatch("RskP550 " + wp.colStr("ex_from_type"));
      if (rc != 1) {
        alertErr2("不良記錄凍結處理: callbatch 失敗");
      } else {
        alertMsg("不良記錄凍結處理中...; 處理結果請至callBatch查詢");
      }
    }

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
