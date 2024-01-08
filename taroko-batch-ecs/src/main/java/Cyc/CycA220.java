/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/09/29  V1.00.30  Allen Ho   Cyc_A160                                   *
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
public class CycA220 extends AccessDAO
{
 private final String PROGNAME = "關帳-紅利特惠專案(二)回饋處理程式 111-11-08  V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate   = "";
 String activeCode     = "";
 String pSeqno         = "";

 int parmCnt=0; 
 String tranSeqno = "",cmpMonth="";
 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA220 proc = new CycA220();
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

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      { activeCode = args[1]; }
   if ( args.length == 3 )
      { pSeqno     = args[2]; }
   
   if ( !connectDataBase() ) 
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if ( args.length >= 1 )
      {
       if (selectPtrWorkday1()!=0)
          {
           showLogMessage("I","","本日非關帳日, 不需執行");
           return(0);
          }
      }
   else if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }
   showLogMessage("I","","本次關帳月份 ["+getValue("wday.this_acct_month")+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktBpnw();
   if (parmCnt==0)
      {
       showLogMessage("I","","本日無符合知參數, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktBpnwMlist();
   loadMktBpnwIntro();
   loadMktBnData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理卡片資料");
   selectCrdCard();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_businday";
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
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int  selectPtrWorkday1() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1,businessDate.substring(6,8));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  setValue("wday.this_close_date" , businessDate);
  setValue("wday.this_acct_month" , businessDate.substring(0,6));
  setValue("wday.next_acct_month" , comm.nextMonth(businessDate.substring(0,6) , 1));

  return(0);
 } 
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  selectSQL = "id_p_seqno,"
            + "major_id_p_seqno,"    
            + "p_seqno,"    
            + "acct_type,"
            + "sup_flag,"
            + "introduce_id,"
            + "promote_emp_no,"
            + "group_code,"
            + "card_type,"
            + "issue_date,"
            + "apply_no,"
            + "major_card_no,"
            + "ori_card_no,"
            + "ori_issue_date,"
            + "card_no";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' "
            + "and   issue_date = ori_issue_date "
            + "and   stmt_cycle = ? "
            + "and   issue_date between ? and ? "
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , comm.nextNDate(businessDate , -40));
  setString(3 , businessDate);

  if (pSeqno.length()>0)
     {
      whereStr  = whereStr 
                + "and p_seqno   = ? "; 
      setString(4 , pSeqno);
     }

  openCursor();

  totalCnt=0;

  int cnt1=0;
  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  while( fetchTable() ) 
   { 
    if (getValue("group_code").length()==0)
       setValue("group_code" , "0000");

    if (pSeqno.length()!=0)
       showLogMessage("I","","card_no["+getValue("card_no") + "]");

    for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("parm.apply_date_s",inti).length()==0)
          setValue("parm.apply_date_s","00000000",inti);
       if (getValue("parm.apply_date_e",inti).length()==0)
          setValue("parm.apply_date_e","30001231",inti);

       parmArr[inti][0]++;
       if (pSeqno.length()!=0) showLogMessage("I","","STEP 1 ");

       if (getValue("parm.apply_date_type",inti).equals("0"))
          {
           if (getValue("issue_date").compareTo(getValue("parm.apply_date_s",inti))<0) continue;
           if (getValue("issue_date").compareTo(getValue("parm.apply_date_e",inti))>0) continue;
          }
       else if (getValue("parm.apply_date_type",inti).equals("1"))
          {
           if (getValue("apply_no").length()==0) continue;
           setValue("apply_date", String.format("%4d", Integer.valueOf(getValue("apply_no").substring(0,3))+1911)
                                + getValue("apply_no").substring(3,7));
           if (getValue("apply_date").compareTo(getValue("parm.apply_date_s",inti))<0) continue;
           if (getValue("apply_date").compareTo(getValue("parm.apply_date_e",inti))>0) continue;
          }

       if (pSeqno.length()!=0) showLogMessage("I","","STEP 2 ");

       if (getValue("parm.applicant_cond",inti).equals("Y"))
          {
           setValue("list.card_no"     , getValue("card_no"));
           setValue("list.active_code" , getValue("parm.active_code",inti));
           cnt1 = getLoadData("list.card_no,list.active_code");
           if (cnt1!=0) continue;
          }
       if (pSeqno.length()!=0) showLogMessage("I","","STEP 3 ");

       if (getValue("parm.introducer_cond",inti).equals("Y"))
          {
           setValue("into.card_no"     , getValue("card_no"));
           setValue("into.active_code" , getValue("parm.active_code",inti));
           cnt1 = getLoadData("into.card_no,into.active_code");
           if (cnt1!=0) continue;
          }

       if (pSeqno.length()!=0) showLogMessage("I","","STEP 4 ");

       setValue("data_key", getValue("parm.active_code",inti));

       parmArr[inti][1]++;
       if (selectMktBnData(getValue("acct_type"),
                              getValue("parm.acct_type_sel",inti),"1",3)!=0) continue;

       if (pSeqno.length()!=0) showLogMessage("I","","STEP 5 ");
       parmArr[inti][2]++;
       if (selectMktBnData(getValue("group_code"),getValue("card_type"),
                              getValue("parm.group_card_sel",inti),"7",2)!=0) continue;

       if (pSeqno.length()!=0) showLogMessage("I","","STEP 6 ");
       parmArr[inti][3]++;

       int newCardFlag=1;
       if ((getValue("parm.new_card_cond",inti).equals("Y"))||
           (getValue("parm.new_card_cond1",inti).equals("Y")))
          {
           if (!getValue("id_p_seqno").equals(getValue("major_id_p_seqno")))
              {
               selectCrdCardMajor();
               setValue("ori_card_no"    , getValue("mard.ori_card_no"));
               setValue("ori_issue_date" , getValue("mard.ori_issue_date"));
              }
           if (selectCrdCardNewcard()!=0) newCardFlag=0;

           if (selectEcsCrdCardNewcard()!=0) newCardFlag=0; 
          }
           
       parmArr[inti][4]++;
       if (pSeqno.length()!=0) showLogMessage("I","","STEP 7 ");

       if (getValue("parm.applicant_cond",inti).equals("Y"))
          {
           if (((getValue("parm.new_card_cond",inti).equals("Y"))&&
                (newCardFlag==1))||
               (!getValue("parm.new_card_cond",inti).equals("Y")))
              {
               parmArr[inti][9]++;
               setValue("mist.proc_date"            , "");
               setValue("mist.proc_flag"            , "N");

               insertMktBpnwMlist(inti);
             }
          }

       if (getValue("parm.introducer_cond",inti).equals("Y"))
          {
           if ((getValue("parm.new_card_cond1",inti).equals("Y"))&&
               (newCardFlag!=1)) continue;
           setValue("in_acct_type"         , "");
           setValue("in_id_p_seqno"        , "");
           setValue("in_p_seqno"           , "");

           if (getValue("introduce_id").length()==10)
              {
               setValue("mod_memo"   , "介紹人ID["+getValue("introduce_id")+"]");
               if (selectCrdIdno()!=0)
                  {
                   setValue("intr.proc_flag"       , "A");
                   insertMktBpnwIntro(inti);
                   continue;
                  }
              }
           else if (getValue("promote_emp_no").length()==6)
              {
               setValue("mod_memo"   , "介紹人員編["+getValue("promote_emp_no")+"]");
               if (selectCrdEmployee()!=0)
                  {
                   if (selectCrdEmployeeA()!=0)
                      {
                       setValue("intr.proc_flag"       , "B");
                       insertMktBpnwIntro(inti);
                       continue;
                      }
                  }
              }

           setValue("intr.proc_flag"       , "N");
           if (getValue("in_acct_type").length()==0)
              setValue("intr.proc_flag"       , "C");

           if (pSeqno.length()!=0) showLogMessage("I","","STEP 8 ");
           
           insertMktBpnwIntro(inti);
          }

      }
    totalCnt++;
   } 
  closeCursor();

  if (pSeqno.length()!=0)
  for (int inti=0;inti<parmCnt;inti++)
    {
       showLogMessage("I","","active_code :["+getValue("parm.active_code",inti)+"]");
     for (int intk=0;intk<20;intk++)
         {
          if (parmArr[inti][intk]==0) continue;
          showLogMessage("I","","業日 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
         }
    }
   
  return(0);
 }
