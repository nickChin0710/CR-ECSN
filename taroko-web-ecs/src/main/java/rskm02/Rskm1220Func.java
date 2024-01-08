package rskm02;

import busi.FuncAction;

public class Rskm1220Func extends FuncAction {
String kk1 = "";


@Override
public void dataCheck() {
   if (this.ibAdd) {
      kk1 = wp.itemStr("kk_risk_group");
   }
   else {
      kk1 = wp.itemStr("risk_group");
   }
   if (this.ibDelete) {
      return;
   }
   String lsCorpCrAbnor = wp.itemNvl("db_corp_cr_abnor_a", "N")
         + wp.itemNvl("db_corp_cr_abnor_b", "N")
         + wp.itemNvl("db_corp_cr_abnor_c", "N")
         + wp.itemNvl("db_corp_cr_abnor_d", "N")
         + wp.itemNvl("db_corp_cr_abnor_n", "N");
   String lsCorpAddNote = wp.itemNvl("db_corp_add_note_a", "N")
         + wp.itemNvl("db_corp_add_note_b", "N")
         + wp.itemNvl("db_corp_add_note_c", "N");
   String lsIdnoCrAbnor = wp.itemNvl("db_idno_cr_abnor_a", "N")
         + wp.itemNvl("db_idno_cr_abnor_b", "N")
         + wp.itemNvl("db_idno_cr_abnor_c", "N")
         + wp.itemNvl("db_idno_cr_abnor_n", "N");
   int liNum = wp.itemStr("inrate_final_val").split(",").length;
   if (liNum > 20) {
      errmsg("內部最終評等 最多可設20組");
      return;
   }
   if (wp.itemEq("capital_cond", "Y")) {
      if (wp.itemNum("capital_amt_b") > wp.itemNum("capital_amt_e")) {
         errmsg("3.資本額區間 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("capital_amt_e", "0");
      wp.itemSet("capital_amt_b", "0");
   }

   if (wp.itemEq("inrate_date_cond", "Y")) {
      if (wp.itemNum("inrate_date_mm") <= 0) {
         errmsg("5.最近內部評等日期距今N月 不可<= 0");
         return;
      }
   }
   else {
      wp.itemSet("inrate_date_mm", "0");
   }
/*				
		if(wp.itemEq("inrate_final_cond","Y")){
			if(empty(wp.itemStr("inrate_final_val"))){
				errmsg("6.最近內部模型最終評等 不可空白");
				return;
			}
		}	else {
			wp.itemSet("inrate_final_val", "");
		}
*/
   if (wp.itemEq("avg6_depos_cond", "Y")) {
      if (wp.itemNum("avg6_depos_b") > wp.itemNum("avg6_depos_e")) {
         errmsg("8.近6個月平均存款餘額區間 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("avg6_depos_b", "0");
      wp.itemSet("avg6_depos_e", "0");
   }

   if (wp.itemEq("cr_ovdue30_bal_cond", "Y")) {
      if (wp.itemNum("cr_ovdue30_bal_b") > wp.itemNum("cr_ovdue30_bal_e")) {
         errmsg("9.授信逾期1-30日總餘額 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("cr_ovdue30_bal_b", "0");
      wp.itemSet("cr_ovdue30_bal_e", "0");
   }

   if (wp.itemEq("avg3_crlimit_rate_cond", "Y")) {
      if (wp.itemNum("avg3_crlimit_rate_b") > wp.itemNum("avg3_crlimit_rate_e")) {
         errmsg("10.平均授信額度3個月期成長率區間 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("avg3_crlimit_rate_e", "0");
      wp.itemSet("avg3_crlimit_rate_b", "0");
   }

   if (wp.itemEq("avg6_crlimit_rate_cond", "Y")) {
      if (wp.itemNum("avg6_crlimit_rate_b") > wp.itemNum("avg6_crlimit_rate_e")) {
         errmsg("11.平均授信額度6個月期成長率區間 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("avg6_crlimit_rate_e", "0");
      wp.itemSet("avg6_crlimit_rate_b", "0");
   }

   if (wp.itemEq("conti_no_consum_cond", "Y")) {
      if (wp.itemNum("conti_no_consum_mm") <= 0 || wp.itemNum("conti_no_consum_mm") > 24) {
         errmsg("12.最近連續N個月未消費且無欠款 輸入錯誤 , 須為 1 ~ 24");
      }
   }
   else {
      wp.itemSet("conti_no_consum_mm", "0");
   }

   if (wp.itemEq("corp_cr_abnor_cond", "Y") && eqIgno(lsCorpCrAbnor, "NNNNN")) {
      errmsg("21.公司信用異常 須指定條件");
      return;
   }

   if (wp.itemEq("corp_add_note_cond", "Y") && eqIgno(lsCorpAddNote, "NNN")) {
      errmsg("12.公司是否有補充註記 須指定條件");
      return;
   }

   if (wp.itemEq("idno6_late_pay_cond", "Y")) {
      if (wp.itemNum("idno6_late_pay_cnt_b") > wp.itemNum("idno6_late_pay_cnt_e")) {
         errmsg("24.負責人信用卡近6個月總遲繳次數 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("idno6_late_pay_cnt_e", "0");
      wp.itemSet("idno6_late_pay_cnt_b", "0");
   }

   if (wp.itemEq("idno_cr_abnor_cond", "Y") && eqIgno(lsIdnoCrAbnor, "NNNN")) {
      errmsg("25.負責人信用異常 須指定條件");
      return;
   }

   if (wp.itemEq("idno_unsecu_bal_cond", "Y")) {
      if (wp.itemNum("idno_unsecu_bal_b") > wp.itemNum("idno_unsecu_bal_e")) {
         errmsg("27.負責人無擔保負債總餘額區間 輸入錯誤");
         return;
      }
   }
   else {
      wp.itemSet("idno_unsecu_bal_e", "0");
      wp.itemSet("idno_unsecu_bal_b", "0");
   }

}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   insertDetl();
   return rc;
}

@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   // -delete temp-data-
   strSql = "delete RSK_TRCORP_PARM" + " where risk_group =?" + " and apr_flag<>'Y'";
   this.sqlExec(strSql, new Object[]{
         wp.itemStr("risk_group")
   });
   insertDetl();
   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "delete RSK_TRCORP_PARM "
         + " where risk_group =:kk1"
         + " and apr_flag =:apr_flag "
         + " and nvl(mod_seqno,0) =:mod_seqno ";
   setString("kk1", kk1);
   item2ParmNum("mod_seqno");
   item2ParmStr("apr_flag");
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   if (rc == 1) {
      deleteData();
   }
   return rc;

}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

public int insertDetl() {
   String lsCorpCrAbnor = wp.itemNvl("db_corp_cr_abnor_a", "N")
         + wp.itemNvl("db_corp_cr_abnor_b", "N")
         + wp.itemNvl("db_corp_cr_abnor_c", "N")
         + wp.itemNvl("db_corp_cr_abnor_d", "N")
         + wp.itemNvl("db_corp_cr_abnor_n", "N");
   String lsCorpAddNote = wp.itemNvl("db_corp_add_note_a", "N")
         + wp.itemNvl("db_corp_add_note_b", "N")
         + wp.itemNvl("db_corp_add_note_c", "N");
   String lsIdnoCrAbnor = wp.itemNvl("db_idno_cr_abnor_a", "N")
         + wp.itemNvl("db_idno_cr_abnor_b", "N")
         + wp.itemNvl("db_idno_cr_abnor_c", "N")
         + wp.itemNvl("db_idno_cr_abnor_n", "N");

   strSql =
         "insert into RSK_TRCORP_PARM ("
               + "risk_group, "   //1
               + "risk_group_desc ,"
               + "acct_pay_type ,"
               + "branch_exchg_type ,"
               + "capital_cond ,"   //5
               + "capital_amt_b ,"
               + "capital_amt_e ,"
               + "mcode_01_cond ,"
               + "mcode_01_yn ,"
               + "inrate_date_cond ,"  //10
               + "inrate_date_mm ,"
               + "inrate_final_cond ,"
               + "inrate_final_val ,"
               + "mm_depos_cond ,"
               + "mm_depos_b ,"           //15
               + "mm_depos_e ,"
               + "avg6_depos_cond ,"
               + "avg6_depos_b ,"
               + "avg6_depos_e ,"
               + "cr_ovdue30_bal_cond ,"//20
               + "cr_ovdue30_bal_b ,"
               + "cr_ovdue30_bal_e ,"
               + "avg3_crlimit_rate_cond ,"
               + "avg3_crlimit_rate_b ,"
               + "avg3_crlimit_rate_e ,"  //25
               + "avg6_crlimit_rate_cond ,"
               + "avg6_crlimit_rate_b ,"
               + "avg6_crlimit_rate_e ,"
               + "conti_no_consum_cond ,"
               + "conti_no_consum_mm ,"   //30
               + "corp_curr_0_cond ,"
               + "corp_curr_0_code ,"
               + "corp_abnor_cond ,"
               + "corp_abnor_yn ,"
               + "corp12_ovdue_cond ,"    //35
               + "corp12_ovdue_yn ,"
               + "corp_cr_abnor_cond ,"
               + "corp_add_note_cond ,"
               + "corp_rept_case_cond ,"
               + "corp_rept_case_yn ,"   //40
               + "idno6_late_pay_cond ,"
               + "idno6_late_pay_cnt_b ,"
               + "idno6_late_pay_cnt_e ,"
               + "idno_cr_abnor_cond ,"
               + "idno12_ovdue_cond ,"    //45
               + "idno12_ovdue_yn ,"
               + "idno_unsecu_bal_cond ,"
               + "idno_unsecu_bal_b ,"
               + "idno_unsecu_bal_e ,"
               + "crt_user ,"         //50
               + "crt_date ,"
               + " apr_flag,"
               + "apr_user ,"
               + "apr_date ,"
               + "idno_cr_abnor_val ,"    //55
               + "corp_add_note_val ,"
               + "corp_cr_abnor_val ,"
               + "corp_imp_cond ,"
               + "corp_jcic_send ,"    //60
               + "idno_jcic_send ,"
               + "issue_card_cond ,"   //65
               + "issue_card_mm ,"
               + "block_reason_cond ,"
               + " mod_user,"
               + " mod_time,"
               + " mod_pgm,"//70
               + " mod_seqno"//71
               + " ) values ("
               + ":risk_group, "//1
               + ":risk_group_desc ,"
               + ":acct_pay_type ,"
               + ":branch_exchg_type ,"
               + ":capital_cond ,"   //5
               + ":capital_amt_b ,"
               + ":capital_amt_e ,"
               + ":mcode_01_cond ,"
               + ":mcode_01_yn ,"
               + ":inrate_date_cond ,"  //10
               + ":inrate_date_mm ,"
               + ":inrate_final_cond ,"
               + "'' ,"
               + ":mm_depos_cond ,"
               + ":mm_depos_b ,"           //15
               + ":mm_depos_e ,"
               + ":avg6_depos_cond ,"
               + ":avg6_depos_b ,"
               + ":avg6_depos_e ,"
               + ":cr_ovdue30_bal_cond ,"//20
               + ":cr_ovdue30_bal_b ,"
               + ":cr_ovdue30_bal_e ,"
               + ":avg3_crlimit_rate_cond ,"
               + ":avg3_crlimit_rate_b ,"
               + ":avg3_crlimit_rate_e ,"  //25
               + ":avg6_crlimit_rate_cond ,"
               + ":avg6_crlimit_rate_b ,"
               + ":avg6_crlimit_rate_e ,"
               + ":conti_no_consum_cond ,"
               + ":conti_no_consum_mm ,"   //30
               + ":corp_curr_0_cond ,"
               + ":corp_curr_0_code ,"
               + ":corp_abnor_cond ,"
               + ":corp_abnor_yn ,"
               + ":corp12_ovdue_cond ,"     //35
               + ":corp12_ovdue_yn ,"
               + ":corp_cr_abnor_cond ,"
               + ":corp_add_note_cond ,"
               + ":corp_rept_case_cond ,"
               + ":corp_rept_case_yn ,"    //40
               + ":idno6_late_pay_cond ,"
               + ":idno6_late_pay_cnt_b ,"
               + ":idno6_late_pay_cnt_e ,"
               + ":idno_cr_abnor_cond ,"
               + ":idno12_ovdue_cond ,"    //45
               + ":idno12_ovdue_yn ,"
               + ":idno_unsecu_bal_cond ,"
               + ":idno_unsecu_bal_b ,"
               + ":idno_unsecu_bal_e ,"
               + ":crt_user ,"         //50
               + "to_char(sysdate,'yyyymmdd') ,"
               + "'N',"
               + "'' ,"
               + "'' ,"
               + ":idno_cr_abnor_val ,"    //55
               + ":corp_add_note_val ,"
               + ":corp_cr_abnor_val ,"
               + ":corp_imp_cond ,"
               + ":corp_jcic_send ,"    //60
               + ":idno_jcic_send ,"
               + ":issue_card_cond ,"   //65
               + ":issue_card_mm ,"
               + ":block_reason_cond ,"
               + ":mod_user,"
               + "sysdate,"
               + ":mod_pgm,"//70
               + "1"//71
               + " )";
   // -set ?value-
   try {
      setString("risk_group", kk1);//1
      item2ParmStr("risk_group_desc");
      item2ParmStr("acct_pay_type");
      item2ParmStr("branch_exchg_type");
      item2ParmNvl("capital_cond", "N");//5
      item2ParmNum("capital_amt_b");
      item2ParmNum("capital_amt_e");
      item2ParmNvl("mcode_01_cond", "N");
      item2ParmNvl("mcode_01_yn", "N");
      item2ParmNvl("inrate_date_cond", "N");//10
      item2ParmNum("inrate_date_mm");
      item2ParmNvl("inrate_final_cond", "N");
      item2ParmNvl("mm_depos_cond", "N");
      item2ParmNum("mm_depos_b");
      item2ParmNum("mm_depos_e");           //15
      item2ParmNvl("avg6_depos_cond", "N");
      item2ParmNum("avg6_depos_b");
      item2ParmNum("avg6_depos_e");
      item2ParmNvl("cr_ovdue30_bal_cond", "N");  //20
      item2ParmNum("cr_ovdue30_bal_b");
      item2ParmNum("cr_ovdue30_bal_e");
      item2ParmNvl("avg3_crlimit_rate_cond", "N");
      item2ParmNum("avg3_crlimit_rate_b");
      item2ParmNum("avg3_crlimit_rate_e");  //25
      item2ParmNvl("avg6_crlimit_rate_cond", "N");
      item2ParmNum("avg6_crlimit_rate_b");
      item2ParmNum("avg6_crlimit_rate_e");
      item2ParmNvl("conti_no_consum_cond", "N");
      item2ParmNum("conti_no_consum_mm");   //30
      item2ParmNvl("corp_curr_0_cond", "N");
      item2ParmNvl("corp_curr_0_code", "N");
      item2ParmNvl("corp_abnor_cond", "N");
      item2ParmNvl("corp_abnor_yn", "N");
      item2ParmNvl("corp12_ovdue_cond", "N");    //35
      item2ParmNvl("corp12_ovdue_yn", "N");
      item2ParmNvl("corp_cr_abnor_cond", "N");
      item2ParmNvl("corp_add_note_cond", "N");
      item2ParmNvl("corp_rept_case_cond", "N");
      item2ParmNvl("corp_rept_case_yn", "N");    //40
      item2ParmNvl("idno6_late_pay_cond", "N");
      item2ParmNum("idno6_late_pay_cnt_b");
      item2ParmNum("idno6_late_pay_cnt_e");
      item2ParmNvl("idno_cr_abnor_cond", "N");
      item2ParmNvl("idno12_ovdue_cond", "N");    //45
      item2ParmNvl("idno12_ovdue_yn", "N");
      item2ParmNvl("idno_unsecu_bal_cond", "N");
      item2ParmNum("idno_unsecu_bal_b");
      item2ParmNum("idno_unsecu_bal_e");
      setString("crt_user", wp.loginUser);            //50
      setString("idno_cr_abnor_val", lsIdnoCrAbnor);
      setString("corp_add_note_val", lsCorpAddNote);
      setString("corp_cr_abnor_val", lsCorpCrAbnor);
      item2ParmNvl("corp_imp_cond", "N");
      item2ParmNvl("corp_jcic_send", "N");
      item2ParmNvl("idno_jcic_send", "N");
      item2ParmNvl("issue_card_cond", "N");
      item2ParmNum("issue_card_mm");
      item2ParmNvl("block_reason_cond", "N");
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.loginUser);//65
   }
   catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
   }

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("insert RSK_TRIAL_PARM error; " + sqlErrtext);
   }
   return rc;
}

public int insertData() throws Exception  {

   msgOK();
   strSql = "insert into RSK_TRCORP_PARMDTL ("
         + " risk_group, " // 1
         + " data_type, "
         + " data_code, "
         + " data_code2, "
         + " apr_flag, " // 5
         + " type_desc, "
         + " mod_user, mod_time, mod_pgm"
         + " ) values ("
         + " :risk_group"
         + ", :data_type"
         + ", :data_code"
         + ", ''"
         + ", 'N'"
         + ", :type_desc"
         + ",:mod_user,sysdate,:mod_pgm"
         + " )";
   item2ParmStr("risk_group");
   item2ParmStr("data_type");
   item2ParmStr("data_code");
   item2ParmStr("type_desc");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert RSK_TRCORP_PARMDTL error, " + getMsg());
   }

   return rc;

}

public int dbInsertDetl() throws Exception  {
   msgOK();
   strSql = "insert into RSK_TRCORP_PARMDTL ("
         + " risk_group, " // 1
         + " data_type, "
         + " data_code, "
         + " data_code2, "
         + " apr_flag, " // 5
         + " type_desc, "
         + " mod_user, mod_time, mod_pgm"
         + " ) values ("
         + " :risk_group"
         + ", :data_type"
         + ", :data_code"
         + ", :data_code2"
         + ", 'N'"
         + ", :type_desc"
         + ",:mod_user,sysdate,:mod_pgm"
         + " )";

   var2ParmStr("risk_group");
   var2ParmStr("data_type");
   var2ParmStr("data_code");
   var2ParmStr("data_code2");
   var2ParmStr("type_desc");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert RSK_TRCORP_PARMDTL error, " + getMsg());
   }

   return rc;
}

public int dbDeleteDetl()  throws Exception {
   msgOK();
   strSql =
         "Delete RSK_TRCORP_PARMDTL"
               + " where nvl(apr_flag,'N')<>'Y' "
        	   + " and risk_group =:risk_group "
               + " and data_type =:data_type "
               + " and nvl(apr_flag,'N') =:apr_flag";
   var2ParmStr("risk_group");
   var2ParmStr("data_type");
   var2ParmNvl("apr_flag", "N");   
   
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parmdtl err=" + getMsg());
      rc = -1;
   }

   return rc;
}

public int deleteData() {
   msgOK();
   strSql =
         "Delete RSK_TRCORP_PARMDTL"
               + " where nvl(apr_flag,'N')<>'Y' "
               + " and risk_group =:risk_group "
               + " and data_type =:data_type "
               + " and nvl(apr_flag,'N') ='N'";
   var2ParmStr("risk_group");
   var2ParmStr("data_type");
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete rsk_trial_parmdtl err=" + getMsg());
      rc = -1;
   }

   return rc;
}

public int copyData() throws Exception  {
   int llRowCnt = 0;
   msgOK();
   String sql1 = " select "
         + " * "
         + " from rsk_trcorp_parm "
         + " where risk_group = ? "
         + " and apr_flag = ? ";

   sqlSelect(sql1, new Object[]{wp.itemStr("risk_group"), wp.itemStr("apr_flag")});

   if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return rc;
   }

   //--delete N

   strSql = " delete rsk_trcorp_parm where risk_group =:risk_group and apr_flag = 'N' ";
   item2ParmStr("risk_group");
   sqlExec(strSql);

   if (sqlRowNum < 0) {
      errmsg("異動處理失敗");
      return rc;
   }
   else rc = 1;

   strSql = " insert into rsk_trcorp_parm ("
         + " risk_group ,"
         + " risk_group_desc ,"
         + " acct_pay_type ,"
         + " branch_exchg_type ,"
         + " capital_cond ,"
         + " capital_amt_b ,"
         + " capital_amt_e ,"
         + " mcode_01_cond ,"
         + " mcode_01_yn ,"
         + " inrate_date_cond ,"
         + " inrate_date_mm ,"
         + " inrate_final_cond ,"
         + " inrate_final_val ,"
         + " mm_depos_cond ,"
         + " mm_depos_b ,"
         + " mm_depos_e ,"
         + " avg6_depos_cond ,"
         + " avg6_depos_b ,"
         + " avg6_depos_e ,"
         + " cr_ovdue30_bal_cond ,"
         + " cr_ovdue30_bal_b ,"
         + " cr_ovdue30_bal_e ,"
         + " avg3_crlimit_rate_cond ,"
         + " avg3_crlimit_rate_b ,"
         + " avg3_crlimit_rate_e ,"
         + " avg6_crlimit_rate_cond ,"
         + " avg6_crlimit_rate_b ,"
         + " avg6_crlimit_rate_e ,"
         + " conti_no_consum_cond ,"
         + " conti_no_consum_mm ,"
         + " corp_curr_0_cond ,"
         + " corp_curr_0_code ,"
         + " corp_abnor_cond ,"
         + " corp_abnor_yn ,"
         + " corp12_ovdue_cond ,"
         + " corp12_ovdue_yn ,"
         + " corp_cr_abnor_cond ,"
         + " corp_cr_abnor_val ,"
         + " corp_add_note_cond ,"
         + " corp_add_note_val ,"
         + " corp_rept_case_cond ,"
         + " corp_rept_case_yn ,"
         + " idno6_late_pay_cond ,"
         + " idno6_late_pay_cnt_b ,"
         + " idno6_late_pay_cnt_e ,"
         + " idno_cr_abnor_cond ,"
         + " idno_cr_abnor_val ,"
         + " idno12_ovdue_cond ,"
         + " idno12_ovdue_yn ,"
         + " idno_unsecu_bal_cond ,"
         + " idno_unsecu_bal_b ,"
         + " idno_unsecu_bal_e ,"
         + " corp_imp_cond ,"
         + " corp_jcic_send ,"
         + " idno_jcic_send ,"
         + " issue_card_cond ,"
         + " issue_card_mm ,"
         + " block_reason_cond ,"
         + " crt_user ,"
         + " crt_date ,"
         + " apr_flag ,"
         + " apr_date ,"
         + " apr_user ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ( "
         + " :risk_group ,"
         + " :risk_group_desc ,"
         + " :acct_pay_type ,"
         + " :branch_exchg_type ,"
         + " :capital_cond ,"
         + " :capital_amt_b ,"
         + " :capital_amt_e ,"
         + " :mcode_01_cond ,"
         + " :mcode_01_yn ,"
         + " :inrate_date_cond ,"
         + " :inrate_date_mm ,"
         + " :inrate_final_cond ,"
         + " :inrate_final_val ,"
         + " :mm_depos_cond ,"
         + " :mm_depos_b ,"
         + " :mm_depos_e ,"
         + " :avg6_depos_cond ,"
         + " :avg6_depos_b ,"
         + " :avg6_depos_e ,"
         + " :cr_ovdue30_bal_cond ,"
         + " :cr_ovdue30_bal_b ,"
         + " :cr_ovdue30_bal_e ,"
         + " :avg3_crlimit_rate_cond ,"
         + " :avg3_crlimit_rate_b ,"
         + " :avg3_crlimit_rate_e ,"
         + " :avg6_crlimit_rate_cond ,"
         + " :avg6_crlimit_rate_b ,"
         + " :avg6_crlimit_rate_e ,"
         + " :conti_no_consum_cond ,"
         + " :conti_no_consum_mm ,"
         + " :corp_curr_0_cond ,"
         + " :corp_curr_0_code ,"
         + " :corp_abnor_cond ,"
         + " :corp_abnor_yn ,"
         + " :corp12_ovdue_cond ,"
         + " :corp12_ovdue_yn ,"
         + " :corp_cr_abnor_cond ,"
         + " :corp_cr_abnor_val ,"
         + " :corp_add_note_cond ,"
         + " :corp_add_note_val ,"
         + " :corp_rept_case_cond ,"
         + " :corp_rept_case_yn ,"
         + " :idno6_late_pay_cond ,"
         + " :idno6_late_pay_cnt_b ,"
         + " :idno6_late_pay_cnt_e ,"
         + " :idno_cr_abnor_cond ,"
         + " :idno_cr_abnor_val ,"
         + " :idno12_ovdue_cond ,"
         + " :idno12_ovdue_yn ,"
         + " :idno_unsecu_bal_cond ,"
         + " :idno_unsecu_bal_b ,"
         + " :idno_unsecu_bal_e ,"
         + " :corp_imp_cond ,"
         + " :corp_jcic_send ,"
         + " :idno_jcic_send ,"
         + " :issue_card_cond ,"
         + " :issue_card_mm ,"
         + " :block_reason_cond ,"
         + " :crt_user ,"
         + " to_char(sysdate,'yyyymmdd') ,"
         + " 'N' ,"
         + " '' ,"
         + " '' ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " 1 "
         + " )"
   ;
   col2ParmStr("risk_group");
   col2ParmStr("risk_group_desc");
   col2ParmStr("acct_pay_type");
   col2ParmStr("branch_exchg_type");
   col2ParmStr("capital_cond");
   col2ParmNum("capital_amt_b");
   col2ParmNum("capital_amt_e");
   col2ParmStr("mcode_01_cond");
   col2ParmStr("mcode_01_yn");
   col2ParmStr("inrate_date_cond");
   col2ParmStr("inrate_date_mm");
   col2ParmStr("inrate_final_cond");
   col2ParmStr("inrate_final_val");
   col2ParmStr("mm_depos_cond");
   col2ParmNum("mm_depos_b");
   col2ParmNum("mm_depos_e");
   col2ParmStr("avg6_depos_cond");
   col2ParmNum("avg6_depos_b");
   col2ParmNum("avg6_depos_e");
   col2ParmStr("cr_ovdue30_bal_cond");
   col2ParmNum("cr_ovdue30_bal_b");
   col2ParmNum("cr_ovdue30_bal_e");
   col2ParmStr("avg3_crlimit_rate_cond");
   col2ParmNum("avg3_crlimit_rate_b");
   col2ParmNum("avg3_crlimit_rate_e");
   col2ParmStr("avg6_crlimit_rate_cond");
   col2ParmNum("avg6_crlimit_rate_b");
   col2ParmNum("avg6_crlimit_rate_e");
   col2ParmStr("conti_no_consum_cond");
   col2ParmNum("conti_no_consum_mm");
   col2ParmStr("corp_curr_0_cond");
   col2ParmStr("corp_curr_0_code");
   col2ParmStr("corp_abnor_cond");
   col2ParmStr("corp_abnor_yn");
   col2ParmStr("corp12_ovdue_cond");
   col2ParmStr("corp12_ovdue_yn");
   col2ParmStr("corp_cr_abnor_cond");
   col2ParmStr("corp_cr_abnor_val");
   col2ParmStr("corp_add_note_cond");
   col2ParmStr("corp_add_note_val");
   col2ParmStr("corp_rept_case_cond");
   col2ParmStr("corp_rept_case_yn");
   col2ParmStr("idno6_late_pay_cond");
   col2ParmNum("idno6_late_pay_cnt_b");
   col2ParmNum("idno6_late_pay_cnt_e");
   col2ParmStr("idno_cr_abnor_cond");
   col2ParmStr("idno_cr_abnor_val");
   col2ParmStr("idno12_ovdue_cond");
   col2ParmStr("idno12_ovdue_yn");
   col2ParmStr("idno_unsecu_bal_cond");
   col2ParmNum("idno_unsecu_bal_b");
   col2ParmNum("idno_unsecu_bal_e");
   col2ParmStr("corp_imp_cond");
   col2ParmStr("corp_jcic_send");
   col2ParmStr("idno_jcic_send");
   col2ParmStr("issue_card_cond");
   col2ParmStr("issue_card_mm");
   col2ParmStr("block_reason_cond");
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("異動處理失敗");
      return rc;
   }

   String sql2 = " select "
         + " * "
         + " from rsk_trcorp_parmdtl "
         + " where 1=1 "
         + " and risk_group = ? "
         + " and apr_flag = ? ";

   sqlSelect(sql2, new Object[]{wp.itemStr("risk_group"), wp.itemStr("apr_flag")});
   if (sqlRowNum < 0) {
      errmsg("異動處理失敗");
      return rc;
   }
   else rc = 1;

   llRowCnt = sqlRowNum;

   if (llRowCnt == 0) return rc;

   strSql = " delete rsk_trcorp_parmdtl where risk_group = :risk_group and apr_flag ='N' ";
   item2ParmStr("risk_group");
   sqlExec(strSql);

   if (sqlRowNum < 0) {
      errmsg("異動處理失敗");
      return rc;
   }
   else rc = 1;

   strSql = " insert into rsk_trcorp_parmdtl ("
         + " apr_flag ,"
         + " data_code ,"
         + " data_code2 ,"
         + " data_type ,"
         + " mod_pgm ,"
         + " mod_time ,"
         + " mod_user ,"
         + " risk_group ,"
         + " type_desc "
         + " ) values ( "
         + " 'N' ,"
         + " :data_code ,"
         + " :data_code2 ,"
         + " :data_type ,"
         + " :mod_pgm ,"
         + " sysdate ,"
         + " :mod_user ,"
         + " :risk_group ,"
         + " :type_desc "
         + " ) "
   ;

   for (int ii = 0; ii < llRowCnt; ii++) {
      setString("data_code", colStr(ii, "data_code"));
      setString("data_code2", colStr(ii, "data_code2"));
      setString("data_type", colStr(ii, "data_type"));
      setString("mod_pgm", wp.modPgm());
      setString("mod_user", wp.loginUser);
      setString("risk_group", colStr(ii, "risk_group"));
      setString("type_desc", colStr(ii, "type_desc"));

      sqlExec(strSql);

      if (sqlRowNum <= 0) {
         errmsg("異動處理失敗");
         break;
      }
   }

   return rc;
}

public int dataApr() throws Exception  {
   msgOK();
   int llRowCnt = 0;
   strSql = " delete rsk_trcorp_parm where risk_group =:risk_group and apr_flag ='Y' ";
   var2ParmStr("risk_group");

   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("覆核失敗");
      return rc;
   }

   strSql = " update rsk_trcorp_parm set "
         + " apr_flag ='Y' , "
         + " apr_date = to_char(sysdate,'yyyymmdd') , "
         + " apr_user =:apr_user "
         + " where risk_group = :risk_group "
         + " and apr_flag = 'N' "
   ;

   item2ParmStr("apr_user", "zz_apr_user");
   var2ParmStr("risk_group");

   sqlExec(strSql);

   if (sqlRowNum <= 0) return rc;

   String sql1 = " select "
         + " * "
         + " from rsk_trcorp_parmdtl "
         + " where 1=1 "
         + " and risk_group = ? "
         + " and apr_flag = 'N' ";

   sqlSelect(sql1, new Object[]{varsStr("risk_group")});

   llRowCnt = sqlRowNum;

   if (sqlRowNum < 0) {
      errmsg("覆核失敗");
      return rc;
   }

   if (sqlRowNum == 0) {
      rc = 1;
      return rc;
   }
   strSql = " delete rsk_trcorp_parmdtl where risk_group = :risk_group and apr_flag = 'Y' ";
   var2ParmStr("risk_group");
   sqlExec(strSql);

   if (sqlRowNum < 0) {
      errmsg("覆核失敗");
      return rc;
   }
   else rc = 1;

   strSql = " update rsk_trcorp_parmdtl set "
         + " apr_flag ='Y' "
         + " where risk_group = :risk_group "
         + " and apr_flag = 'N' "
   ;
   var2ParmStr("risk_group");

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("覆核失敗");
   }
   return rc;
}

public int deleteDetl() throws Exception  {
   msgOK();

   strSql = " delete rsk_trcorp_parmdtl where risk_group =:risk_group "
         + " and data_type =:data_type and data_code =:data_code "
         + " and apr_flag <> 'Y' "
   ;

   var2ParmStr("risk_group");
   var2ParmStr("data_type");
   var2ParmStr("data_code");

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
   }

   return rc;
}

}
