package rskm01;
/**
 * rsk_chgback 公用程式
 * 2019-1128   JH    UAT-bug
 * 2019-0907   JH    --rsk_ctrlseqno_log
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0511:	JH		批次reference_seq=0
 * 2018-0320:	JH		++rskP0010_batch
 * 2018-0131:	JH		+VD dbb_bill, dbc_card, dba_acno
 */

import busi.FuncBase;
import taroko.base.Parm2Sql;


public class RskChgback extends FuncBase {
busi.CommBusi zzComm = new busi.CommBusi();
busi.CommCurr zzCurr = new busi.CommCurr();

public boolean checkCardBase=true;
public String isReferNo = "", isCtrlSeqno = "";
int referSeq = 0;
boolean ibDebit = false;
String refnoOri = "", contrNo = "";

int errUpdate(String strName) {
   if (isAdd())
      errmsg(strName + "insert RSK_CHGBACK error; ref-No=%s", isReferNo);
   else if (isDelete())
      errmsg(strName + "delete RSK_CHGBACK error; ref-No=%s", isReferNo);
   else errmsg(strName + "update RSK_CHGBACK error; ref-No=%s", isReferNo);
   return rc;
}

public int dataSelect() throws Exception  {
   if (wpIsNull())
      return rc;

   if (varEmpty("reference_no") || varEmpty("reference_seq")) {
      errmsg("帳單參考號碼, 序號: 不可空白");
      return -1;
   }

   isReferNo = varsStr("reference_no");
   referSeq = varsInt("reference_seq");

   this.daoTid = "CB_";
   strSql = "select A.*, hex(rowid) as rowid"
         + ", decode(chg_times,2,'',send_flag) as db_fst_send_flag "
         + ", decode(chg_times,2,send_flag,'') as db_sec_send_flag "
         + ", uf_idno_name2(A.card_no,A.debit_flag) as db_idno_name "
         + ", uf_idno_id2(A.card_no,A.debit_flag) as major_idno "
         + ", uf_dc_curr(curr_code) as curr_code "
         + ", decode(A.curr_code,'901',A.dest_amt,'',A.dest_amt,A.dc_dest_amt) as dc_dest_amt "
         + " from rsk_chgback A"
         + " where reference_no =?"
         + " and reference_seq =?"
   ;
   sqlSelect(strSql, new Object[]{isReferNo, referSeq});
   if (sqlRowNum <= 0) {
      errmsg("查無扣款資料; kk=" + isReferNo + "," + referSeq);
      return -1;
   }

   this.colDataToWpCol("CB_");
   if (colStr("CB_chg_stage").compareTo("3") >= 0 && !colEmpty("CB_sec_status")) {
      //-一扣資料-
      wp.colSet("CB_fst_status", colStr("CB_sec_status"));
      wp.colSet("CB_fst_reverse_mark ", colStr("CB_sec_reverse_mark "));
      wp.colSet("CB_fst_reverse_date ", colStr("CB_sec_reverse_date "));
      wp.colSet("CB_fst_rebuild_mark ", colStr("CB_sec_rebuild_mark "));
      wp.colSet("CB_fst_rebuild_date ", colStr("CB_sec_rebuild_date "));
      wp.colSet("CB_fst_send_date    ", colStr("CB_sec_send_date    "));
      wp.colSet("CB_fst_send_cnt     ", colStr("CB_sec_send_cnt     "));
      wp.colSet("CB_fst_usage_code   ", colStr("CB_sec_usage_code   "));
      wp.colSet("CB_fst_reason_code  ", colStr("CB_sec_reason_code  "));
      wp.colSet("CB_fst_msg          ", colStr("CB_sec_msg          "));
      wp.colSet("CB_fst_doc_mark     ", colStr("CB_sec_doc_mark     "));
      wp.colSet("CB_fst_amount       ", colStr("CB_sec_amount       "));
      wp.colSet("CB_fst_twd_amt      ", colStr("CB_sec_twd_amt      "));
      wp.colSet("CB_fst_dc_amt       ", colStr("CB_sec_dc_amt       "));
      wp.colSet("CB_fst_part_mark    ", colStr("CB_sec_part_mark    "));
      wp.colSet("CB_fst_expire_date  ", colStr("CB_sec_expire_date  "));
      wp.colSet("CB_fst_add_date     ", colStr("CB_sec_add_date     "));
      wp.colSet("CB_fst_add_user     ", colStr("CB_sec_add_user     "));
      wp.colSet("CB_fst_apr_date     ", colStr("CB_sec_apr_date     "));
      wp.colSet("CB_fst_apr_user     ", colStr("CB_sec_apr_user     "));
      wp.colSet("CB_fst_disb_yn      ", colStr("CB_sec_disb_yn      "));
      wp.colSet("CB_fst_disb_amt     ", colStr("CB_sec_disb_amt     "));
      wp.colSet("CB_fst_disb_dc_amt  ", colStr("CB_sec_disb_dc_amt  "));
      wp.colSet("CB_fst_disb_add_date", colStr("CB_sec_disb_add_date"));
      wp.colSet("CB_fst_disb_add_user", colStr("CB_sec_disb_add_user"));
      wp.colSet("CB_fst_disb_apr_date", colStr("CB_sec_disb_apr_date"));
      wp.colSet("CB_fst_disb_apr_user", colStr("CB_sec_disb_apr_user"));
      //-二扣資料-
      wp.colSet("CB_sec_status", colStr("CB_fst_status"));
      wp.colSet("CB_sec_reverse_mark ", colStr("CB_fst_reverse_mark "));
      wp.colSet("CB_sec_reverse_date ", colStr("CB_fst_reverse_date "));
      wp.colSet("CB_sec_rebuild_mark ", colStr("CB_fst_rebuild_mark "));
      wp.colSet("CB_sec_rebuild_date ", colStr("CB_fst_rebuild_date "));
      wp.colSet("CB_sec_send_date    ", colStr("CB_fst_send_date    "));
      wp.colSet("CB_sec_send_cnt     ", colStr("CB_fst_send_cnt     "));
      wp.colSet("CB_sec_usage_code   ", colStr("CB_fst_usage_code   "));
      wp.colSet("CB_sec_reason_code  ", colStr("CB_fst_reason_code  "));
      wp.colSet("CB_sec_msg          ", colStr("CB_fst_msg          "));
      wp.colSet("CB_sec_doc_mark     ", colStr("CB_fst_doc_mark     "));
      wp.colSet("CB_sec_amount       ", colStr("CB_fst_amount       "));
      wp.colSet("CB_sec_twd_amt      ", colStr("CB_fst_twd_amt      "));
      wp.colSet("CB_sec_dc_amt       ", colStr("CB_fst_dc_amt       "));
      wp.colSet("CB_sec_part_mark    ", colStr("CB_fst_part_mark    "));
      wp.colSet("CB_sec_expire_date  ", colStr("CB_fst_expire_date  "));
      wp.colSet("CB_sec_add_date     ", colStr("CB_fst_add_date     "));
      wp.colSet("CB_sec_add_user     ", colStr("CB_fst_add_user     "));
      wp.colSet("CB_sec_apr_date     ", colStr("CB_fst_apr_date     "));
      wp.colSet("CB_sec_apr_user     ", colStr("CB_fst_apr_user     "));
      wp.colSet("CB_sec_disb_yn      ", colStr("CB_fst_disb_yn      "));
      wp.colSet("CB_sec_disb_amt     ", colStr("CB_fst_disb_amt     "));
      wp.colSet("CB_sec_disb_dc_amt  ", colStr("CB_fst_disb_dc_amt  "));
      wp.colSet("CB_sec_disb_add_date", colStr("CB_fst_disb_add_date"));
      wp.colSet("CB_sec_disb_add_user", colStr("CB_fst_disb_add_user"));
      wp.colSet("CB_sec_disb_apr_date", colStr("CB_fst_disb_apr_date"));
      wp.colSet("CB_sec_disb_apr_user", colStr("CB_fst_disb_apr_user"));
   }

   return 1;
}

void getRefnoOri(String aRefno) {
   isReferNo = aRefno;
   refnoOri = aRefno;
   contrNo = "";
   if (ibDebit)
      return;

   strSql = "select A.contract_no"
         + ", uf_refno_ori(A.contract_no,A.reference_no_original) as refno_ori"
         + " from bil_bill A"
         + " where A.reference_no =?"
   ;
   setParm(1, aRefno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("帳單流水號: 不存在");
      return;
   }

   if (empty(colStr("contract_no")) == false) {
      contrNo = colStr("contract_no");
      refnoOri = colStr("refno_ori");
   }
}

void selectBilBill(String aRefNo) {
   getRefnoOri(aRefNo);
   String lsReferNo = refnoOri;

   daoTid = "B-";
   String sql1 = "select A.*,"
         + " A.reference_no_original as reference_no_ori,"
         + " decode(replace(A.mcht_chi_name,'　',''),A.mcht_eng_name,A.mcht_chi_name) as mcht_name,"
         + " '' as xxx";
   if (ibDebit)
      sql1 += " from dbb_bill A";
   else sql1 += " from bil_bill A";
   sql1 += " where reference_no =? ";
   sqlSelect(sql1, new Object[]{lsReferNo});
   if (sqlRowNum == 0) {
      daoTid = "B-";
      sql1 = "select "
            + "  A.*"
            + ", A.reference_no_original as reference_no_ori "
            + ", '' as billed_date "
            + ", '' as acct_month "
            + ", '' as billed_flag"
            + ", 0 as cash_adv_fee"
            + ", '' as post_date"
            + ", '' as card_type"
            + ", '' as interest_date"
            + ", 0 as install_tot_term1"
            + ", 0 as dc_curr_adjust_amt"
            + ", dc_amount as dc_dest_amt"
      ;
      if (ibDebit)
         sql1 += " from dbb_curpost A";
      else sql1 += " from bil_curpost A";
      sql1 += " where reference_no =? ";

      sqlSelect(sql1, new Object[]{lsReferNo});
   }
   if (sqlRowNum <= 0) {
      errmsg("select BIL[dbb]_BILL(curpost) no-find, ref-no=" + lsReferNo);
      return;
   }
   //--
   String lsContNo =colStr("B-contract_no");
   String lsCurrCode =colStr("B-curr_code");
   if (notEmpty(lsContNo) && eq(lsCurrCode,"901")) {
      strSql ="select tot_amt from bil_contract where contract_no=? "+commSqlStr.rownum(1);
      sqlSelect(strSql,lsContNo);
      if (sqlRowNum >0) {
         colSet("B-dc_dest_amt", colNum("tot_amt"));
      }
   }
   
   if (colEq("B-bill_type", "FIFC")) {	   
	   errmsg("費用類交易: 不可列問交");
	   return;
   }
   
}

void selectCrdCard(String aCardNo) {
   String lsCardNo = nvl(aCardNo);

   daoTid = "C-";
   String sql1 = "select major_id_p_seqno"
         + ", corp_p_seqno"
         + ", bin_type, acct_type";
   if (ibDebit) {
      sql1 += ", p_seqno as acno_p_seqno, p_seqno from dbc_card ";
   }
   else {
      sql1 += ", acno_p_seqno, p_seqno from crd_card ";
   }
   sql1 += " where card_no =?";

   sqlSelect(sql1, new Object[]{lsCardNo});
   if (checkCardBase && sqlRowNum <= 0) {
//      errmsg("select CRD[dbc]_CARD no-find");
   }
}

int checkBatch() throws Exception  {
   //
   String lsRefNo = isReferNo;
   String lsRefNoOri = refnoOri;

   int liCnt = 0;
   if (empty(contrNo)) {
      strSql = "select count(*) as aa_cnt from rsk_chgback" +
            " where reference_no in (?,?) and reference_seq=0";
      setParm(1, lsRefNo);
      setParm(2,lsRefNoOri);
      sqlSelect(strSql);
      liCnt = colInt("aa_cnt");
   }
   else {
      liCnt = checkAdded(contrNo);
   }
   if (liCnt > 0) {
      errmsg("帳單已做過扣款");
      return -1;
   }
   //-非首期-
//   int li_term =colInt("B-install_curr_term");
//	if (li_term !=1) {
//		errmsg("分期帳單 [非首期] 不可執行扣款");
//		return -1;
//	}

   return rc;
}

private int checkAdded(String aContrNo) {
   if (empty(aContrNo)) return 0;

   strSql = "select count(*) as xx_cnt from rsk_chgback" +
         " where reference_no in (select reference_no from bil_bill" +
         " where contract_no =?)" +
         " and reference_seq=0";
   sqlSelect(strSql, new Object[]{aContrNo});
   if (sqlRowNum > 0)
      return colInt("xx_cnt");
   return 0;
}

public int rskp0010Batch(String aRefNo)  throws Exception {
   this.msgOK();

   ibDebit = false;
   this.actionCode = "A";

   isReferNo = aRefNo;
   //--
   selectBilBill(aRefNo);
   if (rc != 1) return rc;
   selectCrdCard(colStr("B-card_no"));
   if (rc != 1) return rc;

   String lsBinType = colStr("C-bin_type");
   wp.itemSet("bin_type", lsBinType);
   wp.itemSet("debit_flag", "N");
   wp.itemSet("fst_usage_code", "");
   if (eq(lsBinType, "V"))
      wp.itemSet("fst_reason_code", "10");
   else if (eq(lsBinType, "M"))
      wp.itemSet("fst_reason_code", "4837");
   else if (eq(lsBinType, "J"))
      wp.itemSet("fst_reason_code", "0546");
   else wp.itemSet("fst_reason_code", "");
   wp.itemSet("fst_msg", "CH DENY THE TRANSACTION.");
   wp.itemSet("fst_doc_mark", "1");
   wp.itemSet("fst_amount", colStr("B-source_amt"));
   wp.itemSet("fst_twd_amt", colStr("B-dest_amt"));
   wp.itemSet("fst_dc_amt", colStr("B-dc_dest_amt"));
   wp.itemSet("fst_part_mark", "N");
   wp.itemSet("fst_vcrcase_no", "");

   wp.itemSet("purchase_date", colStr("B-purchase_date"));
   wp.itemSet("card_no", colStr("B-card_no"));
   wp.itemSet("source_amt", colStr("B-source_amt"));
   wp.itemSet("curr_code", colStr("B-curr_code"));
   wp.itemSet("dc_dest_amt", colStr("B-dc_dest_amt"));
   wp.itemSet("dest_amt", colStr("B-dest_amt"));
   wp.itemSet("mcht_name", colStr("B-mcht_name"));
   wp.itemSet("auth_code", colStr("B-auth_code"));
   wp.itemSet("txn_code", colStr("B-txn_code"));
   wp.itemSet("rsk_rept_mark", "");
   wp.itemSet("rsk_prbl1_mark", "");

   //-check是否可扣款-
   if (checkBatch() < 0) {
      return -1;
   }
   //-return 2rskP0010-
   wp.itemSet("xx_batch_dest_amt",colStr("B-dc_dest_amt"));

   return dataInsert();
}

public int rskm0210Insert() {
   String ls_debit =wp.itemStr("debit_flag");
   ibDebit = zzComm.isDebit(ls_debit);

   this.actionCode = "A";
   if (empty(isReferNo))
	   isReferNo = wp.colStr("reference_no");

   //--
   selectBilBill(isReferNo);

   if (rc != 1)
      return rc;
   selectCrdCard(colStr("B-card_no"));
   if (rc != 1)
      return rc;

   if (checkAdded(contrNo) > 0) {
      errmsg("(分期)帳單已做過扣款");
      return rc;
   }

   return dataInsert();
}

private int dataInsert() {
   //-get控制流水號-
   getCtrlSeqno(isReferNo);
   if (rc != 1)
      return rc;

   //-insert rsk_chgback-
   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("rsk_chgback");
   spp.ppstr("reference_no", isReferNo);
   spp.ppint("reference_seq", 0);
   spp.ppstr("ctrl_seqno", isCtrlSeqno);
   spp.ppstr("ctrl_seqno2", "");
   spp.ppstr("debit_flag", wp.itemNvl("debit_flag", "N"));
   spp.ppstr("chg_stage", "1");
   spp.ppstr("sub_stage", "10");
   spp.ppstr("send_flag", "1");
   spp.ppint("chg_times", 1);
   spp.ppstr("fst_status", "10");
   //
   spp.ppstr("bin_type", wp.itemStr("bin_type"));
   spp.ppstr("fst_usage_code", wp.itemStr("fst_usage_code"));
   spp.ppstr("fst_reason_code", wp.itemStr("fst_reason_code"));
   spp.ppstr("fst_msg", wp.itemStr("fst_msg"));
   spp.ppstr("fst_doc_mark", wp.itemStr("fst_doc_mark"));
   spp.ppnum("fst_amount", wp.itemNum("fst_amount"));
   spp.ppnum("fst_twd_amt", wp.itemNum("fst_twd_amt"));
   spp.ppnum("fst_dc_amt", wp.itemNum("fst_dc_amt"));
   spp.ppstr("fst_part_mark", wp.itemStr("fst_part_mark"));
   spp.ppstr("fst_vcrcase_no", wp.itemStr("fst_vcrcase_no"));
   //
   spp.ppymd("fst_add_date");
   spp.ppstr("fst_add_user", modUser);
   spp.ppstr("card_no", colStr("B-card_no"));
   spp.ppstr("id_p_seqno", colStr("B-id_p_seqno"));
   spp.ppstr("major_id_p_seqno", colStr("C-major_id_p_seqno"));
   spp.ppstr("corp_p_seqno", colStr("C-corp_p_seqno"));
   spp.ppstr("p_seqno", colStr("C-p_seqno"));
   spp.ppstr("acct_type", colStr("C-acct_type"));
   spp.ppstr("txn_code", colStr("B-txn_code"));
   spp.ppstr("film_no", colStr("B-film_no"));
   spp.ppstr("acq_member_id", colStr("B-acq_member_id"));
   spp.ppnum("source_amt", colNum("B-source_amt"));
   spp.ppstr("source_curr", colStr("B-source_curr"));
   spp.ppnum("settl_amt", colNum("B-settl_amt"));
   //spp.ppstr("settl_flag",colStr("B-settl_flag"));
   spp.ppnum("dest_amt", colNum("B-dest_amt"));
   spp.ppstr("dest_curr", colStr("B-dest_curr"));
   spp.ppstr("mcht_eng_name", colStr("B-mcht_eng_name"));
   spp.ppstr("mcht_city", colStr("B-mcht_city"));
   spp.ppstr("mcht_country", colStr("B-mcht_country"));
   spp.ppstr("mcht_category", colStr("B-mcht_category"));
   spp.ppstr("mcht_zip", colStr("B-mcht_zip"));
   spp.ppstr("mcht_state", colStr("B-mcht_state"));
   spp.ppstr("mcht_no", colStr("B-mcht_no"));
   spp.ppstr("mcht_chi_name", colStr("B-mcht_chi_name"));
   spp.ppstr("auth_code", colStr("B-auth_code"));
   spp.ppstr("acct_month", colStr("B-acct_month"));
   spp.ppstr("bill_type", colStr("B-bill_type"));
   spp.ppstr("purchase_date", colStr("B-purchase_date"));
   spp.ppstr("post_date", colStr("B-post_date"));
   spp.ppstr("payment_type", colStr("B-payment_type"));
   spp.ppnum("curr_tx_amount", colNum("B-curr_tx_amount"));
   spp.ppint("install_tot_term1", colInt("B-install_tot_term1"));
   spp.ppnum("deduct_bp", colNum("B-deduct_bp"));
   spp.ppnum("cash_pay_amt", colNum("B-cash_pay_amt"));
   spp.ppnum("dc_dest_amt", colNum("B-dc_dest_amt"));
   spp.ppstr("curr_code", colStr("B-curr_code"));
   spp.ppstr("v_card_no", colStr("B-v_card_no"));
   spp.ppstr("contract_no", contrNo);
   spp.ppstr("reference_no_ori", refnoOri);
   spp.ppstr("sign_flag", colStr("B-sign_flag"));
   spp.ppstr("tpan_type", colStr("B-tpan_type"));
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_chgback err; kk[%s]", isReferNo);
   }

//	update_rsk_ctrlseqno_log();
   return rc;
}

