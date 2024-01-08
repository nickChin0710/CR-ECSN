/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
* 2020/10/11 V1.00.01  yanghan     initial	                              *
* 2020/11/05 V1.01.01  yanghan     修改了寫入db的數據                         *
* 109/11/20  V1.01.02  yanghan     修改了部分流程                             *
* 109/11/29  V1.01.03  yanghan     解決卡號重複等問題                         *
* 109/12/19  V1.01.04  yanghan     修改 部分流程                              *
* 112/05/02  V1.01.05  Lai         增排除非一般消費、CMS_AIRPORT_LIST增欄位   *
* 112/07/10  V1.01.07  Lai         CMS_RIGHT_PARM有增欄位(選1或選2),及調整    *
* 112/07/13  V1.01.08  Lai         TCBINFINITECARD_110、TCBINFINITECARD10_110 *
* 112/07/19  V1.01.09  Lai         modify ftp                                 *
* 112/08/17  V1.02.01  Lai         error (curr_cond 指加碼)                   *
* 112/09/01  V1.02.02  Lai         add 一般消費 check cms_right_parm_detl     *
* 112/09/11  V1.02.03  Lai         modify input parm  by acct_month           *
* 112/11/07  V1.02.04  Lai         Add    isBegDate_air                       *
* 112/11/21  V1.02.05  Lai         modify Tot_amt、MOD_TYPE                   *
* 112/11/22  V1.00.06  Kirin       每月1日執行                                *
* 112/12/04  V1.02.07  lai         modify join crd_card current_code = '0'    *
* 112/12/24  V1.00.09  Zuwei Su   fix 弱掃弱點           *
* 112/12/28  V1.00.10  Ryan       hBusinessPrevMonth 拿掉-1 ,移除1日執行檢核           
* 113/01/04  V1.00.11  Ryan       調整PREFERENTIAL                *
******************************************************************************/
package Cms;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import org.omg.CORBA.portable.ApplicationException;

import com.AccessDAO;
import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.*;

//import hidden.org.codehaus.plexus.interpolation.util.StringUtils;

public class CmsC003 extends BaseBatch {
  private String progname = "肯驛機場接送名單程式    112/12/28 V1.00.10";
  CommFunction    comm = new CommFunction();
  CommCrd         comc = new CommCrd();
  CommCrdRoutine comcr = null;

        int DEBUG   = 1;
        int DEBUG_F = 0;
        int DEBUG_DAT = 0;

	String prgmId = "CmsC003";
	String hCallBatchSeqno = "";
	String txt1 = "";
        String rptName1   = "TCBINFINITECARD_110";
	int    rptSeq1 = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

        String rptName2   = "TCBINFINITECARD10_110";
	String txt2 = "";
	int    rptSeq2 = 0;
	List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();

	String hBusiBusinessDate  = "";
        String hBusinessPrevMonth = "";
        String hBusinessPrevMonthEnd = "";
        String hBusinessUseMonth  = "";
        String hBusinessPrevYear  = "";
	String hSystemDate = "";
	String parmTmsPasswd = "";
	int okCount = 0;
	int airCnt  = 0;
	int lastMm  = 0;

	int tmpCount = 0;
	int tmpCount1 = 0;
	int out = -1;
	int countSelect = 0;
	int maxLastCnt = 0;
	String currAmtCond = "";
	String acctTypeFlag = "";
	String groupCardFlag = "";
	String debutYearFlag = "";
	String condPer = "";
	String airCond = "";
	String bUseCond = "";
	String cUseCond = "";
	String debutGroupCond = "";
	String chooseCond = "";
        String stderr    = "";

    List<ParmDetl> parmDetlList  = new ArrayList<ParmDetl>();
    List<String>   cardNoList    = new ArrayList<String>();
	String cardHldrFlagTmp = "";
	String debutSupFlag0 = "";
	String debutSupFlag1 = "";
	String consumeType = "";
	String airSupFlag0 = "";
	String airSupFlag1 = "";

	String consumeBl = "";
	String consumeCa = "";
	String consumeIt = "";
	String consumeAo = "";
	String consumeId = "";
	String consumeOt = "";

	int consume00Cnt = 0;
	int currPreMonth = 0;
	String debutMonth1 = "";
	String debutMonth2 = "";

	String currCond = "";
	String lastCond = "";
	String condPerls = "";
	int currCnt = 0;
	int maxDestAmt = 0;
	int maxLastAmt = 0;
	int currMinAmt = 0;
	int maxBaseAmt = 0;
	int TotBaseAmt = 0;
	String projCode = "";
	String isIssueDate = "";
	String idNo = "";
	Boolean isEffectivity = false;
	String supFlag = "";
	int currAmt = 0;
	String currTotCnt = "";
	int perAmt = 0;
	int perCnt = 0;
	String outFileName  = "";
	String outFileName2 = "";
	String engName = "";
	String purchaseDate = "";
	String referenceNo  = "";
	String mchtCategory = "";
	String mchtGroup    = "";
	String mchtCategoryTmp = "";
	String cardNo          = "";
	String majorCardNo     = "";
	String hBinType        = "";
	String hMajorIdPSeqno  = "";
	String hOldCardNo      = "";
	String hIdNo           = "";
	String hChiName        = "";
	String hMajorId        = "";
	int currCntCond = 0;

	String acctType      = "";
	String groupCode     = "";
	String cardType      = "";
	String idPSeqno      = "";
	String majorIdPSeqno = "";
	String cardPurchCode = "";
	String it1Type       = "";
	String currentCode   = "";
	String airAmtType    = "";
	int airAmt = 0;
	String isEndYm = "";
	String isBegYm = "";
	String isEndDate_air = "";
	String isBegDate_air = "";
	String airDay = "";
	java.util.Date startDate = null;
	java.util.Date purchaseDateEnd = null;
	java.util.Date purchaseDateTemp = null;
        String tmpstr1 = "";
        String tmpstr2 = "";

