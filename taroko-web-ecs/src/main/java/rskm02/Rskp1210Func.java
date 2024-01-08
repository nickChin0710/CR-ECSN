package rskm02;
/**
 * 商務卡第一次分群處理
 * 2019-0620:  JH    p_xxx >>acno_pxxx
 */

import busi.FuncAction;
import taroko.base.Parm2Sql;

public class Rskp1210Func extends FuncAction {

int il_ok = 0, il_err = 0;
String kk = "";
String is_risk_group = "";
taroko.base.CommDate zzdate = new taroko.base.CommDate();
busi.DataSet dsBank = null;
busi.DataSet ds_parm = null;
busi.SqlPrepare spList = new busi.SqlPrepare();

void selectRsk_trcorp_mast(String a_batch_no) {
   if (empty(a_batch_no)) {
      errmsg("[覆審批號] 不可空白");
      return;
   }
   strSql = "select group_date1, group_proc_date2, data_yymm "
         + " from rsk_trcorp_mast"
         + " where batch_no =?";
   setParm(1, a_batch_no);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select rsk_trcorp_mast error, kk[%s]", a_batch_no);
      return;
   }

}

@Override
public void dataCheck()  {
   selectRsk_trcorp_mast(kk);
   if (rc != 1)
      return;

   if (empty(colStr("group_date1"))) {
      errmsg("未設定 第一次分群參數設定");
      return;
   }

   if (empty(colStr("group_proc_date2")) == false) {
      errmsg("已執行 第二次分群處理, 不可再執行");
      return;
   }
   if (checkGroup(kk) == false) {
      errmsg("查無 分群設定參數, 批號=" + kk);
      return;
   }

   if (checkDataYymm(colStr("data_yymm")) == false) {
      errmsg("查無 分行往來統計資料, 資料年月=" + colStr("data_yymm"));
      return;
   }
}

boolean checkGroup(String batch_no)  {
   String sql1 = "select count(*) as db_cnt "
         + " from rsk_trcorp_parm A, rsk_trcorp_mast_group B"
         + " where A.risk_group = B.risk_group "
         + " and batch_no =?"
         + " and B.group_type ='1'";
   sqlSelect(sql1, new Object[]{
         batch_no
   });
   return sqlRowNum > 0 && colNum("db_cnt") > 0;
}