void keyValue() {
   isReferNo = wp.itemStr("reference_no");
   referSeq = strToInt(wp.itemNvl("reference_seq", "0"));
//	if (this.ss_2int(kk2)==0)
//		kk2="1";
   //is_ctrl_seqno =wp.itemStr("ctrl_seqno");
}

public int rskm0210First() {
   ibDebit = zzComm.isDebit(wp.itemStr("debit_flag"));

   this.actionCode = "U";
   keyValue();

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Update("rsk_chgback");
   spp.ppstr("chg_stage", "1");
   spp.ppstr("sub_stage", "10");
   spp.ppstr("send_flag", "1");
   spp.ppint("chg_times", 1);
   spp.ppstr("fst_status", "10");
   spp.ppstr("fst_usage_code", wp.itemStr("fst_usage_code"));
   spp.ppstr("fst_reason_code", wp.itemStr("fst_reason_code"));
   spp.ppstr("fst_msg", wp.itemStr("fst_msg"));
   spp.ppstr("fst_doc_mark", wp.itemStr("fst_doc_mark"));
   spp.ppnum("fst_amount", wp.itemNum("fst_amount"));
   spp.ppnum("fst_twd_amt", wp.itemNum("fst_twd_amt"));
   spp.ppnum("fst_dc_amt", wp.itemNum("fst_dc_amt"));
   spp.ppstr("fst_part_mark", wp.itemStr("fst_part_mark"));
   spp.ppymd("fst_add_date");
   spp.ppstr("fst_add_user", modUser);
   spp.ppstr("fst_vcrcase_no", wp.itemStr("fst_vcrcase_no"));
   spp.modxxx(modUser, modPgm);
   spp.sql2Where(" where reference_no =?", isReferNo);
   spp.sql2Where(" and reference_seq =?", referSeq);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (rc <= 0) {
      return errUpdate("扣款(一扣)修改: ");
   }

//	update_rsk_ctrlseqno_log();
   return rc;
}

