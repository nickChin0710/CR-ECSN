/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/02  V1.01.00  Brian       program initial                           *
 *  109-12-15   V1.00.01    tanwei      updated for project coding standard    *
 ******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF00a extends AccessDAO {
    private String progname = "每月簽帳資料檔(B10B)產生  109/12/15 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;


    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    int    debug = 1;
    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hIcdrRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String tmpstr1 = "";
    String hTfinFileIden = "B10B";


    String hIcdrRequestDate = "";
    String hIcdrRequestTime = "";
    String hIcdrIchCardNo  = "";
    int    hIcdrTxCnt       = 0;
    long   hIcdrTxAmt       = 0;


    int forceFlag = 0;
    int totCnt    = 0;
    int hTnlgRecordCnt = 0;
    String hHash   = "";
    String allData = "";

    Buf1 detailSt = new Buf1();
    String out = "";
    //************************************************************************************
    public int mainProcess(String[] args)
    {
        try
        {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchF00a [notify_date] [force_flag (Y/N)]", "");
            }

            // 固定要做的

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
            String hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
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


            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s",comc.ICH_BANK_ID3,hTnlgNotifyDate
                    ,hTfinFileIden);
            showLogMessage("I", "", "Process date=["+forceFlag+"]"+hTnlgNotifyDate+","+tmpstr1);

            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if(hTnlgNotifyDate.substring(6, 8).equals("28") == false) {
                    exceptExit = 0;
                    String stderr = String.format("本程式限每月28日執行 [%s]",hTnlgNotifyDate);
                    comcr.errRtn(stderr, "",comcr.hCallBatchSeqno);
                }
                if (selectIchNotifyLogA() != 0) {
                    String errMsg = String.format("select_ich_notify_log_a error !");
                    comcr.errRtn(errMsg, "",comcr.hCallBatchSeqno);
                }
            } else {
                updateIchB10bMonthA();
            }

            fileOpen();

            selectIchB10bMonth();

            if(totCnt > 0)
            {
                hHash  = comc.encryptSHA(allData, "SHA-1", "big5");
                tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n",hTfinFileIden
                        ,"01", "0001", comc.ICH_BANK_ID3, String.format("%08d",totCnt) + "A", hHash);

                lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
            }

            hTnlgRecordCnt = totCnt;

            fileClose();


            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
        }
    }
    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate     = getValue("business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
            hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
        }

    }
    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd  = "select media_create_date,";
        sqlCmd += " ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate     = getValue("ftp_send_date");
        }

        if (hTnlgFtpSendDate.length() != 0) {
            String stderr = String.format("通知檔 [%s] 已FTP至ICH , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIchB10bMonthA() throws Exception {
        daoTable  = "ich_b10b_month";
        updateSQL = "proc_flag       = 'N'";
        whereStr  = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", hTnlgFileName
                    , comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void fileOpen() throws Exception
    {
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        out = temstr1;


  /*selectSQL  = " count(*)   as all_cnt ";
  daoTable   = " ich_card_parm b ";
  whereStr   = " where 1=1               ";

  int recCnt = selectTable();*/

        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n",hTfinFileIden,"01"
                ,"0001",comc.ICH_BANK_ID3, "00000000A",hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
    }
    /***********************************************************************/
    void fileClose() throws Exception {
        comc.writeReport(out, lpar, "big5", false);
    }
    /***********************************************************************/
    void selectIchB10bMonth() throws Exception
    {
        sqlCmd = "select ";
        sqlCmd += "a.request_date,";
        sqlCmd += "a.request_time,";
        sqlCmd += "a.ich_card_no,";
        sqlCmd += "a.tx_cnt,";
        sqlCmd += "a.tx_amt,";
        sqlCmd += "a.rowid as rowid1 ";
        sqlCmd += "from  ich_b10b_month a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
        openCursor();
        while (fetchTable()) {
            hIcdrRequestDate = getValue("request_date");
            hIcdrRequestTime = getValue("request_time");
            hIcdrIchCardNo  = getValue("ich_card_no");
            hIcdrTxCnt       = getValueInt("tx_cnt");
            hIcdrTxAmt       = getValueLong("tx_amt");
            hIcdrRowid = getValue("rowid1");


            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            writeRtn();
            updateIchB10bMonth();

        }
        closeCursor();
    }
    /***********************************************************************/
    void writeRtn() throws Exception
    {

        String tmpstr = "";

        detailSt = new Buf1();

        detailSt.type = "D";


        tmpstr = String.format("%-8.8s", hIcdrRequestDate);
        detailSt.requestDate = tmpstr;
        tmpstr = String.format("%-6.6s", hIcdrRequestTime);
        detailSt.requestTime = tmpstr;
        tmpstr = String.format("%-16.16s", hIcdrIchCardNo);
        detailSt.ichCardNo = tmpstr;
        tmpstr = String.format("%08d", hIcdrTxCnt);
        detailSt.txCnt = tmpstr;
        tmpstr = String.format("%08d", hIcdrTxAmt);
        detailSt.txAmt = tmpstr;
        tmpstr = String.format("%-2.2s", " ");
        detailSt.space = tmpstr;

        tmpstr = String.format("%-14.14s", sysDate+sysTime  );
        detailSt.sysDatetime  = tmpstr;

        detailSt.fillerEnd    = "\r\n";

        String buf = detailSt.allText();
        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        allData += buf;

        if(debug==1)
            showLogMessage("I", "", "  Write =["+buf+"]"+ buf.getBytes("big5").length+","
                    + allData.getBytes("big5").length);

        return;
    }
    /***********************************************************************/
    void updateIchB10bMonth() throws Exception
    {
        daoTable   = "ich_b10b_month";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgFileName);
        setString(4, javaProgram);
        setRowId( 5, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "",comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception
    {
        IchF00a proc = new IchF00a();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
    class Buf1 {
        String type;
        String requestDate;
        String requestTime;
        String ichCardNo;
        String txCnt;
        String txAmt;
        String space;
        String sysDatetime;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type, 1);
            rtn += fixLeft(requestDate, 8);
            rtn += fixLeft(requestTime, 6);
            rtn += fixLeft(ichCardNo, 16);
            rtn += fixLeft(txCnt, 8);
            rtn += fixLeft(txAmt, 8);
            rtn += fixLeft(space    , 2);
            rtn += fixLeft(sysDatetime, 14);
            rtn += fixLeft(fillerEnd, 2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            int size = (Math.floorDiv(len, 100) + 1) * 100;
            String spc = "";
            for (int i = 0; i < size; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
        }
    }

}
