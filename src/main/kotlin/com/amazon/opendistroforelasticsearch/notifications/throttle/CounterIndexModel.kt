/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.notifications.throttle

import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParser.Token.END_OBJECT
import org.elasticsearch.common.xcontent.XContentParser.Token.START_OBJECT
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.index.seqno.SequenceNumbers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing the doc for a day.
 */
internal data class CounterIndexModel(
    val counterDay: Date,
    val requestCount: Int,
    val emailSentSuccessCount: Int,
    val emailSentFailureCount: Int,
    val seqNo: Long = SequenceNumbers.UNASSIGNED_SEQ_NO,
    val primaryTerm: Long = SequenceNumbers.UNASSIGNED_PRIMARY_TERM
) : ToXContentObject {
    companion object {
        private const val COUNTER_DAY_TAG = "counter_day"
        private const val REQUEST_COUNT_TAG = "request_count"
        private const val EMAIL_SENT_SUCCESS_COUNT = "email_sent_success_count"
        private const val EMAIL_SENT_FAILURE_COUNT = "email_sent_failure_count"
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

        /**
         * get the ID for a given date
         * @param day the day to create ID
         * @return ID for the day
         */
        fun getIdForDate(day: Date): String {
            return DATE_FORMATTER.format(day)
        }

        /**
         * get/create Counter index model from counters
         * @param day the day to create model
         * @param counters the counter values
         * @return created counter index model
         */
        fun getCounterIndexModel(day: Date, counters: Counters): CounterIndexModel {
            return CounterIndexModel(day,
                counters.requestCount.get(),
                counters.emailSentSuccessCount.get(),
                counters.emailSentFailureCount.get())
        }

        /**
         * Parse the data from parser and create Counter index model
         * @param parser data referenced at parser
         * @param seqNo the seqNo of the document
         * @param primaryTerm the primaryTerm of the document
         * @return created counter index model
         */
        fun parse(parser: XContentParser, seqNo: Long, primaryTerm: Long): CounterIndexModel {
            var counterDay: Date? = null
            var requestCount: Int? = null
            var emailSentSuccessCount: Int? = null
            var emailSentFailureCount: Int? = null
            XContentParserUtils.ensureExpectedToken(START_OBJECT, parser.currentToken(), parser::getTokenLocation)
            while (END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    COUNTER_DAY_TAG -> counterDay = DATE_FORMATTER.parse(parser.text())
                    REQUEST_COUNT_TAG -> requestCount = parser.intValue()
                    EMAIL_SENT_SUCCESS_COUNT -> emailSentSuccessCount = parser.intValue()
                    EMAIL_SENT_FAILURE_COUNT -> emailSentFailureCount = parser.intValue()
                    else -> throw IllegalArgumentException("CounterIndexModel:Unknown field $fieldName")
                }
            }
            counterDay ?: throw IllegalArgumentException("$COUNTER_DAY_TAG field not present")
            requestCount ?: throw IllegalArgumentException("$REQUEST_COUNT_TAG field not present")
            emailSentSuccessCount ?: throw IllegalArgumentException("$EMAIL_SENT_SUCCESS_COUNT field not present")
            emailSentFailureCount ?: throw IllegalArgumentException("$EMAIL_SENT_FAILURE_COUNT field not present")
            return CounterIndexModel(counterDay,
                requestCount,
                emailSentSuccessCount,
                emailSentFailureCount,
                seqNo,
                primaryTerm)
        }
    }

    /**
     * copy/create Counter index model from this object
     * @param counters the counter values to add to this object
     * @return created counter index model
     */
    fun copyAndIncrementBy(counters: Counters): CounterIndexModel {
        return copy(
            requestCount = requestCount + counters.requestCount.get(),
            emailSentSuccessCount = emailSentSuccessCount + counters.emailSentSuccessCount.get(),
            emailSentFailureCount = emailSentFailureCount + counters.emailSentFailureCount.get()
        )
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @return created XContentBuilder object
     */
    fun toXContent(): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder? {
        if (builder != null) {
            builder.startObject()
                .field(COUNTER_DAY_TAG, getIdForDate(counterDay))
                .field(REQUEST_COUNT_TAG, requestCount)
                .field(EMAIL_SENT_SUCCESS_COUNT, emailSentSuccessCount)
                .field(EMAIL_SENT_FAILURE_COUNT, emailSentFailureCount)
                .endObject()
        }
        return builder
    }
}