boolean checkDataYymm(String data_yymm)  {
   String sql1 = "select count(*) as db_cnt "
         + " from RSK_TRCORP_BANK_STAT"
         + " where data_yymm=? ";
   sqlSelect(sql1, new Object[]{
         data_yymm
   });
   return sqlRowNum > 0 && colNum("db_cnt") > 0;
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
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc()  {
   kk = varsStr("batch_no");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   //-call-batch--

   //	delete_trcorp(kk);
//	if (rc != 1) {
//		return rc;
//	}

//	select_rsk_trcorp_parm(kk);
//	if (rc!=1)
//		return rc;
//
//	select_rsk_trcorp_bank_stat(colStr("data_yymm"));
//	if (rc != 1) {
//		return rc;
//	}
//
//	updata_mast();
   return rc;
}

void selectRsk_trcorp_parm(String a_batch_no)  {
   ds_parm = new busi.DataSet();
   strSql = "select A.*"
         + " from rsk_trcorp_parm A join rsk_trcorp_mast_group B"
         + " on A.risk_group =B.risk_group"
         + " where B.batch_no =?"
         + " and B.group_type ='1'"
         + " order by A.risk_group"
   ;
   ds_parm.colList = sqlQuery(strSql, new Object[]{
         a_batch_no
   });
   if (sqlRowNum <= 0) {
      errmsg("未定義[第一次分群參數], kk[%s]", a_batch_no);
   }
   return;
}

void selectRsk_trcorp_bank_stat(String a_yymm)  {
   dsBank = new busi.DataSet();

   strSql = "select A.*,"
         + " B.corp_no,"
         + " B.charge_id, B.charge_name, B.capital"
         + " from crd_corp B join rsk_trcorp_bank_stat A on B.corp_p_seqno =A.corp_p_seqno"
         + " where A.data_yymm =?";
   dsBank.colList = this.sqlQuery(strSql, new Object[]{
         a_yymm
   });
//	wp.ddd("dsBank=" + dsBank.list_rows());

   if (sqlRowNum <= 0) {
//		ddd("data_yymm : " + varsStr("data_yymm"));
      errmsg("查無 分行往來統計資料, 資料年月=, kk[%s]", a_yymm);
      return;
   }

   for (int ii = 0; ii < dsBank.listRows(); ii++) {
      dsBank.listToCol(ii);
      String ls_corp_p_seqno = dsBank.colStr("corp_p_seqno");
      //-流通卡-
      if (checkCurrent_code(ls_corp_p_seqno) == false)
         continue;

      //--
      if (setRisk_group() != 1)
         continue;

      insertTrcorp_list();
   }
}

int setRisk_group()  {
   is_risk_group = "";
   String ls_corp_no = dsBank.colStr("corp_no");
   String ls_corp_pseqno = dsBank.colStr("corp_p_seqno");

   //-帳戶總繳/個繳數-
   int li_cnt1 = 0, li_cnt2 = 0;
   strSql = "select sum(decode(A.acno_flag,'Y',1,0)) as xx_cnt1"
         + " , sum(decode(A.acno_flag,'Y',0,1)) as xx_cnt2"
         + " from act_acno A join crd_card B on A.acno_p_seqno =B.acno_p_seqno"
         + " where B.current_code ='0'"
         + " and B.acct_type ='02'"
         + " and B.corp_p_seqno =?";
   setParm(1, ls_corp_pseqno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      li_cnt1 = 0;
      li_cnt2 = 0;
   }
   else {
      li_cnt1 = colInt("xx_cnt1");
      li_cnt2 = colInt("xx_cnt2");
   }

   String ss = "";
   int ll_cnt = 0;
   double lm1 = 0, lm2 = 0, lm_amt = 0;
   for (int kk = 0; kk < ds_parm.listRows(); kk++) {
      ds_parm.listToCol(kk);

      //帳戶屬性-
      ss = ds_parm.colStr("acct_pay_type");
      if (eqIgno(ss, "1")) {
         if (li_cnt1 == 0) //-總繳-
            continue;
      }
      else if (eqIgno(ss, "2") && li_cnt2 == 0) {
         continue;
      }
      else if (li_cnt1 == 0 && li_cnt2 == 0)
         continue;

      //分行往來類別-
      ss = ds_parm.colStr("branch_exchg_type");
      if (eqIgno(ss, "0") == false) {
         if (!eqIgno(dsBank.colStr("branch_exchg_type"), ss))
            continue;
      }
      //-資本額條件-
      if (eqIgno(ds_parm.colStr("capital_cond"), "Y")) {
         lm1 = ds_parm.colNum("capital_amt_b");
         lm2 = ds_parm.colNum("capital_amt_e");
         lm_amt = dsBank.colNum("capital");
         if (lm_amt < lm1 || lm_amt > lm2)
            continue;
      }
      //-最近一月M1_COND-
      if (eqIgno(ds_parm.colStr("mcode_01_cond"), "Y")) {
         strSql = "select count(*) as xx_cnt"
               + " from act_acno"
               + " where corp_p_seqno =?"
               + " and acno_flag in ('2','3')"
               + " and payment_rate1 ='01'";
         setParm(1, ls_corp_pseqno);
         sqlSelect(strSql);
         if (sqlRowNum < 0) {
            errmsg("select act_acno error, kk[%s]", ls_corp_pseqno);
            return rc;
         }
         if (eqIgno(ds_parm.colStr("mcode_01_yn"), "N") && colInt("xx_cnt") > 0)
            continue;
         if (eqIgno(ds_parm.colStr("mcode_01_yn"), "Y") && colInt("xx_cnt") == 0)
            continue;
      }
      //最近內部評等日期,<=N月內-
      if (eqIgno(ds_parm.colStr("inrate_date_cond"), "Y")) {
         ss = dsBank.colStr("inrate_date");
         if (empty(ss) || eqAny(ss, "00010101"))
            continue;
         int li_mm = zzdate.monthsBetween(zzdate.sysDate(), ss);
         if (li_mm > ds_parm.colInt("inrate_date_mm"))
            continue;
      }
      //最近內部最終評等-
      if (eq(ds_parm.colStr("inrate_final_cond"), "Y")) {
         String ls_final = dsBank.colStr("INRATE_FINAL_CODE");
         if (empty(ls_final))
            continue;
         String[] cde = new String[2];
         cde[0] = ds_parm.colStr("inrate_final_val");
         cde[1] = ",";
         ll_cnt = 0;
         while (!empty(cde[0])) {
            ss = commString.token(cde);
            if (eq(ss, ls_final))
               ll_cnt++;
         }
         if (ll_cnt == 0)
            continue;
      }
      //月底存款餘額-
      if (eq(ds_parm.colStr("mm_depos_cond"), "Y")) {
         if (dsBank.colNum("TOT_DEPOS_BAL") < ds_parm.colNum("mm_depos_b") ||
               dsBank.colNum("TOT_DEPOS_BAL") > ds_parm.colNum("mm_depos_e"))
            continue;
      }
      //近6月平均存款-
      if (eq(ds_parm.colStr("avg6_depos_cond"), "Y")) {
         lm1 = ds_parm.colNum("avg5_depos_b");
         lm2 = ds_parm.colNum("avg5_depos_e");
         lm_amt = dsBank.colNum("avg_depos_bal01_06");
         if (lm_amt < lm1 || lm_amt > lm2)
            continue;
      }
      //授信逾期30日總餘額-
      if (eq(ds_parm.colStr("cr_ovdue30_bal_cond"), "Y")) {
         lm1 = ds_parm.colNum("cr_ovdue30_bal_b");
         lm2 = ds_parm.colNum("cr_ovdue30_bal_e");
         lm_amt = dsBank.colNum("cr_ovdue_bal30");
         if (lm_amt < lm1 || lm_amt > lm2)
            continue;
      }
      //10.平均授信額度3月成長率-
      if (eq(ds_parm.colStr("avg3_crlimit_rate_cond"), "Y")) {
         lm1 = ds_parm.colNum("avg3_crlimit_rate_b");
         lm2 = ds_parm.colNum("avg3_crlimit_rate_e");
         lm_amt = dsBank.colNum("avg_cr_limit01_03");
         if (lm_amt < lm1 || lm_amt > lm2)
            continue;
      }
      //11.平均授信額度6月成長率_COND	      	varchar2(1)	1		n	false	false
      if (eq(ds_parm.colStr("avg6_crlimit_rate_cond"), "Y")) {
         lm1 = ds_parm.colNum("avg6_crlimit_rate_b");
         lm2 = ds_parm.colNum("avg6_crlimit_rate_e");
         lm_amt = dsBank.colNum("avg_cr_limit01_06");
         if (lm_amt < lm1 || lm_amt > lm2)
            continue;
      }
      //12.連續N月未消費無欠款-
      if (eq(ds_parm.colStr("conti_no_consum_cond"), "Y")) {
         if (acnoPayrate_0E(ls_corp_pseqno, ds_parm.colInt("conti_no_consum_mm")) > 0)
            continue;
      }
      //13.公司負責人流通卡-
      if (eq(ds_parm.colStr("corp_curr_0_cond"), "Y")) {
         if (chargeId_card() == false)
            continue;
      }

      //-----------------------------------------------------------------------------
      //-第一次分群無jcic資料, so ship-
      //-----------------------------------------------------------------------------
      //-JCIC公司資料-===============================================================
      //14.公司設立異常狀況-
      if (eq(ds_parm.colStr("corp_abnor_cond"), "Y")) continue;
      //15.公司12月授信逾期
      if (eq(ds_parm.colStr("corp12_ovdue_cond"), "Y")) continue;
      //16.公司信用異常-
      if (eq(ds_parm.colStr("corp_cr_abnor_cond"), "Y")) continue;
      //17.公司是否有補充註記-
      if (eq(ds_parm.colStr("corp_add_note_cond"), "Y")) continue;
      //18.公司是否有通報案件-
      if (eq(ds_parm.colStr("corp_rept_case_cond"), "Y")) continue;
      //=JCIC負責人==================================================================
      //19.負責人信用卡6月遲繳次數-
      if (eq(ds_parm.colStr("idno6_late_pay_cond"), "Y")) continue;
      //20.負責人信用異常-
      if (eq(ds_parm.colStr("idno_cr_abnor_cond"), "Y")) continue;
      //21.負責人12月授信逾期-
      if (eq(ds_parm.colStr("idno12_ovdue_cond"), "Y")) continue;
      //22.負責人無擔保負債總餘-
      if (eq(ds_parm.colStr("idno_unsecu_bal_cond"), "Y")) continue;

      is_risk_group = ds_parm.colStr("risk_group");
      return 1;
   }
   return 0;
}

int acnoPayrate_0E(String a_corp_pseqno, int lm1)  {
   String ls_pay_rate = commString.left(commString.repeat("0E", lm1), 48);
   //12.連續N月未消費無欠款-
   strSql = "select count(*) as xx_cnt"
         + " from act_acno"
         + " where corp_p_seqno =?"
         + " and acno_flag in ('2','3')"
         + " and (payment_rate1||payment_rate2||payment_rate3"
         + "||payment_rate4||payment_rate5||payment_rate6"
         + "||payment_rate7||payment_rate8||payment_rate9"
         + "||payment_rate10||payment_rate11||payment_rate12"
         + "||payment_rate13||payment_rate14||payment_rate15"
         + "||payment_rate16||payment_rate17||payment_rate18"
         + "||payment_rate19||payment_rate20||payment_rate21"
         + "||payment_rate22||payment_rate23||payment_rate24"
         + ") not like ?";
   setParm(1, a_corp_pseqno);
   setParm(2, ls_pay_rate + "%");
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return 0;

   return colInt("xx_cnt");
}

boolean chargeId_card()  {
   //-13.公司負責人-流通卡-
   String ls_charge_id = dsBank.colStr("charge_id");
   if (empty(ls_charge_id))
      return false;
   String ls_code = ds_parm.colStr("CORP_CURR_0_CODE");
   strSql = "select sum(decode(A.acno_flag,'1',1,0)) as xx_cnt1,"
         + " sum(decode(A.acno_flag,'1',0,1)) as xx_cnt2"
         + " from crd_card A join crd_idno B on A.id_p_seqno =B.id_p_seqno"
         + " where B.id_no =?"
         + " and A.current_code ='0' and A.sup_flag='0'";
   setParm(1, ls_charge_id);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return false;

   //-僅有流通商務卡[02]-
   if (eq(ls_code, "1")) {
      return colInt("xx_cnt1") <= 0 && colInt("xx_cnt2") != 0;
   }
   else if (eq(ls_code, "2")) {
      //-只要01,05,06
      return colInt("xx_cnt1") != 0;
   }
   else if (eq(ls_code, "2")) {
      //-無任何流通正卡-
      return colInt("xx_cnt1") <= 0 && colInt("xx_cnt2") <= 0;
   }
   return true;
}

int selectCrd_corp(String a_corp_pseqno)  {
   strSql = "select corp_p_seqno, corp_no"
         + " charge_id, charge_name, capital"
         + " FROM crd_corp "
         + " where corp_p_seqno =?";
   setParm(1, a_corp_pseqno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return 0;

   return 1;
}

public int deleteTrcorp(String a_batch_no)  {
   strSql = "delete RSK_TRCORP_LIST "
         + " where batch_no =? ";

   setParm(1, a_batch_no);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete rsk_trcorp_list error; " + this.sqlErrtext);
   }
   return rc;
}

public int updataMast()  {
   strSql = "update rsk_trcorp_mast set "
         + " group_proc_date1 = " + commSqlStr.sysYYmd + ","
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where batch_no =? ";

   setParm(1, kk);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_trcorp_mast error, kk[%s]", kk);
   }
   return rc;
}


