//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.Set;

import lombok.Data;
import net.catenax.irs.aaswrapper.dto.AspectModel;

/**
 * AssemblyPartRelationship
 */
@Data
class AssemblyPartRelationship implements AspectModel {

    /**
     * catenaXId
     */
    private String catenaXId;

    /**
     * childParts
     */
    private Set<ChildData> childParts;
}
