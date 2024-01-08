/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/06  V1.00.00  Allen Ho                                              *
* 109-12-04  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktA780 extends AccessDAO
{
 private  String progname = "紅利-指定活動無效卡達三個月歸零處理程式 109/12/04 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hBusiBusinessDate  = "";
 String tranSeqno     = "";

 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA780 proc = new MktA780();
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
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   loadMktBnData();
   loadCrdCard();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","移除 指定活動無效卡 資料"); 
   selectMktBonusDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 移除 ["+updateCnt+"] 筆");
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************ 
 void  selectMktBonusDtl() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.active_code,"
            + "sum(a.end_tran_bp) as end_tran_bp, "
            + "max(a.bonus_type) as bonus_type,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "max(a.acct_type) as acct_type,"
            + "max(a.active_name) as active_name," 
            + "sum(decode(a.tax_flag,'Y',end_tran_bp,0)) as tax_tran_bp";
  daoTable  = "mkt_bonus_dtl a,mkt_bpmh3 b";
  whereStr  = "where a.active_code = b.active_code "
            + "and   b.group_oppost_cond = 'Y' "
            + "and   b.group_card_sel = '1' "
            + "and   a.end_tran_bp != 0 "
            + "group by a.p_seqno,a.active_code "
            + "having sum(a.end_tran_bp) != 0 "
            ;
  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   {
    totalCnt++;
   
    setValue("card.p_seqno" , getValue("p_seqno"));
    int cnt1 = getLoadData("card.p_seqno");
    if (cnt1==0)
       {
//      showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"] not card error");
   
        insertMktBonusDtl();
        updateMktBonusDtl();
   
        updateCnt++;
        continue;
       }
    int okFlag=0;
    for (int inti=0;inti>cnt1;inti++)
      {
       if (selectMktBnData(getValue("card.group_code",inti),getValue("card.card_type",inti),
                                        "1","2",3)!=0) continue;

//     if (comm.nextMonthDate(getValue("card.oppost_date",inti),3).compareTo(h_busi_business_date)<0) continue;
       okFlag=1;
       break;
      }
    if (okFlag==1) continue;

//  showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"]");

    insertMktBonusDtl();
    updateMktBonusDtl();
   
    updateCnt++;
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  extendField = "mbdl.";

  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.acct_date"            , hBusiBusinessDate);
  setValue("mbdl.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mbdl.bonus_type"           , getValue("bonus_type"));
  setValue("mbdl.p_seqno"              , getValue("p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("mbdl.acct_type"            , getValue("acct_type"));
  setValue("mbdl.tran_code"            , "6");
  setValue("mbdl.active_code"          , getValue("active_code"));
  setValue("mbdl.active_name"          , getValue("active_name"));
  setValue("mbdl.mod_desc"             , "指定活動無效卡達三個月");
  setValue("mbdl.mod_memo"             , "");
  setValue("mbdl.tax_flag"             , "N");
  setValueInt("mbdl.tax_tran_bp"       , getValueInt("tax_tran_bp"));
  setValueInt("mbdl.beg_tran_bp"       , getValueInt("end_tran_bp")*-1);
  setValue("mbdl.end_tran_bp"          , "0"); 
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.mod_user"             , javaProgram); 
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktBonusDtl() throws Exception
 {
  dateTime();
  updateSQL = "effect_flag    = 'Y', "
            + "remove_date    = ?, "
            + "mod_memo       = ?,"
            + "link_seqno     = ?,"
            + "link_tran_bp   = end_tran_bp,"
            + "end_tran_bp    = 0,"
            + "mod_pgm        = ?, "
            + "mod_time       = sysdate";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE p_seqno   =  ? "
            + "and active_code = ? "
            + "and end_tran_bp != 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+tranSeqno+"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("active_code"));

  updateTable();
  return;
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
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_BPMH3' "
            + "and   data_type =  '2' "
            + "and   data_key in ("
            + "      select active_code "
            + "      from   mkt_bpmh3 "
            + "      where group_oppost_cond = 'Y') "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "group_code,"
            + "card_type,"
            + "decode(oppost_date,'','30001231',oppost_date) as oppost_date";
  daoTable  = "crd_card a,"
            + "     (select distinct p_seqno as p_seqno" 
            + "      from mkt_bonus_dtl c,mkt_bpmh3 d "
            + "      where c.active_code = d.active_code "
            + "      and   d.group_oppost_cond = 'Y' "
            + "      and   d.group_card_sel = '1' "
            + "      and   c.end_tran_bp != 0) b";
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and   a.major_card_no = a.card_no "
            + "and   decode(oppost_date,'','30001231',oppost_date) > ? "
//          + "order by a.p_seqno "
            ;

  setString(1 , comm.nextMonthDate(hBusiBusinessDate , -3));

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card : ["+n+"]");
 }
// ************************************************************************



}  // End of class FetchSample
