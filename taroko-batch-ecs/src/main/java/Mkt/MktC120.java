/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/09/18  V1.00.19  Allen Ho   New                                        *
* 110/09/22  V1.01.01  Allen Ho   channel_bill id_no maybe =''               *
* 111/01/11  V1.02.04  Allen Ho   CR-1339                                    *
* 111/11/11  V1.02.05  Yang Bo    sync code from mega                        *
******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktC120 extends AccessDAO
{
 private final String PROGNAME = "通路活動-登錄資格判斷處理程式 111/11/11  V1.02.05";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String businessDate = "";
 String activeCode = "";
 String idNo = "";
 String DEBUG         = "";

 String[][] recordGroupNo = new String[20][20];
 String[][] recordDateSel = new String[20][20];
 String[][] activeSeq = new String[20][20];

 String minDate ="", maxDate ="";
 int  parmCnt  = 0, recordFlag =0;;
 int[]  pseqCnt = new int[100];
 int  totalCnt=0,insertCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC120 proc = new MktC120();
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
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 4)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [id_no]");
       showLogMessage("I","","PARM 4 : [COMMIT");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      { activeCode = args[1]; }
   if ( args.length >= 3 )
      { idNo = args[2]; }
   if ( args.length == 4 )
      { DEBUG         = args[3]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktChannelParm();
   if (recordFlag ==0)
      {
       showLogMessage("I","","無登錄資料需判斷");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktChannelBill();
   loadMktBnData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","還原資料");
   updateMktChannelBill0();;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","Record date ["+ minDate +"]-["+ maxDate +"]");
   showLogMessage("I","","載入網路登錄(web_active)資料");
   selectWebActive();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入語音(mkt_voice)登錄資料");
   selectMktVoice();;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入語音(mkt_vocdata)登錄資料");
   selectMktVocdata();;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","未登錄更新註記(A1)");
   updateMktChannelBill();;
   showLogMessage("I","","=========================================");

   if ((idNo.length()==0)||(DEBUG.equals("COMMIT"))) finalProcess();
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
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************
 int selectMktChannelParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND     ((stop_flag = 'N') "
            + " or      (stop_flag = 'Y' "
            + "  and     stop_date > ? )) "
            + "AND     cal_def_date      = ?  "
            + "AND     record_cond       = 'Y'  "
            ;

  setString(1 , businessDate); 
  setString(2 , businessDate); 

  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   active_code = ?  ";
      setString(3 , activeCode);
     }
  parmCnt = selectTable();

  minDate = "99999999";
  maxDate = "00000000";
  for (int inti=0;inti<parmCnt;inti++)
    {
     showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)+"] 名稱:["+getValue("parm.active_name",inti)+"]");


     recordFlag = 1;
     deleteMktChannelRecord(inti);

     selectMktChanrecParm(inti);
    }

  return(0);
 }
// ************************************************************************
 int selectMktChanrecParm(int inti) throws Exception
 {
  extendField = "pseq.";
  daoTable  = "mkt_chanrec_parm";
  whereStr  = "WHERE active_code     = ? "
            ;

  setString(1 , getValue("parm.active_code",inti));

  int recCnt = selectTable();

  showLogMessage("I","","符合之登錄seq :["+getValue("parm.active_code",inti)+"]["+recCnt+"]");

  pseqCnt[inti] = recCnt;
  for (int intk=0;intk<recCnt;intk++)
     {
      showLogMessage("I","","START 日期區間:["+ minDate +"]-["+ maxDate + "]");
      if (getValue("pseq.record_group_no",intk).length()!=0) 
         {
          if (selectWebRecordGroup(intk)!=0)
             {
              showLogMessage("I","","登錄群組:["+getValue("pseq.record_group_no",intk)+"] 參數不存在");
              continue;
             }
          else
             {
              if (selectWebActivityParm(intk)==0)
                 {
//                showLogMessage("I","","登錄群組:["+getValue("pseq.record_group_no",intk)+"]["
//                                     + getValue("wapm.min_date")+"]["
//                                     + getValue("wapm.max_date")+"]");

                  if (getValue("wapm.min_date").compareTo(minDate)<0)
                     minDate = getValue("wapm.min_date");
                  if (getValue("wapm.max_date").compareTo(maxDate)>0)
                     maxDate = getValue("wapm.max_date");
                 }
             }
         }

      showLogMessage("I","","符合之燈覆群組:["+getValue("pseq.record_group_no",intk)+"]");
      showLogMessage("I","","      日期區間:["+ minDate +"]-["+ maxDate + "]");
      activeSeq[inti][intk]      = getValue("pseq.active_seq",intk);
      recordGroupNo[inti][intk] = getValue("pseq.record_group_no",intk);
      recordDateSel[inti][intk] = getValue("pseq.record_date_sel",intk);
     }

  return(0);
 }
