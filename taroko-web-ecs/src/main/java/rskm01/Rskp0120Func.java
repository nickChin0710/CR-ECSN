package rskm01;
/** 調單作業結案主管覆核
 * 2020-1222         JH    bil_sysexp
 * 2020-1204.4917    JH    mcht_no=bill.mcht_no
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0329:	JH		modify
 */

import taroko.base.Parm2Sql;


public class Rskp0120Func extends busi.FuncProc {

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
   strSql = "select ctrl_seqno, bin_type, reference_no, rept_status"
         + ", fees_flag, fees_amt, mcht_no"
         + ", card_no, acct_type, p_seqno"
         + " from rsk_receipt"
         + " where 1=1"
         + " and rowid =?"
         + " and mod_seqno =?";

   this.setRowId(1, varsStr("rowid"));
   this.setInt(2, strToInt(varsStr("mod_seqno")));

   daoTid = "A.";
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg(errOtherModify);
      return;
   }
}

@Override
public int dataProc() {
   dataCheck();
   if (rc != 1)
      return rc;

   RskReceipt oorept = new RskReceipt();
   oorept.setConn(wp);
   oorept.isRowid = varsStr("rowid");
   oorept.isCtrlSeqno = colStr("A.ctrl_seqno");
   oorept.isReferNo = colStr("A.reference_no");

   rc = oorept.rskp0120Update();
   if (rc != 1) {
      errmsg(oorept.getMsg());
      return rc;
   }

   if (rc == 1 &&
         colEq("A.fees_flag", "Y") && colNum("A.fees_amt") > 0) {
      Parm2Sql tt = new Parm2Sql();
      tt.insert("bil_sysexp");
      tt.parmSet("card_no", colStr("A.card_no"));
      tt.parmSet("acct_type", colStr("A.acct_type"));
      tt.parmSet("p_seqno", colStr("A.p_seqno"));
      tt.parmSet("bill_type", "OKOL");   //-bill-
      tt.parmSet("bill_desc", "調單費用");
      tt.parmSet("txn_code", "RR");
      tt.parmYmd("purchase_date");
      tt.parmSet("mcht_no", colStr("A.mcht_no"));
      tt.parmSet("dest_amt", colNum("A.fees_amt"));
      tt.parmSet("dest_curr", "901");
      tt.parmSet("dc_dest_amt", colNum("A.fees_amt"));
      tt.parmSet("curr_code", "901");
      tt.parmSet("src_amt", colNum("A.fees_amt"));
      tt.parmSet("post_flag", "N");
      tt.modxxxSet(modUser,modPgm);

      sqlExec(tt.getSql(), tt.getParms());
      if (sqlRowNum <=0) {
         errmsg("insert bil_sysexp error");
      }
   }

   return rc;
}


}
