package rskm01;
/**
 * 調單資料檔公用程式
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-1002:	JH		select_bil_bill()
 * 2018-0131:	JH		+VD dbb_bill, dbc_card, dba_acno
 */

import busi.FuncBase;

public class RskReceipt extends FuncBase {
busi.CommBusi zzComm = new busi.CommBusi();

public boolean checkCardBase=false;
public String isCtrlSeqno = "";
public String isReferNo = "";
public String isRowid = "";
private String refnoOri="", contrNo="";

int errUpdate(String strName) {
   if (isAdd())
      errmsg(strName + " insert RSK_RECEIPT error; " + sqlErrtext);
   else if (isDelete())
      errmsg(strName + " delete RSK_RECEIPT error; " + sqlErrtext);
   else errmsg(strName + " update RSK_RECEIPT error; " + sqlErrtext);
   return rc;
}

public int dataSelect(String referNo, int referSeq) throws Exception  {
   strSql = "select A.*,"
         + " uf_nvl(debit_flag,'N') as debit_flag,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt(curr_code,dest_amt,dc_dest_amt) as dc_dest_amt,"
         + " uf_idno_name(id_p_seqno) as db_chi_name,"
         + " hex(rowid) as rowid"
         + " from rsk_receipt A"
         + " where 1=1"
         + " and reference_no =?"
         + " and  reference_seq =?";
   setString(1, referNo);
   setInt(2, referSeq);
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      return 0;
   }
   this.colDataToWpCol("");

   return sqlRowNum;
}

boolean debitCard() {
   return zzComm.isDebit(wp.colStr("debit_flag"));
}

void getRefnoOri(String aRefno) {
   isReferNo = aRefno;
   refnoOri = aRefno;
   contrNo = "";
   if (debitCard())
      return;

   strSql = "select A.contract_no"
         + ", uf_refno_ori(A.contract_no,A.reference_no_original) as refno_ori"
         + " from bil_bill A"
         + " where A.reference_no =?"
   ;
   setString(1, aRefno);
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

void selectBilBill(String aRefno) {
   getRefnoOri(aRefno);
   String lsReferNo = refnoOri;

   daoTid = "B-";
   String sql1 = "select A.* "
         + ", uf_rsk_stage_days(A.bin_type,A.txn_code,'R','') as last_return_date"
         ;
   if (debitCard()) {
      sql1 += ", 0 as dc_dest_amt"
            + ", '901' as curr_code"
            + ", '' as v_card_no"
            + ", '' as tpan_type"
            + ", '' as refno_ori"
      ;
      sql1 += " from dbb_bill A";
   }
   else {
      sql1 += ", A.reference_no_original as reference_no_ori";
      sql1 += " from bil_bill A";
   }
   sql1 += " where reference_no =? ";
   sqlSelect(sql1, new Object[]{lsReferNo});

   //-原始帳單-
   if (sqlRowNum == 0) {
      daoTid = "B-";
      sql1 = "select A.*,"
            + "	A.reference_no_original as reference_no_ori "
            + ", uf_rsk_stage_days(A.bin_type,A.txn_code,'R','') as last_return_date "
      ;
      if (debitCard())
         sql1 += " from dbb_bill";
      else sql1 += " from bil_curpost A";
      sql1 += " where reference_no =? ";
      sqlSelect(sql1, new Object[]{lsReferNo});
   }
   if (sqlRowNum <= 0) {
      errmsg("select BIL[DBB]_BILL(curpost) no-find, ref-no=" + lsReferNo);
      return;
   }

   if (debitCard()==false) {
      if (colEmpty("B-reference_no_ori")) {
         colSet("B-reference_no_ori", wp.itemStr("reference_no_ori"));
      }
      if (colEmpty("B-contract_no")) {
         colSet("B-contract_no", wp.itemStr("contract_no"));
      }
   }
   
   if (colEq("B-bill_type", "FIFC")) {	   
	   errmsg("費用類交易: 不可列問交");
	   return;
   }
   
}

void selectCrdCard() {
   daoTid = "C-";

   String sql1 = "select major_id_p_seqno, corp_p_seqno, bin_type";
   if (debitCard())
      sql1 += " from dbc_card";
   else sql1 += " from crd_card ";
   sql1 += " where card_no =?";
   sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});

   if (checkCardBase && sqlRowNum <= 0) {
//      errmsg("select CRD_CARD no-find");
   }
}

