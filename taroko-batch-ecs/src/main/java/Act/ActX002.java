/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/08/13  V1.00.01  Allen Ho   act_x001                                   *
* 111/10/12  V1.00.02  Suzuwei    sync from mega & updated for project coding standard   * 
*                                                                            *
******************************************************************************/
package Act;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class ActX002 extends AccessDAO
{
 private final String PROGNAME = "檢核acct_jrnl_bal, min_pay_bal是否無誤程式 111/10/12 V1.00.02";

 String businessDate   = "";

 long    totalCnt=0,wdayCnt=0,sumCnt=0;
 int cnt1=0;
  int errCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ActX002 proc = new ActX002();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if ( !connectDataBase() ) exitProgram(1);

   selectPtrBusinday();

   selectPtrWorkday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入 act_acct_sum");
   loadActAcctSum();
   showLogMessage("I","","=========================================");
   for (int inti=0;inti<wdayCnt;inti++)
      {
       showLogMessage("I","","處理 stmt_cycle ["+getValue("wday.stmt_cycle",inti)+"]");
       showLogMessage("I","","-----------------------------------------");
       loadActAcag(getValue("wday.stmt_cycle",inti));
       selectActAcct(getValue("wday.stmt_cycle",inti));
       showLogMessage("I","","-----------------------------------------");
      }

  if (errCnt>0) 
      exitProgram(1);
  else
     { 
      showLogMessage("I","","****************************************");
      showLogMessage("I","","***                                  ***");
      showLogMessage("I","","**                                    **");
      showLogMessage("I","","**            CHECK OK !!!            **");
      showLogMessage("I","","**                                    **");
      showLogMessage("I","","***                                  ***");
      showLogMessage("I","","****************************************");
     }

   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "w";

  wdayCnt = selectTable();

  return(0);
 }
// ************************************************************************
 void  selectActAcct(String stmtCycle) throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "id_p_seqno,"
            + "end_bal_op,"
            + "end_bal_lk,"
            + "adi_end_bal,"
            + "acct_jrnl_bal,"
            + "min_pay_bal";
  daoTable  = "act_acct";
  whereStr  = "where stmt_cycle = ? "
            ;

  setString(1 , stmtCycle);

  openCursor();
  double sumValue = 0;
  while( fetchTable() ) 
   {
    setValue("aasm.p_seqno",getValue("p_seqno"));
    cnt1 = getLoadData("aasm.p_seqno");
    if (cnt1==0) setValue("aasm.end_bal" , "0");

    sumValue = getValueDouble("aasm.end_bal")
              - getValueDouble("end_bal_op")
              - getValueDouble("end_bal_lk");

    if (getValueDouble("acct_jrnl_bal") != (sumValue + getValueInt("adi_end_bal")))
       {
        errCnt++;
        selectCrdIdno();
        String errmsg = "ERROR ["
                + getValue("acct_type") + "-"
                + getValue("idno.id_no") + getValue("idno.id_no_code") + "][" 
                + getValue("p_seqno") + "]" 
                + " ACCT=" 
                + String.format("%.0f",getValueDouble("acct_jrnl_bal"))
                + " DEBT="
                + String.format("%.0f",getValueDouble("aasm.end_bal"))
                + " DIFF="
                + String.format("%.0f",(getValueDouble("acct_jrnl_bal") 
                                      - getValueDouble("adi_end_bal")
                                      - sumValue));
        showLogMessage("I","",errmsg);
//        processNoticeIT(errmsg);

        selectActAcctSum();

        showLogMessage("I",""," AF=" + getActAcctSum("AF")
                             +" LF=" + getActAcctSum("LF")
                             +" CF=" + getActAcctSum("CF")
                             +" PF=" + getActAcctSum("PF")
                             +" BL=" + getActAcctSum("BL"));
        showLogMessage("I",""," CA=" + getActAcctSum("CA")
                             +" IT=" + getActAcctSum("IT")
                             +" ID=" + getActAcctSum("ID")
                             +" RI=" + getActAcctSum("RI")
                             +" PN=" + getActAcctSum("PN"));

        showLogMessage("I",""," AI=" + getActAcctSum("AI")
                             +" SF=" + getActAcctSum("SF")
                             +" AO=" + getActAcctSum("AO")
                             +" CB=" + getActAcctSum("CB")
                             +" CI=" + getActAcctSum("CI"));

        showLogMessage("I",""," OT=" + getActAcctSum("OT")
                             +" CC=" + getActAcctSum("CC")
                             +" DB=" + getActAcctSum("DB"));

        showLogMessage("I",""," ADI=" + String.format("%10.0f",getValueDouble("adi_end_bal"))
                             +" OP =" + String.format("%10.0f",getValueDouble("end_bal_op"))
                             +" LK =" + String.format("%10.0f",getValueDouble("end_bal_lk")));
       }


    if (getValueDouble("min_pay_bal") >= 0) 
       {
        setValue("acag.p_seqno",getValue("p_seqno"));
        cnt1 = getLoadData("acag.p_seqno");
        if (cnt1<=0) setValue("acag.pay_amt" , "0");

        if (getValueDouble("min_pay_bal") != getValueDouble("acag.pay_amt"))
           {
            errCnt++;
            selectCrdIdno();
            String errmsg = "MP ["
                    + getValue("acct_type") + "-"
                    + getValue("idno.id_no") + getValue("idno.id_no_code") + "]"
                    + "ACCT="
                    + String.format("%10.0f",getValueDouble("min_pay_bal"))
                    + "ACAG="
                    + String.format("%10.0f",getValueDouble("acag.pay_amt"))
                    + "ACAG="
                    + String.format("%10.0f",getValueDouble("acag.pay_amt"))
                    + "DIFF="
                    + String.format("%10.0f", getValueDouble("min_pay_bal")
                    +                       - getValueDouble("acag.pay_amt"));
            showLogMessage("I","",errmsg);

//            processNoticeIT(errmsg);
            }
       }      
    if (errCnt>20) exitProgram(1);
   } 
  closeCursor();

  return;
 }
