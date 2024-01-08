package cmsm03;
/** 新貴通卡請款匯入作業
 * 2023-0410:  Zuwei Su    copy from mega
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * V.2018-0911.alex
 */

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import taroko.com.TarokoExcel;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

@SuppressWarnings({"unchecked", "deprecation"})
public class Cmsm5010 extends BaseAction implements InfaceExcel {
taroko.base.CommDate zzdate = new taroko.base.CommDate();
XSSFWorkbook wb = null;
XSSFSheet sheet = null;
XSSFRow row = null;
XSSFRow dummyRow = null;
XSSFCell cell = null;
SimpleDateFormat sdf = null;

InputStream inExcelFile = null;

int ilTotCnt = 0;
int rr = -1;

@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "C":   //新貴匯入--
         doImport(); break;
      case "C2":  //--
         doImportC2(); break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
    try {
        wp.optionKey = wp.colStr("ex_corp_no");
        dddwList("dddw_corp_no", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type ='SUPPLIER' order by wf_id,wf_desc");
    } catch (Exception ex) {
    }
}

// 資料匯入-新貴通
void doImport() throws Exception {
   int liSheetNo = 0;
   if (itemIsempty("zz_file_name")) {
      alertErr("匯入檔名 不可空白");
      return;
   }

   if (inExcelFile == null) {
      inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot() + "/upload/" + wp.itemStr("zz_file_name"));
      wb = new XSSFWorkbook(inExcelFile);
   }

   //--從 0 開始
//		li_sheet_no = (int) (wp.item_num("ex_sheet") -1);
   liSheetNo = 0;

   sheet = wb.getSheetAt(liSheetNo);
   Iterator rowIterator = sheet.rowIterator();

