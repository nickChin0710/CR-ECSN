/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/11/20  V1.00.01    Brian     error correction                          *
 *  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
 *  110/07/13  V1.00.03    castor    add Debit-card process                    *
 *  112/05/02  V1.00.04    Alex      改寫黑名單規則改為TCB版本                                                                   *
 *  112/09/19  V1.00.05    Wilson    調整讀取資料邏輯                                                                                      *
 *  112/09/28  V1.00.06    Wilson    增加讀取其他停用碼                                                                                  *
 *  112/10/04  V1.00.07    Wilson    增加讀取凍結碼38                               *
 *  112/10/06  V1.00.08    Wilson    增加讀取同業強停(其他停用-E2)                     *
 *  112/10/12  V1.00.09    Wilson    增加讀取卡特指&所有凍結碼                                                                    *
 *  112/10/20  V1.00.10    Wilson    調整讀取毀損補發邏輯
 *  2024-0104 V1.00.11     JH        凍結:--rsk_problem
 ******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
//import com.CommFunction;

/*悠遊卡黑名單檔資料處理程式*/
public class TscR001 extends AccessDAO {

    private final String progname = "悠遊卡黑名單檔資料處理程式  2024-0104 V1.00.11";
//    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";
    String hBkecCrtDate = "";
    String hBkecCrtTime = "";
    String hTardTscCardNo = "";
    String hTardCardNo = "";
    String hCardPSeqno = "";
    String hBkexBlackFlag = "";
    String hBkexBlackDate = "";
    String hTardCurrentCode = "";
    String hTardOppostDate = "";
    String hTardBlackltSDate = "";
    String hBkecFromMark = "";
    String hTardTscSignFlag = "";
    String hBkecFromCrdKind = "";
    int intCnt = 0;
//    String hRcpmCardLostCond = "";
//    String hRcpmCardOppoCond = "";
//    String hRcpmMcodeCond = "";
//    String hRcpmPaymentRate = "";
//    double hRcpmMcodeAmt = 0;
//    String hRcpmBkecBlockCond = "";
//    String hRcpmBkecBlockReason = "";
//    int tempInt = 0;
//    double tempDouble = 0;
//    String isBlockReason = "";

