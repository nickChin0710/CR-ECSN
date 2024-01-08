/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/07  V1.00.45  Allen Ho   New                                        *
* 110/11/19  V1.01.02  Allen Ho   issue_date range                           *
* 111/10/13  V1.01.03  Suzuwei    sync from mega & updated for project coding standard   * 
* 111/10/20  V1.01.04  Zuwei Su   getValue("parm.active_code.inti"); 改為 getValue("parm.active_code",inti);
* 112/03/08  V1.01.05  Grace      rename parm.megalite_cond, detl.megalite_flag 為 banklite_cond, banklite_flag *
* 112/03/20  V1.01.06  Zuwei Su   以crd_card.id_p_seqno檢核新發卡人是否已存在首刷禮領取歷史資料檔(mkt_fstp_carddtl_h);  *
* 112/03/27  V1.01.07  Zuwei Su   依“匯入名單”參數設定(mkt_fstp_parm /mkt_imfstp_list), 判讀首刷禮回饋條件  *
* 112/04/17  V1.01.08  Grace      (1) 調整比對首刷禮歷史回饋資料的extendfield name (因為重複了, checkMktFstpCarddtlH())      *
*                                 (2) 調整 loadMktImfstpList() 的 select 欄位、from table;                               *
*                                 (3) 增加 loadMktImfstpListCid()                                                       *
***********************************************************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC400 extends AccessDAO
{
 private final String PROGNAME = "首刷禮-每日撈取新發卡名單處理程式 112/03/27  V1.01.07";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String businessDate = "";
 String lastDate = "";
 String classCode = "";
 String minIssueDate ="";
 String maxIssueDate ="";
 String activeCode ="";
 String idPSeqno     ="";

 int  parmCnt  = 0;
 int  pseqCnt  = 0;
 int  returnAmt = 0;
 int  returnCnt = 0;
 int  backdateCnt=3;	//做為issue_date期間控制
 int  newSeq     =0;

 int  totalCnt=0;
 int  existsCnt=0;
 int  cnt1 =0;
 int  selInt =0;
 String[] procFlag = new String[200];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC400 proc = new MktC400();
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
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [id_p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length >= 2 )
       activeCode = args[1];

   if ( args.length >= 3 )
       idPSeqno     = args[2];

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入首刷禮參數資料");
   selectMktFstpParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","無首刷禮參數資料");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktFstpCarddtl();
   loadMktBnData();
   loadMktImfstpList();
   loadMktImfstpListCid();
// select_crd_card_test();
   selectMktBnDataCard();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理卡檔(crd_card)資料, issue_date區間: 營業日- 往前 "+backdateCnt+"天");
   
   selectCrdCard();
   //showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","處理已領取首刷禮 ["+existsCnt+"] 筆");
   showLogMessage("I","","=========================================");

   if (idPSeqno.length()==0) finalProcess();

   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
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
 void selectCrdCard() throws Exception
 {
  selectSQL = "card_no,"
            + "acct_type,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "card_note,"
            + "issue_date,"
            + "group_code,"
            + "source_code,"
            + "promote_dept,"
            + "ori_card_no,"
            + "ori_issue_date,"
            + "major_id_p_seqno, "	
            + "card_type";
  daoTable  = "crd_card";
  whereStr  = "where issue_date between ?  and ? "
            + "and   card_no      = ori_card_no "
            + "and   card_no      = major_card_no "
            + "and   current_code = '0' "
            + "and   issue_date between ? and ? "
            ;

  setString(1, comm.nextNDate(businessDate,backdateCnt*-1));
  setString(2, businessDate);
  setString(3, minIssueDate);
  setString(4, maxIssueDate);

  if (idPSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   id_p_seqno = ? ";
      setString(4, idPSeqno);
     }

  whereStr  = whereStr
            + "order by card_no,issue_date";

  showLogMessage("I",""," 發卡日區間1 ["+  comm.nextNDate(businessDate,backdateCnt*-1)
                       +"]-["+ businessDate +"]");
  showLogMessage("I",""," 發卡日區間2 ["+ minIssueDate +"]-["+maxIssueDate+"]");

  openCursor();

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  while( fetchTable() ) 
   {
    totalCnt++;

    if (getValue("group_code").length()==0) setValue("group_code" , "0000");
    
    String tmpIdPSeqno = getValue("id_p_seqno");
    if (totalCnt == 1) {
    	showLogMessage("I","","=========================================");
        showLogMessage("I",""," 處理過濾已領取首刷禮名單");
    }
    int hcnt = checkMktFstpCarddtlH(tmpIdPSeqno);
    if (hcnt > 0) {
        existsCnt++;
        setValue("detl.error_desc"   , "已領取首刷禮(mkt_fstp_carddtl_h)");
        insertMktFstpCarddtl(0,"A10"); 
        continue;
    }

    for (int inti=0;inti<parmCnt;inti++)
        {
         if (procFlag[inti].equals("N")) continue;
         parmArr[inti][0]++;
         setValue("data_key"   , getValue("parm.active_code",inti));

         if (getValue("issue_date").compareTo(getValue("parm.issue_date_s",inti))<0) continue;
         if (getValue("issue_date").compareTo(getValue("parm.issue_date_e",inti))>0) continue;
         parmArr[inti][1]++;

         setValue("mfcl.card_no"    , getValue("card_no"));
         setValue("mfcl.active_code", getValue("parm.active_code",inti));
         cnt1 = getLoadData("mfcl.card_no,mfcl.active_code");
         if (cnt1>0) continue;  // 已入檔

         if (selectMktBnData(getValue("acct_type"),
                                getValue("parm.acct_type_sel",inti),"1",3)!=0) 
            {
             setValue("detl.error_desc"   , "帳戶類別不符");
             insertMktFstpCarddtl(inti,"A1"); 
             continue;
            }
         parmArr[inti][2]++;

         if (selectMktBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0)
            {
             setValue("detl.error_desc"   , "團體代號不符");
             insertMktFstpCarddtl(inti,"A2");  
             continue;
            }
         parmArr[inti][3]++;

         if (selectMktBnData(getValue("source_code"),
                                getValue("parm.source_code_sel",inti),"3",3)!=0)
            {
             setValue("detl.error_desc"   , "來源代號不符");
             insertMktFstpCarddtl(inti,"A3");  
             continue;
            }
         parmArr[inti][4]++;

         if (selectMktBnData(getValue("card_type"),
                                getValue("parm.card_type_sel",inti),"4",3)!=0)
            {
             setValue("detl.error_desc"   , "卡種不符");
             insertMktFstpCarddtl(inti,"A4");  
             continue;
            }
         parmArr[inti][5]++;

         if (selectMktBnData(getValue("promote_dept"),
                                getValue("parm.promote_dept_sel",inti),"5",3)!=0)
            {
             setValue("detl.error_desc"   , "通路代號不符");
             insertMktFstpCarddtl(inti,"A5");  
             continue;
            }
         parmArr[inti][6]++;

         if (getValue("parm.new_hldr_cond",inti).equals("Y"))
            {
             if (idPSeqno.length()!=0)
                showLogMessage("I",""," STEP 2 首辦卡友判斷 card_no ["+ getValue("card_no") +"]");

             selInt = Integer.valueOf(getValue("parm.new_hldr_sel",inti));
             if (getValue("parm.new_hldr_flag",inti).equals("1"))
                {
                 if (idPSeqno.length()!=0)
                    showLogMessage("I",""," STEP 3 全新卡友 card_no ["+ getValue("card_no") +"]");

                 int new_hldr_sel =0;
                 if (selectCrdCardNewcard(inti)!=0) new_hldr_sel=1; 
                 if (new_hldr_sel==0)
                    if (selectEcsCrdCardNewcard(inti)!=0) new_hldr_sel=2;
                      
                 if ((getValue("parm.new_hldr_sel",inti).equals("1"))&&
                     (new_hldr_sel!=0))
                    {
                     if (idPSeqno.length()!=0)
                        showLogMessage("I",""," STEP 4 card_no ["+ getValue("card_no") +"]");
                     if (new_hldr_sel==1)
                        setValue("detl.error_desc"   , "卡檔非全新卡友不符");
                     else
                        setValue("detl.error_desc"   , "瘦身卡檔非全新卡友不符");
                     insertMktFstpCarddtl(inti,"A7");    
                     continue;
                    }
                 else if ((getValue("parm.new_hldr_sel",inti).equals("2"))&&
                     (new_hldr_sel==0))
                    {
                     if (idPSeqno.length()!=0)
                        showLogMessage("I",""," STEP 5 card_no ["+ getValue("card_no") +"]");
                     setValue("detl.error_desc"   , "卡檔全新卡友不符");
                     insertMktFstpCarddtl(inti,"A7");    
                     continue;
                    }
                }
             else if (getValue("parm.new_hldr_flag",inti).equals("2"))
                {
                 if (idPSeqno.length()!=0)
                    showLogMessage("I",""," STEP 6 於核卡日前 card_no ["+ getValue("card_no") +"]");

                 int new_hldr_sel = selectCrdCardNewcardgroup(inti,selInt);
                 if (new_hldr_sel!=0)
                    {
                     if (idPSeqno.length()!=0)
                        showLogMessage("I",""," STEP 6.A  新卡友判斷於核卡日前不符");
                     setValue("detl.error_desc"   , "新卡友判斷於核卡日前不符");
                     insertMktFstpCarddtl(inti,"A9");   
                     continue;
                    }
                }
            }
         if (idPSeqno.length()!=0)
            showLogMessage("I",""," STEP 7 card_no ["+ getValue("card_no") +"]");
         parmArr[inti][7]++;

         setValue("mfcl.id_p_seqno" , getValue("id_p_seqno"));
         setValue("mfcl.issue_date" , getValue("ori_issue_date"));
         setValue("mfcl.active_code", getValue("parm.active_code.inti"));
         int cnt1 = getLoadData("mfcl.id_p_seqno,mfcl.active_code,mfcl.issue_date");
         int ok_flag=0;
         for (int intm=0;intm<cnt1;intm++)
           {
            if (!getValue("mfcl.error_code",intm).equals("00")) continue;
            if (getNewseq(getValue("group_code"),getValue("card_type")).compareTo(
                getNewseq(getValue("mfcl.group_code",intm),getValue("mfcl.card_type",intm)))>0)
               {
                setValue("detl.error_desc"   , "同發卡日順序較大");
                insertMktFstpCarddtl(inti,"A8"); 
                ok_flag=1;
                break;
               }
            else
               {
                setValue("detl.error_desc"   , "同發卡日順序較大");
                updateMktFstpCarddtl(inti,"A8",getValue("mfcl.card_no",intm)); 
               }
           }
         if (ok_flag==1) continue;

         parmArr[inti][8]++;
         //modified by grace
         if ( "Y".equals(getValue("parm.list_cond",inti)) ) {
              if (getValue("parm.list_use_sel",inti).equals("1"))
                 {
                  if (getValue("parm.list_flag",inti).equals("1"))
                     {
                      setValue("milc.id_p_seqno" , getValue("major_id_p_seqno"));
                      //setValue("milt.list_data" , getValue("major_id_p_seqno"));	
                      setValue("milc.active_code", getValue("parm.active_code",inti));
                      //cnt1 =  getLoadData("milt.id_p_seqno,milc.active_code");	
                      cnt1 =  getLoadData("milc.id_p_seqno,milc.active_code");
                      if (cnt1==0) continue;
                     }
                  else 
                     {
                      setValue("milt.card_no"    , getValue("card_no"));
//                      setValue("milt.list_data" , getValue("card_no"));	
                      setValue("milt.active_code", getValue("parm.active_code",inti));
                      cnt1 =  getLoadData("milt.card_no,milt.active_code");
                      if (cnt1==0) continue;
                     }
                 }
              else
                 {
                  if (getValue("parm.list_flag",inti).equals("1"))
                     {
                      setValue("milc.id_p_seqno" , getValue("major_id_p_seqno"));
//                      setValue("milt.list_data" , getValue("major_id_p_seqno"));	
                      setValue("milc.active_code", getValue("parm.active_code",inti));
                      cnt1 =  getLoadData("milc.id_p_seqno,milc.active_code");
                      if (cnt1!=0) continue;
                     }
                  else 
                     {
                      setValue("milt.card_no"    , getValue("card_no"));
//                      setValue("milt.list_data" , getValue("card_no"));
                      setValue("milt.active_code", getValue("parm.active_code",inti));
                      cnt1 =  getLoadData("milt.card_no,milt.active_code");
                      if (cnt1!=0) continue;
                     }
                 }
             }

          if (getValue("group_code").length()==0)
             setValue("group_code" , "0000");

          setValue("data_key" , getValue("parm.active_code",inti));

          parmArr[inti][9]++;

         setValue("detl.error_desc"   , "");
         //showLogMessage("I","","selectCrdCard(), before insertMktFstpCarddtl(), active_code ["+ getValue("parm.active_code",inti) + "], cardno: "+getValue("card_no")+", totalCnt: "+totalCnt );
         insertMktFstpCarddtl(inti,"00");
        }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","selectCrdCard(), 處理筆數 ["+ totalCnt + "] 筆" );

  
  if (activeCode.length()!=0)
  for (int inti=0;inti<parmCnt;inti++)
     {
      showLogMessage("I","","    ["+String.format("%03d",inti)
                           + "] 基金 [" + getValue("parm.active_code",inti) 
                           + "]-[" + getValue("parm.active_name",inti) +"]"); 
   
      for (int intk=0;intk<20;intk++)
        {
         if (parmArr[inti][intk]==0) continue;
         showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
        }
   
     }
  showLogMessage("I","","=========================================");
    

 }
//************************************************************************
int checkMktFstpCarddtlH(String tmpIdPSeqno) throws Exception {
    //extendField = "pseq.";
	extendField = "dtlh.";	//grace 異動
    selectSQL = "count(*) as hcnt ";
    daoTable = "mkt_fstp_carddtl_h";
    whereStr = "WHERE ID_P_SEQNO    = ? ";

    setString(1, tmpIdPSeqno);
    selectTable();
    return getValueInt("hcnt");
}

// ************************************************************************
 int insertMktFstpCarddtl(int inti,String procCode) throws Exception
 {
  dateTime();
  setValue("detl.ori_card_no"        , getValue("ori_card_no"));
  setValue("detl.card_no"            , getValue("card_no"));
  setValue("detl.acct_type"          , getValue("acct_type"));
  setValue("detl.p_seqno"            , getValue("p_seqno"));
  setValue("detl.id_p_seqno"         , getValue("id_p_seqno"));
  setValue("detl.issue_date"         , getValue("issue_date"));
  setValue("detl.group_code"         , getValue("group_code"));
  setValue("detl.card_type"          , getValue("card_type"));
  setValue("detl.card_note"          , getValue("card_note"));
  setValue("detl.active_code"        , getValue("parm.active_code",inti));
  setValue("detl.achieve_cond"       , getValue("parm.achieve_cond",inti));
  setValue("detl.error_code"         , procCode);
  if (!procCode.equals("00"))
     {
      setValue("detl.proc_flag"       , "X"); 
      setValue("detl.proc_date"       , businessDate); 
     }
  else
     {
      setValue("detl.proc_flag"       , "N");
      setValue("detl.proc_date"       , "");
     }
   
  setValue("detl.nopurc_msg_pgm"  , "");
  setValue("detl.half_msg_pgm"  , "");
  setValue("detl.send_msg_pgm"  , "");
  if (procCode.equals("00"))
     {
      setValue("data_key"   , getValue("parm.active_code",inti));

      if (getValue("parm.sms_nopurc_cond",inti).equals("Y"))
         {
          if (getValue("parm.nopurc_g_cond",inti).equals("Y"))
             {
              if (selectMktBnData(getValue("group_code"),"","1","B",3)==0)
                 setValue("detl.nopurc_msg_pgm" , getValue("parm.nopurc_msg_pgm",inti));
             }
          else
             {
              setValue("detl.nopurc_msg_pgm" , getValue("parm.nopurc_msg_pgm",inti));
             }
         }

      if (getValue("parm.sms_half_cond",inti).equals("Y"))
         {
          if (getValue("parm.half_g_cond",inti).equals("Y"))
             {
              if (selectMktBnData(getValue("group_code"),"","1","C",3)==0)
                 setValue("detl.half_msg_pgm" ,  getValue("parm.half_msg_pgm",inti));
             }
          else
             {
              setValue("detl.half_msg_pgm" ,  getValue("parm.half_msg_pgm",inti));
             }
         }
           
      if (getValue("parm.sms_send_cond",inti).equals("Y"))
          setValue("detl.send_msg_pgm" ,  getValue("parm.send_msg_pgm",inti)); 
     }

  setValue("detl.last_execute_date" , "");
  setValue("detl.last_execute_date" ,comm.nextNDate(getValue("issue_date"),
                                     (getValueInt("parm.n1_days",inti)+getValueInt("parm.purchase_days",inti)+1)));

  setValue("detl.linebc_flag"     , "X"); 
  //setValue("detl.megalite_flag"   , "X");	//grace rename
  setValue("detl.banklite_flag"   , "X");	
  setValue("detl.selfdeduct_flag" , "X");
  setValue("detl.anulfee_flag"    , "X");
      
  if (getValue("parm.linebc_cond",inti).equals("Y"))
     setValue("detl.linebc_flag"     , "N"); 		
  /*
   if (getValue("parm.megalite_cond",inti).equals("Y"))			//grace rename 
       setValue("detl.megalite_flag"   , "N");
     */ 
  if (getValue("parm.banklite_cond",inti).equals("Y"))		 
      setValue("detl.banklite_flag"   , "N");
  if (getValue("parm.selfdeduct_cond",inti).equals("Y"))
     setValue("detl.selfdeduct_flag" , "N");
  if (getValue("parm.anulfee_cond",inti).equals("Y"))
     setValue("detl.anulfee_flag"    , "N");

  setValue("detl.record_flag"        , getValue("parm.record_cond",inti));

  if (getValue("parm.multi_fb_type",inti).equals("2"))
  if (procCode.equals("00"))
  if (getValue("detl.record_flag").equals("M"))
    {
     if (selectMktFstpParmseq(inti)==0)
        setValue("detl.record_flag"    , "Y");
    }
  setValue("detl.multi_fb_type"      , getValue("parm.multi_fb_type",inti)); 
  setValue("detl.crt_date"           , businessDate);
  setValue("detl.crt_time"           , sysTime);
  setValue("detl.mod_user"           , javaProgram); 
  setValue("detl.mod_time"           , sysDate+sysTime);
  setValue("detl.mod_pgm"            , javaProgram);

//showLogMessage("I",""," error_code  ["+ getValue("detl.error_code") +"]");
//showLogMessage("I",""," ACTIEV_CODE ["+ getValue("parm.active_code",inti) +"]");
//showLogMessage("I",""," NOPURC_MSG  ["+ getValue("detl.nopurc_msg_pgm") +"]");
//showLogMessage("I",""," p_seqno     ["+ getValue("detl.p_seqno") +"]");
//showLogMessage("I",""," card_no     ["+ getValue("detl.card_no") +"]");
  
  extendField = "detl.";
  daoTable  = "mkt_fstp_carddtl";
  
//  debugInsert="Y";	//檢核寫檔各欄位值
  
  insertTable();

//if ( dupRecord.equals("Y") )
   //showLogMessage("I","","  insert mkt_fstp_carddtl dup card_no ["+ getValue("detl.card_no") +"]" );

  return(0);
 }
// ************************************************************************
 int selectMktFstpParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_fstp_parm";
  whereStr  = "WHERE apr_flag        = 'Y' "
            + "AND   apr_date       != ''  "
            + "AND   (stop_flag     != 'Y'  "
            + " or    (stop_flag     = 'Y'  "
            + "  and  stop_date      > ? )) "
            + "and ? between issue_date_s and issue_date_e "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";  

      setString(3 , activeCode);
     }

  parmCnt = selectTable();

  if (parmCnt==0) return(1);

  minIssueDate = "99999999";
  maxIssueDate = "00000000";
  for (int inti=0;inti<parmCnt;inti++)
    {
     procFlag[inti]="Y";
     if (getValue("parm.issue_date_e",inti).compareTo(businessDate)<0) 
        {
         procFlag[inti]="N";
         continue;
        }

     if (getValue("parm.multi_fb_type",inti).equals("2"))
        {
         pseqCnt=0;
         selectMktFstpParmseq(inti);
         if (pseqCnt==0)
            {
             procFlag[inti]="N";
             continue;
            }
        }

     showLogMessage("I","","活動代號:["+ getValue("parm.active_code",inti) +"]-"+getValue("parm.active_name",inti));
     if (getValue("parm.issue_date_e",inti).length()==0)
        setValue("parm.issue_date_e", "30001231", inti);
     if (getValue("parm.issue_date_s",inti).compareTo(minIssueDate)<0)
        minIssueDate = getValue("parm.issue_date_s",inti);
     if (getValue("parm.issue_date_e",inti).compareTo(maxIssueDate)>0)
        maxIssueDate = getValue("parm.issue_date_e",inti);
     showLogMessage("I","","=========================================");
//   delete_mkt_fstp_carddtl(inti);   // not to work better , use rerun  program to test,or delete mkt_fstp_carddtl 
    }
  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  showLogMessage("I","","   日期區間: ["+ minIssueDate+"]-["+ maxIssueDate+"]");
  return(0);
 }
