package rskm03;

import busi.FuncAction;
import taroko.base.Parm2Sql;

public class Rskm3260Func extends FuncAction {
String kk1 = "", kk2 = "";
Parm2Sql tt=new Parm2Sql();

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("case_no");
   if (this.ibAdd) {
      kk2 = wp.itemStr("kk_ctrl_seqno");
   }
   else {
      kk2 = wp.itemStr("ctrl_seqno");
   }

   if (empty(kk1) || empty(kk2)) {
      errmsg("Case No or Control No 不可空白 !");
      return;
   }

   if (this.ibDelete)
      return;

   if (this.ibAdd) {
      if (checkTxn() == false) {
         errmsg("Control No 在交易資料中已存在");
         return;
      }

      if (checkProc() == false) {
         errmsg("Control No 在交易處理資料中已存在");
         return;
      }
   }


}

boolean checkTxn()  {
   String sql1 = " select count(*) as ll_cnt "
         + " from rsk_ctfc_txn "
         + " where 1=1 "
         + " and ctrl_seqno = ? ";
   sqlSelect(sql1, new Object[]{kk2});
   return !(colNum("ll_cnt") > 0);

}

boolean checkProc()  {
   String sql1 = " select count(*) as ll_cnt "
         + " from rsk_ctfc_proc "
         + " where 1=1 "
         + " and ctrl_seqno = ? ";
   sqlSelect(sql1, new Object[]{kk2});
   return !(colNum("ll_cnt") > 0);
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "insert into rsk_ctfc_txn ("
         + " case_no ,"
         + " ctrl_seqno ,"
         + " on_us_flag ,"
         + " on_us_mcht_name ,"
         + " txn_date ,"
         + " arn_year ,"
         + " arn_ddd ,"
         + " txn_amt ,"
         + " dc_txn_amt ,"
         + " proc_status ,"
         + " ecs_close_date ,"
         + " ecs_close_reason ,"
         + " reference_no ,"
         + " reference_seq ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ("
         + " :kk1 ,"
         + " :kk2 ,"
         + " :on_us_flag ,"
         + " :on_us_mcht_name ,"
         + " :txn_date ,"
         + " :arn_year ,"
         + " :arn_ddd ,"
         + " :txn_amt ,"
         + " :dc_txn_amt ,"
         + " :proc_status ,"
         + " :ecs_close_date ,"
         + " :ecs_close_reason ,"
         + " :reference_no ,"
         + " :reference_seq ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " '1' "
         + " )";
   setString("kk1", kk1);
   setString("kk2", kk2);
   item2ParmNvl("on_us_flag", "N");
   item2ParmStr("on_us_mcht_name");
   item2ParmStr("txn_date");
   item2ParmStr("arn_year");
   item2ParmStr("arn_ddd");
   item2ParmNum("txn_amt");
   item2ParmNum("dc_txn_amt");
   item2ParmStr("proc_status");
   item2ParmStr("ecs_close_date");
   item2ParmStr("ecs_close_reason");
   item2ParmStr("reference_no");
   item2ParmNum("reference_seq");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   sqlExec(strSql);
   if (sqlRowNum <= 0)
      errmsg("insert rsk_ctfc_txn error !");


   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = " update rsk_ctfc_txn set "
         + " on_us_flag =:on_us_flag ,"
         + " on_us_mcht_name =:on_us_mcht_name ,"
         + " txn_date =:txn_date ,"
         + " arn_year =:arn_year ,"
         + " arn_ddd =:arn_ddd ,"
         + " txn_amt =:txn_amt ,"
         + " dc_txn_amt =:dc_txn_amt ,"
         + " proc_status =:proc_status ,"
         + " ecs_close_date =:ecs_close_date ,"
         + " ecs_close_reason =:ecs_close_reason ,"
         + " mod_user =:mod_user ,"
         + " mod_time =sysdate ,"
         + " mod_pgm =:modPgm ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where case_no =:kk1 "
         + " and ctrl_seqno =:kk2 "
   ;

   item2ParmNvl("on_us_flag", "N");
   item2ParmStr("on_us_mcht_name");
   item2ParmStr("txn_date");
   item2ParmStr("arn_year");
   item2ParmStr("arn_ddd");
   item2ParmNum("txn_amt");
   item2ParmNum("dc_txn_amt");
   item2ParmStr("proc_status");
   item2ParmStr("ecs_close_date");
   item2ParmStr("ecs_close_reason");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   setString("kk1", kk1);
   setString("kk2", kk2);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfc_txn error !");
   }

   return rc;
}

