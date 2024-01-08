/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/11/29  V1.00.02  Brian      add method encryptForDb() & decryptForDb() *
* 109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *
*****************************************************************************/
package ecsfunc;

public class DEncryptForDB {

  /************************************************************************/
  /* ADD for db pass encrypt & decrypt */

  public String encryptForDb(String str1) {
    String uLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String uLetterEn = "QAZWSXEDCRFVTGBYHNUJMIKOLP";
    String lLetter = "abcdefghijklmnopqrstuvwxyz";
    String lLetterEn = "qazwsxedcrfvtgbyhnujmikolp";
    String num = "1234567890";
    String numEn = "0987654321";
    String rtnstr = "";
    char[] temarr = str1.toCharArray();
    for (char tempchar : temarr) {
      if (Character.isUpperCase(tempchar)) {
        rtnstr += uLetterEn.charAt(uLetter.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isLowerCase(tempchar)) {
        rtnstr += lLetterEn.charAt(lLetter.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isDigit(tempchar)) {
        rtnstr += numEn.charAt(num.indexOf(Character.toString(tempchar)));
        continue;
      }
      rtnstr += Character.toString(tempchar);
    }
    return rtnstr;
  }

  public String decryptForDb(String str1) {
    String uLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String uLetterEn = "QAZWSXEDCRFVTGBYHNUJMIKOLP";
    String lLetter = "abcdefghijklmnopqrstuvwxyz";
    String lLetterEn = "qazwsxedcrfvtgbyhnujmikolp";
    String num = "1234567890";
    String numEn = "0987654321";
    String rtnstr = "";
    char[] temarr = str1.toCharArray();
    for (char tempchar : temarr) {
      if (Character.isUpperCase(tempchar)) {
        rtnstr += uLetter.charAt(uLetterEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isLowerCase(tempchar)) {
        rtnstr += lLetter.charAt(lLetterEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isDigit(tempchar)) {
        rtnstr += num.charAt(numEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      rtnstr += Character.toString(tempchar);
    }
    return rtnstr;
  }

}
