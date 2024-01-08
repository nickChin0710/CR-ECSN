package rskm03;
/**
 *
 */

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Rskm3170Func extends FuncEdit {
String kk1 = "", kk2 = "";

public Rskm3170Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
}

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect() {
   // TODO Auto-generated method stub
   return 0;
}


@Override
public void dataCheck()  {
   kk1 = wp.itemStr("card_no");
   if (this.ibAdd) {
      if (empty(wp.itemStr("card_no"))) {
         errmsg("卡號：不可空白");
         return;
      }
   }
   if (this.isAdd()) {
      return;
   }
   sqlWhere = " where 1=1"
         + " and card_no='" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   if (this.isOtherModify("rsk_ctfi_case", sqlWhere)) {
      return;
   }
}

void txn_dataCheck()  {
   kk1 = wp.itemStr("card_no");
   kk2 = wp.itemStr("rowid");
   if (this.ibAdd) {
      if (empty(wp.itemStr("card_no"))) {
         errmsg("卡號：不可空白");
         return;
      }
   }
   if (this.isAdd()) {
      return;
   }
   sqlWhere = " where 1=1"
         + " and rowid=x'" + kk2 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   if (this.isOtherModify("rsk_ctfi_txn", sqlWhere)) {
      return;
   }
}

@Override
public int dbInsert()  {
   actionInit("A");
   txn_dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "insert into rsk_ctfi_txn ("
         + " card_no, "
         + " txn_date, "
         + " txn_time, "
         + " mcht_no, "
         + " term_id, "
         + " mcht_category, "
         + " pos_em, "
         + " mcht_name, "
         + " mcht_addr, "
         + " mcht_tel_no, "
         + " txn_amt, "
         + " resp_code, "
         + " mcht_city, "
         + " mcht_country, "
         + " arq_bank_no, "
         + " txn_remark, "
         + " three_d_flag, "
         + " ip_addr, "
         + " eci_data, "
         + " ucaf_data, "
         + " source_amt, "
         + " source_curr,"
         + " mcht_ename, "
         + " crt_user,"
         //	+ " crt_date,"
         + " mod_user,"
         + " mod_time,"
         + " mod_pgm,"
         + " mod_seqno "
         + " ) values ("
         + " :card_no, :txn_date, :txn_time, :mcht_no, :term_id, :mcht_category"
         + ",:pos_em, :mcht_name, :mcht_addr, :mcht_tel_no, :txn_amt"
         + ",:resp_code, :mcht_city, :mcht_country, :arq_bank_no"
         + ",:txn_remark"
         + ",:three_d_flag, :ip_addr, :eci_data, :ucaf_data, :source_amt, :source_curr"
         + ",:mcht_ename, :crt_user  "
         + ",:mod_user, sysdate, :mod_pgm, 1"
         + " )";
   try {
      this.setString("card_no", kk1);
      item2ParmStr("txn_date", "txn_date");
      item2ParmStr("txn_time", "txn_time");
      item2ParmStr("mcht_no", "mcht_no");
      item2ParmStr("term_id", "term_id");
      item2ParmStr("mcht_category", "mcht_category");
      item2ParmStr("pos_em", "pos_em");
      item2ParmStr("mcht_name", "mcht_name");
      item2ParmStr("mcht_addr", "mcht_addr");
      item2ParmStr("mcht_tel_no", "mcht_tel_no");
      item2ParmNum("txn_amt", "txn_amt");
      item2ParmStr("resp_code", "resp_code");
      item2ParmStr("mcht_city", "mcht_city");
      item2ParmStr("mcht_country", "mcht_country");
      item2ParmStr("arq_bank_no", "arq_bank_no");
      item2ParmStr("txn_remark", "txn_remark");
      item2ParmNvl("three_d_flag", "N");
      item2ParmStr("ip_addr", "ip_addr");
      item2ParmStr("eci_data", "eci_data");
      item2ParmStr("ucaf_data", "ucaf_data");
      item2ParmNum("source_amt", "source_amt");
      item2ParmStr("source_curr", "source_curr");
      item2ParmStr("mcht_ename", "mcht_ename");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
   }
   catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
   }
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
   }

   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   if (eqIgno(wp.respHtml, "rskm3170_detl")) {
      dataCheck();
      if (rc != 1) {
         return rc;
      }
      strSql = "update rsk_ctfi_case set "
            + " survey_user =?, "
            + " case_remark =?, "
            + " mod_user =?, mod_time=sysdate, mod_pgm =? "
            + ", mod_seqno =nvl(mod_seqno,0)+1 "
            + sqlWhere;
      Object[] param = new Object[]{
            wp.itemStr("survey_user"),
            wp.itemStr("case_remark"),
            wp.loginUser,
            wp.itemStr("mod_pgm")
      };


      rc = sqlExec(strSql, param);
      if (sqlRowNum <= 0) {
         errmsg("update rsk_ctfi_case error: " + this.sqlErrtext);
      }
      return rc;
   }
   else if (eqIgno(wp.respHtml, "rskm3170_txn")) {
      dbUpdate_txn();
   }
   return rc;
}

