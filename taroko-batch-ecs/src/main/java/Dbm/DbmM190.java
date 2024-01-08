/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/09/12  V1.00.10  Allen Ho   dbm_m190                                   *
* 112/03/29  V1.00.11  Zuwei Su  新增[ 一般消費群組 ]參數帳單資料篩選處理                                                                           *
*                                                                            *
******************************************************************************/
package Dbm;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class DbmM190 extends AccessDAO
{
 private  String progname = "VD紅利-新發卡開卡消費加贈點數處理程式 112/03/29  V1.00.11";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDBonus comb = null;

 String hBusiBusinessDate = "";
 String hDbdlTranSeqno     = "";

 long    totalCnt=0;
 int     parmCnt =0,cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM190 proc = new DbmM190();
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
   
   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   if (!hBusiBusinessDate.substring(6,8).equals("10"))
      {
       showLogMessage("I","","本程式只在每月9日換日後執行,本日為"+hBusiBusinessDate+"日..");
       showLogMessage("I","","=========================================");
       return(0);
      } 

   showLogMessage("I","","載入參數資料");
   selectDbmSysparm();
   selectDbmBpis();
   if (parmCnt==0)
      {
       showLogMessage("I","","無參數資料需執行");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadDbcCard();
   loadDbmBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入交易暫存資料");
   loadDbaDeductTxn();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理請款資料");
   selectDbbBill();
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
 void  selectDbbBill() throws Exception
 {
  selectSQL = "mcht_no,"
            + "reference_no,"
            + "acct_type,"
            + "id_p_seqno,"
            + "mcht_category,"
            + "pos_entry_mode,"
            + "decode(group_code,'','0000',group_code),"
            + "purchase_date,"
            + "card_no,"
            + "mcht_no,"
            + "ecs_platform_kind ,"
            + "ecs_cus_mcht_no ,"            
            + "sign_flag,"
            + "acq_member_id";
  daoTable  = "dbb_bill";
  whereStr  = "where  purchase_date like ? "
            + "and    acct_code = 'BL' "
            + "and   sign_flag  = '+' "
            + "and   issue_date >= ? "
            + "";

  setString(1 , comm.lastMonth(hBusiBusinessDate,1)+"%");               
  setString(2 , comm.nextNDate(comm.lastMonth(hBusiBusinessDate)+"01" ,-180));
                 
  showLogMessage("I","","MAX issue_date : ["+comm.nextNDate(comm.lastMonth(hBusiBusinessDate)+"01" ,-180)+"]");

  openCursor();
  int[] purchcnt = new int[parmCnt];
  int[] feedCnt  = new int[parmCnt];

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     {
      feedCnt[inti]=0;
      purchcnt[inti]=0;
      for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
     }

  totalCnt = 0;
  String acqId ="";  
  //int   inti = 0;  
  while( fetchTable() ) 
   { 
    totalCnt++;
    for (int inti=0;inti<parmCnt;inti++) {    
	    parmArr[inti][0]++;
	
	    setValue("lcrd.card_no"      , getValue("card_no"));
	    
	    cnt1 = getLoadData("lcrd.card_no");
	    // multi-parameter 非開卡回饋期間
	    if (cnt1==0) continue;    
	    // single-parameter 非開卡回饋期間 
	    if ( getValue("lcrd.activate_date").compareTo(getValue("parm.active_s_date",inti)) < 0 )
	    	continue;   
	    if ( getValue("lcrd.activate_date").compareTo(getValue("parm.active_e_date",inti)) > 0 )
	    	continue; 
	    if ( getValue("lcrd.activate_date").compareTo(
	    	 comm.nextMonth(hBusiBusinessDate,(getValueInt("parm.re_months",inti))*-1)+"01" ) < 0 )
	    	continue; 
	    if ( getValue("lcrd.activate_date").compareTo(comm.nextMonth(hBusiBusinessDate,-1)+"31" ) > 0 )
		    	continue; 	    
	    //setString(1 , comm.nextMonth(hBusiBusinessDate,(getValueInt("parm.re_months"))*-1)+"01"); 
	    //setString(2 , comm.nextMonth(hBusiBusinessDate,-1)+"31");   	    
	    
	    parmArr[inti][1]++;
	//  showLogMessage("I","","id_p_seqno  : ["+ getValue("id_p_seqno") +"]");
	
	    if (getValue("parm.new_card_cond",inti).equals("Y"))
	       {
	    parmArr[inti][2]++;
	        if (selectCrdCard1()!=0) continue;
	    parmArr[inti][3]++;
	       }
	
	    setValue("duct.reference_no" , getValue("reference_no"));
	    cnt1 = getLoadData("duct.reference_no");
	    if (cnt1<=0) continue;
	
	    acqId = "";
	    if (getValue("acq_member_id").length()!=0)
	       acqId = comm.fillZero(getValue("acq_member_id"),8);
	
	    parmArr[inti][4]++;
	    setValue("data_key" , getValue("parm.active_code",inti));
	
	    if (getValueInt("duct.deduct_amt") <= getValueInt("parm.bp_amt",inti)) continue;
	
	    parmArr[inti][5]++;
	
	    parmArr[inti][6]++;
	
	    if (selectDbmBnData(getValue("mcht_no"),acqId,
	                            getValue("parm.merchant_sel",inti),"1",3)!=0)
	       continue;
	    parmArr[inti][7]++;
	
	
	    if (selectDbmBnData(getValue("group_code"),
	                           getValue("parm.group_code_sel",inti),"2",3)!=0)
	       continue;
	
	    parmArr[inti][8]++;
	
	    if (selectDbmBnData(getValue("acct_type"),
	                           getValue("parm.acct_type_sel",inti),"3",3)!=0)
	       continue;
	
	    parmArr[inti][9]++;
	
	    if (selectDbmBnData(getValue("pos_entry_mode"),
	                           getValue("parm.pos_entry_sel",inti),"4",3)!=0)
	       continue;
	
	    parmArr[inti][10]++;
	
	    if (selectDbmBnData(getValue("mcht_category"),
	                           getValue("parm.mcc_code_sel",inti),"5",3)!=0)
	       continue;
	
	    parmArr[inti][11]++;
	    if (selectMktMchtgpData(getValue("mcht_no"),acqId,
	                         getValue("parm.mcht_group_sel",inti),"6")!=0)
	       continue;
	   
	    parmArr[inti][12]++;
	    
	    if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"), "", getValue("parm.platform_kind_sel", inti),
	            "P") != 0)
	        continue;
	    parmArr[inti][13]++;    
	
	    setValue("bdtl.active_code" , getValue("parm.active_code",inti));
	    setValue("bdtl.tax_flag"    , getValue("parm.tax_flag",inti));
	    setValue("bdtl.active_name" , getValue("parm.active_name",inti));
	    setValue("bdtl.tran_code"   , "2");
	    setValue("bdtl.mod_memo"    , "VD紅利新發卡消費贈送");
	
	    int deductBpInt = (int)Math.ceil(
	                        (getValueInt("duct.deduct_amt")
	                      / getValueInt("parm.bp_amt",inti))
	                      * getValueInt("parm.bp_pnt",inti) 
	                      );
	
	    int deductBp = (int)Math.round(
	                    (deductBpInt
	                  * getValueInt("parm.add_times",inti))
	                  + getValueInt("parm.add_point",inti));
	
	    if (deductBp==0) continue;
	
	    parmArr[inti][14]++;
	
	    setValueInt("bdtl.beg_tran_bp"  , deductBp);
	    setValueInt("bdtl.end_tran_bp"  , deductBp);
	    insertDbmBonusDtl();
	//  comb.DBonus_func(h_dbdl_tran_seqno);
	    parmArr[inti][15]++;
    }
    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
/*   
  for (inti=0;inti<parmCnt;inti++)
     {
      for (int intk=0;intk<20;intk++)
        {
         if (parmArr[inti][intk]==0) continue;
         showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
        }
   
     }
  showLogMessage("I","","=========================================");
*/  
 }
// ************************************************************************
 void  selectDbmBpis() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpis";
  whereStr  = "WHERE apr_flag = 'Y' "
            + "and   ? between active_s_date and "
            + "      to_char(add_months(to_date(active_e_date,'yyyymmdd'),re_months+1),'yyyymmdd') "
            ;

  setString(1 , hBusiBusinessDate);
                 
  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return;
 }
