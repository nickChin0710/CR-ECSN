package Mkt;
/**
 * 2023-0808 V1.00.01   JH    re-coding
 * 2023-1026 V1.00.02   JH    regp_mcht_group_id=''
 * 2023-1122 V1.00.03   JH    acct_month=busi_date -1MM
 * 2024-0104 V1.00.04   JH    bugfix-mkt_re_group.dupl; ls_branch=ZZZZ
 * */
import com.DataSet;

public class MktA220 extends com.BaseBatch {
private final String PROGNAME = "消費回饋聯名機構處理程式  2024-0104 V1.00.04";
HH hh=new HH();
//-----------
class HH {
   String rowid="";
   String group_code ="";                 //團體代號        請參考ptr_group_code
   String description ="";                //說明
   String reward_type ="";                //回饋方式        0:不產生 1: 消費總金額   2: 分店內店外
   double base_amt_1 =0;                  //例外扣除額
   double[] purch_amt_s ={0,0,0,0,0};              //消費級距_起_a1..a5
   double[] purch_amt_e ={0,0,0,0,0};              //消費級距_迄_a1..a5
   double[] rate_a ={0,0,0,0,0};                     //回饋比率_a1..a5
   double[] rate_a2 ={0,0,0,0,0};                    //回饋比率_a12
   double[] int_amt_s ={0,0,0,0,0};                 //店內消費級距_起_1..5
   double[] int_amt_e ={0,0,0,0,0};                 //店內消費級距_迄_1..5
   double[] int_rate ={0,0,0,0,0};                  //店內提撥率_1..5
   double[] int_rate_2 ={0,0,0,0,0};                 //店內提撥率_12
   double[] out_amt_s ={0,0,0,0,0};                 //店外消費級距_起_1
   double[] out_amt_e ={0,0,0,0,0};                 //店外消費級距_迄_1
   double[] out_rate ={0,0,0,0,0};                  //店外提撥率_1
   double[] out_rate_2 ={0,0,0,0,0};                 //店外提撥率_12
   boolean acctCode_bl =false;              //消費本金BL
   boolean acctCode_bl_in =false;           //消費本金BL_IN
   boolean acctCode_bl_out =false;          //消費本金BL_OUT
   boolean acctCode_it =false;              //消費本金IT
   boolean acctCode_it_in =false;           //消費本金IT_IN
   boolean acctCode_it_out =false;          //消費本金IT_OUT
   boolean acctCode_ca =false;              //消費本金CA
   boolean acctCode_id =false;              //消費本金ID
   boolean acctCode_ao =false;              //消費本金AO
   boolean acctCode_ot =false;              //消費本金OT
   String present_type ="";               //回饋比例計算方式
   String program_code ="";               //專案代號
   String purch_date_type ="";            //活動期間類別
   int    run_time_dd =0;                 //執行區間執行日
   //---------
   int    ex_mcht_group=0;
   int    ex_mcht_no=0;
   int    in_mcht_group=0;
   int    in_mcht_pos=0;
   int    exMchtFlag=0;  //排除特店--
   int    inOutFlag =0;  //店內特店
   int    posFlag=0;     //終端機
   String bill_post_date="";
   String bill_acct_month="";
   //-mkt_re_group---------
   String regp_mcht_group_id ="";              //特店群組代號
   String regp_rew_yyyymm ="";                 //回饋年月         YYYYMM
   double regp_total_purch_amt =0;             //總消費筆數
   int    regp_total_purch_cnt =0;             //總消費額
   double regp_avg_purch_amt =0;               //平均消費額
   double regp_discount_amt =0;                //需扣除之例外金額
   double regp_out_purch_amt =0;               //店外消費金額
   double regp_out_amt =0;                     //店外提撥金額
   double regp_out_amt2 =0;                    //店外提撥金額
   double regp_int_purch_amt =0;               //店內消費金額
   double regp_int_amt =0;                     //店內提撥金額
   double regp_in_amt2 =0;                     //店內提撥金額2
   double regp_rew_amount =0;                  //回饋金額
   double regp_rew_amount2 =0;                 //總回饋金額2
   double regp_bl_amt =0;                      //簽帳款
   double regp_bl_amt_in =0;                   //簽帳款店內
   double regp_it_amt =0;                      //分期付款
   double regp_it_amt_in =0;                   //分期付款店內
   double regp_ca_amt =0;                      //預借現金
   double regp_id_amt =0;                      //代收款
   double regp_ao_amt =0;                      //餘額代償
   double regp_ot_amt =0;                      //其他應收款
   double regp_exp_tax_amt =0;                 //排除特店消費款
   double regp_term_rew_amount =0;             //終端機回饋金額
   int    regp_term_total_cnt =0;              //終端機消費筆數
   double regp_term_total_amt =0;              //終端機消費金額
   String regp_card_note ="";                  //卡片等級

   //----------------------
   public void init_data() {
      rowid ="";
      group_code ="";
      description ="";
      reward_type ="";
      base_amt_1 =0;
      for (int ii = 0; ii <5 ; ii++) {
         purch_amt_s[ii] =0;
         purch_amt_e[ii] =0;
         rate_a[ii] =0;
         rate_a2[ii] =0;
         int_amt_s[ii] =0;
         int_amt_e[ii] =0;
         int_rate[ii] =0;
         int_rate_2[ii] =0;
         out_amt_s[ii] =0;
         out_amt_e[ii] =0;
         out_rate[ii] =0;
         out_rate_2[ii] =0;
      }
      acctCode_bl =false;
      acctCode_bl_in =false;
      acctCode_bl_out =false;
      acctCode_it =false;
      acctCode_it_in =false;
      acctCode_it_out =false;
      acctCode_ca =false;
      acctCode_id =false;
      acctCode_ao =false;
      acctCode_ot =false;
      present_type ="";
      program_code ="";
      purch_date_type ="";
      run_time_dd =0;
      //--
      ex_mcht_group =0;
      ex_mcht_no =0;
      in_mcht_group =0;
      in_mcht_pos=0;
      exMchtFlag=0;
      inOutFlag =0;
      posFlag =0;
      bill_post_date="";
      bill_acct_month="";
   }
   void init_reGroup() {
      regp_mcht_group_id ="";
      regp_rew_yyyymm ="";
      regp_total_purch_amt =0;
      regp_total_purch_cnt =0;
      regp_avg_purch_amt =0;
      regp_discount_amt =0;
      regp_out_purch_amt =0;
      regp_out_amt =0;
      regp_int_purch_amt =0;
      regp_int_amt =0;
      regp_rew_amount =0;
      regp_in_amt2 =0;
      regp_out_amt2 =0;
      regp_rew_amount2 =0;
      regp_bl_amt =0;
      regp_bl_amt_in =0;
      regp_it_amt =0;
      regp_it_amt_in =0;
      regp_ca_amt =0;
      regp_id_amt =0;
      regp_ao_amt =0;
      regp_ot_amt =0;
      regp_exp_tax_amt =0;
      regp_term_rew_amount =0;
      regp_term_total_cnt =0;
      regp_term_total_amt =0;
      regp_card_note ="";
   }
}
//--
//parm--
String isAcctMonth="", isGroupCode="";
int iiParmDD=0, iiBusiDD=0;
boolean ibForceRun=false;
//------------
String isExpMchtSql="";  //排除一般消費特店bill.ecs_cus_mcht_no
//=*****************************************************************************
public static void main(String[] args) {
   MktA220 proc = new MktA220();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 4) {
      printf("Usage : MktA220 [busiDate] [group_code] [run_time_dd]");
      printf(" parm: run_time_DD: 依消費日，每月DD日執行");
      okExit(0);
   }

