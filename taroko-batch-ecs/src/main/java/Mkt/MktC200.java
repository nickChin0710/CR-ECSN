/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/04/28  V1.00.00    Yang Bo                 program initial                                   *
 *  112/05/04  V1.00.01    Zuwei Su                增加log                                           *
 *  112/05/12  V1.00.01    Grace Huang             增'回饋分析日期'(cal_def_date) 欄位,                 *
 *                                                 做為識別相同活動代號, 不同回饋周期                     *
 *****************************************************************************************************/
package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class MktC200 extends AccessDAO {
    private final String PROGNAME = "通路活動-名單(一般)回饋處理  112/04/28 V1.00.00";
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommFunction comm = new CommFunction();

    int parmCnt = 0;
    int totalCnt = 0;
    int lackCnt = 0;	//缺少電子禮券 筆數
    int noCallCnt = 0;	//手機號碼錯誤 筆數

    // ************************************************************************
    public static void main(String[] args) throws Exception {
        MktC200 proc = new MktC200();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", " ");

            if (comm.isAppActive(javaProgram)) {
                showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
                return (0);
            }

            if (args.length > 1) {
                showLogMessage("I","","請輸入參數:");
                showLogMessage("I","","PARM 1 : [business_date]");
                return(1);
               }

            if ( args.length == 1 ) { 
                businessDate = args[0]; 
                showLogMessage("I", "", "input 參數(本日營業日) : [" + businessDate + "]");
            }
            
            // =====================================
            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (businessDate.length() == 0) {
                selectPtrBusinday();
            }
            showLogMessage("I", "", "=========================================");
            showLogMessage("I", "", "載入參數資料(mkt_channel_parm)");
            selectMktChannelParm();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totalCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    private void selectPtrBusinday() throws Exception {
        extendField = "busi.";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select ptr_businday error!");
            exitProgram(1);
        }

        if (businessDate.length() == 0) {
            businessDate = getValue("busi.BUSINESS_DATE");
        }
        showLogMessage("I", "", "PTR_BUSINDAY.本日營業日 : [" + businessDate + "]");
    }

    // ************************************************************************
    private void selectMktChannelParm() throws Exception {
        extendField = "parm.";
        daoTable = "mkt_channel_parm";
        whereStr = " where feedback_date = '' "
                + "    and feedback_apr_date >= ? "
                + "    and lottery_cond = 'Y' "
                + "    and lottery_type = '3' "
                + "    and lottery_date = '' ";
        setString(1, comm.lastDate(businessDate));
        parmCnt = selectTable();

        showLogMessage("I", "", "參數檔載入筆數: [" + parmCnt + "]");
        for (int inti = 0; inti < parmCnt; inti++) {
            showLogMessage("I", "", "=========================================");
            showLogMessage("I", "", "符合之活動:[" + getValue("parm.active_code", inti)
                    + "] 名稱:[" + getValue("parm.active_name", inti) + "]");
            showLogMessage("I", "", "=========================================");

            totalCnt = lackCnt = noCallCnt = 0;
            showLogMessage("I", "", "處理一般名單明細資料\n");

            deleteMktNormalList(inti);
            selectMktChannelList(inti);
            updateMktChannelList(inti);

            showLogMessage("I", "", "累計處理         [" + totalCnt + "] 筆");
            //showLogMessage("I", "", "    缺少電子禮券 [" + lackCnt + "] 筆");
            //showLogMessage("I", "", "    手機號碼錯誤 [" + noCallCnt + "] 筆");
            showLogMessage("I", "", "=========================================");
            updateMktChannelParm(inti);
        }
    }

    // ************************************************************************
    private void deleteMktNormalList(int inti) throws Exception {
        daoTable = "mkt_normal_list";
        whereStr = " where active_code = ? "
        		+ "    and cal_def_date = ? ";
        setString(1, getValue("parm.active_code", inti));
        setString(2, getValue("parm.cal_def_date", inti));
        int recCnt = deleteTable();

        showLogMessage("I", "", "刪除 mkt_normal_list 筆數  : [" + recCnt + "]");
    }

    // ************************************************************************
    private void selectMktChannelList(int inti) throws Exception {
        selectSQL = "";
        daoTable = "mkt_channel_list";
        whereStr = " where lottery_int > 0 "
                + "    and active_code = ? "
                + "    and cal_def_date = ? "
                + "    and lottery_type = '3' "
                + "    and lottery_date = '' ";

        setString(1, getValue("parm.active_code", inti));
        setString(2, getValue("parm.cal_def_date", inti));

        openCursor();
        totalCnt = 0;
        while (fetchTable()) {
            totalCnt++;
            insertMktNormalList(inti);

            if (getValue("vd_flag").equals("Y")) {
                selectDbcCard();
            } else {
                selectCrdCard();
            }
        }
        closeCursor();
    }

    // ************************************************************************
    private void updateMktChannelList(int inti) throws Exception {
        daoTable = "mkt_channel_list";
        updateSQL = "proc_date = ?, "
                + "  proc_flag = 'N', "
                + "  mod_pgm = ?, "
                + "  mod_time = sysdate ";
        whereStr = " where active_code = ? "
        		+ "    and cal_def_date = ? "
                + "    and lottery_int > 0 ";

        setString(1, businessDate);
        setString(2, javaProgram);
        setString(3, getValue("parm.active_code", inti));
        setString(4, getValue("parm.cal_def_date", inti));

        int recCnt = updateTable();
        showLogMessage("I", "", "修改 mkt_channel_list 筆數  : [" + recCnt + "]");
    }

    // ************************************************************************
    private void updateMktChannelParm(int inti) throws Exception {
        daoTable = "mkt_channel_parm";
        updateSQL = "lottery_date = ?, "
                + "  mod_pgm = ?, "
                + "  mod_time = sysdate ";
        whereStr = " where active_code = ? "
                + "   and cal_def_date = ? "
                + "   and lottery_date = '' ";

        setString(1, businessDate);
        setString(2, javaProgram);
        setString(3, getValue("parm.active_code", inti));
        setString(4, getValue("parm.cal_def_date", inti));

        int recCnt = updateTable();
        showLogMessage("I", "", "修改 mkt_channel_parm 筆數  : [" + recCnt + "]");
    }

    // ************************************************************************
    private void insertMktNormalList(int inti) throws Exception {
        extendField = "lott.";

        setValue("lott.active_code", getValue("parm.active_code", inti));
        setValue("lott.cal_def_date", getValue("parm.cal_def_date", inti));
        setValue("lott.vd_flag", getValue("vd_flag"));
        setValue("lott.acct_type", getValue("acct_type"));
        setValue("lott.p_seqno", getValue("p_seqno"));
        setValue("lott.id_p_seqno", getValue("id_p_seqno"));
        setValue("lott.feedback_int", getValue("lottery_int"));
        setValue("lott.feedback_date", sysDate);
        setValue("lott.mod_time", sysDate + sysTime);
        setValue("lott.mod_pgm", javaProgram);

        daoTable = "mkt_normal_list";

        int recCnt = insertTable();
        //showLogMessage("I", "", "新增 mkt_normal_list 筆數  : [" + recCnt + "]" ) ;
    }

    // ************************************************************************
    private void selectDbcCard() throws Exception {
        extendField = "card.";
        selectSQL = "a.chi_name as name,"
                + "a.id_no,"
                + "a.id_no,"
                + "a.resident_addr1||a.resident_addr2||a.resident_addr3||a.resident_addr4||a.resident_addr5 as comm_addr,"
                + "a.resident_zip as comm_zip,"
                + "a.cellar_phone,"
                + "a.e_mail_addr as mail";
        daoTable = "dbc_idno a,dbc_card b";
        whereStr = " where a.id_p_seqno = b.id_p_seqno "
                + "    and a.id_p_seqno = ? ";

        setString(1, getValue("id_p_seqno"));

        int parmCnt = 2;
        if (getValue("p_seqno").length() != 0) {
            whereStr += "and b.p_seqno = ? ";
            setString(parmCnt++, getValue("p_seqno"));
        }

        if (getValue("card_no").length() != 0) {
            whereStr += "and b.card_no = ? ";
            setString(parmCnt, getValue("card_no"));
        }
        whereStr += " order by b.current_code, b.acct_type";

        selectTable();
    }

    // ************************************************************************
    private void selectCrdCard() throws Exception {
        extendField = "card.";
        selectSQL = "a.chi_name as name,"
                + "a.id_no,"
                + "a.resident_addr1||a.resident_addr2||a.resident_addr3||a.resident_addr4||a.resident_addr5 as comm_addr,"
                + "a.resident_zip as comm_zip,"
                + "a.cellar_phone,"
                + "a.e_mail_addr as mail";
        daoTable = "crd_idno a,crd_card b";
        whereStr = " where a.id_p_seqno = b.id_p_seqno "
                + "    and a.id_p_seqno = ? ";

        setString(1, getValue("id_p_seqno"));

        int parmCnt = 2;
        if (getValue("p_seqno").length() != 0) {
            whereStr += " and b.p_seqno = ? ";
            setString(parmCnt++, getValue("p_seqno"));
        }

        if (getValue("card_no").length() != 0) {
            whereStr += " and b.card_no = ? ";
            setString(parmCnt, getValue("card_no"));
        }
        whereStr += " order by b.current_code, b.acct_type";

        selectTable();
    }
}
