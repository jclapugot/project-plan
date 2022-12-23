package com.exist.scheduler;

import java.util.Scanner;

public class Application {

    public static void main(String[] args) {
        final Scheduler scheduler = new Scheduler();
        final Scanner sc = new Scanner(System.in);

        System.out.println("Enter tasks with task name and duration, i.e. task task1 5");
        System.out.println("Enter dependencies with task name and dependency name, i.e. dependency task1 task2");
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\\s+");
            if (parts[0].equals("task")) {
                try {
                    int duration = Integer.parseInt(parts[2]);
                    if (duration > 0) {
                        scheduler.addTask(parts[1], duration);
                    } else {
                        System.err.println("Please enter number greater than 0 for duration");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Please enter a valid number for duration");
                }

            } else if (parts[0].equals("dependency")) {
                scheduler.addDependency(parts[1], parts[2]);
            }
        }

        sc.close();

        scheduler.generateSchedule();
        scheduler.printSchedule();
    }
}
