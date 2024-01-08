package rskm01;
/*再提示整批登錄維護 V.2018-0403
 *
 * */

public class Rskm0240Func extends busi.FuncAction {
double imRepDcAmt = 0, imRepTwAmt = 0;
busi.CommCurr zzCurr = new busi.CommCurr();

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

void selectRskChgback(String aRowid) {
   strSql = "select ctrl_seqno, bin_type, debit_flag, reference_no,"
         + " chg_stage, sub_stage,fst_disb_apr_date,fst_disb_yn,"
         + " final_close, rep_apr_date,"
         + " curr_code, dest_amt, dc_dest_amt,"
         + " mod_seqno"
         + " from rsk_chgback"
         + " where 1=1"
         + " and rowid = ? ";
   daoTid = "A.";
   setRowId(1,aRowid);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      this.sqlErr("rsk_chgback.select");
      return;
   }
   if (colNum("A.mod_seqno") != wp.itemNum("mod_seqno")) {
      errmsg(errOtherModify);
   }
   return;
}

@Override
public void dataCheck() {
   selectRskChgback(wp.itemStr("rowid"));
   if (rc != 1)
      return;

   if (colPos(",C,S", "A.final_close") > 0) {
      errmsg("扣款已結案, 不可維護");
      return;
   }

   if (colEmpty("A.rep_apr_date") == false) {
      errmsg("再提示已覆核, 不可修改");
      return;
   }
   if (colPos(",1,2", "A.chg_stage") <= 0) {
      errmsg("扣款階段不是 [一扣,再提示] 不可修改");
      return;
   }
   if (colEmpty("A.fst_disb_apr_date")) {
      errmsg("一扣未撥款, 不可登錄再提示");
      return;
   }

   imRepDcAmt = zzCurr.resetAmt(wp.itemNum("rep_dc_amt"), colStr("A.curr_code"));
   imRepTwAmt = zzCurr.dc2twAmt(colStr("A.curr_code"),colNum("A.dest_amt"), colNum("A.dc_dest_amt"), imRepDcAmt);

   if (imRepDcAmt < 0) {
      errmsg("再提示金額 需大於 0");
      return;
   }

   String lsRepDate = wp.itemStr("repsent_date");
   if (imRepDcAmt == 0 && !empty(lsRepDate)) {
      errmsg("再提示金額=0, 再提示日期 不可輸入?!");
      return;
   }
   if (imRepDcAmt > 0 && empty(lsRepDate)) {
      errmsg("再提示金額>0, 再提示日期 不可空白?!");
      return;
   }
   if (!empty(lsRepDate) && wp.itemEmpty("rep_ac_no")) {
      errmsg("[沖銷借方科目] 不可空白?!");
      return;
   }

   //--
   String ss = colStr("A.chg_stage") + colStr("A.sub_stage") + colStr("A.fst_disb_yn");
   if (!eqIgno(ss, "130Y") && !eqIgno(ss, "210Y")) {
      errmsg("扣款狀態: 不是[1-30-Y], [2-10]; 不可存檔");
      return;
   }
}

@Override
public int dataProc() {
   return 0;
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

   busi.SqlPrepare chgb = new busi.SqlPrepare();
   chgb.sql2Update("rsk_chgback");
   if (imRepDcAmt > 0) {
      chgb.ppstr("chg_stage", "2");
      chgb.ppstr("sub_stage", "10");
      chgb.ppstr("rep_status", "10");
      chgb.ppnum("rep_dc_amt", imRepDcAmt);
      chgb.ppnum("rep_amt_twd", imRepTwAmt);
      chgb.ppstr("repsent_date", wp.itemStr("repsent_date"));
      chgb.ppstr("rep_glmemo3", wp.itemStr("rep_glmemo3"));
      chgb.ppstr("rep_ac_no", wp.itemStr("rep_ac_no"));
      chgb.ppymd("rep_add_date");
      chgb.ppstr("rep_add_user", modUser);
      chgb.modxxx(modUser, modPgm);
      chgb.rowid2Where(wp.itemStr("rowid"));
   }
   else {
      chgb.ppstr("chg_stage", "1");
      chgb.ppstr("sub_stage", "30");
      chgb.ppstr("rep_status", "");
      chgb.ppnum("rep_dc_amt", 0);
      chgb.ppnum("rep_amt_twd", 0);
      chgb.ppstr("repsent_date", "");
      chgb.ppstr("rep_glmemo3", "");
      chgb.ppstr("rep_ac_no", "");
      chgb.ppstr("rep_add_date", "");
      chgb.ppstr("rep_add_user", "");
      chgb.modxxx(modUser, modPgm);
      chgb.rowid2Where(wp.itemStr("rowid"));
   }

   sqlExec(chgb.sqlStmt(), chgb.sqlParm());
   if (sqlRowNum <= 0) {
      sqlErr("rsk_chgback.Update");
      return -1;
   }

   return rc;
}


@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}


}
