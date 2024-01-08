/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  109/05/18   V1.01.01  Rou              Initial                           *
*  109/11/17  V1.00.02  yanghan       修改了變量名稱和方法名稱
*  109/11/30  V1.01.03  Wilson      outPutTextFile調整                                                                 *    
*  109/12/24  V1.01.04  yanghan       修改了變量名稱和方法名稱                                                            * 
*  110/01/05  V1.01.05  Wilson      tsc_vd_cdrp_log -> tsc_dcrp_log         *
*  111/12/19  V1.00.06  Wilson      調整為最新格式                                                                                   *
*  112/01/16  V1.00.07  Wilson      mark getIccardData、地址改成fixLeft        *
*  112/02/08  V1.00.08  Wilson      產檔路徑調整                                                                                        *
*  112/03/04  V1.00.09  Wilson      AP1金融卡製卡檔檔名調整                                                                 *
*  112/03/06  V1.00.10  Wilson      增加update dbc_debit                     *
*  112/03/13  V1.00.11  Wilson      調整procFTP執行順序                                                                    *
*  112/03/15  V1.00.12  Wilson      AP1金融卡製卡檔檔名調整                                                                 *
*  112/04/14  V1.00.13  Wilson      讀參數判斷是否由新系統編列票證卡號                                              *
*  112/05/12  V1.00.14  Wilson      wf_key change to VD_ELEC_CARD_NO        *
*  112/06/01  V1.00.15  Wilson      update mail_type、mail_branch            *
*  112/06/13  V1.00.16  Wilson      無檔案不當掉                                                                                       *
*  112/08/26  V1.00.17  Wilson      修正弱掃問題                                                                                       *
*  112/12/11  V1.00.18  Wilson      crd_item_unit不判斷卡種                                                        *
****************************************************************************/

package Dbc;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;
import com.CommFTP;

/*產生VISA DEBIT續卡製卡檔程式*/
public class DbcD063 extends AccessDAO {
    private String progname = "產生VISA DEBIT續卡製卡檔程式  112/12/11 V1.00.18";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommSecr     comsecr = null;
    CommString   commStr = new CommString();
    CommFTP      commFTP = null;

    int debug = 1;

    String prgmId = "DbcD063";
    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    BufferedWriter nccc = null;
    buf1 ncccData = new buf1();
    protected final String dt1Str = "col1,col2,col3,col4,col5,col6,col7,col8,col9,col10,col11,col12,"   
    		+ "col13,col14,col15,col16,col17,col18,col19,col20,col21,col22,col23,col24,col25,col26," 
    		+ "col27,col28,col29,col30,col31,col32,col33,col34,col35,col36,col37,col38,col39,col40," 
    		+ "col41,col42,col43,col44,col45,col46,col47,col48,col49,col50,col51,col52,col53,col54," 
    		+ "col55,col56,col57,col58,col59,col60,col61,col62,col63,col64,col65,col66,col67,col68," 
    		+ "col69,col70,col71,col72,col73,col74,col75,col76,col77,col78,col79,col80,col81,col82," 
    		+ "col83,col84,col85,col86,col87,col88,col89,col90,col91,col92,col93,col94,col95,col96," 
    		+ "col97,col98,col99,col100,col101,col102,col103,col104,col105,col106,col107,col108,"  
    		+ "col109,col110,col111,col112,col113,col114,col115,receive_type,receive_branch,"
    		+ "receive_zip,receive_addr,col120";
        
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
    
    int rptSeq1 = 0;
    int vendorCnt = 0;
    int embossCnt = 0;
    String buf = "";
    String stderr = "";
    String hModUser = "";
    String hCallBatchSeqno = "";
    String hVendor = "";
    String pServiceCode = "";
    String pKeyType = "";
    String pDerivKey = "";
    int pLOfflnLmt = 0;
    int pUOfflnLmt = 0;
    String pIcIndicator = "";
    String pExpireDate = "";
    String pCheckKeyExpire = "";   
    String hCardIndicator = "";
    String gAcctType = "";    
    String hSavingActno = "";
    String h3thData = "";   
    String hNcccFilename = "";
    int hRecCnt1 = 0;    
    String hDcesBatchno = "";
    double hDcesRecno = 0;
    String hDcesEmbossSource = "";
    String hDcesEmbossReason = "";
    String hDcesAcctType = "";
    String hDcesAcctKey = "";
    String hDcesToNcccCode = "";
    String hDcesCardType = "";
    String hDcesGroupCode = "";
    String hDcesCardNo = "";
    String hDcesBinNo = "";
    String hDcesMajorCardNo = "";
    String hDcesApplyId = "";
    String hDcesApplyIdCode = "";
    String hDcesValidFm = "";
    String hDcesValidTo = "";
    String hDcesMailZip = "";
    String hDcesBirthday = "";
    String hDcesNation = "";
    String hDcesBusinessCode = "";
    String hDcesEducation = "";
    String hDcesActNo = "";
    String hDcesHomeAreaCode1 = "";
    String hDcesHomeTelNo1 = "";
    String hDcesOrgEmbossData = "";
    String hDcesEmboss4ThData = "";
    String hDcesMemberId = "";
    String hDcesPmId = "";
    String hDcesPmIdCode = "";
    String hDcesCorpNo = "";
    String hDcesCorpNoCode = "";
    String hDcesForceFlag = "";
    String hDcesServiceCode = "";
    String hDcesEngName = "";
    String hDcesMarriage = "";
    String hDcesRelWithPm = "";
    String hDcesUnitCode = "";
    String hDcesSex = "";
    String hDcesPvv = "";
    String hDcesCvv = "";
    String hDcesPvki = "";
    String hDcesCvv2 = "";
    String hDcesNcccFilename = "";
    String hTscCardNo = "";
    String hDcesOpenNum = "";
    String hDcesOldCardNo = "";
    String hDcesChiName = "";
    String hMailAddr = "";
    String hDcesSupFlag = "";
    String hDcesNcccType = "";
    String hDcesRowid = "";
    String hDcesOldEndDate = "";
    String hDcesStatusCode = "";
    String hDcesReasonCode = "";
    String hDcesComboIndicator = "";
    String hDcesIcFlag = "";
    String hDcesBranch = "";
    String hDcesMailAttach1 = "";
    String hDcesMailAttach2 = "";
    String hDcesCsc = "";
    String hDcesVendor = "";
    String hDcesChkNcccFlag = "";
    String hDcesIcCvv = "";
    String hDcesApplySource = "";
    String hDcesRegBankNo = "";
    String hDcesBillApplyFlag = "";
    String hDcesStmtCycle = "";
    String hDcesToNcccDate = "";
    String hDcesCrtBankNo = "";
    String hDcesVdBankNo = "";
    String hDcesElectronicCode = "";
    String hDcesSourceCode = "";
    String hDcesMailType = "";
    String hDcesCardRefNum = "";
    String hDcesMailBranch ="";
    String hUnitIckind = "";
    String cmdStr = "";
    String tmpMailType = "";
    String tmpBranch = "";
    
