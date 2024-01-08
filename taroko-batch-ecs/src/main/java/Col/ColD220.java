/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/13  V1.00.21  Allen Ho   Initial                                    *
* 112/06/14  V1.00.22  Sunny      CRF75,CRM208                               *
* 112/07/20  V1.00.23  Allen Ho   Mod execute date                           *
* 112/08/23  V1.00.24  Sunny      修正利息、費用為已使用額度的加項           *
* 112/09/04  V1.00.30  Allen Ho   修正TXT為未動用金額                        *
* 112/09/05  V1.00.31  Sunny      修改報表顯示逾期文字,以新系統Mcode定義為主 *
* 112/09/13  V1.00.32  Allen Ho   修正呆卡數值                               *
* 112/11/10  V1.00.35  Allen Ho   Modify Method dateOfLastWorkday            *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD220 extends AccessDAO
{
 private  String PROGNAME = "IFRS9 報表及檔案處理程式 112/11/10  V1.00.35";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrd        comc  = new CommCrd();
 CommCrdRoutine comcr = null;

 String businessDate    = "";
 String chiDate         = "";

 long   totCnt=0,perCnt=0,comCnt=0;
 int[] loadCnt = new int[30];

 String rptId_r1  = "";
 String rptName1  = "";
 int    rptSeq1   = 0;
 String buf = "",tmp="";
 int pageCnt=0,lineCnt=0;

 String idnoCode   = "";
 String localDir   = "";
 String fileName1  = "";
 String fileName2  = "";
 String fileName3  = "";
 String fileName4  = "";
 int fo1,fo2,fo3,cnt1;
 double inCreditAmt  = 0;
 double outCreditAmt = 0;
 String   newLine="\n";
 String idnoTYpe="";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD220 proc = new ColD220();
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

   showLogMessage("I","","===============================================");
   showLogMessage("I","","檢核執行日期");
   if (businessDate.equals(comm.lastdateOfmonth(businessDate)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料計算日期"); }
   else if (businessDate.equals(dateOfLastWorkday(businessDate,1)))
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料試算日期");  }
   else if ((businessDate.equals(dateOfLastWorkday(businessDate,0)))&&
            (businessDate.substring(4,6).equals("12")))
      { showLogMessage("I","","本日["+ businessDate + "]為年度最後業日期");  }
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }
   deletePtrBatchRpt();
      
   if ((businessDate.equals(comm.lastdateOfmonth(businessDate)))||
       (businessDate.equals(dateOfLastWorkday(businessDate,1)))||
       ((businessDate.equals(dateOfLastWorkday(businessDate,0)))&&
            (businessDate.substring(4,6).equals("12"))))
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","  載入暫存檔");
       loadCrdCorp();
       loadCrdIdno();
      }
   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  讀取 col_ifrs_base");
   selectColIfrsBaseR1();
   selectColIfrsBaseR2();
   if ((businessDate.equals(comm.lastdateOfmonth(businessDate)))&&
       (businessDate.substring(4,6).equals("12")))
      {
       showLogMessage("I","","本日["+ businessDate + "]為每年約定融資曝險額信用轉換係數");
       selectColIfrsBaseR3();
      }   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  寫入媒體 CRF75");
   writeFileCRF75();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  寫入媒體 CRM210");
   writeFileCRM210();
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
  chiDate = String.format("%03d",Integer.valueOf(sysDate.substring(0,4))-1911)+sysDate.substring(4,8);
 }
