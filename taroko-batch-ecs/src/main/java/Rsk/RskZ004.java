package Rsk;
/**
 * 2023-1201.00   JH    initial
 * */

public class RskZ004 extends com.BaseBatch {
private final String PROGNAME = "預借現金額度預設10%  2023-1201.00";
/*
HH hh=new HH();
//-----------
class HH {
   void initData() {
   }
}
*/
//--
//=*****************************************************************************
public static void main(String[] args) {
   RskZ004 proc = new RskZ004();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : RskZ004 [commit_row]");
      okExit(0);
   }

   int ll_pRow=0;
   if (args.length >= 1) {
      ll_pRow =ss2int(args[0]);
   }
   if (ll_pRow <=0) ll_pRow=100;

   dbConnect();

   selectAct_acno();

//   printf("每次處理(commit)筆數[%s]", ll_pRow);

//   int llNrow=ll_pRow;
//   while (llNrow >=ll_pRow) {
//      llNrow =updateAct_acno();
//      sqlCommit();
//
//      totalCnt +=llNrow;
//      dspProcRow(10000);
//   }

   sqlCommit();
   endProgram();
}
//=======================
void selectAct_acno() throws Exception {
   sqlCmd =" select acno_p_seqno"
//(SELECT id_no FROM crd_idno WHERE id_p_seqno=act_acno.id_p_seqno) id_no\n"+
       +", line_of_credit_amt, line_of_credit_amt_cash "
       +" from act_acno "
       +" where 1=1"
       +" AND acct_type ='01'"
       +" AND line_of_credit_amt_cash =0 and line_of_credit_amt >0 "
       +" AND EXISTS (SELECT 1 FROM crd_card WHERE current_code='0' AND p_seqno=act_acno.p_seqno)"
       ;

   openCursor();
   while (fetchTable()) {
      String ls_pseqno =colSs("acno_p_seqno");
//      double lm_creditAmt =colNum("line_of_credit_amt");
      updateAct_acno(ls_pseqno);
      totalCnt++;
      processDisplay(10000);
      if ((totalCnt % 100)==0) {
         sqlCommit();
      }
   }
   closeCursor();
}
//---
int tiUacno=-1;
int updateAct_acno(String ls_Pseqno) throws Exception {
   if (tiUacno <=0) {
      sqlCmd ="UPDATE act_acno SET "+
          " line_of_credit_amt_cash =line_of_credit_amt * 0.1 "+
          ", mod_pgm ='RskZ004' "+
          " WHERE 1=1 "+
          " and acno_p_seqno =?"
          ;
      tiUacno =ppStmtCrt("tiUacno","");
   }
   ppp(1, ls_Pseqno);
   sqlExec(tiUacno);

   return sqlNrow;
}

}
