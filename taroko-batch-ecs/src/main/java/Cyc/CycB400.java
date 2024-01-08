/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/08/18  V1.01.01   Lai        Initial                                   *
* 109/12/19  V1.00.02   shiyuqi    updated for project coding standard       *
* 110/02/20  V1.00.03   JeffKung   fulfilled TCB requirement                 *
* 112/09/12  v1.00.04   JeffKung   performance tuning                        *
*****************************************************************************/
package Cyc;

import com.*;

public class CycB400 extends AccessDAO {
    private String progname = "產生續期(包含第二年之後)年費作業     112/09/12  V1.00.04";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    int insCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;
    String hParamMonth = "";
    String hParamMonthIssue = "";

    String cardCardNo = "";
    String cardCorpNo = "";
    String cardCorpPSeqno = "";
    String cardNewEndDate = "";
    String cardExpireChgFlag = "";
    String cardIssueDate = "";
    String cardOriIssueDate = "";
    String cardCurrFeeCode = "";
    String cardCardType = "";
    String cardGroupCode = "";
    String cardSupFlag = "";
    String cardFeeDate = "";

    String hCorpCardSince = "";
    String hCorpCorpPSeqno = "";
    String hPaccCardIndicator = "";
    String hReasonCode = "";
    String hDataType = "";

    String hCardType = "";
    String hGroupCode = "";
    double hStandardFee = 0;
    int hFirstFee = 0;
    int hOtherFee = 0;
    double hSupRate = 0;
    int hSupEndMonth = 0;
    double hSupEndRate = 0;
    int pOtherFeeAmt = 0;
    double hAnnualFee = 0;
    
    String hNofeRowid = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CycB400 proc = new CycB400();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

            //set DEBUG mode
            for (int argi=0; argi < args.length ; argi++ ) {
          	  if (args[argi].equalsIgnoreCase("debug")) {
          		  debug=1;
          	  }
            }
            
            if (args.length > 2) {
                String err1 = "CycB400 [busindate] [seq_no]\n";
                String err2 = "CycB400 [busindate] [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }
            
            if (args.length > 0) {
                if (args[0].length() < 8) {
                    showLogMessage("I", "", "日期錯誤＝[" + args[0] + "]");
                    comcr.errRtn("日期錯誤 !!", "", comcr.hCallBatchSeqno);
                }
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();

            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            dateTime();
            selectPtrBusinday();
            
            if (args.length > 0 && args[0].length()==8) {
            	hBusiBusinessDate = args[0];
            }
            
            long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
            hBusiChiDate = Long.toString(hLongChiDate);
            showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");

            if (!"01".equals(hBusiBusinessDate.substring(6, 8))) {
                showLogMessage("I", "", "不為每月1日,不需跑此程式 !!");
                finalProcess();
                return 0;
            }
            
            hParamMonthIssue = comm.nextMonth(hBusiBusinessDate, -11);
            hParamMonth = comm.nextMonth(hBusiBusinessDate, +1);
            showLogMessage("I", "", "處理原始開戶年月["+ hParamMonthIssue+ "]之前發卡的卡片資料");
            showLogMessage("I", "", "處理年費年月=" + hParamMonth );   //compare to card_fee_date(yyyymm)

            totalCnt = 0;

            selectCrdCard();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "][" + insCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess
      // ************************************************************************

    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");

    }

    // ************************************************************************
    public void selectCrdCard() throws Exception {
        int flag = 0;
        int nofee = 0;
        int chk = 0;

        selectSQL = "a.card_no         , a.corp_no        , a.new_end_date    , "
                  + "a.expire_chg_flag , a.issue_date     , a.curr_fee_code   , "
                  + "a.card_type       , a.group_code     , a.sup_flag        , "
                  + "a.p_seqno         , a.id_p_seqno     , a.acct_type       , "
                  + "a.apply_no        , a.source_code    , a.stmt_cycle      , "
                  + "a.reg_bank_no     , a.introduce_id   , a.introduce_emp_no, " 
                  + "a.ori_issue_date  , "
                  + "b.card_indicator  , a.card_fee_date  , a.corp_p_seqno ";
        daoTable  = "ptr_acct_type b, crd_card a";
        whereStr  = "where a.current_code  = '0'        and b.acct_type     = a.acct_type  ";
        whereStr += "  and ( a.card_fee_date = ? ";
        whereStr += "   or ( a.ori_issue_date <= ? and substr(a.ori_issue_date,5,2) = ? ) ) ";

        setString(1, hParamMonth);
        setString(2, hParamMonthIssue+"31");
        setString(3, comc.getSubString(hParamMonth, 4));

        openCursor();

        while (fetchTable()) {
            initRtn();

            hPaccCardIndicator = getValue("card_indicator");

            cardCardNo = getValue("card_no");
            cardCorpNo = getValue("corp_no");
            cardCorpPSeqno = getValue("corp_p_seqno");
            cardNewEndDate = getValue("new_end_date");
            cardExpireChgFlag = getValue("expire_chg_flag");
            cardIssueDate = getValue("issue_date");
            cardOriIssueDate = getValue("ori_issue_date");
            cardCurrFeeCode = getValue("curr_fee_code");
            cardCardType = getValue("card_type");
            cardGroupCode = getValue("group_code");
            cardSupFlag = getValue("sup_flag");
            cardFeeDate = getValue("card_fee_date");

            hCardType = cardCardType;
            hGroupCode = cardGroupCode;

            totalCnt++;
            if (debug == 1) {
                showLogMessage("I", "", "  888 Card=[" + cardCardNo + "]");
                showLogMessage("I", "", "  888  src=[" + cardFeeDate + "]");
            }

            /* 檢核年費 */
            flag = processAnnual();
            if (flag != 0)
                continue;

            /*******************************************
             * 應收年費資資料,若線上登錄免年費時, rcv_annual_fee = 0
             *******************************************/
            hReasonCode = "";
            
            if(hAnnualFee > 0)
              { 
            	if(checkCycNofee() == 0) 
            	{
                   hReasonCode = "Z1";
                   hAnnualFee = 0; 
                   nofee=1; 
                   updateCycNofee();
                 }
              }

            
            chk = insertCycAfee();
            if (chk == 0) {
                updateCrdCard();
            }
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
            
        }

    }

    // ************************************************************************
    public int chkBusAnnualFee(String chkCd) throws Exception {

        hStandardFee = pOtherFeeAmt;
        switch (chkCd) {
        case "":
        case "0": /* 當有source code時,抓取參數年費設定之標準年費 */
            hAnnualFee = hStandardFee;
            break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
        case "7":
        case "8":
        case "9":
        case "Z":
            hAnnualFee = 0;
            break;
        default:
            hAnnualFee = pOtherFeeAmt;
            break;
        }

        return (0);
    }

    // ************************************************************************
    public int processAnnual() throws Exception {
        int flag = 0;

        flag = getGroupCard();
        if (flag == 0) {
            flag = chkOrgAnnualFee(cardCurrFeeCode);
        }
        
        /* 一般卡及商務卡都是看GroupCard
        if (hPaccCardIndicator.equals("1")) {
            flag = getGroupCard();
            if (flag == 0) {
                flag = chkOrgAnnualFee(cardCurrFeeCode);
            }
        }
        if (hPaccCardIndicator.equals("2")) {
            flag = getCorpFee();
            if (flag == 0) {
                flag = chkBusAnnualFee(cardCurrFeeCode);
            }
        }
        */

        return (flag);
    }

    // ******* 抓取年費參數檔 ******************************************************
    public int getGroupCard() throws Exception {

        selectSQL = "first_fee     ,other_fee        , " 
                  + "sup_rate      ,sup_end_month    , sup_end_rate ";
        daoTable  = "ptr_group_card ";
        whereStr  = "where card_type    = ? " + "  and group_code   = ? ";

        setString(1, hCardType);
        setString(2, hGroupCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
        	showLogMessage("E", "", " 標準年費參數未建檔(mktm6110)=[" + hCardType +","+ hGroupCode + "]");
            String err1 = "select_ptr_group_card error[notFound]";
            String err2 = cardCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hFirstFee = getValueInt("first_fee");
        hOtherFee = getValueInt("other_fee");
        hSupRate = getValueDouble("sup_rate");
        hSupEndMonth = getValueInt("sup_end_month");
        hSupEndRate = getValueDouble("sup_end_rate");

        return (0);
    }

    // ************************************************************************
    public int insertCycAfee() throws Exception {

    	/* 用unique key來擋, 不用先select (20230912)
    	
        selectSQL = "count(*) as afee_cnt ";
        daoTable  = "cyc_afee      ";
        whereStr  = "where card_no   = ? and card_fee_date = ? ";

        setString(1, cardCardNo);
        setString(2, hParamMonth);
        tmpInt = selectTable();

        if (debug == 1) showLogMessage("I", "", " insert afee=[" + getValueInt("afee_cnt") + "]");

        if (getValueInt("afee_cnt") > 0)
            return (1);
            
        */

        insCnt++;
        hDataType = "2";

        setValue("card_no"             , cardCardNo);
        setValue("fee_date"            , hBusiBusinessDate);
        setValue("card_type"           , cardCardType);
        setValue("p_seqno"             , getValue("p_seqno"));
        setValue("acct_type"           , getValue("acct_type"));
        setValue("id_p_seqno"          , getValue("id_p_seqno"));
        setValue("corp_p_seqno"        , cardCorpPSeqno);
        setValue("corp_no"             , cardCorpNo);
        setValue("sup_flag"            , getValue("sup_flag"));
        setValue("expire_date"         , cardNewEndDate);
        setValue("issue_date"          , getValue("issue_date"));
        setValue("apply_no"            , getValue("apply_no"));
        setValue("data_type"           , hDataType);
        setValue("indicator"           , "");
        setValue("group_code"          , cardGroupCode);
        setValue("source_code"         , getValue("source_code"));
        setValue("fee_type"            , "");
        setValue("stmt_cycle"          , getValue("stmt_cycle"));
        setValue("reason_code"         , hReasonCode);
        setValue("reg_bank_no"         , getValue("reg_bank_no"));
        setValue("introduce_emp_no"    , getValue("introduce_emp_no"));
        setValue("introduce_id"        , getValue("introduce_id"));
        setValueDouble("org_annual_fee", (double) hStandardFee);
        setValueDouble("rcv_annual_fee", (double) hAnnualFee);
        setValue("maintain_code"       , "");
        setValue("card_fee_date"      , hParamMonth);
        String purchReviewMonthBeg = "";
        double diffMonth = 12.0;
        purchReviewMonthBeg = comm.nextMonth(hParamMonth, -10);
        
        if (cardOriIssueDate.length() == 8) {
        	diffMonth = comm.monthBetween(cardOriIssueDate, purchReviewMonthBeg);
        }
        else {
        	if (cardIssueDate.length() == 8) {
        		diffMonth = comm.monthBetween(cardIssueDate, purchReviewMonthBeg);		
        	}
        }
        
        /* 若是首次Review年費, 消費區間為發卡年月~  (多一個月) */
        if (diffMonth <= 1 ) 
        {
        	purchReviewMonthBeg = comm.nextMonth(hParamMonth, -11);
        }
        setValue("purch_review_month_beg"      , purchReviewMonthBeg);
        setValue("purch_review_month_end"      , comm.nextMonth(hParamMonth, +1));
        setValue("crt_date"            , sysDate);
        setValue("apr_user"            , javaProgram);
        setValue("apr_date"            , sysDate);
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , javaProgram);

        daoTable = "cyc_afee    ";

        insertTable();

        if (dupRecord.equals("Y")) {
        	showLogMessage("I", "", " insert_cyc_afee error(dupRecord) , card_no=[" + cardCardNo + "]");
            //String err1 = "insert_cyc_afee          error[dupRecord]=" + cardCardNo;
            //String err2 = "";
            //comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int updateCrdCard() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd card  =[" + cardCurrFeeCode + "]");
        if (cardCurrFeeCode.length() < 1)
            return (1);

        if (comm.isNumber(cardCurrFeeCode)) {
            int tmpVal = Integer.parseInt(cardCurrFeeCode);
            
            if (tmpVal==0)  //無免年費數
            	return (1);

            tmpVal   = tmpVal - 1;
            tmpChar = String.valueOf(tmpVal) ;

            updateSQL = "curr_fee_code     =  ? , " + "mod_pgm           =  ? , "
                      + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_card ";
            whereStr  = "where card_no     = ? ";

            if (debug == 1) showLogMessage("I", "", " upd card 2=[" + tmpChar + "]");

            setString(1, tmpChar);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, cardCardNo);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_card error[notFound]=" + cardCardNo;
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }

        return (0);
    }

    // ************************************************************************
    public int chkOrgAnnualFee(String chkCd) throws Exception {
        int pAnnualFee = 0;

        /* 正卡 */
        if (cardSupFlag.equals("0"))
            pAnnualFee = hOtherFee;
        else
            pAnnualFee = (int) (hOtherFee * hSupRate) / 100;  //算完轉成整數 (不考慮小數點, 直接捨去)

        hStandardFee = pAnnualFee;
        switch (chkCd) {
        case "":
        case "0": /* 當有source code時,抓取參數年費設定之標準年費 */
            hAnnualFee = pAnnualFee;
            break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
        case "7":
        case "8":
        case "9":
        case "Z":
            hAnnualFee = 0;
            break;
        default:
            hAnnualFee = pAnnualFee;
            break;
        }

        //若是原本就免收年費的卡片就不寫入
        if (hStandardFee > 0) {
        	return (0);
        } else {
        	return (1);
        }
        
    }

    // ************************************************************************
    public int getCorpFee() throws Exception {
        selectSQL = "other_fee_amt    ";
        daoTable = "ptr_corp_fee   ";
        whereStr = "where card_type    = ? " + "  and corp_p_seqno = ? ";

        setString(1, hCardType);
        setString(2, hCorpCorpPSeqno);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            tmpInt = getGroupCard();
            if (tmpInt != 0) {
            	showLogMessage("E", "", " 標準年費參數未建檔(mktm6110)=[" + hCardType +","+ hGroupCode + "]");
                return (1);
            }
            
            pOtherFeeAmt = hOtherFee;
        } else {

        	pOtherFeeAmt = getValueInt("other_fee_amt"); 
        }

        return (0);
    }

