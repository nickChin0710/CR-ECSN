package rskm02;
/**
 * 2020-0205    Alex    copyAfter dataRead
 * 2019-1206    Alex    add initButton
 * 2019-1007    JH      query-list
 */

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Rskm1220 extends BaseAction {
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void userAction() throws Exception {
//	wp.pgm_version("V.18-0814");

   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      detlRead2();
   }
   else if (eqIgno(wp.buttonCode, "S3")) {
      /* 動態查詢 */
      dspDetailData();
   }
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "C2")) {
      // -資料異動複製-
      dataProc();
   }
   else if (eqIgno(wp.buttonCode, "C3")) {
      // -資料異動複製-
      dataApr();
   }
   else if (eqIgno(wp.buttonCode, "U2")) {
      // -新增-明細資料-
      strAction = "U2";
      insertDetl();
   }
   else if (eqIgno(wp.buttonCode, "D2")) {
      // -刪除-明細資料-
      strAction = "D2";
      deleteDel();
   }
   else if (eqIgno(wp.buttonCode, "R2")) {
      // -讀取-明細資料-
      strAction = "R2";
      detlRead2();
   }

}

@Override
public void dddwSelect() {

}

@Override
public void queryFunc() throws Exception {
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_risk_group"), "risk_group")
         + sqlCol(wp.itemStr("ex_crt_user"), "crt_user", "like%")
         + sqlCol(wp.itemStr("ex_crt_date"), "crt_date", ">=");

   if (!wp.itemEq("ex_apr_flag", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "risk_group,"
         + " acct_pay_type,"
         + " branch_exchg_type," 
         + " corp_jcic_send, "
         + " idno_jcic_send, "
         + " crt_user,"
         + " crt_date,"
         + " apr_date,"
         + " apr_user,"
         + " apr_flag,"
         + " risk_group_desc";
   wp.daoTable = "rsk_trcorp_parm ";
   wp.whereOrder = " order by risk_group , apr_flag  ";
   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();
   querryAfter();
}

public void querryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "acct_pay_type", "0")) {
         wp.colSet(ii, "tt_acct_pay_type", "全部卡戶");
      }
      else if (wp.colEq(ii, "acct_pay_type", "1")) {
         wp.colSet(ii, "tt_acct_pay_type", "總繳戶");
      }
      else if (wp.colEq(ii, "acct_pay_type", "2")) {
         wp.colSet(ii, "tt_acct_pay_type", "個繳戶");
      }

      if (wp.colEq(ii, "branch_exchg_type", "0")) {
         wp.colSet(ii, "tt_branch_exchg_type", "全部");
      }
      else if (wp.colEq(ii, "branch_exchg_type", "1")) {
         wp.colSet(ii, "tt_branch_exchg_type", "授信戶");
      }
      else if (wp.colEq(ii, "branch_exchg_type", "2")) {
         wp.colSet(ii, "tt_branch_exchg_type", "國外匯兌/存款戶");
      }
      else if (wp.colEq(ii, "branch_exchg_type", "3")) {
         wp.colSet(ii, "tt_branch_exchg_type", "無往來戶");
      }

   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk3 = wp.itemStr("data_k2");
   dataRead();

}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) kk1 = itemkk("risk_group");
   if (empty(kk3)) kk3 = wp.itemStr("apr_flag");
   wp.selectSQL = "A.*,"
         + " hex(A.rowid) as rowid,"
         + " substrb(A.corp_cr_abnor_val,1,1) as db_corp_cr_abnor_a,"
         + " substrb(A.corp_cr_abnor_val,2,1) as db_corp_cr_abnor_b,"
         + " substrb(A.corp_cr_abnor_val,3,1) as db_corp_cr_abnor_c,"
         + " substrb(A.corp_cr_abnor_val,4,1) as db_corp_cr_abnor_d,"
         + " substrb(A.corp_cr_abnor_val,5,1) as db_corp_cr_abnor_n,"
         + " substrb(A.corp_add_note_val,1,1) as db_corp_add_note_a,"
         + " substrb(A.corp_add_note_val,2,1) as db_corp_add_note_b,"
         + " substrb(A.corp_add_note_val,3,1) as db_corp_add_note_c,"
         + " substrb(A.idno_cr_abnor_val,1,1) as db_idno_cr_abnor_a,"
         + " substrb(A.idno_cr_abnor_val,2,1) as db_idno_cr_abnor_b,"
         + " substrb(A.idno_cr_abnor_val,3,1) as db_idno_cr_abnor_c,"
         + " substrb(A.idno_cr_abnor_val,4,1) as db_idno_cr_abnor_n,"
         + " to_char(A.mod_time,'yyyymmdd') as mod_date";
   wp.daoTable = "rsk_trcorp_parm A";
   wp.whereStr =
         " where 1=1 " + sqlCol(kk1, "A.risk_group") + sqlCol(kk3, "A.apr_flag");
   this.logSql();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
   dspDetailData();
}