	Document1 data1 = new Document1();
	Document2 data2 = new Document2();

/*******************************************************************************/
public int mainProcess(String[] args) 
{
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (comm.isAppActive(javaProgram)) {
				//comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
				showLogMessage("Error!! Someone is running this program now!!!", "", "Please wait a moment to run again!!");
        		return 0; 
			}

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = hCallBatchSeqno;
			comcr.hCallRProgramCode = javaProgram;

			if(args.length >= 1) {
			   if(args[0].length() == 8) {
			      hBusiBusinessDate = args[0];
//			      hBusinessPrevMonth= commDate.dateAdd(hBusiBusinessDate, 0,-1, 0).substring(0, 6);
			      hBusinessPrevMonth = hBusiBusinessDate.substring(0, 6);
			   } else {
				showLogMessage("I","",String.format("->error,must be[business_date]"));
				comcr.errRtn("", "", comcr.hCallBatchSeqno);
			   }
			} else {
                            commonRtn();
			} 
                        hBusinessUseMonth = hBusiBusinessDate.substring(0, 6);
                        hBusinessPrevYear = commDate.dateAdd(hBusiBusinessDate, -1,0,0).substring(0,4);
                        sqlCmd = "select to_char(last_day(to_date(cast(? as varchar(6)),'yyyymm')),'yyyymmdd') h_end_date ";
                        sqlCmd += " from ptr_businday ";
                        setString(1, hBusinessPrevMonth);
                        int recordCnt = selectTable();
                        hBusinessPrevMonthEnd = getValue("h_end_date");

//            if (!hBusiBusinessDate.substring(6).equals("01")) {                            
//               showLogMessage("I", "", "本程式只在每月1日執行, 本日非執行日!! process end....");
//               return 0;
//            }                        
			showLogMessage("I","","處理營業日日期="+hBusiBusinessDate + ",Acct_m="+ hBusinessPrevMonth+",Use_M="+ hBusinessUseMonth+","+hBusinessPrevMonthEnd+",去年="+hBusinessPrevYear);

			comcr.callbatch(0, 0, 0);
			deleteCmsAirportList();// 刪除前24個月的數據

			tmpCount = 0;
			tmpCount1 = 0;
	
	//outPutTextFile();
			selectCmsRightParm(hBusiBusinessDate);

                       tmpstr1 = String.format("%s/media/cms/%s", comc.getECSHOME(), rptName1);
                       tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
	               showLogMessage("I", "", "Output Filepath1 = [" + tmpstr1 + "]");
                       tmpstr2 = String.format("%s/media/cms/%s", comc.getECSHOME(), rptName2);
                       tmpstr2 = Normalizer.normalize(tmpstr2, java.text.Normalizer.Form.NFKD);
	               showLogMessage("I", "", "Output Filepath2 = [" + tmpstr2 + "]");
/*
			if(!lpar1.isEmpty()) {
                           comc.writeReport(tmpstr1, lpar1);
			   lpar1.clear();
			}
			if(!lpar2.isEmpty()) {
			   comc.writeReport(tmpstr2, lpar2);
			   lpar2.clear();
			}
*/
	comc.writeReport(tmpstr1, lpar1);
	comc.writeReport(tmpstr2, lpar2);

	showLogMessage("I","",String.format("筆數  tot_count=[%d],ok_count=[%d]",tmpCount,tmpCount1));

        ftpRtn(rptName1);
        ftpRtn(rptName2);

	// ==============================================
	// 固定要做的
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
/*******************************************************************************/
void commonRtn() throws Exception {
    sqlCmd = "select business_date ";
//    sqlCmd += "  , substr(to_char(add_months(to_date(business_date,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_business_prev_month ";
    sqlCmd += " , left(business_date,6) as h_business_prev_month ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hBusinessPrevMonth = getValue("h_business_prev_month");
        hBusiBusinessDate  = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                           : hBusiBusinessDate;

    } else {
        comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
    }


    sqlCmd  = "select substr(to_char(add_months(to_date(?,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_prev_month ";
    sqlCmd += "     , substr(to_char(add_months(to_date(?,'yyyymmdd'),-12),'yyyymmdd'),1,4) h_business_prev_year  ";
    sqlCmd += " from dual ";
    setString(1, hBusiBusinessDate);
    setString(2, hBusiBusinessDate);
    recordCnt = selectTable();
    if (recordCnt > 0) {
        hBusinessPrevMonth = getValue("h_prev_month");
        hBusinessPrevYear  = getValue("h_business_prev_year");
    }
    hBusinessUseMonth = hBusiBusinessDate.substring(0, 6);

}
/***********************************************************************/
// 2.4. 讀取【cms_right_parm】 查看是否有符合條件的數據 若有則繼續 若無則結束程序
	void selectCmsRightParm(String date) throws Exception {

		sqlCmd = "select *";
		sqlCmd += "  from cms_right_parm";
		sqlCmd += " where active_status='Y' and apr_flag='Y' and ITEM_NO='08'";
		sqlCmd += " and decode(proj_date_s,'','20010101',proj_date_s)<=?";
		sqlCmd += " and decode(proj_date_e,'','99991231',proj_date_e)>=?";
		ppp(1, date);
		ppp(2, date);
		sqlSelect();
//System.out.println(sqlCmd);
		int ilSelectRow = sqlNrow;
                showLogMessage("I", "", "Main select cms_right_parm cnt=["+ilSelectRow+"] Date="+date);
		if (sqlNrow <= 0) {
			printf("select cms_right_parm not found");
                        showLogMessage("I", "", " Warning select cms_right_parm not found=" + date);
			return;
		} else {
			for (int ii = 0; ii < ilSelectRow; ii++) {
				acctTypeFlag = "";
				groupCardFlag = "";
				debutYearFlag = "";
				condPer = "";
				airCond = "";
				bUseCond = "";
				cUseCond = "";
				cardHldrFlagTmp = "";
				debutSupFlag0 = "";
				debutSupFlag1 = "";
				consume00Cnt = 0;
				debutMonth1 = "";
				debutMonth2 = "";
				currMinAmt = 0;
				currCond = "";
				lastCond = "";
				condPerls = "";
				currCnt = 0;
				projCode = "";
				currAmt = 0;
				currTotCnt = "";

				perAmt = 0;
				perCnt = 0;
				acctTypeFlag = "";
				groupCardFlag = "";
				debutYearFlag = "";
				condPer = "";
				airCond = "";
				bUseCond = "";
				cUseCond = "";
				debutGroupCond = "";
				airSupFlag0 = "";
				airSupFlag1 = "";
				consumeType = "";
				consumeBl = "";
				consumeCa = "";
				consumeIt = "";
				consumeAo = "";
				consumeId = "";
				consumeOt = "";
				it1Type = "";
				currAmtCond = "";
				currCntCond = 0;
				isEndYm = "";
				isBegYm = "";
				isEndDate_air = "";
				isBegDate_air = "";
				airCnt = 0;
				airDay = "";
				debutGroupCond = "";
				maxLastCnt = 0;
				maxLastAmt = 0;
				currPreMonth = 0;
				airAmtType = "";
				airAmt = 0;
				chooseCond = "";
				lastMm = 0;
				
				// 选出last_amt1-last_amt6中最大的值
				for (int ll = 1; ll <= 6; ll++) {
					int lastAmt = colInt(ii, "last_amt" + ll);
					if (lastAmt > maxLastAmt) {
						maxLastAmt = lastAmt;
						maxLastCnt = colInt(ii, "last_cnt" + ll);
					}
				}
				// 月份

				currPreMonth = colInt(ii, "curr_pre_month");
				airAmtType   = colSs(ii, "air_amt_type");
				airAmt       = colInt(ii, "air_amt");

				projCode = colSs(ii, "PROJ_CODE");
				acctTypeFlag = colSs(ii, "acct_type_flag");
				groupCardFlag = colSs(ii, "group_card_flag");
				debutYearFlag = colSs(ii, "debut_year_flag");
				condPer = colSs(ii, "cond_per");
				airCond = colSs(ii, "air_cond");
				bUseCond = colSs(ii, "b_use_cond");
				cUseCond = colSs(ii, "c_use_cond");
				debutGroupCond = colSs(ii, "debut_group_cond");
				cardHldrFlagTmp = colSs(ii, "card_hldr_flag");
				debutSupFlag0 = colSs(ii, "debut_sup_flag_0");
				debutSupFlag1 = colSs(ii, "debut_sup_flag_1");

				consumeType = colSs(ii, "consume_type");
				consumeBl = colSs(ii, "consume_bl");
				consumeCa = colSs(ii, "consume_ca");
				consumeIt = colSs(ii, "consume_it");
				consumeAo = colSs(ii, "consume_ao");
				consumeId = colSs(ii, "consume_id");
				consumeOt = colSs(ii, "consume_ot");

				airSupFlag1 = colSs(ii, "air_sup_flag_1");
				airSupFlag0 = colSs(ii, "air_sup_flag_0");
				acctTypeFlag = colSs(ii, "acct_type_flag");
				groupCardFlag = colSs(ii, "group_card_flag");
				debutYearFlag = colSs(ii, "debut_year_flag");
				condPer = colSs(ii, "cond_per");
				airCond = colSs(ii, "air_cond");
				bUseCond = colSs(ii, "b_use_cond");
				cUseCond = colSs(ii, "c_use_cond");
				debutGroupCond = colSs(ii, "debut_group_cond");

				consume00Cnt = colInt(ii, "consume_00_cnt");
				currCnt = colInt(ii, "curr_cnt");
				currAmt = colInt(ii, "curr_amt");
				currCntCond = colInt(ii, "curr_cnt_cond");
				currTotCnt = colSs(ii, "CURR_TOT_CNT");
				perAmt = colInt(ii, "PER_AMT");
				perCnt = colInt(ii, "PER_CNT");
				airCnt = colInt(ii, "air_cnt");

				currCond    = colSs(ii, "CURR_COND");
				currAmtCond = colSs(ii, "CURR_AMT_COND");
				lastCond  = colSs(ii, "LAST_COND");
				condPerls = colSs(ii, "COND_PER");
				projCode  = colSs(ii, "proj_code");
				chooseCond  = colSs(ii, "CHOOSE_COND");
				lastMm      = colInt(ii, "last_mm");
	showLogMessage("I","","Proj="+projCode+",maxLastCnt="+maxLastCnt+","+projCode+",ii="+ii+",CurrCnt="+currCnt+" airCond="+airCond);
				
				debutMonth1 = colSs(ii, "debut_month1");
				debutMonth2 = colSs(ii, "debut_month2");
				airDay = colSs(ii, "air_day");
				currMinAmt = colInt(ii, "curr_min_amt");
				maxLastAmt = colInt(ii, "maxLastAmt");

				it1Type = colSs(ii, "IT_1_TYPE");
showLogMessage("I","","  lastMm=" + lastMm+","+projCode+" acctT="+acctType+" groupCard="+groupCardFlag+" debutYear="+debutYearFlag+" condPer="+condPer+",con="+consumeType);
				// 獲取acct month
				getAcctMonth();
showLogMessage("I","","  查詢消費記錄開始時間= " + isBegYm + " 截至時間 " + isEndYm+","+isBegDate_air+","+isEndDate_air);
				if ((eqIgno(debutYearFlag, "1") || eqIgno(debutYearFlag, "2")) && eqIgno(debutGroupCond, "0")) {
				// 如果 DEBUT_YEAR_FLAG=1 (or 2)且DEBUT_GROUP_COND=0 则不需要找出资料 跳过此种情况
					continue;
				}
				// 取得【cms_right_parm】join【cms_right_parm_detl】資料
				selectCmsRightParmJoinCmsRightParmDetl();
				//獲取卡號
				getCardNo();
				//檢核以及 計算免費次數
				selectBillAndContract();
			}
		}
	}

