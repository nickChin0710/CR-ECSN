package rskm03;
/**
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import busi.FuncAction;

public class Rskm3250Func extends FuncAction {
String kk1 = "", ls_seqno = "";
public String is_case_no = "";

@Override
public void dataCheck()  {
   if (ibAdd)
      kk1 = wp.itemStr("kk_case_no");
   else
      kk1 = wp.itemStr("case_no");

   if (empty(wp.itemStr("card_no"))) {
      errmsg("卡號 不可空白");
      return;
   }

   if (empty(wp.itemStr("idno"))) {
      errmsg("正卡ID 不可空白");
      return;
   }

   if (empty(wp.itemStr("assign_date"))) {
      errmsg("Assign Date 不可空白");
      return;
   }

   if (empty(wp.itemStr("proc_user"))) {
      errmsg("Processor 不可空白");
      return;
   }

   if (empty(wp.itemStr("source_type"))) {
      errmsg("Source 不可空白");
      return;
   }

   if (ibAdd) {
      selectSeqno();
   }

   if (!ibAdd) {
      selectCtfcTxn();
   }

   if (this.ibDelete) {
      if (colNum("ll_cnt") > 0) {
         errmsg("已有 交易資料 不可刪除");
         return;
      }
   }

}

void selectSeqno()  {
   String sql1 = " select "
         + " lpad(to_char(to_number(max(substrb(rpad(case_no,12,'0'),11,2)))+1),2,'0') as ls_seqno "
         + " from rsk_ctfc_mast "
         + " where case_no like ? ";
   sqlSelect(sql1, new Object[]{wp.itemStr("idno") + "%"});

   if (sqlRowNum == 0 || empty(colStr("ls_seqno")))
      ls_seqno = "01";
   else if (sqlRowNum < 0) {
      String sql2 = " select max(case_no) as ls_case_no "
            + " from rsk_ctfc_mast where case_no like ? ";
      sqlSelect(sql2, new Object[]{wp.itemStr("idno") + "%"});
      errmsg("無法取得 Case No , 最大 Case No :" + colStr("ls_case_no"));
      rc = -1;
      return;
   }
   else if (sqlRowNum > 0) {
      ls_seqno = colStr("ls_seqno");
   }
   rc = 1;
   is_case_no = wp.itemStr("idno") + ls_seqno;

}

void selectCtfcTxn()  {
   String sql1 = " select count(*) as ll_cnt from rsk_ctfc_txn where case_no =? ";
   sqlSelect(sql1, new Object[]{kk1});
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "insert into rsk_ctfc_mast ("
         + " case_no ,"
         + " idno ,"
         + " assign_date ,"
         + " card_no ,"
         + " proc_user ,"
         + " source_type ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ("
         + " :case_no ,"
         + " :idno ,"
         + " :assign_date ,"
         + " :card_no ,"
         + " :proc_user ,"
         + " :source_type ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " '1' "
         + " )";

   setString("case_no", is_case_no);
   item2ParmStr("idno");
   item2ParmStr("assign_date");
   item2ParmStr("card_no");
   item2ParmStr("proc_user");
   item2ParmStr("source_type");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3250");
   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("insert rsk_ctfc_mast error !");
   return rc;
}

void alertCaseNO() {

}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = " update rsk_ctfc_mast set "
         + " assign_date =:assign_date ,"
         + " proc_user =:proc_user ,"
         + " source_type =:source_type ,"
         + " mod_user =:mod_user ,"
         + " mod_time =sysdate ,"
         + " mod_pgm =:mod_pgm ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where case_no=:kk1 "
   ;

   item2ParmStr("assign_date");
   item2ParmStr("proc_user");
   item2ParmStr("source_type");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   setString("kk1", kk1);

   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("update rsk_ctfc_mast error !");

   return rc;
}

@Override
public int dbDelete()  {
   actionInit("D");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = " delete rsk_ctfc_mast where case_no =:kk1 ";
   setString("kk1", kk1);
   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("delete rsk_ctfc_mast error !");
   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

}
