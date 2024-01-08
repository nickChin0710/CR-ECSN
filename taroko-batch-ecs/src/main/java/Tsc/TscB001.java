/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 106/06/01  V1.00.00   Edson       program initial                           *
* 108/08/23  V1.11.01   Chen        RECS-s1080819-087 15000 => 12000          *
* 109/04/28  V1.11.02   Brian       update to V1.11.01                        *
* 109-11-13  V1.00.03   tanwei      updated for project coding standard       *
* 112/06/21  V1.00.04   JeffKung    for TCB                                   *
*******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡簽帳資料檔(STMT)資料處理程式*/
public class TscB001 extends AccessDAO {
    private boolean debugT = false;
    private final String progname = "悠遊卡簽帳資料檔(STMT)資料處理程式  112/06/21 V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug   = 1;
    String hCallBatchSeqno = "";

    String hStmtCrtDate = "";
    String hBusiBusinessDate = "";
    String hBillAcctMonth = "";
    String hStmtCrtTime = "";
    String hTspmItemEnameBl = "";
    String hTspmItemEnameIt = "";
    String hTspmItemEnameId = "";
    String hTspmItemEnameCa = "";
    String hTspmItemEnameAo = "";
    String hTspmItemEnameOt = "";
    String hTspmExclMccFlag = "";
    String hTspmExclMchtGroupFlag = "";
    String hMBillRealCardNo = "";
    double hMBillDestinationAmt = 0;
    int hBillDestinationCnt = 0;
    double hBillDestinationAmt = 0;
    double hStmtFeedbackAmt = 0;
    String hBillRealCardNo = "";
    double hTshtDestinationAmt = 0;
    double hTshtFeedbackAmt = 0;
    String hTrlgTranCode = "";
    double hTrlgTranAmt = 0;
    int hTempCnt = 0;
    long hTspmRepayAmt1s = 0;
    long hTspmRepayAmt2s = 0;
    long hTspmRepayAmt3s = 0;
    long hTspmRepayAmt4s = 0;
    long hTspmRepayAmt5s = 0;
    double hTspmRepayRate1 = 0;
    double hTspmRepayRate2 = 0;
    double hTspmRepayRate3 = 0;
    double hTspmRepayRate4 = 0;
    double hTspmRepayRate5 = 0;
    String hTspmExclMchtFlag = "";
    String hTfinRunDay = "";
    double hTsrdRefundAmt = 0;
    long[] nTempRepayAmt = new long[10];
    double[] nTempRepayRate = new double[10];

