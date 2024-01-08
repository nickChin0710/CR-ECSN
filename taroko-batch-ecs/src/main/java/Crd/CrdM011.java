/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  106/06/01  V1.00.00    SUP       program initial                         *
*  107/11/21  V1.07.01    詹曜維    BECS-1071120-084 emboss_soucre '5' check*
*  108/05/13  V1.07.01    Brian     update to V1.07.01                      *
*  109/02/03  V1.07.01    Rou        Update、add crd_emboss_pp()             *
*  109/03/17  V1.08.01    Wilson     insert id改id_p_seqno                   *
*  109/12/24  V1.00.02   shiyuqi       updated for project coding standard   *
*  112/04/23  V2.00.03   Wilson      where條件刪除bin_type                     *
*  112/04/24  V2.00.04   Wilson      hEmbpPpCardNo -> hEmbpOldCardNo        * 
*  112/06/14  V2.00.05   Wilson      apply_credit_card_no改成card_no         *   
*  112/07/26  V2.00.06   Wilson      增加毀損補發處理                                                                             *
****************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

/*PP卡*/
public class CrdM011 extends AccessDAO {
    public static final boolean debugMode = false;
    private String progname = "貴賓卡入主檔作業 112/07/26  V1.00.06 ";
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String hTempUser = "";

    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";

