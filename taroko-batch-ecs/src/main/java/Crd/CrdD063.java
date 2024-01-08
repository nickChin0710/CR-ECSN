/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/12/19  V1.00.00    Pino      program initial                           *
*  109/03/12  V1.00.01    Pino      產生檔案格式調整                                                                                      *
*  109/04/01  V1.00.02    Pino      加金融卡製卡檔合併產生                                                                           *
*  109/04/06  V1.00.03    Pino      讀txt檔                                                                                                    *
*  109/04/06  V1.00.04    Pino      刪除不需要的檢核                                                                                      *
*  109/04/06  V1.00.05    Pino      產檔格式修正及產出txt檔                                                                      *
*  109/04/07  V1.00.06    Pino      update_crd_emboss修正及檔案移至backup          *
*  109/12/23  V1.00.07   shiyuqi       updated for project coding standard   *
*  111/12/19  V1.00.08   Wilson     調整為最新格式                                                                                         * 
*  112/01/16  V1.00.09   Wilson     mark getIccardData、地址改成fixLeft          * 
*  112/02/08  V1.00.10   Wilson     產檔路徑調整                                                                                             *  
*  112/03/04  V1.00.11   Wilson     AP1金融卡製卡檔檔名調整                                                                      *
*  112/03/06  V1.00.12   Wilson     增加update crd_combo                       *
*  112/03/07  V1.00.13   Wilson     新增procFTP                                *
*  112/03/08  V1.00.14   Wilson     AP1金融卡製卡檔檔名調整為combo_debit_mkc_ns_yyyymmdd01.txt*
*  112/03/12  V1.00.15   Wilson     調整procFTP執行順序                                                                          * 
*  112/04/14  V1.00.16   Wilson     讀參數判斷是否由新系統編列票證卡號                                                    *
*  112/06/01  V1.00.17   Wilson      update mail_type、mail_branch            *
*  112/06/13  V1.00.18   Wilson     無檔案不當掉                                                                                             *
*  112/07/04  V1.00.19   Wilson     處理悠遊卡自動加值旗標                                                                           *
*  112/08/26  V1.00.20   Wilson     修正弱掃問題                                                                                             *
*  112/12/06  V1.00.21   Wilson     crd_item_unit不判斷卡種                                                             *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.CommFTP;

import com.*;

public class CrdD063 extends AccessDAO {
    private String progname = "產生 COMBO卡新製/重製製卡檔程式  112/12/06  V1.00.21  ";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommSecr comsecr = null;
    CommString commStr = new CommString();
    CommFTP commFTP = null;

    int debug   = 1;
    int debugD = 1;
    int tmpInt = 0;
    int totalCnt = 0;
    int totalCnt2 = 0;
    String checkHome = "";
    String hCallErrorDesc = "";
    String fileName1 = "", fileName2 = "";
    
    String groupName = "";
    String groupCmd = "";
    String rptName1 = "";
    int recordCnt = 0;
    int recordCnt1 = 0;
    int vendorCnt = 0;
    int embossCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hModWs = "";
    String hModLog = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallRProgramCode = "";

    String hTempUser = "";
    String hVendor = "";
    String hBinType = "";
    String hIcaNo = "";
    String hBinNo = "";
    String hCardNote = "";
    String pServiceCode = "";
    String pKeyType = "";
    String pDerivKey = "";
    int pLOffLnLmt = 0;
    int pUOffLnLmt = 0;
    String pIcIndicator = "";
    String pExpireDate = "";
    String pIcKind = "";
    String pCheckKeyExpire = "";
    String pClmFlag = "";
    String pBankNo = "";
    String pBalReverseFlag = "";
    String hEmbossData = "";
    String hCoMemberFlag = "";
    String pEmbossData = "";
    String hCardIndicator = "";
    String gAcctType = "";
    String hPmNcccNo1 = "";
    String hPmNcccNo2 = "";
    String hSupNcccNo1 = "";
    String hSupNcccNo2 = "";
    String hServiceCode = "";
    String hMajorCardNo = "";
    String hNcccFilename = "";
    int hRecCnt1 = 0;
    int hRecCnt2 = 0;
    String fileDate = "";
    int hNn = 0;
    String hDebtActNo = "";
    String hAcnoBillApplyFlag = "";
    double hAcnoRevolveIntRate = 0.0;
    String hAcnoLineOfCreditAmtCash = "";
    String hAcnoStmtCycle = "";
    String hAcnoBillSendingZip = "";
    String hAcnoBillSendingAddr1 = "";
    String hAcnoBillSendingAddr2 = "";
    String hAcnoBillSendingAddr3 = "";
    String hAcnoBillSendingAddr4 = "";
    String hAcnoBillSendingAddr5 = "";
    String hAcnoComboAcctNo = "";
    String hMbosBatchno = "";
    double hMbosRecno = 0;
    String hMbosEmbossSource = "";
    String hMbosEmbossReason = "";
    String hMbosAcctType = "";
    String hMbosAcctKey = "";
    String hMbosToNcccCode = "";
    String hMbosCardType = "";
    String hMbosGroupCode = "";
    String hMbosBinNo = "";
    String hMbosElectronicCode = "";
    String hMbosCardNo = "";
    String hMbosMajorCardNo = "";
    String hMbosApplyId = "";
    String hMbosApplyIdCode = "";
    String hMbosValidFm = "";
    String hMbosValidTo = "";
    String hMbosMailZip = "";
    String hMbosBirthday = "";
    String hMbosNation = "";
    String hMbosBusinessCode = "";
    String hMbosEducation = "";
    String hMbosActNo = "";
    String hMbosHomeAreaCode1 = "";
    String hMbosHomeTelNo1 = "";
    String hMbosOrgEmbossData = "";
    String hMbosEmboss4ThData = "";
    String hMbosMemberId = "";
    String hMbosPmId = "";
    String hMbosPmIdCode = "";
    String hMbosCorpNo = "";
    String hMbosCorpNoCode = "";
    String hMbosRegBankNo = "";
    String hMbosForceFlag = "";
    String hMbosServiceCode = "";
    String hMbosEngName = "";
    String hMbosMarriage = "";
    String hMbosRelWithPm = "";
    String hMbosUnitCode = "";
    String hMbosSex = "";
    String hMbosPvv = "";
    String hMbosCvv = "";
    String hMbosPvki = "";
    String hMbosCvv2 = "";
    String hMbosOpenNum = "";
    String hMbosOldCardNo = "";
    String hMbosChiName = "";
    String hMailAddr = "";
    String hMbosSupFlag = "";
    String hMbosNcccType = "";
    String hMbosAuthCreditLmt = "";
    String hMbosIndivCrdLmt = "";
    String hMbosRowid = "";
    String hMbosOldEndDate = "";
    String hMbosStatusCode = "";
    String hMbosReasonCode = "";
    String hMbosComboIndicator = "";
    String hMbosIcFlag = "";
    String hMbosBranch = "";
    String hMbosMailAttach1 = "";
    String hMbosMailAttach2 = "";
    String hMbosCsc = "";
    String hMbosVendor = "";
    String hMbosChkNcccFlag = "";
    String hMbosIcCvv = "";
    String hMbosSpecialCardRate = "";
    String hMbosSourceCode = "";
    String hMbosMailType = "";
    String hMbosMailBranch = "";
    String hMbosTscAutoloadFlag = "";
    String hCardIndivCrdLmt = "";
    String hUnitVirtualFlag = "";
    String hUnitIckind = "";
    String hCardPSeqno = "";
    String hMbosToNcccDate = "";
    String hMbosCrtBankNo = "";
    String hMbosVdBankNo = "";
    String hMbosCardRefNum = "";
    String hMbosSendPwdFlag = "";
    String hChiName = "";
    String hBirthday = "";
    String hMajorChiName = "";
    String hMajorBirthday = "";
    String hRowid = "";
    String hApscStatusCode = "";
    String hMailType = "";
    String hMailNo = "";
    String hMailBranch = "";
    String hMailProcDate = "";
    String hOppostDate = "";
    String hCurrentCode = "";
    String hApscCardNo = "";
    String hApscValidDate = "";
    String hApscStopDate = "";
    String hApscReissueDate = "";
    String hApscStopReason = "";
    String hApscMailType = "";
    String hApscMailNo = "";
    String hApscMailBranch = "";
    String hApscMailDate = "";
    String hApscPmId = "";
    String hApscPmIdCode = "";
    String hApscPmBirthday = "";
    String hApscSupId = "";
    String hApscSupIdCode = "";
    String hApscSupBirthday = "";
    String hApscCorpNo = "";
    String hApscCorpNoCode = "";
    String hApscCardType = "";
    String hApscPmName = "";
    String hApscSupName = "";
    String hApscSupLostStatus = "";
    String hApscGroupCode = "";
    String hIdnoIdPSeqno = "";
    String hIdnoResidentZip = "";
    String hIdnoMailZip = "";
    String hIdnoCompanyZip = "";
    String hEmpChiName = "";
    String hIdnoChiName = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoHomeTelExt1 = "";
    String hIdnoOfficeAreaCode1 = "";
    String hIdnoOfficeTelNo1 = "";
    String hIdnoOfficeTelExt1 = "";
    String hIdnoResidentAddr1 = "";
    String hIdnoMailAddr1 = "";
    String hIdnoCompanyAddr1 = "";
    String hIdnoResidentAddr2 = "";
    String hIdnoMailAddr2 = "";
    String hIdnoCompanyAddr2 = "";
    String hIdnoResidentAddr3 = "";
    String hIdnoMailAddr3 = "";
    String hIdnoCompanyAddr3 = "";
    String hIdnoResidentAddr4 = "";
    String hIdnoMailAddr4 = "";
    String hIdnoCompanyAddr4 = "";
    String hIdnoResidentAddr5 = "";
    String hIdnoMailAddr5 = "";
    String hIdnoCompanyAddr5 = "";
    String hIdnoCellarPhone = "";
    double hPtrcRcrateYear = 0.0;
    String hYy = "";
    String hIsssuDate = "";
    String hCardIssueDate = "";
    String hMbosNcccFilename = "";
    String hNcccTypeNo = "";
    String hTscCardNo = "";
    String hTscRowid = "";
    String hIpsCardNo = "";
    String hIpsRowid = "";
    String hIchCardNo = "";
    String hIchRowid = "";
    String cmdStr = "";
    String allName = "";
    String currDate = "";
    int tempSeq = 0;
    String tempSlip = "";
    String tempX10 = "";
    String tempX01 = "";
    String tempX02 = "";
    String tempX011 = "";
    String tmpConvert = "";
    String aftConvert = "";
    String data1000 = "";
    String ap1ReceiveType = "";
    String ap1ReceiveBranch = "";
    String ap1ReceiveZip = "";
    String ap1ReceiveAddr = "";
    int rtn;
    int errCode1 = 0;
    int visaCard = 0;
    int tempInt = 0;
    int totalFile = 0;
    int fi;
    String hBusiBusinessDate = "";
    String hSysDate = "";
    String tmpWfValue = "";
    String tmpMailType = "";
    String tmpBranch = "";

    BufferedWriter nccc = null;
    Buf1 ncccData = new Buf1();
    protected final String dt1Str = "col1,col2,col3,col4,col5,col6,col7,col8,col9,col10,col11,col12,"
    		+ "col13,col14,col15,col16,col17,col18,col19,col20,col21,col22,col23,col24,col25,col26," 
    		+ "col27,col28,col29,col30,col31,col32,col33,col34,col35,col36,col37,col38,col39,col40," 
    		+ "col41,col42,col43,col44,col45,col46,col47,col48,col49,col50,col51,col52,col53,col54," 
    		+ "col55,col56,col57,col58,col59,col60,col61,col62,col63,col64,col65,col66,col67,col68," 
    		+ "col69,col70,col71,col72,col73,col74,col75,col76,col77,col78,col79,col80,col81,col82," 
    		+ "col83,col84,col85,col86,col87,col88,col89,col90,col91,col92,col93,col94,col95,col96," 
    		+ "col97,col98,col99,col100,col101,col102,col103,col104,col105,col106,col107,col108,"  
    		+ "col109,col110,col111,col112,col113,col114,col115,receive_type,receive_branch,"
    		+ "receive_zip,receive_addr,col119";
        
