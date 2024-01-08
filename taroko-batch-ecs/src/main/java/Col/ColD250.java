/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/14  V1.00.11  Allen Ho   Initial                                    *
* 112/06/14  V1.00.12  Sunny      backup filename加日期                                               *
* 112/06/16  V1.00.13  Allen Ho                                              *
* 112/08/02  V1.00.14  Sunny      調整292行revolve_int_rate                    *
* 112/08/23  V1.00.24  Sunny      修正利息、費用為已使用額度的加項                                       *
* 112/09/07  V1.00.26  Sunny      CARDINT的利率一欄右靠  (取消debug log顯示)                 *
* 112/09/11  V1.00.27  Ryan       CARDOUT的利率一欄右靠  (取消debug log顯示)                 *
* 112/09/18  V1.00.28  Ryan       fileColumn[35] 四捨五入至小數第2位,格式化為00.00000   *
* 112/09/21  V1.00.29  Sunny      調整債務協商註記                                                              *
* 112/10/23  V1.00.30  Sunny      調整債務協商註記[37]先清空                                                *
* 112/11/09  V1.00.31  Sunny      修正CARDOUT已動用循環(810060019)及未動用循環科目(8100700006)，兩個相反*
* 112/11/09  V1.00.32  Sunny      風管部郁信需求增加傳送有加日期的檔案&增加年度最後營業日期執行(Method dateOfLastWorkday) *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD250 extends AccessDAO
{
 private  String PROGNAME = "IFRS9 資本計提檔案處理程式 112/11/09  V1.00.32";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrd        comc  = new CommCrd();
 CommString     comStr  = new CommString();
 CommCrdRoutine comcr = null;

 String businessDate    = "";

 long   totCnt=0,perCnt=0,comCnt=0;
 int[] loadCnt = new int[30];

 String buf = "",tmp="";
 String localDir   = "";
 String fileName1  = "";
 String fileName2  = "";
 String fileName4  = "";
 int    fo1,fo2,cnt1,rCnt1,rCnt2;
 double inCreditAmt  = 0;
 double outCreditAmt = 0;

 String newLine="\r\n";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD250 proc = new ColD250();
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

   comr  = new CommRoutine(getDBconnect(),getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if (businessDate.equals(comm.lastdateOfmonth(businessDate)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料計算日期"); }
   else if (businessDate.equals(dateOfLastWorkday(businessDate,1)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料試算日期"); }
   else if ((businessDate.equals(dateOfLastWorkday(businessDate,0)))&&
           (businessDate.substring(4,6).equals("12")))
     { showLogMessage("I","","本日["+ businessDate + "]為年度最後營業日期");  }
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }
    
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  載入暫存檔");
   loadActAcno();
   loadCrdCard();
   loadPtrActgeneralN();
   loadCrdCorp();
   loadCrdIdno();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","  讀取 col_ifrs_base");
   selectColIfrsBase();
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
 int  selectColIfrsBase() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir + "/CARDINT_" + sysDate + ".TXT"; //備份檔加日期
  fileName2 = localDir + "/CARDOUT_" + sysDate + ".TXT"; //備份檔加日期

  fo1 = openOutputText(fileName1,"MS950");
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }
  fo2 = openOutputText(fileName2,"MS950");
  if (fo2 == -1)
     {
      showLogMessage("I","","檔案"+fileName2+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "id_p_seqno,"
            + "corp_p_seqno,"
            + "stage_type,"
            + "stage_flag,"
            + "card_flag,"
            + "int_rate_mcode,"
            + "revolve_int_rate,"
            + "acct_status,"
            + "cap_end_bal,"
            + "int_end_bal,"
            + "fee_end_bal,"
            + "op_end_bal,"
            + "unpost_end_bal,"
            + "debt_end_bal,"
            + "liac_nego_flag,"
            + "(decode(sign(cap_end_bal - op_end_bal + int_end_bal + "
            + "             fee_end_bal + unpost_end_bal),-1,line_of_credit_amt,"
            + "   line_of_credit_amt -(cap_end_bal - op_end_bal + int_end_bal + "
            + "    fee_end_bal + unpost_end_bal))) as uuse_credit_amt,"  
            + "(cap_end_bal - op_end_bal + int_end_bal + "
            + " fee_end_bal + unpost_end_bal) as used_credit_amt";  
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            ;

  setString( 1 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","total   cnt :" + recCnt);

  String[] fileColumn = new String[40];
  String[] acctDesc   = new String[6];
  double[] acctEndBal = new double[6];
  String cardNo="",issueDate="",regBankNo="";

  for (int inti=0;inti<40;inti++) fileColumn[inti] = "";
  for (int inti=0;inti<6 ;inti++) acctEndBal[inti] = 0;

  for (int inti=0;inti<recCnt;inti++)
     {
      inCreditAmt  = getValueDouble("rept.used_credit_amt",inti);
      outCreditAmt = getValueDouble("rept.uuse_credit_amt",inti);
      if (outCreditAmt <0) outCreditAmt =0;

      fileColumn[1]="8";
      if (getValue("rept.card_flag",inti).equals("2"))
         fileColumn[1]="5";

      if (getValue("rept.id_p_seqno",inti).length()!=0)
         {
          setValue("idno.id_p_seqno",getValue("rept.id_p_seqno",inti));
          cnt1 = getLoadData("idno.id_p_seqno");
          if (cnt1==0)
             {
              showLogMessage("I","","id_p_seqno[" + getValue("rept.id_p_seqno",inti) +"] not found" );
              continue;
             }
          fileColumn[3] = getValue("idno.id_no");
         }
      else
         {
          setValue("corp.corp_p_seqno",getValue("rept.corp_p_seqno",inti));
          cnt1 = getLoadData("corp.corp_p_seqno");
          if (cnt1==0)
              {
              showLogMessage("I","","corp_p_seqno[" + getValue("rept.corp_p_seqno",inti) +"] not found" );
              continue;
             }
          fileColumn[3] = getValue("corp.corp_no");
         }
      if (fileColumn[3].length()>10) fileColumn[3]=fileColumn[3].substring(0,10);

      if (getValueInt("rept.int_rate_mcode",inti)==0) 
         fileColumn[9] = "0";
      else if (getValueInt("rept.int_rate_mcode",inti)>=7) 
         fileColumn[9] = "195";
      else
         fileColumn[9] = String.format("%d", (getValueInt("rept.int_rate_mcode",inti) * 30)-15);

      if (Arrays.asList("0","1").contains(getValue("rept.int_rate_mcode",inti)))
         fileColumn[11] = "1";
      else if (Arrays.asList("2","3").contains(getValue("rept.int_rate_mcode",inti)))
         fileColumn[11] = "2";
      else if (Arrays.asList("4","5","6").contains(getValue("rept.int_rate_mcode",inti)))
         fileColumn[11] = "4";
      else
         fileColumn[11] = "5";

      fileColumn[15] = "0";

      fileColumn[21] = "0"; 
      if (getValue("rept.acct_status",inti).equals("3")) fileColumn[21] = "1";

      cardNo    = "";
      issueDate = "";
      if (getValue("rept.card_flag",inti).equals("1"))
         {
          setValue("card.p_seqno",getValue("rept.p_seqno",inti));
          rCnt1 = getLoadData("card.p_seqno");
          if (rCnt1==0) 
             {
              showLogMessage("I","","1 p_seqno [" +  getValue("rept.p_seqno",inti) +"] no card_no error!" );
              continue;
             }
          cardNo    = getValue("card.card_no");
          regBankNo = getValue("card.reg_bank_no");
         }
      else
         {
          cardNo    = "9999999999999999";
          issueDate = "99999999";
          setValue("acno.corp_p_seqno", getValue("rept.corp_p_seqno",inti));
          setValue("acno.acct_type"   , getValue("rept.acct_type",inti));
          rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");

          for (int int1=0;int1<rCnt1;int1++)
            {
             setValue("card.p_seqno",getValue("acno.acno_p_seqno",int1));
             rCnt2 = getLoadData("card.p_seqno");
             if (rCnt2==0) 
                {
                 showLogMessage("I","","2  acno_p_seqno [" +  getValue("acno.acno_p_seqno",int1) +"] no card_no error!" );
                 continue;
                }
             if ((issueDate.compareTo(getValue("card.issue_date"))>0)||
                 ((issueDate.compareTo(getValue("card.issue_date"))==0)&&
                  (cardNo.compareTo(getValue("card.card_no"))>0)))
                {
                 cardNo = getValue("card.card_no");
                 issueDate = getValue("card.issue_date");
                 regBankNo = getValue("card.reg_bank_no");
                }
            }
         }
      if (issueDate.equals("99999999"))
         {
          showLogMessage("I","","3 p_seqno [" +  getValue("rept.p_seqno",inti) +"] no card_no error!" );
          continue;
         }
      fileColumn[23] = regBankNo + cardNo;

      fileColumn[33] = "6";
       
      setValue("pacn.acct_type" , getValue("rept.acct_type",inti));
      rCnt1 = getLoadData("pacn.acct_type");
      if (rCnt1==0) 
         {
          showLogMessage("I","","p_seqno [" +  getValue("rept.p_seqno",inti) +"] no acct_type error!" );
          continue;
         }
      fileColumn[35] = String.format("%08.5f",new BigDecimal(Math.round((getValueDouble("pacn.revolving_interest1")
                                                         -getValueDouble("rept.revolve_int_rate",inti))
                                                       *3650.0)/1000.0).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
      
      //利率為負值時則處理為0%
      if (fileColumn[35].compareTo("00.00000")<0)
      {
    	  showLogMessage("I","","p_seqno [" +  getValue("rept.p_seqno",inti) +",data35["+fileColumn[35]+"]" );
    	  fileColumn[35] = "00.00000";
    	  showLogMessage("I","","p_seqno [" +  getValue("rept.p_seqno",inti) +",data35["+fileColumn[35]+"]" );
      }
      
      //debug[35]，利率除錯使用
      //showLogMessage("I","","p_seqno [" +  getValue("rept.p_seqno",inti) +"] rate1["+getValueDouble("pacn.revolving_interest1")+"],acno_rate["+getValueDouble("rept.revolve_int_rate",inti)+"],data35["+fileColumn[35]+"]" );      
      
      if (getValue("rept.acct_status",inti).equals("3"))
         fileColumn[35] = "00.00000";

      //[37]債務協商註記
      fileColumn[37] = "";
      if (Arrays.asList("1","2","3","4","5","6").contains(getValue("rept.liac_nego_flag",inti)))
         fileColumn[37] = "2";

      fileColumn[38] = "";
      
      if (
          (!getValue("rept.acct_status",inti).equals("3"))&&
          ((getValue("rept.stage_type",inti).equals("1"))||
            (getValue("rept.acct_type",inti).equals("06"))))
         { 
          fileColumn[7] = String.format("%.0f",outCreditAmt); //未使用額度
          if ((getValue("rept.acct_type",inti).equals("06"))||
              (getValue("rept.stage_flag",inti).equals("1")))
              fileColumn[17] = "810070006"; //未動用額度科目
          else
              fileColumn[17] = "810060019"; //已動用額度科目

          buf  = fixLeft(fileColumn[1]        , 1  )
               + fixLeft(fileColumn[2]        , 1  )
               + fixLeft(fileColumn[3]        , 10 )
               + fixLeft(fileColumn[4]        , 1  )
               + fixLeft(fileColumn[5]        , 3  )
               + fixLeft(fileColumn[6]        , 1  )
               + fixRight(fileColumn[7]       , 11 )
               + fixLeft(fileColumn[8]        , 1  )
               + fixRight(fileColumn[9]       , 5  )
               + fixLeft(fileColumn[10]       , 1  )    
               + fixLeft(fileColumn[11]       , 1  )
               + fixLeft(fileColumn[12]       , 1  )
               + fixLeft(fileColumn[13]       , 1  )
               + fixLeft(fileColumn[14]       , 1  )
               + fixRight(fileColumn[15]      , 11 )
               + fixLeft(fileColumn[16]       , 1  )
               + fixLeft(fileColumn[17]       , 9  )    
               + fixLeft(fileColumn[18]       , 1  )
               + fixRight(String.format("%.0f",Math.floor(outCreditAmt/2))  , 11  )
               + fixLeft(" "                  , 1  )
               + fixLeft("050"                , 3  )
               + fixLeft(" "                  , 1  )
               + fixLeft(fileColumn[19]       , 1  ) 
               + fixLeft(fileColumn[20]       , 1  )
               + fixLeft(fileColumn[21]       , 1  )
               + fixLeft(fileColumn[22]       , 1  )
               + fixLeft(fileColumn[23]       , 20 )
               + fixLeft(fileColumn[24]       , 1  )
               + fixLeft(fileColumn[25]       , 6  )
               + fixLeft(fileColumn[26]       , 1  )
               + fixLeft(fileColumn[27]       , 3  )
               + fixLeft(fileColumn[28]       , 1  )
               + fixLeft(fileColumn[29]       , 7  )
               + fixLeft(fileColumn[30]       , 1  )
               + fixLeft(fileColumn[31]       , 10 )
               + fixLeft(fileColumn[32]       , 1  )
               + fixLeft(fileColumn[33]       , 1  )
               + fixLeft(fileColumn[34]       , 1  )
               + fixRight(fileColumn[35]       , 8  )
               + fixLeft(fileColumn[36]       , 1  )
               + fixLeft(fileColumn[37]       , 1  )
               + fixLeft(fileColumn[38]       , 56 );

          writeTextFile(fo2,buf+newLine);
         }

      acctEndBal[1] = getValueDouble("rept.fee_end_bal",inti);
      acctEndBal[2] = getValueDouble("rept.cap_end_bal",inti);
      acctEndBal[3] = getValueDouble("rept.int_end_bal",inti);
      acctEndBal[4] = getValueDouble("rept.unpost_end_bal",inti);
      acctEndBal[5] = getValueDouble("rept.debt_end_bal",inti);
//debug
//      if (getValueDouble("rept.op_end_bal",inti)>0)
//         {
////        showLogMessage("I","","p_seqno [" +  getValue("rept.p_seqno",inti) +"] had op_end_bal!" );
//         }
      acctDesc[1] = "130270024";
      acctDesc[2] = "130270032";
      acctDesc[3] = "130270041";
      acctDesc[4] = "130270059";
      acctDesc[5] = "155410016";

      for (int intk=1;intk<=5;intk++)
         { 
          if (acctEndBal[intk]==0) continue;

          fileColumn[7] = String.format("%.0f",acctEndBal[intk]);
          fileColumn[17] = acctDesc[intk];

          buf  = fixLeft(fileColumn[1]        , 1  )
               + fixLeft(fileColumn[2]        , 1  )
               + fixLeft(fileColumn[3]        , 10 )
               + fixLeft(fileColumn[4]        , 1  )
               + fixLeft(fileColumn[5]        , 3  )
               + fixLeft(fileColumn[6]        , 1  )
               + fixRight(fileColumn[7]       , 11 )
               + fixLeft(fileColumn[8]        , 1  )
               + fixRight(fileColumn[9]       , 5  )
               + fixLeft(fileColumn[10]       , 1  )
               + fixLeft(fileColumn[11]       , 1  )
               + fixLeft(fileColumn[12]       , 1  )
               + fixLeft(fileColumn[13]       , 1  )
               + fixLeft(fileColumn[14]       , 1  )
               + fixRight(fileColumn[15]      , 11 )
               + fixLeft(fileColumn[16]       , 1  )
               + fixLeft(fileColumn[17]       , 9  )
               + fixLeft(fileColumn[18]       , 1  )
               + fixLeft(fileColumn[19]       , 1  )
               + fixLeft(fileColumn[20]       , 1  )
               + fixLeft(fileColumn[21]       , 1  )
               + fixLeft(fileColumn[22]       , 1  )
               + fixLeft(fileColumn[23]       , 20 )
               + fixLeft(fileColumn[24]       , 1  )
               + fixLeft(fileColumn[25]       , 6  )
               + fixLeft(fileColumn[26]       , 1  )
               + fixLeft(fileColumn[27]       , 3  )
               + fixLeft(fileColumn[28]       , 1  )
               + fixLeft(fileColumn[29]       , 7  )
               + fixLeft(fileColumn[30]       , 1  )
               + fixLeft(fileColumn[31]       , 10 )
               + fixLeft(fileColumn[32]       , 1  )
               + fixLeft(fileColumn[33]       , 1  )
               + fixLeft(fileColumn[34]       , 1  )
               + fixRight(fileColumn[35]       , 8  )
               + fixLeft(fileColumn[36]       , 1  )
               + fixLeft(fileColumn[37]       , 1  )
               + fixLeft(fileColumn[38]      , 72 );

          writeTextFile(fo1,buf+newLine);
         }
     }

  closeOutputText(fo1);
  closeOutputText(fo2);

  localDir  = "/crdatacrea/RM";
  
  fileName4 = localDir + "/CARDINT.TXT";
  copyFileUsingStream(fileName1,fileName4);
  showLogMessage("I","","copy file1: ["+fileName4+"]");
  fileName4 = localDir + "/CARDOUT.TXT";
  copyFileUsingStream(fileName2,fileName4);
  showLogMessage("I","","copy file2: ["+fileName4+"]");
//增加多傳送給風管部有帶日期的檔案(CARDINT_YYYYMMDD.TXT)
  fileName4 = localDir + "/CARDINT_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName1,fileName4);
  showLogMessage("I","","copy file3: ["+fileName4+"]");
//增加多傳送給風管部有帶日期的檔案(CARDOUT_YYYYMMDD.TXT)
  fileName4 = localDir + "/CARDOUT_" + sysDate + ".TXT";
  copyFileUsingStream(fileName2,fileName4);
  showLogMessage("I","","copy file4: ["+fileName4+"]");
  return(0);
 }
// ************************************************************************
 String fixLeft(String str, int len) throws UnsupportedEncodingException 
  {
   int size = (Math.floorDiv(len, 100) + 1) * 100;
   String spc = "";
   for (int i = 0; i < size; i++)
       spc += " ";
   if (str == null)
       str = "";
   str = str + spc;
   byte[] bytes = str.getBytes("MS950");
   byte[] vResult = new byte[len];
   System.arraycopy(bytes, 0, vResult, 0, len);

   return new String(vResult, "MS950");
 }
/************************************************************************/
 String fixRight(String str, int len) throws UnsupportedEncodingException 
   {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
        spc += " ";
    if (str == null)
        str = "";
    str = spc + str;
    byte[] bytes = str.getBytes("MS950");
    int offset = bytes.length - len;
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, offset, vResult, 0, len);
    return new String(vResult, "MS950");
   }
/************************************************************************/
 void loadCrdCorp() throws Exception 
 {
  extendField = "corp.";
  selectSQL = "corp_p_seqno,"
            + "corp_no";
  daoTable  = "crd_corp a";
  whereStr  = "where exists ("
            + "  select 1 "
            + "  from col_ifrs_base "
            + "  where proc_month = ? "
            + "  and   corp_p_seqno = a.corp_p_seqno) ";

  setString( 1 , businessDate.substring(0,6));

  int  n = loadTable();
  setLoadData("corp.corp_p_seqno");
  showLogMessage("I","","Load crd_corp  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdIdno() throws Exception // 前置調解
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "crd_idno a";
  whereStr  = "where exists ("
            + "  select 1 "
            + "  from col_ifrs_base "
            + "  where proc_month = ? "
            + "  and   id_p_seqno = a.id_p_seqno) ";

  setString( 1 , businessDate.substring(0,6));

  int  n = loadTable();
  setLoadData("idno.id_p_seqno");
  showLogMessage("I","","Load crd_idno  Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrActgeneralN() throws Exception
 {
  extendField = "pacn.";
  selectSQL = "acct_type,"
            + "revolving_interest1";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("pacn.acct_type");

  showLogMessage("I","","Load ptr_actgeneral_n  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCard() throws Exception // 判斷個卡效期到期日
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "issue_date,"
            + "reg_bank_no,"
            + "card_no";
  daoTable  = "crd_card";
  whereStr  = "order by p_seqno,issue_date,card_no";

  int  n = loadTable();
  setLoadData("card.p_seqno");
  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcno() throws Exception // 公司戶
 {
  extendField = "acno.";
  selectSQL = "corp_p_seqno,"
            + "acct_type,"
            + "acno_p_seqno,"
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
 String X_dateOfLastWorkday(String businessDate,int calDay) throws Exception
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
      if (okFlag==0) daysOfWork++;
      if (daysOfWork==calDay) 
         {
          okFlag=inti-1;
          break;
         }
     }
  return(businessDate.substring(0,6)+String.format("%02d",okFlag));
 }

//************************************************************************
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
 void copyFileUsingStream(String source, String dest) throws IOException 
  {
   InputStream is = null;
   OutputStream os = null;
   try {
       is = new FileInputStream(source);
       os = new FileOutputStream(dest);
       byte[] buffer = new byte[1024];
       int length;
       while ((length = is.read(buffer)) > 0) {
           os.write(buffer, 0, length);
       }
   } finally {
       is.close();
       os.close();
   }
 }
// ************************************************************************

}  // End of class FetchSample
