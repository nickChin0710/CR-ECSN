/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/02/20 V1.01.01  Lai         program initial                            *
*  112/07/05 V1.01.02  Bo Yang     update by naming rule                      *
*  112/07/12 V1.01.03  Wilson      寫入報表紀錄檔                                                                                            *
*  112/07/17 V1.01.04  Wilson      刪除程式重複執行判斷                                                                                  *
*  112/07/21 V1.01.05  Wilson      指定一般消費科目                                                                                         *
*  112/08/22 V1.01.06  Wilson      有效卡改為判斷最後消費日期                                                                      *
*  112/10/26 V1.01.07  Wilson      增加處理AI501                                 *
*  112/11/08 V1.01.08  Wilson      日期減一天                                                                                                    *
*  112/12/19 V1.01.09  Wilson      調整累計迄日                                                                                                *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class CrdR052 extends AccessDAO {
    private final String PROGNAME = "全部信用卡卡量彙總表  112/12/19 V1.01.09";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 34;
    String prgmId    = "CrdR052";

    String rptIdR1 = "CRD52";
    String rptName1  = "全部信用卡卡量彙總表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hLastDateOfMonth = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastMonth =  "";

    String cardCardNo = "";
    String cardCurrentCode = "";
    String cardOriIssueDate = "";
    String cardOppostDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinNo = "";
    String cardName = "";
    String cardLastConsumeDate = "";
    int issueCnt = 0;
    int oppostCnt = 0;
    int currentCnt = 0;
    int effcCnt = 0;
    int issueAll = 0;
    int oppostAll = 0;
    int currentAll = 0;
    int effcAll = 0;
    String tempGroupCode = "";
    String tempBinNo = "";
    String tempName = "";

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";
    
    int cardIssueTotal = 0;
    int tmpSumField1 = 0;

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
       comc.errExit("Usage : CrdR052 [yyyymmdd] [seq_no] ", "");
      }
  
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
    
    rptData = new JSONObject();
 
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
    
    hLastDateOfMonth = comm.lastdateOfmonth(hBusiBusinessDate);

    selectCrdCard();
    
    if (hBusiBusinessDate.equals(hLastDateOfMonth)) {
    	processAi501();
    }
 
//改為線上報表
//    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
//    filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
//    comc.writeReport(filename, lpar1);
    comcr.insertPtrBatchRpt(lpar1);
 
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
public int selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                            : hBusiBusinessDate;       
   }

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymmdd') h_beg_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd +="      , to_char(to_date(?,'yyyymmdd'),'yyyymmdd') h_end_date ";
   sqlCmd +="      , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm') h_last_month ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);
   setString(5, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
	  hBegDateBil = getValue("h_beg_date_bil");
	  hEndDateBil = getValue("h_end_date_bil");
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
      hLastMonth = getValue("h_last_month");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate, hLastMonth));
   return 0;
}
/***********************************************************************/
void selectCrdCard() throws Exception 
{
  String tmp  = "";
  int    cnt1 = 0;
        
  sqlCmd =  " select a.card_no      ,a.current_code        , ";
  sqlCmd += " a.ori_issue_date      ,a.oppost_date         , ";
  sqlCmd += " a.group_code          ,a.card_type           , ";
  sqlCmd += " a.bin_no              ,b.name                , ";
  sqlCmd += " a.last_consume_date                            ";
  sqlCmd += " from ptr_group_card b, crd_card a ";
  sqlCmd += " where b.card_type  = a.card_type ";
  sqlCmd += "   and b.group_code = a.group_code  ";
  sqlCmd += "   and ((a.current_code = '0') or (a.ori_issue_date between ? and ?) or (a.oppost_date between ? and ?)) ";
  sqlCmd += " order by group_code , bin_no  ";
  
  setString(1, hBegDate);
  setString(2, hEndDate);
  setString(3, hBegDate);
  setString(4, hEndDate);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardCardNo = getValue("card_no");
     cardCurrentCode = getValue("current_code");
     cardOriIssueDate = getValue("ori_issue_date");
     cardOppostDate = getValue("oppost_date");
     cardGroupCode = getValue("group_code");
     cardCardType = getValue("card_type");
     cardBinNo = getValue("bin_no");
     cardName = getValue("name");
     cardLastConsumeDate = getValue("last_consume_date");

     if(totCnt == 1 ||  
        (tempGroupCode.compareTo(cardGroupCode) != 0 || 
         tempBinNo.compareTo(cardBinNo) != 0) )
       {
        if(totCnt == 1)    
          {  headFile(); }
        else writeFile();

        initData();
        tempGroupCode = cardGroupCode;
        tempBinNo = cardBinNo;
        tempName = cardName;
       }
  // if(DEBUG==1) showLogMessage("I","","Read card="+card_card_no+" G="+card_group_code+" NAME="+card_name+" Cnt="+tot_cnt);

     if(totCnt % 5000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R052 Process 1 record=[%d]\n", totCnt));

     if(cardOppostDate.length() > 0 && comc.getSubString(cardOppostDate, 0, 6).equals(comc.getSubString(hBusiBusinessDate, 0, 6))) {
    	 oppostCnt++;
    	 oppostAll++;
     }
     
     if(comc.getSubString(cardOriIssueDate, 0, 6).equals(comc.getSubString(hBusiBusinessDate, 0, 6))) {
    	 issueCnt++;
    	 issueAll++;
     }

     if(cardCurrentCode.equals("0") || cardCurrentCode.length() == 0) {
    	 currentCnt++;
    	 currentAll++;  
    	         
    	 if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil)) {
	    	 effcCnt++;
	    	 effcAll++;  
         }   	 
      }
     
