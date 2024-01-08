/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/02/05  V1.00.01    Rou       add insert_crd_seqno_log                  *
*  109/03/16  V1.00.02    Wilson    card_flag = '1'                           *
*  109/04/09  V2.00.03    Wilson    post_flag = 'Y'                           *
*  109/05/12  V2.00.04    Wilson    Error:  參數設定錯誤                                                                       *
*  109/11/03  V2.00.05    Wilson    DBC_C008 -> DbcC008                                                                       *
*  112/01/31  v2.00.06    Ryan       調整編列卡號邏輯的部分                                                                        *
*  112/02/02  V2.00.07    Wilson    增加confirm_date <> ''、confirm_user <> '' *
*  112/12/11  V2.00.08    Wilson    crd_item_unit不判斷卡種                                                              *
******************************************************************************/

package Dbc;

import java.security.SecureRandom;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*DEBIT CARD 非新製卡 自動編列卡號處理*/
public class DbcC008 extends AccessDAO {
    private String progname = "DEBIT CARD 非新製卡 自動編列卡號處理  112/12/11 V2.00.08";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommString     comStr  = new CommString();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;
    long totalCnt = 0;
    String hTempUser = "";

    String prgmId = "DbcC008";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hBinNo = "";
    String hBegSeqno = "";
    String hEndSeqno = "";
    String hCardFlag = "";
    String hTransNo = "";
    String hSeqno = "";
    String hSeqnoRowid = "";
    String hGroupCode = "";
    String hAgeIndicator = "";
    String hDespModUser = "";
    String hDespCardType = "";
    String hDespUnitCode = "";
    String hDespGroupCode = "";
    String hDespMemberNote = "";
    String hDespApplyId = "";
    String hDespPmId = "";
    String hDespPmIdCode = "";
    String hDespSupFlag = "";
    String hDespOldCardNo = "";
    String hDespMajorCardNo = "";
    String hDespBirthday = "";
    String hDespAgeIndicator = "";
    String hDespRowid = "";
    String hWhtrCardItem = "";
    String hFirstSeqno = "";
    String hCardType = "";
    String hOrgCardnoFlag = "";
    String pApplyId = "";
    String pRowid = "";
    String hDespCardnoCode = "";
    String hServiceCode = "";
    String modUser = "";
    String hProgCode = "";
    String hWfValue = "";
    String hSysdate = "";
    String transId = "";
    String currDate = "";
    int totCnt = 0;
    int failCnt = 0;
    int total = 0;
    int recCnt = 0;
    private String hRandomSeqno = "";
    int tmpInt = 0;
    // ************************************************************************

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
                comc.errExit("Usage : DbcC008 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
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

//            if (checkProcess(1, "DbcC008") != 0) {
//                if (comcr.hCallBatchSeqno.length() == 20)
//                    comcr.callbatch(1, 99 , 1); // 1: 結束
//                finalProcess();
//                return 0;
//            }
            while(true) {
           
            	tmpInt = checkProcess(1, "DbcC008");
            	if(tmpInt!=0) {
            		showLogMessage("I", "", "DbcC008正在執行中,sleep 120 sec 後重新執行");
            		TimeUnit.SECONDS.sleep(120);
            		continue;
            	}
            	
            	break;
            }
            processDbcEmbossTmp();

//            if (checkProcess(2, "DbcC008") != 0) {
//                if (comcr.hCallBatchSeqno.length() == 20)
//                    comcr.callbatch(1, 99 , 1); // 1: 結束
//                finalProcess();
//                return 0;
//            }

            checkProcess(2, "DbcC008");

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
    void getSysdate() throws Exception {
        hSysdate = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_sysdate ";
        sqlCmd += "  from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSysdate = getValue("h_sysdate");
        }
    }

