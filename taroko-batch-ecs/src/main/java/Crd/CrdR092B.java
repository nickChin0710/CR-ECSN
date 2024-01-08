/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/12/20 V1.01.01  Wilson       program initial                           *
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

public class CrdR092B extends AccessDAO {
    private final String PROGNAME = "國際信用卡鍵檔資料上傳處理回覆明細表(COMBO卡)  112/12/20 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 31;
    String prgmId    = "CrdR092B";

    String rptIdR1 = "CRD92B";
    String rptName1  = "國際信用卡鍵檔資料上傳處理回覆明細表(COMBO卡)";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hRunBatchNo = "";
    String hLastDateOfMonth = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastMonth =  "";

    String emapApplyNo = "";
    String emapApplyNo2 = "";
    String emapSupFlag = "";
    String emapChiName = "";
    String emapApplyId = "";
    String emapGroupCode = "";
    String emapCardNo = "";
    String emapPmId = "";
    int emapCreditLmt = 0;
    double emapRevolveIntRateYear = 0.0;
    String emapPoliceNo1 = "";
    String emapPoliceNo2 = "";
    String emapCheckCode = "";
    String emapInMainDate = "";
    int uploadCnt = 0;
    int caseCnt = 0;
    int cardCnt = 0;
    int sucCnt = 0;
    int failCnt = 0;
    
    String lastApplyNo = "";
    String lastApplyNo2 = "";

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";
    
    int cardIssueTotal = 0;
    int tmpSumField1 = 0;

