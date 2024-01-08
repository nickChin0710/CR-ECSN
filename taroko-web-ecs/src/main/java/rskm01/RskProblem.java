package rskm01;
/**
 * rsk_problem 公共程式
 * 2019-1204:  Alex  fix vd bug
 * 2019-1127   JH    UAT-bug
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0511:	JH		reference_seq=1 for Insert
 * 2018-0327:	JH		++rskm0022
 * 2018-0131: ++VD dbc_bill, dbc_card, dba_acno
 */

import busi.FuncBase;

public class RskProblem extends FuncBase {
busi.CommBusi zzComm = new busi.CommBusi();

public String isCtrlSeqno = "";
public String isReferNo = "";
private int referSeq = 0;      //reference_seq
private boolean ibDebit = false;

int errUpdate(String strName) {
   if (this.isAdd())
      errmsg(strName + "insert RSK_PROBLEM error; " + sqlErrtext);
   else if (isDelete())
      errmsg(strName + "delete RSK_PROBLEM error; " + sqlErrtext);
   else errmsg(strName + "update RSK_PROBLEM error; " + sqlErrtext);
   return -1;
}
//void kk_Value() {
//	kk1 =wp.colStr("reference_no");
//	kk2 =wp.col_int("reference_seq");
//	is_ctrl_seqno =wp.colStr("ctrl_seqno");
//}

public int dataSelect(String kCtrlSeqno, String kBinType) throws Exception  {
   return dataSelect(kCtrlSeqno, kBinType, "");
}

public int dataSelect(String kCtrlSeqno, String kBinType, String kRefNo) {
   strSql = "select A.*,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt(curr_code,dest_amt, dc_dest_amt) as dc_dest_amt,"
         + " uf_tt_idtab('%'||prb_mark||'-CLO-RESULT',clo_result) as tt_clo_result,"
         + " uf_dc_amt(curr_code,mcht_repay, dc_mcht_repay) as dc_mcht_repay,"
         + " uf_tt_idtab('%'||prb_mark||'-CLO-RESULT',clo_result_2) as tt_clo_result_2,"
         + " uf_dc_amt(curr_code,mcht_repay_2,dc_mcht_repay_2) as mcht_repay_2,"
         + " uf_tt_idtab('PRBL-REASON-CODE',prb_reason_code) as tt_prb_reason_code,"
         + " hex(rowid) as rowid,"
         + " uf_dc_amt2(prb_amount,dc_prb_amount) as dc_prb_amount , "
         + " decode(A.back_status,'','','S','成功','F','失敗') as tt_back_status "
         + " from rsk_problem A"
         + " where 1=1"
//         + commSqlStr.col(kCtrlSeqno, "ctrl_seqno")
//         + commSqlStr.col(kBinType, "bin_type")
//         + commSqlStr.col(kRefNo, "reference_no")
         ;
   
   if(empty(kCtrlSeqno) == false) {
	   strSql += " and ctrl_seqno = ? ";
	   setString(kCtrlSeqno);
   }
   
   if(empty(kBinType) == false) {
	   strSql += " and bin_type = ? ";
	   setString(kBinType);
   }
   
   if(empty(kRefNo) == false) {
	   strSql += " and reference_no = ? ";
	   setString(kRefNo);
   }
  
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("查無問交資料, ctrl_seqno=%s", kCtrlSeqno);
   }
   this.colDataToWpCol("");

   //
   wp.colSet("prb_fraud_rpt", colNvl("prb_fraud_rpt", "0"));
   wp.colSet("letter_flag", colNvl("letter_flag", "N"));

   return rc;
}

