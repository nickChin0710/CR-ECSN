package rdsm01;
/**	道路救援服務元件 V.2019-1227.Alex
 * 2023-0502   JH    ++rds_pcard
 * 2023-0413.00   JH    排除消費(特店)
 * 2019-1210  V1.00.01  Alex  bug fix
 * 2019-1227  V1.00.02  Alex  fix bil amt
 * 2020-0113  V1.00.03  Ru    SQL bug fix
 * */


public class RdsFunc extends busi.FuncBase {
taroko.base.CommDate commDate = new taroko.base.CommDate();

public String lsCardNo = "";
public String lsCarNo = "";
public String lsChType = "";
public String lsIdPSeqno = "";
public String lsPSeqno = "";
public String lsProjNo = "";
public String lsRmType = "";
public int idcPurchAmt = 0;
public int ilPurchCnt = 0;
public int idcLastAmt = 0;
//public String msg = "";
public int lsRmPayamt = 0;
public String lsModPgm = "";

String acctType = "";
String cardType = "";
String groupCode = "";
String corpNo = "";
String currentCode = "";

String outstadningCond;

busi.DataSet idsParm = new busi.DataSet();

public String getRdsPcard(String aCardNo) {
   if (empty(aCardNo)) return "";
   
   String sql1 = "select A.group_code, B.rds_pcard"
   +" from crd_card A left join ptr_card_type B on A.card_type=B.card_type"
   +" where A.card_no =?"
   ;
   
   sqlSelect(sql1, aCardNo);
   if (sqlNotfind) {
     return "";
   }
   if (colEq("group_code","1622")) return "V";
   return colStr("rds_pcard");
}

void selectCrdCard() {
   // -read crd_card-
   String sql1 = " select  acct_type ,  card_type ,  group_code , "
                         + " uf_corp_no(corp_p_seqno) as corp_no ,  id_p_seqno ,  current_code ,  p_seqno "
                         + " from crd_card "
                         + " where card_no = ? ";
   sqlSelect(sql1, new Object[]{lsCardNo});
   if (sqlRowNum <= 0) {
      errmsg("select crd_card err, card_no=" + lsCardNo);
      return;
   }
   
   acctType = colStr("acct_type");
   cardType = colStr("card_type");
   groupCode = colStr("group_code");
   corpNo = colStr("corp_no");
   lsIdPSeqno = colStr("id_p_seqno");
   currentCode = colStr("current_code");
   lsPSeqno = colStr("p_seqno");
   
   if (!eqIgno(currentCode, "0")) {
      errmsg("卡片 為無效卡");
   }
}

void selectRdsRoadparm2() {
   StringBuilder sb = new StringBuilder();
   sb.append(" select  * ");
   sb.append(" from cms_roadparm2 ");
   sb.append(" where 1=1 and proj_no in ");
   sb.append(" ( select distinct proj_no from cms_roadparm2_dtl ");
   sb.append(" where acct_type =? and card_type = ? ");
   sb.append(" and uf_nvl(valid_end_date,'99991231') >= to_char(sysdate,'yyyymmdd') ");
   sb.append(" and nvl(apr_flag,'N') = 'Y' ");
   setString2(1, acctType);
   setString(cardType);
   if (notEmpty(groupCode) && eq(groupCode, "0000") == false) {
      sb.append(" and decode(group_code,'',?,group_code) = ? ");
      setString(groupCode);
      setString(groupCode);
   }
   if (notEmpty(corpNo)) {
      sb.append(" and decode(corp_no,'',?,group_code) = ? ");
      setString(corpNo);
      setString(corpNo);
   }
   sb.append(" )");
   
   idsParm.colList = sqlQuery(sb.toString());
}

public int chkAcctamtFree(String asCardNo, String asYear) {
   // --check 免費道路救援資格--
   // --Return: 0 -> no process
   // -- 1 -> 符合
   // -- 2 -> 不符合
   // -- -1 -> error
   // ------------------------------------------------------------------------
   if (empty(asCardNo))
      return 0;
   if (empty(asYear))
      return 0;
   String lsPseqno = "", lsVal1 = "", lsVal2 = "";
   int ldcAmt1 = 0, ldcAmt2 = 0, ldcAmt3 = 0, ldcAmt4 = 0;
   // --get P_SEQNO--
   String sql1 =
           " select  nvl(p_seqno,'') as ls_p_seqno  from crd_card  where card_no = ? ";
   sqlSelect(sql1, new Object[]{asCardNo});
   if (sqlRowNum <= 0 || empty(colStr("ls_p_seqno")))
      return -1;
   lsPseqno = colStr("ls_p_seqno");
   
   // --Get 消費金額:get anal_sub acctmm-1---------------------------------------
   // --acctYY is 2001: 帳務年月 is 200012 to 200111---
   lsVal1 = commString.intToStr((int) commString.strToNum(asYear) - 1) + "12";
   lsVal2 = asYear + "11";
   
   String sql2 = " select  sum(nvl(his_purchase_amt,0)) as ldc_amt1 , "
                         + " sum(nvl(his_cash_amt,0)) as ldc_amt2 "
                         + " from act_anal_sub "
                         + " where p_seqno = ?  and acct_month between ? and ? ";
   sqlSelect(sql2, new Object[]{lsPseqno, lsVal1, lsVal2});
   
   ldcAmt1 = (int) colNum("ldc_amt1");
   ldcAmt2 = (int) colNum("ldc_amt2");
   
   // --get 道路救援參數--
   String sql3 =
           " select  nvl(rpm_freeamt,0) as ldc_amt3 ,  nvl(rpm_cyearamt,0) as ldc_amt4 "
                   + " from cms_roadparm "
                   + " where 1=1 " + commSqlStr.rownum(1);
   
   sqlSelect(sql3);
   ldcAmt3 = (int) colNum("ldc_amt3");
   ldcAmt4 = (int) colNum("ldc_amt4");
   
   if (ldcAmt1 + ldcAmt2 >= ldcAmt3) {
      return 1;
   }
   else {
      return 2;
   }
}

public double getExpendamt() {
   // -- get 自費金額--
   double ldcExpamt = 0;
   String sql1 =
           " select  rpm_rcvamt  from cms_roadparm  where 1=1 " + commSqlStr.rownum(1);
   
   sqlSelect(sql1);
   
   ldcExpamt = colNum("rpm_rcvamt");
   
   return ldcExpamt;
}

// public int upd_carlog (String as_carno){
//
// }

public int chkFreeamtCuryy(String asCardno, String asYear) {
   // --check 當年恢復免費道路救援資格--
   // --Return: 0 -> no process
   // -- 1 -> 符合
   // -- -1 -> 不符合
   // ------------------------------------------------------------------------
   String lsSseqno = "", lsVal1 = "", lsVal2 = "", lsGrcode = "";
   int ldcAmt = 0, ldcAmt2 = 0, ldcAmt3 = 0;
   
   if (empty(asCardno.trim()))
      return 0;
   if (empty(asYear.trim()))
      return 0;
   
   // --get P_SEQNO--
   
   String sql1 = " select  nvl(p_seqno,'') as ls_pseqno ,  nvl(group_code,'0000') as ls_grcode "
                         + " from crd_card  where card_no = ? ";
   
   sqlSelect(sql1, new Object[]{asCardno});
   lsSseqno = colStr("ls_pseqno");
   lsGrcode = colStr("ls_grcode");
   if (sqlRowNum <= 0 || empty(lsSseqno.trim()))
      return -1;
   // --終身金卡---
   if (eqIgno(lsGrcode, "8888"))
      return 1;
   // --Get 消費金額--
   lsVal1 = commDate.dateAdd(asYear, 1, 0, 0) + "12";
   lsVal2 = asYear + "11";
   
   String sql2 =
           " select  sum(nvl(his_purchase_amt,0)) + sum(nvl(his_cash_amt,0)) as ldc_amt "
                   + " from act_anal_sub "
                   + " where p_seqno = ?  and acct_month between ? and ? ";
   sqlSelect(sql2, new Object[]{lsSseqno, lsVal1, lsVal2});
   ldcAmt = (int) colNum("ldc_amt");
   
   // --get 道路救援參數--
   String sql3 =
           " select  nvl(rpm_freeamt,0) as ldc_amt2 ,  nvl(rpm_cyearamt,0) as ldc_amt3 "
                   + " from cms_roadparm "
                   + " where 1=1 " + commSqlStr.rownum(1);
   sqlSelect(sql3);
   ldcAmt2 = (int) colNum("ldc_amt2");
   ldcAmt3 = (int) colNum("ldc_amt3");
   if (ldcAmt >= ldcAmt3)
      return 1;
   
   return -1;
   
}

public boolean chkFirstfree(String asCardno) {
   // --- adding 白金卡 -- shu yu tsai 20030211
   // --CHECK 首次免費道路救援登錄--
   boolean lbRc = true;
   
   String lsVal1 = "", lsVal2 = "", lsVal3 = "", lsVal4 = "", lsVal5 = "";
   String lsVal6 = "", lsVal7 = "", lsVal8 = "", lsVal9 = "", lsVal10 = "";

//    msg = "";
   if (empty(asCardno.trim()))
      return false;
   
   String sql1 =
           " select  nvl(A.group_code,'0000') as ls_val1 ,  nvl(B.card_not,'') as ls_val2 , "
                   + " nvl(A.current_code,'N') as ls_val3 ,  A.acct_type as ls_val4 ,  B.card_type as ls_val5 "
                   + " from crd_card A , ptr_card_type B "
                   + " where A.card_type = B.card_type  and A.card_no = ? ";
   sqlSelect(sql1, new Object[]{asCardno});
   
   if (sqlRowNum <= 0) {
      errmsg("卡號之卡種資料不存在");
      return false;
   }
   
   lsVal1 = colStr("ls_val1");
   lsVal2 = colStr("ls_val2");
   lsVal3 = colStr("ls_val3");
   lsVal4 = colStr("ls_val4");
   lsVal5 = colStr("ls_val5");
   
   // --無效卡--
   
   if (!eqIgno(lsVal3.trim(), "0")) {
      errmsg("卡號無效卡");
      return false;
   }
   
   // --例外:Group_code(8888)--
   if (eqIgno(lsVal1, "8888"))
      return true;
   
   // --acct_type--
   // --商務卡--
   
   String sql2 =
           " select  nvl(card_indicator,'') as ls_val6 ,  nvl(car_service_flag,'N') as ls_val7 "
                   + " from ptr_acct_type "
                   + " where acct_type = ? ";
   
   sqlSelect(sql2, new Object[]{lsVal4});
   
   if (sqlRowNum <= 0) {
      errmsg("卡號無法判定帳戶類別");
      return false;
   }
   
   lsVal6 = colStr("ls_val6");
   lsVal7 = colStr("ls_val7");
   
   if (!eqIgno(lsVal7.trim(), "Y")) {
      errmsg("帳戶類別未享有道路救援");
      return false;
   }
   
   // >>CARD==================================================================
   lsVal8 = "N";
   lsVal9 = "N";
   
   if (!empty(lsVal1.trim()) && !eqIgno(lsVal1.trim(), "0000")) {
      String sql3 = " select  nvl(gcard_flag,'N') as ls_val8 ,  nvl(bcard_flag,'N') as ls_val9 "
                            + " from cms_roadgroup "
                            + " where group_code = ? ";
      sqlSelect(sql3, new Object[]{lsVal1});
      lsVal8 = colStr("ls_val8");
      lsVal9 = colStr("ls_val9");
      if (sqlRowNum <= 0)
         lsVal1 = "0000";
   }
   
   if (empty(lsVal1.trim()) || eqIgno(lsVal1.trim(), "0000")) {
      String sql4 =
              " select  nvl(gcard_flag,'N') as ls_val8 ,  nvl(bcard_flag,'N') as ls_val9 ,  nvl(pcard_flag,'P') as ls_val10 "
                      + " from cms_roadparm " + commSqlStr.rownum(1);
      sqlSelect(sql4);
      
      lsVal8 = colStr("ls_val8");
      lsVal9 = colStr("ls_val9");
      lsVal10 = colStr("ls_val10");
   }
   
   lbRc = true;
   
   if (eqIgno(lsVal2, "C")) { // --普卡
      if (!eqIgno(lsVal9, "Y")) {
         if (!eqIgno(lsVal5, "VB") || !eqIgno(lsVal5, "MB")) {
            errmsg("普卡不可登錄");
            lbRc = false;
         }
      }
   }
   else if (eqIgno(lsVal2, "G")) { // --金卡
      if (!eqIgno(lsVal8, "Y")) {
         errmsg("金卡不可登錄");
         lbRc = false;
      }
   }
   else if (eqIgno(lsVal2, "P")) { // --白金卡
      if (eqIgno(lsVal10, "Y")) {
         errmsg("金卡不可登錄");
         lbRc = false;
      }
   }
   else {
      errmsg("無法判定金/普卡");
      lbRc = false;
   }
   return lbRc;
}

public int autonoDtl(String asKey) {
   // --get SEQNO from road detail--
   // =================================================
   String lsKey = "";
   int llSeqno = 0;
   
   lsKey = asKey.trim();
   if (empty(lsKey))
      lsKey = sysDate;  //commDate.sysDate();
   
   String sql1 = " select  max(rd_seqno) as ll_seqno "
                         + " from cms_roaddetail "
                         + " where rd_moddate = ? ";
   sqlSelect(sql1, new Object[]{lsKey});
   if (sqlRowNum > 0) {
      llSeqno = colInt("ll_seqno");
   }
   
   return llSeqno + 1;
}

// public int upd_carlog (String as_carno){
//
// }
// public int createds_car () {
//
// }
public int updBilSysexp() {
   // --產生道路救援自費帳單資料--
   // ========================================================================
   long L = 0;
   String lsKey1 = "", lsKey2 = "";
   String lsVal1 = "", lsVal2 = "", lsVal3 = "";
   String lsMod = "";
   double ldcAmt = 0;
   
   L = 1;
   lsKey1 = lsCardNo;
   lsKey2 = lsRmType;
   if (!eqIgno(lsKey2, "E"))
      return 0;
   
   // --AMT--
   ldcAmt = lsRmPayamt;
   if (ldcAmt <= 0)
      return 0;
   // --sysdate--
   lsVal1 = commDate.sysDate();
   // --acct_type/key--
   String sql1 = " select  acct_type as ls_val2 ,  acct_key as ls_val3 "
                         + " from crd_card  where card_no = ? ";
   sqlSelect(sql1, new Object[]{lsKey1});
   if (sqlRowNum <= 0)
      return -1;
   
   lsVal2 = colStr("ls_val2");
   lsVal3 = colStr("ls_val3");
   
   // --MOD-Attrib---
   
   String sql2 = " insert into bil_sysexp (  card_no ,  bill_type ,  txn_code ,  purchase_date , "
                         // + " acct_type , "
                         // + " acct_key , "
                         + " dest_amt ,  dest_curr ,  src_amt ,  post_flag ,  mod_user , "
                         + " mod_time ,  mod_pgm ,  mod_seqno  ) values (  :card_no , "
                         + " 'INCF' ,  '05' ,  :purchase_date , "
                         // + " :acct_type , "
                         // + " :acct_key , "
                         + " :dest_amt ,  '901' ,  :src_amt ,  'N' ,  :mod_user , "
                         + " sysdate ,  :mod_pgm ,  1  ) ";
   setString("card_no", lsKey1);
   setString("purchase_date", lsVal1);
   // setString("acct_type",lsVal2);
   // setString("acct_key",lsVal3);
   setString("dest_amt", "" + ldcAmt);
   setString("src_amt", "" + ldcAmt);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", lsModPgm);
   
   sqlExec(sql2);
   
   if (sqlRowNum <= 0)
      return -1;
   
   return 1;
}

public int carUnique(String asCarno) {
   if (empty(asCarno)) {
      errmsg("車號不可空白");
      return 0;
   }
   
   String tmpStr = "";
   int llCnt = 0;
   tmpStr = asCarno.trim();
   // --check CMS_ROADDETAIL--------------------------------------------
   String sql1 = " select  count(*) as ll_cnt  from cms_roaddetail "
                         + " where apr_date = ''  and rd_carno = ? ";
   sqlSelect(sql1, new Object[]{tmpStr});
   if (sqlRowNum <= 0) {
      errmsg("read cms_roaddetail error");
      return -1;
   }
   llCnt = colInt("ll_cnt");
   if (llCnt > 0) {
      errmsg("車號有異動 or 登錄, 請先作覆核");
      return -1;
   }
   
   String sql2 = " select  count(*) as ll_cnt  from cms_roaddetail "
                         + " where apr_date = ''  and rd_newcarno = ? ";
   sqlSelect(sql2, new Object[]{tmpStr});
   if (sqlRowNum <= 0) {
      errmsg("read cms_roaddetail error");
      return -1;
   }
   llCnt = colInt("ll_cnt");
   if (llCnt > 0) {
      errmsg("車號有異動 or 登錄, 請先作覆核");
      return -1;
   }
   // --check CMS_ROADMASTER---------------------------------------------
   String sql3 = " select  count(*) as ll_cnt  from cms_roadmaster "
                         + " where rm_type = 'F'  and card_no = ?  and nvl(rm_status,'0') <> '0' ";
   sqlSelect(sql3, new Object[]{tmpStr});
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error ");
      return -1;
   }
   llCnt = colInt("ll_cnt");
   if (llCnt > 0) {
      errmsg("車號已登錄免費, 不可再重覆登錄");
      return -1;
   }
   return 1;
}

