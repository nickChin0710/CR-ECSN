/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00  Edson       program initial                           *
*  109/05/01  V1.02.02  陳君暘      RECS-s1090413-041 NMIP move, chg put name *
*  109/11/24  V1.00.01   shiyuqi       updated for project coding standard    *
*  112/06/16  V1.00.02   Wilson     FTP路徑名稱調整                                                                                     *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*大數據-傳送持卡人特徵檔至NCCC(O952)*/
public class CrdF082 extends AccessDAO {
    private String progname = "大數據-傳送持卡人特徵檔至NCCC(O952)  112/06/16  V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;

    int debug = 1;

    String prgmId = "CrdF082";
    String prgmName = "大數據-傳送持卡人特徵檔至NCCC(O952)";
    String rptName = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String filename = "";
    String filenameB = "";
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    long hMCurpModSeqno = 0;

    String hBusinssDate = "";
    String hSystemDate = "";
    String hSystemMmddyy = "";
    String hSystemYddd = "";
    String hSystemPrevDate = "";
    String hSystemDateF = "";
    String hDateFm = "";
    String hDateTo = "";
    String hMIdhiModAudcode = "";
    String hMCardCardNo = "";
    String hMIdhiSex = "";
    String hMIdhiBirthday = "";
    long hMIdnoAnnualIncome = 0;
    String hMIdnoBusinessCode = "";
    String hMIdhiEducation = "";
    String hMAcnoBillSendingZip = "";
    String hMCardKind = "";
    String hBusiBusinessDate = "";
    int zipCnt = 0;
    String hArgv = "";

    int fileCnt = 0;
    int fileSeq = 0;
    long totCnt = 0;
    long realCnt = 0;
    long maxSendCnt = 300000;
    String tempX15 = "";
    String tempX04 = "";
    String tempStr = "";
    int chkDig = 0;

