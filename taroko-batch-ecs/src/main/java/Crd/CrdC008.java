/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/02/05  V1.00.01    Rou       add insert_crd_seqno_log                  *
*  109/03/16  V2.00.02    Wilson    card_flag = '1'                           *
*  109/04/09  V2.00.03    Wilson    post_flag = 'Y'                           *
*  109/11/03  V2.00.04    Wilson    CRD_C008 -> CrdC008                       *
*  109/12/18  V1.00.05    shiyuqi       updated for project coding standard   *
*  112/01/31  v2.00.06    Ryan       調整編列卡號邏輯的部分                                                                             *
*  112/02/02  V2.00.07    Wilson    增加confirm_date <> ''、confirm_user <> ''  *
*  112/12/03  V2.00.08    Wilson    crd_item_unit不判斷卡種                                                              *
******************************************************************************/

package Crd;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

public class CrdC008 extends AccessDAO {
    private String progname = "非新製卡 自動編列卡號  112/12/03  V2.00.08";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommString     comStr  = new CommString();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "CrdC008";
    String prgmName = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCallRProgramCode = "";

    String hTempUser = "";
    String hBinNo = "";
    String hSeqno = "";
    String hSeqnoRowid = "";
    String hFirstSeqno = "";
    String hRandomSeqno = "";
    String hGroupCode = "";
    String hMbtmModUser = "";
    String hMbtmModPgm = "";
    double hMbtmModSeqno = 0;
    String hMbtmCardType = "";
    String hMbtmUnitCode = "";
    String hMbtmGroupCode = "";
    String hMbtmApplyId = "";
    String hMbtmPmId = "";
    String hMbtmPmIdCode = "";
    String hMbtmSupFlag = "";
    String hMbtmOldCardNo = "";
    String hMbtmMajorCardNo = "";
    String hMbtmRowid = "";
    String hCardType = "";
    String hOrgCardnoFlag = "";
    String pApplyId = "";
    String pRowid = "";
    String hMbtmBinNo = "";
    String hMbtmCardNo = "";
    String hMbtmCardnoCode = "";
    String modUser = "";
    String hProgCode = "";
    String hWfValue = "";
    String hSysdate = "";
    String hBegSeqno = "";
    String hEndSeqno = "";
    String hWhtrCardItem = "";
    String hCardFlag = "";
    String hTransNo = "";

    int failCnt = 0;
    int totCnt = 0;
    int total = 0;
    int tmpInt = 0;
    // ************************************************************

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
                comc.errExit("Usage : CrdC008 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
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

            getSysdate();
            hMbtmModPgm = "";
            hMbtmModPgm = javaProgram;

            hMbtmModUser = comc.commGetUserID();

//            if (checkProcess(1, "CrdC008") != 0) {
//                comcr.errRtn("check_process1  error", "", comcr.hCallBatchSeqno);
//            }
            
            while(true) {
            	
             	tmpInt = checkProcess(1, "CrdC008");
            	if(tmpInt!=0) {
            		showLogMessage("I", "", "CrdB005,CrdC008,DbcB005,DbcC008'正在執行中,sleep 120 sec 後重新執行");
            		TimeUnit.SECONDS.sleep(120);
            		continue;
            	}
            	break;
            }

            processCrdEmbossTmp();

            checkProcess(2, "CrdC008");

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
    void getSysdate() throws Exception {
        hSysdate = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_sysdate ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSysdate = getValue("h_sysdate");
        }
    }

