package com.cedarsoftware.util.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

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
final class BigDecimalConversions {
    static final BigDecimal GRAND = BigDecimal.valueOf(1000);

    private BigDecimalConversions() { }

    static Instant toInstant(Object from, Converter converter) {
        BigDecimal seconds = (BigDecimal) from;
        BigDecimal nanos = seconds.remainder(BigDecimal.ONE);
        return Instant.ofEpochSecond(seconds.longValue(), nanos.movePointRight(9).longValue());
    }

    static Duration toDuration(Object from, Converter converter) {
        BigDecimal seconds = (BigDecimal) from;
        BigDecimal nanos = seconds.remainder(BigDecimal.ONE);
        return Duration.ofSeconds(seconds.longValue(), nanos.movePointRight(9).longValue());
    }

    static LocalDate toLocalDate(Object from, Converter converter) {
        return toZonedDateTime(from, converter).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(Object from, Converter converter) {
        return toZonedDateTime(from, converter).toLocalDateTime();
    }

    static OffsetDateTime toOffsetDateTime(Object from, Converter converter) {
        return toZonedDateTime(from, converter).toOffsetDateTime();
    }

    static ZonedDateTime toZonedDateTime(Object from, Converter converter) {
        return toInstant(from, converter).atZone(converter.getOptions().getZoneId());
    }

    static Date toDate(Object from, Converter converter) {
        return Date.from(toInstant(from, converter));
    }

    static java.sql.Date toSqlDate(Object from, Converter converter) {
        return new java.sql.Date(toInstant(from, converter).toEpochMilli());
    }

    static Timestamp toTimestamp(Object from, Converter converter) {
        return Timestamp.from(toInstant(from, converter));
    }

    static BigInteger toBigInteger(Object from, Converter converter) {
        return ((BigDecimal)from).toBigInteger();
    }

    static String toString(Object from, Converter converter) {
        return ((BigDecimal) from).stripTrailingZeros().toPlainString();
    }

    static UUID toUUID(Object from, Converter converter) {
        BigInteger bigInt = ((BigDecimal) from).toBigInteger();
        return BigIntegerConversions.toUUID(bigInt, converter);
    }
}