/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/05/18 V1.01.01  lai         program initial                            *
*                                                                             *
******************************************************************************/
package Bil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class BilRM216 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "信用卡各類分期月報表處理  112/05/18 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 0;

    String prgmId   = "BilRM216";
    String rptName  = "信用卡各類分期月報表";
    String rptId    = "BilRM216";
    int rptSeq      = 0;
    int pageCnt     = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String hModUser = "";
    String hModTime = "";
    String hModPgm  = "BilRM216";
    String pgmName  = "";

    String tmpstr   = "";
    String buf      = "";
    String tmp      = "";
    String szTmp    = "";


    String hCallBatchSeqno = "";
    String hBusinessDate   = "";
    String hChiDate        = "";
    String hLastDate       = "";
    String hBegDate        = "";

    String hMchtNo          = "";
    String hNewProcDate     = "";
    double hTotAmt          = 0;
    double AllTotAmt1       = 0;
    double AllTotAmt2       = 0;
    double AllTotAmt3       = 0;
    double AllTotAmt4       = 0;
    double AllTotAmt5       = 0;
    double AllTotAmt6       = 0;
    double AllTotAmt7       = 0;
    double AllTotAmt8       = 0;
    double AllTotAmt9       = 0;
    int    arrayY           = 10;
    String[] allDataH = new String [arrayY];
    double[] allData  = new double [arrayY];


    int    totalCnt         = 0;
    int    kindCnt          = 0;
    int    lineCnt          = 0;
    String tempMchtNo       = "";
    String tempNewProcDate  = "";

    int seqCnt = 1;

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
   showLogMessage("I", "", javaProgram + " " + PROGNAME);
   // =====================================
   if (args.length > 1) {
       comc.errExit("Usage : BilRM216, this program need only one parameter  ", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }
   
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   String runDate = "";
   if (args.length >  0) {
        if(args[0].length() == 8) {
           runDate   = args[0];
          } else {
           String ErrMsg = String.format("指定營業日期[%s]", args[0]);
           comcr.errRtn(ErrMsg, "營業日期長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
   }
   	
   showLogMessage("I", "", String.format("程式參數處理YYYYMMDD=[%s]",runDate));

   selectPtrBusinday(runDate);
   
   showLogMessage("I", "", String.format("\n分期付款交易處理 ......."));
   procBilContract();

   if(totalCnt > 0)
     {
      printDetail();
      printFooter();
      String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptId, sysDate);
      //改為線上報表
      comcr.insertPtrBatchRpt(lpar1);
      if(DEBUG == 1) 
         comc.writeReport(filename, lpar1);
     }
     

   comcr.hCallErrorDesc = "程式執行結束";
   comcr.callbatchEnd();
   finalProcess();
   return 0;
  } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
          }
}
/***********************************************************************/
void selectPtrBusinday(String runDate) throws Exception {

   hBusinessDate = "";
   hLastDate     = "";
   hBegDate      = "";
   hChiDate      = "";
   sqlCmd  = "select business_date ";
   sqlCmd += "     , substr(to_char(to_number(business_date) - 19110000,'0000000'),2,7) h_chi_date ";
   sqlCmd += "     , to_char(last_day(to_date(business_date,'yyyymmdd')),'yyyymmdd')    h_last_date ";
   sqlCmd += " from ptr_businday ";

   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select_ptr_businday not found!", "", "");
   }
   if (recordCnt > 0) {
       hBusinessDate = getValue("business_date");
       hChiDate      = getValue("h_chi_date");
       hLastDate     = getValue("h_last_date");
   }
   
   if (runDate.length() == 8) {
        hBusinessDate = runDate;
   }
   hBegDate      = hBusinessDate.substring(0, 6) + "01";

   showLogMessage("I", "", String.format("本日營業日期=[%s][%s]",hBusinessDate,hBegDate));
   
}
/***********************************************************************/
void initRtn() throws Exception {
    hMchtNo          = "";
    hNewProcDate     = "";
    hTotAmt          = 0;
}
/***********************************************************************/
void  initArray() throws Exception
{
   for (int i = 0; i < arrayY; i++)
       {
        allDataH[i] = "";
        allData[i]  = 0;
       }
}
/***********************************************************************/
void procBilContract() throws Exception {


   sqlCmd  = "select a.new_proc_date  ";
   sqlCmd += "     , case when a.mcht_no in ('106000000001','106000000002','106000000003','106000000004','106000000005','106000000006','106000000007','106000000008','106000000009') ";
   sqlCmd += "       then a.mcht_no else a.ptr_mcht_no end as h_mcht_no  ";
// sqlCmd += "     , sum(tot_amt) as tot_amt ";
   sqlCmd += "     , tot_amt ";
   sqlCmd += "  from bil_contract a ";
   sqlCmd += " where a.new_proc_date between ? and ?  ";
   sqlCmd += "   and a.tot_amt       > 0         ";
   sqlCmd += "   and (a.mcht_no      like '1060000000%'  or ";
   sqlCmd += "        a.ptr_mcht_no  like '1060000000%') ";
//if(DEBUG==1) sqlCmd += " and a.mcht_no in ('000000000000030','9912771100','001010840010','0100801518') ";
// sqlCmd += " group by a.new_proc_date,mcht_no  ";
   sqlCmd += " order by a.new_proc_date,h_mcht_no  ";
   setString(1, hBegDate);
   setString(2, hBusinessDate);
		
   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {
         initRtn();

         hNewProcDate   = getValue("new_proc_date");
         hMchtNo        = getValue("h_mcht_no");
         hTotAmt        = getValueDouble("tot_amt"); 

         totalCnt++;

if(DEBUG == 1) showLogMessage("E","","Read Date=["+hNewProcDate+"] M="+hMchtNo+","+hTotAmt+","+tempNewProcDate);

         if(lineCnt == 0) {
            printHeader();
            lineCnt++;
            tempMchtNo      = hMchtNo;
            tempNewProcDate = hNewProcDate;
           }
         if(!tempNewProcDate.equals(hNewProcDate))
           {
            printDetail();
            initArray();
            tempMchtNo      = hMchtNo;
            tempNewProcDate = hNewProcDate;
           }
       switch (hMchtNo)
         {
          case "106000000001": allDataH[1] = tempNewProcDate; allData[1] += hTotAmt; break;
          case "106000000002": allDataH[2] = tempNewProcDate; allData[2] += hTotAmt; break;
          case "106000000003": allDataH[3] = tempNewProcDate; allData[3] += hTotAmt; break;
          case "106000000004": allDataH[4] = tempNewProcDate; allData[4] += hTotAmt; break;
          case "106000000005": allDataH[5] = tempNewProcDate; allData[5] += hTotAmt; break;
          case "106000000006": allDataH[6] = tempNewProcDate; allData[6] += hTotAmt; break;
          case "106000000007": allDataH[7] = tempNewProcDate; allData[7] += hTotAmt; break;
          case "106000000008": allDataH[8] = tempNewProcDate; allData[8] += hTotAmt; break;
          case "106000000009": allDataH[9] = tempNewProcDate; allData[9] += hTotAmt; break;
         }
if(DEBUG == 1) showLogMessage("E","","  case="+allData[1]+","+allData[2]+","+allData[3]);
     }
   closeCursor(cursorIndex);
}
/***********************************************************************/
void printHeader() {
     pageCnt++;

     buf = "";
     buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
     buf = comcr.insertStrCenter(buf, rptName                              ,130);
     buf = comcr.insertStr(buf, "保存年限: 二年"                           ,110);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

     buf = "";
     tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiDate.substring(0, 3),
                 hChiDate.substring(3, 5), hChiDate.substring(5));
     buf = comcr.insertStr(buf, "報表代號:" + rptId              ,  1);
     buf = comcr.insertStrCenter(buf, "中華民國 " + tmp          ,130);
     buf = comcr.insertStr(buf, "頁    次:"                      ,110);
     szTmp = String.format("%4d", pageCnt);
     buf = comcr.insertStr(buf, szTmp                            ,120);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

     buf = "";
     buf = comcr.insertStr(buf, "項 目"       ,   1);
     buf = comcr.insertStr(buf, " 一      般" ,   7);
     buf = comcr.insertStr(buf, " 綜  所  稅" ,  19);
     buf = comcr.insertStr(buf, " 學      費" ,  31);
     buf = comcr.insertStr(buf, " 稅      費" ,  43);
     buf = comcr.insertStr(buf, " 長      循" ,  55);
     buf = comcr.insertStr(buf, " 旅      遊" ,  67);
     buf = comcr.insertStr(buf, " 帳      單" ,  79);
     buf = comcr.insertStr(buf, " 特      店" ,  91);
     buf = comcr.insertStr(buf, " 想分 就 分" , 103);
     buf = comcr.insertStr(buf, "    總   計" , 115);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     buf = comcr.insertStr(buf, "日 期"       ,   1);
     buf = comcr.insertStr(buf, " 金      額" ,   7);
     buf = comcr.insertStr(buf, " 金      額" ,  19);
     buf = comcr.insertStr(buf, " 金      額" ,  31);
     buf = comcr.insertStr(buf, " 金      額" ,  43);
     buf = comcr.insertStr(buf, " 金      額" ,  55);
     buf = comcr.insertStr(buf, " 金      額" ,  67);
     buf = comcr.insertStr(buf, " 金      額" ,  79);
     buf = comcr.insertStr(buf, " 金      額" ,  91);
     buf = comcr.insertStr(buf, " 金      額" , 103);
     buf = comcr.insertStr(buf, "    金   額" , 115);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     for (int i = 0; i < 130; i++)
         buf += "=";
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     lineCnt = 6;
}
/***********************************************************************/
void printFooter() {
   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "總計:"       ,   1);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt1);
   buf = comcr.insertStr(buf, szTmp         ,   7);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt2);
   buf = comcr.insertStr(buf, szTmp         ,  19);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt3);
   buf = comcr.insertStr(buf, szTmp         ,  31);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt4);
   buf = comcr.insertStr(buf, szTmp         ,  43);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt5);
   buf = comcr.insertStr(buf, szTmp         ,  55);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt6);
   buf = comcr.insertStr(buf, szTmp         ,  67);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt7);
   buf = comcr.insertStr(buf, szTmp         ,  79);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt8);
   buf = comcr.insertStr(buf, szTmp         ,  91);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt9);
   buf = comcr.insertStr(buf, szTmp         , 103);
   szTmp = comcr.commFormat("2$,3$,3$", AllTotAmt1+AllTotAmt2+AllTotAmt3+AllTotAmt4+AllTotAmt5
                                      + AllTotAmt6+AllTotAmt7+AllTotAmt8+AllTotAmt9);
   buf = comcr.insertStr(buf, szTmp         , 115); 

   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   buf = comcr.insertStr(buf, "分期說明:"     ,   1);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
   buf = "";
   buf = "         106000000001-一般分期      106000000002-綜所稅分期      106000000003-學費分期";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
   buf = "         106000000004-稅費分期      106000000005-長循分期        106000000006-旅遊分期";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
   buf = "         106000000007-帳單分期      106000000008-特店分期        106000000009-想分就分專案";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
}
/***********************************************************************/
void printDetail() throws Exception {

if(DEBUG == 1) showLogMessage("E","","  print D="+hNewProcDate+","+tempNewProcDate);
  if(lineCnt > 55) {
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
     printHeader();
     lineCnt = 0;
    }

   lineCnt++;

   buf = "";
   buf = comcr.insertStr(buf, tempNewProcDate.substring(4, 6)+"/"+tempNewProcDate.substring(6, 8),  1);
   szTmp = comcr.commFormat("2$,3$,3$", allData[1]);
   buf = comcr.insertStr(buf, szTmp         ,   7);
   szTmp = comcr.commFormat("2$,3$,3$", allData[2]);
   buf = comcr.insertStr(buf, szTmp         ,  19);
   szTmp = comcr.commFormat("2$,3$,3$", allData[3]);
   buf = comcr.insertStr(buf, szTmp         ,  31);
   szTmp = comcr.commFormat("2$,3$,3$", allData[4]);
   buf = comcr.insertStr(buf, szTmp         ,  43);
   szTmp = comcr.commFormat("2$,3$,3$", allData[5]);
   buf = comcr.insertStr(buf, szTmp         ,  55);
   szTmp = comcr.commFormat("2$,3$,3$", allData[6]);
   buf = comcr.insertStr(buf, szTmp         ,  67);
   szTmp = comcr.commFormat("2$,3$,3$", allData[7]);
   buf = comcr.insertStr(buf, szTmp         ,  79);
   szTmp = comcr.commFormat("2$,3$,3$", allData[8]);
   buf = comcr.insertStr(buf, szTmp         ,  91);
   szTmp = comcr.commFormat("2$,3$,3$", allData[9]);
   buf = comcr.insertStr(buf, szTmp         , 103);
   szTmp = comcr.commFormat("2$,3$,3$", allData[1]+allData[2]+allData[3]+allData[4]+allData[5]
                                      + allData[6]+allData[7]+allData[8]+allData[9]);
   buf = comcr.insertStr(buf, szTmp         , 115);

   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   AllTotAmt1  += allData[1];
   AllTotAmt2  += allData[2];
   AllTotAmt3  += allData[3];
   AllTotAmt4  += allData[4];
   AllTotAmt5  += allData[5];
   AllTotAmt6  += allData[6];
   AllTotAmt7  += allData[7];
   AllTotAmt8  += allData[8];
   AllTotAmt9  += allData[9];
}
/***********************************************************************/
public static void main(String[] args) throws Exception {

        BilRM216 proc = new BilRM216();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