// ************************************************************************
 int selectMktFstpParmseq(int inti) throws Exception
 {
  extendField = "pseq.";
  selectSQL = "count(*) as pseqcnt,"
            + "max(record_cond) as record_cond";
  daoTable  = "mkt_fstp_parmseq";
  whereStr  = "WHERE active_code    = ? ";

  setString(1 , getValue("parm.active_code",inti));

  selectTable();

  pseqCnt = getValueInt("pseq.pseqcnt");

  if (!getValue("pseq.record_cond").equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 void loadMktFstpCarddtl() throws Exception
 {
  extendField = "mfcl.";
  selectSQL = "card_no,"
            + "id_p_seqno,"
            + "issue_date,"
            + "group_code,"
            + "card_type,"
            + "error_code,"
            + "active_code";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where active_code in "
            + "      (select active_code "
            + "       from   mkt_fstp_parm "
            + "       WHERE apr_flag        = 'Y' "
            + "       AND   apr_date       != ''  "
            + "       AND   (stop_flag     != 'Y'  "
            + "        or    (stop_flag     = 'Y'  "
            + "         and  stop_date      > ? ))) "
            ;

  setString(1 , businessDate);

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";  

      setString(2 , activeCode);
     }

  if (idPSeqno.length()>0)
     { 
      whereStr  = whereStr 
                + "and id_p_seqno = ? ";  

      setString(3 , idPSeqno);
     }

  whereStr  = whereStr 
            + "order by id_p_seqno,active_code ";

  int  n = loadTable();

  setLoadData("mfcl.id_p_seqno,mfcl.active_code,mfcl.issue_date");
  setLoadData("mfcl.card_no,mfcl.active_code");

  showLogMessage("I","","Load mkt_fstp_carddtl count : ["+n+"]");
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
 void loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_FSTP_PARM' "
            + "order by data_key,data_type,data_code,data_code2 ";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data2.data_key,data2.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 void selectMktBnDataCard() throws Exception
 {
  extendField = "prio.";
  selectSQL = "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "where table_name = 'MKT_FSTP_PARM_CARD' "
            + "and   data_key = 'FSTP_CARD_PRIORITY' "
            + "order by data_code,data_code2 desc";

  newSeq = selectTable();

  showLogMessage("I","","select mkt_bn_data_card Count: ["+ newSeq +"]");
 }
// ************************************************************************
 String getNewseq(String groupCode,String cardType) throws Exception
 {
  for (int inti=0;inti<newSeq;inti++)
      {
       if (!groupCode.equals(getValue("prio.data_code",inti))) continue;
       if (getValue("prio.data_code2",inti).length()==0)
          {
           return(getValue("prio.data_type",inti));
          }
       if (!cardType.equals(getValue("prio.data_code2",inti))) continue;
       return(getValue("prio.data_type",inti));
      }
  return("9999");
 }
// ************************************************************************
 int selectCrdCardNewcard(int intk) throws Exception 
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

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("ori_card_no"));
  setString(3 , getValue("ori_issue_date"));

  int recCnt = selectTable();

  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************
 int selectEcsCrdCardNewcard(int intk) throws Exception 
 {
  extendField = "card.";
  selectSQL = "a.group_code,"
            + "a.card_type,"
            + "a.oppost_date";
  daoTable  = "ecs_crd_card a";
  whereStr  = "WHERE  a.id_p_seqno     = ? "
            + "AND    a.ori_card_no   !=  ? "
            + "AND    a.ori_issue_date < ? "
            ;

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("ori_card_no"));
  setString(3 , getValue("ori_issue_date"));

  int recCnt = selectTable();

  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktFstpCarddtl(int inti,String errorCode,String cardNo) throws Exception
 {
  daoTable  = "mkt_fstp_carddtl";
  updateSQL = "error_code        = ?,"
            + "error_desc         = ?,"
            + "proc_flag         = 'X',"
            + "proc_Date         = ?,"
            + "mod_time          = sysdate,"
            + "mod_pgm           = ? ";
  whereStr  = "where active_code   = ? "
            + "and   card_no       = ? "
            ;

  setString(1 , errorCode);
  setString(2 , getValue("detl.error_desc"));
  setString(3 , businessDate);
  setString(4 , javaProgram);
  setString(5 , getValue("parm.active_code",inti));
  setString(6 , cardNo);

  int n = updateTable();
  return n;
 }
// ************************************************************************
 void deleteMktFstpCarddtl(int inti) throws Exception
 {
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where  active_code  = ? "
            + "and    issue_date = ? "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2, businessDate);

  int n = deleteTable();

  if (n>0)
     showLogMessage("I","","  刪除活動代號 ["+ getValue("parm.active_code",inti) + "] ["+ n +"] 筆" );

  return;
 }
