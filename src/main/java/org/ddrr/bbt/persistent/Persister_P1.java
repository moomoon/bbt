package org.ddrr.bbt.persistent;

import android.accounts.AccountManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import java.io.IOException;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.25.
 */
public class Persister_P1 extends DIPersister {
    private final static String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    final ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
            "192.168.1.78", Integer.parseInt("5222"), "csdn.shimiso.com");

    @Override
    protected String createLine(Map<String, String> entry) {
        connectionConfig.setReconnectionAllowed(true);
        connectionConfig.setSendPresence(true);

        XMPPConnection connection = new XMPPTCPConnection(connectionConfig);

        FileTransferManager manager = new FileTransferManager(connection);
        try {
            connection.connect();// 开启连接
            org.jivesoftware.smack.AccountManager am = org.jivesoftware.smack.AccountManager.getInstance(connection);
            ChatManager.getInstanceFor(connection).addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean b) {
                    chat.addMessageListener(new MessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            message.
                        }
                    });
                }
            });
            String to = connection.getRoster().getPresence("operator@domain.corp").getFrom();
            OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(to);
            transfer.se
        } catch (IOException|XMPPException |SmackException e) {
            throw new IllegalStateException(e);
        }

// 登录
        try {
            connection.login("admin", "admin");
            ChatManager cm = ChatManager.getInstanceFor(connection);
            cm.
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(connection.getUser());
        connection.getChatManager().createChat("shimiso@csdn.shimiso.com",null).sendMessage("Hello word!");


        StringBuilder sb = new StringBuilder();
        String[] date = entry.get("Date").split("\\-");
        entry.put("Date", String.format("%s-%s-%s", date[2], MONTH_NAMES[Integer.parseInt(date[1]) - 1], date[0].substring(2)));
        for (Map.Entry<?, String> e : entry.entrySet()) {
            String value = e.getValue();
            if (null != value) {
                sb.append(e.getValue());
            }
            sb.append('\t');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