// ************************************************************************
 String getActAcctSum(String acctCode) throws Exception
 {
  for (int inti=0;inti<sumCnt;inti++)
     {
      if (getValue("asum.acct_code",inti).equals(acctCode))
         return(String.format("%10.0f",getValueDouble("asum.end_bal",inti)));
     }
  return("0");
 }
// ************************************************************************
 int selectActAcctSum() throws Exception
 {
  extendField = "asum.";
  selectSQL = "acct_code,"
            + "(unbill_end_bal+billed_end_bal) as end_bal";
  daoTable  = "act_acct_sum";
  whereStr  = "where p_seqno   = ? "
            ;

  setString(1 , getValue("p_seqno"));

  sumCnt = selectTable();

  if (sumCnt==0) 
    {
     showLogMessage("I","","act_acct_sum p_seqno :["+ getValue("p_seqno") +"] not found error");
     return(1);
    }
  return(0);
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_no,"
            + "id_no_code";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno   = ? "
            ;

  setString(1 , getValue("id_p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) 
    {
     showLogMessage("I","","crd_idno id_p_seqno :["+ getValue("id_p_seqno") +"] not found error");
     return(1);
    }
  return(0);
 }
// ************************************************************************
 void loadActAcctSum() throws Exception
 {
  extendField = "aasm.";
  selectSQL = "p_seqno,"
            + "sum(unbill_end_bal+billed_end_bal) as end_bal";
  daoTable  = "act_acct_sum";
  whereStr  = "where acct_code !='DP' "
            + "group by p_seqno "
            ;

  int  n = loadTable();
  setLoadData("aasm.p_seqno");

  showLogMessage("I","","Load act_acct_sum Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcag(String stmtCycle) throws Exception
 {
  extendField = "acag.";
  selectSQL = "p_seqno, "
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where stmt_cycle = ? "
            + "group by p_seqno "
            ;

  setString(1 , stmtCycle);

  int  n = loadTable();
  setLoadData("acag.p_seqno");

  showLogMessage("I","","Load act_acag Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
