/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/12/03  V1.00.20  Allen Ho   New                                        *
* 111/10/13  V1.00.21  Suzuwei    sync from mega & updated for project coding standard   * 
* 112/04/26  V1.00.22  Zuwei Su   增‘多贈品匯入名單(多檔)’篩選, bug修訂   * 
* 112/07/18  V1.00.23  Castor     增加‘票證加值’ 判讀                         * 
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
public class MktC410 extends AccessDAO
{
 private final String PROGNAME = "首刷禮-消費處理程式 112/07/18 V1.00.23";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

// String businessDate = "";
 String lastDate = "";
 String minDate = "";
 String acqId  = "";
 String activeCode ="";
 int  parmCnt  = 0;
 int  pseqCnt  = 0;
 int  returnAmt = 0;
 int  returnCnt = 0;

 int     totalCnt=0;
 int     okCnt=0;
 String[] procFlag = new String[200];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC410 proc = new MktC410();
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   activeCode = "";
   if ( args.length == 2 )
       activeCode = args[1];

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktFstpParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","無符合首刷禮參數資料");
       showLogMessage("I","","=========================================");
       return(0);
      }
   if (loadMktFstpCarddtl()==0)
      {
       showLogMessage("I","","本日無符合首刷禮卡友資料");
       showLogMessage("I","","=========================================");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(bil_bill)資料");
   selectBilBill();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","Final: Insert count: " + okCnt + "   mkt_fstp_purcdtl");

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
 void  selectBilBill() throws Exception
 {
  selectSQL = "dest_amt,"
            + "curr_adjust_amt,"
            + "purchase_date,"
            + "sign_flag,"
            + "acct_type,"
            + "acct_code,"
            + "install_curr_term,"
            + "contract_amt,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "acq_member_id,"
            + "issue_date,"
            + "pos_entry_mode,"
            + "ucaf,"
            + "ec_ind,"
            + "mcht_no,"
            + "mcht_category,"
            + "major_card_no,"
            + "card_no,"
            + "contract_no,"
            + "acct_month,"
            + "post_date,"
            + "decode(acct_date,'',post_date,acct_date) as acct_date,"
            + "reference_no,"
            + "mcht_chi_name,"
            + "ecs_platform_kind";
  daoTable  = "bil_bill";
  whereStr  = "where acct_code in ('BL','IT','ID','CA','OT','AO') "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            + "and   post_date between ? and ? "
            + "and   (acct_date   = '' "
            + " or    (acct_date !='' "
            + " and    acct_date  = ? )) "
            + "and   issue_date >= ? "
//          + "and   p_seqno = '0002234197' "  // debug 202010013
//          + "and   reference_no in ('2022126117') "   // debug 20201203
            ;

  setString(1, comm.nextNDate(businessDate,-5));
  setString(2, businessDate);
  setString(3, businessDate);
  setString(4, minDate);

  openCursor();

  double[][] parmArr = new double [parmCnt][30];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<30;intk++) parmArr[inti][intk]=0;

  int ok_flag=0;  // test flag
  while( fetchTable() ) 
   {
    totalCnt++;
    //showLogMessage("I",""," selectBilBill(), totalCnt++ = "+totalCnt);
    if (getValueInt("dest_amt") + getValueInt("curr_adjust_amt")==0) continue;

    setValueInt("dest_amt" , getValueInt("dest_amt")+getValueInt("curr_adjust_amt"));

    if (!getValue("sign_flag").equals("+"))
       setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);

    for (int inti=0;inti<parmCnt;inti++)
        {
         if (procFlag[inti].equals("N")) continue;
         //檢核票證加值
         if (getValue("parm.add_value_cond",inti).equals("Y")) {
             if( !(getValue("mcht_chi_name").contains("一卡通加值")|
            		 getValue("mcht_chi_name").contains("悠遊卡加值")|
            		 getValue("mcht_chi_name").contains("愛金卡加值")|
            		 getValue("ecs_platform_kind").equals("ET")|
            		 getValue("ecs_platform_kind").equals("EP")|
            		 getValue("ecs_platform_kind").equals("EI"))) continue;
         }
         parmArr[inti][0]++;
         if (getValue("purchase_date").compareTo(getValue("parm.issue_date_s",inti))<0) continue;

         if (getValue("purchase_date").compareTo(
           comm.nextNDate(getValue("issue_date"),(getValueInt("parm.purchase_days",inti)+1)))>0) continue;
         if (getValue("post_date").compareTo(
           comm.nextNDate(getValue("issue_date"),
                         (getValueInt("parm.n1_days",inti)+getValueInt("parm.purchase_days",inti)+1)))>0) continue;

         parmArr[inti][1]++;

         if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
         if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) continue;
         if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
         if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
         if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) continue;
         if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;

         if ((getValue("acct_code").equals("IT"))&&(getValue("parm.it_flag",inti).equals("1")))
            {
             if (getValueInt("install_curr_term")!=1) continue;
             if (selectBilContract()!=0) continue;
             setValue("dest_amt" , getValue("contract_amt"));
            }

         parmArr[inti][2]++;

         setValue("mfcl.card_no"     , getValue("major_card_no"));
         setValue("mfcl.active_code" , getValue("parm.active_code",inti));
         int cnt1 = getLoadData("mfcl.card_no,mfcl.active_code");
         if (cnt1==0) continue; // 卡片不符

         parmArr[inti][3]++;

         if (getValue("purchase_date").compareTo(
           comm.nextNDate(getValue("mfcl.issue_date"),(getValueInt("parm.purchase_days",inti))+1))>0) continue;
            
         parmArr[inti][4]++;

         setValue("data_key" , getValue("parm.active_code",inti));

         parmArr[inti][5]++;

         if (selectMktBnData(getValue("mcht_category"),
                                getValue("parm.mcc_code_sel",inti),"6",3)!=0) continue;
         parmArr[inti][6]++;
    
         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("acq_member_id"),8);

         if (getValue("parm.mcht_seq_flag",inti).equals("N"))
            if (selectMktBnData(getValue("mcht_no"),acqId ,
                                   getValue("parm.merchant_sel",inti),"7",3)!=0) continue;
         parmArr[inti][7]++;

         if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                                     getValue("parm.mcht_group_sel",inti),"8")!=0) continue;
         parmArr[inti][8]++;

         if (selectMktBnData(getValue("pos_entry_mode"),
                                getValue("parm.pos_entry_sel",inti),"11",3)!=0) continue;
         parmArr[inti][9]++;

         if (selectMktBnData(getValue("ucaf"),
                                getValue("parm.ucaf_sel",inti),"12",3)!=0) continue;
         parmArr[inti][10]++;

         if (selectMktBnData(getValue("ec_ind"),
                                getValue("parm.eci_sel",inti),"13",3)!=0) continue;

         setValue("purc.in_flag" , "N");
         setValueDouble("purc.in_dest_amt" , 0 );
         int ex_flag = 0;
         if (getValue("parm.mcht_in_cond",inti).equals("Y"))
            {
             if (selectMktBnData(getValue("mcht_no"),acqId,
                                    getValue("parm.int_merchant_sel",inti),"9",3)==0) ex_flag++;

             if (ex_flag==0)
             if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                                         getValue("parm.mcht_group_sel",inti),"10")==0) ex_flag++;
             if (ex_flag!=0) 
                {
                 setValueDouble("purc.in_dest_amt" , getValueDouble("dest_amt"));
                 setValue("purc.in_flag" , "Y");
                }
            }
         parmArr[inti][11]++;

         if (getValue("parm.mcht_seq_flag",inti).equals("Y"))
            {
             selectMktFstpParmseq1(inti);
             for (int intk=0;intk<pseqCnt;intk++)
               {
                if (getValue("pseq.pur_date_sel",intk).equals("2"))
                   {
                    if (getValue("purchase_date").compareTo(
                        comm.nextNDate(getValue("issue_date"),(getValueInt("pseq.purchase_days",intk)+1)))>0) continue;
                    if (getValue("post_date").compareTo(
                        comm.nextNDate(getValue("issue_date"),
                         (getValueInt("parm.n1_days",inti)+getValueInt("pseq.purchase_days",intk)+1)))>0) continue;
                   }

                setValue("data_key" , getValue("parm.active_code",inti)
                                    + getValue("pseq.active_seq",intk)  );

                if (selectMktBnData(getValue("mcht_no"),acqId ,
                                       getValue("pseq.merchant_sel",intk),"7",3)!=0) continue;

                if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                                           getValue("pseq.mcht_group_sel",intk),"8")!=0) continue;

                // 增加匯入名單檢核
                if ("Y".equals(getValue("pseq.list_cond")) && selectMktImfstpList(intk) > 0) {
                    continue;
                }

                setValue("purc.active_seq"    , getValue("pseq.active_seq",intk));
                insertMktFstpPurcdtl(inti);
               }
            }
         else
            {
             setValue("purc.active_seq"    , "00");
             insertMktFstpPurcdtl(inti);
            }
        }

    processDisplay(10000); // every 10000 display message
    if (ok_flag==1) break;    // test only flag, don't remove
   } 
  closeCursor();
  
  if (activeCode.length()!=0)
  {
  showLogMessage("I","","=========================================");
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++)
       {
        if (parmArr[inti][intk]==0) continue;
        showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
       };  
  showLogMessage("I","","=========================================");
  }
 }
