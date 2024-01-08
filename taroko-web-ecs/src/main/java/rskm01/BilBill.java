package rskm01;

import busi.func.CrdFunc;

/**
 * 帳單公用程式
 * 2019-1223:  Alex  add getChinese
 * 2019-1031   JH    ++qr_flag, onus_pf, mcht_pan
 * 2019-0905:  JH    select_xxx_idno()
 * 2019-0325:  JH    sql.uf_refno_ori()
 */

//import java.sql.Connection;

public class BilBill extends busi.FuncBase {


//public String[] aa_acct_code=new String[6];

private boolean ibDebit = false;
private String idPseqNo = "";

public void debitFlag(String s1) {
   ibDebit = s1.equalsIgnoreCase("Y");
}

public boolean isOverseaFee(String is_refno, String is_refno_ori) {
   if (empty(is_refno) && empty(is_refno_ori)) {
      return false;
   }
   if (ibDebit) {
      strSql = "select count(*) as db_cnt"
            + " from dbb_bill"
            + " where reference_no =?"
            + " and nvl(reference_no_fee_f,'') <>''";
      setString(1, is_refno);
   }
   else {
      strSql = "select count(*) as db_cnt"
            + " from bil_bill"
            + " where reference_no =?"
            + " and nvl(reference_no_fee_f,'') <>''";
      if (empty(is_refno_ori)) {
         setString(1, is_refno);
      }
      else setString(1, is_refno_ori);
   }
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      return false;
   }
   return colNum("db_cnt") > 0;
}

public int billCurpost() throws Exception  {
   if (varEmpty("reference_no")) {
      errmsg("帳單參考號碼: 不可空白");
      return -1;
   }

   //-exist in bil_bill-
   if (dataSelect() == 1)
      return 1;

   //-select_bil_curpost-
   if (selectBilCurpost() != 1)
      return -1;

   return 1;
}

public int dataSelect(String k_refer_no, String k_debit_flag) {
   varsSet("reference_no", k_refer_no);
   varsSet("debit_flag", k_debit_flag);
   return dataSelect();
}
public int billDataOri(String a_refno) throws Exception {
   if (wpIsNull())
      return -1;

   if (empty(a_refno)) {
      errmsg("帳單參考號碼: 不可空白");
      return -1;
   }

   strSql = "select A.* "
         + " from bil_bill A"
         + " where A.reference_no =?";
   sqlSelect(strSql,a_refno);
   if (sqlRowNum == 0) {
      strSql = "select A.*"
            + ", A.dc_amount as dc_dest_amt"
      +" from bil_curpost A where A.reference_no =?"
      ;
      sqlSelect(strSql,a_refno);
   }
   if (sqlRowNum <=0) return 0;
   //-move to BILL-
   wp.colSet("BL_bill_type",colStr("bill_type"));
   wp.colSet("BL_txn_code",colStr("txn_code"));
   wp.colSet("BL_film_no            ", colStr("film_no            "));
   wp.colSet("BL_acq_member_id      ", colStr("acq_member_id      "));
   wp.colSet("BL_dest_amt           ", colNum("dest_amt           "));
   wp.colSet("BL_dest_curr          ", colStr("dest_curr          "));
   wp.colSet("BL_source_amt         ", colNum("source_amt         "));
   wp.colSet("BL_source_curr        ", colStr("source_curr        "));
   wp.colSet("BL_dc_dest_amt        ", colNum("dc_dest_amt        "));
   wp.colSet("BL_dc_exchange_rate   ", colNum("dc_exchange_rate   "));
   //wp.colSet("BL_dc_curr_adjust_amt ", colNum("dc_curr_adjust_amt "));
   wp.colSet("BL_settl_amt          ", colNum("settl_amt          "));
   wp.colSet("BL_mcht_eng_name      ", colStr("mcht_eng_name      "));
   wp.colSet("BL_mcht_city          ", colStr("mcht_city          "));
   wp.colSet("BL_mcht_country       ", colStr("mcht_country       "));
   wp.colSet("BL_mcht_category      ", colStr("mcht_category      "));
   wp.colSet("BL_mcht_zip           ", colStr("mcht_zip           "));
   wp.colSet("BL_mcht_state         ", colStr("mcht_state         "));
   wp.colSet("BL_mcht_no            ", colStr("mcht_no            "));
   wp.colSet("BL_mcht_chi_name      ", colStr("mcht_chi_name      "));
   wp.colSet("BL_mcht_zip_tw        ", colStr("mcht_zip_tw        "));
   wp.colSet("BL_mcht_type          ", colStr("mcht_type          "));
   wp.colSet("BL_auth_code          ", colStr("auth_code          "));
   wp.colSet("BL_purchase_date      ", colStr("purchase_date      "));
   wp.colSet("BL_acct_code          ", colStr("acct_code          "));
   wp.colSet("BL_payment_type       ", colStr("payment_type       "));
   wp.colSet("BL_ucaf               ", colStr("ucaf               "));
   wp.colSet("BL_ec_ind             ", colStr("ec_ind             "));
   wp.colSet("BL_electronic_term_ind", colStr("electronic_term_ind"));
   wp.colSet("BL_terminal_id        ", colStr("terminal_id        "));   
   return rc;
}

