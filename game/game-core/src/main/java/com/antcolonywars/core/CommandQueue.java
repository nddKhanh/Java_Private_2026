package com.antcolonywars.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pending commands of every bot, one FIFO queue per team and subject (unit, headquarters or building).
 * Each tick runs only the head of each queue; a failed command drops the rest of its batch for that subject.
 * Not thread-safe: the owning match coordinator serializes access.
 */
public final class CommandQueue {
    private final int maxCommandsPerBot;
    private final Map<Key, Deque<Entry>> queues = new LinkedHashMap<>();
    private long nextSequence;

    public CommandQueue(int maxCommandsPerBot) {
        if (maxCommandsPerBot < 1) {
            throw new IllegalArgumentException("Queue length must be positive");
        }
        this.maxCommandsPerBot = maxCommandsPerBot;
    }

    /** Independent copy, so a tick can be discarded if persisting it fails. */
    public CommandQueue copy() {
        CommandQueue copy = new CommandQueue(maxCommandsPerBot);
        queues.forEach((key, queue) -> copy.queues.put(key, new ArrayDeque<>(queue)));
        copy.nextSequence = nextSequence;
        return copy;
    }

    /** The bot that runs a command: the production source for SPAWN, the acting unit otherwise. */
    public static String subjectOf(ActionCommand command) {
        if (command instanceof SpawnCommand spawn) {
            return spawn.sourceId() == null ? "" : spawn.sourceId();
        }
        return command.unitId();
    }

    /**
     * Appends a batch; a subject whose queue would exceed the limit has its whole share of the batch refused.
     * Returns the subjects that were refused.
     */
    public List<String> enqueue(String batchId, String teamId, List<? extends ActionCommand> commands) {
        Objects.requireNonNull(batchId, "batchId");
        Objects.requireNonNull(teamId, "teamId");
        Map<String, List<ActionCommand>> bySubject = new LinkedHashMap<>();
        for (ActionCommand command : commands) {
            bySubject.computeIfAbsent(subjectOf(command), key -> new ArrayList<>()).add(command);
        }
        List<String> refused = new ArrayList<>();
        Map<String, Integer> accepted = new HashMap<>();
        bySubject.forEach((subject, items) -> {
            int pending = size(teamId, subject);
            if (pending + items.size() > maxCommandsPerBot) {
                refused.add(subject);
            } else {
                accepted.put(subject, items.size());
            }
        });
        for (ActionCommand command : commands) {
            String subject = subjectOf(command);
            if (accepted.containsKey(subject)) {
                queues.computeIfAbsent(new Key(teamId, subject), key -> new ArrayDeque<>())
                        .addLast(new Entry(nextSequence++, batchId, teamId, subject, command));
            }
        }
        return refused;
    }

    /** Heads of all queues in the order their commands were queued. */
    public List<Entry> heads() {
        return queues.values().stream()
                .map(Deque::peekFirst)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(Entry::sequence))
                .toList();
    }

    public void pop(Entry entry) {
        Deque<Entry> queue = queues.get(entry.key());
        if (queue != null && queue.peekFirst() == entry) {
            queue.removeFirst();
            removeIfEmpty(entry.key());
        }
    }

    /** Removes the failed entry and every later command of the same batch for the same subject. */
    public int dropBatch(Entry failed) {
        Deque<Entry> queue = queues.get(failed.key());
        if (queue == null) {
            return 0;
        }
        int before = queue.size();
        queue.removeIf(entry -> entry.batchId().equals(failed.batchId()) && entry.sequence() >= failed.sequence());
        int dropped = before - queue.size();
        removeIfEmpty(failed.key());
        return dropped;
    }

    /** True while another queue still holds an earlier command of the same batch. */
    public boolean hasEarlierPending(Entry entry) {
        return queues.entrySet().stream()
                .filter(item -> !item.getKey().equals(entry.key()))
                .flatMap(item -> item.getValue().stream())
                .anyMatch(other -> other.batchId().equals(entry.batchId()) && other.sequence() < entry.sequence());
    }

    public int clear(String teamId, String subjectId) {
        Deque<Entry> removed = queues.remove(new Key(teamId, subjectId));
        return removed == null ? 0 : removed.size();
    }

    public void clearAll() {
        queues.clear();
    }

    public int size(String teamId, String subjectId) {
        Deque<Entry> queue = queues.get(new Key(teamId, subjectId));
        return queue == null ? 0 : queue.size();
    }

    public boolean isEmpty() {
        return queues.isEmpty();
    }

    /** Pending commands of one team keyed by subject, oldest first. */
    public Map<String, List<ActionCommand>> pendingFor(String teamId) {
        Map<String, List<ActionCommand>> result = new LinkedHashMap<>();
        queues.forEach((key, queue) -> {
            if (key.teamId().equals(teamId)) {
                result.put(key.subjectId(), queue.stream().map(Entry::command).toList());
            }
        });
        return result;
    }

    private void removeIfEmpty(Key key) {
        Deque<Entry> queue = queues.get(key);
        if (queue != null && queue.isEmpty()) {
            queues.remove(key);
        }
    }

    record Key(String teamId, String subjectId) {
    }

    public record Entry(long sequence, String batchId, String teamId, String subjectId, ActionCommand command) {
        Key key() {
            return new Key(teamId, subjectId);
        }
    }
}