    DT1Str dt1 = new DT1Str();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 3) {
                comc.errExit("Usage : CrdF082 [P/T] [from_date] [to_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            hArgv = "P";
            hDateFm = hSystemPrevDate;
            hDateTo = hSystemPrevDate;
            if (args.length > 0) {

                showLogMessage("I", "", "****  Process arg ="+args.length + ","+ args[0]);
                hArgv = args[0];
                if (args.length == 2) {
                    hDateFm = args[1];
                    hDateTo = "29991231";
                }
                if (args.length == 3) {
                    hDateFm = args[1];
                    hDateTo = args[2];
                }
            }

            showLogMessage("I", "", String.format("****  Process date =[%s]-[%s], max=[%d]"
                    , hDateFm, hDateTo, maxSendCnt));

            selectCrdIdnoTonccc();

            showLogMessage("I", "", String.format("** idno_tonccc 總筆數=[%d], 檔案數=[%d]"
                    , totCnt, fileCnt));
            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_ptr_businday  False !";
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }

        hBusiBusinessDate = hBusinssDate;
        hSystemMmddyy = "";
        hSystemPrevDate = "";

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'mmddyy') h_system_mmddyy,";
        sqlCmd += "to_char(sysdate,'YDDD') h_system_yddd,";
        sqlCmd += "to_char(sysdate-1 days,'yyyymmdd') h_system_prev_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_dual False!";
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemMmddyy = getValue("h_system_mmddyy");
            hSystemYddd = getValue("h_system_yddd");
            hSystemPrevDate = getValue("h_system_prev_date");
            hSystemDateF = getValue("h_system_date_f");
        }
        hModUser = comc.commGetUserID();
    }

    /***********************************************************************/
    void selectCrdIdnoTonccc() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "decode(a.mod_audcode,'U','C',mod_audcode) h_m_idhi_mod_audcode,";
        sqlCmd += "c.card_no,";
        sqlCmd += "decode(a.sex,'1','M','2','F','M') h_m_idhi_sex,";
        sqlCmd += "substr(a.birthday,1, 6) h_m_idhi_birthday,";
        sqlCmd += "round(a.annual_income/1000) h_m_idno_annual_income,";
        sqlCmd += "a.business_code,";
        sqlCmd += "decode(a.education,'','6',a.education) h_m_idhi_education,";
        sqlCmd += "b.bill_sending_zip, ";
        sqlCmd += "'C' h_m_card_kind ";
        sqlCmd += " from crd_card c,act_acno b, crd_idno_tonccc a ";
        sqlCmd += "where c.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "  and c.acno_p_seqno = b.acno_p_seqno ";
        sqlCmd += "  and a.mod_time between ? || '000000' ";
        sqlCmd +=                      "and ? || '235959' ";
        sqlCmd += "UNION ALL ";
        sqlCmd += "select decode(a.mod_audcode,'U','C',mod_audcode) h_m_idhi_mod_audcode, ";
        sqlCmd += "c.card_no, ";
        sqlCmd += "decode(a.sex,'1','M','2','F','M') h_m_idhi_sex, ";
        sqlCmd += "substr(a.birthday, 1, 6) h_m_idhi_birthday, ";
        sqlCmd += "round(1000/1000) h_m_idno_annual_income, ";
        sqlCmd += "a.business_code, ";
        sqlCmd += "'6' h_m_idhi_education, ";
        sqlCmd += "b.bill_sending_zip, ";
        sqlCmd += "'D' h_m_card_kind ";
        sqlCmd += " from dbc_card c,dba_acno b, dbc_idno_tonccc a ";
        sqlCmd += "where c.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "  and c.p_seqno = b.p_seqno ";
        sqlCmd += "  and a.mod_time between ? || '000000' ";
        sqlCmd +=                      "and ? || '235959' ";
        setString(1, hDateFm);
        setString(2, hDateTo);
        setString(3, hDateFm);
        setString(4, hDateTo);
        int recordCnt = selectTable();
        if (debug == 1) showLogMessage("I", "", "  888  check =[" + recordCnt + "]");
        for (int i = 0; i < recordCnt; i++) {
            hMIdhiModAudcode = getValue("h_m_idhi_mod_audcode", i);
            hMCardCardNo = getValue("card_no", i);
            hMIdhiSex = getValue("h_m_idhi_sex", i);
            hMIdhiBirthday = getValue("h_m_idhi_birthday", i);
            hMIdnoAnnualIncome = getValueLong("h_m_idno_annual_income", i);
            hMIdnoBusinessCode = getValue("business_code", i);
            hMIdhiEducation = getValue("h_m_idhi_education", i);
            hMAcnoBillSendingZip = getValue("bill_sending_zip", i);
            hMCardKind = getValue("'h_m_card_kind'", i);

            totCnt++;

            if (totCnt % 10000 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]\n", totCnt));
            }

            realCnt++;
            if (realCnt == 1 || realCnt == maxSendCnt + 1) {
                if (realCnt == maxSendCnt + 1) {
                    recordTail();
                }
                buf = String.format("FH0061039%-6.6s%02d%-1.1s%-82.82s\r", hSystemDate.substring(2), fileSeq, hArgv, " ");
                lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "0", buf));
            }

            record1Rtn();

        }

        if (realCnt > 0) {
            recordTail();
        }
    }

    /***********************************************************************/
    void recordTail() throws Exception {
        if (realCnt > maxSendCnt)
            realCnt = maxSendCnt;
        buf = String.format("FT%06d%92.92s\r", realCnt, " ");
        lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "0", buf));

        fileSeq++;
        // filename   = String.format("O952017%-8.8s%02dC", h_system_date, file_seq);
        filenameB = String.format("O952006%-8.8s%02d", hSystemDate, fileSeq);
        filename   = String.format("%s/media/crd/%s", comc.getECSHOME(), filenameB);
        filename   = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
        comc.writeReport(filename, lpar1);

		commFTP = new CommFTP(getDBconnect(), getDBalias());
	    comr = new CommRoutine(getDBconnect(), getDBalias());
        procFTP();
        renameFile1(filenameB);

        realCnt = 1;
        fileCnt++;
    }

    /***********************************************************************/
    void record1Rtn() throws Exception {

        dt1.txCode = hMIdhiModAudcode;

//        tempX15 = String.format("%-10.10s12345", hMCardCardNo);
//        chkDig = comc.chgnRtn(tempX15);
//        tempStr = String.format("%-15.15s%1d   ", tempX15, chkDig);
//        dt1.cardNo = tempStr;
        
        dt1.cardNo = hMCardCardNo;

        dt1.sex = hMIdhiSex;
        dt1.birthday = hMIdhiBirthday;

        if (hMIdnoAnnualIncome <= 0)
            hMIdnoAnnualIncome = 1;
        tempStr = String.format("%06d", hMIdnoAnnualIncome);
        dt1.income = tempStr;

//        businessRtn();
//        dt1.job = tempX04;
        
        dt1.job = hMIdnoBusinessCode;

        dt1.education = hMIdhiEducation;

//        if (hMCardKind.equals("D"))
//            selectPtrZipcode();

        dt1.zipCode = hMAcnoBillSendingZip;

        buf = String.format("%-100.100s\r", dt1.allText());
        lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    /** @throws Exception *******************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + filenameB + " 開始傳送....");
        int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + filenameB);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + filenameB + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(filenameB);          
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
  	
    /****************************************************************************/
    int writeFile(String filename, String data) throws Exception {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "MS950"));
            out.write(String.format("file_name[%s]\n", data));
            out.close();
        } catch (IOException e) {
            comcr.errRtn("write_file error!", "", hCallBatchSeqno);
        }

        return 0;
    }

    /***********************************************************************/