/*
 * void dataRead2(){ if(empty(kk1)) kk1=item_kk("risk_group");
 *
 * wp.selectSQL = "A.*," + " hex(A.rowid) as rowid," +
 * " substrb(A.corp_cr_abnor_val,1,1) as db_corp_cr_abnor_a," +
 * " substrb(A.corp_cr_abnor_val,2,1) as db_corp_cr_abnor_b," +
 * " substrb(A.corp_cr_abnor_val,3,1) as db_corp_cr_abnor_c," +
 * " substrb(A.corp_cr_abnor_val,4,1) as db_corp_cr_abnor_d," +
 * " substrb(A.corp_cr_abnor_val,5,1) as db_corp_cr_abnor_n," +
 * " substrb(A.corp_add_note_val,1,1) as db_corp_add_note_a," +
 * " substrb(A.corp_add_note_val,2,1) as db_corp_add_note_b," +
 * " substrb(A.corp_add_note_val,3,1) as db_corp_add_note_c," +
 * " substrb(A.idno_cr_abnor_val,1,1) as db_idno_cr_abnor_a," +
 * " substrb(A.idno_cr_abnor_val,2,1) as db_idno_cr_abnor_b," +
 * " substrb(A.idno_cr_abnor_val,3,1) as db_idno_cr_abnor_c," +
 * " substrb(A.idno_cr_abnor_val,4,1) as db_idno_cr_abnor_n"; wp.daoTable =
 * "rsk_trcorp_parm A"; wp.whereStr = " where 1=1 and apr_flag='Y'" +
 * sqlCol(kk1, "risk_group")
 *
 * ; this.logSql(); pageSelect(); if (sqlNotFind()) { alert_err("查無資料, key=" +
 * kk1); return; } }
 */
@Override
public void saveFunc() throws Exception {
   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);

   if (isDelete() && wp.itemEq("apr_flag", "Y") && this.checkApproveZz() == false) {
      return;
   }

   rc = func.dbSave(strAction);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   this.sqlCommit(rc);
   this.saveAfter(false);

}

@Override
public void procFunc() throws Exception {

   if (wp.itemEq("apr_flag", "Y")) {
      alertErr("已覆核 不可上傳檔案");
      return;
   }

   if (itemallEmpty("zz_file_name")) {
      alertErr("上傳檔名: 不可空白");
      return;
   }

   fileDataImp();
}

void fileDataImp() throws Exception {
   TarokoFileAccess tf = new TarokoFileAccess(wp);

   // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
   //String inputFile = wp.dataRoot + "/upload/" + wp.itemStr("file_name");
   String inputFile = wp.itemStr("zz_file_name");
   // int fi = tf.openInputText(inputFile,"UTF-8");
   int fi = tf.openInputText(inputFile, "MS950");
   if (fi == -1) {
      return;
   }

   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);

   wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

   if (func.deleteData() > 0) {
      sqlCommit(1);
   }

   int llOk = 0, llCnt = 0;
   while (true) {
      String ss = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
         break;
      }
      if (ss.length() < 2) {
         continue;
      }
      // String merchantNo = readData.substring(0,15);
      // wp.ddd(ss);
      llCnt++;
      wp.itemSet("data_code", commString.midBig5(ss, 0, 8));

      if (func.insertData() == 1) {
         llOk++;
         this.sqlCommit(1);
      }
      else {
         this.sqlCommit(-1);
         wp.log(ss + ", error=" + func.getMsg());
      }
   }

   tf.closeInputText(fi);
   tf.deleteFile(inputFile);

   alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk);

   return;
}

