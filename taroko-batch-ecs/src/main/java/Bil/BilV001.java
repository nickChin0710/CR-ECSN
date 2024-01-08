/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/21  V1.00.01    JeffKung  program initial                           *
 *  112/12/04  V1.00.02    JeffKung  區分ATM及櫃檯預借現金         
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
public class BilV001 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "BIL會計起帳處理  112/12/04 V1.00.02" ;
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
                comc.errExit("Usage : BilV001, this program need only one parameter  ", "");
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
            showLogMessage("I", "", String.format("BIL會計分錄開始......."));
            comcr.vouchPageCnt = 0;
            comcr.rptSeq       = 0;
            pgmName = String.format("BilV001");

            //學雜費
            showLogMessage("I", "", String.format("學雜費......."));
            procVouchData01();
            
            //VD清算(正向)
            showLogMessage("I", "", String.format("VD清算(正向)......."));
            procVouchData02();
            
            //VD清算(負向)
            showLogMessage("I", "", String.format("VD清算(負向)......."));
            procVouchData03();
            
            //信用卡台幣清算(正向)
            showLogMessage("I", "", String.format("信用卡台幣清算(正向)......."));
            procVouchData04();
            
            //信用卡美金清算(正向)
            showLogMessage("I", "", String.format("信用卡美金清算(正向)......."));
            procVouchData04USD();
            
            //信用卡日幣清算(正向)
            showLogMessage("I", "", String.format("信用卡日幣清算(正向)......."));
            procVouchData04JPY();
            
            //信用卡台幣清算(負向)
            showLogMessage("I", "", String.format("信用卡台幣清算(負向)......."));
            procVouchData05();
            
            //信用卡美金清算(負向)
            showLogMessage("I", "", String.format("信用卡美金清算(負向)......."));
            procVouchData05USD();
            
            //信用卡日幣清算(負向)
            showLogMessage("I", "", String.format("信用卡日幣清算(負向)......."));
            procVouchData05JPY();
            
            //NCCC分期付款(正向)
            showLogMessage("I", "", String.format("NCCC分期付款(正向)......."));
            procVouchData06();
            
            //NCCC分期付款(負向)
            showLogMessage("I", "", String.format("NCCC分期付款(負向)......."));
            procVouchData07();
            
            //ATM預借現金(正向)
            showLogMessage("I", "", String.format("ATM預借現金(正向)......."));
            procVouchData08();
            
            //櫃檯預借現金(正向)
            showLogMessage("I", "", String.format("櫃檯預借現金(正向)......."));
            procVouchData08_1();
            
            //自行收單請款(正向+負向)-A005
            showLogMessage("I", "", String.format("自行收單請款(正向+負向)...."));
            procVouchData09();
            
            //自行分期付款(正向)-A008
            showLogMessage("I", "", String.format("自行分期付款(正向)......."));
            procVouchData11();
            
            //自行分期付款(負向)-A009
            showLogMessage("I", "", String.format("自行分期付款(負向)......."));
            procVouchData12();


            //信用卡台幣清算(再提示正向)
            showLogMessage("I", "", String.format("信用卡台幣清算(再提示正向)......."));
            procVouchData13();
            
            //信用卡美金清算(再提示正向)
            showLogMessage("I", "", String.format("信用卡美金清算(再提示正向)......."));
            procVouchData13USD();
            
            //信用卡日幣清算(再提示正向)
            showLogMessage("I", "", String.format("信用卡日幣清算(再提示正向)......."));
            procVouchData13JPY();
            
            //信用卡台幣清算(再提示負向)
            showLogMessage("I", "", String.format("信用卡台幣清算(再提示負向)......."));
            procVouchData14();
            
            //信用卡美金清算(再提示負向)
            showLogMessage("I", "", String.format("信用卡美金清算(再提示負向)......."));
            procVouchData14USD();
            
            //信用卡日幣清算(再提示負向)
            showLogMessage("I", "", String.format("信用卡日幣清算(再提示負向)......."));
            procVouchData14JPY();
            
            // ==============================================
            // 固定要做的
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
	void procVouchData01() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where ecs_platform_kind in('b1','G2') and this_close_date = ? ";
		sqlCmd += " union all ";
		sqlCmd += "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from dbb_curpost ";
		sqlCmd += "where ecs_platform_kind in('b1','G2') and this_close_date = ? ";
		
		setString(1,hBusiBusinessDate);
		setString(2,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			}

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

		tmpstr = String.format("BilV001_%s.%s_%s","93", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R1";

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
			comcr.hGsvhMemo1 = "";
			comcr.hGsvhMemo2 = "";
			comcr.hGsvhMemo3 = "";

			if ("D".equals(hGsvhDbcr)) {
				drIdx++;
				callVoucherAmt = vouchDRAmt[drIdx];
			} else {
				crIdx++;
				callVoucherAmt = vouchCRAmt[crIdx];
			}
			
			switch (hGsvhDbcrSeq) {
            case 1:
            	comcr.hGsvhMemo1 = "中國信託學費/財金學費";
            	break;
            case 3:
            	comcr.hGsvhMemo1 = "財金學費";
            	break;
            case 4:
            	comcr.hGsvhMemo1 = "中國信託學費";
                break;
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

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from dbb_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and settl_flag in ('0','6','8') ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dest_amt");
			}

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

		tmpstr = String.format("BilV001_%s.%s_%s","D02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R2";

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
			comcr.hGsvhMemo1 = "VISA DEBIT";
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

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from dbb_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and settl_flag in ('0','6','8') ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
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

		tmpstr = String.format("BilV001_%s.%s_%s","D02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R3";

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
			comcr.hGsvhMemo1 = "VD退貨";
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
	void procVouchData04() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*不含雙幣卡的外幣
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and ( (curr_code = '901' and settl_flag = '0') ";
		sqlCmd += "        or settl_flag in ('6','8') ) ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dest_amt");
			}

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R4";

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
			comcr.hGsvhMemo1 = "購貨";
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
	void procVouchData04USD() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*雙幣卡美金
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dc_amount ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and curr_code = '840' ";
		sqlCmd += " and settl_flag = '0' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dc_amount");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dc_amount");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dc_amount");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dc_amount");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "840";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R4U";

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
			comcr.hGsvhMemo1 = "雙幣購貨-USD";
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
	void procVouchData04JPY() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*雙幣卡美金
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dc_amount ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and curr_code = '392' ";
		sqlCmd += " and settl_flag = '0' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dc_amount");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dc_amount");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dc_amount");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dc_amount");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "392";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R4J";

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
			comcr.hGsvhMemo1 = "雙幣購貨-JPY";
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
	void procVouchData05() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and ( (curr_code = '901' and settl_flag = '0') ";
		sqlCmd += "        or settl_flag in ('6','8') ) ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R5";

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
			comcr.hGsvhMemo1 = "退貨";
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
	void procVouchData05USD() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dc_amount ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and curr_code = '840' ";
		sqlCmd += " and settl_flag = '0' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dc_amount");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dc_amount");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dc_amount");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dc_amount");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
		String currCode = "840";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R5U";

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
			comcr.hGsvhMemo1 = "雙幣退貨-USD";
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
	void procVouchData05JPY() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dc_amount ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and curr_code = '392' ";
		sqlCmd += " and settl_flag = '0' ";

		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dc_amount");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dc_amount");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dc_amount");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dc_amount");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
		String currCode = "392";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R5J";

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
			comcr.hGsvhMemo1 = "雙幣退貨-JPY";
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
	void procVouchData06() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*NCCC分期付款正向
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " and settl_flag = '8' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A024";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R6";

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
			comcr.hGsvhMemo1 = "NCCC分期付款";
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
	void procVouchData07() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*NCCC分期付款退貨
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " and settl_flag = '8' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A025";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R7";

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
			comcr.hGsvhMemo1 = "NCCC分期付款退貨";
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
	void procVouchData08() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*ATM預借現金
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and txn_code = '07' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and settl_flag = '9' ";
		sqlCmd += " and mcht_category = '6011' ";   //ATM預借
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("9".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A026";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BIL_V001R8";

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
			comcr.hGsvhMemo1 = "ATM預借現金";
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
	void procVouchData08_1() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*櫃檯預借現金
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and txn_code = '07' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and settl_flag = '9' ";
		sqlCmd += " and mcht_category <> '6011' ";   //櫃檯預借
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("9".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A026";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R81";

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
			comcr.hGsvhMemo1 = "櫃檯預借現金";
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
	void procVouchData09() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*自行收單請款 (購貨和退貨相減，以淨額出帳)
		 */
		sqlCmd =  "select card_no,settl_flag,sign_flag, ";
		sqlCmd += " decode(sign_flag,'-',dest_amt*-1,dest_amt) dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and acct_code <> 'CA' "; //排除預借現金
		sqlCmd += " and settl_flag = '9' ";
		sqlCmd += " union all ";
		sqlCmd +=  "select card_no,settl_flag,sign_flag, ";
		sqlCmd += " decode(sign_flag,'-',dest_amt*-1,dest_amt) dest_amt ";
		sqlCmd += " from dbb_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " and acct_code <> 'CA' "; //排除預借現金
		sqlCmd += " and settl_flag = '9' ";
		
		setString(1,hBusiBusinessDate);
		setString(2,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("9".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} 

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A005";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R09";

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
			comcr.hGsvhMemo1 = "本行信用卡交易";
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
	void procVouchData11() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*自行分期付款正向
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '+' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " and settl_flag = '9' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");

			if ("9".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A008";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R11";

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
			comcr.hGsvhMemo1 = "本行信用卡交易-分期";
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
	void procVouchData12() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*自行分期付款退貨
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and sign_flag = '-' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " and settl_flag = '9' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[1] += getValueDouble("dest_amt");

			if ("9".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A009";
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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R12";

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
			comcr.hGsvhMemo1 = "本行信用卡交易-分期退貨";
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
	void procVouchData13() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示正向(台幣)
		 */
		sqlCmd =  "select card_no,settl_flag,round(dest_amt) dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('65','67','86') ";
		sqlCmd += " and  dest_curr = '901' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dest_amt");
			}

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R13";

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
			comcr.hGsvhMemo1 = "再提示";
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
	void procVouchData13USD() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示正向(美金)
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('65','67','86') ";
		sqlCmd += " and  dest_curr = '840' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "840";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV01R13U";

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
			comcr.hGsvhMemo1 = "再提示-USD";
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
	void procVouchData13JPY() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示正向(日幣)
		 */
		sqlCmd =  "select card_no,settl_flag,round(dest_amt) dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('65','67','86') ";
		sqlCmd += " and  dest_curr = '392' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchCRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchCRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchCRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A001";
		String currCode = "392";

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

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV01R13J";

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
			comcr.hGsvhMemo1 = "再提示-JPY";
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
	void procVouchData14() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示負向(台幣)
		 */
		sqlCmd =  "select card_no,settl_flag,round(dest_amt) dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('66','85','87') ";
		sqlCmd += " and  dest_curr = '901' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A002]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV001R14";

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
			comcr.hGsvhMemo1 = "再提示沖正";
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
	void procVouchData14USD() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示負向(美金)
		 */
		sqlCmd =  "select card_no,settl_flag,dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('66','85','87') ";
		sqlCmd += " and  dest_curr = '840' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
		String currCode = "840";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A002]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV01R14U";

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
			comcr.hGsvhMemo1 = "再提示沖正-USD";
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
	void procVouchData14JPY() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*再提示負向(日幣)
		 */
		sqlCmd =  "select card_no,settl_flag,round(dest_amt) dest_amt,dest_curr ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('66','85','87') ";
		sqlCmd += " and  dest_curr = '392' ";
		
		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchCRAmt[2] += getValueDouble("dest_amt");

			if ("6".equals(getValue("settl_flag"))) {
				vouchDRAmt[1] += getValueDouble("dest_amt");
			} else if ("8".equals(getValue("settl_flag"))) {
				vouchDRAmt[2] += getValueDouble("dest_amt");
			} else if ("0".equals(getValue("settl_flag"))) {
				vouchDRAmt[3] += getValueDouble("dest_amt");
			}

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "A002";
		String currCode = "392";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[A002]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV001_%s.%s_%s","C02", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV01R14J";

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
			comcr.hGsvhMemo1 = "再提示沖正-JPY";
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

        BilV001 proc = new BilV001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
