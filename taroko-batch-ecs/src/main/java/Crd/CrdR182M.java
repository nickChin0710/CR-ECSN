/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/10/02 V1.00.01  Wilson      program initial                            *
*  112/11/10 V1.00.02  Wilson      日期減一天                                                                                                    *                                                                        *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class CrdR182M extends AccessDAO {
    private final String PROGNAME = "MASTER CARD季報表  112/11/10 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 45;
    String prgmId    = "CrdR182M";

    String rptIdR1 = "CRM182M";
    String rptName1  = "MASTER CARD季報表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;
    int totCnt1 = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hFirstDay =  "";
    String h20YearsOldDate =  "";

    String cardCardNo = "";
    String cardAcnoPSeqno = "";
    String cardPSeqno = "";
    String cardIdPSeqno = "";
    String cardOriIssueDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinType = "";
    String cardSupFlag = "";
    String cardComboIndicator = "";
    String cardCurrentCode = "";
    String cardLastConsumeDate = "";
    int issueCnt = 0;
    int oppostCnt = 0;
    int currentCnt = 0;
    int vAllCnt = 0;
    int mAllCnt = 0;
    int jAllCnt = 0;
    int loadBilCnt = 0;
    int loadCrdCnt = 0;
    String typeCardNote = "";
    String acctCardIndicator = "";
    String idnoStudent = "";
    String idnoBirthday = "";
    String idno20Flag = "";


    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    int arrayX = 24;
    int arrayY = 9;
    String[] allDataH = new String [arrayX];
    int[][] allData = new int [arrayX][arrayY];   

    buft htail = new buft();
    buf1 data  = new buf1();

/***********************************************************************/
public int mainProcess(String[] args) 
{
 try 
   {
    // ====================================
    // 固定要做的
    dateTime();
    setConsoleMode("Y");
    javaProgram = this.getClass().getName();
    showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=["+args.length+"]");
 
    // 固定要做的
    if(!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
    // =====================================
    if(args.length > 3) {
       comc.errExit("Usage : CrdR182M [yyyymmdd] [seq_no] ", "");
      }
  
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
 
    hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
    if(comc.getSubString(hCallBatchSeqno,0,8).equals(comc.getSubString(comc.getECSHOME(),0,8)))
      { hCallBatchSeqno = "no-call"; 
      }
 
    String checkHome = comc.getECSHOME();
    if(hCallBatchSeqno.length() > 6) {
       if(comc.getSubString(hCallBatchSeqno,0,6).equals(comc.getSubString(checkHome,0,6))) 
         {
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

    if (args.length >  0) {
        hBusiBusinessDate = "";
        if(args[0].length() == 8) {
           hBusiBusinessDate = args[0];
          } else {
           String errMsg = String.format("指定營業日[%s]", args[0]);
           comcr.errRtn(errMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
    }
    selectPtrBusinday();
    
    if (!hBusiBusinessDate.equals("20230331") && !hBusiBusinessDate.equals("20230630") &&
    		!hBusiBusinessDate.equals("20230930") && !hBusiBusinessDate.equals("20231231")) {
		showLogMessage("E", "", "報表日不為該季最後一天,不執行此程式");
		return 0;
    }
 
    initArray();
    
    selectCrdIdno();

    selectActDebt1();

    selectActDebt2();
    
    selectCrdCard();

    headFile();
    writeFile();
    tailFile();

//改為線上報表 
//    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
//    filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
//    comc.writeReport(filename, lpar1);
    comcr.insertPtrBatchRpt(lpar1);
 
    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束";
    showLogMessage("I", "", comcr.hCallErrorDesc);
    if (comcr.hCallBatchSeqno.length() == 20)   comcr.callbatch(1, 0, 1); // 1: 結束

    finalProcess();
    return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
                         }
}
// ************************************************************************
public int selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                           : hBusiBusinessDate;
   }
   
   hFirstDay = hBusiBusinessDate.substring(0, 6) + "01";

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hBegDateBil = getValue("h_beg_date_bil");
      hEndDateBil = getValue("h_end_date_bil");	   
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate));
   return 0;
}
/***********************************************************************/
void initArray() throws Exception
{
   for (int i = 0; i < arrayX; i++)
       {
        allDataH[i] = "";
        switch (i) 
         {
          case 0+1 :
            allDataH[i] = "至少動用一次有效戶數";
            break;
          case 1+1 :
            allDataH[i] = "收取費用違約金戶數";
            break;
          case 2+1 :
            allDataH[i] = "              金額";
            break;
          case 3+1 :
            allDataH[i] = "已繳款（ＴＸ：２０）金額";
            break;
          case 4+1 :
            allDataH[i] = "逾期（筆數／金額）";
            break;
          case 5+1 :
            allDataH[i] = "低於３０天          筆數";
            break;
          case 6+1 :
            allDataH[i] = "                    金額";
            break;
          case 7+1 :
            allDataH[i] = "    ３０天          筆數";
            break;
          case 8+1 :
            allDataH[i] = "                    金額";
            break;
          case 9+1 :
            allDataH[i] = "    ６０天          筆數";
            break;
          case 10+1 :
            allDataH[i] = "                    金額";
            break;
          case 11+1 :
            allDataH[i] = "    ９０天          筆數";
            break;
          case 12+1 :
            allDataH[i] = "                    金額";
            break;
          case 13+1 :
            allDataH[i] = "  １２０天          筆數";
            break;
          case 14+1 :
            allDataH[i] = "                    金額";
            break;
          case 15+1 :
            allDataH[i] = "    合計            筆數";
            break;
          case 16+1 :
            allDataH[i] = "                    金額";
            break;
          case 17+1 :
            allDataH[i] = "季流通戶數 ";
            break;
          case 18+1 :
            allDataH[i] = "      卡數";
            break;
          case 19+1 :
            allDataH[i] = "季新增戶數";
            break;
          case 20+1 :
            allDataH[i] = "      卡數";
            break;
          case 21+1 :
            allDataH[i] = "季停用戶數";
            break;
          case 22+1 :
            allDataH[i] = "      卡數 ";
            break;
         }
       }
}

/***********************************************************************/
    void selectCrdIdno() throws Exception {
	
	    //至少動用(消費)一次有效戶數(普卡)
    	allData[1][1] = selectCrdIdnoConsumeC();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeC ="+ allData[1][1]);
	    
	    //至少動用(消費)一次有效戶數(金卡)
	    allData[1][2] = selectCrdIdnoConsumeG();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeG ="+ allData[1][2]);
	    
	    //至少動用(消費)一次有效戶數(白金卡)
	    allData[1][3] = selectCrdIdnoConsumeP1();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeP1 ="+ allData[1][3]);
	    
	    //至少動用(消費)一次有效戶數(商務卡)
	    allData[1][4] = selectCrdIdnoConsumeC2();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeC2 ="+ allData[1][4]);
	    
	    //至少動用(消費)一次有效戶數(商旅卡)
	    allData[1][5] = selectCrdIdnoConsumeP2();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeP2 ="+ allData[1][5]);
	    
	    //至少動用(消費)一次有效戶數(鈦金商旅卡)
	    allData[1][6] = selectCrdIdnoConsumeS1();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeS1 ="+ allData[1][6]);
	    
	    //至少動用(消費)一次有效戶數(鈦金卡)
	    allData[1][7] = selectCrdIdnoConsumeS2();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeS2 ="+ allData[1][7]);
	    
	    //至少動用(消費)一次有效戶數(無限卡)
	    allData[1][8] = selectCrdIdnoConsumeI();
	    showLogMessage("I","","Read END selectCrdIdnoConsumeI ="+ allData[1][8]);
    }

/***********************************************************************/
	int selectCrdIdnoConsumeC() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'C' ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeG() throws Exception {
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'G' ";
	  sqlCmd += "     and a.last_consume_date between ? and ? ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  setString(1 , hBegDate);
	  setString(2 , hBusiBusinessDate);
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoConsumeP1() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'P' ";
		  sqlCmd += "     and a.group_code not in ('1670','1671') ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeC2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type in ('03','06') ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'C' ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeP2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'P' ";
		  sqlCmd += "     and a.group_code in ('1670','1671') ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeS1() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'S' ";
		  sqlCmd += "     and a.group_code in ('6673','6674') ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeS2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'S' ";
		  sqlCmd += "     and a.group_code not in ('6673','6674') ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoConsumeI() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and a.current_code = '0' ";
		  sqlCmd += "     and b.card_note = 'I' ";
		  sqlCmd += "     and a.last_consume_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
    void selectActDebt1() throws Exception {
	
    	//違約金(戶數/金額)(普卡)
    	selectActDebtPnC();
    	
    	//違約金(戶數/金額)(金卡)
    	selectActDebtPnG();
    	
    	//違約金(戶數/金額)(白金卡)
    	selectActDebtPnP1();
    	
    	//違約金(戶數/金額)(商務卡)
    	selectActDebtPnC2();
    	
    	//違約金(戶數/金額)(商旅卡)
    	selectActDebtPnP2();
    	
    	//違約金(戶數/金額)(鈦金商務卡)
    	selectActDebtPnS1();
    	
    	//違約金(戶數/金額)(鈦金卡)
    	selectActDebtPnS2();
    	
    	//違約金(戶數/金額)(無限卡)
    	selectActDebtPnI();
    }
    
/***********************************************************************/
	void selectActDebtPnC() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'C' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][1] = tmpFlowCnt;
	    		
	    allData[3][1] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnG() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'G' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][2] = tmpFlowCnt;
	    		
	    allData[3][2] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnP1() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'P' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.group_code not in ('1670','1671') ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][3] = tmpFlowCnt;
	    		
	    allData[3][3] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnC2() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.acct_type in ('03','06') ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.card_note = 'C' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][4] = tmpFlowCnt;
	    		
	    allData[3][4] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnP2() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'P' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.group_code in ('1670','1671') ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][5] = tmpFlowCnt;
	    		
	    allData[3][5] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnS1() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'S' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.group_code in ('6673','6674') ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][6] = tmpFlowCnt;
	    		
	    allData[3][6] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnS2() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'S' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.group_code not in ('6673','6674') ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][7] = tmpFlowCnt;
	    		
	    allData[3][7] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
	void selectActDebtPnI() throws Exception {
		int tmpFlowCnt = 0;
		int tmpBegBalPnSum = 0;
	  
	    sqlCmd = " select count(DISTINCT(d.acno_p_seqno)) as flow_count_a, ";
	    sqlCmd += "       sum(a.beg_bal) as beg_bal_pn_sum ";
	    sqlCmd += "  from crd_card d, act_debt a, act_acno c ";
	    sqlCmd += " where c.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.p_seqno = a.p_seqno ";
	    sqlCmd += "   and d.bin_type = 'M' ";
	    sqlCmd += "   and d.acct_type = '01' ";
	    sqlCmd += "   and d.card_note = 'I' ";
	    sqlCmd += "   and a.acct_code  ='PN' ";
	    sqlCmd += "   and a.curr_code  = '901' ";
	    sqlCmd += "   and a.acct_month between ? and ? ";
	    setString(1 , hBegDate.substring(0, 6));
	    setString(2 , hBusiBusinessDate.substring(0, 6));
	    int recCnt = selectTable();
	  
	    if(recCnt > 0) {
	    	tmpFlowCnt = getValueInt("flow_count_a");
	    	tmpBegBalPnSum = getValueInt("beg_bal_pn_sum");
	    }
	    
	    allData[2][8] = tmpFlowCnt;
	    		
	    allData[3][8] = tmpBegBalPnSum;
	}
	
	/***********************************************************************/
    void selectActDebt2() throws Exception {
	
	    //逾期(筆數/金額)
        selectActDebtLate();
        
        //合計
        allData[16][1] = allData[6][1] + allData[8][1] + allData[10][1] + allData[12][1] + allData[14][1];
        		
        allData[17][1] = allData[7][1] + allData[9][1] + allData[11][1] + allData[13][1] + allData[15][1];
        		
        allData[16][2] = allData[6][2] + allData[8][2] + allData[10][2] + allData[12][2] + allData[14][2];
                		
        allData[17][2] = allData[7][2] + allData[9][2] + allData[11][2] + allData[13][2] + allData[15][2];
        
        allData[16][3] = allData[6][3] + allData[8][3] + allData[10][3] + allData[12][3] + allData[14][3];
		
        allData[17][3] = allData[7][3] + allData[9][3] + allData[11][3] + allData[13][3] + allData[15][3];

        allData[16][4] = allData[6][4] + allData[8][4] + allData[10][4] + allData[12][4] + allData[14][4];
		
        allData[17][4] = allData[7][4] + allData[9][4] + allData[11][4] + allData[13][4] + allData[15][4];

        allData[16][5] = allData[6][5] + allData[8][5] + allData[10][5] + allData[12][5] + allData[14][5];
		
        allData[17][5] = allData[7][5] + allData[9][5] + allData[11][5] + allData[13][5] + allData[15][5];

        allData[16][6] = allData[6][6] + allData[8][6] + allData[10][6] + allData[12][6] + allData[14][6];
		
        allData[17][6] = allData[7][6] + allData[9][6] + allData[11][6] + allData[13][6] + allData[15][6];        
    }
    
