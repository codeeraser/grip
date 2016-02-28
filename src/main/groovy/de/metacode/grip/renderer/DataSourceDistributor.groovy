package de.metacode.grip.renderer

import groovy.util.logging.Slf4j

import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Created by mloesch on 07.08.15.
 */

@Slf4j
trait DataSourceDistributor implements AttachmentProvider {

    def toFile(String path) {
        log.debug("writing data to path $path")
        def fos = new FileOutputStream(new File(path))
        fos.write(toDataSource().inputStream.bytes)
        fos.close()
    }

    def toRemoteFile(Map params) {
        log.debug("write data to remote destination. params: $params")
        def name = UUID.randomUUID().toString()
        File tmp = File.createTempFile(name, "dat")
        log.debug("write data into tmp file: $tmp.absolutePath")
        def fos = new FileOutputStream(tmp)
        fos.write(toDataSource().inputStream.bytes)
        fos.close()
        def ant = new AntBuilder()
        log.debug("execute scp")
        ant.scp(trust: true
                , verbose: true
                , file: "${tmp.absolutePath}"
                , todir: "${params.user}@${params.host}:${params.path}"
                , password: "${params.pwd}"
        )
        log.debug("delete tmp file")
        tmp.delete()
    }

    def sendMail(Map params, String filename) {
        log.debug("sending mail with params $params")

        validate(params)
        String smtpHost = params.smtpHost
        String from = params.from
        String to = params.to
        String cc = params.cc
        String bcc = params.bcc
        String subject = params.subject
        String text = params.text

        def properties = new Properties()
        properties["mail.smtp.host"] = smtpHost
        def session = Session.getInstance(properties, null)

        def message = new MimeMessage(session)
        message.from = new InternetAddress(from)
        message.setRecipients(Message.RecipientType.TO, to)
        if (cc) {
            message.setRecipients(Message.RecipientType.CC, cc)
        }
        if (bcc) {
            message.setRecipients(Message.RecipientType.BCC, bcc)
        }
        message.subject = subject
        message.sentDate = new Date()

        def messagePart = new MimeBodyPart()
        messagePart.setText(text, null, "html")

        //The following fixes "javax.activation.UnsupportedDataTypeException: no object DCH for MIME type multipart/mixed;"
        //Source: http://tanyamadurapperuma.blogspot.de/2014/01/struggling-with-nosuchproviderexception.html
        ClassLoader classLoader = Thread.currentThread().contextClassLoader;
        Thread.currentThread().contextClassLoader = Session.class.classLoader;
        try {
            def exlAttachment = new MimeBodyPart()
            exlAttachment.dataHandler = new DataHandler(toDataSource())
            exlAttachment.fileName = filename
            def multipart = new MimeMultipart()
            multipart.addBodyPart(messagePart)
            multipart.addBodyPart(exlAttachment)
            message.content = multipart;
            Transport.send(message);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        } finally {
            Thread.currentThread().contextClassLoader = classLoader;
        }
        log.debug("sent mail succesfully")
    }

    def validate(Map params) {
        if (!(params.smtpHost && params.from && params.to)) {
            throw new IllegalArgumentException("needs at least 'smtpHost', 'from' and 'to'")
        }
    }
}