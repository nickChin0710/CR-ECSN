/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/05/17 V1.01.01  lai         program initial                            *
*  112/07/28 V1.01.02  JeffKung    mchtChiName get from bil_contract          *
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
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*會計起帳(BIL)處理*/
public class BilRM45 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "每月產出分期付款交易總表計處理  112/07/28 V1.01.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 0;

    String prgmId   = "BilRM45";
    String rptName  = "分期付款未到期總表";
    String rptId    = "CRM45";
    int rptSeq      = 0;
    int pageCnt     = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String hModUser = "";
    String hModTime = "";
    String hModPgm  = "BilRM45";
    String pgmName  = "";

    String tmpstr   = "";
    String buf      = "";
    String tmp      = "";
    String szTmp    = "";


    String hCallBatchSeqno = "";
    String hBusinessDate   = "";
    String hChiDate        = "";
    String hLastDate       = "";

    String hMchtNo          = "";
    String hMchtChiName     = "";
    String hProductNo       = "";
    String hProductName     = "";
    double hTotAmt          = 0;
    double hUnAmt           = 0;
    double SubTotAmt        = 0;
    double SubUnAmt         = 0;
    double AllTotAmt        = 0;
    double AllUnAmt         = 0;

    int    totalCnt         = 0;
    int    kindCnt          = 0;
    int    lineCnt          = 0;
    String tempMchtNo       = "";
    String tempProductNo    = "";

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
       comc.errExit("Usage : BilRM45, this program need only one parameter  ", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }
   
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   String runDate = "";
   String runYM   = "";
   if (args.length >  0) {
        runDate      = "";
        if(args[0].length() == 6) {
           runYM     = args[0];
          } else {
           String ErrMsg = String.format("指定營業月份[%s]", args[0]);
           comcr.errRtn(ErrMsg, "營業月份長度錯誤[yyyymm], 請重新輸入!", hCallBatchSeqno);
          }
   }
   	
   showLogMessage("I", "", String.format("程式參數處理YYYYMM=[%s]",runYM));

   selectPtrBusinday(runYM);
   
   if(runYM.length() == 0 && !hBusinessDate.equals(hLastDate))
   {
	   showLogMessage("I", "", String.format("\n本日非月底日,不需執行!!"));
	   return 0;
   }
   
   showLogMessage("I", "", String.format("\n分期付款交易處理 ......."));
   procBilContract();

   if(totalCnt > 0)
     {
      printSub();
      printFooter();
      String ftpName = String.format("%s.%s_%s", rptId, sysDate, hBusinessDate);
      String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusinessDate);
      //改為線上報表
      //comcr.insertPtrBatchRpt(lpar1);

      comc.writeReport(filename, lpar1);
      ftpMput(ftpName);
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
void selectPtrBusinday(String runYM) throws Exception {

   hBusinessDate = "";
   hLastDate     = "";
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
   
   if(runYM.length() > 0)
     {
       hLastDate     = comm.lastdateOfmonth(runYM);
     }

   showLogMessage("I", "", String.format("本日營業日期=[%s][%s]",hBusinessDate,hLastDate));
   
}
/***********************************************************************/
void initRtn() throws Exception {
    hMchtNo          = "";
    hMchtChiName     = "";
    hProductNo       = "";
    hProductName     = "";
    hTotAmt          = 0;
    hUnAmt           = 0;
}
/***********************************************************************/
void procBilContract() throws Exception {


   sqlCmd  = "select a.mcht_no, a.product_no, a.mcht_chi_name, a.product_name ";
   sqlCmd += "     , sum(tot_amt) as tot_amt ";
   sqlCmd += "     , sum(decode(install_curr_term,0,tot_amt,unit_price*(install_tot_term-install_curr_term)+remd_amt)) as un_amt ";
   sqlCmd += "  from bil_contract a ";
   sqlCmd += " where a.install_curr_term <> a.install_tot_term ";
   sqlCmd += "   and a.tot_amt            > 0         ";
   sqlCmd += " group by a.mcht_no, a.product_no, a.mcht_chi_name, a.product_name ";
   sqlCmd += " order by a.mcht_no, a.product_no ";
		
   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {
         initRtn();

         hMchtNo        = getValue("mcht_no");
         hMchtChiName   = getValue("mcht_chi_name");
         hProductNo     = getValue("product_no");
         hProductName   = getValue("product_name");
         hTotAmt        = getValueDouble("tot_amt"); 
         hUnAmt         = getValueDouble("un_amt"); 

         totalCnt++;

if(DEBUG == 1) showLogMessage("E","","Read Mcht=["+hMchtNo+"] A="+hProductNo+","+hTotAmt+","+hUnAmt);

         if(lineCnt == 0) {
            printHeader();
            lineCnt++;
            tempMchtNo      = hMchtNo;
            tempProductNo   = hProductNo;
           }
         if(!tempMchtNo.equals(hMchtNo))
           {
            printSub();
            SubTotAmt  = 0; SubUnAmt  = 0;
            tempMchtNo      = hMchtNo;
            tempProductNo   = hProductNo;
           }

         SubTotAmt     += hTotAmt;
         SubUnAmt      += hUnAmt;
         AllTotAmt     += hTotAmt;
         AllUnAmt      += hUnAmt;

         printDetail();
     }
   closeCursor(cursorIndex);
}
/***********************************************************************/
void printHeader() {
     pageCnt++;

     buf = "";
     buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
     buf = comcr.insertStrCenter(buf, rptName                              ,130);
     buf = comcr.insertStr(buf, "保存年限: 五年"                           ,110);
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
     buf = comcr.insertStr(buf, "特店代號"     ,   1);
     buf = comcr.insertStr(buf, "特店中文名稱" ,  17);
     buf = comcr.insertStr(buf, "產品代號"     ,  48);
     buf = comcr.insertStr(buf, "產品名稱"     ,  61);
     buf = comcr.insertStr(buf, "總  金  額"   ,  90);
     buf = comcr.insertStr(buf, "未到期金額"   , 110);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     for (int i = 0; i < 130; i++)
         buf += "-";
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     lineCnt = 6;
}
/***********************************************************************/
void printSub() {
     buf = "";
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     buf = comcr.insertStr(buf, "合  計:"  ,  75);
     szTmp = comcr.commFormat("3$,3$,3$"   , SubTotAmt);
     buf = comcr.insertStr(buf, szTmp      ,  88);
     szTmp = comcr.commFormat("3$,3$,3$"   , SubUnAmt);
     buf = comcr.insertStr(buf, szTmp      , 108);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     for (int i = 0; i < 130; i++)    buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     lineCnt = lineCnt + 3;
 }