@Override
public int dbDelete()  {
   actionInit("D");
   dataCheck();
   if (rc != 1)
      return rc;
   strSql = " delete rsk_ctfc_txn where case_no=:kk1 and ctrl_seqno=:kk2 ";
   setString("kk1", kk1);
   setString("kk2", kk2);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delect rsk_ctfc_txn error !");
   }
   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

void dataCheck_call() {
   if (empty(wp.itemStr("ex_call_date")) || empty(wp.itemStr("ex_call_time"))) {
      errmsg("記錄日期/時間 不可空白");
      return;
   }

   if (empty(wp.itemStr("ex_tel_no")) && (empty(wp.itemStr("ex_call_desc")) && empty(wp.itemStr("ex_call_desc2"))) && empty(wp.itemStr("ex_attn_man"))) {
      errmsg("[電話],[通話對象],[內容] 不可全部空白");
      return;
   }

}

public int insert_call()  {
   msgOK();
   dataCheck_call();
   if (rc != 1)
      return rc;
   String ls_desc = wp.itemStr("ex_call_desc") + wp.itemStr("ex_call_desc2");
   strSql = "insert into rsk_ctfc_call ("
         + " case_no ,"
         + " call_date ,"
         + " call_time ,"
         + " tel_no ,"
         + " attn_man ,"
         + " call_desc ,"
         + " proc_user ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ("
         + " :case_no ,"
         + " :call_date ,"
         + " :call_time ,"
         + " :tel_no ,"
         + " :attn_man ,"
         + " :call_desc ,"
         + " :proc_user ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " '1' "
         + " )";
   item2ParmStr("case_no");
   item2ParmStr("call_date", "ex_call_date");
   item2ParmStr("call_time", "ex_call_time");
   item2ParmStr("tel_no", "ex_tel_no");
   item2ParmStr("attn_man", "ex_attn_man");
   setString("call_desc", ls_desc);
   setString("proc_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("新增明細失敗 !");
   }
   return rc;
}

public int saveProc()  {
   msgOK();
   if (checkProc2() == false) {
      insert_rsk_ctfc_Proc();
   }
   else if (checkProc2()) {
      update_rsk_ctfc_Proc();
   }
   return rc;
}

boolean checkProc2()  {
   String sql1 = " select count(*) as db_cnt "
         + " from rsk_ctfc_proc "
         + " where ctrl_seqno = ? ";
   sqlSelect(sql1, new Object[]{wp.itemStr("ctrl_seqno")});
   return !(colNum("db_cnt") <= 0);
}

void insert_rsk_ctfc_Proc()  {

   tt.insert("rsk_ctfc_proc");
   tt.parmSet("ctrl_seqno", wp.itemStr("ctrl_seqno"));
   tt.parmSet("hold_date", wp.itemStr("hold_date"));
   tt.parmSet("hold_amt", wp.num("hold_amt"));
   tt.parmSet("hold_reason", wp.itemStr("hold_reason"));
   tt.parmSet("recv_date", wp.itemStr("recv_date"));
   tt.parmSet("recv_resp_date", wp.itemStr("recv_resp_date"));
   tt.parmSet("cb_date_1st", wp.itemStr("cb_date_1st"));
   tt.parmSet("cb_reason_1st", wp.itemStr("cb_reason_1st"));
   tt.parmSet("cb_amt_1st", wp.num("cb_amt_1st"));
   tt.parmSet("rp_date", wp.itemStr("rp_date"));
   tt.parmSet("cb_date_2nd", wp.itemStr("cb_date_2nd"));
   tt.parmSet("cb_reason_2nd", wp.itemStr("cb_reason_2nd"));
   tt.parmSet("cb_amt_2nd", wp.num("cb_amt_2nd"));
   tt.parmSet("pre_arbi_date", wp.itemStr("pre_arbi_date"));
   tt.parmSet("pre_arbi_flag", wp.itemStr("pre_arbi_flag"));
   tt.parmSet("arbi_date", wp.itemStr("arbi_date"));
   tt.parmSet("arbi_result", wp.itemStr("arbi_result"));
   tt.parmSet("pre_comp_date", wp.itemStr("pre_comp_date"));
   tt.parmSet("pre_comp_reason", wp.itemStr("pre_comp_reason"));
   tt.parmSet("pre_comp_amt", wp.num("pre_comp_amt"));
   tt.parmSet("pre_comp_flag", wp.itemStr("pre_comp_flag"));
   tt.parmSet("comp_date", wp.itemStr("comp_date"));
   tt.parmSet("comp_result", wp.itemStr("comp_result"));
// tt.parmSet();aa("faith_date", wp.itemStr("faith_date"));
// tt.parmSet();aa("faith_flag", wp.itemStr("faith_flag"));
   tt.parmSet("hold_code", wp.itemStr("hold_code"));
   tt.parmSet("cb_code_1st", wp.itemStr("cb_code_1st"));
   tt.parmSet("cb_code_2nd", wp.itemStr("cb_code_2nd"));
   tt.modxxxSet(modUser,modPgm);

   sqlExec(tt.getSql(),tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfc_proc error !");
   }
   return;
}