public int carUniqueDetail(String asCarno, String asRowid) {
   // --Check car-no is Unique in CMS_ROADDETAIL for UNapprove
   // --
   // =====================================================================
   String tmpStr = "", sErrmsg = "", sRowid = "";
   long llCnt = 0;
   int iRc = 1;

//    msg = "";
   
   if (empty(asCarno)) {
      errmsg("車號不可空白");
      return 0;
   }
   
   tmpStr = asCarno.trim();
   
   if (empty(asRowid)) {
      sRowid = "";
   }
   else {
      sRowid = asRowid.trim();
   }
   // --check CMS_ROADDETAIL--------------------------------------------
   if (sRowid.length() == 0) {
      String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roaddetail "
                            + " where apr_date = '' " + " and rd_carno = ? " + " and rd_type = 'F' ";
      sqlSelect(sql1, new Object[]{tmpStr});
   }
   else {
      String sql1 =
              " select " + " count(*) as ll_cnt " + " from cms_roaddetail " + " where apr_date = '' "
                      + " and rd_carno = ? " + " and rd_type = 'F' " + " and rowid <> ? ";
      setString2(1, tmpStr);
      setRowId(2, sRowid);
      sqlSelect(sql1);
   }
   
   if (sqlRowNum <= 0) {
      errmsg("read cms_roaddetail error");
      return -1;
   }
   
   // --變更車號----------------------------------
   if (sRowid.length() == 0) {
      String sql2 = " select " + " count(*) as ll_cnt " + " from cms_roaddetail "
                            + " where apr_date = '' " + " and nvl(rd_newcarno,'') = ? " + " and rd_type = 'F' ";
      sqlSelect(sql2, new Object[]{tmpStr});
   }
   else {
      String sql2 =
              " select " + " count(*) as ll_cnt " + " from cms_roaddetail " + " where apr_date = '' "
                      + " and nvl(rd_newcarno,'') = ? " + " and rd_type = 'F' " + " and rowid <> ? ";
      setString2(1, tmpStr);
      setRowId(2, sRowid);
      sqlSelect(sql2);
   }
   
   if (sqlRowNum <= 0) {
      errmsg("read cms_roaddetail error");
      return -1;
   }
   
   llCnt = colInt("ll_cnt");
   if (llCnt > 0) {
      errmsg("車號有異動 or 登錄, 請先作覆核");
      return -1;
   }
   
   return 1;
}

