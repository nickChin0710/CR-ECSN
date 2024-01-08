/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/16  V1.00.08  Allen Ho   Initial                                    *
*                                                                            *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD260 extends AccessDAO
{
 private  String PROGNAME = "年度信用卡呆帳恢復動用率報表理程式 112/06/16 V1.00.08";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String businessDate    = "";
 String chiDate         = "";

 String rptId_r1  = "";
 String rptName1  = "";
 int    rptSeq1   = 0;
 String buf = "",tmp="";
 int pageCnt=0,lineCnt=0;

 String localDir   = "";
 String fileName1  = "";
 String fileName4  = "";
 int fo1;
 String   newLine="\n";
 long   totCnt=0,perCnt=0,comCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD260 proc = new ColD260();
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
  
   if ( args.length >= 1 ) 
      { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if ((businessDate.equals(comm.lastdateOfmonth(businessDate)))&&
       (Arrays.asList("12").contains(businessDate.substring(4,6))))
      { 
       showLogMessage("I","","本日["+ businessDate + "]為年度信用卡呆帳恢復動用率報表");
      }   
   else if ((args.length == 2 ) && (args[1].equals("TEST")) )
      { 
       showLogMessage("I","","本日["+ businessDate + "]為年度信用卡呆帳恢復動用率報表(測試模式)");
      }   
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }
   deleteColIdleResume();                                                       
   deleteColRtnRate();                                                       
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadBilBill();
   loadCrdCard();
   loadActAcno();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  新增當年度 col_idle_resume");
   selectActAcno();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  更新上年度(" + comm.lastMonth(businessDate.substring(0,4)+"01")+") col_idle_resume");
   selectColIdleResume();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","  產生 CRM207 報表");
   deletePtrBatchRpt();
   selectColRtnRateR1();
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
 void selectActAcno() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acno_flag,"
            + "acct_type,"
            + "id_p_seqno,"
            + "corp_p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag in ('1','2') ";

  openCursor();

  int rCnt1=0,rCnt2=0;
  String idleFlag="";

  while( fetchTable() )
    {
     totCnt++;

     if (getValue("acno_flag").equals("1"))
        {
         perCnt++; 
         setValue("card.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("card.p_seqno");
         if (rCnt1==0) continue;   // 無流通卡
      
         setValue("bill.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("bill.p_seqno");
         if (rCnt1!=0)
            if (getValueDouble("bill.dest_amt")>0) continue;   // 非呆卡
        }
     else
        {
         comCnt++; 
         setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("acno.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");

         idleFlag ="";
         for (int int1=0;int1<rCnt1;int1++)
           {
            setValue("card.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("card.p_seqno");
            if (rCnt2==0) continue;   // 無流通卡

            idleFlag="1";
            setValue("bill.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("bill.p_seqno");
            if (rCnt2==0) continue;        // 呆卡
            else if (rCnt1!=0)
               if (getValueDouble("bill.dest_amt")>0)  // 非呆卡
                  {
                   idleFlag="2";
                   break;
                  }
           }
         if (idleFlag.length()==0) continue;  // 無流通卡
         if (idleFlag.equals("2")) continue;  // 非呆卡 
        }
     insertColIdleResume();
    }
  showLogMessage("I","","total   cnt :" + totCnt);
  showLogMessage("I","","person  cnt :" + perCnt);
  showLogMessage("I","","compant cnt :" + comCnt);

  closeCursor();
 }
// ************************************************************************
 void insertColIdleResume() throws Exception
 {
  extendField = "idle.";
  setValue("idle.proc_month"         , businessDate.substring(0,6));
  setValue("idle.p_seqno"            , getValue("p_seqno"));
  setValue("idle.acno_flag"          , getValue("acno_flag"));
  setValue("idle.acct_type"          , getValue("acct_type"));
  setValue("idle.id_p_seqno"         , getValue("id_p_seqno"));
  setValue("idle.corp_p_seqno"       , getValue("corp_p_seqno"));
  setValue("idle.mod_time"           , sysDate+sysTime);
  setValue("idle.mod_pgm"            , javaProgram);

  daoTable = "col_idle_resume";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_idle_resume error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 void selectColIdleResume() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acno_flag,"
            + "acct_type,"
            + "corp_p_seqno,"
            + "rowid as rowid";
  daoTable  = "col_idle_resume";
  whereStr  = "where proc_month like ? ";

  setString(1 , comm.lastdateOfmonth(businessDate).substring(0,4)+"%");

  openCursor();

  int rCnt1=0,rCnt2=0;
  String idleFlag="";
  totCnt=perCnt=0;
  while( fetchTable() )
    {
     totCnt++;

     if (getValue("acno_flag").equals("1"))
        {
         setValue("card.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("card.p_seqno");
         if (rCnt1==0) 
            {
             setValue("idle.resume_flag"  , "1");
             setValue("idle.resume_date"  , "");
             updateColIdleResume();
             continue;   // 無流通卡
            }
      
         setValue("bill.p_seqno",getValue("p_seqno"));
         rCnt1 = getLoadData("bill.p_seqno");
         if (rCnt1==0) continue;   // 呆卡
         if (rCnt1!=0)             // 非呆卡
            if (getValueDouble("bill.dest_amt")>0) 
               {
                setValue("idle.resume_flag"  , "Y");
                setValue("idle.resume_date"  , getValue("bill.purchase_date"));
               }
        }
     else
        {
         setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
         setValue("acno.acct_type"   , getValue("acct_type"));
         rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");

         idleFlag ="";
         for (int int1=0;int1<rCnt1;int1++)
           {
            setValue("card.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("card.p_seqno");
            if (rCnt2==0) continue;   // 無流通卡

            idleFlag="1";

            setValue("bill.p_seqno",getValue("acno.acno_p_seqno",int1));
            rCnt2 = getLoadData("bill.p_seqno");
            if (rCnt2!=0)             // 非呆卡
               if (getValueDouble("bill.dest_amt")>0) 
               {
                idleFlag="2";
                setValue("idle.resume_flag"  , "Y");
                setValue("idle.resume_date"  , getValue("bill.purchase_date"));
                break;
               }
           }
         if (idleFlag.length()==0) 
            {
             setValue("idle.resume_flag"  , "1");
             setValue("idle.resume_date"  , "");
             updateColIdleResume();
             continue;   // 無流通卡
            }
         if (idleFlag.equals("1")) continue;
        }
     perCnt++;
     updateColIdleResume();
    }
  if (totCnt>0)
     {
      setValueDouble("rate.in_stop_cnt"        , totCnt);
      setValueDouble("rate.in_active_cnt"      , perCnt);
      insertColRtnRate();
     }

  showLogMessage("I","","total   cnt :" + totCnt);
  showLogMessage("I","","resume  cnt :" + perCnt);

  closeCursor();
 }
// ***********************************************************************
 int deleteColIdleResume() throws Exception
 {
  daoTable  = "col_idle_resume";
  whereStr  = "where proc_month like  ? ";

  setString(1 , businessDate.substring(0,4)+"%");

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_idle_resume[" + businessDate.substring(0,6) + "] cnt :["+ recCnt +"]");
  return(0);
 }
// ************************************************************************
 int deleteColRtnRate() throws Exception
 {
  daoTable  = "col_rtn_rate";
  whereStr  = "where in_year = ? ";

  setString(1 , businessDate.substring(0,4));

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_rtn_rate[" + businessDate.substring(0,4) + "] cnt :["+ recCnt +"]");
  return(0);
 }
// ************************************************************************
 void loadActAcno() throws Exception // 公司戶
 {
  extendField = "acno.";
  selectSQL = "corp_p_seqno,"
            + "acct_type,"
            + "acno_p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag in ('3','Y') "
            + "and payment_rate1 != '' "  
            + "order by corp_p_seqno,acct_type ";

  int  n = loadTable();
  setLoadData("acno.corp_p_seqno,acno.acct_type");
  showLogMessage("I","","Load act_acno  Count: ["+n+"]");
 }
// ************************************************************************
 void loadBilBill() throws Exception 
 {
  extendField = "bill.";
  selectSQL = "p_seqno,"
            + "min(purchase_date) as purchase_date,"
            + "sum(decode(sign_flag,'-',dest_amt*-1,dest_amt)) as dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where rsk_type not in ('1','2','3') "
            + "and   nvl(acct_code,'') in ('BL','ID','IT','AO','OT','CA')  "
            + "and   dest_amt  != 0 "
            + "and   purchase_date between ? and ? "
            + "group by p_seqno ";

  setString(1 , comm.lastMonth(businessDate,5)+"01");
  setString(2 , businessDate);

  showLogMessage("I","","purchase_date ["+ comm.lastMonth(businessDate,5)+"01" + "] - ["+ businessDate +"]");

  int  n = loadTable();
  setLoadData("bill.p_seqno");
  showLogMessage("I","","Load bil_bill1  Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCard() throws Exception // 判斷強停卡
 {
  extendField = "card.";
  selectSQL = "distinct p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' ";

  int  n = loadTable();
  setLoadData("card.p_seqno");
  showLogMessage("I","","Load crd_card  Count: ["+n+"]");
 }
// ************************************************************************
 int updateColIdleResume() throws Exception
 {
  daoTable  = "col_idle_resume";
  updateSQL = "resume_flag = ?,"
            + "resume_date = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";
  whereStr  = "where rowid  = ? "
            ;

  setString(1 , getValue("idle.resume_flag"));
  setString(2 , getValue("idle.resume_date")); 
  setString(3 , javaProgram);
  setRowId(4 , getValue("rowid"));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 void insertColRtnRate() throws Exception
 {
  extendField = "rate.";
  setValueDouble("rate.in_percent"   , Math.round(getValueDouble("rate.in_active_cnt")*10000.0
                                     / getValueDouble("rate.in_stop_cnt"))/100.0);
  setValue("rate.in_year"            , businessDate.substring(0,4));
  setValue("rate.crt_date"           , sysDate);
  setValue("rate.mod_user"           , javaProgram);
  setValue("rate.mod_time"           , sysDate+sysTime);
  setValue("rate.mod_pgm"            , javaProgram);

  daoTable = "col_rtn_rate";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_idle_resume error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 int  selectColRtnRateR1() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  fileName1 = localDir+"/CRM207_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }

  selectSQL = "in_year,"
            + "in_stop_cnt," 
            + "in_active_cnt," 
            + "in_percent";
  daoTable  = "col_rtn_rate";    
  whereStr  = "where in_year >= ? "
            + "order by 1";

  setString( 1 , comm.lastMonth(businessDate,36).substring(0,4)); 

  openCursor();

  headFileR1();

  double debitCntA=0,debitCntB=0;
  buf  = "呆卡恢復動用率"; 
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  while( fetchTable() )
    {
     debitCntA = debitCntA + getValueDouble("in_stop_cnt");
     debitCntB = debitCntB + getValueDouble("in_active_cnt");

     buf  = String.format("%03d",Integer.valueOf(getValue("in_year").substring(0,4))-1911)+"年度";
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
                                                                                                                                                                                                                                                            
     buf  = fixLeft("年初呆卡戶數（Ａ）：" , 34  )
          + fixRight(String.format("%,.0f",getValueDouble("in_stop_cnt")) , 24 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
                                                                                                                                                                                                                                                            
     buf  = fixLeft("年初呆卡至年底恢復動用戶數（Ｂ）：" , 34  )
          + fixRight(String.format("%,.0f",getValueDouble("in_active_cnt")), 24 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
                                                                                                                                                                                                                                                            
     buf  = fixLeft("恢復動用率（Ｃ＝Ａ／Ｂ）：" , 34  )
          + fixRight(String.format("%,.2f",getValueDouble("in_percent"))+"%" , 25 );
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);

     buf  = ""; 
     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
    }

  buf  = ""; 
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = fixLeft("三年平均率" , 34  )
       + fixRight(String.format("%,20.2f",(debitCntB/debitCntA)*100.0)+"%" , 25 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  closeCursor();
  closeOutputText(fo1);
  localDir  = "/crdatacrea/CREDITCARD";
  fileName4 = localDir+"/CRM207_" + sysDate + ".TXT";
  copyFileUsingStream(fileName1,fileName4); 
  return(0);
 }
// ************************************************************************
 void headFileR1() throws Exception 
 {
  String temp = "";
  rptId_r1  = "ColD260R1";
  rptName1  = "信用卡呆卡恢復動用率報表";

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
  buf = comcr.insertStr(buf, "報表代號: CRM207    科目代號:"            ,  1);
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
 int deletePtrBatchRpt() throws Exception
 {
  daoTable  = "ptr_batch_rpt";
  whereStr  = "where program_code like 'ColD260%' "
            + "and   start_date = ? ";

  setString(1 , sysDate);

  int recCnt = deleteTable();

  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
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

