/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-04-25  V1.00.01  Alex        program initial                          *
 *  2323-1006 V1.00.02  JH    show process Month
 *  2323-1120 V1.00.03  JH    twMonth=busiDate-1MM
 *  2323-1128 V1.00.04  JH    ++b_code_flag
 *****************************************************************************/
package Rsk;

import com.*;

import java.text.DecimalFormat;

public class RskP197 extends BaseBatch {
private final String progname = "產生給風險管理部 LGD/EAD 模型檔案產生 2323-1128 V1.00.04";
//	CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;

String fileName = "";
String fileName2 = "";
private int iiFileNum = 0;
private int iiFileNum2 = 0;
String twMonth = "";

String branch = "";
String idNo = "";
String haveCard = "";
String bCode_flag = "";
double cycleRatio1 = 0;
double cycleRatio2 = 0;
double cycleRatio3 = 0;
double cycleRatio4 = 0;
double cycleRatio5 = 0;
double cycleRatio6 = 0;
double cycleRatio7 = 0;
double cycleRatio8 = 0;
double cycleRatio9 = 0;
double cycleRatio10 = 0;
double cycleRatio11 = 0;
double cycleRatio12 = 0;
double lineOfCreditAmt1 = 0;
double lineOfCreditAmt2 = 0;
double lineOfCreditAmt3 = 0;
double lineOfCreditAmt4 = 0;
double lineOfCreditAmt5 = 0;
double lineOfCreditAmt6 = 0;
double lineOfCreditAmt7 = 0;
double lineOfCreditAmt8 = 0;
double lineOfCreditAmt9 = 0;
double lineOfCreditAmt10 = 0;
double lineOfCreditAmt11 = 0;
double lineOfCreditAmt12 = 0;
double lineOfCreditAmtCash1 = 0;
double lineOfCreditAmtCash2 = 0;
double lineOfCreditAmtCash3 = 0;
double lineOfCreditAmtCash4 = 0;
double lineOfCreditAmtCash5 = 0;
double lineOfCreditAmtCash6 = 0;
double lineOfCreditAmtCash7 = 0;
double lineOfCreditAmtCash8 = 0;
double lineOfCreditAmtCash9 = 0;
double lineOfCreditAmtCash10 = 0;
double lineOfCreditAmtCash11 = 0;
double lineOfCreditAmtCash12 = 0;
double useCreditAmt1 = 0;
double useCreditAmt2 = 0;
double useCreditAmt3 = 0;
double useCreditAmt4 = 0;
double useCreditAmt5 = 0;
double useCreditAmt6 = 0;
double useCreditAmt7 = 0;
double useCreditAmt8 = 0;
double useCreditAmt9 = 0;
double useCreditAmt10 = 0;
double useCreditAmt11 = 0;
double useCreditAmt12 = 0;
double amtBalance1 = 0;
double amtBalance2 = 0;
double amtBalance3 = 0;
double amtBalance4 = 0;
double amtBalance5 = 0;
double amtBalance6 = 0;
double amtBalance7 = 0;
double amtBalance8 = 0;
double amtBalance9 = 0;
double amtBalance10 = 0;
double amtBalance11 = 0;
double amtBalance12 = 0;
double cashBalance1 = 0;
double cashBalance2 = 0;
double cashBalance3 = 0;
double cashBalance4 = 0;
double cashBalance5 = 0;
double cashBalance6 = 0;
double cashBalance7 = 0;
double cashBalance8 = 0;
double cashBalance9 = 0;
double cashBalance10 = 0;
double cashBalance11 = 0;
double cashBalance12 = 0;
String mCode1 = "";
String mCode2 = "";
String mCode3 = "";
String mCode4 = "";
String mCode5 = "";
String mCode6 = "";
String mCode7 = "";
String mCode8 = "";
String mCode9 = "";
String mCode10 = "";
String mCode11 = "";
String mCode12 = "";
double consumeAmt1 = 0;
double consumeAmt2 = 0;
double consumeAmt3 = 0;
double consumeAmt4 = 0;
double consumeAmt5 = 0;
double consumeAmt6 = 0;
double consumeAmt7 = 0;
double consumeAmt8 = 0;
double consumeAmt9 = 0;
double consumeAmt10 = 0;
double consumeAmt11 = 0;
double consumeAmt12 = 0;

//--write report
String trunConsume1 = "";
String trunConsume2 = "";
String trunConsume3 = "";
String trunConsume4 = "";
String trunConsume5 = "";
String trunConsume6 = "";
String trunConsume7 = "";
String trunConsume8 = "";
String trunConsume9 = "";
String trunConsume10 = "";
String trunConsume11 = "";
String trunConsume12 = "";

public static void main(String[] args) {
   RskP197 proc = new RskP197();
   // proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP197 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }

//		if (empty(hBusiDate))
//			hBusiDate = comc.getBusiDate();

   dbConnect();

//   twMonth = commDate.toTwDate(commDate.dateAdd(hBusiDate, 0, -1, 0)).substring(0, 5);
   selectPtr_workday();
   twMonth = commDate.toTwDate(is_acctMonth+"01").substring(0, 5);
   printf("-- process twMonth=[%s], this_acct_month[%s]", twMonth, is_acctMonth);

   fileName = "LGD.OUT.TXT";
   fileName2 = "EAD_DATA_"+hBusiDate+".TXT";
   checkOpen();
   selectLgd_ead_model();
   closeOutputText(iiFileNum);
   closeOutputText(iiFileNum2);
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   endProgram();
}
//-----
String is_acctMonth ="";
void selectPtr_workday() throws Exception {
   sqlCmd ="select max(this_acct_month) proc_acct_month"
       +" from ptr_workday"
       +" where 1=1";
   sqlSelect("");
   if (sqlNrow >0) {
      is_acctMonth =colSs("proc_acct_month");
   }
}
//----
void procFTP() throws Exception {
   //--LGD
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput "+fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
   }

