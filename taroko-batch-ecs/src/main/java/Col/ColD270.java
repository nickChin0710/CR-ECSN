/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/14  V1.00.08  Allen Ho   Initial                                    *
*                                                                            *
******************************************************************************/
package Col;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ColD270 extends AccessDAO
{
 private  String PROGNAME = "IFRS9 呆帳年度合計平均折現回收率計算處理程式 112/06/14 V1.00.08";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String businessDate    = "";
 double bad_amt=0,end_bal=0,new_add_amt,resume_amt=0;
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
 int[] loadCnt = new int[30];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ColD270 proc = new ColD270();
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
      { showLogMessage("I","","本日["+ businessDate + "]為IFRS9資料計算日期"); }
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }

   deletePtrBatchRpt();
   deleteColBadMResume();
   deleteColBadResume();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadActDebt();
   loadColBadDetail();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增當月轉呆 (col_bad_r_resume)");
   selectActAcno();  
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新非當月轉呆新增及收回 (col_bad_r_resume)");
   selectColBadMResume();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增轉呆統計 (col_bad_resume)");
   selectColBadMResume1();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","產生報表");
   selectColBadResumeR("1");
   selectColBadResumeR("2");
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
            + "acct_type,"
            + "corp_p_seqno,"
            + "status_change_date,"
            + "id_p_seqno,"
            + "acno_flag";
  daoTable  = "act_acno";
  whereStr  = "where acct_status ='4' "
            + "and   status_change_date like ? ";

  setString( 1 , businessDate.substring(0,6)+"%");
   
  openCursor();

  int rCnt1=0,rCnt2=0,yearCnt=0;

  while( fetchTable() )
    {
     totCnt++;

     setValue("badd.p_seqno",getValue("p_seqno"));
     rCnt1 = getLoadData("badd.p_seqno");
     if (rCnt1==0)
        {
         showLogMessage("I","","p_seqno ["+getValue("p_seqno") +"] col_bad_detail not found");
         continue;
        }

     setValueDouble("bamr.bad_amt" , getValueDouble("badd.new_add_amt"));
     insertColBadMResume();
    }
  showLogMessage("I","","total   cnt :" + totCnt);

  closeCursor();
 }
// ************************************************************************
 void selectColBadMResume() throws Exception
 {
  selectSQL = "p_seqno,"
            + "status_change_date,"
            + "new_add_amt_0,"
            + "new_add_amt_1,"
            + "new_add_amt_2,"
            + "end_bal_0,"
            + "end_bal_1,"
            + "end_bal_2,"
            + "resume_amt_0,"
            + "resume_amt_1,"
            + "resume_amt_2,"
            + "bad_amt";
  daoTable  = "col_bad_m_resume";
  whereStr  = "where status_change_date between ? and ?";

  setString( 1 , comm.lastMonth(businessDate,36).substring(0,4)+"0101");
  setString( 2 , comm.lastMonth(businessDate)+"31");
   
  openCursor();

  int rCnt1=0,rCnt2=0,yearCnt=0;

  while( fetchTable() )
    {
     totCnt++;

     yearCnt =0;
     if (getValue("status_change_date").substring(0,4).equals(comm.lastMonth(businessDate,36).substring(0,4)))
        yearCnt =3;
     else if (getValue("status_change_date").substring(0,4).equals(comm.lastMonth(businessDate,24).substring(0,4)))
        yearCnt =2;
     else if (getValue("status_change_date").substring(0,4).equals(comm.lastMonth(businessDate,12).substring(0,4)))
        yearCnt =1;
     // *******************************************************
     new_add_amt = 0;
     setValue("badd.p_seqno",getValue("p_seqno"));
     rCnt1 = getLoadData("badd.p_seqno");
     if (rCnt1!=0)
        {
         new_add_amt = getValueDouble("badd.new_add_amt");
         if (yearCnt==0)
             new_add_amt = new_add_amt - getValueDouble("bad_amt");
        }
     // ******************************************************* // 欠款餘額
     end_bal = 0;
     setValue("debt.p_seqno",getValue("p_seqno"));
     rCnt1 = getLoadData("debt.p_seqno");
     if (rCnt1!=0) 
        end_bal = getValueDouble("debt.end_bal");
     // *******************************************************
     if (yearCnt==0)
        {
         resume_amt  = getValueDouble("bad_amt") 
                     + new_add_amt - end_bal;
        }
     else if (yearCnt==1)
        {
         resume_amt  = getValueDouble("bad_amt") 
                     + getValueDouble("new_add_amt_0") - getValueDouble("resume_amt_0")  
                     + new_add_amt - end_bal;
        }
     else if (yearCnt==2)
        {
         resume_amt  = getValueDouble("bad_amt") 
                     + getValueDouble("new_add_amt_0") - getValueDouble("resume_amt_0")  
                     + getValueDouble("new_add_amt_1") - getValueDouble("resume_amt_1")  
                     + new_add_amt - end_bal;
        }
     else if (yearCnt==3)
        {
         resume_amt  = getValueDouble("bad_amt") 
                     + getValueDouble("new_add_amt_0") - getValueDouble("resume_amt_0")  
                     + getValueDouble("new_add_amt_1") - getValueDouble("resume_amt_1")  
                     + getValueDouble("new_add_amt_2") - getValueDouble("resume_amt_2")  
                     + new_add_amt - end_bal;
        }

     // *******************************************************  // 新增科目呆帳
     updateColBadMResume(yearCnt);
    }

  showLogMessage("I","","total   cnt :" + totCnt);

  closeCursor();
 }
