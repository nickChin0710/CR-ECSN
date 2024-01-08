package bank.authbatch.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;


import bank.authbatch.vo.CcaSysParm3Vo;
import bank.authbatch.vo.Data130Vo;
import bank.AuthIntf.AuthGate;
import bank.AuthIntf.BicFormat;
import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaCardOpenDao;
import bank.authbatch.dao.CcaOutgoingDao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.SystemDao;

public class AuthBatch130 extends BatchProgBase {

    String UID_NAME = "ECS130";
    String CB_CARD_ACCT_CLASS = "";
    String CB_DEBIT_FLAG = "";
    String CB_EFF_DATE_END = "";
    String CB_OLD_CARD_EFF_DATE_END = "";
    String h_card_no = "";
    String h_card_valid_to = "";
    String h_card_launch_type = "";
    String h_card_launch_date = "";
    String h_card_contract = "";

    public int startProcess(Data130Vo P_Data130Vo) {

        getData(P_Data130Vo);

        int nL_Result = -1;
        try {

//       #if DEBUG
//                  fprintf(stderr,"card_no[%s] valid_to[%s] launch type[%s] date[%s]\n",
//        h_m_card_no[int1].arr,h_m_card_valid_to[int1].arr,
//        h_m_card_launch_type[int1].arr,h_m_card_launch_date[int1].arr);
//       #endif
            if (TB_card_base() == false)
                return nL_Result;
            nL_Result = 0; /** success **/
            if (proc_open_card() == false) {
                nL_Result = -1; /** error **/
            }
            if (CB_CARD_ACCT_CLASS.equals("A4") == false) {
                TB_outgoing();
            }
            return nL_Result;

        } catch (Exception e) {
            // TODO: handle exception
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String sRtn = sw.toString();
            sw = null;

            System.out.println(sRtn);
            nL_Result = -1;
            return nL_Result;
        }

    }

    /***************************************************************/
    void getData(Data130Vo P_Data130Vo) {
        h_card_no = P_Data130Vo.getCardNo();
        h_card_valid_to = P_Data130Vo.getCardValidTo();
        h_card_launch_type = P_Data130Vo.getCardLaunchType();
        h_card_launch_date = P_Data130Vo.getCardLaunchDate();
        h_card_contract = P_Data130Vo.getContract();
    }

    /***************************************************************/
    boolean TB_card_base() throws Exception {

        String sL_Sql = " SELECT (select card_acct_class from cca_card_acct_index where B.card_acct_idx = card_acct_idx fetch first 1 rows only) as CARD_ACCT_CLASS, "
                + "              case when A.DEBIT_FLAG = 'Y' then (select new_end_date from dbc_card where card_no = A.CARD_NO) else (select new_end_date from crd_card where card_no = A.CARD_NO) end as EFF_DATE_END, "
                + "              case when A.DEBIT_FLAG = 'Y' then (select old_end_date from dbc_card where card_no = A.CARD_NO) else (select old_end_date from crd_card where card_no = A.CARD_NO) end as OLD_CARD_EFF_DATE_END, "
                + "              A.DEBIT_FLAG "
                + "        FROM  CCA_CARD_BASE A,CCA_CARD_ACCT B "
                + "       WHERE  A.CARD_NO = ? " 
                + "         AND  B.CARD_ACCT_IDX = A.CARD_ACCT_IDX ";

        ResultSet sL_CardBaseRS = CcaCardBaseDao.getCcaCardBase130(sL_Sql, h_card_no);
        sL_CardBaseRS.next();
        CB_CARD_ACCT_CLASS = sL_CardBaseRS.getString("CARD_ACCT_CLASS");
        CB_EFF_DATE_END = sL_CardBaseRS.getString("EFF_DATE_END");
        CB_OLD_CARD_EFF_DATE_END = sL_CardBaseRS.getString("OLD_CARD_EFF_DATE_END");
        CB_DEBIT_FLAG = sL_CardBaseRS.getString("DEBIT_FLAG");
        sL_CardBaseRS.close();

        return true;
    }

