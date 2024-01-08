/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/12/19  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  108/12/04  V1.00.02    phopho     from BRD: change args                    *
*  109/02/12  V1.00.03    phopho     Mantis 0002594: 列印表日設定為系統日期.   *
*  109/12/11  V1.00.04    shiyuqi       updated for project coding standard   *
*  111/11/07  V1.00.05    sunny      同步版本至0520                                                             *  
*  112/06/26  V1.00.06    Ryan       參數調整                                                                      *  
*  112/06/29  V1.00.07    sunny      調整報表顯示的位置，排齊，並修正顯示錯誤問題                *
*  112/08/21  V1.00.08    Ryan       報表Bug修正,參數調整                                            *
*  112/08/22  V1.00.09    sunny      參數調整(D)， 調整報表數字顯示的位置                       *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

public class ColB003 extends AccessDAO {
    private String progname = "轉列催收款日報表、月報表處理程式  112/08/22  V1.00.09 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommString     comStr     = new CommString();

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "COLB003R1";
    String rptDesc1 = "";
    //String rptDesc = "轉列催收款報告表";
    String rptDesc = "轉列催收款";
    int    rptSeq1  = 0;
    String buf                = "";
    String szTmp              = "";
    String hCallBatchSeqno = "";

    String hPaccCurrCode = "";
    String hPcceCurrChiName = "";
    int hTempCount = 0;
    String hPaccAcctType = "";
    String hPaccChinName = "";
    int hTempType = 0;
    String hTempSDate = "";
    String hTempEDate = "";
    String hTempIdNo = "";
    String hTempIdPSeqno = "";
    String hCbdtPSeqno = "";
    String hCbdtAcctType = "";
    String hCbdtTransDate = "";
    String hCbdtStmtCycle = "";
    String hCbdtIdPSeqno = "";
    String hCbdtId = "";
    String hCbdtIdCode = "";
    String hCbdtCorpPSeqno = "";
    String hCbdtCorpNo = "";
    double hCbdtLineOfCreditAmt = 0;
    String hCbdtTranSource = "";
    String hCbdtCreditActNo = "";
    double hTempCbEndBal = 0;
    double hTempCiEndBal = 0;
    double hTempCcEndBal = 0;
    double hTempCiEndBalPn=0;
    double hTempCiEndBalRi=0;
    String hIdnoChiName = "";
    String hCorpChiName = "";
    String hAcnoAcctType = "";
    String hPrintName = "";
    String hRptName = "";
    String dispDate1 = "";
    String sId = "";
    String sName = "";
    String tmpAcctType = "";
    String temstr                    = "";
    String hBusinessDate = "";

    int pageCnt = 0, lineCnt = 0, totalCnt = 0, ttotalCnt = 0, indexCnt = 0, pageLine = 0, page1Cnt = 0;
    double pageAmt = 0, page1Amt = 0, totalAmt = 0, ttotalAmt = 0;
    double pageCbAmt = 0, page1CbAmt = 0, totalCbAmt = 0, ttotalCbAmt = 0;
    double pageCiAmt = 0, page1CiAmt = 0, totalCiAmt = 0, ttotalCiAmt = 0;
    double pageCiAmtPn = 0, pageCiAmtRi = 0, page1CiAmtPn = 0, page1CiAmtRi = 0;
    double totalCiAmtPn = 0,totalCiAmtRi = 0;
    double pageCcAmt = 0, page1CcAmt = 0, totalCcAmt = 0, ttotalCcAmt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            
            if(args.length>=1 && args[0].length()>1 && ",D,M,Y,C".indexOf(args[0]) < 0) {
            	comc.errExit("", "第一個參數必需為 D or M or Y or C");
            }