    int totCnt = 0;
    String seqMark = "";
    String wfValueP2 = "";

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
                comc.errExit("Usage : TscR001", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            
            selectPtrSysParmP2();
            
            selectTscCard();
            
            showLogMessage("I", "", String.format("Process records = [%d][%d]", totCnt, intCnt));
            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_bkec_crt_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_bkec_crt_time  ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hBkecCrtDate = getValue("h_bkec_crt_date");
            hBkecCrtTime = getValue("h_bkec_crt_time");
        }

    }

    /***********************************************************************/
	void selectPtrSysParmP2() throws Exception {
		sqlCmd = "SELECT WF_VALUE3 FROM PTR_SYS_PARM WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'ROLLBACK_P2'";
		int parmCnt = selectTable();
		if (parmCnt > 0) {
			wfValueP2 = getValue("WF_VALUE3");
			wfValueP2 = comcr.increaseDays(wfValueP2.substring(0,6) + "01",-7);
		}
				
		showLogMessage("I","","wfValueP2 = [ " + wfValueP2 + " ]");
	}
	
    /***********************************************************************/
    void selectTscCard() throws Exception {

    	     //人工-強制報送、黑名單(信用卡)-排序1
        sqlCmd = " select A.tsc_card_no , A.card_no , '' as acno_p_seqno , B.black_flag , B.black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '1' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
        	   + " from tsc_card A join tsc_bkec_expt B on A.tsc_card_no = B.tsc_card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and ( (B.black_flag ='2') or (B.black_flag = '1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e))) "
        	   + " union "
        	 //人工-強制報送、黑名單(VD卡)-排序1
        	   + " select A.tsc_card_no , A.vd_card_no as card_no , '' as acno_p_seqno , B.black_flag , B.black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '1' as mark , A.tsc_sign_flag , 'dbc' as crd_kind,'' as oppost_date "
        	   + " from tsc_vd_card A join tsc_bkec_expt B on A.tsc_card_no = B.tsc_card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and ( (B.black_flag ='2') or (B.black_flag = '1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e))) "
        	   + " union "
        	 //人工-強制報送已餘轉(信用卡)-排序1
        	   + " select A.tsc_card_no , A.card_no , '' as acno_p_seqno , B.black_flag , B.black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '1' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
        	   + " from tsc_card A join tsc_bkec_expt B on A.tsc_card_no = B.tsc_card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.autoload_flag = 'Y' "
        	   + " and b.black_flag = '4' "
        	   + " union "
        	 //人工-強制報送已餘轉(VD卡)-排序1
        	   + " select A.tsc_card_no , A.vd_card_no as card_no , '' as acno_p_seqno , B.black_flag , B.black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '1' as mark , A.tsc_sign_flag , 'dbc' as crd_kind,'' as oppost_date "
        	   + " from tsc_vd_card A join tsc_bkec_expt B on A.tsc_card_no = B.tsc_card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.autoload_flag = 'Y' "
        	   + " and b.black_flag = '4' "
        	   + " union "
          	 //問題交易檔-排序2
      	       + " select A.tsc_card_no , A.card_no , '' as acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '2' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
      	       + " from tsc_card A join tsc_prtn_log C on A.tsc_card_no = C.tsc_card_no "
      	       + " where A.new_end_date > ? and A.lock_date = '' "
      	       + " and not exists (select B.tsc_card_no from tsc_bkec_expt B where B.tsc_card_no = A.tsc_card_no and ((B.black_flag ='2') or (B.black_flag ='1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e)))) "        	   
      	       + " union "    	   
        	 //強停、系統停用、凍結碼38-排序3
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '3' as mark , A.tsc_sign_flag , 'crd' as crd_kind,C.oppost_date "	
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no join cca_card_acct D on C.acno_p_seqno = D.acno_p_seqno "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and ( (C.current_code ='3') or (C.current_code <> '0' and C.oppost_reason in ('AX','AP','T1','F1','E4','AA','E2')) "
        	   + "        or (D.block_reason1 = '38') or (D.block_reason2 = '38') or (D.block_reason3 = '38') or (D.block_reason4 = '38') or (D.block_reason5 = '38') ) "
        	   + " and not exists (select B.tsc_card_no from tsc_bkec_expt B where B.tsc_card_no = A.tsc_card_no and ((B.black_flag ='2') or (B.black_flag ='1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e)))) "
        	   + " union "
        	 //逾期-排序4
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '4' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no join act_acno D on C.acno_p_seqno = D.acno_p_seqno "
        	   + "      join act_acct E on C.p_seqno = E.p_seqno "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and D.INT_RATE_MCODE >= 1 "
        	   + " and not exists (select B.tsc_card_no from tsc_bkec_expt B where B.tsc_card_no = A.tsc_card_no and ((B.black_flag ='2') or (B.black_flag ='1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e)))) "
        	   + " union "
        	 //凍結碼-排序5
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '5' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no join cca_card_acct D on C.acno_p_seqno = D.acno_p_seqno "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and (D.block_reason1 || D.block_reason2 || D.block_reason3 || D.block_reason4 || D.block_reason5 <> '') "
            +" AND ('38' NOT IN (D.block_reason1,D.block_reason2,D.block_reason3,D.block_reason4,D.block_reason5)) "
        	   + " union "
        	 //卡特指-排序6
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '5' as mark , A.tsc_sign_flag , 'crd' as crd_kind,'' as oppost_date "
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no join cca_card_base D on C.card_no = D.card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and D.spec_status <> '' "
        	   + " union "
        	 //掛失轉卡、毁損補發-排序7  
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '6' as mark , A.tsc_sign_flag , 'crd' as crd_kind,C.oppost_date "
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and ( (C.current_code ='2' and C.oppost_date >= ?) or (A.current_code ='6') or (A.current_code ='4') ) "
        	   + " and not exists (select B.tsc_card_no from tsc_bkec_expt B where B.tsc_card_no = A.tsc_card_no and ((B.black_flag ='2') or (B.black_flag ='1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e)))) "
        	   + " union "    	   
        	 //其他停用碼-排序8 
        	   + " select A.tsc_card_no , A.card_no , C.acno_p_seqno , '' as black_flag , '' as black_date , A.current_code , A.oppost_date , A.blacklt_s_date , '7' as mark , A.tsc_sign_flag , 'crd' as crd_kind,C.oppost_date "
        	   + " from tsc_card A join crd_card C on A.card_no = C.card_no "
        	   + " where A.new_end_date > ? and A.lock_date = '' and A.return_date = '' and A.balance_date = '' and A.autoload_flag = 'Y' "
        	   + " and C.current_code in ('1','4','5') and C.oppost_reason not in ('AX','AP','T1','F1','E4','AA','S1') "
        	   + " and not exists (select B.tsc_card_no from tsc_bkec_expt B where B.tsc_card_no = A.tsc_card_no and ((B.black_flag ='2') or (B.black_flag ='1' and to_char(sysdate,'yyyymmdd') between decode(B.send_date_s, '', '19000101', B.send_date_s) and decode(B.send_date_e, '', '29991231', B.send_date_e)))) "
        	   + " order by 9 , 11 , 12 DESC "
        	   ;
    	
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        setString(7, hBusiBusinessDate);
        setString(8, hBusiBusinessDate);
        setString(9, hBusiBusinessDate);
        setString(10, hBusiBusinessDate);
        setString(11, wfValueP2);
        setString(12, hBusiBusinessDate);
        int cursorIndex = openCursor();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_card not found!", "", hCallBatchSeqno);
        }

        while (fetchTable(cursorIndex)) {
            hTardTscCardNo = getValue("tsc_card_no");
            hTardCardNo = getValue("card_no");
            hCardPSeqno = getValue("acno_p_seqno");
            hBkexBlackFlag = getValue("black_flag");
            hBkexBlackDate = getValue("black_date");
            hTardCurrentCode = getValue("current_code");
            hTardOppostDate = getValue("oppost_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hBkecFromMark = getValue("mark");
            hTardTscSignFlag = getValue("tsc_sign_flag");
            hBkecFromCrdKind = getValue("crd_kind");

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process record=[%d]", totCnt));
            
            if (inserTtscBkecLog() != 0)
                continue;

            if (hBkecFromCrdKind.equals("crd")) {
                updateTscCard();
                intCnt++;
         	}
         	else if (hBkecFromCrdKind.equals("dbc")) {
                updateTscVdCard();
                intCnt++;
         	}        
        }
        closeCursor(cursorIndex);

    }
    
    /***********************************************************************/
    int inserTtscBkecLog() throws Exception {
        daoTable = "tsc_bkec_log";
        setValue("crt_date", hBkecCrtDate);
        setValue("crt_time", hBkecCrtTime);
        setValue("tsc_card_no", hTardTscCardNo);
        setValue("current_code", hTardCurrentCode);
        setValue("oppost_date", hTardOppostDate);
        setValue("from_mark", hBkecFromMark);
        setValue("proc_flag", "N");
        setValueInt("order_seqno", intCnt);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscCard() throws Exception {
        daoTable = "tsc_card";
        updateSQL = "blacklt_flag    = 'Y',";
        updateSQL += " blacklt_s_date = decode( blacklt_s_date, '', ?, decode( blacklt_e_date, '', blacklt_s_date, ? )),";
        updateSQL += " blacklt_e_date = '',";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr = "where tsc_card_no = ? ";
        setString(1, hBkecCrtDate);
        setString(2, hBkecCrtDate);
        setString(3, javaProgram);
        setString(4, hTardTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    void updateTscVdCard() throws Exception {
        daoTable = "tsc_vd_card";
        updateSQL = "blacklt_flag    = 'Y',";
        updateSQL += " blacklt_s_date = decode( blacklt_s_date, '', ?, decode( blacklt_e_date, '', blacklt_s_date, ? )),";
        updateSQL += " blacklt_e_date = '',";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr = "where tsc_card_no = ? ";
        setString(1, hBkecCrtDate);
        setString(2, hBkecCrtDate);
        setString(3, javaProgram);
        setString(4, hTardTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscR001 proc = new TscR001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
