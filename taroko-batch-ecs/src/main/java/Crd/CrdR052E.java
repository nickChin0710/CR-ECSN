/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/01 V1.01.01  Castor       program initial                           *                                                            *
*  112/08/31 V1.01.02  Wilson       改讀bil_fiscdtl                            *
*  112/11/08 V1.01.03  Wilson       日期減一天                                                                                                 *
*  112/11/14 V1.01.04  Wilson       讀取oempay主檔全部資料                                                                     *
*  112/12/19 V1.01.05  Wilson       調整累計迄日                                                                                             *
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

public class CrdR052E extends AccessDAO {
    private final String PROGNAME = "Google Pay下載交易情形表   112/12/19 V1.01.05";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 34;
    String prgmId    = "CrdR052E";

    String rptIdR1 = "CRD52E";
    String rptName1  = "Google Pay下載交易情形表 ";
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

    String cardVCardNo = "";
    String cardStatusCode = "";
    String cardCrtDate = "";
    String cardChangeDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinNo = "";
    String cardName = "";
    int loadBilEffcCnt = 0;
    int loadBilPurchaseCnt = 0;
    int crtCnt = 0;
    int changeCnt = 0;
    int statusCnt = 0;
    int effcCnt = 0;
    int purchasecnt = 0;
    int pamt = 0;
    int purchaseamt = 0;
    int sumPurchaseCnt = 0;
    int sumPurchaseAmt = 0;
    int crtAll = 0;
    int changeAll = 0;
    int statusAll = 0;
    int effcAll = 0;
    int purchasecntAll = 0;
    int purchaseamtAll = 0;
    String tempGroupCode = "";
    String tempBinNo = "";
    String tempName = "";

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
       comc.errExit("Usage : CrdR052E [yyyymmdd] [seq_no] ", "");
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
    
    loadBilEffc();
    
    loadBilPurchase();