public int rskm0210Second(String aStage) {
   ibDebit = zzComm.isDebit(wp.itemStr("debit_flag"));

   this.actionCode = "U";
   keyValue();

   int liTime = 1;
   if (eq(aStage, "3"))
      liTime = 2;

   taroko.base.Parm2Sql tt = new Parm2Sql();
   tt.update("rsk_chgback");
   if (liTime == 1) {
      tt.parmSet("chg_stage", "3");
      tt.parmSet("sub_stage", "10");
      tt.parmSet("chg_times", 2);
      tt.funcSet("sec_status", "fst_status", "");
      tt.funcSet("sec_reverse_mark ", "fst_reverse_mark ", "");
      tt.funcSet("sec_reverse_date ", "fst_reverse_date ", "");
      tt.funcSet("sec_rebuild_mark ", "fst_rebuild_mark ", "");
      tt.funcSet("sec_rebuild_date ", "fst_rebuild_date ", "");
      tt.funcSet("sec_send_date    ", "fst_send_date    ", "");
      tt.funcSet("sec_send_cnt     ", "fst_send_cnt     ", "");
      tt.funcSet("sec_usage_code   ", "fst_usage_code   ", "");
      tt.funcSet("sec_reason_code  ", "fst_reason_code  ", "");
      tt.funcSet("sec_msg          ", "fst_msg          ", "");
      tt.funcSet("sec_doc_mark     ", "fst_doc_mark     ", "");
      tt.funcSet("sec_amount       ", "fst_amount       ", "");
      tt.funcSet("sec_twd_amt      ", "fst_twd_amt      ", "");
      tt.funcSet("sec_dc_amt       ", "fst_dc_amt       ", "");
      tt.funcSet("sec_part_mark    ", "fst_part_mark    ", "");
      tt.funcSet("sec_expire_date  ", "fst_expire_date  ", "");
      tt.funcSet("sec_add_date     ", "fst_add_date     ", "");
      tt.funcSet("sec_add_user     ", "fst_add_user     ", "");
      tt.funcSet("sec_apr_date     ", "fst_apr_date     ", "");
      tt.funcSet("sec_apr_user     ", "fst_apr_user     ", "");
      tt.funcSet("sec_disb_yn      ", "fst_disb_yn      ", "");
      tt.funcSet("sec_disb_amt     ", "fst_disb_amt     ", "");
      tt.funcSet("sec_disb_dc_amt  ", "fst_disb_dc_amt  ", "");
      tt.funcSet("sec_disb_add_date", "fst_disb_add_date", "");
      tt.funcSet("sec_disb_add_user", "fst_disb_add_user", "");
      tt.funcSet("sec_disb_apr_date", "fst_disb_apr_date", "");
      tt.funcSet("sec_disb_apr_user", "fst_disb_apr_user", "");
      tt.funcSet("sec_vcrcase_no   ", "fst_vcrcase_no", "");
   }

   tt.parmSet("send_flag", "1");
   tt.parmSet("send_apr_flag", "N");
   //-move fst_xxx to sec_xxx-

   //-set fst_xxx-
   double lmDcAmt2 = wp.itemNum("sec_dc_amt");
   double lmBillDc = wp.itemNum("BL_dc_dest_amt");
   double lmBillSetl = wp.itemNum("BL_settl_amt");
   double lmBillDest = wp.itemNum("BL_dest_amt");
   double lmPart = 1;
   double lmAmt = 0;
   if (lmDcAmt2 == lmBillDc) {
      tt.parmSet("fst_part_mark", "N");
   }
   else {
      tt.parmSet("fst_part_mark", "Y");
      lmPart = lmDcAmt2 / lmBillDc;
   }
   lmAmt = lmPart * lmBillSetl;
   tt.parmSet("fst_amount", lmAmt);
   lmAmt = lmPart * lmBillDest;
   tt.parmSet("fst_twd_amt ", lmAmt);
   tt.parmSet("fst_dc_amt ", lmDcAmt2);

   tt.parmSet("fst_status ", "10");
   tt.parmSet("fst_reverse_mark ", "");
   tt.parmSet("fst_reverse_date ", "");
   tt.parmSet("fst_rebuild_mark ", "");
   tt.parmSet("fst_rebuild_date ", "");
   tt.parmSet("fst_send_date ", "");
   tt.parmSet("fst_send_cnt ", 0);
   tt.parmSet("fst_usage_code ", wp.itemStr("sec_usage_code"));
   tt.parmSet("fst_reason_code ", wp.itemStr("sec_reason_code"));
   tt.parmSet("fst_msg ", wp.itemStr("sec_msg"));
   tt.parmSet("fst_doc_mark ", wp.itemStr("sec_doc_mark"));
   tt.parmSet("fst_expire_date ", "");
   tt.parmYmd("fst_add_date");
   tt.parmSet("fst_add_user", modUser);
   tt.parmSet("fst_apr_date", "");
   tt.parmSet("fst_apr_user", "");
   tt.parmSet("fst_disb_yn", "");
   tt.parmSet("fst_disb_amt", 0);
   tt.parmSet("fst_disb_dc_amt", 0);
   tt.parmSet("fst_disb_add_date", "");
   tt.parmSet("fst_disb_add_user", "");
   tt.parmSet("fst_disb_apr_date", "");
   tt.parmSet("fst_disb_apr_user", "");
   tt.parmSet("fst_vcrcase_no", wp.itemStr("sec_vcrcase_no"));
   tt.modxxxSet(modUser, modPgm);
   tt.whereParm(" where reference_no =?", isReferNo);
   tt.whereParm(" and reference_seq =?", referSeq);

   sqlExec(tt.getSql(), tt.getParms());
   if (rc <= 0) {
      return errUpdate("扣款(二扣)修改: ");
   }
   return rc;
}

