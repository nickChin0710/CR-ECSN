/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  109-07-22  V1.00.01  Zuwei       updated for project coding standard      *
*                                                                            *  
******************************************************************************/
package Dxc.Util.Pgp;

public class PgpFunction {

  public PgpFunction() {
    // TODO Auto-generated constructor stub
  }

  public boolean encryptFile(String sPFullPathSourceFileName, String sPFullPathTargetFileName,
      String sPPassPhrase, String sPFullPathPublicKeyFileName) {

    boolean bLResult = true;
    try {
      PGPFileProcessor p = new PGPFileProcessor();
      p.setInputFileName(sPFullPathSourceFileName);
      p.setOutputFileName(sPFullPathTargetFileName);
      p.setPassphrase(sPPassPhrase);
      p.setPublicKeyFileName(sPFullPathPublicKeyFileName);
      p.encrypt();

    } catch (Exception e) {
      // TODO: handle exception
      bLResult = false;
    }

    return bLResult;

  }

  public boolean decryptFile(String sPFullPathSourceFileName, String sPFullPathTargetFileName,
      String sPPassPhrase, String sPFullPathPrivateKeyFileName) {

    boolean bLResult = true;
    try {
      PGPFileProcessor p = new PGPFileProcessor();
      p.setInputFileName(sPFullPathSourceFileName);
      p.setOutputFileName(sPFullPathTargetFileName);
      p.setPassphrase(sPPassPhrase);
      p.setSecretKeyFileName(sPFullPathPrivateKeyFileName);
      p.decrypt();

    } catch (Exception e) {
      // TODO: handle exception
      bLResult = false;
    }

    return bLResult;

  }

}
