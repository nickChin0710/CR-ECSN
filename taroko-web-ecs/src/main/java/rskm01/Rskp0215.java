package rskm01;
/**
 * 2020-0116   Alex  add cond
 * 2019-1023   JH    modify
 */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskp0215 extends BaseProc {
Rskp0215Func func;
int il_ok = 0;
int il_err = 0;
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
//				is_action="new";
//				clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      //-資料處理-
      dataProcess();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
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
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date1")) == false) {
      alertErr("登錄日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
//   		+ "substr(bill_type,1,2) in ('NC','OI')" +
         + " and chg_stage in ('1','3') and fst_reverse_mark ='P' and final_close ='' "
         + sqlCol(wp.itemStr("ex_add_user"), "fst_add_user", "like%")
         + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
         + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")         
         + sqlCol(wp.itemStr("ex_fst_reverse_date"), "fst_reverse_date", ">=")
   ;


   switch (wp.colStr("ex_debit")) {
      case "1":
         wp.whereStr += " and debit_flag <>'Y'";
         break;
      case "2":
         wp.whereStr += " and debit_flag ='Y'";
         break;
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "card_no ,"
         + " purchase_date,"
         + " ctrl_seqno,"
         + " chg_times,"
         + " fst_reason_code,"
         + " fst_doc_mark,"
         + " fst_amount,"
         + " fst_msg,"
         + " fst_add_date,"
         + " fst_add_user,"
         + " reference_no,"
         + " reference_seq,"
         + " fst_status,"
         + " fst_apr_date,"
         + " fst_apr_user,"
         + " sub_stage,"
         + " chg_stage,"
         + " bin_type,"
         + " source_amt,"
         + " source_curr,"
         + " debit_flag,"
         + " uf_nvl(curr_code,'901') as curr_code ,"
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt, "
         + " fst_twd_amt,"
         + " v_card_no "
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by fst_add_date ASC ";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
   }

   wp.setPageValue();
//   set_canAppr("fst_add_user");
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   rskm01.RskChgback func = new rskm01.RskChgback();
   func.setConn(wp);
   func.varsSet("reference_no", kk1);
   func.varsSet("reference_seq", kk2);
   rc = func.dataSelect();
   if (rc != 1) {
      alertErr(func.getMsg());
      wp.actionCode = "";
      return;
   }

   //--
   BilBill ooBill = new BilBill();
   ooBill.setConn(wp);
   ooBill.varsSet("reference_no", kk1);
   ooBill.varsSet("bin_type", kk3);
   ooBill.varsSet("debit_flag", wp.colStr("CB_debit_flag"));
   ooBill.dataSelect();

   wp.actionCode = "";
}

@Override
public void dataProcess() throws Exception {
   func = new Rskp0215Func();
   func.setConn(wp);

   String[] lsRefno = wp.itemBuff("reference_no");
   String[] lsRefSeq = wp.itemBuff("reference_seq");
   String[] lsCt = wp.itemBuff("chg_times");
   String[] opt = wp.itemBuff("opt");
   wp.listCount[0] = lsRefno.length;

   int rr = -1;
   rr = optToIndex(opt[0]);
   if (rr < 0) {
      alertErr("請點選欲覆核資料");
      return;
   }

   for (int ii = 0; ii < opt.length; ii++) {
      rr = optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "X");
      if (!apprBankUnit(wp.itemStr(rr,"fst_add_user"),wp.loginUser)) {
         il_err++;
         break;
      }

      func.varsSet("chg_times", lsCt[rr]);
      func.varsSet("reference_no", lsRefno[rr]);
      func.varsSet("reference_seq", lsRefSeq[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         il_ok++;
         continue;
      }
      il_err++;
   }

   alertMsg("覆核處理: 成功筆數=" + il_ok + "; 失敗筆數=" + il_err);
}

@Override
public void initPage() {
	if(eqIgno(wp.respHtml,"rskp0215")) {
		wp.colSet("ex_fst_reverse_date", getSysDate());
	}
}

}
