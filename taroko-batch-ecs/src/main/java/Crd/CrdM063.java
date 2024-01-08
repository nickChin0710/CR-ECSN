/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/05  V1.00.00   Pino       program initial                           *
*  109/03/23  V1.00.01   Wilson     select vendor修改、帳單地址兩段30碼                                       *
*  109/12/24  V1.00.02   shiyuqi       updated for project coding standard   *
*  111/12/28  V1.00.03   Wilson     調整為最新格式                                                                                         *    
*  112/03/13  V1.00.04   Wilson     新增procFTP                                *
*  112/04/23  V1.00.05   Wilson     調整產檔路徑                                                                                              *
*  112/05/02  V1.00.06   Wilson     調整track2格式                                                                                    *
*  112/05/22  V1.00.07   Wilson     english2後面加?                                                                                 *
*  112/05/25  V1.00.08   Wilson     稱謂、英文姓名調整                                                                                   *
*  112/12/03  V1.00.09   Wilson     crd_item_unit不判斷卡種                                                              *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommRoutine;
import com.CommSecr;

import Crd.CrdD061.Buf1;

public class CrdM063 extends AccessDAO {
    private String progname = "產生龍騰卡新製/補發製卡檔程式    112/12/03  V1.00.09";
    CommCrd comc = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommSecr comsecr = null;
    Buf1 ncccData = new Buf1();
    BufferedWriter nccc = null;
    CommFTP commFTP = null;
    String hTempUser = "";

    String errMsg = "";
    String stderr = "";
    String hModUser = "";
    String hCallBatchSeqno = "";

    String hVendor = "";
    String hNcccFilename = "";
    int hNn = 0;
    int totCnt = 0;
    String hYyyymmdd = "";
    String hEmbpBatchno = "";
    double hEmbpRecno = 0;
    String hEmbpEmbossSource = "";
    String hEmbpUnitCode = "";
    String hEmbpCardType = "";
    String hEmbpGroupCode = "";
    String hEmbpPpCardNo = "";
    String hEmbpId = "";
    String hEmbpIdCode = "";
    String hEmbpValidFm = "";
    String hEmbpValidTo = "";
    String hEmbpZipCode = "";
    String hEmbpBinType = "";
    String hEmbpMailBranch = "";
    String hEmbpMailAddr1 = "";
    String hEmbpMailAddr2 = "";
    String hEmbpMailAddr3 = "";
    String hEmbpMailAddr4 = "";
    String hEmbpMailAddr5 = "";
    String hIdnoBirthday = "";
    String hEmbpEngName = "";
    String hUdnoChiName = "";
    String hMailAddr = "";
    String hEmbpRowid = "";
    String hIdnoSex = "";

    String temstr = "";
    String tempSlip = "";
    String ftpStr = "";
    int errCode = 0;

    int seq = 0;

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : CrdM063  [test] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comsecr = new CommSecr(getDBconnect(), getDBalias());
            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 1);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            hModUser = comc.commGetUserID();
            if (hTempUser.length() == 0) {
                hTempUser = hModUser;
            }
            sqlCmd = "select ";
            sqlCmd += "distinct decode(a.emboss_source,'1',d.new_vendor,'5',d.mku_vendor,d.chg_vendor) vendor  ";
            sqlCmd += "from crd_emboss_pp a, ptr_group_card c, crd_item_unit d ";
            sqlCmd += "where a.to_vendor_flag != 'Y' ";
            sqlCmd += "and c.group_code = decode(a.group_code,'','0000',a.group_code) ";
            sqlCmd += "and c.card_type = a.card_type ";
