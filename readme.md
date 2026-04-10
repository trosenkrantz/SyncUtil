# SyncUtil
Utility for synchronisation in Java.
It supports Java 8 and newer.

## Capabilities
- Only run a `Runnable` once despite calling it from multiple threads simultaneously
  - Dynamically decide which `Runnable` to run
  - Suspend / resume
- Drive execution of tasks
  - Both synchronous and asynchronous tasks
  - Listen to execution updates
  - Dynamically throttle max number of simultaneous tasks
  - Suspend / resume
  - Define task dependencies
  - Repeat tasks
  - Define priority of tasks
- Orchestrate events in execution windows.
  - Ensure exactly one event runs per window.
  - Automatically roll to new windows on event completion.
  - Track inactivity and trigger fallback tasks when activity markers are missed.
- OSGi support

## Getting Started
1. Choose a release, usually the newest.
2. Include the released JAR files in your project.
   - `sync-util-<version>-sources.jar` is optional and will allow your IDE to display the source code, including Javadoc.
   - SyncUtil has no runtime dependencies.
3. Include `sync-util-<version>.jar` in your build as a dependency.

## Examples

### Timeout Protection
Provide a timeout for some heavy work. [`SingleRunnable`](src/main/java/com/github/trosenkrantz/sync/util/runnable/SingleRunnable.java)  prevents race conditions:
```java
SingleRunnable runnable = new SingleRunnable(this::onDone);
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.schedule(runnable, 10, TimeUnit.SECONDS);

// Heavy work

runnable.run();
```

### Dynamic Execution Selection
Same, but dynamically decide which `Runnable` to run with [`SingleRunnableManager`](src/main/java/com/github/trosenkrantz/sync/util/runnable/SingleRunnableManager.java):
```java
SingleRunnableManager runnableManager = new SingleRunnableManager();
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.schedule(runnableManager.wrap(this::onTimeout), 10, TimeUnit.SECONDS);

// Heavy work

runnableManager.run(this::onSuccess);
```

### Managed Concurrency
Execute asynchronous requests. [`ConcurrentTaskDriver`](src/main/java/com/github/trosenkrantz/sync/util/concurrency/ConcurrentTaskDriver.java) ensures at most 8 ongoing requests at the same time.
```java
ConcurrentTaskDriver driver = new ConcurrentTaskDriver();
driver.setMaxRunningTasks(8);
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

### Synchronised Event Windows
Use [`SynchronisedScheduler`](src/main/java/com/github/trosenkrantz/sync/util/runnable/SynchronisedScheduler.java) to manage a sequence of events where only one "win" is allowed per period, with built-in inactivity fallbacks:
```Java
SynchronisedScheduler scheduler = new SynchronisedScheduler(executor);
scheduler.schedule(this::onSuccess, 10, TimeUnit.SECONDS); // Schedule a success task
scheduler.scheduleOnInactivity(this::onInactivityAlert, 30, TimeUnit.SECONDS); // Also trigger an alert if no activity is seen for 30 seconds
scheduler.markActivity(); // Marking activity resets the inactivity timer
scheduler.nextWindow(); // Transitioning manually rolls the window and cancels pending tasks
```