// ************************************************************************
 void selectWebActive() throws Exception
 {
  selectSQL = "active_no as record_no,"
            + "rec_date as record_date," 
            + "rec_time as record_time," 
            + "card_no_8," 
            + "major_id_p_seqno," 
            + "vd_flag,"
            + "data_key,"
            + "id_no"; 
  daoTable  = "web_active";
  whereStr  = "where rec_date between ? and ? "
            + "and   active_no != ''  "
            + "and   id_no     != ''  "
            ;

  setString(1, minDate);
  setString(2, maxDate);

  if (idNo.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   id_no = ?  ";
      setString(3 , idNo);
     }

  openCursor();

  totalCnt = 0;
  int outCnt = 0;
  int cnt3   = 0;
  insertCnt=0;
  setValue("data_from" , "1");
  while( fetchTable() ) 
   {
    totalCnt++;
    if (idNo.length()!=0)
       { 
        showLogMessage("I","","STEP 1 record_date :["+getValue("record_date")+"]");
        showLogMessage("I","","       vd_flag     :["+getValue("vd_flag")+"]");
        showLogMessage("I","","       id_no       :["+getValue("id_no")+"]");
        showLogMessage("I","","       id_p_seqno  :["+getValue("major_id_p_seqno")+"]");
        showLogMessage("I","","       record_no   :["+getValue("record_no")+"]");
       }
    setValue("record_time" , "240000");

    if (getValue("data_key").equals("A"))
       {
        cnt3=1;
        if (getValue("major_id_p_seqno").length()==0)
           {
            if (!getValue("vd_flag").equals("Y"))
               {
                selectCrdCard();
               }
            else
               {
                selectDbcCard();
               }
           }
       }
    else
       {
        cnt3 = selectCrdCardNa();
        if (cnt3==0) continue;
       }
    if (idNo.length()!=0)
        showLogMessage("I","","STEP 2 id_no       :["+getValue("id_no")+"]");

    for (int intm=0;intm<cnt3;intm++)
      {
       if (getValue("major_id_p_seqno").length()==0)
         setValue("major_id_p_seqno" , getValue("card.major_id_p_seqno",intm));
       if (idNo.length()!=0)
          showLogMessage("I","","STEP 2.1 major_id_p_seqno:["+getValue("major_id_p_seqno")+"]");
       if (!getValue("vd_flag").equals("Y"))
          {
           selectCrdIdno();
          }
       else
          {
           selectDbcIdno();
          }
       setValue("id_no"   , getValue("idno.id_no"));

       if (idNo.length()!=0)
          showLogMessage("I","","STEP 2.5 id_p_seqno:["+getValue("idno.id_p_seqno")+"]");

       setValue("bill.id_p_seqno" , getValue("idno.id_p_seqno"));
       int cnt1 = getLoadData("bill.id_p_seqno");
       if (cnt1==0)
          {
           outCnt++;
           continue;
          }

       if (idNo.length()!=0)
           showLogMessage("I","","STEP 3 id_no       :["+getValue("id_no")+"]");

       for (int inti=0;inti<parmCnt;inti++)
         {
          if (getValue("record_date").compareTo(businessDate)>0) continue;

          if (idNo.length()!=0)
              showLogMessage("I","","STEP 4 record_date :["+ getValue("record_date") +"]");

          for (int intk=0;intk<pseqCnt[inti];intk++)
            {
             if (recordGroupNo[inti][intk].length()==0) continue;

             if (idNo.length()!=0)
                showLogMessage("I","","STEP 5 record_group_no :["+ recordGroupNo[inti][intk] +"]");

             setValue("mbda.data_key"  , recordGroupNo[inti][intk]);
             setValue("mbda.data_type" , "2");
             setValue("mbda.data_code" , getValue("record_no"));
             int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");

             if (cnt2==0) continue;

             if (idNo.length()!=0)
                showLogMessage("I","","STEP 6 record_group_no :["+ recordGroupNo[inti][intk] +"]");

             if (updateMktChannelRecord(inti,intk)!=0)
                {
                 if (idNo.length()!=0)
                    showLogMessage("I","","STEP 7 record_group_no :["+ recordGroupNo[inti][intk] +"]");
                 insertMktChannelRecord(inti,intk);
                }
            }
         }
      }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","不符合之ID筆數 :["+ outCnt +"] 新增筆數["+ insertCnt +"]");
 }
// ************************************************************************
 void selectMktVocdata() throws Exception
 {
  selectSQL  = "docu_code as record_no,"
             + "crt_date as record_date,"
             + "crt_time as record_time,"
             + "p_seqno";
  daoTable  = "mkt_vocdata";
  whereStr  = "where crt_date between ? and ? "
            + "and   docu_code!='' "
            + "and   p_seqno  !='' "
            ;

  setString(1, minDate);
  setString(2, maxDate);

  openCursor();

  totalCnt = 0;
  int outCnt = 0;
  insertCnt=0;
  setValue("data_from" , "2");
  while( fetchTable() ) 
   {
    totalCnt++;

    setValue("bill.p_seqno" , getValue("p_seqno"));
    int cnt1 = getLoadData("bill.p_seqno");
    if (cnt1==0) 
       {
        outCnt++;
        continue;
       }

    for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("record_date").compareTo(businessDate)>0) continue;

       for (int intk=0;intk<pseqCnt[inti];intk++)
         {
          if (recordGroupNo[inti][intk].length()==0) continue;

          setValue("mbda.data_key"  , recordGroupNo[inti][intk]);
          setValue("mbda.data_type" , "1");
          setValue("mbda.data_code" , getValue("record_no"));
          int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");
          if (cnt2==0) continue;

          if (updateMktChannelRecord(inti,intk)!=0)
             insertMktChannelRecord(inti,intk);
         }
      }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","不符合之ID筆數 :["+ outCnt +"] 新增筆數["+ insertCnt +"]");
 }
