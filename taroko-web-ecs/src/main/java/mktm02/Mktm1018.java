package mktm02;
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-07-23  V1.00.01  Machao     新增功能: mktm1018_市區停車請款匯入作業     *
* 110-11-19  V1.00.05  Yangbo       joint sql replace to parameters way  
* 112-08-21  V1.00.06  Machao      文檔名忽略大小寫驗證*
******************************************************************************/


import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import Dxc.Util.SecurityUtil;
import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;



public class Mktm1018 extends  BaseAction {

	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	  busi.ecs.CommRoutine comr = null;
	  mktm02.Mktm1018Func func = null;
	  XSSFWorkbook wb = null;
	  XSSFSheet sheet = null;
	  XSSFRow row = null;
	  XSSFRow dummyRow = null;
	  XSSFCell cell = null;
	  HSSFWorkbook hwb = null;
	  HSSFSheet hsheet = null;
	  HSSFRow hrow = null;
	  HSSFRow hdummyRow = null;
	  HSSFCell hcell = null;
	  SimpleDateFormat sdf = null;

	  InputStream inExcelFile = null;
	  int rr = -1;
	  int llOK = 0, llCnt = 0 ,llErr = 0, errs = 0;
	  @Override
	  public void userAction() throws Exception {

	    if (eqIgno(wp.buttonCode, "X")) {
	      /* 轉換顯示畫面 */
	      strAction = "new";
	      clearFunc();
	    } else if (eqIgno(wp.buttonCode, "Q")) {
	      /* 查詢功能 */
	      strAction = "Q";
	      queryFunc();
	    } else if (eqIgno(wp.buttonCode, "R")) {
	      // -資料讀取-
	      strAction = "R";
	      dataRead();
	    } else if (eqIgno(wp.buttonCode, "A")) {
	      /* 新增功能 */
	      saveFunc();
	    } else if (eqIgno(wp.buttonCode, "U")) {
	      /* 更新功能 */

	    } else if (eqIgno(wp.buttonCode, "D")) {
	      /* 刪除功能 */
	      saveFunc();
	    } else if (eqIgno(wp.buttonCode, "M")) {
	      /* 瀏覽功能 :skip-page */
	      queryRead();
	    } else if (eqIgno(wp.buttonCode, "S")) {
	      /* 動態查詢 */
	      querySelect();
	    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
	      procFunc();
	    } else if (eqIgno(wp.buttonCode, "L")) {
	      /* 清畫面 */
	      strAction = "";
	      clearFunc();
	    } else if (eqIgno(wp.buttonCode, "C")) {
	      // -資料處理-
	      procFunc();
	    } else if (eqIgno(wp.buttonCode, "C1")) {
	      // -資料處理-
	      procFunc();
	    }
	    dddwSelect();


	  }