public int dataSelect() {
   if (wpIsNull())
      return -1;

   if (varEmpty("reference_no")) {
      errmsg("帳單參考號碼: 不可空白");
      return -1;
   }
   debitFlag(varsStr("debit_flag"));
   String ls_refer_no = varsStr("reference_no");

   this.daoTid = "BL_";
   if (!ibDebit) {
      //-select bil_bill-
      strSql = "select A.*,"
            + " uf_acno_key(A.p_seqno) as acct_key "
            + ", 'N' as debit_flag"
            + ", uf_refno_ori(contract_no,reference_no_original) as reference_no_ori"
            + ", decode(sign_flag,'-',-1,1) as tx_sign"
            + ", uf_dc_curr(curr_code) as curr_code "
            + ", uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt "
            + ", decode(A.bin_type,'',uf_bin_type(A.card_no),A.bin_type) as bin_type2"
            + " from bil_bill A"
            + " where A.reference_no =?"
      ;
   }
   else {
      //-select bil_bill-
      strSql = "select A.*"
            + ", uf_vd_acno_key(A.p_seqno) as acct_key "
            + ", 'Y' as debit_flag"
            + ", '' as reference_no_ori"
            + ", decode(sign_flag,'-',-1,1) as tx_sign"
            + ", dest_amt as dc_dest_amt "
            + ", dest_curr as curr_code "
            + ", decode(A.bin_type,'',uf_bin_type(A.card_no),A.bin_type) as bin_type2"
            + " from dbb_bill A"
            + " where A.reference_no =?"
      ;
   }
   setString(1, ls_refer_no);
   sqlSelect(strSql);
   if (sqlRowNum == 0) {
      strSql = "select A.*"
            + ", A.reference_no_original as reference_no_ori "
            + ", decode(sign_flag,'-',-1,1) as tx_sign"
            + ", dest_curr as curr_code "
            + ", decode(A.bin_type,'',uf_bin_type(A.card_no),A.bin_type) as bin_type2"
      ;
      if (ibDebit) {
         strSql +=", dest_amt as dc_dest_amt"+
               " from dbb_curpost A";
      }
      else {
         strSql +=", dc_amount as dc_dest_amt"+
               " from bil_curpost A";
      }
      strSql += " where reference_no =? ";

      this.daoTid = "BL_";
      sqlSelect(strSql, new Object[]{
            ls_refer_no
      });
   }

   if (sqlRowNum <= 0) {
      wp.notFound = "Y";
      errmsg("帳單資料不存在; ref_no=" + varsStr("reference_no"));
      return -1;
   }
   if (colEmpty("BL_bin_type"))
      colSet("BL_bin_type", colStr("BL_bin_type2"));
   this.colDataToWpCol("BL_");
   wp.colSet("debit_flag", (ibDebit ? "Y" : "N"));

   selectBilNccc300Dtl();
   selectXxxIdno();
   ttPaymentType();

   //--
   String ls_qr_flag =wp.colStr("BL_qr_flag");
   //String ss =zzstr.decode(ls_qr_flag,"t,一維被掃(t),Q,台灣Pay收單主掃(Q),Y,台灣Pay主掃(Y),q,台灣Pay/兆豐Pay繳費(q),A,台灣Pay繳稅(A),H,兆豐Pay繳稅(H)");
   wp.colSet("BL_qr_flag",ecsfunc.DeCodeBil.qrFlag(ls_qr_flag));

   return 1;
}