   //--EAD
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "RM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   errCode = commFTP.ftplogName("RM", "mput "+fileName2);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName2+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
   }

   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   errCode = commFTP.ftplogName("CREDITCARD", "mput "+fileName2);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName2+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
   }
}

public int insertEcsNotifyLog(String fileName) throws Exception {
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_name", "媒體檔名:"+fileName);
   setValue("notify_desc1", "程式 "+javaProgram+" 無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", commFTP.hEflgTransSeqno);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";

   insertTable();

   return (0);
}

void renameFile() throws Exception {
   String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName);

   if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+fileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+fileName+"] 已移至 ["+tmpstr2+"]");

   String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileName2);
   String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName2);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+fileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+fileName+"] 已移至 ["+tmpstr2+"]");
}

void checkOpen() throws Exception {
   String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum == -1) {
      this.showLogMessage("I", "", "無檔案可處理 !");
      okExit(0);
   }

   String lsFile2 = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName2);

   iiFileNum2 = openOutputText(lsFile2, "MS950");
   if (iiFileNum == -1) {
      this.showLogMessage("I", "", "無檔案可處理 !");
      okExit(0);
   }

   return;
}

void selectLgd_ead_model() throws Exception {

   sqlCmd = "select A.* "
//       +", uf_idno_id(A.id_p_seqno) as id_no"
       +", (select id_no from crd_idno where id_p_seqno=A.id_p_seqno) as id_no"
       +" from lgd_ead_model A "
       +" where A.data_date = ? ";
   setString(1, twMonth);

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      dspProcRow(20000);

      initData();
      branch = colSs("branch");
      idNo = colSs("id_no");
      haveCard = colSs("card_flag");
      bCode_flag =colSs("b_code_flag");
      cycleRatio1 = colNum("cycle_ratio_1");
      cycleRatio2 = colNum("cycle_ratio_2");
      cycleRatio3 = colNum("cycle_ratio_3");
      cycleRatio4 = colNum("cycle_ratio_4");
      cycleRatio5 = colNum("cycle_ratio_5");
      cycleRatio6 = colNum("cycle_ratio_6");
      cycleRatio7 = colNum("cycle_ratio_7");
      cycleRatio8 = colNum("cycle_ratio_8");
      cycleRatio9 = colNum("cycle_ratio_9");
      cycleRatio10 = colNum("cycle_ratio_10");
      cycleRatio11 = colNum("cycle_ratio_11");
      cycleRatio12 = colNum("cycle_ratio_12");
      lineOfCreditAmt1 = colNum("lint_of_credit_amt_1");
      lineOfCreditAmt2 = colNum("lint_of_credit_amt_2");
      lineOfCreditAmt3 = colNum("lint_of_credit_amt_3");
      lineOfCreditAmt4 = colNum("lint_of_credit_amt_4");
      lineOfCreditAmt5 = colNum("lint_of_credit_amt_5");
      lineOfCreditAmt6 = colNum("lint_of_credit_amt_6");
      lineOfCreditAmt7 = colNum("lint_of_credit_amt_7");
      lineOfCreditAmt8 = colNum("lint_of_credit_amt_8");
      lineOfCreditAmt9 = colNum("lint_of_credit_amt_9");
      lineOfCreditAmt10 = colNum("lint_of_credit_amt_10");
      lineOfCreditAmt11 = colNum("lint_of_credit_amt_11");
      lineOfCreditAmt12 = colNum("lint_of_credit_amt_12");
      lineOfCreditAmtCash1 = colNum("lint_of_credit_amt_cash_1");
      lineOfCreditAmtCash2 = colNum("lint_of_credit_amt_cash_2");
      lineOfCreditAmtCash3 = colNum("lint_of_credit_amt_cash_3");
      lineOfCreditAmtCash4 = colNum("lint_of_credit_amt_cash_4");
      lineOfCreditAmtCash5 = colNum("lint_of_credit_amt_cash_5");
      lineOfCreditAmtCash6 = colNum("lint_of_credit_amt_cash_6");
      lineOfCreditAmtCash7 = colNum("lint_of_credit_amt_cash_7");
      lineOfCreditAmtCash8 = colNum("lint_of_credit_amt_cash_8");
      lineOfCreditAmtCash9 = colNum("lint_of_credit_amt_cash_9");
      lineOfCreditAmtCash10 = colNum("lint_of_credit_amt_cash_10");
      lineOfCreditAmtCash11 = colNum("lint_of_credit_amt_cash_11");
      lineOfCreditAmtCash12 = colNum("lint_of_credit_amt_cash_12");
      useCreditAmt1 = colNum("use_credit_amt_1");
      useCreditAmt2 = colNum("use_credit_amt_2");
      useCreditAmt3 = colNum("use_credit_amt_3");
      useCreditAmt4 = colNum("use_credit_amt_4");
      useCreditAmt5 = colNum("use_credit_amt_5");
      useCreditAmt6 = colNum("use_credit_amt_6");
      useCreditAmt7 = colNum("use_credit_amt_7");
      useCreditAmt8 = colNum("use_credit_amt_8");
      useCreditAmt9 = colNum("use_credit_amt_9");
      useCreditAmt10 = colNum("use_credit_amt_10");
      useCreditAmt11 = colNum("use_credit_amt_11");
      useCreditAmt12 = colNum("use_credit_amt_12");
      amtBalance1 = colNum("amt_balance_1");
      amtBalance2 = colNum("amt_balance_2");
      amtBalance3 = colNum("amt_balance_3");
      amtBalance4 = colNum("amt_balance_4");
      amtBalance5 = colNum("amt_balance_5");
      amtBalance6 = colNum("amt_balance_6");
      amtBalance7 = colNum("amt_balance_7");
      amtBalance8 = colNum("amt_balance_8");
      amtBalance9 = colNum("amt_balance_9");
      amtBalance10 = colNum("amt_balance_10");
      amtBalance11 = colNum("amt_balance_11");
      amtBalance12 = colNum("amt_balance_12");
      cashBalance1 = colNum("cash_balance_1");
      cashBalance2 = colNum("cash_balance_2");
      cashBalance3 = colNum("cash_balance_3");
      cashBalance4 = colNum("cash_balance_4");
      cashBalance5 = colNum("cash_balance_5");
      cashBalance6 = colNum("cash_balance_6");
      cashBalance7 = colNum("cash_balance_7");
      cashBalance8 = colNum("cash_balance_8");
      cashBalance9 = colNum("cash_balance_9");
      cashBalance10 = colNum("cash_balance_10");
      cashBalance11 = colNum("cash_balance_11");
      cashBalance12 = colNum("cash_balance_12");
      mCode1 = colSs("m_code_1");
      mCode2 = colSs("m_code_2");
      mCode3 = colSs("m_code_3");
      mCode4 = colSs("m_code_4");
      mCode5 = colSs("m_code_5");
      mCode6 = colSs("m_code_6");
      mCode7 = colSs("m_code_7");
      mCode8 = colSs("m_code_8");
      mCode9 = colSs("m_code_9");
      mCode10 = colSs("m_code_10");
      mCode11 = colSs("m_code_11");
      mCode12 = colSs("m_code_12");
      consumeAmt1 = colNum("consume_amt_1");
      consumeAmt2 = colNum("consume_amt_2");
      consumeAmt3 = colNum("consume_amt_3");
      consumeAmt4 = colNum("consume_amt_4");
      consumeAmt5 = colNum("consume_amt_5");
      consumeAmt6 = colNum("consume_amt_6");
      consumeAmt7 = colNum("consume_amt_7");
      consumeAmt8 = colNum("consume_amt_8");
      consumeAmt9 = colNum("consume_amt_9");
      consumeAmt10 = colNum("consume_amt_10");
      consumeAmt11 = colNum("consume_amt_11");
      consumeAmt12 = colNum("consume_amt_12");
      covertConsumeAmt();
      textfileLgd();
   }
   closeCursor();
}

