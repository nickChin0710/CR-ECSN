/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/24  V1.00.01    JeffKung  program initial                           *
*  112/11/27  V1.00.02    JeffKung  起會計帳
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*國際信用卡清算明細表-VD悠遊卡掛失餘轉*/
public class BilRD09DCBD extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-VD悠遊卡掛失餘轉程式  112/11/27  V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD09DCBD";
    String prgmName = "列印國際信用卡清算明細表-VD悠遊卡掛失餘轉程式";
    String rptName = "國際信用卡清算明細表-VD悠遊卡掛失餘轉";
    String rptId = "D09_DCBD";
    int rptSeq = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
    String rptNameD02TWD = "國際信用卡清算彙計表-信用卡TWD";
    String rptIdD02TWD = "CRD02_TWD";
    int rptSeqD02TWD = 0;
    List<Map<String, Object>> lparD02TWD = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;

    double totalTWDDestAmtCR = 0;
    double totalTWDDestAmtDR = 0;
   
    int totalTWDCntCR = 0;
    int totalTWDCntDR = 0;

    int lineCnt = 0;
    
    String hTempVouchDate = "";
    String hTempVouchChiDate = "";
    String hBusiVouchDate = "";
    String chiDate = "";

    String hVouchCdKind = "";
    String hGsvhAcNo = "";
    String hGsvhDbcr = "";

    String hAccmMemo3Kind = "";
    String hAccmMemo3Flag = "";
    String hAccmDrFlag = "";
    String hAccmCrFlag = "";

    String pgmName = "";
    String hPcceCurrEngName = "";
    String tmpstr = "";
    String hPcceCurrChiName = "";
    String hPcceCurrCodeGl = "";
    String hPccdGlcode = "";

    int hGsvhDbcrSeq = 0;
    double callVoucherAmt = 0;

    int seqCnt = 1;

    private int maxvouchCRAmtLength = 10;
    double[] vouchCRAmt = new double[maxvouchCRAmtLength];
    double[] vouchDRAmt = new double[maxvouchCRAmtLength];

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            }
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");

            selectTscOrgdataLog();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]"+ pageCnt);

            if (pageCnt > 0) {

                String ftpName = String.format("%s.%s_%s", rptId, sysDate, hBusinssDate);
                String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusinssDate);
                //改為線上報表
                comc.writeReport(filename, lpar1);
                //comcr.insertPtrBatchRpt(lpar1);
                
                ftpMput(ftpName);
            }
            
            commitDataBase();
            
            //處理CRD02_TWD報表
            //printBilRD02TWD();

            //處理票證掛失餘轉會計帳出帳
            procVouchData();
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        hBusinssDate = "";
        hTempVouchDate = "";
        hTempVouchChiDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += " vouch_date,";
        sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
        sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_busi_vouch_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
            hTempVouchDate = getValue("vouch_date");
            hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
            hBusiVouchDate = getValue("h_busi_vouch_date");
        }
    }

    /***********************************************************************/
    void selectTscOrgdataLog() throws Exception {

    	sqlCmd =  "select org_data,rpt_resp_code ";
		sqlCmd += " from tsc_orgdata_log ";
		sqlCmd += "where notify_date = ? ";
		sqlCmd += " and  file_iden = 'DCBD' ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeader();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
            }
            
            printDetail(getValue("org_data"));
        }

        if (indexCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void printHeader() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId, 1);
        buf = comcr.insertStrCenter(buf, rptName, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, "入帳日 :", 20);
        buf = comcr.insertStr(buf, hBusinssDate, 30);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "外顯卡號", 1);   //20
        buf = comcr.insertStr(buf, "掛卡日期", 23);  //8
        buf = comcr.insertStr(buf, "", 34);  //8
        buf = comcr.insertStr(buf, "交易摘要", 45);  //40
        buf = comcr.insertStr(buf, "退款金額", 88);  //8
        buf = comcr.insertStr(buf, "回應代碼", 99);  //4
        buf = comcr.insertStr(buf, "處理結果", 110); //4
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
        buf = comcr.insertStr(buf, "掛失餘轉帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 20);
        buf = comcr.insertStr(buf, "筆數:", 65);
        szTmp = String.format("%7d", totalTWDCntDR);
        buf = comcr.insertStr(buf, szTmp, 71);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "掛失餘轉剔退合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 20);
        buf = comcr.insertStr(buf, "筆數:", 65);
        szTmp = String.format("%7d", totalTWDCntCR);
        buf = comcr.insertStr(buf, szTmp, 71);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }

    /***********************************************************************/
    void printDetail(String orgData) throws Exception {
        lineCnt++;
        indexCnt++;
        
        byte[] bytes = orgData.getBytes("MS950");
        
        String tscCardNo = comc.subMS950String(bytes, 3, 20);
        String tranAmt6h = comc.subMS950String(bytes, 23, 13);
        String tranFee6h = comc.subMS950String(bytes, 36, 13);
        String tranAmt = comc.subMS950String(bytes, 49, 13);
        String tranDate = comc.subMS950String(bytes, 62, 8);
        String tsccRespCode = comc.subMS950String(bytes, 70, 4);
        String tranAmt0h = comc.subMS950String(bytes, 74, 13);

        int intTranAmt6h = comc.str2int(tranAmt6h);
        int intTranFee6h = comc.str2int(tranFee6h);
        int intTranAmt   = comc.str2int(tranAmt);
        int inttranAmt0h = comc.str2int(tranAmt0h);

        buf = "";
        buf = comcr.insertStr(buf, tscCardNo, 1);  //外顯卡號
        buf = comcr.insertStr(buf, tranDate, 23); //交易日期
        buf = comcr.insertStr(buf, "", 35); //交易時間
        buf = comcr.insertStr(buf, "掛失卡片餘額轉置", 45); //交易摘要
        buf = comcr.insertStr(buf, comcr.commFormat("3$,3$",intTranAmt), 88); //交易金額
        buf = comcr.insertStr(buf, tsccRespCode, 101); //交易代號
        buf = comcr.insertStr(buf, getValue("rpt_resp_code"), 110); //處理結果
       
        totalTWDDestAmtDR = totalTWDDestAmtDR + intTranAmt;
        totalTWDCntDR = totalTWDCntDR + 1;

        //若剔退時加總
        if("0000".equals(getValue("rpt_resp_code"))==false) {
        	totalTWDDestAmtCR = totalTWDDestAmtCR + intTranAmt;
        	totalTWDCntCR = totalTWDCntCR + 1;
        } 
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {

        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款(悠遊卡)", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 34, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—悠遊卡帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 37, "0", buf));
        
        insertPtrBatchRpt(lparD02TWD);

    }
    
	/***********************************************************************/
	void procVouchData() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		vouchDRAmt[1]  = totalTWDDestAmtDR;
		vouchCRAmt[1]  = totalTWDDestAmtDR;

		// 會科套號
		hVouchCdKind = "A028";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A028]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("D09DCBD_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_DCBD";

		int drIdx = 0;
		int crIdx = 0;

		for (int i = 0; i < itemCnt; i++) {
			hGsvhAcNo = getValue("ac_no", i);
			hGsvhDbcrSeq = getValueInt("dbcr_seq", i);
			hGsvhDbcr = getValue("dbcr", i);
			hAccmMemo3Kind = getValue("memo3_kind", i);
			hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
			hAccmDrFlag = getValue("h_accm_dr_flag", i);
			hAccmCrFlag = getValue("h_accm_cr_flag", i);

			/* Memo 1, Memo 2, Memo3 */
			comcr.hGsvhMemo1 = "VD悠遊餘額退回-掛失";
			comcr.hGsvhMemo2 = "";
			comcr.hGsvhMemo3 = "";

			if ("D".equals(hGsvhDbcr)) {
				drIdx++;
				callVoucherAmt = vouchDRAmt[drIdx];
			} else {
				crIdx++;
				callVoucherAmt = vouchCRAmt[crIdx];
			}
			
			if (callVoucherAmt != 0) {
				if (comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCodeGl) != 0) {
					showLogMessage("E", "", "call detail_vouch error, AcNo=[" + hGsvhAcNo + "]");
					return;
				}
			}

		}

	}
	
	/***********************************************************************/
    void selectPtrCurrcode(String currCode) throws Exception {
        hPcceCurrEngName = "";
        hPcceCurrChiName = "";
        hPcceCurrCodeGl = "";
        sqlCmd = "select curr_eng_name,";
        sqlCmd += " curr_chi_name,";
        sqlCmd += " curr_code_gl ";
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
            hPcceCurrCodeGl = getValue("curr_code_gl");
        }

    }
    
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
    
    int insertPtrBatchRpt(List<Map<String, Object>> lpar) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        String tmpStr = hBusinssDate + "000002";
        for (int i = 0; i < lpar.size(); i++) {
            if (tmpStr.length() > 8) {
                setValue("start_date", tmpStr.substring(0, 8));
                setValue("start_time", tmpStr.substring(8));
            } else {
                setValue("start_date", tmpStr.substring(0));
                setValue("start_time", "");
            }
            setValue("program_code", lpar.get(i).get("prgmId").toString());
            setValue("rptname", lpar.get(i).get("prgmName").toString());
            setValue("seq", lpar.get(i).get("seq").toString());
            setValue("kind", lpar.get(i).get("kind").toString());
            setValue("report_content", lpar.get(i).get("content").toString());

            daoTable = "ptr_batch_rpt";
            insertTable();
            if (dupRecord.equals("Y")) {
                return 0;
            }
        }
        noTrim = "";
        return actCnt;
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
        BilRD09DCBD proc = new BilRD09DCBD();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