public int rskp0010Batch(String aRefNo) throws Exception  {
   msgOK();

//   is_ctrl_seqno =wp.colStr("ctrl_seqno");
   isReferNo = aRefNo;

   wp.itemSet("debit_flag", "N");
   ibDebit = false;
   this.msgOK();
   if (empty(aRefNo)) {
      errmsg("帳單流水號(reference_no): 不可空白");
      return -1;
   }
   strSql = "select count(*) as ll_cnt from rsk_problem"
         + " where reference_no =?";
   setParm(1, aRefNo);
   sqlSelect(strSql);
   if (sqlRowNum > 0 && colNum("ll_cnt") > 0) {
      errmsg("帳單已列問交, 不可再列印問交");
      return -1;
   }

   this.actionCode = "A";
   //--
   selectBilBill();
   if (rc != 1)
      return rc;

   selectCardAcno();
   if (rc != 1)
      return rc;

   //-OK----------------------------------------
   wp.itemSet("bin_type", colStr("B-bin_type"));
   wp.itemSet("prb_src_code", "RQ");      //人工列問交
   wp.itemSet("prb_mark", "Q");
   wp.itemSet("prb_reason_code", "22");
   wp.itemSet("prb_amount", colStr("B-dest_amt"));
   wp.itemSet("dc_prb_amount", colStr("B-dc_dest_amt"));
   wp.itemSet("prb_print_flag", "N");
   wp.itemSet("prb_fraud_rpt", "0");
   wp.itemSet("letter_flag", "Y");
   wp.itemSet("prb_status", "10");

   return dataInsert();
}

public int rskm0010Insert() {
   this.actionCode = "A";
//	is_ctrl_seqno =wp.colStr("ctrl_seqno");
   if (empty(isReferNo))
      isReferNo = wp.itemStr("reference_no");
   //--
   selectBilBill();
   if (rc != 1)
      return rc;

   selectCardAcno();
   if (rc != 1)
      return rc;

   return dataInsert();
}