    /***************************************************************/
    boolean proc_open_card() throws Exception {
        String szOldNew = "";
        String CardOpen_EFF_DATE_END = "";
        String CardOpen_NEW_OLD = "";

        String CB_CARD_OPEN_FLAG = "";
        String CB_CARD_OPEN_SOURCE = "";
        String CB_OPEN_DATE = "";
        String CB_OPEN_UID = "";

        if (CB_EFF_DATE_END.equals(h_card_valid_to)) /* 新卡 */
            szOldNew = "N";
        else
            szOldNew = "O";

        CardOpen_EFF_DATE_END = "";
        CardOpen_NEW_OLD = szOldNew;

        int sL_Result = 0;
        String sL_Sql = "";
        
        if (szOldNew.equals("N")) /* 新卡 */
        {
            CardOpen_EFF_DATE_END = CB_EFF_DATE_END;
            CB_CARD_OPEN_FLAG = "2"; /* open */
            CB_CARD_OPEN_SOURCE = h_card_launch_type;
            CB_OPEN_DATE = h_card_launch_date;
            if ((CB_OPEN_DATE.length() == 0) || (CB_OPEN_DATE.equals("      ")))
                CB_CARD_OPEN_FLAG = "1"; /* close */

            if (h_card_contract.length() > 0) {
                CB_OPEN_UID = h_card_contract;
            } else {
                CB_OPEN_UID = UID_NAME;
            }

//             sL_Sql = " UPDATE  CCA_CARD_BASE " 
//                    + "    SET  CARD_OPEN_FLAG   = ?, " 
//                    + "         CARD_OPEN_SOURCE = ?, "
//                    + "         OPEN_DATE        = ?, " 
//                    + "         OPEN_TIME        = ?, "
//                    + "         OPEN_UID         = ? " 
//                    + "  WHERE  CARD_NO = ? ";
            if (CB_DEBIT_FLAG.equals("Y"))
                sL_Sql = "UPDATE DBC_CARD ";
            else
                sL_Sql = "UPDATE CRD_CARD ";
            sL_Sql += "      SET ACTIVATE_TYPE = ?, " 
                    + "          ACTIVATE_FLAG = ?, "
                    + "          ACTIVATE_DATE = ? "
                    + "   WHERE  CARD_NO = ? ";
                
        } else {
            CardOpen_EFF_DATE_END = CB_OLD_CARD_EFF_DATE_END;
            CB_CARD_OPEN_FLAG = "2"; /* open */
            CB_CARD_OPEN_SOURCE = h_card_launch_type;
            CB_OPEN_DATE = h_card_launch_date;
            if ((CB_OPEN_DATE.length() == 0) || (CB_OPEN_DATE.equals("      ")))
                CB_CARD_OPEN_FLAG = "1"; /* close */

            if (h_card_contract.length() > 0) {
                CB_OPEN_UID = h_card_contract;
            } else {
                CB_OPEN_UID = UID_NAME;
            }

//             sL_Sql = " UPDATE  CCA_CARD_BASE " 
//                    + "    SET  OLD_CARD_OPEN_FLAG   = ?, " 
//                    + "         OLD_CARD_OPEN_SOURCE = ?, "
//                    + "         OLD_OPEN_DATE        = ?, " 
//                    + "         OLD_OPEN_TIME        = ?, "
//                    + "         OLD_OPEN_UID         = ? " 
//                    + "  WHERE  CARD_NO = ? ";
            if (CB_DEBIT_FLAG.equals("Y"))
                sL_Sql = "UPDATE DBC_CARD ";
            else
                sL_Sql = "UPDATE CRD_CARD ";
            sL_Sql += "      SET OLD_ACTIVATE_TYPE = ?, " 
                    + "          OLD_ACTIVATE_FLAG = ?, "
                    + "          OLD_ACTIVATE_DATE = ? "
                    + "   WHERE  CARD_NO = ? ";
        }
        sL_Result = CcaCardBaseDao.updateCcaCardBase130(sL_Sql, CB_CARD_OPEN_SOURCE, CB_CARD_OPEN_FLAG, CB_OPEN_DATE,
                h_card_no);

        if (sL_Result != 0) {
            System.out.println("update card_base error");
            return false;
        }
        sL_Sql = "   INSERT INTO CCA_CARD_OPEN " 
                + "      (CARD_NO,new_end_date ,new_old_flag , "
                + "          open_type , OPEN_DATE, open_user ) " 
                + "   VALUES(?,?,?,?,?,?) ";
        sL_Result = CcaCardOpenDao.insertCcaCardOpen130(sL_Sql, h_card_no, CardOpen_EFF_DATE_END, CardOpen_NEW_OLD,
                CB_CARD_OPEN_SOURCE, CB_OPEN_DATE, CB_OPEN_UID);
        if (sL_Result == -1) { /* duplicated */
            sL_Sql = " UPDATE CCA_CARD_OPEN " 
                    + "  set new_old_flag      = ?, " 
                    + "      open_type  = ?, "
                    + "      OPEN_DATE    = ?, " 
                    + "      open_user     = ? " 
                    + "WHERE CARD_NO      = ? "
                    + "  and new_end_date = ? ";
            sL_Result = CcaCardOpenDao.updateCcaCardOpen130(sL_Sql, CardOpen_NEW_OLD, CB_CARD_OPEN_SOURCE, CB_OPEN_DATE,
                    CB_OPEN_UID, h_card_no, CardOpen_EFF_DATE_END);

        }
        if (sL_Result != 0) {
            System.out.println("insert card_open error");
            return false;
        }
        return true;
    }