// ************************************************************************
 void selectColBadMResume1() throws Exception
 {
  selectSQL = "substr(status_change_date,1,4) as proc_year,"
            + "decode(corp_p_seqno,'','1','2') as card_flag,"
            + "sum(new_add_amt_0 + new_add_amt_1) as new_add_amt_1,"
            + "sum(new_add_amt_2) as new_add_amt_2,"
            + "sum(new_add_amt_3) as new_add_amt_3,"
            + "sum(resume_amt_0 + resume_amt_1) as resume_amt_1,"
            + "sum(resume_amt_2) as resume_amt_2,"
            + "sum(resume_amt_3) as resume_amt_3,"
            + "sum(bad_amt+new_add_amt_0+new_add_amt_1+new_add_amt_2+new_add_amt_3) as bad_amt";
  daoTable  = "col_bad_m_resume";
  whereStr  = "where status_change_date between ? and ? "
            + "group by substr(status_change_date,1,4),decode(corp_p_seqno,'','1','2') ";

  setString( 1 , comm.lastMonth(businessDate,36).substring(0,4)+"0101");
  setString( 2 , comm.lastMonth(businessDate,12).substring(0,4)+"1231");
   
  openCursor();

  while( fetchTable() )
    {
     totCnt++;

     insertColBadResume();
    }

  showLogMessage("I","","total   cnt :" + totCnt);

  closeCursor();
 }
// ************************************************************************
 int insertColBadMResume() throws Exception
 {
  extendField = "bamr.";
  setValue("bamr.p_seqno"             , getValue("p_seqno")); 
  setValue("bamr.acct_type"           , getValue("acct_type")); 
  setValue("bamr.corp_p_seqno"        , getValue("corp_p_seqno")); 
  setValue("bamr.status_change_date"  , getValue("status_change_date")); 
  setValue("bamr.id_p_seqno"          , getValue("id_p_seqno"));
  setValue("bamr.acno_flag"           , getValue("acno_flag"));
  setValue("bamr.mod_time"            , sysDate+sysTime);
  setValue("bamr.mod_pgm"             , javaProgram);

  daoTable = "col_bad_m_resume";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_bad_m_resume error[dupRecord]");
      return(1);
     }
  return(0);
 }
// ***********************************************************************
 int updateColBadMResume(int yearCnt) throws Exception
 {
  if (new_add_amt <0) new_add_amt = 0; 

  String yearStr = Integer.toString(yearCnt); 
  daoTable  = "col_bad_m_resume";
  updateSQL = "end_bal_"     + yearStr + " = ?,"
            + "resume_amt_"  + yearStr + " = ?,"
            + "new_add_amt_" + yearStr + " = ?,"
            + "mod_pgm       = ?,"
            + "mod_time      = sysdate";
  whereStr  = "where p_seqno            = ? "
            + "and   status_change_date = ? ";

  setDouble(1 , end_bal);
  setDouble(2 , resume_amt);
  setDouble(3 , new_add_amt);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("status_change_date"));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 int insertColBadResume() throws Exception
 {
  extendField = "bame.";
  setValue("bame.proc_year"           , getValue("proc_year"));
  setValue("bame.card_flag"           , getValue("card_flag"));
  setValue("bame.bad_amt"             , getValue("bad_amt"));  
  setValue("bame.new_add_amt_1"       , getValue("new_add_amt_1"));     
  setValue("bame.new_add_amt_2"       , getValue("new_add_amt_2"));     
  setValue("bame.new_add_amt_3"       , getValue("new_add_amt_3"));     
  setValue("bame.resume_amt_1"        , getValue("resume_amt_1"));      
  setValue("bame.resume_amt_2"        , getValue("resume_amt_2"));      
  setValue("bame.resume_amt_3"        , getValue("resume_amt_3"));
  setValue("bame.mod_time"            , sysDate+sysTime);
  setValue("bame.mod_pgm"             , javaProgram);

  daoTable = "col_bad_resume";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_bad_resume error[dupRecord]");
      return(1);
     }
  return(0);
 }