private int rskm0210Second() throws Exception  {
   ibDebit = zzComm.isDebit(wp.itemStr("debit_flag"));

   this.actionCode = "U";
   keyValue();

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Update("rsk_chgback");
   spp.ppstr("chg_stage", "3");
   spp.ppstr("sub_stage", "10");
   spp.ppstr("send_flag", "1");
   spp.ppstr("send_apr_flag", "N");
   spp.ppint("chg_times", 2);
   //-move fst_xxx to sec_xxx-
   spp.addsql(", sec_status =fst_status ");
   spp.addsql(", sec_reverse_mark  =fst_reverse_mark ");
   spp.addsql(", sec_reverse_date  =fst_reverse_date ");
   spp.addsql(", sec_rebuild_mark  =fst_rebuild_mark ");
   spp.addsql(", sec_rebuild_date  =fst_rebuild_date ");
   spp.addsql(", sec_send_date     =fst_send_date    ");
   spp.addsql(", sec_send_cnt      =fst_send_cnt     ");
   spp.addsql(", sec_usage_code    =fst_usage_code   ");
   spp.addsql(", sec_reason_code   =fst_reason_code  ");
   spp.addsql(", sec_msg           =fst_msg          ");
   spp.addsql(", sec_doc_mark      =fst_doc_mark     ");
   spp.addsql(", sec_amount        =fst_amount       ");
   spp.addsql(", sec_twd_amt       =fst_twd_amt      ");
   spp.addsql(", sec_dc_amt        =fst_dc_amt       ");
   spp.addsql(", sec_part_mark     =fst_part_mark    ");
   spp.addsql(", sec_expire_date   =fst_expire_date  ");
   spp.addsql(", sec_add_date      =fst_add_date     ");
   spp.addsql(", sec_add_user      =fst_add_user     ");
   spp.addsql(", sec_apr_date      =fst_apr_date     ");
   spp.addsql(", sec_apr_user      =fst_apr_user     ");
   spp.addsql(", sec_disb_yn       =fst_disb_yn      ");
   spp.addsql(", sec_disb_amt      =fst_disb_amt     ");
   spp.addsql(", sec_disb_dc_amt   =fst_disb_dc_amt  ");
   spp.addsql(", sec_disb_add_date =fst_disb_add_date");
   spp.addsql(", sec_disb_add_user =fst_disb_add_user");
   spp.addsql(", sec_disb_apr_date =fst_disb_apr_date");
   spp.addsql(", sec_disb_apr_user =fst_disb_apr_user");
   spp.addsql(", sec_vcrcase_no    =fst_vcrcase_no");

   //-set fst_xxx-
   double lmDcAmt2 = wp.itemNum("sec_dc_amt");
   double lmBillDc = wp.itemNum("BL_dc_dest_amt");
   double lmBillSetl = wp.itemNum("BL_settl_amt");
   double lmBillDest = wp.itemNum("BL_dest_amt");
   double lmPart = 1;
   double lmAmt = 0;
   if (lmDcAmt2 == lmBillDc) {
      spp.ppstr("fst_part_mark", "N");
   }
   else {
      spp.ppstr("fst_part_mark", "Y");
      lmPart = lmDcAmt2 / lmBillDc;
   }
   lmAmt = lmPart * lmBillSetl;
   spp.ppnum("fst_amount", lmAmt);
   lmAmt = lmPart * lmBillDest;
   spp.ppnum("fst_twd_amt ", lmAmt);
   spp.ppnum("fst_dc_amt ", lmDcAmt2);

   spp.ppstr("fst_status ", "10");
   spp.ppstr("fst_reverse_mark ", "");
   spp.ppstr("fst_reverse_date ", "");
   spp.ppstr("fst_rebuild_mark ", "");
   spp.ppstr("fst_rebuild_date ", "");
   spp.ppstr("fst_send_date ", "");
   spp.ppint("fst_send_cnt ", 0);
   spp.ppstr("fst_usage_code ", wp.itemStr("sec_usage_code"));
   spp.ppstr("fst_reason_code ", wp.itemStr("sec_reason_code"));
   spp.ppstr("fst_msg ", wp.itemStr("sec_msg"));
   spp.ppstr("fst_doc_mark ", wp.itemStr("sec_doc_mark"));
   spp.ppstr("fst_expire_date ", "");
   spp.ppymd("fst_add_date");
   spp.ppstr("fst_add_user", modUser);
   spp.ppstr("fst_apr_date", "");
   spp.ppstr("fst_apr_user", "");
   spp.ppstr("fst_disb_yn", "");
   spp.ppnum("fst_disb_amt", 0);
   spp.ppnum("fst_disb_dc_amt", 0);
   spp.ppstr("fst_disb_add_date", "");
   spp.ppstr("fst_disb_add_user", "");
   spp.ppstr("fst_disb_apr_date", "");
   spp.ppstr("fst_disb_apr_user", "");
   spp.ppstr("fst_vcrcase_no", wp.itemStr("sec_vcrcase_no"));
   spp.modxxx(modUser, modPgm);
   spp.sql2Where(" where reference_no =?", isReferNo);
   spp.sql2Where(" and reference_seq =?", referSeq);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (rc <= 0) {
      return errUpdate("扣款(二扣)修改: ");
   }

//   update_rsk_ctrlseqno_log();
   return rc;
}

