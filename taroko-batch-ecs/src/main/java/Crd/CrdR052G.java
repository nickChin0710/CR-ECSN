/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/29 V1.00.01  Wilson       program initial                           *
*  112/11/08 V1.00.02  Wilson       日期減一天                                                                                                 *
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

import Crd.CrdR065.buft;

public class CrdR052G extends AccessDAO {
    private final String PROGNAME = "EPAY&台灣PAY綁卡數統計表  112/11/08 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 34;
    String prgmId    = "CrdR052G";

    String rptIdR1 = "CRD52G";
    String rptName1  = "EPAY&台灣PAY綁卡數統計表";
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

    String cardCardNo = "";
    String cardStatusCode = "";
    String cardCrtDate = "";
    String cardChangeDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinNo = "";
    String cardName = "";
    int epayCardCnt = 0;
    int hceCardCnt = 0;
    int mergeIdnoCnt = 0;
    int hceCardUseCnt = 0;

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
       comc.errExit("Usage : CrdR052G [yyyymmdd] [seq_no] ", "");
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
    
	initData();
	
	//合庫EPAY綁卡數
    epayCardCnt = selectEpayCard();
    showLogMessage("I","","Read END selectEpayCard ="+ epayCardCnt);
	
	//台灣PAY綁卡數
    hceCardCnt = selectHceCard();
    showLogMessage("I","","Read END selectHceCard ="+ hceCardCnt);
    
	//合庫EPAY+台灣PAY合併歸戶綁卡數
    mergeIdnoCnt = selectMergeIdno();
    showLogMessage("I","","Read END selectMergeIdno ="+ mergeIdnoCnt);   
    
	//台灣PAY近1年有使用之卡數
    hceCardUseCnt = selectHceCardUse();
    showLogMessage("I","","Read END selectHceCardUse ="+ hceCardUseCnt);

    headFile();
    writeFile();
    tailFile();
 
//改為線上報表
//    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
//    filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
//    comc.writeReport(filename, lpar1);
    comcr.insertPtrBatchRpt(lpar1);
 
    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束";
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

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-12),'yyyymmdd') h_beg_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
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
void initData() throws Exception 
{
	epayCardCnt = 0;
	hceCardCnt = 0;
	mergeIdnoCnt = 0;
	hceCardUseCnt = 0;

}
/***********************************************************************/
int selectEpayCard() throws Exception 
{	
  sqlCmd = " select count(*) as tpan_cnt ";
  sqlCmd += "  from epay_card ";
  int recCnt = selectTable();

  return  getValueInt("tpan_cnt");
}
/***********************************************************************/
int selectHceCard() throws Exception 
{	
  sqlCmd = " select count(*) as tpan_cnt ";
  sqlCmd += "  from hce_card ";
  int recCnt = selectTable();

  return  getValueInt("tpan_cnt");
}
/***********************************************************************/
int selectMergeIdno() throws Exception 
{	
  sqlCmd = " select count(*) as tpan_cnt ";
  sqlCmd += "  from crd_card ";
  sqlCmd += "  where card_no in (select card_no from epay_card) or card_no in (select card_no from hce_card) ";
  int recCnt = selectTable();

  return  getValueInt("tpan_cnt");
}
/***********************************************************************/
int selectHceCardUse() throws Exception 
{	
	sqlCmd = "	select count(*) as tpan_cnt "; 
	sqlCmd += "   from crd_card ";
	sqlCmd += "  where card_no in (select ecs_real_card_no "; 
	sqlCmd += "                      from bil_fiscdtl ";
	sqlCmd += "                     where ecs_v_card_no in (select v_card_no from hce_card) ";
	sqlCmd += "                       and ecs_acct_code in ('BL','ID','IT','AO','OT') ";
	sqlCmd += "                       and batch_date between ? and ?) ";
	setString(1, hBegDateBil);
	setString(2, hEndDateBil);
  int recCnt = selectTable();

  return  getValueInt("tpan_cnt");
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
        buf = comcr.insertStr(buf, "報表代號: CRD52G      科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = " 合庫EPAY綁卡數   台灣PAY綁卡數  合庫EPAY+台灣PAY合併歸戶綁卡數   台灣PAY近1年有使用之卡數 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "================ =============== ================================ ==========================";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
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

        tmp = String.format("%11d", epayCardCnt);
        data.epayCardCnt = tmp;
        tmp = String.format("%16d", hceCardCnt);
        data.hceCardCnt = tmp;
        tmp = String.format("%25d", mergeIdnoCnt);
        data.mergeIdnoCnt = tmp;
        tmp = String.format("%29d", hceCardUseCnt);
        data.hceCardUseCnt = tmp;

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;

        return;
    }
/************************************************************************/
void tailFile() throws UnsupportedEncodingException {
  buf = "";
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
  
  buf = "";
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

  htail.fileValue = "備註: ";
  buf = htail.allText();
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
  
  htail.fileValue = "(1)合庫EPAY綁卡數: 該客戶有3張信用卡，有2張有申請EAPY --> 綁卡數=2 ";
  buf = htail.allText();
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
  
  htail.fileValue = "(2)台灣PAY綁卡數: 該客戶有3張信用卡，有2張有申請台灣PAY且其中1張有綁在2個裝置(共3張虛擬卡) --> 綁卡數=3 ";
  buf = htail.allText();
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
  
  htail.fileValue = "(3)合庫EPAY+台灣PAY合併歸戶綁卡數: 該客戶有3張信用卡，有2張有申請EPAY或台灣PAY --> 綁卡數=2 ";
  buf = htail.allText();
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

  htail.fileValue = "(4)台灣PAY近1年有使用之卡數: 該客戶有3張信用卡，有2張有申請台灣PAY且其中1張有綁在2個裝置(共3張虛擬卡)，3張虛擬卡都有消費 --> 有使用卡數=2 ";
  buf = htail.allText();
  lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR052G proc = new CrdR052G();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
class buft 
{
    String fileValue;
    String len;

    String allText() throws UnsupportedEncodingException {
        String rtn = "";
        rtn += fixLeft(fileValue,140);
//      rtn += fixLeft(len, 1);
        return rtn;
    }
    
}
  class buf1 
    {
        String epayCardCnt;
        String hceCardCnt;
        String mergeIdnoCnt;
        String hceCardUseCnt;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";

            rtn += fixLeft(epayCardCnt, 10+1);
            rtn += fixLeft(hceCardCnt,  15+1);
            rtn += fixLeft(mergeIdnoCnt, 24+1);
            rtn += fixLeft(hceCardUseCnt,  28+1);
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