	// --------------------------------
	// 2.5. 取得參數資料:以【cms_right_parm】join【cms_right_parm_detl】
	void selectCmsRightParmJoinCmsRightParmDetl() throws Exception {
	//	sqlCmd = "select *";
		sqlCmd = "select data_type,data_code,data_code2,data_code3 ";
		sqlCmd += "  from cms_right_parm a left join cms_right_parm_detl b on a.PROJ_CODE=b.PROJ_CODE  and table_id = 'RIGHT'";
		sqlCmd += " where a.active_status='Y' and a.apr_flag='Y' and a.ITEM_NO='08'";
		sqlCmd += " and decode(proj_date_s,'','20010101',proj_date_s)<=?";
		sqlCmd += " and decode(proj_date_e,'','99991231',proj_date_e)>=?";
		sqlCmd += " and a.proj_code=?";
		sqlCmd += " AND ( b.data_type in (decode(?,'Y','01','XX'), ";
		sqlCmd += " decode(?,'Y','02','XX'), ";
		sqlCmd += " decode(?,'Y','05','XX'), ";
		sqlCmd += " decode(?,'Y','06','XX'), ";
		if (debutYearFlag.equals(1) || debutYearFlag.equals(2)) {
			if (!(debutYearFlag.equals(2) && debutGroupCond.equals(2))) {
				sqlCmd += " '03', ";
			}
		}
		sqlCmd += "  decode(?,'Y','07','XX'), ";
		sqlCmd += "  decode(?,'Y','08','XX')) ";
		sqlCmd += " )";
//System.out.println(sqlCmd);
		ppp(1, hBusiBusinessDate);
		ppp(2, hBusiBusinessDate);
		ppp(3, projCode);
		ppp(4, acctTypeFlag);
		ppp(5, groupCardFlag);
		ppp(6, condPer);
		ppp(7, airCond);
		ppp(8, bUseCond);
		ppp(9, cUseCond);
		sqlSelect();
	  	int selectRow = sqlNrow;
                showLogMessage("I",""," DETL Cnt=["+selectRow+"]Proj_cd="+ projCode+","+groupCardFlag);
		if (sqlNrow <= 0) {
			printf("select cms_right_parm && cms_right_parm_detl not found");
		} else {
			parmDetlList.clear();
			for (int ii = 0; ii < selectRow; ii++) {
				ParmDetl detl=new ParmDetl();
				detl.setDataTypeTmp(colSs(ii, "data_type"));
				detl.setDataCodeTmp(colSs(ii, "data_code"));
				detl.setDataCode2Tmp(colSs(ii, "data_code2"));
				detl.setDataCode3Tmp(colSs(ii, "data_code3"));				
				parmDetlList.add(detl);
			}
		}
	}
	void getCardNo() throws Exception {
if(DEBUG==1) showLogMessage("I",""," Begin getCardNo  currCond="+ currCond + ", BL="+ consumeBl);
	     cardNoList.clear();
	     sqlCmd  = "  SELECT distinct a.card_no";
	     sqlCmd += "    FROM crd_card c, bil_bill a ";
	     sqlCmd += "    left join bil_contract b  on b.contract_no     = a.contract_no     ";
	     sqlCmd +=                             " and b.contract_seq_no = a.contract_seq_no ";
	     sqlCmd += "   WHERE  c.card_no       = a.card_no ";
	     sqlCmd += "     and  c.current_code  = '0' ";
	     sqlCmd += "     and (ecs_cus_mcht_no = '' or (ecs_cus_mcht_no <> '' and not exists   ";
             sqlCmd +=                " (select 1 from mkt_mcht_gp x  left join ";
             sqlCmd +=                "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id ";
             sqlCmd +=                "   where y.table_name    = 'MKT_MCHT_GP'  ";
             sqlCmd +=                "     and y.data_key     in ('MKTR00001') ";
             sqlCmd +=                "     and x.platform_flag = '2'  ";
             sqlCmd +=                "     and y.data_code     = a.ecs_cus_mcht_no ))) ";
	     sqlCmd += "     AND  a.acct_code in (decode(?,'Y','BL','XX'), ";
	     sqlCmd += "                          decode(?,'Y','IT','XX'), ";
	     sqlCmd += "                          decode(?,'Y','ID','XX'), ";
	     sqlCmd += "                          decode(?,'Y','CA','XX'), ";
	     sqlCmd += "                          decode(?,'Y','AO','XX'), ";
	     sqlCmd += "                          decode(?,'Y','OT','XX'))";
	 if (currCond.equals("Y"))
	     sqlCmd += "     AND  a.acct_month    between ? and ? ";
         else
	     sqlCmd += "     AND  a.purchase_date between ? and ? ";
if(DEBUG_DAT == 1) sqlCmd += "     AND  group_code = '1620' and card_type = 'VI' AND  a.card_no  in ('4258700000049110','4258700000049201') ";
	     sqlCmd += " order by 1 ";

		ppp(1, consumeBl); 
		ppp(2, consumeIt); 
		ppp(3, consumeId); 
		ppp(4, consumeCa); 
		ppp(5, consumeAo);
		ppp(6, consumeOt);
		if (currCond.equals("Y")) {
		    ppp(7, isBegYm);
		    ppp(8, isEndYm);
                    showLogMessage("I",""," OPEN CRD_CARD=["+sqlNrow+"] "+ isBegYm+","+isEndYm);
                   }
                else {
		    ppp(7, isBegDate_air);
		    ppp(8, isEndDate_air);
                    showLogMessage("I",""," OPEN CRD_CARD A=["+sqlNrow+"] "+ isBegDate_air+","+isEndDate_air);
                   }
		sqlSelect();
                showLogMessage("I",""," OPEN CRD_CARD END=["+sqlNrow+"]");
		if (sqlNrow <= 0) {
			return;
		}
		int selectRow = sqlNrow;

		for(int i=0;i<sqlNrow;i++) {
//  if(DEBUG_F==1)      System.out.println("card no="+colSs(i, "card_no"));
			cardNoList.add(colSs(i, "card_no"));
		}
	}
/**************************************************************************************/
	// 檢核並 讀取消費資料
	void selectBillAndContract() throws Exception {

	int freecnt = 0;// 加碼次
	int basecnt = 0;// 基本次數
	int cnt     = 0;
	int cntAmt  = 0;
	int allcnt  = 0;
	mchtCategoryTmp="";
        showLogMessage("I","","   All card cnt= ["+cardNoList.size()+"]");
	for(int i=0;i<cardNoList.size();i++) {
		//獲取卡號信息
		int cardIsExict=checkCardHldrFlag(cardNoList.get(i));

                if(i % 20000 == 0 || i == 0)
                  {
                   sqlCommit();
                   showLogMessage("I","",String.format("  Read bil_bill CARD Cnt=[%d] Bill=[%d]\n", i, allcnt));
                  }

if(DEBUG_F==1) showLogMessage("I","","****** Bill loop cnt=["+i+"] C="+ cardNo+",Current="+currentCode);
		if(cardIsExict==0) {
			continue;
		}
		// 獲取issueDate
		String issueDateMin = getCardIssueDate();
		// 為檢核做的準備 獲取開始時間
		java.util.Date beginDate = perpareForCheckIssuedata(issueDateMin);

if(DEBUG_F==1)	System.out.println("  Step 00 起始時間" + beginDate + " issue_date=" + issueDateMin + "截止時間=" + sysDate);

	     // 讀取該卡號的消費記錄 找出符合消费区间的记录
	     sqlCmd  = "  SELECT * ";
	     sqlCmd += "    FROM bil_bill a ";
	  // sqlCmd += "    left join bil_contract b  on b.contract_no     = a.contract_no     ";
	  // sqlCmd +=                             " and b.contract_seq_no = a.contract_seq_no ";
	     sqlCmd += "   WHERE  1=1  ";
	     sqlCmd += "     and (ecs_cus_mcht_no = '' or (ecs_cus_mcht_no <> '' and not exists   ";
             sqlCmd +=                " (select 1 from mkt_mcht_gp x  left join ";
             sqlCmd +=                "     mkt_mchtgp_data y ON y.data_key = x.mcht_group_id ";
             sqlCmd +=                "   where y.table_name    = 'MKT_MCHT_GP'  ";
             sqlCmd +=                "     and y.data_key     in ('MKTR00001') ";
             sqlCmd +=                "     and x.platform_flag = '2'  ";
             sqlCmd +=                "     and y.data_code     = a.ecs_cus_mcht_no ))) ";
		/**********************
		 * 消費資料 六大本金類
		 ******************************************************/
	     sqlCmd += "     AND  a.acct_code in (decode(?,'Y','BL','XX'), ";
	     sqlCmd += "                          decode(?,'Y','IT','XX'), ";
	     sqlCmd += "                          decode(?,'Y','ID','XX'), ";
	     sqlCmd += "                          decode(?,'Y','CA','XX'), ";
	     sqlCmd += "                          decode(?,'Y','AO','XX'), ";
	     sqlCmd += "                          decode(?,'Y','OT','XX'))";
		/**********************
		 * 消費資料 消費期間
		 ********************************************************/
	     if(currCond.equals("Y")) 
		sqlCmd += "   AND nvl(a.acct_month,'')     between ? and ? ";
	     else 
		sqlCmd += "   AND nvl(a.purchase_date,'')  between ? and ? ";
		sqlCmd += "   AND a.card_no in (select card_no from crd_card ";
		sqlCmd += "                      where current_code = '0' ";
		sqlCmd += "                        and card_type    = ?  ";
		sqlCmd += "                        and decode(group_code,'','0000',group_code) = ? ";
		if (consumeType.equals("1")) {
			sqlCmd += " and id_p_seqno = ?)";
		} else if (consumeType.equals("2")) {
			sqlCmd += " and (card_no =? or major_card_no = ?) )";
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			sqlCmd += " and card_no = ? )";
		}
		ppp(1,consumeBl); 
		ppp(2, consumeIt); 
		ppp(3, consumeId); 
		ppp(4, consumeCa); 
		ppp(5, consumeAo);
		ppp(6, consumeOt);
		if (currCond.equals("Y")) {
		    ppp(7, isBegYm);
		    ppp(8, isEndYm);
                   }
                else {
		    ppp(7, isBegDate_air);
		    ppp(8, isEndDate_air);
                   }
		ppp(9, cardType);
		ppp(10, groupCode);
	
		if (consumeType.equals("1")) {
			ppp(11, idPSeqno);
		} else if (consumeType.equals("2")) {
			ppp(11, cardNo);
			ppp(12, cardNo);
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			ppp(11, cardNo);
		}
// System.out.println(sqlCmd);
		sqlSelect();
	        allcnt = allcnt + sqlNrow;
if(DEBUG==1) showLogMessage("I","","    Bill select=["+sqlNrow+"]"+cardNo+" All="+allcnt + ",conType="+consumeType);
		if (sqlNrow <= 0) {// 查詢不到該卡號的消費記錄直接跳過
			continue;
		}
		tmpCount++;
		basecnt =  0;
		freecnt =  0;
	        maxBaseAmt = 0;
	        TotBaseAmt = 0;
		int selectRow = sqlNrow;
		// 检核是否為有效卡 若為有效卡則進入下一步
	     // isEffectivity = checkCrdCard(cardNo);
if(DEBUG_F==1) 	System.out.println("  Step 2 current_code=" + currentCode+","+cardNo);
		if (!currentCode.equalsIgnoreCase("0")) {// 若不是有效卡則寫入 db以及文檔
/*
	 	    insertCmsAirroptList("1", 0,0, idPSeqno, 0, 0,mchtCategoryTmp,1);// 寫入db 其中 金額、次數都為0
	    	txt1 = data1.allText();
		    lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", txt1));
*/
		continue;
		}
		// 比對cms_right_parm_detl與當前car_card的相關數據
		// 先檢核 符合條件在計算
if(DEBUG_F==1)  System.out.println("  Step 3 acctType=" + acctTypeFlag +" ,=" + acctType);
		if (acctTypeFlag.equals("Y")) {
			if (!checkDetl("01", acctType, "", "0")) {
				continue;
			}
		}

if(DEBUG_F==1)  System.out.println("  Step 4 groupType=" + groupCardFlag+" ,=" + groupCode +","+cardType);
		if (groupCardFlag.equals("Y")) {
			if (!checkDetl("02", groupCode, cardType, "0")) {
				continue;
			}
		}

                // debutYearFlag  1.新發卡 2.首辦卡 3.舊卡友
if(DEBUG_F==1) System.out.println("  Step 5 debutYearFlag=" + debutYearFlag+","+issueDateMin+","+beginDate);
		// debutGroupCond == "1"
		if ((debutYearFlag.equals("1") || debutYearFlag.equals("2"))) {
			// 檢核正副卡
			if ((debutSupFlag0.equals("Y") && supFlag.equals("0"))
					|| (debutSupFlag0.equals("Y") && supFlag.equals("1"))) {
if(DEBUG_F==1)	System.out.println("  符合條件");
			} else {
				continue;
			}

			// 檢核issue_date介於期間
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			if (format.parse(issueDateMin).compareTo(beginDate) < 0 || // 檢核issue_date
			    format.parse(issueDateMin).compareTo(format.parse(sysDate)) > 0) {
				continue;
			}

			if (debutGroupCond.equals("1")) {
				// 檢核 type =“03” in
				if (!checkDetl("03", groupCode, cardType, "0")) {
					continue;
				}
			} else if (debutGroupCond.equals("2")) {
				// 檢核 type =“03” not in
				if (!checkDetl("03", groupCode, cardType, "1")) {
					continue;
				}
			}

		} else if (debutYearFlag.equals("3")) {// 檢核核卡日非系統日當年
			int liIssueYy = ss2int(issueDateMin.substring(0, 4));
			int liSysYy = ss2int(sysDate.substring(0, 4));
			if (liIssueYy >= liSysYy) {
				continue;
			}
			if (lastCond.equals("Y")) {
			    freecnt += maxLastCnt;
			}
		}
		// 计算消费次数以及金額
		cnt = selectCnt(idPSeqno, cardPurchCode, cardType, groupCode);
		cntAmt = selectMktCardConsume(cardType, majorIdPSeqno, groupCode);

if(DEBUG_F==1) System.out.println("  Step 6 cnt=" + cnt +","+cntAmt+",currCond="+currCond+","+maxDestAmt+","+currMinAmt+",currCnt="+currCnt);
		if (cnt < 0) {// 若筆數小於0 直接下一筆
			continue;
		}
		// 當年消費門檻 直接查詢出當前符合條件的消費數據中金額最大的與單筆最低金額比較
		if (currCond.equals("Y")) {
			if (maxDestAmt >= currMinAmt && (currAmtCond.equals("Y") && cntAmt >= currAmt)
					|| (currTotCnt.equals("Y") && cnt >= currCntCond)) {
				basecnt += currCnt;
			}
		}
if(DEBUG_F==1) 	System.out.println("  Step 7 當年消費門檻currCond=" + currCond + ",Per="+condPer+" 累積金額  " + currAmtCond + " 累積刷卡次數 " + currTotCnt+",Base="+basecnt+","+currCnt);

		if (condPer.equals("Y")) {
//			if (!checkDetl("05", groupCode, null, "0")) {// 检核 cms detl
//				continue;
//			}
			if((cntAmt - currAmt) >= perAmt) {
				int perAmt1 = (int)((cntAmt - currAmt) /perAmt); 
				perCnt += perAmt1;
			}
		
if(DEBUG_F==1) 	System.out.println("  Step 8 currCond=" + currCond + "  condPer=" + condPer + " currAmtCond=" + currAmtCond + " currMinAmt=" + currMinAmt);
			if (currCond.equals("Y") && currAmtCond.equals("Y")) {
				// 加碼點數
				int rasiseAmt = cntAmt - currAmt;
if(DEBUG_F==1) 	System.out.println("rasiseAmt=" + rasiseAmt + "   perAmt=" + perAmt);
				if (rasiseAmt < perAmt) {
					freecnt = 0;
				} else if (rasiseAmt > perAmt) {
					freecnt = rasiseAmt / perAmt;
				} else if (rasiseAmt == perAmt) {
					freecnt = currCnt;
				}
			} else if (currCond.equals("Y") && currMinAmt > 0) {
				// 計算加碼點數
				int rasiseAmt = cntAmt - currMinAmt;
if(DEBUG_F==1) 	System.out.println("rasiseAmt=" + rasiseAmt);
				if (rasiseAmt < perAmt) {
					freecnt = 0;
				} else if (rasiseAmt > perAmt) {
					freecnt = rasiseAmt / perAmt;
				} else if (rasiseAmt == perAmt) {
					freecnt = currCnt;
				}
			}
if(DEBUG_F==1) 	System.out.println("  Step 9 加碼次數" + freecnt + " 消費次數cnt=" + cnt + " cntAmt=" + cntAmt);
		}

		int purchaseDateTmp = 0;
if(DEBUG_F==1) 	System.out.println("  Step 10 airSupFlag0=" + airSupFlag0 + " supFlag=" + supFlag + " airSupFlag1=" + airSupFlag1 + "cUseCond=" + cUseCond + "  bUseCond==" + bUseCond+",Cnt B="+selectRow);
		for (int ii = 0; ii < selectRow; ii++) {
			mchtCategory = "";
			purchaseDate = "";
			purchaseDateEnd  = null;
			purchaseDateTemp = null;
			startDate        = null;
			mchtCategory     = colSs(ii, "MCHT_CATEGORY");
                        mchtGroup        = getCmsMccGroup(mchtCategory);
	                referenceNo      = colSs(ii, "reference_no");
	  	        int tmpAmt       = colInt(ii, "dest_amt");
if(DEBUG_F==1) System.out.println("  消費REF=["+ii+"]"+referenceNo+",AMT="+tmpAmt);
		
			// 獲得當前消費記錄的 消費日期以及特店代號
			if (airCond.equals("Y")) {
				if (airSupFlag0.equals("Y") == false && airSupFlag1.equals("Y") == false) {
					break;
				}
				if (airSupFlag0.equals("Y") && airSupFlag1.equals("Y")) {
					startDate = checkIssueDate(cardNo);   
				} else {
					if (airSupFlag0.equals("Y")) {
						if (supFlag.equals("0")) {
							startDate = checkIssueDate(cardNo);
						} else {
							break;
						}
					} else if (airSupFlag1.equals("Y")) {
						if (supFlag.equals("1")) {
							startDate = checkIssueDate(cardNo);
						} else {
							break;
						}
					}

				}
				if (!getNumOfDatatype("06")) {
					continue;
				}
		              //if (!checkdetlMcc("06", mchtCategory, groupCode, cardType)) 
				if (!checkdetlMcc("06", mchtGroup   , groupCode, cardType)) {// 若不滿足 則PURCHASE_DATE=空之後將該值 寫入db中
if(DEBUG_F==1) System.out.println("當前消費信息找不到匹配的MCHT_CATEGORY =["+mchtGroup   +"]"+groupCode+","+cardType+",CATE="+mchtCategory+","+ referenceNo+","+tmpAmt);
			                purchaseDate = "";
					continue;
				} else {// cms 表中有匹配的MCHT_CATEGORY
					purchaseDate = colSs(ii, "purchase_date");
if(DEBUG_F==1) System.out.println("當前消費信息找到匹配的MCHT_CATEGORY =["+mchtGroup   +"]"+groupCode+","+cardType+",PUR="+purchaseDate+","+startDate+","+purchaseDateEnd+",CATE="+mchtCategory+","+tmpAmt);
				// 檢核purchaseDate 若purchaseDate 不在此區間內，則繼續下一筆消費記錄
					if (!checkPurchaseDate(startDate, purchaseDateEnd, purchaseDate)) {
					//	purchaseDate = "";
						purchaseDateTmp = Integer.parseInt(purchaseDate);
						continue;
					}

				//	freecnt += getMcccCodeCnt("06");// 計算mcc 筆數以及金額
					freecnt  = getMcccCodeCnt("06");// 計算mcc 筆數以及金額
					// 獲取符合條件 的最晚purchase_date以及对应的mchtCategory
					if (purchaseDateTmp < Integer.parseInt(purchaseDate)) {
						purchaseDateTmp = Integer.parseInt(purchaseDate);
						mchtCategoryTmp=mchtCategory;
					}
				}

			}
			// 同上
			if (bUseCond.equals("Y")) {
				if (!checkdetlMcc("07", mchtCategory, groupCode, cardType)) {// 检核 cms detl
					continue;
				}
				freecnt += getMcccCodeCnt("07");// 計算mcc 筆數以及金額
			}

			if (cUseCond.equals("Y")) {
				if (!checkdetlMcc("08", mchtCategory, groupCode, cardType)) {// 检核 cms detl
					continue;
				}
				freecnt += getMcccCodeCnt("08");// 計算mcc 筆數以及金額
			}
	                TotBaseAmt = TotBaseAmt + tmpAmt;
                        if(tmpAmt > maxBaseAmt)
                          { maxBaseAmt = tmpAmt;
                          }
if(DEBUG_F==1) showLogMessage("I","","   tmpAmt="+tmpAmt+" Max="+maxBaseAmt+" Tot="+TotBaseAmt);

		} // for循環 end

		// 若purchaseDateTmp 大於0 則表示 有值符合MCHT_CATEGORY 需獲取最大purchase_date
		if (purchaseDateTmp > 0) {
			purchaseDate = Integer.toString(purchaseDateTmp);
		}
if(DEBUG_F==1) showLogMessage("I","","   purchaseDateTmp=["+purchaseDateTmp+"]"+ purchaseDate+","+freecnt+","+basecnt+","+maxBaseAmt+",Tot="+TotBaseAmt);
//System.out.println(freecnt + "    " + basecnt);
		// 若次數為0則
		if (freecnt + basecnt < 1) {
			continue;
		}

		if (consumeType.equals("0")) { // 不需再檢核消費金額 且消費次數值(consume_00_cnt)>=1 且消費筆數>1
			if (consume00Cnt >= 1 && cnt > 0) {// 插入
				insertCmsAirroptList("0", freecnt, basecnt, idPSeqno, cntAmt, cnt,mchtCategoryTmp,2);// 寫入db

				// 根據outType的值 選擇寫入的文檔 lpar1對應TCBINFINITECARD.txt lpar2對應TCBINFINITECARD10.txt
				// 等程序運行完之後 在將lpar1以及lpar2 寫入相應文檔 -》line 174 到181
				if (data1.outType.equals("A")) {
				    txt1 = data1.allText();
				    lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", txt1));
				}
		          //    if (freecnt > 0)  
		                if (basecnt > 0) {
				    txt2 = data2.allText();
				    lpar2.add(comcr.putReport("", "", sysDate, ++rptSeq2, "0", txt2));
				}
//				if (data1.outType.equals("P")) {
//				    data1.outType = "A";
//				    txt1 = data1.allText();
//				    lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", txt1));
		         if (data1.outType.equals("C")) {
//	                    if (basecnt > 0) {
			        txt2 = data2.allText();
			        lpar2.add(comcr.putReport("","",sysDate,++rptSeq2, "0", txt2));
//                             }
		         }
		                 // if (freecnt > 0) {

//				}

			}

		}
/*
lai mark  && add freecnt
freecnt = 10; basecnt = 5;
*/

if(DEBUG_F==1) 	System.out.println("  Step 33 要寫入db了=" + consumeType+ ","+consumeBl+",Free="+freecnt+",basecnt="+basecnt+",OTYPE="+data1.outType+",MM=["+purchaseDate+"]");

		if ((consumeType.equals("1") || consumeType.equals("2") || consumeType.equals("3")) &&
		    (consumeBl.equals("Y")   || consumeCa.equals("Y")   || consumeIt.equals("Y") || 
		     consumeAo.equals("Y")   || consumeId.equals("Y")   || consumeOt.equals("Y")) ) {
		    if (freecnt + basecnt >= 1 && cntAmt >= currAmt) {// 寫入db以及文檔
	                insertCmsAirroptList("0", freecnt,basecnt,  idPSeqno, cntAmt, cnt,mchtCategoryTmp,3);
		 	String txt0 = data1.allText();
/* lai test
data1.outclass     = "A,";
data2.outclass     = "A,";
data1.outType      = "A,";
*/

                        // 優惠別 A:基本 // C:加碼   (
 	                if (comc.getSubString(data1.outType,0,1).equals("A")) { 
		 	    txt1 = data1.allText();
			    lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", txt1));
			}
 	                if (comc.getSubString(data1.outType,0,1).equals("C")) { 
		          //if(purchaseDate != null and purchaseDate.compareTo("") <> 0) {
		            if(purchaseDate.length() > 0) {
		               txt2 = data2.allText();
			       lpar2.add(comcr.putReport("", "", sysDate, ++rptSeq2, "0", txt2));
                              }
			}
 	                if (comc.getSubString(data1.outType,0,1).equals("P")) { 
			    data1.outType = "A,";
			    txt1 = data1.allText();
			    lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", txt1));

			    data2.outType = "C,";
			    txt2 = data2.allText();
			    lpar2.add(comcr.putReport("", "", sysDate, ++rptSeq2, "0", txt2));
			}
		    }
		}
	}   // end loop fetch
}
/**************************************************************************/
	// 檢核行員
	int checkCardHldrFlag(String cardNoTmp) throws Exception {
		idNo = "";
		isIssueDate = "";
		supFlag = "";
		engName = "";
		cardNo = "";
		majorCardNo = "";
		acctType = "";
		groupCode = "";
		cardType = "";
		idPSeqno = "";
		majorIdPSeqno = "";
		cardPurchCode = "";
		currentCode = "";
		if (cardHldrFlagTmp.equals("2") || cardHldrFlagTmp.equals("3")) {
	            sqlCmd = "SELECT * FROM CRD_CARD a " + "LEFT JOIN CRD_IDNO b ON a.id_p_seqno=b.id_p_seqno " + " WHERE a.card_no=?";
			ppp(1, cardNoTmp);
			sqlSelect();
			if (sqlNrow <= 0) {
				return 0;
			}
			currentCode = colSs("current_code");
			acctType = colSs("acct_type");
			groupCode = colSs("group_code");
			cardType = colSs("card_type");
			idPSeqno = colSs("ID_P_SEQNO");
			majorIdPSeqno = colSs("MAJOR_ID_P_SEQNO");
			cardPurchCode = colSs("card_purch_code");
			idNo = colSs("id_no");
			isIssueDate = colSs("issue_date");
			supFlag = colSs("sup_flag");
			engName = colSs("eng_name");
			cardNo = colSs("CARD_NO");
			majorCardNo = colSs("MAJOR_CARD_NO");
			return 1;
		}
		// 若是行員 则首先检核行员状态
		if (cardHldrFlagTmp.equals("1")) {
		    sqlCmd = "SELECT * FROM CRD_CARD a "
                           + "  LEFT JOIN CRD_IDNO     b ON a.id_p_seqno=b.id_p_seqno "
			   + "  LEFT JOIN CRD_EMPLOYEE c ON b.ID_NO=c.id AND status_id in ('1','7') " 
                           + " WHERE a.card_no    = ? " 
                           + "   AND b.STAFF_FLAG = 'Y' ";
			ppp(1, cardNoTmp);
			sqlSelect();
			if (sqlNrow <= 0) {
				printf("檢核是否是行員 --- error");
				return 0;
			}
			currentCode = colSs("current_code");
			cardPurchCode = colSs("card_purch_code");
			acctType = colSs("acct_type");
			groupCode = colSs("group_code");
			cardType = colSs("card_type");
			idPSeqno = colSs("ID_P_SEQNO");
			majorIdPSeqno = colSs("MAJOR_ID_P_SEQNO");//
			isIssueDate = colSs("issue_date");
			idNo = colSs("id_no");
			supFlag = colSs("sup_flag");
			engName = colSs("eng_name");
			cardNo = colSs("CARD_NO");
			majorCardNo = colSs("MAJOR_CARD_NO");
			return 1;
		}
		return 0;
	}