public int hasFree(String asCardNo) {
   // --是否可享有免費道路救援
   // -- cms_roadparm2_dtl
   // =================================================================
   
   String lsSysDate = "";

//    msg = "";
   lsProjNo = "";
   lsPSeqno = "";
   lsCardNo = nvl(asCardNo, "");
   if (empty(lsCardNo))
      return 0;
   
   selectCrdCard();
   if (sqlRowNum <= 0) {
      errmsg("卡號不存在 or 已停卡");
      return -1;
   }
   
   // -一般卡-
   if (pos("|02|03", acctType) == 0) {
      String sql2 =
              " select  a.proj_no as ls_proj_no  from cms_roadparm2 b , cms_roadparm2_dtl a "
                      + " where a.proj_no = b.proj_no  and a.acct_type = ?  and a.card_type = ? "
                      + commSqlStr.col(groupCode, "a.group_code")
                      + " and uf_nvl(b.valid_end_date,'99991231') > to_char(sysdate,'yyyymmdd') "
                      + " and uf_nvl(apr_flag,'N') = 'Y' " + commSqlStr.rownum(1);
      sqlSelect(sql2, new Object[]{acctType, cardType});
   }
   else {
      String sql2 = " select  a.proj_no as ls_proj_no "
                            + " from cms_roadparm2 b , cms_roadparm2_dtl a  where a.proj_no = b.proj_no "
                            + " and a.acct_type = ?  and a.card_type = ? " + commSqlStr.col(corpNo, "a.corp_no")
                            + " and uf_nvl(b.valid_end_date,'99991231') > to_char(sysdate,'yyyymmdd') "
                            + " and uf_nvl(apr_flag,'N') = 'Y' " + commSqlStr.rownum(1);
      sqlSelect(sql2, new Object[]{acctType, cardType});
   }
   if (sqlRowNum > 0) {
      lsProjNo = colStr("ls_proj_no");
   }
   // -card_type+group_code-
   
   if (sqlRowNum <= 0) {
      String sql3 =
              " select  a.proj_no as ls_proj_no  from cms_roadparm2 b , cms_roadparm2_dtl a "
                      + " where a.acct_type = ?  and a.card_type = ? "
                      + " and a.group_code = '' and a.corp_no = ''  and a.proj_no = b.proj_no "
                      + " and uf_nvl(b.valid_end_date,'99991231') > to_char(sysdate,'yyyymmdd') "
                      + " and uf_nvl(apr_flag,'N') = 'Y' ";
      sqlSelect(sql3, new Object[]{acctType, cardType});
      if (sqlRowNum <= 0) {
         errmsg("卡號無免費道路救援權利");
         return -1;
      }
      lsProjNo = colStr("ls_proj_no");
   }
   
   // -min-issue-date-
   String lsIssueDate = "", lsOppostDate = "", lsSysDate2 = "";
   
   lsSysDate = commDate.sysDate();
   lsSysDate2 = commDate.dateAdd(lsSysDate, 0, -12, 0);
   
   String sql4 = " select " + " min(issue_date) as ls_issue_date " + " from crd_card "
                         + " where p_seqno = ? " + " and current_code = '0' ";
   
   sqlSelect(sql4, new Object[]{lsPSeqno});
   lsIssueDate = colStr("ls_issue_date").trim();
   
   String sql5 = " select " + " max(oppost_date) as ls_oppost_date " + " from crd_card "
                         + " where p_seqno = ? " + " and current_code <>'0' ";
   sqlSelect(sql5, new Object[]{lsPSeqno});
   lsOppostDate = colStr("ls_oppost_date").trim();
   
   // -非首年(B)-
   if (!eqIgno(lsIssueDate.substring(0, 4), lsSysDate.substring(0, 4))) {
      lsChType = "B";
      return 1;
   }
   
   // -首年-
   if (this.chkStrend(lsOppostDate, lsSysDate2) == 1) {
      lsChType = "A";
      return 1;
   }
   
   // -非首年:MIN(發卡日)in今年-
   lsChType = "C";
   
   return 1;
   
}

