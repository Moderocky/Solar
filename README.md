Solar
=====

### Opus #16

A replacement candidate for Java's Remote Method Interface.
Solar is designed to allow [seamless distributed computing](https://github.com/Moderocky/Cobweb/blob/master/Identity-Relations-Distributed-Computing.pdf), where a program can call and interact with code on a completely separate machine without any meaningful indication that it is (in fact) distributed.

Solar has several improvements on Java's RMI:
1. Entire objects can be exported, rather than just interfaces.
2. Transferred objects do not need to be explicitly serializable.
3. Single socket-pipes are re-used for minimal resource toll.
4. Remote servers do not need all the classes present.
5. The entire system is easy to modify and extend.

## Maven Information
```xml
<repository>
    <id>kenzie</id>
    <name>Kenzie's Repository</name>
    <url>https://repo.kenzie.mx/releases</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>solar</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Examples

Starting a local server.
```java 
final Server local = Server.create(port); // creates an accessible server on this port
```

Making an object available from the local server.
```java 
final Code code = new Code("thing"); // generates a 64-bit unique identifier code
final MyCoolThing thing = new MyCoolThing(); // our object
local.export(thing, code); // exports this object, it can now be retrieved with the code
```

Connecting to a remote (or local) server.
```java 
final Server server = Server.connect(address, port); // connect to the remote server
```

Requesting an object from a remote server.
```java 
final Handle<MyCoolThing> handle = server.request(code); // gets a "handle" that can control this object
final MyCoolThing remote = handle.reference(); // gets the usable object reference
```

```java 
final MyCoolThing remote = server.<MyCoolThing>request(code).reference(); // inline version
```

Using the retrieved object.
```java 
final MyCoolThing remote = server.<MyCoolThing>request(code).reference();
assert remote != null;
assert remote.toString().equals("hello from the other side"); // tostring
assert remote.myNumber() == 4; // some method from the object
```

## Queries

The contents of a server can be 'queried' to find matching handles. While this would typically require downloading the entire contents of the server and searching it locally, Solar has support for searching on the remote machine using a supported query.

```java 
final Query query = handle -> handle.reference() instanceof MyThing thing && thing.isAlive();
final Handle<?>[] handles = server.query(query);
```

Queries can be written in long form as concrete classes instead of lambdas. This may prevent class transmission errors if the remote machine is struggling to define the query due to an access privilege error.

```java
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.Query;

public class MyQuery implements Query {
    
    @Override
    public boolean matches(Handle<?> handle) {
        return handle.reference() instanceof Box;
    }
}
```

A query object can be passed to a remote machine. If the query class is available on the remote for reconstruction, it will be marshalled as normal.
However, the query is likely to be an anonymous lambda function or method reference, which would not be present on the remote.

To solve this problem, Solar will transmit the class bytecode (if it is available) to be defined on the remote. It will then be available for future queries.
This remote class will be destroyed once the remote server closes.

**N.B.** Bytecode transmission is impossible for anonymous or runtime-created classes without available binary source in their protection domain.
Transmission may also be unavailable in some VM distributions with irregular class-loaders or no source (e.g. IntelliJ direct test/run environment.)

### Safety Notice

Clearly, transmitting bytecode is open to malicious code-injection. Safety measures should be taken (such as using a security key for servers, keeping access IP-restricted, etc.)
This is good practice for any remotely-accessible server.

## Reins

Reins are Solar's solution to the [distributed identity problem](https://github.com/Moderocky/Cobweb/blob/master/Identity-Relations-Distributed-Computing.pdf).

One server may hold the reins to an object at a time. Having an object's reins means that server is responsible for its field-values, garbage collection and distribution. In most cases, this should be the server that created the object.

However, there may be occasions where an object ought to be 'moved' according to the server that is accessing it.
An example of this might be when an object is purely functional and needs to perform some action on the virtual machine it exists in (e.g. interacting with files or some local data.)
In this case, the object should use a 'fluid' handle. Another virtual machine may request the reins to the object, at which point access will throttle until the object is fully transferred to the new host.

Reins must be acquired by a local server, which will integrate that server into the object ownership network.

Reins acquisition is a _cascade_ action: the call will follow up the network to the object root, and reverse the direction of the ownership chain back down to the new owner.

**N.B.** Acquiring the reins in a complex multi-orbit network will introduce a socket deadlock until the reins are resolved.

## Stubs

To avoid the inevitable problem of references becoming invalid due to a handle moving, programs may keep an object 'stub' instead of the direct reference or caching the handle.
The stub is designed to be a safe means of access.

The stub is guaranteed to be constant, and is valid for as long as the handle itself is valid (i.e. the `stub()` method will return the same stub even once the volatile object is killed and garbage-collected.)
The stub is never the reference itself, even for constant handle varieties.

Access via the stub is necessarily slower (since calls are locked via synchronization) but this may be by a very minor factor, since it uses a forwarding mimic.

The stub does not hold a strong reference to the reference-object itself, so it can be safely held without interfering with garbage-collection or handle destruction.

## Handles

Exporting or retrieving an object gives a 'handle'. These handles hold a volatile reference to the object.
For regular (local/remote) handles, this reference will be constant until destruction.

Local handles hold the original object. Remote handles hold a mimicked copy of the object that redirects method calls to the original version.
Fields will be empty on the remote copy.

The local handle always has the reins for an object.

Fluid handles are arbitrarily remote or local, and this is only known by checking whether the current VM has the object's reins.