//   if(DEBUG==1) showLogMessage("I","","  BILL cnt="+card_card_no+" Cnt="+cnt1+",iss="+issue_cnt+",all="+issue_all);
    }

  showLogMessage("I",""," Read end="+ totCnt +",iss="+ issueCnt +",all="+ issueAll);
  if(totCnt > 0)
    {
      tempGroupCode = cardGroupCode;
      tempBinNo = cardBinNo;
      tempName = cardName;
      writeFile();
      tailFile();
    }

}
/***********************************************************************/
void initData() throws Exception 
{
    issueCnt = 0;
    oppostCnt = 0;
    currentCnt = 0;
    effcCnt = 0;
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardCardNo = "";
     cardCurrentCode = "";
     cardOriIssueDate = "";
     cardOppostDate = "";
     cardGroupCode = "";
     cardCardType = "";
     cardBinNo = "";
     cardName = "";
     cardLastConsumeDate = "";
}
/***********************************************************************/
void headFile() throws Exception 
{
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 51);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRD52     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "累計起訖日: " + hBegDate +" - " + hEndDate;
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = "卡別 BIN-NO        當月發卡數   當月停卡數   流通卡數   有效卡數   卡片名稱 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "==== ============= =========- ---------- ---------- ---------- ======================= ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
        htail.filler01    = "     ";
        htail.fileValue = "合  計 :";
        tmp = String.format("%11d", issueAll);
        htail.issueAll = tmp;
        tmp = String.format("%11d", oppostAll);
        htail.oppostAll = tmp;
        tmp = String.format("%11d", currentAll);
        htail.currentAll = tmp;
        tmp = String.format("%11d", effcAll);
        htail.effcAll = tmp;

        buf = htail.allText();

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeFile() throws Exception 
{
        String tmp = "";

        if(lineCnt1 > reportPageLine) {
           headFile();
          }

        data = null;
        data = new buf1();

        data.groupCode = tempGroupCode;
        data.binNo = tempBinNo;
        tmp = String.format("%11d", issueCnt);
        data.issueCnt = tmp;
        tmp = String.format("%11d", oppostCnt);
        data.oppostCnt = tmp;
        tmp = String.format("%11d", currentCnt);
        data.currentCnt = tmp;
        tmp = String.format("%11d", effcCnt);
        data.effcCnt = tmp;

        data.filler01    = " ";
        tmp = String.format("%s", comc.fixLeft(tempName, 40) );
        data.name        = tmp;

if(DEBUG == 1) showLogMessage("I", "", "   Name="+ data.name +","+ tempName);

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;        

        return;
    }

/***********************************************************************/
void processAi501() throws Exception {

	selectMisReportData();
	
	cardIssueTotal = tmpSumField1 + issueAll;

	rptData.put("THIS_MONTH_CARD_ISSUE_CNT", issueAll);
	rptData.put("THIS_MONTH_CARD_OPPOST_CNT", oppostAll);
	rptData.put("THIS_MONTH_CARD_EFFC_CNT", effcAll);
	
	insertMisReportData();
}

/***********************************************************************/
void selectMisReportData() throws Exception {
        sqlCmd = "select sum_field1 ";
        sqlCmd += " from mis_report_data ";
        sqlCmd += " where data_month = ?   ";
        sqlCmd += "   and data_from = 'CRD52' ";
        setString(1, hLastMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tmpSumField1 = getValueInt("sum_field1");
        }
}

/***********************************************************************/
void insertMisReportData() throws Exception {
	
	//重跑時要先刪除上一次產生的資料
	deleteExistRptRecord();
	commitDataBase();
	
    setValue("DATA_MONTH", comc.getSubString(hBusiBusinessDate,0,6));
    setValue("DATA_FROM", "CRD52");
    setValue("DATA_DATE", hBusiBusinessDate);
    setValueDouble("SUM_FIELD1", cardIssueTotal);
    setValueDouble("SUM_FIELD2", 0);
    setValueDouble("SUM_FIELD3", 0);
    setValueDouble("SUM_FIELD4", 0);
    setValueDouble("SUM_FIELD5", 0);
    setValue("DATA_CONTENT", rptData.toString());
    setValue("MOD_TIME", sysDate+sysTime);
    setValue("MOD_PGM", javaProgram);

    daoTable = "mis_report_data";
    insertTable();    
}

/************************************************************************/
void deleteExistRptRecord() throws Exception {
	
	daoTable  = " mis_report_data ";
	whereStr  = " where 1=1 "; 
	whereStr += " and data_month = ? ";
	whereStr += " and data_from = 'CRD52' ";
	
	setString(1, comc.getSubString(hBusiBusinessDate,0,6));

	deleteTable();
	
}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR052 proc = new CrdR052();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String fileValue;
        String issueAll;
        String oppostAll;
        String currentAll;
        String effcAll;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01   , 5);
            rtn += fixLeft(fileValue,13);
            rtn += fixLeft(issueAll,11);
            rtn += fixLeft(oppostAll,11);
            rtn += fixLeft(currentAll,11);
            rtn += fixLeft(effcAll,11);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String groupCode;
        String binNo;
        String issueCnt;
        String oppostCnt;
        String currentCnt;
        String effcCnt;
        String filler01;
        String name;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(groupCode,  4+1);
            rtn += fixLeft(binNo,  12+1);
            rtn += fixLeft(issueCnt,  10+1);
            rtn += fixLeft(oppostCnt,  10+1);
            rtn += fixLeft(currentCnt,  10+1);
            rtn += fixLeft(effcCnt,  10+1);
            rtn += fixLeft(filler01     ,  1);
            rtn += fixLeft(name         ,  40);
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