    protected final int[] dt1Length = { 1, 7, 1, 13, 1, 10, 1, 2, 1, 37,
    		1, 2, 104, 1, 8, 20, 2, 8, 10, 8,
    		3, 3, 1, 8, 8, 60, 16, 16, 16, 16,
    		16, 16, 16, 16, 24, 24, 24, 24, 24, 24,
    		24, 24, 16, 1, 4, 8, 2, 2, 2, 2,
    		1, 4, 8, 2, 2, 2, 2, 1, 4, 8,
    		2, 2, 2, 2, 1, 4, 8, 2, 2, 2,
    		2, 1, 4, 8, 2, 2, 2, 2, 1, 4,
    		8, 2, 2, 2, 2, 1, 4, 8, 2, 2,
    		2, 2, 1, 4, 8, 2, 2, 2, 2, 1,
    		4, 8, 2, 2, 2, 2, 1, 4, 8, 2,
    		2, 2, 2, 33, 4, 1, 4, 5, 60, 26};
    
    Map<String, Object> map2 = new HashMap<>();
    protected  String[] dt1 = new String[] {};
    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
        	dt1 = dt1Str.split(",");
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            if (args.length > 1) {
                comc.errExit("Usage : " + javaProgram, "file_date[yyyymmdd]");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comr  = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comsecr = new CommSecr(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
//			if (args.length == 0) {
//				fileDate = hBusiBusinessDate;
//			} else if (args.length == 1) {
//				fileDate = args[0];
//			}
//			if (fileDate.length() != 8) {
//				comc.errExit("Usage : " + javaProgram, "file_date[yyyymmdd]");
//			}
                    	
        	if(getComboDebit()) {
                   	
                sqlCmd = "select ";
                sqlCmd += "distinct decode(a.nccc_type,'1',b.new_vendor,'2',b.chg_vendor,b.mku_vendor) as h_vendor ";
                sqlCmd += " from crd_emboss a, crd_item_unit b, ptr_group_card c ";
                sqlCmd += "where a.in_main_date   <> ''  ";
                sqlCmd += "  and a.to_nccc_date   <> ''  ";
                sqlCmd += "  and a.rtn_nccc_date  <> ''  ";
                sqlCmd += "  and a.to_vendor_date  = '29991231' ";
                sqlCmd += "  and a.reject_code     = ''  ";
                sqlCmd += "  and a.in_main_error   = '0' ";
                sqlCmd += "  and a.nccc_type       in ('1','3') ";
//                sqlCmd += "  and b.card_type       = a.card_type ";
                sqlCmd += "  and b.unit_code       = a.unit_code ";
                sqlCmd += "  and c.group_code      = a.group_code ";
                sqlCmd += "  and c.card_type       = a.card_type ";
                sqlCmd += "  and decode(c.card_mold_flag,'','O',c.card_mold_flag) != 'M'  ";
                sqlCmd += "  and a.combo_indicator <> 'N' ";
                sqlCmd += "order by h_vendor ";
//     if (DEBUG == 1) showLogMessage("D", "", " CMD=[" + sqlCmd + "] ");
                
                vendorCnt = selectTable();
                for (int i = 0; i < vendorCnt; i++) {
                    hVendor = getValue("h_vendor", i);
                    if (debug == 1)
                        showLogMessage("D", "", " VENDOR=[" + hVendor + "] ");
                
                    if (openTextFile() != 0) {
                        comcr.errRtn(errMsg, "open_text_file        error", comcr.hCallBatchSeqno);
                    }
                
                    process();
                    
                    nccc.close();
                
                    if (hRecCnt1 <= 0) {
                        cmdStr = String.format("rm -i -f %s", allName);
                        if (comc.fileDelete(allName) == false) {
                            showLogMessage("I", "", "ERROR : mv 檔案=" + cmdStr);
                        }
                    }
                    else {
                		commFTP = new CommFTP(getDBconnect(), getDBalias());
                	    comr = new CommRoutine(getDBconnect(), getDBalias());
                	    procFTP();
                	    renameFile1(hNcccFilename);
                    }                
                }
        	}

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "][" + hRecCnt1 +
            		"]金融卡檔[" + totalCnt2 + "]信用卡+金融卡檔[" + hRecCnt2 + "]";
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
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		hSysDate = "";

		sqlCmd = " select business_date, ";
		sqlCmd += " to_char(sysdate,'yyyymmdd') as sysdate ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
		hSysDate = getValue("sysdate");
	}

	/************************************************************************/
    int openTextFile() throws Exception {
        int tmpVendor;

        tmpVendor = comcr.str2int(hVendor);
        
        checkFileCtl();
         
        hNcccFilename = String.format("cb_%02d_makecard_ns_%s%02d.txt", tmpVendor, hSysDate, hNn);

        allName = String.format("%s/media/crd/%s", comc.getECSHOME(), hNcccFilename);
        allName = Normalizer.normalize(allName, java.text.Normalizer.Form.NFKD);
        if (debug == 1)
            showLogMessage("D", "", " OPEN File=[" + allName + "] ");

        nccc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allName), "MS950"));

        return (0);
    }

    /***********************************************************************/
    void checkFileCtl() throws Exception {
        int tmpVendor;
        tmpVendor = comcr.str2int(hVendor);
    	String likeFilename = "";
    	String hFileName = "";
    	likeFilename = String.format("cb_%02d_makecard_ns_%s", tmpVendor, hSysDate)+"%";
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
        }else {
            hFileName = getValue("file_name");
            hNn = Integer.valueOf(hFileName.substring(26, 28))+1;
        }

    }
    /***********************************************************************/
    boolean getComboDebit() throws Exception 
    {
        String hTempFilename = "";
        String tmpstr = String.format("%s/media/crd", comc.getECSHOME());

        tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);

        List<String> listOfFiles = comc.listFS(tmpstr, "", "");

