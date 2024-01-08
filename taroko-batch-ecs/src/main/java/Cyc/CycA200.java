/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/05/04  V1.00.25  Allen Ho   Cyc_A150                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                            *                                                                  *
* 112-03-09  V1.00.02    Yang Bo    update active_code replace years         *
* 112-03-20  V1.00.03    Zuwei Su  data_key 改為 取值ACTIVE_CODE,CYC_BPID->CYC_BPID2          *
* 112-03-21  V1.00.04    Zuwei Su  變數businessDate 與AccessDAO的變數衝突，parm取值錯誤
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA200 extends AccessDAO
{
 private final String PROGNAME = "關帳-消費新增紅利回饋處理程式 112-03-21  V1.00.04"  ;
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusinessDate   = "";
 String activeCode     = "";

 String pSeqno ="";
 String majorCardNo ="";
 String idPSeqno ="";
 String acctType  ="";

 int bonuCnt = 0,parmCnt=0; 
 String tranSeqno = "",cmpMonth="";
 long    totalCnt=0,updateCnt=0;
   boolean DEBUG  = false;
   boolean DEBUG1 = false;
   boolean DEBUG2 = false;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA200 proc = new CycA200();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
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
       showLogMessage("I","","PARM 2 : DEBUG ");
       return(1);
      }

   if ( args.length >= 1 )
      { hBusinessDate = args[0]; }
   
   if ( args.length == 2 )
      { activeCode = args[1]; }
   
   if ( args.length == 3 )
      { DEBUG = true; }
   
   if ( !connectDataBase() )
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }
   showLogMessage("I","","this_acct_month["+getValue("wday.this_acct_month")+"]");

//   select_mkt_bpid_data(); 

   if (DEBUG)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","   Delete Mode");
       deleteMktBpidData();
      }
  else 
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","清除暫存檔(mkt_bpid_data)");
       commitDataBase();
       truncateTable("MKT_BPID_DATA");
       commitDataBase();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   //loadBilMerchant();
   loadCycBnData();
   loadMktMchtgpData();
   loadActSysexpLog();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectCycBpid0();
   selectCycBpid();
   if (parmCnt==0)     
   {
    showLogMessage("I","","今日["+hBusinessDate+"]無基本活動回饋參數資料進行處理!");
    return(0);
   }     
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(bil_bill)資料");

   selectBilBill();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

   finalProcess();  
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

  if (hBusinessDate.length()==0)
      hBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusinessDate+"]");
 }
