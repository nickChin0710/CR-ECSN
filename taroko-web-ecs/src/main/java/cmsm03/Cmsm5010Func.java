package cmsm03;
/**
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 2023-0410:  Zuwei Su    copy from mega
 */

import busi.FuncAction;

public class Cmsm5010Func extends FuncAction {
private busi.SqlPrepare sp = new busi.SqlPrepare();
String isSqlVisit = "";
String rowid = "" , idPSeqno = "" , majorIdPSeqno = "" , chiName ="" , dataSeqno = "";
String acctType = "" , acnoPSeqno = "" , majorCardNo = "" , groupCode = "" , stmtCycle = "", currentCode="";
int iiBatchSeq =0;
String isBatchNo="";
//=============================================================
@Override
public void dataCheck() {
   // TODO Auto-generated method stub

}
void initData() {
   acctType = "" ; acnoPSeqno = "" ; majorCardNo = "" ; groupCode = "" ; stmtCycle = "";
   idPSeqno = ""; majorIdPSeqno = ""; chiName =""; currentCode = "";
}
int getDataSeqNo() throws Exception {
   String sql1 = "select right('0000000000'||col_modseq.nextval,10) as data_seqno from SYSIBM.SYSDUMMY1";
   sqlSelect(sql1);
   if(sqlRowNum >0)
      dataSeqno = colStr("data_seqno");
   else	{
      errmsg("取流水號失敗");
      return -1;
   }
   return 0;
}
int getCrdIdno(String idno) throws Exception {
    String sql1 = "select 1 from crd_idno where ID_P_SEQNO=?";
    sqlSelect(sql1, new Object[] {idno});
    if(sqlRowNum >0)
       return 0;
    else {
       return -1;
    }
 }
void getInitData(String _card_no) throws Exception {
   initData();
   if(getDataSeqNo() == -1)
      return ;

   if(empty(_card_no)) {
      errmsg("卡號: 不可空白");
      return ;
   }

   String sql2 = "select id_p_seqno , major_id_p_seqno , acct_type , acno_p_seqno , major_card_no , group_code , stmt_cycle , "
           + " uf_chi_name(card_no) as chi_name, CURRENT_CODE from crd_card where card_no = ? ";

   sqlSelect(sql2,new Object[]{_card_no});
   if(sqlRowNum >0) {
      idPSeqno = colStr("id_p_seqno");
      majorIdPSeqno = colStr("major_id_p_seqno");
      acctType = colStr("acct_type");
      acnoPSeqno = colStr("acno_p_seqno");
      majorCardNo = colStr("major_card_no");
      groupCode = colStr("group_code");
      stmtCycle = colStr("stmt_cycle");
      chiName = colStr("chi_name");
      currentCode = colStr("CURRENT_CODE");
   }	else	{
//      errmsg("讀取卡片資料失敗");
       rc = -1;
      return;
   }

}

void insertBilPostcntl() throws Exception {
   iiBatchSeq=0;
   isBatchNo ="";
   strSql ="select max(batch_seq) as batch_seq"
           +" from bil_postcntl"
           +" where batch_unit ='OI'"
           +" and batch_date =?";
   sqlSelect(strSql, sysDate);
   if (sqlRowNum >0) {
      iiBatchSeq =colInt("batch_seq");
   }
   iiBatchSeq++;
   isBatchNo =sysDate+"OI"+commString.right("0000"+iiBatchSeq,4);

   sp.sql2Insert("bil_postcntl");
   sp.ppstr("batch_date", sysDate);
   sp.ppstr("batch_unit", "OI");
   sp.ppint("batch_seq", iiBatchSeq); //    INTEGER (4) );
   sp.ppstr("batch_no", isBatchNo);  //批次號碼
   // setValueInt("tot_record", 0);
   // setValueDouble("tot_amt", 0);
   sp.ppstr("confirm_flag_p", "Y");
   sp.ppstr("confirm_flag", "Y");
   // setValue("apr_user", "");
   // setValue("apr_date", "");
   sp.ppstr("this_close_date", sysDate);
   sp.modxxx(modUser,modPgm);

   sqlExec(sp.sqlStmt(),sp.sqlParm());
   if (sqlRowNum <=0) {
      sqlErr("insert bil_postcntl error");
   }
}

public int dbInsertC2() throws Exception {
   msgOK();
   String procFlag = "";
   getInitData(wp.itemStr("C2.card_no"));
   if(rc <0) {
//      return rc;
       procFlag = "1";
   } else {
       if(!"0".equals(currentCode)) {
           procFlag = "10";
       }
       String idNo = wp.itemStr("C2.id_no");
       // 檢核與【CRD_IDNO】的ID_NO是否相同
       if (idNo != null && idNo.length() > 0 && getCrdIdno(idNo) != 0) {
           procFlag = "9";
       }
       String purchaseDate = wp.itemStr("C2.purchase_date");
       String purchaseDateE = wp.itemStr("C2.purchase_date_e");
       if (purchaseDate == null || purchaseDate.length() != 8 
               || purchaseDateE == null || purchaseDateE.length() != 8) {
           procFlag = "4";
       }
   }
   
   msgOK();
   if (empty(isBatchNo)) {
      insertBilPostcntl();
      if (rc !=1) return rc;
      wp.itemSet("C2.batch_no",isBatchNo);
   }

   sp.sql2Insert("bil_mcht_apply_tmp");
   sp.ppstr("crt_date", sysDate);
   sp.ppstr("crt_time", sysTime);
   sp.ppstr("file_name", wp.itemStr("imp_file_name"));
   sp.ppstr("data_seqno", dataSeqno);
//   sp.ppstr("data_seq1", wp.itemStr("C2.data_seq1"));
   sp.ppstr("corp_no", wp.itemStr("ex_corp_no"));
//   sp.ppstr("company_name", wp.itemStr("C2.company_name"));
   sp.ppstr("file_type", "09");
   sp.ppstr("batch_no",isBatchNo);
   sp.ppstr("id_p_seqno", idPSeqno);
   sp.ppstr("major_id_p_seqno", majorIdPSeqno);
   sp.ppstr("acct_type", acctType);
   sp.ppstr("acno_p_seqno", acnoPSeqno);
   sp.ppstr("major_card_no", majorCardNo);
   sp.ppstr("group_code", groupCode);
   sp.ppstr("stmt_cycle", stmtCycle);

   sp.ppstr("card_no", wp.itemStr("C2.card_no"));
   sp.ppstr("purchase_date", wp.itemStr("C2.purchase_date"));
   sp.ppstr("purchase_date_e", wp.itemStr("C2.purchase_date_e"));
   sp.ppstr("id_no", wp.itemStr("C2.id_no"));
   sp.ppstr("car_no", wp.itemStr("C2.car_no"));
   sp.ppstr("service_days", wp.itemStr("C2.service_days"));
   sp.ppstr("chi_name", wp.itemStr("C2.chi_name"));
   sp.ppstr("contact_tel_no", wp.itemStr("C2.contact_tel_no"));
   sp.ppstr("order_no", wp.itemStr("C2.order_no"));
   sp.ppstr("service_no", wp.itemStr("C2.service_no"));
   sp.ppstr("service_name", wp.itemStr("C2.service_name"));
   sp.ppstr("service_code", wp.itemStr("C2.service_code"));
   sp.ppstr("service_item", wp.itemStr("C2.service_item"));

   sp.ppstr("project_no", "機場接送");
   sp.ppstr("proc_flag", procFlag);
//   sp.ppstr("mod_user", modUser);
   sp.ppdate("mod_time");
   sp.ppstr("mod_pgm", modPgm);
//   sp.ppint("mod_seqno", 1);

   wp.notFound = "";
   sqlExec(sp.sqlStmt(),sp.sqlParm());
   if(sqlRowNum <=0) {
      errmsg("insert bil_mcht_apply_tmp error ");
      return rc;
   }

   return rc;
}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   msgOK();
   String lsFileType =wp.itemStr("ex_file_type");
   String lsBatchNo =wp.itemStr("ex_batch_no");
   if (empty(lsFileType) || empty(lsBatchNo)) {
      errmsg("[檔案類別, 匯入批號] 不可空白");
      return rc;
   }

