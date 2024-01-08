/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/09  V1.00.12  Allen Ho   CommDCFund initial                         *
* 110/12/14  V1.00.13  Simon      M#9145 0.01 variation                      *
* 111/11/08  V1.00.14  Zuwei Su   sync from mega & coding standard update    *
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.Math;
import java.math.BigDecimal;

public class CommDCFund extends AccessDAO
{
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
  public double hCommTranAmt = 0.0; 
  public String modPgm         = "";

  String hMcdlPSeqno       = "";
  String hMcdlCurrCode    = "";
  String hMcdlFundCode    = "";
  double hMcdlEndTranAmt = 0.0;
  double hMcdlTranAmt     = 0.0;

  int    updateCnt     = 0;
  double linkTranAmt = 0.0;
  double mainTranAmt = 0.0;
  String linkSeqno    = "";
  String mainSeqno    = "";

  String[] DBNAME = new String[10];

// ************************************************************************
 public CommDCFund(Connection conn[],String[] dbAlias) throws Exception
 {
   javaProgram = this.getClass().getName();
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];

   javaProgram = "CommDCFund";

   return;
 }
// ************************************************************************
 public  int dcfundFunc() throws Exception
 {
  javaProgram = this.getClass().getName();
  setConsoleMode("N");
  if (modPgm.length()!=0) javaProgram = modPgm;
  selectSQL = "max(tran_seqno) as tran_seqno ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt != 0 " 
            + "group by p_seqno,fund_code,curr_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 " 
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0 "
            ;

  openCursor();

  while( fetchTable() )
    {
     dcfundFunc(getValue("tran_seqno"));
    }
  closeCursor();
  setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public  int dcfundFunc(String tranSeqno) throws Exception
 {
  javaProgram = this.getClass().getName();
  if (modPgm.length()!=0) javaProgram = modPgm;
  setConsoleMode("N");
  initData();
  extendField = "tran.";
  selectSQL = "end_tran_amt, "
            + "p_seqno, "
            + "fund_code, "
            + "curr_code, "
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

   hMcdlPSeqno     =  getValue("tran.p_seqno");
   hMcdlFundCode  =  getValue("tran.fund_code");
   hMcdlCurrCode  =  getValue("tran.curr_code");
   hMcdlEndTranAmt =  getValueDouble("tran.end_tran_amt");

   mainSeqno = tranSeqno;
   mainTranAmt = 0.0;
   hMcdlTranAmt = selectCycDcFundDtlM(hMcdlEndTranAmt);

   linkSeqno = "";
   linkTranAmt = 0.0;
   if ((mainSeqno.length()!=0)&&
       (mainTranAmt!=0))
      {
       linkSeqno     = mainSeqno;
       linkTranAmt = mainTranAmt;
      }

   if (hMcdlTranAmt!=hMcdlEndTranAmt)
      updateCycDcFundDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"));
  setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 double selectCycDcFundDtlM(double tempTranAmt) throws Exception
 {
  setConsoleMode("N");
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno, "
            + "end_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt  != 0 " 
            + "and    apr_flag      = 'Y' "  
            + "and    fund_code     = ? "
            + "and    curr_code     = ? "
            + "and    p_seqno       = ? "
            + "and    decode(sign(end_tran_amt),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) "
            + "order by  decode(effect_e_date,'','99999999',effect_e_date),crt_date "
            ;

  setString(1,hMcdlFundCode);
  setString(2,hMcdlCurrCode);
  setString(3,hMcdlPSeqno);
  setDouble(4,tempTranAmt);

  int recCnt = selectTable();

  if (recCnt==0) return(tempTranAmt); 

  hCommTranAmt      = 0;
  double  hMcdlPTranAmt  = 0;
  double  hMcdlMTranAmt  = 0;

  for ( int inti=0; inti<recCnt; inti++ )
    {
     hCommTranAmt      = commCurrAmt(hMcdlCurrCode,
                            hCommTranAmt
                          + tempTranAmt 
                          + getValueDouble("com.end_tran_amt",inti),0);

     if (tempTranAmt > 0)
        {
         hMcdlPTranAmt  = tempTranAmt;
         hMcdlMTranAmt  = getValueDouble("com.end_tran_amt",inti);
        }
     else
        {
         hMcdlMTranAmt  = tempTranAmt;
         hMcdlPTranAmt  = getValueDouble("com.end_tran_amt",inti); 
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

     hMcdlTranAmt =  commCurrAmt(hMcdlCurrCode,hMcdlTranAmt,0);

     if (hMcdlTranAmt==0)
        {
         linkTranAmt = commCurrAmt(hMcdlCurrCode, 
                                       getValueDouble("com.end_tran_amt",inti),0)
                       - commCurrAmt(hMcdlCurrCode, 
                                       hMcdlTranAmt,0);

         linkSeqno = mainSeqno;
        }
     else
        {    
         linkTranAmt = 0;
         linkSeqno = "";
         mainTranAmt = 0.0
                       - commCurrAmt(hMcdlCurrCode, 
                                       getValueDouble("com.end_tran_amt",inti),0)
                       + commCurrAmt(hMcdlCurrCode, 
                                       hMcdlTranAmt,0);
         mainSeqno = getValue("com.tran_seqno",inti);
        }  

     updateCycDcFundDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti));

     if (tempTranAmt==0) break;
    }

  tempTranAmt =  commCurrAmt(hMcdlCurrCode,tempTranAmt,0);
  return(tempTranAmt); 
 }
// ************************************************************************
 int updateCycDcFundDtl(String tempRowid,double tempModSeqno) throws Exception
 {
  setConsoleMode("N");
  dateTime();
  updateSQL = "end_tran_amt = ?, "
            + "link_seqno   = ?, "
            + "link_tran_amt = ?, "
            + "mod_pgm     = ?, "
            + "mod_seqno   = mod_seqno + 1 , "
            + "mod_time    = timestamp_format(?,'yyyymmddhh24miss')";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setDouble(1 , hMcdlTranAmt);
  setString(2 , linkSeqno);
  setDouble(3 , linkTranAmt);
  setString(4 , modPgm);
  setString(5 , sysDate+sysTime);
  setRowId(6  , tempRowid);
  setDouble(7 , tempModSeqno);

  int cnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE cyc_dc_fund_dtl rror "+getValue("rowid"));
      exitProgram(1);
     }

  return(0);
 }