// ************************************************************************
 void selectMktVoice() throws Exception
 {
  selectSQL  = "document as record_no,"
             + "input_date as record_date,"
             + "input_time as record_time,"
             + "p_seqno";
  daoTable  = "mkt_voice";
  whereStr  = "where function_code != '8050' "
            + "and   input_date between ? and ? "
            + "and   document != '' "
            + "and   p_seqno  !='' "
            ;

  setString(1, minDate);
  setString(2, maxDate);

  openCursor();

  totalCnt = 0;
  int outCnt = 0;
  insertCnt=0;
  setValue("data_from" , "3");
  while( fetchTable() ) 
   {
    totalCnt++;

    setValue("bill.p_seqno" , getValue("p_seqno"));
    int cnt1 = getLoadData("bill.p_seqno");
    if (cnt1==0) 
       {
        outCnt++;
        continue;
       }

    for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("record_date").compareTo(businessDate)>0) continue;

       for (int intk=0;intk<pseqCnt[inti];intk++)
         {
          if (recordGroupNo[inti][intk].length()==0) continue;

          setValue("mbda.data_key"  , recordGroupNo[inti][intk]);
          setValue("mbda.data_type" , "1");
          setValue("mbda.data_code" , getValue("record_no"));
          int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");
          if (cnt2==0) continue;

//        if (getValue("record_date").compareTo(getValue("mbda.data_code2"))<0) continue;
//        if (getValue("record_date").compareTo(getValue("mbda.data_code3"))>0) continue;

          if (updateMktChannelRecord(inti,intk)!=0)
             insertMktChannelRecord(inti,intk);
         }
      }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","不符合之ID筆數 :["+ outCnt +"] 新增筆數["+ insertCnt +"]");
 }
