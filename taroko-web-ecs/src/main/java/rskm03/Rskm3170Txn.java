package rskm03;
/**
 *
 */

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Rskm3170Txn extends FuncEdit {

String kk1 = "", kk2 = "";

public Rskm3170Txn(TarokoCommon wr) {
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
   log("sql-where=" + sqlWhere);
   log("kk2:" + kk2);
   log("mod_seqno:" + wp.modSeqno());
   if (this.isOtherModify("rsk_ctfi_txn", sqlWhere)) {
      return;
   }

}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
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
         + " ic_fallback, "
         + " crt_user,"
         + " crt_date,"
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
         + ",:mcht_ename, :ic_fallback , :crt_user, to_char(sysdate,'yyyymmdd') "
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
      item2ParmNvl("ic_fallback", "N");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
   }
   catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
   }
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("insert RSK_CTFI_TXN error; " + sqlErrtext);
   }

   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "update rsk_ctfi_txn set "
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
         + " ic_fallback =:ic_fallback , "
//         + " crt_user = :crt_user, "
//         + " crt_date = sysdate, "
         + " mod_user = :mod_user, "
         + " mod_time = sysdate, "
         + " mod_pgm = :mod_pgm, "
         + " mod_seqno =nvl(mod_seqno,0)+1"
         + " where rowid =x'" + kk2 + "'"
         + " and nvl(mod_seqno,0) =:mod_seqno"
   ;

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
   item2ParmNvl("ic_fallback", "N");
//			setString("crt_user",wp.loginUser);
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
public int dbDelete()  {
   actionInit("D");

   strSql = "delete rsk_ctfi_txn where 1=1 " +commSqlStr.whereRowid;
   //zzsql.where_rowid(wp.itemStr("rowid"));
   setString(1,wp.itemStr("rowid"));
   setParm(1, wp.itemStr("rowid"));
   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("delete rsk_ctfi_txn error");
   }

   return rc;
}

}
