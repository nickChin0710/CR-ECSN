/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
*  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/08/04  V0.00.02    Zuwei     fix code scan issue                   *
*                                                                             *
******************************************************************************/
package com;

import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailObject {
  String mailServer = "";
  String portNo = "";
  String from = "";
  String to = "";
  String subject = "";
  String bodyText = "";
  String attachFile = "";

  public void sendEmail() throws Exception {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", mailServer);
    properties.put("mail.smtp.port", portNo);
    properties.put("mail.smtp.socketFactory.port", "465");
    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    Session session = Session.getDefaultInstance(properties, null);

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    subject = new String(subject.getBytes(), "BIG5");
    message.setSubject(subject, "BIG5");
    message.setSentDate(new Date());

    // Set the email message text.

    MimeBodyPart messagePart = new MimeBodyPart();
    bodyText = new String(bodyText.getBytes(), "BIG5");
    messagePart.setText(bodyText, "BIG5");

    // Set the email attachment file

    MimeBodyPart attachmentPart = new MimeBodyPart();
    FileDataSource fileDataSource = new FileDataSource(attachFile) {
      @Override
      public String getContentType() {
        return "application/octet-stream";
      }
    };
    attachmentPart.setDataHandler(new DataHandler(fileDataSource));
    attachmentPart.setFileName(attachFile);

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messagePart);
    multipart.addBodyPart(attachmentPart);
    message.setContent(multipart);
    Transport.send(message);
    return;
  }
} // end Of Class
