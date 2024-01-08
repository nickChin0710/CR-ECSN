/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/12/18  V1.00.01    Ryan      program initial                           *
 ******************************************************************************/

package Cms;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;

/*會計起帳(BIL)處理*/
public class CmsC020 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "cmsm020貴賓室會計分錄處理程式  112/12/18  V1.00.01" ;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate comd = new CommDate();
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
                comc.errExit("Usage : CmsC020, this program need only one parameter  ", "");
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
            comcr.vouchPageCnt = 0;
            comcr.rptSeq       = 0;
            pgmName = String.format("CmsC020");

            //機場貴賓室使用費處理
            showLogMessage("I", "", String.format("機場貴賓室使用費處理......."));
            procMktThsrDisc01();
            
            
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
	void procMktThsrDisc01() throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		/*
		 */
		sqlCmd =  " SELECT sum(ch_cost_amt+guest_cost_amt) as dest_amt ";
		sqlCmd += " from cms_ppcard_visit ";
		sqlCmd += " where ERR_CODE='00' and (ch_visits + guests_count - free_use_cnt - guest_free_cnt)>0 ";
		sqlCmd += " AND substr(visit_date,1,6) =? ";
		setString(1,comd.monthAdd(hBusiBusinessDate, -1));
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			vouchDRAmt[1] += getValueDouble("dest_amt");
			vouchCRAmt[1] += getValueDouble("dest_amt");

		}
		closeCursor(cursorIndex);

		// 會科套號
		hVouchCdKind = "F011";
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[F011]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("CmsC020_%s.%s_%s","F011", hVouchCdKind, hPcceCurrCodeGl);  //CRD93繳學費分錄
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "CMS_C020R1";

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
			
			comcr.hGsvhMemo1 = "機場貴賓室使用費";

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

        CmsC020 proc = new CmsC020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