    int tempInt = 0;
    int rtn = 0;
    int visaCard = 0;
    int hNn = 0;
    int tempSeq = 0;
    
    String hBinType = "";
    String acnoBillSendingZip;
    String acnoBillSendingAddr1;
    String acnoBillSendingAddr2;
    String acnoBillSendingAddr3;
    String acnoBillSendingAddr4;
    String acnoBillSendingAddr5;    
    String idnoHomeAreaCode1;
    String idnoHomeTelNo1;
    String idnoHomeTelExt1;
    String idnoOfficeAreaCode1;
    String idnoOfficeTelNo1;
    String idnoOfficeTelExt1;    
    String getFileName;
    String outFileName;
    String accountNo;
    String data1000;
    String ap1ReceiveType;
    String ap1ReceiveBranch;
    String ap1ReceiveZip = "";
    String ap1ReceiveAddr = "";
    int totalData;
    int totalInputFile;
    int totalOutputFile;
    int dataCntTmp = 0;
    String hBusiBusinessDate = "";
    String hSysDate = "";
    String fileDate = "";
    String tmpWfValue = "";
    
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
                comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comsecr = new CommSecr(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
//			if (args.length == 0) {
//				fileDate = hBusiBusinessDate;
//			} else if (args.length == 1) {
//				fileDate = args[0];
//			}
//			if (fileDate.length() != 8) {
//				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
//			}

            hModUser = comc.commGetUserID();
            
            if(getVdDebit()) {
            	selectDbcEmboss();
            }            
        	
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束,筆數 = ["+ totalData +"][" + hRecCnt1 + "]"
               		+ ",金融卡檔 = ["+ totalInputFile +"],信用卡+金融卡檔 = ["+ totalOutputFile +"]");
            
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    /***********************************************************************/
    int checkRtn() throws Exception {

        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from dbc_debit d, dbc_emboss a,dbp_acct_type b, ptr_group_card c  ";
        sqlCmd += "where a.to_nccc_date   = ''  ";
        sqlCmd += "  and a.reject_code    = ''  ";
        sqlCmd += "  and a.nccc_filename  = ''  ";
        sqlCmd += "  and nvl(rtrim(a.eng_name,' '),' ') = ' '  ";
        sqlCmd += "  and a.chi_name   <> ''  ";
        sqlCmd += "  and b.acct_type   = a.acct_type  ";
        sqlCmd += "  and c.group_code  = decode(a.group_code,'','0000',a.group_code)  ";
        sqlCmd += "  and c.card_type   = a.card_type  ";
        sqlCmd += "  and d.batchno     = a.batchno  ";
        sqlCmd += "  and d.card_no     = a.card_no  ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

        if (tempInt > 0)
            return (1);

        return (0);
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
	boolean getVdDebit() throws Exception {    	
    	int fileCount = 0;
    	
    	String tmpstr = String.format("%s/media/dbc", comc.getECSHOME());

        tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);
        List<String> listOfFiles = comc.listFS(tmpstr, "", "");
        
        for (String file : listOfFiles) {
            getFileName = file;
            if (getFileName.length() != 29)    
            	continue;
            if (!getFileName.substring(0, 15).equals("vd_debit_mkc_t_"))
                continue;           
//            if (!getFileName.substring(15, 23).equals(fileDate))
//            	continue;
//          if (checkFileCtl() != 0)
//            	continue;           
            fileCount ++;
            getFileName(getFileName);            
        }
        if (fileCount < 1) {
        	showLogMessage("I", "", "無檔案可處理");
        	return false;
        }
        return true;
    }
    
    /**********************************************************************/
    int getFileName(String fileName) throws Exception {
        String rec = "";
        String fileName2;
        int fi;
        fileName2 = comc.getECSHOME() + "/media/dbc/" + fileName;

        int f = openInputText(fileName2);
        if (f == -1) {
            return 1;
        }
        closeInputText(f);

        setConsoleMode("N");
        fi = openInputText(fileName2, "MS950");
        setConsoleMode("Y");
        if (fi == -1) {
            return 1;
        }
        
        showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/dbc/ ]");
        showLogMessage("I", "", " Process file =[" + fileName + "]");
        
        while (true) {      
        	rec = readTextFile(fi); //read file data
         	if (endFile[fi].equals("Y")) 
            	break;
         	
            totalInputFile ++;
         	moveData(processDataRecord(getFieldValue(rec, dt1Length), dt1));         	
            processDisplay(1000);
       }
                        
        closeInputText(fi);

        insertFileCtl1(fileName);

        renameFile(fileName);

