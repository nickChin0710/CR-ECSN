/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/05  V1.00.00    phopho     program initial                          *
*  109/01/10  V1.00.01    phopho     fix writeTextFile add "\n"               *
*  109/12/10  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCpi;

import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA520 extends AccessDAO {
    private String progname = "無擔保債務-Z581 媒體產生處理程式 109/12/10  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommCpi        comcpi = new CommCpi();

    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String buf                    = "";
    String hBusiBusinessDate = "";
    String hTempSysdate = "";
    String hCluoId = "";
    String hCluoApplyDate = "";
    String hCluoApplyReltCode = "";
    String hTempDebitorAttr = "";
    String hCluoAprDate1 = "";
    String hCluoIdPSeqno = "";
    String hCluoPSeqno = "";
    String hCluoRowid = "";
    String hTempResidentAddr = "";
    String hTempTelNo1 = "";
    String hTempTelNo2 = "";
    String hIdnoCellarPhone = "";
    String hTempSendingAddr = "";
    int totalCnt = 0;

    private int fptr2 = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA520 [business_date [sysdate]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length > 0)
                hBusiBusinessDate = args[0];
            if (args.length > 1)
                hTempSysdate = args[1];
            selectPtrBusinday();

            fileOpen();
            selectColLiauNego();
            // fseek(fptr1, 0L, SEEK_SET);
            buf = String.format("本頁總筆數:,%08d%c%c", totalCnt, 13, 10);
            writeTextFile(fptr2, String.format("%s",buf));
            closeOutputText(fptr2);

            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));

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
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date, ";
        sqlCmd += "decode(cast(? as varchar(8)),'',to_char(decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'18'),1,sysdate+1,sysdate), 'yyyy.mm.dd'), cast(? as varchar(8))) temp_sysdate ";
        sqlCmd += "from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hTempSysdate);
        setString(4, hTempSysdate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("temp_sysdate");
        }
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String temstr1 = String.format("%s/media/col/LIAC_OUT/%sZ581.csv", comc.getECSHOME(), hTempSysdate);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
//        log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temstr1), "big5"));
        fptr2 = openOutputText(temstr1, "MS950");
        if (fptr2 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }
        
        buf = String.format("本頁總筆數:,%08d%c%c",0,13,10);
        writeTextFile(fptr2, String.format("%s",buf + "\n"));  //base level bug
    }

    /***********************************************************************/
    void selectColLiauNego() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no,";
        sqlCmd += "apply_date,";
        sqlCmd += "apply_relt_code,";
        sqlCmd += "debitor_attr1||debitor_attr2||debitor_attr3 debitor_attr,";
        sqlCmd += "apr_date1,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liau_nego ";
        sqlCmd += "where report_date1 = '' ";
        sqlCmd += "and decode(proc_flag,'','x',proc_flag) != 'Y' ";
        sqlCmd += "and bank_code = '017' ";
        sqlCmd += "and apr_user1 != 'SYSTEM' ";
        sqlCmd += "and apr_date1 <> '' ";

        openCursor();
        while (fetchTable()) {
            hCluoId = getValue("id_no");
            hCluoApplyDate = getValue("apply_date");
            hCluoApplyReltCode = getValue("apply_relt_code");
            hTempDebitorAttr = getValue("debitor_attr");
            hCluoAprDate1 = getValue("apr_date1");
            hCluoIdPSeqno = getValue("id_p_seqno");
            hCluoPSeqno = getValue("p_seqno");
            hCluoRowid = getValue("rowid");

            selectCrdIdno();
            selectActAcno();
            buf = String.format("581,C,017,%s,%07d,%s,%s,%s,%s,%s,%s,%s,%s,%07d,%s%c%c", hCluoId,
                    comcr.str2long(hCluoApplyDate) - 19110000, " ", hCluoApplyReltCode, hTempDebitorAttr,
                    hTempResidentAddr, hTempSendingAddr, hTempTelNo1, hTempTelNo2, hIdnoCellarPhone,
                    comcr.str2long(hCluoAprDate1) - 19110000, " ", 13, 10);
            writeTextFile(fptr2, String.format("%s",buf + "\n"));  //base level bug

            totalCnt++;

            updateColLiauNego();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hTempSendingAddr = "";
        sqlCmd = "select bill_sending_zip||bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 sending_addr ";
        sqlCmd += " from act_acno ";
//        sqlCmd += "where p_seqno= ? ";
        sqlCmd += "where acno_p_seqno= ? ";
        setString(1, hCluoPSeqno);
        
        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempSendingAddr = getValue("act_acno.sending_addr");
        }

        String tmpstr = String.format("%s", hTempSendingAddr);
        tmpstr = comcpi.commTransChinese(tmpstr);
        hTempSendingAddr = tmpstr;
    }

    /***********************************************************************/
    void updateColLiauNego() throws Exception {
        daoTable = "col_liau_nego";
        updateSQL = "proc_flag = '2',";
        updateSQL += " proc_date = ?,";
        updateSQL += " report_date1 = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hCluoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liau_nego not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hTempResidentAddr = "";
        hTempTelNo1 = "";
        hTempTelNo2 = "";
        hIdnoCellarPhone = "";
        sqlCmd = "select resident_zip||resident_addr1||resident_addr2||resident_addr3||resident_addr4||resident_addr5 resident_addr,";
        sqlCmd += "home_area_code1||decode(home_area_code1,'','','-')||home_tel_no1 h_temp_tel_no1,";
        sqlCmd += "home_area_code2||decode(home_area_code2,'','','-')||home_tel_no2 h_temp_tel_no2,";
        sqlCmd += "cellar_phone ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCluoIdPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempResidentAddr = getValue("crd_idno.resident_addr");
            hTempTelNo1 = getValue("crd_idno.h_temp_tel_no1");
            hTempTelNo2 = getValue("crd_idno.h_temp_tel_no2");
            hIdnoCellarPhone = getValue("crd_idno.cellar_phone");
        }

        String tmpstr = String.format("%s", hTempResidentAddr);
        tmpstr = comcpi.commTransChinese(tmpstr);
        hTempResidentAddr = tmpstr;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA520 proc = new ColA520();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