	// 獲取 acct month的範圍
	void getAcctMonth() throws Exception {
                showLogMessage("I","","  getAcctMonth 0= ["+hBusiBusinessDate+"] "+ currPreMonth+",currCond="+currCond+","+chooseCond);
		sqlCmd = " select to_char(add_months(to_date(?,'yyyymmdd'),0),'yyyymm')    is_end_ym ";
		sqlCmd += "      ,to_char(add_months(to_date(?,'yyyymmdd'),?),'yyyymm')    is_beg_ym ";
                sqlCmd += "      ,to_char(add_months(to_date(?,'yyyymmdd'),?),'yyyymm')    is_last_ym ";
                sqlCmd += "      ,to_char(to_date(?,'yyyymmdd')+cast(? as int),'yyyymmdd') is_air_day ";
		sqlCmd += "   from dual ";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		if (currPreMonth > 0) {
			setInt(3, 1 - currPreMonth);
		} else {
			setInt(3, 0);
		}
		setString(4, hBusiBusinessDate);
	        setInt(5, 0 - lastMm);
                setString(6, hBusiBusinessDate);
                setInt(7, 0 - (Integer.parseInt(airDay)));  
		
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("add_month error", "", comcr.hCallBatchSeqno);
		}
		if (recordCnt > 0) {
		    isEndYm = getValue("is_end_ym");
		    isBegYm = getValue("is_beg_ym");
		}
		if (currPreMonth > 0) {
                    String busiPreYear  = commDate.dateAdd(hBusiBusinessDate, -1, 0, 0).substring(0, 4);
		    if(currPreMonth < 10) 
		       isBegYm = busiPreYear + "0" + currPreMonth;
                    else 
		       isBegYm = busiPreYear + currPreMonth;
		}   
                else  // 備註: 如果curr_pre_month=0 , is_beg_ym=今年1月(非去年N月)
                {  
                    isBegYm = hBusiBusinessDate.substring(0, 6) + "01";
                }  
                showLogMessage("I","","  getAcctMonth 1="+isBegYm+","+isEndYm);

