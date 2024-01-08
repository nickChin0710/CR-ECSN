package cmsm03;
/**
 * 2019-0614: JH p_xxx >>acno_pxxx
 * 2023-1119: Zuwei Su 新模板格式
 */

import busi.FuncAction;
import taroko.base.CommString;

public class Cmsm5030Func extends FuncAction {
    CommString commStr = new CommString();
    String isSqlVisit = "";
    String rowid = "", idPSeqno = "", majorIdPSeqno = "", chiName = "", dataSeqno = "";
    String acctType = "", acnoPSeqno = "", majorCardNo = "", groupCode = "", stmtCycle = "";
    int iiBatchSeq = 0;
    String isBatchNo = "";

    // =============================================================
    @Override
    public void dataCheck() {
        // TODO Auto-generated method stub

    }

    void initData() {
        acctType = "";
        acnoPSeqno = "";
        majorCardNo = "";
        groupCode = "";
        stmtCycle = "";
        idPSeqno = "";
        majorIdPSeqno = "";
        chiName = "";
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
        msgOK();
        String lsFileType = wp.itemStr("ex_file_type");
        String lsBatchNo = wp.itemStr("ex_batch_no");
        if (empty(lsFileType) || empty(lsBatchNo)) {
            errmsg("[檔案類別, 匯入批號] 不可空白");
            return rc;
        }

        strSql = "delete bil_mcht_apply_tmp where batch_no =? and file_type =?";
        setString(1, lsBatchNo);
        setString(lsFileType);
        sqlExec(strSql);
        if (sqlRowNum < 0) {
            sqlErr("delete bil_mcht_apply_tmp error");
            return rc;
        }

        errmsg("刪除筆數=[%s]", sqlRowNum);
        rc = 1;
        return rc;
    }

