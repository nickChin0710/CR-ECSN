/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/01/05  V1.00.00    phopho     program initial                          *
*  107/11/08  V1.01.01    phopho     RECS-s1070329-022 同步新舊批次程式.      *
*  109/12/15  V1.00.02    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.03    Zuwei       “89822222”改為”23317531”            *
*  110/04/06  V1.00.04    Justin     use common value                         *
******************************************************************************/

package Col;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommJcic;
import com.CommRoutine;

import hdata.jcic.JcicEnum;
import hdata.jcic.JcicHeader;
import hdata.jcic.LRPad;

public class ColC035 extends AccessDAO {
    private String progname = "更生資料送JCIC處理程式   110/04/06  V1.00.04 ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_Z55;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCpi comcpi = new CommCpi();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCdlgHolderIdPSeqno = "";
    String hCdlgHolderId = "";
    String hCdlgDocNo = "";
    String hCdlgRenewLiquStatus = "";
    String hCdlgUserTranType = "";
    String hCdlgRowid = "";
    String hCdlgSendTranType = "";
    String hClrwCourtId = "";
    String hClrwCourtDept = "";
    String hClrwSignDocNo = "";
    int hClrwRenewInt = 0;
    double hClrwRenewRate = 0;
    double hClrwOrgDebtAmt = 0;
    double hClrwOrgDebtAmtBef = 0;
    double hClrwRenewLoseAmt = 0;
    String hClrwJudicActionFlag = "";
    String hClrwActionDateS = "";
    String hClrwJudicCancelFlag = "";
    String hClrwCancelDate = "";
    String hClrwConfirmDate = "";
    String hClrwRunRenewFlag = "";
    String hClrwSuperName = "";
    String hClrwRowid = "";
    String hTempInstDateS = "";
    String hTempInstDateE = "";
    String hClrwCaseDate = "";
    String hClrwCaseLetter = "";
    String hClrwCaseLetterDesc = "";  //phopho add
    String hClrwJcicSendFlag = "";  //phopho add
    String hClrwJcicSendDate = "";  //phopho add
    String hClrwPaymentDay = "";
    int totalCnt = 0;
    int hClrwCaseYear = 0;
    int hSendnotdcnt = 0;
    int hSenddcnt = 0;

    private int fptr1 = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ColC035 [business_date] [callbatch_seqno]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            if (args.length >= 1) {
                String sGArgs1 = "";
                sGArgs1 = args[0];
                sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                hBusiBusinessDate = sGArgs1;
            }

            selectPtrBusinday();