public int rskm0210SecondCancel() {
   ibDebit = zzComm.isDebit(wp.itemStr("debit_flag"));

   this.actionCode = "U";
   keyValue();

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Update("rsk_chgback");
   spp.ppstr("chg_stage", "2");
   spp.ppstr("sub_stage", "30");
   spp.ppstr("send_flag", "0");
   spp.ppstr("send_apr_flag", "");
   spp.ppint("chg_times", 1);
   //-move sec_xxx to fst_xxx-
   spp.addsql(", fst_status =sec_status ");
   spp.addsql(", fst_reverse_mark  =sec_reverse_mark ");
   spp.addsql(", fst_reverse_date  =sec_reverse_date ");
   spp.addsql(", fst_rebuild_mark  =sec_rebuild_mark ");
   spp.addsql(", fst_rebuild_date  =sec_rebuild_date ");
   spp.addsql(", fst_send_date     =sec_send_date    ");
   spp.addsql(", fst_send_cnt      =sec_send_cnt     ");
   spp.addsql(", fst_usage_code    =sec_usage_code   ");
   spp.addsql(", fst_reason_code   =sec_reason_code  ");
   spp.addsql(", fst_msg           =sec_msg          ");
   spp.addsql(", fst_doc_mark      =sec_doc_mark     ");
   spp.addsql(", fst_amount        =sec_amount       ");
   spp.addsql(", fst_twd_amt       =sec_twd_amt      ");
   spp.addsql(", fst_dc_amt        =sec_dc_amt       ");
   spp.addsql(", fst_part_mark     =sec_part_mark    ");
   spp.addsql(", fst_expire_date   =sec_expire_date  ");
   spp.addsql(", fst_add_date      =sec_add_date     ");
   spp.addsql(", fst_add_user      =sec_add_user     ");
   spp.addsql(", fst_apr_date      =sec_apr_date     ");
   spp.addsql(", fst_apr_user      =sec_apr_user     ");
   spp.addsql(", fst_disb_yn       =sec_disb_yn      ");
   spp.addsql(", fst_disb_amt      =sec_disb_amt     ");
   spp.addsql(", fst_disb_dc_amt   =sec_disb_dc_amt  ");
   spp.addsql(", fst_disb_add_date =sec_disb_add_date");
   spp.addsql(", fst_disb_add_user =sec_disb_add_user");
   spp.addsql(", fst_disb_apr_date =sec_disb_apr_date");
   spp.addsql(", fst_disb_apr_user =sec_disb_apr_user");
   spp.addsql(", fst_vcrcase_no    =sec_vcrcase_no");
   //-set fst_xxx-
   spp.ppstr("sec_status ", "");
   spp.ppstr("sec_reverse_mark ", "");
   spp.ppstr("sec_reverse_date ", "");
   spp.ppstr("sec_rebuild_mark ", "");
   spp.ppstr("sec_rebuild_date ", "");
   spp.ppstr("sec_send_date ", "");
   spp.ppint("sec_send_cnt ", 0);
   spp.ppstr("sec_usage_code ", "");
   spp.ppstr("sec_reason_code ", "");
   spp.ppstr("sec_msg ", "");
   spp.ppstr("sec_doc_mark ", "");
   spp.ppnum("sec_amount ", 0);
   spp.ppnum("sec_twd_amt ", 0);
   spp.ppnum("sec_dc_amt ", 0);
   spp.ppstr("sec_part_mark ", "");
   spp.ppstr("sec_expire_date ", "");
   spp.ppymd("sec_add_date");
   spp.ppstr("sec_add_user", "");
   spp.ppstr("sec_apr_date", "");
   spp.ppstr("sec_apr_user", "");
   spp.ppstr("sec_disb_yn", "");
   spp.ppnum("sec_disb_amt", 0);
   spp.ppnum("sec_disb_dc_amt", 0);
   spp.ppstr("sec_disb_add_date", "");
   spp.ppstr("sec_disb_add_user", "");
   spp.ppstr("sec_disb_apr_date", "");
   spp.ppstr("sec_disb_apr_user", "");
   spp.ppstr("sec_vcrcase_no", "");
   spp.modxxx(modUser, modPgm);
   spp.sql2Where(" where reference_no =?", isReferNo);
   spp.sql2Where(" and reference_seq =?", referSeq);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (rc <= 0) {
      return errUpdate("扣款(二扣)修改: ");
   }

   //update_rsk_ctrlseqno_log();
   return rc;
}

