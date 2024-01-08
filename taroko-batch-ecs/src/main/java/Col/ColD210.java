/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/04  V1.00.26  Allen Ho   Initial  clear MEGA                        *
* 112/06/14  V1.00.27  Sunny      調整loadColCpbdue3條件                     *
* 112/08/07  V1.00.28  Sunny      fix revolve_int_rate 處理判斷              *
* 112/09/05  V1.00.31  Allen Ho   Modify for base condition                  *
* 112/10/04  V1.00.34  Sunny      調整分期未到期授權碼的判斷 (只要是未入帳一律列入)               *
* 112/11/06  V1.00.37  Allen Ho   Modify Method dateOfLastWorkday&paymentRate*
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD210 extends AccessDAO
{
 private  String PROGNAME = "IFRS9 STAGE 試算處理程式 112/11/06 V1.00.37";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate    = "";

 long   totCnt=0,perCnt=0,comCnt=0;
 int[] loadCnt = new int[30];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD210 proc = new ColD210();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
   if ( args.length == 1 ) 
      { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if (businessDate.equals(comm.lastdateOfmonth(businessDate)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料計算日期"); }
   else if (businessDate.equals(dateOfLastWorkday(businessDate,1)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料試算日期");  }
   else if ((businessDate.equals(dateOfLastWorkday(businessDate,0)))&&
            (businessDate.substring(4,6).equals("12")))
      { showLogMessage("I","","本日["+ businessDate + "]為年度最後營業日期");  }
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }

   deleteColIfrsBase();                                                       
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadActAcno();
   loadActDebt();
   loadBilContract();
   loadCrdCorp();
   if ((businessDate.equals(comm.lastdateOfmonth(businessDate)))&&
       (businessDate.substring(4,6).equals("12")))
      loadBilBill(2);
   else
      loadBilBill(1);
   loadActAcct();
   loadCrdCard();
   loadCrdCard1();
   loadColLiacNego();
   loadCcaCardAcct();
   loadColCpbdue1();
   loadColCpbdue2();
   loadColCpbdue3();
   loadColCpbdue4();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  新增 col_ifrs_base");
   selectActAcno();
   showLogMessage("I","","=========================================");
  
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("business_date");
  showLogMessage("I","","本日營業日 : ["+ businessDate+"]");
 }
// ************************************************************************
 void selectActAcno() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acno_flag,"
            + "acct_type,"
            + "id_p_seqno,"
            + "corp_p_seqno,"
            + "acct_status,"
            + "decode(payment_rate1,'','0E',payment_rate1) as payment_rate1,"
            + "int_rate_mcode,"
            + "revolve_int_rate,"
            + "status_change_date,"
            + "line_of_credit_amt";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag in ('1','2') "
            + "and   acct_status <='3' ";

  openCursor();

  for (int int1=0;int1<loadCnt.length;int1++) loadCnt[int1]=0;
  int rCnt1=0,rCnt2=0,maxIssueMonths=0,intRateMcode=0;
  double revolveIntRate=0;
  String ifrsRate1="",acnoRate1="",newEndDate="";
  String[] loadDesc = new String[20];
  loadDesc[0]="P ACT_DEST     ";
  loadDesc[1]="P BIL_CONTRACT ";
  loadDesc[2]="P ACT_ACCT     ";
  loadDesc[3]="P CRD_CARD     ";
  loadDesc[4]="P CCA_CARD_ACCT";
  loadDesc[5]="P BIL_BILL     ";
  loadDesc[6]="P COL_CPBDUE1  ";
  loadDesc[7]="P COL_CPBDUE2  ";
  loadDesc[8]="P COL_CPBDUE4  ";
  loadDesc[9]="P COL_LIAC_NEGO";
  loadDesc[10]="P CRD_CARD issu";
  loadDesc[11]="C ACT_DEST     ";
  loadDesc[12]="C BIL_CONTRACT ";
  loadDesc[13]="C ACT_ACCT     ";
  loadDesc[14]="C CRD_CARD     ";
  loadDesc[15]="C CCA_CARD_ACCT";
  loadDesc[16]="C COL_CPBDUE3  ";
  loadDesc[17]="C CRD_CORP     ";
  loadDesc[18]="C Crd_card issu";

  while( fetchTable() )
    {
     totCnt++;

     setValue("ifrs.payment_rate1"      , getValue("payment_rate1"));
     setValue("ifrs.acct_status"        , getValue("acct_status"));
     setValue("ifrs.int_rate_mcode"     , getValue("int_rate_mcode"));
     setValue("ifrs.revolve_int_rate"   , getValue("revolve_int_rate"));
     setValue("ifrs.tear_cap_end_bal"   , "0");
     setValue("ifrs.cap_end_bal"        , "0");
     setValue("ifrs.int_end_bal"        , "0");
     setValue("ifrs.fee_end_bal"        , "0");
     setValue("ifrs.debt_end_bal"       , "0");
     setValue("ifrs.unpost_end_bal"     , "0");
     setValue("ifrs.op_end_bal"         , "0");
     setValue("ifrs.nego_flag"          , "");
     setValue("ifrs.liac_nego_flag"     , "");
     setValue("ifrs.stop_flag"          , "");
     setValue("ifrs.debt_flag"          , "");
     setValue("ifrs.issue_months"       , "0");
     setValue("ifrs.new_end_date"       , "");
     setValue("ifrs.empoly_type"        , "");

     if (getValue("acno_flag").equals("1"))
        { 
         setValue("ifrs.card_flag", "1");
         // *******************************************************
         setValue("debt.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("debt.p_seqno");
         if (rCnt1!=0) 
            {
             loadCnt[0]++;
             setValue("ifrs.cap_end_bal" , getValue("debt.cap_end_bal"));
             setValue("ifrs.int_end_bal" , getValue("debt.int_end_bal"));
             setValue("ifrs.fee_end_bal" , getValue("debt.fee_end_bal"));
             setValue("ifrs.debt_end_bal", getValue("debt.debt_end_bal"));
            }
         // *******************************************************
         setValue("cont.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("cont.p_seqno");
         if (rCnt1!=0) 
            {
             loadCnt[1]++;
             setValue("ifrs.unpost_end_bal", getValue("cont.unpost_end_bal"));
            }
         // *******************************************************
         setValue("acct.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("acct.p_seqno");
         if (rCnt1!=0) 
            {
             loadCnt[2]++;
             setValue("ifrs.op_end_bal", getValue("acct.op_end_bal"));
            }
         // *******************************************************  判斷最大效期
         setValue("car1.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("car1.p_seqno");
         if (rCnt1==0) 
            {
             setValue("ifrs.issue_months", "0");
             if (getValueInt("ifrs.cap_end_bal") + getValueInt("ifrs.int_end_bal") +
                 getValueInt("ifrs.fee_end_bal") + getValueInt("ifrs.debt_end_bal") +
                 getValueInt("ifrs.unpost_end_bal") ==0)
                 continue;
            }
          else
            {
             loadCnt[10]++;
             setValueInt("ifrs.issue_months", getValueInt("car1.issue_months"));
             setValue("ifrs.new_end_date", getValue("car1.new_end_date"));
            }
         // *******************************************************  判斷停用原因 凍結碼
         perCnt++;
         setValue("card.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("card.p_seqno");
         if (rCnt1!=0) 
            {
             loadCnt[3]++;
             setValue("ifrs.stop_flag", "S");
            }
          else
            {
             setValue("ccaa.p_seqno",getValue("p_seqno"));
             rCnt1 = getLoadData("ccaa.p_seqno");
             if (rCnt1!=0) 
                {
                 loadCnt[4]++;
                 setValue("ifrs.stop_flag", "F");
                }
            }
         // *******************************************************  判斷呆戶 
         setValue("bill.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("bill.p_seqno");
         if (rCnt1==0) 
            {
             loadCnt[5]++;
             setValue("ifrs.debt_flag", "Y");
            }
         else
            {
             if (getValueDouble("bill.debt_amt")==0)
                {
                 loadCnt[5]++;
                 setValue("ifrs.debt_flag", "Y");
                }
             if (businessDate.substring(4,6).equals("12"))
                setValueDouble("ifrs.year_dest_amt" , getValueDouble("bill.dest_amt"));
            }
         // ******************************************************* 判斷債務協商資料
         setValue("nego.id_p_seqno",getValue("id_p_seqno"));
         rCnt1 = getLoadData("nego.id_p_seqno");
         if (rCnt1!=0)
            {
             if (getValue("nego.liac_status").equals("3"))
                {
                 loadCnt[9]++;
                 setValue("ifrs.nego_flag", "0");
                }
             setValue("ifrs.liac_nego_flag", getValue("nego.liac_status"));
            }
         if (getValue("ifrs.nego_flag").length()==0)
            {
             setValue("cpb1.id_p_seqno",getValue("id_p_seqno"));
             rCnt1 = getLoadData("cpb1.id_p_seqno");
             if (rCnt1!=0) 
                {
                 loadCnt[6]++;
                 setValue("ifrs.nego_flag", "1");
                }
             else
                {
                 setValue("cpb2.id_p_seqno",getValue("id_p_seqno"));
                 rCnt1 = getLoadData("cpb2.id_p_seqno");
                 if (rCnt1!=0) 
                    {
                     loadCnt[7]++;
                     setValue("ifrs.nego_flag", "2");
                    }
                 else
                    {
                     setValue("cpb4.id_p_seqno",getValue("id_p_seqno"));
                     rCnt1 = getLoadData("cpb4.id_p_seqno");
                     if (rCnt1!=0) 
                        {
                         loadCnt[8]++;
                         setValue("ifrs.nego_flag", "4");
                        }
                    }
                }
            }
         // *******************************************************
         checkStage();
        }
     else
        {
         setValue("ifrs.card_flag", "2");

         setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("acno.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");
  //showLogMessage("I","","acno_p_seqno ["+getValue("corp_p_seqno") +"]");
         // *******************************************************
         int aliveFlag=0;
         maxIssueMonths = 0;
         newEndDate     = "";
         ifrsRate1 = "00000";
         for (int int1=0;int1<rCnt1;int1++)
           {
            if (getValue("acno.payment_rate1",int1).length()!=0)
               {
                if (getValue("acno.payment_rate1",int1).equals("0A"))
                   acnoRate1 = "001" + getValue("acno.payment_rate1",int1);
                else if (getValue("acno.payment_rate1",int1).equals("0B"))
                   acnoRate1 = "002" + getValue("acno.payment_rate1",int1);
                else if (getValue("acno.payment_rate1",int1).equals("0C"))
                   acnoRate1 = "003" + getValue("acno.payment_rate1",int1);
                else if (getValue("acno.payment_rate1",int1).equals("0D"))
                   acnoRate1 = "004" + getValue("acno.payment_rate1",int1);
                else if (getValue("acno.payment_rate1",int1).equals("0E"))
                   acnoRate1 = "000" + getValue("acno.payment_rate1",int1);
                else 
                   acnoRate1 = getValue("acno.payment_rate1",int1) + "000";
       
                if (acnoRate1.compareTo(ifrsRate1)>0)
                   ifrsRate1 = acnoRate1;
               }
            setValue("debt.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("debt.p_seqno");
            
            if (rCnt2!=0) 
               {
                loadCnt[11]++;
                setValueInt("ifrs.cap_end_bal" , getValueInt("ifrs.cap_end_bal")  + getValueInt("debt.cap_end_bal"));
                setValueInt("ifrs.int_end_bal" , getValueInt("ifrs.int_end_bal") + getValueInt("debt.int_end_bal"));
                setValueInt("ifrs.fee_end_bal" , getValueInt("ifrs.fee_end_bal") + getValueInt("debt.fee_end_bal"));
                setValueInt("ifrs.debt_end_bal", getValueInt("ifrs.debt_end_bal")+ getValueInt("debt.debt_end_bal"));
               }
            // *******************************************************
            setValue("cont.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("cont.p_seqno");
            if (rCnt2!=0) 
               {
                loadCnt[12]++;
                setValueInt("ifrs.unpost_end_bal" , getValueInt("ifrs.unpost_end_bal") + getValueInt("cont.unpost_end_bal"));
               }
            // *******************************************************
            setValue("car1.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("car1.p_seqno");
            if (rCnt2!=0)
               {
                aliveFlag=1;
                loadCnt[18]++;
                if (newEndDate.compareTo(getValue("car1.new_end_date"))<0)
                   {
                    maxIssueMonths = getValueInt("car1.issue_months");
                    newEndDate   = getValue("car1.new_end_date");
                   }
               }
           }
         if (aliveFlag==0)
         if (getValueInt("ifrs.cap_end_bal") + getValueInt("ifrs.int_end_bal") +
             getValueInt("ifrs.fee_end_bal") + getValueInt("ifrs.debt_end_bal") +
             getValueInt("ifrs.unpost_end_bal") + getValueInt("ifrs.op_end_bal")==0) 
             continue;

         if (ifrsRate1.substring(0,2).equals("00"))
            setValue("ifrs.payment_rate1"  , ifrsRate1.substring(3,5));
         else
            setValue("ifrs.payment_rate1"  , ifrsRate1.substring(0,2));
         // *******************************************************  判斷停用原因 凍結碼
         setValueInt("ifrs.issue_months"        , maxIssueMonths);
         setValue("ifrs.new_end_date"           , newEndDate);
         comCnt++;
         intRateMcode   = 0;
         revolveIntRate = 0;
         setValue("ifrs.debt_flag"          , "Y");
         for (int int1=0;int1<rCnt1;int1++)
           {
            if (getValueInt("acno.int_rate_mcode",int1)>intRateMcode)
               intRateMcode = getValueInt("acno.int_rate_mcode",int1);

            //20230807 sunny fix
            //if (getValueInt("acno.revolve_int_rate",int1)>intRateMcode)
            if (getValueInt("acno.revolve_int_rate",int1)>revolveIntRate)
               revolveIntRate = getValueDouble("acno.revolve_int_rate",int1);

            setValue("acct.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("acct.p_seqno");
            if (rCnt2!=0) 
               {
                loadCnt[13]++;
                setValueInt("ifrs.op_end_bal",  getValueInt("ifrs.op_end_bal") + getValueInt("acct.op_end_bal"));
               }
            // *******************************************************
            setValue("bill.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt1 = getLoadData("bill.p_seqno");
            if (rCnt2!=0)
               {
                if (getValueDouble("bill.debt_amt")!=0)
                   setValue("ifrs.debt_flag", "");

                if (businessDate.substring(4,6).equals("12"))
                   setValueInt("ifrs.year_dest_amt",  getValueInt("ifrs.year_dest_amt") + getValueInt("bill.dest_amt"));
               }
            // *******************************************************
            if (getValue("ifrs.stop_flag").length()!=0)
               {
                setValue("card.p_seqno",getValue("acno.acno_p_seqno",int1));
                rCnt2 = getLoadData("card.p_seqno");
                if (rCnt2!=0) 
                   {
                    loadCnt[14]++;
                    setValue("ifrs.stop_flag", "S");
                   }
                 else
                   {
                    setValue("ccaa.p_seqno",getValue("acno.acno_p_seqno",int1));
                    rCnt2 = getLoadData("ccaa.p_seqno");
                    if (rCnt2!=0) 
                       {
                        loadCnt[15]++;
                        setValue("ifrs.stop_flag", "F");
                       }
                   }
               }

            // *******************************************************
           }
         setValueInt("ifrs.int_rate_mcode"      , intRateMcode);
         setValueDouble("ifrs.revolve_int_rate" , revolveIntRate);
         // *******************************************************
         setValue("cpb3.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("cpb3.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("cpb3.corp_p_seqno,cpb3.acct_type");
         if (rCnt1!=0) 
            {
             loadCnt[16]++;
             setValue("ifrs.nego_flag", "1");
            }
         // ******************************************************* 
         setValue("corp.corp_p_seqno",getValue("corp_p_seqno"));
         rCnt1 = getLoadData("corp.corp_p_seqno");
         if (rCnt1!=0) 
            {
             loadCnt[17]++;
             setValue("ifrs.empoly_type", getValue("corp.empoly_type"));
            }
         // ******************************************************* 判斷債務協商資料
         checkStage();
        }
/*
     if ((businessDate.equals(dateOfLastWorkday(businessDate,0)))&&
         (businessDate.substring(4,6).equals("12")))
        {
         setValue("bila.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("bila.p_seqno");
         if (rCnt1!=0) setValueDouble("ifrs.year_cap_end_bal" , getValueDouble("bila.dest_amt"));
        }
*/
     insertColIfrsBase();
    }
  showLogMessage("I","","total   cnt :" + totCnt);
  showLogMessage("I","","person  cnt :" + perCnt);
  showLogMessage("I","","compant cnt :" + comCnt);

  for (int int1=0;int1<=18;int1++)
      showLogMessage("I","","loadCnt[" + int1 + "][" + loadDesc[int1] +"] = " + loadCnt[int1]);

  closeCursor();
 }
// ************************************************************************
 void insertColIfrsBase() throws Exception
 {
  extendField = "ifrs.";
  setValue("ifrs.proc_month"         , businessDate.substring(0,6));
  setValue("ifrs.p_seqno"            , getValue("p_seqno"));
  setValue("ifrs.acct_type"          , getValue("acct_type")); 
  setValue("ifrs.acno_flag"          , getValue("acno_flag")); 
  setValue("ifrs.id_p_seqno"         , getValue("id_p_seqno")); 
  setValue("ifrs.corp_p_seqno"       , getValue("corp_p_seqno")); 
  setValue("ifrs.acct_status"        , getValue("acct_status")); 
  setValue("ifrs.line_of_credit_amt" , getValue("line_of_credit_amt"));
  setValue("ifrs.mod_time"           , sysDate+sysTime);
  setValue("ifrs.mod_pgm"            , javaProgram);

  daoTable = "col_ifrs_base";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_ifrs_base error[dupRecord]["+ getValue("p_seqno") +"]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 String dateOfLastWorkday(String businessDate,int calDay) throws Exception
 {
  extendField = "holi.";
  selectSQL = "substr(holiday,7,2) as hday";
  daoTable  = "ptr_holiday";
  whereStr  = "WHERE holiday like ? "
            + "order by holiday desc "
            ;

  setString(1 , businessDate.substring(0,6)+"%");

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","Table ptr_holiday 資料不完整, 請確認["+ recCnt +"]");
      exitProgram(1);
     }

  String maxMonthDay=comm.lastdateOfmonth(businessDate).substring(6,8);
  int okFlag=0,daysOfWork=0;
  
  for (int inti= Integer.valueOf(maxMonthDay);inti>=1;inti--)
     {
      okFlag=0;
      for (int intk= 0;intk<recCnt;intk++)
        {
         if (String.format("%02d",inti).compareTo(getValue("holi.hday",intk))>0) break;
         if (String.format("%02d",inti).equals(getValue("holi.hday",intk))) 
            {
             okFlag=1;
             break;
            }
        }
      if (okFlag==0) 
         {
          daysOfWork++;
          if (calDay==0)
             {
              okFlag=inti;
              break;
             }
         }
      if ((daysOfWork==calDay)&&(calDay!=0))
         {
          okFlag=inti-1;
          break;
         }
     }
  return(businessDate.substring(0,6)+String.format("%02d",okFlag));
 }
// ************************************************************************
 void  loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "p_seqno,"
            + "sum(decode(acct_code,"
            + "           'BL',end_bal,'CA',end_bal,'IT',end_bal,"
            + "           'ID',end_bal,'AO',end_bal,'OT',end_bal,0)) as cap_end_bal,"
            + "sum(decode(acct_code,"
            + "           'CB',end_bal,'CI',end_bal,'CC',end_bal,"
            + "           'SF',end_bal,0)) as debt_end_bal,"
            + "sum(decode(acct_code,'RI',end_bal,0)) as int_end_bal,"
            + "sum(decode(acct_code,"
            + "           'CF',end_bal,'AF',end_bal,'LF',end_bal,"
            + "           'PF',end_bal,'PN',end_bal,0)) as fee_end_bal";
  daoTable  = "act_debt";
  whereStr  = "where end_bal > 0 "
            + "and acct_code not in ('DB','AI','DP') "
            + "group by p_seqno "
            + "having sum(decode(acct_code,"
            + "           'BL',end_bal,'CA',end_bal,'IT',end_bal,"
            + "           'ID',end_bal,'AO',end_bal,'OT',end_bal,"
            + "           'CB',end_bal,'CI',end_bal,'CC',end_bal,"
            + "           'SF',end_bal,'RI',end_bal,"
            + "           'CF',end_bal,'AF',end_bal,'LF',end_bal,"
            + "           'PF',end_bal,'PN',end_bal,0)) > 0 ";

  int  n = loadTable();
  setLoadData("debt.p_seqno");
  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 void loadBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "p_seqno,"
            + " sum(unit_price*(install_tot_term - install_curr_term) +"
            + "     remd_amt +"
            + "     decode(install_curr_term,0,first_remd_amt,0)) as unpost_end_bal";
  daoTable  = "bil_contract";
  whereStr  = "where install_tot_term != install_curr_term "
//TCB取消
//                  + "and   post_cycle_dd > 0 "
//            + "and   auth_code not in ('','N','REJECT','P','reject','LOAN') " 
//            + "and   ((post_cycle_dd > 0 "
//            + "  or    installment_kind ='F') "
//            + " or    (post_cycle_dd = 0 "
//            + "  and   delv_confirm_flag='Y' "
//            + "  and   auth_code='DEBT')) "
            + "group by p_seqno "
            + "having "
            + " sum(unit_price*(install_tot_term - install_curr_term) +"
            + "     remd_amt +"
            + "     decode(install_curr_term,0,first_remd_amt,0)) > 0 ";

  int  n = loadTable();
  setLoadData("cont.p_seqno");
  showLogMessage("I","","Load bil_contract Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "sum(end_bal_op+end_bal_lk) as op_end_bal";
  daoTable  = "act_acct";
  whereStr  = "where (end_bal_op + end_bal_lk)>0 "
            + "group by p_seqno";

  int  n = loadTable();
  setLoadData("acct.p_seqno");
  showLogMessage("I","","Load act_acct Count: ["+n+"]");
 }
// ************************************************************************
 void loadColLiacNego() throws Exception // 前置協商
 {
  extendField = "nego.";
  selectSQL = "id_p_seqno,"
            + "liac_status";
  daoTable  = "col_liac_nego";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("nego.id_p_seqno");
  showLogMessage("I","","Load col_liac_nego  Count: ["+n+"]");
 }
// ************************************************************************
 void loadBilBillx() throws Exception // 判斷呆戶
 {
  extendField = "bill.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "bil_bill";
  whereStr  = "where purchase_date >= ? "
            + "and   acct_code in ('BL','CA','ID','AO','OT') "
            + " or   (acct_code = 'IT' "
            + "  and  install_curr_term = 1 ) ";

  setString(1 , comm.lastMonth(businessDate,5)+"01");
  showLogMessage("I","","purchase_date : ["+ comm.lastMonth(businessDate,5)+"01" +"]");

  int  n = loadTable();
  setLoadData("bill.p_seqno");
  showLogMessage("I","","Load bil_bill  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCard() throws Exception // 判斷停卡
 {
  extendField = "card.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where current_code != '0' "
            + "and  oppost_date != '' "
            + "and  oppost_reason in ('T1','F1') ";

  int  n = loadTable();
  setLoadData("card.p_seqno");
  showLogMessage("I","","Load crd_card  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCard1() throws Exception // 判斷個卡效期到期日
 {
  extendField = "car1.";
  selectSQL = "p_seqno,"
            + "max(ceiling(months_between(to_date(new_end_date,'yyyymmdd'),to_date(?,'yyyymmdd')))) as issue_months,"
            + "max(new_end_date) as new_end_date";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' "
            + "and  new_end_date  >= ?  "
            + "group by p_seqno ";

  setString(1 , comm.lastdateOfmonth(businessDate)); 
  setString(2 , comm.lastdateOfmonth(businessDate));
 
  int  n = loadTable();
  setLoadData("car1.p_seqno");
  showLogMessage("I","","Load crd_card1  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCcaCardAcct() throws Exception // 判斷凍結
 {
  extendField = "ccaa.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "cca_card_acct";
  whereStr  = "where block_reason1 in ('06','0C','0F','14','15') "
            + " or   block_reason2 in ('06','0C','0F','14','15') " 
            + " or   block_reason3 in ('06','0C','0F','14','15') " 
            + " or   block_reason4 in ('06','0C','0F','14','15') " 
            + " or   block_reason5 in ('06','0C','0F','14','15') ";

  int  n = loadTable();
  setLoadData("ccaa.p_seqno");
  showLogMessage("I","","Load cca_card_acct  Count: ["+n+"]");
 }
// ************************************************************************
 void loadColCpbdue1() throws Exception // 公會協商
 {
  extendField = "cpb1.";
  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
  daoTable  = "col_cpbdue";
  whereStr  = "where cpbdue_type='1' "
            + "and   cpbdue_bank_type= '3' "
            + "and   cpbdue_acct_type='01' ";

  int  n = loadTable();
  setLoadData("cpb1.id_p_seqno");
  showLogMessage("I","","Load col_cpbfue1  Count: ["+n+"]");
 }
// ************************************************************************
 void loadColCpbdue2() throws Exception // 個別協商—個人 
 {
  extendField = "cpb2.";
  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
  daoTable  = "col_cpbdue";
  whereStr  = "where cpbdue_type='2' "             
            + "and   cpbdue_tcb_type= '3' "
            + "and   cpbdue_acct_type='01' ";

  int  n = loadTable();
  setLoadData("cpb2.id_p_seqno");
  showLogMessage("I","","Load col_cpbfue2  Count: ["+n+"]");
 }
// ************************************************************************
 void loadColCpbdue3() throws Exception // 個別協商—公司 
 {
  extendField = "cpb3.";
  selectSQL = "cpbdue_id_p_seqno as corp_p_seqno,"
            + "cpbdue_acct_type  as acct_type";
  daoTable  = "col_cpbdue";
  whereStr  = "where cpbdue_type='2' "
            + "and cpbdue_tcb_type= '3' "
            + "and cpbdue_acct_type='03' "
            + "group by cpbdue_id_p_seqno,cpbdue_acct_type "
            + "order by cpbdue_id_p_seqno,cpbdue_acct_type ";

  int  n = loadTable();
  setLoadData("cpb3.corp_p_seqno,cpb3.acct_type");
  showLogMessage("I","","Load col_cpbfue3  Count: ["+n+"]");
 }
// ************************************************************************
 void loadColCpbdue4() throws Exception // 前置調解
 {
  extendField = "cpb4.";
  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
  daoTable  = "col_cpbdue";
  whereStr  = "where cpbdue_type='3' "
            + "and cpbdue_medi_type= '3' "
            + "and cpbdue_acct_type='01' ";

  int  n = loadTable();
  setLoadData("cpb4.id_p_seqno");
  showLogMessage("I","","Load col_cpbfue4  Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcno() throws Exception // 公司戶
 {
  extendField = "acno.";
  selectSQL = "corp_p_seqno,"
            + "acct_type,"
            + "acno_p_seqno,"
            + "decode(payment_rate1,'','0E',payment_rate1) as payment_rate1,"
            + "revolve_int_rate,"
            + "int_rate_mcode";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag in ('3','Y') "  
            + "order by corp_p_seqno,acct_type ";

  int  n = loadTable();
  setLoadData("acno.corp_p_seqno,acno.acct_type");
  showLogMessage("I","","Load act_acno  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCorp() throws Exception // 前置調解
 {
  extendField = "corp.";
  selectSQL = "corp_p_seqno,"
            + "empoly_type";
  daoTable  = "crd_corp";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("corp.corp_p_seqno");
  showLogMessage("I","","Load crd_corp  Count: ["+n+"]");
 }
// ************************************************************************
 void checkStage() throws Exception
 {
  setValue("ifrs.stage_type", "1");
  if (Arrays.asList("S","F").contains(getValue("ifrs.stop_flag")))
     {
      setValue("ifrs.stage_type", "2");
     }
  if (getValue("ifrs.nego_flag").length()!=0)
     {
      setValue("ifrs.stage_type", "3");
     }

  if (getValue("ifrs.stage_type").equals("1"))
     {
      if (Arrays.asList("0A","0E").contains(getValue("ifrs.payment_rate1")))
         setValue("ifrs.stage_flag", "1");
      else if (Arrays.asList("0C").contains(getValue("ifrs.payment_rate1")))
         setValue("ifrs.stage_flag", "2");
      else if (Arrays.asList("0B","0D","01").contains(getValue("ifrs.payment_rate1")))
         setValue("ifrs.stage_flag", "3");
      else 
         {
          setValue("ifrs.stage_type", "2");
         }
     }
  if (getValue("ifrs.stage_type").equals("2"))
     {
      if (Arrays.asList("0A","0B","0C","0D","0E","01","02").contains(getValue("ifrs.payment_rate1")))
         setValue("ifrs.stage_flag", "1");
      else if (getValue("ifrs.payment_rate1").equals("03"))
         setValue("ifrs.stage_flag", "2");
      else 
         {
          setValue("ifrs.stage_type", "3");
         }

     }
  if (getValue("ifrs.stage_type").equals("3"))
     {
      if (Arrays.asList("0A","0B","0C","0D","0E","01","02","03","04","05","06").contains(getValue("ifrs.payment_rate1")))
         setValue("ifrs.stage_flag", "1");
      else
         setValue("ifrs.stage_flag", "2");

      if (getValue("ifrs.nego_flag").length()!=0)
         setValue("ifrs.stage_flag", "3");
     }
  
  return;
 }
// ***********************************************************************
 int deleteColIfrsBase() throws Exception
 {
  daoTable  = "col_ifrs_base";
  whereStr  = "where proc_month = ? ";

  setString(1 , businessDate.substring(0,6));

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_ifrs_base cnt :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 void loadBilBill(int year_type) throws Exception 
 {
  extendField = "bill.";
  selectSQL = "p_seqno,"
            + "sum(case when purchase_date >= ? then "
            + "    decode(sign_flag,'-',dest_amt*-1,dest_amt) else 0 end) as debt_amt,"
            + "sum(decode(sign_flag,'-',dest_amt*-1,dest_amt)) as dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where rsk_type not in ('1','2','3') "
            + "and   acct_code in ('BL','ID','IT','AO','OT','CA')  "
            + "and   dest_amt  != 0 "
            + "and   purchase_date between ? and ? "
            + "group by p_seqno ";

  setString(1 , comm.lastMonth(businessDate,5)+"01");
  if (year_type==1)
      setString(2 , comm.lastMonth(businessDate,5)+"01");
  else
      setString( 2 , businessDate.substring(0,4)+"0101");
  setString(3 , businessDate);

  showLogMessage("I","","purchase_date ["+ comm.lastMonth(businessDate,5)+"01" + "] - ["+ businessDate +"]");

  int  n = loadTable();
  setLoadData("bill.p_seqno");
  showLogMessage("I","","Load bil_bill1  Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample


