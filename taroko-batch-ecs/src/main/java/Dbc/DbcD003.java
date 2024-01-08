/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-07-07  V1.01.00    Ryan      ptr_keys_table change to ptr_hsm_keys     *
*  109/11/13  V1.01.02  yanghan       修改了變量名稱和方法名稱      
*  109/11/25  V1.01.03    Wilson    connectRacal                              *
*  109/12/24  V1.01.04  yanghan       修改了變量名稱和方法名稱            *   
*  110/01/22  V1.01.05  Wilson      新增 insert tsc_dcrp_log                    *
*  111/12/25  V1.01.06  Wilson     M跟J的ICVV要用V的KEY                          * 
*  112/02/08  V1.00.07  Wilson     增加判斷to_ibm_date                          *
*  112/02/14  V1.00.08  Wilson     to_ibm_date改為rtn_ibm_date                 *
*  112/04/14  V1.00.09  Wilson     讀參數判斷是否由新系統編列票證卡號                                                       *
*  112/05/12  V1.00.10  Wilson     wf_key change to VD_ELEC_CARD_NO           *
*  112/06/19  V1.00.11  Wilson     where條件增加check_code = ""                 *
*  112/12/11  V1.00.12  Wilson     crd_item_unit不判斷卡種                                                                *
******************************************************************************/

package Dbc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import bank.Auth.HSM.HsmUtil;

/*產生DEBIT CARD PVV,CVV,CVC2,PIN,CSC ICVV*/
public class DbcD003 extends AccessDAO {
	private String progname = "產生DEBIT CARD PVV,CVV,CVC2,PIN,CSC ICVV   112/12/11 V1.00.12";

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 1;
	int debugD = 1;

	String prgmId = "DbcD003";
	String hCallBatchSeqno = "";

	String hDcesCardNo = "";
	String hCardNoAe = "";
	String hDcesEmbossSource = "";
	String hDcesEmbossReason = "";
	String hDcesCardType = "";
	String hDcesGroupCode = "";
	String hDcesValidTo = "";
	String hDcesIcFlag = "";
	String hDcesUnitCode = "";
	String hDcesElectronicCode = "";
	String hDcesVendor = "";
	String hDcesRowid = "";
	String hDcesServiceCode = "";
	int hCnt = 0;
	String hNewEndDate = "";
	String hPvki = "";
	String hPinBlock = "";
	String hDcesPvv = "";
	String hDcesCvv = "";
	String hTransCvv2 = "";
	String hDcesCsc = "";
	String hDcesPinBlock = "";
	String hDcesPvki = "";
	String hDcesIcCvv = "";
	String hPktbEcsPvk1 = "";
	String hPktbEcsPvk2 = "";
	String hPktbEcsCvk1 = "";
	String hPktbEcsCvk2 = "";
    String hPktbEcsIcvv1= "";
    String hPktbEcsIcvv2= "";
	int hPktbRacalPort = 0;
	String hPktbRacalIpAddr = "";
	String hPktbEcsCsck1 = "";
	String hPctpServiceCode = "";
	String pGroupCode = "";
	String racalServer = "";
	int racalPort = 0;
	String hBinType = "";
	int visaCard = 0;
	String hCardAe = "";
	String hDcesCvv2 = "";
	int totalCnt = 0;
    String tmpRacalServer;
    int tmpRacalPort;
    Socket socket = null;
    String hBinNo = "";
    String hTempX09 = "";
    String hTempX10 = "";
    String hTempX16 = "";
    int chkDig = 0;
    long hUpperLmt = 0;
    long hUpperLmtAcmm = 0;
    String cmbRtnIbmDate = "";
    String tmpWfValue = "";

	String[] pvkKey1 = new String[6];
	String[] pvkKey2 = new String[6];

	pvvOutbuf gpvvRep = new pvvOutbuf();
	pvvInbuf gpvvReq = new pvvInbuf();
	pinInbuf gpinReq = new pinInbuf();
	pinOutbuf gpinRep = new pinOutbuf();
	cvvInbuf gcvvReq = new cvvInbuf();
	cvvOutbuf gcvvRep = new cvvOutbuf();
	cscOutbuf gcscRep = new cscOutbuf();
	cscInbuf gcscReq = new cscInbuf();

	// ************************************************************
	public int mainProcess(String[] args) throws IOException {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : DbcD003 ", "");
			}
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			
            selectHsmIP();
            
            if (connectRacal() != 0) {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                comcr.errRtn("connectRacal error!", "", hCallBatchSeqno);
            }

