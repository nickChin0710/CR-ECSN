package rskm01;
/**
 * rskm0260: 扣款沖銷作業處理
 * 2020-0116   JH    update FST_xxx
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * *rskm0265: 扣款沖銷作業取消處理 dbDelete
 */

import busi.FuncEdit;

public class Rskm0260Func extends FuncEdit {
String isRefno = "";

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect() {
   isRefno = varsStr("reference_no");
   int liRefSeq = varsInt("reference_seq");

   strSql = "select rsk_chgback.*, "
         + " uf_nvl(curr_code,'901') as curr_code,"
         + " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         + " uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt,"
         + " uf_dc_amt(curr_code,sec_twd_amt,sec_dc_amt) as sec_dc_amt,"
         + " uf_dc_amt(curr_code,sec_disb_amt,sec_disb_dc_amt) as sec_disb_dc_amt,"
         + " hex(rowid) as rowid"
         + " from rsk_chgback"
         + " where 1=1"
         + " and reference_no =?"
         + " order by reference_seq "
         + commSqlStr.rownum(1)
   ;
   setString(1, isRefno);
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return 0;

   this.colDataToWpCol("");
   if (colStr("chg_stage").compareTo("3") >= 0 && colEmpty("sec_status") == false) {
      //-一扣資料-
      wp.colSet("fst_status", colStr("sec_status"));
      wp.colSet("fst_reverse_mark ", colStr("sec_reverse_mark "));
      wp.colSet("fst_reverse_date ", colStr("sec_reverse_date "));
      wp.colSet("fst_rebuild_mark ", colStr("sec_rebuild_mark "));
      wp.colSet("fst_rebuild_date ", colStr("sec_rebuild_date "));
      wp.colSet("fst_send_date    ", colStr("sec_send_date    "));
      wp.colSet("fst_send_cnt     ", colStr("sec_send_cnt     "));
      wp.colSet("fst_usage_code   ", colStr("sec_usage_code   "));
      wp.colSet("fst_reason_code  ", colStr("sec_reason_code  "));
      wp.colSet("fst_msg          ", colStr("sec_msg          "));
      wp.colSet("fst_doc_mark     ", colStr("sec_doc_mark     "));
      wp.colSet("fst_amount       ", colStr("sec_amount       "));
      wp.colSet("fst_twd_amt      ", colStr("sec_twd_amt      "));
      wp.colSet("fst_dc_amt       ", colStr("sec_dc_amt       "));
      wp.colSet("fst_part_mark    ", colStr("sec_part_mark    "));
      wp.colSet("fst_expire_date  ", colStr("sec_expire_date  "));
      wp.colSet("fst_add_date     ", colStr("sec_add_date     "));
      wp.colSet("fst_add_user     ", colStr("sec_add_user     "));
      wp.colSet("fst_apr_date     ", colStr("sec_apr_date     "));
      wp.colSet("fst_apr_user     ", colStr("sec_apr_user     "));
      wp.colSet("fst_disb_yn      ", colStr("sec_disb_yn      "));
      wp.colSet("fst_disb_amt     ", colStr("sec_disb_amt     "));
      wp.colSet("fst_disb_dc_amt  ", colStr("sec_disb_dc_amt  "));
      wp.colSet("fst_disb_add_date", colStr("sec_disb_add_date"));
      wp.colSet("fst_disb_add_user", colStr("sec_disb_add_user"));
      wp.colSet("fst_disb_apr_date", colStr("sec_disb_apr_date"));
      wp.colSet("fst_disb_apr_user", colStr("sec_disb_apr_user"));
      //-二扣資料-
      wp.colSet("sec_status", colStr("fst_status"));
      wp.colSet("sec_reverse_mark ", colStr("fst_reverse_mark "));
      wp.colSet("sec_reverse_date ", colStr("fst_reverse_date "));
      wp.colSet("sec_rebuild_mark ", colStr("fst_rebuild_mark "));
      wp.colSet("sec_rebuild_date ", colStr("fst_rebuild_date "));
      wp.colSet("sec_send_date    ", colStr("fst_send_date    "));
      wp.colSet("sec_send_cnt     ", colStr("fst_send_cnt     "));
      wp.colSet("sec_usage_code   ", colStr("fst_usage_code   "));
      wp.colSet("sec_reason_code  ", colStr("fst_reason_code  "));
      wp.colSet("sec_msg          ", colStr("fst_msg          "));
      wp.colSet("sec_doc_mark     ", colStr("fst_doc_mark     "));
      wp.colSet("sec_amount       ", colStr("fst_amount       "));
      wp.colSet("sec_twd_amt      ", colStr("fst_twd_amt      "));
      wp.colSet("sec_dc_amt       ", colStr("fst_dc_amt       "));
      wp.colSet("sec_part_mark    ", colStr("fst_part_mark    "));
      wp.colSet("sec_expire_date  ", colStr("fst_expire_date  "));
      wp.colSet("sec_add_date     ", colStr("fst_add_date     "));
      wp.colSet("sec_add_user     ", colStr("fst_add_user     "));
      wp.colSet("sec_apr_date     ", colStr("fst_apr_date     "));
      wp.colSet("sec_apr_user     ", colStr("fst_apr_user     "));
      wp.colSet("sec_disb_yn      ", colStr("fst_disb_yn      "));
      wp.colSet("sec_disb_amt     ", colStr("fst_disb_amt     "));
      wp.colSet("sec_disb_dc_amt  ", colStr("fst_disb_dc_amt  "));
      wp.colSet("sec_disb_add_date", colStr("fst_disb_add_date"));
      wp.colSet("sec_disb_add_user", colStr("fst_disb_add_user"));
      wp.colSet("sec_disb_apr_date", colStr("fst_disb_apr_date"));
      wp.colSet("sec_disb_apr_user", colStr("fst_disb_apr_user"));
      wp.colSet("rowid", colStr("rowid"));
   }

   return 1;
}

@Override
public void dataCheck() {
   String lsRefno = wp.itemStr("reference_no");
   int liRefseq = (int) wp.itemNum("reference_seq");

   strSql = "select * from rsk_chgback where reference_no =? and reference_seq =?";   
   sqlSelect(strSql,new Object[] {lsRefno,liRefseq});
   if (sqlRowNum <= 0) {
      errmsg("扣款資料已不存; [%s-%s]", lsRefno, liRefseq);
      return;
   }

   if (!empty(colStr("final_close"))) {
      errmsg("此筆資料已結案, 不可進行沖銷處理");
      return;
   }

   if (!colEq("sub_stage", "30")) {
      errmsg("未覆核, 不可進行沖銷處理");
      return;
   }

   if (!colEq("chg_stage", "1") && !colEq("chg_stage", "3")) {
      errmsg("此階段, 不可進行沖銷處理");
      return;
   }

   if (colEq("fst_send_flag", "1")) {
      errmsg("此筆資料在待傳送狀態, 不可進行沖銷處理");
      return;
   }

   if (colInt("fst_send_cnt") == 0) {
      errmsg("此筆資料未曾傳送過, 不可進行沖銷處理");
      return;
   }

   if (colEq("fst_reverse_mark", "P")) {
      errmsg("此筆資料在沖銷待覆核中, 不可進行沖銷處理");
      return;
   }

   if (colEq("fst_reverse_mark", "R")) {
      errmsg("此筆資料在沖銷已覆核, 不可進行沖銷處理");
      return;
   }

}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "update rsk_chgback set" +
         " fst_reverse_mark ='P', fst_reverse_date =" + commSqlStr.sysYYmd +
         commSqlStr.modxxxSet(modUser, modPgm) +
         " where reference_no =?" +
         " and reference_seq =?";

