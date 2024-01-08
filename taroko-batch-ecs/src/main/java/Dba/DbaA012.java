/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/07/12  V1.00.00    Brian     program initial                           *
 *  109/05/25  V1.00.01    Pino      for TCB layout                            *
 *  109/07/03  V1.00.02    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.03    shiyuqi     coding standard,                   *
 *  109/09/11  V1.00.04    JeffKung change data source
 *  109/09/23  V1.00.05    JeffKung open file in text mode
 *  109/09/25  V1.00.06    JeffKung proc_code!='00' update deduct_amt=0
 *  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *
 *  111/02/14  V1.00.08    Ryan      big5 to MS950                                           *
 *  111/03/23  V1.00.09    JeffKung 調整至換日後執行                                           *
 *  111/11/21  V1.00.10    JeffKung 判斷檔案為空檔時,正常結束程式                                *
 *  112/06/08  V1.00.11    JeffKung 會計起帳處理                                                             *
 ******************************************************************************/

package Dba;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*VD扣款回覆處理(舊系統)程式*/
public class DbaA012 extends AccessDAO {
    private final String progname = "VD扣款回覆處理(舊系統)程式 112/06/08 V1.00.11";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    
    String   modUser               = "";
    String   hDatnModUser        = "";
    String   hDatnModPgm         = "";
    int      inta                   = 0;
    String   hVouchCdKind        = "";
    String   hGsvhModPgm         = "";
    String   tmpstr                 = "";
    String   hBusiBusinessDate   = "";
    String   hPreBusinessDate  = "";  //V1.00.09
    String   hProcDate = "";  //V1.00.09
    String   hCallBatchSeqno     = "";
    long     hDetlReceiveSucCnt = 0;
    long     hDetlReceiveFalCnt = 0;
    long     hDetlReceiveCnt     = 0;
    double   hDetlReceiveAmt = 0;
    double   hDetlReceiveSucAmt = 0;
    double   hDetlReceiveFalAmt = 0;
    long     hDetlSendCnt        = 0;
    String   inputFileName         = "";
    String   text = "";
	byte[]   bytesArr = null;
    String   str600                 = "";
    double[] tTotalAmt            = new double[13];
    String   hDetlCreateDate     = "";
    String   hDetlReceiveDate    = "";
    String   hDetlDeductDate     = "";

    double hDatnDeductAmt       = 0;
    int    tempInt                = 0;
    String hDatnReferenceNo     = "";
    String hTempAcctNo          = "";
    String hDatnPSeqno          = "";
    String hDatnAcctType        = "";
    String hDatnCardNo          = "";
    String hDatnIdPSeqno       = "";
    String hDatnId               = "";
    String hDatnIdCode          = "";
    String hDatnDebtStatus      = "";
    String hDatnTransColDate   = "";
    String hDatnTransBadDate   = "";
    String hDatnPurchaseDate    = "";
    String hDatnItemPostDate   = "";
    String hDatnStmtCycle       = "";
    String hDatnAcctItemEname  = "";
    double hDatnOrgDeductAmt   = 0;
    String hDatnFromCode        = "";
    String hDatnRowid            = "";
    String hDatnDeductProcCode = "";
    String hDatnAcctNo          = "";
    String hDatnDeductSeq       = "";
    String hBusiVouchDate       = "";
    double hDebtEndBal          = 0;
    double hDebtDAvailableBal  = 0;  
    String hDetlMediaSum  = ""; 
    String hDetlRowid      = "";
    String hTempReceiveDate = "";
    
    
    String hPcceCurrEngName = "";
    String hPcceCurrChiName = "";
    String hPcceCurrCodeGl  = "";
    String hPccdGlcode      = "";
    String hGsvhAcNo        = "";
    String hGsvhDbcr        = "";

    String hAccmMemo3Kind   = "";
    String hAccmMemo3Flag   = "";
    String hAccmDrFlag      = "";
    String hAccmCrFlag      = "";

    int    hGsvhDbcrSeq     = 0;
    double callVoucherAmt   = 0;
    
    String buf              = "";
    String szTmp            = "";
    String rptName          = "VD繳款銷帳明細報表(舊系統)";
    String rptId            = "report01";
    int    rptSeq           = 0;
    int    pageCnt          = 0;
    int    lineCnt          = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : DbaA002 [fileDate]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            modUser = comc.commGetUserID();
            hDatnModUser = modUser;
            hDatnModPgm = javaProgram;
            selectPtrBusinday();
            checkOpen(args);
            for (inta = 0; inta < 13; inta++)
                tTotalAmt[inta] = 0;
            checkProcess();
            if (tTotalAmt[1] > 0) {
                hVouchCdKind = "G001";
                hGsvhModPgm = javaProgram;
                vouchRtn();       
            }
            //updateActMasterBal();

            showLogMessage("I", "", String.format("媒體總筆數[%d]  成功[%d] 失敗[%d]!"
            		,hDetlReceiveCnt,hDetlReceiveSucCnt,hDetlReceiveFalCnt));
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