//            sqlCmd += "and d.card_type = a.card_type ";
            sqlCmd += "and d.unit_code = a.unit_code ";
            sqlCmd += "and a.vip_kind = '2' ";
            sqlCmd += "order by vendor ";
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hVendor = getValue("vendor", i);
                if (openTextFile() != 0) {
                    errMsg = String.format("open_text_file        error");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }

                process();
                nccc.close();

                if (totCnt <= 0) {
                    comc.fileDelete(temstr);
                }
                else {
            		commFTP = new CommFTP(getDBconnect(), getDBalias());
            	    comr = new CommRoutine(getDBconnect(), getDBalias());
                    procFTP();
            	    renameFile1(hNcccFilename);
                }
            }
            
            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]\n", totCnt));

            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
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
    int openTextFile() throws Exception {
        int tmpVendor;

        tmpVendor = comcr.str2int(hVendor);
        hYyyymmdd = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_yyyymmdd ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hYyyymmdd = getValue("h_yyyymmdd");
        }
        checkFileCtl();
        
        hNcccFilename = String.format("dp_%02d_makecard_ns_%8s%02d.txt", tmpVendor, hYyyymmdd, hNn);

        temstr = String.format("%s/media/crd/%s", comc.getECSHOME(), hNcccFilename);
        
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

        nccc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temstr), "MS950"));

        return (0);
    }

    /***********************************************************************/
    void checkFileCtl() throws Exception {
        int tmpVendor;
        tmpVendor = comcr.str2int(hVendor);
    	String likeFilename = "";
    	String hFileName = "";
    	likeFilename = String.format("dp_%02d_makecard_ns_%8s", tmpVendor, hYyyymmdd)+"%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        int tmpInt = selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
            hFileName = getValue("file_name");
            hNn = Integer.valueOf(hFileName.substring(26, 28))+1;
        }

    }

    /***********************************************************************/
    void process() throws Exception {
        totCnt = 0;
        sqlCmd = "select ";
        sqlCmd += "a.batchno,";
        sqlCmd += "a.recno,";
        sqlCmd += "a.emboss_source,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.pp_card_no,";
        sqlCmd += "a.id_no,";
        sqlCmd += "a.id_no_code,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.zip_code,";
        sqlCmd += "a.bin_type,";
        sqlCmd += "a.mail_branch,";
        sqlCmd += "b.birthday,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "b.chi_name,";
        sqlCmd += "rtrim(a.mail_addr1)||" + "rtrim(a.mail_addr2)||" + "rtrim(a.mail_addr3)||" + "rtrim(a.mail_addr4)||"
                + "rtrim(a.mail_addr5) h_mail_addr,";
        sqlCmd += "a.mail_addr1,";
        sqlCmd += "a.mail_addr2,";
        sqlCmd += "a.mail_addr3,";
        sqlCmd += "a.mail_addr4,";
        sqlCmd += "a.mail_addr5,";
        sqlCmd += "b.sex,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += "from ptr_group_card c,crd_idno b,crd_emboss_pp a ,crd_item_unit d ";
        sqlCmd += "where a.to_vendor_flag != 'Y' ";
        sqlCmd += "and a.in_main_error = '0' ";
        sqlCmd += "and b.id_no  = a.id_no ";
        sqlCmd += "and b.id_no_code  = a.id_no_code ";
        sqlCmd += "and c.group_code = decode(a.group_code,'','0000',a.group_code) ";
//        sqlCmd += "and d.card_type = a.card_type ";
        sqlCmd += "and d.unit_code = a.unit_code ";
        sqlCmd += "and a.emboss_source not in ('3','4') ";
        sqlCmd += "and decode(a.emboss_source,'1',d.new_vendor,'5',d.mku_vendor,d.chg_vendor) = ? ";
        sqlCmd += "and a.vip_kind = '2' ";
        sqlCmd += "order by a.card_type,a.unit_code ";
            setString(1, hVendor);
        openCursor();
        while(fetchTable()) {
            hEmbpBatchno = getValue("batchno");
            hEmbpRecno = getValueDouble("recno");
            hEmbpEmbossSource = getValue("emboss_source");
            hEmbpUnitCode = getValue("unit_code");
            hEmbpCardType = getValue("card_type");
            hEmbpGroupCode = getValue("group_code");
            hEmbpPpCardNo = getValue("pp_card_no");
            hEmbpId = getValue("id_no");
            hEmbpIdCode = getValue("id_no_code");
            hEmbpValidFm = getValue("valid_fm");
            hEmbpValidTo = getValue("valid_to");
            hEmbpZipCode = getValue("zip_code");
            hEmbpBinType = getValue("bin_type");
            hEmbpMailBranch = getValue("mail_branch");            
            hIdnoBirthday = getValue("birthday");
            hEmbpEngName = getValue("eng_name");
            hUdnoChiName = getValue("chi_name");
            hMailAddr = getValue("h_mail_addr");
            hEmbpMailAddr1 = getValue("mail_addr1");
            hEmbpMailAddr2 = getValue("mail_addr2");
            hEmbpMailAddr3 = getValue("mail_addr3");
            hEmbpMailAddr4 = getValue("mail_addr4");
            hEmbpMailAddr5 = getValue("mail_addr5");
            hIdnoSex = getValue("sex");
            hEmbpRowid = getValue("rowid");

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]\n", totCnt));
            }

            createNcccNewFile();

            updateCrdEmbossPp();
        }
        closeCursor();
    
        if (totCnt > 0) {
            insertFileCtl();        	
        }

        return;
    }

    /***********************************************************************/
    void createNcccNewFile() throws Exception {
    	String tmpTitle = "";
    	
        seq++;
        ncccData.seq                  = String.format("%06d", seq);
    	ncccData.ppCardNo           = hEmbpPpCardNo;
    	ncccData.filler1        	   = "";
    	
    	if(hIdnoSex.equals("2")) {
    		tmpTitle = "MS.";
    	}
    	else {
    		tmpTitle = "MR.";
    	}
    	
    	ncccData.title1        		   = tmpTitle;
    	ncccData.english1            = hEmbpEngName;
    	ncccData.filler2    	       = "";
    	ncccData.expMm1       	   = hEmbpValidTo.substring(4, 6);
    	ncccData.filler3    		   = "/";
    	ncccData.expYy1       	   = hEmbpValidTo.substring(2, 4);
    	ncccData.filler4    		   = "%PP";
    	ncccData.filler5    		   = "/";
    	ncccData.title2        		   = tmpTitle;
    	ncccData.filler6    	       = "/";
    	ncccData.english2            = hEmbpEngName.replaceFirst(" ","//") + "?";
    	ncccData.filler7         	   = ";";
    	ncccData.track2      = hEmbpPpCardNo + "=" + hEmbpValidTo.substring(4, 6) + "20" + hEmbpValidTo.substring(2, 4) + "?";
    	ncccData.vBranch         	   = hEmbpMailBranch;
    	ncccData.vZip                = hEmbpZipCode;
    	ncccData.vChineseName       = hUdnoChiName;
    	ncccData.filler12      	   = ">>>";
    	
    	String tmpEcsReceiveAddr = "";
    	String tmpEcsReceiveAddr60 = "";
    	   	
    	tmpEcsReceiveAddr = hEmbpMailAddr1.trim() + hEmbpMailAddr2.trim() +
    			            hEmbpMailAddr3.trim() + hEmbpMailAddr4.trim() +
    			            hEmbpMailAddr5.trim();
    	tmpEcsReceiveAddr = fixAllLeft(tmpEcsReceiveAddr, 60);
    	ncccData.billAddr30 = fixAllLeft(tmpEcsReceiveAddr, 30);
    	
    	tmpEcsReceiveAddr60 = tmpEcsReceiveAddr.substring(ncccData.billAddr30.length());
    	ncccData.billAddr60 = fixAllLeft(tmpEcsReceiveAddr60, 30);     
    	
    	ncccData.filler13    	       = ">>>";
    	ncccData.filler14    	       = ">>>";
    	
        String data = ncccData.allText();
        ncccData.initString();
        nccc.write(data + "\r\n"); //因應卡部需求調整為0D0A

        return;
    }
    /***********************************************************************/
    void updateCrdEmbossPp() throws Exception {

        daoTable = "crd_emboss_pp";
        updateSQL = " to_vendor_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " to_vendor_flag = 'Y',";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_user    = ?,";
        updateSQL += " mod_pgm     = ? ";
        whereStr = "where rowid  = ? ";
        setString(1, hModUser);
        setString(2, javaProgram);
        setRowId(3, hEmbpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss_pp not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertFileCtl() throws Exception {
        daoTable = "crd_file_ctl";
        setValue("file_name", hNcccFilename);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", totCnt);
        setValueInt("record_cnt", totCnt);
        setValue("trans_in_date", sysDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL = "head_cnt  = ?,";
            updateSQL += " record_cnt = ?,";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name  = ? ";
            setInt(1, totCnt);
            setInt(2, totCnt);
            setString(3, hNcccFilename);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }
    /***********************************************************************/
    class Buf1 {
    	String seq;
        String ppCardNo;
        String filler1;
        String title1;
        String english1;
        String filler2;
        String expMm1;
        String filler3;
        String expYy1;
        String filler4;
        String filler5;
        String title2;
        String filler6;
        String english2;
        String filler7;
        String track2;
        String vBranch;
        String vZip;
        String vChineseName;
        String filler12;
        String billAddr30;
        String filler13;
        String billAddr60;
        String filler14;
        String filler15;
        
        void initString() {
        	seq          		 = "";
        	ppCardNo           = "";
        	filler1        		 = "";
        	title1        		 = "";
        	english1            = "";
        	filler2    		     = "";
        	expMm1       	     = "";
        	filler3    		     = "";
        	expYy1       		 = "";
        	filler4    		     = "";
        	filler5    		     = "";
        	title2        		 = "";
        	filler6    	         = "";
        	english2            = "";
        	filler7         	 = "";
        	track2          	 = "";
        	vBranch         	 = "";
        	vZip                = "";
        	vChineseName       = "";
        	filler12      	     = "";
        	billAddr30			 = "";
        	filler13    	     = "";
        	billAddr60			 = "";
        	filler14    	     = "";
        	filler15    	     = "";
        	
        }
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(seq, 6);
            rtn += fixLeft(ppCardNo, 16);
            rtn += fixLeft(filler1, 4);
            rtn += fixLeft(title1, 3);
            rtn += fixLeft(english1, 26);
            rtn += fixLeft(filler2, 31);
            rtn += fixLeft(expMm1, 2);
            rtn += fixLeft(filler3, 1);
            rtn += fixLeft(expYy1, 2);
            rtn += fixLeft(filler4, 3);
            rtn += fixLeft(filler5, 1);
            rtn += fixLeft(title2, 3);
            rtn += fixLeft(filler6, 1);
            rtn += fixLeft(english2, 62);
            rtn += fixLeft(filler7, 1);
            rtn += fixLeft(track2, 26);
            rtn += fixLeft(vBranch, 4);
            rtn += fixLeft(vZip, 6);
            rtn += fixLeft(vChineseName, 30);
            rtn += fixLeft(filler12, 3);
            rtn += fixLeft(billAddr30, 30);
            rtn += fixLeft(filler13, 3);
            rtn += fixLeft(billAddr60, 30);
            rtn += fixLeft(filler14, 3);
            rtn += fixLeft(filler15, 103);
            
            return rtn;
        }
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
            	            
          /****************************************************************************/        
    public static void main(String[] args) throws Exception {
    	CrdM063 proc = new CrdM063();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