// ************************************************************************
 void selectCrdCardTest() throws Exception
 {
  selectSQL = "card_no,"
            + "acct_type,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "card_note,"
            + "issue_date,"
            + "group_code,"
            + "source_code,"
            + "promote_dept,"
            + "ori_card_no,"
            + "ori_issue_date,"
            + "card_type";
  daoTable  = "crd_card";
  whereStr  = "where id_p_seqno = '0001449936' "
            + "and   card_no = '"+ idPSeqno + "' "
            + "order by issue_date "
            ;


  openCursor();

  while( fetchTable() ) 
   {
  showLogMessage("I",""," STEP 1 card_no    ["+ getValue("card_no") +"]");
  showLogMessage("I","","        issue_date ["+ getValue("ori_issue_date") +"]");

    for (int inti=0;inti<parmCnt;inti++)
        {
         if (procFlag[inti].equals("N")) continue;
         setValue("data_key"   , getValue("parm.active_code",inti));

         if (getValue("parm.new_hldr_cond",inti).equals("Y"))
            {
             selInt = Integer.valueOf(getValue("parm.new_hldr_sel",inti));
             if (getValue("parm.new_hldr_flag",inti).equals("1"))
                {
                 int new_hldr_sel =0;
                 if (selectCrdCardNewcard(inti)!=0) new_hldr_sel=1; 
                 if (new_hldr_sel==0)
                    if (selectEcsCrdCardNewcard(inti)!=0) new_hldr_sel=2;
                      
                 if ((getValue("parm.new_hldr_sel",inti).equals("1"))&&
                     (new_hldr_sel!=0))
                    {
                     if (new_hldr_sel==1)
  showLogMessage("I",""," STEP 01 msg [卡檔非全新卡友不符]");
                     else
  showLogMessage("I",""," STEP 01 msg [瘦身卡檔非全新卡友不符]");
                     continue;
                    }
                 else if ((getValue("parm.new_hldr_sel",inti).equals("2"))&&
                     (new_hldr_sel==0))
                    {
  showLogMessage("I",""," STEP 01 msg [卡檔全新卡友不符]");
                     continue;
                    }
                }
             else if (getValue("parm.new_hldr_flag",inti).equals("2"))
                {
                 int new_hldr_sel = selectCrdCardNewcardgroup(inti,selInt);
                 if (new_hldr_sel!=0)
                    {
  showLogMessage("I",""," STEP 01 msg [新卡友判斷於核卡日前不符]");
                     continue;
                    }
                }
            }
/*
         setValue("mfcl.id_p_seqno" , getValue("id_p_seqno"));
         setValue("mfcl.issue_date" , getValue("ori_issue_date"));
         setValue("mfcl.active_code", getValue("parm.active_code.inti"));
         int cnt1 = getLoadData("mfcl.id_p_seqno,mfcl.active_code,mfcl.issue_date");
         int ok_flag=0;
         for (int intm=0;intm<cnt1;intm++)
           {
            if (!getValue("mfcl.error_code",intm).equals("00")) continue;
            if (get_newseq(getValue("group_code"),getValue("card_type")).compareTo(
                get_newseq(getValue("mfcl.group_code",intm),getValue("mfcl.card_type",intm)))>0)
               {
                setValue("detl.error_desc"   , "同發卡日順序較大");
                ok_flag=1;
                break;
               }
            else
               {
                setValue("detl.error_desc"   , "同發卡日順序較大");
                update_mkt_fstp_carddtl(inti,"A8",getValue("mfcl.card_no",intm)); 
               }
           }
         if (ok_flag==1) continue;
*/
        }

   } 
  closeCursor();
 }
