/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
* ---------- ---------- ----------- ----------------------------------------  *
* 2023/08/16 V1.00.01   Lai         initial(copy from MktR490)                *
*                                                                             *
******************************************************************************/
package Mkt;

import com.AccessDAO;
import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.Date;

/*員工信用卡招攬獎勵紀錄處理程式*/
public class MktR490 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private String progname = "企業員工共銷招攬卡數統計   112/08/14 V1.01.01";
    CommFunction    comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int DEBUG   = 0;
    int DEBUG_F = 0;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hAcctMonth = "";
    String hMifdProgramCode = "";
//    String hMifdExclObjFlag = "";
//    String hMifdExclPurFlag = "";
    String hMifdItemEnameBl = "";
    String hMifdItemEnameCa = "";
    String hMifdItemEnameIt = "";
    String hMifdItemEnameId = "";
    String hMifdItemEnameOt = "";
    String hMifdItemEnameAo = "";
    String hMifdApplyDateS = "";
    String hMifdApplyDateE = "";
    String hMifdDebutYearFlag = "";
    Integer hMifdDebutMonth1 = 0;
    String hMifdDebutSupFlag0 = "";
    String hMifdDebutSupFlag1 = "";
    String hMifdRewardBank = "";
    String hMifdExcludeBank = "";
    String hMifdExcludeFinance = "";
    String hMifdAcctTypeFlag = "";
    String hMifdGroupCodeFlag = "";
    String hMifdCardTypeFlag = "";
    String hMifdConsumeType = "";
    String hMifdConsumeFlag = "";
    Integer hMifdCurrMonth = 0;
    Integer hMifdNextMonth = 0;
    Double hMifdCurrAmt = 0.0;
    String hMifdCurrTotCond = "";
    Integer hMifdCurrTotCnt = 0;
    String hMifdDataType = "";
    String hMifdDataCode1 = "";
    String hCardCardNo  = "";
    String hCardMCardNo = "";
    String hCardOCardNo = "";
    String hCardIssueDate = "";
    String hEmplId = "";
    String hEmplEmployNo = "";
    // -- 條件核對所需參數
    String hCardSupFlag = "";
    String hCardIntroduceEmpNo = "";
    String hCrdEmpEmployNo = "";
    String hCrdEmpStatusId = "";
    String hCrdIdIdPSeqno  = "";
    String hCrdIdIdNo = "";
    String hCardMIdPSeqno = "";
    String hCardAcctType = "";
    String hCardGroupCode = "";
    String hCardCardType = "";
    String hCardActivateFlag = "";
    String hCardOppostDate   = "";
    String hCardCurrentCode  = "";
    String hCardRegBankNo    = "";
    String hCardMemberId     = "";
    String hCardPromoteDept  = "";
    String hCardPromoteEmpNo = "";
    String hCardIntroduceId  = "";
    String hCardChiName      = "";
    String hCardFstConsumeDate = "";
    String hLastPurchaseDate   = "";
    double hBillAmtBl   = 0;
    double hBillAmtCa   = 0;
    double hBillAmtIt   = 0;
    double hBillAmtId   = 0;
    double hBillAmtAo   = 0;
    double hBillAmtOt   = 0;
    double yearTotalAmt = 0;

    int totCnt = 0;
    int insCnt = 0;
    String[] tempWday = { "03", "06", "09", "12", "15", "18", "21", "24", "27" };
    String tmpstr = "";

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
                comc.errExit("Usage : MktR490 [[businessdate][acct_month]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // comcr.h_call_batch_seqno = h_call_batch_seqno;
            // comcr.h_call_r_program_code = javaProgram;

            // comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            selectPtrBusinday();
            if(args.length >= 1) {
               if(args[0].length() == 8) {
                  hBusiBusinessDate  = args[0];
                  hAcctMonth = String.format("%6.6s", hBusiBusinessDate);
                 } 
            } 

            hWdayStmtCycle     = String.format("%2.2s", hBusiBusinessDate.substring(6));

            showLogMessage("I", "", String.format("處理月份=[%s] 處理日期=[%s]"
                              , hAcctMonth, hBusiBusinessDate));

            deleteMktIssueReward();// 刪除前24個月的數據

            selectMktIntrFund();

            showLogMessage("I","","處理筆數="+totCnt+" Insert="+ insCnt);

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
void selectPtrBusinday() throws Exception 
{
   sqlCmd  = "select decode(cast(? as varchar(8)),'',business_date,?) h_busi_business_date ";
   sqlCmd += "  from ptr_businday ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
      }
   hBusiBusinessDate = getValue("h_busi_business_date");

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm') as h_prev_month ";
   sqlCmd += "  from ptr_businday ";
   setString(1, hBusiBusinessDate);
   recordCnt = selectTable();
   
   hAcctMonth = getValue("h_prev_month");
}
/****************************************************************************/
void deleteMktIssueReward() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.YEAR, -2);
        java.util.Date y = c.getTime();
        String yearmonth = format.format(y);

        showLogMessage("I", "", "======= DELETE mkt_issue_reward =[" + yearmonth.substring(0,4) + "]");
        daoTable  = "mkt_issue_reward   ";
        whereStr  = "where static_month <= ? ";
        setString(1, yearmonth.substring(0,4) );
        int recCnt = deleteTable();

        showLogMessage("I", "", "======= DELETE mkt_issue_reward curr =[" + hAcctMonth + "]");
        daoTable  = "mkt_issue_reward   ";
        whereStr  = "where static_month like ? || '%' ";
        setString(1, hAcctMonth);
        recCnt = deleteTable();
}
/***********************************************************************/
int selectMktIntrFund() throws Exception {

        sqlCmd  = "select ";
        sqlCmd += " a.program_code,";
        sqlCmd += " debut_year_flag,";
        sqlCmd += " debut_month1,";
        sqlCmd += " debut_sup_flag_0,";
        sqlCmd += " debut_sup_flag_1,";
        sqlCmd += " reward_bank,";
        sqlCmd += " exclude_bank,";
        sqlCmd += " exclude_finance,";
        sqlCmd += " acct_type_flag,";
        sqlCmd += " group_code_flag,";
        sqlCmd += " card_type_flag,";
        sqlCmd += " consume_type,";
        sqlCmd += " consume_flag,";
        sqlCmd += " curr_month,";
        sqlCmd += " next_month,";
        sqlCmd += " curr_amt,";
        sqlCmd += " curr_tot_cond,";
        sqlCmd += " curr_tot_cnt,";
//      sqlCmd += " excl_obj_flag,";
//      sqlCmd += " excl_pur_flag,";
        sqlCmd += " item_ename_bl,";
        sqlCmd += " item_ename_ca,";
        sqlCmd += " item_ename_it,";
        sqlCmd += " item_ename_id,";
        sqlCmd += " item_ename_ot,";
        sqlCmd += " item_ename_ao,";
        sqlCmd += " a.apply_date_s,";
        sqlCmd += " decode(a.apply_date_e, '', '30001231', a.apply_date_e) apply_date_e ";
        sqlCmd += "  from mkt_intr_fund a ";
//      sqlCmd += "  left join mkt_intr_dtl b on a.program_code = b.program_code ";
        sqlCmd += " where a.apr_flag       = 'Y' ";
        sqlCmd += "   and a.reward_finance = 'Y' ";
        sqlCmd += "   and decode(a.apply_date_s, '', '20100101', a.apply_date_s) <= ? ";
        sqlCmd += "   and decode(a.apply_date_e, '', '30001231', a.apply_date_e) >= ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        int tableCol = 0;
        int SelectRow = selectTable();
        showLogMessage("I","","Main select mkt_intr_fund cnt=["+SelectRow+"] Date="+hBusiBusinessDate);
        if (SelectRow <= 0) {
            showLogMessage("I", "", " Warning select mkt_intr_fund not found=" + hBusiBusinessDate);
            return 0;
           }

        for (int ii = 0; ii < SelectRow; ii++) {
            hMifdProgramCode   = getValue("program_code", ii);
            hMifdDebutYearFlag = getValue("debut_year_flag", ii);
            hMifdDebutMonth1   = getValueInt("debut_month1", ii);
            hMifdDebutSupFlag0 = getValue("debut_sup_flag_0", ii);
            hMifdDebutSupFlag1 = getValue("debut_sup_flag_1", ii);
            hMifdRewardBank = getValue("reward_bank", ii);
            hMifdExcludeBank = getValue("exclude_bank", ii);
            hMifdExcludeFinance = getValue("exclude_finance", ii);
            hMifdAcctTypeFlag = getValue("acct_type_flag", ii);
            hMifdGroupCodeFlag = getValue("group_code_flag", ii);
            hMifdCardTypeFlag = getValue("card_type_flag", ii);
            hMifdConsumeType = getValue("consume_type", ii);
            hMifdConsumeFlag = getValue("consume_flag", ii);
            hMifdCurrMonth = getValueInt("curr_month", ii);
            hMifdNextMonth = getValueInt("next_month", ii);
            hMifdCurrAmt   = getValueDouble("curr_amt", ii);
            hMifdCurrTotCond = getValue("curr_tot_cond", ii);
            hMifdCurrTotCnt  = getValueInt("curr_tot_cnt", ii);
//          hMifdExclObjFlag = getValue("excl_obj_flag", ii);
//          hMifdExclPurFlag = getValue("excl_pur_flag", ii);
            hMifdItemEnameBl = getValue("item_ename_bl", ii);
            hMifdItemEnameCa = getValue("item_ename_ca", ii);
            hMifdItemEnameIt = getValue("item_ename_it", ii);
            hMifdItemEnameId = getValue("item_ename_id", ii);
            hMifdItemEnameOt = getValue("item_ename_ot", ii);
            hMifdItemEnameAo = getValue("item_ename_ao", ii);
            hMifdApplyDateS = getValue("apply_date_s", ii);
            hMifdApplyDateE = getValue("apply_date_e", ii);

            showLogMessage("I", "", String.format("Fund_code=[%s] Processing....", hMifdProgramCode));

            totCnt = 0;
            tableCol++;
            // 正卡:N-不回饋   附卡:N-不回饋
            if ("N".equalsIgnoreCase(hMifdDebutSupFlag0) &&
                "N".equalsIgnoreCase(hMifdDebutSupFlag1)) {
                showLogMessage("I", "", " 正,附卡同時為 'N', 不處理 !!!");
                continue;
               }
            selectCrdCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totCnt));
        }
        
        if (tableCol > 0) {
            return 0;
        } else {
            return -1;
        }
}
/***********************************************************************/
void selectMktCardConsume() throws Exception {
   yearTotalAmt = 0;
   sqlCmd  = "select (sum(d.consume_bl_amt) + sum(d.consume_ca_amt) + sum(d.consume_it_amt) "
           + "     +  sum(d.consume_ao_amt) + sum(d.consume_id_amt) + sum(d.consume_ot_amt) "
           + "     +  sum(d.foreign_bl_amt) + sum(d.foreign_ca_amt) + sum(d.foreign_it_amt) "
           + "     -  sum(d.sub_bl_amt) - sum(d.sub_ca_amt) - sum(d.sub_it_amt) "
           + "     -  sum(d.sub_ao_amt) - sum(d.sub_id_amt) - sum(d.sub_ot_amt) "
           + "     -  sum(d.refund_it_amt))  as year_total_amt "
           + "  from mkt_card_consume d where card_no = ? and acct_month like ?||'%' ";
   setString(1, hCardCardNo);
   setString(2, hAcctMonth.substring(0,4));

   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
        comcr.errRtn("mkt_card_consume    not found!", hCardCardNo+","+hAcctMonth , hCallBatchSeqno);
      }
   yearTotalAmt = getValueDouble("year_total_amt");

}
/***********************************************************************/
void selectCrdCardData() throws Exception {
        sqlCmd  = "  select ";
        sqlCmd += "   a.card_no,  a.major_card_no,    a.ori_card_no, a.issue_date, ";
        sqlCmd += "   a.sup_flag, a.introduce_emp_no, b.status_id,   b.employ_no, b.id, ";
        sqlCmd += "   a.major_id_p_seqno, a.id_p_seqno,i.id_no, i.chi_name,";
        sqlCmd += "   a.activate_flag,a.oppost_date,a.current_code,a.reg_bank_no,a.member_id, ";
        sqlCmd += "   a.promote_dept,a.promote_emp_no,a.introduce_emp_no,a.introduce_id, ";
        sqlCmd += "   a.acct_type, a.group_code, a.card_type, a.frst_consume_date  ";
        sqlCmd += "  from crd_card a ";
        sqlCmd += "   left join crd_employee b on a.introduce_emp_no = b.employ_no ";
        sqlCmd += "   left join crd_idno     i on a.id_p_seqno = i.id_p_seqno ";
        sqlCmd += " where card_no = ? ";

   setString(1, hCardCardNo);

   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
        comcr.errRtn("crd_card  data      not found!", hCardCardNo, hCallBatchSeqno);
      }
   
            hCardMCardNo   = getValue("major_card_no");
            hCardOCardNo   = getValue("ori_card_no");
            hCardIssueDate = getValue("issue_date");
            hEmplId        = getValue("id");
            hEmplEmployNo  = getValue("employ_no");
            hCardSupFlag   = getValue("sup_flag");
            hCrdEmpEmployNo = getValue("employ_no");
            hCrdEmpStatusId = getValue("status_id");
            hCrdIdIdPSeqno  = getValue("id_p_seqno");
            hCrdIdIdNo      = getValue("id_no");
            hCardMIdPSeqno    = getValue("major_id_p_seqno");
            hCardAcctType     = getValue("acct_type");
            hCardGroupCode    = getValue("group_code");
            hCardCardType     = getValue("card_type");
            hCardActivateFlag = getValue("activate_flag");
            hCardOppostDate   = getValue("oppost_date");
            hCardCurrentCode  = getValue("current_code");
            hCardRegBankNo    = getValue("reg_bank_no");
            hCardMemberId     = getValue("member_id");
            hCardPromoteDept  = getValue("promote_dept");
            hCardPromoteEmpNo = getValue("promote_emp_no");
            hCardIntroduceEmpNo = getValue("introduce_emp_no");
            hCardIntroduceId    = getValue("introduce_id");
            hCardChiName        = getValue("chi_name");