// ************************************************************************
 double  commCurrAmt(String currCode,double val,int rnd) throws Exception
 {
  extendField = "pcde.";
  selectSQL = "curr_amt_dp ";
  daoTable  = "ptr_currcode";
  whereStr  = "where curr_code = ? ";

  setString(1,currCode);

  int recCnt = selectTable();

  double calVal=1;
  for (int inti=0;inti<getValueInt("pcde.curr_amt_dp");inti++)
     calVal = calVal * 10.0;

  val = val * calVal;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal(String.format("%.0f",calVal)));

  if (recCnt==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 public int dCFundReverse(String tranSeqno) throws Exception
 {
  int intq=0;
  String[] tranQueue = new String[30];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectCycDcFundDtlQueue(tranQueue[intq]);
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
     dCFundReverseM(tranQueue[inti]);
    }
   
  return(0);
 }
// ************************************************************************
 int dCFundReverseM(String tranSeqno) throws Exception
 {
  updateCnt=0;
   if (modPgm.length()!=0) javaProgram = modPgm;
  comr = new CommRoutine(getDBconnect(),getDBalias());
  extendField = "tran.";
  selectSQL = "fund_code, "
            + "fund_name,"  
            + "curr_code,"  
            + "acct_date,"  
            + "beg_tran_amt,"  
            + "end_tran_amt,"  
            + "tran_seqno,"  
            + "acct_type, "
            + "mod_desc, "
            + "p_seqno, "
            + "id_p_seqno, "
            + "link_seqno, "
            + "link_tran_amt,"
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

  mainTranAmt = 0;

  if (getValue("tran.link_seqno").length()>0)
     {
      if (getValueDouble("tran.beg_tran_amt")<0)
         {
          if (getValueDouble("tran.beg_tran_amt")>getValueDouble("tran.link_tran_amt"))
              setValueDouble("tran.link_tran_amt",getValueDouble("tran.beg_tran_amt"));
         }
      else
         {
          if (getValueDouble("tran.beg_tran_amt")<getValueDouble("tran.link_tran_amt"))
              setValueDouble("tran.link_tran_amt",getValueDouble("tran.beg_tran_amt"));
         }
     }

  if (commCurrAmt(getValue("tran.curr_code"),getValueDouble("tran.beg_tran_amt"),0)  == 
      commCurrAmt(getValue("tran.curr_code"),getValueDouble("tran.end_tran_amt"),0)) return(0);

  selectCycDcFundDtlR1();

  if (getValue("tran.link_seqno").length()!=0)
      selectCycDcFundDtlR2();
  
  updateCycDcFundDtlR2(commCurrAmt(getValue("tran.curr_code"),
                                          getValueDouble("tran.beg_tran_amt"),0));

   
  if (commCurrAmt(getValue("tran.curr_code"),
                    getValueDouble("tran.beg_tran_amt") 
                   -getValueDouble("tran.end_tran_amt"),0) != 
      commCurrAmt(getValue("tran.curr_code"),mainTranAmt,0))
     {
      selectVmktFundName();

      selectPtrBusinday();

      double begTranAmt = commCurrAmt(getValue("tran.curr_code"), 
                            (getValueDouble("tran.beg_tran_amt")
                          -  getValueDouble("tran.end_tran_amt")
                          -  mainTranAmt),0);

      if (begTranAmt!=0)  
         insertCycDcFundDtl(begTranAmt*-1);
     }
   
  return(updateCnt);
 }
