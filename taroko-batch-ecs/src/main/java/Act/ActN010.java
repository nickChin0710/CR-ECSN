/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/25  V1.00.01    Brian     error correction                          *
 *  110/10/08  V1.00.02    Brian     mantis#8854 修正報表異動日期              *
 *  111/10/12  V1.00.03  jiangyigndong  updated for project coding standard    *
 *  112/03/11  V1.00.04    Simon     1."017" changed to "006"                  *
 *                                   2.read ptr_sys_parm for contact value1    *
 *  112/03/17  V1.00.05    Simon     產生 006%4.4s1.kk4.HDR、006%4.4s2.kk4.HDR *
 *                                   產生 006%4.4s1.kk4.DAT、006%4.4s2.kk4.DAT *
 *  112/03/29  V1.00.06    Simon     產生 KK4_YYMMDD.DAT、KK4_YYMMDD.HDR       *
 *  112/04/05  V1.00.07    Simon     國外預借現金額度、循環信用年利率 取自 act_jcic_log*
 *  112/04/25  V1.00.08    Simon     comc.errExit() 取代 comcr.errRtn() 顯示"關帳日後一日執行"*
 *  112/05/09  V1.00.09    Simon     produce KK4_%6.6s.DAT & .HDR only on cycle_date + 1day*
 *  112/06/06  V1.00.10    Simon     1.換日後跑，檔案日期減一天                *
 *                                   2.procFTP1() to folder-JCIC、procFTP2() to folder-CRM*
 *  112/06/11  V1.00.11    Simon     1.procFTP1() to folder-JCIC               *
 *                                   2.procFTP2() to folder-CRDATACREA         *
 *                                   3.procFTP3() to folder-CRM                *
 *  112/09/15  V1.00.12    Simon     1.額度為0時不搬空白並新增搬臨調額度       *
 *                                   2.若非cycle當日，則亦須出空檔(首、尾兩筆)給CRM*
 ******************************************************************************/

package Act;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.*;



/*JCIC界面-產生信用卡戶繳款媒體資料檔處理程式*/
public class ActN010 extends AccessDAO {

    private final String PROGNAME = 
    "JCIC界面-產生信用卡戶繳款媒體資料檔處理程式  112/09/15  V1.00.12";
  	private final static String MEDIA_FOLDER = "/media/act/";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
	  CommTxInf commTxInf = null;
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;

    String prgmId = "ActN010";
    String rptName2 = "act_n010";
    String prgName2 = "";
    int recordCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq2 = 0;
    int errCnt = 0;
    String ErrMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String ecsServer = "";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iPostDate = "";

    String cycle = "";
    String hAjlgLogType = "";
    String hWdayThisAcctMonth = "";
    String hWdayStmtCycle = "";
    String hBusiBusinessDate = "";
    String hAjlgSubLogType = "";
    String hAjlgAcctMonth = "";
    String hAjlgAcctType = "";
    String hAjlgPSeqno = "";
    String hAjlgId = "";
    String hAjlgIdCode = "";
    String hAjlgBillTypeFlag = "";
    String hAjlgStmtCycleDate = "";
    double hAjlgLineOfCreditAmt = 0;
    String hAjlgStmtLastPayday = "";
    String hAjlgBinType = "";
    String hAjlgPaymentTimeRate = "";
    double hAjlgStmtPaymentAmt = 0;
    String hAjlgJcicAcctStatus = "";
    String hAjlgStatusChangeDate = "";
    String hAjlgJcicAcctStatusFlag = "";
    String hAjlgDebtCloseDate = "";
    String hAjlgSaleDate = "";
    String hAjlgNplCorpNo = "";
    String hAjlgJcicRemark = "";
    String hAjlgReportReason = "";
    String hAjlgRowid = "";
    double hAjcrThisTtlAmt = 0;
    String hPrintName = "";
    String hRptName = "";
    String hTempBusinessDate = "";
    String hWdayThisCloseDate = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiId = "";
    String hChgiIdCode = "";
    String hChgiOldId = "";
    String hChgiOldIdCode = "";
    String hCbdtAlwBadDate = "";
    String hChgiPostJcicFlag = "";
    double hAjlgCashLmtBalance = 0;
    double hAjlgCashadvLimit = 0;
    double hOverseaCashadvLimit = 0;
    double hAjlgStmtThisTtlAmt = 0;
    double hAjlgStmtMp = 0;
    double hAjlgBilledEndBalBl = 0;
    double hAjlgBilledEndBalIt = 0;
    double hAjlgBilledEndBalId = 0;
    double hAjlgBilledEndBalOt = 0;
    double hAjlgBilledEndBalCa = 0;
    double hAjlgBilledEndBalAo = 0;
    double hAjlgBilledEndBalAf = 0;
    double hAjlgBilledEndBalLf = 0;
    double hAjlgBilledEndBalPf = 0;
    double hAjlgBilledEndBalRi = 0;
    double hAjlgBilledEndBalPn = 0;
    double hAjlgTtlAmtBal = 0;
    double hAjlgBillInterest = 0;
    double hAjlgStmtAdjustAmt = 0;
    double hAjlgUnpostInstFee = 0;
    double hAjlgUnpostCardFee = 0;
    double hAjlgStmtLastTtl = 0;
    double hCareRealIntRate = 0;
    double hAgenRevolvingInterest1 = 0;
    double hAjlgUnpostInstStageFee = 0;
    double hAjlgTempCreditAmt = 0;

