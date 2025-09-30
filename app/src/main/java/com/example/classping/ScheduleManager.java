package com.example.classping;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleManager {
    private final ArrayList<Schedule> schedules = new ArrayList<>();

    public void add(Schedule schedule) {
        schedules.add(schedule);
        sortSchedules();
    }

    public void update(Schedule original, Schedule newSchedule) {
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).equals(original)) {
                schedules.set(i, newSchedule);
                break;
            }
        }
        sortSchedules();
    }

    public void remove(Schedule schedule) {
        schedules.remove(schedule);
    }

    public List<Schedule> getSchedulesByDay(String day) {
        ArrayList<Schedule> result = new ArrayList<>();
        for (Schedule s : schedules) {
            if (s.getDay().equalsIgnoreCase(day)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<String> getDisplayByDay(String day) {
        ArrayList<String> result = new ArrayList<>();
        for (Schedule s : getSchedulesByDay(day)) {
            result.add(s.toDisplayString());
        }
        return result;
    }

    public void parseFromText(String text) {
        schedules.clear();

        String currentDay = "";
        String currentCourse = "";
        String currentProgram = "";
        String currentRoom = "";

        String[] lines = text.split("\\n");

        Pattern dayPattern = Pattern.compile("^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)", Pattern.CASE_INSENSITIVE);
        Pattern coursePattern = Pattern.compile("[A-Z]{3,5}\\s?\\d{3,4}[A-Z]?", Pattern.CASE_INSENSITIVE);
        Pattern roomPattern = Pattern.compile("[AC]-\\d{3}");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher dayMatcher = dayPattern.matcher(line);
            if (dayMatcher.find()) {
                currentDay = dayMatcher.group(1).toUpperCase();
                continue;
            }

            Matcher courseMatcher = coursePattern.matcher(line);
            Matcher roomMatcher = roomPattern.matcher(line);

            if (courseMatcher.find()) {
                currentCourse = courseMatcher.group(0);
            } else if (roomMatcher.find()) {
                currentRoom = roomMatcher.group(0);
                if (!currentCourse.isEmpty()) {
                    schedules.add(new Schedule(
                            currentDay,
                            currentCourse,
                            currentProgram,
                            currentRoom,
                            "", // startTime
                            "", // endTime
                            "", // notes
                            "None" // reminder default
                    ));
                    currentCourse = "";
                    currentRoom = "";
                    currentProgram = "";
                }
            } else {
                currentProgram = line;
            }
        }

        sortSchedules();
    }

    private void sortSchedules() {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule s1, Schedule s2) {
                return compareTimes(s1.getStartTime(), s2.getStartTime());
            }
        });
    }

    private int compareTimes(String t1, String t2) {
        try {
            if (t1.isEmpty()) return -1;
            if (t2.isEmpty()) return 1;
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            return sdf.parse(t1).compareTo(sdf.parse(t2));
        } catch (Exception e) {
            return 0;
        }
    }
}
