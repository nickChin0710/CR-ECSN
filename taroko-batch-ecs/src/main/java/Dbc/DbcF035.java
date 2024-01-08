/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  110/07/26  V1.01.01  Castor      Initial                                 *
*  111/06/28  V1.01.02  Justin      增加新增預製卡功能                      *
*  111/07/22  V1.00.03  Ryan        檔案格式調整                                                                                              *
*  111/08/09  V1.00.04  Ryan        修改為檔案寫入TMP，在讀出處理                                                     *
*  111/09/02  V1.00.05  Wilson      變更程式名稱、增加判斷假日參數、調整錯誤處理方式                   *
*  111/09/07  V1.00.06  Wilson      batchno、recno調整                                                                 *
*  111/09/15  V1.00.07  Wilson      申請書編號寫入、中文欄位去全形空白、優惠辦法不處理                *
*  112/12/11  V1.00.08  Wilson      crd_item_unit不判斷卡種                                                        *
****************************************************************************/
package Dbc;

	import java.io.BufferedWriter;
    import java.io.UnsupportedEncodingException;
//    import java.text.Normalizer;
//    import java.io.UnsupportedEncodingException;
	import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
//	import java.text.Normalizer;
	import com.AccessDAO;
	import com.CommCrd;
	import com.CommCrdRoutine;
import com.CommDate;
//	import com.CommDate;
	import com.CommFTP;
	import com.CommFunction;
	import com.CommRoutine;
	import com.CommSecr;
	import com.CommString;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;

