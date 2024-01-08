/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/29  V1.00.20  Allen Ho   Debit 紅利沖低                             *
* 111/11/05  V1.01.03  Zuwei Su   sync from mega & coding standard update    *       
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.Math;

public class CommDBonus extends AccessDAO
{
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
// 已下3欄位只有需要知充抵應稅即免稅多少時用
  public int    hCommTranBp      = 0;
  public int    hCommTranBpTax  = 0;
  public String modPgm     = "";
  public int    tranBpTax  = 0;
  public int    tranBpNotax = 0;

  String hMbdlIdPSeqno  = "";
  String hMbdlAcctType   = "";
  String hMbdlBonusType  = "";
  String hBklgTaxFlag    = "";
  String hMbdlTaxFlag    = "";
  int    hMbdlEndTranBp = 0;
  int    hMbdlTranBp     = 0;
  int    hMbdlPTranBp  = 0;
  int    hMbdlMTranBp  = 0;
  int    linkTranBp      = 0;
  int    linkTaxTranBp  = 0;
  int    mainTranBp      = 0;
  String linkSeqno = "";
  String mainSeqno = "";
  int    rTaxTranBp     = 0;

  static boolean DEBUG   = false;
  static int    updateCnt     = 0;

  String[] DBNAME = new String[10];

// ************************************************************************
 public CommDBonus(Connection conn[],String[] dbAlias) throws Exception
 {
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];
   javaProgram = "CommDB";

   return;
 }
