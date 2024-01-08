/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/27  V1.00.01    JeffKung  program initial                           *
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

/*會計起帳(BIL)處理*/
public class BilV003 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "BIL會計起帳(分期付款)處理  112/03/28 V1.00.01" ;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 0;

    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
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
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilV003, this program need only one parameter  ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            String runDate = "";
            if (args.length == 1) {
            	runDate = args[0];
            }
            	
            selectPtrBusinday(runDate);
            
            /*******************************************************************************/
            showLogMessage("I", "", String.format("BilV003會計分錄開始......."));
            comcr.vouchPageCnt = 0;
            comcr.rptSeq       = 0;
            pgmName = String.format("BilV003");

            //分期付款成立本金(A006)
            showLogMessage("I", "", String.format("分期付款成立本金(A006)......."));
            procVouchData00();
            
            //分期付款期金(A007)
            showLogMessage("I", "", String.format("分期付款期金(A007)......."));
            procVouchData01();
            
            //分期付款手續費(XXXX)
            showLogMessage("I", "", String.format("分期付款手續費(B004)......."));
            procVouchData02();
            
            //分期付款利息(B003)
            showLogMessage("I", "", String.format("分期付款利息(B003)......."));
            procVouchData03();
            
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
        hTempVouchDate = "";
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
            hTempVouchDate = getValue("vouch_date");
            hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
            hBusiVouchDate = getValue("h_busi_vouch_date");
        }
        
        showLogMessage("I", "", String.format("本日營業日期=[%s]",hBusiBusinessDate));
        
        if (runDate.length() == 8) {
        	hBusiBusinessDate = runDate;
        }

        showLogMessage("I", "", String.format("程式處理日期=[%s]",hBusiBusinessDate));

    }
    
	/***********************************************************************/
	void procVouchData00() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*分期付款契約成立入帳
		 */
		sqlCmd =  "select card_no,tot_amt,install_tot_term ";
		sqlCmd += " from bil_contract ";
		sqlCmd += "where first_post_date >= ? ";
		sqlCmd += " and  new_proc_date = ? ";
		sqlCmd += " and  install_curr_term <= 1 ";
		sqlCmd += " and  (mcht_no LIKE '1060000000%' OR ptr_mcht_no LIKE '1060000000%' ) ";
		
		setString(1,hBusiBusinessDate);
		setString(2,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("tot_amt");
			
			vouchCRAmt[1] += getValueDouble("tot_amt");
			
		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A006";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A007]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV003_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV03R00";

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
			comcr.hGsvhMemo1 = "分期付款本金";
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
	void procVouchData01() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*分期付款期金
		 */
		sqlCmd =  "select card_no,dest_amt,curr_code,txn_code ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and  bill_type = 'OICU' ";
		sqlCmd += " and  txn_code in ('IN') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";

		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");
			
			vouchCRAmt[1] += getValueDouble("dest_amt");
			
		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A007";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A007]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV003_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV03R01";

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
			comcr.hGsvhMemo1 = "分期付款期金";
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
	void procVouchData02() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*分期手續費
		 */
		sqlCmd =  "select card_no,dest_amt,curr_code,txn_code ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and  bill_type = 'OICU' ";
		sqlCmd += " and  txn_code in ('PF') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");
			
			vouchCRAmt[1] += getValueDouble("dest_amt");
		
		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "B004";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[B004]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV003_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV03R02";

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
			comcr.hGsvhMemo1 = "分期手續費";
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
	void procVouchData03() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*分期利息
		 */
		sqlCmd =  "select card_no,dest_amt,curr_code,txn_code ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and  bill_type = 'OICU' ";
		sqlCmd += " and  txn_code in ('IF','RI') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");
			
			vouchCRAmt[1] += getValueDouble("dest_amt");
			
		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "B003";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[B003]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV003_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV03R03";

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
			comcr.hGsvhMemo1 = "分期利息";
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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        BilV003 proc = new BilV003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