   strSql="delete bil_mcht_apply_tmp where batch_no =? and file_type =?";
   setString(1,lsBatchNo);
   setString(lsFileType);
   sqlExec(strSql);
   if (sqlRowNum <0) {
       sqlErr("delete bil_mcht_apply_tmp error");
      return rc;
   }

   errmsg("刪除筆數=[%s]",sqlRowNum);
   rc =1;
   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

public int insertVist(int ll)  {
   msgOK();

   if (empty(isSqlVisit)) {
      isSqlVisit = " insert into cms_ppcard_visit ("
            + " crt_date ,"
            + " bin_type ,"
            + " data_seqno ,"
            + " from_type ,"
            + " bank_name ,"
            + " deal_type ,"
            + " associate_code ,"
            + " ica_no ,"
            + " pp_card_no ,"
            + " ch_ename ,"
            + " visit_date ,"
            + " lounge_name ,"
            + " lounge_code ,"
            + " domestic_int ,"
            + " iso_conty ,"
            + " iso_conty_code ,"
            + " ch_visits ,"
            + " guests_count ,"
            + " total_visits ,"
            + " batch_no ,"
            + " voucher_no ,"
            + " mc_billing_region ,"
            + " curr_code ,"
            + " fee_per_holder ,"
            + " fee_per_guest ,"
            + " total_fee ,"
            + " total_free_guests ,"
            + " free_guests_value ,"
            + " tot_charg_guest ,"
            + " charg_guest_value ,"
            + " billing_region ,"
            + " terminal_no ,"
            + " use_city ,"
            + " id_no ,"
            + " id_no_code ,"
            + " id_p_seqno ,"
            + " free_use_cnt ,"
            + " guest_free_cnt ,"
            + " ch_cost_amt ,"
            + " guest_cost_amt ,"
            + " card_no ,"
            + " mcht_no ,"
            + " user_remark ,"
            + " crt_user ,"
            + " imp_file_name ,"
            + " mod_user ,"
            + " mod_time ,"
            + " mod_pgm ,"
            + " mod_seqno "
            + " ) values ( "
            + " :crt_date ,"
            + " :bin_type ,"
            + " :data_seqno ,"
            + " :from_type ,"
            + " :bank_name ,"
            + " :deal_type ,"
            + " :associate_code ,"
            + " :ica_no ,"
            + " :pp_card_no ,"
            + " :ch_ename ,"
            + " :visit_date ,"
            + " :lounge_name ,"
            + " :lounge_code ,"
            + " :domestic_int ,"
            + " :iso_conty ,"
            + " :iso_conty_code ,"
            + " :ch_visits ,"
            + " :guests_count ,"
            + " :total_visits ,"
            + " :batch_no ,"
            + " :voucher_no ,"
            + " :mc_billing_region ,"
            + " :curr_code ,"
            + " :fee_per_holder ,"
            + " :fee_per_guest ,"
            + " :total_fee ,"
            + " :total_free_guests ,"
            + " :free_guests_value ,"
            + " :tot_charg_guest ,"
            + " :charg_guest_value ,"
            + " :billing_region ,"
            + " :terminal_no ,"
            + " :use_city ,"
            + " :id_no ,"
            + " :id_no_code ,"
            + " :id_p_seqno ,"
            + " :free_use_cnt ,"
            + " :guest_free_cnt ,"
            + " :ch_cost_amt ,"
            + " :guest_cost_amt ,"
            + " :card_no ,"
            + " :mcht_no ,"
            + " '' ,"
            + " :crt_user ,"
            + " :imp_file_name ,"
            + " :mod_user ,"
            + " sysdate ,"
            + " :mod_pgm ,"
            + " 1 "
            + " ) "
      ;
   }

   setString("crt_date", wp.colStr(ll, "crt_date"));
   setString("bin_type", wp.colStr(ll, "bin_type"));
   setNumber("data_seqno", wp.colNum(ll, "data_seqno"));
   setString("from_type", wp.colStr(ll, "from_type"));
   setString("bank_name", wp.colStr(ll, "bank_name"));
   setString("deal_type", wp.colStr(ll, "deal_type"));
   setString("associate_code", wp.colStr(ll, "associate_code"));
   setString("ica_no", wp.colStr(ll, "ica_no"));
   setString("pp_card_no", wp.colStr(ll, "pp_card_no"));
   setString("ch_ename", wp.colStr(ll, "ch_ename"));
   setString("visit_date", wp.colStr(ll, "visit_date"));
   setString("lounge_name", commString.left(wp.colStr(ll, "lounge_name"), 50));
   setString("lounge_code", wp.colStr(ll, "lounge_code"));
   setString("domestic_int", wp.colStr(ll, "domestic_int"));
   setString("iso_conty", wp.colStr(ll, "iso_conty"));
   setString("iso_conty_code", wp.colStr(ll, "iso_conty_code"));
   setNumber("ch_visits", wp.colNum(ll, "cardholder_visits"));
   setNumber("guests_count", wp.colNum(ll, "guests_count"));
   setNumber("total_visits", wp.colNum(ll, "total_visits"));
   setNumber("batch_no", wp.colNum(ll, "batch_no"));
   setString("voucher_no", wp.colStr(ll, "voucher_no"));
   setString("mc_billing_region", wp.colStr(ll, "mc_billing_region"));
   setString("curr_code", wp.colStr(ll, "curr_code"));
   setNumber("fee_per_holder", wp.colNum(ll, "fee_per_holder"));
   setNumber("fee_per_guest", wp.colNum(ll, "fee_per_guest"));
   setNumber("total_fee", wp.colNum(ll, "total_fee"));
   setNumber("total_free_guests", wp.colNum(ll, "total_free_guests"));
   setNumber("free_guests_value", wp.colNum(ll, "free_guests_value"));
   setNumber("tot_charg_guest", wp.colNum(ll, "tot_charg_guest"));
   setNumber("charg_guest_value", wp.colNum(ll, "charg_guest_value"));
   setString("billing_region", wp.colStr(ll, "billing_region"));
   setString("terminal_no", commString.left(wp.colStr(ll, "terminal_no"), 30));
   setString("use_city", wp.colStr(ll, "use_city"));
   setString("id_no", wp.colStr(ll, "id_no"));
   setString("id_no_code", wp.colStr(ll, "id_no_code"));
   setString("id_p_seqno", wp.colStr(ll, "id_p_seqno"));
   setNumber("free_use_cnt", wp.colNum(ll, "free_use_cnt"));
   setNumber("guest_free_cnt", wp.colNum(ll, "guest_free_cnt"));
   setNumber("ch_cost_amt", wp.colNum(ll, "ch_cost_amt"));
   setNumber("guest_cost_amt", wp.colNum(ll, "guest_cost_amt"));
   setString("card_no", wp.colStr(ll, "card_no"));
   setString("mcht_no", wp.colStr(ll, "mcht_no"));
   setString("crt_user", modUser);
   setString("imp_file_name", wp.colStr(ll, "imp_file_name"));
   setString("mod_user", modUser);
   setString("mod_pgm", modPgm);

//		var2Parm_ss("crt_date");
//		var2Parm_ss("bin_type");
//		var2Parm_num("data_seqno");
//		var2Parm_ss("from_type");
//		var2Parm_ss("bank_name");
//		var2Parm_ss("deal_type");
//		var2Parm_ss("associate_code");
//		var2Parm_ss("ica_no");
//		var2Parm_ss("pp_card_no");
//		var2Parm_ss("ch_ename");
//		var2Parm_ss("visit_date");
//		var2Parm_ss("lounge_name");
//		var2Parm_ss("lounge_code");
//		var2Parm_ss("domestic_int");
//		var2Parm_ss("iso_conty");
//		var2Parm_ss("iso_conty_code");
//		var2Parm_num("ch_visits");
//		var2Parm_num("guests_count");
//		var2Parm_num("total_visits");
//		var2Parm_num("batch_no");
//		var2Parm_ss("voucher_no");
//		var2Parm_ss("mc_billing_region");
//		var2Parm_ss("curr_code");
//		var2Parm_num("fee_per_holder");
//		var2Parm_num("fee_per_guest");
//		var2Parm_num("total_fee");
//		var2Parm_num("total_free_guests");
//		var2Parm_num("free_guests_value");
//		var2Parm_num("tot_charg_guest");
//		var2Parm_num("charg_guest_value");
//		var2Parm_ss("billing_region");
//		var2Parm_ss("terminal_no");
//		var2Parm_ss("use_city");
//		var2Parm_ss("id_no");
//		var2Parm_ss("id_no_code");
//		var2Parm_ss("id_p_seqno");
//		var2Parm_num("free_use_cnt");
//		var2Parm_num("guest_free_cnt");
//		var2Parm_num("ch_cost_amt");
//		var2Parm_num("guest_cost_amt");
//		var2Parm_ss("card_no");
//		var2Parm_ss("mcht_no");
//		var2Parm_ss("crt_user");
//		var2Parm_ss("imp_file_name");
//		setString("mod_user",wp.loginUser);
//		setString("mod_pgm",wp.mod_pgm());

   sqlExec(isSqlVisit);

   if (sqlRowNum <= 0) {
      errmsg("insert cms_ppcard_visit error ");
   }

   return rc;
}

}
