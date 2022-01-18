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

## Reins

Reins are Solar's solution to the [distributed identity problem](https://github.com/Moderocky/Cobweb/blob/master/Identity-Relations-Distributed-Computing.pdf).

One server may hold the reins to an object at a time. Having an object's reins means that server is responsible for its field-values, garbage collection and distribution. In most cases, this should be the server that created the object.

However, there may be occasions where an object ought to be 'moved' according to the server that is accessing it.
An example of this might be when an object is purely functional and needs to perform some action on the virtual machine it exists in (e.g. interacting with files or some local data.)
In this case, the object should use a 'fluid' handle. Another virtual machine may request the reins to the object, at which point access will throttle until the object is fully transferred to the new host.

## Handles

Exporting or retrieving an object gives a 'handle'. These handles hold a volatile reference to the object.
For regular (local/remote) handles, this reference will be constant.

Local handles hold the original object. Remote handles hold a mimicked copy of the object that redirects method calls to the original version.
Fields will be empty on the remote copy.

The local handle always has the reins for an object.

Fluid handles are arbitrarily remote or local, and this is only known by checking whether the current VM has the object's reins.
