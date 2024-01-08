package rdsm01;

import busi.func.EcsFunc;

public class Rdsm0030 extends ofcapp.BaseAction{
String kk_rowid="", kk_modDate="";

@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "X":
         String lsCardNo = wp.itemStr("ex_card_no");
         String lsCarNo = wp.itemStr("ex_car_no");
         String lsIdNo = wp.itemStr("ex_id_no");
         strAction = "new";
         if (selectNewData(lsCardNo, lsCarNo, lsIdNo) == false) {
            return;
         }
         break;
      case "XLS":
         doPrint_xls(); break;
      default:
         defaultAction();
   }
}

void tabClick() {
   String lsClick = "";
   lsClick = wp.itemStr2("tab_click");
   if (eqIgno(wp.buttonCode, "Q"))
      wp.colSet("a_click_1", "");
   if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "tab_active");
   } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "tab_active");
   } else {
      wp.colSet("a_click_1", "tab_active");
   }
}

boolean selectNewData(String aCardNo, String aCarNo, String aIdNo) {
   if (empty(aCardNo)) {
      alertErr("卡號不可空白");
      return false;
   }

   String sql1 = " select  A.id_p_seqno ,  A.id_no ,  A.chi_name , "
           + " A.office_area_code1 ,  A.office_tel_no1 ,  A.office_tel_ext1 , "
           + " A.home_area_code1 ,  A.home_tel_no1 ,  A.home_tel_ext1 , "
           + " A.cellar_phone"+
           ", B.major_card_no ,  B.card_type ,  B.new_end_date"+
           ", B.group_code"+
           //", P.rds_pcard"
         " from crd_card B join crd_idno A on A.id_p_seqno=B.id_p_seqno"+
           //" left join ptr_card_type P on P.card_type=B.card_type"
           " where B.card_no = ? ";
   sqlSelect(sql1, aCardNo);

   if (sqlRowNum <= 0) {
      alertErr("卡號輸入錯誤");
      return false;
   }
   String ls_idNo=aIdNo;
   if (empty(ls_idNo)) ls_idNo=sqlStr("id_no");
   // 卡號存在CRD_CARD，檢核身分證號是否相同
   if (empty(ls_idNo)) {
      alertErr("身分證號不存在");
      return false;
   }
   if (!empty(aIdNo) && !eqIgno(ls_idNo,sqlStr("id_no"))) {
      alertErr("身份證號(ID_NO)比對不符，該卡號身份證號碼是" + sqlStr("id_no"));
      return false;
   }

   //-優惠別-
   RdsFunc loFunc =new RdsFunc();
   loFunc.setConn(wp.getConn());
   String ls_pCard=loFunc.getRdsPcard(aCardNo);
   if (commString.strIn(ls_pCard,",I,P,V")==false) {
      alertErr("卡號優惠別不昰 I,V,P");
      return false;
   }

   String ls_idPseqno = sqlStr("id_p_seqno");
   if (!checkRoadMaster(aCardNo,ls_idNo, ls_idPseqno)) {
      return false;
   }

   wp.colSet("rd_carmanid", sqlStr("id_no"));
   wp.colSet("chi_name", sqlStr("chi_name"));
   wp.colSet("rd_carmanname", sqlStr("chi_name"));
   wp.colSet("rd_otelno1", sqlStr("office_area_code1"));
   wp.colSet("rd_otelno2", sqlStr("office_tel_no1"));
   wp.colSet("rd_otelno3", sqlStr("office_tel_ext1"));
   wp.colSet("rd_htelno1", sqlStr("home_area_code1"));
   wp.colSet("rd_htelno2", sqlStr("home_tel_no1"));
   wp.colSet("rd_htelno3", sqlStr("home_tel_ext1"));
   wp.colSet("cellar_phone", sqlStr("cellar_phone"));
   wp.colSet("card_no", sqlStr("major_card_no"));
   wp.colSet("group_code", sqlStr("group_code"));
   wp.colSet("appl_card_no", aCardNo);
   wp.colSet("rd_validdate", commString.mid(sqlStr("new_end_date"), 0, 6));
   wp.colSet("xx_sys_date", wp.sysDate);
   wp.colSet("rds_pcard", ls_pCard);
   wp.colSet("give_flag","Y");
   wp.colSet("id_p_seqno", ls_idPseqno);  //apply.idpseqno

   return true;
}