    int forceFlag = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
                comc.errExit("Usage : TscB001 [notify_date] [flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hStmtCrtDate = "";
            forceFlag = 0;
            
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hStmtCrtDate = args[0];
            } else if (args.length >= 2) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hStmtCrtDate = args[0];
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
            }

            selectPtrBusinday();
            if(hStmtCrtDate.substring(6, 8).equals("01") == false) {
               exceptExit = 0;
               String stderr = String.format("本程式限每月01日 7:00 前執行 [%s]",hStmtCrtDate);
               comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            showLogMessage("I", "", String.format("處理月份 [%s]", hBillAcctMonth));
            if (forceFlag == 0) {
                if (selectTscStmtLoga() != 0) {
                	exceptExit = 0;
                    comcr.errRtn("程式結束", "", hCallBatchSeqno);
                }
            }

            deleteTscStmtHst();
            deleteTscStmtLog();
            deleteTscRefundLog();
            //selectTscStmtParm();
            hTshtDestinationAmt = 0;
            hTshtFeedbackAmt = 0;
            selectBilBill();

            insertTscStmtHst();
            showLogMessage("I", "", String.format("Process records = [%d]", totalCnt));
            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束";
            showLogMessage("I", "", comcr.hCallErrorDesc);
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
        sqlCmd  = "select business_date,";
        sqlCmd += "to_char( decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'07'),1," 
                + "sysdate+1 days,sysdate), 'yyyymmdd') h_stmt_crt_date,";
        sqlCmd += "decode(cast(? as varchar(10)),'',"
                + "to_char( decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'07'),"
                + "1,add_months(sysdate+1 days,-1),"
                + "add_months(sysdate,-1)), 'yyyymm'),to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')) h_bill_acct_month,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_stmt_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hStmtCrtDate);
        setString(2, hStmtCrtDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hStmtCrtDate = hStmtCrtDate.length() == 0 ? getValue("h_stmt_crt_date") : hStmtCrtDate;
            hBillAcctMonth = getValue("h_bill_acct_month");
            hStmtCrtTime = getValue("h_stmt_crt_time");
        }
    }
    /***********************************************************************/
    int selectTscStmtLoga() throws Exception {
        sqlCmd  = "select 1 cnt ";
        sqlCmd += " from tsc_stmt_log  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBillAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCnt = getValueInt("cnt");
        } else
            return (0);

        showLogMessage("I", "", String.format("本月[%s]簽帳資料已產生, 不可重複執行 , 請通知相關人員處理(error)", hBillAcctMonth));
        return (1);
    }
    /***********************************************************************/
    void deleteTscStmtHst() throws Exception {
        daoTable = "tsc_stmt_hst";
        whereStr = "where acct_month = ? ";
        setString(1, hBillAcctMonth);
        deleteTable();
    }
    /***********************************************************************/
    void deleteTscStmtLog() throws Exception {
        daoTable = "tsc_stmt_log";
        whereStr = "where acct_month = ? ";
        setString(1, hBillAcctMonth);
        deleteTable();

    }
    /***********************************************************************/
    void deleteTscRefundLog() throws Exception {
        daoTable = "tsc_refund_log";
        whereStr = "where acct_month = ? ";
        setString(1, hBillAcctMonth);
        deleteTable();

    }
    /***********************************************************************/
    void selectTscStmtParm() throws Exception {
        hTspmRepayAmt1s = 0;
        hTspmRepayAmt2s = 0;
        hTspmRepayAmt3s = 0;
        hTspmRepayAmt4s = 0;
        hTspmRepayAmt5s = 0;
        hTspmRepayRate1 = 0;
        hTspmRepayRate2 = 0;
        hTspmRepayRate3 = 0;
        hTspmRepayRate4 = 0;
        hTspmRepayRate5 = 0;
        hTspmItemEnameBl = "";
        hTspmItemEnameCa = "";
        hTspmItemEnameId = "";
        hTspmItemEnameAo = "";
        hTspmItemEnameIt = "";
        hTspmItemEnameOt = "";
        hTspmExclMchtFlag = "";

        sqlCmd = "select repay_amt1_s,";
        sqlCmd += "repay_amt2_s,";
        sqlCmd += "repay_amt3_s,";
        sqlCmd += "repay_amt4_s,";
        sqlCmd += "repay_amt5_s,";
        sqlCmd += "repay_rate1,";
        sqlCmd += "repay_rate2,";
        sqlCmd += "repay_rate3,";
        sqlCmd += "repay_rate4,";
        sqlCmd += "repay_rate5,";
        sqlCmd += "item_ename_bl,";
        sqlCmd += "item_ename_ca,";
        sqlCmd += "item_ename_id,";
        sqlCmd += "item_ename_ao,";
        sqlCmd += "item_ename_it,";
        sqlCmd += "item_ename_ot,";
        sqlCmd += "excl_mcht_flag, ";
        sqlCmd += "excl_mcc_flag, ";
        sqlCmd += "excl_mcht_group_flag ";
        sqlCmd += " from tsc_stmt_parm  ";
        sqlCmd += "where apr_flag = 'Y' fetch first 1 rows only";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_stmt_parm not found!", "", hCallBatchSeqno);
        }
        hTspmRepayAmt1s   = getValueLong("repay_amt1_s");
        hTspmRepayAmt2s   = getValueLong("repay_amt2_s");
        hTspmRepayAmt3s   = getValueLong("repay_amt3_s");
        hTspmRepayAmt4s   = getValueLong("repay_amt4_s");
        hTspmRepayAmt5s   = getValueLong("repay_amt5_s");
        hTspmRepayRate1    = getValueDouble("repay_rate1");
        hTspmRepayRate2    = getValueDouble("repay_rate2");
        hTspmRepayRate3    = getValueDouble("repay_rate3");
        hTspmRepayRate4    = getValueDouble("repay_rate4");
        hTspmRepayRate5    = getValueDouble("repay_rate5");
        hTspmItemEnameBl  = getValue("item_ename_bl");
        hTspmItemEnameCa  = getValue("item_ename_ca");
        hTspmItemEnameId  = getValue("item_ename_id");
        hTspmItemEnameAo  = getValue("item_ename_ao");
        hTspmItemEnameIt  = getValue("item_ename_it");
        hTspmItemEnameOt  = getValue("item_ename_ot");
        hTspmExclMchtFlag = getValue("excl_mcht_flag");
        hTspmExclMccFlag  = getValue("excl_mcc_flag");
        hTspmExclMchtGroupFlag = getValue("excl_mcht_group_flag");

        nTempRepayAmt[0] = hTspmRepayAmt1s;
        nTempRepayAmt[1] = hTspmRepayAmt2s;
        nTempRepayAmt[2] = hTspmRepayAmt3s;
        nTempRepayAmt[3] = hTspmRepayAmt4s;
        nTempRepayAmt[4] = hTspmRepayAmt5s;
        nTempRepayRate[0] = hTspmRepayRate1;
        nTempRepayRate[1] = hTspmRepayRate2;
        nTempRepayRate[2] = hTspmRepayRate3;
        nTempRepayRate[3] = hTspmRepayRate4;
        nTempRepayRate[4] = hTspmRepayRate5;
    }
	/***********************************************************************/
	void selectBilBill() throws Exception {
		daoTable = "bil_fiscdtl";

		sqlCmd = "SELECT a.batch_date,a.ecs_real_card_no,a.ecs_platform_kind,a.mcht_chi_name,      ";
		sqlCmd += "       a.ecs_sign_code,a.mcc_code,a.ecs_tx_code,round(a.dest_amt) dest_amt,     ";
		sqlCmd += "       a.ecs_bill_type                                                          ";
		sqlCmd += "FROM bil_fiscdtl a,                                                             ";
		sqlCmd += "    (select card_no                                                             ";
		sqlCmd += "     from tsc_card                                                              ";
		sqlCmd += "     where 1=1                                                                  ";
		sqlCmd += "     and autoload_flag  = 'Y'                                                   ";
		sqlCmd += "     and ((current_code = '0' and substr(new_end_date,1,6) > ? ) or             ";
		sqlCmd += "           (current_code!='0' and                                               ";
		sqlCmd += "            substr(decode(oppost_date ,'','19110101', oppost_date),1,6) > ?))   ";
		sqlCmd += "     and ((substr(decode(lock_date ,'','30001231', lock_date)  ,1,6) > ?) and   ";
		sqlCmd += "          (substr(decode(balance_date,'','30001231',balance_date),1,6) > ?) and ";
		sqlCmd += "          (substr(decode(return_date ,'','30001231',return_date) ,1,6) > ?))    ";
		sqlCmd += "     GROUP by card_no ) c                                                       ";
		sqlCmd += "WHERE a.card_no = c.card_no                                                     ";
		sqlCmd += "AND   a.batch_date like ?                                                       ";
		sqlCmd += "AND   a.ecs_bill_type = 'FISC'                                                  ";

		setString(1, hBillAcctMonth);
		setString(2, hBillAcctMonth);
		setString(3, hBillAcctMonth);
		setString(4, hBillAcctMonth);
		setString(5, hBillAcctMonth);
		setString(6, hBillAcctMonth+"%");

		//平台交易
		String[] listOfSkipKind = new String[]
			    {"f1","G1","G2","d1","M1","e1",
			     "10","11","12","13","14",
			     "20","21","22","23","24","25",
			     "V1","V2","V3","V4","V5","V6",
			     "FL","CL"};
		
		//特定MCC
		String[] listOfSkipMCC = new String[]
				{"9311","8398","0000","","0037","5960",
			     "5965","6300"};

		String keepCardNo = "";
		boolean firstCardNo = true;
		
		openCursor();
		while (fetchTable()) {
			
			for (int i = 0; i < listOfSkipKind.length; i++) {
				if (getValue("ecs_platform_kind").equals(listOfSkipKind[i])) {
					continue;
				}
			}
			
			for (int j = 0; j < listOfSkipMCC.length; j++) {
				if (getValue("mcc_code").equals(listOfSkipMCC[j])) {
					continue;
				}
			}
			
			//特店中文內含保險兩個字
			if (getValue("mcht_chi_name").indexOf("保險") > 0 ) continue;
			
			hBillRealCardNo = getValue("ecs_real_card_no");
			if (keepCardNo.equals(hBillRealCardNo) == false ) {
				if (firstCardNo==false) {
					if (hBillDestinationAmt > 0) {
						insertTscStmtLog(keepCardNo);
					}
				}
				
				keepCardNo = hBillRealCardNo;
				hBillDestinationAmt = 0;
				hBillDestinationCnt = 0;
				firstCardNo = false;
			}
			
			if ("+".equals(getValue("ecs_sign_code"))) {
				hBillDestinationAmt += getValueDouble("dest_amt");
			} else {
				hBillDestinationAmt -= getValueDouble("dest_amt");
			}
			
			hBillDestinationCnt ++;

			hTshtDestinationAmt = hTshtDestinationAmt + hBillDestinationAmt;

			totalCnt++;
		}
		
		if (hBillDestinationAmt > 0) {
			insertTscStmtLog(keepCardNo);
		}

		closeCursor();

	}

    /***********************************************************************/
    void selectBilBillOld() throws Exception {
        long tempLAmt, templ2Amt, tempDAmt;
        double dTempTotAmt, dTempDAmt;

        sqlCmd = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "sum(decode(a.acct_code,'IT'," 
                + "       decode(b.refund_apr_flag,'Y',"
                + "          decode(sign(substr(decode(b.refund_apr_date,'', null, b.refund_apr_date),1,6) - ?),"
                + "               0,(b.install_curr_term*b.unit_price+b.first_remd_amt)*-1,0),a.dest_amt),"
                + "       case when a.txn_code in ('06','25','27','28','29') "
                + "      then a.dest_amt*-1 else a.dest_amt end)) h_bill_destination_amt ";
        sqlCmd += "from bil_bill a, ";
        sqlCmd += "(select distinct(card_no) card_no from tsc_card ";
        sqlCmd += "  where substr(decode(addvalue_date, '', null , addvalue_date),1,6) < ? ";
        sqlCmd += "    and autoload_flag  = 'Y' ";
        sqlCmd += "    and ((current_code = '0' and substr(new_end_date,1,6) > ? ) or ";
        sqlCmd +=         " (current_code!= '0' and ";
        sqlCmd +=         "  substr(decode(oppost_date  ,'','19110101', oppost_date),1,6) > ?)) ";
        sqlCmd += "and ((substr(decode(lock_date   ,'','30001231', lock_date)  ,1,6) > ?) ";
        sqlCmd += " and (substr(decode(balance_date,'','30001231',balance_date),1,6) > ?) ";
        sqlCmd += " and (substr(decode(return_date ,'','30001231',return_date) ,1,6) > ?)) ";
        sqlCmd += ") c ";
        if(debugT){
            //sqlCmd += ", tsc_card d ";
        }
        
        sqlCmd += "left join bil_contract b ";
        sqlCmd += "  on b.contract_no = decode(a.contract_no,'','x', a.contract_no) ";
        sqlCmd += "  and b.contract_seq_no = a.contract_seq_no " ;
        sqlCmd += "where a.card_no    = c.card_no ";
        if(debugT){
            //sqlCmd += " and tsc_card_no = '8201750006204416' and a.card_no =d.CARD_NO ";
            sqlCmd += " and c.card_no = '3565538600678759' ";
        }
        sqlCmd += "  and a.acct_month = ? ";
        sqlCmd += "  and decode(a.rsk_type,'','4',a.rsk_type) = '4' ";
        /** 消費資料 六大本金類 **/
        sqlCmd += "and a.acct_code in (decode(cast(? as varchar(10)),'Y','BL','XX'), ";
        sqlCmd += "decode(cast(? as varchar(10)),'Y','IT','XX'), ";
        sqlCmd += "decode(cast(? as varchar(10)),'Y','ID','XX'), ";
        sqlCmd += "decode(cast(? as varchar(10)),'Y','CA','XX'), ";
        sqlCmd += "decode(cast(? as varchar(10)),'Y','AO','XX'), ";
        sqlCmd += "decode(cast(? as varchar(10)),'Y','OT','XX')) ";
        /** 消費資料 排除特店 **/
        sqlCmd += "and (? = 'N'   ";
        sqlCmd += " or (? = 'Y' and not exists (select tsc_bn_data.data_code ";
        sqlCmd += " from tsc_bn_data ";
        sqlCmd += "where table_name            = 'TSC_STMT_PARM' ";
        sqlCmd += "  and data_type             = '1' ";
        sqlCmd += "  and tsc_bn_data.apr_flag  = 'Y' ";
        sqlCmd += "  and tsc_bn_data.data_code = decode(a.mcht_no,'','xx', a.mcht_no)))) ";
        /** 消費資料 排除MCC **/
        sqlCmd += "  and (? = 'N'   ";
        sqlCmd += " or (? = 'Y' and not exists (select tsc_bn_data.data_code ";
        sqlCmd +=                              "  from tsc_bn_data ";
        sqlCmd +=                              " where table_name = 'TSC_STMT_PARM' ";
        sqlCmd +=                              "   and data_type = '2' ";
        sqlCmd +=                              "   and tsc_bn_data.apr_flag = 'Y' ";
        sqlCmd +=                              "   and tsc_bn_data.data_code = decode(a.mcht_category,'','xx', a.mcht_category)))) ";
        /** 排除特店群組  **/ //add by brian 20180810
        sqlCmd += "and (? = 'N'   ";
        sqlCmd += " or (? = 'Y' and not exists (select t.data_code ";
        sqlCmd += "from tsc_bn_data t, mkt_mchtgp_data g ";
        sqlCmd += "where t.table_name = 'MKT_MCHTCOSU_PARM' ";
        sqlCmd += "  and t.data_type  = '3' ";
        sqlCmd += "  and t.apr_flag   = 'Y' ";
        sqlCmd += "  and g.table_name = 'MKT_MCHT_GP' ";
        sqlCmd += "  and g.data_key   = t.data_code ";
        sqlCmd += "  and g.data_type  = '1' ";
        sqlCmd += "  and g.data_code  = decode(a.mcht_no,'','xx', a.mcht_no) ";
        sqlCmd += "  and decode(g.data_code2, '', lpad(a.acq_member_id,8,'00000000'), g.data_code2) = lpad(a.acq_member_id,8,'00000000'))"
                + ")) ";
/* lai test mark
*/
        /******************************/
        sqlCmd += "group by a.card_no ";

if(debug==1) 
   showLogMessage("I", "", "[PARM] h_bill_acct_month = " + hBillAcctMonth 
                     + ", bl=" + hTspmItemEnameBl + ", it=" + hTspmItemEnameIt 
                     + ", id=" + hTspmItemEnameId + ", ca=" + hTspmItemEnameCa 
                     + ", ao=" + hTspmItemEnameAo + ", ot=" + hTspmItemEnameOt);
        setString(1, hBillAcctMonth);
        setString(2, hBillAcctMonth);
        setString(3, hBillAcctMonth);
        setString(4, hBillAcctMonth);
        setString(5, hBillAcctMonth);
        setString(6, hBillAcctMonth);
        setString(7, hBillAcctMonth);
        setString(8, hBillAcctMonth);
        setString(9, hTspmItemEnameBl);
        setString(10, hTspmItemEnameIt);
        setString(11, hTspmItemEnameId);
        setString(12, hTspmItemEnameCa);
        setString(13, hTspmItemEnameAo);
        setString(14, hTspmItemEnameOt);
        setString(15, hTspmExclMchtFlag);
        setString(16, hTspmExclMchtFlag);
        setString(17, hTspmExclMccFlag);
        setString(18, hTspmExclMccFlag);
        setString(19, hTspmExclMchtGroupFlag);
        setString(20, hTspmExclMchtGroupFlag);
/* lai test mark
*/
        openCursor();
        while(fetchTable()) {
            hBillRealCardNo = getValue("card_no");
            hBillDestinationAmt = getValueDouble("h_bill_destination_amt");
if(debug==1) 
   showLogMessage("I", "", "  CARD_NO="+hBillRealCardNo+","+hBillDestinationAmt+",TOT="
                                                               +hTshtDestinationAmt);

            if (hBillDestinationAmt == 0)
                continue;
            if (hBillDestinationAmt < 0) {
                hTrlgTranCode = "A";
                hTrlgTranAmt = hBillDestinationAmt;
                insertTscRefundLog(hBillRealCardNo);
                continue;
            }
            selectTscStmtRefund();
            if (hTsrdRefundAmt != 0) {
                dTempTotAmt = 0;
                if (hTsrdRefundAmt + hBillDestinationAmt <= 0) {
                    hTrlgTranAmt = hBillDestinationAmt;
                    hBillDestinationAmt = 0;
                } else if (hTsrdRefundAmt + hBillDestinationAmt > 0) {
                    hTrlgTranAmt = hTsrdRefundAmt * -1;
                    hBillDestinationAmt = hBillDestinationAmt + hTsrdRefundAmt;
                }

                hTrlgTranCode = "D";
                insertTscRefundLog(hBillRealCardNo);
                if (hBillDestinationAmt <= 0)
                    continue;
            }
            /*** 設定上限 ***/
            /* Hesyuan 修改 */
            tempDAmt = (long) (hBillDestinationAmt > 12000 ? 12000 : hBillDestinationAmt);

            dTempDAmt = dTempTotAmt = 0;
            for (int int1a = 4; int1a >= 0; int1a--)
                if ((nTempRepayAmt[int1a] > 0) && (tempDAmt >= nTempRepayAmt[int1a])) {
                    tempLAmt   = nTempRepayAmt[int1a];
                    tempDAmt   = tempDAmt - (nTempRepayAmt[int1a] - 1);
                    if (tempDAmt < 0)
                        tempDAmt = 0;
                    templ2Amt  = (long) ((tempDAmt * nTempRepayRate[int1a] + 5) / 10.0);
                    dTempDAmt = templ2Amt / 10.0;

                    dTempTotAmt = dTempTotAmt + dTempDAmt;
                    tempDAmt     = tempLAmt;
                }

            totalCnt++;
            hStmtFeedbackAmt    = dTempTotAmt;
            hTshtDestinationAmt = hTshtDestinationAmt + hBillDestinationAmt;
            hTshtFeedbackAmt    = hTshtFeedbackAmt + hStmtFeedbackAmt;
            insertTscStmtLog(hBillRealCardNo);
        }
        closeCursor();
    }
    /***********************************************************************/
    void selectTscStmtRefund() throws Exception {
        hTsrdRefundAmt = 0;

        sqlCmd  = "select refund_amt ";
        sqlCmd += " from tsc_stmt_refund  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "  and card_no    = ? ";
        setString(1, hBillAcctMonth);
        setString(2, hBillRealCardNo);
        if (selectTable() > 0) {
            hTsrdRefundAmt = getValueDouble("refund_amt");
        }
    }
    /***********************************************************************/
    void insertTscRefundLog(String keepCardNo) throws Exception {
        sqlCmd = "insert into tsc_refund_log ";
        sqlCmd += "(acct_month,";
        sqlCmd += "card_no,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "tran_code,";
        sqlCmd += "tran_amt,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "sysdate ";
        sqlCmd += "from tsc_card a " 
          + "where card_no  = ? "
          + "  and new_end_date = (select max(new_end_date) from tsc_card b where b.card_no = ?) "
          + "fetch first 1 rows only ";
        setString(1, hBillAcctMonth);
        setString(2, hBillRealCardNo);
        setString(3, hTrlgTranCode);
        setDouble(4, hTrlgTranAmt);
        setString(5, javaProgram);
        setString(6, keepCardNo);
        setString(7, keepCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            showLogMessage("E","","insert" + daoTable + " "+ keepCardNo + "duplicate!");
        }

    }

    /***********************************************************************/
    void insertTscStmtLog(String keepCardNo) throws Exception {
        sqlCmd  = "insert into tsc_stmt_log ";
        sqlCmd += "(crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "acct_month,";
        sqlCmd += "tran_code,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "feedback_amt,";
        sqlCmd += "proc_flag,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'I',";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'N',";
        sqlCmd += "?,";
        
        //sqlCmd += "?,";//test only
        
        sqlCmd += "sysdate ";
        sqlCmd += " from tsc_card a ";
        sqlCmd += " where card_no  = ? ";
        sqlCmd += "   and new_end_date = (select max(new_end_date) from tsc_card b where b.card_no = ?) ";
        sqlCmd += " fetch first 1 rows only ";
        
        setString(1, hStmtCrtDate);
        setString(2, hStmtCrtTime);
        setString(3, hBillAcctMonth);
        setDouble(4, hBillDestinationAmt);
        setDouble(5, hStmtFeedbackAmt);
        setString(6, javaProgram);
        setString(7, keepCardNo);
        setString(8, keepCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
        	showLogMessage("E","","insert" + daoTable + " "+ keepCardNo + "duplicate!");
        }

    }

    /***********************************************************************/
    void insertTscStmtHst() throws Exception {
        setValue("acct_month"        , hBillAcctMonth);
        setValueDouble("dest_amt"    , hTshtDestinationAmt);
        setValueDouble("feedback_amt", hTshtFeedbackAmt);
        setValue("mod_pgm"           , javaProgram);
        setValue("mod_time"          , sysDate + sysTime);
        daoTable = "tsc_stmt_hst";
        insertTable();
        if (dupRecord.equals("Y")) {
        	showLogMessage("E","","insert_tsc_stmt_hst duplicate!");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB001 proc = new TscB001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