		if(currCond.equals("Y")) {
                   switch (chooseCond)
                      {
                       case "1": chooseCondRtn();
                                 break;
                       case "2": isBegYm = hBusinessPrevYear + getValue("is_last_ym");
                                 isEndYm = hBusinessPrevMonth;
                                 break;
                      }
		}
//  air_cond='Y' ,則business_date-N天(air_day的值=200天), =20230601-201天
		if(airCond.equals("Y")) {
                   isBegDate_air = getValue("is_air_day");
                   isEndDate_air = commDate.dateAdd(hBusiBusinessDate,  0, 0, -1);
		}
                else
		{
                   isBegDate_air = isEndYm + "31";
                   isEndDate_air = isEndYm + "31";
		}
         showLogMessage("I","","  getAcctMonth end= ["+airCond+"] "+ getValue("is_air_day")+",YM="+isBegYm+","+isEndYm + ",AIR="+airDay+", isBegDate_air="+isBegDate_air+","+isEndDate_air);
	}
/*********************************************************/
void  chooseCondRtn()  throws Exception 
{
   isBegYm = hBusinessPrevYear + "01";
   if(currPreMonth < 10 && currPreMonth > 0)  isBegYm = hBusinessPrevYear + "0" + currPreMonth;  
   if(currPreMonth > 9)  isBegYm = hBusinessPrevYear + currPreMonth;  

   isEndYm = hBusiBusinessDate.substring(0,4) + "12";
   showLogMessage("I","","  chooseCondRtn= ["+currPreMonth+"]"+isBegYm+","+isEndYm);
}
/*********************************************************/
// 為檢核issueDate做準備
	java.util.Date perpareForCheckIssuedata(String issueDateMin) throws Exception {
		java.util.Date beginDate = null;

		// 數據中存在 issue_date 為空的情況 此處暫時賦一個值
		if (issueDateMin.equals("")) {
			issueDateMin = "19970616";
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		java.util.Date issueDateTmp = format.parse(issueDateMin);
if(DEBUG_F==1) System.out.println(debutMonth1+", " + debutMonth2 + " debutYearFlag=" + debutYearFlag);
		if (debutYearFlag.equals("1")) {
			java.util.Date date = format.parse(sysDate);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.MONTH, -ss2int(debutMonth1));
			beginDate = c.getTime();
		} else if (debutYearFlag.equals("2")) {
			java.util.Date date = format.parse(issueDateMin);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.YEAR, -1);
			c.add(Calendar.MONTH, ss2int(debutMonth2));
			beginDate = c.getTime();
		}
		return beginDate;
	}
/*************************************************************************************/
	void   getCardData(String cardNo) throws Exception {
	        hBinType        = ""; hMajorIdPSeqno  = ""; hIdNo           = "";
	        hChiName        = ""; hMajorId        = ""; hOldCardNo      = "";

                extendField = "card.";
		sqlCmd  = " select a.bin_type, a.major_id_p_seqno, a.old_card_no  ";
		sqlCmd += "      , b.id_no   , b.chi_name ";
		sqlCmd += "      , c.id_no   as major_id   ";
		sqlCmd += "      from crd_idno c, crd_idno b, crd_card a ";
		sqlCmd += "     where b.id_p_seqno = a.id_p_seqno ";
		sqlCmd += "       and c.id_p_seqno = a.major_id_p_seqno  ";
		sqlCmd += "       and a.card_no    = ? ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		hBinType       = getValue("card.bin_type");
	        hOldCardNo     = getValue("card.old_card_no");
		hMajorIdPSeqno = getValue("card.major_id_p_seqno");
		hIdNo          = getValue("card.id_no");
		hChiName       = getValue("card.card.chi_name");
		hMajorId       = getValue("card.major_id");

		return;
	}
/*************************************************************************************/
	// 獲得卡號最早發卡日期
	String getCardIssueDate() throws Exception {
		sqlCmd  = " select min(issue_date) h_crdp_issue_date ";
		sqlCmd += "      from crd_card ";
		sqlCmd += "     where id_p_seqno = ? ";
		sqlCmd += "       and card_type = ? ";
		sqlCmd += "       and decode(group_code, '', '0000', group_code) = ? ";
		setString(1, idPSeqno);
		setString(2, cardType);
		setString(3, groupCode);
		int recordCnt = selectTable();
		return getValue("h_crdp_issue_date");
	}