// ************************************************************************
 int  selectColIfrsBaseR1() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRF75_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
    selectSQL = "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','0','0E','0','1'),stage_type),stage_type)) as stage_type,"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','1','0E','1',stage_flag),stage_flag),stage_flag)) as stage_flag,"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','1','0E','1',card_flag),card_flag),card_flag)) as card_flag,"
            + "sum(decode(stop_flag,'',line_of_credit_amt,0)) as line_of_credit_amt,"
            + "sum(decode(sign(cap_end_bal - op_end_bal + int_end_bal +"              
            + "                fee_end_bal + unpost_end_bal),-1,line_of_credit_amt,"    
            + "    decode(sign(line_of_credit_amt -(cap_end_bal - op_end_bal + int_end_bal +" 
            + "                fee_end_bal + unpost_end_bal)),-1,0,"                           
            + "    line_of_credit_amt -(cap_end_bal - op_end_bal + int_end_bal +" 
            + "                         fee_end_bal + unpost_end_bal)))) as uuse_credit_amt,"
            + "sum(cap_end_bal - op_end_bal + int_end_bal + "
            + "    fee_end_bal + unpost_end_bal) as used_credit_amt";  
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            + "and   acct_status <= '2' "
            + "group by "
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','0','0E','0','1'),stage_type),stage_type)),"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','1','0E','1',stage_flag),stage_flag),stage_flag)),"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',decode(payment_rate1,'0A','1','0E','1',card_flag),card_flag),card_flag)) "
            + "order by 1,2,3";

  setString( 1 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","selectColIfrsBaseR1 total   cnt :" + recCnt);

  headFileR1();

  String[] stageTypeDesc     = new String[25];
  String[] stageFlagDesc     = new String[25];
  String[] usedCreditAmt     = new String[25];
  String[] uuseCreditAmt     = new String[25];
  String[] uuseCreditAmtC    = new String[25];
  String[] uuseCreditAmtD    = new String[25];

  double[] usedCreditAmtA    = new double[25];
  double[] uuseCreditAmtA    = new double[25];

  double[] sumusedCreditAmt  = new double[4];
  double[] sumuuseCreditAmt  = new double[4];
  double[] sumuuseCreditAmtC = new double[4];
  double[] sumuuseCreditAmtD = new double[4];

  for  (int inti=0;inti<4;inti++) 
     sumusedCreditAmt[inti] = sumuuseCreditAmt[inti] = sumuuseCreditAmtC[inti] = sumuuseCreditAmtD[inti] = 0;

  for  (int inti=0;inti<25;inti++)
     {
      stageTypeDesc[inti]=stageFlagDesc[inti]=usedCreditAmt[inti]=uuseCreditAmt[inti]=uuseCreditAmtC[inti]=uuseCreditAmtD[inti]= "";
      usedCreditAmtA[inti] = uuseCreditAmtA[inti] = 0;
     }

  stageTypeDesc[0]  = stageTypeDesc[4] = stageTypeDesc[7] = "STAGE1";
  stageTypeDesc[10] = stageTypeDesc[13] = "STAGE2";
  stageTypeDesc[16] = stageTypeDesc[19] = stageTypeDesc[22] = "STAGE3";

  stageFlagDesc[0] = "全清";
  stageFlagDesc[4] = "繳足最低應繳"; 
  stageFlagDesc[7] = "逾期一個月(Ｍ１)";
  stageFlagDesc[10]= "逾期二個月(Ｍ２)";
  stageFlagDesc[13]= "逾期三個月(Ｍ３)";
  stageFlagDesc[16]= "逾期四-六個月(Ｍ４－Ｍ６)";
  stageFlagDesc[19]= "逾期七個月以上(Ｍ７＋)";
  stageFlagDesc[22]= "債務協商";

 stageFlagDesc[1]="呆卡";
 for (int inti=1;inti<25;inti=inti+3)
     {
      stageFlagDesc[inti+1]="個人卡";
      stageFlagDesc[inti+2]="法人卡";
     }

 int intk=0,intm=0;
 for (int inti=0;inti<recCnt;inti++)
   {
    inCreditAmt  = getValueDouble("rept.used_credit_amt",inti);
    outCreditAmt = getValueDouble("rept.uuse_credit_amt",inti);
    if (outCreditAmt <0) outCreditAmt =0;

    if ((getValue("rept.stage_type",inti).equals("0"))&&
        (getValue("rept.card_flag",inti).equals("0")))
       {
        buf  = fixLeft("SATGE1"             , 8  )
             + fixLeft("政府採購卡"         , 25 )
             + fixRight(String.format("%,.0f", inCreditAmt) , 20 )
             + fixRight(String.format("%,.0f", outCreditAmt) , 20 )
             + fixRight("0"                  , 22 )
             + fixRight(String.format("%,.0f", outCreditAmt) , 22 );

        insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
        writeTextFile(fo1,buf+newLine);
   
        sumusedCreditAmt[0] =  inCreditAmt;
        sumuuseCreditAmt[0] =  outCreditAmt ;
        sumuuseCreditAmtD[0] = sumuuseCreditAmt[0];
  
        continue;
       }
    
    if (getValue("rept.stage_type",inti).equals("0")) intk = 1;
    else if (getValue("rept.stage_type",inti).equals("1")) intk = 2;
    else if (getValue("rept.stage_type",inti).equals("2")) intk=11;
    else intk=17;

/*
     if (getValue("rept.stage_type",inti).equals("0"))                                    
        {
         showLogMessage("I","","stage_type :["+ getValueInt("rept.stage_type",inti) +"]");
         showLogMessage("I","","stage_flag :["+ getValueInt("rept.stage_flag",inti) +"]");
         showLogMessage("I","","card_flag  :["+ getValueInt("rept.card_flag",inti) +"]");
         showLogMessage("I","","sum1       :["+ String.format("%,.0f",getValueDouble("rept.line_of_credit_amt",inti)) + "]");
         showLogMessage("I","","sum2       :["+ String.format("%,.0f",getValueDouble("rept.used_credit_amt",inti))  + "]");
         showLogMessage("I","","sum3       :["+ String.format("%,.0f",(getValueDouble("rept.line_of_credit_amt",inti)-getValueDouble("rept.used_credit_amt",inti)))  + "]");
        }
*/     
    intm= getValueInt("rept.stage_type",inti);
    if (intm==0) intm=1;
    sumusedCreditAmt[intm-1] = sumusedCreditAmt[intm-1] + inCreditAmt;

    sumuuseCreditAmt[intm-1] = sumuuseCreditAmt[intm-1] + outCreditAmt;

    if (getValue("rept.stage_flag",inti).equals("1"))                                    
       sumuuseCreditAmtD[intm-1] = sumuuseCreditAmtD[intm-1] + outCreditAmt;
    else
       sumuuseCreditAmtC[intm-1] = sumuuseCreditAmtC[intm-1] + outCreditAmt;
   
    
    usedCreditAmtA[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3]  
      =  usedCreditAmtA[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3]  
      +  inCreditAmt;

    uuseCreditAmtA[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3] 
      = uuseCreditAmtA[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3]
      + outCreditAmt;
       
    usedCreditAmt[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3] 
      = String.format("%,.0f", usedCreditAmtA[(intk-1) 
      + getValueInt("rept.card_flag",inti)
      + (getValueInt("rept.stage_flag",inti)-1)*3]);
                  
    uuseCreditAmt[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3] 
      = String.format("%,.0f", uuseCreditAmtA[(intk-1) 
      + getValueInt("rept.card_flag",inti)
      + (getValueInt("rept.stage_flag",inti)-1)*3]);
       
    if (getValue("rept.stage_flag",inti).equals("1"))                                    
       uuseCreditAmtD[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3] 
         = String.format("%,.0f", outCreditAmt);
    else 
       uuseCreditAmtC[(intk-1) + getValueInt("rept.card_flag",inti)+(getValueInt("rept.stage_flag",inti)-1)*3] 
         = String.format("%,.0f", outCreditAmt);
   }

  String sumuuseCreditAmtS = ""; 
  String sumuuseCreditAmtSC= ""; 
  String sumuuseCreditAmtSD= ""; 
  for (int inti=0;inti<25;inti++)
    {
     if ((inti!=0)&&((inti-1)%3!=0))
        if (usedCreditAmt[inti].length()==0) usedCreditAmt[inti]="0";

     if (inti> 10)
        { 
         uuseCreditAmt[inti] = "";
         uuseCreditAmtC[inti] = "";
         uuseCreditAmtD[inti] = "";
        }
     else
        {
         if ((uuseCreditAmtC[inti].length()==0)&&
            (usedCreditAmt[inti].length()!=0)) uuseCreditAmtC[inti]="0";
         if ((uuseCreditAmtD[inti].length()==0)&&
            (usedCreditAmt[inti].length()!=0)) uuseCreditAmtD[inti]="0";
        }

     buf  = fixLeft(stageTypeDesc[inti]  , 8  )
          + fixLeft(stageFlagDesc[inti]  , 25 )
          + fixRight(usedCreditAmt[inti] , 20 )
          + fixRight(uuseCreditAmt[inti] , 20 )
          + fixRight(uuseCreditAmtC[inti] , 22 )
          + fixRight(uuseCreditAmtD[inti] , 22 );

     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     if ((inti==9)||(inti==15)||(inti==24))
        {
         
         if (inti==9) intk=0;
         if (inti==15) intk=1;
         if (inti==24) intk=2;

         sumuuseCreditAmtS = String.format("%,.0f",sumuuseCreditAmt[intk]);
         sumuuseCreditAmtSC= String.format("%,.0f",sumuuseCreditAmtC[intk]);
         sumuuseCreditAmtSD= String.format("%,.0f",sumuuseCreditAmtD[intk]);
         if (intk!=0) 
             sumuuseCreditAmtS = sumuuseCreditAmtSC =sumuuseCreditAmtSD ="";
              
         buf  = fixLeft("小計"               , 8  )
              + fixLeft(""                   , 25 )
              + fixRight(String.format("%,.0f",sumusedCreditAmt[intk]) , 20 )
              + fixRight(sumuuseCreditAmtS , 20 ) 
              + fixRight(sumuuseCreditAmtSC, 22 ) 
              + fixRight(sumuuseCreditAmtSD, 22 );

         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
         writeTextFile(fo1,buf+newLine);

         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", "");
         writeTextFile(fo1,newLine);
         if (inti==24) 
            {
             buf  = fixLeft("總計"               , 8  )
                  + fixLeft(""                   , 25 )
                  + fixRight(String.format("%,.0f",(sumusedCreditAmt[0]+sumusedCreditAmt[1]+sumusedCreditAmt[2])) , 20 )
                  + fixRight(String.format("%,.0f",sumuuseCreditAmt[0]) , 20 ) 
                  + fixRight(String.format("%,.0f",sumuuseCreditAmtC[0]) , 22) 
                  + fixRight(String.format("%,.0f",sumuuseCreditAmtD[0]) , 22);

             insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
             writeTextFile(fo1,buf+newLine);
            }
        }
    }
  closeOutputText(fo1);
  
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRF75_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName1,fileName4);
  
  return(0);
 }