void textfileLgd() throws Exception {

   String tmpString = "";
   StringBuffer tt = new StringBuffer();
   StringBuffer ee = new StringBuffer();
   DecimalFormat df = new DecimalFormat("0");
   String newLineA = "\r\n";

   //--資料日期 X5
   tt.append(commString.bbFixlen(twMonth, 5));

   //--分行 X4
   tt.append(commString.bbFixlen(branch, 4));

   //--身分證字號 X10
   tt.append(commString.bbFixlen(idNo, 10));

   //--客戶性質 X2 放空白
   tt.append(commString.space(2));

   //--貸款額度 X11 放空白
   tt.append(commString.space(11));

   //--還本繳息方式 X1 放空白
   tt.append(commString.space(1));

   //--平均相對帳齡 X4 放空白
   tt.append(commString.space(4));

   //--貸放超逾6個月註記 X1
   tt.append(commString.space(1));

   //--是否有本行信用卡 X1
   tt.append(commString.bbFixlen(haveCard, 1));

   //--信貸平均還款本金 X11 放空白
   tt.append(commString.space(11));

   //--房貸平均放款本金 X11 放空白
   tt.append(commString.space(11));

   //--是否有房貸 X1 放空白
   tt.append(commString.space(1));

   //--違約註記 X1 放空白
//   tt.append(commString.space(1));
   tt.append(commString.bbFixlen(bCode_flag,1));
   //--X324 放空白
   tt.append(commString.space(324));

   //--X6 近1個月循環信用比率
   tmpString = df.format(cycleRatio1 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近2個月循環信用比率
   tmpString = df.format(cycleRatio2 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近3個月循環信用比率
   tmpString = df.format(cycleRatio3 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近4個月循環信用比率
   tmpString = df.format(cycleRatio4 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近5個月循環信用比率
   tmpString = df.format(cycleRatio5 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近6個月循環信用比率
   tmpString = df.format(cycleRatio6 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近7個月循環信用比率
   tmpString = df.format(cycleRatio7 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近8個月循環信用比率
   tmpString = df.format(cycleRatio8 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近9個月循環信用比率
   tmpString = df.format(cycleRatio9 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近10個月循環信用比率
   tmpString = df.format(cycleRatio10 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近11個月循環信用比率
   tmpString = df.format(cycleRatio11 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X6 近12個月循環信用比率
   tmpString = df.format(cycleRatio12 * 10000);
   tt.append(commString.lpad(tmpString, 6, "0"));

   //--X11 近1個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt1);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近2個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt2);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近3個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt3);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近4個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt4);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近5個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt5);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近6個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt6);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近7個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt7);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近8個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt8);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近9個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt9);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近10個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt10);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近11個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt11);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近12個月信用卡永久額度
   tmpString = df.format(lineOfCreditAmt12);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近1個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash1);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近2個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash2);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近3個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash3);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近4個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash4);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近5個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash5);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近6個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash6);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近7個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash7);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近8個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash8);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近9個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash9);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近10個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash10);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近11個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash11);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近12個月預借現金額度
   tmpString = df.format(lineOfCreditAmtCash12);
   if ("0".equals(tmpString))
      tmpString = "-9999999999";
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近1個月信用卡使用額度
   tmpString = df.format(useCreditAmt1);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近2個月信用卡使用額度
   tmpString = df.format(useCreditAmt2);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近3個月信用卡使用額度
   tmpString = df.format(useCreditAmt3);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近4個月信用卡使用額度
   tmpString = df.format(useCreditAmt4);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近5個月信用卡使用額度
   tmpString = df.format(useCreditAmt5);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近6個月信用卡使用額度
   tmpString = df.format(useCreditAmt6);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近7個月信用卡使用額度
   tmpString = df.format(useCreditAmt7);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近8個月信用卡使用額度
   tmpString = df.format(useCreditAmt8);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近9個月信用卡使用額度
   tmpString = df.format(useCreditAmt9);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近10個月信用卡使用額度
   tmpString = df.format(useCreditAmt10);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近11個月信用卡使用額度
   tmpString = df.format(useCreditAmt11);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近12個月信用卡使用額度
   tmpString = df.format(useCreditAmt12);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近1個月循環餘額
   tmpString = df.format(amtBalance1);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近2個月循環餘額
   tmpString = df.format(amtBalance2);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近3個月循環餘額
   tmpString = df.format(amtBalance3);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近4個月循環餘額
   tmpString = df.format(amtBalance4);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近5個月循環餘額
   tmpString = df.format(amtBalance5);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近6個月循環餘額
   tmpString = df.format(amtBalance6);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近7個月循環餘額
   tmpString = df.format(amtBalance7);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近8個月循環餘額
   tmpString = df.format(amtBalance8);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近9個月循環餘額
   tmpString = df.format(amtBalance9);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近10個月循環餘額
   tmpString = df.format(amtBalance10);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近11個月循環餘額
   tmpString = df.format(amtBalance11);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近12個月循環餘額
   tmpString = df.format(amtBalance12);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近1個月預借現金餘額
   tmpString = df.format(cashBalance1);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近2個月預借現金餘額
   tmpString = df.format(cashBalance2);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近3個月預借現金餘額
   tmpString = df.format(cashBalance3);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近4個月預借現金餘額
   tmpString = df.format(cashBalance4);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近5個月預借現金餘額
   tmpString = df.format(cashBalance5);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近6個月預借現金餘額
   tmpString = df.format(cashBalance6);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近7個月預借現金餘額
   tmpString = df.format(cashBalance7);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近8個月預借現金餘額
   tmpString = df.format(cashBalance8);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近9個月預借現金餘額
   tmpString = df.format(cashBalance9);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近10個月預借現金餘額
   tmpString = df.format(cashBalance10);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近11個月預借現金餘額
   tmpString = df.format(cashBalance11);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X11 近12個月預借現金餘額
   tmpString = df.format(cashBalance12);
   tt.append(commString.lpad(tmpString, 11, "0"));

   //--X1 近1個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode1, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode2, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode3, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode4, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode5, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode6, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode7, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode8, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode9, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode10, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode11, 1));

   //--X1 近2個月信用卡繳款狀況
   tt.append(commString.bbFixlen(mCode12, 1));

   //--將LGD文件複製到EAD文件
   ee.append(tt.toString());

   //--換行符號 0D0A
   tt.append(newLineA);

   writeTextFile(iiFileNum, tt.toString());

   //--X11 近1個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume1, 11));

   //--X11 近2個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume2, 11));

   //--X11 近3個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume3, 11));

   //--X11 近4個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume4, 11));

   //--X11 近5個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume5, 11));

   //--X11 近6個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume6, 11));

   //--X11 近7個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume7, 11));

   //--X11 近8個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume8, 11));

   //--X11 近9個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume9, 11));

   //--X11 近10個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume10, 11));

   //--X11 近11個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume11, 11));

   //--X11 近12個月信用卡消費金額
   ee.append(commString.bbFixlen(trunConsume12, 11));

   //--換行符號0D0A
   ee.append(newLineA);

   writeTextFile(iiFileNum2, ee.toString());
}

