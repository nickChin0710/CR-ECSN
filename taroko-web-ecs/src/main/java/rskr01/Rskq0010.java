package rskr01;
/**
 * 2020-0210   Alex  dataRead add
 * 2019-1211   JH    UAT
 * 2019-1202   JH    UAT-modify
 * 2019-0908   JH    --ctrlseqno_log
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;

public class Rskq0010 extends BaseAction {
String kk1 = "", kk2 = "";

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc();
         break;
      case "Q": //查詢功能
         queryFunc();
         break;
      case "R": // -資料讀取-
         dataRead();
         break;
      case "A": //新增功能
         saveFunc();
         break;
      case "U": //更新功能
         saveFunc();
         break;
      case "D": //刪除功能
         saveFunc();
         break;
      case "M": //瀏覽功能 :skip-page-
         queryRead();
         break;
      case "S": //動態查詢--
         querySelect();
         break;
      case "L": //清畫面--
         strAction = "";
         clearFunc();
         break;
      case "C": // -資料處理-
         procFunc();
         break;
//      case "UPLOAD":
//         procFunc();
//         break;
//      case "XLS":  //-Excel-
//         is_action = "XLS";
////			xlsPrint();
//         break;
//      case "PDF": //-PDF-
//         is_action = "PDF";
//         pdfPrint();
//         break;
   }
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskq0010")) {
         wp.optionKey = wp.colStr(0, "ex_acct_type");
         dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("問交新增日期 : 起迄錯誤 !");
      return;
   }

   if (itemallEmpty("ex_acct_key,ex_card_no,ex_ctrl_seqno,ex_acct_month,ex_date1,ex_date2,ex_v_card_no")) {
      alertErr("篩選條件不可全部空白");
      return;
   }

   String lsWhere = " where A.rsk_ctrl_seqno<>'' "
         + sqlCol(wp.itemStr("ex_acct_month"), "A.purchase_date", "like%")
         + sqlCol(wp.itemStr("ex_acct_type"), "A.acct_type")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "A.rsk_ctrl_seqno", "like%")
         + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "B.add_date")
         + sqlCol(wp.itemStr("ex_v_card_no"),"A.v_card_no","like%")
         ;

   if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "A.card_no");
   }
   else if (!empty(wp.itemStr("ex_acct_key"))) {
      String lsAcctKey = "";
      lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
//      lsAcctKey =get_acct_key(wp.itemStr("ex_acct_key"),wp.itemStr("ex_acct_type"));
      if (lsAcctKey.length() != 11) {
         alertErr("帳戶帳號: 輸入錯誤");
         return;
      }
      lsWhere += " and A.p_seqno in (select p_seqno from act_acno where 1=1 "
            + sqlCol(lsAcctKey, "acct_key")
            + sqlCol(wp.itemNvl("ex_acct_type", "01"), "acct_type")
            + " ) "
      ;
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " A.rsk_ctrl_seqno, 'N' as debit_flag, A.reference_no,"
         + " A.bin_type , "
         + " A.card_no , "
         + " A.purchase_date , "
         + " A.curr_code , "
         + " A.dc_dest_amt , "
         + " A.source_curr , A.source_amt,"
         + " A.mcht_country , "
         + " A.payment_type , "
         + " A.txn_code,"
         + " B.prb_status as prbl_status, B.add_date as prbl_add_date, "
         +" decode(B.clo_result,'','','-'||B.clo_result) as prbl_clo_result,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_name"
         + ", '' as chgb_status, '' as rept_status , "
         + " A.v_card_no "
   ;
   wp.daoTable = " bil_bill A left join rsk_problem B" +
         " on B.reference_no=A.reference_no and B.reference_seq=0";

   wp.whereOrder = " order by A.rsk_ctrl_seqno ";
//   sql_ddd();
   pageQuery();
   wp.setListCount(0);

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

   query_After();
}

void query_After() throws Exception {
   String sql1 = "select chgb_stage1||chgb_stage2 as chgb_status, rept_status " +
         ", compl_mark as compl_status, arbit_mark as arbit_status" +
         " from vrsk_ctrlseqno_bil" +
         " where reference_no =?";

   for (int ll = 0; ll < wp.listCount[0]; ll++) {
      if (wp.colEmpty(ll, "rsk_ctrl_seqno")) continue;
      String ls_refno = wp.colStr(ll, "reference_no");
      sqlSelect(sql1, ls_refno);
      if (sqlRowNum <= 0) continue;

      sql2wp(ll, "chgb_status");
      sql2wp(ll, "rept_status");
      sql2wp(ll, "compl_status");
      sql2wp(ll, "arbit_status");
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   rskr01.Rskq0010Func func = new rskr01.Rskq0010Func();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("ctrl_seqno", kk2);
   func.readData();
   wp.listCount[0] = (func.wpRr + 1);
   if (func.wpRr < 0) alertMsg("查無資料");

   //--說明 > 中文
   String sql1 = " select wf_desc from ptr_sys_idtab "
         + " where wf_type ='RSK-STATUS-DESC' "
         + " and wf_id = ? ";
   String lsLastDate = "", lsLastRemark = "";
   for (int ii = 0; ii < wp.listCount[0]; ii++) {
      setSerNum(ii);
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "ex_s5")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "ex_s5", sqlStr("wf_desc"));
      }
      if (ii == 0) {
         lsLastDate = wp.colStr(ii, "ex_s3");
         lsLastRemark = wp.colStr(ii, "ex_s5");
      }
      else {
         if (chkStrend(lsLastDate, wp.colStr(ii, "ex_s3")) == true) {
            lsLastDate = wp.colStr(ii, "ex_s3");
            lsLastRemark = wp.colStr(ii, "ex_s5");
         }
      }
   }
   wp.colSet("ex_last_date", lsLastDate);
   wp.colSet("ex_last_remark", lsLastRemark);
   //--
   rskm01.BilBill bill = new rskm01.BilBill();
   bill.setConn(wp);

   bill.varsSet("reference_no", kk1);
   bill.varsSet("debit_flag", "N");
   if (bill.dataSelect() == 1) {
      wp.actionCode = "";
   }
   else {
      wp.notFound = "Y";
      alertErr(bill.getMsg());
      return;
   }
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