    /**********************************************************************/
    public int checkProcess(int iType, String iProgCode) throws Exception 
    {
    if(debug == 1) showLogMessage("I", "", " check=[" + iType + "] " + iProgCode);

            if (iType == 2) {
                updateSQL = " wf_value         = 'NO' , " 
                          + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                          + " mod_pgm          = ?   ";
                daoTable  = "ptr_sys_parm";
                whereStr  = "WHERE wf_parm     = 'CRD_BATCH' " 
                          + "  and wf_key      = ? ";

                setString(1, sysDate + sysTime);
                setString(2, javaProgram);
                setString(3, iProgCode);

                updateTable();

                if (notFound.equals("Y")) {
                    String err1 = "update_ptr_sys_parm 2 error[not find] + i_prog_code";
                    String err2 = "";
                    comcr.errRtn(err1, err2, hCallBatchSeqno);
                }

                return (0);
            }


            selectSQL = " wf_value ";
            daoTable = "ptr_sys_parm";
            whereStr = "WHERE wf_parm  =  'CRD_BATCH' " + "  and wf_key  in ('CrdB005','CrdC008','DbcB005','DbcC008') ";
            int n = selectTable();
            
            for(int i=0;i<n;i++) {
            	String tmpChar = getValue("wf_value",i);
            	if (tmpChar.trim().compareTo("YES") == 0 || tmpChar.trim().compareTo("yes") == 0) {
    				showLogMessage("D", "", "Error:新製卡編列卡號,不可同時執行或參數檔被鎖住");
    				return (1);
            	}
            }

    		updateSQL = " wf_value         = 'YES' , " + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
    				+ " mod_pgm          = ?   ";
    		daoTable = "ptr_sys_parm";
    		whereStr = "WHERE wf_parm     = 'CRD_BATCH' " + "  and wf_key      = ? ";

    		setString(1, sysDate + sysTime);
    		setString(2, javaProgram);
    		setString(3, iProgCode);

    		updateTable();

    		if (notFound.equals("Y")) {
    			String err1 = "update_ptr_sys_parm 1 error[not find] + i_prog_code";
    			String err2 = "";
    			comcr.errRtn(err1, err2, hCallBatchSeqno);
    		}

    		commitDataBase();
    		/*
    		 * lai test
    		 */
          return (0);
    }
    /***********************************************************************/
    void processCrdEmbossTmp() throws Exception {
        int flag = 0;
        String prevCardType = "";
        String prevGroupCode = "";

        sqlCmd = "select ";
        sqlCmd += "card_type,";
        sqlCmd += "unit_code,";
        sqlCmd += "decode(group_code,'','0000',group_code) h_mbtm_group_code,";
        sqlCmd += "apply_id,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "old_card_no,";
        sqlCmd += "major_card_no,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from crd_emboss_tmp ";
        sqlCmd += "where card_no  = '' ";
        sqlCmd += "  and confirm_date <> '' ";
        sqlCmd += "  and confirm_user <> '' ";
        sqlCmd += "  and apr_date = '' ";
        sqlCmd += "  and apr_user = '' ";
        sqlCmd += "order by card_type,decode(group_code,'','0000',group_code),batchno,recno ";

        int recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 Read cnt=[" + recordCnt + "]");
        for (int i = 0; i < recordCnt; i++) {
            hMbtmCardType = getValue("card_type", i);
            hMbtmUnitCode = getValue("unit_code", i);
            hMbtmGroupCode = getValue("h_mbtm_group_code", i);
            hMbtmApplyId = getValue("apply_id", i);
            hMbtmPmId = getValue("pm_id", i);
            hMbtmPmIdCode = getValue("pm_id_code", i);
            hMbtmSupFlag = getValue("sup_flag", i);
            hMbtmOldCardNo = getValue("old_card_no", i);
            hMbtmMajorCardNo = getValue("major_card_no", i);
            hMbtmRowid = getValue("rowid", i);

            if (debug == 1)
                showLogMessage("I", "", "888 Read id=[" + hMbtmApplyId + "]");
            showLogMessage("I", "", "888 Read old_card_no=[" + hMbtmOldCardNo + "]");

            flag = 0;
            if (!prevCardType.equals(hMbtmCardType)) {
                flag = 1;
                prevCardType = hMbtmCardType;
            }
            if (!prevGroupCode.equals(hMbtmGroupCode)) {
                flag = 1;
                prevGroupCode = hMbtmGroupCode;
            }
          
            hMbtmBinNo = "";
            hMbtmCardNo = "";
            if (getCardNo() == 0) {
                if (combineCardNo() == 0) { /****
                                               * 檢核附卡之正卡卡號是否正確 2002/04/28
                                               ***/
                    if (hMbtmSupFlag.equals("0")) {
                        writeSupData();
                    }
                    total++;
                    hSeqno = hFirstSeqno;
                }
            } else {
                continue;
            }
            totCnt++;
            updateCrdEmbossTmp();

            commitDataBase();
        }
    }

