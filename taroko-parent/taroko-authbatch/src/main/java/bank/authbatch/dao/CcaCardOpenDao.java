package bank.authbatch.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;

public class CcaCardOpenDao extends AuthBatchDbHandler {

    public CcaCardOpenDao() throws Exception {
        // TODO Auto-generated constructor stub
    }

    public static int insertCcaCardOpen130(String sP_Sql, String h_card_no, String cardOpen_EFF_DATE_END,
            String cardOpen_NEW_OLD, String sP_CARD_OPEN_SOURCE, String sP_OPEN_DATE, String sP_OPEN_UID) {
        int nL_Result = 0;
        try {
            PreparedStatement ps = getPreparedStatement(sP_Sql, false);
            ps.setString(1, h_card_no);
            ps.setString(2, cardOpen_EFF_DATE_END);
            ps.setString(3, cardOpen_NEW_OLD);
            ps.setString(4, sP_CARD_OPEN_SOURCE);
            ps.setString(5, sP_OPEN_DATE);
            ps.setString(6, sP_OPEN_UID);

            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex2) {
            if (ex2.getErrorCode() == 1 || ex2.getErrorCode() == -803) {
                return -1; // duplicated
            }
        } catch (Exception e) {
            // TODO: handle exception
            nL_Result = -2;
        }
        return nL_Result;
    }

    public static int updateCcaCardOpen130(String sP_Sql, String sP_cardOpen_NEW_OLD, String sP_CARD_OPEN_SOURCE,
            String sP_OPEN_DATE, String sP_OPEN_UID, String h_card_no, String sP_cardOpen_EFF_DATE_END) {
        int nL_Result = 0;
        try {
            PreparedStatement ps = getPreparedStatement(sP_Sql, false);

            ps.setString(1, sP_cardOpen_NEW_OLD);
            ps.setString(2, sP_CARD_OPEN_SOURCE);
            ps.setString(3, sP_OPEN_DATE);
            ps.setString(4, sP_OPEN_UID);
            ps.setString(5, h_card_no);
            ps.setString(6, sP_cardOpen_EFF_DATE_END);

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            // TODO: handle exception
            nL_Result = -1;
        }
        return nL_Result;
    }

}