public int rskm0210Repsent() throws Exception  {
   ibDebit = zzComm.isDebit(wp.itemStr("debit_flag"));

   this.actionCode = "U";
   keyValue();
   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Update("rsk_chgback");

   spp.ppstr("rep_msg", wp.itemStr("rep_msg"));
   spp.modxxx(modUser, modPgm);
   spp.sql2Where(" where reference_no =?", isReferNo);
   spp.sql2Where(" and reference_seq =?", referSeq);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (rc <= 0) {
      return errUpdate("再提示修改: ");
   }

//   update_rsk_ctrlseqno_log();
   return rc;
}

public int rskp0210Update(String a_rowid) {

   strSql = "update rsk_chgback set"
         + " sub_stage ='30'"
         + ", send_flag ='1'"
         + ", fst_status ='30'"
         + ", fst_send_date =" + commSqlStr.sysYYmd
         + ", fst_expire_date =uf_rsk_stage_days(bin_type,txn_code,'1','') "
         + ", fst_apr_date =" + commSqlStr.sysYYmd
         + ", fst_apr_user =?"
         + " ," + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";

   setParm(1, modUser);
   this.setRowId(2, a_rowid);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback error; " + this.getMsg());
   }

   //update_rsk_ctrlseqno_log();
   return rc;
}