    /*************************************************************************/
    int checkOpen(String[] args) throws Exception {
        if (args.length == 0) {
        	hProcDate = hPreBusinessDate;
        } else {
        	hProcDate = args[0];
        }

        inputFileName = String.format("%s/media/dba/VDTC20.%8.8s", comc.getECSHOME() , hProcDate);
        showLogMessage("I", "", "檔案處理日期=[" +hProcDate + "]");
        
        return 0;

    }

	/**************************************************************************/
	void checkProcess() throws Exception {
		String stra = "";
		String hCardNo = "";
		int hDeductAmt = 0;
		String hDeductDate = "";
		long failCnt = 0, successCnt = 0;
		double successAmt = 0, tmpAmt = 0;

		hDetlReceiveCnt = 0;
		hDetlReceiveAmt = 0;
		hDetlReceiveSucCnt = 0;
		hDetlReceiveSucAmt = 0;
		hDetlReceiveFalCnt = 0;
		hDetlReceiveFalAmt = 0;

		int readlen = 0;

		if (openInputText(inputFileName, "MS950") == -1) {
			exceptExit = 0;
			comcr.errRtn(String.format("[%s]目前無資料需處理", inputFileName), "", hCallBatchSeqno);
		}
		while (true) {
			text = readTextFile(0);
			bytesArr = text.getBytes("MS950");

			// 讀到檔尾
			if ("Y".equals(endFile[0])) {
				showLogMessage("I", "", String.format("[%s]檔案處理完畢", inputFileName));
				break;
			}

			hCardNo = comc.subMS950String(bytesArr, 0, 16);
			hDeductAmt = comc.str2int(comc.subMS950String(bytesArr, 16, 13));
			int tmpDate = comc.str2int(comc.subMS950String(bytesArr, 33, 7));
			hDeductDate = String.format("%8d",(tmpDate + 19110000));
			
			hDetlReceiveCnt++;
			hDetlReceiveAmt = hDetlReceiveAmt + hDeductAmt;
			
			if(selectDbcCard(hCardNo)==0) {
				hDetlReceiveSucCnt++;
				hDetlReceiveSucAmt = hDetlReceiveSucAmt + hDeductAmt;
				insertDbaJrnl(hCardNo,hDeductDate,hDeductAmt);
				
				tTotalAmt[1] = tTotalAmt[1] + hDeductAmt;
				tTotalAmt[2] = tTotalAmt[2] + hDeductAmt;
			}

			if (lineCnt == 0) {
				printHeader();
				lineCnt++;
			}

			if (lineCnt > 45) {
				lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
				printHeader();
				lineCnt = 0;
			}

			lineCnt++;

			buf = "";
			buf = comcr.insertStr(buf, hDeductDate, 1);
			buf = comcr.insertStr(buf, "BL-代墊款", 11);
			buf = comcr.insertStr(buf, hCardNo, 40);
			szTmp = comcr.commFormat("3$,3$,3$,3$", hDeductAmt);
			buf = comcr.insertStr(buf, szTmp, 66);
			buf = comcr.insertStr(buf, comc.fixLeft("繳款銷帳", 20), 85);

			lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		}
		closeInputText(0);

		if (hDetlReceiveCnt > 0) {
			printFooter();
			String ftpName = String.format("%s_DBAA012.%s_%s", rptId, sysDate, hBusiBusinessDate);
			String filename = String.format("%s/reports/%s_DBAA012.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusiBusinessDate);
			comc.writeReport(filename, lpar1);
			ftpMput(ftpName);
		}
	}
    /*************************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        hBusiVouchDate = "";
        hPreBusinessDate = "";

        sqlCmd  = " select business_date,  to_char( to_date(business_date, 'yyyymmdd') - 1 DAYS , 'yyyymmdd') as prev_business_date , ";
        sqlCmd += "        vouch_date ";
        sqlCmd += " from   ptr_businday ";
        sqlCmd += " fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hPreBusinessDate = getValue("prev_business_date");
        hBusiVouchDate = getValue("vouch_date");
        
        showLogMessage("I", "", "本日營業日期=" +hBusiBusinessDate);
        
    }
    
    /**************************************************************************/
    int selectDbcCard(String cardNo) throws Exception {
    	
    	extendField = "DbcCard.";
        sqlCmd = " SELECT  p_seqno, acct_no, acct_type,";
        sqlCmd += "        id_p_seqno, stmt_cycle, ";
        sqlCmd += "        'dummy' as dummy ";
        sqlCmd += "  from  dbc_card ";
        sqlCmd += "  where card_no = ? ";
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("","","select DbcCard not found! card_no=" + cardNo);
        	return -1;
        }
        return 0;
    }
    /*************************************************************************/
    void insertDbaJrnl(String hCardNo, String hDeductDate, int hDeductAmt) throws Exception {
        daoTable = "dba_jrnl";
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("p_seqno", getValue("DbcCard.p_seqno"));
        setValue("acct_type", getValue("DbcCard.acct_type"));
        setValue("acct_no", getValue("DbcCard.acct_no"));
        setValue("id_p_seqno", getValue("DbcCard.id_p_seqno"));
        setValue("deduct_seq", "old_system");
        setValue("debt_status", "");
        setValue("trans_col_date", "");
        setValue("trans_bad_date", "");
        setValue("acct_date", hBusiBusinessDate);
        setValue("tran_class", "D");
        setValue("tran_type", "VD");
        setValue("acct_code", "BL");
        setValue("dr_cr", "D");
        setValueDouble("transaction_amt", hDeductAmt);
        setValue("purchase_date", hDeductDate);
        setValue("item_post_date", hBusiBusinessDate);
        setValue("item_date", hBusiBusinessDate);
        setValue("reference_no", "");
        setValue("stmt_cycle", getValue("DbcCard.stmt_cycle"));
        setValue("card_no", hCardNo);
        setValue("pay_id", "");
        setValueDouble("jrnl_bal", 0);
        setValueDouble("item_bal", 0);
        setValueDouble("item_d_bal", 0);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dba_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

	/*****************************************************************************/
	void vouchRtn() throws Exception {

		// 會科套號
		hVouchCdKind = "G001";

		String currCode = "901";
		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[" + hVouchCdKind + "]");
			return;
		}

		comcr.hGsvhCurr = "00";

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("DbaA012_%s.%s_%s", "D02", hVouchCdKind, hPcceCurrCodeGl);
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "DBAA012";

		for (int i = 0; i < itemCnt; i++) {
			hGsvhAcNo = getValue("ac_no", i);
			hGsvhDbcrSeq = getValueInt("dbcr_seq", i);
			hGsvhDbcr = getValue("dbcr", i);
			hAccmMemo3Kind = getValue("memo3_kind", i);
			hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
			hAccmDrFlag = getValue("h_accm_dr_flag", i);
			hAccmCrFlag = getValue("h_accm_cr_flag", i);

			/* Memo 1, Memo 2, Memo3 */
			comcr.hGsvhMemo1 = "VD繳款銷帳";
			comcr.hGsvhMemo2 = "";
			comcr.hGsvhMemo3 = "";

			callVoucherAmt = tTotalAmt[i + 1];

			if (callVoucherAmt != 0) {
				if (comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCodeGl) != 0) {
					showLogMessage("E", "", "call detail_vouch error, AcNo=[" + hGsvhAcNo + "]");
					return;
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
    
    /*****************************************************************************/
    void updateActMasterBal() throws Exception {
        daoTable = "act_master_bal";
        updateSQL = " vd_deduct_amt  = decode(cast(? as varchar(8)),?,0,?), ";
        updateSQL += "               mod_user       = ?, ";
        updateSQL += "               mod_time       = sysdate, ";
        updateSQL += "               mod_pgm        = ? ";
        whereStr = "   where check_date     = ? ";
        whereStr += "   and   curr_code      = '901'  ";
        setString(1, hBusiVouchDate);
        setString(2, hDetlDeductDate);
        setDouble(3, hDetlReceiveSucAmt);
        setString(4, hDatnModUser);
        setString(5, hDatnModPgm);
        setString(6, hDetlDeductDate);
        updateTable();

        if (notFound.equals("Y")) {
            daoTable = "act_master_bal";
            setValue("check_date", hDetlDeductDate);
            setValue("curr_code", "901");
            setValueDouble("vd_deduct_amt", hBusiVouchDate.equals(hDetlDeductDate) ? 0 : hDetlReceiveSucAmt);
            setValue("mod_user", hDatnModUser);
            setValue("mod_time", sysDate + sysTime);
            setValue("mod_pgm", hDatnModPgm);
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_act_master_bal duplicate!", "", hCallBatchSeqno);
            }

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
            buf = comcr.insertStr(buf, "備    註"       , 110);
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
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

            buf = "";
            buf = comcr.insertStr(buf, "代墊款:"  ,  21);
            szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
            buf = comcr.insertStr(buf, szTmp      ,  48);
            szTmp = comcr.commFormat("3$,3$,3$,3$", tTotalAmt[2]);
            buf = comcr.insertStr(buf, szTmp      ,  72);
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
            
            buf = "";
            buf = comcr.insertStr(buf, "手續費:"  ,  21);
            szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
            buf = comcr.insertStr(buf, szTmp      ,  48);
            szTmp = comcr.commFormat("3$,3$,3$,3$", tTotalAmt[4]);
            buf = comcr.insertStr(buf, szTmp      ,  72);
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
            
            buf = "";
            buf = comcr.insertStr(buf, "合計:"  ,  21);
            szTmp = comcr.commFormat("3$,3$,3$,3$", 0);
            buf = comcr.insertStr(buf, szTmp      ,  48);
            szTmp = comcr.commFormat("3$,3$,3$,3$", tTotalAmt[1]);
            buf = comcr.insertStr(buf, szTmp      ,  72);
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
        DbaA012 proc = new DbaA012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
