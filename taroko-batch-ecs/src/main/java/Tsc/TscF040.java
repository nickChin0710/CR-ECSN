/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/11/17  V1.00.01    Brian     error correction                          *
 *  108/03/22  V1.00.02    David     悠遊卡-不記名悠遊卡掛失, 由轉基金做法變更                                    *
 *  109-11-17  V1.00.03    tanwei    updated for project coding standard       *
  *  109/12/30  V1.00.04    Zuwei       “兆豐”改為”合庫”   *
******************************************************************************/

package Tsc;


import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommMsgSendAttach;

/*每週撈取掛失先行墊付基金之不記名悠遊卡名單處理程式*/
public class TscF040 extends AccessDAO {

    private final String progname = "每週撈取掛失先行墊付基金之不記名悠遊卡名單處理程式  109/12/30 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hSystemDate = "";
    String hPre8Date = "";
    String hPre1Date = "";
    String hTnlhAcctType = "";
    String hTnlhAcctKey = "";
    String hTnlhIdPSeqno = "";
    String hTnlhPSeqno = "";
    String hTnlhMajorIdPSeqno = "";
    String hTnlhCardNo = "";
    String hTnlhTscCardNo = "";
    String hTardOppostDate = "";
    String hParmUser = "";
    String hParmTel = "";
    String hParmExtension = "";
    String hParmEmail = "";
    String hParmAddr = "";
    String hParmWindow = "";
    String hParmPswd = "";
    String hParmEmail2 = "";

    String type = "";
    String pk600 = "";
    String str601 = "";
    String ftpStr = "";
    String str1000 = "";
    String str100 = "";
    String str101 = "";
    int tmpCount = 0;

