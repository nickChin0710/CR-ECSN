/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/11/20  V2.00.00    Pino      check_code 3碼                                                                               *
*  109/02/24  V2.00.01    Pino      get_card_no()change logic                 *
*  109/03/16  V2.00.02    Wilson    card_flag = '1'                           *
*  109/03/23  V2.00.03    Wilson    insert crd_seqno_log 新增 seqno_old         *
*  109/04/09  V2.00.04    Wilson    post_flag = 'Y'                           *                                                                            
*  109/11/03  V2.00.05    Wilson    Dbc_B005 -> DbcB005                       *   
*  109/11/12  V2.00.06  yanghan       修改了變量名稱和方法名稱                                                                          *
*  109/12/24  V2.00.07  yanghan       修改了變量名稱和方法名稱            *
*  110/06/18  V2.00.08    Wilson    where條件刪除card_no = ''，讓預製卡資料進入                  * 
*  111/06/16  V1.00.09    Justin    弱點修正                                  * 
*  112/01/31  v2.00.10    Ryan       調整編列卡號邏輯的部分                                                                        *
*  112/12/11  V2.00.11    Wilson    crd_item_unit不判斷卡種                                                              *
******************************************************************************/

package Dbc;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*DEBIT CARD自動編列卡號處理*/
public class DbcB005 extends AccessDAO {
    private String progname = "DEBIT CARD自動編列卡號處理 112/12/11  V2.00.11";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;
    int totalCnt = 0;
    String prgmId = "DbcB005";
    String stderr = "";
    long hModSeqno = 0;
    String hTempUser = "";
    String hCallErrorDesc = "";

    String hBinNo = "";
    String hSeqno = "";
    String hSeqnoRowid = "";
    String hBegSeqno        = "";
    String hEndSeqno        = "";
    String hRandomSeqno = "";
    String hGroupCode = "";
    String hEmapModUser = "";
    double hEmapModSeqno = 0;
    String hEmapUnitCode = "";
    String hEmapCardType = "";
    String hEmapGroupCode = "";
    String hEmapApplyId = "";
    String hEmapApplyIdCode = "";
    String hEmapPmId = "";
    String hEmapPmIdCode = "";
    String hEmapRowid = "";
    String hEmapBatchno = "";
    double hEmapRecno = 0;
    String hCardType = "";
    String hOrgCardnoFlag = "";
    String hEmapBinNo  = "";
    String hEmapCardNo = "";
    String hEmapCardnoCode = "";
    String modUser = "";
    String hProgCode = "";
    String hWfValue = "";
    String hSysdate = "";
    String pApplyId = "";
    String pRowid = "";
    int total = 0;
    int failCnt = 0;
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
            if (args.length > 2) {
                comc.errExit("Usage : DbcB005 batch_seqno", "");
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

            dataPrepare();
//            if (checkProcess(1, "DbcB005") != 0) {
//                comcr.errRtn("check_process(1, \"DbcB005\") != 0", "", comcr.hCallBatchSeqno);
//            }
            while(true) {
           
             	tmpInt = checkProcess(1, "DbcB005");
            	if(tmpInt!=0) {
            		showLogMessage("I", "", "DbcB005正在執行中,sleep 120 sec 後重新執行");
            		TimeUnit.SECONDS.sleep(120);
            		continue;
            	}
        
            	break;
            }
            processDbcEmapTmp();

            checkProcess(2, "DbcB005");

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
    void dataPrepare() {
        modUser = comc.commGetUserID();
        hEmapModUser = modUser;
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
                  comcr.errRtn(err1, err2, "");
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
  			comcr.errRtn(err1, err2, "");
  		}

  		commitDataBase();
  		/*
  		 * lai test
  		 */
        return (0);
    }

