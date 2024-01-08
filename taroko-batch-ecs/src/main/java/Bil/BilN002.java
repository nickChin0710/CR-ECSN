/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  106/06/01 V1.00.00   Edson      program initial                            *
*  107/08/28 V1.09.01   陳佳琪     RECS-s1070821-072 開放紅利積點抵用停車費            *
*  107/09/06 v1.10.01   林志鴻     RECS-s1070821-072 合併 bil_n007 bil_n010      *
*  107/10/03 V2.00.00   David      RECS-s1070821-072(JAVA)                    *
*  109/07/03 V2.00.01   Pino       產生市區免費停車名單                             *
*  109/12/03 V1.00.01   shiyuqi    updated for project coding standard        * 
*  110/07/09 V1.00.02   suzuwei    消費計算方式:卡號改為正附卡合併,及相關邏輯              * 
*  112/04/21 V1.01.01   Lai        調整程式每日檢核名單卡況                           *
*  112/07/18 V1.01.02   Lai        modify ftp                                 *
*  112/08/22 V1.01.03   Lai        modify card_no 檔案內卡號重覆                   *
*  112/08/31 V1.01.04   Lai        modify show                                *
*  112/09/11 V1.01.05   Lai        modify delete by acct_month                *
*  112/11/10 V1.01.06   Lai        modify parm                                *
*  112/12/08 V1.01.07   Kirin      modify data_key = 'MKTR00001'  移除 comc.errExit--> return code非0            
*  112/12/28 V1.01.08   Ryan       hBusinessPrevMonth, 拿掉-1                         
*  112/01/05 V1.01.09   Ryan       modify chkBilDodoParm *                     
*  112/01/05 V1.01.10   Ryan       modify join11 , join21 , join31                                          *
******************************************************************************/
package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.io.UnsupportedEncodingException;

import com.*;

/*產生市區免費停車名單*/
public class BilN002 extends AccessDAO {
  private String progname = "產生市區免費停車名單   112/01/05 V1.01.09";
  CommFunction   comm  = new CommFunction();
  CommCrd        comc  = new CommCrd();
  CommCrdRoutine comcr = null;
  CommBonus       comb = null;
  CommDate    commDate = new CommDate();
  CommString    comms = new CommString();

  String  endflag = "";
  String  hTempUser = "";
  int DEBUG   = 0;
  int DEBUG_F = 0;
  int DEBUG_DATA = 0;

  private String rptName1   = "TWPARKM9";
  private String hFileName1 ="";
  private int    rptSeq1    = 0;
  private List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
  
  private String tmpStr    = "";
  String buf       = "";
  String szTmp     = "";
  String stderr    = "";
  long   hModSeqno = 0;
  String hCallBatchSeqno = "";

  String hBusinessDate      = "";
  String hBusinessPrevMonth = "";
  String hSystemDate        = "";
  String hCardGroupCode     = "";
  String hCardCardType      = "";
  String hCardCardNo        = "";
  String hCardStmtCycle     = "";
  String hCardIssueDate     = "";
  String hCardNextIssue     = "";
  String hCardMajorCardNo   = "";
  String hCardOldCardNo     = "";
  String hCardAcnoPSeqno    = "";
  String hCardMajorIdPSeqno = "";
  String hCardCurrentCode   = "";
  String hIdnoChiName       = "";
  String hIdnoIdNo          = "";
  String hIdnoMajorId       = "";
  String hIssuNextCloseDate = "";
  String hNewDate7          = "";
  String hIdNo              = "";

  List<String>  hDopaActionCd       = new ArrayList<String>();
  List<String>  hDopaDocumentDesc   = new ArrayList<String>();
  List<Integer> hDopaCarHours       = new ArrayList<Integer>();
  List<String>  hDopaIvrFlag        = new ArrayList<String>();
  List<String>  hDopaDocument       = new ArrayList<String>();
  List<Double>  hDopaConsumeAmtFm1  = new ArrayList<Double>();
  List<Double>  hDopaConsumeAmtTo1  = new ArrayList<Double>();
  List<Integer> hDopaConsumeCnt1    = new ArrayList<Integer>();
  List<String>  hDopaConsumePeriod1 = new ArrayList<String>();
  List<String>  hDopaDateFm1        = new ArrayList<String>();
  List<String>  hDopaDateTo1        = new ArrayList<String>();
  List<Integer> hDopaLimitDays1     = new ArrayList<Integer>();
  List<String>  hDopaConsumeMethod1 = new ArrayList<String>();
  List<Double>  hDopaConsumeAmtFm2  = new ArrayList<Double>();
  List<Double>  hDopaConsumeAmtTo2  = new ArrayList<Double>();
  List<Integer> hDopaConsumeCnt2    = new ArrayList<Integer>();
  List<String>  hDopaConsumePeriod2 = new ArrayList<String>();
  List<String>  hDopaDateFm2        = new ArrayList<String>();
  List<String>  hDopaDateTo2        = new ArrayList<String>();
  List<Integer> hDopaLimitDays2     = new ArrayList<Integer>();
  List<String>  hDopaConsumeMethod2 = new ArrayList<String>();
  List<String>  hDopaExtBatchNo     = new ArrayList<String>();
  List<Integer> hDopaTotalBonus     = new ArrayList<Integer>();
  List<String>  hDopaItemEnameBl1   = new ArrayList<String>();
  List<String>  hDopaItemEnameIt1   = new ArrayList<String>();
  List<String>  hDopaItemEnameCa1   = new ArrayList<String>();
  List<String>  hDopaItemEnameId1   = new ArrayList<String>();
  List<String>  hDopaItemEnameAo1   = new ArrayList<String>();
  List<String>  hDopaItemEnameOt1   = new ArrayList<String>();
  List<String>  hDopaMchtNo1        = new ArrayList<String>();
  List<String>  hDopaItemEnameBl2   = new ArrayList<String>();
  List<String>  hDopaItemEnameIt2   = new ArrayList<String>();
  List<String>  hDopaItemEnameCa2   = new ArrayList<String>();
  List<String>  hDopaItemEnameId2   = new ArrayList<String>();
  List<String>  hDopaItemEnameAo2   = new ArrayList<String>();
  List<String>  hDopaItemEnameOt2   = new ArrayList<String>();
  List<String>  hDopaMchtNo2        = new ArrayList<String>();
  String sqlSt = "";
  int hCnt = 0;
  double hAmt    = 0;
  double hMaxAmt = 0;
  String hMktInputDate7 = "";
  int tDocument = 0;
  int tempInt   = 0;
  List<String> hWdayStmtCycle     = new ArrayList<String>();
  List<String> hWdayNextCloseDate = new ArrayList<String>();
  List<String> hWdayLastAcctMonth = new ArrayList<String>();
  List<String> hWdayThisAcctMonth = new ArrayList<String>();
  String dateTo = "";
  String days = "";
  String tempX08 = "";
  String hOwsmWfValue3 = "";

  String hSmidMsgId = "";
  String hSmidMsgDept = "";
  String hSmidMsgSendFlag = "";
  String hSmidAcctTypeSel = "";
  String hSmidMsgUserid = "";
  double hTempModSeqno = 0;
  String hIdnoCellarPhone = "";
  String hTempCellphoneCheckFlag = "";
  String hSmsChiName    = "";
  String szTmpsms       = "";
  String hCardAcctType  = "";
  String hCardAcctKey   = "";
  String hCardIdPSeqno  = "";
  String hCardPSeqno    = "";
  String hDomsRowid     = "";
  String hBillAcctMonth = "";
  String hBillPurchaseDate = "";
  int hInt1 = 0;
  String hIdnoSex = "";
  String hDomsBegDate = "";
  String hDomsEndDate = "";
  String hDomsDodoMsg = "";
  String tempPeriod = "";
  String hBegDate = "";
  String hEndDate = "";
  String smsMsgFlag = "";
  String tHour = "";
  String hTempEndDate  = "";
  double hBpcdNetTtlBp = 0;
  String hCrd1CardNo   = "";
  String hNewCardNo    = "";

  String tempDateFm = "";
  String tempDateTo = "";
  String tempMethod = "";
  String tempItemEnameBl = "";
  String tempItemEnameIt = "";
  String tempItemEnameCa = "";
  String tempItemEnameId = "";
  String tempItemEnameAo = "";
  String tempItemEnameOt = "";
  String tempMchtNo   = "";
  String tempDataType = "";
  String hTempCardNo  = "";