public int dataDelete() {
   actionCode = "D";

   isCtrlSeqno = wp.colStr("ctrl_seqno");
   isReferNo = wp.colStr("reference_no");

   strSql = "delete rsk_chgback "
         + " where rowid =? "
         + " and mod_seqno =?"
   //+" and bin_type =?"
   ;
   //setString("rowid",wp.itemStr("rowid"));

   sqlExec(strSql, new Object[]{commSqlStr.strToRowid(wp.colStr("rowid")),wp.colNum("mod_seqno")
   });
   if (sqlRowNum <= 0) {
      return errUpdate("扣款維護:");
   }

   //update_rsk_ctrlseqno_log();
   return rc;
}


void getCtrlSeqno(String aRefno) {
   RskCtrlseqno lolog = new RskCtrlseqno();
   lolog.setConn(wp);

   String ss = lolog.getCtrlSeqno(isReferNo);
//   wp.colSet("ctrl_seqno",ss);
   if (empty(ss)) {
      errmsg("無法取得控制流水號");
   }

   isCtrlSeqno = ss;
}

//void update_rsk_ctrlseqno_log() {
//   if (empty(is_refer_no)) {
//      is_refer_no =wp.itemStr("reference_no");
//   }
//   if (empty(is_ctrl_seqno)) {
//      is_ctrl_seqno =wp.itemStr("ctrl_seqno");
//   }
//   Rsk_ctrlseqno_log oolog=new Rsk_ctrlseqno_log();
//   oolog.setConn(wp);
//   oolog.is_refer_no =is_refer_no;
//   oolog.is_ctrl_seqno =is_ctrl_seqno;
//   if (oolog.update_Chgback()!=1) {
//      errmsg(oolog.getMsg());
//   }
//}


}