if(debugD == 1) showLogMessage("I", "", " 888 PATH=["+tmpstr+"]"+ listOfFiles.size());
        if (listOfFiles.size() == 0) {
        	showLogMessage("I", "", "無檔案可處理");
        	return false;
        }

        for (String file : listOfFiles) {
if(debugD == 1) showLogMessage("I", "", " 888 NAME=["+file+"]"+ file.length());
                if (file.length() != 33)    continue; 
                if (!file.substring(0,19).equals("combo_debit_mkc_ns_"))
                    continue;
//                if (!file.substring(19,27).equals(String.valueOf(fileDate)))
//                    continue;
                hTempFilename = file;
                if (getFileName(hTempFilename) == 1)
                	continue;
if(debugD == 1) showLogMessage("I", "", " 888 read file=["+totalFile+"]"+ hTempFilename);

                if (totalCnt2 > 0) {
                    comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt2 + "]";
                    showLogMessage("I", "", hCallErrorDesc);
                }
                commitDataBase();

        }

        return true;
    }
    /**********************************************************************/
    public void renameFile(String removeFileName) throws Exception {
        String tmpstr1 = checkHome + "/media/crd/" + removeFileName;
        File oldName = new File(tmpstr1);
        String tmpstr2 = checkHome + "/media/crd/backup/" + removeFileName;
        File newName = new File(tmpstr2);

        if (!oldName.renameTo(newName)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }    
    /**********************************************************************/
    public int getFileName(String fileNameI) throws Exception {
        String rec = "";
        checkHome = comc.getECSHOME();
        fileName1 = fileNameI;
        fileName2 = checkHome + "/media/crd/" + fileName1;
if(debug == 1) showLogMessage("I", "", "  888 Process file=" + fileName2);

        fi = openInputText(fileName2, "MS950");
        setConsoleMode("Y");
        if (fi == -1) {
            return 1;
        }

        showLogMessage("I", "", "888 Process file=" + fileName2);

//        tmp_int = select_crd_file_ctl();
//        if (tmp_int > 0)
//            return 1;

        totalFile++;

        String tmpStr = "";
        totalCnt2      = 0;
        
        while (true) {
            rec = readTextFile(fi);
            if(rec.length()==0) break;
            byte[] bt = rec.getBytes("MS950");
            
            totalCnt2++;

            moveData(processDataRecord(getFieldValue(rec, dt1Length), dt1));
            processDisplay(1000);
        }
        closeInputText(fi);
        
        insertFileCtl1(fileName1);
        
        renameFile(fileName1);
        return 0;
    }
    // ************************************************************************
    public String[] getFieldValue(String rec, int[] parm) {
        int x = 0;
        int y = 0;
        byte[] bt = null;
        String[] ss = new String[parm.length];
        try {
            bt = rec.getBytes("MS950");
        } catch (Exception e) {
            showLogMessage("I", "", comc.getStackTraceString(e));
        }
        for (int i : parm) {
            try {
                ss[y] = new String(bt, x, i, "MS950");
            } catch (Exception e) {
                showLogMessage("I", "", comc.getStackTraceString(e));
            }
            y++;
            x = x + i;
        }

        return ss;
    }
    // ************************************************************************
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            // if(DEBUG == 1) showLogMessage("D",""," Data=" + s + ":[" + row[i]
            // + "]");
            i++;
        }
        return map;
        
    }
    // ************************************************************************
    private int moveData(Map<String, Object> map) throws Exception {
        dateTime();         
        data1000 = "";

        hDebtActNo = (String) map.get("col4");
        hDebtActNo = hDebtActNo.trim();

        data1000 = (String) map.get("col1")+(String) map.get("col2")+(String) map.get("col3")+
        (String) map.get("col4")+(String) map.get("col5")+(String) map.get("col6")+
        (String) map.get("col7")+(String) map.get("col8")+(String) map.get("col9")+
        (String) map.get("col10")+(String) map.get("col11")+(String) map.get("col12")+
        (String) map.get("col13")+(String) map.get("col14")+(String) map.get("col15")+
        (String) map.get("col16")+(String) map.get("col17")+(String) map.get("col18")+
        (String) map.get("col19")+(String) map.get("col20")+(String) map.get("col21")+
        (String) map.get("col22")+(String) map.get("col23")+(String) map.get("col24")+
        (String) map.get("col25")+(String) map.get("col26")+(String) map.get("col27")+
        (String) map.get("col28")+(String) map.get("col29")+(String) map.get("col30")+
        (String) map.get("col31")+(String) map.get("col32")+(String) map.get("col33")+
        (String) map.get("col34")+(String) map.get("col35")+(String) map.get("col36")+
        (String) map.get("col37")+(String) map.get("col38")+(String) map.get("col39")+
        (String) map.get("col40")+(String) map.get("col41")+(String) map.get("col42")+
        (String) map.get("col43")+(String) map.get("col44")+(String) map.get("col45")+
        (String) map.get("col46")+(String) map.get("col47")+(String) map.get("col48")+
        (String) map.get("col49")+(String) map.get("col50")+(String) map.get("col51")+
        (String) map.get("col52")+(String) map.get("col53")+(String) map.get("col54")+
        (String) map.get("col55")+(String) map.get("col56")+(String) map.get("col57")+
        (String) map.get("col58")+(String) map.get("col59")+(String) map.get("col60")+
        (String) map.get("col61")+(String) map.get("col62")+(String) map.get("col63")+
        (String) map.get("col64")+(String) map.get("col65")+(String) map.get("col66")+
        (String) map.get("col67")+(String) map.get("col68")+(String) map.get("col69")+
        (String) map.get("col70")+(String) map.get("col71")+(String) map.get("col72")+
        (String) map.get("col73")+(String) map.get("col74")+(String) map.get("col75")+
        (String) map.get("col76")+(String) map.get("col77")+(String) map.get("col78")+
        (String) map.get("col79")+(String) map.get("col80")+(String) map.get("col81")+
        (String) map.get("col82")+(String) map.get("col83")+(String) map.get("col84")+
        (String) map.get("col85")+(String) map.get("col86")+(String) map.get("col87")+
        (String) map.get("col88")+(String) map.get("col89")+(String) map.get("col90")+
        (String) map.get("col91")+(String) map.get("col92")+(String) map.get("col93")+
        (String) map.get("col94")+(String) map.get("col95")+(String) map.get("col96")+
        (String) map.get("col97")+(String) map.get("col98")+(String) map.get("col99")+
        (String) map.get("col100")+(String) map.get("col101")+(String) map.get("col102")+
        (String) map.get("col103")+(String) map.get("col104")+(String) map.get("col105")+
        (String) map.get("col106")+(String) map.get("col107")+(String) map.get("col108")+
        (String) map.get("col109")+(String) map.get("col110")+(String) map.get("col111")+
        (String) map.get("col112")+(String) map.get("col113")+(String) map.get("col114")+
        (String) map.get("col115")+(String) map.get("receive_type")+(String) map.get("receive_branch")+
        (String) map.get("receive_zip")+(String) map.get("receive_addr")+(String) map.get("col119");
        map2.put(hDebtActNo, data1000);

        return (0);
    }
    // ************************************************************************
    public int selectCrdFileCtl() throws Exception {
        selectSQL = "count(*) as all_cnt";
        daoTable = "crd_file_ctl";
        whereStr = "WHERE file_name  = ? ";

        setString(1, fileName1);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + fileName1 + "]");
            return 1;
        }

        return 0;
    }
    /***********************************************************************/
    void process() throws Exception {
        String prevAcctType = "";
        rtn = 0;
        int chkFlag = 0;
        int errFlag = 0;
        int foundAe;

        hRecCnt1 = 0;
        sqlCmd = "select ";
        sqlCmd += "a.batchno,";
        sqlCmd += "a.recno,";
        sqlCmd += "a.emboss_source,";
        sqlCmd += "a.emboss_reason,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.to_nccc_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "decode(a.group_code,'','0000',a.group_code) as group_code,";
        sqlCmd += "a.bin_no,";
        sqlCmd += "a.electronic_code,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.apply_id,";
        sqlCmd += "a.apply_id_code,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.mail_zip,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.nation,";
        sqlCmd += "a.business_code,";
        sqlCmd += "decode(a.education,'','6',a.education) as h_mbos_education,";
        sqlCmd += "a.act_no,";
        sqlCmd += "a.home_area_code1,";
        sqlCmd += "a.home_tel_no1,";
        sqlCmd += "a.org_emboss_data,";
        sqlCmd += "a.emboss_4th_data,";
        sqlCmd += "a.member_id,";
        sqlCmd += "a.pm_id,";
        sqlCmd += "a.pm_id_code,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "a.corp_no_code,";
        sqlCmd += "a.reg_bank_no,";
        sqlCmd += "a.force_flag,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "decode(a.marriage,'','2',a.marriage) as h_mbos_marriage,";
        sqlCmd += "a.rel_with_pm,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "a.sex,";
        sqlCmd += "a.pvv,";
        sqlCmd += "a.cvv,";
        sqlCmd += "a.pvki,";
        sqlCmd += "a.trans_cvv2,";
        sqlCmd += "a.open_passwd,";
        sqlCmd += "a.old_card_no,";
        sqlCmd += "a.chi_name,";
        sqlCmd += "rtrim(a.mail_addr1)||rtrim(a.mail_addr2)||rtrim(a.mail_addr3)||rtrim(a.mail_addr4)||rtrim(a.mail_addr5) h_mail_addr,";
        sqlCmd += "a.service_code,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.nccc_type,";
        sqlCmd += "a.auth_credit_lmt,";
        sqlCmd += "a.old_end_date,";
        sqlCmd += "a.status_code,";
        sqlCmd += "a.reason_code,";
        sqlCmd += "decode(a.combo_indicator,'','N',a.combo_indicator) as h_mbos_combo_indicator,";
        sqlCmd += "a.ic_flag,";
        sqlCmd += "a.branch,";
        sqlCmd += "a.mail_attach1,";
        sqlCmd += "a.mail_attach2,";
        sqlCmd += "a.ic_cvv, ";
        sqlCmd += "a.csc,";
        sqlCmd += "decode(a.nccc_type,'1',b.new_vendor,'2',b.chg_vendor,b.mku_vendor)  as vendor ,";
        sqlCmd += "decode(c.chk_nccc_flag,'' ,'N',c.chk_nccc_flag)  as h_mbos_chk_nccc_flag,";
        sqlCmd += "a.rowid  as rowid,  ";
        sqlCmd += "a.special_card_rate,  ";
        sqlCmd += "a.card_ref_num,  ";
        sqlCmd += "a.send_pwd_flag,  ";
        sqlCmd += "a.source_code,  ";
        sqlCmd += "a.mail_type,  ";
        sqlCmd += "a.mail_branch,  ";
        sqlCmd += "a.to_nccc_date,  ";
        sqlCmd += "d.indiv_crd_lmt, ";
        sqlCmd += "b.virtual_flag, ";
        sqlCmd += "b.ic_kind, ";
        sqlCmd += "d.p_seqno, ";
        sqlCmd += "a.tsc_autoload_flag ";
        sqlCmd += " from crd_emboss a, crd_item_unit b, ptr_group_card c,crd_card d ";
        sqlCmd += "where a.in_main_date   <> ''  ";
        sqlCmd += "  and a.nccc_type       in ('1','3') ";
        sqlCmd += "  and a.to_nccc_date   <> ''  ";
        sqlCmd += "  and a.rtn_nccc_date  <> ''  ";
        sqlCmd += "  and a.to_vendor_date  = '29991231' ";
        sqlCmd += "  and a.reject_code     = ''  ";
        sqlCmd += "  and a.in_main_error   = '0' ";
        sqlCmd += "  and a.card_no         = d.card_no   ";
//        sqlCmd += "  and b.card_type       = a.card_type ";
        sqlCmd += "  and b.unit_code       = a.unit_code ";
        sqlCmd += "  and c.group_code      = a.group_code ";
        sqlCmd += "  and c.card_type       = a.card_type ";
        sqlCmd += "  and a.combo_indicator <> 'N' ";
        sqlCmd += "  and decode(c.card_mold_flag,'','O',c.card_mold_flag) != 'M'  ";
        sqlCmd += "order by vendor,a.acct_type,a.card_type,a.unit_code,a.sup_flag ";
        /*
         * test sqlCmd += "fetch first 1 rows only ";
         */

        embossCnt = selectTable();

        for (int i = 0; i < embossCnt; i++) {
            hMbosBatchno = getValue("batchno", i);
            hMbosRecno = getValueDouble("recno", i);
            hMbosEmbossSource = getValue("emboss_source", i);
            hMbosEmbossReason = getValue("emboss_reason", i);
            hMbosAcctType = getValue("acct_type", i);
            hMbosAcctKey = getValue("acct_key", i);
            hMbosToNcccCode = getValue("to_nccc_code", i);
            hMbosCardType = getValue("card_type", i);
            hMbosGroupCode = getValue("group_code", i);
            hMbosBinNo = getValue("bin_no", i);
            hMbosElectronicCode = getValue("electronic_code", i);
            hMbosCardNo = getValue("card_no", i);
            hMbosMajorCardNo = getValue("major_card_no", i);
            hMbosApplyId = getValue("apply_id", i);
            hMbosApplyIdCode = getValue("apply_id_code", i);
            hMbosValidFm = getValue("valid_fm", i);
            hMbosValidTo = getValue("valid_to", i);
            hMbosMailZip = getValue("mail_zip", i);
            hMbosBirthday = getValue("birthday", i);
            hMbosNation = getValue("nation", i);
            hMbosBusinessCode = getValue("business_code", i);
            hMbosEducation = getValue("h_mbos_education", i);
            hMbosActNo = getValue("act_no", i);
            hMbosHomeTelNo1 = getValue("home_tel_no1", i);
            hMbosHomeAreaCode1 = getValue("home_area_code1", i);
            hMbosOrgEmbossData = getValue("org_emboss_data", i);
            hMbosEmboss4ThData = getValue("emboss_4th_data", i);
            hMbosMemberId = getValue("member_id", i);
            hMbosPmId = getValue("pm_id", i);
            hMbosPmIdCode = getValue("pm_id_code", i);
            hMbosCorpNo = getValue("corp_no", i);
            hMbosCorpNoCode = getValue("corp_no_code", i);
            hMbosRegBankNo = getValue("reg_bank_no", i);
            hMbosForceFlag = getValue("force_flag", i);
            hMbosEngName = getValue("eng_name", i);
            hMbosMarriage = getValue("h_mbos_marriage", i);
            hMbosRelWithPm = getValue("rel_with_pm", i);
            hMbosUnitCode = getValue("unit_code", i);
            hMbosSex = getValue("sex", i);
            hMbosPvv = getValue("pvv", i);
            hMbosCvv = getValue("cvv", i);
            hMbosPvki = getValue("pvki", i);
            hMbosCvv2 = getValue("trans_cvv2", i);
            hMbosOpenNum = getValue("open_passwd", i);
            hMbosOldCardNo = getValue("old_card_no", i);
            hMbosChiName = getValue("chi_name", i);
            hMailAddr = getValue("h_mail_addr", i);
            hMbosServiceCode = getValue("service_code", i);
            hMbosSupFlag = getValue("sup_flag", i);
            hMbosNcccType = getValue("nccc_type", i);
            hMbosAuthCreditLmt = getValue("auth_credit_lmt", i);
            hMbosIndivCrdLmt = getValue("indiv_crd_lmt", i);
            hMbosOldEndDate = getValue("old_end_date", i);
            hMbosStatusCode = getValue("status_code", i);
            hMbosReasonCode = getValue("reason_code", i);
            hMbosComboIndicator = getValue("h_mbos_combo_indicator", i);
            hMbosIcFlag = getValue("ic_flag", i);
            hMbosBranch = getValue("branch", i);
            hMbosMailAttach1 = getValue("mail_attach1", i);
            hMbosMailAttach2 = getValue("mail_attach2", i);
            hMbosCsc = getValue("csc", i);
            hMbosVendor = getValue("vendor", i);
            hMbosChkNcccFlag = getValue("h_mbos_chk_nccc_flag", i);
            hMbosIcCvv = getValue("ic_cvv", i);
            hMbosRowid = getValue("rowid", i);
            hMbosSpecialCardRate = getValue("special_card_rate", i);
            hMbosCardRefNum = getValue("card_ref_num", i);
            hMbosSendPwdFlag = getValue("send_pwd_flag", i);
            hMbosSourceCode = getValue("source_code", i);
            hMbosMailType = getValue("mail_type", i);
            hMbosMailBranch = getValue("mail_branch", i);
            hMbosToNcccDate = getValue("to_nccc_date", i);
            hCardIndivCrdLmt = getValue("indiv_crd_lmt", i);
            hUnitVirtualFlag = getValue("virtual_flag", i);          
            hUnitIckind = getValue("ic_kind", i);
            hCardPSeqno = getValue("p_seqno", i);
            hMbosTscAutoloadFlag = getValue("tsc_autoload_flag", i);
            
            totalCnt++;
if(debug == 1) {
   showLogMessage("D", "", " 888 read card=["+ hMbosCardNo + "]"+totalCnt+","+ hMbosApplyId);
   showLogMessage("D", "", "      group  =[" + hMbosGroupCode + "] " + hMbosCardType);
   showLogMessage("D", "", "      vendor =[" + hMbosVendor + "]"+ hVendor +","+ hMbosBatchno);
  }

            if (!hMbosVendor.equals(hVendor))
                continue;

            visaCard = 0;
            hBinNo = "";
            hBinType = "";

            selPtrBintable();
            hServiceCode = hMbosServiceCode;

            if (hBinType.equals("V"))
                visaCard = 1;

            if (!prevAcctType.equals(hMbosAcctType)) {
                getCardIndicator(hMbosAcctType);
                prevAcctType = hMbosAcctType;
            }

            foundAe = 0;

            if ((hMbosCardNo.length() == 15) && (hMbosCardNo.substring(0, 1).equals("3")))
                foundAe = 1;

            errFlag = 0;
            pKeyType = "";
            pDerivKey = "";
            pIcIndicator = "";
            pServiceCode = "";
            pLOffLnLmt = 0;
            pUOffLnLmt = 0;

//            if (hMbosIcFlag.equals("Y")) {
//                errFlag = getIccardData();
//                if (errFlag != 0) {
//                    updateDiffEmboss();
//                    continue;
//                }
//            }
            /******************************************************
             * 不為combo卡才有凸字第四行
             ******************************************************/
            if (hMbosComboIndicator.compareTo("N") == 0) {
            	switch (comcr.str2int(hCardIndicator)) {
            	case 1:
            		getEmbossData();
            		break;
                case 2:
                    getBusEmbossData();
                    break;
                }
            }

            chkFlag = 0;

            /********** 不送製卡資料,只產生凸字第四行 ********************/
            if (hMbosToNcccCode.equals("N")) {
                hMbosNcccFilename = "";
                if (hMbosEmbossSource.equals("5")) {
                    if (hMbosEmbossReason.equals("1") || hMbosEmbossReason.equals("3")) {
                        chkFlag = 1;
                    }
                }
                if (hMbosEmbossSource.substring(0, 1).equals("7")) {
                    chkFlag = 1;
                }
                if (chkFlag == 1) {
                    processApscard();
                }
                // ** 不做成製卡格式 ***
                updateCrdEmboss();
                continue;
            }
            
            //20221209虛擬卡不產生製卡檔
            if(hUnitVirtualFlag.equals("Y")) {
            	updateCrdEmboss();
                continue;
            }

            // **** 無PVV,CVV不可產生資料,不包括不送製卡 ********************
            if (hMbosToNcccCode.equals("Y")) {
                if (foundAe == 1) {
                    if ((hMbosCsc.length() <= 0) || (hMbosCvv2.length() <= 0) || (hMbosPvki.length() <= 0)) {
                        continue;
                    }
                } else {
                    if ((hMbosPvv.length() <= 0) || (hMbosCvv.length() <= 0) || (hMbosCvv2.length() <= 0) || (hMbosPvki.length() <= 0)) {
                        continue;
                    }
                }
            }

            switch (comcr.str2int(hMbosEmbossSource)) {
            case 1:
            case 2:
            case 3:
            case 4:
                createNcccNewFile();
                break;
            case 5:
                createNcccNewFile();
                if (rtn == 0) {
                    switch (comcr.str2int(hMbosEmbossReason)) {
                    case 1:
                        processApscard();
                        break;
                    case 3:
                        processApscard();
                        break;
                    }
                }
                break;
            }

            /******************************************
             * combo卡抓取到正卡第三軌資料才製卡卡片
             ******************************************/
            if (rtn == 0) {
            	updateCrdEmboss();
            	updateCrdCombo();
            	
            	if (!hMbosElectronicCode.equals("00")) {
            		if(tmpWfValue.equals("Y")) {
            			if (hMbosElectronicCode.equals("01")) {
            				updateTscCdrpLog();
            			}            			 
            			if (hMbosElectronicCode.equals("02")) {
            				updateIpsCdrpLog();
            			}
            			if (hMbosElectronicCode.equals("03")) {
            				updateIchB07bCard();
            			}           			 
            		}
            	}
            	
                hRecCnt1++;
            }
        }
        if (hRecCnt1 > 0)
            insertFileCtl();
    }

    // ************************************************************************
    public int selPtrBintable() throws Exception {
        selectSQL = " bin_type,ica_no ";
        daoTable  = "ptr_bintable ";
        whereStr  = "WHERE bin_no   = ? " 
                  + "FETCH first 1 rows only    ";
        setString(1, hMbosBinNo);

        selectTable();

        if(notFound.equals("Y")) {
           comcr.errRtn("select_ptr_bintable  error ", hMbosBinNo,comcr.hCallBatchSeqno);
        }
        hBinNo = hMbosBinNo;
        hBinType = getValue("bin_type");
        hIcaNo = getValue("ica_no");

        selectSQL = " a.card_note      ";
        daoTable  = " ptr_card_type a  ";
        whereStr  = "WHERE a.card_type  =  ? ";
        setString(1, hMbosCardType);

        selectTable();

        if(notFound.equals("Y")) {
           comcr.errRtn("select_ptr_card_type error ", hMbosCardType,comcr.hCallBatchSeqno);
        }
        hCardNote = getValue("card_note");

        if (debug == 1) showLogMessage("D", "", " 888 select note =[" + hCardNote + "] ");

        return (0);
    }

    /***********************************************************************/
    void getCardIndicator(String acctType) throws Exception {
        String gAcctType = "";

        gAcctType = "";
        hCardIndicator = "";
        gAcctType = acctType;
        sqlCmd = "select card_indicator ";
        sqlCmd += " from ptr_acct_type  ";
        sqlCmd += "where acct_type = ? ";
        setString(1, gAcctType);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hCardIndicator = getValue("card_indicator");
        }
    }

    /***********************************************************************/
