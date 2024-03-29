/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/11/02  V1.00.00  Allen Ho   mkt_b405                                   *
* 108/12/25  V1.00.01  Brian      IT於select_bil_contract統計                                                 *
* 109-12-04  V1.00.02  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktB405 extends AccessDAO
{
 private  String progname = "消費統計-每月每卡消費(入帳日)統計處理程式 109/12/04 V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate   = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktB405 proc = new MktB405();
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
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!hBusiBusinessDate.substring(6,8).equals("11"))
      {
       showLogMessage("I","","本程式設定為每月 11日執行");
       return(0);
      }   

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除重複執行資料...");
   deleteMktPostConsume();
   commitDataBase();
   
   showLogMessage("I","","=======bil_bill==========================");
   totalCnt = 0;
   selectMktBill();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");

   showLogMessage("I","","======bil_contract=======================");
   totalCnt=0;
   selectBilContract();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理IT ["+totalCnt+"] 筆");
   finalProcess();
   return 0;
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
 public void  selectMktBill() throws Exception
 {
  selectSQL = "major_card_no,"
            + "card_no,"
            + "max(purchase_date) as last_purchase_date,"
            + "sum(decode(acct_code,'BL',decode(sign_flag,'+',dest_amt,dest_amt*-1))) as consume_bl_amt, "
            + "sum(decode(acct_code,'CA',decode(sign_flag,'+',dest_amt,dest_amt*-1))) as consume_ca_amt,"
            + "sum(decode(acct_code,'AO',decode(sign_flag,'+',dest_amt,dest_amt*-1))) as consume_ao_amt,"
            + "sum(decode(acct_code,'ID',decode(sign_flag,'+',dest_amt,dest_amt*-1))) as consume_id_amt,"
            + "sum(decode(acct_code,'OT',decode(sign_flag,'+',dest_amt,dest_amt*-1))) as consume_ot_amt,"
            + "sum(decode(acct_code,'BL',decode(sign_flag,'+',1,-1))) as consume_bl_cnt,"
            + "sum(decode(acct_code,'CA',decode(sign_flag,'+',1,-1))) as consume_ca_cnt,"
            + "sum(decode(acct_code,'AO',decode(sign_flag,'+',1,-1))) as consume_ao_cnt,"
            + "sum(decode(acct_code,'ID',decode(sign_flag,'+',1,-1))) as consume_id_cnt,"
            + "sum(decode(acct_code,'OT',decode(sign_flag,'+',1,-1))) as consume_ot_cnt";
  daoTable  = "bil_bill";
  whereStr  = "where  acct_code in ('BL','CA','AO','ID','OT') "
            + "and    post_date like ? "
            + "group  by major_card_no,card_no ";

            ;

  setString(1  , comm.nextMonth(hBusiBusinessDate,-1)+"%");

  openCursor();

  int cnt1 = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    extendField = "card.";
    selectSQL = "card_no,"
              + "p_seqno,"
              + "id_p_seqno,"
              + "major_id_p_seqno,"
              + "acct_type,"
              + "ori_card_no,"
              + "card_type,"
              + "group_code,"
              + "source_code,"
              + "card_note";
    daoTable  = "crd_card";
    whereStr  = "where card_no = ? ";

    setString(1  , getValue("card_no"));
    
    cnt1 = selectTable();

    if (cnt1<=0) continue;

    setValue("p_seqno"          , getValue("card.p_seqno"));
    setValue("id_p_seqno"        , getValue("card.id_p_seqno"));
    setValue("major_id_p_seqno"  , getValue("card.major_id_p_seqno"));
    setValue("group_code"        , getValue("card.group_code"));
    setValue("card_type"         , getValue("card.card_type"));
    setValue("card_note"         , getValue("card.card_note"));
    setValue("source_code"       , getValue("card.source_code"));
    setValue("acct_type"         , getValue("card.acct_type"));
    setValue("ori_card_no"       , getValue("card.ori_card_no"));

    insertMktPostConsume();

    if (totalCnt%5000==0)
       {
        showLogMessage("I","","處理 ["+totalCnt +"] 筆");
        commitDataBase();
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
public int insertMktPostConsume() throws Exception
 {
  setValue("acct_month"           , comm.nextMonth(hBusiBusinessDate,-1));
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_post_consume";

  insertTable();

  return(0);
 }
 
// ************************************************************************ 
 public void  selectBilContract() throws Exception
 {
  sqlCmd = "SELECT max(d.p_seqno) as p_seqno, "
          +"       max(c.acct_type) as acct_type, "
          +"       max(c.purchase_date) as last_purchase_date,"
          +"       d.major_card_no, "
          +"       c.card_no, "
          +"       sum(c.tot_amt) as consume_it_amt, "
          +"       sum(1) as consume_it_cnt, "
          +"       max(d.group_code) as group_code, "
          +"       max(d.source_code) as source_code, "
          +"       max(d.card_type) as card_type, "
          +"       max(d.card_note) as card_note, "
          +"       max(d.id_p_seqno) as id_p_seqno, "
          +"       max(d.major_id_p_seqno) as major_id_p_seqno, "
          +"       max(d.ori_card_no) as ori_card_no "
          +"  FROM bil_contract c,crd_card d "
          +" WHERE c.card_no = d.card_no "
          +"   and c.first_post_date like ? "
          +"   and c.contract_kind   = '1' "
          +"   and c.mcht_no not in ('106000000005','106000000007') "   //分期償還(長循轉分期),帳單分期排除
          +"   and c.installment_kind <> 'N' "   //人工鍵入的分期合約排除
          +" group by d.major_card_no,c.card_no ";
		  
  setString(1  , comm.nextMonth(hBusiBusinessDate,-1)+"%");

  openCursor();

  while( fetchTable() ) 
   { 
    totalCnt++;

	extendField = "mkpc2.";
	setValue      (extendField + "card_no"        , getValue("card_no"));
	setValue      (extendField + "major_card_no"  , getValue("major_card_no"));
	setValue      (extendField + "acct_month"     , comm.nextMonth(hBusiBusinessDate,-1));
	setValue      (extendField + "last_purchase_date", getValue("last_purchase_date"));
	setValue      (extendField + "acct_type"      , getValue("acct_type"));
	setValue      (extendField + "group_code"     , getValue("group_code"));
	setValue      (extendField + "source_code"    , getValue("source_code"));
	setValue      (extendField + "card_type"      , getValue("card_type"));
	setValue      (extendField + "card_note"      , getValue("card_note"));
	setValueDouble(extendField + "consume_it_amt" , getValueDouble("consume_it_amt"));
	setValueInt   (extendField + "consume_it_cnt" , getValueInt("consume_it_cnt"));
	setValue      (extendField + "mod_time"       , sysDate+sysTime);
	setValue      (extendField + "mod_pgm"        , javaProgram);
	setValue      (extendField + "p_seqno"         , getValue("p_seqno"));
    setValue      (extendField + "id_p_seqno"     , getValue("id_p_seqno"));
    setValue      (extendField + "major_id_p_seqno", getValue("major_id_p_seqno"));
    setValue      (extendField + "ori_card_no"    , getValue("ori_card_no"));
	
    daoTable  = "mkt_post_consume";
    insertTable();
    if (dupRecord.equals("Y")) {
        updateSQL = "" 
                 +" consume_it_amt = ?, "
                 +" consume_it_cnt = ?, "
                 +" mod_time       = sysdate, "
                 +" mod_pgm        = ? ";
	    daoTable = "mkt_post_consume";
        whereStr =" WHERE card_no        = ? "
                 +" AND major_card_no  = ? "
                 +" AND acct_month     = ? ";
				 
	    setDouble(1, getValueDouble("consume_it_amt"));
	    setInt   (2, getValueInt("consume_it_cnt"));
	    setString(3, javaProgram);
	    setString(4, getValue("card_no"));
	    setString(5, getValue("major_card_no"));
	    setString(6, comm.nextMonth(hBusiBusinessDate,-1));
		
        updateTable();
    }

	
    if (totalCnt%5000==0)
       {
        showLogMessage("I","","處理 ["+totalCnt +"] 筆");
        commitDataBase();
       }
   } 
  closeCursor();
  return;
 }
 
 // ************************************************************************
 void deleteMktPostConsume() throws Exception
 {
	 while(true) {
		  daoTable  = "mkt_post_consume a";
		  whereStr  = "WHERE acct_month = ?  fetch first 5000 rows only ";

		  setString(1  , comm.nextMonth(hBusiBusinessDate,-1));  

		  totalCnt = deleteTable();
		  commitDataBase();
			
			if (totalCnt == 0 )	break;  
	 }

  return;
  
 }

}  // End of class FetchSample