public int checkReasonCode() {
   reasonCodeDeft();
   String strName = wp.itemStr("reason_code");
   if (wp.itemEq("bin_type", "J") && strName.length() != 4) {
      errmsg("JCB-調單理由碼為4碼");
      return rc;
   }
   if (wp.itemEq("bin_type", "V") && strName.length() != 2) {
      errmsg("VISA-調單理由碼為2碼");
      return rc;
   }
   if (wp.itemEq("bin_type", "M") && strName.length() != 4) {
      errmsg("Master-調單理由碼為4碼");
      return rc;
   }

   return rc;
}

private void reasonCodeDeft() {
   if (!wp.itemEmpty("reason_code"))
      return;
   if (wp.itemEq("bin_type", "V"))
      wp.itemSet("reason_code", "30");
   else if (wp.itemEq("bin_type", "M"))
      wp.itemSet("reason_code", "6321");
   else if (wp.itemEq("bin_type", "J"))
      wp.itemSet("reason_code", "0005");
}

public int rskm0110Insert() {
   msgOK();
   this.actionCode = "A";
//	is_ctrl_seqno =wp.colStr("ctrl_seqno");
   isReferNo = wp.colStr("reference_no");

   //--
   selectBilBill(isReferNo);
   if (rc != 1)
      return rc;
   selectCrdCard();
   if (rc != 1)
      return rc;
   if (colEmpty("B-bin_type")) {
      colSet("B-bin_type", colStr("C-bin_type"));
   }

   reasonCodeDeft();

   //-get控制流水號-
   RskCtrlseqno lolog = new RskCtrlseqno();
   lolog.setConn(wp);
   isCtrlSeqno = lolog.getCtrlSeqno(isReferNo);
   if (empty(isCtrlSeqno)) {
      errmsg(lolog.mesg());
      return rc;
   }

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("rsk_receipt");
   spp.ppstr("reference_no", isReferNo);
   spp.ppint("reference_seq", 0);
   spp.ppstr("bin_type", colStr("B-bin_type"));
   spp.ppstr("ctrl_seqno", isCtrlSeqno);
   spp.ppstr("debit_flag", wp.itemStr("debit_flag"));
   spp.ppint("rept_seqno", 1);
   spp.ppstr("rept_status", "30");
   spp.ppstr("reject_mark", wp.itemStr("reject_mark"));
   spp.ppstr("reject_date", wp.itemStr("reject_date"));
   spp.ppstr("send_flag", "1");
   spp.ppstr("rept_type", wp.itemStr("rept_type"));
   spp.ppstr("reason_code", wp.itemStr("reason_code"));
   spp.ppstr("last_return_date", colStr("B-last_return_date"));
   spp.ppymd("add_date");
   spp.ppstr("add_user", modUser);
   spp.ppstr("card_no", colStr("B-card_no"));
   spp.ppstr("id_p_seqno", colStr("B-id_p_seqno"));
   spp.ppstr("major_id_p_seqno", colStr("C-major_id_p_seqno"));
   spp.ppstr("corp_p_seqno", colStr("C-corp_p_seqno"));
   spp.ppstr("p_seqno", colStr("B-p_seqno"));
   spp.ppstr("acct_type", colStr("B-acct_type"));
   spp.ppstr("txn_code", colStr("B-txn_code"));
   spp.ppstr("film_no", colStr("B-film_no"));
   spp.ppstr("acq_member_id", colStr("B-acq_member_id"));
   spp.ppnum("source_amt", colNum("B-source_amt"));
   spp.ppstr("source_curr", colStr("B-source_curr"));
   spp.ppnum("settl_amt", colNum("B-settl_amt"));
   spp.ppnum("dest_amt", colNum("B-dest_amt"));
   spp.ppstr("dest_curr", colStr("B-dest_curr"));
   spp.ppstr("mcht_eng_name", colStr("B-mcht_eng_name"));
   spp.ppstr("mcht_city", colStr("B-mcht_city"));
   spp.ppstr("mcht_country", colStr("B-mcht_country"));
   spp.ppstr("mcht_category", colStr("B-mcht_category"));
   spp.ppstr("mcht_zip", colStr("B-mcht_zip"));
   spp.ppstr("mcht_no", colStr("B-mcht_no"));
   spp.ppstr("mcht_chi_name", colStr("B-mcht_chi_name"));
   spp.ppstr("auth_code", colStr("B-auth_code"));
   spp.ppstr("acct_month", colStr("B-acct_month"));
   spp.ppstr("bill_type", colStr("B-bill_type"));
   spp.ppstr("purchase_date", colStr("B-purchase_date"));
   spp.ppstr("process_date", colStr("B-process_date"));
   spp.ppstr("post_date", colStr("B-post_date"));
   spp.ppstr("payment_type", colStr("B-payment_type"));
   spp.ppnum("dc_dest_amt", colNum("B-dc_dest_amt"));
   spp.ppstr("curr_code", colStr("B-curr_code"));
   spp.ppstr("v_card_no", colStr("B-v_card_no"));
   spp.ppstr("tpan_type", colStr("B-tpan_type"));
   spp.ppstr("contract_no", colStr("B-contract_no"));
   spp.ppstr("reference_no_ori", colStr("B-reference_no_ori"));
   spp.modxxx(modUser, modPgm);

//	wp.ddd(spp.sql_stmt(),spp.sql_parm());
   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      sqlErr("調單新增: ");
      return rc;
   }