@Override
public void initButton() {

   if (eqIgno(wp.respHtml, "rskm1220")) {
      btnModeAud("XX");
   }

   if (wp.respHtml.indexOf("_detl") > 0 ||
         eqIgno(wp.respHtml, "rskm1220_block_reason") ||
         eqIgno(wp.respHtml, "rskm1220_corp_imp") ||
         eqIgno(wp.respHtml, "rskm1220_inrate_final")) {
      btnModeAud();
      if (wp.colEq("apr_flag", "Y")) {
         this.btnUpdateOn(false);
      }
      else {
         this.buttonOff("btnCopy_off");
      }
   }
   if (eqAny("U2", strAction) || eqAny("R2", strAction)) {
      if (itemEq("apr_flag", "Y")) {
         btnUpdateOn(false);
      }
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

void detlRead2() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   if (empty(kk1))
      kk1 = wp.itemStr("risk_group");
   if (empty(kk2))
      kk2 = wp.itemStr("data_type");
   kk3 = wp.itemStr("apr_flag");
   wp.selectSQL = "data_code ";
   wp.daoTable = "RSK_TRCORP_PARMDTL";
   wp.whereStr = " where 1=1 "
         + sqlCol(kk1, "risk_group")
         + sqlCol(kk2, "data_type")
         + sqlCol(kk3, "apr_flag");
   logSql();
   pageQuery();
   if (sqlNotFind()) {
      wp.notFound = "N";
   }
   wp.setListCount(1);
   wp.colSet("IND_NUM", "" + wp.selectCnt);
}

void detlReadY() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");

   wp.selectSQL = "data_code ";
   wp.daoTable = "RSK_TRCORP_PARMDTL";
   wp.whereStr = " where 1=1 and apr_flag='Y' "
         + sqlCol(kk1, "risk_group")
         + sqlCol(kk2, "data_type");
   logSql();
   logSql();
   pageQuery();
   if (sqlNotFind()) {
      alertErr("此條件查無資料");
   }
   wp.setListCount(1);
   wp.colSet("IND_NUM", "" + wp.selectCnt);
}

void dataProc() throws Exception {
   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);

   rc = func.copyData();
   sqlCommit(rc);

   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      kk1 = wp.itemStr("risk_group");
      kk3 = "N";
      dataRead();
      alertMsg("異動處理完成");
   }

}

void dataApr()  throws Exception {
   int llOk = 0, llErr = 0;
   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);
   String[] lsRiskGroup = wp.itemBuff("risk_group");
   String[] aaOpt = wp.itemBuff("opt");
   String[] lsAprFlag = wp.itemBuff("apr_flag");
   wp.listCount[0] = wp.itemRows("risk_group");

   if (checkApproveZz() == false) {
      return;
   }

   for (int ii = 0; ii < wp.itemRows("risk_group"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false) continue;
      if (eqIgno(lsAprFlag[ii], "Y")) {
         llErr++;
         wp.colSet(ii, "ok_flag", "X");
         continue;
      }

      func.varsSet("risk_group", lsRiskGroup[ii]);
      if (func.dataApr() == 1) {
         llOk++;
         wp.colSet(ii, "ok_flag", "V");
         continue;
      }
      else {
         llErr++;
         wp.colSet(ii, "ok_flag", "X");
         continue;
      }
   }

   if (llOk > 0) sqlCommit(1);

   alertMsg("資料覆核完成 , 成功:" + llOk + " 失敗:" + llErr);

}

