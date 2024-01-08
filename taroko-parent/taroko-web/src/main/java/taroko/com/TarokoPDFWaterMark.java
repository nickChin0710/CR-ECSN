/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
*                                                                            *  
******************************************************************************/
package taroko.com;

import taroko.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoPDFWaterMark {
  int waterSize = 24;
  String pngSource = "/waterMark/waterMark.png";
  String pdfWater = "";

  public void createWaterMark(TarokoCommon wp) throws Exception {

    if (wp.loginUser.length() == 0) {
      return;
    }

    wp.dateTime();
    wp.showLogMessage("I", "CreateWaterMark", "started");

    String waterText = "合作金庫商業銀行 " + wp.loginUser;
    File srcWaterMark = new File(TarokoParm.getInstance().getDataRoot() + pngSource);
    File destWaterMark = new File(TarokoParm.getInstance().getDataRoot() + "/work/" + wp.loginUser + pdfWater + ".png");

    BufferedImage sourceImage = ImageIO.read(srcWaterMark);
    Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

    // initializes necessary graphic properties
    AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
    g2d.setComposite(alphaChannel);
    g2d.rotate(-0.7);
    g2d.setColor(Color.GRAY);
    g2d.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, waterSize));
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(waterText, g2d);

    // calculates the coordinate where the String is painted
    int centerX = (sourceImage.getWidth() - (int) rect.getWidth()) / 2;
    int centerY = sourceImage.getHeight() / 2 + 25;

    // paints the textual watermark
    if (waterSize == 24) {
      g2d.drawString(waterText, centerX - 100, centerY + 30);
    } else {
      g2d.drawString(waterText, centerX - 180, centerY + 90);
    }
    waterText = wp.sysDate + wp.sysTime.substring(0, 4);
    g2d.setColor(Color.GRAY);

    if (waterSize == 24) {
      g2d.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 22));
      g2d.drawString(waterText, centerX - 50, centerY + 53);
    } else {
      g2d.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 28));
      g2d.drawString(waterText, centerX - 120, centerY + 130);
    }

    ImageIO.write(sourceImage, "png", destWaterMark);
    g2d.dispose();
    wp.dateTime();
    wp.showLogMessage("I", "CreateWaterMark", "ended");
  } // end of CreateWaterMark

} // end of class
