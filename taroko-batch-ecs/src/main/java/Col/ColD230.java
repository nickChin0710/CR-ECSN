/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/03/28  V1.00.03  Allen Ho   Initial                                    *
* 112/07/28  V1.00.04  Sunny      調整loadColCpbdue3條件                     *
* 112/11/03  V1.00.07  Allen Ho   Redefine report Item                       *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD230 extends AccessDAO
{
 private  String PROGNAME = "每季應收帳款帳面金額變動(預估信用損失)計算程式 112/11/03  V1.00.07";
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
  ColD230 proc = new ColD230();
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
   deleteColAcctStage();                                                       
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadActAcno();
   loadColAcctStage();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  新增 col_acct_stage");
   selectColIfrsBase();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  寫入媒體 CRM206");
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
            + "(cap_end_bal - op_end_bal + int_end_bal + "
            + " fee_end_bal + unpost_end_bal) as now_end_amt";  
  daoTable  = "col_ifrs_base";
  whereStr  = "where proc_month = ? "
            + " and (cap_end_bal - op_end_bal + int_end_bal + "
            + "      fee_end_bal + unpost_end_bal) > 0 "
            ;

  setString( 1 , businessDate.substring(0,6));

  openCursor();

  while( fetchTable() )
    {
     totCnt++;
     setValue("casn.from_stage"        , "00");
     setValue("casn.beg_end_amt"       , "0");

     setValue("caso.p_seqno",getValue("p_seqno"));
     rCnt1 = getLoadData("caso.p_seqno");
     if (rCnt1!=0)
         setValue("casn.from_stage"  , getValue("caso.stage_type")+getValue("caso.stage_flag"));

     if (getValue("card_flag").equals("1"))
        {
         if (rCnt1!=0)
            setValue("casn.beg_end_amt" , getValue("caso.now_end_amt"));
        }
     else
        {
         if (rCnt1==0) continue;
            
         setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("acno.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");

         for (int int1=0;int1<rCnt1;int1++)
           {
            setValue("caso.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("caso.p_seqno");
            if (rCnt2!=0) 
                setValueDouble("casn.beg_end_amt" , getValueDouble("casn.beg_end_amt")
                                                  + getValueDouble("caso.now_end_amt"));
           }
        }

     insertColAcctStage();
    }
  showLogMessage("I","","total   cnt :" + totCnt);

  closeCursor();
 }
// ************************************************************************
 void insertColAcctStage() throws Exception
 {
  extendField = "casn.";
  setValue("casn.proc_month"         , businessDate.substring(0,6));
  setValue("casn.p_seqno"            , getValue("p_seqno"));
  setValue("casn.acct_type"          , getValue("acct_type")); 
  setValue("casn.id_p_seqno"         , getValue("id_p_seqno")); 
  setValue("casn.corp_p_seqno"       , getValue("corp_p_seqno")); 
  setValue("casn.stage_type"         , getValue("stage_type")); 
  setValue("casn.stage_flag"         , getValue("stage_flag")); 
  setValue("casn.card_flag"          , getValue("card_flag")); 
  setValue("casn.now_end_amt"        , getValue("now_end_amt")); 
  setValue("casn.mod_time"           , sysDate+sysTime);
  setValue("casn.mod_pgm"            , javaProgram);

  daoTable = "col_acct_stage";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_acct_stage error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 int deleteColAcctStage() throws Exception
 {
  daoTable  = "col_acct_stage";
  whereStr  = "where proc_month = ? ";

  setString(1 , businessDate.substring(0,6));

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_acct_stage[" + businessDate.substring(0,6) + " cnt :["+ recCnt +"]");
  return(0);
 }
// ************************************************************************
 void  loadColAcctStage() throws Exception
 {
  extendField = "caso.";
  selectSQL = "p_seqno,"
            + "stage_type,"
            + "stage_flag,"
            + "now_end_amt";
  daoTable  = "col_acct_stage";    
  whereStr  = "where proc_month   = ? ";

  setString(1 , String.format("%04d12",Integer.valueOf(businessDate.substring(0,4))-1));

  int  n = loadTable();
  setLoadData("caso.p_seqno");
  showLogMessage("I","","Load col_acct_stage Count: ["+n+"]");
 }
// ************************************************************************
 int writeFileCRM206() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRM206_" + sysDate + "_1.TXT";

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
  selectColAcctStage();

  headFileR("(個人卡)");
  writeReport();

  for (int inti=0;inti<10;inti++) penCreditAmt[inti] = copCreditAmt[inti];
  headFileR("(法人卡)");
  writeReport();
    
  closeOutputText(fo1);
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM206_" + sysDate + "_1.TXT";
  copyFileUsingStream(fileName1,fileName4);
  return(0);
 }
// ************************************************************************
 void headFileR(String headItem) throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD230R1";
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

  buf = "金額資產轉為存續期間預期信用減損資產";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 1 變動至Stage 2";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    全清變動至Stage 2";
  stageTypeDesc[1] = "    繳足最低變動至Stage 2";
  stageTypeDesc[2] = "    逾期１－３０天變動至Stage 2";
  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;

  for (int inti=0;inti<3;inti++)
    {
     if (penCreditAmt[inti] !=0)
        revCreditAmt          = penCreditAmt[inti] *-1;
     else
        revCreditAmt          = 0;

     buf  = fixLeft(stageTypeDesc[inti-0]  , 32 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%d"   , 0                ) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
    }
  if (sumCreditAmt[0]!=0)
     sumCreditAmt[1] = sumCreditAmt[0] * -1;
  else
     sumCreditAmt[1] = 0;

  buf  = fixLeft("小計（Ｂ）"   , 32 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
          + fixRight(String.format("%d", 0)              , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "金額資產轉為信用減損資產";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 1 變動至Stage 3";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    全清變動至Stage 3";
  stageTypeDesc[1] = "    繳足最低變動至Stage 3";
  stageTypeDesc[2] = "    逾期１－３０天變動至Stage 3";
  sumCreditAmt[0] = 0;
  for (int inti=3;inti<6;inti++)
    {
     if (penCreditAmt[inti] !=0)
        revCreditAmt          = penCreditAmt[inti] *-1;
     else
        revCreditAmt          = 0;

     buf  = fixLeft(stageTypeDesc[inti-3]  , 32 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 )
          + fixRight(String.format("%d", 0     )               , 26 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti]*-1;
     sumCreditAmt[2] = sumCreditAmt[2] + penCreditAmt[inti];
    }

  buf = "Stage 2 變動至Stage 3";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期３１－６０天動至Stage 3";
  stageTypeDesc[1] = "    逾期６１－９０天動至Stage 3";
  for (int inti=6;inti<8;inti++)
    {
     if (penCreditAmt[inti] !=0)
        revCreditAmt          = penCreditAmt[inti] *-1;
     else
        revCreditAmt          = 0;

     buf  = fixLeft(stageTypeDesc[inti-6]  , 32 )
          + fixRight(String.format("%d", 0)                    , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[1] = sumCreditAmt[1] + penCreditAmt[inti]*-1;
     sumCreditAmt[2] = sumCreditAmt[2] + penCreditAmt[inti];
    }

  buf  = fixLeft("小計（Ｃ）"   , 32 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[2]) , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) sumCreditAmt[inti] = 0;
  buf = "金額資產轉為１２個月預期信用減損資產";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 2 變動至 Stage 1";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  stageTypeDesc[0] = "    逾期３１－６０天動至Stage 1";
  stageTypeDesc[1] = "    逾期６１－９０天動至Stage 1";
  sumCreditAmt[0] = 0;
  for (int inti=8;inti<10;inti++)
    {
     if (penCreditAmt[inti] !=0)
        revCreditAmt          = penCreditAmt[inti] *-1;
     else
        revCreditAmt          = 0;

     buf  = fixLeft(stageTypeDesc[inti-8]  , 32 )
          + fixRight(String.format("%,.0f",penCreditAmt[inti]) , 26 )
          + fixRight(String.format("%,.0f",revCreditAmt      ) , 26 )
          + fixRight(String.format("%,.0f", 0.0)               , 26 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     sumCreditAmt[0] = sumCreditAmt[0] + penCreditAmt[inti];
    }
  if (sumCreditAmt[0]!=0)
     sumCreditAmt[1] = sumCreditAmt[0] * -1;
  else
     sumCreditAmt[1] = 0;
  buf  = fixLeft("小計（Ｄ）"   , 32 )
       + fixRight(String.format("%,.0f",sumCreditAmt[0]) , 26 )
       + fixRight(String.format("%,.0f",sumCreditAmt[1]) , 26 )
          + fixRight(String.format("%,.0f", 0.0)         , 26 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "備註";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 1 : 全清，繳足最低應繳，逾期１－３０天";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 2 : 逾期３１天－９０天";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "Stage 3 : 逾期９１天(含)以上，債務協商";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  buf = "除列金額(收回款項)係指全體持卡人每季TX20~27 CODE加總金額，並以季底持卡人狀態分類每季Stage 1-3。";
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
 int selectColAcctStage() throws Exception
 {
  selectSQL = "from_stage||stage_type as stage_order,"
            + "card_flag,"
            + "sum(now_end_amt) as now_end_amt ";
  daoTable  = "col_acct_stage";    
  whereStr  = "where proc_month = ? "
            + "and now_end_amt !=0 "
            + "and from_stage||stage_type in "
            + "    ('112','122','132','113','123','133','213','223','211','221') "
            + "group by from_stage||stage_type,card_flag "
            + "order by from_stage||stage_type,card_flag "
            ;

  setString(1 , businessDate.substring(0,6));

  int recCnt = selectTable();
  showLogMessage("I","","total   cnt :" + recCnt);

  String[] stageRule = {"112","122","132","113","123","133","213","223","211","221"};

  for (int inti=0;inti<recCnt;inti++)
    {
     for (int intk=0;intk<10;intk++)
        {

//showLogMessage("I","","card_flag["+ getValue("card_flag",inti) +"] stage oreder[" + getValue("stage_order",inti) +"]-[" + stageRule[intk] +"] = [" + getValueDouble("now_end_amt",inti)+ "]");

         if (getValue("stage_order",inti).equals(stageRule[intk]))
            {
             if (getValue("card_flag",inti).equals("1"))
                penCreditAmt[intk] = penCreditAmt[intk] + getValueDouble("now_end_amt",inti);
             else
               {
                copCreditAmt[intk] = copCreditAmt[intk] + getValueDouble("now_end_amt",inti);
               }

             break;
            }
        }
    }
  return(0);
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