/*********************************************************************************/
	String getCmsMccGroup(String mccCode) throws Exception {
		sqlCmd  = " select mcc_group ";
		sqlCmd += "   from cms_mcc_group ";
		sqlCmd += "  where mcc_code   = ? ";
		setString(1, mccCode);
		int recordCnt = selectTable();
		String tMccGroup = getValue("mcc_group");
if(DEBUG_F==1) 	System.out.println("   getCmsMccGroup=" + mccCode + " mcc_group=" + tMccGroup);
		return getValue("mcc_group");
	}
/*************************************************************************/
	// 获取当前卡號的issue_date 加上air_day 之後 返回日期 以供之後的purchaseDate檢核
	java.util.Date checkIssueDate(String cardNo) throws Exception {
		sqlCmd  = " select issue_date h_crdp_issue_date ";
		sqlCmd += "      from crd_card ";
		sqlCmd += "     where card_no = ? ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		// 要判空
	//	java.util.Date issueDateTmp = format.parse(getValue("h_crdp_issue_date"));
		java.util.Date issueDateTmp = format.parse(hBusinessPrevMonthEnd);
		Calendar c = Calendar.getInstance();
		c.setTime(issueDateTmp);
if(DEBUG_F==1) showLogMessage("I","","   checkIssueDate= ["+cardNo+"] "+ hBusinessPrevMonthEnd+","+airDay+","+issueDateTmp);
              //c.add(Calendar.DATE, ss2int(airDay));
		c.add(Calendar.DATE, -ss2int(airDay));
		purchaseDateEnd = c.getTime();  // 前推
		purchaseDateTemp= purchaseDateEnd; 
		startDate       = purchaseDateTemp; 
		purchaseDateEnd = issueDateTmp;

	      //return issueDateTmp;
	        return purchaseDateTemp;
	}

	// 檢核purchaseDate
	Boolean checkPurchaseDate(java.util.Date startDate, java.util.Date purchaseDateEnd2, String purchaseDate)
			throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
if(DEBUG_F==1)	System.out.println("startDate=" + startDate + "  purchaseDate=" + purchaseDate + "  endDate=" + purchaseDateEnd2);
		if (format.parse(purchaseDate).compareTo(startDate) < 0 || 
                    format.parse(purchaseDate).compareTo(purchaseDateEnd2) > 0) {
if(DEBUG_F==1)	    System.out.println("当前purchaseDate chk 不通过时间检核");
			return false;
		}
if(DEBUG_F==1)	System.out.println("startDate= END");

		return true;
	}
/******************************************************************************************/
int outPutTextFile() throws Exception {
        String hNcccFilename = rptName1;         // A:基本
	showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");
	outFileName = String.format("%s/media/cms/%s", comc.getECSHOME(), hNcccFilename);
	outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
	showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

	String hNcccFilename1 = rptName2;        // C:加碼
	showLogMessage("I", "", "Output Filename = [" + hNcccFilename1 + "]");
	outFileName2 = String.format("%s/media/cms/%s", comc.getECSHOME(), hNcccFilename1);
	outFileName2 = Normalizer.normalize(outFileName2, java.text.Normalizer.Form.NFKD);
	showLogMessage("I", "", "Output Filepath = [" + outFileName2 + "]");

	return 0;
}
/***********************************************************************/
private void ftpRtn(String hFileNameI) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";
    String hOwsWfValue3 = "";

    sqlCmd  = "select wf_value2 ";
    sqlCmd += " from ptr_sys_parm  ";
    sqlCmd += "where wf_parm = 'SYSPARM'  ";
    sqlCmd += "  and wf_key  = 'CITY_PARK_ZIP_PWD' ";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_sys_parm not found!", "CITY_PARK_ZIP_PWD", comcr.hCallBatchSeqno);
    }
    hOwsWfValue3 = getValue("wf_value2");

    /*** PKZIP 壓縮 ***/
    temstr1 = String.format("%s/media/cms/%s",comc.getECSHOME(), hFileNameI);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    String filename = temstr1;
/*
    temstr2 = String.format("%s.zip",temstr1);
    String hPasswd  = hOwsWfValue3;
    String zipFile  = temstr2;
    int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
    if(tmpInt != 0) {
       comcr.errRtn(String.format("無法壓縮檔案[%s]", filename),"", hCallBatchSeqno);
    }
    comc.chmod777(zipFile);
    showLogMessage("I", "", "** PKZIP= [" + filename + "]"+ zipFile+","+hPasswd);
*/

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

    String  hEflgRefIpCode  = "NCR2TCB";
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgSystemId   = "NCR2TCB";
    commFTP.hEflgGroupId    = "0000";
    commFTP.hEflgSourceFrom = "EcsFtpBil";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/cms", comc.getECSHOME());
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
//  filename  = String.format("%s.zip", hFileNameI);
    filename  = String.format("%s"    , hFileNameI);
    procCode  = String.format("put %s", filename);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(filename);
    }
}
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/cms/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/cms/backup/%s_%s",comc.getECSHOME(),filename,sysDate);

   if (comc.fileRename(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }

   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
}
/******************************************************************************************/
	int getMcccCodeCnt(String dataTypeTmp) throws Exception {

		if (!airCond.equals("Y")) {
			return 0;
		}

		sqlCmd = "  SELECT  count(*) db_cnt, ";
		sqlCmd += "          sum(decode(a.acct_code,'IT', ";
		sqlCmd += "                decode(?,'2', ";
		sqlCmd += "                 decode(a.install_curr_term,1, ";
		sqlCmd += "                   decode(b.refund_apr_flag,'Y',0,b.tot_amt), ";
		sqlCmd += "                   0), ";
		sqlCmd += "                 decode(b.refund_apr_flag,'Y',0,a.dest_amt)), ";
		sqlCmd += "  case when a.txn_code in ('06','25','27','28','29') ";
		sqlCmd += "  then a.dest_amt*-1 else a.dest_amt end)) db_amt,max(a.DEST_AMT) max_dest_amt ";
		sqlCmd += "  FROM  bil_bill a left join bil_contract b ";

		sqlCmd +=                       "    on  b.contract_no     = a.contract_no   ";
		sqlCmd +=                       "   and  b.contract_seq_no = a.contract_seq_no ";
		sqlCmd += " WHERE  1=1 "; 
		/**********************
		 * 消費資料 六大本金類
		 ******************************************************/
		sqlCmd += "   AND  decode(a.acct_code,'','  ',a.acct_code) in (decode(?,'Y','BL','XX'), ";
		sqlCmd += "                                  decode(?,'Y','IT','XX'), ";
		sqlCmd += "                                  decode(?,'Y','ID','XX'), ";
		sqlCmd += "                                  decode(?,'Y','CA','XX'), ";
		sqlCmd += "                                  decode(?,'Y','AO','XX'), ";
		sqlCmd += "                                  decode(?,'Y','OT','XX')) ";
		/**********************
		 * 特殊消費 MCC-Code
		 ********************************************************/
		sqlCmd += "   and exists (select data_code3 ";
		sqlCmd += "  from cms_right_parm_detl t ";
		sqlCmd += " where t.table_id   = upper('RIGHT') ";
		sqlCmd += "   and t.data_code  = ?  ";
		sqlCmd += "   and t.data_code2 = ?  ";
		sqlCmd += "   and t.data_type  = ? ";
		sqlCmd += "   and t.data_code3 in (select v.mcc_group from cms_mcc_group v where v.mcc_code = a.mcht_category)) ";
		/**********************
		 * 消費資料 消費期間
		 ********************************************************/
		sqlCmd += " AND acct_month between ? and ? ";
		sqlCmd += " AND a.card_no in (select card_no from crd_card ";
		sqlCmd += "                    where card_type =?  ";
		sqlCmd += "                      and decode(group_code, '', '0000', group_code) =? ";
		if (consumeType.equals("1")) {
			sqlCmd += " and id_p_seqno =?)";
		} else if (consumeType.equals("2")) {
			sqlCmd += " and (card_no =? or major_card_no =?) )";
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			sqlCmd += " and card_no =? )";
		}
		int idx_f = 1;
		setString(idx_f++, it1Type);
		setString(idx_f++, consumeBl);
		setString(idx_f++, consumeIt);
		setString(idx_f++, consumeId);
		setString(idx_f++, consumeCa);
		setString(idx_f++, consumeAo);
		setString(idx_f++, consumeOt);
		setString(idx_f++, groupCode);
		setString(idx_f++, cardType);
	        setString(idx_f++, dataTypeTmp);
		setString(idx_f++, isBegYm);
		setString(idx_f++, isEndYm);
		setString(idx_f++, cardType);
		setString(idx_f++, groupCode);
if(DEBUG_F==1) showLogMessage("I", "", "     getMcccCodeCnt=["+dataTypeTmp+ "]"+isBegYm+","+isEndYm+","+cardType+","+groupCode+","+cardNo);
		if (consumeType.equals("1")) {
			setString(idx_f++, idPSeqno);
		} else if (consumeType.equals("2")) {
			setString(idx_f++, cardNo);
			setString(idx_f++, cardNo);
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			setString(idx_f++, cardNo);
		}
		if (selectTable() > 0) {
			int specCnt = getValueInt("db_cnt");
			int specAmt = getValueInt("db_amt");
			int specDestAmt = getValueInt("max_dest_amt");
if(DEBUG_F==1)	System.out.println("mcc cnt=" +  airAmtType+ " spec=" + specAmt + " specDestAmt=" + specDestAmt+","+airAmt+",airCnt="+airCnt);
			if (airAmtType.equals("1") && specDestAmt >= airAmt) {
				return airCnt;
			}
			if (airAmtType.equals("2") && specAmt >= airAmt) {
				return airCnt;
			}

		}
		return 0;

	}