	  @Override
	  public void queryFunc() throws Exception {
		  if (itemIsempty("zz_file_name")) {
		      alertErr2("匯入檔名 不可空白");
		      return;
		    }

		  if (wp.itemEmpty("mk_park_vendor")) {
		    	alertErr2("廠商必須選其中一個");
		    	return;
		    }

		  if (selectMktUploadfileCtl()>0) {
		    	alertErr2("同一檔案不可重複匯入");
		    	return;
		    }
		//2.5.2.4.	檔名檢核如下
		  String filename = wp.itemStr("zz_file_name");
		  wp.itemSet("file_name", wp.itemStr("zz_file_name"));
		  int loc = filename.lastIndexOf(".");
		  String ext = "";
		  String fname = "";
		  if (loc >= 0) {
			  fname = filename.substring(0, loc);
			  ext = filename.substring(loc + 1);
		  } else {
			  fname = filename;
		  }
		    String[] list = fname.split("_");

		    //2.5.2.7.	依據選取的廠商,檢核如下
		    if(wp.itemStr("mk_park_vendor").equals("24963309")) {
		    	wp.itemSet("park_vendor", wp.itemStr("mk_park_vendor"));
		    	if(!list[0].equalsIgnoreCase("vivi")) {
		    		alertErr2("VIVI_PARK檔名前4碼,必需是VIVI開頭…");
		    		return;
		    	}else if(list[0].equalsIgnoreCase("vivi") && !ext.equals("txt")) {
		    		alertErr2(" VIVI_PARK檔必需是txt檔");
		    		return;
		    	}else {
		    		//上傳檔名中的時間
		    		String strMonth = list[list.length-2];

		    		int imonth = Integer.parseInt(strMonth) + 191100;
		    		strMonth = String.valueOf(imonth);

			    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMM");

			    	String tmpMonth = sdf.format(sdf.parse(strMonth));
			    	if(!tmpMonth.equals(strMonth)) {
			    		alertErr2("最後5碼是民國年月");
			    	}

			    	java.util.Date date=new java.util.Date();
			    	String str=sdf.format(date);
			    	int data2 = Integer.parseInt(str)-191100;
			    	if(imonth<=data2) {
			    		String dataDate = strMonth+"01";
			    		wp.itemSet("data_date", dataDate);
			    		wp.itemSet("file_Name", wp.itemStr("zz_file_name"));
			    	}
		    		fileTxtImp();
		    	}
		    }

		    if(wp.itemStr("mk_park_vendor").equals("86517413")) {
		    	wp.itemSet("park_vendor", wp.itemStr("mk_park_vendor"));
		    	if(!list[0].equalsIgnoreCase("TAIWAN")) {
		    		alertErr2("台灣聯通檔名前6碼,必需是TAIWAN開頭…");
			    	return;
		    	}else if(list[0].equalsIgnoreCase("TAIWAN") && !ext.equals("xlsx") ) {
		    		alertErr2("台灣聯通必需是xlsx檔");
			    	return;
		    	}else {
		    		//上傳檔名中的時間
		    		String strMonth = list[list.length-2];

		    		int imonth = Integer.parseInt(strMonth);
		    		strMonth = String.valueOf(imonth);

			    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMM");

			    	String tmpMonth = sdf.format(sdf.parse(strMonth));
			    	if(!tmpMonth.equals(strMonth)) {
			    		alertErr2("最後6碼是西元年月");
			    	}

			    	java.util.Date date=new java.util.Date();
			    	String str=sdf.format(date);
			    	int data2 = Integer.parseInt(str);
			    	if(imonth<=data2) {
			    		String dataDate = strMonth+"01";
			    		wp.itemSet("data_date", dataDate);
			    		wp.itemSet("file_Name", wp.itemStr("zz_file_name"));

		    	}
			    	fileXlsxImp();
		    	}
		    }
		    if(wp.itemStr("mk_park_vendor").equals("23959144")) {
		    	wp.itemSet("park_vendor", wp.itemStr("mk_park_vendor"));
		    	if(!list[0].equalsIgnoreCase("TPS")) {
		    		alertErr2("永固停車場檔名前3碼,必需是TPS開頭…");
			    	return;
		    	}else if(list[0].equalsIgnoreCase("TPS") && !ext.equals("xls")) {
		    		alertErr2("永固停車場檔必需是xls檔");
			    	return;
		    	}else {
		    		//最後6碼是熙元年月
		    		//上傳檔名中的時間
		    		String strMonth = list[list.length-2];

		    		int imonth = Integer.parseInt(strMonth);
		    		strMonth = String.valueOf(imonth);

			    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMM");

			    	String tmpMonth = sdf.format(sdf.parse(strMonth));
			    	if(!tmpMonth.equals(strMonth)) {
			    		alertErr2("最後6碼是西元年月");
			    	}
			    	//日期驗證
			    	java.util.Date date=new java.util.Date();
			    	String str=sdf.format(date);
			    	int data2 = Integer.parseInt(str);
			    	if(imonth<=data2) {
			    		String dataDate = strMonth+"01";
			    		wp.itemSet("data_date", dataDate);
			    		wp.itemSet("file_Name", wp.itemStr("zz_file_name"));

		    	}
			    	fileXlsImp();
		    	}
		    }
	}



