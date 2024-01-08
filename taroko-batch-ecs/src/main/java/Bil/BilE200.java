/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/10/12  V1.00.00    JeffKung  program initial                          *
*  111/02/14  V1.00.01    Ryan      big5 to MS950                            *
*  111/12/22  V1.00.02    JeffKung  Phase3修改                                                               *
*  112/06/01  V1.00.03    JeffKung  增加效期與營業日的比較                                          *
*  112/07/21  V1.00.04    JeffKung  add hReversalFlag                        *
*  112/08/30  V1.00.05    JeffKung  add hBypassIdCheck                       *
*  112/11/03  V1.00.06    JeffKung  auth system problem handle               *
*****************************************************************************/
package Bil;

import java.text.Normalizer;
import java.util.Arrays;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;
import com.CommFunction;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGateway;

public abstract class BilE200 extends AccessDAO {
    private  final String PROGNAME = "ONUS請款檔抽象類別 112/11/03 V1.00.06";
    
    boolean debug = false;
    
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommFunction comm = new CommFunction();
    CommTxBill commTxBill = null;
    String fctlNo = "";
    String hBusinessDate = "";
    
    public String hBillUnit = "SK";
    public String hBillType = "SKIC";
    public String hFileType = "SKI";
    public String hReversalFlag = "N";
    public String hBypassIdCheck = "N";
    String hBatchSeq = "";
    
    /*
     * For PTR_BINTYPE use
     */
    String hPtrBintableCurrencyCode = "";
    String hPtrBintableBinType = "";
    String hPtrBintableDebitFlag = "";
    
    int hPostBatchSeq = 0;
    String hPostBatchDate = "";
    String hPostBatchUnit = "";
    String hPostBatchNo = "";
    String hPostConfirmFlag = "";
    String hPostConfirmFlagP = "";
    int hPostTotalCnt = 0;
    double hPostTotalAmt = 0.0;
    String hPtrBillunitConfFlag = "";
    
    /*
     * For CardRecord use
     */
    String hCrdCardNo = "";
    String hCrdPSeqno = "";
    String hCrdAcnoPSeqno = "";
    String hCrdIdPSeqno = "";
    String hCrdMajorIdPSeqno = "";
    String hCrdCardId = "";
    String hCrdCardIdCode = "";
    String hCrdCorpNo = "";
    String hCrdCardType = "";
    String hCrdCardUrgentFlag = "";
    String hCrdGroupCode = "";
    String hCrdSourceCode = "";
    String hCrdSupFlag = "";
    String hCrdMemberId = "";
    String hCrdCurrentCode = "";
    String hCrdEngName = "";
    String hCrdRegBankNo = "";
    String hCrdUnitCode = "";
    String hCrdOldBegDate = "";
    String hCrdOldEndDate = "";
    String hCrdNewBegDate = "";
    String hCrdNewEndDate = "";
    String hCrdIssueDate = "";
    String hCrdChangeDate = "";
    String hCrdMajorCardNo = "";
    String hCrdApplyNo = "";
    String hCrdPromoteDept = "";
    String hCrdMajorRelation = "";
    String hCrdOppostReason = "";
    String hCrdOppostDate = "";
    String hCrdNewCardNo = "";
    String hCrdOldCardNo = "";
    String hCrdAcctType = "";
    String hCrdPromoteEmpNo = "";
    String hCrdIntroduceId = "";
    String hCrdIntroduceName = "";
    String hCrdProdNo = "";
    String hCrdFeeCode = "";
    String hCrdExpireChgFlag = "";
    String hCrdActivateFlag = "";
    String hCrdActivateDate = "";
    String hCrdSonCardFlag = "";
    String hCrdSetCode = "";
    String hCrdMailType = "";
    String hCrdMailNo = "";
    String hCrdCreateDate = "";
    String hCrdCreateId = "";
    
    /*
     * For AcnoRecord use
     */
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo= "";
    String hAcnoAutoInstallment = "";

    /*
     * For PtrBillTypeRecord use
     */
    String hBityBillType = "";
    String hBityTxnCode = "";
    String hBitySignFlag = "";
    String hBityAcctCode = "";
    String hBityAcctItem = "";
    String hBityFeesState = "";
    double hBityFeesFixAmt = 0;
    double hBityFeesPercent = 0;
    double hBityFeesMin = 0;
    double hBityFeesMax = 0;
    String hBityFeesBillType = "";
    String hBityFeesTxnCode = "";
    String hBityInterDesc = "";
    String hBityExterDesc = "";
    String hBityInterestMode = "";
    String hBityAdvWkday = "";
    String hBityBalanceState = "";
    String hBityCollectionMode = "";
    String hBityChargeBackMonth = "";
    String hBityCashAdvState = "";
    String hBityEntryAcct = "";
    String hBityChkErrBill = "";
    String hBityDoubleChk = "";
    String hBityFormatChk = "";
    double hBityMerchFee = 0;
    
    /*
     * For PtrActcodeRecord use
     */
    String hPtrActcodeQueryType = "";
    String hPtrActcodeAcctCode = "";
    String hPtrActcodeChiShortName = "";
    String hPtrActcodeChiLongName = "";
    String hPtrActcodeEngShortName = "";
    String hPtrActcodeEngLongName = "";
    String hPtrActcodeItemClassNormal = "";
    String hPtrActcodeItemClassBackDate = "";
    String hPtrActcodeItemClassRefund = "";
    String hPtrActcodeItemOrderNormal = "";
    String hPtrActcodeItemOrderBackDate = "";
    String hPtrActcodeItemOrderRefund = "";
    String hPtrActcodeInterRateCode = "";
    String hPtrActcodePartRev = "";
    String hPtrActcodeRevolve = "";
    String hPtrActcodeAcctMethod = "";
    String hPtrActcodeInterestMethod = "";
    String hPtrActcodeUrge1st = "";
    String hPtrActcodeUrge2st = "";
    String hPtrActcodeUrge3st = "";
    String hPtrActcodeOccupy = "";
    String hPtrActcodeReceivables = "";
    String hPtrActcodeFileDate = "";
    String hPtrActcodeFileTime = "";
    
    protected abstract int inputFileProcess() throws Exception;

    protected abstract int outputFileProcess() throws Exception;

    public void setConn(Connection conn[], String[] dbAlias) throws Exception {
        super.conn = conn;
        setDBalias(dbAlias);
        setSubParm(dbAlias);
    }