  List<Integer> hCarHours = new ArrayList<Integer>();
  List<String>  hActionCd = new ArrayList<String>();
  String lastMonthF = "";
  String lastMonthT = "";
  String swNext = "";
  String sData = "";
  List<String> sDataParam = new ArrayList<>();
  int maxRows = 250;
  int rowsToFetch = 250;
  int stmtCycleCnt = 0;
  int prevCnt = 0;
  int totCnt  = 0;
  int totCnt0 = 0;
  int totCnt1 = 0;
  int dodoParmCnt = 0;
  int hTempCnt = 0;
  int rtn = 0;
  int dodoBnDataCnt = 0;
  double hTempAmt = 0;
  private int smsTag = 0;
  boolean parmFlag = false;
  
// ***********************************************************
public int mainProcess(String[] args) 
{
 try 
  {
   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);
   // =====================================
   if (args.length > 2) {
    comc.errExit("Usage : BilN002 [YYYYMM] [batch_seq]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
    comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
   comb = new CommBonus(getDBconnect(), getDBalias());

   comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

   String checkHome = comc.getECSHOME();
   if (comcr.hCallBatchSeqno.length() > 6) {
    if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
     comcr.hCallBatchSeqno = "no-call";
    }
   }

   comcr.hCallRProgramCode = javaProgram;
   hTempUser = "";
   if (comcr.hCallBatchSeqno.length() == 20) {
    comcr.callbatch(0, 0, 1);
    selectSQL = " user_id ";
    daoTable  = "ptr_callbatch";
    whereStr  = "WHERE batch_seqno   = ?  ";

    setString(1, comcr.hCallBatchSeqno);
    int recCnt = selectTable();
    hTempUser  = getValue("user_id");
   }
   if (hTempUser.length() == 0) {
    hTempUser = comc.commGetUserID();
   }
   if (args.length >  0) {
       hBusinessDate = "";
       if(args[0].length() == 8) {
    	  parmFlag = true;
          hBusinessDate    = args[0];
         }
   }
   showLogMessage("I","",String.format("參數輸入日期=[%s]\n",hBusinessDate));

   commonRtn();
   
   if (!hBusinessDate.substring(6).equals("06")) {                            
       showLogMessage("I", "", "本程式只在每月6日執行, 本日非執行日!! process end....");
       return 0;
    }                        
	showLogMessage("I","","處理營業日日期="+hBusinessDate);

   deleteBilDodoDtl();
   deleteBilDodoDtlTemp(); //V2.00.01新增

   /*-- Read 簡訊參數 --*/
   selectSmsMsgId();
   smsMsgFlag = "N";

   selectPtrWorkday();

   selectBilDodoParm();

   selectCrdCard();

   selectBilDodoDtlTemp();

   String filenames = "";
   hFileName1 = String.format("%s-%s.txt", rptName1 , hBusinessDate);
   filenames  = String.format("%s/media/bil/%s", comc.getECSHOME(),hFileName1);
   filenames  = Normalizer.normalize(filenames, java.text.Normalizer.Form.NFKD);
   comc.writeReport(filenames, lpar1);

   ftpRtn(hFileName1);

   // ==============================================
   // 固定要做的

   comcr.hCallErrorDesc = "程式執行結束，檔案筆數[" + totCnt + "] Int=["+totCnt0+"]";
   showLogMessage("I", "", comcr.hCallErrorDesc);
   if (comcr.hCallBatchSeqno.length() == 20)
       comcr.callbatch(1, 0, 1); // 1: 結束
   finalProcess();
   return 0;
  }  catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
  }
}
/***********************************************************************/
void commonRtn() throws Exception {
    sqlCmd = "select business_date,";
//    sqlCmd += "substr(to_char(add_months(to_date(business_date,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_business_prev_month ";
    sqlCmd += " left(business_date,6) h_business_prev_month ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hBusinessDate      = hBusinessDate.length() == 0 ? getValue("business_date")
                           : hBusinessDate;

    } else {
        comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
    }

    sqlCmd  = "select to_char(sysdate,'yyyymmdd') h_system_date ";
//    sqlCmd += ", substr(to_char(add_months(to_date(?,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_prev_month ";
    sqlCmd += ", left(?,6) h_prev_month ";
    sqlCmd += " from dual ";
    setString(1, hBusinessDate);
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hSystemDate        = getValue("h_system_date");
      hBusinessPrevMonth = getValue("h_prev_month");
    }

   showLogMessage("I","",String.format("處理營業日日期=[%s]Acct_mon=[%s],Use_month=[%s]\n"
                     ,hBusinessDate, hBusinessPrevMonth, hBusinessDate.substring(0,6)));
//   int h_ten_day  = Integer.parseInt(comc.getSubString(hBusinessDate, 6, 8));
//   if(h_ten_day != 6)
//     {
//      comc.errExit("今天非6號，不執行。 今天是=["+h_ten_day+"]", "");
//     }

    hModSeqno = comcr.getModSeq();
}
/***************************************************************************/
    private void deleteBilDodoDtl() throws Exception {
      showLogMessage("I", "", "======= DELETE DTL 前24個月=[" + hSystemDate + "] -24(Month)");
      daoTable = "bil_dodo_dtl";
      whereStr = "where tx_date <= to_char(add_months(to_date(?,'yyyymmdd'),-24),'yyyymmdd')";
      setString(1, hSystemDate);
      deleteTable();

      showLogMessage("I", "", "======= DELETE DTL =[" + hBusinessPrevMonth + "]");
      daoTable = "bil_dodo_dtl";
   // whereStr = "where nvl(mod_user,'') !='SYSCNV' AND acct_month = ? ";
      whereStr = "where acct_month = ? ";
      setString(1, hBusinessPrevMonth);
      deleteTable();

      showLogMessage("I", "", "======= DELETE MST =[" + hBusinessPrevMonth + "]");
      daoTable = "bil_dodo_mst";
   // whereStr = "where nvl(mod_user,'') !='SYSCNV' AND acct_month = ? ";
      whereStr = "where acct_month = ? ";
      setString(1, hBusinessPrevMonth);
      deleteTable();

    }
    /***************************************************************************/
    private void deleteBilDodoDtlTemp() throws Exception {
      daoTable = "bil_dodo_dtl_temp";
      whereStr = "where tx_date <= to_char(add_months(to_date(?,'yyyymmdd'),-24),'yyyymmdd')";
      setString(1, hSystemDate);
      deleteTable();

      showLogMessage("I", "", "======= DELETE TEMP =[" + hBusinessPrevMonth + "]");
      daoTable = "bil_dodo_dtl_temp ";
      whereStr = "where acct_month = ? ";
      setString(1, hBusinessPrevMonth);
      deleteTable();

    }
    /***********************************************************************/
    void selectSmsMsgId() throws Exception {

        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "N";
        hSmidAcctTypeSel = "";
        hSmidMsgUserid = "";

        sqlCmd = "select msg_id,";
        sqlCmd += "msg_dept,";
        sqlCmd += "msg_send_flag,";
        sqlCmd += "decode(acct_type_sel,'','Y',acct_type_sel) h_smid_acct_type_sel,";
        sqlCmd += "msg_userid ";
        sqlCmd += " from sms_msg_id  ";
        sqlCmd += "where msg_pgm    =? ";
        sqlCmd += "and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
        setString(1, javaProgram);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "--簡訊暫停發送 (SMS_P010)");
            smsTag = 1;
        }
        if (recordCnt > 0) {
            hSmidMsgId = getValue("msg_id");
            hSmidMsgDept = getValue("msg_dept");
            hSmidMsgSendFlag = getValue("msg_send_flag");
            hSmidAcctTypeSel = getValue("h_smid_acct_type_sel");
            hSmidMsgUserid = getValue("msg_userid");
        }

    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {

        hWdayStmtCycle.clear();
        hWdayNextCloseDate.clear();
        hWdayLastAcctMonth.clear();
        hWdayThisAcctMonth.clear();
        
        sqlCmd = "select stmt_cycle,";
        sqlCmd += "next_close_date,";
        sqlCmd += "last_acct_month,";
        sqlCmd += "this_acct_month ";
        sqlCmd += " from ptr_workday ";
        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            hWdayStmtCycle.add(getValue("stmt_cycle", i));
            hWdayNextCloseDate.add(getValue("next_close_date", i));
            hWdayLastAcctMonth.add(getValue("last_acct_month", i));
            hWdayThisAcctMonth.add(getValue("this_acct_month", i));
        }

        stmtCycleCnt = recordCnt;
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        int idx = 0, tempInt1 = 0, tempInt2 = 0, tempInt3 = 0;
        
        tempInt = 0; 
        sqlCmd = "select count(*) as temp_int ";
        sqlCmd += "  from bil_dodo_parm ";
        sqlCmd += " where card_type_flag  = '0' ";
        sqlCmd += "   and group_code_flag = '0' ";
        sqlCmd += "   and apr_flag        = 'Y' ";
        if (selectTable() > 0) {
            tempInt = getValueInt("temp_int");
        }

        sqlCmd  = "select ";
        sqlCmd += "decode(a.group_code,'','0000',a.group_code) h_card_group_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.old_card_no,";
        sqlCmd += "a.major_id_p_seqno,";
        sqlCmd += "b.chi_name ,";
        sqlCmd += "b.id_no    ,";
        sqlCmd += "c.id_no  as major_id,";
        sqlCmd += "a.current_code,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "decode(ori_issue_date, null, '29900101', '', '29900101', ori_issue_date) as ori_issue_date ";
   //   sqlCmd += "to_char(add_months(to_date(decode(ori_issue_date, null, '29900101', '', '29900101', ori_issue_date),'yyyymmdd'),12),'yyyymmdd') h_card_next_issue,";
   //   sqlCmd += "(substr(decode(ori_issue_date, null, '29900101', '', '29900101', ori_issue_date),1,6) || stmt_cycle) as h_issu_next_close_date ";
        sqlCmd += " from crd_idno b, crd_idno c, crd_card a ";
        sqlCmd += "where a.current_code  = '0' ";
        sqlCmd += "  and b.id_p_seqno    = a.id_p_seqno ";
        sqlCmd += "  and c.id_p_seqno    = a.major_id_p_seqno ";
        sqlCmd += "  and ((? > 0) or  (? = 0 ";
        sqlCmd += "  and ((card_type in ( select data_code ";
        sqlCmd +=                         " from bil_dodo_bn_data ";
        sqlCmd +=                         "where data_type    = '01' ";
        sqlCmd +=                         "  and apr_flag     = 'Y')) or ";
        sqlCmd +=       " (decode(group_code, '','0000', group_code) in ( select data_code  ";
        sqlCmd += "          from bil_dodo_bn_data  ";
        sqlCmd += "         where data_type = '02' ";
        sqlCmd += "           and apr_flag  = 'Y'))))) ";