	  void fileTxtImp() throws Exception {
		  //txt 文檔解析
		    TarokoFileAccess tf = new TarokoFileAccess(wp);

		    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
		    String inputFile = wp.itemStr("zz_file_name");
		    // int fi = tf.openInputText(inputFile,"UTF-8");
		    int fi = tf.openInputText(inputFile, "MS950");
		    if (fi == -1) {
		      return;
		    }
		    int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");

		    mktm02.Mktm1018Func func = new mktm02.Mktm1018Func();
		    func.setConn(wp);

		    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));


		    int lineCnt = 0;
		    while (true) {
		      String file = tf.readTextFile(fi);
		      if(file.length()<100 && file.length()!=0)
		    	  break;
		      if (tf.endFile[fi].equals("Y"))
		        break;
		      lineCnt++;
		      if (lineCnt < 2)
		        continue;
		      if (file.length() < 2)
		    	  continue;
		      llCnt++;

		      //換區txt文件中的每一欄數據
		      String[] txt = new String[2];
		      txt[0] = file;
		      txt = commString.token(txt, ",");
		      String stationid = txt[1];


		      if(stationid.isEmpty()) {
		    	  wp.itemSet("station_id", "");
		    	  wp.itemSet("err_code", "33");
		    	  errs++;
		      }
		      wp.itemSet("station_id", stationid);

		      txt = commString.token(txt, ",");
		      String datetime = txt[1];
		      String dd = datetime.substring(0, 8);
		      String dt = datetime.substring(8, 12);
		      wp.itemSet("data_date", dd);
		      wp.itemSet("data_time", dt);

		      txt = commString.token(txt, ",");
		      String idno = txt[1];
		      wp.itemSet("id_no", idno);

		      txt = commString.token(txt, ",");
		      String cardno = txt[1];
		      if(cardno.isEmpty()) {
		    	  wp.itemSet("err_code", "31");
		    	  errs++;
//		    	  continue;
		      }
		      else if(selcetCrdcardCtl(cardno)>0) {
//		    	  continue;
		      }

		      wp.itemSet("card_no", cardno);

		      txt = commString.token(txt, ",");
		      String parkts = txt[1];
		      if(parkts.length() != 12 && parkts.length() != 14) {
		    	  wp.itemSet("err_code", "35");
		    	  errs++;
		      }else {
		    	  String pds = parkts.substring(0, 8);
			      String pts = parkts.substring(8, 12) + "00";
			      wp.itemSet("park_date_s", pds);
			      wp.itemSet("park_time_s", pts);
		      }

		      txt = commString.token(txt, ",");
		      String parkte = txt[1];
		      if(parkte.length() != 12 && parkte.length() != 14) {
		    	  wp.itemSet("err_code", "36");
		    	  errs++;
		      }else {
		    	  String pde = parkte.substring(0, 8);
			      String pte = parkte.substring(8, 12) + "00";
			      wp.itemSet("park_date_e", pde);
			      wp.itemSet("park_time_e", pte);


		      }

		      txt = commString.token(txt, ",");
		      String actioncd = txt[1];
		      if(!actioncd.isEmpty()) {
		    	 if(selectBildodoparmCtl(actioncd)>0) {
//		    		 continue;
		    	 }
		      }
		      wp.itemSet("action_cd", actioncd);

		      txt = commString.token(txt, ",");
		      String parkhr = txt[1];
		      if(parkhr.isEmpty()) {
		    	  wp.itemSet("err_code", "37");
		    	  errs++;
		      }else {
		    	  wp.itemSet("park_hr", parkhr);
		      }

		      txt = commString.token(txt, ",");
		      String freehr = txt[1];
		      wp.itemSet("free_hr", freehr);

		      txt = commString.token(txt, ",");
		      String usebonushr = txt[1];
		      wp.itemSet("use_bonus_hr", usebonushr);



		     if(errs==0) {
		    	 wp.itemSet("err_code", "99");
		     }



		      //執行插入數據命令
		      if(func.insertRighList() == 1) {
	  		    	llOK++;
	  		    }else {
	  		    	llErr++;
	  		    }

		    }

		    if (llOK > 0) {
		      sqlCommit(1);
		    } else {
		      sqlCommit(-1);
		    }

		    tf.closeOutputText(fileErr);
		    tf.closeInputText(fi);
		    tf.deleteFile(inputFile);

		    if (llErr > 0) {
		      wp.setDownload(inputFile + ".errs");
		    }

		    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOK + ", 失敗筆數=" + llErr);
		    wp.colSet("zz_file_name", "");
		    return;
 		  }

	  void fileXlsxImp() throws Exception{
		//xlsx文檔解析

		  if (inExcelFile == null) {
			  	String inputFile = wp.itemStr("zz_file_name");
				String filePath = TarokoParm.getInstance().getDataRoot()  + "/upload/" + inputFile;
				filePath = SecurityUtil.verifyPath(filePath);
				inExcelFile = new FileInputStream(filePath);
				wb = new XSSFWorkbook(inExcelFile);
		    }

		  mktm02.Mktm1018Func func = new mktm02.Mktm1018Func();
		    func.setConn(wp);
		  //讀取工作表
		  	int liSheetNo = 1;
		    sheet = wb.getSheetAt(liSheetNo);
		    int totalRows = sheet.getPhysicalNumberOfRows();


		    for(int i= 1;i<totalRows; i++) {
		    	//每一行數據
		    	row = sheet.getRow(i);
		    	if(row == null) {
		    		continue;
		    	}else {
		    		if(row.getCell(0) == null)
					    continue;
		    			llCnt++;

		    			//獲取每個單元格數據，並將數據保存
					    String stationid = row.getCell(0).getStringCellValue();
					    wp.itemSet("station_id", stationid);

					    String stationname = row.getCell(1).getStringCellValue();
					    wp.itemSet("station_name", stationname);

					    //第三欄時間處理
					    String datadate = row.getCell(2).getStringCellValue();
					    String[] diti = datadate.split(" ");
					    String[] dd = diti[0].split("-");
					    String ddate = dd[0]+dd[1]+dd[2];
					    String[] dt = diti[1].split(":");
					    String dtime = dt[0]+dt[1]+dt[2];
					    if(dtime.length()!=6)
					    dtime = dtime + "00";
					    wp.itemSet("data_date", ddate);
					    wp.itemSet("data_time", dtime);

					    String cardno = row.getCell(3).getStringCellValue();
					    if(cardno.isEmpty()) {
					    	  wp.itemSet("err_code", "31");
					    	  errs++;
					      }
					      else if(selcetCrdcardCtl(cardno)>0) {
					      }

					    wp.itemSet("card_no", cardno);


					    String parkdates = row.getCell(4).getStringCellValue();
					    String[] pds = parkdates.split(" ");

					    if(pds.length!=2) {
					    	wp.itemSet("err_code", "35");
					    	errs++;
					    }else {
					    	String[] dds = pds[0].split("-");
						    String pdate = dds[0]+dds[1]+dds[2];
						    String[] pdt = pds[1].split(":");
						    String ptime = pdt[0]+pdt[1]+pdt[2];
						    if(ptime.length()!=6)
						    ptime = ptime + "00";
						    wp.itemSet("park_date_s", pdate);
						    wp.itemSet("park_time_s", ptime);
					    }

					    String parkdatee = row.getCell(5).getStringCellValue();
					    String[] pde = parkdatee.split(" ");

					    if(pde.length!=2) {
					    	wp.itemSet("err_code", "36");
					    	errs++;
					    }else {
					    	String[] dde = pde[0].split("-");
						    String pdee = dde[0]+dde[1]+dde[2];
						    String[] pdte = pde[1].split(":");
						    String ptimee = pdte[0]+pdte[1]+pdte[2];
						    if(ptimee.length()!=6)
						    ptimee = ptimee + "00";
						    wp.itemSet("park_date_e", pdee);
						    wp.itemSet("park_time_e", ptimee);
					    }

					    String filet = row.getCell(6).getStringCellValue();
					    wp.itemSet("file_t", filet);

					    String parkhr = row.getCell(7).getStringCellValue();
					    wp.itemSet("park_hr", parkhr);
					    String freehr = row.getCell(8).getStringCellValue();
					    wp.itemSet("free_hr", freehr);

					    if(errs==0) {
					    	 wp.itemSet("err_code", "99");
					     }

					    //插入數據
		  		      if(func.insertRighList() == 1) {
		  		    	llOK++;
		  		    	continue;
		  		    }else {
		  		    	llErr++;
		  		    }
		  		    llCnt = llOK + llErr;
		    	}
		    }
		    if (llOK > 0) {
			      sqlCommit(1);
			    } else {
			      sqlCommit(-1);
			    }
		    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOK + ",失敗筆數=" + llErr);
		    return;

	  }

	  void fileXlsImp() throws Exception{
		  //xls 文件解析

		  if (inExcelFile == null) {
			  	String inputFile = wp.itemStr("zz_file_name");
				String filePath = TarokoParm.getInstance().getDataRoot()  + "/upload/" + inputFile;
				filePath = SecurityUtil.verifyPath(filePath);
				inExcelFile = new FileInputStream(filePath);
				hwb = new HSSFWorkbook(inExcelFile);
		    }

		  mktm02.Mktm1018Func func = new mktm02.Mktm1018Func();
		    func.setConn(wp);

		  //讀取工作表
		  	int liSheetNo = 0;
		    hsheet = hwb.getSheetAt(liSheetNo);
		    int totalRows = hsheet.getPhysicalNumberOfRows();


		    for(int i= 1;i<totalRows; i++) {
		    	hrow = hsheet.getRow(i);
		    	if(hrow == null) {
		    		continue;
		    	}else {
		    		if(hrow.getCell(0) == null)
					    continue;
		    			llCnt++;
					    String stationid = hrow.getCell(0).getStringCellValue();
					    wp.itemSet("station_id", stationid);
					    String cardno = hrow.getCell(1).getStringCellValue();
					    if(cardno.isEmpty()) {
					    	  wp.itemSet("err_code", "31");
					    	  errs++;
//					    	  continue;
					      }
					      else if(selcetCrdcardCtl(cardno)>0) {
//					    	  continue;
					      }

					    wp.itemSet("card_no", cardno);


					    String park_date_s = hrow.getCell(2).getStringCellValue();
					    if(park_date_s.length()!=12) {
					    	wp.itemSet("err_code", "35");
					    	errs++;
					    }else {
					    	String pds = hrow.getCell(2).getStringCellValue().substring(0, 8);
						    wp.itemSet("park_date_s", pds);
						    String pts = hrow.getCell(2).getStringCellValue().substring(8, 12) + "00";
						    wp.itemSet("park_time_s", pts);
					    }

					    String park_date_e = hrow.getCell(3).getStringCellValue();
					    if(park_date_e.length()!=12) {
					    	wp.itemSet("err_code", "36");
					    	errs++;
					    }else {
					    	String pde = hrow.getCell(3).getStringCellValue().substring(0, 8);
						    wp.itemSet("park_date_e", pde);
						    String pte = hrow.getCell(3).getStringCellValue().substring(8, 12) + "00";
						    wp.itemSet("park_time_e", pte);
					    }

					    String parkhr = hrow.getCell(4).getStringCellValue();
					    wp.itemSet("park_hr", parkhr);
					    String freehr = hrow.getCell(5).getStringCellValue();
					    wp.itemSet("free_hr", freehr);

					    if(errs==0) {
					    	 wp.itemSet("err_code", "99");
					     }


		  		      if(func.insertRighList() == 1) {
		  		    	llOK++;
		  		    	continue;
		  		    }else {
		  		    	llErr++;
		  		    }

		    	}
		    }
		    if (llOK > 0) {
			      sqlCommit(1);
			    } else {
			      sqlCommit(-1);
			    }
		    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOK + ",失敗筆數=" + llErr);
		    return;

	  }


	@Override
	  public void queryRead() throws Exception {

	  }

	  @Override
	  public void querySelect() throws Exception {


	  }

	  @Override
	  public void dataRead() throws Exception {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public void saveFunc() throws Exception {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public void procFunc() throws Exception {


	  }

	public void commErrCode(String cde1) throws Exception {
		    String[] cde = {"31", "32", "33", "34", "35", "36", "37"};
		    String[] txt = {"錯誤!!卡號空白", "ID不存在", "站別代號為空值", "優惠方案不存在", "入場日期為不正確", "出場日期為不正確", "停車時數=0"};
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

	  }



	  @Override
	  public void initButton() {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public void initPage() {
	    // TODO Auto-generated method stub

	  }
	  int selectMktUploadfileCtl() throws Exception {
		    wp.sqlCmd = " select " + " 1 as rowdata " + " from mkt_dodo_resp_t "
//		        + " where  file_name   = '"
//		        + wp.itemStr("zz_file_name") + "' ";
		        + " where 1 = 1" + sqlCol(wp.itemStr("zz_file_name"), "file_name");
		    this.sqlSelect();

		    if (sqlRowNum > 0)
		      return (1);

		    return (0);
		  }
	  int selectBildodoparmCtl(String actioncd) throws Exception {

		  String ac111 = actioncd;

		  wp.sqlCmd = "select " + " car_hours " + " from bil_dodo_parm "
//				  +" where action_cd = '"
//				  +actioncd  +"' ";
				  +" where 1 = 1 " + sqlCol(actioncd, "action_cd");
		  this.sqlSelect();

		  String carhours = this.sqlStr("car_hours");

		  if(carhours.isEmpty())  {
	    		 wp.itemSet("err_code", "34");
	    		 errs++;
	    	 }
		  wp.itemSet("car_hours", carhours);

		  if (sqlRowNum <= 0)
		      return (1);



		  return (0);
	  }
	  int selcetCrdcardCtl(String cardno) throws Exception{
		  String Card_No = cardno;
		  wp.sqlCmd = "select " + " id_p_seqno, " + " acct_type, " + " p_seqno " + " from crd_card "
//				  +" where card_no = '"
//				  + Card_No +"' ";
				  +" where 1 = 1 " + sqlCol(Card_No, "card_no");

		  this.sqlSelect();

		  String idpseqno = this.sqlStr("id_p_seqno");
		  wp.itemSet("id_p_seqno", idpseqno);

		  String accttype = this.sqlStr("acct_type");
		  wp.itemSet("acct_type", accttype);

		  String pseqno = this.sqlStr("p_seqno");

		  if(pseqno.isEmpty()){
	    	  wp.itemSet("err_code", "01");
	    	  errs++;
	      }

		  wp.itemSet("p_seqno", pseqno);

		  if (sqlRowNum <= 0)
		      return (1);


		  return (0);
	  }

	  public void dddwSelect() {
		  if ((wp.respHtml.equals("mktm1018"))) {
		        wp.initOption = "--";
		        wp.optionKey = "";
		        if (wp.colStr("mk_park_vendor").length() > 0) {
		          wp.optionKey = wp.colStr("mk_park_vendor");
		        }
		        try {
					this.dddwList("dddw_park_vendor", "mkt_park_parm", "trim(park_vendor)", "trim(vendor_name)",
					    " order by 'park_vendor'");
				} catch (Exception e) {
					e.printStackTrace();
				}
		      }
		  }


	}