// ************************************************************************
 int selectCrdCardNewcardgroup(int intk,int hldrSel) throws Exception 
 {
  if (idPSeqno.length()!=0)
     showLogMessage("I",""," STEP 6.0   1.首辦 2.非首辦 ["+ hldrSel +"]");
  extendField = "card.";
  selectSQL = "a.group_code,"
            + "a.card_no,"
            + "a.card_type,"
            + "a.oppost_date,"
            + "b.card_indicator";
  daoTable  = "crd_card a,ptr_acct_type b";
  whereStr  = "WHERE  a.id_p_seqno     = ? "
            + "AND    a.ori_card_no   !=  ? "
            + "AND    a.ori_issue_date < ? "
            + "AND    a.sup_flag       = '0' "
            + "AND    a.acct_type = b.acct_type "
            ;

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("ori_card_no"));
  setString(3 , getValue("ori_issue_date"));

  int recCnt = selectTable();

  int new_flag=0;
  int new_flag2=0;

  if (idPSeqno.length()!=0)
     showLogMessage("I",""," STEP 6.1  讀取筆數 ["+ recCnt +"]");
  if (recCnt==0)
     {
      if (idPSeqno.length()!=0)
         showLogMessage("I",""," STEP 6.01  [ 全新卡友 ]");
      if (hldrSel==2) return(1);
      else return(0);
     }

  for ( int inti=0; inti<recCnt; inti++ )
      {
       if (idPSeqno.length()!=0)
          {
           showLogMessage("I",""," STEP 6.2  card_no["+ getValue("card.card_no",inti) +"]");
           showLogMessage("I",""," STEP 6.3  1.首辦/全新卡友 2.於核卡日前 ["+ getValue("parm.new_hldr_flag",intk) +"]");
          }

       if (getValue("parm.new_hldr_flag",intk).equals("2"))
          {
           if (idPSeqno.length()!=0)
              showLogMessage("I",""," STEP 6.40  未持有團代 ["+ getValue("parm.new_group_cond",intk) +"]");
           if (getValue("parm.new_group_cond",intk).equals("Y"))
              {
               if (idPSeqno.length()!=0)
                  showLogMessage("I",""," STEP 6.41  團代 ["+ getValue("card.group_code",inti) +"]");
                if (selectMktBnData(getValue("card.group_code",inti),"",
                                    "1","G",3)!=0) continue;
               }
           if (idPSeqno.length()!=0)
              {
               showLogMessage("I",""," STEP 6.41  持有團代 ["+ getValue("parm.new_group_cond",intk) +"]");
               showLogMessage("I",""," STEP 6.42  前 N 日 停卡日 ["+ getValue("card.oppost_date",inti) +"]");
              }

           if (getValue("card.oppost_date",inti).length()==0)  
              {
               if (idPSeqno.length()!=0)
                  showLogMessage("I",""," STEP 6.44  [ 非全新卡友 ]");  
               new_flag=1;
               break;
              }
           else 
              {
               if (idPSeqno.length()!=0)
                  showLogMessage("I",""," STEP 6.43  前 N 日 日期 ["+ 
                                 comm.nextNDate(getValue("card.oppost_date",inti),
                                 getValueInt("parm.new_hldr_days",intk)) +"]");

               if (comm.nextNDate(getValue("card.oppost_date",inti),
                   getValueInt("parm.new_hldr_days",intk)).compareTo(getValue("ori_issue_date"))<0) continue;
              }
           if (idPSeqno.length()!=0)
              showLogMessage("I",""," STEP 6.44  [ 前 N 日 ]");

          }
       else 
          {
           if (idPSeqno.length()!=0)
              showLogMessage("I",""," STEP 6.5  card_no["+ getValue("card.card_no",inti) +"]");
           if (getValue("card.oppost_date",inti).length()==0)  
              {
               new_flag=1;
               break;
              }
          }

       if (getValue("parm.new_group_cond",intk).equals("Y"))
          {
           if (hldrSel==3) new_flag2=1;
          }
       new_flag=1;
      }
   if (idPSeqno.length()!=0)
      {
       showLogMessage("I",""," STEP 6.6  new)flag ["+ new_flag +"]");
       showLogMessage("I","","           new)flag2 ["+ new_flag2 +"]");
      }

  if (hldrSel==1) 
     {
      if (idPSeqno.length()!=0)
        showLogMessage("I",""," STEP 6.7  [1.首辦 ]");
      if (new_flag==0) return(0);
      else return(1);
     }
  else if (hldrSel==2) 
     {
      if (idPSeqno.length()!=0)
         showLogMessage("I",""," STEP 6.8  [2.非首辦]");
      if (new_flag==0) 
         {
      if (idPSeqno.length()!=0)
         showLogMessage("I",""," STEP 6.81  [ 全新卡友 ]");
          return(1);
         }
      else 
         {
         if (idPSeqno.length()!=0)
            showLogMessage("I",""," STEP 6.82  [ 非全新卡友 ]");
          return(0);
         }
     }
  else
     {
      if (idPSeqno.length()!=0)
         showLogMessage("I",""," STEP 6.9  new)flag2 ["+ new_flag2 +"]");
      if (new_flag2==0) return(0);
      else return(1);
     }
 }

 // ************************************************************************
 void loadMktImfstpList() throws Exception {
     extendField = "milt.";
     selectSQL = "active_code," + "id_p_seqno," + "card_no ";	//grace resume
     //selectSQL = "active_code," + "list_data ";
     daoTable = "mkt_imfstp_list";
     whereStr = "where active_code in ( "
             + "      select active_code "
             //+ "      from mkt_channel_parm "
             + "      from mkt_fstp_parm "
             + "      where apr_flag      = 'Y' "
             + "	  AND   apr_date      != ''  "	//grace add
             + "      and     ((stop_flag != 'Y') "
             + "       or      (stop_flag = 'Y' "
             + "        and     stop_date > ? )) "
             + "	  and ? between issue_date_s and issue_date_e "	//grace add
             //grace remark (begin) -------------------------
             //+ "      and     feedback_apr_date = '' "
             //+ "      and     cal_def_date      = ?  "
             //grace remark (end) -------------------------
             + "      and     list_cond         = 'Y' )  "
             + "	  and   list_flag != '1'    "
             ;
     
     
     setString(1, businessDate);
     setString(2, businessDate);

     int n = loadTable();
     setLoadData("milt.id_p_seqno,milt.active_code");
     setLoadData("milt.card_no,milt.active_code");
     //setLoadData("milt.list_data,milt.active_code");	//remarked by grace

     showLogMessage("I", "", "Load mkt_imfstp_list Count: [" + n + "]");
 }
//************************************************************************
void loadMktImfstpListCid() throws Exception {
    extendField = "milc.";
    selectSQL = "a.active_code,"
    		  + "b.id_p_seqno";
    daoTable  = "mkt_imfstp_list a,crd_idno b";
    whereStr  = "where active_code in ( "
              + "      select active_code "
              + "      from mkt_fstp_parm "
              + "      where apr_flag      = 'Y' "
              + "	   AND   apr_date      != ''  "
              + "      and     ((stop_flag != 'Y') "
              + "       or      (stop_flag = 'Y' "
              + "        and     stop_date > ? )) "
              + "	   and ? between issue_date_s and issue_date_e "              
              + "      and     list_cond         = 'Y' )  "
              + "	   and   a.list_data = b.id_no   "
              + "	   and   a.list_flag = '1'    "
              ;

setString(1 , businessDate); 
setString(2 , businessDate); 

int  n = loadTable();
setLoadData("milc.id_p_seqno,milc.active_code");

showLogMessage("I","","Load mkt_imfstp_list_cid Count: ["+n+"]");
}// ************************************************************************

}  // End of class FetchSample