// ************************************************************************
 int insertMktFstpPurcdtl(int inti) throws Exception
 {
  dateTime();
  extendField = "purc.";
  setValue("purc.card_no"            , getValue("card_no"));
  setValue("purc.major_card_no"      , getValue("major_card_no"));
  setValue("purc.active_code"        , getValue("parm.active_code",inti));
  setValue("purc.ori_major_card_no"  , getValue("mfcl.ori_major_card_no"));
  setValue("purc.acct_type"          , getValue("acct_type"));
  setValue("purc.p_seqno"            , getValue("p_seqno"));
  setValue("purc.id_p_seqno"         , getValue("id_p_seqno"));
  setValue("purc.acct_date"          , getValue("acct_date"));
  if (getValue("acct_date").length()==0)
     setValue("purc.acct_date"       , getValue("post_date"));
  setValue("purc.acct_month"         , getValue("acct_month"));
  setValue("purc.reference_no"       , getValue("reference_no"));
  setValue("purc.purchase_date"      , getValue("purchase_date"));
  setValue("purc.dest_amt"           , getValue("dest_amt"));
  setValue("purc.crt_date"           , businessDate);
  setValue("purc.mod_time"           , sysDate+sysTime);
  setValue("purc.mod_pgm"            , javaProgram);

  daoTable  = "mkt_fstp_purcdtl";

  int rcode = insertTable();
  
  if (rcode != 0 && !"Y".equals(dupRecord)) {
      okCnt++;
  }

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
            + "and   issue_date_s <= ? "
            + "and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "             (purchase_days+n1_days+1)  days,'yyyymmdd') >= ?  "
            ;             

  if (activeCode.length()>0) 
     whereStr  = whereStr 
               + "and active_code = ? ";  

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);
  if (activeCode.length()>0) 
     setString(4 , activeCode);

  parmCnt = selectTable();

  minDate = "99999999";
  int matchInt=parmCnt;
  for (int inti=0;inti<parmCnt;inti++)
    {
     procFlag[inti]="Y";

     if (getValue("parm.mcht_seq_flag",inti).length()==0) 
         setValue("parm.mcht_seq_flag", "N" ,inti);
          
     if (getValue("parm.multi_fb_type",inti).equals("2"))
        {
         pseqCnt=0;
         selectMktFstpParmseq(inti);
         if (pseqCnt==0)
            {
             procFlag[inti]="N";
             matchInt--;
             continue;
            }
        }

     showLogMessage("I","","活動代號:["+ getValue("parm.active_code",inti) +"]-"+getValue("parm.active_name",inti));
     if (getValue("parm.issue_date_s",inti).compareTo(minDate)<0)
        minDate = getValue("parm.issue_date_s",inti);
     deleteMktFstpPurcdtl(inti);
    }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]  符合["+matchInt+"]筆" );
  if (parmCnt>0)
     showLogMessage("I","","    最小發卡日: ["+ minDate+"]");
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

  return(0);
 }
