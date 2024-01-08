package Cms;
/**
 * 2023-1011 V1.00.01   JH    bugfix
 * 2023-0529 V1.00.00   JH    initial
 * */
import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class CmsC007 extends com.BaseBatch {
private final String PROGNAME = "龍騰卡使用檔匯入處理  2023-1011 V1.00.01";
hdata.EcsFtpLog hEflg =new hdata.EcsFtpLog();
HH hh=new HH();
//-----------
class HH {
   String file_date="";
   String pp_card_no="";
   String purch_date="";
   int card_man=0;
   int bonda_man=0;
   int tot_man=0;
   String guest_name="";
   String use_country="";
   //-------
   String err_code="";
   String err_desc="";
   String id_no="";
   String id_pseqno="";
   String card_no="";
   String bin_type="";
   void init_data() {
      pp_card_no="";
      purch_date="";
      card_man=0;
      bonda_man=0;
      tot_man=0;
      guest_name="";
      use_country="";
      //
      err_code="";
      err_desc="";
      id_no="";
      id_pseqno="";
      card_no="";
      bin_type="";
   }
}
//---------
int iiFileNum=-1;
//============================================================

//========================================
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CmsC007 [busi_date(08), callbatch_seqno]");
      okExit(0);
   }

   dbConnect();

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate = Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
      callBatchSeqno(args[0]);
   }
   if (args.length == 2) {
      callBatchSeqno(args[1]);
   }
   callBatch(0, 0, 0);


   selectEcsFtpLog();

   sqlCommit();
   endProgram();
}
//=========
void selectEcsFtpLog() throws Exception {
   int liFileCnt=0;
   sqlCmd = "select trans_seqno ,"
           + " file_name ,"
           + " group_id ,"		//-batch_no-
           + " hex(rowid) as rowid "
           + " from ecs_ftp_log "
           + " where 1=1"
//           +" and system_id = 'MCHT_APPLY' "
           + " and trans_resp_code ='Y' "
           + " and proc_code <> 'Y' "
           +" and file_name like 'GUEST_68861403%' "
//           + " and source_from ='05' "
//           + " and crt_date = ? "
           + " order by file_date , file_name ";

   daoTid ="ftp.";
//   ppp(1, hBusiDate);
   sqlSelect();
   int llNrow = sqlNrow;
   for (int ll=0; ll<llNrow; ll++) {
      hEflg.initData();

      hEflg.transSeqno = colSs("ftp.trans_seqno");
      hEflg.fileName = colSs("ftp.file_name");
      hEflg.groupId = colSs("ftp.group_id");		//-batch_no-
      hEflg.rowid = colSs("ftp.rowid");

      liFileCnt++;
      fileOpen(hEflg.fileName);
      if (iiFileNum <0) continue;
      fileRead();

      updateEcsFtpLog();
      sqlCommit();
   }

   if (liFileCnt == 0) {
      printf("今日無資料處理");
      this.okExit(0);
   }

}
//======
void fileRead() throws Exception {
   int liDataCnt=0, liTotCnt=0;

   while (true && totalCnt <99999) {
      totalCnt++;

      int liRc =0;
      String txt = this.readTextFile(iiFileNum);
      if (empty(txt))
         break;

      if (txt.length() < 19)
         continue;
      String[] tt=new String[]{txt,""};
      String ss=commString.bbToken(tt,1);
      //-Header-
      if (eq(ss,"1")) {
         ss = commString.bbToken(tt,10);  //68861403
         hh.file_date =commString.bbToken(tt,8);
         continue;
      }
      //-Footer-
      if (eq(ss,"3")) {
         ss = commString.bbToken(tt,10);  //68861403
         ss =commString.bbToken(tt,9);
         liTotCnt =commString.ss2int(ss);
         continue;
      }
      //-Detail-
      hh.init_data();
      liDataCnt++;
      hh.pp_card_no =commString.bbToken(tt,16).trim();
      hh.purch_date =commString.bbToken(tt,8).trim();
      ss =commString.bbToken(tt,2);
      hh.card_man =ss2int(ss);
      ss =commString.bbToken(tt,2);
      hh.bonda_man =ss2int(ss);
      ss =commString.bbToken(tt,3);
      hh.tot_man =ss2int(ss);
      hh.guest_name =commString.bbToken(tt,20).trim();
      hh.use_country =commString.bbToken(tt,20).trim();

      //--
      hh.err_code="00";
      if (empty(hh.pp_card_no)) {
         hh.err_code="01";
         hh.err_desc ="PP卡號空白";
      }
      if (eq(hh.err_code,"00")) {
         if (hh.card_man <=0 && hh.bonda_man <=0) {
            hh.err_code="07";
            hh.err_desc ="使用次數及攜帶人數同時<=0";
         }
      }
      if (eq(hh.err_code,"00")) {
         selectCrd_card_pp();
      }

      liRc=insertCms_ppcard_visit(liDataCnt);
      if (liRc !=0) {
         printf("error:[%s]",txt);
      }
   }
   closeInputText(iiFileNum);

   printf("file[%s], 匯入筆數[%s]", hEflg.fileName, liDataCnt);
   if (liDataCnt != liTotCnt) {
      hEflg.procDesc ="明細筆數與尾部總筆數不合";
   }
}