if(DEBUG==1) 
   showLogMessage("I","","       crd_"+hCardIssueDate+","+getValue("issue_date")+","+hCardChiName);
}
/***********************************************************************/
void selectCrdCard() throws Exception {

        fetchExtend = "main.";
        sqlCmd  = " select ";
        sqlCmd += " a.card_no, ";
        sqlCmd += " sum(decode(c.acct_code,'BL', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_bl, ";
        sqlCmd += " sum(decode(c.acct_code,'CA', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_ca, ";
        sqlCmd += " sum(decode(c.acct_code,'IT', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_it, ";
        sqlCmd += " sum(decode(c.acct_code,'AO', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_ao, ";
        sqlCmd += " sum(decode(c.acct_code,'ID', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_id, ";
        sqlCmd += " sum(decode(c.acct_code,'OT', decode(sign_flag,'+',dest_amt,dest_amt*-1),0)) as amt_ot, ";
        sqlCmd += " max(c.purchase_date) last_purchase_date ";
        sqlCmd += "  from crd_card a ";
        sqlCmd += "   inner join bil_bill c on a.card_no = c.card_no ";
        // 核卡日期為上個月 && current_code = '0' && old_card_no = ''
        sqlCmd += "     and a.issue_date <= to_char(last_day(to_date(?, 'yyyymmdd')), 'yyyymmdd') ";
        sqlCmd += "     and a.issue_date >= to_char(first_day(to_date(?, 'yyyymmdd')), 'yyyymmdd') ";
        sqlCmd += "     and a.current_code = '0' ";
        sqlCmd += "     and a.old_card_no  = '' ";
        sqlCmd += "   left join crd_employee b on a.introduce_emp_no = b.employ_no ";
        sqlCmd += "   left join crd_idno     i on a.id_p_seqno = i.id_p_seqno ";
        sqlCmd += " where 1 = 1 ";
        // 消費金額是否滿足條件
        sqlCmd += "   and ((c.dest_amt >= ? ";
        // 或消費筆數設定檔有效時, 消費筆數是否滿足條件
        sqlCmd += "           or ( ? = 'Y' ";
        sqlCmd += "              and decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1, 1) >= ? )) ";
        // If CONSUME_TYPE = 1, 核對是否為核卡後N個月內刷卡消費
        sqlCmd += "       and (( ? = '1' ";
        sqlCmd += "             and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                              and substr(to_char(last_day(add_months(to_date(a.issue_date, 'yyyymmdd'), ?)), 'yyyymmdd'), 1, 6)) ";
        // If CONSUME_TYPE = 2, 核對是否為核卡年度至下一年度N個月內刷卡消費
        sqlCmd += "           or ( ? = '2' ";
        sqlCmd += "              and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                               and (substr(a.issue_date, 1, 4) + 1)||?))) ";
        sqlCmd += "   and a.card_no in (select card_no ";
        sqlCmd += "                         from crd_card ";
        sqlCmd += "                     where card_no = decode(?,'1',major_id_p_seqno,'2',id_p_seqno,'3',card_no, 'x')) ";
        sqlCmd += "   and decode(c.acct_code, '', 'x', c.acct_code) in (decode(?,'Y','BL','xx'), ";
        sqlCmd += "                                                decode(?,'Y','CA','xx'), ";
        sqlCmd += "                                                decode(?,'Y','IT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','ID','xx'), ";
        sqlCmd += "                                                decode(?,'Y','OT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','AO','xx')) ";
        sqlCmd += "   and decode( c.rsk_type, '', 'x',  c.rsk_type) not in ('1','2','3') ";
        sqlCmd += "   and c.acct_month = ? ";
        sqlCmd += " group by  ";
        sqlCmd += "   a.card_no ";

        int idx_f = 1;
        setString(idx_f++, hBusiBusinessDate);
        setString(idx_f++, hBusiBusinessDate);
        setDouble(idx_f++, hMifdCurrAmt);
        setString(idx_f++, hMifdCurrTotCond);
        setInt(   idx_f++, hMifdCurrTotCnt);
        setString(idx_f++, hMifdConsumeFlag);
        setInt(   idx_f++, hMifdCurrMonth);
        setString(idx_f++, hMifdConsumeFlag);
        setString(idx_f++, hMifdNextMonth.toString().length() == 2 ? hMifdNextMonth.toString() : "0" + hMifdNextMonth);
        setString(idx_f++, hMifdConsumeType);
        setString(idx_f++, hMifdItemEnameBl);
        setString(idx_f++, hMifdItemEnameCa);
        setString(idx_f++, hMifdItemEnameIt);
        setString(idx_f++, hMifdItemEnameId);
        setString(idx_f++, hMifdItemEnameOt);
        setString(idx_f++, hMifdItemEnameAo);
        setString(idx_f++, hAcctMonth);
        
        showLogMessage("I", ""," Open crd_card ......="+hAcctMonth);
        int cursorIndex = openCursor();
        showLogMessage("I", ""," Open crd_card End.....="+hAcctMonth);
        while (fetchTable(cursorIndex)) {
            hCardCardNo    = getValue("main.card_no");
            hCardFstConsumeDate = getValue("main.frst_consume_date");
            hLastPurchaseDate   = getValue("main.last_purchase_date");
            hBillAmtBl          = getValueDouble("main.amt_bl");
            hBillAmtCa          = getValueDouble("main.amt_ca");
            hBillAmtIt          = getValueDouble("main.amt_it");
            hBillAmtAo          = getValueDouble("main.amt_ao");
            hBillAmtId          = getValueDouble("main.amt_id");
            hBillAmtOt          = getValueDouble("main.amt_ot");
            selectCrdCardData();
            selectMktCardConsume();
        
            totCnt++;
            if ((totCnt % 10000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totCnt));
                commitDataBase();
            }
if(DEBUG==1) {
   showLogMessage("I","","  Read crd_card="+hCardCardNo+","+hCardIssueDate+","+hLastPurchaseDate+","+totCnt);
   showLogMessage("I","","           id  ="+hCrdIdIdPSeqno+","+hMifdDebutMonth1);
  }
        setString(1, hCrdIdIdPSeqno);
        setString(2, hCardIssueDate);
        setString(3, hMifdDebutMonth1.toString());

            // -- 根據MKT_INTR_FUND參數, 進行條件核對

if(DEBUG_F==1) showLogMessage("I","","    Step 1="+hMifdDebutYearFlag);
            // DEBUT_YEAR_FLAG  -- 是否為新卡
            if ("1".equalsIgnoreCase(hMifdDebutYearFlag)) {
                // 不滿足條件則跳過此筆數據
                if (commDebutYearFlag1() == 0) {
                    continue;
                }
            } else if ("2".equalsIgnoreCase(hMifdDebutYearFlag)) {
                if (commDebutYearFlag2() == 0) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 2="+hMifdDebutSupFlag0);
            // DEBUT_SUP_FLAG_0 = 'Y' -回饋
            if ("N".equalsIgnoreCase(hMifdDebutSupFlag0)) {
                // SUP_FLAG == 0, 不滿足條件, 跳過此筆
                if ("0".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 3="+hMifdDebutSupFlag1);
            // DEBUT_SUP_FLAG_1 = 'Y' -回饋
            if ("N".equalsIgnoreCase(hMifdDebutSupFlag1)) {
                // SUP_FLAG == 1, 不滿足條件, 跳過此筆
                if ("1".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 4="+hMifdRewardBank);
            // REWARD_BANK
            if ("Y".equalsIgnoreCase(hMifdRewardBank)) {
                if (commRewardBank() == 0) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 5="+hMifdExcludeBank);
            // EXCLUDE_BANK
            if ("Y".equalsIgnoreCase(hMifdExcludeBank)) {
                if (commExcludeBank() == 0) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 6="+hMifdExcludeFinance);
            // EXCLUDE_FINANCE
            if ("Y".equalsIgnoreCase(hMifdExcludeFinance)) {
                if (commExcludeFinance() == 0) {
                    continue;
                }
            }

if(DEBUG_F==1) showLogMessage("I","","    Step 7="+hMifdExcludeFinance);
            // ACCT_TYPE
            if (commAcctType() == 0) {
                continue;
            }
if(DEBUG_F==1) showLogMessage("I","","    Step 8="+hMifdExcludeFinance);

            // GROUP_CODE
            if (commGroupCode() == 0) {
                continue;
            }
if(DEBUG_F==1) showLogMessage("I","","    Step 9="+hMifdExcludeFinance);

            // CARD_TYPE
            if (commCardType() == 0) {
                continue;
            }

            insertMktIssueReward();
        }
        closeCursor(cursorIndex);
}
/*********************************************************************/
    // 判斷參數條件：DEBUT_YEAR_FLAG == 1, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commDebutYearFlag1() throws Exception {
        sqlCmd = " select count(1) as crd_cnt from crd_card cc " +
                " where 1 = 1 " +
                " and cc.id_p_seqno = ? " +
                " and cc.issue_date between to_char(add_months(to_date(?, 'yyyymmdd'), '-'||?), 'yyyymmdd') " +
                " and to_char(add_days(to_date(?, 'yyyymmdd'), '-1'), 'yyyymmdd') ";
        setString(1, hCrdIdIdPSeqno);
        setString(2, hCardIssueDate);
        setString(3, hMifdDebutMonth1.toString());
        setString(4, hCardIssueDate);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：DEBUT_YEAR_FLAG == 2, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commDebutYearFlag2() throws Exception {
        sqlCmd = " select count(1) as crd_cnt from crd_card cc " +
                " where cc.issue_date < ? " +
                " and cc.id_p_seqno = ? ";
        setString(1, hCardIssueDate);
        setString(2, hCrdIdIdPSeqno);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：REWARD_BANK == Y, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commRewardBank() {
        if (hCardIntroduceEmpNo.equalsIgnoreCase(hCrdEmpEmployNo)) {
            if ("1".equalsIgnoreCase(hCrdEmpStatusId)
                    || "7".equalsIgnoreCase(hCrdEmpStatusId)) {
                return 1;
            }
        }
        return 0;
    }

    // 判斷參數條件：EXCLUDE_BANK == Y, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commExcludeBank() throws Exception {
        if (hCrdIdIdPSeqno == null) {
            return 0;
        }
        sqlCmd = " select count(1) from crd_idno i " +
                " inner join crd_employee e on e.id = ? " +
                " where i.id_p_seqno = ? and i.staff_flag = 'Y' ";
        setString(1, hCrdIdIdNo);
        setString(2, hCrdIdIdPSeqno);
        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：EXCLUDE_FINANCE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commExcludeFinance() throws Exception {
        if (hCrdIdIdPSeqno == null) {
            return 0;
        }
        sqlCmd = " select count(1) from crd_employee_a e where e.id = ? ";
        setString(1, hCrdIdIdNo);
        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：ACCT_TYPE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commAcctType() throws Exception {
        // IF ACCT_TYPE = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdAcctTypeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '01'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF ACCT_TYPE = 1 && ACCT_TYPE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdAcctTypeFlag)) {
                if (dataCodes.contains(hCardAcctType)) {
                    return 1;
                }
            }
            // IF ACCT_TYPE = 2, ACCT_TYPE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdAcctTypeFlag)) {
                if (!dataCodes.contains(hCardAcctType)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    // 判斷參數條件：GROUP_CODE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commGroupCode() throws Exception {
        // IF GROUP_CODE_FLAG = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdGroupCodeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '02'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF GROUP_CODE_FLAG = 1 && GROUP_CODE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdGroupCodeFlag)) {
                if (dataCodes.contains(hCardGroupCode)) {
                    return 1;
                }
            }
            // IF GROUP_CODE_FLAG = 2, GROUP_CODE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdGroupCodeFlag)) {
                if (!dataCodes.contains(hCardGroupCode)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    // 判斷參數條件：CARD_TYPE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commCardType() throws Exception {
        // IF CARD_TYPE_FLAG = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdCardTypeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '03'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF CARD_TYPE_FLAG = 1 && CARD_TYPE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdCardTypeFlag)) {
                if (dataCodes.contains(hCardCardType)) {
                    return 1;
                }
            }
            // IF CARD_TYPE_FLAG = 2, CARD_TYPE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdCardTypeFlag)) {
                if (!dataCodes.contains(hCardCardType)) {
                    return 1;
                }
            }
        }

        return 0;
    }
/***********************************************************************/
    void insertMktIssueReward() throws Exception {

if(DEBUG==1) showLogMessage("I", "", "     INSERT ="+insCnt+","+hCardCardNo+","+hMifdProgramCode);
        insCnt++;
        setValue("prod_no"         , hMifdProgramCode);
        setValue("static_month"    , hAcctMonth);
        setValue("proc_type"       , "1");
        setValue("card_no"         , hCardCardNo);
        setValue("acct_type"       , hCardAcctType);
        setValue("group_code"      , hCardGroupCode);
        setValue("card_type"       , hCardCardType);
        setValue("major_card_no"   , hCardMCardNo);
        setValue("ori_card_no"     , hCardOCardNo);
        setValue("id_p_seqno"      , hCrdIdIdPSeqno);
        setValue("major_id_p_seqno", hCardMIdPSeqno);
        setValue("issue_date"      , hCardIssueDate);
        setValue("activate_flag"   , hCardActivateFlag);
        setValue("oppost_date"     , hCardOppostDate);
        setValue("current_code"    , hCardCurrentCode);
        setValue("reg_bank_no"     , hCardRegBankNo);
        setValue("member_id"       , hCardMemberId);
        setValue("promote_dept"    , hCardPromoteDept);
        setValue("promote_emp_no"  , hCardPromoteEmpNo);
        setValue("introduce_emp_no", hCardIntroduceEmpNo);
        setValue("introduce_id"    , hCardIntroduceId);
        setValue("chi_name"        , hCardChiName);
        setValue("first_purchase_date" , hCardFstConsumeDate);
        setValue("last_purchase_date"  , hLastPurchaseDate);

        setValueInt("circulate_cnt", 1);
        setValueInt("valid_cnt"    , 0);
        if((hBillAmtBl+hBillAmtCa+hBillAmtIt+hBillAmtId+hBillAmtOt+hBillAmtAo) > 0)
            setValueInt("valid_cnt"    , 1);
        setValueDouble("amt_bl", hBillAmtBl);
        setValueDouble("amt_ca", hBillAmtCa);
        setValueDouble("amt_it", hBillAmtIt);
        setValueDouble("amt_id", hBillAmtId);
        setValueDouble("amt_ot", hBillAmtOt);
        setValueDouble("amt_ao", hBillAmtAo);
        setValueDouble("year_total_amt", yearTotalAmt);

        setValue("crt_date", sysDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm" , javaProgram);
        setValue("mod_user", javaProgram);

        daoTable = "mkt_issue_reward";
        insertTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktR490 proc = new MktR490();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