			selectCrdEmboss();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束=[" + totalCnt + "]");

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectHsmIP() throws Exception {
		int hPktbRacalPort = 0;
		String hPktbRacalIpAddr = "";
		sqlCmd = "select ";
		sqlCmd += " hsm_port1,";
		sqlCmd += " hsm_ip_addr1 ";
		sqlCmd += " from ptr_hsm_keys  ";
		sqlCmd += " where hsm_keys_org ='00000000' ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hPktbRacalPort = getValueInt("hsm_port1");
			hPktbRacalIpAddr = getValue("hsm_ip_addr1");
		}
		racalPort = hPktbRacalPort;
		racalServer = hPktbRacalIpAddr;
	}
	/***********************************************************************/
    int connectRacal() {
        try {
        	tmpRacalServer = racalServer;
        	tmpRacalPort = racalPort;
            /*
             * String host =
             * InetAddress.getByName(tmpRacalServer).getHostAddress(); int port =
             * tmpRacalPort;
             */
            String host = racalServer;
            int port = racalPort;

            if (debug == 1)
                showLogMessage("I", "", "  888 IP=[" + host + "]" + port);

            socket = new Socket(host, port);
            if (debug == 1)
                showLogMessage("I", "", "  888 IP OK=[" + host + "]" + port);

        } catch (IOException e) {
            showLogMessage("I", "", "RACAL CONNECT error !!");
            return 1;
        }
        showLogMessage("I", "", "RACAL CONNECT ok !!");

        return (0);
    }

    /***********************************************************************/
	void selectPtrHsmKeys(String aCardNo) throws Exception {
		hPktbEcsPvk1 = "";
		hPktbEcsPvk2 = "";
		hPktbEcsCvk1 = "";
		hPktbEcsCvk2 = "";
		hPktbEcsIcvv1 = "";
		hPktbEcsIcvv2 = "";
		hPktbRacalPort = 0;
		hPktbRacalIpAddr = "";
		hPktbEcsCsck1 = "";

		String sqls = getSqlVMJ(aCardNo);
		sqlCmd = " select " + sqls + " hsm_port1 ,hsm_ip_addr1 ,ecs_csck1       ";
		sqlCmd += " from ptr_hsm_keys  ";
		sqlCmd += " where hsm_keys_org ='00000000' ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_hsm_keys not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hPktbEcsPvk1 = getValue("ecs_pvk1");
			hPktbEcsPvk2 = getValue("ecs_pvk2");
			hPktbEcsCvk1 = getValue("ecs_cvk1");
			hPktbEcsCvk2 = getValue("ecs_cvk2");
			hPktbRacalPort = getValueInt("hsm_port1");
			hPktbRacalIpAddr = getValue("hsm_ip_addr1");
			hPktbEcsCsck1 = getValue("ecs_csck1");
	        hPktbEcsIcvv1 = getValue("ecs_icvv1");
	        hPktbEcsIcvv2 = getValue("ecs_icvv2");

		}

		pvkKey1[0] = hPktbEcsPvk1;
		pvkKey1[1] = hPktbEcsPvk1;
		pvkKey1[2] = hPktbEcsPvk1;
		pvkKey1[3] = hPktbEcsPvk1;
		pvkKey1[4] = hPktbEcsPvk1;
		pvkKey1[5] = hPktbEcsPvk1;
		pvkKey2[0] = hPktbEcsPvk2;
		pvkKey2[1] = hPktbEcsPvk2;
		pvkKey2[2] = hPktbEcsPvk2;
		pvkKey2[3] = hPktbEcsPvk2;
		pvkKey2[4] = hPktbEcsPvk2;
		pvkKey2[5] = hPktbEcsPvk2;
		racalPort = hPktbRacalPort;
		racalServer = hPktbRacalIpAddr;
		showLogMessage("I", "", String.format("RACLA_IP [%s] ", racalServer));
	}

	/***********************************************************************/
	void selectCrdEmboss() throws Exception {
		int totalNum;
		int flag = 0;
		String tmpstr = "";
		int num = 0;
		String aCardNo = "";
		sqlCmd = "select ";
		sqlCmd += "card_no, ";
		sqlCmd += "decode(length(card_no),15,card_no||'0',card_no) h_dces_card_no,";
		sqlCmd += "decode(length(card_no),15,card_no||'0000',card_no) h_card_no_ae,";
		sqlCmd += "emboss_source,";
		sqlCmd += "emboss_reason,";
		sqlCmd += "card_type,";
		sqlCmd += "group_code,";
		sqlCmd += "valid_to,";
		sqlCmd += "ic_flag,";
		sqlCmd += "unit_code,";
		sqlCmd += "electronic_code,";
		sqlCmd += "vendor,";
		sqlCmd += "rowid  as rowid";
		sqlCmd += " from dbc_emboss ";
		sqlCmd += "where card_no     <> '' ";
		sqlCmd += "  and pin_block    = '' ";
		sqlCmd += "  and check_code  = '' ";
		sqlCmd += "  and reject_code  = '' ";
		sqlCmd += "  and in_main_date = '' ";
		sqlCmd += "  and to_nccc_code = 'Y' ";
		int recordCnta = selectTable();
		if (debug == 1)
			showLogMessage("I", "", "ALL  cnt=" + recordCnta);
		for (int i = 0; i < recordCnta; i++) {
			aCardNo = getValue("card_no", i);
			hDcesCardNo = getValue("h_dces_card_no", i);
			hCardNoAe = getValue("h_card_no_ae", i);
			hDcesEmbossSource = getValue("emboss_source", i);
			hDcesEmbossReason = getValue("emboss_reason", i);
			hDcesCardType = getValue("card_type", i);
			hDcesGroupCode = getValue("group_code", i);
			hDcesValidTo = getValue("valid_to", i);
			hDcesIcFlag = getValue("ic_flag", i);
			hDcesUnitCode = getValue("unit_code", i);
			hDcesElectronicCode = getValue("electronic_code", i);
			hDcesVendor = getValue("vendor", i);
			hDcesRowid = getValue("rowid", i);

			if (debug == 1)
				showLogMessage("I", "", "  Card=" + hDcesCardNo);

			selectPtrHsmKeys(aCardNo);
				
			checkDbcDebit();
            
			if(cmbRtnIbmDate.equals("")) {
				continue;
            }

			flag = 0;

			hBinType = "";
			visaCard = 0;
			totalCnt++;
			
			selectPtrCardType();

			sqlCmd = "select bin_type     ";
			sqlCmd += "  from ptr_bintable ";
			sqlCmd += " where 1=1          ";
			sqlCmd += "   and bin_no || bin_no_2_fm || '0000' <= ?  ";
			sqlCmd += "   and bin_no || bin_no_2_to || '9999' >= ?  ";
			setString(1, hDcesCardNo);
			setString(2, hDcesCardNo);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hBinType = getValue("bin_type");
			}
			if (debug == 1)
				showLogMessage("I", "", "Read =" + hDcesCardNo + "," + hBinType + "," + totalCnt);

			if (hBinType.equals("V"))
				visaCard = 1;

			totalNum = 0;

			if (flag == 0) {
				for (int inta = 2; inta < 6; inta++) {
					totalNum = totalNum + (hDcesValidTo.toCharArray()[inta] - 48);
				}
				int rem = totalNum % 6;
				tmpstr = String.format("%1d", rem + 1);
				hDcesPvki = tmpstr;
				hPktbEcsPvk1 = pvkKey1[rem];
				hPktbEcsPvk2 = pvkKey2[rem];
				getPin();
			} else {
				num = comcr.str2int(hDcesPvki) - 1;
				hPktbEcsPvk1 = pvkKey1[num];
				hPktbEcsPvk2 = pvkKey2[num];
				gpinRep.pin = hDcesPinBlock;
			}

			/* 判斷是否為AE */
			tmpstr = hDcesCardNo.substring(1, 2);
			if ((hCardAe.length() == 19) && (tmpstr.equals("3"))) {
				// is AE
				getCsc();
			} else {
				// is not AE
				getPvv();
				getCvv();
			}
			if ((visaCard == 1) && (hDcesIcFlag.equals("Y")))
				getIcvv();

			/* 判斷是否為AE */
			if ((hCardAe.length() == 19) && (tmpstr.equals("3"))) {
				hDcesCsc = comc.transPasswd(0, hDcesCsc);
			} else {
				hDcesPvv = comc.transPasswd(0, hDcesPvv);
				hDcesCvv = comc.transPasswd(0, hDcesCvv);
			}
			if (hDcesIcCvv.length() > 0) {
				hDcesIcCvv = comc.transPasswd(0, hDcesIcCvv);
			}
			
			hUpperLmt = 0;
            hUpperLmtAcmm = 0;
			if(hDcesElectronicCode.equals("01")) {
				selectPtrSysParm();
				if(tmpWfValue.equals("Y")) {
					insertTscDcrpLog();
				}				
			}

			updateEmboss();
			commitDataBase();
		}
		dateTime();
	}

	/***********************************************************************/
    public int checkDbcDebit() throws Exception {
    	
    	cmbRtnIbmDate = "";
    	
    	selectSQL = "rtn_ibm_date ";
        daoTable = "dbc_debit";
        whereStr = "where card_no    = ?  ";

        setString(1, hDcesCardNo);

        int recordCnt = selectTable();

        if(recordCnt > 0) {
        	cmbRtnIbmDate = getValue("rtn_ibm_date");
        }        
        
        return (0);
    }
    
    // ************************************************************************

	public int selectPtrCardType() throws Exception {
    	
    	hPctpServiceCode = "";
    	
    	selectSQL = "service_code ";
        daoTable = "crd_item_unit ";
        whereStr = "where unit_code = ?   ";

        setString(1, hDcesUnitCode);

        int recordCnt = selectTable();

        if (recordCnt > 0) {
        	hPctpServiceCode = getValue("service_code");
        } else {
            String err1 = "select_crd_item_unit  error[not find]" + hDcesUnitCode;
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        if (debug == 1)
            showLogMessage("I", "", " 888 select item_unit=[" + hPctpServiceCode + "]"
                              + hDcesUnitCode + "," + recordCnt);

        hDcesServiceCode = hPctpServiceCode;
        return (0);
    }

    // ************************************************************************
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

	int insertTscDcrpLog() throws Exception 
	{
	        String hRowid = "";
	        double hSeqnoCurrent = 0;

	        hBinNo = "";
	        sqlCmd  = "select a.tsc_bin_no ,b.seq_no_current ";
	        sqlCmd += "  from tsc_bintable a ";
	        sqlCmd += "  left outer join tsc_bin_curr b";
	        sqlCmd += "    on b.tsc_bin_no = a.tsc_bin_no ";
	        sqlCmd += " where decode(a.card_type,  '','00'  , a.card_type ) = ";
	        sqlCmd +=       " decode(cast(? as varchar(10)) , '', '00'  , ?) ";
	        sqlCmd += "   and decode(a.group_code, '','0000', a.group_code) = ";
	        sqlCmd +=       " decode(cast(? as varchar(10)) , '', '0000', ?) ";
	        setString(1, hDcesCardType);
	        setString(2, hDcesCardType);
	        setString(3, hDcesUnitCode);
	        setString(4, hDcesUnitCode);
	        int recordCnt = selectTable();
	if(debug == 1)
	  showLogMessage("I","","  sel tsc_bin="+ hDcesCardType +",unit="+ hDcesUnitCode +","+recordCnt);
	        if (recordCnt > 0) {
	            hBinNo = getValue("tsc_bin_no");
	            hSeqnoCurrent = getValueDouble("seq_no_current");
	        } else {
	           comcr.errRtn("select_tsc_bintable  found 1 !="+ hDcesCardType +",", hDcesUnitCode
	                         , hCallBatchSeqno);
	        }

	        hTempX09 = "";
	        sqlCmd = "select substr(to_char( cast(? as double) + 1,'000000000'),2,10) as h_temp_x09";
	        sqlCmd += " from dual ";
	        setDouble(1, hSeqnoCurrent);
	        recordCnt = selectTable();
	        if (recordCnt > 0) {
	            hTempX09 = getValue("h_temp_x09");
	        }

	        hTempX10 = "";
	        sqlCmd = "select vendor_tscc ";
	        sqlCmd += " from ptr_vendor_setting  ";
	        sqlCmd += "where vendor = ? ";
	        setString(1, hDcesVendor);
	        recordCnt = selectTable();
	        if (recordCnt > 0) {
	            hTempX10 = getValue("vendor_tscc");
	        }

	        chkDig = comc.chgnRtn(String.format("%6.6s%9.9s", hBinNo, hTempX09));
	        hTempX16 = String.format("%6.6s%9.9s%1d", hBinNo, hTempX09, chkDig);
	if(debug == 1)
	   showLogMessage("I", "", "   888 card =["+ hTempX16 +"]" + hBinNo +","+ hTempX09);

	        daoTable  = "tsc_bin_curr ";
	        updateSQL = "seq_no_current = ? + 1";
	        whereStr  = "where tsc_bin_no   = ? ";
	        setDouble(1, hSeqnoCurrent);
	        setString(2, hBinNo);
	        updateTable();
	        if (notFound.equals("Y")) {
	            comcr.errRtn("update_tsc_bin_curr  not found!", hBinNo, hCallBatchSeqno);
	        }

	        daoTable = "tsc_dcrp_log";
	        setValue("tsc_card_no"       , hTempX16);
	        setValue("card_no"           , hDcesCardNo);
	        setValue("tsc_emboss_rsn"    , hDcesEmbossSource);
	        setValue("tsc_vendor_cd"     , hTempX10);
	        setValue("emboss_date"       , sysDate);
	        setValueLong("upper_lmt"     , hUpperLmt);
	        setValueLong("upper_lmt_acmm", hUpperLmtAcmm);
	        setValue("mod_time"          , sysDate + sysTime);
	        setValue("mod_pgm"           , "DbcD003");
	        insertTable();
	        if (dupRecord.equals("Y")) {
	            comcr.errRtn("insert_tsc_dcrp_log duplicate!", "", hCallBatchSeqno);
	        }

	        return (0);
	}
	/***********************************************************************/
	void updateEmboss() throws Exception {
		String hTransCvv2 = "";

		if (debug == 1)
			showLogMessage("I", "", "Update=" + hDcesCvv2 + "," + hDcesPvv + "," + hDcesPvki);

		hTransCvv2 = "";
		hTransCvv2 = hDcesCvv2;
		hTransCvv2 = comc.transPasswd(0, hTransCvv2);

		daoTable = "dbc_emboss";
		updateSQL = " pvv        = ?,";
		updateSQL += " cvv        = ?,";
		updateSQL += " trans_cvv2 = ?,";
		updateSQL += " csc        = ?,";
		updateSQL += " pin_block  = ?,";
		updateSQL += " pvki       = ?,";
		updateSQL += " ic_cvv     = ?,";
		updateSQL += " mod_time   = sysdate,";
		updateSQL += " mod_pgm    = ?,";
		updateSQL += " mod_user   = ?";
		whereStr = "where rowid = ? ";
		setString(1, hDcesPvv);
		setString(2, hDcesCvv);
		setString(3, hTransCvv2);
		setString(4, hDcesCsc);
		setString(5, hDcesPinBlock);
		setString(6, hDcesPvki);
		setString(7, hDcesIcCvv);
		setString(8, prgmId);
		setString(9, comc.commGetUserID());
		setRowId(10, hDcesRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_emboss not found!", "", hDcesCardNo);
		}
	}

	/***********************************************************************/
	void getIcvv() throws Exception {
		String realCardno = "";
		String tmpstr = "";

		gcvvReq.msgHeader = "BANK";
		gcvvReq.cmdCode = "CW";
		gcvvReq.cvkPair1 = hPktbEcsIcvv1;
		gcvvReq.cvkPair2 = hPktbEcsIcvv2;
		if (hDcesCardNo.substring(0, 4).equals("4000")) {
			realCardno = "9000";
		} else {
			realCardno = hDcesCardNo.substring(0, 4);
		}
		realCardno += hDcesCardNo.substring(4);
		gcvvReq.cardNo = realCardno;
		gcvvReq.delimiter1 = ";";
		gcvvReq.expireDate = String.format("%4.4s", hDcesValidTo.substring(2));
		gcvvReq.serviceCode = "999";
		gcvvReq.delimiter = Character.toString((char) 25);
		gcvvReq.msgEnder = "BANK";
		String tmp = callRacal("CW");
		splitCvvOutbuf(tmp);
		if (!gcvvRep.errorCode.equals("00")) {
			comcr.errRtn("RECAL CW cvv2 process error!", "", comcr.hCallBatchSeqno);
		}
		hDcesIcCvv = gcvvRep.cvv;

		tmpstr = String.format("%3.3s", gcvvRep.cvv);
		hDcesIcCvv = tmpstr;
		return;
	}

	/***********************************************************************/
	void getCvv() throws Exception {
		String realCardno = "";
		String tmpstr = "";

		gcvvReq.msgHeader = "BANK";
		gcvvReq.cmdCode = "CW";
		gcvvReq.cvkPair1 = hPktbEcsCvk1;
		gcvvReq.cvkPair2 = hPktbEcsCvk2;
		if (hDcesCardNo.substring(0, 4).equals("4000")) {
			realCardno = "9000";
		} else {
			realCardno = hDcesCardNo.substring(0, 4);
		}
		realCardno += hDcesCardNo.substring(4);
		gcvvReq.cardNo = realCardno;
		gcvvReq.delimiter1 = ";";
		gcvvReq.expireDate = String.format("%4.4s", hDcesValidTo.substring(2));
		gcvvReq.serviceCode = hDcesServiceCode;
		gcvvReq.delimiter = Character.toString((char) 25);
		gcvvReq.msgEnder = "BANK";
		if (debug == 1)
			showLogMessage("I", "", "  step 3.1=" + gcvvReq.allText());
		String tmp = callRacal("CW");
		if (debug == 1)
			showLogMessage("I", "", "  step 3.2=" + tmp);
		splitCvvOutbuf(tmp);
		if (!gcvvRep.errorCode.equals("00")) {
			showLogMessage("I", "", String.format("888 buf=[%s]", gcvvReq));
			comcr.errRtn(String.format("RECAL CW cvv2 process error[%2.2s]!", gcvvRep.errorCode), "",
					comcr.hCallBatchSeqno);
		}
		hDcesCvv = gcvvRep.cvv;

		tmpstr = String.format("%3.3s", gcvvRep.cvv);
		hDcesCvv = tmpstr;
		/**************************************
		 * cvv2 service code改為'000'
		 **************************************/
		gcvvReq.serviceCode = "000";
		if (debug == 1)
			showLogMessage("I", "", "  step 4.1=" + gcvvReq.allText());
		tmp = callRacal("CW");
		if (debug == 1)
			showLogMessage("I", "", "  step 4.2=" + tmp);
		splitCvvOutbuf(tmp);
		if (!gcvvRep.errorCode.equals("00")) {
			comcr.errRtn("RECAL CW cvv2 1            !", "", comcr.hCallBatchSeqno);
		}
		tmpstr = String.format("%3.3s", gcvvRep.cvv);
		hDcesCvv2 = tmpstr;
	}

	/***********************************************************************/
	void getPvv() throws Exception {
		String tmpstr = "";

		gpvvReq.msgHeader = "BANK";
		gpvvReq.cmdCode = "DG";
		gpvvReq.pvkPair1 = hPktbEcsPvk1;
		gpvvReq.pvkPair2 = hPktbEcsPvk2;
		gpvvReq.pin = gpinRep.pin;
		gpvvReq.cardNo = String.format("%12.12s", hDcesCardNo.substring(3));
		gpvvReq.pvki = hDcesPvki;
		gpvvReq.delimiter = Character.toString((char) 25);
		gpvvReq.msgEnder = "BANK";

		if (debug == 1)
			showLogMessage("I", "", "  step 2.1=" + gpvvReq.allText());
		String tmp = callRacal("DG");
		if (debug == 1)
			showLogMessage("I", "", "  step 2.2=" + tmp);
		splitPvvOutbuf(tmp);

		if (!gpvvRep.errorCode.substring(0, 2).equals("00")) {
			comcr.errRtn("RECAL DG process error!", "", hCallBatchSeqno);
		}
		tmpstr = String.format("%4.4s", gpvvRep.pvv);
		hDcesPvv = tmpstr;
		return;
	}

	/***********************************************************************/
	void getCsc() throws Exception {
		gcscReq.msgHeader = "BANK";
		gcscReq.cmdCode = "RY";
		gcscReq.mode = "3";
		gcscReq.flag = "0";
		gcscReq.csck1 = hPktbEcsCsck1;
		gcscReq.cardNo = hCardAe;
		gcscReq.expireDate = String.format("%4.4s", hDcesValidTo.substring(2));
		gcscReq.delimiter = Character.toString((char) 25);
		gcscReq.msgEnder = "BANK";

		String tmp = callRacal("RY");
		splitCscOutbuf(tmp);

		if (!gcscRep.errorCode.equals("00")) {
			comcr.errRtn("RECAL DG process error!", "", hCallBatchSeqno);
		}
		hDcesCsc = gcscRep.csc1;
		hDcesCvv2 = gcscRep.csc3;
	}

	/***********************************************************************/
	void getPin() throws Exception {
		String tmpstr = "";
		String tmpStr1 = "";
		gpinReq.msgHeader = "BANK";
		gpinReq.cmdCode = "JA";

		tmpStr1 = hDcesCardNo.substring(1, 2);
		if ((hCardAe.length() == 19) && (tmpStr1.equals("3")))
			gpinReq.cardNo = String.format("%12.12s", hDcesCardNo.substring(2));
		else
			gpinReq.cardNo = String.format("%12.12s", hDcesCardNo.substring(3));

		gpinReq.pinLen = "04";
		gpinReq.delimiter = Character.toString((char) 25);
		;
		gpinReq.msgEnder = "BANK";

		if (debug == 1)
			showLogMessage("I", "", "  step 1.1=" + gpinReq.allText());
		String tmp = callRacal("JA");
		if (debug == 1)
			showLogMessage("I", "", "  step 1.2=" + tmp);
		splitPinOutbuf(tmp);

		if (!gpinRep.errorCode.substring(0, 2).equals("00")) {
			comcr.errRtn(String.format("RECAL JA process error!", gpinRep.errorCode), "", hCallBatchSeqno);
		}
		tmpstr = String.format("%5.5s", gpinRep.pin);
		hDcesPinBlock = tmpstr;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcD003 proc = new DbcD003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	String callRacal(String type) throws IOException {
		String sLResult = "";

		HsmUtil lHsmUtil = new HsmUtil(racalServer, racalPort);

		try {
			switch (type) {
			case "JA":
				sLResult = lHsmUtil.hsmCommandJA(gpinReq.cardNo, gpinReq.pinLen);
				break;
			case "DG":
				sLResult = lHsmUtil.hsmCommandDG(gpvvReq.pvkPair1 + gpvvReq.pvkPair2, gpvvReq.pin,
						gpvvReq.cardNo, gpvvReq.pvki);
				break;
			case "CW":
				sLResult = lHsmUtil.hsmCommandCW(gcvvReq.cardNo, gcvvReq.expireDate, gcvvReq.serviceCode,
						gcvvReq.cvkPair1, gcvvReq.cvkPair2);
				break;
			case "RY": // CrdD003 proC有type[RY]
				sLResult = lHsmUtil.hsmCommandRY(gcscReq.mode, gcscReq.flag, gcscReq.csck1, gcscReq.cardNo,
						gcscReq.expireDate, gcscReq.cmdCode, ""/* sP_Zmk */
						, ""/* sP_5DigitCsc */, ""/* sP_4DigitCsc */, ""/* sP_3DigitCsc */);
				break;
			default:
				showLogMessage("I", "", " HsmUtil =[" + type + " != 'JA','DG','CW' " + "]");
			}

			showLogMessage("I", "", "  888 HsmUtil R=[" + sLResult + "]");
			if ("00".equals(sLResult.substring(0, 2))) {
				showLogMessage("I", "", "  成功，Result== " + sLResult.substring(2, sLResult.length()) + "]");
			} else {
				showLogMessage("I", "", "  失敗，Result== " + sLResult + "]");
				comcr.errRtn("RECAL " + type + " process error!", "", comcr.hCallBatchSeqno);
			}

			return sLResult;

		} catch (Exception e) {
			showLogMessage("I", "", "  Error HsmUtil !");
			e.printStackTrace();
		}
		return sLResult;
	}

	/***********************************************************************/
	class pinInbuf {
		String msgHeader;
		String cmdCode;
		String cardNo;
		String pinLen;
		String delimiter;
		String msgEnder;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(cmdCode, 2);
			rtn += comc.fixLeft(cardNo, 12);
			rtn += comc.fixLeft(pinLen, 2);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(msgEnder, 4);
			rtn += comc.fixLeft(filler, 75);
			return rtn;
		}
	}

	/***********************************************************************/
	class pinOutbuf {
		String msgHeader;
		String respCode;
		String errorCode;
		String pin;
		String delimiter;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(respCode, 2);
			rtn += comc.fixLeft(errorCode, 2);
			rtn += comc.fixLeft(pin, 5);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(filler, 60);
			return rtn;
		}

	}

	void splitPinOutbuf(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		gpinRep.errorCode = comc.subMS950String(bytes, 0, 2);
		gpinRep.pin = comc.subMS950String(bytes, 2, 5);
		/*
		 * mark gpin_rep.msg_header = comc.subMS950String(bytes, 0, 4);
		 * gpin_rep.resp_code = comc.subMS950String(bytes, 4, 2); gpin_rep.error_code =
		 * comc.subMS950String(bytes, 6, 2); gpin_rep.pin = comc.subMS950String(bytes,
		 * 8, 5); gpin_rep.delimiter = comc.subMS950String(bytes, 13, 1);
		 * gpin_rep.filler = comc.subMS950String(bytes, 14, 60);
		 */
	}

	/***********************************************************************/
	class pvvInbuf {
		String msgHeader;
		String cmdCode;
		String pvkPair1;
		String pvkPair2;
		String pin;
		String cardNo;
		String pvki;
		String delimiter;
		String msgEnder;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(cmdCode, 2);
			rtn += comc.fixLeft(pvkPair1, 16);
			rtn += comc.fixLeft(pvkPair2, 16);
			rtn += comc.fixLeft(pin, 5);
			rtn += comc.fixLeft(cardNo, 12);
			rtn += comc.fixLeft(pvki, 1);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(msgEnder, 4);
			rtn += comc.fixLeft(filler, 39);
			return rtn;
		}
	}

	/***********************************************************************/
	class pvvOutbuf {
		String msgHeader;
		String respCode;
		String errorCode;
		String pvv;
		String delimiter;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(respCode, 2);
			rtn += comc.fixLeft(errorCode, 2);
			rtn += comc.fixLeft(pvv, 4);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(filler, 61);
			return rtn;
		}
	}

	void splitPvvOutbuf(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		gpvvRep.errorCode = comc.subMS950String(bytes, 0, 2);
		gpvvRep.pvv = comc.subMS950String(bytes, 2, 4);
		/*
		 * mark gpvv_rep.msg_header = comc.subMS950String(bytes, 0, 4);
		 * gpvv_rep.resp_code = comc.subMS950String(bytes, 4, 2); gpvv_rep.error_code =
		 * comc.subMS950String(bytes, 6, 2); gpvv_rep.pvv = comc.subMS950String(bytes,
		 * 8, 4); gpvv_rep.delimiter = comc.subMS950String(bytes, 12, 1);
		 * gpvv_rep.filler = comc.subMS950String(bytes, 13, 61);
		 */
	}

	/***********************************************************************/
	class cvvInbuf {
		String msgHeader;
		String cmdCode;
		String cvkPair1;
		String cvkPair2;
		String cardNo;
		String delimiter1;
		String expireDate;
		String serviceCode;
		String delimiter;
		String msgEnder;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(cmdCode, 2);
			rtn += comc.fixLeft(cvkPair1, 16);
			rtn += comc.fixLeft(cvkPair2, 16);
			rtn += comc.fixLeft(cardNo, 16);
			rtn += comc.fixLeft(delimiter1, 1);
			rtn += comc.fixLeft(expireDate, 4);
			rtn += comc.fixLeft(serviceCode, 3);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(msgEnder, 4);
			rtn += comc.fixLeft(filler, 37);
			return rtn;
		}
	}

	/***********************************************************************/
	class cvvOutbuf {
		String msgHeader;
		String respCode;
		String errorCode;
		String cvv;
		String delimiter;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(respCode, 2);
			rtn += comc.fixLeft(errorCode, 2);
			rtn += comc.fixLeft(cvv, 3);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(filler, 62);
			return rtn;
		}
	}

	void splitCvvOutbuf(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		gcvvRep.errorCode = comc.subMS950String(bytes, 0, 2);
		gcvvRep.cvv = comc.subMS950String(bytes, 2, 3);
		/*
		 * mark gcvv_rep.msg_header = comc.subMS950String(bytes, 0, 4);
		 * gcvv_rep.resp_code = comc.subMS950String(bytes, 4, 2); gcvv_rep.error_code =
		 * comc.subMS950String(bytes, 6, 2); gcvv_rep.cvv = comc.subMS950String(bytes,
		 * 8, 3); gcvv_rep.delimiter = comc.subMS950String(bytes, 11, 1);
		 * gcvv_rep.filler = comc.subMS950String(bytes, 12, 62);
		 */
	}

	/***********************************************************************/
	class cscInbuf {
		String msgHeader;
		String cmdCode;
		String mode;
		String flag;
		String csck1;
		String cardNo;
		String expireDate;
		String delimiter;
		String msgEnder;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(cmdCode, 2);
			rtn += comc.fixLeft(mode, 1);
			rtn += comc.fixLeft(flag, 1);
			rtn += comc.fixLeft(csck1, 32);
			rtn += comc.fixLeft(cardNo, 19);
			rtn += comc.fixLeft(expireDate, 4);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(msgEnder, 4);
			rtn += comc.fixLeft(filler, 63);
			return rtn;
		}
	}

	/***********************************************************************/
	class cscOutbuf {
		String msgHeader;
		String respCode;
		String errorCode;
		String mode;
		String csc1;
		String csc2;
		String csc3;
		String delimiter;
		String msgEnder;
		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(msgHeader, 4);
			rtn += comc.fixLeft(respCode, 2);
			rtn += comc.fixLeft(errorCode, 2);
			rtn += comc.fixLeft(mode, 1);
			rtn += comc.fixLeft(csc1, 5);
			rtn += comc.fixLeft(csc2, 4);
			rtn += comc.fixLeft(csc3, 3);
			rtn += comc.fixLeft(delimiter, 1);
			rtn += comc.fixLeft(msgEnder, 4);
			rtn += comc.fixLeft(filler, 37);
			return rtn;
		}
	}

	void splitCscOutbuf(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		gcscRep.errorCode = comc.subMS950String(bytes, 0, 2);
		gcscRep.mode = comc.subMS950String(bytes, 2, 1);
		gcscRep.csc1 = comc.subMS950String(bytes, 3, 5);
		gcscRep.csc2 = comc.subMS950String(bytes, 8, 4);
		gcscRep.csc3 = comc.subMS950String(bytes, 12, 3);
		/*
		 * mark gcsc_rep.msg_header = comc.subMS950String(bytes, 0, 4);
		 * gcsc_rep.resp_code = comc.subMS950String(bytes, 4, 2); gcsc_rep.error_code =
		 * comc.subMS950String(bytes, 6, 2); gcsc_rep.mode = comc.subMS950String(bytes,
		 * 8, 1); gcsc_rep.csc1 = comc.subMS950String(bytes, 9, 5); gcsc_rep.csc2 =
		 * comc.subMS950String(bytes, 14, 4); gcsc_rep.csc3 = comc.subMS950String(bytes,
		 * 18, 3); gcsc_rep.delimiter = comc.subMS950String(bytes, 21, 1);
		 * gcsc_rep.msg_ender = comc.subMS950String(bytes, 22, 4); gcsc_rep.filler =
		 * comc.subMS950String(bytes, 26, 37);
		 */
	}
	// ***************************************************************************

	String getSqlVMJ(String aCardNo) throws Exception {
		String sqls = "";
		sqlCmd = "select bin_type from ptr_bintable where bin_no || bin_no_2_fm || '0000' <= ? ";
		sqlCmd += " and bin_no || bin_no_2_to || '9999' >= ? ";
		setString(1, aCardNo);
		setString(2, aCardNo);
		selectTable();
		String aBinType = getValue("bin_type");
		switch (aBinType) {
		case "V":
			sqls = " visa_pvka as ecs_pvk1, ";
			sqls += " visa_pvkb as ecs_pvk2, ";
			sqls += " visa_cvka as ecs_cvk1, ";
			sqls += " visa_cvkb as ecs_cvk2, ";
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		case "M":
			sqls = " master_pvka as ecs_pvk1, ";
			sqls += " master_pvkb as ecs_pvk2, ";
			sqls += " master_cvka as ecs_cvk1, ";
			sqls += " master_cvkb as ecs_cvk2, ";
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		case "J":
			sqls = " jcb_pvka as ecs_pvk1, ";
			sqls += " jcb_pvkb as ecs_pvk2, ";
			sqls += " jcb_cvka as ecs_cvk1, ";
			sqls += " jcb_cvkb as ecs_cvk2, ";
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		default:
			sqls = " '' as ecs_pvk1, ";
			sqls += " '' as ecs_pvk2, ";
			sqls += " '' as ecs_cvk1, ";
			sqls += " '' as ecs_cvk2, ";
			sqls += " '' as ecs_icvv1, ";
			sqls += " '' as ecs_icvv2, ";
			break;
		}

		return sqls;
	}
}
