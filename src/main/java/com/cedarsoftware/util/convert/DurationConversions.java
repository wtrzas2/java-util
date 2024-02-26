package com.cedarsoftware.util.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.cedarsoftware.util.CompactLinkedMap;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
final class DurationConversions {

    private DurationConversions() {}

    static Map toMap(Object from, Converter converter) {
        long sec = ((Duration) from).getSeconds();
        long nanos = ((Duration) from).getNano();
        Map<String, Object> target = new CompactLinkedMap<>();
        target.put("seconds", sec);
        target.put("nanos", nanos);
        return target;
    }

    static BigInteger toBigInteger(Object from, Converter converter) {
        Duration duration = (Duration) from;
        BigInteger epochSeconds = BigInteger.valueOf(duration.getSeconds());
        BigInteger nanos = BigInteger.valueOf(duration.getNano());

        // Convert seconds to nanoseconds and add the nanosecond part
        return epochSeconds.multiply(BigIntegerConversions.BILLION).add(nanos);
    }

    static double toDouble(Object from, Converter converter) {
        Duration duration = (Duration) from;
        return duration.getSeconds() + duration.getNano() / 1_000_000_000d;
    }

    static BigDecimal toBigDecimal(Object from, Converter converter) {
        Duration duration = (Duration) from;
        BigDecimal seconds = new BigDecimal(duration.getSeconds());
        
        // Convert nanoseconds to fractional seconds and add to seconds
        BigDecimal fracSec = BigDecimal.valueOf(duration.getNano(), 9);
        return seconds.add(fracSec);
    }
    
    static Timestamp toTimestamp(Object from, Converter converter) {
        Duration duration = (Duration) from;
        Instant epoch = Instant.EPOCH;
        Instant timeAfterDuration = epoch.plus(duration);
        return Timestamp.from(timeAfterDuration);
    }
}