if(DEBUG_DATA==1)
  {
   sqlCmd = sqlCmd + " and a.card_no    in ('4258700022946103') ";
/*
   sqlCmd = sqlCmd + " and a.card_type  in ('VI','MI') ";
   sqlCmd = sqlCmd + " and a.group_code in ('1620','1630') ";
   sqlCmd = sqlCmd + " limit 20000 ";
*/
  }
        setInt(1, tempInt);
        setInt(2, tempInt);
        showLogMessage("I",""," Open card  parm=" + tempInt);
        openCursor();
        showLogMessage("I",""," Open card  end");
        while(fetchTable()) {
            hCardGroupCode     = getValue("h_card_group_code");
            hCardCardType      = getValue("card_type");
            hCardCardNo        = getValue("card_no");
            hCardAcctType      = getValue("acct_type");
            hCardIdPSeqno      = getValue("id_p_seqno");
            hCardPSeqno        = getValue("acno_p_seqno");
            hCardMajorCardNo   = getValue("major_card_no");
            hCardOldCardNo     = getValue("old_card_no");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hCardCurrentCode   = getValue("current_code");
            hIdnoChiName       = getValue("chi_name");
            hIdnoIdNo          = getValue("id_no");
            hIdnoMajorId       = getValue("major_id");
            hCardStmtCycle     = getValue("stmt_cycle");
            hCardIssueDate     = getValue("ori_issue_date");
            if(hCardIssueDate.equals("00000000"))
               hCardIssueDate  = "29900101";
         // hCardNextIssue     = getValue("h_card_next_issue");
         // hIssuNextCloseDate = getValue("h_issu_next_close_date");
            hCardNextIssue     = commDate.dateAdd(hCardIssueDate, 0, 12, 0);
            hIssuNextCloseDate = hCardIssueDate.substring(0,6)+hCardStmtCycle;

            totCnt++;
            if (totCnt % 10000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Data Process record=[%d]\n", totCnt));
            if (DEBUG == 1)
                showLogMessage("I","","Read card_no=" + hCardCardNo+","+hCardIssueDate+","+hIssuNextCloseDate+","+totCnt);

            sqlCmd = "select to_char((to_date(?,'yyyymmdd')+7 days),'yyyymmdd') as h_new_date_7 ";
            sqlCmd += "  from dual ";
            setString(1, hCardIssueDate);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hNewDate7 = getValue("h_new_date_7");
            }

            hTempCnt = 0;
            hTempAmt = 0;

            rtn = chkStmtCycle(hCardStmtCycle);
            if (DEBUG == 1) showLogMessage("I", "", " 888 chk_stmt =[" + rtn + "]" + hCardStmtCycle+",Acct="+hBillAcctMonth);
            if (rtn > 0)      continue;

            selectBilDodoBnData(hCardGroupCode, hCardCardType);

            if (DEBUG == 1)
                showLogMessage("I", "", " 888 cnt=[" + dodoBnDataCnt + "]");
            swNext = "N";
            if (comcr.str2double(hCardNextIssue) < comcr.str2double(hBusinessDate))
                swNext = "Y";

            for (idx = 0; idx < dodoBnDataCnt; idx++) {
                selectBilDodoMst(hActionCd.get(idx));

                rtn = chkBilDodoParm(hActionCd.get(idx));
            }
        }
        closeCursor();
        if(DEBUG == 1) showLogMessage("I","", String.format("  get crd end tot=[%d]", totCnt));
    }

    /***********************************************************************/
    int chkStmtCycle(String tStmtCycle) {
        int idx1 = 0;

        hBillAcctMonth = "";
        lastMonthF = "";
        lastMonthT = "";

        for (idx1 = 0; idx1 < stmtCycleCnt; idx1++) {
            if (tStmtCycle.equals(hWdayStmtCycle.get(idx1))) {
                hBillAcctMonth = hWdayThisAcctMonth.get(idx1);
                idx1 = 999;
            }
        }
        lastMonthF = String.format("%s01", hBusinessPrevMonth);
        lastMonthT = String.format("%s31", hBusinessPrevMonth);

        if (idx1 < 999)
            return 1;
        return 0;
    }

    /***********************************************************************/
    void selectBilDodoBnData(String tGroupCode, String tCardType) throws Exception {
        int idx = 0;

        hActionCd.clear();
        hCarHours.clear();
        
        sqlCmd = "select ";
        sqlCmd += "action_cd,";
        sqlCmd += "car_hours ";
        sqlCmd += " from bil_dodo_parm p ";
        sqlCmd += "where apr_flag = 'Y' ";
        sqlCmd += "   and ((card_type_flag = '0'  or  (card_type_flag = '1' ";
        sqlCmd += "                               and exists (select 1 from bil_dodo_bn_data c  ";
        sqlCmd += "                                            where p.action_cd = c.action_cd "; 
        sqlCmd += "                                              and c.data_type = '01' ";
        sqlCmd += "                                              and c.data_code = ? ";
        sqlCmd += "                                              and c.apr_flag  = 'Y'))) ";
        sqlCmd += "   and  (group_code_flag = '0' or  (group_code_flag = '1' ";
        sqlCmd += "                               and exists (select 1 from bil_dodo_bn_data g  ";
        sqlCmd += "                                            where p.action_cd = g.action_cd  ";
        sqlCmd += "                                              and g.data_type = '02' ";
        sqlCmd += "                                              and g.data_code = ? ";
        sqlCmd += "                                              and g.apr_flag  = 'Y')))) ";
        setString(1, tCardType);
        setString(2, tGroupCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            String actionCd = getValue("action_cd", i);
            int carHours = getValueInt("car_hours", i);

            hActionCd.add(actionCd);
            hCarHours.add(carHours);
            idx++;

        }
        dodoBnDataCnt = idx;
    }
    /***********************************************************************/
    void selectBilDodoMst(String tActionCd) throws Exception {
        hDomsBegDate = "";
        hDomsEndDate = "";
        hDomsDodoMsg = "";
        hDomsRowid = "";

        sqlCmd = "select beg_date,";
        sqlCmd += "end_date,";
        sqlCmd += "dodo_msg,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_dodo_mst  ";
        sqlCmd += "where card_no   = ?  ";
        sqlCmd += "  and action_cd = ? ";
        setString(1, hCardCardNo);
        setString(2, tActionCd);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDomsBegDate = getValue("beg_date");
            hDomsEndDate = getValue("end_date");
            hDomsDodoMsg = getValue("dodo_msg");
            hDomsRowid = getValue("rowid");
        }
    }
    /***********************************************************************/
    int chkBilDodoParm(String tActionCd) throws Exception {
        long tempCnt = 0;
        int tempDays = 0;
        double tempAmtFm = 0;
        double tempAmtTo = 0;

        for (int idx1 = 0; idx1 < dodoParmCnt; idx1++) {
            if (tActionCd.equals(hDopaActionCd.get(idx1)) == false)
                continue;

            tempCnt = hDopaConsumeCnt1.get(idx1);
            tempAmtFm = hDopaConsumeAmtFm1.get(idx1);
            tempAmtTo = hDopaConsumeAmtTo1.get(idx1);
            tempDateFm = hDopaDateFm1.get(idx1);
            tempDateTo = hDopaDateTo1.get(idx1);
            tempMethod = hDopaConsumeMethod1.get(idx1);
            tempPeriod = hDopaConsumePeriod1.get(idx1);
            tempDays = hDopaLimitDays1.get(idx1);
            /*** 首年 ***/
            tempItemEnameBl = hDopaItemEnameBl1.get(idx1);
            tempItemEnameIt = hDopaItemEnameIt1.get(idx1);
            tempItemEnameCa = hDopaItemEnameCa1.get(idx1);
            tempItemEnameId = hDopaItemEnameId1.get(idx1);
            tempItemEnameAo = hDopaItemEnameAo1.get(idx1);
            tempItemEnameOt = hDopaItemEnameOt1.get(idx1);
            tempMchtNo = hDopaMchtNo1.get(idx1);
            tempDataType = "03";
            if (swNext.equals("Y")) {
                tempCnt = hDopaConsumeCnt2.get(idx1);
                tempAmtFm = hDopaConsumeAmtFm2.get(idx1);
                tempAmtTo = hDopaConsumeAmtTo2.get(idx1);
                tempDateFm = hDopaDateFm2.get(idx1);
                tempDateTo = hDopaDateTo2.get(idx1);
                tempMethod = hDopaConsumeMethod2.get(idx1);
                tempPeriod = hDopaConsumePeriod2.get(idx1);
                tempDays = hDopaLimitDays2.get(idx1);
                /*** 次年以後 ***/
                tempItemEnameBl = hDopaItemEnameBl2.get(idx1);
                tempItemEnameIt = hDopaItemEnameIt2.get(idx1);
                tempItemEnameCa = hDopaItemEnameCa2.get(idx1);
                tempItemEnameId = hDopaItemEnameId2.get(idx1);
                tempItemEnameAo = hDopaItemEnameAo2.get(idx1);
                tempItemEnameOt = hDopaItemEnameOt2.get(idx1);
                tempMchtNo = hDopaMchtNo2.get(idx1);
                tempDataType = "04";
            }
if(DEBUG == 1) showLogMessage("I", "", "  action="+tActionCd+","+swNext+","+tempMethod+","+tempPeriod);

            if (hDopaTotalBonus.get(idx1) > 0) {
                // ${剩餘紅利點數} = comb.bonus_sum(getValue("p_seqno"));
                hBpcdNetTtlBp = comb.bonusSum(hCardPSeqno);
                if (hBpcdNetTtlBp < hDopaTotalBonus.get(idx1))
                    continue;
            }
            
//            if ((tempCnt < 0 || hCnt < tempCnt || hAmt < tempAmtFm || hAmt > tempAmtTo ||
//                    ((tempAmtFm < 0 && tempAmtTo < 0)))) {
//            	 continue;
//            }
            
            /* 新申請 當月中 */
//            if (comcr.str2double(hDomsEndDate) >= comcr.str2double(hBusinessDate) &&
//                comcr.str2double(hDomsBegDate) <= comcr.str2double(hBusinessDate)) {
//            	  if (((tempCnt >= 0 && hCnt >= tempCnt) &&
//                          ((tempAmtFm > 0 || tempAmtTo > 0)&&(hAmt >= tempAmtFm && hAmt <= tempAmtTo)))) {
//            	         okRtn(hDopaCarHours.get(idx1), tActionCd , 1);   // no bill
//            	  }
//       
//                continue;
//            }

            if (hDopaDocument.get(idx1).length() > 0) {
                rtn = chkMktVoice(hDopaDocument.get(idx1));
                if (rtn > 0)
                    continue;
            }
            /* 第一次 新申請 */
//            if (hDomsBegDate.length() == 0 && tempPeriod.equals("1")) {
//          	  if (((tempCnt >= 0 && hCnt >= tempCnt) &&
//                      ((tempAmtFm > 0 || tempAmtTo > 0)&&(hAmt >= tempAmtFm && hAmt <= tempAmtTo)))) {
//          	     rtn = checkNewCard(hDopaCarHours.get(idx1), tActionCd, hDopaDocument.get(idx1));
//          	   if (rtn > 0)
//                   continue;
//          	  }
//            }
            
            if(DEBUG == 1) showLogMessage("I","", String.format("  method=[%s],[%s]"
                                  , hDopaConsumeMethod1.get(idx1) , hDopaConsumePeriod1.get(idx1)));
            
            /* consume_method ==> 1: 帳戶 2: 正附卡合併 3: ID */
            /* consume_period ==> 1: 結帳日 2: 月 3: 區間 */
            sData = "";
            if (tempMethod.equals("1")) {
                switch (comcr.str2int(tempPeriod)) {
                case 1:
                    join11(tActionCd);
                    break;
                case 2:
                    join12(tActionCd);
                    break;
                case 3:
                    rtn = join13(tempDateFm, tempDateTo, tempDays, tActionCd);
                    if (rtn > 0)
                        continue;
                    break;
                }
            } else if (tempMethod.equals("2")) {
                prevCnt = chkPrevCard(tActionCd, tempDays);
                hTempCardNo = hCardCardNo;
                switch (comcr.str2int(tempPeriod)) {
                case 1:
                    join21(tActionCd);
                    break;
                case 2:
                    join22(tActionCd);
                    break;
                case 3:
                    rtn = join23(tempDateFm, tempDateTo, tempDays, tActionCd);
                    if (rtn > 0)
                        continue;
                    break;
                }
            } else if (tempMethod.equals("3")) {
                switch (comcr.str2int(tempPeriod)) {
                case 1:
                    join31(tActionCd);
                    break;
                case 2:
                    join32(tActionCd);
                    break;
                case 3:
                    rtn = join33(tempDateFm, tempDateTo, tempDays, tActionCd);
                    if (rtn > 0)
                        continue;
                    break;
                }
            }

   if(DEBUG == 1) showLogMessage("I","", "  step excuteRtn=["+ tActionCd +"]"+lastMonthF+","+lastMonthT+",Data_type="+tempDataType+","+tempMchtNo+","+hCardIdPSeqno+",acct_m="+hBillAcctMonth); 
            excuteRtn();

            if (prevCnt > 0) {
                hCnt = hCnt + hTempCnt;
                hAmt = hAmt + hTempAmt;
            }

//            if (((tempCnt > 0 && hCnt >= tempCnt) ||
            if (((tempCnt >= 0 && hCnt >= tempCnt) &&
                 ((tempAmtFm > 0 || tempAmtTo > 0)&&(hAmt >= tempAmtFm && hAmt <= tempAmtTo)))) {
//                || (tempCnt == 0 && tempAmtFm == 0 && tempAmtTo == 0)) {
//            	&& (tempCnt >= 0)) {
            
                hBegDate = hBusinessDate;
                okRtn(hDopaCarHours.get(idx1), tActionCd , 2);   //  bill
            }

        }

        return 0;
    }
