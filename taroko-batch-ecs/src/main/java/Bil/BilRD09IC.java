/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/24  V1.00.01    JeffKung  program initial                           *
*  112/11/24  V1.00.02    JeffKung  判斷回應碼(Ich)
*  112/11/27  V1.00.03    JeffKung  會計起帳
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

/*國際信用卡清算明細表-愛金卡加值*/
public class BilRD09IC extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-愛金卡加值程式  112/11/27  V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD09IC";
    String prgmName = "列印國際信用卡清算明細表-愛金卡加值程式";
    String rptName = "國際信用卡清算明細表-愛金卡加值";
    String rptId = "BILR_D09_IC";
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

            selectIchOrgdataLog();

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
            printBilRD02TWD();

            //處理票證加值會計帳出帳
            showLogMessage("I", "", "會計起帳....");
            procVouchData();
            procVouchData_A04B();  //處理票證問題交易會計帳出帳
            procVouchData_A09B();  //處理票證餘轉會計帳出帳
            procVouchData_A10B();  //處理票證調帳會計帳出帳
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
    void selectIchOrgdataLog() throws Exception {

    	sqlCmd =  "select org_data,rpt_resp_code ";
		sqlCmd += " from ich_orgdata_log ";
		sqlCmd += "where notify_date = ? ";
		sqlCmd += " and  file_iden = 'A07B' ";
		
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

		closeCursor(cursorIndex);
		
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
        buf = comcr.insertStr(buf, "交易日期", 23);  //8
        buf = comcr.insertStr(buf, "交易時間", 34);  //8
        buf = comcr.insertStr(buf, "交易摘要", 45);  //40
        buf = comcr.insertStr(buf, "交易金額", 88);  //8
        buf = comcr.insertStr(buf, "交易代號", 99);  //8
        buf = comcr.insertStr(buf, "連線註記", 110); //1 + 8
        buf = comcr.insertStr(buf, "處理結果", 122); //1
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
        buf = comcr.insertStr(buf, "自動加值帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 20);
        buf = comcr.insertStr(buf, "筆數:", 65);
        szTmp = String.format("%7d", totalTWDCntDR);
        buf = comcr.insertStr(buf, szTmp, 71);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "自動加值剔退合計:", 1);
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
        String ichCardNo = comc.subMS950String(bytes, 1, 16);
        String purchaseDate = comc.subMS950String(bytes, 82, 8);
        String purchaseTime = comc.subMS950String(bytes, 90, 6);
        String destinationAmt = comc.subMS950String(bytes, 96, 8);
        String trafficAbbr = comc.subMS950String(bytes, 17, 20).trim();
        String addrAbbr = comc.subMS950String(bytes, 37, 20).trim();
        String ichTxCode = comc.subMS950String(bytes, 126, 1);
        String onlineMark = comc.subMS950String(bytes, 127, 1);
        String rtnValue = comc.subMS950String(bytes, 118, 8);
        
        buf = "";
        buf = comcr.insertStr(buf, ichCardNo, 1);  //外顯卡號
        buf = comcr.insertStr(buf, purchaseDate, 23); //交易日期
        buf = comcr.insertStr(buf, purchaseTime, 35); //交易時間
        buf = comcr.insertStr(buf, String.format("%-40.40s",(trafficAbbr+"-"+addrAbbr)), 45); //交易摘要
        buf = comcr.insertStr(buf, comcr.commFormat("3$,3$",comc.str2int(destinationAmt)), 88); //交易金額
        buf = comcr.insertStr(buf, ichTxCode, 99); //交易代號
        buf = comcr.insertStr(buf, (onlineMark+" "+rtnValue), 110); //連線註記
        buf = comcr.insertStr(buf, getValue("rpt_resp_code"), 125); //處理結果
       
        //回覆碼要等於"00000000"才需要入帳 (20231124)
        if(comc.getSubString(rtnValue, 0, 8).equals("00000000") ||
           comc.getSubString(rtnValue, 0, 1).equals("F") ||
           comc.getSubString(rtnValue, 0, 1).equals("C")) {
        	totalTWDDestAmtDR = totalTWDDestAmtDR + comc.str2int(destinationAmt);
        	
        	//若剔退時加總
            if("0".equals(getValue("rpt_resp_code"))==false) {
            	totalTWDDestAmtCR = totalTWDDestAmtCR + comc.str2int(destinationAmt);
            	totalTWDCntCR = totalTWDCntCR + 1;
            } 
        }
        
        totalTWDCntDR = totalTWDCntDR + 1;

        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {

        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款(愛金卡)", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 36, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—愛金卡帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 39, "0", buf));
        
        insertPtrBatchRpt(lparD02TWD);

    }
    
	/***********************************************************************/
	void procVouchData() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		vouchDRAmt[1]  = totalTWDDestAmtDR - totalTWDDestAmtCR;
		vouchCRAmt[4]  = totalTWDDestAmtDR - totalTWDDestAmtCR;

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A001]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilRD09_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_RD09IC";

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
			comcr.hGsvhMemo1 = "愛金卡自動加值";
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
	void procVouchData_A04B() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		sqlCmd =  "select org_data,rpt_resp_code ";
		sqlCmd += " from ich_orgdata_log ";
		sqlCmd += "where notify_date = ? ";
		sqlCmd += " and  file_iden = 'A04B' ";
		
		setString(1,hBusinssDate);

		String exceptionCd = "";
		String txnCd = "";
		String txnAmt = "";
		String respCodeIch = "";
		int destAmt = 0;
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			
            byte[] bytes = getValue("org_data").getBytes("MS950");
            exceptionCd = comc.subMS950String(bytes, 11, 8).trim();
            txnCd = comc.subMS950String(bytes, 35, 2).trim();
            txnAmt = comc.subMS950String(bytes, 99, 8).trim();
            respCodeIch = comc.subMS950String(bytes, 124, 8).trim();
            destAmt = 0;
            
            //剔退交易不處理
            if ("0".equals(getValue("rpt_resp_code"))==false) {
            	continue;
            }
            
            if(comc.getSubString(respCodeIch, 0, 8).equals("00000000") ||
               comc.getSubString(respCodeIch, 0, 1).equals("F") ||
               comc.getSubString(respCodeIch, 0, 1).equals("C")) {
                ;
            } else {
            	continue;
            }
            
            if ("E011".equals(exceptionCd)) {
            	if(txnCd.equals("21") || txnCd.equals("24")) {
            		destAmt = comc.str2int(txnAmt);	
            	} else if (txnCd.equals("22") || txnCd.equals("23")) {
            		destAmt = comc.str2int(txnAmt) * -1;
            	}
            } else {
            	continue;
            }

            vouchDRAmt[1] += destAmt; 
            vouchCRAmt[4] += destAmt;
        }

		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A001]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("D09A04B_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "ICH_A04B";

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
			comcr.hGsvhMemo1 = "愛金卡問題交易";
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
	void procVouchData_A09B() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		sqlCmd =  "select org_data,rpt_resp_code ";
		sqlCmd += " from ich_orgdata_log ";
		sqlCmd += "where notify_date = ? ";
		sqlCmd += " and  file_iden = 'A09B' ";
		
		setString(1,hBusinssDate);

		String txnAmt = "";
		String balanceAmt = "";
		String respCodeIch = "";
		int destAmt = 0;
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			
            byte[] bytes = getValue("org_data").getBytes("MS950");
            txnAmt = comc.subMS950String(bytes, 96, 8).trim();
            balanceAmt = comc.subMS950String(bytes, 104, 8).trim();
            respCodeIch = comc.subMS950String(bytes, 112, 8).trim();

            destAmt = 0;
            
            //剔退交易不處理
            if ("0".equals(getValue("rpt_resp_code"))==false) {
            	continue;
            }
            
            if(comc.getSubString(respCodeIch, 0, 8).equals("00000000") ||
               comc.getSubString(respCodeIch, 0, 1).equals("F") ||
               comc.getSubString(respCodeIch, 0, 1).equals("C")) {
                ;
            } else {
            	continue;
            }
            
            destAmt = comc.str2int(balanceAmt);

            vouchDRAmt[1] += destAmt; 
            vouchCRAmt[1] += destAmt;
        }

		closeCursor(cursorIndex);

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

		tmpstr = String.format("D09A09B_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "ICH_A09B";

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
			comcr.hGsvhMemo1 = "icash卡餘額退回";
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
	void procVouchData_A10B() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		sqlCmd =  "select org_data,rpt_resp_code ";
		sqlCmd += " from ich_orgdata_log ";
		sqlCmd += "where notify_date = ? ";
		sqlCmd += " and  file_iden = 'A10B' ";
		
		setString(1,hBusinssDate);

		String adjustAmt = "";
		String payVendor = "";
		int destAmt = 0;
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			
            byte[] bytes = getValue("org_data").getBytes("MS950");
            adjustAmt = comc.subMS950String(bytes, 87, 10).trim();
            payVendor = comc.subMS950String(bytes, 105, 2).trim();

            destAmt = 0;
            
            //剔退交易不處理
            if ("0".equals(getValue("rpt_resp_code"))==false) {
            	continue;
            }
            
            if("IC".equals(payVendor)) {
            	destAmt = comc.str2int(adjustAmt);  //愛金卡公司付款
            } else {
            	destAmt = comc.str2int(adjustAmt) * -1;  //持卡人付款
            }

            vouchDRAmt[1] += destAmt; 
            vouchCRAmt[1] += destAmt;
        }

		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A028";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A001]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("D09A10B_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "ICH_A10B";

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
			comcr.hGsvhMemo1 = "icash帳務調整";
			comcr.hGsvhMemo2 = "";
			comcr.hGsvhMemo3 = "";

			if ("D".equals(hGsvhDbcr)) {
				drIdx++;
				callVoucherAmt = vouchDRAmt[drIdx];
			} else {
				crIdx++;
				callVoucherAmt = vouchCRAmt[crIdx];
			}
			
			if (callVoucherAmt > 0) {
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
        BilRD09IC proc = new BilRD09IC();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
