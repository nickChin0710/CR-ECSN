/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-05  V1.00.00  OrisChang  program initial                            *
 * 111/10/23  V1.00.01  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/

package actq01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import java.util.*;

public class Actq0050 extends BaseEdit {
    CommString commString = new CommString();
    taroko.base.CommDate commDate = new taroko.base.CommDate();
    String mAccttype = "";
    String mAcctkey = "";
    String pPSeqno = "";
    String mProgName = "actq0050";
    HashMap<String,Double>  acagHash = new  HashMap<String,Double>();
    long hMpAmt = 0;

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) {
            // -資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        }

        dddwSelect();
        initButton();
    }

    @Override
    public void dddwSelect() {
        try {
            wp.optionKey = wp.colStr("ex_acct_type");
            this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
        } catch (Exception ex) {}
    }

    @Override
    public void queryFunc() throws Exception {
        if(empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_card_no"))) {
            alertErr("帳號, 卡號不可均為空白");
            return;
        }

        //start-查詢權限檢查，參考【f_auth_query】
        String lmQryKey = "";
        ColFunc colfunc =new ColFunc();
        colfunc.setConn(wp);
        if (!empty(wp.itemStr("ex_acct_key"))) {
            lmQryKey = wp.itemStr("ex_acct_key");
        }
        else if (!empty(wp.itemStr("ex_card_no"))) {
            lmQryKey = wp.itemStr("ex_card_no");
        }
        else {
            lmQryKey = "";
        }

        if (colfunc.fAuthQuery(mProgName, lmQryKey)!=1) {
            alertErr(colfunc.getMsg());
            return;
        }
        //end-查詢權限檢查，參考【f_auth_query】


        // 設定queryRead() SQL條件
        String lsAcnoPSeqno = getInitParm();

        if (lsAcnoPSeqno.equals("")) {
            alertErr("無此帳號/卡號");
            return;
        }

        if (!lsAcnoPSeqno.equals("")) {
            getDtlData(lsAcnoPSeqno);
        }

        //p_p_seqno = ls_p_seqno;
        pPSeqno = wp.colStr("q_p_seqno");

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.setQueryMode();

        wp.selectSQL = "a.acct_status, a.stmt_cycle, a.line_of_credit_amt, a.stop_status, b.block_status, " +
                " nvl( a.payment_rate1,' ') as pr1 ,nvl( a.payment_rate2,' ') as pr2 ,nvl( a.payment_rate3,' ') as pr3 ,       " +
                " nvl( a.payment_rate4,' ') as pr4 ,nvl( a.payment_rate5,' ') as pr5 ,nvl( a.payment_rate6,' ') as pr6 ,       " +
                " nvl( a.payment_rate7,' ') as pr7 ,nvl( a.payment_rate8,' ') as pr8 ,nvl( a.payment_rate9,' ') as pr9 ,       " +
                " nvl( a.payment_rate10,' ') as pr10 ,nvl( a.payment_rate11,' ') as pr11 ,nvl( a.payment_rate12,' ') as pr12 , " +
                " nvl( a.payment_rate13,' ') as pr13 ,nvl( a.payment_rate14,' ') as pr14 ,nvl( a.payment_rate15,' ') as pr15 , " +
                " nvl( a.payment_rate16,' ') as pr16 ,nvl( a.payment_rate17,' ') as pr17 ,nvl( a.payment_rate18,' ') as pr18 , " +
                " nvl( a.payment_rate19,' ') as pr19 ,nvl( a.payment_rate20,' ') as pr20 ,nvl( a.payment_rate21,' ') as pr21 , " +
                " nvl( a.payment_rate22,' ') as pr22 ,nvl( a.payment_rate23,' ') as pr23 ,nvl( a.payment_rate24,' ') as pr24 , " +
                " nvl( a.payment_rate25,' ') as pr25 ";

        wp.daoTable = "act_acno a, cca_card_acct b";

        wp.whereStr = "where 1=1 "
                + " and a.acno_p_seqno = b.acno_p_seqno "
                + " and a.acno_p_seqno = :p_seqno ";
        setString("p_seqno", pPSeqno);

        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料");
        }
        listWkdata();
        dataRead();
    }

    void listWkdata() throws Exception {
        String ss = "";
        ss =wp.colStr("acct_status");
        wp.colSet("tt_acct_status", commString.decode(ss, ",1,2,3,4", ",1-正常,2-逾放,3-催收,4-呆帳"));

        String[] cde=new String[]{"11","12","21","22"};
        String[] txt=new String[]{"11-人工禁超","12-系統禁超","21-人工解超","22-系統解超"};
        ss =wp.colStr("block_status");
        wp.colSet("tt_block_status", commString.decode(ss, cde, txt));
    }

    private String getInitParm() throws Exception {
        String lsSql = "";

        //ls_sql  = " select acct_type, acct_key, acno_p_seqno, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
        lsSql  = " select acct_type, acct_key, acno_p_seqno, p_seqno ";
        lsSql += " from act_acno ";
        lsSql += " where 1=1 ";
        lsSql += " and p_seqno = acno_p_seqno ";

        mAccttype = wp.itemStr("ex_acct_type");
        mAcctkey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));

        if (empty(mAcctkey) == false) {
            lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
            setString("acct_type", mAccttype);
            setString("acct_key", mAcctkey);
        } else {
            lsSql += "and acno_p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
            setString("card_no", wp.itemStr("ex_card_no"));
        }

        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
            wp.colSet("h_acct_type", sqlStr("acct_type"));
            wp.colSet("ex_acct_type", sqlStr("acct_type"));
            wp.colSet("f", sqlStr("acct_key"));
            wp.colSet("q_p_seqno", sqlStr("p_seqno"));
            wp.colSet("q_acno_p_seqno", sqlStr("acno_p_seqno"));
            //wp.colSet("q_id_cname", sqlStr("acno_cname"));
            //wp.colSet("q_corp_cname", sqlStr("acno_cname"));
            wp.colSet("ex_acct_type", sqlStr("acct_type"));
            wp.colSet("ex_acct_key", sqlStr("acct_key"));
            return sqlStr("acno_p_seqno");
        }
        return "";
    }

    private void getDtlData(String acnoPSeqno) throws Exception {
        String lsSql = "";
        lsSql = "select b.id_no, b.id_no_code, b.birthday, d.this_acct_month, " +
                " uf_corp_name(a.corp_p_seqno) as acno_cname, uf_idno_name(a.id_p_seqno) as acno_iname " +
                " from ptr_workday d, act_acno a " +
                " left join crd_idno b on b.id_p_seqno = a.id_p_seqno " +
                " where a.stmt_cycle = d.stmt_cycle " +
                " and a.acno_p_seqno = :acno_p_seqno ";
        setString("acno_p_seqno", acnoPSeqno);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
            wp.colSet("dsp_id_no", sqlStr("id_no"));
            wp.colSet("dsp_id_no_code", sqlStr("id_no_code"));
            wp.colSet("dsp_birthday", sqlStr("birthday"));
            wp.colSet("dsp_acct_month", sqlStr("this_acct_month"));
            wp.colSet("this_acct_month", sqlStr("this_acct_month"));
            wp.colSet("q_id_cname", sqlStr("acno_iname"));
            wp.colSet("q_corp_cname", sqlStr("acno_cname"));

        }
    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        this.selectNoLimit();

        wp.selectSQL = "acct_month, pay_amt ";

        wp.daoTable = "act_acag";

        wp.whereStr =" where p_seqno = :p_seqno ";
        setString("p_seqno", pPSeqno);

        wp.whereOrder =" order by acct_month DESC ";

        String lsAlertMsg = "";
        pageQuery();
        if (sqlNotFind()) {
            alertErr("無帳齡資料");
            if (!empty(pPSeqno)) {
                lsAlertMsg = "無帳齡資料";
                wp.errMesg =lsAlertMsg;//若前面有執行過 alertErr("xxx")，執行此codes可覆蓋"資料錯誤:"的字樣，
                //此行程式碼不執行 prompt window
            }
        }
        wp.setListCount(1);

        hMpAmt = 0;
        wfAcagHashSet();
        wp.colSet("q_mp", numToStr(hMpAmt, "###,###,###,##0"));
        wfGetMcode();

    }

    void wfAcagHashSet() throws Exception {
        //double ls_pay_amt = 0, tw_amt_sub = 0;
        double lsPayAmt = 0;
        String lsKey  = "";

        for (int ii = 0; ii < wp.listCount[0]; ii++) {
            lsKey  = wp.colStr(ii,"acct_month");
            lsPayAmt = commString.strToNum(wp.colStr(ii,"pay_amt"));
            hMpAmt += lsPayAmt;
            Double twAmtSub = (Double)acagHash.get(lsKey);
            if ( twAmtSub == null )
            { acagHash.put(lsKey,lsPayAmt); }
            else
            { twAmtSub +=  lsPayAmt;
                acagHash.put(lsKey,twAmtSub); }
        }
    }

    void wfGetMcode() throws Exception {
        //String ls_cycl_ym = "" , ls_acct_ym="" , ldt_val1 ="" , ldt_val2 ="" , tt_mcode="" ;
        String lsCyclYm = "" , ldtVal1 ="" , ldtVal2 ="" , ttMcode="" ;
        double ldcMpamt = 0 , lmAmt = 0 ;
        int liMcode = 0 ;
        lsCyclYm = wp.colStr("this_acct_month");
        if(empty(lsCyclYm)){
            alertErr("未取得關帳週期年月");
            return ;
        }

        String sql1 = " select "
                + " mix_mp_balance "
                + " from ptr_actgeneral "
                + " where 1=1 "
                +commSqlStr.rownum(1)
                ;

        sqlSelect(sql1);

        if(sqlRowNum<=0){
            ldcMpamt = 0 ;
        }	else	{
            ldcMpamt = sqlNum("mix_mp_balance");
        }

        String lsMinAcctYymm = "" ;

        for ( Map.Entry m : acagHash.entrySet() )
        {
            String lsAcctYm = (String)m.getKey();
            if(lsAcctYm.compareTo(lsCyclYm)>=0)	continue;

            lmAmt = (Double)acagHash.get(lsAcctYm);
            if(lmAmt <= ldcMpamt)	continue;

            if(lmAmt > ldcMpamt) {
                if(empty(lsMinAcctYymm)) {
                    lsMinAcctYymm = lsAcctYm;
                }
                else if(commString.strToNum(lsAcctYm) < commString.strToNum(lsMinAcctYymm)) {
                    lsMinAcctYymm = lsAcctYm;
                }
            }
        }

        if(empty(lsMinAcctYymm))	liMcode=0;
        else	{
            ldtVal1 = lsCyclYm+"01";
            ldtVal2 = lsMinAcctYymm+"01";
            liMcode = commDate.monthsBetween(ldtVal1,ldtVal2);
        }

        //tt_mcode = String.format("%02d", li_mcode);
        ttMcode = String.format("%3d", liMcode);
        wp.colSet("q_mcode", ttMcode);

    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    String fillZeroAcctKey(String acctkey) throws Exception {
        String rtn = acctkey;
        if (acctkey.trim().length()==8) rtn += "000";
        if (acctkey.trim().length()==10) rtn += "0";

        return rtn;
    }
}