/***********************************************************************/
void okRtn(int tHour, String tActionCd, int idx) throws Exception 
{
  if(DEBUG == 1) showLogMessage("I", "", " 888 insert okRtn idx=[" + idx + "]"+ hCardCardNo);

  if (hDomsDodoMsg.equals("N") || hDomsDodoMsg.length() == 0) {
      selectCrdIdno();
      if ((hSmidMsgSendFlag.equals("Y")) && (checkAcctType() == 0)) {
           szTmpsms = String.format("%s,%s,%s,%s", hSmidMsgUserid, hSmidMsgId
                                                 , hIdnoCellarPhone, "市區停車");

           if (selectSmsMsgDtl() == 0)     insertSmsMsgDtl();

      }
  }
  daoTable = "bil_dodo_mst";
  extendField = daoTable + ".";
  setValue(extendField + "card_no" , hCardCardNo);
  setValue(extendField + "consume_period_1", tempPeriod);
  setValue(extendField + "action_cd", tActionCd);
  setValue(extendField + "beg_date" , hBegDate);
  setValue(extendField + "end_date" , hEndDate);
  setValue(extendField + "dodo_msg" , smsMsgFlag);
  setValue(extendField + "send_mcht", "D");
  setValue(extendField + "mod_pgm"  , javaProgram);
  setValue(extendField + "mod_time" , sysDate + sysTime);
  setValue(extendField + "acct_month"      , hBusinessPrevMonth);
  insertTable();

  totCnt0++;
  daoTable = "bil_dodo_dtl";
  extendField = daoTable + ".";
  setValue(extendField + "id_p_seqno"  , hCardIdPSeqno);
  setValue(extendField + "card_no"     , hCardCardNo);
  setValueInt(extendField + "car_hours", tHour);
  setValue(extendField + "tx_date"     , hSystemDate);
  setValue(extendField + "action_cd"   , tActionCd);
  setValue(extendField + "send_mcht"   , "D");
  setValue(extendField + "mod_pgm"     , javaProgram);
  setValue(extendField + "mod_time"    , sysDate + sysTime);
  setValue(extendField + "acct_month"      , hBusinessPrevMonth);
  insertTable();
  if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_bil_dodo_mst duplicate", hCardCardNo+","+tActionCd, comcr.hCallBatchSeqno);
  }