    int out = -1;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            // if (args.length != 0) {
            // comc.err_exit("Usage : TscF040 callbatch_seqno", "");
            // }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length > 2 || args.length < 1) {
                /*** A:由系統寄信 M:只產生檔案不寄信 ***/
                comcr.errRtn(String.format("參數錯誤,正確為：TscF040 type(A:Auto M:Manual) sysdate(yyyymmdd) "), "",
                        hCallBatchSeqno);
            }

            type = String.format(args[0]);
            String sgArgs1 = "";
            if(args.length > 1) {
				sgArgs1 = args[1];
				sgArgs1 = Normalizer.normalize(sgArgs1, java.text.Normalizer.Form.NFKD);
			}
			hSystemDate = sgArgs1;
            selectSystemDate();
            openFile();
            selectTscCard();
            selectPtrRptParm();

            if (tmpCount > 0) {

                /*** 壓縮加密 ***/
                pk600 = String.format("%s/media/msg/TSC_CARD_NO_%8.8s.zip", comc.getECSHOME(), hSystemDate);
                pk600 = Normalizer.normalize(pk600, java.text.Normalizer.Form.NFKD);
                String filename = str601;
                String hPasswd = hParmPswd;
                String zipFile = pk600;
                int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
                if (tmpInt != 0) {
                    comcr.errRtn(String.format("無法壓縮檔案[%s]", filename), "", hCallBatchSeqno);
                } else {
                    /*
                     * sender_dept_no,group_id,subject,contents,attach_filename
                     */
                    str100 = String.format("%s-%s共%d張合庫悠遊卡掛失名單-查詢餘額", hPre8Date, hPre1Date, tmpCount);
                    str1000 = String.format(
                            "Dear %s，你好：<br>%s-%s共%d筆合庫悠遊卡掛失名單-查詢餘額，煩請提供客戶悠遊卡餘額，謝謝您!!<br>P.S. 密碼為您的電話後4碼<br><br><br>************************************<br>合作金庫商業銀行 信用卡處<br>卡處作業 %s<br>TEL：%s 分機%s<br>MAIL：%s<br>MAIL：%s<br>%s<br>************************************<br>",
                            hParmWindow, hPre8Date, hPre1Date, tmpCount, hParmUser, hParmTel,
                            hParmExtension, hParmEmail, hParmEmail2, hParmAddr);
                    str101 = String.format("TSC_CARD_NO_%8.8s.zip", hSystemDate);

                    if (type.equals("A")) {
                        CommMsgSendAttach cmsa = new CommMsgSendAttach(getDBconnect(), getDBalias());
                        cmsa.hPmsgSystemName = "ECS";
                        cmsa.hPmsgPgmName = "TSC_F040";
                        cmsa.commMsgSendAttach("OP", "TSC_F040", str100, str1000, str101);
                    }
                    showLogMessage("I", "", String.format("刪除 str=[%s]", str601));
                    comc.fileDelete(str601);
                }
            }

            /*** 紀錄程式結束時間 ***/
            showLogMessage("I", "", "執行結束");
            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }


    /***********************************************************************/
    void selectSystemDate() throws Exception {
        hPre8Date = "";
        hPre1Date = "";

        sqlCmd = "select decode( cast(? as varchar(8)), '', to_char(sysdate,'yyyymmdd'), ?) h_system_date";
        sqlCmd += ", decode( to_char(to_date( decode(cast(? as varchar(8)),'',null,cast(? as varchar(8))) , 'yyyymmdd')-7 days,'yyyymmdd') , ''"
                + ", to_char(sysdate-7 days,'yyyymmdd')"
                + ", to_char(to_date( decode(cast(? as varchar(8)),'',null,cast(? as varchar(8))) , 'yyyymmdd')-7 days,'yyyymmdd')) h_pre8_date";
        sqlCmd += ", decode( to_char(to_date( decode(cast(? as varchar(8)),'',null,cast(? as varchar(8))) , 'yyyymmdd')-1 days,'yyyymmdd') , ''"
                + ", to_char(sysdate-1 days,'yyyymmdd')"
                + ", to_char(to_date( decode(cast(? as varchar(8)),'',null,cast(? as varchar(8))) , 'yyyymmdd')-1 days,'yyyymmdd')) h_pre1_date ";
        sqlCmd += " from dual ";
        setString(1, hSystemDate);
        setString(2, hSystemDate);
        setString(3, hSystemDate);
        setString(4, hSystemDate);
        setString(5, hSystemDate);
        setString(6, hSystemDate);
        setString(7, hSystemDate);
        setString(8, hSystemDate);
        setString(9, hSystemDate);
        setString(10, hSystemDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_systen_date not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hPre8Date = getValue("h_pre8_date");
            hPre1Date = getValue("h_pre1_date");
        }

    }

    /***********************************************************************/
    void openFile() throws Exception {
        /*** 開媒體檔 ***/
        str601 = String.format("%s/media/msg/TSC_CARD_NO_%8.8s.txt", comc.getECSHOME(), hSystemDate);
        str601 = Normalizer.normalize(str601, java.text.Normalizer.Form.NFKD);
            
        out = openOutputText(str601, "big5");
        if(out == -1)
            comcr.errRtn(str601, "檔案開啓失敗！", hCallBatchSeqno);
        showLogMessage("I", "", String.format("Open file=[%s]", str601));
        /** fprintf(fptr,"項次,掛失日期,悠遊卡卡號\r\n"); **/
    }

    /***********************************************************************/
    void selectTscCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " c.acct_type,";
        sqlCmd += " a.acct_key,";
        sqlCmd += " c.id_p_seqno,";
        sqlCmd += " c.major_id_p_seqno,";
        sqlCmd += " c.acno_p_seqno,";
        sqlCmd += " t.card_no,";
        sqlCmd += " t.tsc_card_no,";
        sqlCmd += " t.oppost_date ";
        sqlCmd += "from tsc_card t,crd_card c, act_acno a ";
        sqlCmd += "where t.card_no = c.card_no ";
        sqlCmd += "  and c.acno_p_seqno = a.acno_p_seqno ";
        //sqlCmd += "  and c.id_p_seqno = i.id_p_seqno ";
        //sqlCmd += "  and c.major_id_p_seqno = m.id_p_seqno ";
        sqlCmd += "  and t.current_code = '2' ";
        /*** 開啟自動加值 ***/
        sqlCmd += "  and decode(t.autoload_flag, '', 'N', t.autoload_flag) = 'Y' ";
        /*** 非記名卡 ***/
        sqlCmd += "  and decode(t.tsc_sign_flag, '', 'N', t.tsc_sign_flag) = 'N' ";
        /*** 未退卡 ***/
        sqlCmd += "  and decode(t.return_flag  , '', 'N', t.return_flag  ) = 'N' ";
        /*** 七天前到前一天 (oppost_date 抓信用卡的停卡日) ***/
        sqlCmd += "  and t.oppost_date between ? and ? ";
        sqlCmd += "order by t.oppost_date ";
        setString(1, hPre8Date);
        setString(2, hPre1Date);
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
            hTnlhAcctType = getValue("acct_type");
            hTnlhAcctKey = getValue("acct_key");
            hTnlhIdPSeqno = getValue("id_p_seqno");
            hTnlhPSeqno = getValue("acno_p_seqno");
            hTnlhMajorIdPSeqno = getValue("major_id_p_seqno");
            hTnlhCardNo = getValue("card_no");
            hTnlhTscCardNo = getValue("tsc_card_no");
            hTardOppostDate = getValue("oppost_date");
            tmpCount++;
            insertTscNsignLoanHst();
            writeTextFile(out, hTnlhTscCardNo + "\n");
            /** fprintf(fptr,"%d,%s,%s\r\n",tmp_count,h_tard_oppost_date.arr,h_tnlh_tsc_card_no.arr); **/
        }
        closeCursor(cursorIndex);
        if(out != -1)
            closeOutputText(out);
    }

    /***********************************************************************/
    void insertTscNsignLoanHst() throws Exception {
        daoTable = "tsc_nsign_loan_hst";
        setValue("acct_type", hTnlhAcctType);
        setValue("id_p_seqno", hTnlhIdPSeqno);
        setValue("major_id_p_seqno", hTnlhMajorIdPSeqno);
        setValue("acno_p_seqno",  hTnlhPSeqno);
        setValue("tsc_oppost_date", hTardOppostDate);
        setValue("card_no", hTnlhCardNo);
        setValue("tsc_card_no", hTnlhTscCardNo);
        setValueDouble("remain_amt", 0);
        setValueInt("prebalance_amt", 0);
        setValue("process_status", "0");
        setValue("mod_time", sysDate + sysTime);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_nsign_loan_hst duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectPtrRptParm() throws Exception {
        hParmUser = "";
        hParmTel = "";
        hParmExtension = "";
        hParmEmail = "";
        hParmAddr = "";
        hParmWindow = "";
        hParmPswd = "";
        hParmEmail2 = "";

        sqlCmd = "select a1,";
        sqlCmd += "a2,";
        sqlCmd += "a3,";
        sqlCmd += "a4,";
        sqlCmd += "a5,";
        sqlCmd += "a6,";
        sqlCmd += "a7,";
        sqlCmd += "a8 ";
        sqlCmd += " from ptr_rpt_parm  ";
        sqlCmd += "where parm_pgm = ? ";
        setString(1, javaProgram);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_rpt_parm not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hParmUser = getValue("a1");
            hParmTel = getValue("a2");
            hParmExtension = getValue("a3");
            hParmEmail = getValue("a4");
            hParmAddr = getValue("a5");
            hParmWindow = getValue("a6");
            hParmPswd = getValue("a7");
            hParmEmail2 = getValue("a8");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF040 proc = new TscF040();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/

}