/***********************************************************************/
void printFooter() {
     buf = "";
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     buf = comcr.insertStr(buf, "總  計:"  ,  75);
     szTmp = comcr.commFormat("3$,3$,3$"   , AllTotAmt);
     buf = comcr.insertStr(buf, szTmp      ,  88);
     szTmp = comcr.commFormat("3$,3$,3$"   , AllUnAmt);
     buf = comcr.insertStr(buf, szTmp      , 108);
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

     buf = "";
     for (int i = 0; i < 130; i++)    buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}
/***********************************************************************/
void printDetail() throws Exception {

  if(lineCnt > 55) {
     lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
     printHeader();
     lineCnt = 0;
    }

   lineCnt++;

   buf = "";
   buf = comcr.insertStr(buf, hMchtNo                        ,  1);
   buf = comcr.insertStr(buf, comc.fixLeft(hMchtChiName, 30) , 17);
   buf = comcr.insertStr(buf, hProductNo                     , 48);
   buf = comcr.insertStr(buf, comc.fixLeft(hProductName, 30) , 61);
   szTmp = comcr.commFormat("3$,3$,3$", hTotAmt);
   buf = comcr.insertStr(buf, szTmp                          , 88);
   szTmp = comcr.commFormat("3$,3$,3$", hUnAmt);
   buf = comcr.insertStr(buf, szTmp                          ,108);

   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}

/***********************************************************************/
int ftpMput(String filename) throws Exception {
    String procCode = "";

    CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
    CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
    commFTP.hEflgModPgm = javaProgram;
    String hEflgRefIpCode = "CREDITCARD";

    System.setProperty("user.dir", commFTP.hEriaLocalDir);

    procCode = "mput " + filename;

    showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

    int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
        comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
    }
    return (0);
}

/***********************************************************************/
public static void main(String[] args) throws Exception {

        BilRM45 proc = new BilRM45();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