//    int getIccardData() throws Exception {
//        pServiceCode = "";
//        pDerivKey = "";
//        pLOffLnLmt = 0;
//        pUOffLnLmt = 0;
//        pCheckKeyExpire = "";
//        pKeyType = "";
//        pIcIndicator = "";
//        pExpireDate = "";
//        
//        selectSQL = "  a.service_code       " + ", a.deriv_key          " 
//                  + ", a.l_offln_lmt        " + ", a.u_offln_lmt        " 
//                  + ", a.check_key_expire   " + ", b.key_type           "
//                  + ", b.ic_indicator       " + ", b.expire_date        ";
//        daoTable  = " crd_item_unit a, ptr_ickey b ";
//        whereStr  = "WHERE a.card_type   = ? " 
//                  + "  and a.unit_code   = ? " 
//                  + "  and b.key_type    = ? " // bin_type
//                  + "  and b.key_id      = a.key_id   ";
//        setString(1, hMbosCardType);
//        setString(2, hMbosUnitCode);
//        setString(3, hBinType);
//
//        tmpInt = selectTable();
//        if (tmpInt > 0) {
//            pServiceCode = getValue("service_code");
//            pKeyType = getValue("key_type");
//            pDerivKey = getValue("deriv_key");
//            pIcIndicator = getValue("ic_indicator");
//            pLOffLnLmt = getValueInt("l_offln_lmt");
//            pUOffLnLmt = getValueInt("u_offln_lmt");
//            pExpireDate = getValue("expire_date");
//            pCheckKeyExpire = getValue("check_key_expire");
//        }
//
//        if (pCheckKeyExpire.equals("Y") && pExpireDate.compareTo(hMbosValidTo) < 0) {
//            return (1);
//        }
//
//        return (0);
//    }
    /***********************************************************************/
    void updateDiffEmboss() throws Exception {
        daoTable = "crd_emboss";
        updateSQL = " diff_code    = 'Y',";
        updateSQL += " ic_indicator = ?,";
        updateSQL += " key_type     = ?,";
        updateSQL += " deriv_key    = ?,";
        updateSQL += " l_offln_lmt  = ?,";
        updateSQL += " u_offln_lmt  = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr = "where rowid   = ? ";
        setString(1, pIcIndicator);
        setString(2, pKeyType);
        setString(3, pDerivKey);
        setInt(4, pLOffLnLmt);
        setInt(5, pUOffLnLmt);
        setString(6, javaProgram);
        setRowId(7, hMbosRowid);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void getEmbossData() throws Exception {
        String hEmbossData = "";
        String hCoMemberFlag = "";
        String tmpData = "";

        if (hMbosEmbossSource.compareTo("2") > 0) {
            return;
        }

        hEmbossData = "";
        hCoMemberFlag = "";
        sqlCmd = "select emboss_data,";
        sqlCmd += " co_member_flag ";
        sqlCmd += " from ptr_group_code  ";
        sqlCmd += " where group_code = decode(cast(? as varchar(4)),'','0000',?) ";
        setString(1, hMbosGroupCode);
        setString(2, hMbosGroupCode);
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            hEmbossData = getValue("emboss_data");
            hCoMemberFlag = getValue("co_member_flag");
        } else {
        }
        if (hEmbossData.length() > 0) {
            tmpData = hEmbossData;
        } else {
            if (hMbosOrgEmbossData.length() > 0) {
                tmpData = hMbosOrgEmbossData;
            }
        }

/* mark no use
        if (h_co_member_flag.substring(0, 1).equals("Y")) {
            if (h_temp_member_id.length() > 0) {
                tmp_data = h_temp_member_id;
                stderr = String.format("%s", h_temp_member_id);
            }
        }
*/
        hMbosEmboss4ThData = tmpData;

        return;
    }

    /***********************************************************************/
    void getBusEmbossData() throws Exception {
        String pEmbossData = "";

        sqlCmd = "select emboss_data ";
        sqlCmd += "  from crd_corp  ";
        sqlCmd += " where corp_no      = ?  ";
        setString(1, hMbosCorpNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            pEmbossData = getValue("emboss_data");
        }
        if (pEmbossData.length() > 0) {
            hMbosEmboss4ThData = pEmbossData;
        }

        return;
    }

    /***********************************************************************/
    void processApscard() throws Exception {
        String hRowid = "";

        if (hMbosEmbossSource.equals("7")) {
            hApscStatusCode = "4";
        }

        if (hMbosEmbossSource.equals("5")) {
            switch (comcr.str2int(hMbosEmbossReason)) {
            case 1:
                hApscStatusCode = "4";
                break;
            case 3:
                hApscStatusCode = "3";
                break;
            }
        }

        sqlCmd = "select rowid   as rowid";
        sqlCmd += " from crd_apscard  ";
        sqlCmd += "where card_no     = ?  ";
        sqlCmd += "  and to_aps_date = ''  ";
        setString(1, hMbosCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hRowid = getValue("rowid");

            daoTable = "crd_apscard";
            updateSQL = " status_code = ?,";
            updateSQL += " mod_time    = sysdate,";
            updateSQL += " mod_pgm     = ?";
            whereStr = "where rowid  = ? ";
            setString(1, hApscStatusCode);
            setString(2, javaProgram);
            setRowId(3, hRowid);
            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_apscard not found!", hMbosCardNo, comcr.hCallBatchSeqno);
            }
        } else {
            insertApscard();
        }
    }

    /***********************************************************************/
    void insertApscard() throws Exception {
        String hMailType = "";
        String hMailNo = "";
        String hMailBranch = "";
        String hMailProcDate = "";
        String hOppostDate = "";
        String hCurrentCode = "";

        getApplyData();
        if (!hMbosApplyId.equals(hMbosPmId)) {
            hApscSupId = hMbosApplyId;
            hApscSupIdCode = hMbosApplyIdCode;
            hApscSupBirthday = hBirthday;
            hApscSupName = hChiName;
            getPmData();
            hApscPmId = hMbosPmId;
            hApscPmIdCode = hMbosPmIdCode;
            hApscPmBirthday = hMajorBirthday;
            hApscPmName = hMajorChiName;
        } else {
            hApscPmId = hMbosPmId;
            hApscPmIdCode = hMbosPmIdCode;
            hApscPmName = hChiName;
            hApscPmBirthday = hBirthday;
        }
        hApscCardNo = hMbosOldCardNo;
        hApscValidDate = hMbosOldEndDate;
        hApscCorpNo = hMbosCorpNo;
        hApscCorpNoCode = hMbosCorpNoCode;
        hApscCardType = hMbosCardType;
        hApscGroupCode = hMbosGroupCode;

        /******* 送舊卡號之郵記相關資料 ************/
        hMailType = "";
        hMailNo = "";
        hMailBranch = "";
        hMailProcDate = "";
        hOppostDate = "";
        hCurrentCode = "";
        sqlCmd = "select mail_type,";
        sqlCmd += "mail_no,";
        sqlCmd += "mail_branch,";
        sqlCmd += "mail_proc_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "current_code ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hMbosOldCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hMailType = getValue("mail_type");
            hMailNo = getValue("mail_no");
            hMailBranch = getValue("mail_branch");
            hMailProcDate = getValue("mail_proc_date");
            hOppostDate = getValue("oppost_date");
            hCurrentCode = getValue("current_code");
        }

        hApscReissueDate = currDate;
        hApscMailType = hMailType;
        hApscMailBranch = hMailBranch;
        hApscMailNo = hMailNo;
        hApscMailDate = hMailProcDate;
        hApscStopDate = hOppostDate;
        switch (comcr.str2int(hCurrentCode)) {
        case 1:
            hApscStopReason = "3";
            break;
        case 2:
            hApscStopReason = "2";
            break;
        case 3:
            hApscStopReason = "1";
            break;
        case 4:
            hApscStopReason = "3";
            break;
        case 5:
            hApscStopReason = "5";
            break;
        }

        if (hMbosSupFlag.equals("1")) {
            hApscSupLostStatus = "0";
        }

        setValue("crt_datetime", sysDate + sysTime);
        setValue("card_no", hApscCardNo);
        setValue("valid_date", hApscValidDate);
        setValue("stop_date", hApscStopDate);
        setValue("reissue_date", hApscReissueDate);
        setValue("stop_reason", hApscStopReason);
        setValue("mail_type", hApscMailType);
        setValue("mail_no", hApscMailNo);
        setValue("mail_branch", hApscMailBranch);
        setValue("mail_date", hApscMailDate);
        setValue("pm_id", hApscPmId);
        setValue("pm_id_code", hApscPmIdCode);
        setValue("pm_birthday", hApscPmBirthday);
        setValue("sup_id", hApscSupId);
        setValue("sup_id_code", hApscSupIdCode);
        setValue("sup_birthday", hApscSupBirthday);
        setValue("corp_no", hApscCorpNo);
        setValue("corp_no_code", hApscCorpNoCode);
        setValue("card_type", hApscCardType);
        setValue("pm_name", hApscPmName);
        setValue("sup_name", hApscSupName);
        setValue("sup_lost_status", hApscSupLostStatus);
        setValue("status_code", hApscStatusCode);
        setValue("group_code", hApscGroupCode);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "crd_apscard";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_apscard duplicate!", hApscCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void getApplyData() throws Exception {

        hChiName = "";
        hBirthday = "";
        sqlCmd = "select chi_name,";
        sqlCmd += "      birthday ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ?  ";
        setString(1, hMbosApplyId);
        setString(2, hMbosApplyIdCode);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hChiName = getValue("chi_name");
            hBirthday = getValue("birthday");
        }
    }

    /************************************************************************/
    void getPmData() throws Exception {
        hMajorChiName = "";
        hMajorBirthday = "";
        sqlCmd = "select chi_name,";
        sqlCmd += "      birthday ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ?  ";
        setString(1, hMbosPmId);
        setString(2, hMbosPmIdCode);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hMajorChiName = getValue("chi_name");
            hMajorBirthday = getValue("birthday");
        }
    }
    