//    void businessRtn() {
//
//        int tempInt = comcr.str2int(hMIdnoBusinessCode);
//        switch (tempInt) {
//            case 4:
//                tempX04 = "1100";
//                break;
//            case 10:
//                tempX04 = "1200";
//                break;
//            case 1:
//            case 5:
//                tempX04 = "1300";
//                break;
//            case 2:
//            case 16:
//                tempX04 = "1400";
//                break;
//            case 11:
//                tempX04 = "1410";
//                break;
//            case 00:
//            case 6:
//            case 15:
//            case 18:
//            case 20:
//            case 21:
//            case 23:
//            case 24:
//            case 26:
//            case 32:
//            case 33:
//            case 34:
//            case 35:
//            case 36:
//            case 37:
//            case 38:
//            case 40:
//            case 42:
//            case 43:
//            case 44:
//                tempX04 = "1500";
//                break;
//            case 22:
//            case 27:
//            case 47:
//                tempX04 = "15A0";
//                break;
//            case 48:
//                tempX04 = "15C0";
//                break;
//            case 8:
//            case 9:
//                tempX04 = "15I0";
//                break;
//            case 3:
//            case 7:
//            case 12:
//            case 13:
//            case 14:
//            case 17:
//            case 19:
//            case 39:
//            case 41:
//            case 49:
//                tempX04 = "1610";
//                break;
//            case 25:
//            case 28:
//            case 29:
//            case 30:
//            case 31:
//                tempX04 = "1620";
//                break;
//            case 45:
//            case 46:
//                tempX04 = "1700";
//                break;
//            default:
//                tempX04 = "1700";
//                break;
//        }
//    }

    /***********************************************************************/
//    void selectPtrZipcode() throws Exception {
//        int zipCnt = 0;
//
//        sqlCmd += "select count(*) zip_cnt ";
//        sqlCmd += "ptr_zipcode ";
//        sqlCmd += "where  zip_code = ? ";
//        setString(1, hMAcnoBillSendingZip);
//        int recordCnt = selectTable();
//        if (recordCnt > 0) {
//            zipCnt = getValueInt("zip_cnt");
//        }
//
//        if (zipCnt == 0) {
//            hMAcnoBillSendingZip = "241";
//        }
//    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF082 proc = new CrdF082();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class DT1Str {
        String txCode;
        String cardNo;
        String sex;
        String birthday;
        String income;
        String job;
        String education;
        String zipCode;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(txCode, 1);
            rtn += comc.fixLeft(cardNo, 19);
            rtn += comc.fixLeft(sex      , 1);
            rtn += comc.fixLeft(birthday , 8);
            rtn += comc.fixLeft(income   , 6);
            rtn += comc.fixLeft(job      , 4);
            rtn += comc.fixLeft(education, 1);
            rtn += comc.fixLeft(zipCode, 5);
            rtn += comc.fixLeft(filler1, 55);
            return rtn;
        }
    }

}