void dspDetailData() throws Exception  {
   int liType1 = 0, liType2 = 0, liType3 = 0;
   String lsType1 = "", lsType2 = "", lsType3 = "";
   if (empty(kk1)) kk1 = wp.itemStr("risk_group");
   if (empty(kk3)) kk3 = wp.itemStr("apr_flag");

   String sql1 = " select "
         + " data_code"
         + " from rsk_trcorp_parmdtl "
         + " where risk_group = ? "
         + " and apr_flag = ? "
         + " and data_type = '01' ";

   sqlSelect(sql1, new Object[]{kk1, kk3});

   if (sqlRowNum <= 0) wp.colSet("inrate_final_cond", "N");
   else wp.colSet("inrate_final_cond", "Y");
   liType1 = sqlRowNum;

   for (int ii = 0; ii < liType1; ii++) {
      if (ii == 0) lsType1 += sqlStr(ii, "data_code");
      else lsType1 += "," + sqlStr(ii, "data_code");

      if (ii == 9) break;
   }

   if (liType1 > 10) {
      lsType1 += "  ... 共計 " + liType1 + " 個";
      wp.colSet("inrate_final_cond", "Y");
   }

   wp.colSet("wk_inrate_final_cond", lsType1);

   String sql2 = " select "
         + " data_code "
         + " from rsk_trcorp_parmdtl "
         + " where risk_group = ? "
         + " and apr_flag = ? "
         + " and data_type = '02' ";

   sqlSelect(sql2, new Object[]{kk1, kk3});

   if (sqlRowNum <= 0) wp.colSet("corp_imp_cond", "N");
   else wp.colSet("corp_imp_cond", "Y");

   liType2 = sqlRowNum;

   for (int ii = 0; ii < liType2; ii++) {
      if (ii == 0) lsType2 += sqlStr(ii, "data_code");
      else lsType2 += "," + sqlStr(ii, "data_code");

      if (ii == 4) break;
   }

   if (liType2 > 5) {
      lsType2 += "  ... 共計 " + liType2 + " 個";
      wp.colSet("corp_imp_cond", "Y");
   }

   wp.colSet("wk_corp_imp_cond", lsType2);

   String sql3 = " select "
         + " data_code "
         + " from rsk_trcorp_parmdtl "
         + " where risk_group = ? "
         + " and apr_flag = ? "
         + " and data_type = '03' ";

   sqlSelect(sql3, new Object[]{kk1, kk3});

   if (sqlRowNum <= 0) wp.colSet("block_reason_cond", "N");
   else wp.colSet("block_reason_cond", "Y");

   liType3 = sqlRowNum;

   for (int ii = 0; ii < liType3; ii++) {
      if (ii == 0) lsType3 += sqlStr(ii, "data_code");
      else lsType3 += "," + sqlStr(ii, "data_code");

      if (ii == 9) break;
   }

   if (liType3 > 10) {
      lsType3 += "  ... 共計 " + liType3 + " 個";
      wp.colSet("block_reason_cond", "Y");
   }

   wp.colSet("wk_block_reason_cond", lsType3);

}

void insertDetl() throws Exception  {
   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);

   wp.listCount[0] = wp.itemRows("data_code");

   if (wp.itemEq("apr_flag", "Y")) {
      alertErr("已覆核 不可異動");
      return;
   }

   func.varsSet("risk_group", wp.itemStr("risk_group"));
   func.varsSet("data_type", wp.itemStr("data_type"));
   func.varsSet("data_code", wp.itemStr("ex_block"));
   func.varsSet("data_code2", "");
   func.varsSet("type_desc", wp.itemStr("type_desc"));

   rc = func.dbInsertDetl();
   sqlCommit(rc);

   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      wp.colSet("ex_block", "");
      alertMsg("新增明細成功 , 請重新讀取");
   }

}

void deleteDel() throws Exception {
   int il_cnt = 0, il_ok = 0, il_err = 0;

   Rskm1220Func func = new Rskm1220Func();
   func.setConn(wp);

   wp.listCount[0] = wp.itemRows("data_code");

   if (wp.itemEq("apr_flag", "Y")) {
      alertErr("已覆核 不可異動");
      return;
   }

   String[] aaOpt = wp.itemBuff("opt");
   String[] lsDataCode = wp.itemBuff("data_code");

   func.varsSet("risk_group", wp.itemStr("risk_group"));
   func.varsSet("data_type", wp.itemStr("data_type"));

   for (int ii = 0; ii < wp.itemRows("data_code"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false) continue;
      func.varsSet("data_code", lsDataCode[ii]);
      il_cnt++;
      rc = func.deleteDetl();
      if (rc != 1) {
         il_err++;
         wp.colSet(ii, "ok_flag", "X");
         dbRollback();
         continue;
      }
      else {
         il_ok++;
         wp.colSet(ii, "ok_flag", "V");
         sqlCommit(1);
         continue;
      }
   }

   if (il_cnt == 0) {
      errmsg("請勾選欲刪除資料");
      return;
   }


   alertMsg("刪除明細完成 成功:" + il_ok + " 失敗:" + il_err);

}

}