public int freeIsAuto(String asCardNo) {
   // --check card_type is auto register
   // -1: error, 1:auto, 0:is not auto
   // -------------------------------------------
   
   String lsCardNo = "", lsCardType = "";
   int llCnt = 0;
   
   lsCardNo = nvl(asCardNo, "");
   if (empty(lsCardNo)) {
      errmsg("卡號為空白");
      return -1;
   }
   String sql1 =
           " select  card_type as ls_card_type " + " from crd_card " + " where card_no = ? ";
   sqlSelect(sql1, new Object[]{lsCardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("卡號不存在");
      return -1;
   }
   
   lsCardType = colStr("ls_card_type");
   
   String sql2 = " select  count(*) as ll_cnt "
                         + " from ptr_card_type "
                         + " where card_type = ?  and rds_pcard = 'A' ";
   sqlSelect(sql2, new Object[]{lsCardType});
   
   if (colInt("ll_cnt") > 0)
      return 1;
   return 0;
   
}

public int carUniqueMaster(String asCarno) {
   String lsCar = "", ss = "";
   long llCnt = 0;

//    msg = "";
   
   if (empty(asCarno)) {
      errmsg("車號不可空白");
      return -1;
   }
   
   lsCar = asCarno.trim();
   
   // --check CMS_ROADMASTER---------------------------------------------
   llCnt = 0;
   String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roadmaster "
                         + " where rm_carno = ? " + " and rm_type = 'F' "
                         + " and (nvl(rm_status,'0') <> '0' or (rm_status = '0' and rm_reason = '2')) ";
   sqlSelect(sql1, new Object[]{lsCar});
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error");
      return -1;
   }
   llCnt = colInt("ll_cnt");
   
   if (llCnt > 0) {
      errmsg("車號已登錄免費, 不可再重覆登錄");
      return -1;
   }
   
   return 1;
}

public int carUniqueFree() {
   String cardNo = "", lsCar = "";
   int llCnt = 0;

//    msg = "";
   
   cardNo = nvl(lsCardNo, "");
   if (empty(cardNo)) {
      errmsg("卡號 不可空白");
   }
   
   lsCar = nvl(lsCarNo, "");
   if (empty(lsCar)) {
      errmsg("車號 不可空白");
      return -1;
   }
   
   String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roadmaster "
                         + " where rm_carno = ? " + " and card_no <> ? "
                         + " and (nvl(rm_status,'0') <> '0' or (rm_status = '0' and rm_reason='2'))";
   sqlSelect(sql1, new Object[]{lsCar, cardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error");
      return -1;
   }
   
   if (colInt("ll_cnt") > 0)
      return 1;
   return 0;
}

public int carUniqueExpn() {
   // --RC: -1.error, 0.no register, 1.registed
   // ============================================
   String cardNo = "", lsCar = "";
   int llCnt = 0;

//    msg = "";
   
   cardNo = nvl(lsCardNo, "");
   if (empty(cardNo)) {
      errmsg("卡號 不可空白");
      return -1;
   }
   
   lsCar = nvl(lsCarNo, "");
   if (empty(lsCar)) {
      errmsg("車號 不可空白");
      return -1;
   }
   
   String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roadmaster "
                         + " where rm_carno = ? " + " and card_no <> ? " + " and nvl(rm_status,'0') <> '0' "
                         + " and rm_validdate >= to_char(sysdate,'yyyymmdd') ";
   sqlSelect(sql1, new Object[]{lsCar, cardNo});
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error[car_unique_expn]");
      return -1;
   }
   
   if (colInt("ll_cnt") > 0)
      return 1;
   return 0;
   
}