//=========
void fileOpen(String afileName) throws Exception {

   String lsPath = getEcsHome() + "/media/cms/" + afileName;
   printf("open file [%s]", lsPath);
   iiFileNum = this.openInputText(lsPath);
   if (iiFileNum < 0) {
      printf("在程式執行目錄下沒有權限讀寫資料, file[cms/%s]",afileName);
      okExit(0);
   }
   //---
//   String lsPath_err = getEcsHome() + "/media/cms/" + afileName+".err";
//   printf("open err-file [%s]", lsPath_err);
//   iiFileNum_err = this.openOutputText(lsPath_err);
//   if (iiFileNum_err < 0) {
//      printf("在程式執行目錄下沒有權限讀寫資料, file[cms/%s]",afileName+".err");
//      okExit(0);
//   }
}
//=========
int tiPpcard=-1;
void selectCrd_card_pp() throws Exception {
   if (tiPpcard <=0) {
      sqlCmd ="select A.id_p_seqno, A.eng_name, A.bin_type"+
              ", B.id_no, C.card_no"+
              " from crd_card_pp A"+
              "   left join crd_idno B on B.id_p_seqno=A.id_p_seqno"+
              "   LEFT JOIN crd_card C ON C.card_no=A.card_no"+
              " where A.pp_card_no =?"
              ;
      tiPpcard =ppStmtCrt("ti-S-ppcard","");
   }
   ppp(1, hh.pp_card_no);
   sqlSelect(tiPpcard);
   if (sqlNrow <=0) {
      hh.err_code ="02";
      hh.err_desc="PP卡號不存在";
      return;
   }

   hh.card_no =colSs("card_no");
   hh.id_no =colSs("id_no");
   hh.id_pseqno =colSs("id_p_seqno");
   hh.bin_type =colSs("bin_type");
   if (empty(hh.id_pseqno)) {
      hh.err_code ="03";
      hh.err_desc="持卡人ID_P_SEQNO不存在";
      return;
   }
   if (empty(hh.id_no)) {
      hh.err_code ="04";
      hh.err_desc="持卡人ID不存在";
      return;
   }
   if (empty(hh.card_no)) {
      hh.err_code ="06";
      hh.err_desc="信用卡卡號不存在";
      return;
   }
   String ls_ename=colSs("eng_name");
   if (noEmpty(ls_ename) && noEmpty(hh.guest_name) &&
           !eq(ls_ename, hh.guest_name)) {
      hh.err_code ="05";
      hh.err_desc="持卡人英文姓名不同";
      return;
   }

}
//=======
com.Parm2sql ttAvisit=null;
int  insertCms_ppcard_visit(int aiDataSeq) throws Exception {
   if (ttAvisit ==null) {
      ttAvisit =new com.Parm2sql();
      ttAvisit.insert("cms_ppcard_visit");
   }

   ttAvisit.aaa("crt_date", sysDate);            	//-VARCHAR (8,0)  鍵檔日期
   ttAvisit.aaa("bin_type", hh.bin_type);            	//-VARCHAR (1,0)  卡別
   ttAvisit.aaa("data_seqno", aiDataSeq);          	//-INTEGER (4,0)  資料序號
   ttAvisit.aaa("from_type", "2");           	//-VARCHAR (1,0)  資料來源        1.人工, 2.批次
   ttAvisit.aaa("bank_name", "68861403");           	//-VARCHAR (60,0)  Bank name
   //005	deal_type           	//-VARCHAR (30,0)  Deal Type
   //006	associate_code      	//-VARCHAR (20,0)  Associate Code
   //007	ica_no              	//-VARCHAR (8,0)  ICA
   ttAvisit.aaa("pp_card_no", hh.pp_card_no);          	//-VARCHAR (20,0)  Priority Pass Id         pp_card_no
   ttAvisit.aaa("ch_ename", hh.guest_name);            	//-VARGRAPH(30,0)  Cardholder Name
   ttAvisit.aaa("visit_date", hh.purch_date);          	//-VARCHAR (8,0)  Visit Date
   //011	lounge_name         	//-VARGRAPH(50,0)  Lounge Name
   //012	lounge_code         	//-VARCHAR (10,0)  Lounge Code
   //013	domestic_int        	//-VARCHAR (5,0)  Domestic or INT
   ttAvisit.aaa("iso_conty", hh.use_country);           	//-VARCHAR (20,0)  ISO Country
   //015	iso_conty_code      	//-VARCHAR (5,0)  Visit ISO Country Code
   ttAvisit.aaa("ch_visits", hh.card_man);           	//-INTEGER (4,0)  Cardholder Visits
   ttAvisit.aaa("guests_count", hh.bonda_man);        	//-INTEGER (4,0)  Guests Count
   ttAvisit.aaa("total_visits", hh.tot_man);        	//-INTEGER (4,0)  Total Visit Count
   //019	batch_no            	//-INTEGER (4,0)  Batch no
   //020	voucher_no          	//-VARCHAR (30,0)  Voucher Number
   //021	mc_billing_region   	//-VARCHAR (10,0)  MC Billing Region of Issue
   //022	curr_code           	//-VARCHAR (10,0)  幣別
   //023	fee_per_holder      	//-INTEGER (4,0)  Fee Per Member
   //024	fee_per_guest       	//-INTEGER (4,0)  Fee Per Guest
   //025	total_fee           	//-INTEGER (4,0)  Total Fee
   //026	total_free_guests   	//-INTEGER (4,0)  Total FREE Guests
   //027	free_guests_value   	//-INTEGER (4,0)  FREE Guests Value
   //028	tot_charg_guest     	//-INTEGER (4,0)  Total Chargeable Guests
   //029	charg_guest_value   	//-INTEGER (4,0)  Chargeable Guest Value
   //030	billing_region      	//-VARCHAR (20,0)  Billing Region
   //031	terminal_no         	//-VARCHAR (30,0)  Terminal
   //032	use_city            	//-VARGRAPH(30,0)  City
   ttAvisit.aaa("id_no", hh.id_no);               	//-VARCHAR (10,0)  卡人ID
   //034	id_no_code          	//-VARCHAR (1,0)  卡人ID識別碼
   ttAvisit.aaa("id_p_seqno", hh.id_pseqno);          	//-VARCHAR (10,0)  卡人流水號
   //036	free_use_cnt        	//-INTEGER (4,0)  免費使用次數
   //037	guest_free_cnt      	//-INTEGER (4,0)  非卡友免費次數
   //038	ch_cost_amt         	//-INTEGER (4,0)  卡友自費金額
   //039	guest_cost_amt      	//-INTEGER (4,0)  非卡友自費金額
   ttAvisit.aaa("card_no", hh.card_no);             	//-VARCHAR (19,0)  卡號
   //041	mcht_no             	//-VARCHAR (15,0)  特店代號
   //042	user_remark         	//-VARGRAPH(60,0)  備註
   ttAvisit.aaa("crt_user", hModUser);            	//-VARCHAR (10,0)  建檔人員
   ttAvisit.aaa("imp_file_name", hEflg.fileName);       	//-VARCHAR (50,0)  資料檔名
   ttAvisit.aaaModxxx(hModUser, hModPgm);
   ttAvisit.aaa("item_no", "10");             	//-VARCHAR (2,0)  權益項目
   ttAvisit.aaa("vip_kind", "2");            	//-VARCHAR (1,0)  加贈免費次數
   //051	in_person_count     	//-INTEGER (4,0)  攜伴總人數
   //052	in_file_type        	//-VARCHAR (1,0)  PP卡:IN-FILE-TYPE
   //053	pymt_cond           	//-VARCHAR (2,0)  PP卡:收費狀況(程式檢核結果寫入)
   //054	pymt_fail_count     	//-INTEGER (4,0)  PP卡:收費失敗持卡人使用數總計
   //055	pymt_fail_person_cou	//-INTEGER (4,0)  PP卡:收費失敗攜伴總入數
   //056	pymt_fail_tot_count 	//-INTEGER (4,0)  PP卡:收費失敗總筆數
   ttAvisit.aaa("err_code", hh.err_code);            	//-VARCHAR (2,0)  錯誤碼
   ttAvisit.aaa("err_desc", hh.err_desc);            	//-VARGRAPH(100,0)  錯誤說明
   //059	cal_flag            	//-VARCHAR (1,0)  權益統計處理
   //060	cal_date            	//-VARCHAR (8,0)  權益統計日期
   //061	use_flag            	//-VARCHAR (1,0)  權益使用處理
   //062	use_date            	//-VARCHAR (9,0)  權益使用日期
   //063	free_cnt            	//-INTEGER (4,0)  免費次數
   //064	expn_cnt            	//-INTEGER (4,0)  自費次數
   //065	free_cnt2           	//-INTEGER (4,0)  加贈免費次數
   //066	cal_seqno           	//-DECIMAL (10,0)  權益流水號
   //067	free_proc_result    	//-VARGRAPH(50,0)  檢核結果

   if (ttAvisit.ti <=0) {
      ttAvisit.ti =ppStmtCrt("ti-A-visit", ttAvisit.getSql());
   }

//   debug=true;
//   dddSql(ttAvisit.ti, ttAvisit.getConvParm(false));
//   debug=true;

   sqlExec(ttAvisit.ti, ttAvisit.getParms());
   if (sqlNrow <=0) {
      printf("insert cms_ppcard_visit error, kk[%s]", hh.pp_card_no);
      return 1;
   }
   return 0;
}
//--------
void updateEcsFtpLog() throws Exception {
   sqlCmd = "update ecs_ftp_log set "
           + " proc_code ='Y' ,"
           + " proc_desc = ? ,"
           + " mod_time = sysdate , "
           + " mod_pgm = ? "
           +commSqlStr.whereRowid(hEflg.rowid)
   ;
   ppp(1, hEflg.procDesc);
   ppp(hModPgm);
   //---
//   ppp(hEflg.rowid);
   sqlExec(sqlCmd);
   if (sqlNrow <=0) {
      sqlerr("update ecs_ftp_log error");
      errExit(1);
   }
}

public static void main(String[] args) {
   CmsC007 proc = new CmsC007();
//	proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}
}