            if (args.length>=1 && "C".equals(args[0]) && args.length < 3) {
//                comc.err_exit("Usage : ColB003 s_date e_date [seqno]", "");  //依BRD需求修改argment
                showLogMessage("I", "", String.format("Usage : ColB003 C s_date e_date [id_no]"));
                showLogMessage("I", "", String.format("               1.s_date : 轉催收起日(yyyymmdd)"));
                showLogMessage("I", "", String.format("               2.e_date : 轉催收迄日(yyyymmdd)"));
                showLogMessage("I", "", String.format("               3.id_no  : 身份證字號"));
                comc.errExit("", "");
            }                     

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            //online call batch 時須記錄
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            hBusinessDate = getBusiDate();
            hTempSDate = hBusinessDate;
    		hTempEDate = hBusinessDate;
    		String parm1 = "";
            if(args.length >= 1) {
            	 parm1 = args[0];
            	 rptName1=rptName1+"_"+parm1; //報表名稱
            	 switch(parm1) {            	
            	 case "D" : 
            		    rptDesc = rptDesc + "日報表";
						if (args.length > 1 && args[1].length() == 8) {
							if (!new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
								comc.errExit("", String.format("轉催收起日(yyyymmdd) ,日期格式輸入有誤[%s]", args[1]));
							}
							String sGArgs0 = "";
							sGArgs0 = args[1];
							sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
							hTempSDate = sGArgs0;
							hTempEDate = hTempSDate;
						}
						break;
            	 case "M" : 
            		       rptDesc = rptDesc + "月報表";
            		       if (args.length > 1 && args[1].length() == 8) {
							if (!new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
								comc.errExit("", String.format("轉催收起日(yyyymmdd) ,日期格式輸入有誤[%s]", args[1]));
							}
							String sGArgs0 = "";
							sGArgs0 = args[1];
							sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
							hTempSDate = sGArgs0;
						}

						hTempSDate = comStr.left(hTempSDate, 6) + "01";
						hTempEDate = comStr.left(hTempSDate, 6) + "31";
						break;
            	 case "Y" :
            		    rptDesc = rptDesc + "年報表";
						if (args.length > 1 && args[1].length() == 8) {
							if (!new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
								comc.errExit("", String.format("轉催收起日(yyyymmdd) ,日期格式輸入有誤[%s]", args[1]));
							}
							String sGArgs0 = "";
							sGArgs0 = args[1];
							sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
							hTempSDate = sGArgs0;
						}

						hTempSDate = comStr.left(hTempSDate, 4) + "0101";
						hTempEDate = comStr.left(hTempSDate, 4) + "1231";
						break;
            	 case "C" :
            		 rptDesc = rptDesc + "指定區間報表";
            		 if(args[1].length()<8 || ! new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
            			 comc.errExit("", String.format("轉催收起日(yyyymmdd) ,日期格式輸入有誤[%s]", args[1]));
            		 }
            		 
            		 if(args[2].length()<8 || ! new CommFunction().checkDateFormat(args[2], "yyyyMMdd")) {
            			 comc.errExit("", String.format("轉催收迄日(yyyymmdd) ,日期格式輸入有誤[%s]", args[2]));
            		 }
            		 
                     if (args[1].length() == 8) {
                         String sGArgs0 = "";
                         sGArgs0 = args[1];
                         sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                         hTempSDate = sGArgs0;
                     }
                     if (args[2].length() == 8) {
                         String sGArgs1 = "";
                         sGArgs1 = args[2];
                         sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                         hTempEDate = sGArgs1;
                     }
                     if ((args.length > 3) && (args[3].length() == 10)) {
                     	String sGArgs2 = "";
                         sGArgs2 = args[3];
                         sGArgs2 = Normalizer.normalize(sGArgs2, java.text.Normalizer.Form.NFKD);
                         hTempIdNo = sGArgs2;
                     }
            		 break;
            	 }
            }
            showLogMessage("I", "", String.format("輸入參數1 = [%s] ,轉催收起日 = [%s] ,轉催收迄日 = [%s]",parm1,hTempSDate,hTempEDate));
//            h_temp_s_date = String.format("%8d", comcr.str2long(args[0]) + 19110000);
//            h_temp_e_date = String.format("%8d", comcr.str2long(args[1]) + 19110000);

            checkOpen();
            if (hTempIdNo.trim().equals("")==false)
             	hTempIdPSeqno = selectCrdIdno1(hTempIdNo);

            selectPtrAcctType();

            comcr.insertPtrBatchRpt(lpar1);  //phopho add 問題單:0001121
            comc.writeReport(temstr, lpar1); //寫file
            
            if (args.length == 3)
                comcr.lpRtn("COL_D_VOUCH", "");

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    /***********************************************************************/
    void selectPtrAcctType() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.curr_code,";
        sqlCmd += "min(a.curr_chi_name) curr_chi_name,";
        sqlCmd += "count(*) temp_count ";
        sqlCmd += "from ptr_currcode a,ptr_acct_type b ";
        sqlCmd += "where a.curr_code = b.curr_code ";
        sqlCmd += "group by b.curr_code ";

