/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/11/03  V1.00.03  Allen Ho   當年度新增消費/繳款回收報表 CRM206-2       *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD240 extends AccessDAO
{
 private  String PROGNAME = "當年度新增消費/繳款回收報表(CRM206-2)程式 112/11/03 V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String businessDate    = "";
 String chiDate         = "";
 long   totCnt=0;

 String rptId_r1  = "";
 String rptName1  = "";
 int    rptSeq1   = 0;
 String buf = "",tmp="";
 int pageCnt=1,lineCnt=0;
 int intAi=0,intKi=0;

 String newLine="\n";
 String localDir   = "";
 String fileName1  = "";
 String fileName4  = "";
 int fo1,rCnt1,rCnt2;
 double[] penCreditAmt  = new double[20];
 double[] copCreditAmt  = new double[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD240 proc = new ColD240();
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
   comcr = new CommCrdRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if (businessDate.equals(comm.lastdateOfmonth(businessDate)))
      { 
       showLogMessage("I","","本日["+ businessDate + "]為每季信用卡消費帳款繳款狀況統計表");
      }   
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }
   deleteColAcctChg();                                                       
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadActAcno();
   loadBilBill();
   loadCycPyaj();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  新增 col_acct_chg");
   selectColIfrsBase();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  寫入媒體 CRM206-2");
   writeFileCRM206();
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
 void selectColIfrsBase() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "id_p_seqno,"
            + "corp_p_seqno,"
            + "stage_type,"
            + "stage_flag,"
            + "card_flag,"
            + "nego_flag";
  daoTable  = "col_ifrs_base";
  whereStr  = "where proc_month = ? "
            + "and   nego_flag = '' ";


  setString( 1 , businessDate.substring(0,6));

  openCursor();

  double sum_dest_amt = 0;
  double sum_payment_amt = 0;

  while( fetchTable() )
    {
     totCnt++;
     setValue("cacg.sum_dest_amt"      , "0");
     setValue("cacg.sum_payment_amt"   , "0");

     if (getValue("card_flag").equals("1"))
        {
         setValue("bill.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("bill.p_seqno");
         if (rCnt1!=0)
             setValue("cacg.sum_dest_amt"    , getValue("bill.dest_amt"));

         setValue("pyaj.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("pyaj.p_seqno");
         if (rCnt1!=0)
             setValue("cacg.sum_payment_amt" , getValue("pyaj.payment_amt"));
        }
     else
        {
         setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("acno.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");

         for (int int1=0;int1<rCnt1;int1++)
           {
            setValue("bill.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("bill.p_seqno");
            if (rCnt2!=0)
                setValueDouble("cacg.sum_dest_amt"    , getValueDouble("cacg.sum_dest_amt")
                                                      + getValueDouble("bill.dest_amt"));

            setValue("pyaj.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("pyaj.p_seqno");
            if (rCnt2!=0)
                setValueDouble("cacg.sum_payment_amt" , getValueDouble("cacg.sum_payment_amt")
                                                      + getValueDouble("pyaj.payment_amt"));
           }
        }
     insertColAcctChg();
    }
  showLogMessage("I","","total   cnt :" + totCnt);

  closeCursor();
 }
// ************************************************************************
 void insertColAcctChg() throws Exception
 {
  extendField = "cacg.";
  setValue("cacg.proc_month"         , businessDate.substring(0,6));
  setValue("cacg.p_seqno"            , getValue("p_seqno"));
  setValue("cacg.acct_type"          , getValue("acct_type")); 
  setValue("cacg.id_p_seqno"         , getValue("id_p_seqno")); 
  setValue("cacg.corp_p_seqno"       , getValue("corp_p_seqno")); 
  setValue("cacg.stage_type"         , getValue("stage_type")); 
  setValue("cacg.stage_flag"         , getValue("stage_flag")); 
  setValue("cacg.card_flag"          , getValue("card_flag")); 
  setValue("cacg.nego_flag"          , getValue("nego_flag")); 
  setValue("cacg.mod_time"           , sysDate+sysTime);
  setValue("cacg.mod_pgm"            , javaProgram);

  daoTable = "col_acct_chg";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_acct_chg error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 int deleteColAcctChg() throws Exception
 {
  daoTable  = "col_acct_chg";
  whereStr  = "where proc_month = ? ";

  setString(1 , businessDate.substring(0,6));

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_acct_chg[" + businessDate.substring(0,6) + " cnt :["+ recCnt +"]");
  return(0);
 }
// ************************************************************************
 int writeFileCRM206() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRM206_" + sysDate + "_2.TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }
  for (int inti=0;inti<10;inti++) 
      {
       penCreditAmt[inti] = 0;
       copCreditAmt[inti] = 0;
      }
  selectColAcctChg();
  headFileR();
  writeReport();

  closeOutputText(fo1);
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM206_" + sysDate + "_2.TXT";
  copyFileUsingStream(fileName1,fileName4);
  return(0);
 }
// ************************************************************************
 void headFileR(String headItem) throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD240R1";
  rptName1  = "每季應收帳款帳面金額變動表(預估信用損失)";

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""           + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM206    科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "   中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: " + pageCnt++                   ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "            項目                      １２個月預期信用損失      存續期間預期信用損失      存續期間預期信用損失";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = headItem + "                                    Stage 1                   Stage 2                    Stage 3 ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "================================================================================================================== ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  lineCnt = 6;
 }
/***********************************************************************/
 int writeReport() throws Exception
 {

  double   revCreditAmt  = 0;
  double[] sumCreditAmt  = new double[3];
  String[] stageTypeDesc= new String[3];

  buf = "Stage 1";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    全清-信用卡";
  stageTypeDesc[1] = "    繳足最低(不含債協)";
  stageTypeDesc[2] = "    逾期天數１－３０天(不含債協)";
  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;

  for (int inti=0;inti<3;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-0]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];

     if (inti==0)
        {
         selectDbbBill();
         buf  = fixLeft("    全清-VD卡"  , 34 )
              + fixRight(String.format("%,.0f",getValueDouble("dbil.dest_amt")) , 26 )
              + fixRight(String.format("%,.0f",0.0)                , 26 )
              + fixRight(String.format("%,.0f",getValueDouble("dbil.dest_amt")) , 26 );
         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
         writeTextFile(fo1,buf+newLine);
         sumCreditAmt[0] = sumCreditAmt[0] + getValueDouble("dbil.dest_amt");
         sumCreditAmt[1] = sumCreditAmt[1] + 0.0;
         sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];

         buf  = fixLeft("全清總計"   , 34 )
              + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
              + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
              + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
         writeTextFile(fo1,buf+newLine);
        }
    }

  buf  = fixLeft("小    計"   , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "Stage 2";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期天數３１－６０天(不含債協)";
  stageTypeDesc[1] = "    逾期天數６１－９０天(不含債協)";
  sumCreditAmt[0] = 0;
  for (int inti=3;inti<5;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-3]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];
    }
  buf  = fixLeft("小    計"   , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "Stage 3";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期天數９１－１８０(不含債協)";
  stageTypeDesc[1] = "    逾期天數１８１(含)以上(不含債協)";
  stageTypeDesc[2] = "    債務協商";
  sumCreditAmt[0] = 0;
  for (int inti=5;inti<8;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-5]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];
    }
  buf  = fixLeft("小    計"  , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "除列金額(回收款項)";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 1";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    全清-信用卡";
  stageTypeDesc[1] = "    繳足最低(不含債協)";
  stageTypeDesc[2] = "    逾期天數１－３０天(不含債協)";
  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;

  for (int inti=8;inti<11;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-8]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];

     if (inti==8)
        {
         selectDbaJrnl();
         buf  = fixLeft("    全清-VD卡"  , 34 )
              + fixRight(String.format("%,.0f",getValueDouble("jrnl.transaction_amt")) , 26 )
              + fixRight(String.format("%,.0f",0.0)                , 26 )
              + fixRight(String.format("%,.0f",getValueDouble("jrnl.transaction_amt")) , 26 );
         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
         writeTextFile(fo1,buf+newLine);

         sumCreditAmt[0] = sumCreditAmt[0] + getValueDouble("jrnl.transaction_amt");
         sumCreditAmt[1] = sumCreditAmt[1] + 0.0;
         sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];

         buf  = fixLeft("全清總計"   , 34 )
              + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
              + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
              + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
         insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
         writeTextFile(fo1,buf+newLine);
        }
    }
  buf  = fixLeft("小    計"   , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "Stage 2";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期天數３１－６０天(不含債協)";
  stageTypeDesc[1] = "    逾期天數６１－９０天(不含債協)";
  sumCreditAmt[0] = 0;
  for (int inti=11;inti<13;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-11]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];
    }

  buf  = fixLeft("小    計"   , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "Stage 3";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期天數９１－１８０(不含債協)";
  stageTypeDesc[1] = "    逾期天數１８１(含)以上(不含債協)";
  stageTypeDesc[2] = "    債務協商";
  sumCreditAmt[0] = 0;
  for (int inti=13;inti<16;inti++)
    {
     revCreditAmt = penCreditAmt[inti] + copCreditAmt[inti];

     buf  = fixLeft(stageTypeDesc[inti-13]  , 34 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",copCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
     sumCreditAmt[1] = sumCreditAmt[1] + copCreditAmt[inti];
     sumCreditAmt[2] = sumCreditAmt[0] + sumCreditAmt[1];
    }
  buf  = fixLeft("小    計"  , 34 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
//insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
//writeTextFile(fo1,buf+newLine);

  buf = " 製表單位：資訊處                            經辦：                               核章： ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "##PPP";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", "\f");
  writeTextFile(fo1,buf+newLine);

  return(0);
 }
// ************************************************************************
 void headFileR() throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD240R1";
  rptName1  = "每季應收帳款帳面金額變動表";

//buf = "";
//insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
//writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM206    科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "   中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: " + pageCnt++                   ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "                                                個人卡                    法人卡                     合  計";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "新增消費款                                      金  額                    金  額                     金  額 ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "================================================================================================================== ";
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
  noTrim= "N";

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
  whereStr  = "where program_code like 'ColD240%' "
            + "and   start_date = ? ";

  setString(1 , sysDate);

  int recCnt = deleteTable();

  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

  return(0);
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
 void loadBilBill() throws Exception
 {
  extendField = "bill.";
  selectSQL = "p_seqno,"
            + "sum(decode(sign_flag,'-',dest_amt*-1,dest_amt)) as dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where rsk_type not in ('1','2','3') "
            + "and   (acct_code in ('BL','ID','AO','OT','CA')  "
            + " or    (acct_code ='IT' "
            + "  and   install_curr_term = 1)) "
            + "and   dest_amt  != 0 "
            + "and   post_date like ? "
            + "group by p_seqno ";

  setString(1 , businessDate.substring(0,4)+"%");

  int  n = loadTable();
  setLoadData("bill.p_seqno");
  showLogMessage("I","","Load bil_bill  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCycPyaj() throws Exception
 {
  extendField = "pyaj.";
  selectSQL = "p_seqno,"
            + "sum(payment_amt) as payment_amt" ;
  daoTable  = "cyc_pyaj";
  whereStr  = "where class_code in ('P','B') "
            + "and   payment_type not in ('REFU','DUMY') "
            + "and   payment_date like ? "
            + "group by p_seqno ";

  setString(1 , businessDate.substring(0,4)+"%");

  int  n = loadTable();
  setLoadData("pyaj.p_seqno");
  showLogMessage("I","","Load cyc_pyaj  Count: ["+n+"]");
 }
// ************************************************************************
 int selectColAcctChg() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"   // MEGA
            + "decode(nego_flag,'',stage_type,'0') as stage_type,"
            + "decode(nego_flag,'',stage_flag,'1') as stage_flag,"
            + "card_flag,"
            + "sum_dest_amt,"
            + "sum_payment_amt";
  daoTable  = "col_acct_chg";    
  whereStr  = "where proc_month = ? "
            + "and   acct_status < '2' ";

  setString(1 , businessDate.substring(0,6));

  openCursor();

  totCnt=0;
  while( fetchTable() )
    {
     totCnt++;
     if (getValue("acct_type").equals("06")) setValue("card_flag","2"); 

     if (getValue("stage_type").equals("0"))
        {
         intAi=7;
         intKi=15;
        }
     else if (getValue("stage_type").equals("1"))
        {
         intAi=0;
         intKi=8;
        }
     else if (getValue("stage_type").equals("2"))
        {
         intAi=3;
         intKi=11;
        }
     else if (getValue("stage_type").equals("3"))
        {
         intAi=5;
         intKi=13;
        }

     if (getValue("card_flag").equals("1"))
        {
         penCreditAmt[intAi + getValueInt("stage_flag")-1]
            = penCreditAmt[intAi + getValueInt("stage_flag")-1]
            + getValueDouble("sum_dest_amt");

         penCreditAmt[intKi + getValueInt("stage_flag")-1]
            = penCreditAmt[intKi + getValueInt("stage_flag")-1]
            + getValueDouble("sum_payment_amt");
        }
     else
        {
         copCreditAmt[intAi + getValueInt("stage_flag")-1]
            = copCreditAmt[intAi + getValueInt("stage_flag")-1]
            + getValueDouble("sum_dest_amt");

         copCreditAmt[intKi + getValueInt("stage_flag")-1]
            = copCreditAmt[intKi + getValueInt("stage_flag")-1]
            + getValueDouble("sum_payment_amt");
        }
    }
   
  return(0);
 }
// ************************************************************************
 void selectDbbBill() throws Exception
 {
  extendField = "dbil.";
  selectSQL = "sum(dest_amt) as dest_amt" ;
  daoTable  = "dbb_bill";
  whereStr  = "where rsk_type not in ('1') "
            + "and   post_date like ? "        
            + "and   acct_code in ('BL','CA') ";

  setString(1 , businessDate.substring(0,4)+"%");

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select dbb_bill error!" );
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 void selectDbaJrnl() throws Exception
 {
  extendField = "jrnl.";
  selectSQL = "sum(transaction_amt) as transaction_amt" ;
  daoTable  = "dba_jrnl";
  whereStr  = "where acct_date like ? "        
            + "and   tran_class in ('D') ";

  setString(1 , businessDate.substring(0,4)+"%");

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select dba_jrnl error!" );
      exitProgram(1);
     }
  return;
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

}  // End of class FetchSample