    /***********************************************************************/
    int checkProcess(int type, String progCode) throws Exception {
    	  if(debug == 1) showLogMessage("I", "", " check=[" + type + "] " + progCode);

          if (type == 2) {
              updateSQL = " wf_value         = 'NO' , " 
                        + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                        + " mod_pgm          = ?   ";
              daoTable  = "ptr_sys_parm";
              whereStr  = "WHERE wf_parm     = 'CRD_BATCH' " 
                        + "  and wf_key      = ? ";

              setString(1, sysDate + sysTime);
              setString(2, javaProgram);
              setString(3, progCode);

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
  		setString(3, progCode);

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
    void processDbcEmbossTmp() throws Exception {
        int flag = 0;
        String prevCardType = "";
        String prevGroupCode = "";
        String prevMemberNote = "";
        String prevAgeIndicator = "";

        sqlCmd = "select ";
        sqlCmd += "decode(unit_code ,'','0000',unit_code ) h_desp_unit_code ,";
        sqlCmd += "card_type,";
        sqlCmd += "decode(group_code,'','0000',group_code) h_desp_group_code,";
        sqlCmd += "member_note,";
        sqlCmd += "apply_id,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "old_card_no,";
        sqlCmd += "major_card_no,";
        sqlCmd += "birthday,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dbc_emboss_tmp ";
        sqlCmd += "where card_no  = '' ";
        sqlCmd += "  and confirm_date <> '' ";
        sqlCmd += "  and confirm_user <> '' ";
        sqlCmd += "  and apr_date = '' ";
        sqlCmd += "  and apr_user = '' ";
        sqlCmd += "order by card_type,decode(group_code,'','0000',group_code),member_note,batchno,recno ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hDespCardType     = getValue("card_type", i);
            hDespUnitCode     = getValue("h_desp_unit_code", i);
            hDespGroupCode    = getValue("h_desp_group_code", i);
            hDespMemberNote   = getValue("member_note", i);
            hDespApplyId      = getValue("apply_id", i);
            hDespPmId         = getValue("pm_id", i);
            hDespPmIdCode    = getValue("pm_id_code", i);
            hDespSupFlag      = getValue("sup_flag", i);
            hDespOldCardNo   = getValue("old_card_no", i);
            hDespMajorCardNo = getValue("major_card_no", i);
            hDespBirthday      = getValue("birthday", i);
            hDespAgeIndicator = "N";
            hDespRowid = getValue("rowid", i);
            if (debug == 1) {
                showLogMessage("I", "", "Beg ID   =[" + hDespApplyId  +"]"+hDespGroupCode);
                showLogMessage("I", "", "    old_card_no =[" + hDespOldCardNo + "]");
                showLogMessage("I", "", "    type =[" + hDespCardType + "]" + totCnt);
            }

            totCnt++;
            totalCnt++;
            if (totCnt % 500 == 0 || totCnt == 1)
                showLogMessage("I", "",String.format("crd Process current record=[%d]", totCnt));

            checkBirthday();
            flag = 0;
            if (!prevCardType.equals(hDespCardType)) {
                flag = 1;
                prevCardType = hDespCardType;
            }
            if (!prevGroupCode.equals(hDespGroupCode)) {
                flag = 2;
                prevGroupCode = hDespGroupCode;
            }
            if (!prevMemberNote.equals(hDespMemberNote)) {
                flag = 3;
                prevMemberNote = hDespMemberNote;
            }

            if (!prevAgeIndicator.equals(hDespAgeIndicator)) {
                flag = 4;
                prevAgeIndicator = hDespAgeIndicator;
            }
           
            hDespMajorCardNo = "";
            if (getCardNo() == 0) {
                if (combineCardNo() == 0) {
                    total++;
                    hSeqno = hFirstSeqno;
                }
            } else {
                continue;
            }

            updateDbcEmbossTmp();
            commitDataBase();
        }
    }

    /***********************************************************************/
    void updateDbcEmbossTmp() throws Exception {

        if (hDespMajorCardNo.length() <= 0) {
            hDespCardnoCode = "1";
        } else {
            recCnt++;
            hDespCardnoCode = "0";
        }

        daoTable   = "dbc_emboss_tmp";
        updateSQL  = " card_no      = ?,";
        updateSQL += " bin_no       = ?,";
        updateSQL += " cardno_code  = ?,";
        updateSQL += " service_code = ?,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr   = "where rowid   = ? ";
        if (debug == 1) {
            showLogMessage("I", "", " card =[" + hDespMajorCardNo + "]");
            showLogMessage("I", "", " ser_ =[" + hServiceCode);
          }
        setString(1, hDespMajorCardNo);
        setString(2, hBinNo);
        setString(3, hDespCardnoCode);
        setString(4, hServiceCode);
        setString(5, hDespModUser);
        setString(6, prgmId);
        setRowId( 7, hDespRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", comcr.hCallBatchSeqno);
        }

    }

// ************************************************************************
public int chkCrdProhibit() throws Exception
{
        selectSQL = " count(*) as star_cnt ";
        daoTable  = "crd_prohibit ";
        whereStr  = "WHERE card_no     = ?  ";

        setString(1, hDespMajorCardNo);

        int recCnt = selectTable();

        if (getValueInt("star_cnt") > 0)
            return (1);

        return (0);
}
/***********************************************************************/
    int combineCardNo() throws Exception {
        // 組合卡號
        String tmpCardNo = hSeqno;

        hDespMajorCardNo = tmpCardNo;
        // if( h_desp_major_card_no.trim().equals("UD") )
        // h_desp_major_card_no = tmp_card_no + check_byte + "0";

        int tmpInt1 = chkCrdProhibit(); // > 0 禁用
        if (debug == 1)
            showLogMessage("I", "", "8888  chk bit=" + tmpInt1);
        if (tmpInt1 > 0) {
            
            return (1);
        } else {
        	insertCrdSeqnoLog(hSeqno);
        } return (0);
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

            setString(1, hDespUnitCode);
            int tmpInt = selectTable();
            hWhtrCardItem = getValue("card_item");
            
            setValueInt("card_type_sort" , 0);
            setValue("bin_no"            , hCslBinNo);
            setValue("seqno"             , hCslSeqno);
            setValue("seqno_old"         , hCslSeqnoOld);
            setValue("card_type"         , hDespCardType );
            setValue("group_code"        , hDespGroupCode);
            setValue("card_flag"         , hCardFlag );
            setValue("reserve"           , "Y");
            setValue("trans_no"          , hTransNo);
            setValue("crt_date"          , sysDate);
            setValue("mod_time"          , sysDate + sysTime);
            setValue("mod_pgm"           , javaProgram);
            setValue("use_date"          , sysDate);
            setValue("use_id"            , javaProgram);
            setValue("card_item"         , hWhtrCardItem);
            setValue("unit_code"         , hDespUnitCode);            
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
    
    /***********************************************************************/
    /* * 若編列卡號成功,則傳回0,否則為1 * @return * @throws Exception */
    int getCardNo() throws Exception {
    	hBegSeqno = "";
        hEndSeqno = "";
        
        sqlCmd  = "select * from (select bin_no, beg_seqno, end_seqno, card_flag, trans_no,"
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
        setString(1, hDespGroupCode);
        setString(2, hDespCardType);
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	hBinNo      = getValue("bin_no");
        	hBegSeqno   = getValue("beg_seqno");
        	hEndSeqno   = getValue("end_seqno");
        	hCardFlag   = getValue("card_flag");
        	hTransNo    = getValue("trans_no");
        }
        else {        	
        	String err1 = "Error: 參數設定錯誤=[" + hDespGroupCode + "][" + hDespCardType + "]";
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
            					, hDespGroupCode,hDespCardType,hBinNo,hBegSeqno,hEndSeqno), "", comcr.hCallBatchSeqno);
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
//
//        selectSQL = " bin_no, seqno   ,  rowid  as rowid ";
//        daoTable = "crd_seqno_log";
//        whereStr = "WHERE bin_no       =  ?  " + "  and group_code   =  ?  " + "  and card_flag    = '1' "
//                + "  and reserve     <> 'Y' " + "FETCH FIRST 1 ROW ONLY   ";
//        setString(1, h_bin_no);
//        setString(2, h_group_code);
//
//        int recCnt = selectTable();
//
//        if (recCnt > 0) {
//            h_seqno = getValue("seqno");
//            h_seqno_rowid = getValue("rowid");
//        }
//        if (notFound.equals("Y") || h_seqno.trim().length() < 1) {
//            showLogMessage("D", "", "Error: 卡號已用完 " + h_desp_card_type + "," + h_desp_group_code);
//            return (1);
//        }

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
    int checkBirthday() throws Exception {

        // temp1=curr_date;
        // temp2=h_desp_birthday;
        // year1 = comcr.str2int(temp1) -
        // comcr.str2int(temp2);
        hServiceCode = "221";
        hDespAgeIndicator = "N";
        return (0);
    }

    /***********************************************************************/
    /*** * 抓取每一種卡種+團體代號之最小序號 * @return * @throws Exception */
    int getMinCardNo() throws Exception {
        int chk = 0;

        if (debug == 1)
            showLogMessage("I", "", " get_min =[" + hDespGroupCode + "]");

        hBinNo = "";
        hFirstSeqno = "";
        hCardType = hDespCardType;
        hGroupCode = hDespGroupCode;
        hAgeIndicator = hDespAgeIndicator;
        if (hDespGroupCode.length() <= 0) {
            hGroupCode = "0000";
        } else {
            hGroupCode = hDespGroupCode;
        }

        sqlCmd = "select min(bin_no) as bin_no,";
        sqlCmd += "       min(seqno)  as seqno ";
        sqlCmd += "  from crd_seqno_log  ";
        sqlCmd += " where card_type   = ?  ";
        sqlCmd += "   and group_code  = ?  ";
        sqlCmd += "   and card_flag   = '1'";
        sqlCmd += "   and reserve     = '' ";
        setString(1, hCardType);
        setString(2, hGroupCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBinNo = getValue("bin_no");
            hFirstSeqno = getValue("seqno");
        }

        if (notFound.equals("Y") || hFirstSeqno.length() == 0) {
            if (hGroupCode.trim().equals("0000")) {
                showLogMessage("D", "", String.format("group_code='0000' card_type [%s] 卡號已用完", hCardType));
                return (1);
            }
            // 卡號區間抓取不到,先到ptr_group_card抓取欄位org_cardno_flag
            // 檢核是否可適用一般卡號區間 :
            // A.適用一般卡號 group_code='0000'
            // B.不適用一般卡號區間,表示此group_code之卡號區間以用完
            chk = chkUseOrgCardnoFlag();

            if (chk == 0) {
                hGroupCode = "0000";
                hAgeIndicator = "N";
            } else {
                showLogMessage("D", "", String.format("type[%s] group[%s] 卡號已用完或沒設卡號區間", hCardType, hGroupCode));
                return (1);
            }
            sqlCmd = "select min(bin_no) as bin_no,";
            sqlCmd += "       min(seqno) as seqno ";
            sqlCmd += " from crd_seqno_log  ";
            sqlCmd += "where card_type   = ?  ";
            sqlCmd += "  and group_code  = ?  ";
            sqlCmd += "  and card_flag   = '1' ";
            sqlCmd += "  and reserve  i  = '' ";
            setString(1, hCardType);
            setString(2, hGroupCode);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hBinNo = getValue("bin_no");
                hFirstSeqno = getValue("seqno");
            } else {
                showLogMessage("D", "", "Error: select from crd_seqno_log  卡號已用完");
                return 1;
            }
        }

        return (0);
    }

    /***********************************************************************/
    int chkUseOrgCardnoFlag() throws Exception {
        String hOrgCardnoFlag = "";

        hOrgCardnoFlag = "";
        sqlCmd = "select org_cardno_flag ";
        sqlCmd += "  from ptr_group_card  ";
        sqlCmd += " where card_type  = ?  ";
        sqlCmd += "   and group_code = ? ";
        setString(1, hCardType);
        setString(2, hGroupCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hOrgCardnoFlag = getValue("org_cardno_flag");
        } else {
            return (2);
        }
        if (hOrgCardnoFlag.equals("N"))
            return (1);

        return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcC008 proc = new DbcC008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