        openCursor();
        while (fetchTable()) {
            hPaccCurrCode = getValue("curr_code");
            hPcceCurrChiName = getValue("curr_chi_name");
            hTempCount = getValueInt("temp_count");

            hTempType = 1;
            hTempCount = 0;
            selectPtrAcctType1();
            if (ttotalCnt > 1) {
                pageCnt = lineCnt = totalCnt = indexCnt = pageLine = page1Cnt = 0;
                pageAmt = page1Amt = totalAmt = 0;
                pageCbAmt = page1CbAmt = totalCbAmt = 0;
                pageCiAmt = page1CiAmt = totalCiAmt = 0;
                pageCcAmt = page1CcAmt = totalCcAmt = 0;
                hTempType = 0;
                totalCnt = ttotalCnt;
                totalAmt = ttotalAmt;
                totalCbAmt = ttotalCbAmt;
                totalCiAmt = ttotalCiAmt;
                totalCcAmt = ttotalCcAmt;
               // printHeader();
               // printTotal();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectPtrAcctType1() throws Exception {
    	sqlCmd = "select ";
        sqlCmd += "acct_type as act,";
        sqlCmd += "chin_name ";
        sqlCmd += "from ptr_acct_type ";
        sqlCmd += "where curr_code = ? ";
        sqlCmd += "order by acct_type ";
        setString(1, hPaccCurrCode);
        
        extendField = "ptr_acct_type_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hPaccAcctType = getValue("ptr_acct_type_1.act", i);
            hPaccChinName = getValue("ptr_acct_type_1.chin_name", i);

            pageCnt = lineCnt = totalCnt = indexCnt = pageLine = page1Cnt = 0;
            pageAmt = page1Amt = totalAmt = 0;
            pageCbAmt = page1CbAmt = totalCbAmt = 0;
            pageCiAmt = page1CiAmt = totalCiAmt = 0;
            pageCcAmt = page1CcAmt = totalCcAmt = 0;

            selectColBadDebt();

            ttotalCnt = ttotalCnt + totalCnt;
            ttotalAmt = ttotalAmt + totalAmt;
            ttotalCbAmt = ttotalCbAmt + totalCbAmt;
            ttotalCiAmt = ttotalCiAmt + totalCiAmt;
            ttotalCcAmt = ttotalCcAmt + totalCcAmt;

            if (totalCnt > 0) {
                printTotal();
                hTempCount++;
            }
        }
    }

    /***********************************************************************/
    void selectColBadDebt() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "trans_date,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code as id_no_code,";
        sqlCmd += "corp_p_seqno,";
        sqlCmd += "corp_no,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "decode(tran_source,'1',' ','2','*','&') tran_source,";
        sqlCmd += "credit_act_no ";
        sqlCmd += "from col_bad_debt ";
        sqlCmd += "where decode(trans_type,'','x',trans_type) = '3' ";
        sqlCmd += "and acct_type = decode(cast(? as integer),0,acct_type,cast(? as varchar(2))) ";
        sqlCmd += "and trans_date between ? and ? ";
        //phopho add
        if (hTempIdPSeqno.equals("")==false)
        	sqlCmd += "and id_p_seqno = ? ";
        //phopho add end
        sqlCmd += "order by id_no ";
        setInt(1, hTempType);
        setString(2, hPaccAcctType);
        setString(3, hTempSDate);
        setString(4, hTempEDate);
        if (hTempIdPSeqno.equals("")==false)
        	setString(5, hTempIdPSeqno);
        
        extendField = "col_bad_debt.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCbdtPSeqno = getValue("col_bad_debt.p_seqno", i);
            hCbdtAcctType = getValue("col_bad_debt.acct_type", i);
            hCbdtTransDate = getValue("col_bad_debt.trans_date", i);
            hCbdtStmtCycle = getValue("col_bad_debt.stmt_cycle", i);
            hCbdtIdPSeqno = getValue("col_bad_debt.id_p_seqno", i);
            hCbdtId = getValue("col_bad_debt.id_no", i);
            hCbdtIdCode = getValue("col_bad_debt.id_no_code", i);
            hCbdtCorpPSeqno = getValue("col_bad_debt.corp_p_seqno", i);
            hCbdtCorpNo = getValue("col_bad_debt.corp_no", i);
            hCbdtLineOfCreditAmt = getValueDouble("col_bad_debt.line_of_credit_amt", i);
            hCbdtTranSource = getValue("col_bad_debt.tran_source", i);
            hCbdtCreditActNo = getValue("col_bad_debt.credit_act_no", i);

