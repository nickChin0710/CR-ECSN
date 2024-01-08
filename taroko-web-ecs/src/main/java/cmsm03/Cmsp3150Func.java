package cmsm03;
/** package cms03
 ** 110-01-11   Tom Hsu      updated for project coding standard     *
 ** 匯入txt 檔     *
 * */
import busi.FuncAction;
import taroko.com.TarokoFileAccess;

public class Cmsp3150Func extends FuncAction {


  int llErr = 0;
  int ll=0;
  int datai=0;
  TarokoFileAccess tf = new TarokoFileAccess(wp);
  String isSqlVisit ="";
  String lsPpCardNo = "";
  String vip_kind="";
  int llDataSeqno = 0;
  int llCnt=0;
  String id_p_seqno="";
  String id_p_seqnoa="";
  String ch_ename="";
  String pp_card_no="";
  String pymt_cond="";
  String pp_id="";
  String id_no1="";
  String ppcard_credit_cardno="";
  String err_code="";
  String err_desc="";


  @Override
  public void dataCheck() {

  }


  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int insertVist(int ll) {
    msgOK();
    String vip_Kind = wp.itemStr("ex_vip_kind");
    System.out.println("vip_kind"+wp.itemStr("ex_vip_kind"));
    // dataCheck();


    // if ("1".equals (wp.itemStr("ex_vip_kind"))) {


    if (empty(isSqlVisit)) {

      isSqlVisit = " insert into cms_ppcard_visit (" + " crt_date ," + " bin_type ,"
              + " data_seqno ," + " from_type ," + " bank_name ," + " deal_type ," + " associate_code ,"
              + " ica_no ," + " pp_card_no ," + " ch_ename ," + " visit_date ," + " lounge_name ,"
              + " lounge_code ," + " domestic_int ," + " iso_conty ," + " iso_conty_code ,"
              + " ch_visits ," + " guests_count ," + " total_visits ,"  + " curr_code ," + " fee_per_holder ," + " fee_per_guest ,"
              + " total_fee ," + " total_free_guests ," + " free_guests_value ," + " tot_charg_guest ,"
              + " charg_guest_value ," + " billing_region ," + " terminal_no ," + " use_city ,"
              + " id_no ," + " id_no_code ," + " id_p_seqno ," + " free_use_cnt ," + " guest_free_cnt ,"
              + " ch_cost_amt ," + " guest_cost_amt ," + " card_no ," + " mcht_no ," + " user_remark ,"
              + " crt_user ," + " imp_file_name ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
              + " mod_seqno ," + "vip_kind,"+"item_no,"+"pymt_cond,"+"err_code,"+"err_desc,"+"pymt_fail_count,"+"pymt_fail_person_count,"+"pymt_fail_tot_count,"+"in_person_count) values ( " + " :crt_date ," + " :bin_type ," + " :data_seqno ,"
              + " :from_type ," + " :bank_name ," + " :deal_type ," + " :associate_code ,"
              + " :ica_no ," + " :pp_card_no ," + " :ch_ename ," + " :visit_date ," + " :lounge_name ,"
              + " :lounge_code ," + " :domestic_int ," + " :iso_conty ," + " :iso_conty_code ,"
              + " :ch_visits ," + " :guests_count ," + " :total_visits ,"
              + " :curr_code ," + " :fee_per_holder ,"
              + " :fee_per_guest ," + " :total_fee ," + " :total_free_guests ,"
              + " :free_guests_value ," + " :tot_charg_guest ," + " :charg_guest_value ,"
              + " :billing_region ," + " :terminal_no ," + " :use_city ," + " :id_no ,"
              + " :id_no_code ," + " :id_p_seqno ," + " :free_use_cnt ," + " :guest_free_cnt ,"
              + " :ch_cost_amt ," + " :guest_cost_amt ," + " :card_no ," +":mcht_no,"+"'',"
              + " :crt_user ," + " :imp_file_name ," + " :mod_user ," + " sysdate ," + " :mod_pgm ,"
              + " 1 ," + ":vip_kind," + ":item_no,"+":pymt_cond,"+":err_code,"+":err_desc,"+":pymt_fail_count,"+":pymt_fail_person_count,"+":pymt_fail_tot_count,"+":in_person_count) ";
    }

    setString2("crt_date", wp.colStr(ll, "crt_date"));
    setString2("bin_type", wp.colStr(ll, "bin_type"));
    setDouble2("data_seqno", wp.colNum(ll, "data_seqno"));
    setString2("from_type", wp.colStr(ll, "from_type"));
    setString2("bank_name", wp.colStr(ll, "bank_name"));
    setString2("deal_type", wp.colStr(ll, "deal_type"));
    setString2("associate_code", wp.colStr(ll, "associate_code"));
    setString2("ica_no", wp.colStr(ll, "ica_no"));
    setString2("pp_card_no", wp.colStr(ll, "pp_card_no"));
    setString2("ch_ename", wp.colStr(ll, "ch_ename"));
    setString2("visit_date", wp.colStr(ll, "visit_date"));
    setString2("lounge_name", commString.left(wp.colStr(ll, "lounge_name"), 50));
    setString2("lounge_code", wp.colStr(ll, "lounge_code"));
    setString2("domestic_int", wp.colStr(ll, "domestic_int"));
    setString2("iso_conty", wp.colStr(ll, "iso_conty"));
    setString2("iso_conty_code", wp.colStr(ll, "iso_conty_code"));
    setDouble2("ch_visits", wp.colNum(ll, "ch_visits"));
    setDouble2("guests_count", wp.colNum(ll, "guests_count"));
    setDouble2("total_visits", wp.colNum(ll, "total_visits"));
    setDouble2("in_person_count", wp.colNum(ll, "in_person_count"));
    //setDouble2("batch_no", wp.colNum(ll, "batch_no"));
    setString2("voucher_no", wp.colStr(ll, "voucher_no"));
    setString2("mc_billing_region", wp.colStr(ll, "mc_billing_region"));
    setString2("curr_code", wp.colStr(ll, "curr_code"));
    setDouble2("fee_per_holder", wp.colNum(ll, "fee_per_holder"));
    setDouble2("fee_per_guest", wp.colNum(ll, "fee_per_guest"));
    setDouble2("total_fee", wp.colNum(ll, "total_fee"));
    setDouble2("total_free_guests", wp.colNum(ll, "total_free_guests"));
    setDouble2("free_guests_value", wp.colNum(ll, "free_guests_value"));
    setDouble2("charg_guest_value", wp.colNum(ll, "charg_guest_value"));
    setDouble2("tot_charg_guest", wp.colNum(ll, "tot_charg_guest"));

    setString2("billing_region",wp.colStr(ll, "billing_region"));
    setString2("terminal_no", commString.left(wp.colStr(ll, "terminal_no"), 30));
    setString2("use_city", wp.colStr(ll, "use_city"));
    setString2("id_no", wp.colStr(ll, "id_no"));
    setString2("id_no_code", wp.colStr(ll, "id_no_code"));
    setString2("id_p_seqno", wp.colStr(ll, "id_p_seqno"));
    setDouble2("free_use_cnt", wp.colNum(ll, "free_use_cnt"));
    setDouble2("guest_free_cnt", wp.colNum(ll, "guest_free_cnt"));
    setDouble2("ch_cost_amt", wp.colNum(ll, "ch_cost_amt"));
    setDouble2("guest_cost_amt", wp.colNum(ll, "guest_cost_amt"));
    setDouble2("in_person_count", wp.colNum(ll, "in_person_count"));
    setDouble2("pymt_fail_count", wp.colNum(ll, "pymt_fail_count"));
    setDouble2("pymt_fail_person_count", wp.colNum(ll, "pymt_fail_person_count"));
    setDouble2("pymt_fail_tot_count", wp.colNum(ll, "pymt_fail_tot_count"));

    setString("pymt_cond", wp.colStr(ll, "pymt_cond"));
    setString("err_code", wp.colStr(ll, "err_code"));
    System.out.println("err_code"+err_code);
    setString("err_desc", wp.colStr(ll, "err_desc"));
    setString("pp_id", wp.colStr(ll, "pp_id"));
    setString("ppcard_credit_cardno", wp.colStr(ll, "ppcard_credit_vardno"));
    setString2("card_no", wp.colStr(ll, "card_no"));
    setString2("mcht_no", wp.colStr(ll, "mcht_no"));
    setString2("crt_user", modUser);
    setString2("imp_file_name", wp.colStr(ll, "imp_file_name"));
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);
    errmsg("vip_kind "+vip_kind);
    System.out.println("vip_kind "+vip_kind);
    if("1".equals(vip_Kind)) {
      setString("vip_kind", "1");
      setString("item_no", "11");
    }
    else if("2".equals(vip_Kind)) {
      setString("vip_kind", "2");
      setString("item_no", "10");
    }


    sqlExec(isSqlVisit);

    if (sqlRowNum <= 0) {
      errmsg("insert cms_ppcard_visit error ");
      rc=-1;
    }

    else {
      rc=1;
    }

    System.out.println("rc"+rc);

    return rc;
  }

}