import Dbc.DbcF035;
//    import Dbc.DbcF035.buf1;

	public class DbcF035 extends AccessDAO {
	private static final int DETAIL_ROW_LENGTH = 1100;
	private static final String SUCCESS = "000";
	private String progname = "接收批次預製卡客戶基本資料處理程式  111/12/11  V1.00.08";
		CommFunction comm = new CommFunction();
		CommCrd comc = new CommCrd();
		CommCrdRoutine comcr = null;
		CommCrdRoutine comcr2 = null;
		CommSecr comsecr = null;
		CommString commString = new CommString();
		CommFTP commFTP = null;
		CommRoutine comr = null;
		CommDate commDate = new CommDate();
		int debug = 1;
      
		String prgmId = "DbcF035";
		
		String fileFolderPath = comc.getECSHOME() + "/media/dbc/";
 
	    String hFilename = "";
		
		String rptName1 = "";
		
		List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
		BufferedWriter nccc = null;
		
		
//		protected  final String dT1Str = "data_type,apply_date,batchno,recno,apply_no,saving_actno,card_ref_num,acct_type2,col1,"
//			   	                       + "apply_type,eng_name,bill_apply_flag,col2,"
//				                       + "home_area_code1,home_tel_no1,cellar_phone,office_area_code1,office_tel_no1,office_tel_ext1,col3,"
//                                       + "apply_id,birthday,sex,col4,"
//                                       + "revolve_int_rate_year,e_mail_addr,col5,"
//                                       + "responser_code,card_no,col6,"
//                                       + "market_agree_base,col7,"
//                                       + "chi_name,resident_zip,resident_addr1,resident_addr2,resident_addr3,resident_addr4,resident_addr5,"
//                                       + "mail_zip,mail_addr1,mail_addr2,mail_addr3,mail_addr4,mail_addr5,"
//                                       + "company_zip,company_addr1,company_addr2,company_addr3,company_addr4,company_addr5,col8";
//		
//
//	    protected final int[] dt1Length = { 1,7,2,6,12,13,2,1,4,1,26,1,2,7,4,10,10,4,10,6,14,11,8,1,26,4,30,24,3,16,58,1,61,102,6,10,10,12,12,56,6,10,10,12,12,56,6,10,10,12,12,56,262 };
//				
		protected  final String dT1Str = "data_type, apply_date, batchno, recno, apply_no, saving_actno, card_ref_num, acct_type2, group_code, "
                   + "apply_type, eng_name, bill_apply_flag, col13, col14, col15, reg_bank_no, mail_branch, crt_bank_no, vd_bank_no, "
                + "home_area_code1, home_tel_no1, cellar_phone, office_area_code1, office_tel_no1, office_tel_ext1, col26, col27, col28, "
                + "apply_id, birthday, sex, col32, col33, col34, col35, col36, stmt_cycle, "
                + "revolve_int_rate_year, e_mail_addr, col40, col41, col42, col43, col44, "
                + "responser_code, card_no, col47, col48, col49, col50, col51, "
                + "market_agree_base, col53, col54, col55, col56, col57, col58, col59, col60, col61, col62, col63, col64, col65, col66, col67, col68, col69, mail_type, col71, "
                + "chi_name, resident_zip, resident_addr1, resident_addr2, resident_addr3, resident_addr4, resident_addr5, "
                + "mail_zip, mail_addr1, mail_addr2, mail_addr3, mail_addr4, mail_addr5, "
                + "company_zip, company_addr1, company_addr2, company_addr3, company_addr4, company_addr5, col91, col92, col93, col94 ";


		protected final int[] dt1Length = { 1, 7, 2, 6, 12, 13, 2, 1, 4, 
						1, 26, 1, 2, 7, 16, 4, 4, 4, 4,
						4, 10, 10, 4, 10, 6, 4, 4, 6,
						11, 8, 1, 1, 1, 11, 8, 3, 2, 
						4, 30, 10, 1, 8, 1, 4,  
						3, 16, 20, 2, 20, 8, 8,  
						1, 8, 10, 1, 6, 2, 4, 1, 4, 7, 2, 3, 1, 1, 1, 8, 1, 1, 1, 105,
						102, 6, 10, 10, 12, 12, 56, 
						6, 10, 10, 12, 12, 56, 
						6, 10, 10, 12, 12, 56, 30, 14, 102, 10 };
		int rptSeq1 = 0;
		String buf = "";
	    String queryDate = "";
		String hModUser = "";
		String hCallBatchSeqno = "";
		String hNcccFilename = "";
		int hRecCnt1 = 0;
		int seq = 0;
        String prevRowType="";
        String preBatchno = "";
        double preRecno = 0;

		String getFileName;
		String outFileName;
		int totalInputFile;
		int totalOutputFile;
		
	    String tmpDataType;	    
	    String tmpApplyDate;
	    String tmpBatchno;
	    String tmpRrecno;
	    String tmpApplyNo;
	    String tmpSavingActno;
	    String tmpCardRefNnum;
	    String tmpAcctType2;
	    
	    String tmpApplyType;
	    String tmpEngName;
	    String tmpBillApplyFlag;
	    
	    String tmpRegBankNo;
	    String tmpMailBranch;
	    String tmpCrtBankNo;
	    String tmpVdBankNo;
	    
	    String tmpHomeAreaCode1;
	    String tmpHomeTelNo1;
	    String tmpCellarPhone;
	    String tmpOfficeAreaCode1;
	    String tmpOfficeTelNo1;
	    String tmpOfficeTelEWxt1;
	    
	    String tmpApplyId;
	    String tmpBirthday;
	    String tmpSex;	    
	    String tmpRevolveIntRateYear;
	    double tmpdRcrateDay;
	    String tmpEMailAddr;	    
	    String tmpResponseCode;
	    String tmpCardNo;	    
	    String tmpMarketAgreeBase;
	    
	    String tmpChiName;
	    String tmpResidentZip;
	    String tmpResidentAddr1;
	    String tmpResidentAddr2;
	    String tmpResidentAddr3;
	    String tmpResidentAddr4;
	    String tmpResidentAddr5;
	    
	    String tmpMailZip;
	    String tmpMailAddr1;
	    String tmpMailAddr2;
	    String tmpMailAddr3;
	    String tmpMailAddr4;
	    String tmpMailAddr5;
	    
	    String tmpCompanyZip;
	    String tmpCompanyAddr1;
	    String tmpCompanyAddr2;
	    String tmpCompanyAddr3;
	    String tmpCompanyAddr4;
	    String tmpCompanyAddr5;
	    
	    String tmpNewEndDate;
	    
	    String cardType  = "";
    	String groupCode = "";
    	String unitCode  = "";
    	String icFlag    = "";
    	String electronicCode = "";
    	String binType   = "";
    	String cardIndicator = "";
    	String stmtCycle = "";
    	String acctType = "";
    	String classCode = "";
    	String sourceCode = "";
    	String binNo = "";
    	String supFlag = "";
    	String validFm = "";
    	String validTo = "";
    	String cardRefNum = "";
    	String batchNo = "";
    	String recno   = "";

    	String idPSeqno  = "";
    	String acnoPSeqno = "";
    	String cardAcctIdx = "";
    	String eNews = "";
    	String acceptMbullet = "";
    	String acceptCallSell = "";
    	String acceptDm = "";
    	String dmFromMark = "";
    	String dmChgDate = "";
    	String acceptSms = "";
		
		String hBusiBusinessDate = "";

		protected String[] dT1 = new String[] {};

	    buf1 data = new buf1();

	    ArrayList<HashMap> tmpList = new  ArrayList<HashMap>();
	    HashMap<String,String> tmpMap = null;
	    
	    int total = 0;
	    int totalTmpCnt = 0;
	    int totalTmpErrCnt = 0;
	    int okCnt = 0;
	    int errCnt = 0;
		
		public int mainProcess(String[] args) {

			try {
				dT1 = dT1Str.split(",");
				// ====================================
				// 固定要做的
				dateTime();
				setConsoleMode("Y");
				javaProgram = this.getClass().getName();
				showLogMessage("I", "", javaProgram + " " + progname);
				// =====================================

				// 固定要做的

				if (!connectDataBase()) {
					comc.errExit("connect DataBase error", "");
				}

				comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
				comcr2 = new CommCrdRoutine(getDBconnect(), getDBalias());
				comsecr = new CommSecr(getDBconnect(), getDBalias());

				hModUser = comc.commGetUserID();
								
	            selectPtrBusinday();
				
	            // 若沒有給定查詢日期，則查詢日期為系統日
				if (args.length == 0) {
					queryDate = hBusiBusinessDate;
				} else if (args.length == 1) {
					if (!new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
						showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
						return -1;
					}
					queryDate = args[0];
				} else {
					comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
				}                    

				if (checkPtrHoliday() != 0) {
					exceptExit = 0;
					comc.errExit("今日為假日,不執行此程式", "");
		        }
				
				openFile();
				showLogMessage("I", "", "讀檔 ,成功筆數 : " + totalInputFile +" ,錯誤筆數 : "+ totalOutputFile);
				showLogMessage("I", "", "寫入DBC_PRE_TMP  ,成功筆數 : " + totalTmpCnt + " ,錯誤筆數 :"+ totalTmpErrCnt );
				showLogMessage("I", "", "開始讀取 DBC_PRE_TMP ....." );
				selectDbcPreTmp();

				// ==============================================
				// 固定要做的
	            showLogMessage("I", "", "處理結束 , 成功筆數 : "+ okCnt +", 錯誤筆數 : "+ errCnt);
				
	            /* 2022/06/29 Justin 刪除產生回饋檔給設一科 */
//	            if (total > 0) {
//					String filename = String.format("%s/%s", fileFolderPath, hFilename);
//					comc.writeReport(filename, lpar2, "MS950");
//					showLogMessage("I", "", " Export file path = [" + fileFolderPath + "]");
//					showLogMessage("I", "", " Export file = [" + hFilename + "]");
//				} else {
//					showLogMessage("I", "", "Export file = [no data]");
//				}
	          
	            finalProcess();
				
				return 0;
			} catch (Exception ex) {
				expMethod = "mainProcess";
				expHandle(ex);
				return exceptExit;
			}
		}

		/***********************************************************************/
		void selectPtrBusinday() throws Exception {
			hBusiBusinessDate = "";

			sqlCmd = " select business_date ";
			sqlCmd += " from ptr_businday ";
			sqlCmd += " fetch first 1 rows only ";
			selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("select_ptr_businday not found!", "", "");
			}
		
			hBusiBusinessDate = getValue("business_date");
				
		}
		/************************************************************************/
		  int checkPtrHoliday() throws Exception {
		      int holidayCount = 0;

		      sqlCmd = "select count(*) holidayCount ";
		      sqlCmd += " from ptr_holiday  ";
		      sqlCmd += "where holiday = ? ";
		      setString(1, queryDate);
		      int recordCnt = selectTable();      
		      if (notFound.equals("Y")) {
		          comc.errExit("select_ptr_holiday not found!", "");
		      }
		      if (recordCnt > 0) {
		    	  holidayCount = getValueInt("holidayCount");
		      }

		      if (holidayCount > 0) {
		          return 1;
		      } else {
		          return 0;
		      }
		  }

		/************************************************************************/
		int openFile() throws Exception {

			List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");

			final String fileNameTemplate = String.format("vd_q_%s[0-9][0-9].*", queryDate.substring(0, 8)); // 檔案正規表達式
			
//			hFilename = String.format("rtn_vd_q_%s01.txt", sysDate);
//			hFilename = String.format("rtn_vd_q_%s01.txt", queryDate.substring(0, 8));
//					
//	        rptName1 = hFilename;       

	        List<String> matchList = new ArrayList<>();
			if (listOfFiles.size() > 0) {	

				for (String file : listOfFiles) {
					getFileName = file;
		//			if (getFileName.length() != 27)
		//				continue;
		//			if (!getFileName.substring(0, 19).equals("M00600000.ICCUSQND."))
		//				continue;
					if( ! getFileName.matches(fileNameTemplate)) {
						continue;
					}
					if (checkFileCtl() != 0) {
						continue;
					}
				   	matchList.add(getFileName);
					readFile(getFileName);
			   }
			}

			//把搬檔案移到最後執行
			for (int i = 0; i < matchList.size(); i++) {
				getFileName = matchList.get(i);
				insertFileCtl1(getFileName);
				renameFile(getFileName);
			}
			
			if (matchList.size() < 1) {
				showLogMessage("I", "", "無檔案可處理,處理日期 = " + queryDate);
				
//				comcr.hCallErrorDesc = "無檔案可處理";
//	            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
			}
			return (0);
		}

		/**********************************************************************/
		int readFile(String fileName) throws Exception {
			String rec = "";
			String fileName2;
			int fi;
			fileName2 = fileFolderPath + fileName;

			int f = openInputText(fileName2);
			if (f == -1) {
				return 1;
			}
			closeInputText(f);

			setConsoleMode("N");
			fi = openInputText(fileName2, "MS950");
			setConsoleMode("Y");
			if (fi == -1) {
				return 1;
			}

			showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
			showLogMessage("I", "", " Process file =[" + fileName + "]");
			prevRowType = "";
			
			int retCode = getEmapBatchno();
	        if (retCode == 1) {
	            String err1 = "Get Batch No Error !!";
	            String err2 = "";
	            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
	        }
			
			while (true) {
				rec = readTextFile(fi); // read file data
				if (endFile[fi].equals("Y"))
					break;

				if (rec.length() == 0)
					continue;
				
				String currRowType = rec.substring(0, 1);
				if (currRowType.equals("1")) {
					if (prevRowType.equals("0") || prevRowType.equals("1")) {
						totalInputFile++;
						if (rec.getBytes("MS950").length < DETAIL_ROW_LENGTH) {
							String err1 = "匯入文字檔,格式錯誤";
							String err2 = "第 " + String.valueOf(totalInputFile) + " 行明細資料長度不足" + rec.getBytes("MS950").length;
							showLogMessage("E", "", rec);
							comcr.errRtn(err1, err2, hCallBatchSeqno);
							break;
						} else {
							boolean result = moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
							if (result) {								
								totalTmpCnt++;
								commitDataBase();
							}else {
								totalTmpErrCnt++;
								rollbackDataBase();
							}
								
							prevRowType = currRowType;
						}
					} else {
						if (prevRowType.equals("")) {
							String err1 = "匯入文字檔,格式錯誤";
							String err2 = "無首筆資料";
							comcr.errRtn(err1, err2, hCallBatchSeqno);
						}
						break;
					}
				} else {
					// current row type is not "DETAIL"
					
					if (!currRowType.equals("9")) {
						// current row type is not "TRAILER" and "DETAIL"
						prevRowType = currRowType;
					} else {
						// current row type is "TRAILER"
						if (prevRowType.equals("0") || prevRowType.equals("1"))
							prevRowType = currRowType;
					}
				}
				processDisplay(1000);
			}
            
			if (!prevRowType.equals("9")) {
				String err1 = "匯入文字檔,格式錯誤";
				String err2 = "";
				if (prevRowType.equals("1")) {
					err2 = "無尾筆資料";
				} else if (prevRowType.equals("")) {
					err2 = "無首筆資料";
				} else {
					err2 = "資料檔名稱異常";
				}

				comcr.errRtn(err1, err2, hCallBatchSeqno);
			}
			closeInputText(fi);

//			insertFileCtl1(fileName);

//			renameFile(fileName);

			return 0;
		}
		/**********************************************************************/
	    public int getEmapBatchno() throws Exception {
	        selectSQL = " max(substr(batchno,7,2))  as maxbatchno ";
	        daoTable = "dbc_pre_tmp a";
	        whereStr = "WHERE a.batchno like ? || '%' ";
	        setString(1, queryDate.substring(2, 8));
	        int recCnt = selectTable();

	        String BatchnoNum = "1";
	        if (getValueInt("maxbatchno") >= 1) {
	        	BatchnoNum = Integer.toString(getValueInt("maxbatchno") + 1);
	        }
	        String BatchnoNumZero = comm.fillZero(BatchnoNum, 2);
	        preBatchno = queryDate.substring(2, 8) + BatchnoNumZero;
	        showLogMessage("D", "", " 批號 =" + preBatchno);

	        return (0);
	    }

		
		/**********************************************************************/
		private void selectDbcPreTmp() throws Exception{

			fetchExtend = "DBC_PRE_TMP.";
			sqlCmd = " SELECT EMBOSS_DATE,BATCHNO,RECNO,APPLY_NO,SOURCE,ACT_NO,  " ;
			sqlCmd += " CARD_REF_NUM,GROUP_CODE,APPLY_SOURCE,ENG_NAME, " ;
			sqlCmd += " BILL_APPLY_FLAG,REG_BANK_NO,MAIL_BRANCH,CRT_BANK_NO, ";
			sqlCmd += " VD_BANK_NO,HOME_AREA_CODE1,HOME_TEL_NO1,CELLAR_PHONE, ";
			sqlCmd += " OFFICE_AREA_CODE1,OFFICE_TEL_NO1,OFFICE_TEL_EXT1, ";
			sqlCmd += " APPLY_ID,BIRTHDAY,SEX,STMT_CYCLE,DISCOUNT_CODE, ";
			sqlCmd += " E_MAIL_ADDR,CARD_NO,MAIL_TYPE,CHI_NAME,RESIDENT_ZIP, ";
			sqlCmd += " RESIDENT_ADDR1,RESIDENT_ADDR2,RESIDENT_ADDR3, ";
			sqlCmd += " RESIDENT_ADDR4,RESIDENT_ADDR5,MAIL_ZIP,MAIL_ADDR1, ";
			sqlCmd += " MAIL_ADDR2,MAIL_ADDR3,MAIL_ADDR4,MAIL_ADDR5,COMPANY_ZIP, ";
			sqlCmd += " COMPANY_ADDR1,COMPANY_ADDR2,COMPANY_ADDR3,COMPANY_ADDR4,COMPANY_ADDR5, ";
			sqlCmd += " MARKET_AGREE_BASE,TRACK2_TYPE ";
			sqlCmd += " FROM DBC_PRE_TMP WHERE CHECK_CODE = '' ";
			int tmpCnt = openCursor();
			
			while (fetchTable(tmpCnt)) {
				setTmpData();
			}
			closeCursor(tmpCnt);
			
			for(int i=0; i<tmpList.size(); i++) {
				boolean result = getTmpData(i);
				if (result) {
					okCnt++;
					commitDataBase();
				}else {
					errCnt++;
					rollbackDataBase();
				}
				updateDbcPreTmp();
				commitDataBase();
			}
		}
		
		/**********************************************************************/
		private void setTmpData() throws Exception{
			tmpMap = new HashMap<String,String>();  
			
			tmpMap.put("EMBOSS_DATE",  getValue("DBC_PRE_TMP.EMBOSS_DATE")); //處理日期
			tmpMap.put("BATCHNO",  getValue("DBC_PRE_TMP.BATCHNO")); //批號
			tmpMap.put("RECNO",  getValue("DBC_PRE_TMP.RECNO")); //序號
			tmpMap.put("APPLY_NO",  getValue("DBC_PRE_TMP.APPLY_NO")); //申請書編號
			tmpMap.put("ACT_NO",  getValue("DBC_PRE_TMP.ACT_NO")); //存款帳號(金融帳號)
			tmpMap.put("CARD_REF_NUM",  getValue("DBC_PRE_TMP.CARD_REF_NUM")); //ATM 卡片序號
			tmpMap.put("TRACK2_TYPE",  getValue("DBC_PRE_TMP.TRACK2_TYPE"));//第二軌型態

			tmpMap.put("APPLY_SOURCE",  getValue("DBC_PRE_TMP.APPLY_SOURCE")); //申請類別 
			tmpMap.put("ENG_NAME",  getValue("DBC_PRE_TMP.ENG_NAME")); //英文姓名
			tmpMap.put("BILL_APPLY_FLAG",  getValue("DBC_PRE_TMP.BILL_APPLY_FLAG")); //帳單寄送註記
			
			tmpMap.put("REG_BANK_NO",  getValue("DBC_PRE_TMP.REG_BANK_NO")); //發卡分行
			tmpMap.put("MAIL_BRANCH",  getValue("DBC_PRE_TMP.MAIL_BRANCH")); //寄件分行
			tmpMap.put("CRT_BANK_NO",  getValue("DBC_PRE_TMP.CRT_BANK_NO")); //建檔分行
			tmpMap.put("VD_BANK_NO",   getValue("DBC_PRE_TMP.VD_BANK_NO")); //記帳分行

			tmpMap.put("HOME_AREA_CODE1",  getValue("DBC_PRE_TMP.HOME_AREA_CODE1")); //住家電話－區碼    
			tmpMap.put("HOME_TEL_NO1",  getValue("DBC_PRE_TMP.HOME_TEL_NO1")); //住家電話
			tmpMap.put("CELLAR_PHONE",  getValue("DBC_PRE_TMP.CELLAR_PHONE")); //行動電話號碼
			tmpMap.put("OFFICE_AREA_CODE1",  getValue("DBC_PRE_TMP.OFFICE_AREA_CODE1")); //公司電話-區碼
			tmpMap.put("OFFICE_TEL_NO1",  getValue("DBC_PRE_TMP.OFFICE_TEL_NO1")); //公司電話
			tmpMap.put("OFFICE_TEL_EXT1",  getValue("DBC_PRE_TMP.OFFICE_TEL_EXT1")); //公司電話－分機
	        
			tmpMap.put("APPLY_ID",  getValue("DBC_PRE_TMP.APPLY_ID")); //身份證字號
			tmpMap.put("BIRTHDAY",  getValue("DBC_PRE_TMP.BIRTHDAY")); //出生日期
			tmpMap.put("SEX",  getValue("DBC_PRE_TMP.SEX")); //性別    
			tmpMap.put("DISCOUNT_CODE",  getValue("DBC_PRE_TMP.DISCOUNT_CODE")); //優惠辦法
			tmpMap.put("E_MAIL_ADDR",  getValue("DBC_PRE_TMP.E_MAIL_ADDR")); //電子信箱
			tmpMap.put("CARD_NO",  getValue("DBC_PRE_TMP.CARD_NO")); //卡號
			tmpMap.put("MARKET_AGREE_BASE",  getValue("DBC_PRE_TMP.MARKET_AGREE_BASE")); //拒絕行銷註記

			tmpMap.put("CHI_NAME",  getValue("DBC_PRE_TMP.CHI_NAME")); //中文姓名        
			tmpMap.put("RESIDENT_ZIP",  getValue("DBC_PRE_TMP.RESIDENT_ZIP")); //戶籍郵遞區號
			tmpMap.put("RESIDENT_ADDR1",  getValue("DBC_PRE_TMP.RESIDENT_ADDR1")); //戶籍地址1      
			tmpMap.put("RESIDENT_ADDR2",  getValue("DBC_PRE_TMP.RESIDENT_ADDR2")); //戶籍地址2
			tmpMap.put("RESIDENT_ADDR3",  getValue("DBC_PRE_TMP.RESIDENT_ADDR3")); //戶籍地址3
			tmpMap.put("RESIDENT_ADDR4",  getValue("DBC_PRE_TMP.RESIDENT_ADDR4")); //戶籍地址4
			tmpMap.put("RESIDENT_ADDR5",  getValue("DBC_PRE_TMP.RESIDENT_ADDR5")); //戶籍地址5

			tmpMap.put("MAIL_ZIP",  getValue("DBC_PRE_TMP.MAIL_ZIP")); //居住郵遞區號
			tmpMap.put("MAIL_ADDR1",  getValue("DBC_PRE_TMP.MAIL_ADDR1")); //居住地址1
			tmpMap.put("MAIL_ADDR2",  getValue("DBC_PRE_TMP.MAIL_ADDR2")); //居住地址2
			tmpMap.put("MAIL_ADDR3",  getValue("DBC_PRE_TMP.MAIL_ADDR3")); //居住地址3
			tmpMap.put("MAIL_ADDR4",  getValue("DBC_PRE_TMP.MAIL_ADDR4")); //居住地址4
			tmpMap.put("MAIL_ADDR5",  getValue("DBC_PRE_TMP.MAIL_ADDR5")); //居住地址5

			tmpMap.put("COMPANY_ZIP",  getValue("DBC_PRE_TMP.COMPANY_ZIP")); //公司郵遞區號
			tmpMap.put("COMPANY_ADDR1",  getValue("DBC_PRE_TMP.COMPANY_ADDR1")); //公司地址1
			tmpMap.put("COMPANY_ADDR2",  getValue("DBC_PRE_TMP.COMPANY_ADDR2")); //公司地址2
			tmpMap.put("COMPANY_ADDR3",  getValue("DBC_PRE_TMP.COMPANY_ADDR3")); //公司地址3
			tmpMap.put("COMPANY_ADDR4",  getValue("DBC_PRE_TMP.COMPANY_ADDR4")); //公司地址4
			tmpMap.put("COMPANY_ADDR5",  getValue("DBC_PRE_TMP.COMPANY_ADDR5")); //公司地址5
			
			tmpList.add(tmpMap);
		}
		
		/**********************************************************************/
		private String getListValue(String col , int cnt) {
			return tmpList.get(cnt).get(col).toString();
		}
		
		/**********************************************************************/
		private boolean moveData(Map<String, Object> map) throws Exception {

			DbcPreTmp tmp = new DbcPreTmp();
			
			preRecno++;

			tmpDataType = ((String) map.get("data_type")).trim(); // 資料檔名稱

			tmp.embossDate = commDate.tw2adDate(((String) map.get("apply_date")).trim()); // 處理日期
//			tmp.batchno = comc.getSubString(tmp.embossDate, 2, 8) + ((String) map.get("batchno")).trim(); // 批號
//			tmp.recno = ((String) map.get("recno")).trim(); // 序號
			tmp.applyNo = ((String) map.get("apply_no")).trim(); // 申請書編號
			tmp.actNo = ((String) map.get("saving_actno")).trim(); // 存款帳號(金融帳號)
			tmp.cardRefNum = ((String) map.get("card_ref_num")).trim(); // ATM 卡片序號
			tmp.track2Type = ((String) map.get("acct_type2")).trim(); // 第二軌型態

			tmp.groupCode = ((String) map.get("group_code")).trim(); // 申請卡別
			tmp.applySource = ((String) map.get("apply_type")).trim(); // 申請類別
			tmp.engName = comc.getSubString(((String) map.get("eng_name")).trim(), 0, 25); // 英文姓名
			tmp.billApplyFlag = ((String) map.get("bill_apply_flag")).trim(); // 帳單寄送註記
			tmp.regBankNo = ((String) map.get("reg_bank_no")).trim(); // 發卡分行
			tmp.mailBranch = ((String) map.get("mail_branch")).trim(); // 寄送分行
			tmp.crtBankNo = ((String) map.get("crt_bank_no")).trim(); // 建檔分行
			tmp.vdBankNo = ((String) map.get("vd_bank_no")).trim(); // 記帳分行
			tmp.homeAreaCode1 = ((String) map.get("home_area_code1")).trim(); // 住家電話－區碼
			tmp.homeTelNo1 = ((String) map.get("home_tel_no1")).trim(); // 住家電話
			tmp.cellarPhone = ((String) map.get("cellar_phone")).trim(); // 行動電話號碼
			tmp.officeAreaCode1 = ((String) map.get("office_area_code1")).trim(); // 公司電話-區碼
			tmp.officeTelNo1 = ((String) map.get("office_tel_no1")).trim(); // 公司電話
			tmp.officeTelExt1 = ((String) map.get("office_tel_ext1")).trim(); // 公司電話－分機

			tmp.applyId = comc.getSubString(((String) map.get("apply_id")).trim(), 0, 10); // 身份證字號
			tmp.birthday = ((String) map.get("birthday")).trim(); // 出生日期
			tmp.sex = ((String) map.get("sex")).trim(); // 性別
			tmp.stmtCycle = ((String) map.get("stmt_cycle")).trim(); // 帳單結帳日

			tmp.discountCode = ((String) map.get("revolve_int_rate_year")).trim(); // 優惠辦法
			tmp.eMailAddr = ((String) map.get("e_mail_addr")).trim(); // 電子信箱
			tmp.cardNo = ((String) map.get("card_no")).trim(); // 卡號
			tmp.marketAgreeBase = ((String) map.get("market_agree_base")).trim(); // 拒絕行銷註記

			tmp.mailType = ((String) map.get("mail_type")).trim(); // 領卡方式
			tmp.chiName = ((String) map.get("chi_name")).replaceAll("　"," ").trim(); // 中文姓名
			tmp.residentZip = ((String) map.get("resident_zip")).trim(); // 戶籍郵遞區號
			tmp.residentAddr1 = ((String) map.get("resident_addr1")).replaceAll("　"," ").trim(); // 戶籍地址1
			tmp.residentAddr2 = ((String) map.get("resident_addr2")).replaceAll("　"," ").trim(); // 戶籍地址2
			tmp.residentAddr3 = ((String) map.get("resident_addr3")).replaceAll("　"," ").trim(); // 戶籍地址3
			tmp.residentAddr4 = ((String) map.get("resident_addr4")).replaceAll("　"," ").trim(); // 戶籍地址4
			tmp.residentAddr5 = ((String) map.get("resident_addr5")).replaceAll("　"," ").trim(); // 戶籍地址5

			tmp.mailZip = ((String) map.get("mail_zip")).trim(); // 居住郵遞區號
			tmp.mailAddr1 = ((String) map.get("mail_addr1")).replaceAll("　"," ").trim(); // 居住地址1
			tmp.mailAddr2 = ((String) map.get("mail_addr2")).replaceAll("　"," ").trim(); // 居住地址2
			tmp.mailAddr3 = ((String) map.get("mail_addr3")).replaceAll("　"," ").trim(); // 居住地址3
			tmp.mailAddr4 = ((String) map.get("mail_addr4")).replaceAll("　"," ").trim(); // 居住地址4
			tmp.mailAddr5 = ((String) map.get("mail_addr5")).replaceAll("　"," ").trim(); // 居住地址5

			tmp.companyZip = ((String) map.get("company_zip")).trim(); // 公司郵遞區號
			tmp.companyAddr1 = ((String) map.get("company_addr1")).replaceAll("　"," ").trim(); // 公司地址1
			tmp.companyAddr2 = ((String) map.get("company_addr2")).replaceAll("　"," ").trim(); // 公司地址2
			tmp.companyAddr3 = ((String) map.get("company_addr3")).replaceAll("　"," ").trim(); // 公司地址3
			tmp.companyAddr4 = ((String) map.get("company_addr4")).replaceAll("　"," ").trim(); // 公司地址4
			tmp.companyAddr5 = ((String) map.get("company_addr5")).replaceAll("　"," ").trim(); // 公司地址5

			return insertDbcPreTmp(tmp);
		}
		
		/***********************************************************************/
	    private boolean getTmpData(int i) throws Exception {       
	         
	        tmpApplyDate = getListValue("EMBOSS_DATE",i); //處理日期
	        tmpBatchno = getListValue("BATCHNO",i); //批號
	        tmpRrecno = getListValue("RECNO",i); //序號
	        tmpApplyNo = getListValue("APPLY_NO",i); //申請書編號
	        tmpSavingActno = getListValue("ACT_NO",i); //存款帳號(金融帳號)
	        tmpCardRefNnum = getListValue("CARD_REF_NUM",i); //ATM 卡片序號
	        tmpAcctType2 = getListValue("TRACK2_TYPE",i);//第二軌型態

	        tmpApplyType = getListValue("APPLY_SOURCE",i); //申請類別 
	        tmpEngName = getListValue("ENG_NAME",i); //英文姓名
	        tmpBillApplyFlag = getListValue("BILL_APPLY_FLAG",i); //帳單寄送註記
	        
	        tmpRegBankNo = getListValue("REG_BANK_NO",i); //發卡分行
	        tmpMailBranch = getListValue("MAIL_BRANCH",i); //寄送分行
	        tmpCrtBankNo = getListValue("CRT_BANK_NO",i); //建檔分行
	        tmpVdBankNo = getListValue("VD_BANK_NO",i); //記帳分行

	        tmpHomeAreaCode1 = getListValue("HOME_AREA_CODE1",i); //住家電話－區碼    
	        tmpHomeTelNo1 = getListValue("HOME_TEL_NO1",i); //住家電話
	        tmpCellarPhone = getListValue("CELLAR_PHONE",i); //行動電話號碼
	        tmpOfficeAreaCode1 = getListValue("OFFICE_AREA_CODE1",i); //公司電話-區碼
	        tmpOfficeTelNo1 = getListValue("OFFICE_TEL_NO1",i); //公司電話
	        tmpOfficeTelEWxt1 = getListValue("OFFICE_TEL_EXT1",i); //公司電話－分機
	        
	        tmpApplyId = getListValue("APPLY_ID",i); //身份證字號
	        tmpBirthday = getListValue("BIRTHDAY",i); //出生日期
	        tmpBirthday =  commDate.tw2adDate(tmpBirthday);
	        tmpSex = getListValue("SEX",i); //性別    
	        tmpRevolveIntRateYear = getListValue("DISCOUNT_CODE",i); //優惠辦法
	        tmpEMailAddr = getListValue("E_MAIL_ADDR",i); //電子信箱
	        tmpCardNo = getListValue("CARD_NO",i); //卡號
	        tmpMarketAgreeBase = getListValue("MARKET_AGREE_BASE",i); //拒絕行銷註記

	        tmpChiName = getListValue("CHI_NAME",i); //中文姓名        
	        tmpResidentZip = getListValue("RESIDENT_ZIP",i); //戶籍郵遞區號
	        tmpResidentAddr1 = getListValue("RESIDENT_ADDR1",i); //戶籍地址1      
	        tmpResidentAddr2 = getListValue("RESIDENT_ADDR2",i); //戶籍地址2
	        tmpResidentAddr3 = getListValue("RESIDENT_ADDR3",i); //戶籍地址3
	        tmpResidentAddr4 = getListValue("RESIDENT_ADDR4",i); //戶籍地址4
	        tmpResidentAddr5 = getListValue("RESIDENT_ADDR5",i); //戶籍地址5

	        tmpMailZip = getListValue("MAIL_ZIP",i); //居住郵遞區號
	        tmpMailAddr1 = getListValue("MAIL_ADDR1",i); //居住地址1
	        tmpMailAddr2 = getListValue("MAIL_ADDR2",i); //居住地址2
	        tmpMailAddr3 = getListValue("MAIL_ADDR3",i); //居住地址3
	        tmpMailAddr4 = getListValue("MAIL_ADDR4",i); //居住地址4
	        tmpMailAddr5 = getListValue("MAIL_ADDR5",i); //居住地址5

	        tmpCompanyZip = getListValue("COMPANY_ZIP",i); //公司郵遞區號
	        tmpCompanyAddr1 = getListValue("COMPANY_ADDR1",i); //公司地址1
	        tmpCompanyAddr2 = getListValue("COMPANY_ADDR2",i); //公司地址2
	        tmpCompanyAddr3 = getListValue("COMPANY_ADDR3",i); //公司地址3
	        tmpCompanyAddr4 = getListValue("COMPANY_ADDR4",i); //公司地址4
	        tmpCompanyAddr5 = getListValue("COMPANY_ADDR5",i); //公司地址5

	        tmpResponseCode = getResponseCode();	   //處理結果   
	        selectNewEndDate(); //有效期限
//	        selectRcrateDay(); //PTR_RCRATE的RcrateDay
	        
	        boolean isUpdateOrInsertSuccess = false;
	        
			// 檢核資料是否正確
			if (SUCCESS.equals(tmpResponseCode)) {

				// 判斷卡號是否已存在DEBIT卡片資料檔
				if (isExistInDebit(tmpCardNo)) {

					// 檢核資料是否正確(已存在卡檔)
					if (isValidForExistingCard()) {
						boolean result = false;

						// 更新主檔資料
//						updateDbcEmboss();
//						updateDbcDebit();
						result = updateDbcCard();
						if (result == false) return false;
						
						result = updateDbcIdno();
						if (result == false) return false;
						
						result = updateDbaAcno();
						if (result == false) return false;

						isUpdateOrInsertSuccess = true;
					}

				} else {

					// 檢核資料是否正確(不存在卡檔)
					if (isValidForNotExistingCard()) {
						boolean result = false;
						
						// 取得必要欄位
						result = getRequiredColumns();
						if (result == false) return false;
						
						// 取得id_p_seqno
						idPSeqno = getIdPSeqno();
						if (idPSeqno == null) return false;
						
						/* 新增主檔資料 */
						
						// 1. 新增DBC_IDNO
						result = insertDbcIdno();
						if (result == false) return false;
						
						acnoPSeqno = getAcnoPSeqno();
						if (acnoPSeqno == null) return false;
						
						cardAcctIdx = Integer.toString(Integer.parseInt(acnoPSeqno));
						
						// 2. 新增DBC_ACNO
						result = insertDbaAcno();
						if (result == false) return false;
						
						result = insertCcaCardAcct();
						if (result == false) return false;
						
						result = insertCcaConsume();
						if (result == false) return false;
						
						// 3. 新增DBC_CARD
						result = insertDbcCard();
						if (result == false) return false;
						
						// 新增CCA_CARD_BASE
						result = insertCcaCardBase();
						if (result == false) return false;
						
						result = updateDbcEmbossForInsert();
						if (result == false) return false;
						
						isUpdateOrInsertSuccess = true;
					}
				}
			}
			
	        // 2022/06/24 Justin 刪除產生回饋檔給設一科
//	        createTextFile();
//            total++;
            
            // 異動DEBIT卡申請檔
			if (isUpdateOrInsertSuccess) {
				updateDbcEmboss(); // updateDbcEmboss改到updateDbcDebit2之前做
//				updateDbcDebit2();
				updateDbcDebit3(); // updateDbcDebit跟updateDbcDebit2合併
				return true;
			} 
			
			return false;
	    }
	    
