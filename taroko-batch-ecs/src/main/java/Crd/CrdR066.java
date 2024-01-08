/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/02/24 V1.01.01  Lai         program initial                            *
*  112/07/05 V1.01.02  Bo Yang     update by naming rule                      *
*  112/07/17 V1.01.03  Wilson      刪除程式重複執行判斷                       *
*  112/08/04 V1.01.04  Wilson      modify                                     *
*  112/08/15 V1.01.05  Lai         modify                                     *
*  112/08/26 V1.01.06  Wilson      調整歸戶數資料讀取邏輯                                                                             * 
*  112/11/08 V1.01.07  Wilson      日期減一天                                                                                                    *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class CrdR066 extends AccessDAO {
    private final String PROGNAME = "每月法人信用卡卡量彙總表  112/11/08 V1.01.07";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate    commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    int    DEBUG   = 0;
    int    DEBUG_F = 0;
    String hTempUser = "";

    int reportPageLine = 45;
    String prgmId    = "CrdR066";

    String rptIdR1 = "CRM66";
    String rptName1  = "每月法人信用卡卡量彙總表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastDay =  "";

    String cardCardNo = "";
    String cardCurrentCode = "";
    String cardOriIssueDate = "";
    String cardAcnoPSeqno = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinType = "";
    String cardName = "";
    String cardPSeqno = "";
    String cardCorpno = "";
    String cardLastConsumeDate = "";
    int issueVCnt = 0;
    int issueMCnt = 0;
    int currentVCnt = 0;
    int currentMCnt = 0;
    int effcVCnt = 0;
    int effcMCnt = 0;
    int issueVAll = 0;
    int issueMAll = 0;
    int currentVAll = 0;
    int currentMAll = 0;
    int effcVAll = 0;
    int effcMAll = 0;
    int acnoCurrentCnt = 0;
    int acnoEffcCnt = 0;
    int acnoCurrentSum = 0;
    int acnoEffcSum = 0;
    String tempGroupCode = "";
    String tempName = "";
    int loadBilCnt = 0;
    int loadAcnoCnt = 0;

    int arrayAcno = 0;
    int arrayEffc = 0;
    int arrayMax = 300000;
    String[] groupAcnoPSeqno = new String[arrayMax];
    String[] groupCorpno     = new String[arrayMax];

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data  = new buf1();
/***********************************************************************/
public int mainProcess(String[] args) 
{
 try 
   {
    // ====================================
    // 固定要做的
    dateTime();
    setConsoleMode("Y");
    javaProgram = this.getClass().getName();
    showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=["+args.length+"]");
 
    // 固定要做的
    if(!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
    // =====================================
    if(args.length > 3) {
       comc.errExit("Usage : CrdR066 [yyyymmdd] [seq_no] ", "");
      }
  
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
 
    hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
    if(comc.getSubString(hCallBatchSeqno,0,8).equals(comc.getSubString(comc.getECSHOME(),0,8)))
      { hCallBatchSeqno = "no-call"; 
      }
 
    String checkHome = comc.getECSHOME();
    if(hCallBatchSeqno.length() > 6) {
       if(comc.getSubString(hCallBatchSeqno,0,6).equals(comc.getSubString(checkHome,0,6))) 
         {
          comcr.hCallBatchSeqno = "no-call";
         }
      }

    comcr.hCallRProgramCode = javaProgram;
    hTempUser = "";
    if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        int recCnt = selectTable();
        hTempUser = getValue("user_id");
    }
    if (hTempUser.length() == 0) {
        hTempUser = comc.commGetUserID();
    }

    if (args.length >  0) {
        hBusiBusinessDate = "";
        if(args[0].length() == 8) {
           hBusiBusinessDate = args[0];
          } else {
           String errMsg = String.format("指定營業日[%s]", args[0]);
           comcr.errRtn(errMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
    }
    selectPtrBusinday(); 
    
    hLastDay = hEndDate;
    
    if (!hBusiBusinessDate.equals(hLastDay)) {
		showLogMessage("E", "", "報表日不為該月最後一天,不執行此程式");
		return 0;
    }

    initArray();

    selectCrdCard();

    comcr.insertPtrBatchRpt(lpar1);
//改為線上報表    
if(DEBUG==1) {
   String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
   filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
   comc.writeReport(filename, lpar1);
}
 
    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
    showLogMessage("I", "", comcr.hCallErrorDesc);
    if (comcr.hCallBatchSeqno.length() == 20)   comcr.callbatch(1, 0, 1); // 1: 結束

    finalProcess();
    return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
                         }
}
// ************************************************************************
void initArray() throws Exception
{
   for (int i = 0; i < arrayMax; i++)
       {
        groupAcnoPSeqno[i] = "";
        groupCorpno[i]     = "";
       }
}
// ************************************************************************
public int selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                            : hBusiBusinessDate;
   }

   sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "    , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "    , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd +="     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hBegDateBil = getValue("h_beg_date_bil");
      hEndDateBil = getValue("h_end_date_bil");
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate));
   return 0;
}
/***********************************************************************/
void selectCrdCard() throws Exception 
{
  String tmp      = "";
        
  sqlCmd =  " select a.card_no      ,a.current_code        , ";
  sqlCmd += " a.ori_issue_date      ,a.acno_p_seqno        , ";
  sqlCmd += " a.group_code          ,a.card_type           , ";
  sqlCmd += " a.bin_type            ,b.name                , ";
  sqlCmd += " a.p_seqno             ,a.corp_no             , ";
  sqlCmd += " a.last_consume_date   ";
  sqlCmd += " from ptr_group_card b, crd_card a ";
  sqlCmd += " where b.group_code = a.group_code ";
  sqlCmd += "  and b.card_type  = a.card_type  ";
  sqlCmd += "  and a.acct_type in ('03','06') ";
  sqlCmd += "  and a.current_code = '0' ";
  sqlCmd += " union ";
  sqlCmd += " select a.card_no      ,a.current_code        , ";
  sqlCmd += " a.ori_issue_date      ,a.acno_p_seqno        , ";
  sqlCmd += " a.group_code          ,a.card_type           , ";
  sqlCmd += " a.bin_type            ,b.name                , ";
  sqlCmd += " a.p_seqno             ,a.corp_no             , ";
  sqlCmd += " a.last_consume_date   ";
  sqlCmd += " from ptr_group_card b, crd_card a ";
  sqlCmd += " where b.group_code = a.group_code ";
  sqlCmd += "  and b.card_type  = a.card_type  ";
  sqlCmd += "  and a.acct_type in ('03','06') ";
  sqlCmd += "  and a.ori_issue_date between ? and ?  ";
  sqlCmd += " order by group_code, corp_no, bin_type ";
  setString(1 , hBegDate);
  setString(2 , hEndDate);
  
if(DEBUG==1) showLogMessage("I","","Read Main="+hBegDate+" , "+hEndDate);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardCardNo = getValue("card_no"     );
     cardCurrentCode = getValue("current_code");
     cardOriIssueDate = getValue("ori_issue_date"  );
     cardAcnoPSeqno = getValue("acno_p_seqno");
     cardGroupCode = getValue("group_code"  );
     cardCardType = getValue("card_type"   );
     cardBinType = getValue("bin_type"    );
     cardName = getValue("name"        );
     cardPSeqno = getValue("p_seqno");
     cardCorpno = getValue("corp_no");
     cardLastConsumeDate = getValue("last_consume_date");

     if(totCnt == 1 ||  
        (tempGroupCode.compareTo(cardGroupCode) != 0) )
       {
        if(totCnt == 1)    
          {  headFile(); }
        else writeFile();

        initData();
        initArray();

        arrayAcno = 0;  // 第1筆
        arrayEffc = 0;
  
        tempGroupCode = cardGroupCode;
        tempName = cardName;
       }

     if(DEBUG==1) showLogMessage("I","","Read card="+ cardCardNo +" G="+ cardGroupCode +" Corp="+ cardCorpno +" Cnt="+ totCnt);
     if(DEBUG==1) showLogMessage("I",""," 888 acno cnt="+ arrayAcno +" Corp="+ cardCorpno +" Load="+ loadAcnoCnt +" , acno effc="+ arrayEffc);

     if(totCnt % 5000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R066 Process 1 record=[%d]\n", totCnt));

     chkCorpno();

     switch (cardBinType)
       {
        case "V": visaRtn();    break;
        case "M": masterRtn();  break;
       }
    }

  showLogMessage("I",""," Read end="+ totCnt);
      
  tempGroupCode = cardGroupCode;
      
  writeFile();      
  tailFile();
}
/***********************************************************************/
void visaRtn() throws Exception
{

   currentVCnt++;
   if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
      comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
     { issueVCnt++; }
   
   if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
     { effcVCnt++; }

   return;
}
/***********************************************************************/
void masterRtn() throws Exception
{

   currentMCnt++;
   if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
      comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
     { issueMCnt++; }
   
   if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
     { effcMCnt++; }

   return;
}
/***********************************************************************/
void initData() throws Exception 
{
    issueVCnt = 0;
    issueMCnt = 0;
    currentVCnt = 0;
    currentMCnt = 0;
    effcVCnt = 0;
    effcMCnt = 0;
    acnoCurrentCnt = 0;
    acnoEffcCnt = 0;
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardCardNo = "";
     cardCurrentCode = "";
     cardOriIssueDate = "";
     cardAcnoPSeqno = "";
     cardGroupCode = "";
     cardCardType = "";
     cardBinType = "";
     cardName = "";
     cardPSeqno = "";
}
/***********************************************************************/
void headFile() throws Exception 
{
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信月卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM66     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "                                                 -- 當月新發卡卡數 --     -- 流通卡卡數 --        -- 有效卡卡數 --       -- 歸戶數 --";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = "信 用 卡 名 稱                           團代    VISA   MASTER   合計    VISA   MASTER   合計    VISA   MASTER   合計  流通戶數 有效戶數";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "======================================= ====== ======= ======= ======= ======= ======= ======= ======= ======= ======= ======== ========";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
        htail.filler01    = "    合  計 :";
        tmp = String.format("%7d", issueVAll);
        htail.issueVCnt = tmp;
        tmp = String.format("%7d", issueMAll);
        htail.issueMCnt = tmp;
        tmp = String.format("%7d", issueMAll + issueVAll);
        htail.issueSum = tmp;
        tmp = String.format("%7d", currentVAll);
        htail.currentVCnt = tmp;
        tmp = String.format("%7d", currentMAll);
        htail.currentMCnt = tmp;
        tmp = String.format("%7d", currentMAll + currentVAll);
        htail.currentSum = tmp;
        tmp = String.format("%7d", effcVAll);
        htail.effcVCnt = tmp;
        tmp = String.format("%7d", effcMAll);
        htail.effcMCnt = tmp;
        tmp = String.format("%7d", effcMAll + effcVAll);
        htail.effcSum = tmp;
        tmp = String.format("%8d", acnoCurrentSum);
        htail.acnoCurrentCnt = tmp;
        tmp = String.format("%8d", acnoEffcSum);
        htail.acnoEffcCnt = tmp;

        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", ""));
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", ""));

   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

//   buf = "備 註: １、本表為ＯＲＧ１０６下所有ＴＹＰＥ，排除ＴＹＰＥ為５９９、９９７、>９９８者";
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   buf = "       ２、流通卡為截至目前未停用之卡片，即控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ>、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   buf = "       ３、有效卡為最近６個月有消費紀錄，且控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ>、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void chkCorpno() throws Exception 
{	
   int tmpCnt = 0;
   for (int i = 0; i <= arrayAcno; i++) {
	   if(DEBUG_F==1) showLogMessage("I","","     888 i=["+i+"]"+groupCorpno[i]+","+cardCorpno+","+cardAcnoPSeqno);
        
	   if(cardCorpno.compareTo(groupCorpno[i]) == 0) {
           tmpCnt = 1;
           break;
       }      
   }

   if(DEBUG==1) showLogMessage("I","","    Chk Acno1="+arrayAcno+","+groupCorpno[arrayAcno]+","+groupCorpno[arrayAcno]+", Exist="+tmpCnt);
     
   if(tmpCnt == 0) {	   	           
	   groupCorpno[arrayAcno] = cardCorpno;
	   
	   if(cardCurrentCode.equals("0")) {
		   arrayAcno++;
	   }

       if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil)) {        	    	  
    	   arrayEffc++;        
       }     
   }

   if(DEBUG==1) showLogMessage("I","","    Chk Acno2="+arrayAcno+","+groupCorpno[arrayAcno]+","+loadAcnoCnt+", EFFC="+ arrayEffc);

   if(DEBUG_F==1) showLogMessage("I","","    888 =Acno["+arrayAcno+"]"+groupCorpno[arrayAcno]+",effc="+arrayEffc+", Load="+loadAcnoCnt);            
}
/***********************************************************************/
void writeFile() throws Exception 
{
        String tmp = "";

        acnoCurrentCnt = arrayAcno;
        acnoEffcCnt = arrayEffc;
if(DEBUG==1) showLogMessage("I",""," Write ACNO="+ acnoCurrentCnt +","+ arrayEffc);

        if(lineCnt1 > reportPageLine) {
           headFile();
          }

        data = null;
        data = new buf1();

        tmp = String.format("%s", comc.fixLeft(tempName, 40) );
        data.name        = tmp;
        data.groupCode = tempGroupCode;

        tmp = String.format("%7d", issueVCnt);
        data.issueVCnt = tmp;
        tmp = String.format("%7d", issueMCnt);
        data.issueMCnt = tmp;
        tmp = String.format("%7d", issueMCnt + issueVCnt);
        data.issueSum = tmp;
        tmp = String.format("%7d", currentVCnt);
        data.currentVCnt = tmp;
        tmp = String.format("%7d", currentMCnt);
        data.currentMCnt = tmp;
        tmp = String.format("%7d", currentMCnt + currentVCnt);
        data.currentSum = tmp;
        tmp = String.format("%7d", effcVCnt);
        data.effcVCnt = tmp;
        tmp = String.format("%7d", effcMCnt);
        data.effcMCnt = tmp;
        tmp = String.format("%7d", effcMCnt + effcVCnt);
        data.effcSum = tmp;
        tmp = String.format("%8d", acnoCurrentCnt);
        data.acnoCurrentCnt = tmp;
        tmp = String.format("%8d", acnoEffcCnt);
        data.acnoEffcCnt = tmp;

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;

        issueVAll = issueVAll + issueVCnt;
        issueMAll = issueMAll + issueMCnt;
        currentVAll = currentVAll + currentVCnt;
        currentMAll = currentMAll + currentMCnt;
        effcVAll = effcVAll + effcVCnt;
        effcMAll = effcMAll + effcMCnt;
        acnoCurrentSum = acnoCurrentSum + acnoCurrentCnt;
        acnoEffcSum = acnoEffcSum + acnoEffcCnt;

        return;
    }
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR066 proc = new CrdR066();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String groupCode;
        String issueVCnt;
        String issueMCnt;
        String issueSum;
        String currentVCnt;
        String currentMCnt;
        String currentSum;
        String effcVCnt;
        String effcMCnt;
        String effcSum;
        String acnoCurrentCnt;
        String acnoEffcCnt;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01     ,  46+1);
            rtn += fixLeft(issueVCnt,  7+1);
            rtn += fixLeft(issueMCnt,  7+1);
            rtn += fixLeft(issueSum,  7+1);
            rtn += fixLeft(currentVCnt,  7+1);
            rtn += fixLeft(currentMCnt,  7+1);
            rtn += fixLeft(currentSum,  7+1);
            rtn += fixLeft(effcVCnt,  7+1);
            rtn += fixLeft(effcMCnt,  7+1);
            rtn += fixLeft(effcSum,  7+1);
            rtn += fixLeft(acnoCurrentCnt,  8+1);
            rtn += fixLeft(acnoEffcCnt,  8+1);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String groupCode;
        String binType;
        String issueVCnt;
        String issueMCnt;
        String issueSum;
        String currentVCnt;
        String currentMCnt;
        String currentSum;
        String effcVCnt;
        String effcMCnt;
        String effcSum;
        String acnoCurrentCnt;
        String acnoEffcCnt;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(name         ,  40+1);
            rtn += fixLeft(groupCode,  5+1);
            rtn += fixLeft(issueVCnt,  7+1);
            rtn += fixLeft(issueMCnt,  7+1);
            rtn += fixLeft(issueSum,  7+1);
            rtn += fixLeft(currentVCnt,  7+1);
            rtn += fixLeft(currentMCnt,  7+1);
            rtn += fixLeft(currentSum,  7+1);
            rtn += fixLeft(effcVCnt,  7+1);
            rtn += fixLeft(effcMCnt,  7+1);
            rtn += fixLeft(effcSum,  7+1);
            rtn += fixLeft(acnoCurrentCnt,  8+1);
            rtn += fixLeft(acnoEffcCnt,  8+1);
 //         rtn += fixLeft(len          ,  1);
            return rtn;
        }

       
    }
String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)    spc += " ";
        if (str == null)                  str  = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
