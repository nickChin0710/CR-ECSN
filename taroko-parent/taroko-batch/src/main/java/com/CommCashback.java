/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/29  V1.00.13  Allen Ho   CommCashback initial                       *
* 111/11/08  V1.00.14  Zuwei Su   sync from mega & coding standard update    *
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.Math;

public class CommCashback extends AccessDAO
{
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
// 已下3欄位只有需要知充抵應稅即免稅多少時用
  public int    hCommTranAmt      = 0; 
  public String modPgm      = "";

  String hMcdlPSeqno     = "";
  String hMcdlFundCode    = "";
  int    hMcdlEndTranAmt = 0;
  int    hMcdlTranAmt     = 0;
  int    updateCnt     = 0;

  int    linkTranAmt      = 0;
  int    mainTranAmt      = 0;
  String linkSeqno = "";
  String mainSeqno = "";

  String[] DBNAME = new String[10];

// ************************************************************************
 public CommCashback(Connection conn[],String[] dbAlias) throws Exception
 {
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];

   javaProgram = "CommCashba";

   return;
 }
// ************************************************************************
 public  int cashbackFunc() throws Exception
 {
  setConsoleMode("N");
   if (modPgm.length()!=0) javaProgram = modPgm;
  selectSQL = "max(tran_seqno) as tran_seqno ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  end_tran_amt != 0 " 
            + "group by p_seqno,fund_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 " 
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0 "
            ;

  openCursor();

  while( fetchTable() )
    {
     hCommTranAmt = 0;
     cashbackFunc(getValue("tran_seqno"));
    }
  closeCursor();
  setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public  int cashbackFunc(String tranSeqno) throws Exception
 {
  setConsoleMode("N");
   if (modPgm.length()!=0) javaProgram = modPgm;
  updateCnt=0;
  initData();

  extendField = "tran.";
  selectSQL = "end_tran_amt, "
            + "p_seqno, "
            + "fund_code, "
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

   hMcdlPSeqno     =  getValue("tran.p_seqno");
   hMcdlFundCode  =  getValue("tran.fund_code");
   hMcdlEndTranAmt =  getValueInt("tran.end_tran_amt");

   mainSeqno = tranSeqno;
   mainTranAmt = 0;
   hMcdlTranAmt = selectMktCashbackDtlM(hMcdlEndTranAmt);

/*
   showLogMessage("I","","STEP 1 h_mcdl_tran_amt     : ["+ h_mcdl_tran_amt + "]");
   showLogMessage("I","","STEP 2 h_mcdl_end_tran_amt : ["+ h_mcdl_end_tran_amt + "]");
   showLogMessage("I","","STEP 3 main_seqno          : ["+ main_seqno + "]");
   showLogMessage("I","","STEP 4 main_tran_ant       : ["+ main_tran_amt + "]");
*/
   linkSeqno = "";
   linkTranAmt = 0;
   if ((mainSeqno.length()!=0)&&
       (mainTranAmt!=0))
      {
       linkSeqno     = mainSeqno;
       linkTranAmt = mainTranAmt;
      }

   if (hMcdlTranAmt!=hMcdlEndTranAmt)
      updateMktCashbackDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"));
  setConsoleMode("Y");
  return(updateCnt); 
 }
// ************************************************************************
 int selectMktCashbackDtlM(int tempTranAmt) throws Exception
 {
  setConsoleMode("N");
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno, "
            + "end_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  end_tran_amt  != 0 " 
            + "and    apr_flag      = 'Y' "  
            + "and    fund_code    = ? "
            + "and    p_seqno       = ? "
            + "and    decode(sign(end_tran_amt),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) "
            + "order by  decode(effect_e_date,'','99999999',effect_e_date),crt_date "
            ;

  setString(1,hMcdlFundCode);
  setString(2,hMcdlPSeqno);
  setInt(3,tempTranAmt);

  int recCnt = selectTable();

  if (recCnt==0) return(tempTranAmt); 

  hCommTranAmt      = 0;
  int  hMcdlPTranAmt  = 0;
  int  hMcdlMTranAmt  = 0;

  for ( int inti=0; inti<recCnt; inti++ )
    {
     hCommTranAmt      = hCommTranAmt
                          + Math.abs(tempTranAmt + getValueInt("com.end_tran_amt",inti));

     if (tempTranAmt > 0)
        {
         hMcdlPTranAmt  = tempTranAmt;
         hMcdlMTranAmt  = getValueInt("com.end_tran_amt",inti);
        }
     else
        {
         hMcdlMTranAmt  = tempTranAmt;
         hMcdlPTranAmt  = getValueInt("com.end_tran_amt",inti); 
        }

     if (tempTranAmt > 0)
        {
         if (hMcdlPTranAmt + hMcdlMTranAmt>0)
            {
             hMcdlTranAmt =  0;
             tempTranAmt   = hMcdlPTranAmt + hMcdlMTranAmt;
            }
         else
            {
             hMcdlTranAmt =  hMcdlPTranAmt + hMcdlMTranAmt;
             tempTranAmt   = 0;
            }
        }
     else
        {
         if (hMcdlPTranAmt + hMcdlMTranAmt<0)
            {
             hMcdlTranAmt = 0;
             tempTranAmt   = hMcdlPTranAmt + hMcdlMTranAmt;
            }
         else
            {
             hMcdlTranAmt =  hMcdlPTranAmt + hMcdlMTranAmt;
             tempTranAmt   = 0;
            }
        }

     if (hMcdlTranAmt==0)
        {
         linkTranAmt = getValueInt("com.end_tran_amt",inti) 
                      - hMcdlTranAmt;
         linkSeqno = mainSeqno;
        }
     else
        {    
         linkTranAmt = 0;
         linkSeqno = "";
         mainTranAmt = 0 - (getValueInt("com.end_tran_amt",inti) - hMcdlTranAmt);
         mainSeqno = getValue("com.tran_seqno",inti);
        }  

     updateMktCashbackDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti));
     if (tempTranAmt==0) break;
    }

  return(tempTranAmt); 
 }
