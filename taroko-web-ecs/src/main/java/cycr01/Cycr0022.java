/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-12-07  V1.00.00  Jack,Liao  帳單補印                                   *
 * 111/10/28  V1.00.01  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/

package cycr01;

import ofcapp.BaseAction;
import taroko.com.TarokoPDF;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

public class Cycr0022 extends BaseAction {

  String pdfFunction = "";
  String mProgName = "cycr0022";

  @Override
  public void userAction() throws Exception {
    rc = 1;
    strAction = wp.buttonCode;
    switch (wp.buttonCode)
    {
      case "Q"    : queryFunc();    /* 查詢功能 */
        break;
      case "L"    : strAction = ""; /* 清畫面   */
        clearFunc();
        break;
      case "PDF"  : pdfPrint();     /* 產生 PDF */
        break;
      default     : break;
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.setQueryMode();
    queryRead();
    dddwSelect();
  }

  @Override
  public void queryRead() throws Exception {

    String acctMonthS = wp.colStr("ex_acct_month_s");
    String acctMonthE = wp.colStr("ex_acct_month_e");

    if ( !processTitleData() )
    { return; }

    wp.pageControl();
    if ( acctMonthE.length() == 0 )
    { acctMonthE = convMonths(acctMonthS,13); }

    int k=0;
    for( int i=0; i<13; i++ ) {
      String printMonth = convMonths(acctMonthS,i);
      String acctMonth  = convMonths(printMonth,-1);
      String cycleDate  = printMonth+wp.getValue("stmt_cycle");
      if ( (i+1) < 10 )
      { wp.setValue("SER_NUM","0"+(i+1),i); }
      else
      { wp.setValue("SER_NUM",""+(i+1),i);  }
      wp.setValue("print_month",printMonth,i);
      wp.setValue("acct_month",acctMonth,i);
      wp.setValue("cycle_date",cycleDate,i);
      wp.setValue("sys_date",wp.sysDate,i);
      selectActAcctHst(i);
      if ( wp.selectCnt == 0 )
      { break; }
      k++;
      deleteCtiAcmm(i);
      insertCtiAcmm(i);
      if ( printMonth.compareTo(acctMonthE) >= 0 )
      { break; }
    }

    wp.notFound ="";
    if ( k == 0  ) {
      alertErr("查無資料 !"); return;
    }

    wp.selectCnt = k;
    wp.setListCount(1);
    return;

  }

  void pdfPrint() throws Exception {

    selectPtrAcctType();
    processTitleData();
    if ( callBatch() == false )
    { return; }
    processPDFdata();
    String mProgName  = "cycr0022";
    wp.reportId = mProgName;
    TarokoPDF pdf = new TarokoPDF();
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.pageCount=9999;
    pdf.sheetNo = 0;
    pdf.pageVert= true;       //直印
    pdf.procesPDFreport(wp);
    pdf = null;

    return;
  }

  public boolean callBatch() throws Exception {

    wp.showLogMessage("I","","callBatch started ");

    String printMonth = convMonths(wp.getValue("acct_month"),1);

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    int  rc = batch.callBatch("CycA650 " + wp.getValue("ex_acct_type") + " " + wp.getValue("ex_acct_key")+ " " + printMonth);
    if ( rc != 1 ) {
      wp.errCode = "Y";
      wp.errMesg = "執行補印 CALL BATCH 失敗-1";
      return false;
    }
    int k=0;
    Thread.sleep(2000);
    for( int i=0; i<20; i++) {
      Thread.sleep(2000);
      Object param1[] = {"",""};
      wp.selectSQL = "p_seqno ";
      wp.daoTable  = "cti_acmm_curr";
      wp.whereStr  = "WHERE p_seqno = ? and acct_month = ?";
      param1[0]    = wp.getValue("p_seqno");
      param1[1]    = wp.getValue("acct_month");
      pageSelect(param1);
      if ( wp.selectCnt != 0 )
      { break; }
      k++;
    }

    if ( k == 20 ) {
      wp.errCode = "Y";
      wp.errMesg = "執行補印 CALL BATCH 失敗-2";
      return false;
    }

    wp.showLogMessage("I","","callBatch ended ");
    return true;
  }

  public void deleteCtiAcmm(int k) throws Exception {

    String isSql = "delete cti_acmm WHERE p_seqno = :p_seqno and acct_month = :acct_month and from_mark = '08' ";
    setString("p_seqno",wp.getValue("p_seqno"));
    setString("acct_month",wp.getValue("acct_month",k));
    sqlExec(isSql);

    return;
  }

  public void insertCtiAcmm(int k) throws Exception {

    String isSql="";
    isSql = "insert into cti_acmm ( "
            + "p_seqno,"
            + "acct_month,"
            + "print_month,"
            + "cti_seq,"
            + "acct_type,"
            + "create_date,"
            + "stmt_cycle,"
            + "cycle_date,"
            + "from_mark,"
            + "proc_flag,"
            + "mod_user,"
            + "mod_time,"
            + "mod_pgm,"
            + "print_date "
            + " ) values ( "
            + ":p_seqno,"
            + ":acct_month,"
            + ":print_month,"
            + ":cti_seq,"
            + ":acct_type,"
            + ":create_date,"
            + ":stmt_cycle,"
            + ":cycle_date,"
            + ":from_mark,"
            + ":proc_flag,"
            + ":mod_user,"
            + "sysdate,"
            + ":mod_pgm,"
            + ":print_date "
            + " ) ";

    String  uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase().substring(0,4);
    String  printMonth = convMonths(wp.getValue("acct_month",k),1);

    setString("p_seqno",wp.getValue("p_seqno"));
    setString("acct_month",wp.getValue("acct_month",k));
    setString("print_month",printMonth);
    setString("cti_seq",uuid);
    setString("acct_type",wp.getValue("acct_type"));
    setString("create_date",wp.sysDate);
    setString("stmt_cycle",wp.getValue("stmt_cycle"));
    setString("cycle_date",printMonth+wp.getValue("stmt_cycle"));
    setString("credit_limit",wp.getValue("line_of_credit_amt"));
    setString("from_mark","08");
    setString("proc_flag","1");
    setString("mod_user",wp.loginUser);
    setString("mod_pgm","Cycr0022");
    setString("print_date",wp.sysDate);
    sqlExec(isSql);
    return;
  }

  public boolean processTitleData() throws Exception {

    Object param1[] = {"",""};
    Object param2[] = {""};

    String cardNo   = wp.colStr("ex_card_no");
    String acctType = wp.colStr("ex_acct_type");
    String acctKey  = wp.colStr("ex_acct_key");

    if ( cardNo.length() == 0 && acctKey.length() == 0 ) {
      alertErr("請輸入帳號或卡號 "); return false;
    }

    if ( cardNo.length() > 0 ) {
      wp.selectSQL = "p_seqno";
      wp.daoTable  = "crd_card";
      wp.whereStr  = "WHERE card_no = ? ";
      param2[0]    = cardNo;
      pageSelect(param2);

      wp.selectSQL = "acct_type,acct_key,p_seqno,stmt_cycle,line_of_credit_amt_cash,line_of_credit_amt,corp_p_seqno";
      wp.daoTable  = "act_acno";
      wp.whereStr  = "WHERE p_seqno = ? ";
      param2[0]    = wp.getValue("p_seqno");
      pageSelect(param2);
      wp.setValue("ex_acct_type",wp.getValue("acct_type"));
      wp.setValue("ex_acct_key",wp.getValue("acct_key"));
      acctKey = wp.getValue("acct_key");
    } else {
      wp.selectSQL = "p_seqno,acct_type,stmt_cycle,line_of_credit_amt_cash,corp_p_seqno,line_of_credit_amt";
      wp.daoTable  = "act_acno";
      wp.whereStr  = "WHERE acct_type = ? and acct_key = ? ";
      param1[0]    = acctType;
      param1[1]    = acctKey;
      pageSelect(param1);
    }

    wp.selectSQL = "OVERSEA_CASH_PCT";
    wp.daoTable  = "CCA_AUTH_PARM";
    wp.whereStr  = "WHERE area_type='T' AND ( card_note = '*' "
            + "OR card_note IN ( SELECT DISTINCT card_note FROM crd_card "
            + "WHERE current_code='0' AND p_seqno = ? ) ) "
            + "ORDER BY decode(card_note,'*','zz',card_note) "
            + "FETCH FIRST 1 ROW ONLY ";
    param2[0]    = wp.getValue("p_seqno");
    pageSelect(param2);

    double calCashLimit = wp.getNumber("LINE_OF_CREDIT_AMT_CASH",0);
    double calOverLimit = wp.getNumber("LINE_OF_CREDIT_AMT",0) * (wp.getNumber("OVERSEA_CASH_PCT",0) / 100);
    double maxCashLimit = wp.getNumber("cashadv_loc_maxamt",0);

    if ( maxCashLimit == 0  )
    { calCashLimit =0; calOverLimit=0; }
    else {
      if ( calCashLimit > maxCashLimit )
      { calCashLimit = maxCashLimit; }
      if ( calOverLimit > maxCashLimit )
      { calOverLimit = maxCashLimit; }
    }

    String localLmt   = String.format("%,14.0f", calCashLimit);
    String foreignLmt = String.format("%,14.0f", calOverLimit);
    wp.setValue("CRD_LMT2",localLmt.trim()+"/"+foreignLmt.trim());

    if ( acctType.equals("02") || acctType.equals("03") ) {
      wp.selectSQL  = "chi_name as send_name ";
      wp.daoTable   = "crd_corp";
      wp.whereStr   = "WHERE corp_p_seqno = ? ";
      param2[0] = wp.getValue("corp_p_seqno");
      pageSelect(param2);
    } else {
      wp.selectSQL = "chi_name||'     '||decode(sex,'1','先生','小姐') as send_name,"
              + "office_area_code1,"
              + "office_tel_no1,"
              + "office_area_code2,"
              + "office_tel_no2,"
              + "home_area_code1,"
              + "home_tel_no1,"
              + "home_area_code2,"
              + "home_tel_no2,"
              + "cellar_phone,"
              + "e_mail_addr ";
      wp.daoTable = "crd_idno";
      wp.whereStr = "WHERE id_no = ? and id_no_code = '0' ";
      if ( acctKey.length() >= 10 )
      { acctKey = acctKey.substring(0,10); }
      param2[0]     = acctKey.substring(0,10);
      pageSelect(param2);
    }

    return true;
  }

  public void processPDFdata() throws Exception {

    selectActAcctHst(0);
    selectCtiAcmmCurr();
    selectCtiAbem();
    selectCrdEmployee();
    selectCrdEmployeeA();
    processIntrRate();
    selectMktBonusHst();
    selectMktFundHst();
    selectPtrBillmsg();
    selectCycBillExt(); // 使用利息及費用資訊
    processContactInfo();

    return;
  }

  public int selectPtrAcctType() throws Exception  {

    Object   param1[] = {""};
    wp.selectSQL = "chin_name||'對帳單' as acct_type_desc,cashadv_loc_maxamt ";
    wp.daoTable  = "ptr_acct_type";
    wp.whereStr  = "WHERE acct_type = ? ";
    param1[0]    = wp.getValue("ex_acct_type");
    pageSelect(param1);

    return wp.selectCnt;
  }

  public void selectCtiAcmmCurr() throws Exception  {

    Object  param2[] = {"",""};
    wp.daoTable  = "cti_acmm_curr a,ptr_currcode b";
    wp.selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.bill_curr_code,"
            + "a.bill_sort_seq,"
            + "a.autopay_acct_bank,"
            + "a.autopay_acct_no as auto_acct_no,"
            + "a.autopay_dc_flag,"
            + "a.dc_last_ttl_amt as last_ttl_amt,"
            + "a.dc_payment_amt  as pay_amt,"
            + "a.dc_adjust_amt   as adj_amt,"
            + "a.dc_new_amt      as new_amt,"
            + "a.dc_this_ttl_amt as ttl_amt,"
            + "a.dc_this_minimum_pay as min_pay,"
            + "a.dc_auto_payment_amt as auto_pay_amt,"
            + "decode(a.curr_code,'901','台幣',b.curr_chi_name) as curr_name ";
    wp.whereStr  = "WHERE a.p_seqno = ? and a.acct_month = ? AND a.curr_code = b.curr_code "
            + "order by a.curr_code desc ";
    param2[0] = wp.getValue("p_seqno");
    param2[1] = wp.getValue("acct_month");
    pageQuery(param2);
    if ( wp.getValue("auto_acct_no").length() == 0 )
    { wp.setValue("AUTO_INFO","您尚未辦妥自動轉款，請依其它繳款方式繳款"); }

    String printMonth = convMonths(wp.getValue("acct_month"),1);
    wp.setValue("cycle_date",printMonth+wp.getValue("stmt_cycle"));

    wp.setValue("DISP-H1",""+wp.selectCnt);
    wp.setValue("DISP-H2",""+wp.selectCnt);

    int cnt = wp.selectCnt;
    for(int i=0; i<cnt; i++) {
      wp.setValue("##"+wp.getValue("curr_code",i),"Y");
      wp.setValue("auto_pay_date",wp.getValue("auto_pay_date"),i);
      wp.setValue("ll_date",wp.getValue("ll_date"),i);
      if ( wp.getValue("auto_acct_no",i).length() > 8 ) {
        if ( wp.getValue("auto_pay_date").length() == 0  )
        { wp.setValue("auto_pay_date",wp.getValue("ll_date"),i); }
        String encAcct = wp.getValue("auto_acct_no",i).substring(0,4) + "XXXX" + wp.getValue("auto_acct_no",i).substring(8);
        wp.setValue("auto_acct_no",encAcct,i);
      }
      if ( wp.getValue("auto_acct_no",i).length() == 0 ) {
        wp.setValue("BILL_CURR_CODE","",i);
        wp.setValue("AUTO_PAY_AMT","",i);
      }
      formatSignAmount(i);
      wp.setValue("#-"+wp.getValue("curr_code",i),wp.getValue("bill_curr_code",i));
      selectActAchBank(i);
    }

    return;
  }

  public void formatSignAmount(int k) throws Exception  {

    if ( wp.getNumber("pay_amt",k) < 0 )
    { wp.setValue("pay_amt","-"+String.format("%,12.2f", (wp.getNumber("pay_amt",k) * -1)),k); }
    else
    { wp.setValue("pay_amt","+"+String.format("%,12.2f", wp.getNumber("pay_amt",k)),k); }

    if ( wp.getNumber("adj_amt",k) < 0 )
    { wp.setValue("adj_amt","-"+String.format("%,10.2f", (wp.getNumber("adj_amt",k) * -1)),k); }
    else
    { wp.setValue("adj_amt","+"+String.format("%,10.2f", wp.getNumber("adj_amt",k)),k); }

    if ( wp.getNumber("new_amt",k) < 0 )
    { wp.setValue("new_amt","-"+String.format("%,12.2f", (wp.getNumber("new_amt",k) * -1)),k); }
    else
    { wp.setValue("new_amt","+"+String.format("%,12.2f", wp.getNumber("new_amt",k)),k); }

    wp.setValue("ttl_amt","="+String.format("%,12.2f", wp.getNumber("ttl_amt",k)),k);
    return;
  }

  public void selectActAchBank(int k) throws Exception  {
    if ( wp.getValue("autopay_acct_bank",k).length() < 3 )
    { return; }
    Object  param2[] = {""};
    wp.daoTable  = "act_ach_bank";
    wp.selectSQL = "bank_name ";
    wp.whereStr  = "WHERE bank_no like ? ";
    param2[0] = wp.getValue("autopay_acct_bank",k)+"%";
    pageSelect(param2);
    if ( wp.selectCnt > 0 ) {
      wp.setValue("auto_acct_no",wp.getValue("bank_name")+" "+wp.getValue("auto_acct_no",k),k);
    }

    if ( wp.selectCnt > 0 && k > 0) {
      wp.setValue("dummy_desc",wp.getValue("curr_name",k)+"帳單",k-1);
    }
    return;
  }

  public void selectCtiAbem() throws Exception  {

    Object  param2[] = {"",""};
    wp.pageRows =999;
    wp.selectSQL = "PRINT_TYPE,"
            + "ACCT_CODE,"
            + "CURR_CODE     as ABEM_CURR,"
            + "PURCHASE_DATE as PU_DATE,"
            + "INTEREST_DATE as IT_DATE,"
            + "description   as ITEM_DESC,"
            + "AREA_CODE,"
            + "EXCHANGE_DATE,"
            + "CURRENCY_CODE,"
            + "SOURCE_AMT  as SRC_AMT,"
            + "DC_DEST_AMT as DEST_AMT,"
            + "DUMMY_CODE,"
            + "CAP_TYPE ";
    wp.daoTable  = "cti_abem";
    wp.whereStr  = "WHERE p_seqno = ? and acct_month = ? "
            + "order by curr_code desc,print_type,print_seq";
    //+ "ORDER  BY curr_code desc,print_type,decode(print_type,'06','x',card_no),"
    //+ "decode(dummy_code,'S','S','Y','Y','T') desc,purchase_date,print_seq";
    param2[0] = wp.getValue("p_seqno");
    param2[1] = wp.getValue("acct_month");
    pageQuery(param2);

    int pnt=0;
    int k=0;
    for(int i=0; i<wp.selectCnt; i++ ) {

      if ( wp.getValue("PRINT_TYPE",i).equals("09") )
      { setDummyLine(i,pnt); pnt++;  continue; }

      String puDate   = wp.getValue("PU_DATE",i);
      if ( puDate.length() == 8 )
      { puDate = toDispDate(puDate); }
      wp.setValue("PU_DATE",puDate,i);

      String itDate   = wp.getValue("IT_DATE",i);
      String ecMark   = wp.getValue("CAP_TYPE",i);
      if ( itDate.length() == 8 )
      { itDate = toDispDate(itDate); }
      wp.setValue("IT_DATE",itDate+ecMark,i);

      String area   = wp.getValue("AREA_CODE",i);
      if ( area.length() > 8)
      { area = area.substring(0,8); }
      String exDate = wp.getValue("EXCHANGE_DATE",i);
      if ( exDate.length() == 8 )
      { exDate   = toDispDate(exDate); }
      if ( wp.getValue("ACCT_CODE",i).equals("PF") )
      { area =""; exDate   = ""; }
      String currData = wp.getValue("CURRENCY_CODE",i);

      wp.setValue("E1",area,i);
      wp.setValue("E2",exDate,i);
      wp.setValue("E3",currData,i);

      double srcAmt  = wp.getNumber("SRC_AMT",i);
      double destAmt = wp.getNumber("DEST_AMT",i);
      String srcData  = String.format("%,14.2f", srcAmt);
      String destData = String.format("%,14.2f", destAmt);
      if ( wp.getValue("DUMMY_CODE",i).equals("Y") )
      { srcData= ""; destData=""; }
      if ( wp.getValue("DUMMY_CODE",i).equals("S") )
      { srcData= ""; }
      if ( Arrays.asList("01","02","03","07").contains(wp.getValue("PRINT_TYPE",i)) )
      { srcData= ""; }

      if ( Arrays.asList("08").contains(wp.getValue("PRINT_TYPE",i)) &&  srcAmt == 0 )
      { srcData= ""; }

      if ( Arrays.asList("08").contains(wp.getValue("PRINT_TYPE",i)) &&  destAmt == 0 )
      { destData= ""; }

      wp.setValue("SRC_AMT",srcData,i);
      wp.setValue("DEST_AMT",destData,i);

      if ( Arrays.asList("02","03").contains(wp.getValue("PRINT_TYPE",i)) && wp.getValue("DUMMY_CODE",i).equals("Y") )
      { wp.setValue("DEST_AMT","",i); }
    }

    wp.setListCount(1);

    return;
  }

  public void  setDummyLine(int i,int n) throws Exception {
    wp.setValue("PU_DATE","==============",i);
    wp.setValue("IT_DATE","===============",i);
    wp.setValue("ITEM_DESC","======================= "+wp.getValue("dummy_desc",n)+" ===============",i);
    wp.setValue("E1","========",i);
    wp.setValue("E2","==========",i);
    wp.setValue("E3","=====",i);
    wp.setValue("SRC_AMT","===================",i);
    wp.setValue("DEST_AMT","==============",i);
    return;
  }

  public void selectCrdEmployee() throws Exception
  {
    Object  param2[] = {""};
    wp.daoTable  = "crd_employee";
    wp.selectSQL = "id ";
    wp.whereStr = "WHERE  id  = ? and status_id = '1' ";
    param2[0] = wp.getValue("ex_acct_key").substring(0,10);
    pageQuery(param2);

    wp.setValue("BANK_MEM","N");
    if ( wp.selectCnt > 0 )
    { wp.setValue("BANK_MEM","Y"); }
    return;
  }

  public void selectCrdEmployeeA() throws Exception
  {
    Object  param2[] = {""};
    wp.daoTable  = "crd_employee_a";
    wp.selectSQL = "id ";
    wp.whereStr = "WHERE  id = ? and status_id = '1' ";
    param2[0] = wp.getValue("ex_acct_key").substring(0,10);
    pageQuery(param2);

    if ( wp.selectCnt > 0 )
    { wp.setValue("BANK_MEM","Y"); }
    return;
  }

  public void processIntrRate() throws Exception {

    double  revolveIntRate=0,revolveIntRate2=0;
    String  revRateSDate="",revRateEDate="",revRateSDate2="",revRateEDate2="",special="";
    String  stmtCycle = wp.getValue("stmt_cycle");

    wp.setValue("DISP-RY","0");
    if ( wp.getValue("BANK_MEM").equals("Y") ) {
      wp.setValue("DISP-Y"  , "0");
      return;
    }

    wp.setValue("DISP-R1","1");
    wp.setValue("DISP-R2","1");

    selectPtrActgeneral();
    selectCycDiffRateLog();

    double adjIntRate1 = wp.getNumber("revolve_int_rate",0);
    if ( adjIntRate1 != 0 && wp.getValue("revolve_rate_s_month").length() == 0 && wp.getValue("revolve_rate_e_month").length() == 0 )
    { adjIntRate1  = 0; special = "L"; }

    if ( wp.getValue("revolve_int_sign").equals("-") || wp.getValue("revolve_int_sign").length() == 0 )
    { adjIntRate1 = adjIntRate1 * -1;  }
    revolveIntRate   = (wp.getNumber("revolving_interest1",0) + adjIntRate1) * 365 / 100;

    if ( wp.getValue("revolve_rate_s_month").length() == 6 ) {
      String cvt_date  = convDates(wp.getValue("revolve_rate_s_month")+ stmtCycle,1);
      revRateSDate  = convMonths(cvt_date,-1)+cvt_date.substring(6);
    } else {
      revRateSDate   = convDates(wp.getValue("run_e_month1")+ stmtCycle,1);
      revRateSDate   = convMonths(revRateSDate,-3)+revRateSDate.substring(6);
    }

    String checkDate1 = "";
    if ( wp.getValue("revolve_rate_e_month").length() == 6 ) {
      revRateEDate   = wp.getValue("revolve_rate_e_month")+ stmtCycle;
      checkDate1 = revRateEDate;
    }
    else {
      revRateEDate   = wp.getValue("run_e_month1") + stmtCycle;
    }

    if ( adjIntRate1  == 0 && checkDate1.length() == 8 && checkDate1.compareTo(wp.getValue("busi.business_date")) <= 0 ) {
      revRateEDate =  wp.getValue("run_e_month1") + stmtCycle;
      special = "L";
    }

    double adjIntRate2 = wp.getNumber("revolve_int_rate_2",0);
    if ( wp.getValue("revolve_int_sign_2").equals("-") || wp.getValue("revolve_int_sign_2").length() == 0 )
    { adjIntRate2  = adjIntRate2 * -1;  }
    revolveIntRate2  = (wp.getNumber("revolving_interest1",0) + adjIntRate2) * 365 / 100;

    if ( wp.getValue("revolve_rate_s_month_2").length() == 6 ) {
      revRateSDate2 = convDates(wp.getValue("revolve_rate_s_month_2")+ stmtCycle,1);
      revRateSDate2 = convMonths(revRateSDate2,-1)+revRateSDate2.substring(6);
    } else {
      revRateSDate2 = convDates(wp.getValue("run_e_month1")+ stmtCycle,1);
    }

    if ( wp.getValue("revolve_rate_e_month_2").length() == 6 )
    { revRateEDate2 = wp.getValue("revolve_rate_e_month_2")+ stmtCycle; }
    else
    { revRateEDate2 = wp.getValue("run_e_month2") + stmtCycle; }

    wp.setValue("R1",String.format("%.2f",revolveIntRate));
    if ( wp.getValue("R1").equals("-0.00") )
    { wp.setValue("R1","0.00"); }

    String rate_1   = String.format("%.2f",revolveIntRate);
    String rate_2   = String.format("%.2f",revolveIntRate2);

    String rateInfo1 = rate_1 +"%適用 "+toDispDate(revRateSDate)  +" 至 "+toDispDate(revRateEDate);
    String rateInfo2 = rate_2 +"%適用 "+toDispDate(revRateSDate2)+" 至 "+toDispDate(revRateEDate2);

    String month = convMonths(wp.getValue("acct_month"),1).substring(4,6);
    if ( Arrays.asList("03","06","09","12").contains(month) )  // 這幾個月不顯示第二期利率
    { wp.setValue("DISP-R2"  , "0");  }

    wp.setValue("DISP-YS","1");
    if ( Arrays.asList("03").contains(wp.getValue("acct_type")) ) { // 採購卡不顯示利率
      special ="M";
      wp.setValue("DISP-R1","0");
      wp.setValue("DISP-R2","0");
      wp.setValue("DISP-YS","0");
    }
    else
    if ( Arrays.asList("02").contains(wp.getValue("acct_type")) ) { // 商務卡只顯示第一期利率其餘不顯示
      special ="M";
      wp.setValue("DISP-R2","0");
      rateInfo1 = rate_1+"%";
      wp.setValue("DISP-YS","0");
    }
    else
    if ( adjIntRate1 != 0 && adjIntRate2 == 0 && checkDate1.length() == 8 && checkDate1.compareTo(revRateSDate2) > 0 )
    { special ="M"; wp.setValue("DISP-R2","0");  } // 不顯示第二期利率
    else
    if ( adjIntRate1 != 0 && adjIntRate2 == 0 && wp.getValue("revolve_rate_e_month").length() == 0 ) {
      special ="M";
      rateInfo1 = rate_1 +"%適用 "+toDispDate(revRateSDate);
      wp.setValue("DISP-R2","0");
    }

    if ( revolveIntRate2 > revolveIntRate && !special.equals("M") )
    { wp.setValue("DISP-RY","1"); }  // 顯示利率調整訊息
    else
    if ( ((adjIntRate1 == 0 && wp.getValue("revolve_rate_s_month").length() == 0) || special.equals("L")) && !special.equals("M") )
    {
      rateInfo1 = rate_1 +"%適用至 "+toDispDate(revRateEDate);
      rateInfo1 = rateInfo1 + "（餘額代償以各專案申請書約定為準）";
      rateInfo2 = rateInfo2 + "（餘額代償以各專案申請書約定為準）";
    }  // 顯示餘額代償訊息

    rateInfo1 = "您的信用卡循環信用利率為"+rateInfo1 + "。";
    rateInfo2 = "　　　　　　　　　　　  "+rateInfo2 + "。";
    wp.setValue("RATE_PERIOD_1",rateInfo1);
    wp.setValue("RATE_PERIOD_2",rateInfo2);

    wp.setValue("DISP-YN","1");
    return;
  }

  public void processContactInfo() throws Exception {

    String  eMail="",encEmail="",officeTel="",homeTel="",mobileTel = "",cvtTel="";

    if ( wp.getValue("office_tel_no1").length() <= 5 ) {
      wp.setValue("office_area_code1",wp.getValue("office_area_code2"),0);
      wp.setValue("office_tel_no1",wp.getValue("office_tel_no2"),0);
    }

    if ( wp.getValue("office_area_code1").length() > 0 )
    { wp.setValue("office_area_code1",wp.getValue("office_area_code1")+"-",0); }

    if ( wp.getValue("office_tel_no1").length() == 7 ) {
      cvtTel = wp.getValue("office_tel_no1");
      officeTel = wp.getValue("office_area_code1")
              + cvtTel.substring(0,2)+"XX"+cvtTel.substring(4,5)+"X"+cvtTel.substring(6);
    } else if ( wp.getValue("office_tel_no1").length() == 6 ) {
      cvtTel    = wp.getValue("office_tel_no1");
      officeTel = wp.getValue("office_area_code1")
              + cvtTel.substring(0,1)+"XX"+cvtTel.substring(3,4)+"X"+cvtTel.substring(5);
    } else {
      officeTel = wp.getValue("office_area_code1");
      if ( wp.getValue("office_tel_no1").length() >= 3 )
      { officeTel += (wp.getValue("office_tel_no1").substring(0,3)+"XX"); }
      if ( wp.getValue("office_tel_no1").length() >= 6 )
      { officeTel += (wp.getValue("office_tel_no1").substring(5,6)+ "X"); }
      if ( wp.getValue("office_tel_no1").length() > 7 )
      { officeTel += wp.getValue("office_tel_no1").substring(7); }
    }

    if ( wp.getValue("home_tel_no1").length() <= 5 ) {
      wp.setValue("home_area_code1",wp.getValue("home_area_code2"),0);
      wp.setValue("home_tel_no1",wp.getValue("home_tel_no2"),0);
    }

    if ( wp.getValue("home_area_code1").length() > 0 )
    { wp.setValue("home_area_code1",wp.getValue("home_area_code1")+"-",0); }

    if ( wp.getValue("home_tel_no1").length() == 7 ) {
      cvtTel  = wp.getValue("home_tel_no1");
      homeTel = wp.getValue("home_area_code1")
              + cvtTel.substring(0,2)+"XX"+cvtTel.substring(4,5)+"X"+cvtTel.substring(6);
    } else if ( wp.getValue("home_tel_no1").length() == 6 ) {
      cvtTel  = wp.getValue("home_tel_no1");
      homeTel = wp.getValue("home_area_code1")
              + cvtTel.substring(0,1)+"XX"+cvtTel.substring(3,4)+"X"+cvtTel.substring(5);
    } else {
      homeTel = wp.getValue("home_area_code1");
      if ( wp.getValue("home_tel_no1").length() >= 3 )
      { homeTel += (wp.getValue("home_tel_no1").substring(0,3)+"XX"); }
      if ( wp.getValue("home_tel_no1").length() >= 6 )
      { homeTel += (wp.getValue("home_tel_no1").substring(5,6)+ "X"); }
      if ( wp.getValue("home_tel_no1").length() > 7 )
      { homeTel += wp.getValue("home_tel_no1").substring(7); }
    }

    if ( wp.getValue("cellar_phone").length() == 10 ) {
      cvtTel     = wp.getValue("cellar_phone");
      mobileTel = cvtTel.substring(0,5)+"XX"+cvtTel.substring(7,8)+"X"+cvtTel.substring(9,10);
    } else {
      if ( wp.getValue("cellar_phone").length() >= 5 )
      { mobileTel += (wp.getValue("cellar_phone").substring(0,5)+"XX"); }
      if ( wp.getValue("cellar_phone").length() >= 8 )
      { mobileTel += (wp.getValue("cellar_phone").substring(7,8)+"X");  }
      if ( wp.getValue("cellar_phone").length() > 9 )
      { mobileTel += wp.getValue("cellar_phone").substring(9); }
    }

    if ( wp.getValue("e_mail_addr").length() > 0 )
    { eMail = wp.getValue("e_mail_addr");   }

    encEmail= eMail;
    int pnt = eMail.indexOf("@");
    if ( pnt > 2 )
    { encEmail = eMail.substring(0,(pnt-2))+"XX"+eMail.substring(pnt); }

    if ( encEmail.length() > 5 )
    { encEmail = "電子信箱 : "+encEmail; }
    else
    { encEmail = "無電子信箱資料 "; }

    wp.setValue("CONT_INFO","貴戶保留於本行之公司電話為 : "+officeTel+"  住家電話為 : "+homeTel+ " 手機電話  : "+mobileTel,0);
    wp.setValue("CONT_INFO",encEmail + " 若資料與上述不符請來電本行信用卡服務專線 （０２）８９８２００００　辦理變更",1);
    wp.setValue("DISP-D5" , "1");
    wp.setValue("DISP-C"  , "2");
    return;
  }

  public void selectActAcctHst(int k) throws Exception {

    Object  param2[] = {"",""};
    wp.daoTable  = "act_acct_hst";
    wp.selectSQL = "acct_type,"
            + "stmt_cycle,"
            + "revolve_int_sign,"
            + "revolve_int_rate,"
            + "revolve_rate_s_month,"
            + "revolve_rate_e_month,"
            + "combo_indicator ,"
            + "combo_acct_no,"
            + "revolve_int_sign_2,"
            + "revolve_int_rate_2,"
            + "revolve_rate_s_month_2,"
            + "revolve_rate_e_month_2,"
            + "STMT_CREDIT_LIMIT CRD_LMT,"
            + "stmt_auto_pay_date as auto_pay_date,"
            + "stmt_last_payday as ll_date ";
    wp.whereStr  = "WHERE p_seqno = ? and acct_month = ? ";
    param2[0] = wp.getValue("p_seqno");
    param2[1] = wp.getValue("acct_month",k);
    pageQuery(param2);

    return;
  }

  public void selectPtrActgeneral() throws Exception
  {
    wp.daoTable  = "ptr_actgeneral_n";
    wp.selectSQL = "max(revolving_interest1) as revolving_interest1";
    wp.whereStr  = "";
    pageSelect();

    wp.setValue("revolving_interest",""+((long)((wp.getNumber("revolving_interest1",0) * 365 / 2)+0.5)));
    return;
  }

  public void selectCycDiffRateLog() throws Exception
  {
    String print_month = convMonths(wp.getValue("acct_month"),1);
    wp.daoTable  = "cyc_diff_rate_log";
    wp.selectSQL = "decode(sign(run_e_month-cast(? as varchar(6))),1,to_char(add_months(to_date(run_e_month,'yyyymm'),-1),'yyyymm'),run_e_month) as run_e_month1,"
            + "to_char(add_months(to_date(run_e_month,'yyyymm'),2),'yyyymm') as run_e_month2,"
            + "decode(sign(cast(? as varchar(6))-to_char(add_months(to_date(run_e_month,'yyyymm'),-1),'yyyymm')),0,0,1) as run_e_month_cnt ";
    wp.whereStr  = "where proc_month = (select max(proc_month) from cyc_diff_rate_log where run_s_month <= ? and run_e_month >= ? ) "
            + "FETCH FIRST 1 ROW ONLY";
    setString(1,"200912");
    setString(2,print_month);
    setString(3,print_month);
    setString(4,print_month);
    pageSelect();
    return;
  }

  public void selectMktBonusHst() throws Exception  {

    String  printMonth = convMonths(wp.getValue("acct_month"),1);
    String  cvtMonth1   = convMonths(printMonth,1);
    String  cvtMonth2   = convMonths(printMonth,2);
    String  nextMM      = String.format("%3d",Integer.valueOf(cvtMonth1.substring(0,4))-1911)
            + "年" + cvtMonth1.substring(4,6) + "月到期   ";
    String  nextMM2     = String.format("%3d",Integer.valueOf(cvtMonth2.substring(0,4))-1911)
            + "年" + cvtMonth2.substring(4,6) + "月到期   ";

    String bonusHead    = "上月餘額  本月新增  本月調整  本月使用  本月贈予  本月餘額  "
            + "次期結帳日前到期  "
            + "次次期結帳日前到期 "
            + "近半年內到期  近一年內到期";

    wp.setValue("BONUS_HD",bonusHead);

    Object  param2[] = {"",""};

    wp.daoTable  = "mkt_bonus_hst";
    wp.selectSQL = "last_month_bonus as LAST_BN,"
            + "new_add_bonus as NEW_BN,"
            + "adjust_bonus+remove_bonus  as ADJ_BN,"
            + "use_bonus  as USE_BN,"
            + "give_bonus as GIVE_BN,"
            + "diff_bonus,"
            + "net_bonus  as NET_BN,"
            + "first_bonus   as net_tt4,"
            + "second_bonus  as net_tt3,"
            + "last_6_bonus  as net_tt2,"
            + "last_12_bonus as net_tt1,"
            + "bonus_name ";
    wp.whereStr  = "WHERE p_seqno = ? AND acct_month = ? and bonus_type = 'BONU' ";
    param2[0] = wp.getValue("p_seqno");
    param2[1] = printMonth;
    pageSelect(param2);
    if ( wp.selectCnt > 0 ) {
      wp.setValue("DISP-B",""+1);
      String bonus_info = String.format("%9s",wp.getValue("LAST_BN"))
              + String.format("%9s",wp.getValue("NEW_BN"))
              + String.format("%9s",wp.getValue("ADJ_BN"))
              + String.format("%9s",wp.getValue("USE_BN"))
              + String.format("%9s",wp.getValue("GIVE_BN"))
              + String.format("%9s",wp.getValue("NET_BN"))
              + String.format("%12s",wp.getValue("NET_TT4"))
              + String.format("%16s",wp.getValue("NET_TT3"))
              + String.format("%14s",wp.getValue("NET_TT2"))
              + String.format("%14s",wp.getValue("NET_TT1"))
              + String.format("%5s","�");
      wp.setValue("BONUS_INFO",bonus_info);
    }
    return;
  }

  public void selectMktFundHst() throws Exception {

    Object  param2[] = {"",""};
    String  printMonth = convMonths(wp.getValue("acct_month"),1);

    wp.daoTable  = "mkt_fund_hst a,ptr_payment c";
    wp.selectSQL = "a.p_seqno,"
            + "a.fund_code,"
            + "a.net_fund,"
            + "c.bill_desc as fund_name ";
    wp.whereStr  = "WHERE a.p_seqno  = ? and a.acct_month =  ? "
            + "and   a.net_fund > 0 and c.fund_flag  = 'Y' and c.bill_flag = 'Y' "
            + "and   a.fund_code = c.payment_type "
            + "order by a.net_fund desc";

    param2[0] = wp.getValue("p_seqno");
    param2[1] = printMonth;
    pageQuery(param2);
    if ( wp.selectCnt > 0 ) {
      wp.setValue("DISP-T",""+1);
      wp.setValue("DISP-F",""+wp.selectCnt);
    }
    wp.showLogMessage("I","","mkt_fund_hst select cnt "+wp.selectCnt);
    return;
  }

  public void selectPtrBillmsg() throws Exception
  {
    Object  param2[] = {"","",0,0};
    Object  param3[] = {"",""};

    String  printMonth = convMonths(wp.getValue("acct_month"),1);

    wp.daoTable  = "ptr_billmsg";
    wp.selectSQL = "stmt_cycle_parm ";
    wp.whereStr  = "WHERE  msg_month = ? AND acct_type = ? ";
    param3[0]    = printMonth;
    param3[1]    = wp.getValue("ex_acct_type");
    pageQuery(param3);

    int pnt = -1;
    switch( wp.getValue("stmt_cycle") )
    {
      case "03" : pnt = 1; break;
      case "06" : pnt = 2; break;
      case "09" : pnt = 3; break;
      case "12" : pnt = 4; break;
      case "15" : pnt = 5; break;
      case "18" : pnt = 6; break;
      case "21" : pnt = 7; break;
      case "24" : pnt = 8; break;
      case "27" : pnt = 9; break;
      default   : break;
    }

    wp.daoTable  = "ptr_msgset a,ptr_billmsg b";
    wp.selectSQL = "b.msg_month,"
            + "b.acct_type,"
            + "b.msg_type,"
            + "b.param1,"
            + "b.param2,"
            + "b.param3,"
            + "b.param4,"
            + "b.param5,"
            + "a.set_data ";
    wp.whereStr = "WHERE  a.mesg_code(+)  = b.msg_code "
            + "AND    a.mesg_type(+)  = b.msg_type "
            + "AND    a.mesg_month(+) = b.msg_month "
            + "AND    b.msg_month     = ? "
            + "AND    b.acct_type     = ? ";

    if ( wp.getValue("stmt_cycle_parm").length() >= 9 ) {
      wp.whereStr = wp.whereStr
              + "AND ( (b.cycle_type = '01' and substr(b.stmt_cycle_parm,?,1) = 'Y') or (b.cycle_type = '02' and substr(b.stmt_cycle_parm,?,1) = 'N') ) "
              + "ORDER  BY b.acct_type,b.msg_code";
      param2[0] = printMonth;
      param2[1] = wp.getValue("ex_acct_type");
      param2[2] = pnt;
      param2[3] = pnt;
      pageQuery(param2);
    } else  {
      wp.whereStr = wp.whereStr  + "ORDER BY b.acct_type,b.msg_code";
      param3[0] = printMonth;
      param3[1] = wp.getValue("ex_acct_type");
      pageQuery(param3);
    }

    int k =0;
    String mesgInfo="";
    for(int i=0; i<wp.selectCnt; i++) {
      mesgInfo = wp.getValue("param1",i) + wp.getValue("param2",i);
      wp.setValue("MESG_INFO",mesgInfo,k); k++;
      mesgInfo = wp.getValue("param3",i) + wp.getValue("param4",i) + wp.getValue("param5",i);
      wp.setValue("MESG_INFO",mesgInfo,k); k++;
      if ( mesgInfo.length() > 0 )
      { wp.setValue("MESG_INFO","",k);  k++; }
    }
    if ( k > 0 ) {
      wp.setValue("DISP-D1","1");
      wp.setValue("DISP-M",""+k);
    }

    return;
  }

  public void selectCycBillExt() throws Exception
  {

    Object  param2[] = {"",""};

    String  printMonth = convMonths(wp.getValue("acct_month"),1);
    String  monthField  = printMonth.substring(4);

    String sumInterField="",sumFeeField="",cvtMonth="";
    int  months = Integer.parseInt(monthField);
    for( int i=1; i<=months; i++ ) {
      if ( i < 10 )
      { cvtMonth = "0"+i; }
      else
      { cvtMonth = ""+i; }
      sumFeeField    +=  "+fee_amt_"+ cvtMonth;
      sumInterField  +=  "+interest_amt_"+ cvtMonth;
    }

    sumFeeField   = sumFeeField.substring(1)  +" as sum_fee_amt,";
    sumInterField = sumInterField.substring(1)+" as sum_inter_amt ";

    daoTid = "bext.";
    wp.daoTable  = "cyc_bill_ext";
    wp.selectSQL = "p_seqno,"
            + "acct_type,"
            + "curr_code,"
            + "capital_amt_"   + monthField+","
            + "min_pay_cnt_"   + monthField+","
            + "min_pay_amt_"   + monthField+","
            + "fee_amt_"       + monthField+","
            + "interest_amt_"  + monthField+","
            + sumFeeField
            + sumInterField;
    wp.whereStr  = "WHERE  p_seqno = ? and acct_year = ? order by curr_code desc";

    param2[0] = wp.getValue("p_seqno");
    param2[1] = printMonth.substring(0,4);
    pageQuery(param2);

    int k =0;
    for( int i=0; i<wp.selectCnt; i++) {
      k = i+2;
      String intAmt = "$"+formatAmount("#######0.00",wp.getNumber("bext.sum_inter_amt",i));
      String feeAmt = "$"+formatAmount("#######0.00",wp.getNumber("bext.sum_fee_amt",i));
      wp.setValue("INTR_INFO_"+k,"本年度(包含本期帳單)您使用信用卡已產生利息及費用累計金額 , ",0);
      wp.setValue("INTR_INFO_"+k,"分別為 : ("+wp.getValue("curr_name",i)+")  "+intAmt+" , "+feeAmt,1);
      wp.setValue("DISP-D"+k,"1");
      wp.setValue("DISP-I"+k,"2");

      if ( i == 0 ) {
        wp.setValue("MIN_PAY_CNT",wp.getValue("bext.min_pay_cnt_"+monthField));
        if ( wp.getNumber("bext.capital_amt_"+monthField,i) != 0 ) {
          String cap_amt = wp.getValue("curr_name") + " $"+formatAmount("#######0.00",wp.getNumber("bext.capital_amt_"+monthField,i));
          wp.setValue("CAP_AMT",cap_amt);
          wp.setValue("DISP-Y2","1");
        }
      }
    }

    processNumonbillInfo(wp.selectCnt,monthField);
    return;
  }

  public void processNumonbillInfo(int cbetCount, String monthField) throws Exception {

    String currChin="",min_pay_cnt="",minPayAmt="",interest_amt="",fee_amt="";
    String mark = "、";

    for( int m=0; m<cbetCount; m++ ) {
      if ( m == (cbetCount-1) )
      { mark = ""; }
      double cvt_min_pay = wp.getNumber("bext.min_pay_amt_"+monthField,m);
      minPayAmt +=  (wp.getValue("#-"+wp.getValue("bext.curr_code",m)) +"$"+ formatAmount("#####0.00",cvt_min_pay) + mark);
    }

    wp.setValue("B_AMT",minPayAmt);
    return;
  }

  public String formatAmount(String pattern,double amount) throws Exception
  {
    DecimalFormat df = new DecimalFormat(pattern);
    return df.format(amount);
  }

  public String toDispDate(String date)
  {
    if ( date.length() != 8 )
    { return date; }
    return date.substring(0,4) +"/"+date.substring(4,6)+"/"+date.substring(6,8);
  }

  public String convDates(String parmDate,int ki) throws Exception
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
    Calendar cal = Calendar.getInstance();
    cal.setTime( dateFormat.parse(parmDate));
    cal.add( Calendar.DATE, ki );
    return dateFormat.format(cal.getTime());
  }

  public String convMonths(String parmDate,int ki) throws Exception
  {
    parmDate  = parmDate.substring(0,6)+"15";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date current = sdf.parse(parmDate);
    Calendar cal = Calendar.getInstance();
    cal.setTime(current);
    cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH)+ki));
    current = cal.getTime();
    String lastDate = ""+cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    String cvtDate  = sdf.format(current);
    cvtDate = cvtDate.substring(0,6);
    return cvtDate;
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey  = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 and acct_type != '07' order by acct_type");

    }
    catch (Exception ex) { }
  }

  @Override
  public void dataRead() throws Exception {
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void initPage() {
  }

  @Override
  public void procFunc() {
  }

  @Override
  public void saveFunc() throws Exception {
  }

  @Override
  public void initButton() {
  }

} // end of class