//	update_rsk_ctrlseqno_log();
//	if (rc ==-1)
//	   return rc;

   wp.colSet("ctrl_seqno", isCtrlSeqno);
   return rc;
}

public int rskm0110Update() {
   actionCode = "U";

   reasonCodeDeft();

   strSql = "update rsk_receipt set"
         + " rept_type =? "
         + ", reason_code =? " +
         commSqlStr.modxxxSet(modUser, modPgm)
         + " where rowid =? "
   ;

   setString(wp.itemStr("rept_type"));
   setString(wp.itemStr("reason_code"));
   setRowId(wp.itemStr("rowid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("調單維護: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

/*-- moveTo rskm0120_func 
public int rskm0120_Update() {
	actionCode ="U";

	strSql ="update rsk_receipt set"
			+" rept_status =:rept_status "
			+", proc_result =:proc_result "
			+", recv_date =:recv_date "
			+", fees_flag =:fees_flag "
			+", fees_amt =:fees_amt "
			+", close_add_user =:close_add_user "
			+", close_add_date =:close_add_date "
			+", mod_user =:mod_user "
			+", mod_time =sysdate "
			+", mod_pgm =:mod_pgm "
			+", mod_seqno =mod_seqno+1 "
			+" where 1=1 "
			+sqlcond.where_rowid(vars_ss("rowid"))
			;
	
	item2Parm_ss("rowid");
	item2Parm_ss("rept_status");
	item2Parm_ss("proc_result");
	item2Parm_ss("recv_date");
	item2Parm_ss("fees_flag");
	item2Parm_num("fees_amt");
	item2Parm_ss("close_add_date");
	item2Parm_ss("close_add_user");
	setString("mod_user",mod_user);
	setString("mod_pgm",mod_pgm);

	sqlExec(strSql);
	if (sqlRowNum<=0) {
		return err_update("調單結案: ");
	}
	
	return rc;
}
--*/

public int rskp0120Update() {
   actionCode = "U";

   if (empty(isRowid)) {
      errmsg("update Key=rowid, is Empty");
      return -1;
   }

   strSql = "update rsk_receipt set"
         + " rept_status ='80' "
         + ", close_apr_user = ? "
         + ", close_apr_date =" + commSqlStr.sysYYmd
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         + " and rowid =?"
//			+commSqlStr.where_rowid(is_rowid)
   ;

   //setString("rowid",aa_rowid[ll]);
   setString(modUser);
   setRowId(isRowid);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      return errUpdate("調單結案覆核: ");
   }

//   update_rsk_ctrlseqno_log();

   return rc;
}

public int dataDelete() {
   actionCode = "D";

   isReferNo = wp.colStr("reference_no");
   isCtrlSeqno = wp.colStr("ctrl_seqno");

   strSql = "delete rsk_receipt "
         + " where rowid =? "
         + " and mod_seqno =?"
   //+" and bin_type =?"
   ;
   //setString("rowid",wp.itemStr("rowid"));

   sqlExec(strSql, new Object[]{commSqlStr.strToRowid(wp.colStr("rowid")),wp.colNum("mod_seqno")});
   if (sqlRowNum <= 0) {
      return errUpdate("調單刪除:");
   }

//   update_rsk_ctrlseqno_log();
   return rc;
}

//void update_rsk_ctrlseqno_log() {
//   if (empty(is_refer_no)) {
//      is_refer_no =wp.itemStr("reference_no");
//   }
//   if (empty(is_ctrl_seqno)) {
//      is_ctrl_seqno =wp.itemStr("ctrl_seqno");
//   }
//
//   Rsk_ctrlseqno_log oolog=new Rsk_ctrlseqno_log();
//   oolog.setConn(wp);
//   oolog.is_refer_no =is_refer_no;
//   oolog.is_ctrl_seqno =is_ctrl_seqno;
//   if (oolog.update_Receipt()!=1) {
//      errmsg(oolog.getMsg());
//   }
//}

}