boolean checkRoadMaster(String lsCardNo, String lsIdNo, String idPSeqno) {
   String sql1 = " select  count(*) as db_cnt  from cms_roadmaster "
           + " where card_no = ?  and rm_carmanid = ? ";
   sqlSelect(sql1, new Object[] {lsCardNo, lsIdNo});
   if (sqlNum("db_cnt") > 0) {
      alertErr("此資料已存在, 請由救援主檔中修改");
      return false;
   }

   sql1 = " select  count(*) as db_cnt  from cms_roadmaster where id_p_seqno = ? ";
   sqlSelect(sql1, new Object[] {idPSeqno});
   if (sqlNum("db_cnt") > 0) {
      alertErr("持卡人己作過免費道路救援");
       return false;
   }

   return true;
}

void doPrint_xls() throws Exception {

}

@Override
public void dddwSelect() {

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_id_no,ex_card_no,ex_car_no")) {
      alertErr2("身分證號，卡號，車號 不可同時空白 ");
      return;
   }

   if (wp.itemEmpty("ex_card_no") == false) {
      zzVipColor(wp.itemStr2("ex_card_no"));
   }

   String lsWhere = " where 1=1  "
           + sqlCol(wp.itemStr("ex_card_no"), "card_no")
           + sqlCol(wp.itemStr("ex_car_no"), "rm_carno")
           + sqlCol(wp.itemStr("ex_id_no"), "rm_carmanid");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   daoTid = "A1_";
   wp.selectSQL = " card_no , rm_type"+
           ", decode(rm_type,'F','免費','E','自費') as tt_rm_type"+
           ", rm_carno , uf_idno_name(card_no) as chi_name"+
           ", rm_carmanname , rm_status"+
           //" decode(rm_status,'1','新增車號','2','變更車號','0','停用','3','取消車號','4','未啟用') as tt_rm_status ,"+
           ", rm_payamt , rm_validdate , rm_moddate , rm_reason "+
           //" decode(rm_reason,'1','到期不續購','2','消費不足暫停服務','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rm_reason "+
           ", rds_pcard, give_flag"+
           ", hex(rowid) as rowid "
           ;
   wp.daoTable = "cms_roadmaster";
   pageQuery();
   wp.setListCount(0);
   if (sqlNotFind()) {
      selectOK();
   }

   queryRead2();

   if (wp.listCount[0] ==0 && wp.listCount[1] ==0) {
      alertErr2("此條件查無資料");
      return;
   }
   queryAfter();
}

void queryRead2() throws Exception {
   daoTid = "A2_";
   wp.selectSQL =" rd_moddate , rd_type"+
           ", decode(rd_type,'F','免費','E','自費') as tt_rd_type"+
           ", card_no , rd_carno"+
           ", uf_idno_name(card_no) as db_cname"+
           ", rd_carmanname , rd_status"+
           //" decode(rd_status,'1','新增車號','2','變更車號','0','停用','3','取消車號', '4', '未啟用') as tt_rd_status ,"+
           ", new_card_no , rd_newcarno , rd_payamt , rd_validdate"+
           ", rd_stoprsn"+
           //", decode(rd_stoprsn,'1','到期不續購','2','消費不足暫停服務','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rd_stoprsn ,"+
           ", apr_date , rd_modtype , rd_seqno , mod_user , appl_card_no "+
           ", rds_pcard, give_flag"+
           ", hex(rowid) as rowid "+
           "";
   wp.daoTable = "cms_roaddetail";
   wp.whereStr = "where 1=1"
           + sqlCol(wp.itemStr("ex_card_no"), "appl_card_no")
           + sqlCol(wp.itemStr("ex_car_no"), "rd_carno")
           + sqlCol(wp.itemStr("ex_id_no"), "rd_carmanid");

   if (eqIgno(wp.itemStr("ex_apr_flag"), "1")) {
      wp.whereStr += " and apr_date ='' ";
   } else if (eqIgno(wp.itemStr("ex_apr_flag"), "2")) {
      wp.whereStr += " and apr_date <>'' ";
   }
   wp.whereOrder = "order by mod_time desc";
   pageQuery();
   wp.setListCount(2);

   if (this.sqlNotFind()) {
      selectOK();
   }
}

void queryAfter() {
   int ll_nrow=wp.listCount[0];
   for (int ll = 0; ll <ll_nrow ; ll++) {
      String ss=wp.colStr(ll,"A1_rm_status");
      wp.colSet(ll,"A1_tt_rm_status",ecsfunc.DeCodeRdsm.rdStatus(ss));
      //tt_rm_reason
      ss =wp.colStr(ll,"A1_rm_reason");
      wp.colSet(ll,"A1_tt_rm_reason",ecsfunc.DeCodeRdsm.rmReason(ss));
   }
   //--
   ll_nrow =wp.listCount[1];
   for (int ll = 0; ll <ll_nrow ; ll++) {
      //rd_status
      String ss=wp.colStr(ll,"A2_rd_status");
      wp.colSet(ll,"A2_tt_rd_status",ecsfunc.DeCodeRdsm.rdStatus(ss));
      //tt_rd_stoprsn
      ss =wp.colStr(ll,"A2_rd_stoprsn");
      wp.colSet(ll,"A2_tt_rd_stoprsn",ecsfunc.DeCodeRdsm.rdStoprsn(ss));
   }
}