   int liExcelRows = 0, liCnt = -1, liSerNum = 0;
   String liSeqno = "";
   String lsPpCardNo = "", ls_visit_date = "";
   while (rowIterator.hasNext()) {
      row = (XSSFRow) rowIterator.next();
      liExcelRows++;

//			 if(li_excel_rows==1 || li_excel_rows < wp.item_num("ex_row1"))	continue;
//
//			 if(!wp.item_empty("ex_row2") && wp.item_num("ex_row2")!=0){
//				 if(li_excel_rows>wp.item_num("ex_row2"))	break;
//			 }

      cell = row.getCell(0);
      if (cell == null) continue;
      if (cell.getCellTypeEnum() != CellType.NUMERIC) continue;
      liSeqno = "" + cell.getNumericCellValue();
      if (wp.itemEq("ex_bin_type", "M")) {
         cell = row.getCell(5);
      }
      else if (wp.itemEq("ex_bin_type", "V")) {
         cell = row.getCell(1);
      }
      lsPpCardNo = cell.getStringCellValue();

      if (empty(liSeqno) && empty(lsPpCardNo)) continue;

      if (!isNumber(liSeqno) || !isNumber(lsPpCardNo)) continue;

      liCnt++;
      liSerNum++;

      if (liSerNum < 10) {
         wp.colSet(liCnt, "ser_num", "0" + liSerNum);
      }
      else {
         wp.colSet(liCnt, "ser_num", "" + liSerNum);
      }

      wp.colSet(liCnt, "crt_date", getSysDate());
      wp.colSet(liCnt, "bin_type", wp.itemStr("ex_bin_type"));
      wp.colSet(liCnt, "data_seqno", "" + liSeqno);
      wp.colSet(liCnt, "pp_card_no", lsPpCardNo);
      wp.colSet(liCnt, "from_type", "2");
      wp.colSet(liCnt, "terminal_no", "");
      wp.colSet(liCnt, "use_city", "");
      wp.colSet(liCnt, "imp_file_name", wp.itemStr("zz_file_name"));
      wp.colSet(liCnt, "free_use_cnt", "0");
      wp.colSet(liCnt, "ch_cost_amt", "0");
      wp.colSet(liCnt, "guest_cost_amt", "0");
      wp.colSet(liCnt, "crt_user", wp.loginUser);

      for (int k = 0; k < row.getLastCellNum(); k++) {
         if (k == 0) {
            sheet.setColumnWidth(0, 0);
         }

         String cellValue = "";
         cell = row.getCell(k);
         if (cell == null) {
            continue;
         }

         if (cell.getCellTypeEnum() == CellType.STRING) {
            cellValue = cell.getStringCellValue();
         }
         else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            cellValue = "" + cell.getNumericCellValue();
         }

         if (wp.itemEq("ex_bin_type", "M")) {
            //--wp.col_set(li_cnt,"", cellValue);
            if (k == 1) {
               wp.colSet(liCnt, "bank_name", cellValue);
            }
            else if (k == 2) {
               wp.colSet(liCnt, "deal_type", cellValue);
            }
            else if (k == 3) {
               wp.colSet(liCnt, "associate_code", cellValue);
            }
            else if (k == 4) {
               wp.colSet(liCnt, "ica_no", cellValue);
            }
            else if (k == 6) {
               wp.colSet(liCnt, "cardholder_ename", cellValue);
            }
            else if (k == 7) {
               if (cell.getCellTypeEnum() != CellType.NUMERIC) continue;
               double value = cell.getNumericCellValue();
               Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
               sdf = new SimpleDateFormat("yyyyMMdd");
               ls_visit_date = sdf.format(date);
               wp.colSet(liCnt, "visit_date", ls_visit_date);
               //wp.col_set(li_cnt,"visit_date", wk_visit_date(cellValue));
            }
            else if (k == 8) {
               wp.colSet(liCnt, "lounge_name", cellValue);
            }
            else if (k == 9) {
               wp.colSet(liCnt, "lounge_code", cellValue);
            }
            else if (k == 10) {
               wp.colSet(liCnt, "domestic_int", cellValue);
            }
            else if (k == 11) {
               wp.colSet(liCnt, "iso_conty", cellValue);
            }
            else if (k == 12) {
               wp.colSet(liCnt, "iso_conty_code", cellValue);
            }
            else if (k == 13) {
               wp.colSet(liCnt, "cardholder_visits", cellValue);
            }
            else if (k == 14) {
               wp.colSet(liCnt, "guests_count", cellValue);
            }
            else if (k == 15) {
               wp.colSet(liCnt, "total_visits", cellValue);
            }
            else if (k == 16) {
               wp.colSet(liCnt, "batch_no", cellValue);
            }
            else if (k == 17) {
               wp.colSet(liCnt, "voucher_no", cellValue);
            }
            else if (k == 18) {
               wp.colSet(liCnt, "mc_billing_region", cellValue);
            }
            else if (k == 19) {
               wp.colSet(liCnt, "curr_code", cellValue);
            }
            else if (k == 20) {
               wp.colSet(liCnt, "fee_per_holder", cellValue);
            }
            else if (k == 21) {
               wp.colSet(liCnt, "fee_per_guest", cellValue);
            }
            else if (k == 22) {
               wp.colSet(liCnt, "total_fee", cellValue);
            }
            else if (k == 23) {
               wp.colSet(liCnt, "total_free_guests", cellValue);
            }
            else if (k == 24) {
               wp.colSet(liCnt, "free_guests_value", cellValue);
            }
            else if (k == 25) {
               wp.colSet(liCnt, "tot_charg_guest", cellValue);
            }
            else if (k == 26) {
               wp.colSet(liCnt, "charg_guest_value", cellValue);
            }
            else if (k == 27) {
               wp.colSet(liCnt, "billing_region", cellValue);
            }
         }
         else if (wp.itemEq("ex_bin_type", "V")) {
            if (k == 2) {
               wp.colSet(liCnt, "cardholder_ename", cellValue);
            }
            else if (k == 10) {//--ori:7
               if (cell.getCellTypeEnum() != CellType.NUMERIC) continue;
               double value = cell.getNumericCellValue();
               Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
               sdf = new SimpleDateFormat("yyyyMMdd");
               ls_visit_date = sdf.format(date);
               wp.colSet(liCnt, "visit_date", ls_visit_date);
            }
            else if (k == 3) {
               wp.colSet(liCnt, "lounge_name", cellValue);
            }
            else if (k == 8) {//--ori:6
               wp.colSet(liCnt, "iso_conty", cellValue);
            }
            else if (k == 11) {//--ori:8
               wp.colSet(liCnt, "cardholder_visits", cellValue);
            }
            else if (k == 12) {//--ori:9
               wp.colSet(liCnt, "guests_count", cellValue);
            }
            else if (k == 13) {//--ori:10
               wp.colSet(liCnt, "total_visits", cellValue);
            }
            else if (k == 17) {//--ori:11
               wp.colSet(liCnt, "voucher_no", cellValue);
            }
            else if (k == 5) {//--ori:4
               wp.colSet(liCnt, "terminal_no", cellValue);
            }
            else if (k == 7) {//--ori:5
               wp.colSet(liCnt, "use_city", cellValue);
            }
         }

      }
   }

   inExcelFile.close();
   inExcelFile = null;
   ilTotCnt = liSerNum;
//		 wp.listCount[0] = li_ser_num;

   wp.logSql = false;
   queryAfter();
   wp.logSql = true;

   procFunc();

}

