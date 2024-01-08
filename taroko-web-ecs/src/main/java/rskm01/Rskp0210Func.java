package rskm01;
/* 扣款新增登錄覆核-主管作業
 *
 * */

import busi.FuncProc;

public class Rskp0210Func extends FuncProc {

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
public void dataCheck() {
   strSql = "select chg_stage, mod_seqno,"
         + " chg_times, ctrl_seqno, bin_type, reference_no,"
         + " '' as xxx"
         + " from rsk_chgback"
         + " where rowid =?"
         + " and mod_seqno =?";
   this.setRowId(1, varsStr("kk-rowid"));
   setString(2, varsStr("kk-mod_seqno"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg(errOtherModify);
      return;
   }

}

@Override
public int dataProc() {
   msgOK();
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   RskChgback oochgb = new RskChgback();
   oochgb.setConn(wp);
   oochgb.isReferNo = colStr("reference_no");
   oochgb.isCtrlSeqno = colStr("ctrl_seqno");
   if (oochgb.rskp0210Update(varsStr("kk-rowid")) != 1) {
      errmsg(oochgb.getMsg());
      return rc;
   }

   return rc;
}

}