@Override
public void querySelect() throws Exception {
   kk_rowid = wp.itemStr("data_k1");
   kk_modDate = wp.itemStr("data_k2");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(kk_rowid)) {
      alertErr("指定要查詢資料(rowid)");
      return;
   }

   if (empty(kk_modDate)) {
      selectRodateMaster();
      btnOnAud(false,true,true);
   }
   else {
      selectRoadDetail();
      btnOnAud(false,false,false);
   }

   //-dataRead_After()-
   String ss=wp.colStr("rm_status");
   wp.colSet("tt_rm_status", ecsfunc.DeCodeRdsm.rdStatus(ss));
}

void selectRodateMaster() {
   wp.selectSQL ="'' rd_moddate"
           +", 0 as rd_seqno"
           +", card_no"
           +", rm_carmanid rd_carmanid"
           +", card_no appl_card_no"
           +", group_code"
           +", (select group_name from ptr_group_code where group_code=A.group_code) as group_name"
           +", rm_type rd_type"
           +", rm_validdate rd_validdate"
           +", rm_status, '' tt_rm_status"
           +", rm_carno rd_carno"
           +", '' rd_newcarno"
           +", rm_carmanname rd_carmanname"
           +", rds_pcard, give_flag"
           +", '' rd_stopdate"
           +", '' rd_stoprsn"
           +", rm_payamt, 0 rd_payamt"
           +", rm_paydate rd_paydate"
           +", outstanding_yn, '' outstanding_cond"
           +", crt_user, crt_date"
           +", '' proj_no, '' cardholder_type"
           +", 0 purch_amt, 0 purch_cnt, 0 purch_amt_lyy"
           +", mod_seqno rm_mod_seqno, hex(rowid) as rm_rowid"
           +", rm_htelno1 rd_htelno1"
           +", rm_htelno2 rd_htelno2"
           +", rm_htelno3 rd_htelno3"
           +", rm_otelno1 rd_otelno1"
           +", rm_otelno2 rd_otelno2"
           +", rm_otelno3 rd_otelno3"
           +", cellar_phone"
   ;
   wp.daoTable = "cms_roadMaster A ";
   wp.whereStr ="where 1=1"+commSqlStr.whereRowid;
   setParm(1, kk_rowid);
   logSql();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("此條件查無資料(cms_roadMaster)");
      return;
   }
   wp.colSet("rd_moddate", wp.sysDate);
}
void selectRoadDetail() {
   wp.selectSQL ="A.*"+
           ", rd_status as rm_status"+
           ", (select group_name from ptr_group_code as pgc where 1=1 and A.group_code = pgc.group_code ) as group_name "+
           ", to_char(mod_time,'yyyymmdd') mod_date"+
           ", hex(A.rowid) as rowid"
   ;
   wp.daoTable = "cms_roaddetail A ";
   wp.whereStr ="where 1=1"+commSqlStr.whereRowid;
   setParm(1, kk_rowid);
   logSql();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("此條件查無資料(cms_roadDaster)");
      return;
   }
}

@Override
public void saveFunc() throws Exception {
   rdsm01.Rdsm0030Func func = new rdsm01.Rdsm0030Func();
   func.setConn(wp);

   //rc = func.dbSave(strAction);
   if (isAdd()) rc=func.dbInsert();
   else if (isUpdate()) rc=func.dbUpdate();
   else if (isDelete()) rc=func.dbDelete();
   if (rc == 2) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "1");
      wp.colSet("conf_mesg", func.getMsg());
      return;
   } else if (rc == 3) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "2");
      wp.colSet("conf_mesg", func.getMsg());
      return;
   } else if (rc == 4) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "3");
      wp.colSet("conf_mesg", func.getMsg());
      return;
   }else if (rc == 5) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "4");
      wp.colSet("conf_mesg", func.getMsg());
      return;
   }

   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
      return;
   }

   saveAfter(false);

}

@Override
public void procFunc() throws Exception {

}

@Override
public void initButton() {

}

@Override
public void initPage() {
   tabClick();
}
}