// ************************************************************************
 int selectCycDcFundDtlR1() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno,"
            + "link_tran_amt,"
            + "beg_tran_amt, "
            + "end_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "cyc_dc_fund_dtl";
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
         if (getValueDouble("com.beg_tran_amt",inti)<0)
            {
             if (getValueDouble("com.beg_tran_amt",inti)>getValueDouble("com.link_tran_amt",inti))
                 setValueDouble("com.link_tran_amt",getValueDouble("com.beg_tran_amt",inti),inti);
            }
         else
            {
             if (getValueDouble("com.beg_tran_amt",inti)<getValueDouble("com.link_tran_amt",inti))
                 setValueDouble("com.link_tran_amt",getValueDouble("com.beg_tran_amt",inti),inti);
            }
        }
     mainTranAmt = commCurrAmt(getValue("tran.curr_code"),
                                   mainTranAmt 
                                 - getValueDouble("com.link_tran_amt",inti),0);

     setValueDouble("end_tran_amt"  , commCurrAmt(getValue("tran.curr_code"), 
                                                    getValueDouble("com.end_tran_amt",inti)
                                                  + getValueDouble("com.link_tran_amt",inti),0));

     updateCycDcFundDtlR1(inti);
    }

  return(0); 
 }
// ************************************************************************
 int selectCycDcFundDtlR2() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "link_tran_amt,"
            + "beg_tran_amt, "
            + "end_tran_amt, "
            + "rowid as rowid ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  tran_seqno = ? "
            + "and    p_seqno     = ? " 
            ;

  setString(1,getValue("tran.link_seqno"));
  setString(2,getValue("tran.p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(1);
   
  if (getValue("com.link_seqno").length()>0)
     {
      if (getValueDouble("com.beg_tran_amt")<0)
         {
          if (getValueDouble("com.beg_tran_amt")>getValueDouble("com.link_tran_amt"))
              setValueDouble("com.link_tran_amt",getValueDouble("com.beg_tran_amt"));
         }
      else
         {
          if (getValueDouble("com.beg_tran_amt")<getValueDouble("com.link_tran_amt"))
              setValueDouble("com.link_tran_amt",getValueDouble("com.beg_tran_amt"));
         }
     }

  mainTranAmt = commCurrAmt(getValue("tran.curr_code"),
                                mainTranAmt
                              + getValueDouble("tran.link_tran_amt"),0);

  setValueDouble("end_tran_amt"  , commCurrAmt(getValue("tran.curr_code"), 
                                                 getValueDouble("com.end_tran_amt")
                                               - getValueDouble("tran.link_tran_amt"),0));

  updateCycDcFundDtlR1(0);

  return(0); 
 }
// ************************************************************************
 int updateCycDcFundDtlR1(int inti) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_amt  = ?, "
            + "link_seqno   = '',"
            + "link_tran_amt = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setDouble(1  , getValueDouble("end_tran_amt"));
  setString(2  , javaProgram);
  setRowId(3   , getValue("com.rowid",inti));
  setDouble(4  , getValueDouble("com.mod_seqno",inti)); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int updateCycDcFundDtlR2(double endTranAmt) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_amt  = ?, "
            + "link_seqno   = '',"
            + "link_tran_amt = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setDouble(1 , endTranAmt);
  setString(2 , javaProgram);
  setRowId(3  , getValue("tran.rowid"));
  setDouble(4 , getValueDouble("tran.mod_seqno")); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertCycDcFundDtl(double endTranAmt) throws Exception
 {
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , comr.getSeqno("MKT_MODSEQ"));

  setValue("mbdl.curr_code"            , getValue("tran.curr_code"));
  setValue("mbdl.fund_code"            , getValue("tran.fund_code"));
  setValue("mbdl.fund_name"            , getValue("tran.fund_name") + "沖回調整");
  setValue("mbdl.effect_e_date"        , "");
  if (endTranAmt>0)
     {
      if (getValueInt("effc.effect_months")>0)
          setValue("mbdl.effect_e_date"     , comm.nextMonthDate(getValue("tran.acct_date")
                                           , getValueInt("effc.effect_months")));
     }
  setValue("mbdl.mod_desc"             , getValue("tran.mod_desc") + "沖回調整");
  setValue("mbdl.mod_memo"             , "tran_seqno:["+getValue("tran.tran_seqno")+"]");
  setValueDouble("mbdl.beg_tran_amt"      , endTranAmt);
  setValueDouble("mbdl.end_tran_amt"      , endTranAmt);
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
  daoTable  = "cyc_dc_fund_dtl";

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


  selectFundTable();

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

  if (recCnt==0) 
      setValue("effc.effect_months" ,"0");

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
 String selectCycDcFundDtlQueue(String tranSeqno) throws Exception
 {
  extendField = "queu.";
  selectSQL = "link_seqno";
  daoTable  = "cyc_dc_fund_dtl";
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


}   // End of class CommDCFund