// ************************************************************************ 
 int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

  dateTime();
  extendField = "bdtl.";
  setValue("bdtl.tran_date"            , sysDate);
  setValue("bdtl.tran_time"            , sysTime);
  setValue("bdtl.crt_date"             , sysDate);
  setValue("bdtl.crt_user"             , javaProgram);
  setValue("bdtl.apr_date"             , sysDate);
  setValue("bdtl.apr_user"             , javaProgram);
  setValue("bdtl.apr_flag"             , "Y");
  setValue("bdtl.effect_e_date"        , comm.nextMonthDate(hBusiBusinessDate,getValueInt("dbmp.effect_months")));
  setValue("bdtl.acct_month"           , hBusiBusinessDate.substring(0,6));
  setValue("bdtl.acct_date"            , hBusiBusinessDate);
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.card_no"              , getValue("card_no"));
  setValue("bdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void loadDbaDeductTxn() throws Exception
 {
  extendField = "duct.";
  selectSQL = "reference_no,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "sum(deduct_amt)as deduct_amt";
  daoTable  = "dba_deduct_txn";
  whereStr  = "where deduct_proc_date >= ? "
            + "and   purchase_date like ? "
            + "and   acct_code = 'BL' "
            + "and   deduct_amt > 0 "
            + "group by reference_no "
            + "";

  setString(1 , comm.lastMonth(hBusiBusinessDate , 1)+"01");
  setString(2 , comm.lastMonth(hBusiBusinessDate,1)+"%");               

  int  n = loadTable();
  setLoadData("duct.reference_no");

  showLogMessage("I","","Load dba_deduct_txn cnt: ["+n+"]");
 }
// ************************************************************************
 void  loadDbmBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "dbm_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'DBM_BPIS' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load dbm_bn_data: ["+n+"]");
 }
