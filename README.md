# SyncUtil

Utility for synchronisation in Java.

[![trosenkrantz](https://circleci.com/gh/trosenkrantz/SyncUtil.svg?style=shield)](https://circleci.com/gh/trosenkrantz/SyncUtil)

## Examples
Provide a timeout for some heavy work. `SingleRunnableManager` prevents race conditions.
```java
SingleRunnableManager runnableManager = new SingleRunnableManager();
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.schedule(runnableManager.wrap(this::onTimeout), 10, TimeUnit.SECONDS);

// Do heavy work

runnableManager.run(this::onComplete);
```

Execute asynchronous requests. `ConcurrentTaskDriver` ensures at most 8 ongoing requests at the same time.
```java
ConcurrentTaskDriver driver = new ConcurrentTaskDriver(8);
requests.forEach(request -> driver.queue(onDone -> {
    request.execute(new ResponseHandler() {
        @Override
        public void onCompleted() {
            onDone.run();
        }

        @Override
        public void onError(Error error) {
            // Log error

            onDone.run();
        }
    });
}));
```
