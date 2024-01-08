/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/20  V1.00.14  Allen Ho   mkt_D062                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA260 extends AccessDAO
{
 private final String PROGNAME = "紅利-紅利特惠活動(五)計算應計紅利處理程式 111-11-08  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String businessDate   = "";
 String activeCode     = "";

 String tranSeqno = "";
 String[] procWork  = new String[300];
 String[] startSMonth  = new String[300];
 String[] startEMonth  = new String[300];
 String[] startSDate   = new String[300];
 String[] startEDate   = new String[300];
 String   stmtCycle     = "";
 String   minSDate   = "";
 String   vdminSDate   = "";

 double[][] addAmtS = new double[300][10];
 double[][] addAmtE = new double[300][10];
 double[][] addAmtS2 = new double[300][10];
 double[][] addAmtE2 = new double[300][10];
 int[][]    addPoint  = new int[300][10];
 int[][]    addPoint1 = new int[300][10];
 int[][]    addTimes  = new int[300][10];
 int      hMbdrAddPoint1 = 0;

 int totalAmtPlus=0,totalAmtMinus=0;
 int vdFlagCnt = 0;
 int cdFlagCnt = 0;
 boolean DEBUG = false;

 long    totalCnt=0,updateCnt=0;
 int parmCnt=0,inti,procMonths=1;
 int[] matchCnt= new int[300];
 int[] matchFlag= new int[300];
 String feedbackType = "";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA260 proc = new CycA260();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
//   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
 	   showLogMessage("I","","PARM 1 : [feedbackType]");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [active_code]");
       return(1);
      }

	if (args.length == 0 || (!args[0].equals("1") &&
			!args[0].equals("2"))) {
		showLogMessage("I","","請傳入回饋方式 : 1.帳單週期 2.每月 ");
		return(1);
	}  

	feedbackType = args[0];
   
   if (args.length >= 2 )
      { businessDate = args[1]; }

   if (args.length == 3 )
      { activeCode  = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
   
   int cycleFlag = selectPtrWorkday();
   
   if ((feedbackType.equals("1")) && !(cycleFlag == 0))   {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
   }
   if ((feedbackType.equals("2")) &&  (cycleFlag == 0) )   {
       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
       return(0);
   }  
   

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktBpmh3();
   if (vdFlagCnt+cdFlagCnt>0)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","載入暫存資料(idno)");
       if (vdFlagCnt>0) loadDbcIdno();
       if (cdFlagCnt>0) loadCrdIdno();

       showLogMessage("I","","=========================================");
       showLogMessage("I","","處理(mkt_bpmh3_data) 補 id_no 資料");
       selectMktBpmh3Data0();
       showLogMessage("I","","處理 ["+totalCnt+"] 筆");

       showLogMessage("I","","=========================================");
       showLogMessage("I","","載入暫存資料(card)");
       if (vdFlagCnt>0) loadDbcCard();
       if (cdFlagCnt>0) loadCrdCard();

       showLogMessage("I","","=========================================");
       showLogMessage("I","","處理(mkt_bpmh3_data)資料");
       selectMktBpmh3Data();
       showLogMessage("I","","處理 ["+totalCnt+"] 筆");
       showLogMessage("I","","=========================================");
      }

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
 int selectMktBpmh3Data0() throws Exception
 {
  selectSQL = "id_p_seqno,"
            + "vd_flag,"
            + "rowid as rowid";
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where feedback_date = ? "
            + "and   proc_flag     = 'N' "
//          +"and    p_seqno = '0001731243' "  //debug 20200110
            ;                   

  setString(1 , businessDate);

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(2 , activeCode);
     }            

  openCursor();

  totalCnt=0;

  String acctType="";
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (getValue("vd_flag").equals("Y"))
       {
        setValue("dbid.id_p_seqno",getValue("id_p_seqno"));
        int cnt1 = getLoadData("dbid.id_p_seqno");
        if (cnt1==0) 
           {
            showLogMessage("I","","dbc_idno id_p_seqno : ["+ getValue("id_p_seqno") +"] not found");
            continue;
           }
        updateMktBpmh3Data0(getValue("dbid.id_no"));
       }
    else
       {
        setValue("idno.id_p_seqno",getValue("id_p_seqno"));
        int cnt1 = getLoadData("idno.id_p_seqno");
        if (cnt1==0) 
           {
            showLogMessage("I","","crd_idno id_p_seqno : ["+ getValue("id_p_seqno") +"] not found");
            continue;
           }
        updateMktBpmh3Data0(getValue("idno.id_no"));
       }

   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpmh3Data() throws Exception
 {
  selectSQL = "active_code,"
            + "id_no,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dest_cnt) as dest_cnt,"
            + "sum(doorsill_amt) as doorsill_amt,"
            + "sum(doorsill_cnt) as doorsill_cnt ";
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where feedback_date = ? "
            + "and   proc_flag        = 'N' "
//          + "and    p_seqno = '0001731243' "  //debug 20200110
            ;

  setString(1 , businessDate);
   
  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(2 , activeCode);
     }            

  whereStr  = whereStr 
            + "GROUP BY active_code,id_no ";

  openCursor();

  totalCnt=0;

  String acctType="";
  while( fetchTable() ) 
   { 
    totalCnt++;
    for (inti=0;inti<parmCnt;inti++)
      {
       if (!getValue("parm.active_code",inti).equals(getValue("active_code"))) continue;

       hMbdrAddPoint1 = 0;
       if (getValue("parm.doorsill_flag",inti).equals("Y"))
          if (checkDoorsill(inti)==0)
             {
              updateMktBpmh3Data("R");   // 不在級距
              break;
             }

       double tempAmt=0,calAmt = 0;
       int tempPoint=0, hM3bsTotalPoint=0 ;

       if (getValue("parm.add_item_flag",inti).equals("1"))
          tempAmt = getValueDouble("dest_amt");
       else
          tempAmt = getValueDouble("dest_cnt");

       for (int inta=0;inta<10;inta++)
         {
          if (addAmtE[inti][inta]==0) break;
          if (getValue("parm.add_type",inti).equals("1"))
             {
              if (tempAmt<addAmtS[inti][inta]) break;
              if (tempAmt>=addAmtE[inti][inta])
                 {
                  if (inta==0) {calAmt = addAmtE[inti][0];}
                  else {calAmt = addAmtE[inti][inta] - addAmtE[inti][inta-1];}
                 }
              else
                 {
                  if (inta==0) {calAmt=tempAmt;}
                  else {calAmt = tempAmt - addAmtE[inti][inta-1];}
                 }
             }
          else
             {
              if ((tempAmt>=addAmtS[inti][inta])&&
                  (tempAmt<=addAmtE[inti][inta]))
                 {
                  calAmt = getValueDouble("dest_amt");
                 }
              else {continue;}
             }
          tempPoint = (int) Math.floor(calAmt / getValueInt("parm.per_point_amt",inti)*1.0);
          hM3bsTotalPoint = hM3bsTotalPoint + (tempPoint*addTimes[inti][inta]) +
                               addPoint[inti][inta];

         }
       hM3bsTotalPoint = hM3bsTotalPoint 
                          + hMbdrAddPoint1;

       if (hM3bsTotalPoint==0) continue;

       if (getValueDouble("parm.bonus_upper",inti)!=0)
          if (hM3bsTotalPoint>getValueInt("parm.bonus_upper",inti))
              hM3bsTotalPoint=getValueInt("parm.bonus_upper",inti);

       setValueInt("total_point", hM3bsTotalPoint);

       setValue("tran_code","2");
//     if (getValue("parm.doorsill_flag",inti).equals("Y"))
//        if (!getValue("parm.d_tax_flag",inti).equals("Y"))
//           setValue("tran_code","1");

       if (selectCrdCard()!=0)
          {
           if (getValueInt("parm.vd_flag_cnt",inti) >0)
              {
               if (selectDbcCard()!=0) 
                  {
                   updateMktBpmh3Data("E");   // 資料異常
                   break;
                  }
               insertMktBpmh3VdBonus();
               break;
              }
           else
              {
               updateMktBpmh3Data("Z");       // 資料異常
               break;
              }
          }
       else
          {
           insertMktBpmh3Bonus();
           break;
          }
      }
    updateMktBpmh3Data("Y");  
   } 
  closeCursor();

  for (inti=0;inti<parmCnt;inti++) 
    {
     setValue("active_code",getValue("parm.active_code",inti));
     updateMktBpmh3();
    }

  return(0);
 }