// ************************************************************************
 int insertMktChannelRecord(int inti, int intk) throws Exception
 {
  dateTime();
  extendField = "reco.";

  setValue("reco.active_code"        , getValue("parm.active_code",inti));
  setValue("reco.id_p_seqno"         , getValue("idno.id_p_seqno"));
  setValue("reco.record_group_no"    , recordGroupNo[inti][intk]);
  setValue("reco.record_no"          , getValue("record_no"));
  setValue("reco.record_date"        , getValue("record_date"));
  setValue("reco.record_time"        , getValue("record_time"));
  setValue("reco.data_from"          , getValue("data_from"));
  setValue("reco.mod_time"           , sysDate+sysTime);
  setValue("reco.mod_pgm"            , javaProgram);

  daoTable  = "mkt_channel_record";

  insertTable();

  insertCnt++;
  return(0);
 }
// ************************************************************************
 void loadMktChannelBill() throws Exception
 {
   extendField = "bill.";
  selectSQL = "p_seqno,"
            + "id_p_seqno";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from   mkt_channel_parm "
            + "      where  cal_def_date = ? "
            ;

  setString(1 , businessDate);

  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and     active_code = ?  ";
      setString(2 , activeCode);
     }

  whereStr  = whereStr  
            + "      and    record_cond  = 'Y' ) "
            + "group by p_seqno,id_p_seqno ";

  int  n = loadTable();
  setLoadData("bill.p_seqno");
  setLoadData("bill.id_p_seqno");

  showLogMessage("I","","Load mkt_channel_bill Count: ["+n+"]");
 }
// ************************************************************************
 int loadMktBnData() throws Exception
 {
  extendField = "mbda.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            +"data_code2,"
            +"data_code3";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'WEB_RECORD_GROUP' "
            ;

  int  n = loadTable();
  setLoadData("mbda.data_key,mbda.data_type,mbda.data_code");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
  return(n);
 }
