/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/07/14  V1.01.01    phopho     Initial                                  *
* 109/01/10  V1.01.02    phopho     fix writeTextFile add "\n"               *
* 109/02/14  V1.01.03    phopho     fix select_ptr_businday SQL              *
* 109/12/09  V1.00.04    shiyuqi       updated for project coding standard   *
* 112/07/04  V1.00.05    sunny      update for TCB format,無資料產生空檔                *
* 112/07/05  V1.00.06    Ryan      FTP至/crdatacrea/ 備份至/media/col/backup    *
* 112/10/02  V1.00.07    sunny     備份檔名增加日期                                                             *
*****************************************************************************/

package Col;

import java.nio.file.Paths;
import java.text.Normalizer;

import com.*;

public class ColA421 extends AccessDAO {
    private String progname = "前置協商回報債權媒體檔產生程式  112/10/02  V1.00.07";
	private static final String FILE_FOLDER = "/media/col/";
	private static final String FILE_NAME = "CARDCREDIT.TXT";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debug1 = 0;
    String hBusiBusinessDate = "";

    String hCcdtId = "";
    String hCcdtChiName = "";
    String hCcdtBankCode = "";
    String hCcdtApplyDate = "";
    String hCcdtNegoSDate = "";
    String hCcdtInterestBaseDate = "";
    double hCcdtInEndBal = 0;
    String hCcdtEthicRiskMark = "";
    double hCcdtOutEndBal = 0;
    double hCcdtLastestPayAmt = 0;
    double hCcdtInEndBalNew = 0;
    double hCcdtOutEndBalNew = 0;
    double hCcdtLastestPayAmtNew = 0;
    String hCcdtNotSendFlag = "";
    String hCcdtAprFlag = "";
    double hCcdtOutCapital = 0;
    double hCcdtOutInterest = 0;
    double hCcdtOutFee = 0;
    double hCcdtOutPn = 0;
    double hCcdtOutCapitalNew = 0;
    double hCcdtOutInterestNew = 0;
    double hCcdtOutFeeNew = 0;
    double hCcdtOutPnNew = 0;
    String hCcdtRowid = "";
    String hClnoLiacStatus = "";
    String hClnoAcctStatus = "";
    String hClnoAcctStatusFlag = "";
    String hAcnoPaymentNoII = "";
    String hOwsmWfValue = "";
    double hOwsmWfValue6 = 0;
    String hCallBatchSeqno = "";
    
    String hTempAcctStatus="";
    String hTempInterestBaseDate="";
    String hTempBusiBusinessDate="";

    long totalCnt = 0;
    String hTempSysdate = "";
    int hTempDays = 0;

    //file1:CARDCREDIT.TXT
    String buf = "";
    String temstr = "";    
    private int fptr = 0;
    
    //file2:CREDIT.TXT
    String buf2 = "";
    String temstr2 = "";    
    private int fptr2 = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColA421 proc = new ColA421();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 檢查參數
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA421 [business_date] [sysdate]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            hTempSysdate = "";
            if (args.length > 0) {
                String sGArgs0 = "";
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hBusiBusinessDate = sGArgs0;
            }
            if (args.length > 1) {
                String sGArgs1 = "";
                sGArgs1 = args[1];
                sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                hTempSysdate = sGArgs1;
            }

            selectOfwSysparm();
            selectPtrBusinday();

            fileOpen();
//            fileOpen2();
            selectColLiacDebt();
            // fseek(fptr1, 0L, SEEK_SET); //todo

//            buf = "";
//            buf = String.format("本頁總筆數:,%08d%c%c", totalCnt, 13, 10);
//            writeTextFile(fptr2, String.format("%s",buf));
            
          //無資料產生空檔
//            buf = "";
//            writeTextFile(fptr, String.format("%s",buf));
            closeOutputText(fptr);
            