    int hAjcrJcicCurrType = 0;
    String hAjlgPaymentAmtRate = "";

    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgModPgm = "";
    String hEriaLocalDir = "";
    String printStr = "";
    int errCodeI = 0;
    String msgCode = "";
    String msgDesc = "";
    int totalCount = 0;
    int totalAll = 0;
    String dcCurrCode = "";
    String temstr  = "";
    String temstr2 = "";
    String hJcicContValue1 = "";
    String hJcicContValue2 = "";
    
    String jcicFileName = "";
    String datFileName = "";
    String hdrFileName = "";

    String[] tempDate = new String[20];
    int out1 = -1;

    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        exceptExit = 1;
        if (args.length != 1 && args.length != 3 && args.length != 4 && args.length != 5) {
            comc.errExit("Usage : ActN010 log_type [acct_month stmt_cycle[business_date]]\n"
                            + "                 1.log_type : 'A':cycle 執行 \n" + "                              'C':每日異動執行 ",
                    "");
        }

        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

        hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        commFTP = new CommFTP(getDBconnect(), getDBalias());
        comr = new CommRoutine(getDBconnect(), getDBalias());
        commTxInf = new CommTxInf(getDBconnect(), getDBalias());

        comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

        if (args.length == 4) {
            String sGArgs3 = "";
            sGArgs3 = args[3];
            sGArgs3 = Normalizer.normalize(sGArgs3, Normalizer.Form.NFKD);
            hBusiBusinessDate = sGArgs3;
        }
        selectPtrBusinday();

