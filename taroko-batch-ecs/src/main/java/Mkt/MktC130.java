/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 109/07/16  V1.00.11  Allen Ho   New                                        *
 * 110/10/29  V1.01.01  Allen Ho   CR1339                                     *
 * 111/01/07  V1.01.02  Allen Ho   CR-1339  chk feedback_lmt_cnt              *
 * 111/11/11  V1.01.03  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktC130 extends AccessDAO
{
    private final String PROGNAME = "通路活動-非登錄類回饋處理程程式 111/01/07 V1.01.02";
    CommFunction comm = new CommFunction();
    CommRoutine comr = null;
    CommBonus comb = null;
    CommDBonus comd = null;

    String businessDate = "";
    String pSeqno = "";
    String activeCode = "";

    double[][] purchaseAmtS =  new double [10][8];
    double[][] purchaseAmtE =  new double [10][8];
    String[][] activeType =  new String [10][8];
    double[][] feedbackRate =  new double [10][8];
    double[][] feedbackAmt =  new double [10][8];
    double[][] feedbackLmt =  new double [10][8];
    int[][] feedbackCnt =  new int [10][5];

    int  parmCnt  = 0;
    int[] acctTypeSel = new int [20];
    int[] amtCntSel = new int [20];

    int  totalCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        MktC130 proc = new MktC130();
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

            if (args.length > 3)
            {
                showLogMessage("I","","請輸入參數:");
                showLogMessage("I","","PARM 1 : [business_date]");
                showLogMessage("I","","PARM 2 : [active_code]");
                showLogMessage("I","","PARM 3 : [id_no/p_seqno/card_no]");
                return(1);
            }

            if ( args.length >= 1 )
            { businessDate = args[0]; }
            if ( args.length >= 2 )
            { activeCode = args[1]; }
            if ( args.length == 3 )
            { pSeqno = args[2]; }

            if ( !connectDataBase() )
                return(1);

            comr = new CommRoutine(getDBconnect(),getDBalias());

            selectPtrBusinday();

            showLogMessage("I","","=========================================");
            showLogMessage("I","","載入參數資料");
            selectMktChannelParm();
            if (parmCnt==0)
            {
                showLogMessage("I","","今日["+ businessDate +"]無活動回饋");
                return(0);
            }
            showLogMessage("I","","=========================================");

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
                + "and   record_cond     = 'N' "
                + "and   cal_def_date    = ? "
                + "and   feedback_apr_date = '' "
        ;

        setString(1 , businessDate);
        setString(2 , businessDate);

        if (activeCode.length()!=0)
        {
            whereStr  = whereStr
                    + "and  active_code = ? ";
            setString(3 , activeCode);
        }

        parmCnt = selectTable();

        for (int inti=0;inti<parmCnt;inti++)
        {
            showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)+"] 名稱:["+getValue("parm.active_name",inti)+"]");
            if (pSeqno.length()!=0)
            {
                showLogMessage("I","","    feedback_key_sel(回饋方式) :["+getValue("parm.feedback_key_sel",inti)+"]");
                showLogMessage("I","","    purchase_type_sel(門檻項目) :["+getValue("parm.purchase_type_sel",inti)+"]");
                showLogMessage("I","","    thresholde_sel(門檻計算方式;1.級距,2.條件) :["+ getValue("parm.threshold_sel",inti) +"]");
            }
            purchaseAmtS[inti][0] = getValueDouble("parm.purchase_amt_s1",inti);
            purchaseAmtS[inti][1] = getValueDouble("parm.purchase_amt_s2",inti);
            purchaseAmtS[inti][2] = getValueDouble("parm.purchase_amt_s3",inti);
            purchaseAmtS[inti][3] = getValueDouble("parm.purchase_amt_s4",inti);
            purchaseAmtS[inti][4] = getValueDouble("parm.purchase_amt_s5",inti);

            for (int intm=0;intm<5;intm++)
            {
                if (purchaseAmtS[inti][intm]==0) break;
                amtCntSel[inti] = intm+1;
            }
            purchaseAmtE[inti][0] = getValueDouble("parm.purchase_amt_e1",inti);
            purchaseAmtE[inti][1] = getValueDouble("parm.purchase_amt_e2",inti);
            purchaseAmtE[inti][2] = getValueDouble("parm.purchase_amt_e3",inti);
            purchaseAmtE[inti][3] = getValueDouble("parm.purchase_amt_e4",inti);
            purchaseAmtE[inti][4] = getValueDouble("parm.purchase_amt_e5",inti);

            feedbackRate[inti][0]  = getValueDouble("parm.feedback_rate_1",inti);
            feedbackRate[inti][1]  = getValueDouble("parm.feedback_rate_2",inti);
            feedbackRate[inti][2]  = getValueDouble("parm.feedback_rate_3",inti);
            feedbackRate[inti][3]  = getValueDouble("parm.feedback_rate_4",inti);
            feedbackRate[inti][4]  = getValueDouble("parm.feedback_rate_5",inti);

            activeType[inti][0]    = getValue("parm.active_type_1",inti);
            activeType[inti][1]    = getValue("parm.active_type_2",inti);
            activeType[inti][2]    = getValue("parm.active_type_3",inti);
            activeType[inti][3]    = getValue("parm.active_type_4",inti);
            activeType[inti][4]    = getValue("parm.active_type_5",inti);

            feedbackAmt[inti][0]   = getValueDouble("parm.feedback_amt_1",inti);
            feedbackAmt[inti][1]   = getValueDouble("parm.feedback_amt_2",inti);
            feedbackAmt[inti][2]   = getValueDouble("parm.feedback_amt_3",inti);
            feedbackAmt[inti][3]   = getValueDouble("parm.feedback_amt_4",inti);
            feedbackAmt[inti][4]   = getValueDouble("parm.feedback_amt_5",inti);

            feedbackLmt[inti][0]   = getValueDouble("parm.feedback_lmt_amt_1",inti);
            feedbackLmt[inti][1]   = getValueDouble("parm.feedback_lmt_amt_2",inti);
            feedbackLmt[inti][2]   = getValueDouble("parm.feedback_lmt_amt_3",inti);
            feedbackLmt[inti][3]   = getValueDouble("parm.feedback_lmt_amt_4",inti);
            feedbackLmt[inti][4]   = getValueDouble("parm.feedback_lmt_amt_5",inti);

            feedbackCnt[inti][0]   = getValueInt("parm.feedback_lmt_cnt_1",inti);
            feedbackCnt[inti][1]   = getValueInt("parm.feedback_lmt_cnt_2",inti);
            feedbackCnt[inti][2]   = getValueInt("parm.feedback_lmt_cnt_3",inti);
            feedbackCnt[inti][3]   = getValueInt("parm.feedback_lmt_cnt_4",inti);
            feedbackCnt[inti][4]   = getValueInt("parm.feedback_lmt_cnt_5",inti);

            showLogMessage("I","","載入暫存資料");
            showLogMessage("I","","=========================================");
            deleteMktChannelRank(inti);
            loadMktChannelBill1(inti);
            loadMktChannelBill2(inti);
            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理(mkt_channel_bill)資料");
            selectMktChannelBill(inti);
            showLogMessage("I","","處理 ["+totalCnt+"] 筆");
            showLogMessage("I","","=========================================");
        }

        showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
        return(0);
    }
    // ************************************************************************
    void selectMktChannelBill(int inti) throws Exception
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
        ;
        setString(1 , getValue("parm.active_code",inti));

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            if (pSeqno.length()!=0)
            {
                whereStr  = whereStr
                        + "and  id_no      = ? ";
                setString(2 , pSeqno);
            }
            whereStr  = whereStr
                    + "group by id_no "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if (pSeqno.length()!=0)
            {
                whereStr  = whereStr
                        + "and  p_seqno    = ? ";
                setString(2 , pSeqno);
            }
            whereStr  = whereStr
                    + "group by p_seqno,vd_flag "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            if (pSeqno.length()!=0)
            {
                whereStr  = whereStr
                        + "and  ori_major_card_no  = ? ";
                setString(2 , pSeqno);
            }
            whereStr  = whereStr
                    + "group by ori_major_card_no "
            ;
        }
        if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            if (pSeqno.length()!=0)
            {
                whereStr  = whereStr
                        + "and  ori_card_no  = ? ";
                setString(2 , pSeqno);
            }
            whereStr  = whereStr
                    + "group by ori_card_no "
            ;
        }


        openCursor();

        int cnt1=0;
        int[] dataCnt = new int[50];
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
            else
            {
                setValue("mcb1.card_no"     , getValue("card_no"));
                cnt1 = getLoadData("mcb1.card_no");
            }

            dataCnt[0]++;
            if (pSeqno.length()!=0)
            {
                showLogMessage("I","","cnt1   : ["+ cnt1                 +"]");
                showLogMessage("I","","vd_flag: ["+ getValue("vd_flag")  +"]");
                showLogMessage("I","","p_seqno: ["+ getValue("p_seqno")  +"]");
                showLogMessage("I","","card_no: ["+ getValue("card_no")  +"]");
                showLogMessage("I","","sum_cnt: ["+ getValueDouble("mcb1.dest_cnt")  +"]");
                showLogMessage("I","","sum_amt: ["+ getValueDouble("mcb1.dest_amt")  +"]");
                showLogMessage("I","","par_amt: ["+ getValueDouble("parm.sum_amt",inti)  +"]");
            }
            if (getValue("parm.sum_amt_cond",inti).equals("Y"))
                if (getValueDouble("mcb1.dest_amt")<getValueDouble("parm.sum_amt",inti)) continue;

            dataCnt[1]++;
            if (getValue("parm.sum_cnt_cond",inti).equals("Y"))
                if (getValueDouble("mcb1.dest_cnt")<getValueDouble("parm.sum_cnt",inti)) continue;

            if (getValueDouble("mcb1.dest_amt")<=0) continue;

            dataCnt[2]++;

            if (getValue("parm.feedback_key_sel",inti).equals("1"))
            {
                dataCnt[3]++;
                setValue("mcb2.id_no"     , getValue("id_no"));  // for per_amt,sum_amt,sum_cnt
                cnt1 = getLoadData("mcb2.id_no");
                if (cnt1==0) continue;
            }
            else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            {
                dataCnt[4]++;
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

            dataCnt[6]++;
            procFeedback(inti,cnt1);

            processDisplay(50000); // every 10000 display message
        }
        closeCursor();
        if (pSeqno.length()!=0)
            for (inti=0;inti<7;inti++)
                showLogMessage("I","","selectMktChannelBill().dataCnt: ["+ inti +"]["+dataCnt[inti]+"]");
    }
    // ************************************************************************
    int procFeedback(int inti, int cnt1) throws Exception
    {
        int[]    rankCnt = {0,0,0,0,0};
        double[] rankAmt = {0,0,0,0,0};
        int[]    lmtCnt = {0,0,0,0,0};
        int calAmt =0;

        if (pSeqno.length()!=0)
        {
        	showLogMessage("I","","-- in procFeedback()--------------------------------------------");
        	showLogMessage("I","","    Cnt1 : ["+ cnt1 +"]");

            if (getValue("parm.feedback_key_sel",inti).equals("1"))
                showLogMessage("I","","    idn_no  : ["+ getValue("id_no") +"]");
            else if (getValue("parm.feedback_key_sel",inti).equals("2"))
                showLogMessage("I","","    p_seqno  : ["+ getValue("vd_flag") +"]["+ getValue("p_seqno") +"]");
            else
                showLogMessage("I","","    card_no  : ["+ getValue("card_no") +"]");
        }

        for (int intk=0;intk<cnt1;intk++)
        {
            int aboveCnt=0;
            if (getValue("parm.threshold_sel",inti).equals("1"))	//門檻計算方式 – 選項 (1.級距式;2.條件式) 
            {
                calAmt = 0;
                for (int intm = 0; intm< amtCntSel[inti]; intm++)
                {
                    double rangeAmt = 0;
                    if (Math.abs(getValueDouble("mcb2.dest_amt",intk))< purchaseAmtS[inti][intm]) continue;

                    lmtCnt[intm]++;
                    if (feedbackCnt[inti][intm]!=0)
                    {
                        if (feedbackCnt[inti][intm]<lmtCnt[intm]) continue;
                    }
                    if (intm==0)
                    {
                        if (Math.abs(getValueDouble("mcb2.dest_amt",intk))> purchaseAmtE[inti][intm])
                            rangeAmt = purchaseAmtE[inti][intm];
                        else
                            rangeAmt = Math.abs(getValueDouble("mcb2.dest_amt",intk));
                    }
                    else
                    {
                        if (Math.abs(getValueDouble("mcb2.dest_amt",intk))> purchaseAmtE[inti][intm])
                            rangeAmt = purchaseAmtE[inti][intm]
                                    - purchaseAmtE[inti][intm-1];
                        else
                            rangeAmt = Math.abs(getValueDouble("mcb2.dest_amt",intk))
                                    - purchaseAmtE[inti][intm-1];
                    }


                    if (getValueDouble("mcb2.dest_amt",intk)<0) rangeAmt =rangeAmt * -1;

                    if (feedbackRate[inti][intm]>0)
                    {
                        calAmt = (int)Math.round(rangeAmt
                                * feedbackRate[inti][intm]
                                / 100.0);

                        if (feedbackLmt[inti][intm]>0)
                            if (Math.abs(calAmt)> feedbackLmt[inti][intm])
                            {
                                if (calAmt>0)
                                    calAmt = (int) feedbackLmt[inti][intm];
                                else  calAmt = (int) feedbackLmt[inti][intm]*-1;
                            }
                    }
                    else
                    {
                        if (getValueDouble("mcb2.dest_amt",intk)<0)
                            calAmt = (int) feedbackAmt[inti][intm]*-1;
                        else
                            calAmt = (int) feedbackAmt[inti][intm];
                    }

                    aboveCnt =1;
                    if (pSeqno.length()!=0)
                        showLogMessage("I","","20200723 STRP 1 desc_amt["
                                + getValueDouble("mcb2.dest_amt",intk)
                                + "]["
                                + getValueDouble("parm.above_amt",inti)
                                + "]["
                                + getValueInt("parm.above_cnt",inti));

                    if (getValue("parm.above_cond",inti).equals("Y"))
                    {
                        if (getValueDouble("mcb2.dest_amt",intk)>0)
                        {
                            if (getValueDouble("mcb2.dest_amt",intk)>=
                                    getValueDouble("parm.above_amt",inti))
                            {
                                aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",intk)/
                                        getValueDouble("parm.above_amt",inti))*
                                        getValueInt("parm.above_cnt",inti);
                                calAmt = calAmt*aboveCnt;
                            }
                        }
                        else
                        {
                            if (getValueDouble("mcb2.dest_amt",intk)*-1>=
                                    getValueDouble("parm.above_amt",inti))
                            {
                                aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",intk)*-1/
                                        getValueDouble("parm.above_amt",inti))*
                                        getValueInt("parm.above_cnt",inti);
                                calAmt = calAmt*aboveCnt;
                            }
                        }
                    }
                    if (pSeqno.length()!=0)
                        showLogMessage("I","","20200723 STRP 2 above_cnt[" + aboveCnt +"]");


                    rankAmt[intm] = rankAmt[intm] + calAmt;
                    if (calAmt>0) rankCnt[intm] = rankCnt[intm] + aboveCnt;
                    else rankCnt[intm] = rankCnt[intm] - aboveCnt;
                }
            }
            else if (getValue("parm.threshold_sel",inti).equals("2"))		//門檻計算方式 – 選項 (1.級距式;2.條件式)
            {
                for (int intm = 0; intm< amtCntSel[inti]; intm++)
                {
                	if (pSeqno.length()!=0)
                	{
                        showLogMessage("I","","   (parm.threshold_sel=2)Math.abs.mcb2.dest_amt[" + Math.abs(getValueDouble("mcb2.dest_amt",intk)) +"]");
                		showLogMessage("I","","   (parm.threshold_sel=2)          purchaseAmtS[" + purchaseAmtS[inti][intm]+"]");
                		showLogMessage("I","","   (parm.threshold_sel=2)          purchaseAmtE[" + purchaseAmtE[inti][intm]+"]");
                	}

                	if ((Math.abs(getValueDouble("mcb2.dest_amt",intk))>= purchaseAmtS[inti][intm])&&
                            (Math.abs(getValueDouble("mcb2.dest_amt",intk))<= purchaseAmtE[inti][intm]))
                    {
                    	if (pSeqno.length()!=0)
                    	{
                            showLogMessage("I","","   (parm.threshold_sel=2)Math.abs.mcb2.dest_amt vs. purchaseAmtS vs. purchaseAmtE --> matched");
                    	}
                        lmtCnt[intm]++;
                        if (feedbackCnt[inti][intm]!=0)
                        {
                            if (feedbackCnt[inti][intm]<lmtCnt[intm]) continue;
                        }
                        calAmt = 0;
                        int activeTypeInt = Integer.valueOf(activeType[inti][intm])-1;
                        if (feedbackRate[inti][intm]>0)
                        {
                            calAmt = (int)Math.round(getValueDouble("mcb2.dest_amt",intk)
                                    * feedbackRate[inti][intm]
                                    / 100.0);

                            if (feedbackLmt[inti][intm]>0)
                                if (Math.abs(calAmt)>Math.abs(feedbackLmt[inti][intm]))
                                {
                                    if (calAmt>0) calAmt = (int) feedbackLmt[inti][intm];
                                    else calAmt = (int) feedbackLmt[inti][intm]*-1;
                                }
                        }
                        else
                        {
                            if (getValueDouble("mcb2.dest_amt",intk)<0)
                                calAmt = (int) feedbackAmt[inti][intm]*-1;
                            else
                                calAmt = (int) feedbackAmt[inti][intm];

                        }
                        aboveCnt =1;

                        if (pSeqno.length()!=0)
                            showLogMessage("I","","20200723 STRP 3 desc_amt["
                                    + getValueDouble("mcb2.dest_amt",intk)
                                    + "]["
                                    + getValueDouble("parm.above_amt",inti)
                                    + "]["
                                    + getValueInt("parm.above_cnt",inti));

                        if (getValue("parm.above_cond",inti).equals("Y"))
                        {
                            if (getValueDouble("mcb2.dest_amt",intk)>0)
                            {
                                if (getValueDouble("mcb2.dest_amt",intk)>=
                                        getValueDouble("parm.above_amt",inti))
                                {
                                    aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",intk)/
                                            getValueDouble("parm.above_amt",inti))*
                                            getValueInt("parm.above_cnt",inti);
                                    calAmt = calAmt*aboveCnt;
                                }
                            }
                            else
                            {
                                if (getValueDouble("mcb2.dest_amt",intk)*-1>=
                                        getValueDouble("parm.above_amt",inti))
                                {
                                    aboveCnt = (int)Math.floor(getValueDouble("mcb2.dest_amt",intk)*-1/
                                            getValueDouble("parm.above_amt",inti))*
                                            getValueInt("parm.above_cnt",inti);
                                    calAmt = calAmt*aboveCnt;
                                }
                            }
                        }

                        if (pSeqno.length()!=0)
                        {
                            showLogMessage("I","","20200723 STRP 4 above_cnt[" + aboveCnt +"]");
                            showLogMessage("I","","20200723 STRP 5 cal_amt  [" + calAmt   +"]");
                            showLogMessage("I","","20200723 STRP 6 rank     [" + intm      +"]");
                        }

                        rankAmt[intm] = rankAmt[intm] + calAmt;
                        if (calAmt>0) rankCnt[intm] = rankCnt[intm] + aboveCnt;
                        else rankCnt[intm] = rankCnt[intm] - aboveCnt;
                        if (pSeqno.length()!=0)
                        {
                            showLogMessage("I","","20200723 STRP 7 rank_cnt [" + rankCnt[intm]  +"]");
                            showLogMessage("I","","20200723 STRP 8 rank_smt [" + rankAmt[intm]  +"]");
                        }
                        break;
                    }
                }
            }
        }

        for (int intk = 0; intk< amtCntSel[inti]; intk++)
        {
            if (rankAmt[intk]<=0) continue;

            if (pSeqno.length()!=0)
            {
            	showLogMessage("I","","-- in procFeedback(), before insertMktChannelRank()--------------");
            	showLogMessage("I","","   intk      : ["+          intk  +"]");
                showLogMessage("I","","   rank_cnt2 : ["+ rankCnt[intk] +"]");
                showLogMessage("I","","   rank_amt2 : ["+ rankAmt[intk] +"]");
            }
            insertMktChannelRank(inti,intk,rankCnt[intk],rankAmt[intk]);
        }

        return(0);
    }
    // ************************************************************************
    void loadMktChannelBill1(int inti) throws Exception
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

        setString(1 , getValue("parm.active_code",inti));

        int setCnt =1;
        if (getValue("parm.per_amt_cond",inti).equals("Y"))
        {
            setCnt++;
            whereStr  = whereStr
                    + "and   (dest_amt   >= ? "
                    + " or    dest_amt   <  0) ";
            setDouble(setCnt , getValueDouble("parm.per_amt",inti));
            if (pSeqno.length()!=0)
                showLogMessage("I","","per_amt    : ["+ getValueDouble("parm.per_amt",inti) +"]");
        }

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  id_no      = ? ";
                setString(setCnt , pSeqno);
            }
            whereStr  = whereStr
                    + "group by id_no ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  p_seqno    = ? ";
                setString(setCnt , pSeqno);
            }
            whereStr  = whereStr
                    + "group by p_seqno,vd_flag "
                    + "order by p_seqno,vd_flag ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  ori_major_card_no    = ? ";
                setString(setCnt , pSeqno);
            }
            whereStr  = whereStr
                    + "group by ori_major_card_no ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  ori_card_no    = ? ";
                setString(setCnt , pSeqno);
            }
            whereStr  = whereStr
                    + "group by ori_card_no ";
        }

        int  n = loadTable();

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            setLoadData("mcb1.id_no");
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            setLoadData("mcb1.p_seqno,mcb1.vd_flag");
        else setLoadData("mcb1.card_no");
