/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.ThrowableAssert;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelFacadeTest {

    @InjectMocks
    private EdcSubmodelFacade testee;

    @Mock
    private EdcSubmodelClient client;

    @Test
    void shouldThrowExecutionExceptionForRelationships() throws EdcClientException {
        // arrange
        final ExecutionException e = new ExecutionException(new EdcClientException("test"));
        final CompletableFuture<List<Relationship>> future = CompletableFuture.failedFuture(e);
        when(client.getRelationships(any(), any())).thenReturn(future);

        // act
        ThrowableAssert.ThrowingCallable action = () -> testee.getRelationships("", null);

        // assert
        assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldThrowEdcClientExceptionForRelationships() throws EdcClientException {
        // arrange
        final EdcClientException e = new EdcClientException("test");
        when(client.getRelationships(any(), any())).thenThrow(e);

        // act
        ThrowableAssert.ThrowingCallable action = () -> testee.getRelationships("", null);

        // assert
        assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldRestoreInterruptOnInterruptExceptionForRelationships()
            throws EdcClientException, ExecutionException, InterruptedException {
        // arrange
        final CompletableFuture<List<Relationship>> future = mock(CompletableFuture.class);
        final InterruptedException e = new InterruptedException();
        when(future.get()).thenThrow(e);
        when(client.getRelationships(any(), any())).thenReturn(future);

        // act
        testee.getRelationships("", null);

        // assert
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }


    @Test
    void shouldThrowExecutionExceptionForSubmodel() throws EdcClientException {
        // arrange
        final ExecutionException e = new ExecutionException(new EdcClientException("test"));
        final CompletableFuture<String> future = CompletableFuture.failedFuture(e);
        when(client.getSubmodelRawPayload(any())).thenReturn(future);

        // act
        ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelRawPayload("");

        // assert
        assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldThrowEdcClientExceptionForSubmodel() throws EdcClientException {
        // arrange
        final EdcClientException e = new EdcClientException("test");
        when(client.getSubmodelRawPayload(any())).thenThrow(e);

        // act
        ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelRawPayload("");

        // assert
        assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldRestoreInterruptOnInterruptExceptionForSubmodel()
            throws EdcClientException, ExecutionException, InterruptedException {
        // arrange
        final CompletableFuture<String> future = mock(CompletableFuture.class);
        final InterruptedException e = new InterruptedException();
        when(future.get()).thenThrow(e);
        when(client.getSubmodelRawPayload(any())).thenReturn(future);

        // act
        testee.getSubmodelRawPayload("");

        // assert
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

}