    /***********************************************************************/
    /*** * 抓取每一種卡種+團體代號之最小序號 * @return * @throws Exception */
    int getMinCardNo() throws Exception {
        int chk = 0;

        hBinNo = "";
        hFirstSeqno = "";
        hCardType = hMbtmCardType;
        hGroupCode = hMbtmGroupCode;
        if (hMbtmGroupCode.length() <= 0) {
            hGroupCode = "0000";
        } else {
            hGroupCode = hMbtmGroupCode;
        }

        sqlCmd  = "select min(bin_no) as h_bin_no,";
        sqlCmd += "       min(seqno)  as h_first_seqno ";
        sqlCmd += "  from crd_seqno_log  ";
        sqlCmd += " where card_type  = ?   ";
        sqlCmd += "   and group_code = ?   ";
        sqlCmd += "   and card_flag  = '1' ";
        sqlCmd += "   and reserve   <> 'Y' ";
     // sqlCmd += " order by bin_no,seqno ";
        setString(1, hCardType);
        setString(2, hGroupCode);
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            hBinNo = getValue("h_bin_no");
            hFirstSeqno = getValue("h_first_seqno");
        }
        if (notFound.equals("Y") || hFirstSeqno.length() == 0) {
            /********************* 一般卡卡號已用完 ,error ***********************/
            if (hGroupCode.substring(0, 4).equals("0000")) {
                stderr = String.format("group='0000' type[%s] 卡號已用完", hCardType);
                showLogMessage("D", "", stderr);
                return (1);
            }

            /*************************************************************
             * shu 90/1/31 卡號區間抓取不到,先到ptr_group_card抓取欄位org_cardno_flag
             * 檢核是否可適用一般卡號區間 : A.適用一般卡號 group_code='0000'
             * B.不適用一般卡號區間,表示此group_code之卡號區間以用完
             **************************************************************/
            chk = chkUseOrgCardnoFlag();

            if (chk == 0) { /* 適用 */
                hGroupCode = "0000";
            } else {
                stderr = String.format("type[%s] group[%s]卡號已用完或沒設卡號區間", hCardType, hGroupCode);
                showLogMessage("I", "", stderr);
                return (1); /* 抓取不到卡號 */
            }
            try {
                sqlCmd  = "select min(bin_no) as h_bin_no,";
                sqlCmd += "       min(seqno)  as h_first_seqno ";
                sqlCmd += "  from crd_seqno_log  ";
                sqlCmd += " where card_type  = ?   ";
                sqlCmd += "   and group_code = ?   ";
                sqlCmd += "   and card_flag  = '1' ";
                sqlCmd += "   and reserve   <> 'Y' ";
                // sqlCmd += " order by bin_no ";
                setString(1, hCardType);
                setString(2, hGroupCode);
                tmpInt = selectTable();
                if (tmpInt > 0) {
                    hBinNo = getValue("h_bin_no");
                    hFirstSeqno = getValue("h_first_seqno");
                } else {
                    showLogMessage("I", "", "select crd_seqno_log not found");
                    return (1);
                }
            } catch (Exception ex) {
                showLogMessage("I", "", String.format("select crd_seqno_log error=[%s]", ex.getMessage()));
                return 1;
            }
        }

        if (debug == 1)
            showLogMessage("I", "", "888 get first=[" + hFirstSeqno + "]");