    // ************************************************************************
    int checkCycNofee() throws Exception {

        hNofeRowid = "";
        
        /**** fee_date (應收年費年月+1個月+"01") = 執行日期businessDate****/

        sqlCmd = "  select rowid as rowid  ";
        sqlCmd += "  from cyc_nofee ";
        sqlCmd += "   where card_no    = ?  ";
        sqlCmd += "   and fee_date   = ? ";
        sqlCmd += "   and nofee_date = '' ";
        setString(1, cardCardNo);
        setString(2, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }

        if (debug == 1)
            showLogMessage("I", "", String.format("cyc_nofee card_no[%s]", cardCardNo));
        
        hNofeRowid = getValue("rowid");
        return (0);
    }

    /*****************************************************************************/
    void updateCycNofee() throws Exception {
        daoTable   = "cyc_nofee";
        updateSQL  = " nofee_date     = ?, ";
        updateSQL += " fin_nofee_code = 'Y', ";
        updateSQL += " org_annual_fee = ? , ";
        updateSQL += " rcv_annual_fee = ? , ";
        updateSQL += " reason_code = ? , ";
        updateSQL += " mod_time       = sysdate, ";
        updateSQL += " mod_pgm        = ?  ";
        whereStr   = " WHERE rowid    = ? ";
        setString(1, hBusiBusinessDate);
        setDouble(2, hStandardFee);
        setDouble(3, hAnnualFee);
        setString(4, hReasonCode);
        setString(5, javaProgram);
        setRowId(6, hNofeRowid);
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "", String.format("update cyc_nofee error , cyc_nofee card_no[%s]", cardCardNo));
            comcr.errRtn("update_cyc_nofee error", "", hCallBatchSeqno);
        }
        return;
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        cardCardNo = "";
        cardCorpNo = "";
        cardCorpPSeqno = "";
        cardNewEndDate = "";
        cardExpireChgFlag = "";
        cardIssueDate = "";
        cardOriIssueDate = "";
        cardCurrFeeCode = "";
        cardCardType = "";
        cardGroupCode = "";
        cardSupFlag = "";
        cardFeeDate = "";

        hCorpCardSince = "";
        hCorpCorpPSeqno = "";
        hPaccCardIndicator = "";
        hDataType = "";
        hReasonCode = "";

        hFirstFee = 0;
        hOtherFee = 0;
        hSupRate = 0;
        hSupEndMonth = 0;
        hSupEndRate = 0;
        pOtherFeeAmt = 0;
        hAnnualFee = 0;
    }
    // ************************************************************************

} // End of class FetchSample
