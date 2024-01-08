/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/10  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-11-23  V1.00.05  Yangbo       joint sql replace to parameters way    *
* 111-05-20  V1.00.06   Ryan        調整file_name查詢sql                      *
* 112-03-31  V1.00.07   Ryan        txt匯入改為excel匯入                                                               *
***************************************************************************/
package mktm02;

import mktm02.Mktm1050Func;
import ofcapp.AppMsg;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import Dxc.Util.SecurityUtil;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;
import taroko.com.TarokoUpload;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1050 extends BaseEdit {
  private String PROGNAME = "媒體檔案上傳作業處理程式108/09/10 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1050Func func = null;
  CommString commStr = new CommString();
  String rowid;
  String orgTabName = "mkt_uploadfile_ctl";
  String controlTaName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  InputStream inExcelFile = null;
  HSSFWorkbook wb = null;
  HSSFSheet sheet = null;
  HSSFRow row = null;
  XSSFRow dummyRow = null;
  HSSFCell cell = null;
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];
  String[] passStr = {"", "", "", "", "", "", "", "", "", "", ""};

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD0")) {/* 匯入檔案 */
      procUploadFile(0);
      checkButtonOff();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " //+ sqlChkEx(wp.itemStr("ex_file_name"), "1", "")
    	+ sqlCol(wp.itemStr("ex_file_name"), "a.file_name", "%like%")
        + sqlCol(wp.itemStr("ex_file_flag"), "a.file_flag", "like%")
        + sqlStrend(wp.itemStr("ex_file_date_s"), wp.itemStr("ex_file_date_e"), "a.file_date")
        + " and a.file_type  =  'MKT_THSR_REDEM' ";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTaName = wp.colStr("org_tab_name");
    else
      controlTaName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.file_date," + "a.file_time," + "a.file_name," + "a.trans_seqno," + "a.file_flag,"
        + "a.file_cnt," + "a.error_cnt," + "a.apr_flag," + "a.proc_date," + "a.crt_user,"
        + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTaName + " a ";
    wp.whereOrder = " " + " order by a.file_date desc,file_time desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commFileFag("comm_file_flag");
    commAprFlag("comm_apr_flag");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (controlTaName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTaName = orgTabName;
      else
        controlTaName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTaName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.file_type,"
        + "a.type_name," + "a.file_date," + "a.file_time," + "a.file_name," + "a.file_flag,"
        + "a.error_desc," + "a.error_memo," + "a.file_cnt," + "a.error_cnt," + "a.proc_flag,"
        + "a.trans_seqno," + "a.proc_date," + "a.crt_user," + "a.crt_date," + "a.apr_user,"
        + "a.apr_date," + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

    wp.daoTable = controlTaName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
    commFileFag("comm_file_flag");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm02.Mktm1050Func func = new mktm02.Mktm1050Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {}

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  public void commFileFag(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"成功", "失敗"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String s2 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, s2).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commAprFlag(String cde1) throws Exception {
    String[] cde = {"Y", "N", "X", "T"};
    String[] txt = {"已覆核", "待覆核", "不同意匯入", "失敗"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    
    if (!".xls".equals(commStr.right(wp.itemStr("zz_file_name"), 4))) {
        alertErr2("檔名錯誤!! 只能匯入excel檔 ");
        return;
    }
    
    if (!"CHKRF2007M".equalsIgnoreCase(commStr.left(wp.itemStr("zz_file_name"), 10))) {
        alertErr2("檔名錯誤!! 檔名前10碼必需是CHKRF2007M ");
        return;
    }
    
    if (loadType == 0)
//      fileDataImp0();
    	procExcelData();
  }

  // ************************************************************************
  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
  }

//  // ************************************************************************
//  void fileDataImp0() throws Exception {
//    TarokoFileAccess tf = new TarokoFileAccess(wp);
//
//    String inputFile = wp.itemStr("zz_file_name");
//    int fi = tf.openInputText(inputFile, "MS950");
//
//    if (fi == -1)
//      return;
//
//    if (selectMktUploadfileCtl() > 0) {
//      alertErr2("同日不可上傳同檔名資料: 重複上傳");
//      return;
//    }
//    mktm02.Mktm1050Func func = new mktm02.Mktm1050Func(wp);
//    func.dbDeleteMktUploadfileDataP();
//
//    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
//    comr.setConn(wp);
//    tranSeqStr = comr.getSeqno("MKT_MODSEQ");
//
//    String tmpStr = "";
//    int llOk = 0, llCnt = 0, llErr = 0;
//    int lineCnt = 0;
//    while (true) {
//      tmpStr = tf.readTextFile(fi);
//      if (tf.endFile[fi].equals("Y"))
//        break;
//      lineCnt++;
//      if (lineCnt < 2)
//        continue;
//
//      if (tmpStr.length() < 2)
//        continue;
//      llCnt++;
//
//      for (int inti = 0; inti < 10; inti++)
//        logMsg[inti] = "";
//      logMsg[10] = String.format("%02d", lineCnt);
//
//      if (checkUploadfile(tmpStr) != 0)
//        continue;
//      llOk++;
//
//      if (errorCnt == 0) {
//        rc = func.dbInsertMktUploadfileData(tranSeqStr, uploadFileCol, uploadFileDat, colNum);
//        if (rc != 1)
//          llErr++;
//      }
//    }
//
//    if (errorCnt > 0) {
//      func.dbDeleteMktUploadfileData(tranSeqStr);
//      func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
//    } else if (notifyCnt == 1) {
//      func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
//    }
//
//    func.dbInsertMktUploadfileCtl(tranSeqStr, llOk, errorCnt, passStr, datachkCnt);
//
//    sqlCommit(1); // 1:commit else rollback
//
//    alertMsg("資料匯入處理筆數: " + (llOk + errorCnt) + ", 成功 = " + llOk + ", 錯誤 = " + errorCnt);
//
//    tf.closeInputText(fi);
//    tf.deleteFile(inputFile);
//
//    return;
//  }
  
  
  // ************************************************************************
	void procExcelData() throws Exception {
		int liSheetNo = 0;
		int llOk = 0, llCnt = 0, llErr = 0;
		if (inExcelFile == null) {
			String filePath = TarokoParm.getInstance().getDataRoot() + "/upload/" + wp.itemStr("zz_file_name");
			filePath = SecurityUtil.verifyPath(filePath);
			inExcelFile = new FileInputStream(filePath);
			wb = new HSSFWorkbook(inExcelFile);
		}

		if (selectMktUploadfileCtl() > 0) {
			alertErr2("同日不可上傳同檔名資料: 重複上傳");
			return;
		}
		mktm02.Mktm1050Func func = new mktm02.Mktm1050Func(wp);
		func.dbDeleteMktUploadfileDataP();

		busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
		comr.setConn(wp);
		tranSeqStr = comr.getSeqno("MKT_MODSEQ");

		liSheetNo = 0;

		sheet = wb.getSheetAt(liSheetNo);
		int rowNumber = 0;
		int coloumNum = sheet.getRow(0).getPhysicalNumberOfCells();
		Iterator rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			row = (HSSFRow) rowIterator.next();
			rowNumber++;
			if (rowNumber < 6)
				continue;
			StringBuffer tmpStr = new StringBuffer();
			for (int i = 0; i < coloumNum; i++) {
				cell = row.getCell(i);
				if (cell == null) {
					break;
				}
				String cellValue = getCellValue(cell);
				if ("ERROR".equals(cellValue)) {
					alertErr2("檔案格式錯誤,不可匯入!!");
					return;
				}
				if (rowNumber == 6 && i == 0) {
					if (empty(cellValue)) {
						alertErr2("檔案無資料,不可匯入");
						return;
					}
					if (this.pos(",購,退", cellValue) < 0) {
						alertErr2("檔案格式錯誤,不可匯入!!");
						return;
					}
				}
				if (i == 0) {
					if (this.pos(",購,退", cellValue) < 0)
						break;
				}
				tmpStr.append(cellValue).append(",");
			}
			if(empty(tmpStr.toString()))
				continue;
			
			if (checkUploadfile(tmpStr.toString()) != 0) {
				llErr++;
				continue;
			}
			rc = func.dbInsertMktUploadfileData(tranSeqStr, uploadFileCol, uploadFileDat, colNum);
			if (rc != 1) {
				llErr++;
				continue;
			}
			
			llOk++;
		}
		
		if (llErr > 0) {
//			func.dbDeleteMktUploadfileData(tranSeqStr);
			func.dbInsertEcsNotifyLog(tranSeqStr, llErr);
		} else if (notifyCnt == 1) {
			func.dbInsertEcsNotifyLog(tranSeqStr, llErr);
		}

		func.dbInsertMktUploadfileCtl(tranSeqStr, llOk, llErr, passStr, datachkCnt);
		sqlCommit(1); // 1:commit else rollback
		alertMsg("資料匯入處理筆數: " + (llOk + llErr) + ", 成功 = " + llOk + ", 錯誤 = " + llErr);
	}

	public String getCellValue(HSSFCell cell2) {
		String cellValue = "";
		switch (cell2.getCellTypeEnum()) {
		case NUMERIC:// 數字
			cellValue = commStr.numFormat(cell2.getNumericCellValue(), "0");
			break;
		case STRING:// 文字
			cellValue = cell2.getStringCellValue();
			break;
		case BLANK:// 空值
			cellValue = "";
			break;
		case ERROR:// 非法字元
			cellValue = "ERROR";
			break;
		default:
			cellValue = "ERROR";
			break;
		}

		return cellValue;
	}
  
  // ************************************************************************

  int checkUploadfile(String tmpStr) throws Exception {
    mktm02.Mktm1050Func func = new mktm02.Mktm1050Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "trans_type";
    uploadFileCol[1] = "trans_date";
    uploadFileCol[2] = "trans_time";
    uploadFileCol[3] = "serial_no";
    uploadFileCol[4] = "pay_cardid";
    uploadFileCol[5] = "authentication_code";
    uploadFileCol[6] = "org_trans_date";
    uploadFileCol[7] = "org_serial_no";
    uploadFileCol[8] = "station_id";
    uploadFileCol[9] = "pnr";
    uploadFileCol[10] = "ticket_id";
    uploadFileCol[11] = "trans_amount";
    uploadFileCol[12] = "discount_value";
    uploadFileCol[13] = "total_amount";
    uploadFileCol[14] = "total_ticket_number";
    uploadFileCol[15] = "depart_date";
    uploadFileCol[16] = "train_no";
    uploadFileCol[17] = "departure_station_id";
    uploadFileCol[18] = "arrival_station_id";
    uploadFileCol[19] = "car_no";
    uploadFileCol[20] = "seat_no";
    uploadFileCol[21] = "plan_code";

    // ======== [I]nsert table column ========
    uploadFileCol[22] = "crt_user";
    uploadFileCol[23] = "crt_date";
    uploadFileCol[24] = "file_date";
    uploadFileCol[25] = "file_name";
    uploadFileCol[26] = "p_seqno";
    uploadFileCol[27] = "id_p_seqno";
    uploadFileCol[28] = "major_id_p_seqno";
    uploadFileCol[29] = "acct_type";
    uploadFileCol[30] = "card_no";
    uploadFileCol[31] = "major_card_no";
    uploadFileCol[32] = "reg_bank_no";
    uploadFileCol[33] = "error_code";
    uploadFileCol[34] = "trans_seqno";

    colNum = 35;

    // ==== insert table content default =====
    uploadFileDat[22] = wp.loginUser;
    uploadFileDat[23] = wp.sysDate;
    uploadFileDat[24] = wp.sysDate;
    uploadFileDat[25] = wp.itemStr("zz_file_name");
    uploadFileDat[34] = tranSeqStr;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};
    for (int inti = 0; inti < 22; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);