// ************************************************************************
 int selectMktFstpParmseq1(int inti) throws Exception
 {
  extendField = "pseq.";
  selectSQL = "active_seq,"
            + "active_code,"
            + "list_cond,"
            + "purchase_days,"
            + "purchase_days,"
            + "pur_date_sel,"
            + "merchant_sel,"
            + "mcht_group_sel";
  daoTable  = "mkt_fstp_parmseq";
  whereStr  = "WHERE active_code    = ? "
          + "order by active_code, active_seq";

  setString(1 , getValue("parm.active_code",inti));

  pseqCnt =  selectTable();

  return(0);
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
  whereStr  = "WHERE TABLE_NAME  in ('MKT_FSTP_PARM','MKT_FSTP_PARMSEQ') "
            + "order by data_key,data_type,data_code,data_code2 ";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
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

  int ok_flag=0;
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
     ok_flag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (ok_flag==0) return(1);
      return(0);
     }
  else
     {
      if (ok_flag==0) return(0);
      return(1);
     }
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
 int selectMktImfstpList(int inti) throws Exception {
     extendField = "milt.";
     selectSQL = "count(*) as imfstpcnt ";
     daoTable = "mkt_imfstp_list ";
     whereStr = "WHERE active_code = ? "
             + "And active_seq = ? ";

     setString(1, getValue("pseq.active_code", inti));
     setString(2, getValue("pseq.active_seq", inti));
     selectTable();

     int cnt = getValueInt("milt.imfstpcnt");
     if (cnt == 0) {
         return 0;
     }
     
     sqlCmd = "select count(*) as flag1cnt "
             + "from mkt_imfstp_list a, MKT_FSTP_PURCDTL b "
             + "WHERE a.active_code = b.ACTIVE_CODE "
             + "AND a.active_code=? "
             + "AND a.list_data = (select id_no from crd_idno where id_p_seqno = ?) "
             + "AND a.list_flag='1' ";
     setString(1, getValue("pseq.active_code", inti)); // active_code
     setString(2, getValue("id_p_seqno")); // bil_bill.id_p_seqno
     selectTable();
     cnt = getValueInt("milt.flag1cnt");
     if (cnt > 0) {
         return 1;
     }
     
     sqlCmd = "Select count(*) as flag2cnt "
             + "from mkt_imfstp_list a, MKT_FSTP_PURCDTL b "
             + "WHERE a.active_code = b.ACTIVE_CODE "
             + "AND a.active_code=? "
             + "AND a.list_data = (select card_no from crd_card where card_no = ?) "
             + "AND a.list_flag<>'1' ";
     setString(1, getValue("pseq.active_code", inti)); // active_code
     setString(2, getValue("card_no")); // bil_bill.card_no
     selectTable();
     cnt = getValueInt("milt.flag2cnt");
     if (cnt > 0) {
         return 1;
     }

     return (0);
 }