void ttPaymentType() {
   //--紅利積點-繳款方式
   String sql1 = " select wf_desc from ptr_sys_idtab where wf_type = 'BIL_PAYMENT_TYPE' and wf_id = ? ";
   sqlSelect(sql1, new Object[]{wp.colStr("BL_payment_type")});
   if (sqlRowNum > 0) {
      wp.colSet("BL_payment_type_chi", colStr("wf_desc"));
   }
}

void selectXxxIdno() {
   strSql = "select id_no, chi_name from crd_idno" +
         " where id_p_seqno =?";
   if (ibDebit) {
      strSql = "select id_no, chi_name from dbc_idno" +
            " where id_p_seqno =?";
   }
   setString(1, wp.colStr("BL_id_p_seqno"));
   sqlSelect(strSql);
   if (sqlRowNum > 0) {
      wp.colSet("BL_id_no", colStr("id_no"));
      wp.colSet("BL_chi_name", colStr("chi_name"));
   }
}

int selectBilCurpost() throws Exception  {
   this.daoTid = "BL_";
   //-select bil_bill-
   if (ibDebit) {
      strSql = "select "
            + " uf_VD_acno_key(A.p_seqno) as acct_key "
            + ", 'Y' as debit_flag"
            + ", '' as reference_no_ori"
            + ", dest_amt as dc_dest_amt"
            + ", A.*"
            + " from dbb_curpost A"
            + " where A.reference_no =:refer_no"
      ;
   }
   else {
      strSql = "select "
            + " uf_acno_key(A.p_seqno) as acct_key "
            + ", 'N' as debit_flag"
            + ", reference_no_original as reference_no_ori"
            + ", dc_amount as dc_dest_amt"
            + ", A.*"
            + " from bil_curpost A"
            + " where A.reference_no =:refer_no"
      ;
   }
   setString("refer_no", varsStr("reference_no"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      wp.notFound = "Y";
      errmsg("帳單[bil_curpost]資料不存在; ref_no=" + varsStr("reference_no"));
      return -1;
   }

   colDataToWpCol("BL_");

   selectBilNccc300Dtl();

   return 1;
}

int selectBilNccc300Dtl() {
   strSql = "select "
         + " tmp_request_flag , "
         + " tmp_service_code , "
         + " usage_code , "
         + " reason_code , "
         + " settlement_flag , "
         + " electronic_term_ind , "
         + " pos_term_capability , "
         + " pos_pin_capability , "
         + " reimbursement_attr , "
         + " second_conversion_date , "
         + " exchange_rate , "
         + " exchange_date , "
         + " ec_ind , "
         + " original_no , "
         + " transaction_source , "
         + " terminal_ver_results , "
         + " transaction_type , "
         + " auth_response_code , "
         + " chip_condition_code , "
         + " terminal_cap_pro , "
         + " iad_result , "
         + " acce_fee , "
         + " acce_fee_in_bc , "
         + " add_acct_type , "
         + " add_amt_type , "
         + " add_curcy_code , "
         + " add_amt_sign , "
         + " add_amt , "
         + " terminal_id , "
         + " bnet_ref_num , "
         + " de22  " +
         ", mcht_pan, onus_pf"
         + " from bil_nccc300_dtl"
         + " where reference_no =:refer_no"
   ;

   setString("refer_no", varsStr("reference_no"));

   daoTid = "BL_";
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
//		errmsg("select bil_nccc300_dtl no-find");
      return 1;
   }
   //col2wpCol("bill_category");
   col2wpCol("BL_tmp_request_flag");
   col2wpCol("BL_tmp_service_code");
   col2wpCol("BL_usage_code");
   col2wpCol("BL_reason_code");
   col2wpCol("BL_settlement_flag");
   col2wpCol("BL_electronic_term_ind");
   col2wpCol("BL_pos_term_capability");
   col2wpCol("BL_pos_pin_capability");
   col2wpCol("BL_reimbursement_attr");
   col2wpCol("BL_second_conversion_date");
   col2wpCol("BL_exchange_rate");
   col2wpCol("BL_exchange_date");
   col2wpCol("BL_ec_ind");
   col2wpCol("BL_original_no");
   col2wpCol("BL_transaction_source");
   col2wpCol("BL_terminal_ver_results");
   col2wpCol("BL_transaction_type");
   col2wpCol("BL_auth_response_code");
   col2wpCol("BL_chip_condition_code");
   col2wpCol("BL_terminal_cap_pro");
   col2wpCol("BL_iad_result");
   col2wpCol("BL_acce_fee");
   col2wpCol("BL_acce_fee_in_bc");
   col2wpCol("BL_add_acct_type");
   col2wpCol("BL_add_amt_type");
   col2wpCol("BL_add_curcy_code");
   col2wpCol("BL_add_amt_sign");
   col2wpCol("BL_add_amt");
   col2wpCol("BL_terminal_id");
   col2wpCol("BL_bnet_ref_num");
   col2wpCol("BL_de22");
   //col2wpCol("BL_qr_flag");
   col2wpCol("BL_mcht_pan");
   col2wpCol("BL_onus_pf");
   return 1;
}

