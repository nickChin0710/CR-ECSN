/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-28   V1.00.02 Justin        zz -> comm
* 110-01-08  V1.00.03  tanwei      修改意義不明確變量                                                                  * 
******************************************************************************/
package ecsfunc;

public class DeCodeBase {
	protected static taroko.base.CommString commString = new taroko.base.CommString();

	static String ddlbOption(String[] optionName, String[] optionTxt, String strName) {

		boolean lbFind = false;
		byte[] carriage = { 0x0D, 0x0A };
		String carStr = new String(carriage, 0, 2);
		String colName = strName.trim();
		StringBuffer sbOpt = new StringBuffer("");

		for (int ii = 0; ii < optionName.length; ii++) {
			if (strName.length() > 0 && colName.equals(optionName[ii])) {
				sbOpt.append(
						"<option value='" + optionName[ii] + "' selected >" + optionName[ii] + "." + optionTxt[ii] + "</option>" + carStr);
				lbFind = true;
			} else {
				sbOpt.append("<option value='" + optionName[ii] + "'>" + optionName[ii] + "." + optionTxt[ii] + "</option>" + carStr);
			}
		}
		if (colName.length() > 0 && lbFind == false) {
			sbOpt.append("<option value='" + strName + "' selected >" + strName + ".--</option>" + carStr);
		}

		return sbOpt.toString();
	}

	static String deCode(String sVal, String sIdtxt) {
		if (sVal.trim().length() == 0 || sIdtxt.trim().length() == 0)
			return sVal;
		String[] aaTxt = sIdtxt.split(",");
		int liLoop = aaTxt.length - 1;
		for (int ii = 0; ii < liLoop; ii++) {
			if (sVal.equals(aaTxt[ii])) {
				if (ii < liLoop) {
					return aaTxt[ii + 1];
				} else
					break;
			}
		}

		return sVal;
	}

}