// ************************************************************************
 void headFileR1() throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD220R1";
  rptName1  = "信用卡 IFRS9 試算表";

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRF75     科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: 1"                               ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "分類            項目                    已動用額度           未動用額度    已動用循環未動用額度  未動用循環未動用額度";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "                                        (A)               (B)=(C)+(D)           (C)                    (D)";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "===================================================================================================================== ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  lineCnt = 6;
 }
/***********************************************************************/
 int  selectColIfrsBaseR2() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRM210_" + sysDate + ".TXT";
  fileName2 = localDir+"/CRM210_REV_" + sysDate + ".TXT";
  fileName3 = localDir+"/CRM210_NREV_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }
  fo2 = openOutputText(fileName2);
  if (fo2 == -1)
     {
      showLogMessage("I","","檔案"+fileName2+"無法開啟寫入 error!" );
      return(1);
     }
  fo3 = openOutputText(fileName3);
  if (fo3 == -1)
     {
      showLogMessage("I","","檔案"+fileName3+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
  selectSQL = "acct_type,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "corp_p_seqno,"
            + "corp_p_seqno,"
            + "stage_flag,"
            + "card_flag,"
            + "payment_rate1,"
            + "debt_flag,"
            + "empoly_type,"
            + "days(to_date(decode(new_end_date,'',?,new_end_date),'yyyymmdd')) - days(to_date(?,'yyyymmdd')) as max_issue_days,"
            + "line_of_credit_amt,"
            + "(cap_end_bal - op_end_bal + int_end_bal + "
            + " fee_end_bal + unpost_end_bal) as used_credit_amt";  
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            + "and   acct_status <= '2' "
            + "and   (stage_type = '1' "
            + " or    acct_type = '06') ";

  setString( 1 , comm.lastdateOfmonth(businessDate));
  setString( 2 , comm.lastdateOfmonth(businessDate));
  setString( 3 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","selectColIfrsBaseR2 total   cnt :" + recCnt);

  headFileR2();

  String[] stageFlagDesc = new String[24];
  double[][]  dayCreditAmt = new double[24][5];

  for (int inti=0;inti<24;inti++)
    { 
     for (int intk=0;intk<5;intk++) dayCreditAmt[inti][intk] = 0;
     stageFlagDesc[inti]= "";
    }

 int intk=0;
 int daysClass = 0;
 double inCreditAmt  = 0;
 double outCreditAmt = 0;
 int repType=0;
 String idnoCode="";
 for (int inti=0;inti<recCnt;inti++)
   {
    inCreditAmt = getValueDouble("rept.used_credit_amt",inti);
    if (inCreditAmt >=0)
       outCreditAmt = getValueDouble("rept.line_of_credit_amt",inti)
                    - getValueDouble("rept.used_credit_amt",inti) ;
    else
       outCreditAmt = getValueDouble("rept.line_of_credit_amt",inti);
    if (outCreditAmt <0) 
       {
        outCreditAmt =0;
        continue;
       }

    if (getValueInt("rept.max_issue_days",inti)<=30) daysClass = 1;
    else if (getValueInt("rept.max_issue_days",inti)<=90) daysClass = 2;
    else if (getValueInt("rept.max_issue_days",inti)<=180) daysClass = 3;
    else if (getValueInt("rept.max_issue_days",inti)<=365) daysClass = 4;
    else if (getValueInt("rept.max_issue_days",inti)>365) daysClass = 5;

    if ((getValueInt("rept.empoly_type",inti)<1)||
        (getValueInt("rept.empoly_type",inti)>5))
       setValue("rept.empoly_type","5",inti);

    if (daysClass==0)
       showLogMessage("I","","p_seqno days error["+ getValue("rept.p_seqno",inti) +"]");

    if (getValue("rept.acct_type",inti).equals("06"))
       {
        repType= 6 +getValueInt("rept.empoly_type",inti);
        dayCreditAmt[6+getValueInt("rept.empoly_type",inti)][daysClass-1] = 
               dayCreditAmt[6+getValueInt("rept.empoly_type",inti)][daysClass-1] + outCreditAmt;
        dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][1]          = 
               dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][1] + outCreditAmt;  
       }
    else if (getValue("rept.stage_flag",inti).equals("1"))
       {
        if (getValue("rept.card_flag",inti).equals("1"))
           {
            repType=6;
            dayCreditAmt[6][daysClass-1] = dayCreditAmt[6][daysClass-1] + outCreditAmt;  
           dayCreditAmt[12][1]           = dayCreditAmt[12][1] + outCreditAmt;  
          }
        else
          {
           repType= 6 +getValueInt("rept.empoly_type",inti);
           dayCreditAmt[6+getValueInt("rept.empoly_type",inti)][daysClass-1] 
               = dayCreditAmt[6+getValueInt("rept.empoly_type",inti)][daysClass-1] + outCreditAmt;
           dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][1] 
               = dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][1] + outCreditAmt;  
          }
       }
    else
       {
        if (getValue("rept.card_flag",inti).equals("1"))
           {
           repType= 0;
           dayCreditAmt[0][daysClass-1] = dayCreditAmt[0][daysClass-1] + outCreditAmt;
           dayCreditAmt[12][0]           = dayCreditAmt[12][0] + outCreditAmt;
          }
        else
          {
           repType = getValueInt("rept.empoly_type",inti);
           dayCreditAmt[getValueInt("rept.empoly_type",inti)][daysClass-1] 
              = dayCreditAmt[getValueInt("rept.empoly_type",inti)][daysClass-1] + outCreditAmt;  
           dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][0] 
              = dayCreditAmt[12+getValueInt("rept.empoly_type",inti)][0] + outCreditAmt;  
          }
       }

  if (getValue("rept.id_p_seqno",inti).length()!=0)
     {
      setValue("idno.id_p_seqno",getValue("rept.id_p_seqno",inti));
      cnt1 = getLoadData("idno.id_p_seqno");
      if (cnt1==0)
         {
          showLogMessage("I","","id_p_seqno[" + getValue("rept.id_p_seqno",inti) +"] not found" );
          continue;
         }
      idnoCode = getValue("idno.id_no");
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
      idnoCode = getValue("corp.corp_no");
     }

  if (inCreditAmt < 0) inCreditAmt = 0;
  buf  = "!"
        + String.format("%d",daysClass) + "!"
        + fixLeft(idnoCode , 11 ) + "!"
        + fixRight(String.format("%.0f",outCreditAmt) , 11 ) + "!"
        + fixRight(String.format("%.0f",getValueDouble("rept.line_of_credit_amt",inti)) , 11 );

  if (repType<=5)
     {
      buf  = String.format("%d",repType+1) + buf;
      writeTextFile(fo2,buf+newLine);
     }
  else
     {
      buf  = String.format("%d",repType-5) + buf;
      writeTextFile(fo3,buf+newLine);
     }

   }

  stageFlagDesc[0] = stageFlagDesc[6]  = stageFlagDesc[12] = "個人卡";
  stageFlagDesc[1] = stageFlagDesc[7]  = stageFlagDesc[13] = "法人卡－民營";
  stageFlagDesc[2] = stageFlagDesc[8]  = stageFlagDesc[14] = "法人卡－公營";
  stageFlagDesc[3] = stageFlagDesc[9]  = stageFlagDesc[15] = "法人卡－政府";
  stageFlagDesc[4] = stageFlagDesc[10] = stageFlagDesc[16] = "法人卡－金融";
  stageFlagDesc[5] = stageFlagDesc[11] = stageFlagDesc[17] = "法人卡－其他";

  buf = "動用循環額度者，其尚未動用之信用額度            ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<6;inti++)
    {
     buf  = fixLeft(stageFlagDesc[inti]  , 12 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][0]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][1]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][2]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][3]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][4]) , 20 );

     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
    }
  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "未動用循環額度者，其尚未動用之信用額度            ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  for (int inti=6;inti<12;inti++)
    {
     buf  = fixLeft(stageFlagDesc[inti]  , 12 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][0]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][1]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][2]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][3]) , 20 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][4]) , 20 );

     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
    }
  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "                          動用循環額度者，其尚未動用之信用額度            未動用循環額度者，其尚未動用之信用額度 ";
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  for (int inti=12;inti<18;inti++)
    {
     buf  = fixLeft(stageFlagDesc[inti]  , 12 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][0]) , 50 )
          + fixRight(String.format("%,.0f",dayCreditAmt[inti][1]) , 50 );

     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
    }
  closeOutputText(fo1);
  closeOutputText(fo2);
  closeOutputText(fo3);

  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM210_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName1,fileName4);
  localDir  = "/crdatacrea/RM";
  fileName4 = localDir+"/CRM210_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName1,fileName4);
  fileName4 = localDir+"/CRM210_REV_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName2,fileName4);
  fileName4 = localDir+"/CRM210_NREV_" + sysDate + ".TXT";
  copyFileUsingStream(fileName3,fileName4);

  return(0);
 }