            procFTP(FILE_NAME,Paths.get(comc.getECSHOME(), FILE_FOLDER).toString());
            moveFile(FILE_NAME,Paths.get(comc.getECSHOME(), FILE_FOLDER).toString());
            
            showLogMessage("I", "", "程式執行結束,累計筆數 : [" + totalCnt + "]");           
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        selectSQL = "decode(cast(? as varchar(8)), '' ,business_date, cast(? as varchar(8))) as business_date, "
                  + "decode(cast(? as varchar(8)), '' ,to_char(decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'18'),"
                  + "1,sysdate+1,sysdate),'yyyy.mm.dd'), cast(? as varchar(8))) as hsysdate";
        daoTable = "ptr_businday";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hTempSysdate);
        setString(4, hTempSysdate);

        if (selectTable() > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("hsysdate");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectOfwSysparm() throws Exception {
        hOwsmWfValue6 = 0;
        selectSQL = "wf_value6 ";
        daoTable = "ptr_sys_parm";
        whereStr = "where wf_parm = 'SYSPARM' and wf_key = 'COL_LIAC'";

        if (selectTable() > 0) {
            hOwsmWfValue6 = getValueDouble("wf_value6");
        }
        
        if (notFound.equals("Y")) {
        	hOwsmWfValue6 = 0;  //預設為0
            //comcr.errRtn("select_ofw_sysparm error!", "", hCallBatchSeqno);
        	showLogMessage("I", "", "select_ofw_sysparm error!wf_parm[COL_LIAC]，wf_value6[0]，預設為0");
            
        }
    }

    // ************************************************************************
    private void selectColLiacDebt() throws Exception {
        selectSQL = "b.id_no, b.chi_name, b.bank_code, b.apply_date, b.interest_base_date, "
                + "b.in_end_bal, b.out_end_bal, b.out_capital, b.out_interest, b.out_fee, "
                + "b.out_pn, b.lastest_pay_amt, b.in_end_bal_new, b.out_end_bal_new, "
                + "b.out_capital_new, b.out_interest_new, b.out_fee_new, b.out_pn_new, "
//                + "b.lastest_pay_amt_new, to_date( ? ,'yyyymmdd') - to_date(b.apply_date,'yyyymmdd') temp_days, "
                + "b.lastest_pay_amt_new, days(to_date( ? ,'yyyymmdd')) - days(to_date(b.apply_date,'yyyymmdd')) temp_days, "
                + "b.not_send_flag, b.apr_flag, trim(b.ethic_risk_mark) ethic_risk_mark, a.liac_status, c.payment_no_ii,"
                + "b.rowid as rowid, b.acct_status,b.acct_status_flag ";
        daoTable = "col_liac_nego a,col_liac_debt b,act_acno c";
        //test
        //whereStr = "where decode(not_send_flag,'','N',not_send_flag) != 'Y' and a.liac_seqno = b.liac_seqno ";
        whereStr = "where b.proc_flag = '1' and decode(not_send_flag,'','N',not_send_flag) != 'Y' and a.liac_seqno = b.liac_seqno ";
        whereStr += "and b.id_p_seqno=c.id_p_seqno and c.acno_flag='1'";
        setString(1, hBusiBusinessDate);

        openCursor();
        while (fetchTable()) {
            hCcdtId = getValue("id_no");
            hCcdtChiName = getValue("chi_name");
            hCcdtBankCode = getValue("bank_code");
            hCcdtApplyDate = getValue("apply_date");
            //hCcdtNegoSDate = getValue("nego_s_date");
            hCcdtInterestBaseDate = getValue("interest_base_date");
            hCcdtInEndBal = getValueInt("in_end_bal");
            hCcdtOutEndBal = getValueInt("out_end_bal");
            hCcdtOutCapital = getValueInt("out_capital");
            hCcdtOutInterest = getValueInt("out_interest");
            hCcdtOutFee = getValueInt("out_fee");
            hCcdtOutPn = getValueInt("out_pn");
            hCcdtLastestPayAmt = getValueInt("lastest_pay_amt");
            hCcdtInEndBalNew = getValueInt("in_end_bal_new");
            hCcdtOutEndBalNew = getValueInt("out_end_bal_new");
            hCcdtOutCapitalNew = getValueInt("out_capital_new");
            hCcdtOutInterestNew = getValueInt("out_interest_new");
            hCcdtOutFeeNew = getValueInt("out_fee_new");
            hCcdtOutPnNew = getValueInt("out_pn_new");
            hCcdtLastestPayAmtNew = getValueInt("lastest_pay_amt_new");
            hTempDays = getValueInt("temp_days");
            hCcdtNotSendFlag = getValue("not_send_flag");
            hCcdtAprFlag = getValue("apr_flag");
            hCcdtEthicRiskMark = getValue("ethic_risk_mark");
            hClnoLiacStatus = getValue("liac_status");
            hClnoAcctStatus = getValue("acct_status");
            hClnoAcctStatusFlag = getValue("liac_status_flag");
            hAcnoPaymentNoII = getValue("payment_no_ii");
            hCcdtRowid = getValue("rowid");

            //放款種類
            hTempAcctStatus = "";
            switch (hClnoAcctStatus) {
            case "1":
            	hTempAcctStatus = "A";
                break;
            case "2":
            	hTempAcctStatus = "A";
                break;
            case "3":
            	hTempAcctStatus = "B";
                break;
            case "4":
            	hTempAcctStatus = "D"; //呆帳
                break;           
            default:
            	hTempAcctStatus = "A";
                break;
            }
 
        // 止息基準日轉民國年
            hTempInterestBaseDate = String.format("%03d", Integer.valueOf(hCcdtInterestBaseDate.substring(0, 4)) - 1911)
					              + hCcdtInterestBaseDate.substring(4, 6) + hCcdtInterestBaseDate.substring(6, 8);

           // showLogMessage("I", "", String.format("InterestBaseDate轉民國年[%s] ID_NO[%s]", hTempInterestBaseDate, hCcdtId));
            
        // 作業日(程式執行日)轉民國年
            hTempBusiBusinessDate = String.format("%03d", Integer.valueOf(hBusiBusinessDate.substring(0, 4)) - 1911)
		              + hBusiBusinessDate.substring(4, 6) + hBusiBusinessDate.substring(6, 8);
           // showLogMessage("I", "", String.format("BusinessDate轉民國年[%s] ID_NO[%s]", hTempBusiBusinessDate, hCcdtId));
            
           
            if ((hClnoLiacStatus.equals("1") == false) && (hClnoLiacStatus.equals("2") == false)) {
                updateColLiacDebt(1);
                showLogMessage("I", "", String.format("update ColLiacDebt(1),ID_NO[%s]", hCcdtId));
                continue;
            }

            if (hCcdtAprFlag.equals("Y") == false) {
//                if (hTempDays != 10)
//                    continue;

                if (hCcdtNotSendFlag.equals("Y"))
                {
                    showLogMessage("I", "", String.format("SELECT NotSendFlag=Y 跳過不處理 , ID_NO[%s]", hCcdtId));
                    continue;
                }

                if ((hCcdtInEndBal <= hOwsmWfValue6) && (hCcdtInEndBal != 0)) {
                    updateColLiacDebt1();
                    showLogMessage("I", "", String.format("update ColLiacDebt1,ID_NO[%s]", hCcdtId));
                    continue;
                 }
                             
                hCcdtInEndBalNew = hCcdtInEndBal;
                hCcdtOutEndBalNew = hCcdtOutEndBal;
                hCcdtOutCapitalNew = hCcdtOutCapital;
                hCcdtOutInterestNew = hCcdtOutInterest;
                hCcdtOutPnNew = hCcdtOutPn;
                hCcdtOutFeeNew = hCcdtOutFee;
                hCcdtLastestPayAmtNew = hCcdtLastestPayAmt;
                
                totalCnt++;
            }

            if (totalCnt % 30000 == 0)
                showLogMessage("I", "", "處理筆數 : [" + totalCnt + "]");

			/* TCB
			 * 正常催收產生CARDCREDIT.TXT及呆帳產生CREDIT.TXT
			1	身分證字號	X(10) 	10	
			2	分隔符號	X(1)	1	逗號
			3	貸款帳號	X(20)	20	未來新系統帳號欄位長度為20碼，帳號欄位將依卡部風管科討論後的規則(3144+個人虛擬編號-99666+英文轉2碼數字+身份證後9位)進行處理
			4	分隔符號	X(1)	1	逗號
			5	止息基準日	X(7)	7	民國年YYYMMDD
			6	分隔符號	X(1)	1	逗號
			7	現欠金額	X(10)	10	靠右前補0
			8	分隔符號	X(1)	1	逗號
			9	利息	X(10)	10	靠右前補0
			10	分隔符號	X(1)	1	逗號
			11	違約金	X(10)	10	靠右前補0
			12	分隔符號	X(1)	1	逗號
			13	最近一期繳款金額	X(10)	10	靠右前補0
			14	分隔符號	X(1)	1	逗號
			15	分隔符號(擔保品類別)	X(1)	1	逗號
			16	分隔符號(原借款金額)	X(1)	1	逗號
			17	分隔符號(每期應付金額)	X(1)	1	逗號
			18	分隔符號(已到期尚未清償金額)	X(1)	1	逗號
			19	分隔符號(每月應還款日)	X(1)	1	逗號
			20	分隔符號(契約起始年月)	X(1)	1	逗號
			21	分隔符號(契約截止年月)	X(1)	1	逗號
			22	分隔符號(最後繳息日)	X(1)	1	逗號
			23	最大債權分行	X(4)	4	固定「3144」
			24	分隔符號	X(1)	1	逗號
			25	放款種類	X(1)	1	「A 正常 B 催收」 呆帳(C或D)?
			26	分隔符號	X(1)	1	逗號
			27	作業日	X(7)	7	.
			*/
			            
//            buf = "";
//            buf = String.format("%s,%s,%s,%s,%s,%.0f,%.0f,%.0f,%.0f,%.0f,%.0f,%.0f%c%c", hCcdtId, hCcdtChiName,
//                    hCcdtBankCode, hCcdtApplyDate, hCcdtInterestBaseDate, hCcdtInEndBalNew,
//                    hCcdtOutEndBalNew, hCcdtLastestPayAmtNew, hCcdtOutCapitalNew, hCcdtOutInterestNew,
//                    hCcdtOutPnNew, hCcdtOutFeeNew, 13, 10);
            
            //金額>0或不為呆帳戶才產生CARDCREDIT.TXT
            
            if ((hCcdtInEndBal > hOwsmWfValue6) && (hCcdtInEndBal > 0) && !hTempAcctStatus.equals("D")) {                     
            
            buf = "";
            buf = String.format("%s,%4s%16s,%s,%010.0f,%010.0f,%010.0f,%010.0f,,,,,,,,,%4s,%1s,%s", //金額欄位前補0
            		hCcdtId, "3144",hAcnoPaymentNoII,hTempInterestBaseDate,                  //ID,帳號,止息基準日
            		hCcdtOutCapital,hCcdtOutInterestNew,hCcdtOutPnNew,hCcdtLastestPayAmt,    //本金,利息,違約金,最後一期繳款金額
            		"3144",hTempAcctStatus,hTempBusiBusinessDate);                           //最大債權分行,放款種類,作業日
            //hCcdtInterestBaseDate,hBusiBusinessDate
            //hTempInterestBaseDate,hTempBusiBusinessDate
            writeTextFile(fptr, String.format("%s",buf + "\n"));  //base level bug
            
            updateColLiacDebt(0);            
            }
            
            // 金額>0且為呆帳戶則產生CREDIT.TXT
            // 20230704 與OA科語柔討論呆帳作業仍以放款系統為主，暫時先不產生。
               
                buf2 = "";
                buf2 = String.format("%s,%4s%16s,%s,%010.0f,%010.0f,%010.0f,%010.0f,,,,,,,,,%4s,%1s,%s",  //金額欄位前補0
                		hCcdtId, "3144",hAcnoPaymentNoII,hTempInterestBaseDate,                  //ID,帳號,止息基準日
                		hCcdtOutCapital,hCcdtOutInterestNew,hCcdtOutPnNew,hCcdtLastestPayAmt,    //本金,利息,違約金,最後一期繳款金額
                		"3144",hTempAcctStatus,hTempBusiBusinessDate);                           //最大債權分行,放款種類,作業日
                //hCcdtInterestBaseDate,hBusiBusinessDate
                //hTempInterestBaseDate,hTempBusiBusinessDate
//                writeTextFile(fptr2, String.format("%s",buf2 + "\n"));

                updateColLiacDebt(0);            
                }
      
        closeCursor();
    }

    // ************************************************************************
    private void updateColLiacDebt(int hInt) throws Exception {
        dateTime();
        updateSQL = "proc_date   = ?, report_date = decode(cast(? as integer),0,cast(? as varchar(8)),''), "
                + "apr_user    = decode(cast(? as integer),0,decode(apr_user,'','ECS',apr_user),apr_user), "
                + "apr_date    = decode(cast(? as integer),0,decode(apr_date,'',cast(? as varchar(8)),apr_date),apr_date), "
                + "proc_flag   = decode(cast(? as integer),0,'2','A'), mod_pgm = ?, mod_time = sysdate ";
        daoTable = "col_liac_debt";
        whereStr = "WHERE rowid = ? ";
        setString(1, hBusiBusinessDate);
        setInt(2, hInt);
        setString(3, hBusiBusinessDate);
        setInt(4, hInt);
        setInt(5, hInt);
        setString(6, hBusiBusinessDate);
        setInt(7, hInt);
        setString(8, javaProgram);
        setRowId(9, hCcdtRowid);

        updateTable();

        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liac_debt not found!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void fileOpen() throws Exception {
    	temstr = String.format("%s/media/col/CARDCREDIT.TXT", comc.getECSHOME());
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        fptr = openOutputText(temstr, "MS950");
        if (fptr == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr), "", hCallBatchSeqno);
        }

//        buf = "";
//        buf = String.format("本頁總筆數:,%08d%c%c", 0, 13, 10);
//        writeTextFile(fptr1, String.format("%s",buf + "\n"));  //base level bug
        
//        buf = "";
        writeTextFile(fptr, String.format("%s",buf)); //無資料產生空檔
    }
    
    // ************************************************************************
    private void fileOpen2() throws Exception {
    	temstr2 = String.format("%s/media/col/CREDIT.TXT", comc.getECSHOME());
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        fptr2 = openOutputText(temstr2, "MS950");
        if (fptr2 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr2), "", hCallBatchSeqno);
        }
        
        buf = "";
        writeTextFile(fptr2, String.format("%s",buf)); //無資料產生空檔
   }
    
    void procFTP(String hdrFileName,String fileFolder) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        String ftpCommand = String.format("mput %s", hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

        if (errCode != 0) {
                showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
                commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
        }
    }
    
	void moveFile(String datFileName1, String fileFolder1) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "/backup" , datFileName1+"_"+hBusiBusinessDate).toString();

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

    // ************************************************************************
    private void updateColLiacDebt1() throws Exception {
        dateTime();
        updateSQL = "proc_date   = ?, proc_flag   = '1', not_send_flag   = 'Y', mod_pgm    = ?, "
                + "mod_time   = sysdate ";
        daoTable = "col_liac_debt";
        whereStr = "WHERE rowid = ?";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCcdtRowid);

        updateTable();

        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liac_debt_1 error", "", hCallBatchSeqno);
        }
    }
}