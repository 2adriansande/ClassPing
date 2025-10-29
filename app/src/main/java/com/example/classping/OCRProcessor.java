package com.example.classping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced OCRProcessor with improved timetable grid parsing
 */
public class OCRProcessor {

    private static final String TAG = "OCRProcessor";

    private final ScheduleManager scheduleManager;
    private final Runnable refreshCallback;
    private final Context context;

    public OCRProcessor(ScheduleManager manager, Runnable refreshCallback, Context context) {
        this.scheduleManager = manager;
        this.refreshCallback = refreshCallback;
        this.context = context;
    }

    public void processAndSaveImage(Uri imageUri, String fallbackDay) {
        if (!(context instanceof FragmentActivity)) {
            Toast.makeText(context, "Invalid context for OCR.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            if (bitmap == null) {
                Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        try {
                            String recognizedText = visionText.getText();
                            if (recognizedText.trim().isEmpty()) {
                                Toast.makeText(context, "No text found in the image.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Get existing schedules to avoid duplicates
                            Set<String> existingKeys = new HashSet<>();
                            for (Schedule s : scheduleManager.getAllSchedules()) {
                                existingKeys.add(makeScheduleKey(s));
                            }

                            // Try enhanced grid parsing first
                            List<Schedule> parsedGrid = parseGridTimetable(visionText, bitmap.getWidth(), bitmap.getHeight(), existingKeys);

                            // Fallback to line-by-line if grid parsing yields few results
                            if (parsedGrid.size() < 3) {
                                List<Schedule> parsedLines = parseScheduleTextLineByLine(recognizedText, fallbackDay, existingKeys);
                                if (parsedLines.size() > parsedGrid.size()) {
                                    parsedGrid = parsedLines;
                                }
                            }

                            if (parsedGrid.isEmpty()) {
                                Toast.makeText(context, "Could not parse schedule from image.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Add new schedules locally
                            int added = 0;
                            for (Schedule s : parsedGrid) {
                                scheduleManager.addSchedule(s);
                                added++;
                            }

                            // ✅ Auto-sync new schedules to Firestore
                            if (added > 0) {
                                Toast.makeText(context, "Saving schedules to Firestore...", Toast.LENGTH_SHORT).show();
                                scheduleManager.syncToFirebase();
                            }

                            refreshCallback.run();
                            Toast.makeText(context, added + " new schedule(s) added and synced.", Toast.LENGTH_LONG).show();

                        } catch (Exception e) {
                            Log.e(TAG, "Parsing error", e);
                            Toast.makeText(context, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "OCR failed", e);
                        Toast.makeText(context, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });


        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(context, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Enhanced grid timetable parser
     * Detects header row, time column, and day columns with better spatial accuracy
     */
    private List<Schedule> parseGridTimetable(Text visionText, int imageWidth, int imageHeight, Set<String> existingKeys) {
        List<Schedule> results = new ArrayList<>();

        List<TextBlock> blocks = visionText.getTextBlocks();
        if (blocks == null || blocks.isEmpty()) return results;

        // Step 1: Find header row with day names
        Map<String, Integer> dayColumnCenters = findDayColumns(blocks, imageWidth);
        if (dayColumnCenters.isEmpty()) {
            Log.w(TAG, "No day columns detected in header");
            return results;
        }

        // Step 2: Find time slots in leftmost column
        List<TimeSlot> timeSlots = findTimeSlots(blocks);
        if (timeSlots.isEmpty()) {
            Log.w(TAG, "No time slots detected");
            return results;
        }

        // Step 3: Parse course blocks by matching to day columns and time rows
        Pattern coursePattern = Pattern.compile("([A-Z]{3,5}\\s*\\d{3,4}[A-Z]?)", Pattern.CASE_INSENSITIVE);
        Pattern sectionPattern = Pattern.compile("^([A-Z]{2,4}\\d{1,2}[A-Z]\\d?)$"); // Matches CS31S1, IT41S1, etc.
        Pattern roomPattern = Pattern.compile("([A-Z]-?\\d{3,4})");

        // Group blocks by spatial proximity to merge course + section + room
        List<BlockGroup> blockGroups = groupNearbyBlocks(blocks, timeSlots, dayColumnCenters);

        for (BlockGroup group : blockGroups) {
            String day = group.day;
            TimeSlot timeSlot = group.timeSlot;

            if (day == null || timeSlot == null) continue;

            String subject = "";
            String section = "";
            String room = "TBA";

            // Collect all text lines from the group
            List<String> allLines = new ArrayList<>();
            for (TextBlock block : group.blocks) {
                for (Line line : block.getLines()) {
                    String text = line.getText().trim();
                    if (!text.isEmpty() && !text.matches("(?i)^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Period|Time|Day|Room)$")) {
                        allLines.add(text);
                    }
                }
            }

            if (allLines.isEmpty()) continue;

            // Extract course code, section, and room
            for (String line : allLines) {
                // Check for course code
                if (subject.isEmpty()) {
                    Matcher courseMatcher = coursePattern.matcher(line);
                    if (courseMatcher.find()) {
                        subject = courseMatcher.group(1).replaceAll("\\s+", " ").trim();
                        continue;
                    }
                }

                // Check for section code
                if (section.isEmpty()) {
                    Matcher sectionMatcher = sectionPattern.matcher(line);
                    if (sectionMatcher.find()) {
                        section = sectionMatcher.group(1);
                        continue;
                    }
                }

                // Check for room
                Matcher roomMatcher = roomPattern.matcher(line);
                if (roomMatcher.find()) {
                    room = roomMatcher.group(1);
                }
            }

            // Skip if no valid subject found
            if (subject.isEmpty() || subject.length() < 3) continue;

            // Skip if subject is just a room code
            if (subject.matches("^[A-Z]-?\\d{3,4}$")) continue;

            // Skip time-only entries
            if (subject.matches(".*\\d{2}:\\d{2}.*")) continue;

            // Skip day names
            if (subject.matches("(?i)^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)$")) continue;

            // Skip header text
            if (subject.matches("(?i)^(Period|Time|Day|Room)$")) continue;

            // Skip if subject is actually a section code
            if (subject.matches("^[A-Z]{2,4}\\d{1,2}[A-Z]\\d?$")) continue;

            // Create schedule entry only if not a duplicate
            Schedule sch = new Schedule(
                    day,
                    subject,
                    section,
                    room,
                    timeSlot.startTime,
                    timeSlot.endTime,
                    "",
                    "None"
            );

            String key = makeScheduleKey(sch);
            if (!existingKeys.contains(key)) {
                results.add(sch);
                existingKeys.add(key);
            }
        }

        return results;
    }

    /**
     * Group nearby blocks that belong to the same schedule entry
     */
    private static class BlockGroup {
        List<TextBlock> blocks = new ArrayList<>();
        String day;
        TimeSlot timeSlot;
    }

    private List<BlockGroup> groupNearbyBlocks(List<TextBlock> blocks, List<TimeSlot> timeSlots, Map<String, Integer> dayColumnCenters) {
        List<BlockGroup> groups = new ArrayList<>();
        Set<TextBlock> processed = new HashSet<>();

        for (TextBlock block : blocks) {
            if (processed.contains(block)) continue;

            Rect bRect = block.getBoundingBox();
            if (bRect == null) continue;

            int blockCenterX = (bRect.left + bRect.right) / 2;
            int blockCenterY = (bRect.top + bRect.bottom) / 2;

            // Determine day and time slot
            String day = findClosestDay(blockCenterX, dayColumnCenters);
            if (day == null) continue;

            TimeSlot timeSlot = findOverlappingTimeSlot(blockCenterY, bRect.top, bRect.bottom, timeSlots);
            if (timeSlot == null) continue;

            // Create a new group
            BlockGroup group = new BlockGroup();
            group.day = day;
            group.timeSlot = timeSlot;
            group.blocks.add(block);
            processed.add(block);

            // Find nearby blocks in the same cell (within 100px vertically, 80px horizontally)
            for (TextBlock otherBlock : blocks) {
                if (processed.contains(otherBlock)) continue;

                Rect oRect = otherBlock.getBoundingBox();
                if (oRect == null) continue;

                int otherCenterX = (oRect.left + oRect.right) / 2;
                int otherCenterY = (oRect.top + oRect.bottom) / 2;

                // Check if in same cell region
                if (Math.abs(otherCenterX - blockCenterX) < 80 &&
                        Math.abs(otherCenterY - blockCenterY) < 100) {

                    String otherDay = findClosestDay(otherCenterX, dayColumnCenters);
                    if (day.equals(otherDay)) {
                        group.blocks.add(otherBlock);
                        processed.add(otherBlock);
                    }
                }
            }

            groups.add(group);
        }

        return groups;
    }

    /**
     * Find day column positions from header row
     */
    private Map<String, Integer> findDayColumns(List<TextBlock> blocks, int imageWidth) {
        Map<String, Integer> dayColumns = new HashMap<>();
        Pattern dayPattern = Pattern.compile("(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)", Pattern.CASE_INSENSITIVE);

        int minHeaderY = Integer.MAX_VALUE;
        List<TextBlock> headerBlocks = new ArrayList<>();

        // Find top row blocks (headers)
        for (TextBlock block : blocks) {
            Rect rect = block.getBoundingBox();
            if (rect != null && rect.top < imageWidth * 0.1f) {
                minHeaderY = Math.min(minHeaderY, rect.top);
                headerBlocks.add(block);
            }
        }

        // Extract day names and their X positions
        for (TextBlock block : headerBlocks) {
            Rect rect = block.getBoundingBox();
            if (rect == null) continue;

            String text = block.getText();
            Matcher matcher = dayPattern.matcher(text);
            if (matcher.find()) {
                String day = matcher.group(1);
                int centerX = (rect.left + rect.right) / 2;
                dayColumns.put(day, centerX);
                Log.d(TAG, "Found day: " + day + " at X=" + centerX);
            }
        }

        return dayColumns;
    }

    /**
     * Find closest day for a given X coordinate
     */
    private String findClosestDay(int x, Map<String, Integer> dayColumns) {
        String closestDay = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : dayColumns.entrySet()) {
            int distance = Math.abs(x - entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                closestDay = entry.getKey();
            }
        }

        return minDistance < 200 ? closestDay : null; // Threshold for column matching
    }

    private static class TimeSlot {
        String startTime;
        String endTime;
        int centerY;
        int topY;
        int bottomY;

        TimeSlot(String start, String end, int cy, int top, int bottom) {
            startTime = start;
            endTime = end;
            centerY = cy;
            topY = top;
            bottomY = bottom;
        }
    }

    /**
     * Find time slots from leftmost column
     */
    private List<TimeSlot> findTimeSlots(List<TextBlock> blocks) {
        List<TimeSlot> slots = new ArrayList<>();
        Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*(AM|PM|am|pm)?\\s*[-–]\\s*(\\d{1,2}:\\d{2})\\s*(AM|PM|am|pm)?");

        int maxLeftBound = 0;
        for (TextBlock block : blocks) {
            Rect rect = block.getBoundingBox();
            if (rect != null) {
                String text = block.getText();
                if (text.contains("AM") || text.contains("PM") || text.matches(".*\\d{2}:\\d{2}.*")) {
                    maxLeftBound = Math.max(maxLeftBound, rect.right);
                }
            }
        }

        for (TextBlock block : blocks) {
            Rect rect = block.getBoundingBox();
            if (rect == null) continue;

            // Only process leftmost column
            if (rect.left > maxLeftBound * 0.8) continue;

            String text = block.getText();
            Matcher matcher = timePattern.matcher(text);

            if (matcher.find()) {
                String start = normalizeTimeToken(matcher.group(1) + " " +
                        (matcher.group(2) != null ? matcher.group(2) : ""));
                String end = normalizeTimeToken(matcher.group(3) + " " +
                        (matcher.group(4) != null ? matcher.group(4) : ""));

                int cy = (rect.top + rect.bottom) / 2;
                slots.add(new TimeSlot(start, end, cy, rect.top, rect.bottom));
                Log.d(TAG, "Found time slot: " + start + " - " + end + " at Y=" + cy);
            }
        }

        // Sort by vertical position
        Collections.sort(slots, new Comparator<TimeSlot>() {
            @Override
            public int compare(TimeSlot a, TimeSlot b) {
                return Integer.compare(a.centerY, b.centerY);
            }
        });

        return slots;
    }

    /**
     * Find which time slot a block overlaps with
     */
    private TimeSlot findOverlappingTimeSlot(int centerY, int topY, int bottomY, List<TimeSlot> slots) {
        // Find slot that best overlaps with this block vertically
        for (TimeSlot slot : slots) {
            // Check if block center is within reasonable range of slot
            int distance = Math.abs(centerY - slot.centerY);
            if (distance < 100) { // Within 100 pixels
                return slot;
            }

            // Or if block overlaps slot region
            if (bottomY > slot.topY && topY < slot.bottomY) {
                return slot;
            }
        }

        // Find closest slot if no direct overlap
        TimeSlot closest = null;
        int minDist = Integer.MAX_VALUE;
        for (TimeSlot slot : slots) {
            int dist = Math.abs(centerY - slot.centerY);
            if (dist < minDist) {
                minDist = dist;
                closest = slot;
            }
        }

        return minDist < 150 ? closest : null;
    }

    /**
     * Fallback: Line-by-line parsing for non-grid formats
     */
    private List<Schedule> parseScheduleTextLineByLine(String text, String fallbackDay, Set<String> existingKeys) {
        List<Schedule> results = new ArrayList<>();

        String currentDay = fallbackDay;
        String[] lines = text.split("\\n");

        Pattern dayPattern = Pattern.compile("^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)", Pattern.CASE_INSENSITIVE);
        Pattern coursePattern = Pattern.compile("([A-Z]{3,5}\\s?\\d{3,4}[A-Z]?)", Pattern.CASE_INSENSITIVE);
        Pattern roomPattern = Pattern.compile("([A-Z]-?\\d{3,4})");
        Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2})(?:\\s*(AM|PM|am|pm))?\\s*[-–to]{1,3}\\s*(\\d{1,2}:\\d{2})(?:\\s*(AM|PM|am|pm))?");

        String currentCourse = "";
        String currentSection = "";
        String currentRoom = "";
        String currentStartTime = "";
        String currentEndTime = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher dayMatcher = dayPattern.matcher(line);
            if (dayMatcher.find()) {
                currentDay = dayMatcher.group(1).toUpperCase();
                continue;
            }

            Matcher courseMatcher = coursePattern.matcher(line);
            if (courseMatcher.find()) {
                if (!currentCourse.isEmpty() && (!currentRoom.isEmpty() || !currentStartTime.isEmpty())) {
                    addScheduleEntry(results, currentDay, currentCourse, currentSection,
                            currentRoom, currentStartTime, currentEndTime, existingKeys);
                    currentCourse = "";
                    currentSection = "";
                    currentRoom = "";
                    currentStartTime = "";
                    currentEndTime = "";
                }

                String courseMatch = courseMatcher.group(1).trim();
                if (courseMatch.contains("-")) {
                    String[] parts = courseMatch.split("-", 2);
                    currentCourse = parts[0].trim();
                    currentSection = parts[1].trim();
                } else {
                    currentCourse = courseMatch;
                    currentSection = "";
                }
                continue;
            }

            Matcher roomMatcher = roomPattern.matcher(line);
            if (roomMatcher.find()) {
                currentRoom = roomMatcher.group(1);

                Matcher timeMatcher = timePattern.matcher(line);
                if (timeMatcher.find()) {
                    currentStartTime = normalizeTimeToken(timeMatcher.group(1) + " " +
                            (timeMatcher.group(2) != null ? timeMatcher.group(2) : ""));
                    currentEndTime = normalizeTimeToken(timeMatcher.group(3) + " " +
                            (timeMatcher.group(4) != null ? timeMatcher.group(4) : ""));
                }

                if (!currentCourse.isEmpty()) {
                    addScheduleEntry(results, currentDay, currentCourse, currentSection,
                            currentRoom, currentStartTime, currentEndTime, existingKeys);
                    currentCourse = "";
                    currentSection = "";
                    currentRoom = "";
                    currentStartTime = "";
                    currentEndTime = "";
                }
                continue;
            }

            Matcher timeMatcher = timePattern.matcher(line);
            if (timeMatcher.find()) {
                currentStartTime = normalizeTimeToken(timeMatcher.group(1) + " " +
                        (timeMatcher.group(2) != null ? timeMatcher.group(2) : ""));
                currentEndTime = normalizeTimeToken(timeMatcher.group(3) + " " +
                        (timeMatcher.group(4) != null ? timeMatcher.group(4) : ""));

                if (!currentCourse.isEmpty()) {
                    addScheduleEntry(results, currentDay, currentCourse, currentSection,
                            currentRoom, currentStartTime, currentEndTime, existingKeys);
                    currentCourse = "";
                    currentSection = "";
                    currentRoom = "";
                    currentStartTime = "";
                    currentEndTime = "";
                }
                continue;
            }

            if (!line.matches(".*\\d+.*") && currentCourse.isEmpty()) {
                currentSection = line;
            }
        }

        if (!currentCourse.isEmpty()) {
            addScheduleEntry(results, currentDay, currentCourse, currentSection,
                    currentRoom, currentStartTime, currentEndTime, existingKeys);
        }

        return results;
    }

    private void addScheduleEntry(List<Schedule> results, String day, String course,
                                  String section, String room, String startTime, String endTime, Set<String> existingKeys) {
        // Only add if we have a valid course code (at least 3 characters)
        if (course.isEmpty() || course.length() < 3) return;

        // Skip if course is just a room code
        if (course.matches("^[A-Z]-?\\d{3,4}$")) return;

        // Skip time-only entries
        if (course.matches(".*\\d{2}:\\d{2}.*")) return;

        // Skip day names
        if (course.matches("(?i)^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)$")) return;

        // Skip header text
        if (course.matches("(?i)^(Period|Time|Day|Room)$")) return;

        String finalRoom = room.isEmpty() ? "TBA" : room;
        String normalizedStart = normalizeTimeForStorage(startTime);
        String normalizedEnd = normalizeTimeForStorage(endTime);

        Schedule sch = new Schedule(day, course, section, finalRoom, normalizedStart, normalizedEnd, "", "None");

        String key = makeScheduleKey(sch);
        if (!existingKeys.contains(key)) {
            results.add(sch);
            existingKeys.add(key);
        }
    }

    private String makeScheduleKey(Schedule s) {
        return (s.getDay() + "|" + s.getSubject() + "|" +
                s.getStartTime() + "|" + s.getEndTime()).toLowerCase(Locale.ROOT);
    }

    private String normalizeTimeToken(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String r = raw.replace(".", ":").trim();
        Pattern p = Pattern.compile("(\\d{1,2}):(\\d{2})(?:\\s*(AM|PM|am|pm))?");
        Matcher m = p.matcher(r);
        if (m.find()) {
            int hh = Integer.parseInt(m.group(1));
            int mm = Integer.parseInt(m.group(2));
            String ampm = m.group(3);
            if (ampm != null && !ampm.isEmpty()) {
                String low = ampm.toLowerCase(Locale.ROOT);
                if (low.equals("pm") && hh < 12) hh += 12;
                if (low.equals("am") && hh == 12) hh = 0;
            }
            return String.format(Locale.getDefault(), "%02d:%02d", hh, mm);
        }
        return "";
    }

    private String normalizeTimeForStorage(String raw) {
        return normalizeTimeToken(raw);
    }
}