        String sGArgs0 = "";
        sGArgs0 = args[0];
        sGArgs0 = Normalizer.normalize(sGArgs0, Normalizer.Form.NFKD);
        hAjlgLogType = sGArgs0;
        if (args.length == 3 || args.length == 4) {
            String sGArgs1 = "";
            sGArgs1 = args[1];
            sGArgs1 = Normalizer.normalize(sGArgs1, Normalizer.Form.NFKD);
            hWdayThisAcctMonth = sGArgs1;
            String sGArgs2 = "";
            sGArgs2 = args[2];
            sGArgs2 = Normalizer.normalize(sGArgs2, Normalizer.Form.NFKD);
            hWdayStmtCycle = sGArgs2;
        } else if (hAjlgLogType.equals("A")) {
            if (selectPtrWorkday() != 0) {
              showLogMessage("I", "", "=== 非關帳日送空檔給 CRM ===");
	            sendNoneDataToCrm();
              exceptExit = 0;
              //comcr.errRtn(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", hBusiBusinessDate), "",
              //        hCallBatchSeqno);
              comc.errExit(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", 
              hBusiBusinessDate), hCallBatchSeqno);
            }
        }
        if (hAjlgLogType.equals("A"))
            rptName2 = "act_n010_ar";
        else
            rptName2 = "act_n010_cr";

		    getContactParm();
      //selectPtrActgeneral();
        checkOpen();

        selectActJcicLog();
        buf = String.format("TRLR%08d%638.638s\n", totalCount, " ");
        writeTextFile(out1, buf);
        closeOutputText(out1);

        if (hAjlgLogType.equals("A")) {
	        temstr2 = String.format("%s/media/act/KK4_%6.6s.DAT", comc.getECSHOME(), 
	        hTempBusinessDate.substring(2));
          comc.fileCopy(temstr, temstr2);

	        datFileName = String.format("KK4_%6.6s.DAT", hTempBusinessDate.substring(2));
      	  String fileFolder =  Paths.get(comc.getECSHOME(),"media/act/").toString();
	        boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, 
	        datFileName, hTempBusinessDate, sysDate, sysTime.substring(0,4), 
	        totalCount);
	        if (isGenerated == false) {
		        comc.errExit("產生HDR檔錯誤!", "");
	        }
	        hdrFileName = String.format("KK4_%6.6s.HDR", hTempBusinessDate.substring(2));
 	        procFTP3();//to folder-CRM
        }

  	  //String filePath = temstr;
   	    jcicFileName=Paths.get(temstr).getFileName().toString();

        if (totalCount > 0) {
   	      procFTP1();//to folder-JCIC
   	      procFTP2();//to folder-CRDATACREA
        }

        printReport();
        comcr.insertPtrBatchRpt(lpar2);

        showLogMessage("I", "", String.format("累計 處理筆數[%d], 新增檔案紀錄[%d]", 
        totalAll, totalCount));

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
    void selectPtrBusinday() throws Exception {

        sqlCmd = "select decode( cast(? as varchar(8)),'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode(cast(? as varchar(8)) ,'',business_date, ? ),'yyyymmdd')-1 days,'yyyymmdd') h_temp_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempBusinessDate = getValue("h_temp_business_date");
        }

    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisAcctMonth = "";
        hWdayThisCloseDate = "";

        sqlCmd = "select stmt_cycle,";
        sqlCmd += " this_acct_month,";
        sqlCmd += " this_close_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = ? ";
        setString(1, hTempBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
        } else
            return (1);
        return (0);
    }

  /***********************************************************************/
	void sendNoneDataToCrm() throws Exception {
		
    getContactParm();
    checkOpen();

    buf = String.format("TRLR%08d%638.638s\n", totalCount, " ");
    writeTextFile(out1, buf);
    closeOutputText(out1);

    temstr2 = String.format("%s/media/act/KK4_%6.6s.DAT", comc.getECSHOME(), 
    hTempBusinessDate.substring(2));
    comc.fileCopy(temstr, temstr2);

    datFileName = String.format("KK4_%6.6s.DAT", hTempBusinessDate.substring(2));
 	  String fileFolder =  Paths.get(comc.getECSHOME(),"media/act/").toString();
    boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, 
    datFileName, hTempBusinessDate, sysDate, sysTime.substring(0,4), 
    totalCount);
    if (isGenerated == false) {
     comc.errExit("產生HDR檔錯誤!", "");
    }
    hdrFileName = String.format("KK4_%6.6s.HDR", hTempBusinessDate.substring(2));

    procFTP3();//to folder-CRM
  	return ;
		
	}

  /***********************************************************************/
	void getContactParm() throws Exception {
		
	  hJcicContValue1 = " ";
	  hJcicContValue2 = " ";

		selectSQL = "wf_value , wf_value2 ";
		daoTable = "ptr_sys_parm";
		whereStr = "where wf_parm ='JCIC_FILE' and wf_key = 'JCIC_KK4' ";
		selectTable();
		
		if("Y".equals(notFound)) {
			return ;
		}	else {
	  	hJcicContValue1 = getValue("wf_value");
	    hJcicContValue2 = getValue("wf_value2");
		}
  	return ;
		
	}
	
    /***********************************************************************/
    int selectPtrActgeneral() throws Exception {
        sqlCmd = "select max(round(revolving_interest1*365/100,2)) h_agen_revolving_interest1 ";
        sqlCmd += " from ptr_actgeneral_n ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("h_agen_revolving_interest1");
        }

        return (0);
    }

  /***********************************************************************/
  void checkOpen() throws Exception {
    if (hAjlgLogType.equals("A")) {
    	temstr = String.format("%s/media/act/006%4.4s2.kk4", comc.getECSHOME(), hTempBusinessDate.substring(4));
    }else {
      temstr = String.format("%s/media/act/006%4.4s1.kk4", comc.getECSHOME(), hTempBusinessDate.substring(4));
    }
    temstr = Normalizer.normalize(temstr, Normalizer.Form.NFKD);
    out1 = openOutputText(temstr, "MS950");
    if (out1 == -1) {
        comcr.errRtn(temstr, "檔案開啓失敗！", hCallBatchSeqno);
    }

    buf = String.format("%-18.18s006%5.5s%07d01%10.10s%-18.18s%-80.80s%507.507s\n", "JCIC-DAT-KK04-V01-", " ",
            comcr.str2long(hTempBusinessDate) - 19110000, " ", hJcicContValue1, hJcicContValue2, " ");
    writeTextFile(out1, buf);

    printStr = String.format("%s/media/rpt/ACT_N010_%sR_%s", comc.getECSHOME(), hAjlgLogType, hTempBusinessDate);
    printStr = Normalizer.normalize(printStr, Normalizer.Form.NFKD);
//        try {
//            out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(printStr), "big5"));
//        } catch (FileNotFoundException exception) {
//            comcr.err_rtn(printStr, "檔案開啓失敗！", h_call_batch_seqno);
//        }
    }

    /***********************************************************************/
    void selectActJcicLog() throws Exception {
        //String tmpstr1 = "";
        String tmpstr2 = "";
        String dcTmpstr = "";

        sqlCmd = "select ";
        //sqlCmd += " decode(sub_log_type,'A','A',log_type) h_ajlg_sub_log_type,";
        //sqlCmd += " decode(log_type,'A','A',sub_log_type) h_ajlg_sub_log_type,";
        sqlCmd += " decode(sub_log_type,'A','A','D','D',log_type) h_ajlg_sub_log_type,";
        sqlCmd += " a.acct_month,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.p_seqno,";
        //sqlCmd += " decode(id,'',corp_no,id) h_ajlg_id,";
        //sqlCmd += " id_code,";
        sqlCmd += " decode(a.id_p_seqno,'',a.corp_no,uf_idno_id(a.id_p_seqno)) h_ajlg_id,";
        sqlCmd += " (select id_no_code from crd_idno where crd_idno.id_p_seqno = a.id_p_seqno fetch first 1 rows only) as id_code,";
        sqlCmd += " bill_type_flag,";
        sqlCmd += " stmt_cycle_date,";
        sqlCmd += " line_of_credit_amt,";
        sqlCmd += " stmt_last_payday,";
        sqlCmd += " bin_type,";
        sqlCmd += " cash_lmt_balance,";
        sqlCmd += " cashadv_limit,";
        sqlCmd += " stmt_this_ttl_amt,";
        sqlCmd += " stmt_mp,";
        sqlCmd += " billed_end_bal_bl,";
        sqlCmd += " billed_end_bal_it,";
        sqlCmd += " billed_end_bal_id,";
        sqlCmd += " billed_end_bal_ot,";
        sqlCmd += " billed_end_bal_ca,";
        sqlCmd += " billed_end_bal_ao,";
        sqlCmd += " billed_end_bal_af,";
        sqlCmd += " billed_end_bal_lf,";
        sqlCmd += " billed_end_bal_pf,";
        sqlCmd += " billed_end_bal_ri,";
        sqlCmd += " billed_end_bal_pn,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " bill_interest,";
        sqlCmd += " stmt_adjust_amt,";
        sqlCmd += " unpost_inst_fee,";
        sqlCmd += " unpost_card_fee,";
        sqlCmd += " stmt_last_ttl,";
        sqlCmd += " payment_amt_rate,";
        sqlCmd += " payment_time_rate,";
        sqlCmd += " stmt_payment_amt,";
        sqlCmd += " jcic_acct_status,";
        sqlCmd += " status_change_date,";
        sqlCmd += " jcic_acct_status_flag,";
        sqlCmd += " decode(jcic_acct_status_flag,'U',debt_close_date,'') h_ajlg_debt_close_date,";
        sqlCmd += " sale_date,";
        sqlCmd += " npl_corp_no,";
        sqlCmd += " jcic_remark,";
        sqlCmd += " decode(cast(? as varchar(8)) ,'A','01',decode(report_reason ,'','01', report_reason)) h_ajlg_report_reason,";
        sqlCmd += " unpost_inst_stage_fee,"; /* 未到期分期償還代墊帳單帳款金額 */
        sqlCmd += " oversea_cashadv_limit,"; 
        sqlCmd += " year_revolve_int_rate,"; 
        sqlCmd += " temp_of_credit_amt,"; 
        sqlCmd += " a.rowid rowid,";
        sqlCmd += " b.jcic_curr_type h_ajcr_jcic_curr_type,";
        sqlCmd += " b.this_ttl_amt h_ajcr_this_ttl_amt ";
        sqlCmd += " from act_jcic_log a  left outer join act_jcic_curr b   on a.p_seqno    = b.p_seqno "
                + "  and decode(a.acct_month, '','x',a.acct_month) = b.acct_month ";
        sqlCmd += "where log_type = ? ";
        //sqlCmd += "  and a.acct_month = decode(cast(? as varchar(8)),'A',cast(? as varchar(8)),a.acct_month) ";
        sqlCmd += "  and ( (log_type = 'A' and a.acct_month = ?) or (log_type <> 'A') ) ";
        sqlCmd += "  and stmt_cycle   = decode(cast(? as varchar(8)),'A',cast(? as varchar(8)),stmt_cycle) ";
        sqlCmd += "  and (   debt_close_date  = '' "
                + "      or (debt_close_date != '' and months_between(to_date(?,'yyyymmdd'), to_date(debt_close_date,'yyyymmdd')) <= 3)) ";
        sqlCmd += "  and proc_flag    = decode(cast(? as varchar(8)),'A',proc_flag,'N') ";
        //Temp
        //sqlCmd += "  and proc_date    = ? ";

        setString(1, hAjlgLogType);
        setString(2, hAjlgLogType);
        //setString(3, h_ajlg_log_type);
        //setString(4, h_wday_this_acct_month);
        setString(3, hWdayThisAcctMonth);
        setString(4, hAjlgLogType);
        setString(5, hWdayStmtCycle);
        setString(6, hTempBusinessDate);
        setString(7, hAjlgLogType);
        //Temp
        //setString(8, h_busi_business_date);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAjlgSubLogType = getValue("h_ajlg_sub_log_type");
            hAjlgAcctMonth = getValue("acct_month");
            hAjlgAcctType = getValue("acct_type");
            hAjlgPSeqno = getValue("p_seqno");
            hAjlgId = getValue("h_ajlg_id");
            hAjlgIdCode = getValue("id_code");
            hAjlgBillTypeFlag = getValue("bill_type_flag");
            hAjlgStmtCycleDate = getValue("stmt_cycle_date");
            hAjlgLineOfCreditAmt = getValueDouble("line_of_credit_amt");
            hAjlgStmtLastPayday = getValue("stmt_last_payday");
            hAjlgBinType = getValue("bin_type").replaceAll(" ", ""); /* remove space */
            hAjlgCashLmtBalance = getValueDouble("cash_lmt_balance");
            hAjlgCashadvLimit = getValueDouble("cashadv_limit");
            hAjlgStmtThisTtlAmt = getValueDouble("stmt_this_ttl_amt");
            hAjlgStmtMp = getValueDouble("stmt_mp");
            hAjlgBilledEndBalBl = getValueDouble("billed_end_bal_bl");
            hAjlgBilledEndBalIt = getValueDouble("billed_end_bal_it");
            hAjlgBilledEndBalId = getValueDouble("billed_end_bal_id");
            hAjlgBilledEndBalOt = getValueDouble("billed_end_bal_ot");
            hAjlgBilledEndBalCa = getValueDouble("billed_end_bal_ca");
            hAjlgBilledEndBalAo = getValueDouble("billed_end_bal_ao");
            hAjlgBilledEndBalAf = getValueDouble("billed_end_bal_af");
            hAjlgBilledEndBalLf = getValueDouble("billed_end_bal_lf");
            hAjlgBilledEndBalPf = getValueDouble("billed_end_bal_pf");
            hAjlgBilledEndBalRi = getValueDouble("billed_end_bal_ri");
            hAjlgBilledEndBalPn = getValueDouble("billed_end_bal_pn");
            hAjlgTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAjlgBillInterest = getValueDouble("bill_interest");
            hAjlgStmtAdjustAmt = getValueDouble("stmt_adjust_amt");
            hAjlgUnpostInstFee = getValueDouble("unpost_inst_fee");
            hAjlgUnpostCardFee = getValueDouble("unpost_card_fee");
            hAjlgStmtLastTtl = getValueDouble("stmt_last_ttl");
            hAjlgPaymentAmtRate = getValue("payment_amt_rate");
            hAjlgPaymentTimeRate = getValue("payment_time_rate");
            hAjlgStmtPaymentAmt = getValueDouble("stmt_payment_amt");
            hAjlgJcicAcctStatus = getValue("jcic_acct_status");
            hAjlgStatusChangeDate = getValue("status_change_date");
            hAjlgJcicAcctStatusFlag = getValue("jcic_acct_status_flag");
            hAjlgDebtCloseDate = getValue("h_ajlg_debt_close_date");
            hAjlgSaleDate = getValue("sale_date");
            hAjlgNplCorpNo = getValue("npl_corp_no");
            hAjlgJcicRemark = getValue("jcic_remark");
            hAjlgReportReason = getValue("h_ajlg_report_reason");
            hAjlgUnpostInstStageFee = getValueDouble("unpost_inst_stage_fee"); /* 增設「未到期分期償還代墊帳單帳款金額」 */
            hAjlgRowid = getValue("rowid");
            hAjcrJcicCurrType = getValueInt("h_ajcr_jcic_curr_type");
            hAjcrThisTtlAmt = getValueDouble("h_ajcr_this_ttl_amt");
            hOverseaCashadvLimit = getValueDouble("oversea_cashadv_limit");
            hCareRealIntRate = getValueDouble("year_revolve_int_rate");
            hAjlgTempCreditAmt = getValueDouble("temp_of_credit_amt");

            totalAll++;

            selectCrdChgId();
            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                if (!hAjlgLogType.equals("A"))
                    updateActJcicLog();
                continue;
            }

          //computeOverseaCashadvLimit();
          //selectCycAcnoRate();
            hAjlgBilledEndBalIt = hAjlgBilledEndBalIt - hAjlgBilledEndBalRi;
            if (hAjlgBilledEndBalIt < 0)
                hAjlgBilledEndBalIt = 0;
            hAjlgBilledEndBalCa = hAjlgBilledEndBalCa + hAjlgBilledEndBalRi;

            for (int int1a = 0; int1a < 9; int1a++)
                tempDate[int1a] = "";
            tempDate[0] = String.format("%07d", comcr.str2long(hAjlgStmtCycleDate) - 19110000);
            if (hAjlgStmtLastPayday.length() > 0)
                tempDate[1] = String.format("%07d", comcr.str2long(hAjlgStmtLastPayday) - 19110000);
            if (hAjlgStatusChangeDate.length() > 0)
                tempDate[2] = String.format("%07d", comcr.str2long(hAjlgStatusChangeDate) - 19110000);
            if (hAjlgDebtCloseDate.length() > 0)
                tempDate[3] = String.format("%07d", comcr.str2long(hAjlgDebtCloseDate) - 19110000);
            if (hAjlgSaleDate.length() != 0) {
                hAjlgJcicAcctStatusFlag = "T";
                tempDate[4] = String.format("%05d", (comcr.str2long(hAjlgSaleDate) - 19110000) / 100);

            }
            tmpstr2 = String.format("%011.0f%011.0f", hAjlgLineOfCreditAmt, 
            hAjlgTempCreditAmt);
            /********************* test for dc currency *****************/
            dcCurrCode = String.format("    ");
            dcTmpstr = " ";
            if (hAjcrJcicCurrType != 0) {
                dcTmpstr = String.format("%d", hAjcrJcicCurrType);
                dcCurrCode = String.format("%-4.4s", dcTmpstr);
                dcTmpstr = String.format("%011.0f", hAjcrThisTtlAmt);
            }
            /********************* test for dc currency *****************/
            if (hAjlgStmtPaymentAmt < 0)
                hAjlgStmtPaymentAmt = hAjlgStmtPaymentAmt * -1;
            buf = String.format(
                    //"8%1.1s006%5.5s%7.7s%7.7s%-10.10s%-7.7s%-2.2s%2.2s%2.2s%-22.22s%011.0f%011.0f%011.0f%011.0f%011.0f%011.0f%011.0f%08.0f%08.0f%011.0f%011.0f%06.3f%6.6s%011.0f%011.0f%011.0f%1.1s%1.1s%1.1s%7.7s%5.5s%1.1s%7.7s%5.5s%-10.10s%-25.25s%158.158s%011.0f00000000000%4.4s%11.11s%163.163s",
                    "8%1.1s006%5.5s%7.7s%7.7s%-10.10s%-7.7s%-2.2s%2.2s%2.2s%-22.22s%011.0f%011.0f%011.0f%011.0f%011.0f%011.0f%011.0f%08.0f%08.0f%011.0f%011.0f%06.3f%6.6s%011.0f%011.0f%011.0f%1.1s%1.1s%1.1s%7.7s%5.5s%1.1s%7.7s%5.5s%-10.10s%-25.25s%158.158s%011.0f%011.0f%4.4s%11.11s%163.163s",
                    hAjlgSubLogType, " ", tempDate[0], tempDate[1], hAjlgId, hAjlgBinType, hAjlgBillTypeFlag,
                    " ", hAjlgReportReason, tmpstr2, hAjlgCashLmtBalance + hAjlgCashadvLimit,
                    hAjlgStmtThisTtlAmt, hAjlgStmtMp,
                    hAjlgBilledEndBalBl + hAjlgBilledEndBalIt + hAjlgBilledEndBalId
                            + hAjlgBilledEndBalOt,
                    hAjlgBilledEndBalCa, hAjlgBilledEndBalAo, hAjlgBillInterest,
                    hAjlgBilledEndBalAf + hAjlgBilledEndBalLf + hAjlgBilledEndBalPf
                            + hAjlgBilledEndBalPn,
                    hAjlgStmtAdjustAmt, hAjlgUnpostInstFee, hAjlgUnpostCardFee, hCareRealIntRate, " ",
                    hAjlgStmtLastTtl, hAjlgStmtPaymentAmt, hAjlgTtlAmtBal, hAjlgPaymentAmtRate,
                    hAjlgPaymentTimeRate, hAjlgJcicAcctStatus, tempDate[2], " ", hAjlgJcicAcctStatusFlag,
                    tempDate[3], tempDate[4], hAjlgNplCorpNo, hAjlgJcicRemark, " ", hAjlgUnpostInstStageFee,
                    //dc_curr_code, dc_tmpstr, " ");
                    hOverseaCashadvLimit, dcCurrCode, dcTmpstr, " ");
            /* 增設「未到期分期償還代墊帳單帳款金額」及「國外預借現金額度」 */
            writeTextFile(out1, buf + "\n");
            totalCount++;
            if (totalCount % 25000 == 0)
                showLogMessage("I", "", String.format("    新增檔案紀錄[%d]", totalCount));
            if (!hAjlgLogType.equals("A"))
                updateActJcicLog();
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectCrdChgId() throws Exception {
        int recordCnt = 0;
        hChgiCreateUser = "";
        hChgiApprovUser = "";
        hChgiChiName = "";
        hChgiId = "";
        hChgiIdCode = "";
        hChgiPostJcicFlag = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " crt_user,";
        sqlCmd += " apr_user,";
        sqlCmd += " id_no,";
        sqlCmd += " id_no_code,";
        sqlCmd += " decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where old_id_no  = ?  ";
        sqlCmd += "  and old_id_no_code = ? ";
        setString(1, hAjlgId);
        setString(2, hAjlgIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCreateUser = getValue("crt_user");
            hChgiApprovUser = getValue("apr_user");
            hChgiId = getValue("id_no");
            hChgiIdCode = getValue("id_no_code");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }

        if (!hAjlgSubLogType.equals("D") &&
                hAjlgJcicAcctStatus.equals("B")) {  //act_jcic_log 不只是從 ActN015及ActN017 insert 而來，補檢核
            hChgiOldId = "";
            hChgiOldIdCode = "";

            sqlCmd = " SELECT old_id_no, ";
            sqlCmd += "        old_id_no_code ";
            sqlCmd += "  FROM  crd_chg_id ";
            sqlCmd += " WHERE  id_no       = ? ";
            sqlCmd += "   AND  id_no_code  = ? ";
            sqlCmd += "   fetch first 1 rows only ";
            setString(1, hAjlgId);
            setString(2, hAjlgIdCode);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hChgiOldId = getValue("old_id");
                hChgiOldIdCode = getValue("old_id_code");
            }

            if (hChgiOldId.length() != 0) {
                selectColBadDebt();
            }
        }

    }

    /***************************************************************************/
    void selectColBadDebt() throws Exception/* *chun-yang modify* */
    {
        int recordCnt = 0;
        hCbdtAlwBadDate = "";

        sqlCmd = " SELECT  alw_bad_date ";
        sqlCmd += "  FROM  col_bad_debt ";
        sqlCmd += " WHERE  trans_type = '4' ";
        sqlCmd += "   AND  id_no         = ? ";
        sqlCmd += "   AND  id_code    = ? ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hChgiOldId);
        setString(2, hChgiOldIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hCbdtAlwBadDate = getValue("alw_bad_date");
        }

        if (hCbdtAlwBadDate.length() != 0) {
            showLogMessage("I", "", String.format("Chen-D N[%s] O[%s]- [%s] change to [%s]", hAjlgId,
                    hChgiOldId, hAjlgStatusChangeDate, hCbdtAlwBadDate));
            hAjlgStatusChangeDate = hCbdtAlwBadDate;
        }
    }

    /***********************************************************************/
    void insertCrdNopassJcic() throws Exception {
        setValue("old_id", hAjlgId);
        setValue("old_id_code", hAjlgIdCode);
        setValue("chi_name", hChgiChiName);
        setValue("post_kind", "kk4");
        setValue("post_jcic_date", sysDate);
        setValue("card_no", "");
        setValue("oppost_reason", "");
        setValue("oppost_date", "");
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "crd_nopass_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_nopass_jcic duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void computeOverseaCashadvLimit() throws Exception {
        double lbOverseaCashPct = 0;
        sqlCmd  = "select max(oversea_cash_pct) as lb_oversea_cash_pct ";
        sqlCmd += " from cca_auth_parm  ";
        sqlCmd += "where area_type = 'T'  ";
        sqlCmd += "  and (card_note = '*' or card_note in ";
        sqlCmd += "       (select distinct card_note from crd_card   ";
        sqlCmd += "        where current_code = '0' and p_seqno = ? )) ";
        setString(1, hAjlgPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            lbOverseaCashPct = getValueDouble("lb_oversea_cash_pct");
        }

        hOverseaCashadvLimit = 0;
        sqlCmd  = "select (case when floor( ? * ? / 100) > cashadv_loc_maxamt ";
        sqlCmd += "        then cashadv_loc_maxamt else floor( ? * ? / 100) end) ";
        sqlCmd += "        as h_oversea_cashadv_limit ";
        sqlCmd += " from ptr_acct_type  ";
        sqlCmd += "where acct_type = ?  ";
        setDouble(1, hAjlgLineOfCreditAmt);
        setDouble(2, lbOverseaCashPct);
        setDouble(3, hAjlgLineOfCreditAmt);
        setDouble(4, lbOverseaCashPct);
        setString(5, hAjlgAcctType);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hOverseaCashadvLimit = getValueDouble("h_oversea_cashadv_limit");
        }

    }

    /***********************************************************************/
    void selectCycAcnoRate() throws Exception {
        hCareRealIntRate = 0;

        sqlCmd = "select real_int_rate ";
        sqlCmd += " from cyc_acno_rate  ";
        sqlCmd += "where acct_month  = ?  ";
        sqlCmd += "  and p_seqno     = ?  ";
        sqlCmd += "  and acct_month >= substr(decode(revolve_rate_s_date,'','00000000',revolve_rate_s_date),1,6)  ";
        sqlCmd += "  and acct_month  < substr(decode(revolve_rate_e_date,'','30001231',revolve_rate_e_date),1,6) ";
        setString(1, hAjlgAcctMonth);
        setString(2, hAjlgPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCareRealIntRate = getValueDouble("real_int_rate");
        } else
            hCareRealIntRate = hAgenRevolvingInterest1;
        if (hCareRealIntRate < 0)
            hCareRealIntRate = 0;
        if (hCareRealIntRate > hAgenRevolvingInterest1)
            hCareRealIntRate = hAgenRevolvingInterest1;
    }

    /***********************************************************************/
    void updateActJcicLog() throws Exception {
        daoTable = "act_jcic_log";
        updateSQL = " proc_flag = 'Y',";
        updateSQL += " proc_date = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = 'ActN010'";
        whereStr = "where rowid = ? ";
        setString(1, hTempBusinessDate);
        setRowId(2, hAjlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printReport() throws Exception {
        String cDate = "";
        String rTemstr = "";


        if (hAjlgLogType.equals("A"))
            prgName2 = "報送新增JCIC信用卡戶繳款資料總數報表";
        else
            prgName2 = "報送異動JCIC信用卡戶繳款資料總數報表";

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        if (hAjlgLogType.equals("A"))
            buf = comcr.insertStr(buf, "報表名稱:act_n010_ar", 1);
        else
            buf = comcr.insertStr(buf, "報表名稱:act_n010_cr", 1);

        if (hAjlgLogType.equals("A"))
            buf = comcr.insertStrCenter(buf, "報送新增JCIC信用卡戶繳款資料總數報表", 80);
        else
            buf = comcr.insertStrCenter(buf, "報送異動JCIC信用卡戶繳款資料總數報表", 80);

        buf = comcr.insertStr(buf, "頁次:", 68);
        rTemstr = String.format("%04d", 1);
        buf = comcr.insertStr(buf, rTemstr, 73);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        rTemstr = String.format("%4.4s", hTempBusinessDate);

        buf = "";
        buf = comcr.insertStr(buf, "單    位:", 1);
        buf = comcr.insertStr(buf, "109", 11);
        buf = comcr.insertStr(buf, "交易日期:", 58);
        cDate = String.format("%03d年%2.2s月%2.2s日", comcr.str2long(rTemstr) - 1911, hTempBusinessDate.substring(4),
                hTempBusinessDate.substring(6));
        buf = comcr.insertStr(buf, cDate, 68);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        szTmp = comcr.commFormat("3z,3z,3z", totalCount);
        if (hAjlgLogType.equals("A"))
            szTmp = String.format("產生資料 : %2.2s月%2.2scycle 筆 數  : %s 筆", hTempBusinessDate.substring(4),
                    hTempBusinessDate.substring(6), szTmp);
        else
            szTmp = String.format("異動日期 : %2.2s月%2.2s日    筆 數  : %s 筆", hTempBusinessDate.substring(4),
                    hTempBusinessDate.substring(6), szTmp);
        buf = comcr.insertStr(buf, szTmp, 1);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        if (hAjlgLogType.equals("A"))
            buf = comcr.insertStr(buf, "備註 1 : CYCLE次一營業日產生報送新增JCIC信用卡繳款評等資料", 1);
        else
            buf = comcr.insertStr(buf, "備註 1 : 異動繳款記錄次一營業日產生報送JCIC信用卡繳款評等資料", 1);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

        buf = comcr.insertStr(buf, "備註 2 : 資料格式依據JCIC民國94年2月版本", 1);
        lpar2.add(comcr.putReport(rptName2, prgName2, sysDate, ++rptSeq2, "0", buf));

    }

  //************************************************************************	
  void procFTP1() throws Exception {
  	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  	commFTP.hEflgSystemId = "JCIC"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
  	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  	commFTP.hEflgModPgm = javaProgram;

  //showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="JCIC"，其ftp_type="0"，在COMMFTP處理只是fileCopy
  	int errCode = commFTP.ftplogName("JCIC", "mput " + jcicFileName);

  	if (errCode != 0) {
  		showLogMessage("I", "", "ERROR:無法傳送 " + jcicFileName + " 資料" + " errcode:" + errCode);
  		insertEcsNotifyLog(jcicFileName);
  	}
  }

  //************************************************************************	
  void procFTP2() throws Exception {
  	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  	commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
  	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  	commFTP.hEflgModPgm = javaProgram;

  //showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="JCIC"，其ftp_type="0"，在COMMFTP處理只是fileCopy
  	int errCode = commFTP.ftplogName("CRDATACREA", "mput " + jcicFileName);

  	if (errCode != 0) {
  		showLogMessage("I", "", "ERROR:無法傳送 " + jcicFileName + " 資料" + " errcode:" + errCode);
  		insertEcsNotifyLog(jcicFileName);
  	} else {
  		renameFile(jcicFileName);
  	}
  }

  //************************************************************************	
  void procFTP3() throws Exception {
  	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  	commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
  	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  	commFTP.hEflgModPgm = javaProgram;

		// 先傳送KK4_YYMMDD.DAT，再傳送KK4_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

	//showLogMessage("I", "", String.format("開始執行傳檔指令[%s]......", ftpCommand));
	//ecs_ref_ip_code.ref_ip_code="JCIC"，其ftp_type="0"，在COMMFTP處理只是fileCopy
  	int errCode = commFTP.ftplogName("CRM", ftpCommand);

  	if (errCode != 0) {
  		showLogMessage("I", "", "ERROR:無法傳送 " + datFileName + " & " + hdrFileName +
  		" 資料" +	" errcode:" + errCode);
  		insertEcsNotifyLog(datFileName);
  		insertEcsNotifyLog(hdrFileName);
  	} else {
  		renameFile(datFileName);
  		renameFile(hdrFileName);
  	}
  }

  //************************************************************************		  
	public int insertEcsNotifyLog(String fileName) throws Exception {
	  setValue("crt_date", sysDate);
	  setValue("crt_time", sysTime);
	  setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	  setValue("obj_type", "3");
	  setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	  setValue("notify_name", "媒體檔名:" + fileName);
	  setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	  setValue("notify_desc2", "");
	  setValue("trans_seqno", commFTP.hEflgTransSeqno);
	  setValue("mod_time", sysDate + sysTime);
	  setValue("mod_pgm", javaProgram);
	  daoTable = "ecs_notify_log";

	  insertTable();

	  return (0);
	}

  //************************************************************************		  
  void renameFile(String fileName) throws Exception {
  	String tmpstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
   	String tmpstr2 = String.format("%s/media/act/backup/%s.%s", comc.getECSHOME(), 
   	fileName,sysDate+sysTime);

  	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
   		showLogMessage("I", "", "ERROR : 檔案[" + tmpstr1 + "]更名失敗!");
   		return;
   	}
   	showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    ActN010 proc = new ActN010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}
