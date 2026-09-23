package com.roombooker.recurrence;

import com.roombooker.exception.InvalidMeetingTimeException;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;

@Component
public class TimezoneResolver {

    public ZoneId parseZoneId(String timezoneStr) {
        try {
            return ZoneId.of(timezoneStr);
        } catch (Exception ex) {
            throw new InvalidMeetingTimeException("Invalid timezone: '" + timezoneStr + "'. Use valid IANA timezone name like 'Asia/Kolkata' or 'America/New_York'.");
        }
    }

    public Instant toInstant(LocalDate date, LocalTime time, ZoneId zoneId) {
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        ZoneRules rules = zoneId.getRules();
        List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);

        if (validOffsets.isEmpty()) {
            // DST Gap (Spring Forward - time does not exist in local clock)
            ZoneOffsetTransition transition = rules.getTransition(localDateTime);
            String message = String.format(
                "Requested local time %s on %s does not exist in timezone %s due to DST gap transition (%s).",
                time, date, zoneId, transition != null ? transition.toString() : "Spring Forward"
            );
            throw new InvalidMeetingTimeException(message);
        }

        // If DST Overlap (Fall Back), validOffsets has 2 entries; pick the first (earlier offset)
        ZoneOffset chosenOffset = validOffsets.get(0);
        return localDateTime.atOffset(chosenOffset).toInstant();
    }
}