// ************************************************************************
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************ 
 int selectBilBill() throws Exception
 {
  //selectSQL = " decode(mcht_country,'TW','1','2') as foreign_code , *  " ;
  daoTable  = " bil_bill ";
  whereStr  = "where rsk_type not in ('1','2','3') "
            + "and   acct_code  in ('BL','ID','IT','AO','OT','CA')  "
            + "and   stmt_cycle  = ? "
            + "and   dest_amt   != 0 "
//          + "and   p_seqno  in ('0000344687','' ) "    //debug 
//          + "and   reference_no  = '1881552575' "
            + "and   acct_month    = ?  "
            + "order by major_card_no ";

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , getValue("wday.this_acct_month")); 


  openCursor();

  totalCnt=0;

  String acqId ="";
  double[] parmAmt = new double [parmCnt];
  for (int inti=0;inti<parmCnt;inti++) parmAmt[inti]=0;
  double[][] parmArr = new double [10][20];
   
  for (int inti=0;inti<10;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
  int cnt1=0;
  while( fetchTable() ) 
   { 

    if (DEBUG2)
       { 
        showLogMessage("I","","STEP 0 p_seqno      ["+ getValue("p_seqno")  +"]");
        showLogMessage("I","","       p_seqno      ["+ pSeqno +"]");
        showLogMessage("I","","       acct_type    ["+ getValue("acct_type")  +"]");
        showLogMessage("I","","       reference_no ["+ getValue("reference_no")  +"]");
        showLogMessage("I","","       acct_code    ["+ getValue("acct_code")  +"]");
        showLogMessage("I","","       mcht_no      ["+ getValue("mcht_no")  +"]");
        showLogMessage("I","","       sign_flag    ["+ getValue("sign_flag")  +"]");
        showLogMessage("I","","       dest_amt     ["+ getValueDouble("dest_amt")  +"]");
       }

//    if ((!pSeqno.equals(getValue("p_seqno")))&&
//        (pSeqno.length()!=0))
//       {
//        if (!DEBUG) procBonus(parmAmt);
//        for (int inti=0;inti<parmCnt;inti++) parmAmt[inti]=0;
//       }
    if ((!majorCardNo.equals(getValue("MAJOR_CARD_NO"))) && (majorCardNo.length()!=0)) {
    	if (!DEBUG) {
    	    procBonus(parmAmt);
    	}
        for (int inti=0;inti<parmCnt;inti++) {
            parmAmt[inti]=0;
        }
    }

    pSeqno    = getValue("p_seqno");
    idPSeqno = getValue("major_id_p_seqno");
    acctType  = getValue("acct_type");
    majorCardNo = getValue("MAJOR_CARD_NO") ;

    if (getValue("group_code").length()==0)
       setValue("group_code" , "0000");

    if (getValue("sign_flag").equals("-"))
       setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
    else    
       setValueDouble("dest_amt" , getValueDouble("dest_amt")+getValueDouble("curr_adjust_amt"));

    if (getValue("merge_flag").equals("Y"))
       {
        setValue("sysp.reference_no" , getValue("reference_no"));
        cnt1 = getLoadData("sysp.reference_no");
        if (cnt1!=0) continue;
       }
    
    if (getValue("mcht_country").length()==3) 
        setValue("mcht_country" , getValue("mcht_country").substring(0,2));
    if (getValue("mcht_country").equals("")) 
        setValue("mcht_country" , "TW");

    for (int inti=0;inti<parmCnt;inti++)
      {
       if (DEBUG2)
          { 
           //showLogMessage("I","","STEP 1 years     ["+ getValue("parm.years",inti)      +"]");
    	   showLogMessage("I","","STEP 1 active_code["+ getValue("parm.active_code",inti)  +"]");
           showLogMessage("I","","       bonus_type["+ getValue("parm.bonus_type",inti) +"]");   
           showLogMessage("I","","       acct_type ["+ getValue("parm.acct_type",inti)  +"]");   
          }

       if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL")))  continue;
       if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT")))  continue;
       if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA")))  continue;
       if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID")))  continue;
       if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO")))  continue;
       if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT")))  continue;
       parmArr[inti][0]++;
       if (!getValue("parm.foreign_code",inti).equals("3")) {
         if(getValue("parm.foreign_code",inti).equals("2") && getValue("mcht_country").equals("TW")) {
           continue ;
         }

         if(getValue("parm.foreign_code",inti).equals("1") && !getValue("mcht_country").equals("TW")) {
           continue ;
         }
       }
       parmArr[inti][1]++;

        if (!getValue("acct_type").equals(getValue("parm.acct_type",inti))) continue;
        parmArr[inti][2]++;

        setValue("data_key", getValue("parm.active_code",inti));

        acqId = "";
        if (getValue("acq_member_id").length()!=0)
          acqId = comm.fillZero(getValue("acq_member_id"),8);

        if (selectCycBnData(getValue("mcht_no"),acqId,
                               getValue("parm.merchant_sel",inti),"1",3)!=0) continue;
        parmArr[inti][3]++;

        if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                            getValue("parm.mcht_group_sel",inti),"4")!=0) continue;

        parmArr[inti][4]++;
        
        if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"),"",
                getValue("parm.platform_kind_sel",inti),"P")!=0) continue;

        parmArr[inti][5]++;
        
        //parameter exist ["group_code" ,"card_type"]["group_code" ,""]
        if (selectCycBnData(getValue("group_code"),getValue("card_type"),
            getValue("parm.group_card_sel",inti),"2",2)!=0)
            if (selectCycBnData(getValue("group_code"),getValue(""),
                getValue("parm.group_card_sel",inti),"2",2)!=0) 
        	    continue;
        parmArr[inti][6]++;

        if (selectCycBnData(getValue("group_code"),getValue("mcht_no"),
                               getValue("parm.group_merchant_sel",inti),"3",2)!=0) continue;
         parmArr[inti][7]++;
        
        if ( getValueInt("parm.limit_amt",inti) == 0 ) {
             parmAmt[inti] = parmAmt[inti] + getValueDouble("dest_amt");
             if (Arrays.asList("BL","CA","IT","ID","AO","OT").contains(getValue("acct_code")))
                insertMktBpidData(inti);             
        }else {
        	parmAmt[inti] = parmAmt[inti] 
        	+ (int)(getValueDouble("dest_amt")/getValueInt("parm.limit_amt",inti)) * getValueInt("parm.limit_amt",inti);
        	if ((int)(getValueDouble("dest_amt")/getValueInt("parm.limit_amt",inti)) * getValueInt("parm.limit_amt",inti) != 0)
        	{
               if (Arrays.asList("BL","CA","IT","ID","AO","OT").contains(getValue("acct_code")))
                   insertMktBpidData(inti);        		
        	}
        		
        }
        
        if (DEBUG2)
           { 
            showLogMessage("I","","STEP 3 parmamt["+ inti + "] = "+ parmAmt[inti]  +"]");
           }
       //if (Arrays.asList("BL","CA","IT","ID","AO","OT").contains(getValue("acct_code")))
       //   insertMktBpidData(inti);
      }


    totalCnt++;
   } 
  closeCursor();

  if (DEBUG)
     for (int inti=0;inti<10;inti++)
         for (int intk=0;intk<20;intk++)
            {
             if (parmArr[inti][intk]==0) continue;
             showLogMessage("I","","絆腳石  :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
            }

  if (!DEBUG) procBonus(parmAmt);

  return(0);
 }