/***********************************************************************/
void updateCrdEmboss() throws Exception 
{
        int type = 0;

        if (hMbosToNcccCode.equals("Y"))
            hMbosNcccFilename = hNcccFilename;

        if (!hMbosEmbossSource.equals("5"))
            type = 1;

        if (hMbosEmbossSource.equals("5")) {
            if (hMbosEmbossReason.substring(0, 1).equals("2"))
                type = 1;
            else
                type = 0;
        }
if(debug == 1) showLogMessage("D", "", " 888 update type=["+type+"] ");
        if (type == 0) {
            daoTable   = "crd_emboss";
            updateSQL  = " to_vendor_date  = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " nccc_filename   = ?,";
            updateSQL += " emboss_4th_data = ?,";
            updateSQL += " major_card_no   = ?,";
            updateSQL += " ic_indicator    = ?,";
            updateSQL += " key_type        = ?,";
            updateSQL += " deriv_key       = ?,";
            updateSQL += " l_offln_lmt     = ?,";
            updateSQL += " u_offln_lmt     = ?,";
            updateSQL += " pvv    = '',";
            updateSQL += " cvv    = '',";
            updateSQL += " csc    = '',";
            updateSQL += " ic_cvv = '',";
            updateSQL += " diff_code       = '',";
            updateSQL += " vendor          = ?,";
            updateSQL += " mail_type = ?,";
            updateSQL += " mail_branch = ?,";
            updateSQL += " mod_time        = sysdate,";
            updateSQL += " mod_pgm         = ? ";
            whereStr = " where rowid     = ? ";
            setString(1, hMbosNcccFilename);
            setString(2, hMbosEmboss4ThData);
            setString(3, hMbosMajorCardNo);
            setString(4, pIcIndicator);
            setString(5, pKeyType);
            setString(6, pDerivKey);
            setInt(7, pLOffLnLmt);
            setInt(8, pUOffLnLmt);
            setString(9, hMbosVendor);
            setString(10, tmpMailType);
            setString(11, tmpBranch);
            setString(12, javaProgram);
            setRowId(13, hMbosRowid);
            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_emboss not found!", hMbosRowid, comcr.hCallBatchSeqno);
            }
        } else {
            daoTable   = "crd_emboss";
            updateSQL  = "to_vendor_date   = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " nccc_filename   = ?,";
            updateSQL += " emboss_4th_data = ?,";
            updateSQL += " ic_indicator    = ?,";
            updateSQL += " key_type        = ?,";
            updateSQL += " deriv_key       = ?,";
            updateSQL += " l_offln_lmt     = ?,";
            updateSQL += " u_offln_lmt     = ?,";
            updateSQL += " pvv       = '',";
            updateSQL += " cvv       = '',";
            updateSQL += " csc       = '',";
            updateSQL += " ic_cvv    = '',";
            updateSQL += " diff_code = '',";
            updateSQL += " vendor          = ?,";
            updateSQL += " mail_type = ?,";
            updateSQL += " mail_branch = ?,";
            updateSQL += " mod_time        = sysdate,";
            updateSQL += " mod_pgm         = ? ";
            whereStr = " where rowid     = ? ";
            setString(1, hMbosNcccFilename);
            setString(2, hMbosEmboss4ThData);
            setString(3, pIcIndicator);
            setString(4, pKeyType);
            setString(5, pDerivKey);
            setInt(6, pLOffLnLmt);
            setInt(7, pUOffLnLmt);
            setString(8, hMbosVendor);
            setString(9, tmpMailType);
            setString(10, tmpBranch);
            setString(11, javaProgram);
            setRowId(12, hMbosRowid);

            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_emboss not found!", hMbosRowid, comcr.hCallBatchSeqno);
            }
        }

}
 /*************************************************************************/
void updateCrdCard() throws Exception {
    daoTable = "crd_card";
    updateSQL = " mail_type = ?,";
    updateSQL = " mail_branch = ?,";
    updateSQL += " mod_time     = sysdate,";
    updateSQL += " mod_pgm      = ? ";
    whereStr = "where card_no   = ? ";
    setString(1, tmpMailType);
    setString(2, tmpBranch);
    setString(3, javaProgram);
    setString(4, hMbosCardNo);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update_crd_card not found!", hMbosCardNo, comcr.hCallBatchSeqno);
    }
}

/***********************************************************************/
void updateTscCdrpLog() throws Exception {
	
    daoTable = "tsc_cdrp_log";
    updateSQL = "vendor_date_to = to_char(sysdate, 'yyyymmdd')";
    whereStr = "where rowid    = ? ";
    setRowId(1, hTscRowid);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update_tsc_cdrp_log not found!", "", hCallBatchSeqno);
    }
}

/*************************************************************************/
void updateIpsCdrpLog() throws Exception {
	
    daoTable = "ips_cdrp_log";
    updateSQL = "vendor_date_to = to_char(sysdate,'yyyymmdd')";
    whereStr = "where rowid   = ? ";
    setRowId(1, hIpsRowid);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update_ips_cdrp_log not found!", "", hCallBatchSeqno);
    }
}

/*************************************************************************/
void updateIchB07bCard() throws Exception {
	
    daoTable  = "ich_b07b_card  ";
    updateSQL = "vendor_date_to = to_char(sysdate, 'yyyymmdd')";
    whereStr  = "where rowid    = ? ";
    setRowId(1, hIchRowid);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update ich_b07b_card  not found!", "", hCallBatchSeqno);
    }
}

/*************************************************************************/
void insertFileCtl1(String fileName) throws Exception {
	
    setValue("file_name", fileName);
    setValue("crt_date", sysDate);
    setValueInt("head_cnt", totalCnt2);
    setValueInt("record_cnt", totalCnt2);
    setValue("trans_in_date", sysDate);
    daoTable = "crd_file_ctl";
    insertTable();        
}