void getIdnoPseqno(String ls_idno) throws Exception  {
//	String sql1="select id_p_seqno from crd_card"
//			+" where id_no =?";
//	setString(1,ls_idno);
//	this.sqlSelect(sql1);
//	if (sqlRowNum<=0) {
//		errmsg("不是本行卡友; id="+ls_idno);
//		return; 
//	}
   CrdFunc ooo = new CrdFunc();
   ooo.setConn(wp);
   idPseqNo = ooo.idnoToPseqno(ls_idno);
   if (empty(idPseqNo)) {
      errmsg(ooo.getMsg());
   }
}

public int cmsM3120Bill(String as_card_purch_flag, String as_card_type, String as_group_code) throws Exception  {
//--pp-card 申請條件
//--card_purch_flag: 1.正附卡合併, 2.正附卡分開
//--Parm-IN: is_idno, is_date1, is_date2, is_it_type, im_low_amt, 
//					bl,it,id,ca,ao,ot
//--Parm-out:	ii_cnt, im_amt=0;
//=====================================================================
   String ls_id_pseqno = varsStr("id_p_seqno");
   String ls_date1 = varsStr("is_date1");
   String ls_date2 = varsStr("is_date2");

   if (empty(as_card_purch_flag))
      as_card_purch_flag = "1";

   if (empty(ls_id_pseqno)) {
      errmsg("身分證ID 不可空白");
      return rc;
   }
//	get_idno_pseqno(ls_idno);
//	if (rc!=1) {
//		return rc;
//	}

   if (empty(as_card_type) || empty(as_group_code)) {
      errmsg("卡種, 團體代號 不可空白");
      return rc;
   }
   if (empty(ls_date1) || empty(ls_date2)) {
      errmsg("讀取起迄期間 不可空白");
      return rc;
   }
   strSql = "SELECT  count(*) as db_cnt,"
         + " sum("
         + "  decode(a.acct_code,'IT',"
         + "    decode(cast(:is_it_type as varchar(1)),'2',"      //:is_it_type,'2',"
         + "    decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
         + "    decode(b.refund_apr_flag,'Y',0,a.dest_amt)),"
         + "    decode(A.sign_flag,'-',-1,1) * A.dest_amt) ) as db_amt"
         + " from bil_bill A left join bil_contract B "
         + "		on A.contract_no =B.contract_no and A.contract_seq_no =B.contract_seq_no"
         + " where 1=1"
         + " and A.card_no in ("
         + " select card_no from crd_card where major_id_p_seqno =:ls_id_pseqno"
         + " and decode(cast(:ls_card_flag as varchar(1)),'1',major_id_p_seqno,id_p_seqno) =:ls_id_pseqno"
         + " and card_type =:ls_card_type and group_code =:ls_group_code"
         + " )"
         //-- 消費資料 六大本金類 --
         + " and A.acct_code in " + condAcctCode()
         //-- 消費資料 最低單筆金額 --
         + " and ( A.sign_flag ='-' or (A.sign_flag='+' and A.dest_amt>=:im_low_amt) )"
         //--消費資料 消費期間 --
         + " and A.acct_month between :is_beg_date and :is_end_date"
   ;
   this.setString("is_it_type", varsStr("is_it_type"));
   setString("ls_id_pseqno", ls_id_pseqno);
   setString("ls_card_flag", as_card_purch_flag);
   setString("ls_card_type", as_card_type);
   setString("ls_group_code", as_group_code);
   setNumber("im_low_amt", varsNum("low_amt"));
   setString("is_beg_date", ls_date1);
   setString("is_end_date", ls_date2);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      varsSet("im_cnt", "0");
      varsSet("im_amt", "0");
      return sqlRowNum;
   }
   varsSet("ii_cnt", "" + colInt("db_cnt"));
   varsSet("im_amt", "" + colNum("db_amt"));
   return 1;
}