        return 0;
    }
    
    /***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {    	
    	
    	data1000 = "";
    	accountNo = (String) map.get("col4");
    	accountNo = accountNo.trim();
    	showLogMessage("I", "", "File = [" + getFileName + "] , act_no = [" + accountNo + "]");
    	   	
    	data1000 = (String) map.get("col1") + (String) map.get("col2") + (String) map.get("col3") + 
    	(String) map.get("col4") + (String) map.get("col5") + (String) map.get("col6") + 
    	(String) map.get("col7") + (String) map.get("col8") + (String) map.get("col9") + 
    	(String) map.get("col10") + (String) map.get("col11") + (String) map.get("col12") + 
    	(String) map.get("col13") + (String) map.get("col14") + (String) map.get("col15") + 
	    (String) map.get("col16") + (String) map.get("col17") + (String) map.get("col18") + 
	    (String) map.get("col19") + (String) map.get("col20") + (String) map.get("col21") + 
	    (String) map.get("col22") + (String) map.get("col23") + (String) map.get("col24") + 
	    (String) map.get("col25") + (String) map.get("col26") + (String) map.get("col27") + 
	    (String) map.get("col28") + (String) map.get("col29") + (String) map.get("col30") + 
	    (String) map.get("col31") + (String) map.get("col32") + (String) map.get("col33") + 
	    (String) map.get("col34") + (String) map.get("col35") + (String) map.get("col36") + 
	    (String) map.get("col37") + (String) map.get("col38") + (String) map.get("col39") + 
	    (String) map.get("col40") + (String) map.get("col41") + (String) map.get("col42") + 
	    (String) map.get("col43") + (String) map.get("col44") + (String) map.get("col45") + 
	    (String) map.get("col46") + (String) map.get("col47") + (String) map.get("col48") + 
	    (String) map.get("col49") + (String) map.get("col50") + (String) map.get("col51") + 
	    (String) map.get("col52") + (String) map.get("col53") + (String) map.get("col54") + 
	    (String) map.get("col55") + (String) map.get("col56") + (String) map.get("col57") + 
	    (String) map.get("col58") + (String) map.get("col59") + (String) map.get("col60") + 
	    (String) map.get("col61") + (String) map.get("col62") + (String) map.get("col63") + 
	    (String) map.get("col64") + (String) map.get("col65") + (String) map.get("col66") + 
	    (String) map.get("col67") + (String) map.get("col68") + (String) map.get("col69") + 
	    (String) map.get("col70") + (String) map.get("col71") + (String) map.get("col72") + 
	    (String) map.get("col73") + (String) map.get("col74") + (String) map.get("col75") + 
	    (String) map.get("col76") + (String) map.get("col77") + (String) map.get("col78") + 
	    (String) map.get("col79") + (String) map.get("col80") + (String) map.get("col81") + 
	    (String) map.get("col82") + (String) map.get("col83") + (String) map.get("col84") + 
	    (String) map.get("col85") + (String) map.get("col86") + (String) map.get("col87") + 
	    (String) map.get("col88") + (String) map.get("col89") + (String) map.get("col90") + 
	    (String) map.get("col91") + (String) map.get("col92") + (String) map.get("col93") + 
	    (String) map.get("col94") + (String) map.get("col95") + (String) map.get("col96") + 
	    (String) map.get("col97") + (String) map.get("col98") + (String) map.get("col99") + 
	    (String) map.get("col100") + (String) map.get("col101") + (String) map.get("col102") + 
	    (String) map.get("col103") + (String) map.get("col104") + (String) map.get("col105") + 
	    (String) map.get("col106") + (String) map.get("col107") + (String) map.get("col108") + 
	   	(String) map.get("col109") + (String) map.get("col110") + (String) map.get("col111") + 
	   	(String) map.get("col112") + (String) map.get("col113") + (String) map.get("col114") + 
	   	(String) map.get("col115")+(String) map.get("receive_type")+(String) map.get("receive_branch")+
	   	(String) map.get("receive_zip")+(String) map.get("receive_addr")+(String) map.get("col120");
    	map2.put(accountNo, data1000);
    	    	
    	return;
    }
    
    /***********************************************************************/
    int openTextFile() throws Exception {
       	int tmpVendor;
        tmpVendor = comcr.str2int(hVendor); 
        String likeFilename = "";
    	String hFileName = "";
    	likeFilename = String.format("vd_%02d_makecard_cg_%s.txt", tmpVendor, hSysDate) + "%";

        sqlCmd  = "select file_name";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1,likeFilename);
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
            hFileName = getValue("file_name");
            hNn = Integer.valueOf(hFileName.substring(26, 28))+1;
        }
        
        hNcccFilename = String.format("vd_%02d_makecard_cg_%s%02d.txt", tmpVendor, hSysDate, hNn);
        
        showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

        outFileName = String.format("%s/media/dbc/%s", comc.getECSHOME(), hNcccFilename);
        outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");
        
        nccc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "MS950"));

    	return 0;
    }

    /***********************************************************************/
    int checkFileCtl() throws Exception {
        int hhCount = 0;
        sqlCmd  = "select count(*) hh_count ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name = ?  ";
        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
        setString(1, getFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hhCount = getValueInt("hh_count");
        }
        if (hhCount > 0) {
            showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已產生不可重複產生(crd_file_ctl)"));
            return (1);
        }
        return (0);
    }
    
    /***********************************************************************/
    void selectDbcEmboss() throws Exception {
    	
    	sqlCmd = "select ";
        sqlCmd += "distinct decode(a.nccc_type,'1',d.new_vendor,'2',d.chg_vendor,d.mku_vendor) vendor ";
        sqlCmd += "from dbc_emboss a, dbp_acct_type b, ptr_group_card c, crd_item_unit d ";
        sqlCmd += "where a.to_nccc_date  = '' ";
        sqlCmd += "  and a.reject_code   = '' ";
        sqlCmd += "  and a.nccc_filename = '' ";
        sqlCmd += "  and b.acct_type     = a.acct_type ";
        sqlCmd += "  and c.group_code    = decode(a.group_code,'','0000',a.group_code) ";
        sqlCmd += "  and c.card_type     = a.card_type ";
        sqlCmd += "  and d.unit_code     = a.unit_code ";
//        sqlCmd += "  and d.card_type     = a.card_type ";
        sqlCmd += "  and a.apply_source = 'T' ";
        sqlCmd += "order by vendor ";
        vendorCnt = selectTable();
        
        showLogMessage("I", "", "  888 Get vendor cnt ="+vendorCnt);

        for (int i = 0; i < vendorCnt; i++) {
            hVendor = getValue("vendor", i);
            
            if (debug == 1)
                showLogMessage("D", "", " VENDOR=[" + hVendor + "] ");
            
            openTextFile();
            
            process();
            
            nccc.close();
            
            if (hRecCnt1 <= 0) {
                cmdStr = String.format("rm -i -f %s", outFileName);
                if (comc.fileDelete(outFileName) == false) {
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
        
        return;  
    }

    /***********************************************************************/
    void process() throws Exception {
        String prevAcctType = "";
        String hComboCardNo = "";
        int rtn = 0;
        int errFlag = 0;
        int foundAe;
        int temp = 0;

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
        sqlCmd += "a.group_code,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.bin_no,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.apply_id,";
        sqlCmd += "a.apply_id_code,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.mail_zip,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.nation,";
        sqlCmd += "a.business_code,";
        sqlCmd += "decode(a.education,'','6',a.education) h_dces_education,";
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
        sqlCmd += "a.force_flag,";
        sqlCmd += "a.service_code,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.marriage,";
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
        sqlCmd += "rtrim(a.mail_addr1)||" + "rtrim(a.mail_addr2)||" + "rtrim(a.mail_addr3)||" + "rtrim(a.mail_addr4)||" + "rtrim(a.mail_addr5) h_mail_addr,";
        sqlCmd += "a.service_code,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.nccc_type,";
        sqlCmd += "a.old_end_date,";
        sqlCmd += "a.status_code,";
        sqlCmd += "a.reason_code,";
        sqlCmd += "a.ic_flag,";
        sqlCmd += "a.branch,";
        sqlCmd += "a.mail_attach1,";
        sqlCmd += "a.mail_attach2,";
        sqlCmd += "a.rowid as rowid,";
        sqlCmd += "a.csc,";
        sqlCmd += "decode(a.nccc_type,'1',d.new_vendor,'2',d.chg_vendor,d.mku_vendor) vendor ,";
        sqlCmd += "decode(c.chk_nccc_flag,'','N',c.chk_nccc_flag) h_dces_chk_nccc_flag,";
        sqlCmd += "a.ic_cvv,";
        sqlCmd += "a.apply_source,";
        sqlCmd += "a.reg_bank_no,";
        sqlCmd += "a.bill_apply_flag,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.to_nccc_date,";
        sqlCmd += "a.crt_bank_no,";
        sqlCmd += "a.vd_bank_no,";
        sqlCmd += "a.electronic_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.mail_type,";
        sqlCmd += "a.card_ref_num,";
        sqlCmd += "a.mail_branch,";
        sqlCmd += "d.ic_kind ";
        sqlCmd += " from dbc_emboss a,dbp_acct_type b, ptr_group_card c, crd_item_unit d ";
        sqlCmd += "where a.to_nccc_date = '' ";
        sqlCmd += "  and a.reject_code  = '' ";
        sqlCmd += "  and a.nccc_filename = '' ";
        sqlCmd += "  and ((decode(rtrim(a.eng_name,' '),'',' ',rtrim(a.eng_name,' ')) != ' ' ";
        sqlCmd += "        and a.chi_name <> ''  ) or ";
        sqlCmd += "       (decode(rtrim(a.eng_name,' '),'',' ',rtrim(a.eng_name,' '))  = ' ' ";
        sqlCmd += "        and a.chi_name = ''  ) ) ";
        sqlCmd += "  and b.acct_type  = a.acct_type ";
        sqlCmd += "  and c.group_code = decode(a.group_code,'','0000',a.group_code) ";
        sqlCmd += "  and c.card_type  = a.card_type ";
//        sqlCmd += "  and d.card_type  = a.card_type ";
        sqlCmd += "  and d.unit_code  = a.unit_code ";
        sqlCmd += "  and a.apply_source = 'T' ";
        sqlCmd += "order by vendor,a.reg_bank_no,a.bank_actno,a.acct_type,a.card_type ";
        embossCnt = selectTable();

if(debug == 1) showLogMessage("I", "", "  888 ALL cnt=[" + embossCnt  + "]");
        for (int i = 0; i < embossCnt; i++) {
            hDcesBatchno         = getValue("batchno", i);
            hDcesRecno           = getValueDouble("recno", i);
            hDcesEmbossSource   = getValue("emboss_source", i);
            hDcesEmbossReason   = getValue("emboss_reason", i);
            hDcesAcctType       = getValue("acct_type", i);
            hDcesAcctKey        = getValue("acct_key", i);
            hDcesToNcccCode    = getValue("to_nccc_code", i);
            hDcesCardType       = getValue("card_type", i);
            hDcesGroupCode      = getValue("group_code", i);
            hDcesCardNo         = getValue("card_no", i);
            hDcesBinNo          = getValue("bin_no", i);
            hDcesMajorCardNo   = getValue("major_card_no", i);
            hDcesApplyId        = getValue("apply_id", i);
            hDcesApplyIdCode   = getValue("apply_id_code", i);
            hDcesValidFm        = getValue("valid_fm", i);
            hDcesValidTo        = getValue("valid_to", i);
            hDcesMailZip        = getValue("mail_zip", i);
            hDcesBirthday        = getValue("birthday", i);
            hDcesNation          = getValue("nation", i);
            hDcesBusinessCode   = getValue("business_code", i);
            hDcesEducation       = getValue("h_dces_education", i);
            hDcesActNo          = getValue("act_no", i);
            hDcesHomeAreaCode1 = getValue("home_area_code1", i);
            hDcesHomeTelNo1    = getValue("home_tel_no1", i);
            hDcesOrgEmbossData = getValue("org_emboss_data", i);
            hDcesEmboss4ThData = getValue("emboss_4th_data", i);
            hDcesMemberId       = getValue("member_id", i);
            hDcesPmId           = getValue("pm_id", i);
            hDcesPmIdCode      = getValue("pm_id_code", i);
            hDcesCorpNo         = getValue("corp_no", i);
            hDcesCorpNoCode    = getValue("corp_no_code", i);
            hDcesForceFlag      = getValue("force_flag", i);
            hDcesServiceCode    = getValue("service_code", i);
            hDcesEngName        = getValue("eng_name", i);
            hDcesMarriage        = getValue("marriage", i);
            hDcesRelWithPm     = getValue("rel_with_pm", i);
            hDcesUnitCode       = getValue("unit_code", i);
            hDcesSex             = getValue("sex", i);
            hDcesPvv             = getValue("pvv", i);
            hDcesCvv             = getValue("cvv", i);
            hDcesPvki            = getValue("pvki", i);
            hDcesCvv2            = getValue("trans_cvv2", i);
            hDcesOpenNum     = getValue("open_passwd", i);
            hDcesOldCardNo     = getValue("old_card_no", i);
            hDcesChiName        = getValue("chi_name", i);
            hMailAddr            = getValue("h_mail_addr", i);
            hDcesServiceCode    = getValue("service_code", i);
            hDcesSupFlag        = getValue("sup_flag", i);
            hDcesNcccType       = getValue("nccc_type", i);
            hDcesOldEndDate    = getValue("old_end_date", i);
            hDcesStatusCode     = getValue("status_code", i);
            hDcesReasonCode     = getValue("reason_code", i);
            hDcesComboIndicator = "Y";
            hDcesIcFlag         = getValue("ic_flag", i);
            hDcesBranch          = getValue("branch", i);
            hDcesMailAttach1    = getValue("mail_attach1", i);
            hDcesMailAttach2    = getValue("mail_attach2", i);
            hDcesRowid           = getValue("rowid", i);
            hDcesCsc             = getValue("csc", i);
            hDcesVendor          = getValue("vendor", i);
            hDcesChkNcccFlag   = getValue("h_dces_chk_nccc_flag", i);
            hDcesIcCvv          = getValue("ic_cvv", i);
            hDcesApplySource    = getValue("apply_source", i);
            hDcesRegBankNo     = getValue("reg_bank_no", i);
            hDcesBillApplyFlag = getValue("bill_apply_flag", i);   
            hDcesStmtCycle      = getValue("stmt_cycle", i);
            hDcesToNcccDate    = getValue("to_nccc_date", i);
            hDcesCrtBankNo     = getValue("crt_bank_no", i);
            hDcesVdBankNo      = getValue("vd_bank_no", i);
            hDcesElectronicCode = getValue("electronic_code", i);
            hDcesSourceCode     = getValue("source_code", i);
            hDcesMailType       = getValue("mail_type", i);
            hDcesCardRefNum    = getValue("card_ref_num", i);   
            hDcesMailBranch     = getValue("mail_branch", i);
            hUnitIckind         = getValue("ic_kind", i);
            
            if (dataCntTmp == 0) {
            	totalData = embossCnt;
            	dataCntTmp = 1;
            }
            
            showLogMessage("I", "", "  888 card_no = ["+ hDcesCardNo +"]" + hDcesVendor+"," + hVendor);
            showLogMessage("I", "", "  888 act_no = ["+ hDcesActNo +"]");
            if (!hDcesVendor.equals(hVendor)) {
                continue;
            }
       
            /******************************************************
             * 檢查是否為visa card
             ******************************************************/
            visaCard = 0;

            sqlCmd  = "select bin_type     ";
            sqlCmd += "  from ptr_bintable ";
            sqlCmd += " where 1=1          ";
            sqlCmd += "   and bin_no || bin_no_2_fm || '0000' <= ?  ";
            sqlCmd += "   and bin_no || bin_no_2_to || '9999' >= ?  ";
            setString(1, hDcesCardNo);
            setString(2, hDcesCardNo);
            int recordCnt1 = selectTable();
            if (notFound.equals("Y")) {
                showLogMessage("I", "", "select PTR_BINTABLE not found!");
            }
            if (recordCnt1 > 0) {
                hBinType = getValue("bin_type");
            }

            if (hBinType.equals("V"))
                visaCard = 1;

            if (!prevAcctType.equals(hDcesAcctType)) {
                getCardIndicator(hDcesAcctType);
                prevAcctType = hDcesAcctType;
            }

            foundAe = 0;

            if ((hDcesCardNo.length() == 15) && (hDcesCardNo.substring(0, 1).equals("3")))
                foundAe = 1;
            /******************************************************
             * 抓取晶片卡相關資料 ,DEV_KEY expire_date到期不可送 NCCC
             ******************************************************/
            errFlag = 0;
            pKeyType = "";
            pDerivKey = "";
            pIcIndicator = "";
            pServiceCode = "";
            pLOfflnLmt = 0;
            pUOfflnLmt = 0;
if(debug == 1) showLogMessage("I", "", "   888 ic=["+hDcesIcFlag+"]");
//            if (hDcesIcFlag.equals("Y")) {
//                errFlag = getIccardData();
//                if (errFlag != 0)
//                    continue;
//            }

if(debug == 1) 
   showLogMessage("I", "", "   pvv="+hDcesPvv+","+hDcesCvv+","+hDcesCvv2+","+hDcesPvki);

            /**** 無PVV,CVV不可產生資料,不包括不送製卡 ********************/
            if (hDcesToNcccCode.equals("Y")) {
                if (foundAe == 1) {
                    if ((hDcesCsc.length() <= 0) || (hDcesCvv2.length() <= 0) || 
                        (hDcesPvki.length() <= 0)) {
                        continue;
                    }
                } else {
                    if ((hDcesPvv.length()  <= 0) || (hDcesCvv.length() <= 0) || 
                        (hDcesCvv2.length() <= 0) || (hDcesPvki.length() <= 0)) {
                        continue;
                    }
                }
            }

            /*********** 重製中,需告知APS舊卡號重製中 ***************/
            rtn = 0;
            switch (Integer.parseInt(hDcesEmbossSource)) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            	createNcccNewFile();
                break;
            }
            
            if (rtn == 0) {
            	updateDbcEmboss();
            	updateDbcDebit();
                hRecCnt1++;
            }
        }
        
        if (hRecCnt1 > 0)
            insertFileCtl();
        
        return;
    }
    
    /***********************************************************************/
    /***
     * 新製卡(新製卡,普昇金,緊急新製卡,掛失補發卡,緊急補發卡,緊急替代卡)
     * 
     * @throws Exception
     */
    void createNcccNewFile() throws Exception {
        String tmpValue = "";
        String ap1Data = "";
        ap1ReceiveType = "";
        ap1ReceiveBranch = "";
        ap1ReceiveZip = "";    	 	 
        ap1ReceiveAddr = "";

        tempSeq++;
        
        ap1Data = (String) map2.get(hDcesActNo);   
        
        ap1ReceiveType = commStr.mid(ap1Data,904,1);
        ap1ReceiveType = ap1ReceiveType.trim();
        
        ap1ReceiveBranch = commStr.mid(ap1Data,905,4);
        ap1ReceiveBranch = ap1ReceiveBranch.trim();
        
        ap1ReceiveZip = commStr.mid(ap1Data,909,5);
        ap1ReceiveZip = ap1ReceiveZip.trim();
        
        ap1ReceiveAddr = commStr.mid(ap1Data,914,60);
        ap1ReceiveAddr = ap1ReceiveAddr.trim().replace("　", "");
        
        ncccData.seq = String.format("%06d", tempSeq);
        ncccData.cntl1 = "(";
        ncccData.cntl2 = "$";
        ncccData.acct1 = hDcesCardNo.substring(0,4)+" "+ hDcesCardNo.substring(4,8)+" "+ hDcesCardNo.substring(8,12)+" "+ hDcesCardNo.substring(12,16);
        ncccData.cntl3 = "#";
        ncccData.dteEffMm = String.format("%-2.2s", hDcesValidFm.substring(4, 6));
        ncccData.filler1 = "/";
        ncccData.dteEffYy = String.format("%-2.2s", hDcesValidFm.substring(2, 4));
        ncccData.filler2 = "";
        ncccData.dteExpMm = String.format("%-2.2s", hDcesValidTo.substring(4, 6));
        ncccData.filler3 = "/";
        ncccData.dteExpYy = String.format("%-2.2s", hDcesValidTo.substring(2, 4));
        ncccData.type    = "";
        ncccData.code    = "*";
        ncccData.cntl4 = "&";
        ncccData.name    = hDcesEngName;
        ncccData.cntl5 = ")";
        ncccData.acct2 = hDcesCardNo.substring(0,4)+" "+ hDcesCardNo.substring(4,8)+" "+ hDcesCardNo.substring(8,12)+" "+ hDcesCardNo.substring(12,16);
        ncccData.filler4 = "";
        
        tmpValue = comc.transPasswd(1, hDcesCvv2);
        ncccData.cvv2 = tmpValue;

        ncccData.cntl6 = "["; //EBCDIC   X'4A'
        ncccData.t1Hex6C = "%";
        ncccData.t1FmtCode = "B";
        ncccData.t1Acct = hDcesCardNo;
        ncccData.t1Hex5F1 = "^"; //EBCDIC X'5F'
        ncccData.t1Name = hDcesEngName;
        ncccData.t1Hex5F2 = "^"; //EBCDIC X'5F'
        ncccData.t1ExpDate = String.format("%-4.4s", hDcesValidTo.substring(2, 6));
        ncccData.t1SvcCode = hDcesServiceCode;
        ncccData.t1Pvki = hDcesPvki;
        
        tmpValue = comc.transPasswd(1, hDcesPvv);
        ncccData.t1Pvv = tmpValue;
        
        ncccData.t1Unused1 = "00000000";
        ncccData.t1Unused2 = "00";
        
        tmpValue = comc.transPasswd(1, hDcesCvv);
        ncccData.t1Cvv = tmpValue;

        ncccData.t1Unused3 = "00";
        ncccData.t1Aci = "0";
        ncccData.t1Unused4 = "000";
        ncccData.t1Cntl2 = "?";
        ncccData.t2Hex5E = ";";
        ncccData.t2Acct = hDcesCardNo;
        ncccData.t2Hex7E = "=";
        ncccData.t2ExpDate = String.format("%-4.4s", hDcesValidTo.substring(2, 6));
        ncccData.t2SvcCode = hDcesServiceCode;
        ncccData.t2Pvki = hDcesPvki;
        
        tmpValue = comc.transPasswd(1, hDcesPvv);
        ncccData.t2Pvv = tmpValue;
        
        tmpValue = comc.transPasswd(1, hDcesCvv);
        ncccData.t2Cvv = tmpValue;
        
        ncccData.t2Unused = "00000";
        ncccData.t2Hex6F = "?"; //在主機EBCDIC看到是一個'?',內碼為6F;
        ncccData.idNumber = hDcesApplyId;
        
        if(!hDcesBirthday.equals("")) {
            tmpValue = String.format("%03d",(Integer.parseInt(hDcesBirthday.substring(0, 4)) - 1911))+ hDcesBirthday.substring(4, 8);
            ncccData.birthDte = tmpValue;
        }
        else {
        	ncccData.birthDte = hDcesBirthday;
        }
        
        ncccData.photoCard = "N";
        
        if(hDcesSupFlag.equals("0")) {
        	ncccData.prinSupp = "Y";
        }
        else {
        	ncccData.prinSupp = "N";
        }

        ncccData.regBankNo1 = hDcesRegBankNo;
                
        ncccData.zipCode = "";
        
        ncccData.filler5      = ">>>";
        
        tmpValue = comc.transPasswd(1, hDcesIcCvv);
        ncccData.icvv = tmpValue;

        ncccData.e3           = "";
        
        if(selectDbcIdnoStaffFlag(hDcesApplyId, hDcesApplyIdCode)) {
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
        
        ncccData.cmrate = "";
        
        if(hDcesElectronicCode.equals("01")) {
            switch (comcr.str2int(hDcesEmbossSource)) {
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
        
        ncccData.crdTyp = hDcesGroupCode;
        ncccData.crdOrg = hDcesAcctType;
        
        if(hDcesElectronicCode.equals("00")) {
            ncccData.autoloadDefYn = "";
        }else {
            ncccData.autoloadDefYn = "Y";
        }

        ncccData.filler6      = "";
        ncccData.filler7      = "<<<";
        ncccData.filler8      = "";
        ncccData.filler9      = "";
        
        ncccData.id1          = hDcesPmId;
        ncccData.id2          = hDcesApplyId;
        
        ncccData.cardNo = hDcesCardNo;
        ncccData.chiName = hDcesChiName;
        ncccData.engName = hDcesEngName;        
        ncccData.authCreditLmt = "";        
        ncccData.stmtCycle = hDcesStmtCycle;
        ncccData.regBankNo2 = hDcesRegBankNo;
        ncccData.validFm = String.format("%-2.2s%-2.2s", hDcesValidFm.substring(4, 6), hDcesValidFm.substring(2, 4));
        ncccData.validTo = String.format("%-2.2s%-2.2s", hDcesValidTo.substring(4, 6), hDcesValidTo.substring(2, 4));
        
        if((ap1ReceiveType.equals("C"))||(ap1ReceiveType.equals("H"))||(ap1ReceiveType.equals("M"))){
        	String tmpAp1ReceiveAddr = "";
        	String tmpAp1ReceiveAddr60 = "";
        	
        	ncccData.zipCode = ap1ReceiveZip;
        	
        	tmpAp1ReceiveAddr = fixLeft(ap1ReceiveAddr, 60);
        	ncccData.billAddr30 = fixLeft(tmpAp1ReceiveAddr, 30);
        	
        	tmpAp1ReceiveAddr60 = tmpAp1ReceiveAddr.substring(ncccData.billAddr30.length());
        	ncccData.billAddr60 = fixLeft(tmpAp1ReceiveAddr60, 30);
        	
        }
        else {
        	ncccData.zipCode = "";
        	ncccData.billAddr30 = "";
        	ncccData.billAddr60 = "";
        }

        ncccData.unitCode = hDcesUnitCode;
        ncccData.cardType = hDcesCardType;
        ncccData.emboss4ThData = hDcesEmboss4ThData;
        
        if (!hDcesElectronicCode.equals("00")) {
        	selectPtrSysParm();
        	if(tmpWfValue.equals("Y")) {
        		if(hDcesElectronicCode.equals("01")) {
        			selectTscVdCardNo();
                	ncccData.electronicCardNo = hTscCardNo;
        		}
         	}
        	else {
        		ncccData.electronicCardNo = "";
        	}
        }
        else {
        	ncccData.electronicCardNo = "";
        }
        
        ncccData.electronicCode = hDcesElectronicCode;
        ncccData.sourceCode = hDcesSourceCode;

        if(ap1ReceiveType.equals("E")) {
        	tmpMailType = "4";
        	tmpBranch = hDcesMailBranch;
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
        if(map2.get(hDcesActNo) != null) {
        	data2 = data + map2.get(hDcesActNo);
        	totalOutputFile++;
        	nccc.write(data2 + "\r\n"); //因應卡部需求調整為0D0A
        }else {
        	rtn = 1;
        }        

        return;
    }
    
    /***********************************************************************/
    boolean selectDbcIdnoStaffFlag(String id, String idCode) throws Exception {
        String hIdnoId = id;
        String hIdnoIdCode = idCode;
        
        sqlCmd = "select staff_flag ";
        sqlCmd += " from dbc_idno ";
        sqlCmd += " where id_no = ? ";
        sqlCmd += " and id_no_code = ? ";
        setString(1, hIdnoId);
        setString(2, hIdnoIdCode);
        int recordCnt = selectTable();
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
    void selectPtrSysParm() throws Exception 
    {
      tmpWfValue = "N";
      
      sqlCmd  = "select wf_value ";
      sqlCmd += "  from ptr_sys_parm   ";
      sqlCmd += " where wf_parm = 'SYSPARM'  ";
      sqlCmd += "   and wf_key = 'VD_ELEC_CARD_NO' ";
      int recordCnt = selectTable();
      if (recordCnt > 0) {
    	  tmpWfValue = getValue("wf_value");
      }
      return;
    }
    /***********************************************************************/
    void selectTscVdCardNo() throws Exception{
    	hTscCardNo ="";        
        selectSQL = " tsc_card_no ";
        daoTable  = " tsc_dcrp_log ";
        whereStr  = " where card_no = ? ";       
        setString(1, hDcesCardNo);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_dcrp_log not found!", hDcesCardNo, hCallBatchSeqno);
        }        
        hTscCardNo = getValue("tsc_card_no");
    }
    
    /***********************************************************************/
    void selectDbaAcno() throws Exception{    	
        
        sqlCmd  = " select bill_sending_zip, bill_sending_addr1, bill_sending_addr2,  ";
        sqlCmd += " bill_sending_addr3, bill_sending_addr4, bill_sending_addr5 ";
        sqlCmd += " from dba_acno ";
        sqlCmd += " where acct_key = ? ";
        setString(1, hDcesApplyId + "0");
        
        if (selectTable() > 0) {
        	acnoBillSendingZip   = getValue("bill_sending_zip");
        	acnoBillSendingAddr1 = getValue("bill_sending_addr1");
        	acnoBillSendingAddr2 = getValue("bill_sending_addr2");
        	acnoBillSendingAddr3 = getValue("bill_sending_addr3");
        	acnoBillSendingAddr4 = getValue("bill_sending_addr4");
        	acnoBillSendingAddr5 = getValue("bill_sending_addr5");
        }
        else {
        	acnoBillSendingZip = "";
        	acnoBillSendingAddr1 = "";
        	acnoBillSendingAddr2 = "";
        	acnoBillSendingAddr3 = "";
        	acnoBillSendingAddr4 = "";
        	acnoBillSendingAddr5 = "";
        }
    }
        
    /***********************************************************************/    
    void selectDbcIdno() throws Exception{
    	
    	sqlCmd  = " select home_area_code1, home_tel_no1, home_tel_ext1, ";
    	sqlCmd += " office_area_code1, office_tel_no1, office_tel_ext1 ";
    	sqlCmd += " from dbc_idno ";
    	sqlCmd += " where id_no = ? ";
        setString(1, hDcesApplyId);
        if (selectTable() > 0) {    
            idnoHomeAreaCode1 = getValue("home_area_code1");
            idnoHomeTelNo1 = getValue("home_tel_no1");
            idnoHomeTelExt1 = getValue("home_tel_ext1");
            idnoOfficeAreaCode1 = getValue("office_area_code1");
            idnoOfficeTelNo1 = getValue("office_tel_no1");
            idnoOfficeTelExt1 = getValue("office_tel_ext1");
        }
    }

    /***********************************************************************/
    void updateDbcEmboss() throws Exception {
        int type = 0;
        if (hDcesToNcccCode.equals("Y"))
            hDcesNcccFilename = hNcccFilename;
        if (!hDcesEmbossSource.equals("5")) {
            type = 1;
        }
        if (hDcesEmbossSource.equals("5")) {
            if (hDcesEmbossReason.equals("2"))
                type = 1;
            else
                type = 0;
        }
        if (type == 0) {
            daoTable = "dbc_emboss";
            updateSQL = "to_nccc_date = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " nccc_filename = ?,";
            updateSQL += " emboss_4th_data = ?,";
            updateSQL += " major_card_no = ?,";
            updateSQL += " ic_indicator = ?,";
            updateSQL += " key_type  = ?,";
            updateSQL += " deriv_key = ?,";
            updateSQL += " l_offln_lmt = ?,";
            updateSQL += " u_offln_lmt = ?,";
            updateSQL += " pvv  = '',";
            updateSQL += " cvv  = '',";
            updateSQL += " csc  = '',";
            updateSQL += " ic_cvv = '',";
            updateSQL += " diff_code = '',";
            updateSQL += " chk_nccc_flag = ?,";
            updateSQL += " vendor = ?,";
            updateSQL += " mail_type = ?,";
            updateSQL += " mail_branch = ?,";
            updateSQL += " mod_time = sysdate,";
            updateSQL += " mod_user = ?,";
            updateSQL += " mod_pgm = ?";
            whereStr = "where rowid = ? ";
            setString(1, hDcesNcccFilename);
            setString(2, hDcesEmboss4ThData);
            setString(3, hDcesMajorCardNo);
            setString(4, pIcIndicator);
            setString(5, pKeyType);
            setString(6, pDerivKey);
            setInt(7, pLOfflnLmt);
            setInt(8, pUOfflnLmt);
            setString(9, hDcesChkNcccFlag);
            setString(10, hDcesVendor);
            setString(11, tmpMailType);
            setString(12, tmpBranch);
            setString(13, hModUser);
            setString(14, prgmId);
            setRowId(15, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
            }
        } else {
            daoTable = "dbc_emboss";
            updateSQL = "to_nccc_date = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " nccc_filename = ?,";
            updateSQL += " emboss_4th_data = ?,";
            updateSQL += " ic_indicator = ?,";
            updateSQL += " key_type  = ?,";
            updateSQL += " deriv_key = ?,";
            updateSQL += " l_offln_lmt = ?,";
            updateSQL += " u_offln_lmt = ?,";
            updateSQL += " pvv  = '',";
            updateSQL += " cvv  = '',";
            updateSQL += " csc  = '',";
            updateSQL += " ic_cvv = '',";
            updateSQL += " diff_code = '',";
            updateSQL += " chk_nccc_flag = ?,";
            updateSQL += " vendor = ?,";
            updateSQL += " mail_type = ?,";
            updateSQL += " mail_branch = ?,";
            updateSQL += " mod_time = sysdate,";
            updateSQL += " mod_user = ?,";
            updateSQL += " mod_pgm = ?";
            whereStr = "where rowid = ? ";
            setString(1, hDcesNcccFilename);
            setString(2, hDcesEmboss4ThData);
            setString(3, pIcIndicator);
            setString(4, pKeyType);
            setString(5, pDerivKey);
            setInt(6, pLOfflnLmt);
            setInt(7, pUOfflnLmt);
            setString(8, hDcesChkNcccFlag);
            setString(9, hDcesVendor);
            setString(10, tmpMailType);
            setString(11, tmpBranch);
            setString(12, hModUser);
            setString(13, prgmId);
            setRowId(14, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
            }
        }
    }
    
    /***********************************************************************/
    void updateDbcDebit() throws Exception {
        daoTable = "dbc_debit";
        updateSQL = " to_nccc_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr = "where card_no   = ? ";
        setString(1, javaProgram);
        setString(2, hDcesCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_debit not found!", hDcesCardNo, comcr.hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
//    int getIccardData() throws Exception {
//        pExpireDate = "";
//        pCheckKeyExpire = "";
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
//        setString(1, hDcesCardType);
//        setString(2, hDcesUnitCode);
//        setString(3, hBinType);
//
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("get_iccard_data() not found!", hDcesUnitCode, hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            pServiceCode     = getValue("service_code");
//            pKeyType         = getValue("key_type");
//            pDerivKey        = getValue("deriv_key");
//            pLOfflnLmt      = getValueInt("l_offln_lmt");
//            pUOfflnLmt      = getValueInt("u_offln_lmt");
//            pIcIndicator     = getValue("ic_indicator");
//            pExpireDate      = getValue("expire_date");
//            pCheckKeyExpire = getValue("check_key_expire");
//        }
//
//        if (pCheckKeyExpire.equals('Y') && pExpireDate.compareTo(hDcesValidTo) < 0) {
//            return (1);
//        }
//        return (0);
//    }
    
    /***********************************************************************/
    void getCardIndicator(String acctType) throws Exception {
        String gAcctType = "";
        gAcctType = "";
        hCardIndicator = "";
        gAcctType = acctType;
        sqlCmd = "select card_indicator ";
        sqlCmd += " from dbp_acct_type  ";
        sqlCmd += "where acct_type = ? ";
        setString(1, gAcctType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbp_acct_type not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCardIndicator = getValue("card_indicator");
        }
        return;
    }
    
    /***********************************************************************/
    void insertFileCtl1(String fileName) throws Exception {
    	
        setValue("file_name", fileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", totalInputFile);
        setValueInt("record_cnt", totalInputFile);
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
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL = "head_cnt = ?,";
            updateSQL += " record_cnt = ?,";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setInt(1, hRecCnt1);
            setInt(2, hRecCnt1);
            setString(3, hNcccFilename);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }   

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD063 proc = new DbcD063();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class buf1 {
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
    }
    
    /****************************************************************************/
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

    /****************************************************************************/
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
    
    /****************************************************************************/
    void renameFile(String removeFileName) throws Exception {
        String tmpstr1 = comc.getECSHOME() + "/media/dbc/" + removeFileName;
        String tmpstr2 = comc.getECSHOME() + "/media/dbc/backup/" + removeFileName;
        
        if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }
    
    /****************************************************************************/
    String[] getFieldValue(String rec, int[] parm) {
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
    
    /****************************************************************************/
    private Map processDataRecord(String[] row, String[] dT) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (String s : dT) {
            map.put(s.trim(), row[i]);
            i++;
        }
        return map;
    }
    /***********************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "MAKECARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/dbc", comc.getECSHOME());
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
  		String tmpstr1 = comc.getECSHOME() + "/media/dbc/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/dbc/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}
}