//V2.00.01新增
  insertBilDodoDtlTemp(tHour, tActionCd);
}
/***********************************************************************/
private void insertBilDodoDtlTemp(int tHour, String tActionCd) throws Exception {
        daoTable    = "bil_dodo_dtl_temp";
        extendField = daoTable + ".";
        setValue(extendField + "chi_name"        , hIdnoChiName);
        setValue(extendField + "id_no"           , hIdnoIdNo);
        setValue(extendField + "major_id"        , hIdnoMajorId);

        setValue(extendField + "group_code"      , hCardGroupCode);
        setValue(extendField + "current_code"    , hCardCurrentCode);
        setValue(extendField + "acno_p_seqno"    , hCardAcnoPSeqno);
        setValue(extendField + "major_card_no"   , hCardMajorCardNo);
        setValue(extendField + "major_id_p_seqno", hCardMajorIdPSeqno);
        setValue(extendField + "old_card_no"     , hCardOldCardNo);

        setValue(extendField + "acct_month"      , hBusinessPrevMonth);
        setValue(extendField + "use_month"       , hBusinessDate.substring(0,6));
        setValue(extendField + "purchase_date"   , hBillPurchaseDate);
        setValueInt(extendField + "curr_tot_cnt" , hCnt );
        setValueDouble(extendField + "curr_max_amt"   , hMaxAmt );
        setValueDouble(extendField + "tot_amt"        , hAmt );
        setValueInt(extendField + "free_cnt"     , 1    );
        setValue(extendField + "data_from"       , "2");
        setValue(extendField + "consume_method"  , tempMethod);
        setValue(extendField + "send_date"       , hBusinessDate);
        setValue(extendField + "id_p_seqno"   , hCardIdPSeqno);
        setValue(extendField + "card_no"      , hCardCardNo);
        setValueInt(extendField + "car_hours" , tHour);
        setValue(extendField + "tx_date"      , hSystemDate);
        setValue(extendField + "action_cd"    , tActionCd);
        setValue(extendField + "send_mcht"    , "D");
        setValue(extendField + "create_date"  , hBusinessDate);
        setValue(extendField + "create_time"  , sysTime);
        setValue(extendField + "aud_type"     , "A");
        setValue(extendField + "mod_pgm"      , javaProgram);
        setValue(extendField + "mod_time"     , sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_dodo_dtl_temp duplicate", hCardCardNo+","+tActionCd, comcr.hCallBatchSeqno);
        }

}
/***********************************************************************/
    void selectCrdIdno() throws Exception {
        hSmsChiName = "";
        hIdnoCellarPhone = "";
        hIdnoSex         = "";

        sqlCmd = "select chi_name,";
        sqlCmd += "cellar_phone,";
        sqlCmd += "sex ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCardIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSmsChiName      = getValue("chi_name");
            hIdnoCellarPhone = getValue("cellar_phone");
            hIdnoSex         = getValue("sex");
        }

    }
    /***
     * @throws Exception
     ***************************************************************/
    int checkAcctType() throws Exception {

        if (hSmidAcctTypeSel.equals("Y"))
            return (0);

        if (hSmidAcctTypeSel.equals("N")) {
            sqlCmd = "select data_code from sms_dtl_data where table_name='SMS_MSG_ID' and data_key = ? and data_type='1'";
            setString(1, javaProgram);
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("data_code", i);
                if (dataCode.equals(hCardAcctType)) {
                    return 0;
                }
            }
        }

        return 1;
    }
    /***********************************************************************/
    int selectSmsMsgDtl() throws Exception {
        if (smsTag == 1)
            return 0;

        sqlCmd = "select 1 ";
        sqlCmd += " from sms_msg_dtl  ";
//欄位移除        sqlCmd += "where office_m_code  = 'DODO'  ";
        sqlCmd += "where 1=1  ";
        sqlCmd += "  and id_p_seqno     = ?  ";
        sqlCmd += "  and decode(send_flag,'','N',send_flag) != 'Y'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hCardIdPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        return 1;
    }
    /***********************************************************************/
    void insertSmsMsgDtl() throws Exception {
        if (smsTag == 1)
            return;

        hTempModSeqno = comcr.getModSeq();

        /*-- check cellphone format --*/
        hTempCellphoneCheckFlag = "Y";
        if (checkPhoneNo(hIdnoCellarPhone) != 0) {
            hTempCellphoneCheckFlag = "N";
        }

        smsMsgFlag = "Y";
        daoTable = "sms_msg_dtl";
	extendField = daoTable + ".";
        setValueDouble(extendField + "msg_seqno", hTempModSeqno);
        setValue(extendField + "msg_pgm", javaProgram);
        setValue(extendField + "msg_dept", hSmidMsgDept);
        setValue(extendField + "msg_userid", hSmidMsgUserid);
        setValue(extendField + "msg_id", hSmidMsgId);
        setValue(extendField + "cellar_phone", hIdnoCellarPhone);
        setValue(extendField + "cellphone_check_flag", hTempCellphoneCheckFlag);
        setValue(extendField + "chi_name", hSmsChiName);
        setValue(extendField + "msg_desc", szTmpsms);
        setValue(extendField + "p_seqno", hCardPSeqno);
        setValue(extendField + "acct_type", hCardAcctType);
        setValue(extendField + "id_p_seqno", hCardIdPSeqno);
        setValue(extendField + "add_mode", "B");
        setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_user", javaProgram);
        setValue(extendField + "apr_date", sysDate);
        setValue(extendField + "apr_user", javaProgram);
        setValue(extendField + "apr_flag", "Y");
        setValue(extendField + "send_flag", "N");
        setValue(extendField + "proc_flag", "N");
        //setValue("office_m_code", "DODO");
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_sms_msg_dtl duplicate", "", comcr.hCallBatchSeqno);
        }

        if (hDomsRowid.length() > 0) {
            daoTable = "bil_dodo_mst";
            updateSQL = "dodo_msg     = 'Y' ";
            whereStr = "where rowid  = ? ";
            setRowId(1, hDomsRowid);
            updateTable();
            if (notFound.equals("Y")) {
                String stderr = "update_bil_dodo_mst not found!";
                comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
            }
        }
    }
    /***********************************************************************/
    int checkPhoneNo(String pno) {

        if (pno.length() != 10)
            return 1;

        for (int i = 0; i < 10; i++) {
            if (pno.substring(i, i + 1).compareTo("0") < 0 || pno.substring(i, i + 1).compareTo("9") > 0)
                return 1;
        }

        return 0;
    }
    /***********************************************************************/
    int chkMktVoice(String tDocument) throws Exception {
        hMktInputDate7 = "";
        sqlCmd = "select min(input_date) h_mkt_input_date_7 ";
        sqlCmd += " from mkt_voice  ";
        sqlCmd += "where document  = ?  ";
        sqlCmd += "and card_no  = ?  ";
        sqlCmd += "and input_date >= '20070301' ";
        setString(1, tDocument);
        setString(2, hCardCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMktInputDate7 = getValue("h_mkt_input_date_7");
        }

        if (hMktInputDate7.length() == 0)
            return 1;
        sqlCmd = "select to_char((to_date(?,'yyyymmdd') + 7 days), 'yyyymmdd') ";
        sqlCmd += " from dual ";
        setString(1, hMktInputDate7);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMktInputDate7 = getValue("h_mkt_input_date_7");
        }

        return 0;
    }

    /**********************************************************************/
    int checkNewCard(int cHour, String tActionCd, String tDocu) throws Exception {
        String hTempDate = "";
        String hTempEndDate = "";

        /* temp_period -> consume_period ==> 1: 結帳日 2: 月 3: 區間 */
        if (tempPeriod.equals("1")) {
            sqlCmd = "select to_char(add_months(to_date(? ,'yyyymmdd'),1) ,'yyyymmdd') as h_temp_end_date ";
            sqlCmd += " from dual ";
            setString(1, hIssuNextCloseDate);
            if (DEBUG == 1)
                showLogMessage("I", "", "  CHK NEXT=" + hIssuNextCloseDate);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_dual not found!", "", comcr.hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hTempEndDate = getValue("h_temp_end_date");
            }

            if (tDocu.length() > 0) {
                hTempDate = hMktInputDate7;
            } else {
                hTempDate = hNewDate7;
            }

            hBegDate = hBusinessDate;
            if (comcr.str2double(hTempDate) < comcr.str2double(hIssuNextCloseDate)) {
                hEndDate = hIssuNextCloseDate;
            } else {
                hEndDate = hTempEndDate;
            }

            if (hBegDate.compareTo(hEndDate) >= 0)
                return 0;

            okRtn(cHour, tActionCd , 3);
            return 1;
        } else if (tempPeriod.equals("2")) {
            sqlCmd = "select (substr(to_char(add_months(to_date(? ,'yyyymmdd'),1),'yyyymmdd'),1,6) || '01') as h_beg_date,";
            sqlCmd += "to_char(last_day(add_months(to_date(? ,'yyyymmdd'),1)) ,'yyyymmdd') h_end_date ";
            sqlCmd += " from dual ";
            setString(1, hBusinessDate);
            setString(2, hBusinessDate);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_dual not found!", "", comcr.hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hBegDate = getValue("h_beg_date");
                hEndDate = getValue("h_end_date");
            }
            if (hBegDate.compareTo(hEndDate) >= 0)
                return 0;

            okRtn(cHour, tActionCd , 4);
            return 1;
        } else if (tempPeriod.equals("3")) {
            hBegDate = tempDateFm;
            hEndDate = tempDateTo;

            if (hBegDate.compareTo(hEndDate) >= 0)
                return 0;

            okRtn(cHour, tActionCd, 5);
            return 1;
        }

        return 0;
    }
    /**********************************************************************/
    int chkPrevCard(String tActionCd, int days) throws Exception {
        int status4 = 0;
        int idxCnt = 0;
        String hMewCardNo = "";

        hCrd1CardNo = "";
        hMewCardNo = hCardCardNo;
        while (status4 == 0) {
            sqlCmd = "select card_no ";
            sqlCmd += " from crd_card a  ";
            sqlCmd += "where id_p_seqno   = ?  ";
            sqlCmd += "and card_type in ( select data_code from bil_dodo_bn_data where data_type = '01'  ";
            sqlCmd += "and apr_flag = 'Y')  ";
            sqlCmd += "and new_card_no = ? ";
            setString(1, hCardIdPSeqno);
            setString(2, hMewCardNo);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                status4 = 1;
                continue;
            }
            if (recordCnt > 0) {
                hCrd1CardNo = getValue("card_no");
            }

            hMewCardNo  = hCrd1CardNo;
            hTempCardNo = hCrd1CardNo;
            switch (comcr.str2int(tempPeriod)) {
            case 1:
                join21(tActionCd);
                break;
            case 2:
                join22(tActionCd);
                break;
            case 3:
                join23(tempDateFm, tempDateTo, days, tActionCd);
                break;
            }

            excuteRtn();

            hTempCnt = hTempCnt + hCnt;
            hTempAmt = hTempAmt + hAmt;
            idxCnt++;
        }

        return idxCnt;
    }
    /**********************************************************************/
    int excuteRtn() throws Exception {
        hCnt = 0;
        hAmt = 0;
        hBillPurchaseDate = "";

        sqlCmd = sData;
        for (int i = 0; i < sDataParam.size(); i++) {
            setString(i + 1, String.valueOf(sDataParam.get(i)));
        }
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("excute_rtn() not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt    = getValueInt("h_cnt");
            hAmt    = getValueDouble("h_amt");
            hMaxAmt = getValueDouble("h_max_amt");
            hBillPurchaseDate = getValue("purchase_date");
        }
   if(DEBUG == 1) showLogMessage("I","", "     excuteRtn end=["+ hCnt +"]"+hAmt); 

        return 0;

    }
    /***********************************************************************/
    void join21(String tActionCd) {
        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt, sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0), "
                + "           case when a.txn_code in ('06','25','27','28','29') "
                + "                then -1 else 1 end)) as h_cnt,  " 
                + "       sum(decode(a.acct_code,'IT',    "
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0), "
                + "           case when a.txn_code in ('06','25','27','28','29') "
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a      "
                + " left join bil_contract b on " 
                +      "  b.contract_no     = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