public int hasFree() {
   msgOK();
   // --是否符合免費救援:ref cms_a014.pc
   // RC: -1.Error, 0.不符合, 1.符合
   // ===============================================
   String lsIdPseqno = "";
   String lsIssueDate = "", lsOppostDate = "", lsSysDate2 = "", lsSysDate = "";
   
   
   if (empty(lsCardNo)) {
      errmsg("卡號不可空白");
      return 0;
   }
   selectCrdCard();
   if (rc != 1)
      return -1;
   
   selectRdsRoadparm2();
   if (idsParm.listRows() <= 0)
      return 0;
   
   // -assign cardholer-type[A/B/C]-
   
   lsSysDate = commDate.sysDate();
   lsSysDate2 = commDate.dateAdd(lsSysDate, 0, -12, 0);
   
   // -min-issue_date-
   
   String sql3 = " select  min(issue_date) as ls_issue_date "
                         + " from crd_card "
                         + " where p_seqno = ?  and current_code = '0' ";
   sqlSelect(sql3, new Object[]{lsPSeqno});
   if (sqlRowNum > 0)
      lsIssueDate = colStr("ls_issue_date");
   
   // -MAX.oppost_date-
   String sql4 = " select  max(oppost_date) as ls_oppost_date  from crd_card "
                         + " where p_seqno = ?  and current_code <> '0' ";
   sqlSelect(sql4, new Object[]{lsPSeqno});
   if (sqlRowNum > 0)
      lsOppostDate = colStr("ls_oppost_date");
   
   // -非首年(B)-
   
   if (!eqIgno(lsIssueDate.substring(0, 4), lsSysDate.substring(0, 4))) {
      lsChType = "B";
      return 1;
   }
   
   // -首年-
   if (this.chkStrend(lsOppostDate, lsSysDate2) == 1) {
      lsChType = "A";
      return 1;
   }
   
   // -非首年:MIN(發卡日)in今年-
   lsChType = "C";
   
   return 1;
}

public int checkRoadparm2() {
   msgOK();
   // --check cms_roadparm2
   // --RC: -1.error, 1.符合, 0.不符合
   // ========================================
   int liRc = 0;
   long LL = 0;
   String chTypeDesc = "";
   
   String[] lsAcctCode = new String[6];
   String[] lsCond = new String[2];
   String lsItFlag = "", ss = "";
   int liMm = 0;
   double ldcOneLowAmt = 0;
   double[] ldcCond = new double[3];
   
   selectCrdCard();
   if (empty(lsPSeqno) && empty(lsCardNo)) {
      errmsg("未取得卡人資料[p_seqno/card_no]");
      return rc;
   }
   
   lsProjNo = "";
   idcPurchAmt = 0;
   ilPurchCnt = 0;
   idcLastAmt = 0;
   
   selectRdsRoadparm2();
   if (idsParm.listRows() <= 0)
      return 0;
   
   // 確認最後一個消費條件判斷是哪個
   boolean isLastConditionChTypeB = false;
   boolean isAnyParmMatch = false;
   
   for (int ll = 0; ll < idsParm.listRows(); ll++) {
      lsAcctCode[0] = "N";
      lsAcctCode[1] = "N";
      lsAcctCode[2] = "N";
      lsAcctCode[3] = "N";
      lsAcctCode[4] = "N";
      lsAcctCode[5] = "N";
      lsCond[0] = "N";
      lsCond[1] = "N";
      ldcCond[0] = 0;
      ldcCond[1] = 0;
      ldcCond[2] = 0;
      lsProjNo = idsParm.listStr(ll, "proj_no");
      outstadningCond = idsParm.listStr(ll, "outstanding_cond");
      if (eqIgno(lsChType, "A")) {
         ss = idsParm.listStr(ll, "fst_cond");
         if (eqIgno(ss, "Y") == false)
            continue;
         lsAcctCode[0] = idsParm.listStr(ll, "fst_acct_code_ao");
         lsAcctCode[1] = idsParm.listStr(ll, "fst_acct_code_bl");
         lsAcctCode[2] = idsParm.listStr(ll, "fst_acct_code_ca");
         lsAcctCode[3] = idsParm.listStr(ll, "fst_acct_code_id");
         lsAcctCode[4] = idsParm.listStr(ll, "fst_acct_code_it");
         lsAcctCode[5] = idsParm.listStr(ll, "fst_acct_code_ot");
         lsItFlag = idsParm.listStr(ll, "fst_acct_code_it_flag");
         liMm = (int) idsParm.listNum(ll, "fst_mm");
         ldcOneLowAmt = idsParm.listNum(ll, "fst_one_low_amt");
         lsCond[0] = idsParm.listStr(ll, "fst_amt_cond");
         ldcCond[0] = idsParm.listNum(ll, "fst_purch_amt");
         lsCond[1] = idsParm.listStr(ll, "fst_row_cond");
         ldcCond[1] = idsParm.listNum(ll, "fst_purch_row");
      }
      else if (eqIgno(lsChType, "B")) {
         ss = idsParm.listStr(ll, "lst_cond");
         if (eqIgno(ss, "Y") == false)
            continue;
         lsAcctCode[0] = idsParm.listStr(ll, "lst_acct_code_ao");
         lsAcctCode[1] = idsParm.listStr(ll, "lst_acct_code_bl");
         lsAcctCode[2] = idsParm.listStr(ll, "lst_acct_code_ca");
         lsAcctCode[3] = idsParm.listStr(ll, "lst_acct_code_id");
         lsAcctCode[4] = idsParm.listStr(ll, "lst_acct_code_it");
         lsAcctCode[5] = idsParm.listStr(ll, "lst_acct_code_ot");
         lsItFlag = idsParm.listStr(ll, "lst_acct_code_it_flag");
         liMm = (int) idsParm.listNum(ll, "lst_mm");
         ldcOneLowAmt = idsParm.listNum(ll, "lst_one_low_amt");
         lsCond[0] = idsParm.listStr(ll, "lst_amt_cond");
         ldcCond[0] = idsParm.listNum(ll, "lst_purch_amt");
         lsCond[1] = idsParm.listStr(ll, "lst_row_cond");
         ldcCond[1] = idsParm.listNum(ll, "lst_purch_row");
      }
      else if (eqIgno(lsChType, "C")) {
         ss = idsParm.listStr(ll, "cur_cond");
         if (eqIgno(ss, "Y") == false)
            continue;
         lsAcctCode[0] = idsParm.listStr(ll, "cur_acct_code_ao");
         lsAcctCode[1] = idsParm.listStr(ll, "cur_acct_code_bl");
         lsAcctCode[2] = idsParm.listStr(ll, "cur_acct_code_ca");
         lsAcctCode[3] = idsParm.listStr(ll, "cur_acct_code_id");
         lsAcctCode[4] = idsParm.listStr(ll, "cur_acct_code_it");
         lsAcctCode[5] = idsParm.listStr(ll, "cur_acct_code_ot");
         lsItFlag = idsParm.listStr(ll, "cur_acct_code_it_flag");
         liMm = (int) idsParm.listNum(ll, "cur_mm");
         ldcOneLowAmt = idsParm.listNum(ll, "cur_one_low_amt");
         lsCond[0] = idsParm.listStr(ll, "cur_amt_cond");
         ldcCond[0] = idsParm.listNum(ll, "cur_purch_amt");
         lsCond[1] = idsParm.listStr(ll, "cur_row_cond");
         ldcCond[1] = idsParm.listNum(ll, "cur_purch_row");
      }
      liRc = 0;
      // --read bil_bill
      if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "1")) {
         liRc = billAmtAcno(lsAcctCode, lsItFlag, liMm, ldcOneLowAmt);
      }
      else if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "2")) {
         liRc = billAmtCard(lsAcctCode, lsItFlag, liMm, ldcOneLowAmt);
      }
      else if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "3")) {
         liRc = billAmtId(lsAcctCode, lsItFlag, liMm, ldcOneLowAmt);
      }
      if (liRc == -1) {
         lsProjNo = "";
         outstadningCond = "";
         return -1;
      }
      
      // --read mkt_card_consume
      if (eqIgno(lsChType, "B")) {
         if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "1")) {
            liRc = lastAmtAcno(lsAcctCode);
         }
         else if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "2")) {
            liRc = lastAmtCard(lsAcctCode);
         }
         else if (eqIgno(idsParm.listStr(ll, "amt_sum_flag"), "3")) {
            liRc = lastAmtId(lsAcctCode);
         }
      }
      
      if (liRc == -1) {
         lsProjNo = "";
         outstadningCond = "";
         return -1;
      }
      
      // --check 消費條件--
      liRc = 0;
      
      if (eqIgno(lsChType, "B") && idcLastAmt < ldcCond[2]) {
         isLastConditionChTypeB = true;
         continue;
      }
      if (eqIgno(lsCond[0], "Y") && idcPurchAmt >= ldcCond[0]) {
         liRc = 1;
         return liRc;
      }
      if (eqIgno(lsCond[1], "Y") && ilPurchCnt >= ldcCond[1]) {
         liRc = 1;
         return liRc;
      }
      isLastConditionChTypeB = false;
   }
   
   // 沒有符合的專案
   lsProjNo = "";
   outstadningCond = "";
   
   if (isAnyParmMatch) {
      if (isLastConditionChTypeB) {
         errmsg("前一年度無消費金額。");
      }
      else {
         errmsg(String.format("累積消費金額[%s]未達%s且累積消費筆數[%s]未達%s筆。"
                 , idcPurchAmt, (int) ldcCond[0], ilPurchCnt, (int) ldcCond[1]));
      }
   }
   
   return liRc;
}

