package com.github.trosenkrantz.sync.util.concurrency;

import java.util.ArrayList;
import java.util.List;

public class AsynchronousTaskTestHelper {
    private final AsynchronousTask asynchronousTask;
    private final List<Runnable> onDoneList;

    public AsynchronousTaskTestHelper() {
        onDoneList = new ArrayList<>();
        asynchronousTask = onDoneList::add;
    }

    public AsynchronousTask getTask() {
        return asynchronousTask;
    }

    public void finishTask() {
        onDoneList.remove(0).run();
    }
}
