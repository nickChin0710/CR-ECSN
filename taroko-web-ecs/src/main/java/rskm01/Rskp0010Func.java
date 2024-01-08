package rskm01;
/**
 * 2019-0708   JH    modify
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 消費帳單查詢及處理{列問交}
 */


import busi.FuncProc;

public class Rskp0010Func extends FuncProc {
busi.CommBusi zzcomm = new busi.CommBusi();

//String is_ctrl_seqno="";
String isReferNo = "";

@Override
public int querySelect() {
   if (wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
      errmsg("[帳戶帳號, 卡號] 不可同時空白");
      return -1;
   }

   String lsKey = "";

   if (wp.itemEmpty("ex_card_no")) {
	   lsKey = wp.itemStr("ex_acct_key");
      if (lsKey.length() != 8 && lsKey.length() < 10) {
         errmsg("帳戶帳號: 輸入錯誤");
         return rc;
      }

      strSql = "select p_seqno from act_acno"
            + " where acct_key like ?"
            + " and acct_type = ?";
      setString(lsKey + "%");
      setString(wp.itemNvl("ex_acct_type", "01"));
   }
   else {
	  lsKey = wp.itemStr("ex_card_no");
      strSql = "select p_seqno from crd_card"
            + " where card_no =?";
      setString(lsKey);
   }
   //-check query_auth-
//	busi.func.Ecs_comm auth = new busi.func.Ecs_comm(conn);
//	if (auth.auth_query(wp.mod_pgm(),ls_key,wp.loginUser)==false) {
//		errmsg(auth.getMsg());
//		return -1;
//	}

   sqlSelect(strSql);
   if (sqlRowNum <= 0 || colEmpty("p_seqno")) {
      errmsg("[帳戶帳號, 卡號] 輸入錯誤, 查無帳戶流水號[p_seqno], kk[%s]", lsKey);
      return -1;
   }
   String lsPSeqNo = this.colStr("p_seqno");
   sqlWhere = " where 1=1 " + commSqlStr.col(lsPSeqNo, "A.p_seqno");
   wp.colSet("ex_p_seqno", lsPSeqNo);

   //-帳務年月-
   String lsAcctMm = wp.itemStr("ex_acct_month");
   String lsNextMm = "";
   strSql = "select b.stmt_cycle, b.next_acct_month, A.acct_status"
         + " from act_acno a, ptr_workday B"
         + " where A.stmt_cycle = B.stmt_cycle"
         + " and A.p_seqno = ? and acno_flag<>'Y'";
   sqlSelect(strSql,new Object[] {lsPSeqNo});
   if (sqlRowNum <= 0) {
      errmsg("查無卡人之關帳周期[stmt_cycle]");
      return -1;
   }
   lsNextMm = colStr("next_acct_month");
   wp.colSet("ex_acct_status", colStr("acct_status"));

   if (wp.itemEq("ex_bill", "1")) {
      sqlWhere += commSqlStr.col(lsAcctMm, "A.acct_month");
   }
   else {
      sqlWhere += commSqlStr.col(lsNextMm, "A.acct_month", ">=") //" and A.acct_month >= '"+ls_next_mm+"'"
            + " and A.billed_date =''";
   }

   sqlWhere += " and A.txn_code not in ('65','66','67','69','85','86','87','89',"
         + "'CD','DF','HC','IF','LF','LP','LS',"
         + "'RB','RR','AF','AI','BF','CF','TX','VF','VP','VR','VT')"
         + " and ( (A.rsk_type<>'' and A.bill_type<>'FIFC')"
         + " or (A.rsk_type='4' and A.bill_type='FIFC') or A.rsk_type ='' )"
   ;
   if (wp.itemEmpty("ex_curr_code") == false) {
      sqlWhere += "" //wp.sqlID+"uf_nvl(A.curr_code,'901') ='"+wp.itemStr("ex_curr_code")+"'"
            + commSqlStr.col(wp.itemStr("ex_curr_code"), "uf_nvl(A.curr_code,'901')")
      ;
   }

   sqlWhere += commSqlStr.col(wp.itemStr("ex_contr_no"), "A.contract_no");

   return 1;
}

@Override
public int dataSelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public void dataCheck() {
   // TODO Auto-generated method stub

}

void getOriRefno() throws Exception  {
   daoTid = "bill-";
   strSql = "select A.bill_type, A.contract_no"
         + ", A.merge_flag, A.txn_code"
         + ", A.install_curr_term as inst_curr_term"
         + ", B.reference_no as reference_no_ori"
         + " from bil_bill A left join bil_contract B on B.contract_no=A.contract_no"
         + " where A.reference_no =?" + commSqlStr.rownum(1);
   setString(isReferNo);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("查無帳單資料, ref-no=" + isReferNo);
      return;
   }

   String ls_refno = colStr("bill-reference_no_ori");
   String ls_contr_no = colStr("bill-contract_no");
   wp.colSet("reference_no_ori", ls_refno);
   wp.colSet("contract_no", ls_contr_no);