   int li_args=args.length;
   if (li_args == 1) {
      if (args[0].length() ==8) setBusiDate(args[0]);
      else if (args[0].length() ==4) isGroupCode =args[0];
      else if (args[0].length() ==2) iiParmDD =ss2int(args[0]);
   }
   else if (li_args == 2) {
      if (args[0].length() ==8) setBusiDate(args[0]);
      else if (args[0].length() ==4) isGroupCode =args[0];
      else if (args[0].length() ==2) iiParmDD =ss2int(args[0]);
      //--
      if (args[1].length() ==4) isGroupCode =args[1];
      else if (args[1].length() ==2) iiParmDD =ss2int(args[1]);
   }
   else if (li_args == 3) {
      if (args[0].length() ==8) setBusiDate(args[0]);
      if (args[1].length() ==4) isGroupCode =args[1];
      if (args[2].length() ==2) iiParmDD =ss2int(args[2]);
   }

   dbConnect();

   //-輸入參數: 表示強制執行--------
   ibForceRun =false;
   if (noEmpty(isGroupCode) || iiParmDD>0) {
      ibForceRun =true;
   }
   isAcctMonth =commDate.monthAdd(hBusiDate,-1);
   iiBusiDD =ss2int(commString.right(hBusiDate,2));

   isExpMchtSql=" SELECT data_code FROM MKT_MCHTGP_DATA" +
           " WHERE 1=1" +
           " AND data_key ='MKTR00001'" +
           " AND table_name ='MKT_MCHT_GP' ";

   loadPtr_bn_data();

   selectMktPurcGp();

   sqlCommit();

//   dsCard.dataClear();
//   dsCard =null;