void update_rsk_ctfc_Proc()  {
   msgOK();

   tt.update("rsk_ctfc_proc");
         //aaa("ctrl_seqno", wp.itemStr("ctrl_seqno"));
   tt.parmSet("hold_date", wp.itemStr("hold_date"));
   tt.parmSet("hold_amt", wp.num("hold_amt"));
   tt.parmSet("hold_reason", wp.itemStr("hold_reason"));
   tt.parmSet("recv_date", wp.itemStr("recv_date"));
   tt.parmSet("recv_resp_date", wp.itemStr("recv_resp_date"));
   tt.parmSet("cb_date_1st", wp.itemStr("cb_date_1st"));
   tt.parmSet("cb_reason_1st", wp.itemStr("cb_reason_1st"));
   tt.parmSet("cb_amt_1st", wp.num("cb_amt_1st"));
   tt.parmSet("rp_date", wp.itemStr("rp_date"));
   tt.parmSet("cb_date_2nd", wp.itemStr("cb_date_2nd"));
   tt.parmSet("cb_reason_2nd", wp.itemStr("cb_reason_2nd"));
   tt.parmSet("cb_amt_2nd", wp.num("cb_amt_2nd"));
   tt.parmSet("pre_arbi_date", wp.itemStr("pre_arbi_date"));
   tt.parmSet("pre_arbi_flag", wp.itemStr("pre_arbi_flag"));
   tt.parmSet("arbi_date", wp.itemStr("arbi_date"));
   tt.parmSet("arbi_result", wp.itemStr("arbi_result"));
   tt.parmSet("pre_comp_date", wp.itemStr("pre_comp_date"));
   tt.parmSet("pre_comp_reason", wp.itemStr("pre_comp_reason"));
   tt.parmSet("pre_comp_amt", wp.num("pre_comp_amt"));
   tt.parmSet("pre_comp_flag", wp.itemStr("pre_comp_flag"));
   tt.parmSet("comp_date", wp.itemStr("comp_date"));
   tt.parmSet("comp_result", wp.itemStr("comp_result"));
   tt.parmSet("faith_date", wp.itemStr("faith_date"));
   tt.parmSet("faith_flag", wp.itemStr("faith_flag"));
   tt.parmSet("hold_code", wp.itemStr("hold_code"));
   tt.parmSet("cb_code_1st", wp.itemStr("cb_code_1st"));
   tt.parmSet("cb_code_2nd", wp.itemStr("cb_code_2nd"));
   tt.modxxxSet(modUser,modPgm);
   tt.whereParm(" where ctrl_seqno =?", wp.itemStr("ctrl_seqno"));

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfc_proc error !");
   }
   return;
}

public int delete_rsk_ctfc_Proc()  {
   msgOK();
   strSql = " delete rsk_ctfc_proc where ctrl_seqno =?";
   setParm(1, wp.itemStr("ctrl_seqno"));

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("delete rsk_ctfc_proc error !");
   }
   return rc;

}

}