    public void processStart() throws Exception {

        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        comm = new CommFunction();

        int rc = 0;
        
        showLogMessage("I", "", "開始處理上傳檔案....");
        
        /*不管檔案處理結果, 皆要繼續做明細資料處理
         * (1) 資料前一次已寫入bil_onusbill_exchange --> rerun作業
         * (2) 可能是首尾筆檢核失敗 , 最後仍舊要產出回覆檔
        */
        rc = inputFileProcess();                  // 讀ECS_FTP_LOG取出要處理的資料檔
        commitDataBase();

        showLogMessage("I", "", "開始處理明細資料....");
        dataProcess();// 明細資料處理
        commitDataBase();
       

        showLogMessage("I", "", "開始處理下傳資料....");
        outputFileProcess();// 產出回覆檔

    }

    /***********************************************************************/
    private void dataProcess() throws Exception {
    	
    	daoTable = "BIL_ONUSBILL_EXCHANGE";
    	fetchExtend = "chkboe.";
		sqlCmd = " select mcht_category,mcht_chi_name,reference_no,purchase_date,txn_code,auth_code,";
		sqlCmd += "	branch,id_no,card_no,expire_yymm,expire_mmyy,tran_code,dest_amt, ";
		sqlCmd += "	dest_curr,extra_info,rowid as rowid ";
		sqlCmd += "  from BIL_ONUSBILL_EXCHANGE ";
		sqlCmd += " where 1=1 ";
		sqlCmd += " and file_date = ?  ";
		sqlCmd += " and file_type = ?  ";
		sqlCmd += " and rec_type = '2'  ";   //明細資料
		sqlCmd += " and status_flag = '99'  ";  //未處理
		sqlCmd += " and post_flag in ('','0')  ";

		setString(1, hBusinessDate);
		setString(2, hFileType);
		
		showLogMessage("I", "", "本日營業日-hBusinessDate=["+hBusinessDate+"]");
		
		int totalProcCnt = 0;
		int successCnt = 0;
		
	
		openCursor();
		while (fetchTable()) {
			
			totalProcCnt ++;
		
			String statusFlag = "00";
			String authCode = "";
			String blockCode = "";
			
			// every 500 display message
			if (totalProcCnt % 500 == 0 || totalProcCnt==1 ) {
			      showLogMessage("I", "processDisplay", "PROCESS COUNT : " + totalProcCnt);
			}
			
			
			String[] resultSet = dataCheck();  //明細資料查核
			statusFlag = resultSet[0];
			authCode = resultSet[1];
			blockCode = resultSet[2];
			if (!statusFlag.equals("99")) {
				updateBilOnusbillExchange(statusFlag,authCode,blockCode);
			} else {
				//如果授權回99代表系統有問題,讓程式abend
				throw new Exception("Auth System has problem...");
			}
			
			/* 批次取授權尚未Ready, 先remark
			statusFlag = sendCcas(""); // 批次取授權
			if (!statusFlag.equals("00")) {
				updateBilOnusbillExchange(statusFlag);
				continue;
			}
			*/
			
			//成功的交易才寫入bil_curpost
			if (statusFlag.equals("00")) {
				successCnt++;
			
				if (successCnt==1) {
					selectPtrBillunit();
				
				}
			
				insertBilCurpost(authCode);
			}
		}
		
		closeCursor();
    }

    /***********************************************************************/
    private String[] dataCheck() throws Exception {

    	int rc=0;
    	String statusFlag = "00";
    	String authCode = "";
    	String blockCode = "";

    	String tCardNo = getValue("chkboe.card_no");
    	String tCardId = getValue("chkboe.id_no");
    	String tExpireDate = formatDate(getValue("chkboe.expire_yymm"),getValue("chkboe.expire_mmyy"));
    	
    	rc = checkCardNo(tCardNo);

    	if (rc!=0) {
    		statusFlag = "04";   //卡號錯誤或卡號不存在
    	} else if ("0".equals(hCrdCurrentCode)==false) {
    		statusFlag = "06";   //卡片控管碼
    		if ("1".equals(hCrdCurrentCode) ) {
    			blockCode = "A";
    		} else if ("2".equals(hCrdCurrentCode)) {
    			blockCode = "L";
    		} else if ("3".equals(hCrdCurrentCode)) {
    			blockCode = "B";
    		} else if ("4".equals(hCrdCurrentCode)) {
    			blockCode = "E";
    		} else if ("5".equals(hCrdCurrentCode)) {
    			blockCode = "F";
    		}
    	} else if (tCardId.length() > 0 && 
    			   (tCardId.equals(hCrdCardId)==false && tCardId.equals(hCrdCorpNo)==false ) )  {
    		if ("Y".equals(hBypassIdCheck)) {
    			;  //如果bypassID檢查,不用上錯誤碼
    		} else {
    			statusFlag = "09";   //持卡人ID與卡號不為同一人(可能有corp_no)
    		}
     	} else if (tExpireDate.equals(hCrdNewEndDate)==false && tExpireDate.equals(hCrdOldEndDate)==false ) {
     		statusFlag = "05";   //效期錯誤
     	} else if (tExpireDate.compareTo(hBusinessDate) < 0) {
     		statusFlag = "05";   //已過效期
     	}
    	
    	/* 1. 卡片狀態不為正常卡:  (current_code !=0)
    	 *    A: 申停、B: 停用(人工)、E: 停用(系統停用)、F: 偽卡停用、L: 掛失、
    	 *    M:陽光AMC、N:換發晶片卡、O: 偽冒不轉卡、S: 掛失不轉卡、T: 暫停卡
    	 * 2.
    	 *********************************************************************************
    	 *   00：成功
         *   01：檔案日期、筆數或總金額不符。
         * V 02：未開卡。 (取批次授權的結果p39=51)
         *   03：卡別錯誤。
         * V 04：卡號錯誤或卡號不存在。
         * V 05：效期錯誤。
         * V 06：卡片控管碼為A、B、E、F、L、M、N、O、S、T 
         *   07：卡片控管碼為C、P(即逾期戶)。
         * V 08：額度不足。(含控管碼為H者)。
         * V 09：持卡人ID與卡號不為同一人。
         *   10：其他
         **********************************************************************************
    	 * */
    	
    	selectActAcno();
    	
    	//基本資料檢核通過, 且為正向交易才取授權 (reversal不用取授權)
    	if (statusFlag.equals("00") && "05".equals(getValue("chkboe.txn_code"))
    		&& "N".equals(hReversalFlag)	) {
    		
    		//20231103如果是超過6秒, retry 3次
    		String sLTranxResult = "";
    		String ccasRespCode = "";
			for (int r = 0; r < 3; r++) {
				sLTranxResult = callAuth();

				// sLTranxResult 八碼00(P39) 成功 加六碼授權碼(P38)
				ccasRespCode = comcr.mid(sLTranxResult, 1, 2);
				if (ccasRespCode.equals("00")) {
					authCode = comcr.mid(sLTranxResult, 3, 6);
					break;
				}  else if ("54".equals(ccasRespCode)) {
					statusFlag = "02"; // 未開卡
					break;
				} else if ("96".equals(ccasRespCode)) {
					statusFlag = "10"; // 系統問題
					break;
				} else if (ccasRespCode.equals("98")) {
					//98 timeout retry
					sLTranxResult = "";
					continue;
				} else if (ccasRespCode.equals("99")) {
					//99 授權系統有問題讓程式abend
					statusFlag = "99";
					break;
				} else {
					statusFlag = "08"; // 08：額度不足。
					break;
				}
			}
    		
    		//test
    		//statusFlag = "00";
    		//authCode   = "123456";
    		
    		showLogMessage("I", "", "cardNO= " + tCardNo + " Debug - ccasRespCode,authCode : [" + ccasRespCode +  "],[" + authCode + "]");
    		if (debug) {
    			showLogMessage("I", "", "Debug - ccasRespCode,authCode : [" + ccasRespCode +  "],[" + authCode + "]");
    		}
     	}
    	
		return new String[] {statusFlag,authCode,blockCode};
    }

