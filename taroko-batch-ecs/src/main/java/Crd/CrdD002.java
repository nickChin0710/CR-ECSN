/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/28  V1.01.01  Lai        Initial                                    *
* 109/04/14  V1.01.02  Pino       Initial                                    *
* 109/12/18  V1.00.03    shiyuqi       updated for project coding standard   *
* 112/08/26  V1.00.04  Wilson     修正弱掃問題                                                                                                *
*****************************************************************************/
package Crd;

import com.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD002 extends AccessDAO {
    private String progname = "產生開卡密碼        112/08/26  V1.00.04 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 0;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosApplyId = "";
    String mbosApplyIdCode = "";
    String mbosBirthday = "";
    String mbosNation   = "";
    String mbosGroupCode = "";
    String mbosCardNo = "";
    String mbosRowid = "";

    String mbosOpenNum = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD002 proc = new CrdD002();
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

            if (args.length > 2) {
                String err1 = "CrdD002  [seq_no]\n";
                String err2 = "CrdD002  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.hCallBatchSeqno = hCallBatchSeqno;

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
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            // showLogMessage("I","", "批號=" + h_batchno + " 製卡來源=" +
            // h_emboss_source);

            dateTime();
            selectPtrBusinday();

            totalCnt = 0;

            selectCrdEmboss();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
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

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selectCrdEmboss() throws Exception {

        selectSQL = "   batchno               " + " , recno                 " 
                  + " , emboss_source         " + " , emboss_reason         " 
                  + " , apply_id              " + " , apply_id_code         "
                  + " , birthday              " + " , group_code            " 
                  + " , decode(nation,'','1',nation) as nation              " 
                  + " , card_no               " + " , rowid      as rowid   ";
        daoTable  = "crd_emboss";
        whereStr  = "where card_no     <> ''   " 
                  + "  and open_passwd  = ''   " 
                  + "  and reject_code  = ''   "
                  + "  and in_main_date = ''   ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            mbosBatchno       = getValue("batchno");
            mbosRecno         = getValueDouble("recno");
            mbosEmbossSource = getValue("emboss_source");
            mbosEmbossReason = getValue("emboss_reason");
            mbosApplyId      = getValue("apply_id");
            mbosApplyIdCode = getValue("apply_id_code");
            mbosBirthday      = getValue("birthday");
            mbosCardNo       = getValue("card_no");
            mbosNation        = getValue("nation");
            mbosGroupCode    = getValue("group_code");

            mbosRowid         = getValue("rowid");

            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "  888 Card=[" + mbosCardNo + "]");
                showLogMessage("I", "", "  888   id=[" + mbosApplyId + "]");
                showLogMessage("I", "", "  888  src=[" + mbosEmbossSource + "]" + mbosEmbossReason);
            }

            /**************************************************************************
             * 1. 檢核是否為星座卡 2. emboss_source='1'新製卡,要產生開卡密碼 3.
             * emboss_source='5'重製卡,回頭抓取新製卡之開卡密碼 (date:2001/08/08)
             **************************************************************************/

            tmpInt = 0;
//            if ((mbos_emboss_source.trim().equals("5")) && (mbos_emboss_reason.trim().equals("2"))) {
//                tmp_int = check_star_card();
//            }

            if (tmpInt == 0) {
                totalCnt++;
                process();
            }

            updateCrdEmboss();
        }
    }

    // ************************************************************************
    public int checkStarCard() throws Exception {
        selectSQL = "count(*) as cnt   ";
        daoTable = "crd_star_card ";
        whereStr = "where card_no      = ? ";

        setString(1, mbosCardNo);

        int recordCnt = selectTable();

        if (recordCnt > 0) {
            selectSQL = "open_passwd    ";
            daoTable = "crd_emboss  ";
            whereStr = "where card_no       = ?   " + "  and emboss_source = '1' ";

            setString(1, mbosCardNo);

            recordCnt = selectTable();

            if (recordCnt > 0) {
                mbosOpenNum = getValue("open_passwd");
            }
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int process() throws Exception {
        String tempX06 = "";

        tmpInt = Integer.parseInt(mbosBatchno.substring(2, 4)); // 取月份
        tmpInt = Integer.parseInt(mbosBirthday);
        tmpChar = String.format("%07d", tmpInt - 19110000);
        tempX06 = tmpChar.substring(1, 7);

        if (debug == 1)
            showLogMessage("I", "", " 887 pass=[" + mbosBatchno     + "]" + tempX06);
        mbosOpenNum = comc.transPasswd(0, tempX06);
        if (debug == 1)
            showLogMessage("I", "", " 888 pass=[" + mbosOpenNum + "]" + tempX06);

        return (0);
    }

    // ************************************************************************
    public int updateCrdEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " update emboss =[" + mbosOpenNum + "]");

        updateSQL = "open_passwd         =  ? , " + "mod_pgm             =  ? , "
                  + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "crd_emboss";
        whereStr  = "where rowid   = ? ";

        setString(1, mbosOpenNum);
        setString(2, javaProgram);
        setString(3, sysDate + sysTime);
        setRowId(4, mbosRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosBatchno = "";
        mbosRecno = 0;
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosApplyId = "";
        mbosApplyIdCode = "";
        mbosBirthday = "";
        mbosNation   = "";
        mbosGroupCode = "";
        mbosCardNo = "";
        mbosRowid = "";

        mbosOpenNum = "";
    }
    // ************************************************************************

} // End of class FetchSample