// 資料匯入-機埸停車
void doImportC2() throws Exception {
   if (itemIsempty("zz_file_name")) {
      alertErr("上傳檔名: 不可空白");
      return;
   }
   if (wp.iempty("ex_corp_no")) {
      alertErr("廠商統編: 不可空白");
      return;
   }

   TarokoFileAccess tf = new TarokoFileAccess(wp);
   String exCorpNo = wp.itemStr("ex_corp_no");
   String inputFile = wp.itemStr("zz_file_name");
   String filename = inputFile.substring(0, inputFile.length() - 4);
   if (!inputFile.toLowerCase().endsWith(".csv")) {
       alertErr("副檔名錯誤!! 需選取csv檔");
       return;
   }
   if (!filename.toLowerCase().startsWith("airport_park")) {
       alertErr("檔名錯誤，檔名必須是AIRPORT_PARK開頭");
       return;
   }
   String fcode = "";
   if (filename.length() > 13) {
       if (filename.length() >= 21) {
           fcode = filename.substring(13, 21);
       } else {
           fcode = filename.substring(13);
       }
   }
   if (!fcode.equals(exCorpNo.substring(0,8))) {
       alertErr("檔名錯誤!!廠商統編與檔名統編不符");
       return;       
   }
   if (checkFileExists(inputFile)) {
       alertErr("檔案重覆匯入, 不可匯入");
       return;       
   }
   
   int fi = tf.openInputText(inputFile, "MS950");
   if (fi == -1) {
      return;
   }

//	int file_err = tf.openOutputText(inputFile + ".err", "UTF-8");

   Cmsm5010Func func = new Cmsm5010Func();
   func.setConn(wp);

   wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

   int llOk = 0, llCnt = 0;
   while (true) {
      String ss = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
         break;
      }
      if (ss.length() < 20) {
         continue;
      }
      llCnt++;

      if(llCnt ==1)
         continue;

      String[] tt = new String[]{ss,","};
      String lsCardNo =commString.token(tt);
      String lsPurchaseDate =commString.token(tt);
      String lsPurchaseDateE =commString.token(tt);
      String lsIdNo =commString.token(tt);
      String lsCarNo =commString.token(tt);
      String lsServiceDays =commString.token(tt);
      String lsChiName =commString.token(tt);
      String lsContactTelNo =commString.token(tt);
      String lsOrderNo =commString.token(tt);
      String lsServiceNo =commString.token(tt);
      String lsServiceName =commString.token(tt);
      String lsServiceCode =commString.token(tt);
      String lsServiceItem =commString.token(tt);
      if (empty(lsCardNo)) continue;

      wp.itemSet("C2.card_no",lsCardNo);
      wp.itemSet("C2.purchase_date",lsPurchaseDate);
      wp.itemSet("C2.purchase_date_e",lsPurchaseDateE);
      wp.itemSet("C2.id_no",lsIdNo);
      wp.itemSet("C2.car_no",lsCarNo);
      wp.itemSet("C2.service_days",lsServiceDays);
      wp.itemSet("C2.chi_name",lsChiName);
      wp.itemSet("C2.contact_tel_no",lsContactTelNo);
      wp.itemSet("C2.order_no",lsOrderNo);
      wp.itemSet("C2.service_no",lsServiceNo);
      wp.itemSet("C2.service_name",lsServiceName);
      wp.itemSet("C2.service_code",lsServiceCode);
      wp.itemSet("C2.service_item",lsServiceItem);

      if (func.dbInsertC2() == 1) {
         llOk++;
      }
   }

   if (llOk > 0) {
      sqlCommit(1);
   }
   else {
      sqlCommit(-1);
   }

   tf.closeInputText(fi);
   tf.deleteFile(inputFile);

//   wp.notFound = "N";
   alertMsg("資料匯入處理筆數: " + (llCnt-1) + ", 成功筆數=" + llOk+"; 匯入批號="+wp.itemStr("C2.batch_no"));
   wp.colSet("zz_file_name", "");
   return;
}

private boolean checkFileExists(String filename) {
    wp.sqlCmd = "select 1 from bil_mcht_apply_tmp where FILE_NAME = ?";
    pageSelect(new Object[] {
            filename
    });
    if (sqlNotFind()) {
        return false;
    }
    return true;
}

@Override
public void queryFunc() throws Exception {

}