// ************************************************************************
 public  int dBonusFunc() throws Exception
 {
  if (!DEBUG)
  setConsoleMode("N");
  selectSQL = "max(tran_seqno) as tran_seqno ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  end_tran_bp != 0 " 
            + "group by id_p_seqno,acct_type,bonus_type "
            + "having sum(decode(sign(end_tran_bp),1,end_tran_bp,0))!=0 " 
            + "and    sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0 "
            ;

  openCursor();

  while( fetchTable() )
    {
     dBonusFunc(getValue("tran_seqno"));
    }
  closeCursor();
  setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public int dBonusFuncP(String idPSeqno) throws Exception
 {
  if (!DEBUG)
  setConsoleMode("N");
  selectSQL = "tran_seqno";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  end_tran_bp != 0 " 
            + "and   id_p_seqno = ?  "
            + "order by end_tran_bp "
            ;

  setString(1 , idPSeqno);
  openCursor();

  while( fetchTable() )
    {
     dBonusFunc(getValue("tran_seqno"));
    }
  closeCursor();
  setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public  int dBonusFunc(String tranSeqno) throws Exception
 {
  if (!DEBUG)
  setConsoleMode("N");
  updateCnt=0;
  initData();
  extendField = "tran.";
  selectSQL = "end_tran_bp, "
            + "tax_tran_bp, "
            + "id_p_seqno, "
            + "acct_type, "
            + "bonus_type, "
            + "tax_flag, "
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

   hMbdlIdPSeqno  =  getValue("tran.id_p_seqno");
   hMbdlAcctType   =  getValue("tran.acct_type");
   hMbdlBonusType  =  getValue("tran.bonus_type");
   if ( hMbdlBonusType.length()==0)  hMbdlBonusType="BONU";
   hMbdlTaxFlag    = getValue("tran.tax_flag");
   hMbdlEndTranBp = getValueInt("tran.end_tran_bp");

   mainSeqno = tranSeqno;
   mainTranBp = 0;
   hMbdlTranBp = selectDbmBonusDtlM(hMbdlEndTranBp);

   linkSeqno = "";
   linkTranBp = 0;
   if ((mainSeqno.length()!=0)&&
       (mainTranBp!=0))
      {
       linkSeqno = mainSeqno;
       linkTranBp = mainTranBp;
      }

   if (hMbdlTranBp!=hMbdlEndTranBp)
      {
      if (hMbdlEndTranBp<0)
         updateDbmBonusDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"),
                              getValueInt("tran.tax_tran_bp")+tranBpTax*-1);
      else
         updateDbmBonusDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"));
      }

  setConsoleMode("Y");
  return(updateCnt);
 }
// ************************************************************************
 public  int selectDbmBonusDtlM(int tempTranBp) throws Exception
 {
  int oriTempTranBp = tempTranBp;
  if (!DEBUG)
  setConsoleMode("N");
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno, "
            + "tran_code, "
            + "end_tran_bp, "
            + "tax_flag, "
            + "rowid as rowid ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  end_tran_bp  != 0 " 
            + "and    apr_flag      = 'Y' "  
            + "and    bonus_type    = ? "
            + "and    id_p_seqno    = ? "
            + "and    acct_type     = ? "
            + "and    decode(sign(end_tran_bp),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) ";

  if (tempTranBp<0)
     {
      if (hMbdlTaxFlag.equals("Y"))
         whereStr  = whereStr + " order by  decode(tax_flag,'Y',1,2),"
                              + "decode(effect_e_date,'','99999999',effect_e_date),"
                              + "tran_date,tran_time ";
      else if (hMbdlTaxFlag.equals("N"))
         whereStr  = whereStr + " order by  decode(tax_flag,'Y',2,1),"
                              + "decode(effect_e_date,'','99999999',effect_e_date),"
                              + "tran_date,tran_time ";
      else
         whereStr  = whereStr + " order by  decode(effect_e_date,'','99999999',effect_e_date),"
                              + "tax_flag,tran_date,tran_time ";
     }
  else
     {
      if (hMbdlTaxFlag.equals("Y"))
         whereStr  = whereStr + " order by  decode(tax_flag,'Y',1,2),"
                              + "decode(effect_e_date,'','99999999',effect_e_date),"
                              + "tran_date,tran_time ";
      else
         whereStr  = whereStr + " order by  decode(effect_e_date,'','99999999',effect_e_date),"
                              + "tax_flag,tran_date,tran_time ";
     }

  setString(1,hMbdlBonusType);
  setString(2,hMbdlIdPSeqno);
  setString(3,hMbdlAcctType);
  setInt(4,tempTranBp);

  int recCnt = selectTable();

  if (recCnt==0) return(tempTranBp); 

  hCommTranBp      = 0;
  hCommTranBpTax  = 0;

  hMbdlPTranBp  = 0;
  hMbdlMTranBp  = 0;
  int thisTaxBp = 0;

  for ( int inti=0; inti<recCnt; inti++ )
    {
     thisTaxBp = 0;
     if (tempTranBp<0) hBklgTaxFlag = getValue("com.tax_flag",inti);
   if (DEBUG)
      {
       showLogMessage("I","","STEP 2 要沖銷金額  : ["+ tempTranBp + "]");
       showLogMessage("I","","       可沖銷金額  : ["+ getValueInt("com.end_tran_bp",inti) + "]");
       showLogMessage("I","","       應免稅註記  : ["+ hBklgTaxFlag + "]");
       showLogMessage("I","","       tran_bp_notax    : ["+ tranBpNotax + "]");
       showLogMessage("I","","       tran_bp_tax    : ["+ tranBpTax + "]");
      }

     if (hBklgTaxFlag.equals("Y"))
        {
         hCommTranBpTax  = hCommTranBpTax
                             + Math.abs(tempTranBp + getValueInt("com.end_tran_bp",inti));
        }
     else
        {
         hCommTranBp      = hCommTranBp
                             + Math.abs(tempTranBp + getValueInt("com.end_tran_bp",inti));
        }
     if (tempTranBp > 0)
        {
         hMbdlPTranBp  = tempTranBp;
         hMbdlMTranBp  = getValueInt("com.end_tran_bp",inti);
        }
     else
        {
         hMbdlMTranBp  = tempTranBp;
         hMbdlPTranBp  = getValueInt("com.end_tran_bp",inti);
        }

     if (tempTranBp > 0)
        {
         if (hMbdlPTranBp + hMbdlMTranBp>0)
            {
             if (hMbdlTaxFlag.equals("Y"))
                {
                 tranBpTax = tranBpTax + hMbdlTranBp;
                 thisTaxBp = hMbdlTranBp;
                }
             else
                tranBpNotax = tranBpNotax + hMbdlTranBp;
             hMbdlTranBp =  0;
             tempTranBp   = hMbdlPTranBp + hMbdlMTranBp;
            }
         else
            {
             if (hMbdlTaxFlag.equals("Y"))
                {
                 tranBpTax = tranBpTax + tempTranBp;
                 thisTaxBp = tempTranBp;
                }
             else
                tranBpNotax = tranBpNotax + tempTranBp;
             hMbdlTranBp =  hMbdlPTranBp + hMbdlMTranBp;
             tempTranBp   = 0;
            }
        }
     else
        {
         if (hMbdlPTranBp + hMbdlMTranBp<0)
            {
             if (getValue("com.tax_flag",inti).equals("Y"))
                {
                 tranBpTax = tranBpTax - hMbdlPTranBp;
                 thisTaxBp = hMbdlPTranBp;
                }
             else
                tranBpNotax = tranBpNotax - hMbdlPTranBp;
             hMbdlTranBp = 0;
             tempTranBp   = hMbdlPTranBp + hMbdlMTranBp;
            }
         else
            {
             if (getValue("com.tax_flag",inti).equals("Y"))
                {
                 tranBpTax = tranBpTax + hMbdlMTranBp;
                 thisTaxBp = hMbdlMTranBp;
                }
             else
                tranBpNotax = tranBpNotax + hMbdlMTranBp;

             hMbdlTranBp =  hMbdlPTranBp + hMbdlMTranBp;
             tempTranBp   = 0;
            }
        }

     if (hMbdlTranBp==0)
        {
         linkTranBp = getValueInt("com.end_tran_bp",inti) 
                      - hMbdlTranBp;
         linkSeqno = mainSeqno;
        }
     else
        {    
         linkTranBp = 0;
         linkSeqno = "";
         mainTranBp = 0 - (getValueInt("com.end_tran_bp",inti) - hMbdlTranBp);
         mainSeqno = getValue("com.tran_seqno",inti);
        }  


     if (oriTempTranBp>0)
        updateDbmBonusDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti),thisTaxBp);
     else
       updateDbmBonusDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti));


     if (tempTranBp==0) break;
    }

  return(tempTranBp); 
 }