    /***********************************************************************/
    void processDbcEmapTmp() throws Exception {
        int flag = 0;
        String prevCardType = "";
        String prevGroupCode = "";

        sqlCmd = "select ";
        sqlCmd += "unit_code,";
        sqlCmd += "card_type,";
        sqlCmd += "decode(group_code,'','0000',group_code) h_emap_group_code,";
        sqlCmd += "apply_id,";
        sqlCmd += "apply_id_code,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "rowid  as rowid,";
        sqlCmd += "batchno,";
        sqlCmd += "recno, ";
        sqlCmd += "card_no ";
        sqlCmd += " from dbc_emap_tmp ";
        sqlCmd += "where card_no = '' ";
        sqlCmd += "  and check_code = '000' ";
        sqlCmd += "order by card_type, group_code, batchno, recno ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hEmapUnitCode     = getValue("unit_code", i);
            hEmapCardType     = getValue("card_type", i);
            hEmapGroupCode    = getValue("h_emap_group_code", i);
            hEmapApplyId      = getValue("apply_id", i);
            hEmapApplyIdCode = getValue("apply_id_code", i);
            hEmapPmId         = getValue("pm_id", i);
            hEmapPmIdCode    = getValue("pm_id_code", i);
            hEmapRowid         = getValue("rowid", i);
            hEmapBatchno       = getValue("batchno", i);
            hEmapRecno         = getValueDouble("recno", i);
            hEmapCardNo        = getValue("card_no", i);
if(debug == 1)
   showLogMessage("D", "", " 888 Read=[" + hEmapBatchno + ", "+hEmapRecno);
            
            if(hEmapCardNo.length() <= 0) {
            	flag = 0;
                if (!prevCardType.equals(hEmapCardType)) {
                    flag = 1;
                    prevCardType = hEmapCardType;
                }
                if (!prevGroupCode.equals(hEmapGroupCode)) {
                    flag = 1;
                    prevGroupCode = hEmapGroupCode;
                }

                hEmapBinNo  = "";
                hEmapCardNo = "";
                String swGetCard = "1";

                while (swGetCard.equals("1")) {
                	if (getCardNo() == 0) {
                		if (combineCardNo() == 0) {
                			totalCnt++;
                			total++;
                			swGetCard = "0"; // 正確 往下
                		}
                		else {
                			continue;
                		}
                	} else {
                		continue;
                	}
                }
                
                updateDbcEmapTmp();
                updateDbcDebitTmp();
            }

        	insertDbcDebit();
            deleteDbcDebitTmp();
        	
        	if ((hEmapApplyId.equals(hEmapPmId)) && (hEmapApplyIdCode.equals(hEmapPmIdCode))) {
        		writeSupData();
        	}
        }
    }

    /***********************************************************************/
    /***
     * 正附卡同時申請,正卡編列完成時,需寫入附卡之major_card_no
     * 
     * @throws Exception
     */
    void writeSupData() throws Exception {
        String pRowid = "";

        pRowid = "";
        pApplyId = "";
        sqlCmd = "select apply_id,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dbc_emap_tmp  ";
        sqlCmd += "where pm_id       = ?  ";
        sqlCmd += "  and pm_id_code  = ?  ";
        sqlCmd += "  and card_type   = ?  ";
        sqlCmd += "  and decode(group_code,'','0000',group_code) = ?  ";
        sqlCmd += "  and apply_id   != pm_id  ";
        sqlCmd += "  and check_code  = '000' ";
        setString(1, hEmapPmId);
        setString(2, hEmapPmIdCode);
        setString(3, hEmapCardType);
        setString(4, hEmapGroupCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            pApplyId = getValue("apply_id");
            pRowid    = getValue("rowid");

            daoTable  = "dbc_emap_tmp";
            updateSQL = "major_card_no = ?";
            whereStr  = "where rowid   = ? ";
            setString(1, hEmapCardNo);
            setRowId(2, pRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emap_tmp not found!", "", comcr.hCallBatchSeqno);
            }
            commitDataBase();
        }
    }

    /***********************************************************************/
    void updateDbcDebitTmp() throws Exception {

        daoTable   = "dbc_debit_tmp";
        updateSQL  = " card_no    = ?, ";
        updateSQL += " mod_user   = ?, ";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_pgm    = ?, ";
        updateSQL += " mod_seqno  = ?  ";
        whereStr   = "where batchno = ? ";
        whereStr  += "  and recno   = ? ";
        setString(1, hEmapCardNo);
        setString(2, hEmapModUser);
        setString(3, prgmId);
        setDouble(4, hEmapModSeqno);
        setString(5, hEmapBatchno);
        setDouble(6, hEmapRecno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_debit_tmp not found!", "", comcr.hCallBatchSeqno);
        }        
    }

    /***********************************************************************/
    void deleteDbcDebitTmp() throws Exception {
        daoTable = "dbc_debit_tmp";
        whereStr = "where card_no = ? ";
        setString(1, hEmapCardNo);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_dbc_debit_tmp not found!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertDbcDebit() throws Exception {
        sqlCmd = "insert into dbc_debit  ";
        sqlCmd += "(card_no,";
        sqlCmd += "apply_id,";
        sqlCmd += "apply_id_code,";
        sqlCmd += "birthday,";
        sqlCmd += "sup_flag,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "apply_date,";
        sqlCmd += "old_card_no,";
        sqlCmd += "batchno,";
        sqlCmd += "recno,";
        sqlCmd += "trans_type,";
        sqlCmd += "saving_actno,";
        sqlCmd += "saving_actno_ext,";
        sqlCmd += "third_data,";
        sqlCmd += "ic_pin,";
        sqlCmd += "to_ibm_date,";
        sqlCmd += "rtn_ibm_date,";
        sqlCmd += "rtn_code,";
        sqlCmd += "fail_proc_code,";
        sqlCmd += "fail_proc_date,";
        sqlCmd += "to_nccc_date,";
        sqlCmd += "rtn_nccc_date,";
        sqlCmd += "reject_code,";
        sqlCmd += "emboss_code,";
        sqlCmd += "emboss_date,";
        sqlCmd += "end_ibm_date,";
        sqlCmd += "end_rtn_code,";
        sqlCmd += "end_rtn_date,";
        sqlCmd += "send_prn_date,";
        sqlCmd += "bank_actno,";
        sqlCmd += "issuer,";
        sqlCmd += "ac_09,";
        sqlCmd += "ac_11,";
        sqlCmd += "seq_1,";
        sqlCmd += "seq_2,";
        sqlCmd += "strip_1,";
        sqlCmd += "strip_ac,";
        sqlCmd += "strip_2,";
        sqlCmd += "expired,";
        sqlCmd += "pos_day_amt,";
        sqlCmd += "pos_lmt_amt,";
        sqlCmd += "pos_rec_no,";
        sqlCmd += "pos_lmt_no,";
        sqlCmd += "over_due,";
        sqlCmd += "day_amt,";
        sqlCmd += "lmt_amt,";
        sqlCmd += "crd_day_amt,";
        sqlCmd += "crd_lmt_amt,";
        sqlCmd += "crd_rec_no,";
        sqlCmd += "crd_lmt_no,";
        sqlCmd += "crd_expired,";
        sqlCmd += "pos_rt_rec_no,";
        sqlCmd += "crd_rt_rec_no,";
        sqlCmd += "sml_amt,";
        sqlCmd += "sml_rec_no,";
        sqlCmd += "country_code,";
        sqlCmd += "bill_code,";
        sqlCmd += "amt_mod,";
        sqlCmd += "txn_pin,";
        sqlCmd += "o_act1,";
        sqlCmd += "i_act1,";
        sqlCmd += "i_bank1,";
        sqlCmd += "o_act2,";
        sqlCmd += "i_act2,";
        sqlCmd += "i_bank2,";
        sqlCmd += "o_act3,";
        sqlCmd += "i_act3,";
        sqlCmd += "i_bank3,";
        sqlCmd += "o_act4,";
        sqlCmd += "i_act4,";
        sqlCmd += "i_bank4,";
        sqlCmd += "o_act5,";
        sqlCmd += "i_act5,";
        sqlCmd += "i_bank5,";
        sqlCmd += "o_act6,";
        sqlCmd += "i_act6,";
        sqlCmd += "i_bank6,";
        sqlCmd += "o_act7,";
        sqlCmd += "i_act7,";
        sqlCmd += "i_bank7,";
        sqlCmd += "o_act8,";
        sqlCmd += "i_act8,";
        sqlCmd += "i_bank8,";
        sqlCmd += "memo,";
        sqlCmd += "id_no,";
        sqlCmd += "birth,";
        sqlCmd += "card,";
        sqlCmd += "pin)";
        sqlCmd += " select ";
        sqlCmd += "card_no,";
        sqlCmd += "apply_id,";
        sqlCmd += "apply_id_code,";
        sqlCmd += "birthday,";
        sqlCmd += "sup_flag,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "apply_date,";
        sqlCmd += "old_card_no,";
        sqlCmd += "batchno,";
        sqlCmd += "recno,";
        sqlCmd += "trans_type,";
        sqlCmd += "saving_actno,";
        sqlCmd += "saving_actno_ext,";
        sqlCmd += "third_data,";
        sqlCmd += "ic_pin,";
        sqlCmd += "to_ibm_date,";
        sqlCmd += "rtn_ibm_date,";
        sqlCmd += "rtn_code,";
        sqlCmd += "fail_proc_code,";
        sqlCmd += "fail_proc_date,";
        sqlCmd += "to_nccc_date,";
        sqlCmd += "rtn_nccc_date,";
        sqlCmd += "reject_code,";
        sqlCmd += "emboss_code,";
        sqlCmd += "emboss_date,";
        sqlCmd += "end_ibm_date,";
        sqlCmd += "end_rtn_code,";
        sqlCmd += "end_rtn_date,";
        sqlCmd += "send_prn_date,";
        sqlCmd += "bank_actno,";
        sqlCmd += "issuer,";
        sqlCmd += "ac_09,";
        sqlCmd += "ac_11,";
        sqlCmd += "seq_1,";
        sqlCmd += "seq_2,";
        sqlCmd += "strip_1,";
        sqlCmd += "strip_ac,";
        sqlCmd += "strip_2,";
        sqlCmd += "expired,";
        sqlCmd += "pos_day_amt,";
        sqlCmd += "pos_lmt_amt,";
        sqlCmd += "pos_rec_no,";
        sqlCmd += "pos_lmt_no,";
        sqlCmd += "over_due,";
        sqlCmd += "day_amt,";
        sqlCmd += "lmt_amt,";
        sqlCmd += "crd_day_amt,";
        sqlCmd += "crd_lmt_amt,";
        sqlCmd += "crd_rec_no,";
        sqlCmd += "crd_lmt_no,";
        sqlCmd += "crd_expired,";
        sqlCmd += "pos_rt_rec_no,";
        sqlCmd += "crd_rt_rec_no,";
        sqlCmd += "sml_amt,";
        sqlCmd += "sml_rec_no,";
        sqlCmd += "country_code,";
        sqlCmd += "bill_code,";
        sqlCmd += "amt_mod,";
        sqlCmd += "txn_pin,";
        sqlCmd += "o_act1,";
        sqlCmd += "i_act1,";
        sqlCmd += "i_bank1,";
        sqlCmd += "o_act2,";
        sqlCmd += "i_act2,";
        sqlCmd += "i_bank2,";
        sqlCmd += "o_act3,";
        sqlCmd += "i_act3,";
        sqlCmd += "i_bank3,";
        sqlCmd += "o_act4,";
        sqlCmd += "i_act4,";
        sqlCmd += "i_bank4,";
        sqlCmd += "o_act5,";
        sqlCmd += "i_act5,";
        sqlCmd += "i_bank5,";
        sqlCmd += "o_act6,";
        sqlCmd += "i_act6,";
        sqlCmd += "i_bank6,";
        sqlCmd += "o_act7,";
        sqlCmd += "i_act7,";
        sqlCmd += "i_bank7,";
        sqlCmd += "o_act8,";
        sqlCmd += "i_act8,";
        sqlCmd += "i_bank8,";
        sqlCmd += "memo,";
        sqlCmd += "id_no,";
        sqlCmd += "birth,";
        sqlCmd += "card,";
        sqlCmd += "pin ";
        sqlCmd += "from dbc_debit_tmp where card_no = ? ";
        setString(1, hEmapCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_"+daoTable+" duplicate!", hEmapCardNo
                                                          , comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateDbcEmapTmp() throws Exception {
        hModSeqno = comcr.getModSeq();
        if (hEmapCardNo.length() <= 0) {
            hEmapCardnoCode = "1";
        } else
            hEmapCardnoCode = "0";

        daoTable   = "dbc_emap_tmp";
        updateSQL  = " card_no     = ?,";
        updateSQL += " bin_no      = ?,";
        updateSQL += " cardno_code = ?,";
        updateSQL += " mod_user    = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = ?,";
        updateSQL += " mod_seqno   = ? ";
        whereStr = "where rowid  = ? ";
        setString(1, hEmapCardNo);
        setString(2, hEmapBinNo);
        setString(3, hEmapCardnoCode);
        setString(4, hEmapModUser);
        setString(5, prgmId);
        setDouble(6, hEmapModSeqno);
        setRowId( 7, hEmapRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_emap_tmp not found!", "", comcr.hCallBatchSeqno);
        }

    }

    // ************************************************************************
    public int combineCardNo() throws Exception {
        // 組合卡號
        String tmpCardNo = hBinNo + hSeqno;
        hEmapBinNo  = hBinNo;
        hEmapCardNo = tmpCardNo;

        int tmpInt1 = chkCrdProhibit(); // > 0 禁用
        if (debug == 1)
            showLogMessage("I", "", "8888  chk bit=" + tmpInt1);
        if (tmpInt1 > 0) {
            return (1);
        } else {
            insertCrdSeqnoLog();
        }

        return (0);
    }
// ************************************************************************
public int chkCrdProhibit() throws Exception
{
        selectSQL = " count(*) as star_cnt ";
        daoTable  = "crd_prohibit ";
        whereStr  = "WHERE card_no     = ?  ";

        setString(1, hEmapCardNo);

        int recCnt = selectTable();

        if (getValueInt("star_cnt") > 0)
            return (1);

        return (0);
}
//************************************************************************
public void insertCrdSeqnoLog() throws Exception 
{
//2: 緊急替代用　5:HCE TPAN用 1:一般用    card_flag
//4:測試用  6:保留                        REASON_CODE
  selectSQL = "card_item     ";
  daoTable  = "crd_item_unit ";
  whereStr  = "where unit_code    = ?  ";

  setString(1, hEmapUnitCode);
  int tmpInt = selectTable();
  String hWhtrCarItem = getValue("card_item");
   dateTime();        
   setValueInt("card_type_sort" , 0);
   setValue("bin_no"            , hBinNo);
   setValue("SEQNO"             , hSeqno);
   setValue("card_type"         , hEmapCardType );
   setValue("group_code"        , hEmapGroupCode);
   setValue("card_flag"         , "1" );
   setValue("reserve"           , "Y");
   setValue("reason_code"       , "1");
   setValue("use_date"          , sysDate);
   setValue("use_id"            , javaProgram);
   setValue("card_item"         , hWhtrCarItem);
   setValue("unit_code"         , hEmapUnitCode);
   setValue("trans_no"          , "");
   setValue("seqno_old"         , hSeqno.substring(0,9));
   setValue("CRT_DATE"          , sysDate);
   setValue("MOD_TIME"          , sysDate + sysTime);
   setValue("MOD_PGM"           , javaProgram);

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
    public int getCardNo() throws Exception {
        // 若編列卡號成功,則傳回0 */
	
	    extendField = "ccrg.";
        selectSQL = " * from (select bin_no,"
        		  + "beg_seqno,"
        		  + "end_seqno,"
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
        daoTable = "crd_cardno_range";
        whereStr = "WHERE group_code   =  ?  " 
                 + "  and card_type    =  ?  " 
                 + "  and card_flag    = '1' "
                 + "  and post_flag    = 'Y' )"
                 + " where unuse > 0 "
                 + " order by unuse "
                 + " FETCH FIRST 1 ROW ONLY   ";
        setString(1, hEmapGroupCode);
        setString(2, hEmapCardType);

        int recCnt = selectTable();
        hBinNo = getValue("ccrg.bin_no");
        hBegSeqno = getValue("ccrg.beg_seqno");
        hEndSeqno = getValue("ccrg.end_seqno");

        if (notFound.equals("Y")) {
        	String err1 = "Error: 參數設定錯誤=[" + hEmapGroupCode + "][" + hEmapCardType + "]";
        	String err2 = "";
        	comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        int n = 1;
        while(true) {
        	
        	if(getRandomSeqno(hBegSeqno,hEndSeqno,n)!=0) continue;
        
        	extendField = "cslg.";
        	selectSQL = "bin_no,"
        			  + "seqno,"
        		      + "reserve";
        	daoTable = "crd_seqno_log";
        	whereStr = "WHERE bin_no   =  ?  " 
        			 + "  and seqno    =  ?  " 
                     + "  and reserve    =  'Y'  ";

        	setString(1, hBinNo);
        	setString(2, hSeqno);
        	recCnt = selectTable();
        	if (recCnt>0) {
        		if(++n>3) {
            		hBegSeqno = String.format("%09d", comStr.ss2int(hBegSeqno) + 1);
            		if(comStr.ss2int(hBegSeqno) > comStr.ss2int(hEndSeqno)) {
            			comcr.errRtn(String.format("該卡號區間無可使用的卡號，團代 = %s ,卡種 = %s ,BIN_NO = %s ,流水號區間起 = %s  ,流水號區間迄 = %s"
            					, hEmapGroupCode,hEmapCardType,hBinNo,hBegSeqno,hEndSeqno), "", comcr.hCallBatchSeqno);
            		}
            	}
        		continue;
        	}
        	if (notFound.equals("Y")) {
                return (0);
        	}
        	
        }
    }

    /***********************************************************************/
    public int getRandomSeqno(String begSeqno,String endSeqno,int n) throws Exception 
    {
    		SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
    		int hBegSeqno = Integer.parseInt(begSeqno.replaceAll(",", "").trim());
    		int hEndSeqno = Integer.parseInt(endSeqno.replaceAll(",", "").trim());
    		long fseqno = (long)hBegSeqno;
    		if(n<=3) {
    			int rnum = rd.nextInt(hEndSeqno - hBegSeqno);
    			fseqno = (long)(hBegSeqno) + (long) (rnum);
    		}	
    if(debug == 1)   showLogMessage("I", "", "888 random="+hBegSeqno+","+fseqno);

            hRandomSeqno = comm.fillZero(Long.toString(fseqno), 9);
            String tmpX15 = hBinNo + hRandomSeqno;
            String hSChkDif = comm.cardChkCode(tmpX15);
            if (!comm.isNumber(hSChkDif)) {
                String err1 = "Error: 檢查碼錯誤=[" + tmpX15 + "][" + hSChkDif + "]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            int retCode = selectCrdCard0(tmpX15 + hSChkDif);
            if (retCode != 0) {
                hCallErrorDesc = "Error:此區間內有卡號已存在卡檔內!";
                showLogMessage("I", "", hCallErrorDesc);
                return (1);
            }
            hSeqno = hRandomSeqno + hSChkDif;
            return (0);
    }
  /************************************************************************/
    public int selectCrdCard0(String cardNo) throws Exception 
    {
         selectSQL = "card_no      ";
         daoTable = "CRD_CARD";
         whereStr = "WHERE CARD_NO = ? ";

         if (debugD == 1)
             showLogMessage("I", "", "     11.1 card=[" + cardNo + "]");

         setString(1, cardNo);
         int recCnt = selectTable();
         if (recCnt > 0)
             return (1);

         selectSQL = "card_no      ";
         daoTable = "DBC_CARD";
         whereStr = "WHERE CARD_NO = ? ";
         setString(1, cardNo);
         recCnt = selectTable();
         if (recCnt > 0)
             return (1);

         return (0);
    }
    /***********************************************************************/
    int chkUseOrgCardnoFlag() throws Exception {
        String hOrgCardnoFlag = "";
        hOrgCardnoFlag = "";
        sqlCmd  = "select org_cardno_flag ";
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
        if (hOrgCardnoFlag.trim().equals("N"))
            return (1);
        else
            return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcB005 proc = new DbcB005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