int dataInsert() {
   RskCtrlseqno oolog = new RskCtrlseqno();
   oolog.setConn(wp);
   isCtrlSeqno = oolog.getCtrlSeqno(isReferNo);
   if (empty(isCtrlSeqno)) {
      errmsg(oolog.mesg());
      return rc;
   }

   busi.SqlPrepare t_pblm = new busi.SqlPrepare();
   t_pblm.sql2Insert("rsk_problem");
   t_pblm.ppstr("reference_no", isReferNo);
   t_pblm.ppnum("reference_seq", 0);
   t_pblm.ppstr("bin_type", colStr("B-bin_type"));
   t_pblm.ppstr("ctrl_seqno", isCtrlSeqno);
   //-user-edit-
   t_pblm.ppstr("debit_flag", wp.itemNvl("debit_flag", "N"));
   t_pblm.ppstr("prb_src_code", wp.itemStr("prb_src_code"));
   t_pblm.ppstr("prb_mark", wp.itemStr("prb_mark"));
   t_pblm.ppstr("prb_reason_code", wp.itemStr("prb_reason_code"));
   t_pblm.ppnum("prb_amount", wp.itemNum("prb_amount"));
   t_pblm.ppnum("dc_prb_amount", wp.itemNum("dc_prb_amount"));
   t_pblm.ppstr("prb_print_flag", wp.itemNvl("prb_print_flag", "N"));
   t_pblm.ppstr("prb_fraud_rpt", wp.itemStr("prb_fraud_rpt"));
   t_pblm.ppstr("prb_comment", wp.itemStr("prb_comment"));
   t_pblm.ppstr("letter_flag", wp.itemStr("letter_flag"));
   //
   t_pblm.ppstr("prb_status", "10");
   t_pblm.ppstr("prb_txfer_date", "");
   t_pblm.ppstr("rsk_err_nr", colStr("B-rsk_err_nr"));
   t_pblm.ppymd("add_date");
   t_pblm.ppstr("add_user", modUser);
   t_pblm.ppstr("major_id_p_seqno", colStr("C-major_id_p_seqno"));
   t_pblm.ppstr("major_card_no", colStr("C-major_card_no"));
   t_pblm.ppstr("corp_p_seqno", colStr("C-corp_p_seqno"));
   t_pblm.ppstr("card_no", colStr("B-card_no"));
   t_pblm.ppstr("p_seqno", colStr("B-p_seqno"));
   t_pblm.ppstr("acct_type", colStr("B-acct_type"));
   t_pblm.ppstr("txn_code", colStr("B-txn_code"));
   t_pblm.ppstr("film_no", colStr("B-film_no"));
   t_pblm.ppstr("acq_member_id", colStr("B-acq_member_id"));
   t_pblm.ppnum("source_amt", colNum("B-source_amt"));
   t_pblm.ppstr("source_curr", colStr("B-source_curr"));
   t_pblm.ppnum("settl_amt", colNum("B-settl_amt"));
   t_pblm.ppnum("dest_amt", colNum("B-dest_amt"));
   t_pblm.ppstr("dest_curr", colStr("B-dest_curr"));
   t_pblm.ppstr("mcht_eng_name", colStr("B-mcht_eng_name"));
   t_pblm.ppstr("mcht_city", colStr("B-mcht_city"));
   t_pblm.ppstr("mcht_country", colStr("B-mcht_country"));
   t_pblm.ppstr("mcht_category", colStr("B-mcht_category"));
   t_pblm.ppstr("mcht_no", colStr("B-mcht_no"));
   t_pblm.ppstr("mcht_chi_name", colStr("B-mcht_chi_name"));
   t_pblm.ppstr("auth_code", colStr("B-auth_code"));
   t_pblm.ppstr("acct_month", colStr("B-acct_month"));
   t_pblm.ppstr("bill_type", colStr("B-bill_type"));
   t_pblm.ppnum("cash_adv_fee", colNum("B-cash_adv_fee"));
   t_pblm.ppstr("purchase_date", colStr("B-purchase_date"));
   t_pblm.ppstr("acquire_date", colStr("B-acquire_date"));
   t_pblm.ppstr("process_date", colStr("B-process_date"));
   t_pblm.ppstr("post_date", colStr("B-post_date"));
   t_pblm.ppstr("stmt_cycle", colStr("B-stmt_cycle"));
   t_pblm.ppstr("interest_date", colStr("B-interest_date"));
   t_pblm.ppstr("fees_reference_no", colStr("B-fees_reference_no"));
   t_pblm.ppstr("reference_no_ori", colStr("B-reference_no_ori"));
   t_pblm.ppstr("rsk_type", colStr("B-rsk_type"));
   t_pblm.ppnum("curr_tx_amount", colNum("B-curr_tx_amount"));
   t_pblm.ppint("install_tot_term1", (int) colNum("B-install_tot_term1"));
   t_pblm.ppnum("install_first_amt", colNum("B-install_first_amt"));
   t_pblm.ppnum("install_per_amt", colNum("B-install_per_amt"));
   t_pblm.ppnum("install_fee", colNum("B-install_fee"));
   t_pblm.ppstr("block_reason", colStr("C-block_reason1"));
   t_pblm.ppstr("block_reason2", colStr("C-block_reason2"));
   t_pblm.ppstr("block_reason3", colStr("C-block_reason3"));
   t_pblm.ppstr("block_reason4", colStr("C-block_reason4"));
   t_pblm.ppstr("block_reason5", colStr("C-block_reason5"));
   if (wp.itemEq("debit_flag", "Y")) {
      t_pblm.ppnum("dc_dest_amt", colNum("B-dest_amt"));
      t_pblm.ppstr("curr_code", colStr("B-dest_curr"));
   }
   else {
      t_pblm.ppnum("dc_dest_amt", colNum("B-dc_dest_amt"));
      t_pblm.ppstr("curr_code", colStr("B-curr_code"));
   }
   t_pblm.ppstr("v_card_no", colStr("B-v_card_no"));
   t_pblm.ppstr("sign_flag", colStr("B-sign_flag"));
   t_pblm.ppstr("id_p_seqno", colStr("B-id_p_seqno"));
   t_pblm.ppstr("back_flag", wp.itemNvl("back_flag", "N"));
   t_pblm.modxxx(modUser, modPgm);

   //wp.ddd(t_pblm.sql_stmt(), t_pblm.sql_parm());
   sqlExec(t_pblm.sqlStmt(), t_pblm.sqlParm());
   if (rc <= 0) {
      return errUpdate("問交新增: ");
   }

   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", isCtrlSeqno);
   return rc;
}