// ************************************************************************
 public int updateDbmBonusDtl(String tempRowid,double tempModSeqno) throws Exception
 {
  return(updateDbmBonusDtl(tempRowid,tempModSeqno,0));
 }
// ************************************************************************
 public int updateDbmBonusDtl(String tempRowid,double tempModSeqno,int taxBp) throws Exception
 {
  if (!DEBUG)
  setConsoleMode("N");
  dateTime();
  updateSQL = "end_tran_bp = ?, "
            + "tax_tran_bp  = ?, "
            + "link_seqno   = ?, "
            + "link_tran_bp = ?, "
            + "mod_pgm     = ?, "
            + "mod_seqno   = mod_seqno + 1 , "
            + "mod_time    = sysdate";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setInt(1 , hMbdlTranBp);
  setInt(2 , taxBp);
  setString(3 , linkSeqno);
  setInt(4 , linkTranBp);
  if (modPgm.length()==0)
     setString(5 , javaProgram);
  else
     setString(5 , modPgm);
  setRowId(6  , tempRowid);
  setDouble(7 , tempModSeqno);

  int cnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE dbm_bonus_dtl rror "+getValue("rowid"));
      exitProgram(1);
     }

  return(0);
 }
// ************************************************************************
 public double bonusSum(String idPSeqno)  throws Exception
 {
  return bonusSum("90",idPSeqno,"BONU");
 }
// ************************************************************************
 public double bonusSum(String acctType,String idPSeqno,String bonusType)  throws Exception
 {
  setConsoleMode("N");
  selectSQL = "sum(end_tran_bp) as end_tran_bp ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where id_p_seqno     = ? "
            + "and   acct_type      = ? "
            + "and   bonus_type  = ? ";

  setString(1,idPSeqno);
  setString(2,acctType);
  setString(3,bonusType);

  int recCnt = selectTable();

  if (recCnt==0) return(0);
  return(getValueDouble("end_tran_bp"));
 }
// ************************************************************************
 public int dBonusReverse(String tranSeqno) throws Exception
 {
  int intq=0;
  String[] tranQueue = new String[300];   // 0032838956  0032847705 0032851234
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectDbmBonusDtlQueue(tranQueue[intq]);
     if (tranQueue[intq+1].length()==0) break;
     intq++;
    }

  for (int inti=intq;inti>=0;inti--)
    {  
     dBonusReverseM(tranQueue[inti]);
    }
   
  return(0);
 }