    String hBusinessDate = "";
    String hSystemDate = "";
    String hCrdpBinType = "";
    String hEmbpBatchno = "";
    int hEmbpRecno = 0;
    String hEmbpEmbossSource = "";
    String hEmbpPpCardNo = "";
    String hEmbpId = "";
    String hEmbpIdCode = "";
    String hEmbpCardType = "";
    String hEmbpUnitCode = "";
    String hEmbpGroupCode = "";
    String hEmbpSourceCode = "";
    String hEmbpCardItem = "";
    String hEmbpEngName = "";
    String hEmbpZipCode = "";
    String hEmbpChangeReason = "";
    String hEmbpChangeStatus = "";
    String hEmbpMailAddr1 = "";
    String hEmbpMailAddr2 = "";
    String hEmbpMailAddr3 = "";
    String hEmbpMailAddr4 = "";
    String hEmbpMailAddr5 = "";
    String hEmbpValidFm = "";
    String hEmbpValidTo = "";
    String hEmbpMailType = "";
    String hEmbpMailNo = "";
    String hEmbpMailBranch = "";
    String hEmbpMailProcDate = "";
    String hEmbpBarcodeNum = "";
    String hEmbpOldCardNo = "";
    String hEmbpOldBegDate = "";
    String hEmbpOldEndDate = "";
    String hEmbpVipKind = "";
    String hEmbpApplyCreditCardNo = "";
    String hEmbpMakecardFee = "";
    String hEmbpReissueReason = "";
    String hEmbpRowid = "";
    String hChangeReason = "";
    String hPSeqno = "";
    String hAcctType = "";
    int tempInt = 0;
    String hError = "";
    int totCnt = 0;
    int rtn1 = 0;
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : CrdM011 [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
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

            commonRtn();

            showLogMessage("I", "", String.format("Process date = [%s]\n", hSystemDate));

            selectCrdEmbossPp();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update" + daoTable + "not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }
    }

    /***********************************************************************/
    void selectCrdEmbossPp() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "batchno, ";
        sqlCmd += "recno, ";
        sqlCmd += "emboss_source, ";
        sqlCmd += "pp_card_no, ";
        sqlCmd += "id_no, ";
        sqlCmd += "id_no_code, ";
        sqlCmd += "card_type, ";
        sqlCmd += "unit_code, ";
        sqlCmd += "group_code, ";
        sqlCmd += "source_code, ";
        sqlCmd += "card_item, ";
        sqlCmd += "eng_name, ";
        sqlCmd += "zip_code, ";
        sqlCmd += "change_reason, ";
        sqlCmd += "change_status, ";
        sqlCmd += "mail_addr1, ";
        sqlCmd += "mail_addr2, ";
        sqlCmd += "mail_addr3, ";
        sqlCmd += "mail_addr4, ";
        sqlCmd += "mail_addr5, ";
        sqlCmd += "valid_fm, ";
        sqlCmd += "valid_to, ";
        sqlCmd += "mail_type, ";
        sqlCmd += "mail_no, ";
        sqlCmd += "mail_branch, ";
        sqlCmd += "mail_proc_date, ";
        sqlCmd += "barcode_num, ";
        sqlCmd += "old_card_no, ";
        sqlCmd += "old_beg_date, ";
        sqlCmd += "old_end_date, ";
        sqlCmd += "vip_kind, ";
        sqlCmd += "makecard_fee, ";
        sqlCmd += "card_no, ";
        sqlCmd += "reissue_reason, ";
        sqlCmd += "rowid  rowid ";
        sqlCmd += " from crd_emboss_pp ";
        sqlCmd += "  where decode(in_main_error,'','0',in_main_error) = '0' ";
        sqlCmd += "  and in_main_date = '' ";
        // in_main_date is null 改為 in_main_date = ''; //SUP!
        openCursor();
        while(fetchTable()) {
            hEmbpBatchno       = getValue("batchno");
            hEmbpRecno         = getValueInt("recno");
            hEmbpEmbossSource = getValue("emboss_source");
            hEmbpPpCardNo    = getValue("pp_card_no");
            hEmbpId            = getValue("id_no");
            hEmbpIdCode       = getValue("id_no_code");
            hEmbpCardType     = getValue("card_type");
            hEmbpUnitCode     = getValue("unit_code");
            hEmbpGroupCode    = getValue("group_code");
            hEmbpSourceCode   = getValue("source_code");
            hEmbpCardItem     = getValue("card_item");
            hEmbpEngName      = getValue("eng_name");
            hEmbpZipCode      = getValue("zip_code");
            hEmbpChangeReason = getValue("change_reason");
            hEmbpChangeStatus = getValue("change_status");
            hEmbpMailAddr1 = getValue("mail_addr1");
            hEmbpMailAddr2 = getValue("mail_addr2");
            hEmbpMailAddr3 = getValue("mail_addr3");
            hEmbpMailAddr4 = getValue("mail_addr4");
            hEmbpMailAddr5 = getValue("mail_addr5");
            hEmbpValidFm   = getValue("valid_fm");
            hEmbpValidTo   = getValue("valid_to");
            hEmbpMailType  = getValue("mail_type");
            hEmbpMailNo    = getValue("mail_no");
            hEmbpMailBranch    = getValue("mail_branch");
            hEmbpMailProcDate = getValue("mail_proc_date");
            hEmbpBarcodeNum    = getValue("barcode_num");
            hEmbpOldCardNo    = getValue("old_card_no");
            hEmbpOldBegDate   = getValue("old_beg_date");
            hEmbpOldEndDate   = getValue("old_end_date");
            hEmbpVipKind       = getValue("vip_kind");
            hEmbpApplyCreditCardNo   = getValue("card_no");
            hEmbpMakecardFee   = getValue("makecard_fee");
            hEmbpReissueReason  = getValue("reissue_reason");
            hEmbpRowid          = getValue("rowid");
            
            showLogMessage("I", "", "pp_card_no = [" + hEmbpPpCardNo + "]");
            
            totCnt++;

            hCrdpBinType = "";

            sqlCmd = "select DISTINCT bin_type ";
            sqlCmd += " from mkt_ppcard_issue  ";
            sqlCmd += "where ppcard_bin_no  = substr(?,1,6) ";
            setString(1, hEmbpPpCardNo);
            if (selectTable() > 0) {
                hCrdpBinType = getValue("bin_type");
            }
           
            rtn1 = chkCrdCard();
            if (rtn1 == 0) {
                hError = "1";
                updateEmboss();
                continue;
            }
            
            if (hEmbpOldCardNo.length() > 0) {
            	if (hEmbpEmbossSource.equals("3") || hEmbpEmbossSource.equals("4")) {
	                rtn1 = chkOldCrdCardPp();
	                if (rtn1 == 0) {
	                    hError = "2";
	                    updateEmboss();
	                    continue;
	                }
	                
	                updateChgCrdCardPp(1);
            	}
            	else if(hEmbpEmbossSource.equals("5") && hEmbpReissueReason.equals("2")) {	                
	                updateChgCrdCardPp(2);
            	}
            	else {
            		updateRenewOldCrdCardPp();
            	}            		
            }           
	            
            if (!hEmbpEmbossSource.equals("3") || !hEmbpEmbossSource.equals("4")) {
            	if(!hEmbpReissueReason.equals("2")) {
                	insertCrdCardPp();
                }
            }
	            	
            daoTable   = "crd_emboss_pp ";
            updateSQL  = "in_main_date      = ?, ";
            updateSQL += "mail_proc_date    = ?, ";
            updateSQL += "bin_type          = ? ";
            whereStr   = "where rowid       = ? ";            
            setString(1, hSystemDate);
            setString(2, hSystemDate);
            setString(3, hCrdpBinType);
            setRowId(4, hEmbpRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_emboss_pp  not found!", "", hCallBatchSeqno);
            }
            
            if (hEmbpMakecardFee.equals("Y")) {
            	insertBilSysexp(hEmbpPpCardNo);
            }
            	            
            if (hEmbpVipKind.equals("2")) {
            	insertCrdDpDragon();
            }            	
        }
        closeCursor();
    }
    
    /***********************************************************************/
    int updateRenewOldCrdCardPp() throws Exception {
        
        daoTable   = "crd_card_pp";
        updateSQL  = "new_card_no      = ? , ";
        updateSQL += "reissue_date     = ? , ";
        updateSQL += "reissue_status   = '3' ";        
        whereStr   = "where pp_card_no = ?   ";
        setString(1, hEmbpPpCardNo);
        setString(2, hSystemDate);
        setString(3, hEmbpOldCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_renew_old_crd_card_pp not found!", "", hCallBatchSeqno);
        }
        return (0);
    }
    
    /***********************************************************************/
    void insertBilSysexp(String ppCardNo) throws Exception {
    	long hMakeFee = 0;
    	String hCardNo = "";
        
        sqlCmd  = "select card_no ";
        sqlCmd += " from crd_emboss_pp a, crd_card b";
        sqlCmd += " where a.pp_card_no = ? ";
        sqlCmd += " and a.card_no = b.card_no ";
        sqlCmd += " and b.current_code = '0' ";
        setString(1, hEmbpPpCardNo); 
        int recCnt = selectTable();
        if (recCnt > 0)
        	hCardNo = getValue("card_no");
        else {
        	sqlCmd  = "select card_no ";
            sqlCmd += " from crd_card ";
            sqlCmd += " where id_p_seqno = ? ";
            sqlCmd += " and card_type in (select card_type from mkt_ppcard_apply) ";
            sqlCmd += " and current_code = '0' ";
            sqlCmd += " fetch first 1 rows only ";
            String idPSeqno = comcr.ufIdnoPseqno(hEmbpId, hEmbpIdCode);
            setString(1, comcr.ufIdnoPseqno(hEmbpId, hEmbpIdCode));           
            recCnt = selectTable();
            if (recCnt > 0)
            	hCardNo = getValue("card_no");
        }
        
        sqlCmd  = "select make_fee ";
        sqlCmd += " from mkt_ppcard_issue a, crd_emboss_pp b";
        sqlCmd += " where a.vip_kind = b.vip_kind ";
        sqlCmd += " and a.group_code = b.group_code ";
        recCnt = selectTable();
        if (recCnt > 0)
        	hMakeFee = getValueLong("make_fee");
        
        getAcctKey(ppCardNo);
        setValue("card_no"      , hCardNo);
        setValue("acct_type"    , hAcctType);
        setValue("p_seqno"      , hPSeqno);
        setValue("bill_type"    , "OSSG");
        setValue("txn_code"     , "MF");
        setValue("purchase_date", sysDate);
        setValue("src_type"     , "OS");
        setValueLong("dest_amt" , hMakeFee);
        setValue("dest_currency", "901");
        setValueLong("src_amt"  , hMakeFee);
        setValue("post_flag"    , "N");
        setValue("mod_user"     , "batch");
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , javaProgram);
        daoTable = "bil_sysexp";
        recCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_sysexp duplicate!", "", comcr.hCallBatchSeqno);
        }

    }     
    
    /***********************************************************************/
    void getAcctKey(String tcardNo) throws Exception { 
        sqlCmd = "select acct_type, acno_p_seqno";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, tcardNo);
        int recCnt = selectTable();
        if (recCnt > 0) {
            hAcctType = getValue("acct_type");
            hPSeqno   = getValue("acno_p_seqno");
        }
    }
    
    /***********************************************************************/
    void insertCrdDpDragon() throws Exception {
        switch (hEmbpEmbossSource) {
        case "1":
        case "2":
        	setValue("mod_audcode"	, "N");
        	setValue("old_dp_card_no"	, "");
        	break;
        case "3":
        case "4":
        	setValue("mod_audcode"	, "R");
        	setValue("old_dp_card_no"	, "");
        	break;
        case "5":
        	if(hEmbpReissueReason.equals("2")) {
        		setValue("mod_audcode"	, "U");
        	}
        	else {
        		setValue("mod_audcode"	, "L");
        	}
        	
        	setValue("old_dp_card_no"	, hEmbpOldCardNo);
        	break;
        default:
        	setValue("old_dp_card_no"	, "");
        	break;
        }
        setValue("dp_card_no"    , hEmbpPpCardNo);
        setValue("id_p_seqno"    , comcr.ufIdnoPseqno(hEmbpId, hEmbpIdCode)); 
        setValue("post_flag"     , "N");        
        setValue("post_date"     , "");
        setValue("crt_date"      , sysDate);
        setValue("crt_user"      , "CrdM011");
        setValue("mod_time"      , sysDate + sysTime);
        setValue("mod_pgm"       , "CrdM011");
        setValue("mod_user"      , "batch");
        daoTable = "crd_dp_dragon";
        int recCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_dp_dragon duplicate!", "", comcr.hCallBatchSeqno);
        }
    }     

    /***********************************************************************/
    int chkCrdCard() throws Exception {
        tempInt = 0;
        String idPSeqno = comcr.ufIdnoPseqno(hEmbpId, hEmbpIdCode);
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "from crd_card ";
        sqlCmd += "where id_p_seqno   = ? ";
        sqlCmd += "  and card_type   in (select card_type from mkt_ppcard_apply) ";
        sqlCmd += "  and current_code = '0' ";
        setString(1, idPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }
        return (tempInt);
    }

    /***********************************************************************/
    void updateEmboss() throws Exception {
        daoTable   = "crd_emboss_pp";
        updateSQL  = "in_main_error    = ? , ";
        updateSQL += "mod_pgm          = ? , ";
        updateSQL += "mod_user         = 'batch', ";
        updateSQL += "mod_time         = sysdate  ";
        whereStr   = "where rowid      = ? ";
        setString(1, hError);
        setString(2, javaProgram);
        setRowId(3, hEmbpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss_pp not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int chkOldCrdCardPp() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from crd_card_pp  ";
        sqlCmd += "where pp_card_no   = ?  ";
        sqlCmd += "  and current_code = '0' ";
        setString(1, hEmbpOldCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }
        return (tempInt);
    }

    /***********************************************************************/
    int insertCrdCardPp() throws Exception {

        daoTable = "crd_card_pp";
        setValue("pp_card_no"    , hEmbpPpCardNo);
        setValue("current_code"  , "0");
        setValue("bin_type"      , hCrdpBinType);
        setValue("emboss_source" , hEmbpEmbossSource);
        setValue("issue_date"    , hSystemDate);
        setValue("batchno"       , hEmbpBatchno);
        setValueInt("recno"      , hEmbpRecno);
 //       setValue("id"            , h_embp_id);
 //       setValue("id_code"       , h_embp_id_code);
        setValue("id_p_seqno"    , comcr.ufIdnoPseqno(hEmbpId, hEmbpIdCode));
        setValue("card_type"     , hEmbpCardType);
        setValue("unit_code"     , hEmbpUnitCode);
        setValue("group_code"    , hEmbpGroupCode);
        setValue("source_code"   , hEmbpSourceCode);
        setValue("card_item"     , hEmbpCardItem);
        setValue("eng_name"      , hEmbpEngName);
        setValue("zip_code "     , hEmbpZipCode);
        setValue("mail_addr1"    , hEmbpMailAddr1);
        setValue("mail_addr2"    , hEmbpMailAddr2);
        setValue("mail_addr3"    , hEmbpMailAddr3);
        setValue("mail_addr4"    , hEmbpMailAddr4);
        setValue("mail_addr5"    , hEmbpMailAddr5);
        setValue("valid_fm"      , hEmbpValidFm);
        setValue("valid_to"      , hEmbpValidTo);
        setValue("mail_type"     , hEmbpMailType);
        setValue("mail_no"       , hEmbpMailNo);
        setValue("mail_branch"   , hEmbpMailBranch);
        setValue("mail_proc_date", hSystemDate);
        setValue("barcode_num"   , hEmbpBarcodeNum);
        setValue("branch_code"   , "009");
        setValue("old_card_no"   , hEmbpOldCardNo);
        setValue("old_beg_date"  , hEmbpOldBegDate);
        setValue("old_end_date"  , hEmbpOldEndDate);
        setValue("mod_time"      , sysDate + sysTime);
        setValue("mod_pgm"       , javaProgram);
        setValue("vip_kind"       , hEmbpVipKind);
        setValue("card_no"       , hEmbpApplyCreditCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_card_pp duplicate!", "", hCallBatchSeqno);
        }

        return (0);
    }

    /***********************************************************************/
    int updateChgCrdCardPp(int type) throws Exception {
        String hChangeReason = "";

        hChangeReason = "1";
        if (hEmbpEmbossSource.equals("4") && hEmbpChangeReason.equals("2")) {
            hChangeReason = "2";
        }

        if(type == 1) {
        	 daoTable   = "crd_card_pp";
             updateSQL  = "new_beg_date     = ? , ";
             updateSQL += "new_end_date     = ? , ";
             updateSQL += "change_date      = ? , ";
             updateSQL += "change_reason    = ? , ";
             updateSQL += "change_status    = '3', ";
             updateSQL += "valid_fm    	   = ? , ";
             updateSQL += "valid_to    	   = ? , ";
             updateSQL += "old_card_no      = ? , ";
             updateSQL += "old_beg_date     = ? , ";
             updateSQL += "old_end_date     = ?   ";
             whereStr   = "where pp_card_no = ?   ";
             setString(1, hEmbpValidFm);
             setString(2, hEmbpValidTo);
             setString(3, hSystemDate);
             setString(4, hChangeReason);
             setString(5, hEmbpValidFm);
             setString(6, hEmbpValidTo);
             setString(7, hEmbpOldCardNo);
             setString(8, hEmbpOldBegDate);
             setString(9, hEmbpOldEndDate);
             setString(10, hEmbpOldCardNo);
             updateTable();
             if (notFound.equals("Y")) {
                 comcr.errRtn("update_crd_card_pp not found!", "", hCallBatchSeqno);
             }
        }
        else {
        	daoTable   = "crd_card_pp";                                            
        	updateSQL  = "new_beg_date     = ? , ";                                
        	updateSQL += "new_end_date     = ? , ";                                                              
        	updateSQL += "valid_fm    	   = ? , ";                                 
        	updateSQL += "valid_to    	   = ? , ";                                                               
        	updateSQL += "old_beg_date     = ? , ";                                
        	updateSQL += "old_end_date     = ? , ";                                
        	updateSQL += "current_code     = '0', ";                               
        	updateSQL += "oppost_date      = '', ";                                
        	updateSQL += "oppost_reason    = '', ";                                
        	updateSQL += "stop_apply_no    = '' ";                                 
        	whereStr   = "where pp_card_no = ?   ";                                
            setString(1, hEmbpValidFm);
            setString(2, hEmbpValidTo);
            setString(3, hEmbpValidFm);
            setString(4, hEmbpValidTo);
            setString(5, hEmbpOldBegDate);
            setString(6, hEmbpOldEndDate);
            setString(7, hEmbpOldCardNo);
        	updateTable();                                                         
        	if (notFound.equals("Y")) {                                            
        	    comcr.errRtn("update_crd_card_pp not found!", "", hCallBatchSeqno);
        	}                                                                              	                                                                       
        }
       
        return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdM011 proc = new CrdM011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