   endProgram();
}
//===============================
void selectMktPurcGp() throws Exception {
   sqlCmd ="select A.*"+
           " from mkt_purc_gp A"+
           " where 1=1"+
           " and reward_type <>'0'"+
           " order by group_code"
           ;
   int liCusr =openCursor();
   while (fetchTable(liCusr)) {
      hh.init_data();
      hh.init_reGroup();
      totalCnt++;

      hh.group_code           =colSs(0, "group_code");
      hh.description          =colSs(0, "description");
      hh.reward_type          =colSs(0, "reward_type");
      hh.base_amt_1           =colNum(0, "base_amt_1");
      hh.purch_amt_s[0]       =colNum(0, "purch_amt_s_a1");
      hh.purch_amt_e[0]       =colNum(0, "purch_amt_e_a1");
      hh.rate_a[0]              =colNum(0, "rate_a1");
      hh.purch_amt_s[1]       =colNum(0, "purch_amt_s_a2");
      hh.purch_amt_e[1]       =colNum(0, "purch_amt_e_a2");
      hh.rate_a2[1]            =colNum(0, "rate_a2");
      hh.purch_amt_s[2]       =colNum(0, "purch_amt_s_a3");
      hh.purch_amt_e[2]       =colNum(0, "purch_amt_e_a3");
      hh.rate_a[2]              =colNum(0, "rate_a3");
      hh.purch_amt_s[3]       =colNum(0, "purch_amt_s_a4");
      hh.purch_amt_e[3]       =colNum(0, "purch_amt_e_a4");
      hh.rate_a[3]              =colNum(0, "rate_a4");
      hh.purch_amt_s[4]       =colNum(0, "purch_amt_s_a5");
      hh.purch_amt_e[4]       =colNum(0, "purch_amt_e_a5");
      hh.rate_a[4]              =colNum(0, "rate_a5");
      hh.int_amt_s[0]          =colNum(0, "int_amt_s_1");
      hh.int_amt_e[0]          =colNum(0, "int_amt_e_1");
      hh.int_rate[0]           =colNum(0, "int_rate_1");
      hh.int_amt_s[1]          =colNum(0, "int_amt_s_2");
      hh.int_amt_e[1]          =colNum(0, "int_amt_e_2");
      hh.int_rate[1]           =colNum(0, "int_rate_2");
      hh.int_amt_s[2]          =colNum(0, "int_amt_s_3");
      hh.int_amt_e[2]          =colNum(0, "int_amt_e_3");
      hh.int_rate[2]           =colNum(0, "int_rate_3");
      hh.int_amt_s[3]          =colNum(0, "int_amt_s_4");
      hh.int_amt_e[3]          =colNum(0, "int_amt_e_4");
      hh.int_rate[3]           =colNum(0, "int_rate_4");
      hh.int_amt_s[4]          =colNum(0, "int_amt_s_5");
      hh.int_amt_e[4]          =colNum(0, "int_amt_e_5");
      hh.int_rate[4]           =colNum(0, "int_rate_5");
      hh.out_amt_s[1]          =colNum(0, "out_amt_s_1");
      hh.out_amt_e[0]          =colNum(0, "out_amt_e_1");
      hh.out_rate[0]           =colNum(0, "out_rate_1");
      hh.out_amt_s[1]          =colNum(0, "out_amt_s_2");
      hh.out_amt_e[1]          =colNum(0, "out_amt_e_2");
      hh.out_rate[1]           =colNum(0, "out_rate_2");
      hh.out_amt_s[2]          =colNum(0, "out_amt_s_3");
      hh.out_amt_e[2]          =colNum(0, "out_amt_e_3");
      hh.out_rate[2]           =colNum(0, "out_rate_3");
      hh.out_amt_s[3]          =colNum(0, "out_amt_s_4");
      hh.out_amt_e[3]          =colNum(0, "out_amt_e_4");
      hh.out_rate[3]           =colNum(0, "out_rate_4");
      hh.out_amt_s[4]          =colNum(0, "out_amt_s_5");
      hh.out_amt_e[4]          =colNum(0, "out_amt_e_5");
      hh.out_rate[4]           =colNum(0, "out_rate_5");
      hh.acctCode_bl           =colEq(0, "item_ename_bl","1");
      hh.acctCode_bl_in        =colEq(0, "item_ename_bl_in","1");
      hh.acctCode_bl_out       =colEq(0, "item_ename_bl_out","1");
      hh.acctCode_it           =colEq(0, "item_ename_it","1");
      hh.acctCode_it_in        =colEq(0, "item_ename_it_in","1");
      hh.acctCode_it_out       =colEq(0, "item_ename_it_out","1");
      hh.acctCode_ca           =colEq(0, "item_ename_ca","1");
      hh.acctCode_id           =colEq(0, "item_ename_id","1");
      hh.acctCode_ao           =colEq(0, "item_ename_ao","1");
      hh.acctCode_ot           =colEq(0, "item_ename_ot","1");
      hh.present_type         =colSs(0, "present_type");
      hh.rate_a2[0]             =colNum(0, "rate_a12");
      hh.rate_a2[1]             =colNum(0, "rate_a22");
      hh.rate_a2[2]             =colNum(0, "rate_a32");
      hh.rate_a2[3]             =colNum(0, "rate_a42");
      hh.rate_a2[4]             =colNum(0, "rate_a52");
      hh.int_rate_2[0]          =colNum(0, "int_rate_12");
      hh.int_rate_2[1]          =colNum(0, "int_rate_22");
      hh.int_rate_2[2]          =colNum(0, "int_rate_32");
      hh.int_rate_2[3]          =colNum(0, "int_rate_42");
      hh.int_rate_2[4]          =colNum(0, "int_rate_52");
      hh.out_rate_2[0]          =colNum(0, "out_rate_12");
      hh.out_rate_2[1]          =colNum(0, "out_rate_22");
      hh.out_rate_2[2]          =colNum(0, "out_rate_32");
      hh.out_rate_2[3]          =colNum(0, "out_rate_42");
      hh.out_rate_2[4]          =colNum(0, "out_rate_52");
      hh.program_code         =colSs(0, "program_code");
      hh.purch_date_type      =colSs(0, "purch_date_type");
      hh.run_time_dd          =colInt(0, "run_time_dd");

      //-check Run---
      if (ibForceRun) {
         //-指定group_code-
         if (noEmpty(isGroupCode)) {
            if (!eq(hh.group_code,isGroupCode)) continue;
         }
         //-指定執行日期-
         if (iiParmDD >0 && eq(hh.purch_date_type,"1")) {
            if (hh.run_time_dd !=iiParmDD) continue;
         }
      }
      else {
         //-已run-
         if (selectMkt_re_group(hh.group_code) >0) continue;
         if (eq(hh.purch_date_type,"1")) {
            if (hh.run_time_dd !=iiBusiDD) continue;
         }
      }
      //--
      deleteMktReGroup(hh.group_code);
      //-期間: 1.依消費日, 0.依關帳年月--
      if (eq(hh.purch_date_type,"1")) {
         hh.bill_acct_month="%";
         hh.bill_post_date =isAcctMonth+"%";
      }
      else {
         hh.bill_acct_month=isAcctMonth;
         hh.bill_post_date ="%";
      }
      printf(" --group_code[%s] acct_month[%s], post_date[%s]"
              , hh.group_code, hh.bill_acct_month, hh.bill_post_date);

      getMchtGroup(hh.group_code);
      //------------
      //1.按消費總金額--
      if (eq(hh.reward_type,"1")) {
         if (hh.ex_mcht_group>0 || hh.ex_mcht_no>0)
            selectBil_bill_mchtGp();
         else selectBil_bill();
         calcRewardAmt_tot();
      }
      else if (eq(hh.reward_type,"2")) {
         selectBil_bill_mchtIn();
         calcRewardAmt_inOut();
      }
      //-Pos回饋-
      if (hh.regp_term_rew_amount >0) {
         hh.regp_term_rew_amount =hh.regp_term_total_amt * 0.002;
         hh.regp_term_rew_amount =commString.numScale(hh.regp_term_rew_amount,0);
      }

      boolean lb_branch=commString.ssIn(hh.group_code,";1686;1688;1689;1690;1691;1692");
      hh.regp_mcht_group_id ="";  //""XXXX";
      insertMkt_re_group();
      //-回饋分社-
      //-->I:台北五信帳戶, 1686
      //-->J:花蓮第二信用合作社, 1688
      //-->K:台中第二信用合作, 1689
      //-->L:花蓮第一信用合作, 1690
      //-->M:台南第三信用合作社, 1691
      //-->N:嘉義第三信用合作, 1692
      if (lb_branch) {
         proc_Branch_bill();
      }
   }

   closeCursor(liCusr);
}
//-----------
void calcRewardAmt_tot() throws Exception {
   double lm_rewAmt=0;
   if (hh.acctCode_bl) lm_rewAmt +=hh.regp_bl_amt;
   if (hh.acctCode_it) lm_rewAmt +=hh.regp_it_amt;
   if (hh.acctCode_ca) lm_rewAmt +=hh.regp_ca_amt;
   if (hh.acctCode_id) lm_rewAmt +=hh.regp_id_amt;
   if (hh.acctCode_ao) lm_rewAmt +=hh.regp_ao_amt;
   if (hh.acctCode_ot) lm_rewAmt +=hh.regp_ot_amt;
   hh.regp_discount_amt =hh.base_amt_1;
   lm_rewAmt = lm_rewAmt - hh.base_amt_1;
   if (lm_rewAmt <=0) return;
   //-級距式-
   if (eq(hh.present_type,"2")) {
      for (int ii = 0; ii <5 ; ii++) {
         double amt1 =hh.purch_amt_s[ii] * 10000;
         double amt2 =hh.purch_amt_e[ii] * 10000;
         double tmpAmt=0;
         if (lm_rewAmt >amt1) {
            if (lm_rewAmt >=amt2) tmpAmt = amt2 - amt1;
            else tmpAmt =lm_rewAmt - amt1;
         }
         else continue;
         hh.regp_rew_amount +=(tmpAmt * hh.rate_a[ii]) /100;
         hh.regp_rew_amount2 +=(tmpAmt * hh.rate_a2[ii]) /100;
      }

   }
   else {
      //-條件式-
      for (int ii = 0; ii <5 ; ii++) {
         int kk=4-ii;
         double amt1 =hh.purch_amt_s[kk] * 10000;
         double amt2 =hh.purch_amt_e[kk] * 10000;
         if (lm_rewAmt >amt1 && lm_rewAmt <=amt2) {
            hh.regp_rew_amount =(lm_rewAmt * hh.rate_a[kk]) / 100.00;
            hh.regp_rew_amount2 =(lm_rewAmt * hh.rate_a2[kk]) / 100.00;
            break;
         }
      }
   }
   hh.regp_rew_amount =commString.numScale(hh.regp_rew_amount,0);
   hh.regp_rew_amount2 =commString.numScale(hh.regp_rew_amount2,0);
}
//------------------------
void calcRewardAmt_inOut() {
   double lm_inAmt=0;
   double lm_outAmt=0;
   if (hh.acctCode_bl) {
      if (hh.acctCode_bl_out) lm_outAmt +=hh.regp_bl_amt;
      if (hh.acctCode_bl_in) lm_inAmt +=hh.regp_bl_amt_in;
   }
   if (hh.acctCode_it) {
      if (hh.acctCode_it_out) lm_outAmt +=hh.regp_it_amt;
      if (hh.acctCode_it_in) lm_inAmt +=hh.regp_it_amt_in;
   }
   if (hh.acctCode_ca) lm_outAmt +=hh.regp_ca_amt;
   if (hh.acctCode_id) lm_outAmt +=hh.regp_id_amt;
   if (hh.acctCode_ao) lm_outAmt +=hh.regp_ao_amt;
   if (hh.acctCode_ot) lm_outAmt +=hh.regp_ot_amt;

   hh.regp_out_purch_amt =lm_outAmt;
   hh.regp_int_purch_amt =lm_inAmt;
   //-店內回饋--
   for (int ii = 4; ii >=0 ; ii--) {
      double amt1 =hh.int_amt_s[ii] * 10000;
      double amt2 =hh.int_amt_e[ii] * 10000;
      if (lm_inAmt >amt1 && lm_inAmt <=amt2) {
         hh.regp_int_amt +=(lm_inAmt * hh.int_rate[ii]) / 100.00;
         hh.regp_in_amt2 +=(lm_inAmt * hh.int_rate_2[ii]) / 100.00;
         break;
      }
   }
   hh.regp_int_amt =commString.numScale(hh.regp_int_amt,0);
   hh.regp_in_amt2 =commString.numScale(hh.regp_in_amt2,0);
   //-店外回饋--
   for (int ii = 4; ii >=0 ; ii--) {
      double amt1 =hh.out_amt_s[ii] * 10000;
      double amt2 =hh.out_amt_e[ii] * 10000;
      if (lm_outAmt >amt1 && lm_outAmt <=amt2) {
         hh.regp_out_amt +=(lm_outAmt * hh.out_rate[ii]) / 100.00;
         hh.regp_out_amt2 +=(lm_outAmt * hh.out_rate[ii]) / 100.00;
         break;
      }
   }
   hh.regp_out_amt =commString.numScale(hh.regp_out_amt,0);
   hh.regp_out_amt2 =commString.numScale(hh.regp_out_amt2,0);
   //------
   hh.regp_rew_amount =hh.regp_int_amt+hh.regp_out_amt;
   hh.regp_rew_amount2 =hh.regp_in_amt2+hh.regp_out_amt2;
}
//============================
int tiBranch=-1;
com.DataSet dsBranch=new DataSet();
void proc_Branch_bill() throws Exception {
   dsBranch.dataClear();
   if (tiBranch <=0) {
      sqlCmd ="select nvl(C.promote_dept,'') branch_id, A.acct_code "
              +", A.mcht_no, A.acq_member_id as acq_id"
              +", sum(decode(A.sign_flag,'-',0 - A.dest_amt, A.dest_amt)) as dest_amt"
              +", sum(decode(A.sign_flag,'-',0,1)) as dest_cnt"
              +" from bil_bill A join crd_card C on A.card_no=C.card_no"
              +" where 1=1"
              +" and A.acct_month like ? and A.post_date like ?"
              +" and A.group_code =?"
              +" and rsk_type not in ('1','2','3') "  //('','4')"
              +" and A.bill_type not in ('OKOL','OSSG')"
              +" and A.acct_code in ('BL','IT','CA','ID','AO','OT')"
              +" and ecs_cus_mcht_no not in ( "+isExpMchtSql+" )"
              +" group by nvl(C.promote_dept,''), A.acct_code, A.mcht_no, A.acq_member_id"
              +" order by nvl(C.promote_dept,''), A.acct_code"
      ;
      tiBranch =ppStmtCrt("tiBranch","");
   }
   ppp(1, hh.bill_acct_month);
   ppp(hh.bill_post_date);
   ppp(hh.group_code);
   sqlSelect(tiBranch);
   int llNrow =sqlNrow;
   for (int ll = 0; ll <llNrow ; ll++) {
      String ls_branch =colSs(ll,"branch_id");
      dsBranch_get(ls_branch);
      String ls_acctCode=colSs(ll,"acct_code");
      double lm_destAmt =colNum(ll, "dest_amt");
      if (lm_destAmt <=0) continue;
      int    li_destCnt =colInt(ll,"dest_cnt");
      String ls_mchtNo =colSs(ll,"mcht_no");
      String ls_acqId =colSs(ll,"acq_id");
      hh.exMchtFlag=0;
      hh.inOutFlag=0;
      //-店內外-
      if (eq(hh.reward_type,"2")) {
         if (hh.in_mcht_group >0) {
            selectMktPurcGp2(ls_mchtNo,ls_acqId);
            if (hh.inOutFlag ==1) {
               if (eq(ls_acctCode,"BL")) {
                  dsBranch_add("bl_amt_in",lm_destAmt);
                  dsBranch_add("bl_amt", 0 - lm_destAmt);
               }
               if (eq(ls_acctCode,"IT")) {
                  dsBranch_add("it_amt_in",lm_destAmt);
                  dsBranch_add("it_amt", 0 - lm_destAmt);
               }
            }
         }
      }
      //-消費總額-
      else {
         //-排除特店-
         if (hh.ex_mcht_group>0 ||hh.ex_mcht_no>0) {
            if (select_ptr_bn_data(ls_mchtNo,ls_acqId) >0) {
               dsBranch_add("exp_amt",lm_destAmt);
               continue;
            }
         }
      }
      //--
      dsBranch_add("purch_cnt",li_destCnt);
      branchTotAmt(ls_acctCode,lm_destAmt);
   }
   //--------------------
   calcRewardAmt_branch();
   for (int ll = 0; ll <dsBranch.rowCount() ; ll++) {
      dsBranch.listCurr(ll);
      insertMkt_re_group_2();
   }
   printf(" -- group_code[%s], branch.row[%s]", hh.group_code, dsBranch.rowCount());
}
//--------
void calcRewardAmt_branch() {
   double lm_gpInAmt=0, lm_gpOutAmt=0;
   boolean lb_rewType2 =eq(hh.reward_type,"2");

   if (hh.acctCode_ca) lm_gpOutAmt +=hh.regp_ca_amt;
   if (hh.acctCode_id) lm_gpOutAmt +=hh.regp_id_amt;
   if (hh.acctCode_ao) lm_gpOutAmt +=hh.regp_ao_amt;
   if (hh.acctCode_ot) lm_gpOutAmt +=hh.regp_ot_amt;

   if (lb_rewType2) {
      if (hh.acctCode_bl) {
         if (hh.acctCode_bl_out) lm_gpOutAmt +=hh.regp_bl_amt;
         if (hh.acctCode_bl_in) lm_gpInAmt +=hh.regp_bl_amt_in;
      }
      if (hh.acctCode_it) {
         if (hh.acctCode_it_out) lm_gpOutAmt +=hh.regp_it_amt;
         if (hh.acctCode_it_in) lm_gpInAmt +=hh.regp_it_amt_in;
      }
   }
   else {
      if (hh.acctCode_bl) lm_gpOutAmt +=hh.regp_bl_amt;
      if (hh.acctCode_it) lm_gpOutAmt +=hh.regp_it_amt;
   }

   double[] remAmt= {0,0};
   double[] remAmtOut={0,0}, remAmtIn={0,0};
   for (int ii = 0; ii <dsBranch.rowCount() ; ii++) {
      dsBranch.listCurr(ii);
      double lm_outAmt=0, lm_inAmt=0;
      double lm_amt1=0, lm_amt2=0;
      if (hh.acctCode_ca) lm_outAmt +=dsBranch.colNum("ca_amt");
      if (hh.acctCode_id) lm_outAmt +=dsBranch.colNum("id_amt");
      if (hh.acctCode_ao) lm_outAmt +=dsBranch.colNum("ao_amt");
      if (hh.acctCode_ot) lm_outAmt +=dsBranch.colNum("ot_amt");
      if (lb_rewType2) {
         if (hh.acctCode_bl) {
            if (hh.acctCode_bl_out) lm_outAmt +=dsBranch.colNum("bl_amt");
            if (hh.acctCode_bl_in) lm_inAmt +=dsBranch.colNum("bl_amt_in");
         }
         if (hh.acctCode_it) {
            if (hh.acctCode_it_out) lm_outAmt +=dsBranch.colNum("it_amt");
            if (hh.acctCode_it_in) lm_inAmt +=dsBranch.colNum("it_amt_in");
         }
         dsBranch_add("out_purch_amt", lm_outAmt);
         dsBranch_add("int_purch_amt", lm_inAmt);

         double inAmt[]={0,0};
         double outAmt[]={0,0};
         if (lm_gpOutAmt >0) {
            outAmt[0] =hh.regp_out_amt * lm_outAmt / lm_gpOutAmt;
            outAmt[1] =hh.regp_out_amt2 * lm_outAmt / lm_gpOutAmt;
         }
         if (lm_gpInAmt >0) {
            inAmt[0] =hh.regp_int_amt * lm_inAmt / lm_gpInAmt;
            inAmt[1] =hh.regp_in_amt2 * lm_inAmt / lm_gpInAmt;
         }
         outAmt[0] =commString.round(outAmt[0],0);
         outAmt[1] =commString.round(outAmt[1],0);
         inAmt[0] =commString.round(inAmt[0],0);
         inAmt[1] =commString.round(inAmt[1],0);
         dsBranch_add("out_amt",outAmt[0]);
         dsBranch_add("out_amt2",outAmt[1]);
         dsBranch_add("in_amt",inAmt[0]);
         dsBranch_add("in_amt2",inAmt[1]);
         lm_amt1 =outAmt[0]+inAmt[0];
         lm_amt2 =outAmt[1]+inAmt[1];
         remAmtOut[0] +=outAmt[0];
         remAmtOut[1] +=outAmt[1];
         remAmtIn[0] +=inAmt[0];
         remAmtIn[1] +=inAmt[1];
      }
      else {
         if (hh.acctCode_bl) lm_outAmt +=dsBranch.colNum("bl_amt");
         if (hh.acctCode_it) lm_outAmt +=dsBranch.colNum("it_amt");
         if (lm_gpOutAmt >0) {
            lm_amt1 =hh.regp_rew_amount * (lm_outAmt / lm_gpOutAmt);
            lm_amt2 =hh.regp_rew_amount2 * (lm_outAmt / lm_gpOutAmt);
         }
         lm_amt1 =commString.round(lm_amt1,0);
         lm_amt2 =commString.round(lm_amt2,0);
      }
      dsBranch_add("rew_amount",lm_amt1);
      dsBranch_add("rew_amount2",lm_amt2);
      remAmt[0] +=lm_amt1;
      remAmt[1] +=lm_amt2;
   }
   if (dsBranch.rowCount() <=0) return;

   //-剩餘回饋金額:加到第1分社----
   dsBranch.listCurr(0);
   remAmtOut[0] =hh.regp_out_amt - remAmtOut[0];
   remAmtOut[1] =hh.regp_out_amt2 - remAmtOut[1];
   dsBranch_add("out_amt",remAmtOut[0]);
   dsBranch_add("out_amt2",remAmtOut[1]);
   remAmtIn[0] =hh.regp_int_amt - remAmtIn[0];
   remAmtIn[1] =hh.regp_in_amt2 - remAmtIn[1];
   dsBranch_add("in_amt",remAmtIn[0]);
   dsBranch_add("in_amt2",remAmtIn[1]);
   remAmt[0] =hh.regp_rew_amount - remAmt[0];
   remAmt[1] =hh.regp_rew_amount2 - remAmt[1];
   dsBranch_add("rew_amount",remAmt[0]);
   dsBranch_add("rew_amount2",remAmt[1]);

}
//---------
void dsBranch_add(String col, double num1) {
   double lm_num =dsBranch.colNum(col)+num1;
   dsBranch.colSet(col,lm_num);
}
int dsBranch_get(String ls_branch) {
   for (int ll = 0; ll <dsBranch.rowCount() ; ll++) {
      dsBranch.listCurr(ll);
      if (dsBranch.colEq("branch_id",ls_branch)) {
         return dsBranch.getCurrRow();
      }
   }
   dsBranch.addrow();
   dsBranch.colSet("branch_id",ls_branch);
   return dsBranch.getCurrRow();
   //   ttAregp.aaa("total_purch_amt"  ::purch_amt);   //-總消費筆數--
   //   ttAregp.aaa("total_purch_cnt"  ::purch_cnt);   //-總消費額--
   //   ttAregp.aaa("out_purch_amt"    ::out_purch_amt);   //-店外消費金額--
   //   ttAregp.aaa("out_amt"          ::out_amt);   //-店外提撥金額--
   //   ttAregp.aaa("out_amt2"         ::out_amt2);   //-店外提撥金額2--
   //   ttAregp.aaa("int_purch_amt"    ::int_purch_amt);   //-店內消費金額--
   //   ttAregp.aaa("int_amt"          ::int_amt);   //-店內提撥金額--
   //   ttAregp.aaa("in_amt2"          ::in_amt2);   //-店內提撥金額2--
   //   ttAregp.aaa("rew_amount"       ::rew_amount);   //-回饋金額--
   //   ttAregp.aaa("rew_amount2"      ::rew_amount2);   //-回饋金額--
   //   ttAregp.aaa("bl_amt"                    , hh.regp_bl_amt);   //-簽帳款--
   //   ttAregp.aaa("bl_amt_in"                 , hh.regp_bl_amt_in);   //-簽帳款店內--
   //   ttAregp.aaa("it_amt"                    , hh.regp_it_amt);   //-分期付款--
   //   ttAregp.aaa("it_amt_in"                 , hh.regp_it_amt_in);   //-分期付款店內--
   //   ttAregp.aaa("ca_amt"                    , hh.regp_ca_amt);   //-預借現金--
   //   ttAregp.aaa("id_amt"                    , hh.regp_id_amt);   //-代收款--
   //   ttAregp.aaa("ao_amt"                    , hh.regp_ao_amt);   //-餘額代償--
   //   ttAregp.aaa("ot_amt"                    , hh.regp_ot_amt);   //-其他應收款--
}
//-------------------------
com.DataSet dsMcht =new DataSet();
void loadPtr_bn_data() throws Exception {
   sqlCmd ="SELECT group_code" +
           ", sum(decode(data_type,'1',1,0)) AS ex_mcht_group" +
           ", sum(decode(data_type,'2',1,0)) AS ex_mcht_no" +
           ", sum(decode(data_type,'3',1,0)) AS in_mcht_group" +
           ", sum(decode(data_type,'5',1,0)) AS in_mcht_pos" +
           " FROM (" +
           " SELECT substr(program_code,10) AS group_code, data_type, data_code " +
           " FROM ptr_bn_data" +
           " WHERE 1=1" +
           " AND lower(program_code) LIKE '%mktm0210%'" +
           " UNION " +
           " SELECT group_code, '3', mcht_group_id AS data_code " +
           " FROM mkt_purcgp_ext" +
           " )" +
           " WHERE 1=1 " +
           " GROUP BY group_code"
           ;
   sqlQuery(dsMcht,"",null);
}
//---------
void getMchtGroup(String aGroupCode) throws Exception {
   for (int ll = 0; ll <dsMcht.rowCount() ; ll++) {
      dsMcht.listCurr(ll);
      if (dsMcht.colEq("group_code", aGroupCode)) {
         hh.ex_mcht_no =dsMcht.colInt("ex_mcht_no");
         hh.ex_mcht_group =dsMcht.colInt("ex_mcht_group");
         hh.in_mcht_group =dsMcht.colInt("in_mcht_group");
         hh.in_mcht_pos =dsMcht.colInt("in_mcht_pos");
         break;
      }
   }
}
//--------------
int tiRegp=-1;
int selectMkt_re_group(String a_groupCode) throws Exception {
   if (tiRegp <=0) {
      sqlCmd ="select count(*) regp_cnt"
              +" from mkt_re_group"
              +" where reward_type ='2' "
              +" and rew_yyyymm =?"
              +" and group_code =?"
              ;
      tiRegp =ppStmtCrt("ti-regp","");
   }
   ppp(1, isAcctMonth);
   ppp(a_groupCode);
   sqlSelect(tiRegp);
   if (sqlNrow <=0) return 0;

   return colInt("regp_cnt");
}
//==================================
int tiBill=-1;
void selectBil_bill() throws Exception {
   if (tiBill <=0) {
      sqlCmd ="select acct_code "
              +", sum(decode(sign_flag,'-',0 - dest_amt, dest_amt)) as dest_amt"
              +", sum(decode(sign_flag,'-',0,1)) as dest_cnt"
              +" from bil_bill"
              +" where 1=1"
              +" and acct_month like ? and post_date like ?"
              +" and group_code =?"
              +" and rsk_type not in ('1','2','3') "  //('','4')"
              +" and bill_type not in ('OKOL','OSSG') "
              +" and acct_code in ('BL','IT','CA','ID','AO','OT') "
              +" and ecs_cus_mcht_no not in ( "+isExpMchtSql+" ) "
              +" group by acct_code"
      ;
      tiBill =ppStmtCrt("tiBill","");
   }
   ppp(1, hh.bill_acct_month);
   ppp(hh.bill_post_date);
   ppp(hh.group_code);
   sqlSelect(tiBill);
   int llNrow =sqlNrow;
   for (int ll = 0; ll <llNrow ; ll++) {
      String ls_acctCode=colSs(ll,"acct_code");
      double lm_destAmt =colNum(ll, "dest_amt");
      if (lm_destAmt <=0) continue;
      int    li_destCnt =colInt(ll,"dest_cnt");
      hh.regp_total_purch_cnt +=li_destCnt;
      acctCodeTotAmt(ls_acctCode,lm_destAmt);
   }
}
//------
void branchTotAmt(String ls_acctCode, double lm_destAmt) {
   dsBranch_add("purch_amt",lm_destAmt);
   if (eq(ls_acctCode,"BL")) dsBranch_add("bl_amt",lm_destAmt);
   else if (eq(ls_acctCode,"IT")) dsBranch_add("it_amt",lm_destAmt);
   else if (eq(ls_acctCode,"CA")) dsBranch_add("ca_amt",lm_destAmt);
   else if (eq(ls_acctCode,"ID")) dsBranch_add("id_amt",lm_destAmt);
   else if (eq(ls_acctCode,"AO")) dsBranch_add("ao_amt",lm_destAmt);
   else if (eq(ls_acctCode,"OT")) dsBranch_add("ot_amt",lm_destAmt);
}
//------
void acctCodeTotAmt(String ls_acctCode, double lm_destAmt) {
   hh.regp_total_purch_amt +=lm_destAmt;
   if (eq(ls_acctCode,"BL")) hh.regp_bl_amt +=lm_destAmt;
   else if (eq(ls_acctCode,"IT")) hh.regp_it_amt +=lm_destAmt;
   else if (eq(ls_acctCode,"CA")) hh.regp_ca_amt +=lm_destAmt;
   else if (eq(ls_acctCode,"ID")) hh.regp_id_amt +=lm_destAmt;
   else if (eq(ls_acctCode,"AO")) hh.regp_ao_amt +=lm_destAmt;
   else if (eq(ls_acctCode,"OT")) hh.regp_ot_amt +=lm_destAmt;
}
//-------------------
int tiMchtgp=-1;
void selectBil_bill_mchtGp() throws Exception {
   //-排除特店群組---
   if (tiMchtgp <=0) {
      sqlCmd ="select acct_code, mcht_no, acq_member_id as acq_id "
              +", sum(decode(sign_flag,'-',0 - dest_amt, dest_amt)) as dest_amt"
              +", sum(decode(sign_flag,'-',0,1)) as dest_cnt"
              +" from bil_bill"
              +" where 1=1"
              +" and acct_month like ? and post_date like ?"
              +" and group_code =?"
              +" and rsk_type not in ('1','2','3') "  //('','4')"
              +" and bill_type not in ('OKOL','OSSG')"
              +" and acct_code in ('BL','IT','CA','ID','AO','OT')"
              +" and ecs_cus_mcht_no not in ( "+isExpMchtSql+" )"
              +" group by acct_code, mcht_no, acq_member_id"
      ;
      tiMchtgp =ppStmtCrt("tiMchtgp","");
   }
   ppp(1, hh.bill_acct_month);
   ppp(hh.bill_post_date);
   ppp(hh.group_code);

   sqlSelect(tiMchtgp);
   int llNrow =sqlNrow;
   for (int ll = 0; ll <llNrow ; ll++) {
      String ls_acctCode=colSs(ll,"acct_code");
      String ls_mchtNo =colSs(ll,"mcht_no");
      String ls_acqId =colSs(ll,"acq_id");
      double lm_destAmt =colNum(ll, "dest_amt");
      int    li_destCnt =colInt(ll,"dest_cnt");
      //-排除特店-
      select_ptr_bn_data(ls_mchtNo,ls_acqId);
      if ( hh.exMchtFlag >0) {
         hh.regp_exp_tax_amt +=lm_destAmt;
         continue;
      }
      hh.regp_total_purch_cnt +=li_destCnt;
      acctCodeTotAmt(ls_acctCode,lm_destAmt);
   }
}
//-----------------
int tiMchtIn =-1;
void selectBil_bill_mchtIn() throws Exception {
   //-排除特店群組---
   if (tiMchtIn <=0) {
      sqlCmd ="select acct_code, mcht_no, acq_member_id as acq_id "
              +", terminal_id as term_id"
              +", sum(decode(sign_flag,'-',0 - dest_amt, dest_amt)) as dest_amt"
              +", sum(decode(sign_flag,'-',0,1)) as dest_cnt"
              +" from bil_bill"
              +" where 1=1"
              +" and acct_month like ? and post_date like ?"
              +" and group_code =?"
              +" and rsk_type not in ('1','2','3') "  //('','4')"
              +" and bill_type not in ('OKOL','OSSG')"
              +" and acct_code in ('BL','IT','CA','ID','AO','OT')"
              +" and ecs_cus_mcht_no not in ( "+isExpMchtSql+" )"
              +" group by acct_code, mcht_no, acq_member_id, terminal_id"
      ;
      tiMchtIn =ppStmtCrt("tiMchtIn","");
   }
   ppp(1, hh.bill_acct_month);
   ppp(hh.bill_post_date);
   ppp(hh.group_code);

   sqlSelect(tiMchtIn);
   int llNrow =sqlNrow;
   for (int ll = 0; ll <llNrow ; ll++) {
      String ls_acctCode=colSs(ll,"acct_code");
      String ls_mchtNo =colSs(ll,"mcht_no");
      String ls_acqId =colSs(ll,"acq_id");
      String ls_termId =colSs(ll, "term_id");
      double lm_destAmt =colNum(ll, "dest_amt");
      int    li_destCnt =colInt(ll,"dest_cnt");
      hh.regp_total_purch_cnt +=li_destCnt;
      acctCodeTotAmt(ls_acctCode,lm_destAmt);
      //---
      if (commString.ssIn(ls_acctCode,",BL,IT")) {
         //-店內外-
         hh.inOutFlag=0;
         if (hh.in_mcht_group >0) {
            selectMktPurcGp2(ls_mchtNo,ls_acqId);
            if (hh.inOutFlag ==1) {
               if (eq(ls_acctCode,"BL")) {
                  hh.regp_bl_amt_in +=lm_destAmt;
                  hh.regp_bl_amt =hh.regp_bl_amt - lm_destAmt;
               }
               if (eq(ls_acctCode,"IT")) {
                  hh.regp_it_amt_in +=lm_destAmt;
                  hh.regp_it_amt =hh.regp_it_amt - lm_destAmt;
               }
            }
         }
         //-指定終端機-
         hh.posFlag =0;
         if (hh.in_mcht_pos>0) {
            selectPosNo(ls_termId);
            if (hh.posFlag >0) {
               hh.regp_term_total_amt +=lm_destAmt;
               hh.regp_term_total_cnt +=li_destCnt;
            }
         }
         //--
      }
   }  //-for-

}
//-----------------------
int tiBndata=-1;
int select_ptr_bn_data(String ls_mchtNo, String ls_acqId) throws Exception {
   hh.exMchtFlag =0;
   if (hh.ex_mcht_group <=0 && hh.ex_mcht_no <=0) return 0;

//1.存在, 0.不存在--改成特店群組,特店代號共存--
   if (tiBndata <=0) {
      sqlCmd = "select 1 as bndata_cnt"
              +" from ptr_bn_data "
              +" where program_code = ? "
              +" and ( (data_type = '2' and data_code = ? )"  // 2:特店代號明細   4:特店群組代號明細
              +" or ( data_type = '1' and data_code in ("
              +" select data_key from mkt_mchtgp_data where table_name = 'MKT_MCHT_GP'"
              +" and data_code =? and data_code2 in ('',?) )"
              +" ) "+commSqlStr.rownum(1)
      ;
      tiBndata =ppStmtCrt("tiBndata","");
   }

   ppp(1, "mktm0210_"+hh.group_code);
   ppp(ls_mchtNo);
   ppp(ls_mchtNo);
   ppp(ls_acqId);

   sqlSelect(tiBndata);
   if (sqlNrow >0) {
      return 1;
   }
   return 0;
}
//---------------
int tiPurcgp2=-1;
void selectMktPurcGp2(String ls_mchtNo, String ls_acqId) throws Exception {
//   regpCnt = 0;
//   String tempGroupCode = "";
//   String tempMchtGroupId = "";
//
//   tempGroupCode  = hBillGroupCode;
   hh.inOutFlag =0; //店外
   if (empty(ls_mchtNo))
      return;

   /* 判斷是否為店內 */
   /* 條件1: 特店代號是否在mkt_mchtgp_data內 */
   /* 條件2: group_code是否在mkt_purcgp_ext內 */
   if (tiPurcgp2 <=0) {
      sqlCmd = "select data_key from mkt_mchtgp_data "
              +" where data_code = ? "
              +" and data_code2 in ('',?)"
              +" and data_key in ( "
              +" select mcht_group_id from mkt_purcgp_ext"
              +" where group_code = ? ) "
              +" and table_name ='MKT_MCHT_GP' and data_type='1'"
      ;
      tiPurcgp2 =ppStmtCrt("tiPurcgp2","");
   }
   ppp(1, ls_mchtNo);
   ppp(ls_acqId);
   ppp(hh.group_code);
   sqlSelect(tiPurcgp2);
   if (sqlNrow >0) {
      hh.inOutFlag =1;
   }

   return;
}
//---
int tiPos=-1;
void selectPosNo(String ls_termId) throws Exception {
   hh.posFlag=0;
   if (empty(ls_termId)) return;
   if (tiPos <=0) {
      sqlCmd =" select 1 "
              +" from ptr_bn_data "+
              " where program_code = ?"+
              " and data_type = '5' "+
              " and data_code = ? ";
      tiPos =ppStmtCrt("tiPos","");
   }
   //mktm0210_1630	5	10303221
   ppp(1, "mktm0210_"+hh.group_code);
   ppp(ls_termId);
   sqlSelect(tiPos);
   if (sqlNrow <=0) return;

   hh.posFlag=1;
}
//================================
int tiDregp=-1;
void deleteMktReGroup(String a_groupCode) throws Exception {
   if (tiDregp <=0) {
      sqlCmd = "delete mkt_re_group"
              +" where rew_yyyymm = ? "
              +" and reward_type = '2' "
              +" and group_code like ?"
      ;
      tiDregp =ppStmtCrt("ti-D-regp","");
   }
   //purch_date_type     	//-x(1)  活動期間類別
//           +" and (decode(purch_date_type, '', '0', purch_date_type) = decode(cast(? as varchar(8)),'Y','x','0') "
//           +"   or group_code = decode(cast(? as varchar(8)),'N','x',cast(? as varchar(8)))) ";
   /* purchase_flag = Y  當月重新執行 , 須下團代參數  */
   ppp(1, isAcctMonth);
   ppp(2, a_groupCode+"%");
   sqlExec(tiDregp);
}
//--------------------
com.Parm2sql ttAregp=null;
void insertMkt_re_group() throws Exception {
   if (ttAregp ==null) {
      ttAregp =new com.Parm2sql();
      ttAregp.insert("mkt_re_group");
   }
   double lm_avgAmt =0;
   if (hh.regp_total_purch_cnt >0) {
      lm_avgAmt =hh.regp_total_purch_amt / hh.regp_total_purch_cnt;
      lm_avgAmt =commString.round(lm_avgAmt,0);
   }
   ttAregp.aaa("group_code"                , hh.group_code);   //-團體代號         請參考ptr_group_code--
   ttAregp.aaa("mcht_group_id"             , hh.regp_mcht_group_id);   //-特店群組代號--
   ttAregp.aaa("rew_yyyymm"                , isAcctMonth);   //-回饋年月         YYYYMM--
   ttAregp.aaa("reward_type"               , "2");   //-回饋類別         1:  年費回饋     2:  消費回饋--
   ttAregp.aaa("total_purch_amt"           , hh.regp_total_purch_amt);   //-總消費筆數--
   ttAregp.aaa("total_purch_cnt"           , hh.regp_total_purch_cnt);   //-總消費額--
   ttAregp.aaa("avg_purch_amt"             , lm_avgAmt);   //-平均消費額--
   ttAregp.aaa("discount_amt"              , hh.regp_discount_amt);   //-需扣除之例外金額--
//   ttAregp.aaa("reward_amt"                , hh.regp_reward_amt);   //-拉卡獎金--
   ttAregp.aaa("out_purch_amt"             , hh.regp_out_purch_amt);   //-店外消費金額--
   ttAregp.aaa("out_amt"                   , hh.regp_out_amt);   //-店外提撥金額--
   ttAregp.aaa("out_amt2"                  , hh.regp_out_amt2);   //-店外提撥金額2--
   ttAregp.aaa("int_purch_amt"             , hh.regp_int_purch_amt);   //-店內消費金額--
   ttAregp.aaa("int_amt"                   , hh.regp_int_amt);   //-店內提撥金額--
   ttAregp.aaa("in_amt2"                   , hh.regp_in_amt2);   //-店內提撥金額2--
   ttAregp.aaa("rew_amount"                , hh.regp_rew_amount);   //-回饋金額--
   ttAregp.aaa("rew_amount2"               , hh.regp_rew_amount2);   //-回饋金額--
   ttAregp.aaa("bl_amt"                    , hh.regp_bl_amt);   //-簽帳款--
   ttAregp.aaa("bl_amt_in"                 , hh.regp_bl_amt_in);   //-簽帳款店內--
   ttAregp.aaa("it_amt"                    , hh.regp_it_amt);   //-分期付款--
   ttAregp.aaa("it_amt_in"                 , hh.regp_it_amt_in);   //-分期付款店內--
   ttAregp.aaa("ca_amt"                    , hh.regp_ca_amt);   //-預借現金--
   ttAregp.aaa("id_amt"                    , hh.regp_id_amt);   //-代收款--
   ttAregp.aaa("ao_amt"                    , hh.regp_ao_amt);   //-餘額代償--
   ttAregp.aaa("ot_amt"                    , hh.regp_ot_amt);   //-其他應收款--
   ttAregp.aaa("exp_tax_amt"               , hh.regp_exp_tax_amt);   //-排除特店消費款--
   ttAregp.aaa("purch_date_type"           , hh.purch_date_type);   //-活動期間類別--
   ttAregp.aaa("term_rew_amount"           , hh.regp_term_rew_amount);   //-終端機回饋金額--
   ttAregp.aaa("term_total_cnt"            , hh.regp_term_total_cnt);   //-終端機消費筆數--
   ttAregp.aaa("term_total_amt"            , hh.regp_term_total_amt);   //-終端機消費金額--
   ttAregp.aaa("card_note"                 , hh.regp_card_note);   //-卡片等級--
//   ttAregp.aaa("crt_user"                  , hh.crt_user);   //-建檔人員--
//   ttAregp.aaa("crt_date"                  , hh.crt_date);   //-建檔日期--
   ttAregp.aaa("apr_flag"                  , "Y");   //-主管覆核--
//   ttAregp.aaa("apr_user"                  , hh.apr_user);   //-覆核人員--
//   ttAregp.aaa("apr_date"                  , hh.apr_date);   //-覆核日期--
   ttAregp.aaa("mod_user"                  , hModUser);   //-異動使用者--
   ttAregp.aaaDtime("mod_time");   //-異動時間--
   ttAregp.aaa("mod_pgm"                   , hModPgm);   //-異動程式--
   ttAregp.aaa("mod_seqno"                 , 1);   //-異動註記--

   if (ttAregp.ti <=0) {
      ttAregp.ti =ppStmtCrt("ttAregp", ttAregp.getSql());
   }

   sqlExec(ttAregp.ti, ttAregp.getParms());
   if (sqlNrow <=0) {
      if (eq(dupRecord,"Y")) {
         printf("insertMkt_re_group.dupl, kk[%s,%s]", hh.group_code,hh.regp_mcht_group_id);
      }
      else {
         printf("insertMkt_re_group.dupl, kk[%s,%s]", hh.group_code,hh.regp_mcht_group_id );
         errExit(0);
      }
   }
}
//--------------------
com.Parm2sql ttArebrch =null;
void insertMkt_re_group_2() throws Exception {
   if (ttArebrch ==null) {
      ttArebrch =new com.Parm2sql();
      ttArebrch.insert("mkt_re_group");
   }
   String ls_branch =dsBranch.colSs("branch_id");
   if (empty(ls_branch)) {
      ls_branch ="ZZZ";   //24-0104--
   }
   double lm_avgPurcAmt =0;
   if (dsBranch.colNum("purch_cnt")>0) {
      lm_avgPurcAmt =dsBranch.colNum("purch_amt") / dsBranch.colNum("purch_cnt");
   }

   ttArebrch.aaa("group_code"                , hh.group_code);   //-團體代號         請參考ptr_group_code--
   ttArebrch.aaa("mcht_group_id"             , ls_branch);   //-特店群組代號--
   ttArebrch.aaa("rew_yyyymm"                , isAcctMonth);   //-回饋年月         YYYYMM--
   ttArebrch.aaa("reward_type"               , "2");   //-回饋類別         1:  年費回饋     2:  消費回饋--
   ttArebrch.aaa("total_purch_amt"           , dsBranch.colNum("purch_amt"));   //-總消費筆數--
   ttArebrch.aaa("total_purch_cnt"           , dsBranch.colNum("purch_cnt"));   //-總消費額--
   ttArebrch.aaa("avg_purch_amt"             , lm_avgPurcAmt);   //-平均消費額--
   ttArebrch.aaa("discount_amt"              , hh.regp_discount_amt);   //-需扣除之例外金額--
   ttArebrch.aaa("out_purch_amt"             , dsBranch.colNum("out_purch_amt"));   //-店外消費金額--
   ttArebrch.aaa("out_amt"                   , dsBranch.colNum("out_amt"));   //-店外提撥金額--
   ttArebrch.aaa("out_amt2"                  , dsBranch.colNum("out_amt2"));   //-店外提撥金額2--
   ttArebrch.aaa("int_purch_amt"             , dsBranch.colNum("int_purch_amt"));   //-店內消費金額--
   ttArebrch.aaa("int_amt"                   , dsBranch.colNum("int_amt"));   //-店內提撥金額--
   ttArebrch.aaa("in_amt2"                   , dsBranch.colNum("in_amt2"));   //-店內提撥金額2--
   ttArebrch.aaa("rew_amount"                , dsBranch.colNum("rew_amount"));   //-回饋金額--
   ttArebrch.aaa("rew_amount2"               , dsBranch.colNum("rew_amount2"));   //-回饋金額--
   ttArebrch.aaa("bl_amt"                    , dsBranch.colNum("bl_amt"));   //-簽帳款--
   ttArebrch.aaa("bl_amt_in"                 , dsBranch.colNum("bl_amt_in"));   //-簽帳款店內--
   ttArebrch.aaa("it_amt"                    , dsBranch.colNum("it_amt"));   //-分期付款--
   ttArebrch.aaa("it_amt_in"                 , dsBranch.colNum("it_amt_in"));   //-分期付款店內--
   ttArebrch.aaa("ca_amt"                    , dsBranch.colNum("ca_amt"));   //-預借現金--
   ttArebrch.aaa("id_amt"                    , dsBranch.colNum("id_amt"));   //-代收款--
   ttArebrch.aaa("ao_amt"                    , dsBranch.colNum("ao_amt"));   //-餘額代償--
   ttArebrch.aaa("ot_amt"                    , dsBranch.colNum("ot_amt"));   //-其他應收款--
   ttArebrch.aaa("exp_tax_amt"               , dsBranch.colNum("exp_amt"));   //-排除特店消費款--
   ttArebrch.aaa("purch_date_type"           , hh.purch_date_type);   //-活動期間類別--
   ttArebrch.aaa("term_rew_amount"           , hh.regp_term_rew_amount);   //-終端機回饋金額--
   ttArebrch.aaa("term_total_cnt"            , hh.regp_term_total_cnt);   //-終端機消費筆數--
   ttArebrch.aaa("term_total_amt"            , hh.regp_term_total_amt);   //-終端機消費金額--
   ttArebrch.aaa("card_note"                 , hh.regp_card_note);   //-卡片等級--
//   ttAregp.aaa("crt_user"                  , hh.crt_user);   //-建檔人員--
//   ttAregp.aaa("crt_date"                  , hh.crt_date);   //-建檔日期--
   ttArebrch.aaa("apr_flag"                  , "Y");   //-主管覆核--
//   ttAregp.aaa("apr_user"                  , hh.apr_user);   //-覆核人員--
//   ttAregp.aaa("apr_date"                  , hh.apr_date);   //-覆核日期--
   ttArebrch.aaa("mod_user"                  , hModUser);   //-異動使用者--
   ttArebrch.aaaDtime("mod_time");   //-異動時間--
   ttArebrch.aaa("mod_pgm"                   , hModPgm);   //-異動程式--
   ttArebrch.aaa("mod_seqno"                 , 1);   //-異動註記--

   if (ttArebrch.ti <=0) {
      ttArebrch.ti =ppStmtCrt("ttArebrch", ttArebrch.getSql());
   }

   sqlExec(ttArebrch.ti, ttArebrch.getParms());
   if (sqlNrow <=0 ) {
      if (eq(dupRecord,"Y")) {
         printf("insertMkt_re_group_2.dupl; kk=[%s,%s]",hh.group_code,ls_branch);
      }
      else {
         sqlerr("insertMkt_re_group_2.error; kk[%s,%s]",hh.group_code,ls_branch);
         errExit(0);
      }
   }
}
}
