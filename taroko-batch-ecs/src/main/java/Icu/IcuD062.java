/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/19  V1.00.00    Pino      program initial                           *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *
 *  109/07/03  V1.01.02    JustinWu                  change the program name*
 * *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/14  V1.00.05   Wilson       資料夾名稱修改為小寫                                                                        *
 *  109/09/03  V1.00.06   Wilson       測試修改                                                                                               *
 *  109/09/11  V1.00.07   Wilson       新增卡片序號                                                                                        *
 *  109/09/14  V1.00.08   Wilson       batchno、recno調整                                                                  *
 *  109/09/25  V1.00.09   Wilson       insert dbc_emboss新增class_code = "C"    *
 *  109/09/29  V1.00.10   Wilson       無檔案不秀error、讀檔不綁檔名日期                                            *
 *  109/09/30  V1.00.11   Wilson       營業日才執行                                                                                        *
 *  109/10/12  V1.00.12   Wilson       檔名日期改營業日                                                                                *
 *  109/10/19  V1.00.13   Wilson       錯誤報表FTP                               *
 *  109-10-19  V1.00.14    shiyuqi       updated for project coding standard     *
 *  109/12/30  V1.00.15   Wilson       新增insert crd_seqno_log                  *
 *  110/12/08  V1.00.16   Wilson       錯誤訊息調整                                                                                          *
 *  111/02/14  V1.00.17   Ryan         big5 to MS950                                           *
 ******************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;

public class IcuD062 extends AccessDAO {
	private final String progname = "設一科VD預製卡(VTM)製卡檔處理程式  111/02/14 V1.00.17";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	VDCardLayout data = new VDCardLayout();

	int debug = 0;

	String prgmId = "IcuD062";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	// col詳VD製卡檔格式
	protected final String dT1Str = " col1 ,col2 ,col3 ,col4 ,col5 ,col6 ,col7 ,col8 ,col9 ,col10 ,col11 ,col12 ,col13 ,col14 ,col15"
			+ " ,col16 ,col17 ,col18 ,col19 ,col20 ,col21 ,col22 ,col23 ,col24 ,col25 ,col26 ,col27 ,col28 ,col29 ,col30"
			+ " ,col31 ,col32 ,col33 ,col34 ,col35 ,col36 ,col37 ,col38 ,col39 ,col40 ,col41 ,col42 ,col43 ,col44 ,col45"
			+ " ,col46 ,col47 ,col48 ,col49 ,col50 ,col51 ,col52 ,col53 ,col54 ,col55 ,col56 ,col57 ,col58 ,col59 ,col60 "
			+ " ,col61 ,col62 ,col63 ,col64 ,col65 ,col66 ,col67 ,col68 ,col69 ,col70 ,col71 ,col72 ,col73 ,col74 ,col75"
			+ " ,col76 ,col77 ,col78 ,col79 ,col80 ,col81 ,col82 ,col83 ,col84 ,col85 ,col86 ,col87 ,col88 ,col89 ,col90"
			+ " ,col91 ,col92 ,col93 ,col94 ,col95 ,col96 ,col97 ,col98 ,col99 ,col100 ,col101 ,col102 ,col103 ,col104 ,col105"
			+ " ,col106 ,col107 ,col108 ,col109 ,col110 ,col111 ,col112 ,col113 ,col114 ,col115 ,col116 ,col117 ,col118 ,col119 ,col120"
			+ " ,col121 ,col122 ,col123 ,col124 ,col125 ,col126 ,col127 ,col128 ,col129 ,col130 ,col131 ,col132 ,col133 ,col134 ,col135"
			+ " ,col136 ,col137 ,col138 ,col139 ,col140 ,col141 ,col142 ,col143 ,col144 ,col145 ,col146 ,col147 ,col148 ,col149 ,col150"
			+ " ,col151 ,col152 ,col153 ,col154 ,col155 ,col156 ,col157 ,col158 ,col159 ,col160 ,col161 ,col162 ,col163 ,col164 ,col165"
			+ " ,col166 ,col167 ,col168 ,col169 ,col170 ,col171 ,col172 ,col173 ,col174 ,col175 ,col176 ,col177 ,col178 ,col179 ,col180"
			+ " ,col181 ,col182 ,col183 ,col184 ,col185 ,col186 ,col187 ,col188 ,col189 ,col190 ,col191 ,col192 ,col193 ,col194 ,col195"
			+ " ,col196 ,col197 ,col198 ,col199 ,col200 ,col201 ,col202 ,col203 ,col204 ,col205 ,col206 ,col207 ,col208 ,col209";