//              + "   where a.card_no             = ?                   "
                + "   where a.major_card_no       = ?                   "
                + "     and a.acct_month          = ?                   "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key ='MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "     and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                           "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                           "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                           "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                           "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                           "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "     and  (? = '0'  or  (? = '1' "
                +    "   and   decode(a.mcht_no,'',' ',a.mcht_no)   in (select data_code " 
                   + " from bil_dodo_bn_data where action_cd = ? " 
                   + "  and data_type = ?)) " + "  or  (? = '2' "
                +    "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " + " from bil_dodo_bn_data where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hTempCardNo);
        sDataParam.add(parmFlag==true?comms.left(hBusinessDate, 6):hBillAcctMonth);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);

    }
    /***********************************************************************/
    void join22(String tActionCd) {

        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt, sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a      "
                + " left join bil_contract b on " 
                +      "  b.contract_no     = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
//                + " where a.card_no          = ? "
                + "   where a.major_card_no    = ? "
                + "   and a.purchase_date     >= ? " 
                + "   and a.purchase_date     <= ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'   or  (? = '1' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?)) " + "    or   (? = '2' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hTempCardNo);
        sDataParam.add(lastMonthF);
        sDataParam.add(lastMonthT);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);

    }
    /***********************************************************************/
    int join23(String dateFm, String dateTo, int days, String tActionCd) throws Exception {
        String tempX08 = "";

        dateFm = dateFm.length() == 0 ? "19000101" : dateFm;
        dateTo = dateTo.length() == 0 ? "29991231" : dateTo;
        
        sqlCmd = "select to_char((to_date(?,'yyyymmdd') + cast(? as int) days), 'yyyymmdd') as temp_x08 ";
        sqlCmd += " from dual ";
        setString(1, dateTo);
        setInt(2, days);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX08 = getValue("temp_x08");
        }

        if (comcr.str2double(hBusinessDate) > comcr.str2double(tempX08))
            return 1;
        if (comcr.str2double(hBusinessDate) <= comcr.str2double(dateTo))
            return 1;

        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt, sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join bil_contract b on " 
                +      "  b.contract_no     = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
 //               + " where a.card_no          = ? "
                + "   where a.major_card_no    = ? "
                + "   and a.purchase_date     >= ? "
                + "   and a.purchase_date     <= ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key ='MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                        " decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                        " decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                        " decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                        " decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                        " decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'   or  (? = '1' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?)) " + "    or   (? = '2' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hTempCardNo);
        sDataParam.add(dateFm);
        sDataParam.add(dateTo);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);

        return 0;
    }
    /***********************************************************************/
    void join11(String tActionCd) {
        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt,  sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join bil_contract b on " 
                + "  b.contract_no = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
                + "   where a.acct_type            = ? "
             // + "     and a.acno_p_seqno         = ? " 
                + "     and a.p_seqno              = ? " 
                + "   and decode(a.acct_month,'','x',a.acct_month)  = ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'   or  (? = '1' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " 
                + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?)) " 
                + "    or   (? = '2' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " 
                + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hCardAcctType);
        sDataParam.add(hCardPSeqno);
        sDataParam.add(parmFlag==true?comms.left(hBusinessDate, 6):hBillAcctMonth);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
    }
    /***********************************************************************/
    void join12(String tActionCd) {
        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt,sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join bil_contract b on " + " b.contract_no     = decode(a.contract_no,'','x',a.contract_no) "
                + " and b.contract_seq_no = a.contract_seq_no      " + " where a.acct_type          = ? "
           //   + "   and a.acno_p_seqno            = ? " 
                + "   and a.p_seqno                 = ? " 
                + "   and a.purchase_date     >= ? "
                + "   and a.purchase_date     <= ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'   or  (? = '1' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?)) " + "    or   (? = '2' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hCardAcctType);
        sDataParam.add(hCardPSeqno);
        sDataParam.add(lastMonthF);
        sDataParam.add(lastMonthT);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
    }
    /**************************************************************/
    int join13(String dateFm, String dateTo, int days, String tActionCd) throws Exception {
        dateFm = dateFm.length() == 0 ? "19000101" : dateFm;
        dateTo = dateTo.length() == 0 ? "29991231" : dateTo;
        
        String tempX08 = "";
        sqlCmd = "select to_char((to_date(?,'yyyymmdd') + cast(? as int) days), 'yyyymmdd') as temp_x08 ";
        sqlCmd += " from dual ";
        setString(1, dateTo);
        setInt(2, days);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX08 = getValue("temp_x08");
        }

        if (comcr.str2double(hBusinessDate) > comcr.str2double(tempX08))
            return 1;
        if (comcr.str2double(hBusinessDate) <= comcr.str2double(dateTo))
            return 1;

        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt, sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join bil_contract b on " + "  b.contract_no = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " + "   where a.acct_type       = ? "
                + "   and a.p_seqno                = ? " 
                + "   and a.purchase_date     >= ? "
                + "   and a.purchase_date     <= ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
           //  +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0' or (? = '1' and a.mcht_no in (select data_code from bil_dodo_bn_data  where action_cd = ? and data_type = ?)) or  "
                       + " (? = '2' and a.mcht_no not in (select data_code from bil_dodo_bn_data where action_cd = ? and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hCardAcctType);
        sDataParam.add(hCardPSeqno);
        sDataParam.add(dateFm);
        sDataParam.add(dateTo);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);

        return 0;
    }