// ************************************************************************
 int dBonusReverseM(String tranSeqno) throws Exception
 {
  updateCnt=0;
  comr = new CommRoutine(getDBconnect(),getDBalias());
  extendField = "tran.";
  selectSQL = "active_code, "
            + "active_name,"  
            + "acct_date,"  
            + "beg_tran_bp,"  
            + "end_tran_bp,"  
            + "tax_tran_bp,"  
            + "tran_seqno,"  
            + "acct_type, "
            + "mod_desc, "
            + "id_p_seqno, "
            + "bonus_type, "
            + "tax_flag, "
            + "link_seqno, "
            + "link_tran_bp,"
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

  mainTranBp = 0;
  linkTaxTranBp = 0;

  if (getValue("tran.link_seqno").length()>0)
     {
      if (getValueInt("tran.beg_tran_bp")<0)
         {
          if (getValueInt("tran.beg_tran_bp")>getValueInt("tran.link_tran_bp"))
              setValueInt("tran.link_tran_bp",getValueInt("tran.beg_tran_bp"));
         }
      else
         {
          if (getValueInt("tran.beg_tran_bp")<getValueInt("tran.link_tran_bp"))
              setValueInt("tran.link_tran_bp",getValueInt("tran.beg_tran_bp"));
         }
     }

  if (getValueInt("tran.beg_tran_bp") == getValueInt("tran.end_tran_bp")) return(0);
  selectDbmBonusDtlR1();

  if (getValue("tran.link_seqno").length()!=0)
      selectDbmBonusDtlR2();
  
  updateDbmBonusDtlR2(getValueInt("tran.beg_tran_bp"),
                          getValueInt("tran.tax_tran_bp")-linkTaxTranBp);
     
  if (getValueInt("tran.beg_tran_bp") - getValueInt("tran.end_tran_bp") != mainTranBp)
     {
     if (getValueInt("tran.beg_tran_bp") <0)
        {
         selectDbmSysparm();
        }
      selectPtrBusinday();

      int begTranBp = getValueInt("tran.beg_tran_bp")
                      - getValueInt("tran.end_tran_bp")
                      - mainTranBp;

      int taxTranBp =  getValueInt("tran.tax_tran_bp")
                      - linkTaxTranBp;

      setValue("tax_flag" , "N");
      if (taxTranBp!=0) setValue("tax_flag" , "Y");

      if ((begTranBp==0)||  
          (begTranBp + taxTranBp==0))
         {
          insertDbmBonusDtl(begTranBp*-1,taxTranBp);
         }
      else
         {
          if (getValueInt("tran.beg_tran_bp")>0)
             {
              insertDbmBonusDtl(begTranBp*-1,0);
             }
          else
             {
              if (taxTranBp!=0)
                 {
                  insertDbmBonusDtl(taxTranBp,taxTranBp);
                 }
              setValue("tax_flag"    , "N");
              insertDbmBonusDtl((begTranBp + taxTranBp)*-1,0);
             }
         }
     }
  return(updateCnt);
 }