//    if (passStr[1].length() == 0) {
//      passStr[1] = uploadFileDat[0];
//      return (1);
//    }
    if(empty(uploadFileDat[4])||selectCrdCard(uploadFileDat[4])==0) {
    	errorCnt ++;
    	passStr[1] = "卡號不存在:%s筆";
    	passStr[2] = commStr.intToStr(errorCnt);
    	return 1;
    }
    
    if (uploadFileDat[3].length() != 14)
      return (1);
    if (uploadFileDat[0].equals("購"))
      uploadFileDat[0] = "P";
    else
      uploadFileDat[0] = "R";

    uploadFileDat[1] = uploadFileDat[1].replace("/", "");
    uploadFileDat[2] = uploadFileDat[2].replace(":", "");
    uploadFileDat[6] = uploadFileDat[6].replace("/", "");
    uploadFileDat[12] = uploadFileDat[12].replace(",", "").replace("(", "").replace(")", "");
    if (uploadFileDat[0].equals("R"))
      uploadFileDat[12] = String.format("%d", toInt(uploadFileDat[12]) * -1);
    uploadFileDat[13] = uploadFileDat[13].replace("/", "");

    uploadFileDat[33] = "00";
    if (selectMktThsrUptxn() != 0)
      uploadFileDat[33] = "01";
    uploadFileDat[26] = sqlStr("p_seqno");
    uploadFileDat[27] = sqlStr("id_p_seqno");
    uploadFileDat[28] = sqlStr("major_id_p_seqno");
    uploadFileDat[29] = sqlStr("acct_type");
    uploadFileDat[30] = sqlStr("card_no");
    uploadFileDat[31] = sqlStr("major_card_no");
    uploadFileDat[32] = sqlStr("reg_bank_no");

    return 0;
  }
  
  
  int selectCrdCard(String cardNo){
	  String sqlCmd = "select count(*) as card_cnt from crd_card where substring(card_no,1,6) = :card_no6 and substring(card_no,13) = :card_no4"; 
	  setString("card_no6",commStr.left(cardNo, 6));
	  setString("card_no4",commStr.right(cardNo, 4));
	  sqlSelect(sqlCmd);
	  return sqlInt("card_cnt");
  }
  

  // ************************************************************************
  // ************************************************************************
  int selectMktUploadfileCtl() throws Exception {
    wp.sqlCmd = " select " + " 1 as rowdata " + " from mkt_uploadfile_ctl "
        + " where file_type   = 'MKT_THSR_REDEM' " + " and   file_date   = '" + wp.sysDate + "' "
        + " and   file_flag   = 'Y' "
//        + " and   file_name   = '" + wp.itemStr("zz_file_name")
//        + "' ";
        + sqlCol(wp.itemStr("zz_file_name"), "file_name");
    this.sqlSelect();

    if (sqlRowNum > 0)
      return (1);

    return (0);
  }

  // ************************************************************************
  int selectMktThsrUptxn() throws Exception {
    wp.sqlCmd = " select " + " acct_type, " + " p_seqno, " + " id_p_seqno, " + " major_id_p_seqno, "
        + " card_no, " + " major_card_no, " + " reg_bank_no " + " from mkt_thsr_uptxn "
        + " where serial_no  = '" + uploadFileDat[3] + "' ";

    this.sqlSelect();

    if (sqlRowNum <= 0)
      return (1);

    return (0);
  }
  // ************************************************************************

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
