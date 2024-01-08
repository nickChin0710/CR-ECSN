/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/06/08 V1.01.01  JeffKung    program initial                            *
*                                                                             *
******************************************************************************/
package Dbb;

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
public class DbbV004 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "統計各種費用的入帳金額,產出會計分錄處理  112/06/08 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 1;

    String prgmId   = "DbbV004";
    String prgmName = "VD卡手續費明細報表";
    String rptName  = "VD卡手續費明細報表";
    String rptId    = "CRD53";
    int rptSeq      = 0;
    int pageCnt     = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String hModUser = "";
    String hModTime = "";
    String hModPgm  = "";
    String pgmName  = "";
    String tmpstr   = "";
    String buf      = "";
    String tmp      = "";
    String szTmp    = "";


    String hCallBatchSeqno   = "";
    String hBusiBusinessDate = "";
    String hTempVouchDate    = "";
    String hTempVouchChiDate = "";
    String hBusiVouchDate    = "";
    String chiDate           = "";

    String hVouchCdKind   = "";
    String hGsvhAcNo      = "";
    String hGsvhDbcr      = "";

    String hAccmMemo3Kind = "";
    String hAccmMemo3Flag = "";
    String hAccmDrFlag    = "";
    String hAccmCrFlag    = "";

    String hChiShortName    = "";
    String hPcceCurrEngName = "";
    String hPcceCurrChiName = "";
    String hPcceCurrCodeGl  = "";
    String hPccdGlcode      = "";
    String runBillType      = "";
    String runAcctCode      = "";
    String runTxnCode       = "";
    String runVouchCode     = "";

    int    hGsvhDbcrSeq     = 0;
    double callVoucherAmt   = 0;
    double vouchKindAmt     = 0;
    double vouchDRAmt       = 0;
    double vouchCRAmt       = 0;
    int    totalCnt         = 0;
    int    kindCnt          = 0;
    int    lineCnt          = 0;
    
    double[] tailKindAmt = new double [3];
    int[]    tailKindCnt = new int [3];

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
       comc.errExit("Usage : DbbV004, this program need only one parameter  ", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }
   
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   String runDate = "";
   if (args.length >  0) {
        runDate      = "";
        if(args[0].length() == 8) {
           runDate   = args[0];
          } else {
           String ErrMsg = String.format("指定營業日[%s]", args[0]);
           comcr.errRtn(ErrMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
   }
   	
   selectPtrBusinday(runDate);
   
   showLogMessage("I", "", String.format("DbbV004會計分錄開始......."));
   comcr.vouchPageCnt = 0;
   comcr.rptSeq       = 0;
   pgmName = String.format("DbbV004");

   for (int k = 0; k < 3; k++)
     {
	   tailKindAmt [k] = 0;
	   tailKindCnt [k] = 0;
      
	   switch (k)
        {
         case 0 :runVouchCode = "C002"; break;
         case 1 :runVouchCode = "C002"; break;
         case 2 :runVouchCode = "C002"; break;
        }

      procVouchData00(k, runVouchCode);
      
     }
   
   showLogMessage("I", "", String.format("\n[%f]費用入帳金額.......",vouchCRAmt));
   
   if(vouchDRAmt > 0 || vouchCRAmt > 0)
     {
      printFooter();
      String ftpName = String.format("%s_DbbV004.%s_%s", rptId, sysDate, hBusiBusinessDate);
      String filename = String.format("%s/reports/%s_DbbV004.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusiBusinessDate);
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
void selectPtrBusinday(String runDate) throws Exception {
   hBusiBusinessDate = "";
   hTempVouchDate    = "";
   hTempVouchChiDate = "";
   sqlCmd = "select business_date,";
   sqlCmd += " vouch_date,";
   sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
   sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_busi_vouch_date ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select_ptr_businday not found!", "", "");
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = getValue("business_date");
       hTempVouchDate    = getValue("vouch_date");
       hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
       hBusiVouchDate    = getValue("h_busi_vouch_date");
   }
   
   showLogMessage("I", "", String.format("本日營業日期=[%s]",hBusiBusinessDate));
   
   if (runDate.length() == 8) {
   	hBusiBusinessDate = runDate;
   }

   showLogMessage("I", "", String.format("程式處理日期=[%s]",hBusiBusinessDate));

}
/***********************************************************************/
void procVouchData00(int idx, String stdVouchCode) throws Exception {

/* PF	第一類	C002	國外交易手續費
   PF	第二類	C002	ＥＧＯＶ手續費
   PF	第三類	C002	語音繳費手續費 
*/
   runBillType      = "";
   runAcctCode      = "";
   runTxnCode       = "";
   String memo = "";

   if (idx==0) {
	   runAcctCode = "PF"; 
	   runBillType = "FIFC"; 
	   runTxnCode = "05";
	   memo = "國外清算手續費";
	   showLogMessage("I", "", String.format("\n[%s]費用入帳金額.......","C002:國外交易手續費"));
   } else if (idx==1) {
	   runAcctCode = "PF"; 
	   runBillType = "OSSG"; 
	   runTxnCode = "VD";
	   memo = "EGOV手續費";
	   showLogMessage("I", "", String.format("\n[%s]費用入帳金額.......","C002:ＥＧＯＶ手續費"));
   } else if (idx==2) {
	   runAcctCode = "PF"; 
	   runBillType = "OSSG"; 
	   runTxnCode = "VM";
	   memo = "繳納交通罰鍰等手續費";
	   showLogMessage("I", "", String.format("\n[%s]費用入帳金額.......","C002:語音繳費手續費"));
   }

   vouchKindAmt  = 0;
   kindCnt       = 0;

   sqlCmd  = "select a.card_no,a.dest_amt,a.purchase_date,a.curr_code,a.txn_code ";
   sqlCmd += "     , b.chi_short_name ";
   sqlCmd += "  from ptr_actcode b, dbb_curpost a ";
   sqlCmd += " where a.this_close_date = ? ";
   sqlCmd += "   and b.acct_code       = a.acct_code ";
   sqlCmd += "   and a.bill_type    = ? ";
   sqlCmd += "   and a.txn_code     = ? ";
   sqlCmd += "   and a.acct_code    = ? ";
   sqlCmd += "   and a.tx_convt_flag <> 'R' ";
		
   setString(1,hBusiBusinessDate);
   setString(2,runBillType);
   setString(3,runTxnCode);
   setString(4,runAcctCode);

   //showLogMessage("I", "", String.format("\n[%s].......",sqlCmd));
   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {
	   
	     //showLogMessage("I", "", String.format("totalCnt=%d",totalCnt));
         hChiShortName  = getValue("chi_short_name");
         vouchKindAmt  += getValueDouble("dest_amt"); 
         kindCnt++;
         vouchDRAmt    += getValueDouble("dest_amt");  // 借
         
         tailKindAmt[idx] += getValueDouble("dest_amt");
         tailKindCnt[idx]++;

         totalCnt++;

         if(lineCnt == 0) {
            printHeader();
            lineCnt++;
           }

         if(lineCnt > 25) {
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
            printHeader();
            lineCnt = 0;
           }

         printDetail();
     }
   
   closeCursor(cursorIndex);

   if(kindCnt > 0)
     {
       // 會科套號
       hVouchCdKind = stdVouchCode;

       String currCode = "901";
       selectPtrCurrcode(currCode);

       int itemCnt = selectGenSysVouch(stdVouchCode);

       if (itemCnt <= 0) {
          showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=["+stdVouchCode+"]");
          return;
       }


	comcr.hGsvhCurr = "00";

	chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

	comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

	tmpstr = String.format("DbbV004_%s.%s_%s","D02", hVouchCdKind, hPcceCurrCodeGl);  
	comcr.hGsvhModPgm = tmpstr;
	comcr.hGsvhModWs = "DBBV04R01";

	int drIdx = 0;
	int crIdx = 0;

	for (int i = 0; i < itemCnt; i++) {
	   hGsvhAcNo      = getValue("ac_no"            , i);
	   hGsvhDbcrSeq   = getValueInt("dbcr_seq"      , i);
	   hGsvhDbcr      = getValue("dbcr"             , i);
	   hAccmMemo3Kind = getValue("memo3_kind"       , i);
	   hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
	   hAccmDrFlag    = getValue("h_accm_dr_flag"   , i);
	   hAccmCrFlag    = getValue("h_accm_cr_flag"   , i);

	   /* Memo 1, Memo 2, Memo3 */
	   comcr.hGsvhMemo1 = memo;
	   comcr.hGsvhMemo2 = "";
	   comcr.hGsvhMemo3 = "";
   
       callVoucherAmt = vouchKindAmt;
       
	   if (callVoucherAmt != 0) {
		if(comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCodeGl) != 0) {
		   showLogMessage("E", "", "call detail_vouch error, AcNo=[" + hGsvhAcNo + "]");
		   return;
		  }
	   }
	}
     }

}
/**************************************************************************/
int selectGenSysVouch(String stdVouchCode) throws Exception {
    	
	sqlCmd = "select ";
	sqlCmd += " gen_sys_vouch.ac_no,";
	sqlCmd += " gen_sys_vouch.dbcr_seq,";
	sqlCmd += " gen_sys_vouch.dbcr,";
	sqlCmd += " gen_acct_m.memo3_kind,";
	sqlCmd += " decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_accm_memo3_flag,";
	sqlCmd += " decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) h_accm_dr_flag,";
	sqlCmd += " decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) h_accm_cr_flag ";
	sqlCmd += " from gen_sys_vouch,gen_acct_m ";
	sqlCmd += "where std_vouch_cd = ? ";
	sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
	sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
		
	setString(1, stdVouchCode);
	int recordCnt1 = selectTable();
		
	return recordCnt1;
}
/***********************************************************************/
void selectPtrCurrcode(String currCode) throws Exception {
        hPcceCurrEngName = "";
        hPcceCurrChiName = "";
        hPcceCurrCodeGl = "";
        sqlCmd  = "select curr_eng_name,";
        sqlCmd += "       curr_chi_name,";
        sqlCmd += "       curr_code_gl ";
        sqlCmd += " from ptr_currcode  ";
        sqlCmd += "where curr_code = ? ";
        setString(1, currCode);
        int recordCnt1 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_currcode not found!", "", "");
        }
        if (recordCnt1 > 0) {
            hPcceCurrEngName = getValue("curr_eng_name");
            hPcceCurrChiName = getValue("curr_chi_name");
            hPcceCurrCodeGl  = getValue("curr_code_gl");
        }

}
/***********************************************************************/
void printHeader() {
        pageCnt++;

        buf = "";
        buf = comcr.insertStr(buf, rptId         ,   1);
        buf = comcr.insertStrCenter(buf, rptName , 132);
        buf = comcr.insertStr(buf, "頁次:"       , 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp         , 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:"       ,  1);
        buf = comcr.insertStr(buf, sysDate           , 10);
        buf = comcr.insertStr(buf, "入帳日 :"        , 20);
        buf = comcr.insertStr(buf, hBusiBusinessDate , 30);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

        buf = "";
        buf = comcr.insertStr(buf, "交易日期"     ,   1);
        buf = comcr.insertStr(buf, "交易摘要"     ,  11);
        buf = comcr.insertStr(buf, "卡     號"    ,  40);
        buf = comcr.insertStr(buf, "交易金額/本金",  69);
        buf = comcr.insertStr(buf, "入帳科子目"   ,  85);
        buf = comcr.insertStr(buf, "備__註"       , 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
}
/***********************************************************************/
void printFooter() {
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, ""  ,  21);
        buf = comcr.insertStr(buf, "借方合計" ,  56);
        buf = comcr.insertStr(buf, "貸方合計" ,  80);
        buf = comcr.insertStr(buf, "筆數"     , 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "國外清算手續費:"  ,  21);
        szTmp = comcr.commFormat("3$,3$,3$,3$", tailKindAmt[0]);
        buf = comcr.insertStr(buf, szTmp      ,  48);
        szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
        buf = comcr.insertStr(buf, szTmp      ,  72);
        szTmp = comcr.commFormat("3z,3z,3z"   , tailKindCnt[0]);
        buf = comcr.insertStr(buf, szTmp      ,  92);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "EGOV手續費:"  ,  21);
        szTmp = comcr.commFormat("3$,3$,3$,3$", tailKindAmt[1]);
        buf = comcr.insertStr(buf, szTmp      ,  48);
        szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
        buf = comcr.insertStr(buf, szTmp      ,  72);
        szTmp = comcr.commFormat("3z,3z,3z"   , tailKindCnt[1]);
        buf = comcr.insertStr(buf, szTmp      ,  92);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "繳納罰鍰等手續費:"  ,  21);
        szTmp = comcr.commFormat("3$,3$,3$,3$", tailKindAmt[2]);
        buf = comcr.insertStr(buf, szTmp      ,  48);
        szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
        buf = comcr.insertStr(buf, szTmp      ,  72);
        szTmp = comcr.commFormat("3z,3z,3z"   , tailKindCnt[2]);
        buf = comcr.insertStr(buf, szTmp      ,  92);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "合計:"  ,  21);
        szTmp = comcr.commFormat("3$,3$,3$,3$", vouchDRAmt);
        buf = comcr.insertStr(buf, szTmp      ,  48);
        szTmp = comcr.commFormat("3$,3$,3$,3$", vouchCRAmt);
        buf = comcr.insertStr(buf, szTmp      ,  72);
        szTmp = comcr.commFormat("3z,3z,3z"   , totalCnt);
        buf = comcr.insertStr(buf, szTmp      ,  92);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
 }
/***********************************************************************/
void printDetail() throws Exception {
   lineCnt++;

   buf = "";
   buf = comcr.insertStr(buf, getValue("purchase_date")      ,  1);
   if ("05".equals(getValue("txn_code"))) {
		buf = comcr.insertStr(buf, "05-國外清算手續費", 11);
   } else if ("VD".equals(getValue("txn_code"))) {
		buf = comcr.insertStr(buf, "VD-EGOV手續費", 11);
   } else if ("VM".equals(getValue("txn_code"))) {
		buf = comcr.insertStr(buf, "VM-繳納罰鍰等手續費", 11);
   }

   buf = comcr.insertStr(buf, getValue("card_no")            , 40);
   szTmp = comcr.commFormat("3$,3$,3$,3$", getValueDouble("dest_amt"));
   buf = comcr.insertStr(buf, szTmp                          , 66);
   buf = comcr.insertStr(buf, comc.fixLeft(hChiShortName, 20), 85);

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

        DbbV004 proc = new DbbV004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