boolean checkCurrent_code(String a_corp_pseqno)  {
   if (empty(a_corp_pseqno)) {
      return false;
   }

   String sql1 = "select count(*) as corp_cnt "
         + " from crd_card"
         + " where corp_p_seqno =?"
         + " and current_code ='0' "
         + " and acct_type ='02'";
   sqlSelect(sql1, new Object[]{
         a_corp_pseqno
   });

   return sqlRowNum > 0 && colNum("corp_cnt") > 0;
}

public int insertTrcorp_list()  {
   //-insert RSK_TRCORP_LIST-
   taroko.base.Parm2Sql spp=new Parm2Sql();
   spp.insert("rsk_trcorp_list");
   spp.parmSet("batch_no", kk);
   spp.parmSet("corp_no", dsBank.colStr("corp_no"));
   spp.parmSet("corp_p_seqno", dsBank.colStr("corp_p_seqno"));
   spp.parmSet("charge_idno", dsBank.colStr("charge_id"));
   spp.parmSet("charge_name", dsBank.colStr("charge_name"));
   spp.parmYmd("group_proc_date1");
   spp.parmSet("risk_group1", is_risk_group);
   spp.parmSet("crt_user", modUser);
   spp.parmSet("crt_date", wp.sysDate);
   spp.parmSet("apr_date", wp.sysDate);
   spp.parmSet("apr_user", modUser);
   spp.modxxxSet(modUser, modPgm);

   sqlExec(spp.getSql(), spp.getParms());
   if (sqlRowNum != 1) {
      errmsg("insert RSK_TRCORP_LIST error, kk[%s]", dsBank.colStr("corp_no"));
      il_err++;
   }
   return rc;
}

}
