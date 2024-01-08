/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  110-12-23  V1.00.02  Justin      log4j1 -> log4j2                         *
******************************************************************************/
package bank.AuthIntf;

import java.util.*;

import org.apache.logging.log4j.core.Logger;

public class ConvertMessage {
  public String[] headField = new String[15];

  Logger logger = null;
  AuthGate gate = null;
  HashMap cvtHash = null;

  public ConvertMessage() {}

  /* VISA OR MASTER �榡 �ഫ�� �@�P�榡 */
  public boolean convertToCommon() {
    if (!convertMesgType("C")) {
      return false;
    }

    if (!gate.srcFormatType.equals("BIC")) {
      if (gate.requestTrans) {
        convertIsoField("C");
      } else {
        restoreIsoField("C");
      }
    }

    return true;
  }

  /* �@�P�榡 �ഫ�� VISA OR MASTER �榡 */
  public boolean convertToInterChange() {
    String cvtType = "", cvtIsoField = "";

    if (!convertMesgType("I")) {
      return false;
    }

    if (!gate.destFormatType.equals("BIC")) {
      if (gate.requestTrans) {
        convertIsoField("I");
      } else {
        restoreIsoField("I");
      }
    }

    return true;
  }

  public boolean convertMesgType(String cvtCode) {
    String cvtType = (String) cvtHash.get(cvtCode + "-0-" + gate.mesgType);
    if (cvtType == null) {
      logger.error("convert to message type Error " + cvtCode + " " + gate.mesgType);
      return false;
    }

    String[] cvtData = cvtType.split("-");
    gate.mesgType = cvtData[0];
    gate.txType = cvtData[1];
    if (gate.txType.equals("0")) {
      gate.requestTrans = true;
    } else {
      gate.requestTrans = false;
    }

    return true;
  }

  public boolean convertIsoField(String cvtCode) {
    String cvtIsoField = "";

    gate.originator = "6";
    headField[7] = "00";
    headField[12] = "00";
    if (cvtCode.equals("I") && gate.destIntfName.equals("VISA")) {
      gate.destStation = "000000";
      gate.srcStation = gate.stationId;
    }

    gate.orgiReserve =
        gate.destStation + "-" + gate.srcStation + "-" + headField[7] + "-" + headField[12] + "-"
            + gate.isoField[3] + "-" + gate.isoField[22] + "-" + gate.isoField[25] + "-" + "#";

    if (gate.isoField[3].length() > 0) {
      cvtIsoField = (String) cvtHash.get(cvtCode + "-3-" + gate.isoField[3]);
      if (cvtIsoField != null) {
        gate.isoField[3] = cvtIsoField;
      }
    }

    if (gate.isoField[22].length() > 0) {
      cvtIsoField = (String) cvtHash.get(cvtCode + "-22-" + gate.isoField[22]);
      if (cvtIsoField != null) {
        gate.isoField[22] = cvtIsoField;
      }
    }

    if (gate.isoField[25].length() > 0) {
      cvtIsoField = (String) cvtHash.get(cvtCode + "-25-" + gate.isoField[25]);
      if (cvtIsoField != null) {
        gate.isoField[25] = cvtIsoField;
      }
    }

    return true;
  }

  public boolean restoreIsoField(String cvtCode) {
    gate.respondor = "6";

    String[] tmpString = gate.orgiReserve.split("-");
    gate.destStation = tmpString[0];
    gate.srcStation = tmpString[1];
    headField[7] = tmpString[2];
    headField[12] = tmpString[3];
    gate.isoField[3] = tmpString[4];
    gate.isoField[22] = tmpString[5];
    gate.isoField[25] = tmpString[6];

    String cvtStation = gate.destStation;
    gate.destStation = gate.srcStation;
    gate.srcStation = cvtStation;
    return true;
  }


} // end of class ConvertMessage
