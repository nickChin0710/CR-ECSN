/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/01/02  V1.00.00    phopho     program initial                          *
*  109/03/05  V1.00.01    sunny      h_agen_revolving_interest參數由int修正為double*
*  109/12/12  V1.00.05    shiyuqi    updated for project coding standard      *
*  111/06/22  V1.00.06    sunny      調整act_debt.acct_code不排除AF/CF/PF       *
*  111/12/08  V1.00.07    sunny      sync from mega                           *
*  112/05/09  V1.00.08    sunny      調整程式參數，不強迫帶值                                             *
*  112/10/04  V1.00.09    sunny      增加平帳作業處理寫入act_jrnl                 *
*  112/10/05  V1.00.10    sunny      修正平帳作業處理寫入act_jrnl(減項)                         *
*  112/11/09  V1.00.12    sunny      補充平帳作業處理寫入act_jrnl(post_date)，人工轉催仍維持明細傳票  *
******************************************************************************/

package Col;

import java.text.Normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class ColB009 extends AccessDAO {
	public final boolean debug = false; //debug用
	public final boolean debug1 = true; //debug用
    private String progname = "每日線上轉正常戶、催收戶及呆帳戶處理程式  112/11/09  V1.00.12";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr     = null;

    List<Map<String, Object>> lparNovou = new ArrayList<Map<String, Object>>();

    String rptNameNovou = "";
    int rptSeqNovou = 0;
    String buf                = "";
    String szTmp              = "";
    String hCallBatchSeqno = "";

    double hCprmReqDebtLmt = 0;
    double hCprmTerminateAmt1 = 0;
    int hCprmTerminateYear1 = 0;
    int hCprmTerminateMonth1 = 0;
    int hCprmTerminateYear2 = 0;
    int hCprmTerminateMonth2 = 0;
    String hBusiBusinessDate = "";
    String hCwtrPSeqno = "";
    String hCwtrSrcAcctStat = "";
    String hCwtrTransType = "";
    String hCwtrAlwBadDate = "";
    String hCwtrPaperConfDate = "";
    String hCwtrValidCancelDate = "";
    String hCwtrPaperName = "";
    String hCwtrCreateDate = "";
    String hCwtrCreateUser = "";
    String hCwtrConfDate = "";
    String hCwtrConfUser = "";
    String hCwtrRowid = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoCreditActNo = "";
    double hAcnoLineOfCreditAmt = 0;
    String hAcnoStmtCycle = "";
    String hAcnoAcctStatus = "";
    String hAcnoAcnoFlag = "";
//    String   h_acno_acct_sub_status      = "";
    String hAcnoLegalDelayCode = "";
    String hAcnoLawsuitMark = "";
    String hAcnoLawsuitMarkDate = "";
    String hAcnoNewCycleMonth = "";
    String hAcnoLastInterestDate = "";
    int hAcnoIntRateMcode = 0;
    String hAcnoRowid = "";
    String hTempAcctStatus = "";
//    String   h_temp_acct_sub_status      = "";
    String hTempLegalDelayCode = "";
    String hAcnoRecourseMark = "";
    String hAcnoModUser = "";
    String hAcnoModPgm = "";
    long hAcnoModSeqno = 0;
    String hAcnoNoDelinquentFlag = "";
    String hAcnoNoDelinquentSDate = "";
    String hAcnoNoDelinquentEDate = "";
    String hAcnoNoCollectionFlag = "";
    String hAcnoNoCollectionSDate = "";
    String hAcnoNoCollectionEDate = "";
    double hAgenRevolvingInterest1 = 0;
    double hAgenRevolvingInterest2 = 0;
    double hAgenRevolvingInterest3 = 0;
    double hAgenRevolvingInterest4 = 0;
    double hAgenRevolvingInterest5 = 0;
    double hAgenRevolvingInterest6 = 0;
    double hCbdtSrcAmt = 0;
    int hTempMonth = 0;
    String hCbdtRowid = "";
    double hAcctAcctJrnlBal = 0;
    double hAcctTempUnbillInterest = 0;
    double hAcctMinPayBal = 0;
    double hAcctRcMinPayM0 = 0;
    double hAcctRcMinPayBal = 0;
    double hAcctTtlAmtBal = 0;
    int hAcctAdjustDrCnt = 0;
    double hAcctAdjustDrAmt = 0;
    String hAcctRowid = "";
    String hTempAcctCode = "";
    String hDebtAcctCode = "";
    String hDebtAcctMonth = "";
    String hDebtPostDate = "";
    String hDebtReferenceSeq = "";
    double hDebtEndBal = 0;
    double hDebtDAvailableBal = 0;
    String hDebtRowid = "";
    double hTempRate = 0;
    String hWdayThisCloseDate = "";
    double hTempRateAmt = 0;
    String hDebtAcctItemType = "";
    String hDebtOrgItemEname = "";
    String hDebtItemOrderNormal = "";
    String hDebtItemOrderBackDate = "";
    String hDebtItemOrderRefund = "";
    String hDebtItemClassNormal = "";
    String hDebtItemClassBackDate = "";
    String hDebtItemClassRefund = "";
    String hDebtAcctItemCname = "";
//    String   h_acno_corp_no_code         = "";
    String hVouchCdKind = "";
    String hTAcNo = "";
    int hTSeqno = 0;
    String hTDbcr = "";
    String hTMemo3Kind = "";
    String hTMemo3Flag = "";
    String hTDrFlag = "";
    String hTCrFlag = "";
    String tMemo3 = "";
    String hWdayNextAcctMonth = "";
    String hWdayStmtCycle = "";
    String hDebtCardNo = "";
    String hWdayThisAcctMonth = "";
    double hAcagPayAmt = 0;
    String hAcagAcctMonth = "";
    String hAcagRowid = "";
    String classCode = "";
//    String   h_system_vouch_date         = "";
    String hBusinssChiDate = "";
    String hPrintName = "";
    String hRptName = "";
//    String   h_gsvh_memo1                = "";
//    String   h_gsvh_memo2                = "";
//    String   h_gsvh_mod_pgm              = "";
//    String   h_gsvh_curr                 = "";
    int hTempCbdtCnt = 0;
    double hTempCbdtAmt = 0;
    double[] cTotalAmt = new double[12];
    double[] dTotalAmt = new double[12];
    double[] nTotalAmt = new double[12];
    double[] tTotalAmt = new double[12];
    double[] t4TotalAmt = new double[12];
    double[] t5TotalAmt = new double[12];
    double[] t6TotalAmt = new double[12];
    String   tmpstr= "", tmpstr1 = "";
//    int pStatus = 0;
    String   temstr= "";
    double amtNovouchAf = 0, amtNovouchCf = 0;
    double amtNovouchPf = 0, amtNovouchAi = 0;
    int      inta                        = 0, mCode = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
//            if (args.length != 1) {
//                comc.errExit("Usage : ColB009 0", "");
//            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hAcnoModUser = comc.commGetUserID();
            hAcnoModPgm = javaProgram;

            selectColParam();
            selectPtrBusinday();
            selectPtrActgeneral();
            if (args.length > 0 && args[0].length() == 8)
                hBusiBusinessDate = args[0];
            for (inta = 0; inta < 10; inta++) {
                t4TotalAmt[inta] = 0;
                t5TotalAmt[inta] = 0;
                t6TotalAmt[inta] = 0;
            }
            selectColWaitTrans();
            deleteColWaitTrans0(); //執行完成後，清空未覆核資料 mod by phopho 2019.2.15
            deleteColBadTrans0();
            
            //呆帳會計帳
            if (t4TotalAmt[1] > 0) {
                for (inta = 1; inta < 10; inta++)
                {
                    tTotalAmt[inta] = t4TotalAmt[inta];
                    if(debug)
                    showLogMessage("I", "", " 777 轉呆 " + tTotalAmt[inta] + "=["+ tTotalAmt[inta]+"]");
                }
                hVouchCdKind = "I002"; //舊C-02
                //tmpstr = String.format("%7d 線上轉呆", comcr.str2long(hBusiBusinessDate) - 19110000);
                
                tmpstr1 = String.format("%s-%s人工轉呆", hAcnoAcctType,hAcnoAcctHolderId);
                
                if (!hAcnoAcnoFlag.equals("1")) { 
                    tmpstr1 = String.format("%s-%s人工轉呆", hAcnoAcctType,hAcnoCorpNo);
                }
                
                comcr.hGsvhMemo1 = tmpstr1;
//                comcr.hGsvhMemo2 = comcr.hGsvhMemo1;
                comcr.hGsvhModPgm = javaProgram;
               
                //20230818 sunny 暫時不起呆帳
                //vouchRtn();
            }
            if (t5TotalAmt[1] > 0) {
                hVouchCdKind = "C-04";
                tTotalAmt[1] = t5TotalAmt[1];
                tTotalAmt[2] = t5TotalAmt[1];
               
                tmpstr = String.format("%7d 線上轉追索", comcr.str2long(hBusiBusinessDate) - 19110000);
                comcr.hGsvhMemo1 = tmpstr1;
//                comcr.hGsvhMemo2 = comcr.hGsvhMemo1;
                comcr.hGsvhModPgm = javaProgram;
                vouchRtn();
            }

// 20230724 TCB取消          
//            if ((amtNovouchAf > 0) || (amtNovouchCf > 0) || (amtNovouchPf > 0) || (amtNovouchAi > 0))
//                novouchReportRtn();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
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
    void selectColParam() throws Exception {
        hCprmReqDebtLmt = 0;
        hCprmTerminateAmt1 = 0;
        hCprmTerminateYear1 = 0;
        hCprmTerminateMonth1 = 0;
        hCprmTerminateYear2 = 0;
        hCprmTerminateMonth2 = 0;

        sqlCmd = "select req_debt_lmt,";
        sqlCmd += "terminate_amt1*10000 terminate_amt1,";
        sqlCmd += "terminate_year1,";
        sqlCmd += "terminate_month1,";
        sqlCmd += "terminate_year2,";
        sqlCmd += "terminate_month2 ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmReqDebtLmt = getValueDouble("req_debt_lmt");
            hCprmTerminateAmt1 = getValueDouble("terminate_amt1");
            hCprmTerminateYear1 = getValueInt("terminate_year1");
            hCprmTerminateMonth1 = getValueInt("terminate_month1");
            hCprmTerminateYear2 = getValueInt("terminate_year2");
            hCprmTerminateMonth2 = getValueInt("terminate_month2");
        }

    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    // 注意:ptr_actgeneral，改成用 ptr_actgeneral_n，多帶入 acct_type
    void selectPtrActgeneral() throws Exception {
        hAgenRevolvingInterest1 = 0;
        hAgenRevolvingInterest2 = 0;
        hAgenRevolvingInterest3 = 0;
        hAgenRevolvingInterest4 = 0;
        hAgenRevolvingInterest5 = 0;
        hAgenRevolvingInterest6 = 0;

        sqlCmd = "select revolving_interest1,";
        sqlCmd += "revolving_interest2,";
        sqlCmd += "revolving_interest3,";
        sqlCmd += "revolving_interest4,";
        sqlCmd += "revolving_interest5,";
        sqlCmd += "revolving_interest6 ";
        sqlCmd += " from ptr_actgeneral_n ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
            hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
            hAgenRevolvingInterest3 = getValueDouble("revolving_interest3");
            hAgenRevolvingInterest4 = getValueDouble("revolving_interest4");
            hAgenRevolvingInterest5 = getValueDouble("revolving_interest5");
            hAgenRevolvingInterest6 = getValueDouble("revolving_interest6");
        }

    }

    /***********************************************************************/
    void selectColWaitTrans() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "src_acct_stat,";
        sqlCmd += "trans_type,";
        sqlCmd += "alw_bad_date,";
        sqlCmd += "paper_conf_date,";
        sqlCmd += "valid_cancel_date,";
        sqlCmd += "paper_name,";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_date,";
        sqlCmd += "apr_user,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_wait_trans ";
        sqlCmd += "where apr_flag = 'Y' ";
        sqlCmd += "and decode(sys_trans_flag,'','N',sys_trans_flag) = 'N' ";

        openCursor();
        while (fetchTable()) {
            hCwtrPSeqno = getValue("p_seqno");
            hCwtrSrcAcctStat = getValue("src_acct_stat");
            hCwtrTransType = getValue("trans_type");
            hCwtrAlwBadDate = getValue("alw_bad_date");
            hCwtrPaperConfDate = getValue("paper_conf_date");
            hCwtrValidCancelDate = getValue("valid_cancel_date");
            hCwtrPaperName = getValue("paper_name");
            hCwtrCreateDate = getValue("crt_date");
            hCwtrCreateUser = getValue("crt_user");
            hCwtrConfDate = getValue("apr_date");
            hCwtrConfUser = getValue("apr_user");
            hCwtrRowid = getValue("rowid");

            selectActAcno();

            hTempAcctStatus = hCwtrTransType;

//            m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
            mCode = hAcnoIntRateMcode;

            if ((hCwtrTransType.equals("3")) || (hCwtrTransType.equals("4")))
                selectColBadDebt();
                     
            if (((hAcnoAcctStatus.equals("1")) || (hAcnoAcctStatus.equals("2"))) && (hCwtrTransType.equals("3"))) {
                if (selectCrdCard2() != 0) {
                    deleteColWaitTrans();
                    continue;
                }
                hCbdtSrcAmt = 0;
                for (inta = 0; inta < 10; inta++)
                    nTotalAmt[inta] = 0;
                selectActAcct();
                selectActDebt();
                updateActAcct();
                updateActAcctCurr();
                insertColBadDebt();
                selectColBadDebt1();
//                pStatus = 1;
                
                /*I001轉催會計帳
                 * *******************************************************
    	            
    	            I001    D    1    155410016    非放款轉列之催收款項-信用卡墊款轉入    每月不定期批次作業產生轉催收起帳
    		            I001    C    2    130270032    應收信用卡款項-信用卡墊款                   每月不定期批次作業產生轉催收起帳
    		            I001    C    3    130270041    應收信用卡款項-循環信用息                   每月不定期批次作業產生轉催收起帳
    		            I001    C    4    130270024    應收信用卡款項-信用卡手續費                每月不定期批次作業產生轉催收起帳
    		            I001    C    5    130270024    應收信用卡款項-信用卡手續費                每月不定期批次作業產生轉催收起帳
                */
                
                /*I004轉催會計帳
                 * *******************************************************
    	            
    	           I004		D	130270041	 1	應收信用卡款項-循環信用息	轉催-違約金轉循環利息
						I004	C	130270024	2	應收信用卡款項-信用卡手續費	轉催-違約金轉循環利息
                */
                
                //判斷是個人戶放ID或公司放統編
                tmpstr1 = String.format("%s-%s人工轉催", hAcnoAcctType,hAcnoAcctHolderId);
                
                if (!hAcnoAcnoFlag.equals("1")) { 
                    tmpstr1 = String.format("%s-%s人工轉催", hAcnoAcctType,hAcnoCorpNo);
                }
                
                //違約金轉催時多一套會計帳
                if (t6TotalAmt[1] > 0) {
                    hVouchCdKind = "I004";
                    
                    for (inta = 1; inta < 10; inta++)
                    {
                        tTotalAmt[inta] = t6TotalAmt[inta];
                        if(debug)
                        showLogMessage("I", "", " 888-t6 get tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                    }
                    
                    tmpstr = "違約金轉循環息";
                    comcr.hGsvhMemo1 = tmpstr1;
                	comcr.hGsvhMemo2= tmpstr;
                    comcr.hGsvhModPgm = javaProgram;
                    vouchRtn();
                }
//                if (t6TotalAmt[1] > 0 ) {
//                    {                     
//                    	t6TotalAmt[1] = t6TotalAmt[1] + dTotalAmt[5];
//                        t6TotalAmt[2] = t6TotalAmt[2] + dTotalAmt[5];
//                        showLogMessage("I", "", " 777 get t6TotalAmt[1]=["+ t6TotalAmt[6]+"]");
//                        showLogMessage("I", "", " 777 get t6TotalAmt[2]=["+ t6TotalAmt[6]+"]");
//                        hVouchCdKind = "I001"; //舊C-01
//                        
//                    }
//                }
                                
                hVouchCdKind = "I001"; //舊C-01
//                tmpstr = String.format("%7d", comcr.str2long(hCwtrAlwBadDate) - 19110000);
//	            if (hAcnoAcctHolderId.length() != 0) {
//	              tmpstr1 = String.format("ID %s 轉催收", hAcnoAcctHolderId);
//		        } else {
//		          tmpstr1 = String.format("ID %s 轉催收", hAcnoCorpNo);
//		        }
                tmpstr="";              
                comcr.hGsvhMemo1 = tmpstr1;
                comcr.hGsvhMemo2 = tmpstr;
                comcr.hGsvhModPgm = javaProgram;
                
                if(debug1)
                showLogMessage("I", "", " 888 get AcnoFlag["+hAcnoAcnoFlag+"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");
                
                for (inta = 1; inta < 10; inta++)
                {
                    tTotalAmt[inta] = nTotalAmt[inta];
                    if(debug)
                    showLogMessage("I", "", " 888 get tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                }
                vouchRtn();
            }

            //modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要修改 COL_BAD_TRANS.PROC_FLAG、COL_BAD_TRANS.PROC_DATE。 mod by phopho 2018.12.25
            if ((hAcnoAcctStatus.equals("3")) && (hCwtrTransType.equals("4"))) { //<--催轉呆?
                hCbdtSrcAmt = 0;
                selectActAcct();
                for (inta = 0; inta < 10; inta++)
                    dTotalAmt[inta] = 0;
                selectActDebt1();

                if (dTotalAmt[1] > hCprmReqDebtLmt) {
                    hAcnoRecourseMark = "Y";
                } else {
                    hAcnoRecourseMark = "";
                }
                insertColBadDebt();
                selectColBadDebt1();
                for (inta = 1; inta < 10; inta++)
                {
                    tTotalAmt[inta] = dTotalAmt[inta];
                    
                    if(debug)
                    showLogMessage("I", "", " 777 get tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                }
                for (inta = 1; inta < 10; inta++)
                {
                    t4TotalAmt[inta] = t4TotalAmt[inta] + dTotalAmt[inta];
                    
                    if(debug)
                    showLogMessage("I", "", " 777 get t4TotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                    
                }
                if (dTotalAmt[1] > hCprmReqDebtLmt) {
                    hAcnoRecourseMark = "Y";
                    t5TotalAmt[1] = t5TotalAmt[1] + dTotalAmt[1];
                    t5TotalAmt[2] = t5TotalAmt[2] + dTotalAmt[1];
                    
                    if(debug)
                    {
                    showLogMessage("I", "", " 777 get t5TotalAmt[1]=["+ nTotalAmt[1]+"]");
                    showLogMessage("I", "", " 777 get t5TotalAmt[2]=["+ nTotalAmt[2]+"]");
                    }
                } else {
                    hAcnoRecourseMark = "";
                }
                //mod by phopho 2018.12.25
                updateColBadTrans();
            }
                      

            if ((hCwtrTransType.equals("4")) || (hCwtrTransType.equals("3"))) {
                hTempLegalDelayCode = "1";
//                h_temp_acct_sub_status = "1";

                if ((mCode >= 7) && (mCode < 12))
                    hTempLegalDelayCode = "2";
                if ((mCode >= 12) && (mCode < 24))
                    hTempLegalDelayCode = "3";
                if (mCode >= 24)
                    hTempLegalDelayCode = "4";
            }

            if (hCwtrTransType.equals("1")) {
//                h_temp_acct_sub_status = "1";
                hTempLegalDelayCode = "9";
            }

            updateActAcno();
            deleteColWaitTrans();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoPSeqno = "";
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoAcctHolderId = "";
        hAcnoAcctHolderIdCode = "";
        hAcnoIdPSeqno = "";
        hAcnoCorpNo = "";
        hAcnoCorpPSeqno = "";
        hAcnoCreditActNo = "";
        hAcnoLineOfCreditAmt = 0;
        hAcnoStmtCycle = "";
        hAcnoAcctStatus = "";
//        h_acno_acct_sub_status = "";
        hAcnoLegalDelayCode = "";
        hAcnoLawsuitMark = "";
        hAcnoLawsuitMarkDate = "";
        hAcnoNoDelinquentFlag = "";
        hAcnoNoDelinquentSDate = "";
        hAcnoNoDelinquentEDate = "";
        hAcnoNoCollectionFlag = "";
        hAcnoNoCollectionSDate = "";
        hAcnoNoCollectionEDate = "";
        hAcnoNewCycleMonth = "";
        hAcnoLastInterestDate = "";
        hAcnoIntRateMcode = 0;
        hAcnoRowid = "";

//        sqlCmd = "select a.p_seqno,";
//        sqlCmd += "a.acct_type,";
//        sqlCmd += "a.acct_key,";
//        sqlCmd += "c.id_no,";
//        sqlCmd += "c.id_no_code,";
//        sqlCmd += "a.id_p_seqno,";
//        sqlCmd += "d.corp_no,";
//        sqlCmd += "a.corp_p_seqno,";
//        sqlCmd += "a.credit_act_no,";
//        sqlCmd += "a.line_of_credit_amt,";
//        sqlCmd += "a.stmt_cycle,";
//        sqlCmd += "a.acct_status,";
////        sqlCmd += "a.acct_sub_status,";
//        sqlCmd += "a.legal_delay_code,";
//        sqlCmd += "decode(a.lawsuit_mark,'','N',a.lawsuit_mark) as lawsuit_mark,";
//        sqlCmd += "decode(a.lawsuit_mark_date,'','30000101',a.lawsuit_mark_date) as lawsuit_mark_date,";
//        sqlCmd += "new_cycle_month,";
//        sqlCmd += "last_interest_date,";
//        sqlCmd += "a.int_rate_mcode,";
//        sqlCmd += "a.rowid as rowid ";
//        sqlCmd += " from act_acno a , crd_idno c ";
//        sqlCmd += " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno ";
//        sqlCmd += "where a.p_seqno = ?  ";
////        sqlCmd += "and  acno_flag <> 'Y' ";
//        sqlCmd += "and  a.p_seqno = a.gp_no ";
//        sqlCmd += "and  a.id_p_seqno = c.id_p_seqno ";
//        setString(1, h_cwtr_p_seqno);
        
        sqlCmd = "select a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "c.id_no,";
        sqlCmd += "c.id_no_code,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "d.corp_no,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.acno_flag,";
        sqlCmd += "a.legal_delay_code,";
        sqlCmd += "decode(a.lawsuit_mark,'','N',a.lawsuit_mark) as lawsuit_mark,";
        sqlCmd += "decode(a.lawsuit_mark_date,'','30000101',a.lawsuit_mark_date) as lawsuit_mark_date,";
        sqlCmd += "new_cycle_month,";
        sqlCmd += "last_interest_date,";
        sqlCmd += "a.int_rate_mcode,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from act_acno a ";
        sqlCmd += " left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno "; 
        sqlCmd += " left join ptr_acct_type e on a.acct_type = e.acct_type ";  //判斷永不轉催
        sqlCmd += "where a.acno_p_seqno = ?  ";
        sqlCmd += "and  a.acno_flag <> 'Y' ";
        sqlCmd += "and  e.no_collection_flag <> 'Y' ";

    
        /*220622 sunny 備份邏輯-old
        sqlCmd += " from act_acno a , crd_idno c ";
        sqlCmd += " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno ";
        sqlCmd += "where a.acno_p_seqno = ?  ";
        sqlCmd += "and  a.acno_flag <> 'Y' ";
        sqlCmd += "and  a.id_p_seqno = c.id_p_seqno ";
        */
        setString(1, hCwtrPSeqno);
        
        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	if(debug)
            //showLogMessage("I", "", String.format("p_seqno[%s]", hCwtrPSeqno));
            comcr.errRtn(String.format("select_act_acno not found! p_seqno[%s]", hCwtrPSeqno), "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno");
            hAcnoAcctType = getValue("act_acno.acct_type");
            hAcnoAcctKey = getValue("act_acno.acct_key");
            hAcnoAcctHolderId = getValue("act_acno.id_no");
            hAcnoAcctHolderIdCode = getValue("act_acno.id_no_code");
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno");
            hAcnoCorpNo = getValue("act_acno.corp_no");
            hAcnoCorpPSeqno = getValue("act_acno.corp_p_seqno");
            hAcnoCreditActNo = getValue("act_acno.credit_act_no");
            hAcnoLineOfCreditAmt = getValueDouble("act_acno.line_of_credit_amt");
            hAcnoStmtCycle = getValue("act_acno.stmt_cycle");
            hAcnoAcctStatus = getValue("act_acno.acct_status");
            hAcnoAcnoFlag   = getValue("act_acno.acno_flag");
//            h_acno_acct_sub_status = getValue("acct_sub_status");
            hAcnoLegalDelayCode = getValue("act_acno.legal_delay_code");
            hAcnoLawsuitMark = getValue("act_acno.lawsuit_mark");
            hAcnoLawsuitMarkDate = getValue("act_acno.lawsuit_mark_date");
            hAcnoNewCycleMonth = getValue("act_acno.new_cycle_month");
            hAcnoLastInterestDate = getValue("act_acno.last_interest_date");
            hAcnoIntRateMcode = getValueInt("act_acno.int_rate_mcode");
            hAcnoRowid = getValue("act_acno.rowid");
        }
    }

    /***********************************************************************/
    void selectColBadDebt() throws Exception {
        hCbdtRowid = "";

        sqlCmd = "select rowid as rowid ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  trans_type = ? ";
        setString(1, hCwtrPSeqno);
        setString(2, hCwtrTransType);
        
        extendField = "col_bad_debt.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCbdtRowid = getValue("col_bad_debt.rowid");
        } else
            return;

        insertColBadDebthst();
        deleteColBadDebt();
    }

    /***********************************************************************/
    void insertColBadDebthst() throws Exception {

        sqlCmd = "insert into col_bad_debthst";
        sqlCmd += " select * from col_bad_debt where rowid = ? ";
        setRowId(1, hCbdtRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteColBadDebt() throws Exception {
        daoTable = "col_bad_debt";
        whereStr = "where rowid = ? ";
        setRowId(1, hCbdtRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_col_bad_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int selectCrdCard2() throws Exception {
        int hint = 0;

        sqlCmd = "select 1 cnt";
        sqlCmd += " from crd_card  ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and current_code = '0' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_2.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hint = getValueInt("crd_card_2.cnt");
        }

        return hint;
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
    	long lAmt;
        double tempEndBal;

        selectPtrWorkday();

        //20230721 AF,CF,PF於TCB均轉入CC催收款科目
        sqlCmd = "select ";
        sqlCmd += "decode(acct_code,'BL','CB','CA','CB','IT','CB','ID','CB','AO','CB','OT','CB','RI','CI','PN','CI','AI','CI','LF','CC','SF','CC','AF','CC','CF','CC','PF','CC',acct_code) h_temp_acct_code,";
        sqlCmd += "acct_code,";
        sqlCmd += "acct_month,";
        sqlCmd += "reference_no,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and acct_code != 'DP' ";
//        sqlCmd += "and acct_code != 'AF' ";
//        sqlCmd += "and acct_code != 'CF' ";
//        sqlCmd += "and acct_code != 'PF' ";
        sqlCmd += "and end_bal > 0 ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTempAcctCode = getValue("act_debt.h_temp_acct_code", i);
            hDebtAcctCode = getValue("act_debt.acct_code", i);
            hDebtAcctMonth = getValue("act_debt.acct_month", i);
            hDebtReferenceSeq = getValue("act_debt.reference_no", i);
            hDebtEndBal = getValueDouble("act_debt.end_bal", i);
            hDebtDAvailableBal = getValueDouble("act_debt.d_avail_bal", i);
            hDebtRowid = getValue("act_debt.rowid", i);

            //本金
            if ((hDebtAcctCode.equals("BL")) || (hDebtAcctCode.equals("CA")) 
            	|| (hDebtAcctCode.equals("IT")) || (hDebtAcctCode.equals("ID")) 
            	|| (hDebtAcctCode.equals("OT")) || (hDebtAcctCode.equals("AO"))) {
//                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
//                nTotalAmt[7] = nTotalAmt[7] + hDebtEndBal;
	              nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; 	//GEN_ACCT_M.dbcr_seq=1
	              nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal; 	//GEN_ACCT_M.dbcr_seq=2
            }

            //利息
            if (hDebtAcctCode.equals("RI")) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                nTotalAmt[6] = nTotalAmt[6] + hDebtEndBal;
            	  nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; 	//GEN_ACCT_M.dbcr_seq=1
	              nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal; 	//GEN_ACCT_M.dbcr_seq=3
            }
            
            //違約金
            if (hDebtAcctCode.equals("PN")) {
//              n_total_amt[3] = n_total_amt[3] + h_debt_end_bal;
//              n_total_amt[5] = n_total_amt[5] + h_debt_end_bal;
                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;   	//催收款項(總和
//              nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;   	//掛費用130270024(營運科)--不用
                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal; 		//掛利息130270041(風管科)--套用
                t6TotalAmt[1] = t6TotalAmt[1] + hDebtEndBal;   	//違約金金額
                t6TotalAmt[2] = t6TotalAmt[2] + hDebtEndBal;   	//違約金金額
            }
            
            //帳外息 TCB取消
//            if (hDebtAcctCode.equals("AI")) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                nTotalAmt[9] = nTotalAmt[9] + hDebtEndBal;
//            }
            
            //掛失費
            if (hDebtAcctCode.equals("LF")) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; //催收款項(總和
                 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; //費用130270024(營運科)
            }
            
            //20230721 AF,CF,PF於TCB均轉入CC催收款科目
            //-------------------------------------------
            //年費
            if (hDebtAcctCode.equals("AF")) {
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; //催收款項(總和
                 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; //費用130270024(營運科)
            }
            
           //預借現金手續費
            if (hDebtAcctCode.equals("CF")) {
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; //催收款項(總和
                 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; //費用130270024(營運科)
            }
            
            //雜項手續費
            if (hDebtAcctCode.equals("PF")) {
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; //催收款項(總和)
                 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; //費用130270024(營運科)
            }
            //-------------------------------------------
            
            //法訴費 TCB 轉催前轉成催收費用類
            if (hDebtAcctCode.equals("SF")) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                nTotalAmt[8] = nTotalAmt[8] + hDebtEndBal;
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; //催收款項(總和)
                 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; //費用130270024(營運科)
            }
            
            updateActDebt();
            //20230912 轉催部分增加平帳處理寫入jrnl            
            if(debug1)
                showLogMessage("I", "", " 770[1]-轉催平帳作業(selectActDebt) acct_code["+hDebtAcctCode+"]");
            insertActJrnl(1); //insert act_jrnl 舊科目(減項)
            insertActJrnl(2); //insert act_jrnl 新科目(加項)
            insertColBadDetail();

            /*
            if ((hDebtAcctCode.equals("AF")) || (hDebtAcctCode.equals("CF")) || (hDebtAcctCode.equals("PF"))) {
                if (hDebtEndBal > 0) {
                    tempEndBal = hDebtEndBal;
                    if (hDebtAcctCode.equals("AF"))
                        amtNovouchAf = amtNovouchAf + hDebtEndBal;
                    if (hDebtAcctCode.equals("CF"))
                        amtNovouchCf = amtNovouchCf + hDebtEndBal;
                    if (hDebtAcctCode.equals("PF"))
                        amtNovouchPf = amtNovouchPf + hDebtEndBal;
                    if (hWdayNextAcctMonth.compareTo(hDebtAcctMonth) != 0) {
                        selectActAcag();
                        hAcctMinPayBal = hAcctMinPayBal - tempEndBal;
                        if (hAcctMinPayBal < 0)
                            hAcctMinPayBal = 0;
                        hAcctRcMinPayBal = hAcctRcMinPayBal - tempEndBal;
                        if (hAcctRcMinPayBal < 0)
                            hAcctRcMinPayBal = 0;
                        hAcctTtlAmtBal = hAcctTtlAmtBal - tempEndBal;
                        if (hAcctTtlAmtBal < 0)
                            hAcctTtlAmtBal = 0;
                        if (hAcctRcMinPayBal < hAcctRcMinPayM0)
                            hAcctRcMinPayM0 = hAcctRcMinPayBal;
                    }
                    hDebtEndBal = tempEndBal;
                    insertCycPyaj(1);
                    hAcctAdjustDrCnt = hAcctAdjustDrCnt + 1;
                    hAcctAdjustDrAmt = hAcctAdjustDrAmt + hDebtEndBal;
                    hAcctAcctJrnlBal = hAcctAcctJrnlBal - hDebtEndBal;
                    insertActJrnl(1);
                    hDebtEndBal = 0;
                    hDebtDAvailableBal = 0;
                    updateActDebt();
                }
            } else {
                updateActDebt();
                insertColBadDetail();
            }
            */
        }

        /* 20230721 tcb先取消，計算cycle結帳日至轉催日之間的利息轉催收款利息*/
        /*
        if ((nTotalAmt[7] == 0) && (hAcctTempUnbillInterest == 0))
            return;
            
        //本金有欠款才計算
             
        hTempAcctCode = "CB";
        selectPtrActcode();

        if ((hWdayThisAcctMonth.compareTo(hAcnoNewCycleMonth) == 0) && (hAcnoNewCycleMonth.length() > 0)
                && (hAcnoLastInterestDate.length() != 0)) {
            hWdayThisCloseDate = hAcnoLastInterestDate;
        }

        hTempRateAmt = 0;
      //DB2 與 Oracle 對日期(時間)直接加減結果的表示方式不同, 語法須修正  phopho 2019.7.19
//        sqlCmd = "select (? * ? * ( to_char(to_date(?,'yyyymmdd'),'yyyymmdd') - to_char(to_date(?,'yyyymmdd'),'yyyymmdd') ))*0.0001 rate";
//        sqlCmd += " from dual ";
        //弱掃不過 Potential ReDoS phopho 2019.8.15
        sqlCmd = "select ( ((?) * (?)) * (days(to_date(?,'yyyymmdd')) - days(to_date(?,'yyyymmdd')))) *0.0001 rate_amt ";
        sqlCmd += "from dual ";
        setDouble(1, hTempRate);
        setDouble(2, nTotalAmt[7]);
        setString(3, hBusiBusinessDate);
        setString(4, hWdayThisCloseDate);
        
        showLogMessage("I", "", " 888 get h_temp_rate=["+ hTempRate +"]");
        showLogMessage("I", "", " 888 get n_total_amt[7]=["+ nTotalAmt[7] +"]");
        showLogMessage("I", "", " 888 get h_busi_business_date=["+ hBusiBusinessDate +"]");
        showLogMessage("I", "", " 888 get h_wday_this_close_date=["+ hWdayThisCloseDate +"]");
        
        extendField = "temp_rate.";
        
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("compute rate_amt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempRateAmt = getValueDouble("temp_rate.rate_amt");
            showLogMessage("I", "", " 888 get temp_rate.rate_amt=["+ hTempRateAmt +"]");
                      
        }

        lAmt = (long)(hTempRateAmt + hAcctTempUnbillInterest);
        hAcctTempUnbillInterest = 0;
        hTempRateAmt = lAmt;
        nTotalAmt[3] = nTotalAmt[3] + lAmt;
        nTotalAmt[9] = nTotalAmt[9] + lAmt;

        hTempAcctCode = "CI";
        selectPtrActcode();
        insertActDebt();
        hDebtAcctCode = "CI";
        hDebtEndBal = hTempRateAmt;
        hDebtDAvailableBal = hTempRateAmt;
        insertColBadDetail();
        hAcctAcctJrnlBal = hAcctAcctJrnlBal + hTempRateAmt;
        insertActJrnl(2);
        insertCycPyaj(2);
        */
    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisCloseDate = "";
        hWdayNextAcctMonth = "";
        hWdayThisAcctMonth = "";

        sqlCmd = "select stmt_cycle,";
        sqlCmd += "this_close_date,";
        sqlCmd += "next_acct_month,";
        sqlCmd += "this_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = ? ";
        setString(1, hAcnoStmtCycle);
        
        extendField = "ptr_workday.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("ptr_workday.stmt_cycle");
            hWdayThisCloseDate = getValue("ptr_workday.this_close_date");
            hWdayNextAcctMonth = getValue("ptr_workday.next_acct_month");
            hWdayThisAcctMonth = getValue("ptr_workday.this_acct_month");
        }
    }

    /***********************************************************************/
    void selectActAcag() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "pay_amt,";
        sqlCmd += "acct_month,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_acag ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "order by acct_month ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acag.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcagPayAmt = getValueDouble("act_acag.pay_amt", i);
            hAcagAcctMonth = getValue("act_acag.acct_month", i);
            hAcagRowid = getValue("act_acag.rowid", i);

            if (hDebtEndBal == 0) {
                break;
            }

            if (hDebtEndBal >= hAcagPayAmt) {
                hDebtEndBal -= hAcagPayAmt;
                hAcagPayAmt = 0;
                deleteActAcag();
            } else {
                hAcagPayAmt -= hDebtEndBal;
                hDebtEndBal = 0;
                updateActAcag();
            }
        }
    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {
        deleteActAcagCurr();

        daoTable = "act_acag";
        whereStr = "where rowid = ? ";
        setRowId(1, hAcagRowid);
        deleteTable();
    }

    /***********************************************************************/
    void deleteActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        whereStr = "where p_seqno  = ?  ";
        whereStr += "and acct_month  = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hAcagAcctMonth);
        deleteTable();
    }

    /***********************************************************************/
    void updateActAcag() throws Exception {
        updateActAcagCurr();

        daoTable = "act_acag";
        updateSQL = "pay_amt = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_user = ?,";
        updateSQL += " mod_pgm = ?,";
        updateSQL += " mod_seqno = ?";
        whereStr = "where rowid  = ? ";
        setDouble(1, hAcagPayAmt);
        setString(2, hAcnoModUser);
        setString(3, hAcnoModPgm);
        setLong(4, hAcnoModSeqno);
        setRowId(5, hAcagRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        updateSQL = "pay_amt  = ?,";
        updateSQL += " dc_pay_amt  = ?,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where p_seqno  = ?  ";
        whereStr += "and acct_month  = ? ";
        setDouble(1, hAcagPayAmt);
        setDouble(2, hAcagPayAmt);
        setString(3, hAcnoModPgm);
        setString(4, hAcnoPSeqno);
        setString(5, hAcagAcctMonth);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag_curr not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActDebt() throws Exception {
        hDebtReferenceSeq = "";

        sqlCmd = "select substr(?,3,2)||substr(to_char(bil_postseq.nextval,'0000000000'),4,8) reference_seq ";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        if (selectTable() > 0) {
            hDebtReferenceSeq = getValue("reference_seq");
        }

        selectCrdCard1();

        daoTable = "act_debt";
        extendField = daoTable + ".";
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);  //phopho add
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
        setValue(extendField+"post_date", hBusiBusinessDate);
        setValue(extendField+"item_order_normal", hDebtItemOrderNormal);
        setValue(extendField+"item_order_back_date", hDebtItemOrderBackDate);
        setValue(extendField+"item_order_refund", hDebtItemOrderRefund);
        setValue(extendField+"item_class_normal", hDebtItemClassNormal);
        setValue(extendField+"item_class_back_date", hDebtItemClassBackDate);
        setValue(extendField+"item_class_refund", hDebtItemClassRefund);
        setValue(extendField+"acct_month", hWdayNextAcctMonth);
        setValue(extendField+"stmt_cycle", hWdayStmtCycle);
        setValue(extendField+"bill_type", "OSSG");
        setValue(extendField+"txn_code", "AI");
        setValueDouble(extendField+"beg_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_beg_bal", hTempRateAmt);
        setValueDouble(extendField+"end_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_end_bal", hTempRateAmt);
        setValueDouble(extendField+"d_avail_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_d_avail_bal", hTempRateAmt);
        setValue(extendField+"card_no", hDebtCardNo);
        setValue(extendField+"acct_code", hTempAcctCode);
//        setValue("acct_item_cname", h_debt_acct_item_cname);  //no column
        setValue(extendField+"interest_date", hBusiBusinessDate);
        setValue(extendField+"purchase_date", hBusiBusinessDate);
//        setValue("acquire_date", h_busi_business_date);  //no column
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {
        hDebtCardNo = "";

        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card  ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_1.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtCardNo = getValue("crd_card_1.card_no");
        }
    }

    /***********************************************************************/
    void insertActJrnl(int type) throws Exception {
    	daoTable = "act_jrnl";
    	extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"acct_key", hAcnoAcctKey);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        // setValue("id_no", h_acno_acct_holder_id);
        // setValue("id_no_code", h_acno_acct_holder_id_code);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
        // setValue("corp_no", h_acno_corp_no);
        // setValue("corp_no_code", h_acno_corp_no_code);
        setValue(extendField+"acct_date", hBusiBusinessDate);
        setValue(extendField+"tran_class", type == 1 ? "A" : "B");
        setValue(extendField+"tran_type", type == 1 ? "CD01" : "CD02");
        setValue(extendField+"acct_code", type == 1 ? hDebtAcctCode:hTempAcctCode); //1使用原acct_code、2使用新的acct_code
        setValue(extendField+"dr_cr", type == 1 ? "D" : "C");
        setValueDouble(extendField+"transaction_amt", hDebtEndBal);
        setValueDouble(extendField+"dc_transaction_amt", hDebtEndBal);
        setValueDouble(extendField+"jrnl_bal", hAcctAcctJrnlBal);
        setValueDouble(extendField+"dc_jrnl_bal", hAcctAcctJrnlBal);
        setValueDouble(extendField+"item_bal", hDebtEndBal);
        setValueDouble(extendField+"dc_item_bal", hDebtEndBal);
        setValueDouble(extendField+"item_d_bal", hDebtDAvailableBal);
        setValueDouble(extendField+"dc_item_d_bal", hDebtDAvailableBal);
        setValue(extendField + "item_date", hDebtPostDate);      //20231119 add 原始交易入帳日期
        setValue(extendField+"reference_no", hDebtReferenceSeq); //20231119 add 交易參考號
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertCycPyaj(int classCode) throws Exception {
    	daoTable = "cyc_pyaj";
    	extendField = daoTable + ".";
        setValue(extendField+"p_seq", hAcnoPSeqno);
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
        // setValue("acct_key", h_acno_acct_key);
        setValue(extendField+"class_code", classCode == 1 ? "A" : "B");
        setValue(extendField+"payment_date", hBusiBusinessDate);
        setValueDouble(extendField+"payment_amount", hDebtEndBal * -1);
        setValueDouble(extendField+"dc_payment_amount", hDebtEndBal * -1);
        setValue(extendField+"payment_type", "CD01");
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"settlement_flag", "U");
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"fee_flag", "Y");
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        hAcnoModSeqno = comcr.getModSeq();

        daoTable = "act_acct";
        updateSQL = "acct_jrnl_bal = ?,";
        updateSQL += " temp_unbill_interest = ?,";
        updateSQL += " min_pay_bal = ?,";
        updateSQL += " rc_min_pay_bal = ?,";
        updateSQL += " rc_min_pay_m0 = ?,";
        updateSQL += " ttl_amt_bal = ?,";
        updateSQL += " adjust_dr_cnt = ?,";
        updateSQL += " adjust_dr_amt = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?";
        whereStr = "where rowid   = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctTempUnbillInterest);
        setDouble(3, hAcctMinPayBal);
        setDouble(4, hAcctRcMinPayBal);
        setDouble(5, hAcctRcMinPayM0);
        setDouble(6, hAcctTtlAmtBal);
        setInt(7, hAcctAdjustDrCnt);
        setDouble(8, hAcctAdjustDrAmt);
        setString(9, hAcnoModUser);
        setString(10, hAcnoModPgm);
        setLong(11, hAcnoModSeqno);
        setRowId(12, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        updateSQL = "acct_jrnl_bal   = ?,";
        updateSQL += " dc_acct_jrnl_bal  = ?,";
        updateSQL += " temp_unbill_interest = ?,";
        updateSQL += " dc_temp_unbill_interest = ?,";
        updateSQL += " min_pay_bal    = ?,";
        updateSQL += " dc_min_pay_bal   = ?,";
        updateSQL += " ttl_amt_bal    = ?,";
        updateSQL += " dc_ttl_amt_bal   = ?,";
        updateSQL += " adjust_dr_cnt   = ?,";
        updateSQL += " adjust_dr_amt   = ?,";
        updateSQL += " dc_adjust_dr_amt  = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = ?";
        whereStr = "where p_seqno     = ?  ";
        whereStr += "and  curr_code    = '901' ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctAcctJrnlBal);
        setDouble(3, hAcctTempUnbillInterest);
        setDouble(4, hAcctTempUnbillInterest);
        setDouble(5, hAcctMinPayBal);
        setDouble(6, hAcctMinPayBal);
        setDouble(7, hAcctTtlAmtBal);
        setDouble(8, hAcctTtlAmtBal);
        setInt(9, hAcctAdjustDrCnt);
        setDouble(10, hAcctAdjustDrAmt);
        setDouble(11, hAcctAdjustDrAmt);
        setString(12, hAcnoModPgm);
        setString(13, hAcnoPSeqno);
        updateTable();
        
        if(debug)
        showLogMessage("I", "", " 888 get hAcnoPSeqno=["+ hAcnoPSeqno +"]");
        
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        hAcctTempUnbillInterest = 0;
        hAcctMinPayBal = 0;
        hAcctRcMinPayBal = 0;
        hAcctRcMinPayM0 = 0;
        hAcctTtlAmtBal = 0;
        hAcctAdjustDrCnt = 0;
        hAcctAdjustDrAmt = 0;

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += "temp_unbill_interest,";
        sqlCmd += "min_pay_bal,";
        sqlCmd += "rc_min_pay_bal,";
        sqlCmd += "rc_min_pay_m0,";
        sqlCmd += "ttl_amt_bal,";
        sqlCmd += "adjust_dr_cnt,";
        sqlCmd += "adjust_dr_amt,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from act_acct ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueDouble("act_acct.acct_jrnl_bal");
            hAcctTempUnbillInterest = getValueDouble("act_acct.temp_unbill_interest");
            hAcctMinPayBal = getValueDouble("act_acct.min_pay_bal");
            hAcctRcMinPayM0 = getValueDouble("act_acct.rc_min_pay_bal");
            hAcctRcMinPayBal = getValueDouble("act_acct.rc_min_pay_m0");
            hAcctTtlAmtBal = getValueDouble("act_acct.ttl_amt_bal");
            hAcctAdjustDrCnt = getValueInt("act_acct.adjust_dr_cnt");
            hAcctAdjustDrAmt = getValueDouble("act_acct.adjust_dr_amt");
            hAcctRowid = getValue("act_acct.rowid");
        }
    }

    /***********************************************************************/
    void selectActDebt1() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "decode(acct_code,'CB','DB','CI','DB','CC','DB') h_temp_acct_code,";
        sqlCmd += "decode(acct_code,'CB','B','CI','I','CC','C') h_debt_acct_item_type,";
        sqlCmd += "acct_code,";
        sqlCmd += "reference_no,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and (acct_code = 'CB' ";
        sqlCmd += "or acct_code = 'CC' ";
        sqlCmd += "or acct_code = 'CI') ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTempAcctCode = getValue("act_debt_1.h_temp_acct_code", i);
            hDebtAcctItemType = getValue("act_debt_1.h_debt_acct_item_type", i);
            hDebtAcctCode = getValue("act_debt_1.acct_code", i);
            hDebtPostDate = getValue("act_debt.post_date",i);
            hDebtReferenceSeq = getValue("act_debt_1.reference_no", i);
            hDebtEndBal = getValueDouble("act_debt_1.end_bal", i);
            hDebtDAvailableBal = getValueDouble("act_debt_1.d_avail_bal", i);
            hDebtRowid = getValue("act_debt_1.rowid", i);

            if (hDebtAcctCode.equals("CB")) {
                dTotalAmt[1] = dTotalAmt[1] + hDebtEndBal;
                dTotalAmt[2] = dTotalAmt[2] + hDebtEndBal;
                //dTotalAmt[2] = dTotalAmt[2] + hDebtEndBal;
            }

            if (hDebtAcctCode.equals("CI")) {
                dTotalAmt[1] = dTotalAmt[1] + hDebtEndBal;
                dTotalAmt[2] = dTotalAmt[2] + hDebtEndBal;
                //dTotalAmt[3] = dTotalAmt[3] + hDebtEndBal;
            }

            if (hDebtAcctCode.equals("CC")) {
                dTotalAmt[1] = dTotalAmt[1] + hDebtEndBal;
                dTotalAmt[2] = dTotalAmt[2] + hDebtEndBal;
//                dTotalAmt[4] = dTotalAmt[4] + hDebtEndBal;
            }

            selectColBadDetail();
            updateActDebt();
            //20230912 轉呆部分，增加平帳處理寫入jrnl
            if(debug1)
                showLogMessage("I", "", " 770[2]-轉呆平帳作業(selectActDebt1) acct_code["+hDebtAcctCode+"]");
            insertActJrnl(1); //insert act_jrnl 舊科目(減項)
            insertActJrnl(2); //insert act_jrnl 新科目(加項)
            insertColBadDetail();
          

        }
    }

    /***********************************************************************/
    void selectColBadDetail() throws Exception {
        hDebtOrgItemEname = "";
        sqlCmd = "select acct_code ";
        sqlCmd += " from col_bad_detail  ";
        sqlCmd += "where trans_type  = '3'  ";
        sqlCmd += "and p_seqno  = ?  ";
        sqlCmd += "and new_acct_code = ?  "; // new_item_ename
        sqlCmd += "and reference_no = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hDebtAcctCode);
        setString(3, hDebtReferenceSeq);
        
        extendField = "col_bad_detail.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtOrgItemEname = getValue("col_bad_detail.acct_code");
        }
    }

    /***********************************************************************/
    void updateActDebt() throws Exception {
        hAcnoModSeqno = comcr.getModSeq();
        selectPtrActcode();

        daoTable = "act_debt";
        updateSQL = "acct_code  = ?,";
        updateSQL += " org_acct_code  = decode(cast(? as varchar(2)),'DB',cast(? as varchar(2)),''),"; // org_item_ename
        updateSQL += " acct_code_type  = decode(cast(? as varchar(2)),'DB',cast(? as varchar(1)),''),"; // acct_item_type
        updateSQL += " item_order_normal = ?,";
        updateSQL += " item_order_back_date = ?,";
        updateSQL += " item_order_refund = ?,";
        updateSQL += " item_class_normal = ?,";
        updateSQL += " item_class_back_date = ?,";
        updateSQL += " item_class_refund = ?,";
//        updateSQL += " acct_item_cname  = ?,"; // no column
        updateSQL += " end_bal    = ?,";
        updateSQL += " dc_end_bal   = ?,";
        updateSQL += " d_avail_bal  = ?,";
        updateSQL += " dc_d_avail_bal = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?";
        whereStr = "where rowid = ? ";
        setString(1, hTempAcctCode);
        setString(2, hDebtAcctCode); //20231119 原TempAcctCode改為轉催前原始科目hDebtAcctCode
        setString(3, hDebtOrgItemEname);
        setString(4, hTempAcctCode);
        setString(5, hDebtAcctItemType);
        setString(6, hDebtItemOrderNormal);
        setString(7, hDebtItemOrderBackDate);
        setString(8, hDebtItemOrderRefund);
        setString(9, hDebtItemClassNormal);
        setString(10, hDebtItemClassBackDate);
        setString(11, hDebtItemClassRefund);
//        setString(12, h_debt_acct_item_cname);
        setDouble(12, hDebtEndBal);
        setDouble(13, hDebtEndBal);
        setDouble(14, hDebtDAvailableBal);
        setDouble(15, hDebtDAvailableBal);
        setString(16, hAcnoModUser);
        setString(17, hAcnoModPgm);
        setLong(18, hAcnoModSeqno);
        setRowId(19, hDebtRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        hDebtItemOrderNormal = "";
        hDebtItemOrderBackDate = "";
        hDebtItemOrderRefund = "";
        hDebtItemClassNormal = "";
        hDebtItemClassBackDate = "";
        hDebtItemClassRefund = "";
        hDebtAcctItemCname = "";
        hTempRate = 0;

        sqlCmd = "select item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "chi_long_name,";
        sqlCmd += "decode(inter_rate_code,'1',?, '2',?, '3',?, '4',?, '5',?, '6',?,0) temp_rate ";
        sqlCmd += " from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setDouble(1, hAgenRevolvingInterest1);
        setDouble(2, hAgenRevolvingInterest2);
        setDouble(3, hAgenRevolvingInterest3);
        setDouble(4, hAgenRevolvingInterest4);
        setDouble(5, hAgenRevolvingInterest5);
        setDouble(6, hAgenRevolvingInterest6);
        setString(7, hTempAcctCode);
        
        extendField = "ptr_actcode.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actcode not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtItemOrderNormal = getValue("ptr_actcode.item_order_normal");
            hDebtItemOrderBackDate = getValue("ptr_actcode.item_order_back_date");
            hDebtItemOrderRefund = getValue("ptr_actcode.item_order_refund");
            hDebtItemClassNormal = getValue("ptr_actcode.item_class_normal");
            hDebtItemClassBackDate = getValue("ptr_actcode.item_class_back_date");
            hDebtItemClassRefund = getValue("ptr_actcode.item_class_refund");
            hDebtAcctItemCname = getValue("ptr_actcode.chi_long_name");
            hTempRate = getValueDouble("ptr_actcode.temp_rate");
            if(debug)
            showLogMessage("I", "", " 888 get h_temp_rate1345=["+ hTempRate +"]");
        }
    }

    /***********************************************************************/
    void insertColBadDetail() throws Exception {
        hCbdtSrcAmt = hCbdtSrcAmt + hDebtEndBal;
        
        daoTable = "col_bad_detail";
        extendField = daoTable + ".";
        setValue(extendField+"trans_type", hTempAcctStatus);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"trans_date", hBusiBusinessDate);
        setValue(extendField+"acct_code", hDebtAcctCode);
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
        setValueDouble(extendField+"end_bal", hDebtEndBal);
        setValueDouble(extendField+"d_avail_bal", hDebtDAvailableBal);
        setValue(extendField+"new_acct_code", hTempAcctCode);
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_detail duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColBadDebt() throws Exception {
        if (hAcctAcctJrnlBal >= hCprmTerminateAmt1) {
            hTempMonth = hCprmTerminateYear1 * 12 - hCprmTerminateMonth1;
        } else {
            hTempMonth = hCprmTerminateYear2 * 12 - hCprmTerminateMonth2;
        }

        daoTable = "col_bad_debt";
        extendField = daoTable + ".";
        setValue(extendField+"trans_type", hCwtrTransType);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"trans_date", hBusiBusinessDate);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"acct_key", hAcnoAcctKey);
        setValue(extendField+"id_no", hAcnoAcctHolderId);
        setValue(extendField+"id_code", hAcnoAcctHolderIdCode);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"corp_no", hAcnoCorpNo);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
        setValueDouble(extendField+"src_amt", hCbdtSrcAmt);
        setValue(extendField+"credit_act_no", hAcnoCreditActNo);
        setValueDouble(extendField+"line_of_credit_amt", hAcnoLineOfCreditAmt);
        setValue(extendField+"alw_bad_date", hCwtrAlwBadDate);
        setValue(extendField+"paper_conf_date", hCwtrPaperConfDate);
        setValue(extendField+"terminate_date", hCwtrPaperConfDate);
        setValue(extendField+"settle_date", hCwtrValidCancelDate);
        setValue(extendField+"paper_name", hCwtrPaperName);
        setValue(extendField+"recourse_mark", hAcnoRecourseMark);
        setValue(extendField+"recourse_mark_date", hAcnoRecourseMark.equals("Y") ? hBusiBusinessDate : "");
        setValue(extendField+"apr_date", hCwtrConfDate);
        setValue(extendField+"apr_user", hCwtrConfUser);
        setValue(extendField+"run_user", hCwtrCreateUser);
        setValue(extendField+"tran_source", "2");
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_debt duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColBadDebt1() throws Exception {
        hTempCbdtCnt = 0;
        hTempCbdtAmt = 0;

        sqlCmd = "select count(src_amt) h_temp_cbdt_cnt,";
        sqlCmd += "sum(src_amt) h_temp_cbdt_amt ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where id_p_seqno = ?  ";
        sqlCmd += "and  trans_type = ? ";
        setString(1, hAcnoIdPSeqno);
        setString(2, hCwtrTransType);
        
        extendField = "col_bad_debt_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCbdtCnt = getValueInt("col_bad_debt_1.h_temp_cbdt_cnt");
            hTempCbdtAmt = getValueDouble("col_bad_debt_1.h_temp_cbdt_amt");
        }

        if (hTempCbdtCnt < 2)
            return;

        updateColBadDebt();
    }

    /***********************************************************************/
    void updateColBadDebt() throws Exception {
        if (hTempCbdtAmt >= hCprmTerminateAmt1) {
            hTempMonth = hCprmTerminateYear1 * 12 - hCprmTerminateMonth1;
        } else {
            hTempMonth = hCprmTerminateYear2 * 12 - hCprmTerminateMonth2;
        }
        
        daoTable = "col_bad_debt";
//        updateSQL = "terminate_date = to_char(add_months(to_date(paper_conf_date,'yyyymmdd'),?),'yyyymmdd'),";
        updateSQL = "terminate_date = to_char(add_months(to_date(decode(paper_conf_date,'',to_char(sysdate,'yyyymmdd'),paper_conf_date),'yyyymmdd'),?),'yyyymmdd'),";
        updateSQL += " mod_time = sysdate";
        whereStr = "where id_p_seqno = ?  ";
        whereStr += "and trans_type = ? ";
        setInt(1, hTempMonth);
        setString(2, hAcnoIdPSeqno);
        setString(3, hCwtrTransType);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_bad_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        if ((hAcnoAcctStatus.compareTo(hTempAcctStatus) == 0)
//                && (h_acno_acct_sub_status.compareTo(h_temp_acct_sub_status) == 0)
                && (hAcnoLegalDelayCode.compareTo(hTempLegalDelayCode) == 0))
            return;

        hAcnoModSeqno = comcr.getModSeq();
        if (!hTempAcctStatus.equals("4"))
            hAcnoRecourseMark = "";

        daoTable = "act_acno";
        updateSQL = "acct_status   = ?,";
//        updateSQL += " acct_sub_status  = ?,";
        updateSQL += " status_change_date = ?,";
        updateSQL += " legal_delay_code  = ?,";
        updateSQL += " legal_delay_code_date = ?,";
        updateSQL += " recourse_mark   = ?,";
        updateSQL += " org_delinquent_date = decode(org_delinquent_date,'',cast(? as varchar(8)),org_delinquent_date),";
        updateSQL += " recourse_mark_date = decode(cast(? as varchar(1)),'Y',cast(? as varchar(8)),''),";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hTempAcctStatus);