// ************************************************************************
 void headFileR2() throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD220R2";
  rptName1  = "信用卡到期總授信承諾額度情形表";

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM210    科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "     中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: 1"                               ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);


  buf = "                      ０－３０天        ３１－９０天      ９１－１８０天        １８１－１年         １年以上 ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "動用循環額度者，其尚未動用之信用額度            ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "================================================================================================================ ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  lineCnt = 6;
 }
/***********************************************************************/
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
 int  selectColIfrsBaseR3() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRM208_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
  selectSQL = "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','0',stage_type),stage_type)) as stage_type,"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','1',stage_flag),stage_flag)) as stage_flag,"
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','1',card_flag),card_flag)) as card_flag, "
            + "decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',"
            + "  decode(payment_rate1,'0A','1','0E','1','2')),'0')) as debt_flag, "
            + "sum(year_dest_amt) as year_dest_amt,"
            + "sum(decode(sign(cap_end_bal - op_end_bal + int_end_bal +"              
            + "                fee_end_bal + unpost_end_bal),-1,line_of_credit_amt,"    
            + "    decode(sign(line_of_credit_amt -(cap_end_bal - op_end_bal + int_end_bal +" 
            + "                fee_end_bal + unpost_end_bal)),-1,0,"                           
            + "    line_of_credit_amt -(cap_end_bal - op_end_bal + int_end_bal +" 
            + "                         fee_end_bal + unpost_end_bal)))) as uuse_credit_amt,"
            + "sum(cap_end_bal - op_end_bal + int_end_bal + "
            + "    fee_end_bal + unpost_end_bal) as used_credit_amt";
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            + "and   acct_status <= '2' "
            + "and   (acct_type  = '06' "
            + " or    stage_type = '1') "
            + "group by decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','0',stage_type),stage_type)),"
            + "         decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','1',stage_flag),stage_flag)),"
            + "         decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1','1',card_flag),card_flag)),"
            + "         decode(acct_type,'06','0',decode(debt_flag,'Y',decode(stage_type,'1',"
            + "           decode(payment_rate1,'0A','1','0E','1','2')),'0')) "
            + "order by 1,2,3,4";

  setString( 1 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","total   cnt :" + recCnt);

  headFileR3();

  String[] stageFlagDesc     = new String[25];
  double   sumusedCreditAmt = 0, sumuuseCreditAmt= 0,sumuuseCreditAmtC = 0,sumuuseCreditAmtD = 0;

 for (int inti=0;inti<recCnt;inti++)
   {
    inCreditAmt  = getValueDouble("rept.used_credit_amt",inti);
    outCreditAmt = getValueDouble("rept.uuse_credit_amt",inti);
    if (outCreditAmt <0) outCreditAmt =0;

    if ((getValue("rept.stage_type",inti).equals("0"))&&
        (getValue("rept.card_flag",inti).equals("0")))
       {
        sumuuseCreditAmt =  getValueDouble("rept.year_dest_amt",inti); 
        sumuuseCreditAmtD = outCreditAmt ; 
  
        continue;
       }
    
    if (getValue("rept.debt_flag",inti).equals("1"))
       {                                    
        sumuuseCreditAmt  = sumuuseCreditAmt  + getValueDouble("rept.year_dest_amt",inti); 
        sumuuseCreditAmtD = sumuuseCreditAmtD + outCreditAmt ;
       }
    else if (getValue("rept.debt_flag",inti).equals("2"))                                    
       {                                    
        sumusedCreditAmt = sumusedCreditAmt  + getValueDouble("rept.year_dest_amt",inti); 
       sumuuseCreditAmtC = sumuuseCreditAmtC + outCreditAmt;
       }
    else if (getValue("rept.stage_flag",inti).equals("1"))                                    
       {                                    
        sumuuseCreditAmt  = sumuuseCreditAmt  + getValueDouble("rept.year_dest_amt",inti); 
        sumuuseCreditAmtD = sumuuseCreditAmtD + outCreditAmt;
       }
    else
       {                                    
        sumusedCreditAmt = sumusedCreditAmt  + getValueDouble("rept.year_dest_amt",inti); 
       sumuuseCreditAmtC = sumuuseCreditAmtC + outCreditAmt;
       }
   }

  String dispCreditAmt = ""; 
  stageFlagDesc[0] = String.format("%03d",Integer.valueOf(sysDate.substring(0,4))-1911)+"年度";
  stageFlagDesc[1] = "未動用額度者：";
  stageFlagDesc[2] = "　　近一年月平均新增消費本金加總（Ａ）　：" + String.format("%,20.2f",sumuuseCreditAmt/12.0);
  stageFlagDesc[3] = "　　剩餘可動用額度加總（Ｂ）：　　　　　　" + String.format("%,20.2f",sumuuseCreditAmtD); 
  stageFlagDesc[4] = "　　未動用額度信用轉換係數（Ｃ＝Ａ／Ｂ）：" + String.format("%,20.2f",sumuuseCreditAmt/12.0/sumuuseCreditAmtD*100.0)+"%";  
  stageFlagDesc[5] = String.format("%03d",Integer.valueOf(sysDate.substring(0,4))-1911)+"年度";
  stageFlagDesc[6] = "已動用額度者：";
  stageFlagDesc[7] = "　　近一年月平均新增消費本金加總（Ａ）　：" + String.format("%,20.2f",sumusedCreditAmt/12.0); 
  stageFlagDesc[8] = "　　剩餘可動用額度加總（Ｂ）：　　　　　　" + String.format("%,20.2f",sumuuseCreditAmtC); 
  stageFlagDesc[9] = "　　已動用額度信用轉換係數（Ｃ＝Ａ／Ｂ）：" + String.format("%,20.2f",sumusedCreditAmt/12.0/sumuuseCreditAmtC*100.0)+"%"; 

  for (int inti=0;inti<10;inti++)
    {
     buf  = stageFlagDesc[inti];
                                                                                                                                                                                                                                                            
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     if (inti==4)
        {
         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", "");
         writeTextFile(fo1,newLine);
        }
    } 

  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", "");
  writeTextFile(fo1,newLine);

  stageFlagDesc[0] = "備註：";
  stageFlagDesc[1] = " （１）未動用循環者：";
  stageFlagDesc[2] = "將未動用循環者之客戶分別計算上述帳戶起近一年１２個月月底之「每月新增消費本金」，取近一年「每月新增消費本金」之";
  stageFlagDesc[3] = "平均值為「近一年均新增消費本金」加總／上述帳戶之「剩餘可動用額度」即為「平均信用額度轉換係數」。";
  stageFlagDesc[4] = " （２）已動用循環者：";
  stageFlagDesc[5] = "將已動用循環者之客戶之「信用額度」減除「本金欠款餘額」為「剩餘可動用額度」，再分別計算上述帳戶近一年之「每月新";
  stageFlagDesc[6] = "增消費本金」，取年月平均新增消費本金」加總∕帳戶之「剩餘可動用額度」即為整體「平均信用額度轉換係數」。";

  for (int inti=0;inti<=6;inti++)
     {
      buf = stageFlagDesc[inti];
      insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
      writeTextFile(fo1,buf+newLine);
     }

  closeOutputText(fo1);
  
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM208_" + sysDate + ".TXT"; 
  copyFileUsingStream(fileName1,fileName4);
  
  return(0);
 }