/***********************************************************************/
	void selectActDebtLate() throws Exception {
		String tmpItemType = "";
		String tmpGroupCode = "";
		String tmpIntRateMcode2 = "";
		int tmpSumAmt = 0;
		int tmpCnt = 0;
	  	  
	    sqlCmd = " select card_note2, ";
	    sqlCmd += "       group_code, ";
	    sqlCmd += "       int_rate_mcode2, ";
	    sqlCmd += "       sum(sum_end_bal) sum_amt, ";
	    sqlCmd += "       count(*) cnt from (select c.card_note, ";
	    sqlCmd += "                                 a.acct_type, ";
	    sqlCmd += "                                 case when a.acct_type='01' then card_note else 'C2' end as card_note2, ";
	    sqlCmd += "                                 c.group_code, ";
	    sqlCmd += "                                 b.int_rate_mcode, ";
	    sqlCmd += "                                 case when int_rate_mcode >= 4 then 4 else int_rate_mcode end as int_rate_mcode2, ";
	    sqlCmd += "                                 c.card_no, ";
	    sqlCmd += "                                 sum(a.end_bal) as sum_end_bal ";
	    sqlCmd += "                            from act_debt a,act_acno b,crd_card c ";
	    sqlCmd += "                           where a.p_seqno = b.p_seqno ";
	    sqlCmd += "                             and a.p_seqno = c.p_seqno ";
	    sqlCmd += "                             and a.card_no = c.card_no ";
	    sqlCmd += "                             and c.bin_type = 'M' ";
	    sqlCmd += "                             and a.end_bal > 0 ";
//	    sqlCmd += "                             and a.acct_code not in ('DB') ";  --先預留是否排除呆帳
	    sqlCmd += "                           group by c.card_note,a.acct_type,c.group_code,b.int_rate_mcode,c.card_no) ";
	    sqlCmd += " group by card_note2,group_code,int_rate_mcode2  ";
	    sqlCmd += " order by card_note2   ";
	    int recCnt = selectTable();
	    
	    for(int i = 0; i < recCnt; i++ ) {
	  	    tmpItemType = getValue("card_note2",i);
	  	    tmpGroupCode = getValue("group_code",i);
	  	    tmpIntRateMcode2 = getValue("int_rate_mcode2",i);
	  	    tmpCnt = getValueInt("cnt",i);
	  	    tmpSumAmt = getValueInt("sum_amt",i);		  	  		     
	  	    
	  	    switch (tmpItemType) {
	  	   
	  	        case "C": 
	  	        	if(tmpIntRateMcode2.equals("0")) {
	  	        		allData[6][1] = tmpCnt;
	  	        		allData[7][1] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("1")) {
	  	        		allData[8][1] = tmpCnt;
	  	        		allData[9][1] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("2")) {
	  	        		allData[10][1] = tmpCnt;
	  	        		allData[11][1] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("3")) {
	  	        		allData[12][1] = tmpCnt;
	  	        		allData[13][1] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("4")) {
	  	        		allData[14][1] = tmpCnt;
	  	        		allData[15][1] = tmpSumAmt;
	  	        	}
	  	        	else {
	  	        		break;
	  	        	}
	  	        	   	  	        
	  	        	break;
	  	        
	  	        case "G": 
	  	        	if(tmpIntRateMcode2.equals("0")) {
	  	        		allData[6][2] = tmpCnt;
	  	        		allData[7][2] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("1")) {
	  	        		allData[8][2] = tmpCnt;
	  	        		allData[9][2] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("2")) {
	  	        		allData[10][2] = tmpCnt;
	  	        		allData[11][2] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("3")) {
	  	        		allData[12][2] = tmpCnt;
	  	        		allData[13][2] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("4")) {
	  	        		allData[14][2] = tmpCnt;
	  	        		allData[15][2] = tmpSumAmt;
	  	        	}
	  	        	else {
	  	        		break;
	  	        	}
	  	        	   	  	        
	  	        	break;
	  	        
	  	        case "P": 
	  	        	if(!tmpGroupCode.equals("1670") && !tmpGroupCode.equals("1671")) {
	  	        		if(tmpIntRateMcode2.equals("0")) {
		  	        		allData[6][3] = tmpCnt;
		  	        		allData[7][3] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("1")) {
		  	        		allData[8][3] = tmpCnt;
		  	        		allData[9][3] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("2")) {
		  	        		allData[10][3] = tmpCnt;
		  	        		allData[11][3] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("3")) {
		  	        		allData[12][3] = tmpCnt;
		  	        		allData[13][3] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("4")) {
		  	        		allData[14][3] = tmpCnt;
		  	        		allData[15][3] = tmpSumAmt;
		  	        	}
		  	        	else {
		  	        		break;
		  	        	}
	  	        	}
	  	        	else {
	  	        		if(tmpIntRateMcode2.equals("0")) {
		  	        		allData[6][5] = tmpCnt;
		  	        		allData[7][5] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("1")) {
		  	        		allData[8][5] = tmpCnt;
		  	        		allData[9][5] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("2")) {
		  	        		allData[10][5] = tmpCnt;
		  	        		allData[11][5] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("3")) {
		  	        		allData[12][5] = tmpCnt;
		  	        		allData[13][5] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("4")) {
		  	        		allData[14][5] = tmpCnt;
		  	        		allData[15][5] = tmpSumAmt;
		  	        	}
		  	        	else {
		  	        		break;
		  	        	}
	  	        	}
	  	        		  	        	   	  	        
	  	        	break;
	  	        
	  	        case "C2": 
	  	        	if(tmpIntRateMcode2.equals("0")) {
	  	        		allData[6][4] = tmpCnt;
	  	        		allData[7][4] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("1")) {
	  	        		allData[8][4] = tmpCnt;
	  	        		allData[9][4] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("2")) {
	  	        		allData[10][4] = tmpCnt;
	  	        		allData[11][4] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("3")) {
	  	        		allData[12][4] = tmpCnt;
	  	        		allData[13][4] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("4")) {
	  	        		allData[14][4] = tmpCnt;
	  	        		allData[15][4] = tmpSumAmt;
	  	        	}
	  	        	else {
	  	        		break;
	  	        	}
	  	        	   	  	        
	  	        	break;
	  	        
	  	        case "S": 
	  	        	if(!tmpGroupCode.equals("6673") && !tmpGroupCode.equals("6674")) {
	  	        		if(tmpIntRateMcode2.equals("0")) {
		  	        		allData[6][7] = tmpCnt;
		  	        		allData[7][7] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("1")) {
		  	        		allData[8][7] = tmpCnt;
		  	        		allData[9][7] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("2")) {
		  	        		allData[10][7] = tmpCnt;
		  	        		allData[11][7] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("3")) {
		  	        		allData[12][7] = tmpCnt;
		  	        		allData[13][7] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("4")) {
		  	        		allData[14][7] = tmpCnt;
		  	        		allData[15][7] = tmpSumAmt;
		  	        	}
		  	        	else {
		  	        		break;
		  	        	}
	  	        	}
	  	        	else {
	  	        		if(tmpIntRateMcode2.equals("0")) {
		  	        		allData[6][6] = tmpCnt;
		  	        		allData[7][6] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("1")) {
		  	        		allData[8][6] = tmpCnt;
		  	        		allData[9][6] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("2")) {
		  	        		allData[10][6] = tmpCnt;
		  	        		allData[11][6] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("3")) {
		  	        		allData[12][6] = tmpCnt;
		  	        		allData[13][6] = tmpSumAmt;
		  	        	}
		  	        	else if(tmpIntRateMcode2.equals("4")) {
		  	        		allData[14][6] = tmpCnt;
		  	        		allData[15][6] = tmpSumAmt;
		  	        	}
		  	        	else {
		  	        		break;
		  	        	}
	  	        	}	  	        	
	  	        	   	  	        
	  	        	break;

	  	        
	  	        case "I": 
	  	        	if(tmpIntRateMcode2.equals("0")) {
	  	        		allData[6][8] = tmpCnt;
	  	        		allData[7][8] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("1")) {
	  	        		allData[8][8] = tmpCnt;
	  	        		allData[9][8] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("2")) {
	  	        		allData[10][8] = tmpCnt;
	  	        		allData[11][8] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("3")) {
	  	        		allData[12][8] = tmpCnt;
	  	        		allData[13][8] = tmpSumAmt;
	  	        	}
	  	        	else if(tmpIntRateMcode2.equals("4")) {
	  	        		allData[14][8] = tmpCnt;
	  	        		allData[15][8] = tmpSumAmt;
	  	        	}
	  	        	else {
	  	        		break;
	  	        	}
	  	        	   	  	        
	  	        	break;
	  	    }
	    }
	}
	
	/***********************************************************************/
	void selectCrdCard() throws Exception {
		
		//季流通戶數(普卡)
	    allData[18][1] = selectCrdIdnoCurrC();
	    showLogMessage("I","","Read END selectCrdIdnoCurrC ="+ allData[18][1]);
	    
		//季流通戶數(金卡)
	    allData[18][2] = selectCrdIdnoCurrG();
	    showLogMessage("I","","Read END selectCrdIdnoCurrG ="+ allData[18][2]);
	    
		//季流通戶數(白金卡)
	    allData[18][3] = selectCrdIdnoCurrP1();
	    showLogMessage("I","","Read END selectCrdIdnoCurrP1 ="+ allData[18][3]);
	    
		//季流通戶數(商務卡)
	    allData[18][4] = selectCrdIdnoCurrC2();
	    showLogMessage("I","","Read END selectCrdIdnoCurrC2 ="+ allData[18][4]);
	    
		//季流通戶數(商旅卡)
	    allData[18][5] = selectCrdIdnoCurrP2();
	    showLogMessage("I","","Read END selectCrdIdnoCurrP2 ="+ allData[18][5]);
	    
	    //季流通戶數(鈦金商務卡)
	    allData[18][6] = selectCrdIdnoCurrS1();
	    showLogMessage("I","","Read END selectCrdIdnoCurrS1 ="+ allData[18][6]);
	    
	    //季流通戶數(鈦金卡)
	    allData[18][7] = selectCrdIdnoCurrS2();
	    showLogMessage("I","","Read END selectCrdIdnoCurrS2 ="+ allData[18][7]);
	    
		//季流通戶數(世界卡)
	    allData[18][8] = selectCrdIdnoCurrI();
	    showLogMessage("I","","Read END selectCrdIdnoCurrI ="+ allData[18][8]);
	    
	    //季流通卡數
	    selectCrdCardCurr();
	    
		//季新增戶數(普卡)
	    allData[20][1] = selectCrdIdnoNewC();
	    showLogMessage("I","","Read END selectCrdIdnoNewC ="+ allData[20][1]);
	    
		//季新增戶數(金卡)
	    allData[20][2] = selectCrdIdnoNewG();
	    showLogMessage("I","","Read END selectCrdIdnoNewG ="+ allData[20][2]);
	    
		//季新增戶數(白金卡)
	    allData[20][3] = selectCrdIdnoNewP1();
	    showLogMessage("I","","Read END selectCrdIdnoNewP1 ="+ allData[20][3]);
	    
		//季新增戶數(商務卡)
	    allData[20][4] = selectCrdIdnoNewC2();
	    showLogMessage("I","","Read END selectCrdIdnoNewC2 ="+ allData[20][4]);
	    
		//季新增戶數(商旅卡)
	    allData[20][5] = selectCrdIdnoNewP2();
	    showLogMessage("I","","Read END selectCrdIdnoNewP2 ="+ allData[20][5]);
	    
		//季新增戶數(鈦金商務卡)
	    allData[20][6] = selectCrdIdnoNewS1();
	    showLogMessage("I","","Read END selectCrdIdnoNewS1 ="+ allData[20][6]);
	    
		//季新增戶數(鈦金卡)
	    allData[20][7] = selectCrdIdnoNewS2();
	    showLogMessage("I","","Read END selectCrdIdnoNewS2 ="+ allData[20][7]);
	    
		//季新增戶數(世界卡)
	    allData[20][8] = selectCrdIdnoNewI();
	    showLogMessage("I","","Read END selectCrdIdnoNewI ="+ allData[20][8]);
	    
	    //季新增卡數
	    selectCrdCardNew();
	    
		//季停用戶數(普卡)
	    allData[22][1] = selectCrdIdnoOppoC();
	    showLogMessage("I","","Read END selectCrdIdnoOppoC ="+ allData[22][1]);
	    
		//季停用戶數(金卡)
	    allData[22][2] = selectCrdIdnoOppoG();
	    showLogMessage("I","","Read END selectCrdIdnoOppoG ="+ allData[22][2]);
	    
		//季停用戶數(白金卡)
	    allData[22][3] = selectCrdIdnoOppoP1();
	    showLogMessage("I","","Read END selectCrdIdnoOppoP1 ="+ allData[22][3]);
	    
		//季停用戶數(商務卡)
	    allData[22][4] = selectCrdIdnoOppoC2();
	    showLogMessage("I","","Read END selectCrdIdnoOppoC2 ="+ allData[22][4]);
	    
		//季停用戶數(商旅卡)
	    allData[22][5] = selectCrdIdnoOppoP2();
	    showLogMessage("I","","Read END selectCrdIdnoOppoP2 ="+ allData[22][5]);

		//季停用戶數(鈦金商務卡)
	    allData[22][6] = selectCrdIdnoOppoS1();
	    showLogMessage("I","","Read END selectCrdIdnoOppoS1 ="+ allData[22][6]);
	    
		//季停用戶數(鈦金卡)
	    allData[22][7] = selectCrdIdnoOppoS2();
	    showLogMessage("I","","Read END selectCrdIdnoOppoS2 ="+ allData[22][7]);
	    
		//季停用戶數(世界卡)
	    allData[22][8] = selectCrdIdnoOppoI();
	    showLogMessage("I","","Read END selectCrdIdnoOppoI ="+ allData[22][8]);
	    
	    //季停用卡數
	    selectCrdCardOppo();	    
	}
	
	/***********************************************************************/
	int selectCrdIdnoCurrC() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'C' ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrG() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'G' ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrP1() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'P' ";
	  sqlCmd += "     and a.group_code not in ('1670','1671') ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrC2() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type in ('03','06') ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'C' ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrP2() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'P' ";
	  sqlCmd += "     and a.group_code in ('1670','1671') ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrS1() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'S' ";
	  sqlCmd += "     and a.group_code in ('6673','6674') ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrS2() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'S' ";
	  sqlCmd += "     and a.group_code not in ('6673','6674') ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoCurrI() throws Exception 
	{
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'I' ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	void selectCrdCardCurr() throws Exception {
		String tmpItemType = "";
		String tmpGroupCode = "";
		int tmpCnt = 0;
	  	  
	    sqlCmd = " select decode(acct_type,'01',card_note,'C2') as item_type, ";
	    sqlCmd += "       group_code, ";
	    sqlCmd += "       count(*) as crd_cnt ";
	    sqlCmd += "  from crd_card ";
	    sqlCmd += " where bin_type = 'M' ";
	    sqlCmd += "   and current_code = '0' ";
	    sqlCmd += " group by card_note,acct_type,group_code ";
	    int recCnt = selectTable();
	    
	    for(int i = 0; i < recCnt; i++ ) {
	  	    tmpItemType = getValue("item_type",i);
	  	    tmpGroupCode = getValue("group_code",i);
	  	    tmpCnt = getValueInt("crd_cnt",i);	  	  		     
	  	    
	  	    switch (tmpItemType) {
	  	   
	  	        case "C": allData[19][1] = allData[19][1] + tmpCnt;   
	  	        break;
	  	        
	  	        case "G": allData[19][2] = allData[19][2] + tmpCnt;   
	  	        break;
	  	        
	  	        case "P": 
	  	        	if(!tmpGroupCode.equals("1670") && !tmpGroupCode.equals("1671")) {
	  	        		allData[19][3] = allData[19][3] + tmpCnt;
	  	        	}
	  	        	else {
	  	        		allData[19][5] = allData[19][5] + tmpCnt;
	  	        	}	  	        		  	        		  	           
	  	        break;
	  	        
	  	        case "C2": allData[19][4] = allData[19][4] + tmpCnt;   
	  	        break;
	  	        
	  	        case "S": 
	  	        	if(!tmpGroupCode.equals("6673") && !tmpGroupCode.equals("6674")) {
	  	        		allData[19][7] = allData[19][7] + tmpCnt;   
	  	        	}
	  	        	else {
	  	        		allData[19][6] = allData[19][6] + tmpCnt;   
	  	        	}	  	        
	  	        break;
	  	        
	  	        case "I": allData[19][8] = allData[19][8] + tmpCnt;   
	  	        break;
	  	    }
	    }
	}
	
	/***********************************************************************/
	int selectCrdIdnoNewC() throws Exception {
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and b.card_note = 'C' ";
	  sqlCmd += "     and a.ori_issue_date between ? and ? ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  setString(1 , hBegDate);
	  setString(2 , hBusiBusinessDate);
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectCrdIdnoNewG() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'G' ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewP1() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'P' ";
		  sqlCmd += "     and a.group_code not in ('1670','1671') ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewC2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type in ('03','06') ";
		  sqlCmd += "     and b.card_note = 'C' ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewP2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'P' ";
		  sqlCmd += "     and a.group_code in ('1670','1671') ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewS1() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'S' ";
		  sqlCmd += "     and a.group_code in ('6673','6674') ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewS2() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'S' ";
		  sqlCmd += "     and a.group_code not in ('6673','6674') ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	int selectCrdIdnoNewI() throws Exception {
		  
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'I' ";
		  sqlCmd += "     and a.ori_issue_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
		/***********************************************************************/
	void selectCrdCardNew() throws Exception {
		String tmpItemType = "";
		String tmpGroupCode = "";
		int tmpCnt = 0;
	  	  
	    sqlCmd = " select decode(acct_type,'01',card_note,'C2') as item_type, ";
	    sqlCmd += "       group_code, ";
	    sqlCmd += "       count(*) as crd_cnt ";
	    sqlCmd += "  from crd_card ";
	    sqlCmd += " where bin_type = 'M' ";
		sqlCmd += "   and ori_issue_date between ? and ? ";
	    sqlCmd += " group by card_note,acct_type,group_code ";
		setString(1 , hBegDate);
		setString(2 , hBusiBusinessDate);
	    int recCnt = selectTable();
	    
	    for(int i = 0; i < recCnt; i++ ) {
	  	    tmpItemType = getValue("item_type",i);
	  	    tmpGroupCode = getValue("group_code",i);
	  	    tmpCnt = getValueInt("crd_cnt",i);	  	  		     
	  	    
	  	    switch (tmpItemType) {
	  	   
	  	        case "C": allData[21][1] = allData[21][1] + tmpCnt;   
	  	        break;
	  	        
	  	        case "G": allData[21][2] = allData[21][2] + tmpCnt;   
	  	        break;
	  	        
	  	        case "P": 
	  	        	if(!tmpGroupCode.equals("1670") && !tmpGroupCode.equals("1671")) {
	  	        		allData[21][3] = allData[21][3] + tmpCnt;   
	  	        	}
	  	        	else {
	  	        		allData[21][5] = allData[21][5] + tmpCnt;   
	  	        	}
	  	        break;
	  	        
	  	        case "C2": allData[21][4] = allData[21][4] + tmpCnt;  
	  	        break;
	  	        
	  	        case "S": 
	  	        	if(!tmpGroupCode.equals("6673") && !tmpGroupCode.equals("6674")) {
	  	        		allData[21][7] = allData[21][7] + tmpCnt;   
	  	        	}
	  	        	else {
	  	        		allData[21][6] = allData[21][6] + tmpCnt;   
	  	        	}
	  	        break;
	  	        
	  	        case "I": allData[21][8] = allData[21][8] + tmpCnt;   
	  	        break;
	  	    }
	    }
	}
	
	/***********************************************************************/
	int selectCrdIdnoOppoC() throws Exception {
		  		  
		sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in ";
		  sqlCmd += " (select a.id_p_seqno ";
		  sqlCmd += "    from crd_card a,ptr_card_type b ";
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' ";
		  sqlCmd += "     and b.card_note = 'C' ";
		  sqlCmd += "     and a.oppost_date between ? and ? ";
		  sqlCmd += "   group by a.id_p_seqno) ";
		  setString(1 , hBegDate);
		  setString(2 , hBusiBusinessDate);
		  int recCnt = selectTable();

		  return  getValueInt("crd_cnt");
		}
	
/****** *****************************************************************/
	int selectCrdIdnoOppoG() throws Exception {
	  
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and b.card_note = 'G' ";
	  sqlCmd += "     and a.oppost_date between ? and ? ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  setString(1 , hBegDate);
	  setString(2 , hBusiBusinessDate);
	  int recCnt = selectTable();
 
	  return  getValueInt("crd_cnt");
	} 
	/***********************************************************************/
	int selectCrdIdnoOppoP1() throws Exception {
	   
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in "; 
	  sqlCmd += " (select a.id_p_seqno "; 
	  sqlCmd += "    from crd_card a,ptr_card_type b "; 
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' "; 
	  sqlCmd += "     and b.card_note = 'P' ";
	  sqlCmd += "     and a.group_code not in ('1670','1671') ";
	  sqlCmd += "     and a.oppost_date between ? and ? "; 
	  sqlCmd += "   group by a.id_p_seqno) "; 
	  setString(1 , hBegDate); 
	  setString(2 , hBusiBusinessDate); 
	  int recCnt = selectTable(); 
 
	  return  getValueInt("crd_cnt"); 
	} 
	
	/***********************************************************************/ 
	int selectCrdIdnoOppoC2() throws Exception { 
	   
	  sqlCmd = " select count(*) as crd_cnt "; 
	  sqlCmd += "  from crd_idno "; 
	  sqlCmd += " where id_p_seqno in "; 
	  sqlCmd += " (select a.id_p_seqno "; 
	  sqlCmd += "    from crd_card a,ptr_card_type b "; 
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type in ('03','06') "; 
	  sqlCmd += "     and b.card_note = 'C' "; 
	  sqlCmd += "     and a.oppost_date between ? and ? "; 
	  sqlCmd += "   group by a.id_p_seqno) "; 
	  setString(1 , hBegDate); 
	  setString(2 , hBusiBusinessDate); 
	  int recCnt = selectTable(); 
 
	  return  getValueInt("crd_cnt"); 
	}
	
	/***********************************************************************/
	int selectCrdIdnoOppoP2() throws Exception {
		   
		  sqlCmd = " select count(*) as crd_cnt ";
		  sqlCmd += "  from crd_idno ";
		  sqlCmd += " where id_p_seqno in "; 
		  sqlCmd += " (select a.id_p_seqno "; 
		  sqlCmd += "    from crd_card a,ptr_card_type b "; 
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' "; 
		  sqlCmd += "     and b.card_note = 'P' ";
		  sqlCmd += "     and a.group_code in ('1670','1671') ";
		  sqlCmd += "     and a.oppost_date between ? and ? "; 
		  sqlCmd += "   group by a.id_p_seqno) "; 
		  setString(1 , hBegDate); 
		  setString(2 , hBusiBusinessDate); 
		  int recCnt = selectTable(); 
	 
		  return  getValueInt("crd_cnt"); 
		} 
		
		/***********************************************************************/ 
	int selectCrdIdnoOppoS1() throws Exception { 
	   
	  sqlCmd = " select count(*) as crd_cnt "; 
	  sqlCmd += "  from crd_idno "; 
	  sqlCmd += " where id_p_seqno in "; 
	  sqlCmd += " (select a.id_p_seqno "; 
	  sqlCmd += "    from crd_card a,ptr_card_type b "; 
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' "; 
	  sqlCmd += "     and b.card_note = 'S' ";
	  sqlCmd += "     and a.group_code in ('6673','6674') ";
	  sqlCmd += "     and a.oppost_date between ? and ? "; 
	  sqlCmd += "   group by a.id_p_seqno) "; 
	  setString(1 , hBegDate); 
	  setString(2 , hBusiBusinessDate); 
	  int recCnt = selectTable(); 
 
	  return  getValueInt("crd_cnt"); 
	}
	
	/***********************************************************************/ 
	int selectCrdIdnoOppoS2() throws Exception { 
		   
		  sqlCmd = " select count(*) as crd_cnt "; 
		  sqlCmd += "  from crd_idno "; 
		  sqlCmd += " where id_p_seqno in "; 
		  sqlCmd += " (select a.id_p_seqno "; 
		  sqlCmd += "    from crd_card a,ptr_card_type b "; 
		  sqlCmd += "   where a.card_type = b.card_type ";
		  sqlCmd += "     and a.bin_type = 'M' ";
		  sqlCmd += "     and a.acct_type = '01' "; 
		  sqlCmd += "     and b.card_note = 'S' ";
		  sqlCmd += "     and a.group_code not in ('6673','6674') ";
		  sqlCmd += "     and a.oppost_date between ? and ? "; 
		  sqlCmd += "   group by a.id_p_seqno) "; 
		  setString(1 , hBegDate); 
		  setString(2 , hBusiBusinessDate); 
		  int recCnt = selectTable(); 
	 
		  return  getValueInt("crd_cnt"); 
		}
		
		/***********************************************************************/ 
	int selectCrdIdnoOppoI() throws Exception { 
	   
	  sqlCmd = " select count(*) as crd_cnt "; 
	  sqlCmd += "  from crd_idno "; 
	  sqlCmd += " where id_p_seqno in "; 
	  sqlCmd += " (select a.id_p_seqno "; 
	  sqlCmd += "    from crd_card a,ptr_card_type b "; 
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.bin_type = 'M' ";
	  sqlCmd += "     and a.acct_type = '01' "; 
	  sqlCmd += "     and b.card_note = 'I' "; 
	  sqlCmd += "     and a.oppost_date between ? and ? "; 
	  sqlCmd += "   group by a.id_p_seqno) "; 
	  setString(1 , hBegDate); 
	  setString(2 , hBusiBusinessDate); 
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	
	/***********************************************************************/
	void selectCrdCardOppo() throws Exception {
		String tmpItemType = "";
		String tmpGroupCode = "";
		int tmpCnt = 0;
	  	  
	    sqlCmd = " select decode(acct_type,'01',card_note,'C2') as item_type, ";
	    sqlCmd += "       group_code, ";
	    sqlCmd += "       count(*) as crd_cnt ";
	    sqlCmd += "  from crd_card ";
	    sqlCmd += " where bin_type = 'M' ";
		sqlCmd += "   and oppost_date between ? and ? ";
	    sqlCmd += " group by card_note,acct_type,group_code ";
		setString(1 , hBegDate);
		setString(2 , hBusiBusinessDate);
	    int recCnt = selectTable();
	    
	    for(int i = 0; i < recCnt; i++ ) {
	  	    tmpItemType = getValue("item_type",i);
	  	    tmpGroupCode = getValue("group_code",i);
	  	    tmpCnt = getValueInt("crd_cnt",i);	  	  		     
	  	    
	  	    switch (tmpItemType) {
	  	   
	  	        case "C": allData[23][1] = allData[23][1] + tmpCnt;   
	  	        break;
	  	        
	  	        case "G": allData[23][2] = allData[23][2] + tmpCnt;   
	  	        break;
	  	        
	  	        case "P": 
	  	        	if(!tmpGroupCode.equals("1670") && !tmpGroupCode.equals("1671")) {
	  	        		allData[23][3] = allData[23][3] + tmpCnt;   
	  	        	}
	  	        	else {
	  	        		allData[23][5] = allData[23][5] + tmpCnt;   
	  	        	}
	  	        break;
	  	        
	  	        case "C2": allData[23][4] = allData[23][4] + tmpCnt;   
	  	        break;
	  	        
	  	        case "S": 
	  	        	if(!tmpGroupCode.equals("6673") && !tmpGroupCode.equals("6674")) {
	  	        		allData[23][7] = allData[23][7] + tmpCnt;   
	  	        	}
	  	        	else {
	  	        		allData[23][6] = allData[23][6] + tmpCnt;   
	  	        	}
	  	        break;
	  	        
	  	        case "I": allData[23][8] = allData[23][8] + tmpCnt;   
	  	        break;
	  	    }
	    }
	}
	
	/***********************************************************************/
void headFile() throws Exception 
{
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 50);
        buf = comcr.insertStr(buf, "保存年限: 一年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM182     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "         項目                普卡          金卡         白金卡        商務卡        商旅卡      鈦金商務卡      鈦金卡        世界卡    ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "123456789012345678901234567890=== ========== ---------- ---------- ";
        buf = "======================== ============= ============= ============= ============= ============= ============= ============= ============= ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
//   buf = "";
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "備 註: １、本表為ＯＲＧ１０６下所有ＴＹＰＥ，排除ＴＹＰＥ為５９９、９９７、９９８者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "       ２、流通卡為截至目前未停用之卡片，即控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "       ３、有效卡為最近６個月有消費紀錄，且控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeFile() throws Exception 
{
     String tmp   = "";
     String szTmp = "";

     if(lineCnt1 > reportPageLine) {
        headFile();
       }
if(DEBUG==1) showLogMessage("I",""," write="+ allData[18][1]+","+ allData[18][2]+","+ allData[18][3]);

     for (int i = 0; i < arrayX; i++)
       {
        if(i == 0)   continue;

        data = null;
        data = new buf1();
        
        data.name = allDataH[i];

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][1]);
        switch (i) 
         {
          case 5: szTmp = "          ";
                   break;
         }
        data.data01      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][2]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data02      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][3]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data03      = szTmp;
        
        szTmp = comcr.commFormat("2z,3z,3z", allData[i][4]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data04      = szTmp;
        
        szTmp = comcr.commFormat("2z,3z,3z", allData[i][5]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data05      = szTmp;
        
        szTmp = comcr.commFormat("2z,3z,3z", allData[i][6]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data06      = szTmp;
        
        szTmp = comcr.commFormat("2z,3z,3z", allData[i][7]);
        switch (i) 
         {
          case 5: szTmp = "          ";
                   break;
         }
        data.data07      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][8]);
        switch (i) 
        {
         case 5: szTmp = "          ";
                  break;
        }
        data.data08      = szTmp;


        buf = data.allText();

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;
       }

     return;
}
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR182M proc = new CrdR182M();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String fileValue;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(fileValue,110);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String data01;
        String data02;
        String data03;
        String data04;
        String data05;
        String data06;
        String data07;
        String data08;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";

            rtn += fixLeft(name         , 26+1);
            rtn += fixLeft(data01       ,  13+1);
            rtn += fixLeft(data02       ,  13+1);
            rtn += fixLeft(data03       ,  13+1);
            rtn += fixLeft(data04       ,  13+1);
            rtn += fixLeft(data05       ,  13+1);
            rtn += fixLeft(data06       ,  13+1);
            rtn += fixLeft(data07       ,  13+1);
            rtn += fixLeft(data08       ,  13+1);
 //         rtn += fixLeft(len          ,  1);
            return rtn;
        }

       
    }
String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)    spc += " ";
        if (str == null)                  str  = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}

