/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-06-17  V1.00.00  Alex        initial                                  *
 *  2023-1016 V1.00.01   JH          modify                                   *
 *  2023-1101 V1.00.02   Wilson      add insert crd_jcic                      *
 *  2023-1130 V1.00.03   JH          Cycle next busi-date
 *****************************************************************************/
package Rsk;

import Cca.CcaOutGoing;
import com.BaseBatch;
import com.CommCrdRoutine;

public class RskP183 extends BaseBatch {
private final String progname = "M2自動停用卡片 2023-1130 V1.00.03";
CcaOutGoing ccaOutGoing = null;
CommCrdRoutine comcr = null;

final String debitFlag = "N";   //--信用卡
final String currentCode = "4";   //--其他停用
final String oppoReason = "E4";   //--兩期以上未繳 (暫定)

String prgmId = "RskP183";
String pgmName = "RskP183";
String acnoPSeqno = "";
int cardAcctIdx = 0;
String cardNo = "";
String rcUseIndicator = "";

public static void main(String[] args) {
   RskP183 proc = new RskP183();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP183 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   dbConnect();
   //-Cycle下一營業日---
   checkRunDate(hBusiDate);

   ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   //Cycle後一天, 停卡處理--
   //selectPtrWorkDay();

   procData();

   sqlCommit();
   endProgram();
}
//--------
void checkRunDate(String a_chkDate) throws Exception {
   String ls_cycleDate="";
   sqlCmd ="select max(this_close_date) this_close_date"
       +" from ptr_workday";
   sqlSelect("");
   if (sqlNrow >0) {
      ls_cycleDate =colSs("this_close_date");
   }
   printf(" 最近關帳日[%s]",ls_cycleDate);
   if (empty(ls_cycleDate)) {
      printf("select ptr_workDay N-file");
      okExit(0);
      return;
   }

   String ls_rDate="";
   for (int ii =1; ii <31 ; ii++) {
      ls_rDate =commDate.dateAdd(ls_cycleDate,0,0,ii);
      if (checkWorkDate(ls_rDate)) continue;
      break;
   }
   if (eq(ls_rDate,a_chkDate)) return;
   //--
   printf("[%s] 不是Cycle下一營業日[%s]", a_chkDate, ls_rDate);
   okExit(0);
}
//-------
void selectPtrWorkDay() throws Exception {
   String ls_runDate =commDate.dateAdd(hBusiDate,0,0,-1);
   //-Cycle-day+1-
   sqlCmd ="select count(*) as cycle_cnt"
       +" from ptr_workday"
       +" where this_close_date =?"
       ;
   ppp(1, ls_runDate);
   sqlSelect("");
   if (sqlNrow >0 && colInt("cycle_cnt")>0) {
      printf(" 停卡處理日期[%s]", ls_runDate);
   }
   else {
      printf(" 不是停卡處理日期[%s]", ls_runDate);
      okExit(0);
   }
}

void procData() throws Exception {

   sqlCmd = " select A.acno_p_seqno"
//       +" , B.card_acct_idx"
       +", C.card_no "
       +", A.rc_use_indicator "
       +" from act_acno A"
//       +"   join cca_card_acct B on A.acno_p_seqno = B.acno_p_seqno "
       +"   join crd_card C on A.acno_p_seqno=C.acno_p_seqno and C.current_code='0' "
       +" where A.int_rate_mcode >= 2 "
   ;

   openCursor();
   printf("-- open-cursor is OK");

   while (fetchTable()) {
      totalCnt++;
      initData();
      acnoPSeqno = colSs("acno_p_seqno");
      cardNo =colSs("card_no");
      rcUseIndicator =colSs("rc_use_indicator");
      //--找出帳戶下活卡進行停卡
//      selectCrdCard();
      if (totalCnt % 5000 == 0) {
         sqlCommit();
         printf(" process count=[%s]", totalCnt);
      }
      updateCrdCard();
      ccaOutGoing.InsertCcaOutGoing(cardNo, currentCode, hBusiDate, oppoReason);
      
      insertCrdJcic();
   }
   closeCursor();
   sqlCommit();
}

//void selectCrdCard() throws Exception {
//
//   String sql1 = " select card_no "
//       +" from crd_card where acno_p_seqno = ? and current_code ='0' ";
//   setString(1, acnoPSeqno);
//   sqlSelect(sql1);
//
//   int rownum = sqlNrow;
//
//   for (int ii = 0; ii < rownum; ii++) {
//      initDataCard();
//      cardNo = colSs(ii, "card_no");
//      updateCrdCard();
//      ccaOutGoing.InsertCcaOutGoing(cardNo, currentCode, hBusiDate, oppoReason);
//   }
//
//}

void updateCrdCard() throws Exception {
   daoTable = "crd_card";
   updateSQL = "current_code = '4' , oppost_date = to_char(sysdate,'yyyymmdd') , oppost_reason =? , lost_fee_code = 'N' , "
       +"mod_user = 'ecs' , mod_pgm = 'RskP183' , mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 "
   ;
   whereStr = " where card_no = ? ";
   setString(1, oppoReason);
   setString(2, cardNo);
   updateTable();
}

void insertCrdJcic() throws Exception {
    String hRowid = "";
    String hPaymentDate = "";
    hRowid = "";
    hPaymentDate = "" ;
    
    sqlCmd = "select rowid  as rowid ";
    sqlCmd += " from crd_jcic  ";
    sqlCmd += "where card_no  = ?  ";
    sqlCmd += "and trans_type = 'C'  ";
    sqlCmd += "and to_jcic_date =''  ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, cardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hRowid = getValue("rowid");

        daoTable   = "crd_jcic";
        updateSQL  = " current_code  = ?,";
        updateSQL += " oppost_reason = ?,";
        updateSQL += " oppost_date   = ?,";
        updateSQL += " payment_date  = ?,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where rowid    = ? ";
        setString(1, currentCode);
        setString(2, oppoReason);
        setString(3, sysDate);
        setString(4, hPaymentDate);
        setString(5, pgmName);
        setString(6, prgmId);
        setRowId(7, hRowid);
        updateTable();

        return;
    }

    setValue("card_no"      , cardNo);
    setValue("crt_date"     , sysDate);
    setValue("crt_user"     , pgmName);
    setValue("trans_type"   , "C");
    setValue("current_code" , currentCode);
    setValue("oppost_reason", oppoReason);
    setValue("oppost_date"  , sysDate);
    setValue("payment_date" , hPaymentDate);
    setValue("is_rc"        , rcUseIndicator);
    setValue("mod_user"     , pgmName);
    setValue("mod_time"     , sysDate + sysTime);
    setValue("mod_pgm"      , prgmId);
    daoTable = "crd_jcic";
    insertTable();
    if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_crd_jcic duplicate!", "", comcr.hCallBatchSeqno);
    }

}

void initData() {
   acnoPSeqno = "";
   cardNo="";
}

//void initDataCard() {
//   cardNo = "";
//}

}
