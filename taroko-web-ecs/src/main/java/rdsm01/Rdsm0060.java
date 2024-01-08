package rdsm01;

import taroko.com.TarokoFileAccess;
import taroko.com.TarokoPDF;

/**
 * 2023-0417.00   JH    initial
 * */

public class Rdsm0060 extends ofcapp.BaseAction {
@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "UPLOAD":
         doFileUpload(); break;
      case "PDF":
         doPrint_pdf(); break;
      default:
         defaultAction();
   }

}

void doPrint_pdf() throws Exception {
   wp.reportId = "rdsm0060";
   String cond1 = "";
   cond1 += " 匯入日期 : " + commString.strToYmd(wp.itemStr("ex_crt_date1")) + " -- "
           + commString.strToYmd(wp.itemStr("ex_crt_date2"));
   if (wp.itemEq("ex_proc_result", "1")) {
      cond1 += "    檢核結果:成功";
   } else if (wp.itemEq("ex_proc_result", "2")) {
      cond1 += "    檢核結果:失敗";
   } else {
      cond1 += "    檢核結果:全部";
   }
   cond1 +="    匯入檔名:"+wp.itemStr("ex_file_name");
   wp.colSet("cond1", cond1);

   printPdf_summ();
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rdsm0060.xlsx";
   pdf.pageCount = 33;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;
}

void printPdf_summ() {
   String sql1 ="select sum(decode(free_proc_result,'',1,0)) as ok_cnt"+
           ", sum(decode(free_proc_result,'',0,1)) as err_cnt"+
           " from bil_mcht_apply_tmp"+
           queryWhere()
           ;
   sqlSelect(sql1);
   if (sqlRowNum <=0) return;

   String lsProc=" 成功: "+sqlInt("ok_cnt")+" 筆  失敗: "+sqlInt("err_cnt")+" 筆";
   wp.colSet("ttl_proc_result", lsProc);
}

void doFileUpload() throws Exception {
   String lsFileName=wp.itemStr("zz_file_name");
   if (empty(lsFileName)) {
      alertErr("匯入檔案 不可空白");
      return;
   }

   fileImport();
}

void fileImport() throws Exception {
   int ll_err = 0;
   TarokoFileAccess tf = new TarokoFileAccess(wp);

   String inputFile = wp.itemStr("zz_file_name");
   int fi = tf.openInputText(inputFile, "MS950");
   if (fi == -1) {
      return;
   }

   //int file_err = tf.openOutputText(inputFile + ".err", "UTF-8");

   Rdsm0060Func func = new Rdsm0060Func();
   func.setConn(wp);

   //wp.itemSet("imp_file_name", wp.col_ss("zz_file_name"));

   int ll_ok = 0, ll_cnt = 0; String ss="";
   while (true) {
      String txt = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
         break;
      }
      if (txt.length() < 2) {
         continue;
      }

      ll_cnt++;
      wp.itemSet("tx_data",txt);

      rc=func.dbInsert();
      sqlCommit(1);
      if (rc !=1) {
         ll_err++;
         continue;
      }
      ll_ok++;
   }

   tf.closeInputText(fi);
//   tf.deleteFile(inputFile);

//   if (ll_err > 0) {
//      wp.setDownload(inputFile + ".err");
//   }

   alertMsg("資料匯入處理筆數: " + ll_cnt + ", 成功筆數=" + ll_ok+", 錯誤筆數="+ll_err);
//   wp.col_set("zz_file_name", "");
   return;
}

@Override
public void dddwSelect() {

}

String queryWhere() {
   String lsWhere = "";
   lsWhere = " where file_type='05' "
           +sqlCol(wp.itemStr("ex_crt_date1"),"crt_date",">=")
           +sqlCol(wp.itemStr("ex_crt_date2"),"crt_date","<=")
           +sqlCol(wp.itemStr("ex_file_name"),"file_name", "like%")
   ;

   String lsProc=wp.itemStr("ex_proc_result");
   if (eqIgno(lsProc,"1")) {
      lsWhere +=" and free_proc_result ='' ";
   }
   else if (eqIgno(lsProc,"2")) {
      lsWhere +=" and free_proc_result <>'' ";
   }

   return lsWhere;
}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_crt_date1,ex_crt_date2,ex_file_name")) {
      alertErr("查詢條件: 不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("匯入日期: 起迄錯誤");
      return;
   }

   wp.whereStr =queryWhere();
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
           +" crt_date, data_seqno"+
           ", service_code, service_name"+
           ", id_no, card_no, car_no, chi_name"+
           ", purchase_date, purchase_date_e"+
           ", file_name, free_proc_result"+
           ", substr(service_no,1,3) as card_no3"+
           ", substr(service_no,4,7) as id_no7"+
           ", substr(service_no,11,6) as car_no6"+
           ", service_code||' '||service_name as db_pcard"
   ;
   wp.daoTable = "bil_mcht_apply_tmp";
   wp.whereOrder = " order by crt_date, file_name, data_seqno ";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {

}

@Override
public void dataRead() throws Exception {

}

@Override
public void saveFunc() throws Exception {

}

@Override
public void procFunc() throws Exception {

}

@Override
public void initButton() {

}

@Override
public void initPage() {

}
}