    selectCrdCard();
 
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

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymmdd') h_beg_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymmdd') h_end_date ";
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
void loadBilEffc() throws Exception {
    extendField = "effc.";
    selectSQL   = "card_no "; 
    daoTable    = "bil_fiscdtl ";
    whereStr    = "where batch_date between ? and ? "
                + "  and ecs_acct_code  in ('BL','IT','ID','AO','OT') "
                + "  and card_no in ( select v_card_no from oempay_card) ";
    	
    setString(1, hBegDateBil);           
    	
    showLogMessage("I","","  get bil_fiscdtl_effc 1 date_fm="+hBegDateBil);
    
    setString(2, hEndDateBil);

    showLogMessage("I","","  get bil_fiscdtl_effc 2 date_to="+hEndDateBil);
   
    int n = loadTable();
    setLoadData("effc.card_no");      // set key

    showLogMessage("I", "", "Load bil_fiscdtl_effc end Count: [" + n + "]" + hBegDateBil + "," + hEndDateBil);
}
// ************************************************************************
void loadBilPurchase() throws Exception {
    extendField = "purchase.";
    selectSQL   = "count(*) as purchase_cnt, " 
    		    + "card_no, "
                + "sum(decode(ecs_sign_code,'+',dest_amt,dest_amt*-1)) as purchase_amt ";
    daoTable    = "bil_fiscdtl ";
    whereStr    = "where batch_date between ? and ? "
                + "  and ecs_acct_code in ('BL','IT','ID','AO','OT') "
                + "  and card_no in ( select v_card_no from oempay_card) "
                + "  group by card_no ";
    	
    setString(1, hBegDate);           
    	
    showLogMessage("I","","  get bil_fiscdtl_purchase 1 date_fm="+hBegDate);
    
    setString(2, hEndDate);

    showLogMessage("I","","  get bil_fiscdtl_purchase 2 date_to="+hEndDateBil);
   
    int n = loadTable();
    setLoadData("purchase.card_no");      // set key

    showLogMessage("I", "", "Load bil_fiscdtl_purchase end Count: [" + n + "]" + hBegDate + "," + hEndDate);
}
// ************************************************************************
void selectCrdCard() throws Exception 
{
  String tmp  = "";
  int    cnt1 = 0;
  int    cnt2 = 0;
 
        
  selectSQL = " a.v_card_no as card_no    ,b.group_code        , "
            + " a.crt_date                ,a.change_date         , "
            + " a.status_code             , "
            + " b.bin_no              ,c.name                  ";
  daoTable = " oempay_card a,crd_card b ,ptr_group_card c";
  whereStr = "where a.card_no  = b.card_no "
           + "  and  b.group_code = c.group_code  "
           + "  and  b.card_type = c.card_type  "
//           + "  and ((a.status_code = '0') or (a.crt_date between ? and ?) or (a.change_date between ? and ?)) "
           + "order by b.group_code  ";
  
//  setString(1, hBegDate);
//  setString(2, hEndDate);
//  setString(3, hBegDate);
//  setString(4, hEndDate);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardVCardNo = getValue("card_no");
     cardStatusCode = getValue("status_code");
     cardCrtDate = getValue("crt_date");
     cardChangeDate = getValue("change_date");
     cardGroupCode = getValue("group_code");
     cardCardType = getValue("card_type");
     cardBinNo = getValue("bin_no");
     cardName = getValue("name");

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
        showLogMessage("I","",String.format("R052B Process 1 record=[%d]\n", totCnt));

     if(cardChangeDate.length() > 0 && comc.getSubString(cardChangeDate, 0, 6).equals(comc.getSubString(hBusiBusinessDate, 0, 6))) {
    	 changeCnt++;
    	 changeAll++;
     }
     
     if(comc.getSubString(cardCrtDate, 0, 6).equals(comc.getSubString(hBusiBusinessDate, 0, 6))) {
    	 crtCnt++;
    	 crtAll++;
     }

     if(cardStatusCode.equals("0") || cardStatusCode.length() == 0) {
    	 statusCnt++;
    	 statusAll++;  
    	 
         loadBilEffcCnt = 0;
         setValue("effc.card_no", cardVCardNo);
         loadBilEffcCnt = getLoadData("effc.card_no");
    	 
    	 if(loadBilEffcCnt > 0) {
	    	 effcCnt++;
	    	 effcAll++;  
	     }     	     	       
     }
     
	 loadBilPurchaseCnt = 0;
     setValue("purchase.card_no", cardVCardNo);
     loadBilPurchaseCnt = getLoadData("purchase.card_no");
     sumPurchaseCnt = getValueInt("purchase.purchase_cnt");
     sumPurchaseAmt = getValueInt("purchase.purchase_amt");
	    		 
     if(loadBilPurchaseCnt > 0) {
         purchasecnt = purchasecnt + sumPurchaseCnt;    		 
         purchasecntAll = purchasecntAll + sumPurchaseCnt;   		 
         purchaseamt = purchaseamt + sumPurchaseAmt;      		 
         purchaseamtAll = purchaseamtAll + sumPurchaseAmt;	  
     }          	          
     
//   if(DEBUG==1) showLogMessage("I","","  BILL cnt="+card_card_no+" Cnt="+cnt1+",iss="+issue_cnt+",all="+issue_all);  
  }

  showLogMessage("I",""," Read end="+ totCnt +",iss="+ crtCnt +",all="+ crtAll);
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
    crtCnt = 0;
    changeCnt = 0;
    statusCnt = 0;
    effcCnt = 0;
    purchasecnt = 0;
    purchaseamt = 0;
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardVCardNo = "";
     cardStatusCode = "";
     cardCrtDate = "";
     cardChangeDate = "";
     cardGroupCode = "";
     cardCardType = "";
     cardBinNo = "";
     cardName = "";
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
        buf = comcr.insertStr(buf, "報表代號: CRD52E      科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "累計起訖日: " + hBegDate +" - " + hEndDate;
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = "團代 BIN-NO        當月發卡數 當月停卡數   流通卡數   有效卡數   簽帳筆數   簽帳金額 卡片名稱 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "==== ============= ---------- ---------- ---------- ---------- ---------- ---------- ============================ ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
        htail.filler01    = "     ";
        htail.fileValue = "合  計 :";
        tmp = String.format("%11d", crtAll);
        htail.issueAll = tmp;
        tmp = String.format("%11d", changeAll);
        htail.oppostAll = tmp;
        tmp = String.format("%11d", statusAll);
        htail.currentAll = tmp;
        tmp = String.format("%11d", effcAll);
        htail.effcAll = tmp;
        tmp = String.format("%11d", purchasecntAll);
        htail.purchasecntAll = tmp;

        tmp = String.format("%11d", purchaseamtAll);
        htail.purchaseamtAll = tmp;

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
        tmp = String.format("%11d", crtCnt);
        data.crtCnt = tmp;
        tmp = String.format("%11d", changeCnt);
        data.changeCnt = tmp;
        tmp = String.format("%11d", statusCnt);
        data.statusCnt = tmp;
        tmp = String.format("%11d", effcCnt);
        data.effcCnt = tmp;
        tmp = String.format("%11d", purchasecnt);
        data.purchasecnt = tmp;
        tmp = String.format("%11d", purchaseamt);
        data.purchaseamt = tmp;

        data.filler01    = " ";
        tmp = String.format("%s", comc.fixLeft(tempName, 40) );
        data.name        = tmp;

if(DEBUG == 1) showLogMessage("I", "", "   Name="+ data.name +","+ tempName);

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;

        return;
    }
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR052E proc = new CrdR052E();
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
        String purchasecntAll;
        String purchaseamtAll;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01   , 5);
            rtn += fixLeft(fileValue,13);
            rtn += fixLeft(issueAll,11);
            rtn += fixLeft(oppostAll,11);
            rtn += fixLeft(currentAll,11);
            rtn += fixLeft(effcAll,11);
            rtn += fixLeft(purchasecntAll,11);
            rtn += fixLeft(purchaseamtAll,11);
            return rtn;
        }

        
    }
  class buf1 
    {
        String groupCode;
        String binNo;
        String crtCnt;
        String changeCnt;
        String statusCnt;
        String effcCnt;
        String purchasecnt;
        String purchaseamt;
        String filler01;
        String name;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(groupCode,  4+1);
            rtn += fixLeft(binNo,  12+1);
            rtn += fixLeft(crtCnt,  10+1);
            rtn += fixLeft(changeCnt,  10+1);
            rtn += fixLeft(statusCnt,  10+1);
            rtn += fixLeft(effcCnt,  10+1);
            rtn += fixLeft(purchasecnt,  10+1);
            rtn += fixLeft(purchaseamt,  10+1);
            rtn += fixLeft(filler01     ,  1);
            rtn += fixLeft(name         ,  40);
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