// ************************************************************************
 int selectDbmBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectDbmBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectDbmBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,dbm_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'DBM_BPIS' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type in ('6','P') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
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
 int selectDbmSysparm() throws Exception
 {
  extendField = "dbmp.";
  selectSQL = "effect_months";
  daoTable  = "dbm_sysparm";
  whereStr  = "WHERE parm_type = '01' "
            + "and   apr_date !='' ";

  int recCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select dbm_sysparm error!" );
      exitProgram(0);
     }
  return(0);
 }
// ************************************************************************
 void  loadDbcCard() throws Exception   
 {
  int 	     max_re_months = 0 ; 
  String 	 min_active_s_date = "" ; 
  String 	 max_active_e_date = "" ;   
  for (int inta=0;inta<parmCnt;inta++)
     {
	  if ( min_active_s_date == "" )
		  min_active_s_date =  getValue("parm.active_s_date",inta) ;
      if ( min_active_s_date.compareTo(getValue("parm.active_s_date",inta)) > 0 )
		  min_active_s_date =  getValue("parm.active_s_date",inta) ;    
	  if ( max_active_e_date == "" )
		  max_active_e_date =  getValue("parm.active_e_date",inta) ;
      if ( max_active_e_date.compareTo(getValue("parm.active_e_date",inta)) < 0  )
    	  max_active_e_date =  getValue("parm.active_e_date",inta) ;  
	  if ( max_re_months == 0)
		  max_re_months =  getValueInt("parm.re_months",inta) ;      
      if ( max_re_months <  getValueInt("parm.re_months",inta) )
    	  max_re_months =  getValueInt("parm.re_months",inta) ;             
     }
	 
  extendField = "lcrd.";
  selectSQL = "card_no,"
            + "decode(ori_issue_date,'',issue_date,ori_issue_date) as ori_issue_date,"
            + "decode(ori_card_no,'',card_no,ori_card_no) as ori_card_no , "
            + "activate_date  " ;
  daoTable  = "dbc_card";
  whereStr  = "WHERE current_code = '0' "
            + "and   activate_date between ? and ? "
            + "and   activate_date between ? and ? "
            ;
  //setString(1 , comm.nextMonth(hBusiBusinessDate,(getValueInt("parm.re_months"))*-1)+"01"); 
  //setString(2 , comm.nextMonth(hBusiBusinessDate,-1)+"31"); 
  //setString(3 , getValue("parm.active_s_date")); 
  //setString(4 , getValue("parm.active_e_date")); 
  
  setString(1 , comm.nextMonth(hBusiBusinessDate,(max_re_months)*-1)+"01"); 
  setString(2 , comm.nextMonth(hBusiBusinessDate,-1)+"31"); 
  setString(3 , min_active_s_date ); 
  setString(4 , max_active_e_date );   

  //showLogMessage("I","","activate range ["
  //                     + comm.nextMonth(hBusiBusinessDate,(getValueInt("parm.re_months"))*-1)+"01"
  //                     + "] - ["
  //                     + comm.nextMonth(hBusiBusinessDate,-1)+"31"
  //                     +"]");
  //showLogMessage("I","","               ["
  //                     + getValue("parm.active_s_date")
  //                     + "] - ["
  //                     + getValue("parm.active_e_date")
  //                     +"]");
  showLogMessage("I","","activate range ["
                       + comm.nextMonth(hBusiBusinessDate,(max_re_months)*-1)+"01"
                       + "] - ["
                       + comm.nextMonth(hBusiBusinessDate,-1)+"31"
                       +"]");
  showLogMessage("I","","               ["
                       + min_active_s_date 
                       + "] - ["
                       + max_active_e_date 
                       +"]");  
  

  int  n = loadTable();

  setLoadData("lcrd.card_no");

  showLogMessage("I","","Load crd_card_1 Count: ["+n+"]");
 }
// ************************************************************************
 int selectCrdCard1() throws Exception 
 {
  extendField = "card.";
  selectSQL = "group_code,"
            + "sup_flag,"
            + "oppost_date";
  daoTable  = "dbc_card";
  whereStr  = "WHERE  id_p_seqno     = ? "
            + "AND    decode(ori_card_no,'',card_no,ori_card_no)  !=  ? "
            + "AND    decode(ori_issue_date,'',issue_date,ori_issue_date)  < ? "
            + "AND    corp_no = '' "
            ;

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("lcrd.ori_card_no"));
  setString(3 , getValue("lcrd.ori_issue_date"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
      {
       if (getValue("card.oppost_date",inti).length()==0)  return(1);

       if (comm.nextNDate(getValue("card.oppost_date",inti),180)
            .compareTo(getValue("lcrd.ori_issue_date"))>=0) return(1);
      }

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