	protected final int[] dt1Length = { 1, 7, 1, 13, 1, 10, 1, 2, 1, 37, 1, 2, 104, 1, 8, 20, 2, 8, 10, 8, 3, 3, 1, 8,
			8, 60, 16, 16, 16, 16, 16, 16, 16, 16, 24, 24, 24, 24, 24, 24, 24, 24, 16, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2,
			2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2,
			2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 33, 4, 96, 6, 1, 1, 19, 1, 2, 1, 2, 5,
			2, 1, 2, 2, 1, 1, 28, 1, 19, 1, 3, 1, 1, 1, 16, 1, 26, 1, 4, 3, 1, 4, 8, 2, 3, 2, 1, 3, 1, 1, 16, 1, 4, 3,
			1, 4, 3, 5, 1, 11, 7, 1, 1, 4, 3, 3, 3, 23, 1, 4, 1, 1, 3, 3, 1, 41, 3, 5, 10, 1, 10, 1, 16, 33, 26, 5, 18,
			18, 9, 9, 9, 9, 2, 4, 4, 4, 33, 3, 33, 3, 97, 3, 1, 3 };

	String fileDate = "";
	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	String tmpBatchno = "";
	int tmpRecno = 0;
	int hRecCnt1 = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	String errCode;
	String hBusiBusinessDate = "";

	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

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
			if (args.length > 1) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());
			
			selectPtrBusinday();
			
			if (args.length == 0) {
				fileDate = hBusiBusinessDate;
			} else if (args.length == 1) {
				fileDate = args[0];
			}
			if (fileDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}
			
			hModUser = comc.commGetUserID();
			
			if (checkPtrHoliday() != 0) {
				exceptExit = 0;
				comc.errExit("今日為假日,不執行此程式", "");
	        }
			
			openFile();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + totalInputFile + "],[" + totalOutputFile + "]");
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
	      setString(1, fileDate);
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

	  /***********************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (getFileName.length() != 30)
				continue;
			if (!getFileName.substring(0, 18).equals("VD_P_VTM_MAKECARD_"))
				continue;

			if (!getFileName.substring(18, 26).equals(fileDate))
				continue;

			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
//			showLogMessage("I", "", "無檔案可處理");
			
			comcr.hCallErrorDesc = "Error : 無檔案可處理";
			comcr.errRtn("Error : 無檔案可處理", "處理日期 = " + fileDate , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName;

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

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

//		sqlCmd = "select max(batchno) as tmp_batchno";
//		sqlCmd += " from dbc_emboss ";
//		int recordCnt = selectTable();
//		tmpBatchno = getValue("tmp_batchno");
//		if (tmpBatchno.subSequence(0, 8).equals(sysDate)) {
//			tmpBatchno = comc.str2int(tmpBatchno) + 1 + "";
//		} else {
//			tmpBatchno = sysDate.substring(2, 8) + "01";
//		}
		
		getBatchno();

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
//            if (rec.length() == 1700) {
			errCode = "";
			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
//            }
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			hRecCnt1 = totalOutputFile;
			insertFileCtl(hNcccFilename);
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);
		hRecCnt1 = totalInputFile;
		insertFileCtl(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
	void getBatchno() throws Exception {
		
        String tmpBatchno1 = "";
        long tmpNo = 0;
        String tmpDate = "";

        sqlCmd = "select to_char(sysdate,'yymmdd') tmpDate ";
        sqlCmd += "  from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tmpDate = getValue("tmpDate");
        }

        sqlCmd  = "select distinct(max(batchno)) tmpBatchno ";
        sqlCmd += " from dbc_emboss  ";
        sqlCmd += "where batchno  like ? || '%' ";
        sqlCmd += "  and nccc_type     = '1' ";
        sqlCmd += "  and to_nccc_date  = '' ";
        setString(1, tmpDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            tmpBatchno = getValue("tmpBatchno");
        }
        if (tmpBatchno.length() > 0) {
            sqlCmd = "select max(recno) tmpRecno ";
            sqlCmd += " from dbc_emboss  ";
            sqlCmd += "where batchno = ? ";
            setString(1, tmpBatchno);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                tmpRecno = getValueInt("tmpRecno");
            }
            return;
        } else {
            sqlCmd  = "select distinct(max(batchno)) tmpBatchno ";
            sqlCmd += " from dbc_emboss  ";
            sqlCmd += "where batchno like ? || '%' ";
            setString(1, tmpDate);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                tmpBatchno = getValue("tmpBatchno");
            }
            if (tmpBatchno.length() > 0) {
                tmpNo = comcr.str2long(tmpBatchno) + 1;
                tmpBatchno1 = String.format("%08d", tmpNo);
            } else {
                tmpNo = 1;
                tmpBatchno1 = String.format("%s%02d", tmpDate, tmpNo);
            }

            tmpBatchno = tmpBatchno1;
        }

        if (debug == 1)
            showLogMessage("I", "", "  888 Batchno=[" + tmpBatchno + "]");

        return;
    }

    /***********************************************************************/	
	private void moveData(Map<String, Object> map) throws Exception {
		String tmpChar = "";
		data.initData();
		data.cardNo = (String) map.get("col120"); // 卡號(每四位間隔一空白)
		data.cardNo = data.cardNo.substring(0, 4) + data.cardNo.substring(5, 9) + data.cardNo.substring(10, 14)
				+ data.cardNo.substring(15, 19);
		data.cardNo = data.cardNo.trim();
		if (debug == 1)
			System.out.println("卡號=" + data.cardNo);

		data.validFmYy = (String) map.get("col124"); // 有效起始年份
		data.validFmYy = data.validFmYy.trim();
		if(data.validFmYy.equals("00")) {			
			data.validFmYy = sysDate.substring(2,4);
		}
		if (debug == 1)
			System.out.println("有效起始年份=" + data.validFmYy);

		data.validFmMm = (String) map.get("col122"); // 有效起始月份
		data.validFmMm = data.validFmMm.trim();
		if(data.validFmMm.equals("00")) {			
			data.validFmMm = sysDate.substring(4,6);
		}
		if (debug == 1)
			System.out.println("有效起始月份=" + data.validFmMm);

		data.validToYy = (String) map.get("col128"); // 有效迄止年份
		data.validToYy = data.validToYy.trim();
		if (debug == 1)
			System.out.println("有效迄止年份=" + data.validToYy);

		data.validToMm = (String) map.get("col126"); // 有效迄止月份
		data.validToMm = data.validToMm.trim();
		if (debug == 1)
			System.out.println("有效迄止月份=" + data.validToMm);

		data.actNo = (String) map.get("col4"); // 帳號
		data.actNo = data.actNo.trim();
		if (debug == 1)
			System.out.println("帳號=" + data.actNo);
		
		data.cardNumber = (String) map.get("col8"); // 卡片序號
		data.cardNumber = data.cardNumber.trim();
		if (debug == 1)
			System.out.println(" 卡片序號=" + data.cardNumber);

		if (selectDbcCard() == 1)
			return;
		if (selectDbcEmboss() == 1)
			return;
		insertDbcEmboss();

		commitDataBase();
		return;
	}

	/***********************************************************************/
	int outPutTextFile() throws Exception {
//		int fileNo = 0;
//
//        sqlCmd  = "select max(substr(file_name, 31, 2)) file_no";
//        sqlCmd += " from crd_file_ctl  ";
//        sqlCmd += " where file_name like ?";
//        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
//        setString(1, "VD_P_VTM_MAKECARD_ERR." + "%" + ".TXT");
//        
//        if (selectTable() > 0) 
//        	fileNo = getValueInt("file_no");
//         
		hNcccFilename = String.format("VD_P_VTM_MAKECARD_ERR_%s.TXT", fileDate);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//		sqlCmd += " and crt_date = to_char(sysdate,'yyyymmdd') ";
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
	void createErrReport() throws Exception {
		ncccData1 = new Buf1();

		ncccData1.cardNo = data.cardNo;

		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "該卡號已存在VD卡片資料檔中");
			break;
		case "2":
			ncccData1.errReason = String.format("%-200s", "該卡號已存在VD製卡暫存檔中");
			break;
		}

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/***********************************************************************/
	int selectDbcCard() throws Exception {
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt > 0) {
				if (debug == 1)
					showLogMessage("I", "", "Error:該卡號已存在VD卡片資料檔中");
				errCode = "1";
				createErrReport();
				totalOutputFile++;
				return 1;
			}

		}
		return 0;
	}

	/***********************************************************************/
	int selectDbcEmboss() throws Exception {
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from dbc_emboss ";
		sqlCmd += " where apply_source = 'P' ";
		sqlCmd += " and card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt > 0) {
				if (debug == 1)
					showLogMessage("I", "", "Error:該卡號已存在VD製卡暫存檔中");
				errCode = "2";
				createErrReport();
				totalOutputFile++;
				return 1;
			}

		}
		return 0;
	}

	/***********************************************************************/
	void insertDbcEmboss() throws Exception {
		tmpRecno++;
		String tmpCardType = "";
		String tmpUnitCode = "";
		String tmpGroupCode = "";
		String tmpSourceCode = "";

		sqlCmd = "select group_code,card_type ";
		sqlCmd += " from crd_cardno_range ";
		sqlCmd += " where bin_no = ? ";
		sqlCmd += " and beg_seqno <= ? ";
		sqlCmd += " and end_seqno >= ? ";
		setString(1, data.cardNo.substring(0, 6));
		setString(2, data.cardNo.substring(6, 15));
		setString(3, data.cardNo.substring(6, 15));
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			tmpGroupCode = getValue("group_code");
			tmpCardType = getValue("card_type");
		}
		if (notFound.equals("Y")) {
			String err1 = "select_crd_cardno_range error[notFound]!";
			String err2 = data.cardNo;
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}

		sqlCmd = "select unit_code,source_code ";
		sqlCmd += " from dbc_card_type ";
		sqlCmd += " where group_code = ? ";
		sqlCmd += " and card_type = ? ";
		setString(1, tmpGroupCode);
		setString(2, tmpCardType);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			tmpUnitCode = getValue("unit_code");
			tmpSourceCode = getValue("source_code");
		}
		if (notFound.equals("Y")) {
			String err1 = "select_dbc_card_type error[notFound]!";
			String err2 = "[" + tmpGroupCode + "]" + "[" + tmpCardType + "]";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}

		extendField = "dbem.";
		setValue("dbem.batchno", tmpBatchno);
		setValueInt("dbem.recno", tmpRecno);
		setValue("dbem.emboss_source", "1");
		setValue("dbem.card_type", tmpCardType);
		setValue("dbem.bin_no", data.cardNo.substring(0, 6));
		setValue("dbem.acct_type", "90");
		setValue("dbem.sup_flag", "0");
		setValue("dbem.unit_code", tmpUnitCode);
		setValue("dbem.card_no", data.cardNo);
		setValue("dbem.group_code", tmpGroupCode);
		setValue("dbem.source_code", tmpSourceCode);
		setValue("dbem.valid_fm", sysDate.substring(0, 2) + data.validFmYy + data.validFmMm + "01");
		setValue("dbem.valid_to", comm.lastdateOfmonth(sysDate.substring(0, 2) + data.validToYy + data.validToMm));
		setValue("dbem.apply_source", "P");
		setValue("dbem.act_no", data.actNo);
		setValue("dbem.card_ref_num", data.cardNumber);
		setValue("dbem.class_code", "C");
		setValue("dbem.crt_user", prgmId);
		setValue("dbem.crt_date", sysDate);
		setValue("dbem.mod_user", "");
		setValue("dbem.mod_time", sysDate + sysTime);
		setValue("dbem.mod_pgm", "");

		daoTable = "dbc_emboss";
		insertTable();
		if (dupRecord.equals("Y")) {
			String err1 = "insert_dbc_emboss error";
			String err2 = "卡號 = [" + data.cardNo + "]" ;
			comcr.errRtn(err1, err2, hCallBatchSeqno);
		}
		
		extendField = "crsl.";
		setValueInt("crsl.card_type_sort" , 0);
		setValue("crsl.bin_no"            , data.cardNo.substring(0, 6));
		setValue("crsl.SEQNO"             , data.cardNo.substring(6));
		setValue("crsl.card_type"         , tmpCardType );
		setValue("crsl.group_code"        , tmpGroupCode);
		setValue("crsl.card_flag"         , "1" );
		setValue("crsl.reserve"           , "Y");
		setValue("crsl.reason_code"       , "1");
		setValue("crsl.use_date"          , sysDate);
		setValue("crsl.use_id"            , javaProgram);
		setValue("crsl.card_item"         , tmpGroupCode + tmpCardType);
		setValue("crsl.unit_code"         , tmpGroupCode);
		setValue("crsl.trans_no"          , "");
		setValue("crsl.seqno_old"         , data.cardNo.substring(6, 15));
		setValue("crsl.CRT_DATE"          , sysDate);
		setValue("crsl.MOD_USER"          , prgmId);
		setValue("crsl.MOD_TIME"          , sysDate + sysTime);
		setValue("crsl.MOD_PGM"           , javaProgram);

		daoTable = "crd_seqno_log";

		insertTable();

		if (dupRecord.equals("Y")) {
		    String err1 = "insert crd_seqno_log error[dupRecord]";
		    String err2 = "BIN_NO = " + data.cardNo.substring(0, 6) + "，SEQNO = " + data.cardNo.substring(6);
		    comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertFileCtl(String filename) throws Exception {
		setValue("file_name", filename);
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
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + hNcccFilename);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(hNcccFilename);          
	      }
	  }
	
	/****************************************************************************/
	public int insertEcsNotifyLog(String fileName) throws Exception {
	      setValue("crt_date", sysDate);
	      setValue("crt_time", sysTime);
	      setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	      setValue("obj_type", "3");
	      setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_name", "媒體檔名:" + fileName);
	      setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_desc2", "");
	      setValue("trans_seqno", commFTP.hEflgTransSeqno);
	      setValue("mod_time", sysDate + sysTime);
	      setValue("mod_pgm", javaProgram);
	      daoTable = "ecs_notify_log";

	      insertTable();

	      return (0);
	  }

	/****************************************************************************/
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		IcuD062 proc = new IcuD062();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String cardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(errReason, 200);
			rtn += fixLeft(date, 8);
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

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.cardNo = comc.subMS950String(bytes, 12, 28);
		ncccData1.errReason = comc.subMS950String(bytes, 28, 228);
		ncccData1.date = comc.subMS950String(bytes, 228, 236);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;

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
	private Map processDataRecord(String[] row, String[] DT) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : DT) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}

	/***********************************************************************/
	class VDCardLayout extends hdata.BaseBin {
		public String cardNo = ""; // 卡號
		public String validFmYy = ""; // 有效起始年份
		public String validFmMm = ""; // 有效起始月份
		public String validToYy = ""; // 有效迄止年份
		public String validToMm = ""; // 有效迄止月份
		public String actNo = ""; // 帳號
		public String cardNumber = ""; // 卡片序號 

		@Override
		public void initData() {
			cardNo = ""; // 卡號
			String validFmYy = ""; // 有效起始年份
			String validFmMm = ""; // 有效起始月份
			String validToYy = ""; // 有效迄止年份
			String validToMm = ""; // 有效迄止月份
			String actNo = ""; // 帳號
			String cardNumber = ""; // 卡片序號 
		}

	}
}
