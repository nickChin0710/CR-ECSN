package rskm03;

import taroko.base.Parm2Sql;

/**
 * 2019-0816   JH    modify
 */

public class Rskm3220Func extends busi.FuncAction {
String kk1 = "";

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

public int dataSelect()  {
   kk1 = wp.itemStr("card_no");
   strSql = "select rsk_ctfi_warn.*" +
         ", to_char(mod_time,'yyyymmdd') as mod_date" +
         ", hex(rowid) as rowid_warn" +
         ", mod_seqno as mod_seqno_warn" +
         " from rsk_ctfi_warn" +
         " where card_no =?";
   setParm(1, kk1);
   sqlSelectWp(strSql);
   return sqlRowNum;
}

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("card_no");
   if (this.isAdd()) {
      return;
   }

   sqlWhere = " where 1=1"
         + " and card_no='" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.itemStr("mod_seqno_warn");

   if (this.isOtherModify("rsk_ctfi_warn", sqlWhere)) {
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
   Parm2Sql tt=new Parm2Sql();
   tt.insert("rsk_ctfi_warn");
   tt.parmSet("card_no", wp.itemStr("card_no"));
   tt.parmSet("fra_fir_date", wp.itemStr("fra_fir_date"));
   tt.parmSet("cntl_date", wp.itemStr("cntl_date"));
   tt.parmSet("cntl_user", wp.itemStr("cntl_user"));
   tt.parmSet("source_type", wp.itemStr("source_type"));
   tt.parmSet("warn_flag", wp.itemNvl("warn_flag", "N"));
   tt.parmSet("cris_ns_alerts", wp.itemNvl("cris_ns_alerts", "否"));
   tt.parmSet("n_rec_alerts", wp.itemNum("n_rec_alerts"));
   tt.parmSet("first_score", wp.itemNum("first_score"));
   tt.parmSet("imput_cnt", wp.itemNum("imput_cnt"));
   tt.parmSet("sms_flag", wp.itemNvl("sms_flag", "N"));
   tt.parmSet("sms_result", wp.itemStr("sms_result"));
   tt.parmSet("sms_reason", wp.itemStr("sms_reason"));
   tt.parmSet("d1_amt", wp.itemNum("d1_amt"));
   tt.parmSet("authed_amt", wp.itemNum("authed_amt"));
   tt.parmSet("d6_amt", wp.itemNum("d6_amt"));
   tt.parmSet("otb_amt", wp.itemNum("otb_amt"));
   tt.parmSet("susp_poct", wp.itemStr("susp_poct"));
   tt.parmSet("poc_terminal", wp.itemStr("poc_terminal"));
   tt.parmSet("skim_type", wp.itemStr("skim_type"));
   tt.parmSet("skim_period", wp.itemStr("skim_period"));
   tt.parmSet("poc_mcc", wp.itemStr("poc_mcc"));
   tt.parmSet("chg_card_date", wp.itemStr("chg_card_date"));
   tt.parmSet("cntl_remark", wp.itemStr("cntl_remark"));
   tt.parmSet("line_flag", wp.itemNvl("line_flag", "N"));
   tt.parmSet("crt_user", modUser);
   tt.parmYmd("crt_date");
   tt.modxxxSet(modUser, modPgm);

   this.sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("Insert rsk_ctfi_warn error; " + getMsg());
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
   taroko.base.Parm2Sql tt=new Parm2Sql();
   tt.update("rsk_ctfi_warn");
   tt.parmSet("fra_fir_date", wp.itemStr("fra_fir_date"));
   tt.parmSet("cntl_date", wp.itemStr("cntl_date"));
   tt.parmSet("cntl_user", wp.itemStr("cntl_user"));
   tt.parmSet("source_type", wp.itemStr("source_type"));
   tt.parmSet("warn_flag", wp.itemNvl("warn_flag", "N"));
   tt.parmSet("cris_ns_alerts", wp.itemNvl("cris_ns_alerts", "否"));
   tt.parmSet("n_rec_alerts", wp.itemNum("n_rec_alerts"));
   tt.parmSet("first_score", wp.itemNum("first_score"));
   tt.parmSet("imput_cnt", wp.itemNum("imput_cnt"));
   tt.parmSet("sms_flag", wp.itemNvl("sms_flag", "N"));
   tt.parmSet("sms_result", wp.itemStr("sms_result"));
   tt.parmSet("sms_reason", wp.itemStr("sms_reason"));
   tt.parmSet("d1_amt", wp.num("d1_amt"));
   tt.parmSet("authed_amt", wp.num("authed_amt"));
   tt.parmSet("d6_amt", wp.num("d6_amt"));
   tt.parmSet("otb_amt", wp.num("otb_amt"));
   tt.parmSet("susp_poct", wp.itemStr("susp_poct"));
   tt.parmSet("poc_terminal", wp.itemStr("poc_terminal"));
   tt.parmSet("skim_type", wp.itemStr("skim_type"));
   tt.parmSet("skim_period", wp.itemStr("skim_period"));
   tt.parmSet("poc_mcc", wp.itemStr("poc_mcc"));
   tt.parmSet("chg_card_date", wp.itemStr("chg_card_date"));
   tt.parmSet("cntl_remark", wp.itemStr("cntl_remark"));
   tt.parmSet("line_flag", wp.itemNvl("line_flag", "N"));
   tt.modxxxSet(modUser, modPgm);
   tt.whereParm("where card_no =?", kk1);
   tt.whereParm(" and nvl(mod_seqno,0) =?", wp.itemNum("mod_seqno_warn"));

   rc = sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_warn error: " + this.sqlErrtext);
   }

   return rc;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc() {
   return 0;
}

}