public int billAmtAcno(String[] asAcctCode, String asItFlag, int aiMm,
                       double adcOneLowAmt) {
   // --RC: 1.OK, -1.error
   // =======================================================
   String[] lsItem = new String[6];
   String lsThisAcctMonth = "", lsThisAcctMonth2 = "";
   
   if (empty(lsPSeqno)) {
      errmsg("未取得持卡人帳戶資料(p_seqno)");
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   
   for (int l = 0; l <= 5; l++) {
      lsItem[l] = nvl(asAcctCode[l], "N");
   }
   
   lsThisAcctMonth = "";
   
   String sql1 = " select A.this_acct_month from ptr_workday A where A.stmt_cycle in "
                         + " (select stmt_cycle from act_acno where p_seqno = ?) ";
   
   sqlSelect(sql1, new Object[]{lsPSeqno});
   if (sqlRowNum <= 0) {
      errmsg("bill_amt.ptr_workday error ");
      return -1;
   }
   lsThisAcctMonth = colStr("this_acct_month");
   lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1 + aiMm * -1);
   idcPurchAmt = 0;
   ilPurchCnt = 0;
   
   String sql2 = " select sum(decode(A.sign_flag,'-',0,1)) as purch_cnt , "
                         + " sum(decode(a.acct_code,'IT',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                         + " from bil_bill a left join bil_contract b on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                         + " where a.p_seqno = :p_seqno " + " and a.acct_code in "
                         + " (decode(:ls_item1,'Y','AO','XX') , decode(:ls_item2,'Y','BL','XX') , decode(:ls_item3,'Y','CA','XX') , "
                         + "  decode(:ls_item4,'Y','ID','XX') , decode(:ls_item5,'Y','IT','XX') , decode(:ls_item6,'Y','OT','XX') ) "
                         + " and ( (a.sign_flag = '-') or (a.sign_flag ='+' and a.dest_amt > :adc_one_low_amt)) "
                         + " and A.acct_month between :ls_month1 and :ls_month2 "
                         + " and A.ecs_cus_mcht_no NOT IN (" +
                         " SELECT data_code FROM MKT_MCHTGP_DATA" +
                         " WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
                         " )";
   
   setString2("as_acct_code_it_flag", asItFlag);
   setString2("p_seqno", lsPSeqno);
   setString2("ls_item1", lsItem[0]);
   setString2("ls_item2", lsItem[1]);
   setString2("ls_item3", lsItem[2]);
   setString2("ls_item4", lsItem[3]);
   setString2("ls_item5", lsItem[4]);
   setString2("ls_item6", lsItem[5]);
   setDouble2("adc_one_low_amt", adcOneLowAmt);
   setString2("ls_month1", lsThisAcctMonth2);
   setString2("ls_month2", lsThisAcctMonth);
   
   sqlSelect(sql2);
   
   idcPurchAmt = colInt("purch_amt");
   ilPurchCnt = colInt("purch_cnt");
   
   
   return 1;
   
}

public int lastAmtAcno(String[] asItem) {
   String[] lsItem = new String[6];
   String lsLastYear = "";
   int ii = 0;
   
   if (empty(nvl(lsPSeqno, ""))) {
      errmsg("未取得持卡人 帳戶資料[p_seqno]");
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   
   int ilArrayList = asItem.length;
   for (int ll = 0; ll < ilArrayList; ll++) {
      if (eqIgno(nvl(asItem[ll], ""), "Y"))
         lsItem[ll] = "Y";
   }
   
   lsLastYear = commDate.dateAdd(commDate.sysDate(), 0, -12, 0).substring(0, 4);
   
   idcLastAmt = 0;
   String sql1 = " select " + " sum(" + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)" + " ) as idc_last_amt "
                         + " from mkt_card_consume " + " where p_seqno = ? "
                         + " and nvl(acct_month,'x') between ? and ? ";
   sqlSelect(sql1, new Object[]{lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4],
           lsItem[5], lsPSeqno, lsLastYear + "01", lsLastYear + "12"});
   
   if (sqlRowNum <= 0) {
      errmsg("[mkt_card_consume] 查無資料,PSeqno=" + lsPSeqno);
      return -1;
   }
   
   idcLastAmt = colInt("idc_last_amt");
   
   return 1;
   
}