/***********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", hNcccFilename);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", hRecCnt1);
        setValueInt("record_cnt", hRecCnt1);
        setValue("trans_in_date", sysDate);
        daoTable = "crd_file_ctl";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_file_ctl duplicate!", hNcccFilename, comcr.hCallBatchSeqno);
        } else {
            daoTable = "crd_file_ctl";
            updateSQL = " head_cnt       = ?,";
            updateSQL += " record_cnt     = ?,";
            updateSQL += " trans_in_date  = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setInt(1, hRecCnt1);
            setInt(2, hRecCnt1);
            setString(3, hNcccFilename);
            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", hNcccFilename, comcr.hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    // *新製卡(新製卡,普昇金,緊急新製卡,掛失補發卡,緊急補發卡,緊急替代卡)
    void createNcccNewFile() throws Exception {
        String tmpValue = "";
        String ap1Data = "";
        ap1ReceiveType = "";
        ap1ReceiveBranch = "";
        ap1ReceiveZip = "";    	 	 
        ap1ReceiveAddr = "";

        rtn = 0;
        tempSeq++;
        
        ap1Data = (String) map2.get(hMbosActNo);       
        
        ap1ReceiveType = commStr.mid(ap1Data,904,1);
        ap1ReceiveType = ap1ReceiveType.trim();
        
        ap1ReceiveBranch = commStr.mid(ap1Data,905,4);
        ap1ReceiveBranch = ap1ReceiveBranch.trim();
        
        ap1ReceiveZip = commStr.mid(ap1Data,909,5);
        ap1ReceiveZip = ap1ReceiveZip.trim();
        
        ap1ReceiveAddr = commStr.mid(ap1Data,914,60);
        ap1ReceiveType = ap1ReceiveAddr.trim();
        
        ncccData.seq = String.format("%06d", tempSeq);
        ncccData.cntl1 = "(";
        ncccData.cntl2 = "$";
        ncccData.acct1 = hMbosCardNo.substring(0,4)+" "+ hMbosCardNo.substring(4,8)+" "+ hMbosCardNo.substring(8,12)+" "+ hMbosCardNo.substring(12,16);
        ncccData.cntl3 = "#";
        ncccData.dteEffMm = String.format("%-2.2s", hMbosValidFm.substring(4, 6));
        ncccData.filler1 = "/";
        ncccData.dteEffYy = String.format("%-2.2s", hMbosValidFm.substring(2, 4));
        ncccData.filler2 = "";
        ncccData.dteExpMm = String.format("%-2.2s", hMbosValidTo.substring(4, 6));
        ncccData.filler3 = "/";
        ncccData.dteExpYy = String.format("%-2.2s", hMbosValidTo.substring(2, 4));
        ncccData.type    = "";
        ncccData.code    = "*";
        ncccData.cntl4 = "&";
        ncccData.name    = hMbosEngName;
        ncccData.cntl5 = ")";
        ncccData.acct2 = hMbosCardNo.substring(0,4)+" "+ hMbosCardNo.substring(4,8)+" "+ hMbosCardNo.substring(8,12)+" "+ hMbosCardNo.substring(12,16);
        ncccData.filler4 = "";
        
        tmpValue = comc.transPasswd(1, hMbosCvv2);
        ncccData.cvv2 = tmpValue;

        ncccData.cntl6 = "["; //EBCDIC   X'4A'
        ncccData.t1Hex6C = "%";
        ncccData.t1FmtCode = "B";
        ncccData.t1Acct = hMbosCardNo;
        ncccData.t1Hex5F1 = "^"; //EBCDIC X'5F'
        ncccData.t1Name = hMbosEngName;
        ncccData.t1Hex5F2 = "^"; //EBCDIC X'5F'
        ncccData.t1ExpDate = String.format("%-4.4s", hMbosValidTo.substring(2, 6));
        ncccData.t1SvcCode = hMbosServiceCode;
        ncccData.t1Pvki = hMbosPvki;
        
        tmpValue = comc.transPasswd(1, hMbosPvv);
        ncccData.t1Pvv = tmpValue;
        
        ncccData.t1Unused1 = "00000000";
        ncccData.t1Unused2 = "00";
        
        tmpValue = comc.transPasswd(1, hMbosCvv);
        ncccData.t1Cvv = tmpValue;

        ncccData.t1Unused3 = "00";
        ncccData.t1Aci = "0";
        ncccData.t1Unused4 = "000";
        ncccData.t1Cntl2 = "?";
        ncccData.t2Hex5E = ";";
        ncccData.t2Acct = hMbosCardNo;
        ncccData.t2Hex7E = "=";
        ncccData.t2ExpDate = String.format("%-4.4s", hMbosValidTo.substring(2, 6));
        ncccData.t2SvcCode = hMbosServiceCode;
        ncccData.t2Pvki = hMbosPvki;
        
        tmpValue = comc.transPasswd(1, hMbosPvv);
        ncccData.t2Pvv = tmpValue;
        
        tmpValue = comc.transPasswd(1, hMbosCvv);
        ncccData.t2Cvv = tmpValue;
        
        ncccData.t2Unused = "00000";
        ncccData.t2Hex6F = "?"; //在主機EBCDIC看到是一個'?',內碼為6F;
        ncccData.idNumber = hMbosApplyId;
        
        tmpValue = String.format("%03d",(Integer.parseInt(hMbosBirthday.substring(0, 4)) - 1911))+ hMbosBirthday.substring(4, 8);
        ncccData.birthDte = tmpValue;
        
        ncccData.photoCard = "N";
        
        if(hMbosSupFlag.equals("0")) {
        	ncccData.prinSupp = "Y";
        }
        else {
        	ncccData.prinSupp = "N";
        }

        ncccData.regBankNo1 = hMbosRegBankNo;
        
        selectActAcno(hMbosAcctKey, hMbosAcctType);
        selectCrdIdno(hMbosApplyId, hMbosApplyIdCode);
                
        ncccData.filler5      = ">>>";
        
        tmpValue = comc.transPasswd(1, hMbosIcCvv);
        ncccData.icvv = tmpValue;

        ncccData.e3           = "";
        
        if(selectCrdIdnoStaffFlag(hMbosApplyId, hMbosApplyIdCode)) {
        	ap1ReceiveType = "E";
        }
        
        if(ap1ReceiveType.equals("E")) {
        	ncccData.coupon = "9";
        }
        else if(ap1ReceiveType.equals("B")){
            ncccData.coupon = "A";
        }
        else {
        	ncccData.coupon = "";
        }
        
        if(hMbosSpecialCardRate.length()>0 && comcr.str2double(hMbosSpecialCardRate)!=0) {
        	Double tmpNum = 0.0;
       	    tmpNum = Math.round((comcr.str2double(hMbosSpecialCardRate)*365/100)*100.0)/100.0;
        	ncccData.cmrate =commStr.numFormat(tmpNum, "##.##");
        }else {
        	selectPtrRcrate(hAcnoRevolveIntRate);
        	ncccData.cmrate =commStr.numFormat(hPtrcRcrateYear, "##.##");
        }
                
        if(hMbosElectronicCode.equals("01")) {
            switch (comcr.str2int(hMbosEmbossSource)) {
            case 1:
            case 2:
            	ncccData.chgReason = "N";
                break;
            case 3:
            case 4:
            	ncccData.chgReason = "C";
                break;
            case 5:
            	ncccData.chgReason = "R";
                break;
            default:
            	ncccData.chgReason = "N";
                break;
            }
        	
        }else {
            ncccData.chgReason = "";
        }
        
        ncccData.crdTyp = hMbosGroupCode;
        ncccData.crdOrg = hMbosAcctType;
        
        if(hMbosElectronicCode.equals("00")) {
            ncccData.autoloadDefYn = "";
        }
        else if(hMbosElectronicCode.equals("01")){
        	if(hMbosTscAutoloadFlag.length() > 0) {
        		ncccData.autoloadDefYn = hMbosTscAutoloadFlag;
        	}
        	else {
        		ncccData.autoloadDefYn = "Y";
        	}
        }
        else {
            ncccData.autoloadDefYn = "Y";
        }

        ncccData.filler6      = "";
        ncccData.filler7      = "<<<";
        ncccData.filler8      = "";
        ncccData.filler9      = "";
        
        ncccData.id1          = hMbosPmId;
        ncccData.id2          = hMbosApplyId;
        
        ncccData.cardNo = hMbosCardNo;
    
        if (hMbosSupFlag.equals("1")) {
        	if(selectCrdIdnoStaffFlag(hMbosPmId, hMbosPmIdCode)) {
        		hEmpChiName = selectCrdEmployeeChiName(hMbosPmId);
        		if(hEmpChiName.length() > 0) {
        			ncccData.chiName = fixLeft(hIdnoChiName, 18) + fixLeft(hEmpChiName, 12);
        		}
        		else {
        			ncccData.chiName = fixLeft(hIdnoChiName, 30);
        		}        		       		
        	}
        	else {
        		ncccData.chiName = fixLeft(hIdnoChiName, 30);
        	}          
        }
        else {
        	ncccData.chiName = hIdnoChiName;
        }
        
        ncccData.engName = hMbosEngName;
        
        if(!hCardIndivCrdLmt.equals("0")) {
        	ncccData.authCreditLmt = hCardIndivCrdLmt;
        }
        else {
        	ncccData.authCreditLmt = hMbosAuthCreditLmt;
        }        

        ncccData.stmtCycle = hAcnoStmtCycle;
        ncccData.regBankNo2 = hMbosRegBankNo;
        ncccData.validFm = String.format("%-2.2s%-2.2s", hMbosValidFm.substring(4, 6), hMbosValidFm.substring(2, 4));
        ncccData.validTo = String.format("%-2.2s%-2.2s", hMbosValidTo.substring(4, 6), hMbosValidTo.substring(2, 4));
        
        if((ap1ReceiveType.equals("C"))||(ap1ReceiveType.equals("H"))||(ap1ReceiveType.equals("M"))){
        	String tmpEcsReceiveAddr = "";
        	String tmpEcsReceiveAddr60 = "";
        	
        	ncccData.zipCode = hAcnoBillSendingZip;
        	
        	tmpEcsReceiveAddr = hAcnoBillSendingAddr1.trim().replace("　", "") + 
		                        hAcnoBillSendingAddr2.trim().replace("　", "") +
		     		            hAcnoBillSendingAddr3.trim().replace("　", "") + 
		     		            hAcnoBillSendingAddr4.trim().replace("　", "") +
		     		            hAcnoBillSendingAddr5.trim().replace("　", "");
            tmpEcsReceiveAddr = fixLeft(tmpEcsReceiveAddr, 60);
            ncccData.billAddr30 = fixLeft(tmpEcsReceiveAddr, 30);

            tmpEcsReceiveAddr60 = tmpEcsReceiveAddr.substring(ncccData.billAddr30.length());
            ncccData.billAddr60 = fixLeft(tmpEcsReceiveAddr60, 30);        	
        }
        else {
        	ncccData.zipCode = "";
        	ncccData.billAddr30 = "";
        	ncccData.billAddr60 = "";
        }
        
        ncccData.unitCode = hMbosUnitCode;
        ncccData.cardType = hMbosCardType;
        ncccData.emboss4ThData = hMbosEmboss4ThData;
        
        if (!hMbosElectronicCode.equals("00")) {
        	selectPtrSysParm();
        	if(tmpWfValue.equals("Y")) {
        		if(hMbosElectronicCode.equals("01")) {
                	selectTscCardNo();
                	ncccData.electronicCardNo = hTscCardNo;
        		}
        		if(hMbosElectronicCode.equals("02")) {
                	selectIpsCardNo();
                	ncccData.electronicCardNo = hIpsCardNo;
        		}
                if(hMbosElectronicCode.equals("03")) {
                	selectIchCardNo();
                	ncccData.electronicCardNo = hIchCardNo;
        		}
        	}
        	else {
        		ncccData.electronicCardNo = "";
        	}
        }
        else {
        	ncccData.electronicCardNo = "";
        }
        
        ncccData.electronicCode = hMbosElectronicCode;
        ncccData.sourceCode = hMbosSourceCode;

        if(ap1ReceiveType.equals("E")) {
        	tmpMailType = "4";
        	tmpBranch = hMbosMailBranch;
        }
        else if (ap1ReceiveType.equals("B")) {
        	tmpMailType = "4";
        	tmpBranch = ap1ReceiveBranch;
        }
        else {
        	tmpMailType = "1";
        	tmpBranch = "";
        }

        ncccData.mailType = tmpMailType;
        ncccData.branch = tmpBranch;
        ncccData.icKind = hUnitIckind;
        
        String data = ncccData.allText();
        String data2 = "";
        ncccData.initString();
        if(map2.get(hMbosActNo) != null) {
        	data2 = data + map2.get(hMbosActNo);
        	hRecCnt2++;
            nccc.write(data2 + "\r\n"); //因應卡部需求調整為0D0A
        }else {
        	rtn = 1;
        }
    }

    /***********************************************************************/
    void convertNoRtn() throws Exception {
        int[] prefixN = { 1, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        tmpConvert = "";
        aftConvert = "";
        tempX01 = tempX10.substring(0, 1);
        convertDig();
        tmpConvert = String.format("%2d", tempInt);

        tempX01 = tempX10.substring(1, 2);
        convertDig();
        tempX02 = String.format("%2d", tempInt);
        tmpConvert += tempX02.substring(1, 2);
        tmpConvert += tempX10.substring(2, 2 + 7);

        for (int int2 = 0; int2 < 10; int2++) {
            tempX01 = String.format("%1.1s", tmpConvert.substring(int2));
            tempX02 = String.format("%2d", comcr.str2int(tempX01) * prefixN[int2]);
            aftConvert += tempX02.substring(1, 2);
        }

        tempInt = 0;
        for (int int2 = 0; int2 < 10; int2++) {
            tempX01 = String.format("%1.1s", aftConvert.substring(int2, int2 + 1));
            tempInt = tempInt + comcr.str2int(tempX01);
        }

        tempX02 = String.format("%2d", tempInt);
        /* 取尾數 */
        tempX01 = tempX02.substring(1);
        /* 10 - 取尾數 */
        tempInt = 10 - comcr.str2int(tempX01);

        if (tempInt == 10) {
            tempX01 = String.format("0");
        } else {
            tempX01 = String.format("%1d", tempInt);
        }

    }

    /***********************************************************************/
    void updateCrdCombo() throws Exception {
        daoTable = "crd_combo";
        updateSQL = " to_nccc_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr = "where card_no   = ? ";
        setString(1, javaProgram);
        setString(2, hMbosCardNo);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_combo not found!", hMbosCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void convertDig() throws Exception {
        if (tempX01.subSequence(0, 1).equals("A"))
            tempInt = 10;
        if (tempX01.subSequence(0, 1).equals("B"))
            tempInt = 11;
        if (tempX01.subSequence(0, 1).equals("C"))
            tempInt = 12;
        if (tempX01.subSequence(0, 1).equals("D"))
            tempInt = 13;
        if (tempX01.subSequence(0, 1).equals("E"))
            tempInt = 14;
        if (tempX01.subSequence(0, 1).equals("F"))
            tempInt = 15;
        if (tempX01.subSequence(0, 1).equals("G"))
            tempInt = 16;
        if (tempX01.subSequence(0, 1).equals("H"))
            tempInt = 17;
        if (tempX01.subSequence(0, 1).equals("I"))
            tempInt = 34;
        if (tempX01.subSequence(0, 1).equals("J"))
            tempInt = 18;
        if (tempX01.subSequence(0, 1).equals("K"))
            tempInt = 19;
        if (tempX01.subSequence(0, 1).equals("L"))
            tempInt = 20;
        if (tempX01.subSequence(0, 1).equals("M"))
            tempInt = 21;
        if (tempX01.subSequence(0, 1).equals("N"))
            tempInt = 22;
        if (tempX01.subSequence(0, 1).equals("O"))
            tempInt = 35;
        if (tempX01.subSequence(0, 1).equals("P"))
            tempInt = 23;
        if (tempX01.subSequence(0, 1).equals("Q"))
            tempInt = 24;
        if (tempX01.subSequence(0, 1).equals("R"))
            tempInt = 25;
        if (tempX01.subSequence(0, 1).equals("S"))
            tempInt = 26;
        if (tempX01.subSequence(0, 1).equals("T"))
            tempInt = 27;
        if (tempX01.subSequence(0, 1).equals("U"))
            tempInt = 28;
        if (tempX01.subSequence(0, 1).equals("V"))
            tempInt = 29;
        if (tempX01.subSequence(0, 1).equals("W"))
            tempInt = 30;
        if (tempX01.subSequence(0, 1).equals("X"))
            tempInt = 31;
        if (tempX01.subSequence(0, 1).equals("Y"))
            tempInt = 32;
        if (tempX01.subSequence(0, 1).equals("Z"))
            tempInt = 33;
    }

    /***********************************************************************/
    void selectIdPSeqno(String id, String idCode) throws Exception {
        String hIdnoId = id;
        String hIdnoIdCode = idCode;

        hIdnoIdPSeqno = "";
        sqlCmd = "select id_p_seqno ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no      = ? ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hIdnoId);
        setString(2, hIdnoIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoIdPSeqno = getValue("id_p_seqno");
        }

    }
    /***********************************************************************/
    void selectActAcno(String hMbosAcctKey, String hMbosAcctType) throws Exception {

    	hAcnoBillApplyFlag = "";
    	hAcnoRevolveIntRate = 0.0;
    	hAcnoLineOfCreditAmtCash = "";
    	hAcnoStmtCycle = "";
    	hAcnoComboAcctNo = "";
    	
        sqlCmd = "select bill_apply_flag, ";
        sqlCmd += " revolve_int_rate, ";
        sqlCmd += " line_of_credit_amt_cash, ";
        sqlCmd += " stmt_cycle, ";
        sqlCmd += " bill_sending_zip, ";
        sqlCmd += " bill_sending_addr1, ";
        sqlCmd += " bill_sending_addr2, ";
        sqlCmd += " bill_sending_addr3, ";
        sqlCmd += " bill_sending_addr4, ";
        sqlCmd += " bill_sending_addr5, ";
        sqlCmd += " combo_acct_no ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acct_key      = ? ";
        sqlCmd += "  and acct_type = ? ";
        if(hCardIndicator.equals("2")) {
        	setString(1, hCardPSeqno + 0);
        }
        else {
        	setString(1, hMbosAcctKey);
        }

        setString(2, hMbosAcctType);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hAcnoBillApplyFlag = getValue("bill_apply_flag");
        	hAcnoRevolveIntRate = getValueDouble("revolve_int_rate");
        	hAcnoLineOfCreditAmtCash = getValue("line_of_credit_amt_cash");
        	hAcnoStmtCycle = getValue("stmt_cycle");
        	hAcnoBillSendingZip = getValue("bill_sending_zip");
        	hAcnoBillSendingAddr1 = getValue("bill_sending_addr1");
        	hAcnoBillSendingAddr2 = getValue("bill_sending_addr2");
        	hAcnoBillSendingAddr3 = getValue("bill_sending_addr3");
        	hAcnoBillSendingAddr4 = getValue("bill_sending_addr4");
        	hAcnoBillSendingAddr5 = getValue("bill_sending_addr5");
        	hAcnoComboAcctNo = getValue("combo_acct_no");
        }

    }
    /***********************************************************************/
    boolean selectCrdIdnoStaffFlag(String id, String idCode) throws Exception {
        String hIdnoId = id;
        String hIdnoIdCode = idCode;
        
        sqlCmd = "select staff_flag ";
        sqlCmd += " from crd_idno ";
        sqlCmd += " where id_no = ? ";
        sqlCmd += " and id_no_code = ? ";
        setString(1, hIdnoId);
        setString(2, hIdnoIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	if(getValue("staff_flag").equals("Y"))
        		return true;
        	else {
        		return false;
        	}
        }
        	return false;
    }
    /***********************************************************************/
    String selectCrdEmployeeChiName(String id, String idCode) throws Exception {
        String hIdnoId = id;
        String hIdnoIdCode = idCode;

        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_employee ";
        sqlCmd += " where id    = ? ";
        sqlCmd += " and id_code = ? ";
        setString(1, hIdnoId);
        setString(2, hIdnoIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	return getValue("chi_name");
        }
        	return getValue("chi_name");
    }
    /***********************************************************************/
    void selectCrdIdno(String id, String idCode) throws Exception {
        String hIdnoId = id;
        String hIdnoIdCode = idCode;

        hIdnoIdPSeqno = "";
        sqlCmd = "select resident_zip,mail_zip, ";
        sqlCmd += " company_zip,chi_name,  ";
        sqlCmd += " home_area_code1,home_tel_no1,  ";
        sqlCmd += " home_tel_ext1,office_area_code1,  ";
        sqlCmd += " office_tel_no1,office_tel_ext1,  ";
        sqlCmd += " resident_addr1,mail_addr1,  ";
        sqlCmd += " company_addr1,resident_addr2,  ";
        sqlCmd += " mail_addr2,company_addr2,  ";
        sqlCmd += " resident_addr3,mail_addr3,  ";
        sqlCmd += " company_addr3,resident_addr4,  ";
        sqlCmd += " mail_addr4,company_addr4,  ";
        sqlCmd += " resident_addr5,mail_addr5,company_addr5  ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no      = ? ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hIdnoId);
        setString(2, hIdnoIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hIdnoResidentZip = getValue("resident_zip");
        	hIdnoMailZip = getValue("mail_zip");
        	hIdnoCompanyZip = getValue("company_zip");
        	hIdnoChiName = getValue("chi_name");
        	hIdnoHomeAreaCode1 = getValue("home_area_code1");
        	hIdnoHomeTelNo1 = getValue("home_tel_no1");
        	hIdnoHomeTelExt1 = getValue("home_tel_ext1");
        	hIdnoOfficeAreaCode1 = getValue("office_area_code1");
        	hIdnoOfficeTelNo1 = getValue("office_tel_no1");
        	hIdnoOfficeTelExt1 = getValue("office_tel_ext1");
        	hIdnoResidentAddr1 = getValue("resident_addr1");
        	hIdnoMailAddr1 = getValue("mail_addr1");
        	hIdnoCompanyAddr1 = getValue("company_addr1");
        	hIdnoResidentAddr2 = getValue("resident_addr2");
        	hIdnoMailAddr2 = getValue("mail_addr2");
        	hIdnoCompanyAddr2 = getValue("company_addr2");
        	hIdnoResidentAddr3 = getValue("resident_addr3");
        	hIdnoMailAddr3 = getValue("mail_addr3");
        	hIdnoCompanyAddr3 = getValue("company_addr3");
        	hIdnoResidentAddr4 = getValue("resident_addr4");
        	hIdnoMailAddr4 = getValue("mail_addr4");
        	hIdnoCompanyAddr4 = getValue("company_addr4");
        	hIdnoResidentAddr5 = getValue("resident_addr5");
        	hIdnoMailAddr5 = getValue("mail_addr5");
        	hIdnoCompanyAddr5 = getValue("company_addr5");
        }

    }
    /***********************************************************************/
    String selectCrdEmployeeChiName(String id) throws Exception {
        String hIdnoId = id;

        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_employee ";
        sqlCmd += " where id    = ? ";
        setString(1, hIdnoId);
       recordCnt = selectTable();
        if (recordCnt > 0) {
        	return getValue("chi_name");
        }
        	return "";
    }
    /***********************************************************************/
    void selectPtrSysParm() throws Exception 
    {
      tmpWfValue = "N";
      
      sqlCmd  = "select wf_value ";
      sqlCmd += "  from ptr_sys_parm   ";
      sqlCmd += " where wf_parm = 'SYSPARM'  ";
      sqlCmd += "   and wf_key = 'ELEC_CARD_NO' ";
      int recordCnt = selectTable();
      if (recordCnt > 0) {
    	  tmpWfValue = getValue("wf_value");
      }
      return;
    }
    /***********************************************************************/
    void selectTscCardNo() throws Exception {
    	hTscCardNo ="";
    	hTscRowid = "";
    	sqlCmd = "select tsc_card_no ";
        sqlCmd += " from tsc_cdrp_log  ";
        sqlCmd += "where card_no        = ?  ";
        sqlCmd += "  and vendor_date_to = ''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hMbosCardNo);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_cdrp_log not found!", hMbosCardNo, hCallBatchSeqno);
        }
        hTscCardNo = getValue("tsc_card_no");
        hTscRowid = getValue("rowid");
    }
    /***********************************************************************/
    void selectIpsCardNo() throws Exception {
    	hIpsCardNo ="";
    	hIpsRowid = "";
    	sqlCmd = "select ips_card_no ";
        sqlCmd += " from ips_cdrp_log  ";
        sqlCmd += "where card_no        = ?  ";
        sqlCmd += "  and vendor_date_to = ''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hMbosCardNo);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ips_cdrp_log not found!", hMbosCardNo, hCallBatchSeqno);
        }
        hIpsCardNo = getValue("ips_card_no");
        hIpsRowid = getValue("rowid");
    }
    /***********************************************************************/
    void selectIchCardNo() throws Exception {
    	hIchCardNo ="";
    	hIchRowid = "";
    	sqlCmd = "select ich_card_no ";
        sqlCmd += " from ich_b02b_fback  ";
        sqlCmd += "where card_no        = ?  ";
        sqlCmd += "  and vendor_date_to = ''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hMbosCardNo);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ich_b02b_fback not found!", hMbosCardNo, hCallBatchSeqno);
        }
        hIchCardNo = getValue("ich_card_no");
        hIchRowid = getValue("rowid");
    }
    /***********************************************************************/
    void selectPtrRcrate(double hAacnoRevolveIntRate) throws Exception {

        sqlCmd = "select rcrate_year ";
        sqlCmd += " from ptr_rcrate ";
        sqlCmd += " where rcrate_day = ? ";
        setDouble(1, hAacnoRevolveIntRate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hPtrcRcrateYear = getValueDouble("rcrate_year");
        }

    }
    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        String spc = "";
        for (int i = 0; i < 100; i++)
            spc += " ";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);
        return new String(vResult, "MS950");
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdD063 proc = new CrdD063();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
       	String seq;                 /*1~6*/    
        String cntl1;               /*7~7*/    
        String cntl2;               /*8~8*/    
        String acct1;               /*9~27*/   
        String cntl3;               /*28~28*/  
        String dteEffMm;            /*29~30*/  
        String filler1;             /*31~31*/  
        String dteEffYy;            /*32~33*/  
        String filler2;             /*34~38*/  
        String dteExpMm;            /*39~40*/  
        String filler3;             /*41~41*/  
        String dteExpYy;            /*42~43*/  
        String type;                /*44~45*/  
        String code;                /*46~46*/  
        String cntl4;               /*47~47*/  
        String name;                /*48~75*/  
        String cntl5;               /*76~76*/  
        String acct2;               /*77~95*/  
        String filler4;             /*96~96*/  
        String cvv2;                /*97~99*/  
        String cntl6;               /*100~100*/
        String t1Hex6C;             /*101~101*/
        String t1FmtCode;           /*102~102*/
        String t1Acct;              /*103~118*/
        String t1Hex5F1;            /*119~119*/
        String t1Name;              /*120~145*/
        String t1Hex5F2;            /*146~146*/
        String t1ExpDate;           /*147~150*/
        String t1SvcCode;           /*151~153*/
        String t1Pvki;              /*154~154*/
        String t1Pvv;               /*155~158*/
        String t1Unused1;           /*159~166*/
        String t1Unused2;           /*167~168*/
        String t1Cvv;               /*169~171*/
        String t1Unused3;           /*172~173*/
        String t1Aci;               /*174~174*/
        String t1Unused4;           /*175~177*/
        String t1Cntl2;             /*178~178*/
        String t2Hex5E;             /*179~179*/
        String t2Acct;              /*180~195*/
        String t2Hex7E;             /*196~196*/
        String t2ExpDate;           /*197~200*/
        String t2SvcCode;           /*201~203*/
        String t2Pvki;              /*204~204*/
        String t2Pvv;               /*205~208*/
        String t2Cvv;               /*209~211*/
        String t2Unused;            /*212~216*/
        String t2Hex6F;             /*217~217*/
        String idNumber;            /*218~228*/
        String birthDte;            /*229~235*/
        String photoCard;           /*236~236*/
        String prinSupp;            /*237~237*/
        String regBankNo1;          /*238~241*/
        String zipCode;             /*242~247*/
        String filler5;             /*248~250*/
        String icvv;                /*251~253*/
        String e3;                  /*254~276*/
        String coupon;              /*277~277*/
        String cmrate;              /*278~282*/
        String chgReason;           /*283~283*/
        String crdTyp;              /*284~287*/
        String crdOrg;              /*288~289*/
        String autoloadDefYn;       /*290~290*/
        String filler6;             /*291~331*/
        String filler7;             /*332~334*/
        String filler8;             /*335~337*/
        String filler9;             /*338~339*/
        String id1;                 /*340~350*/
        String id2;                 /*351~361*/
        String cardNo;              /*362~377*/
        String chiName;             /*378~407*/
        String engName;             /*408~433*/
        String authCreditLmt;       /*434~443*/
        String stmtCycle;           /*444~445*/
        String regBankNo2;          /*446~449*/
        String validFm;             /*450~453*/
        String validTo;             /*454~457*/
        String billAddr30;          /*458~487*/
        String billAddr60;          /*488~517*/
        String unitCode;            /*518~521*/
        String cardType;            /*522~523*/
        String emboss4ThData;       /*524~543*/
        String electronicCardNo;    /*544~563*/
        String electronicCode;      /*564~565*/
        String sourceCode;          /*566~571*/
        String mailType;            /*572~572*/
        String branch;              /*573~576*/
        String icKind;             /*577~577*/
        String filler10;           /*578~700*/
        
        void initString() {
        	seq          		    = "";
        	cntl1 = "";
        	cntl2 = "";
        	acct1 = "";
        	cntl3 = "";
        	dteEffMm = "";
        	filler1       		    = "";
        	dteEffYy = "";
        	filler2       		    = "";
        	dteExpMm = "";
        	filler3        		    = "";
        	dteExpYy = "";
        	type             	    = "";
        	code            	    = "";
        	cntl4 = "";
        	name           		    = "";
        	cntl5 = "";
        	acct2 = "";
        	filler4                 = "";
        	cvv2         	        = "";
        	cntl6 = "";
        	t1Hex6C = "";
        	t1FmtCode = "";
        	t1Acct = "";
        	t1Hex5F1 = "";
        	t1Name = "";
        	t1Hex5F2 = "";
        	t1ExpDate = "";
        	t1SvcCode = "";
        	t1Pvki = "";
        	t1Pvv = "";
        	t1Unused1 = "";
        	t1Unused2 = "";
        	t1Cvv = "";
        	t1Unused3 = "";
        	t1Aci = "";
        	t1Unused4 = "";
        	t1Cntl2 = "";
        	t2Hex5E = "";
        	t2Acct = "";
        	t2Hex7E = "";
        	t2ExpDate = "";
        	t2SvcCode = "";
        	t2Pvki = "";
        	t2Pvv = "";
        	t2Cvv = "";
        	t2Unused = "";
        	t2Hex6F = "";
        	idNumber = "";
        	birthDte = "";
        	photoCard = "";
        	prinSupp = "";
        	regBankNo1 = "";
        	zipCode = "";
        	filler5					= "";
        	icvv					= "";
        	e3						= "";
        	coupon					= "";
        	cmrate					= "";
        	chgReason = "";
        	crdTyp = "";
        	crdOrg = "";
        	autoloadDefYn = "";
        	filler6					= "";
        	filler7					= "";
        	filler8					= "";
        	filler9					= "";
        	id1						= "";
        	id2						= "";
        	cardNo = "";
        	chiName = "";
        	engName = "";
        	authCreditLmt = "";
        	stmtCycle = "";
        	regBankNo2 = "";
        	validFm = "";
        	validTo = "";
         	billAddr30 = "";
        	billAddr60 = "";
        	unitCode = "";
        	cardType = "";
        	emboss4ThData = "";
        	electronicCardNo = "";
        	electronicCode = "";
        	sourceCode = "";
        	mailType = "";
        	branch					= "";
        	icKind                  = "";
        	filler10                = "";
        	
        }
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(seq, 6);
            rtn += fixLeft(cntl1, 1);
            rtn += fixLeft(cntl2, 1);
            rtn += fixLeft(acct1, 19);
            rtn += fixLeft(cntl3, 1);
            rtn += fixLeft(dteEffMm, 2);
            rtn += fixLeft(filler1, 1);
            rtn += fixLeft(dteEffYy, 2);
            rtn += fixLeft(filler2, 5);
            rtn += fixLeft(dteExpMm, 2);
            rtn += fixLeft(filler3, 1);
            rtn += fixLeft(dteExpYy, 2);
            rtn += fixLeft(type, 2);
            rtn += fixLeft(code, 1);
            rtn += fixLeft(cntl4, 1);
            rtn += fixLeft(name, 28);
            rtn += fixLeft(cntl5, 1);
            rtn += fixLeft(acct2, 19);
            rtn += fixLeft(filler4, 1);
            rtn += fixLeft(cvv2, 3);
            rtn += fixLeft(cntl6, 1);
            rtn += fixLeft(t1Hex6C, 1);
            rtn += fixLeft(t1FmtCode, 1);
            rtn += fixLeft(t1Acct, 16);
            rtn += fixLeft(t1Hex5F1, 1);
            rtn += fixLeft(t1Name, 26);
            rtn += fixLeft(t1Hex5F2, 1);
            rtn += fixLeft(t1ExpDate, 4);
            rtn += fixLeft(t1SvcCode, 3);
            rtn += fixLeft(t1Pvki, 1);
            rtn += fixLeft(t1Pvv, 4);
            rtn += fixLeft(t1Unused1, 8);
            rtn += fixLeft(t1Unused2, 2);
            rtn += fixLeft(t1Cvv, 3);
            rtn += fixLeft(t1Unused3, 2);
            rtn += fixLeft(t1Aci, 1);
            rtn += fixLeft(t1Unused4, 3);
            rtn += fixLeft(t1Cntl2, 1);
            rtn += fixLeft(t2Hex5E, 1);
            rtn += fixLeft(t2Acct, 16);
            rtn += fixLeft(t2Hex7E, 1);
            rtn += fixLeft(t2ExpDate, 4);
            rtn += fixLeft(t2SvcCode, 3);
            rtn += fixLeft(t2Pvki, 1);
            rtn += fixLeft(t2Pvv, 4);
            rtn += fixLeft(t2Cvv, 3);
            rtn += fixLeft(t2Unused, 5);
            rtn += fixLeft(t2Hex6F, 1);
            rtn += fixLeft(idNumber, 11);
            rtn += fixLeft(birthDte, 7);
            rtn += fixLeft(photoCard, 1);
            rtn += fixLeft(prinSupp, 1);
            rtn += fixLeft(regBankNo1, 4);
            rtn += fixLeft(zipCode, 6);
            rtn += fixLeft(filler5, 3);
            rtn += fixLeft(icvv, 3);
            rtn += fixLeft(e3, 23);
            rtn += fixLeft(coupon, 1);
            rtn += fixLeft(cmrate, 5);
            rtn += fixLeft(chgReason, 1);
            rtn += fixLeft(crdTyp, 4);
            rtn += fixLeft(crdOrg, 2);
            rtn += fixLeft(autoloadDefYn, 1);
            rtn += fixLeft(filler6, 41);
            rtn += fixLeft(filler7, 3);
            rtn += fixLeft(filler8, 3);
            rtn += fixLeft(filler9, 2);
            rtn += fixLeft(id1, 11);
            rtn += fixLeft(id2, 11);
            rtn += fixLeft(cardNo, 16);
            rtn += fixLeft(chiName, 30);
            rtn += fixLeft(engName, 26);
            rtn += fixLeft(authCreditLmt, 10);
            rtn += fixLeft(stmtCycle, 2);
            rtn += fixLeft(regBankNo2, 4);
            rtn += fixLeft(validFm, 4);
            rtn += fixLeft(validTo, 4);
            rtn += fixLeft(billAddr30, 30);
            rtn += fixLeft(billAddr60, 30);
            rtn += fixLeft(unitCode, 4);
            rtn += fixLeft(cardType, 2);
            rtn += fixLeft(emboss4ThData, 20);
            rtn += fixLeft(electronicCardNo, 20);
            rtn += fixLeft(electronicCode, 2);
            rtn += fixLeft(sourceCode, 6);
            rtn += fixLeft(mailType, 1);
            rtn += fixLeft(branch, 4);
            rtn += fixLeft(icKind, 1);
            rtn += fixLeft(filler10, 123);
            
            return rtn;
        }

         String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 200; i++)
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
    String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
        String spc = "";
        for (int i = 0; i < 100; i++)
            spc += "　";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);
        return new String(vResult, "MS950");
    }
    /***********************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "MAKECARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
        int errCode = commFTP.ftplogName("MAKECARD", "mput " + hNcccFilename);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(hNcccFilename);          
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