/***********************************************************************/
	    
	    private String getAcnoPSeqno() throws Exception {
			// initial acnoPSeqno
	    	String acnoPSeqno = "";

			sqlCmd = " select to_char(ecs_acno.nextval,'0000000000') acno_seqno from dual ";
			int sqlRowNum = selectTable();
			if (sqlRowNum <= 0) {
				comcr.errRtn("select_acno_p_seqno error[not find],無法取得ACNO_P_SEQNO，卡號 = ", tmpCardNo, "");
//				showLogMessage("W", "", "無法取得ACNO_P_SEQNO");
//				return null;
			}
			acnoPSeqno = getValue("acno_seqno");
			
			return acnoPSeqno;
		}
	    
	    /***********************************************************************/
	    
	    private String getIdPSeqno() throws Exception {
			// initial idPSeqno
	    	String idPSeqno = "";
			
			sqlCmd = " select id_p_seqno,id_no_code from dbc_idno where id_no = ? and id_no_code = '0' fetch first 1 rows only ";
			setString(1, tmpApplyId);
			int sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				idPSeqno = getValue("id_p_seqno");
				return idPSeqno;
			}

			sqlCmd = " select id_p_seqno from crd_idno_seqno where id_no = ? ";
			setString(1, tmpApplyId);
			sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				idPSeqno = getValue("id_p_seqno");
				return idPSeqno;
			}

			sqlCmd = " select to_char(ecs_acno.nextval,'0000000000') id_p_seqno from dual ";
			sqlRowNum = selectTable();
			if (sqlRowNum <= 0) {
				comcr.errRtn("select_ecs_acno error[notFind]，無法取得ID_P_SEQNO, idNo = ", tmpApplyId, "");
//				showLogMessage("W", "", String.format("無法取得ID_P_SEQNO, idNo[%s]", tmpApplyId));
//				return null;
			}
			idPSeqno = getValue("id_p_seqno");
			
			boolean result = insertCrdIdnoSeqno(idPSeqno);
			
			return result ? idPSeqno : null;
		}
	    
	    /***********************************************************************/

		private boolean insertCrdIdnoSeqno(String idPSeqno) throws Exception {
			
			extendField = "CRD_IDNO_SEQNO.";
			setValue("CRD_IDNO_SEQNO.id_no", tmpApplyId);
			setValue("CRD_IDNO_SEQNO.id_p_seqno", idPSeqno);
			setValue("CRD_IDNO_SEQNO.id_flag", "");
			setValue("CRD_IDNO_SEQNO.bill_apply_flag", "");
			setValue("CRD_IDNO_SEQNO.debit_idno_flag", "Y");
		    daoTable = "CRD_IDNO_SEQNO";
		    int cnt = insertTable();
			if (cnt <= 0) {
				tmpResponseCode = "D54";
				showLogMessage("I", "", String.format("Error: insertCrdIdnoSeqno, id_no[%s], id_p_seqno[%s]", tmpApplyId, idPSeqno));
				return false;
			}
			return true;
		}

		/***********************************************************************/
	    
	    private boolean getRequiredColumns() throws Exception {
	    	// initialize these variables
	    	cardType  = "";
	    	groupCode = "";
	    	unitCode  = "";
	    	icFlag    = "";
	    	electronicCode = "";
	    	binType   = "";
	    	cardIndicator = "";
	    	stmtCycle = "";
	    	acctType  = "";
	    	classCode = "";
	    	sourceCode = "";
	    	binNo = "";
	    	supFlag = "";
	    	validFm = "";
	    	validTo = "";
	    	cardRefNum = "";
	    	batchNo = "";
	    	recno   = "";
	    	
	    	sqlCmd = " SELECT batchno, recno, card_type, group_code, unit_code, acct_type, class_code "
	    		   + " source_code, bin_no, sup_flag, valid_fm, valid_to, card_ref_num "
	    		   + " from dbc_emboss "
	    		   + " WHERE apply_source = 'P' "
	    		   + " AND in_main_date = '' "
	    		   + " AND prefab_cancel_flag != 'Y' "
	    		   + " AND CARD_NO = ? ";
			setString(1, tmpCardNo);
			int sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				cardType  = getValue("card_type");
				groupCode = getValue("group_code");
				unitCode  = getValue("unit_code");
				acctType  = getValue("acct_type");
		    	classCode = getValue("class_code");
		    	sourceCode = getValue("source_code");
		    	binNo     = getValue("bin_no");
		    	supFlag   = getValue("sup_flag");
		    	validFm   = getValue("valid_fm");
		    	validTo   = getValue("valid_to");
		    	cardRefNum = getValue("card_ref_num");
		    	batchNo   = getValue("batchno");
		    	recno     = getValue("recno");
			}else {
				tmpResponseCode = "D40";
				return false;
			}
	    	
	    	// 取得IC_FLAG、ELECTRONIC_CODE
	    	sqlCmd = " select ic_flag, electronic_code from crd_item_unit where unit_code = ? ";
			setString(1, unitCode);
			sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				icFlag = getValue("ic_flag");
				electronicCode = getValue("electronic_code");
			}else {
				comcr.errRtn("select_crd_item_unit error[notFound]", "卡種 = " + cardType + ",認同集團碼 = " + unitCode,"");
			}

			//取得BIN_TYPE
			sqlCmd = " select bin_type from ptr_bintable where bin_no || bin_no_2_fm || '0000' <= ? and bin_no || bin_no_2_to || '9999' >= ? ";
			setString(1, tmpCardNo);
			setString(2, tmpCardNo);
			sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				binType = getValue("bin_type");
			}else {
				comcr.errRtn("select_ptr_bintable error[not found]", "卡號 = " + tmpCardNo, "");
			}

			// 取得CARD_INDICATOR 、STMT_CYCLE
			sqlCmd = " select a.card_indicator,a.stmt_cycle from dbp_acct_type a,dbp_prod_type b where b.group_code = ? and b.card_type = '' and a.acct_type = b.acct_type ";
			setString(1, groupCode);
			sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				cardIndicator = getValue("card_indicator");
				stmtCycle = getValue("stmt_cycle");
			} else {
				sqlCmd = " select a.card_indicator,a.stmt_cycle from dbp_acct_type a,dbp_prod_type b where b.card_type = ? and a.acct_type = b.acct_type ";
				setString(1, cardType);
				sqlRowNum = selectTable();
				if (sqlRowNum > 0) {
					cardIndicator = getValue("card_indicator");
					stmtCycle = getValue("stmt_cycle");
				}else {
					comcr.errRtn("Error : 找不到帳戶資料", "團代 = " + groupCode + "卡種 =  " + cardType, "");
				}
			}
			
			// initialize these variables
	    	eNews = "";
			acceptMbullet = "";
			acceptCallSell = "";
			acceptDm = "";
			dmFromMark = "";
			dmChgDate = "";
			acceptSms = "";
	    	
	    	sqlCmd = "select e_news, accept_mbullet, accept_call_sell, accept_dm, dm_from_mark, dm_chg_date, accept_sms from crd_idno where id_no = ? and id_no_code = '0' ";
			setString(1, tmpApplyId);
			sqlRowNum = selectTable();
			if (sqlRowNum > 0) {
				eNews = getValue("e_news");
				acceptMbullet = getValue("accept_mbullet");
				acceptCallSell = getValue("accept_call_sell");
				acceptDm = getValue("accept_dm");
				dmFromMark = getValue("dm_from_mark");
				dmChgDate = getValue("dm_chg_date");
				acceptSms = getValue("accept_sms");
			}
			
			return true;
		}

		/***********************************************************************/
	    
		private boolean insertDbcCard() throws Exception {
			extendField = "DBC_CARD.";
			setValue("DBC_CARD.card_no", tmpCardNo);
			setValue("DBC_CARD.id_p_seqno", idPSeqno);
			setValue("DBC_CARD.group_code", groupCode);
			setValue("DBC_CARD.source_code", sourceCode);
			setValue("DBC_CARD.unit_code", unitCode);
			setValue("DBC_CARD.bin_no", binNo);
			setValue("DBC_CARD.bin_type", binType);
			setValue("DBC_CARD.sup_flag", supFlag);
			setValue("DBC_CARD.acno_flag", "1");
			setValue("DBC_CARD.major_id_p_seqno", idPSeqno);
			setValue("DBC_CARD.major_card_no", tmpCardNo);
			setValue("DBC_CARD.current_code", "0");
			setValue("DBC_CARD.new_beg_date", validFm);
			setValue("DBC_CARD.new_end_date", validTo);
			setValue("DBC_CARD.issue_date", sysDate);
			setValue("DBC_CARD.acct_type", acctType);
			setValue("DBC_CARD.p_seqno", acnoPSeqno);
			setValue("DBC_CARD.stmt_cycle", stmtCycle);
			setValue("DBC_CARD.activate_type", "O");
			setValue("DBC_CARD.activate_flag", "2");
			setValue("DBC_CARD.activate_date", sysDate);
			setValue("DBC_CARD.acct_no", tmpSavingActno);
			setValue("DBC_CARD.beg_bal", "0");
			setValue("DBC_CARD.end_bal", "0");
			setValue("DBC_CARD.ic_flag", icFlag);
			setValue("DBC_CARD.card_ref_num", cardRefNum);
			setValue("DBC_CARD.electronic_code", electronicCode);
			setValue("DBC_CARD.prefab_flag", "Y");
			setValue("DBC_CARD.prefab_use_code", "1");
			setValue("DBC_CARD.card_type", cardType);
			setValue("DBC_CARD.crt_date", sysDate);
			setValue("DBC_CARD.crt_user", prgmId);
			setValue("DBC_CARD.apr_date", sysDate);
			setValue("DBC_CARD.apr_user", prgmId);
			setValue("DBC_CARD.mod_user", prgmId);
			setValue("DBC_CARD.mod_time", sysDate + sysTime);
			setValue("DBC_CARD.mod_pgm", prgmId);
			setValue("DBC_CARD.mod_seqno", "0");
			// 新增
			setValue("DBC_CARD.ENG_NAME", tmpEngName);
			setValue("DBC_CARD.reg_bank_no", tmpRegBankNo);
			setValue("DBC_CARD.mail_branch", tmpMailBranch);
			setValue("DBC_CARD.crt_bank_no", tmpCrtBankNo);
			setValue("DBC_CARD.vd_bank_no", tmpVdBankNo);
			setValue("DBC_CARD.apply_no", tmpApplyNo);
		    daoTable = "DBC_CARD";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	tmpResponseCode = "D58";
		    	showLogMessage("I", "", String.format("Error: insertDbcCard()[duplicate], card_no[%s]", tmpCardNo));
		    	return false;
		    }
		    return true;
		}
		