void queryAfter() throws Exception  {
   double lmChAmt = 0, lmGuestAmt = 0;
   String lsPpcardNo = "";
   int llErr = 0, liFree = 0, liUse = 0;
   busi.func.UcPpcard ucpp = new busi.func.UcPpcard();
   ucpp.setConn(wp);

   for (int ii = 0; ii < ilTotCnt; ii++) {

      lsPpcardNo = wp.colStr(ii, "pp_card_no");
//			if(ucpp.get_Cardholder(ls_ppcard_no)!=1){
//				wp.col_set(ii,"proc_flag", "N");
//				wp.col_set(ii,"db_errmsg", ucpp.getMsg());
//				ll_err++;
//				continue;
//			}

      String ls_card_no = ucpp.getCardNo(lsPpcardNo);
      if (empty(ls_card_no)) {
         llErr++;
         wp.colSet(ii, "proc_flag", "N");
         wp.colSet(ii, "db_errmsg", ucpp.getMsg());
         continue;
      }
      wp.colSet(ii, "card_no", ls_card_no);
      wp.colSet(ii, "id_no", ucpp.idNo);
      wp.colSet(ii, "id_no_code", ucpp.idnoCode);
      wp.colSet(ii, "id_p_seqno", ucpp.idPseqno);
      wp.colSet(ii, "mcht_no", ucpp.mchtNo);

      //--check free use
      liFree = ucpp.getFreeCnt(lsPpcardNo, wp.colStr(ii, "visit_date"));
      liUse = (int) wp.colNum("cardholder_visits");

      if (liFree >= liUse) {
         wp.colSet(ii, "free_use_cnt", liUse);
      }
      else {
         wp.colSet(ii, "free_use_cnt", liFree);
      }

      if (ucpp.getVmjAmt(wp.colStr(ii, "bin_type")) == 1) {
         wp.colSet(ii, "ch_cost_amt", ucpp.imHolderAmt);
         wp.colSet(ii, "guest_cost_amt", ucpp.imGuestAmt);
      }

      lmChAmt = (wp.colNum(ii, "cardholder_visits") - wp.colNum(ii, "free_use_cnt")) * wp.colNum(ii, "ch_cost_amt ");
      wp.colSet(ii, "wk_ch_amt", lmChAmt);

      lmGuestAmt = wp.colNum("guest_cost_amt") * wp.colNum("guests_count");
      wp.colSet(ii, "wk_guest_amt", lmGuestAmt);
   }
}

@Override
public void queryRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void saveFunc() throws Exception {
    if ("D".equals(strAction)) {
        if (wp.iempty("ex_file_type") || wp.iempty("ex_batch_no")) {
            alertErr("[檔案類別, 匯入批號] 不可空白");
            return;
        }
    }

   Cmsm5010Func func = new Cmsm5010Func();
   func.setConn(wp);
//   func.actionInit(strAction);

   switch (strAction) {
      case "A":
         rc =func.dbInsert(); break;
      case "U":
         rc =func.dbUpdate(); break;
      case "D":
         rc =func.dbDelete(); break;
   }
   this.sqlCommit(rc);

   if (rc !=1) {
      alertErr(func.getMsg());
      return;
   }
   alertMsg(func.getMsg());
}

@Override
public void procFunc() throws Exception {
   int llOk = 0, llErr = 0;

   Cmsm5010Func func = new Cmsm5010Func();
   func.setConn(wp);

   for (int ii = 0; ii < ilTotCnt; ii++) {
      if (wp.colEq(ii, "proc_flag", "N") == false) {
         int liRc = func.insertVist(ii);
         if (liRc == 1) {
            llOk++;
            continue;
         }
         wp.colSet(ii, "db_errmsg", func.getMsg());
      }
      //-error-
      llErr++;
      listErrData(ii);
   }
   if (llOk > 0) {
      sqlCommit(1);   //one-time-commit
   }

   //wp.listCount[0] = ll_err;
   wp.setListSernum(0, "", llErr);
   alertMsg("資料匯入完成 , 成功:" + llOk + " 失敗:" + llErr);
   
   if(llErr>0) {
   	try {
         log("xlsFunction: started--------");
         wp.reportId = "cmsm5010";
         wp.colSet("user_id", wp.loginUser);
         TarokoExcel xlsx = new TarokoExcel();
         wp.fileMode = "Y";
         xlsx.excelTemplate = "cmsm5010.xlsx";
         wp.pageRows = 9999;
         xlsx.processExcelSheet(wp);
         xlsx.outputExcelUrl();
         String lsUrl = wp.linkURL;
         xlsx = null;
         wp.colSet("url_err_file", lsUrl);
         wp.colSet("err_file", wp.exportFile);
         wp.linkURL = "";
         wp.linkMode = "";
         log("xlsFunction: ended-------------");
      }
      catch (Exception ex) {
         wp.expMethod = "xlsPrint";
         wp.expHandle(ex);
      }
   }      
}

