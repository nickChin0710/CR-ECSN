/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/09/17  V1.00.38  Allen Ho   CommBonus initial                          *
* 110/09/30  V1.01.02  Allen Ho   DEBUG error                                *
* 111/11/05  V1.01.03  Zuwei Su   sync from mega & coding standard update    *                                                                           *
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.Math;

public class CommBonus extends AccessDAO
{
 private final String PROGNAME = "111/11/05  V1.01.03";
 
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
// 已下3欄位只有需要知充抵應稅即免稅多少時用
  public int    hCommTranBp      = 0; 
  public int    hCommTranBpTax  = 0; 
  public String dispFlag      = "N";
  public String modPgm       = "";
  public String debugPgm       = "";
  public int    tranBpTax   = 0; 
  public int    tranBpNotax = 0;

  String hMbdlPSeqno     = "";
  String hMbdlBonusType  = "";
  String hBklgTaxFlag    = "";
  String hMbdlTaxFlag    = "";
  String hMbdlTranCode   = "";
  int    hMbdlEndTranBp = 0;
  int    hMbdlTranBp     = 0;
  int    hMbdlPTranBp  = 0;
  int    hMbdlMTranBp  = 0;
  final boolean DEBUG   = false;
  int    updateCnt     = 0;
  int    linkTranBp      = 0;
  int    linkTaxTranBp  = 0;
  int    mainTranBp      = 0;
  int    rTaxTranBp     = 0;
  String linkSeqno = "";
  String mainSeqno = "";
  String oriMainSeqno  = "";
  String oriActiveCode = "";
  String oriActiveName = "";
  String oriAcctDate   = "";

  String[] DBNAME = new String[10];
  int intq = 0;
// ************************************************************************
 public CommBonus(Connection conn[],String[] dbAlias) throws Exception
 {
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];
   javaProgram = "CommBonus";

   return;
 }
// ************************************************************************
 public int bonusFunc() throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      dispFlag = "N";
     }
  selectSQL = "max(tran_seqno) as tran_seqno ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  end_tran_bp != 0 " 
            + "group by p_seqno,bonus_type "
            + "having sum(decode(sign(end_tran_bp),1,end_tran_bp,0))!=0 " 
            + "and    sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0 "
            ;

  openCursor();

  while( fetchTable() )
    {
     bonusFunc(getValue("tran_seqno"));
    }
  closeCursor();
//setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public int bonusFuncP(String pSeqno) throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      dispFlag = "N";
     }
  selectSQL = "tran_seqno";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  end_tran_bp != 0 " 
            + "and   p_seqno = ?  "
            + "order by end_tran_bp "
            ;

  setString(1 , pSeqno);
  openCursor();

  while( fetchTable() )
    {
     bonusFunc(getValue("tran_seqno"));
    }
  closeCursor();
//setConsoleMode("Y");
  return(0); 
 }
// ************************************************************************
 public int bonusFunc(String tranSeqno) throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      dispFlag = "N";
     }

  initData();
  updateCnt=0;
  extendField = "tran.";
  selectSQL = "end_tran_bp, "
            + "beg_tran_bp, "
            + "tax_tran_bp, "
            + "p_seqno, "
            + "tran_code, "
            + "bonus_type, "
            + "tax_flag, "
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  tran_seqno = ? "
            + "order by tran_date desc"
            ; 
            

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);


   hMbdlTranCode   = getValue("tran.tran_code");
   hMbdlPSeqno     = getValue("tran.p_seqno");
   hMbdlBonusType  = getValue("tran.bonus_type");
   hMbdlTaxFlag    = getValue("tran.tax_flag");
   hMbdlEndTranBp = getValueInt("tran.end_tran_bp");


   if (DEBUG)
       showLogMessage("I","","STEP 1 p_seqno     : ["+ getValue("tran.p_seqno") + "]");

   mainSeqno = tranSeqno;
   mainTranBp = 0;

   hMbdlTranBp = selectMktBonusDtlM(hMbdlEndTranBp);

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
         updateMktBonusDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"),
                              getValueInt("tran.tax_tran_bp")+tranBpTax*-1);
      else
         updateMktBonusDtl(getValue("tran.rowid"),getValueDouble("tran.mod_seqno"));
      }
  return(updateCnt);
 }
