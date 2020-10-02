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

package com.amazon.opendistroforelasticsearch.notifications.core

/**
 * Data class for storing channel message.
 */
internal data class ChannelMessage(
    val title: String,
    val textDescription: String,
    val htmlDescription: String?,
    val attachment: Attachment?
) {
    /**
     * Data class for storing attachment of channel message.
     */
    internal data class Attachment(
        val fileName: String,
        val fileEncoding: String,
        val fileData: String,
        val fileContentType: String?
    )
}