/****************************************************************************/
void deleteCmsAirportList() throws Exception {
	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	Calendar c = Calendar.getInstance();
	c.add(Calendar.MONTH, -1);
	c.add(Calendar.YEAR, -2);
	java.util.Date y = c.getTime();
	String yearmonth = format.format(y);//
	sqlCmd = " delete cms_airport_list " + " where CRT_DATE <=? ";
	ppp(1, yearmonth);
        showLogMessage("I", "", "======= DELETE AIR= [" + yearmonth + "]");
	sqlExec(sqlCmd);

        sqlCmd = " delete cms_airport_list  where acct_month like ? || '%' and item_no = '08' ";
     // ppp(1, sysDate.substring(0, 6));
        ppp(1, hBusinessPrevMonth);
        showLogMessage("I", "", "======= DELETE AIR curr = [" + hBusinessPrevMonth + "]");
        sqlExec(sqlCmd);
}
/****************************************************************************/
void insertCmsAirroptList(String currentCodels, int raiseCnt, int raiseCnt1, String idPseqNo, int totAmt, int totCnt,String dataCode3Tmp, int idx) throws Exception {
	if (currCond.equals("Y") ) {
		if(totAmt < currAmt) {
			return;
		}
	}
	
	if (airCond.equals("Y") ) {
		if(totAmt < airAmt) {
			return;
		}
	}

	int perAmt1 = 0;
	if((totAmt - currAmt) >= perAmt) {
		perAmt1 = (int)((totAmt - currAmt) /perAmt); 
	}
	        getCardData(cardNo);

		int freeCnt = 0;
		int freeCntBasic = 0;
		daoTable = "cms_airport_list";

        //      extendField = daoTable + ".";
        //      setValue(extendField + "acct_month"      , hBusinessPrevMonth);

		setValue("CRT_DATE", sysDate);
		setValue("cal_ym"  , sysDate.substring(0, 4));// 填入系統年
		if (currentCodels.equals("0")) {// 通过CurrentCodels 来分辨使用方法的是有效卡还是无效卡
			setValue("CHANGE_CLASS", "A");
			// 首先根據參數情況確定好 free_cnt_basic以及FREE_CNT_raise 的值
			// 因為preferential的值需要 FREE_CNT_raise以及free_cnt_basic來確定

			if (consumeType.equals("0")) {
				setValueInt("free_cnt_basic", consume00Cnt + perAmt1 );
				setValueInt("FREE_TOT_CNT", raiseCnt + consume00Cnt);
				// System.out.println("free_cnt_basic:"+consume00Cnt);
				freeCnt = raiseCnt + consume00Cnt;
				freeCntBasic = consume00Cnt;
			}
			setValueInt("FREE_TOT_CNT", raiseCnt + currCnt);
			if (currCond.equals("Y")) {
				setValueInt("free_cnt_basic", currCnt + perAmt1);
				freeCnt = raiseCnt + currCnt;
				freeCntBasic = currCnt;
			}
			if (airCond.equals("Y")) {
				setValueInt("free_cnt_raise", airCnt);
			}
			if (lastCond.equals("Y") || condPerls.equals("Y")) {
				setValueInt("FREE_CNT_raise", raiseCnt); // 通過計算
			}
//    	System.out.println(freeCnt+"=freeCnts"+" raiseCnt==="+raiseCnt);
//  	System.out.println(lastCond+"  "+condPerls);
	// lai          setValueInt("curr_max_amt", maxDestAmt);// maxDestAmt
	//              setValueInt("tot_amt"     , totAmt);    // MKT_CARD_CONSUME 的sum金額
			setValueInt("curr_max_amt", maxBaseAmt);// maxDestAmt
	//              setValueInt("tot_amt"     , TotBaseAmt);// maxDestAmt
			setValueInt("tot_amt"     , maxBaseAmt);// maxDestAmt
		        setValueInt("curr_tot_cnt", totCnt);

if(DEBUG_F==1) showLogMessage("I","","  INSERT LIST=["+idx+"]C="+cardNo+","+freeCntBasic+","+raiseCnt);

			// preferential  剛好相反, raiseCnt 是基本(raiseCnt > 0)--優惠別 A:基本 C:加碼  
//			if (freeCntBasic >= 0 || raiseCnt >= 0) {
		              //if (freeCntBasic >= 0 && raiseCnt <= 0) {
				if (freeCntBasic >= 0) {
					setValue("preferential", "C");
					data1.outType = "C,";
					data2.outType = "C,";
				}
//				if (freeCntBasic < 0 && raiseCnt > 0) {
				if (raiseCnt > 0) {
					setValue("preferential", "A");
					data1.outType = "A,";
					data2.outType = "A,";
				}
//				if (freeCntBasic > 0 && raiseCnt > 0) {
//					setValue("preferential", "P");
//					data1.outType = "P";// 此處先賦值為p 到了要寫入文檔時 在根據 outType的值以及文檔类型 重新賦值 A 或者C
//					data2.outType = "P";
//				}
				if(currCond.equals("Y")&&airCond.equals("Y")) {
					if (freeCntBasic > 0 && raiseCnt >= 0) {
						setValue("preferential", "C");
						data1.outType = "C,";
						data2.outType = "C,";
					}
				}		
//			}

			data1.outclass = "A,";// 插入數據時順便將下一步寫入文檔所需數據 完善一部分
			data2.outclass = "A,";
			int   freeCnt_tmp = 0;
			if(freeCntBasic >  0 && raiseCnt <= 0)  freeCnt_tmp = freeCntBasic;
			if(freeCntBasic <  0 && raiseCnt >  0)  freeCnt_tmp = raiseCnt;
			// freeCnt_tmp = freeCnt - raiseCnt;
	              //data1.outFreeCount = String.format("%-4.4s,",String.format("%4d", freeCnt_tmp));
			data2.outFreeCount = String.format("%-4.4s,",String.format("%04d", currCnt));
			data1.outFreeCount = String.format("%-4.4s,",String.format("%04d", airCnt));
                      // 當年消費門檻: 次數 curr_cnt(基本), 06.團體代號+MCC CODE:次數:air_cnt(加碼)
                      // write file 相反
		} else {// CurrentCode <>0
			setValue("CHANGE_CLASS"     , "A");
			setValue("preferential"     , "A");

			// CURRENT_CODE<>'0' 次數 金額為0
			setValueInt("free_cnt_basic", 0);
			setValueInt("FREE_tot_CNT"  , 0);
			setValueInt("FREE_CNT_raise", 0);
			setValueInt("curr_max_amt"  , 0);

			data1.outclass     = "A, ";
			data1.outType      = "A, ";
			data1.outFreeCount = "   0, ";
		}

		data1.filler08  = "********,";
		data2.filler08  = "********,";
		data1.outCardNo = cardNo.substring(cardNo.length() - 8) + ", ";
		data2.outCardNo = cardNo.substring(cardNo.length() - 8) + ", ";
		data1.outlastCardNo = "        , ";
		data2.outlastCardNo = "        , ";
		if(hOldCardNo.length() > 8) {
		   data1.outlastCardNo = hOldCardNo.substring(hOldCardNo.length() - 8) + ", ";
		   data2.outlastCardNo = hOldCardNo.substring(hOldCardNo.length() - 8) + ", ";
		}
		data1.outName   = engName;
		data2.outName   = engName;

		if (purchaseDate != null) {// 若BIL_BILL.PURCHASE_DATE的值非空白填入MM,否則不填
			data1.outTransMM = "  ";
			data1.outTransDD = "  ,";
			data2.outTransMM = "  ";
			data2.outTransDD = "  ,";
		        if (purchaseDate.length() > 7) {
			    data1.outTransMM = purchaseDate.substring(4,6);
			    data1.outTransDD = purchaseDate.substring(6,8);
			    data1.filler01   = ",";
			    data2.outTransMM = purchaseDate.substring(4,6);
			    data2.outTransDD = purchaseDate.substring(6,8);
			    data2.filler01   = ",";
                        }
		}

		setValue("ITEM_NO"     , "08");
		setValue("CARD_NO"     , cardNo);
		setValue("LAST_CARD_NO", majorCardNo);
        //      if (airCond.equals("Y") && dataType.equals("06") && dataCode3Tmp != null) 
  	        setValue("PURCHASE_DATE", purchaseDate); 

		setValue("eng_name"       , engName); // CRD_IDNO.ENG_NAME
		setValue("id_p_seqno"     , idPseqNo);// CRD_CARD.ID_P_SEQ

		setValue("proj_code"      , projCode);
		setValue("mod_time"       , sysDate + sysTime);
		setValue("mod_pgm"        , "CmsC003");
		setValue("mod_type"        , "A"           );
		setValue("data_from"       , "2");
                setValue("acct_month"      , hBusinessPrevMonth);
                setValue("curr_month"      , hBusinessPrevMonth);
                setValue("use_month"       , hBusinessUseMonth);
		setValue("send_date"       , hBusiBusinessDate);
		setValue("current_code"    , currentCode);
		setValue("consume_type"    , consumeType);
		setValue("old_card_no"     , hOldCardNo    );
		setValue("chi_name"        , hChiName      );
		setValue("major_id"        , hMajorId      );
		setValue("major_id_p_seqno", hMajorIdPSeqno);
		setValue("bin_type"        , hBinType      );

             // setValue("id_no"           , hIdNo         );

                tmpCount1++;
		insertTable();
		if (dupRecord.equals("Y")) {
		    comcr.errRtn("    **insert_cms_airport_list duplicate !", "", cardNo +","+projCode);
		}
if(DEBUG_F==1) System.out.println("  插入數據庫成功 ");
	}

	// 檢核 groupCode/card type
	Boolean checkDetl(String code1, String code2, String code3, String isequal) throws Exception {
		int count0 =0;
		int count1 = 0;
		Iterator it=parmDetlList.iterator();
		while(it.hasNext()) {
			ParmDetl parmDetlTmp=(ParmDetl) it.next();
			if(parmDetlTmp.getDataTypeTmp().equals(code1)) {
				count0++;
			}
		}		
if(DEBUG_F==1)	System.out.println("當前detl中dataType="+code1+"的有"+count0+"條");
		if (count0 <= 0) {
			return true;
		}
		for(int i=0;i<parmDetlList.size();i++) {
			ParmDetl parmDetlTmp1=parmDetlList.get(i);
			if (code1.length() > 0) {
				if(!code1.equals(parmDetlTmp1.getDataTypeTmp())) {
					continue;
				}
			}
			if (isequal.equals("0")) {
				if (code3 != null) {
					if(code2.equals(parmDetlTmp1.getDataCodeTmp())&&code3.equals(parmDetlTmp1.getDataCode2Tmp())) {
						count1++;
					}
				} else {
					if(code2.equals(parmDetlTmp1.getDataCodeTmp())) {
						count1++;
					}
				}
			} else {
				if(!code2.equals(parmDetlTmp1.getDataCodeTmp())||!code3.equals(parmDetlTmp1.getDataCode2Tmp())) {
					count1++;
				}
			}
		}	
if(DEBUG_F==1)	System.out.println("dataType"+code1+" code="+code2+" code2="+code3+" 符合的有"+count1+"條");
		if (count1 <= 0) {// 不符合條件 下一筆
			return false;
		}
		return true;
	}

	// 檢核 MCHT_CATEGORY
	Boolean checkdetlMcc(String dataType, String code1, String code2, String code3) throws Exception {
if(DEBUG_F==1)System.out.println("    Step 11 MCC dataType=" + dataType+", MCHT="+code1+","+code2+","+code3);
                int count0=0;
		Iterator it=parmDetlList.iterator();
		while(it.hasNext()) {
			ParmDetl parmDetlTmp=(ParmDetl) it.next();
//if(DEBUG_F==1)	System.out.println("data code3=="+parmDetlTmp.getDataCode3Tmp());
			if(parmDetlTmp.getDataTypeTmp().equals(dataType) &&
                           parmDetlTmp.getDataCode3Tmp().equals(code1) &&
                           parmDetlTmp.getDataCode2Tmp().equals(code3) && 
                           parmDetlTmp.getDataCodeTmp().equals(code2)	) {
				count0++;
			}
		}	
if(DEBUG_F==1)	System.out.println("MCHT_CATEGORY存在" + count0 + "條");
		if (count0 <= 0) {// 不符合條件 下一筆
			return false;
		}
		return true;
	}

	Boolean getNumOfDatatype(String dataType) throws Exception {
		int count0=0;
		Iterator it=parmDetlList.iterator();
		while(it.hasNext()) {
			ParmDetl parmDetlTmp=(ParmDetl) it.next();
			if(parmDetlTmp.getDataTypeTmp().equals(dataType)) {
				count0++;
			}
		}		
		if (count0 <= 0) {
			return false;
		}
		return true;
	}

	// 讀取 消費金額
	/*****************************************************************************/
	int selectMktCardConsume(String cardType, String idPSeqno, String groupCode) throws Exception {
		sqlCmd = "  SELECT sum(decode(?,'Y',a.consume_bl_amt,0)+ ";
		sqlCmd += "             decode(?,'Y',a.consume_it_amt,0)+ ";
		sqlCmd += "             decode(?,'Y',a.consume_id_amt,0)+ ";
		sqlCmd += "             decode(?,'Y',a.consume_ca_amt,0)+ ";
		sqlCmd += "             decode(?,'Y',a.consume_ao_amt,0)+ ";
		sqlCmd += "             decode(?,'Y',a.consume_ot_amt,0)) db_amt  ";
		sqlCmd += "  FROM   mkt_card_consume a ";
		sqlCmd += "   where  a.acct_month between ? and ? ";
		sqlCmd += "  and  a.card_no in (select card_no from crd_card  where ";
		sqlCmd += "    card_type =? ";
		sqlCmd += "   and decode(group_code, '','0000', group_code) =? ";
		if (consumeType.equals("1")) {
			sqlCmd += " and id_p_seqno =?)";
		} else if (consumeType.equals("2")) {
			sqlCmd += " and (card_no =? or major_card_no =?) )";
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			sqlCmd += " and card_no =? )";
		}
	// System.out.println(sqlCmd);
		setString(1, consumeBl);
		setString(2, consumeIt);
		setString(3, consumeId);
		setString(4, consumeCa);
		setString(5, consumeAo);
		setString(6, consumeOt);
		setString(7, isBegYm);
		setString(8, isEndYm);
		setString(9, cardType);
		setString(10, groupCode);
		if (consumeType.equals("1")) {
			setString(11, idPSeqno);
		} else if (consumeType.equals("2")) {
			setString(11, cardNo);
			setString(12, cardNo);
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			setString(11, cardNo);
		}

		if (selectTable() > 0) {
if(DEBUG_F==1)		System.out.println("db_amt==" + getValueInt("db_amt"));
			return getValueInt("db_amt");
		}
		return 0;
	}

	// 讀取消費次數
	int selectCnt(String idPSeqno, String cardPurchCode, String cardType, String groupCode) throws Exception {
		maxDestAmt = 0;
		sqlCmd = "   SELECT  count(*) db_cnt,max(a.DEST_AMT) max_dest_amt ";
		sqlCmd += "    FROM  bil_bill a left join bil_contract b ";
		sqlCmd += "      on  b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) ";
		sqlCmd += "     AND  b.contract_seq_no = a.contract_seq_no ";
		sqlCmd += "   WHERE  1=1  ";
		/**********************
		 * 消費資料 六大本金類
		 ******************************************************/
		sqlCmd += "     AND  a.acct_code in ( ";
		if (consumeBl.equals("Y")) sqlCmd += "'BL', ";

		if (consumeIt.equals("Y")) sqlCmd += "'IT', ";

		if (consumeId.equals("Y")) sqlCmd += "'ID', ";
		
		if (consumeCa.equals("Y")) sqlCmd += "'CA', ";

		if (consumeAo.equals("Y")) sqlCmd += "'AO', ";

		if (consumeOt.equals("Y")) sqlCmd += "'OT', ";

		sqlCmd = sqlCmd.substring(0, sqlCmd.length() - 2);// 将最后的逗号删除
		sqlCmd += ")";

		/**********************
		 * 消費資料 消費期間
		 ********************************************************/
	//	sqlCmd += "   AND a.acct_month    between ? and ? ";
		sqlCmd += "   AND a.purchase_date between ? and ? ";
		sqlCmd += "   AND a.card_no in (select card_no from crd_card ";
		sqlCmd += "                      where card_type =?  ";
// sqlCmd += " and decode(?,'1',major_id_p_seqno,id_p_seqno) =? ";
		sqlCmd += "                        and decode(group_code, '', '0000', group_code) =? ";
		if (consumeType.equals("1")) {
			sqlCmd += " and id_p_seqno =?)";
		} else if (consumeType.equals("2")) {
			sqlCmd += " and (card_no =? or major_card_no =?) )";
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			sqlCmd += " and card_no =? )";
		}
		setString(1, isBegDate_air);
		setString(2, isEndDate_air);
		setString(3, cardType);
		setString(4, groupCode);
		if (consumeType.equals("1")) {
			setString(5, idPSeqno);
		} else if (consumeType.equals("2")) {
			setString(5, cardNo);
			setString(6, cardNo);
		} else if (consumeType.equals("3") || consumeType.equals("0")) {
			setString(5, cardNo);
		}
///if(DEBUG_F==1) 	System.out.println(sqlCmd);
		if (selectTable() > 0) {
			maxDestAmt = getValueInt("max_dest_amt");
			return getValueInt("db_cnt");
		} else {
			return 0;
		}
	}

	//
	/***********************************************************************/
	class Document1 {// 文檔TCBINFINITECARD_110
		String outclass;
		String outType;
		String filler01;
		String filler08;
		String outCardNo;
		String outTransMM;
		String outTransDD;
		String outlastCardNo;
		String outFreeCount;
		String outName;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";

			rtn += fixLeft(outclass     , 1+1);
			rtn += fixLeft(outType      , 1+1);
			rtn += fixLeft(filler08     , 8);
			rtn += fixLeft(outCardNo    , 8+1);
			rtn += fixLeft(outTransMM   , 2);
			rtn += fixLeft(outTransDD   , 2);
			rtn += fixLeft(filler01     , 1);
			rtn += fixLeft(outFreeCount , 4+1);// outFreeCount
			rtn += fixLeft(outlastCardNo, 8+1);
			rtn += fixLeft(outName      ,10);
			return rtn;
		}
