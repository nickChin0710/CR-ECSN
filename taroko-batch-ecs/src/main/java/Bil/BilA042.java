/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/24  V1.00.01   shiyuqi       updated for project coding standard    *
*  109/12/30  V1.00.02    Zuwei       “89822222”改為”23317531”            *
*  110/04/01  V1.00.03    Justin    use common value                      *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommJcic;
import com.CommRoutine;

import hdata.jcic.JcicEnum;
import hdata.jcic.JcicHeader;
import hdata.jcic.LRPad;

/*持卡人轉換機制報送JCIC*/
public class BilA042 extends AccessDAO {
    private String progname = "持卡人轉換機制報送JCIC 110/04/01  V1.00.03 ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_KKS1;
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    CommFTP      commFTP = null;

    String prgmId = "BilA042";
    String prgmName = "持卡人轉換機制報送JCIC";
    String rptName = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String errMsg = "";
    String buf = "";

    String hBusiBusinessDate = "";
    String hSystemChiDate = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hSystemMmddyy = "";
    String hSystemYddd = "";
    String hSystemDateF = "";
    String hParmMonth = "";
    int tempSeq = 1;
    String tFileName = "";
    int realCnt = 0;
    String hMCojcTxCode = "";
    String hMCojcTxDate = "";
    String hMCojcId = "";
    String hMCojcTxType = "";
    double hMCojcTotAmt = 0;
    double hMCojcTxRate = 0;
    int hMCojcTotTerm = 0;
    String hMCojcRowid = "";

    int totCnt = 0;
    double tempDouble = 0;
    String tempX06 = "";
    String tempX03 = "";

    public int mainProcess(String[] args) {

        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilA042 yyyymm", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            if (args.length == 1) {
            	hBusiBusinessDate = args[0];
            } else {
            	if (!hBusiBusinessDate.equals(comcr.increaseDays((comc.getSubString(hBusiBusinessDate,0,6)+"07"), -1)) ) {
            		//每月6日執行, 遇假日提前至前一個營業日
            		showLogMessage("I", "", "["+hBusiBusinessDate+"]非執行日期: 每月6日執行, 遇假日提前至前一個營業日!!");
            		return 0;
            	}
            }

            totCnt = 0;
            realCnt = 0;
            selectBilContractJcic();
            procFTP(tFileName,"JCIC");
            
            procFTP(tFileName,"CRDATACREA");

            showLogMessage("I", "", String.format("** bil_contract 總筆數=[%d],run=[%d]", totCnt, realCnt));
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_ptr_businday  False !";
            comcr.errRtn(errMsg, "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }

        hBusiBusinessDate = hBusinssDate;
        hSystemMmddyy = "";
        hSystemChiDate = "";
        hParmMonth = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'mmddyy') h_system_mmddyy,";
        sqlCmd += "to_char(sysdate,'YDDD') h_system_yddd,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f,";
        sqlCmd += "to_char(add_months(sysdate,-1),'yyyymm') h_parm_month ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_dual False!";
            comcr.errRtn(errMsg, "", "");
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemMmddyy = getValue("h_system_mmddyy");
            hSystemYddd = getValue("h_system_yddd");
            hSystemDateF = getValue("h_system_date_f");
            hParmMonth = getValue("h_parm_month");
        }
    }

    /***********************************************************************/
    void selectBilContractJcic() throws Exception {
        headRtn();

        sqlCmd = "select ";
        sqlCmd += "tx_code,";
        sqlCmd += "tx_date,";
        sqlCmd += "nvl(uf_idno_id(id_p_seqno),'') as id,";
        sqlCmd += "tx_type,";
        sqlCmd += "tot_amt,";
        sqlCmd += "tx_rate,";
        sqlCmd += "tot_term,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_contract_jcic ";
        sqlCmd += "where decode(post_flag,'','N',post_flag) != 'Y' ";
        sqlCmd += "  and confirm_flag = 'Y' ";
        sqlCmd += "order by id_p_seqno ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMCojcTxCode = getValue("tx_code", i);
            hMCojcTxDate = getValue("tx_date", i);
            hMCojcId = getValue("id", i);
            hMCojcTxType = getValue("tx_type", i);
            hMCojcTotAmt = getValueDouble("tot_amt", i);
            hMCojcTxRate = getValueDouble("tx_rate", i);
            hMCojcTotTerm = getValueInt("tot_term", i);
            hMCojcRowid = getValue("rowid", i);

            totCnt++;

            realCnt++;

            moveDataRtn();

            daoTable   = "bil_contract_jcic";
            updateSQL  = " post_flag  = 'Y',";
            updateSQL += " send_date  = ?,";
            updateSQL += " mod_pgm    = ?,";
            updateSQL += " mod_time   = sysdate";
            whereStr   = "where rowid = ? ";
            setString(1, hBusiBusinessDate);
            setString(2, prgmId);
            setRowId(3, hMCojcRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_bil_contract_jcic not found!", "", "");
            }
        }

        buf = String.format("%s%08d", CommJcic.TAIL_LAST_MARK, realCnt);
        lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "1", buf));
        saveFile();
    }

    void saveFile() throws Exception {
        tempSeq = 1;

        tFileName = String.format("%s%-4.4s%1d.ks1", CommJcic.JCIC_BANK_NO, hBusiBusinessDate.substring(4), tempSeq);
        String filename = String.format("%s/media/bil/%s", comc.getECSHOME(), tFileName);
        filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
        comc.writeReport(filename, lpar1);

    }

    /***********************************************************************/
    void headRtn() throws Exception {
    	
    	// set header
        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(comcr.str2long(hBusiBusinessDate) - 19110000, "0", 7, LRPad.L));
        jcicHeader.setFileExt(commJcic.getPadString(tempSeq, "0", 2, LRPad.L));
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 80));
        jcicHeader.setFiller3("");
        jcicHeader.setLen("");
        
        buf = jcicHeader.produceStr();
        //

//        buf = String.format("%-18.18s%-3.3s%-5.5s%07d%02d%-10.10s%-16.16s%-80.80s", "JCIC-DAT-KKS1-V01-", "017", " ",
//                comcr.str2long(hSystemDate) - 19110000, tempSeq, " ", "02-23317531#1523",
//                "資訊單位聯絡人—資訊部林谷峰，單位聯絡人—審查信用卡處范素榕");

        lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "0", buf));

    }

    /***********************************************************************/
    void moveDataRtn() throws Exception {

        tempDouble = hMCojcTotAmt;
        tempX06 = String.format("%06.3f", hMCojcTxRate);
        tempX03 = String.format("%03d", hMCojcTotTerm);

        buf = String.format("%-1.1s%-3.3s%07d%-10.10s%-1.1s%011.0f%-6.6s%-3.3s", hMCojcTxCode, CommJcic.JCIC_BANK_NO,
                comcr.str2long(hMCojcTxDate) - 19110000, hMCojcId, hMCojcTxType, tempDouble, tempX06,
                tempX03);
        lpar1.add(comcr.putReport(prgmId, rptName, sysDate, ++rptSeq, "1", buf));

    }

    /***********************************************************************/
	void procFTP(String isFileName, String refIpCode) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName(refIpCode, "put " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
		} else {
			comc.fileRename2(String.format("%s/media/bil/", comc.getECSHOME()) + isFileName,
					String.format("%s/media/bil/backup/", comc.getECSHOME()) + isFileName);
		}
	}
	
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA042 proc = new BilA042();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