   setString(wp.itemStr("reference_no"));
   setDouble(wp.itemNum("reference_seq"));

//	if(wp.item_eq("chg_times", "1")){
//		strSql = " update rsk_chgback set "
//				 + " fst_reverse_mark = 'P' , "
//				 + " fst_reverse_date = to_char(sysdate,'yyyymmdd') , "
//				 + " mod_user =:mod_user , "
//				 + " mod_time = sysdate , "
//				 + " mod_pgm =:mod_pgm , "
//				 + " mod_seqno = nvl(mod_seqno,0)+1 "
//				 + " where 1=1 "
//				 + " and reference_no =:reference_no "
//				 + " and reference_seq =:reference_seq  "
//				 ;
//	}	else	{
//		strSql = " update rsk_chgback set "
//				 + " sec_reverse_mark = 'P' , "
//				 + " sec_reverse_date = to_char(sysdate,'yyyymmdd') , "
//				 + " mod_user =:mod_user , "
//				 + " mod_time = sysdate , "
//				 + " mod_pgm =:mod_pgm , "
//				 + " mod_seqno = nvl(mod_seqno,0)+1 "
//				 + " where 1=1 "
//				 + " and reference_no =:reference_no "
//				 + " and reference_seq =:reference_seq  "
//				 ;
//	}

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("沖銷失敗");
   }

   return rc;
}

void dataCheckCancel() {
   if (!wp.itemEmpty("final_close")) {
      errmsg("此筆資料已結案, 不可進行沖銷取消處理");
      return;
   }

   if (!wp.itemEq("sub_stage", "30")) {
      errmsg("未覆核, 不可進行沖銷取消處理");
      return;
   }

   if (!wp.itemEq("chg_stage", "1") && !wp.itemEq("chg_stage", "3")) {
      errmsg("此階段, 不可進行沖銷取消處理");
      return;
   }

   if (wp.itemEq("fst_send_flag", "1")) {
      errmsg("此筆資料在待傳送狀態, 不可進行沖銷取消處理");
      return;
   }

   if (wp.itemNum("fst_send_cnt") == 0) {
      errmsg("此筆資料未曾傳送過, 不可進行沖銷取消處理");
      return;
   }

   if (wp.itemEq("fst_reverse_mark", "R")) {
      errmsg("此筆資料在沖銷已覆核狀態, 不可進行沖銷取消處理");
      return;
   }

   if (wp.itemEmpty("fst_reverse_mark")) {
      errmsg("此筆資料在非沖銷狀態, 不可進行沖銷取消處理");
      return;
   }

}

@Override
public int dbDelete() {
   dataCheckCancel();
   if (rc != 1)
      return rc;
   if (wp.itemEq("chg_times", "1")) {
      strSql = " update rsk_chgback set "
            + " fst_reverse_mark = '' , "
            + " fst_reverse_date = '' , "
            + " mod_user =:mod_user , "
            + " mod_time = sysdate , "
            + " mod_pgm =:mod_pgm , "
            + " mod_seqno = nvl(mod_seqno,0)+1 "
            + " where 1=1 "
            + " and reference_no =:reference_no "
            + " and reference_seq =:reference_seq  "
      ;
   }
   else {
      strSql = " update rsk_chgback set "
            + " sec_reverse_mark = '' , "
            + " sec_reverse_date = '' , "
            + " mod_user =:mod_user , "
            + " mod_time = sysdate , "
            + " mod_pgm =:mod_pgm , "
            + " mod_seqno = nvl(mod_seqno,0)+1 "
            + " where 1=1 "
            + " and reference_no =:reference_no "
            + " and reference_seq =:reference_seq  "
      ;
   }
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   item2ParmStr("reference_no");
   item2ParmNum("reference_seq");

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("沖銷取消失敗");
   }

   return rc;
}

}