// ************************************************************************
 int selectMktBpnw() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpnw";
  whereStr  = "WHERE apr_flag        = 'Y' "
            + "AND   apr_date       != ''  "
            + "AND   (stop_flag     != 'Y'  "
            + " or    (stop_flag     = 'Y'  "
            + "  and  stop_date      > ? )) "
            + "AND   (active_date_e  = ''  "
            + " or    active_date_e >= ?) "           
            + "AND   (active_date_s  = ''  "
            + " or    active_date_s <= ?) "            
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? "; 
      setString(4 , activeCode);
     }

  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
      {
       deleteMktBpnwMlist(inti);
       deleteMktBpnwIntro(inti);
      }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int insertMktBpnwMlist(int inti) throws Exception
 {
  dateTime();

  extendField = "mist.";

  setValue("mist.create_date"          , businessDate);
  setValue("mist.active_code"          , getValue("parm.active_code",inti)); 
  setValue("mist.card_no"              , getValue("card_no"));
  setValue("mist.sup_flag"             , getValue("sup_flag"));
  setValue("mist.acct_type"            , getValue("acct_type"));
  setValue("mist.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("mist.p_seqno"              , getValue("p_seqno"));
  setValue("mist.mod_time"             , sysDate+sysTime);
  setValue("mist.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpnw_mlist";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktBpnwIntro(int inti) throws Exception
 {
  dateTime();

  extendField = "intr.";

  setValue("intr.create_date"          , businessDate);
  setValue("intr.introduce_id"         , getValue("introduce_id"));
  setValue("intr.promote_emp_no"       , getValue("promote_emp_no"));

  setValue("intr.in_acct_type"         , getValue("in_acct_type"));
  setValue("intr.in_id_p_seqno"        , getValue("in_id_p_seqno"));
  setValue("intr.in_p_seqno"           , getValue("in_p_seqno"));

  setValueInt("intr.feedback_bp"       , getValueInt("beg_tran_bp"));
  setValue("intr.proc_date"            , businessDate);

  setValue("intr.active_code"          , getValue("parm.active_code",inti)); 
  setValue("intr.card_no"              , getValue("card_no"));
  setValue("intr.sup_flag"             , getValue("sup_flag"));
  setValue("intr.acct_type"            , getValue("acct_type"));
  setValue("intr.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("intr.p_seqno"              , getValue("p_seqno"));
  setValue("intr.mod_time"             , sysDate+sysTime);
  setValue("intr.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpnw_intro";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_BPNW' "
            + "and   data_type in ('1','7') "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
            }
        }
     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "p_seqno,"
            + "b.acct_type,"
            + "major_id_p_seqno";
  daoTable  = "crd_card b,crd_idno c,ptr_acct_type a";
  whereStr  = "where c.id_no = ? "
            + "and   b.id_p_seqno = b.major_id_p_seqno "
            + "and   b.id_p_seqno = c.id_p_seqno "
            + "and   b.acct_type  = a.acct_type "
            + "and   a.card_indicator = '1' "
            ;

  setString(1 , getValue("introduce_id"));

  if (pSeqno.length()>0)
     {
      whereStr  = whereStr 
                + "and b.p_seqno   = ? "; 
      setString(2 , pSeqno);
     }

  whereStr  = whereStr 
            + "order by decode(a.acct_type,'01',0,'05',1,2) ";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  int okFlag=1;
  for (int inti=0;inti<recordCnt;inti++)
    {
     if (selectCrdCard0(getValue("idno.p_seqno",inti),getValue("idno.major_id_p_seqno",inti))==0)
        {
         setValue("in_p_seqno"    , getValue("idno.p_seqno",inti));
         setValue("in_id_p_seqno" , getValue("idno.major_id_p_seqno",inti));
         setValue("in_acct_type"  , getValue("idno.acct_type",inti));
         okFlag=0;
        }
    }
  return(okFlag);
 }
// ************************************************************************
 int selectCrdEmployee() throws Exception
 {
  extendField = "idno.";
  selectSQL = "p_seqno,"
            + "major_id_p_seqno,"
            + "b.acct_type ";
  daoTable  = "crd_card b,crd_idno c,ptr_acct_type a,crd_employee d";
  whereStr  = "where d.employ_no  = ? "
            + "and   c.id_no      = d.id "
            + "and   c.id_no_code = d.id_code "
            + "and   b.id_p_seqno = c.id_p_seqno "
            + "and   b.acct_type  = a.acct_type "
            + "and   a.card_indicator = '1' "
            ;

  setString(1 , getValue("promote_emp_no"));

  if (pSeqno.length()>0)
     {
      whereStr  = whereStr 
                + "and b.p_seqno   = ? "; 
      setString(2 , pSeqno);
     }

  whereStr  = whereStr 
            + "order by decode(b.acct_type,'01',0,'05',1,2) ";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  int okFlag=1;
  for (int inti=0;inti<recordCnt;inti++)
    {
     if (selectCrdCard0(getValue("idno.p_seqno",inti),getValue("idno.major_id_p_seqno",inti))==0)
        {
         setValue("in_p_seqno"    , getValue("idno.p_seqno",inti));
         setValue("in_id_p_seqno" , getValue("idno.major_id_p_seqno",inti));
         setValue("in_acct_type"  , getValue("idno.acct_type",inti));
         okFlag=0;
        }
    }
  return(okFlag);
 }
// ************************************************************************
 int selectCrdEmployeeA() throws Exception
 {
  extendField = "idno.";
  selectSQL = "p_seqno,"
            + "major_id_p_seqno,"
            + "b.acct_type ";
  daoTable  = "crd_card b,crd_idno c,ptr_acct_type a,crd_employee_a d";
  whereStr  = "where d.employ_no  = ? "
            + "and   c.id_no      = d.id "
            + "and   c.id_no_code = d.id_code "
            + "and   b.id_p_seqno = c.id_p_seqno "
            + "and   b.acct_type  = a.acct_type "
            + "and   a.card_indicator = '1' "
            ;

  setString(1 , getValue("promote_emp_no"));

  if (pSeqno.length()>0)
     {
      whereStr  = whereStr 
                + "and b.p_seqno   = ? "; 
      setString(2 , pSeqno);
     }

  whereStr  = whereStr 
            + "order by decode(b.acct_type,'01',0,'05',1,2) ";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  int okFlag=1;
  for (int inti=0;inti<recordCnt;inti++)
    {
     if (selectCrdCard0(getValue("idno.p_seqno",inti),getValue("idno.major_id_p_seqno",inti))==0)
        {
         setValue("in_p_seqno"    , getValue("idno.p_seqno",inti));
         setValue("in_id_p_seqno" , getValue("idno.major_id_p_seqno",inti));
         setValue("in_acct_type"  , getValue("idno.acct_type",inti));
         okFlag=0;
        }
    }
  return(okFlag);
 }
// ************************************************************************
 int selectCrdCard0(String pSeqno,String idPSeqno) throws Exception
 {
  extendField = "idno.";
  selectSQL = "p_seqno,"
            + "major_id_p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where p_seqno    = ? "
            + "and   id_p_seqno = ? "
            + "and   current_code = '0' ";

  setString(1 , pSeqno);
  setString(2 , idPSeqno);

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void deleteMktBpnwMlist(int inti) throws Exception
 {
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "where create_date = ? "
            + "and   active_code = ? ";

  setString(1, businessDate);
  setString(2, getValue("parm.active_code",inti));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteMktBpnwIntro(int inti) throws Exception
 {
  daoTable  = "mkt_bpnw_intro";
  whereStr  = "where create_date = ? "
            + "and   active_code = ? ";

  setString(1, businessDate);
  setString(2, getValue("parm.active_code",inti));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void  loadMktBpnwMlist() throws Exception
 {
  extendField = "list.";
  selectSQL = "card_no,"
            + "active_code";
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from   mkt_bpnw "
            + "      where apr_flag        = 'Y' "
            + "      AND   apr_date       != ''  "
            + "      AND   (stop_flag     != 'Y'  "
            + "       or    (stop_flag     = 'Y'  "
            + "        and  stop_date      > ? )) "
            + "      AND   (active_date_e  = ''  "
            + "       or    active_date_e >= ?) "
            + "      AND   (active_date_s  = ''  "
            + "       or    active_date_s <= ?) "
            + "      AND   applicant_cond = 'Y'  "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? "; 
      setString(4 , activeCode);
     }
  whereStr  = whereStr 
            + "      ) "
            + " and   proc_flag = 'N' "
            + " order by card_no,active_code";


  int  n = loadTable();

  setLoadData("list.card_no,list.active_code");

  showLogMessage("I","","Load mkt_bpnw_mlist Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktBpnwIntro() throws Exception
 {
  extendField = "into.";
  selectSQL = "card_no,"
            + "active_code";
  daoTable  = "mkt_bpnw_intro";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from   mkt_bpnw "
            + "      where apr_flag        = 'Y' "
            + "      AND   apr_date       != ''  "
            + "      AND   (stop_flag     != 'Y'  "
            + "       or    (stop_flag     = 'Y'  "
            + "        and  stop_date      > ? )) "
            + "      AND   (active_date_e  = ''  "
            + "       or    active_date_e >= ?) "
            + "      AND   (active_date_s  = ''  "
            + "       or    active_date_s <= ?) "
            + "      AND   introducer_cond = 'Y'  "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + " and active_code = ? "; 
      setString(4 , activeCode);
     }
  whereStr  = whereStr 
            + "      ) "
            + " and   proc_flag = 'N' "
            + " order by card_no,active_code";


  int  n = loadTable();

  setLoadData("into.card_no,into.active_code");

  showLogMessage("I","","Load mkt_bpnw_intro Count: ["+n+"]");
 }
// ************************************************************************
 int selectCrdCardMajor() throws Exception 
 {
  extendField = "mard.";
  selectSQL = "ori_card_no,"
            + "ori_issue_date";
  daoTable  = "crd_card";
  whereStr  = "WHERE  card_no = ? "
            ;

  setString(1 , getValue("major_card_no"));

  int recCnt = selectTable();

  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************
 int selectCrdCardNewcard() throws Exception 
 {
  extendField = "card.";
  selectSQL = "a.group_code,"
            + "a.card_type,"
            + "a.oppost_date";
  daoTable  = "crd_card a";
  whereStr  = "WHERE  a.id_p_seqno     = ? "
            + "AND    a.ori_card_no   !=  ? "
            + "AND    a.sup_flag       = '0' "
            + "AND    a.ori_issue_date < ? "
            ;

  setString(1 , getValue("major_id_p_seqno"));
  setString(2 , getValue("ori_card_no"));
  setString(3 , getValue("ori_issue_date"));

  int recCnt = selectTable();

  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************
 int selectEcsCrdCardNewcard() throws Exception 
 {
  extendField = "card.";
  selectSQL = "a.group_code,"
            + "a.card_type,"
            + "a.oppost_date";
  daoTable  = "ecs_crd_card a";
  whereStr  = "WHERE  a.id_p_seqno     = ? "
            ;

  setString(1 , getValue("major_id_p_seqno"));

  int recCnt = selectTable();

  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************


}  // End of class FetchSample