void covertConsumeAmt() {
   double tmp[] = new double[12];
   String convertTmp[] = new String[12];
   DecimalFormat df = new DecimalFormat("0");
   tmp[0] = consumeAmt1;
   tmp[1] = consumeAmt2;
   tmp[2] = consumeAmt3;
   tmp[3] = consumeAmt4;
   tmp[4] = consumeAmt5;
   tmp[5] = consumeAmt6;
   tmp[6] = consumeAmt7;
   tmp[7] = consumeAmt8;
   tmp[8] = consumeAmt9;
   tmp[9] = consumeAmt10;
   tmp[10] = consumeAmt11;
   tmp[11] = consumeAmt12;
   double temp = 0;
   String tmpStr = "", tmpSign = "", lastStr = "";
   for (int i = 0; i < 12; i++) {
      temp = tmp[i];
      if (temp < 0)
         temp = 0;
      tmpStr = df.format(temp);
      if (temp < 10) {
         lastStr = tmpStr;
      } else {
         lastStr = tmpStr.substring(tmpStr.length()-1, tmpStr.length());
      }

      if (temp >= 0) {
         switch (lastStr) {
            case "0":
               tmpSign = "{";
               break;
            case "1":
               tmpSign = "A";
               break;
            case "2":
               tmpSign = "B";
               break;
            case "3":
               tmpSign = "C";
               break;
            case "4":
               tmpSign = "D";
               break;
            case "5":
               tmpSign = "E";
               break;
            case "6":
               tmpSign = "F";
               break;
            case "7":
               tmpSign = "G";
               break;
            case "8":
               tmpSign = "H";
               break;
            case "9":
               tmpSign = "I";
               break;
         }
      }
      convertTmp[i] = commString.lpad(tmpStr.substring(0, tmpStr.length()-1)+tmpSign, 11, "0");
   }

   trunConsume1 = convertTmp[0];
   trunConsume2 = convertTmp[1];
   trunConsume3 = convertTmp[2];
   trunConsume4 = convertTmp[3];
   trunConsume5 = convertTmp[4];
   trunConsume6 = convertTmp[5];
   trunConsume7 = convertTmp[6];
   trunConsume8 = convertTmp[7];
   trunConsume9 = convertTmp[8];
   trunConsume10 = convertTmp[9];
   trunConsume11 = convertTmp[10];
   trunConsume12 = convertTmp[11];
}

