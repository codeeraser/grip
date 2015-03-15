package de.metacode.grip.util

import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Created by mloesch on 15.03.15.
 */

class Mail {

    static def send(params, AttachmentProvider attachmentProvider, String filename) {
        String smtpHost = params.smtpHost
        String from = params.from
        String to = params.to
        String subject = params.subject
        String text = params.text

        def properties = new Properties()
        properties.put("mail.smtp.host", smtpHost)
        def session = Session.getDefaultInstance(properties, null)

        def message = new MimeMessage(session)
        message.from = new InternetAddress(from)
        message.setRecipients(Message.RecipientType.TO, to)
        message.subject = subject
        message.sentDate = new Date()

        def messagePart = new MimeBodyPart()
        messagePart.setText(text, null, "html")

        def exlAttachment = new MimeBodyPart()
        exlAttachment.dataHandler = new DataHandler(attachmentProvider.toDataSource())
        exlAttachment.fileName = filename
        def multipart = new MimeMultipart()
        multipart.addBodyPart(messagePart)
        multipart.addBodyPart(exlAttachment)

        message.content = multipart
        Transport.send(message)
    }
}
