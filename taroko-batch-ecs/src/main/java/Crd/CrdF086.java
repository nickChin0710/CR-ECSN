/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/12/01  V1.00.00  Zuwei Su    program initial                           *
*  112/12/16  V1.00.01  Wilson      增加讀取變更ID的資料                                                                             *
*  112/12/18  V1.00.02  Wilson      一律寫入crd_file_ctl                         *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.CommFTP;

import com.*;

public class CrdF086 extends AccessDAO {
    private String progname = "產生送金庫幣系統卡人卡片資料異動檔程式  112/12/18  V1.00.02 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate commDate = new CommDate();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;
    CommSecr comsecr = null;
    CommString commStr = new CommString();
    CommFTP commFTP = null;

    int debug = 0;
    int debugD = 0;
    int tmpInt = 0;
    int totalCnt = 0;

    int actCnt = 0;
    String errmsg = "";

    String allName = "";

    BufferedWriter fileWriter = null;
    // ************************************************************************

    String filename = "";
    int hYyyymmdd = 0;
    int hNn = 0;
    private String idNo;
    private String cardNo;
    private String newEndDate;
    private String currentCode;

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : CrdF086 [sysdate ex:yyyymmdd] ", "");
            }
           
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comsecr = new CommSecr(getDBconnect(), getDBalias());
            
            String lastDate = commDate.dateAdd(sysDate, 0, 0, -1);
            if (args != null && args.length == 1) {
                lastDate = args[0];
            }
            
            showLogMessage("I", "", "資料日期=[" + lastDate + "]");
            
            if (openTextFile() != 0) {
                comcr.errRtn(errmsg, "open_text_file        error", comcr.hCallBatchSeqno);
            }
            getCardIdnoData(lastDate);
            fileWriter.close();
                            
            insertFileCtl();
            procFTP();
            renameFile1(filename);

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    int openTextFile() throws Exception {
        sqlCmd = "select to_number(to_char(sysdate,'yyyymmdd')) h_yyyymmdd ";
        sqlCmd += " from dual ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hYyyymmdd = getValueInt("h_yyyymmdd");
        }

        checkFileCtl();

        filename = String.format("CAHF%08d%02d.TXT", hYyyymmdd, hNn);

        allName = String.format("%s/media/crd/%s", comc.getECSHOME(), filename);
        allName = Normalizer.normalize(allName, java.text.Normalizer.Form.NFKD);
        if (debug == 1)
            showLogMessage("D", "", " OPEN File=[" + allName + "] ");

        fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allName), "MS950"));

        return (0);
    }

    /***********************************************************************/
    void checkFileCtl() throws Exception {
        String likeFilename = "";
        String hFileName = "";
        likeFilename = String.format("CAHF%08d", hYyyymmdd) + "%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        tmpInt = selectTable();
        if (notFound.equals("Y")) {
            hNn++;
        } else {
            hFileName = getValue("file_name");
            hNn = Integer.valueOf(hFileName.substring(12, 14)) + 1;
        }

    }

    /***********************************************************************/
    /* 讀取前一日新增&異動的信用卡卡人卡片資料 */
    void getCardIdnoData(String preDate) throws Exception {
        sqlCmd = "  SELECT B.ID_NO, "
                + "        A.CARD_NO, "
                + "        A.NEW_END_DATE, "
                + "        A.CURRENT_CODE "
                + "   FROM CRD_CARD A,CRD_IDNO B "
                + "  WHERE A.ID_P_SEQNO = B.ID_P_SEQNO "
                + "    AND ((A.CRT_DATE = ?) OR "
                + "         (to_char(A.MOD_TIME,'yyyymmdd') <> A.CRT_DATE AND to_char(A.MOD_TIME,'yyyymmdd') = ?) "
                + "         ) "
                + "  UNION "
                + " SELECT B.ID_NO, "
                + "        A.CARD_NO, "
                + "        A.NEW_END_DATE, "
                + "        A.CURRENT_CODE "
                + "   FROM CRD_CARD A,CRD_CHG_ID B "
                + "  WHERE A.ID_P_SEQNO = B.ID_P_SEQNO "
                + "    AND B.CHG_DATE = ? ";
        setString(1, preDate);
        setString(2, preDate);
        setString(3, preDate);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            idNo = getValue("ID_NO");
            cardNo = getValue("CARD_NO");
            newEndDate = getValue("NEW_END_DATE");
            currentCode = getValue("CURRENT_CODE");
        }
        for (int i = 0; i < tmpInt; i++) {
            idNo = getValue("ID_NO", i);
            cardNo = getValue("CARD_NO", i);
            newEndDate = getValue("NEW_END_DATE", i);
            currentCode = getValue("CURRENT_CODE", i);

            totalCnt++;
            if (debug == 1) {
                showLogMessage("D", "",
                        " 888 read card=[" + cardNo + "]" + totalCnt + "," + idNo);
            }
            
            String lineData = comc.fixLeft("D", 1);// 明細資料
            lineData += comc.fixLeft(idNo, 11);                                                             // 持卡人ID
            lineData += comc.fixLeft(cardNo, 19);                                                           // 信用卡卡號
            lineData += comc.fixLeft(newEndDate.length() > 6 ? newEndDate.substring(0, 6) : newEndDate, 6); // 信用卡有效年月
            lineData += comc.fixLeft("", 3);                                                               // 卡別
            lineData += comc.fixLeft("", 6);                                                               // BIN
            lineData += comc.fixLeft("", 1);                                                               // 正附卡別
            lineData += comc.fixLeft("", 1);                                                               // 姓別
            lineData += comc.fixLeft("", 8);                                                               // 生日
            lineData += comc.fixLeft("0".equals(currentCode) ? "0" : "1", 1);                               // 卡片現狀碼
            lineData += comc.fixLeft("", 2);                                                               // 點數轉移方式
            lineData += comc.fixLeft("", 1);                                                               // 點數轉移條件
            lineData += comc.fixLeft("", 19);                                                               // 舊卡號
            lineData += comc.fixLeft("", 6);                                                               // 舊卡有效年月
            lineData += comc.fixLeft("", 11);                                                               // 正卡人ID
            lineData += comc.fixLeft("", 2);                                                               // 客戶類別
            lineData += comc.fixLeft("", 8);                                                               // 開戶日期
            lineData += comc.fixLeft("", 19);                                                               // 主卡卡號
            lineData += comc.fixLeft("", 12);                                                               // 持卡人姓名
            lineData += comc.fixLeft("", 15);                                                               // 手機號碼
            lineData += comc.fixLeft("", 30);                                                               // 電子郵件地址
            lineData += comc.fixLeft("", 3);                                                               // CVV
            lineData += comc.fixLeft("", 15);                                                               // 保留欄位
            
            fileWriter.write(lineData + "\r\n");
        }
        String lastLine = comc.fixLeft("T", 1);// Check Sum資料
        lastLine += comc.fixLeft(commStr.fill("0", 7), 7);                                                             // 持卡人ID
        lastLine += comc.fixLeft("", 192);  
        fileWriter.write(lastLine + "\r\n");

        return;
    }

    /*************************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", filename);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", totalCnt);
        setValueInt("record_cnt", totalCnt);
        setValue("trans_in_date", sysDate);
        daoTable = "crd_file_ctl";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_file_ctl duplicate!", filename, comcr.hCallBatchSeqno);
        } else {
            daoTable = "crd_file_ctl";
            updateSQL = " head_cnt       = ?,";
            updateSQL += " record_cnt     = ?,";
            updateSQL += " trans_in_date  = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setInt(1, totalCnt);
            setInt(2, totalCnt);
            setString(3, filename);
            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", filename,
                        comcr.hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF086 proc = new CrdF086();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    void procFTP() throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;


        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + filename + " 開始傳送....");
        int errCode = commFTP.ftplogName("CRDATACREA", "mput " + filename);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + filename + " 資料" + " errcode:" + errCode);
            insertEcsNotifyLog(filename);
        }
    }

    /****************************************************************************/
    public int insertEcsNotifyLog(String fileName) throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("unit_code", comr.getObjectOwner("3", javaProgram));
        setValue("obj_type", "3");
        setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_name", "媒體檔名:" + fileName);
        setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_desc2", "");
        setValue("trans_seqno", commFTP.hEflgTransSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ecs_notify_log";

        insertTable();

        return (0);
    }

    /****************************************************************************/
    void renameFile1(String removeFileName) throws Exception {
        String tmpstr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
        String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName;

        if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }
}