// ************************************************************************
 void headFileR3() throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD220R3";
  rptName1  = "約定融資曝險額信用轉換係數";

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM208    科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "   中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: 1"                               ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "           項目                                                                                                    ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "================================================================================================================== ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  lineCnt = 6;
 }
/***********************************************************************/
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
 void insertPtrBatchRpt(String rptId_r1,String rptName1,int seq,String kind,String  buf) throws Exception 
 {
  noTrim= "Y";
  extendField = "rpt1.";
  setValue("rpt1.program_code"       , rptId_r1);
  setValue("rpt1.rptname"            , rptName1);
  setValue("rpt1.start_date"         , sysDate);
  setValue("rpt1.start_time"         , sysTime); 
  setValueInt("rpt1.seq"             , seq);
  setValue("rpt1.kind"               , kind);
  setValue("rpt1.report_content"    , buf);

  daoTable = "ptr_batch_rpt";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_ptr_batch_rpt error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 int deletePtrBatchRpt() throws Exception
 {
  daoTable  = "ptr_batch_rpt";
  whereStr  = "where program_code like 'ColD220%' "
            + "and   start_date = ? ";

  setString(1 , sysDate);

  int recCnt = deleteTable();

  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
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
 int  writeFileCRF75() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRF75_STAGE3A_" + sysDate + ".TXT"; 
  fileName2 = localDir+"/CRF75_STAGE3B_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  fo2 = openOutputText(fileName2);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }
  if (fo2 == -1)
     {
      showLogMessage("I","","檔案"+fileName2+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
  selectSQL = "id_p_seqno,"
            + "stage_type,"
            + "corp_p_seqno,"
            + "line_of_credit_amt,"
            + "(cap_end_bal - op_end_bal + int_end_bal + "
            + " fee_end_bal + unpost_end_bal) as used_credit_amt";  
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            + "and   acct_status <='2' "
            + "and   stage_type = '3' ";

  setString( 1 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","total   cnt :" + recCnt);

 int intk=0;
 for (int inti=0;inti<recCnt;inti++)
   {
    if (getValueDouble("rept.used_credit_amt",inti)<=0) continue;

    if (getValue("rept.id_p_seqno",inti).length()!=0)
       {
        setValue("idno.id_p_seqno",getValue("rept.id_p_seqno",inti));
        cnt1 = getLoadData("idno.id_p_seqno");
        if (cnt1==0)
           {
            showLogMessage("I","","id_p_seqno[" + getValue("rept.id_p_seqno",inti) +"] not found" );
            continue;
           }
        idnoCode = getValue("idno.id_no");
        idnoTYpe = "P";
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
        idnoCode = getValue("corp.corp_no");
        idnoTYpe = "B";
       }
    intk++;
    buf  =  fixRight(String.format("%08d",intk) , 8 ) +";"  
          + fixLeft(idnoCode , 11 ) +";";
    if (getValueDouble("rept.used_credit_amt",inti)>0)
       buf = buf + fixRight(String.format("+%,.0f", getValueDouble("rept.used_credit_amt",inti)) , 17 );
    else
       buf = buf + fixRight(String.format("%,.0f", getValueDouble("rept.used_credit_amt",inti)) , 17 );
    buf = buf + ";" + idnoTYpe + getValue("rept.stage_type",inti);

    writeTextFile(fo1,buf+newLine);

    buf  =  fixRight(String.format("%08d",intk) , 8 ) +";"  
          + fixLeft(idnoCode.substring(0,2)+"****"+idnoCode.substring(6) , 11 ) +";";
    if (getValueDouble("rept.used_credit_amt",inti)>0)
       buf = buf + fixRight(String.format("+%,.0f", getValueDouble("rept.used_credit_amt",inti)) , 17 );
    else
       buf = buf + fixRight(String.format("%,.0f", getValueDouble("rept.used_credit_amt",inti)) , 17 );
    buf = buf + ";" + idnoTYpe + getValue("rept.stage_type",inti);

    writeTextFile(fo2,buf+newLine);
   }                   
  closeOutputText(fo1);
  closeOutputText(fo2);

  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRF75_STAGE3A_" + sysDate + ".TXT";
  copyFileUsingStream(fileName1,fileName4); 
  fileName4 = localDir+"/CRF75_STAGE3B_" + sysDate + ".TXT";
  copyFileUsingStream(fileName2,fileName4);
  return(0);
 }
// ************************************************************************
 int writeFileCRM210() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  String fileNameB1 = localDir+"/CRM210_BCARD1_" + sysDate + ".TXT";
  String fileNameB2 = localDir+"/CRM210_BCARD2_" + sysDate + ".TXT";
  String fileNameP1 = localDir+"/CRM210_PCARD1_" + sysDate + ".TXT";
  String fileNameP2 = localDir+"/CRM210_PCARD2_" + sysDate + ".TXT";
  String buf1,buf2;
  
  int fo1 = openOutputText(fileNameB1);
  int fo2 = openOutputText(fileNameB2);
  int fo3 = openOutputText(fileNameP1);
  int fo4 = openOutputText(fileNameP2);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileNameB1+"無法開啟寫入 error!" );
      return(1);
     }
  if (fo2 == -1)
     {
      showLogMessage("I","","檔案"+fileNameB2+"無法開啟寫入 error!" );
      return(1);
     }
  if (fo3 == -1)
     {
      showLogMessage("I","","檔案"+fileNameP1+"無法開啟寫入 error!" );
      return(1);
     }
  if (fo4 == -1)
     {
      showLogMessage("I","","檔案"+fileNameP2+"無法開啟寫入 error!" );
      return(1);
     }

  extendField = "rept.";
  selectSQL = "acct_type,"
            + "stage_type,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "corp_p_seqno,"
            + "stage_flag,"
            + "card_flag,"
            + "line_of_credit_amt,"
            + "(cap_end_bal - op_end_bal + int_end_bal + "
            + " fee_end_bal + unpost_end_bal) as used_credit_amt";  
  daoTable  = "col_ifrs_base";    
  whereStr  = "where proc_month   = ? "
            + "and   acct_status <='2' "
            + "and   ((stage_type  = '1' "
            + "  and   stage_flag  = '1') "
            + " or    acct_type = '06') ";

  setString( 1 , businessDate.substring(0,6));
  int recCnt = selectTable();

  showLogMessage("I","","total   cnt :" + recCnt);

 int[] intk = new int[4];
 for (int inti=0;inti<4;inti++) intk[inti]=0;
 for (int inti=0;inti<recCnt;inti++)
   {
    inCreditAmt = getValueDouble("rept.used_credit_amt",inti);
    if (inCreditAmt >=0)
       outCreditAmt = getValueDouble("rept.line_of_credit_amt",inti)
                    - getValueDouble("rept.used_credit_amt",inti) ;
    else
       outCreditAmt = getValueDouble("rept.line_of_credit_amt",inti);
    if (outCreditAmt <0) 
       {
        outCreditAmt =0;
        continue;
       }

    if (getValue("rept.id_p_seqno",inti).length()!=0)
       {
        setValue("idno.id_p_seqno",getValue("rept.id_p_seqno",inti));
        cnt1 = getLoadData("idno.id_p_seqno");
        if (cnt1==0)
           {
            showLogMessage("I","","id_p_seqno[" + getValue("rept.id_p_seqno",inti) +"]["+ getValue("rept.id_p_seqno").length() +"]" );
            showLogMessage("I","","   p_seqno[" + getValue("rept.p_seqno",inti) +"] not found" );
            continue;
           }
        idnoCode = getValue("idno.id_no");
        idnoTYpe = "P";
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
        idnoCode = getValue("corp.corp_no");
        idnoTYpe = "B";
       }

    buf1  =  ";"  
           + fixLeft(idnoCode , 11 ) +";";
    if (outCreditAmt>0)
       buf1 = buf1 + fixRight(String.format("+%,.0f", outCreditAmt) , 17 );
    else
       buf1 = buf1 + fixRight(String.format("%,.0f", outCreditAmt) , 17 );

    buf1 = buf1 + ";" + idnoTYpe + getValue("rept.stage_type",inti);

    buf2  =  ";"  
           + fixLeft(idnoCode.substring(0,2)+"****"+idnoCode.substring(6) , 11 ) +";";

    if (getValueDouble("rept.used_credit_amt",inti)>0)
       buf2 = buf2 + fixRight(String.format("+%,.0f", outCreditAmt) , 17 );
    else
       buf2 = buf2 + fixRight(String.format("%,.0f", outCreditAmt) , 17 );
    buf2 = buf2 + ";" + idnoTYpe + getValue("rept.stage_type",inti);

    if (getValue("rept.acct_type",inti).equals("06"))
       {
        intk[0]++;
        buf1  =  fixRight(String.format("%08d",intk[0]) , 8 ) + buf1;  
        writeTextFile(fo1,buf1+newLine);
        intk[1]++;
        buf2  =  fixRight(String.format("%08d",intk[1]) , 8 ) + buf2;  
        writeTextFile(fo2,buf2+newLine);
       }
    else if (getValue("rept.stage_flag",inti).equals("1"))
       {
        if (getValue("rept.card_flag",inti).equals("1"))
           {
            intk[2]++;
            buf1  =  fixRight(String.format("%08d",intk[2]) , 8 ) + buf1;  
            writeTextFile(fo3,buf1+newLine);
            intk[3]++;
            buf2  =  fixRight(String.format("%08d",intk[3]) , 8 ) + buf2;  
            writeTextFile(fo4,buf2+newLine);
           }
         else
           {
            intk[0]++;
            buf1  =  fixRight(String.format("%08d",intk[0]) , 8 ) + buf1;  
            writeTextFile(fo1,buf1+newLine);
            intk[1]++;
            buf2  =  fixRight(String.format("%08d",intk[1]) , 8 ) + buf2;  
            writeTextFile(fo2,buf2+newLine);
           }
       }
   }                   
  closeOutputText(fo1);
  closeOutputText(fo2);
  closeOutputText(fo3);
  closeOutputText(fo4);
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM210_BCARD1_" + sysDate + ".TXT";
  copyFileUsingStream(fileNameB1,fileName4); 
  fileName4 = localDir+"/CRM210_BCARD2_" + sysDate + ".TXT";
  copyFileUsingStream(fileNameB2,fileName4); 
  fileName4 = localDir+"/CRM210_PCARD1_" + sysDate + ".TXT";
  copyFileUsingStream(fileNameP1,fileName4); 
  fileName4 = localDir+"/CRM210_PCARD2_" + sysDate + ".TXT";
  copyFileUsingStream(fileNameP2,fileName4); 

  return(0);
 }
// ************************************************************************
 void loadCrdCorp() throws Exception // 前置調解
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
 void loadCrdIdno() throws Exception 
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


