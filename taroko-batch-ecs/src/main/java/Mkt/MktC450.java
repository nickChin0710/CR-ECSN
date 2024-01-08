/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/11/22  V1.00.00  Allen Ho   New                                        *
* 109-12-08  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;                             
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC450 extends AccessDAO
{
 private  String progname = "首刷禮 - 簡訊發送檢核處理程式  109/12/08 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String hBusiBusinessDate = "";
 String lastDate = "";
 String classCode = "";
 int  parmCnt  = 0;
 int  returnAmt = 0;
 int  returnCnt = 0;

 int     totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC450 proc = new MktC450();
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
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   selectMktFstpParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","未定義灣數! ");
       return(0);
      }


   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktFstpPurcdtl();
   loadMktBnData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(crd_card)資料");
   selectMktFstpCarddtl();
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
 void  selectMktFstpCarddtl() throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where error_code  = '00' "
            + "and  ( proc_flag   = 'N' "
            + " or   (proc_flag   = 'Y' "
            + "  and  sms_send_flag !='Y')) "
            ;

  openCursor();

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  while( fetchTable() ) 
   {
    totalCnt++;

    setValue("data_key"        , getValue("active_code")); 

    for (int inti=0;inti<parmCnt;inti++)
        {
         if (!getValue("active_code").equals(getValue("parm.active_code",inti))) continue;

         if (getValue("proc_flag").equals("N"))
         if (getValue("parm.sms_nopurc_cond",inti).equals("Y"))
            {
             if (!comm.nextNDate(getValue("issue_date"), getValueInt("parm.sms_nopurc_days",inti)).
                 equals(hBusiBusinessDate)) continue;

             if (getValue("sms_nopurc_flag").equals("Y")) continue;

             setValue("purc.card_no"     , getValue("card_no"));
             setValue("purc.active_code" , getValue("active_code"));
             int cnt1 = getLoadData("purc.card_no,purc.active_code");
             if (cnt1==0) 
                {
                 setValue("sms_nopurc_date" , "");
                 setValue("sms_nopurc_flag" , "N");
                 setValue("nopurc_msg_id"   , "");

                 if (getValue("parm.nopurc_msg_id_g",inti).length()!=0)
                 if (selectMktBnData(getValue("group_code"),"1","A",3)==0)
                    {
                     procSendSms(getValue("parm.nopurc_msg_id_g",inti));
                     setValue("sms_nopurc_date" , hBusiBusinessDate);
                     setValue("sms_nopurc_flag" , "Y");
                     setValue("nopurc_msg_id"   , getValue("parm.nopurc_msg_id_g",inti));
                     if (!getValue("cellphone_check_flag").equals("Y"))
                        setValue("sms_nopurc_flag" , "F");

                    }
                 if (getValue("parm.nopurc_msg_id_c",inti).length()!=0)
                 if (selectMktBnData(getValue("card_note"),getValue("card_type"),
                                        "1","B",3)==0)
                    {
                     procSendSms(getValue("parm.nopurc_msg_id_c",inti));
                     setValue("sms_nopurc_date" , hBusiBusinessDate);
                     setValue("sms_nopurc_flag" , "Y");
                     setValue("nopurc_msg_id"   , getValue("parm.nopurc_msg_id_c",inti));
                     if (!getValue("cellphone_check_flag").equals("Y"))
                        setValue("sms_nopurc_flag" , "F");
                    }
                }
             else
                {
                 setValue("sms_nopurc_date" , "");
                 setValue("sms_nopurc_flag" , "A");
                 setValue("nopurc_msg_id"   , "");
                }
            }
         else
            {
             setValue("sms_nopurc_date" , "");
             setValue("sms_nopurc_flag" , "X");
             setValue("nopurc_msg_id"   , "");
            }
             
         if (getValue("proc_flag").equals("N"))
         if (getValue("parm.sms_half_cond",inti).equals("Y"))
            {
             if (!comm.nextNDate(getValue("issue_date"), getValueInt("parm.sms_half_days",inti)).
                 equals(hBusiBusinessDate)) continue;

             if (getValue("sms_half_flag").equals("Y")) continue;

             setValue("purc.card_no"     , getValue("card_no"));
             setValue("purc.active_code" , getValue("active_code"));
             int cnt1 = getLoadData("purc.card_no,purc.active_code");
             if (cnt1!=0) 
                {
                 setValue("sms_half_date" , "");
                 setValue("sms_half_flag" , "N");
                 setValue("half_msg_id"   , "");

                 if (getValue("parm.half_msg_id_g",inti).length()!=0)
                 {
                 if (selectMktBnData(getValue("group_code"),"1","C",3)==0)
                 {
                 if (checkHalfCond(inti)==0)
                    {
                     procSendSms(getValue("parm.half_msg_id_g",inti));
                     setValue("sms_half_date" , hBusiBusinessDate);
                     setValue("sms_half_flag" , "Y");
                     setValue("half_msg_id"   , getValue("parm.half_msg_id_g",inti));
                     if (!getValue("cellphone_check_flag").equals("Y"))
                        setValue("sms_half_flag" , "F");
                    }
                 }
                 }
                 if (getValue("parm.half_msg_id_c",inti).length()!=0)
                 if (selectMktBnData(getValue("card_note"),getValue("card_type"),
                                        "1","D",3)==0)
                 if (checkHalfCond(inti)==0)
                    {
                     procSendSms(getValue("parm.half_msg_id_c",inti));
                     setValue("sms_half_date" , hBusiBusinessDate);
                     setValue("sms_half_flag" , "Y");
                     setValue("half_msg_id"   , getValue("parm.half_msg_id_c",inti));
                     if (!getValue("cellphone_check_flag").equals("Y"))
                        setValue("sms_half_flag" , "F");
                    }
                }
             else
                {
                 setValue("sms_half_date" , "");
                 setValue("sms_half_flag" , "B");
                 setValue("half_msg_id"   , "");
                }
            }
         else
            {
             setValue("sms_half_date" , "");
             setValue("sms_half_flag" , "X");
             setValue("half_msg_id"   , "");
            }
             
//showLogMessage("I","","STEP 1 : ["+ getValue("proc_flag") +"]");
//showLogMessage("I","","STEP 2 : ["+ getValue("parm.sms_send_cond",inti) +"]");
//showLogMessage("I","","STEP 3 : ["+ comm.nextNDate(getValue("issue_date"), getValueInt("parm.sms_send_days",inti)) +"]");
         if (getValue("proc_flag").equals("Y"))
         if (getValue("parm.sms_send_cond",inti).equals("Y"))
            {
             if (!comm.nextNDate(getValue("feedback_date"), getValueInt("parm.sms_send_days",inti)).
                 equals(hBusiBusinessDate)) continue;

             if (getValue("sms_send_flag").equals("Y")) continue;

             setValue("sms_send_date" , "");
             setValue("sms_send_flag" , "N");
             setValue("send_msg_id"   , "");

             procSendSms(getValue("parm.send_msg_id",inti));
             setValue("sms_send_date" , hBusiBusinessDate);
             setValue("sms_send_flag" , "Y");
             setValue("send_msg_id"   , getValue("parm.send_msg_id",inti));
             if (!getValue("cellphone_check_flag").equals("Y"))
                setValue("sms_send_flag" , "F");
            } 

         updateMktFstpCarddtl();
        }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
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
            + "and (sms_nopurc_cond = 'Y'  "
            + " or  sms_half_cond   = 'Y'  "
            + " or  sms_send_cond   = 'Y')  "
            ;

  setString(1 , hBusiBusinessDate);

  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("parm.sms_nopurc_cond",inti).equals("Y"))
          {
           if (getValue("parm.nopurc_msg_id_g",inti).length()!=0)
              if (selectSmsMsgId(getValue("parm.nopurc_msg_id_g",inti))!=0)
                 showLogMessage("I","", "SMS_MSG_ID pmktm6240_"
                                      + getValue("parm.nopurc_msg_id_g",inti)
                                      + " no define error!" );
          }
     }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");

  return(0);
 }