    @Override
    public int dataProc() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int insertVist(int ll) throws Exception {
        msgOK();

        if (empty(isSqlVisit)) {
            isSqlVisit = " insert into cms_ppcard_visit ("
                    + " crt_date ,"
                    + " bin_type ,"
                    + " data_seqno ,"
                    + " from_type ,"
                    + " bank_name ,"
                    + " deal_type ,"
                    + " associate_code ,"
                    + " ica_no ,"
                    + " pp_card_no ,"
                    + " ch_ename ,"
                    + " visit_date ,"
                    + " lounge_name ,"
                    + " lounge_code ,"
                    + " domestic_int ,"
                    + " iso_conty ,"
                    + " iso_conty_code ,"
                    + " ch_visits ,"
                    + " guests_count ,"
                    + " total_visits ,"
                    + " batch_no ,"
                    + " voucher_no ,"
                    + " mc_billing_region ,"
                    + " curr_code ,"
                    + " fee_per_holder ,"
                    + " fee_per_guest ,"
                    + " total_fee ,"
                    + " total_free_guests ,"
                    + " free_guests_value ,"
                    + " tot_charg_guest ,"
                    + " charg_guest_value ,"
                    + " billing_region ,"
                    + " terminal_no ,"
                    + " use_city ,"
                    + " id_no ,"
                    + " id_no_code ,"
                    + " id_p_seqno ,"
                    + " free_use_cnt ,"
                    + " guest_free_cnt ,"
                    + " ch_cost_amt ,"
                    + " guest_cost_amt ,"
                    + " card_no ,"
                    + " mcht_no ,"
                    + " user_remark ,"
                    + " crt_user ,"
                    + " imp_file_name ,"
                    + " mod_user ,"
                    + " mod_time ,"
                    + " mod_pgm ,"
                    + " mod_seqno "
                    + " ) values ( "
                    + " :crt_date ,"
                    + " :bin_type ,"
                    + " :data_seqno ,"
                    + " :from_type ,"
                    + " :bank_name ,"
                    + " :deal_type ,"
                    + " :associate_code ,"
                    + " :ica_no ,"
                    + " :pp_card_no ,"
                    + " :ch_ename ,"
                    + " :visit_date ,"
                    + " :lounge_name ,"
                    + " :lounge_code ,"
                    + " :domestic_int ,"
                    + " :iso_conty ,"
                    + " :iso_conty_code ,"
                    + " :ch_visits ,"
                    + " :guests_count ,"
                    + " :total_visits ,"
                    + " :batch_no ,"
                    + " :voucher_no ,"
                    + " :mc_billing_region ,"
                    + " :curr_code ,"
                    + " :fee_per_holder ,"
                    + " :fee_per_guest ,"
                    + " :total_fee ,"
                    + " :total_free_guests ,"
                    + " :free_guests_value ,"
                    + " :tot_charg_guest ,"
                    + " :charg_guest_value ,"
                    + " :billing_region ,"
                    + " :terminal_no ,"
                    + " :use_city ,"
                    + " :id_no ,"
                    + " :id_no_code ,"
                    + " :id_p_seqno ,"
                    + " :free_use_cnt ,"
                    + " :guest_free_cnt ,"
                    + " :ch_cost_amt ,"
                    + " :guest_cost_amt ,"
                    + " :card_no ,"
                    + " :mcht_no ,"
                    + " '' ,"
                    + " :crt_user ,"
                    + " :imp_file_name ,"
                    + " :mod_user ,"
                    + " sysdate ,"
                    + " :mod_pgm ,"
                    + " 1 "
                    + " ) ";
        }

        setString("crt_date", wp.colStr(ll, "crt_date"));
        setString("bin_type", wp.colStr(ll, "bin_type"));
        setNumber("data_seqno", wp.colNum(ll, "data_seqno"));
        setString("from_type", wp.colStr(ll, "from_type"));
        setString("bank_name", wp.colStr(ll, "bank_name"));
        setString("deal_type", wp.colStr(ll, "deal_type"));
        setString("associate_code", wp.colStr(ll, "associate_code"));
        setString("ica_no", wp.colStr(ll, "ica_no"));
        setString("pp_card_no", wp.colStr(ll, "pp_card_no"));
        setString("ch_ename", wp.colStr(ll, "ch_ename"));
        setString("visit_date", wp.colStr(ll, "visit_date"));
        setString("lounge_name", commStr.left(wp.colStr(ll, "lounge_name"), 50));
        setString("lounge_code", wp.colStr(ll, "lounge_code"));
        setString("domestic_int", wp.colStr(ll, "domestic_int"));
        setString("iso_conty", wp.colStr(ll, "iso_conty"));
        setString("iso_conty_code", wp.colStr(ll, "iso_conty_code"));
        setNumber("ch_visits", wp.colNum(ll, "cardholder_visits"));
        setNumber("guests_count", wp.colNum(ll, "guests_count"));
        setNumber("total_visits", wp.colNum(ll, "total_visits"));
        setNumber("batch_no", wp.colNum(ll, "batch_no"));
        setString("voucher_no", wp.colStr(ll, "voucher_no"));
        setString("mc_billing_region", wp.colStr(ll, "mc_billing_region"));
        setString("curr_code", wp.colStr(ll, "curr_code"));
        setNumber("fee_per_holder", wp.colNum(ll, "fee_per_holder"));
        setNumber("fee_per_guest", wp.colNum(ll, "fee_per_guest"));
        setNumber("total_fee", wp.colNum(ll, "total_fee"));
        setNumber("total_free_guests", wp.colNum(ll, "total_free_guests"));
        setNumber("free_guests_value", wp.colNum(ll, "free_guests_value"));
        setNumber("tot_charg_guest", wp.colNum(ll, "tot_charg_guest"));
        setNumber("charg_guest_value", wp.colNum(ll, "charg_guest_value"));
        setString("billing_region", wp.colStr(ll, "billing_region"));
        setString("terminal_no", commStr.left(wp.colStr(ll, "terminal_no"), 30));
        setString("use_city", wp.colStr(ll, "use_city"));
        setString("id_no", wp.colStr(ll, "id_no"));
        setString("id_no_code", wp.colStr(ll, "id_no_code"));
        setString("id_p_seqno", wp.colStr(ll, "id_p_seqno"));
        setNumber("free_use_cnt", wp.colNum(ll, "free_use_cnt"));
        setNumber("guest_free_cnt", wp.colNum(ll, "guest_free_cnt"));
        setNumber("ch_cost_amt", wp.colNum(ll, "ch_cost_amt"));
        setNumber("guest_cost_amt", wp.colNum(ll, "guest_cost_amt"));
        setString("card_no", wp.colStr(ll, "card_no"));
        setString("mcht_no", wp.colStr(ll, "mcht_no"));
        setString("crt_user", modUser);
        setString("imp_file_name", wp.colStr(ll, "imp_file_name"));
        setString("mod_user", modUser);
        setString("mod_pgm", modPgm);

        // var2Parm_ss("crt_date");
        // var2Parm_ss("bin_type");
        // var2Parm_num("data_seqno");
        // var2Parm_ss("from_type");
        // var2Parm_ss("bank_name");
        // var2Parm_ss("deal_type");
        // var2Parm_ss("associate_code");
        // var2Parm_ss("ica_no");
        // var2Parm_ss("pp_card_no");
        // var2Parm_ss("ch_ename");
        // var2Parm_ss("visit_date");
        // var2Parm_ss("lounge_name");
        // var2Parm_ss("lounge_code");
        // var2Parm_ss("domestic_int");
        // var2Parm_ss("iso_conty");
        // var2Parm_ss("iso_conty_code");
        // var2Parm_num("ch_visits");
        // var2Parm_num("guests_count");
        // var2Parm_num("total_visits");
        // var2Parm_num("batch_no");
        // var2Parm_ss("voucher_no");
        // var2Parm_ss("mc_billing_region");
        // var2Parm_ss("curr_code");
        // var2Parm_num("fee_per_holder");
        // var2Parm_num("fee_per_guest");
        // var2Parm_num("total_fee");
        // var2Parm_num("total_free_guests");
        // var2Parm_num("free_guests_value");
        // var2Parm_num("tot_charg_guest");
        // var2Parm_num("charg_guest_value");
        // var2Parm_ss("billing_region");
        // var2Parm_ss("terminal_no");
        // var2Parm_ss("use_city");
        // var2Parm_ss("id_no");
        // var2Parm_ss("id_no_code");
        // var2Parm_ss("id_p_seqno");
        // var2Parm_num("free_use_cnt");
        // var2Parm_num("guest_free_cnt");
        // var2Parm_num("ch_cost_amt");
        // var2Parm_num("guest_cost_amt");
        // var2Parm_ss("card_no");
        // var2Parm_ss("mcht_no");
        // var2Parm_ss("crt_user");
        // var2Parm_ss("imp_file_name");
        // setString("mod_user",wp.loginUser);
        // setString("mod_pgm",wp.mod_pgm());

        sqlExec(isSqlVisit);

        if (sqlRowNum <= 0) {
            errmsg("insert cms_ppcard_visit error ");
        }

        return rc;
    }

}