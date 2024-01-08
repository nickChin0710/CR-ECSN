/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -----------------------  -------------------------------------- *
*  109/02/11  V1.00.01  Simon Hsiao   copy CycB900                           *
*  111/10/13  V1.00.02  Yang Bo       sync code from mega                    *
*****************************************************************************/
package Act;

import com.*;

@SuppressWarnings("unchecked")
public class ActF090 extends AccessDAO
{
 private final String PROGNAME = "卡人帳務調整後重算mcode處理程式 111/10/13  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;
 

 String hBusiBusinessDate = "",  hTempSysdate = "";

 int     checkCnt =0, updateCnt=0;
 long    totalCnt=0;
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ActF090 proc = new ActF090();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+ PROGNAME);
  
   if ( !connectDataBase() )
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   showLogMessage("I","","本日營業日 : ["+ hBusiBusinessDate +"]");

   selectPtrWorkday();

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void selectPtrBusinday() throws Exception
 {
        hBusiBusinessDate = "";
        hTempSysdate = "";

        sqlCmd = "select business_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_sysdate ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("h_temp_sysdate");
        }

 }
// ************************************************************************
 public int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "order by stmt_cycle";

  int recCnt = selectTable();

   for (int inti=0;inti<recCnt;inti++)
     {
      showLogMessage("I","","處理 CYCLE : ["+ getValue("wday.stmt_cycle",inti) +"]");
      loadActAcag(inti);
      selectActAcno(inti);
      showLogMessage("I","","     讀取筆數 : ["+ totalCnt +"]   處理筆數 ["+updateCnt+"]");
      commitDataBase();
     }

  return(0);
 }
// ************************************************************************
public void selectActAcno(int intk) throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "a.int_rate_mcode,"
            + "a.rowid as rowid,"
            + "b.mix_mp_balance";
  daoTable  = "act_acno a,ptr_actgeneral_n b";
  whereStr  = "where a.acct_type = b.acct_type "
            + "and   a.stmt_cycle = ? "
            + "and a.p_seqno in ( select distinct P_SEQNO from act_acaj where process_flag = 'Y' "
            + "                   and to_char(mod_time,'yyyymmdd') >= ? ) "
            ;

  setString(1 , getValue("wday.stmt_cycle",intk));
  setString(2 , hBusiBusinessDate);

  openCursor();

  int mcode=0;
  totalCnt=0;  
  updateCnt=0;
  while( fetchTable() )
   {
    totalCnt++;

    setValue("acag.p_seqno" , getValue("p_seqno"));
    int cnt1 = getLoadData("acag.p_seqno");
    if (cnt1==0)
       {
        if (getValueInt("int_rate_mcode")==0) continue;
        updateActAcno(0);
        updateCnt++;
        continue;
       }

    double minAmount = 0;
    mcode =0;
    for (int inti=0; inti<cnt1; inti++ )
      {
       minAmount = minAmount + getValueDouble("acag.pay_amt",inti);
       if (minAmount > getValueInt("mix_mp_balance"))
          {
           mcode = (int)comm.monthBetween(getValue("acag.acct_month",inti),
                                          getValue("wday.this_acct_month",intk));
           break;
          }
      }

    if (totalCnt % 100000 == 0)
       showLogMessage("I","","  Proc Records :  "+totalCnt+ " Update Records : "+updateCnt);

    if (getValueInt("int_rate_mcode")==mcode) continue;
         
    updateCnt++;
    updateActAcno(mcode);
   }
  closeCursor();
 }
// ************************************************************************
 public void updateActAcno(int mcode) throws Exception
 {
  updateSQL  = "int_rate_mcode  = ? ";
  daoTable   = "act_acno";
  whereStr   = "where rowid = ? "
            ; 

  setInt(  1 , mcode);
  setRowId(2 , getValue("rowid"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_act_acno error!" );
      showLogMessage("I","","rowid=["+getValue("rowid")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 public void loadActAcag(int intk) throws Exception
 {
  extendField = "acag.";
  selectSQL = "p_seqno, "
            + "acct_month,"
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where stmt_cycle = ? "
            + "and   acct_month != ? "
            + "and   p_seqno in ( select distinct P_SEQNO from act_acaj where process_flag = 'Y' "
            + "                   and to_char(mod_time,'yyyymmdd') >= ? ) "
            + "group by p_seqno,acct_month "
            + "order by p_seqno,acct_month "
            ;

  setString(1 , getValue("wday.stmt_cycle",intk));
  setString(2 , getValue("wday.this_acct_month",intk));
  setString(3 , hBusiBusinessDate);

  int  n = loadTable();
  setLoadData("acag.p_seqno");

  showLogMessage("I","","Load act_acag Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