/***********************************************************************/
void join31(String tActionCd) throws Exception 
{
  sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt, "
        + "       sum(decode(a.acct_code,'IT', decode(a.install_curr_term,1, "
        + "                decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
        + "       case when a.txn_code in ('06','25','27','28','29') then -1 else 1 end)) as h_cnt,"
        + "       sum(decode(a.acct_code,'IT',"
        + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
        + "           case when a.txn_code in ('06','25','27','28','29')"
        + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt "
        + "  from bil_bill a   "
        + " left join crd_card c on a.card_no = c.card_no   left join bil_contract b on "
        + "  b.contract_no = decode(a.contract_no,'','x',a.contract_no) "
        + "   and b.contract_seq_no = a.contract_seq_no      " 
        + "   where a.id_p_seqno                   = ? "
        + "   and decode(a.acct_month,'','x',a.acct_month)  = ? "
        + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
        + "   and a.acct_type = '01' " //V2.00.01新增
        + "     and (ecs_cus_mcht_no = '' or not exists "
        +                      " (select 1 from mkt_mcht_gp x  left join "
        +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
        +                      "   where y.table_name = 'MKT_MCHT_GP' "
//      +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
        +                      "     and y.data_key = 'MKTR00001' "
        +                      "     and x.platform_flag = '2'  "
        +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
        + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
        +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
        +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
        +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
        +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
        +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
        + "   and  (? = '0'   or  (? = '1' "
        + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " + " from bil_dodo_bn_data "
        + "where action_cd = ? " + "  and data_type = ?)) " + "    or   (? = '2' "
        + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code from bil_dodo_bn_data "
        +                                                  " where action_cd = ? and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hCardIdPSeqno);
        sDataParam.add(parmFlag==true?comms.left(hBusinessDate, 6):hBillAcctMonth);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
}
/****************************************************************************/
void join32(String tActionCd) throws Exception 
{
        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt,sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join crd_card c on a.card_no = c.card_no " 
                + " left join bil_contract b on "
                + "  b.contract_no = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
                + " where a.id_p_seqno              = ? "
                + "   and decode(a.purchase_date,'','x',a.purchase_date) between ? and ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
                + "   and a.acct_type = '01' " //V2.00.01新增
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'   or  (? = '1' and decode(a.mcht_no,'',' ',a.mcht_no) "
                +                                            " in (select data_code " 
                +                                                  " from bil_dodo_bn_data "
                +                                                  "where action_cd = ? " 
                +                                                  "  and data_type = ?)) " 
                +                "    or  (? = '2' and decode(a.mcht_no,'',' ',a.mcht_no) "
                +                                            " not in (select data_code " 
                +                                                     "  from bil_dodo_bn_data "
                +                                                     " where action_cd = ? " 
                +                                                     "   and data_type = ?))) ";

//if(DEBUG == 1) showLogMessage("I", "", "    888 32 SQL=[" + sData + "]");

        sDataParam.clear();
        sDataParam.add(hCardIdPSeqno);
        sDataParam.add(lastMonthF);
        sDataParam.add(lastMonthT);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
}
/************************************************************/
int join33(String dateFm, String dateTo, int days, String tActionCd) throws Exception 
{

        dateFm = dateFm.length() == 0 ? "19000101" : dateFm;
        dateTo = dateTo.length() == 0 ? "29991231" : dateTo;
        
        String tempX08 = "";
        sqlCmd = "select to_char((to_date(?,'yyyymmdd') + cast(? as int) days), 'yyyymmdd') as temp_x08 ";
        sqlCmd += " from dual ";
        setString(1, dateTo);
        setInt(2, days);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX08 = getValue("temp_x08");
        }

        if (comcr.str2double(hBusinessDate) > comcr.str2double(tempX08))
            return 1;
        if (comcr.str2double(hBusinessDate) <= comcr.str2double(dateTo))
            return 1;

        sData = "select max(a.purchase_date) as purchase_date, max(a.dest_amt) as h_max_amt,sum(decode(a.acct_code,'IT', "
                + "           decode(a.install_curr_term,1,decode(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0),0,0,1),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then -1 else 1 end)) as h_cnt," 
                + "       sum(decode(a.acct_code,'IT',"
                + "           decode(a.install_curr_term,1,b.tot_amt*(b.qty-decode(b.refund_apr_flag,'Y',b.refund_qty,0)),0),"
                + "           case when a.txn_code in ('06','25','27','28','29')"
                + "                then a.dest_amt*-1 else a.dest_amt end)) as h_amt " 
                + "  from bil_bill a   "
                + " left join crd_card c on a.card_no = c.card_no " 
                + " left join bil_contract b on "
                + "  b.contract_no = decode(a.contract_no,'','x',a.contract_no) "
                + "   and b.contract_seq_no = a.contract_seq_no      " 
                + " where a.id_p_seqno         = ? "
                + "   and decode(a.purchase_date,'','x',a.purchase_date) >= ? "
                + "   and decode(a.purchase_date,'','x',a.purchase_date) <= ? "
                + "   and decode(a.rsk_type,'','N',a.rsk_type) not in ('1','2','3')  "
                + "   and a.acct_type = '01' " //V2.00.01新增
               + "     and (ecs_cus_mcht_no = '' or not exists "
               +                      " (select 1 from mkt_mcht_gp x  left join "
               +                      "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id"
               +                      "   where y.table_name = 'MKT_MCHT_GP' "
//             +                      "     and y.data_key IN ('MKTR000001','RPT_M040') "
               +                      "     and y.data_key = 'MKTR00001' "
               +                      "     and x.platform_flag = '2'  "
               +                      "     and y.data_code     = a.ecs_cus_mcht_no )) "
                + "   and a.acct_code  in (decode(cast(? as varchar(10)),'Y','BL','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','IT','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','ID','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','CA','XX'),  " 
                +                         "decode(cast(? as varchar(10)),'Y','AO','XX'),  "
                +                         "decode(cast(? as varchar(10)),'Y','OT','XX'))  " 
                + "   and  (? = '0'  or  (? = '1' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no)     in (select data_code " 
                + " from bil_dodo_bn_data "
                + "where action_cd = ? " 
                + "  and data_type = ?)) " 
                + "    or   (? = '2' "
                + "   and   decode(a.mcht_no,'',' ',a.mcht_no) not in (select data_code " 
                + " from bil_dodo_bn_data "
                + "where action_cd = ? " + "  and data_type = ?))) ";

        sDataParam.clear();
        sDataParam.add(hCardIdPSeqno);
        sDataParam.add(dateFm);
        sDataParam.add(dateTo);
        sDataParam.add(tempItemEnameBl);
        sDataParam.add(tempItemEnameIt);
        sDataParam.add(tempItemEnameId);
        sDataParam.add(tempItemEnameCa);
        sDataParam.add(tempItemEnameAo);
        sDataParam.add(tempItemEnameOt);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);
        sDataParam.add(tempMchtNo);
        sDataParam.add(tActionCd);
        sDataParam.add(tempDataType);

        return 0;
}
/***********************************************************************/
void selectBilDodoParm() throws Exception {

        redefine();
        
        sqlCmd = "select action_cd,";
        sqlCmd += "document_desc,";
        sqlCmd += "car_hours,";
        sqlCmd += "ivr_flag,";
        sqlCmd += "document,";
        sqlCmd += "consume_amt_fm_1,";
        sqlCmd += "consume_amt_to_1,";
        sqlCmd += "consume_cnt_1,";
        sqlCmd += "consume_period_1,";
        sqlCmd += "date_fm_1,";
        sqlCmd += "date_to_1,";
        sqlCmd += "limit_days_1,";
        sqlCmd += "consume_method_1,";
        sqlCmd += "consume_amt_fm_2,";
        sqlCmd += "consume_amt_to_2,";
        sqlCmd += "consume_cnt_2,";
        sqlCmd += "consume_period_2,";
        sqlCmd += "date_fm_2,";
        sqlCmd += "date_to_2,";
        sqlCmd += "limit_days_2,";
        sqlCmd += "consume_method_2,";
        sqlCmd += "ext_batch_no,";
        sqlCmd += "total_bonus,";
        sqlCmd += "item_ename_bl_1,";
        sqlCmd += "item_ename_it_1,";
        sqlCmd += "item_ename_ca_1,";
        sqlCmd += "item_ename_id_1,";
        sqlCmd += "item_ename_ao_1,";
        sqlCmd += "item_ename_ot_1,";
        sqlCmd += "mcht_no_1,";
        sqlCmd += "item_ename_bl_2,";
        sqlCmd += "item_ename_it_2,";
        sqlCmd += "item_ename_ca_2,";
        sqlCmd += "item_ename_id_2,";
        sqlCmd += "item_ename_ao_2,";
        sqlCmd += "item_ename_ot_2,";
        sqlCmd += "mcht_no_2 ";
        sqlCmd += " from bil_dodo_parm  ";
        sqlCmd += "where apr_flag = 'Y' order by car_hours desc ";
        int recordCnt = 0;
        openCursor();
        while(fetchTable()) {
            recordCnt++;
            hDopaActionCd.add(getValue("action_cd"));
            hDopaDocumentDesc.add(getValue("document_desc"));
            hDopaCarHours.add(getValueInt("car_hours"));
            hDopaIvrFlag.add(getValue("ivr_flag"));
            hDopaDocument.add(getValue("document"));
            hDopaConsumeAmtFm1.add(getValueDouble("consume_amt_fm_1"));
            hDopaConsumeAmtTo1.add(getValueDouble("consume_amt_to_1"));
            hDopaConsumeCnt1.add(getValueInt("consume_cnt_1"));
            hDopaConsumePeriod1.add(getValue("consume_period_1"));
            hDopaDateFm1.add(getValue("date_fm_1"));
            hDopaDateTo1.add(getValue("date_to_1"));
            hDopaLimitDays1.add(getValueInt("limit_days_1"));
            hDopaConsumeMethod1.add(getValue("consume_method_1"));
            hDopaConsumeAmtFm2.add(getValueDouble("consume_amt_fm_2"));
            hDopaConsumeAmtTo2.add(getValueDouble("consume_amt_to_2"));
            hDopaConsumeCnt2.add(getValueInt("consume_cnt_2"));
            hDopaConsumePeriod2.add(getValue("consume_period_2"));
            hDopaDateFm2.add(getValue("date_fm_2"));
            hDopaDateTo2.add(getValue("date_to_2"));
            hDopaLimitDays2.add(getValueInt("limit_days_2"));
            hDopaConsumeMethod2.add(getValue("consume_method_2"));
showLogMessage("I", "", "Get PARM=["+getValue("action_cd")+"],method="+getValue("consume_method_1")
  +","+getValue("consume_method_2")+",period="+getValue("consume_period_1")+","+getValue("consume_period_2"));
            hDopaExtBatchNo.add(getValue("ext_batch_no"));
            hDopaTotalBonus.add(getValueInt("total_bonus"));
            hDopaItemEnameBl1.add(getValue("item_ename_bl_1"));
            hDopaItemEnameIt1.add(getValue("item_ename_it_1"));
            hDopaItemEnameCa1.add(getValue("item_ename_ca_1"));
            hDopaItemEnameId1.add(getValue("item_ename_id_1"));
            hDopaItemEnameAo1.add(getValue("item_ename_ao_1"));
            hDopaItemEnameOt1.add(getValue("item_ename_ot_1"));
            hDopaMchtNo1.add(getValue("mcht_no_1"));
            hDopaItemEnameBl2.add(getValue("item_ename_bl_2"));
            hDopaItemEnameIt2.add(getValue("item_ename_it_2"));
            hDopaItemEnameCa2.add(getValue("item_ename_ca_2"));
            hDopaItemEnameId2.add(getValue("item_ename_id_2"));
            hDopaItemEnameAo2.add(getValue("item_ename_ao_2"));
            hDopaItemEnameOt2.add(getValue("item_ename_ot_2"));
            hDopaMchtNo2.add(getValue("mcht_no_2"));
        }
        closeCursor();
        dodoParmCnt = recordCnt;
    }