// ***********************************************************************
 int updateColBadResume() throws Exception
 {
  daoTable  = "col_bad_resume";
  updateSQL = "resume_rate_1 = ?,"
            + "resume_rate_2 = ?,"
            + "resume_rate_3 = ?,"
            + "mod_pgm       = ?,"
            + "mod_time      = sysdate";
  whereStr  = "where rowid   = ? ";

  setDouble(1 , getValueDouble("resume_rate_1"));
  setDouble(2 , getValueDouble("resume_rate_2"));
  setDouble(3 , getValueDouble("resume_rate_3"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 void  loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "p_seqno,"
            + "sum(end_bal) as end_bal";
  daoTable  = "act_debt";
  whereStr  = "where end_bal > 0 "
            + "and   acct_code = 'DB' "
            + "and   acct_month <= ? "
            + "group by p_seqno ";

   setString(1 , businessDate.substring(0,6));
  int  n = loadTable();
  setLoadData("debt.p_seqno");
  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 void  loadColBadDetail() throws Exception
 {
  extendField = "badd.";
  selectSQL = "p_seqno,"
            + "sum(end_bal) as new_add_amt";
  daoTable  = "col_bad_detail";
  whereStr  = "where trans_type = '4' "
            + "and   trans_date like ? "
            + "group by p_seqno ";

  setString( 1 , businessDate.substring(0,4)+"%");

  int  n = loadTable();
  setLoadData("badd.p_seqno");
  showLogMessage("I","","Load col_bad_detail Count: ["+n+"]");
 }
// ************************************************************************
 int  selectColBadResumeR(String card_flag) throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/col/backup";
  if (card_flag.equals("1"))
     fileName1 = localDir+"/CRM151A_" + sysDate + ".TXT";
  else
    fileName1 = localDir+"/CRM151B_" + sysDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }

  selectPtrRptRealrate(card_flag);
  selectSQL = "proc_year,"
            + "bad_amt," 
            + "round((resume_amt_1)*?/bad_amt,2) as resume_rate_1,"
            + "round((resume_amt_2)*?/bad_amt,2) as resume_rate_2,"  
            + "round((resume_amt_3)*?/bad_amt,2) as resume_rate_3,"  
            + "rowid as rowid";
  daoTable  = "col_bad_resume";    
  whereStr  = "where proc_year >= ? "
            + "and card_flag = ? "
            + "order by 1";

  setDouble( 1 , 100.0+getValueDouble("real_int_rate"));
  setDouble( 2 , 100.0+getValueDouble("real_int_rate"));
  setDouble( 3 , 100.0+getValueDouble("real_int_rate"));
  setString( 4 , comm.lastMonth(businessDate,60).substring(0,4)); 
  setString( 5 , card_flag); 

  openCursor();

  headFileR(card_flag);

  double debitCntA=0,debitCntB=0;
  int[] intCnt = new int[3];
  double[] yearAvg = new double[3];
  for (int inti=0;inti<3;inti++) intCnt[inti]=0;
  for (int inti=0;inti<3;inti++) yearAvg[inti]=0;
  while( fetchTable() )
    {
     totCnt++;

     if (getValueDouble("resume_rate_1")!=0)intCnt[0]++;
     if (getValueDouble("resume_rate_2")!=0) intCnt[1]++;
     if (getValueDouble("resume_rate_3")!=0) intCnt[2]++;
     yearAvg[0] = yearAvg[0] + getValueDouble("resume_rate_1");
     yearAvg[1] = yearAvg[1] + getValueDouble("resume_rate_2");
     yearAvg[2] = yearAvg[2] + getValueDouble("resume_rate_3");

     buf  = fixLeft(String.format("%03d",Integer.valueOf(getValue("proc_year").substring(0,4))-1911)+"年度轉銷呆帳折現回收率",26)  
          + fixRight(String.format("%,.2f", getValueDouble("resume_rate_1"))+"%" , 20 );

     if (getValue("proc_year").compareTo(comm.lastMonth(businessDate,24).substring(0,4))>0)
        buf = buf + fixRight("-   " , 20 );
     else
        buf = buf 
            + fixRight(String.format("%,.2f", getValueDouble("resume_rate_2"))+"%" , 20 );

     if (getValue("proc_year").compareTo(comm.lastMonth(businessDate,36).substring(0,4))>0)
        buf = buf + fixRight("-   " , 20 );
     else
        buf = buf 
            + fixRight(String.format("%,.2f", getValueDouble("resume_rate_3"))+"%" , 20 );

     insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
     writeTextFile(fo1,buf+newLine);
     updateColBadResume();
    }

  buf  = ""; 
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  for (int inti=0;inti<3;inti++) 
     if (intCnt[inti]==0) intCnt[inti]=1;

  buf  = fixLeft("  年度合計平均折現回收率",26)
       + fixRight(String.format("%,.2f", yearAvg[0]/intCnt[0])+"%" , 20 )
       + fixRight(String.format("%,.2f", yearAvg[1]/intCnt[1])+"%" , 20 )
       + fixRight(String.format("%,.2f", yearAvg[2]/intCnt[2])+"%" , 20 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = fixLeft("年度合計平均淨折現回收率",26)
       + fixRight(String.format("%,.2f", yearAvg[0]/intCnt[0]*0.8)+"%" , 20 )
       + fixRight(String.format("%,.2f", yearAvg[1]/intCnt[1]*0.8)+"%" , 20 )
       + fixRight(String.format("%,.2f", yearAvg[2]/intCnt[2]*0.8)+"%" , 20 )
       + fixRight(String.format("%,.2f", (yearAvg[0]/intCnt[0]+yearAvg[1]/intCnt[1]+yearAvg[2]/intCnt[2]))+"%" , 20 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = ""; 
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = "年度合計平均淨折現回收率 = 年度合計平均折現回收率*80%(呆帳委外收回另需支付收回率額%20為傭金)";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = ""; 
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = " 製表單位：資訊處                            經辦：                               核章： ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  closeCursor();
  closeOutputText(fo1);

  localDir  = "/crdatacrea/CREDITCARD";
  if (card_flag.equals("1"))
     fileName4 = localDir+"/CRM151A_" + sysDate + ".TXT";
  else
    fileName4 = localDir+"/CRM151B_" + sysDate + ".TXT";
  copyFileUsingStream(fileName1,fileName4); 

  return(0);
 }
// ************************************************************************
 void headFileR(String card_flag) throws Exception 
 {
  String temp = "";
  if (card_flag.equals("1"))
     {
      rptId_r1  = "ColD270R1";
      rptName1  = "非法人信用卡呆帳年度合計平均折現回收率計算表";
     }
  else
     {
      rptId_r1  = "ColD270R2";
      rptName1  = "法人信用卡呆帳年度合計平均折現回收率計算表";
     }

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, ""              + rptName1                 , 40);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM151A   科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: 1"                               ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "           折現回收率           第一年折現回收率    第二年折現回收率   第三年折現回收率           合  計            ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "年度                                                                                                               ";
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
  whereStr  = "where program_code like 'ColD270%' "
            + "and   start_date = ? ";

  setString(1 , sysDate);

  int recCnt = deleteTable();

  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

  return(0);
 }
// ***********************************************************************
 int deleteColBadMResume() throws Exception
 {
  daoTable  = "col_bad_m_resume";
  whereStr  = "where status_change_date like ? ";

  setString(1 , businessDate.substring(0,6)+"%");

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_bad_m_resume cnt :["+ recCnt +"]");

  return(0);
 }
// ***********************************************************************
 int deleteColBadResume() throws Exception
 {
  daoTable  = "col_bad_resume";
  whereStr  = "where proc_year between ? and ? ";

  setString( 1 , comm.lastMonth(businessDate,36).substring(0,4));
  setString( 2 , comm.lastMonth(businessDate,12).substring(0,4));

  int recCnt = deleteTable();

  showLogMessage("I","","delete col_bad_resume cnt :["+ recCnt +"]");

  return(0);
 }
// ***********************************************************************
 void selectPtrRptRealrate(String card_flag) throws Exception
 {
  selectSQL = "real_int_rate";
  daoTable   = "ptr_rpt_realrate";
  whereStr  = "where proc_month = ? "
            + "and   card_flag  = ? ";

  setString( 1 , businessDate.substring(0,6));
  //setString( 1 , "202205");    // for test
  setString( 2 , card_flag);

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_rpt_realrate ["+businessDate.substring(0,6)+"]["+ card_flag +"] error!" );
      exitProgram(1);
     }
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