void initData() {
   branch = "";
   idNo = "";
   haveCard = "";
   bCode_flag = "";
   //------
   cycleRatio1 = 0;
   cycleRatio2 = 0;
   cycleRatio3 = 0;
   cycleRatio4 = 0;
   cycleRatio5 = 0;
   cycleRatio6 = 0;
   cycleRatio7 = 0;
   cycleRatio8 = 0;
   cycleRatio9 = 0;
   cycleRatio10 = 0;
   cycleRatio11 = 0;
   cycleRatio12 = 0;
   lineOfCreditAmt1 = 0;
   lineOfCreditAmt2 = 0;
   lineOfCreditAmt3 = 0;
   lineOfCreditAmt4 = 0;
   lineOfCreditAmt5 = 0;
   lineOfCreditAmt6 = 0;
   lineOfCreditAmt7 = 0;
   lineOfCreditAmt8 = 0;
   lineOfCreditAmt9 = 0;
   lineOfCreditAmt10 = 0;
   lineOfCreditAmt11 = 0;
   lineOfCreditAmt12 = 0;
   lineOfCreditAmtCash1 = 0;
   lineOfCreditAmtCash2 = 0;
   lineOfCreditAmtCash3 = 0;
   lineOfCreditAmtCash4 = 0;
   lineOfCreditAmtCash5 = 0;
   lineOfCreditAmtCash6 = 0;
   lineOfCreditAmtCash7 = 0;
   lineOfCreditAmtCash8 = 0;
   lineOfCreditAmtCash9 = 0;
   lineOfCreditAmtCash10 = 0;
   lineOfCreditAmtCash11 = 0;
   lineOfCreditAmtCash12 = 0;
   useCreditAmt1 = 0;
   useCreditAmt2 = 0;
   useCreditAmt3 = 0;
   useCreditAmt4 = 0;
   useCreditAmt5 = 0;
   useCreditAmt6 = 0;
   useCreditAmt7 = 0;
   useCreditAmt8 = 0;
   useCreditAmt9 = 0;
   useCreditAmt10 = 0;
   useCreditAmt11 = 0;
   useCreditAmt12 = 0;
   amtBalance1 = 0;
   amtBalance2 = 0;
   amtBalance3 = 0;
   amtBalance4 = 0;
   amtBalance5 = 0;
   amtBalance6 = 0;
   amtBalance7 = 0;
   amtBalance8 = 0;
   amtBalance9 = 0;
   amtBalance10 = 0;
   amtBalance11 = 0;
   amtBalance12 = 0;
   cashBalance1 = 0;
   cashBalance2 = 0;
   cashBalance3 = 0;
   cashBalance4 = 0;
   cashBalance5 = 0;
   cashBalance6 = 0;
   cashBalance7 = 0;
   cashBalance8 = 0;
   cashBalance9 = 0;
   cashBalance10 = 0;
   cashBalance11 = 0;
   cashBalance12 = 0;
   mCode1 = "";
   mCode2 = "";
   mCode3 = "";
   mCode4 = "";
   mCode5 = "";
   mCode6 = "";
   mCode7 = "";
   mCode8 = "";
   mCode9 = "";
   mCode10 = "";
   mCode11 = "";
   mCode12 = "";
   consumeAmt1 = 0;
   consumeAmt2 = 0;
   consumeAmt3 = 0;
   consumeAmt4 = 0;
   consumeAmt5 = 0;
   consumeAmt6 = 0;
   consumeAmt7 = 0;
   consumeAmt8 = 0;
   consumeAmt9 = 0;
   consumeAmt10 = 0;
   consumeAmt11 = 0;
   consumeAmt12 = 0;
   trunConsume1 = "";
   trunConsume2 = "";
   trunConsume3 = "";
   trunConsume4 = "";
   trunConsume5 = "";
   trunConsume6 = "";
   trunConsume7 = "";
   trunConsume8 = "";
   trunConsume9 = "";
   trunConsume10 = "";
   trunConsume11 = "";
   trunConsume12 = "";
}

}