            checkOpen();
            selectColLiadLog();
            String buf = String.format("%s%08d%188.188s\n", CommJcic.TAIL_LAST_MARK, totalCnt, " ");
            writeTextFile(fptr1, String.format("%s",buf));
            closeOutputText(fptr1);
            ftpProc();

            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        selectSQL = "decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /***********************************************************************/
    void selectColLiadLog() throws Exception {
        String tmpstr = "";
        String tmpstr1 = "";

        sqlCmd = "select ";
        sqlCmd += "holder_id_p_seqno,";
        sqlCmd += "holder_id,";
        sqlCmd += "doc_no,";
        sqlCmd += "renew_liqu_status,";
        sqlCmd += "user_tran_type,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liad_log ";
        sqlCmd += "where liad_type = '1' ";
        sqlCmd += "and event_type = 'S' ";
        sqlCmd += "and proc_flag = 'N' ";
        sqlCmd += "and renew_liqu_status!='7' "; /*狀態7.更生駁回不報送JCIC*/
        sqlCmd += " order by holder_id,doc_no,renew_liqu_status,crt_date,crt_time ";

        openCursor();
        while (fetchTable()) {
        	hCdlgHolderIdPSeqno = getValue("holder_id_p_seqno");
        	hCdlgHolderId = getValue("holder_id");
            hCdlgDocNo = getValue("doc_no");
            hCdlgRenewLiquStatus = getValue("renew_liqu_status");
            hCdlgUserTranType = getValue("user_tran_type");
            hCdlgRowid = getValue("rowid");

            totalCnt++;
            if (totalCnt % 5000 == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if (hCdlgUserTranType.length() != 0) {
                hCdlgSendTranType = hCdlgUserTranType;
            } else {
                if (selectColLiadLog1() != 0) {
                    hCdlgSendTranType = "A";
                }
            }
            selectColLiadRenew();
            hTempInstDateS = "";
            hTempInstDateE = "";
            if ((comc.getSubString(hCdlgRenewLiquStatus, 0, 1).equals("3")) && (hClrwConfirmDate.length() != 0)) {
                selectColLiadInstallS();
                selectColLiadInstallE();
            }
            tmpstr = "";
            if (hClrwCaseDate.length() != 0)
                tmpstr = String.format("%07d", comcr.str2long(hClrwCaseDate)-19110000);
            String buf = String.format("55%1.1s%s%-10.10s%1.1s%7.7s%5.5s", hCdlgSendTranType, CommJcic.JCIC_BANK_NO, hCdlgHolderId,
                    hCdlgRenewLiquStatus, tmpstr, " ");
            writeTextFile(fptr1, String.format("%s",buf));

            //批次修改需求v3.6 【col_c035 更生資料送JCIC處理程式】、【col_c036 清算資料送JCIC處理程式】，
            //兩支批次中，以case_letter_desc 代替 sign_doc_no，且需要限制為40個字 (因為case_letter_desc
            //資料庫中的長度可至 110)。此欄位內容中，有英數字者，需要改為全型。 2019.10.16 phopho
//            tmpstr = String.format("%s", h_clrw_sign_doc_no);
            tmpstr = String.format("%s", hClrwCaseLetterDesc);
            tmpstr = comcpi.commTransChinese(tmpstr);  //轉全型
            
//            buf = String.format("%-3.3s%03d%-4.4s%-40.40s", h_clrw_court_id, h_clrw_case_year - 1911, h_clrw_court_dept, tmpstr);
            buf = String.format("%-3.3s%03d%s%s", hClrwCourtId, hClrwCaseYear - 1911,
            		comc.fixLeft(hClrwCourtDept,4), comc.fixLeft(tmpstr,40));
            writeTextFile(fptr1, String.format("%s",buf));

            tmpstr = "";
            if (hTempInstDateS.length() != 0)
                tmpstr = String.format("%07d", comcr.str2long(hTempInstDateS)-19110000);
            tmpstr1 = "";
            if (hTempInstDateE.length() != 0)
                tmpstr1 = String.format("%07d", comcr.str2long(hTempInstDateE)-19110000);

            buf = String.format("%7.7s%7.7s", tmpstr, tmpstr1);
            writeTextFile(fptr1, String.format("%s",buf));

            tmpstr = "";
            if (hClrwActionDateS.length() != 0)
                tmpstr = String.format("%07d", comcr.str2long(hClrwActionDateS)-19110000);
            tmpstr1 = "";
            if (hClrwCancelDate.length() != 0)
                tmpstr1 = String.format("%07d", comcr.str2long(hClrwCancelDate)-19110000);

//            buf = String.format("%03d%05.2f%09.0f%09.0f%-1.1s%7.7s%1.1s%7.7s%1.1s%-10.10s%54.54s\n", 
//            		h_clrw_renew_int, h_clrw_renew_rate, h_clrw_org_debt_amt_bef, /*h_clrw_org_debt_amt,*/ 
//            		h_clrw_renew_lose_amt, h_clrw_judic_action_flag, tmpstr, 
//                    h_clrw_judic_cancel_flag, tmpstr1, h_clrw_run_renew_flag, h_clrw_super_name, " ");
            
            buf = String.format("%03d%05.2f%09.0f%09.0f%-1.1s%7.7s%1.1s%7.7s%1.1s%s%54.54s\n",
                    hClrwRenewInt, hClrwRenewRate, hClrwOrgDebtAmtBef, /*h_clrw_org_debt_amt,*/
                    hClrwRenewLoseAmt, hClrwJudicActionFlag, tmpstr,
                    hClrwJudicCancelFlag, tmpstr1, hClrwRunRenewFlag, comc.fixLeft(hClrwSuperName,10), " ");
            writeTextFile(fptr1, String.format("%s",buf));
            
            //批次修改需求v3.6 回寫更生/清算主檔的【jcic_send_date】欄位的method，修改相關邏輯內容。
            selectColLiadLog2();  //2019.10.16 phopho
            updateColLiadRenew();  //2019.9.26 phopho
            updatColLiadLog();
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectColLiadLog1() throws Exception {
        sqlCmd = "select decode(send_tran_type,'D','A','C') h_cdlg_send_tran_type ";
        sqlCmd += " from col_liad_log  ";
//        sqlCmd += "where holder_id   = ?  ";
        sqlCmd += "where holder_id_p_seqno = ? ";
        sqlCmd += "and  doc_no = ? ";
        sqlCmd += "and  renew_liqu_status = ? ";
        sqlCmd += "and  liad_type   = '1' ";
        sqlCmd += "and  event_type  = 'S' ";
        sqlCmd += "and  proc_flag   = 'Y' order by crt_date desc,crt_time desc ";
//        setString(1, h_cdlg_holder_id);
        setString(1, hCdlgHolderIdPSeqno);
        setString(2, hCdlgDocNo);
        setString(3, hCdlgRenewLiquStatus);
        
        extendField = "col_liad_log_1.";
        
        if (selectTable() > 0) {
            hCdlgSendTranType = getValue("col_liad_log_1.h_cdlg_send_tran_type");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    int selectColLiadRenew() throws Exception {
        hClrwCaseDate = "";
        hClrwCourtId = "";
        hClrwCaseYear = 0;
        hClrwCourtDept = "";
        hClrwSignDocNo = "";
        hClrwRenewInt = 0;
        hClrwRenewRate = 0;
        hClrwOrgDebtAmt = 0;
        hClrwRenewLoseAmt = 0;
        hClrwJudicActionFlag = "";
        hClrwActionDateS = "";
        hClrwJudicCancelFlag = "";
        hClrwCancelDate = "";
        hClrwConfirmDate = "";
        hClrwCaseLetter = "";
        hClrwCaseLetterDesc = "";
        hClrwJcicSendFlag = "";
        hClrwJcicSendDate = "";
        hClrwPaymentDay = "";
        hClrwRunRenewFlag = "";
        hClrwSuperName = "";
        hClrwRowid = "";

        /*sunny note1:當狀態為2時renew_cancel_date改放case_date*/  
        sqlCmd = "select decode(renew_status,'2',case_date, '4',deliver_date, '6',case_date,judic_date) as case_date,";
        sqlCmd += "court_id,";
        sqlCmd += "case_year,";
        sqlCmd += "court_dept,";
        sqlCmd += "sign_doc_no,";
        sqlCmd += "renew_int,";
        sqlCmd += "renew_rate,";
        sqlCmd += "org_debt_amt,";
        sqlCmd += "org_debt_amt_bef,";  /* sunny add*/
        sqlCmd += "renew_lose_amt,";
        sqlCmd += "decode(judic_action_flag,'','N',judic_action_flag) as judic_action_flag,";
        sqlCmd += "action_date_s,";
        sqlCmd += "decode(judic_cancel_flag,'','N',judic_cancel_flag) as judic_cancel_flag,";
        sqlCmd += "cancel_date,";
        sqlCmd += "confirm_date,";
        sqlCmd += "case_letter,";
        sqlCmd += "case_letter_desc,";
        sqlCmd += "jcic_send_flag,";
        sqlCmd += "jcic_send_date,";
        sqlCmd += "run_renew_flag,";
        sqlCmd += "super_name,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from col_liad_renew ";
//        sqlCmd += "where id_no   = ?  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and  liad_doc_no = ? ";
        sqlCmd += "and  renew_status = ? order by recv_date desc ";
//        setString(1, h_cdlg_holder_id);
        setString(1, hCdlgHolderIdPSeqno);
        setString(2, hCdlgDocNo);
        setString(3, hCdlgRenewLiquStatus);
        
        extendField = "col_liad_renew.";
        
        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hClrwCaseDate = getValue("col_liad_renew.case_date");
        hClrwCourtId = getValue("col_liad_renew.court_id");
        hClrwCaseYear = getValueInt("col_liad_renew.case_year");
        hClrwCourtDept = getValue("col_liad_renew.court_dept");
        hClrwSignDocNo = getValue("col_liad_renew.sign_doc_no");
        hClrwRenewInt = getValueInt("col_liad_renew.renew_int");
        hClrwRenewRate = getValueDouble("col_liad_renew.renew_rate");
        hClrwOrgDebtAmt = getValueDouble("col_liad_renew.org_debt_amt");
        hClrwOrgDebtAmtBef = getValueDouble("col_liad_renew.org_debt_amt_bef");  /*sunny add*/
        hClrwRenewLoseAmt = getValueDouble("col_liad_renew.renew_lose_amt");
        hClrwJudicActionFlag = getValue("col_liad_renew.judic_action_flag");
        hClrwActionDateS = getValue("col_liad_renew.action_date_s");
        hClrwJudicCancelFlag = getValue("col_liad_renew.judic_cancel_flag");
        hClrwCancelDate = getValue("col_liad_renew.cancel_date");
        hClrwConfirmDate = getValue("col_liad_renew.confirm_date");
        hClrwCaseLetter = getValue("col_liad_renew.case_letter");
        hClrwCaseLetterDesc = getValue("col_liad_renew.case_letter_desc");
        hClrwJcicSendFlag = getValue("col_liad_renew.jcic_send_flag");
        hClrwJcicSendDate = getValue("col_liad_renew.jcic_send_date");
        hClrwRunRenewFlag = getValue("col_liad_renew.run_renew_flag");
        hClrwSuperName = getValue("col_liad_renew.super_name");
        hClrwRowid = getValue("col_liad_renew.rowid");

        return 0;
    }

    /***********************************************************************/
    void selectColLiadInstallS() throws Exception {
        hTempInstDateS = "";
        sqlCmd = "select substr(inst_date_s,1,6)|| decode(sign(payment_day-to_char(last_day(sysdate),'dd')),1, to_char(last_day(sysdate),'dd'), to_char(to_date(payment_day,'dd'),'dd')) h_temp_inst_date_s ";
        sqlCmd += " from col_liad_install  ";
//        sqlCmd += "where holder_id = ?  ";
        sqlCmd += "where holder_id_p_seqno = ? ";
        sqlCmd += "and  case_letter = ? ";
//        sqlCmd += "and  inst_seq = (select min(inst_seq) from col_liad_install where holder_id = ?  ";
        sqlCmd += "and  inst_seq = (select min(inst_seq) from col_liad_install where holder_id_p_seqno = ? ";
        sqlCmd += "and  case_letter = ?)  ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_cdlg_holder_id);
        setString(1, hCdlgHolderIdPSeqno);
        setString(2, hClrwCaseLetter);
        setString(3, hCdlgHolderIdPSeqno);
        setString(4, hClrwCaseLetter);
        
        extendField = "col_liad_install_s.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liad_install_s not found", "", comcr.hCallBatchSeqno);
        }
        hTempInstDateS = getValue("col_liad_install_s.h_temp_inst_date_s");
    }

    /***********************************************************************/
    void selectColLiadInstallE() throws Exception {
        hTempInstDateE = "";
        sqlCmd = "select substr(inst_date_s,1,6)|| decode(sign(payment_day-to_char(last_day(sysdate),'dd')),1, to_char(last_day(sysdate),'dd'), to_char(to_date(payment_day,'dd'),'dd')) h_temp_inst_date_e ";
        sqlCmd += " from col_liad_install  ";
//        sqlCmd += "where holder_id = ?  ";
        sqlCmd += "where holder_id_p_seqno = ? ";
        sqlCmd += "and  case_letter = ? ";
//        sqlCmd += "and  inst_seq = (select max(inst_seq) from col_liad_install where holder_id = ?  ";
        sqlCmd += "and  inst_seq = (select max(inst_seq) from col_liad_install where holder_id_p_seqno = ? ";
        sqlCmd += "and  case_letter = ?)  ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_cdlg_holder_id);
        setString(1, hCdlgHolderIdPSeqno);
        setString(2, hClrwCaseLetter);
        setString(3, hCdlgHolderIdPSeqno);
        setString(4, hClrwCaseLetter);
        
        extendField = "col_liad_install_e.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liad_install_e not found", "", comcr.hCallBatchSeqno);
        }
        hTempInstDateE = getValue("col_liad_install_e.h_temp_inst_date_e");
    }

    /***********************************************************************/
    void selectColLiadLog2() throws Exception {
    	//sendNotDCnt、sendDCnt 相減，若為0，表示【報送後，再送出D，等於沒有報送】
    	//sendNotDCnt、sendDCnt 相減，若 >0，表示【有報送】
    	hSendnotdcnt = 0;
		hSenddcnt = 0;

//    	sqlCmd = "Select count(*)  as sendNotDCnt ";
//    	sqlCmd += " FROM   col_liad_log ";
//    	sqlCmd += "WHERE   doc_no            = ? ";
//    	sqlCmd += "  AND   liad_type         = '1' ";   //(1表示為更生)
//    	sqlCmd += "  AND   event_type        = 'S' ";   //(S表示為報送)
//    	sqlCmd += "  AND   user_tran_type    <> 'D' ";  //(表示報送非為D)
//    	setString(1, h_cdlg_doc_no);
//    	extendField = "col_liad_log_2_1.";
//    	if (selectTable() > 0) {
//    		h_sendNotDCnt = getValueInt("col_liad_log_2_1.sendNotDCnt");
//        }
//    	  
//    	sqlCmd = "Select count(*)  as sendDCnt ";
//    	sqlCmd += " FROM   col_liad_log ";
//    	sqlCmd += "WHERE   doc_no            = ? ";
//    	sqlCmd += "  AND   liad_type         = '1' "; 
//    	sqlCmd += "  AND   event_type        = 'S' "; 
//    	sqlCmd += "  AND   user_tran_type    = 'D' ";
//    	setString(1, h_cdlg_doc_no);
//    	extendField = "col_liad_log_2_1.";
//    	if (selectTable() > 0) {
//    		h_sendDCnt = getValueInt("col_liad_log_2_1.sendDCnt");
//        }
    	
    	sqlCmd = "Select sum(decode(user_tran_type,'D',0,1)) as sendNotDCnt, ";
    	sqlCmd += "      sum(decode(user_tran_type,'D',1,0)) as sendDCnt ";
    	sqlCmd += " FROM col_liad_log ";
    	sqlCmd += "WHERE doc_no     = ? ";
    	sqlCmd += "  AND liad_type  = '1' "; 
    	sqlCmd += "  AND event_type = 'S' "; 
    	setString(1, hCdlgDocNo);
    	
    	extendField = "col_liad_log_2.";
    	
    	if (selectTable() > 0) {
    		hSendnotdcnt = getValueInt("col_liad_log_2.sendNotDCnt");
    		hSenddcnt = getValueInt("col_liad_log_2.sendDCnt");
        }
    	
//    	1. 若原來主檔資料的 jcic_send_flag == 'Y'
//		   a. 如果 (sendNotDCnt - sendDCnt = 0)  
//		         則 col_liad_renew.jcic_send_flag = ''
//		            col_liad_renew.jcic_send_date = ''
//		   b. 如果 (sendNotDCnt - sendDCnt > 0)
//		         則 col_liad_renew.jcic_send_flag = 'Y' (或者不變更也可以，反正已經是Y)
//		            若 col_liad_renew.jcic_send_date 非為空值，就不變更
//					若 col_liad_renew.jcic_send_date 為空值，就填入系統日
//					
//		2. 若原來主檔資料的 jcic_send_flag <> 'Y'
//		   a. 如果 (sendNotDCnt - sendDCnt = 0)  
//		         則 col_liad_renew.jcic_send_flag = ''
//		            col_liad_renew.jcic_send_date = ''
//		   b. 如果 (sendNotDCnt - sendDCnt > 0)
//		         則 col_liad_renew.jcic_send_flag = 'Y'
//		            col_liad_renew.jcic_send_date = 系統日
    	
    	if (hClrwJcicSendFlag.equals("Y")) {
    		if ((hSendnotdcnt - hSenddcnt) == 0) {
    			hClrwJcicSendFlag = "";
    			hClrwJcicSendDate = "";
    		} else if ((hSendnotdcnt - hSenddcnt) > 0) {
    			if (hClrwJcicSendDate.equals("")) hClrwJcicSendDate = sysDate;
    		}
    	} else {
    		if ((hSendnotdcnt - hSenddcnt) == 0) {
    			hClrwJcicSendFlag = "";
    			hClrwJcicSendDate = "";
    		} else if ((hSendnotdcnt - hSenddcnt) > 0) {
    			hClrwJcicSendFlag = "Y";
    			hClrwJcicSendDate = sysDate;
    		}
    	}
    }
    
    /***********************************************************************/
    void updateColLiadRenew() throws Exception {
        daoTable = "col_liad_renew";
        updateSQL = "jcic_send_flag = ?,";
        updateSQL += " jcic_send_date = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where rowid   = ? ";
//        setString(1, h_busi_business_date);
        setString(1, hClrwJcicSendFlag);
        setString(2, hClrwJcicSendDate);
        setString(3, javaProgram);
        setRowId(4, hClrwRowid);
        
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_renew not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updatColLiadLog() throws Exception {
        daoTable = "col_liad_log";
        updateSQL = "proc_flag  = 'Y',";
        updateSQL += " proc_Date  = ?,";
        updateSQL += " send_tran_type = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where rowid   = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hCdlgSendTranType);
        setString(3, javaProgram);
        setRowId(4, hCdlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liad_log not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        String temstr = String.format("%s/media/col/LIAD/%s%4.4sa.055", comc.getECSHOME(), CommJcic.JCIC_BANK_NO, comc.getSubString(hBusiBusinessDate,4));
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

        fptr1 = openOutputText(temstr, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr), "", hCallBatchSeqno);
        }

        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(comcr.str2long(hBusiBusinessDate) - 19110000, "0", 7, LRPad.L));
        jcicHeader.setFileExt("01");
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 80));
        jcicHeader.setFiller3(commJcic.getFiller(" ", 59));
        jcicHeader.setLen("");
        
        String buf = jcicHeader.produceStr();
        