void selectBilBill() {
   ibDebit = wp.itemEq("debit_flag", "Y");

   daoTid = "B-";
   strSql = "select A.*"
//			+ ", A.rsk_ctrl_seqno as ctrl_seqno "
         + ", A.reference_no_original as reference_no_ori "
   ;
   if (ibDebit) {
      strSql += ", 'V' as bin_type2";
      strSql += " from dbb_bill A";
   }
   else {
      strSql += ", uf_refno_ori(A.contract_no,A.reference_no_original) as refno_ori ";
      strSql += ", uf_bin_type(A.card_no) as bin_type2";
      strSql += " from bil_bill A";
   }
   strSql += " where A.reference_no =?";

   sqlSelect(strSql, new Object[]{isReferNo});
   if (sqlRowNum <= 0) {
      errmsg("帳單資料不存在");
      return;
   }

   //--
   if (colNeq("B-sign_flag", "+")) {
      errmsg("負項交易: 不可列問交");
      return;
   }
   if (colEq("B-bill_type", "FIFC")) {
      errmsg("費用類交易: 不可列問交");
      return;
   }

   if (ibDebit == false) {
      String ls_refno_ori = colStr("B-reference_no_ori");
      if (empty(ls_refno_ori)) {
         colSet("B-reference_no_ori", colStr("B-refno_ori"));
      }
   }

   if (colEmpty("B-bin_type")) {
      colSet("B-bin_type", colStr("B-bin_type2"));
   }

   return;
}

void selectCardAcno() {
   daoTid = "C-";
   this.sqlSelect = "select "
         + " A.block_reason1 "
         + ", A.block_reason2 "
         + ", A.block_reason3 "
         + ", A.block_reason4 "
         + ", A.block_reason5 "
         + ", C.major_card_no "
         + ", C.major_id_p_seqno "
         + ", C.corp_p_seqno "
   ;
   if (ibDebit) {
      this.sqlFrom = " from dbc_card C left join cca_card_acct A "
            + " on C.p_seqno = A.p_seqno"
            + " and A.debit_flag ='Y'"
      ;
   }
   else {
      this.sqlFrom = " from crd_card C left join cca_card_acct A "
            + " on C.p_seqno = A.p_seqno and A.debit_flag <>'Y' and A.acno_flag<>'Y'";
   }
   sqlWhere = " where card_no =?";
   strSql = sqlSelect + sqlFrom + sqlWhere;
   sqlSelect(strSql, new Object[]{colStr("B-card_no")});
   if (sqlRowNum <= 0) {
      errmsg("select CRD_CARD+CCA_CARD_ACCT no-find; " + sqlErrtext);
   }
   return;
}

