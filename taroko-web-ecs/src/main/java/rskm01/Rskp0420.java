package rskm01;
/** 調單/扣款/雜項費用-待傳送資料製作處理
 * 2020-0421   JH    VM.國外不送
 * 2020-0414	JH		modify
 */

import ecsfunc.EcsCallbatch;
import ofcapp.BaseAction;

public class Rskp0420 extends BaseAction {

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc(); break;
      case "Q": //查詢功能
         queryFunc(); break;
      case "R": // -資料讀取-
         dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "C": // -資料處理-
         procFunc(); break;
      case "C2": // -資料處理-
         callBatch(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("新增登錄日期: 起迄輸入錯誤");
      return;
   }

//	String ls_where = " where 1=1 and send_flag ='1' and send_apr_flag<>'Y' "
//		+ " and substr(bill_type,1,2) in ('NC','OI') and film_no<>'' ";

   //wp.whereStr = ls_where;
//   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   daoTid = "A.";
   wp.sqlCmd = "Select "
         + " send_date , "
         + " bin_type , "
         + " card_no , "
         + " rept_type , "
         + " ctrl_seqno , "
         + " film_no , "
         + " acq_member_id , "
         + " purchase_date , "
         + " source_amt , "
         + " source_curr , "
         + " payment_type , "
         + " mcht_eng_name , "
         + " mcht_city , "
         + " mcht_country , "
         + " mcht_category , "
         + " mcht_zip , "
         + " rept_seqno , "
         + " reason_code , "
         + " mcht_no , "
         + " process_date , "
         + " add_date , "
         + " dest_amt , "
         + " settl_amt , "
         + " auth_code , "
         + " v_card_no , "
         + " reference_no , "
         + " reference_no_ori , "
         + " debit_flag , "
         + " send_flag , "
         + " decode(rept_type,'1','52','51') as db_tran_code , "
         + " hex(rowid) as rowid "
         + " from rsk_receipt"
         + " where 1=1"
         + " and send_flag ='1' and send_apr_flag <>'Y'"
//         + " and substr(bill_type,1,2) in ('NC','OI') and film_no<>''"         
         + " and film_no <> '' "
         + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
         + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
         + " order by decode(mcht_country,'TW','',mcht_country), decode(bin_type,'J',1,'M',2,'V',3,9)"
   ;

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      wp.notFound = "N";
   }
   queryOpt1();
   wp.setPageValue();
   queryRead2();
   //queryRead3();
   wp.colSet("t1_rows", "-" + wp.listCount[0]);
   wp.colSet("t2_rows", "-" + wp.listCount[1]);
}

void queryOpt1() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "A.send_flag", "1")) wp.colSet(ii, "A.opt_chk", "checked");
   }
}

void queryRead2() throws Exception {
   daoTid = "B.";
   wp.sqlCmd = " select "
         + " fst_send_date ,"
         + " bin_type ,"
         + " ctrl_seqno ,"
         + " card_no ,"
         + " txn_code ,"
         + " chg_times ,"
         + " dest_amt ,"
         + " dest_curr ,"
         + " source_amt ,"
         + " source_curr ,"
         + " fst_amount ,"
         + " fst_twd_amt ,"
         + " purchase_date ,"
         + " settl_amt ,"
         + " film_no ,"
         + " acq_member_id ,"
         + " fst_reverse_mark ,"
         + " fst_rebuild_mark ,"
         + " fst_usage_code ,"
         + " fst_part_mark ,"
         + " payment_type ,"
         + " mcht_eng_name ,"
         + " mcht_city ,"
         + " mcht_country ,"
         + " mcht_category ,"
         + " mcht_zip ,"
         + " mcht_state ,"
         + " mcht_no ,"
         + " fst_msg ,"
         + " fst_doc_mark ,"
         + " fst_add_date ,"
         + " auth_code ,"
         + " dc_dest_amt ,"
         + " fst_dc_amt ,"
         + " curr_code ,"
         + " v_card_no ,"
         + " send_flag ,"
         + " hex(rowid) as rowid "
         + " from rsk_chgback "
         + " where 1=1 "
         + " and send_flag ='1' and fst_apr_date<>''"
         + " and send_apr_flag <>'Y'"
//         + " and substr(bill_type,1,2) in ('NC','OI') "
         + " and film_no <>''"
         +" and ( (bin_type in ('V','M') and mcht_country in ('','TW','TWN')) "  //-VM:國內交易-
         +" or bin_type not in ('V','M') )"
//         + commSqlStr.strend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "fst_send_date")
         + sqlCol(wp.itemStr("ex_date1"),"fst_send_date",">=")
         + sqlCol(wp.itemStr("ex_date2"),"fst_send_date","<=")
         + " order by mcht_country, decode(bin_type,'J',1,'M',2,'V',3,9) ";

   pageQuery();
   wp.setListCount(2);
   if (sqlRowNum <= 0) {
      wp.notFound = "N";
      return;
   }
   queryAfter2();
}

