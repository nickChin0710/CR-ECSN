package rskm01;
/**
 * 問題交易維護作業-主管覆核
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2019-0528:  JH    分期一次到期
 * V00.00	2017-1128	JH:
 */

import busi.FuncAction;
import busi.SqlPrepare;

public class Rskp0020Func extends FuncAction {
busi.CommBusi zzcomm = new busi.CommBusi();

private String isPrbStatus = "";
private String isCtrlSeqno = "", isBinType = "";
boolean ibDebit = false;

@Override
public void dataCheck() {
   daoTid = "A.";
   strSql = "select ctrl_seqno, bin_type, prb_status,"
         + " card_no, purchase_date, "
         + " txn_code, mcht_no, "
         + " prb_amount, "
         + " uf_dc_amt(curr_code,prb_amount,dc_prb_amount) as dc_prb_amount,"
         + " film_no, "
         + commSqlStr.sqlDebitFlag + ","
         + " reference_no, reference_seq, add_user,"
         + " p_seqno, dest_amt,"
         + " uf_dc_curr(curr_code) as curr_code, "
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt"
         + ", debit_flag"
         + " from rsk_problem "
         + " where rowid =?"
         + " and mod_seqno =?"
   ;

   this.setRowId(1, varsStr("rowid"));
   setInt(2, varsInt("mod_seqno"));
   sqlSelect(strSql);

   if (sqlRowNum <= 0) {
      errmsg("問交資料 已不存在 or 被異動");
      return;
   }

   ibDebit = zzcomm.isDebit(colStr("A.debit_flag"));

   if (colEq("A.prb_status", "10")) {
      isPrbStatus = "30";
   }
   else if (colEq("A.prb_status", "40")) {
      isPrbStatus = "50";
   }
   else {
      errmsg("問交狀態: 不是 10, 40 不可覆核");
   }
   isCtrlSeqno = colStr("A.ctrl_seqno");
   isBinType = colStr("A.bin_type");
}

@Override
public int dbInsert() {
   return 0;
}

@Override
public int dbUpdate() {
   return 0;
}

@Override
public int dbDelete() {
   return 0;
}

//-放行-
@Override
public int dataProc() {
   msgOK();
   dataCheck();
   if (rc != 1)
      return rc;

   //--
   rskm01.RskProblem ooprbl = new rskm01.RskProblem();
   ooprbl.setConn(wp);
   ooprbl.varsSet("rowid", varsStr("rowid"));
   ooprbl.varsSet("reference_no", colStr("A.reference_no"));
   ooprbl.varsSet("ctrl_seqno", colStr("A.ctrl_seqno"));
   ooprbl.varsSet("bin_type", colStr("A.bin_type"));
   ooprbl.varsSet("prb_status", isPrbStatus);

   if (ooprbl.rskp0020Approve() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   if (eqAny(isPrbStatus, "30")) {
      wfRskCtfcTxnAdd();
      if (rc != 1) {
         return rc;
      }

      //-act[dba]_acaj-
      col2wpItem("A.reference_no");
      col2wpItem("A.curr_code");
      col2wpItem("A.prb_amount");
      col2wpItem("A.dc_prb_amount");
      col2wpItem("A.ctrl_seqno");
      col2wpItem("A.bin_type");
      wp.itemSet("A.adjust_type", "DP01");
      wp.itemSet("A.prb_status", "30");

      if (ibDebit) {
         DbaAcaj ooacaj = new DbaAcaj();
         ooacaj.setConn(wp);
         if (ooacaj.rskp0020Approve() != 1) {
            errmsg(ooacaj.getMsg());
         }
      }
      else {
         ActAcaj ooacaj2 = new ActAcaj();
         ooacaj2.setConn(wp);
         if (ooacaj2.rskp0020Approve() != 1) {
            errmsg(ooacaj2.getMsg());
         }
      }
   }

   //-分期交易一次到期-
   if (colEq("A.debit_flag", "Y"))
      return rc;

   strSql = "select contract_no from bil_bill" +
         " where reference_no =? " + commSqlStr.rownum(1);
   setString(1, colStr("A.reference_no"));
   sqlSelect(strSql);
   if (sqlRowNum > 0 && !colEmpty("contract_no")) {
      strSql = "update bil_contract set" +
            " forced_post_flag ='Y'" +
            ", forced_post_from ='1'" +
            ", post_cycle_dd =0"+      //-當天nightBatch執行-
            commSqlStr.modxxxSet(modUser, modPgm)
            + " where contract_no =?"
            + " and install_tot_term <>install_curr_term"
            + " and refund_flag <>'Y'";
      setString(1, colStr("contract_no"));
      sqlExec(strSql);
      if (sqlRowNum < 0) {
         errmsg("update bil_contract error");
      }
   }

   return rc;
}

void wfRskCtfcTxnAdd() {
   if (colEmpty("A.card_no"))
      return;

   strSql = "select max(case_no) as ctfc_case_no"
         + " from rsk_ctfc_mast"
         + " where 1=1"
         + " and card_no =?";
   setString(1, colStr("A.card_no"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;
   if (colEmpty("ctfc_case_no"))
      return;

   if (colEmpty("A.ctrl_seqno"))
      return;

   strSql = "select count(*) as db_cnt"
         + " from rsk_ctfc_txn"
         + " where case_no =?"
         + " and ctrl_seqno =?";
   setString(1, colStr("ctfc_case_no"));
   setString(2, zzcomm.ctrlSeqno(isCtrlSeqno, isBinType));

   sqlSelect(strSql);
   if (sqlRowNum > 0 && colNum("db_cnt") > 0)
      return;

//			ls_txn_date =dw_data.item(ll,'purchase_date')
//			lm_txn_amt =dec(dw_data.item(ll,'prb_amount'))
//			lm_dc_txn_amt=dec(dw_data.item(ll,'dc_prb_amount'))
//			ss =dw_data.item(ll,'film_no')
//			//微縮影NO:7-431330-[3][323]000902111629 ，代表2013年第323天
//			ls_yy =mid(ss,8,1)
//			ls_ddd =mid(ss,9,3)
//			ls_ref_no =dw_data.item(ll,'reference_no')
//			li_ref_seq =integer(dw_data.item(ll,'reference_seq'))
//			ls_mod_user =dw_data.item(ll,'prb_add_id')

   String lsCtrlSeqno = zzcomm.ctrlSeqno(isCtrlSeqno, isBinType);
   String ls_yy = colStr("A.film_no").substring(7, 8);
   String ls_ddd = colStr("A.film_no").substring(8, 11);

   SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("rsk_ctfc_txn");
   spp.ppstr("case_no", colStr("ctfc_case_no"));
   spp.ppstr("ctrl_seqno", lsCtrlSeqno);
   spp.ppstr("txn_date", colStr("A.purchase_date"));
   spp.ppstr("arn_year", ls_yy);
   spp.ppstr("arn_ddd", ls_ddd);
   spp.ppnum("txn_amt", colNum("A.prb_amount"));
   spp.ppnum("dc_txn_amt", colNum("A.dc_prb_amount"));
   spp.ppstr("reference_no", colStr("A.reference_no"));
   spp.ppint("reference_seq", colInt("reference_seq"));
   spp.modxxx(modUser, modPgm);
   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("insert RSK_CTFC_TXN error");
   }
   return;
}

void cancel_check() throws Exception  {
   daoTid = "A.";
   strSql = "select ctrl_seqno, bin_type, prb_status, "
         + commSqlStr.sqlDebitFlag + ","
         + " add_apr_date"
         + " from rsk_problem "
         + " where rowid =?"
         + " and mod_seqno =?"
   ;
   setRowId(1, varsStr("rowid"));
   setInt(2, varsInt("mod_seqno"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("問交資料: 已不存在");
      return;
   }

   ibDebit = zzcomm.isDebit(colStr("A.debit_flag"));
   //-不是當天覆核-
   if (colEq("A.add_apr_date", this.getSysDate()) == false) {
      errmsg("不是當天覆核, 不可解覆核");
      return;
   }

   if (colEq("A.prb_status", "30")) {
      isPrbStatus = "10";
   }
   else if (colEq("A.prb_status", "50")) {
      isPrbStatus = "40";
   }
   else {
      errmsg("問交狀態: 不是 30, 50 不可取消覆核");
   }

   //-check調帳-
   if (colEq("A.prb_status", "30") == false)
      return;

   String lsRskSeqno = zzcomm.rskCtrlSeqnoPrbl(colStr("A.ctrl_seqno"), colStr("A.bin_type"), "30");
   if (ibDebit) {
      strSql = "select proc_flag"
            + " from dba_acaj"
            + " where rsk_ctrl_seqno =?";
   }
   else {
      strSql = "select process_flag as proc_flag"
            + " from act_acaj"
            + " where rsk_ctrl_seqno =?";
   }
   setString(1, lsRskSeqno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;

   if (colEq("proc_flag", "Y")) {
      errmsg("帳務調整己處理, 不可解覆核");
   }
}

public int cancelProc() throws Exception  {
   msgOK();
   cancel_check();
   if (rc != 1)
      return rc;

   //--
   RskProblem ooprbl = new RskProblem();
   ooprbl.setConn(wp);

   ooprbl.varsSet("rowid", varsStr("rowid"));
   //ooprbl.vars_set("ctrl_seqno",colStr("A.ctrl_seqno"));
   //ooprbl.vars_set("bin_type",colStr("A.bin_type"));
   ooprbl.varsSet("prb_status", isPrbStatus);
   ooprbl.modUser = wp.loginUser;
   ooprbl.modPgm = wp.modPgm();
   if (ooprbl.rskp0020Cancel() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   //-Delete.act_acaj-
   if (colEq("A.prb_status", "30")) {
      String lsRskSeqno = zzcomm.rskCtrlSeqnoPrbl(colStr("A.ctrl_seqno"), colStr("A.bin_type"), "30");
      if (ibDebit) {
         strSql = "delete dba_acaj"
               + " where rsk_ctrl_seqno =?";
      }
      else {
         strSql = "delete act_acaj"
               + " where rsk_ctrl_seqno =?";
      }
      setString(1, lsRskSeqno);
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
         errmsg("帳務調整: 已不存在; kk=" + lsRskSeqno);
      }
   }

   return rc;
}

}