    /***********************************************************************/
    private int checkCardNo(String cardNo) throws Exception {
    	
    	if(debug) {
    		showLogMessage("I","","Debug---checkCardNo: cardNo =" + cardNo); //debug
    	}
    	
    	int rcCheckCardNo = -1;
    	//判斷是CreditCard或是DebitCard
    	selectPtrBintable(cardNo);
    	
    	initCrdCard();
    	
        if (hPtrBintableDebitFlag.equals("Y")) {
        	extendField = "dbc.";
            sqlCmd = "select a.major_card_no,";
            sqlCmd += "a.current_code,";
            sqlCmd += "a.issue_date,";
            sqlCmd += "a.oppost_date,";
            sqlCmd += "a.promote_dept,";
            sqlCmd += "a.prod_no,";
            sqlCmd += "a.group_code,";
            sqlCmd += "a.source_code,";
            sqlCmd += "a.card_type,";
            sqlCmd += "a.p_seqno,";
            sqlCmd += "a.major_id_p_seqno, ";
            sqlCmd += "a.id_p_seqno, ";
            sqlCmd += "a.new_end_date, ";
            sqlCmd += "a.old_end_date, ";
            sqlCmd += "nvl(b.id_no,'') as id_no ";
            sqlCmd += "from dbc_card a left join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
            sqlCmd += "where a.card_no  = ? ";
            setString(1, cardNo);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
            	rcCheckCardNo = 0;
                hCrdMajorCardNo = getValue("dbc.major_card_no");
                hCrdCurrentCode = getValue("dbc.current_code");
                hCrdIssueDate = getValue("dbc.issue_date");
                hCrdOppostDate = getValue("dbc.oppost_date");
                hCrdPromoteDept = getValue("dbc.promote_dept");
                hCrdProdNo = getValue("dbc.prod_no");
                hCrdGroupCode = getValue("dbc.group_code");
                hCrdSourceCode = getValue("dbc.source_code");
                hCrdCardType = getValue("dbc.card_type");
                hCrdPSeqno = getValue("dbc.p_seqno");
                hCrdIdPSeqno = getValue("dbc.id_p_seqno");
                hCrdMajorIdPSeqno = getValue("dbc.major_id_p_seqno");
                hCrdNewEndDate = getValue("dbc.new_end_date");
                hCrdOldEndDate = getValue("dbc.old_end_date");
                hCrdCardId = getValue("dbc.id_no");
            }
        } else {
        	extendField = "crd.";
            sqlCmd = "select a.major_card_no,";
            sqlCmd += "a.current_code,";
            sqlCmd += "a.issue_date,";
            sqlCmd += "a.oppost_date,";
            sqlCmd += "a.promote_dept,";
            sqlCmd += "a.prod_no,";
            sqlCmd += "a.group_code,";
            sqlCmd += "a.source_code,";
            sqlCmd += "a.card_type,";
            sqlCmd += "a.acct_type,";
            sqlCmd += "a.p_seqno,";
            sqlCmd += "a.acno_p_seqno,";
            sqlCmd += "a.major_id_p_seqno,";
            sqlCmd += "a.id_p_seqno, ";
            sqlCmd += "a.new_end_date, ";
            sqlCmd += "a.old_end_date, ";
            sqlCmd += "a.corp_no, ";
            sqlCmd += "nvl(b.id_no,'') as id_no ";
            sqlCmd += "from crd_card a left join crd_idno b on a.id_p_seqno = b.id_p_seqno  ";
            sqlCmd += "where a.card_no  = ? ";
            setString(1, cardNo);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
            	rcCheckCardNo = 0;
            	hCrdMajorCardNo = getValue("crd.major_card_no");
                hCrdCurrentCode = getValue("crd.current_code");
                hCrdIssueDate = getValue("crd.issue_date");
                hCrdOppostDate = getValue("crd.oppost_date");
                hCrdPromoteDept = getValue("crd.promote_dept");
                hCrdProdNo = getValue("crd.prod_no");
                hCrdGroupCode = getValue("crd.group_code");
                hCrdSourceCode = getValue("crd.source_code");
                hCrdCardType = getValue("crd.card_type");
                hCrdAcctType = getValue("crd.acct_type");
                hCrdPSeqno = getValue("crd.p_seqno");
                hCrdAcnoPSeqno = getValue("crd.acno_p_seqno");
                hCrdMajorIdPSeqno = getValue("crd.major_id_p_seqno");
                hCrdIdPSeqno = getValue("crd.id_p_seqno");
                hCrdNewEndDate = getValue("crd.new_end_date");
                hCrdOldEndDate = getValue("crd.old_end_date");
                hCrdCardId = getValue("crd.id_no");
                hCrdCorpNo = getValue("crd.corp_no");
            }
        }
        return rcCheckCardNo;
    }
    
	/***********************************************************************/
	void selectActAcno() throws Exception {

		hAcnoAcctType = "";
		hAcnoAcctKey = "";
		hAcnoAcctStatus = "";
		hAcnoStmtCycle = "";
		hAcnoPayByStageFlag = "";
		hAcnoAutopayAcctNo = "";
		hAcnoAutoInstallment = "";

		if (hPtrBintableDebitFlag.equals("Y")) {
			sqlCmd = "select acct_type,";
			sqlCmd += "acct_key,";
			sqlCmd += "acct_status,";
			sqlCmd += "stmt_cycle,";
			sqlCmd += "pay_by_stage_flag,";
			sqlCmd += "autopay_acct_no ";
			sqlCmd += "from dba_acno  ";
			sqlCmd += "where p_seqno  = ? ";
			setString(1, hCrdPSeqno);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hAcnoAcctType = getValue("acct_type");
				hAcnoAcctKey = getValue("acct_key");
				hAcnoAcctStatus = getValue("acct_status");
				hAcnoStmtCycle = getValue("stmt_cycle");
				hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
				hAcnoAutopayAcctNo = getValue("autopay_acct_no");
			}

			hAcnoAutoInstallment = "N";

		} else {
			sqlCmd = "select acct_type,";
			sqlCmd += "acct_key,";
			sqlCmd += "acct_status,";
			sqlCmd += "stmt_cycle,";
			sqlCmd += "pay_by_stage_flag,";
			sqlCmd += "auto_installment,";
			sqlCmd += "autopay_acct_no ";
			sqlCmd += "from act_acno  ";
			sqlCmd += "where acno_p_seqno  = ? ";
			setString(1, hCrdAcnoPSeqno);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hAcnoAcctType = getValue("acct_type");
				hAcnoAcctKey = getValue("acct_key");
				hAcnoAcctStatus = getValue("acct_status");
				hAcnoStmtCycle = getValue("stmt_cycle");
				hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
				hAcnoAutoInstallment = getValue("auto_installment");
				hAcnoAutopayAcctNo = getValue("autopay_acct_no");
			}
		}
	}
    
    /***********************************************************************/
    private String sendCcas(String input_str) throws Exception {
        Socket socket = null;
        String rtn = "";

        /* 20190724 add by brain */
        sqlCmd = "select wf_value, wf_value2, wf_value3, wf_value4 from ptr_sys_parm"
                + " where wf_parm ='SYSPARM' and wf_key='CCASLINK'";
        if (selectTable() <= 0) {
            showLogMessage("I", "", "select ptr_sys_parm error  ");
            return "";
        }
        String CCAS_SVRNAME = getValue("wf_value");
        int port = comc.str2int(getValue("wf_value2"));

        try {
            String host = InetAddress.getByName(CCAS_SVRNAME).getHostAddress();
            socket = new Socket(host, port);
            DataInputStream input = null;

            try {
                byte[] sndbuf = input_str.getBytes("MS950");
                int rc = sndbuf.length;
                byte[] outData = new byte[rc + 2];
                if (outData.length > 0)
                    outData[0] = (byte) (rc / 256);
                if (outData.length > 1)
                    outData[1] = (byte) (rc & 0xff);
                System.arraycopy(sndbuf, 0, outData, 2, sndbuf.length);

                comc.sendSocket(socket, outData);
                
                if (debug) {
                	showLogMessage("I", "", "Debug - SEND CCAS DATA : " + input_str);
                }
                input = new DataInputStream(socket.getInputStream());
                int inputLen = 0;
                byte[] inData = new byte[2048];

                /* 從 SOCKET 讀取交易資料 */
                byte[] lenData = new byte[2];
                int headLen = input.read(lenData, 0, 2);
                int packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

                inputLen = input.read(inData, 0, packetLen);
                if (inputLen > 0) {
                    rtn = new String(inData, 0, inputLen);
                    
                    if (debug) {
                    	showLogMessage("I", "", "Debug - RECEIVE CCAS DATA : " + rtn);
                    }
                }
                inData = null;
            } catch (IOException e) {
                return "";
            } finally {
                if (input != null) {
                    input.close();
                    input = null;
                }
                
                if (debug) {
                	showLogMessage("I", "", "Debug - Terminated");
                }
            }
        } catch (IOException e) {
            showLogMessage("I", "", "Exception : " + e.getMessage());
            showLogMessage("I", "", comc.getStackTraceString(e));
            return "";
        } finally {

            if (socket != null)
                socket.close();

            if (debug) {
            	showLogMessage("I", "", "Debug - Socked Closed");
            }
        }

        return rtn;
    }
    
    /***********************************************************************/
    void insertBilCurpost(String authCode) throws Exception {

        hPostTotalCnt++;
        hPostTotalAmt += getValueDouble("chkboe.dest_amt");
        
        selectPtrBilltype(hBillType, getValue("chkboe.txn_code"));
        selectPtrActcode();

        setValue("reference_no", getValue("chkboe.reference_no"));
        setValue("bill_type", hBityBillType);
        setValue("txn_code", hBityTxnCode);
        setValue("card_no", getValue("chkboe.card_no"));
        setValue("film_no", "");
        setValue("acq_member_id", "");
        setValue("purchase_date", getValue("chkboe.purchase_date"));
        setValueDouble("dest_amt", getValueDouble("chkboe.dest_amt"));
        setValueDouble("dc_amount", getValueDouble("chkboe.dest_amt"));
        setValue("dest_curr", getValue("chkboe.dest_curr"));
        setValueDouble("source_amt", getValueDouble("chkboe.dest_amt"));
        setValue("source_curr", getValue("chkboe.dest_curr"));
        setValue("mcht_eng_name", "");
        setValue("mcht_city", "");
        setValue("mcht_country", "TW");
        setValue("mcht_category", getValue("chkboe.mcht_category"));
        setValue("mcht_zip", "");
        setValue("mcht_state", "");
        setValueDouble("settl_amt", 0.0);
        setValue("auth_code", authCode);
        setValue("pos_entry_mode", "00");
        setValue("process_date", hBusinessDate);
        setValue("mcht_no", "");
        setValue("mcht_chi_name", getValue("chkboe.mcht_chi_name"));
        setValue("acquire_date", hBusinessDate);
        setValue("contract_no", "");
        setValueInt("term", 0);
        setValueInt("total_term", 0);
        setValue("batch_no", hPostBatchNo);
        setValue("sign_flag", hBitySignFlag);
        setValue("acct_code", hBityAcctCode);
        setValue("acct_item", hBityAcctItem);
        setValue("acct_eng_short_name", hPtrActcodeEngShortName);
        setValue("acct_chi_short_name", hPtrActcodeChiShortName);
        setValue("item_order_normal", hPtrActcodeItemOrderNormal);
        setValue("item_order_back_date", hPtrActcodeItemOrderBackDate);
        setValue("item_order_refund", hPtrActcodeItemOrderRefund);
        setValue("acexter_desc", hBityExterDesc);
        setValue("entry_acct", hBityEntryAcct);
        setValue("item_class_normal", hPtrActcodeItemClassNormal);
        setValue("item_class_back_date", hPtrActcodeItemClassBackDate);
        setValue("item_class_refund", hPtrActcodeItemClassRefund);
        setValue("collection_mode", hBityCollectionMode);
        setValue("fees_state", hBityFeesState);
        setValue("cash_adv_state", hBityCashAdvState);
        setValue("this_close_date", hBusinessDate);
        setValue("manual_upd_flag", "");
        setValue("valid_flag", "");
        setValue("doubt_type", "");
        setValue("duplicated_flag", "");
        setValue("rsk_type", "");
        setValue("acct_type", hAcnoAcctType);
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("major_card_no", hCrdMajorCardNo);
        setValue("promote_dept", hCrdPromoteDept);
        setValue("issue_date", hCrdIssueDate);
        setValue("prod_no", hCrdProdNo);
        setValue("group_code", hCrdGroupCode);
        setValue("bin_type", hPtrBintableBinType);
        setValue("acno_p_seqno", hCrdAcnoPSeqno);
        setValue("p_seqno", hCrdPSeqno);
        setValue("major_id_p_seqno", hCrdMajorIdPSeqno);
        setValue("id_p_seqno", hCrdIdPSeqno);
        setValue("reference_no_original", "");
        setValue("fees_reference_no", "");
        setValue("tx_convt_flag", "Y");
        setValue("acctitem_convt_flag", "Y");
        setValue("format_chk_ok_flag", hBityFormatChk);
        setValue("double_chk_ok_flag", hBityDoubleChk);
        setValue("err_chk_ok_flag", hBityChkErrBill);
        setValue("source_code", hCrdSourceCode);
        setValue("payment_type", "");
        setValueDouble("curr_tx_amount", 0.0);
        setValueDouble("install_tot_term", 0.0);
        setValueDouble("install_first_amt", 0.0);
        setValueDouble("install_per_amt", 0.0);
        setValueDouble("install_fee", 0.0);
        setValueLong("deduct_bp", 0);
        setValueDouble("cash_pay_amt", getValueDouble("chkboe.dest_amt"));
        
        if ("".equals(getValue("chkboe.expire_yymm")) ) {
        	setValue("expir_date", getValue("chkboe.expire_mmyy"));  //請款檔帶進來的資料
        } else {
        	setValue("expir_date", getValue("chkboe.expire_yymm"));  //請款檔帶進來的資料
        }
        
        setValueDouble("issue_fee", 0.0);
        setValueDouble("include_fee_amt", 0.0);
        setValue("reference_no_fee_f", "");
        setValue("curr_post_flag", "N");
        setValue("v_card_no", "");
        setValue("curr_code", "901");
        setValue("mod_user", "");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValueLong("mod_seqno", 0);
        
        //公用事業才需要特別處理
        String platformKind = "";
        String custMchtNo = "";
        if("CHUP".equals(hBillType)) {
        	platformKind = comc.getSubString(getValue("chkboe.extra_info"),0,2);
        	custMchtNo = "006"+platformKind+"00001";
        }
        
        setValue("ecs_platform_kind", platformKind);
        setValue("ecs_cus_mcht_no", custMchtNo);
        
        //需要收取公用事業代繳手續費用的交易將fee_state改為'Y'
      	if ("CHUP".equals(hBillType) 
      			&& ("3700".equals(hCrdGroupCode) || "3720".equals(hCrdGroupCode)) 
      			&& getValueDouble("chkboe.dest_amt") >= 30000 ) {    
      		setValue("FEES_STATE", "Y");  
      	} 
        
        daoTable = "bil_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertBilCurpost duplicate", "", "reference_no:[" + getValue("reference_no") + "]");
        }

        /*自行帳單應該不需要新增Bil_NCCC300_DTL及Bil_NCCC300
        
        // **insert bil_nccc300_dtl
        setValue("reference_no", getValue("chkboe.reference_no"));
        setValue("card_no", getValue("chkboe.card_no"));
        setValue("tmp_request_flag", "");
        setValue("usage_code", "");
        setValue("reason_code", "");
        setValue("settlement_flag", "");
        setValueDouble("SETTLEMENT_AMT", 0.0);
        setValue("tmp_service_code", "");
        setValue("pos_term_capability", "");
        setValueLong("pos_pin_capability", 0);
        setValue("pos_entry_mode", "");
        setValue("reimbursement_attr", "");
        setValue("ec_ind", "");
        setValue("second_conversion_date", "");
        setValue("electronic_term_ind", "");
        setValue("transaction_source", "");
        setValue("original_no", "");
        setValue("batch_no", h_curp_batch_no);
        setValue("exchange_rate", "");
        setValue("exchange_date", "");
        setValue("query_type", hPtrActcodeQueryType);
        setValue("floor_limit", "");
        setValue("chip_condition_code", "");
        setValue("auth_response_code", "");
        setValue("transaction_type", "");
        setValue("terminal_ver_results", "");
        setValue("iad_result", "");
        setValue("card_seq_num", "");
        setValue("unpredic_num", "");
        setValue("app_tran_count", "");
        setValue("app_int_pro", "");
        setValue("cryptogram", "");
        setValue("der_key_index", "");
        setValue("cry_ver_num", "");
        setValue("data_auth_code", "");
        setValue("cry_info_data", "");
        setValue("terminal_cap_pro", "");
        setValue("life_cyc_sup_ind", "");
        setValue("banknet_date", "");
        setValue("inter_rate_des", "");
        setValue("transaction_amt_char", "");
        setValue("dac", "");
        setValue("service_code", "");
        setValue("amt_mccr", "");
        setValue("amt_iccr", "");
        setValueDouble("amt_mccr_num", 0.0);
        setValueDouble("amt_iccr_num", 0.0);
        setValueDouble("issue_fee", 0.0);
        setValueDouble("include_fee_amt", 0.0);
        setValue("ucaf", "");
        setValue("issue_s_r", "");
        setValueDouble("acce_fee", 0.0);
        setValueDouble("acce_fee_in_bc", 0.0);
        setValue("add_acct_type", "");
        setValue("add_amt_type", "");
        setValue("add_curcy_code", "");
        setValue("add_amt_sign", "");
        setValueDouble("add_amt", 0.0);
        setValue("v_card_no", "");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "bil_nccc300_dtl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_nccc300_dtl duplicate in insert_bill_curpost()", "", getValue("reference_no"));
        }

        insert_bil_nccc300();
        
        */
        
        
    }
    
    /**
     * @throws Exception
     *********************************************************************/
    void selectPtrBilltype(String t_bill_type, String t_tx_code) throws Exception {

        initPtrBilltype();

        sqlCmd = "select bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "sign_flag,";
        sqlCmd += "acct_code,";
        sqlCmd += "acct_item,";
        sqlCmd += "fees_state,";
        sqlCmd += "fees_fix_amt,";
        sqlCmd += "fees_percent,";
        sqlCmd += "fees_min,";
        sqlCmd += "fees_max,";
        sqlCmd += "fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "inter_desc,";
        sqlCmd += "exter_desc,";
        sqlCmd += "interest_mode,";
        sqlCmd += "adv_wkday,";
        sqlCmd += "balance_state,";
        sqlCmd += "cash_adv_state,";
        sqlCmd += "entry_acct,";
        sqlCmd += "chk_err_bill,";
        sqlCmd += "double_chk,";
        sqlCmd += "format_chk,";
        sqlCmd += "merch_fee ";
        sqlCmd += "from ptr_billtype  ";
        sqlCmd += "where bill_type = ?  ";
        sqlCmd += "and txn_code = ? ";
        setString(1, t_bill_type);
        setString(2, t_tx_code);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("selectPtrBilltype not found", "", "bill_type:["+t_bill_type+"],txn_code:["+t_tx_code+"]");
        }
        if (recordCnt > 0) {
        	hBityBillType = getValue("bill_type");
        	hBityTxnCode = getValue("txn_code");
        	hBitySignFlag = getValue("sign_flag");
        	hBityAcctCode = getValue("acct_code");
        	hBityAcctItem = getValue("acct_item");
        	hBityFeesState = getValue("fees_state");
        	hBityFeesFixAmt = getValueLong("fees_fix_amt");
        	hBityFeesPercent = getValueLong("fees_percent");
        	hBityFeesMin = getValueLong("fees_min");
        	hBityFeesMax = getValueLong("fees_max");
        	hBityFeesBillType = getValue("fees_bill_type");
        	hBityFeesTxnCode = getValue("fees_txn_code");
        	hBityInterDesc = getValue("inter_desc");
        	hBityExterDesc = getValue("exter_desc");
        	hBityInterestMode = getValue("interest_mode");
        	hBityAdvWkday = getValue("adv_wkday");
        	hBityBalanceState = getValue("balance_state");
        	hBityCashAdvState = getValue("cash_adv_state");
        	hBityEntryAcct = getValue("entry_acct");
        	hBityChkErrBill = getValue("chk_err_bill");
        	hBityDoubleChk = getValue("double_chk");
        	hBityFormatChk = getValue("format_chk");
        	hBityMerchFee = getValueLong("merch_fee");
        }
        if (hPtrBintableDebitFlag.equals("Y")) {
        	hBityFeesState = "N";
        	hBityCashAdvState = "N";
        }
    }

    /***********************************************************************/
    void initPtrBilltype() {
        hBityBillType = "";
        hBityTxnCode = "";
        hBitySignFlag = "";
        hBityAcctCode = "";
        hBityAcctItem = "";
        hBityFeesState = "";
        hBityFeesFixAmt = 0;
        hBityFeesPercent = 0;
        hBityFeesMin = 0;
        hBityFeesMax = 0;
        hBityFeesBillType = "";
        hBityFeesTxnCode = "";
        hBityInterDesc = "";
        hBityExterDesc = "";
        hBityInterestMode = "";
        hBityAdvWkday = "";
        hBityBalanceState = "";
        hBityCollectionMode = "";
        hBityChargeBackMonth = "";
        hBityCashAdvState = "";
        hBityEntryAcct = "";
        hBityChkErrBill = "";
        hBityDoubleChk = "";
        hBityFormatChk = "";
        hBityMerchFee = 0;
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        initPtrActcode();
        sqlCmd = "select acct_code,";
        sqlCmd += "chi_short_name,";
        sqlCmd += "chi_long_name,";
        sqlCmd += "eng_short_name,";
        sqlCmd += "eng_long_name,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "inter_rate_code,";
        sqlCmd += "part_rev,";
        sqlCmd += "revolve,";
        sqlCmd += "acct_method,";
        sqlCmd += "interest_method,";
        sqlCmd += "urge_1st,";
        sqlCmd += "urge_2st,";
        sqlCmd += "urge_3st,";
        sqlCmd += "occupy,";
        sqlCmd += "receivables,";
        sqlCmd += "query_type ";
        sqlCmd += "from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setString(1, hBityAcctCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("selectPtrActcode not found", "", "acct_code:[" + hBityAcctCode+"]");
        }
        if (recordCnt > 0) {
        	hPtrActcodeAcctCode = getValue("acct_code");
        	hPtrActcodeChiShortName = getValue("chi_short_name");
        	hPtrActcodeChiLongName = getValue("chi_long_name");
        	hPtrActcodeEngShortName = getValue("eng_short_name");
        	hPtrActcodeEngLongName = getValue("eng_long_name");
        	hPtrActcodeItemOrderNormal = getValue("item_order_normal");
        	hPtrActcodeItemOrderBackDate = getValue("item_order_back_date");
        	hPtrActcodeItemOrderRefund = getValue("item_order_refund");
        	hPtrActcodeItemClassNormal = getValue("item_class_normal");
        	hPtrActcodeItemClassBackDate = getValue("item_class_back_date");
        	hPtrActcodeItemClassRefund = getValue("item_class_refund");
        	hPtrActcodeInterRateCode = getValue("inter_rate_code");
        	hPtrActcodePartRev = getValue("part_rev");
        	hPtrActcodeRevolve = getValue("revolve");
        	hPtrActcodeAcctMethod = getValue("acct_method");
        	hPtrActcodeInterestMethod = getValue("interest_method");
        	hPtrActcodeUrge1st = getValue("urge_1st");
        	hPtrActcodeUrge2st = getValue("urge_2st");
        	hPtrActcodeUrge3st = getValue("urge_3st");
        	hPtrActcodeOccupy = getValue("occupy");
        	hPtrActcodeReceivables = getValue("receivables");
        	hPtrActcodeQueryType = getValue("query_type");
        }
    }

    /***********************************************************************/
    void initPtrActcode() {
        hPtrActcodeQueryType = "";
        hPtrActcodeAcctCode = "";
        hPtrActcodeChiShortName = "";
        hPtrActcodeChiLongName = "";
        hPtrActcodeEngShortName = "";
        hPtrActcodeEngLongName = "";
        hPtrActcodeItemClassNormal = "";
        hPtrActcodeItemClassBackDate = "";
        hPtrActcodeItemClassRefund = "";
        hPtrActcodeItemOrderNormal = "";
        hPtrActcodeItemOrderBackDate = "";
        hPtrActcodeItemOrderRefund = "";
        hPtrActcodeInterRateCode = "";
        hPtrActcodePartRev = "";
        hPtrActcodeRevolve = "";
        hPtrActcodeAcctMethod = "";
        hPtrActcodeInterestMethod = "";
        hPtrActcodeUrge1st = "";
        hPtrActcodeUrge2st = "";
        hPtrActcodeUrge3st = "";
        hPtrActcodeOccupy = "";
        hPtrActcodeReceivables = "";
        hPtrActcodeFileDate = "";
        hPtrActcodeFileTime = "";
    }

    /***********************************************************************/
    void insert_dbb_curpost() throws Exception {

        setValue("reference_no", getValue("chkboe.reference_no"));
        setValue("bill_type", hBityBillType);
        setValue("txn_code", hBityTxnCode);
        setValue("card_no", getValue("chkboe.card_no"));
        setValue("film_no", "");
        setValue("acq_member_id", "");
        setValue("purchase_date", getValue("chkboe.purchase_date"));
        setValueDouble("dest_amt", getValueDouble("chkboe.dest_amt"));
        setValue("dest_curr", getValue("chkboe.dest_curr"));
        setValueDouble("source_amt", getValueDouble("chkboe.dest_amt"));
        setValue("source_curr", getValue("chkboe.dest_curr"));
        setValue("mcht_eng_name", "");
        setValue("mcht_city", "");
        setValue("mcht_country", "TWN");
        setValue("mcht_category", getValue("chkboe.mcht_category"));
        setValue("mcht_zip", "");
        setValue("mcht_state", "");
        setValueDouble("settl_amt", 0.0);
        setValue("auth_code", getValue("chkboe.auth_code"));
        setValue("pos_entry_mode", "00");
        setValue("process_date", hBusinessDate);
        setValue("mcht_no", "");
        setValue("mcht_chi_name", String.format("%s%12s", getValue("chkboe.mcht_chi_name"),getValue("chkboe.extra_info")));
        setValue("acquire_date", hBusinessDate);
        setValue("contract_no", "");
        setValueInt("term", 0);
        setValueInt("total_term", 0);
        setValue("batch_no", hPostBatchNo);
        setValue("sign_flag", hBitySignFlag);
        setValue("acct_code", hBityAcctCode);
        setValue("acct_item", hBityAcctItem);
        setValue("acct_eng_short_name", hPtrActcodeEngShortName);
        setValue("acct_chi_short_name", hPtrActcodeChiShortName);
        setValue("item_order_normal", hPtrActcodeItemOrderNormal);
        setValue("item_order_back_date", hPtrActcodeItemOrderBackDate);
        setValue("item_order_refund", hPtrActcodeItemOrderRefund);
        setValue("acexter_desc", hBityExterDesc);
        setValue("entry_acct", hBityEntryAcct);
        setValue("item_class_normal", hPtrActcodeItemClassNormal);
        setValue("item_class_back_date", hPtrActcodeItemClassBackDate);
        setValue("item_class_refund", hPtrActcodeItemClassRefund);
        setValue("collection_mode", hBityCollectionMode);
        setValue("fees_state", hBityFeesState);
        setValue("cash_adv_state", hBityCashAdvState);
        setValue("this_close_date", hBusinessDate);
        setValue("manual_upd_flag", "");
        setValue("valid_flag", "");
        setValue("doubt_type", "");
        setValue("duplicated_flag", "");
        setValue("rsk_type", "");
        setValue("acct_type", hAcnoAcctType);
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("major_card_no", hCrdMajorCardNo);
        setValue("promote_dept", hCrdPromoteDept);
        setValue("issue_date", hCrdIssueDate);
        setValue("prod_no", hCrdProdNo);
        setValue("bin_type", hPtrBintableBinType);
        setValue("group_code", hCrdGroupCode);
        setValue("p_seqno", hCrdPSeqno);
        setValue("id_p_seqno", hCrdIdPSeqno);
        setValue("major_id_p_seqno", hCrdMajorIdPSeqno);
        setValue("tx_convt_flag", "Y");
        setValue("acctitem_convt_flag", "Y");
        setValue("format_chk_ok_flag", hBityFormatChk);
        setValue("double_chk_ok_flag", hBityDoubleChk);
        setValue("err_chk_ok_flag", hBityChkErrBill);
        setValue("source_code", hCrdSourceCode);
        setValue("payment_type", "");
        setValueDouble("curr_tx_amount", 0.0);
        setValueDouble("install_tot_term", 0.0);
        setValueDouble("install_first_amt", 0.0);
        setValueDouble("install_per_amt", 0.0);
        setValueDouble("install_fee", 0.0);
        setValueLong("deduct_bp", 0);
        setValueDouble("cash_pay_amt", 0.0);
        setValue("expir_date", getValue("chkboe.expire_yymm"));  //請款檔帶進來的資料
        setValue("reference_no_fee_f", "");
        setValue("curr_post_flag", "");
        setValue("mod_user", "batch");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValueLong("mod_seqno", 0);
        daoTable = "dbb_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertDbbCurpost duplicate", "", "reference_no:[" + getValue("reference_no") + "]");
        }
    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {

        sqlCmd = "select conf_flag ";
        sqlCmd += "from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, hBillType);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPtrBillunitConfFlag = getValue("conf_flag");
        }

        sqlCmd = "select substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4) as hBatchSeq ";
        sqlCmd += "from bil_postcntl  ";
        sqlCmd += "where batch_unit = substr(?,1,2)  ";
        sqlCmd += "and batch_date = ? ";
        setString(1, hBillType);
        setString(2, hBusinessDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBatchSeq = getValue("hBatchSeq");
        }
        insertBillPostcntl();
    }

    /***********************************************************************/
    void insertBillPostcntl() throws Exception {
        hPostBatchSeq = comcr.str2int(hBatchSeq);
        hPostBatchDate = hBusinessDate;
        hPostBatchUnit = hBillUnit;

        hPostBatchNo = String.format("%8s%2s%4s", hPostBatchDate, hPostBatchUnit, hBatchSeq);
        hPostConfirmFlag = hPtrBillunitConfFlag;
        hPostConfirmFlagP = "N";
        if (hPostConfirmFlag.equalsIgnoreCase("Y") == false) {
        	hPostConfirmFlagP = "Y";
        }

        setValue("batch_date", hBusinessDate);
        setValue("batch_unit", hBillUnit);
        setValue("batch_seq", hBatchSeq);
        setValue("batch_no", hPostBatchNo);
        setValueDouble("tot_record", 0.0);
        setValueDouble("tot_amt", 0.0);
        setValue("confirm_flag_p", hPostConfirmFlagP);
        setValue("confirm_flag", hPostConfirmFlag);
        setValue("this_close_date", hBusinessDate);
        setValue("mod_user", "ecs");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValueLong("mod_seqno", 0);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertBilPostcntl duplicate", "", "batchNo:[" + hPostBatchNo + "]");
        }
    }
    
    private void updateBilOnusbillExchange(String statusFlag,String authCode,String blockCode) throws Exception {
    	daoTable = " bil_onusbill_exchange ";
		updateSQL = " status_flag = ?,  auth_code = ?, post_flag = ?, mod_pgm = ?,  mod_time = sysdate ";
		whereStr = " where rowid = ?  ";
		
		setString(1, statusFlag);
		setString(2, authCode);
		setString(3, blockCode);
		setString(4, javaProgram);
		setRowId(5, getValue("chkboe.rowid"));

		int returnInt = updateTable();
		if (returnInt == 0) {
			showLogMessage("E","","Fail to update BilOnusbillExchange.statusFlag, rowid:["+getValue("chkboe.rowid")+"]");
		}
    }
    
    void selectPtrBintable(String cardNo) throws Exception 
    {
    	if(debug) {
    		showLogMessage("I","","Debug---selectPtrBintable: cardNo =" + cardNo); //debug
    	}

    	hPtrBintableCurrencyCode = "901";
        hPtrBintableBinType = "";
        hPtrBintableDebitFlag = "N";

        extendField = "pbtb.";
        sqlCmd = "select ";
        sqlCmd += "bin_type,";
        sqlCmd += "decode(debit_flag,'','N',debit_flag) debit_flag ";
        sqlCmd += "  from ptr_bintable c ";
        sqlCmd += " where 1=1 ";
        sqlCmd += "   and c.bin_no || c.bin_no_2_fm || '0000' <= ?  ";
        sqlCmd += "   and c.bin_no || c.bin_no_2_to || '9999' >= ?  ";
        setString(1, cardNo);
        setString(2, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hPtrBintableCurrencyCode = "901";
        	hPtrBintableBinType = getValue("pbtb.bin_type");
        	hPtrBintableDebitFlag  = getValue("pbtb.debit_flag");
        } 
    }
   
    /**
     * @throws Exception *********************************************************************/
	public String callAuth() throws Exception {
		sqlCmd = "select wf_value,wf_value2 from ptr_sys_parm where wf_parm='SYSPARM' and WF_KEY='CCASLINK'";
		int recordCnt = selectTable();
		String wfValue = getValue("wf_value");
		String wfValue2 = getValue("wf_value2");
		if (recordCnt <= 0 && wfValue.trim().length() <= 0) {
			comcr.errRtn("無法取得IP位置", "", "WF_KEY=CCASLINK");
		}
		AuthGateway authGatewayTest = new AuthGateway();
		AuthData lAuthData = new AuthData();
		lAuthData.setCardNo(getValue("chkboe.card_no"));
		lAuthData.setCvv2("");  //testing 
		lAuthData.setExpireDate(formatDate(getValue("chkboe.expire_yymm"),getValue("chkboe.expire_mmyy"))); /** YYYYMMDD */
		lAuthData.setLocalTime(sysDate + sysTime);
		lAuthData.setMccCode(getValue("chkboe.mcht_category")); /* bit18 mcc code */
		lAuthData.setMchtNo("");/* bit42 acceptor_id=mcht_no */
		lAuthData.setOrgAuthNo("");
		lAuthData.setOrgRefNo("");
		if (debug) {
			showLogMessage("I","","dest_amt : ["+ String.format("%s",getValueLong("chkboe.dest_amt")) +"]");
		}
			
		lAuthData.setTransAmt(String.format("%s",getValueLong("chkboe.dest_amt")));
		String ss = getValue("chkboe.txn_code");
		String[] cde = new String[] { "05", "06", "25" };
		String[] txt = new String[] { "1", "2", "2" };
		lAuthData.setTransType(decode(ss, cde, txt));/* 1: regular 2:refund 3:reversal 4:代行 */
		lAuthData.setTypeFlag("B");/// * A: install B: mail*/
		String sLTranxResult = authGatewayTest.startProcess(lAuthData, wfValue, wfValue2);

		return sLTranxResult;
	}

	String decode(String s1, String[] id1, String[] txt1) {
		if (s1 == null || s1.trim().length() == 0)
			return "";

		int ii = Arrays.asList(id1).indexOf(s1.trim());
		if (ii >= 0 && ii < txt1.length) {
			return txt1[ii];
		}

		return s1;
	}
	
	String formatDate(String expireYYMM , String expireMMYY) throws Exception{
		String expireDate ="";
		if(!expireYYMM.isEmpty()){
			String yy1 = comcr.mid(sysDate,1,2);
			expireDate = yy1+expireYYMM+"01";
		} else if(!expireMMYY.isEmpty()){
			String yy1 = comcr.mid(sysDate,1,2);
			String yy2 = comcr.mid(expireMMYY,3,2);
			String mm = comcr.mid(expireMMYY,1,2);
			expireDate = yy1+yy2+mm+"01";
		}
		if(expireDate.length()<8)
			return "";
		
	    String sdate = comm.lastdateOfmonth(expireDate);
		return sdate;

	}
    /***********************************************************************/
    void initCrdCard() {
        hCrdCardNo = "";
        hCrdPSeqno = "";
        hCrdCardId = "";
        hCrdCardIdCode = "";
        hCrdCorpNo = "";
        hCrdCardType = "";
        hCrdCardUrgentFlag = "";
        hCrdGroupCode = "";
        hCrdSourceCode = "";
        hCrdSupFlag = "";
        hCrdMemberId = "";
        hCrdCurrentCode = "";
        hCrdEngName = "";
        hCrdRegBankNo = "";
        hCrdUnitCode = "";
        hCrdOldBegDate = "";
        hCrdOldEndDate = "";
        hCrdNewBegDate = "";
        hCrdNewEndDate = "";
        hCrdIssueDate = "";
        hCrdChangeDate = "";
        hCrdMajorCardNo = "";
        hCrdApplyNo = "";
        hCrdPromoteDept = "";
        hCrdMajorRelation = "";
        hCrdOppostReason = "";
        hCrdOppostDate = "";
        hCrdNewCardNo = "";
        hCrdOldCardNo = "";
        hCrdAcctType = "";
        hCrdPromoteEmpNo = "";
        hCrdIntroduceId = "";
        hCrdIntroduceName = "";
        hCrdProdNo = "";
        hCrdFeeCode = "";
        hCrdExpireChgFlag = "";
        hCrdActivateFlag = "";
        hCrdActivateDate = "";
        hCrdSonCardFlag = "";
        hCrdSetCode = "";
        hCrdMailType = "";
        hCrdMailNo = "";
        hCrdCreateDate = "";
        hCrdCreateId = "";
    }

}

class BilE200InData {
	String recType = "";
	String branch = "";
	String idNo = "";
	String cardNo = "";
	String expireYYMM = "";
	String expireMMYY = "";
	String tranCode = "";
	String destinationAmount = "0";
	String destinationCurrency = "";
	String extraInfo = "";
	String statusFlag = "";
	String purchaseDate = "";
	String authorizeCode = "";
	String rowData = "";
	String mchtCategory = "";
	String mchtChiName = "";
	String txnCode = "";
	String authCode = "";
}