// ************************************************************************
 int selectMktBonusDtlM(int tempTranBp) throws Exception
 {
  int oriTempTranBp = tempTranBp;
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno, "
            + "tran_code, "
            + "end_tran_bp, "
            + "tax_flag, "
            + "rowid as rowid ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  end_tran_bp  != 0 " 
            + "and    apr_flag      = 'Y' "  
            + "and    bonus_type    = ? "
            + "and    p_seqno       = ? "
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
  setString(2,hMbdlPSeqno);
  setInt(3,tempTranBp);

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
       showLogMessage("I","","STEP 2 要衝銷金額  : ["+ tempTranBp + "]");
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
                 tranBpTax = tranBpTax - hMbdlMTranBp;  // 20200320 modify no know right or not
                 thisTaxBp = 0 - hMbdlMTranBp;
                }
             else
                {
                 tranBpNotax = tranBpNotax + hMbdlTranBp;
                }

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

   if (DEBUG)
      {
       showLogMessage("I","","STEP 3 temp_tran_bp  : ["+ tempTranBp + "]");
       showLogMessage("I","","       mod_seqno     : ["+ getValueDouble("com.mod_seqno",inti) + "]");
       showLogMessage("I","","       tran_bp_notax    : ["+ tranBpNotax + "]");
       showLogMessage("I","","       tran_bp_tax    : ["+ tranBpTax + "]");
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
        updateMktBonusDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti),thisTaxBp);
     else
       updateMktBonusDtl(getValue("com.rowid",inti),getValueDouble("com.mod_seqno",inti));

     if (tempTranBp==0) break;
    }

  return(tempTranBp); 
 }
// ************************************************************************
 int updateMktBonusDtl(String tempRowid,double tempModSeqno) throws Exception
 {
  return(updateMktBonusDtl(tempRowid,tempModSeqno,0));
 }
// ************************************************************************
 int updateMktBonusDtl(String tempRowid,double tempModSeqno,int taxBp) throws Exception
 {
  if (!DEBUG)
  dateTime();
  updateSQL = "end_tran_bp  = ?, "
            + "tax_tran_bp  = ?, "
            + "link_seqno   = ?, "
            + "link_tran_bp = ?, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "mkt_bonus_dtl";
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
      showLogMessage("I","","UPDATE mkt_bonus_dtl rror "+getValue("rowid"));
      exitProgram(1);
     }

  updateCnt++;
  return(0);
 }
// ************************************************************************
 public double bonusSum(String pSeqno)  throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      dispFlag = "N";
     }
  return(bonusSum(pSeqno,"BONU"));
 }
// ************************************************************************
 public double bonusSum(String pSeqno,String bonusType)  throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      dispFlag = "N";
     }
  selectSQL = "sum(end_tran_bp+res_tran_bp) as end_tran_bp ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where p_seqno     = ? " 
            + "and   bonus_type  = ? ";

  setString(1,pSeqno);
  setString(2,bonusType);

  int recCnt = selectTable();

  if (recCnt==0) return(0);
  return(getValueDouble("end_tran_bp"));
 }
// ************************************************************************
 public int bonusReverse(String tranSeqno) throws Exception
 {
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[CommBonus Version = ["+ PROGNAME + "]");
      if (!modPgm.equals(debugPgm))
         dispFlag = "N";
     }

  intq=0;
  String[] tranQueue = new String[300];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectMktBonusDtlQueue(tranQueue[intq]);
     if (tranQueue[intq+1].length()==0) break;
     intq++;
    }
  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","[DEBUG] tran_seqno : ["+ tranSeqno + "]");
      showLogMessage("I","","[DEBUG] intq : ["+ intq + "]");
      for (int inti=0;inti<=intq;inti++)
         showLogMessage("I","","[DEBUG] Tran_seqno queue : ["+ inti + "][" + tranQueue[inti] + "]");
     }

  
  for (int inti=intq;inti>=0;inti--)
    {  
     if (dispFlag.equals("Y"))
        showLogMessage("I","","[DEBUG] RUN queue : ["+ inti +"][" + tranQueue[inti] + "]");
     bonusReverseM(tranQueue[inti],inti);
    }
   
  return(0);
 }
