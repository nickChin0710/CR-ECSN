/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/03  V1.00.01  Pino       Initial 提供製卡徵審進件使用                                                           *
* 109/01/16  V1.00.02  Pino       製卡徵審進件程式名稱改為CrdE001                    *
* 109-12-03  V1.00.03     tanwei   updated for project coding standard       *
*  109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          *                                                                           *
******************************************************************************/
package Rds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.BaseBatch;

@SuppressWarnings("unchecked")
public class RdsB020 extends BaseBatch{
    private  String progname = "道路救援登錄作業(batch) 109/12/30 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    hdata.CmsRoadmaster hRoad = new hdata.CmsRoadmaster();
    hdata.CmsRoaddetail hRode = new hdata.CmsRoaddetail();
    hdata.CrdCard hCard = new hdata.CrdCard();
    hdata.CrdIdno hIdno = new hdata.CrdIdno();
    hdata.CmsRoadparm2 hRoadparm2 = new hdata.CmsRoadparm2();
	com.DataSetWeb idsParm = new com.DataSetWeb();

    public String hCallBatchSeqno = "";
    public String hIdPSeqno = "";
    public String hCardNo = "";
    public String hCarNo = "";
    public String hPgmName = "";
    public int hSeqno = 0;
    public String hRdsPcard = "";
    public String hRmCarmanname = "";
    public String hRmCarmanid = "";
    public String isChType = ""; // A:首年   B:非首年   C:非首年:MIN(發卡日)in今年-
    public String isRentCar = "N";
    public String hErrReason = "";
    public String changeCardFlag = "N";
    public String hProcDate = "";
    public String hBusinessDate = "";
    public String hSystemDate = "";
    public String hTempUser = "";
    public int idcPurchAmt = 0;
    public int ilPurchCnt = 0;
    public int idcLastAmt = 0;
    public int tmpInt = 0;
// ************************************************************************
//  public static void main(String[] args) throws Exception {
//      RdsB020 proc = new RdsB020();
//      
//      proc.mainProcess(args);
//      proc.system_Exit(0);
//  }
// ************************************************************************
    public int ReceiveRoadCar(String iIdPSeqno,String iCardNo,String iCarNo,String iPgmName) throws Exception {
        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
            return 1;
        }

        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        hIdPSeqno = iIdPSeqno;
        hCardNo = iCardNo;
        hCarNo = iCarNo;
        hPgmName = iPgmName;
        allInit();
            if(checkCardNo()==false){
                commitDataBase();
                return 1;
            }
            if(checkIdPSeqNo()==false){
                commitDataBase();
                return 1;
            }
            if(checkCmsRoadmaster()==false){
                commitDataBase();
                return 1;
            }
            if(checkRentCarNo(hCarNo)>0) {
                commitDataBase();
                return 1;
            }
            if(hasFree()<=0) {
                commitDataBase();
                return 1;
            }
            if(checkPtrCardType()==false) {
                commitDataBase();
                return 1;
            }
            //--有效期限 預設到明年12月 若為免費則改為卡片效期
            if(freeIsAuto(hCardNo)==1) { 
                hRode.rdValiddate = hCard.newEndDate.substring(0, 6);
                hRoad.rmValiddate = hCard.newEndDate.substring(0, 6);
            }else if (freeIsAuto(hCardNo)==0) {
                String lsDate = "";
                lsDate = commDate.dateAdd(sysDate, 1, 0, 0).substring(0, 6);  
                hRode.rdValiddate = commString.mid(lsDate, 0,4)+"12";
                hRoad.rmValiddate = commString.mid(lsDate, 0,4)+"12";
            }else {         
                return 1;
            }

            switch (checkRoadparm2()) {
            case -1:
                return 1;
            case 0:
                if(hPgmName.equals("CrdE001")) { //徵審進件 消費不足狀態給4:未啟用  V1.00.02
                    hRode.rdStatus = "4";
                    hRoad.rmStatus = "4";                 
                }else {
                    hRode.rdStatus = "0"; //其他進件 消費不足狀態給0:停用
                    hRoad.rmStatus = "0";
                }
                hRoad.rmReason = "";
                if(changeCardFlag=="N") { 
                    insertCmsRoaddetail();
                    insertCmsRoadmaster();
                    commitDataBase();
                    return 0;
                }else {
                    insertCmsRoaddetailC();
                    updateCmsRoadmaster();
                    commitDataBase();
                    return 0;
                }
            case 1:
                hRode.rdStatus = "1";
                hRoad.rmStatus = "1";
                hRoad.rmReason = "";
                if(changeCardFlag=="N") { 
                    insertCmsRoaddetail();
                    insertCmsRoadmaster();
                    commitDataBase();
                    return 0;
                }else {
                    insertCmsRoaddetailC();
                    updateCmsRoadmaster();
                    commitDataBase();
                    return 0;
                }
            }
            return 0;

//自費  參考rdsm0020_func           
//          /*
//          if(eq_igno(wp.item_ss("rd_type"),"F")){             
//              if(wf_chk_roadfree()==-1)   return;             
//          }   else if(eq_igno(wp.item_ss("rd_type"),"E")){
//              if(wf_chk_roadexpend()==-1) return ;
//          }
//          */
    }
    // ************************************************************************
    public boolean checkIdPSeqNo() throws Exception {
        if(hIdPSeqno.length()<0) {
            hErrReason = "卡人流水號碼(ID_P_SEQNO)不可空白";
            insertCmsRoadmasterLog();
            return false;
        }    
        selectSQL = " count(*) cnt";
        daoTable = "crd_idno";
        whereStr = "WHERE id_p_seqno   = ?  ";
        setString(1, hIdPSeqno);
        int recCnt = selectTable();
        recCnt = getValueInt("cnt");
        if(recCnt==0) {
            hErrReason = "卡人流水號碼(ID_P_SEQNO)不存在";
            insertCmsRoadmasterLog();
            return false;
        }
        return true;
    }
 // ************************************************************************
    public boolean checkCardNo() throws Exception {
        if(hCardNo.length()<1) {
            hErrReason = "卡號不可為空白";
            insertCmsRoadmasterLog();
            return false;
        }
        selectSQL = " current_code ";
        daoTable = "crd_card";
        whereStr = "WHERE card_no   = ?  ";
        setString(1, hCardNo);
        int recCnt = selectTable();
        if(recCnt==0) {
            hErrReason = "卡號不存在";
            insertCmsRoadmasterLog();
            return false;
        }else {
            if (!getValue("current_code").equals("0")) {
                hErrReason = "此卡為無效卡,不可登錄";
                insertCmsRoadmasterLog();
                return false;
            }
        }
        return true;
    }
 // ************************************************************************
    public void selectCrdCardIdno() throws Exception {
        selectSQL = " * ";
        daoTable = " crd_idno a , crd_card b ";
        whereStr = " where a.id_p_seqno = b.id_p_seqno "
                 + " and a.id_p_seqno = ? "
                 + " and b.current_code = '0' "
                 + " and b.card_no = ? ";
        setString(1, hIdPSeqno);
        setString(2, hCardNo);
        int recCnt = selectTable();
        if (notFound.equals("Y")||recCnt<1) {
            hErrReason = "卡人流水號碼(ID_P_SEQNO)比對不符";
            insertCmsRoadmasterLog();
        }
        hIdno.idNo             = getValue("id_no");
        hIdno.chiName          = getValue("chi_name");
        hIdno.homeAreaCode1   = getValue("home_area_code1");
        hIdno.homeTelNo1      = getValue("home_tel_no1");
        hIdno.homeTelExt1     = getValue("home_tel_ext1");
        hIdno.officeAreaCode1 = getValue("office_area_code1");
        hIdno.officeTelNo1    = getValue("office_tel_no1");
        hIdno.officeTelExt1   = getValue("office_tel_ext1");
        hIdno.cellarPhone      = getValue("cellar_phone");
        hIdno.idPSeqno        = getValue("id_p_seqno");
        hCard.idPSeqno        = getValue("id_p_seqno");
        hCard.majorCardNo     = getValue("major_card_no");
        hCard.cardType         = getValue("card_type");
        hCard.newEndDate      = getValue("new_end_date");
        hCard.groupCode        = getValue("group_code");
        hCard.acctType         = getValue("acct_type");
        hCard.corpPSeqno      = getValue("corp_p_seqno");
        hCard.corpNo           = getValue("corp_no");
        hCard.majorIdPSeqno  = getValue("major_id_p_seqno");
        hCard.currentCode      = getValue("current_code");
        hCard.pSeqno           = getValue("p_seqno");
        hCard.cardNo           = getValue("card_no");

    }
// ************************************************************************
    void selectRdsRoadparm2() throws Exception {
       String sql2 = " select "
             + " * "
             + " from cms_roadparm2 "
             + " where 1=1 and proj_no in "
             + " ( select distinct proj_no from cms_roadparm2_dtl "
             + " where acct_type = ? and card_type = ?"
             + " and uf_nvl(valid_end_date,'99991231') >= to_char(sysdate,'yyyymmdd') "
             + " and nvl(apr_flag,'N') = 'Y' ";
       ppp(1,hCard.acctType);
       ppp(2,hCard.cardType);
       if (!isEmpty(hCard.groupCode) && eq(hCard.groupCode,"0000")==false) {
          sql2 += " and decode(group_code,'',?,group_code) =?";
          ppp(hCard.groupCode);
          ppp(hCard.groupCode);
       }
       if (!isEmpty(hCard.corpNo)) {
          sql2 += " and decode(corp_no,'',?,group_code) =?";
          ppp(hCard.corpNo);
          ppp(hCard.corpNo);
       }
       sql2 +=" )";

       idsParm.colList = sqlQuery(sql2);
    }
    /************************************************************************/
    public List<Map<String,String>> sqlQuery(String sql1) throws Exception { //Pino add
        return sqlQuery(sql1,null);
    } 
    public List<Map<String, String>> sqlQuery(String sql1, Object[] param) throws Exception {
        sqlNrow = 0;

        // java.sql.ResultSet rs = null;
        ArrayList<Map<String, String>> colList = new ArrayList<>();

        if (empty(sql1) == false) {
            sqlCmd = sql1;
        }

        int tid = this.preparedStmtGet();
        sqlSelect(tid, param);
        if (sqlNrow <= 0)
            return colList;

        for (int ll = 0; ll < sqlNrow; ll++) {
            Map<String, String> map = new HashMap<>();
            for (int ii = 0; ii < columnCnt[tid]; ii++) {
                map.put(this.columnName[tid][ii], colSs(ll, this.columnName[tid][ii]));
            }
            colList.add(map);
        }

        return colList;
    }    