    buft htail = new buft();
    buf1 data  = new buf1();
/***********************************************************************/
public int mainProcess(String[] args) {
 try {
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
       comc.errExit("Usage : CrdR092B [yyyymmdd] [seq_no] ", "");
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
    
    selectCrdEmapTmp();
     
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
  } catch (Exception ex) { 
	  expMethod = "mainProcess"; expHandle(ex); return exceptExit;                       
  }
}
// ************************************************************************
public int selectPtrBusinday() throws Exception {

   sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date"): hBusiBusinessDate;
       hRunBatchNo = comc.getSubString(hBusiBusinessDate, 2, 8);
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
void selectCrdEmapTmp() throws Exception {
  String tmp  = "";
     	  	 
  headFile(); 
        
  sqlCmd =  " select a.apply_no, ";
  sqlCmd += "        a.sup_flag, ";
  sqlCmd += "        a.chi_name, ";
  sqlCmd += "        a.apply_id, ";
  sqlCmd += "        a.group_code, ";
  sqlCmd += "        a.card_no, ";
  sqlCmd += "        a.pm_id, ";
  sqlCmd += "        a.credit_lmt, ";
  sqlCmd += "        a.revolve_int_rate_year, ";
  sqlCmd += "        a.police_no1, ";
  sqlCmd += "        a.police_no2, ";
  sqlCmd += "        decode(a.check_code,'000','00',a.check_code) as check_code, ";
  sqlCmd += "        '' as in_main_date ";
  sqlCmd += " from crd_emap_tmp a, ptr_prod_type b ";
  sqlCmd += " where a.group_code = b.group_code ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and a.combo_indicator = 'Y' ";
  sqlCmd += "   and a.source = '1' ";
  sqlCmd += "   and substring(a.batchno,1,6) = ? ";
  sqlCmd += " union ";
  sqlCmd += " select apply_no, ";
  sqlCmd += "        sup_flag, ";
  sqlCmd += "        chi_name, ";
  sqlCmd += "        apply_id, ";
  sqlCmd += "        group_code, ";
  sqlCmd += "        card_no, ";
  sqlCmd += "        pm_id, ";
  sqlCmd += "        credit_lmt, ";
  sqlCmd += "        revolve_int_rate_year, ";
  sqlCmd += "        police_no1, ";
  sqlCmd += "        police_no2, ";
  sqlCmd += "        check_code, ";
  sqlCmd += "        in_main_date ";
  sqlCmd += " from crd_emboss ";
  sqlCmd += " where acct_type = '01' ";
  sqlCmd += "   and combo_indicator = 'Y' ";
  sqlCmd += "   and emboss_source = '1' ";
  sqlCmd += "   and substring(source_batchno,1,6) = ? ";
  sqlCmd += "   order by apply_no,sup_flag ";
  
  setString(1, hRunBatchNo);
  setString(2, hRunBatchNo);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;
     cardCnt++;

     emapApplyNo = getValue("apply_no");
     emapApplyNo2 = comc.getSubString(emapApplyNo, 0, 11);
     emapSupFlag = getValue("sup_flag");
     emapChiName = getValue("chi_name");
     emapApplyId = getValue("apply_id");
     emapGroupCode = getValue("group_code");
     emapCardNo = getValue("card_no");
     emapPmId = getValue("pm_id");
     emapCreditLmt = getValueInt("credit_lmt");
     emapRevolveIntRateYear = getValueDouble("revolve_int_rate_year");
     emapPoliceNo1 = getValue("police_no1");
     emapPoliceNo2 = getValue("police_no2");
     emapCheckCode = getValue("check_code");
     emapInMainDate = getValue("in_main_date");
    	 
     if(emapInMainDate.length() > 0) {
    	 emapCheckCode = "000";
     }
     
     if(!lastApplyNo.equals(emapApplyNo)) {
    	 uploadCnt++;
     }
     
     if(!lastApplyNo2.equals(emapApplyNo2)) {
    	 caseCnt++;
     }
     
     if(emapCheckCode.equals("000")) {
    	 sucCnt++;
     }
     else {
    	 failCnt++;
     }
              
     writeFile();
     
     lastApplyNo = emapApplyNo;
     lastApplyNo2 = emapApplyNo2;
  }
    	
  writeFile2();  	  
  tailFile();

}
/***********************************************************************/
void initRtn() throws Exception {
    emapApplyNo = "";
    emapSupFlag = "";
    emapChiName = "";
    emapApplyId = "";
    emapGroupCode = "";
    emapCardNo = "";
    emapPmId = "";
    emapCreditLmt = 0;
    emapRevolveIntRateYear = 0.0;
    emapPoliceNo1 = "";
    emapPoliceNo2 = "";
    emapCheckCode = "";
    emapInMainDate = "";
}
/***********************************************************************/
void headFile() throws Exception {
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > reportPageLine)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 50);
        buf = comcr.insertStr(buf, "保存年限: 一年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRD92B     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "序號   案件編號   正附卡  持卡人      持卡人      團體                       正卡人      核准  年利率  鍵檔人員 鍵檔人員 資料處理 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        
        buf = "";
        buf = "                  註記    中文姓名    ＩＤ    　　    代號        卡號           ＩＤ        額度          代號一   代號二   狀    態 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "==== ============ ====== ========== ============ ====== ================== ============ ====== ======  ======== ======== ======== ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException {
	 
	    buf = "";
	    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

	    buf = "";
	    buf = "上傳筆數: " + uploadCnt + "筆     案件合計: " + caseCnt + "筆     卡數合計: " + cardCnt + "卡     成功: " + sucCnt + "卡     失敗: " + failCnt + "卡" ;
	    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeFile() throws Exception {
        String tmp = "";

        if(lineCnt1 > reportPageLine) {
           headFile();
          }

        data = null;
        data = new buf1();
                                
        data.number = String.format("%d", totCnt);
        data.applyNo = emapApplyNo;
        data.supFlag = emapSupFlag;
        data.chiName = emapChiName;
        data.applyId = emapApplyId;
        data.groupCode = emapGroupCode;
        data.cardNo = emapCardNo;
        data.pmId = emapPmId;
        data.creditLmt = String.format("%d", emapCreditLmt/10000);
        data.revolveIntRateYear = String.format("%04d", (int)Math.round(emapRevolveIntRateYear*100));
        data.policeNo1 = emapPoliceNo1;
        data.policeNo2 = emapPoliceNo2;
        data.checkCode = emapCheckCode;

if(DEBUG == 1) showLogMessage("I", "", "   applyNo="+ data.applyNo);

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;        
        
        if(lineCnt1 > reportPageLine) {
        	writeFile2();
        }

        return;
    }

/***********************************************************************/
void writeFile2() throws Exception  {
	
    buf = "";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

    buf = "";
    buf = "備註: (1)正附卡註記   0=正卡  1=附卡 ";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
    buf = "";
    buf = "      (2)資料處理狀態   000=成功  其餘=失敗 ";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));


}
/***********************************************************************/
public static void main(String[] args) throws Exception {
       CrdR092B proc = new CrdR092B();
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
  class buf1 {
	  
	    String number = "";
	    String applyNo = "";
	    String supFlag = "";
	    String chiName = "";
	    String applyId = "";
	    String groupCode = "";
	    String cardNo = "";
	    String pmId = "";
	    String creditLmt = "";
	    String revolveIntRateYear = "";
	    String policeNo1 = "";
	    String policeNo2 = "";
	    String checkCode = "";
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(len,  1);
            rtn += fixLeft(number,  3+1);
            rtn += fixLeft(applyNo,  14+1);
            rtn += fixLeft(supFlag,  4+1);
            rtn += fixLeft(chiName,  11+1);
            rtn += fixLeft(applyId,  12+1);
            rtn += fixLeft(groupCode,  6+1);
            rtn += fixLeft(cardNo,  18+1);
            rtn += fixLeft(pmId,  12+1);
            rtn += comc.fixRight(creditLmt,  2+1);
            rtn += fixLeft(len,  4);
            rtn += fixLeft(revolveIntRateYear,  7+1);
            rtn += fixLeft(policeNo1,  8+1);
            rtn += fixLeft(policeNo2,  9+1);
            rtn += fixLeft(checkCode,  8+1);
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