public int dbUpdate_txn()  {
   actionInit("U");
   txn_dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "update rsk_ctfi_txn set "
         + " card_no = :card_no, "
         + " txn_date = :txn_date, "
         + " txn_time = :txn_time, "
         + " mcht_no = :mcht_no, "
         + " term_id = :term_id, "
         + " mcht_category = :mcht_category, "
         + " pos_em = :pos_em, "
         + " mcht_name = :mcht_name, "
         + " mcht_addr = :mcht_addr, "
         + " mcht_tel_no = :mcht_tel_no, "
         + " txn_amt = :txn_amt, "
         + " resp_code = :resp_code, "
         + " mcht_city = :mcht_city, "
         + " mcht_country = :mcht_country, "
         + " arq_bank_no = :arq_bank_no, "
         + " txn_remark = :txn_remark, "
         + " three_d_flag = :three_d_flag, "
         + " ip_addr = :ip_addr, "
         + " source_amt = :source_amt, "
         + " source_curr = :source_curr, "
         + " mcht_ename = :mcht_ename, "
         + " eci_data = :eci_data, "
         + " ucaf_data = :ucaf_data, "
         + " crt_user = :crt_user, "
         + " crt_date = sysdate, "
         + " mod_user = :mod_user, "
         + " mod_time = sysdate, "
         + " mod_pgm = :mod_pgm, "
         + " mod_seqno =nvl(mod_seqno,0)+1"
         + " where rowid =x'" + kk2 + "'"
         + " and nvl(mod_seqno,0) ="
   ;

   this.setString("card_no", kk1);
   item2ParmStr("txn_date");
   item2ParmStr("txn_time");
   item2ParmStr("mcht_no");
   item2ParmStr("term_id");
   item2ParmStr("mcht_category");
   item2ParmStr("pos_em");
   item2ParmStr("mcht_name");
   item2ParmStr("mcht_addr");
   item2ParmStr("mcht_tel_no");
   item2ParmNum("txn_amt");
   item2ParmStr("resp_code");
   item2ParmStr("mcht_city");
   item2ParmStr("mcht_country");
   item2ParmStr("arq_bank_no");
   item2ParmStr("txn_remark");
   item2ParmNvl("three_d_flag", "N");
   item2ParmStr("ip_addr");
   item2ParmNum("source_amt");
   item2ParmStr("source_curr");
   item2ParmStr("mcht_ename");
   item2ParmStr("eci_data");
   item2ParmStr("ucaf_data");
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   item2ParmStr("mod_pgm");
   item2ParmNum("mod_seqno");


   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_txn error: " + this.sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

public int procFuncBill()  {
   int il_select_cnt = 0;
   String sql1 = " select "
         + " A.card_no, "
         + " A.purchase_date as txn_date, "
         + " A.dest_amt as txn_amt, "
         + " A.mcht_no as mcht_no, "
         + " A.mcht_chi_name as mcht_name,"
         + " A.mcht_eng_name as mcht_ename,"
         + " A.mcht_city ,"
         + " A.mcht_country,"
         + " A.mcht_category,"
         + " A.source_amt,"
         + " A.source_curr,"
         + " A.terminal_id as term_id ,"
         + " B.EC_IND as eci_data,"
         + " B.UCAF as ucaf_data,"
         + " A.pos_entry_mode as pos_em , "
         + " A.acq_member_id as arq_bank_no "
         + " from bil_bill A left join bil_nccc300_dtl B on A.reference_no = B.reference_no "
         + " where 1=1 and A.reference_no = ? ";
   sqlSelect(sql1, new Object[]{varsStr("reference_no")});

   il_select_cnt = sqlRowNum;

   if (il_select_cnt == 0) return -1;

   strSql = " insert into rsk_ctfi_txn ( "
         + " card_no, "
         + " txn_date, "
         + " mcht_no, "
         + " term_id, "
         + " mcht_category, "
         + " mcht_name, "
         + " txn_amt, "
         + " resp_code, "
         + " mcht_city, "
         + " mcht_country, "
         + " eci_data, "
         + " ucaf_data, "
         + " source_amt, "
         + " source_curr,"
         + " mcht_ename, "
         + " pos_em , "
         + " arq_bank_no , "
         + " crt_user,"
         + " crt_date,"
         + " mod_user,"
         + " mod_time,"
         + " mod_pgm,"
         + " mod_seqno "
         + " ) values ( "
         + " :card_no, "
         + " :txn_date, "
         + " :mcht_no, "
         + " '', "
         + " :mcht_category, "
         + " :mcht_name, "
         + " :txn_amt, "
         + " '00', "
         + " :mcht_city, "
         + " :mcht_country, "
         + " :eci_data, "
         + " :ucaf_data, "
         + " :source_amt, "
         + " :source_curr,"
         + " :mcht_ename, "
         + " :pos_em , "
         + " :arq_bank_no , "
         + " :crt_user,"
         + " to_char(sysdate,'yyyymmdd'),"
         + " :mod_user,"
         + " sysdate,"
         + " :mod_pgm,"
         + " 1 "
         + " ) "
   ;

   col2ParmStr("card_no");
   col2ParmStr("txn_date");
   col2ParmStr("mcht_no");
   //col2ParmStr("term_id");
   col2ParmStr("mcht_category");
   col2ParmStr("mcht_name");
   col2ParmNum("txn_amt");
   col2ParmStr("mcht_city");
   col2ParmStr("mcht_country");
   col2ParmStr("eci_data");
   col2ParmStr("ucaf_data");
   col2ParmNum("source_amt");
   col2ParmStr("source_curr");
   col2ParmStr("mcht_ename");
   col2ParmStr("pos_em");
   col2ParmStr("arq_bank_no");
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", modPgm);

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfi_txn error ");
   }

   return rc;
}