// ************************************************************************
 int bonusReverseM(String tranSeqno,int qCnt) throws Exception
 {
  if (dispFlag.equals("Y"))
     showLogMessage("I","","[DEBUG] Bonus_Reverse_M : ["+ tranSeqno + "][" + qCnt + "]");
  
  updateCnt=0;
  comr = new CommRoutine(getDBconnect(),getDBalias());
  extendField = "tran.";
  selectSQL = "active_code, "
            + "active_name,"  
            + "acct_date,"  
            + "beg_tran_bp,"  
            + "end_tran_bp,"  
            + "tax_tran_bp,"  
            + "res_tran_bp,"  
            + "tran_seqno,"  
            + "acct_type, "
            + "mod_desc, "
            + "p_seqno, "
            + "id_p_seqno, "
            + "bonus_type, "
            + "tax_flag, "
            + "link_seqno, "
            + "link_tran_bp,"
            + "mod_seqno, "
            + "rowid as rowid ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(0);

  if (qCnt==0) 
     {
      oriActiveCode = getValue("tran.active_code");
      oriActiveName = getValue("tran.active_name");
      oriAcctDate   = getValue("tran.acct_date");
      if (dispFlag.equals("Y"))
         {
          showLogMessage("I","","[DEBUG] ori_active_code : ["+ oriActiveCode +  "]");
          showLogMessage("I","","[DEBUG] ori_active_name : ["+ oriActiveName + "]");
          showLogMessage("I","","[DEBUG] ori_acct_date   : ["+ oriAcctDate   + "]");
         }
     }
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
   if (dispFlag.equals("Y"))
      showLogMessage("I","","[DEBUG] tran.link_tran_bp : ["+ getValueInt("tran.link_tran_bp") +  "]");
   
  if (getValueInt("tran.beg_tran_bp") == 
      getValueInt("tran.end_tran_bp") + getValueInt("tran.res_tran_bp")) return(0);

  selectMktBonusDtlR1();   // get main_tran_bp

  if (getValue("tran.link_seqno").length()!=0)
      selectMktBonusDtlR2();

  if (intq==qCnt)
     updateMktBonusDtlR2(getValueInt("tran.end_tran_bp") + mainTranBp,      // 如果部分非新系統沖銷不應列入 
                                 getValueInt("tran.tax_tran_bp")-linkTaxTranBp);
  else 
     updateMktBonusDtlR2(getValueInt("tran.beg_tran_bp"),      // 無論如何通通還原
                                 getValueInt("tran.tax_tran_bp")-linkTaxTranBp);

  if (dispFlag.equals("Y"))
     {
      showLogMessage("I","","STEP_1 20200330 tran_beg_bp ori  : ["+ getValueInt("tran.beg_tran_bp") + "]");
      showLogMessage("I","","STEP_2 20200330 end_tran_bp ori  : ["+ getValueInt("tran.end_tran_bp") + "]");
      showLogMessage("I","","STEP_3 20200330 tax_tran_bp ori  : ["+ getValueInt("tran.tax_tran_bp") + "]");
      showLogMessage("I","","STEP_4 20200330 main_tran_bp     : ["+ mainTranBp               + "]");
      showLogMessage("I","","STEP_5 20200330 tax_tran_bp      : ["+ linkTaxTranBp + "]");
     }

  if (intq!=qCnt)
  if (getValueInt("tran.beg_tran_bp") - getValueInt("tran.end_tran_bp") 
                                      - getValueInt("tran.res_tran_amt") != mainTranBp)
     {
     if (getValueInt("tran.beg_tran_bp") <0)
        {
         if (getValue("tran.active_code").length()==0)
             setValue("active_code", getValue("tran.acct_date").substring(0,4)
                                   + getValue("tran.bonus_type")
                                   + getValue("tran.acct_type")
                                   + "1");
         else setValue("active_code", getValue("tran.active_code"));

         selectVmktBonusActiveName();
        }
      selectPtrBusinday();

      int begTranBp = getValueInt("tran.beg_tran_bp")           
                      - getValueInt("tran.end_tran_bp")
                      - getValueInt("tran.res_tran_bp")
                      - mainTranBp;

      int taxTranBp =  getValueInt("tran.tax_tran_bp")
                      - linkTaxTranBp;

      setValue("tax_flag" , "N");
      if (taxTranBp!=0) setValue("tax_flag" , "Y");

      if ((begTranBp==0)||  
          (begTranBp + taxTranBp==0))
         {
          if (dispFlag.equals("Y"))
             showLogMessage("I","","[DEBUG] insert bonus 01 : ["+ begTranBp*-1 +"][" + taxTranBp +  "]");

          insertMktBonusDtl(begTranBp*-1,taxTranBp);
         }
      else
         {
          if (getValueInt("tran.beg_tran_bp")>0)
             {
              if (dispFlag.equals("Y"))
                 showLogMessage("I","","[DEBUG] insert bonus 02 : ["+ begTranBp*-1 + "]");
              insertMktBonusDtl(begTranBp*-1,0);
             }
          else
             {
              if (taxTranBp!=0)
                 {
          if (dispFlag.equals("Y"))
             showLogMessage("I","","[DEBUG] insert bonus 03 : ["+ taxTranBp +"][" + taxTranBp +  "]");
                  insertMktBonusDtl(taxTranBp,taxTranBp);
                 }
              setValue("tax_flag"    , "N");
              insertMktBonusDtl((begTranBp + taxTranBp)*-1,0);
          if (dispFlag.equals("Y"))
             showLogMessage("I","","[DEBUG] insert bonus 04 : ["+ (begTranBp + taxTranBp)*-1 +"][" + 0 +  "]");
             }
         }
     }
    
  return(updateCnt);
 }
