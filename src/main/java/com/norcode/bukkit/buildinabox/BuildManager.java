package com.norcode.bukkit.buildinabox;

import com.norcode.bukkit.schematica.Clipboard;
import org.bukkit.util.BlockVector;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages all building jobs on the entire server.
 *
 */
public class BuildManager implements Runnable {
    BuildInABox plugin;
    private int maxBlocksPerTick;
    private LinkedList<BuildTask> buildTasks = new LinkedList<BuildTask>();

    public BuildManager(BuildInABox plugin, int maxBlocksPerTick) {
        this.maxBlocksPerTick = maxBlocksPerTick;
    }

    public void finishSafely() {
        for (BuildTask task: buildTasks) {
            while (task.getRemainingBlocks() > 0) {
                task.tick();
            }
            task.complete();
        }
    }

    @Override
    public void run() {
        if (buildTasks.isEmpty()) {
            return;
        }
        BuildTask task;
        boolean oneTicked = false;
        for (int i=0;i<maxBlocksPerTick;i++) {
            task = buildTasks.pop();
            if (task == null) break;
            if (!task.tick()) {
                i--;
            } else {
                task.callsThisTick++;
            }
            if (task.getRemainingBlocks() > 0) {
                buildTasks.add(task);
            } else {
                task.complete();
            }
        }
        for (int i=0;i<buildTasks.size();i++) {
            buildTasks.get(i).callsThisTick = 0;
        }
    }

    public void scheduleTask(BuildTask task) {
        this.buildTasks.add(task);
    }

    public static abstract class BuildTask {

        protected Clipboard clipboard;
        protected int blocksPerTick;
        protected int callsThisTick = 0;
        protected List<BlockVector> vectorQueue;
        protected int pointer = 0;

        public BuildTask(Clipboard clipboard, List<BlockVector> vectorQueue, int blocksPerTick) {
            this.clipboard = clipboard;
            this.vectorQueue = vectorQueue;
            this.blocksPerTick = blocksPerTick;
        }

        protected void complete() { onComplete(); }

        protected int getRemainingBlocks() {
            return vectorQueue.size() - pointer;
        }

        protected boolean tick() {
            if (callsThisTick >= blocksPerTick) return false;
            processBlock(vectorQueue.get(pointer));
            pointer++;
            return true;
        }

        public abstract void processBlock(BlockVector clipboardPoint);
        public abstract void onComplete();
    }
}