// ************************************************************************
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,mkt_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME in ('MKT_FSTP_PARM','MKT_FSTP_PARMSEQ') "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  in ('8','10') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();
  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int loadMktFstpCarddtl() throws Exception
 {
  extendField = "mfcl.";
  selectSQL = "a.active_code,"
            + "b.card_no,"
            + "a.card_no as ori_major_card_no,"
            + "a.id_p_seqno,"
            + "a.issue_date,"
            + "a.last_execute_date";
  daoTable  = "mkt_fstp_carddtl a,crd_card b";          // 相同原始卡號之所有卡
  whereStr  = "where a.proc_flag     = 'N' "   
            + "and   a.error_code    = '00' "
            + "and   a.p_seqno       = b.p_seqno "
            + "and   a.card_no       = b.ori_card_no "
            + "and   a.active_code in ( "
            + "      select active_code "
            + "      from mkt_fstp_parm "
            + "      where apr_flag        = 'Y' "
            + "      and   apr_date       != ''  "
            + "      and   (stop_flag     != 'Y'  "
            + "       or    (stop_flag     = 'Y'  "
            + "        and  stop_date      > ? )) "
            + "      and   issue_date_s <= ? "
            + "      and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "                   (purchase_days+1) days,'yyyymmdd') >= ?) "
            + "order by a.card_no,a.active_code "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);
            ;

  int  n = loadTable();
  setLoadData("mfcl.card_no,mfcl.active_code");

  showLogMessage("I","","Load mkt_fstp_carddtl Count: ["+n+"] (businessDate:"+businessDate+")");
  return(n);
 }
// ************************************************************************
 int selectBilContract() throws Exception
 {
  selectSQL = "1 as check_flag";
  daoTable  = "bil_contract";
  whereStr  = "WHERE contract_no = ? "
            + "and   refund_apr_Date !='' "
            ;

  setString(1 , getValue("contract_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return 0;

  return 1;
 }
// ************************************************************************
 void deleteMktFstpPurcdtl(int inti) throws Exception
 {
  daoTable  = "mkt_fstp_purcdtl";
  whereStr  = "where  active_code  = ? "
            + "and    acct_date    = ? "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , businessDate);

  int n = deleteTable();

  if (n>0)
     showLogMessage("I","","  刪除活動代號 ["+ getValue("parm.active_code",inti) + "] ["+ n +"] 筆" );

  return;
 }
// ************************************************************************


}  // End of class FetchSample