// ************************************************************************
 int  selectSmsMsgId(String smsNo) throws Exception
 {
  extendField = "smsg.";
  selectSQL = "msg_id,"
            + "msg_pgm,"
            + "msg_dept,"
            + "msg_userid";
  daoTable  = "sms_msg_id";
  whereStr  = "WHERE msg_pgm       = ? "
            + "AND   msg_send_flag ='Y' ";

  setString(1 , "mktm6240_"+smsNo);

  selectTable();

    if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  insertSmsMsgDtl() throws Exception
 {
  extendField = "sdtl.";
  setValue("sdtl.msg_seqno"            , comr.getSeqno("ECS_MODSEQ"));
  setValue("sdtl.msg_pgm"              , javaProgram);
  setValue("sdtl.msg_dept"             , getValue("smsg.msg_dept"));
  setValue("sdtl.msg_userid"           , getValue("smsg.msg_userid"));
  setValue("sdtl.msg_id"               , getValue("smsg.msg_id"));
  setValue("sdtl.cellar_phone"         , getValue("idno.cellar_phone"));
  setValue("sdtl.cellphone_check_flag" , getValue("cellphone_check_flag"));
  setValue("sdtl.chi_name"             , getValue("idno.chi_name"));
  setValue("sdtl.msg_desc"             , getValue("msg_desc"));
  setValue("sdtl.p_seqno"              , getValue("p_seqno"));
  setValue("sdtl.acct_type"            , getValue("acct_type"));
  setValue("sdtl.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("sdtl.add_mode"             , "B");
  setValue("sdtl.crt_date"             , sysDate);
  setValue("sdtl.crt_user"             , "AIX");
  setValue("sdtl.apr_date"             , sysDate);
  setValue("sdtl.apr_user"             , "AIX");
  setValue("sdtl.apr_flag"             , "Y");
  setValue("sdtl.mod_user"             , "AIX");
  setValue("sdtl.mod_time"             , sysDate+sysTime);
  setValue("sdtl.mod_pgm"              , javaProgram);

  daoTable = "sms_msg_dtl";

  insertTable();

  if ( dupRecord.equals("Y") )
      showLogMessage("I","","insert_sms_msg_dtl  error[dupRecord]");

  return;
 }
// ************************************************************************
 void selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  daoTable  = "CRD_IDNO";
  whereStr  = "WHERE ID_P_SEQNO = ?";

  setString(1,getValue("id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select crd_idno error!" );
      showLogMessage("I","","major_id_p_seqno["+getValue("id_p_seqno")+"]" );
      exitProgram(1);
     }
 }
// ************************************************************************
 int procSendSms(String smsNo) throws Exception
 {
  if (selectSmsMsgId(smsNo)!=0)
     {
      showLogMessage("I","","SMS_MSG_ID mktm6240_"+smsNo+" no define error!" );
      return(1);
     }
  selectCrdIdno();
  setValue("cellphone_check_flag", "Y");

  if (getValue("idno.cellar_phone").length()!=10)
     setValue("cellphone_check_flag", "N");

  if (!getValue("idno.cellar_phone").matches("[0-9]+"))
     setValue("cellphone_check_flag", "N");

  String tmpstr = getValue("smsg.msg_userid") + ","
                + getValue("smsg.msg_id") + ","
                + getValue("idno.cellar_phone") + ","
                + getValue("idno.chi_name");

   setValue("msg_desc",tmpstr);

  insertSmsMsgDtl();
  return(0);
 }
// ************************************************************************
 void loadMktFstpPurcdtl() throws Exception
 {
  extendField = "purc.";
  selectSQL = "active_code,"
            + "ori_major_card_no as card_no,"
            + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt,"
            + "sum(dest_amt) as dest_amt";
  daoTable  = "mkt_fstp_purcdtl";
  whereStr  = "where (active_code,p_seqno) in ( "
            + "      select active_code,p_seqno "
            + "      from   mkt_fstp_carddtl "
            + "      where  proc_flag  = 'N' "
            + "      AND   error_code  = '00' ) "
            + "group by ori_major_card_no,active_code "
            ;

  int  n = loadTable();
  setLoadData("purc.card_no,purc.active_code");

  showLogMessage("I","","Load mkt_fstp_purcdtl Count: ["+n+"]");
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
  whereStr  = "WHERE TABLE_NAME = 'MKT_FSTP_PARM' "
            + "and   data_type in ('A','B','C','D') "
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
int updateMktFstpCarddtl() throws Exception
 {
  dateTime();
  updateSQL = "sms_nopurc_date      = ?,"
            + "sms_nopurc_flag      = ?, "
            + "nopurc_msg_id        = ?, "
            + "sms_half_date        = ?, "
            + "sms_half_flag        = ?, "
            + "half_msg_id          = ?, "
            + "sms_send_date        = ?, "
            + "sms_send_flag        = ?, "
            + "send_msg_id          = ?, "
            + "mod_time             = sysdate,"
            + "mod_user             = ?, "
            + "mod_pgm              = ? ";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "WHERE  card_no       = ? "
            + "AND    active_code   = ? ";

  setString(1 , getValue("sms_nopurc_date"));
  setString(2 , getValue("sms_nopurc_flag"));
  setString(3 , getValue("nopurc_msg_id"));
  setString(4 , getValue("sms_half_date"));
  setString(5 , getValue("sms_half_flag"));
  setString(6 , getValue("half_msg_id"));
  setString(7 , getValue("sms_send_date"));
  setString(8 , getValue("sms_send_flag"));
  setString(9 , getValue("send_msg_id"));
  setString(10, javaProgram);
  setString(11, javaProgram);
  setString(12, getValue("card_no"));
  setString(13, getValue("active_code"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int checkHalfCond(int inti) throws Exception
 {
  int cnt1=0,amt1=0;
  if (getValue("parm.half_cnt_cond",inti).equals("Y"))
     if (getValueInt("parm.half_cnt",inti)<=getValueInt("purc.dest_cnt")) cnt1=1;
  if (getValue("parm.half_amt_cond",inti).equals("Y"))
     if (getValueDouble("parm.half_amt",inti)<=getValueDouble("purc.dest_amt")) amt1=1;

  if ((getValue("parm.half_cnt_cond",inti).equals("Y"))&&
      (getValue("parm.half_amt_cond",inti).equals("Y")))
     {
      if (getValue("parm.half_andor_cond",inti).equals("Y"))
         {
          if (cnt1+amt1!=2) return(1);
         }
      else
         {
          if (cnt1+amt1==0) return(1);
         }
     }
  else
     {
      if (cnt1+amt1==0) return(1);
     }

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
