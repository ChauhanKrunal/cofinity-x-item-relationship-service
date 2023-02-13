/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.semanticshub;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Pagination wrapper Object.
 * @param <T> Type of paginated item
 */
@SuppressWarnings({ "PMD.UnusedFormalParameter" })
public class PaginatedResponse<T> extends PageImpl<T> {
    public PaginatedResponse(final @JsonProperty("items") List<T> items,
            final @JsonProperty("totalItems") int totalItems,
            final @JsonProperty("currentPage") int currentPage,
            final @JsonProperty("totalPages") int totalPages,
            final @JsonProperty("itemCount") int itemCount) {
        super(items, PageRequest.of(currentPage, itemCount), totalItems);
    }

    public PaginatedResponse(final List<T> content, final Pageable pageable, final long total) {
        super(content, pageable, total);
    }

    public PaginatedResponse(final List<T> content) {
        super(content);
    }
}