// ************************************************************************
 int selectDbmBonusDtlR1() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno,"
            + "link_tran_bp,"
            + "tax_flag, "
            + "tax_tran_bp, "
            + "beg_tran_bp, "
            + "end_tran_bp, "
            + "rowid as rowid ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where link_seqno  = ? "
            + "and   id_p_seqno     = ? " 
            ;

  setString(1,getValue("tran.tran_seqno"));
  setString(2,getValue("tran.id_p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(0); 

  for ( int inti=0; inti<recCnt; inti++ )
    {
     if (getValue("com.link_seqno",inti).length()>0)
        {
         if (getValueInt("com.beg_tran_bp",inti)<0)
            {
             if (getValueInt("com.beg_tran_bp",inti)>getValueInt("com.link_tran_bp",inti))
                 setValueInt("com.link_tran_bp",getValueInt("com.beg_tran_bp",inti),inti);
            }
         else
            {
             if (getValueInt("com.beg_tran_bp",inti)<getValueInt("com.link_tran_bp",inti))
                 setValueInt("com.link_tran_bp",getValueInt("com.beg_tran_bp",inti),inti);
            }
        }
     mainTranBp = mainTranBp - getValueInt("com.link_tran_bp",inti);

     if (getValueInt("tran.beg_tran_bp")<0)
     if (getValue("com.tax_flag",inti).equals("Y"))
        {
         linkTaxTranBp = linkTaxTranBp 
                          + getValueInt("com.link_tran_bp",inti);
        }
     setValueInt("end_tran_bp"  , getValueInt("com.end_tran_bp",inti)
                                + getValueInt("com.link_tran_bp",inti));
                                 
     rTaxTranBp = 0;
     if (getValueInt("com.beg_tran_bp",inti)<0)
     if (getValue("tran.tax_flag").equals("Y"))
        {
         rTaxTranBp = getValueInt("com.tax_tran_bp",inti) 
                       + getValueInt("com.link_tran_bp",inti);  
        }

     updateDbmBonusDtlR1(inti);
    }

  return(0); 
 }
// ************************************************************************
 int selectDbmBonusDtlR2() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "link_tran_bp,"
            + "tax_flag, "
            + "tax_tran_bp, "
            + "beg_tran_bp, "
            + "end_tran_bp, "
            + "tax_flag, "
            + "rowid as rowid ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  tran_seqno = ? "
            + "and    id_p_seqno = ? " 
            ;

  setString(1,getValue("tran.link_seqno"));
  setString(2,getValue("tran.id_p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(1); 

  if (getValue("com.link_seqno").length()>0)
     {
      if (getValueInt("com.beg_tran_bp")<0)
         {
          if (getValueInt("com.beg_tran_bp")>getValueInt("com.link_tran_bp"))
              setValueInt("com.link_tran_bp",getValueInt("com.beg_tran_bp"));
         }
      else
         {
          if (getValueInt("com.beg_tran_bp")<getValueInt("com.link_tran_bp"))
              setValueInt("com.link_tran_bp",getValueInt("com.beg_tran_bp"));
         }
     }

  mainTranBp = mainTranBp + getValueInt("tran.link_tran_bp");

  if (getValueInt("tran.beg_tran_bp")<0)
  if (getValue("com.tax_flag").equals("Y"))
     {
      linkTaxTranBp = linkTaxTranBp 
                       - getValueInt("tran.link_tran_bp");
     }
  setValueInt("end_tran_bp"  , getValueInt("com.end_tran_bp")
                             - getValueInt("tran.link_tran_bp"));
                              
  rTaxTranBp = 0;
  if (getValueInt("com.beg_tran_bp")<0)
  if (getValue("tran.tax_flag").equals("Y"))
     {
      rTaxTranBp = getValueInt("com.tax_tran_bp")
                    + getValueInt("com.link_tran_bp");
     }

  updateDbmBonusDtlR1(0);

  return(0); 
 }
// ************************************************************************
 int updateDbmBonusDtlR1(int inti) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp  = ?, "
            + "link_seqno   = '',"
            + "tax_tran_bp  = ?, "
            + "link_tran_bp = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setInt(1    , getValueInt("end_tran_bp"));
  setInt(2    , rTaxTranBp);
  setString(3 , javaProgram);
  setRowId(4  , getValue("com.rowid",inti));
  setDouble(5 , getValueDouble("com.mod_seqno",inti)); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int updateDbmBonusDtlR2(int endTranBp,int taxTranBp) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp  = ?, "
            + "tax_tran_bp  = ?,"
            + "link_seqno   = '',"
            + "link_tran_bp = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE rowid = ? "
            + "AND mod_seqno   = ? ";

  setInt(1    , endTranBp);
  setInt(2    , taxTranBp);
  setString(3 , javaProgram);
  setRowId(4  , getValue("tran.rowid"));
  setDouble(5 , getValueDouble("tran.mod_seqno")); 

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertDbmBonusDtl(int endTranBp,int taxTranBp) throws Exception
 {
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , getSeqno("ECS_DBMSEQ"));

  setValue("mbdl.active_code"          , getValue("tran.active_code"));
  setValue("mbdl.active_name"          , getValue("tran.active_name") + "沖回調整");
  setValue("mbdl.effect_e_date"        , "");
  if (getValueInt("parm.effect_months")>0)
     setValue("mbdl.effect_e_date"     , comm.nextMonthDate(getValue("tran.acct_date")
                                       , getValueInt("parm.effect_months")));
  setValue("mbdl.tax_flag"             , getValue("tax_flag"));
  setValue("mbdl.mod_desc"             , getValue("tran.mod_desc")); 
  setValue("mbdl.mod_memo"             , "tran_seqno:["+getValue("tran.tran_seqno")+"]");
  setValueInt("mbdl.beg_tran_bp"       , endTranBp);
  setValueInt("mbdl.end_tran_bp"       , endTranBp);
  setValueInt("mbdl.tax_tran_bp"       , taxTranBp);
  setValue("mbdl.tran_code"            , "3");
  setValue("mbdl.bonus_type"           , getValue("tran.bonus_type"));
  setValue("mbdl.acct_date"            , getValue("busi.business_date"));
  setValue("mbdl.proc_month"           , getValue("busi.business_date").substring(0,6));
  setValue("mbdl.acct_type"            , getValue("tran.acct_type"));
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
  daoTable  = "dbm_bonus_dtl";

  insertTable();

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
 String selectDbmBonusDtlQueue(String tranSeqno) throws Exception
 {
  extendField = "queu.";
  selectSQL = "link_seqno";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return("");

  return(getValue("queu.link_seqno")); 
 }
// ************************************************************************
 int selectDbmSysparm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "effect_months";
  daoTable  = "dbm_sysparm";
  whereStr  = "where parm_type = '01' ";

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      setValue("parm.effect_months" ,"36");
      return(1);
     }

  return(0);
 }
// ************************************************************************
 void initData() throws Exception
 {
  hCommTranBp      = 0;
  hCommTranBpTax  = 0;
  tranBpTax  = 0;
  tranBpNotax = 0;


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

}   // End of class CommBonus