            if (hCbdtCorpNo.length() != 0) {
                selectCrdCorp();
                sId = String.format("%s", hCbdtCorpNo);
                sName = String.format("%1.1s%s", hCbdtTranSource, hCorpChiName);
            }

            if (hCbdtId.length() != 0) {
                selectCrdIdno();
                sId = String.format("%s", hCbdtId);
                sName = String.format("%1.1s%s", hCbdtTranSource, hIdnoChiName);
            }
            selectColBadDetail();

            if (indexCnt == 0)
            {            	
            	rptDesc1= rptDesc+"("+hTempSDate+"-"+hTempEDate+")";                                
            	lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
            	printHeader();
            }
            selectActAcno();
            printDetail();
            if (indexCnt >= 28) {
                printFooter();
                indexCnt = 0;
            }
        }
        if (indexCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCorpChiName = "";

        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_corp ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hCbdtCorpPSeqno);
        
        extendField = "crd_corp.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCorpChiName = getValue("crd_corp.chi_name");
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";

        sqlCmd = "select chi_name h_idno_chi_name ";
        sqlCmd += " from crd_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCbdtIdPSeqno);
        int recordCnt = selectTable();
        
        extendField = "crd_idno.";
        
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("h_idno_chi_name");
        }
    }

    /***********************************************************************/
    void selectColBadDetail() throws Exception {
        hTempCbEndBal = 0;
        hTempCiEndBal = 0;
        hTempCcEndBal = 0;
        hTempCiEndBalPn=0;
        hTempCiEndBalRi=0;

        sqlCmd = "select sum(decode(new_acct_code,'CB',end_bal,0)) cb_end_bal,";
        sqlCmd += "sum(decode(new_acct_code,'CI',end_bal,0)) ci_end_bal,";
        sqlCmd += " NVL(SUM(case when new_acct_code='CI' and acct_code='PN' then end_bal END),0) ci_end_bal_pn, ";  /*違約金*/
        sqlCmd += " NVL(SUM(case when new_acct_code='CI' and acct_code<>'PN' then end_bal END),0) ci_end_bal_ri, "; /*利息*/
        sqlCmd += "sum(decode(new_acct_code,'CC',end_bal,0)) cc_end_bal ";        
        sqlCmd += " from col_bad_detail ";
        sqlCmd += "where trans_type = '3' ";
        sqlCmd += "and trans_date = ?  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hCbdtTransDate);
        setString(2, hCbdtPSeqno);
        
        extendField = "col_bad_detail.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCbEndBal = getValueDouble("col_bad_detail.cb_end_bal");
            hTempCiEndBal = getValueDouble("col_bad_detail.ci_end_bal");
            hTempCcEndBal = getValueDouble("col_bad_detail.cc_end_bal");
            hTempCiEndBalPn = getValueDouble("col_bad_detail.ci_end_bal_pn");
            hTempCiEndBalRi = getValueDouble("col_bad_detail.ci_end_bal_ri");
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
//        sqlCmd = "select ";
//        sqlCmd += "a.acct_type ";
//        sqlCmd += "from act_acno a, crd_idno c ";
//        sqlCmd += "where a.p_seqno != ? ";
////        sqlCmd += "and acct_holder_id = ? ";
////        sqlCmd += "and acct_holder_id_code = ? ";
//        sqlCmd += "and c.id_no = ? ";
//        sqlCmd += "and c.id_no_code = ? ";
//        sqlCmd += "and a.acct_status < '3' ";
//        setString(1, h_cbdt_p_seqno);
//        setString(2, h_cbdt_id);
//        setString(3, h_cbdt_id_code);

    	tmpAcctType = "";
    	
        sqlCmd = "select distinct ";
        sqlCmd += "acct_type ";
        sqlCmd += "from act_acno ";
//        sqlCmd += "where p_seqno != ? ";
        sqlCmd += "where acno_p_seqno != ? ";
        sqlCmd += "and id_p_seqno = ? ";
        sqlCmd += "and acct_status < '3' ";
        setString(1, hCbdtPSeqno);
        setString(2, hCbdtIdPSeqno);
        
        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            if (i != 0) tmpAcctType += ",";
            tmpAcctType += hAcnoAcctType;
        }
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(sId, 10)); //身分證號/統編
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(sName, 15));  //客戶名稱
        sb.append(comc.fixLeft(hCbdtAcctType, 2)); //產品別 
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixLeft(hCbdtTransDate, 8)); //轉催日 
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", hTempCbEndBal);    //本金
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", hTempCiEndBalPn);  //違約金
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", hTempCiEndBalRi);  //利息
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", hTempCcEndBal);    //費用
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", hTempCbEndBal+hTempCiEndBalPn+hTempCiEndBalRi+hTempCcEndBal); //催收款餘額 
        sb.append(comc.fixRight(szTmp, 11));
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        sb.append(comc.fixLeft(tmpAcctType, 14)); //尚有其他帳戶未轉催收
         
        buf = sb.toString();
       
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        
        pageCnt++;
        totalCnt++;

        if (hCbdtTranSource.trim().length() > 0) {
            page1Cnt++;
            page1CbAmt = page1CbAmt + hTempCbEndBal;
            page1CiAmt = page1CiAmt + hTempCiEndBal;
            page1CiAmtPn = page1CiAmtPn + hTempCiEndBalPn;
            page1CiAmtRi = page1CiAmtRi + hTempCiEndBalRi;
            page1CcAmt = page1CcAmt + hTempCcEndBal;
            page1Amt = page1Amt + hTempCcEndBal + hTempCiEndBal + hTempCbEndBal;
        }
        pageCbAmt = pageCbAmt + hTempCbEndBal;
        pageCiAmt = pageCiAmt + hTempCiEndBal;
        pageCiAmtPn = pageCiAmtPn + hTempCiEndBalPn;
        pageCiAmtRi = pageCiAmtRi + hTempCiEndBalRi;
        pageCcAmt = pageCcAmt + hTempCcEndBal;
        pageAmt = pageAmt + hTempCcEndBal + hTempCiEndBal + hTempCbEndBal;
        totalCbAmt = totalCbAmt + hTempCbEndBal;
        totalCiAmt = totalCiAmt + hTempCiEndBal;
        totalCiAmtPn = totalCiAmtPn + hTempCiEndBalPn;
        totalCiAmtRi = totalCiAmtRi + hTempCiEndBalRi;
        totalCcAmt = totalCcAmt + hTempCcEndBal;
        totalAmt = totalAmt + hTempCcEndBal + hTempCiEndBal + hTempCbEndBal;
    }

    /***********************************************************************/
    void printFooter() throws Exception {
    	 	
       	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
 
         
    	  StringBuffer sb = new StringBuffer();       
          sb.append(comc.fixLeft("本頁筆數:", 9));
          szTmp = String.format("%,6d", indexCnt);
          sb.append(comc.fixRight(szTmp, 7));
          sb.append(comc.fixLeft(" ", 13));  //空白分隔
          szTmp = comcr.commFormat("zz,3z,3z,3#", pageCbAmt);    //本金小計
          sb.append(comc.fixRight(szTmp, 10));
          sb.append(comc.fixLeft(" ", 1));  //空白分隔
          szTmp = comcr.commFormat("zz,3z,3z,3#", pageCiAmtPn);  //違約金小計
          sb.append(comc.fixRight(szTmp, 10));
          sb.append(comc.fixLeft(" ", 1));  //空白分隔
          szTmp = comcr.commFormat("zz,3z,3z,3#", pageCiAmtRi);  //利息小計
          sb.append(comc.fixRight(szTmp, 10));
          sb.append(comc.fixLeft(" ", 1));  //空白分隔
          szTmp = comcr.commFormat("zz,3z,3z,3#", pageCcAmt);  //費用小計
          sb.append(comc.fixRight(szTmp, 10));
          sb.append(comc.fixLeft(" ", 1));  //空白分隔
          szTmp = comcr.commFormat("zz,3z,3z,3#", pageAmt);    //催收款餘額小計 
          sb.append(comc.fixRight(szTmp, 11));
          
          buf = "";
          buf = sb.toString();
          lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));  
               
        pageCnt = 0;
        pageCbAmt = 0;
        pageCiAmt = 0;
        pageCiAmtPn = 0;
        pageCiAmtRi = 0;
        pageCcAmt = 0;
        pageAmt = 0;
    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageLine++;

        buf = comcr.insertStr(buf, "COL_B003R1", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        buf = comcr.insertStr(buf, "列印表日 :", 110);
        //disp_date = comc.convDates(sysDate, 1);
        //Mantis 0002594:
        //1. 目前現行系統（舊系統）是「執行批次日期+1天」為報表之「列印表日」。
        //2. 新系統可以執行線上批次，造成日期差一天的疑慮。
        //3. 現依據user提出，將「列印表日」修改為執行批次之日期。
        dispDate1 = sysDate;
        
        buf = comcr.insertStr(buf, dispDate1, 121);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStrCenter(buf, "轉 列 催 收 款 報 告 表", 132);
        buf = comcr.insertStr(buf, "列印頁數 :", 110);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別:", 1);
        if (hTempType != 0) {
            szTmp = String.format("%2.2s %s", hPaccAcctType, hPaccChinName);
        } else {
            szTmp = String.format("%s 合計", hPcceCurrChiName);
        }
        buf = comcr.insertStr(buf, szTmp, 10);
//        szTmp = String.format("%03.0f年%2.2s月%2.2s日 ~ %03.0f年%2.2s月%2.2s日",
//                (comcr.str2long(h_temp_s_date) - 19110000) / 10000.0, h_temp_s_date.substring(4), h_temp_s_date.substring(6),
//                (comcr.str2long(h_temp_e_date) - 19110000) / 10000.0, h_temp_e_date.substring(4), h_temp_e_date.substring(6));
        szTmp = String.format("%4.4s年%2.2s月%2.2s日 ~ %4.4s年%2.2s月%2.2s日",
                hTempSDate, comc.getSubString(hTempSDate,4), comc.getSubString(hTempSDate,6),
                hTempEDate, comc.getSubString(hTempEDate,4), comc.getSubString(hTempEDate,6));
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        szTmp = String.format("貨幣單位 : %s 元", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "證號/統編  客戶名稱     產品別 轉催日期   本    金     違約金   利    息   費    用  催收款餘額 尚有其他帳戶未轉催收", 1);
         
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printTotal() throws Exception {
    	
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        
        StringBuffer sb = new StringBuffer();       
        sb.append(comc.fixLeft("*非批次:", 8));
        szTmp = String.format("%,6d", page1Cnt);
        sb.append(comc.fixRight(szTmp, 7));
        sb.append(comc.fixLeft(" ", 25));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", page1CbAmt);    //本金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", page1CiAmtPn);  //違約金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", page1CiAmtRi);  //利息小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", page1CcAmt);  //費用小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", page1Amt);    //催收款餘額小計 
        sb.append(comc.fixRight(szTmp, 11));
        
        buf = "";
        buf = sb.toString();
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));  
     
        sb = new StringBuffer();       
        sb.append(comc.fixLeft(" 批次:", 8));
        szTmp = String.format("%,6d", totalCnt - page1Cnt);
        sb.append(comc.fixRight(szTmp, 7));
        sb.append(comc.fixLeft(" ", 25));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCbAmt - page1CbAmt);    //本金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCiAmtPn - page1CiAmtPn);  //違約金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCiAmtRi - page1CiAmtRi);  //利息小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCcAmt - page1CcAmt);  //費用小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalAmt - page1Amt);    //催收款餘額小計 
        sb.append(comc.fixRight(szTmp, 11));
        
        buf = "";
        buf = sb.toString();
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));        

        sb = new StringBuffer();
        sb.append(comc.fixLeft(" 總筆數:", 8));
        szTmp = String.format("%,6d", totalCnt);
        sb.append(comc.fixRight(szTmp, 7));
        sb.append(comc.fixLeft(" ", 25));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCbAmt);    //本金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCiAmtPn);  //違約金小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCiAmtRi);  //利息小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalCcAmt);  //費用小計
        sb.append(comc.fixRight(szTmp, 10));
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalAmt);    //催收款餘額小計 
        sb.append(comc.fixRight(szTmp, 11));
        
        buf = "";
        buf = sb.toString();
        buf += "\n\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        
        buf = String.format("%10.10s經/副理%20.20s襄理%18.18s會計%18.18s覆核%18.18s文件製作人", " ", " ", " ", " ", " ");
        buf += "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        temstr = String.format("%s/reports/COL_B003_%s_%s", comc.getECSHOME(), hTempSDate, hTempEDate);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("報表名稱 : %s", temstr));

    }
    
    /***********************************************************************/
    String selectCrdIdno1(String asIdNo) throws Exception {
    	String outIdPSeqno = asIdNo;
        sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
        setString(1, asIdNo);
        
        extendField = "crd_idno_1.";
        
        if (selectTable() > 0) {
        	outIdPSeqno = getValue("crd_idno_1.id_p_seqno");
        }

        return outIdPSeqno;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB003 proc = new ColB003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