// ************************************************************************
 int deleteMktChannelRecord(int inti) throws Exception
 {
  daoTable  = "mkt_channel_record";
  whereStr  = "where  active_code   = ? ";

  setString(1 , getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_channel_record 筆數 :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 void updateMktChannelBill0() throws Exception
 {
  daoTable  = "mkt_channel_bill a";
  updateSQL = "error_code            = '00'";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from mkt_channel_parm "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND     ((stop_flag = 'N') "
            + "       or      (stop_flag = 'Y' "
            + "        and     stop_date > ? )) "
            + "      AND     cal_def_date      = ?  "
            + "      AND     record_cond       = 'Y')  "
            + "and   error_code = 'A1' "
            ;

  setString(1 , businessDate); 
  setString(2 , businessDate); 

  int n = updateTable();
  showLogMessage("I","","還原 mkt_channel_bill 筆數 :["+ n +"]");
  return;
 }
// ************************************************************************
 void updateMktChannelBill() throws Exception
 {
  daoTable  = "mkt_channel_bill a";
  updateSQL = "error_code            = 'A1'";
  whereStr  = "where id_p_seqno not in ("
            + "      select id_p_seqno "
            + "      from mkt_channel_record "
            + "      where id_p_seqno  = a.id_p_seqno "
            + "      and   active_code = a.active_code) "
            + "and   active_code in ( "
            + "      select active_code "
            + "      from mkt_channel_parm "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND     ((stop_flag = 'N') "
            + "       or      (stop_flag = 'Y' "
            + "        and     stop_date > ? )) "
            + "      AND     cal_def_date      = ?  "
            + "      AND     record_cond       = 'Y')  "
            ;

  setString(1 , businessDate); 
  setString(2 , businessDate); 

  updateTable();
  return;
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_no,"
            + "id_p_seqno";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno = ? "
            ;

  setString(1 , getValue("major_id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectDbcIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_no,"
            + "id_p_seqno";
  daoTable  = "dbc_idno";
  whereStr  = "where id_p_seqno = ? "
            ;

  setString(1 , getValue("major_id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "distinct major_id_p_seqno";
  daoTable  = "crd_idno a,crd_card b";
  whereStr  = "where a.id_p_seqno = b.id_p_seqno "
            + "and   a.id_no      = ? "
            + "and   substr(b.card_no,length(b.card_no)-7,8) = ? "
            ;

  setString(1 , getValue("id_no"));
  setString(2 , getValue("card_no_8"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectCrdCardNa() throws Exception
 {
  extendField = "card.";
  selectSQL = "distinct major_id_p_seqno";
  daoTable  = "crd_idno a,crd_card b";
  whereStr  = "where a.id_p_seqno = b.id_p_seqno "
            + "and   a.id_no      = ? "
            + "and   b.current_code = '0' "
            ;

  setString(1 , getValue("id_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(0);
  return(recCnt);
 }
// ************************************************************************
 int selectDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "distinct major_id_p_seqno";
  daoTable  = "dbc_idno a,dbc_card b";
  whereStr  = "where a.id_p_seqno = b.id_p_seqno "
            + "and   a.id_no      = ? "
            + "and   substr(b.card_no,length(b.card_no)-7,8) = ? "
            ;

  setString(1 , getValue("id_no"));
  setString(2 , getValue("card_no_8"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectWebRecordGroup(int intk) throws Exception
 {
  extendField = "regp.";
  selectSQL = "active_date_s,"
            + "active_date_e"; 
  daoTable  = "web_record_group";
  whereStr  = "where record_group_no = ? "
            ;

  setString(1 , getValue("pseq.record_group_no",intk));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int updateMktChannelRecord(int inti, int intk) throws Exception
 {
  daoTable  = "mkt_channel_record";
  updateSQL = "record_date   = ? ";
  whereStr  = "where id_p_seqno      = ? "
            + "and   active_code     = ? "
            + "and   record_group_no = ? "
            + "and   record_no       = ? "
            ;

  setString(1 , getValue("record_date"));
  setString(2 , getValue("bill.id_p_seqno"));
  setString(3 , getValue("parm.active_code",inti));
  setString(4 , recordGroupNo[inti][intk]);
  setString(5 , getValue("record_no"));
   
  if (!recordDateSel[inti][intk].equals("0"))
     {
      if (recordDateSel[inti][intk].equals("1"))
         whereStr  = whereStr  
                   + "and   record_date     < ? ";
      else
         whereStr  = whereStr  
                   + "and   record_date     > ? ";
      setString(6 , getValue("record_date"));
     }
  updateTable();

  if ( notFound.equals("Y") ) return(1);
   
  return(0);
 }
// ************************************************************************
 int selectWebActivityParm(int intk) throws Exception
 {
  extendField = "wapm.";
  selectSQL = "min(c.web_date_s) as min_date,"
            + "max(c.web_date_e) as max_date," 
            + "count(*) as rec_cnt"; 
  daoTable  = "mkt_chanrec_parm a,mkt_bn_data b,web_activity_parm c";
  whereStr  = "where b.table_name = 'WEB_RECORD_GROUP' "
            + "and   b.data_type = '2' "
            + "and   a.record_group_no =b.data_key "
            + "and   b.data_code = c.web_record_no "
            + "and   a.record_group_no = ? "

            ;

  setString(1 , getValue("pseq.record_group_no",intk));

  int recordCnt = selectTable();

  if (getValueInt("wapm.rec_cnt") ==0 ) return(1);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