        return (0);
    }

    /***********************************************************************/
    int chkUseOrgCardnoFlag() throws Exception {
        String hOrgCardnoFlag = "";

        try {
            sqlCmd = "select org_cardno_flag ";
            sqlCmd += "  from ptr_group_card  ";
            sqlCmd += " where card_type  = ?  ";
            sqlCmd += "   and group_code = ? ";
            setString(1, hCardType);
            setString(2, hGroupCode);
            int tmpInt = selectTable();
            if (tmpInt > 0) {
                hOrgCardnoFlag = getValue("org_cardno_flag");
            } else {
                stderr = String.format("select prtr_group_card type[%s] group_code[%s]***\n", hCardType,
                        hGroupCode);
                showLogMessage("I", "", stderr);
                return (2);
            }
        } catch (Exception ex) {
            stderr = String.format("select prtr_group_card type[%s] group_code[%s]***\n", hCardType, hGroupCode);
            showLogMessage("I", "", stderr);
            return (2);
        }
        if (hOrgCardnoFlag.equals("N"))
            return (1);

        return (0);
    }

    /***********************************************************************/
    int getCardNo() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", "888 get_card_no=" + hSeqno + "]" + hFirstSeqno);
        // if(h_first_seqno.trim().length() == 0) h_first_seqno = "0";
        if (hSeqno.trim().length() == 0)
            hSeqno = "0";

        getRandomSeqno(hSeqno);
        
        hBegSeqno = "";
        hEndSeqno = "";
        
        sqlCmd  = " select * from (select bin_no, beg_seqno, end_seqno, card_flag, trans_no,"
          +"(end_seqno - beg_seqno) - ( " 
		  + "    (select count(*) " 
		  + "     from crd_seqno_log a "
		  + "     where a.reserve='Y' " 
		  + "     and a.card_type = crd_cardno_range.card_type "
		  + "     and a.group_code = crd_cardno_range.group_code and a.bin_no = crd_cardno_range.bin_no "
		  + " and substr(a.seqno,1,9) >= crd_cardno_range.beg_seqno"
		  + " and substr(a.seqno,1,9) <= crd_cardno_range.end_seqno ) + " 
		  + "    (select count(*) "
		  + "     from crd_prohibit a "
		  + "     where substr(a.card_no,1,6) = crd_cardno_range.bin_no "
		  + "     and substr(a.card_no,7,9) >= crd_cardno_range.beg_seqno "
		  + "     and substr(a.card_no,7,9) <= crd_cardno_range.end_seqno) " 
		  + ") as unuse ";
        sqlCmd += " from crd_cardno_range ";
        sqlCmd += " where group_code = ?";
        sqlCmd += " and card_type = ?";
        sqlCmd += " and card_flag = '1'";
        sqlCmd += " and post_flag    = 'Y') ";
        sqlCmd += " where unuse > 0 "
                + " order by unuse "
                + " FETCH FIRST 1 ROW ONLY ";
        setString(1, hMbtmGroupCode);
        setString(2, hMbtmCardType);
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	hBinNo = getValue("bin_no");
        	hBegSeqno = getValue("beg_seqno");
        	hEndSeqno = getValue("end_seqno");
        	hCardFlag = getValue("card_flag");
        	hTransNo = getValue("trans_no");
        }
        else {        	
        	String err1 = "Error: 參數設定錯誤=[" + hMbtmGroupCode + "][" + hMbtmCardType + "]";
        	String err2 = "";
        	comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        
        //在h_beg_seqno與h_end_seqno的區間內取得一個亂數卡號，最後一碼為檢查碼
//        SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
//        int maxRange   = Integer.parseInt(hEndSeqno) - Integer.parseInt(hBegSeqno) + 1;
//        int rNum = rd.nextInt(Integer.parseInt(hEndSeqno) - Integer.parseInt(hBegSeqno));
//        long rSeqno    = (long)(Integer.parseInt(hBegSeqno)) + (long) (rNum);
       
//        for (int i = 0; i < maxRange; i++) {
        int n=1;
        while(true) {	
            SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
            long rSeqno    = (long)(Integer.parseInt(hBegSeqno));
            if(n<=3) {
                int rNum = rd.nextInt(Integer.parseInt(hEndSeqno) - Integer.parseInt(hBegSeqno));
                rSeqno    = (long)(Integer.parseInt(hBegSeqno)) + (long) (rNum);
            }
            hRandomSeqno = comm.fillZero(Long.toString(rSeqno), 9);
            String tmpX15 = hBinNo + hRandomSeqno; //取得亂數卡號
            String hSChkDif = comm.cardChkCode(tmpX15); //取得檢查碼
            if (!comm.isNumber(hSChkDif)) {
                String err1 = "Error: 檢查碼錯誤 = [ " + tmpX15 + "][" + hSChkDif + "]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            int retCode = selectCrdSeqnoLog(tmpX15 + hSChkDif); //檢查該亂數卡號是否存在Table:crd_seqno_log
            if (retCode != 0) {
                showLogMessage("I", "", "Error:此區間內有卡號已存在卡檔內!");
               	if(++n>3) {
            		hBegSeqno = String.format("%09d", comStr.ss2int(hBegSeqno) + 1);
            		if(comStr.ss2int(hBegSeqno) > comStr.ss2int(hEndSeqno)) {
            			comcr.errRtn(String.format("該卡號區間無可使用的卡號，團代 = %s ,卡種 = %s ,BIN_NO = %s ,流水號區間起 = %s  ,流水號區間迄 = %s"
            					, hMbtmGroupCode,hMbtmCardType,hBinNo,hBegSeqno,hEndSeqno), "", comcr.hCallBatchSeqno);
            		}
            	}
                continue;
            }
            else {            	
            	hSeqno = tmpX15 + hSChkDif;
            	return 0;
            }
        }
      
//        h_seqno = "";
//        h_seqno_rowid = "";
//        sqlCmd = "select bin_no,";
//        sqlCmd += "seqno,";
//        sqlCmd += "rowid  as rowid ";
//        sqlCmd += "  from crd_seqno_log  ";
//        sqlCmd += " where bin_no     = ?  ";
//        sqlCmd += "   and seqno     >= ?  ";
//        sqlCmd += "   and seqno     <= ?  ";
//        sqlCmd += "   and group_code = ?  ";
//        sqlCmd += "   and card_flag  = '1'  ";
//        sqlCmd += "   and reserve   <> 'Y'  ";
//        sqlCmd += " fetch first 1 rows only ";
//        setString(1, h_bin_no);
//        setString(2, h_first_seqno);
//        setString(3, h_random_seqno);
//        setString(4, h_group_code);        
//        int tmp_int = selectTable();
//        if (tmp_int > 0) {
//            h_bin_no      = getValue("bin_no");
//            h_seqno       = getValue("seqno");
//            h_seqno_rowid = getValue("rowid");
//        }
//        if (notFound.equals("Y") || h_seqno.length() == 0) {
//            sqlCmd = "select bin_no,";
//            sqlCmd += "seqno,";
//            sqlCmd += "rowid  as rowid ";
//            sqlCmd += "  from crd_seqno_log  ";
//            sqlCmd += " where bin_no     = ?  ";
//            sqlCmd += "   and group_code = ?  ";
//            sqlCmd += "   and card_flag  = '1'  ";
//            sqlCmd += "   and reserve   <> 'Y'  ";
//            sqlCmd += " fetch first 1 rows only ";
//            setString(1, h_bin_no);
//            setString(2, h_group_code);
//            tmp_int = selectTable();
//            if (tmp_int > 0) {
//                h_bin_no      = getValue("bin_no");
//                h_seqno       = getValue("seqno");
//                h_seqno_rowid = getValue("rowid");
//            }
//            if (notFound.equals("Y") || h_seqno.length() == 0)
//                return (1);
//        }

//        if (debug == 1)
//            showLogMessage("I", "", "888 get_card_no end=" + hSeqno + "]");
//        return (0);
    }
    
    /***********************************************************************/
    int selectCrdSeqnoLog(String cardNo) throws Exception 
    {
            sqlCmd  = "select * ";
            sqlCmd += " from crd_seqno_log ";
            sqlCmd += " where bin_no = substr(?, 1, 6) ";
            sqlCmd += " and seqno = substr(?, 7) ";
            sqlCmd += " and reserve = 'Y' ";            
            setString(1, cardNo);
            setString(2, cardNo);
            
            showLogMessage("I", "", "card=[" + cardNo + "]");
            
            int recCnt = selectTable();
            if (recCnt > 0)
            	return recCnt;
            else
            	return 0;
    }

    /***********************************************************************/
    void getRandomSeqno(String firstSeqno) throws Exception  {
        int rnum = comc.getNextRandom(100);
        if (debug == 1)
            showLogMessage("I", "", "888 1.1  cnt=[" + rnum + "]" + firstSeqno);
        long fseqno = Long.parseLong(firstSeqno) + (long) (rnum);

        if (debug == 1)
            showLogMessage("I", "", "888 1.2  cnt=[" + fseqno + "]");
        hRandomSeqno = comm.fillZero(Long.toString(fseqno), 9);
        if (firstSeqno.trim().length() != 9) {
            hRandomSeqno = comm.fillZero(Long.toString(fseqno), 8);
        }
    }

/***********************************************************************/
int combineCardNo() throws Exception 
{
        // 組合卡號
        if (debug == 1)
            showLogMessage("I", "", "888 combine h_seqno=[" + hSeqno + "]");
//        String tmp_card_no = h_bin_no + h_seqno;

        hMbtmBinNo = hBinNo;
        hMbtmCardNo = hSeqno;
        /*
         * String check_byte = comm.cardChkCode(tmp_card_no); h_mbtm_card_no =
         * tmp_card_no + check_byte; if( h_mbtm_card_no.trim().equals("UD") )
         * h_mbtm_card_no = tmp_card_no + check_byte + "0";
         */

        int tmpInt1 = chkCrdProhibit(); // > 0 禁用
        if (debug == 1)
            showLogMessage("I", "", "8888  chk bit=" + tmpInt1);
        if (tmpInt1 > 0) {            
            return (1);
        } else {
        	insertCrdSeqnoLog(hSeqno);
        }

        return (0);
}

//************************************************************************
public void insertCrdSeqnoLog(String seqNo) throws Exception 
{
        dateTime();
        String hCslBinNo = seqNo.substring(0, 6);
        String hCslSeqno = seqNo.substring(6);
        String hCslSeqnoOld = hCslSeqno.substring(0, 9);
        
        selectSQL = "card_item     ";
        daoTable  = "crd_item_unit ";
        whereStr  = "where unit_code    = ?  ";

        setString(1, hMbtmUnitCode);
        int tmpInt = selectTable();
        hWhtrCardItem = getValue("card_item");
        
        setValueInt("card_type_sort" , 0);
        setValue("bin_no"            , hCslBinNo);
        setValue("seqno"             , hCslSeqno);
        setValue("seqno_old"         , hCslSeqnoOld);
        setValue("card_type"         , hMbtmCardType);
        setValue("group_code"        , hMbtmGroupCode);
        setValue("card_flag"         , hCardFlag);
        setValue("reserve"           , "Y");
        setValue("trans_no"          , hTransNo);
        setValue("crt_date"          , sysDate);
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_pgm"           , javaProgram);
        setValue("use_date"          , sysDate);
        setValue("use_id"            , javaProgram);
        setValue("card_item"         , hWhtrCardItem);
        setValue("unit_code"         , hMbtmUnitCode);
        setValue("trans_no"          , "");
       

        daoTable = "crd_seqno_log";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "crd_seqno_log       error[dupRecord]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        return;
}

// ************************************************************************
public int chkCrdProhibit() throws Exception
{
        selectSQL = " count(*) as star_cnt ";
        daoTable  = "crd_prohibit ";
        whereStr  = "WHERE card_no     = ?  ";

        setString(1, hMbtmCardNo);

        int recCnt = selectTable();

        if (getValueInt("star_cnt") > 0)
            return (1);

        return (0);
}
/**********************************************************************/
    void writeSupData() throws Exception {
        String pRowid = "";
        int tmpInt = 0;
        try {
            sqlCmd = "select apply_id,";
            sqlCmd += "rowid  as rowid ";
            sqlCmd += "  from crd_emboss_tmp  ";
            sqlCmd += " where pm_id      = ?  ";
            sqlCmd += "   and pm_id_code = ?  ";
            sqlCmd += "   and card_type  = ?  ";
            sqlCmd += "   and decode(group_code,'','0000',group_code) = ?  ";
            sqlCmd += "   and apply_id  != pm_id ";
            setString(1, hMbtmPmId);
            setString(2, hMbtmPmIdCode);
            setString(3, hMbtmCardType);
            setString(4, hMbtmGroupCode);
            tmpInt = selectTable();

        } catch (Exception ex) {
            tmpInt = 0;
        }

        if (tmpInt > 0) {
            pApplyId = getValue("apply_id");
            pRowid = getValue("rowid");

            daoTable = "crd_emboss_tmp";
            updateSQL = "major_card_no = ?";
            whereStr = "where rowid   = ? ";
            setString(1, hMbtmCardNo);
            setRowId(2, pRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_emboss_tmp not found!", "", comcr.hCallBatchSeqno);
            }
        }

        return;
    }

    /**********************************************************************/
    void updateCrdEmbossTmp() throws Exception {
        hMbtmModSeqno = comcr.getModSeq();

        if (hMbtmCardNo.length() <= 0) {
            hMbtmCardnoCode = "1";
        } else
            hMbtmCardnoCode = "0";

        daoTable   = "crd_emboss_tmp";
        updateSQL  = " bin_no      = ?,";
        updateSQL += " card_no     = ?,";
        updateSQL += " cardno_code = ?,";
        updateSQL += " mod_user    = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = ? ";
        whereStr   = "where rowid  = ? ";
        setString(1, hMbtmBinNo);
        setString(2, hMbtmCardNo);
        setString(3, hMbtmCardnoCode);
        setString(4, hMbtmModUser);
        setString(5, hMbtmModPgm);
        setRowId( 6, hMbtmRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss_tmp not found!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdC008 proc = new CrdC008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
