/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/09/15  V1.00.20  Allen Ho   New                                        *
 * 110/11/29  V1.01.02  Allen Ho   CR-1339                                    *
 * 111/01/07  V1.01.03  Allen Ho   CR-1339  chk feedback_lmt_cnt              *
 * 111/11/11  V1.01.04  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktC140 extends AccessDAO
{
    private final String PROGNAME = "通路活動-登錄類回饋處理程程式 111/01/07 V1.01.03";
    CommFunction comm = new CommFunction();
    CommRoutine comr = null;
    CommBonus comb = null;
    CommDBonus comd = null;

    String businessDate = "";

    double[][] purchaseAmtS =  new double [30][5];
    double[][] purchaseAmtE =  new double [30][5];
    String[][] activeType =  new String [30][5];
    double[][] feedbackRate =  new double [30][5];
    double[][] feedbackAmt =  new double [30][5];
    double[][] feedbackLmt =  new double [30][5];
    int[][] feedbackCnt =  new int [30][5];
    String[][] activeSeq = new String[20][20];
    String[][] recordGroupNo = new String[20][20];

    int  parmCnt  = 0;
    int[] acctTypeSel = new int[20];
    int[] amtCntSel = new int [20];

    String pSeqno = "";

    int  totalCnt=0,updateCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        MktC140 proc = new MktC140();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
        return;
    }
    // ************************************************************************
    public int mainProcess(String[] args) {
        try
        {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I","",javaProgram+" "+PROGNAME);

            if (comm.isAppActive(javaProgram))
            {
                showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
                return(0);
            }

            if (args.length > 2)
            {
                showLogMessage("I","","請輸入參數:");
                showLogMessage("I","","PARM 1 : [business_date]");
                showLogMessage("I","","PARM 2 : [p_seqno]");
                return(1);
            }

            if ( args.length >= 1 )
            { businessDate = args[0]; }

            if ( args.length >= 2 )
            { pSeqno = args[1]; }

            if ( !connectDataBase() )
                return(1);

            comr = new CommRoutine(getDBconnect(),getDBalias());
            comb = new CommBonus(getDBconnect(),getDBalias());
            comd = new CommDBonus(getDBconnect(),getDBalias());

            selectPtrBusinday();
            showLogMessage("I","","=========================================");
            showLogMessage("I","","載入依特定期間資料");
            loadMktBnData();
            showLogMessage("I","","=========================================");
            showLogMessage("I","","載入參數資料 ...");
            selectMktChannelParm();
            if (parmCnt==0)
            {
                showLogMessage("I","","今日["+ businessDate +"]無活動回饋");
                return(0);
            }

            if (pSeqno.length()==0) finalProcess();
            return(0);
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    void selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (businessDate.length()==0)
            businessDate =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
    }
    // ************************************************************************
    int selectMktChannelParm() throws Exception
    {
        extendField = "parm.";
        daoTable  = "mkt_channel_parm";
        whereStr  = "WHERE apr_flag        = 'Y' "
                + "AND   apr_date       != ''  "
                + "AND   (stop_flag     != 'Y'  "
                + " or    (stop_flag     = 'Y'  "
                + "  and  stop_date      > ? )) "
                + "and   record_cond     = 'Y' "
                + "and   cal_def_date    = ? "
                + "and   feedback_apr_date = '' "
        ;

        setString(1 , businessDate);
        setString(2 , businessDate);

        parmCnt = selectTable();

        if (parmCnt==0) return(0);

        showLogMessage("I","","=========================================");
        showLogMessage("I","","載入登錄資料 ....");
        loadMktChannelRecord();

        for (int inti=0;inti<parmCnt;inti++)
        {
            showLogMessage("I","","=========================================");
            showLogMessage("I","","["+ (inti+1) +" 活動代號:["+getValue("parm.active_code",inti)+"]["+getValue("parm.active_name",inti)+"]");
            showLogMessage("I","","  重複執行刪除資料");
            deleteMktChannelRank(inti);
            showLogMessage("I","","=========================================");
            showLogMessage("I","","載入登錄參數");
            selectMktChanrecParm(inti);
        }

        showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
        return(0);
    }
    // ************************************************************************
    int selectMktChanrecParm(int inti) throws Exception
    {
        extendField = "pseq.";
        daoTable  = "mkt_chanrec_parm";
        whereStr  = "WHERE active_code = ? "
                + "order by active_seq   "
        ;

        setString(1 , getValue("parm.active_code",inti));

        int recCnt = selectTable();

        for (int intk=0;intk<recCnt;intk++)
        {
            updateMktChannelBill0(inti,intk);
            showLogMessage("I","","  =========================================");
            showLogMessage("I","","  登錄群組代碼:["+getValue("pseq.record_group_no",intk)+"]");

            activeSeq[inti][intk]      = getValue("pseq.active_seq",intk);
            recordGroupNo[inti][intk] = getValue("pseq.record_group_no",intk);
/*
      feedback_limit[intk][0] = getValueDouble("parm.b_feedback_limit",intk);
      feedback_limit[intk][1] = getValueDouble("parm.f_feedback_limit",intk);
      feedback_limit[intk][2] = getValueDouble("parm.s_feedback_limit",intk);
*/
            purchaseAmtS[intk][0] = getValueDouble("pseq.purchase_amt_s1",intk);
            purchaseAmtS[intk][1] = getValueDouble("pseq.purchase_amt_s2",intk);
            purchaseAmtS[intk][2] = getValueDouble("pseq.purchase_amt_s3",intk);
            purchaseAmtS[intk][3] = getValueDouble("pseq.purchase_amt_s4",intk);
            purchaseAmtS[intk][4] = getValueDouble("pseq.purchase_amt_s5",intk);

            for (int intm=0;intm<5;intm++)
            {
                if (purchaseAmtS[intk][intm]==0) break;
                amtCntSel[intk] = intm+1;
            }

            purchaseAmtE[intk][0] = getValueDouble("pseq.purchase_amt_e1",intk);
            purchaseAmtE[intk][1] = getValueDouble("pseq.purchase_amt_e2",intk);
            purchaseAmtE[intk][2] = getValueDouble("pseq.purchase_amt_e3",intk);
            purchaseAmtE[intk][3] = getValueDouble("pseq.purchase_amt_e4",intk);
            purchaseAmtS[intk][4] = getValueDouble("pseq.purchase_amt_s1",intk);

            feedbackRate[intk][0]  = getValueDouble("pseq.feedback_rate_1",intk);
            feedbackRate[intk][1]  = getValueDouble("pseq.feedback_rate_2",intk);
            feedbackRate[intk][2]  = getValueDouble("pseq.feedback_rate_3",intk);
            feedbackRate[intk][3]  = getValueDouble("pseq.feedback_rate_4",intk);
            feedbackRate[intk][4]  = getValueDouble("pseq.feedback_rate_5",intk);

            activeType[intk][0]    = getValue("pseq.active_type_1",intk);
            activeType[intk][1]    = getValue("pseq.active_type_2",intk);
            activeType[intk][2]    = getValue("pseq.active_type_3",intk);
            activeType[intk][3]    = getValue("pseq.active_type_4",intk);
            activeType[intk][4]    = getValue("pseq.active_type_5",intk);

            feedbackAmt[intk][0]   = getValueDouble("pseq.feedback_amt_1",intk);
            feedbackAmt[intk][1]   = getValueDouble("pseq.feedback_amt_2",intk);
            feedbackAmt[intk][2]   = getValueDouble("pseq.feedback_amt_3",intk);
            feedbackAmt[intk][3]   = getValueDouble("pseq.feedback_amt_4",intk);
            feedbackAmt[intk][4]   = getValueDouble("pseq.feedback_amt_5",intk);

            feedbackLmt[intk][0]   = getValueDouble("pseq.feedback_lmt_amt_1",intk);
            feedbackLmt[intk][1]   = getValueDouble("pseq.feedback_lmt_amt_2",intk);
            feedbackLmt[intk][2]   = getValueDouble("pseq.feedback_lmt_amt_3",intk);
            feedbackLmt[intk][3]   = getValueDouble("pseq.feedback_lmt_amt_4",intk);
            feedbackLmt[intk][4]   = getValueDouble("pseq.feedback_lmt_amt_5",intk);

            feedbackCnt[intk][0]   = getValueInt("pseq.feedback_lmt_cnt_1",intk);
            feedbackCnt[intk][1]   = getValueInt("pseq.feedback_lmt_cnt_2",intk);
            feedbackCnt[intk][2]   = getValueInt("pseq.feedback_lmt_cnt_3",intk);
            feedbackCnt[intk][3]   = getValueInt("pseq.feedback_lmt_cnt_4",intk);
            feedbackCnt[intk][4]   = getValueInt("pseq.feedback_lmt_cnt_5",intk);

            showLogMessage("I","","  -----------------------------------------");
            showLogMessage("I","","  排除未登錄資料");
            selectMktChannelBill0(inti,intk);
            showLogMessage("I","","  處理筆數:["+ totalCnt+"] 合格筆數:["+updateCnt+"]");
            if (getValue("pseq.cap_sel",intk).equals("2"))
            {
                showLogMessage("I","","  -----------------------------------------");
                showLogMessage("I","","  排除本金類資料");
                selectMktChannelBill1(inti,intk);
                showLogMessage("I","","  處理筆數:["+ totalCnt+"] 合格筆數:["+updateCnt+"]");
            }
            if (updateCnt>0)
            {
                showLogMessage("I","","  -----------------------------------------");
                showLogMessage("I","","  載入暫存資料");
                loadMktChannelBill1(inti,intk);
                loadMktChannelBill2(inti,intk);
                showLogMessage("I","","  -----------------------------------------");
                showLogMessage("I","","  處理(mkt_channel_bill)資料");
                selectMktChannelBill(inti,intk);
                showLogMessage("I","","  處理 ["+totalCnt+"] 筆");
            }
            showLogMessage("I","","=========================================");
        }

        return(0);
    }
    // ************************************************************************
    void selectMktChannelBill0(int inti, int intk) throws Exception
    {
        selectSQL = "id_p_seqno,"
                + "purchase_date,"
                + "rowid as rowid";
        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code  = ?  "
                + "and   error_code   = '00' "
        ;

        setString(1 , getValue("parm.active_code",inti));

        if (pSeqno.length()!=0)
        {
            whereStr  = whereStr
                    + "and p_seqno = ? ";
            setString(2 , pSeqno);
        }
        else
        {
            whereStr  = whereStr
                    + "and   proc_flag  = '0' ";
        }

        openCursor();

        totalCnt = 0;
        int cnt2=0;
        while( fetchTable() )
        {
            totalCnt++;

            setValue("recd.id_p_seqno"      , getValue("id_p_seqno"));
            setValue("recd.active_code"     , getValue("parm.active_code",inti));
            setValue("recd.record_group_no" , getValue("pseq.record_group_no",intk));

            if (pSeqno.length()!=0)
            {
                showLogMessage("I","","STEP 0 ["+ getValue("id_p_seqno") +"]");
                showLogMessage("I","","       ["+ getValue("pseq.record_group_no",intk) +"]");
            }

            cnt2 = getLoadData("recd.id_p_seqno,recd.active_code,recd.record_group_no");

            if (cnt2==0)
            {
                updateMktChannelBill("B0");
                continue;
            }

            if (pSeqno.length()!=0)
            {
                showLogMessage("I","","STEP 1 ["+ inti +"][" + intk + "]");
                showLogMessage("I","","STEP 2 ["+ getValue("pseq.record_date_sel",intk) + "]");
            }

            if (getValue("pseq.record_date_sel",intk).equals("0"))
            {
            }
            else if (getValue("pseq.record_date_sel",intk).equals("1"))
            {
                if (pSeqno.length()!=0)
                {
                    showLogMessage("I","","STEP 3 ["+ getValue("purchase_date") + "]");
                    showLogMessage("I","","STEP 4 ["+ getValue("recd.record_date") + "]");
                }
                if (getValue("purchase_date").compareTo(getValue("recd.record_date"))>0)
                {
                    updateMktChannelBill("B1");
                    continue;
                }
            }
            else if (getValue("pseq.record_date_sel",intk).equals("2"))
            {
                if (getValue("purchase_date").compareTo(getValue("recd.record_date"))<0)
                {
                    updateMktChannelBill("B2");
                    continue;
                }
            }

            if (getValue("pseq.pur_date_sel",intk).equals("2"))
            {
                if ((getValue("purchase_date").compareTo(getValue("pseq.purchase_date_s",intk))<0)||
                        (getValue("purchase_date").compareTo(getValue("pseq.purchase_date_e",intk))>0))
                {
                    updateMktChannelBill("B3");
                    continue;
                }
            }
            if (pSeqno.length()!=0)
                showLogMessage("I","","STEP 3 ["+ getValue("purchase_date") + "]");

            setValue("data_key" , getValue("parm.active_code",inti)+getValue("pseq.active_seq",intk));

            if (getValue("pseq.week_cond",intk).equals("Y"))
            {
                if (pSeqno.length()!=0)
                    showLogMessage("I","","STEP 4 ["+ String.format("%01d",comm.getConvertDate("DAY_OF_WEEK",getValue("purchase_date"))) + "]");
                if (selectMktBnData(String.format("%01d",comm.getConvertDate("DAY_OF_WEEK",getValue("purchase_date"))),
                        "1","5",3)!=0)
                {
                    updateMktChannelBill("B4");
                    continue;
                }
            }

            if (getValue("pseq.month_cond",intk).equals("Y"))
            {
                if (selectMktBnData(getValue("purchase_date").substring(6,8),"1","6",3)!=0)
                {
                    updateMktChannelBill("B5");
                    continue;
                }
            }
            updateMktChannelBill2(intk);

            updateCnt++;
        }
        closeCursor();
    }
    // ************************************************************************
    void selectMktChannelBill1(int inti, int intk) throws Exception
    {
        selectSQL = "id_p_seqno,"
                + "purchase_date,"
                + "acct_code,"
                + "rowid as rowid";
        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code  = ?  "
                + "and   active_seq  != '00' "
                + "and   error_code   = '00' "
        ;

        setString(1 , getValue("parm.active_code",inti));

        if (pSeqno.length()!=0)
        {
            whereStr  = whereStr
                    + "and p_seqno = ? ";
            setString(2 , pSeqno);
        }
        else
        {
            whereStr  = whereStr
                    + "and   proc_flag  = '0' ";
        }

        openCursor();

        totalCnt = 0;
        int cnt2=0;
        while( fetchTable() )
        {
            totalCnt++;

            if (((!getValue("pseq.bl_cond",intk).equals("Y"))&&(getValue("acct_code").equals("BL")))||
                    ((!getValue("pseq.it_cond",intk).equals("Y"))&&(getValue("acct_code").equals("IT")))||
                    ((!getValue("pseq.ca_cond",intk).equals("Y"))&&(getValue("acct_code").equals("CA")))||
                    ((!getValue("pseq.id_cond",intk).equals("Y"))&&(getValue("acct_code").equals("ID")))||
                    ((!getValue("pseq.ao_cond",intk).equals("Y"))&&(getValue("acct_code").equals("AO")))||
                    ((!getValue("pseq.ot_cond",intk).equals("Y"))&&(getValue("acct_code").equals("OT"))))
            {
                updateMktChannelBill("BA");
                continue;
            }
        }
        closeCursor();
    }
    // ************************************************************************
    void selectMktChannelBill(int inti, int intk) throws Exception
    {
        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            selectSQL  = "id_no";
        if (getValue("parm.feedback_key_sel",inti).equals("2"))
            selectSQL  = "p_seqno,"
                    + "vd_flag,"
                    + "max(id_p_seqno) as id_p_seqno,"
                    + "max(acct_type) as acct_type";
        if (getValue("parm.feedback_key_sel",inti).equals("3"))
            selectSQL  = "ori_major_card_no as card_no,"
                    + "max(id_p_seqno) as id_p_seqno,"
                    + "max(vd_flag) as vd_flag,"
                    + "max(p_seqno) as p_seqno,"
                    + "max(acct_type) as acct_type";
        if (getValue("parm.feedback_key_sel",inti).equals("4"))
            selectSQL  = "ori_card_no as card_no,"
                    + "max(id_p_seqno) as id_p_seqno,"
                    + "max(vd_flag) as vd_flag,"
                    + "max(p_seqno) as p_seqno,"
                    + "max(acct_type) as acct_type";

        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code  = ?  "
                + "and   error_code   = '00' "
                + "and   active_seq   = ? "
        ;

        setString(1 , getValue("parm.active_code",inti));
        setString(2 , getValue("pseq.active_seq" ,intk));

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            whereStr  = whereStr
                    + "group by id_no "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if (pSeqno.length()!=0)
            {
                whereStr  = whereStr
                        + "and p_seqno = ? ";
                setString(3 , pSeqno);
            }
            whereStr  = whereStr
                    + "and   proc_flag    = '0' "
                    + "group by p_seqno,vd_flag "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            whereStr  = whereStr
                    + "and   proc_flag    = '0' "
                    + "group by ori_major_card_no "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            whereStr  = whereStr
                    + "and   proc_flag    = '0' "
                    + "group by ori_card_no "
            ;
        }


//showLogMessage("I","","STEP 2.0 [" + selectSQL +"]");
//showLogMessage("I","","STEP 2.1 [" + whereStr +"]");
        openCursor();

        int cnt1=0;
        int[] dataCnt = new int[50];
//showLogMessage("I","","STEP 2.2 [" + getValue("parm.active_code",inti) +"]");

        while( fetchTable() )
        {
            totalCnt++;

            if (getValue("parm.feedback_key_sel",inti).equals("1"))
            {
                setValue("mcb1.id_no"     , getValue("id_no"));  // for per_amt,sum_amt,sum_cnt
                cnt1 = getLoadData("mcb1.id_no");
            }
            else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            {
                setValue("mcb1.vd_flag"     , getValue("vd_flag"));
                setValue("mcb1.p_seqno"     , getValue("p_seqno"));
                cnt1 = getLoadData("mcb1.p_seqno,mcb1.vd_flag");
            }
            else if (getValue("parm.feedback_key_sel",inti).equals("3"))
            {
                setValue("mcb1.major_card_no"   , getValue("card_no"));
                cnt1 = getLoadData("mcb1.major_card_no");
            }
            else
            {
                setValue("mcb1.card_no"     , getValue("card_no"));
                cnt1 = getLoadData("mcb1.card_no");
            }

            if (getValue("pseq.sum_amt_cond",intk).equals("Y"))
                if (getValueDouble("mcb1.dest_amt")<getValueDouble("pseq.sum_amt",intk)) continue;

            if (getValue("pseq.sum_cnt_cond",intk).equals("Y"))
                if (getValueDouble("mcb1.dest_cnt")<getValueDouble("pseq.sum_cnt",intk)) continue;

            if (getValue("parm.feedback_key_sel",inti).equals("1"))
            {
                setValue("mcb2.id_no"     , getValue("id_no"));  // for per_amt,sum_amt,sum_cnt
                cnt1 = getLoadData("mcb2.id_no");
                if (cnt1==0) continue;
            }
            else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            {
                setValue("mcb2.vd_flag"     , getValue("vd_flag"));
                setValue("mcb2.p_seqno"     , getValue("p_seqno"));
                cnt1 = getLoadData("mcb2.p_seqno,mcb2.vd_flag");
                if (cnt1==0) continue;
            }
            else if (getValue("parm.feedback_key_sel",inti).equals("3"))
            {
                setValue("mcb2.major_card_no"     , getValue("card_no"));
                cnt1 = getLoadData("mcb2.major_card_no");
                if (cnt1==0) continue;
            }
            else
            {
                setValue("mcb2.card_no"     , getValue("card_no"));
                cnt1 = getLoadData("mcb2.card_no");
                if (cnt1==0) continue;
            }
            procFeedback(inti,intk,cnt1);
        }
        closeCursor();
//for (inti=0;inti<7;inti++)
//   showLogMessage("I","","dataCnt: ["+ inti +"]["+dataCnt[inti]+"]");
    }
    // ************************************************************************
    int procFeedback(int inti, int intk, int cnt1) throws Exception
    {
        int[]    rankCnt = {0,0,0,0,0};
        double[] rankAmt = {0,0,0,0,0};
        int[]    lmtCnt = {0,0,0,0,0};
        int calAmt =0;

        if (pSeqno.length()!=0)
            showLogMessage("I","","STEP 0001 [" + cnt1 +"]");
        for (int inta=0;inta<cnt1;inta++)
        {
            int aboveCnt=0;
            if (getValue("pseq.threshold_sel",intk).equals("1"))
            {
                if (pSeqno.length()!=0)
                    showLogMessage("I","","STEP 0002 [" + amtCntSel[intk] +"]");
                calAmt = 0;
                for (int intm = 0; intm< amtCntSel[intk]; intm++)
                {
                    if (pSeqno.length()!=0)
                    {
                        showLogMessage("I","","STEP 0003A[" + getValueDouble("mcb2.dest_amt",inta) +"]");
                        showLogMessage("I","","STEP 0003B[" + purchaseAmtS[intk][intm] +"]");
                    }
                    double rangeAmt = 0;
                    if (Math.abs(getValueDouble("mcb2.dest_amt",inta))< purchaseAmtS[intk][intm]) continue;

                    lmtCnt[intm]++;
                    if (feedbackCnt[intk][intm]!=0)
                    {
                        if (feedbackCnt[intk][intm]<lmtCnt[intm]) continue;
                    }
                    if (pSeqno.length()!=0)
                        showLogMessage("I","","STEP 0004 [" + intm +"]");
                    if (intm==0)
                    {
                        if (Math.abs(getValueDouble("mcb2.dest_amt",inta))> purchaseAmtE[intk][intm])
                            rangeAmt = purchaseAmtE[intk][intm];
                        else
                            rangeAmt = Math.abs(getValueDouble("mcb2.dest_amt",inta));
                        if (pSeqno.length()!=0)
                            showLogMessage("I","","STEP 0005 [" + rangeAmt +"]");
                    }
                    else
                    {
                        if (Math.abs(getValueDouble("mcb2.dest_amt",inta))> purchaseAmtE[intk][intm])
                            rangeAmt = purchaseAmtE[intk][intm]
                                    - purchaseAmtE[intk][intm-1];
                        else
                            rangeAmt = Math.abs(getValueDouble("mcb2.dest_amt",inta))
                                    - purchaseAmtE[intk][intm-1];
                        if (pSeqno.length()!=0)
                            showLogMessage("I","","STEP 0006 [" + rangeAmt +"]");
                    }

                    if (getValueDouble("mcb2.dest_amt",inta)<0) rangeAmt =rangeAmt * -1;

                    if (pSeqno.length()!=0)
                        showLogMessage("I","","STEP 0007 [" + rangeAmt +"]");
                    if (feedbackRate[intk][intm]>0)
                    {
                        calAmt = (int)Math.round(rangeAmt
                                * feedbackRate[intk][intm]
                                / 100.0);

                        if (feedbackLmt[intk][intm]>0)
                            if (Math.abs(calAmt)> feedbackLmt[intk][intm])
                            {
                                if (calAmt>0)
                                    calAmt = (int) feedbackLmt[intk][intm];
                                else  calAmt = (int) feedbackLmt[intk][intm]*-1;
                            }
                        if (pSeqno.length()!=0)
                            showLogMessage("I","","STEP 0008 [" + calAmt +"]");
                    }
                    else
                    {
                        if (getValueDouble("mcb2.dest_amt",inta)<0)
                            calAmt = (int) feedbackAmt[intk][intm]*-1;
                        else
                            calAmt = (int) feedbackAmt[intk][intm];
                        if (pSeqno.length()!=0)
                            showLogMessage("I","","STEP 0009 [" + calAmt +"]");
                    }
                    aboveCnt =1;

                    if (getValue("parm.above_cond",inti).equals("Y"))
                    {
                        if (getValueDouble("mcb2.dest_amt",inta)>0)
                        {
                            if (getValueDouble("mcb2.dest_amt",inta)>=
                                    getValueDouble("parm.above_amt",intk))
                            {
                                aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",inta)/
                                        getValueDouble("parm.above_amt",intk))*
                                        getValueInt("parm.above_cnt",intk);
                                calAmt = calAmt*aboveCnt;
                            }
                            if (pSeqno.length()!=0)
                                showLogMessage("I","","STEP 0010 [" + calAmt +"]");
                        }
                        else
                        {
                            if (getValueDouble("mcb2.dest_amt",inta)*-1>=
                                    getValueDouble("parm.above_amt",intk))
                            {
                                aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",inta)*-1/
                                        getValueDouble("parm.above_amt",intk))*
                                        getValueInt("parm.above_cnt",intk);
                                calAmt = calAmt*aboveCnt;
                            }
                            if (pSeqno.length()!=0)
                                showLogMessage("I","","STEP 0011 [" + calAmt +"]");
                        }
                    }

                    rankAmt[intm] = rankAmt[intm] + calAmt;
                    if (calAmt>0) rankCnt[intm] = rankCnt[intm] + aboveCnt;
                    else rankCnt[intm] = rankCnt[intm] - aboveCnt;
                    if (pSeqno.length()!=0)
                    {
                        showLogMessage("I","","STEP 0012 [" + rankAmt[intm] +"]");
                        showLogMessage("I","","STEP 0013 [" + rankCnt[intm] +"]");
                    }
                }
            }
            else if (getValue("pseq.threshold_sel",intk).equals("2"))
            {
                if (pSeqno.length()!=0)
                    showLogMessage("I","","STEP 0003 [" + cnt1 +"]");
                for (int intm = 0; intm< amtCntSel[intk]; intm++)
                {
                    if ((Math.abs(getValueDouble("mcb2.dest_amt",inta))>= purchaseAmtS[intk][intm])&&
                            (Math.abs(getValueDouble("mcb2.dest_amt",inta))<= purchaseAmtE[intk][intm]))
                    {
                        lmtCnt[intm]++;
                        if (feedbackCnt[intk][intm]!=0)
                        {
                            if (feedbackCnt[intk][intm]<lmtCnt[intm]) continue;
                        }
                        calAmt = 0;
                        int active_type_int = Integer.valueOf(activeType[intk][intm])-1;
                        if (feedbackRate[intk][intm]>0)
                        {
                            calAmt = (int)Math.round(getValueDouble("mcb2.dest_amt",inta)
                                    * feedbackRate[intk][intm]
                                    / 100.0);

                            if (feedbackLmt[intk][intm]>0)
                                if (Math.abs(calAmt)>Math.abs(feedbackLmt[intk][intm]))
                                {
                                    if (calAmt>0) calAmt = (int) feedbackLmt[intk][intm];
                                    else calAmt = (int) feedbackLmt[intk][intm]*-1;
                                }
                        }
                        else
                        {
                            if (getValueDouble("mcb2.dest_amt",inta)<0)
                                calAmt = (int) feedbackAmt[intk][intm]*-1;
                            else
                                calAmt = (int) feedbackAmt[intk][intm];

                        }
                        aboveCnt =1;

                        if (getValue("parm.above_cond",intk).equals("Y"))
                        {
                            if (getValueDouble("mcb2.dest_amt",inta)>0)
                            {
                                if (getValueDouble("mcb2.dest_amt",inta)>=
                                        getValueDouble("parm.above_amt",intk))
                                {
                                    aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",inta)/
                                            getValueDouble("parm.above_amt",intk))*
                                            getValueInt("parm.above_cnt",intk);
                                    calAmt = calAmt*aboveCnt;
                                }
                            }
                            else
                            {
                                if (getValueDouble("mcb2.dest_amt",inta)*-1>=
                                        getValueDouble("parm.above_amt",intk))
                                {
                                    aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",inta)*-1/
                                            getValueDouble("parm.above_amt",intk))*
                                            getValueInt("parm.above_cnt",intk);
                                    calAmt = calAmt*aboveCnt;
                                }
                            }
                        }
                        rankAmt[intm] = rankAmt[intm] + calAmt;
                        if (calAmt>0) rankCnt[intm] = rankCnt[intm] + aboveCnt;
                        else rankCnt[intm] = rankCnt[intm] - aboveCnt;
                        break;
                    }
                }
            }
        }

        if (pSeqno.length()!=0)
            showLogMessage("I","","STEP 000A [" + amtCntSel[intk] +"]");
        for (int intm = 0; intm< amtCntSel[intk]; intm++)
        {
            if (pSeqno.length()!=0)
                showLogMessage("I","","STEP 000B [" + rankAmt[intm] +"]");
            if (rankAmt[intm]<=0) continue;

            if (pSeqno.length()!=0)
                showLogMessage("I","","STEP 000C [" + rankAmt[intm] +"]");

            insertMktChannelRank(inti,intk,intm,rankCnt[intm],rankAmt[intm]);
            updateMktChannelBill1(inti,intk);
        }

        return(0);
    }
    // ************************************************************************
    void loadMktChannelRecord() throws Exception
    {
        extendField = "recd.";
        selectSQL = "id_p_seqno,"
                + "active_code,"
                + "record_date,"
                + "record_group_no";
        daoTable  = "mkt_channel_record";
        whereStr  = "WHERE active_code in ( "
                + "      select active_code "
                + "      from mkt_channel_parm "
                + "      WHERE apr_flag        = 'Y' "
                + "      AND   apr_date       != ''  "
                + "      AND   (stop_flag     != 'Y'  "
                + "       or    (stop_flag     = 'Y'  "
                + "        and  stop_date      > ? )) "
                + "      and   record_cond     = 'Y' "
                + "      and   cal_def_date    = ? ) "
                + "order by id_p_seqno,active_code,record_group_no "
        ;

        setString(1 , businessDate);
        setString(2 , businessDate);

        int  n = loadTable();
        setLoadData("recd.id_p_seqno,recd.active_code,recd.record_group_no");

        showLogMessage("I","","Load mkt_channel_record Count: ["+n+"]");
    }
    // ************************************************************************
    void loadMktChannelBill1(int inti, int intk) throws Exception
    {
        extendField = "mcb1.";

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            selectSQL = "id_no,"
                    + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt,"
                    + "sum(dest_amt) as dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            selectSQL = "vd_flag,"
                    + "p_seqno,"
                    + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt,"
                    + "sum(dest_amt) as dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            selectSQL = "ori_major_card_no as card_no,"
                    + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt,"
                    + "sum(dest_amt) as dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            selectSQL = "ori_card_no as card_no,"
                    + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt,"
                    + "sum(dest_amt) as dest_amt";
        }
        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code = ? "
                + "and   error_code  = '00' ";

        if (getValue("pseq.per_amt_cond",intk).equals("Y"))
            whereStr  = whereStr
                    + "and   (dest_amt   >= ? "
                    + " or    dest_amt   <  0) ";

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            whereStr  = whereStr
                    + "group by id_no "
                    + "order by id_no ";
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            whereStr  = whereStr
                    + "group by p_seqno,vd_flag "
                    + "order by p_seqno,vd_flag ";
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
            whereStr  = whereStr
                    + "group by ori_major_card_no "
                    + "order by ori_major_card_no ";
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
            whereStr  = whereStr
                    + "group by ori_card_no "
                    + "order by ori_card_no ";

        setString(1 , getValue("parm.active_code",inti));

        if (getValue("pseq.per_amt_cond",intk).equals("Y"))
            setDouble(2 , getValueDouble("pseq.per_amt",intk));

        int  n = loadTable();

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            setLoadData("mcb1.id_no");
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            setLoadData("mcb1.p_seqno,mcbl.vd_flag");
        else setLoadData("mcb1.card_no");

        showLogMessage("I","","Load mkt_channel_bill_1 Count: ["+n+"]");
    }
    // ************************************************************************
    void loadMktChannelBill2(int inti, int intk) throws Exception
    {
        extendField = "mcb2.";
        double perAmt = 0;
        if (getValue("pseq.per_amt_cond",intk).equals("Y"))
            perAmt = getValueDouble("pseq.per_amt",intk);

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            if (getValue("pseq.purchase_type_sel",intk).equals("1"))
                selectSQL = "id_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("2"))
                selectSQL = "id_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("3"))
                selectSQL = "id_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("4"))
                selectSQL = "id_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                selectSQL = "id_no,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if (getValue("pseq.purchase_type_sel",intk).equals("1"))
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("2"))
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("3"))
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("4"))
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            if (getValue("pseq.purchase_type_sel",intk).equals("1"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("2"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("3"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("4"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            if (getValue("pseq.purchase_type_sel",intk).equals("1"))
                selectSQL = "ori_card_no as card_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("2"))
                selectSQL = "ori_card_no as card_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("3"))
                selectSQL = "ori_card_no as card_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("4"))
                selectSQL = "ori_card_no as card_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                selectSQL = "ori_card_no as card_no,"
                        + "dest_amt";
        }
        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code = ? "
                + "and   error_code  = '00' ";
        if (getValue("pseq.per_amt_cond",intk).equals("Y"))
            whereStr  = whereStr
                    + "and   (dest_amt   >= ? "
                    + " or    dest_amt   <  0) ";

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            if ((getValue("pseq.purchase_type_sel",intk).equals("1"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("2")))
                whereStr  = whereStr
                        + "group by id_no "
                        + "order by id_no ";
            else if ((getValue("pseq.purchase_type_sel",intk).equals("3"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("4")))
                whereStr  = whereStr
                        + "group by id_no,purchase_date "
                        + "order by id_no,purchase_date ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if ((getValue("pseq.purchase_type_sel",intk).equals("1"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("2")))
                whereStr  = whereStr
                        + "group by vd_flag,p_seqno "
                        + "order by vd_flag,p_seqno ";
            else if ((getValue("pseq.purchase_type_sel",intk).equals("3"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("4")))
                whereStr  = whereStr
                        + "group by vd_flag,p_seqno,purchase_date "
                        + "order by vd_flag,p_seqno,purchase_date ";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                whereStr  = whereStr
                        + "order by vd_flag,p_seqno,purchase_date ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            if ((getValue("pseq.purchase_type_sel",intk).equals("1"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("2")))
                whereStr  = whereStr
                        + "group by ori_major_card_no "
                        + "order by ori_major_card_no ";
            else if ((getValue("psea.purchase_type_sel",intk).equals("3"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("4")))
                whereStr  = whereStr
                        + "group by ori_major_card_no,purchase_date "
                        + "order by ori_major_card_no,purchase_date ";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                whereStr  = whereStr
                        + "order by ori_major_card_no";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            if ((getValue("pseq.purchase_type_sel",intk).equals("1"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("2")))
                whereStr  = whereStr
                        + "group by ori_card_no "
                        + "order by ori_card_no ";
            else if ((getValue("pseq.purchase_type_sel",intk).equals("3"))||
                    (getValue("pseq.purchase_type_sel",intk).equals("4")))
                whereStr  = whereStr
                        + "group by ori_card_no,purchase_date "
                        + "order by ori_card_no,purchase_date ";
            else if (getValue("pseq.purchase_type_sel",intk).equals("5"))
                whereStr  = whereStr
                        + "order by ori_card_no";
        }

        setString(1 , getValue("parm.active_code",inti));
        if (getValue("pseq.per_amt_cond",intk).equals("Y"))
            setDouble(2 , getValueDouble("pseq.per_amt",intk));

//showLogMessage("I","","STEP 1 selectsql["+ selectSQL +"]");
//showLogMessage("I","","       wheretsql["+ whereStr +"]");

        int  n = loadTable();

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            setLoadData("mcb2.id_no");
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            setLoadData("mcb2.p_seqno,mcb2.vd_flag");
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
            setLoadData("mcb2.major_card_no");
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
            setLoadData("mcb2.card_no");

        showLogMessage("I","","Load mkt_channel_bill_2 Count: ["+n+"]");
    }
    // ************************************************************************
    void loadMktBnData() throws Exception
    {
        extendField = "data.";
        selectSQL = "data_key,"
                + "data_type,"
                + "data_code,"
                + "data_code2";
        daoTable  = "mkt_bn_data";
        whereStr  = "WHERE TABLE_NAME = 'MKT_CHANREC_PARM' "
                + "order by data_key,data_type,data_code,data_code2 ";

        int  n = loadTable();

        setLoadData("data.data_key,data.data_type,data.data_code");
        setLoadData("data.data_key,data.data_type");
        setLoadData("data.data_key,data.data_type,data.data_code,data.data_code2");

        showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
    }
    // ************************************************************************
    int selectMktBnData(String col1, String sel, String data_type, int dataNum) throws Exception
    {
        return selectMktBnData(col1,"","",sel,data_type,dataNum);
    }
    // ************************************************************************
    int selectMktBnData(String col1, String col2, String sel, String data_type, int dataNum) throws Exception
    {
        return selectMktBnData(col1,col2,"",sel,data_type,dataNum);
    }
    // ************************************************************************
    int selectMktBnData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
    {
        if (sel.equals("0")) return(0);

        setValue("data.data_key" , getValue("data_key"));
        setValue("data.data_type",dataType);

        int cnt1=0;
        if (dataNum==2)
        {
            cnt1 = getLoadData("data.data_key,data.data_type");
        }
        else
        {
            setValue("data.data_code",col1);
            cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
        }

        int okFlag=0;
        for (int intm=0;intm<cnt1;intm++)
        {
            if (dataNum==2)
            {
                if (getValue("data.data_code",intm).length()!=0)
                {
                    if (col1.length()!=0)
                    {
                        if (!getValue("data.data_code",intm).equals(col1)) continue;
                    }
                    else
                    {
                        if (sel.equals("1")) continue;
                    }
                }
            }
            if (getValue("data.data_code2",intm).length()!=0)
            {
                if (col2.length()!=0)
                {
                    if (!getValue("data.data_code2",intm).equals(col2)) continue;
                }
                else
                {
                    continue;
                }
            }

            if (getValue("data.data_code3",intm).length()!=0)
            {
                if (col3.length()!=0)
                {
                    if (!getValue("data.data_code3",intm).equals(col3)) continue;
                }
                else
                {
                    continue;
                }
            }
            okFlag=1;
            break;
        }

        if (sel.equals("1"))
        {
            if (okFlag==0) return(1);
            return(0);
        }
        else
        {
            if (okFlag==0) return(0);
            return(1);
        }
    }
    // ************************************************************************
    int insertMktChannelRank(int inti, int intk, int intm, int rankCnt, double rankAmt) throws Exception
    {
        extendField = "rank.";
        setValue("rank.id_no"                , "");
        setValue("rank.vd_flag"              ,  "");
        setValue("rank.p_seqno"              ,  "");
        setValue("rank.card_no"              ,  "");

        setValue("rank.active_code"          , getValue("parm.active_code",inti));
/*
  setValue("rank.feedback_key_sel"     , getValue("parm.feedback_key_sel",inti));
  setValue("rank.stmt_cycle"           , getValue("stmt_cycle"));
*/
        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            setValue("rank.id_no"                , getValue("id_no"));
//    setValue("rank.fb_acct_no"           , getValue("acct_no"));
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            setValue("rank.vd_flag"              , getValue("vd_flag"));
            setValue("rank.p_seqno"              , getValue("p_seqno"));
        }
        else
        {
            setValue("rank.card_no"              , getValue("card_no"));
        }
        setValue("rank.active_seq"           , getValue("pseq.active_seq" ,intk));
        setValue("rank.active_type"          , activeType[intk][intm]);
        setValue("rank.record_group_no"      , getValue("pseq.record_group_no",intk));
        setValueInt("rank.rank_seq"          , intm);
        setValueInt("rank.rank_cnt"          , rankCnt);
        setValueDouble("rank.rank_amt"       , rankAmt);

        setValue("rank.mod_time"             , sysDate+sysTime);
        setValue("rank.mod_pgm"              , javaProgram);

        daoTable  = "mkt_channel_rank";

        insertTable();

        return(0);
    }
    // ************************************************************************
    int deleteMktChannelRank(int inti) throws Exception
    {
        daoTable  = "mkt_channel_rank";
        whereStr  = "where  active_code   = ? ";

        setString(1 , getValue("parm.active_code",inti));

        int recCnt = deleteTable();

        showLogMessage("I","","Delete mkt_channel_rank [" + recCnt +"] 筆");

        return(0);
    }
    // ************************************************************************
    void updateMktChannelBill(String errorCode) throws Exception
    {
        daoTable  = "mkt_channel_bill a";
        updateSQL = "error_code  = ? ";
        whereStr  = "where rowid     = ? ";

        setString(1 , errorCode);
        setRowId(2 , getValue("rowid"));

        updateTable();
        return;
    }
    // ************************************************************************
    int updateMktChannelBill0(int inti, int intk) throws Exception
    {
        daoTable  = "mkt_channel_bill";
        updateSQL = "error_code         = '00',";
        if (intk==0)
            updateSQL = updateSQL
                    + "proc_flag          = '0',";

        updateSQL = updateSQL
                + "active_seq         = '00'";
        whereStr  = "where active_code  = ?  "
                + "and   (error_code  like 'B%' "
                + " or    error_code  = '00')  "
        ;

        setString(1 , getValue("parm.active_code",inti));

        totalCnt = updateTable();
        showLogMessage("I","","更新還原 mkt_channel_bill [" + totalCnt +"] 筆");
        return 0;
    }
    // ************************************************************************
    void updateMktChannelBill1(int inti, int intk) throws Exception
    {
        daoTable  = "mkt_channel_bill a";
        updateSQL = "active_seq      = ?, "
                + "proc_flag       = '1'";

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            whereStr  = "where error_code = '00' "
                    + "and   id_no      = ? "
                    + "and   active_code = ? ";
            setString(1 , getValue("pseq.active_seq" ,intk));
            setString(2 , getValue("id_no"));
            setString(3 , getValue("parm.active_code",inti));
        }
        if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            whereStr  = "where error_code = '00' "
                    + "and   p_seqno =  ? "
                    + "and   vd_flag = ?  "
                    + "and   active_code = ? ";
            setString(1 , getValue("pseq.active_seq" ,intk));
            setString(2 , getValue("p_seqno"));
            setString(3 , getValue("vd_flag"));
            setString(4 , getValue("parm.active_code",inti));
        }
        if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            whereStr  = "where error_code = '00' "
                    + "and   ori_major_card_no =  ? "
                    + "and   active_code       = ? ";
            setString(1 , getValue("pseq.active_seq" ,intk));
            setString(2 , getValue("card_no"));
            setString(3 , getValue("parm.active_code",inti));
        }
        if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            whereStr  = "where error_code = '00' "
                    + "and   ori_card_no =  ? "
                    + "and   active_code = ? ";
            setString(1 , getValue("pseq.active_seq" ,intk));
            setString(2 , getValue("card_no"));
            setString(3 , getValue("parm.active_code",inti));
        }

        updateTable();
        return;
    }
    // ************************************************************************
    int updateMktChannelBill2(int intk) throws Exception
    {
        daoTable  = "mkt_channel_bill";
        updateSQL = "active_seq    = ?";
        whereStr  = "where rowid = ?   "
                + "and   active_seq = '00' "
        ;

        setString(1 , getValue("pseq.active_seq",intk));
        setRowId(2 , getValue("rowid"));

        totalCnt = updateTable();
        return 0;
    }
// ************************************************************************


}  // End of class FetchSample