/*
  if (getValue("parm.feedback_key_sel",inti).equals("1"))
     {
      setLoadData("mcb1.id_no");
     }
  else if (getValue("parm.feedback_key_sel",inti).equals("2"))
     {
      setLoadData("mcb1.p_seqno,mcb1.vd_flag");
     }
  else
     {
      setLoadData("mcb1.card_no");
     }
*/

        if (pSeqno.length()!=0)
            showLogMessage("I","","Load mkt_channel_bill_1 Count: ["+n+"]");
    }
    // ************************************************************************
    void loadMktChannelBill2(int inti) throws Exception
    {
        extendField = "mcb2.";
        double perAmt = 0;
        if (getValue("parm.per_amt_cond",inti).equals("Y"))
            perAmt = getValueDouble("parm.per_amt",inti);

        if (pSeqno.length()!=0)
        {
        	showLogMessage("I","","---in loadMktChannelBill2() ---------");
        	showLogMessage("I","","      STEP feedback_key ["+ getValue("parm.feedback_key_sel",inti) +"]");
            showLogMessage("I","","      STEP purchase_type["+ getValue("parm.purchase_type_sel",inti) +"]");
        }

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            showLogMessage("I","","STEP check 001 ["+ getValue("parm.feedback_key_sel",inti) +"]");
            if (getValue("parm.purchase_type_sel",inti).equals("1"))
                selectSQL = "id_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("2"))
                selectSQL = "id_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("3"))
                selectSQL = "id_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("4"))
                selectSQL = "id_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                selectSQL = "id_no,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))	//回饋方式 - 2.帳戶 -----------------
        {
            if (getValue("parm.purchase_type_sel",inti).equals("1"))		//門檻項目 - 1.累積金額
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("2"))	//門檻項目 - 2.累積筆數
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("3"))	//門檻項目 - 3.日累積金額
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("4"))	//門檻項目 - 4.日累積筆數
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))	//門檻項目 - 5.單筆消費金額
                selectSQL = "vd_flag,"
                        + "p_seqno,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))	//回饋方式 - 3.正卡卡號 -----------------
        {
            if (getValue("parm.purchase_type_sel",inti).equals("1"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("2"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("3"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("4"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                selectSQL = "ori_major_card_no as major_card_no,"
                        + "dest_amt";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))	//回饋方式 - 4.卡號 -----------------
        {
            if (getValue("parm.purchase_type_sel",inti).equals("1"))
                selectSQL = "ori_card_no as card_no,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("2"))
                selectSQL = "ori_card_no as card_no,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("3"))
                selectSQL = "ori_card_no as card_no,"
                        + "purchase_date,"
                        + "sum(dest_amt) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("4"))
                selectSQL = "ori_card_no as card_no,"
                        + "purchase_date,"
                        + "sum(decode(sign(dest_amt),1,1,0)) as dest_amt";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                selectSQL = "ori_card_no as card_no,"
                        + "dest_amt";
        }
        daoTable  = "mkt_channel_bill";
        whereStr  = "where active_code = ? "
                + "and   error_code   = '00' "
        ;
        setString(1 , getValue("parm.active_code",inti));

        int setCnt = 1;
        if (getValue("parm.per_amt_cond",inti).equals("Y"))
        {
            setCnt++;
            whereStr  = whereStr
                    + "and   (dest_amt   >= ? "
                    + " or    dest_amt   <  0) ";
            setDouble(setCnt , getValueDouble("parm.per_amt",inti));
        }

        if (getValue("parm.feedback_key_sel",inti).equals("1"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  id_no      = ? ";
                setString(setCnt , pSeqno);
            }
            if ((getValue("parm.purchase_type_sel",inti).equals("1"))||
                    (getValue("parm.purchase_type_sel",inti).equals("2")))
                whereStr  = whereStr + "group by id_no ";
            else if ((getValue("parm.purchase_type_sel",inti).equals("3"))||
                    (getValue("parm.purchase_type_sel",inti).equals("4")))
                whereStr  = whereStr
                        + "group by id_no,purchase_date "
                        + "order by id_no,purchase_date ";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                whereStr  = whereStr
                        + "order by id_no ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  p_seqno    = ? ";
                setString(setCnt , pSeqno);
            }
            if ((getValue("parm.purchase_type_sel",inti).equals("1"))||
                    (getValue("parm.purchase_type_sel",inti).equals("2")))
                whereStr  = whereStr + "group by vd_flag,p_seqno ";
            else if ((getValue("parm.purchase_type_sel",inti).equals("3"))||
                    (getValue("parm.purchase_type_sel",inti).equals("4")))
                whereStr  = whereStr
                        + "group by vd_flag,p_seqno,purchase_date "
                        + "order by vd_flag,p_seqno,purchase_date ";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                whereStr  = whereStr
                        + "order by vd_flag,p_seqno,purchase_date ";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  ori_major_card_no = ? ";
                setString(setCnt , pSeqno);
            }
            if ((getValue("parm.purchase_type_sel",inti).equals("1"))||
                    (getValue("parm.purchase_type_sel",inti).equals("2")))
                whereStr  = whereStr + "group by ori_major_card_no ";
            else if ((getValue("parm.purchase_type_sel",inti).equals("3"))||
                    (getValue("parm.purchase_type_sel",inti).equals("4")))
                whereStr  = whereStr
                        + "group by ori_major_card_no,purchase_date "
                        + "order by ori_major_card_no,purchase_date ";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                whereStr  = whereStr
                        + "order by ori_major_card_no";
        }
        else if (getValue("parm.feedback_key_sel",inti).equals("4"))
        {
            if (pSeqno.length()!=0)
            {
                setCnt++;
                whereStr  = whereStr
                        + "and  ori_card_no = ? ";
                setString(setCnt , pSeqno);
            }
            if ((getValue("parm.purchase_type_sel",inti).equals("1"))||
                    (getValue("parm.purchase_type_sel",inti).equals("2")))
                whereStr  = whereStr + "group by ori_card_no ";
            else if ((getValue("parm.purchase_type_sel",inti).equals("3"))||
                    (getValue("parm.purchase_type_sel",inti).equals("4")))
                whereStr  = whereStr
                        + "group by ori_card_no,purchase_date "
                        + "order by ori_card_no,purchase_date ";
            else if (getValue("parm.purchase_type_sel",inti).equals("5"))
                whereStr  = whereStr
                        + "order by ori_card_no";
        }
        if (pSeqno.length()!=0)
        {
            showLogMessage("I","","STEP SQL ["+ selectSQL +"]");
            showLogMessage("I","","STEP whe ["+ whereStr +"]");
        }

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
    int insertMktChannelRank(int inti, int intk, int rankCnt, double rankAmt) throws Exception
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
        setValue("rank.active_seq"           , "00");
        setValue("rank.active_type"          , activeType[inti][intk]);
        setValue("rank.record_group_no"      , "");
        setValueInt("rank.rank_seq"          , intk);
        setValueInt("rank.rank_cnt"          , rankCnt);
        setValueDouble("rank.rank_amt"       , rankAmt);

        setValue("rank.mod_time"             , sysDate+sysTime);
        setValue("rank.mod_pgm"              , javaProgram);

        daoTable  = "mkt_channel_rank";

        int n = insertTable();

        return(0);
    }
    // ************************************************************************
    int deleteMktChannelRank(int inti) throws Exception
    {
        daoTable  = "mkt_channel_rank";
        whereStr  = "where  active_code   = ? ";

        setString(1 , getValue("parm.active_code",inti));

        int recCnt = deleteTable();

        showLogMessage("I","","刪除 mkt_channel_rank 筆數  : ["+ recCnt +"]");

        return(0);
    }
// ************************************************************************


}  // End of class FetchSample