// ************************************************************************
 int selectMktBpmh3() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpmh3";
  whereStr  = "WHERE feedback_date = ? "
//          + "AND   feedback_flag = 'N' "          // debug
//          + "AND   active_code   = '10601004' "    // debug
            ;
  whereStr += " and run_time_type = ?";
  int i = 1;
  setString(i++ , businessDate);
  setString(i++ , feedbackType);
   
  if("2".equals(feedbackType)) {
	  CommString coms = new CommString();
	  whereStr += " and run_time_dd = ? ";
	  setInt(i++ ,coms.ss2int(coms.right(businessDate, 2)));
  }
  
  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(i++ , activeCode);
     }            

  parmCnt = selectTable();

  minSDate = comm.nextMonthDate(businessDate.substring(0,6)+"01",-1);
  vdminSDate = comm.nextMonthDate(businessDate.substring(0,6)+"01",-1);
  String tempDate="";
  for (int inti=0;inti<parmCnt;inti++)
    {
     setValue("active_code",getValue("parm.active_code",inti));
     showLogMessage("I","","活動代號 : ["+getValue("active_code")+"] doorsill_flag["+getValue("parm.doorsill_flag",inti)+"]");
     deleteMktBpmh3Bonus(inti);
     deleteMktBpmh3VdBonus(inti);

     if (Arrays.asList("1","2","3").contains(getValue("parm.list_cond",inti)))
        if (getValue("parm.vd_cond",inti).equals("Y"))
           {
            vdFlagCnt = vdFlagCnt + 1;
           }

     addAmtS[inti][0] = getValueDouble("parm.add_amt_s1",inti);
     addAmtS[inti][1] = getValueDouble("parm.add_amt_s2",inti);
     addAmtS[inti][2] = getValueDouble("parm.add_amt_s3",inti);
     addAmtS[inti][3] = getValueDouble("parm.add_amt_s4",inti);
     addAmtS[inti][4] = getValueDouble("parm.add_amt_s5",inti);
     addAmtS[inti][5] = getValueDouble("parm.add_amt_s6",inti);
     addAmtS[inti][6] = getValueDouble("parm.add_amt_s7",inti);
     addAmtS[inti][7] = getValueDouble("parm.add_amt_s8",inti);
     addAmtS[inti][8] = getValueDouble("parm.add_amt_s9",inti);
     addAmtS[inti][9] = getValueDouble("parm.add_amt_s10",inti);

     addAmtE[inti][0] = getValueDouble("parm.add_amt_e1",inti);
     addAmtE[inti][1] = getValueDouble("parm.add_amt_e2",inti);
     addAmtE[inti][2] = getValueDouble("parm.add_amt_e3",inti);
     addAmtE[inti][3] = getValueDouble("parm.add_amt_e4",inti);
     addAmtE[inti][4] = getValueDouble("parm.add_amt_e5",inti);
     addAmtE[inti][5] = getValueDouble("parm.add_amt_e6",inti);
     addAmtE[inti][6] = getValueDouble("parm.add_amt_e7",inti);
     addAmtE[inti][7] = getValueDouble("parm.add_amt_e8",inti);
     addAmtE[inti][8] = getValueDouble("parm.add_amt_e9",inti);
     addAmtE[inti][9] = getValueDouble("parm.add_amt_e10",inti);

     addAmtS2[inti][0] = getValueDouble("parm.d_add_amt_s1",inti);
     addAmtS2[inti][1] = getValueDouble("parm.d_add_amt_s2",inti);
     addAmtS2[inti][2] = getValueDouble("parm.d_add_amt_s3",inti);
     addAmtS2[inti][3] = getValueDouble("parm.d_add_amt_s4",inti);
     addAmtS2[inti][4] = getValueDouble("parm.d_add_amt_s5",inti);
     addAmtS2[inti][5] = getValueDouble("parm.d_add_amt_s6",inti);
     addAmtS2[inti][6] = getValueDouble("parm.d_add_amt_s7",inti);
     addAmtS2[inti][7] = getValueDouble("parm.d_add_amt_s8",inti);
     addAmtS2[inti][8] = getValueDouble("parm.d_add_amt_s9",inti);
     addAmtS2[inti][9] = getValueDouble("parm.d_add_amt_s10",inti);

     addAmtE2[inti][0] = getValueDouble("parm.d_add_amt_e1",inti);
     addAmtE2[inti][1] = getValueDouble("parm.d_add_amt_e2",inti);
     addAmtE2[inti][2] = getValueDouble("parm.d_add_amt_e3",inti);
     addAmtE2[inti][3] = getValueDouble("parm.d_add_amt_e4",inti);
     addAmtE2[inti][4] = getValueDouble("parm.d_add_amt_e5",inti);
     addAmtE2[inti][5] = getValueDouble("parm.d_add_amt_e6",inti);
     addAmtE2[inti][6] = getValueDouble("parm.d_add_amt_e7",inti);
     addAmtE2[inti][7] = getValueDouble("parm.d_add_amt_e8",inti);
     addAmtE2[inti][8] = getValueDouble("parm.d_add_amt_e9",inti);
     addAmtE2[inti][9] = getValueDouble("parm.d_add_amt_e10",inti);
     
     addTimes[inti][0] = getValueInt("parm.add_times1",inti);
     addTimes[inti][1] = getValueInt("parm.add_times2",inti);
     addTimes[inti][2] = getValueInt("parm.add_times3",inti);
     addTimes[inti][3] = getValueInt("parm.add_times4",inti);
     addTimes[inti][4] = getValueInt("parm.add_times5",inti);
     addTimes[inti][5] = getValueInt("parm.add_times6",inti);
     addTimes[inti][6] = getValueInt("parm.add_times7",inti);
     addTimes[inti][7] = getValueInt("parm.add_times8",inti);
     addTimes[inti][8] = getValueInt("parm.add_times9",inti);
     addTimes[inti][9] = getValueInt("parm.add_times10",inti);

     addPoint[inti][0] = getValueInt("parm.add_point1",inti);
     addPoint[inti][1] = getValueInt("parm.add_point2",inti);
     addPoint[inti][2] = getValueInt("parm.add_point3",inti);
     addPoint[inti][3] = getValueInt("parm.add_point4",inti);
     addPoint[inti][4] = getValueInt("parm.add_point5",inti);
     addPoint[inti][5] = getValueInt("parm.add_point6",inti);
     addPoint[inti][6] = getValueInt("parm.add_point7",inti);
     addPoint[inti][7] = getValueInt("parm.add_point8",inti);
     addPoint[inti][8] = getValueInt("parm.add_point9",inti);
     addPoint[inti][9] = getValueInt("parm.add_point10",inti);

     addPoint1[inti][0] = getValueInt("parm.d_add_point1",inti);
     addPoint1[inti][1] = getValueInt("parm.d_add_point2",inti);
     addPoint1[inti][2] = getValueInt("parm.d_add_point3",inti);
     addPoint1[inti][3] = getValueInt("parm.d_add_point4",inti);
     addPoint1[inti][4] = getValueInt("parm.d_add_point5",inti);
     addPoint1[inti][5] = getValueInt("parm.d_add_point6",inti);
     addPoint1[inti][6] = getValueInt("parm.d_add_point7",inti);
     addPoint1[inti][7] = getValueInt("parm.d_add_point8",inti);
     addPoint1[inti][8] = getValueInt("parm.d_add_point9",inti);
     addPoint1[inti][9] = getValueInt("parm.d_add_point10",inti);
                               
     selectVmktAcctType(inti);
     setValue("parm.vd_flag_cnt" , getValue("vmkt.vd_flag_cnt"),inti);
     setValue("parm.cd_flag_cnt" , getValue("vmkt.cd_flag_cnt"),inti);
     vdFlagCnt = vdFlagCnt + getValueInt("vmkt.vd_flag_cnt");
     cdFlagCnt = cdFlagCnt + getValueInt("vmkt.cd_flag_cnt");
    }                          

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 void updateMktBpmh3Data0(String idNo) throws Exception
 {
  dateTime();
  updateSQL = "id_no = ? ,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate"; 
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where  rowid   = ? ";

  setString(1 , idNo);
  setString(2 , javaProgram);  
  setRowId(3  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpmh3Data(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag = ? ,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate"; 
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where  active_code   = ? "
            + "and    id_no         = ? "
            + "and    feedback_date = ? ";

  setString(1 , procFlag);
  setString(2 , javaProgram);
  setString(3 , getValue("active_code"));
  setString(4 , getValue("id_no"));
  setString(5 , businessDate);

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpmh3() throws Exception
 {
  dateTime();
  updateSQL = "feedback_flag = 'Y', "
            + "mod_pgm       = ?, "
            + "mod_time      = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "mkt_bpmh3";
  whereStr  = "WHERE active_code   = ? ";

  setString(1 , javaProgram);
  setString(2 , sysDate+sysTime);
  setString(3 , getValue("active_code"));

  updateTable();
  return;
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.acct_type,"
            + "b.id_no,"
            + "a.id_p_seqno,"
            + "a.p_seqno";
  daoTable  = "crd_card a,crd_idno b,"
            + "   (select distinct id_no as id_no "
            + "    from   mkt_bpmh3_data "
            + "    where proc_flag = 'N' "
            + "    and   feedback_date = '"+ businessDate +"' "
            + "    and   vd_flag = 'N' ) c ";
  whereStr  = "WHERE a.id_p_seqno = b.id_p_seqno "
            + "and   b.id_no = c.id_no "
            + "and   a.current_code = '0' "
            + "and   a.card_no      = a.major_card_no "
            + "order by b.id_no,decode(acct_type,'01',1,'05',2,'06',3) ";

  int  n = loadTable();
  setLoadData("card.id_no");
  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "crd_idno";
  whereStr  = "WHERE id_p_seqno in ("
            + "     select distinct id_p_seqno "
            + "     from   mkt_bpmh3_data "
            + "     where proc_flag = 'N' "
            + "     and   feedback_date = ? " 
            + "     and   vd_flag   = 'N' )";

  setString(1 , businessDate);

  int  n = loadTable();
  setLoadData("idno.id_p_seqno");
  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
 }
// ************************************************************************
 void  loadDbcCard() throws Exception
 {
  extendField = "dbcd.";
  selectSQL = "a.acct_type,"
            + "b.id_no,"
            + "a.id_p_seqno";
  daoTable  = "dbc_card a,dbc_idno b,"
            + "   (select distinct id_no as id_no "
            + "    from   mkt_bpmh3_data "
            + "    where proc_flag = 'N' "
            + "    and   feedback_date = '"+ businessDate +"' "
            + "    and   vd_flag = 'Y' ) c ";
  whereStr  = "WHERE a.id_p_seqno = b.id_p_seqno "
            + "and   b.id_no = c.id_no "
            + "and   a.current_code = '0' ";

  int  n = loadTable();
  setLoadData("dbcd.id_no");
  showLogMessage("I","","Load dbc_card Count: ["+n+"]");
 }
// ************************************************************************
 void  loadDbcIdno() throws Exception
 {
  extendField = "dbid.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "dbc_idno";
  whereStr  = "WHERE id_p_seqno in ("
            + "     select distinct id_p_seqno "
            + "     from   mkt_bpmh3_data "
            + "     where proc_flag     = 'N' "
            + "     and   feedback_date = ? " 
            + "     and   vd_flag       = 'Y' )";

  setString(1 , businessDate);

  int  n = loadTable();
  setLoadData("dbid.id_p_seqno");
  showLogMessage("I","","Load dbc_idno Count: ["+n+"]");
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  setValue("card.id_no" , getValue("id_no"));

  int cnt1 = getLoadData("card.id_no");
  if (cnt1==0) return(1);

  setValue("acct_type"  , getValue("card.acct_type"));
  setValue("p_seqno"    , getValue("card.p_seqno"));
  setValue("id_p_seqno" , getValue("card.id_p_seqno"));

  return(0);
 }
// ************************************************************************
 int selectDbcCard() throws Exception
 {
  setValue("dbcd.id_no" , getValue("id_no"));

  int cnt1 = getLoadData("dbcd.id_no");
  if (cnt1==0) return(1);

  setValue("acct_type"  , getValue("dbcd.acct_type"));
  setValue("id_p_seqno" , getValue("dbcd.id_p_seqno"));
  return(0);
 }
// ************************************************************************
 int insertMktBpmh3Bonus() throws Exception
 {
  setValue("active_code"          , getValue("active_code"));
  setValue("feedback_date"        , businessDate);
  setValue("acct_type"            , getValue("acct_type"));
  setValue("p_seqno"              , getValue("p_seqno")); 
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  setValue("tran_code"            , getValue("tran_code"));  
  setValueInt("total_point"       , getValueInt("total_point"));
  setValue("proc_flag"            , "N"); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpmh3_bonus";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktBpmh3VdBonus() throws Exception
 {
  setValue("active_code"          , getValue("active_code"));
  setValue("feedback_date"        , businessDate);
  setValue("acct_type"            , getValue("acct_type"));
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  setValue("tran_code"            , getValue("tran_code"));  
  setValueInt("total_point"       , getValueInt("total_point"));
  setValue("proc_flag"            , "N"); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpmh3_vd_bonus";

  insertTable();

  return(0);
 }
// ************************************************************************
 int  checkDoorsill(int inti) throws Exception
 {
  double tempAmt;
  int matchFlag =0;

  if (getValue("parm.d_add_item_flag",inti).equals("1"))
     tempAmt = getValueDouble("doorsill_amt");
  else
     tempAmt = getValueDouble("doorsill_cnt");

  for (int inta=0;inta<10;inta++)
    {
     if (addAmtE2[inti][inta]==0) break;

     if ((tempAmt>=addAmtS2[inti][inta])&&
         (tempAmt<=addAmtE2[inti][inta]))
        {
         hMbdrAddPoint1 = addPoint1[inti][inta];
         matchFlag =1;
        }
     else {continue;}
    }
  return(matchFlag);
 }
// ************************************************************************
 int selectVmktAcctType(int inti) throws Exception
 {
  extendField = "vmkt.";
  selectSQL = "sum(decode(vd_flag,'Y',1,0)) as vd_flag_cnt,"
            + "sum(decode(vd_flag,'Y',0,1)) as cd_flag_cnt";
  daoTable  = "MKT_bn_data a,VMKT_ACCT_TYPE b";
  whereStr  = "WHERE table_name='MKT_BPMH3' "
            + "and data_type='1' "
            + "and a.data_code = b.acct_type "
            + "and a.data_key  = ? ";
  
  setString(1, getValue("parm.active_code",inti));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int deleteMktBpmh3Bonus(int inti) throws Exception
 {
  daoTable  = "mkt_bpmh3_bonus";
  whereStr  = "where  active_code   = ? "
            + "and    feedback_date = ? "
            ;

  setString(1 , getValue("parm.active_code",inti)); 
  setString(2 , businessDate); 

  totalCnt = deleteTable();

  if (totalCnt>0) 
     showLogMessage("I","","    刪除檔案 [" + totalCnt +"] 筆");

  return(0);
 }
// ************************************************************************
 int deleteMktBpmh3VdBonus(int inti) throws Exception
 {
  daoTable  = "mkt_bpmh3_vd_bonus";
  whereStr  = "where  active_code   = ? "
            + "and    feedback_date = ? "
            ;

  setString(1 , getValue("parm.active_code",inti)); 
  setString(2 , businessDate); 

  totalCnt = deleteTable();

  if (totalCnt>0) 
     showLogMessage("I","","    刪除檔案 [" + totalCnt +"] 筆");

  return(0);
 }
//************************************************************************
int selectPtrWorkday() throws Exception
{
extendField = "wday.";
selectSQL = "this_acct_month,"
          + "stmt_cycle";
daoTable  = "ptr_workday";
whereStr  = "WHERE this_close_date = ? ";

setString(1, businessDate);

selectTable();

if ( notFound.equals("Y") ) return(1);

return(0);
}
// ************************************************************************

}  // End of class FetchSample