//        String buf = String.format("%-18.18s017%5.5s%07d01%10.10s%-16.16s%-80.80s%59.59s\n", 
//        		"JCIC-DAT-Z055-V01-", " ", comcr.str2long(hBusiBusinessDate) - 19110000, " ",
//        		"02-23317531#1108", "審查單位聯絡人－卡務催收經辦", " ");
        
        writeTextFile(fptr1, String.format("%s",buf));
    }

    /***********************************************************************/
    void ftpProc() throws Exception {
        /**********
         * COMM_FTP common function usage
         ****************************************/
        String tojcicmsg = "";
        boolean retCode;
        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

//        commFTP.h_eflg_trans_seqno = String.format("%010d", comr.getSeqno("ECS_MODSEQ")); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "COL_LIAD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "055"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/col/LIAD", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "JCIC_FTP";
        String procCode = "";
        System.setProperty("user.dir", commFTP.hEriaLocalDir);
        procCode = String.format("put %s%4.4sa.055", CommJcic.JCIC_BANK_NO, comc.getSubString(hBusiBusinessDate,4));

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s]檔案傳送JCIC_FTP有誤(error), 請通知相關人員處理", procCode));
        }
        
        //=================================================================
        commFTP.hEflgSourceFrom = "合作金庫"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        hEflgRefIpCode = "DWFTP_IP";
        errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s]檔案傳送DW_FTP有誤(error), 請通知相關人員處理", procCode));

            /*** SENDMSG ***/
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"ColC035執行完成 傳送JCIC失敗[%s]\"", hEflgRefIpCode);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, retCode));
        } else {
            /*** SENDMSG ***/
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"ColC035執行完成 傳送JCIC無誤[%s]\"", hEflgRefIpCode);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, retCode));
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC035 proc = new ColC035();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