// ************************************************************************
 int updateMktCashbackDtl(String tempRowid,double tempModSeqno) throws Exception
 {
  setConsoleMode("N");
  dateTime();
  updateSQL = "end_tran_amt = ?, "
            + "link_seqno   = ?, "
            + "link_tran_amt = ?, "
            + "mod_pgm     = ?, "
            + "mod_seqno   = mod_seqno + 1 , "
            + "mod_time    = sysdate ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE rowid   = ? "
            + "AND mod_seqno = ? ";

  setInt(1 , hMcdlTranAmt);
  setString(2 , linkSeqno);
  setInt(3 , linkTranAmt);
  setString(4 , modPgm);
  setRowId(5  , tempRowid);
  setDouble(6 , tempModSeqno);

  int cnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_cashback_dtl rror "+getValue("rowid"));
      exitProgram(1);
     }
  updateCnt++;
  return(0);
 }
// ************************************************************************
 public int cashbackReverse(String tranSeqno) throws Exception
 {
  int intq=0;
  String[] tranQueue = new String[300];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectMktCashbackDtlQueue(tranQueue[intq]);
     if (tranQueue[intq+1].length()==0) break;
     intq++;
    }
/*
  showLogMessage("I","","tran_seqno : ["+ tran_seqno + "]");
  showLogMessage("I","","intq : ["+ intq + "]");

  for (int inti=0;inti<=intq;inti++)
     showLogMessage("I","","Tran_seqno queue : ["+ tran_queue[inti] + "]");
*/

  for (int inti=intq;inti>=0;inti--)
    {  
//   showLogMessage("I","","RUN queue : ["+ tran_queue[inti] + "]");
     cashbackReverseM(tranQueue[inti]);
    }
   
  return(0);
 }
// ************************************************************************
 int cashbackReverseM(String tranSeqno) throws Exception
 {
  updateCnt=0;
   if (modPgm.length()!=0) javaProgram = modPgm;
  comr = new CommRoutine(getDBconnect(),getDBalias());
  extendField = "tran.";
  selectSQL = "fund_code, "
            + "fund_name,"  
            + "acct_date,"  
            + "beg_tran_amt,"  
            + "end_tran_amt,"  
            + "res_tran_amt,"  
            + "tran_seqno,"  
            + "acct_type, "
            + "mod_desc, "
            + "p_seqno, "
            + "id_p_seqno, "
            + "link_seqno, "
            + "link_tran_amt,"
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

  mainTranAmt = 0;

  if (getValue("tran.link_seqno").length()>0)
     {
      if (getValueInt("tran.beg_tran_amt")<0)
         {
          if (getValueInt("tran.beg_tran_amt")>getValueInt("tran.link_tran_amt"))
              setValueInt("tran.link_tran_amt",getValueInt("tran.beg_tran_amt"));
         }
      else
         {
          if (getValueInt("tran.beg_tran_amt")<getValueInt("tran.link_tran_amt"))
              setValueInt("tran.link_tran_amt",getValueInt("tran.beg_tran_amt"));
         }
     }

  if (getValueInt("tran.beg_tran_amt") == 
      getValueInt("tran.end_tran_amt") +  getValueInt("tran.res_tran_amt")) return(0);

  selectMktCashbackDtlR1();

  if (getValue("tran.link_seqno").length()!=0)
      selectMktCashbackDtlR2();
  
  updateMktCashbackDtlR2(getValueInt("tran.beg_tran_amt")-getValueInt("tran.res_tran_amt"));

//   showLogMessage("I","","beg_tran_amt : ["+ getValueInt("tran.beg_tran_amt") + "]");
//   showLogMessage("I","","end_tran_amt : ["+ getValueInt("tran.end_tran_amt") + "]");
//   showLogMessage("I","","res_tran_amt : ["+ getValueInt("tran.res_tran_amt") + "]");
//   showLogMessage("I","","main_tran_amt: ["+ main_tran_amt                    + "]");

  if (getValueInt("tran.beg_tran_amt") - getValueInt("tran.end_tran_amt") 
                                       - getValueInt("tran.res_tran_amt") != mainTranAmt)
     {
      selectVmktFundName();

      selectPtrBusinday();

      int begTranAmt = getValueInt("tran.beg_tran_amt")
                       - getValueInt("tran.end_tran_amt")
                       - getValueInt("tran.res_tran_amt")
                       - mainTranAmt;

      if (begTranAmt!=0)  
         insertMktCashbackDtl(begTranAmt*-1);
     }
  return(updateCnt);
 }