// ************************************************************************
//    public void select_rds_roadparm2_test() throws Exception {
//      selectSQL = " * ";
//        daoTable = "cms_roadparm2 ";
//        whereStr = "where 1=1 and proj_no in " 
//               + " ( select distinct proj_no from cms_roadparm2_dtl "
//                 + " where acct_type = ? and card_type = ? "
//                 + " and uf_nvl(valid_end_date,'99991231') >= to_char(sysdate,'yyyymmdd') "
//                 + " and nvl(apr_flag,'N') = 'Y' "
//                 + " and decode(group_code,'',cast(? as char(4)),group_code) = ? "
//                 + " and decode(corp_no,'',cast(? as char(8)),corp_no) = ? "
//                 + " )";
//        setString(1, h_card.acct_type);
//        setString(2, h_card.card_type);
//        setString(3, h_card.group_code);
//        setString(4, h_card.group_code);
//        setString(5, h_card.corp_no);
//        setString(6, h_card.corp_no);
//
//        select_roadparm2_cnt = openCursor();
//
//        while (fetchTable()) {
//            for (int inti=0;inti<select_roadparm2_cnt;inti++){
//              h_roadparm2.init_data();
//              h_roadparm2.proj_no           = getValue("proj_no",inti);
//              h_roadparm2.proj_desc         = getValue("proj_desc",inti);
//              h_roadparm2.fst_acct_code_ao  = getValue("fst_acct_code_ao",inti);
//              h_roadparm2.fst_acct_code_bl  = getValue("office_tel_no1",inti);
//              h_roadparm2.fst_acct_code_ca  = getValue("office_tel_ext1",inti);
//              h_roadparm2.fst_acct_code_id  = getValue("home_area_code1",inti);
//              h_roadparm2.fst_acct_code_it  = getValue("home_tel_ext1",inti);
//              h_roadparm2.fst_acct_code_ot  = getValue("cellar_phone",inti);
//              h_roadparm2.fst_acct_code_it_flag = getValue("id_p_seqno",inti);
//              h_roadparm2.fst_mm            = getValueInt("fst_mm",inti);
//              h_roadparm2.fst_one_low_amt   = getValueDouble("fst_one_low_amt",inti);
//              h_roadparm2.fst_amt_cond      = getValue("fst_amt_cond",inti);
//              h_roadparm2.fst_purch_amt     = getValueDouble("fst_purch_amt",inti);
//              h_roadparm2.fst_row_cond      = getValue("fst_row_cond",inti);
//              h_roadparm2.fst_purch_row     = getValueInt("fst_purch_row",inti);
//              h_roadparm2.lst_cond          = getValue("lst_cond",inti);
//              h_roadparm2.lst_acct_code_ao  = getValue("lst_acct_code_ao",inti);
//              h_roadparm2.lst_acct_code_bl  = getValue("lst_acct_code_bl",inti);
//              h_roadparm2.lst_acct_code_ca  = getValue("lst_acct_code_ca",inti);
//              h_roadparm2.lst_acct_code_id  = getValue("lst_acct_code_id",inti);
//              h_roadparm2.lst_acct_code_it  = getValue("lst_acct_code_it",inti);
//              h_roadparm2.lst_acct_code_ot  = getValue("lst_acct_code_ot",inti);
//              h_roadparm2.lst_acct_code_it_flag = getValue("lst_acct_code_it_flag",inti);
//              h_roadparm2.lst_mm            = getValueInt("lst_mm",inti);
//              h_roadparm2.lst_one_low_amt   = getValueDouble("lst_one_low_amt",inti);
//              h_roadparm2.lst_amt_cond      = getValue("lst_amt_cond",inti);
//              h_roadparm2.lst_purch_amt     = getValueDouble("lst_purch_amt",inti);
//              h_roadparm2.lst_row_cond      = getValue("lst_row_cond",inti);
//              h_roadparm2.lst_purch_row     = getValueInt("lst_purch_row",inti);
//              h_roadparm2.cur_cond          = getValue("cur_cond",inti);
//              h_roadparm2.cur_acct_code_ao  = getValue("cur_acct_code_ao",inti);
//              h_roadparm2.cur_acct_code_bl  = getValue("cur_acct_code_bl",inti);
//              h_roadparm2.cur_acct_code_ca  = getValue("cur_acct_code_ca",inti);
//              h_roadparm2.cur_acct_code_id  = getValue("cur_acct_code_id",inti);
//              h_roadparm2.cur_acct_code_it  = getValue("cur_acct_code_it",inti);
//              h_roadparm2.cur_acct_code_ot  = getValue("cur_acct_code_ot",inti);
//              h_roadparm2.cur_acct_code_it_flag = getValue("cur_acct_code_it_flag",inti);
//              h_roadparm2.cur_mm            = getValueInt("cur_mm",inti);
//              h_roadparm2.cur_one_low_amt   = getValueDouble("cur_one_low_amt",inti);
//              h_roadparm2.cur_amt_cond      = getValue("cur_amt_cond",inti);
//              h_roadparm2.cur_purch_amt     = getValueDouble("cur_purch_amt",inti);
//              h_roadparm2.cur_row_cond      = getValue("cur_row_cond",inti);
//              h_roadparm2.cur_purch_row     = getValueInt("cur_purch_row",inti);
//              h_roadparm2.amt_sum_flag      = getValue("amt_sum_flag",inti);
//            }
//        }
//
//  }

// ************************************************************************
    public boolean checkCmsRoadmaster() throws Exception {
        selectSQL = " count(*) cnt";
        daoTable = "cms_roadmaster";
        whereStr = "WHERE id_p_seqno   = ?  "
                 + " and card_no   = ?  "
                 + " and rm_type   = 'F' ";
        setString(1, hIdPSeqno);
        setString(2, hCardNo);
        int recCnt = selectTable();
        recCnt = getValueInt("cnt");
        if(recCnt>0) {
            hErrReason = "此卡已作過免費道路救援登錄";
            insertCmsRoadmasterLog();
            return false;
        }
        selectSQL = " * ";
        daoTable = "cms_roadmaster";
        whereStr = "WHERE id_p_seqno   = ?  "
                 + " and rm_type   = 'F' "
                 + " and rm_status = '0' ";
        setString(1, hIdPSeqno);
        recCnt = selectTable();
        if (notFound.equals("Y")||recCnt<1) {
            selectSQL = " count(*) cnt";
            daoTable = "cms_roadmaster";
            whereStr = "WHERE rm_carno   = ?  "
                     + " and rm_type = 'F'  "
                     + " and (nvl(rm_status,'0') <> '0' or (rm_status = '0' and rm_reason = '2')) ";
            setString(1, hCarNo);
            recCnt = selectTable();
            recCnt = getValueInt("cnt");
            if(recCnt>0) {
                hErrReason = "車號已登錄免費, 不可再重覆登錄";
                insertCmsRoadmasterLog();
                return false;
            }
        }else {
            if(!getValue("card_no").equals(hCardNo)) {
                changeCardFlag = "Y";             
                hRode.rdSeqno = 0;
                hRode.rdModdate = sysDate;
                hRode.rdModtype = "B";
                hRode.cardNo = getValue("card_no");
                hRode.newCardNo = hCardNo;
                hRode.rdType = "F";
                hRode.applCardNo = hCardNo;
                hRode.groupCode = getValue("group_code");
                hRode.rdCarno = getValue("rm_carno");
                hRode.rdCarmanname = getValue("rm_carmanname");
                hRode.rdCarmanid = getValue("rm_carmanid");
                hRode.rdNewcarno = "";
                hRode.rdHtelno1 = getValue("rm_htelno1");
                hRode.rdHtelno2 = getValue("rm_htelno2");
                hRode.rdHtelno3 = getValue("rm_htelno3");
                hRode.rdOtelno1 = getValue("rm_otelno1");
                hRode.rdOtelno2 = getValue("rm_otelno2");
                hRode.rdOtelno3 = getValue("rm_otelno3");
                hRode.cellarPhone = getValue("cellar_phone");
                hRode.rdValiddate = getValue("rm_validdate");
                hRode.rdStatus = "0"; //停用
                hRode.rdPayamt = getValueDouble("rm_payamt");
                hRode.rdPayno = getValue("rm_payno");
                hRode.rdPaydate = getValue("rm_paydate");
                hRode.rdStopdate = sysDate;
                hRode.rdStoprsn = "8";
                hRode.crtUser = getValue("crt_user");
                hRode.crtDate = getValue("crt_date");
                hRode.aprUser = getValue("apr_user");
                hRode.aprDate = getValue("apr_date");
                hRode.rdSenddate = "";
                hRode.rdSendsts = "";
                hRode.rdSendyn = "";
                hRode.rdSendadd = 0;
                hRode.rdSendstop = 0;
                hRode.projNo = "";
                hRode.purchAmt = 0;
                hRode.purchCnt = 0;
                hRode.purchAmtLyy = 0;
                hRode.cardholderType = "";
                hRode.modUser = "rdsb020";
                hRode.modTime = sysDate+sysTime;
                hRode.modPgm = hModPgm;
                hRode.rdsPcard = getValue("rds_pcard");
                hRode.idPSeqno = getValue("id_p_seqno");
                insertCmsRoaddetailBc();
                commitDataBase();
            }           
        }
        
        return true;
    }
// ************************************************************************
    public int checkRentCarNo(String hCarNo) throws Exception {
        //return 0:是租賃車  -1:不是租賃車  1:其它車號檢核錯誤
        String hCarNoTmp = "";
        int llCnt = 0;
        
        hCarNoTmp = hCarNo.trim();
        
        if (empty(hCarNoTmp)) {
            hErrReason = "車號不可空白";
            insertCmsRoadmasterLog();
            return 1;
        }

        // --check CMS_ROADDETAIL--------------------------------------------
        sqlCmd  = " select "
                + " count(*) as ll_cnt "
                + " from cms_roaddetail "
                + " where apr_date = '' "
                + " and rd_carno = ? ";
        setString(1, hCarNoTmp);
        int recCnt = selectTable();
        llCnt = getValueInt("ll_cnt");
        
        if (llCnt > 0) {
            hErrReason = "車號有異動 or 登錄, 請先作覆核";
            insertCmsRoadmasterLog();
            return 1;
        }

        sqlCmd  = " select "
                + " count(*) as ll_cnt "
                + " from cms_roaddetail "
                + " where apr_date = '' "
                + " and rd_newcarno = ? ";
        setString(1, hCarNoTmp);
        recCnt = selectTable();
        llCnt = getValueInt("ll_cnt");

        if (llCnt > 0) {
            hErrReason = "車號有異動 or 登錄, 請先作覆核";
            insertCmsRoadmasterLog();
            return 1;
        }

        if(hCarNo.length()==6) {
            if(hCarNo.substring(0, 1).equals(hCarNo.substring(1, 2))||hCarNo.substring(4, 5).equals(hCarNo.substring(5, 6))) {
                hRoad.rdsPcard = "L";
                hRode.rdsPcard = "L";
                isRentCar = "Y";
            return 0;
            }
        }else if(hCarNo.length()==7){
            if(hCarNo.substring(0, 1).equalsIgnoreCase("R")) {
                hRoad.rdsPcard = "L";
                hRode.rdsPcard = "L";
                isRentCar = "Y";
            return 0;
            }
        }else {
            return -1;
        }
        return -1;
    
    
    }

// ************************************************************************
    public boolean checkPtrCardType() throws Exception {
            selectSQL = " count(*) cnt";
            daoTable = "ptr_card_type";
            whereStr = "WHERE card_type   = ?  "
                     + " and rds_pcard not in ('A','I','P') ";
            setString(1, hCard.cardType);
            int recCnt = selectTable();
                recCnt = getValueInt("cnt");
                if(recCnt>0) {
                    hErrReason = "持卡人卡種消費資格不符免費道路救援-優惠別";
                    insertCmsRoadmasterLog();
                    return false;
                }
                return true;
    }