/*
     03  OUT-CLASS                PIC X(01)    VALUE SPACE.    卡片別(A:新增U:更新 D:刪除)       
     03  OUT-TYPE                 PIC X(01)    VALUE SPACE.    優惠別 (A:基本)  
     03  FILLER                   PIC X(08)    VALUE '********'.
     03  OUT-CARD-NO              PIC X(08)    VALUE SPACE.    卡號末8位
     03  OUT-TRANS-MM             PIC X(02)    VALUE SPACE.    符合旅行團費或機票消費紀 錄之月日
     03  OUT-TRANS-DD             PIC X(02)    VALUE SPACE.     
     03  OUT-FREE-COUNT           PIC 9(04)    VALUE ZERO.     免費機接次數
     03  OUT-LAST-CARD-NO         PIC X(08)    VALUE SPACE.     
     03  OUT-NAME                 PIC X(10)    VALUE SPACE.    持卡人姓名

*/
		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	class Document2 {// 文檔TCBINFINITECARD10_110
		String outclass;
		String outType;
		String filler01;
		String filler08;
		String outCardNo;
		String outTransMM;
		String outTransDD;
		String outlastCardNo;
		String outFreeCount;
		String outName;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";

			rtn += fixLeft(outclass     , 1+1);
			rtn += fixLeft(outType      , 1+1);
			rtn += fixLeft(filler08     , 8);
			rtn += fixLeft(outCardNo    , 8+1);
			rtn += fixLeft(outTransMM   , 2);
			rtn += fixLeft(outTransDD   , 2);// outFreeCount
			rtn += fixLeft(filler01     , 1);
			rtn += fixLeft(outFreeCount , 4+1);
			rtn += fixLeft(outlastCardNo, 8+1);
			rtn += fixLeft(outName      ,10);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}
	class ParmDetl {// 
		String dataTypeTmp;
		String dataCodeTmp;
		String dataCode2Tmp;
		String dataCode3Tmp;
		
		public String getDataTypeTmp() {
			return dataTypeTmp;
		}
		public void setDataTypeTmp(String dataTypeTmp) {
			this.dataTypeTmp = dataTypeTmp;
		}
		public String getDataCodeTmp() {
			return dataCodeTmp;
		}
		public void setDataCodeTmp(String dataCodeTmp) {
			this.dataCodeTmp = dataCodeTmp;
		}
		public String getDataCode2Tmp() {
			return dataCode2Tmp;
		}
		public void setDataCode2Tmp(String dataCode2Tmp) {
			this.dataCode2Tmp = dataCode2Tmp;
		}
		public String getDataCode3Tmp() {
			return dataCode3Tmp;
		}
		public void setDataCode3Tmp(String dataCode3Tmp) {
			this.dataCode3Tmp = dataCode3Tmp;
		}
	
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CmsC003 proc = new CmsC003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		// TODO Auto-generated method stub

	}
}