void queryRead3() throws Exception {
   //[雜費]取消
//	daoTid = "C.";
//	wp.sqlCmd = " select "
//		+ " bin_type ,"
//		+ " ctrl_seqno ,"
//		+ " misc_status ,"
//		+ " coll_disb ,"
//		+ " dest_amt ,"
//		+ " settl_amt ,"
//		+ " misc_amt ,"
//		+ " reason_code ,"
//		+ " event_date ,"
//		+ " country_code ,"
//		+ " acqu_bin ,"
//		+ " card_no ,"
//		+ " purchase_date ,"
//		+ " source_bin ,"
//		+ " misc_msg ,"
//		+ " decode(coll_disb,'C','10','20') as db_tran_code ,"
//		+ " send_flag as opt_send ,"
//		+ " hex(rowid) as rowid "
//		+ " from rsk_miscfee2 "
//		+ " where 1=1 "
//		+ " and send_flag ='1' "
//		+ " and send_apr_flag<>'Y' "
//		+ " order by decode(country_code,'TW','',country_code), decode(bin_type,'J',1,'M',2,'V',3,9) ";
//	pageQuery();
//	if (sqlRowNum <= 0) {
//		wp.notFound = "N";
//		return;
//	}
//	wp.setListCount(3);
}

void queryAfter2() {
   int ll_select = wp.listCount[1];

   for (int ii = 0; ii < ll_select; ii++) {
      if (eqIgno(wp.colStr(ii, "B.fst_reverse_mark"), "R")) {
         if (eqIgno(wp.colStr(ii, "B.txn_code"), "05"))
            wp.colSet(ii, "B.db_tran_code", "35");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "06"))
            wp.colSet(ii, "B.db_tran_code", "36");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "07"))
            wp.colSet(ii, "B.db_tran_code", "37");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "0A"))
            wp.colSet(ii, "B.db_tran_code", "3A");
         else
            wp.colSet(ii, "B.db_tran_code", wp.colStr(ii, "B.txn_code"));
      }
      else {
         if (eqIgno(wp.colStr(ii, "B.txn_code"), "05"))
            wp.colSet(ii, "B.db_tran_code", "15");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "06"))
            wp.colSet(ii, "B.db_tran_code", "16");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "07"))
            wp.colSet(ii, "B.db_tran_code", "17");
         else if (eqIgno(wp.colStr(ii, "B.txn_code"), "0A"))
            wp.colSet(ii, "B.db_tran_code", "1A");
         else
            wp.colSet(ii, "B.db_tran_code", wp.colStr(ii, "B.txn_code"));
      }
      if (wp.colEq(ii, "B.send_flag", "1")) wp.colSet(ii, "B.opt_chk", "checked");
   }
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
   int llOk = 0, llErr = 0;
   int llNoSendCnt = 0;
   String ls_opt_send = "";
   rskm01.Rskp0420Func func = new rskm01.Rskp0420Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("A.opt");
   String[] aaOptSend = wp.itemBuff("A.opt_send");
   String[] aaRowid = wp.itemBuff("A.rowid");
   String[] bbOpt = wp.itemBuff("B.opt");
   String[] bbOpt_send = wp.itemBuff("B.opt_send");
   String[] bbRowid = wp.itemBuff("B.rowid");
   wp.listCount[0] = wp.itemRows("A.rowid");
   wp.listCount[1] = wp.itemRows("B.rowid");

//--opt keep	
   optNumKeep(wp.itemRows("A.rowid"), aaOpt);
   for (int ii = 0; ii < wp.itemRows("A.rowid"); ii++) {
      if (!checkBoxOptOn(ii, aaOptSend)) {
         continue;
      }
      wp.colSet(ii, "opt_on2", "checked");
   }

   for (int ii = 0; ii < wp.itemRows("B.rowid"); ii++) {
      if (!checkBoxOptOn(ii, bbOpt)) {
         continue;
      }
      wp.colSet(ii, "opt_on3", "checked");
   }

   for (int ii = 0; ii < wp.itemRows("B.rowid"); ii++) {
      if (!checkBoxOptOn(ii, bbOpt_send)) {
         continue;
      }
      wp.colSet(ii, "opt_on4", "checked");
   }

//--	
   for (int aa = 0; aa < aaRowid.length; aa++) {
      if (!checkBoxOptOn(aa, aaOpt)) {
         continue;
      }

      if (checkBoxOptOn(aa, aaOptSend)) {
         func.varsSet("send_flag", "1");
      }
      else {
         func.varsSet("send_flag", "0");
      }

      func.varsSet("rowid", aaRowid[aa]);
      if (func.updateReceipt() == 1) {
         llOk++;
         wp.colSet(aa, "A.ok_flag", "V");
      }
      else {
         llErr++;
         wp.colSet(aa, "A.ok_flag", "X");
      }
   }

   for (int bb = 0; bb < bbRowid.length; bb++) {
      if (!checkBoxOptOn(bb, bbOpt)) {
         continue;
      }

      String ls_send = wp.itemStr("B.opt_send-" + bb);

      wp.colSet(bb, "B.opt_send", ls_send);

      if (checkBoxOptOn(bb, bbOpt_send)) {
         func.varsSet("send_flag", "1");
      }
      else {
         func.varsSet("send_flag", "0");
      }

      func.varsSet("rowid", bbRowid[bb]);

      if (func.updateChgback() == 1) {
         llOk++;
         wp.colSet(bb, "B.ok_flag", "V");
      }
      else {
         llErr++;
         wp.colSet(bb, "B.ok_flag", "X");
      }
   }

   if (llOk > 0) {
      sqlCommit(1);
   }
   alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr + " , 取消傳送=" + llNoSendCnt);

   //-Update 國外交易-
   int li_rc = func.updateChgbackNoTW(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"));
   if (li_rc == 1) {
      sqlCommit(1);
   }

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   wp.colSet("ex_date1", wp.sysDate);
}

void callBatch() throws Exception {
   EcsCallbatch batch = new EcsCallbatch(wp);
   rc = batch.callBatch("RskP430");
   if (rc != 1) {
      alertErr("callBatch error; " + batch.getMesg());
   }
   else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
   }
}

}