public int procFuncTxlog()  {
   int il_select_cnt = 0;
   String ls_threeD_flag = "";
   String sql1 = " select "
         + " card_no , "
         + " tx_date as txn_date , "
         + " tx_time as txn_time , "
         + " mcht_no , "
//				 		+ " term_id , "
         + " mcc_code as mcht_category , "
         + " pos_mode as pos_em , "
         + " pos_term_id as term_id , "
         + " nt_amt as txn_amt , "
         + " iso_resp_code as resp_code , "
         + " stand_in as arq_bank_no , "
         + " mcht_city , "
         + " mcht_country , "
         + " ec_flag as eci_data , "
//				 		+ " ec_ind as eci_data , "
         + " ucaf as ucaf_data , "
         + " mcht_name as mcht_ename , "
         + " ori_amt as source_amt , "
         + " tx_currency as source_curr , "
         + " auth_remark as txn_remark , "
         + " fallback as ic_fallback "
         + " from cca_auth_txlog "
         + " where 1=1 "
         + " and auth_seqno = ? "
         + " and card_no = ? "
         + " and nt_amt = ? ";

   sqlSelect(sql1, new Object[]{varsStr("auth_seqno"), varsStr("card_no"), varsNum("nt_amt")});

   il_select_cnt = sqlRowNum;

   if (il_select_cnt == 0) return -1;

   String sql2 = " select mcht_name , mcht_addr from cca_mcht_bill where mcht_no = ? ";
   sqlSelect(sql2, new Object[]{colStr("mcht_no")});

   String sql3 = " select bin_type from crd_card where card_no = ? "
         + " union "
         + " select bin_type from dbc_card where card_no = ? ";
   sqlSelect(sql3, new Object[]{colStr("card_no"), colStr("card_no")});

   if (eqIgno(colStr("bin_type"), "M")) {
      if (eqIgno(colStr("ucaf_data"), "2")) ls_threeD_flag = "Y";
   }
   else if (eqIgno(colStr("bin_type"), "V") || eqIgno(colStr("bin_type"), "J")) {
      if (eqIgno(colStr("eci_data"), "5")) ls_threeD_flag = "Y";
   }


   strSql = "insert into rsk_ctfi_txn ("
         + " card_no, "
         + " txn_date, "
         + " txn_time, "
         + " mcht_no, "
         + " term_id, "
         + " mcht_category, "
         + " pos_em, "
         + " mcht_name, "
         + " mcht_addr, "
         + " txn_amt, "
         + " resp_code, "
         + " mcht_city, "
         + " mcht_country, "
         + " arq_bank_no, "
         + " txn_remark, "
         + " eci_data, "
         + " ucaf_data, "
         + " source_amt, "
         + " source_curr,"
         + " mcht_ename, "
         + " ic_fallback , "
         + " three_d_flag , "
         + " crt_user,"
         + " crt_date,"
         + " mod_user,"
         + " mod_time,"
         + " mod_pgm,"
         + " mod_seqno "
         + " ) values ( "
         + " :card_no, "
         + " :txn_date, "
         + " :txn_time, "
         + " :mcht_no, "
         + " :term_id, "
         + " :mcht_category, "
         + " :pos_em, "
         + " :mcht_name, "
         + " :mcht_addr, "
         + " :txn_amt, "
         + " :resp_code, "
         + " :mcht_city, "
         + " :mcht_country, "
         + " :arq_bank_no, "
         + " :txn_remark, "
         + " :eci_data, "
         + " :ucaf_data, "
         + " :source_amt, "
         + " :source_curr,"
         + " :mcht_ename, "
         + " :ic_fallback , "
         + " :three_d_flag , "
         + " :crt_user,"
         + " to_char(sysdate,'yyyymmdd'),"
         + " :mod_user,"
         + " sysdate,"
         + " :mod_pgm,"
         + " 1 "
         + " ) "
   ;

   col2ParmStr("card_no");
   col2ParmStr("txn_date");
   col2ParmStr("txn_time");
   col2ParmStr("mcht_no");
   col2ParmStr("term_id");
   col2ParmStr("mcht_category");
   col2ParmStr("pos_em");
   col2ParmStr("mcht_name");
   col2ParmStr("mcht_addr");
   col2ParmNum("txn_amt");
   col2ParmStr("resp_code");
   col2ParmStr("mcht_city");
   col2ParmStr("mcht_country");
   col2ParmStr("arq_bank_no");
   col2ParmStr("txn_remark");
   col2ParmStr("eci_data");
   col2ParmStr("ucaf_data");
   col2ParmNum("source_amt");
   col2ParmStr("source_curr");
   col2ParmStr("mcht_ename");
   col2ParmStr("ic_fallback");
   setString("three_d_flag", ls_threeD_flag);
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", modPgm);


   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfi_txn error ");
   }

   return rc;
}

}