// ************************************************************************
 int selectMktCashbackDtlR1() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno,"
            + "link_tran_amt,"
            + "beg_tran_amt, "
            + "res_tran_amt, "
            + "end_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where link_seqno  = ? "
            + "and   p_seqno     = ? " 
            ;

  setString(1,getValue("tran.tran_seqno"));
  setString(2,getValue("tran.p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(0); 

  for ( int inti=0; inti<recCnt; inti++ )
    {
     if (getValue("com.link_seqno",inti).length()>0)
        {
         if (getValueInt("com.beg_tran_amt",inti)<0)
            {
             if (getValueInt("com.beg_tran_amt",inti)>getValueInt("com.link_tran_amt",inti))
                 setValueInt("com.link_tran_amt",getValueInt("com.beg_tran_amt",inti),inti);
            }
         else
            {
             if (getValueInt("com.beg_tran_amt",inti)<getValueInt("com.link_tran_amt",inti))
                 setValueInt("com.link_tran_amt",getValueInt("com.beg_tran_amt",inti),inti);
            }
        }
     mainTranAmt = mainTranAmt - getValueInt("com.link_tran_amt",inti);

     setValueInt("end_tran_amt"  , getValueInt("com.end_tran_amt",inti)
                                 + getValueInt("com.link_tran_amt",inti));

     updateMktCashbackDtlR1(inti);
    }

  return(0); 
 }
// ************************************************************************
 int selectMktCashbackDtlR2() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "link_tran_amt,"
            + "beg_tran_amt, "
            + "end_tran_amt, "
            + "res_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  tran_seqno = ? "
            + "and    p_seqno     = ? " 
            ;

  setString(1,getValue("tran.link_seqno"));
  setString(2,getValue("tran.p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(1);
   
  if (getValue("com.link_seqno").length()>0)
     {
      if (getValueInt("com.beg_tran_amt")<0)
         {
          if (getValueInt("com.beg_tran_amt")>getValueInt("com.link_tran_amt"))
              setValueInt("com.link_tran_amt",getValueInt("com.beg_tran_amt"));
         }
      else
         {
          if (getValueInt("com.beg_tran_amt")<getValueInt("com.link_tran_amt"))
              setValueInt("com.link_tran_amt",getValueInt("com.beg_tran_amt"));
         }
     }

  mainTranAmt = mainTranAmt + getValueInt("tran.link_tran_amt");

  setValueInt("end_tran_amt"  , getValueInt("com.end_tran_amt")
//                            + getValueInt("com.res_tran_amt")
                              - getValueInt("tran.link_tran_amt"));
                               
  updateMktCashbackDtlR1(0);

  return(0); 
 }
// ************************************************************************
 int updateMktCashbackDtlR1(int inti) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_amt  = ?, "
            + "link_seqno   = '',"
            + "link_tran_amt = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setInt(1    , getValueInt("end_tran_amt"));
  setString(2 , javaProgram);
  setRowId(3  , getValue("com.rowid",inti));
  setDouble(4 , getValueDouble("com.mod_seqno",inti)); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktCashbackDtlR2(int endTranAmt) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_amt  = ?, "
            + "link_seqno   = '',"
            + "link_tran_amt = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setInt(1    , endTranAmt);
  setString(2 , javaProgram);
  setRowId(3  , getValue("tran.rowid"));
  setDouble(4 , getValueDouble("tran.mod_seqno")); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktCashbackDtl(int endTranAmt) throws Exception
 {
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , getSeqno("MKT_MODSEQ"));

  setValue("mbdl.fund_code"            , getValue("tran.fund_code"));
  setValue("mbdl.fund_name"            , getValue("tran.fund_name") + "沖回調整");
  setValue("mbdl.effect_e_date"        , "");
  if (endTranAmt>0)
     {
      if (getValue("effc.effect_type").equals("1"))
         {
          if (getValueInt("effc.effect_months")>0)
              setValue("mbdl.effect_e_date"     , comm.nextMonthDate(getValue("tran.acct_date")
                                           , getValueInt("effc.effect_months")));
         }
      else if (getValue("effc.effect_type").equals("2"))
         {
          String[]  stra = new String[10];
          stra[0] = getValue("tran.acct_date");
          int[] inta = new int[10];
          inta[0] = getValueInt("effc.effect_years")-1;
          inta[1] = getValueInt("effc.effect_fix_month");
          stra[1] = String.format("%02d", inta[1]);

          setValue("mbdl.effect_e_date"    , comm.lastdateOfmonth(
                                             comm.nextMonth(stra[0],inta[0]*12).substring(0,4)+stra[1]+"01"));
         }
     }
  setValue("mbdl.mod_desc"             , getValue("tran.mod_desc") + "沖回調整");
  setValue("mbdl.mod_memo"             , "tran_seqno:["+getValue("tran.tran_seqno")+"]");
  setValueInt("mbdl.beg_tran_amt"      , endTranAmt);
  setValueInt("mbdl.end_tran_amt"      , endTranAmt);
  setValue("mbdl.tran_code"            , "3");
  setValue("mbdl.acct_date"            , getValue("busi.business_date"));
  setValue("mbdl.proc_month"           , getValue("busi.business_date").substring(0,6));
  setValue("mbdl.acct_type"            , getValue("tran.acct_type"));
  setValue("mbdl.p_seqno"              , getValue("tran.p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("tran.id_p_seqno"));
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_user"             , javaProgram);
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , javaProgram);
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  extendField = "mbdl.";
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectVmktFundName() throws Exception
 {
  extendField = "parm.";
  selectSQL = "table_name";
  daoTable  = "vmkt_fund_name";
  whereStr  = "where  fund_code = ? "
            ;

  setString(1,getValue("tran.fund_code"));

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      setValue("effc.effect_type" ,"0");
      setValue("effc.effect_months" ,"0");
      return(1);
     }

  if (getValue("parm.table_name").toUpperCase().equals("PTR_FUNDP"))
     {
      selectPtrFundp();
     }
  else
     {
      selectFundTable();
     }

  return(0); 
 }
// ************************************************************************
 int selectFundTable() throws Exception
 {
  extendField = "effc.";
  selectSQL = "effect_months";
  daoTable  = getValue("parm.table_name");
  whereStr  = "where  fund_code = ? "
            ;

  setString(1,getValue("tran.fund_code"));

  int recCnt = selectTable();


  setValue("effc.effect_type" ,"0");
  if (recCnt==0) 
      setValue("effc.effect_months" ,"0");

  if (getValueInt("effc.effect_months")>0)
      setValue("effc.effect_type" ,"1");

  return(0); 
 }
// ************************************************************************
 int selectPtrFundp() throws Exception
 {
  extendField = "effc.";
  selectSQL = "effect_type,"
            + "effect_years,"
            + "effect_months,"
            + "effect_fix_month";
  daoTable  = "ptr_fundp";
  whereStr  = "where  fund_code = ? "
            ;

  setString(1,getValue("tran.fund_code"));

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      setValue("effc.effect_type" ,"0");
      setValue("effc.effect_months" ,"0");
     } 

  return(0); 
 }
// ************************************************************************
 int selectPtrBusinday() throws Exception
 {
  extendField = "busi.";
  selectSQL = "business_date";
  daoTable  = "ptr_businday";
  whereStr  = "";

  int recCnt = selectTable();

  if (recCnt==0) 
      setValue("busi.business_date" , sysDate);

  return(0); 
 }
// ************************************************************************
 String selectMktCashbackDtlQueue(String tranSeqno) throws Exception
 {
  extendField = "queu.";
  selectSQL = "link_seqno";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return("");

  return(getValue("queu.link_seqno")); 
 }
// ************************************************************************
 void initData() throws Exception
 {
  hCommTranAmt = 0;

  return; 
 }
// ************************************************************************
 String  getSeqno(String seqName) throws Exception
 {
  selectSQL = "NEXTVAL FOR " + seqName + " AS MOD_SEQNO " ;
  daoTable  = "SYSIBM.SYSDUMMY1";
  selectTable();

  String output = String.format("%010.0f",getValueDouble("MOD_SEQNO"));
  while (output.length() < 10) output = "0" + output;

  return output;
 }
// ************************************************************************

}   // End of class CommCashback