   return;
}

public int reptSelect() throws Exception  {
	isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   getOriRefno();
   if (rc != 1)
      return rc;
   if (eqAny(colStr("bill-bill_type"), zzcomm.TSCC_bill_type)) {
      errmsg("悠遊卡自動加值 不可調單");
      return -1;
   }
   if (colEmpty("bill-contract_no") == false && colInt("bill-inst_curr_term") != 1) {
      errmsg("不是帳單分期-首期 不可調單");
      return -1;
   }
   if (selectCheck() != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("bill-reference_no_ori"));
   wp.colSet("contract_no", colStr("bill-contract_no"));
   return rc;
}

public int chgbSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   getOriRefno();
   if (rc != 1)
      return rc;
//	if (eqAny(colStr("bill-bill_type"),zzcomm.TSCC_bill_type)) {
//		errmsg("悠遊卡自動加值 不可調單");
//		return -1;
//	}
   //-jh-191024-
//	if (colEmpty("bill-contract_no")==false && colInt("bill-inst_curr_term")!=1) {
//		errmsg("不是帳單分期-首期 不可扣款");
//		return -1;
//	}
   if (selectCheck() != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("bill-reference_no_ori"));
   wp.colSet("contract_no", colStr("bill-contract_no"));
   return rc;
}

//public int arbit_Select() throws Exception  {
//   isReferNo = varsStr("reference_no");
//   if (empty(isReferNo)) {
//      errmsg("帳單參考號: 不可空白");
//      return -1;
//   }
//   get_Ori_refno();
//   if (rc != 1)
//      return rc;
//
//   return rc;
//}

public int complSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   getOriRefno();
   if (rc != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("bill-reference_no_ori"));
   wp.colSet("contract_no", colStr("bill-contract_no"));
   return rc;
}

int selectCheck() {
   if (colEq("bill-merge_flag", "Y")) {
      errmsg("合併帳戶之帳單, 不可[調單/扣款/列問交]");
      return -1;
   }
   String ss = colStr("bill-bill_type").substring(0, 2);
   if (pos("|OK|OS|I1|I2", ss) > 0) {
      errmsg("請選擇正確的帳單類別碼！！");
      return 0;
   }
   if (colEq("bill-bill_type", "NCFC")) {
      errmsg("國外手續費: 不可 列問交,調單,扣款");
      return -1;
   }

   ss = colStr("bill-txn_code");
   if (pos("06|25|27|28|29|RI", ss) > 0) {
      errmsg("請選擇正確的交易碼！！" + ss);
      return 0;
   }

   return 1;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

public int batchProblem(String a_ref_no) throws Exception  {
   msgOK();

   if (rc != 1) {
      return rc;
   }

   rskm01.RskProblem ooprbl = new rskm01.RskProblem();
   ooprbl.setConn(wp);

   if (ooprbl.rskp0010Batch(a_ref_no) != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }
   varsSet("ctrl_seqno", ooprbl.isCtrlSeqno);
   //is_ctrl_seqno =ooprbl.is_ctrl_seqno;

   //-update bil_bill-
   strSql = "update bil_bill set"
         + " rsk_post =decode(rsk_post,'','O',rsk_post)"
         + ", rsk_ctrl_seqno =?  "
         + " where reference_no =?"
   ;
   setString(ooprbl.isCtrlSeqno);
   setString(a_ref_no);
   this.sqlExec(strSql);
   if (sqlRowNum != 1) {
      errmsg("update bil_bill error; kk[%s]", a_ref_no);
      return rc;
   }

   return 1;
}

public int batchChgback(String a_ref_no) throws Exception  {
   msgOK();

   rskm01.RskChgback oochgb = new rskm01.RskChgback();
   oochgb.setConn(wp);
   if (oochgb.rskp0010Batch(a_ref_no) != 1) {
      errmsg(oochgb.getMsg());
      return rc;
   }
   varsSet("ctrl_seqno", oochgb.isCtrlSeqno);

   //-update bil_bill-
   strSql = "update bil_bill set"
         + " rsk_post =decode(rsk_post,'','O',rsk_post)"
         + ", rsk_ctrl_seqno =?  "
         + " where reference_no =?"
   ;
   setString(oochgb.isCtrlSeqno);
   setString(a_ref_no);
   this.sqlExec(strSql);
   if (sqlRowNum != 1) {
      errmsg("update bil_bill error; kk[%s]", a_ref_no);
      return rc;
   }

   return 1;
}
//
//void get_Ctrl_seqno(String a_refno) {
//   Rsk_ctrlseqno_log lo_log=new Rsk_ctrlseqno_log();
//   lo_log.setConn(wp);
//
//   is_ctrl_seqno =lo_log.get_Ctrl_seqno(a_refno);
//   if (no_empty(is_ctrl_seqno))
//      return;
//
//   errmsg("無法取得: 控制流水號");
//   return;
//}

}
