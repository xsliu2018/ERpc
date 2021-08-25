# ERpc
A light RPC framework based on Netty, ZooKeeper and Spring
### Features:
* Simple code and framework
* Service registry/discovery support by ZooKeeper
* High availability, load balance and failover
* Support different load balance strategy,random, consistentHash,round
* Support asynchronous/synchronous call
* Support different versions of service
* Support different serializer/deserializer
* Dead TCP connection detecting with heartbeat
### How to use (e-rpc-test)
1. Define an interface:
    ```  
    public interface HelloService { 
        String hello(String name); 
        String hello(Person person);
    }
    ```  
2. Implement the interface with annotation @ServiceProvider:
    ```  
    @ServiceProvider(HelloService.class, version = "1.0")
    public class HelloServiceImpl implements HelloService {
        public HelloServiceImpl(){}
	
        @Override
        public String hello(String name) {
            return "Hello " + name;
        }

        @Override
        public String hello(Person person) {
            return "Hello " + person.getFirstName() + " " + person.getLastName();
        }
    }
    ```  
3. Run zookeeper

   For example: zookeeper is running on 127.0.0.1:2181

4. Start server:
   Start server with spring config: RpcServerBootstrap

5. Call the service:
    1. Use the client:
    ```  
   	final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
   		
   	// Sync call
   	HelloService helloService = rpcClient.createService(HelloService.class, "1.0");
   	String result = helloService.hello("World");
   		
   	// Async call
   	RpcService client = rpcClient.createAsyncService(HelloService.class, "2.0");
   	RPCFuture helloFuture = client.call("hello", "World");
   	String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
	``` 
    2. Use annotation @ServiceConsumer:
    ``` 
    public class Baz implements Foo {
        @ServiceConsumer(version = "1.0")
        private HelloService helloService1;
           
        @ServiceConsumer(version = "2.0")
        private HelloService helloService2;
           
        @Override
        public String say(String s) {
            return helloService1.hello(s);
        }
    }
    ``` 