public int billAmtCard(String asAcctCode[], String asItFlag, int aiMm,
                       double adcOneLowAmt) {
   // --RC: 1.OK, -1.error
   // =======================================================
   String[] lsItem = new String[6];
   String lsThisAcctMonth = "", lsThisAcctMonth2 = "";
   int ii = 0;
   
   if (empty(lsCardNo)) {
      errmsg("未取得 卡片資料(card_no)");
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   
   int ilArrayLength = asAcctCode.length;
   for (int ll = 0; ll < ilArrayLength; ll++) {
      lsItem[ll] = nvl(asAcctCode[ll], "N");
   }
   
   // --
   
   lsThisAcctMonth = "";
   
   String sql1 = " select " + " this_acct_month as ls_this_acct_month " + " from ptr_workday "
                         + " where stmt_cycle = " + " (select stmt_cycle from crd_card where card_no = ? ) ";
   sqlSelect(sql1, new Object[]{lsCardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("[ptr_workday] 查無資料,cardNo=" + lsCardNo);
      return -1;
   }
   
   lsThisAcctMonth = colStr("ls_this_acct_month");
   lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1 + aiMm * -1);
   idcPurchAmt = 0;
   ilPurchCnt = 0;
   
   String sql2 = " select sum(decode(A.sign_flag,'-',0,1)) as purch_cnt , "
                         + " sum(decode(a.acct_code,'IT',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                         + " from bil_bill a left join bil_contract b on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                         + " where a.p_seqno =:p_seqno "
                         + " and a.major_card_no in (select major_card_no from crd_card where card_no =:card_no) "
                         + " and a.acct_code in "
                         + " (decode(:ls_item1,'Y','AO','XX') , decode(:ls_item2,'Y','BL','XX') , decode(:ls_item3,'Y','CA','XX') , "
                         + "  decode(:ls_item4,'Y','ID','XX') , decode(:ls_item5,'Y','IT','XX') , decode(:ls_item6,'Y','OT','XX') ) "
                         + " and (A.sign_flag ='-' or (A.sign_flag = '+' and A.dest_amt > :adc_one_low_amt)) "
                         + " and A.acct_month between :ls_month1 and :ls_month2 "
                         + " and A.ecs_cus_mcht_no NOT IN (" +
                         " SELECT data_code FROM MKT_MCHTGP_DATA" +
                         " WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
                         " )";
   
   setString2("as_acct_code_it_flag", asItFlag);
   setString2("p_seqno", lsPSeqno);
   setString2("card_no", lsCardNo);
   setString2("ls_item1", lsItem[0]);
   setString2("ls_item2", lsItem[1]);
   setString2("ls_item3", lsItem[2]);
   setString2("ls_item4", lsItem[3]);
   setString2("ls_item5", lsItem[4]);
   setString2("ls_item6", lsItem[5]);
   setDouble2("adc_one_low_amt", adcOneLowAmt);
   setString2("ls_month1", lsThisAcctMonth2);
   setString2("ls_month2", lsThisAcctMonth);
   
   sqlSelect(sql2);
   
   idcPurchAmt = colInt("purch_amt");
   ilPurchCnt = colInt("purch_cnt");
   return 1;
   
}

public int lastAmtCard(String asItem[]) {
   String[] lsItem = new String[6];
   String lsLastYear = "";
   
   if (empty(lsCardNo)) {
      errmsg("未取得持卡人 卡片資料[card_no]");
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   int ilArrayLength = asItem.length;
   
   for (int ll = 0; ll < ilArrayLength; ll++) {
      if (eqIgno(nvl(asItem[ll]), "Y"))
         lsItem[ll] = "Y";
   }
   
   lsLastYear = commDate.dateAdd(commDate.sysDate(), 0, -12, 0).substring(0, 4);
   
   idcLastAmt = 0;
   
   String sql1 = " select " + " sum(" + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)" + " ) as idc_last_amt "
                         + " from mkt_card_consume " + " where major_card_no in "
                         + " (select major_card_no from crd_card where card_no = ? ) "
                         + " and nvl(acct_month,'x') between ? and ? ";
   sqlSelect(sql1, new Object[]{lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4],
           lsItem[5], lsCardNo, lsLastYear + "01", lsLastYear + "12"});
   
   if (sqlRowNum <= 0) {
      errmsg("[mkt_card_consume] 查無資料,cardNo=" + lsCardNo);
      return -1;
   }
   
   idcLastAmt = (int) colNum("idc_last_amt");
   
   return 1;
   
}