/************************************************************************/
    void redefine() {
        hDopaActionCd.clear();
        hDopaDocumentDesc.clear();
        hDopaCarHours.clear();
        hDopaIvrFlag.clear();
        hDopaDocument.clear();
        hDopaConsumeAmtFm1.clear();
        hDopaConsumeAmtTo1.clear();
        hDopaConsumeCnt1.clear();
        hDopaConsumePeriod1.clear();
        hDopaDateFm1.clear();
        hDopaDateTo1.clear();
        hDopaLimitDays1.clear();
        hDopaConsumeMethod1.clear();
        hDopaConsumeAmtFm2.clear();
        hDopaConsumeAmtTo2.clear();
        hDopaConsumeCnt2.clear();
        hDopaConsumePeriod2.clear();
        hDopaDateFm2.clear();
        hDopaDateTo2.clear();
        hDopaLimitDays2.clear();
        hDopaConsumeMethod2.clear();
        hDopaExtBatchNo.clear();
        hDopaTotalBonus.clear();
        hDopaItemEnameBl1.clear();
        hDopaItemEnameIt1.clear();
        hDopaItemEnameCa1.clear();
        hDopaItemEnameId1.clear();
        hDopaItemEnameAo1.clear();
        hDopaItemEnameOt1.clear();
        hDopaMchtNo1.clear();
        hDopaItemEnameBl2.clear();
        hDopaItemEnameIt2.clear();
        hDopaItemEnameCa2.clear();
        hDopaItemEnameId2.clear();
        hDopaItemEnameAo2.clear();
        hDopaItemEnameOt2.clear();
        hDopaMchtNo2.clear();
    }
  /***********************************************************************/
  private void iniRtn() throws Exception {
    hCardCardNo     = "";
    hIdNo           = "";
  }
/***********************************************************************/
void selectBilDodoDtlTemp() throws Exception 
{
    writeHead(9);

    fetchExtend = "file.";
    sqlCmd  = "select ";
    sqlCmd += "  a.card_no";
    sqlCmd += ", b.id_no";
    sqlCmd += " from crd_card c, crd_idno b, bil_dodo_dtl_temp a ";
    sqlCmd += "where b.id_p_seqno   = a.id_p_seqno ";
    sqlCmd += "  and c.card_no      = a.card_no    ";
    sqlCmd += "  and a.current_code = '0' ";
    sqlCmd += "  and a.aud_type     = 'A' ";
    sqlCmd += "  and a.acct_month   = ?   ";
    sqlCmd += "  and create_date    = ? ";
if(DEBUG_DATA==1)
  {
    sqlCmd = sqlCmd + " and a.card_no    in ('4258700008672111') ";
  }
    sqlCmd += "group by b.id_no, a.card_no ";
    sqlCmd += "order by b.id_no, a.card_no ";
    setString(1, hBusinessPrevMonth);
    setString(2, hBusinessDate);

    openCursor();

    while (fetchTable()) {
      iniRtn();
      hCardCardNo     = getValue("file.card_no");
      hIdNo           = getValue("file.id_no");

      totCnt1++;
      if(totCnt1 % 5000 == 0 || totCnt1 == 1)
         showLogMessage("I", "", String.format("Main Process record=[%d]", totCnt1));
      if(DEBUG == 1) showLogMessage("I", "", "Write card=" + hCardCardNo + ","+hIdNo+",Cnt="+ totCnt1);

      writeDtl(9);
    }

    writeTail(9);

    closeCursor();
  }
  /***********************************************************************/
  private void writeHead(int idx) throws Exception {
    buf = "";
    buf += fixLeft("1"         ,  1);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hSystemDate ,  8);
    buf += fixLeft("="         ,  1);
    buf += fixLeft("TcbNew"    ,  6);
    tmpStr = String.format("%18.18s"," ");
    buf += fixLeft(tmpStr      , 18);

    lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
  }
  /***********************************************************************/
  private void writeDtl(int idx) throws Exception {
    buf = "";
    buf += fixLeft("2"         ,  1);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hIdNo       , 16);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hCardCardNo , 16);

    lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
  }
  /***********************************************************************/
  private void writeTail(int idx) throws Exception {
    buf = "";
    buf += fixLeft("3"         ,  1);
    buf += fixLeft("="         ,  1);
    tmpStr = String.format("%07d",totCnt0);
    buf += fixLeft(tmpStr      ,  7);
    tmpStr = String.format("%26.26s"," ");
    buf += fixLeft(tmpStr      , 26);

    lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
}
/***********************************************************************/
private void ftpRtn(String hFileNameI) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";
    String hOwsWfValue3 = "";

    sqlCmd  = "select wf_value2 ";
    sqlCmd += " from ptr_sys_parm  ";
    sqlCmd += "where wf_parm = 'SYSPARM'  ";
    sqlCmd += "  and wf_key  = 'CITY_PARK_ZIP_PWD' ";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_sys_parm not found!", "CITY_PARK_ZIP_PWD", comcr.hCallBatchSeqno);
    }
    hOwsWfValue3 = getValue("wf_value2");

    /*** PKZIP 壓縮 ***/
    temstr1 = String.format("%s/media/bil/%s",comc.getECSHOME(), hFileNameI);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    temstr2 = String.format("%s.zip",temstr1);
    String filename = temstr1;
    String hPasswd  = hOwsWfValue3;
    String zipFile  = temstr2;
    int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
    if(tmpInt != 0) {
       comcr.errRtn(String.format("無法壓縮檔案[%s]", filename),"", hCallBatchSeqno);
    }
    comc.chmod777(zipFile);

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

    String  hEflgRefIpCode  = "NCR2TCB";
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgSystemId   = "NCR2TCB";
    commFTP.hEflgGroupId    = "0000";
    commFTP.hEflgSourceFrom = "EcsFtpBil";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/bil", comc.getECSHOME());
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
    filename  = String.format("%s.zip", hFileNameI);
    procCode  = String.format("put %s", filename);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(filename);
    }
}
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/bil/backup/%s_%s",comc.getECSHOME(),filename,sysDate);

   if (comc.fileRename(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }
       
   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
}
/***************************************************************************/
private String fixLeft(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += " ";
    if (str == null)
      str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);
    return new String(vResult, "MS950");
}
/***************************************************************************/
public static void main(String[] args) throws Exception {
        BilN002 proc = new BilN002();
        int   retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
}
/***************************************************************************/
}