/***********************************************************************/
	    
		private boolean insertCcaCardBase() throws Exception {
			extendField = "CCA_CARD_BASE.";
			setValue("CCA_CARD_BASE.card_no", tmpCardNo);
			setValue("CCA_CARD_BASE.debit_flag", "Y");
			setValue("CCA_CARD_BASE.bin_type", binType);
			setValue("CCA_CARD_BASE.id_p_seqno", idPSeqno);
			setValue("CCA_CARD_BASE.acno_p_seqno", acnoPSeqno);
			setValue("CCA_CARD_BASE.p_seqno", acnoPSeqno);
			setValue("CCA_CARD_BASE.major_id_p_seqno", idPSeqno);
			setValue("CCA_CARD_BASE.acno_flag", "1");
			setValue("CCA_CARD_BASE.acct_type", acctType);
			setValue("CCA_CARD_BASE.sup_flag", supFlag);
			setValue("CCA_CARD_BASE.card_acct_idx", cardAcctIdx);
			setValue("CCA_CARD_BASE.card_indicator", "1");
			setValue("CCA_CARD_BASE.mod_user", prgmId);
			setValue("CCA_CARD_BASE.mod_time", prgmId);
			setValue("CCA_CARD_BASE.mod_pgm", prgmId);
			setValue("CCA_CARD_BASE.mod_seqno", "0");
		    daoTable = "CCA_CARD_BASE";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	tmpResponseCode = "D59";
		    	showLogMessage("I", "", String.format("Error: insertCcaCardBase()[duplicate], card_no[%s]", tmpCardNo));
		    	return false;
		    }
		    return true;
		}
		
		/***********************************************************************/

		private boolean insertDbaAcno() throws Exception {
			// 若檔案的帳單寄送註記為1、2、3 , 則將值放入BILL_APPLY_FLAG
			String billApplyFlag = Arrays.asList("1", "2", "3").contains(tmpBillApplyFlag) ? tmpBillApplyFlag : "";
			// 若檔案的帳單寄送註記為4  STAT_SEND_INTERNET =“Y”
			String statSendInternet = "4".equals(tmpBillApplyFlag) ? "Y" : "";
			
			extendField = "DBA_ACNO.";
			setValue("DBA_ACNO.p_seqno", acnoPSeqno);
			setValue("DBA_ACNO.acct_type", acctType);
			setValue("DBA_ACNO.acct_key", tmpApplyId + "0");
			setValue("DBA_ACNO.acct_status", "1");
			setValue("DBA_ACNO.acct_sub_status", "1");
			setValue("DBA_ACNO.stmt_cycle", stmtCycle);
			setValue("DBA_ACNO.id_p_seqno", idPSeqno);
			setValue("DBA_ACNO.acct_holder_id", tmpApplyId);
			setValue("DBA_ACNO.acct_holder_id_code", "0");
			setValue("DBA_ACNO.card_indicator", cardIndicator);
			setValue("DBA_ACNO.rc_use_b_adj", "1");
			setValue("DBA_ACNO.autopay_indicator", "1");
			setValue("DBA_ACNO.worse_mcode", "0");
			setValue("DBA_ACNO.legal_delay_code", "9");
			setValue("DBA_ACNO.inst_auth_loc_amt", "0");
			setValue("DBA_ACNO.special_stat_code", "5");
			setValue("DBA_ACNO.acct_no", tmpSavingActno);
			setValue("DBA_ACNO.new_vdchg_flag", "Y");
			setValue("DBA_ACNO.class_code", classCode);
			setValue("DBA_ACNO.crt_date", sysDate);
			setValue("DBA_ACNO.mod_user", prgmId);
			setValue("DBA_ACNO.mod_time", sysDate + sysTime);
			setValue("DBA_ACNO.mod_pgm", prgmId);
			setValue("DBA_ACNO.mod_seqno", "0");
			// 新增
			setValue("DBA_ACNO.BILL_APPLY_FLAG", billApplyFlag);
			setValue("DBA_ACNO.STAT_SEND_INTERNET", statSendInternet);
//			setValueDouble("DBA_ACNO.REVOLVE_INT_RATE", tmpdRcrateDay);
		    daoTable = "DBA_ACNO";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	tmpResponseCode = "D55";
		    	showLogMessage("I", "", String.format("Error: insertDbaAcno()[duplicate], p_seqno[%s]", acnoPSeqno));
		    	return false;
		    } 		    		
		    return true;
		}
		
		/***********************************************************************/

		private boolean insertCcaCardAcct() throws Exception {

			extendField = "CCA_CARD_ACCT.";
			setValue("CCA_CARD_ACCT.acno_p_seqno", acnoPSeqno);
			setValue("CCA_CARD_ACCT.debit_flag", "Y");
			setValue("CCA_CARD_ACCT.acno_flag", "1");
			setValue("CCA_CARD_ACCT.acct_type", acctType);
			setValue("CCA_CARD_ACCT.id_p_seqno", idPSeqno);
			setValue("CCA_CARD_ACCT.p_seqno", acnoPSeqno);
			setValue("CCA_CARD_ACCT.card_acct_idx", cardAcctIdx);
			setValue("CCA_CARD_ACCT.mod_user", prgmId);
			setValue("CCA_CARD_ACCT.mod_time", sysDate + sysTime);
			setValue("CCA_CARD_ACCT.mod_pgm", prgmId);
			setValue("CCA_CARD_ACCT.mod_seqno", "0");
			
		    daoTable = "CCA_CARD_ACCT";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	tmpResponseCode = "D56";
		    	showLogMessage("I", "", String.format("Error: insertCcaCardAcct()[duplicate], card_acct_idx[%s]", cardAcctIdx));
		    	return false;		    	
		    }
		    return true;
		}
		
		/***********************************************************************/

		private boolean insertCcaConsume() throws Exception {

			extendField = "CCA_CONSUME.";
			setValue("CCA_CONSUME.p_seqno", acnoPSeqno);
			setValue("CCA_CONSUME.card_acct_idx", cardAcctIdx);
			setValue("CCA_CONSUME.mod_user", prgmId);
			setValue("CCA_CONSUME.mod_time", sysDate + sysTime);
			setValue("CCA_CONSUME.mod_pgm", prgmId);
			
		    daoTable = "CCA_CONSUME";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	tmpResponseCode = "D57";
		    	showLogMessage("I", "", String.format("Error: insertCcaConsume()[duplicate], card_acct_idx[%s]", cardAcctIdx));
		    	return false;
		    }
		    return true;
		}
		
		/***********************************************************************/

		private boolean insertDbcIdno() throws Exception {
			extendField = "DBC_IDNO.";
			
			setValue("DBC_IDNO.ID_P_SEQNO", idPSeqno);
			setValue("DBC_IDNO.ID_NO", tmpApplyId);
			setValue("DBC_IDNO.ID_NO_CODE", "0");
			setValue("DBC_IDNO.CARD_SINCE", sysDate);
			setValue("DBC_IDNO.FST_STMT_CYCLE", stmtCycle);
			setValue("DBC_IDNO.CRT_DATE", sysDate);
			setValue("DBC_IDNO.E_NEWS", eNews == null || eNews.isEmpty() ? "N" : eNews );
			setValue("DBC_IDNO.ACCEPT_MBULLET", acceptMbullet == null || acceptMbullet.isEmpty() ? "N" : acceptMbullet);
			setValue("DBC_IDNO.ACCEPT_CALL_SELL", acceptCallSell == null || acceptCallSell.isEmpty() ? "N" : acceptCallSell);
			setValue("DBC_IDNO.ACCEPT_DM", acceptDm == null || acceptDm.isEmpty() ? "N" : acceptDm);
			setValue("DBC_IDNO.DM_FROM_MARK", dmFromMark == null || dmFromMark.isEmpty() ? "N" : dmFromMark);
			setValue("DBC_IDNO.DM_CHG_DATE", dmChgDate == null || dmChgDate.isEmpty() ? "N" : dmChgDate);
			setValue("DBC_IDNO.ACCEPT_SMS", acceptSms == null || acceptSms.isEmpty() ? "N" : acceptSms);
			setValue("DBC_IDNO.MSG_FLAG", "Y");
			setValue("DBC_IDNO.MOD_USER", prgmId);
			setValue("DBC_IDNO.MOD_TIME", sysDate + sysTime);
			setValue("DBC_IDNO.MOD_PGM", prgmId);
			// 新增
			setValue("DBC_IDNO.CHI_NAME", tmpChiName);
			setValue("DBC_IDNO.SEX", tmpSex);
			setValue("DBC_IDNO.BIRTHDAY", tmpBirthday);
			setValue("DBC_IDNO.ENG_NAME", tmpEngName);
			setValue("DBC_IDNO.HOME_AREA_CODE1", tmpHomeAreaCode1);
			setValue("DBC_IDNO.HOME_TEL_NO1", tmpHomeTelNo1);
			setValue("DBC_IDNO.CELLAR_PHONE", tmpCellarPhone);
			setValue("DBC_IDNO.OFFICE_AREA_CODE1", tmpOfficeAreaCode1);
			setValue("DBC_IDNO.OFFICE_TEL_NO1", tmpOfficeTelNo1);
			setValue("DBC_IDNO.OFFICE_TEL_EXT1", tmpOfficeTelEWxt1);
			setValue("DBC_IDNO.RESIDENT_ZIP", tmpResidentZip);
			setValue("DBC_IDNO.RESIDENT_ADDR1", tmpResidentAddr1);
			setValue("DBC_IDNO.RESIDENT_ADDR2", tmpResidentAddr2);
			setValue("DBC_IDNO.RESIDENT_ADDR3", tmpResidentAddr3);
			setValue("DBC_IDNO.RESIDENT_ADDR4", tmpResidentAddr4);
			setValue("DBC_IDNO.RESIDENT_ADDR5", tmpResidentAddr5);
			setValue("DBC_IDNO.MAIL_ZIP", tmpMailZip);
			setValue("DBC_IDNO.MAIL_ADDR1", tmpMailAddr1);
			setValue("DBC_IDNO.MAIL_ADDR2", tmpMailAddr2);
			setValue("DBC_IDNO.MAIL_ADDR3", tmpMailAddr3);
			setValue("DBC_IDNO.MAIL_ADDR4", tmpMailAddr4);
			setValue("DBC_IDNO.MAIL_ADDR5", tmpMailAddr5);
			setValue("DBC_IDNO.COMPANY_ZIP", tmpCompanyZip);
			setValue("DBC_IDNO.COMPANY_ADDR1", tmpCompanyAddr1);
			setValue("DBC_IDNO.COMPANY_ADDR2", tmpCompanyAddr2);
			setValue("DBC_IDNO.COMPANY_ADDR3", tmpCompanyAddr3);
			setValue("DBC_IDNO.COMPANY_ADDR4", tmpCompanyAddr4);
			setValue("DBC_IDNO.COMPANY_ADDR5", tmpCompanyAddr5);
			setValue("DBC_IDNO.E_MAIL_ADDR", tmpEMailAddr);
			setValue("DBC_IDNO.MARKET_AGREE_BASE", tmpMarketAgreeBase);
		    daoTable = "DBC_IDNO";
		    int cnt = insertTable();
		    if ("Y".equals(dupRecord)) {
		    	boolean result = false;
		    	result = updateDbcIdno();
		    	if (result == false) return false;
		    }
		    return true;
		}

		/***********************************************************************/

		private boolean isValidForNotExistingCard() throws Exception {
			tmpResponseCode = SUCCESS;
			
			// 檔案的金融帳號是否已存在DEBIT帳戶資料檔
			// 若金融帳號已存在DEBIT帳戶資料檔則不新增主檔資料，並回覆處理結果 = D52
			if (chkData("D52"))
				tmpResponseCode = "D52";
			
			// 檔案的卡號是否已註銷
			// 若卡號已註銷則不新增主檔資料，並回覆處理結果 = D53
			else if (chkData("D53"))
				tmpResponseCode = "D53";
			
			if (SUCCESS.equals(tmpResponseCode) == false) {
				showLogMessage("I", "", String.format("檢核資料是否正確(不存在卡檔):錯誤[%s], cardNo[%s], savingActno[%s]", 
						tmpResponseCode, tmpCardNo, tmpSavingActno));
				return false;
			}

			return true;
		}
	    
	    /***********************************************************************/
	    
		private boolean isValidForExistingCard() throws Exception {
			tmpResponseCode = SUCCESS;
			
			// 檔案的金融帳號是否跟DEBIT卡片基本資料檔的金融帳號相同
			// 若金融帳號跟DEBIT卡片基本資料檔的金融帳號不同則不更新主檔資料，並回覆處理結果 = D45
			if (!chkData("D45"))
				tmpResponseCode = "D45";
			
			// 檔案的身分證字號是否跟DEBIT卡人基本資料檔的身分證字號相同
			// 若身分證字號跟DEBIT卡人基本資料檔的身分證字號不同則不更新主檔資料，並回覆處理結果 = D46
			else if (!chkData("D46"))
				tmpResponseCode = "D46";
			
			// 檔案的金融帳號是否跟DEBIT帳戶基本資料檔的金融帳號相同
			// 若金融帳號跟DEBIT帳戶基本資料檔的金融帳號不同則不更新主檔資料，並回覆處理結果 = D47
			else if (!chkData("D47"))
				tmpResponseCode = "D47";
			
			if (SUCCESS.equals(tmpResponseCode) == false) {
				showLogMessage("I", "", String.format("檢核資料是否正確(已存在卡檔):錯誤[%s], cardNo[%s], savingActno[%s], applyId[%s]", 
						tmpResponseCode, tmpCardNo, tmpSavingActno, tmpApplyId));
				return false;
			}

			return true;
		}

		/***********************************************************************/

		private boolean isExistInDebit(String cardNo) throws Exception {
			sqlCmd =  " SELECT count(*) totalCount ";
			sqlCmd += " from DBC_CARD ";
			sqlCmd += " WHERE CARD_NO = ? ";
			setString(1, cardNo);

			int recordCnt = selectTable();

			if (recordCnt > 0)
				return getValueInt("totalCount") > 0;
				
			return false;
		}

		/***********************************************************************/
		int checkFileCtl() throws Exception {
			int totalCount = 0;

			sqlCmd = "select count(*) totalCount ";
			sqlCmd += " from crd_file_ctl ";
			sqlCmd += " where file_name = ? ";
			
	        setString(1, getFileName);
	        
			int recordCnt = selectTable();

			if (recordCnt > 0)
				totalCount = getValueInt("totalCount");

			if (totalCount > 0) {
	            showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理過不可重複處理(crd_file_ctl)"));
				return (1);
			}
			return (0);
		}
		
		/***********************************************************************/
		private boolean updateDbcEmbossForInsert() throws Exception {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE DBC_EMBOSS set ")
				  .append(" apply_id = ?")
				  .append(",act_no = ?  ")
				  .append(",in_main_date = to_char(sysdate,'yyyymmdd') ")
				  .append(",rtn_nccc_date = to_char(sysdate,'yyyymmdd') ")
				  .append(",mod_time = sysdate ")
				  .append(",mod_user = ? ")
				  .append(",mod_pgm = ? ")
				  .append(",BIRTHDAY = ? ")
				  .append(",CELLAR_PHONE = ? ")
				  .append(" where batchno = ? and recno = ? ");
				sqlCmd = sb.toString();
				int i = 1;
				setString(i++, tmpApplyId);
				setString(i++, tmpSavingActno);
				setString(i++, prgmId);
				setString(i++, prgmId);
				setString(i++, tmpBirthday);
				setString(i++, tmpCellarPhone);
				setString(i++, batchNo);
				setString(i++, recno);
				int updateCnt = updateTable();
				if (updateCnt <= 0) {
					tmpResponseCode = "D60";
					showLogMessage("I", "", String.format("ERROR: updateDbcEmbossForInsert(), batchno[%s] and recno[%s]", batchNo, recno));
					return false;
				}
			} catch (Exception e) {
				if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
					tmpResponseCode = "D61";
					showLogMessage("I", "", String.format("ERROR: updateDbcEmbossForInsert() [duplicate] , batchno[%s] and recno[%s]", batchNo, recno));
					return false;
				}
				throw e;
			} finally {
				sqlCmd = "";
			}

			return true;
		}

		/***********************************************************************/
		void updateDbcEmboss() throws Exception {
			double tmpdRevolveIntRateYear;
			try {
				tmpdRevolveIntRateYear = Double.valueOf(tmpRevolveIntRateYear.substring(0,2)) + (Double.valueOf(tmpRevolveIntRateYear.substring(2,4))/100);
			}catch (Exception e) {
				showLogMessage("E", "", String.format("updateDbcEmboss(): tmpRevolveIntRateYear[%s]錯誤,cardNo[%s]", tmpRevolveIntRateYear, tmpCardNo));
				throw e;
			}

			int columnRC = 0;
			
			daoTable = "dbc_emboss";
			updateSQL = " apply_id = ?,";
			updateSQL += " pm_id = ?,";
			updateSQL += " birthday = ?,";
			updateSQL += " pm_birthday = ?,";
			
			updateSQL += " sex = ?,";
			updateSQL += " eng_name = ?,";
			updateSQL += " home_area_code1 = ?,";
			updateSQL += " home_tel_no1 = ?,";

			updateSQL += " cellar_phone = ?,";
			updateSQL += " office_area_code1 = ?,";
			updateSQL += " office_tel_no1 = ?,";
			updateSQL += " office_tel_ext1 = ?,";

			updateSQL += " chi_name = ?,";
			if ("1".equals(tmpBillApplyFlag) || "2".equals(tmpBillApplyFlag) || "3".equals(tmpBillApplyFlag)) {
				updateSQL += " bill_apply_flag = ?,";
			} else if ("4".equals(tmpBillApplyFlag)) {
				updateSQL += " stat_send_internet = ?,";
			} else {
				columnRC = -1;
			}
	        
			updateSQL += " e_mail_addr = ?,";
//			updateSQL += " revolve_int_rate_year = ?,";
			updateSQL += " market_agree_base = ?,";

			updateSQL += " resident_zip = ?,";
			updateSQL += " resident_addr1 = ?,";
			updateSQL += " resident_addr2 = ?,";
			updateSQL += " resident_addr3 = ?,";
			updateSQL += " resident_addr4 = ?,";
			updateSQL += " resident_addr5 = ?,";
			
			updateSQL += " mail_zip = ?,";
			updateSQL += " mail_addr1 = ?,";
			updateSQL += " mail_addr2 = ?,";
			updateSQL += " mail_addr3 = ?,";
			updateSQL += " mail_addr4 = ?,";
			updateSQL += " mail_addr5 = ?,";
			
			updateSQL += " company_zip = ?,";
			updateSQL += " company_addr1 = ?,";
			updateSQL += " company_addr2 = ?,";
			updateSQL += " company_addr3 = ?,";
			updateSQL += " company_addr4 = ?,";
			updateSQL += " company_addr5 = ?,";
			
			updateSQL += " reg_bank_no = ?,";
			updateSQL += " mail_branch = ?,";
			updateSQL += " crt_bank_no = ?,";
			updateSQL += " vd_bank_no = ?,";
			
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_user  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	        whereStr = " where card_no = ?  ";  
	        whereStr +=" and in_main_date <> '' ";
//	       whereStr +=" and end_ibm_date = '' ";
	        setString(1, tmpApplyId);
	        setString(2, tmpApplyId);
	        setString(3, tmpBirthday);
	        setString(4, tmpBirthday);	        

	        setString(5, tmpSex);
	        setString(6, tmpEngName);
	        setString(7, tmpHomeAreaCode1);
	        setString(8, tmpHomeTelNo1);
		    
	        setString(9, tmpCellarPhone);
	        setString(10, tmpOfficeAreaCode1);
	        setString(11, tmpOfficeTelNo1);
	        setString(12, tmpOfficeTelEWxt1);
		    
	        setString(13, tmpChiName);
			if ("1".equals(tmpBillApplyFlag) || "2".equals(tmpBillApplyFlag) || "3".equals(tmpBillApplyFlag)) {
				setString(14, tmpBillApplyFlag);
			} else if ("4".equals(tmpBillApplyFlag)) {
				setString(14, "Y");
			}
	        setString(columnRC + 15, tmpEMailAddr);
//	        setDouble(columnRC + 16, tmpdRevolveIntRateYear);
	        setString(columnRC + 16, tmpMarketAgreeBase);

	        setString(columnRC + 17, tmpResidentZip);
	        setString(columnRC + 18, tmpResidentAddr1);
	        setString(columnRC + 19, tmpResidentAddr2);
	        setString(columnRC + 20, tmpResidentAddr3);
	        setString(columnRC + 21, tmpResidentAddr4);
	        setString(columnRC + 22, tmpResidentAddr5);

	        setString(columnRC + 23, tmpMailZip);
	        setString(columnRC + 24, tmpMailAddr1);
	        setString(columnRC + 25, tmpMailAddr2);
	        setString(columnRC + 26, tmpMailAddr3);
	        setString(columnRC + 27, tmpMailAddr4);
	        setString(columnRC + 28, tmpMailAddr5);

	        setString(columnRC + 29, tmpCompanyZip);
	        setString(columnRC + 30, tmpCompanyAddr1);
	        setString(columnRC + 31, tmpCompanyAddr2);
	        setString(columnRC + 32, tmpCompanyAddr3);
	        setString(columnRC + 33, tmpCompanyAddr4);
	        setString(columnRC + 34, tmpCompanyAddr5);
	        
	        setString(columnRC + 35, tmpRegBankNo);
	        setString(columnRC + 36, tmpMailBranch);
	        setString(columnRC + 37, tmpCrtBankNo);
	        setString(columnRC + 38, tmpVdBankNo);
		    	        
	        setString(columnRC + 39, prgmId);
	        setString(columnRC + 40, prgmId);
	        setString(columnRC + 41, sysDate + sysTime);
	        setString(columnRC + 42, tmpCardNo);

			updateTable();
			
			if (notFound.equals("Y")) {
				// 2022/06/24 Justin updateDbcEmboss找不到改成不當掉
//				String err1 ="crd_emboss無可更新資料";
//				String err2 = tmpCardNo;
//				comcr.errRtn(err1,err2, hCallBatchSeqno);
				showLogMessage("I", "", String.format("dbc_emboss無可更新資料,cardNo[%s]", tmpCardNo));
			}

			return;
		}
		
		/***********************************************************************/
		void updateDbcDebit() throws Exception {

			daoTable = "dbc_debit";
			updateSQL = " apply_id = ?,";
			updateSQL += " pm_id = ?,";
			updateSQL += " birthday = ?,";	
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	        whereStr = " where card_no = ?  ";  
	        setString(1, tmpApplyId);
	        setString(2, tmpApplyId);
	        setString(3, tmpBirthday);  
	        setString(4, prgmId);
	        setString(5, sysDate + sysTime);
	        setString(6, tmpCardNo);

			updateTable();
			
			if (notFound.equals("Y")) {
				String err1 ="dbc_debit無可更新資料";
				String err2 = tmpCardNo;
				comcr.errRtn(err1,err2, hCallBatchSeqno);
			}

			return;
		}

		/***********************************************************************/
		void updateDbcDebit2() throws Exception {

			daoTable = "dbc_debit";
	        updateSQL  = " end_ibm_date = ?, ";
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	        whereStr = " where card_no = ?  ";  
	        setString(1, sysDate);
	        setString(2, prgmId);
	        setString(3, sysDate + sysTime);
	        setString(4, tmpCardNo);

			updateTable();
			
			if (notFound.equals("Y")) {
				String err1 ="dbc_debit無可更新資料";
				String err2 = tmpCardNo;
				comcr.errRtn(err1,err2, hCallBatchSeqno);
			}

			return;
		}
		/***********************************************************************/
		private void updateDbcDebit3() throws Exception {
			
			daoTable = "dbc_debit";
			updateSQL = " apply_id = ?,";
			updateSQL += " pm_id = ?,";
			updateSQL += " birthday = ?,";	
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') , ";
			updateSQL += " end_ibm_date = ? ";
	        whereStr = " where card_no = ?  ";  
	        setString(1, tmpApplyId);
	        setString(2, tmpApplyId);
	        setString(3, tmpBirthday);  
	        setString(4, prgmId);
	        setString(5, sysDate + sysTime);
	        setString(6, sysDate);
	        setString(7, tmpCardNo);

			updateTable();
			
			if (notFound.equals("Y")) {
				// 2022/06/24 Justin 找不到改成不當掉
//				String err1 ="dbc_debit無可更新資料";
//				String err2 = tmpCardNo;
//				comcr.errRtn(err1,err2, hCallBatchSeqno);
				showLogMessage("I", "", String.format("dbc_debit無可更新資料, cardNo[%s]", tmpCardNo));
			}

			return;
		}
		/***********************************************************************/
		private boolean updateDbcCard() throws Exception {

			daoTable = "dbc_card";	
			updateSQL = " eng_name = ?,";
			updateSQL += " reg_bank_no = ?,";
			updateSQL += " mail_branch = ?,";
			updateSQL += " crt_bank_no = ?,";
			updateSQL += " vd_bank_no = ?,";
			updateSQL += " apply_no = ?,";
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_user  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	        whereStr = " where card_no = ?  ";  
	        setString(1, tmpEngName);
	        setString(2, tmpRegBankNo);
	        setString(3, tmpMailBranch);
	        setString(4, tmpCrtBankNo);
	        setString(5, tmpVdBankNo);
	        setString(6, tmpApplyNo);
	        setString(7,prgmId);
	        setString(8,prgmId);
	        setString(9, sysDate + sysTime);
	        setString(10, tmpCardNo);
	        
	        updateTable();
			
			if (notFound.equals("Y")) {
				tmpResponseCode = "D62";
				showLogMessage("I", "", String.format("ERROR: updateDbcCard(), card_no[%s]", tmpCardNo));
				return false;
			}
			return true;
		}

		/***********************************************************************/
		private boolean updateDbcIdno() throws Exception {
            			
			daoTable = "dbc_idno";
			updateSQL += " chi_name = ?,";
			updateSQL += " sex = ?,";
			updateSQL += " birthday = ?,";
			updateSQL += " eng_name = ?,";
			
			updateSQL += " home_area_code1 = ?,";
			updateSQL += " home_tel_no1 = ?,";
			updateSQL += " cellar_phone = ?,";
			
			updateSQL += " office_area_code1 = ?,";
			updateSQL += " office_tel_no1 = ?,";
			updateSQL += " office_tel_ext1 = ?,";			

			updateSQL += " resident_zip = ?,";
			updateSQL += " resident_addr1 = ?,";
			updateSQL += " resident_addr2 = ?,";
			updateSQL += " resident_addr3 = ?,";
			updateSQL += " resident_addr4 = ?,";
			updateSQL += " resident_addr5 = ?,";

			updateSQL += " mail_zip = ?,";
			updateSQL += " mail_addr1 = ?,";
			updateSQL += " mail_addr2 = ?,";
			updateSQL += " mail_addr3 = ?,";
			updateSQL += " mail_addr4 = ?,";
			updateSQL += " mail_addr5 = ?,";

			
			updateSQL += " company_zip = ?,";
			updateSQL += " company_addr1 = ?,";
			updateSQL += " company_addr2 = ?,";
			updateSQL += " company_addr3 = ?,";
			updateSQL += " company_addr4 = ?,";
			updateSQL += " company_addr5 = ?,";
			
			updateSQL += " e_mail_addr = ?,";
			updateSQL += " market_agree_base = ?,";
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";			
	        whereStr = " where id_no = ?  ";  
	        

	        setString(1, tmpChiName);
	        setString(2, tmpSex);
	        setString(3, tmpBirthday);
	        setString(4, tmpEngName);
	        
	        setString(5, tmpHomeAreaCode1);
	        setString(6, tmpHomeTelNo1);		    
	        setString(7, tmpCellarPhone);
	        
	        setString(8, tmpOfficeAreaCode1);
	        setString(9, tmpOfficeTelNo1);
	        setString(10, tmpOfficeTelEWxt1);

	        setString(11, tmpResidentZip);
	        setString(12, tmpResidentAddr1);
	        setString(13, tmpResidentAddr2);
	        setString(14, tmpResidentAddr3);
	        setString(15, tmpResidentAddr4);
	        setString(16, tmpResidentAddr5);

	        setString(17, tmpMailZip);
	        setString(18, tmpMailAddr1);
	        setString(19, tmpMailAddr2);
	        setString(20, tmpMailAddr3);
	        setString(21, tmpMailAddr4);
	        setString(22, tmpMailAddr5);

	        setString(23, tmpCompanyZip);
	        setString(24, tmpCompanyAddr1);
	        setString(25, tmpCompanyAddr2);
	        setString(26, tmpCompanyAddr3);
	        setString(27, tmpCompanyAddr4);
	        setString(28, tmpCompanyAddr5);

	        setString(29, tmpEMailAddr);
	        setString(30, tmpMarketAgreeBase);		    
	        setString(31,prgmId);
	        setString(32, sysDate + sysTime);
	        setString(33, tmpApplyId);

			updateTable();
			
			if (notFound.equals("Y")) {
				tmpResponseCode = "D63";
				showLogMessage("I", "", String.format("ERROR: updateDbcIdno(), id_no[%s]", tmpApplyId));
				return false;
			}
			return true;
		}

		/***********************************************************************/
		private boolean updateDbaAcno() throws Exception {
            

		  int columnRC = 0;
			
		   daoTable = "dba_acno";
//		   updateSQL = " revolve_int_rate = ?,";		
		   if (tmpBillApplyFlag.equals("1")||tmpBillApplyFlag.equals("2")||tmpBillApplyFlag.equals("3")) {
			   updateSQL += " bill_apply_flag = ?,";
	        }
	        else if (tmpBillApplyFlag.equals("4")) {
	           updateSQL += " stat_send_internet = ?,";
	        } 	      
	        else	        
	        {
	        	columnRC = -1 ;
	        }
				
	        updateSQL += " mod_pgm  = ? ,";
	        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	        whereStr = " where acct_no = ?  ";  


//	        setDouble(1, tmpdRcrateDay);
	        if (tmpBillApplyFlag.equals("1")||tmpBillApplyFlag.equals("2")||tmpBillApplyFlag.equals("3")) {
	           setString( 1, tmpBillApplyFlag);
	        }
	        else if (tmpBillApplyFlag.equals("4")) {
	           setString( 1, "Y");
	        }
	      	        	      
	        setString(columnRC + 2,prgmId);
	        setString(columnRC + 3, sysDate + sysTime);
	        setString(columnRC + 4, tmpSavingActno);

			updateTable();
			
			if (notFound.equals("Y")) {
				tmpResponseCode = "D64";
				showLogMessage("I", "", String.format("ERROR: updateDbaAcno(), acct_no[%s]", tmpSavingActno));
				return false;
			}
			return true;
		}
		
		/***********************************************************************/
		void insertFileCtl1(String fileName) throws Exception {

			setValue("file_name", fileName);
			setValue("crt_date", sysDate);
			setValue("trans_in_date", sysDate);
			daoTable = "crd_file_ctl";
			insertTable();
		}

		/***********************************************************************/
		void insertFileCtl() throws Exception {
			setValue("file_name", hNcccFilename);
			setValue("crt_date", sysDate);
			setValueInt("head_cnt", hRecCnt1);
			setValueInt("record_cnt", hRecCnt1);
			setValue("trans_in_date", sysDate);
			daoTable = "crd_file_ctl";
			insertTable();
			if (dupRecord.equals("Y")) {
				daoTable = "crd_file_ctl";
				updateSQL = "head_cnt = ?,";
				updateSQL += " record_cnt = ?,";
				updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
				whereStr = "where file_name = ? ";
				setInt(1, hRecCnt1);
				setInt(2, hRecCnt1);
				setString(3, hNcccFilename);
				updateTable();
				if (notFound.equals("Y")) {
					comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
				}
			}
		}
		
		/***********************************************************************/
		private boolean insertDbcPreTmp(DbcPreTmp tmp) throws Exception{
			extendField = "DBC_PRE_TMP.";
			setValue("DBC_PRE_TMP.EMBOSS_DATE", tmp.embossDate);
			setValue("DBC_PRE_TMP.BATCHNO", preBatchno);
			setValueDouble("DBC_PRE_TMP.RECNO", preRecno);
			setValue("DBC_PRE_TMP.APPLY_NO", tmp.applyNo);
			setValue("DBC_PRE_TMP.SOURCE", tmp.source);
			setValue("DBC_PRE_TMP.ACT_NO", tmp.actNo);
			setValue("DBC_PRE_TMP.CARD_REF_NUM", tmp.cardRefNum);
			setValue("DBC_PRE_TMP.TRACK2_TYPE", tmp.track2Type);
			setValue("DBC_PRE_TMP.GROUP_CODE", tmp.groupCode);
			setValue("DBC_PRE_TMP.APPLY_SOURCE", tmp.applySource);
			setValue("DBC_PRE_TMP.ENG_NAME", tmp.engName);
			setValue("DBC_PRE_TMP.BILL_APPLY_FLAG", tmp.billApplyFlag);
			setValue("DBC_PRE_TMP.REG_BANK_NO", tmp.regBankNo);
			setValue("DBC_PRE_TMP.MAIL_BRANCH", tmp.mailBranch);
			setValue("DBC_PRE_TMP.CRT_BANK_NO", tmp.crtBankNo);
			setValue("DBC_PRE_TMP.VD_BANK_NO", tmp.vdBankNo);
			setValue("DBC_PRE_TMP.HOME_AREA_CODE1", tmp.homeAreaCode1);
			setValue("DBC_PRE_TMP.HOME_TEL_NO1", tmp.homeTelNo1);
			setValue("DBC_PRE_TMP.CELLAR_PHONE", tmp.cellarPhone);
			setValue("DBC_PRE_TMP.OFFICE_AREA_CODE1", tmp.officeAreaCode1);
			setValue("DBC_PRE_TMP.OFFICE_TEL_NO1", tmp.officeTelNo1);
			setValue("DBC_PRE_TMP.OFFICE_TEL_EXT1", tmp.officeTelExt1);
			setValue("DBC_PRE_TMP.APPLY_ID", tmp.applyId);
			setValue("DBC_PRE_TMP.BIRTHDAY", tmp.birthday);
			setValue("DBC_PRE_TMP.SEX", tmp.sex);
			setValue("DBC_PRE_TMP.STMT_CYCLE", tmp.stmtCycle);
			setValue("DBC_PRE_TMP.DISCOUNT_CODE", tmp.discountCode);
			setValue("DBC_PRE_TMP.E_MAIL_ADDR", tmp.eMailAddr);
			setValue("DBC_PRE_TMP.CARD_NO", tmp.cardNo);
			setValue("DBC_PRE_TMP.MARKET_AGREE_BASE", tmp.marketAgreeBase);
			setValue("DBC_PRE_TMP.MAIL_TYPE", tmp.mailType);
			setValue("DBC_PRE_TMP.CHI_NAME", tmp.chiName);
			setValue("DBC_PRE_TMP.RESIDENT_ZIP", tmp.residentZip);
			setValue("DBC_PRE_TMP.RESIDENT_ADDR1", tmp.residentAddr1);
			setValue("DBC_PRE_TMP.RESIDENT_ADDR2", tmp.residentAddr2);
			setValue("DBC_PRE_TMP.RESIDENT_ADDR3", tmp.residentAddr3);
			setValue("DBC_PRE_TMP.RESIDENT_ADDR4", tmp.residentAddr4);
			setValue("DBC_PRE_TMP.RESIDENT_ADDR5", tmp.residentAddr5);
			setValue("DBC_PRE_TMP.MAIL_ZIP", tmp.mailZip);
			setValue("DBC_PRE_TMP.MAIL_ADDR1", tmp.mailAddr1);
			setValue("DBC_PRE_TMP.MAIL_ADDR2", tmp.mailAddr2);
			setValue("DBC_PRE_TMP.MAIL_ADDR3", tmp.mailAddr3);
			setValue("DBC_PRE_TMP.MAIL_ADDR4", tmp.mailAddr4);
			setValue("DBC_PRE_TMP.MAIL_ADDR5", tmp.mailAddr5);
			setValue("DBC_PRE_TMP.COMPANY_ZIP", tmp.companyZip);
			setValue("DBC_PRE_TMP.COMPANY_ADDR1", tmp.companyAddr1);
			setValue("DBC_PRE_TMP.COMPANY_ADDR2", tmp.companyAddr2);
			setValue("DBC_PRE_TMP.COMPANY_ADDR3", tmp.companyAddr3);
			setValue("DBC_PRE_TMP.COMPANY_ADDR4", tmp.companyAddr4);
			setValue("DBC_PRE_TMP.COMPANY_ADDR5", tmp.companyAddr5);
			setValue("DBC_PRE_TMP.CHECK_CODE", tmp.checkCode);
			setValue("DBC_PRE_TMP.crt_date", sysDate);
			setValue("DBC_PRE_TMP.crt_user", prgmId);
			setValue("DBC_PRE_TMP.mod_user", prgmId);
			setValue("DBC_PRE_TMP.mod_time", sysDate + sysTime);
			setValue("DBC_PRE_TMP.mod_pgm", prgmId);
		    daoTable = "DBC_PRE_TMP";
		    int cnt = insertTable();
		    if (cnt <= 0) {
		    	if ("Y".equals(dupRecord))
		    		showLogMessage("I", "", String.format("Error: insertDbcPreTmp()[duplicate], BATCHNO=[%s] ,RECNO=[%s]", preBatchno,preRecno));
		    	else 
		    		showLogMessage("I", "", String.format("Error: insertDbcPreTmp(), BATCHNO=[%s] ,RECNO=[%s]", preBatchno,preRecno));
		    	return false;
		    }
		    return true;

		}
		
		/***********************************************************************/
		private void updateDbcPreTmp() throws Exception{
			daoTable = "DBC_PRE_TMP";
			updateSQL = "CHECK_CODE = ?,";
			updateSQL += " MOD_USER = ?,";
			updateSQL += " MOD_PGM = ?,";
			updateSQL += " MOD_TIME = sysdate ";
			whereStr = "where BATCHNO = ? and RECNO = ? ";
			setString(1, tmpResponseCode);
			setString(2, prgmId);
			setString(3, prgmId);
			setString(4, tmpBatchno);
			setString(5, tmpRrecno);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("I", "",String.format("update DBC_PRE_TMP not found ,BATCHNO=[%s] ,RECNO=[%s]", tmpBatchno, tmpRrecno));
			}
		}
		
		

		/****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		
		/****************************************************************************/
		String getResponseCode() throws Exception {
			String tmpRC=SUCCESS;
			
			// 檔案的卡號是否存在送製卡檔(Debit)
			if(!chkData("D40"))
				tmpRC="D40";
			
			/* 2022/06/25 Justin 刪除資料檢核
			//檔案的卡號是否已寫入主檔
			else if(chkData("D41"))
				tmpRC="D41";
			
			//檔案的卡號是否存在DEBIT卡申請檔
			else if(!chkData("D42"))
				tmpRC="D42";
			
			//檔案的卡號是否尚未成功異動過資料
			else if(chkData("D43"))
				tmpRC="D43";
			
			//檔案的卡號是否存在DEBIT卡片基本資料檔
			else if(!chkData("D44"))
				tmpRC="D44";
			
			//檔案的金融帳號是否跟DEBIT卡片基本資料檔的金融帳號相同
			else if(!chkData("D45"))
				tmpRC="D45";
			
			//檔案的身分證字號是否跟DEBIT卡人基本資料檔的身分證字號相同
			else if(!chkData("D46"))
				tmpRC="D46";
			
			//檔案的金融帳號是否跟DEBIT帳戶基本資料檔的金融帳號相同
			else if(!chkData("D47"))
				tmpRC="D47";
			*/
	        
			//檔案的第二軌型態是否正確
			if(!tmpAcctType2.equals("V"))
				tmpRC ="D48";
			
			//檔案的申請類別是否正確
			if(!tmpApplyType.equals("Q"))
				tmpRC ="D49";

			return tmpRC;
		}
		
		/***********************************************************************/
		boolean chkData(String args) throws Exception {

	         switch (args) {
	         case "D40":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_emboss ";
	 			sqlCmd += " where card_no = ? ";
//	 			sqlCmd += " and in_main_date <> '' ";	 			
	 			setString(1, tmpCardNo);
	            break; 
	             
	         case "D41":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_emboss ";
	 			sqlCmd += " where card_no = ? ";
	 			sqlCmd += " and in_main_date = '' ";	 			
	 			setString(1, tmpCardNo);
	            break; 
	             
	         case "D42":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_debit ";
	 			sqlCmd += " where card_no = ? ";	 			
	 			setString(1, tmpCardNo);
	            break; 
	             
	         case "D43":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_debit ";
	 			sqlCmd += " where card_no = ? ";	 	
	 			sqlCmd += " and end_ibm_date <> '' ";			
	 			setString(1, tmpCardNo);
	            break; 
	             
	         case "D44":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_card ";
	 			sqlCmd += " where card_no = ? ";	 			
	 			setString(1, tmpCardNo);
	            break; 
	            
	         case "D45":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from dbc_card ";
	 			sqlCmd += " where card_no = ? ";	
	 			sqlCmd += " and  acct_no = ? ";	 			
	 			setString(1, tmpCardNo);	 			
	 			setString(2, tmpSavingActno);
	 			
	            break; 
	            
	         case "D46":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from DBC_CARD dc ,DBC_IDNO di ";
	 			sqlCmd += " where dc.card_no = ? ";			
	 			sqlCmd += " and di.id_no = ? ";					
	 			sqlCmd += " and dc.ID_P_SEQNO = di.ID_P_SEQNO  ";			
	 			setString(1, tmpCardNo);	 			
	 			setString(2, tmpApplyId);	 		
	 			
	            break; 
	            
	         case "D47":

	 			sqlCmd = " select count(*) r_cnt";
	 			sqlCmd += " from DBC_CARD dc , DBA_ACNO da ";
	 			sqlCmd += " where dc.card_no = ? ";			
	 			sqlCmd += " and da.acct_no = ? ";					
	 			sqlCmd += " and dc.P_SEQNO = da.P_SEQNO  ";			
	 			setString(1, tmpCardNo);	 			
	 			setString(2, tmpSavingActno);	 		
	 			
	            break; 
	            
			case "D52":

				sqlCmd =  " select count(*) r_cnt";
				sqlCmd += " from DBA_ACNO ";
				sqlCmd += " where acct_no = ? ";
				setString(1, tmpSavingActno);

				break;

			case "D53":

				sqlCmd =  " select count(*) r_cnt";
				sqlCmd += " from  DBC_EMBOSS ";
				sqlCmd += " where card_no = ? ";
				sqlCmd += " AND   PREFAB_CANCEL_FLAG = 'Y' ";
				setString(1, tmpCardNo);

				break;
	            
	         }
	        selectTable();
			int recordCnt = 0;
			recordCnt=getValueInt("r_cnt");
	        if (recordCnt > 0) {
				return true;
			}else {
				return false;
			}
			
				
		}
	  
	    /***********************************************************************/
	    void createTextFile() throws Exception {
	        
	        data.cardKind = "V";	        
	        data.cardSource = "Q";	        
	        data.savingActno = tmpSavingActno;
	        data.cardNo = tmpCardNo;
	        data.cardRefNum = tmpCardRefNnum;
	        data.validTo = tmpNewEndDate;
	       	data.tscCardNo = "";
	        data.responseDate = sysDate;
	        data.responseCode = tmpResponseCode;
	        data.ap1ApplyDate = sysDate;
	        
	        
	        buf = data.allText();
	        lpar2.add(comcr2.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

	        return;
	    }
	    /***********************************************************************/
	    void selectNewEndDate() throws Exception {
			
			sqlCmd = " select substr(new_end_date,3,4) new_end_date ";
			sqlCmd += " from dbc_card ";
			sqlCmd += " where card_no = ? ";			
 			setString(1, tmpCardNo);	 		
			
			selectTable();
			if (notFound.equals("Y")) {
				tmpNewEndDate = "";
			}
		
			tmpNewEndDate = getValue("new_end_date");
				
		}
	    /***********************************************************************/
	    void selectRcrateDay() throws Exception {
			
	    	double tmpdRevolveIntRateYear = Double.valueOf(tmpRevolveIntRateYear.substring(0,2)) + (Double.valueOf(tmpRevolveIntRateYear.substring(2,4))/100);
			sqlCmd = " select rcrate_day ";
			sqlCmd += " from ptr_rcrate ";
			sqlCmd += " where rcrate_year = ? ";			
			setDouble(1, tmpdRevolveIntRateYear);
 			
			
			selectTable();
			if (notFound.equals("Y")) {
				tmpdRcrateDay = 0;
			} else {
				tmpdRcrateDay = Double.valueOf(getValue("rcrate_day"));
			}
				
		}
		/****************************************************************************/	
		public static void main(String[] args) throws Exception {
			DbcF035 proc = new DbcF035();
			int retCode = proc.mainProcess(args);
			proc.programEnd(retCode);
		}

		
		/****************************************************************************/
		void renameFile(String removeFileName) throws Exception {
			String tmpstr1 = fileFolderPath + removeFileName;
			String tmpstr2 = fileFolderPath +"backup/" + removeFileName + "." + sysDate;

			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

		/****************************************************************************/
		String[] getFieldValue(String rec, int[] parm) {
			int x = 0;
			int y = 0;
			byte[] bt = null;
			String[] ss = new String[parm.length];
			try {
				bt = rec.getBytes("MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			for (int i : parm) {
				try {
					ss[y] = new String(bt, x, i, "MS950");
				} catch (Exception e) {
					showLogMessage("I", "", comc.getStackTraceString(e));
				}
				y++;
				x = x + i;
			}
			return ss;
		}

		/****************************************************************************/
		
		private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
			Map<String, Object> map = new HashMap<>();
			int i = 0;
			for (String s : dt) {
				map.put(s.trim(), row[i]);
				i++;
			}
			return map;
		}
	
		   /***********************************************************************/
	    class buf1 {
	        String cardKind = "";
	        String cardSource = "";
	        String savingActno = "";
	        String cardNo = "";
	        String cardRefNum = "";
	        String validTo = "";
	        String tscCardNo = "";
	        String responseDate = "";
	        String responseCode = "";
	        String ap1ApplyDate = "";

	        String allText() throws UnsupportedEncodingException {
	            String rtn = "";
	            rtn += comc.fixLeft(cardKind      , 1);
	            rtn += comc.fixLeft(cardSource    , 1);
	            rtn += comc.fixLeft(savingActno   , 13);
	            rtn += comc.fixLeft(cardNo    , 16);
	            rtn += comc.fixLeft(cardRefNum   , 2);
	            rtn += comc.fixLeft(validTo   , 4);
	            rtn += comc.fixLeft(tscCardNo    , 20);
	            rtn += comc.fixLeft(responseDate  , 8);
	            rtn += comc.fixLeft(responseCode  , 3);
	            rtn += comc.fixLeft(ap1ApplyDate , 8);           
	            return rtn;
	        }
	    }
	    
	    /***********************************************************************/
	    class DbcPreTmp { //DBC_PRE_TMP
	    	String embossDate = ""; //EMBOSS_DATE
	    	String batchno = ""; //BATCHNO
	    	String recno = ""; //RECNO
	    	String applyNo = ""; //APPLY_NO
	    	String source = "1"; //SOURCE
	    	String actNo = ""; //ACT_NO
	    	String cardRefNum = ""; //CARD_REF_NUM
	    	String track2Type = ""; //TRACK2_TYPE
	    	String groupCode = ""; //GROUP_CODE
	    	String applySource = ""; //APPLY_SOURCE
	    	String engName = ""; //ENG_NAME
	    	String billApplyFlag = ""; //BILL_APPLY_FLAG
	    	String regBankNo = ""; //REG_BANK_NO
	    	String mailBranch = ""; //MAIL_BRANCH
	    	String crtBankNo = ""; //CRT_BANK_NO
	    	String vdBankNo = ""; //VD_BANK_NO
	    	String homeAreaCode1 = ""; //HOME_AREA_CODE1
	    	String homeTelNo1 = ""; //HOME_TEL_NO1
	    	String cellarPhone = ""; //CELLAR_PHONE
	    	String officeAreaCode1 = ""; //OFFICE_AREA_CODE1
	    	String officeTelNo1 = ""; //OFFICE_TEL_NO1
	    	String officeTelExt1 = ""; //OFFICE_TEL_EXT1
	    	String applyId = ""; //APPLY_ID
	    	String birthday = ""; //BIRTHDAY
	    	String sex = ""; //SEX
	    	String stmtCycle = ""; //STMT_CYCLE
	    	String discountCode = ""; //DISCOUNT_CODE
	    	String eMailAddr = ""; //E_MAIL_ADDR
	    	String cardNo = ""; //CARD_NO
	    	String marketAgreeBase = ""; //MARKET_AGREE_BASE
	    	String mailType = ""; //MAIL_TYPE
	    	String chiName = ""; //CHI_NAME
	    	String residentZip = ""; //RESIDENT_ZIP
	    	String residentAddr1 = ""; //RESIDENT_ADDR1
	    	String residentAddr2 = ""; //RESIDENT_ADDR2
	    	String residentAddr3 = ""; //RESIDENT_ADDR3
	    	String residentAddr4 = ""; //RESIDENT_ADDR4
	    	String residentAddr5 = ""; //RESIDENT_ADDR5
	    	String mailZip = ""; //MAIL_ZIP
	    	String mailAddr1 = ""; //MAIL_ADDR1
	    	String mailAddr2 = ""; //MAIL_ADDR2
	    	String mailAddr3 = ""; //MAIL_ADDR3
	    	String mailAddr4 = ""; //MAIL_ADDR4
	    	String mailAddr5 = ""; //MAIL_ADDR5
	    	String companyZip = ""; //COMPANY_ZIP
	    	String companyAddr1 = ""; //COMPANY_ADDR1
	    	String companyAddr2 = ""; //COMPANY_ADDR2
	    	String companyAddr3 = ""; //COMPANY_ADDR3
	    	String companyAddr4 = ""; //COMPANY_ADDR4
	    	String companyAddr5 = ""; //COMPANY_ADDR5
	    	String checkCode = ""; //CHECK_CODE
	    }
	}