public int billAmtId(String asAcctCode[], String asItFlag, int aiMm,
                     double adcOneLowAmt) {
   // --RC: 1.OK, -1.error
   // =======================================================
   String[] lsItem = new String[6];
   String lsThisAcctMonth = "", lsThisAcctMonth2 = "";
   int ii = 0;
   
   if (empty(lsCardNo)) {
      errmsg("未取得 卡片資料,cardNo=" + lsCardNo);
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   
   int ilArrayLength = asAcctCode.length;
   for (int ll = 0; ll < ilArrayLength; ll++) {
      lsItem[ll] = nvl(asAcctCode[ll], "N");
   }
   
   // --
   
   lsThisAcctMonth = "";
   
   String sql1 = " select " + " this_acct_month as ls_this_acct_month " + " from ptr_workday "
                         + " where stmt_cycle = " + " (select stmt_cycle from crd_card where card_no = ? ) ";
   sqlSelect(sql1, new Object[]{lsCardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("[ptr_workday] 查無資料,cardNo=" + lsCardNo);
      return -1;
   }
   
   lsThisAcctMonth = colStr("ls_this_acct_month");
   lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1 + aiMm * -1);
   idcPurchAmt = 0;
   ilPurchCnt = 0;
   
   String sql2 = " select sum(decode(A.sign_flag,'-',0,1)) as purch_cnt , "
                         + " sum(decode(a.acct_code,'IT',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                         + " from bil_bill a left join bil_contract b on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                         + " where a.p_seqno =:p_seqno "
                         + " and a.card_no =:card_no "
                         + " and a.acct_code in "
                         + " (decode(:ls_item1,'Y','AO','XX') , decode(:ls_item2,'Y','BL','XX') , decode(:ls_item3,'Y','CA','XX') , "
                         + "  decode(:ls_item4,'Y','ID','XX') , decode(:ls_item5,'Y','IT','XX') , decode(:ls_item6,'Y','OT','XX') ) "
                         + " and (sign_flag ='-' or (sign_flag = '+' and a.dest_amt > :adc_one_low_amt)) "
                         + " and A.acct_month between :ls_month1 and :ls_month2 "
                         + " and A.ecs_cus_mcht_no NOT IN (" +
                         " SELECT data_code FROM MKT_MCHTGP_DATA" +
                         " WHERE table_name ='MKT_MCHT_GP' AND data_key ='MKTR00001' AND data_type='1'" +
                         " )";
   
   
   setString2("as_acct_code_it_flag", asItFlag);
   setString2("p_seqno", lsPSeqno);
   setString2("card_no", lsCardNo);
   setString2("ls_item1", lsItem[0]);
   setString2("ls_item2", lsItem[1]);
   setString2("ls_item3", lsItem[2]);
   setString2("ls_item4", lsItem[3]);
   setString2("ls_item5", lsItem[4]);
   setString2("ls_item6", lsItem[5]);
   setDouble2("adc_one_low_amt", adcOneLowAmt);
   setString2("ls_month1", lsThisAcctMonth2);
   setString2("ls_month2", lsThisAcctMonth);

//    String sql2 = " select " + " count(*) as purch_cnt , "
//            + " sum(decode(a.acct_code,'IT',decode(:as_acct_code_it_flag,'2',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)),case when a.txn_code in ('06','25','27','28','29')then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
//            + " from bil_contract b left join bil_bill a on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
//            + " where a.p_seqno =:p_seqno " + " and a.id_p_seqno =:id_p_seqno " + " and a.acct_code in "
//            + " (decode(:ls_item1,'Y','AO','XX') , decode(:ls_item2,'Y','BL','XX') , decode(:ls_item3,'Y','CA','XX') , "
//            + "  decode(:ls_item4,'Y','ID','XX') , decode(:ls_item5,'Y','IT','XX') , decode(:ls_item6,'Y','OT','XX') ) "
//            + " and ( (a.txn_code in ('06','25','27','28','29')) or (txn_code not in ('06','25','27','28','29') and a.dest_amt > :adc_one_low_amt)) "
//            + " and acct_month between :ls_month1 and :ls_month2 ";
//    setString2("as_acct_code_it_flag", asItFlag);
//    setString2("p_seqno", isPSeqno);
//    setString2("id_p_seqno", isIdPSeqno);
//    setString2("ls_item1", lsItem[0]);
//    setString2("ls_item2", lsItem[1]);
//    setString2("ls_item3", lsItem[2]);
//    setString2("ls_item4", lsItem[3]);
//    setString2("ls_item5", lsItem[4]);
//    setString2("ls_item6", lsItem[5]);
//    setDouble2("adc_one_low_amt", adcOneLowAmt);
//    setString2("ls_month1", lsThisAcctMonth);
//    setString2("ls_month2", lsThisAcctMonth2);
   
   sqlSelect(sql2);
   
   idcPurchAmt = colInt("purch_amt");
   ilPurchCnt = colInt("purch_cnt");
   
   return 1;
   
}

public int lastAmtId(String asItem[]) {
   String[] lsItem = new String[6];
   String lsLastYear = "";
   
   if (empty(lsCardNo)) {
      errmsg("未取得持卡人 卡片資料[card_no]");
      return -1;
   }
   
   lsItem[0] = "N";
   lsItem[1] = "N";
   lsItem[2] = "N";
   lsItem[3] = "N";
   lsItem[4] = "N";
   lsItem[5] = "N";
   int ilArrayLength = asItem.length;
   
   for (int ll = 0; ll < ilArrayLength; ll++) {
      if (eqIgno(nvl(asItem[ll]), "Y"))
         lsItem[ll] = "Y";
   }
   
   lsLastYear = commDate.dateAdd(commDate.sysDate(), 0, -12, 0).substring(0, 4);
   
   idcLastAmt = 0;
   
   String sql1 = " select " + " sum(" + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
                         + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)" + " ) as idc_last_amt "
                         + " from mkt_card_consume "
                         + " where card_no = ?  "
                         + " and nvl(acct_month,'x') between ? and ? ";
   sqlSelect(sql1, new Object[]{lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4],
           lsItem[5], lsCardNo, lsLastYear + "01", lsLastYear + "12"});

//    String sql1 = " select " + " sum(" + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
//        + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
//        + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
//        + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
//        + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
//        + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)" + " ) as idc_last_amt "
//        + " from mkt_card_consume " + " where id_p_seqno = ? "
//        + " and nvl(acct_month,'x') between ? and ? ";
//    sqlSelect(sql1, new Object[] {lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4],
//        lsItem[5], isIdPSeqno, lsLastYear + "01", lsLastYear + "12"});
   
   if (sqlRowNum <= 0) {
      errmsg("[mkt_card_consume] 查無資料,cardNo=" + lsCardNo);
      return -1;
   }
   
   idcLastAmt = (int) colNum("idc_last_amt");
   
   return 1;
   
}

public int carUniqueFree(String asCardNo, String asCar) {
   String lsCardNo = "", lsCar = "";
   long llCnt = 0;

//    msg = "";
   
   lsCardNo = nvl(asCardNo, "");
   
   if (empty(lsCardNo)) {
      errmsg("卡號不可空白");
      return -1;
   }
   
   lsCar = nvl(asCar, "");
   if (empty(lsCar)) {
      errmsg("車號 不可空白");
      return -1;
   }
   
   String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roadmaster "
                         + " where rm_carno = ? " + " and card_no <> ? "
                         + " and (nvl(rm_status,'0') <> '0' or (rm_status ='0' and rm_reason = '2')) ";
   sqlSelect(sql1, new Object[]{lsCar, lsCardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error");
      return -1;
   }
   
   llCnt = colInt("ll_cnt");
   if (llCnt > 0)
      return 1;
   
   return 0;
   
}

public int carUniqueExpn(String asCardNo, String asCar) {
   // --RC: -1.error, 0.no register, 1.registed
   // ============================================
   String lsCardNo = "", lsCar = "";
   long llCnt = 0;
//    msg = "";
   
   lsCardNo = nvl(asCardNo, "");
   if (empty(lsCardNo)) {
      errmsg("卡號不可空白");
      return -1;
   }
   
   lsCar = nvl(asCar, "");
   if (empty(lsCar)) {
      errmsg("車號 不可空白");
      return -1;
   }
   
   String sql1 = " select " + " count(*) as ll_cnt " + " from cms_roadmaster "
                         + " where rm_carno = ? " + " and card_no <> ? " + " and nvl(rm_status,'0') <> '0' "
                         + " and rm_validdate >= to_char(sysdate,'yyyymm') ";
   sqlSelect(sql1, new Object[]{llCnt, lsCardNo});
   
   if (sqlRowNum <= 0) {
      errmsg("read cms_roadmaster error[car_unique_expn]");
      return -1;
   }
   
   llCnt = colInt("ll_cnt");
   if (llCnt > 0)
      return 1;
   
   return 0;
   
}

}