// ************************************************************************
// Rds_func 沒使用到
//    public int chk_acctamt_free(String as_card_no, String as_year) throws Exception {
//      // --check 免費道路救援資格--
//      // -- 1 -> 符合
//      // -- 2 -> 不符合
//      // -- -1 -> error
//      // ------------------------------------------------------------------------
//
//      String ls_p_seqno = "", ls_val1 = "", ls_val2 = "";
//      int ldc_amt1 = 0, ldc_amt2 = 0, ldc_amt3 = 0, ldc_amt4 = 0;
//      // --get P_SEQNO--      
//        sqlCmd  = " select ";
//        sqlCmd += " nvl(p_seqno,'') as ls_p_seqno ";
//        sqlCmd += " from crd_card ";
//        sqlCmd += " where card_no = ? ";
//        setString(1, as_card_no);
//        int recCnt = selectTable();
//        if (notFound.equals("Y")||recCnt<1) {
//            return -1;
//        }
//        ls_p_seqno = getValue("ls_p_seqno");
//
//      // --Get 消費金額:get anal_sub acctmm-1---------------------------------------
//      // --acctYY is 2001: 帳務年月 is 200012 to 200111---
//        
//        ls_val1 = Double.toString(Double.parseDouble(as_year)-1) + "12";
//      ls_val2 = as_year + "11";
//      
//      sqlCmd  = " select ";
//        sqlCmd += " sum(nvl(his_purchase_amt,0)) as ldc_amt1 , ";
//        sqlCmd += " sum(nvl(his_cash_amt,0)) as ldc_amt2 ";
//        sqlCmd += " from act_anal_sub ";
//        sqlCmd += " where p_seqno = ? ";
//        sqlCmd += " and acct_month between ? and ? ";
//        setString(1, ls_p_seqno);
//      setString(2, ls_val1);
//      setString(3, ls_val2);
//        recCnt = selectTable();
//        ldc_amt1 = (int) getValueDouble("ldc_amt1");
//        ldc_amt2 = (int) getValueDouble("ldc_amt2");
//
//      // --get 道路救援參數--
//      sqlCmd  = " select ";
////        + " nvl(rpm_freeamt,0) as ldc_amt3 , "
////        + " nvl(rpm_cyearamt,0) as ldc_amt4 "
////?        sqlCmd += " nvl(free_amt,0) as ldc_amt3 , ";
////?        sqlCmd += " nvl(cyear_amt,0) as ldc_amt4 ";
//        sqlCmd += " from cms_roadparm ";
//        sqlCmd += " where 1=1 ";
//        sqlCmd += " fetch first 1 rows only ";
//        recCnt = selectTable();
//      ldc_amt3 = (int) getValueDouble("ldc_amt3");
//      ldc_amt4 = (int) getValueDouble("ldc_amt4");
//
//      if (ldc_amt1 + ldc_amt2 >= ldc_amt3) {
//          return 1;
//      }
//      else {
//          return 2;
//      }
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public double get_expendamt() throws Exception {
//      // -- get 自費金額--
//      double ldc_expamt = 0;
//      sqlCmd  = " select "
//              + " rpm_rcvamt "
//              + " from cms_roadparm "
//              + " where 1=1 "
//              + " fetch first 1 rows only ";
//
//        int recCnt = selectTable();
//
//      ldc_expamt = getValueDouble("rpm_rcvamt");
//
//      return ldc_expamt;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int chk_freeamt_curyy(String as_cardno, String as_year) throws Exception {
//      // --check 當年恢復免費道路救援資格--
//      // -- 1 -> 符合
//      // -- -1 -> 不符合
//      // ------------------------------------------------------------------------
//      String ls_pseqno = "", ls_val1 = "", ls_val2 = "", ls_grcode = "";
//      int ldc_amt = 0, ldc_amt2 = 0, ldc_amt3 = 0;
//
//      // --get P_SEQNO--
//
//      sqlCmd  = " select "
//              + " nvl(p_seqno,'') as ls_pseqno , "
//                + " nvl(group_code,'0000') as ls_grcode "
//                + " from crd_card "
//              + " where card_no = ? ";
//        setString(1, as_cardno);
//        int recCnt = selectTable();
//        if (notFound.equals("Y")||recCnt<1) {
//            return -1;
//        }
//        ls_pseqno = getValue("ls_pseqno");
//        ls_grcode = getValue("ls_grcode");
//
//      // --終身金卡---
//      if (ls_grcode.equals("8888")) {
//          return 1;
//      }
//      // --Get 消費金額--
//        ls_val1 = Double.toString(Double.parseDouble(as_year)-1) + "12";
//      ls_val2 = as_year + "11";
//
//      sqlCmd  = " select "
//              + " sum(nvl(his_purchase_amt,0)) + sum(nvl(his_cash_amt,0)) as ldc_amt "
//              + " from act_anal_sub "
//              + " where 1=1 "
//              + " and p_seqno = ? "
//              + " and acct_month between ? and ? ";
//        setString(1, ls_pseqno);
//      setString(2, ls_val1);
//      setString(3, ls_val2);
//        recCnt = selectTable();
//        ldc_amt = (int) getValueDouble("ldc_amt");
//
//
//      // --get 道路救援參數--
//      sqlCmd  = " select "
//              + " nvl(rpm_freeamt,0) as ldc_amt2 , "
//              + " nvl(rpm_cyearamt,0) as ldc_amt3 "
//              + " from cms_roadparm "
//              + " where 1=1 "
//              + " fetch first 1 rows only ";
//        recCnt = selectTable();
//      ldc_amt2 = (int) getValueDouble("ldc_amt2");
//      ldc_amt3 = (int) getValueDouble("ldc_amt3");
//      if (ldc_amt >= ldc_amt3)
//          return 1;
//
//      return -1;
//
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public boolean chk_firstfree(String as_cardno) throws Exception {
//      // --- adding 白金卡 -- shu yu tsai 20030211
//      // --CHECK 首次免費道路救援登錄--
//      boolean lb_rc = true;
//
//      String ls_val1 = "", ls_val2 = "", ls_val3 = "", ls_val4 = "", ls_val5 = "";
//      String ls_val6 = "", ls_val7 = "", ls_val8 = "", ls_val9 = "", ls_val10 = "";
//
//      h_err_reason = "";
//
//      sqlCmd  = " select "
//              + " nvl(A.group_code,'0000') as ls_val1 , "
//              + " nvl(B.card_not,'') as ls_val2 , "
//              + " nvl(A.current_code,'N') as ls_val3 , "
//              + " A.acct_type as ls_val4 , "
//              + " B.card_type as ls_val5 "
//              + " from crd_card A , ptr_card_type B "
//              + " where A.card_type = B.card_type "
//              + " and A.card_no = ? ";
//        setString(1, as_cardno);
//        int recCnt = selectTable();
//        if (notFound.equals("Y")||recCnt<1) {
//          h_err_reason = "卡號之卡種資料不存在";
//          insert_cms_roadmaster_log();
//          return false;           
//        }
//        ls_val1 = getValue("ls_val1");
//        ls_val2 = getValue("ls_val2");
//        ls_val3 = getValue("ls_val3");
//        ls_val4 = getValue("ls_val4");
//        ls_val5 = getValue("ls_val5");
//
//      // --無效卡--
//
//      if (!ls_val3.trim().equals("0")) {
//          h_err_reason = "卡號無效卡";
//          insert_cms_roadmaster_log();
//          return false;
//      }
//
//      // --例外:Group_code(8888)--
//      if (ls_val1.equals("8888")) {
//          return true;
//      }
//      // --acct_type--
//      // --商務卡--
//
//      sqlCmd  = " select "
//              + " nvl(card_indicator,'') as ls_val6 , "
//              + " nvl(car_service_flag,'N') as ls_val7 "
//              + " from ptr_acct_type "
//              + " where acct_type = ? ";
//        setString(1, ls_val4);        
//      recCnt = selectTable();
//        if (notFound.equals("Y")||recCnt<1) {
//          h_err_reason = "卡號無法判定帳戶類別";
//          insert_cms_roadmaster_log();
//          return false;           
//        }
//        ls_val6 = getValue("ls_val6");
//        ls_val7 = getValue("ls_val7");
//
//      if (!ls_val7.trim().equals("Y")) {
//          h_err_reason = "帳戶類別未享有道路救援";
//          insert_cms_roadmaster_log();
//          return false;
//      }
//
//      // >>CARD==================================================================
//      ls_val8 = "N";
//      ls_val9 = "N";
//
//      if (ls_val1.trim().length()>0 && !ls_val1.trim().equals("0000")) {
//          sqlCmd  = " select "
//                  + " nvl(gcard_flag,'N') as ls_val8 , "
//                  + " nvl(bcard_flag,'N') as ls_val9 "
//                  + " from cms_roadgroup "
//                  + " where group_code = ? ";
//            setString(1, ls_val1);
//          recCnt = selectTable();
//          ls_val8 = getValue("ls_val8");
//          ls_val9 = getValue("ls_val9");
//            
//      }
//
//      if (ls_val1.trim().length()<1 || ls_val1.trim().equals("0000")) {
//          sqlCmd  = " select "
//                  + " nvl(gcard_flag,'N') as ls_val8 , "
//                  + " nvl(bcard_flag,'N') as ls_val9 , "
//                  + " nvl(pcard_flag,'P') as ls_val10 "
//                  + " from cms_roadparm "
//                  + " fetch first 1 rows only ";
//          recCnt = selectTable();
//          ls_val8 = getValue("ls_val8");
//          ls_val9 = getValue("ls_val9");
//          ls_val10 = getValue("ls_val10");
//          
//      }
//
//      lb_rc = true;
//
//      if (ls_val2.equals("C")) { // --普卡
//          if (!ls_val9.equals("Y")) {
//              if (!ls_val5.equals("VB") || !ls_val5.equals("MB")) {
//                  h_err_reason = "普卡不可登錄";
//                  insert_cms_roadmaster_log();
//                  lb_rc = false;
//              }
//          }
//      }
//      else if (ls_val2.equals("G")) { // --金卡
//          if (!ls_val8.equals("Y")) {
//              h_err_reason = "金卡不可登錄";
//              insert_cms_roadmaster_log();
//              lb_rc = false;
//          }
//      }
//      else if (ls_val2.equals("P")) { // --白金卡
//          if (ls_val10.equals("Y")) {
//              h_err_reason = "金卡不可登錄";
//              insert_cms_roadmaster_log();
//              lb_rc = false;
//          }
//      }
//      else {
//          h_err_reason = "無法判定金/普卡";
//          insert_cms_roadmaster_log();
//          lb_rc = false;
//      }
//      return lb_rc;
//    }
// ************************************************************************
    public int autonoDtl(String asKey) throws Exception {
        // --get SEQNO from road detail--
        // =================================================
        String lsKey = "";
        int llSeqno = 0;

        lsKey = asKey.trim();
        if (lsKey.length()<1)
            lsKey = sysDate;

        sqlCmd  = " select "
                + " max(rd_seqno) as ll_seqno "
                + " from cms_roaddetail "
                + " where rd_moddate = ? ";
        setString(1, lsKey);
        int recCnt = selectTable();
        llSeqno = getValueInt("ll_seqno");


        return llSeqno + 1;
    }
 // ************************************************************************
    public int autonoErrLog(String asKey) throws Exception {
        // --get SEQNO from road detail--
        // =================================================
        String lsKey = "";
        int llSeqno = 0;

        lsKey = asKey.trim();
        if (lsKey.length()<1)
            lsKey = sysDate;

        sqlCmd  = " select "
                + " max(seqno) as ll_seqno "
                + " from cms_roadmaster_log "
                + " where moddate = ? ";
        setString(1, lsKey);
        int recCnt = selectTable();
        llSeqno = getValueInt("ll_seqno");


        return llSeqno + 1;
    }
// ************************************************************************
// Rds_func 沒使用到
//    public int upd_bil_sysexp() throws Exception {
//      // --產生道路救援自費帳單資料--
//      // ========================================================================
//      long L = 0;
//      String ls_key1 = "", ls_key2 = "";
//      String ls_val1 = "", ls_val2 = "", ls_val3 = "";
//      String ls_mod = "";
//      double ldc_amt = 0;
//
//      L = 1;
//      ls_key1 = h_card_no;
//      ls_key2 = h_road.rm_type;
//      if (!ls_key2.equals("E"))
//          return 0;
//
//      // --AMT--
//      ldc_amt = h_road.rm_payamt;
//      if (ldc_amt <= 0)
//          return 0;
//      // --sysdate--
//      ls_val1 = h_system_date;
//      // --acct_type/key--
//      sqlCmd  = " select "
//              + " acct_type as ls_val2 , "
//                + " acct_key as ls_val3 "
//              + " from crd_card "
//              + " where card_no = ? ";
//        setString(1, ls_key1);
//        int recCnt = selectTable();
//        if (recCnt <= 0)
//          return -1;
//        ls_val2 = getValue("ls_val2");
//        ls_val3 = getValue("ls_val3");
//
//
//      // --MOD-Attrib---
//        setValue("card_no"            , ls_key1);
//        setValue("bill_type"          , "INCF");
//        setValue("txn_code"           , "05");
//        setValue("purchase_date"      , ls_val1);
////        setValue("acct_type"          , ls_val2);
////        setValue("acct_key"           , ls_val3);
//        setValueDouble("dest_amt"     , ldc_amt);
//        setValue("dest_curr"          , "901");
//        setValueDouble("src_amt"      , ldc_amt);
//        setValue("post_flag"          , "N");
//        setValue("mod_user"           , "rdsb020");
//        setValue("mod_time"           , sysDate + sysTime);
//        setValue("mod_pgm"            , "RdsB020");
//        daoTable = "bil_sysexp";
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_bil_sysexp duplicate!", "", "");
//            return -1;
//        }
//
//      return 1;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique(String as_carno) throws Exception {
//      if (as_carno.length()<1) {
//          h_err_reason = "車號不可空白";
//          insert_cms_roadmaster_log();
//          return 0;
//      }
//
//      String ss = "";
//      int ll_cnt = 0;
//      ss = as_carno.trim();
//      // --check CMS_ROADDETAIL--------------------------------------------
//      sqlCmd  = " select "
//              + " count(*) as ll_cnt "
//              + " from cms_roaddetail "
//              + " where apr_date = '' "
//              + " and rd_carno = ? ";
//        setString(1, ss);
//        int recCnt = selectTable();
//        ll_cnt = getValueInt("ll_cnt");
//        
//      if (ll_cnt > 0) {
//          h_err_reason = "車號有異動 or 登錄, 請先作覆核";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      sqlCmd  = " select "
//              + " count(*) as ll_cnt "
//              + " from cms_roaddetail "
//              + " where apr_date = '' "
//              + " and rd_newcarno = ? ";
//        setString(1, ss);
//        recCnt = selectTable();
//        ll_cnt = getValueInt("ll_cnt");
//
//      if (ll_cnt > 0) {
//          h_err_reason = "車號有異動 or 登錄, 請先作覆核";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//      // --check CMS_ROADMASTER---------------------------------------------
//      sqlCmd  = " select "
//              + " count(*) as ll_cnt "
//              + " from cms_roadmaster "
//              + " where rm_type = 'F' "
//              + " and rm_carno = ? "
//              + " nvl(rm_status,'0') <> '0' ";
//        setString(1, ss);
//        recCnt = selectTable();
//        ll_cnt = getValueInt("ll_cnt");
//
//      if (ll_cnt > 0) {
//          h_err_reason = "車號已登錄免費, 不可再重覆登錄";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//      return 1;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_detail(String as_carno, String as_rowid) throws Exception {
//      // --Check car-no is Unique in CMS_ROADDETAIL for UNapprove
//      // --
//      // =====================================================================
//      String ss = "", s_errmsg = "", s_rowid = "";
//      long ll_cnt = 0;
//      int i_RC = 1;
//
//      h_err_reason = "";
//
//      if (as_carno.length()<1) {
//          h_err_reason = "車號不可空白";
//          insert_cms_roadmaster_log();
//          return 0;
//      }
//
//      ss = as_carno.trim();
//
//      if (as_rowid.length()<1) {
//          s_rowid = "";
//      }
//      else {
//          s_rowid = as_rowid.trim();
//      }
//      // --check CMS_ROADDETAIL--------------------------------------------
//      if (s_rowid.length() == 0) {
//          sqlCmd  = " select "
//                  + " count(*) as ll_cnt "
//                  + " from cms_roaddetail "
//                  + " where apr_date = '' "
//                  + " and rd_carno = ? "
//                  + " and rd_type = 'F' ";
//            setString(1, ss);
//            int recCnt = selectTable();
//            ll_cnt = getValueInt("ll_cnt");
//
//      }
//      else {
//          sqlCmd  = " select "
//                  + " count(*) as ll_cnt "
//                  + " from cms_roaddetail "
//                  + " where apr_date = '' "
//                  + " and rd_carno = ? "
//                  + " and rd_type = 'F' "
//                  + " and rowid <> ? ";
//            setString(1, ss);
//            setString(2, s_rowid);
//            int recCnt = selectTable();
//            ll_cnt = getValueInt("ll_cnt");
//      }
//
//      // --變更車號----------------------------------
//      if (s_rowid.length() == 0) {
//          sqlCmd  = " select "
//                  + " count(*) as ll_cnt "
//                  + " from cms_roaddetail "
//                  + " where apr_date = '' "
//                  + " and nvl(rd_newcarno,'') = ? "
//                  + " and rd_type = 'F' ";
//            setString(1, ss);
//            int recCnt = selectTable();
//            ll_cnt = getValueInt("ll_cnt");
//      }
//      else {
//          sqlCmd  = " select "
//                  + " count(*) as ll_cnt "
//                  + " from cms_roaddetail "
//                  + " where apr_date = '' "
//                  + " and nvl(rd_newcarno,'') = ? "
//                  + " and rd_type = 'F' "
//                  + " and rowid <> ? ";
//            setString(1, ss);
//            setString(2, s_rowid);
//            int recCnt = selectTable();
//            ll_cnt = getValueInt("ll_cnt");
//      }
//
//      if (ll_cnt > 0) {
//          h_err_reason = "車號有異動 or 登錄, 請先作覆核";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      return 1;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int has_free(String as_card_no) throws Exception {
//      // --是否可享有免費道路救援
//      // -- cms_roadparm2_dtl
//      // =================================================================
//
//      String ls_sys_date = "";    
//
//      h_err_reason = "";
////        is_proj_no = ""; //h_roadparm2.proj_no
////        is_p_seqno = ""; //h_card.p_seqno
////        is_card_no = nvl(as_card_no, ""); //h_card_no   
////        if (empty(is_card_no))  return 0;
//
//      
//      // -一般卡-
//      sqlCmd = "select * ";
//      sqlCmd += " from cms_roadparm2 ";
//      sqlCmd += " where 1=1 and proj_no in ";
//      sqlCmd += " ( select distinct proj_no from cms_roadparm2_dtl ";
//      sqlCmd += " where acct_type = ? and card_type = ? ";
//      sqlCmd += " and uf_nvl(valid_end_date,'99991231') >= to_char(sysdate,'yyyymmdd') ";
//      sqlCmd += " and nvl(apr_flag,'N') = 'Y' ";
//      sqlCmd += " and decode(group_code,'',cast(? as char(4)),group_code) = ? ";
//        sqlCmd += " and decode(corp_no,'',cast(? as char(8)),corp_no) =?";
//        sqlCmd +=" )";
//        setString(1, h_card.acct_type);
//        setString(2, h_card.card_type);
//        setString(3, h_card.group_code);
//        setString(4, h_card.group_code);
//        setString(5, h_card.corp_no);
//        setString(6, h_card.corp_no);
//
//        int recCnt = selectTable();
//        if (notFound.equals("Y")||recCnt<1) {
//          h_err_reason = "卡號無免費道路救援權利";
//          insert_cms_roadmaster_log();
//        }
//        h_roadparm2.proj_no           = getValue("proj_no");
//        h_roadparm2.proj_desc         = getValue("proj_desc");
//        h_roadparm2.fst_acct_code_ao  = getValue("fst_acct_code_ao");
//        h_roadparm2.fst_acct_code_bl  = getValue("fst_acct_code_bl");
//        h_roadparm2.fst_acct_code_ca  = getValue("fst_acct_code_ca");
//        h_roadparm2.fst_acct_code_id  = getValue("fst_acct_code_id");
//        h_roadparm2.fst_acct_code_it  = getValue("fst_acct_code_it");
//        h_roadparm2.fst_acct_code_ot  = getValue("fst_acct_code_ot");
//        h_roadparm2.fst_acct_code_it_flag = getValue("fst_acct_code_it_flag");
//        h_roadparm2.fst_mm            = getValueInt("fst_mm");
//        h_roadparm2.fst_one_low_amt   = getValueDouble("fst_one_low_amt");
//        h_roadparm2.fst_amt_cond      = getValue("fst_amt_cond");
//        h_roadparm2.fst_purch_amt     = getValueDouble("fst_purch_amt");
//        h_roadparm2.fst_row_cond      = getValue("fst_row_cond");
//        h_roadparm2.fst_purch_row     = getValueInt("fst_purch_row");
//        h_roadparm2.lst_cond          = getValue("lst_cond");
//        h_roadparm2.lst_acct_code_ao  = getValue("lst_acct_code_ao");
//        h_roadparm2.lst_acct_code_bl  = getValue("lst_acct_code_bl");
//        h_roadparm2.lst_acct_code_ca  = getValue("lst_acct_code_ca");
//        h_roadparm2.lst_acct_code_id  = getValue("lst_acct_code_id");
//        h_roadparm2.lst_acct_code_it  = getValue("lst_acct_code_it");
//        h_roadparm2.lst_acct_code_ot  = getValue("lst_acct_code_ot");
//        h_roadparm2.lst_acct_code_it_flag = getValue("lst_acct_code_it_flag");
//        h_roadparm2.lst_mm            = getValueInt("lst_mm");
//        h_roadparm2.lst_one_low_amt   = getValueDouble("lst_one_low_amt");
//        h_roadparm2.lst_amt_cond      = getValue("lst_amt_cond");
//        h_roadparm2.lst_purch_amt     = getValueDouble("lst_purch_amt");
//        h_roadparm2.lst_row_cond      = getValue("lst_row_cond");
//        h_roadparm2.lst_purch_row     = getValueInt("lst_purch_row");
//        h_roadparm2.cur_cond          = getValue("cur_cond");
//        h_roadparm2.cur_acct_code_ao  = getValue("cur_acct_code_ao");
//        h_roadparm2.cur_acct_code_bl  = getValue("cur_acct_code_bl");
//        h_roadparm2.cur_acct_code_ca  = getValue("cur_acct_code_ca");
//        h_roadparm2.cur_acct_code_id  = getValue("cur_acct_code_id");
//        h_roadparm2.cur_acct_code_it  = getValue("cur_acct_code_it");
//        h_roadparm2.cur_acct_code_ot  = getValue("cur_acct_code_ot");
//        h_roadparm2.cur_acct_code_it_flag = getValue("cur_acct_code_it_flag");
//        h_roadparm2.cur_mm            = getValueInt("cur_mm");
//        h_roadparm2.cur_one_low_amt   = getValueDouble("cur_one_low_amt");
//        h_roadparm2.cur_amt_cond      = getValue("cur_amt_cond");
//        h_roadparm2.cur_purch_amt     = getValueDouble("cur_purch_amt");
//        h_roadparm2.cur_row_cond      = getValue("cur_row_cond");
//        h_roadparm2.cur_purch_row     = getValueInt("cur_purch_row");
//        h_roadparm2.amt_sum_flag      = getValue("amt_sum_flag");
//      
//      // -min-issue-date-
//      String ls_issue_date = "", ls_oppost_date = "", ls_sys_date2 = "";
//
//      ls_sys_date = commDate.sysDate();
//      ls_sys_date2 = commDate.dateAdd(ls_sys_date, 0, -12, 0);
//
//      sqlCmd = " select "
//             + " min(issue_date) as ls_issue_date "
//             + " from crd_card "
//             + " where p_seqno = ? "
//             + " and current_code = '0' ";
//        setString(1, h_card.p_seqno);
//        recCnt = selectTable();
//        ls_issue_date = getValue("ls_issue_date").trim();
//      
//      sqlCmd = " select "
//             + " max(oppost_date) as ls_oppost_date "
//             + " from crd_card "
//             + " where p_seqno = ? "
//             + " and current_code <>'0' ";
//        setString(1, h_card.p_seqno);
//        recCnt = selectTable();
//        ls_issue_date = getValue("ls_oppost_date").trim();
//
//
//      // -非首年(B)-
//      if (!(ls_issue_date.substring(0, 4).equals(ls_sys_date.substring(0, 4)))) {
//          is_ch_type = "B";
//          return 1;
//      }
//
//      // -首年-
//      if (this.chk_strend(ls_oppost_date, ls_sys_date2) == 1) {
//          is_ch_type = "A";
//          return 1;
//      }
//
//      // -非首年:MIN(發卡日)in今年-
//      is_ch_type = "C";
//
//      return 1;
//
//    }
// ************************************************************************
    public int freeIsAuto(String asCardNo) throws Exception {
        // --check card_type is auto register
        // -1: error, 1:auto, 0:is not auto
        // -------------------------------------------

        String lsCardNo = "", lsCardType = "";
        int llCnt = 0;

        lsCardNo = nvl(asCardNo, "");
        if (empty(lsCardNo)) {
            hErrReason = "卡號為空白";
            insertCmsRoadmasterLog();
            return -1;
        }
        sqlCmd = " select "
               + " card_type as ls_card_type "
               + " from crd_card "
               + " where card_no = ? ";
        setString(1, lsCardNo);
        int recCnt = selectTable();
        lsCardType = getValue("ls_card_type");

        if (recCnt <= 0) {
            hErrReason = "卡號不存在";
            insertCmsRoadmasterLog();
            return -1;
        }

        sqlCmd = " select "
               + " rds_pcard "
               + " from ptr_card_type "
               + " where card_type = ? ";
        setString(1, lsCardType);
        recCnt = selectTable();
        if (getValue("rds_pcard")=="A" ) {
            if(isRentCar == "N") {
                hRoad.rdsPcard = "A";
                hRode.rdsPcard = "A";
            } 
            return 1;
        }else { 
            if(isRentCar == "N") {
                hRoad.rdsPcard = getValue("rds_pcard");
                hRode.rdsPcard = getValue("rds_pcard");
            } 
        return 0;
        }
    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_master(String as_carno) throws Exception {
//      String ls_car = "", ss = "";
//      long ll_cnt = 0;
//
//      h_err_reason = "";
//
//      if (empty(as_carno)) {
//          h_err_reason = "車號不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ls_car = as_carno.trim();
//
//      // --check CMS_ROADMASTER---------------------------------------------
//      ll_cnt = 0;
//      sqlCmd = " select "
//             + " count(*) as ll_cnt "
//             + " from cms_roadmaster "
//             + " where rm_carno = ? "
//             + " and rm_type = 'F' "
//             + " and (nvl(rm_status,'0') <> '0' or (rm_status = '0' and rm_reason = '2')) ";
//        setString(1, ls_car);
//        int recCnt = selectTable();
//        ll_cnt = getValueInt("ll_cnt");
//
//      if (ll_cnt > 0) {
//          h_err_reason = "車號已登錄免費, 不可再重覆登錄";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      return 1;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_free() throws Exception {
//      String ls_card_no = "", ls_car = "";
//      int ll_cnt = 0;
//
//      h_err_reason = "";
//
//      ls_card_no = nvl(h_card_no, "");
//      if (empty(ls_card_no)) {
//          h_err_reason = "卡號 不可空白";
//          insert_cms_roadmaster_log();
//      }
//
//      ls_car = nvl(h_car_no, "");
//      if (empty(ls_car)) {
//          h_err_reason = "車號 不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      sqlCmd = " select "
//             + " count(*) as ll_cnt "
//             + " from cms_roadmaster "
//             + " where rm_carno = ? "
//             + " and card_no <> ? "
//             + " and (nvl(rm_status,'0') <> '0' or (rm_status = '0' and rm_reason='2'))";
//        setString(1, ls_car);
//        setString(2, ls_card_no);
//        int recCnt = selectTable();
//
//      if (getValueInt("ll_cnt") > 0)
//          return 1;
//      return 0;
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_expn() throws Exception {
//      // --RC: -1.error, 0.no register, 1.registed
//      // ============================================
//      String ls_card_no = "", ls_car = "";
//      int ll_cnt = 0;
//
//      h_err_reason = "";
//
//      ls_card_no = nvl(h_card_no, "");
//      if (empty(ls_card_no)) {
//          h_err_reason = "卡號 不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ls_car = nvl(h_car_no, "");
//      if (empty(ls_car)) {
//          h_err_reason = "卡號 不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      sqlCmd = " select "
//             + " count(*) as ll_cnt "
//             + " from cms_roadmaster "
//             + " where rm_carno = ? "
//             + " and card_no <> ? "
//             + " and nvl(rm_status,'0') <> '0' "
//             + " and rm_validdate >= to_char(sysdate,'yyyymmdd') ";
//        setString(1, ls_car);
//        setString(2, ls_card_no);
//        int recCnt = selectTable();
//
//      if (getValueInt("ll_cnt") > 0)
//          return 1;
//      return 0;
//
//    }
// ************************************************************************
    public int hasFree() throws Exception {
            // --是否符合免費救援:ref cms_a014.pc
            // RC: -1.Error, 0.不符合, 1.符合
            // ===============================================
            String lsIdPSeqno = "";
            String lsIssueDate = "", lsOppostDate = "", lsSysDate2 = "", lsSysDate = "";


            if(empty(hCardNo)) {
                hErrReason = "卡號不可空白";
                insertCmsRoadmasterLog();
                return -1;
            }
            selectCrdCardIdno();
            if(hIdno.idNo.isEmpty())
                return -1;
            selectRdsRoadparm2();
    		if (idsParm.listRows()<=0) {
                hErrReason = "卡號 不符合免費道路救援參數";
                insertCmsRoadmasterLog();
                   return 0;
            }
            // -assign cardholer-type[A/B/C]-

            lsSysDate = commDate.sysDate();
            lsSysDate2 = commDate.dateAdd(lsSysDate, 0, -12, 0);

            // -min-issue_date-

            sqlCmd = " select "
                   + " min(issue_date) as ls_issue_date "
                   + " from crd_card "
                   + " where p_seqno = ? "
                   + " and current_code = '0' ";
            setString(1, hCard.pSeqno);
            int recCnt = selectTable();
            lsIssueDate = getValue("ls_issue_date");

            // -MAX.oppost_date-
            sqlCmd = " select "
                   + " max(oppost_date) as ls_oppost_date "
                   + " from crd_card "
                   + " where p_seqno = ? "
                   + " and current_code <> '0' ";
            setString(1, hCard.pSeqno);
            recCnt = selectTable();
            lsOppostDate = getValue("ls_oppost_date");

            // -非首年(B)-

            if (!lsIssueDate.substring(0, 4).equals(lsSysDate.substring(0, 4))) {
                isChType = "B";
                return 1;
            }

            // -首年-
            if (this.chkStrend(lsOppostDate, lsSysDate2) == 1) {
                isChType = "A";
                return 1;
            }

            // -非首年:MIN(發卡日)in今年-
            isChType = "C";

            return 1;
        }
// ************************************************************************
    public int checkRoadparm2() throws Exception {
            // --check cms_roadparm2
            // --RC: -1.error, 1.符合, 0.不符合
            // ========================================
            int liRc = 0;
            long ll = 0;
            
            String[] lsAcctCode = new String[6];
            String[] lsCond = new String[2];
            String lsItFlag = "" , ss = "";
            int liMm = 0;
            double ldcOneLowAmt = 0 ;
            double[] ldcCond = new double[3];
            
//          select_crd_card_idno();
//          if(empty(h_card.p_seqno) && empty(h_card.card_no)){
//              h_err_reason = "未取得卡人資料[p_seqno/card_no]";
//              insert_cms_roadmaster_log();
//              return -1;
//          }
            
            String isProjNo = "";
//          double idc_purch_amt = 0;
//          int il_purch_cnt = 0;
//          double idc_last_amt = 0;
            
            selectRdsRoadparm2();
    		if (idsParm.listRows()<=0)
                   return 0;        
            
    		for (int i=0; i<idsParm.listRows(); i++) {
                lsAcctCode[0] = "N"; lsAcctCode[1] = "N"; lsAcctCode[2] = "N"; lsAcctCode[3] = "N"; lsAcctCode[4] = "N"; lsAcctCode[5] = "N";
                lsCond[0] = "N" ;  lsCond[1] = "N";
                ldcCond[0] = 0;    ldcCond[1] = 0; ldcCond[2] = 0;
                if(eqIgno(isChType,"A")){
                    ss = idsParm.colss(i, "fst_cond");
                    if(eqIgno(ss,"Y")==false)  continue;
                    lsAcctCode[0] = idsParm.colss(i,"fst_acct_code_ao");
                    lsAcctCode[1] = idsParm.colss(i,"fst_acct_code_bl");
                    lsAcctCode[2] = idsParm.colss(i,"fst_acct_code_ca");
                    lsAcctCode[3] = idsParm.colss(i,"fst_acct_code_id");
                    lsAcctCode[4] = idsParm.colss(i,"fst_acct_code_it");
                    lsAcctCode[5] = idsParm.colss(i,"fst_acct_code_ot");
                    lsItFlag = idsParm.colss(i,"fst_acct_code_it_flag");
                    liMm = (int)idsParm.colnum(i, "fst_mm");
                    ldcOneLowAmt = idsParm.colnum(i,"fst_one_low_amt");
                    lsCond[0] = idsParm.colss(i,"fst_amt_cond");
                    ldcCond[0] = idsParm.colnum(i,"fst_purch_amt");
                    lsCond[1] = idsParm.colss(i,"fst_row_cond");
                    ldcCond[1] = idsParm.colnum(i,"fst_purch_row");
                }   else if(eqIgno(isChType,"B")){
                    ss = idsParm.colss(i, "lst_cond");
                    if(eqIgno(ss,"Y")==false)  continue;
                    lsAcctCode[0] = idsParm.colss(i,"lst_acct_code_ao");
                    lsAcctCode[1] = idsParm.colss(i,"lst_acct_code_bl");
                    lsAcctCode[2] = idsParm.colss(i,"lst_acct_code_ca");
                    lsAcctCode[3] = idsParm.colss(i,"lst_acct_code_id");
                    lsAcctCode[4] = idsParm.colss(i,"lst_acct_code_it");
                    lsAcctCode[5] = idsParm.colss(i,"lst_acct_code_ot");
                    lsItFlag = idsParm.colss(i,"lst_acct_code_it_flag");
                    liMm = (int)idsParm.colnum(i, "lst_mm");
                    ldcOneLowAmt = idsParm.colnum(i,"lst_one_low_amt");
                    lsCond[0] = idsParm.colss(i,"lst_amt_cond");
                    ldcCond[0] = idsParm.colnum(i,"lst_purch_amt");
                    lsCond[1] = idsParm.colss(i,"lst_row_cond");
                    ldcCond[1] = idsParm.colnum(i,"lst_purch_row");
                }   else if(eqIgno(isChType,"C")){
                    ss = idsParm.colss(i, "cur_cond");
                    if(eqIgno(ss,"Y")==false)  continue;
                    lsAcctCode[0] = idsParm.colss(i,"cur_acct_code_ao");
                    lsAcctCode[1] = idsParm.colss(i,"cur_acct_code_bl");
                    lsAcctCode[2] = idsParm.colss(i,"cur_acct_code_ca");
                    lsAcctCode[3] = idsParm.colss(i,"cur_acct_code_id");
                    lsAcctCode[4] = idsParm.colss(i,"cur_acct_code_it");
                    lsAcctCode[5] = idsParm.colss(i,"cur_acct_code_ot");
                    lsItFlag = idsParm.colss(i,"cur_acct_code_it_flag");
                    liMm = (int)idsParm.colnum(i, "cur_mm");
                    ldcOneLowAmt = idsParm.colnum(i,"cur_one_low_amt");
                    lsCond[0] = idsParm.colss(i,"cur_amt_cond");
                    ldcCond[0] = idsParm.colnum(i,"cur_purch_amt");
                    lsCond[1] = idsParm.colss(i,"cur_row_cond");
                    ldcCond[1] = idsParm.colnum(i,"cur_purch_row");
                }
                isProjNo = idsParm.colss(i,"proj_no");
                hRoadparm2.projNo = idsParm.colss(i,"proj_no");
                liRc = 0 ;
                //--read bil_bill
                if (eqIgno(idsParm.colss(i,"amt_sum_flag"),"1")){
                    liRc = billAmtAcno(lsAcctCode,lsItFlag,liMm,ldcOneLowAmt);
                } else if (eqIgno(idsParm.colss(i,"amt_sum_flag"),"2")) {
                    liRc = billAmtCard(lsAcctCode,lsItFlag,liMm,ldcOneLowAmt);
                } else if (eqIgno(idsParm.colss(i,"amt_sum_flag"),"3")) {
                    liRc = billAmtId(lsAcctCode,lsItFlag,liMm,ldcOneLowAmt);
                }
                if(liRc==-1)   return -1;
                        
                //--read mkt_card_consume
                if(eqIgno(isChType,"B")){
                    if(eqIgno(idsParm.colss(i,"amt_sum_flag"),"1")){
                        liRc = lastAmtAcno(lsAcctCode);
                    } else if (eqIgno(idsParm.colss(i,"amt_sum_flag"),"2")) {
                        liRc = lastAmtCard(lsAcctCode);
                    } else if (eqIgno(idsParm.colss(i,"amt_sum_flag"),"3")) {
                        liRc = lastAmtId(lsAcctCode);
                    }
                }
                
                if(liRc ==-1)  return -1;
                
                //--check 消費條件--
                liRc = 0 ;
                if(eqIgno(isChType,"B") && idcLastAmt < ldcCond[2])   continue;
                if(eqIgno(lsCond[0],"Y")&& idcPurchAmt>=ldcCond[0]){
                    liRc =1;
                    return liRc;
                }
                if(eqIgno(lsCond[1],"Y")&& ilPurchCnt>=ldcCond[1]){
                    liRc =1;
                    return liRc;
                }
           }

            return liRc;
        }
// ************************************************************************
    public int billAmtAcno(String[] asAcctCode,String asItFlag,int aiMm,double adcOneLowAmt) throws Exception {
        // --RC: 1.OK, -1.error
        // =======================================================
        String[] lsItem = new String[6];   String lsThisAcctMonth = "" , lsThisAcctMonth2 = "";
        
        if(empty(hCard.pSeqno)){
            hErrReason = "未取得持卡人帳戶資料(p_seqno)";
            insertCmsRoadmasterLog();
            return -1;
        }
        
        lsItem[0] = "N";lsItem[1] = "N";lsItem[2] = "N";lsItem[3] = "N";lsItem[4] = "N";lsItem[5] = "N";
        
        for(int l=0;l<=5;l++){
            lsItem[l] = nvl(asAcctCode[l],"N");
        }
        
        lsThisAcctMonth = "";
        
        String sql1 = " select A.this_acct_month from ptr_workday A where A.stmt_cycle in "
                        + " (select stmt_cycle from act_acno where p_seqno = ?) "
                        ;
        
        sqlSelect(sql1,new Object[]{hCard.pSeqno});
        if(sqlNrow<=0){
            hErrReason = "bill_amt.ptr_workday error ";
            insertCmsRoadmasterLog();
            return -1;
        }
        lsThisAcctMonth = colSs("this_acct_month");
        lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1+aiMm*-1);
        idcPurchAmt = 0 ;
        ilPurchCnt = 0 ;
        
        String sql2 = " select "
                        + " count(*) as purch_cnt , "
                        + " sum(decode(a.acct_code,'IT',decode(?,'2',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)),case when a.txn_code in ('06','25','27','28','29')then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                        + " from bil_contract b left join bil_bill a on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                        + " where a.p_seqno = ? "
                        + " and a.acct_code in "
                        + " (decode(?,'Y','AO','XX') , decode(?,'Y','BL','XX') , decode(?,'Y','CA','XX') , "
                        + "  decode(?,'Y','ID','XX') , decode(?,'Y','IT','XX') , decode(?,'Y','OT','XX') ) "
                        + " and ( (a.txn_code in ('06','25','27','28','29')) or (txn_code not in ('06','25','27','28','29') and a.dest_amt > ?)) "
                        + " and acct_month between ? and ? "
                        ;
        
        ppp(1,asItFlag);//as_it_flag
        ppp(2,hCard.pSeqno);
        ppp(3,lsItem[0]);
        ppp(4,lsItem[1]);
        ppp(5,lsItem[2]);
        ppp(6,lsItem[3]);
        ppp(7,lsItem[4]);
        ppp(8,lsItem[5]);
        ppp(9,adcOneLowAmt);
        ppp(10,lsThisAcctMonth);
        ppp(11,lsThisAcctMonth2);
        
        sqlSelect(sql2);

        idcPurchAmt = colInt("purch_amt");
        ilPurchCnt = colInt("purch_cnt");
        

        return 1;

    }
// ************************************************************************
    public int lastAmtAcno(String[] asItem) throws Exception {
        String[] lsItem = new String[6];
        String lsLastYear = "";
        int ii = 0;

        if (empty(nvl(hCard.pSeqno, ""))) {
            hErrReason = "未取得持卡人 帳戶資料[p_seqno]";
            insertCmsRoadmasterLog();
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
        String sql1 = " select "
            + " sum("
            + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)"
            + " ) as idc_last_amt "
            + " from mkt_card_consume "
            + " where p_seqno = ? "
            + " and nvl(acct_month,'x') between ? and ? ";
        sqlSelect(sql1, new Object[] {
            lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4], lsItem[5], hCard.pSeqno, lsLastYear
                + "01", lsLastYear + "12"
        });

        if (sqlNrow <= 0) {
            hErrReason = "SQL error:[mkt_card_consume]";
            insertCmsRoadmasterLog();
            return -1;
        }

        idcLastAmt = colInt("idc_last_amt");

        return 1;

    }
// ************************************************************************
    public int billAmtCard(String asAcctCode[],String asItFlag,int aiMm,double adcOneLowAmt) throws Exception {
        // --RC: 1.OK, -1.error
        // =======================================================
        String[] lsItem = new String[6];
        String lsThisAcctMonth = "" , lsThisAcctMonth2 = "";
        int ii = 0;

        if (empty(hCard.cardNo)) {
            hErrReason = "未取得 卡片資料(card_no)";
            insertCmsRoadmasterLog();
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

        String sql1 = " select "
            + " this_acct_month as ls_this_acct_month "
            + " from ptr_workday "
            + " where stmt_cycle = "
            + " (select stmt_cycle from crd_card where card_no = ? ) ";
        sqlSelect(sql1, new Object[] {
            hCardNo
        });

        if (sqlNrow <= 0) {
            hErrReason = "SQLerror:[ptr_workday] ";
            insertCmsRoadmasterLog();
            return -1;
        }

        lsThisAcctMonth = colSs("ls_this_acct_month");
        lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1+aiMm*-1);
        idcPurchAmt = 0;
        ilPurchCnt = 0;
        
        String sql2 = " select "
                        + " count(*) as purch_cnt , "
                        + " sum(decode(a.acct_code,'IT',decode(?,'2',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)),case when a.txn_code in ('06','25','27','28','29')then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                        + " from bil_contract b left join bil_bill a on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                        + " where a.p_seqno = ? "
                        + " and a.major_card_no in (select major_card_no from crd_card where card_no = ?) "
                        + " and a.acct_code in "
                        + " (decode(?,'Y','AO','XX') , decode(?,'Y','BL','XX') , decode(?,'Y','CA','XX') , "
                        + "  decode(?,'Y','ID','XX') , decode(?,'Y','IT','XX') , decode(?,'Y','OT','XX') ) "
                        + " and ( (a.txn_code in ('06','25','27','28','29')) or (txn_code not in ('06','25','27','28','29') and a.dest_amt > ?)) "
                        + " and acct_month between ? and ? "
                        ;
        
        ppp(1,asItFlag);
        ppp(2,hCard.pSeqno);
        ppp(3,hCard.cardNo);
        ppp(4,lsItem[0]);
        ppp(5,lsItem[1]);
        ppp(6,lsItem[2]);
        ppp(7,lsItem[3]);
        ppp(8,lsItem[4]);
        ppp(9,lsItem[5]);
        ppp(10,adcOneLowAmt);
        ppp(11,lsThisAcctMonth);
        ppp(12,lsThisAcctMonth2);
        
        sqlSelect(sql2);
        
        idcPurchAmt = colInt("purch_amt");
        ilPurchCnt = colInt("purch_cnt");

        return 1;

    }
// ************************************************************************
    public int lastAmtCard(String asItem[]) throws Exception {
        String[] lsItem = new String[6];
        String lsLastYear = "";
        int ii = 0;

        if (empty(hCard.cardNo)) {
            hErrReason = "未取得持卡人 卡片資料[card_no]";
            insertCmsRoadmasterLog();
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

        String sql1 = " select "
            + " sum("
            + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)"
            + " ) as idc_last_amt "
            + " from mkt_card_consume "
            + " where major_card_no in "
            + " (select major_card_no from crd_card where card_no = ? ) "
            + " and nvl(acct_month,'x') between ? and ? ";
        sqlSelect(sql1, new Object[] {
            lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4], lsItem[5], hCard.cardNo, lsLastYear
                + "01", lsLastYear + "12"
        });

        if (sqlNrow <= 0) {
            hErrReason = "error:[mkt_card_consume]";
            insertCmsRoadmasterLog();
            return -1;
        }

        idcLastAmt =(int) colNum("idc_last_amt");

        return 1;

    }
// ************************************************************************
    public int billAmtId(
            String asAcctCode[],
            String asItFlag,
            int aiMm,
            double adcOneLowAmt) throws Exception {
        // --RC: 1.OK, -1.error
        // =======================================================
        String[] lsItem = new String[6];
        String lsThisAcctMonth = "" , lsThisAcctMonth2 = "";
        int ii = 0;

        if (empty(hCard.idPSeqno)) {
            hErrReason = "未取得 卡人流水號(id_p_seqno)";
            insertCmsRoadmasterLog();
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

        String sql1 = " select "
            + " this_acct_month as ls_this_acct_month "
            + " from ptr_workday "
            + " where stmt_cycle = "
            + " (select stmt_cycle from crd_card where card_no = ? ) ";
        sqlSelect(sql1, new Object[] {
                hCardNo
        });

        if (sqlNrow <= 0) {
            hErrReason = "SQLerror:[ptr_workday] ";
            insertCmsRoadmasterLog();
            return -1;
        }

        lsThisAcctMonth = colSs("ls_this_acct_month");
        lsThisAcctMonth2 = commDate.monthAdd(lsThisAcctMonth, 1+aiMm*-1);
        idcPurchAmt = 0;
        ilPurchCnt = 0;
        
        String sql2 = " select "
                        + " count(*) as purch_cnt , "
                        + " sum(decode(a.acct_code,'IT',decode(?,'2',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)),case when a.txn_code in ('06','25','27','28','29')then a.dest_amt*-1 else a.dest_amt end)) as purch_amt "
                        + " from bil_contract b left join bil_bill a on b.contract_no = nvl(a.contract_no,'x') and b.contract_seq_no = a.contract_seq_no "
                        + " where a.p_seqno =? "
                        + " and a.id_p_seqno =? "
                        + " and a.acct_code in "
                        + " (decode(?,'Y','AO','XX') , decode(?,'Y','BL','XX') , decode(?,'Y','CA','XX') , "
                        + "  decode(?,'Y','ID','XX') , decode(?,'Y','IT','XX') , decode(?,'Y','OT','XX') ) "
                        + " and ( (a.txn_code in ('06','25','27','28','29')) or (txn_code not in ('06','25','27','28','29') and a.dest_amt > ?)) "
                        + " and acct_month between ? and ? "
                        ;
        
        ppp(1,asItFlag);
        ppp(2,hCard.pSeqno);
        ppp(3,hCard.idPSeqno);
        ppp(4,lsItem[0]);
        ppp(5,lsItem[1]);
        ppp(6,lsItem[2]);
        ppp(7,lsItem[3]);
        ppp(8,lsItem[4]);
        ppp(9,lsItem[5]);
        ppp(10,adcOneLowAmt);
        ppp(11,lsThisAcctMonth);
        ppp(12,lsThisAcctMonth2);
        
        sqlSelect(sql2);
        
        idcPurchAmt = colInt("purch_amt");
        ilPurchCnt = colInt("purch_cnt");

        return 1;

    }
// ************************************************************************
    public int lastAmtId(String asItem[]) throws Exception {
        String[] lsItem = new String[6];
        String lsLastYear = "";
        int ii = 0;

        if (empty(hCard.idPSeqno)) {
            hErrReason = "未取得 卡人流水號(id_p_seqno)";
            insertCmsRoadmasterLog();
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

        String sql1 = " select "
            + " sum("
            + "decode(cast(? as varchar(1)),'Y',consume_ao_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_bl_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ca_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_id_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_it_amt,0)+"
            + "decode(cast(? as varchar(1)),'Y',consume_ot_amt,0)"
            + " ) as idc_last_amt "
            + " from mkt_card_consume "
            + " where id_p_seqno = ? "
            + " and nvl(acct_month,'x') between ? and ? ";
        sqlSelect(sql1, new Object[] {
            lsItem[0], lsItem[1], lsItem[2], lsItem[3], lsItem[4], lsItem[5], hCard.idPSeqno, lsLastYear
                + "01", lsLastYear + "12"
        });

        if (sqlNrow <= 0) {
            hErrReason = "error:[mkt_card_consume]";
            insertCmsRoadmasterLog();
            return -1;
        }

        idcLastAmt =(int) colNum("idc_last_amt");

        return 1;

    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_free(String as_card_no, String as_car) throws Exception {
//      String ls_card_no = "", ls_car = "";
//      long ll_cnt = 0;
//
//      h_err_reason = "";
//
//      ls_card_no = nvl(as_card_no, "");
//
//      if (empty(ls_card_no)) {
//          h_err_reason = "卡號不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ls_car = nvl(as_car, "");
//      if (empty(ls_car)) {
//          h_err_reason = "車號 不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      String sql1 = " select "
//          + " count(*) as ll_cnt "
//          + " from cms_roadmaster "
//          + " where rm_carno = ? "
//          + " and card_no <> ? "
//          + " and (nvl(rm_status,'0') <> '0' or (rm_status ='0' and rm_reason = '2')) ";
//      sqlSelect(sql1, new Object[] {
//          ls_car, ls_card_no
//      });
//
//      if (sql_nrow <= 0) {
//          h_err_reason = "read cms_roadmaster error";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ll_cnt = col_int("ll_cnt");
//      if (ll_cnt > 0)
//          return 1;
//
//      return 0;
//
//    }
// ************************************************************************
// Rds_func 沒使用到
//    public int car_unique_expn(String as_card_no, String as_car) throws Exception {
//      // --RC: -1.error, 0.no register, 1.registed
//      // ============================================
//      String ls_card_no = "", ls_car = "";
//      long ll_cnt = 0;
//      h_err_reason = "";
//
//      ls_card_no = nvl(as_card_no, "");
//      if (empty(ls_card_no)) {
//          h_err_reason = "卡號不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ls_car = nvl(as_car, "");
//      if (empty(ls_car)) {
//          h_err_reason = "車號 不可空白";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      String sql1 = " select "
//          + " count(*) as ll_cnt "
//          + " from cms_roadmaster "
//          + " where rm_carno = ? "
//          + " and card_no <> ? "
//          + " and nvl(rm_status,'0') <> '0' "
//          + " and rm_validdate >= to_char(sysdate,'yyyymm') ";
//      sqlSelect(sql1, new Object[] {
//          ls_car, ls_card_no
//      });
//
//      if (sql_nrow <= 0) {
//          h_err_reason = "read cms_roadmaster error[car_unique_expn]";
//          insert_cms_roadmaster_log();
//          return -1;
//      }
//
//      ll_cnt = col_int("ll_cnt");
//      if (ll_cnt > 0)
//          return 1;
//
//      return 0;
//
//    }
// ************************************************************************
    public void insertCmsRoadmasterLog() throws Exception {
        
        setValue("moddate", sysDate);
        setValueInt("seqno", autonoErrLog(sysDate));
        setValue("card_no", hCardNo);
        setValue("rds_pcard", hRoad.rdsPcard);
        setValue("rm_carno", hCarNo);
        setValue("rm_carmanname", hIdno.chiName);
        setValue("rm_carmanid", hIdno.idNo);
        setValue("id_p_seqno", hIdPSeqno);
        setValue("err_reason", hErrReason);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hPgmName);
        daoTable = "cms_roadmaster_log";
        tmpInt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cms_roadmaster_log duplicate!", "", comcr.hCallBatchSeqno);
        } 
    }
 // ************************************************************************
    public void insertCmsRoaddetail() throws Exception {
        setValue("rd_moddate", sysDate);
        setValueInt("rd_seqno", autonoDtl(sysDate));   
        setValue("rd_modtype", "B");
        setValue("card_no", hCardNo);
        setValue("new_card_no", "");
        setValue("rd_type", "F"); //F:免費 E:自費
        setValue("appl_card_no", hCardNo);
        setValue("group_code", hCard.groupCode);
        setValue("rd_carno", hCarNo);
        setValue("rd_carmanname", hIdno.chiName);
        setValue("rd_carmanid", hIdno.idNo);
        setValue("rd_newcarno", "");
        setValue("rd_htelno1", hIdno.homeAreaCode1);
        setValue("rd_htelno2", hIdno.homeTelNo1);
        setValue("rd_htelno3", hIdno.homeTelExt1);
        setValue("rd_otelno1", hIdno.officeAreaCode1);
        setValue("rd_otelno2", hIdno.officeTelNo1);
        setValue("rd_otelno3", hIdno.officeTelExt1);
        setValue("cellar_phone", hIdno.cellarPhone);
        setValue("rd_validdate", hRode.rdValiddate);
        setValue("rd_status", hRode.rdStatus);
        setValueDouble("rd_payamt", 0); //免費 自費金額0
        setValue("rd_payno", ""); //請款批號
        setValue("rd_paydate", ""); //請款日期
        setValue("rd_stopdate", ""); //停用日期
        setValue("rd_stoprsn", ""); //停用原因
        setValue("crt_user", "rdsb020");
        setValue("crt_date", sysDate);
        setValue("apr_user", "BATCH");
        setValue("apr_date", sysDate);
        setValue("rd_senddate", "");
        setValue("rd_sendsts", "");
        setValue("rd_sendyn", "");
        setValueInt("rd_sendadd", 0);
        setValueInt("rd_sendstop", 0);
        setValue("proj_no", hRoadparm2.projNo);
        setValueDouble("purch_amt", idcPurchAmt);
        setValueInt("purch_cnt", ilPurchCnt);
        setValueDouble("purch_amt_lyy", idcLastAmt);
        setValue("cardholder_type", isChType);
        setValue("mod_user", "rdsb020");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hPgmName);
        setValue("rds_pcard", hRode.rdsPcard);
        setValue("id_p_seqno", hIdPSeqno);
        daoTable = "cms_roaddetail";
        tmpInt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cms_roaddetail duplicate!", "", comcr.hCallBatchSeqno);
        } 
    }
    // ************************************************************************
    public void insertCmsRoadmaster() throws Exception {
        setValue("card_no", hCardNo);
        setValue("rm_type", "F"); //F:免費 E:自費
        setValue("rm_carno", hCarNo);
        setValue("group_code", hCard.groupCode);     
        setValue("rm_carmanname", hIdno.chiName);
        setValue("rm_carmanid", hIdno.idNo);
        setValue("rm_oldcarno", "");
        setValue("rm_htelno1", hIdno.homeAreaCode1);
        setValue("rm_htelno2", hIdno.homeTelNo1);
        setValue("rm_htelno3", hIdno.homeTelExt1);
        setValue("rm_otelno1", hIdno.officeAreaCode1);
        setValue("rm_otelno2", hIdno.officeTelNo1);
        setValue("rm_otelno3", hIdno.officeTelExt1);
        setValue("cellar_phone", hIdno.cellarPhone);
        setValue("rm_status", hRoad.rmStatus);
        setValue("rm_validdate", hRoad.rmValiddate);
        setValue("rm_moddate", sysDate);
        setValue("rm_reason", "");
        setValue("rm_payno", "");
        setValue("rm_paydate", "");
        setValue("crt_user", "rdsb020");
        setValue("crt_date", sysDate);
        setValue("apr_user", "BATCH");
        setValue("apr_date", sysDate);
        setValue("never_check", hRoad.neverCheck);
        setValue("mod_user", "rdsb020");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hPgmName);
        setValue("rds_pcard", hRoad.rdsPcard);
        setValue("id_p_seqno", hIdPSeqno);
        daoTable = "cms_roadmaster";
        tmpInt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cms_roadmaster duplicate!", "", comcr.hCallBatchSeqno);
        } 
    }
    // ************************************************************************
    public void insertCmsRoaddetailBc() throws Exception {
        setValue("rd_moddate", hRode.rdModdate);
        setValueInt("rd_seqno", 0);   
        setValue("rd_modtype", hRode.rdModtype);
        setValue("card_no", hRode.cardNo);
        setValue("new_card_no", hRode.newCardNo);
        setValue("rd_type", hRode.rdType); //F:免費 E:自費
        setValue("appl_card_no", hRode.applCardNo);
        setValue("group_code", hRode.groupCode);
        setValue("rd_carno", hRode.rdCarno);
        setValue("rd_carmanname", hRode.rdCarmanname);
        setValue("rd_carmanid", hRode.rdCarmanid);
        setValue("rd_newcarno", hRode.rdNewcarno);
        setValue("rd_htelno1", hRode.rdHtelno1);
        setValue("rd_htelno2", hRode.rdHtelno2);
        setValue("rd_htelno3", hRode.rdHtelno3);
        setValue("rd_otelno1", hRode.rdOtelno1);
        setValue("rd_otelno2", hRode.rdOtelno2);
        setValue("rd_otelno3", hRode.rdOtelno3);
        setValue("cellar_phone", hRode.cellarPhone);
        setValue("rd_validdate", hRode.rdValiddate);
        setValue("rd_status", hRode.rdStatus);
        setValueDouble("rd_payamt", hRode.rdPayamt); //免費 自費金額0
        setValue("rd_payno", hRode.rdPayno);
        setValue("rd_paydate", hRode.rdPaydate);
        setValue("rd_stopdate", hRode.rdStopdate);
        setValue("rd_stoprsn", hRode.rdStoprsn);
        setValue("crt_user", hRode.crtUser);
        setValue("crt_date", hRode.crtDate);
        setValue("apr_user", hRode.aprUser);
        setValue("apr_date", hRode.aprDate);
        setValue("rd_senddate", hRode.rdSenddate);
        setValue("rd_sendsts", hRode.rdSendsts);
        setValue("rd_sendyn", hRode.rdSendyn);
        setValueInt("rd_sendadd", hRode.rdSendadd);
        setValueInt("rd_sendstop", hRode.rdSendstop);
        setValue("proj_no", hRode.projNo);
        setValueDouble("purch_amt", hRode.purchAmt);
        setValueInt("purch_cnt", hRode.purchCnt);
        setValueDouble("purch_amt_lyy", hRode.purchAmtLyy);
        setValue("cardholder_type", hRode.cardholderType);
        setValue("mod_user", hRode.modUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hPgmName);
        setValue("rds_pcard", hRode.rdsPcard);
        setValue("id_p_seqno", hRode.idPSeqno);
        daoTable = "cms_roaddetail";
        tmpInt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cms_roaddetail duplicate!", "", comcr.hCallBatchSeqno);
        } 
    }
    // ************************************************************************
    public void insertCmsRoaddetailC() throws Exception {
        setValue("rd_moddate", sysDate);
        setValueInt("rd_seqno", autonoDtl(sysDate));   
        setValue("rd_modtype", "B");
        setValue("card_no", hRode.cardNo);
        setValue("new_card_no", hCardNo);
        setValue("rd_type", "F"); //F:免費 E:自費
        setValue("appl_card_no", hRode.applCardNo);
        setValue("group_code", hRode.groupCode);
        setValue("rd_carno", hRode.rdCarno);
        setValue("rd_carmanname", hRode.rdCarmanname);
        setValue("rd_carmanid", hRode.rdCarmanid);
        setValue("rd_newcarno", hRode.rdNewcarno);
        setValue("rd_htelno1", hIdno.homeAreaCode1);
        setValue("rd_htelno2", hIdno.homeTelNo1);
        setValue("rd_htelno3", hIdno.homeTelExt1);
        setValue("rd_otelno1", hIdno.officeAreaCode1);
        setValue("rd_otelno2", hIdno.officeTelNo1);
        setValue("rd_otelno3", hIdno.officeTelExt1);
        setValue("cellar_phone", hIdno.cellarPhone);
        setValue("rd_validdate", hRode.rdValiddate);
        setValue("rd_status", hRode.rdStatus);
        setValueDouble("rd_payamt", 0); //免費 自費金額0
        setValue("rd_payno", hRode.rdPayno);
        setValue("rd_paydate", hRode.rdPaydate);
        setValue("rd_stopdate", hRode.rdStopdate);
        setValue("rd_stoprsn", hRode.rdStoprsn);
        setValue("crt_user", "rdsb020");
        setValue("crt_date", sysDate);
        setValue("apr_user", "BATCH");
        setValue("apr_date", sysDate);
        setValue("rd_senddate", hRode.rdSenddate);
        setValue("rd_sendsts", hRode.rdSendsts);
        setValue("rd_sendyn", hRode.rdSendyn);
        setValueInt("rd_sendadd", hRode.rdSendadd);
        setValueInt("rd_sendstop", hRode.rdSendstop);
        setValue("proj_no", hRoadparm2.projNo);
        setValueDouble("purch_amt", hRode.purchAmt);
        setValueInt("purch_cnt", hRode.purchCnt);
        setValueDouble("purch_amt_lyy", hRode.purchAmtLyy);
        setValue("cardholder_type", hRode.cardholderType);
        setValue("mod_user", "rdsb020");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hPgmName);
        setValue("rds_pcard", hRode.rdsPcard);
        setValue("id_p_seqno", hIdPSeqno);
        daoTable = "cms_roaddetail";
        tmpInt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cms_roaddetail duplicate!", "", comcr.hCallBatchSeqno);
        } 
    }
    // ************************************************************************
    public void updateCmsRoadmaster() throws Exception {
        daoTable   = "cms_roadmaster";
        updateSQL  = " card_no   = ?,";
        updateSQL += " rm_type   = ?, ";
        updateSQL += " rm_carno   = ?, ";
        updateSQL += " group_code   = ?, ";
        updateSQL += " rm_carmanname   = ?, ";
        updateSQL += " rm_carmanid   = ?, ";
        updateSQL += " rm_oldcarno   = ?, ";
        updateSQL += " rm_htelno1   = ?, ";
        updateSQL += " rm_htelno2   = ?, ";
        updateSQL += " rm_htelno3   = ?, ";
        updateSQL += " rm_otelno1   = ?, ";
        updateSQL += " rm_otelno2   = ?, ";
        updateSQL += " rm_otelno3   = ?, ";
        updateSQL += " cellar_phone   = ?, ";
        updateSQL += " rm_status   = ?, ";
        updateSQL += " rm_validdate   = ?, ";
        updateSQL += " rm_moddate   = ?, ";
        updateSQL += " rm_reason   = ?, ";
        updateSQL += " rm_payno   = ?, ";
        updateSQL += " rm_paydate   = ?, ";
        updateSQL += " crt_user   = ?, ";
        updateSQL += " crt_date   = ?, ";
        updateSQL += " apr_user   = ?, ";
        updateSQL += " apr_date   = ?, ";
        updateSQL += " never_check   = ?, ";
        updateSQL += " mod_user   = ?, ";
        updateSQL += " mod_time   = sysdate, ";
        updateSQL += " mod_pgm   = ?, ";
        updateSQL += " rds_pcard   = ? ";
        whereStr = "where id_p_seqno  = ? ";
        setString(1, hCardNo);
        setString(2, "F"); //F:免費 E:自費
        setString(3, hCarNo);
        setString(4, hCard.groupCode);
        setString(5, hIdno.chiName);
        setString(6, hIdno.idNo);
        setString(7, hRoad.rmOldcarno);
        setString(8, hIdno.homeAreaCode1);
        setString(9, hIdno.homeTelNo1);
        setString(10, hIdno.homeTelExt1);
        setString(11, hIdno.officeAreaCode1);
        setString(12, hIdno.officeTelNo1);
        setString(13, hIdno.officeTelExt1);
        setString(14, hIdno.cellarPhone);
        setString(15, hRoad.rmStatus);
        setString(16, hRoad.rmValiddate);
        setString(17, sysDate);
        setString(18, hRoad.rmReason);
        setString(19, "");
        setString(20, "");
        setString(21, "rdsb020");
        setString(22, sysDate);
        setString(23, "BATCH");
        setString(24, sysDate);
        setString(25, hRoad.neverCheck);
        setString(26, "rdsb020");
        setString(27, hPgmName);
        setString(28, hRoad.rdsPcard);
        setString(29, hIdPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_cms_roadmaster not found!", "", comcr.hCallBatchSeqno);
        }

    }
// ************************************************************************
    public void allInit() {
        hErrReason = "";
        hRoad.initData();
        hRode.initData();
        hCard.initData();
        hIdno.initData();
        hRoadparm2.initData();
        changeCardFlag = "N";
        isRentCar = "N";
    }
// ************************************************************************
    protected boolean isEmpty(String string1) {
        if (string1 == null)
            return true;
        if (string1.trim().length() == 0)
            return true;

        return false;
    }
    protected int chkStrend(String lsOppostDate, String lsSysDate2) {
        if (isEmpty(lsOppostDate) || isEmpty(lsSysDate2))
            return 1;
        if (nvl(lsOppostDate).compareTo(nvl(lsSysDate2)) > 0)
            return -1;

        return 1;
    }
    protected String nvl(String string1) {
        if (string1 == null) {
            return "";
        }
        return string1.trim();
    }
    @Override
    protected void dataProcess(String[] args) throws Exception {
        // TODO Auto-generated method stub
        
    }

// ************************************************************************
}