void listErrData(int ll) {
   rr++;

   wp.colSet(rr, "er_crt_date", wp.colStr(ll, "crt_date"));
   wp.colSet(rr, "er_bin_type", wp.colStr(ll, "bin_type"));
   wp.colSet(rr, "er_data_seqno", wp.colStr(ll, "data_seqno"));
   wp.colSet(rr, "er_pp_card_no", wp.colStr(ll, "pp_card_no"));
   wp.colSet(rr, "er_from_type", wp.colStr(ll, "from_type"));
   wp.colSet(rr, "er_terminal_no", wp.colStr(ll, "terminal_no"));
   wp.colSet(rr, "er_city", wp.colStr(ll, "use_city"));
   wp.colSet(rr, "er_imp_file_name", wp.colStr(ll, "imp_file_name"));
   wp.colSet(rr, "er_free_use_cnt", wp.colStr(ll, "free_use_cnt"));
   wp.colSet(rr, "er_ch_cost_amt", wp.colStr(ll, "ch_cost_amt"));
   wp.colSet(rr, "er_guest_cost_amt", wp.colStr(ll, "guest_cost_amt"));
   wp.colSet(rr, "er_crt_user", wp.colStr(ll, "crt_user"));
   wp.colSet(rr, "er_id_no", wp.colStr(ll, "id_no"));
   wp.colSet(rr, "er_id_no_code", wp.colStr(ll, "id_no_code"));
   wp.colSet(rr, "er_id_p_seqno", wp.colStr(ll, "id_p_seqno"));
   wp.colSet(rr, "er_mcht_no", wp.colStr(ll, "mcht_no"));
   wp.colSet(rr, "er_card_no", wp.colStr(ll, "card_no"));
   wp.colSet(rr, "er_bank_name", wp.colStr(ll, "bank_name"));
   wp.colSet(rr, "er_deal_type", wp.colStr(ll, "deal_type"));
   wp.colSet(rr, "er_associate_code", wp.colStr(ll, "associate_code"));
   wp.colSet(rr, "er_ica_no", wp.colStr(ll, "ica_no"));
   wp.colSet(rr, "er_cardholder_ename", wp.colStr(ll, "cardholder_ename"));
   wp.colSet(rr, "er_visit_date", wp.colStr(ll, "visit_date"));
   wp.colSet(rr, "er_lounge_name", wp.colStr(ll, "lounge_name"));
   wp.colSet(rr, "er_lounge_code", wp.colStr(ll, "lounge_code"));
   wp.colSet(rr, "er_domestic_int", wp.colStr(ll, "domestic_int"));
   wp.colSet(rr, "er_iso_conty", wp.colStr(ll, "iso_conty"));
   wp.colSet(rr, "er_iso_conty_code", wp.colStr(ll, "iso_conty_code"));
   wp.colSet(rr, "er_cardholder_visits", wp.colStr(ll, "cardholder_visits"));
   wp.colSet(rr, "er_guests_count", wp.colStr(ll, "guests_count"));
   wp.colSet(rr, "er_total_visits", wp.colStr(ll, "total_visits"));
   wp.colSet(rr, "er_batch_no", wp.colStr(ll, "batch_no"));
   wp.colSet(rr, "er_voucher_no", wp.colStr(ll, "voucher_no"));
   wp.colSet(rr, "er_mc_billing_region", wp.colStr(ll, "mc_billing_region"));
   wp.colSet(rr, "er_curr_code", wp.colStr(ll, "curr_code"));
   wp.colSet(rr, "er_fee_per_holder", wp.colStr(ll, "fee_per_holder"));
   wp.colSet(rr, "er_fee_per_guest", wp.colStr(ll, "fee_per_guest"));
   wp.colSet(rr, "er_total_fee", wp.colStr(ll, "total_fee"));
   wp.colSet(rr, "er_total_free_guests", wp.colStr(ll, "total_free_guests"));
   wp.colSet(rr, "er_free_guests_value", wp.colStr(ll, "free_guests_value"));
   wp.colSet(rr, "er_tot_charg_guest", wp.colStr(ll, "tot_charg_guest"));
   wp.colSet(rr, "er_charg_guest_value", wp.colStr(ll, "charg_guest_value"));
   wp.colSet(rr, "er_billing_region", wp.colStr(ll, "billing_region"));
   wp.colSet(rr, "er_errmsg", wp.colStr(ll, "db_errmsg"));
}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

@Override
public void xlsPrint() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void logOnlineApprove() throws Exception {
   // TODO Auto-generated method stub

}

}