// ************************************************************************
 int insertMktBpidData(int inti) throws Exception
 {
  dateTime();
  extendField = "bpid.";
  setValue("bpid.acct_month"           , getValue("wday.this_acct_month"));
  setValue("bpid.p_seqno"              , getValue("p_seqno"));
  setValue("bpid.id_p_seqno"           , idPSeqno);
  setValue("bpid.acct_type"            , getValue("acct_type"));
  setValue("bpid.reference_no"         , getValue("reference_no"));
  setValue("bpid.purchase_date"        , getValue("purchase_date"));
  setValue("bpid.stmt_cycle"           , getValue("stmt_cycle"));
  setValue("bpid.card_no"              , getValue("card_no"));
  setValue("bpid.issue_date"           , getValue("issue_date"));
  setValue("bpid.ori_issue_date"       , getValue("issue_date"));
  if (getValue("ori_card_no").length()==0)
      setValue("ori_card_no"            , getValue("card_no"));
  setValue("bpid.ori_card_no"          , getValue("ori_card_no"));
  setValue("bpid.group_code"           , getValue("group_code"));
  setValue("bpid.card_type"            , getValue("card_type"));
  setValue("bpid.acct_code"            , getValue("acct_code"));
  setValue("bpid.mcht_category"        , getValue("mcht_category"));
  setValue("bpid.pos_entry_mode"       , getValue("pos_entry_mode"));
  setValue("bpid.source_curr"          , getValue("source_curr"));
  setValue("bpid.mcht_no"              , getValue("mcht_no"));
  setValue("bpid.acq_member_id"        , getValue("acq_member_id"));
  setValue("bpid.bin_type"             , getValue("bin_type"));
  setValue("bpid.proc_flag"            , "N");
  setValue("bpid.proc_date"            , "");
  setValue("bpid.dest_amt"             , getValue("dest_amt"));
  setValue("bpid.mod_time"             , sysDate+sysTime);
  setValue("bpid.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpid_data";

  insertTable();
  
  if("Y".equals(dupRecord)) {
      return 0;
  }
  

  return(0);
 }
// ************************************************************************
 int procBonus(double[] parmAmt) throws Exception
 {
  String[] memoStr = new String [bonuCnt];
  int[] bonuBp = new int [bonuCnt];

     if (DEBUG1)
         showLogMessage("I","","STEP X p_seqno ["+ pSeqno +"]");

  for (int inti=0;inti<parmCnt;inti++)
    {
     if (DEBUG1)
         showLogMessage("I","","STEP A parmamt[" +inti +"] = "+ parmAmt[inti]  +"]");
          
     if (parmAmt[inti]==0) continue;
     double totAmt = parmAmt[inti];

     if (parmAmt[inti]<0) totAmt=totAmt*-1;

     double exchgRate = 0;

     if ((totAmt>=getValueInt("parm.limit_1_beg",inti))&&
         (totAmt<=getValueInt("parm.limit_1_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_1",inti);
        }
     else  if ((totAmt>=getValueInt("parm.limit_2_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_2_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_2",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_3_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_3_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_3",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_4_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_4_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_4",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_5_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_5_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_5",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_6_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_6_end",inti)))
        {
         exchgRate = getValueInt("parm.exchange_6",inti);
        }

     if (exchgRate==0) continue;
     int totBp = (int)Math.floor(totAmt/exchgRate);

     if (parmAmt[inti]<0) totBp=totBp*-1;
     

     if (DEBUG1)
        {
         showLogMessage("I","","STEP B tot_bp = ["+ totAmt  +"]/["+exchgRate+"] = ["+totBp+"]");
        }

     if (totBp==0) continue;

     int begTranBp = totBp;
     
     if (getValueInt("parm.feedback_lmt",inti) != 0 )
	     if (Math.abs(begTranBp) > getValueInt("parm.feedback_lmt",inti)) {
	    	 showLogMessage("I","","majorCardNo="+ majorCardNo  +" [bp="+ begTranBp + " ] [>=Lmt Bp:"
	         +getValueInt("parm.feedback_lmt",inti) +"]");
	       if (begTranBp < 0) {
	         begTranBp = -Math.abs(getValueInt("parm.feedback_lmt",inti));
	       } else {
	         begTranBp = Math.abs(getValueInt("parm.feedback_lmt",inti));
	       }
	     }

     setValueInt("mbdl.beg_tran_bp"       , begTranBp);
     setValueInt("mbdl.end_tran_bp"       , begTranBp);

     int okFlag=0;

     setValue("mbdl.mod_memo"   , "信用卡紅利點數基本回饋");

     if (begTranBp < 0) 
        setValue("mbdl.mod_memo"   , getValue("mbdl.mod_memo")+"退貨扣回");
     if (begTranBp != 0)
        insertMktBonusDtl(inti);

//   if (ok_flag==1) comb.Bonus_func(tran_seqno);
    }

  return(0);
 }
// ************************************************************************
 int selectCycBpid0() throws Exception
 {
  extendField = "bonu.";
  selectSQL = "bonus_type,"
            + "effect_months";
  daoTable  = "cyc_bpid2";
  //whereStr  = "WHERE years  = ?  "
  //          + "group by bonus_type,effect_months "
  whereStr  = "WHERE  apr_flag   = 'Y'  "		  
  		    + " and (stop_flag  = 'N' or stop_flag  = '' "
            + " or  (stop_flag = 'Y' "
            + " and  stop_date > ? )) "
            + " and   ? between decode(active_month_s,'','000000',active_month_s) "
            + " and     decode(active_month_e,'','999999',active_month_e) "		  
            ;

  //setString(1 , businessDate.substring(0,4));
  setString(1 , hBusinessDate);
  setString(2 , getValue("wday.this_acct_month"));  
  
  if (activeCode.length()>0)
  {
   whereStr  = whereStr 
             + "and active_code = ? "; 
   setString(3 , activeCode);
  }
  
  whereStr  = whereStr 
          + "group by bonus_type,effect_months  ";  

  bonuCnt = selectTable();

  for (int inti=0;inti<bonuCnt;inti++)
      showLogMessage("I","","筆數 : ["+ (inti+1) + "] 紅利代碼= ["+ getValue("bonu.bonus_type",inti) 
                                     + "] 效期月數= ["+ getValueInt("bonu.effect_months",inti)+ "]");

  return(0);
 }
// ************************************************************************
 int selectCycBpid() throws Exception
 {
  extendField = "parm.";
  daoTable  = "cyc_bpid2";
  whereStr  = " where apr_flag   = 'Y'   "
            + " and   (stop_flag  = 'N' or stop_flag  = ''  " 
            + " or    (stop_flag = 'Y' "
            + " and    stop_date > ? ))  "
            + " and  ?  between decode(active_month_s,'','000000',active_month_s) "
            + " and     decode(active_month_e,'','999999',active_month_e) "	            
//            + "order by bonus_type,acct_type,active_code "
            ;
  setString(1 , hBusinessDate);
  setString(2 , getValue("wday.this_acct_month"));  
  
  if (activeCode.length()>0)
  {
   whereStr  = whereStr 
             + "and active_code = ? "; 
   setString(3 , activeCode);
  }
  
  whereStr  = whereStr 
          + " order by bonus_type,acct_type,active_code  ";  
  parmCnt = selectTable();
  
  for (int inti=0;inti<parmCnt;inti++)
  {
   showLogMessage("I","","基本回饋代號 : ["+ getValue("parm.active_code",inti) +"]-[" + getValue("parm.active_name",inti) +"]");
  }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  extendField = "mbdl.";
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("mbdl.active_code"     , getValue("parm.active_code",inti));
  //setValue("mbdl.active_name"     , "消費新增紅利回饋");
  setValue("mbdl.active_name"     , getValue("parm.active_name",inti));
  setValue("mbdl.major_card_no"   , majorCardNo);
  setValue("mbdl.p_seqno"         , pSeqno);
  setValue("mbdl.id_p_seqno"      , idPSeqno);
  setValue("mbdl.acct_type"       , acctType);
  setValue("mbdl.tax_flag"        , "N");
  setValue("mbdl.tran_code"       , "1");
  setValue("mbdl.tran_date"       , sysDate);
  setValue("mbdl.tran_time"       , sysTime);
  setValue("mbdl.tran_seqno"      , tranSeqno);
  setValue("mbdl.effect_e_date"   , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("mbdl.effect_e_date"   , comm.nextMonthDate(hBusinessDate,getValueInt("parm.effect_months",inti)));
  setValue("mbdl.bonus_type"      , getValue("parm.bonus_type",inti));
  setValue("mbdl.acct_date"       , hBusinessDate);
  setValue("mbdl.proc_month"      , hBusinessDate.substring(0,6));
  setValue("mbdl.tran_pgm"        , javaProgram);
  setValue("mbdl.apr_flag"        , "Y");
  setValue("mbdl.apr_user"        , javaProgram);
  setValue("mbdl.apr_date"        , sysDate);
  setValue("mbdl.crt_user"        , javaProgram);
  setValue("mbdl.crt_date"        , sysDate);
  setValue("mbdl.mod_user"        , javaProgram);
  setValue("mbdl.mod_time"        , sysDate+sysTime);
  setValue("mbdl.mod_pgm"         , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadActSysexpLog() throws Exception
 {
  extendField = "sysp.";
  selectSQL = "reference_no_new as reference_no ";
  daoTable  = "act_sysexp_log";
  whereStr  = "WHERE source_type = '02' ";

  int  n = loadTable();

  setLoadData("sysp.reference_no");

  showLogMessage("I","","Load act_sysexp_log Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCycBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "cyc_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'CYC_BPID2' "
  //        + "and   data_key like ?  "
            + "order by data_key,data_type,data_code,data_code2";

  //setString(1 , businessDate.substring(0,4)+"%");

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load cyc_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectCycBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectCycBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectCycBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectCycBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectCycBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
  daoTable  = "mkt_mchtgp_data a,cyc_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'CYC_BPID2' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  in ('4','P') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
	int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("mcht.data_key", getValue("data_key"));
		setValue("mcht.data_type", dataType);
		setValue("mcht.data_code", col1);

		int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
		int okFlag = 0;

		for (int inti = 0; inti < cnt1; inti++) {
			if ("P".equals(dataType)) {
				okFlag = 1;
				break;
			} else {
				if ((getValue("mcht.data_code2", inti).length() == 0)
						|| ((getValue("mcht.data_code2", inti).length() != 0)
								&& (getValue("mcht.data_code2", inti).equals(col2)))) {
					okFlag = 1;
					break;
				}
			}
		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}
// ************************************************************************
 void  loadBilMerchant() throws Exception
 {
  extendField = "unon.";
  selectSQL = "mcht_no";
  daoTable  = "bil_merchant";
  //whereStr  = "WHERE UNIFORM_NO  = '78506552' "
  whereStr  = "WHERE UNIFORM_NO  = '70799128' "
            + "and   MCC_CODE   in ('6010','4814') ";

  int  n = loadTable();
  setLoadData("unon.mcht_no");

  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktBpidData() throws Exception
 {
  extendField = "bpdt.";
  selectSQL = "count(*) as data_cnt";
  daoTable  = "mkt_bpid_data";
  whereStr  = "WHERE acct_month  = ?  "
            + "and   stmt_cycle  = ?  "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  selectTable();

  return(getValueInt("bpdt.data_cnt"));
 }
// ************************************************************************
 int truncateTable(String tableName) throws Exception
 {
  String truncateSQL = "TRUNCATE TABLE "+ tableName + " "
                     + "IGNORE DELETE TRIGGERS "
                     + "DROP STORAGE "
                     + "IMMEDIATE "
                     ;

  showLogMessage("I","","Truncate Table : ["+ tableName + "]");

  executeSqlCommand(truncateSQL);

  return(0);
 }
// ************************************************************************
 void deleteMktBpidData() throws Exception
 {
  daoTable  = "mkt_bpid_data";
  whereStr  = "where acct_month   = ? "
            + "and   stmt_cycle   = ? "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete mkt_bpid_data [" + n + "] records");

  return;
 }
// ************************************************************************

}  // End of class FetchSample

