/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/09/07  V1.00.11  Allen Ho   new                                        *
* 110/09/17  V1.01.01  Allen Ho   Bonus_reverse mod_pgm                      *
* 111/12/07  V1.01.02  Zuwei      sync from mega                             *
*                                                                            *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT080 extends AccessDAO
{
 private  String PROGNAME = "高鐵車廂升等-退貨後減免尚有餘額處理程式 111/12/07 V1.01.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate = "";
 String tranSeqno     = "";

 int   totalCnt=0;
 int   cnt1=0;
 int   dataCnt=0,branchCnt=0;
 int   okFlag=0;
 int[] procInt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT080 proc = new MktT080();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
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

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { businessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comb.dispFlag="Y";

   selectPtrBusinday();

   if (!businessDate.substring(6,8).equals("01"))
      {
       showLogMessage("I","","本程式為每月01執行 "+javaProgram);
       return(0);
      }
          
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUpidnoMon();
   showLogMessage("I","","累計處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","    判斷卡片資料       ["+ procInt[0] +"] 筆");
   showLogMessage("I","","      有可減免筆數資料 ["+ procInt[3] +"] 筆");
   showLogMessage("I","","      無使用可減免     ["+ (procInt[3]-procInt[5]-procInt[7]) +"] 筆");
   showLogMessage("I","","      有使用可減免     ["+ (procInt[5]+procInt[7]) +"] 筆");
   showLogMessage("I","","        減免紅利       ["+ procInt[11] +"] 筆");
   showLogMessage("I","","        減免費用成功   ["+ procInt[13] +"] 筆");
   showLogMessage("I","","        減免費用失敗   ["+ procInt[5] +"] 筆");
   showLogMessage("I","","      減免成功筆數     ["+ procInt[7] +"] 筆");
   showLogMessage("I","","    判斷正卡資料       ["+ procInt[1] +"] 筆");
   showLogMessage("I","","      有可減免筆數資料 ["+ procInt[4] +"] 筆");
   showLogMessage("I","","      無使用可減免     ["+ (procInt[4]-procInt[6]-procInt[8]) +"] 筆"); 
   showLogMessage("I","","      有使用可減免     ["+ (procInt[6]+procInt[8]) +"] 筆");
   showLogMessage("I","","        減免紅利       ["+ procInt[12] +"] 筆");
   showLogMessage("I","","        減免費用成功   ["+ procInt[14] +"] 筆");
   showLogMessage("I","","        減免費用失敗   ["+ procInt[6] +"] 筆");
   showLogMessage("I","","      減免成功筆數     ["+ procInt[8] +"] 筆");
   showLogMessage("I","","===============================");

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
 void  selectMktThsrUpidnoMon() throws Exception
 {
  selectSQL = "id_p_seqno";
  daoTable  = "mkt_thsr_upidno_mon";
  whereStr  = "WHERE refund_flag = 'Y' "
            + "and   trans_month    = ? "
            + "group by id_p_seqno "
            ;

  setString(1 , businessDate.substring(0,6));

  openCursor();
  totalCnt = 0;
  int okFlag = 0;
  int existFlag = 0;
  while( fetchTable() ) 
   {
    totalCnt++;

    selectMktThsrUpidno();
    existFlag=0;
    for (int intm=0;intm<branchCnt;intm++)
      {
       if (selectMktThsrUpidnoMon(intm) <=0) continue;
       existFlag=1;
       break;
      }
    if (existFlag==1) continue;

    selectMktThsrUptxn();
    
    procInt[2]++;
   }                
  closeCursor();
 }
// ************************************************************************
 int selectMktThsrUptxn() throws Exception
 {
  extendField = "mtu1.";
  selectSQL = "card_no, "
            + "major_id_p_seqno, "
            + "tran_seqno, "
            + "p_seqno, "
            + "acct_type, "
            + "proc_date, "
            + "group_code, "
            + "card_type, "
            + "trans_date, "
            + "pay_type, "
            + "deduct_amt, "
            + "deduct_bp, "
            + "deduct_bp_tax, "
            + "serial_no, "
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE trans_date like   ? " 
            + "AND   proc_flag    =  'Y' "
            + "AND   refund_flag != 'Y' "
            + "AND   ((pay_type    = '1' "
            + "  and   deduct_bp + deduct_bp_tax >0) "
            + " or    (pay_type    = '2' "
            + "  and   deduct_amt>0)) "
            + "AND   (major_id_p_seqno = ? "
            + " or   id_p_seqno        = ? )"
            + "order by pay_type desc "
            ;
  setString(1,getValue("trans_month")+"%");
  setString(2,getValue("id_p_seqno"));
  setString(3,getValue("id_p_seqno"));

  int recCnt = selectTable();
  int intq =-1;

  for (int inti=0;inti<recCnt;inti++)
    {
     selectMktThsrUpidno();
     intq =-1;
     for (int intm=0;intm<branchCnt;intm++)
       {
        if (selectMktThsrUpidnoMon(intm) <=0) continue;
        intq =intm;
        break;
       }
     if (intq==-1) continue;

     if (!getValue("mtul.major_id_p_seqno",inti).equals(getValue("id_p_seqno")))
        setValue("upidno_type" , "1");
     else
        setValue("upidno_type" , "2");

     if (getValue("mtu1.pay_type",inti).equals("1"))
        {
         if (getValue("upidno_type").equals("1")) procInt[11]++;
         else procInt[12]++;
         okFlag = 0;
         if ((!getValue("mtu1.tran_seqno",inti).equals(""))&&
             (selectMktBonusDtl(inti)==0))
            {
             setValue("bpid.effect_months" , "0");
             comb.modPgm = javaProgram;
             comb.bonusReverse(getValue("mtu1.tran_seqno",inti));
             if (comb.dispFlag.equals("Y")) comb.dispFlag="N";

             updateMktBonusDtl(inti);

             setValueInt("bpid.effect_months" , 0);
             setValue("mbdl.mod_desc"        , "重減免日期:"+getValue("mutl.trans_date",inti)+" 流水號["
                                              + getValue("mutl.serial_no",inti)+"]");
             setValue("mbdl.tax_flag"        , "N");
             setValueInt("mbdl.beg_tran_bp"  , getValueInt("mbd2.beg_tran_bp")*-1);
             setValueInt("mbdl.end_tran_bp"  , 0 );
             insertMktBonusDtl(inti);
            }
         else
            {
             selectCycBpid(inti);
             if (getValueInt("mtu1.deduct_bp",inti) > 0)
                {
                 setValue("mbdl.mod_desc"     , "減免退紅利點數(免稅)");
                 setValue("mbdl.tax_flag"     , "N");
                 setValue("mbdl.beg_tran_bp"  , getValue("mtu1.deduct_bp",inti));
                 setValue("mbdl.end_tran_bp"  , getValue("mtu1.deduct_bp",inti));
                 insertMktBonusDtl(inti);

                 comb.modPgm = javaProgram;
                 comb.bonusFunc(tranSeqno);
                 if (comb.dispFlag.equals("Y")) comb.dispFlag="N";
                }

             if (getValueInt("mtu1.deduct_bp_tax",inti) > 0)
                {
                 setValue("mbdl.mod_desc"     , "減免退紅利點數(應稅)");
                 setValue("mbdl.tax_flag"     , "Y");
                 setValue("mbdl.beg_tran_bp"  , getValue("mtu1.deduct_bp_tax",inti));
                 setValue("mbdl.end_tran_bp"  , getValue("mtu1.deduct_bp_tax",inti));
                 insertMktBonusDtl(inti);

                 comb.modPgm = javaProgram;
                 comb.bonusFunc(tranSeqno);
                 if (comb.dispFlag.equals("Y")) comb.dispFlag="N";
                }
            }

         setValue("refund_amt"    , "0");
         setValue("refund_bp"     , getValue("mtu1.deduct_bp",inti));
         setValue("refund_bp_tax" , getValue("mtu1.deduct_bp_tax",inti));
         insertMktThsrUpidnoMon(inti,intq);
         updateMktThsrUptxn3(inti,intq);
         return(0);
        }
     else if (getValue("mtu1.pay_type",inti).equals("2"))
        {
         if (selectBilSysexp(inti)==0)
            {
             deleteBilSysexp();
             setValue("refund_amt"    , getValue("mutl.deduct_amt",inti));
             setValue("refund_bp"     , "0");
             setValue("refund_bp_tax" , "0");
             insertMktThsrUpidnoMon(inti,intq);
             updateMktThsrUptxn3(inti,intq);
             if (getValue("upidno_type").equals("1")) procInt[13]++;
             else procInt[14]++;
             return(0);
            }
         else
            {
             if (selectActDebt(inti)!=0)
                if (selectActDebtHst(inti)!=0) return(1);

             if (getValueInt("debt.d_avail_bal")!=0)
                {
                 setValueInt("dest_amt"        ,  getValueInt("debt.d_avail_bal"));
                 insertBilSysexp(inti);
                 setValue("refund_amt"         , getValue("mtu1.deduct_amt",inti));
                 setValue("refund_bp"          , "0");
                 setValue("refund_bp_tax"      , "0");
                 updateMktThsrUptxn3(inti,intq);
                 insertMktThsrUpidnoMon(inti,intq);
                 if (getValue("upidno_type").equals("1")) procInt[13]++;
                 else procInt[14]++;
                 return(0);
                }
            }
        }
    }
     
  return(0);
 }
// ************************************************************************ 
 int selectMktThsrUpidno() throws Exception 
 {
  extendField = "upid.";
  selectSQL = "b.id_p_seqno,"
            + "a.upidno_seqno,"
            + "free_cnt";
 daoTable   = "mkt_thsr_upidno a,crd_idno b";
 whereStr   = "WHERE  a.id_no   = b.idno "
            + "and    ? between substr(a.free_date_s,1,6) "
            + "         and     substr(b.free_date_e,1,6) "
            + "and    b.id_p_seqno = ? "
            + "order by id_p_seqno,upidno_seqno"
            ;

  setString(1 , comm.lastMonth(businessDate , 1));
  setString(2 , getValue("id_p_seqno"));

  branchCnt = selectTable();

  return(0);
 }
// ************************************************************************
 int selectMktThsrUpidnoMon(int intm) throws Exception 
 {
  extendField = "umon.";
  selectSQL = "sum(decode(refund_flag,'N',1,0)) as use_cnt";
  daoTable  = "mkt_thsr_upidno_mon";
  whereStr  = "WHERE id_p_seqno   = ?  "
            + "and   trans_month  = ? "
            + "and   upidno_seqno = ? "
            ;

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("trans_month")); 
  setString(3 , getValue("upid.upidno_seqno",intm));

  selectTable();

  return(getValueInt("upid.free_cnt",intm)-getValueInt("umon.use_cnt")); 
 }
// ************************************************************************
 int selectMktBonusDtl(int inti) throws Exception
 {
  extendField = "mbd2.";
  selectSQL = "beg_tran_bp";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE  tran_seqno = ? "
            ;

  setString(1,getValue("mtu1.tran_seqno",inti));

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp      = 0,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE tran_seqno = ? ";

  setString(1 , javaProgram);
  setString(2 , getValue("mtu1.tran_seqno",inti));

  updateTable();
  return;
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  extendField = "mbdl.";
                                                     
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.bonus_type"           , "BONU");
  setValue("mbdl.tran_code"            , "4");
  setValue("mbdl.mod_memo"             , "卡號["+getValue("mtu1.card_no",inti)+"]");
  if (getValue("mtu1.tran_seqno",inti).length()>0)
     setValue("mbdl.mod_memo"          , getValue("mbdl.mod_memo")
                                       + " 原交易序號["+ getValue("mtu1.tran_seqno",inti) +"]");
  setValue("mbdl.active_name"          , "高鐵車廂升等紅利減免退還點數");
  setValue("mbdl.acct_date"            , businessDate);
  setValue("mbdl.acct_type"            , getValue("mut1.acct_type",inti));
  setValue("mbdl.p_seqno"              , getValue("mut1.p_seqno",inti));
  setValue("mbdl.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.effect_e_date"        , "");
  if (getValueInt("bpid.effect_months")>0)
     setValue("mbdl.effect_e_date"     , comm.nextMonthDate(getValue("mtu1.proc_date",inti)
                                       , getValueInt("bpid.effect_months")));
  setValue("mbdl.apr_user"             , javaProgram);
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , javaProgram); 
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectBilSysexp(int inti) throws Exception
 {
  extendField = "sys1.";
  selectSQL = "dest_amt,"
            + "rowid as rowid";
  daoTable  = "bil_sysexp";
  whereStr  = "where purchase_date  = ? "
            + "and   bill_type      = 'INHU' "
            + "and   txn_code       = '05' "
            + "and   card_no        = ? "
            + "and   ref_key        = ? "
            + "and   dest_amt       = ? "
            + "and   post_flag     != 'Y' "
            + "and   curr_code      = '901' ";

  setString(1, getValue("mtu1.trans_date",inti));
  setString(2, getValue("mtu1.card_no",inti));
  setString(3, getValue("mtu1.serial_no",inti));
  setInt(4   , getValueInt("mtu1.deduct_amt",inti));

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int deleteBilSysexp() throws Exception
 {
  daoTable  = "bil_sysexp";
  whereStr  = "WHERE rowid    = ? ";

  setRowId(1  , getValue("sys1.rowid"));

  deleteTable();

  return(0);
 }
// ************************************************************************
 int insertMktThsrUpidnoMon(int inti,int intm) throws Exception
 {
  setValue("imon.serial_no"            , getValue("mtu1.serial_no",inti));
  setValue("imon.file_type"            , getValue("upid.file_type",intm));
  setValue("imon.branch_code"          , getValue("upid.branch_code",intm));
  setValue("imon.trans_month"          , getValue("mtu1.trans_date",inti).substring(0,6));
  setValue("imon.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("imon.upidno_seqno"         , getValue("upid.upidno_seqno",intm));
  setValue("imon.refund_flag"          , "N");
  setValue("imon.mod_time"             , sysDate+sysTime);
  setValue("imon.mod_pgm"              , javaProgram);

  extendField = "imon.";
  daoTable  = "mkt_thsr_upidno_mon";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectActDebt(int inti) throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHU' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? ";

  setString(1, getValue("mtu1.p_seqno",inti));
  setString(2, getValue("mtu1.card_no",inti));
  setString(3, getValue("mtu1.trans_date",inti));
  setInt(4   , getValueInt("mtu1.deduct_amt",inti));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int selectActDebtHst(int inti) throws Exception
 {
  extendField = "dest.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt_hst";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHU' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? ";

  setString(1, getValue("mtu1.p_seqno",inti));
  setString(2, getValue("mtu1.card_no",inti));
  setString(3, getValue("mtu1.trans_date",inti));
  setInt(4   , getValueInt("mtu1.deduct_amt",inti));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int insertBilSysexp(int inti) throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , getValue("mtu1.card_no",inti));
  setValue("sysp.p_seqno"            , getValue("mtu1.p_seqno",inti));
  setValue("sysp.acct_type"          , getValue("mtu1.acct_type",inti));
  setValue("sysp.bill_type"          , "INHU");
  setValue("sysp.txn_code"           , "06");
  setValue("sysp.purchase_date"      , businessDate);
  setValue("sysp.src_amt"            , getValue("dest_amt"));
  setValue("sysp.dest_amt"           , getValue("dest_amt"));
  setValue("sysp.dc_dest_amt"        , getValue("dest_amt"));
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "退還高鐵升等扣款金額(減免)");
  setValue("sysp.post_flag"          , "N"); 
  setValue("sysp.ref_key"            , getValue("mtu1.serial_no")); 
  setValue("sysp.mod_user"           , javaProgram); 
  setValue("sysp.mod_time"           , sysDate+sysTime);
  setValue("sysp.mod_pgm"            , javaProgram);
                     
  daoTable  = "bil_sysexp";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktThsrUptxn3(int inti,int intq) throws Exception
 {
  updateSQL = "refund_date      = ?,"
            + "refund_flag      = 'Y',"
            + "pay_type         = '3',"
            + "refund_amt       = ?,"
            + "refund_bp        = ?,"
            + "refund_bp_tax    = ?,"
            + "upidno_seqno     = ?,"
            + "upidno_type      = ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , businessDate);
  setInt(2    , getValueInt("refund_amt"));
  setInt(3    , getValueInt("refund_bp"));
  setInt(4    , getValueInt("refund_bp_tax"));
  setString(5 , getValue("upid.upidno_seqno",intq));
  setString(6 , getValue("upidno_type"));
  setString(7 , javaProgram);
  setRowId(8  , getValue("mtu1.rowid",inti));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_3 error "+getValue("mtu1.rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void selectCycBpid(int inti) throws Exception
 {
  extendField = "bpid.";
  selectSQL = "effect_months";
  daoTable  = "cyc_bpid";
  whereStr  = "WHERE item_code  = '1' "
            + "and   bonus_type = 'BONU' "
            + "and   years      = ? "
            + "and   acct_type  = ? ";

  setString(1,getValue("mtu1.proc_date",inti).substring(0,4));
  setString(2,getValue("mtu1.acct_type",inti));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) 
     setValueInt("bpid.effect_months" , 36);
 }
// ************************************************************************


}  // End of class FetchSample