public int rskm0010Update() {
   isCtrlSeqno = wp.colStr("ctrl_seqno");
   isReferNo = wp.colStr("reference_no");

   this.actionCode = "U";
   strSql = "update rsk_problem set"
         + " prb_status =:prb_status "
         + ", prb_reason_code =:prb_reason_code "
         + ", prb_fraud_rpt =:prb_fraud_rpt "
         + ", prb_comment =:prb_comment "
         + ", prb_amount =:prb_amount "
         + ", dc_prb_amount =:dc_prb_amount "
         + ", letter_flag =:letter_flag "
         + ", back_flag =:back_flag "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1"
         + " where reference_no =:kk1"
         + "   and ctrl_seqno =:kk2"
   ;
   setString("prb_status", wp.itemStr("prb_status"));
   item2ParmStr("prb_reason_code");
   item2ParmStr("prb_fraud_rpt");
   item2ParmStr("prb_comment");
   item2ParmNum("prb_amount");
   item2ParmStr("dc_prb_amount");
   item2ParmStr("letter_flag");
   item2ParmNvl("back_flag","N");
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   setString("kk1", isReferNo);
   setString("kk2", isCtrlSeqno);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交維護: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskm0010Delete() {
   this.actionCode = "D";


   strSql = "delete rsk_problem"
         + " where rowid =?"
         + " and mod_seqno =?"
   ;
   setRowId(wp.colStr("rowid"));
   setDouble(wp.colNum("mod_seqno"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delete rsk_problem error; " + sqlErrtext);
      return rc;
   }

   isReferNo = wp.colStr("reference_no");
   isCtrlSeqno = wp.colStr("ctrl_seqno");
//   update_rsk_ctrlseqno_log();

   return rc;
}

//-結案-
public int rskm0020Update() {
   strSql = "update rsk_problem set"
         + " close_add_user =:mod_user "
         + ", close_add_date =to_char(sysdate,'yyyymmdd') "
         + ", prb_status ='60' "
         + ", clo_result =:clo_result "
         + ", org_card_no =:org_card_no "
         + ", prb_glmemo3 =:prb_glmemo3 "
         + ", dc_mcht_repay =:dc_mcht_repay "
         + ", mcht_repay =:mcht_repay "
         + ", mcht_close_fee =:mcht_close_fee "
         + ", contract_no =:contract_no "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1"
         + " where ctrl_seqno =:kk1"
         + "   and bin_type =:kk2"
   ;
   setString("kk1", wp.itemStr("ctrl_seqno"));
   setString("kk2", wp.itemStr("bin_type"));
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   item2ParmStr("clo_result");
   item2ParmStr("org_card_no");
   item2ParmStr("prb_glmemo3");
   item2ParmNum("dc_mcht_repay");
   item2ParmNum("mcht_repay");
   item2ParmNum("mcht_close_fee");
   item2ParmStr("contract_no");
   
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交結案: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

//-結案取消-
public int rskm0020Delete() {
   strSql = "update rsk_problem set"
         + " close_add_user ='' "
         + ", close_add_date ='' "
         + ", prb_status ='30' "
         + ", clo_result ='' "
         + ", org_card_no ='' "
         + ", prb_glmemo3 ='' "
         + ", mcht_repay =0 "
         + ", dc_mcht_repay =0 "
         + ", mcht_close_fee =0 "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1"
         + " where ctrl_seqno =:ctrl_seqno"
         + " and	bin_type =:bin_type"
   ;
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   setString("ctrl_seqno", isCtrlSeqno);
   setString("bin_type", wp.itemStr("bin_type"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("結案取消: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

//-二次結案-
public int rskm0022Update() {
   strSql = "update rsk_problem set"
         + " close_add_user_2 =:add_user "
         + ", close_add_date_2 =" + commSqlStr.sysYYmd
         + ", prb_status ='83' "
         + ", clo_result_2 =:clo_result "
//			+", org_card_no =:org_card_no "
         + ", prb_glmemo3_2 =:prb_glmemo3 "
         + ", dc_mcht_repay_2 =:dc_mcht_repay "
         + ", mcht_repay_2 =:mcht_repay "
         + ", mcht_close_fee_2 =:mcht_close_fee, "
         + commSqlStr.setModxxx(modUser, modUser)
         + " where ctrl_seqno =:kk1"
         + "   and bin_type =:kk2"
   ;
   setString("add_user", modUser);
   setString("clo_result", wp.itemStr("clo_result_2"));
//	item2ParmStr("org_card_no");
   setString("prb_glmemo3", wp.itemStr("prb_glmemo3_2"));
   setDouble("dc_mcht_repay", wp.itemNum("dc_mcht_repay_2"));
   setDouble("mcht_repay", wp.itemNum("mcht_repay_2"));
   setDouble("mcht_close_fee", wp.itemNum("mcht_close_fee_2"));
   setString("kk1", wp.itemStr("ctrl_seqno"));
   setString("kk2", wp.itemStr("bin_type"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交結案(二): ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

//-二次結案取消-
public int rskm0022Delete() {
   strSql = "update rsk_problem set"
         + " close_add_user_2 ='' "
         + ", close_add_date_2 ='' "
         + ", prb_status ='80' "
         + ", clo_result_2 ='' "
//			+", org_card_no ='' "
         + ", prb_glmemo3_2 ='' "
         + ", mcht_repay_2 =0 "
         + ", dc_mcht_repay_2 =0 "
         + ", mcht_close_fee_2 =0, "
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where ctrl_seqno =:ctrl_seqno"
         + " and	bin_type =:bin_type"
   ;
   setString("ctrl_seqno", wp.itemStr("ctrl_seqno"));
   setString("bin_type", wp.itemStr("bin_type"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("取消結案(二): ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskm0025Update() {
   strSql = "update rsk_problem set"
         + " prb_status ='60',"
         + " close_add_date =" + commSqlStr.sysYYmd + ","
         + " close_add_user =?,"
         + " clo_result =?,"
         + " mcht_repay =?,"
         + " dc_mcht_repay =?,"
         + " mcht_close_fee =0,"
         + " prb_glmemo3 =?,"
         + " clo_merge_vouch =?,"
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";

   if (wp.itemEq("ex_sys_close", "Y")) {
      setString("system");
   }
   else setString(modUser);

   setString(wp.itemStr("prbl-clo_result"));
   setDouble(wp.itemNum("prbl-mcht_repay"));
   setDouble(wp.itemNum("prbl-dc_mcht_repay"));
   setString(wp.itemStr("prbl-prb_glmemo3"));
   setString(wp.itemStr("prbl-clo_merge_vouch"));
   setRowId(7, wp.itemStr("prbl-rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      this.errmsg("Update rsk_problem error; " + sqlErrtext);
      return rc;
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskm0025Delete(String aRowid) {
   msgOK();

   strSql = "update rsk_problem set"
         + " prb_status ='30'"
         + ", close_add_date =''"
         + ", close_add_user =''"
         + ", clo_result =''"
         + ", org_card_no =''"
         + ", mcht_repay =0"
         + ", dc_mcht_repay =0"
         + ", mcht_close_fee =0"
         + ", prb_glmemo3 =''"
         + ", clo_merge_vouch ='' ,"
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?"
   ;
   this.setRowId(aRowid);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update RSK_PROBLEM error; " + sqlErrtext);
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskp0020Approve() {
   isReferNo = varsStr("reference_no");
   isCtrlSeqno = varsStr("ctrl_seqno");

   strSql = "update rsk_problem set "
         + " prb_status =:prb_status "
         + ", add_apr_date =to_char(sysdate,'yyyymmdd') "
         + ", add_apr_user =:mod_user "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1 "
         + " where rowid =:kk1"
   ;
   setString("prb_status", varsStr("prb_status"));

   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   setRowId("kk1", varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交維護覆核: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskp0020Cancel() throws Exception  {
   isReferNo = varsStr("reference_no");
   isCtrlSeqno = varsStr("ctrl_seqno");

   strSql = "update rsk_problem set "
         + " prb_status =:prb_status "
         + ", add_apr_date ='' "
         + ", add_apr_user =''"
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1 "
         + " where rowid =:kk1"
   ;

   setString("prb_status", varsStr("prb_status"));
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   setRowId("kk1", varsStr("rowid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交維護解覆核: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}


public int rskp0030Approve() throws Exception  {
   isReferNo = varsStr("reference_no");
   isCtrlSeqno = varsStr("ctrl_seqno");
   referSeq = varsInt("reference_seq");
   String lsRowid = varsStr("rowid");

   strSql = "select prb_status"
         + " from rsk_problem"
         + " where rowid =? "
         + " and nvl(mod_seqno,0) = ? "
         //+" and reference_seq =?"
         ;
   setRowId(1, lsRowid);
   setString(2,varsStr("mod_seqno"));
   //setInt(2,kk2);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料已被異動 OR 不存在");
      return -1;
   }
   if (colNeq("prb_status", "60")) {
      errmsg("問交狀態: 不是[60], 不可覆核");
      return -1;
   }

   strSql = "update rsk_problem set "
         + " prb_status =:prb_status "
         + ", close_apr_date =to_char(sysdate,'yyyymmdd') "
         + ", close_apr_user =:mod_user "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1 "
         + " where rowid =?"
   ;
   setString("prb_status", "80");
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);
   setRowId("kk1", lsRowid);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交結案覆核: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskp0030Cancel() throws Exception  {

   strSql = "select prb_status, ctrl_seqno, bin_type"
         + ", reference_no, ctrl_seqno"
         + " from rsk_problem"
         + " where rowid =?"
         //+" and reference_seq =?"
         + " and nvl(mod_seqno,0) = ? ";
   setRowId(1, varsStr("rowid"));
   setString(2,varsStr("mod_seqno"));   
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg(errOtherModify);
      return -1;
   }
   if (colNeq("prb_status", "80")) {
      errmsg("問交狀態: 不是[80], 不可[解覆核]");
      return -1;
   }

   isCtrlSeqno = colStr("ctrl_seqno");
   isReferNo = colStr("reference_no");

   strSql = "update rsk_problem set "
         + " prb_status ='60' "
         + ", close_apr_date ='' "
         + ", close_apr_user ='' "
         + ", mod_user =:mod_user "
         + ", mod_time =sysdate "
         + ", mod_pgm =:mod_pgm "
         + ", mod_seqno =mod_seqno+1 "
         + " where rowid =?"
   ;
   setRowId(1, varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("問交結案解覆核: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int rskP0030NewPRBL() {
   strSql = "update rsk_problem set"
         + " reference_seq =reference_seq+1 "
         + " where rowid =?";
   this.setRowId(1, varsStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("更新 RSK_PROBLEM 檔(For '92')失敗？！");
      return -1;
   }

   strSql = "select *"
         + " from rsk_problem"
         + " where rowid =?";
   this.setRowId(1, varsStr("rowid"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("讀取 RSK_PROBLEM 檔(For '92')失敗？！");
      return -1;
   }

   isCtrlSeqno = colStr("ctrl_seqno");
   isReferNo = colStr("reference_no");

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("rsk_problem");
   spp.ppstr("reference_no", isReferNo);
   spp.ppint("reference_seq", 0);
   spp.ppstr("bin_type", colStr("bin_type"));
   spp.ppstr("ctrl_seqno", isCtrlSeqno);
   spp.ppstr("prb_src_code", colStr("prb_src_code"));
   spp.ppstr("prb_mark", "Q");
   spp.ppstr("prb_status", "30");
   spp.ppstr("prb_reason_code", "98");
   spp.ppnum("prb_amount", colNum("prb_amount"));
   spp.ppstr("prb_txfer_date", colStr("prb_txfer_date"));
   spp.ppstr("prb_print_flag", colStr("prb_print_flag"));
   spp.ppstr("prb_print_comp", colStr("prb_print_comp"));
   spp.ppstr("prb_fraud_rpt", colStr("prb_fraud_rpt"));
   spp.ppstr("prb_comment", colStr("prb_comment"));
   spp.ppstr("org_card_no", colStr("org_card_no"));
   spp.ppstr("rsk_err_nr", colStr("rsk_err_nr"));
   spp.ppymd("add_date");
   spp.ppstr("add_user", colStr("close_add_user"));
   spp.ppymd("add_apr_date");
   spp.ppstr("add_apr_user", modUser);
   spp.ppstr("clo_result", colStr("clo_result"));
   spp.ppnum("mcht_repay", colNum("mcht_repay"));
   spp.ppstr("prb_glmemo3", colStr("prb_glmemo3"));
   spp.ppstr("close_add_date", "");
   spp.ppstr("close_add_user", "");
   spp.ppstr("close_apr_date", "");
   spp.ppstr("close_apr_user", "");
   spp.ppstr("major_id_p_seqno", colStr("major_id_p_seqno"));
   spp.ppstr("major_card_no", colStr("major_card_no"));
   spp.ppstr("corp_p_seqno", colStr("corp_p_seqno"));
   spp.ppstr("card_no", colStr("card_no"));
   spp.ppstr("p_seqno", colStr("p_seqno"));
   spp.ppstr("acct_type", colStr("acct_type"));
   spp.ppstr("txn_code", colStr("txn_code"));
   spp.ppstr("film_no", colStr("film_no"));
   spp.ppstr("acq_member_id", colStr("acq_member_id"));
   spp.ppnum("source_amt", colNum("source_amt"));
   spp.ppstr("source_curr", colStr("source_curr"));
   spp.ppnum("settl_amt", colNum("settl_amt"));
   spp.ppnum("dest_amt", colNum("dest_amt"));
   spp.ppstr("dest_curr", colStr("dest_curr"));
   spp.ppstr("mcht_eng_name", colStr("mcht_eng_name"));
   spp.ppstr("mcht_city", colStr("mcht_city"));
   spp.ppstr("mcht_country", colStr("mcht_country"));
   spp.ppstr("mcht_category", colStr("mcht_category"));
   spp.ppstr("mcht_no", colStr("mcht_no"));
   spp.ppstr("mcht_chi_name", colStr("mcht_chi_name"));
   spp.ppstr("auth_code", colStr("auth_code"));
   spp.ppstr("acct_month", colStr("acct_month"));
   spp.ppstr("bill_type", colStr("bill_type"));
   spp.ppnum("cash_adv_fee", colNum("cash_adv_fee"));
   spp.ppstr("purchase_date", colStr("purchase_date"));
   spp.ppstr("acquire_date", colStr("acquire_date"));
   spp.ppstr("process_date", colStr("process_date"));
   spp.ppstr("post_date", colStr("post_date"));
   spp.ppstr("stmt_cycle", colStr("stmt_cycle"));
   spp.ppstr("interest_date", colStr("interest_date"));
   spp.ppstr("fees_reference_no", colStr("fees_reference_no"));
   spp.ppstr("reference_no_ori", colStr("reference_no_ori"));
   spp.ppstr("debit_flag", colStr("debit_flag"));
   spp.ppstr("rsk_type", colStr("rsk_type"));
   spp.ppstr("payment_type", colStr("payment_type"));
   spp.ppnum("curr_tx_amount", colNum("curr_tx_amount"));
   spp.ppnum("install_tot_term1", colNum("install_tot_term1"));
   spp.ppnum("install_first_amt", colNum("install_first_amt"));
   spp.ppnum("install_per_amt", colNum("install_per_amt"));
   spp.ppnum("install_fee", colNum("install_fee"));
   spp.ppnum("mcht_close_fee", colNum("mcht_close_fee"));
   spp.ppstr("block_reason", colStr("block_reason"));
   spp.ppstr("block_reason2", colStr("block_reason2"));
   spp.ppstr("block_reason3", colStr("block_reason3"));
   spp.ppstr("block_reason4", colStr("block_reason4"));
   spp.ppstr("block_reason5", colStr("block_reason5"));
   spp.ppstr("curr_code", colStr("curr_code"));
   spp.ppnum("dc_dest_amt", colNum("dc_dest_amt"));
   spp.ppnum("dc_prb_amount", colNum("dc_prb_amount"));
   spp.ppnum("dc_mcht_repay", colNum("dc_mcht_repay"));
   spp.ppstr("v_card_no", colStr("v_card_no"));
   spp.ppstr("sign_flag", colStr("sign_flag"));
   spp.ppstr("id_p_seqno", colStr("id_p_seqno"));
   spp.ppstr("letter_flag", colStr("letter_flag"));
   spp.ppstr("letter_print_date", colStr("letter_print_date"));
   spp.ppstr("letter_print_user", colStr("letter_print_user"));
   spp.ppstr("clo_result_2", colStr("clo_result_2"));
   spp.ppnum("mcht_repay_2", colNum("mcht_repay_2"));
   spp.ppnum("dc_mcht_repay_2", colNum("dc_mcht_repay_2"));
   spp.ppstr("prb_glmemo3_2", colStr("prb_glmemo3_2"));
   spp.ppstr("close_add_date_2", colStr("close_add_date_2"));
   spp.ppstr("close_add_user_2", colStr("close_add_user_2"));
   spp.ppstr("close_apr_date_2", colStr("close_apr_date_2"));
   spp.ppstr("close_apr_user_2", colStr("close_apr_user_2"));
   spp.ppstr("add_vouch_flag", colStr("add_vouch_flag"));
   spp.ppstr("add_vouch_date", colStr("add_vouch_date"));
   spp.ppstr("close_vouch_flag", colStr("close_vouch_flag"));
   spp.ppstr("close_vouch_date", colStr("close_vouch_date"));
   spp.ppstr("close2_vouch_flag", colStr("close2_vouch_flag"));
   spp.ppstr("close2_vouch_date", colStr("close2_vouch_date"));
   spp.ppstr("tpan_type", colStr("tpan_type"));
   spp.ppstr("contract_no", colStr("contract_no"));
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增  RSK_PROBLEM 檔(For '92')失敗？！");
      return -1;
   }

   return rc;
}

}
