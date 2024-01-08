package Mkt;
/**
 * 2023-0807 V1.00.00   JH    initial
 * 2023-0809 V1.00.01   JH    ++CRM18I..N
 * 2023-1026 V1.00.02   JH    ++getAcctNo()
 * */

import com.CommCrdRoutine;
import com.DataSet;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "deprecation"})
public class MktRm18 extends com.BaseBatch {
private final String PROGNAME = "聯名卡企業消費回饋金入帳表  2023-1026 V1.00.02";
CommCrdRoutine comcr = null;
//---------
//--
//List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
int reportSeq = 0;
String reportId = "CRM18D";
String reportName = "聯名卡企業店內外消費回饋金入帳表";
int reportPage=0;
String reportHead01="";

String isProcYm="";
//=*****************************************************************************
public static void main(String[] args) {
   MktRm18 proc = new MktRm18();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : MktRm18 [busi_date, callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
        // setBusiDate(Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD));
         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
         isProcYm=commString.left(hBusiDate,6);
      }
   }
   if (args.length > 0) {
      callBatchSeqno(args[args.length -1]);
   }

   dbConnect();
   callBatch(0, 0, 0);
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());


   String lsDD=commString.mid(hBusiDate,6,2);
   if (!eq(lsDD,"02")) {
      errmsg("每月2日執行,本日非執行日!! busi_date[%s], process end....", hBusiDate );
      okExit(0);
   }
      
   //isProcYm=commString.left(hBusiDate,6);
   if (empty(isProcYm)) {
      sqlCmd = "select max(rew_yyyymm) as rew_yymm"
       +" from mkt_re_group "
       +" where reward_type ='2'"
      ;
      sqlSelect("");
      if (sqlNrow >0) isProcYm=colSs(0,"rew_yymm");
   }
   if (empty(isProcYm)) {
      isProcYm=commString.left(hBusiDate,6);
   }
   printf(" -->處理年月[%s], 列印日期[%s]", isProcYm, sysDate);

   loadMktMemberDtl();
   procCrm18D();
   procCrm18E();

   //-同0.2%回饋-
   //CRM18F : 宜蘭信合社聯名卡企業消費回饋金  (團代1680)--------------
   reportId="CRM18F";
   reportName="宜蘭信合社聯名卡企業消費回饋金";
   reportHead01 ="宜蘭信合社帳戶";
   String lsWhere=" and A.group_code in ('1680')";
   procCrm18F(lsWhere);
   //CRM18G : 彰化五信合社聯名卡企業消費回饋金(團代1682)---------------
   reportId="CRM18G";
   reportName="彰化五信合社聯名卡企業消費回饋金";
   reportHead01 ="彰化五信合社帳戶";  //宜蘭信合社
   lsWhere=" and A.group_code in ('1682')";
   procCrm18F(lsWhere);
   //CRM18H : 高雄三信企業消費回饋金(團代1683)----------------
   reportId="CRM18H";
   reportName="高雄三信企業消費回饋金";
   reportHead01 ="高雄三信帳戶";
   lsWhere=" and A.group_code in ('1683')";
   procCrm18F(lsWhere);
   //CRM18I : 台北第五信用合作社聯名卡企業消費回饋金(團代1686)----------------
   //-由MktRM18I,J,K,L,M,N取代----
   reportId="CRM18I";
   reportName="台北第五信用合作社聯名卡企業消費回饋金";
   reportHead01 ="台北五信帳戶";
   lsWhere=" and A.group_code in ('1686') ";
   procCrm18I(lsWhere);
   //J:花蓮第二信用合作社, 1688----
   reportId="CRM18J";
   reportName="花蓮第二信用合作社聯名卡企業消費回饋金";
   reportHead01 ="花蓮二信帳戶";
   lsWhere=" and A.group_code in ('1688') ";
   procCrm18I(lsWhere);
   //-K:台中第二信用合作, 1689-
   reportId="CRM18K";
   reportName="台中第二信用合作社聯名卡企業消費回饋金";
   reportHead01 ="台中二信帳戶";
   lsWhere=" and A.group_code in ('1689') ";
   procCrm18I(lsWhere);
   //-L:花蓮第一信用合作, 1690-
   reportId="CRM18L";
   reportName="花蓮第一信用合作社聯名卡企業消費回饋金";
   reportHead01 ="花蓮一信帳戶";
   lsWhere=" and A.group_code in ('1690') ";
   procCrm18I(lsWhere);
   //-M:台南第三信用合作社, 1691-
   reportId="CRM18M";
   reportName="台南第三信用合作社聯名卡企業消費回饋金";
   reportHead01 ="台南三信帳戶";
   lsWhere=" and A.group_code in ('1691') ";
   procCrm18I(lsWhere);
   //-N:嘉義第三信用合作, 1692-
   reportId="CRM18N";
   reportName="嘉義第三信用合作社聯名卡企業消費回饋金";
   reportHead01 ="嘉義三信帳戶";
   lsWhere=" and A.group_code in ('1692') ";
   procCrm18I(lsWhere);

   sqlCommit();
   endProgram();
}
//-----
//--------------------
com.DataSet dsBranch=new DataSet();
void loadMktMemberDtl() throws Exception {
   sqlCmd ="SELECT member_corp_no, branch, member_name " +
           " FROM mkt_member_dtl " +
           " ORDER BY 1,2"
           ;
   sqlQuery(dsBranch,"",null);
   printf(" load mkt_member_dtl rows[%s]", dsBranch.rowCount());
}
//========================
void procCrm18D() throws Exception {
   reportSeq=0;
   reportId="CRM18D";
   reportName ="漢來美食聯名卡企業店內外消費回饋金入帳表";
   List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
   printf(" -- printReport: %s %s", reportId, reportName);
   //---------
   StringBuffer tt = new StringBuffer();

   tt =setHeader01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt =setHeader02(1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt=new StringBuffer("");
   commString.insert(tt,"漢來美食活期帳戶",1);
   commString.insert(tt,"店內外消費總額",37);
   commString.insert(tt,"0.2%回饋金",70);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt = new StringBuffer();
   String ss =commString.repeat("=",132);
   commString.insert(tt,ss,1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-------
   String sql1 ="select min(acct_no) acct_no"
           +", sum(nvl(out_purch_amt,0)) out_purch_amt"
           +", sum(nvl(int_purch_amt,0)) int_purch_amt"
           +", sum(nvl(rew_amount,0)) rew_amount from ("
           +" SELECT A.group_code, A.member_corp_no, C.acct_no"
           +", B.rew_yyyymm, B.out_purch_amt, B.int_purch_amt, B.rew_amount"
           +" FROM ptr_group_code A"
           +" JOIN mkt_member C ON C.member_corp_no=A.member_corp_no"
           +" left JOIN MKT_RE_GROUP B ON B.group_code=A.group_code"
           +" and B.rew_yyyymm >=? and B.rew_yyyymm <=? and B.reward_type='2' "
           +" WHERE 1=1 "
           +" AND A.group_code IN ('1677','1631','1893')"
           //+" AND B.member_corp_no<>''"
           +" )"
           ;
   //-月小計--------------
   ppp(1, isProcYm);
   ppp(isProcYm);
   sqlSelect(sql1);
   double lm_totAmt=0, lm_rewAmt=0;
   String lsAcctNo="";
   if (sqlNrow >0) {
      totalCnt++;
      lsAcctNo =colSs(0,"acct_no");
      lm_totAmt =colNum(0,"out_purch_amt")+colNum(0,"int_purch_amt");
      lm_rewAmt =colNum(0,"rew_amount");
   }
   else {
      lsAcctNo =getAcctNo(" and A.group_code IN ('1677','1631','1893')");
   }
   //-年累計----------
   ss =commString.left(isProcYm,4)+"01";
   ppp(1, ss);
   ppp(isProcYm);
   sqlSelect(sql1);
   double lm_totAmtYY=0, lm_rewAmtYY=0;
   if (sqlNrow >0) {
      lm_totAmtYY =colNum(0,"out_purch_amt")+colNum(0,"int_purch_amt");
      lm_rewAmtYY =colNum(0,"rew_amount");
   }

   tt =new StringBuffer("");
   commString.insert(tt,lsAcctNo,1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-----
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //-------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", commString.repeat("-",132)));
   //-小計-
   tt =new StringBuffer("");
   commString.insert(tt,"小  計:",1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-累計-
   tt =new StringBuffer("");
   commString.insert(tt,"本年累積總計:",1);
   ss =commString.numFormat(lm_totAmtYY,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmtYY,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //--------
   ss="說明：新增一般消費金額計算方式: TXCode:40+30+42-41-31惟需排除各類稅款、規費、學雜費及代收公共事業、代收明";
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //------------
   //ss="　　　水扶輪社社員費及悠遊卡、一卡通自動加值等所產生帳款";
   tt =new StringBuffer("");
   commString.insert(tt,"水扶輪社社員費及悠遊卡、一卡通自動加值等所產生帳款",7);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));

   tt =setFooter01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   comcr.insertPtrBatchRpt(lpar1);

   printf(" 產生報表: "+reportId+", row="+reportSeq);
   lpar1.clear();
   lpar1 =null;
}
//=============
void procCrm18E() throws Exception {
   reportSeq=0;
   reportId="CRM18E";
   reportName ="鹿港天后宮認同卡企業回饋金明細表";
   List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
   printf(" -- printReport: %s %s", reportId, reportName);
   //---------
   StringBuffer tt = new StringBuffer();

   tt =setHeader01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt=setHeader02(1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt=new StringBuffer("");
   //鹿港天后宮帳戶                            消費總額                   0.3%回饋金
   commString.insert(tt,"鹿港天后宮帳戶",1);
   commString.insert(tt,"消費總額",43);
   commString.insert(tt,"0.3%回饋金",70);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   String ss =commString.repeat("=",132);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //-------
   String sql1 ="select min(acct_no) acct_no"
           +", sum(nvl(out_purch_amt,0)) out_purch_amt"
           +", sum(nvl(int_purch_amt,0)) int_purch_amt"
           +", sum(nvl(rew_amount,0)) rew_amount from ("
           +" SELECT A.group_code, A.member_corp_no, C.acct_no"
           +", B.rew_yyyymm, B.out_purch_amt, B.int_purch_amt, B.rew_amount"
           +" FROM ptr_group_code A"
           +" JOIN mkt_member C ON C.member_corp_no=A.member_corp_no"
           +" left JOIN MKT_RE_GROUP B ON B.group_code=A.group_code"
           +" and B.rew_yyyymm >=? and B.rew_yyyymm <=? and B.reward_type ='2' "
           +" WHERE 1=1 "
           +" AND A.group_code IN ('1679','1894')"
           +" )"
           ;
   //-月小計--------------
   ppp(1, isProcYm);
   ppp(isProcYm);
   sqlSelect(sql1);
   String lsAcctNo="";
   double lm_totAmt=0, lm_rewAmt=0;
   if (sqlNrow >0) {
      totalCnt++;
      lsAcctNo =colSs(0,"acct_no");
      lm_totAmt =colNum(0,"out_purch_amt")+colNum(0,"int_purch_amt");
      lm_rewAmt =colNum(0,"rew_amount");
   }
   else {
      lsAcctNo =getAcctNo(" and A.group_code IN ('1679','1894')");
   }
   tt =new StringBuffer("");
   commString.insert(tt,lsAcctNo,1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-----
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //-------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", commString.repeat("-",132)));
   //-小計-
   tt =new StringBuffer("");
   commString.insert(tt,"小  計:",1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //--------
   ss="說明：新增一般消費金額計算方式: TXCode:40+42-41惟需排除各類稅款、規費、學雜費及代收公共事業、富邦壽";
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //------------
   //ss="　　　保險及電子票證自動加值等所產生帳款";
   tt =new StringBuffer("");
   commString.insert(tt,"保險及電子票證自動加值等所產生帳款",7);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));

   tt =setFooter01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   //--------------
   comcr.insertPtrBatchRpt(lpar1);

   printf(" 產生報表: "+reportId+", row="+reportSeq);
   lpar1.clear();
   lpar1 =null;
}
//========================
void procCrm18F(String lsWhere) throws Exception {
//CRM18F,G,H--
   String ss="";
   reportSeq=0;
   List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
   printf(" -- printReport: %s %s", reportId, reportName);
   //---------
//   StringBuffer tt = new StringBuffer();

   StringBuffer tt =setHeader01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt =setHeader02(1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-Title_01-----------
   tt=new StringBuffer("");
   commString.insert(tt,reportHead01,1);
   commString.insert(tt,"店內外消費總額",37);
   commString.insert(tt,"0.2%回饋金",70);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt = new StringBuffer();
   ss =commString.repeat("=",132);
   commString.insert(tt,ss,1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-------
   String sql1 ="select min(acct_no) acct_no"
           +", sum(nvl(out_purch_amt,0)) out_purch_amt"
           +", sum(nvl(int_purch_amt,0)) int_purch_amt"
           +", sum(nvl(rew_amount,0)) rew_amount from ("
           +" SELECT A.group_code, A.member_corp_no, C.acct_no"
           +", B.rew_yyyymm, B.out_purch_amt, B.int_purch_amt, B.rew_amount"
           +" FROM ptr_group_code A"
           +" JOIN mkt_member C ON C.member_corp_no=A.member_corp_no"
           +" left JOIN MKT_RE_GROUP B ON B.group_code=A.group_code"
           +" and B.rew_yyyymm >=? and B.rew_yyyymm <=? and B.reward_type='2' "
           +" WHERE 1=1 "
           +lsWhere
           +" )"
           ;
   //-月小計--------------
   ppp(1, isProcYm);
   ppp(isProcYm);
   sqlSelect(sql1);
   String lsAcctNo="";
   double lm_totAmt=0, lm_rewAmt=0;
   if (sqlNrow >0) {
      totalCnt++;
      lsAcctNo =colSs(0,"acct_no");
      lm_totAmt =colNum(0,"out_purch_amt")+colNum(0,"int_purch_amt");
      lm_rewAmt =colNum(0,"rew_amount");
   }
   else {
      lsAcctNo =getAcctNo(lsWhere);
   }
   //-年累計----------
   ss =commString.left(isProcYm,4)+"01";
   ppp(1, ss);
   ppp(isProcYm);
   sqlSelect(sql1);
   double lm_totAmtYY=0, lm_rewAmtYY=0;
   if (sqlNrow >0) {
      lm_totAmtYY =colNum(0,"out_purch_amt")+colNum(0,"int_purch_amt");
      lm_rewAmtYY =colNum(0,"rew_amount");
   }

   tt =new StringBuffer("");
   commString.insert(tt,lsAcctNo,1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-----
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //-------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", commString.repeat("-",132)));
   //-小計-
   tt =new StringBuffer("");
   commString.insert(tt,"小  計:",1);
   ss =commString.numFormat(lm_totAmt,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmt,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-累計-
   tt =new StringBuffer("");
   commString.insert(tt,"本年累積總計:",1);
   ss =commString.numFormat(lm_totAmtYY,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,33);
   ss =commString.numFormat(lm_rewAmtYY,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,66);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //--------
   ss="說明：新增一般消費金額計算方式: TXCode:40+30+42-41-31惟需排除各類稅款、規費、學雜費及代收公共事業、代收明";
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //------------
   //ss="　　　水扶輪社社員費及悠遊卡、一卡通自動加值等所產生帳款";
   tt =new StringBuffer("");
   commString.insert(tt,"水扶輪社社員費及悠遊卡、一卡通自動加值等所產生帳款",7);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));

   tt =setFooter01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   comcr.insertPtrBatchRpt(lpar1);

   printf(" 產生報表: "+reportId+", row="+reportSeq);
   lpar1.clear();
   lpar1 =null;
}
//========================
void procCrm18I(String lsWhere) throws Exception {
//-報表CRM18I, CRM18J ~  CRM18N--
   String ss="";
   reportSeq=0;
   List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
   printf(" -- printReport: %s %s", reportId, reportName);
   //---------

   StringBuffer tt =setHeader01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   tt =setHeader02(1);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //-Title_01-----------
   tt=new StringBuffer("");
   commString.insert(tt,reportHead01,1);
   commString.insert(tt,"消費總額",41);
   commString.insert(tt,"0.2%回饋金",65);
   commString.insert(tt,"分社代號",81);
   commString.insert(tt,"分社中文名稱",94);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   ss =commString.repeat("=",132);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //-------
   String sql1 ="select acct_no, member_corp_no, mcht_group_id as branch_no"
           +", nvl(total_purch_amt,0) tot_purch_amt"
           +", nvl(rew_amount,0) rew_amt"
           +" from ("
           +" SELECT A.group_code, A.member_corp_no, C.acct_no "
           +", B.total_purch_amt, B.rew_amount, B.mcht_group_id "
           +" FROM ptr_group_code A "
           +" JOIN mkt_member C ON C.member_corp_no=A.member_corp_no "
           +" left JOIN MKT_RE_GROUP B ON B.group_code=A.group_code "
           +" and B.rew_yyyymm >=? and B.rew_yyyymm <=? "
           +" and B.reward_type='2' and B.mcht_group_id<>'XXXX' "
           +" WHERE 1=1"
           +lsWhere
           +" )"  // group by acct_no, member_corp_no, mcht_group_id"
           ;
   //-月小計--------------
   ppp(1, isProcYm);
   ppp(isProcYm);
   sqlSelect(sql1);
   int llNrow =sqlNrow;
   String ls_acctNo="", ls_corpNo="", ls_brnNo="", ls_brnName="";
   double lm_totAmt=0, lm_rewAmt=0;
   double lm_totAmtSum=0, lm_rewAmtSum=0;
   for (int ll = 0; ll <llNrow ; ll++) {
      ls_acctNo=colSs(ll,"acct_no");
      ls_corpNo =colSs(ll,"member_corp_no");
      ls_brnNo =colSs(ll, "branch_no");
      lm_totAmt =colNum(ll,"tot_purch_amt");
      lm_rewAmt =colNum(ll, "rew_amt");
      ls_brnName =getBranchName(ls_corpNo,ls_brnNo);

      tt =new StringBuffer("");
      commString.insert(tt,ls_acctNo,1);
      ss =commString.numFormat(lm_totAmt,"#,##0");
      ss =commString.lpad(ss,18," ");
      commString.insert(tt,ss,31);
      ss =commString.numFormat(lm_rewAmt,"#,##0");
      ss =commString.lpad(ss,14," ");
      commString.insert(tt,ss,61);
      commString.insert(tt,ls_brnNo,81);
      commString.insert(tt,ls_brnName,95);
      lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

      lm_totAmtSum +=lm_totAmt;
      lm_rewAmtSum +=lm_rewAmt;
   }

   //-----
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //-------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", commString.repeat("-",132)));
   //-小計-
   tt =new StringBuffer("");
   commString.insert(tt,"小  計:",1);
   ss =commString.numFormat(lm_totAmtSum,"#,##0");
   ss =commString.lpad(ss,18," ");
   commString.insert(tt,ss,31);
   ss =commString.numFormat(lm_rewAmtSum,"#,##0");
   ss =commString.lpad(ss,14," ");
   commString.insert(tt,ss,61);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   //--------
   ss="說明：新增一般消費金額計算方式: TXCode:40+42-41惟需排除各類稅款、規費、學雜費及代收公共事業、富邦壽";
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", ss));
   //------------
   tt =new StringBuffer("");
//   ss="　　　保險及電子票證自動加值等所產生帳款";
   tt =new StringBuffer("");
   commString.insert(tt,"保險及電子票證自動加值等所產生帳款",7);
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));
   //--------
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", "  "));

   tt =setFooter01();
   lpar1.add(comcr.putReport(reportId, reportName,"", ++reportSeq, "0", tt.toString()));

   comcr.insertPtrBatchRpt(lpar1);

   printf(" 產生報表: "+reportId+", row="+reportSeq);
   lpar1.clear();
   lpar1 =null;
}
//-------------
String getBranchName(String ls_corpNo, String ls_brnNo) {
   String ls_name="無名社";
   for (int ll = 0; ll <dsBranch.rowCount() ; ll++) {
      dsBranch.listCurr(ll);
      if (dsBranch.colEq("member_corp_no",ls_corpNo)==false) continue;
      if (dsBranch.colEq("branch",ls_brnNo)==false) continue;

      ls_name =dsBranch.colSs("member_name");
      break;
   }

   return ls_name;
}
//----------
String getAcctNo(String a_where) throws Exception {
   String ls_where =a_where.replaceAll(" and group_code "," and A.group_code ");

   sqlCmd ="SELECT DISTINCT C.acct_no"
       +" FROM ptr_group_code A"
       +" JOIN mkt_member C ON C.member_corp_no=A.member_corp_no"
       +" WHERE 1=1 "
       +ls_where
   ;
   sqlSelect();
   if (sqlNrow >0) {
      return colSs("acct_no");
   }
   return "";
}
//=============
StringBuffer setHeader01() {
   StringBuffer tt =new StringBuffer("");
   commString.insert(tt, "分行代號: 3144信用卡部", 1);
   commString.insertCenter(tt,reportName,110);
   commString.insert(tt, "保存年限: 二年", 97);
   return tt;
}
//--------
StringBuffer setHeader02(int aiPage) {
   StringBuffer tt =new StringBuffer("");
   commString.insert(tt,"報表代號: "+reportId+" 科目代號:",1);
   String ss=commDate.toTwDate(sysDate);
   ss ="中華民國 "+commString.mid(ss,0,3)+"年 "+commString.mid(ss,3,2)+"月 "+commString.mid(ss,5)+"日";
   commString.insertCenter(tt,ss,110);
   reportPage =1;
   ss ="第 "+String.format("%04d",aiPage)+"頁";
   commString.insert(tt, ss, 97);
   return tt;
}
//----------
StringBuffer setFooter01() {
   StringBuffer tt =new StringBuffer("");
   commString.insert(tt,"  報表單位: 資訊部",1);
   commString.insert(tt,"經 辦",31);
   commString.insert(tt,"核 章",55);
   commString.insert(tt,"FORM:MRGS21",78);
   return tt;
}

}