public int cmsM3120Card(String as_card_flag, String as_card_type, String as_group_code) throws Exception  {
   //--card_purch_flag: 1.正附卡合併, 2.正附卡分開
   //================================================================
   String ls_idno = varsStr("is_idno");
   if (empty(ls_idno)) {
      errmsg("身分證ID 不可同空白");
      return rc;
   }
   if (empty(as_card_type) || empty("as_group_code")) {
      errmsg("卡種, 團體代號 不可空白");
      return rc;
   }
   if (empty(as_card_flag))
      as_card_flag = "N";

   getIdnoPseqno(ls_idno);
   if (rc != 1) {
      return rc;
   }

   if (empty(varsStr("beg_ym")) || empty(varsStr("end_ym"))) {
      errmsg("帳務年月起迄期間 不可空白");
      return rc;
   }
   if (commString.strComp(varsStr("beg_ym"), varsStr("end_ym")) > 0) {
      errmsg("帳務年月:  起迄錯誤");
      return rc;
   }
   strSql = "select sum(consume_bl_amt) as bl_amt,"
         + " sum(consume_it_amt) as it_amt,"
         + " sum(consume_id_amt) as id_amt,"
         + " sum(consume_ca_amt) as ca_amt,"
         + " sum(consume_ao_amt) as ao_amt,"
         + " sum(consume_ot_amt) as ot_amt,"
         + " sum(consume_bl_cnt) as bl_cnt,"
         + " sum(consume_it_cnt) as it_cnt,"
         + " sum(consume_id_cnt) as id_cnt,"
         + " sum(consume_ca_cnt) as ca_cnt,"
         + " sum(consume_ao_cnt) as ao_cnt,"
         + " sum(consume_ot_cnt) as ot_cnt "
         + " from mkt_card_consume"
         + " where 1=1"
         + " and card_no in ( "
         + " select card_no from crd_card"
         + " where major_id_p_seqno =:ls_id_pseqno";
   if (eqAny(as_card_flag, "1") == false) {
      strSql += " and id_p_seqno =:ls_id_pseqno";
   }
   strSql += " and card_type =:ls_card_type"
         + " and group_code =:ls_group_code"
         + ") and acct_month between :ls_beg_ym and :ls_end_ym";
   setString("ls_id_pseqno", idPseqNo);
   setString("ls_card_type", as_card_type);
   setString("ls_group_code", nvl(as_group_code, "0000"));
   setString("ls_beg_ym", varsStr("beg_ym"));
   setString("ls_end_ym", varsStr("end_ym"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      varsSet("ii_cnt", "0");
      varsSet("im_amt", "0");
      return rc;
   }

   int li_cnt = 0;
   double lm_amt = 0;
   if (eqAny(varsStr("bl"), "Y")) {
      li_cnt += colInt("bl_ant");
      lm_amt += colNum("bl_amt");
   }
   if (eqAny(varsStr("it"), "Y")) {
      li_cnt += colInt("it_ant");
      lm_amt += colNum("it_amt");
   }
   if (eqAny(varsStr("id"), "Y")) {
      li_cnt += colInt("id_ant");
      lm_amt += colNum("id_amt");
   }
   if (eqAny(varsStr("ca"), "Y")) {
      li_cnt += colInt("ca_ant");
      lm_amt += colNum("ca_amt");
   }
   if (eqAny(varsStr("ao"), "Y")) {
      li_cnt += colInt("ao_ant");
      lm_amt += colNum("ao_amt");
   }
   if (eqAny(varsStr("ot"), "Y")) {
      li_cnt += colInt("ot_ant");
      lm_amt += colNum("ot_amt");
   }
   varsSet("ii_cnt", "" + li_cnt);
   varsSet("im_amt", "" + lm_amt);
   return rc;
}

private String condAcctCode() {
   String ss = "(''";
   if (eqIgno(varsStr("bl"), "Y")) {
      ss += ",'BL'";
   }
   if (eqIgno(varsStr("it"), "Y"))
      ss += ",'IT'";
   if (eqIgno(varsStr("id"), "Y"))
      ss += ",'ID'";
   if (eqIgno(varsStr("ca"), "Y"))
      ss += ",'CA'";
   if (eqIgno(varsStr("ao"), "Y"))
      ss += ",'AO'";
   if (eqIgno(varsStr("ot"), "Y"))
      ss += ",'OT'";
   return ss + ")";
}

}
