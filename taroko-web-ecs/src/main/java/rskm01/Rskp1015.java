package rskm01;
/**
 * 2021-0720   JH    where-cond: rsk_type<>''
 * 2021-0718   JH    from bil_bill left join rsk_problem
 * */

import ofcapp.BaseAction;

public class Rskp1015 extends BaseAction {
String kk1 = "", isReferNo = "", isCtrlSeqno = "";
taroko.base.CommDate zzdate = new taroko.base.CommDate();
int procClose = 0;
int iiCurrTerm=0;
Rskp1010 rskp1010 = new rskm01.Rskp1010();	

@Override
public void userAction() throws Exception {
	strAction = wp.buttonCode;
	rskp1010.strAction = strAction;
	rskp1010.wp = wp;
	if (eqIgno(wp.requHtml,"rskp1015")) {
		switch (wp.buttonCode) {
			case "S":
				if (wp.respHtml.indexOf("_bill") > 0) {
					rskp1010.querySelect();  //--
		         return;
		      }				
				strAction = "R";
		      //--
		      if (rskp1010.procCheckOpt() != 1) {
		         return;
		      }
		      
		      if (wp.respHtml.indexOf("_rept") > 0)
		      	rskp1010.dataReadRept();
		      else if (wp.respHtml.indexOf("_chgb") > 0)
		      	rskp1010.dataReadChgb(); 
		      break;
			case "Q": //-查詢功能-
				queryFunc(); break;
         case "M": //瀏覽功能 :skip-page--
         	queryRead(); break;
         case "L": //-清畫面-
            strAction ="";
            rskp1010.clearFunc(); break;         
		}				
	}
	
	if (wp.requHtml.indexOf("_bill") >0) {
      switch (wp.buttonCode) {
         case "R":
         	rskp1010.dataRead(); break;
      }
      return;
   }
   
	if (wp.requHtml.indexOf("_rept") >0) {
      switch (wp.buttonCode) {
         case "R":
         	rskp1010.dataReadRept(); break;
         case "A":
         case "U":
         case "D":
         	rskp1010.dbSaveReceipt(); break;
      }
      return;
   }
   if (wp.requHtml.indexOf("_chgb") >0) {
      switch (wp.buttonCode) {
         case "R":
         	rskp1010.dataReadChgb(); break;
         case "A":
         case "U":
         case "D":
         	rskp1010.dbSaveChgback(); break;
      }
      return;
   }          
	
}

@Override
public void dddwSelect() {
	// TODO Auto-generated method stub
	
}

@Override
public void queryFunc() throws Exception {
	busi.func.ColFunc func2 = new busi.func.ColFunc();
   func2.setConn(wp);
   
   if (wp.itemEmpty("ex_card_no") == false) {
      if (func2.fAuthQuery(wp.modPgm(), wp.itemStr("ex_card_no")) != 1) {
         alertErr(func2.getMsg());
         return;
      }
   }
   
   String lsKey = "";
   if (wp.itemEmpty("ex_card_no") == false) {
      lsKey = wp.itemStr("ex_card_no");
      zzVipColor(lsKey);
   }
   
   if (logQueryIdno(lsKey) == false) {
      return;
   }
   
   String lsWhere = " where 1=1 "
//         + "　and A.txn_code not in ('65','66','67','69','85','86','87','89', "
//   		+ "'CD','DF','HC','IF','LF','LP','LS','RB','RR','AF','AI','BF','CF','TX','VF','VP','VR','VT')"
//   		+ " and ( (A.rsk_type<>'' and A.bill_type<>'NCFC')  or (A.rsk_type='4' and A.bill_type='NCFC') or A.rsk_type ='' ) "
   		+ sqlCol(wp.itemStr("ex_card_no"),"A.card_no")
   		;
   
   if(wp.itemEmpty("ex_acct_month") == false) {
   	String date1 = "" , date2 = "";
   	date1 = wp.itemStr("ex_acct_month")+"01";
   	date2 = wp.itemStr("ex_acct_month")+"99";
   	lsWhere += sqlCol(date1,"A.post_date",">=")
   				+  sqlCol(date2,"A.post_date","<=");
   }
   
   if(wp.itemEq("ex_prb_mark", "E")) {
   	lsWhere += " and A.rsk_type = '1' ";
   }	else if(wp.itemEq("ex_prb_mark", "Q")) {
   	lsWhere += " and A.rsk_type in ('2','3') ";
   }	else if(wp.itemEq("ex_prb_mark", "S")) {
   	lsWhere += " and A.rsk_type = '4' ";
   }
   else {
      lsWhere +=" and A.rsk_type in ('1','2','3','4') ";
   }
   
   if(wp.itemEq("ex_prb_close", "1")) {
   	//--未結案
   	lsWhere += " and B.prb_status < '60' ";
   } else if(wp.itemEq("ex_prb_close", "2")) {
   	//--已結案
   	lsWhere += " and B.prb_status >= '60' ";
   }
   
   wp.whereStr = lsWhere ;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();
   
   //--
   setSqlParmNoClear(true);
   String ls_sql="select count(*) as wk_tot_cnt, sum(decode(A.sign_flag,'-',0 - A.dest_amt,A.dest_amt)) as wk_tot_amt"+
         " from dbb_bill A left join rsk_problem B on A.reference_no = B.reference_no "+wp.whereStr;
   sqlSelect(ls_sql);
   
   sql2wp("wk_tot_cnt");
   sql2wp("wk_tot_amt");

   queryRead();
}

@Override
public void queryRead() throws Exception {
	wp.pageControl();
	
	wp.selectSQL = ""
         + " A.acct_date , A.post_date,"
         + " A.purchase_date , "
         + " A.interest_date , "
         + " A.card_no , "
         + " A.source_amt , "
         + " A.dest_amt,"
         + " '901' as curr_code,"
         + " A.dest_amt as dc_dest_amt,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_name,"
         + " A.mcht_city,"
         + " A.auth_code,"
         + " A.txn_code,"
         + " A.bin_type,"
         + " A.payment_type,"
         + " A.cash_pay_amt,"
         + " A.reference_no, A.contract_no, "
         + " '' as merge_flag,"
         + " '' as v_card_no,"
         + " A.rsk_ctrl_seqno, A.install_curr_term, "
         + " '' as rept_mark , "
         + " '' as prbl_mark , "
         + " '' as chgb_mark , "
         + " '' as compl_mark , "
         + " '' as arbit_mark "
         +", A.reference_no_original as reference_no_ori"
         +", (SELECT reference_no FROM bil_contract WHERE contract_no = A.contract_no AND A.contract_no<>'') AS refer_no_cont"
   ;
   wp.daoTable = "dbb_bill A left join rsk_problem B on A.reference_no = B.reference_no ";
   wp.whereOrder = " order by A.purchase_date, A.card_no, A.post_date, A.install_curr_term, A.reference_no";

   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   rskp1010.queryAfter(wp.listCount[0]);

   wp.setPageValue();
	
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
	// TODO Auto-generated method stub

}

@Override
public void initButton() {
	buttonOff("");
   if (eqIgno(wp.respHtml, "rskp1010_prbl")) {
      this.btnModeAud("rskm0010", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_rept") > 0) {
      this.btnModeAud("rskm0110", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_chgb") > 0) {
      this.btnModeAud("rskm0210", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_arbit") > 0) {
      this.btnModeAud("rskm0250", wp.colStr("rowid"));
   }
   else if (wp.respHtml.indexOf("_compl") > 0) {
      this.btnModeAud("rskm0450", wp.colStr("rowid"));
      int li_status = wp.colInt("pre_status");
      if (li_status < 30)
         this.buttonOff("btnProc1_off");
      if (wp.colInt("com_status") < 30)
         this.buttonOff("btnProc2_off");
   }
   else if (eqIgno(wp.respHtml, "rskp1015")) {
      btnModeAud("XX");
   }

}

@Override
public void initPage() {
	// TODO Auto-generated method stub

}

int procCheckOpt() {
   String[] aa_refno = wp.getInBuffer("reference_no");
   // String[] aa_ctrl_seqno =wp.getInBuffer("rsk_ctrl_seqno");
   String[] opt = wp.itemBuff("opt");

   //int rr = (int) this.to_Num(opt[0]) - 1;
   int rr = this.optToIndex(opt[0]);

   if (opt == null || opt.length == 0 || rr < 0) {
      alertErr("未點選列問交資料");
      wp.notFound = "Y";
      return -1;
   }

   isReferNo = aa_refno[rr];
   isCtrlSeqno = wp.colStr(rr, "rsk_ctrl_seqno");
   iiCurrTerm =(int)wp.itemNum(rr,"install_curr_term");

   if (wp.respHtml.indexOf("_rept") > 0 ||
         wp.respHtml.indexOf("_chgb") > 0 ||
         wp.respHtml.indexOf("_arbit") > 0 ||
         wp.respHtml.indexOf("_compl") > 0 ||
         wp.buttonCode.equals("C1") ||
         wp.buttonCode.equals("C3")) {
      if (iiCurrTerm >1) {
         alertErr("分期帳單: 不是首期, 不可[調單, 扣款, 仲裁, 依從權, 整批列問交, 整批扣款]");
         return -1;
      }
   }
   return 1;
}

}