    /** Exception *************************************************************/
    boolean TB_outgoing() throws Exception {
        String h_key_value = "";
        String h_key_table = "";
        String h_bitmap = "";
        String h_sucess_flag = "";
        long h_send_times = 0;
        String h_act_code = "";

        h_key_value = "";
        h_key_table = "";
        h_bitmap = "";
        h_sucess_flag = "";
        h_act_code = "";
        h_send_times = 0;
        h_act_code = "D";
        /* ================================================================= */
        /* =Process NCCC neg file ISO8583 string============================ */
        /* ================================================================= */
        h_key_table = "CARD_OPEN";
        h_key_value = "NCCC";
        String szISOString = "";
        /* Set ISO8583 結構 */
        szISOString = EDS_Iso2Bit();
        if (szISOString.length() == 0) {
            System.out.println("Iso2Bit error");
            return false;
        }
        h_bitmap = szISOString;
        h_sucess_flag = "0";
        /* ================================================================= */
        String sql = "   INSERT INTO CCA_OUTGOING " 
                + "         (CARD_NO,KEY_VALUE,KEY_TABLE, "
                + "          BITMAP,PROC_FLAG,SEND_TIMES,CRT_DATE,CRT_TIME, "
                + "          CRT_USER,PROC_DATE,PROC_TIME,PROC_USER,ACT_CODE) "
                + "   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ";

        if (CcaOutgoingDao.insertOutgoing(sql, h_card_no, h_key_value, h_key_table, h_bitmap, h_sucess_flag,
                h_send_times, HpeUtil.getCurDateStr(""), HpeUtil.getCurTimeStr(), UID_NAME, HpeUtil.getCurDateStr(""),
                HpeUtil.getCurTimeStr(), UID_NAME, h_act_code))
            return false;

        return true;

    }

    /**
     * @throws Exception
     *************************************************************/
    String EDS_Iso2Bit() throws Exception {

        String ls_ncccnew = "0";

        if (CcaSysParm3Dao.getCcaSysParm3("NCCC", "VERSION", true, "N")) {
            ls_ncccnew = CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1;
        }

        AuthGate gate = new AuthGate();
        if (ls_ncccnew.equals("1"))
            gate.bicHead = "ISO086000051";
        else
            gate.bicHead = "ISO085000051";
        gate.mesgType = "0300";

        gate.isoField[2] = h_card_no;
        /**** AE open card ****/
        if (gate.isoField[2].substring(0, 6).equals("900002") == false) {
            if (h_card_no.substring(0, 4).equals("4000")) {
                gate.isoField[2] = "9" + h_card_no.substring(1, 16);
            } else {
                gate.isoField[2] = h_card_no.substring(0, 16);
            }
        }
        String db_SYS_SEQ_NO = SystemDao.getNextSeqVal("ECS_TRACE_NO");

        gate.isoField[7] = HpeUtil.getCurDateStr("").substring(4) + HpeUtil.getCurTimeStr();
        gate.isoField[11] = String.format("%06d", Integer.parseInt(db_SYS_SEQ_NO));

        String szSysBankVisa = "";
        if (CcaSysParm3Dao.getCcaSysParm3("FIID", "BK_ID_VISA", true, "N"))
            szSysBankVisa = CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1;
        else
            szSysBankVisa = "BK02";
        gate.isoField[48] = "000" + szSysBankVisa + "31"
                + "0000000000000000000000000000000000000000000000000000000000000000000";
        gate.isoField[49] = "901";
        if (ls_ncccnew.equals("1"))
            gate.isoField[60] = "90020000PRO200000000000000YY";
        else
            gate.isoField[60] = "90020000PRO100000000000000YY";
        gate.isoField[60] += "000000000000000000000000000000";

        gate.isoField[91] = "3";
        gate.isoField[101] = "NF";

        BicFormat bic = new BicFormat(null, gate, null);
        if (bic.host2Iso()==false) {
            return "";
        }

        return gate.isoString.substring(2);

    } /* End of ISOvoice0300 */

    /***************************************************************/

    public void initProg(Connection P_Db2Conn) throws Exception {
        setDbConn(P_Db2Conn);
//        initPrepareStatement(G_ECS080ID);
        // getAuthParm();

    }

    public AuthBatch130() throws Exception {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void startProcess(String[] sP_Parameters) {
        // TODO Auto-generated method stub

    }

}