//        setString(2, h_temp_acct_sub_status);
        setString(2, hBusiBusinessDate);
        setString(3, hTempLegalDelayCode);
        setString(4, hBusiBusinessDate);
        setString(5, hAcnoRecourseMark);
        setString(6, hCwtrValidCancelDate);
        setString(7, hAcnoRecourseMark);
        setString(8, hBusiBusinessDate);
        setString(9, hAcnoModUser);
        setString(10, hAcnoModPgm);
        setLong(11, hAcnoModSeqno);
        setRowId(12, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteColWaitTrans() throws Exception {
        daoTable = "col_wait_trans";
        whereStr = "where rowid = ? ";
        setRowId(1, hCwtrRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_col_wait_trans not found!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void deleteColWaitTrans0() throws Exception {
    	//modify, 清空 col_wait_trans 未覆核資料 mod by phopho 2019.2.15
        daoTable = "col_wait_trans";
        whereStr = "where decode(apr_flag,'','N',apr_flag) = 'N' ";
        deleteTable();
    }
    
    /***********************************************************************/
    void deleteColBadTrans0() throws Exception {
    	//modify, 清空 col_bad_trans 未覆核資料 mod by phopho 2019.2.15
        daoTable = "col_bad_trans";
        whereStr = "where decode(apr_flag,'','N',apr_flag) = 'N' ";
        deleteTable();
    }
    
    /***********************************************************************/
    void updateColBadTrans() throws Exception {
    	//modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要修改 COL_BAD_TRANS.PROC_FLAG、COL_BAD_TRANS.PROC_DATE。 mod by phopho 2018.12.25
        daoTable = "col_bad_trans";
        updateSQL = " proc_flag = ?,";
        updateSQL += " proc_date = ?, ";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno = nvl(mod_seqno,0)+1 ";
        whereStr = "where p_seqno = ? ";
        whereStr += "and decode(proc_flag,'','N',proc_flag) <> 'Y' ";
        setString(1, "Y");
        setString(2, hBusiBusinessDate);
        setString(3, hAcnoModUser);
        setString(4, hAcnoModPgm);
        setString(5, hCwtrPSeqno);
        updateTable();
    }

    /***********************************************************************/
    void vouchRtn() throws Exception {

        comcr.startVouch("4", hVouchCdKind);

//        comcr.hGsvhMemo1 = tmpstr1;
//        comcr.hGsvhMemo2 = comcr.hGsvhMemo2;
        
        if(debug1)
        showLogMessage("I", "", " 999 get AcnoFlag["+hAcnoAcnoFlag+"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");

        sqlCmd = "select ";
        sqlCmd += "gen_sys_vouch.ac_no,";
        sqlCmd += "gen_sys_vouch.dbcr_seq,";
        sqlCmd += "gen_sys_vouch.dbcr,";
        sqlCmd += "gen_acct_m.memo3_kind,";
        sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) memo3_flag,";
        sqlCmd += "decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) dr_flag,";
        sqlCmd += "decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) cr_flag ";
        sqlCmd += "from gen_sys_vouch, gen_acct_m ";
        sqlCmd += "where std_vouch_cd = ? ";
        sqlCmd += "and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
        sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
        setString(1, hVouchCdKind);
        
        extendField = "gen_sys_vouch.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTAcNo = getValue("gen_sys_vouch.ac_no", i);
            hTSeqno = getValueInt("gen_sys_vouch.dbcr_seq", i);
            hTDbcr = getValue("gen_sys_vouch.dbcr", i);
            hTMemo3Kind = getValue("gen_sys_vouch.memo3_kind", i);
            hTMemo3Flag = getValue("gen_sys_vouch.memo3_flag", i);
            hTDrFlag = getValue("gen_sys_vouch.dr_flag", i);
            hTCrFlag = getValue("gen_sys_vouch.cr_flag", i);
            
            tMemo3 = "";

            if (tTotalAmt[hTSeqno] != 0) {
            	comcr.hGsvhMemo1= tmpstr1;
            	comcr.hGsvhMemo2= comcr.hGsvhMemo2;
            	comcr.hGsvhCurr = "00";
                comcr.detailVouch(hTAcNo, hTSeqno, tTotalAmt[hTSeqno]);
               
                if(debug)
                showLogMessage("I", "", " 999-2 get AcnoFlag["+ hAcnoAcnoFlag +"],tTotalAmt["+tTotalAmt[hTSeqno]+"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");

            }
        }
    }

    /***********************************************************************/
    void novouchReportRtn() throws Exception {

        temstr = String.format("%s/reports/COL_B009_NOVOU_%4.4s", comc.getECSHOME(), comc.getSubString(hBusiBusinessDate,4));
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱  :COL_B009_NOVOU", 3);
        buf = comcr.insertStrCenter(buf, "催收(人工線上)現金制未起帳金額報表", 80);
        buf = comcr.insertStr(buf, "頁次:", 68);
        szTmp = String.format("%04d", 1);
        buf = comcr.insertStr(buf, szTmp, 73);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        sqlCmd = "select substr(to_char(to_number(?)- 19110000,'0000000'),2,7) ";
        sqlCmd += " from ptr_businday ";
        setString(1, comcr.hSystemVouchDate);
        if (selectTable() > 0) {
            hBusinssChiDate = getValue("h_businss_chi_date");
        }

        buf = "";
        buf = comcr.insertStr(buf, "單    位:", 1);
        buf = comcr.insertStr(buf, "009", 11);
        buf = comcr.insertStr(buf, "交易日期:", 58);
        String cDate = String.format("%3.3s年%2.2s月%2.2s日", hBusinssChiDate,
        		comc.getSubString(hBusinssChiDate,3),comc.getSubString(hBusinssChiDate,5));
        buf = comcr.insertStr(buf, cDate, 68);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "調 整 性 質  :", 10);
        buf = comcr.insertStr(buf, " D 檔", 25);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "幣        別 :", 10);
        buf = comcr.insertStr(buf, "TWD", 25);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        for (int i = 0; i < 78; i++)
            buf += "-";
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "年 費 金 額   :", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "預 現 手 續 費:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchCf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "其 它 手 續 費:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchPf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳 外 息 金 額:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAi);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        for (int i = 0; i < 78; i++)
            buf += "-";
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        comc.writeReport(temstr, lparNovou);
        comcr.lpRtn("COL_D_VOUCH", "");

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB009 proc = new ColB009();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