// ************************************************************************
 int selectMktBonusDtlR1() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "tran_seqno,"
            + "link_seqno,"
            + "link_tran_bp,"
            + "tax_flag, "
            + "tax_tran_bp, "
            + "beg_tran_bp, "
            + "end_tran_bp, "
            + "res_tran_bp, "
            + "rowid as rowid ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where link_seqno  = ? "
            + "and   p_seqno     = ? " 
            ;

  setString(1,getValue("tran.tran_seqno"));
  setString(2,getValue("tran.p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(1); 

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
                                 
     updateMktBonusDtlR1(inti);
    }

  return(0); 
 }
// ************************************************************************
 int selectMktBonusDtlR2() throws Exception
 {
  extendField = "com.";
  selectSQL = "mod_seqno, "
            + "link_seqno,"
            + "link_tran_bp,"
            + "tax_flag, "
            + "tax_tran_bp, "
            + "beg_tran_bp, "
            + "end_tran_bp, "
            + "res_tran_bp, "
            + "tax_flag, "
            + "rowid as rowid ";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  tran_seqno = ? "
            + "and    p_seqno     = ? " 
            ;

  setString(1,getValue("tran.link_seqno"));
  setString(2,getValue("tran.p_seqno"));

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

  updateMktBonusDtlR1(0);

  return(0); 
 }
// ************************************************************************
 int updateMktBonusDtlR1(int inti) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp  = ?, "
            + "link_seqno   = '',"
            + "tax_tran_bp  = ?, "
            + "link_tran_bp = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "mkt_bonus_dtl";
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
 int updateMktBonusDtlR2(int endTranBp,int taxTranBp) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp  = ?, "
            + "tax_tran_bp  = ?,"
            + "link_seqno   = '',"
            + "link_tran_bp = 0, "
            + "mod_pgm      = ?, "
            + "mod_seqno    = mod_seqno + 1 , "
            + "mod_time     = sysdate ";
  daoTable  = "mkt_bonus_dtl";
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
 int insertMktBonusDtl(int endTranBp,int taxTranBp) throws Exception
 {
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , getSeqno("MKT_MODSEQ"));

  setValue("mbdl.active_code"          , getValue("tran.active_code"));
  setValue("mbdl.active_name"          , getValue("tran.active_name") + "衝回調整");
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
  setValue("mbdl.p_seqno"              , getValue("tran.p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("tran.id_p_seqno"));
  setValue("mbdl.tran_pgm"             , modPgm);
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_user"             , modPgm);
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , modPgm);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , modPgm);
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , modPgm);

  extendField = "mbdl.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectVmktBonusActiveName() throws Exception
 {
  extendField = "parm.";
  selectSQL = "active_name,"
            + "effect_months ";
  daoTable  = "vmkt_bonus_active_name";
  whereStr  = "where  active_code = ? "
            ;

  setString(1,getValue("active_code"));

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      setValue("parm.effect_months" ,"36");
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
 String selectMktBonusDtlQueue(String tranSeqno) throws Exception
 {
  extendField = "queu.";
  selectSQL = "link_seqno";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  tran_seqno = ? ";

  setString(1,tranSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return("");

  return(getValue("queu.link_seqno")); 
 }
// ************************************************************************
 void initData() throws Exception
 {
  hCommTranBp      = 0;
  hCommTranBpTax  = 0;
  tranBpTax         = 0;
  tranBpNotax       = 0;

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
