package rskm01;
/**
 * 調單作業結案主管覆核
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2019-0322:  JH    modify
 * 18-1005:		JH		modify
 */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskp0120 extends BaseProc {
String kk1 = "";
int il_ok = 0;
int il_err = 0;

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
   // wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("問交結案放行日期起迄：輸入錯誤");
      return;
   }

   wp.whereStr = " where close_apr_date ='' and close_add_date<>'' and fees_flag='Y'"
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no")
         + sqlCol(wp.itemStr("ex_user"), "close_add_user")
         + sqlCol(wp.itemStr("ex_date1"),"close_add_date",">=")
         + sqlCol(wp.itemStr("ex_date2"),"close_add_date","<=")
//         + commSqlStr.strend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "close_add_date")
         ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "hex(rowid) as rowid , mod_seqno,"
         + " bin_type,"
         + " ctrl_seqno, rept_status,"
         + " rept_seqno,"
         + " card_no,"
         + " v_card_no,"
         + " reason_code,"
         + " proc_result,"
         + " recv_date,"
         + " fees_flag,"
         + " fees_amt,"
         + commSqlStr.sqlDebitFlag + ","
         + " close_add_date,"
         + " close_add_user"
   ;
   wp.daoTable = "rsk_receipt";
   wp.whereOrder = " order by ctrl_seqno ";
   pageQuery();
   wp.listCount[0] = sqlRowNum;
   if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
   }
   wp.setPageValue();
//   set_canAppr("close_add_user");
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   wp.sqlCmd = "select A.*,"
         + " uf_idno_name2(A.card_no,A.debit_flag) as db_chi_name,"
         + "hex(A.rowid) as rowid"
         + " from rsk_receipt A"
         + " where 1=1"
         + " and rowid = ? "
         ;
   setRowId(kk1);
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
}

@Override
public void dataProcess() throws Exception {
   rskm01.Rskp0120Func func = new rskm01.Rskp0120Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModseq = wp.itemBuff("mod_seqno");

   wp.listCount[0] = aaRowid.length;
   this.optNumKeep(aaRowid.length, aaOpt);
   if (optToIndex(aaOpt[0])<0) {
      alertErr(appMsg.optApprove);
      return;
   }

   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr =optToIndex(aaOpt[ii]);
      if (rr<0) continue;

      wp.colSet(rr, "ok_flag", "-");
      if (!apprBankUnit(wp.itemStr(rr,"close_add_user"),wp.loginUser)) {
         wp.colSet(rr,"ok_flag","X");
         il_err++;
         break;
      }

      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModseq[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }
      il_err++;
      wp.colSet(rr, "ok_flag", "X");
   }

   // -re-Query-
   // queryRead();
   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

}
