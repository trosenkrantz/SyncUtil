# SyncUtil

Utility for synchronisation in Java.
It is built for Java 8 or newer.

[![trosenkrantz](https://circleci.com/gh/trosenkrantz/SyncUtil.svg?style=shield)](https://circleci.com/gh/trosenkrantz/SyncUtil)

- Only run a `Runnable` once despite calling it from multiple threads simultaneously
  - Dynamically decide which `Runnable` to run
  - Suspend / resume
- Drive execution of tasks
  - Both synchronous and asynchronous tasks
  - Listen to execution updates
  - Dynamically throttle max number of simultaneous tasks
  - Suspend / resume
- OSGi support

## How to Use
1. Choose a release, usually the newest.
2. Include the released JAR files in your project.
   - `sync-util-<version>-sources.jar` is optional and will allow your IDE to display the source code, including JavaDoc.
   - SyncUtil has no runtime dependencies.
3. Include `sync-util-<version>.jar` in your build as a dependency.

## Examples
Provide a timeout for some heavy work. [`SingleRunnable`](src/main/java/com/github/trosenkrantz/sync/util/runnable/SingleRunnable.java)  prevents race conditions:
```java
SingleRunnable runnable = new SingleRunnable(this::onDone);
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.schedule(runnable, 10, TimeUnit.SECONDS);

// Heavy work

runnable.run();
```

Same, but dynamically decide which `Runnable` to run with [`SingleRunnableManager`](src/main/java/com/github/trosenkrantz/sync/util/runnable/SingleRunnableManager.java):
```java
SingleRunnableManager runnableManager = new SingleRunnableManager();
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.schedule(runnableManager.wrap(this::onTimeout), 10, TimeUnit.SECONDS);

// Heavy work

runnableManager.run(this::onSuccess);
```

Execute asynchronous requests. [`ConcurrentTaskDriver`](src/main/java/com/github/trosenkrantz/sync/util/concurrency/ConcurrentTaskDriver.java) ensures at most 8 ongoing requests at the same time.
```java
ConcurrentTaskDriver driver = new ConcurrentTaskDriver(8);
requests.forEach(request -> driver.queue(onDone -> {
    request.execute(new ResponseHandler() {
        @Override
        public void onSuccess() {
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
