package com.exist.scheduler;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {

    private final Map<String, Task> projectPlan = new HashMap<>();
    private final Set<String> cyclicDependencyChecker = new HashSet<>();

    public void generateSchedule() {
        projectPlan.values().stream()
                .peek(task -> {
                    task.setStartDate(LocalDate.now());
                    task.setEndDate(task.getStartDate().plusDays(task.getDuration()));
                })
                .collect(Collectors.toList())
                .forEach(task -> task.getDependencies().stream()
                        .filter(next -> task.getStartDate().isBefore(next.getEndDate()))
                        .max(Comparator.comparing(Task::getEndDate))
                        .ifPresent(next -> {
                            task.setStartDate(next.getEndDate());
                            task.setEndDate(task.getStartDate().plusDays(task.getDuration()));
                        }));
    }

    public void printSchedule() {
        System.out.println("====================== Generated Schedule ======================");
        projectPlan.values()
                .stream().sorted(Comparator.comparing(Task::getStartDate).thenComparing(Task::getName))
                .forEach(task -> System.out.println(task.getName() + ": " + task.getStartDate() + " - " + task.getEndDate()));
    }

    public void addTask(String name, int duration) {
        if (projectPlan.containsKey(name)) {
            showTaskAlreadyExistError(name);
        } else {
            projectPlan.put(name, new Task(name, duration));
        }
    }

    public void addDependency(String name, String dependency) {
        if (!projectPlan.containsKey(name)) {
            showNoTaskError(name);
            return;
        }

        if (!projectPlan.containsKey(dependency)) {
            showNoTaskError(dependency);
            return;
        }

        var task = projectPlan.get(name);
        var dependencyTask = projectPlan.get(dependency);
        if (hasCyclicDependency(task, dependencyTask)) {
            showCyclicDependencyError(name, dependency);
            cyclicDependencyChecker.clear();
        } else if (task.getDependencies().contains(dependencyTask)) {
            showDuplicateDependencyError(name, dependency);
        } else {
            task.getDependencies().add(dependencyTask);
        }
    }

    private boolean hasCyclicDependency(Task task, Task dependencyTask) {
        final Queue<Task> queue = new LinkedList<>(dependencyTask.getDependencies());

        while (!queue.isEmpty()) {
            Task currentDependency = queue.poll();
            if (currentDependency.getName().equals(task.getName())) {
                return true;
            }

            if (cyclicDependencyChecker.add(task.getName())) {
                queue.addAll(currentDependency.getDependencies());
            }
        }
        return false;
    }

    private void showTaskAlreadyExistError(String name) {
        System.err.println("Task named " + name + " already exists");
    }

    private void showNoTaskError(String name) {
        System.err.println("No task named " + name);
    }

    private void showDuplicateDependencyError(String name, String dependency) {
        System.err.println(dependency + " is already a dependency of " + name);
    }

    private void showCyclicDependencyError(String name, String dependency) {
        System.err.println("Making " + dependency + " as a dependency of " + name + " will cause a cyclic dependency");
    }
}
